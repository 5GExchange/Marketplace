#!/bin/sh -e
# Replace environment variables in the configuration file
envsubst '$BILLING_URL $ORCHESTRATOR_URL $NFS_URL' < /etc/nginx/conf.d/default.template > /etc/nginx/conf.d/default.conf
exec "$@"
