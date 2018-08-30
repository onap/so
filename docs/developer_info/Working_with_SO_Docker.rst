.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2017 Huawei Technologies Co., Ltd.

Working with SO Docker
======================

Verify that docker images are built
------------------------------------

.. code-block:: bash

  docker images openecomp/mso

  *Example Output:*

  REPOSITORY     TAG                                  IMAGE ID     CREATED       SIZE

  openecomp/mso  1.1-SNAPSHOT-latest                  419e9d8a17e8 3 minutes ago 1.62GB

  openecomp/mso  1.1.0-SNAPSHOT-STAGING-20170926T2015 419e9d8a17e8 3 minutes ago 1.62GB

  openecomp/mso  latest                               419e9d8a17e8 3 minutes ago 1.62GB

Start the mariadb container
----------------------------

.. code-block:: bash

  cd $HOME/onap/workspace/SO/docker-config

  MTU=1500 docker-compose up mariadb

*Example Output:*

.. code-block:: bash

  . . . many lines omitted . . .
  mariadb_1  | Version: '10.1.11-MariaDB-1~jessie-log'  socket: '/var/run/mysqld/mysqld.sock'  port: 3306  mariadb.org binary distribution

Log into the mariadb container and run the mysql client program
---------------------------------------------------------------

.. code-block:: bash

  docker exec -it dockerconfig_mariadb_1 /bin/bash
  mysql -uroot -ppassword

Start the mso container
-----------------------

.. code-block:: bash

  cd $HOME/onap/workspace/SO/docker-config

  MTU=1500 docker-compose up mso

*Example Output:*

.. code-block:: bash

  . . . many lines omitted . . .
  mso_1      | 20:59:31,586 INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: WildFly Full 10.1.0.Final 
  (WildFly Core 2.2.0.Final) started in 59937ms - Started 2422 of 2747 services (604 services are lazy, passive or
  on-demand)

Log into the mso container
--------------------------

.. code-block:: bash

  docker exec -it dockerconfig_mso_1 /bin/bash

Inspect a docker image
----------------------

This command shows interesting information about the structure of the mso image.  Note that an image is NOT a running container.  It is the template that a container is created from.

.. code-block:: bash

  docker inspect openecomp/mso
  Example Output:

  [
    {
        "Id": "sha256:419e9d8a17e8d7e876dfc36c1f3ed946bccbb29aa6faa6cd8e32fbc77c0ef6e5",
        "RepoTags": [
            "openecomp/mso:1.1-SNAPSHOT-latest",
            "openecomp/mso:1.1.0-SNAPSHOT-STAGING-20170926T2015",
            "openecomp/mso:latest"
        ],
        "RepoDigests": [],
        "Parent": "sha256:70f1ba3d6289411fce96ba78755a3fd6055a370d33464553d72c753889b12693",
        "Comment": "",
        "Created": "2017-09-26T20:40:10.179358574Z",
        "Container": "284aa05909390a3c0ffc1ec6d0f6e2071799d56b08369707505897bc73d2ea30",
        "ContainerConfig": {
            "Hostname": "6397aa10f0c4",
            "Domainname": "",
            "User": "root",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "ExposedPorts": {
                "8080/tcp": {}
            },
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "HTTP_PROXY=",
                "HTTPS_PROXY=",
                "http_proxy=",
                "https_proxy=",
                "JBOSS_HOME=/opt/jboss",
                "CHEF_REPO_NAME=chef-repo",
                "CHEF_CONFIG_NAME=mso-config"
            ],
            "Cmd": [
                "/bin/sh",
                "-c",
                "#(nop) ",
                "CMD [\"/opt/mso/scripts/start-jboss-server.sh\"]"
            ],
            "ArgsEscaped": true,
            "Image": "sha256:70f1ba3d6289411fce96ba78755a3fd6055a370d33464553d72c753889b12693",
            "Volumes": {
                "/shared": {}
            },
            "WorkingDir": "",
            "Entrypoint": null,
            "OnBuild": [],
            "Labels": {
                "Description": "This image contains the ONAP SO",
                "Version": "1.0"
            }
        },
        "DockerVersion": "17.05.0-ce",
        "Author": "\"The ONAP Team\"",
        "Config": {
            "Hostname": "6397aa10f0c4",
            "Domainname": "",
            "User": "root",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "ExposedPorts": {
                "8080/tcp": {}
            },
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "HTTP_PROXY=",
                "HTTPS_PROXY=",
                "http_proxy=",
                "https_proxy=",
                "JBOSS_HOME=/opt/jboss",
                "CHEF_REPO_NAME=chef-repo",
                "CHEF_CONFIG_NAME=mso-config"
            ],
            "Cmd": [
                "/opt/mso/scripts/start-jboss-server.sh"
            ],
            "ArgsEscaped": true,
            "Image": "sha256:70f1ba3d6289411fce96ba78755a3fd6055a370d33464553d72c753889b12693",
            "Volumes": {
                "/shared": {}
            },
            "WorkingDir": "",
            "Entrypoint": null,
            "OnBuild": [],
            "Labels": {
                "Description": "This image contains the ONAP SO",
                "Version": "1.0"
            }
        },
        "Architecture": "amd64",
        "Os": "linux",
        "Size": 1616881263,
        "VirtualSize": 1616881263,
        "GraphDriver": {
            "Data": null,
            "Name": "aufs"
        },
        "RootFS": {
            "Type": "layers",
            "Layers": [
                "sha256:a2022691bf950a72f9d2d84d557183cb9eee07c065a76485f1695784855c5193",
                "sha256:ae620432889d2553535199dbdd8ba5a264ce85fcdcd5a430974d81fc27c02b45",
                . . .  many lines omitted . . .
                "sha256:0f9e9dacce9191617e979f05e32ee782b1632e07130fd7fee19b0b2d635aa006",
                "sha256:84572c6389f8ae41150e14a8f1a28a70720de91ab1032f8755b5449dc04449c9"
            ]
        }
    }
]

Log into the mso image
-----------------------

This command allows you to inspect the files inside the mso image.  Note that an image is NOT a running container.  It is the template that a container is created from.

.. code-block:: bash

  docker run -it --entrypoint=/bin/bash openecomp/mso -i
