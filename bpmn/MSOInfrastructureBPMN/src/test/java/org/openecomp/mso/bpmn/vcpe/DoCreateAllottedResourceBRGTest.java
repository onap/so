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
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.mock.FileUtil;


public class DoCreateAllottedResourceBRGTest extends AbstractTestBase {

	private static final String PROCNAME = "DoCreateAllottedResourceBRG";
	private final CallbackSet callbacks = new CallbackSet();

	public DoCreateAllottedResourceBRGTest() throws IOException {
		callbacks.put("assign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyAssignCallback.xml"));
		callbacks.put("create", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyCreateCallback.xml"));
		callbacks.put("activate", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyActivateCallback.xml"));
		callbacks.put("query", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceBRG/SDNCTopologyQueryCallback.xml"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/GenericGetService.bpmn", 
			"subprocess/SDNCAdapterV1.bpmn", 
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceBRG.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRG_Success() throws Exception{

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
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/GenericGetService.bpmn", 
			"subprocess/SDNCAdapterV1.bpmn", 
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceBRG.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRG_NoSI() throws Exception{

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
			"subprocess/DoCreateAllottedResourceBRG.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRG_ActiveAr() throws Exception{

		// TODO: use INST instead of DEC_INST
		/*
		 * should be INST instead of DEC_INST, but AAI utilities appear to
		 * have a bug in that they don't URL-encode the SI id before using
		 * it in the query
		 */
		MockNodeQueryServiceInstanceById(DEC_INST, "GenericFlows/getSIUrlById.xml");
		MockNodeQueryServiceInstanceById(DEC_PARENT_INST, "GenericFlows/getParentSIUrlById.xml");
		
		MockGetServiceInstance(CUST, SVC, INST, "VCPE/DoCreateAllottedResourceBRG/getSIandAR.xml");
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceBRG/getArBrg2.xml");
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
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/GenericGetService.bpmn", 
			"subprocess/SDNCAdapterV1.bpmn", 
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceBRG.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRG_NoParentSI() throws Exception{

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
			"subprocess/DoCreateAllottedResourceBRG.bpmn",
			"subprocess/DoCreateAllottedResourceBRGRollback.bpmn"})
	public void testDoCreateAllottedResourceBRG_SubProcessError() throws Exception{

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
		variables.put("isDebugLogEnabled", "true");
		variables.put("failExists", "true");
		variables.put("disableRollback", "true");
		variables.put("msoRequestId", requestId);
		variables.put("mso-request-id", "requestId");
		variables.put("sourceNetworkId", "snId");
		variables.put("sourceNetworkRole", "snRole");
		variables.put("allottedResourceRole", "txc");
		variables.put("allottedResourceType", "BRG");
		variables.put("allottedResourceId", ARID);
		variables.put("vni", "BRG");
		variables.put("vgmuxBearerIP", "bearerip");
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
