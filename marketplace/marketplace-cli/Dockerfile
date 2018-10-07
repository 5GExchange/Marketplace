FROM python:2.7
ARG GIT_REVISION=unknown
LABEL git-revision=$GIT_REVISION    

ENV PYTHONUNBUFFERED 1
ADD dummy_image.qcow2 marketplace-cli.py marketplace-cli.conf requirements.txt vnf_onboarding.sh /opt/marketplace-cli/
WORKDIR /opt/marketplace-cli
RUN pip install -r requirements.txt
CMD ["python"]
