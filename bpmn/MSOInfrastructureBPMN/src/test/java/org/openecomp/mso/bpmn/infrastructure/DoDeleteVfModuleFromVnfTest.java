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

public class DoDeleteVfModuleFromVnfTest extends WorkflowTest {
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
		"<output xmlns=\"com:att:sdnctl:l3api\">" + EOL +
		"  <svc-request-id>{{REQUEST-ID}}</svc-request-id>" + EOL +
		"  <ack-final-indicator>Y</ack-final-indicator>" + EOL +
		"</output>" + EOL;
	
	public DoDeleteVfModuleFromVnfTest() throws IOException {
		callbacks.put("deactivate", sdncAdapterDeleteCallback);
		callbacks.put("unassign", sdncAdapterDeleteCallback);
		callbacks.put("vnfDelete", vnfAdapterDeleteCallback);
		callbacks.put("vnfDeleteFail", vnfAdapterDeleteCallbackFail);
	}
	
	private final String wfeString = "WorkflowException";

	@Test
	@Deployment(resources = {
			"subprocess/DoDeleteVfModuleFromVnf.bpmn",			
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn",
			"subprocess/DeleteAAIVfModule.bpmn"
		})
	public void  TestDoDeleteVfModuleFromVnfSuccess() {
		// delete the Base Module and Generic Vnf
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
		String request =
			"<vnf-request xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">" + EOL +
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
			"  <vnf-params xmlns:tns=\"http://org.openecomp/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;
		logStart();
		MockDoDeleteVfModule_SDNCSuccess();
		MockDoDeleteVfModule_DeleteVNFSuccess();
		MockAAIGenericVnfSearch();	
		MockAAIDeleteVfModule();
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("msoRequestId", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("serviceInstanceId", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("vnfId", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		variables.put("vfModuleId", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
		variables.put("lcpCloudRegionId", "RDM2WAGPLCP");
		variables.put("tenantId", "fba1bd1e195a404cacb9ce17a9b2b421");
		variables.put("sdncVersion", "1707");
		
		invokeSubProcess("DoDeleteVfModuleFromVnf", businessKey, variables);

		injectSDNCCallbacks(callbacks, "deactivate");
		injectVNFRestCallbacks(callbacks, "vnfDelete");
		//waitForRunningProcessCount("vnfAdapterDeleteV1", 0, 120000);
		injectSDNCCallbacks(callbacks, "unassign");

		waitForProcessEnd(businessKey, 10000);
		WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, wfeString);
		checkVariable(businessKey, wfeString, null);
		if (wfe != null) {
			System.out.println("TestDoDeleteVfModuleSuccess: ErrorCode=" + wfe.getErrorCode() +
					", ErrorMessage=" + wfe.getErrorMessage());
		}
		logEnd();
	}

	
	// start of mocks used locally and by other VF Module unit tests
	

	
	public static void MockDoDeleteVfModule_SDNCSuccess() {
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				  .withRequestBody(containing("SvcAction>deactivate"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DeleteGenericVNFV1/sdncAdapterResponse.xml")));
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				  .withRequestBody(containing("SvcAction>unassign"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DeleteGenericVNFV1/sdncAdapterResponse.xml")));
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

	
}