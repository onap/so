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

package org.onap.so.bpmn.common;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for DeleteAAIVfModule.bpmn.
 */

public class DeleteAAIVfModuleIT extends BaseIntegrationTest {
	private static final String EOL = "\n";
	
	Logger logger = LoggerFactory.getLogger(DeleteAAIVfModuleIT.class);
	
	@Test	
	public void  TestDeleteGenericVnfSuccess_200() {
		// delete the Base Module and Generic Vnf
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
		new MockAAIGenericVnfSearch();
		new MockAAIDeleteGenericVnf();
		new MockAAIDeleteVfModule();
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", UUID.randomUUID().toString());
		variables.put("DeleteAAIVfModuleRequest","<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\"> <request-info> <action>DELETE_VF_MODULE</action> <source>PORTAL</source> </request-info> <vnf-inputs> <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id> <vnf-name>STMTN5MMSC21</vnf-name> <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id> <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name> </vnf-inputs> <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/> </vnf-request>");
		String processId = invokeSubProcess("DeleteAAIVfModule", variables);
		String response = BPMNUtil.getVariable(processEngine, "DeleteAAIVfModule", "DAAIVfMod_deleteGenericVnfResponseCode",processId);
		String responseCode = BPMNUtil.getVariable(processEngine, "DeleteAAIVfModule", "DAAIVfMod_deleteGenericVnfResponseCode",processId);
		Assert.assertEquals("200", responseCode);
		logger.debug(response);
	}

	@Test	
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

		new MockAAIGenericVnfSearch();
		new MockAAIDeleteGenericVnf();
		new MockAAIDeleteVfModule();
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", UUID.randomUUID().toString());
		variables.put("DeleteAAIVfModuleRequest",request);
		String processId = invokeSubProcess("DeleteAAIVfModule", variables);
		String response = BPMNUtil.getVariable(processEngine, "DeleteAAIVfModule", "DAAIVfMod_deleteVfModuleResponseCode",processId);
		String responseCode = BPMNUtil.getVariable(processEngine, "DeleteAAIVfModule", "DAAIVfMod_deleteVfModuleResponseCode",processId);
		Assert.assertEquals("200", responseCode);
		logger.debug(response);
	}

	@Test	
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
		new MockAAIGenericVnfSearch();
		new MockAAIDeleteGenericVnf();
		new MockAAIDeleteVfModule();
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", UUID.randomUUID().toString());
		variables.put("DeleteAAIVfModuleRequest",request);
		String processId = invokeSubProcess("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngine, "DeleteAAIVfModule", "WorkflowException",processId);
		Assert.assertEquals(5000, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("<messageId>SVC3002</messageId>"));
		logger.debug(exception.getErrorMessage());
	}

	@Test	
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
		new MockAAIGenericVnfSearch();
		new MockAAIDeleteGenericVnf();
		new MockAAIDeleteVfModule();
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", UUID.randomUUID().toString());
		variables.put("DeleteAAIVfModuleRequest",request);
		String processId = invokeSubProcess("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngine, "DeleteAAIVfModule", "WorkflowException",processId);
		Assert.assertEquals(1002, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("Generic VNF Not Found"));
		logger.debug(exception.getErrorMessage());
	}

	@Test	
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
		new MockAAIGenericVnfSearch();
		new MockAAIDeleteGenericVnf();
		new MockAAIDeleteVfModule();
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", UUID.randomUUID().toString());
		variables.put("DeleteAAIVfModuleRequest",request);
		String processId = invokeSubProcess("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngine, "DeleteAAIVfModule", "WorkflowException",processId);
		Assert.assertEquals(5000, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("<messageId>SVC3002</messageId>"));
		logger.debug(exception.getErrorMessage());
	}
	
	@Test	
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
		new MockAAIGenericVnfSearch();
		new MockAAIDeleteGenericVnf();
		new MockAAIDeleteVfModule();
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", UUID.randomUUID().toString());
		variables.put("DeleteAAIVfModuleRequest",request);
		String processId = invokeSubProcess("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngine, "DeleteAAIVfModule", "WorkflowException",processId);
		Assert.assertEquals(5000, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("<messageId>SVC3002</messageId>"));
		logger.debug(exception.getErrorMessage());
	}
	
	@Test	
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
		new MockAAIGenericVnfSearch();
		new MockAAIDeleteGenericVnf();
		new MockAAIDeleteVfModule();
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", UUID.randomUUID().toString());
		variables.put("DeleteAAIVfModuleRequest",request);
		String processId = invokeSubProcess("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngine, "DeleteAAIVfModule", "WorkflowException",processId);
		Assert.assertEquals(1002, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("is Base Module, not Last Module"));
		logger.debug(exception.getErrorMessage());
	}

	@Test	
	public void  TestDeleteVfModuleFailure_1002_2() {
		// failure attempting to delete a Vf Module that does not exist (A&AI returns 404)
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c720, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a76
		new MockAAIGenericVnfSearch();
		new MockAAIDeleteGenericVnf();
		new MockAAIDeleteVfModule();
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", UUID.randomUUID().toString());
		variables.put("DeleteAAIVfModuleRequest","<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\"> <request-info> <action>DELETE_VF_MODULE</action> <source>PORTAL</source> </request-info> <vnf-inputs> <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c720</vnf-id> <vnf-name>STMTN5MMSC20</vnf-name> <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a76</vf-module-id> <vf-module-name>STMTN5MMSC20-MMSC::module-2-0</vf-module-name> </vnf-inputs> <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/> </vnf-request>");
		String processId = invokeSubProcess("DeleteAAIVfModule", variables);
		WorkflowException exception = BPMNUtil.getRawVariable(processEngine, "DeleteAAIVfModule", "WorkflowException",processId);
		Assert.assertEquals(1002, exception.getErrorCode());
		Assert.assertEquals(true, exception.getErrorMessage().contains("does not exist for Generic Vnf Id"));
		logger.debug(exception.getErrorMessage());
	}
}

