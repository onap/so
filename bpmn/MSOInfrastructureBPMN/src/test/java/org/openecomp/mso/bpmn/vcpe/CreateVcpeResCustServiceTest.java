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
import static org.junit.Assert.assertTrue;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetCustomer;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.MockGetServiceResourcesCatalogData;
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
import org.openecomp.mso.bpmn.mock.FileUtil;

public class CreateVcpeResCustServiceTest extends AbstractTestBase {

	private static final String PROCNAME = "CreateVcpeResCustService";
	private static final String Prefix = "CVRCS_";
	
	private final CallbackSet callbacks = new CallbackSet();
	private final String request;
	
	public CreateVcpeResCustServiceTest() throws IOException {
		callbacks.put("assign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyAssignCallback.xml"));
		callbacks.put("create", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyCreateCallback.xml"));
		callbacks.put("activate", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyActivateCallback.xml"));
		callbacks.put("queryTXC", FileUtil.readResourceFile("__files/VCPE/CreateVcpeResCustService/SDNCTopologyQueryTXCCallback.xml"));
		callbacks.put("queryBRG", FileUtil.readResourceFile("__files/VCPE/CreateVcpeResCustService/SDNCTopologyQueryBRGCallback.xml"));
		callbacks.put("deactivate", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeactivateCallback.xml"));
		callbacks.put("delete", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeleteCallback.xml"));
		callbacks.put("unassign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyUnassignCallback.xml"));
		
		request = FileUtil.readResourceFile("__files/VCPE/CreateVcpeResCustService/requestNoSIName.json");
	}
	
	@Test
	@Ignore // 1802 merge
	@Deployment(resources = {
			"process/CreateVcpeResCustService.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/GenericPutService.bpmn",
			"subprocess/BuildingBlock/DecomposeService.bpmn",
            "subprocess/DoCreateServiceInstance.bpmn",
			"subprocess/DoCreateAllottedResourceTXC.bpmn",
			"subprocess/DoCreateAllottedResourceBRG.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",

            // stubs
			"VCPE/stubprocess/Homing.bpmn",
            "VCPE/stubprocess/DoCreateVnfAndModules.bpmn"})
	
	public void testCreateVcpeResCustService_Success() throws Exception {

		System.out.println("starting:  testCreateVcpeResCustService_Success\n");
		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "2", "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
		MockGetCustomer(CUST, "VCPE/CreateVcpeResCustService/getCustomer.xml");
		
		// TODO: the SI should NOT have to be URL-encoded yet again!
		MockPutServiceInstance(CUST, SVC, INST.replace("%", "%25"), "GenericFlows/getServiceInstance.xml");
		MockGetServiceInstance(CUST, SVC, INST.replace("%", "%25"), "GenericFlows/getServiceInstance.xml");
		
		MockNodeQueryServiceInstanceById(INST, "GenericFlows/getSIUrlById.xml");
		MockNodeQueryServiceInstanceById(PARENT_INST, "GenericFlows/getParentSIUrlById.xml");		
		MockGetServiceInstance(CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
		MockGetServiceInstance(CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
		MockPutAllottedResource(CUST, SVC, PARENT_INST, ARID);
		MockPatchAllottedResource(CUST, SVC, PARENT_INST, ARID);

		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		Map<String, Object> variables = setupVariables();

		String businessKey = UUID.randomUUID().toString();
		invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);
		
		// for SI
		injectSDNCCallbacks(callbacks, "assign");
		
		// for TXC
		injectSDNCCallbacks(callbacks, "assign");
		injectSDNCCallbacks(callbacks, "create");
		injectSDNCCallbacks(callbacks, "activate");
		injectSDNCCallbacks(callbacks, "queryTXC");
		
		// for BRG
		injectSDNCCallbacks(callbacks, "assign");
		injectSDNCCallbacks(callbacks, "create");
		injectSDNCCallbacks(callbacks, "activate");
		injectSDNCCallbacks(callbacks, "queryBRG");
		
		waitForProcessEnd(businessKey, 10000);

		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);

		String completionReq = BPMNUtil.getVariable(processEngineRule, PROCNAME, Prefix+VAR_COMP_REQ);
		System.out.println("completionReq:\n" + completionReq);
		
		assertEquals("true", BPMNUtil.getVariable(processEngineRule, PROCNAME, PROCNAME+VAR_SUCCESS_IND));
		assertEquals("200", BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_RESP_CODE));
		assertEquals(null, workflowException);
		assertTrue(completionReq.contains("request-id>testRequestId<"));
		assertTrue(completionReq.contains("action>CREATE<"));
		assertTrue(completionReq.contains("source>VID<"));

		assertEquals("1", BPMNUtil.getVariable(processEngineRule, PROCNAME, Prefix+"VnfsCreatedCount"));
	}
	
	@Test
	@Ignore // 1802 merge
	@Deployment(resources = {
			"process/CreateVcpeResCustService.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/GenericPutService.bpmn",
			"subprocess/BuildingBlock/DecomposeService.bpmn",
            "subprocess/DoCreateServiceInstance.bpmn",
			"subprocess/DoCreateAllottedResourceTXC.bpmn",
			"subprocess/DoCreateAllottedResourceBRG.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",

            // stubs
			"VCPE/stubprocess/Homing.bpmn",
            "VCPE/stubprocess/DoCreateVnfAndModules.bpmn"})
	
	public void testCreateVcpeResCustService_NoParts() throws Exception {

		System.out.println("starting: testCreateVcpeResCustService_NoParts\n"  );
		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "2", "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesNoData.json");
		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesNoData.json");
		MockGetCustomer(CUST, "VCPE/CreateVcpeResCustService/getCustomer.xml");
		
		// TODO: the SI should NOT have to be URL-encoded yet again!
		MockPutServiceInstance(CUST, SVC, INST.replace("%", "%25"), "GenericFlows/getServiceInstance.xml");
		MockGetServiceInstance(CUST, SVC, INST.replace("%", "%25"), "GenericFlows/getServiceInstance.xml");
		
		MockNodeQueryServiceInstanceById(INST, "GenericFlows/getSIUrlById.xml");		
		MockGetServiceInstance(CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
		MockNodeQueryServiceInstanceById(PARENT_INST, "GenericFlows/getParentSIUrlById.xml");
		MockGetServiceInstance(CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
		
		// TODO: should these really be PARENT_INST, or should they be INST?
		MockPutAllottedResource(CUST, SVC, PARENT_INST, ARID);
		MockPatchAllottedResource(CUST, SVC, PARENT_INST, ARID);

		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		Map<String, Object> variables = setupVariables();

		String businessKey = UUID.randomUUID().toString();
		invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);
		
		// for SI
		injectSDNCCallbacks(callbacks, "assign");
		
		waitForProcessEnd(businessKey, 10000);

		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);

		String completionReq = BPMNUtil.getVariable(processEngineRule, PROCNAME, Prefix+VAR_COMP_REQ);
		System.out.println("completionReq:\n" + completionReq);
		
		assertEquals("true", BPMNUtil.getVariable(processEngineRule, PROCNAME, PROCNAME+VAR_SUCCESS_IND));
		assertEquals("200", BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_RESP_CODE));
		assertEquals(null, workflowException);
		assertTrue(completionReq.contains("request-id>testRequestId<"));
		assertTrue(completionReq.contains("action>CREATE<"));
		assertTrue(completionReq.contains("source>VID<"));

		assertEquals("0", BPMNUtil.getVariable(processEngineRule, PROCNAME, Prefix+"VnfsCreatedCount"));
	}
	
	@Test
	@Ignore // 1802 merge
	@Deployment(resources = {
			"process/CreateVcpeResCustService.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/GenericPutService.bpmn",
			"subprocess/BuildingBlock/DecomposeService.bpmn",
            "subprocess/DoCreateServiceInstance.bpmn",
			"subprocess/DoCreateAllottedResourceBRG.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",

            // this stub will trigger a fault
			"VCPE/stubprocess/DoCreateAllottedResourceTXC.bpmn",

            // stubs
			"VCPE/stubprocess/Homing.bpmn",
            "VCPE/stubprocess/DoCreateVnfAndModules.bpmn"})
	
	public void testCreateVcpeResCustService_Fault_NoRollback() throws Exception {

		System.out.println("starting:  testCreateVcpeResCustService_Fault_NoRollback\n");
		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "2", "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
		MockGetCustomer(CUST, "VCPE/CreateVcpeResCustService/getCustomer.xml");
		
		// TODO: the SI should NOT have to be URL-encoded yet again!
		MockPutServiceInstance(CUST, SVC, INST.replace("%", "%25"), "GenericFlows/getServiceInstance.xml");
		MockGetServiceInstance(CUST, SVC, INST.replace("%", "%25"), "GenericFlows/getServiceInstance.xml");
		
		MockNodeQueryServiceInstanceById(INST, "GenericFlows/getSIUrlById.xml");
		MockNodeQueryServiceInstanceById(PARENT_INST, "GenericFlows/getParentSIUrlById.xml");		
		MockGetServiceInstance(CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
		MockGetServiceInstance(CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
		MockPutAllottedResource(CUST, SVC, PARENT_INST, ARID);
		MockPatchAllottedResource(CUST, SVC, PARENT_INST, ARID);

		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		Map<String, Object> variables = setupVariables();

		String req = FileUtil.readResourceFile("__files/VCPE/CreateVcpeResCustService/requestNoSINameNoRollback.json");
		
		String businessKey = UUID.randomUUID().toString();
		invokeAsyncProcess(PROCNAME, "v1", businessKey, req, variables);
		
		// for SI
		injectSDNCCallbacks(callbacks, "assign");
		
		waitForProcessEnd(businessKey, 10000);

		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);

		String completionReq = BPMNUtil.getVariable(processEngineRule, PROCNAME, Prefix+VAR_COMP_REQ);
		System.out.println("completionReq:\n" + completionReq);
		
		assertEquals(null, BPMNUtil.getVariable(processEngineRule, PROCNAME, PROCNAME+VAR_SUCCESS_IND));
		assertEquals(null, BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_RESP_CODE));
		assertNotNull(workflowException);
		
		BPMNUtil.assertNoProcessInstance(processEngineRule, "DoCreateAllottedResourceBRGRollback");
		BPMNUtil.assertNoProcessInstance(processEngineRule, "DoCreateVnfAndModulesRollback");
		BPMNUtil.assertNoProcessInstance(processEngineRule, "DoCreateAllottedResourceTXCRollback");
	}
	
	@Test
	@Ignore // 1802 merge
	@Deployment(resources = {
			"process/CreateVcpeResCustService.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/GenericPutService.bpmn",
			"subprocess/BuildingBlock/DecomposeService.bpmn",
            "subprocess/DoCreateServiceInstance.bpmn",
			"subprocess/DoCreateAllottedResourceTXC.bpmn",
			"subprocess/DoCreateAllottedResourceTXCRollback.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",

            // this stub will trigger a fault
			"VCPE/stubprocess/DoCreateAllottedResourceBRG.bpmn",

            // stubs
			"VCPE/stubprocess/DoCreateAllottedResourceBRGRollback.bpmn",
			"VCPE/stubprocess/DoCreateVnfAndModulesRollback.bpmn",
            "VCPE/stubprocess/DoCreateServiceInstanceRollback.bpmn",
			"VCPE/stubprocess/Homing.bpmn",
            "VCPE/stubprocess/DoCreateVnfAndModules.bpmn"})
	
	public void testCreateVcpeResCustService_Fault_Rollback() throws Exception {

		System.out.println("starting:  testCreateVcpeResCustService_Fault_Rollback\n");
		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "2", "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "VCPE/CreateVcpeResCustService/getCatalogServiceResourcesData.json");
		MockGetCustomer(CUST, "VCPE/CreateVcpeResCustService/getCustomer.xml");
		
		// TODO: the SI should NOT have to be URL-encoded yet again!
		MockPutServiceInstance(CUST, SVC, INST.replace("%", "%25"), "GenericFlows/getServiceInstance.xml");
		MockGetServiceInstance(CUST, SVC, INST.replace("%", "%25"), "GenericFlows/getServiceInstance.xml");
		
		MockNodeQueryServiceInstanceById(INST, "GenericFlows/getSIUrlById.xml");
		MockNodeQueryServiceInstanceById(PARENT_INST, "GenericFlows/getParentSIUrlById.xml");		
		MockGetServiceInstance(CUST, SVC, INST, "GenericFlows/getServiceInstance.xml");
		MockGetServiceInstance(CUST, SVC, PARENT_INST, "GenericFlows/getParentServiceInstance.xml");
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/CreateVcpeResCustService/arGetById.xml");
		MockGetAllottedResource(CUST, SVC, PARENT_INST, ARID, "VCPE/CreateVcpeResCustService/arGetById.xml");
		MockPutAllottedResource(CUST, SVC, PARENT_INST, ARID);
		MockPatchAllottedResource(CUST, SVC, PARENT_INST, ARID);
		MockDeleteAllottedResource(CUST, SVC, PARENT_INST, ARID, ARVERS);

		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		Map<String, Object> variables = setupVariables();

		String businessKey = UUID.randomUUID().toString();
		invokeAsyncProcess(PROCNAME, "v1", businessKey, request, variables);
		
		// for SI
		injectSDNCCallbacks(callbacks, "assign");
		
		// for TXC
		injectSDNCCallbacks(callbacks, "assign");
		injectSDNCCallbacks(callbacks, "create");
		injectSDNCCallbacks(callbacks, "activate");
		injectSDNCCallbacks(callbacks, "queryTXC");
		
		// BRG is a stub so don't need SDNC callbacks
		
		// for TXC rollback
		injectSDNCCallbacks(callbacks, "deactivate");
		injectSDNCCallbacks(callbacks, "delete");
		injectSDNCCallbacks(callbacks, "unassign");
		
		waitForProcessEnd(businessKey, 10000);

		String workflowException = BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_WFEX);
		System.out.println("workflowException:\n" + workflowException);

		String completionReq = BPMNUtil.getVariable(processEngineRule, PROCNAME, Prefix+VAR_COMP_REQ);
		System.out.println("completionReq:\n" + completionReq);
		
		assertEquals(null, BPMNUtil.getVariable(processEngineRule, PROCNAME, PROCNAME+VAR_SUCCESS_IND));
		assertEquals(null, BPMNUtil.getVariable(processEngineRule, PROCNAME, VAR_RESP_CODE));
		assertEquals(null, completionReq);
		assertNotNull(workflowException);
		
		BPMNUtil.assertAnyProcessInstanceFinished(processEngineRule, "DoCreateAllottedResourceBRGRollback");
		BPMNUtil.assertAnyProcessInstanceFinished(processEngineRule, "DoCreateVnfAndModulesRollback");
		BPMNUtil.assertAnyProcessInstanceFinished(processEngineRule, "DoCreateAllottedResourceTXCRollback");
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
		return variables;

	}
}
