.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2018 Huawei Intellectual Property.  All rights reserved.


Service Orchestrator Release Notes
==================================

The SO provides the highest level of service orchestration in the ONAP architecture. 

Version: 1.3.1
--------------

:Release Date: 2018-09-23

Temp release for Casablanca at M4.
**New Features**

* Support PNF resource type.
* Extend the support of homing to vFW, VDNS usecases.
* Workflow Designer Integration.
* Monitoring BPMN worflow capabilities through UI.
* Support to the CCVPN Usecase.
* SO internal architecture improvements 


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
