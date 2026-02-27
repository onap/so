/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.db.catalog.beans.BBNameSelectionReference;
import org.onap.so.db.catalog.beans.BuildingBlockDetail;
import org.onap.so.db.catalog.beans.BuildingBlockRollback;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.db.catalog.beans.ExternalServiceToInternalService;
import org.onap.so.db.catalog.beans.HomingInstance;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkRecipe;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.OrchestrationAction;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.onap.so.db.catalog.beans.PnfResource;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.ProcessingFlags;
import org.onap.so.db.catalog.beans.ResourceType;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfComponentsRecipe;
import org.onap.so.db.catalog.beans.VnfRecipe;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.blackpepper.bowman.Client;

/**
 * Comprehensive unit tests verifying that every public method of {@link CatalogDbClient} that delegates to a bowman
 * {@link Client} calls the correct helper ({@code getSingleResource}, {@code getMultipleResources},
 * {@code postSingleResource}, or {@code deleteSingleResource}) with the expected bowman client and URI.
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogDbClientBowmanUsageTest {

    private static final String ENDPOINT = "http://localhost:8080";

    @Spy
    private CatalogDbClient catalogDbClient;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(catalogDbClient, "endpoint", ENDPOINT);
        catalogDbClient.init();
    }

    // ==================== getSingleResource-based methods ====================

    @Test
    public void testGetServiceByID() {
        Service service = new Service();
        doReturn(service).when(catalogDbClient).getSingleResource(any(), any());
        Service result = catalogDbClient.getServiceByID("uuid-123");
        assertNotNull(result);
        assertEquals("uuid-123", result.getModelUUID());
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create(ENDPOINT + "/service/" + "uuid-123")));
    }

    @Test
    public void testGetServiceByIDReturnsNull() {
        doReturn(null).when(catalogDbClient).getSingleResource(any(), any());
        Service result = catalogDbClient.getServiceByID("uuid-123");
        assertNull(result);
    }

    @Test
    public void testGetVfModuleByModelUUID() {
        VfModule vfModule = new VfModule();
        doReturn(vfModule).when(catalogDbClient).getSingleResource(any(), any());
        VfModule result = catalogDbClient.getVfModuleByModelUUID("vf-uuid");
        assertNotNull(result);
        assertEquals("vf-uuid", result.getModelUUID());
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create(ENDPOINT + "/vfModule/" + "vf-uuid")));
    }

    @Test
    public void testGetVfModuleByModelUUIDReturnsNull() {
        doReturn(null).when(catalogDbClient).getSingleResource(any(), any());
        VfModule result = catalogDbClient.getVfModuleByModelUUID("vf-uuid");
        assertNull(result);
    }

    @Test
    public void testGetVnfResourceByModelUUID() {
        VnfResource vnfResource = new VnfResource();
        doReturn(vnfResource).when(catalogDbClient).getSingleResource(any(), any());
        VnfResource result = catalogDbClient.getVnfResourceByModelUUID("vnf-uuid");
        assertNotNull(result);
        assertEquals("vnf-uuid", result.getModelUUID());
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create(ENDPOINT + "/vnfResource/" + "vnf-uuid")));
    }

    @Test
    public void testGetVnfResourceByModelUUIDReturnsNull() {
        doReturn(null).when(catalogDbClient).getSingleResource(any(), any());
        VnfResource result = catalogDbClient.getVnfResourceByModelUUID("vnf-uuid");
        assertNull(result);
    }

    @Test
    public void testGetPnfResourceByModelUUID() {
        PnfResource pnfResource = new PnfResource();
        doReturn(pnfResource).when(catalogDbClient).getSingleResource(any(), any());
        PnfResource result = catalogDbClient.getPnfResourceByModelUUID("pnf-uuid");
        assertNotNull(result);
        assertEquals("pnf-uuid", result.getModelUUID());
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create(ENDPOINT + "/pnfResource/" + "pnf-uuid")));
    }

    @Test
    public void testGetPnfResourceByModelUUIDReturnsNull() {
        doReturn(null).when(catalogDbClient).getSingleResource(any(), any());
        PnfResource result = catalogDbClient.getPnfResourceByModelUUID("pnf-uuid");
        assertNull(result);
    }

    @Test
    public void testGetPnfResourceCustomizationByModelCustomizationUUID() {
        PnfResourceCustomization pnfCust = new PnfResourceCustomization();
        doReturn(pnfCust).when(catalogDbClient).getSingleResource(any(), any());
        PnfResourceCustomization result = catalogDbClient
                .getPnfResourceCustomizationByModelCustomizationUUID("pnf-cust-uuid");
        assertNotNull(result);
        assertEquals("pnf-cust-uuid", result.getModelCustomizationUUID());
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create(ENDPOINT + "/pnfResourceCustomization/" + "pnf-cust-uuid")));
    }

    @Test
    public void testGetPnfResourceCustomizationByModelCustomizationUUIDReturnsNull() {
        doReturn(null).when(catalogDbClient).getSingleResource(any(), any());
        PnfResourceCustomization result = catalogDbClient
                .getPnfResourceCustomizationByModelCustomizationUUID("pnf-cust-uuid");
        assertNull(result);
    }

    @Test
    public void testGetNetworkCollectionResourceCustomizationByID() {
        NetworkCollectionResourceCustomization ncrc = new NetworkCollectionResourceCustomization();
        doReturn(ncrc).when(catalogDbClient).getSingleResource(any(), any());
        NetworkCollectionResourceCustomization result = catalogDbClient
                .getNetworkCollectionResourceCustomizationByID("ncrc-uuid");
        assertNotNull(result);
        assertEquals("ncrc-uuid", result.getModelCustomizationUUID());
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create(ENDPOINT + "/networkCollectionResourceCustomization/" + "ncrc-uuid")));
    }

    @Test
    public void testGetCollectionNetworkResourceCustomizationByID() {
        CollectionNetworkResourceCustomization cnrc = new CollectionNetworkResourceCustomization();
        doReturn(cnrc).when(catalogDbClient).getSingleResource(any(), any());
        CollectionNetworkResourceCustomization result = catalogDbClient
                .getCollectionNetworkResourceCustomizationByID("cnrc-uuid");
        assertNotNull(result);
        assertEquals("cnrc-uuid", result.getModelCustomizationUUID());
    }

    @Test
    public void testGetInstanceGroupByModelUUID() {
        InstanceGroup ig = new InstanceGroup();
        doReturn(ig).when(catalogDbClient).getSingleResource(any(), any());
        InstanceGroup result = catalogDbClient.getInstanceGroupByModelUUID("ig-uuid");
        assertNotNull(result);
        assertEquals("ig-uuid", result.getModelUUID());
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create(ENDPOINT + "/instanceGroup/" + "ig-uuid")));
    }

    @Test
    public void testGetNetworkResourceCustomizationByModelCustomizationUUID() {
        NetworkResourceCustomization nrc = new NetworkResourceCustomization();
        doReturn(nrc).when(catalogDbClient).getSingleResource(any(), any());
        NetworkResourceCustomization result = catalogDbClient
                .getNetworkResourceCustomizationByModelCustomizationUUID("nrc-uuid");
        assertNotNull(result);
        assertEquals("nrc-uuid", result.getModelCustomizationUUID());
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create(ENDPOINT + "/networkResourceCustomization/" + "nrc-uuid")));
    }

    @Test
    public void testGetVfModuleCustomizationByModelCuztomizationUUID() {
        VfModuleCustomization vfmc = new VfModuleCustomization();
        doReturn(vfmc).when(catalogDbClient).getSingleResource(any(), any());
        VfModuleCustomization result = catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("vfmc-uuid");
        assertNotNull(result);
        verify(catalogDbClient)
                .getSingleResource(any(Client.class),
                        eq(URI.create(ENDPOINT
                                + "/vfModuleCustomization/search/findFirstByModelCustomizationUUIDOrderByCreatedDesc"
                                + "?MODEL_CUSTOMIZATION_UUID=vfmc-uuid")));
    }

    @Test
    public void testGetBuildingBlockDetail() {
        BuildingBlockDetail bbd = new BuildingBlockDetail();
        doReturn(bbd).when(catalogDbClient).getSingleResource(any(), any());
        BuildingBlockDetail result = catalogDbClient.getBuildingBlockDetail("AssignServiceBB");
        assertNotNull(result);
        assertEquals("AssignServiceBB", result.getBuildingBlockName());
    }

    @Test
    public void testGetOrchestrationStatusStateTransitionDirective() {
        OrchestrationStatusStateTransitionDirective directive = new OrchestrationStatusStateTransitionDirective();
        doReturn(directive).when(catalogDbClient).getSingleResource(any(), any());
        OrchestrationStatusStateTransitionDirective result = catalogDbClient
                .getOrchestrationStatusStateTransitionDirective(ResourceType.VNF, OrchestrationStatus.ACTIVE,
                        OrchestrationAction.ASSIGN);
        assertNotNull(result);
        verify(catalogDbClient).getSingleResource(any(Client.class), eq(UriBuilder.fromUri(ENDPOINT
                + "/orchestrationStatusStateTransitionDirective/search/findOneByResourceTypeAndOrchestrationStatusAndTargetAction")
                .queryParam("resourceType", "VNF").queryParam("orchestrationStatus", "ACTIVE")
                .queryParam("targetAction", "ASSIGN").build()));
    }

    @Test
    public void testGetNorthBoundRequestByActionAndIsALaCarteAndRequestScope() {
        NorthBoundRequest nbr = new NorthBoundRequest();
        doReturn(nbr).when(catalogDbClient).getSingleResource(any(), any());
        NorthBoundRequest result = catalogDbClient
                .getNorthBoundRequestByActionAndIsALaCarteAndRequestScope("createInstance", "service", true);
        assertNotNull(result);
        verify(catalogDbClient).getSingleResource(any(Client.class), eq(UriBuilder
                .fromUri(ENDPOINT + "/northbound_request_ref_lookup/search/findOneByActionAndRequestScopeAndIsAlacarte")
                .queryParam("action", "createInstance").queryParam("requestScope", "service")
                .queryParam("isALaCarte", true).build()));
    }

    @Test
    public void testGetNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner() {
        NorthBoundRequest nbr = new NorthBoundRequest();
        doReturn(nbr).when(catalogDbClient).getSingleResource(any(), any());
        NorthBoundRequest result = catalogDbClient
                .getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner("createInstance", "service",
                        true, "cloudOwner1");
        assertNotNull(result);
    }

    @Test
    public void testGetNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwnerAndServiceType() {
        NorthBoundRequest nbr = new NorthBoundRequest();
        doReturn(nbr).when(catalogDbClient).getSingleResource(any(), any());
        NorthBoundRequest result = catalogDbClient
                .getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwnerAndServiceType("createInstance",
                        "service", true, "cloudOwner1", "serviceType1");
        assertNotNull(result);
    }

    @Test
    public void testGetRainyDayHandlerStatus() {
        RainyDayHandlerStatus rdhs = new RainyDayHandlerStatus();
        doReturn(rdhs).when(catalogDbClient).getSingleResource(any(), any());
        RainyDayHandlerStatus result = catalogDbClient.getRainyDayHandlerStatus("flow1", "serviceType1", "vnfType1",
                "404", "workStep1", "error msg", "serviceRole1");
        assertNotNull(result);
    }

    @Test
    public void testGetFirstByServiceModelUUIDAndAction() {
        ServiceRecipe sr = new ServiceRecipe();
        doReturn(sr).when(catalogDbClient).getSingleResource(any(), any());
        ServiceRecipe result = catalogDbClient.getFirstByServiceModelUUIDAndAction("model-uuid", "createInstance");
        assertNotNull(result);
    }

    @Test
    public void testGetFirstNetworkRecipeByModelNameAndAction() {
        NetworkRecipe nr = new NetworkRecipe();
        doReturn(nr).when(catalogDbClient).getSingleResource(any(), any());
        NetworkRecipe result = catalogDbClient.getFirstNetworkRecipeByModelNameAndAction("modelName1",
                "createInstance");
        assertNotNull(result);
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(UriBuilder.fromUri(ENDPOINT + "/networkRecipe/search/findFirstByModelNameAndAction")
                        .queryParam("modelName", "modelName1").queryParam("action", "createInstance").build()));
    }

    @Test
    public void testGetControllerSelectionReferenceByVnfTypeAndActionCategory() {
        ControllerSelectionReference csr = new ControllerSelectionReference();
        doReturn(csr).when(catalogDbClient).getSingleResource(any(), any());
        ControllerSelectionReference result = catalogDbClient
                .getControllerSelectionReferenceByVnfTypeAndActionCategory("vnfType1", "actionCat1");
        assertNotNull(result);
        verify(catalogDbClient).getSingleResource(any(Client.class), eq(UriBuilder.fromUri(ENDPOINT
                + "/controllerSelectionReference/search/findControllerSelectionReferenceByVnfTypeAndActionCategory")
                .queryParam("VNF_TYPE", "vnfType1").queryParam("ACTION_CATEGORY", "actionCat1").build()));
    }

    @Test
    public void testGetFirstByModelNameOrderByModelVersionDesc() {
        Service service = new Service();
        doReturn(service).when(catalogDbClient).getSingleResource(any(), any());
        Service result = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc("modelName1");
        assertNotNull(result);
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(UriBuilder.fromUri(ENDPOINT + "/service/search/findFirstByModelNameOrderByModelVersionDesc")
                        .queryParam("modelName", "modelName1").build()));
    }

    @Test
    public void testGetBBNameSelectionReference() {
        BBNameSelectionReference ref = new BBNameSelectionReference();
        doReturn(ref).when(catalogDbClient).getSingleResource(any(), any());
        BBNameSelectionReference result = catalogDbClient.getBBNameSelectionReference("actor1", "scope1", "action1");
        assertNotNull(result);
    }

    @Test
    public void testFindExternalToInternalServiceByServiceName() {
        ExternalServiceToInternalService mapping = new ExternalServiceToInternalService();
        doReturn(mapping).when(catalogDbClient).getSingleResource(any(), any());
        ExternalServiceToInternalService result = catalogDbClient
                .findExternalToInternalServiceByServiceName("serviceName1");
        assertNotNull(result);
    }

    @Test
    public void testFindServiceRecipeByActionAndServiceModelUUID() {
        ServiceRecipe recipe = new ServiceRecipe();
        doReturn(recipe).when(catalogDbClient).getSingleResource(any(), any());
        ServiceRecipe result = catalogDbClient.findServiceRecipeByActionAndServiceModelUUID("createInstance",
                "model-uuid");
        assertNotNull(result);
    }

    @Test
    public void testGetServiceByModelName() {
        Service service = new Service();
        doReturn(service).when(catalogDbClient).getSingleResource(any(), any());
        Service result = catalogDbClient.getServiceByModelName("modelName1");
        assertNotNull(result);
    }

    @Test
    public void testGetServiceByModelUUID() {
        Service service = new Service();
        doReturn(service).when(catalogDbClient).getSingleResource(any(), any());
        Service result = catalogDbClient.getServiceByModelUUID("model-uuid-123");
        assertNotNull(result);
    }

    @Test
    public void testGetFirstVnfResourceByModelInvariantUUIDAndModelVersion() {
        VnfResource vnfResource = new VnfResource();
        doReturn(vnfResource).when(catalogDbClient).getSingleResource(any(), any());
        VnfResource result = catalogDbClient.getFirstVnfResourceByModelInvariantUUIDAndModelVersion("invariant-uuid",
                "1.0");
        assertNotNull(result);
    }

    @Test
    public void testGetFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources() {
        VnfResourceCustomization vrc = new VnfResourceCustomization();
        doReturn(vrc).when(catalogDbClient).getSingleResource(any(), any());
        VnfResource vnfResource = new VnfResource();
        vnfResource.setModelUUID("vnf-model-uuid");
        VnfResourceCustomization result = catalogDbClient
                .getFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources("instanceName1", vnfResource);
        assertNotNull(result);
    }

    @Test
    public void testGetFirstVnfRecipeByNfRoleAndAction() {
        VnfRecipe recipe = new VnfRecipe();
        doReturn(recipe).when(catalogDbClient).getSingleResource(any(), any());
        VnfRecipe result = catalogDbClient.getFirstVnfRecipeByNfRoleAndAction("nfRole1", "createInstance");
        assertNotNull(result);
    }

    @Test
    public void testGetFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction() {
        VnfComponentsRecipe recipe = new VnfComponentsRecipe();
        doReturn(recipe).when(catalogDbClient).getSingleResource(any(), any());
        VnfComponentsRecipe result = catalogDbClient
                .getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction("vf-uuid", "vnf",
                        "createInstance");
        assertNotNull(result);
    }

    @Test
    public void testGetFirstVnfComponentsRecipeByVnfComponentTypeAndAction() {
        VnfComponentsRecipe recipe = new VnfComponentsRecipe();
        doReturn(recipe).when(catalogDbClient).getSingleResource(any(), any());
        VnfComponentsRecipe result = catalogDbClient.getFirstVnfComponentsRecipeByVnfComponentTypeAndAction("vnf",
                "createInstance");
        assertNotNull(result);
    }

    @Test
    public void testGetCloudifyManager() {
        CloudifyManager cm = new CloudifyManager();
        doReturn(cm).when(catalogDbClient).getSingleResource(any(), any());
        CloudifyManager result = catalogDbClient.getCloudifyManager("cm-id");
        assertNotNull(result);
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create(ENDPOINT + "/cloudifyManager/" + "cm-id")));
    }

    @Test
    public void testGetCloudSite() {
        CloudSite cs = new CloudSite();
        doReturn(cs).when(catalogDbClient).getSingleResource(any(), any());
        CloudSite result = catalogDbClient.getCloudSite("cs-id");
        assertNotNull(result);
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create(ENDPOINT + "/cloudSite/" + "cs-id")));
    }

    @Test
    public void testGetCloudSiteWithUri() {
        CloudSite cs = new CloudSite();
        doReturn(cs).when(catalogDbClient).getSingleResource(any(), any());
        CloudSite result = catalogDbClient.getCloudSite("cs-id", "http://other:9090/cloudSite/");
        assertNotNull(result);
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create("http://other:9090/cloudSite/" + "cs-id")));
    }

    @Test
    public void testGetCloudSiteByClliAndAicVersion() {
        CloudSite cs = new CloudSite();
        doReturn(cs).when(catalogDbClient).getSingleResource(any(), any());
        CloudSite result = catalogDbClient.getCloudSiteByClliAndAicVersion("clli1", "2.5");
        assertNotNull(result);
    }

    @Test
    public void testGetHomingInstance() {
        HomingInstance hi = new HomingInstance();
        doReturn(hi).when(catalogDbClient).getSingleResource(any(), any());
        HomingInstance result = catalogDbClient.getHomingInstance("si-id");
        assertNotNull(result);
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create(ENDPOINT + "/homingInstance/" + "si-id")));
    }

    @Test
    public void testGetHomingInstanceWithUri() {
        HomingInstance hi = new HomingInstance();
        doReturn(hi).when(catalogDbClient).getSingleResource(any(), any());
        HomingInstance result = catalogDbClient.getHomingInstance("si-id", "http://other:9090/homingInstance/");
        assertNotNull(result);
        verify(catalogDbClient).getSingleResource(any(Client.class),
                eq(URI.create("http://other:9090/homingInstance/" + "si-id")));
    }

    @Test
    public void testGetServiceByModelVersionAndModelInvariantUUID() {
        Service service = new Service();
        doReturn(service).when(catalogDbClient).getSingleResource(any(), any());
        Service result = catalogDbClient.getServiceByModelVersionAndModelInvariantUUID("1.0", "invariant-uuid");
        assertNotNull(result);
    }

    @Test
    public void testGetVfModuleByModelInvariantUUIDAndModelVersion() {
        VfModule vfModule = new VfModule();
        doReturn(vfModule).when(catalogDbClient).getSingleResource(any(), any());
        VfModule result = catalogDbClient.getVfModuleByModelInvariantUUIDAndModelVersion("invariant-uuid", "1.0");
        assertNotNull(result);
    }

    @Test
    public void testFindWorkflowByArtifactUUID() {
        Workflow workflow = new Workflow();
        doReturn(workflow).when(catalogDbClient).getSingleResource(any(), any());
        Workflow result = catalogDbClient.findWorkflowByArtifactUUID("artifact-uuid");
        assertNotNull(result);
    }

    @Test
    public void testFindProcessingFlagsByFlag() {
        ProcessingFlags flags = new ProcessingFlags();
        doReturn(flags).when(catalogDbClient).getSingleResource(any(), any());
        ProcessingFlags result = catalogDbClient.findProcessingFlagsByFlag("flag1");
        assertNotNull(result);
    }

    // ==================== getMultipleResources-based methods ====================

    @Test
    public void testGetVnfResourceCustomizationByModelCustomizationUUID() {
        List<VnfResourceCustomization> list = new ArrayList<>();
        VnfResourceCustomization vrc = new VnfResourceCustomization();
        vrc.setModelCustomizationUUID("cust-uuid");
        list.add(vrc);
        doReturn(list).when(catalogDbClient).getMultipleResources(any(), any());
        VnfResourceCustomization result = catalogDbClient
                .getVnfResourceCustomizationByModelCustomizationUUID("cust-uuid");
        assertNotNull(result);
        assertEquals("cust-uuid", result.getModelCustomizationUUID());
    }

    @Test
    public void testGetVnfResourceCustomizationByModelCustomizationUUIDReturnsNull() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        VnfResourceCustomization result = catalogDbClient
                .getVnfResourceCustomizationByModelCustomizationUUID("cust-uuid");
        assertNull(result);
    }

    @Test
    public void testGetVnfResourceCustomizationByModelUuid() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<VnfResourceCustomization> result = catalogDbClient.getVnfResourceCustomizationByModelUuid("model-uuid");
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class), any(URI.class));
    }

    @Test
    public void testGetPnfResourceCustomizationByModelUuid() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<PnfResourceCustomization> result = catalogDbClient.getPnfResourceCustomizationByModelUuid("model-uuid");
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class), any(URI.class));
    }

    @Test
    public void testGetOrchestrationFlowByAction() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<OrchestrationFlow> result = catalogDbClient.getOrchestrationFlowByAction("createInstance");
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(UriBuilder.fromUri(ENDPOINT + "/orchestrationFlow/search/findByAction")
                        .queryParam("action", "createInstance").build()));
    }

    @Test
    public void testGetVnfcInstanceGroupsByVnfResourceCust() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<VnfcInstanceGroupCustomization> result = catalogDbClient
                .getVnfcInstanceGroupsByVnfResourceCust("cust-uuid");
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(UriBuilder.fromUri(ENDPOINT + "/vnfcInstanceGroupCustomization/search/findByModelCustomizationUUID")
                        .queryParam("modelCustomizationUUID", "cust-uuid").build()));
    }

    @Test
    public void testGetCollectionResourceInstanceGroupCustomizationByModelCustUUID() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<CollectionResourceInstanceGroupCustomization> result = catalogDbClient
                .getCollectionResourceInstanceGroupCustomizationByModelCustUUID("cust-uuid");
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class), eq(UriBuilder
                .fromUri(ENDPOINT + "/collectionResourceInstanceGroupCustomization/search/findByModelCustomizationUUID")
                .queryParam("modelCustomizationUUID", "cust-uuid").build()));
    }

    @Test
    public void testGetVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID() {
        VfModuleCustomization vfmc = new VfModuleCustomization();
        doReturn(vfmc).when(catalogDbClient).getSingleResource(any(), any());
        VfModuleCustomization result = catalogDbClient
                .getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID("cust-uuid", "module-uuid");
        assertNotNull(result);
    }

    @Test
    public void testGetServiceByModelInvariantUUIDOrderByModelVersionDesc() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<Service> result = catalogDbClient.getServiceByModelInvariantUUIDOrderByModelVersionDesc("invariant-uuid");
        assertNotNull(result);
    }

    @Test
    public void testGetVfModuleByModelInvariantUUIDOrderByModelVersionDesc() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<VfModule> result = catalogDbClient
                .getVfModuleByModelInvariantUUIDOrderByModelVersionDesc("invariant-uuid");
        assertNotNull(result);
    }

    @Test
    public void testGetCloudSites() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<CloudSite> result = catalogDbClient.getCloudSites();
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(UriBuilder.fromUri(ENDPOINT + "/cloudSite").queryParam("size", "1000").build()));
    }

    @Test
    public void testFindWorkflowByVnfModelUUID() {
        String vnfResourceModelUUID = "vnf-model-uuid";
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<Workflow> result = catalogDbClient.findWorkflowByVnfModelUUID(vnfResourceModelUUID);
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(URI.create(UriBuilder.fromUri(ENDPOINT + "/workflow/search/findWorkflowByVnfModelUUID")
                        .queryParam("vnfResourceModelUUID", vnfResourceModelUUID).build().toString())));
    }

    @Test
    public void testFindWorkflowBySource() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<Workflow> result = catalogDbClient.findWorkflowBySource("source1");
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(URI.create(UriBuilder.fromUri(ENDPOINT + "/workflow/search/findBySource")
                        .queryParam("source", "source1").build().toString())));
    }

    @Test
    public void testFindWorkflowByResourceTarget() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<Workflow> result = catalogDbClient.findWorkflowByResourceTarget("pnf");
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(URI.create(UriBuilder.fromUri(ENDPOINT + "/workflow/search/findByResourceTarget")
                        .queryParam("resourceTarget", "pnf").build().toString())));
    }

    @Test
    public void testFindWorkflowByPnfModelUUID() {
        String pnfResourceModelUUID = "pnf-model-uuid";
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<Workflow> result = catalogDbClient.findWorkflowByPnfModelUUID(pnfResourceModelUUID);
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(URI.create(UriBuilder.fromUri(ENDPOINT + "/workflow/search/findWorkflowByPnfModelUUID")
                        .queryParam("pnfResourceModelUUID", pnfResourceModelUUID).build().toString())));
    }

    @Test
    public void testFindWorkflowByOperationName() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<Workflow> result = catalogDbClient.findWorkflowByOperationName("PNFSoftwareUpgrade");
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(URI.create(UriBuilder.fromUri(ENDPOINT + "/workflow/search/findByOperationName")
                        .queryParam("operationName", "PNFSoftwareUpgrade").build().toString())));
    }

    @Test
    public void testGetServiceRecipes() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<ServiceRecipe> result = catalogDbClient.getServiceRecipes();
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(UriBuilder.fromUri(ENDPOINT + "/serviceRecipe").queryParam("size", "1000").build()));
    }

    @Test
    public void testGetNetworkRecipes() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<NetworkRecipe> result = catalogDbClient.getNetworkRecipes();
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(UriBuilder.fromUri(ENDPOINT + "/networkRecipe").queryParam("size", "1000").build()));
    }

    @Test
    public void testGetNetworkResources() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<NetworkResource> result = catalogDbClient.getNetworkResources();
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(UriBuilder.fromUri(ENDPOINT + "/networkResource").queryParam("size", "1000").build()));
    }

    @Test
    public void testGetVnfResources() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<VnfResource> result = catalogDbClient.getVnfResources();
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(UriBuilder.fromUri(ENDPOINT + "/vnfResource").queryParam("size", "1000").build()));
    }

    @Test
    public void testGetVnfRecipes() {
        doReturn(new ArrayList<>()).when(catalogDbClient).getMultipleResources(any(), any());
        List<VnfRecipe> result = catalogDbClient.getVnfRecipes();
        assertNotNull(result);
        verify(catalogDbClient).getMultipleResources(any(Client.class),
                eq(UriBuilder.fromUri(ENDPOINT + "/vnfRecipe").queryParam("size", "1000").build()));
    }

    // ==================== postSingleResource-based methods ====================

    @Test
    public void testPostOofHomingCloudSite() {
        CloudSite cloudSite = new CloudSite();
        doReturn(URI.create("/cloudSite/new")).when(catalogDbClient).postSingleResource(any(), any());
        catalogDbClient.postOofHomingCloudSite(cloudSite);
        verify(catalogDbClient).postSingleResource(any(Client.class), eq(cloudSite));
    }

    @Test
    public void testPostHomingInstance() {
        HomingInstance homingInstance = new HomingInstance();
        doReturn(URI.create("/homingInstance/new")).when(catalogDbClient).postSingleResource(any(), any());
        catalogDbClient.postHomingInstance(homingInstance);
        verify(catalogDbClient).postSingleResource(any(Client.class), eq(homingInstance));
    }

    // ==================== deleteSingleResource-based methods ====================

    @Test
    public void testDeleteServiceRecipe() {
        doNothing().when(catalogDbClient).deleteSingleResource(any(), any());
        catalogDbClient.deleteServiceRecipe("recipe-123");
        verify(catalogDbClient).deleteSingleResource(any(Client.class),
                eq(UriBuilder.fromUri(ENDPOINT + "/serviceRecipe/" + "recipe-123").build()));
    }
}
