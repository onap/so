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

import org.onap.so.logger.LoggingAnchor
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.GenericVnf
import org.onap.so.bpmn.core.WorkflowException
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory




public class UpdateAAIGenericVnf extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( UpdateAAIGenericVnf.class);


	private XmlParser xmlParser = new XmlParser()
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(DelegateExecution execution) {
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
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		try {
			def xml = execution.getVariable('UpdateAAIGenericVnfRequest')
			logger.debug('Received request xml:\n' + xml)
			logger.debug("UpdateAAIGenericVnf Request XML: " + xml)
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

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logger.error(e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
		}
	}

	/**
	 * Using the received vnfId, query AAI to get the corresponding Generic VNF.
	 * A 200 response is expected with the VF Module in the response body.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void getGenericVnf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.getGenericVnf(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('UAAIGenVnf_vnfId')

			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
			uri.depth(Depth.ONE)
			try {
				Optional<GenericVnf> genericVnf = getAAIClient().get(GenericVnf.class,uri)
				if(genericVnf.isPresent()){
					execution.setVariable('UAAIGenVnf_getGenericVnfResponseCode', 200)
					execution.setVariable('UAAIGenVnf_getGenericVnfResponse', genericVnf.get())
				}else{
					execution.setVariable('UAAIGenVnf_getGenericVnfResponseCode', 404)
					execution.setVariable('UAAIGenVnf_getGenericVnfResponse', "Generic VNF not found for VNF ID: "+vnfId)
				}
			}catch (Exception ex) {
				logger.error(ex.getMessage())
				logger.debug('Exception occurred while executing AAI GET:' + ex.getMessage())
				execution.setVariable('UAAIGenVnf_getGenericVnfResponseCode', 500)
				execution.setVariable('UAAIGenVnf_getGenericVnfResponse', 'AAI GET Failed:' + ex.getMessage())
			}
			logger.trace('Exited ' + method)
		} catch (Exception e) {
			logger.error(e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in getGenericVnf(): ' + e.getMessage())
		}
	}

	/**
	 * Construct and send a PUT request to AAI to update the Generic VNF.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void updateGenericVnf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.updateGenericVnf(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('UAAIGenVnf_vnfId')
			GenericVnf genericVnf = execution.getVariable('UAAIGenVnf_getGenericVnfResponse')
			def origRequest = execution.getVariable('UpdateAAIGenericVnfRequest')

			logger.debug("UpdateGenericVnf Request: " + origRequest)
			// Handle persona-model-id/persona-model-version

			String newPersonaModelId = execution.getVariable('UAAIGenVnf_personaModelId')
			String newPersonaModelVersion = execution.getVariable('UAAIGenVnf_personaModelVersion')
			String personaModelVersionEntry = null
			if (newPersonaModelId != null || newPersonaModelVersion != null) {
				if (newPersonaModelId != genericVnf.getModelInvariantId()) {
					def msg = 'Can\'t update Generic VNF ' + vnfId + ' since there is \'persona-model-id\' mismatch between the current and new values'
					logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
							ErrorCode.UnknownError.getValue())
					throw new Exception(msg)
				}

				// Construct payload
				personaModelVersionEntry = updateGenericVnfNode(origRequest, 'model-version-id')
			}

			// Handle ipv4-oam-address
			String ipv4OamAddress = execution.getVariable('UAAIGenVnf_ipv4OamAddress')
			String ipv4OamAddressEntry = null
			if (ipv4OamAddress != null) {
				// Construct payload
				ipv4OamAddressEntry = updateGenericVnfNode(origRequest, 'ipv4-oam-address')
			}

			// Handle management-v6-address
			String managementV6Address = execution.getVariable('UAAIGenVnf_managementV6Address')
			String managementV6AddressEntry = null
			if (managementV6Address != null) {
				// Construct payload
				managementV6AddressEntry = updateGenericVnfNode(origRequest, 'management-v6-address')
			}

			// Handle orchestration-status
			String orchestrationStatus = execution.getVariable('UAAIGenVnf_orchestrationStatus')
			String orchestrationStatusEntry = null
			if (orchestrationStatus != null) {
				// Construct payload
				orchestrationStatusEntry = updateGenericVnfNode(origRequest, 'orchestration-status')
			}

			org.onap.aai.domain.yang.GenericVnf payload = new org.onap.aai.domain.yang.GenericVnf();
			payload.setVnfId(vnfId)
			payload.setPersonaModelVersion(personaModelVersionEntry)
			payload.setIpv4OamAddress(ipv4OamAddressEntry)
			payload.setManagementV6Address(managementV6AddressEntry)
			payload.setOrchestrationStatus(orchestrationStatusEntry)

			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))

			try {
				getAAIClient().update(uri,payload)
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponseCode', 200)
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponse', "Success")
			} catch (Exception ex) {
				logger.debug('Exception occurred while executing AAI PATCH: {}', ex.getMessage(), ex)
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponseCode', 500)
				execution.setVariable('UAAIGenVnf_updateGenericVnfResponse', 'AAI PATCH Failed:' + ex.getMessage())
			}
			logger.trace('Exited ' + method)
		} catch (Exception e) {
			logger.error(e)
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
	public String updateGenericVnfNode(String origRequest, String elementName) {

		if (!utils.nodeExists(origRequest, elementName)) {
			return null
		}
		def elementValue = utils.getNodeText(origRequest, elementName)

		if (elementValue == 'DELETE') {
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

		logger.error('Error occurred attempting to query AAI, Response Code ' + execution.getVariable('UAAIGenVnf_getGenericVnfResponseCode'))
		String processKey = getProcessKey(execution)
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('UAAIGenVnf_getGenericVnfResponse'))
		execution.setVariable('WorkflowException', exception)

		logger.debug("Workflow Exception occurred when handling Quering AAI: " + exception.getErrorMessage())
		logger.trace('Exited ' + method)
	}

	/**
	 * Generates a WorkflowException if updating a VF Module in AAI returns a response code other than 200.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void handleUpdateGenericVnfFailure(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.handleUpdateGenericVnfFailure(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		logger.error('Error occurred attempting to update Generic VNF in AAI, Response Code ' + execution.getVariable('UAAIGenVnf_updateGenericVnfResponseCode'))

		String processKey = getProcessKey(execution)
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable('UAAIGenVnf_updateGenericVnfResponse'))
		execution.setVariable('WorkflowException', exception)

		logger.debug("Workflow Exception occurred when Updating GenericVnf: " + exception.getErrorMessage())
		logger.trace('Exited ' + method)
	}
}
