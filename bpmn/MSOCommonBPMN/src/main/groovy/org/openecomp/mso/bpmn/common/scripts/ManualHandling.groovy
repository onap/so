/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.mso.bpmn.common.scripts;

import static org.apache.commons.lang3.StringUtils.*;

import java.time.chrono.AbstractChronology
import java.util.List
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.task.TaskQuery
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject;
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.client.ruby.*



/**
 * This groovy class supports the <class>ManualHandling.bpmn</class> process.
 *
 * @author
 *
 * Inputs:
 * @param - msoRequestId
 * @param - isDebugLogEnabled
 * @param - serviceType
 * @param - vnfType
 * @param - requestorId
 * @param - currentActivity
 * @param - workStep
 * @param - failedActivity
 * @param - errorCode
 * @param - errorText
 * @param - validResponses
 * @param - vnfName
 *
 * Outputs:
 * @param - WorkflowException
 * @param - taskId
 *
 */
public class ManualHandling extends AbstractServiceTaskProcessor {

	String Prefix="MH_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	
	JsonUtils jsonUtils = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** preProcessRequest of ManualHandling *****",  isDebugLogEnabled)

		try {
			execution.setVariable("prefix", Prefix)
			setBasicDBAuthHeader(execution, isDebugLogEnabled)
			// check for required input
			String requestId = execution.getVariable("msoRequestId")
			utils.log("DEBUG", "msoRequestId is: " + requestId, isDebugLogEnabled)		
			def serviceType = execution.getVariable("serviceType")
			utils.log("DEBUG", "serviceType is: " + serviceType, isDebugLogEnabled)
			def vnfType = execution.getVariable("vnfType")
			utils.log("DEBUG", "vnftype is: " + vnfType, isDebugLogEnabled)
			def currentActivity = execution.getVariable("currentActivity")
			utils.log("DEBUG", "currentActivity is: " + currentActivity, isDebugLogEnabled)
			def workStep = execution.getVariable("workStep")
			utils.log("DEBUG", "workStep is: " + workStep, isDebugLogEnabled)
			def failedActivity = execution.getVariable("failedActivity")
			utils.log("DEBUG", "failedActivity is: " + failedActivity, isDebugLogEnabled)
			def errorCode = execution.getVariable("errorCode")
			utils.log("DEBUG", "errorCode is: " + errorCode, isDebugLogEnabled)
			def errorText = execution.getVariable("errorText")
			utils.log("DEBUG", "errorText is: " + errorText, isDebugLogEnabled)
			def requestorId = execution.getVariable("requestorId")
			utils.log("DEBUG", "requestorId is: " + requestorId, isDebugLogEnabled)
			def validResponses = execution.getVariable("validResponses")
			utils.log("DEBUG", "validResponses is: " + validResponses, isDebugLogEnabled)
			
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit preProcessRequest of RainyDayHandler *****",  isDebugLogEnabled)
	}

	public void createManualTask (DelegateExecution execution) {
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** createManualTask of ManualHandling *****",  isDebugLogEnabled)

		try {
			String taskId = UUID.randomUUID()
			execution.setVariable('taskId', taskId)
			String type = "fallout"
			String nfRole = execution.getVariable("vnfType")
			String subscriptionServiceType = execution.getVariable("serviceType")
			String originalRequestId = execution.getVariable("msoRequestId")
			String originalRequestorId = execution.getVariable("requestorId")
			String errorSource = execution.getVariable("failedActivity")
			String errorCode = execution.getVariable("errorCode")
			String errorMessage = execution.getVariable("errorText")			
			String buildingBlockName = execution.getVariable("currentActivity")
			String buildingBlockStep = execution.getVariable("workStep")
			String validResponses = execution.getVariable("validResponses")
			
			utils.log("DEBUG", "Before creating task", isDebugLogEnabled)			
			
			Map<String, String> taskVariables = new HashMap<String, String>()
			taskVariables.put("type", type)
			taskVariables.put("nfRole", nfRole)
			taskVariables.put("subscriptionServiceType", subscriptionServiceType)
			taskVariables.put("originalRequestId", originalRequestId)
			taskVariables.put("originalRequestorId", originalRequestorId)
			taskVariables.put("errorSource", errorSource)
			taskVariables.put("errorCode", errorCode)
			taskVariables.put("errorMessage", errorMessage)
			taskVariables.put("buildingBlockName", buildingBlockName)
			taskVariables.put("buildingBlockStep", buildingBlockStep)
			taskVariables.put("validResponses", validResponses)			
			
			TaskService taskService = execution.getProcessEngineServices().getTaskService()			
			Task manualTask = taskService.newTask(taskId)			
			taskService.saveTask(manualTask)			
			taskService.setVariables(taskId, taskVariables)
			utils.log("DEBUG", "successfully created task: "+ taskId, isDebugLogEnabled)

		} catch (BpmnError e) {
			utils.log("DEBUG", "BPMN exception: " + e.errorMessage, isDebugLogEnabled)
			throw e;
		} catch (Exception ex){
			msg = "Exception in createManualTask " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit createManualTask of ManualHandling *****",  isDebugLogEnabled)
	}
	
	public void setTaskVariables (DelegateTask task) {
				
		DelegateExecution execution = task.getExecution()
		
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** setTaskVariables of ManualHandling *****",  isDebugLogEnabled)
		String taskId = task.getId()
		utils.log("DEBUG", "taskId is: " + taskId, isDebugLogEnabled)

		try {			
			execution.setVariable('taskId', taskId)
			String type = "fallout"
			String nfRole = execution.getVariable("vnfType")
			String subscriptionServiceType = execution.getVariable("serviceType")
			String originalRequestId = execution.getVariable("msoRequestId")
			String originalRequestorId = execution.getVariable("requestorId")
			String errorSource = execution.getVariable("failedActivity")
			String errorCode = execution.getVariable("errorCode")
			String errorMessage = execution.getVariable("errorText")
			String buildingBlockName = execution.getVariable("currentActivity")
			String buildingBlockStep = execution.getVariable("workStep")
			String validResponses = execution.getVariable("validResponses")
			
			utils.log("DEBUG", "Before creating task", isDebugLogEnabled)
			
			Map<String, String> taskVariables = new HashMap<String, String>()
			taskVariables.put("type", type)
			taskVariables.put("nfRole", nfRole)
			taskVariables.put("subscriptionServiceType", subscriptionServiceType)
			taskVariables.put("originalRequestId", originalRequestId)
			taskVariables.put("originalRequestorId", originalRequestorId)
			taskVariables.put("errorSource", errorSource)
			taskVariables.put("errorCode", errorCode)
			taskVariables.put("errorMessage", errorMessage)
			taskVariables.put("buildingBlockName", buildingBlockName)
			taskVariables.put("buildingBlockStep", buildingBlockStep)
			taskVariables.put("validResponses", validResponses)
			TaskService taskService = execution.getProcessEngineServices().getTaskService()
			
				
			taskService.setVariables(taskId, taskVariables)
			utils.log("DEBUG", "successfully created task: "+ taskId, isDebugLogEnabled)

		} catch (BpmnError e) {
			utils.log("DEBUG", "BPMN exception: " + e.errorMessage, isDebugLogEnabled)
			throw e;
		} catch (Exception ex){
			msg = "Exception in createManualTask " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit createManualTask of ManualHandling *****",  isDebugLogEnabled)
	}
	
	public void completeTask (DelegateTask task) {
		
		DelegateExecution execution = task.getExecution()

		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** completeTask of ManualHandling *****",  isDebugLogEnabled)
		String taskId = task.getId()	
		utils.log("DEBUG", "taskId is: " + taskId, isDebugLogEnabled)

		try {			
			TaskService taskService = execution.getProcessEngineServices().getTaskService()	
		
			Map<String, String> taskVariables = taskService.getVariables(taskId)
			String responseValue = taskVariables.get("responseValue")
						
			utils.log("DEBUG", "Received responseValue on completion: "+ responseValue, isDebugLogEnabled)
			// Have to set the first letter of the response to upper case			
			String responseValueForRainyDay = responseValue.substring(0, 1).toUpperCase() + responseValue.substring(1)
			utils.log("DEBUG", "ResponseValue to RainyDayHandler: "+ responseValueForRainyDay, isDebugLogEnabled)
			execution.setVariable("responseValue", responseValueForRainyDay)

		} catch (BpmnError e) {
			utils.log("DEBUG", "BPMN exception: " + e.errorMessage, isDebugLogEnabled)
			throw e;
		} catch (Exception ex){
			msg = "Exception in createManualTask " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit completeTask of ManualHandling *****",  isDebugLogEnabled)
	}
	
	public void prepareRequestsDBStatusUpdate (DelegateExecution execution, String requestStatus){
		
		def method = getClass().getSimpleName() + '.prepareRequestsDBStatusUpdate(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		utils.log("DEBUG"," ***** prepareRequestsDBStatusUpdate of ManualHandling *****",  isDebugLogEnabled)
		try {
			def requestId = execution.getVariable("msoRequestId")
			String payload = """
				<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.openecomp.mso/requestsdb">
						   <soapenv:Header/>
						   <soapenv:Body>
						      <req:updateInfraRequest>
						         <requestId>${requestId}</requestId>
						         <lastModifiedBy>ManualHandling</lastModifiedBy>						         
						         <requestStatus>${requestStatus}</requestStatus>								 
						      </req:updateInfraRequest>
						   </soapenv:Body>
						</soapenv:Envelope>
				"""
			
			execution.setVariable("setUpdateDBstatusPayload", payload)
			utils.log("DEBUG", "Outgoing Update Mso Request Payload is: " + payload, isDebugLogEnabled)
			utils.logAudit("setUpdateDBstatusPayload: " + payload)
		
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}
		
		utils.log("DEBUG"," ***** Exit prepareRequestsDBStatusUpdate of ManualHandling *****",  isDebugLogEnabled)
	}
	
	public void createAOTSTicket (DelegateExecution execution) {
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** createAOTSTicket of ManualHandling *****",  isDebugLogEnabled)
		
		// This method will not be throwing an exception, but rather log the error

		try {
			execution.setVariable("prefix", Prefix)
			setBasicDBAuthHeader(execution, isDebugLogEnabled)
			// check for required input
			String requestId = execution.getVariable("msoRequestId")
			utils.log("DEBUG", "requestId is: " + requestId, isDebugLogEnabled)			
			def currentActivity = execution.getVariable("currentActivity")
			utils.log("DEBUG", "currentActivity is: " + currentActivity, isDebugLogEnabled)
			def workStep = execution.getVariable("workStep")
			utils.log("DEBUG", "workStep is: " + workStep, isDebugLogEnabled)
			def failedActivity = execution.getVariable("failedActivity")
			utils.log("DEBUG", "failedActivity is: " + failedActivity, isDebugLogEnabled)
			def errorCode = execution.getVariable("errorCode")
			utils.log("DEBUG", "errorCode is: " + errorCode, isDebugLogEnabled)
			def errorText = execution.getVariable("errorText")
			utils.log("DEBUG", "errorText is: " + errorText, isDebugLogEnabled)
			def vnfName = execution.getVariable("vnfName")
			utils.log("DEBUG", "vnfName is: " + vnfName, isDebugLogEnabled)			
			
			String rubyRequestId = UUID.randomUUID()
			utils.log("DEBUG", "rubyRequestId: " + rubyRequestId, isDebugLogEnabled)
			String sourceName = vnfName
			utils.log("DEBUG", "sourceName: " + sourceName, isDebugLogEnabled)
			String reason = "VID Workflow failed at " + failedActivity + " " + workStep + " call with error " + errorCode
			utils.log("DEBUG", "reason: " + reason, isDebugLogEnabled)
			String workflowId = requestId
			utils.log("DEBUG", "workflowId: " + workflowId, isDebugLogEnabled)
			String notification = "Request originated from VID | Workflow fallout on " + vnfName + " | Workflow step failure: " + workStep + " failed | VID workflow ID: " + workflowId
			utils.log("DEBUG", "notification: " + notification, isDebugLogEnabled)			
			
			utils.log("DEBUG", "Creating AOTS Ticket request")			
			
			RubyClient rubyClient = new RubyClient()
			rubyClient.rubyCreateTicketCheckRequest(rubyRequestId, sourceName, reason, workflowId, notification)			
			
		} catch (BpmnError e) {
			msg = "BPMN error in createAOTSTicket " + ex.getMessage()
			utils.log("ERROR", msg, isDebugLogEnabled)			
		} catch (Exception ex){
			msg = "Exception in createAOTSTicket " + ex.getMessage()
			utils.log("ERROR", msg, isDebugLogEnabled)			
		}
		utils.log("DEBUG"," ***** Exit createAOTSTicket of ManualHandling *****",  isDebugLogEnabled)
	}

	
	
}
