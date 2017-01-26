#!/bin/sh -e

# Wait for database to get available
./waitdb.py
python manage.py syncdb --noinput
python manage.py update_initial_data
python manage.py runserver 0.0.0.0:8000

