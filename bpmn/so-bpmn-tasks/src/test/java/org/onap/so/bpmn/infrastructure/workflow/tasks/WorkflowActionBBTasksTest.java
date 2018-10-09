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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkflowActionBBTasksTest extends BaseTaskTest {

	@Autowired
	protected WorkflowAction workflowAction;
	
	@Autowired
	protected WorkflowActionBBTasks workflowActionBBTasks;
	
	private DelegateExecution execution;
	
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
	public void getUpdatedRequestTest() throws Exception{
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
		ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock();
		BuildingBlock bb1 = new BuildingBlock();
		bb1.setBpmnFlowName("CreateNetworkBB");
		flowsToExecute.add(ebb1);
		ebb1.setBuildingBlock(bb1);
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		BuildingBlock bb2 = new BuildingBlock();
		bb2.setBpmnFlowName("ActivateNetworkBB");
		flowsToExecute.add(ebb2);
		ebb2.setBuildingBlock(bb2);
		String requestId = "requestId";
		execution.setVariable("mso-request-id", requestId);
		execution.setVariable("flowsToExecute", flowsToExecute);
		int currentSequence = 2;
		String expectedStatusMessage = "Execution of CreateNetworkBB has completed successfully, next invoking ActivateNetworkBB (Execution Path progress: BBs completed = 1; BBs remaining = 1).";
		Long expectedLong = new Long(52);
		InfraActiveRequests mockedRequest = new InfraActiveRequests();
		when(requestsDbClient.getInfraActiveRequestbyRequestId(requestId)).thenReturn(mockedRequest);
		InfraActiveRequests actual = workflowActionBBTasks.getUpdatedRequest(execution, currentSequence);
		assertEquals(expectedStatusMessage, actual.getStatusMessage());
		assertEquals(expectedLong, actual.getProgress());
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
	public void msoCompleteProcessTest() throws Exception{
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", "createInstance");
		execution.setVariable("resourceId", "123");
		execution.setVariable("source","MSO");
		execution.setVariable("resourceName", "Service");
		execution.setVariable("aLaCarte", true);
		workflowActionBBTasks.setupCompleteMsoProcess(execution);
		String response = (String) execution.getVariable("CompleteMsoProcessRequest");
		assertEquals(response,"<aetgt:MsoCompletionRequest xmlns:aetgt=\"http://org.onap/so/workflow/schema/v1\" xmlns:ns=\"http://org.onap/so/request/types/v1\"><request-info xmlns=\"http://org.onap/so/infra/vnf-request/v1\"><request-id>00f704ca-c5e5-4f95-a72c-6889db7b0688</request-id><action>createInstance</action><source>MSO</source></request-info><status-message>ALaCarte-Service-createInstance request was executed correctly.</status-message><serviceInstanceId>123</serviceInstanceId><mso-bpel-name>WorkflowActionBB</mso-bpel-name></aetgt:MsoCompletionRequest>");
	}
	
	@Test
	public void setupFalloutHandlerTest(){
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("serviceInstanceId", "123");
		execution.setVariable("WorkflowActionErrorMessage", "Error in WorkFlowAction");
		execution.setVariable("requestAction", "createInstance");
		workflowActionBBTasks.setupFalloutHandler(execution);
		assertEquals(execution.getVariable("falloutRequest"),"<aetgt:FalloutHandlerRequest xmlns:aetgt=\"http://org.onap/so/workflow/schema/v1\"xmlns:ns=\"http://org.onap/so/request/types/v1\"xmlns:wfsch=\"http://org.onap/so/workflow/schema/v1\"><request-info xmlns=\"http://org.onap/so/infra/vnf-request/v1\"><request-id>00f704ca-c5e5-4f95-a72c-6889db7b0688</request-id><action>createInstance</action><source>VID</source></request-info><aetgt:WorkflowException xmlns:aetgt=\"http://org.onap/so/workflow/schema/v1\"><aetgt:ErrorMessage>Error in WorkFlowAction</aetgt:ErrorMessage><aetgt:ErrorCode>7000</aetgt:ErrorCode></aetgt:WorkflowException></aetgt:FalloutHandlerRequest>");
	}
	
	@Test
	public void rollbackExecutionPathTest(){
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
		
		workflowActionBBTasks.rollbackExecutionPath(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");
		assertEquals(0,execution.getVariable("gCurrentSequence"));
	}
	
	@Test
	public void rollbackExecutionPathUnfinishedFlowTest(){
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
		
		workflowActionBBTasks.rollbackExecutionPath(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");	
		assertEquals(0,execution.getVariable("gCurrentSequence"));
	}
	
	@Test
	public void rollbackExecutionTest(){
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
		
		workflowActionBBTasks.rollbackExecutionPath(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"UnassignNetworkBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkCollectionBB");	
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"UnassignServiceInstanceBB");
		assertEquals(0,execution.getVariable("gCurrentSequence"));
	}
	
	@Test
	public void checkRetryStatusTest(){
		execution.setVariable("handlingCode","Retry");
		execution.setVariable("retryCount", 1);
		execution.setVariable("gCurrentSequence",1);
		workflowActionBBTasks.checkRetryStatus(execution);
		assertEquals(0,execution.getVariable("gCurrentSequence"));
	}
	
	@Test
	public void checkRetryStatusNoRetryTest(){
		execution.setVariable("retryCount", 3);
		execution.setVariable("handlingCode","Success");
		execution.setVariable("gCurrentSequence",1);
		workflowActionBBTasks.checkRetryStatus(execution);
		assertEquals(0,execution.getVariable("retryCount"));
	}
}
