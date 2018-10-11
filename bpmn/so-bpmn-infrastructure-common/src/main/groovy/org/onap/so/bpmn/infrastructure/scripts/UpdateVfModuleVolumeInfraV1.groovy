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

package org.onap.so.bpmn.infrastructure.scripts

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AaiUtil;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.VfModuleBase;
import org.onap.so.bpmn.common.scripts.VidUtils;
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.springframework.web.util.UriUtils

import groovy.json.JsonSlurper

class UpdateVfModuleVolumeInfraV1 extends VfModuleBase {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, UpdateVfModuleVolumeInfraV1.class);

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	private void initProcessVariables(DelegateExecution execution) {
		execution.setVariable('prefix', 'UPDVfModVol_')
		execution.setVariable('UPDVfModVol_Request', null)
		execution.setVariable('UPDVfModVol_requestInfo', null)
		execution.setVariable('UPDVfModVol_requestId', null)
		execution.setVariable('UPDVfModVol_source', null)
		execution.setVariable('UPDVfModVol_volumeInputs', null)
		execution.setVariable('UPDVfModVol_volumeGroupId', null)
		execution.setVariable('UPDVfModVol_vnfType', null)
		execution.setVariable('UPDVfModVol_serviceId', null)
		execution.setVariable('UPDVfModVol_aicCloudRegion', null)
		execution.setVariable('UPDVfModVol_tenantId', null)
		execution.setVariable('UPDVfModVol_volumeParams', null)
		execution.setVariable('UPDVfModVol_volumeGroupHeatStackId', null)
		execution.setVariable('UPDVfModVol_volumeGroupTenantId', null)
		execution.setVariable('UpdateVfModuleVolumeSuccessIndicator', false)
	}


	/**
	 * Perform initial processing, such as request validation, initialization of variables, etc.
	 * * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		preProcessRequest(execution, isDebugEnabled)
	}

	public void preProcessRequest(DelegateExecution execution, isDebugLogEnabled) {

		initProcessVariables(execution)
		String jsonRequest = validateRequest(execution)

		def request = ""

		try {
			def jsonSlurper = new JsonSlurper()
			Map reqMap = jsonSlurper.parseText(jsonRequest)

			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			def volumeGroupId = execution.getVariable('volumeGroupId')
			//def vnfId = execution.getVariable('vnfId')

			def vidUtils = new VidUtils(this)
			request = vidUtils.createXmlVolumeRequest(reqMap, 'UPDATE_VF_MODULE_VOL', serviceInstanceId, volumeGroupId)

			execution.setVariable('UPDVfModVol_Request', request)
			execution.setVariable("UPDVfModVol_isVidRequest", true)

			//need to get persona-model-id aka model-invariantId to use later to validate vf-module relation in AAI

			def modelInvariantId = reqMap.requestDetails.modelInfo.modelInvariantUuid ?: ''
			execution.setVariable('UPDVfModVol_modelInvariantId', modelInvariantId)

			msoLogger.debug("modelInvariantId from request: " + modelInvariantId)
			msoLogger.debug("XML request:\n" + request)
		}
		catch(groovy.json.JsonException je) {
			msoLogger.debug(" Request is in XML format.")
			// assume request is in XML format - proceed as usual to process XML request
		}

		def requestId = execution.getVariable('mso-request-id')

		def requestInfo = getRequiredNodeXml(execution, request, 'request-info')
		execution.setVariable('UPDVfModVol_requestInfo', requestInfo)
		execution.setVariable('UPDVfModVol_requestId', requestId)
		//execution.setVariable('UPDVfModVol_requestId', getRequiredNodeText(execution, requestInfo, 'request-id'))
		execution.setVariable('UPDVfModVol_source', getNodeTextForce(requestInfo, 'source'))

		def volumeInputs = getRequiredNodeXml(execution, request, 'volume-inputs')
		execution.setVariable('UPDVfModVol_volumeInputs', volumeInputs)
		execution.setVariable('UPDVfModVol_volumeGroupId', getRequiredNodeText(execution, volumeInputs, 'volume-group-id'))
		execution.setVariable('UPDVfModVol_vnfType', getRequiredNodeText(execution, volumeInputs, 'vnf-type'))
		execution.setVariable('UPDVfModVol_vnfVersion', getRequiredNodeText(execution, volumeInputs, 'asdc-service-model-version'))
		execution.setVariable('UPDVfModVol_serviceId', utils.getNodeText(volumeInputs, 'service-id'))
		execution.setVariable('UPDVfModVol_aicCloudRegion', getRequiredNodeText(execution, volumeInputs, 'aic-cloud-region'))
		execution.setVariable('UPDVfModVol_tenantId', getRequiredNodeText(execution, volumeInputs, 'tenant-id'))
		//execution.setVariable('UPDVfModVol_modelCustomizationId', getRequiredNodeText(execution, volumeInputs, 'model-customization-id'))

		setBasicDBAuthHeader(execution, isDebugLogEnabled)

		def volumeParams = utils.getNodeXml(request, 'volume-params')
		execution.setVariable('UPDVfModVol_volumeParams', volumeParams)
	}

	/**
	 * Prepare and send the synchronous response.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void sendSynchResponse(DelegateExecution execution, isDebugLogEnabled) {

		def requestInfo = execution.getVariable('UPDVfModVol_requestInfo')
		def requestId = execution.getVariable('UPDVfModVol_requestId')
		def source = execution.getVariable('UPDVfModVol_source')
		def progress = getNodeTextForce(requestInfo, 'progress')
		if (progress.isEmpty()) {
			progress = '0'
		}
		def startTime = getNodeTextForce(requestInfo, 'start-time')
		if (startTime.isEmpty()) {
			startTime = System.currentTimeMillis()
		}
		def volumeInputs = execution.getVariable('UPDVfModVol_volumeInputs')

		String xmlSyncResponse = """
			<volume-request xmlns="http://org.onap/so/infra/vnf-request/v1">
				<request-info>
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>UPDATE_VF_MODULE_VOL</action>
					<request-status>IN_PROGRESS</request-status>
					<progress>${MsoUtils.xmlEscape(progress)}</progress>
					<start-time>${MsoUtils.xmlEscape(startTime)}</start-time>
					<source>${MsoUtils.xmlEscape(source)}</source>
				</request-info>
				${volumeInputs}
			</volume-request>
		"""

		def syncResponse = ''
		def isVidRequest = execution.getVariable('UPDVfModVol_isVidRequest')

		if(isVidRequest) {
			def volumeGroupId = execution.getVariable('volumeGroupId')
			syncResponse = """{"requestReferences":{"instanceId":"${volumeGroupId}","requestId":"${requestId}"}}""".trim()
		}
		else {
			syncResponse = utils.formatXml(xmlSyncResponse)
		}

		msoLogger.debug('Sync response: ' + syncResponse)
		execution.setVariable('UPDVfModVol_syncResponseSent', true)
		sendWorkflowResponse(execution, 200, syncResponse)
	}

	/**
	 * Prepare a Request for querying AAI for Volume Group information using the
	 * Volume Group Id and Aic Cloud Region.
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIForVolumeGroup(DelegateExecution execution, isDebugLogEnabled) {

		def volumeGroupId = execution.getVariable('UPDVfModVol_volumeGroupId')
		def aicCloudRegion = execution.getVariable('UPDVfModVol_aicCloudRegion')

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), aicCloudRegion, volumeGroupId)
		def queryAAIVolumeGroupRequest = aaiUtil.createAaiUri(uri)

		msoLogger.debug('Query AAI volume group by ID: ' + queryAAIVolumeGroupRequest)
		msoLogger.debug('Query AAI volume group by ID: ' + queryAAIVolumeGroupRequest)

		APIResponse response = aaiUtil.executeAAIGetCall(execution, queryAAIVolumeGroupRequest)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI query volume group by id return code: " + returnCode)
		msoLogger.debug("AAI query volume group by id response: " + aaiResponseAsString)

		msoLogger.debug("AAI Volume Group return code: " + returnCode)
		msoLogger.debug("AAI Volume Group response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		if ((returnCode == '200') || (returnCode == '204')) {

			execution.setVariable('UPDVfModVol_aaiVolumeGroupResponse', aaiResponseAsString)
			//def heatStackId = getNodeTextForce(aaiResponseAsString, 'heat-stack-id')
			//execution.setVariable('UPDVfModVol_volumeGroupHeatStackId', heatStackId)

			def volumeGroupTenantId = getTenantIdFromVolumeGroup(aaiResponseAsString)
			if (volumeGroupTenantId == null) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Could not find Tenant Id element in Volume Group with Volume Group Id " + volumeGroupId
					+ ", AIC Cloud Region " + aicCloudRegion)
			}
			execution.setVariable('UPDVfModVol_volumeGroupTenantId', volumeGroupTenantId)
			msoLogger.debug("Received Tenant Id " + volumeGroupTenantId + " from AAI for Volume Group with Volume Group Id " + volumeGroupId + ", AIC Cloud Region " + aicCloudRegion)

			def relatedVfModuleLink = getRelatedVfModuleRelatedLink(aaiResponseAsString)
			msoLogger.debug("Related VF Module link: " + relatedVfModuleLink)
			execution.setVariable('UPDVfModVol_relatedVfModuleLink', relatedVfModuleLink)

		}
		else if (returnCode == '404') {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume Group " + volumeGroupId + " not found at AAI")
		}
		else {
			WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
			throw new BpmnError("MSOWorkflowException")
		}
	}

	/**
	 * Query AAI service instance
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void queryAAIForGenericVnf(DelegateExecution execution, isDebugEnabled) {

		def vnfId = execution.getVariable('vnfId')

		AaiUtil aaiUtil = new AaiUtil(this)
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId)
		String queryAAIRequest = aaiUtil.createAaiUri(uri)
		
		msoLogger.debug("AAI query generic vnf request: " + queryAAIRequest)

		APIResponse response = aaiUtil.executeAAIGetCall(execution, queryAAIRequest)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI query generic vnf return code: " + returnCode)
		msoLogger.debug("AAI query generic vnf response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		if (returnCode=='200') {
			msoLogger.debug('Generic vnf ' + vnfId + ' found in AAI.')
			execution.setVariable('UPDVfModVol_AAIQueryGenericVfnResponse', aaiResponseAsString)
		} else {
			if (returnCode=='404') {
				def message = 'Generic vnf ' + vnfId + ' was not found in AAI. Return code: 404.'
				msoLogger.debug(message)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, message)
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}

	/**
	 * Query AAI for VF Module using vf-module-id
	 * @param execution
	 * @param isDebugLogEnabled
	 */
	public void queryAAIForVfModule(DelegateExecution execution, isDebugLogEnabled) {

			AaiUtil aaiUtil = new AaiUtil(this)
			String queryAAIVfModuleRequest = execution.getVariable('UPDVfModVol_relatedVfModuleLink')
			execution.setVariable('UPDVfModVol_personaModelId', '')

			msoLogger.debug('Query AAI VF Module: ' + queryAAIVfModuleRequest)
			msoLogger.debug('Query AAI VF Module: ' + queryAAIVfModuleRequest)

			def aaiUrl = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			msoLogger.debug('A&AI URL: ' + aaiUrl)

			def requestEndpoint = aaiUrl + queryAAIVfModuleRequest
			msoLogger.debug('A&AI request endpoint: ' + requestEndpoint)

			APIResponse response = aaiUtil.executeAAIGetCall(execution, requestEndpoint)

			String returnCode = response.getStatusCode()
			String aaiResponseAsString = response.getResponseBodyAsString()

			msoLogger.debug("AAI query vf-module: " + returnCode)
			msoLogger.debug("AAI query vf-module response: " + aaiResponseAsString)

			msoLogger.debug("AAI query vf-module:: " + returnCode)
			msoLogger.debug("AAI query vf-module response: " + aaiResponseAsString)

			ExceptionUtil exceptionUtil = new ExceptionUtil()

			if ((returnCode == '200') || (returnCode == '204')) {
				def personaModelId =  utils.getNodeText(aaiResponseAsString, 'model-invariant-id')
				if(personaModelId == null) {
					//check old attribute name
					personaModelId =  utils.getNodeText(aaiResponseAsString, 'persona-model-id')
				}
				msoLogger.debug("vfModule personaModelId: " + personaModelId)
				execution.setVariable('UPDVfModVol_personaModelId', personaModelId)
			}
			else if (returnCode == '404') {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "VF Module not found at AAI")
			}
			else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	/**
	 *
	 */
	public String getRelatedVfModuleRelatedLink(xml) {
		def list = new XmlSlurper().parseText(xml)
		def vfModuleRelationship = list.'**'.find { node -> node.'related-to'.text() == 'vf-module'	}
		return vfModuleRelationship?.'related-link'?.text() ?: ''
	}

	/**
	 * Prepare a Request for invoking the VnfAdapterRest subflow to do
	 * a Volume Group update.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepVnfAdapterRest(DelegateExecution execution, isDebugLogEnabled) {

		def aicCloudRegion = execution.getVariable('UPDVfModVol_aicCloudRegion')
		def tenantId = execution.getVariable('UPDVfModVol_tenantId')
		def volumeGroupId = execution.getVariable('UPDVfModVol_volumeGroupId')

		def aaiVolumeGroupResponse = execution.getVariable('UPDVfModVol_aaiVolumeGroupResponse')
		def volumeGroupHeatStackId = getNodeTextForce(aaiVolumeGroupResponse, 'heat-stack-id')
		def volumeGroupName = getNodeTextForce(aaiVolumeGroupResponse, 'volume-group-name')
		def modelCustomizationId = getNodeTextForce(aaiVolumeGroupResponse, 'vf-module-model-customization-id')
		if(modelCustomizationId == null) {
			// Check old attribute name
			modelCustomizationId = getNodeTextForce(aaiVolumeGroupResponse, 'vf-module-persona-model-customization-id')
		}

		def vnfType = execution.getVariable('UPDVfModVol_vnfType')
		def vnfVersion = execution.getVariable('UPDVfModVol_vnfVersion')

		def aaiGenericVnfResponse = execution.getVariable('UPDVfModVol_AAIQueryGenericVfnResponse')
		def vnfId = utils.getNodeText(aaiGenericVnfResponse, 'vnf-id')
		def vnfName = utils.getNodeText(aaiGenericVnfResponse, 'vnf-name')


		def volumeParamsXml = execution.getVariable('UPDVfModVol_volumeParams')
		def volumeGroupParams = transformVolumeParamsToEntries(volumeParamsXml)

		def requestId = execution.getVariable('UPDVfModVol_requestId')
		def serviceId = execution.getVariable('UPDVfModVol_serviceId')

		def messageId = execution.getVariable('mso-request-id') + '-' + System.currentTimeMillis()
		def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
		def useQualifiedHostName = UrnPropertiesReader.getVariable("mso.use.qualified.host",execution)
		if ('true'.equals(useQualifiedHostName)) {
				notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}

		String vnfAdapterRestRequest = """
			<updateVolumeGroupRequest>
				<cloudSiteId>${MsoUtils.xmlEscape(aicCloudRegion)}</cloudSiteId>
				<tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
				<vnfId>${MsoUtils.xmlEscape(vnfId)}</vnfId>
				<vnfName>${MsoUtils.xmlEscape(vnfName)}</vnfName>
				<volumeGroupId>${MsoUtils.xmlEscape(volumeGroupId)}</volumeGroupId>
				<volumeGroupName>${MsoUtils.xmlEscape(volumeGroupName)}</volumeGroupName>
				<volumeGroupStackId>${MsoUtils.xmlEscape(volumeGroupHeatStackId)}</volumeGroupStackId>
				<vnfType>${MsoUtils.xmlEscape(vnfType)}</vnfType>
				<vnfVersion>${MsoUtils.xmlEscape(vnfVersion)}</vnfVersion>
				<vfModuleType></vfModuleType>
				<modelCustomizationUuid>${MsoUtils.xmlEscape(modelCustomizationId)}</modelCustomizationUuid>
				<volumeGroupParams>
					<entry>
						<key>vnf_id</key>
						<value>${MsoUtils.xmlEscape(vnfId)}</value>
					</entry>
					<entry>
						<key>vnf_name</key>
						<value>${MsoUtils.xmlEscape(vnfName)}</value>
					</entry>
					<entry>
						<key>vf_module_id</key>
						<value>${MsoUtils.xmlEscape(volumeGroupId)}</value>
					</entry>
					<entry>
						<key>vf_module_name</key>
						<value>${MsoUtils.xmlEscape(volumeGroupName)}</value>
					</entry>
					${volumeGroupParams}
			    </volumeGroupParams>
				<skipAAI>true</skipAAI>
			    <msoRequest>
			        <requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
			        <serviceInstanceId>${MsoUtils.xmlEscape(serviceId)}</serviceInstanceId>
			    </msoRequest>
			    <messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
			    <notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
			</updateVolumeGroupRequest>
		"""
		vnfAdapterRestRequest = utils.formatXml(vnfAdapterRestRequest)
		execution.setVariable('UPDVfModVol_vnfAdapterRestRequest', vnfAdapterRestRequest)
		msoLogger.debug('Request for VNFAdapter Rest:\n' + vnfAdapterRestRequest)
	}

	/**
	 * Prepare a Request for updating the DB for this Infra request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepDbInfraDbRequest(DelegateExecution execution, isDebugLogEnabled) {

		def requestId = execution.getVariable('UPDVfModVol_requestId')
		ExceptionUtil exceptionUtil = new ExceptionUtil();

		String updateInfraRequest = """
			<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
					xmlns:req="http://org.onap.so/requestsdb">
				<soapenv:Header/>
				<soapenv:Body>
					<req:updateInfraRequest>
						<requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
						<lastModifiedBy>BPEL</lastModifiedBy>
						<requestStatus>COMPLETE</requestStatus>
						<progress>100</progress>
					</req:updateInfraRequest>
				</soapenv:Body>
			</soapenv:Envelope>
		"""

		updateInfraRequest = utils.formatXml(updateInfraRequest)
		execution.setVariable('UPDVfModVol_updateInfraRequest', updateInfraRequest)
		msoLogger.debug('Request for Update Infra Request:\n' + updateInfraRequest)
	}

	/**
	 * Build a "CompletionHandler" request.
	 * @param execution The flow's execution instance.
	 */
	public void prepCompletionHandlerRequest(DelegateExecution execution, requestId, action, source, isDebugLogEnabled) {

		String content = """
		<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					xmlns:ns="http://org.onap/so/request/types/v1">
			<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
				<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
				<action>UPDATE</action>
				<source>${MsoUtils.xmlEscape(source)}</source>
   			</request-info>
   			<aetgt:mso-bpel-name>BPMN VF Module Volume action: UPDATE</aetgt:mso-bpel-name>
		</aetgt:MsoCompletionRequest>
		"""

		content = utils.formatXml(content)
		msoLogger.debug('Request for Completion Handler:\n' + content)
		execution.setVariable('UPDVfModVol_CompletionHandlerRequest', content)
	}


	/**
	 * Build a "FalloutHandler" request.
	 * @param execution The flow's execution instance.
	 */
	public void prepFalloutHandler(DelegateExecution execution, isDebugLogEnabled) {
		def requestId = execution.getVariable('UPDVfModVol_requestId')
		def source = execution.getVariable('UPDVfModVol_source')

		String requestInfo = """
		<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
		<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
		<action>UPDATE</action>
		<source>${MsoUtils.xmlEscape(source)}</source>
	   </request-info>"""

		def WorkflowException workflowException = execution.getVariable("WorkflowException")
		def errorResponseCode = workflowException.getErrorCode()
		def errorResponseMsg = workflowException.getErrorMessage()
		def encErrorResponseMsg = ""
		if (errorResponseMsg != null) {
			encErrorResponseMsg = errorResponseMsg
		}

		String content = """
			<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
					xmlns:reqtype="http://org.onap/so/request/types/v1"
					xmlns:msoservtypes="http://org.onap/so/request/types/v1"
					xmlns:structuredtypes="http://org.onap/so/structured/types/v1">
				${requestInfo}
				<sdncadapterworkflow:WorkflowException>
					<sdncadapterworkflow:ErrorMessage>${MsoUtils.xmlEscape(encErrorResponseMsg)}</sdncadapterworkflow:ErrorMessage>
					<sdncadapterworkflow:ErrorCode>${MsoUtils.xmlEscape(errorResponseCode)}</sdncadapterworkflow:ErrorCode>
				</sdncadapterworkflow:WorkflowException>
			</sdncadapterworkflow:FalloutHandlerRequest>
		"""
		content = utils.formatXml(content)
		msoLogger.debug('Request for Fallout Handler:\n' + content)
		execution.setVariable('UPDVfModVol_FalloutHandlerRequest', content)
	}

	/**
	 * Create a WorkflowException for the error case where the Tenant Id from
	 * AAI did not match the Tenant Id in the incoming request.
	 * @param execution The flow's execution instance.
	 */
	public void handleTenantIdMismatch(DelegateExecution execution, isDebugLogEnabled) {

		def volumeGroupId = execution.getVariable('UPDVfModVol_volumeGroupId')
		def aicCloudRegion = execution.getVariable('UPDVfModVol_aicCloudRegion')
		def tenantId = execution.getVariable('UPDVfModVol_tenantId')
		def volumeGroupTenantId = execution.getVariable('UPDVfModVol_volumeGroupTenantId')

		def String errorMessage = "TenantId " + tenantId + " in incoming request does not match Tenant Id " + volumeGroupTenantId +
			" retrieved from AAI for Volume Group Id " + volumeGroupId + ", AIC Cloud Region " + aicCloudRegion

		ExceptionUtil exceptionUtil = new ExceptionUtil()
		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Error in UpdateVfModuleVol: ' + errorMessage, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception");
		exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
	}

	/**
	 * Create a WorkflowException for the error case where the Personal Model Id from
	 * AAI did not match the model invariant ID in the incoming request.
	 * @param execution The flow's execution instance.
	 */
	public void handlePersonaModelIdMismatch(DelegateExecution execution, isDebugLogEnabled) {

		def modelInvariantId = execution.getVariable('UPDVfModVol_modelInvariantId')
		def personaModelId = execution.getVariable('UPDVfModVol_personaModelId')

		def String errorMessage = "Model Invariant ID " + modelInvariantId + " in incoming request does not match persona model ID " + personaModelId +
			" retrieved from AAI for Volume Group Id "

		ExceptionUtil exceptionUtil = new ExceptionUtil()
		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Error in UpdateVfModuleVol: ' + errorMessage, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception");
		exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
	}

}
