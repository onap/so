# ONAP SO

----
----

# Introduction

SO (Service Orchestrator) project is mostly composed of java & groovy code along with camunda BPMN code flow.

SO consists of following sub-components:
 - API Handler (*/mso-api-handlers*) set of REST services for incoming requests (northbound clients)
 - BPMN Execution Engine (*/bpmn*) contains all business logic of service order execution  and interact with AAI, SDNC, requestdb, catalogdb etc. Exposes rest interface for Api Handler
 - Set of adapters (*/adapters*) adapters that interact with ONAP components (SDNC, VFC, Request DB, Catalog DB) or external components (VNFM, Openstack)
 - Data Stores: Catalog DB (configuration is here */mso-catalog-db*) to store service and resource models, recipes and workflows
 - SDC Client and Controller (*/asdc-controller) to receive updated models from SDC and populate Catalog DB
 - SO Monitoring (*/so-monitoring) service to monitor BPMN workflow execution status

# Compiling SO

SO can be compiled with `mvn clean install`. By default it executes:
 - the standard unit tests
 - the Spring integration tests
 - javadoc and doclint creation
 - BUT *does not build the docker images*

Integration tests are started with the following profile `-P with-integration-tests`

You can disable the integration tests by executing: `mvn clean install -DskipTests=true -Dmaven.test.skip=true`

You can disable the javadoc or doclint creation by executing `mvn clean install -Dmaven.javadoc.skip=true -Dadditionalparam=-Xdoclint:none`

# Code Formatting

Your build may fail if you don't follow Code Guidelines. In order to format files run `mvn process-sources -P format`

# Building Docker images

You can build docker images by executing profile "docker": `mvn clean install -P docker`

If you want to build docker images with out executing test and javadoc, then run the below command `mvn clean install -U -DskipTests=true -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Dadditionalparam=-Xdoclint:none -P docker`

# Getting the containers

ONAP SO containers are stored on [here](https://nexus3.onap.org:10002) for the releases, and [here](https://nexus3.onap.org:10003) for the snapshots

The following Docker images are the actual deployment images used for running SO

| Name            | Tag     | Description                                                                                                                   |
|-----------------|---------|-------------------------------------------------------------------------------------------------------------------------------|
| onap/so/api-handler-infra | 1.4.4 | MSO Api handler for SO REST service entry point |
| onap/so/bpmn-infra | 1.4.4 | BPMN-Infra contains business logic of execution flow |
| onap/so/catalog-db-adapter | 1.4.4 | CatalogDB to interact with mariaDB catalogdb schema |
| onap/so/openstack-adapter | 1.4.4 | Adapter to interact with Openstack as a VIM |
| onap/so/request-db-adapter | 1.4.4 | RequestDB to interact with mariaDB requestdb schema |
| onap/so/sdc-controller | 1.4.4 | SDC-controller to interact with SDC module |
| onap/so/sdnc-adapter | 1.4.4 | SDNC Adapter to interacts with SDNC module |
| onap/so/so-monitoring | 1.4.4 | SO Monitoring for monitoring the SO workflows |
| onap/so/vfc-adapter | 1.4.4 | Adapter to interact with VFC module |
| onap/so/vnfm-adapter | 1.4.4 | Adapter to interact with external VNFMs through SOL003 interface |
| library/mariadb | 10.1.11 | MariaDB image from Docker.io, this image hosts the database and is preloaded with SO schema and configuration at startup |

# Starting SO

### docker-compose

You can use docker-compose to start SO. For running docker-compose, you need to checkout docker-config project.

docker-config code is located in a single git repository named *so/docker-config*

To start SO:
 - `cd docker-config`
 - set up DOCKER_HOST env variable with your specific host+port i.e. `export DOCKER_HOST=tcp://127.0.0.1:2375`
 - run helper script `./deploy.sh` (OR `docker-compose up -d`)

You can also run / restart independent docker, like to run bpmn-infra docker, use command `docker-compose up -d bpmn-infra`

**NOTE**: container *onap/so/vnfm-adapter* is not started via docker-compose script

### Heat template

A heat template that can be used on RackSpace to spin up the SO Host VM and run docker-compose is currently being built by the Demo Team.

# Accessing SO

SO UIs are not really used for operating SO, but they provide information on what is currently happening and get an insight on the components.

### Spring Boot Actuator Endpoints

Some of SO components (Api Handler, SO monitoring) use Embedded Tomcat from Spring boot to run application. 
To monitor the app, Actuator endpoint can be used:

 - /manage/health - Shows application health information
 - /manage/info - Displays arbitrary application info

### SO Camunda Cockpit console

SO orchestration processes can be monitored with the [Camunda Engine cockpit UI](https://camunda.org/features/cockpit/). It gives an insight about the available processes, allows to trigger them manually and provides monitoring of the currently running processes

**IMPORTANT NOTE** : since ONAP SO only uses Camunda Community version, which don't show history of running processes - SO-Monitoring component was developed for that purpose.

#### Accessing the Cockpit

The cockpit is available at the following address : http://containerHostname:8080/cockpit

When the container is started it will create a default admin user (admin) with the password `placeholder` for UI

The cockpit gives an overview of the available BPMN (orchestration) processes (with a visual representation).
It is also possible to trigger them from the UI if you know the parameters that are needed.

***screenshots to be uploaded when rrelease***

### SO APIs

Most of the SO features within ONAP SO are triggered by using **RESTful interfaces**. SO supports both **HTTP** and **HTTPS**, but is configured on this release with HTTP only using Basic Authentification.

The SO APIs are configured to accept requests having a **basic auth. header** set with various **username and password** depending on which API is being triggered.

All API endpoints are exposed on port **8080**, it is possible to reach all SO subsystems directly with the proper query (see more information below on how to test SO functions)

##### Main API endpoints

- ***to be completed*** APIHandler health checks
- ***to be completed*** VID API

VID endpoint : http://vm1.mso.simpledemo.onap.org:8080/ecomp/mso/infra/serviceInstances/v2

The typical easy way to trigger these endpoints is to use a RESTful client or automation framework.

# Configuration of SO

It is important to understand that the Docker containers are using a configuration file (JSON) in order to provision SO basic configuration, in the above Jenkins Job, Jenkins pulls that JSON file from the SO repository, any other mean to provide that JSON file (for specific environments) would also work.

Once the deployment of the docker images is done, you will need to configure your installation to be able to interact with all the components that SO needs.

Change the environment file located here : **/shared/mso-docker.json** then run the following command `chef-solo -c /var/berks-cookbooks/chef-repo/solo.rb -o recipe[mso-config::apih],recipe[mso-config::bpmn],recipe[mso-config::jra]`

**Important note:** The host SO is mapped automatically to c1.vm1.mso.simpledemo.onap.org in /etc/host of the docker image so you can keep mso:8080 when you want to mention the APIH, JRA or Camunda host.

Here are the main parameters you could change:

- mso_config_path: define the path where the configuration files for APIH, JRA and Camunda will be deployed. This parameter should not be changed.
- In the section mso-bpmn-urn-config, adaptersOpenecompDbEndpoint: This configuration must point to the APIH hostname. It should have this form: http://mso:8080/dbadapters/RequestsDbAdapter Do not change it if you are not sure.
- In the section mso-bpmn-urn-config, aaiEndpoint: This parameter should point to the A&AI component. It should be something like: https://c1.vm1.aai.simpledemo.opap.org:8443
- In the section mso-bpmn-urn-config, aaiAuth: This parameter is the encrypted value of login:password to access the A&AI server. The key used to encrypt is defined in the parameter msoKey.
- In the section asdc-connection, asdcAddresss: Change the values with the value provided by the ASDC team. Possible value: https://c2.vm1.sdc.simpledemo.onap.org:8443 The password field may be changed as well.
- In the section mso-sdnc-adapter-config, sdncurls: Change all the values with the value provided by the SDNC team. Possible value: https://c1.vm1.sdnc.simpledemo.onap.org:8443/... ? The sdncauth field may be changed as well.
- In the section mso-appc-adapter-config, appc_url: Change the value with the value provided by the APPC team. Possible value: http://c1.vm1.appc.simpledemo.onap.org:8080 ? The appc_auth field may be changed as well.
- In the section mso-po-adapter-config, identity_url: Change the values with the value provided by the PO team. Possible value: https://identity.api.rackspacecloud.com/v2.0 ?

The credentials are defined in 2 places:

- In the application.yaml in projects
- application.yaml can be overriden in two places: in *so/docker-config* repository and directories *so/docker-config/volumes/so/config/so-monitoring/onapheat/override.yaml*
- In *oom* repository (if intstallation is done via oom)

You can find default users there for specific so component.
**Note** that these default users should be changed.

You can replace the authentication in the environment by the value returned by the following API `GET on http://c1.vm1.mso.simpledemo.onap.org:8080/asdc/properties/encrypt/{value}/{cryptKey}` where {value} is the string login:password and cryptKey (also defined in the environment) is the key to use to encrypt the credentials

# Logging

### EELF

EELF framework is used for **specific logs** (audit, metric and error logs). They are tracking inter component logs (request and response) and allow to follow a complete flow through the SO subsystem

Logs are located at the following locations in SO containers :

- /var/log/ecomp/MSO (each module has its own folder)

The DEBUG mode is enabled by module and has to be re-enabled if the application restart.

It can be enabled with a GET on the following APIs:
- Camunda (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/mso/logging/debug
- APIH Infra (use any user with role InfraPortal-Client for authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/ecomp/mso/infra/logging/debug
- ASDC (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/asdc/logging/debug
- DBAdapter (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/dbadapters/logging/debug
- Network adapter (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/networks/rest/logging/debug
- SDNC adapter (use any user with role MSO-Client for authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/adapters/rest/logging/debug
- VNF adapter (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/vnfs/rest/logging/debug
- Tenant adapter (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/tenants/rest/logging/debug
- APPC adapter (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/appc/rest/logging/debug

# Testing SO Functionalities

For this first release of SO, the queries to start the various VNFs should come first through API Handler.

To help with the testing we are providing here a sample [SoapUI](https://www.soapui.org/) project [file](add link when rrealease) with the main queries that VID should send to SO

### To simulate Loading of Artifacts & models (bypass ASDC)i

The MariaDB container can load up special SQL scripts that simulates the loading of ASDC components (as if they were received through the ASDC client)

Simply use the load ability embedded to run the 'preload SQL' script for vFirewall or vDNS

### Once the HEAT artifacts are loaded into SO

It is also possible to simulate queries to the PO (platform orchestrator) adapter of SO (thus bypassing BPMN flows and API handler) to verify SO interaction with Rackspace and verify the behavior of the Adapter (so that it loads HEAT and connect to Rackspace and instantiate elements)

Below is a query used from FireFox RESTClient plugin to trigger SO adapter directly (replace values accordingly)

```
 POST http://<containername>:8080/vnfs/rest/v1/vnfs/5259ba4a-cf0d-4791-9c60-9117faa5cdea/vf-modules
Header: content-type: application/json
+Authorization
login/password BPELClient/password1$F

 {"createVfModuleRequest":{"messageId":"ec9537bb-c837-477f-86a5-21c717be96f1-1479156376597","skipAAI":true,"notificationUrl":"http://bpmnhost:8080/mso/vnfAdapterRestNotify","cloudSiteId":"RACKSPACE","tenantId":"1015548","vnfId":"5259ba4a-cf0d-4791-9c60-9117faa5cdea","vnfType":"vfw-service/VFWResource-1","vnfVersion":"1.0","vfModuleId":"7d8412bb-b288-44ff-92ef-723018f940fc","vfModuleName":"MSO_VFW_TEST","vfModuleType":"VF_RI1_VFW::module-1","volumeGroupId":"","volumeGroupStackId":"","baseVfModuleId":"","baseVfModuleStackId":"","requestType":"","failIfExists":true,"backout":true,"vfModuleParams":{"vf_module_name":"MSO_VFW_TEST","vnf_name":"vfw-service/VFWResource-1","vnf_id":"5259ba4a-cf0d-4791-9c60-9117faa5cdea","vf_module_id":"7d8412bb-b288-44ff-92ef-723018f940fc"},"msoRequest":{"requestId":"ec9537bb-c837-477f-86a5-21c717be96f1","serviceInstanceId":"369cdf85-1b61-41ff-b637-c6b7dd020326"},"synchronous":false}}
```

# Getting Help

Subscribe and post messages with SO tag in onap-discuss group at https://lists.onap.org/g/onap-discuss

