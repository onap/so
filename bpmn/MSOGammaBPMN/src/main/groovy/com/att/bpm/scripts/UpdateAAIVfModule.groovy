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


public class UpdateAAIVfModule extends AbstractServiceTaskProcessor {

	private XmlParser xmlParser = new XmlParser()

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(Execution execution) {
		execution.setVariable('prefix', 'UAAIVfMod_')
		execution.setVariable('UAAIVfMod_vnfId', null)
		execution.setVariable('UAAIVfMod_vfModuleId', null)
		execution.setVariable('UAAIVfMod_orchestrationStatus', null)
		execution.setVariable('UAAIVfMod_heatStackId', null)
		execution.setVariable('UAAIVfMod_volumeGroupId', null)
		execution.setVariable('UAAIVfMod_getVfModuleResponseCode' ,null)
		execution.setVariable('UAAIVfMod_getVfModuleResponse', '')
		execution.setVariable('UAAIVfMod_updateVfModuleResponseCode', null)
		execution.setVariable('UAAIVfMod_updateVfModuleResponse', '')
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
			def xml = execution.getVariable('UpdateAAIVfModuleRequest')
			logDebug('Received request xml:\n' + xml, isDebugLogEnabled)
			initProcessVariables(execution)

			def vnfId = getRequiredNodeText(execution, xml,'vnf-id')
			execution.setVariable('UAAIVfMod_vnfId', vnfId)

			def vfModuleId = getRequiredNodeText(execution, xml,'vf-module-id')
			execution.setVariable('UAAIVfMod_vfModuleId', vfModuleId)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
		}
	}

	/**
	 * Using the received vnfId and vfModuleId, query AAI to get the corresponding VF Module.
	 * A 200 response is expected with the VF Module in the response body.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void getVfModule(Execution execution) {
		def method = getClass().getSimpleName() + '.getVfModule(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('UAAIVfMod_vnfId')
			def vfModuleId = execution.getVariable('UAAIVfMod_vfModuleId')

			// Construct endpoint
			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)
			String endPoint = execution.getVariable('URN_aai_endpoint') + aai_uri + '/' + UriUtils.encode(vnfId, "UTF-8") + '/vf-modules/vf-module/' + UriUtils.encode(vfModuleId, "UTF-8")

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
				execution.setVariable('UAAIVfMod_getVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('UAAIVfMod_getVfModuleResponse', responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI GET:' + ex.getMessage(),isDebugLogEnabled)
				execution.setVariable('UAAIVfMod_getVfModuleResponseCode', 500)
				execution.setVariable('UAAIVfMod_getVfModuleResponse', 'AAI GET Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in getVfModule(): ' + e.getMessage())
		}
	}

	/**
	 * Construct and send a PUT request to AAI to update the VF Module.
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
			def vnfId = execution.getVariable('UAAIVfMod_vnfId')
			def vfModuleId = execution.getVariable('UAAIVfMod_vfModuleId')
			def vfModule = execution.getVariable('UAAIVfMod_getVfModuleResponse')
			def origRequest = execution.getVariable('UpdateAAIVfModuleRequest')
			def Node vfModuleNode = xmlParser.parseText(vfModule)
			
			// Confirm resource-version is in retrieved VF Module
			if (utils.getChildNode(vfModuleNode, 'resource-version') == null) {
				def msg = 'Can\'t update VF Module ' + vfModuleId + ' since \'resource-version\' is missing'
				logError(msg)
				throw new Exception(msg)
			}
			
			// TEMPORARY!!! Disable Volume Group Check
			// Check volume-group-id conditions
			//def checkVgiResult = checkVolumeGroupId(origRequest, vfModuleNode, isDebugLogEnabled)
			//if (checkVgiResult != null) {
			//	def msg = 'Can\'t update VF Module ' + vfModuleId + ': ' + checkVgiResult
			//	logError(msg)
			//	throw new Exception(msg)
			//}
			
			// Handle persona-model-id/persona-model-version
			def boolean doPersonaModelVersion = true
			def String newPersonaModelId = utils.getNodeText1(origRequest, 'persona-model-id')
			def String newPersonaModelVersion = utils.getNodeText1(origRequest, 'persona-model-version')
			if ((newPersonaModelId == null) || (newPersonaModelVersion == null)) {
				doPersonaModelVersion = false
			} else {
				// Confirm "new" persona-model-id is same as "current" persona-model-id
				def String currPersonaModelId = utils.getChildNodeText(vfModuleNode, 'persona-model-id')
				if (currPersonaModelId == null) {
					currPersonaModelId = ''
				}
				if (!newPersonaModelId.equals(currPersonaModelId)) {
					def msg = 'Can\'t update VF Module ' + vfModuleId + ' since there is \'persona-model-id\' mismatch between the current and new values'
					logError(msg)
					throw new Exception(msg)
				}
			}
			
			// Construct payload
			updateVfModuleNode(origRequest, vfModuleNode, 'orchestration-status')
			updateVfModuleNode(origRequest, vfModuleNode, 'heat-stack-id')
			if (doPersonaModelVersion) {
				updateVfModuleNode(origRequest, vfModuleNode, 'persona-model-version')
			}
			updateVfModuleNode(origRequest, vfModuleNode, 'contrail-service-instance-fqdn')
			def payload = utils.nodeToString(vfModuleNode)

			// Construct endpoint
			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)
			String endPoint = execution.getVariable('URN_aai_endpoint') + aai_uri + '/' + UriUtils.encode(vnfId, "UTF-8") + '/vf-modules/vf-module/' + UriUtils.encode(vfModuleId, "UTF-8")

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
				execution.setVariable('UAAIVfMod_updateVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('UAAIVfMod_updateVfModuleResponse', responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI PUT:' + ex.getMessage(),isDebugLogEnabled)
				execution.setVariable('UAAIVfMod_updateVfModuleResponseCode', 500)
				execution.setVariable('UAAIVfMod_updateVfModuleResponse', 'AAI PUT Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in updateVfModule(): ' + e.getMessage())
		}
	}

	/**
	 * Insert a new Node, replace the value of an existing Node, or delete an existing Node in the current
	 * VF Module Node, as necessary.
	 *
	 * If the Node with the same name already exists in current VF Module, but is not being updated, then do
	 * nothing. If the element is being updated and it already exists in the current VF Module, then check
	 * the value specified in the original request. If the value is 'DELETE', remove that Node from the
	 * current VF Module.  Otherwise, change the value to the specified new value. If the element is
	 * being updated but doesn't exist in the current VF Module, and the new value is not 'DELETE', then
	 * create an appropriate new node and add it to the VF Module.
	 *
	 * @param origRequest Incoming update request with VF Module elements to be updated.
	 * @param vfModule Current VF Module retrieved from AAI.
	 * @param element Name of element to be inserted.
	 */
	private void updateVfModuleNode(String origRequest, Node vfModuleNode, String elementName) {

		if (!utils.nodeExists(origRequest, elementName)) {
			return
		}
		def elementValue = utils.getNodeText(origRequest, elementName)

		def Node childNode = utils.getChildNode(vfModuleNode, elementName)
		if (childNode == null) {
			if (elementValue.equals('DELETE')) {
				// Element doesn't exist but is being deleted, so do nothing
				return
			}
			// Node doesn't exist, create a new Node as a child
			new Node(vfModuleNode, elementName, elementValue)
		} else {
			if (elementValue.equals('DELETE')) {
				// Node exists, but should be deleted
				vfModuleNode.remove(childNode)
			} else {
				// Node already exists, just give it a new value
				childNode.setValue(elementValue)
			}
		}
	}
	
	/**
	 * Check the Volume Group ID from the incoming update request against the Volume Group ID from the
	 * given VF Module.  If they are equal or if they are both 'null', then that is acceptable and 'null'
	 * is returned.  Otherwise a message describing how the values are unacceptable/incompatible is returned.
	 * 
	 * @param origRequest Incoming update request with VF Module elements to be updated.
	 * @param vfModuleNode VF Module (as a Node) retrieved from AAI.
	 * @param isDebugLogEnabled Is DEBUG log enabled?
	 * @return 'null' if the Volume Group IDs are acceptable. Otherwise return a message describing how the
	 * values are unacceptable/incompatible.
	 */
	private String checkVolumeGroupId(String origRequest, Node vfModuleNode, String isDebugLogEnabled) {
		def requestVolumeGroupId = utils.getNodeText1(origRequest, 'volume-group-id')
		def currVolumeGroupId = getCurrVolumeGroupId(vfModuleNode)
		
		logDebug('Check volume-group-id: volume-group-id in original request is \'' + requestVolumeGroupId +
			'\', volume-group-id from VF Module is \'' + currVolumeGroupId + '\'', isDebugLogEnabled)
		
		def result = null
		
		if (requestVolumeGroupId == null) {
			if (currVolumeGroupId == null) {
				// This is OK
			} else {
				result = 'Cannot detach a volume group from an existing VF Module'
			}
		} else {
			if (currVolumeGroupId == null) {
				result = 'Cannot add a volume gruop to an existing VF Module'
			} else {
				if (!requestVolumeGroupId.equals(currVolumeGroupId)) {
					result = 'Cannot change the volume group on an existing VF Module'
				}
			}
		}
		
		return result
	}
	
	/**
	 * Find and return the value of the Volume Group ID for the specified VF Module.  If
	 * the value of the Volume Group ID cannot be found for any reason, 'null' is returned.
	 * 
	 * @param vfModuleNode VF Module (as a Node) retrieved from AAI.
	 * @return the value of the Volume Group ID for the specified VF Module.  If the
	 * value of the Volume Group ID cannot be found for any reason, 'null' is returned.
	 */
	private String getCurrVolumeGroupId(Node vfModuleNode) {
		def Node relationshipList = utils.getChildNode(vfModuleNode, 'relationship-list')
		if (relationshipList == null) {
			return null
		}
		def NodeList relationships = utils.getIdenticalChildren(relationshipList, 'relationship')
		for (Node relationshipNode in relationships) {
			def String relatedTo = utils.getChildNodeText(relationshipNode, 'related-to')
			if ((relatedTo != null) && relatedTo.equals('volume-group')) {
				def NodeList relationshipDataList = utils.getIdenticalChildren(relationshipNode, 'relationship-data')
				for (Node relationshipDataNode in relationshipDataList) {
					def String relationshipKey = utils.getChildNodeText(relationshipDataNode, 'relationship-key')
					if ((relationshipKey != null) && relationshipKey.equals('volume-group.volume-group-id')) {
						return utils.getChildNodeText(relationshipDataNode, 'relationship-value')
					}
				}
			}
		}
		return null
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
			execution.getVariable('UAAIVfMod_getVfModuleResponseCode') + ', Error Response ' +
			execution.getVariable('UAAIVfMod_getVfModuleResponse'))
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('UAAIVfMod_getVfModuleResponse'))
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
			execution.getVariable('UAAIVfMod_updateVfModuleResponseCode') + ', Error Response ' +
			execution.getVariable('UAAIVfMod_updateVfModuleResponse'))
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('UAAIVfMod_updateVfModuleResponse'))
		execution.setVariable('WorkflowException', exception)

		logDebug('Exited ' + method, isDebugLogEnabled)
	}
}
