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
 * Unit test cases for UpdateVfModuleVolume.bpmn
 */
public class UpdateVfModuleVolumeInfraV1Test extends WorkflowTest {
	
	private final CallbackSet callbacks = new CallbackSet();

	public UpdateVfModuleVolumeInfraV1Test() throws IOException {
		callbacks.put("volumeGroupUpdate", FileUtil.readResourceFile(
			"__files/VfModularity/VNFAdapterRestVolumeGroupCallback.xml"));
	}

	/**
	 * Happy path scenario.
	 * 
	 * @throws Exception
	 */
	@Test
	@Deployment(resources = {
		"process/UpdateVfModuleVolumeInfraV1.bpmn",
		"subprocess/VnfAdapterRestV1.bpmn",
		"subprocess/CompleteMsoProcess.bpmn",
		"subprocess/GenericNotificationService.bpmn",
		"subprocess/FalloutHandler.bpmn"
		})
	public void happyPath() throws Exception {

		logStart();
		
		MockGetGenericVnfById("/TEST-VNF-ID-0123", "CreateVfModuleVolumeInfraV1/GenericVnf.xml", 200);
		MockGetVolumeGroupById("mdt1", "78987", "UpdateVfModuleVolumeInfraV1/queryVolumeId_AAIResponse_Success.xml");
		MockGetVfModuleId("9e48f6ea-f786-46de-800a-d480e5ccc846", "6a1dc898-b590-47b9-bbf0-34424a7a2ec3/", "UpdateVfModuleVolumeInfraV1/vf_module_aai_response.xml", 200);
		mockPutVNFVolumeGroup("78987", 202);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		String updaetVfModuleVolRequest =
			FileUtil.readResourceFile("__files/UpdateVfModuleVolumeInfraV1/updateVfModuleVolume_VID_request.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("serviceInstanceId", "test-service-instance-id");
		testVariables.put("volumeGroupId", "78987");
		testVariables.put("vnfId", "TEST-VNF-ID-0123");
		TestAsyncResponse asyncResponse = invokeAsyncProcess("UpdateVfModuleVolumeInfraV1",	"v1", businessKey, updaetVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		injectVNFRestCallbacks(callbacks, "volumeGroupUpdate");
		
		waitForProcessEnd(businessKey, 10000);
		checkVariable(businessKey, "UpdateVfModuleVolumeSuccessIndicator", true);
		
		logEnd();
	}
	
	/**
	 * VF Module Personal model id does not match request model invariant id
	 * @throws Exception
	 */
	@Test
	//@Ignore
	@Deployment(resources = {
		"process/UpdateVfModuleVolumeInfraV1.bpmn",
		"subprocess/VnfAdapterRestV1.bpmn",
		"subprocess/CompleteMsoProcess.bpmn",
		"subprocess/GenericNotificationService.bpmn",
		"subprocess/FalloutHandler.bpmn"
		})
	public void testPersonaModelIdNotMatch() throws Exception {

		logStart();
		
		MockGetVolumeGroupById("mdt1", "78987", "UpdateVfModuleVolumeInfraV1/queryVolumeId_AAIResponse_Success.xml");
		MockGetGenericVnfById("/TEST-VNF-ID-0123", "CreateVfModuleVolumeInfraV1/GenericVnf.xml", 200);
		MockGetVfModuleId("9e48f6ea-f786-46de-800a-d480e5ccc846", "6a1dc898-b590-47b9-bbf0-34424a7a2ec3/", "UpdateVfModuleVolumeInfraV1/vf_module_aai_response.xml", 200);
		mockPutVNFVolumeGroup("78987", 202);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		String updaetVfModuleVolRequest =
			FileUtil.readResourceFile("__files/UpdateVfModuleVolumeInfraV1/updateVfModuleVolume_VID_request_2.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("serviceInstanceId", "test-service-instance-id");
		testVariables.put("volumeGroupId", "78987");
		testVariables.put("vnfId", "TEST-VNF-ID-0123");
		TestAsyncResponse asyncResponse = invokeAsyncProcess("UpdateVfModuleVolumeInfraV1",	"v1", businessKey, updaetVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		injectVNFRestCallbacks(callbacks, "volumeGroupUpdate");
		
		waitForProcessEnd(businessKey, 10000);
		checkVariable(businessKey, "UpdateVfModuleVolumeSuccessIndicator", true);
		
		logEnd();
	}	
}
