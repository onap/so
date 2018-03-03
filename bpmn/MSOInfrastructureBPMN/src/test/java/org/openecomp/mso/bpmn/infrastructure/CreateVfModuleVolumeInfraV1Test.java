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

public class CreateVfModuleVolumeInfraV1Test extends WorkflowTest {

	public static final String _prefix = "CVFMODVOL2_";
	
	private final CallbackSet callbacks = new CallbackSet();

	public CreateVfModuleVolumeInfraV1Test() throws IOException {
		callbacks.put("volumeGroupCreate", FileUtil.readResourceFile(
				"__files/CreateVfModuleVolumeInfraV1/CreateVfModuleVolumeCallbackResponse.xml"));
		callbacks.put("volumeGroupDelete", FileUtil.readResourceFile(
				"__files/DeleteVfModuleVolumeInfraV1/DeleteVfModuleVolumeCallbackResponse.xml"));
		callbacks.put("volumeGroupException", FileUtil.readResourceFile(
				"__files/CreateVfModuleVolumeInfraV1/CreateVfModuleCallbackException.xml"));
		callbacks.put("volumeGroupRollback", FileUtil.readResourceFile(
				"__files/CreateVfModuleVolumeInfraV1/RollbackVfModuleVolumeCallbackResponse.xml"));
	}
	
	/**
	 * Happy path scenario for VID
	 *****************************/
	@Test
	//@Ignore
	@Deployment(resources = {"process/CreateVfModuleVolumeInfraV1.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/DoCreateVfModuleVolumeV2.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestSuccess() throws Exception {

		logStart();
		
		MockNodeQueryServiceInstanceById("test-service-instance-id", "CreateVfModuleVolumeInfraV1/getSIUrlById.xml");
		MockGetGenericVnfById("/TEST-VNF-ID-0123", "CreateVfModuleVolumeInfraV1/GenericVnf.xml", 200);
		MockPutVolumeGroupById("AAIAIC25", "TEST-VOLUME-GROUP-ID-0123", "CreateVfModuleVolumeInfraV1/createVfModuleVolume_createVolumeName_AAIResponse_Success.xml", 201);
		MockGetVolumeGroupByName("AAIAIC25", "MSOTESTVOL101a-vSAMP12_base_vol_module-0", "CreateVfModuleVolumeInfraV1/createVfModuleVolume_queryVolumeName_AAIResponse_Success.xml", 200);
		MockPutVolumeGroupById("AAIAIC25", "8424bb3c-c3e7-4553-9662-469649ed9379", "CreateVfModuleVolumeInfraV1/createVfModuleVolume_updateVolumeName_AAIResponse_Success.xml", 200);
		mockPostVNFVolumeGroup(202);

		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/CreateVfModuleVolumeInfraV1/createVfModuleVolume_VID_request.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("serviceInstanceId", "test-service-instance-id");
		testVariables.put("vnfId", "TEST-VNF-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
		testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
				
		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVfModuleVolumeInfraV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 1000000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		injectVNFRestCallbacks(callbacks, "volumeGroupCreate");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "CVMVINFRAV1_SuccessIndicator", true);
		
		logEnd();
	}
	
	/**
	 * Fail - trigger rollback
	 *****************************/
	@Test
	//@Ignore
	@Deployment(resources = {"process/CreateVfModuleVolumeInfraV1.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/DoCreateVfModuleVolumeV2.bpmn",
			"subprocess/DoCreateVfModuleVolumeRollback.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestRollback() throws Exception {

		logStart();
		
		MockNodeQueryServiceInstanceById("test-service-instance-id", "CreateVfModuleVolumeInfraV1/getSIUrlById.xml");
		MockGetGenericVnfById("/TEST-VNF-ID-0123", "CreateVfModuleVolumeInfraV1/GenericVnf.xml", 200);
		MockPutVolumeGroupById("AAIAIC25", "TEST-VOLUME-GROUP-ID-0123", "CreateVfModuleVolumeInfraV1/createVfModuleVolume_createVolumeName_AAIResponse_Success.xml", 201);
		mockPostVNFVolumeGroup(202);
		mockPutVNFVolumeGroupRollback("TEST-VOLUME-GROUP-ID-0123", 202);
		MockDeleteVolumeGroupById("AAIAIC25", "8424bb3c-c3e7-4553-9662-469649ed9379", "1460134360", 202);
		StubResponseAAI.MockGetVolumeGroupByName_404("AAIAIC25", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
		StubResponseAAI.MockGetVolumeGroupByName("AAIAIC25", "MSOTESTVOL101a-vSAMP12_base_vol_module-0", "CreateVfModuleVolumeInfraV1/createVfModuleVolume_queryVolumeName_AAIResponse_Success.xml", 200);
		StubResponseAAI.MockDeleteVolumeGroup("AAIAIC25", "8424bb3c-c3e7-4553-9662-469649ed9379", "1460134360");
		
		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/CreateVfModuleVolumeInfraV1/createVfModuleVolume_VID_request.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("serviceInstanceId", "test-service-instance-id");
		testVariables.put("vnfId", "TEST-VNF-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
		testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
				
		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVfModuleVolumeInfraV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 1000000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		injectVNFRestCallbacks(callbacks, "volumeGroupCreate");
		injectVNFRestCallbacks(callbacks, "volumeGroupDelete");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "CVMVINFRAV1_SuccessIndicator", false);
		
		logEnd();
	}
	
	/**
	 * Happy path scenario for VID
	 *****************************/
	@Test
	@Ignore
	@Deployment(resources = {"process/CreateVfModuleVolumeInfraV1.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/DoCreateVfModuleVolumeV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestVolumeGroupAlreadyExists() throws Exception {

		logStart();
		
		MockGetVolumeGroupByName("AAIAIC25", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0", "CreateVfModuleVolumeInfraV1/createVfModuleVolume_queryVolumeName_AAIResponse_Success.xml", 200);
		MockGetGenericVnfById("TEST-VNF-ID-0123", "CreateVfModuleVolumeInfraV1/GenericVnf.xml", 200);
		MockNodeQueryServiceInstanceById("test-service-instance-id", "CreateVfModuleVolumeInfraV1/getSIUrlById.xml");

		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/CreateVfModuleVolumeInfraV1/createVfModuleVolume_VID_request.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("serviceInstanceId", "test-service-instance-id");
		testVariables.put("vnfId", "TEST-VNF-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
		testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
				
		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVfModuleVolumeInfraV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 1000000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		//injectVNFRestCallbacks(callbacks, "volumeGroupCreate");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "CVMVINFRAV1_SuccessIndicator", false);
		
		logEnd();
	}
	
	/**
	 *Vnf Create fail
	 *****************************/
	@Test
	@Ignore
	@Deployment(resources = {"process/CreateVfModuleVolumeInfraV1.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/DoCreateVfModuleVolumeV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestVNfCreateFail() throws Exception {

		logStart();
		
		MockNodeQueryServiceInstanceById("test-service-instance-id", "CreateVfModuleVolumeInfraV1/getSIUrlById.xml");
		MockGetGenericVnfById("/TEST-VNF-ID-0123", "CreateVfModuleVolumeInfraV1/GenericVnf.xml", 200);
		MockPutVolumeGroupById("AAIAIC25", "TEST-VOLUME-GROUP-ID-0123", "CreateVfModuleVolumeInfraV1/createVfModuleVolume_createVolumeName_AAIResponse_Success.xml", 201);
		MockGetVolumeGroupByName("AAIAIC25", "MSOTESTVOL101a-vSAMP12_base_vol_module-0", "CreateVfModuleVolumeInfraV1/createVfModuleVolume_queryVolumeName_AAIResponse_Success.xml", 200);
		MockPutVolumeGroupById("AAIAIC25", "8424bb3c-c3e7-4553-9662-469649ed9379", "CreateVfModuleVolumeInfraV1/createVfModuleVolume_updateVolumeName_AAIResponse_Success.xml", 200);
		mockPostVNFVolumeGroup(202);
		MockDeleteVolumeGroupById("AAIAIC25", "8424bb3c-c3e7-4553-9662-469649ed9379", "1460134360", 204);

		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/CreateVfModuleVolumeInfraV1/createVfModuleVolume_VID_request.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("serviceInstanceId", "test-service-instance-id");
		testVariables.put("vnfId", "TEST-VNF-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
		testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
				
		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVfModuleVolumeInfraV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 1000000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		injectVNFRestCallbacks(callbacks, "volumeGroupException");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "CVMVINFRAV1_SuccessIndicator", false);
		
		logEnd();
	}
	

	/**
	 * Error scenario - vnf not found
	 ********************************/
	@Test
	@Ignore
	@Deployment(resources = {"process/CreateVfModuleVolumeInfraV1.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/DoCreateVfModuleVolumeV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestFailVnfNotFound() throws Exception {

		logStart();
		
		MockNodeQueryServiceInstanceById("test-service-instance-id", "CreateVfModuleVolumeInfraV1/getSIUrlById.xml");

		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/CreateVfModuleVolumeInfraV1/createVfModuleVolume_VID_request_noreqparm.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("serviceInstanceId", "test-service-instance-id");
		testVariables.put("vnfId", "TEST-VNF-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
				
		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVfModuleVolumeInfraV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 1000000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		//injectVNFRestCallbacks(callbacks, "volumeGroupCreate");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "CVMVINFRAV1_SuccessIndicator", false);
		
		logEnd();
	}

	/**
	 * Error scenario - error in validation
	 **************************************/
	@Test
	@Ignore
	@Deployment(resources = {"process/CreateVfModuleVolumeInfraV1.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/DoCreateVfModuleVolumeV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestFailNoVnfPassed() throws Exception {

		logStart();
		
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/CreateVfModuleVolumeInfraV1/createVfModuleVolume_VID_request.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("serviceInstanceId", "test-service-instance-id");
		//testVariables.put("vnfId", "TEST-VNF-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
				
		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVfModuleVolumeInfraV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 1000000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "CVMVINFRAV1_SuccessIndicator", false);
		
		logEnd();
	}
	
	/**
	 * Error scenario - service instance not found
	 *********************************************/
	@Test
	@Ignore
	@Deployment(resources = {"process/CreateVfModuleVolumeInfraV1.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/DoCreateVfModuleVolumeV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestFailServiceInstanceNotFound() throws Exception {

		logStart();
		
		MockNodeQueryServiceInstanceById("test-service-instance-id", "CreateVfModuleVolumeInfraV1/getSIUrlById.xml");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/CreateVfModuleVolumeInfraV1/createVfModuleVolume_VID_request.json");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("requestId", "TEST-REQUEST-ID-0123");
		testVariables.put("serviceInstanceId", "test-service-instance-id");
		//testVariables.put("vnfId", "TEST-VNF-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
				
		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVfModuleVolumeInfraV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 1000000);

		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "CVMVINFRAV1_SuccessIndicator", false);
		
		logEnd();
	}
}
