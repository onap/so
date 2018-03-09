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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockQueryAllottedResourceById;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.mock.FileUtil;


public class DoDeleteAllottedResourceTXCTest extends AbstractTestBase {

	private static final String PROCNAME = "DoDeleteAllottedResourceTXC";
	private final CallbackSet callbacks = new CallbackSet();
	
	public DoDeleteAllottedResourceTXCTest() throws IOException {
		callbacks.put("deactivate", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeactivateCallback.xml"));
		callbacks.put("deactivateNF", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeactivateCallbackNotFound.xml"));
		callbacks.put("delete", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeleteCallback.xml"));
		callbacks.put("unassign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyUnassignCallback.xml"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoDeleteAllottedResourceTXC.bpmn"})
	public void testDoDeleteAllottedResourceTXC_Success() throws Exception {
		
		MockQueryAllottedResourceById(ARID, "GenericFlows/getARUrlById.xml");
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml");
		MockPatchAllottedResource(CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId1");
		
		invokeSubProcess(PROCNAME, businessKey, variables);
		
		injectSDNCCallbacks(callbacks, "deactivate");
		injectSDNCCallbacks(callbacks, "delete");
		injectSDNCCallbacks(callbacks, "unassign");

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoDeleteAllottedResourceTXC.bpmn"})
	public void testDoDeleteAllottedResourceTXC_ARNotInSDNC() throws Exception {
		
		MockQueryAllottedResourceById(ARID, "GenericFlows/getARUrlById.xml");
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml");
		MockPatchAllottedResource(CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId1");

		variables.put("failNotFound", "false");
		
		invokeSubProcess(PROCNAME, businessKey, variables);
		
		injectSDNCCallbacks(callbacks, "deactivateNF");

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
	}
	
	// TODO - exception is not caught
	@Test
	@Ignore
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoDeleteAllottedResourceTXC.bpmn"})
	public void testDoDeleteAllottedResourceTXC_SubProcessError() throws Exception {
		
		MockQueryAllottedResourceById(ARID, "GenericFlows/getARUrlById.xml");
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml");
		MockPatchAllottedResource(CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, ARVERS);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		mockSDNCAdapter(500);
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId1");
		
		invokeSubProcess(PROCNAME, businessKey, variables);

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);
		assertNotNull(workflowException);
	}

	private void setVariablesSuccess(Map<String, Object> variables, String requestId) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("failNotFound", "true");
		variables.put("msoRequestId", requestId);
		variables.put("mso-request-id", "requestId");
		variables.put("allottedResourceId", ARID);
		
		variables.put("serviceInstanceId", DEC_INST);
		variables.put("parentServiceInstanceId", DEC_PARENT_INST);
	}

}
