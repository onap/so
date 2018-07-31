/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.bpmn.process;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;

@Ignore
public class WorkflowActionBBTest extends BaseBPMNTest {
	
	@Test
	public void sunnyDaySuccessIsTopLevelFlow() throws InterruptedException, IOException {
		variables.put("isTopLevelFlow", true);
		variables.put("completed", true);
		
		Map<String, String> map = new HashMap<>();
		map.put("handlingCode", "Success");
		mockSubprocess("ExecuteBuildingBlock", "Mocked ExecuteBuildingBlock", "GenericStub", map);
		mockSubprocess("CompleteMsoProcess", "Mocked CompleteMsoProcess", "GenericStub");
		
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("WorkflowActionBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_WorkflowActionBB", "Task_RetrieveBBExectuionList", "ExclusiveGateway_isTopLevelFlow", "Task_SendSync",
				"Task_SelectBB", "Call_ExecuteBB", "ExclusiveGateway_Finished", "ExclusiveGateway_isTopLevelFlowCompleted", "Task_SetupCompleteMsoProcess", "Call_CompleteMsoProcess",
				"End_WorkflowActionBB");
		assertThat(pi).isEnded();
	}

	@Test
	public void sunnyDaySuccessNotTopLevelFlow() throws InterruptedException, IOException {
		variables.put("isTopLevelFlow", false);
		variables.put("completed", true);

		Map<String, String> map = new HashMap<>();
		map.put("handlingCode", "Success");
		mockSubprocess("ExecuteBuildingBlock", "Mocked ExecuteBuildingBlock", "GenericStub", map);
		
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("WorkflowActionBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_WorkflowActionBB", "Task_RetrieveBBExectuionList", "ExclusiveGateway_isTopLevelFlow",
				"Task_SelectBB", "Call_ExecuteBB", "ExclusiveGateway_Finished", "ExclusiveGateway_isTopLevelFlowCompleted", "End_WorkflowActionBB");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void sunnyDayRollback() throws InterruptedException, IOException {
		variables.put("isTopLevelFlow", false);
		variables.put("isRollbackNeeded", false);

		Map<String, String> map = new HashMap<>();
		map.put("handlingCode", "Rollback");
		mockSubprocess("ExecuteBuildingBlock", "Mocked ExecuteBuildingBlock", "GenericStub", map);
		
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("WorkflowActionBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_WorkflowActionBB", "Task_RetrieveBBExectuionList", "ExclusiveGateway_isTopLevelFlow",
				"Task_SelectBB", "Call_ExecuteBB", "ExclusiveGateway_Finished", "Task_RollbackExecutionPath", "Task_UpdateRequestToFailed", "End_RollbackFailed");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void rainyDayAbort() throws Exception {
		variables.put("isTopLevelFlow", true);
		variables.put("completed", false);

		Map<String, String> map = new HashMap<>();
		map.put("handlingCode", "Abort");
		
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(workflowActionBBTasks).abortCallErrorHandling(any(DelegateExecution.class));
		mockSubprocess("ExecuteBuildingBlock", "Mocked ExecuteBuildingBlock", "GenericStub", map);
		
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("WorkflowActionBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_WorkflowActionBB", "Task_RetrieveBBExectuionList", "ExclusiveGateway_isTopLevelFlow", "Task_SendSync",
				"Task_SelectBB", "Call_ExecuteBB", "ExclusiveGateway_Finished", "ExclusiveGateway_isTopLevelFlowAbort", "Task_AbortAndCallErrorHandling", "ErrorStart",
				"Task_UpdateDb", "ErrorEnd");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void retrieveBBExecutionListerrorHandling() throws Exception {
		variables.put("isTopLevelFlow", true);
		variables.put("sentSyncResponse", false);
		doThrow(new IllegalStateException("TESTING ERRORS")).when(workflowAction).selectExecutionList(any(DelegateExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("WorkflowActionBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_WorkflowActionBB", "Task_RetrieveBBExectuionList", "StartEvent_runtimeError", "ServiceTask_HandleRuntimeError", "EndEvent__runtimeError", "SubProcess_0rze15o");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void errorCatchSubprocessHandlingTest() throws Exception {
		variables.put("isTopLevelFlow", true);
		variables.put("sentSyncResponse", false);
		doThrow(new IllegalStateException("TESTING ERRORS")).when(workflowAction).selectExecutionList(any(DelegateExecution.class));
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(workflowAction).handleRuntimeException(any(DelegateExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("WorkflowActionBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_WorkflowActionBB", "Task_RetrieveBBExectuionList", "StartEvent_runtimeError", "ServiceTask_HandleRuntimeError", "SubProcess_0fuugr9", "ErrorStart", "ExclusiveGateway_10q79b6", "Task_SendSyncAckError", "Task_UpdateDb", "ErrorEnd", "SubProcess_18226x4");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void errorCatchBpmnSubprocessHandlingTest() throws Exception {
		variables.put("isTopLevelFlow", true);
		variables.put("sentSyncResponse", false);
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(workflowActionBBTasks).selectBB(any(DelegateExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("WorkflowActionBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_WorkflowActionBB", "Task_RetrieveBBExectuionList","ExclusiveGateway_isTopLevelFlow","Task_SendSync","Task_SelectBB", "ErrorStart", "ExclusiveGateway_10q79b6", "Task_SendSyncAckError", "Task_UpdateDb", "ErrorEnd", "SubProcess_18226x4");
		assertThat(pi).isEnded();
	}
}
