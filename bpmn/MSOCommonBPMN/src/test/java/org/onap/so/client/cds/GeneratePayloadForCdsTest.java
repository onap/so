/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada.
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
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
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.PayloadGenerationException;
import org.onap.so.serviceinstancebeans.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GeneratePayloadForCdsTest {
    private static final String VF_MODULE_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce1";
    private static final String VNF_MODEL_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String GENERAL_BLOCK_EXECUTION_MAP_KEY = "gBBInput";
    private static final String VNF_SCOPE = "vnf";
    private static final String SERVICE_SCOPE = "service";
    private static final String SERVICE_ACTION = "create";
    private static final String VF_SCOPE = "vfmodule";
    private static final String ASSIGN_ACTION = "configAssign";
    private static final String DEPLOY_ACTION = "configDeploy";
    private static final String DOWNLOAD_ACTION = "downloadNESw";
    private static final String MSO_REQUEST_ID = "msoRequestId";
    private static final String BUILDING_BLOCK = "buildingBlock";
    private static final String PUBLIC_NET_ID = "public-net-id";
    private static final String CLOUD_REGION = "acl-cloud-region";
    private static final String TEST_MODEL_UUID = "6bc0b04d-1873-4721-b53d-6615225b2a28";
    private static final String TEST_SERVICE_INSTANCE_ID = "test_service_id";
    private static final String TEST_PROCESS_KEY = "processKey1";
    private static final String TEST_PNF_RESOURCE_INSTANCE_NAME = "PNF_demo_resource";
    private static final String TEST_PNF_CORRELATION_ID = "PNFDemo";
    private static final String TEST_PNF_RESOURCE_CUSTOMIZATION_UUID = "9acb3a83-8a52-412c-9a45-901764938144";
    private static final String TEST_MSO_REQUEST_ID = "ff874603-4222-11e7-9252-005056850d2e";
    private static final String TEST_PNF_UUID = "5df8b6de-2083-11e7-93ae-92361f002671";
    private static final String TEST_SOFTWARE_VERSION = "demo-sw-ver2.0.0";
    private static final String PNF_CORRELATION_ID = "pnfCorrelationId";
    private static final String PNF_UUID = "pnfUuid";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    private static final String MODEL_UUID = "modelUuid";
    private static final String PRC_CUSTOMIZATION_UUID = "PRC_customizationUuid";
    private static final String PRC_INSTANCE_NAME = "PRC_instanceName";
    private static final String PRC_TARGET_SOFTWARE_VERSION = "targetSoftwareVersion";
    private static final String SCOPE = "scope";
    private static final String ACTION = "action";
    private static final String PROCESS_KEY = "testProcessKey";
    private static final String PRC_BLUEPRINT_NAME = "PRC_blueprintName";
    private static final String PRC_BLUEPRINT_VERSION = "PRC_blueprintVersion";
    private static final String TEST_PNF_RESOURCE_BLUEPRINT_NAME = "blueprintOnap";
    private static final String TEST_PNF_RESOURCE_BLUEPRINT_VERSION = "1.0.1";

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

    @Mock
    private PnfCDSRequestProvider pnfCDSRequestProvider;


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
        assertThat(assignPayload).isEqualTo(payload);
        assertThat(propertyBean.getRequestId()).isEqualTo(TEST_MSO_REQUEST_ID);
        assertThat(propertyBean.getOriginatorId()).isEqualTo("SO");
        assertNotNull(propertyBean.getSubRequestId());
        assertThat(propertyBean.getActionName()).isEqualTo(ASSIGN_ACTION);
        assertThat(propertyBean.getMode()).isEqualToIgnoringCase("sync");
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
        assertThat(propertyBean.getRequestId()).isEqualTo(TEST_MSO_REQUEST_ID);
        assertThat(propertyBean.getOriginatorId()).isEqualTo("SO");
        assertNotNull(propertyBean.getSubRequestId());
        assertThat(propertyBean.getActionName()).isEqualTo(DEPLOY_ACTION);
        assertThat(propertyBean.getMode()).isEqualToIgnoringCase("sync");
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
        assertThat(servicePayload).isEqualTo(payload);
        assertThat(propertyBean.getRequestId()).isEqualTo(TEST_MSO_REQUEST_ID);
        assertThat(propertyBean.getOriginatorId()).isEqualTo("SO");
        assertNotNull(propertyBean.getSubRequestId());
        assertThat(propertyBean.getActionName()).isEqualTo(SERVICE_ACTION);
        assertThat(propertyBean.getMode()).isEqualToIgnoringCase("sync");
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
        assertThat(propertyBean.getRequestId()).isEqualTo(TEST_MSO_REQUEST_ID);
        assertThat(propertyBean.getOriginatorId()).isEqualTo("SO");
        assertNotNull(propertyBean.getSubRequestId());
        assertThat(propertyBean.getActionName()).isEqualTo(DEPLOY_ACTION);
        assertThat(propertyBean.getMode()).isEqualToIgnoringCase("sync");
    }

    @Test
    public void testBuildCdsPropertiesBeanDownloadPnf() throws Exception {
        // given
        final String downloadPayload =
                "{\"downloadNeSw-request\":{\"resolution-key\":\"PNFDemo\",\"downloadNeSw-properties\":{\"service-instance-id\":\"test_service_id\",\"service-model-uuid\":\"6bc0b04d-1873-4721-b53d-6615225b2a28\",\"pnf-id\":\"5df8b6de-2083-11e7-93ae-92361f002671\",\"pnf-name\":\"PNFDemo\",\"pnf-customization-uuid\":\"9acb3a83-8a52-412c-9a45-901764938144\",\"target-software-version\":\"demo-sw-ver2.0.0\"}}}";
        DelegateExecution execution = prepareDelegateExecutionObj(PayloadConstants.PNF_SCOPE, DOWNLOAD_ACTION);
        doReturn(Optional.of(downloadPayload)).when(pnfCDSRequestProvider).buildRequestPayload(DOWNLOAD_ACTION);
        doReturn(TEST_PNF_RESOURCE_BLUEPRINT_NAME).when(pnfCDSRequestProvider).getBlueprintName();
        doReturn(TEST_PNF_RESOURCE_BLUEPRINT_VERSION).when(pnfCDSRequestProvider).getBlueprintVersion();

        // when
        AbstractCDSPropertiesBean propertyBean = configurePayloadForCds.buildCdsPropertiesBean(execution);

        // verify
        assertNotNull(propertyBean);
        String payload = propertyBean.getRequestObject();
        assertThat(downloadPayload).isEqualTo(payload);
        assertThat(propertyBean.getRequestId()).isEqualTo(TEST_MSO_REQUEST_ID);
        assertThat(propertyBean.getOriginatorId()).isEqualTo("SO");
        assertNotNull(propertyBean.getSubRequestId());
        assertThat(propertyBean.getActionName()).isEqualTo(DOWNLOAD_ACTION);
        assertThat(propertyBean.getMode()).isEqualToIgnoringCase("async");
        assertThat(propertyBean.getBlueprintName()).isEqualToIgnoringCase(TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        assertThat(propertyBean.getBlueprintVersion()).isEqualToIgnoringCase(TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
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
        requestContext.setMsoRequestId(TEST_MSO_REQUEST_ID);
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

    private DelegateExecution prepareDelegateExecutionObj(String scope, String action) {
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable(PROCESS_KEY, TEST_PROCESS_KEY);
        execution.setVariable(PNF_CORRELATION_ID, TEST_PNF_CORRELATION_ID);
        execution.setVariable(MODEL_UUID, TEST_MODEL_UUID);
        execution.setVariable(SERVICE_INSTANCE_ID, TEST_SERVICE_INSTANCE_ID);
        execution.setVariable(MSO_REQUEST_ID, TEST_MSO_REQUEST_ID);
        execution.setVariable(PNF_UUID, TEST_PNF_UUID);
        execution.setVariable(PRC_INSTANCE_NAME, TEST_PNF_RESOURCE_INSTANCE_NAME);
        execution.setVariable(PRC_CUSTOMIZATION_UUID, TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        execution.setVariable(PRC_TARGET_SOFTWARE_VERSION, TEST_SOFTWARE_VERSION);
        execution.setVariable(PRC_BLUEPRINT_NAME, TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        execution.setVariable(PRC_BLUEPRINT_VERSION, TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        execution.setVariable(SCOPE, scope);
        execution.setVariable(ACTION, action);
        execution.setVariable("mode", "async");

        return execution;
    }
}
