/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra.
 * Copyright (C) 2019 Nokia.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.Vnfs;

public class ConfigAssignVnfTest {

    private static final String GENERIC_VNF_ID = "vnfId_configVnfTest";
    private static final String GENERIC_VNF_NAME = "vnfName_configVnfTest";
    private static final String VNF_MODEL_CUSTOMIZATION_ID = "0c1ac643-377e-475b-be50-6be65f91a7ad";
    private static final String SERVICE_INSTANCE_ID = "serviceInst_configTest";
    private static final String SERVICE_MODEL_UUID = "5af91c26-8418-4d3f-944c-965842deda94";
    private static final String TARGET_VNF_MODEL_CUSTOMIZATION_UUID = "0c1ac643-377e-475b-be50-6be65f91a7ad";
    private static final String GENERAL_BLOCK_EXECUTION_MAP_KEY = "gBBInput";
    private static final int THE_NUMBER_OF_EXPECTED_CONFIG_PROPERTIES = 8;

    private static final String INSTANCE_PARAM1_NAME = "paramName1";
    private static final String INSTANCE_PARAM1_VALUE = "paramValue1";
    private static final String INSTANCE_PARAM2_NAME = "paramName2";
    private static final String INSTANCE_PARAM2_VALUE = "paramValue2";
    private static final String INSTANCE_PARAM3_NAME = "paramName3";
    private static final String INSTANCE_PARAM3_VALUE = "paramValue3";
    private static final String INSTANCE_PARAM4_NAME = "paramName4";
    private static final String INSTANCE_PARAM4_VALUE = "paramValue4";
    private static final String INSTANCE_PARAM5_NAME = "paramName5";
    private static final String INSTANCE_PARAM5_VALUE = "paramValue5";


    private ConfigAssignVnf testedObject;

    private BuildingBlockExecution buildingBlockExecution;
    private ExtractPojosForBB extractPojosForBB;

    @Before
    public void setup() {
        buildingBlockExecution = createBuildingBlockExecution();
        extractPojosForBB = mock(ExtractPojosForBB.class);
        testedObject = new ConfigAssignVnf(extractPojosForBB, new ExceptionBuilder());
    }

    @Test
    public void prepareAbstractCDSPropertiesBean_success() throws Exception {
        // given
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.GENERIC_VNF_ID))
                .thenReturn(createGenericVnf());
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.SERVICE_INSTANCE_ID))
                .thenReturn(createServiceInstance());
        // when
        testedObject.preProcessAbstractCDSProcessing(buildingBlockExecution);
        // then
        verifyConfigAssignPropertiesJsonContent();
    }

    private void verifyConfigAssignPropertiesJsonContent() throws Exception {
        AbstractCDSPropertiesBean abstractCDSPropertiesBean = buildingBlockExecution.getVariable("executionObject");
        String payload = abstractCDSPropertiesBean.getRequestObject();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode payloadJson = mapper.readTree(payload);
        JsonNode configAssignPropertiesNode = payloadJson.findValue("config-assign-properties");
        assertThat(configAssignPropertiesNode.size()).isEqualTo(THE_NUMBER_OF_EXPECTED_CONFIG_PROPERTIES);
        assertThat(configAssignPropertiesNode.get("service-instance-id").asText()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(configAssignPropertiesNode.get("vnf-id").asText()).isEqualTo(GENERIC_VNF_ID);
        assertThat(configAssignPropertiesNode.get("vnf-name").asText()).isEqualTo(GENERIC_VNF_NAME);
        assertThat(configAssignPropertiesNode.get("service-model-uuid").asText()).isEqualTo(SERVICE_MODEL_UUID);
        assertThat(configAssignPropertiesNode.get("vnf-customization-uuid").asText())
                .isEqualTo(VNF_MODEL_CUSTOMIZATION_ID);
        assertThat(configAssignPropertiesNode.has(INSTANCE_PARAM1_NAME)).isTrue();
        assertThat(configAssignPropertiesNode.get(INSTANCE_PARAM1_NAME).asText()).isEqualTo(INSTANCE_PARAM1_VALUE);
        assertThat(configAssignPropertiesNode.has(INSTANCE_PARAM2_NAME)).isTrue();
        assertThat(configAssignPropertiesNode.get(INSTANCE_PARAM2_NAME).asText()).isEqualTo(INSTANCE_PARAM2_VALUE);
        assertThat(configAssignPropertiesNode.has(INSTANCE_PARAM3_NAME)).isTrue();
        assertThat(configAssignPropertiesNode.get(INSTANCE_PARAM3_NAME).asText()).isEqualTo(INSTANCE_PARAM3_VALUE);
    }

    private BuildingBlockExecution createBuildingBlockExecution() {
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable(GENERAL_BLOCK_EXECUTION_MAP_KEY, createGeneralBuildingBlock());
        return new DelegateExecutionImpl(execution);
    }

    private ServiceInstance createServiceInstance() {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(SERVICE_INSTANCE_ID);
        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelUuid(SERVICE_MODEL_UUID);
        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);
        return serviceInstance;
    }

    private GenericVnf createGenericVnf() {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId(GENERIC_VNF_ID);
        genericVnf.setVnfName(GENERIC_VNF_NAME);
        ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
        modelInfoGenericVnf.setModelCustomizationUuid(TARGET_VNF_MODEL_CUSTOMIZATION_UUID);
        modelInfoGenericVnf.setBlueprintName("blueprintTest");
        modelInfoGenericVnf.setBlueprintVersion("blueprintVerTest");
        genericVnf.setModelInfoGenericVnf(modelInfoGenericVnf);
        return genericVnf;
    }

    private GeneralBuildingBlock createGeneralBuildingBlock() {
        GeneralBuildingBlock generalBuildingBlock = new GeneralBuildingBlock();
        RequestContext requestContext = new RequestContext();
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUserParams(createRequestUserParams());
        requestContext.setRequestParameters(requestParameters);
        generalBuildingBlock.setRequestContext(requestContext);
        return generalBuildingBlock;
    }

    private List<Map<String, Object>> createRequestUserParams() {
        List<Map<String, Object>> userParams = new ArrayList<>();
        Map<String, Object> userParamMap = new HashMap<>();
        userParamMap.put("service", createService());
        userParams.add(userParamMap);
        return userParams;
    }

    private Service createService() {
        Service service = new Service();
        Resources resources = new Resources();
        resources.setVnfs(createVnfList());
        service.setResources(resources);
        return service;
    }

    private List<Vnfs> createVnfList() {
        List<Map<String, String>> instanceParamsListSearchedVnf = new ArrayList<>();
        Map<String, String> instanceParam = new HashMap<>();
        instanceParam.put(INSTANCE_PARAM1_NAME, INSTANCE_PARAM1_VALUE);
        instanceParam.put(INSTANCE_PARAM2_NAME, INSTANCE_PARAM2_VALUE);
        Map<String, String> instanceParam2 = new HashMap<>();
        instanceParam2.put(INSTANCE_PARAM3_NAME, INSTANCE_PARAM3_VALUE);
        instanceParamsListSearchedVnf.add(instanceParam);
        instanceParamsListSearchedVnf.add(instanceParam2);
        Vnfs searchedVnf = createVnf(VNF_MODEL_CUSTOMIZATION_ID, instanceParamsListSearchedVnf);

        List<Map<String, String>> instanceParamsListForAnotherVnf = new ArrayList<>();
        Map<String, String> instanceParam3 = new HashMap<>();
        instanceParam3.put(INSTANCE_PARAM4_NAME, INSTANCE_PARAM4_VALUE);
        instanceParam3.put(INSTANCE_PARAM5_NAME, INSTANCE_PARAM5_VALUE);
        instanceParamsListForAnotherVnf.add(instanceParam3);
        Vnfs anotherVnf = createVnf("2d1ac656-377e-467b-be50-6ce65f66a7ca", instanceParamsListForAnotherVnf);

        List<Vnfs> vnfList = new ArrayList<>();
        vnfList.add(searchedVnf);
        vnfList.add(anotherVnf);
        return vnfList;
    }

    private Vnfs createVnf(String vnfModelCustomizationId, List<Map<String, String>> instanceParamsList) {
        Vnfs vnf = new Vnfs();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(vnfModelCustomizationId);
        vnf.setModelInfo(modelInfo);
        vnf.setInstanceParams(instanceParamsList);
        return vnf;
    }
}
