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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.core.env.Environment;

public class WorkflowActionBBTasksTest extends BaseTaskTest {

	@Mock
	protected WorkflowAction workflowAction;
	
	@Mock
	protected WorkflowActionBBFailure workflowActionBBFailure;
	
	@InjectMocks
	@Spy
	protected WorkflowActionBBTasks workflowActionBBTasks;
	
	@Mock
	InfraActiveRequests reqMock;
	
	private DelegateExecution execution;
	
	@Mock
	protected Environment environment;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void before() throws Exception {
		execution = new DelegateExecutionFake();
		org.onap.aai.domain.yang.ServiceInstance servInstance = new org.onap.aai.domain.yang.ServiceInstance();
		servInstance.setServiceInstanceId("TEST");
		when(bbSetupUtils.getAAIServiceInstanceByName(anyString(), anyObject())).thenReturn(servInstance);
		workflowAction.setBbInputSetupUtils(bbSetupUtils);
		workflowAction.setBbInputSetup(bbInputSetup);
	}
	
	@Test
	public void selectBBTest() throws Exception{
		String gAction = "Delete-Network-Collection";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		execution.setVariable("gCurrentSequence", 0);
		execution.setVariable("homing", false);
		execution.setVariable("calledHoming", false);
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
		ExecuteBuildingBlock ebb = new ExecuteBuildingBlock();
		flowsToExecute.add(ebb);
		execution.setVariable("flowsToExecute", flowsToExecute);
		workflowActionBBTasks.selectBB(execution);
		boolean success = (boolean) execution.getVariable("completed");
		assertEquals(true,success);
	}
	
	@Test
	public void select2BBTest() throws Exception{
		String gAction = "Delete-Network-Collection";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		execution.setVariable("gCurrentSequence", 0);
		execution.setVariable("homing", false);
		execution.setVariable("calledHoming", false);
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
		ExecuteBuildingBlock ebb = new ExecuteBuildingBlock();
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		flowsToExecute.add(ebb);
		flowsToExecute.add(ebb2);
		execution.setVariable("flowsToExecute", flowsToExecute);
		workflowActionBBTasks.selectBB(execution);
		boolean success = (boolean) execution.getVariable("completed");
		assertEquals(false,success);
	}
	
	@Test
	public void updateRequestStatusToCompleteTest() throws Exception{
		String reqId = "reqId123";
		execution.setVariable("mso-request-id", reqId);
		execution.setVariable("requestAction", "createInstance");
		execution.setVariable("resourceName", "Service");
		execution.setVariable("aLaCarte", true);
		InfraActiveRequests req = new InfraActiveRequests();
		doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
		doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
		workflowActionBBTasks.updateRequestStatusToComplete(execution);
		assertEquals("ALaCarte-Service-createInstance request was executed correctly.",execution.getVariable("finalStatusMessage"));
	}
	
	@Test
	public void updateRequestStatusToFailedFlowStatusTest() {
		String reqId = "reqId123";
		execution.setVariable("mso-request-id", reqId);
		execution.setVariable("isRollbackComplete", false);
		execution.setVariable("isRollback", false);
		ExecuteBuildingBlock ebb = new ExecuteBuildingBlock();
		BuildingBlock buildingBlock = new BuildingBlock();
		buildingBlock.setBpmnFlowName("CreateNetworkBB");
		ebb.setBuildingBlock(buildingBlock);
		execution.setVariable("buildingBlock", ebb);
		WorkflowException wfe = new WorkflowException("failure", 1, "failure");
		execution.setVariable("WorkflowException", wfe);
		InfraActiveRequests req = new InfraActiveRequests();
		doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
		doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
		workflowActionBBTasks.updateRequestStatusToFailed(execution);
		assertEquals("CreateNetworkBB has failed.",execution.getVariable("flowStatus"));
	}
	
	@Test
	public void rollbackExecutionPathTest(){
		execution.setVariable("handlingCode", "Rollback");
		execution.setVariable("isRollback", false);
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
		ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock();
		BuildingBlock bb1 = new BuildingBlock();
		bb1.setBpmnFlowName("AssignVfModuleBB");
		ebb1.setBuildingBlock(bb1);
		flowsToExecute.add(ebb1);
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		BuildingBlock bb2 = new BuildingBlock();
		bb2.setBpmnFlowName("CreateVfModuleBB");
		ebb2.setBuildingBlock(bb2);
		flowsToExecute.add(ebb2);
		ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock();
		BuildingBlock bb3 = new BuildingBlock();
		bb3.setBpmnFlowName("ActivateVfModuleBB");
		ebb3.setBuildingBlock(bb3);
		flowsToExecute.add(ebb3);
		
		execution.setVariable("flowsToExecute", flowsToExecute);
		execution.setVariable("gCurrentSequence", 3);
		doNothing().when(workflowActionBBFailure).updateRequestErrorStatusMessage(isA(DelegateExecution.class));
		
		workflowActionBBTasks.rollbackExecutionPath(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");
		assertEquals(0,execution.getVariable("gCurrentSequence"));
	}
	
	@Test
	public void rollbackExecutionPathUnfinishedFlowTest(){
		execution.setVariable("handlingCode", "Rollback");
		execution.setVariable("isRollback", false);
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
		ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock();
		BuildingBlock bb1 = new BuildingBlock();
		bb1.setBpmnFlowName("AssignVfModuleBB");
		ebb1.setBuildingBlock(bb1);
		flowsToExecute.add(ebb1);
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		BuildingBlock bb2 = new BuildingBlock();
		bb2.setBpmnFlowName("CreateVfModuleBB");
		ebb2.setBuildingBlock(bb2);
		flowsToExecute.add(ebb2);
		ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock();
		BuildingBlock bb3 = new BuildingBlock();
		bb3.setBpmnFlowName("ActivateVfModuleBB");
		ebb3.setBuildingBlock(bb3);
		flowsToExecute.add(ebb3);
		
		execution.setVariable("flowsToExecute", flowsToExecute);
		execution.setVariable("gCurrentSequence", 2);
		doNothing().when(workflowActionBBFailure).updateRequestErrorStatusMessage(isA(DelegateExecution.class));
		
		workflowActionBBTasks.rollbackExecutionPath(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");	
		assertEquals(0,execution.getVariable("gCurrentSequence"));
		assertEquals(0,execution.getVariable("retryCount"));
	}
	
	@Test
	public void rollbackExecutionTest(){
		execution.setVariable("handlingCode", "Rollback");
		execution.setVariable("isRollback", false);
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
		ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock();
		BuildingBlock bb1 = new BuildingBlock();
		bb1.setBpmnFlowName("AssignServiceInstanceBB");
		ebb1.setBuildingBlock(bb1);
		flowsToExecute.add(ebb1);
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		BuildingBlock bb2 = new BuildingBlock();
		bb2.setBpmnFlowName("CreateNetworkCollectionBB");
		ebb2.setBuildingBlock(bb2);
		flowsToExecute.add(ebb2);
		ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock();
		BuildingBlock bb3 = new BuildingBlock();
		bb3.setBpmnFlowName("AssignNetworkBB");
		ebb3.setBuildingBlock(bb3);
		flowsToExecute.add(ebb3);
		ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock();
		BuildingBlock bb4 = new BuildingBlock();
		bb4.setBpmnFlowName("CreateNetworkBB");
		ebb4.setBuildingBlock(bb4);
		flowsToExecute.add(ebb4);
		
		execution.setVariable("flowsToExecute", flowsToExecute);
		execution.setVariable("gCurrentSequence", 3);
		doNothing().when(workflowActionBBFailure).updateRequestErrorStatusMessage(isA(DelegateExecution.class));
		
		workflowActionBBTasks.rollbackExecutionPath(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"UnassignNetworkBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkCollectionBB");	
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"UnassignServiceInstanceBB");
		assertEquals(0,execution.getVariable("gCurrentSequence"));
	}
	
	@Test
	public void rollbackExecutionRollbackToAssignedTest(){
		execution.setVariable("isRollback", false);
		execution.setVariable("handlingCode", "RollbackToAssigned");
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
		ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock();
		BuildingBlock bb1 = new BuildingBlock();
		bb1.setBpmnFlowName("AssignVfModuleBB");
		ebb1.setBuildingBlock(bb1);
		flowsToExecute.add(ebb1);
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		BuildingBlock bb2 = new BuildingBlock();
		bb2.setBpmnFlowName("CreateVfModuleBB");
		ebb2.setBuildingBlock(bb2);
		flowsToExecute.add(ebb2);
		ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock();
		BuildingBlock bb3 = new BuildingBlock();
		bb3.setBpmnFlowName("ActivateVfModuleBB");
		ebb3.setBuildingBlock(bb3);
		flowsToExecute.add(ebb3);
		
		execution.setVariable("flowsToExecute", flowsToExecute);
		execution.setVariable("gCurrentSequence", 2);
		
		workflowActionBBTasks.rollbackExecutionPath(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals("DeleteVfModuleBB",ebbs.get(0).getBuildingBlock().getBpmnFlowName());
		assertEquals(0,execution.getVariable("gCurrentSequence"));
		assertEquals(1,ebbs.size());
	}
	
	@Test
	public void checkRetryStatusTest(){
		String reqId = "reqId123";
		execution.setVariable("mso-request-id", reqId);
		doNothing().when(workflowActionBBFailure).updateRequestErrorStatusMessage(isA(DelegateExecution.class));
		doReturn("6").when(environment).getProperty("mso.rainyDay.maxRetries");
		execution.setVariable("handlingCode","Retry");
		execution.setVariable("retryCount", 1);
		execution.setVariable("gCurrentSequence",1);
		InfraActiveRequests req = new InfraActiveRequests();
		doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
		workflowActionBBTasks.checkRetryStatus(execution);
		assertEquals(0,execution.getVariable("gCurrentSequence"));
	}
	
	@Test
	public void checkRetryStatusTestExceededMaxRetries(){
		String reqId = "reqId123";
		execution.setVariable("mso-request-id", reqId);
		doNothing().when(workflowActionBBFailure).updateRequestErrorStatusMessage(isA(DelegateExecution.class));
		doReturn("6").when(environment).getProperty("mso.rainyDay.maxRetries");
		execution.setVariable("handlingCode","Retry");
		execution.setVariable("retryCount", 6);
		execution.setVariable("gCurrentSequence",1);
		InfraActiveRequests req = new InfraActiveRequests();
		doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
		try{
			workflowActionBBTasks.checkRetryStatus(execution);
		} catch (BpmnError e) {
			WorkflowException exception = (WorkflowException) execution.getVariable("WorkflowException");
			assertEquals("Exceeded maximum retries. Ending flow with status Abort",exception.getErrorMessage());
		}
	}
	
	@Test
	public void checkRetryStatusNoRetryTest(){
		String reqId = "reqId123";
		execution.setVariable("mso-request-id", reqId);
		execution.setVariable("retryCount", 3);
		execution.setVariable("handlingCode","Success");
		execution.setVariable("gCurrentSequence",1);
		InfraActiveRequests req = new InfraActiveRequests();
		doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
		workflowActionBBTasks.checkRetryStatus(execution);
		assertEquals(0,execution.getVariable("retryCount"));
	}
}
