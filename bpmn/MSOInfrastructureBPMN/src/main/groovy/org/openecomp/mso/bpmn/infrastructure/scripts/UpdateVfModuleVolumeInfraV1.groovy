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

package org.openecomp.mso.bpmn.infrastructure.scripts

import groovy.json.JsonSlurper

import java.util.concurrent.ExecutionException

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*
import org.springframework.web.util.UriUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.VfModuleBase
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

class UpdateVfModuleVolumeInfraV1 extends VfModuleBase {
	
	/**
	 * Initialize the flow's variables.
	 * 
	 * @param execution The flow's execution instance.
	 */
	private void initProcessVariables(Execution execution) {
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
	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		preProcessRequest(execution, isDebugEnabled)
	}
	
	public void preProcessRequest(Execution execution, isDebugLogEnabled) {

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
		
			utils.log("DEBUG", "modelInvariantId from request: " + modelInvariantId, isDebugLogEnabled)
			utils.log("DEBUG", "XML request:\n" + request, isDebugLogEnabled)
		}
		catch(groovy.json.JsonException je) {
			utils.log("DEBUG", " Request is in XML format.", isDebugLogEnabled)
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
		execution.setVariable('UPDVfModVol_serviceId', utils.getNodeText1(volumeInputs, 'service-id'))
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
	public void sendSynchResponse(Execution execution, isDebugLogEnabled) {

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
			<volume-request xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
				<request-info>
					<request-id>${requestId}</request-id>
					<action>UPDATE_VF_MODULE_VOL</action>
					<request-status>IN_PROGRESS</request-status>
					<progress>${progress}</progress>
					<start-time>${startTime}</start-time>
					<source>${source}</source>
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
		
		logDebug('Sync response: ' + syncResponse, isDebugLogEnabled)
		execution.setVariable('UPDVfModVol_syncResponseSent', true)
		sendWorkflowResponse(execution, 200, syncResponse)
	}
	
	/**
	 * Prepare a Request for querying AAI for Volume Group information using the
	 * Volume Group Id and Aic Cloud Region.
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIForVolumeGroup(Execution execution, isDebugLogEnabled) {

		def volumeGroupId = execution.getVariable('UPDVfModVol_volumeGroupId')
		def aicCloudRegion = execution.getVariable('UPDVfModVol_aicCloudRegion')

		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = aaiUtil.getCloudInfrastructureCloudRegionEndpoint(execution)
		String queryAAIVolumeGroupRequest = aaiEndpoint + '/' + URLEncoder.encode(aicCloudRegion, "UTF-8") + "/volume-groups/volume-group/" + UriUtils.encode(volumeGroupId, "UTF-8")
		
		utils.logAudit('Query AAI volume group by ID: ' + queryAAIVolumeGroupRequest)
		logDebug('Query AAI volume group by ID: ' + queryAAIVolumeGroupRequest, isDebugLogEnabled)
		
		APIResponse response = aaiUtil.executeAAIGetCall(execution, queryAAIVolumeGroupRequest)
		
		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()
		aaiResponseAsString = StringEscapeUtils.unescapeXml(aaiResponseAsString)
		
		utils.logAudit("AAI query volume group by id return code: " + returnCode)
		utils.logAudit("AAI query volume group by id response: " + aaiResponseAsString)
		
		logDebug("AAI Volume Group return code: " + returnCode, isDebugLogEnabled)
		logDebug("AAI Volume Group response: " + aaiResponseAsString, isDebugLogEnabled)
		
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
			logDebug("Received Tenant Id " + volumeGroupTenantId + " from AAI for Volume Group with Volume Group Id " + volumeGroupId + ", AIC Cloud Region " + aicCloudRegion, isDebugLogEnabled)

			def relatedVfModuleLink = getRelatedVfModuleRelatedLink(aaiResponseAsString)
			logDebug("Related VF Module link: " + relatedVfModuleLink, isDebugLogEnabled)
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
	public void queryAAIForGenericVnf(Execution execution, isDebugEnabled) {
		
		def vnfId = execution.getVariable('vnfId')
		
		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = aaiUtil.getNetworkGenericVnfEndpoint(execution)
		def String queryAAIRequest = aaiEndpoint + "/" + UriUtils.encode(vnfId, "UTF-8")
		
		utils.logAudit("AAI query generic vnf request: " + queryAAIRequest)
		
		APIResponse response = aaiUtil.executeAAIGetCall(execution, queryAAIRequest)
		
		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()
		aaiResponseAsString = StringEscapeUtils.unescapeXml(aaiResponseAsString)
		
		utils.logAudit("AAI query generic vnf return code: " + returnCode)
		utils.logAudit("AAI query generic vnf response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()
		
		if (returnCode=='200') {
			utils.log("DEBUG", 'Generic vnf ' + vnfId + ' found in AAI.', isDebugEnabled)
			execution.setVariable('UPDVfModVol_AAIQueryGenericVfnResponse', aaiResponseAsString)
		} else {
			if (returnCode=='404') {
				def message = 'Generic vnf ' + vnfId + ' was not found in AAI. Return code: 404.'
				utils.log("DEBUG", message, isDebugEnabled)
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
	public void queryAAIForVfModule(Execution execution, isDebugLogEnabled) {
		
			AaiUtil aaiUtil = new AaiUtil(this)
			String queryAAIVfModuleRequest = execution.getVariable('UPDVfModVol_relatedVfModuleLink')
			execution.setVariable('UPDVfModVol_personaModelId', '')
			
			utils.logAudit('Query AAI VF Module: ' + queryAAIVfModuleRequest)
			logDebug('Query AAI VF Module: ' + queryAAIVfModuleRequest, isDebugLogEnabled)
			
			def aaiUrl = execution.getVariable("URN_aai_endpoint")
			logDebug('A&AI URL: ' + aaiUrl, isDebugLogEnabled)
			
			def requestEndpoint = aaiUrl + queryAAIVfModuleRequest
			logDebug('A&AI request endpoint: ' + requestEndpoint, isDebugLogEnabled)
			
			APIResponse response = aaiUtil.executeAAIGetCall(execution, requestEndpoint)
			
			String returnCode = response.getStatusCode()
			String aaiResponseAsString = response.getResponseBodyAsString()
			aaiResponseAsString = StringEscapeUtils.unescapeXml(aaiResponseAsString)
			
			utils.logAudit("AAI query vf-module: " + returnCode)
			utils.logAudit("AAI query vf-module response: " + aaiResponseAsString)
			
			logDebug("AAI query vf-module:: " + returnCode, isDebugLogEnabled)
			logDebug("AAI query vf-module response: " + aaiResponseAsString, isDebugLogEnabled)
			
			ExceptionUtil exceptionUtil = new ExceptionUtil()
			
			if ((returnCode == '200') || (returnCode == '204')) {
				def personaModelId =  utils.getNodeText1(aaiResponseAsString, 'model-invariant-id')
				if(personaModelId == null) {
					//check old attribute name
					personaModelId =  utils.getNodeText1(aaiResponseAsString, 'persona-model-id')
				}
				logDebug("vfModule personaModelId: " + personaModelId, isDebugLogEnabled)
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
	public void prepVnfAdapterRest(Execution execution, isDebugLogEnabled) {
		
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
		def vnfId = utils.getNodeText1(aaiGenericVnfResponse, 'vnf-id')
		def vnfName = utils.getNodeText1(aaiGenericVnfResponse, 'vnf-name')

		
		def volumeParamsXml = execution.getVariable('UPDVfModVol_volumeParams')
		def volumeGroupParams = transformVolumeParamsToEntries(volumeParamsXml)
		
		def requestId = execution.getVariable('UPDVfModVol_requestId')
		def serviceId = execution.getVariable('UPDVfModVol_serviceId')
		
		def messageId = execution.getVariable('mso-request-id') + '-' + System.currentTimeMillis()
		def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
		def useQualifiedHostName = execution.getVariable("URN_mso_use_qualified_host")
		if ('true'.equals(useQualifiedHostName)) {
				notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}

		String vnfAdapterRestRequest = """
			<updateVolumeGroupRequest>
				<cloudSiteId>${aicCloudRegion}</cloudSiteId>
				<tenantId>${tenantId}</tenantId>
				<vnfId>${vnfId}</vnfId>
				<vnfName>${vnfName}</vnfName>
				<volumeGroupId>${volumeGroupId}</volumeGroupId>
				<volumeGroupName>${volumeGroupName}</volumeGroupName>
				<volumeGroupStackId>${volumeGroupHeatStackId}</volumeGroupStackId>
				<vnfType>${vnfType}</vnfType>
				<vnfVersion>${vnfVersion}</vnfVersion>
				<vfModuleType></vfModuleType>
				<modelCustomizationUuid>${modelCustomizationId}</modelCustomizationUuid>
				<volumeGroupParams>
					<entry>
						<key>vnf_id</key>
						<value>${vnfId}</value>
					</entry>
					<entry>
						<key>vnf_name</key>
						<value>${vnfName}</value>
					</entry>
					<entry>
						<key>vf_module_id</key>
						<value>${volumeGroupId}</value>
					</entry>
					<entry>
						<key>vf_module_name</key>
						<value>${volumeGroupName}</value>
					</entry>
					${volumeGroupParams}
			    </volumeGroupParams>
				<skipAAI>true</skipAAI>
			    <msoRequest>
			        <requestId>${requestId}</requestId>
			        <serviceInstanceId>${serviceId}</serviceInstanceId>
			    </msoRequest>
			    <messageId>${messageId}</messageId>
			    <notificationUrl>${notificationUrl}</notificationUrl>
			</updateVolumeGroupRequest>
		"""
		vnfAdapterRestRequest = utils.formatXml(vnfAdapterRestRequest)
		execution.setVariable('UPDVfModVol_vnfAdapterRestRequest', vnfAdapterRestRequest)
		logDebug('Request for VNFAdapter Rest:\n' + vnfAdapterRestRequest, isDebugLogEnabled)
	}
	
	/**
	 * Prepare a Request for updating the DB for this Infra request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepDbInfraDbRequest(Execution execution, isDebugLogEnabled) {

		def requestId = execution.getVariable('UPDVfModVol_requestId')
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		
		String updateInfraRequest = """
			<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
					xmlns:req="http://org.openecomp.mso/requestsdb">
				<soapenv:Header/>
				<soapenv:Body>
					<req:updateInfraRequest>
						<requestId>${requestId}</requestId>
						<lastModifiedBy>BPEL</lastModifiedBy>
						<requestStatus>COMPLETE</requestStatus>
						<progress>100</progress>
					</req:updateInfraRequest>
				</soapenv:Body>
			</soapenv:Envelope>
		"""

		updateInfraRequest = utils.formatXml(updateInfraRequest)
		execution.setVariable('UPDVfModVol_updateInfraRequest', updateInfraRequest)
		logDebug('Request for Update Infra Request:\n' + updateInfraRequest, isDebugLogEnabled)
	}
	
	/**
	 * Build a "CompletionHandler" request.
	 * @param execution The flow's execution instance.
	 */
	public void prepCompletionHandlerRequest(Execution execution, requestId, action, source, isDebugLogEnabled) {

		String content = """
		<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
					xmlns:ns="http://org.openecomp/mso/request/types/v1">
			<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
				<request-id>${requestId}</request-id>
				<action>UPDATE</action>
				<source>${source}</source>
   			</request-info>
   			<aetgt:mso-bpel-name>BPMN VF Module Volume action: UPDATE</aetgt:mso-bpel-name>
		</aetgt:MsoCompletionRequest>		
		"""

		content = utils.formatXml(content)
		logDebug('Request for Completion Handler:\n' + content, isDebugLogEnabled)
		execution.setVariable('UPDVfModVol_CompletionHandlerRequest', content)
	}
	

	/**
	 * Build a "FalloutHandler" request.
	 * @param execution The flow's execution instance.
	 */
	public void prepFalloutHandler(Execution execution, isDebugLogEnabled) {
		def requestId = execution.getVariable('UPDVfModVol_requestId')
		def source = execution.getVariable('UPDVfModVol_source')
		
		String requestInfo = """
		<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
		<request-id>${requestId}</request-id>
		<action>UPDATE</action>
		<source>${source}</source>
	   </request-info>"""
		
		def WorkflowException workflowException = execution.getVariable("WorkflowException")
		def errorResponseCode = workflowException.getErrorCode()
		def errorResponseMsg = workflowException.getErrorMessage()
		def encErrorResponseMsg = ""
		if (errorResponseMsg != null) {
			encErrorResponseMsg = errorResponseMsg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
		}

		String content = """
			<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
					xmlns:reqtype="http://org.openecomp/mso/request/types/v1"
					xmlns:msoservtypes="http://org.openecomp/mso/request/types/v1"
					xmlns:structuredtypes="http://org.openecomp/mso/structured/types/v1">				
				${requestInfo}
				<sdncadapterworkflow:WorkflowException>
					<sdncadapterworkflow:ErrorMessage>${encErrorResponseMsg}</sdncadapterworkflow:ErrorMessage>
					<sdncadapterworkflow:ErrorCode>${errorResponseCode}</sdncadapterworkflow:ErrorCode>
				</sdncadapterworkflow:WorkflowException>	
			</sdncadapterworkflow:FalloutHandlerRequest>
		"""
		content = utils.formatXml(content)
		logDebug('Request for Fallout Handler:\n' + content, isDebugLogEnabled)
		execution.setVariable('UPDVfModVol_FalloutHandlerRequest', content)
	}
	
	/**
	 * Create a WorkflowException for the error case where the Tenant Id from
	 * AAI did not match the Tenant Id in the incoming request.
	 * @param execution The flow's execution instance.
	 */
	public void handleTenantIdMismatch(Execution execution, isDebugLogEnabled) {
		
		def volumeGroupId = execution.getVariable('UPDVfModVol_volumeGroupId')
		def aicCloudRegion = execution.getVariable('UPDVfModVol_aicCloudRegion')
		def tenantId = execution.getVariable('UPDVfModVol_tenantId')
		def volumeGroupTenantId = execution.getVariable('UPDVfModVol_volumeGroupTenantId')
		
		def String errorMessage = "TenantId " + tenantId + " in incoming request does not match Tenant Id " + volumeGroupTenantId +
			" retrieved from AAI for Volume Group Id " + volumeGroupId + ", AIC Cloud Region " + aicCloudRegion 
		
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		logError('Error in UpdateVfModuleVol: ' + errorMessage)
		exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
	}
	
	/**
	 * Create a WorkflowException for the error case where the Personal Model Id from
	 * AAI did not match the model invariant ID in the incoming request.
	 * @param execution The flow's execution instance.
	 */
	public void handlePersonaModelIdMismatch(Execution execution, isDebugLogEnabled) {
		
		def modelInvariantId = execution.getVariable('UPDVfModVol_modelInvariantId')
		def personaModelId = execution.getVariable('UPDVfModVol_personaModelId')
		
		def String errorMessage = "Model Invariant ID " + modelInvariantId + " in incoming request does not match persona model ID " + personaModelId +
			" retrieved from AAI for Volume Group Id "
		
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		logError('Error in UpdateVfModuleVol: ' + errorMessage)
		exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
	}
	
}
