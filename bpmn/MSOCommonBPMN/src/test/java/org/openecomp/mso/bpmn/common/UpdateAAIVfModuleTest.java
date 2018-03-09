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

package org.openecomp.mso.bpmn.common;

import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockAAIVfModuleBadPatch;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithPriority;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfById_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchVfModuleId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutGenericVnf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.bpmn.mock.FileUtil;

/**
 * Unit tests for UpdateAAIVfModuleTest.bpmn.
 */
public class UpdateAAIVfModuleTest extends WorkflowTest {
		
	/**
	 * Test the happy path through the flow.
	 */
	@Test	
	@Deployment(resources = {
			"subprocess/UpdateAAIVfModule.bpmn"
		})
	public void happyPath() throws IOException {
		logStart();
		
		String updateAAIVfModuleRequest = FileUtil.readResourceFile("__files/VfModularity/UpdateAAIVfModuleRequest.xml"); 
		MockGetGenericVnfByIdWithPriority("/skask/vf-modules/vf-module/supercool", 200, "VfModularity/VfModule-supercool.xml");
		MockPutGenericVnf("/skask/vf-modules/vf-module/supercool", "PCRF", 200);
		MockPatchVfModuleId("skask", "supercool");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("mso-request-id", "999-99-9999");
		variables.put("isDebugLogEnabled","true");
		variables.put("UpdateAAIVfModuleRequest", updateAAIVfModuleRequest);
		invokeSubProcess("UpdateAAIVfModule", businessKey, variables);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String response = (String) getVariableFromHistory(businessKey, "UAAIVfMod_updateVfModuleResponse");
		Integer responseCode = (Integer) getVariableFromHistory(businessKey, "UAAIVfMod_updateVfModuleResponseCode");
		System.out.println("Subflow response code: " + responseCode);
		System.out.println("Subflow response: " + response);
		Assert.assertEquals(200, responseCode.intValue());
		
		logEnd();
	}

	/**
	 * Test the case where the GET to AAI returns a 404.
	 */
	@Test	
	@Deployment(resources = {
			"subprocess/UpdateAAIVfModule.bpmn"
		})
	public void badGet() throws IOException {
		
		logStart();
		
		String updateAAIVfModuleRequest = FileUtil.readResourceFile("__files/VfModularity/UpdateAAIVfModuleRequest.xml"); 
		MockGetGenericVnfById("/skask/vf-modules/vf-module/.*", "VfModularity/VfModule-supercool.xml", 404);
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("mso-request-id", "999-99-9999");
		variables.put("isDebugLogEnabled","true");
		variables.put("UpdateAAIVfModuleRequest", updateAAIVfModuleRequest);
		invokeSubProcess("UpdateAAIVfModule", businessKey, variables);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String response = (String) getVariableFromHistory(businessKey, "UAAIVfMod_getVfModuleResponse");
		Integer responseCode = (Integer) getVariableFromHistory(businessKey, "UAAIVfMod_getVfModuleResponseCode");
		System.out.println("Subflow response code: " + responseCode);
		System.out.println("Subflow response: " + response);
		Assert.assertEquals(404, responseCode.intValue());
		
		logEnd();
	}

	/**
	 * Test the case where the GET to AAI is successful, but he subsequent PUT returns 404.
	 */
	@Test	
	@Deployment(resources = {
			"subprocess/UpdateAAIVfModule.bpmn"
		})
	public void badPatch() throws IOException {
		
		logStart();
		
		String updateAAIVfModuleRequest = FileUtil.readResourceFile("__files/VfModularity/UpdateAAIVfModuleRequest.xml"); 
		MockGetGenericVnfById_404("/skask/vf-modules/vf-module/supercool");
		MockGetGenericVnfById("/skask/vf-modules/vf-module/supercool", "VfModularity/VfModule-supercool.xml", 200);
		MockAAIVfModuleBadPatch("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask/vf-modules/vf-module/supercool", 404);
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("mso-request-id", "999-99-9999");
		variables.put("isDebugLogEnabled","true");
		variables.put("UpdateAAIVfModuleRequest", updateAAIVfModuleRequest);
		invokeSubProcess("UpdateAAIVfModule", businessKey, variables);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String response = (String) getVariableFromHistory(businessKey, "UAAIVfMod_updateVfModuleResponse");
		Integer responseCode = (Integer) getVariableFromHistory(businessKey, "UAAIVfMod_updateVfModuleResponseCode");
		System.out.println("Subflow response code: " + responseCode);
		System.out.println("Subflow response: " + response);
		Assert.assertEquals(404, responseCode.intValue());
		
		logEnd();
	}
}

