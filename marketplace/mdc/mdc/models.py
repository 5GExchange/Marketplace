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

from django.db import models
from django.utils import timezone
from datetime import datetime



class Domain(models.Model):

    #Id of the owner domain
    domain = models.CharField(primary_key=True, max_length=254, default=None, blank=False, unique=True)
    #whether this domain is local or external
    localDomain = models.BooleanField(default=False, blank=True)
    #Entry Point of the domain for future queries
    entryPoint = models.CharField(max_length=254, blank=False)
    #whether this domain available or not at the moment
    available = models.BooleanField(default=False, blank=True)
    dateCreated = models.DateTimeField(auto_now_add=True)

    def __unicode__(self):
        return '%s' % (self.domain)



class Mdc(models.Model):

    TYPES = (('vnf', 'VNF'), ('ns', 'NS'))
    #vnf|service
    productType = models.CharField(max_length=3, choices=TYPES, blank=False)
    #Id of the VNF or the Service in the system
    productId = models.CharField(max_length=256, default=None, blank=False)
    #type of the NS or VNF
    kind = models.CharField(max_length=256, default=None, blank=False)
    #Id of the owner domain
    #domainId = models.CharField(max_length=256, default=None, blank=False)

    domainId = models.ForeignKey(Domain, to_field='domain', on_delete=models.CASCADE)
    #Short name of the element
    name = models.CharField(max_length=50, default=None, null=True, blank=False)
    #Description of the features of the element
    description = models.CharField(max_length=256, default=None, null=True, blank=False)
    #Id of the seller
    provider = models.CharField(max_length=256, null=True, default=None, blank=False)
    #Link to the descriptor of the element in its original domain
    descriptorLink = models.CharField(max_length=256, null=True, default=None, blank=False)
    #Link to the image of the element in its original domain
    imageLink = models.CharField(max_length=256, null=True, default=None, blank=True)
    #SLA description of the element
    sla = models.CharField(max_length=256, null=True, default=None, blank=True)
    #Price description of the element
    #price = models.CharField(max_length=256, null=True, default=None, blank=True)
    billing_model = models.CharField(max_length=50, default=None, null=True, blank=True)
    price_per_period = models.IntegerField(null=True, default=0, blank=True)
    price_setup = models.IntegerField(null=True, default=0, blank=True)
    period = models.CharField(max_length=10, null=True, default=None, blank=True)
    currency = models.CharField(max_length=10, null=True, default=None, blank=True)

    #element sharing preferences
    shareWithAll = models.BooleanField(default=False, blank=True)

    dateCreated = models.DateTimeField(auto_now_add=True)
    dateModified = models.DateTimeField(auto_now=True)


    def __unicode__(self):
        return 'Service %s (%s)' % (self.productId, self.period)




class DomainShare(models.Model):

    #Id of the owner domain
    #domainId = models.CharField(max_length=256, default=None, blank=False)
    domainId = models.ForeignKey(Domain, to_field='domain', on_delete=models.CASCADE)
    share = models.BooleanField(default=True)
    element = models.ForeignKey(Mdc, related_name='sharedWith', on_delete=models.CASCADE, null=True, blank=True)

   
    '''
    def __unicode__(self):
      return json.dumps({"domainId": self.domainId, "share": self.share})
    '''

