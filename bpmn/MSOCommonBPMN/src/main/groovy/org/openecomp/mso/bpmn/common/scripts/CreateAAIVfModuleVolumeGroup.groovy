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
import org.openecomp.mso.rest.APIResponse


public class CreateAAIVfModuleVolumeGroup extends AbstractServiceTaskProcessor {

	private XmlParser xmlParser = new XmlParser()
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(Execution execution) {
		execution.setVariable('prefix', 'CAAIVfModVG_')
		execution.setVariable('CAAIVfModVG_vnfId', null)
		execution.setVariable('CAAIVfModVG_vfModuleId', null)
		execution.setVariable('CAAIVfModVG_aicCloudRegion', null)
		execution.setVariable('CAAIVfModVG_volumeGroupId', null)
		execution.setVariable('CAAIVfModVG_getVfModuleResponseCode' ,null)
		execution.setVariable('CAAIVfModVG_getVfModuleResponse', '')
		execution.setVariable('CAAIVfModVG_updateVfModuleResponseCode', null)
		execution.setVariable('CAAIVfModVG_updateVfModuleResponse', '')
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
			def xml = execution.getVariable('CreateAAIVfModuleVolumeGroupRequest')
			logDebug('Received request xml:\n' + xml, isDebugLogEnabled)
			utils.logAudit("CreateAAIVfModuleVolume Received Request XML: " + xml)
			initProcessVariables(execution)

			def vnfId = getRequiredNodeText(execution, xml,'vnf-id')
			execution.setVariable('CAAIVfModVG_vnfId', vnfId)

			def vfModuleId = getRequiredNodeText(execution, xml,'vf-module-id')
			execution.setVariable('CAAIVfModVG_vfModuleId', vfModuleId)
			
			def aicCloudRegion = getRequiredNodeText(execution, xml,'aic-cloud-region')
			execution.setVariable('CAAIVfModVG_aicCloudRegion', aicCloudRegion)
			
			def volumeGroupId = getRequiredNodeText(execution, xml,'volume-group-id')
			execution.setVariable('CAAIVfModVG_volumeGroupId', volumeGroupId)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())

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
			def vnfId = execution.getVariable('CAAIVfModVG_vnfId')
			def vfModuleId = execution.getVariable('CAAIVfModVG_vfModuleId')

			// Construct endpoint
			AaiUtil aaiUtil = new AaiUtil(this)
			def aai_uri = aaiUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)
			String endPoint = execution.getVariable('URN_aai_endpoint') + aai_uri + '/' + URLEncoder.encode(vnfId, "UTF-8") + '/vf-modules/vf-module/' + URLEncoder.encode(vfModuleId, "UTF-8")

			try {
				logDebug('sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
				utils.logAudit("aaiResponse GET TO AAI Endpoint: " + endPoint)
				APIResponse response = aaiUtil.executeAAIGetCall(execution, endPoint)
				def responseData = response.getResponseBodyAsString()
				execution.setVariable('CAAIVfModVG_getVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('CAAIVfModVG_getVfModuleResponse', responseData)
				
				utils.logAudit("CreateAAIVfModule Response Code: " + response.getStatusCode())
				utils.logAudit("CreateAAIVfModule Response: " + response)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI GET:' + ex.getMessage(),isDebugLogEnabled)
				execution.setVariable('CAAIVfModVG_getVfModuleResponseCode', 500)
				execution.setVariable('CAAIVfModVG_getVfModuleResponse', 'AAI GET Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in getVfModule(): ' + e.getMessage())
		}
	}

	/**
	 * Construct and send a PUT request to AAI to update the VF Module with the
	 * created Volume Group relationship.
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
			def vnfId = execution.getVariable('CAAIVfModVG_vnfId')
			def vfModuleId = execution.getVariable('CAAIVfModVG_vfModuleId')
			def vfModule = execution.getVariable('CAAIVfModVG_getVfModuleResponse')
			def origRequest = execution.getVariable('CreateAAIVfModuleVolumeGroupRequest')
			def Node vfModuleNode = xmlParser.parseText(vfModule)
			
			// Confirm resource-version is in retrieved VF Module
			if (utils.getChildNode(vfModuleNode, 'resource-version') == null) {
				def msg = 'Can\'t update VF Module ' + vfModuleId + ' since \'resource-version\' is missing'
				logError(msg)
				throw new Exception(msg)
			}
						
			// Construct payload by creating a Volume Group relationhip and inserting it into the VF Module
			def aicCloudRegion = execution.getVariable('CAAIVfModVG_aicCloudRegion')
			def volumeGroupId = execution.getVariable('CAAIVfModVG_volumeGroupId')
			def Node vgRelationshipNode = createVolumeGroupRelationshipNode(aicCloudRegion, volumeGroupId)
			insertVolumeGroupRelationshipNode(vfModuleNode, vgRelationshipNode)
			def payload = utils.nodeToString(vfModuleNode)

			// Construct endpoint
			AaiUtil aaiUtil = new AaiUtil(this)
			def aai_uri = aaiUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)
			String endPoint = execution.getVariable('URN_aai_endpoint') + aai_uri + '/' + URLEncoder.encode(vnfId, "UTF-8") + '/vf-modules/vf-module/' + URLEncoder.encode(vfModuleId, "UTF-8")

			try {
				utils.logAudit("CreateAAIVfModuleVolume Sendind PUT to AAI Endpoint \n " + endPoint + " with payload \n " + payload)
				logDebug('sending PUT to AAI endpoint \'' + endPoint + '\'' + 'with payload \n' + payload, isDebugLogEnabled)
				APIResponse response = aaiUtil.executeAAIPutCall(execution, endPoint, payload)
				def responseData = response.getResponseBodyAsString()
				execution.setVariable('CAAIVfModVG_updateVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('CAAIVfModVG_updateVfModuleResponse', responseData)
				
				utils.logAudit("CreateAAIVfModule Response code: " + response.getStatusCode())
				utils.logAudit("CreateAAIVfModule Response: " + responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI PUT:' + ex.getMessage(),isDebugLogEnabled)
				execution.setVariable('CAAIVfModVG_updateVfModuleResponseCode', 500)
				execution.setVariable('CAAIVfModVG_updateVfModuleResponse', 'AAI PUT Failed:' + ex.getMessage())
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
	 * Construct a Volume Group relationship Node with the given AIC Cloud Region and
	 * Volume Group ID for insertion into a VF Module.
	 * 
	 * @param aicCloudRegion Cloud Region ID to use in the Volume Group relationship
	 * @param volumeGroupId Volume Group ID to use in the Volume Group relationship
	 * @return a Node representing the new Volume Group relationship
	 */
	private Node createVolumeGroupRelationshipNode(String aicCloudRegion, String volumeGroupId) {
		
		def Node relatedTo = new Node(null, 'related-to', 'volume-group')
		
		def Node relationshipKeyCO = new Node(null, 'relationship-key', 'cloud-region.cloud-owner')
		def Node relationshipValueCO = new Node(null, 'relationship-value', 'att-aic')
		def Node relationshipDataCO = new Node(null, 'relationship-data')
		relationshipDataCO.append(relationshipKeyCO)
		relationshipDataCO.append(relationshipValueCO)
		
		def Node relationshipKeyCRI = new Node(null, 'relationship-key', 'cloud-region.cloud-region-id')
		def Node relationshipValueCRI = new Node(null, 'relationship-value', aicCloudRegion)
		def Node relationshipDataCRI = new Node(null, 'relationship-data')
		relationshipDataCRI.append(relationshipKeyCRI)
		relationshipDataCRI.append(relationshipValueCRI)

		def Node relationshipKeyVGI = new Node(null, 'relationship-key', 'volume-group.volume-group-id')
		def Node relationshipValueVGI = new Node(null, 'relationship-value', volumeGroupId)
		def Node relationshipDataVGI = new Node(null, 'relationship-data')
		relationshipDataVGI.append(relationshipKeyVGI)
		relationshipDataVGI.append(relationshipValueVGI)
		
		def Node volumeGroupRelationship = new Node(null, 'relationship')
		volumeGroupRelationship.append(relatedTo)
		volumeGroupRelationship.append(relationshipDataCO)
		volumeGroupRelationship.append(relationshipDataCRI)
		volumeGroupRelationship.append(relationshipDataVGI)
		
		return volumeGroupRelationship
	}
	
	/**
	 * Insert the given Volume Group relationship Node into the given VF Module.
	 * If the VF Module does NOT contain a relationship list:
	 * 	- Create a relationship list containing the Volume Group relationship and insert it into the VF Module
	 * If the VF Module contains a relationship list but not a Volume Group relationship:
	 * 	- Insert the the Volume Group relationship into the relationship lsit
	 * If the VF Module contains a relationship list and has a Volume Group relationship:
	 * 	- Replace the existing Volume Group relationship with the new one
	 * @param vfModuleNode
	 * @param volumeGroupRelationshipNode
	 */
	private void insertVolumeGroupRelationshipNode(Node vfModuleNode, Node volumeGroupRelationshipNode) {
		def Node relationshipList = utils.getChildNode(vfModuleNode, 'relationship-list')
		if (relationshipList == null) {
			relationshipList = new Node(null, 'relationship-list')
			relationshipList.append(volumeGroupRelationshipNode)
			vfModuleNode.append(relationshipList)
		} else {
			def Node currVolumeGroupRelationshipNode = getCurrVolumeGroupRelationshipNode(relationshipList)
			if (currVolumeGroupRelationshipNode == null) {
				relationshipList.append(volumeGroupRelationshipNode)
			} else {
				currVolumeGroupRelationshipNode.replaceNode(volumeGroupRelationshipNode)
			}
		}
	}
		
	/**
	 * Find and return the value of the Volume Group ID for the specified VF Module.  If
	 * the value of the Volume Group ID cannot be found for any reason, 'null' is returned.
	 * 
	 * @param vfModuleNode VF Module (as a Node) retrieved from AAI.
	 * @return the value of the Volume Group ID for the specified VF Module.  If the
	 * value of the Volume Group ID cannot be found for any reason, 'null' is returned.
	 */
	private Node getCurrVolumeGroupRelationshipNode(Node relationshipList) {
		def Node currVolumeGroupRelationshipNode = null
		def NodeList relationships = utils.getIdenticalChildren(relationshipList, 'relationship')
		for (Node relationshipNode in relationships) {
			def String relatedTo = utils.getChildNodeText(relationshipNode, 'related-to')
			if ((relatedTo != null) && relatedTo.equals('volume-group')) {
				currVolumeGroupRelationshipNode = relationshipNode
			}
		}
		return currVolumeGroupRelationshipNode
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
			execution.getVariable('CAAIVfModVG_getVfModuleResponseCode') + ', Error Response ' +
			execution.getVariable('CAAIVfModVG_getVfModuleResponse'))
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		exceptionUtil.buildWorkflowException(execution, 5000, execution.getVariable('CAAIVfModVG_getVfModuleResponse'))

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
			execution.getVariable('CAAIVfModVG_updateVfModuleResponseCode') + ', Error Response ' +
			execution.getVariable('CAAIVfModVG_updateVfModuleResponse'))
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		exceptionUtil.buildWorkflowException(execution, 5000, execution.getVariable('CAAIVfModVG_updateVfModuleResponse'))

		logDebug('Exited ' + method, isDebugLogEnabled)
	}
}
