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


public class DoCreateAllottedResourceTXCTest extends AbstractTestBase {

	private static final String PROCNAME = "DoCreateAllottedResourceTXC";
	private final CallbackSet callbacks = new CallbackSet();

	public DoCreateAllottedResourceTXCTest() throws IOException {
		callbacks.put("assign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyAssignCallback.xml"));
		callbacks.put("create", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyCreateCallback.xml"));
		callbacks.put("activate", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyActivateCallback.xml"));
		callbacks.put("query", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceTXC/SDNCTopologyQueryCallback.xml"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/GenericGetService.bpmn", 
			"subprocess/SDNCAdapterV1.bpmn", 
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceTXC.bpmn",
			"subprocess/DoCreateAllottedResourceTXCRollback.bpmn"})
	public void testDoCreateAllottedResourceTXC_Success() throws Exception{

		// TODO: use INST instead of DEC_INST
		/*
		 * should be INST instead of DEC_INST, but AAI utilities appear to
		 * have a bug in that they don't URL-encode the SI id before using
		 * it in the query
		 */
		MockNodeQueryServiceInstanceById(DEC_INST, "GenericFlows/getSIUrlById.xml");
		MockNodeQueryServiceInstanceById(DEC_PARENT_INST, "GenericFlows/getParentSIUrlById.xml");
		
		MockGetServiceInstance(CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
		MockGetServiceInstance(CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
		MockPutAllottedResource(CUST, SVC, PARENT_INST, ARID);
		MockPatchAllottedResource(CUST, SVC, PARENT_INST, ARID);
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId123");
		
		invokeSubProcess(PROCNAME, businessKey, variables);
		
		injectSDNCCallbacks(callbacks, "assign");
		injectSDNCCallbacks(callbacks, "create");
		injectSDNCCallbacks(callbacks, "activate");
		injectSDNCCallbacks(callbacks, "query");

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		assertEquals(null, workflowException);

		assertEquals("namefromrequest", BPMNUtil.getVariable(processEngineRule, PROCNAME, "allotedResourceName"));
		assertEquals("my-vni", BPMNUtil.getVariable(processEngineRule, PROCNAME, "vni"));
		assertEquals("my-bearer-ip", BPMNUtil.getVariable(processEngineRule, PROCNAME, "vgmuxBearerIP"));
		assertEquals("my-lan-ip", BPMNUtil.getVariable(processEngineRule, PROCNAME, "vgmuxLanIP"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/GenericGetService.bpmn", 
			"subprocess/SDNCAdapterV1.bpmn", 
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceTXC.bpmn",
			"subprocess/DoCreateAllottedResourceTXCRollback.bpmn"})
	public void testDoCreateAllottedResourceTXC_NoSI() throws Exception{

		// TODO: use INST instead of DEC_INST
		/*
		 * should be INST instead of DEC_INST, but AAI utilities appear to
		 * have a bug in that they don't URL-encode the SI id before using
		 * it in the query
		 */
		MockNodeQueryServiceInstanceById(DEC_INST, "GenericFlows/getNotFound.xml");
		MockNodeQueryServiceInstanceById(DEC_PARENT_INST, "GenericFlows/getParentSIUrlById.xml");
		
		MockGetServiceInstance(CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
		MockGetServiceInstance(CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
		MockPutAllottedResource(CUST, SVC, PARENT_INST, ARID);
		MockPatchAllottedResource(CUST, SVC, PARENT_INST, ARID);
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId123");
		
		invokeSubProcess(PROCNAME, businessKey, variables);

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		assertNotNull(workflowException);
		
		assertEquals(null, BPMNUtil.getVariable(processEngineRule, PROCNAME, "allotedResourceName"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/GenericGetService.bpmn", 
			"subprocess/SDNCAdapterV1.bpmn", 
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceTXC.bpmn",
			"subprocess/DoCreateAllottedResourceTXCRollback.bpmn"})
	public void testDoCreateAllottedResourceTXC_ActiveAr() throws Exception{

		// TODO: use INST instead of DEC_INST
		/*
		 * should be INST instead of DEC_INST, but AAI utilities appear to
		 * have a bug in that they don't URL-encode the SI id before using
		 * it in the query
		 */
		MockNodeQueryServiceInstanceById(DEC_INST, "GenericFlows/getSIUrlById.xml");
		MockNodeQueryServiceInstanceById(DEC_PARENT_INST, "GenericFlows/getParentSIUrlById.xml");
		
		MockGetServiceInstance(CUST, SVC, INST, "VCPE/DoCreateAllottedResourceTXC/getSIandAR.xml");
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXC/getArTxc2.xml");
		MockGetServiceInstance(CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
		MockPutAllottedResource(CUST, SVC, PARENT_INST, ARID);
		MockPatchAllottedResource(CUST, SVC, PARENT_INST, ARID);
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId123");

		variables.put("failExists", "false");
		
		invokeSubProcess(PROCNAME, businessKey, variables);
		
		injectSDNCCallbacks(callbacks, "query");

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		assertEquals(null, workflowException);

		assertEquals("namefromrequest", BPMNUtil.getVariable(processEngineRule, PROCNAME, "allotedResourceName"));
		assertEquals("my-vni", BPMNUtil.getVariable(processEngineRule, PROCNAME, "vni"));
		assertEquals("my-bearer-ip", BPMNUtil.getVariable(processEngineRule, PROCNAME, "vgmuxBearerIP"));
		assertEquals("my-lan-ip", BPMNUtil.getVariable(processEngineRule, PROCNAME, "vgmuxLanIP"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/GenericGetService.bpmn", 
			"subprocess/SDNCAdapterV1.bpmn", 
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceTXC.bpmn",
			"subprocess/DoCreateAllottedResourceTXCRollback.bpmn"})
	public void testDoCreateAllottedResourceTXC_NoParentSI() throws Exception{

		// TODO: use INST instead of DEC_INST
		/*
		 * should be INST instead of DEC_INST, but AAI utilities appear to
		 * have a bug in that they don't URL-encode the SI id before using
		 * it in the query
		 */
		MockNodeQueryServiceInstanceById(DEC_INST, "GenericFlows/getSIUrlById.xml");
		
		MockNodeQueryServiceInstanceById(DEC_PARENT_INST, "GenericFlows/getNotFound.xml");
		
		MockGetServiceInstance(CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
		MockGetServiceInstance(CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
		MockPutAllottedResource(CUST, SVC, PARENT_INST, ARID);
		MockPatchAllottedResource(CUST, SVC, PARENT_INST, ARID);
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId123");
		
		invokeSubProcess(PROCNAME, businessKey, variables);

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		assertNotNull(workflowException);
		
		assertEquals(null, BPMNUtil.getVariable(processEngineRule, PROCNAME, "allotedResourceName"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/GenericGetService.bpmn", 
			"subprocess/SDNCAdapterV1.bpmn", 
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceTXC.bpmn",
			"subprocess/DoCreateAllottedResourceTXCRollback.bpmn"})
	public void testDoCreateAllottedResourceTXC_SubProcessError() throws Exception{

		// TODO: use INST instead of DEC_INST
		/*
		 * should be INST instead of DEC_INST, but AAI utilities appear to
		 * have a bug in that they don't URL-encode the SI id before using
		 * it in the query
		 */
		MockNodeQueryServiceInstanceById(DEC_INST, "GenericFlows/getSIUrlById.xml");
		MockNodeQueryServiceInstanceById(DEC_PARENT_INST, "GenericFlows/getParentSIUrlById.xml");
		
		MockGetServiceInstance(CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
		MockGetServiceInstance(CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
		MockPutAllottedResource(CUST, SVC, PARENT_INST, ARID);
		MockPatchAllottedResource(CUST, SVC, PARENT_INST, ARID);
		mockSDNCAdapter(404);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId123");
		
		invokeSubProcess(PROCNAME, businessKey, variables);

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		assertNotNull(workflowException);
		
		assertEquals(null, BPMNUtil.getVariable(processEngineRule, PROCNAME, "allotedResourceName"));
	}

	private void setVariablesSuccess(Map<String, Object> variables, String requestId) {
		// TODO: need all of these?
		variables.put("isDebugLogEnabled", "true");
		variables.put("failExists", "true");
		variables.put("disableRollback", "true");
		variables.put("msoRequestId", requestId);
		variables.put("mso-request-id", "requestId");
		variables.put("sourceNetworkId", "snId");
		variables.put("sourceNetworkRole", "snRole");
		variables.put("allottedResourceRole", "brg");
		variables.put("allottedResourceType", "TXC");
		variables.put("allottedResourceId", ARID);
		variables.put("brgWanMacAddress", "wanmac");

		variables.put("serviceInstanceId", DEC_INST);
		variables.put("parentServiceInstanceId", DEC_PARENT_INST);
		
		variables.put("serviceChainServiceInstanceId", "scsiId");
		
		String arModelInfo = "{ "+ "\"modelType\": \"allotted-resource\"," +
				"\"modelInvariantUuid\": \"ff5256d2-5a33-55df-13ab-12abad84e7ff\"," +
				"\"modelUuid\": \"fe6478e5-ea33-3346-ac12-ab121484a3fe\"," +
				"\"modelName\": \"vSAMP12\"," +
				"\"modelVersion\": \"1.0\"," +
				"\"modelCustomizationUuid\": \"MODEL-ID-1234\"," +
				"}";
		variables.put("allottedResourceModelInfo", arModelInfo);
	}

}
