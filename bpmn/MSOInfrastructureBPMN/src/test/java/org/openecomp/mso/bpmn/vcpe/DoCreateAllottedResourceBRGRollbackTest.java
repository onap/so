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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class DoCreateAllottedResourceBRGRollbackTest extends AbstractTestBase {

	private static final String PROCNAME = "DoCreateAllottedResourceBRGRollback";
	private static final String RbType = "DCARBRG_";
	private final CallbackSet callbacks = new CallbackSet();
	
	public DoCreateAllottedResourceBRGRollbackTest() throws IOException {
		callbacks.put("deactivate", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeactivateCallback.xml"));
		callbacks.put("delete", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeleteCallback.xml"));
		callbacks.put("unassign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyUnassignCallback.xml"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRGRollback_Success() throws Exception {
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
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
		
		assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("true", BPMNUtil.getVariable(processEngineRule, PROCNAME, "rolledBack"));
		assertNull(BPMNUtil.getVariable(processEngineRule, PROCNAME, "rollbackError"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRGRollback_skipRollback() throws Exception {
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		RollbackData rollbackData = setVariablesSuccess(variables, "testRequestId1");

		rollbackData.put(RbType, "rollbackAAI", "false");
		rollbackData.put(RbType, "rollbackSDNCassign", "false");
		
		invokeSubProcess(PROCNAME, businessKey, variables);

		waitForProcessEnd(businessKey, 10000);
		
		assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals(null, BPMNUtil.getVariable(processEngineRule, PROCNAME, "rolledBack"));
		assertNull(BPMNUtil.getVariable(processEngineRule, PROCNAME, "rollbackError"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRGRollback_DoNotRollBack() throws Exception {
		
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		RollbackData rollbackData = setVariablesSuccess(variables, "testRequestId1");

		// this will cause "rollbackSDNC" to be set to false
		rollbackData.put(RbType, "rollbackSDNCassign", "false");
		
		invokeSubProcess(PROCNAME, businessKey, variables);

		waitForProcessEnd(businessKey, 10000);
		
		assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("true", BPMNUtil.getVariable(processEngineRule, PROCNAME, "rolledBack"));
		assertNull(BPMNUtil.getVariable(processEngineRule, PROCNAME, "rollbackError"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRGRollback_NoDeactivate() throws Exception {
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		RollbackData rollbackData = setVariablesSuccess(variables, "testRequestId1");

		rollbackData.put(RbType, "rollbackSDNCactivate", "false");
		
		invokeSubProcess(PROCNAME, businessKey, variables);

		injectSDNCCallbacks(callbacks, "delete");
		injectSDNCCallbacks(callbacks, "unassign");

		waitForProcessEnd(businessKey, 10000);
		
		assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("true", BPMNUtil.getVariable(processEngineRule, PROCNAME, "rolledBack"));
		assertNull(BPMNUtil.getVariable(processEngineRule, PROCNAME, "rollbackError"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRGRollback_NoDelete() throws Exception {
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		RollbackData rollbackData = setVariablesSuccess(variables, "testRequestId1");

		rollbackData.put(RbType, "rollbackSDNCcreate", "false");
		
		invokeSubProcess(PROCNAME, businessKey, variables);

		injectSDNCCallbacks(callbacks, "deactivate");
		injectSDNCCallbacks(callbacks, "unassign");

		waitForProcessEnd(businessKey, 10000);
		
		assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("true", BPMNUtil.getVariable(processEngineRule, PROCNAME, "rolledBack"));
		assertNull(BPMNUtil.getVariable(processEngineRule, PROCNAME, "rollbackError"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRGRollback_NoUnassign() throws Exception {
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		RollbackData rollbackData = setVariablesSuccess(variables, "testRequestId1");

		rollbackData.put(RbType, "rollbackSDNCassign", "false");
		
		/*
		 * Note: if assign == false then the flow/script will set
		 * "skipRollback" to false, which will cause ALL of the SDNC steps
		 * to be skipped, not just the unassign step.
		 */
		
		invokeSubProcess(PROCNAME, businessKey, variables);

		waitForProcessEnd(businessKey, 10000);
		
		assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("true", BPMNUtil.getVariable(processEngineRule, PROCNAME, "rolledBack"));
		assertNull(BPMNUtil.getVariable(processEngineRule, PROCNAME, "rollbackError"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRGRollback_SubProcessError() throws Exception {
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, ARVERS);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		mockSDNCAdapter(404);
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId1");
		
		invokeSubProcess(PROCNAME, businessKey, variables);

		waitForProcessEnd(businessKey, 10000);
		
		assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("false", BPMNUtil.getVariable(processEngineRule, PROCNAME, "rolledBack"));
		assertNotNull(BPMNUtil.getVariable(processEngineRule, PROCNAME, "rollbackError"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRGRollback_JavaException() throws Exception {
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRGRollback/arGetById.xml");
		MockPatchAllottedResource(CUST, SVC, INST, ARID);
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, ARVERS);
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId1");

		variables.put("rollbackData", "string instead of rollback data");
		
		invokeSubProcess(PROCNAME, businessKey, variables);

		waitForProcessEnd(businessKey, 10000);
		
		assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		assertEquals("false", BPMNUtil.getVariable(processEngineRule, PROCNAME, "rolledBack"));
		assertNotNull(BPMNUtil.getVariable(processEngineRule, PROCNAME, "rollbackError"));
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
		rollbackData.put(RbType, "aaiARPath", "http://localhost:28090/aai/v9/business/customers/customer/"+CUST+"/service-subscriptions/service-subscription/"+SVC+"/service-instances/service-instance/"+INST+"/allotted-resources/allotted-resource/"+ARID);
		
		rollbackData.put(RbType, "sdncActivateRollbackReq", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceBRGRollback/sdncActivateRollbackReq.xml"));
		rollbackData.put(RbType, "sdncCreateRollbackReq", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceBRGRollback/sdncCreateRollbackReq.xml")); 
		rollbackData.put(RbType, "sdncAssignRollbackReq", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceBRGRollback/sdncAssignRollbackReq.xml"));
		
		variables.put("rollbackData",rollbackData);
		
		return rollbackData;
	}

}
