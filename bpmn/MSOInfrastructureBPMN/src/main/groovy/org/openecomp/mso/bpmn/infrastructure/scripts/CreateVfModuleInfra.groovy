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

package org.openecomp.mso.bpmn.infrastructure.scripts;

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils;
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils;
import org.openecomp.mso.bpmn.common.scripts.VidUtils;
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils

public class CreateVfModuleInfra extends AbstractServiceTaskProcessor {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	/**
	 * Validates the request message and sets up the workflow.
	 * @param execution the execution
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		execution.setVariable("CVFMI_sentSyncResponse", false)
		
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

		setBasicDBAuthHeader(execution, isDebugLogEnabled)
		
		// check if request is xml or json
		try {
			def jsonSlurper = new JsonSlurper()
			def jsonOutput = new JsonOutput()
			Map reqMap = jsonSlurper.parseText(incomingRequest)
			utils.log("DEBUG", " Request is in JSON format.", isDebugLogEnabled)

			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			def vnfId = execution.getVariable('vnfId')
			
			execution.setVariable(prefix + 'serviceInstanceId', serviceInstanceId)
			execution.setVariable(prefix+'vnfId', vnfId)
			execution.setVariable("isVidRequest", "true")
			
			def vnfName = ''
			def asdcServiceModelVersion = ''
			def serviceModelInfo = null
			def vnfModelInfo = null
			
			def relatedInstanceList = reqMap.requestDetails?.relatedInstanceList
						
			if (relatedInstanceList != null) {
				relatedInstanceList.each {
					if (it.relatedInstance.modelInfo?.modelType == 'service') {
						asdcServiceModelVersion = it.relatedInstance.modelInfo?.modelVersion
						serviceModelInfo = jsonOutput.toJson(it.relatedInstance.modelInfo)
						
					}
					if (it.relatedInstance.modelInfo.modelType == 'vnf') {
						vnfName = it.relatedInstance.instanceName ?: ''
						vnfModelInfo = jsonOutput.toJson(it.relatedInstance.modelInfo)
					}
				}
			}
			
			execution.setVariable(prefix + 'vnfName', vnfName)
			execution.setVariable(prefix + 'asdcServiceModelVersion', asdcServiceModelVersion)
			execution.setVariable(prefix + 'serviceModelInfo', serviceModelInfo)
			execution.setVariable(prefix + 'vnfModelInfo', vnfModelInfo)
			
			
			def vnfType = execution.getVariable('vnfType')
			execution.setVariable(prefix + 'vnfType', vnfType)	
			def vfModuleId = execution.getVariable('vfModuleId')
			execution.setVariable(prefix + 'vfModuleId', vfModuleId)
			def volumeGroupId = execution.getVariable('volumeGroupId')
			execution.setVariable(prefix + 'volumeGroupId', volumeGroupId)
			def userParams = reqMap.requestDetails?.requestParameters?.userParams					
			
			Map<String, String> userParamsMap = [:]
			if (userParams != null) {
				userParams.each { userParam ->
					userParamsMap.put(userParam.name, jsonOutput.toJson(userParam.value).toString())
				}							
			}		
						
			utils.log("DEBUG", 'Processed user params: ' + userParamsMap, isDebugLogEnabled)		
			
			execution.setVariable(prefix + 'vfModuleInputParams', userParamsMap)
			
			def isBaseVfModule = "false"
			if (execution.getVariable('isBaseVfModule') == true) {
				isBaseVfModule = "true"
			}			
			
			execution.setVariable(prefix + 'isBaseVfModule', isBaseVfModule)
						
			def requestId = execution.getVariable("mso-request-id")
			execution.setVariable(prefix + 'requestId', requestId)
			
			def vfModuleModelInfo = jsonOutput.toJson(reqMap.requestDetails?.modelInfo)
			execution.setVariable(prefix + 'vfModuleModelInfo', vfModuleModelInfo)
			
			def suppressRollback = reqMap.requestDetails?.requestInfo?.suppressRollback
			
			
			def backoutOnFailure = ""
			if(suppressRollback != null){
				if ( suppressRollback == true) {
					backoutOnFailure = "false"
				} else if ( suppressRollback == false) {
					backoutOnFailure = "true"
				}
			}
			
			execution.setVariable('disableRollback', suppressRollback)
			
			def vfModuleName = reqMap.requestDetails?.requestInfo?.instanceName ?: null
			execution.setVariable(prefix + 'vfModuleName', vfModuleName)
			
			def serviceId = reqMap.requestDetails?.requestParameters?.serviceId ?: ''
			execution.setVariable(prefix + 'serviceId', serviceId)
			
			def usePreload = reqMap.requestDetails?.requestParameters?.usePreload
			execution.setVariable(prefix + 'usePreload', usePreload)
			
			// This is aLaCarte flow, so aLaCarte flag is always on				
			execution.setVariable(prefix + 'aLaCarte', true)
			
			def cloudConfiguration = reqMap.requestDetails?.cloudConfiguration
			def lcpCloudRegionId	= cloudConfiguration.lcpCloudRegionId
			execution.setVariable(prefix + 'lcpCloudRegionId', lcpCloudRegionId)
			def tenantId = cloudConfiguration.tenantId
			execution.setVariable(prefix + 'tenantId', tenantId)
			
			def globalSubscriberId = reqMap.requestDetails?.subscriberInfo?.globalSubscriberId ?: ''
			execution.setVariable(prefix + 'globalSubscriberId', globalSubscriberId)
			
			execution.setVariable(prefix + 'sdncVersion', '1702')

			execution.setVariable("CreateVfModuleInfraSuccessIndicator", false)
			execution.setVariable("RollbackCompleted", false)
			
			execution.setVariable("isDebugLogEnabled", isDebugLogEnabled)
			
			
			def source = reqMap.requestDetails?.requestInfo?.source
			execution.setVariable("CVFMI_source", source)
			
			//For Completion Handler & Fallout Handler
			String requestInfo =
			"""<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>CREATE</action>
					<source>${source}</source>
				   </request-info>"""
			
			execution.setVariable("CVFMI_requestInfo", requestInfo)
			
			//backoutOnFailure
			
			execution.setVariable("CVFMI_originalWorkflowException", null)
			

			def newVfModuleId = UUID.randomUUID().toString()
			execution.setVariable("newVfModuleId", newVfModuleId)
			execution.setVariable(prefix + 'vfModuleId', newVfModuleId)

			logDebug('RequestInfo: ' + execution.getVariable("CVFMI_requestInfo"), isDebugLogEnabled)			
			
			logDebug('rollbackEnabled: ' + execution.getVariable("CVFMI_rollbackEnabled"), isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError bpmnError) {
			throw bpmnError
		}
		catch(groovy.json.JsonException je) {
			utils.log("DEBUG", " Request is not in JSON format.", isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Internal Error - During PreProcessRequest")
		}
		catch(Exception e) {
			String restFaultMessage = e.getMessage()
			//execution.setVariable("CVFMODVOL2_RESTFault", restFaultMessage)
			//execution.setVariable("CVFMODVOL2_isDataOk", false)
			utils.log("ERROR", " Exception Encountered - " + "\n" + restFaultMessage, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Internal Error - During PreProcessRequest")
		}

	}

	/**
	 * Validates a workflow response.
	 * @param execution the execution
	 * @param responseVar the execution variable in which the response is stored
	 * @param responseCodeVar the execution variable in which the response code is stored
	 * @param errorResponseVar the execution variable in which the error response is stored
	 */
	public void validateWorkflowResponse(DelegateExecution execution, String responseVar,
			String responseCodeVar, String errorResponseVar) {
		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, responseVar, responseCodeVar, errorResponseVar)
	}


	/**
	 * Sends the empty, synchronous response back to the API Handler.
	 * @param execution the execution
	 */
	public void sendResponse(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.sendResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestInfo = execution.getVariable('CVFMI_requestInfo')
			def requestId = execution.getVariable('CVFMI_requestId')
			def source = execution.getVariable('CVFMI_source')			
			
			// RESTResponse (for API Handler (APIH) Reply Task)
			def newVfModuleId = execution.getVariable("newVfModuleId")
			String synchResponse = """{"requestReferences":{"instanceId":"${newVfModuleId}","requestId":"${requestId}"}}""".trim()

			sendWorkflowResponse(execution, 200, synchResponse)

			execution.setVariable("CVFMI_sentSyncResponse", true)
			utils.logAudit("CreateVfModule Infra Response: " + synchResponse)
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in sendResponse(): ' + e.getMessage())
		}
	}

	/**
	 *
	 * @param execution the execution
	 */
	public void postProcessResponse(DelegateExecution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " ======== STARTED PostProcessResponse Process ======== ", isDebugEnabled)
		try{			
			def requestInfo = execution.getVariable("CVFMI_requestInfo")
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
	public String validateInfraRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.validateInfraRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		String processKey = getProcessKey(execution);
		def prefix = execution.getVariable("prefix")

		if (prefix == null) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " prefix is null")
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
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " request is null")
			}

			/*

			def requestId = execution.getVariable("mso-request-id")

			if (requestId == null) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " request has no mso-request-id")
			}

			setVariable(execution, prefix + 'requestId', requestId)

			def serviceInstanceId = execution.getVariable("mso-service-instance-id")

			if (serviceInstanceId == null) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " request message has no mso-service-instance-id")
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
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Invalid Message")
		}
	}

	public void prepareUpdateInfraRequest(DelegateExecution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " ======== STARTED prepareUpdateInfraRequest Process ======== ", isDebugEnabled)
		try{
			
			
			String requestInfo = execution.getVariable("CVFMI_requestInfo")			
			def aicCloudRegion	= execution.getVariable("CVFMI_lcpCloudRegionId")
			def tenantId = execution.getVariable("CVFMI_tenantId")
			def requestId = utils.getNodeText1(requestInfo, "request-id")
			def vnfId = execution.getVariable("CVFMI_vnfId")
			def vfModuleId = execution.getVariable("CVFMI_vfModuleId")
			// vfModuleName may be generated by DoCreateVfModule subprocess if it is not specified on the input
			def vfModuleName = execution.getVariable("CVFMI_vfModuleName")

			def dbAdapterEndpoint = execution.getVariable("URN_mso_adapters_openecomp_db_endpoint")
			execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			utils.log("DEBUG", "DB Adapter Endpoint is: " + dbAdapterEndpoint, isDebugEnabled)

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
							<vfModuleName>${vfModuleName}</vfModuleName>
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
	public void falloutHandlerPrep(DelegateExecution execution, String resultVar) {
		def method = getClass().getSimpleName() + '.falloutHandlerPrep(' +
			'execution=' + execution.getId() +
			', resultVar=' + resultVar +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)


		try {
			def WorkflowException workflowException = execution.getVariable("WorkflowException")			
			def requestInformation = execution.getVariable("CVFMI_requestInfo")
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
			exceptionUtil.buildWorkflowException(execution, 2000, 'Internal Error')
		}
	}

	public void logAndSaveOriginalException(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.validateRollbackResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		logWorkflowException(execution, 'CreateVfModuleInfra caught an event')
		saveWorkflowException(execution, 'CVFMI_originalWorkflowException')
	}

	public void validateRollbackResponse(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.validateRollbackResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		def originalException = execution.getVariable("CVFMI_originalWorkflowException")
		execution.setVariable("WorkflowException", originalException)

		execution.setVariable("RollbackCompleted", true)

	}
	
	public void sendErrorResponse(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " *** STARTED CreateVfModulenfra sendErrorResponse Process *** ", isDebugEnabled)
		try {
			def sentSyncResponse = execution.getVariable("CVFMI_sentSyncResponse")
			if(sentSyncResponse == false){
				WorkflowException wfex = execution.getVariable("WorkflowException")
				String response = exceptionUtil.buildErrorResponseXml(wfex)

				utils.logAudit(response)
				sendWorkflowResponse(execution, 500, response)
			}else{
				utils.log("DEBUG", "Not Sending Error Response.  Sync Response Already Sent", isDebugEnabled)
			}

		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateVfModuleInfra sendErrorResponse Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVfModuleInfra sendErrorResponse Process")

		}
		utils.log("DEBUG", "*** COMPLETED CreateVfModuleInfra sendErrorResponse Process ***", isDebugEnabled)
	}


}
