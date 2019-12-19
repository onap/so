/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(Parameterized.class)
public class BBInputSetupPopulateMethodsTest extends BaseBBInputSetupTest {
    private String vnfType;
    private static final String RESOURCE_PATH = "src/test/resources/__files/ExecuteBuildingBlock/";

    protected ObjectMapper mapper = new ObjectMapper();

    @Spy
    @InjectMocks
    private BBInputSetup SPY_bbInputSetup = new BBInputSetup();

    @Mock
    private BBInputSetupUtils bbInputSetupUtils;

    @Mock
    private CloudInfoFromAAI cloudInfoFromAAI;

    @Spy
    private BBInputSetupMapperLayer SPY_bbInputSetupMapperLayer;

    @Mock
    private RequestsDbClient requestsDbClient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public BBInputSetupPopulateMethodsTest(String vnfType) {
        this.vnfType = vnfType;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Parameterized.Parameters
    public static Collection input() {
        return Arrays.asList(new Object[] {"vnfType", null});
    }

    @Test
    public void test_getGBBMacro_populateL3Network_shouldBeCalled() throws Exception {
        // given
        String resourceId = "123";
        String vnfType = this.vnfType;
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
        executeBB = changeInEbbConfResourceKeys(executeBB, configResourceKeys);
        executeBB = changeInEbbRequestDetails(executeBB, requestDetails);
        executeBB = changeInBbFlowName(executeBB, AssignFlows.NETWORK_MACRO.toString());
        executeBB = changeInBbKey(executeBB, "ab153b6e-c364-44c0-bef6-1f2982117f04");

        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());
        // when
        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        // then
        verify(SPY_bbInputSetup, times(1)).populateL3Network(any(String.class), any(String.class), isA(ModelInfo.class),
                isA(Service.class), any(String.class), isA(ServiceInstance.class), any(), any(String.class), any());
    }

    @Test
    public void test_getGBBMacro_populateGenericVnf_shouldBeCalled() throws Exception {
        // given
        String resourceId = "123";
        String vnfType = this.vnfType;
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        InfraActiveRequests request = Mockito.mock(InfraActiveRequests.class);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();

        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        executeBB = changeInBbKey(executeBB, "ab153b6e-c364-44c0-bef6-1f2982117f04");
        executeBB = changeInBbFlowName(executeBB, AssignFlows.VNF.toString());

        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(request).when(bbInputSetupUtils).getInfraActiveRequest(executeBB.getRequestId());
        doReturn(service).when(bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());
        doNothing().when(bbInputSetupUtils).updateInfraActiveRequestVnfId(request,
                lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID));
        doReturn(aaiVnf).when(bbInputSetupUtils).getAAIGenericVnf(any(String.class));

        // when
        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        // then
        verify(SPY_bbInputSetup, times(1)).populateGenericVnf(any(String.class), isA(ModelInfo.class),
                any(String.class), isA(org.onap.so.serviceinstancebeans.Platform.class),
                isA(org.onap.so.serviceinstancebeans.LineOfBusiness.class), isA(Service.class), any(String.class),
                isA(ServiceInstance.class), any(), any(), any(String.class), eq(vnfType), any(), any(String.class),
                any(String.class));
    }

    @Test
    public void test_getGBBMacro_populateVfModule_shouldBeCalled() throws Exception {
        // given
        String resourceId = "123";
        String vnfType = this.vnfType;
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockWithVnf.json"),
                GeneralBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, null);

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        executeBB = changeInBbFlowName(executeBB, AssignFlows.VF_MODULE.toString());
        executeBB = changeInBbKey(executeBB, "a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        executeBB = changeInEbbConfResourceKeys(executeBB, configResourceKeys);
        executeBB = changeInEbbRequestDetails(executeBB, requestDetails);

        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());
        doReturn("vnfId").when(SPY_bbInputSetup).getVnfId(executeBB, lookupKeyMap);
        doReturn(aaiVnf).when(bbInputSetupUtils).getAAIGenericVnf(any(String.class));
        // when
        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        // then
        verify(SPY_bbInputSetup, times(1)).populateVfModule(any(String.class), isA(ModelInfo.class), isA(Service.class),
                any(String.class), isA(ServiceInstance.class), any(), any(String.class), any(), any(String.class),
                any(), isA(CloudConfiguration.class), Mockito.anyBoolean());
    }

    @Test
    public void test_getGBBMacro_populateVolumeGroup_shouldBeCalled() throws Exception {
        // given
        String resourceId = "123";
        String vnfType = this.vnfType;
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockWithVnf.json"),
                GeneralBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, null);

        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        executeBB = changeInBbFlowName(executeBB, AssignFlows.VOLUME_GROUP.toString());
        executeBB = changeInBbKey(executeBB, "72d9d1cd-f46d-447a-abdb-451d6fb05fa8");

        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());
        doReturn("vnfId").when(SPY_bbInputSetup).getVnfId(executeBB, lookupKeyMap);
        doReturn(aaiVnf).when(bbInputSetupUtils).getAAIGenericVnf(any(String.class));
        // when
        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        // then
        verify(SPY_bbInputSetup, times(1)).populateVolumeGroup(any(String.class), isA(ModelInfo.class),
                isA(Service.class), any(String.class), isA(ServiceInstance.class), any(), any(String.class),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(vnfType), any());
    }

    @Test
    public void test_getGBBMacro_populateConfiguration_shouldBeCalled() throws Exception {
        // given
        String resourceId = "123";
        String vnfType = this.vnfType;
        String requestAction = "createInstance";
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setModelCustomizationId("modelCustId");
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();

        ConfigurationResourceKeys configResourceKeys = prepareConfigurationResourceKeys();
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        executeBB = changeInBbFlowName(executeBB, "AssignFabricConfigurationBB");
        executeBB = changeInBbKey(executeBB, "72d9d1cd-f46d-447a-abdb-451d6fb05fa9");
        executeBB = changeInEbbConfResourceKeys(executeBB, configResourceKeys);
        executeBB = changeInEbbRequestDetails(executeBB, requestDetails);

        Configuration configuration = new Configuration();
        configuration.setConfigurationId("configurationId");
        gBB.getServiceInstance().getConfigurations().add(configuration);
        List<ConfigurationResourceCustomization> configurationCustList = new ArrayList<>();
        ConfigurationResourceCustomization configurationCust = new ConfigurationResourceCustomization();
        configurationCust.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa9");
        configurationCustList.add(configurationCust);

        doReturn(configurationCustList).when(service).getConfigurationCustomizations();
        doReturn(gBB).when(SPY_bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
                requestAction, lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(service).when(bbInputSetupUtils)
                .getCatalogServiceByModelUUID(gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());
        doNothing().when(SPY_bbInputSetup).populateConfiguration(any(String.class), isA(ModelInfo.class),
                isA(Service.class), any(String.class), isA(ServiceInstance.class), any(), any(String.class),
                ArgumentMatchers.isNull(), isA(ConfigurationResourceKeys.class), isA(RequestDetails.class));
        // when
        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        // then
        verify(SPY_bbInputSetup, times(1)).populateConfiguration(any(String.class), isA(ModelInfo.class),
                isA(Service.class), any(String.class), isA(ServiceInstance.class), any(), any(String.class),
                ArgumentMatchers.isNull(), isA(ConfigurationResourceKeys.class), isA(RequestDetails.class));
    }
}
