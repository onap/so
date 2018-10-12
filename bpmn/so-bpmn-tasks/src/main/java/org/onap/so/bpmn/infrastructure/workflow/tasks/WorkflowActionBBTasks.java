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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.common.workflow.context.WorkflowCallbackResponse;
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkflowActionBBTasks {

	private static final String G_CURRENT_SEQUENCE = "gCurrentSequence";
	private static final String G_REQUEST_ID = "mso-request-id";
	private static final String G_ALACARTE = "aLaCarte";
	private static final String G_ACTION = "requestAction";
	private static final String RETRY_COUNT = "retryCount";
	private static final Logger logger = LoggerFactory.getLogger(WorkflowActionBBTasks.class);

	@Autowired
	private RequestsDbClient requestDbclient;
	@Autowired
	private WorkflowAction workflowAction;
	
	public void selectBB(DelegateExecution execution) {
		List<ExecuteBuildingBlock> flowsToExecute = (List<ExecuteBuildingBlock>) execution
				.getVariable("flowsToExecute");
		execution.setVariable("MacroRollback", false);
		int currentSequence = (int) execution.getVariable(G_CURRENT_SEQUENCE);
		ExecuteBuildingBlock ebb = flowsToExecute.get(currentSequence);
		boolean homing = (boolean) execution.getVariable("homing");
		boolean calledHoming = (boolean) execution.getVariable("calledHoming");
		if (homing && !calledHoming) {
			if (ebb.getBuildingBlock().getBpmnFlowName().equals("AssignVnfBB")) {
				ebb.setHoming(true);
				execution.setVariable("calledHoming", true);
			}
		} else {
			ebb.setHoming(false);
		}
		execution.setVariable("buildingBlock", ebb);
		currentSequence++;
		if (currentSequence >= flowsToExecute.size()) {
			execution.setVariable("completed", true);
		} else {
			execution.setVariable("completed", false);
			execution.setVariable(G_CURRENT_SEQUENCE, currentSequence);
		}
	}
	
	public void updateFlowStatistics(DelegateExecution execution) {
		try{
			int currentSequence = (int) execution.getVariable(G_CURRENT_SEQUENCE);
			if(currentSequence > 1) {
				InfraActiveRequests request = this.getUpdatedRequest(execution, currentSequence);
				requestDbclient.updateInfraActiveRequests(request);
			}
		}catch (Exception ex){
			logger.warn("Bpmn Flow Statistics was unable to update Request Db with the new completion percentage. Competion percentage may be invalid.");
		}
	}

	protected InfraActiveRequests getUpdatedRequest(DelegateExecution execution, int currentSequence) {
		List<ExecuteBuildingBlock> flowsToExecute = (List<ExecuteBuildingBlock>) execution
				.getVariable("flowsToExecute");
		String requestId = (String) execution.getVariable(G_REQUEST_ID);
		InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
		ExecuteBuildingBlock completedBB = flowsToExecute.get(currentSequence - 2);
		ExecuteBuildingBlock nextBB = flowsToExecute.get(currentSequence - 1);
		int completedBBs = currentSequence - 1;
		int totalBBs = flowsToExecute.size();
		int remainingBBs = totalBBs - completedBBs;
		String statusMessage = this.getStatusMessage(completedBB.getBuildingBlock().getBpmnFlowName(), 
				nextBB.getBuildingBlock().getBpmnFlowName(), completedBBs, remainingBBs);
		Long percentProgress = this.getPercentProgress(completedBBs, totalBBs);
		request.setStatusMessage(statusMessage);
		request.setProgress(percentProgress);
		request.setLastModifiedBy("CamundaBPMN");
		return request;
	}
	
	protected Long getPercentProgress(int completedBBs, int totalBBs) {
		double ratio = (completedBBs / (totalBBs * 1.0));
		int percentProgress = (int) (ratio * 95);
		return new Long(percentProgress + 5);
	}
	
	protected String getStatusMessage(String completedBB, String nextBB, int completedBBs, int remainingBBs) {
		return "Execution of " + completedBB + " has completed successfully, next invoking " + nextBB
				+ " (Execution Path progress: BBs completed = " + completedBBs + "; BBs remaining = " + remainingBBs
				+ ").";
	}

	public void sendSyncAck(DelegateExecution execution) {
		final String requestId = (String) execution.getVariable(G_REQUEST_ID);
		final String resourceId = (String) execution.getVariable("resourceId");
		ServiceInstancesResponse serviceInstancesResponse = new ServiceInstancesResponse();
		RequestReferences requestRef = new RequestReferences();
		requestRef.setInstanceId(resourceId);
		requestRef.setRequestId(requestId);
		serviceInstancesResponse.setRequestReferences(requestRef);
		ObjectMapper mapper = new ObjectMapper();
		String jsonRequest = "";
		try {
			jsonRequest = mapper.writeValueAsString(serviceInstancesResponse);
		} catch (JsonProcessingException e) {
			workflowAction.buildAndThrowException(execution,
					"Could not marshall ServiceInstancesRequest to Json string to respond to API Handler.", e);
		}
		WorkflowCallbackResponse callbackResponse = new WorkflowCallbackResponse();
		callbackResponse.setStatusCode(200);
		callbackResponse.setMessage("Success");
		callbackResponse.setResponse(jsonRequest);
		String processKey = execution.getProcessEngineServices().getRepositoryService()
				.getProcessDefinition(execution.getProcessDefinitionId()).getKey();
		WorkflowContextHolder.getInstance().processCallback(processKey, execution.getProcessInstanceId(), requestId,
				callbackResponse);
		logger.info("Successfully sent sync ack.");
	}

	public void sendErrorSyncAck(DelegateExecution execution) {
		final String requestId = (String) execution.getVariable(G_REQUEST_ID);
		try {
			ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
			String errorMsg = (String) execution.getVariable("WorkflowActionErrorMessage");
			if (errorMsg == null) {
				errorMsg = "WorkflowAction failed unexpectedly.";
			}
			String processKey = exceptionBuilder.getProcessKey(execution);
			String buildworkflowException = "<aetgt:WorkflowException xmlns:aetgt=\"http://org.onap/so/workflow/schema/v1\"><aetgt:ErrorMessage>"
					+ errorMsg
					+ "</aetgt:ErrorMessage><aetgt:ErrorCode>7000</aetgt:ErrorCode></aetgt:WorkflowException>";
			WorkflowCallbackResponse callbackResponse = new WorkflowCallbackResponse();
			callbackResponse.setStatusCode(500);
			callbackResponse.setMessage("Fail");
			callbackResponse.setResponse(buildworkflowException);
			WorkflowContextHolder.getInstance().processCallback(processKey, execution.getProcessInstanceId(), requestId,
					callbackResponse);
			execution.setVariable("sentSyncResponse", true);
		} catch (Exception ex) {
			logger.error(" Sending Sync Error Activity Failed. {}"  , ex.getMessage(), ex);
		}
	}

	public void setupCompleteMsoProcess(DelegateExecution execution) {
		final String requestId = (String) execution.getVariable(G_REQUEST_ID);
		final String action = (String) execution.getVariable(G_ACTION);
		final String resourceId = (String) execution.getVariable("resourceId");
		final boolean aLaCarte = (boolean) execution.getVariable(G_ALACARTE);
		final String resourceName = (String) execution.getVariable("resourceName");
		final String source = (String) execution.getVariable("source");
		String macroAction = "";
		if (aLaCarte) {
			macroAction = "ALaCarte-" + resourceName + "-" + action;
		} else {
			macroAction = "Macro-" + resourceName + "-" + action;
		}
		String msoCompletionRequest = "<aetgt:MsoCompletionRequest xmlns:aetgt=\"http://org.onap/so/workflow/schema/v1\" xmlns:ns=\"http://org.onap/so/request/types/v1\"><request-info xmlns=\"http://org.onap/so/infra/vnf-request/v1\"><request-id>"
				+ requestId + "</request-id><action>" + action + "</action><source>" + source
				+ "</source></request-info><status-message>" + macroAction
				+ " request was executed correctly.</status-message><serviceInstanceId>" + resourceId
				+ "</serviceInstanceId><mso-bpel-name>WorkflowActionBB</mso-bpel-name></aetgt:MsoCompletionRequest>";
		execution.setVariable("CompleteMsoProcessRequest", msoCompletionRequest);
		execution.setVariable("mso-request-id", requestId);
		execution.setVariable("mso-service-instance-id", resourceId);
	}

	public void setupFalloutHandler(DelegateExecution execution) {
		final String requestId = (String) execution.getVariable(G_REQUEST_ID);
		final String action = (String) execution.getVariable(G_ACTION);
		final String resourceId = (String) execution.getVariable("resourceId");
		String exceptionMsg = "";
		if (execution.getVariable("WorkflowActionErrorMessage") != null) {
			exceptionMsg = (String) execution.getVariable("WorkflowActionErrorMessage");
		} else {
			exceptionMsg = "Error in WorkflowAction";
		}
		execution.setVariable("mso-service-instance-id", resourceId);
		execution.setVariable("mso-request-id", requestId);
		String falloutRequest = "<aetgt:FalloutHandlerRequest xmlns:aetgt=\"http://org.onap/so/workflow/schema/v1\"xmlns:ns=\"http://org.onap/so/request/types/v1\"xmlns:wfsch=\"http://org.onap/so/workflow/schema/v1\"><request-info xmlns=\"http://org.onap/so/infra/vnf-request/v1\"><request-id>"
				+ requestId + "</request-id><action>" + action
				+ "</action><source>VID</source></request-info><aetgt:WorkflowException xmlns:aetgt=\"http://org.onap/so/workflow/schema/v1\"><aetgt:ErrorMessage>"
				+ exceptionMsg
				+ "</aetgt:ErrorMessage><aetgt:ErrorCode>7000</aetgt:ErrorCode></aetgt:WorkflowException></aetgt:FalloutHandlerRequest>";
		execution.setVariable("falloutRequest", falloutRequest);
	}

	public void checkRetryStatus(DelegateExecution execution) {
		String handlingCode = (String) execution.getVariable("handlingCode");
		int retryCount = (int) execution.getVariable(RETRY_COUNT);
		if (handlingCode.equals("Retry")){
			if(retryCount<5){
				int currSequence = (int) execution.getVariable("gCurrentSequence");
				execution.setVariable("gCurrentSequence", currSequence-1);
				execution.setVariable(RETRY_COUNT, retryCount + 1);
			}else{
				workflowAction.buildAndThrowException(execution, "Exceeded maximum retries. Ending flow with status Abort");
			}
		}else{
			execution.setVariable(RETRY_COUNT, 0);
		}
	}

	/**
	 * Rollback will only handle Create/Activate/Assign Macro flows. Execute
	 * layer will rollback the flow its currently working on.
	 */
	public void rollbackExecutionPath(DelegateExecution execution) {
		if(!(boolean)execution.getVariable("isRollback")){
			List<ExecuteBuildingBlock> flowsToExecute = (List<ExecuteBuildingBlock>) execution
					.getVariable("flowsToExecute");
			List<ExecuteBuildingBlock> rollbackFlows = new ArrayList();
			int currentSequence = (int) execution.getVariable(G_CURRENT_SEQUENCE);
			int listSize = flowsToExecute.size();
			for (int i = listSize - 1; i >= 0; i--) {
				if (i > currentSequence - 1) {
					flowsToExecute.remove(i);
				} else {
					String flowName = flowsToExecute.get(i).getBuildingBlock().getBpmnFlowName();
					if (flowName.contains("Assign")) {
						flowName = "Unassign" + flowName.substring(6, flowName.length());
					} else if (flowName.contains("Create")) {
						flowName = "Delete" + flowName.substring(6, flowName.length());
					} else if (flowName.contains("Activate")) {
						flowName = "Deactivate" + flowName.substring(8, flowName.length());
					}else{
						continue;
					}
					flowsToExecute.get(i).getBuildingBlock().setBpmnFlowName(flowName);
					rollbackFlows.add(flowsToExecute.get(i));
				}
			}
			if (rollbackFlows.isEmpty())
				execution.setVariable("isRollbackNeeded", false);
			else
				execution.setVariable("isRollbackNeeded", true);
			execution.setVariable("flowsToExecute", rollbackFlows);
			execution.setVariable("handlingCode", "PreformingRollback");
			execution.setVariable("isRollback", true);
			execution.setVariable("gCurrentSequence", 0);
		}else{
			workflowAction.buildAndThrowException(execution, "Rollback has already been called. Cannot rollback a request that is currently in the rollback state.");
		}
	}

	public void abortCallErrorHandling(DelegateExecution execution) {
		String msg = "Flow has failed. Rainy day handler has decided to abort the process.";
		logger.error(msg);
		throw new BpmnError(msg);
	}

	public void updateRequestStatusToFailed(DelegateExecution execution) {
		try {
			String requestId = (String) execution.getVariable(G_REQUEST_ID);
			InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
			String errorMsg = null;
			boolean rollback = (boolean) execution.getVariable("isRollbackComplete");
			try {
				WorkflowException exception = (WorkflowException) execution.getVariable("WorkflowException");
				if(exception != null && (exception.getErrorMessage()!=null || !exception.getErrorMessage().equals(""))){
					errorMsg = exception.getErrorMessage();
				}
			} catch (Exception ex) {
				//log error and attempt to extact WorkflowExceptionMessage
				logger.error("Failed to extract workflow exception from execution.",ex);
			}
			if (errorMsg == null){
				try {
					errorMsg = (String) execution.getVariable("WorkflowExceptionErrorMessage");
				} catch (Exception ex) {
					logger.error("Failed to extract workflow exception message from WorkflowException",ex);
					errorMsg = "Unexpected Error in BPMN.";
				}
			}
			if(rollback){
				errorMsg = errorMsg + " + Rollback has been completed successfully.";
			}
			request.setProgress(Long.valueOf(100));
			request.setStatusMessage(errorMsg);
			request.setRequestStatus("FAILED");
			request.setLastModifiedBy("CamundaBPMN");
			requestDbclient.updateInfraActiveRequests(request);
		} catch (Exception e) {
			workflowAction.buildAndThrowException(execution, "Error Updating Request Database", e);
		}
	}
	
	public void updateRequestStatusToFailedWithRollback(DelegateExecution execution) {
		execution.setVariable("isRollbackComplete", true);
		updateRequestStatusToFailed(execution);
	}
}
