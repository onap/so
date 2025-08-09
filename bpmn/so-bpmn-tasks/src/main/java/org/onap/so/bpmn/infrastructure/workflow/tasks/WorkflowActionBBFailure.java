/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import java.sql.Timestamp;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_REQ_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_REQ_ERROR;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_REQ_MESSAGE_NAME;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.CHILD_SVC_REQ_STATUS;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.IS_CHILD_PROCESS;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.PARENT_CORRELATION_ID;

@Component
public class WorkflowActionBBFailure {

    private static final String DEACTIVATE_FABRIC_CONFIGURATION_FLOW = "DeactivateFabricConfigurationBB";
    private static final String UNASSIGN_FABRIC_CONFIGURATION_FLOW = "UnassignFabricConfigurationBB";
    private static final String DELETE_FABRIC_CONFIGURATION_FLOW = "DeleteFabricConfigurationBB";
    private static final Logger logger = LoggerFactory.getLogger(WorkflowActionBBFailure.class);
    public static final String ROLLBACK_TARGET_STATE = "rollbackTargetState";

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
            if (errorMsgOp.isPresent()) {
                errorMsg = errorMsgOp.get();
            } else {
                errorMsg = "Failed to determine error message";
            }
            Boolean isRollback = (Boolean) execution.getVariable("isRollback");
            if (!Boolean.TRUE.equals(isRollback)) {
                request.setStatusMessage(errorMsg);
            } else {
                request.setRollbackStatusMessage(errorMsg);
            }
            request.setProgress(Long.valueOf(100));
            request.setLastModifiedBy("CamundaBPMN");
            request.setEndTime(new Timestamp(System.currentTimeMillis()));
            requestDbclient.updateInfraActiveRequests(request);
        } catch (Exception e) {
            logger.error(
                    "Failed to update Request db with the status message after retry or rollback has been initialized.",
                    e);
        }
    }

    public void updateRequestStatusToFailed(DelegateExecution execution) {
        try {
            String requestId = (String) execution.getVariable("mso-request-id");
            InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
            String rollbackErrorMsg = "";
            String errorMsg = "";
            String childErrorMessage = "";
            Boolean rollbackCompletedSuccessfully = (Boolean) execution.getVariable("isRollbackComplete");
            Boolean isRollbackFailure = (Boolean) execution.getVariable("isRollback");
            ExecuteBuildingBlock ebb = (ExecuteBuildingBlock) execution.getVariable("buildingBlock");
            if (rollbackCompletedSuccessfully == null)
                rollbackCompletedSuccessfully = false;

            if (isRollbackFailure == null)
                isRollbackFailure = false;

            if (rollbackCompletedSuccessfully) {
                rollbackErrorMsg = "Rollback has been completed successfully.";
                childErrorMessage = rollbackErrorMsg;
                request.setRollbackStatusMessage(rollbackErrorMsg);
                execution.setVariable("RollbackErrorMessage", rollbackErrorMsg);
                String rollbackTargetState = (String) execution.getVariable(ROLLBACK_TARGET_STATE);
                request.setRequestStatus(rollbackTargetState);
            } else if (isRollbackFailure) {
                if (ebb != null && ebb.getBuildingBlock() != null && ebb.getBuildingBlock().getBpmnFlowName() != null) {
                    String flowName = ebb.getBuildingBlock().getBpmnFlowName();
                    if (DEACTIVATE_FABRIC_CONFIGURATION_FLOW.equalsIgnoreCase(flowName)
                            || UNASSIGN_FABRIC_CONFIGURATION_FLOW.equalsIgnoreCase(flowName)
                            || DELETE_FABRIC_CONFIGURATION_FLOW.equalsIgnoreCase(flowName)) {
                        String statusMessage = String.format(
                                "%s Warning: The vf-module is active but configuration was not removed completely for one or more VMs.",
                                request.getStatusMessage());
                        request.setStatusMessage(statusMessage);
                    }
                }
                Optional<String> rollbackErrorMsgOp = retrieveErrorMessage(execution);
                if (rollbackErrorMsgOp.isPresent()) {
                    rollbackErrorMsg = rollbackErrorMsgOp.get();
                } else {
                    rollbackErrorMsg = "Failed to determine rollback error message.";
                }
                childErrorMessage = rollbackErrorMsg;
                request.setRollbackStatusMessage(rollbackErrorMsg);
                execution.setVariable("RollbackErrorMessage", rollbackErrorMsg);
                request.setRequestStatus(Status.FAILED.toString());
            } else {
                Optional<String> errorMsgOp = retrieveErrorMessage(execution);
                if (errorMsgOp.isPresent()) {
                    errorMsg = errorMsgOp.get();
                } else {
                    errorMsg = "Failed to determine error message";
                }
                childErrorMessage = errorMsg;
                request.setStatusMessage(errorMsg);
                execution.setVariable("ErrorMessage", errorMsg);
                String handlingCode = (String) execution.getVariable("handlingCode");
                if ("Abort".equalsIgnoreCase(handlingCode)) {
                    request.setRequestStatus(Status.ABORTED.toString());
                } else {
                    request.setRequestStatus(Status.FAILED.toString());
                }
            }
            if (ebb != null && ebb.getBuildingBlock() != null) {
                String flowStatus = "";
                if (rollbackCompletedSuccessfully) {
                    flowStatus = "All Rollback flows have completed successfully";
                } else {
                    flowStatus = ebb.getBuildingBlock().getBpmnFlowName() + " has failed.";
                }
                request.setFlowStatus(flowStatus);
                execution.setVariable("flowStatus", flowStatus);
            }

            if (Boolean.TRUE.equals(execution.getVariable(IS_CHILD_PROCESS))) {
                String parentCorrelationId = (String) execution.getVariable(PARENT_CORRELATION_ID);
                logger.info("Child service creation failed. Sending message to parent with correlationId: {}",
                        parentCorrelationId);
                execution.getProcessEngineServices().getRuntimeService()
                        .createMessageCorrelation(CHILD_SVC_REQ_MESSAGE_NAME)
                        .setVariable(CHILD_SVC_REQ_STATUS, "FAILED").setVariable(CHILD_SVC_REQ_ERROR, childErrorMessage)
                        .processInstanceVariableEquals(CHILD_SVC_REQ_CORRELATION_ID, parentCorrelationId).correlate();
            }

            request.setProgress(Long.valueOf(100));
            request.setLastModifiedBy("CamundaBPMN");
            request.setEndTime(new Timestamp(System.currentTimeMillis()));
            requestDbclient.updateInfraActiveRequests(request);
        } catch (Exception e) {
            workflowAction.buildAndThrowException(execution, "Error Updating Request Database", e);
        }
    }

    private Optional<String> retrieveErrorMessage(DelegateExecution execution) {
        String errorMsg = null;
        try {
            WorkflowException exception = (WorkflowException) execution.getVariable("WorkflowException");
            if (exception != null && (exception.getErrorMessage() != null || !"".equals(exception.getErrorMessage()))) {
                errorMsg = exception.getErrorMessage();
            }
            if (errorMsg == null || "".equals(errorMsg)) {
                errorMsg = (String) execution.getVariable("WorkflowExceptionErrorMessage");
            }
            if (errorMsg == null) {
                throw new IllegalStateException(
                        "could not find WorkflowException or WorkflowExceptionErrorMessage in execution");
            }
            return Optional.of(errorMsg);
        } catch (Exception ex) {
            logger.error("Failed to extract workflow exception from execution.", ex);
        }
        return Optional.empty();
    }

    public void updateRequestStatusToFailedWithRollback(DelegateExecution execution) {
        execution.setVariable("isRollbackComplete", true);
        updateRequestStatusToFailed(execution);
    }

    public void abortCallErrorHandling() {
        String msg = "Flow has failed. Rainy day handler has decided to abort the process.";
        logger.error(msg);
        throw new BpmnError(msg);
    }
}
