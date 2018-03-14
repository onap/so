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

package org.openecomp.mso.bpmn.infrastructure.scripts

import groovy.util.Node
import groovy.util.XmlParser;
import groovy.xml.QName

import java.io.Serializable;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution

import groovy.json.JsonSlurper

import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.common.scripts.VidUtils;
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils;


public class DeleteVfModuleInfra extends AbstractServiceTaskProcessor {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable('prefix', 'DELVfModI_')
		execution.setVariable('DELVfModI_requestInfo', null)
		execution.setVariable('DELVfModI_requestId', null)
		execution.setVariable('DELVfModI_source', null)
		execution.setVariable('DELVfModI_vnfInputs', null)
		execution.setVariable('DELVfModI_vnfId', null)
		execution.setVariable('DELVfModI_vfModuleId', null)
		execution.setVariable('DELVfModI_tenantId', null)
		execution.setVariable('DELVfModI_volumeGroupId', null)
		execution.setVariable('DELVfModI_vnfParams', null)
		execution.setVariable('DELVfModI_updateInfraRequest', null)
		execution.setVariable('DeleteVfModuleRequest', null)
		execution.setVariable('DeleteVfModuleSuccessIndicator', false)
	}

	/**
	 * Process the incoming DELETE_VF_MODULE vnf-request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("isVidRequest", "false")
		initProcessVariables(execution)

		def prefix = execution.getVariable('prefix')

		def incomingRequest = execution.getVariable('bpmnRequest')

		utils.log("DEBUG", "Incoming Infra Request: " + incomingRequest, isDebugLogEnabled)
		utils.logAudit("DeleteVfModule Infra incoming Request: " + incomingRequest)

		// check if request is xml or json
		try {
			def jsonSlurper = new JsonSlurper()
			Map reqMap = jsonSlurper.parseText(incomingRequest)
			utils.log("DEBUG", " Request is in JSON format.", isDebugLogEnabled)

			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			utils.log("DEBUG", "serviceInstanceId is: " + serviceInstanceId, isDebugLogEnabled)
			def vnfId = execution.getVariable('vnfId')
			utils.log("DEBUG", "vnfId is: " + vnfId, isDebugLogEnabled)
			def cloudConfiguration = jsonUtil.getJsonValue(incomingRequest, "requestDetails.cloudConfiguration")
			execution.setVariable("cloudConfiguration", cloudConfiguration)
			utils.log("DEBUG", "CloudConfiguration is: " + cloudConfiguration, isDebugLogEnabled)
			def vfModuleModelInfo = jsonUtil.getJsonValue(incomingRequest, "requestDetails.modelInfo")

			execution.setVariable("vfModuleModelInfo", vfModuleModelInfo)
			utils.log("DEBUG", "VfModuleModelInfo is: " + vfModuleModelInfo, isDebugLogEnabled)
			
			// This is aLaCarte flow, so aLaCarte flag is always on
			execution.setVariable('aLaCarte', true)
			
			def vidUtils = new VidUtils(this)

			String requestInXmlFormat = vidUtils.createXmlVfModuleRequest(execution, reqMap, 'DELETE_VF_MODULE', serviceInstanceId)

			utils.log("DEBUG", " Request in XML format: " + requestInXmlFormat, isDebugLogEnabled)

			setBasicDBAuthHeader(execution, isDebugLogEnabled)
			
			execution.setVariable(prefix + 'Request', requestInXmlFormat)
			execution.setVariable(prefix+'vnfId', vnfId)
			execution.setVariable("isVidRequest", "true")

		}
		catch(groovy.json.JsonException je) {
			utils.log("DEBUG", " Request is not in JSON format.", isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Internal Error - During PreProcess Request")

		}
		catch(Exception e) {
			String restFaultMessage = e.getMessage()
			utils.log("ERROR", " Exception Encountered - " + "\n" + restFaultMessage, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Internal Error - During PreProcess Request")
		}


		try {

			String request = validateRequest(execution)
			execution.setVariable('DeleteVfModuleRequest', request)
			utils.logAudit("DeleteVfModuleInfra Request: " + request)

			def requestInfo = getRequiredNodeXml(execution, request, 'request-info')
			execution.setVariable('DELVfModI_requestInfo', requestInfo)
			execution.setVariable('DELVfModI_requestId', getRequiredNodeText(execution, requestInfo, 'request-id'))
			execution.setVariable('DELVfModI_source', getNodeTextForce(requestInfo, 'source'))

			def vnfInputs = getRequiredNodeXml(execution, request, 'vnf-inputs')
			execution.setVariable('DELVfModI_vnfInputs', vnfInputs)
			execution.setVariable('DELVfModI_vnfId', getRequiredNodeText(execution, vnfInputs, 'vnf-id'))
			execution.setVariable('DELVfModI_vfModuleId', getRequiredNodeText(execution, vnfInputs, 'vf-module-id'))
			execution.setVariable('DELVfModI_vfModuleName', getNodeTextForce(vnfInputs, 'vf-module-name'))
			execution.setVariable('DELVfModI_tenantId', getRequiredNodeText(execution, vnfInputs, 'tenant-id'))
			execution.setVariable('DELVfModI_volumeGroupId', getNodeTextForce(vnfInputs, 'volume-group-id'))

			def vnfParams = utils.getNodeXml(request, 'vnf-params')
			execution.setVariable('DELVfModI_vnfParams', vnfParams)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
		}
	}

	/**
	 * Sends the 'IN_PROGRESS' synchronous response.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void sendSynchResponse(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.sendResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestInfo = execution.getVariable('DELVfModI_requestInfo')
			def requestId = execution.getVariable('DELVfModI_requestId')
			def source = execution.getVariable('DELVfModI_source')
			def progress = getNodeTextForce(requestInfo, 'progress')
			if (progress.isEmpty()) {
				progress = '0'
			}
			def startTime = getNodeTextForce(requestInfo, 'start-time')
			if (startTime.isEmpty()) {
				startTime = System.currentTimeMillis()
			}

			// RESTResponse (for API Handler (APIH) Reply Task)
			def vfModuleId = execution.getVariable('DELVfModI_vfModuleId')
			String synchResponse = """{"requestReferences":{"instanceId":"${vfModuleId}","requestId":"${requestId}"}}""".trim()

			utils.logAudit("DeleteVfModuleInfra Synch Response: " + synchResponse)
			sendWorkflowResponse(execution, 200, synchResponse)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in sendResponse(): ' + e.getMessage())
		}
	}

	/**
	 * Currently passing the entire DELETE_VF_MODULE vnf-request to DoDeleteVfModule.
	 * 'DeleteVfModuleRequest' is now being set in preProcessRequest().
	 * TBD: may want to eventually create a specific request that only contains the needed fields.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepDoDeleteVfModule(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepDoDeleteVfModule(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepDoDeleteVfModule(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare the DB update to add an entry for the Vf Module request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepUpdateInfraRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepUpdateInfraRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestId = execution.getVariable('DELVfModI_requestId')
			def vnfId = execution.getVariable('DELVfModI_vnfId')
			def vfModuleId = execution.getVariable('DELVfModI_vfModuleId')
			def tenantId = execution.getVariable('DELVfModI_tenantId')
			def volumeGroupId = execution.getVariable('DELVfModI_volumeGroupId')

			String updateInfraRequest = """
				<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
					xmlns:req="http://org.openecomp.mso/requestsdb">
					<soapenv:Header/>
					<soapenv:Body>
						<req:updateInfraRequest>
							<requestId>${requestId}</requestId>
							<lastModifiedBy>BPMN</lastModifiedBy>
							<requestStatus>COMPLETED</requestStatus>
							<progress>100</progress>
						</req:updateInfraRequest>
					</soapenv:Body>
				</soapenv:Envelope>
			"""

			updateInfraRequest = utils.formatXml(updateInfraRequest)
			execution.setVariable('DELVfModI_updateInfraRequest', updateInfraRequest)
			utils.logAudit("DeleteAAIVfModuleInfra Update Request: " + updateInfraRequest)
			logDebug('Request for Update Infra Request:\n' + updateInfraRequest, isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepInfraRequest(): ' + e.getMessage())
		}
	}

	/**
	 * Builds a "CompletionHandler" request and stores it in the specified execution variable.
	 *
	 * @param execution the execution
	 * @param resultVar the execution variable in which the result will be stored
	 */
	public void completionHandlerPrep(DelegateExecution execution, String resultVar) {
		def method = getClass().getSimpleName() + '.completionHandlerPrep(' +
			'execution=' + execution.getId() +
			', resultVar=' + resultVar +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def request = execution.getVariable("DeleteVfModuleRequest")
			def requestInfo = utils.getNodeXml(request, 'request-info', false)
			def action = utils.getNodeText1(requestInfo, "action")

			String content =
					"""  <aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
						   xmlns:ns="http://org.openecomp/mso/request/types/v1"
						   xmlns:ns8="http://org.openecomp/mso/workflow/schema/v1">
		<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
			${requestInfo}
			</request-info>
			<ns8:status-message>Vf Module has been deleted successfully.</ns8:status-message>
			<ns8:mso-bpel-name>BPMN</ns8:mso-bpel-name>
			</aetgt:MsoCompletionRequest>"""

			content = utils.formatXml(content)
			logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)
			utils.logAudit("DeleteVfModule Infra Completion Handler Request: " + content)
			execution.setVariable(resultVar, content)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, 'Internal Error')
		}
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
			def prefix = execution.getVariable('prefix')
			def request = execution.getVariable("DeleteVfModuleRequest")
			def requestInfo = utils.getNodeXml(request, 'request-info', false)
			def WorkflowException workflowException = execution.getVariable("WorkflowException")
			def errorResponseCode = workflowException.getErrorCode()
			def errorResponseMsg = workflowException.getErrorMessage()
			def encErrorResponseMsg = ""
			if (errorResponseMsg != null) {
				encErrorResponseMsg = errorResponseMsg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
			}

			String content = """
			<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
					xmlns:reqtype="http://org.openecomp/mso/request/types/v1"
					xmlns:msoservtypes="http://org.openecomp/mso/request/types/v1"
					xmlns:structuredtypes="http://org.openecomp/mso/structured/types/v1">
					${requestInfo}
					<sdncadapterworkflow:WorkflowException>
						<sdncadapterworkflow:ErrorMessage>${encErrorResponseMsg}</sdncadapterworkflow:ErrorMessage>
						<sdncadapterworkflow:ErrorCode>${errorResponseCode}</sdncadapterworkflow:ErrorCode>
					</sdncadapterworkflow:WorkflowException>
				</sdncadapterworkflow:FalloutHandlerRequest>
			"""
			content = utils.formatXml(content)
			utils.logAudit("DeleteVfModuleInfra Fallout Handler Request: " + content)
			logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)
			execution.setVariable(resultVar, content)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildWorkflowException(execution, 2000, 'Internal Error')
		}
	}
}
