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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockAAIVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithDepth;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithPriority;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetVfModuleIdNoResponse;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetVolumeGroupById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchVfModuleId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutVfModuleIdNoResponse;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;
import static org.openecomp.mso.bpmn.mock.StubResponseVNFAdapter.mockVNFPut;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.mock.FileUtil;

/**
 * Unit tests for DoUpdateVfModule.bpmn.
 */
public class DoUpdateVfModuleTest extends WorkflowTest {

    private final CallbackSet callbacks = new CallbackSet();

    public DoUpdateVfModuleTest() throws IOException {
        callbacks.put("changeassign", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyChangeAssignCallback.xml"));
        callbacks.put("query", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyQueryCallback.xml"));
        callbacks.put("activate", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyActivateCallback.xml"));
        callbacks.put("vnfUpdate", FileUtil.readResourceFile(
                "__files/VfModularity/VNFAdapterRestUpdateCallback.xml"));
    }

    /**
     * Test the happy path through the flow.
     */
    @Test
    @Ignore
    @Deployment(resources = {
            "subprocess/DoUpdateVfModule.bpmn",
            "subprocess/PrepareUpdateAAIVfModule.bpmn",
            "subprocess/ConfirmVolumeGroupTenant.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/UpdateAAIGenericVnf.bpmn",
            "subprocess/UpdateAAIVfModule.bpmn"
    })
    public void happyPath() throws IOException {

        logStart();

        String doUpdateVfModuleRequest = FileUtil.readResourceFile("__files/VfModularity/DoUpdateVfModuleRequest.xml");
        MockGetGenericVnfByIdWithDepth("skask", 1, "VfModularity/GenericVnf.xml");
        MockGetVfModuleIdNoResponse("skask", "PCRF", "supercool");
        MockPutVfModuleIdNoResponse("skask", "PCRF", "supercool");
        MockGetVolumeGroupById("MDTWNJ21", "78987", "VfModularity/VolumeGroup.xml");
        mockSDNCAdapter("/SDNCAdapter", "SvcAction>query", 200, "VfModularity/StandardSDNCSynchResponse.xml");
        mockSDNCAdapter("/SDNCAdapter", "SvcInstanceId><", 200, "VfModularity/StandardSDNCSynchResponse.xml");
        mockVNFPut("skask", "/supercool", 202);
        MockPutGenericVnf("skask");
        MockGetGenericVnfByIdWithPriority("skask", "supercool", 200, "VfModularity/VfModule-supercool.xml", 1);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", "DEV-VF-0011");
        variables.put("isDebugLogEnabled", "true");
        variables.put("DoUpdateVfModuleRequest", doUpdateVfModuleRequest);
        invokeSubProcess("DoUpdateVfModule", businessKey, variables);

        injectSDNCCallbacks(callbacks, "changeassign, query");
        injectVNFRestCallbacks(callbacks, "vnfUpdate");
        injectSDNCCallbacks(callbacks, "activate");

        waitForProcessEnd(businessKey, 10000);

        Assert.assertTrue(isProcessEnded(businessKey));
        checkVariable(businessKey, "DoUpdateVfModuleSuccessIndicator", true);

        String heatStackId = (String) getVariableFromHistory(businessKey, "DOUPVfMod_heatStackId");
        System.out.println("Heat stack Id from AAI: " + heatStackId);

        logEnd();
    }

    /**
     * Test the happy path through the flow with Building Blocks interface.
     */
    @Test

    @Deployment(resources = {
            "subprocess/DoUpdateVfModule.bpmn",
            "subprocess/PrepareUpdateAAIVfModule.bpmn",
            "subprocess/ConfirmVolumeGroupTenant.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/UpdateAAIGenericVnf.bpmn",
            "subprocess/UpdateAAIVfModule.bpmn"
    })
    public void happyPathBB() throws IOException {

        logStart();

        MockAAIVfModule();
        MockPatchGenericVnf("skask");
        MockPatchVfModuleId("skask", ".*");
        MockSDNCAdapterVfModule();
        MockVNFAdapterRestVfModule();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", "DEV-VF-0011");
        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "DEV-VF-0011");
        variables.put("isBaseVfModule", "false");
        variables.put("isDebugLogEnabled", "true");
        variables.put("disableRollback", "true");
        variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
        variables.put("vnfId", "skask");
        variables.put("vfModuleId", "supercool");
        variables.put("vnfType", "pcrf-capacity");
        variables.put("isVidRequest", "true");
        variables.put("volumeGroupId", "78987");
        variables.put("usePreload", true);

        variables.put("sdncVersion", "1702");

        variables.put("lcpCloudRegionId", "MDTWNJ21");
        variables.put("tenantId", "fba1bd1e195a404cacb9ce17a9b2b421");

        String vfModuleModelInfo = "{ " + "\"modelType\": \"vfModule\"," +
                "\"modelInvariantUuid\": \"introvert\"," +
                "\"modelUuid\": \"3.14\"," +
                "\"modelName\": \"STMTN5MMSC21-MMSC::model-1-0\"," +
                "\"modelVersion\": \"1\"," +
                "\"modelCustomizationUuid\": \"MODEL-123\"" + "}";
        variables.put("vfModuleModelInfo", vfModuleModelInfo);

        String vnfModelInfo = "{ " + "\"modelType\": \"vnf\"," +
                "\"modelInvariantUuid\": \"introvert\"," +
                "\"modelUuid\": \"3.14\"," +
                "\"modelName\": \"VNF-STMTN5MMSC21-MMSC::model-1-0\"," +
                "\"modelVersion\": \"1\"," +
                "\"modelCustomizationUuid\": \"VNF-MODEL-123\"" + "}";
        variables.put("vnfModelInfo", vnfModelInfo);

        invokeSubProcess("DoUpdateVfModule", businessKey, variables);

        injectSDNCCallbacks(callbacks, "changeassign, query");
        injectVNFRestCallbacks(callbacks, "vnfUpdate");
        injectSDNCCallbacks(callbacks, "activate");

        waitForProcessEnd(businessKey, 10000);

        Assert.assertTrue(isProcessEnded(businessKey));
        checkVariable(businessKey, "DoUpdateVfModuleSuccessIndicator", true);

        String heatStackId = (String) getVariableFromHistory(businessKey, "DOUPVfMod_heatStackId");
        System.out.println("Heat stack Id from AAI: " + heatStackId);

        logEnd();
    }

    // start of mocks used locally and by other VF Module unit tests
    public static void MockSDNCAdapterVfModule() {
        // simplified the implementation to return "success" for all requests
        stubFor(post(urlEqualTo("/SDNCAdapter"))
//			.withRequestBody(containing("SvcInstanceId><"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBodyFile("VfModularity/StandardSDNCSynchResponse.xml")));
//		stubFor(post(urlEqualTo("/SDNCAdapter"))
//				.withRequestBody(containing("vnf-type>STMTN"))
//				.willReturn(aResponse()
//					.withStatus(200)
//					.withHeader("Content-Type", "text/xml")
//					.withBodyFile("VfModularity/StandardSDNCSynchResponse.xml")));
//		stubFor(post(urlEqualTo("/SDNCAdapter"))
//				.withRequestBody(containing("SvcAction>query"))
//				.willReturn(aResponse()
//					.withStatus(200)
//					.withHeader("Content-Type", "text/xml")
//					.withBodyFile("VfModularity/StandardSDNCSynchResponse.xml")));
    }

    public static void MockVNFAdapterRestVfModule() {
        stubFor(put(urlEqualTo("/vnfs/v1/vnfs/skask/vf-modules/supercool"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/xml")));
        stubFor(post(urlMatching("/vnfs/v1/vnfs/.*/vf-modules"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/xml")));
        stubFor(post(urlEqualTo("/vnfs/v1/vnfs/skask/vf-modules"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/xml")));
        stubFor(put(urlEqualTo("/vnfs/v1/volume-groups/78987"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/xml")));
    }
}

