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
import org.springframework.web.util.UriUtils


public class UpdateAAIGenericVnf extends AbstractServiceTaskProcessor {

	private XmlParser xmlParser = new XmlParser()
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(Execution execution) {
		execution.setVariable('prefix', 'UAAIGenVnf_')
		execution.setVariable('UAAIGenVnf_vnfId', null)
		execution.setVariable('UAAIGenVnf_personaModelId', null)
		execution.setVariable('UAAIGenVnf_personaModelVersion', null)
		execution.setVariable("UAAIGenVnf_ipv4OamAddress", null)
		execution.setVariable('UAAIGenVnf_managementV6Address', null)
		execution.setVariable('UAAIGenVnf_orchestrationStatus', null)
		execution.setVariable('UAAIGenVnf_getGenericVnfResponseCode' ,null)
		execution.setVariable('UAAIGenVnf_getGenericVnfResponse', '')
		execution.setVariable('UAAIGenVnf_updateGenericVnfResponseCode', null)
		execution.setVariable('UAAIGenVnf_updateGenericVnfResponse', '')
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
			def xml = execution.getVariable('UpdateAAIGenericVnfRequest')
			logDebug('Received request xml:\n' + xml, isDebugLogEnabled)
			utils.logAudit("UpdateAAIGenericVnf Request XML: " + xml)
			initProcessVariables(execution)

			def vnfId = getRequiredNodeText(execution, xml,'vnf-id')
			execution.setVariable('UAAIGenVnf_vnfId', vnfId)

			def personaModelId = getNodeTextForce(xml,'persona-model-id')
			if (personaModelId != null && !personaModelId.isEmpty()) {
				execution.setVariable('UAAIGenVnf_personaModelId', personaModelId)
			}

			def personaModelVersion = getNodeTextForce(xml,'persona-model-version')
			if (personaModelVersion != null && !personaModelVersion.isEmpty()) {
				execution.setVariable('UAAIGenVnf_personaModelVersion', personaModelVersion)
			}

			def ipv4OamAddress = getNodeTextForce(xml, 'ipv4-oam-address')
			if (ipv4OamAddress != null && !ipv4OamAddress.isEmpty()) {
				execution.setVariable('UAAIGenVnf_ipv4OamAddress', ipv4OamAddress)
			}

			def managementV6Address = getNodeTextForce(xml, 'management-v6-address')
			if (managementV6Address != null && !managementV6Address.isEmpty()) {
				execution.setVariable('UAAIGenVnf_managementV6Address', managementV6Address)
			}
			
			def orchestrationStatus = getNodeTextForce(xml, 'orchestration-status')
			if (orchestrationStatus != null && !orchestrationStatus.isEmpty()) {
				execution.setVariable('UAAIGenVnf_orchestrationStatus', orchestrationStatus)
			}

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
	 * A 200 response is expected with the VF Module in the response body.
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
			def vnfId = execution.getVariable('UAAIGenVnf_vnfId')

			// Construct endpoint
			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)
			String endPoint = execution.getVariable('URN_aai_endpoint') + aai_uri + '/' + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"

			try {
				logDebug('sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
				utils.logAudit("Sending GET to AAI endpoint: " + endPoint)

				APIResponse response = aaiUriUtil.executeAAIGetCall(execution, endPoint)
				def responseData = response.getResponseBodyAsString()
				execution.setVariable('UAAIGenVnf_getGenericVnfResponseCode', response.getStatusCode())
				execution.setVariable('UAAIGenVnf_getGenericVnfResponse', responseData)
				utils.logAudit("UpdateAAIGenericVnf Response data: " + responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI GET:' + ex.getMessage(),isDebugLogEnabled)
				execution.setVariable('UAAIGenVnf_getGenericVnfResponseCode', 500)
				execution.setVariable('UAAIGenVnf_getGenericVnfResponse', 'AAI GET Failed:' + ex.getMessage())
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
	 * Construct and send a PUT request to AAI to update the Generic VNF.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void updateGenericVnf(Execution execution) {
		def method = getClass().getSimpleName() + '.updateGenericVnf(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('UAAIGenVnf_vnfId')
			def genericVnf = execution.getVariable('UAAIGenVnf_getGenericVnfResponse')
			def origRequest = execution.getVariable('UpdateAAIGenericVnfRequest')

			utils.logAudit("UpdateGenericVnf Request: " + origRequest)
			// Confirm resource-version is in retrieved Generic VNF
			def Node genericVnfNode = xmlParser.parseText(genericVnf)
			if (utils.getChildNode(genericVnfNode, 'resource-version') == null) {
				def msg = 'Can\'t update Generic VNF ' + vnfId + ' since \'resource-version\' is missing'
				logError(msg)
				throw new Exception(msg)
			}

			// Handle persona-model-id/persona-model-version

			def String newPersonaModelId = execution.getVariable('UAAIGenVnf_personaModelId')
			def String newPersonaModelVersion = execution.getVariable('UAAIGenVnf_personaModelVersion')
			def String personaModelVersionEntry = ""
			if (newPersonaModelId != null || newPersonaModelVersion != null) {

				// Confirm "new" persona-model-id is same as "current" persona-model-id
				def Node currPersonaModelIdNode = utils.getChildNode(genericVnfNode, 'model-invariant-id')
				if (currPersonaModelIdNode == null) {
					// check the old attribute name
					currPersonaModelIdNode = utils.getChildNode(genericVnfNode, 'persona-model-id')
				}
				def String currPersonaModelId = ''
				if (currPersonaModelIdNode != null) {
					currPersonaModelId = currPersonaModelIdNode.text()
				}
				if (!newPersonaModelId.equals(currPersonaModelId)) {
					def msg = 'Can\'t update Generic VNF ' + vnfId + ' since there is \'persona-model-id\' mismatch between the current and new values'
					logError(msg)
					throw new Exception(msg)
				}

				// Construct payload
				personaModelVersionEntry = updateGenericVnfNode(origRequest, genericVnfNode, 'model-version-id')
			}

			// Handle ipv4-oam-address
			def String ipv4OamAddress = execution.getVariable('UAAIGenVnf_ipv4OamAddress')
			def String ipv4OamAddressEntry = ""
			if (ipv4OamAddress != null) {
				// Construct payload
				ipv4OamAddressEntry = updateGenericVnfNode(origRequest, genericVnfNode, 'ipv4-oam-address')
			}

			// Handle management-v6-address
			def String managementV6Address = execution.getVariable('UAAIGenVnf_managementV6Address')
			def String managementV6AddressEntry = ""
			if (managementV6Address != null) {
				// Construct payload
				managementV6AddressEntry = updateGenericVnfNode(origRequest, genericVnfNode, 'management-v6-address')
			}
			
			// Handle orchestration-status
			def String orchestrationStatus = execution.getVariable('UAAIGenVnf_orchestrationStatus')
			def String orchestrationStatusEntry = ""
			if (orchestrationStatus != null) {
				// Construct payload
				orchestrationStatusEntry = updateGenericVnfNode(origRequest, genericVnfNode, 'orchestration-status')
			}

			def payload = """
					{	${personaModelVersionEntry}
						${ipv4OamAddressEntry}
						${managementV6AddressEntry}
						${orchestrationStatusEntry}
						"vnf-id": "${vnfId}"					
					}
			"""

			// Construct endpoint
			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)
			String endPoint = execution.getVariable('URN_aai_endpoint') + aai_uri + '/' + UriUtils.encode(vnfId, "UTF-8")

			try {
				logDebug('sending PATCH to AAI endpoint \'' + endPoint + '\'' + 'with payload \n' + payload, isDebugLogEnabled)
				utils.logAudit("Sending PATCH to AAI endpoint: " + endPoint)

				APIResponse response = aaiUriUtil.executeAAIPatchCall(execution, endPoint, payload)
				def responseData = response.getResponseBodyAsString()
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponseCode', response.getStatusCode())
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponse', responseData)
				utils.logAudit("UpdateAAIGenericVnf Response Data: " + responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI PATCH:' + ex.getMessage(),isDebugLogEnabled)
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponseCode', 500)
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponse', 'AAI PATCH Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in updateGenericVnf(): ' + e.getMessage())
		}
	}

	/**
	 * Sets up json attributes for PATCH request for Update
	 * 
	 * @param origRequest Incoming update request with Generic VNF element(s) to be updated.
	 * @param genericVnf Current Generic VNF retrieved from AAI.
	 * @param element Name of element to be inserted.
	 */
	public String updateGenericVnfNode(String origRequest, Node genericVnfNode, String elementName) {

		if (!utils.nodeExists(origRequest, elementName)) {
			return ""
		}
		def elementValue = utils.getNodeText(origRequest, elementName)

		if (elementValue.equals('DELETE')) {
			// Set the element being deleted to null
			return """"${elementName}": null,"""
		}
		else {
			return """"${elementName}": "${elementValue}","""		
		}
		
	}

	/**
	 * Generates a WorkflowException if the AAI query returns a response code other than 200.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void handleAAIQueryFailure(Execution execution) {
		def method = getClass().getSimpleName() + '.handleAAIQueryFailure(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		logError('Error occurred attempting to query AAI, Response Code ' +
			execution.getVariable('UAAIGenVnf_getGenericVnfResponseCode') + ', Error Response ' +
			execution.getVariable('UAAIGenVnf_getGenericVnfResponse'))
		String processKey = getProcessKey(execution)
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('UAAIGenVnf_getGenericVnfResponse'))
		execution.setVariable('WorkflowException', exception)

		utils.logAudit("Workflow Exception occurred when handling Quering AAI: " + exception.getErrorMessage())
		logDebug('Exited ' + method, isDebugLogEnabled)
	}

	/**
	 * Generates a WorkflowException if updating a VF Module in AAI returns a response code other than 200.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void handleUpdateGenericVnfFailure(Execution execution) {
		def method = getClass().getSimpleName() + '.handleUpdateGenericVnfFailure(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		logError('Error occurred attempting to update Generic VNF in AAI, Response Code ' +
			execution.getVariable('UAAIGenVnf_updateGenericVnfResponseCode') + ', Error Response ' +
			execution.getVariable('UAAIGenVnf_updateGenericVnfResponse'))

		String processKey = getProcessKey(execution)
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('UAAIGenVnf_updateGenericVnfResponse'))
		execution.setVariable('WorkflowException', exception)

		utils.logAudit("Workflow Exception occurred when Updating GenericVnf: " + exception.getErrorMessage())
		logDebug('Exited ' + method, isDebugLogEnabled)
	}
}