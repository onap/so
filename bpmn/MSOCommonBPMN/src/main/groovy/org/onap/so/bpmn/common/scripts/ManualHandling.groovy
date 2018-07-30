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

package org.onap.so.bpmn.common.scripts;

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
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.ruby.*
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger





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
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, ManualHandling.class);


	String Prefix="MH_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	JsonUtils jsonUtils = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
		String msg = ""
		msoLogger.trace("preProcessRequest of ManualHandling ")

		try {
			execution.setVariable("prefix", Prefix)
			setBasicDBAuthHeader(execution, execution.getVariable('isDebugLogEnabled'))
			// check for required input
			String requestId = execution.getVariable("msoRequestId")
			msoLogger.debug("msoRequestId is: " + requestId)
			def serviceType = execution.getVariable("serviceType")
			msoLogger.debug("serviceType is: " + serviceType)
			def vnfType = execution.getVariable("vnfType")
			msoLogger.debug("vnftype is: " + vnfType)
			def currentActivity = execution.getVariable("currentActivity")
			msoLogger.debug("currentActivity is: " + currentActivity)
			def workStep = execution.getVariable("workStep")
			msoLogger.debug("workStep is: " + workStep)
			def failedActivity = execution.getVariable("failedActivity")
			msoLogger.debug("failedActivity is: " + failedActivity)
			def errorCode = execution.getVariable("errorCode")
			msoLogger.debug("errorCode is: " + errorCode)
			def errorText = execution.getVariable("errorText")
			msoLogger.debug("errorText is: " + errorText)
			def requestorId = execution.getVariable("requestorId")
			msoLogger.debug("requestorId is: " + requestorId)
			def validResponses = execution.getVariable("validResponses")
			msoLogger.debug("validResponses is: " + validResponses)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit preProcessRequest of RainyDayHandler ")
	}

	public void createManualTask (DelegateExecution execution) {
		String msg = ""
		msoLogger.trace("createManualTask of ManualHandling ")

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

			msoLogger.debug("Before creating task")

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
			msoLogger.debug("successfully created task: "+ taskId)
		} catch (BpmnError e) {
			msoLogger.debug("BPMN exception: " + e.errorMessage)
			throw e;
		} catch (Exception ex){
			msg = "Exception in createManualTask " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit createManualTask of ManualHandling ")
	}

	public void setTaskVariables (DelegateTask task) {

		DelegateExecution execution = task.getExecution()

		String msg = ""
		msoLogger.trace("setTaskVariables of ManualHandling ")
		String taskId = task.getId()
		msoLogger.debug("taskId is: " + taskId)

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

			msoLogger.debug("Before creating task")

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
			msoLogger.debug("successfully created task: "+ taskId)
		} catch (BpmnError e) {
			msoLogger.debug("BPMN exception: " + e.errorMessage)
			throw e;
		} catch (Exception ex){
			msg = "Exception in createManualTask " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit createManualTask of ManualHandling ")
	}

	public void completeTask (DelegateTask task) {

		DelegateExecution execution = task.getExecution()

		String msg = ""
		msoLogger.trace("completeTask of ManualHandling ")
		String taskId = task.getId()
		msoLogger.debug("taskId is: " + taskId)

		try {
			TaskService taskService = execution.getProcessEngineServices().getTaskService()

			Map<String, String> taskVariables = taskService.getVariables(taskId)
			String responseValue = taskVariables.get("responseValue")

			msoLogger.debug("Received responseValue on completion: "+ responseValue)
			// Have to set the first letter of the response to upper case
			String responseValueForRainyDay = responseValue.substring(0, 1).toUpperCase() + responseValue.substring(1)
			msoLogger.debug("ResponseValue to RainyDayHandler: "+ responseValueForRainyDay)
			execution.setVariable("responseValue", responseValueForRainyDay)

		} catch (BpmnError e) {
			msoLogger.debug("BPMN exception: " + e.errorMessage)
			throw e;
		} catch (Exception ex){
			msg = "Exception in createManualTask " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit completeTask of ManualHandling ")
	}

	public void prepareRequestsDBStatusUpdate (DelegateExecution execution, String requestStatus){

		def method = getClass().getSimpleName() + '.prepareRequestsDBStatusUpdate(' +'execution=' + execution.getId() +')'
		msoLogger.trace("prepareRequestsDBStatusUpdate of ManualHandling ")
		try {
			def requestId = execution.getVariable("msoRequestId")
			String payload = """
				<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.onap.so/requestsdb">
						   <soapenv:Header/>
						   <soapenv:Body>
						      <req:updateInfraRequest>
						         <requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
						         <lastModifiedBy>ManualHandling</lastModifiedBy>						         
						         <requestStatus>${MsoUtils.xmlEscape(requestStatus)}</requestStatus>								 
						      </req:updateInfraRequest>
						   </soapenv:Body>
						</soapenv:Envelope>
				"""

			execution.setVariable("setUpdateDBstatusPayload", payload)
			msoLogger.debug("Outgoing Update Mso Request Payload is: " + payload)
			msoLogger.debug("setUpdateDBstatusPayload: " + payload)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}

		msoLogger.trace("Exit prepareRequestsDBStatusUpdate of ManualHandling ")
	}

	public void createAOTSTicket (DelegateExecution execution) {
		String msg = ""
		msoLogger.trace("createAOTSTicket of ManualHandling ")
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		// This method will not be throwing an exception, but rather log the error

		try {
			execution.setVariable("prefix", Prefix)
			setBasicDBAuthHeader(execution,isDebugLogEnabled)
			// check for required input
			String requestId = execution.getVariable("msoRequestId")
			msoLogger.debug("requestId is: " + requestId)
			def currentActivity = execution.getVariable("currentActivity")
			msoLogger.debug("currentActivity is: " + currentActivity)
			def workStep = execution.getVariable("workStep")
			msoLogger.debug("workStep is: " + workStep)
			def failedActivity = execution.getVariable("failedActivity")
			msoLogger.debug("failedActivity is: " + failedActivity)
			def errorCode = execution.getVariable("errorCode")
			msoLogger.debug("errorCode is: " + errorCode)
			def errorText = execution.getVariable("errorText")
			msoLogger.debug("errorText is: " + errorText)
			def vnfName = execution.getVariable("vnfName")
			msoLogger.debug("vnfName is: " + vnfName)

			String rubyRequestId = UUID.randomUUID()
			msoLogger.debug("rubyRequestId: " + rubyRequestId)
			String sourceName = vnfName
			msoLogger.debug("sourceName: " + sourceName)
			String reason = "VID Workflow failed at " + failedActivity + " " + workStep + " call with error " + errorCode
			msoLogger.debug("reason: " + reason)
			String workflowId = requestId
			msoLogger.debug("workflowId: " + workflowId)
			String notification = "Request originated from VID | Workflow fallout on " + vnfName + " | Workflow step failure: " + workStep + " failed | VID workflow ID: " + workflowId
			msoLogger.debug("notification: " + notification)

			msoLogger.debug("Creating AOTS Ticket request")

			RubyClient rubyClient = new RubyClient()
			rubyClient.rubyCreateTicketCheckRequest(rubyRequestId, sourceName, reason, workflowId, notification)

		} catch (BpmnError e) {
			msg = "BPMN error in createAOTSTicket " + ex.getMessage()
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
		} catch (Exception ex){
			msg = "Exception in createAOTSTicket " + ex.getMessage()
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
		}
		msoLogger.trace("Exit createAOTSTicket of ManualHandling ")
	}



}
