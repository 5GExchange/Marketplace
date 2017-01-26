FROM openjdk:8-jre

ADD . /opt
ADD target/service-selection-1.0.jar /opt/app.jar
ADD bin/application.properties /opt/application.properties

RUN bash -c 'touch /opt/app.jar'

ENTRYPOINT ["java","-jar","/opt/app.jar", "--spring.config.location=/opt/application.properties"]
