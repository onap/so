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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.openecomp.mso.bpmn.common.DeleteAAIVfModuleTest.MockAAIDeleteVfModule;
import static org.openecomp.mso.bpmn.common.DeleteAAIVfModuleTest.MockAAIGenericVnfSearch;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockAAIVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDBUpdateVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteVfModuleId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithDepth;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithPriority;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetPserverByVnfId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfsByVnfId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchVfModuleId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutVfModuleIdNoResponse;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockVNFAdapterRestVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockSetInMaintFlagByVnfId;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.MockGetServiceResourcesCatalogData;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponsePolicy.MockPolicyAbort;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;
import static org.openecomp.mso.bpmn.mock.StubResponseVNFAdapter.mockVNFPut;
import static org.openecomp.mso.bpmn.mock.StubResponsePolicy.MockPolicySkip;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;
import org.openecomp.mso.bpmn.mock.FileUtil;

/**
 * Unit test cases for UpdateVnfInfra.bpmn
 */
public class ReplaceVnfInfraTest extends WorkflowTest {

    private final CallbackSet callbacks = new CallbackSet();
    private static final String EOL = "\n";
    private final String vnfAdapterDeleteCallback =
            "<deleteVfModuleResponse>" + EOL +
                    "    <vnfId>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnfId>" + EOL +
                    "    <vfModuleId>973ed047-d251-4fb9-bf1a-65b8949e0a73</vfModuleId>" + EOL +
                    "    <vfModuleDeleted>true</vfModuleDeleted>" + EOL +
                    "    <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
                    "</deleteVfModuleResponse>" + EOL;

    public ReplaceVnfInfraTest() throws IOException {
        callbacks.put("deactivate", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyActivateCallback.xml"));
        callbacks.put("unassign", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyActivateCallback.xml"));
        callbacks.put("vnfDelete", vnfAdapterDeleteCallback);
        callbacks.put("changeassign", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyChangeAssignCallback.xml"));
        callbacks.put("activate", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyActivateCallback.xml"));
        callbacks.put("query", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyQueryCallback.xml"));
        callbacks.put("vnfUpdate", FileUtil.readResourceFile(
                "__files/VfModularity/VNFAdapterRestUpdateCallback.xml"));
    }

    /**
     * Sunny day scenario.
     *
     * @throws Exception
     */
    @Test
    @Ignore
    @Deployment(resources = {
            "process/ReplaceVnfInfra.bpmn",
            "subprocess/DoDeleteVfModule.bpmn",
            "subprocess/DoDeleteVnfAndModules.bpmn",
            "subprocess/DeleteAAIVfModule.bpmn",
            "subprocess/PrepareUpdateAAIVfModule.bpmn",
            "subprocess/ConfirmVolumeGroupTenant.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/DoDeleteVnfAndModules.bpmn",
            "subprocess/GenericDeleteVnf.bpmn",
            "subprocess/DoDeleteVnf.bpmn",
            "subprocess/DoDeleteVfModule.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/UpdateAAIGenericVnf.bpmn",
            "subprocess/UpdateAAIVfModule.bpmn",
            "subprocess/GenericGetService.bpmn",
            "subprocess/GenericGetVnf.bpmn",
            "subprocess/GenericPutVnf.bpmn",
            "subprocess/DoCreateVnf.bpmn",
            "subprocess/GenerateVfModuleName.bpmn",
            "subprocess/DoCreateVfModule.bpmn",
            "subprocess/DoCreateVnfAndModules.bpmn",
            "subprocess/ConfirmVolumeGroupName.bpmn",
            "subprocess/CreateAAIVfModule.bpmn",
            "subprocess/CreateAAIVfModuleVolumeGroup.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/DoCreateVnfAndModulesRollback.bpmn",
            "subprocess/BuildingBlock/DecomposeService.bpmn",
            "subprocess/BuildingBlock/RainyDayHandler.bpmn",
            "subprocess/BuildingBlock/ManualHandling.bpmn"

    })
    public void sunnyDay() throws Exception {

        logStart();
        MockAAIGenericVnfSearch();
        MockGetGenericVnfById("testVnfId123.*", "GenericFlows/getGenericVnfByNameResponse.xml");
        MockDeleteGenericVnf("testVnfId123", "testReVer123");
        mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
        mockSDNCAdapter(200);
        MockDoDeleteVfModule_SDNCSuccess();
        MockDoDeleteVfModule_DeleteVNFSuccess();
        MockAAIDeleteVfModule();

        MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlByIdVipr.xml");
        MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");
        //MockGetGenericVnfById_404("testVnfId");
        MockGetServiceResourcesCatalogData("995256d2-5a33-55df-13ab-12abad84e7ff", "1.0", "VIPR/getCatalogServiceResourcesDataForReplaceVnfInfra.json");
        //MockGetGenericVnfByIdWithDepth("skask", 1, "VfModularity/GenericVnf.xml");
        //MockPutGenericVnf(".*");
        MockAAIVfModule();
        MockPatchGenericVnf("a27ce5a9-29c4-4c22-a017-6615ac73c721");
        MockPatchVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", ".*");
        //mockSDNCAdapter("VfModularity/StandardSDNCSynchResponse.xml");
        //mockVNFPut("skask", "/supercool", 202);
        //mockVNFPut("skask", "/lukewarm", 202);
        //MockVNFAdapterRestVfModule();
        //MockDBUpdateVfModule();
        //MockGetPserverByVnfId("skask", "AAI/AAI_pserverByVnfId.json", 200);
        //MockGetGenericVnfsByVnfId("skask", "AAI/AAI_genericVnfsByVnfId.json", 200);
        MockSetInMaintFlagByVnfId("skask", 200);
        MockPolicySkip();

        //mockSDNCAdapter("VfModularity/StandardSDNCSynchResponse.xml");
        //mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

        String businessKey = UUID.randomUUID().toString();
        String updaetVnfRequest =
                FileUtil.readResourceFile("__files/InfrastructureFlows/ReplaceVnf_VID_request.json");

        Map<String, Object> variables = setupVariablesSunnyDayVID();


        TestAsyncResponse asyncResponse = invokeAsyncProcess("ReplaceVnfInfra",
                "v1", businessKey, updaetVnfRequest, variables);

        WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);

        String responseBody = response.getResponse();
        System.out.println("Workflow (Synch) Response:\n" + responseBody);

        //injectSDNCCallbacks(callbacks, "deactivate");
        //injectSDNCCallbacks(callbacks, "deactivate");
        injectVNFRestCallbacks(callbacks, "vnfDelete");
        //injectSDNCCallbacks(callbacks, "unassign");
        MockGetGenericVnfById("a27ce5a9-29c4-4c22-a017-6615ac73c721", "GenericFlows/getGenericVnfByNameResponse.xml");
        injectSDNCCallbacks(callbacks, "unassign");


        injectSDNCCallbacks(callbacks, "changeassign, query");
        injectVNFRestCallbacks(callbacks, "vnfUpdate");
        injectSDNCCallbacks(callbacks, "activate");
        injectSDNCCallbacks(callbacks, "changeassign, query");
        injectVNFRestCallbacks(callbacks, "vnfUpdate");
        injectSDNCCallbacks(callbacks, "activate");

        // TODO add appropriate assertions

        waitForProcessEnd(businessKey, 10000);
        checkVariable(businessKey, "ReplaceVfModuleInfraSuccessIndicator", true);

        logEnd();
    }

    // Active Scenario
    private Map<String, Object> setupVariablesSunnyDayVID() {
        Map<String, Object> variables = new HashMap<>();
        //try {
        //	variables.put("bpmnRequest", FileUtil.readResourceFile("__files/CreateVfModule_VID_request.json"));
        //}
        //catch (Exception e) {

        //}
        //variables.put("mso-request-id", "testRequestId");
        variables.put("requestId", "testRequestId");
        variables.put("isDebugLogEnabled", "true");
        variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
        variables.put("vnfId", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
        variables.put("vnfType", "vSAMP12");
        variables.put("serviceType", "MOG");

        return variables;

    }

    public static void MockDoDeleteVfModule_SDNCSuccess() {
        stubFor(post(urlEqualTo("/SDNCAdapter"))
                .withRequestBody(containing("SvcAction>deactivate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBodyFile("DeleteGenericVNFV1/sdncAdapterResponse.xml")));
        stubFor(post(urlEqualTo("/SDNCAdapter"))
                .withRequestBody(containing("SvcAction>unassign"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBodyFile("DeleteGenericVNFV1/sdncAdapterResponse.xml")));
    }


    public static void MockDoDeleteVfModule_DeleteVNFSuccess() {
        stubFor(delete(urlMatching("/vnfs/v1/vnfs/.*/vf-modules/.*"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/xml")));
        stubFor(delete(urlMatching("/vnfs/v1/volume-groups/78987"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/xml")));
    }

}
