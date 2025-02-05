.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2020 Huawei Technologies Co., Ltd.

Docker-config
=============

In SO (Service Orchestration) every component running on docker engine and respective containers. here we can see how so is working with Dokcer.

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


Configurations:-
================

Every component has its own over-ride yaml file. We can over-ride the file according the Configurations and Dependencies required for Deploying. 

Over-ride yaml for api-handler
==============================

Path:- /docker-config/volumes/so/config/api-handler-infra/onapheat/override.yaml

.. code-block:: bash

     server:
        port: 8080
        tomcat:
            max-threads: 50
     ssl-enable: false
    
     mso:
      msoKey: 07a7159d3bf51a0e53be7a8f89699be7
      logPath: logs
      site-name: onapheat
      adapters:
        requestDb:
          endpoint: http://request-db-adapter:8083
          auth: Basic YnBlbDpwYXNzd29yZDEk
      catalog:
        db:
          spring:
            endpoint: http://catalog-db-adapter:8082
      db:
        auth: Basic YnBlbDpwYXNzd29yZDEk
      config:
        path: /src/main/resources/
      infra:
        default:
          alacarte:
            orchestrationUri: /mso/async/services/ALaCarteOrchestrator
            recipeTimeout: 180
            testApi: VNF_API
          service:
            macro:
              default:
                testApi: GR_API
      camundaURL: http://bpmn-infra:8081
      camundaAuth: AE2E9BE6EF9249085AF98689C4EE087736A5500629A72F35068FFB88813A023581DD6E765071F1C04075B36EA4213A
      async:
        core-pool-size: 50
        max-pool-size: 50
        queue-capacity: 500
      sdc:
        client:
          auth: F3473596C526938329DF877495B494DC374D1C4198ED3AD305EA3ADCBBDA1862
        activate:
          instanceid: test
          userid: cs0008
        endpoint: http://c1.vm1.mso.simpledemo.onap.org:28090
      tenant:
        isolation:
          retry:
        count: 3
      aai:
        endpoint: https://aai.api.simpledemo.onap.org:8443
        auth: 2630606608347B7124C244AB0FE34F6F
      extApi:
        endpoint: http://nbi.onap:8080/nbi/api/v3
      so:
        operational-environment:
          dmaap:
            username: testuser
            password: VjR5NDcxSzA=
            host: http://c1.vm1.mso.simpledemo.onap.org:28090
            auth: 51EA5414022D7BE536E7516C4D1A6361416921849B72C0D6FC1C7F262FD9F2BBC2AD124190A332D9845A188AD80955567A4F975C84C221EEA8243BFD92FFE6896CDD1EA16ADD34E1E3D47D4A
          publisher:
            topic: com.att.ecomp.mso.operationalEnvironmentEvent
    
     spring:
      datasource:
        hikari:
          jdbcUrl: jdbc:mariadb://mariadb:3306/catalogdb
          username: cataloguser
          password: catalog123
          driver-class-name: org.mariadb.jdbc.Driver
          pool-name: catdb-pool
          registerMbeans: true
      jpa:
          show-sql: true
          hibernate:
            dialect: org.hibernate.dialect.MySQLDialect
            ddl-auto: validate
            naming-strategy: org.hibernate.cfg.ImprovedNamingStrategy
            enable-lazy-load-no-trans: true
      jersey:
        type: filter
    
      security:
        usercredentials:
        -
          username: sitecontrol
          password: '$2a$10$Fh9ffgPw2vnmsghsRD3ZauBL1aKXebigbq3BB1RPWtE62UDILsjke'
          role: SiteControl-Client
        -
          username: gui
          password: '$2a$10$Fh9ffgPw2vnmsghsRD3ZauBL1aKXebigbq3BB1RPWtE62UDILsjke'
          role: GUI-Client
        -
          username: infraportal
          password: '$2a$10$Fh9ffgPw2vnmsghsRD3ZauBL1aKXebigbq3BB1RPWtE62UDILsjke'
          role: InfraPortal-Client
        -
          username: InfraPortalClient
          password: '$2a$10$Fh9ffgPw2vnmsghsRD3ZauBL1aKXebigbq3BB1RPWtE62UDILsjke'
          role: InfraPortal-Client
        -
          username: bpel
          password: '$2a$10$Fh9ffgPw2vnmsghsRD3ZauBL1aKXebigbq3BB1RPWtE62UDILsjke'
          role: BPEL-Client
        -
          username: mso_admin
          password: '$2a$10$Fh9ffgPw2vnmsghsRD3ZauBL1aKXebigbq3BB1RPWtE62UDILsjke'
          role: ACTUATOR
    
     request:
      datasource:
        hikari:
          jdbcUrl: jdbc:mariadb://mariadb:3306/requestdb
          username: requestuser
          password: request123
          driver-class-name: org.mariadb.jdbc.Driver
          pool-name: reqdb-pool
          registerMbeans: true
     org:
      onap:
        so:
          cloud-owner: CloudOwner
          adapters:
            network:
              encryptionKey: 07a7159d3bf51a0e53be7a8f89699be7



Start the  container
=======================
cd /home/root1/docker-config/

CMD:-
===

sudo docker-compose up -d 

*Example Output:*

root1@slave-node:~/docker-config$ sudo docker-compose up -d 
docker-config_mariadb_1 is up-to-date
Starting docker-config_catalog-db-adapter_1 ... done
Starting docker-config_request-db-adapter_1 ... done
Starting docker-config_bpmn-infra_1         ... done
Starting docker-config_vfc-adapter_1        ... done
Starting docker-config_sdc-controller_1     ... done
Starting docker-config_sdnc-adapter_1       ... done
Starting docker-config_openstack-adapter_1  ... done
Starting docker-config_api-handler-infra_1  ... done
Starting docker-config_so-monitoring_1      ... done
Starting docker-config_nssmf-adapter_1      ... done


Example Output:
===============

docker ps

*Example Output:*

root1@slave-node:~/docker-config$ sudo docker ps
CONTAINER ID        IMAGE                                              COMMAND                  CREATED             STATUS              PORTS                     NAMES
d930caf28508        nexus3.onap.org:10001/onap/so/openstack-adapter    "/app/wait-for.sh -q…"   5 weeks ago         Up 30 seconds       0.0.0.0:8087->8087/tcp    docker-config_openstack-adapter_1
599af283319e        nexus3.onap.org:10001/onap/so/vfc-adapter          "/app/wait-for.sh -q…"   5 weeks ago         Up 30 seconds       0.0.0.0:8084->8084/tcp    docker-config_vfc-adapter_1
5549305c8dd6        nexus3.onap.org:10001/onap/so/api-handler-infra    "/app/wait-for.sh -q…"   5 weeks ago         Up 27 seconds       0.0.0.0:8080->8080/tcp    docker-config_api-handler-infra_1
59d3aa684ecb        nexus3.onap.org:10001/onap/so/sdnc-adapter         "/app/wait-for.sh -q…"   5 weeks ago         Up 29 seconds       0.0.0.0:8086->8086/tcp    docker-config_sdnc-adapter_1
ade4cef97bd3        nexus3.onap.org:10001/onap/so/bpmn-infra           "/app/wait-for.sh -q…"   5 weeks ago         Up 29 seconds       0.0.0.0:8081->8081/tcp    docker-config_bpmn-infra_1
e9558560c4d7        nexus3.onap.org:10001/onap/so/sdc-controller       "/app/wait-for.sh -q…"   5 weeks ago         Up 25 seconds       0.0.0.0:8085->8085/tcp    docker-config_sdc-controller_1
ae27ec2f8b04        nexus3.onap.org:10001/onap/so/so-monitoring        "/app/wait-for.sh -q…"   5 weeks ago         Up 26 seconds       0.0.0.0:8088->8088/tcp    docker-config_so-monitoring_1
8d2c64d48f1a        nexus3.onap.org:10001/onap/so/request-db-adapter   "/app/wait-for.sh -q…"   5 weeks ago         Up 32 seconds       0.0.0.0:8083->8083/tcp    docker-config_request-db-adapter_1
a126dd29c540        nexus3.onap.org:10001/mariadb:10.1.11              "/docker-entrypoint.…"   5 weeks ago         Up 17 minutes       0.0.0.0:32768->3306/tcp   docker-config_mariadb_1

Inspect a docker image
======================
This command shows interesting information about the structure of the mso image. Note that an image is NOT a running container. It is the template that a container is created from.

CMD:-
=====
sudo docker inspect onap/so/api-handler-infra


Example Output:

.. code-block:: bash


    [
    {
      "Id": "sha256:2573165483e9ac87826da9c08984a9d0e1d93a90c681b22d9b4f90ed579350dc",
      "RepoTags": [
          "onap/so/api-handler-infra:1.3.0-SNAPSHOT",
          "onap/so/api-handler-infra:1.3.0-SNAPSHOT-20190213T0846",
          "onap/so/api-handler-infra:1.3.0-SNAPSHOT-latest",
          "onap/so/api-handler-infra:latest"
      ],
      "RepoDigests": [],
      "Parent": "sha256:66b508441811ab4ed9968f8702a0d0a697f517bbc10d8d9076e5b98ae4437344",
      "Comment": "",
      "Created": "2019-02-13T09:37:33.770342225Z",
      "Container": "8be46c735d21935631130f9017c3747779aab26eab54a9149b1edde122f7576d",
      "ContainerConfig": {
          "Hostname": "ac4a12e21390",
          "Domainname": "",
          "User": "",
          "AttachStdin": false,
          "AttachStdout": false,
          "AttachStderr": false,
          "Tty": false,
          "OpenStdin": false,
          "StdinOnce": false,
          "Env": [
              "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin",
              "LANG=C.UTF-8",
              "JAVA_HOME=/usr/lib/jvm/java-1.8-openjdk",
              "JAVA_VERSION=8u191",
              "JAVA_ALPINE_VERSION=8.191.12-r0",
              "HTTP_PROXY=",
              "HTTPS_PROXY=",
              "http_proxy=",
              "https_proxy="
          ],
          "Cmd": [
              "/bin/sh",
              "-c",
              "#(nop) ",
              "CMD [\"/app/start-app.sh\"]"
          ],
          "ArgsEscaped": true,
          "Image": "sha256:66b508441811ab4ed9968f8702a0d0a697f517bbc10d8d9076e5b98ae4437344",
          "Volumes": {
              "/app/ca-certificates": {},
              "/app/config": {}
          },
          "WorkingDir": "/app",
          "Entrypoint": null,
          "OnBuild": [],
          "Labels": {}
      },
      "DockerVersion": "17.05.0-ce",
      "Author": "",
      "Config": {
          "Hostname": "ac4a12e21390",
          "Domainname": "",
          "User": "",
          "AttachStdin": false,
          "AttachStdout": false,
          "AttachStderr": false,
          "Tty": false,
          "OpenStdin": false,
          "StdinOnce": false,
          "Env": [
              "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin",
              "LANG=C.UTF-8",
              "JAVA_HOME=/usr/lib/jvm/java-1.8-openjdk",
              "JAVA_VERSION=8u191",
              "JAVA_ALPINE_VERSION=8.191.12-r0",
              "HTTP_PROXY=",
              "HTTPS_PROXY=",
              "http_proxy=",
              "https_proxy="
          ],
          "Cmd": [
              "/app/start-app.sh"
          ],
          "ArgsEscaped": true,
          "Image": "sha256:66b508441811ab4ed9968f8702a0d0a697f517bbc10d8d9076e5b98ae4437344",
          "Volumes": {
              "/app/ca-certificates": {},
              "/app/config": {}
          },
          "WorkingDir": "/app",
          "Entrypoint": null,
          "OnBuild": [],
          "Labels": {}
      },
      "Architecture": "amd64",
      "Os": "linux",
      "Size": 245926705,
      "VirtualSize": 245926705,
      "GraphDriver": {
          "Data": null,
          "Name": "aufs"
      },
      "RootFS": {
          "Type": "layers",
          "Layers": [
              "sha256:503e53e365f34399c4d58d8f4e23c161106cfbce4400e3d0a0357967bad69390",
              "sha256:744b4cd8cf79c70508aace3697b6c3b46bee2c14f1c14b6ff09fd0ba5735c6d4",
              "sha256:4c6899b75fdbea2f44efe5a2f8d9f5319c1cf7e87151de0de1014aba6ce71244",
              "sha256:2e076d24f6d1277456e33e58fc8adcfd69dfd9c025f61aa7b98d500e7195beb2",
              "sha256:bb67f2d5f8196c22137a9e98dd4190339a65c839822d16954070eeb0b2a17aa2",
              "sha256:afbbd0cc43999d5c5b0ff54dfd82365a3feb826e5c857d9b4a7cf378001cd4b3",
              "sha256:1920a7ca0f8ae38a79a1339ce742aaf3d7a095922d96e37074df67cf031d5035",
              "sha256:1261fbaef67c5be677dae1c0f50394587832ea9d8c7dc105df2f3db6dfb92a3a",
              "sha256:a33d8ee5c18908807458ffe643184228c21d3c5d5c5df1251f0f7dfce512f7e8",
              "sha256:80704fca12eddb4cc638cee105637266e04ab5706b4e285d4fc6cac990e96d63",
              "sha256:55abe39073a47f29aedba790a92c351501f21b3628414fa49a073c010ee747d1",
              "sha256:cc4136c2c52ad522bd492545d4dd18265676ca690aa755994adf64943b119b28",
              "sha256:2163a1f989859fdb3af6e253b74094e92a0fc1ee59f5eb959971f94eb1f98094"
          ]
      }
     }
    ]
