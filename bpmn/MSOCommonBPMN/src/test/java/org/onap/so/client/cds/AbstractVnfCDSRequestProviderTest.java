/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.client.cds;

import com.google.gson.JsonParser;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.serviceinstancebeans.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public abstract class AbstractVnfCDSRequestProviderTest {

    protected static final String GENERIC_VNF_ID = "vnfId_configVnfTest1";
    protected static final String VF_MODULE_ID = "vf-module-id-1";
    protected static final String VF_MODULE_NAME = "vf-module-name-1";
    protected static final String VF_MODULE_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce1";
    protected static final String GENERIC_VNF_NAME = "vnf-name-1";
    protected static final String SERVICE_INSTANCE_ID = "serviceInst_configTest";
    protected static final String SERVICE_MODEL_UUID = "b45b5780-e5dd-11e9-81b4-2a2ae2dbcce4";
    protected static final String SERVICE_INSTANCE_NAME = "test-service-instance";
    protected static final String VNF_MODEL_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce4";
    protected static final String GENERAL_BLOCK_EXECUTION_MAP_KEY = "gBBInput";
    protected static final String VNF_SCOPE = "vnf";
    protected static final String SERVICE_SCOPE = "service";
    protected static final String SERVICE_ACTION = "create";
    protected static final String VF_SCOPE = "vfModule";
    protected static final String ASSIGN_ACTION = "configAssign";
    protected static final String DEPLOY_ACTION = "configDeploy";
    protected static final String MSO_REQUEST_ID = "1234";
    protected static final String BUILDING_BLOCK = "buildingBlock";
    protected static final String PUBLIC_NET_ID = "public-net-id";
    protected static final String CLOUD_REGION = "acl-cloud-region";

    @Mock
    protected ExtractPojosForBB extractPojosForBB;

    protected BuildingBlockExecution buildingBlockExecution;

    protected ExecuteBuildingBlock executeBuildingBlock;


    @Before
    public void setUp() {
        buildingBlockExecution = createBuildingBlockExecution();
        executeBuildingBlock = new ExecuteBuildingBlock();
    }

    protected BuildingBlockExecution createBuildingBlockExecution() {
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable(GENERAL_BLOCK_EXECUTION_MAP_KEY, createGeneralBuildingBlock());
        return new DelegateExecutionImpl(execution);
    }

    protected GeneralBuildingBlock createGeneralBuildingBlock() {
        GeneralBuildingBlock generalBuildingBlock = new GeneralBuildingBlock();
        RequestContext requestContext = new RequestContext();
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUserParams(createRequestUserParams());
        requestContext.setRequestParameters(requestParameters);
        requestContext.setMsoRequestId(MSO_REQUEST_ID);
        generalBuildingBlock.setRequestContext(requestContext);
        return generalBuildingBlock;
    }

    protected ServiceInstance createServiceInstance() {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceName(SERVICE_INSTANCE_NAME);
        serviceInstance.setServiceInstanceId(SERVICE_INSTANCE_ID);
        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelUuid(SERVICE_MODEL_UUID);
        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);
        return serviceInstance;
    }

    protected GenericVnf createGenericVnf() {
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

    protected VfModule createVfModule() {
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId(VF_MODULE_ID);
        vfModule.setVfModuleName(VF_MODULE_NAME);
        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        modelInfoVfModule.setModelCustomizationUUID(VF_MODULE_CUSTOMIZATION_UUID);
        vfModule.setModelInfoVfModule(modelInfoVfModule);
        return vfModule;
    }

    protected List<Map<String, Object>> createRequestUserParams() {
        List<Map<String, Object>> userParams = new ArrayList<>();
        Map<String, Object> userParamMap = new HashMap<>();
        userParamMap.put("service", getUserParams());
        userParams.add(userParamMap);
        return userParams;
    }

    protected Service getUserParams() {
        Service service = new Service();
        Resources resources = new Resources();
        resources.setVnfs(createVnfList());
        service.setResources(resources);
        return service;
    }

    protected List<Vnfs> createVnfList() {
        List<Map<String, String>> instanceParamsListSearchedVnf = new ArrayList<>();
        Map<String, String> instanceParam = new HashMap<>();
        instanceParam.put("public_net_id", PUBLIC_NET_ID);
        instanceParam.put("acl-cloud-region", CLOUD_REGION);
        instanceParamsListSearchedVnf.add(instanceParam);
        Vnfs searchedVnf = createVnf(instanceParamsListSearchedVnf);
        List<Vnfs> vnfList = new ArrayList<>();
        vnfList.add(searchedVnf);
        return vnfList;
    }

    protected Vnfs createVnf(List<Map<String, String>> instanceParamsList) {
        Vnfs vnf = new Vnfs();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(VNF_MODEL_CUSTOMIZATION_UUID);
        vnf.setModelInfo(modelInfo);
        vnf.setInstanceParams(instanceParamsList);

        // Set instance parameters and modelinfo for vf-module
        VfModules vfModule = new VfModules();
        ModelInfo modelInfoForVfModule = new ModelInfo();
        modelInfoForVfModule.setModelCustomizationId(VF_MODULE_CUSTOMIZATION_UUID);
        vfModule.setModelInfo(modelInfoForVfModule);

        List<Map<String, String>> instanceParamsListSearchedVfModule = new ArrayList<>();
        Map<String, String> instanceParams = new HashMap<>();
        instanceParams.put("public-net-vf-module-id", PUBLIC_NET_ID);
        instanceParams.put("aci-cloud-region-vf-module", CLOUD_REGION);

        instanceParamsListSearchedVfModule.add(instanceParams);
        vfModule.setInstanceParams(instanceParamsListSearchedVfModule);

        List<VfModules> vfModules = new ArrayList<>();
        vfModules.add(vfModule);

        vnf.setVfModules(vfModules);

        return vnf;
    }

    protected boolean verfiyJsonFromString(String payload) {
        JsonParser parser = new JsonParser();
        return parser.parse(payload).isJsonObject();
    }

    protected void setScopeAndAction(String scope, String action) {
        BuildingBlock buildingBlock = new BuildingBlock();
        buildingBlock.setBpmnScope(scope);
        buildingBlock.setBpmnAction(action);
        executeBuildingBlock.setBuildingBlock(buildingBlock);
        buildingBlockExecution.setVariable(BUILDING_BLOCK, executeBuildingBlock);
    }

    protected void setScopeAndActionWithoutUserParams(String scope, String action) {
        buildingBlockExecution.getGeneralBuildingBlock().getRequestContext().getRequestParameters()
                .setUserParams(new LinkedList<>());
        setScopeAndAction(scope, action);
    }
}
