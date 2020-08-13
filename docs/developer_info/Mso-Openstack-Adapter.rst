.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2020 Huawei Technologies Co., Ltd.

MSO-OPENSTACK-ADAPTER :
=======================

.. contents:: :depth: 3

Cloud site operations
++++++++++++++++++++++
* Here we have 3 api's createCloudRegion, updateCloudRegion, deleteCloudRegion.

1.Create Cloud site/Cloud region:

Path: /v1/cloud-region

Method Type: POST

Request Body:
*************
{
  "id": "MTN13",
  "regionId": "mtn13",
  "cloudVersion": "3.0",
  "clli": "MDT13",
  "platform": "testFlatform",
  "orchestrator": "testOrchestrator",
  "cloudifyId": "mtn13",
  "cloudOwner": "testCloudOwner",

  "identityService": {
    "identityUrl": "testUrl",
    "msoId": "admin",
    "msoPass": "admin",
    "projectDomainName": "testDomain",
    "userDomainName": "testDomain",
    "adminTenant": "test",
    "memberRole": "test",
    "tenantMetadata": "test",
    "identityServerType": "test",
    "identityAuthenticationType": "test",
    "lastUpdatedBy": "test",
    "created": "date",
    "updated": "date"

  },

  "identityServiceId": "123",
  "lastUpdatedBy": "test",
  "created": "test",
  "updated": "test",
  "supportFabric": "true",
  "uri": "test"

}

* In this api we can create cloud region and cloud site and saving in catalog db and in AAI.

* This api requires cloud-region-id and cloudSite as inputs.

* In createCloudRegion two steps are there.

* CreateRegionInCatalogDb(cloudSite) :- here we are creating cloud region in catalogdb if it is not exists in db(cloud_sites).

* CreateCloudRegionInAAI(cloudSite) :- mapping cloudSite into cloudregion , this cloudregion we are creating in AAI and create CloudRegion   NetworkTechnology Relationship.

2.Delete Cloud site:

* Path:/v1/cloud-region/{cloud-region-id}/{cloud-owner}

* Here we can delete Cloud site by cloud region id

3.Update Cloud site:

* Path:/v1/cloud-region/{cloud-region-id}/{cloud-owner}

* Here we can update Cloud site by cloud region id 

Create Tenant in Cloud:
++++++++++++++++++++++++

Path: http://host:port/vnfs/rest/v1/tenants

Method Type: POST

Request Body:
*************

{ 
"cloudSiteId": "DAN", 
"tenantName":"RAA_1",
"failIfExists": true, 
"msoRequest": { 
"requestId": "ra1",
"serviceInstanceId":"sa1" 
}
} 

RESPONSE:
**********

{

"cloudSiteId": "DAN",
"tenantId": "128e10b9996d43a7874f19bbc4eb6749", 
"tenantCreated": true,
"tenantRollback": {
"tenantId": "128e10b9996d43a7874f19bbc4eb6749",
"cloudId": "DAN", // RAA? cloudId instead of cloudSiteId
"tenantCreated": true,
"msoRequest": { 
"requestId": "ra1",
"serviceInstanceId": "sa1"
 
} 
} 
}

Code Flow:

* Call to MSO createTenant adapter then  call to catalog db for cloud site info and from cloud site get IdentityServerType.
* based on Cloud IdentityServerType it returns ORM or KEYSTONE Utils
* Query for a tenant with the specified name in the given cloud. If the tenant exists, return an MsoTenant object. If not, return null.
* Create a tenant with the specified name in the given cloud. If the tenant already exists, an Exception will be thrown. The MSO User will also be added to the "member" list of the new tenant to perform subsequent Nova/Heat commands in the tenant. If the MSO User association fails, the entire transaction will be rolled back.

TaskServices
++++++++++++++
**1. CreateInventory**

Flow Diagram:

.. image :: ../images/InventoryCreate.png

Code Flow:

* Get cloud site using cloud region id from catalog db.

* Instantiate heat bridge client by passing required values.

* Authenticate heat bridge client.

* If authentication is success we are getting all stack resources(openstack servers. servers contains openstack images and flavours.)

* From stack resources we are getting osServers/ compute resources.

* CreatePserversAndPinterfacesIfNotPresentInAai --Transform Openstack Server object to AAI Pserver object. create pserver in AAI if it is not exist. and get openstackresource id's / ports -- transform ports to pinterface Object in AAI.

* After extract osimages and os flavours from osservers.

* After that transform osimages to AAI images and osflavours to AAI flavors and add to AAI.

* Create Vservers in AAI and make relationships to AAI image, AAI flavor, AAI pserver, vf-module.

* Add Vservers and Linterfaces to AAI--Transform port to Linterface and for that port Get Ip addresses and updateLInterfaceIps and updateLInterfaceVlan.

* Finally update AAI. 
  
**2. AuditCreateStack**

Flow Diagram:

.. image :: ../images/InventoryAddAudit.png

Code Flow:

* First we are capturing entry timestamp, MSO REQUESTID,  Servicename/topic name,partnername as openstack adapter, invocation id  is generating one random uuid.

* After that getting auditInventory details from externalTask object

* From auditInventory object we are getting cloud region,clowd owner,tenant id, heatstack name.these details we are passing to get AAIObjectAuditList.

* Use cloudRegion, tenantId, heatStackName to get resources from Heat client.

* To get resources we need heat client it means getting keystone url and token. 

* Get keystone url based on server type(keystone/keystonev3)

* From resources object we are getting novaResources and resource groups and neutronPortDetails.

* Create VserverSet by passing resources, novaResources, neutronPortDetails and setting up servers with ports to Set of Servers.

* From resource groups we are getting Link(stackLink) from that link we are extracting resource path .if path is present we are Fetching nested Resource Stack Information.

* audit all vservers,l-interfaces and sub l-interfaces. and checking in these vservers , l-interfaces and sub interfaces are existed or not in AAI. 

* If AAIObjectAuditList is present then we are setting audit type as create and Heat stack name to AAIObjectAuditList.

* If Audit create success putting in variable to complete external task other wise sending exception.

* If retrycount=1 sending error as retrycount exhausted.

**3.  AuditQueryStack**

Flow Diagram:

.. image :: ../images/InventoryQueryAudit.png


Code Flow:

* Get AuditInventory details from external task.

* Get AAIObjectAuditList from auditHeatStack  by passing these parametersauditInventory.getCloudRegion(),auditInventory.getCloudOwner(), auditInventory.getTenantId(), auditInventory.getHeatStackName().

* Fetching Top Level Stack Information.i.e get all the heat model resources.

* From heat model resources get all OS::Nova::Servers and OS::Heat::ResourceGroups and neutronPortDetails.

* Once u got all the stack info u create vserverset and add that vserverset to AAIObjectAuditList.

* After add AAIObjectAuditList to requestdb if it is not exist in requestdb.

* If it is success complete the external task.

**4. AuditDeleteStack**

Flow Diagram:

.. image :: ../images/InventoryDeleteAudit.png

Code Flow:

* Get AuditInventory details from external task 

* GetStackDataFromRequestDb usiing AuditInventory details.

* Get RequestProcessingData from requestdb using auditInventory.getVfModuleId(),auditInventory.getHeatStackName(), "AuditStackData".

* From RequestProcessingData get AAIObjectAuditList.

* If AAIObjectAuditList is empty check vservers is exist or not in AAI and set that result as setDoesObjectExist(true/false).

* Else Auditing Vservers based on vf module relationships--i.e based on genericVnfId and vfModuleName get all the vservers and create AAIObject and set setDoesObjectExist(true) and set that object to list.

* After that set audit type as delete and heatstackname and check that audit create success or not.

* If it is success complete the external task.

**5. DeleteInventory**

Flow Diagram:

.. image :: ../images/DeleteInventory.png


Code Flow:

* Get CloudInformation from external task.

* First get CloudSite details from catalog db by using regionid.

* From CloudSite get CloudIdentity details.

* Create a heatbridgeclient. and authenticate the heatbridge.

* After that delete the VFmodule data using vnfid and vf moduleid.

* Once delete success complete the external task.

**6. StackService**

Flow Diagram:

.. image :: ../images/OpenstackAdapterInvoke.png


Code Flow:

* From external task  getting openstackAdapterTaskRequest and adding to string(xmlRequest).

* From xmlrequest we are getting requestType.

1. If requestType is createVolumeGroupRequest then we are creating volume group.

* Marshalling xmlRequest  to CreateVolumeGroupRequest.

* From CreateVolumeGroupRequest getting completeVnfVfModuleType.

* After that we are creating vf module by passing required paremeters.

* If request type Start with X then we are orchestrating a VNF - *NOT* a module!

* RequestType start with Volume then then we are sending true as volume request.

* Creating a VnfRollback object by passing required parameters.

* If nestedStackId is not null we are getting nested heat stack(stack information).

* If nested heat stack is not null we are getting that output and storing into nestedVolumeOutputs object.

* If nestedBaseStackId is not null we are getting nestedBaseHeatStack information and setting output to nestedBaseHeatStack.

* If modelCustomizationUuid is available then with that mcu we are getting VFModuleCustomization(vfmc);

* If we get VFModuleCustomization then we are extracting VfModule.

* If vf version is not null then we will get vnfresource by passing vnftype and vnfversion.

* By here - we would have either a vf or vnfResource.

* If vf module is not null we are geting vnfresource from vf and from this vnfresource  we are getting minversionvnf and maxversionvnf.

* If minversionvnf and maxversionvnf are not null we are checking cloudConfig is null or not, if cloudConfig is not null then we are getting cloud site intormation by passing cloud site id. once we get the cloud site details we are getting min and max cloud site versions. 

* By the time we get here - heatTemplateId and heatEnvtId should be populated (or null)

* If it is old way we will get heatTemplate directly. else if vf module is not null then
* If it is a volume request then we will get volumeheattemplate and volumeheatenvironment.
* If it is not a volume request then we will get ModuleHeatTemplate and HeatEnvironment.

* Once we get heat templates we are getting nestedtemplates.

* Also add the files: for any get_files associated with this vnf_resource_id

* Add ability to turn on adding get_files with volume requests (by property).

* If it is not a volume request get heat files from vf module.

* CheckRequiredParameters in MsoVnfAdapterImpl.CHECK_REQD_PARAMS.

* Parse envt entries to see if reqd parameter is there (before used a simple grep

* Only submit to openstack the parameters in the envt that are in the heat template

*  Convert what we got as inputs (Map<String, String>) to a Map<String, Object> - where the object matches the param type identified in the template This will also not copy over params that aren't identified in the template

* Now simply add the outputs as we received them - no need to convert to string

* Add in the volume stack outputs if applicable

* Get all heat template parameters and add to list.

* Check for missing parameters null or not.if no missing parameters we can proceed for next step.

* Next create stack with all required values.

* After creating add heat stackname to vfrollback and copy heatstack outputs to outputs value. so now vf module is created successfully.

* After sending the response to create volume group.once it is created that response we are setting to VolumeGroup response object. 

2. If requestType is createVfModuleRequest then we are creating VfModule.

* Marshalling xmlRequest  to CreateVolumeGroupRequest.

* From CreateVolumeGroupRequest getting completeVnfVfModuleType.

* After that we are creating vf module by passing required paremeters.

* If request type Start with X then we are orchestrating a VNF - *NOT* a module!

* RequestType start with Volume then then we are sending true as volume request.

* Creating a VnfRollback object by passing required parameters.

* If nestedStackId is not null we are getting nested heat stack(stack information).

* If nested heat stack is not null we are getting that output and storing into nestedVolumeOutputs object.

* If nestedBaseStackId is not null we are getting nestedBaseHeatStack information and setting output to nestedBaseHeatStack.

* If modelCustomizationUuid is available then with that mcu we are getting VFModuleCustomization(vfmc);

* If we get VFModuleCustomization then we are extracting VfModule.

* If vf version is not null then we will get vnfresource by passing vnftype and vnfversion.

* By here - we would have either a vf or vnfResource.

* If vf module is not null we are geting vnfresource from vf and from this vnfresource  we are getting minversionvnf and maxversionvnf.

* If minversionvnf and maxversionvnf are not null we are checking cloudConfig is null or not, if cloudConfig is not null then we are getting cloud site intormation by passing cloud site id. once we get the cloud site details we are getting min and max cloud site versions. 

* By the time we get here - heatTemplateId and heatEnvtId should be populated (or null)

* If it is old way we will get heatTemplate directly. else if vf module is not null then

* If it is a volume request then we will get volumeheattemplate and volumeheatenvironment.

* If it is not a volume request then we will get ModuleHeatTemplate and HeatEnvironment.

* Once we get heat templates we are getting nestedtemplates.

* Also add the files: for any get_files associated with this vnf_resource_id

* Add ability to turn on adding get_files with volume requests (by property).

* If it is not a volume request get heat files from vf module.

* CheckRequiredParameters in MsoVnfAdapterImpl.CHECK_REQD_PARAMS.

* Parse envt entries to see if reqd parameter is there (before used a simple grep

* Only submit to openstack the parameters in the envt that are in the heat template

* Convert what we got as inputs (Map<String, String>) to a Map<String, Object> - where the object matches the param type identified in the template This will also not copy over params that aren't identified in the template

* Now simply add the outputs as we received them - no need to convert to string

* Add in the volume stack outputs if applicable

* Get all heat template parameters and add to list.

* Check for missing parameters null or not.if no missing parameters we can proceed for next step.

* Next create stack with all required values.

* After creating add heat stackname to vfrollback and copy heatstack outputs to outputs value. so now vf module is created successfully.

3. If requestType is deleteVfModuleRequest then we are deleting VfModule .

* Get stackinfo using msoHeatUtils by passing cloudSiteId, cloudOwner, tenantId, vnfName parameters.

* After that using modelCustomizationUuid we are getting VFModuleCustomizaiton--VFModule--heattemplate.

* After that we are callling msoHeatUtils.deleteStack to delete StackInfo once it deletes we are updating status as deleted using msoHeatUtils.updateResourceStatus.

4. If requestType is deleteVolumeGroupRequest then we are deleting volume group.

* Making DeleteVolumeGroupRequest by Unmarshalling xml request.

* Getting stack information by passing stackName, cloudSiteId, tenantId.

* If current stack is null then we confirm that heat status not found.

* If current stack is not null then we are deleting the stack.

* Once volumegroup is deleted we are setting the response to perticular response class.

5. If requestType is createNetworkRequest then we are creating network.

* Making CreateNetworkRequest by Unmarshalling xmlrequest.

* Check request is contrailRequest or not if it is contrailRequest we are setting shared,external,routeTargets,fqdns,routeTable values else we are setting physicalNetworkName and vlans.

* Now all the above required values  we are passing to create network.

* Build a default NetworkRollback object (if no actions performed).

* Getting the cloud site by passing cloudsite id.

* After we are doing a network check and Retrieve the Network Resource definition.

* If modelCustomizationUuid null and networkType is not null based on networkType we are getting Network Resource.

* If modelCustomizationUuid not null then based on modelCustomizationUuid we are getting NetworkResourceCustomization and from that we are getting Network Resource.

* If NetworkResourceCustomization is null then based on modelCustomizationUuid we are getting CollectionNetworkResourceCustomization and from that we are getting Network Resource.

* Even though Network Resource is null we are sending error Unable to get network resource with NetworkType.

* From the network resource we are extracting mode and network type.

* All Networks are orchestrated via HEAT or Neutron if it is other than that we are sending error.

* After that we are checking network resorce min and max versions with cloud site version if version is not supported throwing error.

* After that validating the network parameters that if any parameter is missing.

* Once we Retrieve the Network Resource we are getting heat template and NetworkType from that.

* From the heat template  we are getting template and checking that template contains os3nw if it is contains making flag is true.

* First, look up to see if the Network already exists (by name).For HEAT orchestration of networks, the stack name will always match the network name

* If stack is already exist with the network name throw an error.

* After that from stackinfo get network id and network fqdn and get outputs from Stackinfo.

* After that get subnetIdMap based on one subnet_id output or multiples subnet_%aaid% outputs from Stackinfo outputs.

* After that update the updateResourceStatus as exits in requestdb. 

* Now we are Ready to deploy the new Network and  Build the common set of HEAT template parameters

* Validate (and update) the input parameters against the DB definition

* Shouldn't happen unless DB config is wrong, since all networks use same inputs and inputs were already validated.

* If subnets are not null and template is os3template then mergesubnetsAIC3.

* If subnets are not null and template is not os3template then merge subnets.

* If policyFqdns are not null & os3template true then mergePolicyRefs.

* After that deploy the network stack-- create stack and once stack is created save into stackinfo.

* For Heat-based orchestration, the MSO-tracked network ID is the heat stack, and the neutronNetworkId is the network UUID returned in stack outputs.

* After update the resource status  like network created successfully.

* After set response to CreateNetworkResponse object.


6. If requestType is deleteNetworkRequest then we are deleting network.

* Form a DeleteNetworkRequest  by unmarshlling Xml request.

* check for mandatory fields networkId, cloudSiteId, tenantId.

* If it is there Retrieve the Network Resource definition.

* After that get networkResource.from networkResource get heat template.

* After that delete stackinfo udpate resource status as deleted.

7. If requestType is updateNetworkRequest then we are updating Network.

* Make UpdateNetworkRequest by unmarshalling xmlRequest.

* Check the params contains key shared and external if it is contains get that shared and external keys.

* Check the request is ContrailRequest or not, if it is ContrailRequest then get routeTargets , fqdns, routeTable from the actual request.

* If it is not ContrailRequest then get the ProviderVlanNetwork details like physicalNetworkName and vlans.

* Params to update network-req.getCloudSiteId(), req.getTenantId(), req.getNetworkType(),req.getModelCustomizationUuid(), req.getNetworkStackId(), req.getNetworkName(), physicalNetworkName,vlans, routeTargets, shared, external, req.getSubnets(), fqdns, routeTable, req.getMsoRequest(),    subnetIdMap, networkRollback.

* Update Network adapter with Network using networkName, networkType,cloudSiteId, tenantId.

* Capture execution time for metrics.

* Build a default rollback object (no actions performed)

* Get cloud site details from catalog db  using cloud site id.

* If cloud site is not present send the error message like CloudSite does not exist in MSO Configuration.

* Getting the cloud site by passing cloudsite id.

* After we are doing a network check and Retrieve the Network Resource definition.

* If modelCustomizationUuid null and networkType is not null based on networkType we are getting Network Resource.

* If modelCustomizationUuid not null then based on modelCustomizationUuid we are getting NetworkResourceCustomization and from that we are getting Network Resource.

* If NetworkResourceCustomization is null then based on modelCustomizationUuid we are getting CollectionNetworkResourceCustomization and from that we are getting Network Resource.

* Even though Network Resource is null we are sending error Unable to get network resource with NetworkType.

* From the network resource we are extracting mode and network type.

* All Networks are orchestrated via HEAT or Neutron if it is other than that we are sending error.

* After that we are checking network resorce min and max versions with cloud site version if version is not supported throwing error.

* After that validating the network parameters that if any parameter is missing.

* Once we Retrieve the Network Resource we are getting heat template and NetworkType from that.

* Use an MsoNeutronUtils for all Neutron commands

* If Orchestaration mode is NEUTRON Verify that the Network exists and For Neutron-based orchestration, the networkId is the Neutron Network UUID.

* Get NetworkInfo from cloud site using networkId, tenantId, cloudSiteId as params.

* If NetworkInfo is null then throw the error message as Network is does not exist.

* Update the network in cloud site which is in catalog db using cloudSiteId,tenantId,networkId,                          			neutronNetworkType, physicalNetworkName, vlans as params.

* Add the network ID and previously queried vlans to the rollback object.

* Save previous parameters such as NetworkName,PhysicalNetwork,Vlans to the NetworkRollback.

* If Orchestaration mode is HEAT then First, look up to see that the Network already exists. For Heat-based orchestration, the networkId is the network Stack ID.

* Get StackInfo by querystack using cloudSiteId, CLOUD_OWNER, tenantId, networkName.

* If StackInfo is null throw error as Network not found else continue the flow.

* Get the previous parameters such as previousNetworkName and previousPhysicalNetwork  for rollback from StackInfo.

* Get the previous vlans  from the heat params and Update Stack with Previous VLANS.

* Now we are Ready to deploy the updated Network via Heat.

* Get the HeatTemplate from NetworkResource and now we got HEAT Template from DB.

* Now get the OS::ContrailV2::VirtualNetwork property from property file , if template contains os3nw set flag as true.

* Build the common set of HEAT template parameters such as  neutronNetworkType, networkName, physicalNetworkName, vlans, routeTargets, shared, external, os3template.

* Validate and update the input parameters against the DB definition and  Shouldn't happen unless DB config is wrong, since all networks use same inputs.

* If subnets are not null and template is os3template then mergesubnetsAIC3.

* If subnets are not null and template is not os3template then merge subnets.

* If policyFqdns are not null & os3template true then mergePolicyRefs.

* If routeTableFqdns are not null os3template not null then mergeRouteTableRefs using routeTableFqdns, stackParams as params.

* Get outputs from StackInfo  and if key is contains subnet  and os3template not null then one subnet output expected else multiples subnet outputs allowed.

* Reach this point if createStack is successful.Populate remaining rollback info and response parameters and save previous parameters.

* Now Network successfully updated via HEAT.

8. If requestType is rollbackNetworkRequest then we are doing rollbackNetwork.

* Make RollbackNetworkRequest by unmarshalling xmlRequest.

* Get NetworkRollback from RollbackNetworkRequest.

* If pollForCompletion is null then set flag as true.

* Get the elements of the VnfRollback object for easier access.

* ROLLBACK Network using  networkId, cloudSiteId, tenantId.

* If NetworkCreated then use tenantId, CLOUD_OWNER, cloudSiteId, networkId, pollForCompletion, timeoutMinutes params to delete stack.

* Once deletion is success set the response to RollbackNetworkResponse.

* Once task stack service is success set the status completed for the external task.


**7. PollService**

Flow Diagram:

.. image :: ../images/OpenstackAdapterPoller.png


Code Flow:

* Get openstackAdapterTaskRequest as xmlRequest from the the external task.

* Get requestType from the xmlRequest.

1. If request type is createVolumeGroupRequest then Execute External Task Rollback Service for Create Volume Group.

* Create CreateVolumeGroupRequest by unmarshalling xmlRequest.

* Use CloudSiteId, CloudOwner, TenantId,VolumeGroupName, MsoRequest ,pollStackStatus as params to delete vnf.

* Call to openstack to delete vnf if it is success nothing to return if it is failure return failure exception.

* Once it is success set the boolean flags pollRollbackStatus and status as true.

2. If request type is createVfModuleRequest then Execute External Task Rollback Service for Create Vf Module.

* Create CreateVfModuleRequest by unmarshalling xmlRequest.

* Use CloudSiteId, CloudOwner, TenantId,VfModuleName, VnfId, VfModuleId, ModelCustomizationUuid,MsoRequest ,Holder as params to delete vf module.

* Call to openstack to get the stack outputs.

* If modelCustomizationUuid is not null then use as a param to get the VfModuleCustomization and from VfModuleCustomization get the vf module details.

* Use tenantId, cloudOwner, cloudSiteId, vnfName, pollForCompletion, timeoutMinutes as params to delete the stack .once it is success update the resources.

* Once it is success set the boolean flags pollRollbackStatus and status as true.

3. If requestType is deleteVfModuleRequest then Execute External Task Poll Service for Delete Vf Module.

* Create DeleteVfModuleRequest by unmarshalling xmlRequest.

* Get stackId from external task by using that get Stack.

* Get pollingFrequency from properties file and calculate no.of polling attempts.

* Using cloudSiteId, tenantId get Heatclient and once you got heatclient querystack to get the Stack object.

* If Stack object is not null update the stack status in requestdb and it returns Stack object.

* If Stack object is not null check the status is DELETE_COMPLETE then Stack Deletion completed successfully.


4. If requestType is deleteVolumeGroupRequest then Execute Execute External Task Poll Service for Delete Volume Group.

* Create DeleteVfModuleRequest by unmarshalling xmlRequest.

* Get stackId from external task by using that get Stack.

* Get pollingFrequency from properties file and calculate no.of polling attempts.

* Using cloudSiteId, tenantId get Heatclient and once you got heatclient querystack to get the Stack object.

* If Stack object is not null update the stack status in requestdb and it returns Stack object.

* If Stack object is not null check the status is DELETE_COMPLETE then Stack Deletion completed successfully.

5. If requestType is deleteNetworkRequest then Execute External Task Poll Service for Delete Network.

* Create DeleteVfModuleRequest by unmarshalling xmlRequest.

* Get stackId from external task by using that get Stack.

* Get pollingFrequency from properties file and calculate no.of polling attempts.

* Using cloudSiteId, tenantId get Heatclient and once you got heatclient querystack to get the Stack object.

* If Stack object is not null update the stack status in requestdb and it returns Stack object.

* If Stack object is not null check the status is DELETE_COMPLETE then Stack Deletion completed successfully.

6. If requestType is createNetworkRequest then get the PollRollbackStatus from the external task.

* If pollRollbackStatus is true then Execute External Task Poll Service for Rollback Create Network.

* Get stackId from external task by using that get Stack.

* Get pollingFrequency from properties file and calculate no.of polling attempts.

* Using cloudSiteId, tenantId get Heatclient and once you got heatclient querystack to get the Stack object.

* If Stack object is not null update the stack status in requestdb and it returns Stack object.

* If Stack object is not null check the status is DELETE_COMPLETE then Stack Deletion completed successfully.

* If pollRollbackStatus is false then Execute External Task Poll Service for Create Network.

* Get stackId from external task by using that get Stack.

* Get pollingFrequency from properties file and calculate no.of polling attempts.

* Using cloudSiteId, tenantId get Heatclient and once you got heatclient querystack to get the Stack object.

* If Stack object is not null update the stack status in requestdb and it returns Stack object.

* If Stack object is not null check the status is CREATE_COMPLETE then Stack Creation completed successfully.

7. If requestType is createNetworkRequest then Execute External Task Rollback Service for Create Network.

* Get stackId from external task by using that get Stack.

* Get pollingFrequency from properties file and calculate no.of polling attempts.

* Using cloudSiteId, tenantId get Heatclient and once you got heatclient querystack to get the Stack object.

* If Stack object is not null update the stack status in requestdb and it returns Stack object.

* If Stack object is not null check the status is UPDATE_COMPLETE then Stack Updation completed successfully.

* Once task stack service is success set the status completed for the external task.

**8. RollbackService**

Flow Diagram:

.. image :: ../images/OpenstackAdapterRollback.png


Code Flow:

* Get openstackAdapterTaskRequest as xmlRequest from the the external task.

* Get requestType from the xmlRequest.

1. If request type is createVolumeGroupRequest then Execute External Task Rollback Service for Create Volume Group.

* Making DeleteVolumeGroupRequest by Unmarshalling xml request.

* Getting stack information by passing stackName, cloudSiteId, tenantId.

* If current stack is null then we confirm that heat status not found.

* If current stack is not null then we are deleting the stack.

* Once volumegroup is deleted we are setting the response to perticular response class.

2. If request type is createVfModuleRequest then Execute External Task Rollback Service for Create Vf Module.

* Get stackinfo using msoHeatUtils by passing cloudSiteId, cloudOwner, tenantId, vnfName parameters.

* After that using modelCustomizationUuid we are getting VFModuleCustomizaiton--VFModule--heattemplate.

* After that we are callling msoHeatUtils.deleteStack to delete StackInfo once it deletes we are updating status as deleted using msoHeatUtils.updateResourceStatus.

* Once it is success set the boolean flags pollRollbackStatus and status as true.

3. If request type is createNetworkRequest then Execute External Task Rollback Service for Create Network.

* Form a DeleteNetworkRequest  by unmarshlling Xml request.

* Check for mandatory fields networkId, cloudSiteId, tenantId.

* If it is there Retrieve the Network Resource definition.

* After that get networkResource.from networkResource get heat template.

* After that delete stackinfo udpate resource status as deleted.

* Once task stack service is success set the status completed for the external task.











