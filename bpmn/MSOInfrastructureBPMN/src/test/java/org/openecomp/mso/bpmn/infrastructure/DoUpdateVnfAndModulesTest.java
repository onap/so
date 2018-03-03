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

import static org.junit.Assert.assertEquals;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockAAIVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDBUpdateVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithDepth;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfById_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchVfModuleId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockSDNCAdapterVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;
import static org.openecomp.mso.bpmn.mock.StubResponseVNFAdapter.mockVNFPut;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockVNFAdapterRestVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.core.domain.ModelInfo;
import org.openecomp.mso.bpmn.core.domain.ModuleResource;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.bpmn.core.domain.VnfResource;
import org.openecomp.mso.bpmn.mock.FileUtil;

/**
 * Unit Test for the DoUpdateVnfAndModules Flow
 *
 */
public class DoUpdateVnfAndModulesTest extends WorkflowTest {

	private final CallbackSet callbacks = new CallbackSet();

	public DoUpdateVnfAndModulesTest() throws IOException {	

		callbacks.put("changeassign", FileUtil.readResourceFile(
				"__files/VfModularity/SDNCTopologyChangeAssignCallback.xml"));
		callbacks.put("activate", FileUtil.readResourceFile(
				"__files/VfModularity/SDNCTopologyActivateCallback.xml"));
		callbacks.put("query", FileUtil.readResourceFile(
				"__files/VfModularity/SDNCTopologyQueryCallback.xml"));		
		callbacks.put("vnfUpdate", FileUtil.readResourceFile(
				"__files/VfModularity/VNFAdapterRestUpdateCallback.xml"));
	}

	@Test
	
	@Deployment(resources = {			
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/PrepareUpdateAAIVfModule.bpmn",
			"subprocess/DoUpdateVfModule.bpmn",
			"subprocess/DoUpdateVnfAndModules.bpmn",		
			"subprocess/VnfAdapterRestV1.bpmn",
			"subprocess/ConfirmVolumeGroupTenant.bpmn",		
			"subprocess/UpdateAAIVfModule.bpmn",			
			"subprocess/UpdateAAIGenericVnf.bpmn"})
	public void testDoUpdateVnfAndModules_success() throws Exception{
		
		MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlByIdVipr.xml");
		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");
		//MockGetGenericVnfById_404("testVnfId");
		MockGetGenericVnfByIdWithDepth("skask", 1, "VfModularity/GenericVnf.xml");
		MockPutGenericVnf(".*");
		MockAAIVfModule();
		MockPatchGenericVnf("skask");
		MockPatchVfModuleId("skask", ".*");
		mockSDNCAdapter("VfModularity/StandardSDNCSynchResponse.xml");	
		mockVNFPut("skask", "/supercool", 202);
		mockVNFPut("skask", "/lukewarm", 202);
		MockVNFAdapterRestVfModule();
		MockDBUpdateVfModule();	
		
		mockSDNCAdapter("VfModularity/StandardSDNCSynchResponse.xml");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "", "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");
		invokeSubProcess("DoUpdateVnfAndModules", businessKey, variables);

		injectSDNCCallbacks(callbacks, "changeassign, query");
		injectVNFRestCallbacks(callbacks, "vnfUpdate");
		injectSDNCCallbacks(callbacks, "activate");
		injectSDNCCallbacks(callbacks, "changeassign, query");
		injectVNFRestCallbacks(callbacks, "vnfUpdate");
		injectSDNCCallbacks(callbacks, "activate");
		waitForProcessEnd(businessKey, 10000);

		Assert.assertTrue(isProcessEnded(businessKey));
		assertVariables("2", "200", null);

	}
	
	

	private void assertVariables(String exModuleCount, String exVnfFound, String exWorkflowException) {

		String moduleCount = BPMNUtil.getVariable(processEngineRule, "DoUpdateVnfAndModules", "DUVAM_moduleCount");		
		String vnfFound = BPMNUtil.getVariable(processEngineRule, "DoUpdateVnfAndModules", "DUVAM_queryAAIVfModuleResponseCode");		
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DoUpdateVnfAndModules", "SavedWorkflowException1");
		
		assertEquals(exModuleCount, moduleCount);
		assertEquals(exVnfFound, vnfFound);		
		assertEquals(exWorkflowException, workflowException);
	}

	private void setVariables(Map<String, String> variables, String request, String requestId, String siId) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("bpmnRequest", request);
		variables.put("msoRequestUdid", requestId);
		variables.put("serviceInstanceId",siId);
		variables.put("testVnfId","testVnfId123");
		variables.put("vnfType", "STMTN");
	
	}

	private void setVariablesSuccess(Map<String, Object> variables, String request, String requestId, String siId) {
		variables.put("isDebugLogEnabled", "true");			
		variables.put("requestId", requestId);
		variables.put("msoRequestId", requestId);
		variables.put("serviceInstanceId",siId);		
		variables.put("disableRollback", "true");		
		//variables.put("testVnfId","testVnfId123");
		variables.put("vnfType", "STMTN");
		variables.put("vnfId", "skask");
		variables.put("tenantId", "88a6ca3ee0394ade9403f075db23167e");
		variables.put("lcpCloudRegionId", "mdt1");
		
		String serviceModelInfo = "{ "+ "\"modelType\": \"service\"," +
				"\"modelInvariantUuid\": \"995256d2-5a33-55df-13ab-12abad84e7ff\"," +
				"\"modelUuid\": \"ab6478e5-ea33-3346-ac12-ab121484a3fe\"," +
				"\"modelName\": \"ServicevSAMP12\"," +
				"\"modelVersion\": \"1.0\"," +
				"}";
		variables.put("serviceModelInfo", serviceModelInfo);
		variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		String vnfModelInfo = "{" + "\"modelType\": \"vnf\"," +
				"\"modelInvariantUuid\": \"ff5256d2-5a33-55df-13ab-12abad84e7ff\"," +
				"\"modelUuid\": \"fe6478e5-ea33-3346-ac12-ab121484a3fe\"," +
				"\"modelName\": \"vSAMP12\"," +
				"\"modelVersion\": \"1.0\"," + 
				"\"modelCustomizationUuid\": \"MODEL-ID-1234\"" + "}";
		variables.put("vnfModelInfo", vnfModelInfo);

		String cloudConfiguration = "{" + "\"cloudConfiguration\": { " +
				"\"lcpCloudRegionId\": \"mdt1\"," +
				"\"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\"" + "}}";
		variables.put("cloudConfiguration", cloudConfiguration);
		variables.put("sdncVersion", "1702");
		variables.put("globalSubscriberId", "subscriber123");
		variables.put("asdcServiceModelVersion", "serviceVersion01");
		
		try {						
			VnfResource vr = new VnfResource();
			ModelInfo mvr = new ModelInfo();
			mvr.setModelName("vSAMP12");
			mvr.setModelInstanceName("v123");
			mvr.setModelInvariantUuid("extrovert");
			mvr.setModelVersion("1.0");
			mvr.setModelCustomizationUuid("MODEL-ID-1234");
			vr.setModelInfo(mvr);
			vr.constructVnfType("vnf1");			
			vr.setNfType("somenftype");
			vr.setNfRole("somenfrole");
			vr.setNfFunction("somenffunction");
			vr.setNfNamingCode("somenamingcode");	
			ModuleResource mr = new ModuleResource();
			ModelInfo mvmr = new ModelInfo();
			mvmr.setModelInvariantUuid("introvert");
			mvmr.setModelName("STMTN5MMSC21-MMSC::model-1-0");
			mvmr.setModelVersion("1");
			mvmr.setModelCustomizationUuid("MODEL-123");
			mr.setModelInfo(mvmr);
			mr.setIsBase(true);
			mr.setVfModuleLabel("MODULELABEL");
			vr.addVfModule(mr);
			ModuleResource mr1 = new ModuleResource();
			ModelInfo mvmr1 = new ModelInfo();
			mvmr1.setModelInvariantUuid("extrovert");
			mvmr1.setModelName("SECONDMODELNAME");
			mvmr1.setModelVersion("1");
			mvmr1.setModelCustomizationUuid("MODEL-123");
			mr1.setModelInfo(mvmr1);
			mr1.setIsBase(false);
			mr1.setVfModuleLabel("MODULELABEL1");
			vr.addVfModule(mr1);			
			variables.put("vnfResourceDecomposition", vr);
			variables.put("isTest", true);
		} catch(Exception e) {
			
		}
		
	}
		
	
}
