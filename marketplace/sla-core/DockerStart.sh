#!/bin/sh

# Wait for database to get available
sleep 10

java -Xmx128m -jar jetty-runner.jar --port 9040 --path / sla-service.war
