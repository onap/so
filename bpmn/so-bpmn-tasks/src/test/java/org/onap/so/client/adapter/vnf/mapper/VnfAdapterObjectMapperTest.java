/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.adapter.vnf.mapper;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.entity.MsoRequest;

public class VnfAdapterObjectMapperTest {
    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/VnfAndVfModuleMapper/";

    @Spy
    private VnfAdapterObjectMapper vnfAdapterObjectMapper = new VnfAdapterObjectMapper();

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);

    }

    @After
    public void after() {
        reset(vnfAdapterObjectMapper);
    }

    @Test
    public void test_createVolumeGroupRequestMapper() throws Exception {
        RequestContext requestContext = new RequestContext();
        requestContext.setMsoRequestId("msoRequestId");

        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");

        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelVersion("modelVersion");
        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);

        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("vnfId");
        genericVnf.setVnfName("vnfName");
        genericVnf.setVnfType("vnfType");
        serviceInstance.getVnfs().add(genericVnf);

        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        modelInfoVfModule.setModelName("modelName");
        modelInfoVfModule.setModelCustomizationUUID("modelCustomizationUUID");

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        volumeGroup.setVolumeGroupName("volumeGroupName");
        volumeGroup.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
        volumeGroup.setModelInfoVfModule(modelInfoVfModule);

        serviceInstance.getVnfs().get(0).getVolumeGroups().add(volumeGroup);


        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setLcpCloudRegionId("lcpCloudRegionId");
        cloudRegion.setTenantId("tenantId");

        OrchestrationContext orchestrationContext = new OrchestrationContext();
        orchestrationContext.setIsRollbackEnabled(true);

        String sdncVfModuleQueryResponse = new String(Files
                .readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopology.json")));

        CreateVolumeGroupRequest expectedCreateVolumeGroupRequest = new CreateVolumeGroupRequest();

        expectedCreateVolumeGroupRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());
        expectedCreateVolumeGroupRequest.setTenantId(cloudRegion.getTenantId());
        expectedCreateVolumeGroupRequest.setVolumeGroupId(volumeGroup.getVolumeGroupId());
        expectedCreateVolumeGroupRequest.setVolumeGroupName(volumeGroup.getVolumeGroupName());
        expectedCreateVolumeGroupRequest.setVnfType(genericVnf.getVnfType());
        expectedCreateVolumeGroupRequest.setVnfVersion(serviceInstance.getModelInfoServiceInstance().getModelVersion());
        expectedCreateVolumeGroupRequest.setVfModuleType(volumeGroup.getModelInfoVfModule().getModelName());
        expectedCreateVolumeGroupRequest
                .setModelCustomizationUuid(volumeGroup.getModelInfoVfModule().getModelCustomizationUUID());

        Map<String, Object> volumeGroupParams = new HashMap<>();
        volumeGroupParams.put("vnf_id", genericVnf.getVnfId());
        volumeGroupParams.put("vnf_name", genericVnf.getVnfName());
        volumeGroupParams.put("vf_module_id", volumeGroup.getVolumeGroupId());
        volumeGroupParams.put("vf_module_name", volumeGroup.getVolumeGroupName());
        volumeGroupParams.put("paramOne", "paramOneValue");
        volumeGroupParams.put("paramTwo", "paramTwoValue");
        volumeGroupParams.put("paramThree", "paramThreeValue");
        expectedCreateVolumeGroupRequest.setVolumeGroupParams(volumeGroupParams);

        expectedCreateVolumeGroupRequest.setSkipAAI(true);
        expectedCreateVolumeGroupRequest
                .setSuppressBackout(Boolean.TRUE.equals(orchestrationContext.getIsRollbackEnabled()));
        expectedCreateVolumeGroupRequest.setFailIfExists(false);

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId(requestContext.getMsoRequestId());
        msoRequest.setServiceInstanceId(serviceInstance.getServiceInstanceId());
        expectedCreateVolumeGroupRequest.setMsoRequest(msoRequest);

        expectedCreateVolumeGroupRequest.setMessageId("messageId");
        expectedCreateVolumeGroupRequest.setNotificationUrl("endpoint/VNFAResponse/messageId");

        doReturn("endpoint/").when(vnfAdapterObjectMapper).getProperty(isA(String.class));
        doReturn("messageId").when(vnfAdapterObjectMapper).getRandomUuid();

        CreateVolumeGroupRequest actualCreateVolumeGroupRequest =
                vnfAdapterObjectMapper.createVolumeGroupRequestMapper(requestContext, cloudRegion, orchestrationContext,
                        serviceInstance, genericVnf, volumeGroup, sdncVfModuleQueryResponse);
        assertThat(actualCreateVolumeGroupRequest, sameBeanAs(expectedCreateVolumeGroupRequest));

        doReturn("false").when(vnfAdapterObjectMapper).getProperty("mso.bridgeEnabled");
        actualCreateVolumeGroupRequest = vnfAdapterObjectMapper.createVolumeGroupRequestMapper(requestContext,
                cloudRegion, orchestrationContext, serviceInstance, genericVnf, volumeGroup, sdncVfModuleQueryResponse);
        assertThat(actualCreateVolumeGroupRequest, sameBeanAs(expectedCreateVolumeGroupRequest));

        doReturn(null).when(vnfAdapterObjectMapper).getProperty("mso.bridgeEnabled");
        expectedCreateVolumeGroupRequest.setEnableBridge(true);
        actualCreateVolumeGroupRequest = vnfAdapterObjectMapper.createVolumeGroupRequestMapper(requestContext,
                cloudRegion, orchestrationContext, serviceInstance, genericVnf, volumeGroup, sdncVfModuleQueryResponse);
        assertThat(actualCreateVolumeGroupRequest, sameBeanAs(expectedCreateVolumeGroupRequest));

        doReturn("true").when(vnfAdapterObjectMapper).getProperty("mso.bridgeEnabled");
        expectedCreateVolumeGroupRequest.setEnableBridge(true);
        actualCreateVolumeGroupRequest = vnfAdapterObjectMapper.createVolumeGroupRequestMapper(requestContext,
                cloudRegion, orchestrationContext, serviceInstance, genericVnf, volumeGroup, sdncVfModuleQueryResponse);
        assertThat(actualCreateVolumeGroupRequest, sameBeanAs(expectedCreateVolumeGroupRequest));
    }

    @Test
    public void test_createVolumeGroupRequestMapper_for_alaCarte_flow() throws Exception {
        RequestContext requestContext = new RequestContext();
        requestContext.setMsoRequestId("msoRequestId");

        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");

        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelVersion("modelVersion");
        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);

        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("vnfId");
        genericVnf.setVnfName("vnfName");
        genericVnf.setVnfType("vnfType");
        serviceInstance.getVnfs().add(genericVnf);


        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        modelInfoVfModule.setModelName("modelName");
        modelInfoVfModule.setModelCustomizationUUID("modelCustomizationUUID");

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        volumeGroup.setVolumeGroupName("volumeGroupName");
        volumeGroup.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
        volumeGroup.setModelInfoVfModule(modelInfoVfModule);
        serviceInstance.getVnfs().get(0).getVolumeGroups().add(volumeGroup);

        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setLcpCloudRegionId("lcpCloudRegionId");
        cloudRegion.setTenantId("tenantId");

        OrchestrationContext orchestrationContext = new OrchestrationContext();
        orchestrationContext.setIsRollbackEnabled(true);

        CreateVolumeGroupRequest expectedCreateVolumeGroupRequest = new CreateVolumeGroupRequest();

        expectedCreateVolumeGroupRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());
        expectedCreateVolumeGroupRequest.setTenantId(cloudRegion.getTenantId());
        expectedCreateVolumeGroupRequest.setVolumeGroupId(volumeGroup.getVolumeGroupId());
        expectedCreateVolumeGroupRequest.setVolumeGroupName(volumeGroup.getVolumeGroupName());
        expectedCreateVolumeGroupRequest.setVnfType(genericVnf.getVnfType());
        expectedCreateVolumeGroupRequest.setVnfVersion(serviceInstance.getModelInfoServiceInstance().getModelVersion());
        expectedCreateVolumeGroupRequest.setVfModuleType(volumeGroup.getModelInfoVfModule().getModelName());
        expectedCreateVolumeGroupRequest
                .setModelCustomizationUuid(volumeGroup.getModelInfoVfModule().getModelCustomizationUUID());

        Map<String, Object> volumeGroupParams = new HashMap<>();
        volumeGroupParams.put("vnf_id", genericVnf.getVnfId());
        volumeGroupParams.put("vnf_name", genericVnf.getVnfName());
        volumeGroupParams.put("vf_module_id", volumeGroup.getVolumeGroupId());
        volumeGroupParams.put("vf_module_name", volumeGroup.getVolumeGroupName());

        expectedCreateVolumeGroupRequest.setVolumeGroupParams(volumeGroupParams);

        expectedCreateVolumeGroupRequest.setSkipAAI(true);
        expectedCreateVolumeGroupRequest
                .setSuppressBackout(Boolean.TRUE.equals(orchestrationContext.getIsRollbackEnabled()));
        expectedCreateVolumeGroupRequest.setFailIfExists(false);

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId(requestContext.getMsoRequestId());
        msoRequest.setServiceInstanceId(serviceInstance.getServiceInstanceId());
        expectedCreateVolumeGroupRequest.setMsoRequest(msoRequest);

        expectedCreateVolumeGroupRequest.setMessageId("messageId");
        expectedCreateVolumeGroupRequest.setNotificationUrl("endpoint/VNFAResponse/messageId");

        doReturn("endpoint/").when(vnfAdapterObjectMapper).getProperty(isA(String.class));
        doReturn("messageId").when(vnfAdapterObjectMapper).getRandomUuid();

        CreateVolumeGroupRequest actualCreateVolumeGroupRequest = vnfAdapterObjectMapper.createVolumeGroupRequestMapper(
                requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf, volumeGroup, null);

        assertThat(actualCreateVolumeGroupRequest, sameBeanAs(expectedCreateVolumeGroupRequest));
    }

    @Test
    public void test_deleteVolumeGroupHeatIdRequestMapper() throws Exception {
        this.test_deleteVolumeGroupRequestMapper("heatStackId");
    }

    @Test
    public void test_deleteVolumeGroupNoHeatIdRequestMapper() throws Exception {
        this.test_deleteVolumeGroupRequestMapper(null);
    }

    private void test_deleteVolumeGroupRequestMapper(String heatStackId) throws Exception {
        DeleteVolumeGroupRequest expectedDeleteVolumeGroupRequest = new DeleteVolumeGroupRequest();

        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setLcpCloudRegionId("lcpCloudRegionId");
        expectedDeleteVolumeGroupRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());

        cloudRegion.setTenantId("tenantId");
        expectedDeleteVolumeGroupRequest.setTenantId(cloudRegion.getTenantId());

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        expectedDeleteVolumeGroupRequest.setVolumeGroupId(volumeGroup.getVolumeGroupId());

        if (heatStackId != null) {
            volumeGroup.setHeatStackId("heatStackId");
            expectedDeleteVolumeGroupRequest.setVolumeGroupStackId(volumeGroup.getHeatStackId());
        } else {
            volumeGroup.setVolumeGroupName("volumeGroupName");
            expectedDeleteVolumeGroupRequest.setVolumeGroupStackId(volumeGroup.getVolumeGroupName());
        }

        expectedDeleteVolumeGroupRequest.setSkipAAI(true);

        MsoRequest msoRequest = new MsoRequest();
        RequestContext requestContext = new RequestContext();
        requestContext.setMsoRequestId("msoRequestId");
        msoRequest.setRequestId(requestContext.getMsoRequestId());
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");
        msoRequest.setServiceInstanceId(serviceInstance.getServiceInstanceId());
        expectedDeleteVolumeGroupRequest.setMsoRequest(msoRequest);

        String messageId = "messageId";
        String endpoint = "endpoint";
        doReturn(messageId).when(vnfAdapterObjectMapper).getRandomUuid();
        doReturn(endpoint).when(vnfAdapterObjectMapper).getProperty(isA(String.class));
        expectedDeleteVolumeGroupRequest.setMessageId(messageId);
        expectedDeleteVolumeGroupRequest.setNotificationUrl(endpoint + "/VNFAResponse/" + messageId);

        DeleteVolumeGroupRequest actualDeleteVolumeGroupRequest = vnfAdapterObjectMapper
                .deleteVolumeGroupRequestMapper(requestContext, cloudRegion, serviceInstance, volumeGroup);

        assertThat(actualDeleteVolumeGroupRequest, sameBeanAs(expectedDeleteVolumeGroupRequest));
    }

    @Test
    public void test_createVolumeGroupParams() throws Exception {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("vnfId");
        genericVnf.setVnfName("vnfName");

        RequestContext requestContext = new RequestContext();

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        volumeGroup.setVolumeGroupName("volumeGroupName");

        String sdncVfModuleQueryResponse = new String(Files
                .readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopology.json")));

        Map<String, String> expectedVolumeGroupParams = new HashMap<>();
        expectedVolumeGroupParams.put("vnf_id", genericVnf.getVnfId());
        expectedVolumeGroupParams.put("vnf_name", genericVnf.getVnfName());
        expectedVolumeGroupParams.put("vf_module_id", volumeGroup.getVolumeGroupId());
        expectedVolumeGroupParams.put("vf_module_name", volumeGroup.getVolumeGroupName());
        expectedVolumeGroupParams.put("paramOne", "paramOneValue");
        expectedVolumeGroupParams.put("paramTwo", "paramTwoValue");
        expectedVolumeGroupParams.put("paramThree", "paramThreeValue");

        Map<String, Object> actualVolumeGroupParams = vnfAdapterObjectMapper.createVolumeGroupParams(requestContext,
                genericVnf, volumeGroup, sdncVfModuleQueryResponse);

        assertEquals(expectedVolumeGroupParams, actualVolumeGroupParams);
    }

    @Test
    public void test_createVolumeGroupParams_without_sdncResponse() throws Exception {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("vnfId");
        genericVnf.setVnfName("vnfName");

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        volumeGroup.setVolumeGroupName("volumeGroupName");

        Map<String, String> expectedVolumeGroupParams = new HashMap<>();
        expectedVolumeGroupParams.put("vnf_id", genericVnf.getVnfId());
        expectedVolumeGroupParams.put("vnf_name", genericVnf.getVnfName());
        expectedVolumeGroupParams.put("vf_module_id", volumeGroup.getVolumeGroupId());
        expectedVolumeGroupParams.put("vf_module_name", volumeGroup.getVolumeGroupName());
        RequestContext requestContext = new RequestContext();
        Map<String, Object> actualVolumeGroupParams =
                vnfAdapterObjectMapper.createVolumeGroupParams(requestContext, genericVnf, volumeGroup, null);

        assertEquals(expectedVolumeGroupParams, actualVolumeGroupParams);
    }

    @Test
    public void test_createVolumeGroupParams_with_user_params() throws Exception {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("vnfId");
        genericVnf.setVnfName("vnfName");

        RequestContext requestContext = new RequestContext();
        Map<String, Object> userParamsMap = new HashMap<>();
        userParamsMap.put("name", "userParamKey");
        userParamsMap.put("value", "userParamValue");
        List<Map<String, Object>> userParams = new ArrayList<>();
        userParams.add(userParamsMap);
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUserParams(userParams);
        requestContext.setRequestParameters(requestParameters);
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        volumeGroup.setVolumeGroupName("volumeGroupName");

        String sdncVfModuleQueryResponse = new String(Files
                .readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopology.json")));

        Map<String, String> expectedVolumeGroupParams = new HashMap<>();
        expectedVolumeGroupParams.put("vnf_id", genericVnf.getVnfId());
        expectedVolumeGroupParams.put("vnf_name", genericVnf.getVnfName());
        expectedVolumeGroupParams.put("vf_module_id", volumeGroup.getVolumeGroupId());
        expectedVolumeGroupParams.put("vf_module_name", volumeGroup.getVolumeGroupName());
        expectedVolumeGroupParams.put("paramOne", "paramOneValue");
        expectedVolumeGroupParams.put("paramTwo", "paramTwoValue");
        expectedVolumeGroupParams.put("paramThree", "paramThreeValue");
        expectedVolumeGroupParams.put("userParamKey", "userParamValue");

        Map<String, Object> actualVolumeGroupParams = vnfAdapterObjectMapper.createVolumeGroupParams(requestContext,
                genericVnf, volumeGroup, sdncVfModuleQueryResponse);

        assertEquals(expectedVolumeGroupParams, actualVolumeGroupParams);
    }

    @Test
    public void test_createMsoRequest() {
        RequestContext requestContext = new RequestContext();
        requestContext.setMsoRequestId("msoRequestId");

        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");

        MsoRequest expectedMsoRequest = new MsoRequest();
        expectedMsoRequest.setRequestId(requestContext.getMsoRequestId());
        expectedMsoRequest.setServiceInstanceId(serviceInstance.getServiceInstanceId());

        MsoRequest actualMsoRequest = vnfAdapterObjectMapper.createMsoRequest(requestContext, serviceInstance);

        assertThat(expectedMsoRequest, sameBeanAs(actualMsoRequest));
    }
}
