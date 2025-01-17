.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2018 Huawei Intellectual Property.  All rights reserved.
.. _release_notes:


Service Orchestrator Release Notes
==================================

The SO provides the highest level of service orchestration in the ONAP architecture. 

Version: 1.15.0
---------------

:Release Date: 2025-01-17

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~

 - so-bpmn-infra **1.15.0**

 - so-catalog-db-adapter **1.15.0**

 - so-openstack-adapter **1.15.0**

 - so-request-db-adapter **1.15.0**

 - so-sdc-controller **1.15.0**

 - so-sdnc-adapter **1.15.0**

 - so-api-handler **1.15.0**


Release Purpose
~~~~~~~~~~~~~
SO OSLO Release.
The key deliverable for this release is refactoring SO for better internal architecture.

**Epics**

*  `SO-4127 <https://lf-onap.atlassian.net/browse/SO-4127>`_ - ONAP RFC 8040 Migration - Oslo Enhancements

**********************************************************************************************************



Version: 1.11.0
---------------

:Release Date: 2022-09-08

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - so-bpmn-infra **1.11.0**

 - so-catalog-db-adapter **1.11.0**

 - so-admin-cockpit **1.8.3**

 - so-nssmf-adapter **1.9.1**

 - so-openstack-adapter **1.11.0**

 - so-request-db-adapter **1.11.0**

 - so-sdc-controller **1.11.0**

 - so-sdnc-adapter **1.11.0**

 - so-sol003-adapter **1.8.2**

 - so-api-handler **1.11.0**

 - so-etsi-nfvo-ns-lcm **1.8.2**

 - so-oof-adapter **1.8.3**

 - so-cnf-adapter **1.11.0**

Release Purpose
~~~~~~~~~~~~~~~
SO Kohn Release.
The key deliverable for this release is refactoring SO for better internal architecture.


**Epics**

*  `REQ-890 <https://jira.onap.org/browse/REQ-890>`_ - ONAP CNF orchestration - Kohn Enhancements
*  `REQ-1041 <https://jira.onap.org/browse/REQ-1041>`_ - VNF LCM Support in SO
*  `SO-3802 <https://jira.onap.org/browse/SO-3802>`_ - Global Requirements Approval
*  `SO-3826 <https://jira.onap.org/browse/SO-3826>`_ - SO impacts for E2E Network Slicing in Kohn Release

**Stories**

The full list of implemented tasks is available on `JIRA Kohn STORY <https://jira.onap.org/browse/SO-3748?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20story%20AND%20fixVersion%20%3D%20%22Kohn%20Release%22>`_


**Tasks**

The full list of implemented tasks is available on `JIRA Kohn TASKS <https://jira.onap.org/browse/SO-3930?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20Task%20AND%20fixVersion%20%3D%20%22Kohn%20Release%22>`_

**Bug Fixes**

The full list of fixed bugs is available on `JIRA Kohn BUGS 
<https://jira.onap.org/browse/SO-3908?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20Bug%20AND%20fixVersion%20%3D%20%22Kohn%20Release%22>`_

Security Notes
~~~~~~~~~~~~~~

*Fixed Security Issues*

*  `SO-3735 <https://jira.onap.org/browse/SO-3735>`_ 
*  `SO-3825 <https://jira.onap.org/browse/SO-3825>`_
*  `SO-3846 <https://jira.onap.org/browse/SO-3846>`_


Quick Links:

- `SO project page`_
- `Passing Badge information for SO`_

**Known Issues**

*  `SO-3237`_ - Exposed HTTP port. 
*  `SO-3745 <https://jira.onap.org/browse/SO-3745>`_ - SO images contain 1 GPLv3 lib

**Upgrade Notes**

	N/A

**Deprecation Notes**

	SO modules Ve-Vnfm-adapter and appc-orchestrator are deprectaed since istanbul release.

**Other**

	N/A

***************************************************************************************


Version: 1.10.0
---------------

:Release Date: 2022-04-08

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - so-bpmn-infra **1.10.0**

 - so-catalog-db-adapter **1.10.0**

 - so-admin-cockpit **1.8.3**

 - so-nssmf-adapter **1.9.1**

 - so-openstack-adapter **1.10.0**

 - so-request-db-adapter **1.10.0**

 - so-sdc-controller **1.10.0**

 - so-sdnc-adapter **1.10.0**

 - so-sol003-adapter **1.8.2**

 - so-api-handler **1.10.0**

 - so-etsi-nfvo-ns-lcm **1.8.2**

 - so-oof-adapter **1.8.3**

 - so-cnf-adapter **1.10.0**

Release Purpose
~~~~~~~~~~~~~~~
SO Jakarta Release.
The key deliverable for this release is refactoring SO for better internal architecture.


**Epics**

*  `REQ-890 <https://jira.onap.org/browse/REQ-890>`_ - ONAP CNF orchestration - Jakarta Enhancements
*  `REQ-1041 <https://jira.onap.org/browse/REQ-1041>`_ - VNF LCM Support in SO
*  `SO-3802 <https://jira.onap.org/browse/SO-3802>`_ - Global Requirements Approval
*  `SO-3826 <https://jira.onap.org/browse/SO-3826>`_ - SO impacts for E2E Network Slicing in Jakarta Release

**Stories**

The full list of implemented tasks is available on `JIRA Jakarta STORY <https://jira.onap.org/browse/SO-3748?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20story%20AND%20fixVersion%20%3D%20%22Jakarta%20Release%22>`_


**Tasks**

The full list of implemented tasks is available on `JIRA Jakarta TASKS <https://jira.onap.org/browse/SO-3930?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20Task%20AND%20fixVersion%20%3D%20%22Jakarta%20Release%22>`_

**Bug Fixes**

The full list of fixed bugs is available on `JIRA Jakarta BUGS 
<https://jira.onap.org/browse/SO-3908?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20Bug%20AND%20fixVersion%20%3D%20%22jakarta%20Release%22>`_

Security Notes
~~~~~~~~~~~~~~

*Fixed Security Issues*

*  `SO-3735 <https://jira.onap.org/browse/SO-3735>`_ 
*  `SO-3825 <https://jira.onap.org/browse/SO-3825>`_
*  `SO-3846 <https://jira.onap.org/browse/SO-3846>`_


Quick Links:

- `SO project page`_
- `Passing Badge information for SO`_

**Known Issues**

*  `SO-3237`_ - Exposed HTTP port. 
*  `SO-3745 <https://jira.onap.org/browse/SO-3745>`_ - SO images contain 1 GPLv3 lib

**Upgrade Notes**

	N/A

**Deprecation Notes**

	SO modules Ve-Vnfm-adapter and appc-orchestrator are deprectaed since istanbul release.

**Other**

	N/A

***************************************************************************************



Version: 1.9.2
--------------

:Release Date: 2021-10-14

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - so-bpmn-infra **1.9.2**

 - so-catalog-db-adapter **1.9.2**

 - so-admin-cockpit **1.8.3**

 - so-nssmf-adapter **1.9.1**

 - so-openstack-adapter **1.9.2**

 - so-request-db-adapter **1.9.2**

 - so-sdc-controller **1.9.2**

 - so-sdnc-adapter **1.9.2**

 - so-sol003-adapter **1.8.2**

 - so-api-handler-infra **1.9.2**

 - so-etsi-nfvo-ns-lcm **1.8.2**

 - so-oof-adapter **1.8.3**

 - so-cnf-adapter **1.9.1**

Release Purpose
~~~~~~~~~~~~~~~
SO Istanbul Release.
The key deliverable for this release is refactoring SO for better internal architecture.


**Epics**

*  `REQ-627 <https://jira.onap.org/browse/REQ-627>`_ - ONAP CNF orchestration - Istanbul Enhancements
*  `SO-3649 <https://jira.onap.org/browse/SO-3649>`_ - SO impacts for E2E Network Slicing in Istanbul Release
*  `SO-3637 <https://jira.onap.org/browse/SO-3637>`_ - Global Requirements Approval
*  `SO-3473 <https://jira.onap.org/browse/SO-3473>`_ - Refactor SO to enhance the usability


**Stories**

The full list of implemented tasks is available on `JIRA Istanbul STORY <https://jira.onap.org/issues/?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20story%20AND%20fixVersion%20%3D%20%22istanbul%20Release%22>`_


**Tasks**

The full list of implemented tasks is available on `JIRA Istanbul TASKS <https://jira.onap.org/issues/?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20Task%20AND%20fixVersion%20%3D%20%22istanbul%20Release%22>`_

**Bug Fixes**

The full list of fixed bugs is available on `JIRA Istanbul BUGS 
<https://jira.onap.org/issues/?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20Bug%20AND%20fixVersion%20%3D%20%22istanbul%20Release%22>`_

Security Notes
~~~~~~~~~~~~~~

*Fixed Security Issues*

*  `SO-3642 <https://jira.onap.org/browse/SO-3642>`_
*  `SO-3724 <https://jira.onap.org/browse/SO-3724>`_


Quick Links:

- `SO project page`_
- `Passing Badge information for SO`_

**Known Issues**

*  `SO-3237`_ - Exposed HTTP port. 


**Upgrade Notes**

	N/A

**Deprecation Notes**

	SO modules Ve-Vnfm-adapter and appc-orchestrator are deprectaed for the Istanbul release.

**Other**

	N/A

***************************************************************************************

Version: 1.8.3
--------------

:Release Date: 2021-09-15

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - so-bpmn-infra **1.8.3**

 - so-catalog-db-adapter **1.8.3**

 - so-nssmf-adapter **1.8.3**

 - so-openstack-adapter **1.8.3**

 - so-request-db-adapter **1.8.3**

 - so-sdc-controller **1.8.3**

 - so-sdnc-adapter **1.8.3**

 - so-api-handler-infra **1.8.3**

Release Purpose
~~~~~~~~~~~~~~~
SO Honolulu Maintence Release.
The key delivereable for this release is fixing the known issues of H release of SO and sync up with the latest CDS client version.


**Epics**

	N/A

**Stories**

	N/A

**Tasks**

	N/A

**Bug Fixes**

*  `SO-3626 <https://jira.onap.org/browse/SO-3626>`_ - SO does not requests CDS for skipPostInstantiation flag set to False.
*  `SO-3628 <https://jira.onap.org/browse/SO-3628>`_ - SO cannot send CDS request due to grpc schema problem.
*  `SO-3703 <https://jira.onap.org/browse/SO-3703>`_ - Changes in Modify and Deallocate Core NSST flows
*  `SO-3721 <https://jira.onap.org/browse/SO-3721>`_ - Fix some attribute issues
*  `SO-3260 <https://jira.onap.org/browse/SO-3260>`_ - Wrong additional parameter for OOF's terminateNxiRequest


Security Notes
~~~~~~~~~~~~~~

*Fixed Security Issues*

*Known Security Issues*

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SO project page`_
- `Passing Badge information for SO`_

**Known Issues**

*  `SO-3237`_ - Exposed HTTP port. 


**Upgrade Notes**

	N/A

**Deprecation Notes**

	SO modules Ve-Vnfm-adapter and appc-orchestrator are deprectaed for the Honolulu release.

**Other**

	N/A

***************************************************************************************


Version: 8.0.0
--------------

:Release Date: 2021-04-19

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - so-bpmn-infra **1.8.2**

 - so-catalog-db-adapter **1.8.2**

 - so-admin-cockpit **1.8.2**

 - so-nssmf-adapter **1.8.3**

 - so-openstack-adapter **1.8.2**

 - so-request-db-adapter **1.8.2**

 - so-sdc-controller **1.8.2**

 - so-sdnc-adapter **1.8.2**

 - so-sol003-adapter **1.8.2**

 - so-api-handler-infra **1.8.2**

 - so-etsi-nfvo-ns-lcm **1.8.2**

 - so-oof-adapter **1.8.3**

 - so-cnf-adapter **1.9.1**

Release Purpose
~~~~~~~~~~~~~~~
SO Honolulu Release.
The key delivereable for this release is refactoring SO for better internal architecture.


**Epics**

*  `SO-3473 <https://jira.onap.org/browse/SO-3473>`_ - Refactor SO to enhance the usability
*  `SO-3381 <https://jira.onap.org/browse/SO-3381>`_ - SO Impacts for E2E Network Slicing in Honolulu
*  `SO-3206 <https://jira.onap.org/browse/SO-3206>`_ - Support for NS LCM and Workflows Management
*  `SO-3493 <https://jira.onap.org/browse/SO-3493>`_ - Java 11 and Python 3 upgrades

**Stories**

The full list of implemented tasks is available on `JIRA Honolulu STORY <https://jira.onap.org/issues/?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20story%20AND%20fixVersion%20%3D%20%22honolulu%20Release%22>`_
Listed below are key functional jira stories handled in the Honolulu release:


**Tasks**

The full list of implemented tasks is available on `JIRA Honolulu TASKS <https://jira.onap.org/issues/?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20Task%20AND%20fixVersion%20%3D%20%22honolulu%20Release%22>`_

**Bug Fixes**

The full list of fixed bugs is available on `JIRA Honolulu BUGS 
<https://jira.onap.org/issues/?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20Bug%20AND%20fixVersion%20%3D%20%22honolulu%20Release%22>`_



Security Notes
~~~~~~~~~~~~~~

*Fixed Security Issues*

*Known Security Issues*

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SO project page`_
- `Passing Badge information for SO`_

**Known Issues**
*  `SO-3628 <https://jira.onap.org/browse/SO-3628>`_ - SO cannot send CDS request due to grpc schema problem.
*  `SO-3626 <https://jira.onap.org/browse/SO-3626>`_ - SO does not requests CDS for skipPostInstantiation flag set to False.
*  `SO-3237`_ - Exposed HTTP port. 


**Upgrade Notes**

	N/A

**Deprecation Notes**

	SO modules Ve-Vnfm-adapter and appc-orchestrator are deprectaed for the Honolulu release.

**Other**

	N/A

***************************************************************************************



Version: 1.7.10
---------------

:Release Date: 2020-11-19

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - so-bpmn-infra **1.7.10**

 - so-catalog-db-adapter **1.7.10**

 - so-monitoring **1.7.10**

 - so-nssmf-adapter **1.7.10**

 - so-openstack-adapter **1.7.10**

 - so-request-db-adapter **1.7.10**

 - so-sdc-controller **1.7.10**

 - so-sdnc-adapter **1.7.10**

 - so-vnfm-adapter **1.7.10**

 - so-api-handler-infra **1.7.10**

 - so-api-handler-infra **1.7.10**

 - so-so-etsi-nfvo-ns-lcm **1.7.7**

 - so-so-oof-adapter **1.7.6**

 - so-cnf-adapter **1.7.10**

Release Purpose
~~~~~~~~~~~~~~~
SO Guilin Release

**Epics**

*  `SO-3167 <https://jira.onap.org/browse/SO-3167>`_ - Design ETSI SOL007 compliant Network Service Descriptor packages
*  `SO-3208 <https://jira.onap.org/browse/SO-3208>`_ - SOL003 Adapter maintenance Enhancements
*  `SO-3036 <https://jira.onap.org/browse/SO-3036>`_ - SO impacts for E2E Network Slicing use case in Guilin
*  `SO-2936 <https://jira.onap.org/browse/SO-2936>`_ - PNF PnP: SO macro flow - use existing PNF instance in a Service Instance
*  `SO-2843 <https://jira.onap.org/browse/SO-2843>`_ - Support NS LCM and Workflows Management
*  `SO-2842 <https://jira.onap.org/browse/SO-2842>`_ - Support for SOL005 NBI API Handler
*  `SO-2841 <https://jira.onap.org/browse/SO-2841>`_ - Support SO NFVO Microservice Plugin Capabilities
*  `SO-2840 <https://jira.onap.org/browse/SO-2840>`_ - Support for ETSI NFV NFVO  Orchestrator in ONAP SO (ONAP SO ETSI-Aligned Hierarchical Orchestration)
*  `SO-2681 <https://jira.onap.org/browse/SO-2681>`_ - SO direct Catalog Management Support - Guilin
*  `SO-2046 <https://jira.onap.org/browse/SO-2046>`_ - support Java 11 upgrade


**Stories**

The full list of implemented tasks is available on `JIRA GUILIN STORY <https://jira.onap.org/issues/?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20story%20AND%20fixVersion%20%3D%20%22Guilin%20Release%22>`_
Listed below are key functional jira stories handled in the Guilin release:

*  `SO-2950 <https://jira.onap.org/browse/SO-2950>`_ - Asynchronous service activation response handling support in MDONS
*  `SO-3028 <https://jira.onap.org/browse/SO-3028>`_ - SO supports the OVP 2.0 test platform
*  `SO-2930 <https://jira.onap.org/browse/SO-2930>`_ - Service level workflow execution API
*  `SO-2929 <https://jira.onap.org/browse/SO-2929>`_ - Service level workflow retrieving API
*  `SO-2928 <https://jira.onap.org/browse/SO-2928>`_ - Service model retrieving API
*  `SO-2927 <https://jira.onap.org/browse/SO-2927>`_ - Generic service level upgrade workflow
*  `SO-2926 <https://jira.onap.org/browse/SO-2926>`_ - New Service Level postCheck building block
*  `SO-2925 <https://jira.onap.org/browse/SO-2925>`_ - New Service Level Upgrade building block
*  `SO-2924 <https://jira.onap.org/browse/SO-2924>`_ - New Service Level Preparation building block
*  `SO-2981 <https://jira.onap.org/browse/SO-2981>`_ - PNF Plug & Play in R7 - SO Building Block Work
*  `SO-3026 <https://jira.onap.org/browse/SO-3026>`_ - Adapter for the SO to interact with the K8S plugin
*  `SO-3025 <https://jira.onap.org/browse/SO-3025>`_ - SO should support CNFO
*  `SO-3039 <https://jira.onap.org/browse/SO-3039>`_ - Containers must crash properly when a failure occurs
*  `SO-3040 <https://jira.onap.org/browse/SO-3040>`_ - ONAP container repository (nexus) must not contain upstream docker images
*  `SO-3029 <https://jira.onap.org/browse/SO-3029>`_ - SO support Multi Tenancy
*  `SO-3077 <https://jira.onap.org/browse/SO-3077>`_ - ONAP shall use STDOUT for logs collection - REQ-374

**Tasks**

The full list of implemented tasks is available on `JIRA GUILIN TASKS <https://jira.onap.org/issues/?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20Task%20AND%20fixVersion%20%3D%20%22Guilin%20Release%22>`_
Listed below are highest and high piority jira tasks handled in the Guilin release:

*  `SO-3205 <https://jira.onap.org/browse/SO-3205>`_ - E2E Network Slicing: Improvements for NST/NSI Selection callback
*  `SO-3120 <https://jira.onap.org/browse/SO-3120>`_ - Create swagger api for software upgrade in SO
*  `SO-2915 <https://jira.onap.org/browse/SO-2915>`_ - Upgrade Vulnerable Direct Dependencies


**Bug Fixes**

The full list of fixed bugs is available on `JIRA GUILIN BUGS 
<https://jira.onap.org/issues/?jql=project%20%3D%20%22Service%20Orchestrator%22%20%20AND%20issuetype%20%3D%20Bug%20AND%20fixVersion%20%3D%20%22Guilin%20Release%22>`_
Listed below are highest and high piority jira tasks handled in the Guilin release:

*  `SO-3375 <https://jira.onap.org/browse/SO-3375>`_ - FlowManipulatorListenerRunner does not invoke for controller execution config-deploy
*  `SO-3369 <https://jira.onap.org/browse/SO-3369>`_ - Fix basic vm test case in onap
*  `SO-3364 <https://jira.onap.org/browse/SO-3364>`_ - SO sends rest request to cds twice per one operation
*  `SO-3360 <https://jira.onap.org/browse/SO-3360>`_ - SO-OpenStack-Adapter attempts to create wrong vserver-to-vnfc relation in AAI
*  `SO-3357 <https://jira.onap.org/browse/SO-3357>`_ - ControllerExecutionBB is triggered, when running a`la carte DeleteVFModule
*  `SO-3352 <https://jira.onap.org/browse/SO-3352>`_ - Exception in org.onap.so.bpmn.infrastructure.workflow.tasks.OrchestrationStatusValidator.validateOrchestrationStatus Orchestration Status Validation failed
*  `SO-3351 <https://jira.onap.org/browse/SO-3351>`_ - Staging image is present in OOM master branch
*  `SO-3346 <https://jira.onap.org/browse/SO-3346>`_ - vFW CNF AssignVfModuleBB has failed
*  `SO-3342 <https://jira.onap.org/browse/SO-3342>`_ - VnfAdapter is configured by default to v1 version whereas v2 version is more complete
*  `SO-3341 <https://jira.onap.org/browse/SO-3341>`_ - Exception of Writing NSSI to AAI for ExternalNssmfManager
*  `SO-3339 <https://jira.onap.org/browse/SO-3339>`_ - Transport Slicing integration: network-policy is missing under allotted-resource
*  `SO-3326 <https://jira.onap.org/browse/SO-3326>`_ - Transport Slicing integration: AAI Exception in DeAllocate TN NSSI WF
*  `SO-3322 <https://jira.onap.org/browse/SO-3322>`_ - PNF service instantiation using building blocks fails during ActivateServiceInstanceBB building block execution
*  `SO-3321 <https://jira.onap.org/browse/SO-3321>`_ - Transport Slicing integration: SO sets wrong subscription-service-type in SDNC payload
*  `SO-3313 <https://jira.onap.org/browse/SO-3313>`_ - SO getting disto error while SDC distribution
*  `SO-3310 <https://jira.onap.org/browse/SO-3310>`_ - Transport Slicing Integration: null pointer exception in saving SDNC rollback data
*  `SO-3309 <https://jira.onap.org/browse/SO-3309>`_ - Transport Slicing integration: unable to get prefix environment variable from execution in TnNssmfUntils
*  `SO-3308 <https://jira.onap.org/browse/SO-3308>`_ - Transport Slicing integration: MSOWorkflowException: mso-request-id not provided
*  `SO-3304 <https://jira.onap.org/browse/SO-3304>`_ - Exception in org.onap.so.bpmn.infrastructure.aai.tasks.AAICreateTasks.createServiceInstance ModelMapper configuration errors
*  `SO-3296 <https://jira.onap.org/browse/SO-3296>`_ - SO has python 2.7 pods
*  `SO-3294 <https://jira.onap.org/browse/SO-3294>`_ - Parameters exception of Deallocating NSSI
*  `SO-3293 <https://jira.onap.org/browse/SO-3293>`_ - Allocate TN NSSI fails to create relationship between allotted-resource and logical-links
*  `SO-3290 <https://jira.onap.org/browse/SO-3290>`_ - SO-VNFM certificates expired
*  `SO-3284 <https://jira.onap.org/browse/SO-3284>`_ - Exceptions in Allocate TN NSSI work flow
*  `SO-3275 <https://jira.onap.org/browse/SO-3275>`_ - Fix 3gppservices URI path in API-Handler
*  `SO-3274 <https://jira.onap.org/browse/SO-3274>`_ - Parameters exception of Allocating NSSI
*  `SO-3271 <https://jira.onap.org/browse/SO-3271>`_ - SO/BB PNF - skip_post_instantiation_configuration is not processed properly.
*  `SO-3270 <https://jira.onap.org/browse/SO-3270>`_ - BB workflow failing sporadically during post instantiation
*  `SO-3266 <https://jira.onap.org/browse/SO-3266>`_ - BPMN config assign bb - NullPointerException in ControllerExecution
*  `SO-3261 <https://jira.onap.org/browse/SO-3261>`_ - Encountering NullPointerException, WorkFlow failure after Java 11 upgrade Code refactorig on SO-bpmn-infra code base.
*  `SO-3243 <https://jira.onap.org/browse/SO-3243>`_ - SO-bpmn-infra Container after Java 11 upgrade encountering SunCertPathBuilder Exception: unable to find valid certification path to requested target in CSIT
*  `SO-3236 <https://jira.onap.org/browse/SO-3236>`_ - SO has java 8 pods
*  `SO-3216 <https://jira.onap.org/browse/SO-3216>`_ - Integration E2E VNF test fails due to missing EdgeRule in AAI call
*  `SO-3196 <https://jira.onap.org/browse/SO-3196>`_ - [SO] so-sdc-controller fails to connect to aai due to cert issue
*  `SO-3193 <https://jira.onap.org/browse/SO-3193>`_ - Macro Workflow fails in AssignVnfBB in step HomingBB
*  `SO-2941 <https://jira.onap.org/browse/SO-2941>`_ - Docker are not built anymore
*  `SO-2939 <https://jira.onap.org/browse/SO-2939>`_ - Master branch uses SNAPSHOT version that are not available anymore
*  `SO-2809 <https://jira.onap.org/browse/SO-2809>`_ - SO build is failing due to unable to download org.onap.appc.client:client-lib:jar:1.7.1-SNAPSHOT
*  `SO-2797 <https://jira.onap.org/browse/SO-2797>`_ - BB workflow with post instantiation is not working


Security Notes
~~~~~~~~~~~~~~

*Fixed Security Issues*

*Known Security Issues*

*Known Vulnerabilities in Used Modules*

Quick Links:

- `SO project page`_
- `Passing Badge information for SO <https://bestpractices.coreinfrastructure.org/en/projects/1702>`_

**Known Issues**
*  `SO-3403 <https://jira.onap.org/browse/SO-3403>`_ - The functionality of the SO cnf-adapter will be tested further and will be delivered by the Guilin Maintenance Release as a 1.7.11 patch.
*  `SO-3237 <https://jira.onap.org/browse/SO-SO-3237>`_ - Exposed HTTP port. 
*  `SO-3414 <https://jira.onap.org/browse/SO-SO-3414>`_ - Search Query does not contain get model data for vFW closed loop. 


**Upgrade Notes**

	N/A

**Deprecation Notes**

	SO modules Ve-Vnfm-adapter and appc-orchestrator are deprectaed for the Guilin release.

**Other**

	N/A

***************************************************************************************


Version: 1.6.4
-----------------------

:Release Date: 13th July 2020

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - onap-so-api-handler-infra
 - onap-so-bpmn-infra
 - onap-so-catalog-db-adapter
 - onap-so-openstack-adapter
 - onap-so-request-db-adapter
 - onap-so-sdc-controller
 - onap-so-sdnc-adapter
 - onap-so-so-monitoring
 - onap-so-vfc-adapter
 - onap-so-vnfm-adapter
 - onap-so-ve-vnfm-adapter
 - onap-so-nssmf-adapter
 - onap-so-appc-orchestrator

**Release Purpose**

The main goal of the Frankfurt maintenance release was to:

	- Appc Orchestraor changes were merged in SO and OOM as part of the release. This also used for the inplace software update flows.
	- MDONS had an issue in its delete flow that was addressed.
	- Vnfm-Adapter was unable to communicate with ETSI-Catalog through MSB, as the MSB cert is changed during the RC2 and this impacted the SO and ETSI Catalog DB connectivity.

**New Features**
--N/A--
**Epics**
--N/A--
**Stories**
--N/A--
**Key Issues Addressed**

-  [`SO-2903 <https://jira.onap.org/browse/SO-2903>`__\ ] - Include so-appc-orchestrator with SO OOM.
-  [`SO-2967 <https://jira.onap.org/browse/SO-2967>`__\ ] - Error in Delete MDONS service flow which causes No such property error.
-  [`SO-2982 <https://jira.onap.org/browse/SO-2982>`__\ ] - Vnfm-Adapter unable to communicate with ETSI-Catalog through MSB.
-  [`SO-3022 <https://jira.onap.org/browse/SO-3022>`__\ ] - Use BB-based VNF-InPlaceUpdate flow for inPlaceSoftwareUpdate requests. 


**Security Notes**
 
 Quick Links:

- `SO project page`_
- `Passing Badge information for SO`_


**Known Issues**


OJSI Issues

	N/A

**Upgrade Notes**

	N/A

**Deprecation Notes**

	N/A

**Other**

	N/A

***************************************************************************************

Version: 1.6.3
--------------

:Release Date: 

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - onap-so-api-handler-infra
 - onap-so-bpmn-infra
 - onap-so-catalog-db-adapter
 - onap-so-openstack-adapter
 - onap-so-request-db-adapter
 - onap-so-sdc-controller
 - onap-so-sdnc-adapter
 - onap-so-so-monitoring
 - onap-so-vfc-adapter
 - onap-so-vnfm-adapter
 - onap-so-ve-vnfm-adapter
 - onap-so-nssmf-adapter

**Release Purpose**

The main goal of the Frankfurt release was to:
	- ETSI alignment improvements - CMCC, Ericcson, Huawei, Samsung, Verizon, ZTE.
	    - SOL005 adaptation
	    - SOL003 adaptation
	    - SOL002 adaptation
	    - SOL004 Package support by ETSI Catalog Manager and SOL003 Adapter
	- PNF orchestration Enhancements - Ericcson, Huawei, Nokia
	    - PNF software upgrade  
	    - PNF PNP enhancement  
	- CCVPN Enhancement
	    - MDONS support -  Fujitsu
	    - Eline support - Bell, Huawei, CMCC
	- 5G Slicing - ATT, Amdocs, CMCC, Huawei, Wipro
	- CDS integration enhancement - ATT, Bell, Tech Mahindra
	- (SO Multi Cloud plugin improvements - Intel)
	- HPA -  Intel (Testing effort)

**New Features**

Features Being considered for F release (As per the resource availability):

+---------------------------------------------------------------------+
|SOL005 Adapter supports communication security                       |                                    
+---------------------------------------------------------------------+
|SOL005 Adapter supports NS LCM                                       |                                    
+---------------------------------------------------------------------+
|Multi-domain Optical Network Service Orchestration Support in SO     |                                    
+---------------------------------------------------------------------+
|SOL002 Adapter - supports EM-triggered VNF/VNFC Management           |                                    
+---------------------------------------------------------------------+
|SO Catalog Management Support                                        |                                    
+---------------------------------------------------------------------+
|Frankfurt release planning milestone                                 |                                    
+---------------------------------------------------------------------+
|Initiate/ Terminate slice service; Activate/deactivate Slice service |                                    
+---------------------------------------------------------------------+
|SO support of Network Slicing Demo in Frankfurt                      |                                    
+---------------------------------------------------------------------+
|ETSI Alignment Support - SOL003 Adapter Enhancement for Frankfurt    |                                    
+---------------------------------------------------------------------+
|AAI update for VNF improvements                                      |                                    
+---------------------------------------------------------------------+
|SO Multicloud plugin to Multicloud improvements                      |                                    
+---------------------------------------------------------------------+
|SO to CDS Enhancement for Generic Implementation                     |                                    
+---------------------------------------------------------------------+
|S3P improvement Requirements                                         |
+---------------------------------------------------------------------+
|Upgrade the APIs to Policy                                           |                                    
+---------------------------------------------------------------------+

**Epics**
-  [`SO-2524 <https://jira.onap.org/browse/SO-2524>`__\ ] - Functionality and API Freeze
-  [`SO-2519 <https://jira.onap.org/browse/SO-2519>`__\ ] - TSC must have for Frankfurt
-  [`SO-2432 <https://jira.onap.org/browse/SO-2432>`__\ ] - Multi-domain Optical Network Service Orchestration Support in SO
-  [`SO-2427 <https://jira.onap.org/browse/SO-2427>`__\ ] - SOL002 Adapter - supports EM-triggered VNF/VNFC Management
-  [`SO-2404 <https://jira.onap.org/browse/SO-2404>`__\ ] - SO Catalog Management Support
-  [`SO-2383 <https://jira.onap.org/browse/SO-2383>`__\ ] - Frankfurt release planning milestone
-  [`SO-2368 <https://jira.onap.org/browse/SO-2368>`__\ ] - Support 5G slice orchestration
-  [`SO-2281 <https://jira.onap.org/browse/SO-2281>`__\ ] - SO support of Network Slicing Demo in Frankfurt
-  [`SO-2156 <https://jira.onap.org/browse/SO-2156>`__\ ] - ETSI Alignment Support - SOL003 Adapter Enhancement for Frankfurt
-  [`SO-2087 <https://jira.onap.org/browse/SO-2087>`__\ ] - AAI update for VNF improvements
-  [`SO-2086 <https://jira.onap.org/browse/SO-2086>`__\ ] - SO Multicloud plugin to Multicloud improvements
-  [`SO-2046 <https://jira.onap.org/browse/SO-2046>`__\ ] - support Java 11 upgrade
-  [`SO-1579 <https://jira.onap.org/browse/SO-1579>`__\ ] - SO supports ETSI SOL005 Alignment of its interfaces with NFVO

**Stories**
-  [`SO-2774 <https://jira.onap.org/browse/SO-2774>`__\ ] - simplify fabric into add/delete steps
-  [`SO-2772 <https://jira.onap.org/browse/SO-2772>`__\ ] - Add validations to prevent out of order deletes
-  [`SO-2770 <https://jira.onap.org/browse/SO-2770>`__\ ] - Added support for volume group request to
-  [`SO-2768 <https://jira.onap.org/browse/SO-2768>`__\ ] - mso vnf configuration update composite flow
-  [`SO-2767 <https://jira.onap.org/browse/SO-2767>`__\ ] - convert openstack to external tasks
-  [`SO-2763 <https://jira.onap.org/browse/SO-2763>`__\ ] - Ingest and Process Service Function
-  [`SO-2762 <https://jira.onap.org/browse/SO-2762>`__\ ] - Update Subprocess to use COMPLETE status
-  [`SO-2761 <https://jira.onap.org/browse/SO-2761>`__\ ] - Use setVariablesLocal for setting task variables
-  [`SO-2753 <https://jira.onap.org/browse/SO-2753>`__\ ] - mso to add support for creating the cloud region
-  [`SO-2744 <https://jira.onap.org/browse/SO-2744>`__\ ] - reworked dsl client code to check for outputs
-  [`SO-2743 <https://jira.onap.org/browse/SO-2743>`__\ ] - split single and plural graph inventory uris
-  [`SO-2735 <https://jira.onap.org/browse/SO-2735>`__\ ] - update poms to be compatible with eclipse IDE
-  [`SO-2726 <https://jira.onap.org/browse/SO-2726>`__\ ] - Added check to prevent camunda history lookup on
-  [`SO-2717 <https://jira.onap.org/browse/SO-2717>`__\ ] - Added git attributes to convert line endings to
-  [`SO-2715 <https://jira.onap.org/browse/SO-2715>`__\ ] - Enhance startTime filtering for OrchestrationRequests
-  [`SO-2713 <https://jira.onap.org/browse/SO-2713>`__\ ] - create custom spring aop annotation for logging
-  [`SO-2700 <https://jira.onap.org/browse/SO-2700>`__\ ] - mso to store the heat template timeout minutes and
-  [`SO-2697 <https://jira.onap.org/browse/SO-2697>`__\ ] - Added simpleNotTaskInfo format modifier
-  [`SO-2683 <https://jira.onap.org/browse/SO-2683>`__\ ] - Enhance CSIT for ETSI package management
-  [`SO-2680 <https://jira.onap.org/browse/SO-2680>`__\ ] - enhance openstack library
-  [`SO-2675 <https://jira.onap.org/browse/SO-2675>`__\ ] - Rename migration script
-  [`SO-2674 <https://jira.onap.org/browse/SO-2674>`__\ ] - mso to add tenant name and product family name to
-  [`SO-2662 <https://jira.onap.org/browse/SO-2662>`__\ ] - Updated pom to release version of logging library
-  [`SO-2660 <https://jira.onap.org/browse/SO-2660>`__\ ] - SO API extension to retrieve all PNF workflow
-  [`SO-2657 <https://jira.onap.org/browse/SO-2657>`__\ ] - mso to add support for creating the cloud region
-  [`SO-2655 <https://jira.onap.org/browse/SO-2655>`__\ ] - added in graceful shutdown to spring boot
-  [`SO-2653 <https://jira.onap.org/browse/SO-2653>`__\ ] - Initial commit to check client alive
-  [`SO-2651 <https://jira.onap.org/browse/SO-2651>`__\ ] - Remove unused param
-  [`SO-2647 <https://jira.onap.org/browse/SO-2647>`__\ ] - Create ConfigDeployPnfBB
-  [`SO-2646 <https://jira.onap.org/browse/SO-2646>`__\ ] - Create ConfigAssignPnfBB
-  [`SO-2644 <https://jira.onap.org/browse/SO-2644>`__\ ] - WaitForPnfReadyBB - set orchestration status to Register and then Registered
-  [`SO-2642 <https://jira.onap.org/browse/SO-2642>`__\ ] - AssignPnfBB - set orchestration status to Assigned after successful assignment
-  [`SO-2641 <https://jira.onap.org/browse/SO-2641>`__\ ] - Include AssignPnfBB, WaitForPnfReadyBB, ActivatePnfBB in Service-Macro-Create flow
-  [`SO-2640 <https://jira.onap.org/browse/SO-2640>`__\ ] - AssignPnfBB - store model related PNF parameters in AAI
-  [`SO-2637 <https://jira.onap.org/browse/SO-2637>`__\ ] - modifications to create network to add lob
-  [`SO-2623 <https://jira.onap.org/browse/SO-2623>`__\ ] - Remove Valet from openstack adapter
-  [`SO-2620 <https://jira.onap.org/browse/SO-2620>`__\ ] - Include stack Status Reason when rollback is
-  [`SO-2616 <https://jira.onap.org/browse/SO-2616>`__\ ] - add manual handling to rainy day handling for bbs
-  [`SO-2615 <https://jira.onap.org/browse/SO-2615>`__\ ] - convert bbinputsetup populate methods to use
-  [`SO-2614 <https://jira.onap.org/browse/SO-2614>`__\ ] - Add Neutron Port and Nova Server to Proxy
-  [`SO-2607 <https://jira.onap.org/browse/SO-2607>`__\ ] - Create ActivatePnfBB
-  [`SO-2606 <https://jira.onap.org/browse/SO-2606>`__\ ] - Create WaitForPnfReadyBB
-  [`SO-2605 <https://jira.onap.org/browse/SO-2605>`__\ ] - AssignPnfBB should make a link in AAI between PNF and service instance
-  [`SO-2603 <https://jira.onap.org/browse/SO-2603>`__\ ] - Replaced annotation with RepositoryRestResource
-  [`SO-2601 <https://jira.onap.org/browse/SO-2601>`__\ ] - Use the timeout from the heat template instead of
-  [`SO-2597 <https://jira.onap.org/browse/SO-2597>`__\ ] - removed powermock dependecy and added it to
-  [`SO-2596 <https://jira.onap.org/browse/SO-2596>`__\ ] - 1911 create appc adapter micro service
-  [`SO-2591 <https://jira.onap.org/browse/SO-2591>`__\ ] - mso stores vnf application id from macro create
-  [`SO-2590 <https://jira.onap.org/browse/SO-2590>`__\ ] - configurable aaf user expires
-  [`SO-2584 <https://jira.onap.org/browse/SO-2584>`__\ ] - consolidated security configuration
-  [`SO-2577 <https://jira.onap.org/browse/SO-2577>`__\ ] - Support for volume groups on replace VF Module.
-  [`SO-2572 <https://jira.onap.org/browse/SO-2572>`__\ ] - Remove references to AIC
-  [`SO-2571 <https://jira.onap.org/browse/SO-2571>`__\ ] - update so to use 1.6.3 snapshot from the logging
-  [`SO-2570 <https://jira.onap.org/browse/SO-2570>`__\ ] - Add simple query format, to limit response content
-  [`SO-2568 <https://jira.onap.org/browse/SO-2568>`__\ ] - Create AssignPnfBB
-  [`SO-2566 <https://jira.onap.org/browse/SO-2566>`__\ ] - Updated simulator test files
-  [`SO-2565 <https://jira.onap.org/browse/SO-2565>`__\ ] - Include service-instance-id and
-  [`SO-2564 <https://jira.onap.org/browse/SO-2564>`__\ ] - Refactor WorkflowAction.valiadteResourceIdInAAI -
-  [`SO-2561 <https://jira.onap.org/browse/SO-2561>`__\ ] - add application id support to so
-  [`SO-2555 <https://jira.onap.org/browse/SO-2555>`__\ ] - refactor fallouthandler
-  [`SO-2548 <https://jira.onap.org/browse/SO-2548>`__\ ] - Terminate Slice Instance
-  [`SO-2547 <https://jira.onap.org/browse/SO-2547>`__\ ] - Deactivate Slice Instance
-  [`SO-2546 <https://jira.onap.org/browse/SO-2546>`__\ ] - Activate Slice Instance
-  [`SO-2545 <https://jira.onap.org/browse/SO-2545>`__\ ] - Instantiate Slice Service
-  [`SO-2540 <https://jira.onap.org/browse/SO-2540>`__\ ] - SO API extension to retrieve PNF workflow
-  [`SO-2523 <https://jira.onap.org/browse/SO-2523>`__\ ] - vnf and vf module replace requests to make
-  [`SO-2516 <https://jira.onap.org/browse/SO-2516>`__\ ] - remove unused columns infra active requests
-  [`SO-2515 <https://jira.onap.org/browse/SO-2515>`__\ ] - Create E2E workflow for software upgrade (PNF)
-  [`SO-2514 <https://jira.onap.org/browse/SO-2514>`__\ ] - Create dispatcher class for PNF Software upgrade.
-  [`SO-2511 <https://jira.onap.org/browse/SO-2511>`__\ ] - Updated to include getEntity extract
-  [`SO-2510 <https://jira.onap.org/browse/SO-2510>`__\ ] - Updated to use getEntity API for ServiceProxy
-  [`SO-2509 <https://jira.onap.org/browse/SO-2509>`__\ ] - Updated logging library version to 1.6.2-SNAPSHOT
-  [`SO-2499 <https://jira.onap.org/browse/SO-2499>`__\ ] - Skip requestId lookup when uri is
-  [`SO-2493 <https://jira.onap.org/browse/SO-2493>`__\ ] - update so to use most recent update of logging
-  [`SO-2490 <https://jira.onap.org/browse/SO-2490>`__\ ] - add new query for requestdb
-  [`SO-2488 <https://jira.onap.org/browse/SO-2488>`__\ ] - refactor repeated duplicate check code to RequestHandlerUtils
-  [`SO-2463 <https://jira.onap.org/browse/SO-2463>`__\ ] - Add so-simulator project
-  [`SO-2460 <https://jira.onap.org/browse/SO-2460>`__\ ] - MDONS: L1 Service Termination
-  [`SO-2459 <https://jira.onap.org/browse/SO-2459>`__\ ] - MDONS: L1 Service Creation
-  [`SO-2444 <https://jira.onap.org/browse/SO-2444>`__\ ] - update scheduled tasks to have mdc setup
-  [`SO-2442 <https://jira.onap.org/browse/SO-2442>`__\ ] - Add column to catalog db
-  [`SO-2439 <https://jira.onap.org/browse/SO-2439>`__\ ] - Authentication and Authorization support between SOL005 Adapter and NFVO
-  [`SO-2438 <https://jira.onap.org/browse/SO-2438>`__\ ] - Secured communication support between SOL005 Adapter and NFVO
-  [`SO-2428 <https://jira.onap.org/browse/SO-2428>`__\ ] - SOL002 Adapter subscribes and consumes VNF LCM notifications from VNFM (Frankfurt)
-  [`SO-2426 <https://jira.onap.org/browse/SO-2426>`__\ ] - feature request to so to save name on deletes
-  [`SO-2412 <https://jira.onap.org/browse/SO-2412>`__\ ] - SOL003 Adapter Package Management by leveraging ONAP-ETSI Catalog Manager
-  [`SO-2406 <https://jira.onap.org/browse/SO-2406>`__\ ] - Enhance SO SDC Controller to invoke ONAP-ETSI Catalog APIs
-  [`SO-2399 <https://jira.onap.org/browse/SO-2399>`__\ ] - Update PNF instance attributes in AAI during instantiation (PnP) workflow
-  [`SO-2398 <https://jira.onap.org/browse/SO-2398>`__\ ] - Converted tests to use LATEST
-  [`SO-2372 <https://jira.onap.org/browse/SO-2372>`__\ ] - Validate SO Multicloud plugin adapter with Macro call / gr-api
-  [`SO-2339 <https://jira.onap.org/browse/SO-2339>`__\ ] - Refactor SO/DMaaP client - move BBS functionality to a workflow Task
-  [`SO-2316 <https://jira.onap.org/browse/SO-2316>`__\ ] - SO to support CDS Actor for ScaleoutBB
-  [`SO-2312 <https://jira.onap.org/browse/SO-2312>`__\ ] - SO to CDS Enhancement for Generic Implementation
-  [`SO-2293 <https://jira.onap.org/browse/SO-2293>`__\ ] - vf-module details in SDNC-Directives to pass through GR-API with v2
-  [`SO-2208 <https://jira.onap.org/browse/SO-2208>`__\ ] - Load proper instanceParams of the object being processed to CDS properties
-  [`SO-2165 <https://jira.onap.org/browse/SO-2165>`__\ ] - Add Config deploy to service-macro-delete and CDS transition directives for vnf
-  [`SO-2091 <https://jira.onap.org/browse/SO-2091>`__\ ] - Create new SO building blocks - activateNESw
-  [`SO-2090 <https://jira.onap.org/browse/SO-2090>`__\ ] - SO-CDS PNF Building Blocks back-end impl
-  [`SO-2089 <https://jira.onap.org/browse/SO-2089>`__\ ] - Create a new SO building block - preCheck
-  [`SO-2073 <https://jira.onap.org/browse/SO-2073>`__\ ] - Create a new SO building blocks - postCheck
-  [`SO-2072 <https://jira.onap.org/browse/SO-2072>`__\ ] - Support PNF CM workflow execution
-  [`SO-2071 <https://jira.onap.org/browse/SO-2071>`__\ ] - SO API extension to support PNF Upgrade
-  [`SO-2070 <https://jira.onap.org/browse/SO-2070>`__\ ] - a generic decision points for API
-  [`SO-2063 <https://jira.onap.org/browse/SO-2063>`__\ ] - AAF integration
-  [`SO-1657 <https://jira.onap.org/browse/SO-1657>`__\ ] - Automated testing for the SO Monitoring component
-  [`SO-1635 <https://jira.onap.org/browse/SO-1635>`__\ ] - Preload using user_param (without UI changes)
-  [`SO-1420 <https://jira.onap.org/browse/SO-1420>`__\ ] - SO should be able to decompose a composite service
-  [`SO-1277 <https://jira.onap.org/browse/SO-1277>`__\ ] - Adapt PNF PnP flow to support updated AAI PNF model
-  [`SO-994 <https://jira.onap.org/browse/SO-994>`__\ ] - Sonar Issue: Replace duplicate strings with Constants in ServiceInstances
-  [`SO-929 <https://jira.onap.org/browse/SO-929>`__\ ] - Removing Sonar reported Vulnerability in AAIObjectMapper file
-  [`SO-2 <https://jira.onap.org/browse/SO-2>`__\ ] - MSO should mount vnfs in appc that appc has to manage

**Key Issues Addressed**


**Security Notes**
 
 Quick Links:

- `SO project page`_
- `Passing Badge information for SO`_


**Known Issues**

-  [`SO-2903 <https://jira.onap.org/browse/SO-2903>`__\ ] - Include so-appc-orchestrator with SO OOM


OJSI Issues

	N/A

**Upgrade Notes**

	N/A

**Deprecation Notes**

	N/A

**Other**

	N/A

Version: 5.0.1
--------------

:Release Date: 2019-10-11

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - onap-so-api-handler-infra,1.5.3
 - onap-so-bpmn-infra,1.5.3
 - onap-so-catalog-db-adapter,1.5.3
 - onap-so-openstack-adapter,1.5.3
 - onap-so-request-db-adapter,1.5.3
 - onap-so-sdc-controller,1.5.3
 - onap-so-sdnc-adapter,1.5.3
 - onap-so-so-monitoring,1.5.3
 - onap-so-vfc-adapter,1.5.3
 - onap-so-vnfm-adapter,1.5.3
 - onap-so-vnfm-simulator,1.5.3

**Release Purpose**

The R5 El Alto release of ONAP is a maintenance release, focusing on deployability, technical debt, and auto test case improvements.

**New Features**

The main goal of the El-Alto release was to improve documentation, UT improvement for various kinds of resources.

**Epics**
-  [`SO-1756 <https://jira.onap.org/browse/SO-1756>`__\ ] - Enhance SO VNFM Adapter

**Stories**
-  [`SO-2376 <https://jira.onap.org/browse/SO-2376>`__\ ] - Improve fall out case handling
-  [`SO-2363 <https://jira.onap.org/browse/SO-2363>`__\ ] - Update Resume Logic and Add Workflow Listeners
-  [`SO-2353 <https://jira.onap.org/browse/SO-2353>`__\ ] - update logging to match onap logging library
-  [`SO-2352 <https://jira.onap.org/browse/SO-2352>`__\ ] - Improvements to relationship handling in VNFM adapter
-  [`SO-2332 <https://jira.onap.org/browse/SO-2332>`__\ ] - Remove unused table requestdb.active_requests.
-  [`SO-2306 <https://jira.onap.org/browse/SO-2306>`__\ ] - getentity csar logging
-  [`SO-2301 <https://jira.onap.org/browse/SO-2301>`__\ ] - Integrate Logging Library
-  [`SO-2297 <https://jira.onap.org/browse/SO-2297>`__\ ] - updated all sql files including in tests to use
-  [`SO-2291 <https://jira.onap.org/browse/SO-2291>`__\ ] - Created external task utils in a common location
-  [`SO-2283 <https://jira.onap.org/browse/SO-2283>`__\ ] - Convert NetworkCollection to use GetEntity API.
-  [`SO-2282 <https://jira.onap.org/browse/SO-2282>`__\ ] - Convert to use the GetEntity API
-  [`SO-2259 <https://jira.onap.org/browse/SO-2259>`__\ ] - Added default value for when ErrorCode is null in mdc
-  [`SO-2244 <https://jira.onap.org/browse/SO-2244>`__\ ] - Updated VNF and VfModules to use the getEntity API.
-  [`SO-2233 <https://jira.onap.org/browse/SO-2233>`__\ ] - fixed dsl builder to correctly add output
-  [`SO-2232 <https://jira.onap.org/browse/SO-2232>`__\ ] - Initial commit of validation framework to APIH
-  [`SO-2231 <https://jira.onap.org/browse/SO-2231>`__\ ] - asdc controller treat distributionid as requestid in mdc
-  [`SO-2224 <https://jira.onap.org/browse/SO-2224>`__\ ] - Updated vnfc instance groups to use the getEntity API.
-  [`SO-2216 <https://jira.onap.org/browse/SO-2216>`__\ ] - health check now entirely config based
-  [`SO-2205 <https://jira.onap.org/browse/SO-2205>`__\ ] - add rainy day handling with SERVICE_ROLE and type
-  [`SO-2202 <https://jira.onap.org/browse/SO-2202>`__\ ] - Updated cvnfc's to use the getEntity API
-  [`SO-2190 <https://jira.onap.org/browse/SO-2190>`__\ ] - VNFM adapter support two way TLS
-  [`SO-2180 <https://jira.onap.org/browse/SO-2180>`__\ ] - Support oauth for calls from VNFM to VNFM adapter
-  [`SO-2169 <https://jira.onap.org/browse/SO-2169>`__\ ] - Add oauth for calls from VNFM adapter to VNFM
-  [`SO-2157 <https://jira.onap.org/browse/SO-2157>`__\ ] - Upgrade springboot.version from 2.0.5 to 2.1.5
-  [`SO-2147 <https://jira.onap.org/browse/SO-2147>`__\ ] - Converted NetworkResource to use the parser getEntity method
-  [`SO-2143 <https://jira.onap.org/browse/SO-2143>`__\ ] - Implement TLS for calls into VNFM adapter
-  [`SO-2142 <https://jira.onap.org/browse/SO-2142>`__\ ] - mso to enhance get orchestration request to include workflow step
-  [`SO-2122 <https://jira.onap.org/browse/SO-2122>`__\ ] - Added servicename to MDC so that it gets logged and added enter and exit markers
-  [`SO-2121 <https://jira.onap.org/browse/SO-2121>`__\ ] - Removing the application-local.yaml files from the projects to fix CSO pen test issues
-  [`SO-2116 <https://jira.onap.org/browse/SO-2116>`__\ ] - Implement TLS for calls from VNFM adapter to VNFM
-  [`SO-2114 <https://jira.onap.org/browse/SO-2114>`__\ ] - We need to expand column request_status on table archived_infra_requests as well
-  [`SO-2111 <https://jira.onap.org/browse/SO-2111>`__\ ] - add query stack data and populate table step
-  [`SO-2097 <https://jira.onap.org/browse/SO-2097>`__\ ] - Global JJB Migration of SO
-  [`SO-2093 <https://jira.onap.org/browse/SO-2093>`__\ ] - mso will support new requeststate values
-  [`SO-2092 <https://jira.onap.org/browse/SO-2092>`__\ ] - update bpmn to save extsystemerrorsource
-  [`SO-2080 <https://jira.onap.org/browse/SO-2080>`__\ ] - support new query param format
-  [`SO-2068 <https://jira.onap.org/browse/SO-2068>`__\ ] - improved logging when no exception data is found
-  [`SO-2066 <https://jira.onap.org/browse/SO-2066>`__\ ] - SO API Security Matrix
-  [`SO-2064 <https://jira.onap.org/browse/SO-2064>`__\ ] - Alpine porting check
-  [`SO-2057 <https://jira.onap.org/browse/SO-2057>`__\ ] - Update failsafe dependency to 2.0.1
-  [`SO-2055 <https://jira.onap.org/browse/SO-2055>`__\ ] - enhance workflowaction to handle resume func
-  [`SO-2054 <https://jira.onap.org/browse/SO-2054>`__\ ] - add rollback ext system error source
-  [`SO-2052 <https://jira.onap.org/browse/SO-2052>`__\ ] - Javadoc and logging improvement
-  [`SO-2048 <https://jira.onap.org/browse/SO-2048>`__\ ] - Building individual repos for reducing compilation time
-  [`SO-2043 <https://jira.onap.org/browse/SO-2043>`__\ ] - Security updates for maven dependencies
-  [`SO-2035 <https://jira.onap.org/browse/SO-2035>`__\ ] - update apih to accept new uri parameter
-  [`SO-2032 <https://jira.onap.org/browse/SO-2032>`__\ ] - support no payload for alacarte deletes
-  [`SO-2024 <https://jira.onap.org/browse/SO-2024>`__\ ] - Validate ServiceInstance name using createNodesUri.
-  [`SO-2023 <https://jira.onap.org/browse/SO-2023>`__\ ] - add is_data_internal column to request processing data
-  [`SO-2022 <https://jira.onap.org/browse/SO-2022>`__\ ] - Validate name for InstanceGroup, Configuration and Network.
-  [`SO-2021 <https://jira.onap.org/browse/SO-2021>`__\ ] - update multi stage code to accurately skip bbs if true
-  [`SO-2020 <https://jira.onap.org/browse/SO-2020>`__\ ] - mso to validate the name uniqueness during object creation in a ai
-  [`SO-2018 <https://jira.onap.org/browse/SO-2018>`__\ ] - Changes related to eviction of connections from connection pool
-  [`SO-2017 <https://jira.onap.org/browse/SO-2017>`__\ ] - use count format and limit one for exists
-  [`SO-2015 <https://jira.onap.org/browse/SO-2015>`__\ ] - support async operation for vf module operations with sdnc
-  [`SO-2001 <https://jira.onap.org/browse/SO-2001>`__\ ] - Added ext_system_error_source column to requestdb
-  [`SO-1999 <https://jira.onap.org/browse/SO-1999>`__\ ] - replaced String.repeat with static final strings
-  [`SO-1990 <https://jira.onap.org/browse/SO-1990>`__\ ] - resume request copying request body rewrite requestorid
-  [`SO-1976 <https://jira.onap.org/browse/SO-1976>`__\ ] - Enhance naming service support
-  [`SO-1975 <https://jira.onap.org/browse/SO-1975>`__\ ] - Accommodate WAN Networking
-  [`SO-1963 <https://jira.onap.org/browse/SO-1963>`__\ ] - apih resume request handling more generic
-  [`SO-1960 <https://jira.onap.org/browse/SO-1960>`__\ ] - apih to populate original request id
-  [`SO-1914 <https://jira.onap.org/browse/SO-1914>`__\ ] - Renamed NF fields in catalog db pojo
-  [`SO-1902 <https://jira.onap.org/browse/SO-1902>`__\ ] - Added script for adding original_request_id column
-  [`SO-1898 <https://jira.onap.org/browse/SO-1898>`__\ ] - Audit service enhancements
-  [`SO-1897 <https://jira.onap.org/browse/SO-1897>`__\ ] - fix keypair conflict issue in openstack adapter
-  [`SO-1893 <https://jira.onap.org/browse/SO-1893>`__\ ] - Initial checkin of updates for vf module replace
-  [`SO-1867 <https://jira.onap.org/browse/SO-1867>`__\ ] - store openstack request status in requestdb
-  [`SO-1866 <https://jira.onap.org/browse/SO-1866>`__\ ] - Update Rainy day handling to be more robust
-  [`SO-1847 <https://jira.onap.org/browse/SO-1847>`__\ ] - Added inProgress request check to resume
-  [`SO-1831 <https://jira.onap.org/browse/SO-1831>`__\ ] - Resume APIH Functionality
-  [`SO-1807 <https://jira.onap.org/browse/SO-1807>`__\ ] - Store Cloud Request in Database, add to request service
-  [`SO-1697 <https://jira.onap.org/browse/SO-1697>`__\ ] - Support State transition for configuration building blocks
-  [`SO-1538 <https://jira.onap.org/browse/SO-1538>`__\ ] - Integration Test for SO VNFM Adapter - Perform the functional test to validate VNFM Adapter NBI and SOL003-based SBI
-  [`SO-1447 <https://jira.onap.org/browse/SO-1447>`__\ ] - Refine multicloud use of SO cloudsites and identify DB
-  [`SO-1446 <https://jira.onap.org/browse/SO-1446>`__\ ] - Multicloud API updates for generic clouds

**Key Issues Addressed**
-  [`SO-2400 <https://jira.onap.org/browse/SO-2400>`__\ ] - vCPE Create Res Cust Service Error : Execption in create execution list
-  [`SO-2382 <https://jira.onap.org/browse/SO-2382>`__\ ] - SO ConfigAssign Java Exception
-  [`SO-2378 <https://jira.onap.org/browse/SO-2378>`__\ ] - Java lang exception in Homing
-  [`SO-2375 <https://jira.onap.org/browse/SO-2375>`__\ ] - vCPE instantiate gmux fails due to API Handler error
-  [`SO-2357 <https://jira.onap.org/browse/SO-2357>`__\ ] - Distribution of K8S service fails
-  [`SO-2354 <https://jira.onap.org/browse/SO-2354>`__\ ] - vCPE model_customization_id not found on create vfmodule
-  [`SO-2351 <https://jira.onap.org/browse/SO-2351>`__\ ] - SO Distribution Error on Allotted Resource - duplicate primary
-  [`SO-2349 <https://jira.onap.org/browse/SO-2349>`__\ ] - Exception in DMAAP Client when PNF_READY event arrives from PRH
-  [`SO-2337 <https://jira.onap.org/browse/SO-2337>`__\ ] - git clone --depth 1 not working for CSIT filename too long
-  [`SO-2289 <https://jira.onap.org/browse/SO-2289>`__\ ] - CreateVcpeResCustService_simplified workflow used in PnP PNF registration workflow returns an exception in Dmaap listener
-  [`SO-2229 <https://jira.onap.org/browse/SO-2229>`__\ ] - sdc adapter and openstack container in crash loopback
-  [`SO-2228 <https://jira.onap.org/browse/SO-2228>`__\ ] - SDC Handler crash loopback
-  [`SO-2222 <https://jira.onap.org/browse/SO-2222>`__\ ] - SO 1.5.0-STAGING-latest containers fail liveness probe
-  [`SO-2221 <https://jira.onap.org/browse/SO-2221>`__\ ] - SO 1.5.0-STAGING-latest container fails to start
-  [`SO-2082 <https://jira.onap.org/browse/SO-2082>`__\ ] - Delete Network does not work correctly
-  [`SO-2038 <https://jira.onap.org/browse/SO-2038>`__\ ] - Fix build and harkari-cp version, Get LF to add dependency
-  [`SO-2003 <https://jira.onap.org/browse/SO-2003>`__\ ] - No workflow assigned to 'Dissociate' button in VID
-  [`SO-1934 <https://jira.onap.org/browse/SO-1934>`__\ ] - ETSI Building Block Fails to Execute - Due to variables not being mapped correctly in the workflow
-  [`SO-1892 <https://jira.onap.org/browse/SO-1892>`__\ ] - CatalogDbClent -  sql query error
-  [`SO-1809 <https://jira.onap.org/browse/SO-1809>`__\ ] - 'DoDeleteE2EServiceInstance' calls 'AAI GenericGetService' sub-process which is deleted from SO common-bpmn
-  [`SO-1644 <https://jira.onap.org/browse/SO-1644>`__\ ] - SO doesn't keep the proxy settings within the containers
-  [`SO-1605 <https://jira.onap.org/browse/SO-1605>`__\ ] - SO fails on updating Camunda table when DoCreateVfModule for vCPE infra service

**Security Notes**
 
 Quick Links:

- `SO project page`_
- `Passing Badge information for SO`_


**Known Issues**

-  [`SO-2063 <https://jira.onap.org/browse/SO-2063>`__\ ] - AAF integration
-  [`SO-2403 <https://jira.onap.org/browse/SO-2403>`__\ ] - Not Displaying correct Workflow Name
-  [`SO-2430 <https://jira.onap.org/browse/SO-2430>`__\ ] - vCPE Create VFmodule Fails on Query to SDNC
-  [`SO-2433 <https://jira.onap.org/browse/SO-2433>`__\ ] - Not providing user options during Pause For Manual Task
-  [`SO-2434 <https://jira.onap.org/browse/SO-2434>`__\ ] - Displaying Un-needed Mandatory User Inputs for Workflow with Pause
-  [`SO-1754 <https://jira.onap.org/browse/SO-1754>`__\ ] - SO-Mariadb: 'VNF_RESOURCE_CUSTOMIZATION' DB update bug when service is distributed.
-  [`SO-2447 <https://jira.onap.org/browse/SO-2447>`__\ ] - Openstack Adatper fails to find Stack Name and creates duplicate stack with address conflict

OJSI Issues

-  [`OJSI-110 <https://jira.onap.org/browse/OJSI-110>`__\ ] - so-monitor exposes plain text HTTP endpoint using port 30224
-  [`OJSI-138 <https://jira.onap.org/browse/OJSI-138>`__\ ] - so exposes plain text HTTP endpoint using port 30277
-  [`OJSI-169 <https://jira.onap.org/browse/OJSI-169>`__\ ] - Port 30224 exposes unprotected service outside of cluster
-  [`OJSI-203 <https://jira.onap.org/browse/OJSI-203>`__\ ] - SO exposes unprotected APIs/UIs (CVE-2019-12128


**Upgrade Notes**

	N/A

**Deprecation Notes**

	N/A

**Other**

	N/A


Version: 1.4.4
-----------------------

:Release Date: 2019-06-13

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - onap-so-api-handler-infra,1.4.4
 - onap-so-bpmn-infra,1.4.4
 - onap-so-catalog-db-adapter,1.4.4
 - onap-so-openstack-adapter,1.4.4
 - onap-so-request-db-adapter,1.4.4
 - onap-so-sdc-controller,1.4.4
 - onap-so-sdnc-adapter,1.4.4
 - onap-so-so-monitoring,1.4.4
 - onap-so-vfc-adapter,1.4.4
 - onap-so-vnfm-adapter,1.4.4

**Release Purpose**


**New Features**

The main goal of the Dublin release was to:
    - Support CCVPN extension
    - Support BroadBand Service Usecase
    - SO SOL003 plugin support
    - Improve PNF PnP
    - Improve SO internal modularity

**Epics**

-  [`SO-1508 <https://jira.onap.org/browse/SO-1508>`__\ ] - ETSI Alignment - SO SOL003 plugin support to connect to external VNFMs
-  [`SO-1468 <https://jira.onap.org/browse/SO-1468>`__\ ] - Hardening of HPA in SO and extension of HPA capabilities to existing use-cases
-  [`SO-1394 <https://jira.onap.org/browse/SO-1394>`__\ ] - Extended and enhance the SO generic building block to support pre and post instantiation. 
-  [`SO-1393 <https://jira.onap.org/browse/SO-1393>`__\ ] - Support the CCVPN Extension
-  [`SO-1392 <https://jira.onap.org/browse/SO-1392>`__\ ] - Support the BroadBand Service Usecase
-  [`SO-1353 <https://jira.onap.org/browse/SO-1353>`__\ ] - SO to be made independent of Cloud technologies
-  [`SO-1273 <https://jira.onap.org/browse/SO-1273>`__\ ] - PNF PnP Dublin updates & improvements
-  [`SO-1271 <https://jira.onap.org/browse/SO-1271>`__\ ] - PNF PnP Casablanca MR updates
-  [`SO-677  <https://jira.onap.org/browse/SO-677>`__\ ] - Improve the issues and findings of the SO Casablanca Release

**Stories**

-  [`SO-1974 <https://jira.onap.org/browse/SO-1974>`__\ ] - Turn off OpenStack heat stack audit
-  [`SO-1924 <https://jira.onap.org/browse/SO-1924>`__\ ] - Add VnfConfigUpdate to the list of native CM workflows returned to VID
-  [`SO-1820 <https://jira.onap.org/browse/SO-1820>`__\ ] - Add Model Version Query
-  [`SO-1806 <https://jira.onap.org/browse/SO-1806>`__\ ] - Fix issue where null variable causes task to not
-  [`SO-1793 <https://jira.onap.org/browse/SO-1793>`__\ ] - add status for delete
-  [`SO-1792 <https://jira.onap.org/browse/SO-1792>`__\ ] - add status message requirement for create vf module event audit
-  [`SO-1791 <https://jira.onap.org/browse/SO-1791>`__\ ] - Moved base client to new location
-  [`SO-1790 <https://jira.onap.org/browse/SO-1790>`__\ ] - Enhanced sniro BB to account for sole service proxies to support 1908.
-  [`SO-1765 <https://jira.onap.org/browse/SO-1765>`__\ ] - Convert Tabs to Spaces
-  [`SO-1760 <https://jira.onap.org/browse/SO-1760>`__\ ] - Add Query param to pull back nested stack information
-  [`SO-1758 <https://jira.onap.org/browse/SO-1758>`__\ ] - Fix POM to allow HTTP long polling to work on camunda
-  [`SO-1749 <https://jira.onap.org/browse/SO-1749>`__\ ] - re add openstack audit of delete functions after refactor
-  [`SO-1748 <https://jira.onap.org/browse/SO-1748>`__\ ] - Add support to parse cdl inside LOB and platform
-  [`SO-1737 <https://jira.onap.org/browse/SO-1737>`__\ ] - if audit fails write sub interface data to a ai
-  [`SO-1729 <https://jira.onap.org/browse/SO-1729>`__\ ] - Monitor Job Status-Delete
-  [`SO-1687 <https://jira.onap.org/browse/SO-1687>`__\ ] - removed unused test classes and methods
-  [`SO-1678 <https://jira.onap.org/browse/SO-1678>`__\ ] - removed extra argument from extractByKey method
-  [`SO-1676 <https://jira.onap.org/browse/SO-1676>`__\ ] - replace all fixed wiremock ports
-  [`SO-1671 <https://jira.onap.org/browse/SO-1671>`__\ ] - skip_post_instantiation_configuration schema and tosca ingestion
-  [`SO-1657 <https://jira.onap.org/browse/SO-1657>`__\ ] - Automated testing for the SO Monitoring component
-  [`SO-1648 <https://jira.onap.org/browse/SO-1648>`__\ ] - Increasing the test coverage of SO-Monitoring UI
-  [`SO-1634 <https://jira.onap.org/browse/SO-1634>`__\ ] - Notification Handling - Terminate
-  [`SO-1633 <https://jira.onap.org/browse/SO-1633>`__\ ] - Terminate VNF (with SVNFM interaction)
-  [`SO-1632 <https://jira.onap.org/browse/SO-1632>`__\ ] - Handle VNF delete and termination (without SVNFM integration)
-  [`SO-1630 <https://jira.onap.org/browse/SO-1630>`__\ ] - Monitor Job Status-Create
-  [`SO-1629 <https://jira.onap.org/browse/SO-1629>`__\ ] - Notification Handling - Instantiate
-  [`SO-1628 <https://jira.onap.org/browse/SO-1628>`__\ ] - Handle Notification Subscription
-  [`SO-1627 <https://jira.onap.org/browse/SO-1627>`__\ ] - Create relationship between esr-vnfm and generic-vnf in AAI
-  [`SO-1626 <https://jira.onap.org/browse/SO-1626>`__\ ] - Monitor Node Status
-  [`SO-1625 <https://jira.onap.org/browse/SO-1625>`__\ ] - Handle Grant Request (Without Homing/OOF)
-  [`SO-1624 <https://jira.onap.org/browse/SO-1624>`__\ ] - Instantiate VNF (with SVNFM Interaction)
-  [`SO-1623 <https://jira.onap.org/browse/SO-1623>`__\ ] - Handle Create VNF request in VNFM adapter
-  [`SO-1622 <https://jira.onap.org/browse/SO-1622>`__\ ] - Check for existing VNF (with SVNFM Interaction)
-  [`SO-1621 <https://jira.onap.org/browse/SO-1621>`__\ ] - Create placeholder implementation for create VNF (without SVNFM interaction)
-  [`SO-1620 <https://jira.onap.org/browse/SO-1620>`__\ ] - Create Shell Adapter
-  [`SO-1619 <https://jira.onap.org/browse/SO-1619>`__\ ] - Create SO VNFM Adapter Northbound Interface using Swagger
-  [`SO-1618 <https://jira.onap.org/browse/SO-1618>`__\ ] - SVNFM Simulator
-  [`SO-1616 <https://jira.onap.org/browse/SO-1616>`__\ ] - Add instance group support to SO
-  [`SO-1604 <https://jira.onap.org/browse/SO-1604>`__\ ] - SO Catalog Enhancement to support CDS Meta Data for VNF/PNF and PNF Tosca Ingestion
-  [`SO-1598 <https://jira.onap.org/browse/SO-1598>`__\ ] - add equals and hashcode support to dslquerybuilder
-  [`SO-1597 <https://jira.onap.org/browse/SO-1597>`__\ ] - improvements to audit inventory feature
-  [`SO-1596 <https://jira.onap.org/browse/SO-1596>`__\ ] - query clients now have more useable result methods
-  [`SO-1590 <https://jira.onap.org/browse/SO-1590>`__\ ] - skip cloud region validation for 1906
-  [`SO-1589 <https://jira.onap.org/browse/SO-1589>`__\ ] - flow validators can now be skipped via an annotation
-  [`SO-1582 <https://jira.onap.org/browse/SO-1582>`__\ ] - vnf spin up gr api vnf s base module fails
-  [`SO-1573 <https://jira.onap.org/browse/SO-1573>`__\ ] - Abstract for CDS Implementation
-  [`SO-1569 <https://jira.onap.org/browse/SO-1569>`__\ ] - do not attempt to commit empty transactions
-  [`SO-1538 <https://jira.onap.org/browse/SO-1538>`__\ ] - Integration Test for SO VNFM Adapter - Perform the functional test to validate VNFM Adapter NBI and SOL003-based SBI
-  [`SO-1534 <https://jira.onap.org/browse/SO-1534>`__\ ] - Create Pre Building Block validator to check if cloud-region orchestration-disabled is true
-  [`SO-1533 <https://jira.onap.org/browse/SO-1533>`__\ ] - flowvaldiator will allow more flexible filtering
-  [`SO-1512 <https://jira.onap.org/browse/SO-1512>`__\ ] - Added Camunda migration scripts and updated camunda springboot version
-  [`SO-1506 <https://jira.onap.org/browse/SO-1506>`__\ ] - E2E Automation - Extend PNF workflow with post-instantiation configuration
-  [`SO-1501 <https://jira.onap.org/browse/SO-1501>`__\ ] - add new functionality to aai client
-  [`SO-1495 <https://jira.onap.org/browse/SO-1495>`__\ ] - made max retries configurable via mso config repo
-  [`SO-1493 <https://jira.onap.org/browse/SO-1493>`__\ ] - restructure a&ai client
-  [`SO-1487 <https://jira.onap.org/browse/SO-1487>`__\ ] - added license headers to various java files
-  [`SO-1485 <https://jira.onap.org/browse/SO-1485>`__\ ] - add DSL endpoint support to A&AI Client
-  [`SO-1483 <https://jira.onap.org/browse/SO-1483>`__\ ] - SO to support a new GRPC client for container to container communication
-  [`SO-1482 <https://jira.onap.org/browse/SO-1482>`__\ ] - SO Generic Building Block to support config deploy action for CONFIGURE Step
-  [`SO-1481 <https://jira.onap.org/browse/SO-1481>`__\ ] - Generic Bulding block for assign shall trigger controller for config assign action
-  [`SO-1477 <https://jira.onap.org/browse/SO-1477>`__\ ] - AAF support for SO
-  [`SO-1476 <https://jira.onap.org/browse/SO-1476>`__\ ] - Do not process vf module being created when building an index
-  [`SO-1475 <https://jira.onap.org/browse/SO-1475>`__\ ] - store raw distribution notification in db
-  [`SO-1474 <https://jira.onap.org/browse/SO-1474>`__\ ] - Test Issue
-  [`SO-1469 <https://jira.onap.org/browse/SO-1469>`__\ ] - Refactor OOF Homing to Java
-  [`SO-1462 <https://jira.onap.org/browse/SO-1462>`__\ ] - Clean up AT&T Acronyms from Unit tests for audit
-  [`SO-1459 <https://jira.onap.org/browse/SO-1459>`__\ ] - add maven build properties to spring actuator
-  [`SO-1456 <https://jira.onap.org/browse/SO-1456>`__\ ] - prototype fetching resources from openstack and compare to a ai
-  [`SO-1452 <https://jira.onap.org/browse/SO-1452>`__\ ] - added list of flows to execution for cockpit
-  [`SO-1451 <https://jira.onap.org/browse/SO-1451>`__\ ] - Updated the SDC API call with the ECOMP OE from AAI
-  [`SO-1450 <https://jira.onap.org/browse/SO-1450>`__\ ] - support for secure communications between SO and Multicloud
-  [`SO-1447 <https://jira.onap.org/browse/SO-1447>`__\ ] - Refine multicloud use of SO cloudsites and identify DB
-  [`SO-1446 <https://jira.onap.org/browse/SO-1446>`__\ ] - Multicloud API updates for generic clouds
-  [`SO-1445 <https://jira.onap.org/browse/SO-1445>`__\ ] - Multicloud support for volume groups and networks
-  [`SO-1444 <https://jira.onap.org/browse/SO-1444>`__\ ] - AAI update after vfmodule creation
-  [`SO-1443 <https://jira.onap.org/browse/SO-1443>`__\ ] - Prepare user_directives for multicloud API
-  [`SO-1442 <https://jira.onap.org/browse/SO-1442>`__\ ] - Prepare sdnc_directives for multicloud API
-  [`SO-1441 <https://jira.onap.org/browse/SO-1441>`__\ ] - Handle distribution of service with generic cloud artifacts
-  [`SO-1436 <https://jira.onap.org/browse/SO-1436>`__\ ] - removed unnecessary repository from pom.xml
-  [`SO-1432 <https://jira.onap.org/browse/SO-1432>`__\ ] - duplicate add custom object support to a ai client
-  [`SO-1431 <https://jira.onap.org/browse/SO-1431>`__\ ] - Test issue 1
-  [`SO-1429 <https://jira.onap.org/browse/SO-1429>`__\ ] - add custom object support to a ai client
-  [`SO-1427 <https://jira.onap.org/browse/SO-1427>`__\ ] - Fix to include alloc pool from dhcpStart/end on reqs
-  [`SO-1426 <https://jira.onap.org/browse/SO-1426>`__\ ] - Upgraded tosca parser to version 1.4.8 and updated imports
-  [`SO-1425 <https://jira.onap.org/browse/SO-1425>`__\ ] - Re-Factor DMAAP Credentials to use encrypted auth
-  [`SO-1421 <https://jira.onap.org/browse/SO-1421>`__\ ] - Support for SO->ExtAPI interface/API
-  [`SO-1414 <https://jira.onap.org/browse/SO-1414>`__\ ] - update all inprogress checks in apih handler
-  [`SO-1413 <https://jira.onap.org/browse/SO-1413>`__\ ] - replaced org.mockito.Matchers with ArgumentMatchers
-  [`SO-1411 <https://jira.onap.org/browse/SO-1411>`__\ ] - Test Issue
-  [`SO-1409 <https://jira.onap.org/browse/SO-1409>`__\ ] - added in validation for number of keys provided
-  [`SO-1405 <https://jira.onap.org/browse/SO-1405>`__\ ] - apih infra shall ensure data for si matches on macro requests
-  [`SO-1404 <https://jira.onap.org/browse/SO-1404>`__\ ] - covert sync calls for create and delete network to async
-  [`SO-1395 <https://jira.onap.org/browse/SO-1395>`__\ ] - E2E Automation - PreInstatition and PostInstatition use cases
-  [`SO-1389 <https://jira.onap.org/browse/SO-1389>`__\ ] - added mso-request-id when calling SDNCHandler subflow
-  [`SO-1388 <https://jira.onap.org/browse/SO-1388>`__\ ] - descriptive messages now returned by validator
-  [`SO-1387 <https://jira.onap.org/browse/SO-1387>`__\ ] - naming ms client fixes
-  [`SO-1385 <https://jira.onap.org/browse/SO-1385>`__\ ] - removed retired A&AI versions from codebase
-  [`SO-1384 <https://jira.onap.org/browse/SO-1384>`__\ ] - sdnc handler was not sending workflow exception upwards
-  [`SO-1383 <https://jira.onap.org/browse/SO-1383>`__\ ] - refactored validator to be more generic
-  [`SO-1381 <https://jira.onap.org/browse/SO-1381>`__\ ] - Quality of Life logging improvements
-  [`SO-1380 <https://jira.onap.org/browse/SO-1380>`__\ ] - Service Proxy Consolidation
-  [`SO-1379 <https://jira.onap.org/browse/SO-1379>`__\ ] - Add validation for vnfs before WorkflowAction starts
-  [`SO-1378 <https://jira.onap.org/browse/SO-1378>`__\ ] - get subnet sequence number from A&AI
-  [`SO-1377 <https://jira.onap.org/browse/SO-1377>`__\ ] - Re-enable Actuator for Springboot 2.0
-  [`SO-1376 <https://jira.onap.org/browse/SO-1376>`__\ ] - Created sniro request pojos for homingV2 flow
-  [`SO-1370 <https://jira.onap.org/browse/SO-1370>`__\ ] - Preparation for next scale-out after successful instantiation of the current scale-out operation
-  [`SO-1369 <https://jira.onap.org/browse/SO-1369>`__\ ] - Processing of configuration parameters during instantiation and scale-out
-  [`SO-1368 <https://jira.onap.org/browse/SO-1368>`__\ ] - VNF Health check during scale-out to be made as a separate workflow
-  [`SO-1367 <https://jira.onap.org/browse/SO-1367>`__\ ] - Invoke the APP-C service configuration API after E2E Service instantiation
-  [`SO-1366 <https://jira.onap.org/browse/SO-1366>`__\ ] - SO Workflow need to call configure API during instantiation
-  [`SO-1362 <https://jira.onap.org/browse/SO-1362>`__\ ] - Changed the MDC sourcing from LoggingInterceptor to JaxRsFilterLogging.
-  [`SO-1346 <https://jira.onap.org/browse/SO-1346>`__\ ] - Use SLF4J/Logback, instead of Log4J
-  [`SO-1307 <https://jira.onap.org/browse/SO-1307>`__\ ] - Add Headers
-  [`SO-1295 <https://jira.onap.org/browse/SO-1295>`__\ ] - Update SDNC client Version in POM
-  [`SO-1293 <https://jira.onap.org/browse/SO-1293>`__\ ] - Vnf Recreate
-  [`SO-1290 <https://jira.onap.org/browse/SO-1290>`__\ ] - Update orchestrationrequest response
-  [`SO-1288 <https://jira.onap.org/browse/SO-1288>`__\ ] - Enhance GRM Clients to use encrypted auth loading
-  [`SO-1287 <https://jira.onap.org/browse/SO-1287>`__\ ] - Change all SDNC Calls in GR_API
-  [`SO-1284 <https://jira.onap.org/browse/SO-1284>`__\ ] - Create Relationship between Vnf and Tenant
-  [`SO-1283 <https://jira.onap.org/browse/SO-1283>`__\ ] - Fix GR_API cloud info retrieval
-  [`SO-1282 <https://jira.onap.org/browse/SO-1282>`__\ ] - Update Alacarte Logic for Recreate Flow
-  [`SO-1279 <https://jira.onap.org/browse/SO-1279>`__\ ] - Replaced the VNFC hardcoded Function
-  [`SO-1278 <https://jira.onap.org/browse/SO-1278>`__\ ] - Move all ecomp.mso properties to org.onap.so
-  [`SO-1276 <https://jira.onap.org/browse/SO-1276>`__\ ] - Add Cloud_Owner to northbound request table
-  [`SO-1275 <https://jira.onap.org/browse/SO-1275>`__\ ] - Resolve path issues
-  [`SO-1274 <https://jira.onap.org/browse/SO-1274>`__\ ] - CreateAndUpdatePNFResource workflow:: Associate PNF instance
-  [`SO-1272 <https://jira.onap.org/browse/SO-1272>`__\ ] - Use UUID to fill pnf-id in PNF PnP sub-flow
-  [`SO-1270 <https://jira.onap.org/browse/SO-1270>`__\ ] - Add New A&AI objects
-  [`SO-1269 <https://jira.onap.org/browse/SO-1269>`__\ ] - Add serviceRole to MSO SNIRO Interface
-  [`SO-1260 <https://jira.onap.org/browse/SO-1260>`__\ ] - Add support for naming service
-  [`SO-1233 <https://jira.onap.org/browse/SO-1233>`__\ ] - Added service role to sniro request when not null
-  [`SO-1232 <https://jira.onap.org/browse/SO-1232>`__\ ] - Switch to SpringAutoDeployment rather than processes.xml
-  [`SO-1229 <https://jira.onap.org/browse/SO-1229>`__\ ] - Remove all usage of AlarmLogger
-  [`SO-1228 <https://jira.onap.org/browse/SO-1228>`__\ ] - Limit Number of Occurs for security reasons
-  [`SO-1227 <https://jira.onap.org/browse/SO-1227>`__\ ] - Remove Swagger UI due to security scan concerns
-  [`SO-1226 <https://jira.onap.org/browse/SO-1226>`__\ ] - changed assign vnf sdnc to use the async subflow
-  [`SO-1225 <https://jira.onap.org/browse/SO-1225>`__\ ] - Add Keystone V3 Support
-  [`SO-1207 <https://jira.onap.org/browse/SO-1207>`__\ ] - accept a la carte create instance group request from vid
-  [`SO-1206 <https://jira.onap.org/browse/SO-1206>`__\ ] - Added groupInstanceId and groupInstanceName columns
-  [`SO-1205 <https://jira.onap.org/browse/SO-1205>`__\ ] - separate error status from progression status in req db
-  [`SO-806 <https://jira.onap.org/browse/SO-806>`__\ ] - SO PNF PnP workflow shall not set "in-maint" AAI flag
-  [`SO-798 <https://jira.onap.org/browse/SO-798>`__\ ] - Externalize the PNF PnP workflow? as a Service Instance Deployment workflow? adding the Controller
-  [`SO-747 <https://jira.onap.org/browse/SO-747>`__\ ] - POC - Enable SO use of Multicloud Generic VNF Instantiation API
-  [`SO-700 <https://jira.onap.org/browse/SO-700>`__\ ] - SO should be able to support CCVPN service assurance
-  [`SO-588 <https://jira.onap.org/browse/SO-588>`__\ ] - Automate robot heatbridge manual step to add VF Module stack resources in AAI
-  [`SO-18 <https://jira.onap.org/browse/SO-18>`__\ ] - Keystone v3 Support in MSO
-  [`SO-12 <https://jira.onap.org/browse/SO-12>`__\ ] - Support Ocata apis
-  [`SO-10 <https://jira.onap.org/browse/SO-10>`__\ ] - Deploy a MSO high availability environment
-  [`SO-7 <https://jira.onap.org/browse/SO-7>`__\ ] - Move modified openstack library to common functions repos
-  [`SO-6 <https://jira.onap.org/browse/SO-6>`__\ ] - Document how to change username/password for UIs


**Security Notes**
 SO code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SO open Critical security vulnerabilities and their risk assessment have been documented as part of the `project`_.

 Quick Links:

- `SO project page`_
- `Passing Badge information for SO`_
- `Project Vulnerability Review Table for SO`_


**Known Issues**

Testing Terminate and Delete of ETSI VNFM Adapter is done and has some of the minor issues pending, it will be done in El Alto.

-  [`SO-2013 <https://jira.onap.org/browse/SO-2013>`__\ ] - Test Terminate/Delete VNF with VNFM Adapter	

**Upgrade Notes**

	N/A

**Deprecation Notes**

	N/A

**Other**

	N/A

Version: 1.4.1
--------------

:Release Date: 2019-04-19

This is the dublin release base version separated from master branch.


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

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - onap-so-api-handler-infra,1.3.7
 - onap-so-bpmn-infra,1.3.7
 - onap-so-catalog-db-adapter,1.3.7
 - onap-so-openstack-adapter,1.3.7
 - onap-so-request-db-adapter,1.3.7
 - onap-so-sdc-controller,1.3.7
 - onap-so-sdnc-adapter,1.3.7
 - onap-so-so-monitoring,1.3.7
 - onap-so-vfc-adapter,1.3.7

**Known Issues**

- `SO-1419 <https://jira.onap.org/browse/SO-1419>`_ - is a stretch goal that is under examination.

- `SDC-1955 <https://jira.onap.org/browse/SDC-1955>`_ - tested with a workaround to avoid this scenario. To be tested further with updated dockers of SDC, UUI and SO.

**Security Notes**

	SO code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SO open Critical security vulnerabilities and their risk assessment have been documented as part of the `project`_.

	Quick Links:

- `SO project page`_
- `Passing Badge information for SO`_
- `Project Vulnerability Review Table for SO`_


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
- `SO-1194`_
- `SO-1248 <https://jira.onap.org/browse/SO-1248>`_
- `SO-1184 <https://jira.onap.org/browse/SO-1184>`_

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - onap-so-api-handler-infra,1.3.6
 - onap-so-bpmn-infra,1.3.6
 - onap-so-catalog-db-adapter,1.3.6
 - onap-so-openstack-adapter,1.3.6
 - onap-so-request-db-adapter,1.3.6
 - onap-so-sdc-controller,1.3.6
 - onap-so-sdnc-adapter,1.3.6
 - onap-so-so-monitoring,1.3.6
 - onap-so-vfc-adapter,1.3.6

**Known Issues**


**Security Notes**

	SO code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SO open Critical security vulnerabilities and their risk assessment have been documented as part of the `project`_.

	Quick Links:

- `SO project page`_
- `Passing Badge information for SO`_
- `Project Vulnerability Review Table for SO`_

New  release over  master branch for Dublin development

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

SO Release Image Versions
~~~~~~~~~~~~~~~~~~~~~~~~~

 - onap-so-api-handler-infra,1.3.3
 - onap-so-bpmn-infra,1.3.3
 - onap-so-catalog-db-adapter,1.3.3
 - onap-so-openstack-adapter,1.3.3
 - onap-so-request-db-adapter,1.3.3
 - onap-so-sdc-controller,1.3.3
 - onap-so-sdnc-adapter,1.3.3
 - onap-so-so-monitoring,1.3.3
 - onap-so-vfc-adapter,1.3.3

**Known Issues**

There are some issues around the HPA and CCVPN that have been resolved in the patch release of 1.3.5

- `SO-1249 <https://jira.onap.org/browse/SO-1249>`_
  The workflow for resource processing use the wrong default value.

- `SO-1257 <https://jira.onap.org/browse/SO-1257>`_
  Authorization header added to multicloud adapter breaks communication.
  
- `SO-1258 <https://jira.onap.org/browse/SO-1258>`_
  OOF Directives are not passed through flows to Multicloud Adapter.

- `SO-1256 <https://jira.onap.org/browse/SO-1256>`_
  Permission support for Vfcadapter is missing.

- `SO-1194 <https://jira.onap.org/browse/SO-1194>`_
  Unable to find TOSCA CSAR location using ServiceModelUUID in DoCreateResource BPMN flow.
  
	
Below issues will be resolved in the next release:

- `SO-1248 <https://jira.onap.org/browse/SO-1248>`_
  Csar needs to be manually placed into the bpmn corresponding directory.

- `SO-1184 <https://jira.onap.org/browse/SO-1184>`_
  Database table is not populated for Generic NeutronNet resource.


**Security Notes**

	SO code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SO open Critical security vulnerabilities and their risk assessment have been documented as part of the `project`_.

	Quick Links:

 - `SO project page`_
 - `Passing Badge information for SO`_
 - `Project Vulnerability Review Table for SO`_

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
- `Project Vulnerability Review Table for SO <https://wiki.onap.org/pages/viewpage.action?pageId=43385708>`_

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
