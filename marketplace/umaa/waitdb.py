#!/usr/bin/env python

import os
import time
from django.db import connection, OperationalError
from MySQLdb.constants import CR, ER

# 36 retries every 5 seconds is an effective timeout of 3 minutes for the DB
# to start up
MAX_RETRIES = 36
RETRY_INTERVAL = 5
VERBOSE = False

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "umaa.settings")
cursor = None
connected = False
retries = 0
print "Waiting for the database to start..."
while not connected:
    try:
        if VERBOSE:
            print "Trying to connect (attempt {})...".format(retries + 1)
        cursor = connection.cursor()
        connected = True
        if VERBOSE:
            print "Connection successful"
        print "Database started"
    except OperationalError as e:
        if e.args[0] in (CR.CONN_HOST_ERROR, ER.DBACCESS_DENIED_ERROR, ER.ACCESS_DENIED_ERROR):
            if retries < MAX_RETRIES:
                if VERBOSE:
                    print "Connection failed, retrying in {} seconds".format(RETRY_INTERVAL)
                time.sleep(RETRY_INTERVAL)
                retries += 1
            else:
                print "Could not connect after maximum number of retries. Aborting."
                raise
        else:
            raise
    finally:
        if cursor is not None:
            cursor.close()
