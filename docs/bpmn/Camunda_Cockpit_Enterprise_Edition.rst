.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2017 Huawei Technologies Co., Ltd.

Camunda Cockpit Enterprise Edition
==================================

The Community Edition of Camunda is the version normally built into SO.  With the Community Edition, you can see process definitions, but you cannot inspect running or completed process instances, which is an essential debugging capability.  This capability is available only in the Enterprise Edition and requires a commercial license.  If you have such a license, you can build SO with the Enterprise Edition.  Your use must be consistent with the terms of your license, of course.

With the Enterprise Edition cockpit, you can:

  * See a trace of tasks executed by a running or completed process instance.
  * Look at execution variables in a running or completed process instance.
  * Look at called subprocesses in a running or completed process instance.

Maven Repository for Camunda EE Artifacts
------------------------------------------

To build with Camunda EE, you will need a maven repository containing the Camunda EE artifacts.   This can be a nexus repository or a local repository in your filesystem.

To construct a local repository, start with this structure for the 7.7.3-ee version of Camunda:

  camunda-ee-repository.zip_

.. _camunda-ee-repository.zip: https://wiki.onap.org/download/attachments/16001686/camunda-ee-repository.zip?version=1&modificationDate=1507838888000&api=v2

*NOTE*: the zip archive does not contain the actual Enterprise Edition JAR and WAR files.  It contains zero-length files as placeholders.  You will need to replace the zero-length files with files you obtain from Camunda.  The archive does contain all the poms, checksums, and metadata required to make it a functional maven repository.

Here's the minimum set of artifacts you need to build the SO Amsterdam release software:

  camunda-engine-7.7.3-ee.jar

  camunda-webapp-7.7.3-ee-classes.jar

  camunda-webapp-7.7.3-ee.war

  camunda-webapp-ee-jboss-standalone-7.7.3-ee.war

  camunda-engine-cdi-7.7.3-ee.jar

  camunda-engine-rest-7.7.3-ee-classes.jar

  camunda-engine-plugin-spin-7.7.3-ee.jar

  camunda-engine-plugin-connect-7.7.3-ee.jar

  camunda-engine-rest-core-7.7.3-ee.jar

  camunda-engine-feel-api-7.7.3-ee.jar

  camunda-engine-feel-juel-7.7.3-ee.jar

  camunda-engine-dmn-7.7.3-ee.jar

  camunda-engine-spring-7.7.3-ee.jar

  camunda-bpmn-model-7.7.3-ee.jar

  camunda-xml-model-7.7.3-ee.jar

  camunda-dmn-model-7.7.3-ee.jar

  camunda-cmmn-model-7.7.3-ee.jar

Maven settings.xml
------------------

Add a profile to your maven settings.xml to include the repository containing your Camunda EE artifacts.

For example:

.. code-block:: bash

  <profile>
  <!-- profile for artifacts not in public repositories -->
  <id>my-local-artifacts</id>
  <repositories>
    <repository>
      <!-- Local repository for Camunda Enterprise Edition -->
      <!-- YOU MUST HAVE A VALID LICENSE TO USE THIS -->
      <id>camunda-ee</id>
      <name>camunda-ee</name>
      <url>file:///home/john/onap/camunda-ee-repository</url>
    </repository>
  </repositories>
  </profile>

And add your profile to the list of active profiles:

.. code-block:: bash

  <activeProfiles>

  <activeProfile>my-local-artifacts</activeProfile>

  <activeProfile>openecomp-staging</activeProfile>

  <activeProfile>openecomp-public</activeProfile>

  <activeProfile>openecomp-release</activeProfile>

  <activeProfile>openecomp-snapshots</activeProfile>

  <activeProfile>opendaylight-release</activeProfile>

  <activeProfile>opendaylight-snapshots</activeProfile>

  </activeProfiles>

Building
--------

Add these options to the mvn command line when you build "so"

.. code-block:: bash

  -Dcamunda.version=7.7.3-ee -Dcamunda.bpm.webapp.artifact=camunda-webapp-ee-jboss-standalone

Installation
-------------

The cockpit is part of the SO image.  There are no special installation requirements.  When you log in, you will need to enter your license information.  Alternatively, you can insert your license directly into the camundabpmn database before you log in:

.. code-block:: bash

  INSERT INTO camundabpmn.ACT_GE_PROPERTY VALUES ('camunda-license-key','YOUR-LICENCE-KEY-HERE',1);

Logging In
-----------

Because port 8080 on the docker host machine is forwarded to port 8080 in the SO docker container you can log into the Enterprise Edition cockpit by browsing to this URL:

  http://*dockerhost*:8080/camunda

  Where dockerhost is the docker host system.

If you can use a browser on the docker host system, then use a localhost address:

  http://localhost:8080/camunda

The user is "admin" and the password is the same as the jboss admin password, which is not displayed here.

.. image:: images/Camunda_Cockpit_Enterprise_1.png
