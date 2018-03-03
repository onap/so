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

public class DoDeleteVfModuleVolumeV2Test extends WorkflowTest {

	private final CallbackSet callbacks = new CallbackSet();
	
	public DoDeleteVfModuleVolumeV2Test() throws IOException {
		callbacks.put("volumeGroupDelete", FileUtil.readResourceFile(
				"__files/DeleteVfModuleVolumeInfraV1/DeleteVfModuleVolumeCallbackResponse.xml"));
	}

	@Test
	//@Ignore 
	@Deployment(resources = {"subprocess/DoDeleteVfModuleVolumeV2.bpmn", "subprocess/VnfAdapterRestV1.bpmn"})
	public void happyPath() throws Exception {

		logStart();
		
		MockGetNetworkCloudRegion("MDTWNJ21", "CreateNetworkV2/cloudRegion30_AAIResponse_Success.xml");
		MockGetVolumeGroupById("RDM2WAGPLCP", "78987", "DeleteVfModuleVolumeInfraV1/queryVolumeId_AAIResponse_Success.xml");
		MockDeleteVolumeGroupById("RDM2WAGPLCP", "78987", "0000020", 200);
		mockPutVNFVolumeGroup("78987", 202);
		mockVfModuleDelete("78987");
		MockDeleteVolumeGroupById("AAIAIC25", "78987", "0000020", 200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockGetVolumeGroupById("AAIAIC25", "78987", "VfModularity/VolumeGroup.xml");
		String businessKey = UUID.randomUUID().toString();

		Map<String, Object> testVariables = new HashMap<>();
		testVariables.put("mso-request-id", "TEST-REQUEST-ID-0123");
		testVariables.put("msoRequestId", "TEST-REQUEST-ID-0123");
		testVariables.put("isDebugLogEnabled", "true");
		//testVariables.put("lcpCloudRegionId", "MDTWNJ21");
		//testVariables.put("tenantId", "fba1bd1e195a404cacb9ce17a9b2b421");
		testVariables.put("volumeGroupId", "78987");
		testVariables.put("serviceInstanceId", "test-service-instance-id-0123");
		
		String cloudConfiguration = "{" + 
				"\"lcpCloudRegionId\": \"MDTWNJ21\"," +		
				"\"tenantId\": \"fba1bd1e195a404cacb9ce17a9b2b421\"" + "}";
		testVariables.put("cloudConfiguration", cloudConfiguration);
		
		invokeSubProcess("DoDeleteVfModuleVolumeV2", businessKey, testVariables);

		injectVNFRestCallbacks(callbacks, "volumeGroupDelete");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "wasDeleted", "true");
		
		logEnd();
	}


	@Test
	//@Ignore 
	@Deployment(resources = {"subprocess/DoDeleteVfModuleVolumeV2.bpmn", "subprocess/VnfAdapterRestV1.bpmn"})
	public void testVolumeGroupInUse() throws Exception {

		logStart();
		MockGetNetworkCloudRegion("MDTWNJ21", "CreateNetworkV2/cloudRegion30_AAIResponse_Success.xml");
		MockGetVolumeGroupById("RDM2WAGPLCP", "78987", "DeleteVfModuleVolumeInfraV1/queryVolumeId_AAIResponse_HasVfModRelationship.xml");
		MockDeleteVolumeGroupById("RDM2WAGPLCP", "78987", "0000020", 200);
		mockVfModuleDelete("78987");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		String businessKey = UUID.randomUUID().toString();

		Map<String, Object> testVariables = new HashMap<>();
		testVariables.put("mso-request-id", "TEST-REQUEST-ID-0123");
		testVariables.put("msoRequestId", "TEST-REQUEST-ID-0123");
		testVariables.put("isDebugLogEnabled", "true");
		testVariables.put("volumeGroupId", "78987");
		testVariables.put("serviceInstanceId", "test-service-instance-id-0123");
		
		String cloudConfiguration = "{" + 
				"\"lcpCloudRegionId\": \"MDTWNJ21\"," +		
				"\"tenantId\": \"fba1bd1e195a404cacb9ce17a9b2b421\"" + "}";
		testVariables.put("cloudConfiguration", cloudConfiguration);
		
		invokeSubProcess("DoDeleteVfModuleVolumeV2", businessKey, testVariables);

		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "wasDeleted", "false");
		WorkflowException msoException = (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
		System.out.println("WorkflowException - Code: " + msoException.getErrorCode() + " Message: " + msoException.getErrorMessage());
		
		
		logEnd();
	}

	@Test
	//@Ignore 
	@Deployment(resources = {"subprocess/DoDeleteVfModuleVolumeV2.bpmn", "subprocess/VnfAdapterRestV1.bpmn"})
	public void testTenantIdMismatch() throws Exception {

		logStart();
		MockGetNetworkCloudRegion("MDTWNJ21", "CreateNetworkV2/cloudRegion30_AAIResponse_Success.xml");
		MockGetVolumeGroupById("RDM2WAGPLCP", "78987", "DeleteVfModuleVolumeInfraV1/queryVolumeId_AAIResponse_Success.xml");
		MockDeleteVolumeGroupById("RDM2WAGPLCP", "78987", "0000020", 200);
		mockVfModuleDelete("78987", 404);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		String businessKey = UUID.randomUUID().toString();

		Map<String, Object> testVariables = new HashMap<>();
		testVariables.put("mso-request-id", "TEST-REQUEST-ID-0123");
		testVariables.put("msoRequestId", "TEST-REQUEST-ID-0123");
		testVariables.put("isDebugLogEnabled", "true");
		testVariables.put("volumeGroupId", "78987");
		testVariables.put("serviceInstanceId", "test-service-instance-id-0123");
		
		String cloudConfiguration = "{" + 
				"\"lcpCloudRegionId\": \"MDTWNJ21\"," +		
				"\"tenantId\": \"fba1bd1e195a404cacb9ce17a9b2b421xxx\"" + "}";
		testVariables.put("cloudConfiguration", cloudConfiguration);
		
		invokeSubProcess("DoDeleteVfModuleVolumeV2", businessKey, testVariables);
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "wasDeleted", "false");
		WorkflowException msoException = (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
		System.out.println("WorkflowException - Code: " + msoException.getErrorCode() + " Message: " + msoException.getErrorMessage());
		
		
		logEnd();
	}
	
	@Test
	//@Ignore 
	@Deployment(resources = {"subprocess/DoDeleteVfModuleVolumeV2.bpmn", "subprocess/VnfAdapterRestV1.bpmn"})
	public void testVnfAdapterCallfail() throws Exception {

		logStart();
		MockGetNetworkCloudRegion("MDTWNJ21", "CreateNetworkV2/cloudRegion30_AAIResponse_Success.xml");
		MockGetVolumeGroupById("RDM2WAGPLCP", "78987", "DeleteVfModuleVolumeInfraV1/queryVolumeId_AAIResponse_Success.xml");
		MockDeleteVolumeGroupById("RDM2WAGPLCP", "78987", "0000020", 200);
		mockVfModuleDelete("78987", 404);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		String businessKey = UUID.randomUUID().toString();

		Map<String, Object> testVariables = new HashMap<>();
		testVariables.put("mso-request-id", "TEST-REQUEST-ID-0123");
		testVariables.put("msoRequestId", "TEST-REQUEST-ID-0123");
		testVariables.put("isDebugLogEnabled", "true");
		testVariables.put("volumeGroupId", "78987");
		testVariables.put("serviceInstanceId", "test-service-instance-id-0123");
		
		String cloudConfiguration = "{" + 
				"\"lcpCloudRegionId\": \"MDTWNJ21\"," +		
				"\"tenantId\": \"fba1bd1e195a404cacb9ce17a9b2b421\"" + "}";
		testVariables.put("cloudConfiguration", cloudConfiguration);
		
		invokeSubProcess("DoDeleteVfModuleVolumeV2", businessKey, testVariables);
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "wasDeleted", "false");
		WorkflowException msoException = (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
		System.out.println("WorkflowException - Code: " + msoException.getErrorCode() + " Message: " + msoException.getErrorMessage());
		
		logEnd();
	}
}
