package org.openecomp.mso.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.mso.bpmn.BaseBPMNTest;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatusValidationDirective;
import org.springframework.beans.factory.annotation.Autowired;

public class ExecuteBuildingBlockTest extends BaseBPMNTest {
	@Autowired
	private TaskService taskService;
	@Autowired
	private ManagementService managementService;
	
	@Test
	public void test_sunnyDayExecuteBuildingBlock_silentSuccess() throws Exception {
		variables.put("orchestrationStatusValidationResult", OrchestrationStatusValidationDirective.SILENT_SUCCESS);
		
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ExecuteBuildingBlock", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
			.hasPassedInOrder("Start_ExecuteBuildingBlock", "Task_BBInputSetup", "StatusPolicy", "CheckOrchestrationStatusValidationResults", "End_ExecuteBuildingBlock")
			.hasNotPassed("Call_BBToExecute", "ErrorStart", "Task_QueryRainyDayTable", "ExclusiveGateway_1aonzik", "ExclusiveGateway_1aonzik", "ErrorEnd2", "Task_SetRetryTimer");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void test_rainyDayExecuteBuildingBlock_rollbackOrAbort() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(bbInputSetup).execute(any(DelegateExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ExecuteBuildingBlock", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
			.hasPassedInOrder("Start_ExecuteBuildingBlock", "Task_BBInputSetup", "BoundaryEvent_0i3q236", "Task_QueryRainyDayTable", "ExclusiveGateway_1aonzik", "ErrorEnd2")
			.hasNotPassed("StatusPolicy", "CheckOrchestrationStatusValidationResults", "Call_BBToExecute", "End_ExecuteBuildingBlock", "ExclusiveGateway_0ey4zpt", "Task_SetRetryTimer");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void test_rainyDayExecuteBuildingBlock_retryNoRetriesLeft() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(bbInputSetup).execute(any(DelegateExecution.class));
		
		variables.put("handlingCode", "Retry");
		variables.put("RetryCount", 5);
		
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ExecuteBuildingBlock", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
			.hasPassedInOrder("Start_ExecuteBuildingBlock", "Task_BBInputSetup", "BoundaryEvent_0i3q236", "Task_QueryRainyDayTable", "ExclusiveGateway_1aonzik", "ExclusiveGateway_0ey4zpt", "ErrorEnd2")
			.hasNotPassed("StatusPolicy", "CheckOrchestrationStatusValidationResults", "Call_BBToExecute", "End_ExecuteBuildingBlock", "Task_SetRetryTimer");
		assertThat(pi).isEnded();
	}
	
	@Test
	@Ignore
	public void test_rainyDayExecuteBuildingBlock_retryRetriesLeft() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(bbInputSetup).execute(any(DelegateExecution.class));
		
		variables.put("handlingCode", "Retry");
		variables.put("RetryCount", 4);
		variables.put("RetryDuration", "PT1S");
		
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ExecuteBuildingBlock", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted();
		assertThat(pi).isWaitingAt("IntermediateCatchEvent_RetryTimer");
		Job job = managementService.createJobQuery().activityId("IntermediateCatchEvent_RetryTimer").singleResult();
		assertNotNull(job);
		managementService.executeJob(job.getId());
		assertThat(pi).isEnded()
			.hasPassedInOrder("Start_ExecuteBuildingBlock", "Task_BBInputSetup", "BoundaryEvent_0i3q236", "Task_QueryRainyDayTable", "ExclusiveGateway_1aonzik", "ExclusiveGateway_0ey4zpt", "Task_SetRetryTimer", "EndEvent_1sez2lh")
			.hasNotPassed("StatusPolicy", "CheckOrchestrationStatusValidationResults", "Call_BBToExecute", "End_ExecuteBuildingBlock", "ErrorEnd2");
	}
}