.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2018 Huawei Intellectual Property.  All rights reserved.


Service Orchestrator Release Notes
==================================

The SO provides the highest level of service orchestration in the ONAP architecture. 

Version: 1.3.7
--------------

:Release Date: 2019-01-31

This is the official release package that released for the Casablanca Maintenance.

Casablanca Release branch

**New Features**

This release is supporting the features of Casablanca and their defect fixes.
- `SO-1400 <https://jira.onap.org/browse/SO-1336>`_
- `SO-1408 <https://jira.onap.org/browse/SO-1408>`_
- `SO-1416 <https://jira.onap.org/browse/SO-1416>`_
- `SO-1417 <https://jira.onap.org/browse/SO-1417>`_

**Docker Images**

Dockers released for SO:

 - onap/so/api-handler-infra,1.3.7
 - onap/so/bpmn-infra,1.3.7
 - onap/so/catalog-db-adapter,1.3.7
 - onap/so/openstack-adapter,1.3.7
 - onap/so/request-db-adapter,1.3.7
 - onap/so/sdc-controller,1.3.7
 - onap/so/sdnc-adapter,1.3.7
 - onap/so/so-monitoring,1.3.7
 - onap/so/vfc-adapter,1.3.7

**Known Issues**

- `SO-1419 <https://jira.onap.org/browse/SO-1419>`_ - is a stretch goal that is under examination.

- `SDC-1955 <https://jira.onap.org/browse/SDC-1955>`_ - tested with a workaround to avoid this scenario. To be tested further with updated dockers of SDC, UUI and SO.

**Security Notes**

	SO code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SO open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://wiki.onap.org/pages/viewpage.action?pageId=43385708>`_.

	Quick Links:

 - `SO project page <https://wiki.onap.org/display/DW/Service+Orchestrator+Project>`_
 - `Passing Badge information for SO <https://bestpractices.coreinfrastructure.org/en/projects/1702>`_
 - `Project Vulnerability Review Table for SO <https://wiki.onap.org/pages/viewpage.action?pageId=43385708>`_


Version: 1.3.6
--------------

:Release Date: 2019-01-10

This is the official release package that released for the Casablanca Maintenance.

Casablanca Release branch

**New Features**

This release is supporting the features of Casablanca and their defect fixes.
- `SO-1336 <https://jira.onap.org/browse/SO-1336>`_
- `SO-1249 <https://jira.onap.org/browse/SO-1249>`_
- `SO-1257 <https://jira.onap.org/browse/SO-1257>`_
- `SO-1258 <https://jira.onap.org/browse/SO-1258>`_
- `SO-1256 <https://jira.onap.org/browse/SO-1256>`_
- `SO-1194 <https://jira.onap.org/browse/SO-1256>`_
- `SO-1248 <https://jira.onap.org/browse/SO-1248>`_
- `SO-1184 <https://jira.onap.org/browse/SO-1184>`_

**Docker Images**

Dockers released for SO:

 - onap/so/api-handler-infra,1.3.6
 - onap/so/bpmn-infra,1.3.6
 - onap/so/catalog-db-adapter,1.3.6
 - onap/so/openstack-adapter,1.3.6
 - onap/so/request-db-adapter,1.3.6
 - onap/so/sdc-controller,1.3.6
 - onap/so/sdnc-adapter,1.3.6
 - onap/so/so-monitoring,1.3.6
 - onap/so/vfc-adapter,1.3.6

**Known Issues**


**Security Notes**

	SO code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SO open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://wiki.onap.org/pages/viewpage.action?pageId=43385708>`_.

	Quick Links:

 - `SO project page <https://wiki.onap.org/display/DW/Service+Orchestrator+Project>`_
 - `Passing Badge information for SO <https://bestpractices.coreinfrastructure.org/en/projects/1702>`_
 - `Project Vulnerability Review Table for SO <https://wiki.onap.org/pages/viewpage.action?pageId=43385708>`_


Version: 1.3.3
--------------

:Release Date: 2018-11-30

This is the official release package that was tested against the 72 hour stability test in integration environment.

Casablanca Release branch

**New Features**

Features delivered in this release:

 - Automatic scale out of VNFs.
 - Extend the support of homing to vFW, vCPE usecases through HPA.
 - Monitoring BPMN workflow capabilities through UI.
 - SO internal architecture improvements.
 - Support PNF resource type.
 - Support to the CCVPN Usecase.
 - Workflow Designer Integration.

**Docker Images**

Dockers released for SO:

 - onap/so/api-handler-infra,1.3.3
 - onap/so/bpmn-infra,1.3.3
 - onap/so/catalog-db-adapter,1.3.3
 - onap/so/openstack-adapter,1.3.3
 - onap/so/request-db-adapter,1.3.3
 - onap/so/sdc-controller,1.3.3
 - onap/so/sdnc-adapter,1.3.3
 - onap/so/so-monitoring,1.3.3
 - onap/so/vfc-adapter,1.3.3

**Known Issues**

There are some issues around the HPA and CCVPN that have been resolved in the patch release of 1.3.5
- `SO-1249 <https://jira.onap.org/browse/SO-1249>`_
- `SO-1257 <https://jira.onap.org/browse/SO-1257>`_
- `SO-1258 <https://jira.onap.org/browse/SO-1258>`_
- `SO-1256 <https://jira.onap.org/browse/SO-1256>`_
- `SO-1194 <https://jira.onap.org/browse/SO-1256>`_
	
Below issues will be resolved in the next release:
- `SO-1248 <https://jira.onap.org/browse/SO-1248>`_
- `SO-1184 <https://jira.onap.org/browse/SO-1184>`_

**Security Notes**

	SO code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SO open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://wiki.onap.org/pages/viewpage.action?pageId=43385708>`_.

	Quick Links:

 - `SO project page <https://wiki.onap.org/display/DW/Service+Orchestrator+Project>`_
 - `Passing Badge information for SO <https://bestpractices.coreinfrastructure.org/en/projects/1702>`_
 - `Project Vulnerability Review Table for SO <https://wiki.onap.org/pages/viewpage.action?pageId=43385708>`_

Version: 1.3.1
--------------

:Release Date: 2018-10-24

Branch cut for Casablanca post M4 for integration test.
**New Features**

Below  features are under test:
 - Automatic scale out of VNFs.
 - Extend the support of homing to vFW, vCPE usecases through HPA.
 - Monitoring BPMN workflow capabilities through UI.
 - SO internal architecture improvements.
 - Support PNF resource type.
 - Support to the CCVPN Usecase.
 - Workflow Designer Integration.


Version: 1.3.0
--------------

:Release Date: 2018-08-22

New  release over  master branch for Casablanca development

Version: 1.2.2
--------------

:Release Date: 2018-06-07

The Beijing release is the second release of the Service Orchestrator (SO) project.

**New Features**

* Enhance Platform maturity by improving SO maturity matrix see `Wiki <https://wiki.onap.org/display/DW/Beijing+Release+Platform+Maturity>`_.
* Manual scaling of network services and VNFs.
* Homing and placement capabilities through OOF interaction. 
* Ability to perform change management.
* Integrated to APPC
* Integrated to OOF 
* Integrated to OOM
 
**Bug Fixes**

	The defects fixed in this release could be found `here <https://jira.onap.org/issues/?jql=project%20%3D%20SO%20AND%20affectedVersion%20%3D%20%22Beijing%20Release%22%20AND%20status%20%3D%20Closed%20>`_.

**Known Issues**

	SO docker image is still on ecmop and not onap in the repository. 
	This will be addressed in the next release.

**Security Notes**

	SO code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SO open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://wiki.onap.org/pages/viewpage.action?pageId=28377799>`_.

Quick Links:

- `SO project page <https://wiki.onap.org/display/DW/Service+Orchestrator+Project>`_
- `Passing Badge information for SO <https://bestpractices.coreinfrastructure.org/en/projects/1702>`_
- `Project Vulnerability Review Table for SO <https://wiki.onap.org/pages/viewpage.action?pageId=28377799>`_

**Upgrade Notes**
	NA

**Deprecation Notes**
	NA

Version: 1.1.2
--------------

:Release Date: 2018-01-18

Bug Fixes
---------
The key defects fixed in this release :

- `SO-344 <https://jira.onap.org/browse/SO-344>`_
  Only pass one VNF to DoCreateVnfAndModules.

- `SO-348 <https://jira.onap.org/browse/SO-348>`_
  Json Analyze Exception in PreProcessRequest.

- `SO-352 <https://jira.onap.org/browse/SO-352>`_
  SO failed to create VNF - with error message: Internal Error Occurred in CreateVnfInfra QueryCatalogDB Process.

- `SO-354 <https://jira.onap.org/browse/SO-354>`_
  Change the Service Type And Service Role


Version: 1.1.1
--------------

:Release Date: 2017-11-16


**New Features**

The SO provides the highest level of service orchestration in the ONAP architecture.
It executes end-to-end service activities by processing workflows and business logic and coordinating other ONAP and external component activities. 

The orchestration engine is a reusable service. Any component of the architecture can execute SO orchestration capabilities. 

* Orchestration services will process workflows based on defined models and recipe. 
* The service model maintains consistency and re-usability across all orchestration activities and ensures consistent methods, structure and version of the workflow execution environment.
* Orchestration processes interact with other platform components or external systems via standard and well-defined APIs.


**Deprecation Notes**

There is a MSO 1.0.0 SO implementation existing in the pre-R1 ONAP Gerrit system.  
The MSO1.0.0 is deprecated by the R1 release and the current release is built over this release.
The Gerrit repos of mso/* are voided and already locked as read-only.
Following are the deprecated SO projects in gerrit repo:

- mso
- mso/chef-repo
- mso/docker-config
- mso/libs
- mso/mso-config
	
**Other**
	NA

===========

End of Release Notes
