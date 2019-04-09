FROM openjdk:8-jdk-alpine

ARG http_proxy
ENV HTTP_PROXY=$http_proxy
ENV http_proxy=$HTTP_PROXY
ARG https_proxy
ENV HTTPS_PROXY=$https_proxy
ENV https_proxy=$HTTPS_PROXY

RUN apk add --no-cache --update busybox-extras bash zip unzip curl wget openssh maven openjdk8 jq httpie py-pip

RUN mkdir /app && mkdir /app/config && mkdir /app/certificates && mkdir /app/logs  && mkdir /app/ca-certificates

COPY maven/app.jar /app
#COPY configs/logging/logback-spring.xml /app
COPY start-app.sh /app
#COPY ca-certificates/onap-ca.crt /app/ca-certificates/onap-ca.crt

RUN chown -R svnfm:svnfm /app && chmod 700 /app/*.sh

# Springboot configuration (required)
VOLUME /app/config

#  Root certificates (optional)
VOLUME /app/ca-certificates

WORKDIR /app
CMD ["/app/start-app.sh"]