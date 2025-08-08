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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockDeleteAllottedResource;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetAllottedResource;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetCustomer;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithDepth;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithPriority;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetNetworkPolicyfqdn;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetServiceInstance_500;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceByName_404;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPatchAllottedResource;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPatchGenericVnf;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPutAllottedResource;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPutGenericVnf;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPutNetwork;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPutServiceInstance;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPutVfModuleIdNoResponse;
import static org.onap.so.bpmn.mock.StubResponseDatabase.MockGetServiceResourcesCatalogData;
import static org.onap.so.bpmn.mock.StubResponseDatabase.MockPostRequestDB;
import static org.onap.so.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.onap.so.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;
import static org.onap.so.bpmn.mock.StubResponseVNFAdapter.mockVNFPost;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.common.BPMNUtil;
import org.onap.so.bpmn.mock.FileUtil;
import org.onap.so.bpmn.mock.StubResponseOof;


public class CreateVcpeResCustServiceIT extends AbstractTestBase {

    private static final String PROCNAME = "CreateVcpeResCustService";
    private static final String Prefix = "CVRCS_";

    private final CallbackSet callbacks = new CallbackSet();
    private final String request;

    @Before
    public void init() {
        BPMNUtil.cleanHistory(processEngine);
    }

    public CreateVcpeResCustServiceIT() throws IOException {

        callbacks.put("oof", JSON, "oofResponse",
                FileUtil.readResourceFile("__files/VCPE/CreateVcpeResCustService/oofCallbackInfraVnf.json"));
        callbacks.put("assign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyAssignCallback.xml"));
        callbacks.put("create", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyCreateCallback.xml"));
        callbacks.put("activate", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyActivateCallback.xml"));
        callbacks.put("queryTXC",
                FileUtil.readResourceFile("__files/VCPE/CreateVcpeResCustService/SDNCTopologyQueryTXCCallback.xml"));
        callbacks.put("queryBRG",
                FileUtil.readResourceFile("__files/VCPE/CreateVcpeResCustService/SDNCTopologyQueryBRGCallback.xml"));
        callbacks.put("deactivate",
                FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeactivateCallback.xml"));
        callbacks.put("delete", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeleteCallback.xml"));
        callbacks.put("unassign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyUnassignCallback.xml"));

        callbacks.put("query", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyQueryCallback.xml"));
        callbacks.put("queryVnf", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyQueryCallbackVnf.xml"));
        callbacks.put("queryModuleNoVnf",
                FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyQueryCallbackVfModuleNoVnf.xml"));
        callbacks.put("queryModule",
                FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyQueryCallbackVfModule.xml"));
        callbacks.put("vnfCreate", FileUtil.readResourceFile("__files/VfModularity/VNFAdapterRestCreateCallback.xml"));

        request = FileUtil.readResourceFile("__files/VCPE/CreateVcpeResCustService/requestNoSIName.json");
    }

    @Test
    public void testCreateVcpeResCustService_Success() {
        System.out.println("starting:  testCreateVcpeResCustService_Success\n");
        MockGetServiceResourcesCatalogData(wireMockServer, "uuid-miu-svc-011-abcdef", "2",
                "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
        MockGetServiceResourcesCatalogData(wireMockServer, "uuid-miu-svc-011-abcdef",
                "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
        MockGetCustomer(wireMockServer, CUST, "VCPE/CreateVcpeResCustService/getCustomer.xml");
        StubResponseOof.mockOof(wireMockServer);
        // TODO: the SI should NOT have to be URL-encoded yet again!
        MockPutServiceInstance(wireMockServer, CUST, SVC, INST.replace("%", "%25"),
                "GenericFlows/getServiceInstance.xml");
        MockGetServiceInstance(wireMockServer, CUST, SVC, INST.replace("%", "%25"),
                "GenericFlows/getServiceInstance.xml");

        MockNodeQueryServiceInstanceById(wireMockServer, INST, "GenericFlows/getSIUrlById.xml");
        MockNodeQueryServiceInstanceById(wireMockServer, PARENT_INST, "GenericFlows/getParentSIUrlById.xml");
        MockGetServiceInstance(wireMockServer, CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
        MockGetServiceInstance(wireMockServer, CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
        MockPutAllottedResource(wireMockServer, CUST, SVC, PARENT_INST, ARID);
        MockPatchAllottedResource(wireMockServer, CUST, SVC, PARENT_INST, ARID);
        MockGetGenericVnfByIdWithDepth(wireMockServer, ".*", 1, "VCPE/CreateVcpeResCustService/GenericVnf.xml");

        MockPutGenericVnf(wireMockServer, ".*");
        MockPatchGenericVnf(wireMockServer, ".*");

        MockGetGenericVnfByIdWithPriority(wireMockServer, ".*", ".*", 200, "VfModularity/VfModule-new.xml", 5);
        // MockGetGenericVnfByIdWithDepth(wireMockServer, "skask", 1, "VfModularity/GenericVnf.xml");
        MockPutVfModuleIdNoResponse(wireMockServer, ".*", "PCRF", ".*");
        MockPutNetwork(wireMockServer, ".*", "VfModularity/AddNetworkPolicy_AAIResponse_Success.xml", 200);

        MockGetNetworkPolicyfqdn(wireMockServer, ".*",
                "CreateNetworkV2/createNetwork_queryNetworkPolicy_AAIResponse_Success.xml", 200);
        MockNodeQueryServiceInstanceByName_404(wireMockServer, ".*");

        mockVNFPost(wireMockServer, "", 202, ".*");

        wireMockServer.stubFor(post(urlMatching("/services/rest/v1/vnfs" + ".*" + "/vf-modules"))
                .willReturn(aResponse().withStatus(202)));
        wireMockServer
                .stubFor(get(urlMatching(".*/business/owning-entities?.*")).willReturn(aResponse().withStatus(404)));
        wireMockServer.stubFor(put(urlMatching(".*/business/owning-entities/owning-entity/.*"))
                .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(put(urlMatching(
                ".*/business/owning-entities/owning-entity/038d99af-0427-42c2-9d15-971b99b9b489/relationship-list/relationship"))
                        .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(put(urlMatching(".*/query?.*")).willReturn(aResponse().withStatus(200)));
        MockPostRequestDB(wireMockServer);
        mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");
        mockSDNCAdapter(wireMockServer, 200);

        Map<String, Object> variables = setupVariables();

        String businessKey = UUID.randomUUID().toString();
        invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);

        injectWorkflowMessages(callbacks, "oof");
        // for SI
        injectSDNCCallbacks(callbacks, "assign");
        // for TXC
        injectSDNCCallbacks(callbacks, "assign");
        injectSDNCCallbacks(callbacks, "create");
        injectSDNCCallbacks(callbacks, "activate");
        injectSDNCCallbacks(callbacks, "queryTXC");

        // For VNF
        injectSDNCCallbacks(callbacks, "assign");
        injectSDNCCallbacks(callbacks, "assign");
        injectSDNCCallbacks(callbacks, "queryModule");
        injectSDNCCallbacks(callbacks, "activate");
        // VF Module
        injectSDNCCallbacks(callbacks, "queryModule");
        injectSDNCCallbacks(callbacks, "assign");
        injectSDNCCallbacks(callbacks, "queryModule");
        injectSDNCCallbacks(callbacks, "queryModule");
        injectSDNCCallbacks(callbacks, "assign");
        injectSDNCCallbacks(callbacks, "queryModuleNoVnf");
        injectVNFRestCallbacks(callbacks, "vnfCreate");
        injectSDNCCallbacks(callbacks, "activate");

        // for BRG
        injectSDNCCallbacks(callbacks, "assign");
        injectSDNCCallbacks(callbacks, "create");
        injectSDNCCallbacks(callbacks, "activate");
        injectSDNCCallbacks(callbacks, "queryBRG");

        waitForProcessEnd(businessKey, 10000);

        String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX);
        System.out.println("workflowException:\n" + workflowException);

        String completionReq = BPMNUtil.getVariable(processEngine, PROCNAME, Prefix + VAR_COMP_REQ);
        System.out.println("completionReq:\n" + completionReq);

        assertEquals("true", BPMNUtil.getVariable(processEngine, PROCNAME, PROCNAME + VAR_SUCCESS_IND));
        assertEquals("200", BPMNUtil.getVariable(processEngine, PROCNAME, VAR_RESP_CODE));
        assertEquals(null, workflowException);
        assertTrue(completionReq.indexOf("request-id>testRequestId<") >= 0);
        assertTrue(completionReq.indexOf("action>CREATE<") >= 0);
        assertTrue(completionReq.indexOf("source>VID<") >= 0);

        assertEquals("1", BPMNUtil.getVariable(processEngine, PROCNAME, Prefix + "VnfsCreatedCount"));
    }

    @Test
    public void testCreateVcpeResCustService_NoParts() {
        System.out.println("starting: testCreateVcpeResCustService_NoParts\n");
        MockGetServiceResourcesCatalogData(wireMockServer, "uuid-miu-svc-011-abcdef", "2",
                "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesNoData.json");
        MockGetServiceResourcesCatalogData(wireMockServer, "uuid-miu-svc-011-abcdef",
                "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesNoData.json");
        MockGetCustomer(wireMockServer, CUST, "VCPE/CreateVcpeResCustService/getCustomer.xml");

        // TODO: the SI should NOT have to be URL-encoded yet again!
        MockPutServiceInstance(wireMockServer, CUST, SVC, INST.replace("%", "%25"),
                "GenericFlows/getServiceInstance.xml");
        MockGetServiceInstance(wireMockServer, CUST, SVC, INST.replace("%", "%25"),
                "GenericFlows/getServiceInstance.xml");

        MockNodeQueryServiceInstanceById(wireMockServer, INST, "GenericFlows/getSIUrlById.xml");
        MockGetServiceInstance(wireMockServer, CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
        MockNodeQueryServiceInstanceById(wireMockServer, PARENT_INST, "GenericFlows/getParentSIUrlById.xml");
        MockGetServiceInstance(wireMockServer, CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");

        // TODO: should these really be PARENT_INST, or should they be INST?
        MockPutAllottedResource(wireMockServer, CUST, SVC, PARENT_INST, ARID);
        MockPatchAllottedResource(wireMockServer, CUST, SVC, PARENT_INST, ARID);

        mockSDNCAdapter(wireMockServer, 200);
        mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");

        StubResponseOof.mockOof(wireMockServer);
        wireMockServer
                .stubFor(get(urlMatching(".*/business/owning-entities?.*")).willReturn(aResponse().withStatus(404)));
        wireMockServer.stubFor(put(urlMatching(".*/business/owning-entities/owning-entity/.*"))
                .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(put(urlMatching(
                ".*/business/owning-entities/owning-entity/038d99af-0427-42c2-9d15-971b99b9b489/relationship-list/relationship"))
                        .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(put(urlMatching(".*/query?.*")).willReturn(aResponse().withStatus(200)));
        MockPostRequestDB(wireMockServer);
        mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");

        Map<String, Object> variables = setupVariables();

        String businessKey = UUID.randomUUID().toString();
        invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);

        injectWorkflowMessages(callbacks, "oof");
        // for SI
        injectSDNCCallbacks(callbacks, "assign");

        waitForProcessEnd(businessKey, 10000);

        String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX);
        System.out.println("workflowException:\n" + workflowException);

        String completionReq = BPMNUtil.getVariable(processEngine, PROCNAME, Prefix + VAR_COMP_REQ);
        System.out.println("completionReq:\n" + completionReq);

        assertEquals("true", BPMNUtil.getVariable(processEngine, PROCNAME, PROCNAME + VAR_SUCCESS_IND));
        assertEquals("200", BPMNUtil.getVariable(processEngine, PROCNAME, VAR_RESP_CODE));
        assertEquals(null, workflowException);
        assertTrue(completionReq.indexOf("request-id>testRequestId<") >= 0);
        assertTrue(completionReq.indexOf("action>CREATE<") >= 0);
        assertTrue(completionReq.indexOf("source>VID<") >= 0);

        assertEquals("0", BPMNUtil.getVariable(processEngine, PROCNAME, Prefix + "VnfsCreatedCount"));
    }

    @Test
    public void testCreateVcpeResCustService_Fault_NoRollback() {
        System.out.println("starting:  testCreateVcpeResCustService_Fault_NoRollback\n");
        MockGetServiceResourcesCatalogData(wireMockServer, "uuid-miu-svc-011-abcdef", "2",
                "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
        MockGetServiceResourcesCatalogData(wireMockServer, "uuid-miu-svc-011-abcdef",
                "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
        MockGetCustomer(wireMockServer, CUST, "VCPE/CreateVcpeResCustService/getCustomer.xml");

        // TODO: the SI should NOT have to be URL-encoded yet again!
        MockPutServiceInstance(wireMockServer, CUST, SVC, INST.replace("%", "%25"),
                "GenericFlows/getServiceInstance.xml");
        MockGetServiceInstance(wireMockServer, CUST, SVC, INST.replace("%", "%25"),
                "GenericFlows/getServiceInstance.xml");

        MockNodeQueryServiceInstanceById(wireMockServer, INST, "GenericFlows/getSIUrlById.xml");
        MockNodeQueryServiceInstanceById(wireMockServer, PARENT_INST, "GenericFlows/getParentSIUrlById.xml");
        MockGetServiceInstance(wireMockServer, CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
        MockGetServiceInstance_500(wireMockServer, CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
        MockPutAllottedResource(wireMockServer, CUST, SVC, PARENT_INST, ARID);
        MockPatchAllottedResource(wireMockServer, CUST, SVC, PARENT_INST, ARID);

        mockSDNCAdapter(wireMockServer, 404);
        mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");

        StubResponseOof.mockOof(wireMockServer);
        wireMockServer
                .stubFor(get(urlMatching(".*/business/owning-entities?.*")).willReturn(aResponse().withStatus(404)));
        wireMockServer.stubFor(put(urlMatching(".*/business/owning-entities/owning-entity/.*"))
                .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(put(urlMatching(
                ".*/business/owning-entities/owning-entity/038d99af-0427-42c2-9d15-971b99b9b489/relationship-list/relationship"))
                        .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(put(urlMatching(".*/query?.*")).willReturn(aResponse().withStatus(200)));
        MockPostRequestDB(wireMockServer);

        Map<String, Object> variables = setupVariables();

        String businessKey = UUID.randomUUID().toString();
        invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);

        injectWorkflowMessages(callbacks, "oof");

        waitForProcessEnd(businessKey, 100000);

        String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX);
        System.out.println("workflowException:\n" + workflowException);

        String completionReq = BPMNUtil.getVariable(processEngine, PROCNAME, Prefix + VAR_COMP_REQ);
        System.out.println("completionReq:\n" + completionReq);

        assertEquals(null, BPMNUtil.getVariable(processEngine, PROCNAME, PROCNAME + VAR_SUCCESS_IND));
        assertEquals(null, BPMNUtil.getVariable(processEngine, PROCNAME, VAR_RESP_CODE));
        assertNotNull(workflowException);

        BPMNUtil.assertNoProcessInstance(processEngine, "DoCreateAllottedResourceBRGRollback");
        BPMNUtil.assertNoProcessInstance(processEngine, "DoCreateVnfAndModulesRollback");
        BPMNUtil.assertNoProcessInstance(processEngine, "DoCreateAllottedResourceTXCRollback");
    }

    @Test
    public void testCreateVcpeResCustService_Fault_Rollback() {
        System.out.println("starting:  testCreateVcpeResCustService_Fault_Rollback\n");
        MockGetServiceResourcesCatalogData(wireMockServer, "uuid-miu-svc-011-abcdef", "2",
                "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
        MockGetServiceResourcesCatalogData(wireMockServer, "uuid-miu-svc-011-abcdef",
                "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
        MockGetCustomer(wireMockServer, CUST, "VCPE/CreateVcpeResCustService/getCustomer.xml");

        // TODO: the SI should NOT have to be URL-encoded yet again!
        MockPutServiceInstance(wireMockServer, CUST, SVC, INST.replace("%", "%25"),
                "GenericFlows/getServiceInstance.xml");
        MockGetServiceInstance(wireMockServer, CUST, SVC, INST.replace("%", "%25"),
                "GenericFlows/getServiceInstance.xml");

        MockNodeQueryServiceInstanceById(wireMockServer, INST, "GenericFlows/getSIUrlById.xml");
        MockNodeQueryServiceInstanceById(wireMockServer, PARENT_INST, "GenericFlows/getParentSIUrlById.xml");
        MockGetServiceInstance(wireMockServer, CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
        MockGetServiceInstance(wireMockServer, CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
        MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID, "VCPE/CreateVcpeResCustService/arGetById.xml");
        MockGetAllottedResource(wireMockServer, CUST, SVC, PARENT_INST, ARID,
                "VCPE/CreateVcpeResCustService/arGetById.xml");
        MockPutAllottedResource(wireMockServer, CUST, SVC, PARENT_INST, ARID);
        MockPatchAllottedResource(wireMockServer, CUST, SVC, PARENT_INST, ARID);
        MockDeleteAllottedResource(wireMockServer, CUST, SVC, PARENT_INST, ARID, ARVERS);

        mockSDNCAdapter(wireMockServer, 200);
        mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");

        StubResponseOof.mockOof(wireMockServer);
        wireMockServer
                .stubFor(get(urlMatching(".*/business/owning-entities?.*")).willReturn(aResponse().withStatus(404)));
        wireMockServer.stubFor(put(urlMatching(".*/business/owning-entities/owning-entity/.*"))
                .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(put(urlMatching(
                ".*/business/owning-entities/owning-entity/038d99af-0427-42c2-9d15-971b99b9b489/relationship-list/relationship"))
                        .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(put(urlMatching(".*/query?.*")).willReturn(aResponse().withStatus(200)));
        MockPostRequestDB(wireMockServer);
        String req = FileUtil.readResourceFile("__files/VCPE/CreateVcpeResCustService/requestRollback.json");

        Map<String, Object> variables = setupVariables();

        String businessKey = UUID.randomUUID().toString();
        invokeAsyncProcess(PROCNAME, "v1", businessKey, req, variables);

        injectWorkflowMessages(callbacks, "oof");
        // for SI
        injectSDNCCallbacks(callbacks, "assign");

        // for TXC
        injectSDNCCallbacks(callbacks, "assign");
        injectSDNCCallbacks(callbacks, "create");

        waitForProcessEnd(businessKey, 10000);

        String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX);
        System.out.println("workflowException:\n" + workflowException);

        String completionReq = BPMNUtil.getVariable(processEngine, PROCNAME, Prefix + VAR_COMP_REQ);
        System.out.println("completionReq:\n" + completionReq);

        assertEquals(null, BPMNUtil.getVariable(processEngine, PROCNAME, PROCNAME + VAR_SUCCESS_IND));
        assertEquals(null, BPMNUtil.getVariable(processEngine, PROCNAME, VAR_RESP_CODE));
        assertEquals(null, completionReq);
        assertNotNull(workflowException);

        BPMNUtil.assertAnyProcessInstanceFinished(processEngine, "DoCreateServiceInstanceRollback");
    }

    // *****************
    // Utility Section
    // *****************

    // Success Scenario
    private Map<String, Object> setupVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("requestId", "testRequestId");
        variables.put("request-id", "testRequestId");
        variables.put("serviceInstanceId", DEC_INST);
        variables.put("allottedResourceId", ARID);
        variables.put("URN_mso_workflow_aai_distribution_delay", "PT5S");
        return variables;

    }
}
