FROM openjdk:8-jre
WORKDIR /usr/local/nfs/
ARG GIT_REVISION=unknown
LABEL git-revision=$GIT_REVISION    
COPY dist/ ./
COPY scripts/nfsMonitor bin/
RUN chmod u+x bin/nfsMonitor `ls | grep apache-tomee`/bin/*
VOLUME /usr/local/nfs/var/data
ENV NFS_DB_FILE /usr/local/nfs/var/data/db/nfs
ENV NFS_STORE_PATH /usr/local/nfs/var/data/store
ENV TOMCAT_HTTP_PORT 8080
ENV TOMCAT_HTTPS_PORT 8443
EXPOSE 8080 8443
ENTRYPOINT ["./bin/nfsMonitor", "start"]
