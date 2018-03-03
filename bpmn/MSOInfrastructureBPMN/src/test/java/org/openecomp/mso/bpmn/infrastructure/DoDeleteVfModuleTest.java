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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Unit test for DoDeleteVfModule.bpmn.
 */
public class DoDeleteVfModuleTest extends WorkflowTest {
	private final CallbackSet callbacks = new CallbackSet();
	
	private static final String EOL = "\n";

	private final String vnfAdapterDeleteCallback = 
		"<deleteVfModuleResponse>" + EOL +
		"    <vnfId>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnfId>" + EOL +
		"    <vfModuleId>973ed047-d251-4fb9-bf1a-65b8949e0a73</vfModuleId>" + EOL +
		"    <vfModuleDeleted>true</vfModuleDeleted>" + EOL +
		"    <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
		"</deleteVfModuleResponse>" + EOL;
			
	private final String vnfAdapterDeleteCallbackFail = 
			"<vfModuleException>" + EOL +
			"    <message>Error processing request to VNF-Async. Not Found.</message>" + EOL +
			"    <category>INTERNAL</category>" + EOL +
			"    <rolledBack>false</rolledBack>" + EOL +
			"    <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
			"</vfModuleException>" + EOL;
				
	private final String sdncAdapterDeleteCallback =
		"<output xmlns=\"org:onap:sdnctl:l3api\">" + EOL +
		"  <svc-request-id>{{REQUEST-ID}}</svc-request-id>" + EOL +
		"  <ack-final-indicator>Y</ack-final-indicator>" + EOL +
		"</output>" + EOL;
	
	public DoDeleteVfModuleTest() throws IOException {
		callbacks.put("sdncChangeDelete", sdncAdapterDeleteCallback);
		callbacks.put("sdncDelete", sdncAdapterDeleteCallback);
		callbacks.put("vnfDelete", FileUtil.readResourceFile(
				"__files/DeleteVfModuleCallbackResponse.xml"));
		callbacks.put("vnfDeleteFail", vnfAdapterDeleteCallbackFail);
	}
	
	private final String wfeString = "WorkflowException";

	@Test
	@Deployment(resources = {
			"subprocess/DoDeleteVfModule.bpmn",
			"subprocess/PrepareUpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn",
			"subprocess/DeleteAAIVfModule.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void  TestDoDeleteVfModuleSuccess() {
		// delete the Base Module and Generic Vnf
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
			"    <vnf-type>asc_heat-int</vnf-type>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
			"    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
			"    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL +
			"    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
			"    <orchestration-status>pending-delete</orchestration-status>" + EOL +
			"    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;
		logStart();
		MockDoDeleteVfModule_SDNCSuccess();
		MockDoDeleteVfModule_DeleteVNFSuccess();
		MockAAIGenericVnfSearch();
		MockAAIVfModulePUT(false);
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		MockPatchVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("mso-service-instance-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("DoDeleteVfModuleRequest",request);
		variables.put("isVidRequest", "true");
		invokeSubProcess("DoDeleteVfModule", businessKey, variables);

		// "changedelete" operation not required for deleting a Vf Module
//		injectSDNCCallbacks(callbacks, "sdncChangeDelete");
		injectVNFRestCallbacks(callbacks, "vnfDelete");
		waitForRunningProcessCount("vnfAdapterDeleteV1", 0, 120000);
		injectSDNCCallbacks(callbacks, "sdncDelete");

		waitForProcessEnd(businessKey, 10000);
		WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, wfeString);
		checkVariable(businessKey, wfeString, null);
		if (wfe != null) {
			System.out.println("TestDoDeleteVfModuleSuccess: ErrorCode=" + wfe.getErrorCode() +
					", ErrorMessage=" + wfe.getErrorMessage());
		}
		logEnd();
	}

	@Test
	@Deployment(resources = {
			"subprocess/DoDeleteVfModule.bpmn",
			"subprocess/PrepareUpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn",
			"subprocess/DeleteAAIVfModule.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void  TestDoDeleteVfModule_Building_Block_Success() {
		logStart();
		MockDoDeleteVfModule_SDNCSuccess();
		MockDoDeleteVfModule_DeleteVNFSuccess();
		MockAAIGenericVnfSearch();
		MockAAIVfModulePUT(false);
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		MockPatchVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		
		variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");	
		variables.put("requestId", "a27ce5a9-29c4-4c22-a017-6615ac73c721");		
		variables.put("isDebugLogEnabled","true");
		variables.put("vnfId", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("vfModuleId", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		variables.put("serviceInstanceId", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("vfModuleName", "STMTN5MMSC21-MMSC::module-0-0");
		variables.put("sdncVersion", "1610");
		variables.put("isVidRequest", "true");
		variables.put("retainResources", false);
		String vfModuleModelInfo = "{" + "\"modelType\": \"vnf\"," +
				"\"modelInvariantUuid\": \"ff5256d2-5a33-55df-13ab-12abad84e7ff\"," + 
				"\"modelUuid\": \"fe6478e5-ea33-3346-ac12-ab121484a3fe\"," +
				"\"modelName\": \"vSAMP12\"," +
				"\"modelVersion\": \"1.0\"," + 
				"\"modelCustomizationUuid\": \"MODEL-ID-1234\"," + 
				"}";
		variables.put("vfModuleModelInfo", vfModuleModelInfo);
			
		String cloudConfiguration = "{" + 
				"\"lcpCloudRegionId\": \"RDM2WAGPLCP\"," +		
				"\"tenantId\": \"fba1bd1e195a404cacb9ce17a9b2b421\"" + "}";
		variables.put("cloudConfiguration", cloudConfiguration);
	
		
		invokeSubProcess("DoDeleteVfModule", businessKey, variables);

		// "changedelete" operation not required for deleting a Vf Module
//		injectSDNCCallbacks(callbacks, "sdncChangeDelete");
		injectVNFRestCallbacks(callbacks, "vnfDelete");
		waitForRunningProcessCount("vnfAdapterDeleteV1", 0, 120000);
		injectSDNCCallbacks(callbacks, "sdncDelete");

		waitForProcessEnd(businessKey, 10000);
		WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, wfeString);
		checkVariable(businessKey, wfeString, null);
		if (wfe != null) {
			System.out.println("TestDoDeleteVfModule_Building_Block_Success: ErrorCode=" + wfe.getErrorCode() +
					", ErrorMessage=" + wfe.getErrorMessage());
		}
		logEnd();
	}

	
	@Test
	@Deployment(resources = {
			"subprocess/DoDeleteVfModule.bpmn",
			"subprocess/PrepareUpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn",
			"subprocess/DeleteAAIVfModule.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void  TestDoDeleteVfModuleSDNCFailure() {
		// delete the Base Module and Generic Vnf - SDNCAdapter failure
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
			"    <vnf-type>asc_heat-int</vnf-type>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
			"    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
			"    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL +
			"    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
			"    <orchestration-status>pending-delete</orchestration-status>" + EOL +
			"    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;

		logStart();
		MockDoDeleteVfModule_SDNCFailure();
		MockDoDeleteVfModule_DeleteVNFSuccess();
		MockAAIGenericVnfSearch();
		MockAAIVfModulePUT(false);
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		MockPatchVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("mso-service-instance-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("DoDeleteVfModuleRequest", request);
		variables.put("isVidRequest", "true");
		invokeSubProcess("DoDeleteVfModule", businessKey, variables);

		// "changedelete" operation not required for deleting a Vf Module
//		injectSDNCCallbacks(callbacks, "sdncChangeDelete");
		injectVNFRestCallbacks(callbacks, "vnfDelete");
		waitForRunningProcessCount("vnfAdapterDeleteV1", 0, 120000);
		// cause a failure by not injecting a callback
//		injectSDNCCallbacks(callbacks, "sdncDelete");

		waitForProcessEnd(businessKey, 10000);
		WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, wfeString);
		Assert.assertNotNull(wfe);
		if (wfe != null) {
			System.out.println("TestDoDeleteVfModuleSDNCFailure: ErrorCode=" + wfe.getErrorCode() +
					", ErrorMessage=" + wfe.getErrorMessage());
			Assert.assertTrue(wfe.getErrorCode() == 7000);
			Assert.assertTrue(wfe.getErrorMessage().startsWith("Could not communicate"));
		}
		logEnd();
	}

	@Test
	@Deployment(resources = {
			"subprocess/DoDeleteVfModule.bpmn",
			"subprocess/PrepareUpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn",
			"subprocess/DeleteAAIVfModule.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void  TestDoDeleteVfModuleSDNCCallbackFailure() {
		// delete the Base Module and Generic Vnf - SDNCAdapter Callback failure
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
			"    <vnf-type>asc_heat-int</vnf-type>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
			"    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
			"    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL +
			"    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
			"    <orchestration-status>pending-delete</orchestration-status>" + EOL +
			"    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;

		logStart();
		MockDoDeleteVfModule_SDNCSuccess();
		MockDoDeleteVfModule_DeleteVNFSuccess();
		MockAAIGenericVnfSearch();
		MockAAIVfModulePUT(false);
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		MockPatchVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("mso-service-instance-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("DoDeleteVfModuleRequest",request);
		variables.put("isVidRequest", "true");
		invokeSubProcess("DoDeleteVfModule", businessKey, variables);

		// "changedelete" operation not required for deleting a Vf Module
//		injectSDNCCallbacks(callbacks, "sdncChangeDelete:ERR");
		injectVNFRestCallbacks(callbacks, "vnfDelete");
		waitForRunningProcessCount("vnfAdapterDeleteV1", 0, 120000);
		// return a failure in the callback
		injectSDNCCallbacks(callbacks, "sdncDelete:ERR");

		waitForProcessEnd(businessKey, 10000);
		WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, wfeString);
		Assert.assertNotNull(wfe);
		if (wfe != null) {
			System.out.println("TestDoDeleteVfModuleSDNCCallbackFailure: ErrorCode=" + wfe.getErrorCode() +
					", ErrorMessage=" + wfe.getErrorMessage());
			Assert.assertTrue(wfe.getErrorCode() == 5310);
			Assert.assertTrue(wfe.getErrorMessage().startsWith("Received error from SDN-C"));
		}
		logEnd();
	}

	@Test
	@Deployment(resources = {
			"subprocess/DoDeleteVfModule.bpmn",
			"subprocess/PrepareUpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn",
			"subprocess/DeleteAAIVfModule.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void  TestDoDeleteVfModuleVNFFailure() {
		// delete the Base Module and Generic Vnf - VNFAdapter failure
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
			"    <vnf-type>asc_heat-int</vnf-type>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
			"    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
			"    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL +
			"    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
			"    <orchestration-status>pending-delete</orchestration-status>" + EOL +
			"    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;

		logStart();
		MockDoDeleteVfModule_SDNCSuccess();
		MockDoDeleteVfModule_DeleteVNFFailure();
		MockAAIGenericVnfSearch();
		MockAAIVfModulePUT(false);
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		MockPatchVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("mso-service-instance-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("DoDeleteVfModuleRequest",request);
		invokeSubProcess("DoDeleteVfModule", businessKey, variables);

		// "changedelete" operation not required for deleting a Vf Module
//		injectSDNCCallbacks(callbacks, "sdncChangeDelete");
		// cause a failure by not injecting a callback
//		injectVNFRestCallbacks(callbacks, "vnfDelete");
//		waitForRunningProcessCount("vnfAdapterDeleteV1", 0, 120000);
//		injectSDNCCallbacks(callbacks, "sdncDelete");

		waitForProcessEnd(businessKey, 10000);
		WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, wfeString);
		Assert.assertNotNull(wfe);
		if (wfe != null) {
			System.out.println("TestDoDeleteVfModuleVNFFailure: ErrorCode=" + wfe.getErrorCode() +
					", ErrorMessage=" + wfe.getErrorMessage());
			Assert.assertTrue(wfe.getErrorCode() == 7020);
			Assert.assertTrue(wfe.getErrorMessage().startsWith("Received error from VnfAdapter"));
		}
		logEnd();
	}

	@Test
	@Deployment(resources = {
			"subprocess/DoDeleteVfModule.bpmn",
			"subprocess/PrepareUpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn",
			"subprocess/DeleteAAIVfModule.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void  TestDoDeleteVfModuleVNFCallbackFailure() {
		// delete the Base Module and Generic Vnf - VNFAdapter Callback failure
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
			"    <vnf-type>asc_heat-int</vnf-type>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
			"    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
			"    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL +
			"    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
			"    <orchestration-status>pending-delete</orchestration-status>" + EOL +
			"    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;

		logStart();
		MockDoDeleteVfModule_SDNCSuccess();
		MockDoDeleteVfModule_DeleteVNFSuccess();
		MockAAIGenericVnfSearch();
		MockAAIVfModulePUT(false);
		MockAAIDeleteGenericVnf();
		MockAAIDeleteVfModule();
		MockPatchVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("mso-service-instance-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("DoDeleteVfModuleRequest",request);
		invokeSubProcess("DoDeleteVfModule", businessKey, variables);

		// "changedelete" operation not required for deleting a Vf Module
//		injectSDNCCallbacks(callbacks, "sdncChangeDelete");
		injectVNFRestCallbacks(callbacks, "vnfDeleteFail");
		waitForRunningProcessCount("vnfAdapterDeleteV1", 0, 120000);
//		injectSDNCCallbacks(callbacks, "sdncDelete");

		waitForProcessEnd(businessKey, 10000);
		WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, wfeString);
		Assert.assertNotNull(wfe);
		if (wfe != null) {
			System.out.println("TestDoDeleteVfModuleVNFCallbackFailure: ErrorCode=" + wfe.getErrorCode() +
					", ErrorMessage=" + wfe.getErrorMessage());
			Assert.assertTrue(wfe.getErrorCode() == 7020);
			Assert.assertTrue(wfe.getErrorMessage().startsWith("Received vfModuleException from VnfAdapter"));
		}
		logEnd();
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

	public static void MockDoDeleteVfModule_SDNCSuccess() {
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				  .withRequestBody(containing("SvcAction>changedelete"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DeleteGenericVNFV1/sdncAdapterResponse.xml")));
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				  .withRequestBody(containing("SvcAction>delete"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DeleteGenericVNFV1/sdncAdapterResponse.xml")));
	}

	public static void MockDoDeleteVfModule_SDNCFailure() {
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				  .withRequestBody(containing("SvcAction>changedelete"))
				  .willReturn(aResponse()
				  .withStatus(500)));
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				  .withRequestBody(containing("SvcAction>delete"))
				  .willReturn(aResponse()
				  .withStatus(500)));
	}

	public static void MockDoDeleteVfModule_DeleteVNFSuccess() {
		stubFor(delete(urlMatching("/vnfs/v1/vnfs/.*/vf-modules/.*"))
				.willReturn(aResponse()
				.withStatus(202)
				.withHeader("Content-Type", "application/xml")));
		stubFor(delete(urlMatching("/vnfs/v1/volume-groups/78987"))
				.willReturn(aResponse()
				.withStatus(202)
				.withHeader("Content-Type", "application/xml")));
	}

	public static void MockDoDeleteVfModule_DeleteVNFFailure() {
		stubFor(delete(urlMatching("/vnfs/v1/vnfs/.*/vf-modules/.*"))
				.willReturn(aResponse()
				.withStatus(500)
				.withHeader("Content-Type", "application/xml")));
	}
}

