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

package com.att.bpm.scripts

import groovy.json.JsonSlurper
import groovy.util.Node
import groovy.util.XmlParser;
import groovy.xml.QName

import java.io.Serializable;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution

import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException


public class UpdateVfModuleInfra extends AbstractServiceTaskProcessor {
		
	/**
	 * Initialize the flow's variables.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(Execution execution) {
		execution.setVariable('prefix', 'UPDVfModI_')
		execution.setVariable('UPDVfModI_Request', null)
		execution.setVariable('UPDVfModI_requestInfo', null)
		execution.setVariable('UPDVfModI_requestId', null)
		execution.setVariable('UPDVfModI_source', null)
		execution.setVariable('UPDVfModI_vnfInputs', null)
		execution.setVariable('UPDVfModI_vnfId', null)
		execution.setVariable('UPDVfModI_vfModuleId', null)
		execution.setVariable('UPDVfModI_tenantId', null)
		execution.setVariable('UPDVfModI_volumeGroupId', null)
		execution.setVariable('UPDVfModI_vnfParams', null)
		execution.setVariable('UPDVfModI_updateInfraRequest', null)
		execution.setVariable('UpdateVfModuleSuccessIndicator', false)
	}	
	
	/**
	 * Check for missing elements in the received request.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void preProcessRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		initProcessVariables(execution)
		
		def prefix = "UPDVfModI_"
		
		execution.setVariable("isVidRequest", "false")
		
		def incomingRequest = execution.getVariable('bpmnRequest')
		
		utils.log("DEBUG", "Incoming Infra Request: " + incomingRequest, isDebugLogEnabled)
		
		// check if request is xml or json
		try {
			def jsonSlurper = new JsonSlurper()
			Map reqMap = jsonSlurper.parseText(incomingRequest)
			utils.log("DEBUG", " Request is in JSON format.", isDebugLogEnabled)
			
			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			def vnfId = execution.getVariable('vnfId')
			
			def vidUtils = new VidUtils(this)
			
			String requestInXmlFormat = vidUtils.createXmlVfModuleRequest(execution, reqMap, 'UPDATE_VF_MODULE', serviceInstanceId)
			
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
			utils.log("ERROR", " Exception Encountered - " + "\n" + restFaultMessage, isDebugLogEnabled)
			workflowException(execution, restFaultMessage, 400)
		}
				

		try {
			
			String request = validateInfraRequest(execution)
			
			def requestInfo = getRequiredNodeXml(execution, request, 'request-info')
			execution.setVariable('UPDVfModI_requestInfo', requestInfo)
			execution.setVariable('UPDVfModI_requestId', getRequiredNodeText(execution, requestInfo, 'request-id'))
			execution.setVariable('UPDVfModI_source', getNodeTextForce(requestInfo, 'source'))
			
			def vnfInputs = getRequiredNodeXml(execution, request, 'vnf-inputs')
			execution.setVariable('UPDVfModI_vnfInputs', vnfInputs)
			execution.setVariable('UPDVfModI_vnfId', getRequiredNodeText(execution, vnfInputs, 'vnf-id'))
			execution.setVariable('UPDVfModI_vfModuleId', getRequiredNodeText(execution, vnfInputs, 'vf-module-id'))
			execution.setVariable('UPDVfModI_tenantId', getRequiredNodeText(execution, vnfInputs, 'tenant-id'))
			execution.setVariable('UPDVfModI_volumeGroupId', getNodeTextForce(vnfInputs, 'volume-group-id'))

			def vnfParams = utils.getNodeXml(request, 'vnf-params')
			execution.setVariable('UPDVfModI_vnfParams', vnfParams)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
		}
	}
	
	/**
	 * Prepare and send the sychronous response for this flow.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void sendSynchResponse(Execution execution) {
		def method = getClass().getSimpleName() + '.sendSynchResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		
		try {
			def requestInfo = execution.getVariable('UPDVfModI_requestInfo')
			def requestId = execution.getVariable('UPDVfModI_requestId')
			def source = execution.getVariable('UPDVfModI_source')
			def progress = getNodeTextForce(requestInfo, 'progress')
			if (progress.isEmpty()) {
				progress = '0'
			}
			def startTime = getNodeTextForce(requestInfo, 'start-time')
			if (startTime.isEmpty()) {
				startTime = System.currentTimeMillis()
			}			
				
			// RESTResponse (for API Handler (APIH) Reply Task)
			def vfModuleId = execution.getVariable("vfModuleId")
			String synchResponse = """{"requestReferences":{"instanceId":"${vfModuleId}","requestId":"${requestId}"}}""".trim()
			
			sendWorkflowResponse(execution, 200, synchResponse)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in sendResponse(): ' + e.getMessage())
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
	public void prepDoUpdateVfModule(Execution execution) {
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
			createWorkflowException(execution, 1002, 'Error in prepDoUpdateVfModule(): ' + e.getMessage())
		}
	}
	
	/**
	 * Prepare the Request for updating the DB for this Infra Request.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void prepUpdateInfraRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.prepUpdateInfraRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestId = execution.getVariable('UPDVfModI_requestId')
			def vnfId = execution.getVariable('UPDVfModI_vnfId')
			def vfModuleId = execution.getVariable('UPDVfModI_vfModuleId')
			def tenantId = execution.getVariable('UPDVfModI_tenantId')
			def volumeGroupId = execution.getVariable('UPDVfModI_volumeGroupId')
			
			String updateInfraRequest = """
				<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
						xmlns:req="http://com.att.mso/requestsdb">
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
			execution.setVariable('UPDVfModI_updateInfraRequest', updateInfraRequest)
			logDebug('Request for Update Infra Request:\n' + updateInfraRequest, isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in prepUpdateInfraRequest(): ' + e.getMessage())
		}
	}
	
	/**
	 * Builds a "CompletionHandler" request and stores it in the specified execution variable.
	 * 
	 * @param execution the execution
	 * @param resultVar the execution variable in which the result will be stored
	 */
	public void completionHandlerPrep(Execution execution, String resultVar) {
		def method = getClass().getSimpleName() + '.completionHandlerPrep(' +
			'execution=' + execution.getId() +
			', resultVar=' + resultVar +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestInfo = getVariable(execution, 'UPDVfModI_requestInfo')
			
			String content = """
				<sdncadapterworkflow:MsoCompletionRequest xmlns:sdncadapterworkflow="http://ecomp.att.com/mso/workflow/schema/v1"
						xmlns:reqtype="http://ecomp.att.com/mso/request/types/v1">
					${requestInfo}
					<sdncadapterworkflow:mso-bpel-name>MSO_ACTIVATE_BPEL</sdncadapterworkflow:mso-bpel-name>
				</sdncadapterworkflow:MsoCompletionRequest>
			"""

			content = utils.formatXml(content)
			logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)
			execution.setVariable(resultVar, content)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 2000, 'Internal Error')
		}
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
			def prefix = execution.getVariable('prefix')
			def request = getVariable(execution, prefix+'Request')
			def requestInformation = utils.getNodeXml(request, 'request-info', false)
			
			def WorkflowException workflowException = execution.getVariable("WorkflowException")
			def errorResponseCode = workflowException.getErrorCode()
			def errorResponseMsg = workflowException.getErrorMessage()
			def encErrorResponseMsg = ""
			if (errorResponseMsg != null) {
				encErrorResponseMsg = errorResponseMsg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
			}

			String content = """
				<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow="http://ecomp.att.com/mso/workflow/schema/v1"
						xmlns:reqtype="http://ecomp.att.com/mso/request/types/v1"
						xmlns:msoservtypes="http://ecomp.att.com/mso/request/types/v1"
						xmlns:structuredtypes="http://ecomp.att.com/mso/structured/types/v1">
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

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 2000, 'Internal Error')
		}
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

			def requestId = execution.getVariable("att-mso-request-id")
			
			if (requestId == null) {
				createWorkflowException(execution, 1002, processKey + " request has no att-mso-request-id")
			}
			
			setVariable(execution, prefix + 'requestId', requestId)

			def serviceInstanceId = execution.getVariable("att-mso-service-instance-id")

			if (serviceInstanceId == null) {
				createWorkflowException(execution, 1002, processKey + " request message has no att-mso-service-instance-id")
			}

			utils.logContext(requestId, serviceInstanceId)
			*/
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
	
}
