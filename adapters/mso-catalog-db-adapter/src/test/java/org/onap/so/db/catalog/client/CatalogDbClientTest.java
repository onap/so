/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.db.catalog.client;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.catalogdb.CatalogDbAdapterBaseTest;
import org.onap.so.db.catalog.beans.AuthenticationType;
import org.onap.so.db.catalog.beans.BBNameSelectionReference;
import org.onap.so.db.catalog.beans.BuildingBlockRollback;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.db.catalog.beans.ExternalServiceToInternalService;
import org.onap.so.db.catalog.beans.HomingInstance;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.PnfResource;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.ProcessingFlags;
import org.onap.so.db.catalog.beans.ServerType;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfComponentsRecipe;
import org.onap.so.db.catalog.beans.VnfRecipe;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;

public class CatalogDbClientTest extends CatalogDbAdapterBaseTest {

    public static final String MTN13 = "mtn13";

    @LocalServerPort
    private int port;

    @Value("${mso.db.auth}")
    private String msoAdaptersAuth;

    @Autowired
    CatalogDbClientPortChanger client;



    @Before
    public void initialize() {
        client.wiremockPort = String.valueOf(port);
        client.setEndpoint(getEndpoint(port));
    }

    protected String getEndpoint(int port) {
        return "http://localhost:" + port;
    }

    @Test
    public void testGetRainyDayHandler_Regex() {
        RainyDayHandlerStatus rainyDayHandlerStatus = client.getRainyDayHandlerStatus("AssignServiceInstanceBB", "*",
                "*", "*", "*", "The Flavor ID (nd.c6r16d20) could not be found.", "*");
        assertEquals("Rollback", rainyDayHandlerStatus.getPolicy());
    }

    @Test
    public void testGetRainyDayHandler__Encoding_Regex() {
        RainyDayHandlerStatus rainyDayHandlerStatus = client.getRainyDayHandlerStatus("AssignServiceInstanceBB", "*",
                "*", "*", "*",
                "resources.lba_0_dmz_vmi_0: Unknown id: Error: oper 1 url /fqname-to-id body {\"fq_name\": [\"zrdm6bvota05-dmz_sec_group\"], \"type\": \"security-group\"} response Name ['zrdm6bvota05-dmz_sec_group'] not found",
                "*");
        assertEquals("Rollback", rainyDayHandlerStatus.getPolicy());
    }

    @Test
    public void testGetCloudSiteHappyPath() throws Exception {
        CloudSite cloudSite = client.getCloudSite(MTN13);
        assertNotNull(cloudSite);
        assertNotNull(cloudSite.getIdentityService());
        assertEquals("MDT13", cloudSite.getClli());
        assertEquals("mtn13", cloudSite.getRegionId());
        assertEquals("MTN13", cloudSite.getIdentityServiceId());
    }

    @Test
    public void testGetCloudSiteNotFound() throws Exception {
        CloudSite cloudSite = client.getCloudSite(UUID.randomUUID().toString());
        assertNull(cloudSite);
    }

    @Test
    public void testGetCloudifyManagerHappyPath() throws Exception {
        CloudifyManager cloudifyManager = client.getCloudifyManager("mtn13");
        assertNotNull(cloudifyManager);
        assertEquals("http://localhost:28090/v2.0", cloudifyManager.getCloudifyUrl());

    }

    @Test
    public void testGetCloudifyManagerNotFound() throws Exception {
        CloudifyManager cloudifyManager = client.getCloudifyManager(UUID.randomUUID().toString());
        assertNull(cloudifyManager);
    }


    @Test
    public void testGetCloudSiteByClliAndAicVersionHappyPath() throws Exception {
        CloudSite cloudSite = client.getCloudSiteByClliAndAicVersion("MDT13", "2.5");
        assertNotNull(cloudSite);
    }

    @Test
    public void testGetCloudSiteByClliAndAicVersionNotFound() throws Exception {
        CloudSite cloudSite = client.getCloudSiteByClliAndAicVersion("MDT13", "232496239746328");
        assertNull(cloudSite);
    }

    @Test
    public void testGetServiceByID() throws Exception {
        Service serviceByID = client.getServiceByID("5df8b6de-2083-11e7-93ae-92361f002671");
        assertNotNull(serviceByID);
        assertEquals("MSOTADevInfra_vSAMP10a_Service", serviceByID.getModelName());
        assertEquals("NA", serviceByID.getServiceType());
        assertEquals("NA", serviceByID.getServiceRole());
    }

    @Test
    public void testGetServiceByIDNotFound() throws Exception {
        Service serviceByID = client.getServiceByID(UUID.randomUUID().toString());
        assertNull(serviceByID);
    }

    @Test
    public void testGetVfModuleByModelUUID() throws Exception {
        VfModule vfModule = client.getVfModuleByModelUUID("20c4431c-246d-11e7-93ae-92361f002671");
        assertNotNull(vfModule);
        assertNotNull(vfModule.getVfModuleCustomization());
        assertEquals("78ca26d0-246d-11e7-93ae-92361f002671", vfModule.getModelInvariantUUID());
        assertEquals("vSAMP10aDEV::base::module-0", vfModule.getModelName());
    }

    @Test
    public void testGetVfModuleByModelUUIDNotFound() throws Exception {
        VfModule vfModule = client.getVfModuleByModelUUID(UUID.randomUUID().toString());
        assertNull(vfModule);
    }

    @Test
    public void testGetVnfResourceByModelUUID() throws Exception {
        VnfResource vnfResource = client.getVnfResourceByModelUUID("ff2ae348-214a-11e7-93ae-92361f002671");
        assertNotNull(vnfResource);
        assertEquals("vSAMP10a", vnfResource.getModelName());
    }

    @Test
    public void testGetVnfResourceByModelUUIDNotFound() throws Exception {
        VnfResource vnfResource = client.getVnfResourceByModelUUID(UUID.randomUUID().toString());
        assertNull(vnfResource);
    }

    @Test
    public void testGetVnfResourceCustomizationByModelCustomizationUUID() {
        VnfResourceCustomization vnfResourceCustomization =
                client.getVnfResourceCustomizationByModelCustomizationUUID("68dc9a92-214c-11e7-93ae-92361f002671");
        assertNotNull(vnfResourceCustomization);
        assertEquals("vSAMP", vnfResourceCustomization.getNfRole());
        assertNotNull(vnfResourceCustomization.getModelCustomizationUUID());
        assertNotNull(vnfResourceCustomization.getVnfResources());
        assertNotNull(vnfResourceCustomization.getVfModuleCustomizations());
        assertEquals("vSAMP10a", vnfResourceCustomization.getVnfResources().getModelName());
        assertFalse("skip post instantiation configuration",
                vnfResourceCustomization.getSkipPostInstConf().booleanValue());
    }

    @Test
    public void testGetVnfResourceCustomizationByModelCustomizationUUINotFound() {
        VnfResourceCustomization vnfResourceCustomization =
                client.getVnfResourceCustomizationByModelCustomizationUUID(UUID.randomUUID().toString());
        assertNull(vnfResourceCustomization);
    }

    @Test
    public void testGetInstanceGroupByModelUUID() {
        InstanceGroup instanceGroup = client.getInstanceGroupByModelUUID("0c8692ef-b9c0-435d-a738-edf31e71f38b");
        assertNotNull(instanceGroup);
        assertEquals("network_collection_resource_1806..NetworkCollection..0", instanceGroup.getModelName());
        assertEquals("org.openecomp.resource.cr.NetworkCollectionResource1806",
                instanceGroup.getToscaNodeType().toString());
    }

    @Test
    public void testGetVfModuleCustomizationByModelCuztomizationUUID() {
        VfModuleCustomization vfModuleCustomization =
                client.getVfModuleCustomizationByModelCuztomizationUUID("cb82ffd8-252a-11e7-93ae-92361f002671");
        assertNotNull(vfModuleCustomization);
        assertNotNull(vfModuleCustomization.getModelCustomizationUUID());
        assertEquals("base", vfModuleCustomization.getLabel());
    }

    @Test
    public void testGetVfModuleCustomizationByModelCuztomizationUUIDNotFound() {
        VfModuleCustomization vfModuleCustomization =
                client.getVfModuleCustomizationByModelCuztomizationUUID(UUID.randomUUID().toString());
        assertNull(vfModuleCustomization);
    }

    @Test
    public void testGetNetworkResourceCustomizationByModelCustomizationUUID() {
        NetworkResourceCustomization networkResourceCustomization =
                client.getNetworkResourceCustomizationByModelCustomizationUUID("3bdbb104-476c-483e-9f8b-c095b3d308ac");
        assertNotNull(networkResourceCustomization);
        assertNotNull(networkResourceCustomization.getModelCustomizationUUID());
        assertEquals("CONTRAIL30_GNDIRECT 9", networkResourceCustomization.getModelInstanceName());
        assertNotNull(networkResourceCustomization.getNetworkResource());
    }

    @Test
    public void testGetNetworkResourceCustomizationByModelCustomizationUUIDNotFound() {
        NetworkResourceCustomization networkResourceCustomization =
                client.getNetworkResourceCustomizationByModelCustomizationUUID(UUID.randomUUID().toString());
        assertNull(networkResourceCustomization);
    }

    @Test
    public void testGgetVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID() {
        VfModuleCustomization vfModuleCustomization =
                client.getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID(
                        "cb82ffd8-252a-11e7-93ae-92361f002672", "20c4431c-246d-11e7-93ae-92361f002672");
        assertNotNull(vfModuleCustomization);
        assertNotNull(vfModuleCustomization.getModelCustomizationUUID());
        assertNotNull(vfModuleCustomization.getVfModule());
        assertEquals("base", vfModuleCustomization.getLabel());
    }

    @Test
    public void testGgetVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUIDNotFound() {
        VfModuleCustomization vfModuleCustomization =
                client.getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID(
                        "cb82ffd8-252a-11e7-93ae-92361f002672", UUID.randomUUID().toString());
        assertNull(vfModuleCustomization);
    }

    @Test
    public void testGetFirstByServiceModelUUIDAndAction() {
        ServiceRecipe serviceRecipe =
                client.getFirstByServiceModelUUIDAndAction("4694a55f-58b3-4f17-92a5-796d6f5ffd0d", "createInstance");
        assertNotNull(serviceRecipe);
        assertNotNull(serviceRecipe.getServiceModelUUID());
        assertNotNull(serviceRecipe.getAction());
        assertEquals("/mso/async/services/CreateGenericALaCarteServiceInstance", serviceRecipe.getOrchestrationUri());
        assertEquals("MSOTADevInfra aLaCarte", serviceRecipe.getDescription());
    }

    @Test
    public void testGetFirstByServiceModelUUIDAndActionNotFound() {
        ServiceRecipe serviceRecipe = client.getFirstByServiceModelUUIDAndAction("5df8b6de-2083-11e7-93ae-92361f002671",
                UUID.randomUUID().toString());
        assertNull(serviceRecipe);
    }

    @Test
    public void testGetFirstVnfResourceByModelInvariantUUIDAndModelVersion() {
        VnfResource vnfResource = client
                .getFirstVnfResourceByModelInvariantUUIDAndModelVersion("2fff5b20-214b-11e7-93ae-92361f002671", "2.0");
        assertNotNull(vnfResource);
        assertNotNull(vnfResource.getModelInvariantId());
        assertNotNull(vnfResource.getModelVersion());
        assertNotNull(vnfResource.getHeatTemplates());
        assertEquals("vSAMP10a", vnfResource.getModelName());
    }

    @Test
    public void testGetFirstVnfResourceByModelInvariantUUIDAndModelVersionNotFound() {
        VnfResource vnfResource = client.getFirstVnfResourceByModelInvariantUUIDAndModelVersion(
                "2fff5b20-214b-11e7-93ae-92361f002671", UUID.randomUUID().toString());
        assertNull(vnfResource);
    }

    @Test
    public void testGetFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources() {
        VnfResource vnfr = new VnfResource();
        vnfr.setModelUUID("ff2ae348-214a-11e7-93ae-92361f002671");
        VnfResourceCustomization firstVnfResourceCustomizationByModelInstanceNameAndVnfResources =
                client.getFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources("vSAMP10a 1", vnfr);
        assertNotNull(firstVnfResourceCustomizationByModelInstanceNameAndVnfResources);
        assertEquals("vSAMP", firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getNfRole());
        assertEquals("vSAMP10a 1",
                firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getModelInstanceName());
        assertNotNull(firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getVnfResources());
        assertNotNull(firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getVfModuleCustomizations());
    }

    @Test
    public void testGetFirstVnfRecipeByNfRoleAndAction() {
        VnfRecipe vnfRecipe = client.getFirstVnfRecipeByNfRoleAndAction("GR-API-DEFAULT", "createInstance");
        assertNotNull(vnfRecipe);
        assertNotNull(vnfRecipe.getNfRole());
        assertNotNull(vnfRecipe.getAction());
        assertEquals("Gr api recipe to create vnf", vnfRecipe.getDescription());
        assertEquals("/mso/async/services/WorkflowActionBB", vnfRecipe.getOrchestrationUri());
    }

    @Test
    public void testGetFirstVnfRecipeByNfRoleAndActionNotFound() {
        VnfRecipe vnfRecipe = client.getFirstVnfRecipeByNfRoleAndAction(UUID.randomUUID().toString(), "createInstance");
        assertNull(vnfRecipe);
    }

    @Test
    public void testGetFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction() {
        VnfComponentsRecipe vnfComponentsRecipe =
                client.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(
                        "20c4431c-246d-11e7-93ae-92361f002671", "volumeGroup", "createInstance");
        assertNotNull(vnfComponentsRecipe);
        assertNotNull(vnfComponentsRecipe.getAction());
        assertNotNull(vnfComponentsRecipe.getVfModuleModelUUID());
        assertNotNull(vnfComponentsRecipe.getVnfComponentType());
        assertEquals("Gr api recipe to create volume-group", vnfComponentsRecipe.getDescription());
        assertEquals("/mso/async/services/WorkflowActionBB", vnfComponentsRecipe.getOrchestrationUri());

    }


    @Test
    public void testGetFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndActionNotFound() {
        VnfComponentsRecipe vnfComponentsRecipe =
                client.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(
                        UUID.randomUUID().toString(), "volumeGroup", "createInstance");
        assertNull(vnfComponentsRecipe);
    }

    @Test
    public void testGetFirstVnfComponentsRecipeByVnfComponentTypeAndAction() {
        VnfComponentsRecipe vnfComponentsRecipe =
                client.getFirstVnfComponentsRecipeByVnfComponentTypeAndAction("volumeGroup", "createInstance");
        assertNotNull(vnfComponentsRecipe);
        assertNotNull(vnfComponentsRecipe.getAction());
        assertNotNull(vnfComponentsRecipe.getVnfComponentType());
        assertEquals("VID_DEFAULT recipe t", vnfComponentsRecipe.getDescription());
        assertEquals("/mso/async/services/CreateVfModuleVolumeInfraV1", vnfComponentsRecipe.getOrchestrationUri());
    }

    @Test
    public void testGetServiceByModelVersionAndModelInvariantUUID() {
        Service service =
                client.getServiceByModelVersionAndModelInvariantUUID("2.0", "9647dfc4-2083-11e7-93ae-92361f002671");
        assertNotNull(service);
        assertNotNull(service.getModelVersion());
        assertNotNull(service.getModelInvariantUUID());
        assertEquals("MSOTADevInfra_vSAMP10a_Service", service.getModelName());
        assertEquals("NA", service.getServiceRole());
    }

    @Test
    public void testGetServiceByModelVersionAndModelInvariantUUIDNotFound() {
        Service service = client.getServiceByModelVersionAndModelInvariantUUID("2.0", UUID.randomUUID().toString());
        assertNull(service);
    }

    @Test
    public void testGetVfModuleByModelInvariantUUIDAndModelVersion() {
        VfModule vfModule =
                client.getVfModuleByModelInvariantUUIDAndModelVersion("78ca26d0-246d-11e7-93ae-92361f002671", "2");
        assertNotNull(vfModule);
        assertNotNull(vfModule.getModelVersion());
        assertNotNull(vfModule.getModelInvariantUUID());
        assertEquals("vSAMP10aDEV::base::module-0", vfModule.getModelName());
        assertEquals("vSAMP10a DEV Base", vfModule.getDescription());
    }

    @Test
    public void testGetVfModuleByModelInvariantUUIDAndModelVersionNotFound() {
        VfModule vfModule = client.getVfModuleByModelInvariantUUIDAndModelVersion(UUID.randomUUID().toString(), "2");
        assertNull(vfModule);
    }

    @Test
    public void testGetServiceByModelInvariantUUIDOrderByModelVersionDesc() {
        List<Service> serviceList =
                client.getServiceByModelInvariantUUIDOrderByModelVersionDesc("9647dfc4-2083-11e7-93ae-92361f002671");
        assertFalse(serviceList.isEmpty());
        assertEquals(2, serviceList.size());
        Service service = serviceList.get(0);
        assertEquals("2.0", service.getModelVersion());
    }

    @Test
    public void testGetServiceByModelInvariantUUIDOrderByModelVersionDescNotFound() {
        List<Service> serviceList =
                client.getServiceByModelInvariantUUIDOrderByModelVersionDesc(UUID.randomUUID().toString());
        assertTrue(serviceList.isEmpty());
    }

    @Test
    public void testGetVfModuleByModelInvariantUUIDOrderByModelVersionDesc() {
        List<VfModule> moduleList =
                client.getVfModuleByModelInvariantUUIDOrderByModelVersionDesc("78ca26d0-246d-11e7-93ae-92361f002671");
        assertFalse(moduleList.isEmpty());
        assertEquals(2, moduleList.size());
        VfModule module = moduleList.get(0);
        assertEquals("vSAMP10a DEV Base", module.getDescription());
    }

    @Test
    public void testCloudSiteClient() {
        CatalogDbClientPortChanger localClient = new CatalogDbClientPortChanger(
                "http://localhost:" + client.wiremockPort, msoAdaptersAuth, client.wiremockPort);
        CloudSite cloudSite = new CloudSite();
        cloudSite.setId("MTN6");
        cloudSite.setClli("TESTCLLI");
        cloudSite.setRegionId("regionId");
        cloudSite.setCloudVersion("VERSION");
        cloudSite.setPlatform("PLATFORM");

        CloudIdentity cloudIdentity = new CloudIdentity();
        cloudIdentity.setId("RANDOMID");
        cloudIdentity.setIdentityUrl("URL");
        cloudIdentity.setMsoId("MSO_ID");
        cloudIdentity.setMsoPass("MSO_PASS");
        cloudIdentity.setAdminTenant("ADMIN_TENANT");
        cloudIdentity.setMemberRole("ROLE");
        cloudIdentity.setIdentityServerType(ServerType.KEYSTONE);
        cloudIdentity.setIdentityAuthenticationType(AuthenticationType.RACKSPACE_APIKEY);
        cloudSite.setIdentityService(cloudIdentity);
        localClient.postCloudSite(cloudSite);
        CloudSite getCloudSite = this.client.getCloudSite("MTN6");
        assertNotNull(getCloudSite);
        assertNotNull(getCloudSite.getIdentityService());
        assertEquals("TESTCLLI", getCloudSite.getClli());
        assertEquals("regionId", getCloudSite.getRegionId());
        assertEquals("RANDOMID", getCloudSite.getIdentityServiceId());

        getCloudSite.setClli("clli2");
        getCloudSite.setRegionId("region2");

        CloudSite updatedCloudSite = this.client.updateCloudSite(getCloudSite);
        assertNotNull(updatedCloudSite);
        assertNotNull(updatedCloudSite.getIdentityService());
        assertEquals("clli2", updatedCloudSite.getClli());
        assertEquals("region2", updatedCloudSite.getRegionId());

        this.client.deleteCloudSite(getCloudSite.getId());
        getCloudSite = this.client.getCloudSite("MTN6");
        assertNull(getCloudSite);
    }

    @Test
    public void testGetHomingInstance() {
        HomingInstance homingInstance = client.getHomingInstance("5df8b6de-2083-11e7-93ae-92361f232671");
        assertNotNull(homingInstance);
        assertNotNull(homingInstance.getCloudOwner());
        assertNotNull(homingInstance.getCloudRegionId());
        assertNotNull(homingInstance.getOofDirectives());
    }

    @Test
    public void testPostHomingInstance() {
        CatalogDbClientPortChanger localClient = new CatalogDbClientPortChanger(
                "http://localhost:" + client.wiremockPort, msoAdaptersAuth, client.wiremockPort);
        HomingInstance homingInstance = new HomingInstance();
        homingInstance.setServiceInstanceId("5df8d6be-2083-11e7-93ae-92361f232671");
        homingInstance.setCloudOwner("CloudOwner-1");
        homingInstance.setCloudRegionId("CloudRegionOne");
        homingInstance.setOofDirectives("{\n" + "\"directives\": [\n" + "{\n" + "\"directives\": [\n" + "{\n"
                + "\"attributes\": [\n" + "{\n" + "\"attribute_value\": \"onap.hpa.flavor31\",\n"
                + "\"attribute_name\": \"firewall_flavor_name\"\n" + "}\n" + "],\n"
                + "\"type\": \"flavor_directives\"\n" + "}\n" + "],\n" + "\"type\": \"vnfc\",\n" + "\"id\": \"vfw\"\n"
                + "},\n" + "{\n" + "\"directives\": [\n" + "{\n" + "\"attributes\": [\n" + "{\n"
                + "\"attribute_value\": \"onap.hpa.flavor32\",\n" + "\"attribute_name\": \"packetgen_flavor_name\"\n"
                + "}\n" + "],\n" + "\"type\": \"flavor_directives\"\n" + "}\n" + "],\n" + "\"type\": \"vnfc\",\n"
                + "\"id\": \"vgenerator\"\n" + "},\n" + "{\n" + "\"directives\": [\n" + "{\n" + "\"attributes\": [\n"
                + "{\n" + "\"attribute_value\": \"onap.hpa.flavor31\",\n" + "\"attribute_name\": \"sink_flavor_name\"\n"
                + "}\n" + "],\n" + "\"type\": \"flavor_directives\"\n" + "}\n" + "],\n" + "\"type\": \"vnfc\",\n"
                + "\"id\": \"vsink\"\n" + "}\n" + "]\n" + "}");
        localClient.postHomingInstance(homingInstance);
        HomingInstance getHomingInstance = this.client.getHomingInstance("5df8d6be-2083-11e7-93ae-92361f232671");
        assertNotNull(getHomingInstance);
        assertNotNull(getHomingInstance.getCloudRegionId());
        assertNotNull(getHomingInstance.getCloudOwner());
        assertEquals("CloudOwner-1", getHomingInstance.getCloudOwner());
        assertEquals("CloudRegionOne", getHomingInstance.getCloudRegionId());
    }

    @Test
    public void testGetServiceByModelName() {
        Service service = client.getServiceByModelName("MSOTADevInfra_Test_Service");
        assertNotNull(service);
        assertNotNull(service.getModelVersion());
        assertNotNull(service.getModelInvariantUUID());
        assertEquals("MSOTADevInfra_Test_Service", service.getModelName());
        assertEquals("NA", service.getServiceRole());
    }

    @Test
    public void testGetServiceByModelNameNotFound() {
        Service service = client.getServiceByModelName("Not_Found");
        assertNull(service);
    }

    @Test
    public void testGetServiceByModelUUID() {
        Service service = client.getServiceByModelUUID("5df8b6de-2083-11e7-93ae-92361f002679");
        assertNotNull(service);
        assertNotNull(service.getModelVersion());
        assertNotNull(service.getModelInvariantUUID());
        assertEquals("5df8b6de-2083-11e7-93ae-92361f002679", service.getModelUUID());
        assertEquals("NA", service.getServiceRole());
    }

    @Test
    public void testGetServiceByModelUUIDNotFound() {
        Service service = client.getServiceByModelUUID("Not_Found");
        assertNull(service);
    }

    @Test
    public void testGetNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner() {
        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setAction("createService");
        northBoundRequest.setRequestScope("service");
        northBoundRequest.setIsAlacarte(true);
        northBoundRequest.setCloudOwner("my-custom-cloud-owner");
        client.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner("createService", "service", true,
                "my-custom-cloud-owner");
        assertNotNull(northBoundRequest);
        assertEquals("createService", northBoundRequest.getAction());
        assertEquals("service", northBoundRequest.getRequestScope());
        assertEquals(true, northBoundRequest.getIsAlacarte());
        assertEquals("my-custom-cloud-owner", northBoundRequest.getCloudOwner());
    }

    @Test
    public void testFindServiceRecipeByActionAndServiceModelUUID() {
        ServiceRecipe serviceRecipe = client.findServiceRecipeByActionAndServiceModelUUID("createInstance",
                "4694a55f-58b3-4f17-92a5-796d6f5ffd0d");
        assertNotNull(serviceRecipe);
        assertNotNull(serviceRecipe.getServiceModelUUID());
        assertNotNull(serviceRecipe.getAction());
        assertEquals("/mso/async/services/CreateGenericALaCarteServiceInstance", serviceRecipe.getOrchestrationUri());
        assertEquals("MSOTADevInfra aLaCarte", serviceRecipe.getDescription());
    }

    @Test
    public void testFindServiceRecipeByActionAndServiceModelUUIDNotFound() {
        ServiceRecipe serviceRecipe =
                client.findServiceRecipeByActionAndServiceModelUUID("not_found", "5df8b6de-2083-11e7-93ae-test");
        assertNull(serviceRecipe);
    }

    @Test
    public void testFindExternalToInternalServiceByServiceName() {
        ExternalServiceToInternalService externalServiceToInternalService =
                client.findExternalToInternalServiceByServiceName("MySpecialServiceName");
        assertNotNull(externalServiceToInternalService);
        assertNotNull(externalServiceToInternalService.getServiceName());
        assertNotNull(externalServiceToInternalService.getServiceModelUUID());
        assertEquals("MySpecialServiceName", externalServiceToInternalService.getServiceName());
    }

    @Test
    public void testFindExternalToInternalServiceByServiceNameNotFound() {
        ExternalServiceToInternalService externalServiceToInternalService =
                client.findExternalToInternalServiceByServiceName("Not_Found");
        assertNull(externalServiceToInternalService);
    }

    @Test
    public void getPnfResourceByModelUUID_validUuid_expectedOutput() {
        PnfResource pnfResource = client.getPnfResourceByModelUUID("ff2ae348-214a-11e7-93ae-92361f002680");
        assertNotNull(pnfResource);
        assertEquals("PNFResource modelUUID", "ff2ae348-214a-11e7-93ae-92361f002680", pnfResource.getModelUUID());
        assertEquals("PNFResource modelInvariantUUID", "2fff5b20-214b-11e7-93ae-92361f002680",
                pnfResource.getModelInvariantUUID());
        assertEquals("PNFResource modelVersion", "1.0", pnfResource.getModelVersion());
        assertEquals("PNFResource orchestration mode", "", pnfResource.getOrchestrationMode());
    }

    @Test
    public void getPnfResourceByModelUUID_invalidUuid_NullOutput() {
        PnfResource pnfResource = client.getPnfResourceByModelUUID(UUID.randomUUID().toString());
        assertNull(pnfResource);
    }

    @Test
    public void getPnfResourceCustomizationByModelCustomizationUUID_validUuid_expectedOutput() {
        PnfResourceCustomization pnfResourceCustomization =
                client.getPnfResourceCustomizationByModelCustomizationUUID("68dc9a92-214c-11e7-93ae-92361f002680");
        assertEquals("modelInstanceName", "PNF routing", pnfResourceCustomization.getModelInstanceName());
        assertEquals("blueprintName", "test_configuration_restconf", pnfResourceCustomization.getBlueprintName());
        assertEquals("blueprintVersion", "1.0.0", pnfResourceCustomization.getBlueprintVersion());
        assertTrue("skip post instantiation configuration", pnfResourceCustomization.getSkipPostInstConf());
        PnfResource pnfResource = pnfResourceCustomization.getPnfResources();
        assertNotNull(pnfResource);
        assertEquals("PNFResource modelUUID", "ff2ae348-214a-11e7-93ae-92361f002680", pnfResource.getModelUUID());
        assertEquals("PNFResource modelInvariantUUID", "2fff5b20-214b-11e7-93ae-92361f002680",
                pnfResource.getModelInvariantUUID());
        assertEquals("PNFResource modelVersion", "1.0", pnfResource.getModelVersion());
        assertEquals("PNFResource orchestration mode", "", pnfResource.getOrchestrationMode());
    }

    @Test
    public void getPnfResourceCustomizationByModelCustomizationUUID_invalidUuid_nullOutput() {
        PnfResourceCustomization pnfResourceCustomization =
                client.getPnfResourceCustomizationByModelCustomizationUUID(UUID.randomUUID().toString());
        assertNull(pnfResourceCustomization);
    }

    @Test
    public void getPnfResourceCustomizationFromJoinTable_validServiceUuid_expectedOutput() {
        List<PnfResourceCustomization> pnfResourceCustomizationList =
                client.getPnfResourceCustomizationByModelUuid("5df8b6de-2083-11e7-93ae-92361f002676");
        assertEquals(1, pnfResourceCustomizationList.size());

        PnfResourceCustomization pnfResourceCustomization = pnfResourceCustomizationList.get(0);
        assertEquals("modelInstanceName", "PNF routing", pnfResourceCustomization.getModelInstanceName());
        assertEquals("blueprintName", "test_configuration_restconf", pnfResourceCustomization.getBlueprintName());
        assertEquals("blueprintVersion", "1.0.0", pnfResourceCustomization.getBlueprintVersion());
        PnfResource pnfResource = pnfResourceCustomization.getPnfResources();
        assertNotNull(pnfResource);

        assertEquals("PNFResource modelUUID", "ff2ae348-214a-11e7-93ae-92361f002680", pnfResource.getModelUUID());
        assertEquals("PNFResource modelInvariantUUID", "2fff5b20-214b-11e7-93ae-92361f002680",
                pnfResource.getModelInvariantUUID());
        assertEquals("PNFResource modelVersion", "1.0", pnfResource.getModelVersion());
        assertEquals("PNFResource orchestration mode", "", pnfResource.getOrchestrationMode());
    }

    @Test
    public void getPnfResourceCustomizationFromJoinTable_invalidServiceUuid_nullOutput() {
        List<PnfResourceCustomization> pnfResourceCustomizationList =
                client.getPnfResourceCustomizationByModelUuid(UUID.randomUUID().toString());
        assertEquals(0, pnfResourceCustomizationList.size());
    }

    @Test
    public void testGetServiceTopologyById() throws Exception {
        org.onap.so.rest.catalog.beans.Service serviceByID =
                client.getServiceModelInformation("5df8b6de-2083-11e7-93ae-92361f002671", "2");
        assertNotNull(serviceByID);
        assertEquals("MSOTADevInfra_vSAMP10a_Service", serviceByID.getModelName());
        assertEquals("NA", serviceByID.getServiceType());
        assertEquals("NA", serviceByID.getServiceRole());
    }

    @Test
    public void testGetServices() throws Exception {
        List<org.onap.so.rest.catalog.beans.Service> services = client.getServices();
        assertEquals(false, services.isEmpty());
    }

    @Test
    public void testVnf() throws Exception {
        org.onap.so.rest.catalog.beans.Vnf vnf = client.getVnfModelInformation("5df8b6de-2083-11e7-93ae-92361f002671",
                "68dc9a92-214c-11e7-93ae-92361f002671", "0");
        assertNotNull(vnf);
        assertEquals("vSAMP10a", vnf.getModelName());
        assertEquals(false, vnf.getNfDataValid());
        assertEquals("vSAMP", vnf.getNfFunction());
        assertEquals("vSAMP", vnf.getNfNamingCode());
        assertEquals("vSAMP", vnf.getNfRole());
        assertEquals("vSAMP", vnf.getNfType());

        vnf.setNfDataValid(true);
        vnf.setNfFunction("nfFunction");
        vnf.setNfRole("nfRole");
        vnf.setNfType("nfType");
        vnf.setNfNamingCode("nfNamingCode");

        client.updateVnf("5df8b6de-2083-11e7-93ae-92361f002671", vnf);
        vnf = client.getVnfModelInformation("5df8b6de-2083-11e7-93ae-92361f002671",
                "68dc9a92-214c-11e7-93ae-92361f002671", "0");
        assertEquals("vSAMP10a", vnf.getModelName());
        assertEquals(true, vnf.getNfDataValid());
        assertEquals("nfFunction", vnf.getNfFunction());
        assertEquals("nfNamingCode", vnf.getNfNamingCode());
        assertEquals("nfRole", vnf.getNfRole());
        assertEquals("nfType", vnf.getNfType());


    }

    @Test
    public void getWorkflowByArtifactUUID_validUuid_expectedOutput() {
        Workflow workflow = client.findWorkflowByArtifactUUID("5b0c4322-643d-4c9f-b184-4516049e99b1");
        assertEquals("artifactName", "testingWorkflow.bpmn", workflow.getArtifactName());
    }

    @Test
    public void getWorkflowByArtifactUUID_invalidUuid_nullOutput() {
        Workflow workflow = client.findWorkflowByArtifactUUID(UUID.randomUUID().toString());
        assertNull(workflow);
    }

    @Test
    public void getWorkflowByVnfModelUUID_validUuid_expectedOutput() {
        List<Workflow> workflows = client.findWorkflowByVnfModelUUID("ff2ae348-214a-11e7-93ae-92361f002671");
        assertTrue(workflows != null);
        assertTrue(workflows.size() != 0);

        assertEquals("testingWorkflow.bpmn", workflows.get(0).getArtifactName());
    }

    @Test
    public void getWorkflowByModelUUID_invalidUuid_nullOutput() {
        Workflow workflow = client.findWorkflowByArtifactUUID(UUID.randomUUID().toString());
        assertNull(workflow);
    }

    @Test
    public void getWorkflowBySource_validSource_expectedOutput() {
        List<Workflow> workflows = client.findWorkflowBySource("sdc");
        assertTrue(workflows != null);
        assertTrue(workflows.size() != 0);

        assertEquals("testingWorkflow.bpmn", workflows.get(0).getArtifactName());
    }

    @Test
    public void getWorkflowBySource_invalidSource_nullOutput() {
        List<Workflow> workflow = client.findWorkflowBySource("abc");
        assertNull(workflow);
    }

    @Test
    public void getCloudSites() {
        List<CloudSite> cloudSites = client.getCloudSites();
        assertNotNull(cloudSites);
        assertNotEquals(0, cloudSites.size());
    }

    @Test
    public void getBBNameSelectionReference_validData_expectedOutput() {
        BBNameSelectionReference bbNameSelectionReference =
                client.getBBNameSelectionReference("APPC", "vfModule", "healthCheck");
        assertNotNull(bbNameSelectionReference);
        assertEquals("GenericVnfHealthCheckBB", bbNameSelectionReference.getBbName());
    }

    @Test
    public void getBBNameSelectionReference_invalidData_nullOutput() {
        BBNameSelectionReference bbNameSelectionReference =
                client.getBBNameSelectionReference("ABC", "vfModule", "healthCheck");
        assertNull(bbNameSelectionReference);

    }

    @Test
    public void testGetProcessingFlagsFromFlag() {
        ProcessingFlags processingFlags = client.findProcessingFlagsByFlag("TESTFLAG");
        assertNotNull(processingFlags);
        assertEquals(processingFlags.getEndpoint(), "TESTENDPOINT");
    }

    @Test
    public void testGetBuildingBlocksList() {
        List<BuildingBlockRollback> rollbackEntries = client.getBuildingBlockRollbackEntries();
        assertTrue(rollbackEntries.size() > 1);
    }


}
