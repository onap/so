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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test cases for AssignServiceInstanceTest.bpmn
 */
@Ignore
public class AssignServiceInstanceTest extends WorkflowTest {

	ObjectMapper mapper = new ObjectMapper();

	public AssignServiceInstanceTest() throws IOException {
	}

	@Test
	@Deployment(resources = { "subprocess/AssignServiceInstanceBB.bpmn",
							  "subprocess/AssignServiceInstanceRollbackBB.bpmn"
							})
	public void sunnyDay() throws Exception {
		logStart();
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<String, Object>();
		setupVariables(variables);
		invokeSubProcess("AssignServiceInstanceBB", businessKey, variables);
		waitForProcessEnd(businessKey, 10000);
		Assert.assertTrue(isProcessEnded(businessKey));
		logEnd();
	}

	// Success Scenario
	private void setupVariables(Map<String, Object> variables) {
		variables.put("mso-request-id", "213");
	}
}
