/*
 * ============LICENSE_START======================================================= ONAP - SO
 * ================================================================================ Copyright (C) 2017 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.vcpe;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockDeleteAllottedResource;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockDeleteServiceInstance;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetAllottedResource;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPatchAllottedResource;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockQueryAllottedResourceById;
import static org.onap.so.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.onap.so.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.common.BPMNUtil;
import org.onap.so.bpmn.mock.FileUtil;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

public class DeleteVcpeResCustServiceIT extends AbstractTestBase {

    private static final String PROCNAME = "DeleteVcpeResCustService";
    private static final String Prefix = "DVRCS_";
    private static final String AR_BRG_ID = "ar-brgB";
    private static final String AR_TXC_ID = "ar-txcA";

    private final CallbackSet callbacks = new CallbackSet();
    private final String request;

    public DeleteVcpeResCustServiceIT() throws IOException {
        callbacks.put("deactivate",
                FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeactivateCallback.xml"));
        callbacks.put("delete", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeleteCallback.xml"));
        callbacks.put("unassign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyUnassignCallback.xml"));

        request = FileUtil.readResourceFile("__files/VCPE/DeleteVcpeResCustService/request.json");
    }

    @Before
    public void init() {
        BPMNUtil.cleanHistory(processEngine);
    }

    @Test
    public void testDeleteVcpeResCustService_Success() throws Exception {
        logStart();
        MockNodeQueryServiceInstanceById(wireMockServer, INST, "GenericFlows/getSIUrlById.xml");

        // TODO: use INST instead of DEC_INST
        /*
         * Seems to be a bug as they fail to URL-encode the SI id before performing the query so we'll add a stub for
         * that case, too.
         */
        MockNodeQueryServiceInstanceById(wireMockServer, DEC_INST, "GenericFlows/getSIUrlById.xml");

        /*
         * cannot use MockGetServiceInstance(wireMockServer, ), because we need to return different responses as we
         * traverse through the flow
         */

        // initially, the SI includes the ARs
        wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + CUST
                + "/service-subscriptions/service-subscription/" + SVC + "/service-instances/service-instance/" + INST))
                        .inScenario("SI retrieval").whenScenarioStateIs(Scenario.STARTED)
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                                .withBodyFile("VCPE/DeleteVcpeResCustService/getSI.xml"))
                        .willSetStateTo("ARs Deleted"));

        // once the ARs have been deleted, the SI should be empty
        wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + CUST
                + "/service-subscriptions/service-subscription/" + SVC + "/service-instances/service-instance/" + INST))
                        .inScenario("SI retrieval").whenScenarioStateIs("ARs Deleted")
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                                .withBodyFile("VCPE/DeleteVcpeResCustService/getSIAfterDelArs.xml")));

        // for BRG
        MockQueryAllottedResourceById(wireMockServer, AR_BRG_ID, "VCPE/DeleteVcpeResCustService/getBRGArUrlById.xml");
        MockGetAllottedResource(wireMockServer, CUST, SVC, INST, AR_BRG_ID,
                "VCPE/DeleteVcpeResCustService/arGetBRGById.xml");
        MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, AR_BRG_ID);
        MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, AR_BRG_ID, ARVERS);

        // for TXC
        MockQueryAllottedResourceById(wireMockServer, AR_TXC_ID, "VCPE/DeleteVcpeResCustService/getTXCArUrlById.xml");
        MockGetAllottedResource(wireMockServer, CUST, SVC, INST, AR_TXC_ID,
                "VCPE/DeleteVcpeResCustService/arGetTXCById.xml");
        MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, AR_TXC_ID);
        MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, AR_TXC_ID, ARVERS);

        // MockGetGenericVnfById("vnfX.*", "GenericFlows/getGenericVnfByNameResponse.xml");
        wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                        .withBodyFile("GenericFlows/getGenericVnfByNameResponse.xml")));


        wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/.*"))
                .willReturn(aResponse().withStatus(204).withHeader("Content-Type", "text/xml")));

        MockDeleteServiceInstance(wireMockServer, CUST, SVC, INST, SVC);

        mockSDNCAdapter(wireMockServer, 200);
        mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = setupVariables(businessKey);

        invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);

        // for BRG
        injectSDNCCallbacks(callbacks, "deactivate");
        injectSDNCCallbacks(callbacks, "delete");
        injectSDNCCallbacks(callbacks, "unassign");

        // for VNF1
        injectSDNCCallbacks(callbacks, "deactivate");
        injectSDNCCallbacks(callbacks, "unassign");

        // for VNF2
        injectSDNCCallbacks(callbacks, "deactivate");
        injectSDNCCallbacks(callbacks, "unassign");

        // for TXC
        injectSDNCCallbacks(callbacks, "deactivate");
        injectSDNCCallbacks(callbacks, "delete");
        injectSDNCCallbacks(callbacks, "unassign");

        // for SI
        injectSDNCCallbacks(callbacks, "deactivate");
        injectSDNCCallbacks(callbacks, "delete");

        waitForProcessEnd(businessKey, 70000);
        assertTrue(isProcessEnded(businessKey));

        String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX);
        System.out.println("workflowException:\n" + workflowException);

        String completionReq = BPMNUtil.getVariable(processEngine, PROCNAME, Prefix + VAR_COMP_REQ);
        System.out.println("completionReq:\n" + completionReq);

        assertEquals("true", BPMNUtil.getVariable(processEngine, PROCNAME, PROCNAME + VAR_SUCCESS_IND));
        assertEquals("200", BPMNUtil.getVariable(processEngine, PROCNAME, VAR_RESP_CODE));
        assertNull(workflowException);
        assertTrue(completionReq.contains("<request-id>" + businessKey + "<"));
        assertTrue(completionReq.contains("<action>DELETE<"));
        assertTrue(completionReq.contains("<source>VID<"));

        assertEquals("2", BPMNUtil.getVariable(processEngine, PROCNAME, Prefix + "vnfsDeletedCount"));

        BPMNUtil.assertAnyProcessInstanceFinished(processEngine, "DoDeleteVnfAndModules");
        logEnd();
    }

    @Test
    public void testDeleteVcpeResCustService_NoBRG_NoTXC_NoVNF() throws Exception {
        logStart();
        MockNodeQueryServiceInstanceById(wireMockServer, INST, "GenericFlows/getSIUrlById.xml");

        // TODO: use INST instead of DEC_INST
        /*
         * Seems to be a bug as they fail to URL-encode the SI id before performing the query so we'll add a stub for
         * that case, too.
         */
        MockNodeQueryServiceInstanceById(wireMockServer, DEC_INST, "GenericFlows/getSIUrlById.xml");

        MockGetServiceInstance(wireMockServer, CUST, SVC, INST, "VCPE/DeleteVcpeResCustService/getSIAfterDelArs.xml");

        mockSDNCAdapter(wireMockServer, 200);
        mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = setupVariables(businessKey);


        invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);

        // for SI
        injectSDNCCallbacks(callbacks, "deactivate");
        injectSDNCCallbacks(callbacks, "delete");

        waitForProcessEnd(businessKey, 70000);

        String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX);
        System.out.println("workflowException:\n" + workflowException);

        String completionReq = BPMNUtil.getVariable(processEngine, PROCNAME, Prefix + VAR_COMP_REQ);
        System.out.println("completionReq:\n" + completionReq);

        assertEquals("true", BPMNUtil.getVariable(processEngine, PROCNAME, PROCNAME + VAR_SUCCESS_IND));
        assertEquals("200", BPMNUtil.getVariable(processEngine, PROCNAME, VAR_RESP_CODE));
        assertNull(workflowException);
        assertTrue(completionReq.contains("<request-id>" + businessKey + "<"));
        assertTrue(completionReq.contains("<action>DELETE<"));
        assertTrue(completionReq.contains("<source>VID<"));

        assertEquals("0", BPMNUtil.getVariable(processEngine, PROCNAME, Prefix + "vnfsDeletedCount"));

        BPMNUtil.assertNoProcessInstance(processEngine, "DoDeleteVnfAndModules");
        logEnd();
    }

    @Test
    public void testDeleteVcpeResCustService_Fault() throws Exception {
        logStart();
        MockNodeQueryServiceInstanceById(wireMockServer, INST, "GenericFlows/getSIUrlById.xml");

        // TODO: use INST instead of DEC_INST
        /*
         * Seems to be a bug as they fail to URL-encode the SI id before performing the query so we'll add a stub for
         * that case, too.
         */
        MockNodeQueryServiceInstanceById(wireMockServer, DEC_INST, "GenericFlows/getSIUrlById.xml");

        MockGetServiceInstance(wireMockServer, CUST, SVC, INST, "VCPE/DeleteVcpeResCustService/getSIAfterDelArs.xml");

        // generate failure
        mockSDNCAdapter(wireMockServer, 404);

        mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = setupVariables(businessKey);

        invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);

        waitForProcessEnd(businessKey, 70000);

        String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX);
        System.out.println("workflowException:\n" + workflowException);

        String completionReq = BPMNUtil.getVariable(processEngine, PROCNAME, Prefix + VAR_COMP_REQ);
        System.out.println("completionReq:\n" + completionReq);

        assertEquals(null, BPMNUtil.getVariable(processEngine, PROCNAME, PROCNAME + VAR_SUCCESS_IND));
        assertEquals(null, BPMNUtil.getVariable(processEngine, PROCNAME, VAR_RESP_CODE));
        assertNotNull(workflowException);
        logEnd();
    }

    private Map<String, Object> setupVariables(String requestId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("isDebugLogEnabled", "true");
        variables.put("requestId", requestId);
        variables.put("serviceInstanceId", DEC_INST);
        variables.put("sdncVersion", "1802");
        variables.put("serviceInstanceName", "some-junk-name");
        return variables;
    }

}
