.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2017 Huawei Technologies Co., Ltd.

Working with SO Docker
======================

Verify that docker images are built
------------------------------------

.. code-block:: bash

  docker images

  *Example Output:*

  REPOSITORY                      TAG                            IMAGE ID            CREATED              SIZE
  onap/so/so-monitoring           1.3.0-SNAPSHOT                 bb8f368a3ddb        7 seconds ago        206MB
  onap/so/so-monitoring           1.3.0-SNAPSHOT-20190213T0846   bb8f368a3ddb        7 seconds ago        206MB
  onap/so/so-monitoring           1.3.0-SNAPSHOT-latest          bb8f368a3ddb        7 seconds ago        206MB
  onap/so/so-monitoring           latest                         bb8f368a3ddb        7 seconds ago        206MB
  onap/so/api-handler-infra       1.3.0-SNAPSHOT                 2573165483e9        21 seconds ago       246MB
  onap/so/api-handler-infra       1.3.0-SNAPSHOT-20190213T0846   2573165483e9        21 seconds ago       246MB
  onap/so/api-handler-infra       1.3.0-SNAPSHOT-latest          2573165483e9        21 seconds ago       246MB
  onap/so/api-handler-infra       latest                         2573165483e9        21 seconds ago       246MB
  onap/so/bpmn-infra              1.3.0-SNAPSHOT                 8b1487665f2e        38 seconds ago       324MB
  onap/so/bpmn-infra              1.3.0-SNAPSHOT-20190213T0846   8b1487665f2e        38 seconds ago       324MB
  onap/so/bpmn-infra              1.3.0-SNAPSHOT-latest          8b1487665f2e        38 seconds ago       324MB
  onap/so/bpmn-infra              latest                         8b1487665f2e        38 seconds ago       324MB
  onap/so/sdc-controller          1.3.0-SNAPSHOT                 c663bb7d7c0d        About a minute ago   241MB
  onap/so/sdc-controller          1.3.0-SNAPSHOT-20190213T0846   c663bb7d7c0d        About a minute ago   241MB
  onap/so/sdc-controller          1.3.0-SNAPSHOT-latest          c663bb7d7c0d        About a minute ago   241MB
  onap/so/sdc-controller          latest                         c663bb7d7c0d        About a minute ago   241MB
  onap/so/vfc-adapter             1.3.0-SNAPSHOT                 dee0005ef18b        About a minute ago   212MB
  onap/so/vfc-adapter             1.3.0-SNAPSHOT-20190213T0846   dee0005ef18b        About a minute ago   212MB
  onap/so/vfc-adapter             1.3.0-SNAPSHOT-latest          dee0005ef18b        About a minute ago   212MB
  onap/so/vfc-adapter             latest                         dee0005ef18b        About a minute ago   212MB
  onap/so/openstack-adapter       1.3.0-SNAPSHOT                 fe9103aa9f36        About a minute ago   235MB
  onap/so/openstack-adapter       1.3.0-SNAPSHOT-20190213T0846   fe9103aa9f36        About a minute ago   235MB
  onap/so/openstack-adapter       1.3.0-SNAPSHOT-latest          fe9103aa9f36        About a minute ago   235MB
  onap/so/openstack-adapter       latest                         fe9103aa9f36        About a minute ago   235MB
  onap/so/sdnc-adapter            1.3.0-SNAPSHOT                 d02d42d92b06        2 minutes ago        231MB
  onap/so/sdnc-adapter            1.3.0-SNAPSHOT-20190213T0846   d02d42d92b06        2 minutes ago        231MB
  onap/so/sdnc-adapter            1.3.0-SNAPSHOT-latest          d02d42d92b06        2 minutes ago        231MB
  onap/so/sdnc-adapter            latest                         d02d42d92b06        2 minutes ago        231MB
  onap/so/request-db-adapter      1.3.0-SNAPSHOT                 5e0136f2201b        2 minutes ago        215MB
  onap/so/request-db-adapter      1.3.0-SNAPSHOT-20190213T0846   5e0136f2201b        2 minutes ago        215MB
  onap/so/request-db-adapter      1.3.0-SNAPSHOT-latest          5e0136f2201b        2 minutes ago        215MB
  onap/so/request-db-adapter      latest                         5e0136f2201b        2 minutes ago        215MB
  onap/so/catalog-db-adapter      1.3.0-SNAPSHOT                 bf1c2fe49acb        2 minutes ago        218MB
  onap/so/catalog-db-adapter      1.3.0-SNAPSHOT-20190213T0846   bf1c2fe49acb        2 minutes ago        218MB
  onap/so/catalog-db-adapter      1.3.0-SNAPSHOT-latest          bf1c2fe49acb        2 minutes ago        218MB
  onap/so/catalog-db-adapter      latest                         bf1c2fe49acb        2 minutes ago        218MB
  onap/so/base-image              1.0                            1685bba9831d        3 minutes ago        108MB
  openjdk                         8-jdk-alpine                   792ff45a2a17        7 days ago           105MB
  nexus3.onap.org:10001/openjdk   8-jdk-alpine                   792ff45a2a17        7 days ago           105MB

Start the containers
---------------------

.. code-block:: bash

  cd $HOME/onap/workspace/SO/docker-config

  ./deploy.sh

  This should also download & start the mariaDB docker.

*Example Output:*

.. code-block:: bash

  Deploying with local images, not pulling them from Nexus.
  docker command: local docker using unix socket
  Removing network dockerconfig_default
  Creating network "dockerconfig_default" with driver "bridge"
  Pulling mariadb (mariadb:10.1.11)...
  10.1.11: Pulling from library/mariadb
  7268d8f794c4: Pull complete
  a3ed95caeb02: Pull complete
  e5a99361f38c: Pull complete
  20b20853e29d: Pull complete
  9dbc63cf121f: Pull complete
  fdebb5c64c6c: Pull complete
  3154860d3699: Pull complete
  3cfa7ffec11c: Pull complete
  943211713cac: Pull complete
  d65a44f4573e: Pull complete
  Digest: sha256:3821f92155bf4311a59b7ec6219b79cbf9a42c75805000a7c8fe5d9f3ad28276
  Status: Downloaded newer image for mariadb:10.1.11
  Creating dockerconfig_mariadb_1
  Waiting for 'dockerconfig_mariadb_1' deployment to finish ...
  Waiting for 'dockerconfig_mariadb_1' deployment to finish ...
  Waiting for 'dockerconfig_mariadb_1' deployment to finish ...
  Waiting for 'dockerconfig_mariadb_1' deployment to finish ...
  Waiting for 'dockerconfig_mariadb_1' deployment to finish ...
  Waiting for 'dockerconfig_mariadb_1' deployment to finish ...
  dockerconfig_mariadb_1 is up-to-date
  Creating dockerconfig_catalog-db-adapter_1
  Creating dockerconfig_request-db-adapter_1
  Creating dockerconfig_sdc-controller_1
  Creating dockerconfig_vfc-adapter_1
  Creating dockerconfig_openstack-adapter_1
  Creating dockerconfig_sdnc-adapter_1
  Creating dockerconfig_api-handler-infra_1
  Creating dockerconfig_so-monitoring_1
  Creating dockerconfig_bpmn-infra_1

Check containers are now up
----------------------------

.. code-block:: bash

  docker ps

  *Example Output:*

  CONTAINER ID        IMAGE                        COMMAND                  CREATED             STATUS              PORTS                     NAMES
  324ce4636285        onap/so/bpmn-infra           "/app/wait-for.sh ..."   5 minutes ago       Up 5 minutes        0.0.0.0:8081->8081/tcp    dockerconfig_bpmn-infra_1
  60986a742f6f        onap/so/so-monitoring        "/app/wait-for.sh ..."   5 minutes ago       Up 5 minutes        0.0.0.0:8088->8088/tcp    dockerconfig_so-monitoring_1
  ea6e3e396166        onap/so/api-handler-infra    "/app/wait-for.sh ..."   5 minutes ago       Up 5 minutes        0.0.0.0:8080->8080/tcp    dockerconfig_api-handler-infra_1
  473ca2dc852c        onap/so/sdnc-adapter         "/app/wait-for.sh ..."   5 minutes ago       Up 5 minutes        0.0.0.0:8086->8086/tcp    dockerconfig_sdnc-adapter_1
  7ae53b222a39        onap/so/vfc-adapter          "/app/wait-for.sh ..."   5 minutes ago       Up 5 minutes        0.0.0.0:8084->8084/tcp    dockerconfig_vfc-adapter_1
  8844999c9fc8        onap/so/openstack-adapter    "/app/wait-for.sh ..."   5 minutes ago       Up 5 minutes        0.0.0.0:8087->8087/tcp    dockerconfig_openstack-adapter_1
  d500c33665b6        onap/so/sdc-controller       "/app/wait-for.sh ..."   5 minutes ago       Up 5 minutes        0.0.0.0:8085->8085/tcp    dockerconfig_sdc-controller_1
  852483370df3        onap/so/request-db-adapter   "/app/wait-for.sh ..."   5 minutes ago       Up 5 minutes        0.0.0.0:8083->8083/tcp    dockerconfig_request-db-adapter_1
  cdfa29ee96cc        onap/so/catalog-db-adapter   "/app/wait-for.sh ..."   5 minutes ago       Up 5 minutes        0.0.0.0:8082->8082/tcp    dockerconfig_catalog-db-adapter_1
  7c7116026c07        mariadb:10.1.11              "/docker-entrypoin..."   5 minutes ago       Up 5 minutes        0.0.0.0:32770->3306/tcp   dockerconfig_mariadb_1

Check SO health
---------------
.. code-block:: bash

  curl http://localhost:8080/manage/health

  *Example Output:*

  {"status":"UP"}

Log into the mso container
--------------------------

.. code-block:: bash

  docker exec -it dockerconfig_api-handler-infra_1 sh

Inspect a docker image
----------------------

This command shows interesting information about the structure of the mso image.  Note that an image is NOT a running container.
It is the template that a container is created from.

.. code-block:: bash

  docker inspect onap/so/api-handler-infra
  Example Output:

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
