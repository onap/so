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

package org.onap.so.bpmn.infrastructure.scripts

import org.onap.so.logger.LoggingAnchor
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.VidUtils;
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonSlurper



public class DeleteVfModuleInfra extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger( DeleteVfModuleInfra.class);
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
		logger.trace('Entered ' + method)
		execution.setVariable("isVidRequest", "false")
		initProcessVariables(execution)

		def prefix = execution.getVariable('prefix')

		def incomingRequest = execution.getVariable('bpmnRequest')
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		
		logger.debug("Incoming Infra Request: " + incomingRequest)

		// check if request is xml or json
		try {
			def jsonSlurper = new JsonSlurper()
			Map reqMap = jsonSlurper.parseText(incomingRequest)
			logger.debug(" Request is in JSON format.")

			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			logger.debug("serviceInstanceId is: " + serviceInstanceId)
			def vnfId = execution.getVariable('vnfId')
			logger.debug("vnfId is: " + vnfId)
			def cloudConfiguration = jsonUtil.getJsonValue(incomingRequest, "requestDetails.cloudConfiguration")
			execution.setVariable("cloudConfiguration", cloudConfiguration)
			logger.debug("CloudConfiguration is: " + cloudConfiguration)
			def vfModuleModelInfo = jsonUtil.getJsonValue(incomingRequest, "requestDetails.modelInfo")

			execution.setVariable("vfModuleModelInfo", vfModuleModelInfo)
			logger.debug("VfModuleModelInfo is: " + vfModuleModelInfo)
			
			// This is aLaCarte flow, so aLaCarte flag is always on
			execution.setVariable('aLaCarte', true)
			
			def vidUtils = new VidUtils(this)

			String requestInXmlFormat = vidUtils.createXmlVfModuleRequest(execution, reqMap, 'DELETE_VF_MODULE', serviceInstanceId)

			logger.debug(" Request in XML format: " + requestInXmlFormat)

			setBasicDBAuthHeader(execution, isDebugLogEnabled)
			
			execution.setVariable(prefix + 'Request', requestInXmlFormat)
			execution.setVariable(prefix+'vnfId', vnfId)
			execution.setVariable("isVidRequest", "true")

		}
		catch(groovy.json.JsonException je) {
			logger.debug(" Request is not in JSON format.")
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Internal Error - During PreProcess Request")

		}
		catch(Exception e) {
			String restFaultMessage = e.getMessage()
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "Caught exception",
					"BPMN", ErrorCode.UnknownError.getValue(),
					"Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Internal Error - During PreProcess Request")
		}


		try {

			String request = validateRequest(execution)
			execution.setVariable('DeleteVfModuleRequest', request)
			logger.debug("DeleteVfModuleInfra Request: " + request)

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

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Caught exception in " + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
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
		logger.trace('Entered ' + method)

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

			logger.debug("DeleteVfModuleInfra Synch Response: " + synchResponse)
			sendWorkflowResponse(execution, 200, synchResponse)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Caught exception in " + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
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
		logger.trace('Entered ' + method)

		try {

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), 'Caught exception in '
					+ method, "BPMN", ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
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
		logger.trace('Entered ' + method)

		try {
			def requestId = execution.getVariable('DELVfModI_requestId')
			def vnfId = execution.getVariable('DELVfModI_vnfId')
			def vfModuleId = execution.getVariable('DELVfModI_vfModuleId')
			def tenantId = execution.getVariable('DELVfModI_tenantId')
			def volumeGroupId = execution.getVariable('DELVfModI_volumeGroupId')

			String updateInfraRequest = """
				<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
					xmlns:req="http://org.onap.so/requestsdb">
					<soapenv:Header/>
					<soapenv:Body>
						<req:updateInfraRequest>
							<requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
							<lastModifiedBy>BPMN</lastModifiedBy>
							<requestStatus>COMPLETED</requestStatus>
							<progress>100</progress>
						</req:updateInfraRequest>
					</soapenv:Body>
				</soapenv:Envelope>
			"""

			updateInfraRequest = utils.formatXml(updateInfraRequest)
			execution.setVariable('DELVfModI_updateInfraRequest', updateInfraRequest)
			logger.debug('Request for Update Infra Request:\n' + updateInfraRequest)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
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
		logger.trace('Entered ' + method)

		try {
			def request = execution.getVariable("DeleteVfModuleRequest")
			def requestInfo = utils.getNodeXml(request, 'request-info', false)
			def action = utils.getNodeText(requestInfo, "action")

			String content =
					"""  <aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
						   xmlns:ns="http://org.onap/so/request/types/v1"
						   xmlns:ns8="http://org.onap/so/workflow/schema/v1">
		<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
			${requestInfo}
			</request-info>
			<ns8:status-message>Vf Module has been deleted successfully.</ns8:status-message>
			<ns8:mso-bpel-name>BPMN</ns8:mso-bpel-name>
			</aetgt:MsoCompletionRequest>"""

			content = utils.formatXml(content)
			logger.debug(resultVar + ' = ' + System.lineSeparator() + content)
			execution.setVariable(resultVar, content)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
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
		logger.trace('Entered ' + method)

		try {
			def prefix = execution.getVariable('prefix')
			def request = execution.getVariable("DeleteVfModuleRequest")
			def requestInfo = utils.getNodeXml(request, 'request-info', false)
			def WorkflowException workflowException = execution.getVariable("WorkflowException")
			def errorResponseCode = workflowException.getErrorCode()
			def errorResponseMsg = workflowException.getErrorMessage()
			def encErrorResponseMsg = ""
			if (errorResponseMsg != null) {
				encErrorResponseMsg = errorResponseMsg
			}

			String content = """
			<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
					xmlns:reqtype="http://org.onap/so/request/types/v1"
					xmlns:msoservtypes="http://org.onap/so/request/types/v1"
					xmlns:structuredtypes="http://org.onap/so/structured/types/v1">
					${requestInfo}
					<sdncadapterworkflow:WorkflowException>
						<sdncadapterworkflow:ErrorMessage>${MsoUtils.xmlEscape(encErrorResponseMsg)}</sdncadapterworkflow:ErrorMessage>
						<sdncadapterworkflow:ErrorCode>${MsoUtils.xmlEscape(errorResponseCode)}</sdncadapterworkflow:ErrorCode>
					</sdncadapterworkflow:WorkflowException>
				</sdncadapterworkflow:FalloutHandlerRequest>
			"""
			content = utils.formatXml(content)
			logger.debug(resultVar + ' = ' + System.lineSeparator() + content)
			execution.setVariable(resultVar, content)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildWorkflowException(execution, 2000, 'Internal Error')
		}
	}
}
