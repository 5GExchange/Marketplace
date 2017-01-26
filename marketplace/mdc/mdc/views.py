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
from mdc.models import Mdc, Domain
from mdc.serializers import MdcSerializer, DomainSerializer
from django.conf import settings
from rest_framework.views import APIView
from django.http import Http404
from rest_framework.response import Response
from rest_framework import status
import json, requests

class MdcList(APIView):
    def parse_vnfd (self, descriptor):
        item = {}
        localDomain = Domain.objects.get(localDomain=True)
        item['domainId'] = localDomain.domain
        item['productType'] = settings.VNF
        item['productId'] = descriptor['id']
        item['name'] = descriptor['name']
        item['provider'] = descriptor['provider']
        item['description'] = descriptor['description']
        item['descriptorLink'] = settings.NFS_VNFD_URL + "/" + str(descriptor['id'])
        item['imageLink'] = descriptor['vdu'][0]['vm_image']
        sla = descriptor['deployment_flavours'][0]
        sla_str = ""
        for flavour in sla['assurance_parameters']:
            sla_str += " - " + flavour['formula']
        item['sla'] = sla['flavour_key'] + sla_str
        price_str = descriptor['billing_model']
        item['price'] = "Model: " + price_str['model'] + " -  Period: " + price_str['period'] + " / " + str(price_str['price']['max_per_period']) + price_str['price']['unit'] + " + " + str(price_str['price']['setup']) + price_str['price']['unit'] + " setup cost"
        item['sharedWith'] = []
        print "JSON: ", json.dumps(item)

        return item

    def parse_nsd (self, descriptor):
        item = {}
        localDomain = Domain.objects.get(localDomain=True)
        item['domainId'] = localDomain.domain
        item['productType'] = settings.NETWORK_SERVICE
        item['productId'] = descriptor['id']
        item['name'] = descriptor['name']
        item['provider'] = "Domain " + localDomain.domain
        item['description'] = descriptor['description']
        item['descriptorLink'] = settings.BSC_URL + "/" + descriptor['id']
        item['imageLink'] = None
        sla = descriptor['sla'][0]
        sla_str = ""
        for flavour in sla['assurance_parameters']:
            sla_str += " - " + flavour['name'] + " " + flavour['value']
        item['sla'] = "Name: " + sla['sla_key'] + ": " + sla_str
        price_str = sla['billing']
        item['price'] = "Model: " + price_str['model'] + " -  Period: P1M / " + str(price_str['price']['price_per_period']) + price_str['price']['unit'] + " + " + str(price_str['price']['setup']) + price_str['price']['unit'] + " setup cost"

        item['sharedWith'] = []
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
            item = self.parse_nsd(request.data['nsd'])
        elif 'vnfd' in request.data:
            item = self.parse_vnfd(request.data['vnfd'])
        else:
            print "DEBUG MDC: Not a valid descriptor"
            return Response(status=status.HTTP_400_BAD_REQUEST)

        serializer = MdcSerializer(data=item)
        if serializer.is_valid():
            try:
                if Mdc.objects.filter(productType=item['productType'], domainId=item['domainId'], productId=item['productId']):
                    print ("  [ERROR] There is an entry already with that productId (%s) for the same ProductType (%s) and the same domain (%s)" % (item['productId'], item['productType'], item['domainId']))
                    return Response(status=status.HTTP_400_BAD_REQUEST)
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
            r = self.delete_vnfd(mdc.productId)
        except Exception as e:
            print "DEBUG MDC_DELETE: ", e
            #return Response(status=status.HTTP_404_NOT_FOUND)
            return Response(status=r)
        mdc.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


class DomainList(APIView):
    """
    List all domain entries, or create a new one.
    """
    def get(self, request, format=None):
        """
        ---
        response_serializer: DomainSerializer
        """
        domains = Domain.objects.all()
        serializer = DomainSerializer(domains, many=True)

        return Response(serializer.data)

    def post(self, request, format=None):
        serializer = DomainSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class DomainDetail(APIView):
    """
    Retrieve, update or delete an Mdcing instance.
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


