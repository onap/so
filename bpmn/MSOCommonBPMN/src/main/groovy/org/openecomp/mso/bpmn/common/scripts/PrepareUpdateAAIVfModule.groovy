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

package org.openecomp.mso.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.springframework.web.util.UriUtils

public class PrepareUpdateAAIVfModule extends VfModuleBase {
	
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	private MsoUtils utils = new MsoUtils()
	/**
	 * Initialize the flow's variables.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(Execution execution) {
		execution.setVariable('prefix', 'PUAAIVfMod_')
		execution.setVariable('PUAAIVfMod_vnfId', null)
		execution.setVariable('PUAAIVfMod_vfModuleId', null)
		execution.setVariable('PUAAIVfMod_vnfName', null)
		execution.setVariable('PUAAIVfMod_orchestrationStatus', null)
		execution.setVariable('PUAAIVfMod_vfModule', null)
		execution.setVariable('PUAAIVfMod_vfModuleOK', false)
		execution.setVariable('PUAAIVfMod_vfModuleValidationError', null)
		execution.setVariable('PUAAIVfMod_getVnfResponseCode' ,null)
		execution.setVariable('PUAAIVfMod_getVnfResponse', '')
		execution.setVariable('PUAAIVfMod_updateVfModuleResponseCode', null)
		execution.setVariable('PUAAIVfMod_updateVfModuleResponse', '')
		execution.setVariable('PUAAIVfMod_outVfModule', null)
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

		try {
			def xml = execution.getVariable('PrepareUpdateAAIVfModuleRequest')
			logDebug('Received request xml:\n' + xml, isDebugLogEnabled)
			utils.logAudit("PrepareUpdateAAIVfModule Request  : " + xml)
			
			initProcessVariables(execution)
			
			def vnfId = getRequiredNodeText(execution, xml,'vnf-id')
			execution.setVariable('PUAAIVfMod_vnfId', vnfId)

			def vfModuleId = getRequiredNodeText(execution, xml,'vf-module-id')
			execution.setVariable('PUAAIVfMod_vfModuleId', vfModuleId)
			
			def orchestrationStatus = getRequiredNodeText(execution, xml,'orchestration-status')
			execution.setVariable('PUAAIVfMod_orchestrationStatus', orchestrationStatus)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
		}
	}
	
	/**
	 * Using the received vnfId, query AAI to get the corresponding Generic VNF.
	 * A 200 response is expected with the Generic VNF in the response body.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void getGenericVnf(Execution execution) {
		def method = getClass().getSimpleName() + '.getGenericVnf(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('PUAAIVfMod_vnfId')
			
			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)
			
			String endPoint = execution.getVariable("URN_aai_endpoint") + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"
				
			utils.logAudit("PrepareUpdateAAIVfModule: AAI endPoint  : " + endPoint)
			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))
			try {
				RESTConfig config = new RESTConfig(endPoint)
				def responseData = ''
				String aaiRequestId = utils.getRequestID()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml')
				if (basicAuthCred != null && !"".equals(basicAuthCred)) {
					client.addAuthorizationHeader(basicAuthCred)
				}
				logDebug('sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
				APIResponse response = client.httpGet()
				utils.logAudit("PrepareUpdateAAIVfModule: - invoking httpGet to AAI")
				
				responseData = response.getResponseBodyAsString()
				execution.setVariable('PUAAIVfMod_getVnfResponseCode', response.getStatusCode())
				execution.setVariable('PUAAIVfMod_getVnfResponse', responseData)
				
				utils.logAudit("PrepareUpdateAAIVfModule: AAI Response : " + responseData)
				utils.logAudit("PrepareUpdateAAIVfModule: AAI ResponseCode : " + response.getStatusCode())
				
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI GET:' + ex.getMessage(), isDebugLogEnabled)
				execution.setVariable('PUAAIVfMod_getVnfResponseCode', 500)
				execution.setVariable('PUAAIVfMod_getVnfResponse', 'AAI GET Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in getGenericVnf(): ' + e.getMessage())
		}
	}
	
	/**
	 * Validate the VF Module.  That is, confirm that a VF Module with the input VF Module ID
	 * exists in the retrieved Generic VNF.  Then, check to make sure that if that VF Module
	 * is the base VF Module and it's not the only VF Module for this Generic VNF, that we're not
	 * attempting to delete it.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void validateVfModule(Execution execution) {
		def method = getClass().getSimpleName() + '.validateVfModule(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		try {
			def genericVnf = execution.getVariable('PUAAIVfMod_getVnfResponse')
			def vnfId = execution.getVariable('PUAAIVfMod_vnfId')
			def vfModuleId = execution.getVariable('PUAAIVfMod_vfModuleId')
			def vnfName = getNodeTextForce(genericVnf, 'vnf-name')
			execution.setVariable('PUAAIVfMod_vnfName', vnfName)
			def VfModule vfModule = findVfModule(genericVnf, vfModuleId)
			if (vfModule == null) {
				def String msg = 'VF Module \'' + vfModuleId + '\' does not exist in Generic VNF \'' + vnfId + '\''
				execution.setVariable('PUAAIVfMod_vfModuleValidationError', msg)
				execution.setVariable('PUAAIVfMod_vfModuleOK', false)
			} else {
				def orchestrationStatus = execution.getVariable('PUAAIVfMod_orchestrationStatus')
				if (isDebugLogEnabled) {
					logDebug('VF Module \'' + vfModuleId + '\': isBaseVfModule=' + vfModule.isBaseVfModule() +
						', isOnlyVfModule=' + vfModule.isOnlyVfModule() + ', new orchestration-status=' + orchestrationStatus,
						isDebugLogEnabled)
				}
				if (vfModule.isBaseVfModule() && !vfModule.isOnlyVfModule() && orchestrationStatus.equals('pending-delete')) {
					def String msg = 'Orchestration status for VF Module \'' + vfModuleId +
						'\' cannot be set to \'pending-delete\' since it is the base VF Module and it\'s not the only VF Module in Generic VNF \'' + vnfId + '\''
					execution.setVariable('PUAAIVfMod_vfModuleValidationError', msg)
					execution.setVariable('PUAAIVfMod_vfModuleOK', false)
				} else {
					execution.setVariable('PUAAIVfMod_vfModule', vfModule)
					execution.setVariable('PUAAIVfMod_vfModuleOK', true)
				}
			}
			
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in validateVfModule(): ' + e.getMessage())
		}
	}
	
	/**
	 * Construct and send a PATCH request to AAI to update the VF Module.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void updateVfModule(Execution execution) {
		def method = getClass().getSimpleName() + '.updateVfModule(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		try {
			// Construct payload
			def VfModule vfModule = (VfModule) execution.getVariable('PUAAIVfMod_vfModule')
			def Node newVfModuleNode = vfModule.getNode().clone()
			def orchestrationStatus = execution.getVariable('PUAAIVfMod_orchestrationStatus')
			def Node orchestrationStatusNode = utils.getChildNode(newVfModuleNode, 'orchestration-status')
			if (orchestrationStatusNode == null) {
				// Node doesn't exist, this should never happen, right?
				new Node(newVfModuleNode, 'orchestration-status', orchestrationStatus)
			} else {
				// Node already exists, just give it a new value
				orchestrationStatusNode.setValue(orchestrationStatus)
			}
			def VfModule newVfModule = new VfModule(newVfModuleNode, vfModule.isOnlyVfModule())
			//def payload = utils.nodeToString(newVfModuleNode)
					
			// Construct endpoint
			def vnfId = execution.getVariable('PUAAIVfMod_vnfId')
			def vfModuleId = execution.getVariable('PUAAIVfMod_vfModuleId')
			
			def payload = """{
					"vf-module-id": "${vfModuleId}",
					"orchestration-status": "${orchestrationStatus}"
				}"""
			
			utils.logAudit("VfModule payload : " + payload)

			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)
			
			String endPoint = execution.getVariable("URN_aai_endpoint") + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "/vf-modules/vf-module/" + UriUtils.encode(vfModuleId, "UTF-8")
			utils.logAudit("PrepareUpdateAAIVfModule: AAI endPoint  : " + endPoint)
			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))
			try {
				RESTConfig config = new RESTConfig(endPoint)
				def responseData = ''
                def aaiRequestId = utils.getRequestID()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/merge-patch+json').
					addHeader('Accept','application/json')
				if (basicAuthCred != null && !"".equals(basicAuthCred)) {
					client.addAuthorizationHeader(basicAuthCred)
				}
				logDebug('sending PATCH to AAI endpoint \'' + endPoint + '\'' + 'with payload \n' + payload, isDebugLogEnabled)
				APIResponse response = client.httpPatch(payload)
				utils.logAudit("PrepareUpdateAAIVfModule: - invoking httpPatch to AAI")
				utils.logAudit("PrepareUpdateAAIVfModule: - invoking httpPatch to AAI")

				responseData = response.getResponseBodyAsString()
				execution.setVariable('PUAAIVfMod_updateVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('PUAAIVfMod_updateVfModuleResponse', responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
				utils.logAudit("PrepareUpdateAAIVfModule: AAI Response : " + responseData)
				utils.logAudit("PrepareUpdateAAIVfModule: AAI ResponseCode : " + response.getStatusCode())
				
				// Set the output for this flow.  The updated VfModule is an output, the generic VNF name, and for
				// backward compatibilty, the heat-stack-id is an output
				execution.setVariable('PUAAIVfMod_outVfModule', newVfModule)
				def vnfName = execution.getVariable('PUAAIVfMod_vnfName')
				logDebug('Output PUAAIVfMod_vnfName set to ' + vnfName, isDebugLogEnabled)
				// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead
				execution.setVariable('WorkflowResponse', newVfModule)
				logDebug('Output PUAAIVfMod_outVfModule set for VF Module Id \'' + newVfModule.getElementText('vf-module-id') + '\'', isDebugLogEnabled)
				def heatStackId = newVfModule.getElementText('heat-stack-id')
				execution.setVariable('PUAAIVfMod_heatStackId', heatStackId)
				logDebug('Output PUAAIVfMod_heatStackId set to \'' + heatStackId + '\'', isDebugLogEnabled)
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI PUT:' + ex.getMessage(), isDebugLogEnabled)
				execution.setVariable('PUAAIVfMod_updateVfModuleResponseCode', 500)
				execution.setVariable('PUAAIVfMod_updateVfModuleResponse', 'AAI PATCH Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in updateVfModule(): ' + e.getMessage())
		}				
	}
		
	/**
	 * Generates a WorkflowException if the AAI query returns a response code other than 200.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void handleVnfNotFound(Execution execution) {
		def method = getClass().getSimpleName() + '.handleVnfNotFound(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		logError('Error occurred attempting to query AAI, Response Code ' +
			execution.getVariable('PUAAIVfMod_getVnfResponseCode') + ', Error Response ' +
			execution.getVariable('PUAAIVfMod_getVnfResponse'))
		String processKey = getProcessKey(execution)
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('PUAAIVfMod_getVnfResponse'))
		execution.setVariable('WorkflowException', exception)
		
		logDebug('Exited ' + method, isDebugLogEnabled)
	}
	
	/**
	 * Generates a WorkflowException if the VF Module does not pass validation.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void handleVfModuleValidationError(Execution execution) {
		def method = getClass().getSimpleName() + '.handleVfModuleValidationError(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
				
		def String errorMsg = 'VF Module validation error: ' + execution.getVariable('PUAAIVfMod_vfModuleValidationError')
		logError(errorMsg)
		utils.logAudit("PrepareUpdateAAIVfModule: Error Message : " + errorMsg)
		
		String processKey = getProcessKey(execution)
		WorkflowException exception = new WorkflowException(processKey, 5000, errorMsg)
		execution.setVariable('WorkflowException', exception)

		logDebug('Exited ' + method, isDebugLogEnabled)
	}
	
	/**
	 * Generates a WorkflowException if updating a VF Module in AAI returns a response code other than 200.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void handleUpdateVfModuleFailure(Execution execution) {
		def method = getClass().getSimpleName() + '.handleUpdateVfModuleFailure(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		logError('Error occurred attempting to update VF Module in AAI, Response Code ' +
			execution.getVariable('PUAAIVfMod_updateVfModuleResponseCode') + ', Error Response ' +
			execution.getVariable('PUAAIVfMod_updateVfModuleResponse'))
		String processKey = getProcessKey(execution)
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('PUAAIVfMod_updateVfModuleResponse'))
		execution.setVariable('WorkflowException', exception)
		
		logDebug('Exited ' + method, isDebugLogEnabled)
	}
}
