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
package org.onap.so.client.cds;

import com.google.gson.JsonParser;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
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
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.Vnfs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeneratePayloadForCdsTest {
    private static final String GENERIC_VNF_ID = "vnfId_configVnfTest1";
    private static final String VF_MODULE_ID = "vf-module-id-1";
    private static final String VF_MODULE_NAME = "vf-module-name-1";
    private static final String VF_MODULE_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce1";
    private static final String GENERIC_VNF_NAME = "vnf-name-1";
    private static final String SERVICE_INSTANCE_ID = "serviceInst_configTest";
    private static final String SERVICE_MODEL_UUID = "b45b5780-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String VNF_MODEL_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String GENERAL_BLOCK_EXECUTION_MAP_KEY = "gBBInput";
    private BuildingBlockExecution buildingBlockExecution;
    private static final String VNF_SCOPE = "vnf";
    private static final String VF_SCOPE = "vf-module";
    private static final String ASSIGN_ACTION = "config-assign";
    private static final String DEPLOY_ACTION = "config-deploy";
    private static final String MSO_REQUEST_ID = "1234";
    private static final String BUILDING_BLOCK = "buildingBlock";
    private ExecuteBuildingBlock executeBuildingBlock;

    @Mock
    protected ExtractPojosForBB extractPojosForBB;

    @Before
    public void setup() {
        buildingBlockExecution = createBuildingBlockExecution();
        executeBuildingBlock = new ExecuteBuildingBlock();
        extractPojosForBB = mock(ExtractPojosForBB.class);
    }

    @Test
    public void testRequestPayloadForConfigAssignVnf() throws Exception {
        // given
        setScopeAndAction(VNF_SCOPE, ASSIGN_ACTION);
        GeneratePayloadForCds configurePayloadForCds =
                new GeneratePayloadForCds(buildingBlockExecution, extractPojosForBB);
        ServiceInstance instance = createServiceInstance();
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.SERVICE_INSTANCE_ID))
                .thenReturn(instance);
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.GENERIC_VNF_ID))
                .thenReturn(createGenericVnf());

        // when
        String payload = configurePayloadForCds.generateConfigPropertiesPayload().get();

        // verify
        assertNotNull(payload);
        assertTrue(verfiyJsonFromString(payload));
    }

    @Test
    public void testRequestPayloadForConfigDeplotyVnf() throws Exception {
        // given
        setScopeAndAction(VNF_SCOPE, DEPLOY_ACTION);
        GeneratePayloadForCds configurePayloadForCds =
                new GeneratePayloadForCds(buildingBlockExecution, extractPojosForBB);
        ServiceInstance instance = createServiceInstance();
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.SERVICE_INSTANCE_ID))
                .thenReturn(instance);
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.GENERIC_VNF_ID))
                .thenReturn(createGenericVnf());

        // when
        String payload = configurePayloadForCds.generateConfigPropertiesPayload().get();

        // verfiy
        assertNotNull(payload);
        assertTrue(verfiyJsonFromString(payload));
    }

    @Test
    public void testRequestPayloadForConfigAssignVfModule() throws Exception {
        // given
        setScopeAndAction(VF_SCOPE, DEPLOY_ACTION);
        GeneratePayloadForCds configurePayloadForCds =
                new GeneratePayloadForCds(buildingBlockExecution, extractPojosForBB);
        ServiceInstance serviceInstance = createServiceInstance();
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.SERVICE_INSTANCE_ID))
                .thenReturn(serviceInstance);
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.GENERIC_VNF_ID))
                .thenReturn(createGenericVnf());
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.VF_MODULE_ID))
                .thenReturn(createVfModule());

        // when
        String payload = configurePayloadForCds.generateConfigPropertiesPayload().get();

        // verify
        assertNotNull(payload);
        assertTrue(verfiyJsonFromString(payload));
    }

    @Test
    public void testAbstractCdsPropertiesBean() throws Exception {
        // given
        setScopeAndAction(VNF_SCOPE, ASSIGN_ACTION);
        GeneratePayloadForCds configurePayloadForCds =
                new GeneratePayloadForCds(buildingBlockExecution, extractPojosForBB);

        ServiceInstance instance = createServiceInstance();
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.SERVICE_INSTANCE_ID))
                .thenReturn(instance);
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.GENERIC_VNF_ID))
                .thenReturn(createGenericVnf());

        // when
        AbstractCDSPropertiesBean cdsPropertiesBean = configurePayloadForCds.buildCdsPropertiesBean();

        // verify
        assertNotNull(cdsPropertiesBean);
    }

    private BuildingBlockExecution createBuildingBlockExecution() {
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable(GENERAL_BLOCK_EXECUTION_MAP_KEY, createGeneralBuildingBlock());
        return new DelegateExecutionImpl(execution);
    }

    private GeneralBuildingBlock createGeneralBuildingBlock() {
        GeneralBuildingBlock generalBuildingBlock = new GeneralBuildingBlock();
        RequestContext requestContext = new RequestContext();
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUserParams(createRequestUserParamsForVnf());
        requestContext.setRequestParameters(requestParameters);
        requestContext.setMsoRequestId(MSO_REQUEST_ID);
        generalBuildingBlock.setRequestContext(requestContext);
        return generalBuildingBlock;
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
        genericVnf.setBlueprintName("test");
        genericVnf.setBlueprintVersion("1.0.0");
        ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
        modelInfoGenericVnf.setModelCustomizationUuid(VNF_MODEL_CUSTOMIZATION_UUID);
        genericVnf.setModelInfoGenericVnf(modelInfoGenericVnf);
        return genericVnf;
    }

    private VfModule createVfModule() {
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId(VF_MODULE_ID);
        vfModule.setVfModuleName(VF_MODULE_NAME);
        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        modelInfoVfModule.setModelCustomizationUUID(VF_MODULE_CUSTOMIZATION_UUID);
        vfModule.setModelInfoVfModule(modelInfoVfModule);
        return vfModule;
    }

    private List<Map<String, Object>> createRequestUserParamsForVnf() {
        List<Map<String, Object>> userParams = new ArrayList<>();
        Map<String, Object> userParamMap = new HashMap<>();
        userParamMap.put("service", getUserParamsForVnf());
        userParams.add(userParamMap);
        return userParams;
    }

    private Service getUserParamsForVnf() {
        Service service = new Service();
        Resources resources = new Resources();
        resources.setVnfs(createVnfList());
        service.setResources(resources);
        return service;
    }

    private List<Vnfs> createVnfList() {
        List<Map<String, String>> instanceParamsListSearchedVnf = new ArrayList<>();
        Map<String, String> instanceParam = new HashMap<>();
        instanceParam.put("public_net_id", "public-net-id");
        instanceParam.put("acl-cloud-region", "acl-cloud-region");
        instanceParamsListSearchedVnf.add(instanceParam);
        Vnfs searchedVnf = createVnf(instanceParamsListSearchedVnf);
        List<Vnfs> vnfList = new ArrayList<>();
        vnfList.add(searchedVnf);
        return vnfList;
    }

    private Vnfs createVnf(List<Map<String, String>> instanceParamsList) {
        Vnfs vnf = new Vnfs();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(VNF_MODEL_CUSTOMIZATION_UUID);
        vnf.setModelInfo(modelInfo);
        vnf.setInstanceParams(instanceParamsList);
        return vnf;
    }

    private boolean verfiyJsonFromString(String payload) {
        JsonParser parser = new JsonParser();
        return parser.parse(payload).isJsonObject();
    }

    private void setScopeAndAction(String scope, String action) {
        BuildingBlock buildingBlock = new BuildingBlock();
        buildingBlock.setBpmnScope(scope);
        buildingBlock.setBpmnAction(action);
        executeBuildingBlock.setBuildingBlock(buildingBlock);
        buildingBlockExecution.setVariable(BUILDING_BLOCK, executeBuildingBlock);
    }
}
