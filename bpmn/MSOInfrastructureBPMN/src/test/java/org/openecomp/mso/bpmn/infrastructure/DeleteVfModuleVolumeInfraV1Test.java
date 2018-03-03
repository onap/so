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
public class DeleteVfModuleVolumeInfraV1Test extends WorkflowTest {
	
	private final CallbackSet callbacks = new CallbackSet();

	public DeleteVfModuleVolumeInfraV1Test() throws IOException {
		callbacks.put("volumeGroupDelete", FileUtil.readResourceFile(
				"__files/DeleteVfModuleVolumeInfraV1/DeleteVfModuleVolumeCallbackResponse.xml"));
	}

	/**
	 * Happy path scenario.
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore // BROKEN TEST
	@Deployment(resources = {"process/DeleteVfModuleVolumeInfraV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void happyPath() throws Exception {

		logStart();
		
//		DeleteVfModuleVolumeInfraV1_success();
		
		String businessKey = UUID.randomUUID().toString();
		String deleteVfModuleVolRequest =
			FileUtil.readResourceFile("__files/DeleteVfModuleVolumeInfraV1/deleteVfModuleVolume_VID_request_st.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("volumeGroupId", "78987");
		testVariables.put("serviceInstanceId", "test-service-instance-id-0123");
		
		TestAsyncResponse asyncResponse = invokeAsyncProcess("DeleteVfModuleVolumeInfraV1",
			"v1", businessKey, deleteVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 100000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		injectVNFRestCallbacks(callbacks, "volumeGroupDelete");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "DELVfModVol_TransactionSuccessIndicator", true);
		
		logEnd();
	}

	/**
	 * Test fails - vf module in use
	 * 
	 * @throws Exception
	 */
	@Test
	@Deployment(resources = {"process/DeleteVfModuleVolumeInfraV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestVfModuleInUseError() throws Exception {

		logStart();
		
//		DeleteVfModuleVolumeInfraV1_inUseError(); // no assertions to check
		
		String businessKey = UUID.randomUUID().toString();
		String deleteVfModuleVolRequest =
			FileUtil.readResourceFile("__files/DeleteVfModuleVolumeInfraV1/deleteVfModuleVolume_VID_request_st.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("volumeGroupId", "78987");
		
		TestAsyncResponse asyncResponse = invokeAsyncProcess("DeleteVfModuleVolumeInfraV1",
			"v1", businessKey, deleteVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 100000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		//injectVNFRestCallbacks(callbacks, "volumeGroupDelete");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "DELVfModVol_TransactionSuccessIndicator", false);
		
		logEnd();
	}
	
	/**
	 * Test fails on vnf adapter call
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore // BROKEN TEST
	@Deployment(resources = {"process/DeleteVfModuleVolumeInfraV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestVnfAdapterCallfail() throws Exception {

		logStart();
		
//		DeleteVfModuleVolumeInfraV1_fail();
		
		String businessKey = UUID.randomUUID().toString();
		String deleteVfModuleVolRequest =
			FileUtil.readResourceFile("__files/DeleteVfModuleVolumeInfraV1/deleteVfModuleVolume_VID_request_st.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("volumeGroupId", "78987");
		
		TestAsyncResponse asyncResponse = invokeAsyncProcess("DeleteVfModuleVolumeInfraV1",
			"v1", businessKey, deleteVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 100000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		//injectVNFRestCallbacks(callbacks, "volumeGroupDelete");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "DELVfModVol_TransactionSuccessIndicator", false);
		
		logEnd();
	}
}
