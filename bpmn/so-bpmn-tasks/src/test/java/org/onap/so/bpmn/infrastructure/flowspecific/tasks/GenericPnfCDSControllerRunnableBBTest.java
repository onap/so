/*
 * ============LICENSE_START======================================================= Copyright (C) 2020 Nokia. All rights
 * reserved. ================================================================================ Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoPnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.ConfigureInstanceParamsForPnf;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.PayloadGenerationException;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.Pnfs;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.ModelInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.EXECUTION_OBJECT;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MSO_REQUEST_ID;
import static org.onap.so.client.cds.PayloadConstants.PRC_BLUEPRINT_NAME;
import static org.onap.so.client.cds.PayloadConstants.PRC_BLUEPRINT_VERSION;
import static org.onap.so.client.cds.PayloadConstants.SCOPE;

@RunWith(MockitoJUnitRunner.class)
public class GenericPnfCDSControllerRunnableBBTest {

    @Mock
    private ExtractPojosForBB extractPojosForBB;
    @Mock
    private ConfigureInstanceParamsForPnf configureInstanceParamsForPnf;

    @InjectMocks
    private GenericPnfCDSControllerRunnableBB genericPnfCDSControllerRunnableBB;

    private ControllerContext<BuildingBlockExecution> controllerContext;
    private BuildingBlockExecution execution;

    private final static String blueprintName = "blueprint_name";
    private final static String blueprintVersion = "blueprint_version";
    private final static String msoRequestId = "mso_request_id";
    private final static String pnfID = "5df8b6de-2083-11e7-93ae-92361f002671";
    private final static String serviceInstanceID = "test_service_id";
    private final static String pnfName = "PNFDemo";
    private final static String serviceModelUUID = "6bc0b04d-1873-4721-b53d-6615225b2a28";
    private final static String pnfCustomizationUUID = "9acb3a83-8a52-412c-9a45-901764938144";
    private final static String action = "action";
    private static final String GENERAL_BLOCK_EXECUTION_MAP_KEY = "gBBInput";

    @Before
    public void setUp() {
        ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();
        BuildingBlock buildingBlock = new BuildingBlock();

        buildingBlock.setBpmnAction(action);
        executeBuildingBlock.setBuildingBlock(buildingBlock);

        execution = new DelegateExecutionImpl(new ExecutionImpl());
        execution.setVariable("buildingBlock", executeBuildingBlock);
        execution.setVariable(PRC_BLUEPRINT_NAME, blueprintName);
        execution.setVariable(PRC_BLUEPRINT_VERSION, blueprintVersion);
        execution.setVariable(MSO_REQUEST_ID, msoRequestId);
        execution.setVariable(SCOPE, "scope");

        controllerContext = new ControllerContext<>();
        controllerContext.setExecution(execution);
    }

    @Test
    public void understandTest() {
        // given
        controllerContext.setControllerScope("pnf");
        controllerContext.setControllerActor("cds");

        // when, then
        assertTrue(genericPnfCDSControllerRunnableBB.understand(controllerContext));
    }

    @Test
    public void readyTest() {
        // when, then
        assertTrue(genericPnfCDSControllerRunnableBB.ready(controllerContext));
    }

    @Test
    public void prepareTest() throws BBObjectNotFoundException, PayloadGenerationException {
        // given
        prepareData();

        // when
        genericPnfCDSControllerRunnableBB.prepare(controllerContext);

        // then
        final AbstractCDSPropertiesBean abstractCDSPropertiesBean = execution.getVariable(EXECUTION_OBJECT);
        final JSONObject actionProperties = new JSONObject(abstractCDSPropertiesBean.getRequestObject())
                .getJSONObject("action-request").getJSONObject("action-properties");

        assertThat(abstractCDSPropertiesBean).isNotNull();
        assertThat(abstractCDSPropertiesBean.getRequestObject()).isNotNull();
        assertThat(abstractCDSPropertiesBean.getRequestObject()).isInstanceOf(String.class);
        assertThat(execution.getGeneralBuildingBlock()).isNotNull();

        assertEquals(blueprintName, abstractCDSPropertiesBean.getBlueprintName());
        assertEquals(blueprintVersion, abstractCDSPropertiesBean.getBlueprintVersion());
        assertEquals(msoRequestId, abstractCDSPropertiesBean.getRequestId());
        assertEquals(action, abstractCDSPropertiesBean.getActionName());
        assertEquals("sync", abstractCDSPropertiesBean.getMode());
        assertEquals("SO", abstractCDSPropertiesBean.getOriginatorId());

        assertEquals(pnfID, actionProperties.get("pnf-id"));
        assertEquals(serviceInstanceID, actionProperties.get("service-instance-id"));
        assertEquals(serviceModelUUID, actionProperties.get("service-model-uuid"));
        assertEquals(pnfName, actionProperties.get("pnf-name"));
        assertEquals(pnfCustomizationUUID, actionProperties.get("pnf-customization-uuid"));
    }

    private void prepareData() throws BBObjectNotFoundException, PayloadGenerationException {
        Pnf pnf = new Pnf();
        ServiceInstance serviceInstance = new ServiceInstance();

        pnf.setPnfName(pnfName);
        pnf.setPnfId(pnfID);
        ModelInfoPnf modelInfoPnf = new ModelInfoPnf();
        modelInfoPnf.setModelCustomizationUuid(pnfCustomizationUUID);
        pnf.setModelInfoPnf(modelInfoPnf);

        serviceInstance.setServiceInstanceId(serviceInstanceID);
        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelUuid(serviceModelUUID);
        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);

        execution.setVariable(GENERAL_BLOCK_EXECUTION_MAP_KEY,
                createGeneralBuildingBlock(createService(createPnfsList())));
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.PNF))).thenReturn(pnf);
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstance);
        doNothing().when(configureInstanceParamsForPnf).populateInstanceParams(any(), any(), any());
    }

    private Service createService(List<Pnfs> pnfList) {
        Service service = new Service();
        Resources resources = new Resources();
        resources.setPnfs(pnfList);
        service.setResources(resources);
        return service;
    }

    private GeneralBuildingBlock createGeneralBuildingBlock(Object serviceJson) {
        GeneralBuildingBlock generalBuildingBlock = new GeneralBuildingBlock();
        RequestContext requestContext = new RequestContext();
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUserParams(createRequestUserParams(serviceJson));
        requestContext.setRequestParameters(requestParameters);
        generalBuildingBlock.setRequestContext(requestContext);
        return generalBuildingBlock;
    }

    private List<Map<String, Object>> createRequestUserParams(Object serviceJson) {
        List<Map<String, Object>> userParams = new ArrayList<>();
        Map<String, Object> userParamMap = new HashMap<>();
        userParamMap.put("service", serviceJson);
        userParams.add(userParamMap);
        return userParams;
    }

    private List<Pnfs> createPnfsList() {
        List<Map<String, String>> instanceParamsListSearchedPnf = new ArrayList<>();
        Map<String, String> instanceParam = new HashMap<>();
        instanceParam.put("INSTANCE_PARAM1_NAME", "INSTANCE_PARAM1_VALUE");
        instanceParam.put("INSTANCE_PARAM2_NAME", "INSTANCE_PARAM2_VALUE");
        Map<String, String> instanceParam2 = new HashMap<>();
        instanceParam2.put("INSTANCE_PARAM3_NAME", "INSTANCE_PARAM3_VALUE");
        instanceParamsListSearchedPnf.add(instanceParam);
        instanceParamsListSearchedPnf.add(instanceParam2);
        Pnfs searchedPnf = createPnfs("0c1ac643-377e-475b-be50-6be65f91a7ad", instanceParamsListSearchedPnf);

        List<Pnfs> pnfList = new ArrayList<>();
        pnfList.add(searchedPnf);
        return pnfList;
    }

    private Pnfs createPnfs(String pnfModelCustomizationId, List<Map<String, String>> instanceParamsList) {
        Pnfs pnfs = new Pnfs();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationUuid(pnfModelCustomizationId);
        pnfs.setModelInfo(modelInfo);
        pnfs.setInstanceParams(instanceParamsList);
        return pnfs;
    }
}
