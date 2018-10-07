FROM python:2.7
MAINTAINER George Alexiou (TEIC)
ARG GIT_REVISION=unknown
LABEL git-revision=$GIT_REVISION    

ENV PYTHONUNBUFFERED 1
WORKDIR /umaa/
ADD requirements.txt /umaa/
RUN pip install -r requirements.txt
ADD . /umaa/

CMD ["/bin/sh", "-e", "DockerStart.sh"]
