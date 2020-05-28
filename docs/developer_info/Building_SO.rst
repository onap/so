.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2018 Huawei Technologies Co., Ltd.

Building SO
============

Build software with unit tests
------------------------------

.. code-block:: bash

  cd $HOME/onap/workspace/SO/libs

  $HOME/onap/apache-maven-3.3.9/bin/mvn -s $HOME/onap/.m2/settings.xml clean install

  cd $HOME/onap/workspace/SO/so

  $HOME/onap/apache-maven-3.3.9/bin/mvn -s $HOME/onap/.m2/settings.xml clean install

Build software without unit tests
----------------------------------

.. code-block:: bash

  cd $HOME/onap/workspace/SO/libs

  $HOME/onap/apache-maven-3.3.9/bin/mvn -s $HOME/onap/.m2/settings.xml -DskipTests -Dmaven.test.skip=true clean install

  cd $HOME/onap/workspace/SO/so

  $HOME/onap/apache-maven-3.3.9/bin/mvn -s $HOME/onap/.m2/settings.xml -DskipTests -Dmaven.test.skip=true clean install

Build docker images
--------------------

SO docker images are built using the "docker" maven profile.  

During the build, the chef-repo and so-docker repositories are cloned from gerrit into the "so" directory structure.  Extra definitions are required in the build environment to make this happen.   You may need to adjust the definition of mso.chef.git.url.prefix to match the way you authenticate yourself when performing git clone --depth 1.

If you are behind a corporate firewall, you can specify proxy definitions for the constructed docker images.

**Remove existing docker containers and images**

.. code-block:: bash

  docker stop $(docker ps -qa)

  docker rm $(docker ps -aq)

  docker rmi -f $(docker images -q)

**Build docker images (without proxy definition):**

.. code-block:: bash

  cd $HOME/onap/workspace/SO/so/packages

  $HOME/onap/apache-maven-3.3.9/bin/mvn -s $HOME/onap/.m2/settings.xml clean install -P docker
  -Dmso.chef.git.url.prefix=ssh://$USER@gerrit.onap.org:29418 -Dmso.chef.git.branchname=master
  -Dmso.chef.git.url.suffix.chef.repo=so/chef-repo -Dmso.chef.git.url.suffix.chef.config=so/so-config
  -Ddocker.buildArg.http_proxy=http://one.proxy.att.com:8080
  -Ddocker.buildArg.https_proxy=http://one.proxy.att.com:8080

**Build docker images (with proxy definition):**

.. code-block:: bash

  cd $HOME/onap/workspace/SO/so/packages
  
  $HOME/onap/apache-maven-3.3.9/bin/mvn -s $HOME/onap/.m2/settings.xml clean install -P docker
  -Dmso.chef.git.url.prefix=ssh://$USER@gerrit.onap.org:29418 -Dmso.chef.git.branchname=master
  -Dmso.chef.git.url.suffix.chef.repo=so/chef-repo -Dmso.chef.git.url.suffix.chef.config=so/so-config
  -Ddocker.buildArg.http_proxy=http://proxyhost:port -Ddocker.buildArg.https_proxy=http://proxyhost:port

Build with Integration Tests
-----------------------------

This is done exactly as described for building docker images, except that the maven profile to use is "with-integration-tests" instead of "docker".  Integration tests are executed inside docker containers constructed by the build.


