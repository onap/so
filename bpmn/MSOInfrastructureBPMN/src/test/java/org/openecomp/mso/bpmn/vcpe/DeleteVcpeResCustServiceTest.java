/*
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
package org.openecomp.mso.bpmn.vcpe;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockQueryAllottedResourceById;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.mock.FileUtil;

import com.github.tomakehurst.wiremock.stubbing.Scenario;

public class DeleteVcpeResCustServiceTest extends AbstractTestBase {

	private static final String PROCNAME = "DeleteVcpeResCustService";
	private static final String Prefix = "DVRCS_";
	private static final String AR_BRG_ID = "ar-brgB";
	private static final String AR_TXC_ID = "ar-txcA";
	
	private final CallbackSet callbacks = new CallbackSet();
	private final String request;
	
	public DeleteVcpeResCustServiceTest() throws IOException {
		callbacks.put("deactivate", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeactivateCallback.xml"));
		callbacks.put("delete", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeleteCallback.xml"));
		callbacks.put("unassign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyUnassignCallback.xml"));
		
		request = FileUtil.readResourceFile("__files/VCPE/DeleteVcpeResCustService/request.json");
	}
	
	@Test
	@Deployment(resources = {
			"process/DeleteVcpeResCustService.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/GenericDeleteService.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
            "subprocess/DoDeleteServiceInstance.bpmn",
			"subprocess/DoDeleteAllottedResourceBRG.bpmn",
			"subprocess/DoDeleteAllottedResourceTXC.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",

            // stubs
            "VCPE/stubprocess/DoDeleteVnfAndModules.bpmn"})
	
	public void testDeleteVcpeResCustService_Success() throws Exception {

		MockNodeQueryServiceInstanceById(INST, "GenericFlows/getSIUrlById.xml");

		// TODO: use INST instead of DEC_INST
		/*
		 * Seems to be a bug in GenericDeleteService (or its subflows) as they
		 * fail to URL-encode the SI id before performing the query so we'll
		 * add a stub for that case, too.
		 */
		MockNodeQueryServiceInstanceById(DEC_INST, "GenericFlows/getSIUrlById.xml");
		
		/*
		 * cannot use MockGetServiceInstance(), because we need to return
		 * different responses as we traverse through the flow
		 */ 

		// initially, the SI includes the ARs
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + CUST + "/service-subscriptions/service-subscription/" + SVC + "/service-instances/service-instance/" + INST))
				.inScenario("SI retrieval")
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("VCPE/DeleteVcpeResCustService/getSI.xml"))
				.willSetStateTo("ARs Deleted"));
		
		// once the ARs have been deleted, the SI should be empty
		stubFor(get(urlMatching("/aai/v[0-9]+/business/customers/customer/" + CUST + "/service-subscriptions/service-subscription/" + SVC + "/service-instances/service-instance/" + INST))
				.inScenario("SI retrieval")
				.whenScenarioStateIs("ARs Deleted")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("VCPE/DeleteVcpeResCustService/getSIAfterDelArs.xml")));

		// for BRG
		MockQueryAllottedResourceById(AR_BRG_ID, "VCPE/DeleteVcpeResCustService/getBRGArUrlById.xml");
		MockGetAllottedResource(CUST, SVC, INST, AR_BRG_ID, "VCPE/DeleteVcpeResCustService/arGetBRGById.xml");
		MockPatchAllottedResource(CUST, SVC, INST, AR_BRG_ID);
		MockDeleteAllottedResource(CUST, SVC, INST, AR_BRG_ID, ARVERS);

		// for TXC
		MockQueryAllottedResourceById(AR_TXC_ID, "VCPE/DeleteVcpeResCustService/getTXCArUrlById.xml");
		MockGetAllottedResource(CUST, SVC, INST, AR_TXC_ID, "VCPE/DeleteVcpeResCustService/arGetTXCById.xml");
		MockPatchAllottedResource(CUST, SVC, INST, AR_TXC_ID);
		MockDeleteAllottedResource(CUST, SVC, INST, AR_TXC_ID, ARVERS);
		
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		Map<String, Object> variables = setupVariables();

		String businessKey = UUID.randomUUID().toString();
		invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);
		
		// for BRG
		injectSDNCCallbacks(callbacks, "deactivate");
		injectSDNCCallbacks(callbacks, "delete");
		injectSDNCCallbacks(callbacks, "unassign");

		// for TXC
		injectSDNCCallbacks(callbacks, "deactivate");
		injectSDNCCallbacks(callbacks, "delete");
		injectSDNCCallbacks(callbacks, "unassign");
		
		// for SI
		injectSDNCCallbacks(callbacks, "deactivate");
		injectSDNCCallbacks(callbacks, "delete");
		
		waitForProcessEnd(businessKey, 10000);

		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);

		String completionReq = BPMNUtil.getVariable(processEngineRule, PROCNAME, Prefix+VAR_COMP_REQ);
		System.out.println("completionReq:\n" + completionReq);
		
		assertEquals("true", BPMNUtil.getVariable(processEngineRule, PROCNAME, PROCNAME+VAR_SUCCESS_IND));
		assertEquals("200", BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_RESP_CODE));
		assertEquals(null, workflowException);
		assertTrue(completionReq.contains("<request-id>testRequestId<"));
		assertTrue(completionReq.contains("<action>DELETE<"));
		assertTrue(completionReq.contains("<source>VID<"));

		assertEquals("2", BPMNUtil.getVariable(processEngineRule, PROCNAME, Prefix+"vnfsDeletedCount"));

		BPMNUtil.assertAnyProcessInstanceFinished(processEngineRule, "DoDeleteVnfAndModules");
	}
	
	@Test
	@Deployment(resources = {
			"process/DeleteVcpeResCustService.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/GenericDeleteService.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
            "subprocess/DoDeleteServiceInstance.bpmn",
			"subprocess/DoDeleteAllottedResourceBRG.bpmn",
			"subprocess/DoDeleteAllottedResourceTXC.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",

            // stubs
            "VCPE/stubprocess/DoDeleteVnfAndModules.bpmn"})
	
	public void testDeleteVcpeResCustService_NoBRG_NoTXC_NoVNF() throws Exception {

		MockNodeQueryServiceInstanceById(INST, "GenericFlows/getSIUrlById.xml");

		// TODO: use INST instead of DEC_INST
		/*
		 * Seems to be a bug in GenericDeleteService (or its subflows) as they
		 * fail to URL-encode the SI id before performing the query so we'll
		 * add a stub for that case, too.
		 */
		MockNodeQueryServiceInstanceById(DEC_INST, "GenericFlows/getSIUrlById.xml");
		
		MockGetServiceInstance(CUST, SVC, INST, "VCPE/DeleteVcpeResCustService/getSIAfterDelArs.xml");
		
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		Map<String, Object> variables = setupVariables();

		String businessKey = UUID.randomUUID().toString();
		invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);
		
		// for SI
		injectSDNCCallbacks(callbacks, "deactivate");
		injectSDNCCallbacks(callbacks, "delete");
		
		waitForProcessEnd(businessKey, 10000);

		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);

		String completionReq = BPMNUtil.getVariable(processEngineRule, PROCNAME, Prefix+VAR_COMP_REQ);
		System.out.println("completionReq:\n" + completionReq);
		
		assertEquals("true", BPMNUtil.getVariable(processEngineRule, PROCNAME, PROCNAME+VAR_SUCCESS_IND));
		assertEquals("200", BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_RESP_CODE));
		assertEquals(null, workflowException);
		assertTrue(completionReq.contains("<request-id>testRequestId<"));
		assertTrue(completionReq.contains("<action>DELETE<"));
		assertTrue(completionReq.contains("<source>VID<"));

		assertEquals("0", BPMNUtil.getVariable(processEngineRule, PROCNAME, Prefix+"vnfsDeletedCount"));
		
		BPMNUtil.assertNoProcessInstance(processEngineRule, "DoDeleteVnfAndModules");
	}
	
	@Test
	@Deployment(resources = {
			"process/DeleteVcpeResCustService.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/GenericDeleteService.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
            "subprocess/DoDeleteServiceInstance.bpmn",
			"subprocess/DoDeleteAllottedResourceBRG.bpmn",
			"subprocess/DoDeleteAllottedResourceTXC.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",

            // stubs
            "VCPE/stubprocess/DoDeleteVnfAndModules.bpmn"})
	
	public void testDeleteVcpeResCustService_Fault() throws Exception {

		MockNodeQueryServiceInstanceById(INST, "GenericFlows/getSIUrlById.xml");

		// TODO: use INST instead of DEC_INST
		/*
		 * Seems to be a bug in GenericDeleteService (or its subflows) as they
		 * fail to URL-encode the SI id before performing the query so we'll
		 * add a stub for that case, too.
		 */
		MockNodeQueryServiceInstanceById(DEC_INST, "GenericFlows/getSIUrlById.xml");
		
		MockGetServiceInstance(CUST, SVC, INST, "VCPE/DeleteVcpeResCustService/getSIAfterDelArs.xml");
		
		// generate failure
		mockSDNCAdapter(404);
		
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		Map<String, Object> variables = setupVariables();

		String businessKey = UUID.randomUUID().toString();
		invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);
		
		waitForProcessEnd(businessKey, 10000);

		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);

		String completionReq = BPMNUtil.getVariable(processEngineRule, PROCNAME, Prefix+VAR_COMP_REQ);
		System.out.println("completionReq:\n" + completionReq);
		
		assertEquals(null, BPMNUtil.getVariable(processEngineRule, PROCNAME, PROCNAME+VAR_SUCCESS_IND));
		assertEquals(null, BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_RESP_CODE));
		assertNotNull(workflowException);
	}
	
	private Map<String, Object> setupVariables() throws UnsupportedEncodingException {
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled", "true");
		variables.put("requestId", "testRequestId");
		variables.put("serviceInstanceId", DEC_INST);
		variables.put("sdncVersion", "1802");
		variables.put("serviceInstanceName", "some-junk-name");
		return variables;
	}
	
}
