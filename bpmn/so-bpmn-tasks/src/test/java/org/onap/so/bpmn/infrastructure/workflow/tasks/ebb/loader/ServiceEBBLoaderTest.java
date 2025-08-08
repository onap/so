/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2020 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2021 Orange
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

package org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.onap.aai.domain.yang.ComposedResource;
import org.onap.aai.domain.yang.ComposedResources;
import org.onap.aai.domain.yang.RelatedToProperty;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.infrastructure.workflow.tasks.VrfBondingServiceException;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;

public class ServiceEBBLoaderTest extends BaseTaskTest {

    private static final String MACRO_ACTIVATE_DELETE_UNASSIGN_JSON = "Macro/ServiceMacroActivateDeleteUnassign.json";
    private static final String MACRO_ASSIGN_JSON = "Macro/ServiceMacroAssign.json";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    protected Relationships relationships;

    private DelegateExecution execution;
    private ServiceEBBLoader serviceEBBLoader;
    private UserParamsServiceTraversal mockUserParamsServiceTraversal;
    private CatalogDbClient mockCatalogDbClient;
    private VrfValidation mockVrfValidation;
    private AAIConfigurationResources mockAaiConfigurationResources;
    private WorkflowActionExtractResourcesAAI mockWorkflowActionExtractResourcesAAI;
    private BBInputSetupUtils mockBbInputSetupUtils;
    private BBInputSetup mockBbInputSetup;

    @Before
    public void before() {
        execution = new DelegateExecutionFake();
        mockUserParamsServiceTraversal = mock(UserParamsServiceTraversal.class);
        mockCatalogDbClient = mock(CatalogDbClient.class);
        mockVrfValidation = mock(VrfValidation.class);
        mockAaiConfigurationResources = mock(AAIConfigurationResources.class);
        mockWorkflowActionExtractResourcesAAI = mock(WorkflowActionExtractResourcesAAI.class);
        mockBbInputSetupUtils = mock(BBInputSetupUtils.class);
        mockBbInputSetup = mock(BBInputSetup.class);
        serviceEBBLoader = new ServiceEBBLoader(mockUserParamsServiceTraversal, mockCatalogDbClient, mockVrfValidation,
                mockAaiConfigurationResources, mockWorkflowActionExtractResourcesAAI, mockBbInputSetupUtils,
                mockBbInputSetup, mock(ExceptionBuilder.class));
    }


    @Test
    public void getResourceListForServiceWithRequestActionAssignInstance()
            throws IOException, VrfBondingServiceException {
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ASSIGN_JSON);
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);
        String requestAction = "assignInstance";
        String serviceInstanceId = "123";
        String resourceId = "si0";
        List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();
        doReturn(prepareListWithResources()).when(mockUserParamsServiceTraversal).getResourceListFromUserParams(any(),
                anyList(), anyString(), anyString());
        List<Resource> resources = serviceEBBLoader.getResourceListForService(sIRequest, requestAction, execution,
                serviceInstanceId, resourceId, aaiResourceIds);
        assertNotNull(resources);
        assertEquals(resources.size(), 6);
    }

    @Test
    public void findCatalogNetworkCollectionTest() {
        Service service = new Service();
        NetworkCollectionResourceCustomization networkCustomization = new NetworkCollectionResourceCustomization();
        networkCustomization.setModelCustomizationUUID("123");
        service.getCollectionResourceCustomizations().add(networkCustomization);
        doReturn(networkCustomization).when(mockCatalogDbClient).getNetworkCollectionResourceCustomizationByID("123");
        CollectionResourceCustomization customization =
                serviceEBBLoader.findCatalogNetworkCollection(execution, service);
        assertNotNull(customization);
    }

    @Test
    public void findCatalogNetworkCollectionEmptyTest() {
        Service service = new Service();
        NetworkCollectionResourceCustomization networkCustomization = new NetworkCollectionResourceCustomization();
        networkCustomization.setModelCustomizationUUID("123");
        service.getCollectionResourceCustomizations().add(networkCustomization);
        CollectionResourceCustomization customization =
                serviceEBBLoader.findCatalogNetworkCollection(execution, service);
        assertNull(customization);
    }

    @Test
    public void findCatalogNetworkCollectionMoreThanOneTest() {
        Service service = new Service();
        NetworkCollectionResourceCustomization networkCustomization1 = new NetworkCollectionResourceCustomization();
        networkCustomization1.setModelCustomizationUUID("123");
        NetworkCollectionResourceCustomization networkCustomization2 = new NetworkCollectionResourceCustomization();
        networkCustomization2.setModelCustomizationUUID("321");
        service.getCollectionResourceCustomizations().add(networkCustomization1);
        service.getCollectionResourceCustomizations().add(networkCustomization2);
        doReturn(networkCustomization1).when(mockCatalogDbClient).getNetworkCollectionResourceCustomizationByID("123");
        doReturn(networkCustomization2).when(mockCatalogDbClient).getNetworkCollectionResourceCustomizationByID("321");
        serviceEBBLoader.findCatalogNetworkCollection(execution, service);
        assertEquals("Found multiple Network Collections in the Service model, only one per Service is supported.",
                execution.getVariable("WorkflowActionErrorMessage"));
    }

    @Test
    public void foundRelatedTest() {
        List<Resource> resourceList = new ArrayList<>();
        resourceList.add(new Resource(WorkflowType.PNF, "model customization id", false, null));
        resourceList.add(new Resource(WorkflowType.VNF, "model customization id", false, null));
        resourceList.add(new Resource(WorkflowType.NETWORK, "model customization id", false, null));
        resourceList.add(new Resource(WorkflowType.NETWORKCOLLECTION, "model customization id", false, null));

        assertTrue(serviceEBBLoader.foundRelated(resourceList));
    }

    @Test
    public void containsWorkflowTypeTest() {
        List<Resource> resourceList = new ArrayList<>();
        resourceList.add(new Resource(WorkflowType.PNF, "resource id", false, null));
        resourceList.add(new Resource(WorkflowType.VNF, "model customization id", false, null));
        resourceList.add(new Resource(WorkflowType.NETWORK, "model customization id", false, null));
        resourceList.add(new Resource(WorkflowType.NETWORKCOLLECTION, "model customization id", false, null));

        assertTrue(serviceEBBLoader.containsWorkflowType(resourceList, WorkflowType.PNF));
        assertTrue(serviceEBBLoader.containsWorkflowType(resourceList, WorkflowType.VNF));
        assertTrue(serviceEBBLoader.containsWorkflowType(resourceList, WorkflowType.NETWORK));
        assertTrue(serviceEBBLoader.containsWorkflowType(resourceList, WorkflowType.NETWORKCOLLECTION));
        assertFalse(serviceEBBLoader.containsWorkflowType(resourceList, WorkflowType.CONFIGURATION));
    }

    @Test
    public void traverseAAIServiceTest() {
        List<Resource> resourceCounter = new ArrayList<>();
        String resourceId = "si0";
        List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();

        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId(resourceId);

        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstance = setServiceInstance();
        GenericVnf genericVnf = setGenericVnf();
        setVfModule(true);
        setVolumeGroup();
        setL3Network();
        setCollection();
        setConfiguration();

        org.onap.aai.domain.yang.GenericVnf genericVnfAai = new org.onap.aai.domain.yang.GenericVnf();
        genericVnfAai.setModelCustomizationId(genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid());

        Configuration config = new Configuration();
        config.setConfigurationId("testConfigurationId2");
        serviceInstance.getConfigurations().add(config);

        Relationship relationship1 = new Relationship();
        relationship1.setRelatedTo("vnfc");
        RelationshipList relationshipList1 = new RelationshipList();
        relationshipList1.getRelationship().add(relationship1);

        Relationship relationship2 = new Relationship();
        relationship2.setRelatedTo("vpn-binding");
        RelationshipList relationshipList2 = new RelationshipList();
        relationshipList2.getRelationship().add(relationship2);

        org.onap.aai.domain.yang.Configuration aaiConfiguration1 = new org.onap.aai.domain.yang.Configuration();
        aaiConfiguration1.setConfigurationId("testConfigurationId");
        aaiConfiguration1.setRelationshipList(relationshipList1);

        org.onap.aai.domain.yang.Configuration aaiConfiguration2 = new org.onap.aai.domain.yang.Configuration();
        aaiConfiguration2.setConfigurationId("testConfigurationId2");
        aaiConfiguration2.setRelationshipList(relationshipList1);

        org.onap.aai.domain.yang.VfModule aaiVfModule = new org.onap.aai.domain.yang.VfModule();
        aaiVfModule.setIsBaseVfModule(true);

        try {
            doReturn(genericVnfAai).when(mockBbInputSetupUtils).getAAIGenericVnf(genericVnf.getVnfId());
            doReturn(serviceInstanceAAI).when(mockBbInputSetupUtils).getAAIServiceInstanceById(resourceId);
            doReturn(serviceInstance).when(mockBbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
            doReturn(Optional.of(aaiConfiguration1)).when(mockAaiConfigurationResources)
                    .getConfiguration("testConfigurationId");
            doReturn(Optional.of(aaiConfiguration2)).when(mockAaiConfigurationResources)
                    .getConfiguration("testConfigurationId2");
            doReturn(aaiVfModule).when(mockBbInputSetupUtils).getAAIVfModule(any(), any());
            serviceEBBLoader.traverseAAIService(execution, resourceCounter, resourceId, aaiResourceIds);
            assertEquals(8, resourceCounter.size());
            assertTrue(resourceCounter.get(2).isBaseVfModule());
            assertThat(aaiResourceIds, sameBeanAs(getExpectedResourceIds()));
        } catch (Exception e) {
            fail("Unexpected exception was thrown.");
        }
    }

    @Test
    public void traverseVrfConfigurationTest() throws VrfBondingServiceException, JsonProcessingException {
        List<Resource> resource = new ArrayList<>();
        List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();

        Service service = new Service();
        List<ConfigurationResourceCustomization> resourceCustomizations = new ArrayList<>();

        ConfigurationResourceCustomization configuration = new ConfigurationResourceCustomization();
        configuration.setModelCustomizationUUID("123");
        resourceCustomizations.add(configuration);
        service.setConfigurationCustomizations(resourceCustomizations);

        Relationship relationship = new Relationship();
        relationship.setRelatedTo("vpn-binding");

        RelationshipList relationshipList = new RelationshipList();
        relationshipList.getRelationship().add(relationship);

        org.onap.aai.domain.yang.L3Network aaiLocalNetwork = new org.onap.aai.domain.yang.L3Network();
        aaiLocalNetwork.setNetworkId("localNetworkId");
        aaiLocalNetwork.setRelationshipList(relationshipList);

        RelatedInstance relatedVpnBinding = new RelatedInstance();
        relatedVpnBinding.setInstanceId("vpnBindingInstanceId");
        RelatedInstance relatedLocalNetwork = new RelatedInstance();
        relatedLocalNetwork.setInstanceId("localNetworkInstanceId");


        doReturn(aaiLocalNetwork).when(mockBbInputSetupUtils).getAAIL3Network("localNetworkInstanceId");

        Resource serviceResource = new Resource(WorkflowType.SERVICE, "1", false, null);
        serviceEBBLoader.traverseVrfConfiguration(aaiResourceIds, resource, serviceResource, service, relatedVpnBinding,
                relatedLocalNetwork);
        assertEquals(resource.size(), 1);
        assertEquals(aaiResourceIds.size(), 0);
    }

    private List<Pair<WorkflowType, String>> getExpectedResourceIds() {
        List<Pair<WorkflowType, String>> resourceIds = new ArrayList<>();
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VNF, "testVnfId1"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VFMODULE, "testVfModuleId1"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VOLUMEGROUP, "testVolumeGroupId1"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.NETWORK, "testNetworkId1"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.NETWORKCOLLECTION, "testId"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.CONFIGURATION, "testConfigurationId"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.CONFIGURATION, "testConfigurationId2"));
        return resourceIds;
    }

    @Test
    public void traverseCatalogDbServiceMultipleNetworkTest() throws IOException, VrfBondingServiceException {
        execution.setVariable("testProcessKey", "testProcessKeyValue");
        Service service = new Service();
        List<NetworkResourceCustomization> networkCustomizations = new ArrayList<>();
        NetworkResourceCustomization networkCust = new NetworkResourceCustomization();
        networkCust.setModelCustomizationUUID("123");
        networkCustomizations.add(networkCust);
        service.setNetworkCustomizations(networkCustomizations);
        NetworkCollectionResourceCustomization collectionResourceCustomization =
                new NetworkCollectionResourceCustomization();
        collectionResourceCustomization.setModelCustomizationUUID("123");
        CollectionResource collectionResource = new CollectionResource();
        collectionResource.setToscaNodeType("NetworkCollection");
        InstanceGroup instanceGroup = new InstanceGroup();
        List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupCustomizations = new ArrayList<>();
        CollectionResourceInstanceGroupCustomization collectionInstanceGroupCustomization =
                new CollectionResourceInstanceGroupCustomization();
        collectionInstanceGroupCustomization.setSubInterfaceNetworkQuantity(3);
        collectionInstanceGroupCustomizations.add(collectionInstanceGroupCustomization);
        instanceGroup.setCollectionInstanceGroupCustomizations(collectionInstanceGroupCustomizations);
        collectionResource.setInstanceGroup(instanceGroup);
        collectionResourceCustomization.setCollectionResource(collectionResource);;
        service.setModelUUID("abc");
        service.getCollectionResourceCustomizations().add(collectionResourceCustomization);
        service.getCollectionResourceCustomizations().add(collectionResourceCustomization);


        doReturn(service).when(mockCatalogDbClient).getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a");
        doReturn(collectionResourceCustomization).when(mockCatalogDbClient)
                .getNetworkCollectionResourceCustomizationByID("123");
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ACTIVATE_DELETE_UNASSIGN_JSON);
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);

        List<Resource> resource = new ArrayList<>();
        List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();

        serviceEBBLoader.traverseCatalogDbService(execution, sIRequest, resource, aaiResourceIds);
        assertEquals(resource.size(), 2);
    }

    private String readBpmnRequestFromFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/__files/" + fileName)));
    }

    private List<Resource> prepareListWithResources() {
        List<Resource> resourceList = new ArrayList<>();
        Resource r1 = new Resource(WorkflowType.SERVICE, "3c40d244-808e-42ca-b09a-256d83d19d0a", false, null);
        resourceList.add(r1);
        Resource r2 = new Resource(WorkflowType.VNF, "ab153b6e-c364-44c0-bef6-1f2982117f04", false, r1);
        resourceList.add(r2);
        resourceList.add(new Resource(WorkflowType.VOLUMEGROUP, "a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f", false, r2));
        resourceList.add(new Resource(WorkflowType.VFMODULE, "72d9d1cd-f46d-447a-abdb-451d6fb05fa8", false, r2));
        resourceList.add(new Resource(WorkflowType.VFMODULE, "3c40d244-808e-42ca-b09a-256d83d19d0a", false, r2));
        resourceList.add(new Resource(WorkflowType.VFMODULE, "72d9d1cd-f46d-447a-abdb-451d6fb05fa8", false, r2));
        return resourceList;
    }

    @Test
    public void traverseServiceInstanceChildServiceTest() {
        List<Resource> resourceList = new ArrayList<>();
        Resource parentResource = new Resource(WorkflowType.SERVICE, "parentId", false, null);
        String resourceId = "siP";
        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId(resourceId);

        RelationshipData relationshipData = new RelationshipData();
        relationshipData.setRelationshipKey("service-instance.service-instance-id");
        relationshipData.setRelationshipValue("80ced9d5-666e-406b-88f0-a05d31328b70");
        RelatedToProperty relatedToProperty = new RelatedToProperty();
        relatedToProperty.setPropertyKey("service-instance.service-instance-name");
        relatedToProperty.setPropertyValue("child_euler_002");

        RelationshipData relationshipData1 = new RelationshipData();
        relationshipData1.setRelationshipKey("service-instance.service-instance-id");
        relationshipData1.setRelationshipValue("fa5640af-c827-4372-baae-7f1c50fdb5ed");
        RelatedToProperty relatedToProperty1 = new RelatedToProperty();
        relatedToProperty1.setPropertyKey("service-instance.service-instance-name");
        relatedToProperty.setPropertyValue("child_euler_001");


        Relationship relationship = new Relationship();
        Relationship relationship1 = new Relationship();
        relationship.setRelatedTo("service-instance");
        relationship1.setRelatedTo("service-instance");
        relationship.getRelationshipData().add(relationshipData);
        relationship.getRelatedToProperty().add(relatedToProperty);
        relationship1.getRelationshipData().add(relationshipData1);
        relationship1.getRelatedToProperty().add(relatedToProperty1);

        RelationshipList relationshipList = new RelationshipList();
        RelationshipList relationshipList1 = new RelationshipList();
        relationshipList.getRelationship().add(relationship);
        relationshipList1.getRelationship().add(relationship1);

        ComposedResource composedResource = new ComposedResource();
        composedResource.setRelationshipList(relationshipList);
        ComposedResource composedResource1 = new ComposedResource();
        composedResource1.setRelationshipList(relationshipList);

        ComposedResources composedResources = new ComposedResources();
        composedResources.getComposedResource().add(composedResource);
        composedResources.getComposedResource().add(composedResource1);

        serviceInstanceAAI.setComposedResources(composedResources);

        serviceEBBLoader.traverseServiceInstanceChildService(resourceList, parentResource, serviceInstanceAAI);
        assertEquals(2, resourceList.size());
    }
}
