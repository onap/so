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

package org.onap.so.bpmn.infrastructure.manualhandling.tasks;

import java.util.Map;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
public class ManualHandlingTasks {
    private static final Logger logger = LoggerFactory.getLogger(ManualHandlingTasks.class);

    private static final String TASK_TYPE_PAUSE = "pause";
    private static final String TASK_TYPE_FALLOUT = "fallout";
    public static final String VNF_TYPE = "vnfType";
    public static final String DESCRIPTION = "description";
    public static final String SERVICE_TYPE = "serviceType";
    public static final String MSO_REQUEST_ID = "mso-request-id";
    public static final String REQUESTOR_ID = "requestorId";
    public static final String ERROR_CODE = "errorCode";
    public static final String VALID_RESPONSES = "validResponses";
    public static final String TASK_TIMEOUT = "taskTimeout";
    public static final String RESPONSE_VALUE_TASK = "responseValueTask";
    public static final String RESPONSE_VALUE = "responseValue";
    private static final String ASTERISK = "*";
    private static final String WORKSTEP = "workStep";

    public static final String TASK_VARIABLE_TYPE = "type";
    public static final String TASK_VARIABLE_NFROLE = "nfRole";
    public static final String TASK_VARIABLE_SUBSCRIPTION_SERVICE_TYPE = "subscriptionServiceType";
    public static final String TASK_VARIABLE_ORIGINAL_REQUEST_ID = "originalRequestId";
    public static final String TASK_VARIABLE_ORIGINAL_REQUESTOR_ID = "originalRequestorId";
    public static final String TASK_VARIABLE_ERROR_SOURCE = "errorSource";
    public static final String TASK_VARIABLE_ERROR_CODE = "errorCode";
    public static final String TASK_VARIABLE_ERROR_MESSAGE = "errorMessage";
    public static final String TASK_VARIABLE_BUILDING_BLOCK_NAME = "buildingBlockName";
    public static final String TASK_VARIABLE_BUILDING_BLOCK_STEP = "buildingBlockStep";
    public static final String TASK_VARIABLE_DESCRIPTION = "description";
    public static final String TASK_VARIABLE_TIMEOUT = "timeout";
    public static final String TASK_VARIABLE_VALID_RESPONSES = "validResponses";

    public static final String BPMN_EXCEPTION = "BPMN exception: ";
    public static final String RAINY_DAY_SERVICE_TYPE = "rainyDayServiceType";
    public static final String RAINY_DAY_VNF_TYPE = "rainyDayVnfType";
    public static final String RAINY_DAY_VNF_NAME = "rainyDayVnfName";
    public static final String G_BUILDING_BLOCK_EXECUTION = "gBuildingBlockExecution";
    public static final String WORKFLOW_EXCEPTION = "WorkflowException";

    @Autowired
    private ExceptionBuilder exceptionUtil;

    @Autowired
    private RequestsDbClient requestDbclient;

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("ExternalTicketCreation")
    private ExternalTicketCreation externalTicketCreation;

    protected String manualTaskTimeoutPath = "mso.rainyDay.manualTask.taskTimeout";
    protected String validResponsesPath = "mso.rainyDay.manualTask.validResponses";


    public void initRainyDayManualHandling(BuildingBlockExecution execution) {
        try {
            String manualTaskTimeout = this.environment.getProperty(manualTaskTimeoutPath);
            execution.setVariable(TASK_TIMEOUT, manualTaskTimeout);
        } catch (Exception e) {
            logger.error("Exception occurred", e);
            throw new BpmnError("Unknown error reading configuration for manual task handling");
        }
    }

    public void setFalloutTaskVariables(DelegateTask task) {

        DelegateExecution execution = task.getExecution();
        try {
            logger.debug("Setting fallout task variables:");
            String taskId = task.getId();
            logger.debug("taskId is: " + taskId);
            String type = TASK_TYPE_FALLOUT;
            BuildingBlockExecution gBuildingBlockExecution =
                    (BuildingBlockExecution) execution.getVariable(G_BUILDING_BLOCK_EXECUTION);
            WorkflowException workflowException = (WorkflowException) execution.getVariable(WORKFLOW_EXCEPTION);
            String nfRole = (String) execution.getVariable(RAINY_DAY_VNF_TYPE);
            logger.debug(TASK_VARIABLE_NFROLE + ": " + nfRole);
            String subscriptionServiceType = (String) execution.getVariable(RAINY_DAY_SERVICE_TYPE);
            logger.debug(TASK_VARIABLE_SUBSCRIPTION_SERVICE_TYPE + ": " + subscriptionServiceType);
            String originalRequestId = (String) execution.getVariable(MSO_REQUEST_ID);
            logger.debug(TASK_VARIABLE_ORIGINAL_REQUEST_ID + ": " + originalRequestId);
            String originalRequestorId =
                    gBuildingBlockExecution.getGeneralBuildingBlock().getRequestContext().getRequestorId();
            logger.debug(TASK_VARIABLE_ORIGINAL_REQUESTOR_ID + ": " + originalRequestorId);
            String description = "Manual user task to handle a failure of a BB execution";
            logger.debug(TASK_VARIABLE_DESCRIPTION + ": " + description);
            String taskTimeout = (String) gBuildingBlockExecution.getVariable(TASK_TIMEOUT);
            String timeout = Date.from((new Date()).toInstant().plus(Duration.parse(taskTimeout))).toGMTString();
            logger.debug(TASK_VARIABLE_TIMEOUT + ": " + timeout);
            String errorSource = ASTERISK;
            if (workflowException != null && workflowException.getExtSystemErrorSource() != null) {
                errorSource = workflowException.getExtSystemErrorSource().toString();
            }
            logger.debug(TASK_VARIABLE_ERROR_SOURCE + ": " + errorSource);
            String errorCode = ASTERISK;
            if (workflowException != null) {
                errorCode = workflowException.getErrorCode() + "";
            }
            logger.debug(TASK_VARIABLE_ERROR_CODE + ": " + errorCode);
            String errorMessage = ASTERISK;
            if (workflowException != null) {
                errorMessage = workflowException.getErrorMessage();
            }
            logger.debug(TASK_VARIABLE_ERROR_MESSAGE + ": " + errorMessage);
            String buildingBlockName = gBuildingBlockExecution.getFlowToBeCalled();
            logger.debug(TASK_VARIABLE_BUILDING_BLOCK_NAME + ": " + buildingBlockName);
            String buildingBlockStep = ASTERISK;
            if (workflowException != null) {
                buildingBlockStep = workflowException.getWorkStep();
            }
            execution.setVariable(WORKSTEP, buildingBlockStep);
            logger.debug(TASK_VARIABLE_BUILDING_BLOCK_STEP + ": " + buildingBlockStep);
            String validResponses = this.environment.getProperty(validResponsesPath);
            logger.debug(TASK_VARIABLE_VALID_RESPONSES + ": " + validResponses);

            Map<String, String> taskVariables = new HashMap<>();
            taskVariables.put(TASK_VARIABLE_TYPE, type);
            taskVariables.put(TASK_VARIABLE_NFROLE, nfRole);
            taskVariables.put(TASK_VARIABLE_SUBSCRIPTION_SERVICE_TYPE, subscriptionServiceType);
            taskVariables.put(TASK_VARIABLE_ORIGINAL_REQUEST_ID, originalRequestId);
            taskVariables.put(TASK_VARIABLE_ORIGINAL_REQUESTOR_ID, originalRequestorId);
            taskVariables.put(TASK_VARIABLE_ERROR_SOURCE, errorSource);
            taskVariables.put(TASK_VARIABLE_ERROR_CODE, errorCode);
            taskVariables.put(TASK_VARIABLE_ERROR_MESSAGE, errorMessage);
            taskVariables.put(TASK_VARIABLE_BUILDING_BLOCK_NAME, buildingBlockName);
            taskVariables.put(TASK_VARIABLE_BUILDING_BLOCK_STEP, buildingBlockStep);
            taskVariables.put(TASK_VARIABLE_VALID_RESPONSES, validResponses);
            taskVariables.put(TASK_VARIABLE_TIMEOUT, timeout);
            taskVariables.put(TASK_VARIABLE_DESCRIPTION, description);
            TaskService taskService = execution.getProcessEngineServices().getTaskService();

            taskService.setVariablesLocal(taskId, taskVariables);
            logger.debug("successfully created fallout task: " + taskId);
        } catch (BpmnError e) {
            logger.debug(BPMN_EXCEPTION + e.getMessage());
            throw e;
        } catch (Exception ex) {
            String msg = "Exception in setFalloutTaskVariables " + ex.getMessage();
            logger.debug(msg);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
        }
    }

    public void setPauseTaskVariables(DelegateTask task) {

        DelegateExecution execution = task.getExecution();

        try {
            String taskId = task.getId();
            logger.debug("taskId is: " + taskId);
            String type = TASK_TYPE_PAUSE;

            String nfRole = (String) execution.getVariable(VNF_TYPE);
            String subscriptionServiceType = (String) execution.getVariable(SERVICE_TYPE);
            String originalRequestId = (String) execution.getVariable(MSO_REQUEST_ID);
            String originalRequestorId = (String) execution.getVariable(REQUESTOR_ID);
            String description = (String) execution.getVariable(DESCRIPTION);
            String timeout = "";
            String errorSource = ASTERISK;
            String errorCode = ASTERISK;
            String errorMessage = ASTERISK;
            String buildingBlockName = ASTERISK;
            String buildingBlockStep = ASTERISK;
            String validResponses = (String) execution.getVariable(VALID_RESPONSES);

            Map<String, String> taskVariables = new HashMap<>();
            taskVariables.put(TASK_VARIABLE_TYPE, type);
            taskVariables.put(TASK_VARIABLE_NFROLE, nfRole);
            taskVariables.put(TASK_VARIABLE_DESCRIPTION, description);
            taskVariables.put(TASK_VARIABLE_TIMEOUT, timeout);
            taskVariables.put(TASK_VARIABLE_SUBSCRIPTION_SERVICE_TYPE, subscriptionServiceType);
            taskVariables.put(TASK_VARIABLE_ORIGINAL_REQUEST_ID, originalRequestId);
            taskVariables.put(TASK_VARIABLE_ORIGINAL_REQUESTOR_ID, originalRequestorId);
            taskVariables.put(TASK_VARIABLE_ERROR_SOURCE, errorSource);
            taskVariables.put(TASK_VARIABLE_ERROR_CODE, errorCode);
            taskVariables.put(TASK_VARIABLE_ERROR_MESSAGE, errorMessage);
            taskVariables.put(TASK_VARIABLE_BUILDING_BLOCK_NAME, buildingBlockName);
            taskVariables.put(TASK_VARIABLE_BUILDING_BLOCK_STEP, buildingBlockStep);
            taskVariables.put(TASK_VARIABLE_VALID_RESPONSES, validResponses);
            TaskService taskService = execution.getProcessEngineServices().getTaskService();

            taskService.setVariablesLocal(taskId, taskVariables);
            logger.debug("successfully created pause task: " + taskId);
        } catch (BpmnError e) {
            logger.debug(BPMN_EXCEPTION + e.getMessage());
            throw e;
        } catch (Exception ex) {
            String msg = "Exception in setPauseTaskVariables " + ex.getMessage();
            logger.debug(msg);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
        }

    }

    public void completeTask(DelegateTask task) {

        DelegateExecution execution = task.getExecution();

        try {

            String taskId = task.getId();
            logger.debug("taskId is: " + taskId);
            TaskService taskService = execution.getProcessEngineServices().getTaskService();

            Map<String, Object> taskVariables = taskService.getVariables(taskId);
            String responseValue = (String) taskVariables.get(RESPONSE_VALUE);

            logger.debug("Received responseValue on completion: " + responseValue);
            // Have to set the first letter of the response to upper case
            String responseValueUppercaseStart =
                    responseValue.substring(0, 1).toUpperCase() + responseValue.substring(1);
            logger.debug("ResponseValue to taskListener: " + responseValueUppercaseStart);
            execution.setVariable(RESPONSE_VALUE_TASK, responseValueUppercaseStart);

        } catch (BpmnError e) {
            logger.debug(BPMN_EXCEPTION + e.getMessage());
            throw e;
        } catch (Exception ex) {
            String msg = "Exception in completeManualTask " + ex.getMessage();
            logger.debug(msg);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
        }

    }

    public void createExternalTicket(BuildingBlockExecution execution) {
        externalTicketCreation.createExternalTicket(execution);
    }

    public void updateRequestDbStatus(BuildingBlockExecution execution, String status) {
        try {
            String requestId = (String) execution.getVariable(MSO_REQUEST_ID);
            InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);

            if (status.equalsIgnoreCase(Status.TIMEOUT.name())) {
                execution.setVariable(RESPONSE_VALUE_TASK, "Timeout");
            }
            request.setRequestStatus(status);
            request.setLastModifiedBy("ManualHandling");

            requestDbclient.updateInfraActiveRequests(request);
        } catch (Exception e) {
            logger.error("Unable to save the updated request status to the DB", e);
        }
    }

}
