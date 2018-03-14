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

package org.openecomp.mso.bpmn.common;

import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.MockGetServiceResourcesCatalogData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;

/**
 * Unit Test for the DecomposeService Flow
 *
 */
public class DecomposeServiceTest extends WorkflowTest {


	public DecomposeServiceTest() throws IOException {

	}

	@Test
	@Deployment(resources = {"subprocess/BuildingBlock/DecomposeService.bpmn"})
	public void testDecomposeService_success() throws Exception{
		MockGetServiceResourcesCatalogData("cmw-123-456-789", "1.0", "/getCatalogServiceResourcesDataWithConfig.json");

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId123", "ff5256d2-5a33-55df-13ab-12abad84e7ff");
		invokeSubProcess("DecomposeService", businessKey, variables);

		waitForProcessEnd(businessKey, 10000);

		Assert.assertTrue(isProcessEnded(businessKey));

	}
	
	//@Test
	@Deployment(resources = {"subprocess/BuildingBlock/DecomposeService.bpmn"})
	public void testDecomposeService_success_partial() throws Exception{
		MockGetServiceResourcesCatalogData("cmw-123-456-789", "/getCatalogServiceResourcesDataNoNetwork.json");


		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, "testRequestId123", "ff5256d2-5a33-55df-13ab-12abad84e7ff");
		invokeSubProcess("DecomposeService", businessKey, variables);

		waitForProcessEnd(businessKey, 10000);

		Assert.assertTrue(isProcessEnded(businessKey));

	}

	private void setVariablesSuccess(Map<String, Object> variables, String requestId, String siId) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("mso-request-id", requestId);
		variables.put("msoRequestId", requestId);
		variables.put("serviceInstanceId",siId);

		String serviceModelInfo = "{ "+ "\"modelType\": \"service\"," +
				"\"modelInvariantUuid\": \"cmw-123-456-789\"," +
				"\"modelVersionId\": \"ab6478e5-ea33-3346-ac12-ab121484a3fe\"," +
				"\"modelName\": \"ServicevSAMP12\"," +
				"\"modelVersion\": \"1.0\"," +
				"}";
		variables.put("serviceModelInfo", serviceModelInfo);

	}

}
