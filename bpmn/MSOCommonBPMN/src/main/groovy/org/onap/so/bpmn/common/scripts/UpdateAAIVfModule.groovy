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
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.rest.APIResponse
import org.springframework.web.util.UriUtils
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger




public class UpdateAAIVfModule extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, UpdateAAIVfModule.class);


	private XmlParser xmlParser = new XmlParser()
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(DelegateExecution execution) {
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
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		try {
			def xml = execution.getVariable('UpdateAAIVfModuleRequest')
			msoLogger.debug('Received request xml:\n' + xml)
			initProcessVariables(execution)

			def vnfId = getRequiredNodeText(execution, xml,'vnf-id')
			execution.setVariable('UAAIVfMod_vnfId', vnfId)

			def vfModuleId = getRequiredNodeText(execution, xml,'vf-module-id')
			execution.setVariable('UAAIVfMod_vfModuleId', vfModuleId)

			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
		}
	}

	/**
	 * Using the received vnfId and vfModuleId, query AAI to get the corresponding VF Module.
	 * A 200 response is expected with the VF Module in the response body.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void getVfModule(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.getVfModule(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('UAAIVfMod_vnfId')
			def vfModuleId = execution.getVariable('UAAIVfMod_vfModuleId')

			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnfId, vfModuleId)
			String endPoint = aaiUriUtil.createAaiUri(uri)
			
			try {
				msoLogger.debug('sending GET to AAI endpoint \'' + endPoint + '\'')
				msoLogger.debug("UpdateAAIVfModule sending GET to AAI endpoint: " + endPoint)

				APIResponse response = aaiUriUtil.executeAAIGetCall(execution, endPoint)
				def responseData = response.getResponseBodyAsString()
				execution.setVariable('UAAIVfMod_getVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('UAAIVfMod_getVfModuleResponse', responseData)
				msoLogger.debug('Response code:' + response.getStatusCode())
				msoLogger.debug('Response:' + System.lineSeparator() + responseData)
				msoLogger.debug("UpdateAAIVfModule response data: " + responseData)
			} catch (Exception ex) {
				ex.printStackTrace()
				msoLogger.debug('Exception occurred while executing AAI GET:' + ex.getMessage())
				execution.setVariable('UAAIVfMod_getVfModuleResponseCode', 500)
				execution.setVariable('UAAIVfMod_getVfModuleResponse', 'AAI GET Failed:' + ex.getMessage())
			}
			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in getVfModule(): ' + e.getMessage())
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
			def vnfId = execution.getVariable('UAAIVfMod_vnfId')
			def vfModuleId = execution.getVariable('UAAIVfMod_vfModuleId')
			def vfModule = execution.getVariable('UAAIVfMod_getVfModuleResponse')
			def origRequest = execution.getVariable('UpdateAAIVfModuleRequest')
			def Node vfModuleNode = xmlParser.parseText(vfModule)
			
			msoLogger.debug("UpdateAAIVfModule request: " + origRequest)
			// Confirm resource-version is in retrieved VF Module
			if (utils.getChildNode(vfModuleNode, 'resource-version') == null) {
				def msg = 'Can\'t update VF Module ' + vfModuleId + ' since \'resource-version\' is missing'
				msoLogger.error(msg);
				throw new Exception(msg)
			}
			
			// Handle persona-model-id/persona-model-version
			def boolean doPersonaModelVersion = true
			def String newPersonaModelId = utils.getNodeText(origRequest, 'persona-model-id')
			def String newPersonaModelVersion = utils.getNodeText(origRequest, 'persona-model-version')
			if ((newPersonaModelId == null) || (newPersonaModelVersion == null)) {
				doPersonaModelVersion = false
			} else {
				// Confirm "new" persona-model-id is same as "current" persona-model-id
				def String currPersonaModelId = utils.getChildNodeText(vfModuleNode, 'model-invariant-id')
				if (currPersonaModelId == null) {
					// check the old attribute name
					currPersonaModelId = utils.getChildNodeText(vfModuleNode, 'model-version-id')
				}
				if (currPersonaModelId == null) {
					currPersonaModelId = ''
				}
				if (!newPersonaModelId.equals(currPersonaModelId)) {
					def msg = 'Can\'t update VF Module ' + vfModuleId + ' since there is \'persona-model-id\' mismatch between the current and new values'
					msoLogger.error(msg)
					throw new Exception(msg)
				}
			}
			
			// Construct payload
			String orchestrationStatusEntry = updateVfModuleNode(origRequest, vfModuleNode, 'orchestration-status')
			String heatStackIdEntry = updateVfModuleNode(origRequest, vfModuleNode, 'heat-stack-id')
			String personaModelVersionEntry = ""
			if (doPersonaModelVersion) {
				personaModelVersionEntry = updateVfModuleNode(origRequest, vfModuleNode, 'persona-model-version')
			}
			String contrailServiceInstanceFqdnEntry = updateVfModuleNode(origRequest, vfModuleNode, 'contrail-service-instance-fqdn')
			def payload = """
					{	${orchestrationStatusEntry}
						${heatStackIdEntry}
						${personaModelVersionEntry}
						${contrailServiceInstanceFqdnEntry}
						"vf-module-id": "${vfModuleId}"						
					}
			"""

			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnfId, vfModuleId)
			String endPoint = aaiUriUtil.createAaiUri(uri)

			try {
				msoLogger.debug('sending PATCH to AAI endpoint \'' + endPoint + '\'' + 'with payload \n' + payload)
				msoLogger.debug("Sending PATCH to AAI endpoint: " + endPoint)

				APIResponse response = aaiUriUtil.executeAAIPatchCall(execution, endPoint, payload)
				def responseData = response.getResponseBodyAsString()
				execution.setVariable('UAAIVfMod_updateVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('UAAIVfMod_updateVfModuleResponse', responseData)
				msoLogger.debug("UpdateAAIVfModule Response data: " + responseData)
				msoLogger.debug('Response code:' + response.getStatusCode())
				msoLogger.debug('Response:' + System.lineSeparator() + responseData)
			} catch (Exception ex) {
				ex.printStackTrace()
				msoLogger.debug('Exception occurred while executing AAI PATCH:' + ex.getMessage())
				execution.setVariable('UAAIVfMod_updateVfModuleResponseCode', 500)
				execution.setVariable('UAAIVfMod_updateVfModuleResponse', 'AAI PATCH Failed:' + ex.getMessage())
			}
			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in updateVfModule(): ' + e.getMessage())
		}
	}

	/**
	 * Sets up json attributes for PATCH request for Update
	 *
	 * @param origRequest Incoming update request with VF Module elements to be updated.
	 * @param vfModule Current VF Module retrieved from AAI.
	 * @param element Name of element to be inserted.
	 */	
	private String updateVfModuleNode(String origRequest, Node vfModuleNode, String elementName) {

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
		def requestVolumeGroupId = utils.getNodeText(origRequest, 'volume-group-id')
		def currVolumeGroupId = getCurrVolumeGroupId(vfModuleNode)
		
		msoLogger.debug('Check volume-group-id: volume-group-id in original request is \'' + requestVolumeGroupId + '\', volume-group-id from VF Module is \'' + currVolumeGroupId + '\'')
		
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
	public void handleAAIQueryFailure(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.handleAAIQueryFailure(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		msoLogger.error( 'Error occurred attempting to query AAI, Response Code ' + execution.getVariable('UAAIVfMod_getVfModuleResponseCode'));
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('UAAIVfMod_getVfModuleResponse'))
		execution.setVariable('WorkflowException', exception)
		msoLogger.debug("UpdateAAIVfModule query failure: " + exception.getErrorMessage())
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

		msoLogger.error('Error occurred attempting to update VF Module in AAI, Response Code ' + execution.getVariable('UAAIVfMod_updateVfModuleResponseCode'));
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('UAAIVfMod_updateVfModuleResponse'))
		execution.setVariable('WorkflowException', exception)

		msoLogger.debug("UpdateAAIVfModule failure: " + exception.getErrorMessage())
		msoLogger.trace('Exited ' + method)
	}
}
