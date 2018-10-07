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

from mdc.models  import Mdc, Domain, DomainShare
from rest_framework import serializers




class DomainShareSerializer(serializers.ModelSerializer):
    class Meta:
        model = DomainShare
        fields = ('domainId', 'share')


class MdcSerializer(serializers.ModelSerializer):
    sharedWith = DomainShareSerializer(many=True)
    class Meta:
        model = Mdc
        #fields = ('id', 'domainId', 'productId', 'productType', 'name', 'description', 'provider', 'descriptorLink', 'imageLink', 'sla', 'price', 'sharedWith', 'shareWithAll', 'dateCreated', 'dateModified')
	fields = ('id', 'domainId', 'productId', 'productType', 'name', 'description', 'kind', 'provider', 'descriptorLink', 'imageLink', 'sla', 'billing_model', 'price_per_period', 'price_setup', 'period', 'currency', 'sharedWith', 'shareWithAll', 'dateCreated', 'dateModified')
        read_only_fields = ('dateCreated', )

    def create(self, validated_data):
        shares_data = validated_data.pop('sharedWith')
        element = Mdc.objects.create(**validated_data)
        for shares_data in shares_data:
            DomainShare.objects.create(element=element, **shares_data)
        return element

    def update(self, instance, validated_data):
        instance.domainId= validated_data.get('domainId', instance.domainId)
        instance.productId= validated_data.get('productId', instance.productId)
        instance.productType= validated_data.get('productType', instance.productType)
        instance.name= validated_data.get('name', instance.name)
	instance.kind= validated_data.get('kind', instance.kind)
        instance.provider= validated_data.get('provider', instance.provider)
        instance.description= validated_data.get('description', instance.description)
        instance.descriptorLink= validated_data.get('descriptorLink', instance.descriptorLink)
        instance.imageLink= validated_data.get('imageLink', instance.imageLink)
        instance.sla= validated_data.get('sla', instance.sla)
        #instance.price= validated_data.get('price', instance.price)
        instance.billing_model= validated_data.get('billing_model', instance.billing_model)
	instance.price_per_period = validated_data.get('price_per_period', instance.price_per_period)
	instance.price_setup= validated_data.get('price_setup', instance.price_setup)
	instance.period= validated_data.get('period', instance.period)
	instance.currency= validated_data.get('currency', instance.currency)
        instance.shareWithAll= validated_data.get('shareWithAll', instance.shareWithAll)
        instance.save()

        new_domains = validated_data.get('sharedWith')
        if new_domains:
            for new_domain in new_domains:
                new_domain_domainId = new_domain.get('domainId', None)
                if new_domain_domainId and DomainShare.objects.filter(domainId=new_domain_domainId, element=instance):
                    #the domain already has a sharing preference established so we update them
                    old_domain = DomainShare.objects.get(domainId=new_domain_domainId, element=instance)
                    old_domain.share = new_domain.get('share', old_domain.share)
                    old_domain.save()
                else:
                    DomainShare.objects.create(element=instance, **new_domain)
        return instance


class MdCSharedSerializer(serializers.ModelSerializer):
    #sharedWith = DomainShareSerializer(many=True)
    class Meta:
        model = Mdc
	fields = ('domainId', 'productId', 'productType', 'name', 'description', 'kind', 'provider', 'descriptorLink', 'imageLink', 'sla', 'billing_model', 'price_per_period', 'price_setup', 'period', 'currency')



class DomainSerializer(serializers.ModelSerializer):
    class Meta:
        model = Domain
        fields = ('domain', 'localDomain', 'entryPoint', 'dateCreated')
        read_only_fields = ('dateCreated', )


class SharedSerializer(serializers.ModelSerializer):
    class Meta:
        model = DomainShare
        fields = ('domainId', 'share', 'element')


