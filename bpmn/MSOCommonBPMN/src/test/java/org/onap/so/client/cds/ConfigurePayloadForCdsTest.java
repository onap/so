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
package org.onap.so.client.cds;

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
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.Vnfs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurePayloadForCdsTest {
    private static final String GENERIC_VNF_ID_1 = "vnfId_configVnfTest1";
    private static final String GENERIC_VNF_ID_2 = "vnfId_configVnfTest2";
    private static final String GENERIC_VNF_NAME_2 = "vnf-name-2";
    private static final String GENERIC_VNF_NAME_1 = "vnf-name-1";
    private static final String SERVICE_INSTANCE_ID = "serviceInst_configTest";
    private static final String SERVICE_MODEL_UUID = "b45b5780-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String VNF_MODEL_CUSTOMIZATION_UUID_1 = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String VNF_MODEL_CUSTOMIZATION_UUID_2 = "23ce9f74-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String GENERAL_BLOCK_EXECUTION_MAP_KEY = "gBBInput";
    private BuildingBlockExecution buildingBlockExecution;
    private ExtractPojosForBB extractPojosForBB;

    @Before
    public void setup() {
        buildingBlockExecution = createBuildingBlockExecution();
        extractPojosForBB = mock(ExtractPojosForBB.class);
    }

    @Test
    public void testRequestPayloadForConfigAssignVnf() throws Exception {
        // given
        ConfigurePayloadForCds configurePayloadForCds = new ConfigurePayloadForCds(buildingBlockExecution, "vnf", "assign", extractPojosForBB);
        List<GenericVnf> genericVnfs = new ArrayList<>();
        genericVnfs.add(createFirstGenericVnf());
        genericVnfs.add(createSecondGenericVnf());
        ServiceInstance instance = createServiceInstance(genericVnfs);
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.SERVICE_INSTANCE_ID))
                .thenReturn(instance);
        // when
        String payload = configurePayloadForCds.buildPayloadForCds();

        // verify
        assertNotNull(payload);
    }

    @Test
    public void testRequestPayloadForConfigDeployVnf() throws Exception {
        // given
        ConfigurePayloadForCds configurePayloadForCds = new ConfigurePayloadForCds(buildingBlockExecution, "vnf", "deploy", extractPojosForBB);
        List<GenericVnf> genericVnfs = new ArrayList<>();
        genericVnfs.add(createSecondGenericVnf());
        ServiceInstance instance = createServiceInstance(genericVnfs);
        when(extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.SERVICE_INSTANCE_ID))
                .thenReturn(instance);
        // when
        String payload = configurePayloadForCds.buildPayloadForCds();

        // verfiy
        assertNotNull(payload);
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
        requestParameters.setUserParams(createRequestUserParams());
        requestContext.setRequestParameters(requestParameters);
        generalBuildingBlock.setRequestContext(requestContext);
        return generalBuildingBlock;
    }

    private ServiceInstance createServiceInstance(List<GenericVnf> vnfs) {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(SERVICE_INSTANCE_ID);
        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelUuid(SERVICE_MODEL_UUID);
        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);
        serviceInstance.getVnfs().addAll(vnfs);
        return serviceInstance;
    }

    private GenericVnf createFirstGenericVnf() {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId(GENERIC_VNF_ID_1);
        genericVnf.setVnfName(GENERIC_VNF_NAME_1);
        ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
        modelInfoGenericVnf.setModelCustomizationUuid(VNF_MODEL_CUSTOMIZATION_UUID_1);
        genericVnf.setModelInfoGenericVnf(modelInfoGenericVnf);
        return genericVnf;
    }

    private GenericVnf createSecondGenericVnf() {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId(GENERIC_VNF_ID_2);
        genericVnf.setVnfName(GENERIC_VNF_NAME_2);
        ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
        modelInfoGenericVnf.setModelCustomizationUuid(VNF_MODEL_CUSTOMIZATION_UUID_2);
        genericVnf.setModelInfoGenericVnf(modelInfoGenericVnf);
        return genericVnf;
    }

    private List<Map<String, Object>> createRequestUserParams() {
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
        instanceParam.put("public_net_id", "public-net-id");
        instanceParam.put("acl-cloud-region", "acl-cloud-region");
        instanceParamsListSearchedVnf.add(instanceParam);
        Vnfs searchedVnf = createVnf(VNF_MODEL_CUSTOMIZATION_UUID_1, instanceParamsListSearchedVnf);
        List<Vnfs> vnfList = new ArrayList<>();
        vnfList.add(searchedVnf);
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
