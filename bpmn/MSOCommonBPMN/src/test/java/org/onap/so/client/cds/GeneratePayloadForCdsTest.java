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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.PayloadGenerationException;
import org.onap.so.serviceinstancebeans.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GeneratePayloadForCdsTest {
    private static final String GENERIC_VNF_ID = "vnfId_configVnfTest1";
    private static final String VF_MODULE_ID = "vf-module-id-1";
    private static final String VF_MODULE_NAME = "vf-module-name-1";
    private static final String VF_MODULE_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce1";
    private static final String GENERIC_VNF_NAME = "vnf-name-1";
    private static final String SERVICE_INSTANCE_ID = "serviceInst_configTest";
    private static final String SERVICE_MODEL_UUID = "b45b5780-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String SERVICE_INSTANCE_NAME = "test-service-instance";
    private static final String VNF_MODEL_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String GENERAL_BLOCK_EXECUTION_MAP_KEY = "gBBInput";
    private static final String VNF_SCOPE = "vnf";
    private static final String SERVICE_SCOPE = "service";
    private static final String SERVICE_ACTION = "create";
    private static final String VF_SCOPE = "vfModule";
    private static final String ASSIGN_ACTION = "configAssign";
    private static final String DEPLOY_ACTION = "configDeploy";
    private static final String MSO_REQUEST_ID = "1234";
    private static final String BUILDING_BLOCK = "buildingBlock";
    private static final String PUBLIC_NET_ID = "public-net-id";
    private static final String CLOUD_REGION = "acl-cloud-region";

    private BuildingBlockExecution buildingBlockExecution;
    private ExecuteBuildingBlock executeBuildingBlock;

    @InjectMocks
    private GeneratePayloadForCds configurePayloadForCds;

    @Mock
    private VnfCDSRequestProvider vnfCDSRequestProvider;

    @Mock
    private VfModuleCDSRequestProvider vfModuleCDSRequestProvider;

    @Mock
    private ServiceCDSRequestProvider serviceCDSRequestProvider;


    @Before
    public void setup() {
        buildingBlockExecution = createBuildingBlockExecution();
        executeBuildingBlock = new ExecuteBuildingBlock();
    }

    @Test
    public void testBuildCdsPropertiesBeanAssignVnf() throws Exception {
        // given
        final String assignPayload =
                "{\"configAssign-request\":{\"resolution-key\":\"vnf-name-1\",\"configAssign-properties\":{\"service-instance-id\":\"serviceInst_configTest\",\"service-model-uuid\":\"b45b5780-e5dd-11e9-81b4-2a2ae2dbcce4\",\"vnf-id\":\"vnfId_configVnfTest1\",\"vnf-name\":\"vnf-name-1\",\"vnf-customization-uuid\":\"23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce4\",\"acl-cloud-region\":\"acl-cloud-region\",\"public_net_id\":\"public-net-id\"}}}";

        setScopeAndAction(VNF_SCOPE, ASSIGN_ACTION);

        doReturn(Optional.of(assignPayload)).when(vnfCDSRequestProvider).buildRequestPayload(ASSIGN_ACTION);

        // when
        AbstractCDSPropertiesBean propertyBean = configurePayloadForCds.buildCdsPropertiesBean(buildingBlockExecution);

        // verify
        assertNotNull(propertyBean);
        String payload = propertyBean.getRequestObject();
        assertThat(assignPayload.equals(payload));
        assertThat(propertyBean.getRequestId().equals(MSO_REQUEST_ID));
        assertThat(propertyBean.getOriginatorId().equals("SO"));
        assertNotNull(propertyBean.getSubRequestId());
        assertThat(propertyBean.getActionName().equals(ASSIGN_ACTION));
        assertThat(propertyBean.getMode().equalsIgnoreCase("sync"));
    }

    @Test
    public void testBuildCdsPropertiesBeanDeployVnf() throws Exception {
        // given
        final String deployPayload =
                "{\"configDeploy-request\":{\"resolution-key\":\"vnf-name-1\",\"configDeploy-properties\":{\"service-instance-id\":\"serviceInst_configTest\",\"service-model-uuid\":\"b45b5780-e5dd-11e9-81b4-2a2ae2dbcce4\",\"vnf-id\":\"vnfId_configVnfTest1\",\"vnf-name\":\"vnf-name-1\",\"vnf-customization-uuid\":\"23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce4\",\"acl-cloud-region\":\"acl-cloud-region\",\"public_net_id\":\"public-net-id\"}}}";
        setScopeAndAction(VNF_SCOPE, DEPLOY_ACTION);

        doReturn(Optional.of(deployPayload)).when(vnfCDSRequestProvider).buildRequestPayload(DEPLOY_ACTION);

        // when
        AbstractCDSPropertiesBean propertyBean = configurePayloadForCds.buildCdsPropertiesBean(buildingBlockExecution);

        // verify
        assertNotNull(propertyBean);
        String payload = propertyBean.getRequestObject();
        assertThat(deployPayload.equals(payload));
        assertThat(propertyBean.getRequestId().equals(MSO_REQUEST_ID));
        assertThat(propertyBean.getOriginatorId().equals("SO"));
        assertNotNull(propertyBean.getSubRequestId());
        assertThat(propertyBean.getActionName().equals(DEPLOY_ACTION));
        assertThat(propertyBean.getMode().equalsIgnoreCase("sync"));
    }

    @Test
    public void testBuildCdsPropertiesBeanCreateService() throws Exception {
        // given
        final String servicePayload =
                "{\"create-request\":{\"resolution-key\":\"test-service-instance\",\"create-properties\":{\"service-instance-id\":\"serviceInst_configTest\",\"service-model-uuid\":\"b45b5780-e5dd-11e9-81b4-2a2ae2dbcce4\"}}}";
        setScopeAndAction(SERVICE_SCOPE, SERVICE_ACTION);
        doReturn(Optional.of(servicePayload)).when(serviceCDSRequestProvider).buildRequestPayload(SERVICE_ACTION);

        // when
        AbstractCDSPropertiesBean propertyBean = configurePayloadForCds.buildCdsPropertiesBean(buildingBlockExecution);

        // verify
        assertNotNull(propertyBean);
        String payload = propertyBean.getRequestObject();
        assertThat(servicePayload.equals(payload));
        assertThat(propertyBean.getRequestId().equals(MSO_REQUEST_ID));
        assertThat(propertyBean.getOriginatorId().equals("SO"));
        assertNotNull(propertyBean.getSubRequestId());
        assertThat(propertyBean.getActionName().equals(SERVICE_ACTION));
        assertThat(propertyBean.getMode().equalsIgnoreCase("sync"));
    }

    @Test
    public void testBuildCdsPropertiesBeanConfigDeployVfModule() throws Exception {
        // given
        final String deployVfModulePayload =
                "{\"configDeploy-request\":{\"resolution-key\":\"vf-module-name-1\",\"template-prefix\":\"vf-module-name-1configDeploy\",\"configDeploy-properties\":{\"service-instance-id\":\"serviceInst_configTest\",\"service-model-uuid\":\"b45b5780-e5dd-11e9-81b4-2a2ae2dbcce4\",\"vnf-id\":\"vnfId_configVnfTest1\",\"vnf-name\":\"vnf-name-1\",\"vf-module-id\":\"vf-module-id-1\",\"vf-module-name\":\"vf-module-name-1\",\"vf-module-customization-uuid\":\"23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce1\",\"aci-cloud-region-vf-module\":\"acl-cloud-region\",\"public-net-vf-module-id\":\"public-net-id\"}}}";
        setScopeAndAction(VF_SCOPE, DEPLOY_ACTION);
        doReturn(Optional.of(deployVfModulePayload)).when(vfModuleCDSRequestProvider)
                .buildRequestPayload(DEPLOY_ACTION);

        // when
        AbstractCDSPropertiesBean propertyBean = configurePayloadForCds.buildCdsPropertiesBean(buildingBlockExecution);

        // verify
        assertNotNull(propertyBean);
        String payload = propertyBean.getRequestObject();
        assertThat(deployVfModulePayload.equals(payload));
        assertThat(propertyBean.getRequestId().equals(MSO_REQUEST_ID));
        assertThat(propertyBean.getOriginatorId().equals("SO"));
        assertNotNull(propertyBean.getSubRequestId());
        assertThat(propertyBean.getActionName().equals(DEPLOY_ACTION));
        assertThat(propertyBean.getMode().equalsIgnoreCase("sync"));
    }

    @Test
    public void testFailureWhenServiceInstanceIsNotPresent() throws Exception {
        // given
        setScopeAndAction(VNF_SCOPE, ASSIGN_ACTION);

        doThrow(PayloadGenerationException.class).when(serviceCDSRequestProvider).buildRequestPayload(ASSIGN_ACTION);

        // when
        final Throwable throwable =
                catchThrowable(() -> configurePayloadForCds.buildCdsPropertiesBean(buildingBlockExecution));

        // verify
        assertThat(throwable).isInstanceOf(PayloadGenerationException.class)
                .hasMessage("Failed to build payload for CDS");
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
        requestContext.setMsoRequestId(MSO_REQUEST_ID);
        generalBuildingBlock.setRequestContext(requestContext);
        return generalBuildingBlock;
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
        instanceParam.put("public_net_id", PUBLIC_NET_ID);
        instanceParam.put("acl-cloud-region", CLOUD_REGION);
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

    private void setScopeAndAction(String scope, String action) {
        BuildingBlock buildingBlock = new BuildingBlock();
        buildingBlock.setBpmnScope(scope);
        buildingBlock.setBpmnAction(action);
        executeBuildingBlock.setBuildingBlock(buildingBlock);
        buildingBlockExecution.setVariable(BUILDING_BLOCK, executeBuildingBlock);
    }
}
