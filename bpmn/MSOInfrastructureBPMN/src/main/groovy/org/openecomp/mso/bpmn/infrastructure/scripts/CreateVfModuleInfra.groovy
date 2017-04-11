/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.bpmn.infrastructure.scripts;

import groovy.json.JsonSlurper

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils;
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils;
import org.openecomp.mso.bpmn.common.scripts.VidUtils;
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException

public class CreateVfModuleInfra extends AbstractServiceTaskProcessor {
	
	/**
	 * Validates the request message and sets up the workflow.
	 * @param execution the execution
	 */
	public void preProcessRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		def prefix = 'CVFMI_'
		logDebug('Entered 1' + method, isDebugLogEnabled)
		execution.setVariable('prefix', prefix)
		logDebug('Entered 2' + method, isDebugLogEnabled)
		execution.setVariable("isVidRequest", "false")
		
		logDebug("Set variables", isDebugLogEnabled)	
		
		def rollbackData = execution.getVariable("RollbackData")
		if (rollbackData == null) {
			rollbackData = new RollbackData()
		}
		execution.setVariable("RollbackData", rollbackData)
		
		logDebug("Set rollback data", isDebugLogEnabled)
		def incomingRequest = execution.getVariable('bpmnRequest')
		
		utils.log("DEBUG", "Incoming Infra Request: " + incomingRequest, isDebugLogEnabled)
		utils.logAudit("CreateVfModule Infra incoming Request: " + incomingRequest)
		
		// check if request is xml or json
		try {
			def jsonSlurper = new JsonSlurper()
			Map reqMap = jsonSlurper.parseText(incomingRequest)
			utils.log("DEBUG", " Request is in JSON format.", isDebugLogEnabled)
			
			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			def vnfId = execution.getVariable('vnfId')
			
			def vidUtils = new VidUtils(this)
			
			String requestInXmlFormat = vidUtils.createXmlVfModuleRequest(execution, reqMap, 'CREATE_VF_MODULE', serviceInstanceId)
			
			utils.log("DEBUG", " Request in XML format: " + requestInXmlFormat, isDebugLogEnabled)
			
			execution.setVariable(prefix + 'Request', requestInXmlFormat)
			execution.setVariable(prefix+'vnfId', vnfId)
			execution.setVariable("isVidRequest", "true")			
			
		}
		catch(groovy.json.JsonException je) {
			utils.log("DEBUG", " Request is not in JSON format.", isDebugLogEnabled)
			workflowException(execution, "Invalid request format", 400)
			
		}
		catch(Exception e) {
			String restFaultMessage = e.getMessage()
			//execution.setVariable("CVFMODVOL2_RESTFault", restFaultMessage)
			//execution.setVariable("CVFMODVOL2_isDataOk", false)
			utils.log("ERROR", " Exception Encountered - " + "\n" + restFaultMessage, isDebugLogEnabled)
			workflowException(execution, restFaultMessage, 400)
		}

		try {
			String request = validateInfraRequest(execution)
			
			execution.setVariable("CreateVfModuleInfraSuccessIndicator", false)
			execution.setVariable("RollbackCompleted", false)
			execution.setVariable("DoCreateVfModuleRequest", request)
			execution.setVariable("isDebugLogEnabled", isDebugLogEnabled)
			execution.setVariable("CVFMI_requestInfo",utils.getNodeXml(request,"request-info"))	
			execution.setVariable("CVFMI_requestId",utils.getNodeText1(request,"request-id"))
			execution.setVariable("CVFMI_source",utils.getNodeText1(request,"source"))
			execution.setVariable("CVFMI_serviceInstanceId", utils.getNodeText1(request, "service-instance-id"))		
			execution.setVariable("CVFMI_vnfInputs",utils.getNodeXml(request,"vnf-inputs"))
			//backoutOnFailure
			
			NetworkUtils networkUtils = new NetworkUtils()
			execution.setVariable("CVFMI_rollbackEnabled", networkUtils.isRollbackEnabled(execution,request))
			execution.setVariable("CVFMI_originalWorkflowException", null)
			def vnfParams = ""
			if (utils.nodeExists(request, "vnf-params")) {
				vnfParams = utils.getNodeXml(request,"vnf-params")
			}
			execution.setVariable("CVFMI_vnfParams", vnfParams)
			
			def newVfModuleId = UUID.randomUUID().toString()
			execution.setVariable("newVfModuleId", newVfModuleId)
			
			logDebug('RequestInfo: ' + execution.getVariable("CVFMI_requestInfo"), isDebugLogEnabled)
			logDebug('VnfInputs: ' + execution.getVariable("CVFMI_vnfInputs"), isDebugLogEnabled)
			logDebug('VnfParams: ' + execution.getVariable("CVFMI_vnfParams"), isDebugLogEnabled)
			logDebug('rollbackEnabled: ' + execution.getVariable("CVFMI_rollbackEnabled"), isDebugLogEnabled)
			
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError bpmnError) {
			buildErrorResponse(execution,bpmnError.getMessage(),"400")
			throw bpmnError
		} catch (Exception exception) {
			workflowException(execution, exception.getMessage(), 400)
		}		 
	}

	/**
	 * Validates a workflow response.
	 * @param execution the execution
	 * @param responseVar the execution variable in which the response is stored
	 * @param responseCodeVar the execution variable in which the response code is stored
	 * @param errorResponseVar the execution variable in which the error response is stored
	 */
	public void validateWorkflowResponse(Execution execution, String responseVar,
			String responseCodeVar, String errorResponseVar) {
		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, responseVar, responseCodeVar, errorResponseVar)
	}


	/**
	 * Sends the empty, synchronous response back to the API Handler.
	 * @param execution the execution
	 */
	public void sendResponse(Execution execution) {
		def method = getClass().getSimpleName() + '.sendResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestInfo = execution.getVariable('CVFMI_requestInfo')
			def requestId = execution.getVariable('CVFMI_requestId')
			def source = execution.getVariable('CVFMI_source')
			def progress = getNodeTextForce(requestInfo, 'progress')
			if (progress.isEmpty()) {
				progress = '0'
			}
			def startTime = getNodeTextForce(requestInfo, 'start-time')
			if (startTime.isEmpty()) {
				startTime = System.currentTimeMillis()
			}			
				
			// RESTResponse (for API Handler (APIH) Reply Task)
			def newVfModuleId = execution.getVariable("newVfModuleId")
			String synchResponse = """{"requestReferences":{"instanceId":"${newVfModuleId}","requestId":"${requestId}"}}""".trim()
			
			sendWorkflowResponse(execution, 200, synchResponse)
			
			utils.logAudit("CreateVfModule Infra Response: " + synchResponse)
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in sendResponse(): ' + e.getMessage())
		}
	}

	/**
	 * 
	 * @param execution the execution
	 */
	public void postProcessResponse(Execution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		
		utils.log("DEBUG", " ======== STARTED PostProcessResponse Process ======== ", isDebugEnabled)
		try{
			def request = execution.getVariable("DoCreateVfModuleRequest")
			def requestInfo = utils.getNodeXml(request, 'request-info', false)
			def action = utils.getNodeText1(requestInfo, "action")
			
			utils.log("DEBUG", "requestInfo is: " + requestInfo, isDebugEnabled)			
			utils.log("DEBUG", "action is: " + action, isDebugEnabled)

			String payload =
					"""  <aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                               xmlns:ns="http://org.openecomp/mso/request/types/v1"
                               xmlns:ns8="http://org.openecomp/mso/workflow/schema/v1">
			<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
			${requestInfo}
			</request-info>
			<ns8:status-message>Vf Module has been created successfully.</ns8:status-message>
			<ns8:mso-bpel-name>BPMN</ns8:mso-bpel-name>
			</aetgt:MsoCompletionRequest>"""

			payload = utils.formatXml(payload)
			execution.setVariable("CVFMI_SuccessFlag", true)
			execution.setVariable("CVFMI_msoCompletionRequest", payload)
			utils.logAudit("CreateVfModuleInfra completion request: " + payload)
			utils.log("DEBUG", "Outgoing MsoCompletionRequest: \n" + payload, isDebugEnabled)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing PostProcessResponse. Exception is:\n" + e, isDebugEnabled)
			execution.setVariable("CVFMI_ErrorResponse", "Error Occured during PostProcessResponse Method:\n" + e.getMessage())
		}
		utils.log("DEBUG", "======== COMPLETED PostProcessResponse Process ======== ", isDebugEnabled)
	}

	
	
	
	
	/**
	 * Validates the request, request id and service instance id.  If a problem is found,
	 * a WorkflowException is generated and an MSOWorkflowException event is thrown. This
	 * method also sets up the log context for the workflow.
	 * @param execution the execution
	 * @return the validated request
	 */
	public String validateInfraRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.validateInfraRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		String processKey = getProcessKey(execution);
		def prefix = execution.getVariable("prefix")

		if (prefix == null) {
			createWorkflowException(execution, 1002, processKey + " prefix is null")
		}

		try {
			def request = execution.getVariable(prefix + 'Request')

			if (request == null) {
				request = execution.getVariable(processKey + 'Request')
	
				if (request == null) {
					request = execution.getVariable('bpmnRequest')
				}
	
				setVariable(execution, processKey + 'Request', null);
				setVariable(execution, 'bpmnRequest', null);
				setVariable(execution, prefix + 'Request', request);
			}

			if (request == null) {
				createWorkflowException(execution, 1002, processKey + " request is null")
			}
			
			/*

			def requestId = execution.getVariable("mso-request-id")
			
			if (requestId == null) {
				createWorkflowException(execution, 1002, processKey + " request has no mso-request-id")
			}
			
			setVariable(execution, prefix + 'requestId', requestId)

			def serviceInstanceId = execution.getVariable("mso-service-instance-id")

			if (serviceInstanceId == null) {
				createWorkflowException(execution, 1002, processKey + " request message has no mso-service-instance-id")
			}

			utils.logContext(requestId, serviceInstanceId)
			*/
			utils.logAudit("CreateVfModule incoming request: " + request)
			logDebug('Incoming message: ' + System.lineSeparator() + request, isDebugLogEnabled)
			logDebug('Exited ' + method, isDebugLogEnabled)
			return request
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, "Invalid Message")
		}
	}
	
	public void prepareUpdateInfraRequest(Execution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		
		utils.log("DEBUG", " ======== STARTED prepareUpdateInfraRequest Process ======== ", isDebugEnabled)
		try{

			String vnfInputs = execution.getVariable("CVFMI_vnfInputs")
			String requestInfo = execution.getVariable("CVFMI_requestInfo")
			def aicCloudRegion	= utils.getNodeText1(vnfInputs, "aic-cloud-region")
			def tenantId = utils.getNodeText1(vnfInputs, "tenant-id")
			def requestId = utils.getNodeText1(requestInfo, "request-id")
			def vnfId = execution.getVariable("CVFMI_vnfId")
			def vfModuleId = execution.getVariable("CVFMI_vfModuleId")

			def dbAdapterEndpoint = execution.getVariable("URN_mso_adapters_db_endpoint")
			execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			utils.log("DEBUG", "DB Adapter Endpoint is: " + dbAdapterEndpoint, isDebugEnabled)
			
			try {
				String basicAuthValueDB = execution.getVariable("URN_mso_adapters_db_auth")
				utils.log("DEBUG", " Obtained BasicAuth userid password for Catalog DB adapter: " + basicAuthValueDB, isDebugEnabled)
				
				def encodedString = utils.getBasicAuth(basicAuthValueDB, execution.getVariable("URN_mso_msoKey"))
				execution.setVariable("BasicAuthHeaderValueDB",encodedString)
			} catch (IOException ex) {
				String dataErrorMessage = " Unable to encode Catalog DB user/password string - " + ex.getMessage()
				utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			}
			
			String payload =
				"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
						xmlns:ns="http://org.openecomp.mso/requestsdb">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.openecomp.mso/requestsdb">
							<requestId>${requestId}</requestId>
							<lastModifiedBy>BPMN</lastModifiedBy>
							<statusMessage>VF Module successfully created</statusMessage>
							<responseBody></responseBody>
							<requestStatus>COMPLETE</requestStatus>
							<progress>100</progress>
							<vnfOutputs>&lt;vnf-outputs xmlns="http://org.openecomp/mso/infra/vnf-request/v1" xmlns:aetgt="http://org.openecomp/mso/infra/vnf-request/v1" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"&gt;&lt;vnf-id&gt;${vnfId}&lt;/vnf-id&gt;&lt;vf-module-id&gt;${vfModuleId}&lt;/vf-module-id&gt;&lt;/vnf-outputs&gt;</vnfOutputs>
							<vfModuleId>${vfModuleId}</vfModuleId>
						</ns:updateInfraRequest> 
					</soapenv:Body>
				</soapenv:Envelope>"""

			payload = utils.formatXml(payload)
			execution.setVariable("CVFMI_updateInfraRequest", payload)
			utils.log("DEBUG", "Outgoing UpdateInfraRequest: \n" + payload, isDebugEnabled)
			utils.logAudit("CreateVfModuleInfra Outgoing UpdateInfra Request: " + payload)
			
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing prepareUpdateInfraRequest. Exception is:\n" + e, isDebugEnabled)
			execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during prepareUpdateInfraRequest Method:\n" + e.getMessage())
		}
		utils.log("DEBUG", "======== COMPLETED prepareUpdateInfraRequest Process ======== ", isDebugEnabled)
	}
	
	/**
	 * Builds a "FalloutHandler" request and stores it in the specified execution variable.
	 * 
	 * @param execution the execution
	 * @param resultVar the execution variable in which the result will be stored
	 */
	public void falloutHandlerPrep(Execution execution, String resultVar) {
		def method = getClass().getSimpleName() + '.falloutHandlerPrep(' +
			'execution=' + execution.getId() +
			', resultVar=' + resultVar +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		
		try {
			def WorkflowException workflowException = execution.getVariable("WorkflowException")
			def request = execution.getVariable("DoCreateVfModuleRequest")
			def requestInformation = utils.getNodeXml(request, 'request-info', false)
			def errorResponseCode = workflowException.getErrorCode()
			def errorResponseMsg = workflowException.getErrorMessage()
			def encErrorResponseMsg = ""
			if (errorResponseMsg != null) {
				encErrorResponseMsg = errorResponseMsg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
			}

			String content = """
				<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
						xmlns:reqtype="http://org.openecomp/mso/request/types/v1"
						xmlns:msoservtypes="http://org.openecomp/mso/request/types/v1"
						xmlns:structuredtypes="http://org.openecomp/mso/structured/types/v1">
						${requestInformation}										
					<aetgt:WorkflowException>
						<aetgt:ErrorMessage>${encErrorResponseMsg}</aetgt:ErrorMessage>
						<aetgt:ErrorCode>${errorResponseCode}</aetgt:ErrorCode>
					</aetgt:WorkflowException>	
				</aetgt:FalloutHandlerRequest>
			"""
			
			logDebug("CONTENT before translation: " + content, isDebugLogEnabled)
			content = utils.formatXml(content)
			logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)
			utils.logAudit("CreateVfModuleInfra FallOutHander Request: " + content)
			execution.setVariable(resultVar, content)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 2000, 'Internal Error')
		}
	}
	
	public void logAndSaveOriginalException(Execution execution) {
		def method = getClass().getSimpleName() + '.validateRollbackResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		logWorkflowException(execution, 'CreateVfModuleInfra caught an event')
		saveWorkflowException(execution, 'CVFMI_originalWorkflowException')
	}
	
	public void validateRollbackResponse(Execution execution) {
		def method = getClass().getSimpleName() + '.validateRollbackResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		def originalException = execution.getVariable("CVFMI_originalWorkflowException")
		execution.setVariable("WorkflowException", originalException)
		
		execution.setVariable("RollbackCompleted", true)
	
	}
	
		
}
