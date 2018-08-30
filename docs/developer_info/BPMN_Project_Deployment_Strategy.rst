.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2017 Huawei Technologies Co., Ltd.

BPMN Project Deployment Strategy
==================================

Single Application with Embedded Process Engine
------------------------------------------------

Deployment in SO is currently limited to a single process application: MSOInfrastructureBPMN.  The WAR file for this application contains everything needed to execute the infrastructure process flows, including:

  * BPMN process flows, java classes, groovy scripts, and resource files from MSOInfrastructureBPMN itself.

  * BPMN process flows, java classes, groovy scripts, and resource files from other SO projects, like MSOCommonBPMN and MSOCoreBPMN.

  * An embedded Camunda Process Engine to execute the flows.

The process application exposes a REST endpoint to the API Handler(s) for receiving flow execution requests.

Development is required in SO to be able to support one a more versatile deployment strategy, probably one of the following:

Shared Process Engine
----------------------

The Camunda Process Engine is created and manged as a Wildfly module.  This single engine is shared by all process applications.

Multiple Applications, each with an Embedded Process Engine
-------------------------------------------------------------

More than one application could be deployed, each having its own embedded process engine.




