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

import static org.junit.Assert.assertEquals;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.MockGetServiceResourcesCatalogData;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.bpmn.mock.FileUtil;
/**
 * Unit test cases for DoCreateServiceInstanceV2.bpmn
 */
public class DoCreateServiceInstanceV2Test extends WorkflowTest {
	
	private final String input = FileUtil.readResourceFile("__files/CreateServiceInstance/DoCreateServiceInstanceInput.json");
	ServiceDecomposition serviceDecomposition = new ServiceDecomposition("{\"serviceResources\":{\"project\": {\"projectName\": \"projectName\"},\"owningEntity\": {\"owningEntityId\": \"id123\",\"owningEntityName\": \"name123\"}}}","abc123");

	public DoCreateServiceInstanceV2Test() throws IOException {
		
		
	}
		
	/**
	 * Sunny day VID scenario.
	 *
	 * @throws Exception
	 */
	@Ignore // 1802 merge
	@Test
	@Deployment(resources = {
			"subprocess/DoCreateServiceInstanceV2.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/CompleteMsoProcess.bpmn",
			"subprocess/DoCreateServiceInstanceRollback.bpmn",
			"subprocess/DoCreateServiceInstanceRollbackV2.bpmn",
			"subprocess/FalloutHandler.bpmn" })
	
	public void sunnyDay() throws Exception {

		logStart();
		
		//SDNC
		mockSDNCAdapter(200);
		//DB
		mockUpdateRequestDB(200, "DBUpdateResponse.xml");
		//Catalog DB
		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef","InfrastructureFlows/DoCreateServiceInstance_request.json");
		
		String businessKey = UUID.randomUUID().toString();

		Map<String, Object> variables =  new HashMap<String, Object>();
		setupVariables(variables);
		invokeSubProcess("DoCreateServiceInstanceV2", businessKey, variables);
		waitForProcessEnd(businessKey, 10000);
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DoCreateServiceInstanceV2", "WorkflowException");
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		
		logEnd();
	}

	// Success Scenario
	private void setupVariables(Map<String, Object> variables) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("msoRequestId", "RaaDSITestRequestId-1");
		variables.put("serviceInstanceId","RaaTest-si-id");
		//variables.put("serviceModelInfo", "{\"modelType\":\"service\",\"modelInvariantUuid\":\"uuid-miu-svc-011-abcdef\",\"modelVersionUuid\":\"ASDC_TOSCA_UUID\",\"modelName\":\"SIModelName1\",\"modelVersion\":\"2\",\"projectName\":\"proj123\",\"owningEntityId\":\"id123\",\"owningEntityName\":\"name123\"}");
		variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("globalSubscriberId", "MCBH-1610");
		variables.put("subscriptionServiceType", "viprsvc");
		variables.put("serviceInstanceName", "RAT-123");
		variables.put("sdncVersion", "1611");
		variables.put("serviceModelInfo", input);
		variables.put("serviceDecomposition", serviceDecomposition);
		variables.put("serviceType", "12e");
	}
}