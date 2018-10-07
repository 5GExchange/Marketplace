#FROM nodesource/node:trusty
FROM python:2.7
MAINTAINER Javier Melian (ATOS)
ARG GIT_REVISION=unknown
LABEL git-revision=$GIT_REVISION    

ENV PYTHONUNBUFFERED 1
RUN apt-get update
#RUN apt-get install -y python-pip python-mysqldb
RUN apt-get install -y vim

WORKDIR /accounting/
ADD requirements.txt /accounting/
RUN pip install -r requirements.txt
ADD . /accounting/

CMD ["/bin/sh","DockerStart.sh"]
