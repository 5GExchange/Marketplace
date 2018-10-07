"""
Copyright 2015 Atos
Contact: Atos <javier.melian@atos.net>

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
"""

#from django.shortcuts import render
from account.models import Account, Monitor, BillingEvent, SLAInfo, SlaViolation
from account.serializers import AccountSerializer, AccountBillSerializer, AccountInstanceListSerializer, MonitorSerializer, BillingEventSerializer, SLAInfoSerializer, SlaViolationSerializer
import datetime, collections
from django.conf import settings
from rest_framework.views import APIView
from django.http import Http404
from rest_framework.response import Response
from rest_framework import status
#import eventChecker2
from slaclient import restclient 
import json, time, requests, re
import urllib2


#for the influxDB and the time series
from influxdb import InfluxDBClient
import pandas as pd
import numpy as np

# Create your views here.

DB_TIME_UNIT=settings.DB_TIME_UNIT

#SLA_URL = "http://localhost:9040"
#SLA_URL = "http://sla:9040"


class createAgreement(object):
    '''
    Arguments:
        templateId: Id of the template where the new agreement is going to be based on.
        clientId: Id of the customer to update the agreementInitiator.
        agreementId: Id of the new agreement. 
    '''

    def readTemplate(object, templateId):
        f = restclient.Factory(settings.SLA_URL)
        a_template = f.templates()
        return a_template.getbyid(templateId)[0] 

    def postAgreement(object, agreement):
        f = restclient.Factory(settings.SLA_URL)
        a_agreement = f.agreements()
        a_agreement.create(json.dumps(agreement)) 

    def __init__(self, templateId, clientId, agreementId):
        agreement = self.readTemplate(templateId)
        agreement['context']['agreementInitiator'] = clientId
        #agreement['context']['agreementResponder'] = clientId
        agreement['agreementId'] = agreementId
        #eliminate fields that are in the Template but not in the Agreement
        try: 
            del agreement['templateId']
            for el1 in agreement['terms']['allTerms']['guaranteeTerms']:
                for el2 in el1['businessValueList']['customBusinessValue']:
                    del el2['duration']
        except Exception as e:
            print "DEBUG: ", e
        #print "AGREEMENT: ", json.dumps(agreement)
        self.postAgreement(agreement)


class AccountList(APIView):
    """
    List all account entries, or create a new one.
    """
    def get(self, request, format=None):
        """
        ---
        response_serializer: AccountSerializer
        """
        accounts = Account.objects.all()
        serializer = AccountSerializer(accounts, many=True)

        return Response(serializer.data)

    def post(self, request, format=None):
        serializer = AccountSerializer(data=request.data)
        if serializer.is_valid():
            try:
                if Account.objects.filter(productType=request.data['productType'], instanceId=request.data['instanceId']):
                    print ("  [ERROR] There is an entry already with that InstanceId (%s) for the same ProductType (%s)" % (request.data['instanceId'], request.data['productType']))
                    return Response(status=status.HTTP_400_BAD_REQUEST)
                serializer.save()

                #create the SLA agreements in the SLA module based on the already created templates
                #templateId = request.data['productType']+request.data['productId']+request.data['flavour']
                templateId = request.data['productType']+ "@"+settings.DOMAIN_ID+"*"+request.data['productId']+request.data['flavour']
                createAgreement(templateId, request.data['clientId'], request.data['agreementId'])
                #start the Agreement enforcement
                f = restclient.Factory(settings.SLA_URL)
                a_enforcement = f.enforcements()
                a_enforcement.start(request.data['agreementId']) 
		#triggers IMoS monitoring sending the NS instance ID
		if (request.data['agreementId'].startswith('ns')):
                    print ("  [INFO] Contacting IMoS with url: %s" % (settings.IMOS_URL + request.data['instanceId']))
		    requests.put(str(settings.IMOS_URL) + str(request.data['instanceId']), data=None)
                    #Virtual Links agreements:


            except Exception as e:
                print "DEBUG: ", e
                return Response(status=status.HTTP_400_BAD_REQUEST)
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class AccountDetail(APIView):
    """
    Retrieve, update or delete an Accounting instance.
    """
    def get_object(self, pk):
        try:
            return Account.objects.get(pk=pk)
        except Account.DoesNotExist:
            raise Http404

    def get(self, request, pk, format=None):
        account = self.get_object(pk)
        serializer = AccountSerializer(account)
        return Response(serializer.data)

    def put(self, request, pk, format=None):    
        account = self.get_object(pk)
        serializer = AccountSerializer(account, data=request.data)
        print "request: ", request.data
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk, format=None):
        account = self.get_object(pk)
        account.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


class ServiceInstancesByClient(APIView):
    """
    2-API endpoint that list of all active services the client is using.  
        /service-instance-list/?clientId=c1
    """
    def get(self, request, format=None):
        """
        ---
        parameters:
            - name: clientId
              description: Client ID
              type: string
              paramType: form
        serializer: AccountSerializer
        """
        if self.request.query_params:
            clientId = self.request.query_params.get('clientId', None)
            if clientId:
                queryset = Account.objects.filter(status=settings.STATUS_RUNNING, productType=settings.NETWORK_SERVICE, clientId=clientId)
            else:
                return Response(status=status.HTTP_400_BAD_REQUEST)
        else:
            queryset = Account.objects.filter(status=settings.STATUS_RUNNING, productType=settings.NETWORK_SERVICE)
        serializer = AccountSerializer(queryset, many=True)
        return Response(serializer.data)


class VNFByClient(APIView):
    """
    4- API endpoint that lists all VNFs purchased by a particular provider (client)
      /vnf-list/?clientId=p1
    """
    def get(self, request, format=None):
        """
        ---
        parameters:
            - name: clientId
              description: Client ID
              type: string
              paramType: form
        serializer: AccountSerializer
        """
        clientId = self.request.query_params.get('clientId', None)
        queryset = Account.objects.filter(productType=settings.VNF, clientId=clientId)
        serializer = AccountSerializer(queryset, many=True)
        return Response(serializer.data)


class VNFBillingModelByClient(APIView):
    """
    5- API endpoint that gives details of the revenue sharing model between SP and FP for the given VNF instance
        /vnf-billing-model/?spId=p1&instanceId=vnfid1
    """
    def get(self, request, format=None):
        """
        ---
        parameters:
            - name: spId
              description: service provider ID
              type: string
              paramType: form
            - name: instanceId
              description: vnf instance ID
              type: string
              paramType: form
        serializer: AccountBillSerializer
        """
        clientId = self.request.query_params.get('spId', None)
        instanceId = self.request.query_params.get('instanceId', None)
        queryset = Account.objects.filter(productType=settings.VNF, clientId=clientId, instanceId=instanceId)
        serializer = AccountBillSerializer(queryset, many=True)
        return Response(serializer.data)


class ServiceBillingModelByClient(APIView): 
    """
    1- API endpoint that gives details about the user's chosen billing model and specs. 
    for the queried service instance id, must have billing cycle start date (for subscription model)* 
        /service-billing-model/?clientId=c1&instanceId=s1
    """
    def get(self, request, format=None):
        """
        ---
        parameters:
            - name: clientId
              description: Client ID
              type: string
              paramType: form
            - name: instanceId
              description: Service instance ID
              type: string
              paramType: form
        serializer: AccountBillSerializer
        """
        clientId = self.request.query_params.get('clientId', None)
        instanceId = self.request.query_params.get('instanceId', None)
        queryset = Account.objects.filter(productType=settings.NETWORK_SERVICE, clientId=clientId, instanceId=instanceId)
        if queryset:
            queryset = queryset[0]
        serializer = AccountBillSerializer(queryset, many=False)
        return Response(serializer.data)


class SlaVNFViolations(APIView):
    """
    7- API endpoint that returns the list of all SLA violations for a given VNF instance for the queried time window
        /sla/vnf-violation/{?instanceId=id02&metric=drops_per_sec}

    instanceId -- VNF instance ID
    metric -- Metric name
    """

    def get(self, request, format=None):
        """
        ---
        parameters:
            - name: instanceId
              description: VNF instance ID
              type: string
              paramType: form
            - name: metric
              description: Metric name
              type: string
              paramType: form
        """
        f = restclient.Factory(settings.SLA_URL)
        instanceId = self.request.query_params.get('instanceId', None)
        agreementId = 'unknown'
        if instanceId is not None:
            print(instanceId)
            #find all the service running instances with the given Service instance ID
            queryset = Account.objects.filter(instanceId=instanceId, productType=settings.VNF)#, status='running')
            if queryset:
                for obj in queryset:
                    agreementId = obj.agreementId
        metric = self.request.query_params.get('metric', None)
        start = self.request.query_params.get('start', None)
        end = self.request.query_params.get('end', None)
         
        print ('agreementID: %s, metric: %s, start: %s, end: %s' % (agreementId, metric, start, end))
        a_client = f.penalties()
        penalties = a_client.getbyagreementTermAndDates(agreementId, metric, start, end)[0]
        return Response(penalties)


class SlaServiceViolations(APIView):
    """
    6- API endpoint that returns the list of all SLA violations for a given servicie instance for the queried time window
        /sla/service-violation/{?instanceId=id02&metric=drops_per_sec}
    """

    def get(self, request, format=None):
        """
        ---
        parameters:
            - name: instanceId
              description: Service instance ID
              type: string
              paramType: form
            - name: metric
              description: Metric name
              type: string
              paramType: form
        """
        print "debug: ", settings.SLA_URL
        f = restclient.Factory(settings.SLA_URL)
        instanceId = self.request.query_params.get('instanceId', None)
        agreementId = 'unknown'
        if instanceId is not None:
            print(instanceId)
            #find all the service running instances with the given Service instance ID
            queryset = Account.objects.filter(instanceId=instanceId, productType=settings.NETWORK_SERVICE)#, status='running')
            if queryset:
                for obj in queryset:
                    agreementId = obj.agreementId
        metric = self.request.query_params.get('metric', None)
        start = self.request.query_params.get('start', None)
        end = self.request.query_params.get('end', None)
         
        print ('agreementID: %s, metric: %s, start: %s, end: %s' % (agreementId, metric, start, end))
        a_client = f.penalties()
        penalties = a_client.getbyagreementTermAndDates(agreementId, metric, start, end)[0]
        #print (violations)
        return Response(penalties)


class ServiceList(APIView):
    """
    8- API endpoint that returns the list of running services that use an specified VNF
        /service-list/?vnfId=vnf1
    """

    def get(self, request, format=None):
        """
        ---
        parameters:
            - name: vnfId
              description: VNF ID
              type: string
              paramType: form
        serializer: AccountSerializer
        """
        if self.request.query_params:
            vnfId = self.request.query_params.get('vnfId', None)
            if vnfId is not None:
                #find all the VNF running instances with the given VNF ID
                queryset = Account.objects.filter(productId=vnfId, productType=settings.VNF, status=settings.STATUS_RUNNING)
                serviceListing = list()
                try:
                    for obj in queryset:
                        if (obj.relatives) is not None:
                            #find all the running services that use that VNF
                            serviceListing.append(Account.objects.filter(status=settings.STATUS_RUNNING, productType=settings.NETWORK_SERVICE, instanceId=obj.relatives)[0])
                    #remove duplicates
                    serviceListing= sorted(set(serviceListing))
                    serializer = AccountSerializer(serviceListing, many=True)
                    print "vnfid: ", vnfId
                    return Response(serializer.data)
                except Exception:
                    print " [ERROR] Database malformed: the service does not exist."
                    #raise
                    return Response(status=status.HTTP_400_BAD_REQUEST)
            else:
                return Response(status=status.HTTP_400_BAD_REQUEST)
                
        serializer = AccountSerializer(Account.objects.filter(productType=settings.VNF, status=settings.STATUS_RUNNING), many=True)
        return Response(serializer.data)#, status=status.HTTP_400_BAD_REQUEST)


class VnfInstanceList(APIView):
    """
    9- API endpoint that returns the list of VNF instance IDs that belong to a given service instanceID
        /vnf-instance-list/?sInstanceId=s1
    """

    def get(self, request, format=None):
        """
        ---
        parameters:
            - name: sInstanceId
              description: Service instance ID
              type: string
              paramType: form
        serializer: AccountInstanceListSerializer
        """
        sInstanceId = self.request.query_params.get('sInstanceId', None)
        if sInstanceId is not None:
            #get the entry that corresponds to that service instance ID
            queryset = Account.objects.filter(instanceId=sInstanceId, productType=settings.NETWORK_SERVICE)
            if queryset:
                resultsList = list()
                try:
                    vnfList = queryset[0].relatives.replace(" ", "").split(",")
                    for vnf in vnfList:
                        #find all the VNFs in the list
                        resultsList.append(Account.objects.filter(productType=settings.VNF, instanceId=vnf)[0])
                except Exception as e:
                    #print "DEBUG: ", e
                    print " [ERROR] Database malformed: the service does not contain any functions."
                    raise
                finally:
                    serializer = AccountInstanceListSerializer(resultsList, many=True)
                    return Response(serializer.data)
            serializer = AccountInstanceListSerializer(queryset, many=True)
            return Response(serializer.data)#, status=status.HTTP_400_BAD_REQUEST)
        return Response(status=status.HTTP_400_BAD_REQUEST)




class updateServiceStatus(APIView): 
    """
    API endpoint to update the status of a Service given the instanceID and the new status
        /servicestatus/<instance-n>/<new_status>
    """
    def startEnforcement(self, agreementId):
        f = restclient.Factory(settings.SLA_URL)
        a_enforcement= f.enforcements()
        a_enforcement.start(agreementId) 

    def stopEnforcement(self, agreementId):
        f = restclient.Factory(settings.SLA_URL)
        a_enforcement= f.enforcements()
        a_enforcement.stop(agreementId) 

    def stopVNF(self, vnfInstance, jsonStatus, new_status):
        vnfquery = Account.objects.filter(productType=settings.VNF, instanceId=vnfInstance) 
        vnf = vnfquery[0]
        vnfSerializer = AccountSerializer(vnf, many=False, data=json.loads(jsonStatus))
        if vnfSerializer.is_valid():
            #print "VNF2: ", json.dumps(vnfserializer.data)
            vnfSerializer.save()
            #Send the message to the billing module
            #send_msg(json.dumps(vnfSerializer.data))
            #start/stop the SLA enforcement accordingly
            if (new_status == settings.STATUS_RUNNING):
                self.startEnforcement(vnf.agreementId)
            if (new_status == settings.STATUS_STOPPED):
                self.stopEnforcement(vnf.agreementId)


    def post(self, request, ns_instance, new_status, format=None):
        """
        ---
        response_serializer: AccountSerializer
        """
        queryset = Account.objects.filter(productType=settings.NETWORK_SERVICE, instanceId=ns_instance)
        if queryset.exists():
            jsonStatus='{"status":"' + new_status + '"}' 
            service = queryset[0]
            serializer = AccountSerializer(service, data=json.loads(jsonStatus))
            try:
                if serializer.is_valid():
                    #saves the new status
                    serializer.save()
                    print "status changed "
                    #start/stop the SLA enforcement accordingly
                    if (new_status == settings.STATUS_RUNNING):
                        self.startEnforcement(service.agreementId)
                    if (new_status == settings.STATUS_STOPPED):
                        self.stopEnforcement(service.agreementId)

                    #prepares and sends the message to the queue
                    message = {}
                    message['instanceId'] = ns_instance
                    message['event'] = new_status
                    #print (json.dumps(serializer.data))
                    #send_msg(json.dumps(serializer.data))

                    #update the status of the participant VNFs
                    vnf_list = service.relative_instances.replace(" ", "").split(",")
                    print "VNF list: ", vnf_list
                    for vnfInstance in vnf_list:
                        time.sleep(1)
                        print "VNF: ", vnfInstance
                        self.stopVNF(vnfInstance, jsonStatus, new_status)
                    return Response(serializer.data, status=status.HTTP_201_CREATED)
            except ReferenceError:
                print "  [ERROR] Could not update the messages queue"
                return Response(status=status.HTTP_408_REQUEST_TIMEOUT)
            except Exception as e:
                print "  [ERROR] ", e
                return Response(status=status.HTTP_400_BAD_REQUEST)
        return Response(queryset, status=status.HTTP_404_NOT_FOUND)


class updateVNFStatus(APIView): 
    """
    API endpoint to update the status of a VNF given the instanceID and the new status
        /vnfstatus/<instance-n>/<new_status>
    """
    def get(self, request, vnf_instance, new_status, format=None):
        queryset = Account.objects.filter(productType=settings.VNF, instanceId=vnf_instance)
        if queryset.exists():
            jsonStatus='{"status":"' + new_status + '"}' 
            for account in queryset:
                serializer = AccountSerializer(account, data=json.loads(jsonStatus))
                try:
                    if serializer.is_valid():
                        #saves the new status
                        serializer.save()
                        print "status changed "
                        #prepares and sends the message to the queue
                        message = {}
                        message['instanceId'] = vnf_instance
                        message['event'] = new_status
                        #send_msg(json.dumps(serializer.data))
                        return Response(serializer.data, status=status.HTTP_201_CREATED)
                except ReferenceError:
                    print "  [ERROR] Could not update the messages queue"
                    return Response(status=status.HTTP_408_REQUEST_TIMEOUT)
                except Exception:
                    return Response(status=status.HTTP_400_BAD_REQUEST)
        return Response(queryset, status=status.HTTP_404_NOT_FOUND)


class SLAInformation(APIView): 
    """
    API endpoint that gives details about the services purchased by a certain user. 
        /sla-info/?clientId=c1&kind=ns
    """
    def get(self, request, format=None):
        """
        ---
        parameters:
            - name: clientId
              description: Client ID
              kind: string
              paramType: form
            - name: type
              description: Type of the SLA information to be returned: ns (for the customer) or vnf (for the SP)
              type: string
              paramType: form
        serializer: SLAInfoSerializer
        """
        clientId = self.request.query_params.get('clientId', None)
        kind = self.request.query_params.get('kind', None)
        print "params: ", clientId, kind
        if (clientId is not None) and (kind is not None):
            f = restclient.Factory(settings.SLA_URL)
            queryset = Account.objects.filter(productType=kind, clientId=clientId)
            if queryset:
                resultsList = list()
                try:
                    for service in queryset:
                        a_client = f.violations()
                        violations = a_client.getbyagreement(service.agreementId)[0]
                        element = {}
                        element['productId'] = service.productId
                        element['productType'] = service.productType
                        element['clientId'] = service.clientId
                        element['providerId'] = service.providerId
                        element['SLAPenalties'] = len(violations)
                        element['agreementId'] = service.agreementId
                        element['dateCreated'] = service.dateCreated
                        if service.status == settings.STATUS_RUNNING:
                            element['dateTerminated'] = None
                        else:
                            element['dateTerminated'] = datetime.datetime.now()
                            
                        resultsList.append(element)
                except Exception as e:
                    print "DEBUG: ", e
                    raise
                finally:
                    serializer = SLAInfoSerializer(resultsList, many=True)
                    return Response(serializer.data)
            serializer = SLAInfoSerializer(queryset, many=True)
            return Response(serializer.data)#, status=status.HTTP_400_BAD_REQUEST)
        return Response(status=status.HTTP_400_BAD_REQUEST)


'''
================ DASHBOARD API ====================
'''

class DashboardServiceList(APIView):
    """
    API endpoint that returns the list of all active services the user is using.  
        /servicelist/<userId>
    """
    def get(self, request, userId, format=None):
        """
        ---
        parameters:
            - name: userId
              description: User ID
              type: string
              paramType: form
        serializer: AccountSerializer
        """
        queryset = Account.objects.filter(status=settings.STATUS_RUNNING, productType=settings.NETWORK_SERVICE, clientId=userId)
        serializer = AccountSerializer(queryset, many=True)
        return Response(serializer.data)


class DashboardVNFList(APIView):
    """
    API endpoint that returns the list of all active VNFs the user is using.  
        /vnflist/<userId>
    """
    def get(self, request, userId, format=None):
        """
        ---
        parameters:
            - name: userId
              description: User ID
              type: string
              paramType: form
        serializer: AccountSerializer
        """
        queryset = Account.objects.filter(status=settings.STATUS_RUNNING, productType=settings.VNF, clientId=userId)
        serializer = AccountSerializer(queryset, many=True)
        return Response(serializer.data)





'''
================ AGGREGATOR API ====================
'''

class getLocalMonitoring(object):
    """
    Returns the local monitoring information for a given kpi shrinking it to a 'max_values' number of values. How this shrinking process is done depends on the aggregation operator that will be applied later on to the aggregated monitoring.
    """
    def __new__(cls, instanceId, kpi, operator, start, end, max_values):
        '''
        ---
        Arguments:
            -instanceId:
            -kpi:
            -operator: Operator that will be applied on the global aggregation
            -start: start time (UNIX timestamp)
            -end: end time (UNIX timestamp)
            -max_values: maximum number of values to be returned in the sample
        Examle:  
            getLocalMonitoring('service01', 'cpu_load', 'MAX', 1254055562000000000, 1454055562000000000, 5)
        Response example:(JSON object)                    
            [
                {
                    "value": 0.76,
                    "time": 1.434055892e+18
                }
            ]
        '''
        if end <= start:
            print "   [DEBUG] Aggregator: Time frame not valid"
            return []#, status.HTTP_400_BAD_REQUEST

        client = InfluxDBClient('influxdb', settings.INFLUXDB_PORT, settings.INFLUXDB_USR, settings.INFLUXDB_PASS, settings.INFLUXDB_NAME)
        #client = InfluxDBClient('localhost', 8086, 'root', 'root', 'fgx')
        if start and end:
            query = "SELECT time,value FROM " + kpi + " WHERE resourceid='" + instanceId + "' and time>=" + str(start) + DB_TIME_UNIT + " and time <=" + str(end) + DB_TIME_UNIT
        else:
            query = "SELECT time,value FROM " + kpi + " WHERE resourceid='" + instanceId + "'"
        # Execute the query getting the timestamp values in nano seconds
        df = pd.DataFrame(client.query(query, epoch=DB_TIME_UNIT, chunked=True, chunk_size=10000).get_points())
        try:
            print "  [INFO] Retrieving data from database (%s) from %s to %s - Sample size: %d" % (settings.INFLUXDB_NAME, start, end, df['time'].count())
            # typecast the parameters
            start = long(start)
            end = long(end)
            max_values = long(max_values)
            interval = (end - start)/max_values
            # group the series by intervals of size 'interval' and apply the operator to that interval
            if operator == 'MAX':
                df = df.groupby(pd.cut(df['time'], np.arange(start-1, end+interval+1, interval))).max()
            elif operator == 'MIN':
                df = df.groupby(pd.cut(df['time'], np.arange(start-1, end+interval+1, interval))).min()
            elif operator == 'SUM':
                df = df.groupby(pd.cut(df['time'], np.arange(start-1, end+interval+1, interval))).mean()
            elif operator == 'AVG':
                df = df.groupby(pd.cut(df['time'], np.arange(start-1, end+interval+1, interval))).mean()
            else:
                print "  [ERROR] Operator (%s) not valid" % (operator)
                return []#, status.HTTP_400_BAD_REQUEST
            # Keep only the rows with at least 1 non-nan values:
            df = df.dropna(thresh=1)
            # dataframe with timestamp and value of the requested kpi converted to json format
            print "     [INFO] Sample size after shrinkage: %d" % (df['time'].count())
        except KeyError as e:
            print "   [DEBUG] Aggregator: No Values were found in the DB for these parameters: [instanceId: %s, KPI: %s, Start: %s, End: %s ] - %s" % (instanceId, kpi, str(start), str(end), e)
            return []#, status.HTTP_200_OK
        except Exception as e:
            print "   [DEBUG] Aggregator: ", e
            return []#, status.HTTP_400_BAD_REQUEST
        result = json.loads(df.to_json(orient='records'))
        return result#, status.HTTP_200_OK



class LocalMonitoring(APIView):
    """
    API endpoint that returns the monitoring information of a time frame of a given KPI that belongs to a given running instance. Only a certain number of values is returned to avoid overload.
    Format: 
        /localmonitoring/?instanceId=<instanceId>&kpi=<kpi>&operator=<operator>&start=<start time in ns>&end=<end time in ns>&max_values=<max values>
    Example:
        /localmonitoring/?instanceId=service102&kpi=cpu_load&operator=MAX&start=1434055171000000000&end=1434055992000000000&max_values=5
    """
    def get(self, request, format=None):
        '''
        Arguments:
            -instanceId: Id of the instance
            -kpi: name of the KPI
            -operator: Operatior that will be applied to the series when shrinking it
            -start: start time (UNIX timestamp)
            -end: end time (UNIX timestamp)
            -max_values: max. number of valuues to be returned
        Response example:                      
            [
                {
                    "value": 0.76,
                    "time": 1.434055892e+18
                }
            ]
        '''
        instanceId = self.request.query_params.get('instanceId', None)
        kpi = self.request.query_params.get('kpi', None)
        operator = self.request.query_params.get('operator', None)
        start = self.request.query_params.get('start', None)
        end = self.request.query_params.get('end', None)
        max_values = self.request.query_params.get('max_values', 5)
        '''
        print "instance: ", instanceId
        print "kpi: ", kpi
        print "start: ", start
        print "end: ", end
        print "values: ", max_values
        '''
        #monitoring, status = getLocalMonitoring (instanceId, kpi, operator, start, end, max_values)
        monitoring = getLocalMonitoring (instanceId, kpi, operator, start, end, max_values)

        return Response(monitoring)#, status)



class AggregateMonitoring(APIView):
    """
    API endpoint that gathers and aggregates the monitoring information within a time frame of a given KPI that belongs to a given SLA agreement. Only a certain number of values is returned to avoid overload. 
    Format:
        /aggregate/?agreementId=<agreement ID>&kpi=<KPI>&formula=<aggregation formula>&start=<start time>&end=<end time>&max_values=<max values>
    Example:
        /aggregate/?agreementId=test_ns_gold_instance101&kpi=cpu_load&formula=AVG(vnf:testvnf1-0@ATOS*cpu_load,%20vnf:testvnf2-0@ATOS*cpu_load)&start=1434055171000000000&end=1434055893000000000&max_values=5
    """
    def getDomainByLocation (self, location):
        #Get domain the location (domainId)
        domain = requests.get(str(settings.MDC_URL) + '/domain/' + str(location) + '/')
        return domain.json()
    
    def parse_formula (self, formula):
        # formula sample: "MAX(vnf:9-0@01*cpu,vnf:8-0@01*cpu,vnf:8-1@01*cpu)"
        # remove all spaces in the formula
        formula = formula.replace(" ", "")
        # take what is between '(',')' and split it groups by ','
        items = formula.partition('(')[-1].rpartition(')')[0].split(',') # ['vnf:9-0@01*cpu', 'vnf:8-0@01*cpu', 'vnf:8-1@01*cpu']
        # get operator
        operator = formula.split('(')[0]
        return operator, items
        
    def get_vnf_entry (self, parent, vnf):
        # vnf:9-0@01*cpu
        delimiters = ":", "-", "@", "*"
        try:
            regexPattern = '|'.join(map(re.escape, delimiters))
            element = re.split(regexPattern, str(vnf))
            # ['vnf', '9', '0', '01', 'cpu'] --> [item type, item ID, instance number within the NS, domain ID, kpi]
            vnf_entry = Account.objects.filter(productId=element[1], relative_instances=parent.instanceId)
        except Exception as e:
            print "     [ERROR] Malformed formula - %s" % (e)
            return None, None
        # if more than one entry is retrieved, pick the one indicated by the instance number within the NS: element[2]
        return vnf_entry[int(element[2])], element[4]

    def get_vnf_metric_monitoring (self, instanceId, kpi, location, operator, start, end, max_values):
        #domain = self.getDomainByLocation(location)
        if True: #domain['localDomain']:
            #print "     [INFO] The KPI (%s) belongs to the local domain: %s" % (kpi, domain['domain'])
            print "     [INFO] The KPI (%s) belongs to the local domain" % (kpi)
            monitoring = getLocalMonitoring (instanceId, kpi, operator, start, end, max_values)
        else:
            # retrieve the monitoring from the remote domain
            print "     [INFO] The KPI (%s) belongs to a remote domain: %s" % (kpi, domain['domain'])
            monitoring = requests.get(str(domain['entryPoint']) + settings.AGGREGATOR_EP + '/localmonitoring/?instanceId='+ instanceId +'&kpi='+ kpi +'&operator='+ operator +'&start=' + str(start) + '&end='+ str(end) +'&max_values='+ str(max_values))
        #print monitoring
        return monitoring

    def aggregate (self, series, operator, start, end, max_values):
        #print "series: ", series 
        print "  [INFO] Aggregating: Initial sample size: %d" % (len(series))
        df = pd.read_json(json.dumps(series))
        start = long(start)
        end = long(end)
        interval = long(end - start)/long(max_values)
        # group the series by intervals of size 'interval' and apply the operator to that interval
        if operator == 'MAX':
            df = df.groupby(pd.cut(df['time'], np.arange(start-1, end+interval+1, interval))).max()
        elif operator == 'MIN':
            print "min"
            df = df.groupby(pd.cut(df['time'], np.arange(start-1, end+interval+1, interval))).min()
        elif operator == 'SUM':
            df = df.groupby(pd.cut(df['time'], np.arange(start-1, end+interval+1, interval))).sum()
        elif operator == 'AVG':
            df = df.groupby(pd.cut(df['time'], np.arange(start-1, end+interval+1, interval))).mean()
        else:
            print "  [ERROR] Operator (%s) not valid" % (operator)
            return status.HTTP_400_BAD_REQUEST
        #Keep only the rows with at least 1 non-nan values:
        df = df.dropna(thresh=1) 
        print "  [INFO] Aggregating: Final sample size: %d" % (df['time'].count())
        return json.loads(df.to_json(orient='records'))


    def get(self, request, format=None):
        """
        ---
        parameters:
            - name: agreementId
              description: ID of the SLA agreement
              kind: string
            - name: kpi
              description: name of the KPI to aggregate the monitoring information
              type: string
            - name: formula
              description: Formula to aggregate/calcuate the KPI monitoring
              kind: string
            - name: start
              description: start timestamp
              kind: string
            - name: end
              description: End timestamp
              kind: string
            - name: max_values
              description: Maximum number of values to be returned
              kind: int
        serializer: None
        """

        #variable initialisation
        agreementId = self.request.query_params.get('agreementId', None)
        kpi = self.request.query_params.get('kpi', None)
        formula = self.request.query_params.get('formula', None)
        start = self.request.query_params.get('start', None)
        end = self.request.query_params.get('end', None)
        max_values = self.request.query_params.get('max_values', 5)

        formula = urllib2.unquote(formula).decode('utf8')

        print "instancia: ", agreementId 
        print "kpi: ", kpi
        print "formula: ", formula
        print "start: ", start
        print "end: ", end
        print "values: ", max_values
        if (agreementId is not None) and (kpi is not None) and (formula is not None):
            # Retrieve the accounting entry with the AgreementId
            item = Account.objects.filter(agreementId=agreementId)
            if item:
                item = item[0]
            else:
            	print "  [ERROR] SLA agreement (%s) doesn't exist" % (agreementId)
                return Response(status=status.HTTP_400_BAD_REQUEST)
            if item.productType == settings.VNF:
            	print "  [INFO] VNF KPI monitoring request"
            	return Response(self.get_vnf_metric_monitoring (item.instanceId, kpi, item.location, 'AVG', start, end, max_values))
            else:
            	print "  [INFO] NS KPI monitoring request"
                operator, vnfs_list = self.parse_formula(formula)
            	print "  [INFO] Found %d components in the formula. Applying operator: %s" % (len(vnfs_list), operator)
                series = []
                try:
                    for vnf in vnfs_list:
            	        print "  [INFO] Analysing component: %s" % (vnf)
                        vnf_entry, vnf_kpi = self.get_vnf_entry (item, vnf)
                        if not vnf_entry:
                            raise ValueError('Entry not found in the Accounting DB')
                        partial_monitoring = self.get_vnf_metric_monitoring (vnf_entry.instanceId, vnf_kpi, vnf_entry.location, operator, start, end, max_values)
                        # merge the partial monitoring with the previous ones
                        series = series + partial_monitoring
                    # apply aggregation formula (operator)
                    result = self.aggregate(series, operator, start, end, max_values)
                except ValueError as e:
                    print "     [ERROR] Due to a malformed formula:  %s" % (e)
                    return Response(None, status.HTTP_400_BAD_REQUEST)
                except Exception as e:
                    print "     [ERROR] %s" % (e)
                    return Response(None, status.HTTP_400_BAD_REQUEST)
                print "   [INFO] Results: ", json.dumps(result)
                return Response(result)

        print "  [ERROR] Parameters 'agreementId', 'kpi' and 'formula' cannot be Null"
        return Response(status=status.HTTP_400_BAD_REQUEST)

