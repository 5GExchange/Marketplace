FROM openjdk:8-jre

WORKDIR /opt/sla
COPY sla-service/target/dependency/jetty-runner.jar ./
COPY sla-service/target/sla-service.war ./
COPY DockerStart.sh ./
RUN chmod u+x DockerStart.sh

ENV DB_URL=jdbc:mysql://mysql:3306/atossla
EXPOSE 9040

ENTRYPOINT ["./DockerStart.sh"]