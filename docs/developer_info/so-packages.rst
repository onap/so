.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2020 Huawei Technologies Co., Ltd.

SO-Packages
====================

Packages are Used to Dockerise the SO(Service orchestration). it has Package of Dockerfiles for every component in SO with CA(Certificate Authority).


CA(Certificate Authority)
=========================

Certificate Authorities/ CAs, issue Digital Certificates. Digital Certificates are verifiable small data files that contain identity credentials to help websites, people, and devices represent their authentic online identity (authentic because the CA has verified the identity). CAs play a critical role in how the Internet operates and how transparent, trusted transactions can take place online. CAs issue millions of Digital Certificates each year, and these certificates are used to protect information, encrypt billions of transactions, and enable secure communication.

CA(file)

/so/packages/docker/src/main/docker/docker-files/ca-certificates/onap-ca.crt

Example CA cirtifiacte:-

-----BEGIN CERTIFICATE-----
MIIEczCCA1ugAwIBAgIBADANBgkqhkiG9w0BAQQFAD..AkGA1UEBhMCR0Ix
EzARBgNVBAgTClNvbWUtU3RhdGUxFDASBgNVBAoTC0..0EgTHRkMTcwNQYD
VQQLEy5DbGFzcyAxIFB1YmxpYyBQcmltYXJ5IENlcn..XRpb24gQXV0aG9y
aXR5MRQwEgYDVQQDEwtCZXN0IENBIEx0ZDAeFw0wMD..TUwMTZaFw0wMTAy
MDQxOTUwMTZaMIGHMQswCQYDVQQGEwJHQjETMBEGA1..29tZS1TdGF0ZTEU
MBIGA1UEChMLQmVzdCBDQSBMdGQxNzA1BgNVBAsTLk..DEgUHVibGljIFBy
aW1hcnkgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkxFD..AMTC0Jlc3QgQ0Eg
THRkMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCg..Tz2mr7SZiAMfQyu
vBjM9OiJjRazXBZ1BjP5CE/Wm/Rr500PRK+Lh9x5eJ../ANBE0sTK0ZsDGM
ak2m1g7oruI3dY3VHqIxFTz0Ta1d+NAjwnLe4nOb7/..k05ShhBrJGBKKxb
8n104o/5p8HAsZPdzbFMIyNjJzBM2o5y5A13wiLitE..fyYkQzaxCw0Awzl
kVHiIyCuaF4wj571pSzkv6sv+4IDMbT/XpCo8L6wTa..sh+etLD6FtTjYbb
rvZ8RQM1tlKdoMHg2qxraAV++HNBYmNWs0duEdjUbJ..XI9TtnS4o1Ckj7P
OfljiQIDAQABo4HnMIHkMB0GA1UdDgQWBBQ8urMCRL..5AkIp9NJHJw5TCB
tAYDVR0jBIGsMIGpgBQ8urMCRLYYMHUKU5AkIp9NJH..aSBijCBhzELMAkG
A1UEBhMCR0IxEzARBgNVBAgTClNvbWUtU3RhdGUxFD..AoTC0Jlc3QgQ0Eg
THRkMTcwNQYDVQQLEy5DbGFzcyAxIFB1YmxpYyBQcm..ENlcnRpZmljYXRp
b24gQXV0aG9yaXR5MRQwEgYDVQQDEwtCZXN0IENBIE..DAMBgNVHRMEBTAD
AQH/MA0GCSqGSIb3DQEBBAUAA4IBAQC1uYBcsSncwA..DCsQer772C2ucpX
xQUE/C0pWWm6gDkwd5D0DSMDJRqV/weoZ4wC6B73f5..bLhGYHaXJeSD6Kr
XcoOwLdSaGmJYslLKZB3ZIDEp0wYTGhgteb6JFiTtn..sf2xdrYfPCiIB7g
BMAV7Gzdc4VspS6ljrAhbiiawdBiQlQmsBeFz9JkF4..b3l8BoGN+qMa56Y
It8una2gY4l2O//on88r5IWJlm1L0oA8e4fR2yrBHX..adsGeFKkyNrwGi/
7vQMfXdGsRrXNGRGnX+vWDZ3/zWI0joDtCkNnqEpVn..HoX
-----END CERTIFICATE-----

Logback Configuration through an External File
==============================================

Logback configuration through  application.properties file will be sufficient for many Spring Boot applications. However, large enterprise applications are likely to have far more complex logging requirements. As I mentioned earlier, Logback supports advanced logging configurations through XML and Groovy configuration files.

In a Spring Boot application, you can specify a Logback XML configuration file as  logback.xml or  logback-spring.xml in the project classpath.

Here is the code of the  logback-spring.xml file.

.. code-block:: xml

   <?xml version="1.0" encoding="UTF-8"?>
  <configuration>
    <include resource="org/springframework/boot/logging/logback/base.xml"/>
    <logger name="guru.springframework.controllers" level="WARN" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </logger>
    <logger name="guru.springframework.helpers" level="WARN" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </logger>
 </configuration>



Docker
======
Docker is a software platform for building applications based on containers — small and lightweight execution environments that make shared use of the operating system kernel but otherwise run in isolation from one another. 

DockerFile
==========
Docker can build images automatically by reading the instructions from a Dockerfile. A Dockerfile is a text document that contains all the commands a user could call on the command line to assemble an image. Using docker build users can create an automated build that executes several command-line instructions in succession.

Filepath
++++++++
/so/packages/docker/src/main/docker/docker-files/Dockerfile.so-app

Dockerfile:-
------------

ENV https_proxy=$HTTPS_PROXY

USER root

RUN mkdir -p /app/config 

RUN mkdir -p /app/certificates

RUN mkdir -p /app/logs

RUN mkdir -p /app/ca-certificates

COPY maven/app.jar /app

COPY configs/logging/logback-spring.xml /app

COPY scripts/start-app.sh /app

COPY scripts/wait-for.sh /app

COPY ca-certificates/onap-ca.crt /app/ca-certificates/onap-ca.crt

RUN chown -R so:so /app

USER so

# Springboot configuration (required)

VOLUME /app/config


#  Root certificates (optional)

VOLUME /app/ca-certificates

WORKDIR /app
ENTRYPOINT ["/app/start-app.sh"]




1)ENV:- ENV values are available to containers, but also RUN-style commands during the Docker build starting with the line where they are introduced.

2)ENTRYPOINT:- ENTRYPOINT configures a container that will run as an executable.

3)RUN:- RUN executes command(s) in a new layer and creates a new image.

4)CMD:- CMD sets default command and/or parameters, which can be overwritten from command line when docker container runs.




 




