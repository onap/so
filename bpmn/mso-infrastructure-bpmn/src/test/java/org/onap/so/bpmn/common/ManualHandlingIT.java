/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.common;

import static org.onap.so.bpmn.mock.StubResponseDatabase.MockPostRequestDB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for RainyDayHandler.bpmn.
 */
public class ManualHandlingIT extends BaseIntegrationTest {
	Logger logger = LoggerFactory.getLogger(ManualHandlingIT.class);
	
	@Test
	public void  TestManualHandlingSuccess() {
		MockPostRequestDB();
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("msoRequestId", "testRequestId");
		variables.put("serviceType", "X");
		variables.put("vnfType", "Y");
		variables.put("currentActivity", "BB1");
		variables.put("workStep", "1");
		variables.put("failedActivity", "AAI");
		variables.put("vnfName", "vSAMP12");
		variables.put("errorCode", "123");
		variables.put("errorText", "update failed");
		variables.put("validResponses", "Rollback");
		variables.put("vnfName", "vSAMP1");


		String businessKey = UUID.randomUUID().toString();
		invokeSubProcess("ManualHandling", businessKey, variables);
		
		try {
			Thread.sleep(5);
		} catch (Exception e) {

		}

		TaskService taskService = processEngine.getTaskService();

		TaskQuery q = taskService.createTaskQuery();

		List<Task> tasks = q.orderByTaskCreateTime().asc().list();
		
		for (Task task : tasks) {
			logger.debug("TASK ID: {}", task.getId());
			logger.debug("TASK NAME: {}", task.getName());
			
			try {
				logger.debug("Completing the task");
				Map<String,Object> completeVariables = new HashMap<>();
				completeVariables.put("responseValue", "skip");
				taskService.complete(task.getId(), completeVariables);
			} catch(Exception e) {
				logger.debug("GOT EXCEPTION: {}", e.getMessage());
			}
		}
		
		waitForProcessEnd(businessKey, 100000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
	}
}
