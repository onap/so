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

package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.rest.APIResponse
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig
import org.springframework.web.util.UriUtils
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger



public class PrepareUpdateAAIVfModule extends VfModuleBase {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, PrepareUpdateAAIVfModule.class);

	
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	private MsoUtils utils = new MsoUtils()
	/**
	 * Initialize the flow's variables.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(DelegateExecution execution) {
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
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		try {
			def xml = execution.getVariable('PrepareUpdateAAIVfModuleRequest')
			msoLogger.debug('Received request xml:\n' + xml)
			msoLogger.debug("PrepareUpdateAAIVfModule Request  : " + xml)
			
			initProcessVariables(execution)
			
			def vnfId = getRequiredNodeText(execution, xml,'vnf-id')
			execution.setVariable('PUAAIVfMod_vnfId', vnfId)

			def vfModuleId = getRequiredNodeText(execution, xml,'vf-module-id')
			execution.setVariable('PUAAIVfMod_vfModuleId', vfModuleId)
			
			def orchestrationStatus = getRequiredNodeText(execution, xml,'orchestration-status')
			execution.setVariable('PUAAIVfMod_orchestrationStatus', orchestrationStatus)

			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
		}
	}
	
	/**
	 * Using the received vnfId, query AAI to get the corresponding Generic VNF.
	 * A 200 response is expected with the Generic VNF in the response body.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void getGenericVnf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.getGenericVnf(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('PUAAIVfMod_vnfId')
			
			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			msoLogger.debug('AAI URI is: ' + aai_uri)
			
			String endPoint = UrnPropertiesReader.getVariable("aai.endpoint", execution) + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"
				
			msoLogger.debug("PrepareUpdateAAIVfModule: AAI endPoint  : " + endPoint)
			String basicAuthCred = utils.getBasicAuth(UrnPropertiesReader.getVariable("aai.auth", execution),UrnPropertiesReader.getVariable("mso.msoKey", execution))
			try {
				RESTConfig config = new RESTConfig(endPoint);
				def responseData = ''
				String aaiRequestId = utils.getRequestID()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml');
				if (basicAuthCred != null && !"".equals(basicAuthCred)) {
					client.addAuthorizationHeader(basicAuthCred)
				}
				msoLogger.debug('sending GET to AAI endpoint \'' + endPoint + '\'')
				APIResponse response = client.httpGet()
				msoLogger.debug("PrepareUpdateAAIVfModule: - invoking httpGet to AAI")
				
				responseData = response.getResponseBodyAsString()
				execution.setVariable('PUAAIVfMod_getVnfResponseCode', response.getStatusCode())
				execution.setVariable('PUAAIVfMod_getVnfResponse', responseData)
				
				msoLogger.debug("PrepareUpdateAAIVfModule: AAI Response : " + responseData)
				msoLogger.debug("PrepareUpdateAAIVfModule: AAI ResponseCode : " + response.getStatusCode())
				
				msoLogger.debug('Response code:' + response.getStatusCode())
				msoLogger.debug('Response:' + System.lineSeparator() + responseData)
			} catch (Exception ex) {
				msoLogger.error(ex);
				msoLogger.debug('Exception occurred while executing AAI GET:' + ex.getMessage())
				execution.setVariable('PUAAIVfMod_getVnfResponseCode', 500)
				execution.setVariable('PUAAIVfMod_getVnfResponse', 'AAI GET Failed:' + ex.getMessage())
			}
			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(e)
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
	public void validateVfModule(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.validateVfModule(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)
		
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
				msoLogger.debug('VF Module \'' + vfModuleId + '\': isBaseVfModule=' + vfModule.isBaseVfModule() +', isOnlyVfModule=' + vfModule.isOnlyVfModule() + ', new orchestration-status=' + orchestrationStatus)
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
			
			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in validateVfModule(): ' + e.getMessage())
		}
	}
	
	/**
	 * Construct and send a PATCH request to AAI to update the VF Module.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void updateVfModule(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.updateVfModule(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)
		
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
			
			msoLogger.debug("VfModule payload : " + payload)

			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			msoLogger.debug('AAI URI is: ' + aai_uri)
			
			String endPoint = UrnPropertiesReader.getVariable("aai.endpoint", execution) + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "/vf-modules/vf-module/" + UriUtils.encode(vfModuleId, "UTF-8")
			msoLogger.debug("PrepareUpdateAAIVfModule: AAI endPoint  : " + endPoint)
			String basicAuthCred = utils.getBasicAuth(UrnPropertiesReader.getVariable("aai.auth", execution),UrnPropertiesReader.getVariable("mso.msoKey", execution))
			try {
				RESTConfig config = new RESTConfig(endPoint);
				def responseData = ''
                def aaiRequestId = utils.getRequestID()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/merge-patch+json').
					addHeader('Accept','application/json');
				if (basicAuthCred != null && !"".equals(basicAuthCred)) {
					client.addAuthorizationHeader(basicAuthCred)
				}
				msoLogger.debug('sending PATCH to AAI endpoint \'' + endPoint + '\'' + 'with payload \n' + payload)
				APIResponse response = client.httpPatch(payload)
				msoLogger.debug("PrepareUpdateAAIVfModule: - invoking httpPatch to AAI")

				responseData = response.getResponseBodyAsString()
				execution.setVariable('PUAAIVfMod_updateVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('PUAAIVfMod_updateVfModuleResponse', responseData)
				msoLogger.debug('Response code:' + response.getStatusCode())
				msoLogger.debug('Response:' + System.lineSeparator() + responseData)
				msoLogger.debug("PrepareUpdateAAIVfModule: AAI Response : " + responseData)
				msoLogger.debug("PrepareUpdateAAIVfModule: AAI ResponseCode : " + response.getStatusCode())
				
				// Set the output for this flow.  The updated VfModule is an output, the generic VNF name, and for
				// backward compatibilty, the heat-stack-id is an output
				execution.setVariable('PUAAIVfMod_outVfModule', newVfModule)
				def vnfName = execution.getVariable('PUAAIVfMod_vnfName')
				msoLogger.debug('Output PUAAIVfMod_vnfName set to ' + vnfName)
				// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead
				execution.setVariable('WorkflowResponse', newVfModule)
				msoLogger.debug('Output PUAAIVfMod_outVfModule set for VF Module Id \'' + newVfModule.getElementText('vf-module-id') + '\'')
				def heatStackId = newVfModule.getElementText('heat-stack-id')
				execution.setVariable('PUAAIVfMod_heatStackId', heatStackId)
				msoLogger.debug('Output PUAAIVfMod_heatStackId set to \'' + heatStackId + '\'')
			} catch (Exception ex) {
				ex.printStackTrace()
				msoLogger.debug('Exception occurred while executing AAI PUT:' + ex.getMessage())
				execution.setVariable('PUAAIVfMod_updateVfModuleResponseCode', 500)
				execution.setVariable('PUAAIVfMod_updateVfModuleResponse', 'AAI PATCH Failed:' + ex.getMessage())
			}
			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in updateVfModule(): ' + e.getMessage())
		}				
	}
		
	/**
	 * Generates a WorkflowException if the AAI query returns a response code other than 200.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void handleVnfNotFound(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.handleVnfNotFound(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		msoLogger.error('Error occurred attempting to query AAI, Response Code ' + execution.getVariable('PUAAIVfMod_getVnfResponseCode'));
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('PUAAIVfMod_getVnfResponse'))
		execution.setVariable('WorkflowException', exception)
		
		msoLogger.trace('Exited ' + method)
	}
	
	/**
	 * Generates a WorkflowException if the VF Module does not pass validation.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void handleVfModuleValidationError(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.handleVfModuleValidationError(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)
				
		def String errorMsg = 'VF Module validation error: ' + execution.getVariable('PUAAIVfMod_vfModuleValidationError')
		msoLogger.error(errorMsg);
		msoLogger.debug("PrepareUpdateAAIVfModule: Error Message : " + errorMsg)
		
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000, errorMsg)
		execution.setVariable('WorkflowException', exception)

		msoLogger.trace('Exited ' + method)
	}
	
	/**
	 * Generates a WorkflowException if updating a VF Module in AAI returns a response code other than 200.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void handleUpdateVfModuleFailure(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.handleUpdateVfModuleFailure(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		msoLogger.error('Error occurred attempting to update VF Module in AAI, Response Code ' + execution.getVariable('PUAAIVfMod_updateVfModuleResponseCode'));
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('PUAAIVfMod_updateVfModuleResponse'))
		execution.setVariable('WorkflowException', exception)
		
		msoLogger.trace('Exited ' + method)
	}
}
