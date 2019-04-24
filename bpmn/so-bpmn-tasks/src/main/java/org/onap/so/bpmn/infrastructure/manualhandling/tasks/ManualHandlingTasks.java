package org.onap.so.bpmn.infrastructure.manualhandling.tasks;

import java.util.Map;
import java.util.HashMap;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.ticket.ExternalTicket;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ManualHandlingTasks {
    private static final Logger logger = LoggerFactory.getLogger(ManualHandlingTasks.class);

    private static final String TASK_TYPE_PAUSE = "pause";
    private static final String TASK_TYPE_FALLOUT = "fallout";
    public static final String VNF_TYPE = "vnfType";
    public static final String SERVICE_TYPE = "serviceType";
    public static final String MSO_REQUEST_ID = "msoRequestId";
    public static final String REQUESTOR_ID = "requestorId";
    public static final String ERROR_CODE = "errorCode";
    public static final String VALID_RESPONSES = "validResponses";
    public static final String DESCRIPTION = "description";
    public static final String BPMN_EXCEPTION = "BPMN exception: ";

    @Autowired
    private ExceptionBuilder exceptionUtil;

    @Autowired
    private RequestsDbClient requestDbclient;

    public void setFalloutTaskVariables(DelegateTask task) {

        DelegateExecution execution = task.getExecution();
        try {
            String taskId = task.getId();
            logger.debug("taskId is: " + taskId);
            String type = TASK_TYPE_FALLOUT;
            String nfRole = (String) execution.getVariable(VNF_TYPE);
            String subscriptionServiceType = (String) execution.getVariable(SERVICE_TYPE);
            String originalRequestId = (String) execution.getVariable(MSO_REQUEST_ID);
            String originalRequestorId = (String) execution.getVariable(REQUESTOR_ID);
            String description = "";
            String timeout = "";
            String errorSource = (String) execution.getVariable("failedActivity");
            String errorCode = (String) execution.getVariable(ERROR_CODE);
            String errorMessage = (String) execution.getVariable("errorText");
            String buildingBlockName = (String) execution.getVariable("currentActivity");
            String buildingBlockStep = (String) execution.getVariable("workStep");
            String validResponses = (String) execution.getVariable(VALID_RESPONSES);

            Map<String, String> taskVariables = new HashMap<>();
            taskVariables.put("type", type);
            taskVariables.put("nfRole", nfRole);
            taskVariables.put("subscriptionServiceType", subscriptionServiceType);
            taskVariables.put("originalRequestId", originalRequestId);
            taskVariables.put("originalRequestorId", originalRequestorId);
            taskVariables.put("errorSource", errorSource);
            taskVariables.put(ERROR_CODE, errorCode);
            taskVariables.put("errorMessage", errorMessage);
            taskVariables.put("buildingBlockName", buildingBlockName);
            taskVariables.put("buildingBlockStep", buildingBlockStep);
            taskVariables.put(VALID_RESPONSES, validResponses);
            taskVariables.put("tmeout", timeout);
            taskVariables.put(DESCRIPTION, description);
            TaskService taskService = execution.getProcessEngineServices().getTaskService();

            taskService.setVariables(taskId, taskVariables);
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
            String timeout = (String) execution.getVariable("taskTimeout");
            String errorSource = "";
            String errorCode = "";
            String errorMessage = "";
            String buildingBlockName = "";
            String buildingBlockStep = "";
            String validResponses = (String) execution.getVariable(VALID_RESPONSES);

            Map<String, String> taskVariables = new HashMap<>();
            taskVariables.put("type", type);
            taskVariables.put("nfRole", nfRole);
            taskVariables.put(DESCRIPTION, description);
            taskVariables.put("timeout", timeout);
            taskVariables.put("subscriptionServiceType", subscriptionServiceType);
            taskVariables.put("originalRequestId", originalRequestId);
            taskVariables.put("originalRequestorId", originalRequestorId);
            taskVariables.put("errorSource", errorSource);
            taskVariables.put(ERROR_CODE, errorCode);
            taskVariables.put("errorMessage", errorMessage);
            taskVariables.put("buildingBlockName", buildingBlockName);
            taskVariables.put("buildingBlockStep", buildingBlockStep);
            taskVariables.put(VALID_RESPONSES, validResponses);
            TaskService taskService = execution.getProcessEngineServices().getTaskService();

            taskService.setVariables(taskId, taskVariables);
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
            String responseValue = (String) taskVariables.get("responseValue");

            logger.debug("Received responseValue on completion: " + responseValue);
            // Have to set the first letter of the response to upper case
            String responseValueUppercaseStart =
                    responseValue.substring(0, 1).toUpperCase() + responseValue.substring(1);
            logger.debug("ResponseValue to taskListener: " + responseValueUppercaseStart);
            execution.setVariable("responseValueTask", responseValueUppercaseStart);

        } catch (BpmnError e) {
            logger.debug(BPMN_EXCEPTION + e.getMessage());
            throw e;
        } catch (Exception ex) {
            String msg = "Exception in completeManualTask " + ex.getMessage();
            logger.debug(msg);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
        }

    }

    public void createExternalTicket(DelegateExecution execution) {

        try {
            ExternalTicket ticket = new ExternalTicket();

            ticket.setRequestId((String) execution.getVariable(MSO_REQUEST_ID));
            ticket.setCurrentActivity((String) execution.getVariable("currentActivity"));
            ticket.setNfRole((String) execution.getVariable(VNF_TYPE));
            ticket.setDescription((String) execution.getVariable(DESCRIPTION));
            ticket.setSubscriptionServiceType((String) execution.getVariable(SERVICE_TYPE));
            ticket.setRequestorId((String) execution.getVariable(REQUESTOR_ID));
            ticket.setTimeout((String) execution.getVariable("taskTimeout"));
            ticket.setErrorSource((String) execution.getVariable("failedActivity"));
            ticket.setErrorCode((String) execution.getVariable(ERROR_CODE));
            ticket.setErrorMessage((String) execution.getVariable("errorText"));
            ticket.setWorkStep((String) execution.getVariable("workStep"));

            ticket.createTicket();
        } catch (BpmnError e) {
            String msg = "BPMN error in createAOTSTicket " + e.getMessage();
            logger.error("{} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN",
                    ErrorCode.UnknownError.getValue());
        } catch (Exception ex) {
            String msg = "Exception in createExternalTicket " + ex.getMessage();
            logger.error("{} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN",
                    ErrorCode.UnknownError.getValue());
        }

    }

    public void updateRequestDbStatus(DelegateExecution execution, String status) {
        try {
            String requestId = (String) execution.getVariable(MSO_REQUEST_ID);
            InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);

            request.setRequestStatus(status);
            request.setLastModifiedBy("ManualHandling");

            requestDbclient.updateInfraActiveRequests(request);
        } catch (Exception e) {
            logger.error("Unable to save the updated request status to the DB", e);
        }
    }

}
