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

package org.openecomp.mso.bpmn.infrastructure.integrationtests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.mock.FileUtil;

@Ignore
public class SetupServiceDecompTest extends WorkflowTest {

	private String input = FileUtil.readResourceFile("__files/CreateServiceInstance/SetupServiceDecompJson.json");
	
	public SetupServiceDecompTest() throws IOException {
	}
	
	@BeforeClass
	public static void setUp(){
		System.setProperty("javax.net.ssl.keyStore", "C:/etc/ecomp/mso/config/msoClientKeyStore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "mso4you");
		System.setProperty("javax.net.ssl.trustStore", "C:/etc/ecomp/mso/config/msoTrustStore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "mso_Domain2.0_4you");
	}
	
	@Test
	@Deployment(resources = {
			"subprocess/CreateServiceInstanceV3.bpmn",
			"subprocess/CreateServiceInstanceV3Rollback.bpmn",
			"subprocess/SetupBBInput.bpmn"
			})
	public void sunnyDay() throws Exception {

		logStart();

		String businessKey = UUID.randomUUID().toString();

		Map<String, Object> variables = new HashMap<String, Object>();
		setupVariables(variables);
		invokeSubProcess("SetupBBInput", businessKey, variables);
		waitForProcessEnd(businessKey, 10000);
		logEnd();
	}

	// Success Scenario
	private void setupVariables(Map<String, Object> variables) {
		variables.put("msoRequestId", "RaaDSITestRequestId-1");
		variables.put("flow", "CreateServiceInstanceV3");
		variables.put("serviceInstanceId", "MSORefactorTest7");
		variables.put("bpmnRequest", "src/main/resources/__files/requestDetails.json");
	}
}