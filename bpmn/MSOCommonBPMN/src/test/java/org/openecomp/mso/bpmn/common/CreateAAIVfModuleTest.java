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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.openecomp.mso.bpmn.common.DeleteAAIVfModuleTest.MockAAIGenericVnfSearch;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.bpmn.core.WorkflowException;

/**
 * Unit test for CreateAAIVfModule.bpmn.
 */
public class CreateAAIVfModuleTest extends WorkflowTest {
	
	@Test	
	@Deployment(resources = {
			"subprocess/CreateAAIVfModule.bpmn"
		})
	public void  TestCreateGenericVnfSuccess_200() {

		MockAAIGenericVnfSearch();
		MockAAICreateGenericVnf();
		MockAAIVfModulePUT(true);
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("isVidRequest", "false");
		variables.put("vnfName", "STMTN5MMSC22");
		variables.put("serviceId", "00000000-0000-0000-0000-000000000000");
		variables.put("personaModelId", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		variables.put("personaModelVersion", "1.0");
		variables.put("vfModuleName", "STMTN5MMSC22-MMSC::module-0-0");
		variables.put("vfModuleModelName", "MMSC::module-0");
		
		runtimeService.startProcessInstanceByKey("CreateAAIVfModule", variables);
		String response = BPMNUtil.getVariable(processEngineRule, "CreateAAIVfModule", "CAAIVfMod_createVfModuleResponseCode");
		String responseCode = BPMNUtil.getVariable(processEngineRule, "CreateAAIVfModule", "CAAIVfMod_createVfModuleResponseCode");
		Assert.assertEquals("201", responseCode);
		System.out.println(response);
	}

	@Test	
	@Deployment(resources = {
			"subprocess/CreateAAIVfModule.bpmn"
		})
	public void  TestCreateVfModuleSuccess_200() {
		// create Add-on VF Module for existing Generic VNF
		MockAAIGenericVnfSearch();
		MockAAICreateGenericVnf();
		MockAAIVfModulePUT(true);
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("isVidRequest", "false");
		variables.put("vnfId", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("serviceId", "00000000-0000-0000-0000-000000000000");
		variables.put("personaModelId", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		variables.put("personaModelVersion", "1.0");
		variables.put("vfModuleName", "STMTN5MMSC21-MMSC::module-1-0");
		variables.put("vfModuleModelName", "STMTN5MMSC21-MMSC::model-1-0");
		runtimeService.startProcessInstanceByKey("CreateAAIVfModule", variables);
		String response = BPMNUtil.getVariable(processEngineRule, "CreateAAIVfModule", "CAAIVfMod_createVfModuleResponseCode");
		String responseCode = BPMNUtil.getVariable(processEngineRule, "CreateAAIVfModule", "CAAIVfMod_createVfModuleResponseCode");
		Assert.assertEquals("201", responseCode);
		System.out.println(response);
	}

	@Test	
	@Deployment(resources = {
			"subprocess/CreateAAIVfModule.bpmn"
		})
	public void  TestQueryGenericVnfFailure_5000() {
		MockAAIGenericVnfSearch();
		MockAAICreateGenericVnf();
		MockAAIVfModulePUT(true);
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");		
		variables.put("isVidRequest", "false");
		variables.put("vnfName", "STMTN5MMSC23");
		variables.put("serviceId", "00000000-0000-0000-0000-000000000000");
		variables.put("personaModelId", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		variables.put("personaModelVersion", "1.0");
		variables.put("vfModuleName", "STMTN5MMSC23-MMSC::module-0-0");
		variables.put("vfModuleModelName", "MMSC::module-0");
		runtimeService.startProcessInstanceByKey("CreateAAIVfModule", variables);		
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "CreateAAIVfModule", "WorkflowException");
		Assert.assertEquals(500, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("Error occurred attempting to query AAI"));
		System.out.println(exception.getErrorMessage());
	}

	@Test	
	@Deployment(resources = {
			"subprocess/CreateAAIVfModule.bpmn"
		})
	public void  TestCreateDupGenericVnfFailure_1002() {
		MockAAIGenericVnfSearch();
		MockAAICreateGenericVnf();
		MockAAIVfModulePUT(true);
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");		
		variables.put("isVidRequest", "false");
		variables.put("vnfName", "STMTN5MMSC21");
		variables.put("serviceId", "00000000-0000-0000-0000-000000000000");
		variables.put("personaModelId", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		variables.put("personaModelVersion", "1.0");
		variables.put("vfModuleName", "STMTN5MMSC21-MMSC::module-0-0");
		variables.put("vfModuleModelName", "MMSC::module-0");
		runtimeService.startProcessInstanceByKey("CreateAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "CreateAAIVfModule", "WorkflowException");
		Assert.assertEquals(1002, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("Invalid request for new Generic VNF which already exists"));
		System.out.println(exception.getErrorMessage());
	}

	@Test	
	@Deployment(resources = {
			"subprocess/CreateAAIVfModule.bpmn"
		})
	public void  TestCreateDupVfModuleFailure_1002() {
		MockAAIGenericVnfSearch();
		MockAAICreateGenericVnf();
		MockAAIVfModulePUT(true);
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");		
		variables.put("isVidRequest", "false");
		variables.put("vnfId", "2f6aee38-1e2a-11e6-82d1-ffc7d9ee8aa4");
		variables.put("serviceId", "00000000-0000-0000-0000-000000000000");
		variables.put("personaModelId", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		variables.put("personaModelVersion", "1.0");
		variables.put("vfModuleName", "STMTN5MMSC20-MMSC::module-1-0");
		variables.put("vfModuleModelName", "STMTN5MMSC20-MMSC::model-1-0");
		runtimeService.startProcessInstanceByKey("CreateAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "CreateAAIVfModule", "WorkflowException");
		Assert.assertEquals(1002, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("already exists for Generic VNF"));
		System.out.println(exception.getErrorMessage());
	}
	
	@Test	
	@Deployment(resources = {
			"subprocess/CreateAAIVfModule.bpmn"
		})
	public void  TestCreateGenericVnfFailure_5000() {
		MockAAIGenericVnfSearch();
		MockAAICreateGenericVnf();
		MockAAIVfModulePUT(true);
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");		
		variables.put("isVidRequest", "false");
		variables.put("vnfName", "STMTN5MMSC22");
		variables.put("serviceId", "99999999-9999-9999-9999-999999999999");
		variables.put("personaModelId", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		variables.put("personaModelVersion", "1.0");
		variables.put("vfModuleName", "STMTN5MMSC22-PCRF::module-1-0");
		variables.put("vfModuleModelName", "PCRF::module-0");
		runtimeService.startProcessInstanceByKey("CreateAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "CreateAAIVfModule", "WorkflowException");
		Assert.assertEquals(5000, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("<messageId>SVC3002</messageId>"));
		System.out.println(exception.getErrorMessage());
	}

	@Test	
	@Deployment(resources = {
			"subprocess/CreateAAIVfModule.bpmn"
		})
	public void  TestCreateGenericVnfFailure_1002() {
		MockAAIGenericVnfSearch();
		MockAAICreateGenericVnf();
		MockAAIVfModulePUT(true);
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");		
		variables.put("isVidRequest", "false");
		variables.put("vnfId", "768073c7-f41f-4822-9323-b75962763d74");
		variables.put("serviceId", "00000000-0000-0000-0000-000000000000");
		variables.put("personaModelId", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		variables.put("personaModelVersion", "1.0");
		variables.put("vfModuleName", "STMTN5MMSC22-PCRF::module-1-0");
		variables.put("vfModuleModelName", "PCRF::module-0");
		runtimeService.startProcessInstanceByKey("CreateAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "CreateAAIVfModule", "WorkflowException");
		Assert.assertEquals(1002, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("Generic VNF Not Found"));
		System.out.println(exception.getErrorMessage());
	}

	@Test	
	@Deployment(resources = {
			"subprocess/CreateAAIVfModule.bpmn"
		})
	public void  TestCreateVfModuleFailure_5000() {
		MockAAIGenericVnfSearch();
		MockAAICreateGenericVnf();
		MockAAIVfModulePUT(true);
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");		
		variables.put("isVidRequest", "false");
		variables.put("vnfId", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("serviceId", "99999999-9999-9999-9999-999999999999");
		variables.put("personaModelId", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		variables.put("personaModelVersion", "1.0");
		variables.put("vfModuleName", "STMTN5MMSC21-PCRF::module-1-0");
		variables.put("vfModuleModelName", "STMTN5MMSC21-PCRF::model-1-0");
		runtimeService.startProcessInstanceByKey("CreateAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "CreateAAIVfModule", "WorkflowException");
		Assert.assertEquals(5000, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("<messageId>SVC3002</messageId>"));
		System.out.println(exception.getErrorMessage());
	}

	public static void MockAAICreateGenericVnf(){
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/.*"))
				.withRequestBody(containing("<service-id>00000000-0000-0000-0000-000000000000</service-id>"))
				.willReturn(aResponse()
						.withStatus(201)));
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/.*"))
				.withRequestBody(containing("<service-id>99999999-9999-9999-9999-999999999999</service-id>"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
	}
	
	// start of mocks used locally and by other VF Module unit tests
	public static void MockAAIVfModulePUT(boolean isCreate){
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/.*/vf-modules/vf-module/.*"))
				.withRequestBody(containing("MMSC"))
				.willReturn(aResponse()
						.withStatus(isCreate ? 201 : 200)));
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/.*/vf-modules/vf-module/.*"))
				.withRequestBody(containing("PCRF"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721"))				
				.willReturn(aResponse()
					.withStatus(200)));
	}
	
}