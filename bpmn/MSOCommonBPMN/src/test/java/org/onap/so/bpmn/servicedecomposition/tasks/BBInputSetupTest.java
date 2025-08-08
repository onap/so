/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

package org.onap.so.bpmn.servicedecomposition.tasks;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.onap.so.bpmn.servicedecomposition.tasks.BaseBBInputSetupTestHelper.prepareConfigurationResourceKeys;
import static org.onap.so.bpmn.servicedecomposition.tasks.BaseBBInputSetupTestHelper.prepareLookupKeyMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperProvider;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Platform;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTableReference;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceProxy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Vnfc;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.entities.ServiceModel;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoCollection;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoPnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceProxy;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.NoServiceInstanceFoundException;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.ServiceModelNotFoundException;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.InstanceGroupType;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.ServiceProxyResourceCustomization;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.SubscriberInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;

@RunWith(MockitoJUnitRunner.class)
public class BBInputSetupTest {
    private static final String RESOURCE_PATH = "src/test/resources/__files/ExecuteBuildingBlock/";

    protected ObjectMapper mapper = new ObjectMapper();
    private static final String CLOUD_OWNER = "CloudOwner";
    private static final String REQUEST_ID = "b20bbd26-af25-4a50-a9fe-222a3c0f9771";

    @Spy
    @InjectMocks
    private BBInputSetup SPY_bbInputSetup = new BBInputSetup();

    @Mock
    private BBInputSetupUtils SPY_bbInputSetupUtils;

    @Mock
    private CloudInfoFromAAI SPY_cloudInfoFromAAI;

    @Spy
    private BBInputSetupMapperLayer bbInputSetupMapperLayer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testGetVolumeGroupIdRelatedToVfModule() {
        String expected = "volumeGroupId";
        String modelCustomizationId = "modelCustomizationId";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(modelCustomizationId);
        String cloudOwner = "cloudOwner";
        String cloudRegionId = "cloudRegionId";
        String volumeGroupId = "volumeGroupId";
        GenericVnf vnf = new GenericVnf();
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId(expected);
        vnf.getVolumeGroups().add(volumeGroup);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, null);
        org.onap.aai.domain.yang.VolumeGroup aaiVolumeGroup = new org.onap.aai.domain.yang.VolumeGroup();
        aaiVolumeGroup.setModelCustomizationId(modelCustomizationId);
        doReturn(aaiVolumeGroup).when(SPY_bbInputSetupUtils).getAAIVolumeGroup(cloudOwner, cloudRegionId,
                volumeGroupId);

        Optional<String> actual = SPY_bbInputSetup.getVolumeGroupIdRelatedToVfModule(vnf, modelInfo, cloudOwner,
                cloudRegionId, lookupKeyMap);

        assertEquals(expected, actual.get());
    }

    @Test
    public void testGetAlaCarteServiceInstance() throws Exception {
        ServiceInstance expected =
                mapper.readValue(new File(RESOURCE_PATH + "ServiceInstance_getServiceInstanceNOAAIExpected.json"),
                        ServiceInstance.class);
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setInstanceName("SharansInstanceName");
        requestDetails.setRequestInfo(requestInfo);
        Customer customer = new Customer();
        String serviceInstanceId = "SharansInstanceId";
        boolean aLaCarte = true;
        Service service = new Service();
        service.setModelUUID("modelUUID");
        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelUuid("modelUUID");
        expected.setModelInfoServiceInstance(modelInfoServiceInstance);
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
        serviceInstanceAAI.setModelVersionId("modelUUIDDifferent");
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String bbName = AssignFlows.SERVICE_INSTANCE.toString();
        Service differentService = new Service();
        differentService.setModelUUID("modelUUIDDifferent");

        doReturn(expected).when(SPY_bbInputSetup).getServiceInstanceHelper(requestDetails, customer, null, null,
                lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
        doReturn(serviceInstanceAAI).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById(serviceInstanceId);
        doReturn(differentService).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(serviceInstanceAAI.getModelVersionId());
        doReturn(expected.getModelInfoServiceInstance()).when(bbInputSetupMapperLayer)
                .mapCatalogServiceIntoServiceInstance(differentService);

        ServiceInstance actual = SPY_bbInputSetup.getALaCarteServiceInstance(service, requestDetails, customer, null,
                null, lookupKeyMap, serviceInstanceId, aLaCarte, bbName);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test(expected = Exception.class)
    public void testGetAlaCarteServiceInstanceException() throws Exception {
        ServiceInstance expected =
                mapper.readValue(new File(RESOURCE_PATH + "ServiceInstance_getServiceInstanceNOAAIExpected.json"),
                        ServiceInstance.class);
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setInstanceName("SharansInstanceName");
        requestDetails.setRequestInfo(requestInfo);
        Customer customer = new Customer();
        String serviceInstanceId = "SharansInstanceId";
        boolean aLaCarte = true;
        Service service = new Service();
        service.setModelUUID("modelUUID");
        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelUuid("modelUUID");
        expected.setModelInfoServiceInstance(modelInfoServiceInstance);
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
        serviceInstanceAAI.setModelVersionId("modelUUIDDifferent");
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String bbName = AssignFlows.SERVICE_INSTANCE.toString();
        Service differentService = new Service();
        differentService.setModelUUID("modelUUIDDifferent");

        doReturn(expected).when(SPY_bbInputSetup).getServiceInstanceHelper(requestDetails, customer, null, null,
                lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
        doReturn(serviceInstanceAAI).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById(serviceInstanceId);
        doReturn(null).when(SPY_bbInputSetupUtils).getCatalogServiceByModelUUID(serviceInstanceAAI.getModelVersionId());

        ServiceInstance actual = SPY_bbInputSetup.getALaCarteServiceInstance(service, requestDetails, customer, null,
                null, lookupKeyMap, serviceInstanceId, aLaCarte, bbName);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetCustomerAndServiceSubscription() throws IOException {
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
        SubscriberInfo subscriberInfo = new SubscriberInfo();
        subscriberInfo.setGlobalSubscriberId("globalSubscriberId");
        RequestParameters requestParams = new RequestParameters();
        requestParams.setSubscriptionServiceType("subscriptionServiceType");
        requestDetails.setRequestParameters(requestParams);
        requestDetails.setSubscriberInfo(subscriberInfo);
        String resourceId = "resourceId";
        Customer expected = new Customer();
        expected.setGlobalCustomerId("globalCustomerId");
        ServiceSubscription serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType("subscriptionServiceType");

        doReturn(expected).when(this.SPY_bbInputSetup).getCustomerFromRequest(requestDetails);
        doReturn(serviceSubscription).when(this.SPY_bbInputSetup).getServiceSubscription(requestDetails, expected);

        Customer actual = this.SPY_bbInputSetup.getCustomerAndServiceSubscription(requestDetails, resourceId);

        assertThat(actual, sameBeanAs(expected));

        requestDetails.setSubscriberInfo(null);


        assertThat(actual, sameBeanAs(expected));

    }

    @Test
    public void testSetHomingFlag() throws IOException {
        GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "vnfId");
        GenericVnf genericVnfExpected = new GenericVnf();
        genericVnfExpected.setVnfId("vnfId");
        genericVnfExpected.setCallHoming(true);
        expected.getCustomer().getServiceSubscription().getServiceInstances().get(0).getVnfs().add(genericVnfExpected);
        boolean homing = true;
        GenericVnf genericVnfActual = new GenericVnf();
        genericVnfActual.setVnfId("vnfId");
        genericVnfActual.setCallHoming(false);
        GeneralBuildingBlock actual = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        actual.getCustomer().getServiceSubscription().getServiceInstances().get(0).getVnfs().add(genericVnfActual);

        SPY_bbInputSetup.setHomingFlag(actual, homing, lookupKeyMap);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetExecuteBBFromExecution() {
        BuildingBlock bb = new BuildingBlock().setBpmnFlowName("AssignServiceInstanceBB");
        ExecuteBuildingBlock expected =
                new ExecuteBuildingBlock().setBuildingBlock(bb).setRequestId("00032ab7-3fb3-42e5-965d-8ea592502017");
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        doReturn(expected).when(execution).getVariable(any(String.class));
        ExecuteBuildingBlock actual = SPY_bbInputSetup.getExecuteBBFromExecution(execution);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetGBB() throws Exception {
        GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);

        ExecuteBuildingBlock executeBB = new ExecuteBuildingBlock().setRequestId("requestId");
        RequestDetails requestDetails = new RequestDetails();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelType(ModelType.service);
        requestDetails.setModelInfo(modelInfo);
        RequestParameters requestParams = new RequestParameters();
        requestParams.setaLaCarte(true);
        requestDetails.setRequestParameters(requestParams);
        doReturn(requestDetails).when(SPY_bbInputSetupUtils).getRequestDetails(executeBB.getRequestId());
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String resourceId = "123";
        String requestAction = "createInstance";
        doReturn(expected).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, resourceId);
        doNothing().when(SPY_bbInputSetup).populateLookupKeyMapWithIds(executeBB.getWorkflowResourceIds(),
                lookupKeyMap);
        boolean aLaCarte = true;
        GeneralBuildingBlock actual =
                SPY_bbInputSetup.getGBB(executeBB, lookupKeyMap, requestAction, aLaCarte, resourceId, null);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetGBBCM() throws Exception {
        GeneralBuildingBlock expected = mapper
                .readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockCMExpected.json"), GeneralBuildingBlock.class);

        ExecuteBuildingBlock executeBB = new ExecuteBuildingBlock().setRequestId("requestId");
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setModelInfo(null);
        RequestParameters requestParams = new RequestParameters();
        requestParams.setaLaCarte(true);
        requestDetails.setRequestParameters(requestParams);
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setSuppressRollback(true);
        requestInfo.setSource("VID");
        requestDetails.setRequestInfo(requestInfo);
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setLcpCloudRegionId("myRegionId");
        requestDetails.setCloudConfiguration(cloudConfiguration);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String resourceId = "123";
        String requestAction = "createInstance";

        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "vnf-001");

        doReturn(new org.onap.aai.domain.yang.GenericVnf()).when(SPY_bbInputSetupUtils)
                .getAAIGenericVnf(ArgumentMatchers.isA(String.class));
        doReturn(null).when(bbInputSetupMapperLayer)
                .mapAAIGenericVnfIntoGenericVnf(ArgumentMatchers.isA(org.onap.aai.domain.yang.GenericVnf.class));
        GeneralBuildingBlock actual =
                SPY_bbInputSetup.getGBBCM(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetGBBCMAddMembersAction() throws Exception {
        GeneralBuildingBlock expected = mapper.readValue(
                new File(RESOURCE_PATH + "GeneralBuildingBlockInstanceGroupExpected.json"), GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_instanceGroupAddMembers.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String requestAction = "addMembers";
        String instanceGroupId = "instance-group-001";

        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setInstanceGroupId(instanceGroupId);
        executeBB.setWorkflowResourceIds(workflowResourceIds);

        lookupKeyMap.put(ResourceKey.INSTANCE_GROUP_ID, instanceGroupId);

        org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = new org.onap.aai.domain.yang.InstanceGroup();
        aaiInstanceGroup.setId(instanceGroupId);
        aaiInstanceGroup.setInstanceGroupName("test instance group 1");

        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        aaiServiceInstance.setServiceInstanceId("service-instance-001");
        aaiServiceInstance.setServiceInstanceName("test service instance 1");
        Optional<org.onap.aai.domain.yang.ServiceInstance> optSI = Optional.of(aaiServiceInstance);

        org.onap.aai.domain.yang.GenericVnf vnf1 = new org.onap.aai.domain.yang.GenericVnf();
        vnf1.setVnfId("vnf-001");
        vnf1.setVnfName("test vnf 1");

        org.onap.aai.domain.yang.GenericVnf vnf2 = new org.onap.aai.domain.yang.GenericVnf();
        vnf2.setVnfId("vnf-002");
        vnf2.setVnfName("test vnf 2");

        doReturn(aaiInstanceGroup).when(SPY_bbInputSetupUtils).getAAIInstanceGroup(instanceGroupId);
        doReturn(optSI).when(SPY_bbInputSetupUtils).getRelatedServiceInstanceFromInstanceGroup(instanceGroupId);
        doReturn(vnf1).when(SPY_bbInputSetupUtils).getAAIGenericVnf("vnf-001");
        doReturn(vnf2).when(SPY_bbInputSetupUtils).getAAIGenericVnf("vnf-002");

        GeneralBuildingBlock actual =
                SPY_bbInputSetup.getGBBCM(executeBB, requestDetails, lookupKeyMap, requestAction, instanceGroupId);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetGBBALaCarteNonService() throws Exception {
        GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setCurrentService(service);
        ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
        String resourceId = "123";
        String vnfType = "vnfType";
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        aaiServiceInstance.setModelVersionId("modelVersionId");
        org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "instanceId");
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById("instanceId");
        doNothing().when(SPY_bbInputSetup).populateObjectsOnAssignAndCreateFlows(any(BBInputSetupParameter.class));
        doReturn(serviceInstance).when(SPY_bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
        doReturn(expected).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));

        GeneralBuildingBlock actual = SPY_bbInputSetup.getGBBALaCarteNonService(executeBB, requestDetails, lookupKeyMap,
                requestAction, resourceId, vnfType);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetGBBALaCarteNonServiceIsReplace() throws Exception {
        GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String requestAction = "replaceInstance";
        Service service = Mockito.mock(Service.class);
        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setCurrentService(service);
        ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
        String resourceId = "123";
        String vnfType = "vnfType";
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        aaiServiceInstance.setModelVersionId("modelVersionId");
        org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "instanceId");
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        doReturn(service).when(SPY_bbInputSetupUtils).getCatalogServiceByModelUUID("modelUUID");
        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById("instanceId");

        doNothing().when(SPY_bbInputSetup).populateObjectsOnAssignAndCreateFlows(any(BBInputSetupParameter.class));
        doReturn(serviceInstance).when(SPY_bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
        doReturn(expected).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));

        GeneralBuildingBlock actual = SPY_bbInputSetup.getGBBALaCarteNonService(executeBB, requestDetails, lookupKeyMap,
                requestAction, resourceId, vnfType);

        verify(SPY_bbInputSetup, times(1)).populateObjectsOnAssignAndCreateFlows(any(BBInputSetupParameter.class));

        verify(SPY_bbInputSetupUtils, times(1)).getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        verify(SPY_bbInputSetupUtils, times(1)).getCatalogServiceByModelUUID("modelUUID");

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetGBBALaCarteNonServiceWithoutServiceModelInfo() throws Exception {
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "si123");
        String requestAction = "createInstance";
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        aaiServiceInstance.setModelVersionId("modelVersionId");
        String resourceId = "123";
        String vnfType = "vnfType";

        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById("si123");
        doReturn(null).when(SPY_bbInputSetupUtils).getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());

        expectedException.expect(ServiceModelNotFoundException.class);
        expectedException.expectMessage(
                "Related service instance model not found in MSO CatalogDB: model-version-id=modelVersionId");

        SPY_bbInputSetup.getGBBALaCarteNonService(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId,
                vnfType);
    }

    @Test
    public void testGetGBBALaCarteNonServiceServiceInstanceNotFoundInAAI() throws Exception {
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "si123");
        String requestAction = "createInstance";
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        aaiServiceInstance.setModelVersionId("modelVersionId");
        String resourceId = "123";
        String vnfType = "vnfType";

        doReturn(null).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById("si123");

        expectedException.expect(NoServiceInstanceFoundException.class);
        expectedException.expectMessage("Related service instance from AAI not found: service-instance-id=si123");

        SPY_bbInputSetup.getGBBALaCarteNonService(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId,
                vnfType);
    }

    @Test
    public void testGetGBBALaCarteNonServiceWithoutRelatedInstances() throws Exception {
        GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_withoutRelatedInstanceList.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setCurrentService(service);
        String resourceId = "123";
        ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        aaiServiceInstance.setModelVersionId("modelVersionId");
        org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);
        String vnfType = "vnfType";
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "instanceId");
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById("instanceId");

        doNothing().when(SPY_bbInputSetup).populateObjectsOnAssignAndCreateFlows(any(BBInputSetupParameter.class));

        doReturn(serviceInstance).when(SPY_bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
        doReturn(expected).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));

        GeneralBuildingBlock actual = SPY_bbInputSetup.getGBBALaCarteNonService(executeBB, requestDetails, lookupKeyMap,
                requestAction, resourceId, vnfType);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetGBBALaCarteService() throws Exception {
        GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();

        org.onap.so.serviceinstancebeans.Project requestProject = new org.onap.so.serviceinstancebeans.Project();
        org.onap.so.serviceinstancebeans.OwningEntity requestOwningEntity =
                new org.onap.so.serviceinstancebeans.OwningEntity();
        requestDetails.setProject(requestProject);
        requestDetails.setOwningEntity(requestOwningEntity);

        Service service = Mockito.mock(Service.class);
        Customer customer = Mockito.mock(Customer.class);
        ServiceSubscription serviceSubscription = Mockito.mock(ServiceSubscription.class);
        Project project = Mockito.mock(Project.class);
        OwningEntity owningEntity = Mockito.mock(OwningEntity.class);
        ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
        String resourceId = "123";
        String requestAction = "createInstance";
        executeBB.setaLaCarte(true);
        executeBB.getBuildingBlock().setBpmnFlowName(AssignFlows.SERVICE_INSTANCE.toString());
        org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);

        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());
        doReturn(project).when(bbInputSetupMapperLayer).mapRequestProject(requestDetails.getProject());
        doReturn(owningEntity).when(bbInputSetupMapperLayer).mapRequestOwningEntity(requestDetails.getOwningEntity());

        doReturn(customer).when(SPY_bbInputSetup).getCustomerAndServiceSubscription(requestDetails, resourceId);
        doReturn(serviceInstance).when(SPY_bbInputSetup).getALaCarteServiceInstance(service, requestDetails, customer,
                project, owningEntity, lookupKeyMap, resourceId, Boolean.TRUE.equals(executeBB.isaLaCarte()),
                executeBB.getBuildingBlock().getBpmnFlowName());
        doReturn(expected).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));

        GeneralBuildingBlock actual = SPY_bbInputSetup.getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, resourceId);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetGBBALaCarteServiceFindServiceByModelVersionId() throws Exception {
        GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();

        org.onap.so.serviceinstancebeans.Project requestProject = new org.onap.so.serviceinstancebeans.Project();
        org.onap.so.serviceinstancebeans.OwningEntity requestOwningEntity =
                new org.onap.so.serviceinstancebeans.OwningEntity();
        requestDetails.setProject(requestProject);
        requestDetails.setOwningEntity(requestOwningEntity);

        Service service = Mockito.mock(Service.class);
        Customer customer = Mockito.mock(Customer.class);
        ServiceSubscription serviceSubscription = Mockito.mock(ServiceSubscription.class);
        Project project = Mockito.mock(Project.class);
        OwningEntity owningEntity = Mockito.mock(OwningEntity.class);
        ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
        String resourceId = "123";
        String requestAction = "createInstance";
        executeBB.setaLaCarte(true);
        executeBB.getBuildingBlock().setBpmnFlowName(AssignFlows.SERVICE_INSTANCE.toString());
        org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);

        doReturn(null).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());
        doReturn(service).when(SPY_bbInputSetupUtils).getCatalogServiceByModelVersionAndModelInvariantUUID(
                requestDetails.getModelInfo().getModelVersion(), requestDetails.getModelInfo().getModelInvariantId());
        doReturn(project).when(bbInputSetupMapperLayer).mapRequestProject(requestDetails.getProject());
        doReturn(owningEntity).when(bbInputSetupMapperLayer).mapRequestOwningEntity(requestDetails.getOwningEntity());

        doReturn(customer).when(SPY_bbInputSetup).getCustomerAndServiceSubscription(requestDetails, resourceId);
        doReturn(serviceInstance).when(SPY_bbInputSetup).getALaCarteServiceInstance(service, requestDetails, customer,
                project, owningEntity, lookupKeyMap, resourceId, Boolean.TRUE.equals(executeBB.isaLaCarte()),
                executeBB.getBuildingBlock().getBpmnFlowName());
        doReturn(expected).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));

        GeneralBuildingBlock actual = SPY_bbInputSetup.getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, resourceId);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetGBBALaCarteServiceNoProjectNoOE() throws Exception {
        GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();

        Service service = Mockito.mock(Service.class);
        Customer customer = Mockito.mock(Customer.class);
        ServiceSubscription serviceSubscription = Mockito.mock(ServiceSubscription.class);
        ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
        String resourceId = "123";
        String requestAction = "createInstance";
        executeBB.setaLaCarte(true);
        executeBB.getBuildingBlock().setBpmnFlowName(AssignFlows.SERVICE_INSTANCE.toString());
        org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);

        Map<String, String> uriKeys = new HashMap<>();
        uriKeys.put("global-customer-id", "globalCustomerId");
        uriKeys.put("service-type", "serviceType");

        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());

        doReturn(customer).when(SPY_bbInputSetup).getCustomerAndServiceSubscription(requestDetails, resourceId);

        doReturn(serviceInstance).when(SPY_bbInputSetup).getALaCarteServiceInstance(service, requestDetails, customer,
                null, null, lookupKeyMap, resourceId, Boolean.TRUE.equals(executeBB.isaLaCarte()),
                executeBB.getBuildingBlock().getBpmnFlowName());
        doReturn(expected).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));

        GeneralBuildingBlock actual = SPY_bbInputSetup.getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, resourceId);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetServiceInstanceHelperCreateScenario() throws Exception {
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setInstanceName("SharansInstanceName");
        requestDetails.setRequestInfo(requestInfo);
        Customer customer = new Customer();
        String serviceInstanceId = "SharansInstanceId";
        boolean aLaCarte = true;
        ServiceInstance expected = new ServiceInstance();
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        Service service = new Service();
        service.setModelUUID("modelUUID");
        String bbName = AssignFlows.SERVICE_INSTANCE.toString();

        doReturn(null).when(SPY_bbInputSetupUtils).getAAIServiceInstanceByName(requestInfo.getInstanceName(), customer);
        doReturn(null).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById(serviceInstanceId);


        doReturn(expected).when(SPY_bbInputSetup).createServiceInstance(requestDetails, null, null, lookupKeyMap,
                serviceInstanceId);

        ServiceInstance actual = SPY_bbInputSetup.getServiceInstanceHelper(requestDetails, customer, null, null,
                lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetServiceInstanceHelperCreateScenarioExisting() throws Exception {
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setInstanceName("SharansInstanceName");
        requestDetails.setRequestInfo(requestInfo);
        Customer customer = new Customer();
        String serviceInstanceId = "SharansInstanceId";
        boolean aLaCarte = true;
        Service service = new Service();
        service.setModelUUID("modelUUID");
        ServiceInstance expected = new ServiceInstance();
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
        serviceInstanceAAI.setModelVersionId("modelUUID");
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String bbName = AssignFlows.SERVICE_INSTANCE.toString();

        doReturn(serviceInstanceAAI).when(SPY_bbInputSetupUtils)
                .getAAIServiceInstanceByName(requestInfo.getInstanceName(), customer);
        doReturn(expected).when(SPY_bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);

        ServiceInstance actual = SPY_bbInputSetup.getServiceInstanceHelper(requestDetails, customer, null, null,
                lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetServiceInstanceHelperCreateScenarioExistingNoNameButWithId() throws Exception {
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setInstanceName("SharansInstanceName");
        requestDetails.setRequestInfo(requestInfo);
        Customer customer = new Customer();
        String serviceInstanceId = "SharansInstanceId";
        boolean aLaCarte = true;
        Service service = new Service();
        service.setModelUUID("modelUUID");
        ServiceInstance expected = new ServiceInstance();
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
        serviceInstanceAAI.setModelVersionId("modelUUID");
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String bbName = "ActivateServiceInstanceBB";

        doReturn(serviceInstanceAAI).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById(serviceInstanceId);
        doReturn(expected).when(SPY_bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);

        ServiceInstance actual = SPY_bbInputSetup.getServiceInstanceHelper(requestDetails, customer, null, null,
                lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetServiceInstanceHelperCreateScenarioExistingNoNameButWithIdDifferentModel() throws Exception {
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestDetails.setRequestInfo(requestInfo);
        Customer customer = new Customer();
        String serviceInstanceId = "SharansInstanceId";
        boolean aLaCarte = true;
        Service service = new Service();
        service.setModelUUID("modelUUID");
        ServiceInstance expected = new ServiceInstance();
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
        serviceInstanceAAI.setModelVersionId("modelUUIDDifferent");
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String bbName = "ActivateServiceInstanceBB";
        Service differentService = new Service();
        differentService.setModelUUID("modelUUIDDifferent");

        doReturn(serviceInstanceAAI).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById(serviceInstanceId);


        ServiceInstance actual = SPY_bbInputSetup.getServiceInstanceHelper(requestDetails, customer, null, null,
                lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetServiceInstanceHelperCreateScenarioExistingWithName() throws Exception {
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestDetails.setRequestInfo(requestInfo);
        ServiceSubscription serviceSub = new ServiceSubscription();
        Customer customer = new Customer();
        customer.setServiceSubscription(serviceSub);
        ServiceInstance expected = new ServiceInstance();
        expected.setServiceInstanceId("serviceInstanceId");
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId("serviceInstanceId");

        doReturn(serviceInstanceAAI).when(SPY_bbInputSetupUtils).getAAIServiceInstanceByIdAndCustomer(Mockito.any(),
                Mockito.any(), Mockito.any());
        ServiceInstance actual = SPY_bbInputSetup.getServiceInstanceHelper(requestDetails, customer, null, null,
                new HashMap<>(), "SharansInstanceId", false, new Service(), "ActivateServiceInstanceBB");
        assertThat(actual, sameBeanAs(expected));
    }

    @Test(expected = Exception.class)
    public void testGetServiceInstanceHelperCreateScenarioExistingNoNameButWithIdExceptionThrown() throws Exception {
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestDetails.setRequestInfo(requestInfo);
        Customer customer = new Customer();
        String serviceInstanceId = "SharansInstanceId";
        boolean aLaCarte = true;
        Service service = new Service();
        service.setModelUUID("modelUUID");
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
        serviceInstanceAAI.setModelVersionId("modelUUIDDifferent");
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String bbName = AssignFlows.SERVICE_INSTANCE.toString();

        ServiceInstance actual = SPY_bbInputSetup.getServiceInstanceHelper(requestDetails, customer, null, null,
                lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
    }

    @Test
    public void testPopulateObjectsOnAssignAndCreateFlows() throws Exception {
        String bbName = AssignFlows.SERVICE_INSTANCE.toString();
        String instanceName = "instanceName";
        String vnfType = "vnfType";
        String resourceId = "networkId";
        String productFamilyId = "productFamilyId";
        String applicationId = "applicationId";
        Service service = Mockito.mock(Service.class);
        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setCurrentService(service);
        ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
        RequestDetails requestDetails = Mockito.mock(RequestDetails.class);
        ModelInfo modelInfo = Mockito.mock(ModelInfo.class);
        RequestInfo requestInfo = Mockito.mock(RequestInfo.class);
        RelatedInstanceList[] relatedInstanceList = new RelatedInstanceList[] {};
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        org.onap.so.serviceinstancebeans.Platform platform =
                Mockito.mock(org.onap.so.serviceinstancebeans.Platform.class);
        org.onap.so.serviceinstancebeans.LineOfBusiness lineOfBusiness =
                Mockito.mock(org.onap.so.serviceinstancebeans.LineOfBusiness.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();

        doReturn(modelInfo).when(requestDetails).getModelInfo();
        doReturn(productFamilyId).when(requestInfo).getProductFamilyId();
        doReturn(requestInfo).when(requestDetails).getRequestInfo();
        doReturn(instanceName).when(requestInfo).getInstanceName();
        doReturn(platform).when(requestDetails).getPlatform();
        doReturn(lineOfBusiness).when(requestDetails).getLineOfBusiness();
        doReturn(relatedInstanceList).when(requestDetails).getRelatedInstanceList();
        doReturn(cloudConfiguration).when(requestDetails).getCloudConfiguration();
        doReturn(applicationId).when(requestInfo).getApplicationId();

        doReturn(ModelType.network).when(modelInfo).getModelType();
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestId(REQUEST_ID)
                .setRequestDetails(requestDetails).setService(service).setBbName(bbName)
                .setServiceInstance(serviceInstance).setLookupKeyMap(lookupKeyMap).setResourceId(resourceId)
                .setVnfType(vnfType).setIsReplace(false).setApplicationId(applicationId).build();

        doNothing().when(SPY_bbInputSetup).populateL3Network(parameter);
        SPY_bbInputSetup.populateObjectsOnAssignAndCreateFlows(parameter);
        parameter.setInstanceName(instanceName);
        parameter.setModelInfo(modelInfo);
        verify(SPY_bbInputSetup, times(1)).populateL3Network(parameter);
        assertEquals("NetworkId populated", true,
                lookupKeyMap.get(ResourceKey.NETWORK_ID).equalsIgnoreCase(resourceId));

        doReturn(ModelType.vnf).when(modelInfo).getModelType();
        resourceId = "vnfId";
        doNothing().when(SPY_bbInputSetup).populateGenericVnf(parameter);
        parameter.setResourceId(resourceId);
        SPY_bbInputSetup.populateObjectsOnAssignAndCreateFlows(parameter);
        verify(SPY_bbInputSetup, times(1)).populateGenericVnf(parameter);
        assertEquals("VnfId populated", true,
                lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID).equalsIgnoreCase(resourceId));
        doReturn(ModelType.volumeGroup).when(modelInfo).getModelType();
        resourceId = "volumeGroupId";
        parameter.setResourceId(resourceId);
        doNothing().when(SPY_bbInputSetup).populateVolumeGroup(parameter);
        SPY_bbInputSetup.populateObjectsOnAssignAndCreateFlows(parameter);
        verify(SPY_bbInputSetup, times(1)).populateVolumeGroup(parameter);
        assertEquals("VolumeGroupId populated", true,
                lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID).equalsIgnoreCase(resourceId));

        doReturn(ModelType.vfModule).when(modelInfo).getModelType();
        resourceId = "vfModuleId";
        parameter.setResourceId(resourceId);
        doNothing().when(SPY_bbInputSetup).populateVfModule(parameter);
        SPY_bbInputSetup.populateObjectsOnAssignAndCreateFlows(parameter);
        verify(SPY_bbInputSetup, times(1)).populateVfModule(parameter);
        assertEquals("VfModuleId populated", true,
                lookupKeyMap.get(ResourceKey.VF_MODULE_ID).equalsIgnoreCase(resourceId));

        String bbNameAssignVolume = AssignFlows.VOLUME_GROUP.toString();
        doReturn(ModelType.vfModule).when(modelInfo).getModelType();
        resourceId = "vfModuleId";
        parameter.setResourceId(resourceId);
        parameter.setBbName(bbNameAssignVolume);
        doNothing().when(SPY_bbInputSetup).populateVolumeGroup(parameter);
        SPY_bbInputSetup.populateObjectsOnAssignAndCreateFlows(parameter);
        verify(SPY_bbInputSetup, times(2)).populateVolumeGroup(parameter);
        assertEquals(resourceId, lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID));
    }

    @Test
    public void testPopulateGBBWithSIAndAdditionalInfo() throws Exception {
        GeneralBuildingBlock expected =
                mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpectedWUserParamsInfo.json"),
                        GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
        RequestContext requestContext =
                mapper.readValue(new File(RESOURCE_PATH + "RequestContextExpected.json"), RequestContext.class);
        ServiceInstance serviceInstance =
                mapper.readValue(new File(RESOURCE_PATH + "ServiceInstance_getServiceInstanceNOAAIExpected.json"),
                        ServiceInstance.class);
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setTenantId("tenantId");
        requestDetails.setCloudConfiguration(cloudConfiguration);
        OrchestrationContext orchestrationContext = new OrchestrationContext();
        orchestrationContext.setIsRollbackEnabled(false);

        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setCloudOwner("test-owner-name");
        cloudRegion.setLcpCloudRegionId("lcpCloudRegionId");
        cloudRegion.setComplex("complexName");
        cloudRegion.setTenantId("tenantId");

        Map<String, String> uriKeys = new HashMap<>();
        uriKeys.put(AAIFluentTypeBuilder.Types.CUSTOMER.getUriParams().globalCustomerId, "global-customer-id");
        uriKeys.put(AAIFluentTypeBuilder.Types.SERVICE_SUBSCRIPTION.getUriParams().serviceType, "service-type");

        Customer customer = new Customer();
        ServiceSubscription serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType("subscriptionServiceType");
        customer.setGlobalCustomerId("globalCustomerId");
        customer.setSubscriberName("subscriberName");
        customer.setSubscriberType("subscriberType");
        customer.setServiceSubscription(serviceSubscription);

        org.onap.so.bpmn.servicedecomposition.bbobjects.Tenant tenant =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.Tenant();
        tenant.setTenantContext("tenantContext");
        tenant.setTenantId("tenantId");
        tenant.setTenantName("tenantName");

        org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);
        org.onap.aai.domain.yang.Tenants aaiTenants = Mockito.mock(org.onap.aai.domain.yang.Tenants.class);
        org.onap.aai.domain.yang.Tenant aaiTenant = new org.onap.aai.domain.yang.Tenant();
        aaiTenant.setTenantId("tenantId");
        List<org.onap.aai.domain.yang.Tenant> tenants = new ArrayList<>();
        tenants.add(aaiTenant);

        String requestAction = "createInstance";

        doReturn(uriKeys).when(SPY_bbInputSetupUtils)
                .getURIKeysFromServiceInstance(serviceInstance.getServiceInstanceId());
        doReturn(customer).when(SPY_bbInputSetup).mapCustomer(uriKeys.get("global-customer-id"),
                uriKeys.get("service-type"));
        doReturn(aaiCloudRegion).when(SPY_bbInputSetupUtils).getCloudRegion(requestDetails.getCloudConfiguration());
        doReturn(orchestrationContext).when(bbInputSetupMapperLayer).mapOrchestrationContext(requestDetails);
        doReturn(requestContext).when(bbInputSetupMapperLayer).mapRequestContext(requestDetails);
        doReturn(cloudRegion).when(bbInputSetupMapperLayer).mapCloudRegion(requestDetails.getCloudConfiguration(),
                aaiCloudRegion);
        doReturn(tenant).when(bbInputSetupMapperLayer).mapTenant(aaiTenant);
        doReturn(aaiTenants).when(aaiCloudRegion).getTenants();
        doReturn(tenants).when(aaiTenants).getTenant();
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setServiceInstance(serviceInstance)
                .setRequestDetails(requestDetails).setExecuteBB(executeBB).setRequestAction(requestAction).build();
        GeneralBuildingBlock actual = SPY_bbInputSetup.populateGBBWithSIAndAdditionalInfo(parameter);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetServiceInstanceNOAAI() throws Exception {
        ServiceInstance expected =
                mapper.readValue(new File(RESOURCE_PATH + "ServiceInstance_getServiceInstanceNOAAIExpected.json"),
                        ServiceInstance.class);
        Service service = mapper.readValue(
                new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
        Customer customer = mapper.readValue(new File(RESOURCE_PATH + "Customer.json"), Customer.class);
        Project project = mapper.readValue(new File(RESOURCE_PATH + "Project.json"), Project.class);
        OwningEntity owningEntity = mapper.readValue(new File(RESOURCE_PATH + "OwningEntity.json"), OwningEntity.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();

        BuildingBlock buildingBlock = new BuildingBlock().setBpmnFlowName(AssignFlows.SERVICE_INSTANCE.toString());
        ExecuteBuildingBlock executeBB = new ExecuteBuildingBlock().setaLaCarte(true).setBuildingBlock(buildingBlock);
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setInstanceName("serviceInstanceName");
        requestDetails.setRequestInfo(reqInfo);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelType(ModelType.service);
        requestDetails.setModelInfo(modelInfo);
        doReturn(null).when(SPY_bbInputSetupUtils)
                .getAAIServiceInstanceByName(requestDetails.getRequestInfo().getInstanceName(), customer);
        doReturn(expected.getModelInfoServiceInstance()).when(bbInputSetupMapperLayer)
                .mapCatalogServiceIntoServiceInstance(service);
        doReturn(null).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById(any(String.class));
        String serviceInstanceId = "3655a595-05d1-433c-93c0-3afd6b572545";
        boolean aLaCarte = true;

        ServiceInstance actual =
                SPY_bbInputSetup.getALaCarteServiceInstance(service, requestDetails, customer, project, owningEntity,
                        lookupKeyMap, serviceInstanceId, aLaCarte, executeBB.getBuildingBlock().getBpmnFlowName());

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetServiceSubscription() {
        ServiceSubscription expected = new ServiceSubscription();
        RequestDetails requestDetails = new RequestDetails();
        RequestParameters params = new RequestParameters();
        params.setSubscriptionServiceType("subscriptionServiceType");
        requestDetails.setRequestParameters(params);
        org.onap.aai.domain.yang.ServiceSubscription aaiServiceSubscription =
                new org.onap.aai.domain.yang.ServiceSubscription();
        Customer customer = new Customer();
        customer.setGlobalCustomerId("globalCustomerId");
        doReturn(aaiServiceSubscription).when(SPY_bbInputSetupUtils).getAAIServiceSubscription(
                customer.getGlobalCustomerId(), requestDetails.getRequestParameters().getSubscriptionServiceType());
        doReturn(expected).when(bbInputSetupMapperLayer).mapAAIServiceSubscription(aaiServiceSubscription);

        ServiceSubscription actual = SPY_bbInputSetup.getServiceSubscription(requestDetails, customer);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetCustomer() {
        Customer expected = new Customer();
        RequestDetails requestDetails = new RequestDetails();
        SubscriberInfo subscriberInfo = new SubscriberInfo();
        subscriberInfo.setGlobalSubscriberId("globalSubscriberId");
        requestDetails.setSubscriberInfo(subscriberInfo);
        org.onap.aai.domain.yang.Customer aaiCustomer = new org.onap.aai.domain.yang.Customer();
        doReturn(aaiCustomer).when(SPY_bbInputSetupUtils)
                .getAAICustomer(requestDetails.getSubscriberInfo().getGlobalSubscriberId());
        doReturn(expected).when(bbInputSetupMapperLayer).mapAAICustomer(aaiCustomer);

        Customer actual = SPY_bbInputSetup.getCustomerFromRequest(requestDetails);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetExistingServiceInstance() throws Exception {
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
        ServiceInstance expected = new ServiceInstance();

        doReturn(expected).when(bbInputSetupMapperLayer).mapAAIServiceInstanceIntoServiceInstance(serviceInstanceAAI);


        ServiceInstance actual = SPY_bbInputSetup.getExistingServiceInstance(serviceInstanceAAI);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testPopulateNetworkCollectionAndInstanceGroupAssign() throws Exception {
        Service service = Mockito.mock(Service.class);
        String key = "collectionCustId";
        ServiceInstance serviceInstance =
                mapper.readValue(new File(RESOURCE_PATH + "ServiceInstance_getServiceInstanceNOAAIExpected.json"),
                        ServiceInstance.class);
        String resourceId = "123";
        Collection collection = SPY_bbInputSetup.createCollection(resourceId);
        InstanceGroup instanceGroup = SPY_bbInputSetup.createInstanceGroup();
        assertNull(serviceInstance.getCollection());
        doReturn(true).when(SPY_bbInputSetup).isVlanTagging(service, key);

        doReturn(collection).when(SPY_bbInputSetup).createCollection(resourceId);
        doReturn(instanceGroup).when(SPY_bbInputSetup).createInstanceGroup();
        doNothing().when(SPY_bbInputSetup).mapCatalogCollection(service, collection, key);

        NetworkCollectionResourceCustomization networkCollection = new NetworkCollectionResourceCustomization();
        networkCollection.setModelCustomizationUUID(key);
        networkCollection.setCollectionResource(new CollectionResource());
        networkCollection.getCollectionResource().setInstanceGroup(new org.onap.so.db.catalog.beans.InstanceGroup());
        networkCollection.getCollectionResource().getInstanceGroup().setToscaNodeType("NetworkCollectionResource");
        networkCollection.getCollectionResource().getInstanceGroup().setType(InstanceGroupType.L3_NETWORK);
        networkCollection.getCollectionResource().getInstanceGroup()
                .setCollectionInstanceGroupCustomizations(new ArrayList<>());
        List<CollectionResourceCustomization> customizations = new ArrayList<>();
        customizations.add(networkCollection);
        doReturn(customizations).when(service).getCollectionResourceCustomizations();
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setService(service)
                .setServiceInstance(serviceInstance).setResourceId(resourceId).setKey(key)
                .setBbName(AssignFlows.NETWORK_COLLECTION.toString()).build();

        SPY_bbInputSetup.populateNetworkCollectionAndInstanceGroupAssign(parameter);
        assertNotNull(serviceInstance.getCollection());
        assertNotNull(serviceInstance.getCollection().getInstanceGroup());

        verify(SPY_bbInputSetup, times(1)).mapCatalogCollection(service, serviceInstance.getCollection(), key);
        verify(SPY_bbInputSetup, times(1)).mapCatalogNetworkCollectionInstanceGroup(service,
                serviceInstance.getCollection().getInstanceGroup(), key);
    }

    @Test
    public void testPopulateInstanceGroup() {
        ModelInfo modelInfo = Mockito.mock(ModelInfo.class);
        Service service = Mockito.mock(Service.class);
        List<InstanceGroup> instanceGroups = Mockito.spy(new ArrayList<>());
        ServiceInstance serviceInstance = Mockito.spy(new ServiceInstance());
        serviceInstance.setServiceInstanceId("si-001");
        serviceInstance.setServiceInstanceName("test service instance");
        serviceInstance.setInstanceGroups(instanceGroups);

        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setModelInfo(modelInfo)
                .setService(service).setServiceInstance(serviceInstance).setResourceId("instance-group-001")
                .setInstanceName("test instance group").build();
        SPY_bbInputSetup.populateInstanceGroup(parameter);
        verify(SPY_bbInputSetup, times(1)).mapCatalogInstanceGroup(isA(InstanceGroup.class), isA(ModelInfo.class),
                isA(Service.class));
        verify(instanceGroups, times(1)).add(isA(InstanceGroup.class));
    }

    @Test
    public void testIsVlanTagging() {
        boolean expected = true;
        Service service = Mockito.mock(Service.class);
        String key = "collectionCustId";
        NetworkCollectionResourceCustomization networkCollection = new NetworkCollectionResourceCustomization();
        networkCollection.setModelCustomizationUUID(key);
        networkCollection.setCollectionResource(new CollectionResource());
        networkCollection.getCollectionResource().setInstanceGroup(new org.onap.so.db.catalog.beans.InstanceGroup());
        networkCollection.getCollectionResource().getInstanceGroup()
                .setToscaNodeType("org.openecomp.resource.cr.NetworkCollectionResource1806");
        List<CollectionResourceCustomization> customizations = new ArrayList<>();
        customizations.add(networkCollection);
        doReturn(customizations).when(service).getCollectionResourceCustomizations();
        boolean actual = SPY_bbInputSetup.isVlanTagging(service, key);
        assertEquals("Is Vlan Tagging check.", expected, actual);
    }

    @Test
    public void testPopulateVolumeGroup() throws Exception {
        RequestDetails requestDetails = new RequestDetails();
        RelatedInstanceList ril = new RelatedInstanceList();
        RelatedInstance ri = new RelatedInstance();
        ModelInfo mi = new ModelInfo();
        mi.setModelType(ModelType.vnf);
        mi.setModelCustomizationUuid("vnfModelCustomizationUUID");
        mi.setModelCustomizationId("vnfModelCustomizationUUID");
        ri.setModelInfo(mi);
        ril.setRelatedInstance(ri);
        requestDetails.setRelatedInstanceList(new RelatedInstanceList[] {ril});

        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId("volumeGroupCustId");

        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setInstanceName("volumeGroupName");
        requestDetails.setModelInfo(modelInfo);
        requestDetails.setRequestInfo(reqInfo);

        ServiceInstance serviceInstance = new ServiceInstance();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("genericVnfId");

        VolumeGroup vg = new VolumeGroup();
        vg.setVolumeGroupName("volumeGroupName");
        vg.setVolumeGroupId("volumeGroupId");
        vg.setModelInfoVfModule(new ModelInfoVfModule());
        vg.getModelInfoVfModule().setModelCustomizationUUID("volumeGroupCustId");
        vnf.getVolumeGroups().add(vg);
        serviceInstance.getVnfs().add(vnf);

        Service service = mapper.readValue(
                new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "genericVnfId");

        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setCurrentService(service);
        String bbName = AssignFlows.VOLUME_GROUP.toString();
        String resourceId = "123";
        doNothing().when(SPY_bbInputSetup).mapCatalogVolumeGroup(isA(VolumeGroup.class), eq(modelInfo), eq(service),
                eq("vnfModelCustomizationUUID"));
        org.onap.aai.domain.yang.GenericVnf aaiGenericVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiGenericVnf.setModelCustomizationId("vnfModelCustomizationUUID");
        doReturn(aaiGenericVnf).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());

        lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, "volumeGroupId");
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestId(REQUEST_ID)
                .setModelInfo(modelInfo).setService(service).setBbName(bbName).setServiceInstance(serviceInstance)
                .setLookupKeyMap(lookupKeyMap).setResourceId(resourceId).setRequestDetails(requestDetails)
                .setInstanceName(reqInfo.getInstanceName()).setServiceModel(serviceModel).build();
        SPY_bbInputSetup.populateVolumeGroup(parameter);
        verify(SPY_bbInputSetup, times(1)).mapCatalogVolumeGroup(eq(vg), isA(ModelInfo.class), eq(service),
                eq("vnfModelCustomizationUUID"));
        vnf.getVolumeGroups().clear();
        SPY_bbInputSetup.populateVolumeGroup(parameter);
        verify(SPY_bbInputSetup, times(1)).mapCatalogVolumeGroup(vnf.getVolumeGroups().get(0), modelInfo, service,
                "vnfModelCustomizationUUID");
    }

    @Test
    public void testMapCatalogVolumeGroup() {
        VolumeGroup volumeGroup = new VolumeGroup();
        RequestDetails requestDetails = new RequestDetails();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationUuid("modelCustomizationUUID");
        requestDetails.setModelInfo(modelInfo);
        Service service = new Service();
        VnfResourceCustomization resourceCust = new VnfResourceCustomization();
        resourceCust.setModelCustomizationUUID("vnfModelCustomizationUUID");
        service.getVnfCustomizations().add(resourceCust);
        VfModuleCustomization vfResourceCust = new VfModuleCustomization();
        vfResourceCust.setModelCustomizationUUID("modelCustomizationUUID");
        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        resourceCust.getVfModuleCustomizations().add(vfResourceCust);

        doReturn(modelInfoVfModule).when(bbInputSetupMapperLayer).mapCatalogVfModuleToVfModule(vfResourceCust);

        SPY_bbInputSetup.mapCatalogVolumeGroup(volumeGroup, modelInfo, service, "vnfModelCustomizationUUID");

        assertEquals(modelInfoVfModule, volumeGroup.getModelInfoVfModule());
    }

    @Test
    public void testPopulateL3Network() throws IOException {
        String instanceName = "networkName";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelType(ModelType.network);

        ServiceInstance serviceInstance = new ServiceInstance();
        L3Network network = new L3Network();
        network.setNetworkId("networkId");
        network.setNetworkName("networkName");
        serviceInstance.getNetworks().add(network);
        String resourceId = "123";
        // Mock service
        Service service = mapper.readValue(
                new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.NETWORK_ID, "networkId");
        String bbName = AssignFlows.NETWORK_A_LA_CARTE.toString();

        doNothing().when(SPY_bbInputSetup).mapCatalogNetwork(network, modelInfo, service);

        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestId(REQUEST_ID)
                .setInstanceName(instanceName).setModelInfo(modelInfo).setService(service).setBbName(bbName)
                .setServiceInstance(serviceInstance).setLookupKeyMap(lookupKeyMap).setResourceId(resourceId).build();
        SPY_bbInputSetup.populateL3Network(parameter);

        lookupKeyMap.put(ResourceKey.NETWORK_ID, null);

        SPY_bbInputSetup.populateL3Network(parameter);
        verify(SPY_bbInputSetup, times(1)).mapCatalogNetwork(network, modelInfo, service);

        instanceName = "networkName2";
        L3Network network2 = SPY_bbInputSetup.createNetwork(lookupKeyMap, instanceName, resourceId, null, parameter);
        SPY_bbInputSetup.populateL3Network(parameter);
        verify(SPY_bbInputSetup, times(2)).mapCatalogNetwork(network2, modelInfo, service);
    }

    @Test
    public void testMapCatalogNetwork() {
        ModelInfoNetwork modelInfoNetwork = new ModelInfoNetwork();
        L3Network network = new L3Network();

        RequestDetails requestDetails = new RequestDetails();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationUuid("modelCustomizationUUID");
        requestDetails.setModelInfo(modelInfo);
        Service service = new Service();
        NetworkResourceCustomization resourceCust = new NetworkResourceCustomization();
        resourceCust.setModelCustomizationUUID("modelCustomizationUUID");
        service.setNetworkCustomizations(Arrays.asList(new NetworkResourceCustomization[] {resourceCust}));

        doReturn(modelInfoNetwork).when(bbInputSetupMapperLayer).mapCatalogNetworkToNetwork(resourceCust);

        SPY_bbInputSetup.mapCatalogNetwork(network, modelInfo, service);

        assertEquals(modelInfoNetwork, network.getModelInfoNetwork());
    }

    @Test
    public void testPopulateConfiguration() throws IOException {
        String instanceName = "configurationName";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationUuid("72d9d1cd-f46d-447a-abdb-451d6fb05fa9");

        ServiceInstance serviceInstance = new ServiceInstance();
        Configuration configuration = new Configuration();
        configuration.setConfigurationId("configurationId");
        configuration.setConfigurationName("configurationName");
        serviceInstance.getConfigurations().add(configuration);
        String resourceId = "configurationId";
        String vnfcName = "vnfcName";
        // Mock service
        Service service = mapper.readValue(
                new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
        ConfigurationResourceCustomization configurationCust = new ConfigurationResourceCustomization();
        configurationCust.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa9");
        service.getConfigurationCustomizations().add(configurationCust);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, "configurationId");
        String bbName = AssignFlows.FABRIC_CONFIGURATION.toString();

        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setCurrentService(service);

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        configResourceKeys.setVnfcName(vnfcName);
        Vnfc vnfc = new Vnfc();
        vnfc.setVnfcName(vnfcName);
        RequestDetails requestDetails = new RequestDetails();
        doNothing().when(SPY_bbInputSetup).mapCatalogConfiguration(configuration, modelInfo, service,
                configResourceKeys);
        doReturn(vnfc).when(SPY_bbInputSetup).getVnfcToConfiguration(vnfcName);
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestId(REQUEST_ID)
                .setModelInfo(modelInfo).setService(service).setBbName(bbName).setServiceInstance(serviceInstance)
                .setLookupKeyMap(lookupKeyMap).setResourceId(resourceId).setInstanceName(instanceName)
                .setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails)
                .setServiceModel(serviceModel).build();
        SPY_bbInputSetup.populateConfiguration(parameter);
        verify(SPY_bbInputSetup, times(1)).mapCatalogConfiguration(configuration, modelInfo, service,
                configResourceKeys);

        lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, null);

        SPY_bbInputSetup.populateConfiguration(parameter);
        verify(SPY_bbInputSetup, times(2)).mapCatalogConfiguration(configuration, modelInfo, service,
                configResourceKeys);

        instanceName = "configurationName2";
        resourceId = "resourceId2";
        lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, "configurationId2");
        parameter.setInstanceName(instanceName);
        parameter.setResourceId(resourceId);
        Configuration configuration2 = SPY_bbInputSetup.createConfiguration(lookupKeyMap, instanceName, resourceId);
        doReturn(configuration2).when(SPY_bbInputSetup).createConfiguration(lookupKeyMap, instanceName, resourceId);
        doNothing().when(SPY_bbInputSetup).mapCatalogConfiguration(configuration2, modelInfo, service,
                configResourceKeys);
        SPY_bbInputSetup.populateConfiguration(parameter);
        verify(SPY_bbInputSetup, times(1)).mapCatalogConfiguration(configuration2, modelInfo, service,
                configResourceKeys);
    }

    @Test
    public void testPopulateConfigurationReplace() throws IOException {
        String instanceName = "configurationName";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationUuid("72d9d1cd-f46d-447a-abdb-451d6fb05fa9");

        ServiceInstance serviceInstance = new ServiceInstance();
        Configuration configuration = new Configuration();
        configuration.setConfigurationId("configurationId");
        configuration.setConfigurationName("configurationName");
        serviceInstance.getConfigurations().add(configuration);
        String resourceId = "configurationId";
        String vnfcName = "vnfcName";
        // Mock service
        Service service = mapper.readValue(
                new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
        ConfigurationResourceCustomization configurationCust = new ConfigurationResourceCustomization();
        configurationCust.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa9");
        service.getConfigurationCustomizations().add(configurationCust);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, "configurationId");
        String bbName = AssignFlows.FABRIC_CONFIGURATION.toString();

        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setNewService(service);

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        configResourceKeys.setVnfcName(vnfcName);
        Vnfc vnfc = new Vnfc();
        vnfc.setVnfcName(vnfcName);
        RequestDetails requestDetails = mapper.readValue(
                new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
        doNothing().when(SPY_bbInputSetup).mapCatalogConfiguration(configuration, modelInfo, service,
                configResourceKeys);
        doReturn(vnfc).when(SPY_bbInputSetup).getVnfcToConfiguration(vnfcName);
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestId(REQUEST_ID)
                .setModelInfo(modelInfo).setService(service).setBbName(bbName).setServiceInstance(serviceInstance)
                .setLookupKeyMap(lookupKeyMap).setResourceId(resourceId).setInstanceName(instanceName)
                .setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails)
                .setServiceModel(serviceModel).setIsReplace(true).build();
        SPY_bbInputSetup.populateConfiguration(parameter);
        configResourceKeys.setVnfResourceCustomizationUUID("my-test-uuid");
        verify(SPY_bbInputSetup, times(1)).mapCatalogConfiguration(configuration, modelInfo, service,
                configResourceKeys);
    }

    @Test
    public void testMapCatalogConfiguration() {
        ModelInfoNetwork modelInfoNetwork = new ModelInfoNetwork();
        L3Network network = new L3Network();

        RequestDetails requestDetails = new RequestDetails();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationUuid("modelCustomizationUUID");
        requestDetails.setModelInfo(modelInfo);
        Service service = new Service();
        NetworkResourceCustomization resourceCust = new NetworkResourceCustomization();
        resourceCust.setModelCustomizationUUID("modelCustomizationUUID");
        service.setNetworkCustomizations(Arrays.asList(new NetworkResourceCustomization[] {resourceCust}));

        doReturn(modelInfoNetwork).when(bbInputSetupMapperLayer).mapCatalogNetworkToNetwork(resourceCust);

        SPY_bbInputSetup.mapCatalogNetwork(network, modelInfo, service);

        assertEquals(modelInfoNetwork, network.getModelInfoNetwork());
    }

    @Test
    public void testPopulateFabricConfiguration() throws IOException {
        String instanceName = "configurationName";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationUuid("72d9d1cd-f46d-447a-abdb-451d6fb05fa9");

        ServiceInstance serviceInstance = new ServiceInstance();
        Configuration configuration = new Configuration();
        configuration.setConfigurationId("configurationId");
        configuration.setConfigurationName("configurationName");
        configuration.setConfigurationSubType("Fabric Config");
        serviceInstance.getConfigurations().add(configuration);
        String resourceId = "configurationId";
        String vnfcName = "vnfcName";
        // Mock service
        Service service = mapper.readValue(
                new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, "configurationId");
        String bbName = AssignFlows.FABRIC_CONFIGURATION.toString();
        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        configResourceKeys.setVnfcName(vnfcName);
        Vnfc vnfc = new Vnfc();
        vnfc.setVnfcName(vnfcName);
        RequestDetails requestDetails = new RequestDetails();

        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setCurrentService(service);

        CvnfcConfigurationCustomization vnfVfmoduleCvnfcConfigurationCustomization =
                new CvnfcConfigurationCustomization();
        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setModelUUID("modelUUID");
        configurationResource.setModelInvariantUUID("modelInvariantUUID");
        vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationResource(configurationResource);

        doReturn(null).when(SPY_bbInputSetup).findConfigurationResourceCustomization(modelInfo, service);
        doReturn(vnfc).when(SPY_bbInputSetup).getVnfcToConfiguration(vnfcName);
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestId(REQUEST_ID)
                .setModelInfo(modelInfo).setService(service).setBbName(bbName).setServiceInstance(serviceInstance)
                .setLookupKeyMap(lookupKeyMap).setResourceId(resourceId).setInstanceName(instanceName)
                .setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails)
                .setServiceModel(serviceModel).build();
        SPY_bbInputSetup.populateConfiguration(parameter);
        verify(SPY_bbInputSetup, times(1)).mapCatalogConfiguration(configuration, modelInfo, service,
                configResourceKeys);
    }

    @Test
    public void testPopulateGenericVnf() throws IOException {
        org.onap.so.serviceinstancebeans.Platform platform = new org.onap.so.serviceinstancebeans.Platform();
        org.onap.so.serviceinstancebeans.LineOfBusiness lineOfBusiness =
                new org.onap.so.serviceinstancebeans.LineOfBusiness();
        String instanceName = "vnfName";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelType(ModelType.vnf);
        ServiceInstance serviceInstance = new ServiceInstance();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("genericVnfId");
        vnf.setVnfName("vnfName");
        serviceInstance.getVnfs().add(vnf);
        String vnfType = "vnfType";
        String applicationId = "applicationId";
        RequestDetails requestDetails =
                mapper.readValue(new File(RESOURCE_PATH + "RequestDetails_CreateVnf.json"), RequestDetails.class);

        Service service = mapper.readValue(
                new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "genericVnfId");
        String bbName = AssignFlows.VNF.toString();


        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setCurrentService(service);

        Platform expectedPlatform = new Platform();
        LineOfBusiness expectedLineOfBusiness = new LineOfBusiness();
        String resourceId = "123";
        doReturn(expectedPlatform).when(bbInputSetupMapperLayer).mapRequestPlatform(platform);
        doReturn(expectedLineOfBusiness).when(bbInputSetupMapperLayer).mapRequestLineOfBusiness(lineOfBusiness);
        org.onap.aai.domain.yang.GenericVnf vnfAAI = new org.onap.aai.domain.yang.GenericVnf();
        vnfAAI.setModelCustomizationId("modelCustId");
        doReturn(vnfAAI).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(vnf, modelInfo, service);
        org.onap.aai.domain.yang.InstanceGroup instanceGroupAAI = new org.onap.aai.domain.yang.InstanceGroup();
        doReturn(instanceGroupAAI).when(SPY_bbInputSetupUtils).getAAIInstanceGroup(any());
        org.onap.so.db.catalog.beans.InstanceGroup catalogInstanceGroup =
                new org.onap.so.db.catalog.beans.InstanceGroup();
        doReturn(catalogInstanceGroup).when(SPY_bbInputSetupUtils).getCatalogInstanceGroup(any());
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestId(REQUEST_ID)
                .setModelInfo(modelInfo).setInstanceName(instanceName).setPlatform(platform)
                .setLineOfBusiness(lineOfBusiness).setService(service).setBbName(bbName)
                .setServiceInstance(serviceInstance).setLookupKeyMap(lookupKeyMap)
                .setRelatedInstanceList(requestDetails.getRelatedInstanceList()).setResourceId(resourceId)
                .setVnfType(vnfType).setProductFamilyId(requestDetails.getRequestInfo().getProductFamilyId())
                .setServiceModel(serviceModel).build();
        SPY_bbInputSetup.populateGenericVnf(parameter);

        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, null);

        SPY_bbInputSetup.populateGenericVnf(parameter);
        verify(SPY_bbInputSetup, times(1)).mapCatalogVnf(vnf, modelInfo, service);

        instanceName = "vnfName2";
        GenericVnf vnf2 = SPY_bbInputSetup.createGenericVnf(lookupKeyMap, instanceName, platform, lineOfBusiness,
                resourceId, vnfType, null, requestDetails.getRequestInfo().getProductFamilyId(), applicationId);

        doNothing().when(SPY_bbInputSetup).mapNetworkCollectionInstanceGroup(vnf2, "{instanceGroupId}");
        doNothing().when(SPY_bbInputSetup).mapVnfcCollectionInstanceGroup(vnf2, modelInfo, service);

        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "genericVnfId2");

        SPY_bbInputSetup.populateGenericVnf(parameter);
        verify(SPY_bbInputSetup, times(2)).mapCatalogVnf(vnf2, modelInfo, service);
        verify(SPY_bbInputSetup, times(2)).mapNetworkCollectionInstanceGroup(vnf2, "{instanceGroupId}");
        verify(SPY_bbInputSetup, times(2)).mapVnfcCollectionInstanceGroup(vnf2, modelInfo, service);

        assertEquals("modelCustId", modelInfo.getModelCustomizationUuid());

    }

    @Test
    public void testPopulateGenericVnfReplace() throws IOException {
        org.onap.so.serviceinstancebeans.Platform platform = new org.onap.so.serviceinstancebeans.Platform();
        org.onap.so.serviceinstancebeans.LineOfBusiness lineOfBusiness =
                new org.onap.so.serviceinstancebeans.LineOfBusiness();
        String instanceName = "vnfName";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelType(ModelType.vnf);
        ServiceInstance serviceInstance = new ServiceInstance();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("genericVnfId");
        vnf.setVnfName("vnfName");
        serviceInstance.getVnfs().add(vnf);
        String vnfType = "vnfType";
        String applicationId = "applicationId";
        RequestDetails requestDetails =
                mapper.readValue(new File(RESOURCE_PATH + "RequestDetails_CreateVnf.json"), RequestDetails.class);

        Service service = mapper.readValue(
                new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "genericVnfId");
        String bbName = AssignFlows.VNF.toString();

        String resourceId = "123";
        org.onap.aai.domain.yang.GenericVnf vnfAAI = new org.onap.aai.domain.yang.GenericVnf();
        vnfAAI.setModelCustomizationId("modelCustId");
        doReturn(vnfAAI).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(vnf, modelInfo, service);
        org.onap.aai.domain.yang.InstanceGroup instanceGroupAAI = new org.onap.aai.domain.yang.InstanceGroup();
        doReturn(instanceGroupAAI).when(SPY_bbInputSetupUtils).getAAIInstanceGroup(any());
        org.onap.so.db.catalog.beans.InstanceGroup catalogInstanceGroup =
                new org.onap.so.db.catalog.beans.InstanceGroup();
        doReturn(catalogInstanceGroup).when(SPY_bbInputSetupUtils).getCatalogInstanceGroup(any());

        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setModelInfo(modelInfo)
                .setInstanceName(instanceName).setPlatform(platform).setLineOfBusiness(lineOfBusiness)
                .setService(service).setBbName(bbName).setServiceInstance(serviceInstance).setLookupKeyMap(lookupKeyMap)
                .setRelatedInstanceList(requestDetails.getRelatedInstanceList()).setResourceId(resourceId)
                .setVnfType(vnfType).setProductFamilyId(requestDetails.getRequestInfo().getProductFamilyId())
                .setApplicationId(applicationId).setIsReplace(true).build();

        SPY_bbInputSetup.populateGenericVnf(parameter);

        assertEquals("my-test-uuid", modelInfo.getModelCustomizationUuid());

    }

    @Test
    public void testMapVnfcCollectionInstanceGroup() {
        VnfResourceCustomization vnfResourceCust = Mockito.mock(VnfResourceCustomization.class);
        GenericVnf genericVnf = new GenericVnf();
        ModelInfo modelInfo = Mockito.mock(ModelInfo.class);
        Service service = Mockito.mock(Service.class);
        org.onap.so.db.catalog.beans.InstanceGroup instanceGroup = new org.onap.so.db.catalog.beans.InstanceGroup();
        instanceGroup.setModelUUID("modelUUID");
        List<VnfcInstanceGroupCustomization> vnfcInstanceGroups = new ArrayList<>();
        VnfcInstanceGroupCustomization vnfcInstanceGroupCust = new VnfcInstanceGroupCustomization();
        vnfcInstanceGroupCust.setInstanceGroup(instanceGroup);
        vnfcInstanceGroupCust.setFunction("function");
        vnfcInstanceGroupCust.setDescription("description");
        vnfcInstanceGroups.add(vnfcInstanceGroupCust);

        ModelInfoInstanceGroup modelInfoInstanceGroup = new ModelInfoInstanceGroup();
        modelInfoInstanceGroup.setModelUUID("modelUUID");
        doReturn(vnfResourceCust).when(SPY_bbInputSetup).getVnfResourceCustomizationFromService(modelInfo, service);
        doReturn(vnfcInstanceGroups).when(vnfResourceCust).getVnfcInstanceGroupCustomizations();
        doReturn(instanceGroup).when(SPY_bbInputSetupUtils).getCatalogInstanceGroup("modelUUID");
        doReturn(modelInfoInstanceGroup).when(bbInputSetupMapperLayer).mapCatalogInstanceGroupToInstanceGroup(null,
                instanceGroup);

        SPY_bbInputSetup.mapVnfcCollectionInstanceGroup(genericVnf, modelInfo, service);

        assertEquals("Instance Group was created", true, genericVnf.getInstanceGroups().size() == 1);
    }

    @Test
    public void testPopulateGenericVnfWhereVnfTypeIsNull()
            throws IOException {
        org.onap.so.serviceinstancebeans.Platform platform = new org.onap.so.serviceinstancebeans.Platform();
        org.onap.so.serviceinstancebeans.LineOfBusiness lineOfBusiness =
                new org.onap.so.serviceinstancebeans.LineOfBusiness();
        String instanceName = "vnfName";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelType(ModelType.vnf);

        ServiceInstance serviceInstance = new ServiceInstance();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("genericVnfId");
        vnf.setVnfName("vnfName");
        serviceInstance.getVnfs().add(vnf);
        String vnfType = null;
        RequestDetails requestDetails =
                mapper.readValue(new File(RESOURCE_PATH + "RequestDetails_CreateVnf.json"), RequestDetails.class);

        Service service = mapper.readValue(
                new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "genericVnfId");
        String bbName = AssignFlows.VNF.toString();

        Platform expectedPlatform = new Platform();
        LineOfBusiness expectedLineOfBusiness = new LineOfBusiness();
        String resourceId = "123";
        String applicationId = "applicationId";
        doReturn(expectedPlatform).when(bbInputSetupMapperLayer).mapRequestPlatform(platform);
        doReturn(expectedLineOfBusiness).when(bbInputSetupMapperLayer).mapRequestLineOfBusiness(lineOfBusiness);
        org.onap.aai.domain.yang.GenericVnf vnfAAI = new org.onap.aai.domain.yang.GenericVnf();
        vnfAAI.setModelCustomizationId("modelCustId");
        doReturn(vnfAAI).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(vnf, modelInfo, service);
        org.onap.aai.domain.yang.InstanceGroup instanceGroupAAI = new org.onap.aai.domain.yang.InstanceGroup();
        doReturn(instanceGroupAAI).when(SPY_bbInputSetupUtils).getAAIInstanceGroup(any());
        org.onap.so.db.catalog.beans.InstanceGroup catalogInstanceGroup =
                new org.onap.so.db.catalog.beans.InstanceGroup();
        doReturn(catalogInstanceGroup).when(SPY_bbInputSetupUtils).getCatalogInstanceGroup(any());
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestId(REQUEST_ID)
                .setModelInfo(modelInfo).setInstanceName(instanceName).setPlatform(platform)
                .setLineOfBusiness(lineOfBusiness).setService(service).setBbName(bbName)
                .setServiceInstance(serviceInstance).setLookupKeyMap(lookupKeyMap)
                .setRelatedInstanceList(requestDetails.getRelatedInstanceList()).setResourceId(resourceId)
                .setVnfType(vnfType).setProductFamilyId(requestDetails.getRequestInfo().getProductFamilyId()).build();
        SPY_bbInputSetup.populateGenericVnf(parameter);

        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, null);

        SPY_bbInputSetup.populateGenericVnf(parameter);
        verify(SPY_bbInputSetup, times(1)).mapCatalogVnf(vnf, modelInfo, service);

        instanceName = "vnfName2";
        GenericVnf vnf2 = SPY_bbInputSetup.createGenericVnf(lookupKeyMap, instanceName, platform, lineOfBusiness,
                resourceId, vnfType, null, requestDetails.getRequestInfo().getProductFamilyId(), applicationId);

        org.onap.aai.domain.yang.GenericVnf vnf2AAI = new org.onap.aai.domain.yang.GenericVnf();
        vnfAAI.setModelCustomizationId("modelCustId2");
        doReturn(vnf2AAI).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf2.getVnfId());
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(vnf2, modelInfo, service);
        doNothing().when(SPY_bbInputSetup).mapNetworkCollectionInstanceGroup(vnf2, "{instanceGroupId}");
        SPY_bbInputSetup.populateGenericVnf(parameter);
        verify(SPY_bbInputSetup, times(2)).mapCatalogVnf(vnf2, modelInfo, service);
        verify(SPY_bbInputSetup, times(2)).mapNetworkCollectionInstanceGroup(vnf2, "{instanceGroupId}");
        verify(SPY_bbInputSetup, times(1)).mapVnfcCollectionInstanceGroup(vnf2, modelInfo, service);
    }

    @Test
    public void testMapCatalogVnf() {
        ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
        GenericVnf genericVnf = new GenericVnf();
        RequestDetails requestDetails = new RequestDetails();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationUuid("modelCustomizationUUID");
        requestDetails.setModelInfo(modelInfo);
        Service service = new Service();
        VnfResourceCustomization resourceCust = new VnfResourceCustomization();
        resourceCust.setModelCustomizationUUID("vnfModelCustomizationUUID");
        service.getVnfCustomizations().add(resourceCust);
        resourceCust.setModelCustomizationUUID("modelCustomizationUUID");

        doReturn(modelInfoGenericVnf).when(bbInputSetupMapperLayer).mapCatalogVnfToVnf(resourceCust);

        SPY_bbInputSetup.mapCatalogVnf(genericVnf, modelInfo, service);

        assertEquals(modelInfoGenericVnf, genericVnf.getModelInfoGenericVnf());
    }

    @Test
    public void testMapCatalogPnf() {
        ModelInfoPnf modelInfoPnf = new ModelInfoPnf();
        Pnf pnf = new Pnf();
        RequestDetails requestDetails = new RequestDetails();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationUuid("modelCustomizationUUID");
        requestDetails.setModelInfo(modelInfo);
        Service service = new Service();

        PnfResourceCustomization resourceCust = new PnfResourceCustomization();
        resourceCust.setModelCustomizationUUID("pnfModelCustomizationUUID");
        service.getPnfCustomizations().add(resourceCust);
        resourceCust.setModelCustomizationUUID("modelCustomizationUUID");

        doReturn(modelInfoPnf).when(bbInputSetupMapperLayer).mapCatalogPnfToPnf(resourceCust);

        SPY_bbInputSetup.mapCatalogPnf(pnf, modelInfo, service);

        assertEquals(modelInfoPnf, pnf.getModelInfoPnf());
    }

    @Test
    public void testMapCatalogCollectionAndInstanceGroup() {
        ModelInfoCollection modelInfoCollection = new ModelInfoCollection();
        modelInfoCollection.setCollectionFunction("collectionFunction");
        modelInfoCollection.setCollectionRole("collectionRole");
        modelInfoCollection.setCollectionType("collectionType");
        modelInfoCollection.setDescription("description");
        modelInfoCollection.setModelInvariantUUID("modelInvariantUUID");
        modelInfoCollection.setQuantity(0);

        ModelInfoInstanceGroup modelInfoInstanceGroup = new ModelInfoInstanceGroup();
        modelInfoInstanceGroup.setFunction("function");
        modelInfoInstanceGroup.setInstanceGroupRole("instanceGroupRole");
        modelInfoInstanceGroup.setModelInvariantUUID("modelInvariantUUID");
        modelInfoInstanceGroup.setModelUUID("modelUUID");
        modelInfoInstanceGroup.setType("VNFC");
        modelInfoInstanceGroup.setDescription("description");

        InstanceGroup instanceGroup = new InstanceGroup();
        Collection collection = new Collection();
        collection.setInstanceGroup(instanceGroup);

        CollectionResource collectionResource = new CollectionResource();
        org.onap.so.db.catalog.beans.InstanceGroup catalogInstanceGroup =
                new org.onap.so.db.catalog.beans.InstanceGroup();
        collectionResource.setToscaNodeType("NetworkCollection");
        collectionResource.setInstanceGroup(catalogInstanceGroup);

        CollectionResourceCustomization collectionCust = new NetworkCollectionResourceCustomization();
        collectionCust.setModelCustomizationUUID("modelCustomizationUUID");
        collectionCust.setCollectionResource(collectionResource);

        Service service = new Service();
        service.getCollectionResourceCustomizations().add(collectionCust);
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setCollection(collection);

        List<CollectionResourceInstanceGroupCustomization> instanceGroupCustList = new ArrayList<>();
        CollectionResourceInstanceGroupCustomization instanceGroupCust =
                new CollectionResourceInstanceGroupCustomization();
        instanceGroupCust.setFunction("function");
        instanceGroupCust.setDescription("description");

        doReturn(modelInfoCollection).when(bbInputSetupMapperLayer).mapCatalogCollectionToCollection(collectionCust,
                collectionResource);

        doReturn(modelInfoInstanceGroup).when(bbInputSetupMapperLayer)
                .mapCatalogInstanceGroupToInstanceGroup(collectionCust, catalogInstanceGroup);

        SPY_bbInputSetup.mapCatalogCollection(service, serviceInstance.getCollection(), "modelCustomizationUUID");
        SPY_bbInputSetup.mapCatalogNetworkCollectionInstanceGroup(service,
                serviceInstance.getCollection().getInstanceGroup(), collectionCust.getModelCustomizationUUID());

        assertThat(collection.getModelInfoCollection(), sameBeanAs(modelInfoCollection));
        assertThat(instanceGroup.getModelInfoInstanceGroup(), sameBeanAs(modelInfoInstanceGroup));
    }

    @Test
    public void testAddRelationshipsToSI() throws Exception {
        ServiceInstance serviceInstance = new ServiceInstance();
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId("serviceInstanceId");

        org.onap.aai.domain.yang.RelationshipList relationshipList = new org.onap.aai.domain.yang.RelationshipList();
        org.onap.aai.domain.yang.Relationship relationship = new org.onap.aai.domain.yang.Relationship();
        relationshipList.getRelationship().add(relationship);
        serviceInstanceAAI.setRelationshipList(relationshipList);

        Map<String, String> uriKeys = new HashMap<>();
        uriKeys.put("global-customer-id", "globalCustomerId");
        uriKeys.put("service-type", "serviceType");


        doNothing().when(SPY_bbInputSetup).mapProject(any(), eq(serviceInstance));
        doNothing().when(SPY_bbInputSetup).mapOwningEntity(any(), eq(serviceInstance));
        doNothing().when(SPY_bbInputSetup).mapL3Networks(any(), eq(serviceInstance.getNetworks()));
        doNothing().when(SPY_bbInputSetup).mapGenericVnfs(any(), eq(serviceInstance.getVnfs()));
        doNothing().when(SPY_bbInputSetup).mapCollection(any(), eq(serviceInstance));

        SPY_bbInputSetup.addRelationshipsToSI(serviceInstanceAAI, serviceInstance);

        verify(SPY_bbInputSetup, times(1)).mapProject(any(), eq(serviceInstance));
        verify(SPY_bbInputSetup, times(1)).mapOwningEntity(any(), eq(serviceInstance));
        verify(SPY_bbInputSetup, times(1)).mapL3Networks(any(), eq(serviceInstance.getNetworks()));
        verify(SPY_bbInputSetup, times(1)).mapGenericVnfs(any(), eq(serviceInstance.getVnfs()));
        verify(SPY_bbInputSetup, times(1)).mapCollection(any(), eq(serviceInstance));
        verify(SPY_bbInputSetup, times(1)).mapConfigurations(any(), eq(serviceInstance.getConfigurations()));
    }

    @Test
    public void testMapConfigurations() throws JsonProcessingException {
        org.onap.aai.domain.yang.Configuration expectedAAI = new org.onap.aai.domain.yang.Configuration();
        org.onap.aai.domain.yang.RelationshipList relationshipList = new org.onap.aai.domain.yang.RelationshipList();
        org.onap.aai.domain.yang.Relationship relationship = new org.onap.aai.domain.yang.Relationship();
        relationshipList.getRelationship().add(relationship);
        expectedAAI.setRelationshipList(relationshipList);

        Configuration expected = new Configuration();
        AAIResourceUri aaiResourceUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configuration("configurationId"));
        AAIResultWrapper configurationWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

        doReturn(configurationWrapper).when(SPY_bbInputSetupUtils).getAAIResourceDepthOne(aaiResourceUri);
        doReturn(expected).when(bbInputSetupMapperLayer)
                .mapAAIConfiguration(isA(org.onap.aai.domain.yang.Configuration.class));

        List<Configuration> configurations = new ArrayList<>();

        SPY_bbInputSetup.mapConfigurations(Arrays.asList(new AAIResourceUri[] {aaiResourceUri}), configurations);

        assertEquals(expected, configurations.get(0));
    }

    @Test
    public void testMapGenericVnfs() throws JsonProcessingException {
        org.onap.aai.domain.yang.GenericVnf expectedAAI = new org.onap.aai.domain.yang.GenericVnf();
        org.onap.aai.domain.yang.RelationshipList relationshipList = new org.onap.aai.domain.yang.RelationshipList();
        org.onap.aai.domain.yang.Relationship relationship = new org.onap.aai.domain.yang.Relationship();
        relationshipList.getRelationship().add(relationship);
        expectedAAI.setRelationshipList(relationshipList);

        GenericVnf expected = new GenericVnf();
        AAIResourceUri aaiResourceUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("vnfId"));
        AAIResultWrapper vnfWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

        doReturn(vnfWrapper).when(SPY_bbInputSetupUtils).getAAIResourceDepthOne(aaiResourceUri);
        doReturn(expected).when(bbInputSetupMapperLayer)
                .mapAAIGenericVnfIntoGenericVnf(isA(org.onap.aai.domain.yang.GenericVnf.class));
        doNothing().when(SPY_bbInputSetup).mapPlatform(any(), eq(expected));
        doNothing().when(SPY_bbInputSetup).mapLineOfBusiness(any(), eq(expected));
        doReturn(new ArrayList<>()).when(SPY_bbInputSetup).mapVolumeGroups(any());

        List<GenericVnf> genericVnfs = new ArrayList<>();

        SPY_bbInputSetup.mapGenericVnfs(Arrays.asList(new AAIResourceUri[] {aaiResourceUri}), genericVnfs);

        assertEquals(expected, genericVnfs.get(0));
        verify(SPY_bbInputSetup, times(1)).mapPlatform(any(), eq(expected));
        verify(SPY_bbInputSetup, times(1)).mapLineOfBusiness(any(), eq(expected));
        verify(SPY_bbInputSetup, times(1)).mapVolumeGroups(any());
    }

    @Test
    public void testMapPnfs() throws JsonProcessingException {
        org.onap.aai.domain.yang.Pnf expectedAAI = new org.onap.aai.domain.yang.Pnf();
        org.onap.aai.domain.yang.RelationshipList relationshipList = new org.onap.aai.domain.yang.RelationshipList();
        org.onap.aai.domain.yang.Relationship relationship = new org.onap.aai.domain.yang.Relationship();
        relationshipList.getRelationship().add(relationship);
        expectedAAI.setRelationshipList(relationshipList);

        Pnf expected = new Pnf();
        AAIResourceUri aaiResourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf("pnfId"));
        AAIResultWrapper pnfWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

        doReturn(pnfWrapper).when(SPY_bbInputSetupUtils).getAAIResourceDepthOne(aaiResourceUri);
        doReturn(expected).when(bbInputSetupMapperLayer).mapAAIPnfIntoPnf(isA(org.onap.aai.domain.yang.Pnf.class));

        List<Pnf> pnfs = new ArrayList<>();

        SPY_bbInputSetup.mapPnfs(Arrays.asList(aaiResourceUri), pnfs);

        assertEquals(expected, pnfs.get(0));
    }

    @Test
    public void testMapVolumeGroups() throws JsonProcessingException {
        org.onap.aai.domain.yang.VolumeGroup expectedAAI = new org.onap.aai.domain.yang.VolumeGroup();

        VolumeGroup expected = new VolumeGroup();
        AAIResultWrapper vnfWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

        doReturn(expected).when(bbInputSetupMapperLayer)
                .mapAAIVolumeGroup(isA(org.onap.aai.domain.yang.VolumeGroup.class));

        List<VolumeGroup> volumeGroupsList =
                SPY_bbInputSetup.mapVolumeGroups(Arrays.asList(new AAIResultWrapper[] {vnfWrapper}));

        assertEquals(expected, volumeGroupsList.get(0));
    }

    @Test
    public void testMapLineOfBusiness() throws JsonProcessingException {
        org.onap.aai.domain.yang.LineOfBusiness expectedAAI = new org.onap.aai.domain.yang.LineOfBusiness();

        LineOfBusiness expected = new LineOfBusiness();
        AAIResultWrapper vnfWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

        doReturn(expected).when(bbInputSetupMapperLayer)
                .mapAAILineOfBusiness(isA(org.onap.aai.domain.yang.LineOfBusiness.class));

        GenericVnf vnf = new GenericVnf();

        SPY_bbInputSetup.mapLineOfBusiness(Arrays.asList(new AAIResultWrapper[] {vnfWrapper}), vnf);

        assertEquals(expected, vnf.getLineOfBusiness());
    }

    @Test
    public void testMapPlatform() throws JsonProcessingException {
        org.onap.aai.domain.yang.Platform expectedAAI = new org.onap.aai.domain.yang.Platform();

        Platform expected = new Platform();
        AAIResultWrapper vnfWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

        doReturn(expected).when(bbInputSetupMapperLayer).mapAAIPlatform(isA(org.onap.aai.domain.yang.Platform.class));

        GenericVnf vnf = new GenericVnf();

        SPY_bbInputSetup.mapPlatform(Arrays.asList(new AAIResultWrapper[] {vnfWrapper}), vnf);

        assertEquals(expected, vnf.getPlatform());
    }

    @Test
    public void testMapCollection() throws JsonProcessingException {
        List<AAIResultWrapper> collections = new ArrayList<>();
        ServiceInstance serviceInstance = new ServiceInstance();

        org.onap.aai.domain.yang.Collection aaiCollection = new org.onap.aai.domain.yang.Collection();
        org.onap.aai.domain.yang.RelationshipList collectionRelationshipList =
                new org.onap.aai.domain.yang.RelationshipList();
        org.onap.aai.domain.yang.Relationship collectionInstanceGroupRelationship =
                new org.onap.aai.domain.yang.Relationship();
        collectionRelationshipList.getRelationship().add(collectionInstanceGroupRelationship);
        aaiCollection.setRelationshipList(collectionRelationshipList);

        collections.add(new AAIResultWrapper(
                new AAICommonObjectMapperProvider().getMapper().writeValueAsString(aaiCollection)));

        Collection collection = new Collection();
        ModelInfoCollection modelInfoCollection = new ModelInfoCollection();
        List<InstanceGroup> instanceGroupsList = new ArrayList<>();
        InstanceGroup instanceGroup = new InstanceGroup();
        instanceGroupsList.add(instanceGroup);
        NetworkCollectionResourceCustomization networkCollectionCust =
                Mockito.mock(NetworkCollectionResourceCustomization.class);
        CollectionResource collectionResource = new CollectionResource();
        doReturn(collection).when(bbInputSetupMapperLayer)
                .mapAAICollectionIntoCollection(isA(org.onap.aai.domain.yang.Collection.class));
        doReturn(instanceGroupsList).when(SPY_bbInputSetup).mapInstanceGroups(any());
        doReturn(networkCollectionCust).when(SPY_bbInputSetupUtils)
                .getCatalogNetworkCollectionResourceCustByID(aaiCollection.getCollectionCustomizationId());
        doReturn(collectionResource).when(networkCollectionCust).getCollectionResource();
        doReturn(modelInfoCollection).when(bbInputSetupMapperLayer)
                .mapCatalogCollectionToCollection(networkCollectionCust, collectionResource);

        SPY_bbInputSetup.mapCollection(collections, serviceInstance);

        assertEquals(collection, serviceInstance.getCollection());
        assertEquals(instanceGroup, collection.getInstanceGroup());

        instanceGroupsList.clear();
        collection = new Collection();

        SPY_bbInputSetup.mapCollection(collections, serviceInstance);
        assertEquals(collection, serviceInstance.getCollection());
        assertNull(collection.getInstanceGroup());
    }

    @Test
    public void testMapL3Networks() throws JsonProcessingException {
        org.onap.aai.domain.yang.L3Network expectedAAI = new org.onap.aai.domain.yang.L3Network();
        org.onap.aai.domain.yang.RelationshipList relationshipList = new org.onap.aai.domain.yang.RelationshipList();
        org.onap.aai.domain.yang.Relationship relationship = new org.onap.aai.domain.yang.Relationship();
        relationshipList.getRelationship().add(relationship);
        expectedAAI.setRelationshipList(relationshipList);

        L3Network expected = new L3Network();
        List<L3Network> l3Networks = new ArrayList<>();
        AAIResultWrapper l3NetworksWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));
        AAIResourceUri aaiResourceUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network("networkId"));

        doReturn(l3NetworksWrapper).when(SPY_bbInputSetupUtils).getAAIResourceDepthTwo(aaiResourceUri);
        doReturn(expected).when(bbInputSetupMapperLayer).mapAAIL3Network(isA(org.onap.aai.domain.yang.L3Network.class));
        doNothing().when(SPY_bbInputSetup).mapNetworkPolicies(any(), eq(expected.getNetworkPolicies()));
        doNothing().when(SPY_bbInputSetup).mapRouteTableReferences(any(),
                eq(expected.getContrailNetworkRouteTableReferences()));

        SPY_bbInputSetup.mapL3Networks(Arrays.asList(new AAIResourceUri[] {aaiResourceUri}), l3Networks);

        assertEquals(expected, l3Networks.get(0));
        verify(SPY_bbInputSetup, times(1)).mapNetworkPolicies(any(), eq(expected.getNetworkPolicies()));
        verify(SPY_bbInputSetup, times(1)).mapRouteTableReferences(any(),
                eq(expected.getContrailNetworkRouteTableReferences()));
    }

    @Test
    public void testMapRouteTableReferences() throws JsonProcessingException {
        org.onap.aai.domain.yang.RouteTableReference expectedAAI = new org.onap.aai.domain.yang.RouteTableReference();

        RouteTableReference expected = new RouteTableReference();
        List<RouteTableReference> contrailNetworkRouteTableReferences = new ArrayList<>();
        AAIResultWrapper vnfWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

        doReturn(expected).when(bbInputSetupMapperLayer).mapAAIRouteTableReferenceIntoRouteTableReference(
                isA(org.onap.aai.domain.yang.RouteTableReference.class));

        SPY_bbInputSetup.mapRouteTableReferences(Arrays.asList(new AAIResultWrapper[] {vnfWrapper}),
                contrailNetworkRouteTableReferences);

        assertEquals(expected, contrailNetworkRouteTableReferences.get(0));
    }

    @Test
    public void testMapOwningEntity() throws JsonProcessingException {
        org.onap.aai.domain.yang.OwningEntity expectedAAI = new org.onap.aai.domain.yang.OwningEntity();

        OwningEntity expected = new OwningEntity();
        AAIResultWrapper vnfWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

        doReturn(expected).when(bbInputSetupMapperLayer)
                .mapAAIOwningEntity(isA(org.onap.aai.domain.yang.OwningEntity.class));

        ServiceInstance serviceInstance = new ServiceInstance();

        SPY_bbInputSetup.mapOwningEntity(Arrays.asList(new AAIResultWrapper[] {vnfWrapper}), serviceInstance);

        assertEquals(expected, serviceInstance.getOwningEntity());
    }

    @Test
    public void testMapProject() throws JsonProcessingException {
        org.onap.aai.domain.yang.Project expectedAAI = new org.onap.aai.domain.yang.Project();

        Project expected = new Project();
        AAIResultWrapper vnfWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

        doReturn(expected).when(bbInputSetupMapperLayer).mapAAIProject(isA(org.onap.aai.domain.yang.Project.class));

        ServiceInstance serviceInstance = new ServiceInstance();

        SPY_bbInputSetup.mapProject(Arrays.asList(new AAIResultWrapper[] {vnfWrapper}), serviceInstance);

        assertEquals(expected, serviceInstance.getProject());
    }

    @Test
    public void testMapCustomer() {
        org.onap.aai.domain.yang.Customer customerAAI = new org.onap.aai.domain.yang.Customer();
        org.onap.aai.domain.yang.ServiceSubscription serviceSubscriptionAAI =
                new org.onap.aai.domain.yang.ServiceSubscription();

        Customer expected = new Customer();
        ServiceSubscription serviceSubscription = new ServiceSubscription();

        String globalCustomerId = "globalCustomerId";
        String subscriptionServiceType = "subscriptionServiceType";

        doReturn(customerAAI).when(SPY_bbInputSetupUtils).getAAICustomer(globalCustomerId);
        doReturn(serviceSubscriptionAAI).when(SPY_bbInputSetupUtils).getAAIServiceSubscription(globalCustomerId,
                subscriptionServiceType);
        doReturn(expected).when(bbInputSetupMapperLayer).mapAAICustomer(isA(org.onap.aai.domain.yang.Customer.class));
        doReturn(serviceSubscription).when(bbInputSetupMapperLayer)
                .mapAAIServiceSubscription(isA(org.onap.aai.domain.yang.ServiceSubscription.class));

        Customer actual = SPY_bbInputSetup.mapCustomer(globalCustomerId, subscriptionServiceType);

        assertEquals(expected, actual);
        assertEquals(serviceSubscription, expected.getServiceSubscription());
    }

    @Test
    public void testPopulateLookupKeyMapWithIds() {
        Map<ResourceKey, String> expected = new HashMap<>();
        Map<ResourceKey, String> actual = new HashMap<>();
        String serviceInstanceId = "serviceInstanceId";
        String networkId = "networkId";
        String vnfId = "vnfId";
        String pnfId = "pnfId";
        String vfModuleId = "vfModuleId";
        String volumeGroupId = "volumeGroupId";
        String configurationId = "configurationId";
        String instanceGroupId = "instancegroupId";
        String vnfInstanceName = "vnfInstanceName";
        String vfModuleInstanceName = "vfModuleInstanceName";
        String childServiceInstanceId = "childServiceInstanceId";
        String childServiceInstanceName = "childServiceInstanceName";
        String pnfInstanceName = "pnfInstanceName";

        expected.put(ResourceKey.SERVICE_INSTANCE_ID, serviceInstanceId);
        expected.put(ResourceKey.NETWORK_ID, networkId);
        expected.put(ResourceKey.GENERIC_VNF_ID, vnfId);
        expected.put(ResourceKey.PNF, pnfId);
        expected.put(ResourceKey.VF_MODULE_ID, vfModuleId);
        expected.put(ResourceKey.VOLUME_GROUP_ID, volumeGroupId);
        expected.put(ResourceKey.CONFIGURATION_ID, configurationId);
        expected.put(ResourceKey.INSTANCE_GROUP_ID, instanceGroupId);
        expected.put(ResourceKey.VNF_INSTANCE_NAME, vnfInstanceName);
        expected.put(ResourceKey.VF_MODULE_INSTANCE_NAME, vfModuleInstanceName);
        expected.put(ResourceKey.CHILD_SERVICE_INSTANCE_ID, childServiceInstanceId);
        expected.put(ResourceKey.CHILD_SERVICE_INSTANCE_NAME, childServiceInstanceName);
        expected.put(ResourceKey.PNF_INSTANCE_NAME, pnfInstanceName);

        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId(serviceInstanceId);
        workflowResourceIds.setNetworkId(networkId);
        workflowResourceIds.setVnfId(vnfId);
        workflowResourceIds.setPnfId(pnfId);
        workflowResourceIds.setVfModuleId(vfModuleId);
        workflowResourceIds.setVolumeGroupId(volumeGroupId);
        workflowResourceIds.setConfigurationId(configurationId);
        workflowResourceIds.setInstanceGroupId(instanceGroupId);
        workflowResourceIds.setVnfInstanceName(vnfInstanceName);
        workflowResourceIds.setVfModuleInstanceName(vfModuleInstanceName);
        workflowResourceIds.setChildServiceInstanceId(childServiceInstanceId);
        workflowResourceIds.setChildServiceInstanceName(childServiceInstanceName);
        workflowResourceIds.setPnfInstanceName(pnfInstanceName);

        SPY_bbInputSetup.populateLookupKeyMapWithIds(workflowResourceIds, actual);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetVfModulesByInstanceName() throws IOException {
        org.onap.so.serviceinstancebeans.Service serviceMacro = mapper.readValue(
                new File(RESOURCE_PATH + "ServiceMacroVfModules.json"), org.onap.so.serviceinstancebeans.Service.class);
        Resources resources = serviceMacro.getResources();
        VfModules expectedVfModule = resources.getVnfs().get(0).getVfModules().get(2);
        assertEquals(expectedVfModule,
                SPY_bbInputSetup.getVfModulesByInstanceName("vmxnjr001_AVPN_base_vRE_BV_expansion_002", resources));
    }

    @Test
    public void testGetVfModulesByKey() throws IOException {
        org.onap.so.serviceinstancebeans.Service serviceMacro = mapper.readValue(
                new File(RESOURCE_PATH + "ServiceMacroVfModules.json"), org.onap.so.serviceinstancebeans.Service.class);
        Resources resources = serviceMacro.getResources();
        VfModules expectedVfModule = resources.getVnfs().get(0).getVfModules().get(0);
        assertEquals(expectedVfModule,
                SPY_bbInputSetup.getVfModulesByKey("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f", resources));
    }

    @Test
    public void testFindVnfsByInstanceName() throws IOException {
        org.onap.so.serviceinstancebeans.Service serviceMacro = mapper.readValue(
                new File(RESOURCE_PATH + "ServiceMacroVfModules.json"), org.onap.so.serviceinstancebeans.Service.class);
        Resources resources = serviceMacro.getResources();
        Vnfs expectedVnf = resources.getVnfs().get(0);
        assertEquals(expectedVnf, SPY_bbInputSetup.findVnfsByInstanceName("vmxnjr001", resources));
    }

    @Test
    public void testVnfsByKey() throws IOException {
        org.onap.so.serviceinstancebeans.Service serviceMacro = mapper.readValue(
                new File(RESOURCE_PATH + "ServiceMacroVfModules.json"), org.onap.so.serviceinstancebeans.Service.class);
        Resources resources = serviceMacro.getResources();
        Vnfs expectedVnf = resources.getVnfs().get(0);
        assertEquals(expectedVnf, SPY_bbInputSetup.findVnfsByKey("ab153b6e-c364-44c0-bef6-1f2982117f04", resources));
    }

    @Test
    public void testGBBMacroNoUserParamsVrfConfiguration() throws Exception {
        String resourceId = "123";
        String vnfType = "vnfType";
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacroVrf.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName("AssignVrfConfigurationBB");
        buildingBlock.setKey("72d9d1cd-f46d-447a-abdb-451d6fb05fa9");

        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());

        RelatedInstance relatedVpnBinding = new RelatedInstance();
        relatedVpnBinding.setInstanceId("vpnBindingInstanceId");
        RelatedInstance relatedLocalNetwork = new RelatedInstance();
        relatedLocalNetwork.setInstanceId("localNetworkInstanceId");
        org.onap.aai.domain.yang.VpnBinding aaiVpnBinding = new org.onap.aai.domain.yang.VpnBinding();
        aaiVpnBinding.setVpnId("vpnBindingId");
        org.onap.aai.domain.yang.L3Network aaiLocalNetwork = new org.onap.aai.domain.yang.L3Network();
        aaiLocalNetwork.setNetworkId("localNetworkId");
        Optional<org.onap.aai.domain.yang.VpnBinding> aaiAICVpnBindingOp =
                Optional.of(new org.onap.aai.domain.yang.VpnBinding());
        aaiAICVpnBindingOp.get().setVpnId("AICVpnBindingId");
        ServiceProxy proxy = new ServiceProxy();
        proxy.setType("transport");
        proxy.setServiceInstance(new ServiceInstance());
        proxy.getServiceInstance().setModelInfoServiceInstance(new ModelInfoServiceInstance());
        proxy.getServiceInstance().getModelInfoServiceInstance().setModelUuid("sourceServiceModelUUID");
        doReturn(relatedVpnBinding).when(SPY_bbInputSetupUtils).getRelatedInstanceByType(requestDetails,
                ModelType.vpnBinding);
        doReturn(relatedLocalNetwork).when(SPY_bbInputSetupUtils).getRelatedInstanceByType(requestDetails,
                ModelType.network);
        doReturn(aaiVpnBinding).when(SPY_bbInputSetupUtils).getAAIVpnBinding(relatedVpnBinding.getInstanceId());
        doReturn(aaiLocalNetwork).when(SPY_bbInputSetupUtils).getAAIL3Network(relatedLocalNetwork.getInstanceId());
        doReturn(aaiAICVpnBindingOp).when(SPY_bbInputSetupUtils).getAICVpnBindingFromNetwork(aaiLocalNetwork);
        doReturn(proxy).when(SPY_bbInputSetup).getServiceProxy(service);

        Configuration configuration = new Configuration();
        configuration.setConfigurationId("configurationId");
        gBB.getServiceInstance().getConfigurations().add(configuration);
        List<ConfigurationResourceCustomization> configurationCustList = new ArrayList<>();
        ConfigurationResourceCustomization configurationCust = new ConfigurationResourceCustomization();
        configurationCust.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa9");
        configurationCustList.add(configurationCust);
        doNothing().when(SPY_bbInputSetup).populateConfiguration(any(BBInputSetupParameter.class));

        gBB = SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        verify(SPY_bbInputSetup, times(1)).populateConfiguration(any(BBInputSetupParameter.class));
        assertEquals(gBB.getCustomer().getVpnBindings().get(0).getVpnId(), "vpnBindingId");
        assertEquals(gBB.getServiceInstance().getNetworks().get(0).getNetworkId(), "localNetworkId");
        assertEquals(gBB.getServiceInstance().getNetworks().get(0).getVpnBindings().get(0).getVpnId(),
                "AICVpnBindingId");
        assertEquals(gBB.getServiceInstance().getServiceProxies().get(0).getType(), "transport");
    }

    @Test
    public void testGetServiceProxy() {
        ServiceProxy expected = new ServiceProxy();
        expected.setType("TRANSPORT");
        expected.setModelInfoServiceProxy(new ModelInfoServiceProxy());
        expected.getModelInfoServiceProxy().setModelUuid("modelUUID");
        expected.getModelInfoServiceProxy().setModelInstanceName("modelInstanceName");
        expected.setServiceInstance(new ServiceInstance());
        expected.getServiceInstance().setModelInfoServiceInstance(new ModelInfoServiceInstance());
        expected.getServiceInstance().getModelInfoServiceInstance().setModelUuid("modelUUID");
        expected.getServiceInstance().getModelInfoServiceInstance().setServiceType("TRANSPORT");
        Service service = new Service();
        ServiceProxyResourceCustomization serviceProxyCatalog = new ServiceProxyResourceCustomization();
        serviceProxyCatalog.setModelCustomizationUUID("modelCustomizationUUID");
        serviceProxyCatalog.setModelInstanceName("modelInstanceName");
        Service sourceService = new Service();
        sourceService.setModelUUID("modelUUID");
        sourceService.setServiceType("TRANSPORT");
        serviceProxyCatalog.setSourceService(sourceService);
        service.setServiceProxyCustomizations(new ArrayList<ServiceProxyResourceCustomization>());
        service.getServiceProxyCustomizations().add(serviceProxyCatalog);
        ServiceProxy actual = SPY_bbInputSetup.getServiceProxy(service);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testgetGBBMacroWithEmptyUserParams() throws Exception {
        String resourceId = "123";
        String vnfType = "vnfType";
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().getUserParams().clear();
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName(AssignFlows.NETWORK_MACRO.toString())
                .setKey("ab153b6e-c364-44c0-bef6-1f2982117f04").setIsVirtualLink(false);

        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());

        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        verify(SPY_bbInputSetup, times(1)).getGBBMacroNoUserParamsCreate(any(ExecuteBuildingBlock.class), any(),
                any(String.class), any(String.class), any(GeneralBuildingBlock.class), any(Service.class));
    }

    @Test(expected = Exception.class)
    public void testgetGBBMacroException() throws Exception {
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        String resourceId = "123";
        String vnfType = "vnfType";
        String requestAction = "createInstance";

        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName("Network").setKey("ab153b6e-c364-44c0-bef6-1f2982117f04");

        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
    }

    @Test
    public void test_getGBBMacroForVnfTypeNull_populateL3Network_shouldBeCalled() throws Exception {
        // given
        String resourceId = "123";
        String vnfType = "null";
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName(AssignFlows.NETWORK_MACRO.toString())
                .setKey("ab153b6e-c364-44c0-bef6-1f2982117f04");

        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());
        // when
        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        // then
        verify(SPY_bbInputSetup, times(1)).populateL3Network(any(BBInputSetupParameter.class));
    }

    @Test
    public void testgetGBBMacroNoUserParamsDeactivateInstnace() throws Exception {
        String resourceId = "123";
        String vnfType = "vnfType";
        String requestAction = "deactivateInstance";
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.NETWORK_ID, "networkId");
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "vnfId");
        lookupKeyMap.put(ResourceKey.VF_MODULE_ID, "vfModuleId");
        lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, "volumeGroupId");
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "serviceInstanceId");
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName("DeactivateServiceInstanceBB").setKey("3c40d244-808e-42ca-b09a-256d83d19d0a");

        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());

        GeneralBuildingBlock actual = SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap,
                requestAction, resourceId, vnfType);
        assertThat(actual, sameBeanAs(gBB));
    }

    @Test
    public void test_getGBBMacroNoUserParamsCreate_shouldBeCalled() throws Exception {
        // given
        String resourceId = "123";
        String vnfType = "vnfType";
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        List<NetworkResourceCustomization> networkCustList = new ArrayList<>();
        NetworkResourceCustomization networkCust = Mockito.mock(NetworkResourceCustomization.class);
        networkCustList.add(networkCust);

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName(AssignFlows.NETWORK_MACRO.toString())
                .setKey("ab153b6e-c364-44c0-bef6-1f2982117f04").setIsVirtualLink(false);

        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());
        doReturn(networkCustList).when(service).getNetworkCustomizations();
        doReturn("ab153b6e-c364-44c0-bef6-1f2982117f04").when(networkCust).getModelCustomizationUUID();
        doNothing().when(SPY_bbInputSetup).populateL3Network(any(BBInputSetupParameter.class));
        // when
        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        // then
        verify(SPY_bbInputSetup, times(1)).getGBBMacroNoUserParamsCreate(executeBB, lookupKeyMap,
                executeBB.getBuildingBlock().getBpmnFlowName(), "ab153b6e-c364-44c0-bef6-1f2982117f04", gBB, service);
    }

    @Test
    public void test_getGBBMacroNoUserParamsCreate_shouldBeCalled_whenBBhasVirtualLink() throws Exception {
        // given
        String resourceId = "123";
        String vnfType = "vnfType";
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        List<NetworkResourceCustomization> networkCustList = new ArrayList<>();
        NetworkResourceCustomization networkCust = Mockito.mock(NetworkResourceCustomization.class);
        networkCustList.add(networkCust);
        CollectionNetworkResourceCustomization collectionNetworkResourceCust =
                Mockito.mock(CollectionNetworkResourceCustomization.class);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        ModelInfoNetwork modelInfoNetwork = Mockito.mock(ModelInfoNetwork.class);
        NetworkResourceCustomization networkResourceCustomization = Mockito.mock(NetworkResourceCustomization.class);

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName(AssignFlows.NETWORK_MACRO.toString())
                .setKey("ab153b6e-c364-44c0-bef6-1f2982117f04").setIsVirtualLink(true);

        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());
        doReturn(collectionNetworkResourceCust).when(SPY_bbInputSetupUtils)
                .getCatalogCollectionNetworkResourceCustByID(any(String.class));
        doReturn(networkResourceCustomization).when(bbInputSetupMapperLayer)
                .mapCollectionNetworkResourceCustToNetworkResourceCust(collectionNetworkResourceCust);
        doReturn(modelInfoNetwork).when(bbInputSetupMapperLayer)
                .mapCatalogNetworkToNetwork(networkResourceCustomization);
        // when
        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        // then
        verify(SPY_bbInputSetup, times(1)).getGBBMacroNoUserParamsCreate(executeBB, lookupKeyMap,
                executeBB.getBuildingBlock().getBpmnFlowName(), "ab153b6e-c364-44c0-bef6-1f2982117f04", gBB, service);
    }

    @Test
    public void test_getGBBMacroNoUserParamsCreate_shouldBeCalled_whenBBhasVirtualLinkAndOtherFlowName()
            throws Exception {
        // given
        String otherFlowName = "CreateNetworkBB";
        String resourceId = "123";
        String vnfType = "vnfType";
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        List<NetworkResourceCustomization> networkCustList = new ArrayList<>();
        NetworkResourceCustomization networkCust = Mockito.mock(NetworkResourceCustomization.class);
        networkCustList.add(networkCust);
        CollectionNetworkResourceCustomization collectionNetworkResourceCust =
                Mockito.mock(CollectionNetworkResourceCustomization.class);

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName(otherFlowName).setKey("ab153b6e-c364-44c0-bef6-1f2982117f04")
                .setIsVirtualLink(true);

        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());
        doReturn(collectionNetworkResourceCust).when(SPY_bbInputSetupUtils)
                .getCatalogCollectionNetworkResourceCustByID(any(String.class));
        // when
        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        // then
        verify(SPY_bbInputSetup, times(1)).getGBBMacroNoUserParamsCreate(executeBB, lookupKeyMap,
                executeBB.getBuildingBlock().getBpmnFlowName(), "ab153b6e-c364-44c0-bef6-1f2982117f04", gBB, service);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testgetGBBMacroNoUserParamsOtherException() throws Exception {
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.NETWORK_ID, "networkId");
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "vnfId");
        lookupKeyMap.put(ResourceKey.VF_MODULE_ID, "vfModuleId");
        lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, "volumeGroupId");
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "serviceInstanceId");
        String resourceId = "123";
        String vnfType = "vnfType";
        Service service = Mockito.mock(Service.class);
        String requestAction = "assignInstance";
        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());

        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
    }

    @Test
    public void test_getGBBMacroNoUserParamsExistingService_forCatalogNetwork() throws Exception {
        // given
        String requestAction = "unassignInstance";
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        L3Network network = new L3Network();
        network.setNetworkId("networkId");
        gBB.getServiceInstance().getNetworks().add(network);
        ServiceInstance serviceInstance = gBB.getServiceInstance();
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        Service service = Mockito.mock(Service.class);
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setLcpCloudRegionId("cloudRegionId");
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        org.onap.aai.domain.yang.L3Network aaiNetwork = new org.onap.aai.domain.yang.L3Network();
        aaiNetwork.setModelCustomizationId("modelCustId");

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName("DeleteNetworkBB").setKey("ab153b6e-c364-44c0-bef6-1f2982117f04");

        aaiServiceInstance.setModelVersionId("modelVersionId");
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils)
                .getAAIServiceInstanceById(lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(serviceInstance).when(SPY_bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
        doReturn(gBB).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));
        doReturn(aaiNetwork).when(SPY_bbInputSetupUtils).getAAIL3Network(network.getNetworkId());
        doNothing().when(SPY_bbInputSetup).mapCatalogNetwork(any(L3Network.class), any(ModelInfo.class),
                any(Service.class));
        // when
        SPY_bbInputSetup.getGBBMacroExistingService(executeBB, lookupKeyMap,
                executeBB.getBuildingBlock().getBpmnFlowName(), requestAction, null);
        // then
        verify(SPY_bbInputSetup, times(1)).mapCatalogNetwork(any(L3Network.class), any(ModelInfo.class),
                any(Service.class));
    }

    @Test
    public void test_getGBBMacroNoUserParamsExistingService_forControllerExecutionBB() throws Exception {
        // given
        String requestAction = "unassignInstance";
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        L3Network network = new L3Network();
        network.setNetworkId("networkId");
        gBB.getServiceInstance().getNetworks().add(network);
        ServiceInstance serviceInstance = gBB.getServiceInstance();
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        Service service = Mockito.mock(Service.class);
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setLcpCloudRegionId("cloudRegionId");
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        org.onap.aai.domain.yang.L3Network aaiNetwork = new org.onap.aai.domain.yang.L3Network();
        aaiNetwork.setModelCustomizationId("modelCustId");
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("vnfId");
        gBB.getServiceInstance().getVnfs().add(vnf);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName("ControllerExecutionBB").setKey("ab153b6e-c364-44c0-bef6-1f2982117f04");
        buildingBlock.setBpmnScope("VNF");

        aaiServiceInstance.setModelVersionId("modelVersionId");
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils)
                .getAAIServiceInstanceById(lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(serviceInstance).when(SPY_bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
        doReturn(gBB).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));
        doReturn(aaiVnf).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(any(GenericVnf.class), any(ModelInfo.class),
                any(Service.class));
        // when
        SPY_bbInputSetup.getGBBMacroExistingService(executeBB, lookupKeyMap,
                executeBB.getBuildingBlock().getBpmnFlowName(), requestAction, cloudConfiguration);
        // then
        verify(SPY_bbInputSetup, times(1)).mapCatalogVnf(any(GenericVnf.class), any(ModelInfo.class),
                any(Service.class));
    }

    @Test
    public void test_getGBBMacroNoUserParamsExistingService_forActivateVnfBB() throws Exception {
        // given
        String requestAction = "unassignInstance";
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        L3Network network = new L3Network();
        network.setNetworkId("networkId");
        gBB.getServiceInstance().getNetworks().add(network);
        ServiceInstance serviceInstance = gBB.getServiceInstance();
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        Service service = Mockito.mock(Service.class);
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setLcpCloudRegionId("cloudRegionId");
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        org.onap.aai.domain.yang.L3Network aaiNetwork = new org.onap.aai.domain.yang.L3Network();
        aaiNetwork.setModelCustomizationId("modelCustId");
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("vnfId");
        gBB.getServiceInstance().getVnfs().add(vnf);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName("ActivateVnfBB").setKey("ab153b6e-c364-44c0-bef6-1f2982117f04");

        aaiServiceInstance.setModelVersionId("modelVersionId");
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils)
                .getAAIServiceInstanceById(lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(serviceInstance).when(SPY_bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
        doReturn(gBB).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));
        doReturn(aaiVnf).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(any(GenericVnf.class), any(ModelInfo.class),
                any(Service.class));
        // when
        SPY_bbInputSetup.getGBBMacroExistingService(executeBB, lookupKeyMap,
                executeBB.getBuildingBlock().getBpmnFlowName(), requestAction, cloudConfiguration);
        // then
        verify(SPY_bbInputSetup, times(1)).mapCatalogVnf(any(GenericVnf.class), any(ModelInfo.class),
                any(Service.class));
    }

    @Test
    public void test_getGBBMacroNoUserParamsExistingService_forControllerExecutionBB_VFModule() throws Exception {
        // given
        String requestAction = "unassignInstance";
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        L3Network network = new L3Network();
        network.setNetworkId("networkId");
        gBB.getServiceInstance().getNetworks().add(network);
        ServiceInstance serviceInstance = gBB.getServiceInstance();
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        Service service = Mockito.mock(Service.class);
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setLcpCloudRegionId("cloudRegionId");
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        org.onap.aai.domain.yang.L3Network aaiNetwork = new org.onap.aai.domain.yang.L3Network();
        aaiNetwork.setModelCustomizationId("modelCustId");
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("vnfId");
        gBB.getServiceInstance().getVnfs().add(vnf);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("vfModuleId");
        gBB.getServiceInstance().getVnfs().get(0).getVfModules().add(vfModule);
        org.onap.aai.domain.yang.VfModule aaiVfModule = new org.onap.aai.domain.yang.VfModule();
        aaiVfModule.setModelCustomizationId("modelCustId");

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName("ControllerExecutionBB").setKey("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        buildingBlock.setBpmnScope("VfModule");

        aaiServiceInstance.setModelVersionId("modelVersionId");
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils)
                .getAAIServiceInstanceById(lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(serviceInstance).when(SPY_bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
        doReturn(gBB).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));
        doReturn(aaiVnf).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(any(GenericVnf.class), any(ModelInfo.class),
                any(Service.class));
        doReturn(aaiVfModule).when(SPY_bbInputSetupUtils).getAAIVfModule(vnf.getVnfId(), vfModule.getVfModuleId());
        // when
        SPY_bbInputSetup.getGBBMacroExistingService(executeBB, lookupKeyMap,
                executeBB.getBuildingBlock().getBpmnFlowName(), requestAction, cloudConfiguration);
        // then
        verify(SPY_bbInputSetup, times(1)).mapCatalogVnf(any(GenericVnf.class), any(ModelInfo.class),
                any(Service.class));
        verify(SPY_bbInputSetup, times(1)).mapCatalogVfModule(any(VfModule.class), any(ModelInfo.class),
                any(Service.class), any(String.class));
    }

    @Test
    public void test_getGBBMacroNoUserParamsExistingService_forUnassignVfModuleBB() throws Exception {
        // given
        String requestAction = "unassignInstance";
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        L3Network network = new L3Network();
        network.setNetworkId("networkId");
        gBB.getServiceInstance().getNetworks().add(network);
        ServiceInstance serviceInstance = gBB.getServiceInstance();
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        Service service = Mockito.mock(Service.class);
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setLcpCloudRegionId("cloudRegionId");
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        org.onap.aai.domain.yang.L3Network aaiNetwork = new org.onap.aai.domain.yang.L3Network();
        aaiNetwork.setModelCustomizationId("modelCustId");
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("vnfId");
        gBB.getServiceInstance().getVnfs().add(vnf);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("vfModuleId");
        gBB.getServiceInstance().getVnfs().get(0).getVfModules().add(vfModule);
        org.onap.aai.domain.yang.VfModule aaiVfModule = new org.onap.aai.domain.yang.VfModule();
        aaiVfModule.setModelCustomizationId("modelCustId");

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName("UnassignVfModuleBB").setKey("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");

        aaiServiceInstance.setModelVersionId("modelVersionId");
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils)
                .getAAIServiceInstanceById(lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(serviceInstance).when(SPY_bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
        doReturn(gBB).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));
        doReturn(aaiVnf).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(any(GenericVnf.class), any(ModelInfo.class),
                any(Service.class));
        doReturn(aaiVfModule).when(SPY_bbInputSetupUtils).getAAIVfModule(vnf.getVnfId(), vfModule.getVfModuleId());
        // when
        SPY_bbInputSetup.getGBBMacroExistingService(executeBB, lookupKeyMap,
                executeBB.getBuildingBlock().getBpmnFlowName(), requestAction, cloudConfiguration);
        // then
        verify(SPY_bbInputSetup, times(1)).mapCatalogVnf(any(GenericVnf.class), any(ModelInfo.class),
                any(Service.class));
        verify(SPY_bbInputSetup, times(1)).mapCatalogVfModule(any(VfModule.class), any(ModelInfo.class),
                any(Service.class), any(String.class));
    }

    @Test
    public void test_getGBBMacroNoUserParamsExistingService_forUnassignVolumeGroupBB() throws Exception {
        // given
        String requestAction = "unassignInstance";
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        L3Network network = new L3Network();
        network.setNetworkId("networkId");
        gBB.getServiceInstance().getNetworks().add(network);
        ServiceInstance serviceInstance = gBB.getServiceInstance();
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        Service service = Mockito.mock(Service.class);
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("vnfId");
        gBB.getServiceInstance().getVnfs().add(vnf);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("vfModuleId");
        gBB.getServiceInstance().getVnfs().get(0).getVfModules().add(vfModule);
        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setLcpCloudRegionId("cloudRegionId");
        cloudRegion.setCloudOwner("CloudOwner");
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        gBB.getServiceInstance().getVnfs().get(0).getVolumeGroups().add(volumeGroup);
        org.onap.aai.domain.yang.VolumeGroup aaiVolumeGroup = new org.onap.aai.domain.yang.VolumeGroup();
        aaiVolumeGroup.setModelCustomizationId("modelCustId");

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName("UnassignVolumeGroupBB").setKey("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");

        aaiServiceInstance.setModelVersionId("modelVersionId");
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils)
                .getAAIServiceInstanceById(lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(serviceInstance).when(SPY_bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
        doReturn(gBB).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));
        doReturn(aaiVnf).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(any(GenericVnf.class), any(ModelInfo.class),
                any(Service.class));
        doReturn(aaiVolumeGroup).when(SPY_bbInputSetupUtils).getAAIVolumeGroup(cloudRegion.getCloudOwner(),
                cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
        doReturn(Optional.of(cloudRegion)).when(SPY_cloudInfoFromAAI).getCloudInfoFromAAI(gBB.getServiceInstance());

        // when
        SPY_bbInputSetup.getGBBMacroExistingService(executeBB, lookupKeyMap,
                executeBB.getBuildingBlock().getBpmnFlowName(), requestAction, null);
        // then
        verify(SPY_bbInputSetup, times(1)).mapCatalogVnf(any(GenericVnf.class), any(ModelInfo.class),
                any(Service.class));
        verify(SPY_bbInputSetup, times(1)).mapCatalogVolumeGroup(isA(VolumeGroup.class), isA(ModelInfo.class),
                isA(Service.class), isA(String.class));
    }

    @Test
    public void test_getGBBMacroNoUserParamsExistingService_forActivateFabricConfigurationBB() throws Exception {
        // given
        String requestAction = "unassignInstance";
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        L3Network network = new L3Network();
        network.setNetworkId("networkId");
        gBB.getServiceInstance().getNetworks().add(network);
        ServiceInstance serviceInstance = gBB.getServiceInstance();
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        Service service = Mockito.mock(Service.class);
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("vnfId");
        gBB.getServiceInstance().getVnfs().add(vnf);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("vfModuleId");
        gBB.getServiceInstance().getVnfs().get(0).getVfModules().add(vfModule);
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        gBB.getServiceInstance().getVnfs().get(0).getVolumeGroups().add(volumeGroup);
        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setLcpCloudRegionId("cloudRegionId");
        Configuration configuration = new Configuration();
        configuration.setConfigurationId("configurationId");
        gBB.getServiceInstance().getConfigurations().add(configuration);
        org.onap.aai.domain.yang.Configuration aaiConfiguration = new org.onap.aai.domain.yang.Configuration();
        aaiConfiguration.setModelCustomizationId("modelCustId");

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        executeBB.setConfigurationResourceKeys(configResourceKeys).setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName("ActivateFabricConfigurationBB").setKey("72d9d1cd-f46d-447a-abdb-451d6fb05fa9");

        aaiServiceInstance.setModelVersionId("modelVersionId");
        doReturn(service).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils)
                .getAAIServiceInstanceById(lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(serviceInstance).when(SPY_bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
        doReturn(gBB).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));
        doReturn(aaiConfiguration).when(SPY_bbInputSetupUtils).getAAIConfiguration(configuration.getConfigurationId());
        doNothing().when(SPY_bbInputSetup).mapCatalogConfiguration(isA(Configuration.class), isA(ModelInfo.class),
                isA(Service.class), isA(ConfigurationResourceKeys.class));

        // when
        SPY_bbInputSetup.getGBBMacroExistingService(executeBB, lookupKeyMap,
                executeBB.getBuildingBlock().getBpmnFlowName(), requestAction, cloudConfiguration);
        // then
        verify(SPY_bbInputSetup, times(1)).mapCatalogConfiguration(any(Configuration.class), any(ModelInfo.class),
                any(Service.class), isA(ConfigurationResourceKeys.class));
    }

    @Test
    public void testGetGBBMacroExistingServiceUpgrade() throws Exception {
        String requestAction = "upgradeInstance";
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceUpgrade.json"), RequestDetails.class);
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        executeBB.setRequestDetails(requestDetails);

        doReturn(new Service()).when(SPY_bbInputSetupUtils)
                .getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());
        doReturn(new org.onap.aai.domain.yang.ServiceInstance()).when(SPY_bbInputSetupUtils)
                .getAAIServiceInstanceById(lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(gBB).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));

        SPY_bbInputSetup.getGBBMacroExistingService(executeBB, lookupKeyMap,
                executeBB.getBuildingBlock().getBpmnFlowName(), requestAction, null);

        verify(SPY_bbInputSetupUtils, times(1))
                .getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());
    }

    @Test
    public void testGetGBBMacroExistingServiceServiceinstancenotFoundInAai() throws Exception {
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "si123");

        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setLcpCloudRegionId("cloudRegionId");

        doReturn(null).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById("si123");

        expectedException.expect(NoServiceInstanceFoundException.class);
        expectedException.expectMessage("Related service instance from AAI not found: service-instance-id=si123");

        SPY_bbInputSetup.getGBBMacroExistingService(executeBB, lookupKeyMap, "AssignVnfBB", "assign",
                cloudConfiguration);
    }

    @Test
    public void testGetGBBMacroExistingServiceServiceModelNotFound() throws Exception {
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "si123");
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        aaiServiceInstance.setModelVersionId("modelVersionId");

        CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setLcpCloudRegionId("cloudRegionId");

        doReturn(aaiServiceInstance).when(SPY_bbInputSetupUtils).getAAIServiceInstanceById("si123");
        doReturn(null).when(SPY_bbInputSetupUtils).getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());

        expectedException.expect(ServiceModelNotFoundException.class);
        expectedException.expectMessage(
                "Related service instance model not found in MSO CatalogDB: model-version-id=modelVersionId");

        SPY_bbInputSetup.getGBBMacroExistingService(executeBB, lookupKeyMap, "AssignVnfBB", "assign",
                cloudConfiguration);
    }

    @Test
    public void testGetVnfId() {
        String expected = "vnfId";
        ExecuteBuildingBlock executeBB = new ExecuteBuildingBlock().setRequestId("requestId");
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        InfraActiveRequests request = new InfraActiveRequests();
        request.setVnfId(expected);
        doReturn(request).when(SPY_bbInputSetupUtils).getInfraActiveRequest(executeBB.getRequestId());

        String actual = SPY_bbInputSetup.getVnfId(executeBB, lookupKeyMap);

        assertEquals("VnfId is set correctly", expected, actual);
    }

    @Test
    public void testCreateVfModule() {
        String vfModuleId = "vfModuleId";
        String instanceName = "vfModuleName";
        Map<String, String> cloudParams = new HashMap<>();
        cloudParams.put("param1", "param1Value");
        VfModule expected = new VfModule();
        expected.setVfModuleId(vfModuleId);
        expected.setVfModuleName(instanceName);
        expected.setCloudParams(cloudParams);
        expected.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        List<Map<String, String>> instanceParams = new ArrayList<>();
        instanceParams.add(cloudParams);

        VfModule actual = SPY_bbInputSetup.createVfModule(lookupKeyMap, vfModuleId, instanceName, instanceParams);

        assertThat(actual, sameBeanAs(expected));
        assertEquals("LookupKeyMap is populated", vfModuleId, lookupKeyMap.get(ResourceKey.VF_MODULE_ID));

        expected.getCloudParams().clear();
        actual = SPY_bbInputSetup.createVfModule(lookupKeyMap, vfModuleId, instanceName, null);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testCreateVolumeGroup() {
        String volumeGroupId = "volumeGroupId";
        String instanceName = "vfModuleName";
        String vnfType = "vnfType";
        Map<String, String> cloudParams = new HashMap<>();
        cloudParams.put("param1", "param1Value");
        VolumeGroup expected = new VolumeGroup();
        expected.setVolumeGroupId(volumeGroupId);
        expected.setVolumeGroupName(instanceName);
        expected.setCloudParams(cloudParams);
        expected.setVnfType(vnfType);
        expected.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        List<Map<String, String>> instanceParams = new ArrayList<>();
        instanceParams.add(cloudParams);

        VolumeGroup actual =
                SPY_bbInputSetup.createVolumeGroup(lookupKeyMap, volumeGroupId, instanceName, vnfType, instanceParams);

        assertThat(actual, sameBeanAs(expected));
        assertEquals("LookupKeyMap is populated", volumeGroupId, lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID));

        expected.getCloudParams().clear();
        actual = SPY_bbInputSetup.createVolumeGroup(lookupKeyMap, volumeGroupId, instanceName, vnfType, null);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testCreateNetwork() {
        String networkId = "networkId";
        String instanceName = "networkName";
        Map<String, String> cloudParams = new HashMap<>();
        cloudParams.put("param1", "param1Value");
        L3Network expected = new L3Network();
        expected.setNetworkId(networkId);
        expected.setNetworkName(instanceName);
        expected.setCloudParams(cloudParams);
        Platform platform = new Platform();
        platform.setPlatformName("platformName");
        expected.setPlatform(platform);
        LineOfBusiness lineOfBusiness = new LineOfBusiness();
        lineOfBusiness.setLineOfBusinessName("lineOfBusiness");
        expected.setLineOfBusiness(lineOfBusiness);
        expected.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        List<Map<String, String>> instanceParams = new ArrayList<>();
        instanceParams.add(cloudParams);
        org.onap.so.serviceinstancebeans.Platform platformRequest = new org.onap.so.serviceinstancebeans.Platform();
        org.onap.so.serviceinstancebeans.LineOfBusiness lineOfBusinessRequest =
                new org.onap.so.serviceinstancebeans.LineOfBusiness();
        lineOfBusinessRequest.setLineOfBusinessName("lineOfBusiness");
        platformRequest.setPlatformName("platformName");
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestId(REQUEST_ID)
                .setPlatform(platformRequest).setLineOfBusiness(lineOfBusinessRequest).build();
        L3Network actual =
                SPY_bbInputSetup.createNetwork(lookupKeyMap, instanceName, networkId, instanceParams, parameter);

        assertThat(actual, sameBeanAs(expected));
        assertEquals("LookupKeyMap is populated", networkId, lookupKeyMap.get(ResourceKey.NETWORK_ID));

        expected.getCloudParams().clear();

        actual = SPY_bbInputSetup.createNetwork(lookupKeyMap, instanceName, networkId, null, parameter);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testCreateGenericVnf() {
        String vnfId = "vnfId";
        String instanceName = "vnfName";
        String vnfType = "vnfType";
        String platformName = "platformName";
        String lineOfBusinessName = "lineOfBusinessName";
        String productFamilyId = "productFamilyId";
        String applicationId = "applicationId";
        Platform platform = new Platform();
        platform.setPlatformName(platformName);
        LineOfBusiness lineOfBusiness = new LineOfBusiness();
        lineOfBusiness.setLineOfBusinessName(lineOfBusinessName);
        Map<String, String> cloudParams = new HashMap<>();
        cloudParams.put("param1", "param1Value");
        GenericVnf expected = new GenericVnf();
        expected.setVnfId(vnfId);
        expected.setVnfName(instanceName);
        expected.setVnfType(vnfType);
        expected.setCloudParams(cloudParams);
        expected.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        expected.setPlatform(platform);
        expected.setLineOfBusiness(lineOfBusiness);
        expected.setProvStatus("PREPROV");
        expected.setServiceId(productFamilyId);
        expected.setApplicationId(applicationId);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        List<Map<String, String>> instanceParams = new ArrayList<>();
        instanceParams.add(cloudParams);
        org.onap.so.serviceinstancebeans.Platform requestPlatform = new org.onap.so.serviceinstancebeans.Platform();
        platform.setPlatformName(platformName);
        org.onap.so.serviceinstancebeans.LineOfBusiness requestLineOfBusiness =
                new org.onap.so.serviceinstancebeans.LineOfBusiness();
        lineOfBusiness.setLineOfBusinessName(lineOfBusinessName);

        doReturn(platform).when(bbInputSetupMapperLayer).mapRequestPlatform(requestPlatform);
        doReturn(lineOfBusiness).when(bbInputSetupMapperLayer).mapRequestLineOfBusiness(requestLineOfBusiness);

        GenericVnf actual = SPY_bbInputSetup.createGenericVnf(lookupKeyMap, instanceName, requestPlatform,
                requestLineOfBusiness, vnfId, vnfType, instanceParams, productFamilyId, applicationId);

        assertThat(actual, sameBeanAs(expected));
        assertEquals("LookupKeyMap is populated", vnfId, lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID));

        expected.getCloudParams().clear();
        actual = SPY_bbInputSetup.createGenericVnf(lookupKeyMap, instanceName, requestPlatform, requestLineOfBusiness,
                vnfId, vnfType, null, productFamilyId, applicationId);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testMapCatalogVfModule() {
        String vnfModelCustomizationUUID = "vnfResourceCustUUID";
        String vfModuleCustomizationUUID = "vfModelCustomizationUUID";
        VfModule vfModule = new VfModule();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationUuid(vfModuleCustomizationUUID);
        Service service = new Service();
        VnfResourceCustomization vnfResourceCust = new VnfResourceCustomization();
        vnfResourceCust.setModelCustomizationUUID(vnfModelCustomizationUUID);
        VfModuleCustomization vfModuleCust = new VfModuleCustomization();
        vfModuleCust.setModelCustomizationUUID(vfModuleCustomizationUUID);
        vnfResourceCust.getVfModuleCustomizations().add(vfModuleCust);
        service.getVnfCustomizations().add(vnfResourceCust);
        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();

        doReturn(modelInfoVfModule).when(bbInputSetupMapperLayer).mapCatalogVfModuleToVfModule(vfModuleCust);

        SPY_bbInputSetup.mapCatalogVfModule(vfModule, modelInfo, service, vnfModelCustomizationUUID);

        assertThat(vfModule.getModelInfoVfModule(), sameBeanAs(modelInfoVfModule));

        modelInfo.setModelCustomizationUuid(null);
        modelInfo.setModelCustomizationId(vfModuleCustomizationUUID);

        SPY_bbInputSetup.mapCatalogVfModule(vfModule, modelInfo, service, vnfModelCustomizationUUID);

        assertThat(vfModule.getModelInfoVfModule(), sameBeanAs(modelInfoVfModule));
    }

    @Test
    public void testMapCatalogVfModuleIfNoVfUnderVnf() {
        String vnfModelCustomizationUUID = "vnfResourceCustUUID";
        String vfModuleCustomizationUUID = "vfModelCustomizationUUID";
        VfModule vfModule = new VfModule();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationUuid(vfModuleCustomizationUUID);
        Service service = new Service();
        VnfResourceCustomization vnfResourceCust = new VnfResourceCustomization();
        vnfResourceCust.setModelCustomizationUUID(vnfModelCustomizationUUID);
        VfModuleCustomization vfModuleCust = new VfModuleCustomization();
        vfModuleCust.setModelCustomizationUUID(vfModuleCustomizationUUID);
        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        doReturn(vfModuleCust).when(SPY_bbInputSetupUtils)
                .getVfModuleCustomizationByModelCuztomizationUUID(vfModuleCustomizationUUID);
        doReturn(modelInfoVfModule).when(bbInputSetupMapperLayer).mapCatalogVfModuleToVfModule(vfModuleCust);

        SPY_bbInputSetup.mapCatalogVfModule(vfModule, modelInfo, service, vnfModelCustomizationUUID);

        assertThat(vfModule.getModelInfoVfModule(), sameBeanAs(modelInfoVfModule));

        verify(SPY_bbInputSetupUtils, times(1))
                .getVfModuleCustomizationByModelCuztomizationUUID(modelInfo.getModelCustomizationId());
    }

    @Test
    public void testPopulateVfModule() throws Exception {
        String vnfId = "vnfId";
        String vfModuleId = "vfModuleId";
        String volumeGroupId = "volumeGroupId";
        String vfModuleCustomizationId = "vfModuleCustomizationId";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(vfModuleCustomizationId);
        Service service = new Service();
        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setCurrentService(service);
        String bbName = AssignFlows.VF_MODULE.toString();
        ServiceInstance serviceInstance = new ServiceInstance();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId(vnfId);
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId(volumeGroupId);
        vnf.getVolumeGroups().add(volumeGroup);
        serviceInstance.getVnfs().add(vnf);
        VfModule vfModule1 = new VfModule();
        vfModule1.setVfModuleId("vfModuleId1");
        VfModule vfModule2 = new VfModule();
        vfModule2.setVfModuleId("vfModuleId2");
        vnf.getVfModules().add(vfModule1);
        vnf.getVfModules().add(vfModule2);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, vnfId);
        String resourceId = vfModuleId;
        RelatedInstanceList[] relatedInstanceList = null;
        String instanceName = "vfModuleName";
        List<Map<String, String>> instanceParams = null;
        CloudConfiguration cloudConfiguration = new CloudConfiguration();

        org.onap.aai.domain.yang.GenericVnf vnfAAI = new org.onap.aai.domain.yang.GenericVnf();
        vnfAAI.setModelCustomizationId("vnfModelCustId");
        org.onap.aai.domain.yang.VolumeGroup volumeGroupAAI = new org.onap.aai.domain.yang.VolumeGroup();
        volumeGroupAAI.setModelCustomizationId(vfModuleCustomizationId);
        org.onap.aai.domain.yang.VfModule vfModuleAAI = new org.onap.aai.domain.yang.VfModule();
        vfModuleAAI.setModelCustomizationId(vfModuleCustomizationId);

        doReturn(vnfAAI).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doReturn(volumeGroupAAI).when(SPY_bbInputSetupUtils).getAAIVolumeGroup(CLOUD_OWNER,
                cloudConfiguration.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
        doReturn(vfModuleAAI).when(SPY_bbInputSetupUtils).getAAIVfModule(isA(String.class), isA(String.class));
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(isA(GenericVnf.class), isA(ModelInfo.class),
                isA(Service.class));
        doNothing().when(SPY_bbInputSetup).mapCatalogVfModule(isA(VfModule.class), isA(ModelInfo.class),
                isA(Service.class), isA(String.class));

        SPY_bbInputSetup.populateVfModule(new BBInputSetupParameter.Builder().setRequestId(REQUEST_ID)
                .setModelInfo(modelInfo).setService(service).setBbName(bbName).setServiceInstance(serviceInstance)
                .setLookupKeyMap(lookupKeyMap).setResourceId(resourceId).setRelatedInstanceList(relatedInstanceList)
                .setInstanceName(instanceName).setInstanceParams(instanceParams)
                .setCloudConfiguration(cloudConfiguration).setServiceModel(serviceModel).build());

        verify(SPY_bbInputSetup, times(3)).mapCatalogVfModule(isA(VfModule.class), isA(ModelInfo.class),
                isA(Service.class), isA(String.class));
        assertEquals("Lookup Key Map populated with VfModule Id", vfModuleId,
                lookupKeyMap.get(ResourceKey.VF_MODULE_ID));
        assertEquals("Lookup Key Map populated with VolumeGroup Id", volumeGroupId,
                lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID));
    }

    @Test
    public void testPopulateVfModuleIsReplaceCorrectPopulationOfVfs() throws Exception {
        String vnfId = "vnfId";
        String vfModuleId = "vfModuleId";
        String volumeGroupId = "volumeGroupId";
        String vfModuleCustomizationId = "vfModuleCustomizationId";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(vfModuleCustomizationId);
        Service currentService = new Service();
        currentService.setModelUUID("currentUUID");
        Service newService = new Service();
        newService.setModelUUID("newUUID");
        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setCurrentService(currentService);
        serviceModel.setNewService(newService);
        String bbName = "ActivateVfModuleBB";
        ServiceInstance serviceInstance = new ServiceInstance();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId(vnfId);
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId(volumeGroupId);
        vnf.getVolumeGroups().add(volumeGroup);
        serviceInstance.getVnfs().add(vnf);
        VfModule vfModule1 = new VfModule();
        vfModule1.setVfModuleId("vfModuleId1");
        VfModule vfModule2 = new VfModule();
        vfModule2.setVfModuleId("vfModuleId2");
        vnf.getVfModules().add(vfModule1);
        vnf.getVfModules().add(vfModule2);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, vnfId);
        lookupKeyMap.put(ResourceKey.VF_MODULE_ID, "vfModuleId1");
        String resourceId = vfModuleId;
        RelatedInstanceList[] relatedInstanceList = new RelatedInstanceList[1];
        RelatedInstanceList relatedInstanceListObj = new RelatedInstanceList();
        RelatedInstance relatedInstance = new RelatedInstance();
        relatedInstance.setInstanceId(vnfId);

        ModelInfo vnfModelInfo = new ModelInfo();
        vnfModelInfo.setModelCustomizationId("vnfCustomizationId");
        vnfModelInfo.setModelType(ModelType.vnf);
        relatedInstance.setModelInfo(vnfModelInfo);
        relatedInstanceListObj.setRelatedInstance(relatedInstance);
        relatedInstanceList[0] = relatedInstanceListObj;

        String instanceName = "vfModuleName";
        List<Map<String, String>> instanceParams = null;
        CloudConfiguration cloudConfiguration = new CloudConfiguration();

        org.onap.aai.domain.yang.GenericVnf vnfAAI = new org.onap.aai.domain.yang.GenericVnf();
        vnfAAI.setModelCustomizationId("vnfModelCustId");
        org.onap.aai.domain.yang.VolumeGroup volumeGroupAAI = new org.onap.aai.domain.yang.VolumeGroup();
        volumeGroupAAI.setModelCustomizationId(vfModuleCustomizationId);
        org.onap.aai.domain.yang.VfModule vfModuleAAI = new org.onap.aai.domain.yang.VfModule();
        vfModuleAAI.setModelCustomizationId(vfModuleCustomizationId);

        doReturn(vnfAAI).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doReturn(volumeGroupAAI).when(SPY_bbInputSetupUtils).getAAIVolumeGroup(CLOUD_OWNER,
                cloudConfiguration.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
        doReturn(vfModuleAAI).when(SPY_bbInputSetupUtils).getAAIVfModule(isA(String.class), isA(String.class));
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(isA(GenericVnf.class), isA(ModelInfo.class),
                isA(Service.class));
        doNothing().when(SPY_bbInputSetup).mapCatalogVfModule(isA(VfModule.class), isA(ModelInfo.class),
                isA(Service.class), isA(String.class));

        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setModelInfo(modelInfo)
                .setService(newService).setBbName(bbName).setServiceInstance(serviceInstance)
                .setLookupKeyMap(lookupKeyMap).setResourceId(resourceId).setRelatedInstanceList(relatedInstanceList)
                .setInstanceName(instanceName).setInstanceParams(instanceParams)
                .setCloudConfiguration(cloudConfiguration).setIsReplace(true).setServiceModel(serviceModel).build();
        SPY_bbInputSetup.populateVfModule(parameter);

        verify(SPY_bbInputSetup, times(1)).mapCatalogVfModule(isA(VfModule.class), isA(ModelInfo.class),
                eq(currentService), eq("vnfModelCustId"));
        verify(SPY_bbInputSetup, times(1)).mapCatalogVfModule(isA(VfModule.class), isA(ModelInfo.class), eq(newService),
                eq("vnfCustomizationId"));
        assertEquals("Lookup Key Map populated with VfModule Id", "vfModuleId1",
                lookupKeyMap.get(ResourceKey.VF_MODULE_ID));
        assertEquals("Lookup Key Map populated with VolumeGroup Id", volumeGroupId,
                lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID));
    }

    @Test
    public void testPopulateVfModuleIsReplace() throws Exception {
        String vnfId = "vnfId";
        String vfModuleId = "vfModuleId";
        String volumeGroupId = "volumeGroupId";
        String vfModuleCustomizationId = "vfModuleCustomizationId";
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(vfModuleCustomizationId);
        Service currentService = new Service();
        Service newService = new Service();
        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setCurrentService(currentService);
        currentService.setModelUUID("currentUUID");
        serviceModel.setNewService(newService);
        currentService.setModelUUID("newUUID");
        String bbName = AssignFlows.VF_MODULE.toString();
        ServiceInstance serviceInstance = new ServiceInstance();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId(vnfId);
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId(volumeGroupId);
        vnf.getVolumeGroups().add(volumeGroup);
        serviceInstance.getVnfs().add(vnf);
        VfModule vfModule1 = new VfModule();
        vfModule1.setVfModuleId("vfModuleId1");
        VfModule vfModule2 = new VfModule();
        vfModule2.setVfModuleId("vfModuleId2");
        vnf.getVfModules().add(vfModule1);
        vnf.getVfModules().add(vfModule2);
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, vnfId);
        lookupKeyMap.put(ResourceKey.VF_MODULE_ID, vfModuleId);
        String resourceId = vfModuleId;
        RelatedInstanceList[] relatedInstanceList = new RelatedInstanceList[1];
        RelatedInstanceList relatedInstanceListObj = new RelatedInstanceList();
        RelatedInstance relatedInstance = new RelatedInstance();
        relatedInstance.setInstanceId(vnfId);

        ModelInfo vnfModelInfo = new ModelInfo();
        vnfModelInfo.setModelCustomizationId("vnfCustomizationId");
        vnfModelInfo.setModelType(ModelType.vnf);
        relatedInstance.setModelInfo(vnfModelInfo);
        relatedInstanceListObj.setRelatedInstance(relatedInstance);
        relatedInstanceList[0] = relatedInstanceListObj;

        String instanceName = "vfModuleName";
        List<Map<String, String>> instanceParams = null;
        CloudConfiguration cloudConfiguration = new CloudConfiguration();

        org.onap.aai.domain.yang.GenericVnf vnfAAI = new org.onap.aai.domain.yang.GenericVnf();
        vnfAAI.setModelCustomizationId("vnfModelCustId");
        org.onap.aai.domain.yang.VolumeGroup volumeGroupAAI = new org.onap.aai.domain.yang.VolumeGroup();
        volumeGroupAAI.setModelCustomizationId(vfModuleCustomizationId);
        org.onap.aai.domain.yang.VfModule vfModuleAAI = new org.onap.aai.domain.yang.VfModule();
        vfModuleAAI.setModelCustomizationId(vfModuleCustomizationId);

        doReturn(vnfAAI).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doReturn(volumeGroupAAI).when(SPY_bbInputSetupUtils).getAAIVolumeGroup(CLOUD_OWNER,
                cloudConfiguration.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
        doReturn(vfModuleAAI).when(SPY_bbInputSetupUtils).getAAIVfModule(isA(String.class), isA(String.class));
        doNothing().when(SPY_bbInputSetup).mapCatalogVnf(isA(GenericVnf.class), isA(ModelInfo.class),
                isA(Service.class));
        doNothing().when(SPY_bbInputSetup).mapCatalogVfModule(isA(VfModule.class), isA(ModelInfo.class),
                isA(Service.class), isA(String.class));

        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setModelInfo(modelInfo)
                .setService(newService).setServiceInstance(serviceInstance).setBbName(bbName)
                .setLookupKeyMap(lookupKeyMap).setResourceId(resourceId).setRelatedInstanceList(relatedInstanceList)
                .setInstanceName(instanceName).setInstanceParams(instanceParams)
                .setCloudConfiguration(cloudConfiguration).setIsReplace(true).setServiceModel(serviceModel).build();


        SPY_bbInputSetup.populateVfModule(parameter);

        verify(SPY_bbInputSetup, times(2)).mapCatalogVfModule(isA(VfModule.class), isA(ModelInfo.class),
                eq(serviceModel.getCurrentService()), isA(String.class));
        verify(SPY_bbInputSetup, times(1)).mapCatalogVfModule(isA(VfModule.class), isA(ModelInfo.class),
                eq(serviceModel.getNewService()), isA(String.class));
        assertEquals("Lookup Key Map populated with VfModule Id", vfModuleId,
                lookupKeyMap.get(ResourceKey.VF_MODULE_ID));
        assertEquals("Lookup Key Map populated with VolumeGroup Id", volumeGroupId,
                lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID));
    }

    @Test
    public void testGettingVnfcToConfiguration() throws Exception {

        String vnfcName = "vnfcName";
        org.onap.aai.domain.yang.Configuration expectedAAI = new org.onap.aai.domain.yang.Configuration();
        AAIResourceUri aaiResourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().vnfc(vnfcName));
        AAIResultWrapper configurationWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

        doReturn(new AAIResultWrapper(null)).when(SPY_bbInputSetupUtils).getAAIResourceDepthOne(aaiResourceUri);
        Vnfc vnfc = SPY_bbInputSetup.getVnfcToConfiguration(vnfcName);
        Assert.assertNull(vnfc);

        doReturn(configurationWrapper).when(SPY_bbInputSetupUtils).getAAIResourceDepthOne(aaiResourceUri);
        vnfc = SPY_bbInputSetup.getVnfcToConfiguration(vnfcName);
        Assert.assertNotNull(vnfc);
    }
}
