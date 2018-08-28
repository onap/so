package org.onap.so.db.catalog.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.catalogdb.CatalogDBApplication;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfComponentsRecipe;
import org.onap.so.db.catalog.beans.VnfRecipe;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CatalogDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CatalogDbClientTest {
    public static final String MTN13 = "mtn13";
    @LocalServerPort
    private int port;
    @Autowired
    CatalogDbClientPortChanger client;

    @Before
    public void initialize() {
        client.wiremockPort = String.valueOf(port);
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
        VnfResourceCustomization vnfResourceCustomization = client.getVnfResourceCustomizationByModelCustomizationUUID("68dc9a92-214c-11e7-93ae-92361f002671");
        Assert.assertNotNull(vnfResourceCustomization);
        Assert.assertEquals("vSAMP", vnfResourceCustomization.getNfRole());
        Assert.assertNotNull(vnfResourceCustomization.getModelCustomizationUUID());
        Assert.assertNotNull(vnfResourceCustomization.getVnfResources());
        Assert.assertNotNull(vnfResourceCustomization.getVfModuleCustomizations());
        Assert.assertEquals("vSAMP10a", vnfResourceCustomization.getVnfResources().getModelName());

    }

    @Test
    public void testGetVnfResourceCustomizationByModelCustomizationUUINotFound() {
        VnfResourceCustomization vnfResourceCustomization = client.getVnfResourceCustomizationByModelCustomizationUUID(UUID.randomUUID().toString());
        Assert.assertNull(vnfResourceCustomization);
    }

    @Test
    public void testGetInstanceGroupByModelUUID() {
        InstanceGroup instanceGroup = client.getInstanceGroupByModelUUID("0c8692ef-b9c0-435d-a738-edf31e71f38b");
        Assert.assertNotNull(instanceGroup);
        Assert.assertEquals("network_collection_resource_1806..NetworkCollection..0", instanceGroup.getModelName());
        Assert.assertEquals("org.openecomp.resource.cr.NetworkCollectionResource1806", instanceGroup.getToscaNodeType().toString());
    }

    @Test
    public void testGetVfModuleCustomizationByModelCuztomizationUUID() {
        VfModuleCustomization vfModuleCustomization = client.getVfModuleCustomizationByModelCuztomizationUUID("cb82ffd8-252a-11e7-93ae-92361f002671");
        Assert.assertNotNull(vfModuleCustomization);
        Assert.assertNotNull(vfModuleCustomization.getModelCustomizationUUID());
        Assert.assertEquals("base", vfModuleCustomization.getLabel());
    }

    @Test
    public void testGetVfModuleCustomizationByModelCuztomizationUUIDNotFound() {
        VfModuleCustomization vfModuleCustomization = client.getVfModuleCustomizationByModelCuztomizationUUID(UUID.randomUUID().toString());
        Assert.assertNull(vfModuleCustomization);
    }

    @Test
    public void testGetNetworkResourceCustomizationByModelCustomizationUUID() {
        NetworkResourceCustomization networkResourceCustomization = client.getNetworkResourceCustomizationByModelCustomizationUUID("3bdbb104-476c-483e-9f8b-c095b3d308ac");
        Assert.assertNotNull(networkResourceCustomization);
        Assert.assertNotNull(networkResourceCustomization.getModelCustomizationUUID());
        Assert.assertEquals("CONTRAIL30_GNDIRECT 9", networkResourceCustomization.getModelInstanceName());
        Assert.assertNotNull(networkResourceCustomization.getNetworkResource());
    }

    @Test
    public void testGetNetworkResourceCustomizationByModelCustomizationUUIDNotFound() {
        NetworkResourceCustomization networkResourceCustomization = client.getNetworkResourceCustomizationByModelCustomizationUUID(UUID.randomUUID().toString());
        Assert.assertNull(networkResourceCustomization);
    }

    @Test
    public void testGgetVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID() {
        VfModuleCustomization vfModuleCustomization = client.getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID("cb82ffd8-252a-11e7-93ae-92361f002672", "20c4431c-246d-11e7-93ae-92361f002672");
        Assert.assertNotNull(vfModuleCustomization);
        Assert.assertNotNull(vfModuleCustomization.getModelCustomizationUUID());
        Assert.assertNotNull(vfModuleCustomization.getVfModule());
        Assert.assertEquals("base", vfModuleCustomization.getLabel());
    }

    @Test
    public void testGgetVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUIDNotFound() {
        VfModuleCustomization vfModuleCustomization = client.getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID("cb82ffd8-252a-11e7-93ae-92361f002672", UUID.randomUUID().toString());
        Assert.assertNull(vfModuleCustomization);
    }

    @Test
    public void testGetFirstByServiceModelUUIDAndAction() {
        ServiceRecipe serviceRecipe = client.getFirstByServiceModelUUIDAndAction("4694a55f-58b3-4f17-92a5-796d6f5ffd0d", "createInstance");
        Assert.assertNotNull(serviceRecipe);
        Assert.assertNotNull(serviceRecipe.getServiceModelUUID());
        Assert.assertNotNull(serviceRecipe.getAction());
        Assert.assertEquals("/mso/async/services/CreateGenericALaCarteServiceInstance", serviceRecipe.getOrchestrationUri());
        Assert.assertEquals("MSOTADevInfra aLaCarte", serviceRecipe.getDescription());
    }

    @Test
    public void testGetFirstByServiceModelUUIDAndActionNotFound() {
        ServiceRecipe serviceRecipe = client.getFirstByServiceModelUUIDAndAction("5df8b6de-2083-11e7-93ae-92361f002671", UUID.randomUUID().toString());
        Assert.assertNull(serviceRecipe);
    }
    
    @Test
    public void testGetFirstVnfResourceByModelInvariantUUIDAndModelVersion() {
        VnfResource vnfResource = client.getFirstVnfResourceByModelInvariantUUIDAndModelVersion("2fff5b20-214b-11e7-93ae-92361f002671", "2.0");
        Assert.assertNotNull(vnfResource);
        Assert.assertNotNull(vnfResource.getModelInvariantId());
        Assert.assertNotNull(vnfResource.getModelVersion());
        Assert.assertNotNull(vnfResource.getHeatTemplates());
        Assert.assertNotNull(vnfResource.getVnfResourceCustomizations());
        Assert.assertEquals("vSAMP10a", vnfResource.getModelName());
    }

    @Test
    public void testGetFirstVnfResourceByModelInvariantUUIDAndModelVersionNotFound() {
        VnfResource vnfResource = client.getFirstVnfResourceByModelInvariantUUIDAndModelVersion("2fff5b20-214b-11e7-93ae-92361f002671", UUID.randomUUID().toString());
        Assert.assertNull(vnfResource);
    }

    @Test
    public void testGetFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources() {
        VnfResource vnfr = new VnfResource();
        vnfr.setModelUUID("ff2ae348-214a-11e7-93ae-92361f002671");
        VnfResourceCustomization firstVnfResourceCustomizationByModelInstanceNameAndVnfResources = client.getFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources("vSAMP10a 1", vnfr);
        Assert.assertNotNull(firstVnfResourceCustomizationByModelInstanceNameAndVnfResources);
        Assert.assertEquals("vSAMP", firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getNfRole());
        Assert.assertEquals("vSAMP10a 1", firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getModelInstanceName());
        Assert.assertNotNull(firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getVnfResources());
        Assert.assertNotNull(firstVnfResourceCustomizationByModelInstanceNameAndVnfResources.getVfModuleCustomizations());
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
        VnfComponentsRecipe vnfComponentsRecipe = client.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction("20c4431c-246d-11e7-93ae-92361f002671", "volumeGroup", "createInstance");
        Assert.assertNotNull(vnfComponentsRecipe);
        Assert.assertNotNull(vnfComponentsRecipe.getAction());
        Assert.assertNotNull(vnfComponentsRecipe.getVfModuleModelUUID());
        Assert.assertNotNull(vnfComponentsRecipe.getVnfComponentType());
        Assert.assertEquals("Gr api recipe to create volume-group", vnfComponentsRecipe.getDescription());
        Assert.assertEquals("/mso/async/services/WorkflowActionBB", vnfComponentsRecipe.getOrchestrationUri());

    }


    @Test
    public void testGetFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndActionNotFound() {
        VnfComponentsRecipe vnfComponentsRecipe = client.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(UUID.randomUUID().toString(), "volumeGroup", "createInstance");
        Assert.assertNull(vnfComponentsRecipe);
    }

    @Test
    public void testGetFirstVnfComponentsRecipeByVnfComponentTypeAndAction() {
        VnfComponentsRecipe vnfComponentsRecipe = client.getFirstVnfComponentsRecipeByVnfComponentTypeAndAction("volumeGroup", "createInstance");
        Assert.assertNotNull(vnfComponentsRecipe);
        Assert.assertNotNull(vnfComponentsRecipe.getAction());
        Assert.assertNotNull(vnfComponentsRecipe.getVnfComponentType());
        Assert.assertEquals("VID_DEFAULT recipe t", vnfComponentsRecipe.getDescription());
        Assert.assertEquals("/mso/async/services/CreateVfModuleVolumeInfraV1", vnfComponentsRecipe.getOrchestrationUri());
    }

    @Test
    public void testGetServiceByModelVersionAndModelInvariantUUID() {
        Service service = client.getServiceByModelVersionAndModelInvariantUUID("2.0", "9647dfc4-2083-11e7-93ae-92361f002671");
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
        VfModule vfModule = client.getVfModuleByModelInvariantUUIDAndModelVersion("78ca26d0-246d-11e7-93ae-92361f002671", "2");
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
        List<Service> serviceList = client.getServiceByModelInvariantUUIDOrderByModelVersionDesc("9647dfc4-2083-11e7-93ae-92361f002671");
        Assert.assertFalse(serviceList.isEmpty());
        Assert.assertEquals(2, serviceList.size());
        Service service = serviceList.get(0);
        Assert.assertEquals("2.0", service.getModelVersion());
    }

    @Test
    public void testGetServiceByModelInvariantUUIDOrderByModelVersionDescNotFound() {
        List<Service> serviceList = client.getServiceByModelInvariantUUIDOrderByModelVersionDesc(UUID.randomUUID().toString());
        Assert.assertTrue(serviceList.isEmpty());
    }

    @Test
    public void testGetVfModuleByModelInvariantUUIDOrderByModelVersionDesc() {
        List<VfModule> moduleList = client.getVfModuleByModelInvariantUUIDOrderByModelVersionDesc("78ca26d0-246d-11e7-93ae-92361f002671");
        Assert.assertFalse(moduleList.isEmpty());
        Assert.assertEquals(2, moduleList.size());
        VfModule module = moduleList.get(0);
        Assert.assertEquals("vSAMP10a DEV Base",module.getDescription());
    }
}
