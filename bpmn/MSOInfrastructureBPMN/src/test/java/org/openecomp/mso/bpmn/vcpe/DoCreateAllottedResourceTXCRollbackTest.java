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

/*
 * © 2014 AT&T Intellectual Property. All rights reserved. Used under license from AT&T Intellectual Property.
 */
package org.openecomp.mso.bpmn.vcpe;

import static org.junit.Assert.assertEquals;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.core.RollbackData;
import org.openecomp.mso.bpmn.mock.FileUtil;


public class DoCreateAllottedResourceTXCRollbackTest extends WorkflowTest {

	private static final String RbType = "DCARTXC_";
	private final CallbackSet callbacks = new CallbackSet();
	
	public DoCreateAllottedResourceTXCRollbackTest() throws IOException {
		callbacks.put("deactivate", FileUtil.readResourceFile("__files/VCPE/VfModularity/SDNCTopologyDeactivateCallback.xml"));
		callbacks.put("delete", FileUtil.readResourceFile("__files/VCPE/VfModularity/SDNCTopologyDeleteCallback.xml"));
		callbacks.put("unassign", FileUtil.readResourceFile("__files/VCPE/VfModularity/SDNCTopologyUnassignCallback.xml"));
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/FalloutHandler.bpmn",
			"subprocess/DoCreateAllottedResourceTXCRollback.bpmn"})
	public void testDoCreateAllottedResourceTXCRollback_success() throws Exception{
		
		MockGetAllottedResource("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "arId-1", "VCPE/DoCreateAllottedResourceTXCRollback/arGetById.xml");
		MockPatchAllottedResource("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "arId-1");
		MockDeleteAllottedResource("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "arId-1", "1490627351232");
		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId1");
		
		invokeSubProcess("DoCreateAllottedResourceTXCRollback", businessKey, variables);
		
		injectSDNCCallbacks(callbacks, "deactivate");
		injectSDNCCallbacks(callbacks, "delete");
		injectSDNCCallbacks(callbacks, "unassign");

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DoCreateAllottedResourceTXCRollback", "WorkflowException");
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
	}

	private void setVariablesSuccess(Map<String, Object> variables, String requestId) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("failNotFound", "true");
		variables.put("msoRequestId", requestId);
		variables.put("mso-request-id", "requestId");
		variables.put("allottedResourceId", "arId-1");
		
		variables.put("serviceInstanceId", "MIS%252F1604%252F0026%252FSW_INTERNET");
		variables.put("parentServiceInstanceId","MIS%252F1604%252F0026%252FSW_INTERNET");
		RollbackData rollbackData = new RollbackData();

		rollbackData.put(RbType, "serviceInstanceId", "MIS%252F1604%252F0026%252FSW_INTERNET");
		rollbackData.put(RbType, "serviceSubscriptionType", "123456789");
		rollbackData.put(RbType, "disablerollback", "false");
		rollbackData.put(RbType, "rollbackAAI", "true");
		rollbackData.put(RbType, "rollbackSDNCassign", "true");
		rollbackData.put(RbType, "rollbackSDNCactivate", "true");
		rollbackData.put(RbType, "rollbackSDNCcreate", "true");
		rollbackData.put(RbType, "aaiARPath", "http://localhost:28090/aai/v9/business/customers/customer/SDN-ETHERNET-INTERNET/service-subscriptions/service-subscription/123456789/service-instances/service-instance/MIS%252F1604%252F0026%252FSW_INTERNET/allotted-resources/allotted-resource/arId-1");
		
		rollbackData.put(RbType, "sdncActivateRollbackReq", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceTXCRollback/sdncActivateRollbackReq.xml"));
		rollbackData.put(RbType, "sdncCreateRollbackReq", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceTXCRollback/sdncCreateRollbackReq.xml")); 
		rollbackData.put(RbType, "sdncAssignRollbackReq", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceTXCRollback/sdncAssignRollbackReq.xml"));
		
		variables.put("rollbackData",rollbackData);
	}

}
