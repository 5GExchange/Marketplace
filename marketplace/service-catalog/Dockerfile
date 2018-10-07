FROM openjdk:8-jre
ARG GIT_REVISION=unknown
LABEL git-revision=$GIT_REVISION    

ADD . /opt
ADD target/service-catalog-1.0.jar /opt/app.jar
ADD bin/application.properties /opt/application.properties
RUN bash -c 'touch /opt/app.jar'

ENV DOMAIN_ID=ATOS_A

ENTRYPOINT ["java","-Xmx128m","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar", "--spring.config.location=/opt/application.properties"]
