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
from mdc.models import Mdc, Domain, DomainShare
from mdc.serializers import MdcSerializer, DomainSerializer, MdCSharedSerializer, SharedSerializer
from django.conf import settings
from rest_framework.views import APIView
from django.http import Http404
from rest_framework.response import Response
from rest_framework import status
import json, requests
from slaclient import restclient 


class createTemplate(object):
    '''
    Arguments:
        templateId: Id of the template where the new agreement is going to be based on.
        clientId: Id of the customer to update the agreementInitiator.
        agreementId: Id of the new agreement. 
    '''

    def readTemplate(object, templateId, sla_url):
        f = restclient.Factory(sla_url)
        a_template = f.templates()
        return a_template.getbyid(templateId)[0] 

    def postTemplate(object, template, sla_url):
		f = restclient.Factory(sla_url)
		a_template = f.templates()
		a_template.create(json.dumps(template)) 

    def __init__(self, templateId, providerDomain, localDomain):
        template = self.readTemplate(templateId, providerDomain.entryPoint+":9040")
        template['context']['agreementInitiator'] = localDomain.domain
        template['context']['agreementResponder'] = providerDomain.domain
        #template['templateId'] = templateId + providerDomain.domain
        #eliminate fields that are in the Template but are not valid
        try: 
            for el1 in template['terms']['allTerms']['guaranteeTerms']:
                for el2 in el1['businessValueList']['customBusinessValue']:
                    del el2['duration']
        except Exception as e:
            print "DEBUG: ", e
        self.postTemplate(template, localDomain.entryPoint+":9040")


class createProvider(object):

    def readProvider(object, providerId, sla_url):
        f = restclient.Factory(sla_url)
        a_provider = f.providers()
        return a_provider.getbyid(providerId)[0] 

    def postProvider(object, provider, sla_url):
		f = restclient.Factory(sla_url)
		a_provider = f.providers()
		a_provider.create(json.dumps(provider)) 

    def __init__(self, providerId, localDomain):
        provider = {}
        provider['uuid'] = providerId
        provider['name'] = providerId
        self.postProvider(provider, localDomain.entryPoint+":9040")


class MdcList(APIView):
    def parse_vnfd (self, descriptor):
        item = {}
        localDomain = Domain.objects.get(localDomain=True)
        item['domainId'] = localDomain.domain
        item['productType'] = settings.VNF
        item['productId'] = descriptor['id']
        item['name'] = descriptor['name']
	item['kind'] = descriptor['type']
        item['provider'] = descriptor['provider']
        item['description'] = descriptor['description']
	print "lkink: ", localDomain.entryPoint
        item['descriptorLink'] = localDomain.entryPoint + settings.NFS_VNFD_EP + "/" + str(descriptor['id'])
        item['imageLink'] = descriptor['vdu'][0]['vm_image']
        sla = descriptor['deployment_flavours'][0]
        sla_str = ""
        for kpi in sla['assurance_parameters']:
            sla_str += " - " + kpi['formula'] + kpi['unit'] + " - Penalty: " + str(kpi['penalty']['expression']) + kpi['penalty']['unit'] + " " + kpi['penalty']['type'] +" for "+ kpi['penalty']['validity'] +" - "+ str(kpi['violation'][0]['breaches_count']) +" breaches in " + str(kpi['violation'][0]['interval'])+ "secs.\n"
        item['sla'] = sla_str
        price_str = descriptor['billing_model']
	item['billing_model'] = price_str['model']
	item['period'] = price_str['period']
	item['price_per_period'] = price_str['price']['max_per_period']
	item['price_setup'] = price_str['price']['setup']
	item['currency'] = price_str['price']['unit']
        item['sharedWith'] = []
        print "JSON: ", json.dumps(item)

        return item

    def parse_nsd (self, descriptor):
        item = {}
        localDomain = Domain.objects.get(localDomain=True)
        item['domainId'] = localDomain.domain
        item['productType'] = settings.NETWORK_SERVICE
        item['productId'] = descriptor['id']
	item['kind'] = 'Generic'
        item['name'] = descriptor['name']
        item['provider'] = "Domain " + localDomain.domain
        item['description'] = descriptor['description']
        item['descriptorLink'] = localDomain.entryPoint + settings.BSC_EP + "/" + descriptor['id']
        item['imageLink'] = None
        sla = descriptor['sla'][0]
        sla_str = ""
        for kpi in sla['assurance_parameters']:
            sla_str += " - " + kpi['name'] + " " + kpi['value'] + kpi['unit'] + " - Penalty: " + str(kpi['penalty']['value']) + kpi['penalty']['unit']  + " " + kpi['penalty']['type'] +" for "+ kpi['penalty']['validity'] +" - "+ str(kpi['violations'][0]['breaches_count']) +" breaches in " + str(kpi['violations'][0]['interval'])+ "secs.\n"
        item['sla'] = sla_str
        price_str = sla['billing']
        #item['price'] = "Model: " + price_str['model'] + " -  Period: P1M / " + str(price_str['price']['price_per_period']) + price_str['price']['unit'] + " + " + str(price_str['price']['setup']) + price_str['price']['unit'] + " setup cost"

	item['billing_model'] = price_str['model']
	item['period'] = "P1M"
	item['price_per_period'] = price_str['price']['price_per_period']
	item['price_setup'] = price_str['price']['setup']
	item['currency'] = price_str['price']['unit']
        item['sharedWith'] = []
        print "JSON: ", json.dumps(item)

        return item

    def parse_exchange (self, descriptor):
        item = {}
        localDomain = Domain.objects.get(localDomain=True)
        item['domainId'] = descriptor['domainId']
        item['productType'] = descriptor['productType']
        item['productId'] = descriptor['productId']
        item['name'] = descriptor['name']
        item['provider'] = descriptor['provider']
        item['kind'] = descriptor['kind']
        item['description'] = descriptor['description']
        item['descriptorLink'] = descriptor['descriptorLink']
        item['imageLink'] = descriptor['imageLink']
        item['sla'] = descriptor['sla']
	item['billing_model'] = descriptor['billing_model']
        item['period'] = descriptor['period']
        item['price_per_period'] = descriptor['price_per_period']
        item['price_setup'] = descriptor['price_setup']
        item['currency'] = descriptor['currency']
        item['sharedWith'] = descriptor['sharedWith']
	if (('sharedWith' in item) and (str(descriptor['domainId']) != str(localDomain.domain))):
	    #control that external items can't have sharing preferences
            item['sharedWith'] = descriptor['sharedWith']
	else:
            item['sharedWith'] = []
        item['sharedWithAll'] = False
	
        print "JSON: ", json.dumps(item)

        return item


    """
    List all mdc entries, or create a new one.
    """
    def get(self, request, format=None):
        """
        ---
        response_serializer: MdcSerializer
        """
        mdcs = Mdc.objects.all()
        serializer = MdcSerializer(mdcs, many=True)

        return Response(serializer.data)

    def post(self, request, format=None):
        if 'nsd' in request.data:
	    print "INFO MDC: adding new NS"
            item = self.parse_nsd(request.data['nsd'])
        elif 'vnfd' in request.data:
            print "INFO MDC: adding new VNF"
            item = self.parse_vnfd(request.data['vnfd'])
        else:
            print "INFO MDC: Adding an external item"
            try:
                item = self.parse_exchange(request.data)
                #Retrieve original SLA template considering: domainId, productType and productId
                print "Domain: ", item['domainId']
                originDomain = Domain.objects.get(domain=item['domainId'])
                localDomain = Domain.objects.get(localDomain=True)
                if originDomain:
                    if item['productType'] == "vnf":
                        originTemplateId = "vnf@"+str(originDomain.domain)+"*"+str(item['productId'])+"gold"
                        print "VNF Template ID: ", originTemplateId
                        createTemplate(originTemplateId, originDomain, localDomain)
                    else:
                        originTemplateId = "ns@"+str(originDomain.domain)+"*"+str(item['productId'])
                        print "NS Template ID: ", originTemplateId
                        createTemplate(originTemplateId, originDomain, localDomain)
                        #print json.dumps(a_template.getbyid("ns"+item['productId'])[0])
                else: 
                    print "EMPTY QUERY"
            except Exception as e:
                print "DEBUG MDC: ", e
                return Response(status=status.HTTP_400_BAD_REQUEST)

        serializer = MdcSerializer(data=item)
        if serializer.is_valid():
            try:
                if Mdc.objects.filter(productType=item['productType'], domainId=item['domainId'], productId=item['productId']):
                    print ("  [ERROR] There is an entry already with that productId (%s) for the same ProductType (%s) and the same domain (%s)" % (item['productId'], item['productType'], item['domainId']))
                    return Response(status=status.HTTP_409_CONFLICT)
                serializer.save()

            except Exception as e:
                print "DEBUG MDC: ", e
                return Response(status=status.HTTP_400_BAD_REQUEST)
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class MdcDetail(APIView):
    #Delete VNFD from NFS
    def get_call_url(self, endpoint):
        protocol = 'https' if settings.NFS_USE_HTTPS else 'http'
        return '%s://%s:%s/%s' % (protocol, settings.NFS_HOST, settings.NFS_PORT, endpoint.strip('/'))

    def delete_vnfd(self, vnfd_id):
        endpoint = 'NFS/vnfds/%s' % vnfd_id

        req = self.get_call_url(endpoint)
        r = requests.delete(req)
        print "request: ", r
        return r.status_code, r.text
    
    """
    Retrieve, update or delete an Mdcing instance.
    """
    def get_object(self, pk):
        try:
            return Mdc.objects.get(pk=pk)
        except Mdc.DoesNotExist:
            raise Http404

    def get(self, request, pk, format=None):
        mdc = self.get_object(pk)
        serializer = MdcSerializer(mdc)
        return Response(serializer.data)

    def put(self, request, pk, format=None):    
        mdc = self.get_object(pk)
        serializer = MdcSerializer(mdc, data=request.data)
        print "request: ", request.data
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk, format=None):
        mdc = self.get_object(pk)
        print "vnfd: ", mdc.productId
        try: 
            #r = self.delete_vnfd(mdc.productId)
  	    mdc.delete()	 
        except Exception as e:
            print "DEBUG MDC_DELETE: ", e
	    return Response(status=status.HTTP_404_NOT_FOUND)
        return Response(status=status.HTTP_204_NO_CONTENT)


class MdcDelete(APIView):

    def delete(self, request, pk, format=None):
        try: 
            localDomain = Domain.objects.get(localDomain=True).domain
            mdc = Mdc.objects.filter(productId=str(pk), domainId=localDomain)[0]
	    print "INFO MdC: Deleted VNFD %s from the local domain (%s)" % (mdc.productId, mdc.domainId)
  	    mdc.delete()	 
        except Exception as e:
            print "DEBUG MDC_DELETE local VNFD: ", e
	    return Response(status=status.HTTP_404_NOT_FOUND)
        return Response(status=status.HTTP_204_NO_CONTENT)


class DomainList(APIView):
    def initial_post (self, item):
        serializer = DomainSerializer(data=item)
        if serializer.is_valid():
            try:
                if (Domain.objects.filter(localDomain=True)) and (item['localDomain']==True):
                    print ("  [ERROR] There is already a local domain registered")
                    return Response(status=status.HTTP_400_BAD_REQUEST)
                serializer.save()
            except Exception as e:
                print "DEBUG MDC: ", e
                return Response(status=status.HTTP_400_BAD_REQUEST)
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
    	
    def get(self, request, format=None):
        """
        List all domain entries, or create a new one.
        ---
        response_serializer: DomainSerializer
        """
        domains = Domain.objects.all()
        serializer = DomainSerializer(domains, many=True)

        return Response(serializer.data)

    def post(self, request, format=None):
	print request
        serializer = DomainSerializer(data=request.data)
        if serializer.is_valid():
            try:
                if (Domain.objects.filter(localDomain=True)) and (request.data['localDomain']==True):
                    print ("  [ERROR] There is already a local domain registered")
                    return Response(status=status.HTTP_400_BAD_REQUEST)
                # Add the new domain to the SLA providers
                localDomain = Domain.objects.get(localDomain=True)
                createProvider(request.data['domain'], localDomain)
                serializer.save()
            except Exception as e:
                print "DEBUG MDC: ", e
                return Response(status=status.HTTP_400_BAD_REQUEST)
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

firstDomain = DomainList()
data={"domain":settings.DOMAIN_ID, "localDomain": True, "entryPoint": settings.ENTRY_POINT}
firstDomain.initial_post(data)


class DomainDetail(APIView):
    """
    Retrieve, update or delete an Mdc instance.
    """
    def get_object(self, pk):
        try:
            return Domain.objects.get(pk=pk)
        except Domain.DoesNotExist:
            raise Http404

    def get(self, request, pk, format=None):
        domainId = self.get_object(pk)
        serializer = DomainSerializer(domainId)
        return Response(serializer.data)

    def put(self, request, pk, format=None):    
        domainId = self.get_object(pk)
        serializer = DomainSerializer(domainId, data=request.data)
        print "request: ", request.data
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk, format=None):
        domainId = self.get_object(pk)
        domainId.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


class NeighbourDomainList(APIView):
    """
    List all neighbour domains (all except the local one)
    """
    def get(self, request, format=None):
        """
        ---
        response_serializer: DomainSerializer
        """
        domains = Domain.objects.filter(localDomain=False)
        serializer = DomainSerializer(domains, many=True)

        return Response(serializer.data)



class Descriptors(APIView):
    #Delete VNFD from NFS
    def get_call_url(self, endpoint):
        protocol = 'https' if settings.NFS_USE_HTTPS else 'http'
        return '%s://%s:%s/%s' % (protocol, settings.NFS_HOST, settings.NFS_PORT, endpoint.strip('/'))

    def delete_vnfd(self, vnfd_id):
        endpoint = 'NFS/vnfds/%s' % vnfd_id

        req = self.get_call_url(endpoint)
        r = requests.delete(req)
        print "request: ", r
        return r.status_code, r.text
    
    """
    Retrieve, update or delete an Mdcing instance.
    """
    def get_object(self, pk):
        try:
            return Mdc.objects.get(pk=pk)
        except Mdc.DoesNotExist:
            raise Http404

    def get(self, request, pk, format=None):
        mdc = self.get_object(pk)
        print "PK: ", pk
        try: 
	    r = requests.get(mdc.descriptorLink)
		
        except Exception as e:
            print "DEBUG DESCRIPTOR: ", e
	    return Response(status=status.HTTP_404_NOT_FOUND)
        return Response(r.json())


class SharedItems(APIView):
    """
    Return local items shared with domain X
    """
    def get(self, request, domainX, format=None):
	resultsList = list()
        shared=DomainShare.objects.filter(domainId=domainX, share=True)
        #serializer = SharedSerializer(shared, many=True)
        localDomain = Domain.objects.get(localDomain=True).domain
	#print "shared: ", json.dumps(serializer.data)
        for element in shared:
	    mdc=element.element
	    if (mdc) and (str(mdc.domainId)==str(localDomain)):
        	resultsList.append(mdc)
        serializer = MdCSharedSerializer(resultsList, many=True)

        return Response(serializer.data)


class SharedWithMe(APIView):
    """
    Return remote catalogue items shared with the local domain
    """
    def get_call_url(self, domain, endpoint):
        return '%s%s/%s' % (domain.entryPoint, settings.MDC_PORT, endpoint)

    def is_new(self, item):
        if (Mdc.objects.filter(domainId=item.domainId, productId=item.productId, productType=item.productType)):
	    return False
	return True 
    
    def get(self, request, format=None):
        print "sharing"
	resultsList = list()
	# get local domain id
        localDomain = Domain.objects.get(localDomain=True)
	# get list of neighbour domains
        domains = Domain.objects.filter(localDomain=False)
        for domain in domains:
	    #Retrieve list of items shared with the local domain
	    print "Retrieving items list from domain: ", domain.domain
            endpoint = 'mdc/sharedwith/%s/' % (localDomain.domain)
            req = self.get_call_url(domain, endpoint)
            print "URL: ", req
            try: 
                items = requests.get(req).json()
		for item in items:
		    #print "item: ", json.dumps(item)
		    #check if the item is not already present in the local domain
		    #if self.is_new(item):
                    resultsList.append(item)
            except Exception as e:
                print "DEBUG ShareWithMe: ", e
        return Response(resultsList)



