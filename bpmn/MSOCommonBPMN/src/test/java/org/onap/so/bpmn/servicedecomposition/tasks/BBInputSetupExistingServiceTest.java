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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.onap.so.bpmn.servicedecomposition.tasks.BaseBBInputSetupTestHelper.prepareLookupKeyMap;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.RequestDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(Parameterized.class)
public class BBInputSetupExistingServiceTest {
    private String requestAction;
    private String bpmnFlowName;
    private String key;
    private static final String RESOURCE_PATH = "src/test/resources/__files/ExecuteBuildingBlock/";

    protected ObjectMapper mapper = new ObjectMapper();

    @Spy
    @InjectMocks
    private BBInputSetup SPY_bbInputSetup = new BBInputSetup();

    @Mock
    private BBInputSetupUtils bbInputSetupUtils;

    @Mock
    private BBInputSetupMapperLayer bbInputSetupMapperLayer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public BBInputSetupExistingServiceTest(String requestAction, String bpmnFlowName, String key) {
        this.requestAction = requestAction;
        this.bpmnFlowName = bpmnFlowName;
        this.key = key;
    }

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Parameterized.Parameters
    public static Collection input() {
        return Arrays
                .asList(new Object[][] {{"deleteInstance", "DeleteNetworkBB", "ab153b6e-c364-44c0-bef6-1f2982117f04"},
                        {"activateInstance", "ActivateNetworkBB", "ab153b6e-c364-44c0-bef6-1f2982117f04"},
                        {"unassignInstance", "UnassignNetworkBB", "ab153b6e-c364-44c0-bef6-1f2982117f04"},
                        {"activateFabricConfiguration", "ActivateFabricConfigurationBB",
                                "ab153b6e-c364-44c0-bef6-134534656234"}});
    }

    @Test
    public void test_getGBBMacro_getGBBMacroExistingService_shouldBeCalled() throws Exception {
        // given
        String resourceId = "123";
        String vnfType = "vnfType";
        String requestAction = this.requestAction;
        Service service = Mockito.mock(Service.class);
        GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
                GeneralBuildingBlock.class);
        ServiceInstance serviceInstance = gBB.getServiceInstance();
        Map<ResourceKey, String> lookupKeyMap = prepareLookupKeyMap();
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
        aaiServiceInstance.setModelVersionId("modelVersionId");
        CloudConfiguration cloudConfig = new CloudConfiguration();
        cloudConfig.setLcpCloudRegionId("lcpCloudRegionId");
        CloudRegion aaiCloudRegion = Mockito.mock(CloudRegion.class);

        RequestDetails requestDetails = mapper
                .readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
        requestDetails.getRequestParameters().setUserParams(null);
        requestDetails.setCloudConfiguration(cloudConfig);
        ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
                ExecuteBuildingBlock.class);
        executeBB.setRequestDetails(requestDetails);
        BuildingBlock buildingBlock = executeBB.getBuildingBlock();
        buildingBlock.setBpmnFlowName(this.bpmnFlowName).setKey(this.key);

        doReturn(service).when(bbInputSetupUtils).getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
        doReturn(aaiServiceInstance).when(bbInputSetupUtils)
                .getAAIServiceInstanceById(lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID));
        doReturn(serviceInstance).when(SPY_bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
        doReturn(gBB).when(SPY_bbInputSetup).populateGBBWithSIAndAdditionalInfo(any(BBInputSetupParameter.class));
        doReturn(aaiCloudRegion).when(bbInputSetupUtils).getCloudRegion(requestDetails.getCloudConfiguration());
        // when
        SPY_bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
        // then
        verify(SPY_bbInputSetup, times(1)).getGBBMacroExistingService(isA(ExecuteBuildingBlock.class), any(),
                any(String.class), isA(String.class), isA(CloudConfiguration.class));
    }
}
