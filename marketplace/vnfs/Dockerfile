FROM python:2.7
MAINTAINER George Alexiou (TEIC)

RUN apt-get update
RUN apt-get install -y vim

ENV PYTHONUNBUFFERED 1
ADD . /code
WORKDIR /code
RUN pip install -r requirements.txt
CMD python app.py
