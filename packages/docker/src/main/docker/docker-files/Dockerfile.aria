FROM httpd:alpine

RUN apk update
RUN apk add python2 py2-pip gcc python2-dev linux-headers musl-dev git curl

RUN pip install apache-ariatosca==0.1.0 jinja2==2.8
RUN curl -sL http://github.com/dfilppi/aria-rest/archive/master.tar.gz|tar xzf -
WORKDIR aria-rest-master
RUN pip install .

CMD aria-rest
