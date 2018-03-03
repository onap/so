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

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for DeleteAAIVfModule.bpmn.
 */
public class DeleteAAIVfModuleTest extends WorkflowTest {
	private static final String EOL = "\n";
	
	@Test	
	@Deployment(resources = {
			"subprocess/DeleteAAIVfModule.bpmn"
		})
	public void  TestDeleteGenericVnfSuccess_200() {
		// delete the Base Module and Generic Vnf
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
		MockAAIGenericVnfSearch();
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("DeleteAAIVfModuleRequest","<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\"> <request-info> <action>DELETE_VF_MODULE</action> <source>PORTAL</source> </request-info> <vnf-inputs> <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id> <vnf-name>STMTN5MMSC21</vnf-name> <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id> <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name> </vnf-inputs> <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/> </vnf-request>");
		runtimeService.startProcessInstanceByKey("DeleteAAIVfModule", variables);
		String response = BPMNUtil.getVariable(processEngineRule, "DeleteAAIVfModule", "DAAIVfMod_deleteGenericVnfResponseCode");
		String responseCode = BPMNUtil.getVariable(processEngineRule, "DeleteAAIVfModule", "DAAIVfMod_deleteGenericVnfResponseCode");
		Assert.assertEquals("200", responseCode);
		System.out.println(response);
	}

	@Test	
	@Deployment(resources = {
			"subprocess/DeleteAAIVfModule.bpmn"
		})
	public void  TestDeleteVfModuleSuccess_200() {
		// delete Add-on Vf Module for existing Generic Vnf
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c720, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a75
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c720</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC20</vnf-name>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a75</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC20-MMSC::module-1-0</vf-module-name>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;

		MockAAIGenericVnfSearch();
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("DeleteAAIVfModuleRequest",request);
		runtimeService.startProcessInstanceByKey("DeleteAAIVfModule", variables);
		String response = BPMNUtil.getVariable(processEngineRule, "DeleteAAIVfModule", "DAAIVfMod_deleteVfModuleResponseCode");
		String responseCode = BPMNUtil.getVariable(processEngineRule, "DeleteAAIVfModule", "DAAIVfMod_deleteVfModuleResponseCode");
		Assert.assertEquals("200", responseCode);
		System.out.println(response);
	}

	@Test	
	@Deployment(resources = {
			"subprocess/DeleteAAIVfModule.bpmn"
		})
	public void  TestQueryGenericVnfFailure_5000() {
		// query Generic Vnf failure (non-404) with A&AI
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c723, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a71
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c723</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC23</vnf-name>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a71</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC20-MMSC::module-1-0</vf-module-name>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;
		MockAAIGenericVnfSearch();
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("DeleteAAIVfModuleRequest",request);
		runtimeService.startProcessInstanceByKey("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "DeleteAAIVfModule", "WorkflowException");
		Assert.assertEquals(5000, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("<messageId>SVC3002</messageId>"));
		System.out.println(exception.getErrorMessage());
	}

	@Test	
	@Deployment(resources = {
			"subprocess/DeleteAAIVfModule.bpmn"
		})
	public void  TestQueryGenericVnfFailure_1002() {
		// attempt to delete Vf Module for Generic Vnf that does not exist (A&AI returns 404)
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c722, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a72
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c722</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC22</vnf-name>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a72</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC22-MMSC::module-1-0</vf-module-name>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;
		MockAAIGenericVnfSearch();
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("DeleteAAIVfModuleRequest",request);
		runtimeService.startProcessInstanceByKey("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "DeleteAAIVfModule", "WorkflowException");
		Assert.assertEquals(1002, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("Generic VNF Not Found"));
		System.out.println(exception.getErrorMessage());
	}

	@Test	
	@Deployment(resources = {
			"subprocess/DeleteAAIVfModule.bpmn"
		})
	public void  TestDeleteGenericVnfFailure_5000() {
		// A&AI failure (non-200) when attempting to delete a Generic Vnf
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c718, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a78
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c718</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC18</vnf-name>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a78</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC18-MMSC::module-0-0</vf-module-name>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;
		MockAAIGenericVnfSearch();
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("DeleteAAIVfModuleRequest",request);
		runtimeService.startProcessInstanceByKey("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "DeleteAAIVfModule", "WorkflowException");
		Assert.assertEquals(5000, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("<messageId>SVC3002</messageId>"));
		System.out.println(exception.getErrorMessage());
	}
	
	@Test	
	@Deployment(resources = {
			"subprocess/DeleteAAIVfModule.bpmn"
		})
	public void  TestDeleteVfModuleFailure_5000() {
		// A&AI failure (non-200) when attempting to delete a Vf Module
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c719, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a77
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c719</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC19</vnf-name>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a77</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC19-MMSC::module-1-0</vf-module-name>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;
		MockAAIGenericVnfSearch();
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("DeleteAAIVfModuleRequest",request);
		runtimeService.startProcessInstanceByKey("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "DeleteAAIVfModule", "WorkflowException");
		Assert.assertEquals(5000, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("<messageId>SVC3002</messageId>"));
		System.out.println(exception.getErrorMessage());
	}
	
	@Test	
	@Deployment(resources = {
			"subprocess/DeleteAAIVfModule.bpmn"
		})
	public void  TestDeleteVfModuleFailure_1002_1() {
		// failure attempting to delete Base Module when not the last Vf Module
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c720, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a74
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c720</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC20</vnf-name>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a74</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC20-MMSC::module-0-0</vf-module-name>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;
		MockAAIGenericVnfSearch();
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("DeleteAAIVfModuleRequest",request);
		runtimeService.startProcessInstanceByKey("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "DeleteAAIVfModule", "WorkflowException");
		Assert.assertEquals(1002, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("is Base Module, not Last Module"));
		System.out.println(exception.getErrorMessage());
	}

	@Test	
	@Deployment(resources = {
			"subprocess/DeleteAAIVfModule.bpmn"
		})
	public void  TestDeleteVfModuleFailure_1002_2() {
		// failure attempting to delete a Vf Module that does not exist (A&AI returns 404)
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c720, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a76
		MockAAIGenericVnfSearch();
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("DeleteAAIVfModuleRequest","<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\"> <request-info> <action>DELETE_VF_MODULE</action> <source>PORTAL</source> </request-info> <vnf-inputs> <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c720</vnf-id> <vnf-name>STMTN5MMSC20</vnf-name> <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a76</vf-module-id> <vf-module-name>STMTN5MMSC20-MMSC::module-2-0</vf-module-name> </vnf-inputs> <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/> </vnf-request>");
		runtimeService.startProcessInstanceByKey("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngineRule, "DeleteAAIVfModule", "WorkflowException");
		Assert.assertEquals(1002, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("does not exist for Generic Vnf Id"));
		System.out.println(exception.getErrorMessage());
	}
	
	// Start of VF Modularization A&AI mocks
	
	public static void MockAAIGenericVnfSearch(){
		String body;
	
		// The following stubs are for CreateAAIVfModule and UpdateAAIVfModule
	
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC23&depth=1"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
	
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC22&depth=1"))
				.willReturn(aResponse()
						.withStatus(404)
						.withHeader("Content-Type", "text/xml")
						.withBody("Generic VNF Not Found")));
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/768073c7-f41f-4822-9323-b75962763d74[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(404)
						.withHeader("Content-Type", "text/xml")
						.withBody("Generic VNF Not Found")));
	
		body =
			"<generic-vnf xmlns=\"http://com.aai.inventory/v7\">" + EOL +
			"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>1508691</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>1508692</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC21&depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		body =
			"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
			"  <vnf-id>2f6aee38-1e2a-11e6-82d1-ffc7d9ee8aa4</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC20</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>1508691</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC20-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>1508692</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a74</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC20-MMSC::module-1-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a74</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>false</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>1508692</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC20&depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/2f6aee38-1e2a-11e6-82d1-ffc7d9ee8aa4[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		// The following stubs are for DeleteAAIVfModule
	
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c723[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
	
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c722[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(404)
						.withHeader("Content-Type", "text/xml")
						.withBody("Generic VNF Not Found")));
	
		body =
				"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
				"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
				"  <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
				"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
				"  <service-id>SDN-MOBILITY</service-id>" + EOL +
				"  <equipment-role>vMMSC</equipment-role>" + EOL +
				"  <orchestration-status>pending-create</orchestration-status>" + EOL +
				"  <in-maint>false</in-maint>" + EOL +
				"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
				"  <resource-version>0000021</resource-version>" + EOL +
				"  <vf-modules>" + EOL +
				"    <vf-module>" + EOL +
				"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
				"      <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
				"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</persona-model-id>" + EOL +
				"      <persona-model-version>1.0</persona-model-version>" + EOL +
				"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
				"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
				"      <orchestration-status>pending-create</orchestration-status>" + EOL +
				"      <resource-version>0000073</resource-version>" + EOL +
				"    </vf-module>" + EOL +
				"  </vf-modules>" + EOL +
				"  <relationship-list/>" + EOL +
				"  <l-interfaces/>" + EOL +
				"  <lag-interfaces/>" + EOL +
				"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		body =
			"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
			"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c720</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC20</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>0000020</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a74</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC20-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a74</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000074</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a75</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC20-MMSC::module-1-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a75</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>false</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000075</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c720[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		body =
			"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
			"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c719</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC19</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>0000019</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a76</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC19-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a76</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000076</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a77</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC19-MMSC::module-1-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a77</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>false</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000077</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c719[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		body =
			"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
			"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c718</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC18</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>0000018</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a78</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC18-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a78</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000078</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		body =
			"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
			"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>0000021</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000073</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a73"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	}
	public static void MockAAIDeleteGenericVnf(){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721/[?]resource-version=0000021"))
				.willReturn(aResponse()
						.withStatus(200)));
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718/[?]resource-version=0000018"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
	}

	public static void MockAAIDeleteVfModule(){
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a73/[?]resource-version=0000073"))
				.willReturn(aResponse()
						.withStatus(200)));
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c720/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a75/[?]resource-version=0000075"))
				.willReturn(aResponse()
						.withStatus(200)));
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a78/[?]resource-version=0000078"))
				.willReturn(aResponse()
						.withStatus(200)));
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c719/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a77/[?]resource-version=0000077"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
		stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy\\?network-policy-fqdn=.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("VfModularity/QueryNetworkPolicy_AAIResponse_Success.xml")));

		stubFor(delete(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/.*"))
				.willReturn(aResponse()
						.withStatus(200)));
	}
}

