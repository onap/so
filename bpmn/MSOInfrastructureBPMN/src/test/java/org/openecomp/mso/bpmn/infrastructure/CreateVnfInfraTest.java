/*
 * © 2014 AT&T Intellectual Property. All rights reserved. Used under license from AT&T Intellectual Property.
 */
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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.openecomp.mso.bpmn.common.BPMNUtil.executeWorkFlow;
import static org.openecomp.mso.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByName_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;
import org.openecomp.mso.bpmn.mock.FileUtil;

/**
 * Unit Test for the CreateVnfInfra Flow
 */
public class CreateVnfInfraTest extends WorkflowTest {

    private String createVnfInfraRequest;
    private final CallbackSet callbacks = new CallbackSet();


    public CreateVnfInfraTest() throws IOException {
        createVnfInfraRequest = FileUtil.readResourceFile("__files/InfrastructureFlows/CreateVnfInfraRequest.json");
        callbacks.put("assign", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyAssignCallback.xml"));
        callbacks.put("activate", FileUtil.readResourceFile(
                "__files/VfModularity/SDNCTopologyActivateCallback.xml"));
    }

    @Test
    @Deployment(resources = {"subprocess/DoCreateVnf.bpmn",
            "subprocess/GenericGetService.bpmn",
            "subprocess/GenericGetVnf.bpmn",
            "subprocess/GenericPutVnf.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "process/CreateVnfInfra.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn"})
    public void testCreateVnfInfra_success() throws Exception {

        MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlById.xml");
        MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");
        MockGetGenericVnfByName_404();
        MockPutGenericVnf();
        mockSDNCAdapter("/SDNCAdapter", "vnf-type>STMTN", 200, "VfModularity/StandardSDNCSynchResponse.xml");
        mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariablesSuccess(variables, createVnfInfraRequest, "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");
        TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVnfInfra",
                "v1", businessKey, createVnfInfraRequest, variables);

        WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);

        String responseBody = response.getResponse();
        System.out.println("Workflow (Synch) Response:\n" + responseBody);

        //injectSDNCCallbacks(callbacks, "assign, query");
        //injectSDNCCallbacks(callbacks, "activate");

        // TODO add appropriate assertions

        waitForProcessEnd(businessKey, 10000);
        String status = BPMNUtil.getVariable(processEngineRule, "CreateVnfInfra", "CreateVnfInfraStatus");
        assertEquals("Success", status);

        logEnd();

        //WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "CreateVnfInfra", variables);
        //	injectSDNCCallbacks(callbacks, "assign");
        //	injectSDNCCallbacks(callbacks, "activate");
        //waitForProcessEnd(businessKey, 10000);
        //waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

        //assertVariables("true", "true", "false", "true", "Success", null);

    }

    @Test
    @Deployment(resources = {"subprocess/GenericGetService.bpmn", "subprocess/GenericGetVnf.bpmn", "subprocess/GenericPutVnf.bpmn", "process/CreateVnfInfra.bpmn", "subprocess/FalloutHandler.bpmn", "subprocess/CompleteMsoProcess.bpmn"})
    public void testCreateVnfInfra_error_badRequest() throws Exception {

        mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

        Map<String, String> variables = new HashMap<>();
        setVariables(variables, null, "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");

        WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "CreateVnfInfra", variables);
        waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

        assertVariables(null, null, null, null, null, "WorkflowException[processKey=CreateVnfInfra,errorCode=2500,errorMessage=Internal Error - WorkflowException Object and/or RequestInfo is null! CreateVnfInfra]");

    }

    @Test
    @Ignore
    @Deployment(resources = {"subprocess/DoCreateVnf.bpmn", "subprocess/GenericGetService.bpmn", "subprocess/GenericGetVnf.bpmn", "subprocess/GenericPutVnf.bpmn", "process/CreateVnfInfra.bpmn", "subprocess/FalloutHandler.bpmn", "subprocess/CompleteMsoProcess.bpmn"})
    public void testCreateVnfInfra_error_siNotFound() throws Exception {

        MockNodeQueryServiceInstanceById_404("MIS%2F1604%2F0026%2FSW_INTERNET");
        mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

        Map<String, String> variables = new HashMap<>();
        setVariables(variables, createVnfInfraRequest, "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");

        WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "CreateVnfInfra", variables);
        waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

        assertVariables(null, null, null, null, null, "WorkflowException[processKey=DoCreateVnf,errorCode=404,errorMessage=Service Instance Not Found]");

    }

    @Test
    @Ignore
    @Deployment(resources = {"subprocess/DoCreateVnf.bpmn",
            "subprocess/GenericGetService.bpmn",
            "subprocess/GenericGetVnf.bpmn",
            "subprocess/GenericPutVnf.bpmn",
            "process/CreateVnfInfra.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn"})
    public void testCreateVnfInfra_error_vnfExist() throws Exception {
        MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlById.xml");
        MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");

        stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf[?]vnf-name=testVnfName123&depth=1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBodyFile("GenericFlows/getGenericVnfResponse.xml")));

        MockPutGenericVnf();
        mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

        Map<String, String> variables = new HashMap<>();
        setVariables(variables, createVnfInfraRequest, "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");

        WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "CreateVnfInfra", variables);
        waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

        assertVariables(null, null, null, null, null, "WorkflowException[processKey=DoCreateVnf,errorCode=5000,errorMessage=Generic Vnf Already Exist.]");

    }

    private void assertVariables(String exSIFound, String exSISucc, String exVnfFound, String exVnfSucc, String exResponse, String exWorkflowException) {

        String siFound = BPMNUtil.getVariable(processEngineRule, "CreateVnfInfra", "GENGS_FoundIndicator");
        String siSucc = BPMNUtil.getVariable(processEngineRule, "CreateVnfInfra", "GENGS_SuccessIndicator");
        String vnfFound = BPMNUtil.getVariable(processEngineRule, "CreateVnfInfra", "GENGV_FoundIndicator");
        String vnfSucc = BPMNUtil.getVariable(processEngineRule, "CreateVnfInfra", "GENGV_SuccessIndicator");
        String response = BPMNUtil.getVariable(processEngineRule, "CreateVnfInfra", "WorkflowResponse");
        String workflowException = BPMNUtil.getVariable(processEngineRule, "CreateVnfInfra", "SavedWorkflowException1");

        assertEquals(exSIFound, siFound);
        assertEquals(exSISucc, siSucc);
        assertEquals(exVnfFound, vnfFound);
        assertEquals(exVnfSucc, vnfSucc);
        assertEquals(exResponse, response);
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
        //variables.put("bpmnRequest", request);
        //variables.put("mso-request-id", requestId);
        variables.put("serviceInstanceId", siId);
        variables.put("requestId", requestId);
        variables.put("testVnfId", "testVnfId123");
        variables.put("vnfType", "STMTN");
    }

}
