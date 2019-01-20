/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowActionBBFailure {

	private static final Logger logger = LoggerFactory.getLogger(WorkflowActionBBFailure.class);
	@Autowired
	private RequestsDbClient requestDbclient;
	@Autowired
	private WorkflowAction workflowAction;
	
	protected void updateRequestErrorStatusMessage(DelegateExecution execution) {
		try {
			String requestId = (String) execution.getVariable("mso-request-id");
			InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
			String errorMsg = "";
			Optional<String> errorMsgOp = retrieveErrorMessage(execution);
			if(errorMsgOp.isPresent()){
				errorMsg = errorMsgOp.get();
			}else{
				errorMsg = "Failed to determine error message";
			}
			request.setStatusMessage(errorMsg);
			requestDbclient.updateInfraActiveRequests(request);
		} catch (Exception e) {
			logger.error("Failed to update Request db with the status message after retry or rollback has been initialized.",e);
		}
	}
	
	public void updateRequestStatusToFailed(DelegateExecution execution) {
		try {
			String requestId = (String) execution.getVariable("mso-request-id");
			InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
			String rollbackErrorMsg = "";
			String errorMsg = "";
			Boolean rollbackCompletedSuccessfully = (Boolean) execution.getVariable("isRollbackComplete");
			Boolean isRollbackFailure = (Boolean) execution.getVariable("isRollback");
			ExecuteBuildingBlock ebb = (ExecuteBuildingBlock) execution.getVariable("buildingBlock");
			if(rollbackCompletedSuccessfully==null)
				rollbackCompletedSuccessfully = false;
			
			if(isRollbackFailure==null)
				isRollbackFailure = false;
			
			if(rollbackCompletedSuccessfully){
				rollbackErrorMsg = "Rollback has been completed successfully.";
				request.setRollbackStatusMessage(rollbackErrorMsg);
				execution.setVariable("RollbackErrorMessage", rollbackErrorMsg);
			}else if(isRollbackFailure){
				Optional<String> rollbackErrorMsgOp = retrieveErrorMessage(execution);
				if(rollbackErrorMsgOp.isPresent()){
					rollbackErrorMsg = rollbackErrorMsgOp.get();
				}else{
					rollbackErrorMsg = "Failed to determine rollback error message.";
				}
				request.setRollbackStatusMessage(rollbackErrorMsg);
				execution.setVariable("RollbackErrorMessage", rollbackErrorMsg);
			}else{
				Optional<String> errorMsgOp = retrieveErrorMessage(execution);
				if(errorMsgOp.isPresent()){
					errorMsg = errorMsgOp.get();
				}else{
					errorMsg = "Failed to determine error message";
				}
				request.setStatusMessage(errorMsg);
				execution.setVariable("ErrorMessage", errorMsg);
			}
			if(ebb!=null && ebb.getBuildingBlock()!=null){
				String flowStatus = "";
				if(rollbackCompletedSuccessfully){
					flowStatus = "All Rollback flows have completed successfully";
				}else{
					flowStatus = ebb.getBuildingBlock().getBpmnFlowName() + " has failed.";
				}
				request.setFlowStatus(flowStatus);
				execution.setVariable("flowStatus", flowStatus);
			}

			request.setProgress(Long.valueOf(100));
			request.setRequestStatus("FAILED");
			request.setLastModifiedBy("CamundaBPMN");
			requestDbclient.updateInfraActiveRequests(request);
		} catch (Exception e) {
			workflowAction.buildAndThrowException(execution, "Error Updating Request Database", e);
		}
	}
	
	private Optional<String> retrieveErrorMessage (DelegateExecution execution){
		String errorMsg = "";
		try {
			WorkflowException exception = (WorkflowException) execution.getVariable("WorkflowException");
			if(exception != null && (exception.getErrorMessage()!=null || !exception.getErrorMessage().equals(""))){
				errorMsg = exception.getErrorMessage();
			}
			if(errorMsg == null || errorMsg.equals("")){
				errorMsg = (String) execution.getVariable("WorkflowExceptionErrorMessage");
			}
			return Optional.of(errorMsg);
		} catch (Exception ex) {
			logger.error("Failed to extract workflow exception from execution.",ex);
		}
		return Optional.empty();
	}
	
	public void updateRequestStatusToFailedWithRollback(DelegateExecution execution) {
		execution.setVariable("isRollbackComplete", true);
		updateRequestStatusToFailed(execution);
	}

	public void abortCallErrorHandling(DelegateExecution execution) {
		String msg = "Flow has failed. Rainy day handler has decided to abort the process.";
		logger.error(msg);
		throw new BpmnError(msg);
	}
}
