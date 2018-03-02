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
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils;
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.core.RollbackData


public class UpdateVfModule extends AbstractServiceTaskProcessor {

	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable('prefix', 'UPDVfMod_')
		execution.setVariable('UPDVfMod_Request', null)
		execution.setVariable('UPDVfMod_requestInfo', null)
		execution.setVariable('UPDVfMod_requestId', null)
		execution.setVariable('UPDVfMod_source', null)
		execution.setVariable('UPDVfMod_vnfInputs', null)
		execution.setVariable('UPDVfMod_vnfId', null)
		execution.setVariable('UPDVfMod_vfModuleId', null)
		execution.setVariable('UPDVfMod_tenantId', null)
		execution.setVariable('UPDVfMod_volumeGroupId', null)
		execution.setVariable('UPDVfMod_vnfParams', null)
		execution.setVariable('UPDVfMod_updateInfraRequest', null)
		execution.setVariable('UpdateVfModuleSuccessIndicator', false)
	}

	/**
	 * Check for missing elements in the received request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			initProcessVariables(execution)
			String request = validateRequest(execution)

			utils.logAudit("UpdateVfModule request: " + request)
			def requestInfo = getRequiredNodeXml(execution, request, 'request-info')
			execution.setVariable('UPDVfMod_requestInfo', requestInfo)
			execution.setVariable('UPDVfMod_requestId', getRequiredNodeText(execution, requestInfo, 'request-id'))
			execution.setVariable('UPDVfMod_source', getNodeTextForce(requestInfo, 'source'))

			def vnfInputs = getRequiredNodeXml(execution, request, 'vnf-inputs')
			execution.setVariable('UPDVfMod_vnfInputs', vnfInputs)
			execution.setVariable('UPDVfMod_vnfId', getRequiredNodeText(execution, vnfInputs, 'vnf-id'))
			execution.setVariable('UPDVfMod_vfModuleId', getRequiredNodeText(execution, vnfInputs, 'vf-module-id'))
			execution.setVariable('UPDVfMod_tenantId', getRequiredNodeText(execution, vnfInputs, 'tenant-id'))
			execution.setVariable('UPDVfMod_volumeGroupId', getNodeTextForce(vnfInputs, 'volume-group-id'))

			def vnfParams = utils.getNodeXml(request, 'vnf-params')
			execution.setVariable('UPDVfMod_vnfParams', vnfParams)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare and send the synchronous response for this flow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void sendSynchResponse(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.sendSynchResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestInfo = execution.getVariable('UPDVfMod_requestInfo')
			def requestId = execution.getVariable('UPDVfMod_requestId')
			def source = execution.getVariable('UPDVfMod_source')
			def progress = getNodeTextForce(requestInfo, 'progress')
			if (progress.isEmpty()) {
				progress = '0'
			}
			def startTime = getNodeTextForce(requestInfo, 'start-time')
			if (startTime.isEmpty()) {
				startTime = System.currentTimeMillis()
			}
			def vnfInputs = execution.getVariable('UPDVfMod_vnfInputs')

			String synchResponse = """
				<vnf-request xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-info>
						<request-id>${requestId}</request-id>
						<action>UPDATE_VF_MODULE</action>
						<request-status>IN_PROGRESS</request-status>
						<progress>${progress}</progress>
						<start-time>${startTime}</start-time>
						<source>${source}</source>
					</request-info>
					${vnfInputs}
				</vnf-request>
			"""

			synchResponse = utils.formatXml(synchResponse)
			sendWorkflowResponse(execution, 200, synchResponse)

			utils.logAudit("UpdateVfModule Synch Response: " + synchResponse)
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in sendSynchResponse(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare the Request for invoking the DoUpdateVfModule subflow.
	 *
	 * NOTE: Currently, the method just logs passing through as the
	 * incoming Request to this main flow is used as the Request to
	 * the DoUpdateVfModule subflow. No preparation processing is
	 * necessary.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepDoUpdateVfModule(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepDoUpdateVfModule(' +
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
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepDoUpdateVfModule(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare the Request for updating the DB for this Infra Request.
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
			def requestId = execution.getVariable('UPDVfMod_requestId')
			def vnfId = execution.getVariable('UPDVfMod_vnfId')
			def vfModuleId = execution.getVariable('UPDVfMod_vfModuleId')
			def tenantId = execution.getVariable('UPDVfMod_tenantId')
			def volumeGroupId = execution.getVariable('UPDVfMod_volumeGroupId')

			String updateInfraRequest = """
				<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
						xmlns:req="http://org.openecomp.mso/requestsdb">
					<soapenv:Header/>
					<soapenv:Body>
						<req:updateInfraRequest>
							<requestId>${requestId}</requestId>
							<lastModifiedBy>BPEL</lastModifiedBy>
							<requestStatus>COMPLETE</requestStatus>
							<progress>100</progress>
							<vnfOutputs>
								<vnf-id>${vnfId}</vnf-id>
								<vf-module-id>${vfModuleId}</vf-module-id>
								<tenant-id>${tenantId}</tenant-id>
								<volume-group-id>${volumeGroupId}</volume-group-id>
							</vnfOutputs>
						</req:updateInfraRequest>
					</soapenv:Body>
				</soapenv:Envelope>
			"""

			updateInfraRequest = utils.formatXml(updateInfraRequest)
			execution.setVariable('UPDVfMod_updateInfraRequest', updateInfraRequest)
			logDebug('Request for Update Infra Request:\n' + updateInfraRequest, isDebugLogEnabled)

			utils.logAudit("UpdateVfModule Infra Request: " + updateInfraRequest)
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepUpdateInfraRequest(): ' + e.getMessage())
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
			def requestInfo = getVariable(execution, 'UPDVfMod_requestInfo')

			String content = """
				<sdncadapterworkflow:MsoCompletionRequest xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
						xmlns:reqtype="http://org.openecomp/mso/request/types/v1">
					${requestInfo}
					<sdncadapterworkflow:mso-bpel-name>MSO_ACTIVATE_BPEL</sdncadapterworkflow:mso-bpel-name>
				</sdncadapterworkflow:MsoCompletionRequest>
			"""

			content = utils.formatXml(content)
			logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)
			execution.setVariable(resultVar, content)

			utils.logAudit("UpdateVfModule CompletionHandler Request: " + content)
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
			def request = getVariable(execution, prefix+'Request')
			def requestInformation = utils.getNodeXml(request, 'request-information', false)

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
					${requestInformation}
					<sdncadapterworkflow:WorkflowException>
						<sdncadapterworkflow:ErrorMessage>${encErrorResponseMsg}</sdncadapterworkflow:ErrorMessage>
						<sdncadapterworkflow:ErrorCode>${errorResponseCode}</sdncadapterworkflow:ErrorCode>
					</sdncadapterworkflow:WorkflowException>
				</sdncadapterworkflow:FalloutHandlerRequest>
			"""
			content = utils.formatXml(content)
			logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)
			execution.setVariable(resultVar, content)

			utils.logAudit("UpdateVfModule fallOutHandler Request: " + content)
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildWorkflowException(execution, 2000, 'Internal Error')
		}
	}
}
