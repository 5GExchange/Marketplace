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

from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns
from mdc import views

urlpatterns = [
    #Domains
    url(r'^domain/$', views.DomainList.as_view()),
    url(r'^domain/neighbours/$', views.NeighbourDomainList.as_view()),
    url(r'^domain/(?P<pk>[\x20-\x7E]+)/$', views.DomainDetail.as_view()),
    #catalogue
    url(r'^mdc/$', views.MdcList.as_view()),
    url(r'^mdc/(?P<pk>[0-9]+)/$', views.MdcDetail.as_view()),
    url(r'^mdc/delete/(?P<pk>\w+)/', views.MdcDelete.as_view()),
    url(r'^mdc/sharedwith/(?P<domainX>[\x20-\x7E]+)/$', views.SharedItems.as_view()),
    #Element sharing
    url(r'^external/descriptor/(?P<pk>[0-9]+)/$', views.Descriptors.as_view()),
    url(r'^external/sharedwithme/$', views.SharedWithMe.as_view()),

]

urlpatterns = format_suffix_patterns(urlpatterns)

