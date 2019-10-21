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
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenericCDSProcessingTest extends BaseTaskTest {

    private static final String VNF_SCOPE = "vnf";
    private static final String VF_SCOPE = "vfModule";
    private static final String TEST_VF_MODULE_ID = "vf-module-id-1";
    private static final String TEST_VF_MODULE_NAME = "vf-module-name-1";
    private static final String TEST_VF_MODULE_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce1";
    private static final String TEST_GENERIC_VNF_ID = "vnfId_configVnfTest1";
    private static final String TEST_GENERIC_VNF_NAME = "vnf-name-1";
    private static final String TEST_VNF_MODEL_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String DEPLOY_ACTION_FOR_CDS = "configDeploy";
    private static final String GENERAL_BLOCK_EXECUTION_MAP_KEY = "gBBInput";
    private static final String BUILDING_BLOCK = "buildingBlock";
    private static final String TEST_MSO_REQUEST_ID = "ff874603-4222-11e7-9252-005056850d2e";
    private static final String TEST_SERVICE_INSTANCE_ID = "serviceInst_configTest";
    private static final String TEST_SERVICE_MODEL_UUID = "b45b5780-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String EXECUTION_OBJECT = "executionObject";
    private static final String BLUEPRINT_NAME = "test";
    private static final String BLUEPRINT_VERSION = "1.0.0";

    @Mock
    private ExtractPojosForBB extractPojosForBB;

    @Mock
    private ExceptionBuilder exceptionBuilder;

    private BuildingBlockExecution buildingBlockExecution;
    private ExecuteBuildingBlock executeBuildingBlock;

    @Before
    public void setUp() {
        buildingBlockExecution = createBuildingBlockExecution();
        executeBuildingBlock = new ExecuteBuildingBlock();
        extractPojosForBB = mock(ExtractPojosForBB.class);
    }

    @Test
    public void testExecutionObjectCreationForVnf() throws Exception {
        // given
        ControllerRunnable<BuildingBlockExecution> controllerRunnable =
                new GenericCDSProcessing<>(exceptionBuilder, extractPojosForBB);
        ControllerContext<BuildingBlockExecution> controllerContext = new ControllerContext<>();
        controllerContext.setExecution(buildingBlockExecution);
        controllerContext.setControllerActor("CDS");
        setScopeAndAction(VNF_SCOPE, DEPLOY_ACTION_FOR_CDS);
        ServiceInstance instance = createServiceInstance();

        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.SERVICE_INSTANCE_ID))
                .thenReturn(instance);
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.GENERIC_VNF_ID))
                .thenReturn(createGenericVnf());

        // when
        Boolean isUnderstandable = controllerRunnable.understand(controllerContext);
        Boolean isReady= controllerRunnable.ready(controllerContext);
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

    private VfModule createVfModule() {
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId(TEST_VF_MODULE_ID);
        vfModule.setVfModuleName(TEST_VF_MODULE_NAME);
        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        modelInfoVfModule.setModelCustomizationUUID(TEST_VF_MODULE_CUSTOMIZATION_UUID);
        vfModule.setModelInfoVfModule(modelInfoVfModule);
        return vfModule;
    }

    private GenericVnf createGenericVnf() {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId(TEST_GENERIC_VNF_ID);
        genericVnf.setVnfName(TEST_GENERIC_VNF_NAME);
        genericVnf.setBlueprintName(BLUEPRINT_NAME);
        genericVnf.setBlueprintVersion(BLUEPRINT_VERSION);
        ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
        modelInfoGenericVnf.setModelCustomizationUuid(TEST_VNF_MODEL_CUSTOMIZATION_UUID);
        genericVnf.setModelInfoGenericVnf(modelInfoGenericVnf);
        return genericVnf;
    }

    private ServiceInstance createServiceInstance() {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(TEST_SERVICE_INSTANCE_ID);
        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelUuid(TEST_SERVICE_MODEL_UUID);
        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);
        return serviceInstance;
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
