.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2021 NOKIA, Ltd.

Goal
====

* Migrate PNF PNP workflow to Building Blocks (BBs/GR_API).
* Include newly created BBs in Service-Macro-Create flow.
* Leave legacy implementation using VNF_API intact.

**By PNF PNP workflow we understand 2 BPMNs:**

* CreateAndActivatePnfResource

.. image:: ../../images/CreateAndActivatePnfResource.png

* ConfigurePnfResource

.. image:: ../../images/ConfigurePnfResource.png

**Both included in CreateVcpeResCustService_simplified BPMN**

.. image:: ../../images/goal3.png
