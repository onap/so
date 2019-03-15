package org.onap.so.bpmn.infrastructure.manualhandling.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.db.request.beans.InfraActiveRequests;

public class ManualHandlingTasksTest extends BaseTaskTest {
	@InjectMocks
	protected ManualHandlingTasks manualHandlingTasks = new ManualHandlingTasks();	
	
	@Mock
	TaskService taskService;
	
	@Mock
	private DelegateExecution mockExecution;
	
	@Mock
	ProcessEngineServices processEngineServices;
	
	@Mock
	private DelegateTask task;
	
	private DelegateExecution delegateExecution;
	
	@Before
	public void before() throws Exception {
		delegateExecution = new DelegateExecutionFake();	
	}		
	
	@Test
	public void setFalloutTaskVariables_Test () {
		when(task.getId()).thenReturn("taskId");
		when(task.getExecution()).thenReturn(mockExecution);
		when(mockExecution.getProcessEngineServices()).thenReturn(processEngineServices);
		when(processEngineServices.getTaskService()).thenReturn(taskService);		
		manualHandlingTasks.setFalloutTaskVariables(task);
		verify(taskService, times(1)).setVariables(any(String.class), any(Map.class));		
	}
	
	@Test
	public void setPauseTaskVariables_Test () {
		when(task.getId()).thenReturn("taskId");
		when(task.getExecution()).thenReturn(mockExecution);
		when(mockExecution.getProcessEngineServices()).thenReturn(processEngineServices);
		when(processEngineServices.getTaskService()).thenReturn(taskService);		
		manualHandlingTasks.setPauseTaskVariables(task);
		verify(taskService, times(1)).setVariables(any(String.class), any(Map.class));		
	}
	
	@Test
	public void completeTask_Test() throws Exception{
		when(task.getId()).thenReturn("taskId");
		when(task.getExecution()).thenReturn(mockExecution);		
		Map<String, Object> taskVariables = new HashMap<String, Object>();
		taskVariables.put("responseValue", "resume");		
		when(mockExecution.getProcessEngineServices()).thenReturn(processEngineServices);
		when(processEngineServices.getTaskService()).thenReturn(taskService);
		when(taskService.getVariables(any(String.class))).thenReturn(taskVariables);
		manualHandlingTasks.completeTask(task);
		verify(mockExecution, times(1)).setVariable("responseValueTask", "Resume");
	}
		
	@Test
	public void updateRequestDbStatus_Test() throws Exception{
		InfraActiveRequests mockedRequest = new InfraActiveRequests();
		delegateExecution.setVariable("msoRequestId", "testMsoRequestId");
		when(requestsDbClient.getInfraActiveRequestbyRequestId(any(String.class))).thenReturn(mockedRequest);
		doNothing().when(requestsDbClient).updateInfraActiveRequests(any(InfraActiveRequests.class));
		manualHandlingTasks.updateRequestDbStatus(delegateExecution, "IN_PROGRESS");		
		verify(requestsDbClient, times(1)).updateInfraActiveRequests(any(InfraActiveRequests.class));
		assertEquals(mockedRequest.getRequestStatus(), "IN_PROGRESS");
	}
	
	@Test
	public void createExternalTicket_Test() throws Exception{
		delegateExecution.setVariable("msoRequestId", ("testMsoRequestId"));
		delegateExecution.setVariable("vnfType", "testVnfType");
		manualHandlingTasks.createExternalTicket(delegateExecution);	
	}
}
