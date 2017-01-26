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

"""
Django settings for mdcatalogue project.

For more information on this file, see
https://docs.djangoproject.com/en/1.7/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/1.7/ref/settings/
"""

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
import os
BASE_DIR = os.path.dirname(os.path.dirname(__file__))


# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/1.7/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = '#(j^o+(dmr*ec*a6mh28pr%$ifcpzu*)_1$u3(22=^zmd7tk2p'

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = True

TEMPLATE_DEBUG = True

#ALLOWED_HOSTS = []
ALLOWED_HOSTS = ['192.168.56.102', 'localhost', '127.0.0.1']


# Application definition

INSTALLED_APPS = (
   'django.contrib.admin',
   'django.contrib.auth',
   'django.contrib.contenttypes',
   'django.contrib.sessions',
   'django.contrib.messages',
   'django.contrib.staticfiles',
   'rest_framework',
   #'django_crontab',
   'rest_framework_swagger',
   'django_extensions',
   'corsheaders',
   'mdc',
)

REST_FRAMEWORK = {
   'DEFAULT_PERMISSON_CLASSES': ('rest_framework.premissions.IsAdminUser',),
   'DEFAULT_FILTER_BACKENDS': ('rest_framework.filters.DjangoFilterBackend',),
   'PAGINATE_BY': 10
}

MIDDLEWARE_CLASSES = (
    'corsheaders.middleware.CorsMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.auth.middleware.SessionAuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
)

ROOT_URLCONF = 'mdcatalogue.urls'

WSGI_APPLICATION = 'mdcatalogue.wsgi.application'


# Database
# https://docs.djangoproject.com/en/1.7/ref/settings/#databases

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': 'mdc2',
        'USER': 'mdcatalogue_usr',
        'PASSWORD': 'mdcatalogue_usr',
        #'HOST': 'localhost',   # Or an IP Address that your DB is hosted on
        'HOST': 'mysql',   # Or an IP Address that your DB is hosted on
        'PORT': '3306',
    }
}

# Internationalization
# https://docs.djangoproject.com/en/1.7/topics/i18n/

LANGUAGE_CODE = 'en-us'

TIME_ZONE = 'UTC'

USE_I18N = True

USE_L10N = True

USE_TZ = True


# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/1.7/howto/static-files/

STATIC_URL = '/static/'

CRONJOBS = [
   ('* * * * *', 'mdcatalogue.mdc.eventChecker2.eventDetection', {'verbose': 1})
]

CORS_ORIGIN_ALLOW_ALL = True


#SLA_URL = 'http://sla.docker:9040'
BSC_URL = 'http://10.254.0.17:42050/service/catalog'
#NFS_VNFD_URL = 'http://10.254.0.17:8080/NFS/vnfds'
NFS_VNFD_URL = 'http://192.168.56.102:8080/NFS/vnfds'
#NFS_HOST = '10.254.0.17'
NFS_HOST = '192.168.56.102'
NFS_PORT = '8080'

NFS_USE_HTTPS = False




#constant to define a service or a VNF
NETWORK_SERVICE = 'ns'
VNF = 'vnf'

#Possible status of an instance
STATUS_RUNNING = 'running'
STATUS_STOPPED = 'stopped'



TEMPLATE_LOADERS = (
    'django.template.loaders.eggs.Loader',
)


