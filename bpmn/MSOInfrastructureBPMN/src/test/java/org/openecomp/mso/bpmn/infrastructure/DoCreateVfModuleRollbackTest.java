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
public class DoCreateVfModuleRollbackTest extends WorkflowTest {
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
	
	public DoCreateVfModuleRollbackTest() throws IOException {
		callbacks.put("sdncChangeDelete", sdncAdapterDeleteCallback);
		callbacks.put("sdncDelete", sdncAdapterDeleteCallback);
		callbacks.put("vnfDelete", vnfAdapterDeleteCallback);
		callbacks.put("vnfDeleteFail", vnfAdapterDeleteCallbackFail);
	}

	@Test
	
	@Deployment(resources = {
			"subprocess/DoCreateVfModuleRollback.bpmn",
			"subprocess/PrepareUpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn",
			"subprocess/DeleteAAIVfModule.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void  TestCreateVfModuleRollbackSuccess() {
		logStart();

		mockSDNCAdapter("/SDNCAdapter", "SvcAction>delete", 200, "DeleteGenericVNFV1/sdncAdapterResponse.xml");
		mockVNFDelete("a27ce5a9-29c4-4c22-a017-6615ac73c721", "/973ed047-d251-4fb9-bf1a-65b8949e0a73", 202);
		MockDeleteGenericVnf("a27ce5a9-29c4-4c22-a017-6615ac73c721", "0000021", 200);
		MockDeleteVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73", "0000073", 200);
		MockPutVfModuleIdNoResponse("a27ce5a9-29c4-4c22-a017-6615ac73c721", "MMSC", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		MockPutGenericVnf("a27ce5a9-29c4-4c22-a017-6615ac73c721");
		MockGetGenericVnfByIdWithDepth("a27ce5a9-29c4-4c22-a017-6615ac73c721", 1, "DoCreateVfModuleRollback/GenericVnf.xml");
		MockGetVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73", "DoCreateVfModuleRollback/GenericVnfVfModule.xml", 200);
		MockPatchGenericVnf("a27ce5a9-29c4-4c22-a017-6615ac73c721");
		MockPatchVfModuleId("a27ce5a9-29c4-4c22-a017-6615ac73c721", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		RollbackData rollbackData = new RollbackData();
		rollbackData.put("VFMODULE", "source", "PORTAL");
		rollbackData.put("VFMODULE", "vnfid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		rollbackData.put("VFMODULE", "vnfname", "STMTN5MMSC21");
		rollbackData.put("VFMODULE", "vnftype", "asc_heat-int");
		rollbackData.put("VFMODULE", "vfmoduleid", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		rollbackData.put("VFMODULE", "vfmodulename", "STMTN5MMSC21-MMSC::module-0-0");
		rollbackData.put("VFMODULE", "tenantid", "fba1bd1e195a404cacb9ce17a9b2b421");
		rollbackData.put("VFMODULE", "aiccloudregion", "RDM2WAGPLCP");
		rollbackData.put("VFMODULE", "heatstackid", "thisisaheatstack");
		rollbackData.put("VFMODULE", "contrailNetworkPolicyFqdn0", "MSOTest:DefaultPolicyFQDN1");
		rollbackData.put("VFMODULE", "contrailNetworkPolicyFqdn1", "MSOTest:DefaultPolicyFQDN2");
		rollbackData.put("VFMODULE", "oamManagementV6Address", "2000:abc:bce:1111");
		rollbackData.put("VFMODULE", "oamManagementV4Address", "127.0.0.1");
		
		rollbackData.put("VFMODULE", "rollbackPrepareUpdateVfModule", "true");
		rollbackData.put("VFMODULE", "rollbackVnfAdapterCreate", "true");
		rollbackData.put("VFMODULE", "rollbackUpdateAAIVfModule", "true");
		rollbackData.put("VFMODULE", "rollbackSDNCRequestActivate", "true");
		rollbackData.put("VFMODULE", "rollbackCreateAAIVfModule", "true");
		rollbackData.put("VFMODULE", "rollbackCreateNetworkPoliciesAAI", "true");
		rollbackData.put("VFMODULE", "rollbackUpdateVnfAAI", "true");
		
	
		
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("mso-service-instance-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		rollbackData.put("VFMODULE", "msorequestid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		rollbackData.put("VFMODULE", "serviceinstanceid", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("rollbackData", rollbackData);
		variables.put("sdncVersion", "1702");
		invokeSubProcess("DoCreateVfModuleRollback", businessKey, variables);

		// "changedelete" operation not required for deleting a Vf Module
//		injectSDNCCallbacks(callbacks, "sdncChangeDelete");
		injectVNFRestCallbacks(callbacks, "vnfDelete");
		waitForRunningProcessCount("vnfAdapterDeleteV1", 0, 120000);
		injectSDNCCallbacks(callbacks, "sdncDelete");

		waitForProcessEnd(businessKey, 10000);
		WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
		checkVariable(businessKey, "WorkflowException", null);
		if (wfe != null) {
			System.out.println("TestCreateVfModuleSuccess: ErrorCode=" + wfe.getErrorCode() +
					", ErrorMessage=" + wfe.getErrorMessage());
		}
		logEnd();
	}

	
}

