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

import jakarta.ws.rs.NotFoundException

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.WorkflowException
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory


public class UpdateAAIVfModule extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( UpdateAAIVfModule.class);


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
		logger.trace('Entered ' + method)

		try {
			def xml = execution.getVariable('UpdateAAIVfModuleRequest')
			logger.debug('Received request xml:\n' + xml)
			initProcessVariables(execution)

			def vnfId = getRequiredNodeText(execution, xml,'vnf-id')
			execution.setVariable('UAAIVfMod_vnfId', vnfId)

			def vfModuleId = getRequiredNodeText(execution, xml,'vf-module-id')
			execution.setVariable('UAAIVfMod_vfModuleId', vfModuleId)

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
			def vnfId = execution.getVariable('UAAIVfMod_vnfId')
			def vfModuleId = execution.getVariable('UAAIVfMod_vfModuleId')
			try {
				AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
				Optional<org.onap.aai.domain.yang.VfModule> vfModule = getAAIClient().get(org.onap.aai.domain.yang.VfModule.class, resourceUri)
				if (vfModule.isPresent()) {
					execution.setVariable('UAAIVfMod_getVfModuleResponseCode', 200)
					execution.setVariable('UAAIVfMod_getVfModuleResponse', vfModule.get())
				} else {
					execution.setVariable('UAAIVfMod_getVfModuleResponseCode', 404)
					execution.setVariable('UAAIVfMod_getVfModuleResponse', "VF Module not found in AAI")
				}
			} catch (Exception ex) {
				logger.debug('Exception occurred while executing AAI GET:' + ex.getMessage())
				execution.setVariable('UAAIVfMod_getVfModuleResponseCode', 500)
				execution.setVariable('UAAIVfMod_getVfModuleResponse', 'AAI GET Failed:' + ex.getMessage())
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
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
		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('UAAIVfMod_vnfId')
			def vfModuleId = execution.getVariable('UAAIVfMod_vfModuleId')
			org.onap.aai.domain.yang.VfModule vfModule = execution.getVariable('UAAIVfMod_getVfModuleResponse')
			def origRequest = execution.getVariable('UpdateAAIVfModuleRequest')

			logger.debug("UpdateAAIVfModule request: " + origRequest)
			// Handle persona-model-id/persona-model-version
			def boolean doPersonaModelVersion = true
			def String newPersonaModelId = utils.getNodeText(origRequest, 'persona-model-id')
			def String newPersonaModelVersion = utils.getNodeText(origRequest, 'persona-model-version')
			if ((newPersonaModelId == null) || (newPersonaModelVersion == null)) {
				doPersonaModelVersion = false
			} else {
				// Confirm "new" persona-model-id is same as "current" persona-model-id
				def String currPersonaModelId = vfModule.getModelInvariantId()
				if (currPersonaModelId == null) {
					// check the old attribute name
					currPersonaModelId = vfModule.getModelVersionId()
				}
				if (currPersonaModelId == null) {
					currPersonaModelId = ''
				}
				if (!newPersonaModelId.equals(currPersonaModelId)) {
					def msg = 'Can\'t update VF Module ' + vfModuleId + ' since there is \'persona-model-id\' mismatch between the current and new values'
					logger.error(msg)
					throw new Exception(msg)
				}
			}

			// Construct payload
			String orchestrationStatusEntry = updateVfModuleNode(origRequest , 'orchestration-status')
			String heatStackIdEntry = updateVfModuleNode(origRequest,  'heat-stack-id')
			String personaModelVersionEntry = ""
			if (doPersonaModelVersion) {
				personaModelVersionEntry = updateVfModuleNode(origRequest,  'persona-model-version')
			}
			String contrailServiceInstanceFqdnEntry = updateVfModuleNode(origRequest,  'contrail-service-instance-fqdn')
			org.onap.aai.domain.yang.VfModule payload = new org.onap.aai.domain.yang.VfModule();
			payload.setVfModuleId(vfModuleId)
			payload.setOrchestrationStatus(orchestrationStatusEntry)
			payload.setHeatStackId(heatStackIdEntry)
			payload.setPersonaModelVersion(personaModelVersionEntry)
			payload.setContrailServiceInstanceFqdn(contrailServiceInstanceFqdnEntry)

            try {
                AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId))
                getAAIClient().update(resourceUri, payload)
				execution.setVariable('UAAIVfMod_updateVfModuleResponseCode', 200)
				execution.setVariable('UAAIVfMod_updateVfModuleResponse', "Success")
            }catch(NotFoundException ignored){
                logger.debug("VF-Module not found!!")
				execution.setVariable('UAAIVfMod_updateVfModuleResponseCode', 404)
                execution.setVariable('UAAIVfMod_updateVfModuleResponse', ignored.getMessage())
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "vf-module " + vfModuleId + " not found for under vnf " + vnfId + " in A&AI!")
            }
            catch(Exception ex){
				execution.setVariable('UAAIVfMod_updateVfModuleResponseCode', 500)
				execution.setVariable('UAAIVfMod_updateVfModuleResponse', 'AAI PATCH Failed:' + ex.getMessage())
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, 'Exception occurred while executing AAI PATCH:' + ex.getMessage())
            }
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in updateVfModule(): ' + e.getMessage())
		}
	}

	/**
	 * Sets up json attributes for PATCH request for Update
	 *
	 * @param origRequest Incoming update request with VF Module elements to be updated.
	 * @param element Name of element to be inserted.
	 */
	private String updateVfModuleNode(String origRequest, String elementName) {

		if (!utils.nodeExists(origRequest, elementName)) {
			return null
		}
		def elementValue = utils.getNodeText(origRequest, elementName)

		if (elementValue.equals('DELETE')) {
			// Set the element being deleted to empty string
			return ""
		}
		else {
			return elementValue
		}		
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

		logger.error('Error occurred attempting to query AAI, Response Code ' + execution.getVariable('UAAIVfMod_getVfModuleResponseCode'))
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('UAAIVfMod_getVfModuleResponse'))
		execution.setVariable('WorkflowException', exception)
		logger.debug("UpdateAAIVfModule query failure: " + exception.getErrorMessage())
		logger.trace('Exited ' + method)
	}
}
