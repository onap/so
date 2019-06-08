.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2018 Huawei Intellectual Property.  All rights reserved.


Service Orchestrator Release Notes
==================================

The SO provides the highest level of service orchestration in the ONAP architecture. 

Version: 1.4.3
==============

:Release Date: 2019-06-06

Docker Images
-------------

**Dockers released for SO:**

 - onap/so/api-handler-infra,1.4.3
 - onap/so/bpmn-infra,1.4.3
 - onap/so/catalog-db-adapter,1.4.3
 - onap/so/openstack-adapter,1.4.3
 - onap/so/request-db-adapter,1.4.3
 - onap/so/sdc-controller,1.4.3
 - onap/so/sdnc-adapter,1.4.3
 - onap/so/so-monitoring,1.4.3
 - onap/so/vfc-adapter,1.4.3

Release Purpose
----------------

**New Features**

The main goal of the Dublin release was to:
    - Support CCVPN extension
    - Support BroadBand Service Usecase
    - SO SOL003 plugin support
    - Improve PNF PnP


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
-  [`SO-166  <https://jira.onap.org/browse/SO-166>`__\ ] - Non-stop operations required.

**Stories**

-  [`SO-1974 <https://jira.onap.org/browse/SO-1974`__ ] -	Turn off OpenStack heat stack audit
-  [`SO-1924 <https://jira.onap.org/browse/SO-1924`__ ] -	Add VnfConfigUpdate to the list of native CM workflows returned to VID
-  [`SO-1820 <https://jira.onap.org/browse/SO-1820`__ ] -	Add Model Version Query
-  [`SO-1806 <https://jira.onap.org/browse/SO-1806`__ ] -	Fix issue where null variable causes task to not
-  [`SO-1793 <https://jira.onap.org/browse/SO-1793`__ ] -	add status for delete
-  [`SO-1792 <https://jira.onap.org/browse/SO-1792`__ ] -	add status message requirement for create vf module event audit
-  [`SO-1791 <https://jira.onap.org/browse/SO-1791`__ ] -	Moved base client to new location
-  [`SO-1790 <https://jira.onap.org/browse/SO-1790`__ ] -	Enhanced sniro BB to account for sole service proxies to support 1908.
-  [`SO-1765 <https://jira.onap.org/browse/SO-1765`__ ] -	Convert Tabs to Spaces
-  [`SO-1760 <https://jira.onap.org/browse/SO-1760`__ ] -	Add Query param to pull back nested stack information
-  [`SO-1758 <https://jira.onap.org/browse/SO-1758`__ ] -	Fix POM to allow HTTP long polling to work on camunda
-  [`SO-1749 <https://jira.onap.org/browse/SO-1749`__ ] -	re add openstack audit of delete functions after refactor
-  [`SO-1748 <https://jira.onap.org/browse/SO-1748`__ ] -	Add support to parse cdl inside LOB and platform
-  [`SO-1737 <https://jira.onap.org/browse/SO-1737`__ ] -	if audit fails write sub interface data to a ai
-  [`SO-1729 <https://jira.onap.org/browse/SO-1729`__ ] -	Monitor Job Status-Delete
-  [`SO-1687 <https://jira.onap.org/browse/SO-1687`__ ] -	removed unused test classes and methods
-  [`SO-1678 <https://jira.onap.org/browse/SO-1678`__ ] -	removed extra argument from extractByKey method
-  [`SO-1676 <https://jira.onap.org/browse/SO-1676`__ ] -	replace all fixed wiremock ports
-  [`SO-1671 <https://jira.onap.org/browse/SO-1671`__ ] -	skip_post_instantiation_configuration schema and tosca ingestion
-  [`SO-1657 <https://jira.onap.org/browse/SO-1657`__ ] -	Automated testing for the SO Monitoring component
-  [`SO-1648 <https://jira.onap.org/browse/SO-1648`__ ] -	Increasing the test coverage of SO-Monitoring UI
-  [`SO-1634 <https://jira.onap.org/browse/SO-1634`__ ] -	Notification Handling - Terminate
-  [`SO-1633 <https://jira.onap.org/browse/SO-1633`__ ] -	Terminate VNF (with SVNFM interaction)
-  [`SO-1632 <https://jira.onap.org/browse/SO-1632`__ ] -	Handle VNF delete and termination (without SVNFM integration)
-  [`SO-1630 <https://jira.onap.org/browse/SO-1630`__ ] -	Monitor Job Status-Create
-  [`SO-1629 <https://jira.onap.org/browse/SO-1629`__ ] -	Notification Handling - Instantiate
-  [`SO-1628 <https://jira.onap.org/browse/SO-1628`__ ] -	Handle Notification Subscription
-  [`SO-1627 <https://jira.onap.org/browse/SO-1627`__ ] -	Create relationship between esr-vnfm and generic-vnf in AAI
-  [`SO-1626 <https://jira.onap.org/browse/SO-1626`__ ] -	Monitor Node Status
-  [`SO-1625 <https://jira.onap.org/browse/SO-1625`__ ] -	Handle Grant Request (Without Homing/OOF)
-  [`SO-1624 <https://jira.onap.org/browse/SO-1624`__ ] -	Instantiate VNF (with SVNFM Interaction)
-  [`SO-1623 <https://jira.onap.org/browse/SO-1623`__ ] -	Handle Create VNF request in VNFM adapter
-  [`SO-1622 <https://jira.onap.org/browse/SO-1622`__ ] -	Check for existing VNF (with SVNFM Interaction)
-  [`SO-1621 <https://jira.onap.org/browse/SO-1621`__ ] -	Create placeholder implementation for create VNF (without SVNFM interaction)
-  [`SO-1620 <https://jira.onap.org/browse/SO-1620`__ ] -	Create Shell Adapter
-  [`SO-1619 <https://jira.onap.org/browse/SO-1619`__ ] -	Create SO VNFM Adapter Northbound Interface using Swagger
-  [`SO-1618 <https://jira.onap.org/browse/SO-1618`__ ] -	SVNFM Simulator
-  [`SO-1616 <https://jira.onap.org/browse/SO-1616`__ ] -	Add instance group support to SO
-  [`SO-1604 <https://jira.onap.org/browse/SO-1604`__ ] -	SO Catalog Enhancement to support CDS Meta Data for VNF/PNF and PNF Tosca Ingestion 
-  [`SO-1598 <https://jira.onap.org/browse/SO-1598`__ ] -	add equals and hashcode support to dslquerybuilder
-  [`SO-1597 <https://jira.onap.org/browse/SO-1597`__ ] -	improvements to audit inventory feature
-  [`SO-1596 <https://jira.onap.org/browse/SO-1596`__ ] -	query clients now have more useable result methods
-  [`SO-1590 <https://jira.onap.org/browse/SO-1590`__ ] -	skip cloud region validation for 1906
-  [`SO-1589 <https://jira.onap.org/browse/SO-1589`__ ] -	flow validators can now be skipped via an annotation
-  [`SO-1582 <https://jira.onap.org/browse/SO-1582`__ ] -	vnf spin up gr api vnf s base module fails
-  [`SO-1573 <https://jira.onap.org/browse/SO-1573`__ ] -	Abstract for CDS Implementation  
-  [`SO-1569 <https://jira.onap.org/browse/SO-1569`__ ] -	do not attempt to commit empty transactions
-  [`SO-1538 <https://jira.onap.org/browse/SO-1538`__ ] -	Integration Test for SO VNFM Adapter - Perform the functional test to validate VNFM Adapter NBI and SOL003-based SBI
-  [`SO-1534 <https://jira.onap.org/browse/SO-1534`__ ] -	Create Pre Building Block validator to check if cloud-region orchestration-disabled is true
-  [`SO-1533 <https://jira.onap.org/browse/SO-1533`__ ] -	flowvaldiator will allow more flexible filtering
-  [`SO-1512 <https://jira.onap.org/browse/SO-1512`__ ] -	Added Camunda migration scripts and updated camunda springboot version
-  [`SO-1506 <https://jira.onap.org/browse/SO-1506`__ ] -	E2E Automation - Extend PNF workflow with post-instantiation configuration
-  [`SO-1501 <https://jira.onap.org/browse/SO-1501`__ ] -	add new functionality to aai client
-  [`SO-1495 <https://jira.onap.org/browse/SO-1495`__ ] -	made max retries configurable via mso config repo
-  [`SO-1493 <https://jira.onap.org/browse/SO-1493`__ ] -	restructure a&ai client
-  [`SO-1487 <https://jira.onap.org/browse/SO-1487`__ ] -	added license headers to various java files
-  [`SO-1485 <https://jira.onap.org/browse/SO-1485`__ ] -	add DSL endpoint support to A&AI Client
-  [`SO-1483 <https://jira.onap.org/browse/SO-1483`__ ] -	SO to support a new GRPC client for container to container communication
-  [`SO-1482 <https://jira.onap.org/browse/SO-1482`__ ] -	SO Generic Building Block to support config deploy action for CONFIGURE Step
-  [`SO-1481 <https://jira.onap.org/browse/SO-1481`__ ] -	Generic Bulding block for assign shall trigger controller for config assign action
-  [`SO-1477 <https://jira.onap.org/browse/SO-1477`__ ] -	AAF support for SO
-  [`SO-1476 <https://jira.onap.org/browse/SO-1476`__ ] -	Do not process vf module being created when building an index
-  [`SO-1475 <https://jira.onap.org/browse/SO-1475`__ ] -	store raw distribution notification in db
-  [`SO-1474 <https://jira.onap.org/browse/SO-1474`__ ] -	Test Issue
-  [`SO-1469 <https://jira.onap.org/browse/SO-1469`__ ] -	Refactor OOF Homing to Java
-  [`SO-1462 <https://jira.onap.org/browse/SO-1462`__ ] -	Clean up AT&T Acronyms from Unit tests for audit
-  [`SO-1459 <https://jira.onap.org/browse/SO-1459`__ ] -	add maven build properties to spring actuator
-  [`SO-1456 <https://jira.onap.org/browse/SO-1456`__ ] -	prototype fetching resources from openstack and compare to a ai
-  [`SO-1452 <https://jira.onap.org/browse/SO-1452`__ ] -	added list of flows to execution for cockpit
-  [`SO-1451 <https://jira.onap.org/browse/SO-1451`__ ] -	Updated the SDC API call with the ECOMP OE from AAI
-  [`SO-1450 <https://jira.onap.org/browse/SO-1450`__ ] -	support for secure communications between SO and Multicloud
-  [`SO-1447 <https://jira.onap.org/browse/SO-1447`__ ] -	Refine multicloud use of SO cloudsites and identify DB
-  [`SO-1446 <https://jira.onap.org/browse/SO-1446`__ ] -	Multicloud API updates for generic clouds
-  [`SO-1445 <https://jira.onap.org/browse/SO-1445`__ ] -	Multicloud support for volume groups and networks
-  [`SO-1444 <https://jira.onap.org/browse/SO-1444`__ ] -	AAI update after vfmodule creation
-  [`SO-1443 <https://jira.onap.org/browse/SO-1443`__ ] -	Prepare user_directives for multicloud API
-  [`SO-1442 <https://jira.onap.org/browse/SO-1442`__ ] -	Prepare sdnc_directives for multicloud API
-  [`SO-1441 <https://jira.onap.org/browse/SO-1441`__ ] -	Handle distribution of service with generic cloud artifacts
-  [`SO-1436 <https://jira.onap.org/browse/SO-1436`__ ] -	removed unnecessary repository from pom.xml
-  [`SO-1432 <https://jira.onap.org/browse/SO-1432`__ ] -	duplicate add custom object support to a ai client
-  [`SO-1431 <https://jira.onap.org/browse/SO-1431`__ ] -	Test issue 1
-  [`SO-1429 <https://jira.onap.org/browse/SO-1429`__ ] -	add custom object support to a ai client
-  [`SO-1427 <https://jira.onap.org/browse/SO-1427`__ ] -	Fix to include alloc pool from dhcpStart/end on reqs
-  [`SO-1426 <https://jira.onap.org/browse/SO-1426`__ ] -	Upgraded tosca parser to version 1.4.8 and updated imports
-  [`SO-1425 <https://jira.onap.org/browse/SO-1425`__ ] -	Re-Factor DMAAP Credentials to use encrypted auth
-  [`SO-1421 <https://jira.onap.org/browse/SO-1421`__ ] -	Support for SO->ExtAPI interface/API
-  [`SO-1414 <https://jira.onap.org/browse/SO-1414`__ ] -	update all inprogress checks in apih handler
-  [`SO-1413 <https://jira.onap.org/browse/SO-1413`__ ] -	replaced org.mockito.Matchers with ArgumentMatchers
-  [`SO-1411 <https://jira.onap.org/browse/SO-1411`__ ] -	Test Issue
-  [`SO-1409 <https://jira.onap.org/browse/SO-1409`__ ] -	added in validation for number of keys provided
-  [`SO-1405 <https://jira.onap.org/browse/SO-1405`__ ] -	apih infra shall ensure data for si matches on macro requests
-  [`SO-1404 <https://jira.onap.org/browse/SO-1404`__ ] -	covert sync calls for create and delete network to async
-  [`SO-1395 <https://jira.onap.org/browse/SO-1395`__ ] -	E2E Automation - PreInstatition and PostInstatition use cases 
-  [`SO-1389 <https://jira.onap.org/browse/SO-1389`__ ] -	added mso-request-id when calling SDNCHandler subflow
-  [`SO-1388 <https://jira.onap.org/browse/SO-1388`__ ] -	descriptive messages now returned by validator
-  [`SO-1387 <https://jira.onap.org/browse/SO-1387`__ ] -	naming ms client fixes
-  [`SO-1385 <https://jira.onap.org/browse/SO-1385`__ ] -	removed retired A&AI versions from codebase
-  [`SO-1384 <https://jira.onap.org/browse/SO-1384`__ ] -	sdnc handler was not sending workflow exception upwards
-  [`SO-1383 <https://jira.onap.org/browse/SO-1383`__ ] -	refactored validator to be more generic
-  [`SO-1381 <https://jira.onap.org/browse/SO-1381`__ ] -	Quality of Life logging improvements
-  [`SO-1380 <https://jira.onap.org/browse/SO-1380`__ ] -	Service Proxy Consolidation
-  [`SO-1379 <https://jira.onap.org/browse/SO-1379`__ ] -	Add validation for vnfs before WorkflowAction starts
-  [`SO-1378 <https://jira.onap.org/browse/SO-1378`__ ] -	get subnet sequence number from A&AI
-  [`SO-1377 <https://jira.onap.org/browse/SO-1377`__ ] -	Re-enable Actuator for Springboot 2.0
-  [`SO-1376 <https://jira.onap.org/browse/SO-1376`__ ] -	Created sniro request pojos for homingV2 flow
-  [`SO-1370 <https://jira.onap.org/browse/SO-1370`__ ] -	Preparation for next scale-out after successful instantiation of the current scale-out operation
-  [`SO-1369 <https://jira.onap.org/browse/SO-1369`__ ] -	Processing of configuration parameters during instantiation and scale-out
-  [`SO-1368 <https://jira.onap.org/browse/SO-1368`__ ] -	VNF Health check during scale-out to be made as a separate workflow
-  [`SO-1367 <https://jira.onap.org/browse/SO-1367`__ ] -	Invoke the APP-C service configuration API after E2E Service instantiation
-  [`SO-1366 <https://jira.onap.org/browse/SO-1366`__ ] -	SO Workflow need to call configure API during instantiation
-  [`SO-1362 <https://jira.onap.org/browse/SO-1362`__ ] -	Changed the MDC sourcing from LoggingInterceptor to JaxRsFilterLogging.
-  [`SO-1346 <https://jira.onap.org/browse/SO-1346`__ ] -	Use SLF4J/Logback, instead of Log4J
-  [`SO-1307 <https://jira.onap.org/browse/SO-1307`__ ] -	Add Headers
-  [`SO-1295 <https://jira.onap.org/browse/SO-1295`__ ] -	Update SDNC client Version in POM
-  [`SO-1293 <https://jira.onap.org/browse/SO-1293`__ ] -	Vnf Recreate
-  [`SO-1290 <https://jira.onap.org/browse/SO-1290`__ ] -	Update orchestrationrequest response
-  [`SO-1288 <https://jira.onap.org/browse/SO-1288`__ ] -	Enhance GRM Clients to use encrypted auth loading
-  [`SO-1287 <https://jira.onap.org/browse/SO-1287`__ ] -	Change all SDNC Calls in GR_API
-  [`SO-1284 <https://jira.onap.org/browse/SO-1284`__ ] -	Create Relationship between Vnf and Tenant
-  [`SO-1283 <https://jira.onap.org/browse/SO-1283`__ ] -	Fix GR_API cloud info retrieval
-  [`SO-1282 <https://jira.onap.org/browse/SO-1282`__ ] -	Update Alacarte Logic for Recreate Flow
-  [`SO-1279 <https://jira.onap.org/browse/SO-1279`__ ] -	Replaced the VNFC hardcoded Function 
-  [`SO-1278 <https://jira.onap.org/browse/SO-1278`__ ] -	Move all ecomp.mso properties to org.onap.so
-  [`SO-1276 <https://jira.onap.org/browse/SO-1276`__ ] -	Add Cloud_Owner to northbound request table
-  [`SO-1275 <https://jira.onap.org/browse/SO-1275`__ ] -	Resolve path issues
-  [`SO-1274 <https://jira.onap.org/browse/SO-1274`__ ] -	CreateAndUpdatePNFResource workflow:: Associate PNF instance
-  [`SO-1272 <https://jira.onap.org/browse/SO-1272`__ ] -	Use UUID to fill pnf-id in PNF PnP sub-flow
-  [`SO-1270 <https://jira.onap.org/browse/SO-1270`__ ] -	Add New A&AI objects
-  [`SO-1269 <https://jira.onap.org/browse/SO-1269`__ ] -	Add serviceRole to MSO SNIRO Interface
-  [`SO-1260 <https://jira.onap.org/browse/SO-1260`__ ] -	Add support for naming service
-  [`SO-1233 <https://jira.onap.org/browse/SO-1233`__ ] -	Added service role to sniro request when not null
-  [`SO-1232 <https://jira.onap.org/browse/SO-1232`__ ] -	Switch to SpringAutoDeployment rather than processes.xml
-  [`SO-1229 <https://jira.onap.org/browse/SO-1229`__ ] -	Remove all usage of AlarmLogger
-  [`SO-1228 <https://jira.onap.org/browse/SO-1228`__ ] -	Limit Number of Occurs for security reasons
-  [`SO-1227 <https://jira.onap.org/browse/SO-1227`__ ] -	Remove Swagger UI due to security scan concerns
-  [`SO-1226 <https://jira.onap.org/browse/SO-1226`__ ] -	changed assign vnf sdnc to use the async subflow
-  [`SO-1225 <https://jira.onap.org/browse/SO-1225`__ ] -	Add Keystone V3 Support
-  [`SO-1207 <https://jira.onap.org/browse/SO-1207`__ ] -	accept a la carte create instance group request from vid
-  [`SO-1206 <https://jira.onap.org/browse/SO-1206`__ ] -	Added groupInstanceId and groupInstanceName columns
-  [`SO-1205 <https://jira.onap.org/browse/SO-1205`__ ] -	separate error status from progression status in req db
-  [`SO-806 <https://jira.onap.org/browse/SO-806`__ ] -	SO PNF PnP workflow shall not set "in-maint" AAI flag
-  [`SO-798 <https://jira.onap.org/browse/SO-798`__ ] -	Externalize the PNF PnP workflow 鈥?as a Service Instance Deployment workflow 鈥?adding the Controller
-  [`SO-747 <https://jira.onap.org/browse/SO-747`__ ] -	POC - Enable SO use of Multicloud Generic VNF Instantiation API
-  [`SO-700 <https://jira.onap.org/browse/SO-700`__ ] -	SO should be able to support CCVPN service assurance
-  [`SO-588 <https://jira.onap.org/browse/SO-588`__ ] -	Automate robot heatbridge manual step to add VF Module stack resources in AAI
-  [`SO-18 <https://jira.onap.org/browse/SO-18`__ ] -	Keystone v3 Support in MSO
-  [`SO-12 <https://jira.onap.org/browse/SO-12`__ ] -	Support Ocata apis
-  [`SO-10 <https://jira.onap.org/browse/SO-10`__ ] -	Deploy a MSO high availability environment 
-  [`SO-7 <https://jira.onap.org/browse/SO-7`__ ] -	Move modified openstack library to common functions repos
-  [`SO-6 <https://jira.onap.org/browse/SO-6`__ ] -	Document how to change username/password for UIs


Security Notes
--------------
	SO code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The SO open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://wiki.onap.org/pages/viewpage.action?pageId=43385708>`_.

Quick Links:

 - `SO project page <https://wiki.onap.org/display/DW/Service+Orchestrator+Project>`_
 - `Passing Badge information for SO <https://bestpractices.coreinfrastructure.org/en/projects/1702>`_
 - `Project Vulnerability Review Table for SO <https://wiki.onap.org/pages/viewpage.action?pageId=43385708>`_


**Known Issues**

	TBD

**Upgrade Notes**

	N/A

**Deprecation Notes**

	N/A

**Other**

	N/A

Version: 1.4.1
==============

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
