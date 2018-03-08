/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockAAIVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDBUpdateVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfById_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchVfModuleId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockSDNCAdapterVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockVNFAdapterRestVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.core.domain.ModelInfo;
import org.openecomp.mso.bpmn.core.domain.ModuleResource;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.bpmn.core.domain.VnfResource;
import org.openecomp.mso.bpmn.mock.FileUtil;

/**
 * Unit Test for the DoCreateVnfAndModules Flow
 */
public class DoCreateVnfAndModulesTest extends WorkflowTest {

    private final CallbackSet callbacks = new CallbackSet();

    public DoCreateVnfAndModulesTest() throws IOException {

        callbacks.put("assign", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyAssignCallback.xml"));
        callbacks.put("activate", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyActivateCallback.xml"));
        callbacks.put("query", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyQueryCallback.xml"));
        callbacks.put("queryVnf", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyQueryCallbackVnf.xml"));
        callbacks.put("queryModuleNoVnf", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyQueryCallbackVfModuleNoVnf.xml"));
        callbacks.put("queryModule", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyQueryCallbackVfModule.xml"));
        callbacks.put("vnfCreate", FileUtil.readResourceFile(
                "__files/VfModularity/VNFAdapterRestCreateCallback.xml"));
    }

    @Test
    @Ignore // IGNORED FOR 1710 MERGE TO ONAP
    @Deployment(resources = {"subprocess/GenericGetService.bpmn",
            "subprocess/GenericGetVnf.bpmn",
            "subprocess/GenericPutVnf.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/DoCreateVnf.bpmn",
            "subprocess/GenerateVfModuleName.bpmn",
            "subprocess/DoCreateVfModule.bpmn",
            "subprocess/DoCreateVnfAndModules.bpmn",
            "subprocess/GenericGetVnf.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/ConfirmVolumeGroupTenant.bpmn",
            "subprocess/ConfirmVolumeGroupName.bpmn",
            "subprocess/CreateAAIVfModule.bpmn",
            "subprocess/UpdateAAIVfModule.bpmn",
            "subprocess/CreateAAIVfModuleVolumeGroup.bpmn",
            "subprocess/UpdateAAIGenericVnf.bpmn",
            "subprocess/DoCreateVnfAndModulesRollback.bpmn"})
    public void testDoCreateVnfAndModulesBaseOnly_success() throws Exception {

        MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlByIdVipr.xml");
        MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");
        MockGetGenericVnfById_404("testVnfId");
        MockPutGenericVnf(".*");
        MockAAIVfModule();
        MockPatchGenericVnf("skask");
        MockPatchVfModuleId("skask", ".*");
        mockSDNCAdapter("VfModularity/StandardSDNCSynchResponse.xml");
        MockVNFAdapterRestVfModule();
        MockDBUpdateVfModule();

        mockSDNCAdapter("VfModularity/StandardSDNCSynchResponse.xml");
        mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariablesSuccess(variables, "", "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");
        invokeSubProcess("DoCreateVnfAndModules", businessKey, variables);

        injectSDNCCallbacks(callbacks, "assign");
        injectSDNCCallbacks(callbacks, "query");
        injectSDNCCallbacks(callbacks, "activate");
        injectSDNCCallbacks(callbacks, "queryVnf");
        injectSDNCCallbacks(callbacks, "assign, queryModuleNoVnf");
        injectVNFRestCallbacks(callbacks, "vnfCreate");
        injectSDNCCallbacks(callbacks, "activate");
        waitForProcessEnd(businessKey, 10000);

        Assert.assertTrue(isProcessEnded(businessKey));
        assertVariables("true", "true", "false", "true", "Success", null);

    }

    @Test
    @Ignore // IGNORED FOR 1710 MERGE TO ONAP
    @Deployment(resources = {"subprocess/GenericGetService.bpmn",
            "subprocess/GenericGetVnf.bpmn",
            "subprocess/GenericPutVnf.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/DoCreateVnf.bpmn",
            "subprocess/GenerateVfModuleName.bpmn",
            "subprocess/DoCreateVfModule.bpmn",
            "subprocess/DoCreateVnfAndModules.bpmn",
            "subprocess/GenericGetVnf.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/ConfirmVolumeGroupTenant.bpmn",
            "subprocess/ConfirmVolumeGroupName.bpmn",
            "subprocess/CreateAAIVfModule.bpmn",
            "subprocess/UpdateAAIVfModule.bpmn",
            "subprocess/CreateAAIVfModuleVolumeGroup.bpmn",
            "subprocess/UpdateAAIGenericVnf.bpmn",
            "subprocess/DoCreateVnfAndModulesRollback.bpmn"})
    public void testDoCreateVnfAndModulesWithAddon_success() throws Exception {

        MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlByIdVipr.xml");
        MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");
        MockGetGenericVnfById_404("testVnfId");
        MockPutGenericVnf(".*");
        MockAAIVfModule();
        MockPatchGenericVnf("skask");
        MockPatchVfModuleId("skask", ".*");
        mockSDNCAdapter("VfModularity/StandardSDNCSynchResponse.xml");
        MockVNFAdapterRestVfModule();
        MockDBUpdateVfModule();
        mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
        mockSDNCAdapter("VfModularity/StandardSDNCSynchResponse.xml");

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariablesAddonSuccess(variables, "", "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");
        invokeSubProcess("DoCreateVnfAndModules", businessKey, variables);

        injectSDNCCallbacks(callbacks, "assign");
        injectSDNCCallbacks(callbacks, "query");
        injectSDNCCallbacks(callbacks, "activate");
        injectSDNCCallbacks(callbacks, "queryVnf");
        injectSDNCCallbacks(callbacks, "assign, queryModuleNoVnf");
        injectVNFRestCallbacks(callbacks, "vnfCreate");
        injectSDNCCallbacks(callbacks, "activate");
        injectSDNCCallbacks(callbacks, "queryVnf");
        injectSDNCCallbacks(callbacks, "assign, queryModuleNoVnf");
        injectVNFRestCallbacks(callbacks, "vnfCreate");
        injectSDNCCallbacks(callbacks, "activate");
        waitForProcessEnd(businessKey, 10000);

        Assert.assertTrue(isProcessEnded(businessKey));
        assertVariables("true", "true", "false", "true", "Success", null);

    }

    private void assertVariables(String exSIFound, String exSISucc, String exVnfFound, String exVnfSucc, String exResponse, String exWorkflowException) {

        String siFound = BPMNUtil.getVariable(processEngineRule, "DoCreateVnf", "GENGS_FoundIndicator");
        String siSucc = BPMNUtil.getVariable(processEngineRule, "DoCreateVnf", "GENGS_SuccessIndicator");
        String vnfFound = BPMNUtil.getVariable(processEngineRule, "DoCreateVnf", "GENGV_FoundIndicator");
        String vnfSucc = BPMNUtil.getVariable(processEngineRule, "DoCreateVnf", "GENGV_SuccessIndicator");
        String response = BPMNUtil.getVariable(processEngineRule, "DoCreateVnf", "WorkflowResponse");
        String workflowException = BPMNUtil.getVariable(processEngineRule, "DoCreateVnf", "SavedWorkflowException1");

        //assertEquals(exSIFound, siFound);
        //assertEquals(exSISucc, siSucc);
        //assertEquals(exVnfFound, vnfFound);
        //assertEquals(exVnfSucc, vnfSucc);
        //assertEquals(exResponse, response);
        assertEquals(exWorkflowException, workflowException);
    }

    private void setVariables(Map<String, String> variables, String request, String requestId, String siId) {
        variables.put("isDebugLogEnabled", "true");
        variables.put("bpmnRequest", request);
        variables.put("mso-request-id", requestId);
        variables.put("serviceInstanceId", siId);
        variables.put("testVnfId", "testVnfId123");
        variables.put("vnfType", "STMTN");
    }

    private void setVariablesSuccess(Map<String, Object> variables, String request, String requestId, String siId) {
        variables.put("isDebugLogEnabled", "true");
        variables.put("mso-request-id", requestId);
        variables.put("requestId", requestId);
        variables.put("msoRequestId", requestId);
        variables.put("serviceInstanceId", siId);
        variables.put("disableRollback", "true");
        //variables.put("testVnfId","testVnfId123");
        variables.put("vnfType", "STMTN");

        String serviceModelInfo = "{ " + "\"modelType\": \"service\"," +
                "\"modelInvariantUuid\": \"995256d2-5a33-55df-13ab-12abad84e7ff\"," +
                "\"modelUuid\": \"ab6478e5-ea33-3346-ac12-ab121484a3fe\"," +
                "\"modelName\": \"ServicevSAMP12\"," +
                "\"modelVersion\": \"1.0\"," +
                "}";
        variables.put("serviceModelInfo", serviceModelInfo);
        variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
        String vnfModelInfo = "{" + "\"modelType\": \"vnf\"," +
                "\"modelInvariantUuid\": \"ff5256d2-5a33-55df-13ab-12abad84e7ff\"," +
                "\"modelUuid\": \"fe6478e5-ea33-3346-ac12-ab121484a3fe\"," +
                "\"modelName\": \"vSAMP12\"," +
                "\"modelVersion\": \"1.0\"," +
                "\"modelCustomizationUuid\": \"MODEL-ID-1234\"" + "}";
        variables.put("vnfModelInfo", vnfModelInfo);

        String cloudConfiguration = "{" + "\"cloudConfiguration\": { " +
                "\"lcpCloudRegionId\": \"mdt1\"," +
                "\"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\"" + "}}";
        variables.put("cloudConfiguration", cloudConfiguration);
        variables.put("sdncVersion", "1707");
        variables.put("globalSubscriberId", "subscriber123");

        try {
            String serviceDecomposition = FileUtil.readResourceFile("__files/VIPR/serviceDecompositionATMFW.json");
            ServiceDecomposition sd = new ServiceDecomposition();
            ModelInfo serviceModel = new ModelInfo();
            serviceModel.setModelName("servicewithVNFs");
            sd.setModelInfo(serviceModel);
            VnfResource vr = new VnfResource();
            ModelInfo mvr = new ModelInfo();
            mvr.setModelName("vSAMP12");
            mvr.setModelInstanceName("v123");
            mvr.setModelInvariantUuid("");
            mvr.setModelVersion("1.0");
            mvr.setModelCustomizationUuid("MODEL-ID-1234");
            vr.setModelInfo(mvr);
            vr.constructVnfType("vnf1");
            vr.setNfType("somenftype");
            vr.setNfRole("somenfrole");
            vr.setNfFunction("somenffunction");
            vr.setNfNamingCode("somenamingcode");
            ModuleResource mr = new ModuleResource();
            ModelInfo mvmr = new ModelInfo();
            mvmr.setModelInvariantUuid("ff5256d2-5a33-55df-13ab-12abad84e7ff");
            mvmr.setModelName("STMTN5MMSC21-MMSC::model-1-0");
            mvmr.setModelVersion("1");
            mvmr.setModelCustomizationUuid("MODEL-123");
            mr.setModelInfo(mvmr);
            mr.setIsBase(true);
            mr.setVfModuleLabel("MODULELABEL");
            vr.addVfModule(mr);
            sd.addVnfResource(vr);

            variables.put("serviceDecomposition", sd);
            variables.put("isTest", true);
        } catch (Exception e) {

        }

    }

    private void setVariablesAddonSuccess(Map<String, Object> variables, String request, String requestId, String siId) {
        variables.put("isDebugLogEnabled", "true");
        variables.put("mso-request-id", requestId);
        variables.put("requestId", requestId);
        variables.put("msoRequestId", requestId);
        variables.put("serviceInstanceId", siId);
        variables.put("disableRollback", "true");
        //variables.put("testVnfId","testVnfId123");
        variables.put("vnfType", "STMTN");

        String serviceModelInfo = "{ " + "\"modelType\": \"service\"," +
                "\"modelInvariantUuid\": \"995256d2-5a33-55df-13ab-12abad84e7ff\"," +
                "\"modelUuid\": \"ab6478e5-ea33-3346-ac12-ab121484a3fe\"," +
                "\"modelName\": \"ServicevSAMP12\"," +
                "\"modelVersion\": \"1.0\"," +
                "}";
        variables.put("serviceModelInfo", serviceModelInfo);
        variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
        String vnfModelInfo = "{" + "\"modelType\": \"vnf\"," +
                "\"modelInvariantUuid\": \"ff5256d2-5a33-55df-13ab-12abad84e7ff\"," +
                "\"modelUuid\": \"fe6478e5-ea33-3346-ac12-ab121484a3fe\"," +
                "\"modelName\": \"vSAMP12\"," +
                "\"modelVersion\": \"1.0\"," +
                "\"modelCustomizationUuid\": \"MODEL-ID-1234\"" + "}";
        variables.put("vnfModelInfo", vnfModelInfo);

        String cloudConfiguration = "{" + "\"cloudConfiguration\": { " +
                "\"lcpCloudRegionId\": \"mdt1\"," +
                "\"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\"" + "}}";
        variables.put("cloudConfiguration", cloudConfiguration);
        variables.put("sdncVersion", "1707");
        variables.put("globalSubscriberId", "subscriber123");

        try {
            String serviceDecomposition = FileUtil.readResourceFile("__files/VIPR/serviceDecompositionATMFW.json");
            ServiceDecomposition sd = new ServiceDecomposition();
            ModelInfo serviceModel = new ModelInfo();
            serviceModel.setModelName("servicewithVNFs");
            sd.setModelInfo(serviceModel);
            VnfResource vr = new VnfResource();
            ModelInfo mvr = new ModelInfo();
            mvr.setModelName("vSAMP12");
            mvr.setModelInstanceName("v123");
            mvr.setModelInvariantUuid("");
            mvr.setModelVersion("1.0");
            mvr.setModelCustomizationUuid("MODEL-ID-1234");
            vr.setModelInfo(mvr);
            vr.setNfType("somenftype");
            vr.setNfRole("somenfrole");
            vr.setNfFunction("somenffunction");
            vr.setNfNamingCode("somenamingcode");
            ModuleResource mr = new ModuleResource();
            ModelInfo mvmr = new ModelInfo();
            mvmr.setModelInvariantUuid("ff5256d2-5a33-55df-13ab-12abad84e7ff");
            mvmr.setModelName("STMTN5MMSC21-MMSC::model-1-0");
            mvmr.setModelVersion("1");
            mvmr.setModelCustomizationUuid("MODEL-123");
            mr.setModelInfo(mvmr);
            mr.setIsBase(true);
            mr.setVfModuleLabel("MODULELABEL");
            vr.addVfModule(mr);
            ModuleResource mr1 = new ModuleResource();
            ModelInfo mvmr1 = new ModelInfo();
            mvmr1.setModelInvariantUuid("ff5256d2-5a33-55df-13ab-12abad84e7ff");
            mvmr1.setModelName("STMTN5MMSC21-MMSC::model-1-0");
            mvmr1.setModelVersion("1");
            mvmr1.setModelCustomizationUuid("MODEL-123");
            mr1.setModelInfo(mvmr1);
            mr1.setIsBase(false);
            mr1.setVfModuleLabel("MODULELABEL");
            mr1.setInitialCount(1);
            vr.addVfModule(mr1);

            sd.addVnfResource(vr);

            variables.put("serviceDecomposition", sd);
            variables.put("isTest", true);
        } catch (Exception e) {


        }
    }
}
