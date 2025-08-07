/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada.
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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.client.cds.AbstractCDSProcessingBBUtils;
import org.onap.so.client.cds.GeneratePayloadForCds;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.Vnfs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

public class GenericCDSProcessingBBTest extends BaseTaskTest {

    private static final String VNF_SCOPE = "vnf";
    private static final String TEST_VNF_MODEL_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String DEPLOY_ACTION_FOR_CDS = "configDeploy";
    private static final String GENERAL_BLOCK_EXECUTION_MAP_KEY = "gBBInput";
    private static final String BUILDING_BLOCK = "buildingBlock";
    private static final String TEST_MSO_REQUEST_ID = "ff874603-4222-11e7-9252-005056850d2e";
    private static final String EXECUTION_OBJECT = "executionObject";
    private static final String BLUEPRINT_NAME = "test";
    private static final String BLUEPRINT_VERSION = "1.0.0";

    @InjectMocks
    private GenericCDSProcessingBB controllerRunnable;

    @Mock
    private AbstractCDSProcessingBBUtils cdsDispather;

    @Mock
    private GeneratePayloadForCds generatePayloadForCds;

    private BuildingBlockExecution buildingBlockExecution;

    private ExecuteBuildingBlock executeBuildingBlock;

    @Before
    public void setUp() {
        buildingBlockExecution = createBuildingBlockExecution();
        executeBuildingBlock = new ExecuteBuildingBlock();
    }

    @Test
    public void testExecutionObjectCreationForVnf() throws Exception {
        // given
        ControllerContext<BuildingBlockExecution> controllerContext = new ControllerContext<>();
        controllerContext.setExecution(buildingBlockExecution);
        controllerContext.setControllerActor("CDS");
        controllerContext.setControllerScope("vnf");
        setScopeAndAction(VNF_SCOPE, DEPLOY_ACTION_FOR_CDS);
        AbstractCDSPropertiesBean cdsBean = prepareCDSBean();

        doReturn(cdsBean).when(generatePayloadForCds).buildCdsPropertiesBean(buildingBlockExecution);
        doNothing().when(cdsDispather).constructExecutionServiceInputObjectBB(buildingBlockExecution);
        doNothing().when(cdsDispather).sendRequestToCDSClientBB(buildingBlockExecution);

        // when
        Boolean isUnderstandable = controllerRunnable.understand(controllerContext);
        Boolean isReady = controllerRunnable.ready(controllerContext);
        controllerRunnable.prepare(controllerContext);
        controllerRunnable.run(controllerContext);

        // verify
        assertEquals(isUnderstandable, true);
        assertEquals(isReady, true);
        AbstractCDSPropertiesBean executionObject = buildingBlockExecution.getVariable(EXECUTION_OBJECT);
        assertNotNull(executionObject);
        assertThat(executionObject).isInstanceOf(AbstractCDSPropertiesBean.class);
        assertEquals(BLUEPRINT_NAME, executionObject.getBlueprintName());
        assertEquals(BLUEPRINT_VERSION, executionObject.getBlueprintVersion());
        assertEquals(TEST_MSO_REQUEST_ID, executionObject.getRequestId());
        assertNotNull(executionObject.getRequestObject());
    }

    private AbstractCDSPropertiesBean prepareCDSBean() {
        AbstractCDSPropertiesBean cdsBean = new AbstractCDSPropertiesBean();
        cdsBean.setBlueprintName(BLUEPRINT_NAME);
        cdsBean.setBlueprintVersion(BLUEPRINT_VERSION);
        cdsBean.setRequestId(TEST_MSO_REQUEST_ID);
        cdsBean.setRequestObject("requestObject");

        return cdsBean;
    }

    private GeneralBuildingBlock createGeneralBuildingBlock() {
        GeneralBuildingBlock generalBuildingBlock = new GeneralBuildingBlock();
        RequestContext requestContext = new RequestContext();
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUserParams(createRequestParameters());
        requestContext.setRequestParameters(requestParameters);
        requestContext.setMsoRequestId(TEST_MSO_REQUEST_ID);
        generalBuildingBlock.setRequestContext(requestContext);
        return generalBuildingBlock;
    }

    private List<Map<String, Object>> createRequestParameters() {
        List<Map<String, Object>> userParams = new ArrayList<>();
        Map<String, Object> userParamMap = new HashMap<>();
        userParamMap.put("service", getUserParams());
        userParams.add(userParamMap);
        return userParams;
    }

    private Service getUserParams() {
        Service service = new Service();
        Resources resources = new Resources();
        resources.setVnfs(createVnfList());
        service.setResources(resources);
        return service;
    }

    private List<Vnfs> createVnfList() {
        List<Map<String, String>> instanceParamsListSearchedVnf = new ArrayList<>();
        Map<String, String> instanceParam = new HashMap<>();
        instanceParam.put("sec_group", "sec_group");
        instanceParam.put("net_id", "acl-cloud-region");
        instanceParamsListSearchedVnf.add(instanceParam);
        Vnfs searchedVnf = createVnf(instanceParamsListSearchedVnf);
        List<Vnfs> vnfList = new ArrayList<>();
        vnfList.add(searchedVnf);
        return vnfList;
    }

    private Vnfs createVnf(List<Map<String, String>> instanceParamsList) {
        Vnfs vnf = new Vnfs();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(TEST_VNF_MODEL_CUSTOMIZATION_UUID);
        vnf.setModelInfo(modelInfo);
        vnf.setInstanceParams(instanceParamsList);
        return vnf;
    }

    private BuildingBlockExecution createBuildingBlockExecution() {
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable(GENERAL_BLOCK_EXECUTION_MAP_KEY, createGeneralBuildingBlock());
        return new DelegateExecutionImpl(execution);
    }

    private void setScopeAndAction(String scope, String action) {
        BuildingBlock buildingBlock = new BuildingBlock();
        buildingBlock.setBpmnScope(scope);
        buildingBlock.setBpmnAction(action);
        executeBuildingBlock.setBuildingBlock(buildingBlock);
        buildingBlockExecution.setVariable(BUILDING_BLOCK, executeBuildingBlock);
    }
}
