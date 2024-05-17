/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.scripts

import org.onap.so.logger.LoggingAnchor
import org.onap.logging.filter.base.ErrorCode;
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory


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
    private static final Logger logger = LoggerFactory.getLogger( ManualHandling.class);


	String Prefix="MH_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	JsonUtils jsonUtils = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
		String msg = ""
		logger.trace("preProcessRequest of ManualHandling ")

		try {
			execution.setVariable("prefix", Prefix)
			setBasicDBAuthHeader(execution, execution.getVariable('isDebugLogEnabled'))
			// check for required input
			String requestId = execution.getVariable("msoRequestId")
			logger.debug("msoRequestId is: " + requestId)
			def serviceType = execution.getVariable("serviceType")
			logger.debug("serviceType is: " + serviceType)
			def vnfType = execution.getVariable("vnfType")
			logger.debug("vnftype is: " + vnfType)
			def currentActivity = execution.getVariable("currentActivity")
			logger.debug("currentActivity is: " + currentActivity)
			def workStep = execution.getVariable("workStep")
			logger.debug("workStep is: " + workStep)
			def failedActivity = execution.getVariable("failedActivity")
			logger.debug("failedActivity is: " + failedActivity)
			def errorCode = execution.getVariable("errorCode")
			logger.debug("errorCode is: " + errorCode)
			def errorText = execution.getVariable("errorText")
			logger.debug("errorText is: " + errorText)
			def requestorId = execution.getVariable("requestorId")
			logger.debug("requestorId is: " + requestorId)
			def validResponses = execution.getVariable("validResponses")
			logger.debug("validResponses is: " + validResponses)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit preProcessRequest of RainyDayHandler ")
	}

	public void createManualTask (DelegateExecution execution) {
		String msg = ""
		logger.trace("createManualTask of ManualHandling ")

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

			logger.debug("Before creating task")

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
			taskVariables.put("description", "")
			taskVariables.put("timeout", "")

			TaskService taskService = execution.getProcessEngineServices().getTaskService()
			Task manualTask = taskService.newTask(taskId)
			taskService.saveTask(manualTask)
			taskService.setVariables(taskId, taskVariables)
			logger.debug("successfully created task: "+ taskId)
		} catch (BpmnError e) {
			logger.debug("BPMN exception: " + e.errorMessage)
			throw e;
		} catch (Exception ex){
			msg = "Exception in createManualTask " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit createManualTask of ManualHandling ")
	}

	public void setTaskVariables (DelegateTask task) {

		DelegateExecution execution = task.getExecution()

		String msg = ""
		logger.trace("setTaskVariables of ManualHandling ")
		String taskId = task.getId()
		logger.debug("taskId is: " + taskId)

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

			logger.debug("Before creating task")

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
			taskVariables.put("description", "")
			taskVariables.put("timeout", "")
			TaskService taskService = execution.getProcessEngineServices().getTaskService()


			taskService.setVariables(taskId, taskVariables)
			logger.debug("successfully created task: "+ taskId)
		} catch (BpmnError e) {
			logger.debug("BPMN exception: " + e.errorMessage)
			throw e;
		} catch (Exception ex){
			msg = "Exception in createManualTask " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit createManualTask of ManualHandling ")
	}

	public void completeTask (DelegateTask task) {

		DelegateExecution execution = task.getExecution()

		String msg = ""
		logger.trace("completeTask of ManualHandling ")
		String taskId = task.getId()
		logger.debug("taskId is: " + taskId)

		try {
			TaskService taskService = execution.getProcessEngineServices().getTaskService()

			Map<String, String> taskVariables = taskService.getVariables(taskId)
			String responseValue = taskVariables.get("responseValue")

			logger.debug("Received responseValue on completion: "+ responseValue)
			// Have to set the first letter of the response to upper case
			String responseValueForRainyDay = responseValue.substring(0, 1).toUpperCase() + responseValue.substring(1)
			logger.debug("ResponseValue to RainyDayHandler: "+ responseValueForRainyDay)
			execution.setVariable("responseValue", responseValueForRainyDay)

		} catch (BpmnError e) {
			logger.debug("BPMN exception: " + e.errorMessage)
			throw e;
		} catch (Exception ex){
			msg = "Exception in createManualTask " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit completeTask of ManualHandling ")
	}

	public void prepareRequestsDBStatusUpdate (DelegateExecution execution, String requestStatus){

		def method = getClass().getSimpleName() + '.prepareRequestsDBStatusUpdate(' +'execution=' + execution.getId() +')'
		logger.trace("prepareRequestsDBStatusUpdate of ManualHandling ")
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
			logger.debug("Outgoing Update Mso Request Payload is: " + payload)
			logger.debug("setUpdateDBstatusPayload: " + payload)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}

		logger.trace("Exit prepareRequestsDBStatusUpdate of ManualHandling ")
	}

}
