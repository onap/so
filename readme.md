# ONAP MSO

----
----

# Introduction

ONAP MSO is delivered with **2 Docker containers**, 1 hosting the **database** (MariaDB) and 1 hosting the **JBoss** application server running all ONAP MSO code.

Both containers runs on the same machine and can be started with **`docker-compose`**.

# Compiling MSO

MSO can be compiled with `mvn clean install`. Integration tests are started with the following profile
`-P with-integration-tests`

**to be edited for rrelease**
Docker containers are build with the following profile
`-P docker -Ddocker.buildArg.chef_repo_branch_name=bugfix/external_adress -Ddocker.buildArg.chef_repo_git_username=git -Ddocker.buildArg.chef_repo_address=23.253.149.175/mso -Ddocker.buildArg.chef_repo_git_name=chef-repo`

# Getting the containers

ONAP MSO containers are stored on [here](https://nexus3.onap.org:10002) for the releases, and [here](https://nexus3.onap.org:10003) for the snapshots

The following Docker images are the actual deployment images used for running MSO

| Name            | Tag     | Description                                                                                                                   |
|-----------------|---------|-------------------------------------------------------------------------------------------------------------------------------|
| onap/mso   | 1.0.0   | Contains **JBoss** + **OpenJDK** + **MSO** components (BPMN engine and MSO API handlers and adapters)                         |
| library/mariadb | 10.1.11 | **MariaDB** image from Docker.io, this image hosts the database and is preloaded with MSO schema and configuration at startup |

# Starting MSO

### docker-compose

You can use `docker-compose` to start MSO.
The file is as the root of the directory.
See `Getting the containers` to pull the images

### Heat template

A heat template that can be used on RackSpace to spin up the MSO Host VM and run docker-compose is currently being built by the Demo Team.

# Accessing MSO

MSO UIs are not really used for operating MSO, but they provide information on what is currently happening and get an insight on the components.

### MSO JBoss console

JBoss Wildly provides administrative functions through the application [server console](https://docs.jboss.org/author/display/WFLY10/Admin+Guide#AdminGuide-Accessingthewebconsole].

Said console can be used to have a look at the status of MSO. It is providing details on deployed artifacts and gives a remote access to the main server log file

The UI can be accessed trough http://containerHostName:9990/

The configuration preloads a default user (admin) with the standard `placeholder` password.

The configuration of JBoss should not be touched. But it is possible to look at the two following sections for insights on the MSO health :

![deployments or runtime](http://img11.hostingpics.net/pics/332403image2016112412225.png)

Deployments shows what is deployed and running on the application server, you should see the following once MSO is up and running (Actual names of the War files may differ but their numbers and general format should be the same)

***to be upload when rrelease***

Runtime can be used to have a look a the main server log files, see JVM status and parameters, environment settings etc,...

![runtime monitor](http://img11.hostingpics.net/pics/244948image20161124123216.png)

See the logging section below for more details about other logfiles (EELF framework)

### MSO Camunda Cockpit console

MSO orchestration processes can be monitored with the [Camunda Engine cockpit UI](https://camunda.org/features/cockpit/). It gives an insight about the available processes, allows to trigger them manually and provides monitoring of the currently running processes

**IMPORTANT NOTE** : since ONAP MSO only uses Camunda Community version it is not possible to see history of running process as this is an Enterprise feature only.

#### Accessing the Cockpit

The cockpit is available at the following address : http://containerHostname:8080/cockpit

When the container is started it will create a default admin user (admin) with the password `placeholder` for UI

The cockpit gives an overview of the available BPMN (orchestration) processes (with a visual representation).
It is also possible to trigger them from the UI if you know the parameters that are needed.

***screenshots to be uploaded when rrelease***

### MSO APIs

Most of the MSO features within ONAP MSO are triggered by using **RESTful interfaces**. MSO supports both **HTTP** and **HTTPS**, but is configured on this release with HTTP only using Basic Authentification.

The MSO APIs are configured to accept requests having a **basic auth. header** set with various **username and password** depending on which API is being triggered.

All API endpoints are exposed on port **8080**, it is possible to reach all MSO subsystems directly with the proper query (see more information below on how to test MSO functions)

##### Main API endpoints in the first open source release

- ***to be completed*** APIHandler health checks
- ***to be completed*** VID API

VID endpoint : http://vm1.mso.simpledemo.onap.org:8080/ecomp/mso/infra/serviceInstances/v2

The typical easy way to trigger these endpoints is to use a RESTful client or automation framework.

# Configuration of MSO

It is important to understand that the Docker containers are using a configuration file (JSON) in order to provision MSO basic configuration, in the above Jenkins Job, Jenkins pulls that JSON file from the MSO repository, any other mean to provide that JSON file (for specific environments) would also work.

Once the deployment of the docker images is done, you will need to configure your installation to be able to interact with all the components that MSO needs.

Change the environment file located here : **/shared/mso-docker.json** then run the following command `chef-solo -c /var/berks-cookbooks/chef-repo/solo.rb -o recipe[mso-config::apih],recipe[mso-config::bpmn],recipe[mso-config::jra]`

**Important note:** The host mso is mapped automatically to c1.vm1.mso.simpledemo.onap.org in /etc/host of the docker image so you can keep mso:8080 when you want to mention the APIH, JRA or Camunda host.

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

- JBoss users credentials are defined in /opt/jboss/standalone/configuration/application-users.properties and are associated to the corresponding role in application-roles.properties (you should not change this file except if you add a new user)
- In the environment. Replace the authorisation key in the file /shared/mso-docker.json and run the command to apply the configuration as explained above.

You can encrypt the JBoss user with the following command `echo -n 'LOGIN:ApplicationRealm:PASSWORD' |openssl dgst -md5` and replace the line corresponding to this user in /opt/jboss/standalone/configuration/application-users.properties

You can replace the authentication in the environment by the value returned by the following API `GET on http://c1.vm1.mso.simpledemo.onap.org:8080/asdc/properties/encrypt/{value}/{cryptKey}` where {value} is the string login:password and cryptKey (also defined in the environment) is the key to use to encrypt the credentials

Exemple of credentials you could change:
- BPELClient: if you change this credentials, you have to change it in JBoss users AND environment file (+ apply the new config) and be careful to set the same password. In the environment it is the parameter "adaptersPoAuth" under the section "mso-bpmn-urn-config". The cryptKey to use is 07a7159d3bf51a0e53be7a8f89699be7
- BPMNClient: if you change this credentials, you have to change it in JBoss users AND environment file (+ apply the new config) and be careful to set the same password. In the environment it is the parameter "camundaAuth" under the sections "mso-api-handler-config" AND "mso-api-handler-infra-config". The cryptKey to use is
aa3871669d893c7fb8abbcda31b88b4f

# Logging

### JBoss

MSO log files are located the [JBoss log](https://docs.jboss.org/author/display/WFLY8/Logging+Configuration) folder in the container.

### EELF

EELF framework is used for **specific logs** (audit, metric and error logs). They are tracking inter component logs (request and response) and allow to follow a complete flow through the MSO subsystem

EELF logs are located at the following location on the MSO JBoss container :

- /var/log/ecomp/MSO (each module has its own folder)

The DEBUG mode is enabled by module and has to be re-enabled if the application restart.

It can be enabled with a GET on the following APIs:
- Camunda (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/mso/logging/debug
- APIH Infra (use any jboss user with role InfraPortal-Client for authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/ecomp/mso/infra/logging/debug
- ASDC (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/asdc/logging/debug
- DBAdapter (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/dbadapters/logging/debug
- Network adapter (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/networks/rest/logging/debug
- SDNC adapter (use any jboss user with role MSO-Client for authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/adapters/rest/logging/debug
- VNF adapter (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/vnfs/rest/logging/debug
- Tenant adapter (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/tenants/rest/logging/debug
- APPC adapter (no authentication): http://c1.vm1.mso.simpledemo.onap.org:8080/appc/rest/logging/debug

Default JBoss users:
- with role CSI-Client: CSIClient/password1$
- with role CSI-Client: InfraPortalClient/password1$
- with role CSI-Client: MSOClient/password1$

Note that these default users should be changed.

# Testing MSO Functionalities

For this first release of MSO, the queries to start the various VNFs should come first through API Handler.

To help with the testing we are providing here a sample [SoapUI](https://www.soapui.org/) project [file](add link when rrealease) with the main queries that VID should send to MSO

### To simulate Loading of Artifacts & models (bypass ASDC)i

The MariaDB container can load up special SQL scripts that simulates the loading of ASDC components (as if they were received through the ASDC client)

Simply use the load ability embedded to run the 'preload SQL' script for vFirewall or vDNS

### Once the HEAT artifacts are loaded into MSO

It is also possible to simulate queries to the PO (platform orchestrator) adapter of MSO (thus bypassing BPMN flows and API handler) to verify MSO interaction with Rackspace and verify the behavior of the Adapter (so that it loads HEAT and connect to Rackspace and instantiate elements)

Below is a query used from FireFox RESTClient plugin to trigger MSO adapter directly (replace values accordingly)

```
 POST http://<containername>:8080/vnfs/rest/v1/vnfs/5259ba4a-cf0d-4791-9c60-9117faa5cdea/vf-modules
Header: content-type: application/json
+Authorization
login/password BPELClient/password1$F

 {"createVfModuleRequest":{"messageId":"ec9537bb-c837-477f-86a5-21c717be96f1-1479156376597","skipAAI":true,"notificationUrl":"http://bpmnhost:8080/mso/vnfAdapterRestNotify","cloudSiteId":"RACKSPACE","tenantId":"1015548","vnfId":"5259ba4a-cf0d-4791-9c60-9117faa5cdea","vnfType":"vfw-service/VFWResource-1","vnfVersion":"1.0","vfModuleId":"7d8412bb-b288-44ff-92ef-723018f940fc","vfModuleName":"MSO_VFW_TEST","vfModuleType":"VF_RI1_VFW::module-1","volumeGroupId":"","volumeGroupStackId":"","baseVfModuleId":"","baseVfModuleStackId":"","requestType":"","failIfExists":true,"backout":true,"vfModuleParams":{"vf_module_name":"MSO_VFW_TEST","vnf_name":"vfw-service/VFWResource-1","vnf_id":"5259ba4a-cf0d-4791-9c60-9117faa5cdea","vf_module_id":"7d8412bb-b288-44ff-92ef-723018f940fc"},"msoRequest":{"requestId":"ec9537bb-c837-477f-86a5-21c717be96f1","serviceInstanceId":"369cdf85-1b61-41ff-b637-c6b7dd020326"},"synchronous":false}}
```

# Getting Help

*** to be completed on rrelease ***

mso@lists.onap.org

MSO Javadoc and Maven site

*** to be completed on rrelease ***

