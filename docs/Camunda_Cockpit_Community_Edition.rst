.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2017 Huawei Technologies Co., Ltd.

Camunda Cockpit Community Edition
=================================

On a deployed instance of SO, you can use the Camunda_Cockpit to view BPMN 2.0 workflow definitions in the deployment.

.. _Camunda_Cockpit: https://camunda.org/features/cockpit/

Unfortunately, the Community Edition of Camunda included with SO is not fully featured.  It cannot be used to inspect running and completed process instances.  For that, the Enterprise Edition is required.

Logging In
-----------

Because port 8080 on the docker host machine is forwarded to port 8080 in the SO docker container you can log into the cockpit by browsing to this URL:

  http://*dockerhost*:8080/camunda/app/admin/default/#/login

  Where dockerhost is the docker host system.

If you can use a browser on the docker host system, then use a localhost address:

  http://localhost:8080/camunda/app/admin/default/#/login

The user is "admin" and the password is the same as the jboss admin password, which is not displayed here.

.. image:: images/Camunda_Cockpit_1.png

Viewing Process Definitions
---------------------------

Use the drop-down menu next to the home icon and select the "Cockpit" option:

.. image:: images/Camunda_Cockpit_2.png

The number of deployed process definitions should be displayed.  Click on it.

.. image:: images/Camunda_Cockpit_3.png

Now you should see an actual list of deployed process definitions.  You can click on any of them to view them.

.. image:: images/Camunda_Cockpit_4.png
