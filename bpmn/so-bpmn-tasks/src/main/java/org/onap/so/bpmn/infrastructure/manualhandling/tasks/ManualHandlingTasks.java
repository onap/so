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
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ManualHandlingTasks {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, ManualHandlingTasks.class);

	private static final String TASK_TYPE_PAUSE = "pause";
	private static final String TASK_TYPE_FALLOUT = "fallout";
	
	@Autowired
	private ExceptionBuilder exceptionUtil;
	 
	@Autowired
	private RequestsDbClient requestDbclient;

	public void setFalloutTaskVariables (DelegateTask task) {
		
		DelegateExecution execution = task.getExecution();	
		try {
			String taskId = task.getId();
			msoLogger.debug("taskId is: " + taskId);			
			String type = TASK_TYPE_FALLOUT;
			String nfRole = (String) execution.getVariable("vnfType");
			String subscriptionServiceType = (String) execution.getVariable("serviceType");
			String originalRequestId = (String) execution.getVariable("msoRequestId");
			String originalRequestorId = (String) execution.getVariable("requestorId");
			String description = "";
			String timeout = "";
			String errorSource = (String) execution.getVariable("failedActivity");
			String errorCode = (String) execution.getVariable("errorCode");
			String errorMessage = (String) execution.getVariable("errorText");
			String buildingBlockName = (String) execution.getVariable("currentActivity");
			String buildingBlockStep = (String) execution.getVariable("workStep");
			String validResponses = (String) execution.getVariable("validResponses");

			Map<String, String> taskVariables = new HashMap<String, String>();
			taskVariables.put("type", type);
			taskVariables.put("nfRole", nfRole);
			taskVariables.put("subscriptionServiceType", subscriptionServiceType);
			taskVariables.put("originalRequestId", originalRequestId);
			taskVariables.put("originalRequestorId", originalRequestorId);
			taskVariables.put("errorSource", errorSource);
			taskVariables.put("errorCode", errorCode);
			taskVariables.put("errorMessage", errorMessage);
			taskVariables.put("buildingBlockName", buildingBlockName);
			taskVariables.put("buildingBlockStep", buildingBlockStep);
			taskVariables.put("validResponses", validResponses);
			taskVariables.put("tmeout", timeout);
			taskVariables.put("description", description);
			TaskService taskService = execution.getProcessEngineServices().getTaskService();

			taskService.setVariables(taskId, taskVariables);
			msoLogger.debug("successfully created fallout task: "+ taskId);
		} catch (BpmnError e) {
			msoLogger.debug("BPMN exception: " + e.getMessage());
			throw e;
		} catch (Exception ex){
			String msg = "Exception in setFalloutTaskVariables " + ex.getMessage();
			msoLogger.debug(msg);
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
		}		
	}
	
	public void setPauseTaskVariables (DelegateTask task) {

		DelegateExecution execution = task.getExecution();

		try {
			String taskId = task.getId();
			msoLogger.debug("taskId is: " + taskId);			
			String type = TASK_TYPE_PAUSE;
			String nfRole = (String) execution.getVariable("vnfType");
			String subscriptionServiceType = (String) execution.getVariable("serviceType");
			String originalRequestId = (String) execution.getVariable("msoRequestId");
			String originalRequestorId = (String) execution.getVariable("requestorId");
			String description = (String) execution.getVariable("description");
			String timeout = (String) execution.getVariable("taskTimeout");
			String errorSource = "";
			String errorCode = "";
			String errorMessage = "";
			String buildingBlockName = "";
			String buildingBlockStep = "";
			String validResponses = (String) execution.getVariable("validResponses");

			Map<String, String> taskVariables = new HashMap<String, String>();
			taskVariables.put("type", type);
			taskVariables.put("nfRole", nfRole);
			taskVariables.put("description", description);
			taskVariables.put("timeout", timeout);
			taskVariables.put("subscriptionServiceType", subscriptionServiceType);
			taskVariables.put("originalRequestId", originalRequestId);
			taskVariables.put("originalRequestorId", originalRequestorId);
			taskVariables.put("errorSource", errorSource);
			taskVariables.put("errorCode", errorCode);
			taskVariables.put("errorMessage", errorMessage);
			taskVariables.put("buildingBlockName", buildingBlockName);
			taskVariables.put("buildingBlockStep", buildingBlockStep);
			taskVariables.put("validResponses", validResponses);
			TaskService taskService = execution.getProcessEngineServices().getTaskService();

			taskService.setVariables(taskId, taskVariables);
			msoLogger.debug("successfully created pause task: "+ taskId);
		} catch (BpmnError e) {
			msoLogger.debug("BPMN exception: " + e.getMessage());
			throw e;
		} catch (Exception ex){
			String msg = "Exception in setPauseTaskVariables " + ex.getMessage();
			msoLogger.debug(msg);
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
		}
		
	}

	public void completeTask (DelegateTask task) {

		DelegateExecution execution = task.getExecution();
		
		try {

			String taskId = task.getId();
			msoLogger.debug("taskId is: " + taskId);
			TaskService taskService = execution.getProcessEngineServices().getTaskService();

			Map<String, Object> taskVariables = taskService.getVariables(taskId);
			String responseValue = (String) taskVariables.get("responseValue");

			msoLogger.debug("Received responseValue on completion: "+ responseValue);
			// Have to set the first letter of the response to upper case
			String responseValueUppercaseStart = responseValue.substring(0, 1).toUpperCase() + responseValue.substring(1);
			msoLogger.debug("ResponseValue to taskListener: "+ responseValueUppercaseStart);
			execution.setVariable("responseValueTask", responseValueUppercaseStart);			
			
		} catch (BpmnError e) {
			msoLogger.debug("BPMN exception: " + e.getMessage());
			throw e;
		} catch (Exception ex){
			String msg = "Exception in completeManualTask " + ex.getMessage();
			msoLogger.debug(msg);
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
		}
		
	}

	public void createExternalTicket (DelegateExecution execution) {
		
		try {			
			ExternalTicket ticket = new ExternalTicket();	
			
			ticket.setRequestId((String) execution.getVariable("msoRequestId"));	
			ticket.setCurrentActivity((String) execution.getVariable("currentActivity"));			
			ticket.setNfRole((String) execution.getVariable("vnfType"));
			ticket.setDescription((String) execution.getVariable("description"));			
			ticket.setSubscriptionServiceType((String) execution.getVariable("serviceType"));
			ticket.setRequestorId((String) execution.getVariable("requestorId"));			
			ticket.setTimeout((String) execution.getVariable("taskTimeout"));			
			ticket.setErrorSource((String) execution.getVariable("failedActivity"));			
			ticket.setErrorCode((String) execution.getVariable("errorCode"));
			ticket.setErrorMessage((String) execution.getVariable("errorText"));			
			ticket.setWorkStep((String) execution.getVariable("workStep"));
	
			ticket.createTicket();
		} catch (BpmnError e) {
			String msg = "BPMN error in createAOTSTicket " + e.getMessage();
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.ErrorCode.UnknownError, "");
		} catch (Exception ex){
			String msg = "Exception in createExternalTicket " + ex.getMessage();
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.ErrorCode.UnknownError, "");
		}
	
	}
	
	public void updateRequestDbStatus(DelegateExecution execution, String status) {
		try {
			String requestId = (String) execution.getVariable("msoRequestId");
			InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
			
			request.setRequestStatus(status);					
			request.setLastModifiedBy("ManualHandling");
				
			requestDbclient.updateInfraActiveRequests(request);
		} catch (Exception e) {
			msoLogger.error("Unable to save the updated request status to the DB",e);
		}
	}

}
