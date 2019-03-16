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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.catalogdb.CatalogDBApplication;
import org.onap.so.db.catalog.beans.AuthenticationType;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.db.catalog.beans.ExternalServiceToInternalService;
import org.onap.so.db.catalog.beans.HomingInstance;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.PnfResource;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.ServerType;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfComponentsRecipe;
import org.onap.so.db.catalog.beans.VnfRecipe;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.ExternalServiceToInternalService;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.parameters.P;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CatalogDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CatalogDbClientTest {

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
    }

    @Test
    public void testGetRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep() {
        RainyDayHandlerStatus rainyDayHandlerStatus = client
            .getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(
                "AssignServiceInstanceBB", "*", "*", "*", "*");
        Assert.assertEquals("Rollback", rainyDayHandlerStatus.getPolicy());
    }

    @Test
    public void testGetRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStepRecordNotFound() {
        RainyDayHandlerStatus rainyDayHandlerStatus = client
            .getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(
                UUID.randomUUID().toString(), "*", "*", "*", "*");
        Assert.assertNull(rainyDayHandlerStatus);
    }

    @Test
    public void testGetCloudSiteHappyPath() throws Exception {
        CloudSite cloudSite = client.getCloudSite(MTN13);
        Assert.assertNotNull(cloudSite);
        Assert.assertNotNull(cloudSite.getIdentityService());
        Assert.assertEquals("MDT13", cloudSite.getClli());
        Assert.assertEquals("mtn13", cloudSite.getRegionId());
        Assert.assertEquals("MTN13", cloudSite.getIdentityServiceId());
    }

    @Test
    public void testGetCloudSiteNotFound() throws Exception {
        CloudSite cloudSite = client.getCloudSite(UUID.randomUUID().toString());
        Assert.assertNull(cloudSite);
    }

    @Test
    public void testGetCloudifyManagerHappyPath() throws Exception {
        CloudifyManager cloudifyManager = client.getCloudifyManager("mtn13");
        Assert.assertNotNull(cloudifyManager);
        Assert.assertEquals("http://localhost:28090/v2.0", cloudifyManager.getCloudifyUrl());

    }

    @Test
    public void testGetCloudifyManagerNotFound() throws Exception {
        CloudifyManager cloudifyManager = client.getCloudifyManager(UUID.randomUUID().toString());
        Assert.assertNull(cloudifyManager);
    }


    @Test
    public void testGetCloudSiteByClliAndAicVersionHappyPath() throws Exception {
        CloudSite cloudSite = client.getCloudSiteByClliAndAicVersion("MDT13", "2.5");
        Assert.assertNotNull(cloudSite);
    }

    @Test
    public void testGetCloudSiteByClliAndAicVersionNotFound() throws Exception {
        CloudSite cloudSite = client.getCloudSiteByClliAndAicVersion("MDT13", "232496239746328");
        Assert.assertNull(cloudSite);
    }

    @Test
    public void testGetServiceByID() throws Exception {
        Service serviceByID = client.getServiceByID("5df8b6de-2083-11e7-93ae-92361f002671");
        Assert.assertNotNull(serviceByID);
        Assert.assertEquals("MSOTADevInfra_vSAMP10a_Service", serviceByID.getModelName());
        Assert.assertEquals("NA", serviceByID.getServiceType());
        Assert.assertEquals("NA", serviceByID.getServiceRole());
    }

    @Test
    public void testGetServiceByIDNotFound() throws Exception {
        Service serviceByID = client.getServiceByID(UUID.randomUUID().toString());
        Assert.assertNull(serviceByID);
    }

    @Test
    public void testGetVfModuleByModelUUID() throws Exception {
        VfModule vfModule = client.getVfModuleByModelUUID("20c4431c-246d-11e7-93ae-92361f002671");
        Assert.assertNotNull(vfModule);
        Assert.assertNotNull(vfModule.getVfModuleCustomization());
        Assert.assertEquals("78ca26d0-246d-11e7-93ae-92361f002671", vfModule.getModelInvariantUUID());
        Assert.assertEquals("vSAMP10aDEV::base::module-0", vfModule.getModelName());
    }

    @Test
    public void testGetVfModuleByModelUUIDNotFound() throws Exception {
        VfModule vfModule = client.getVfModuleByModelUUID(UUID.randomUUID().toString());
        Assert.assertNull(vfModule);
    }

    @Test
    public void testGetVnfResourceByModelUUID() throws Exception {
        VnfResource vnfResource = client.getVnfResourceByModelUUID("ff2ae348-214a-11e7-93ae-92361f002671");
        Assert.assertNotNull(vnfResource);
        Assert.assertEquals("vSAMP10a", vnfResource.getModelName());
    }

    @Test
    public void testGetVnfResourceByModelUUIDNotFound() throws Exception {
        VnfResource vnfResource = client.getVnfResourceByModelUUID(UUID.randomUUID().toString());
        Assert.assertNull(vnfResource);
    }

    @Test
    public void testGetVnfResourceCustomizationByModelCustomizationUUID() {
        VnfResourceCustomization vnfResourceCustomization = client
            .getVnfResourceCustomizationByModelCustomizationUUID("68dc9a92-214c-11e7-93ae-92361f002671");
        Assert.assertNotNull(vnfResourceCustomization);
        Assert.assertEquals("vSAMP", vnfResourceCustomization.getNfRole());
        Assert.assertNotNull(vnfResourceCustomization.getModelCustomizationUUID());
        Assert.assertNotNull(vnfResourceCustomization.getVnfResources());
        Assert.assertNotNull(vnfResourceCustomization.getVfModuleCustomizations());
        Assert.assertEquals("vSAMP10a", vnfResourceCustomization.getVnfResources().getModelName());
    }

    @Test
    public void getVnfResourceCustomizationFromJoinTable_serviceUuid_expectedOutput() {
        List<VnfResourceCustomization> vnfResourceCustomizationList = client
            .getVnfResourceCustomizationFromJoinTable("5df8b6de-2083-11e7-93ae-92361f002671");
        assertEquals(1, vnfResourceCustomizationList.size());
        VnfResourceCustomization vnfResourceCustomization = vnfResourceCustomizationList.get(0);
        Assert.assertNotNull(vnfResourceCustomization);
        Assert.assertEquals("vSAMP", vnfResourceCustomization.getNfRole());
        Assert.assertNotNull(vnfResourceCustomization.getModelCustomizationUUID());
        Assert.assertNotNull(vnfResourceCustomization.getVnfResources());
        Assert.assertNotNull(vnfResourceCustomization.getVfModuleCustomizations());
        Assert.assertEquals("vSAMP10a", vnfResourceCustomization.getVnfResources().getModelName());
    }

    @Test
    public void getVnfResourceCustomizationFromJoinTable_invalidServiceUuid_nullOutput() {
        List<VnfResourceCustomization> vnfResourceCustomizationList = client
            .getVnfResourceCustomizationFromJoinTable(UUID.randomUUID().toString());
        assertEquals(0, vnfResourceCustomizationList.size());
    }

    @Test
    public void testGetVnfResourceCustomizationByModelCustomizationUUINotFound() {
        VnfResourceCustomization vnfResourceCustomization = client
            .getVnfResourceCustomizationByModelCustomizationUUID(UUID.randomUUID().toString());
        Assert.assertNull(vnfResourceCustomization);
    }

    @Test
    public void testGetInstanceGroupByModelUUID() {
        InstanceGroup instanceGroup = client.getInstanceGroupByModelUUID("0c8692ef-b9c0-435d-a738-edf31e71f38b");
        Assert.assertNotNull(instanceGroup);
        Assert.assertEquals("network_collection_resource_1806..NetworkCollection..0", instanceGroup.getModelName());
        Assert.assertEquals("org.openecomp.resource.cr.NetworkCollectionResource1806",
            instanceGroup.getToscaNodeType().toString());
    }

    @Test
    public void testGetVfModuleCustomizationByModelCuztomizationUUID() {
        VfModuleCustomization vfModuleCustomization = client
            .getVfModuleCustomizationByModelCuztomizationUUID("cb82ffd8-252a-11e7-93ae-92361f002671");
        Assert.assertNotNull(vfModuleCustomization);
        Assert.assertNotNull(vfModuleCustomization.getModelCustomizationUUID());
        Assert.assertEquals("base", vfModuleCustomization.getLabel());
    }

    @Test
    public void testGetVfModuleCustomizationByModelCuztomizationUUIDNotFound() {
        VfModuleCustomization vfModuleCustomization = client
            .getVfModuleCustomizationByModelCuztomizationUUID(UUID.randomUUID().toString());
        Assert.assertNull(vfModuleCustomization);
    }

    @Test
    public void testGetNetworkResourceCustomizationByModelCustomizationUUID() {
        NetworkResourceCustomization networkResourceCustomization = client
            .getNetworkResourceCustomizationByModelCustomizationUUID("3bdbb104-476c-483e-9f8b-c095b3d308ac");
        Assert.assertNotNull(networkResourceCustomization);
        Assert.assertNotNull(networkResourceCustomization.getModelCustomizationUUID());
        Assert.assertEquals("CONTRAIL30_GNDIRECT 9", networkResourceCustomization.getModelInstanceName());
        Assert.assertNotNull(networkResourceCustomization.getNetworkResource());
    }

    @Test
    public void testGetNetworkResourceCustomizationByModelCustomizationUUIDNotFound() {
        NetworkResourceCustomization networkResourceCustomization = client
            .getNetworkResourceCustomizationByModelCustomizationUUID(UUID.randomUUID().toString());
        Assert.assertNull(networkResourceCustomization);
    }

    @Test
    public void testGgetVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID() {
        VfModuleCustomization vfModuleCustomization = client
            .getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID(
                "cb82ffd8-252a-11e7-93ae-92361f002672", "20c4431c-246d-11e7-93ae-92361f002672");
        Assert.assertNotNull(vfModuleCustomization);
        Assert.assertNotNull(vfModuleCustomization.getModelCustomizationUUID());
        Assert.assertNotNull(vfModuleCustomization.getVfModule());
        Assert.assertEquals("base", vfModuleCustomization.getLabel());
    }

    @Test
    public void testGgetVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUIDNotFound() {
        VfModuleCustomization vfModuleCustomization = client
            .getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID(
                "cb82ffd8-252a-11e7-93ae-92361f002672", UUID.randomUUID().toString());
        Assert.assertNull(vfModuleCustomization);
    }

    @Test
    public void testGetFirstByServiceModelUUIDAndAction() {
        ServiceRecipe serviceRecipe = client
            .getFirstByServiceModelUUIDAndAction("4694a55f-58b3-4f17-92a5-796d6f5ffd0d", "createInstance");
        Assert.assertNotNull(serviceRecipe);
        Assert.assertNotNull(serviceRecipe.getServiceModelUUID());
        Assert.assertNotNull(serviceRecipe.getAction());
        Assert.assertEquals("/mso/async/services/CreateGenericALaCarteServiceInstance",
            serviceRecipe.getOrchestrationUri());
        Assert.assertEquals("MSOTADevInfra aLaCarte", serviceRecipe.getDescription());
    }

    @Test
    public void testGetFirstByServiceModelUUIDAndActionNotFound() {
        ServiceRecipe serviceRecipe = client
            .getFirstByServiceModelUUIDAndAction("5df8b6de-2083-11e7-93ae-92361f002671", UUID.randomUUID().toString());
        Assert.assertNull(serviceRecipe);
    }

    @Test
    public void testGetFirstVnfResourceByModelInvariantUUIDAndModelVersion() {
        VnfResource vnfResource = client
            .getFirstVnfResourceByModelInvariantUUIDAndModelVersion("2fff5b20-214b-11e7-93ae-92361f002671", "2.0");
        Assert.assertNotNull(vnfResource);
        Assert.assertNotNull(vnfResource.getModelInvariantId());
        Assert.assertNotNull(vnfResource.getModelVersion());
        Assert.assertNotNull(vnfResource.getHeatTemplates());
        Assert.assertNotNull(vnfResource.getVnfResourceCustomizations());
        Assert.assertEquals("vSAMP10a", vnfResource.getModelName());
    }

    @Test
    public void testGetFirstVnfResourceByModelInvariantUUIDAndModelVersionNotFound() {
        VnfResource vnfResource = client
            .getFirstVnfResourceByModelInvariantUUIDAndModelVersion("2fff5b20-214b-11e7-93ae-92361f002671",
                UUID.randomUUID().toString());
        Assert.assertNull(vnfResource);
    }

    @Test
    public void testGetFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources() {
        VnfResource vnfr = new VnfResource();
        vnfr.setModelUUID("ff2ae348-214a-11e7-93ae-92361f002671");
        VnfResourceCustomization firstVnfResourceCustomizationByModelInstanceNameAndVnfResources = client
            .getFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources("vSAMP10a 1", vnfr);
        Assert.assertNotNull(firstVnfResourceCustomizationByModelInstanceNameAndVnfResources);
        Assert.assertEquals("vSAMP", firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getNfRole());
        Assert.assertEquals("vSAMP10a 1",
            firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getModelInstanceName());
        Assert.assertNotNull(firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getVnfResources());
        Assert
            .assertNotNull(firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getVfModuleCustomizations());
    }

    @Test
    public void testGetFirstVnfRecipeByNfRoleAndAction() {
        VnfRecipe vnfRecipe = client.getFirstVnfRecipeByNfRoleAndAction("GR-API-DEFAULT", "createInstance");
        Assert.assertNotNull(vnfRecipe);
        Assert.assertNotNull(vnfRecipe.getNfRole());
        Assert.assertNotNull(vnfRecipe.getAction());
        Assert.assertEquals("Gr api recipe to create vnf", vnfRecipe.getDescription());
        Assert.assertEquals("/mso/async/services/WorkflowActionBB", vnfRecipe.getOrchestrationUri());
    }

    @Test
    public void testGetFirstVnfRecipeByNfRoleAndActionNotFound() {
        VnfRecipe vnfRecipe = client.getFirstVnfRecipeByNfRoleAndAction(UUID.randomUUID().toString(), "createInstance");
        Assert.assertNull(vnfRecipe);
    }

    @Test
    public void testGetFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction() {
        VnfComponentsRecipe vnfComponentsRecipe = client
            .getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(
                "20c4431c-246d-11e7-93ae-92361f002671", "volumeGroup", "createInstance");
        Assert.assertNotNull(vnfComponentsRecipe);
        Assert.assertNotNull(vnfComponentsRecipe.getAction());
        Assert.assertNotNull(vnfComponentsRecipe.getVfModuleModelUUID());
        Assert.assertNotNull(vnfComponentsRecipe.getVnfComponentType());
        Assert.assertEquals("Gr api recipe to create volume-group", vnfComponentsRecipe.getDescription());
        Assert.assertEquals("/mso/async/services/WorkflowActionBB", vnfComponentsRecipe.getOrchestrationUri());

    }


    @Test
    public void testGetFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndActionNotFound() {
        VnfComponentsRecipe vnfComponentsRecipe = client
            .getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(UUID.randomUUID().toString(),
                "volumeGroup", "createInstance");
        Assert.assertNull(vnfComponentsRecipe);
    }

    @Test
    public void testGetFirstVnfComponentsRecipeByVnfComponentTypeAndAction() {
        VnfComponentsRecipe vnfComponentsRecipe = client
            .getFirstVnfComponentsRecipeByVnfComponentTypeAndAction("volumeGroup", "createInstance");
        Assert.assertNotNull(vnfComponentsRecipe);
        Assert.assertNotNull(vnfComponentsRecipe.getAction());
        Assert.assertNotNull(vnfComponentsRecipe.getVnfComponentType());
        Assert.assertEquals("VID_DEFAULT recipe t", vnfComponentsRecipe.getDescription());
        Assert
            .assertEquals("/mso/async/services/CreateVfModuleVolumeInfraV1", vnfComponentsRecipe.getOrchestrationUri());
    }

    @Test
    public void testGetServiceByModelVersionAndModelInvariantUUID() {
        Service service = client
            .getServiceByModelVersionAndModelInvariantUUID("2.0", "9647dfc4-2083-11e7-93ae-92361f002671");
        Assert.assertNotNull(service);
        Assert.assertNotNull(service.getModelVersion());
        Assert.assertNotNull(service.getModelInvariantUUID());
        Assert.assertEquals("MSOTADevInfra_vSAMP10a_Service", service.getModelName());
        Assert.assertEquals("NA", service.getServiceRole());
    }

    @Test
    public void testGetServiceByModelVersionAndModelInvariantUUIDNotFound() {
        Service service = client.getServiceByModelVersionAndModelInvariantUUID("2.0", UUID.randomUUID().toString());
        Assert.assertNull(service);
    }

    @Test
    public void testGetVfModuleByModelInvariantUUIDAndModelVersion() {
        VfModule vfModule = client
            .getVfModuleByModelInvariantUUIDAndModelVersion("78ca26d0-246d-11e7-93ae-92361f002671", "2");
        Assert.assertNotNull(vfModule);
        Assert.assertNotNull(vfModule.getModelVersion());
        Assert.assertNotNull(vfModule.getModelInvariantUUID());
        Assert.assertEquals("vSAMP10aDEV::base::module-0", vfModule.getModelName());
        Assert.assertEquals("vSAMP10a DEV Base", vfModule.getDescription());
    }

    @Test
    public void testGetVfModuleByModelInvariantUUIDAndModelVersionNotFound() {
        VfModule vfModule = client.getVfModuleByModelInvariantUUIDAndModelVersion(UUID.randomUUID().toString(), "2");
        Assert.assertNull(vfModule);
    }

    @Test
    public void testGetServiceByModelInvariantUUIDOrderByModelVersionDesc() {
        List<Service> serviceList = client
            .getServiceByModelInvariantUUIDOrderByModelVersionDesc("9647dfc4-2083-11e7-93ae-92361f002671");
        Assert.assertFalse(serviceList.isEmpty());
        Assert.assertEquals(2, serviceList.size());
        Service service = serviceList.get(0);
        Assert.assertEquals("2.0", service.getModelVersion());
    }

    @Test
    public void testGetServiceByModelInvariantUUIDOrderByModelVersionDescNotFound() {
        List<Service> serviceList = client
            .getServiceByModelInvariantUUIDOrderByModelVersionDesc(UUID.randomUUID().toString());
        Assert.assertTrue(serviceList.isEmpty());
    }

    @Test
    public void testGetVfModuleByModelInvariantUUIDOrderByModelVersionDesc() {
        List<VfModule> moduleList = client
            .getVfModuleByModelInvariantUUIDOrderByModelVersionDesc("78ca26d0-246d-11e7-93ae-92361f002671");
        Assert.assertFalse(moduleList.isEmpty());
        Assert.assertEquals(2, moduleList.size());
        VfModule module = moduleList.get(0);
        Assert.assertEquals("vSAMP10a DEV Base", module.getDescription());
    }

    @Test
    public void testPostCloudSite() {
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
        Assert.assertNotNull(getCloudSite);
        Assert.assertNotNull(getCloudSite.getIdentityService());
        Assert.assertEquals("TESTCLLI", getCloudSite.getClli());
        Assert.assertEquals("regionId", getCloudSite.getRegionId());
        Assert.assertEquals("RANDOMID", getCloudSite.getIdentityServiceId());
    }

    @Test
    public void testGetHomingInstance() {
        HomingInstance homingInstance = client.getHomingInstance("5df8b6de-2083-11e7-93ae-92361f232671");
        Assert.assertNotNull(homingInstance);
        Assert.assertNotNull(homingInstance.getCloudOwner());
        Assert.assertNotNull(homingInstance.getCloudRegionId());
        Assert.assertNotNull(homingInstance.getOofDirectives());
    }

    @Test
    public void testPostHomingInstance() {
        CatalogDbClientPortChanger localClient = new CatalogDbClientPortChanger(
            "http://localhost:" + client.wiremockPort, msoAdaptersAuth, client.wiremockPort);
        HomingInstance homingInstance = new HomingInstance();
        homingInstance.setServiceInstanceId("5df8d6be-2083-11e7-93ae-92361f232671");
        homingInstance.setCloudOwner("CloudOwner-1");
        homingInstance.setCloudRegionId("CloudRegionOne");
        homingInstance.setOofDirectives("{\n" +
            "\"directives\": [\n" +
            "{\n" +
            "\"directives\": [\n" +
            "{\n" +
            "\"attributes\": [\n" +
            "{\n" +
            "\"attribute_value\": \"onap.hpa.flavor31\",\n" +
            "\"attribute_name\": \"firewall_flavor_name\"\n" +
            "}\n" +
            "],\n" +
            "\"type\": \"flavor_directives\"\n" +
            "}\n" +
            "],\n" +
            "\"type\": \"vnfc\",\n" +
            "\"id\": \"vfw\"\n" +
            "},\n" +
            "{\n" +
            "\"directives\": [\n" +
            "{\n" +
            "\"attributes\": [\n" +
            "{\n" +
            "\"attribute_value\": \"onap.hpa.flavor32\",\n" +
            "\"attribute_name\": \"packetgen_flavor_name\"\n" +
            "}\n" +
            "],\n" +
            "\"type\": \"flavor_directives\"\n" +
            "}\n" +
            "],\n" +
            "\"type\": \"vnfc\",\n" +
            "\"id\": \"vgenerator\"\n" +
            "},\n" +
            "{\n" +
            "\"directives\": [\n" +
            "{\n" +
            "\"attributes\": [\n" +
            "{\n" +
            "\"attribute_value\": \"onap.hpa.flavor31\",\n" +
            "\"attribute_name\": \"sink_flavor_name\"\n" +
            "}\n" +
            "],\n" +
            "\"type\": \"flavor_directives\"\n" +
            "}\n" +
            "],\n" +
            "\"type\": \"vnfc\",\n" +
            "\"id\": \"vsink\"\n" +
            "}\n" +
            "]\n" +
            "}");
        localClient.postHomingInstance(homingInstance);
        HomingInstance getHomingInstance = this.client.getHomingInstance("5df8d6be-2083-11e7-93ae-92361f232671");
        Assert.assertNotNull(getHomingInstance);
        Assert.assertNotNull(getHomingInstance.getCloudRegionId());
        Assert.assertNotNull(getHomingInstance.getCloudOwner());
        Assert.assertEquals("CloudOwner-1", getHomingInstance.getCloudOwner());
        Assert.assertEquals("CloudRegionOne", getHomingInstance.getCloudRegionId());
    }

    @Test
    public void testGetServiceByModelName() {
        Service service = client.getServiceByModelName("MSOTADevInfra_Test_Service");
        Assert.assertNotNull(service);
        Assert.assertNotNull(service.getModelVersion());
        Assert.assertNotNull(service.getModelInvariantUUID());
        Assert.assertEquals("MSOTADevInfra_Test_Service", service.getModelName());
        Assert.assertEquals("NA", service.getServiceRole());
    }

    @Test
    public void testGetServiceByModelNameNotFound() {
        Service service = client.getServiceByModelName("Not_Found");
        Assert.assertNull(service);
    }

    @Test
    public void testGetServiceByModelUUID() {
        Service service = client.getServiceByModelUUID("5df8b6de-2083-11e7-93ae-92361f002679");
        Assert.assertNotNull(service);
        Assert.assertNotNull(service.getModelVersion());
        Assert.assertNotNull(service.getModelInvariantUUID());
        Assert.assertEquals("5df8b6de-2083-11e7-93ae-92361f002679", service.getModelUUID());
        Assert.assertEquals("NA", service.getServiceRole());
    }

    @Test
    public void testGetServiceByModelUUIDNotFound() {
        Service service = client.getServiceByModelUUID("Not_Found");
        Assert.assertNull(service);
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
        Assert.assertNotNull(northBoundRequest);
        Assert.assertEquals("createService", northBoundRequest.getAction());
        Assert.assertEquals("service", northBoundRequest.getRequestScope());
        Assert.assertEquals(true, northBoundRequest.getIsAlacarte());
        Assert.assertEquals("my-custom-cloud-owner", northBoundRequest.getCloudOwner());
    }

    @Test
    public void testFindServiceRecipeByActionAndServiceModelUUID() {
        ServiceRecipe serviceRecipe = client
            .findServiceRecipeByActionAndServiceModelUUID("createInstance", "4694a55f-58b3-4f17-92a5-796d6f5ffd0d");
        Assert.assertNotNull(serviceRecipe);
        Assert.assertNotNull(serviceRecipe.getServiceModelUUID());
        Assert.assertNotNull(serviceRecipe.getAction());
        Assert.assertEquals("/mso/async/services/CreateGenericALaCarteServiceInstance",
            serviceRecipe.getOrchestrationUri());
        Assert.assertEquals("MSOTADevInfra aLaCarte", serviceRecipe.getDescription());
    }

    @Test
    public void testFindServiceRecipeByActionAndServiceModelUUIDNotFound() {
        ServiceRecipe serviceRecipe = client
            .findServiceRecipeByActionAndServiceModelUUID("not_found", "5df8b6de-2083-11e7-93ae-test");
        Assert.assertNull(serviceRecipe);
    }

    @Test
    public void testFindExternalToInternalServiceByServiceName() {
        ExternalServiceToInternalService externalServiceToInternalService = client
            .findExternalToInternalServiceByServiceName("MySpecialServiceName");
        Assert.assertNotNull(externalServiceToInternalService);
        Assert.assertNotNull(externalServiceToInternalService.getServiceName());
        Assert.assertNotNull(externalServiceToInternalService.getServiceModelUUID());
        Assert.assertEquals("MySpecialServiceName", externalServiceToInternalService.getServiceName());
    }

    @Test
    public void testFindExternalToInternalServiceByServiceNameNotFound() {
        ExternalServiceToInternalService externalServiceToInternalService = client
            .findExternalToInternalServiceByServiceName("Not_Found");
        Assert.assertNull(externalServiceToInternalService);
    }

    @Test
    public void getPnfResourceByModelUUID_validUuid_expectedOutput() {
        PnfResource pnfResource = client.getPnfResourceByModelUUID("ff2ae348-214a-11e7-93ae-92361f002680");
        Assert.assertNotNull(pnfResource);
        assertEquals("PNFResource modelUUID", "ff2ae348-214a-11e7-93ae-92361f002680", pnfResource.getModelUUID());
        assertEquals("PNFResource modelInvariantUUID", "2fff5b20-214b-11e7-93ae-92361f002680",
            pnfResource.getModelInvariantUUID());
        assertEquals("PNFResource modelVersion", "1.0", pnfResource.getModelVersion());
        assertEquals("PNFResource orchestration mode", "", pnfResource.getOrchestrationMode());
    }

    @Test
    public void getPnfResourceByModelUUID_invalidUuid_NullOutput() {
        PnfResource pnfResource = client.getPnfResourceByModelUUID(UUID.randomUUID().toString());
        Assert.assertNull(pnfResource);
    }

    @Test
    public void getPnfResourceCustomizationByModelCustomizationUUID_validUuid_expectedOutput() {
        PnfResourceCustomization pnfResourceCustomization = client
            .getPnfResourceCustomizationByModelCustomizationUUID("68dc9a92-214c-11e7-93ae-92361f002680");
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
    public void getPnfResourceCustomizationFromJoinTable_validServiceUuid_expectedOutput() {
        List<PnfResourceCustomization> pnfResourceCustomizationList = client
            .getPnfResourceCustomizationFromJoinTable("5df8b6de-2083-11e7-93ae-92361f002676");
        assertEquals(1, pnfResourceCustomizationList.size());

        PnfResourceCustomization pnfResourceCustomization= pnfResourceCustomizationList.get(0);
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
        List<PnfResourceCustomization> pnfResourceCustomizationList = client
            .getPnfResourceCustomizationFromJoinTable(UUID.randomUUID().toString());
        assertEquals(0, pnfResourceCustomizationList.size());
    }

    @Test
    public void getPnfResourceCustomizationByModelCustomizationUUID_invalidUuid_nullOutput() {
        PnfResourceCustomization pnfResourceCustomization = client
            .getPnfResourceCustomizationByModelCustomizationUUID(UUID.randomUUID().toString());
        Assert.assertNull(pnfResourceCustomization);
    }

}
