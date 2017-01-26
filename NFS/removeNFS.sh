#!/bin/bash

echo "stopping NFS"
service nfs stop

echo "Clear DB"
rm -rf /usr/local/nfs/var/db/*

echo "remove old NFS"
rm -rf /usr/local/nfs/apache-tomee-plus-1.7.4/webapps/NFS

echo "Create the new NFS"
ant -lib lib.no.deploy

echo "Copying the new NFS to the right location"
cp ./prod/war/NFS.war /usr/local/nfs/apache-tomee-plus-1.7.4/webapps/

echo "starting NFS"
service nfs start

echo "ALL SET"


