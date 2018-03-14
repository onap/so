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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.runtime.Execution;

import static org.openecomp.mso.bpmn.common.BPMNUtil.executeWorkFlow;
import static org.openecomp.mso.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.openecomp.mso.bpmn.common.BPMNUtil.executeWorkFlow;
import static org.openecomp.mso.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
import static org.openecomp.mso.bpmn.mock.StubResponsePolicy.MockPolicyAbort;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;
import org.openecomp.mso.bpmn.core.WorkflowException;

/**
 * Unit test for RainyDayHandler.bpmn.
 */
public class ManualHandlingTest extends WorkflowTest {
	
	@Test	
	@Deployment(resources = {			
			"subprocess/BuildingBlock/ManualHandling.bpmn"
		})
	public void  TestManualHandlingSuccess() {

		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
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
		
		TaskService taskService = processEngineRule.getTaskService();
		
		TaskQuery q = taskService.createTaskQuery();		
	
		List<Task> tasks = q.orderByTaskCreateTime().asc().list();
		  int i = 0;
		  
		  for (Task task : tasks) {		  
			 
		    
		        System.out.println("TASK ID: " + task.getId());
		        System.out.println("TASK NAME: " + task.getName());
		        try {
		        	System.out.println("Completing the task");
		        	Map<String,Object> completeVariables = new HashMap<>();
		        	completeVariables.put("responseValue", "skip");
		        	taskService.complete(task.getId(), completeVariables);		        
		        }
		        catch(Exception e) {
		        	System.out.println("GOT EXCEPTION: " + e.getMessage());
		        }		        
		 	}	

		waitForProcessEnd(businessKey, 100000);

		Assert.assertTrue(isProcessEnded(businessKey));
		
	}
	
	
}