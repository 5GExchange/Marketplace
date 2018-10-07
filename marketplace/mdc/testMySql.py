#!/usr/bin/python
import MySQLdb
import time

maxTries = 60
sleepSecs = 3
ready = False
tries = 0
while (ready is False and tries < maxTries):
    try:
        print("verifying mysql db availability")
        tries = tries + 1
        db = MySQLdb.connect(host="mysql",  # your host
                         user="umaa_db_usr",       # username
                         passwd="HeVBz6T5",     # password
                         db="umaa_db")   # name of the database

        # Create a Cursor object to execute queries.
        cur = db.cursor()

        # Select data from table using SQL query.
        cur.execute("SELECT * FROM auth_user")

        # print the first and second columns
        for row in cur.fetchall() :
            print row[0], " ", row[1]
            ready = True
        time.sleep(sleepSecs)
    except Exception as e:
        print(e)
        time.sleep(sleepSecs)
