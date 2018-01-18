.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2017 Huawei Intellectual Property.  All rights reserved.


Service Orchestrator Release Notes
==================================

The SO provides the highest level of service orchestration in the ONAP architecture. 

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
* The service model maintains consistency and reusability across all orchestration activities and ensures consistent methods, structure and version of the workflow execution environment.
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

===========

End of Release Notes
