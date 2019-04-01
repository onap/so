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

package org.onap.so.bpmn.vcpe;

import org.junit.Test;
import org.onap.so.bpmn.common.BPMNUtil;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.mock.FileUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.onap.so.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockDeleteAllottedResource;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetAllottedResource;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPatchAllottedResource;
import static org.onap.so.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.onap.so.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;

public class DoCreateAllottedResourceBRGRollbackIT extends AbstractTestBase {

	private static final String PROCNAME = "DoCreateAllottedResourceBRGRollback";
	private static final String RbType = "DCARBRG_";
	private final CallbackSet callbacks = new CallbackSet();

	public DoCreateAllottedResourceBRGRollbackIT() throws IOException {
		callbacks.put("deactivate", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeactivateCallback.xml"));
		callbacks.put("delete", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeleteCallback.xml"));
		callbacks.put("unassign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyUnassignCallback.xml"));
	}
	
	@Test
	public void testDoCreateAllottedResourceBRGRollback_Success() throws Exception {
        logStart();
		MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(wireMockServer, 200);
		mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, businessKey);
		
		String processId = invokeSubProcess(PROCNAME, variables);
		
		injectSDNCCallbacks(callbacks, "deactivate");
		injectSDNCCallbacks(callbacks, "delete");
		injectSDNCCallbacks(callbacks, "unassign");
		
		waitForWorkflowToFinish(processEngine,processId);
		
		assertTrue(isProcessEndedByProcessInstanceId(processId));
		String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX,processId);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("true", BPMNUtil.getVariable(processEngine, PROCNAME, "rolledBack",processId));
		assertNull(BPMNUtil.getVariable(processEngine, PROCNAME, "rollbackError",processId));
		logEnd();
	}
	
	@Test
	public void testDoCreateAllottedResourceBRGRollback_skipRollback() throws Exception {
		logStart();
		MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(wireMockServer, 200);
		mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		RollbackData rollbackData = setVariablesSuccess(variables, businessKey);

		rollbackData.put(RbType, "rollbackAAI", "false");
		rollbackData.put(RbType, "rollbackSDNCassign", "false");
		
		String processId = invokeSubProcess(PROCNAME, variables);

		waitForWorkflowToFinish(processEngine,processId);
		
		assertTrue(isProcessEndedByProcessInstanceId(processId));
		String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX,processId);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals(null, BPMNUtil.getVariable(processEngine, PROCNAME, "rolledBack",processId));
		assertNull(BPMNUtil.getVariable(processEngine, PROCNAME, "rollbackError",processId));
		logEnd();
	}
	
	@Test
	public void testDoCreateAllottedResourceBRGRollback_DoNotRollBack() throws Exception {
		logStart();
	    MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, ARID, ARVERS);
		mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		RollbackData rollbackData = setVariablesSuccess(variables, businessKey);

		// this will cause "rollbackSDNC" to be set to false
		rollbackData.put(RbType, "rollbackSDNCassign", "false");
		
		String processId = invokeSubProcess(PROCNAME, variables);

		waitForWorkflowToFinish(processEngine,processId);
		
		assertTrue(isProcessEndedByProcessInstanceId(processId));
		String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX,processId);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("true", BPMNUtil.getVariable(processEngine, PROCNAME, "rolledBack",processId));
		assertNull(BPMNUtil.getVariable(processEngine, PROCNAME, "rollbackError",processId));
	    logEnd();
	}

	@Test
	public void testDoCreateAllottedResourceBRGRollback_NoDeactivate() throws Exception {
		logStart();
		MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(wireMockServer, 200);
		mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		RollbackData rollbackData = setVariablesSuccess(variables, businessKey);

		rollbackData.put(RbType, "rollbackSDNCactivate", "false");
		
		String processId = invokeSubProcess(PROCNAME, variables);

		injectSDNCCallbacks(callbacks, "delete");
		injectSDNCCallbacks(callbacks, "unassign");

		waitForWorkflowToFinish(processEngine,processId);
		
		assertTrue(isProcessEndedByProcessInstanceId(processId));
		String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX,processId);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("true", BPMNUtil.getVariable(processEngine, PROCNAME, "rolledBack",processId));
		assertNull(BPMNUtil.getVariable(processEngine, PROCNAME, "rollbackError",processId));
		logEnd();
	}

	@Test
	public void testDoCreateAllottedResourceBRGRollback_NoDelete() throws Exception {
		logStart();
		MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(wireMockServer, 200);
		mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		RollbackData rollbackData = setVariablesSuccess(variables, businessKey);

		rollbackData.put(RbType, "rollbackSDNCcreate", "false");
		
		String processId = invokeSubProcess(PROCNAME, variables);

		injectSDNCCallbacks(callbacks, "deactivate");
		injectSDNCCallbacks(callbacks, "unassign");

		waitForWorkflowToFinish(processEngine,processId);
		
		assertTrue(isProcessEndedByProcessInstanceId(processId));
		String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX,processId);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("true", BPMNUtil.getVariable(processEngine, PROCNAME, "rolledBack",processId));
		assertNull(BPMNUtil.getVariable(processEngine, PROCNAME, "rollbackError",processId));
		logEnd();
	}
	
	@Test
	public void testDoCreateAllottedResourceBRGRollback_NoUnassign() throws Exception {
		logStart();
		MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(wireMockServer, 200);
		mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		RollbackData rollbackData = setVariablesSuccess(variables, businessKey);

		rollbackData.put(RbType, "rollbackSDNCassign", "false");
		
		/*
		 * Note: if assign == false then the flow/script will set
		 * "skipRollback" to false, which will cause ALL of the SDNC steps
		 * to be skipped, not just the unassign step.
		 */
		
		String processId = invokeSubProcess(PROCNAME, variables);

		waitForWorkflowToFinish(processEngine,processId);
		
		assertTrue(isProcessEndedByProcessInstanceId(processId));
		String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX,processId);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("true", BPMNUtil.getVariable(processEngine, PROCNAME, "rolledBack",processId));
		assertNull(BPMNUtil.getVariable(processEngine, PROCNAME, "rollbackError",processId));
	    logEnd();
	}
	
	@Test
	public void testDoCreateAllottedResourceBRGRollback_SubProcessError() throws Exception {
		logStart();
		MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, ARID, ARVERS);
		mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");

		mockSDNCAdapter(wireMockServer, 404);
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, businessKey);
		
		String processId = invokeSubProcess(PROCNAME, variables);

		waitForWorkflowToFinish(processEngine,processId);
		
		assertTrue(isProcessEndedByProcessInstanceId(processId));
		String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX,processId);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("false", BPMNUtil.getVariable(processEngine, PROCNAME, "rolledBack",processId));
		assertNotNull(BPMNUtil.getVariable(processEngine, PROCNAME, "rollbackError",processId));
	    logEnd();
	}
	
	@Test
	public void testDoCreateAllottedResourceBRGRollback_JavaException() throws Exception {
		logStart();
		MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(wireMockServer, 200);
		mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, businessKey);

		variables.put("rollbackData", "string instead of rollback data");
		
		String processId = invokeSubProcess(PROCNAME, variables);

		waitForWorkflowToFinish(processEngine,processId);
		
		assertTrue(isProcessEndedByProcessInstanceId(processId));
		String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, VAR_WFEX,processId);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("false", BPMNUtil.getVariable(processEngine, PROCNAME, "rolledBack",processId));
		assertNotNull(BPMNUtil.getVariable(processEngine, PROCNAME, "rollbackError",processId));
	    logEnd();
	}

	private RollbackData setVariablesSuccess(Map<String, Object> variables, String requestId) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("failNotFound", "true");
		variables.put("msoRequestId", requestId);
		variables.put("mso-request-id", "requestId");
		variables.put("allottedResourceId", ARID);

		variables.put("serviceInstanceId", DEC_INST);
		variables.put("parentServiceInstanceId", DEC_PARENT_INST);
		
		RollbackData rollbackData = new RollbackData();

		rollbackData.put(RbType, "serviceInstanceId", DEC_INST);
		rollbackData.put(RbType, "serviceSubscriptionType", SVC);
		rollbackData.put(RbType, "disablerollback", "false");
		rollbackData.put(RbType, "rollbackAAI", "true");
		rollbackData.put(RbType, "rollbackSDNCassign", "true");
		rollbackData.put(RbType, "rollbackSDNCactivate", "true");
		rollbackData.put(RbType, "rollbackSDNCcreate", "true");
		rollbackData.put(RbType, "aaiARPath", "business/customers/customer/"+CUST+"/service-subscriptions/service-subscription/"+SVC+"/service-instances/service-instance/"+INST+"/allotted-resources/allotted-resource/"+ARID);
		
		rollbackData.put(RbType, "sdncActivateRollbackReq", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceBRGRollback/sdncActivateRollbackReq.xml"));
		rollbackData.put(RbType, "sdncCreateRollbackReq", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceBRGRollback/sdncCreateRollbackReq.xml")); 
		rollbackData.put(RbType, "sdncAssignRollbackReq", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceBRGRollback/sdncAssignRollbackReq.xml"));
		
		variables.put("rollbackData",rollbackData);
		
		return rollbackData;
	}

}
