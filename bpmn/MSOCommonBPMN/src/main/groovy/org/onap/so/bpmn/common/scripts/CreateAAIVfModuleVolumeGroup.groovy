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

package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class CreateAAIVfModuleVolumeGroup extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( CreateAAIVfModuleVolumeGroup.class);

	private XmlParser xmlParser = new XmlParser()
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(DelegateExecution execution) {
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
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		try {
			def xml = execution.getVariable('CreateAAIVfModuleVolumeGroupRequest')
			logger.debug('Received request xml:\n' + xml)
			logger.debug("CreateAAIVfModuleVolume Received Request XML: " + xml)
			initProcessVariables(execution)

			def vnfId = getRequiredNodeText(execution, xml,'vnf-id')
			execution.setVariable('CAAIVfModVG_vnfId', vnfId)

			def vfModuleId = getRequiredNodeText(execution, xml,'vf-module-id')
			execution.setVariable('CAAIVfModVG_vfModuleId', vfModuleId)

			def aicCloudRegion = getRequiredNodeText(execution, xml,'aic-cloud-region')
			execution.setVariable('CAAIVfModVG_aicCloudRegion', aicCloudRegion)

			def cloudOwner = getRequiredNodeText(execution, xml,'cloud-owner')
			execution.setVariable('CAAIVfModVG_cloudOwner', cloudOwner)

			def volumeGroupId = getRequiredNodeText(execution, xml,'volume-group-id')
			execution.setVariable('CAAIVfModVG_volumeGroupId', volumeGroupId)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
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
		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('CAAIVfModVG_vnfId')
			def vfModuleId = execution.getVariable('CAAIVfModVG_vfModuleId')
			try {
				AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
				Optional<org.onap.aai.domain.yang.VfModule> vfModule = getAAIClient().get(org.onap.aai.domain.yang.VfModule.class, resourceUri)
				if(vfModule.isPresent()){
					execution.setVariable('CAAIVfModVG_getVfModuleResponseCode', 200)
					execution.setVariable('CAAIVfModVG_getVfModuleResponse', vfModule.get())
				}else{
					execution.setVariable('CAAIVfModVG_getVfModuleResponseCode', 404)
					execution.setVariable('CAAIVfModVG_getVfModuleResponse', "VF-Module Not found!!")
				}
			}catch (Exception ex) {
				logger.debug('Exception occurred while executing AAI GET: {}', ex.getMessage(), ex)
				execution.setVariable('CAAIVfModVG_getVfModuleResponseCode', 500)
				execution.setVariable('CAAIVfModVG_getVfModuleResponse', 'AAI GET Failed:' + ex.getMessage())
			}
			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in getVfModule(): ' + e.getMessage())
		}
	}

	/**
	 * Construct and send a PUT request to AAI to update the VF Module with the
	 * created Volume Group relationship.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void updateVfModule(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.updateVfModule(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('CAAIVfModVG_vnfId')
			def vfModuleId = execution.getVariable('CAAIVfModVG_vfModuleId')
			org.onap.aai.domain.yang.VfModule vfModule = execution.getVariable('CAAIVfModVG_getVfModuleResponse')

			// Confirm resource-version is in retrieved VF Module
			if (vfModule.getResourceVersion() == null) {
				def msg = 'Can\'t update VF Module ' + vfModuleId + ' since \'resource-version\' is missing'
				logger.error(msg);
				throw new Exception(msg)
			}

			// Construct payload by creating a Volume Group relationhip and inserting it into the VF Module
			def aicCloudRegion = execution.getVariable('CAAIVfModVG_aicCloudRegion')
			def cloudOwner = execution.getVariable('CAAIVfModVG_cloudOwner')
			def volumeGroupId = execution.getVariable('CAAIVfModVG_volumeGroupId')

			try {
				AAIResourceUri vfModuleUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
				AAIResourceUri volumeGroupUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(cloudOwner, aicCloudRegion).volumeGroup(volumeGroupId));
				logger.debug("Creating relationship between Vf Module: " + vfModuleUri.build().toString() + " and Volume Group: " + volumeGroupUri.build().toString())
				getAAIClient().connect(vfModuleUri,volumeGroupUri)
				execution.setVariable('CAAIVfModVG_updateVfModuleResponseCode', 200)
				execution.setVariable('CAAIVfModVG_updateVfModuleResponse', "Success")
				logger.debug("CreateAAIVfModule Response code: " + 200)
				logger.debug("CreateAAIVfModule Response: " + "Success")
			} catch (Exception ex) {
				logger.debug('Exception occurred while executing AAI PUT: {}', ex.getMessage(), ex)
				execution.setVariable('CAAIVfModVG_updateVfModuleResponseCode', 500)
				execution.setVariable('CAAIVfModVG_updateVfModuleResponse', 'AAI PUT Failed:' + ex.getMessage())
			}
			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in updateVfModule(): ' + e.getMessage())
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
	public void handleAAIQueryFailure(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.handleAAIQueryFailure(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)
		logger.error('Error occurred attempting to query AAI, Response Code ' + execution.getVariable('CAAIVfModVG_getVfModuleResponseCode'));
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		exceptionUtil.buildWorkflowException(execution, 5000, execution.getVariable('CAAIVfModVG_getVfModuleResponse'))

		logger.trace('Exited ' + method)
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
		logger.trace('Entered ' + method)

		logger.error('Error occurred attempting to update VF Module in AAI, Response Code ' + execution.getVariable('CAAIVfModVG_updateVfModuleResponseCode'));
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		exceptionUtil.buildWorkflowException(execution, 5000, execution.getVariable('CAAIVfModVG_updateVfModuleResponse'))

		logger.trace('Exited ' + method)
	}
}
