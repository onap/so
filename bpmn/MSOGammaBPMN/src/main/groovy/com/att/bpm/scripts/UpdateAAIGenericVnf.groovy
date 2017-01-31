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

import groovy.util.Node
import groovy.util.XmlParser;
import groovy.xml.QName

import java.io.Serializable;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.springframework.web.util.UriUtils

import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException


public class UpdateAAIGenericVnf extends AbstractServiceTaskProcessor {

	private XmlParser xmlParser = new XmlParser()

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
			initProcessVariables(execution)

			def vnfId = getRequiredNodeText(execution, xml,'vnf-id')
			execution.setVariable('UAAIGenVnf_vnfId', vnfId)

			def personaModelId = getRequiredNodeText(execution, xml,'persona-model-id')
			execution.setVariable('UAAIGenVnf_personaModelId', personaModelId)
			
			def personaModelVersion = getRequiredNodeText(execution, xml,'persona-model-version')
			execution.setVariable('UAAIGenVnf_personaModelVersion', personaModelVersion)
			
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
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
			String endPoint = execution.getVariable('URN_aai_endpoint') + aai_uri + '/' + UriUtils.encode(vnfId, "UTF-8")

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			try {
				RESTConfig config = new RESTConfig(endPoint);
				def responseData = ''
				def aaiRequestId = UUID.randomUUID().toString()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml');
				if (basicAuthCred != null && !"".equals(basicAuthCred)) {
					client.addAuthorizationHeader(basicAuthCred)
				}

				logDebug('sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
				APIResponse response = client.httpGet()

				responseData = response.getResponseBodyAsString()
				execution.setVariable('UAAIGenVnf_getGenericVnfResponseCode', response.getStatusCode())
				execution.setVariable('UAAIGenVnf_getGenericVnfResponse', responseData)
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
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in getGenericVnf(): ' + e.getMessage())
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
			
			// Confirm "new" persona-model-id is same as "current" persona-model-id
			def Node currPersonaModelIdNode = utils.getChildNode(genericVnfNode, 'persona-model-id')
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
			updateGenericVnfNode(origRequest, genericVnfNode, 'persona-model-version')
			def payload = utils.nodeToString(genericVnfNode)

			// Construct endpoint
			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)
			String endPoint = execution.getVariable('URN_aai_endpoint') + aai_uri + '/' + UriUtils.encode(vnfId, "UTF-8")

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			try {
				RESTConfig config = new RESTConfig(endPoint);
				def responseData = ''
				def aaiRequestId = UUID.randomUUID().toString()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml');
				if (basicAuthCred != null && !"".equals(basicAuthCred)) {
					client.addAuthorizationHeader(basicAuthCred)
				}

				logDebug('sending PUT to AAI endpoint \'' + endPoint + '\'' + 'with payload \n' + payload, isDebugLogEnabled)
				APIResponse response = client.httpPut(payload)

				responseData = response.getResponseBodyAsString()
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponseCode', response.getStatusCode())
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponse', responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI PUT:' + ex.getMessage(),isDebugLogEnabled)
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponseCode', 500)
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponse', 'AAI PUT Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in updateGenericVnf(): ' + e.getMessage())
		}
	}

	/**
	 * Insert a new Node, replace the value of an existing Node, or delete an existing Node in the current
	 * Generic VNF Node, as necessary.
	 *
	 * If the Node with the same name already exists in current Generic VNF, but is not being updated, then do
	 * nothing. If the element is being updated and it already exists in the current Generic VNF, then check
	 * the value specified in the original request. If the value is 'DELETE', remove that Node from the
	 * current Generic VNF.  Otherwise, change the value to the specified new value. If the element is
	 * being updated but doesn't exist in the current Generic VNF, and the new value is not 'DELETE', then
	 * create an appropriate new node and add it to the Generic VNF.
	 *
	 * @param origRequest Incoming update request with Generic VNF element(s) to be updated.
	 * @param genericVnf Current Generic VNF retrieved from AAI.
	 * @param element Name of element to be inserted.
	 */
	public void updateGenericVnfNode(String origRequest, Node genericVnfNode, String elementName) {

		if (!utils.nodeExists(origRequest, elementName)) {
			return
		}
		def elementValue = utils.getNodeText(origRequest, elementName)

		def Node childNode = utils.getChildNode(genericVnfNode, elementName)
		if (childNode == null) {
			if (elementValue.equals('DELETE')) {
				// Element doesn't exist but is being deleted, so do nothing
				return
			}
			// Node doesn't exist, create a new Node as a child
			new Node(genericVnfNode, elementName, elementValue)
		} else {
			if (elementValue.equals('DELETE')) {
				// Node exists, but should be deleted
				genericVnfNode.remove(childNode)
			} else {
				// Node already exists, just give it a new value
				childNode.setValue(elementValue)
			}
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
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('UAAIGenVnf_getGenericVnfResponse'))
		execution.setVariable('WorkflowException', exception)

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
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('UAAIGenVnf_updateGenericVnfResponse'))
		execution.setVariable('WorkflowException', exception)

		logDebug('Exited ' + method, isDebugLogEnabled)
	}
}
