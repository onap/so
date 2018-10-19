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

package org.onap.so.bpmn.infrastructure.scripts;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AaiUtil;
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.VidUtils;
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.constants.Defaults
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.springframework.web.util.UriUtils


import groovy.json.JsonSlurper

/**
 * This groovy class supports the <class>DeleteVfModuleVolume.bpmn</class> process.
 */
public class DeleteVfModuleVolumeInfraV1 extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DeleteVfModuleVolumeInfraV1.class);

	private XmlParser xmlParser = new XmlParser()
	/**
	 * This method is executed during the preProcessRequest task of the <class>DeleteVfModuleVolume.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(DelegateExecution execution){
		execution.setVariable('prefix', 'DELVfModVol_')
		execution.setVariable("DELVfModVol_volumeRequest", null)
		execution.setVariable('DELVfModVol_requestInfo', null)
		execution.setVariable('DELVfModVol_requestId', null)
		execution.setVariable('DELVfModVol_source', null)
		execution.setVariable('DELVfModVol_volumeInputs', null)
		execution.setVariable('DELVfModVol_volumeOutputs', null)
		execution.setVariable('DELVfModVol_volumeGroupId', null)
		execution.setVariable('DELVfModVol_vnfType', null)
		execution.setVariable('DELVfModVol_serviceId', null)
		execution.setVariable('DELVfModVol_cloudRegion', null)
		execution.setVariable('DELVfModVol_tenantId', null)
		execution.setVariable('DELVfModVol_volumeParams', null)
		execution.setVariable('DELVfModVol_volumeGroupHeatStackId', null)
		execution.setVariable('DELVfModVol_volumeGroupTenantId', null)
		execution.setVariable("DELVfModVol_queryAAIVolGrpResponse", null)
		execution.setVariable('DELVfModVol_messageId', null)
		execution.setVariable('DELVfModVol_deleteVnfARequest', null)
		execution.setVariable('DELVfModVol_updateInfraRequest', null)
		execution.setVariable('DELVfModVol_CompleteMsoProcessRequest', null)
		execution.setVariable('DELVfModVol_WorkflowException', null)
		execution.setVariable('DELVfModVol_TransactionSuccessIndicator', false)
		execution.setVariable("DELVfModVol_isErrorMessageException", false)
		execution.setVariable('DELVfModVol_syncResponseSent', false)
	}

	/**
	 * Perform initial processing, such as request validation, initialization of variables, etc.
	 * * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		preProcessRequest(execution, isDebugEnabled)
	}

	/**
	 * This method is executed during the preProcessRequest task of the <class>DeleteVfModuleVolume.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution, isDebugLogEnabled) {

		InitializeProcessVariables(execution)

		String createVolumeIncoming = validateRequest(execution)

		// check if request is xml or json
		try {
			def jsonSlurper = new JsonSlurper()
			Map reqMap = jsonSlurper.parseText(createVolumeIncoming)
			msoLogger.debug(" Request is in JSON format.")

			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			def volumeGroupId = execution.getVariable('volumeGroupId')
			def vidUtils = new VidUtils(this)
			createVolumeIncoming = vidUtils.createXmlVolumeRequest(reqMap, 'DELETE_VF_MODULE_VOL', serviceInstanceId, volumeGroupId)
			execution.setVariable("DELVfModVol_isVidRequest", true)
		}
		catch(groovy.json.JsonException je) {
			msoLogger.debug(" Request is in XML format.")
			// assume request is in XML format - proceed as usual to process XML request
		}

		String request = utils.getNodeXml(createVolumeIncoming, "volume-request").drop(38).trim().replace("tag0:","").replace(":tag0","")
		execution.setVariable("DELVfModVol_volumeRequest", request)

		def requestInfo = getRequiredNodeXml(execution, request, 'request-info')
		execution.setVariable('DELVfModVol_requestInfo', requestInfo)
		String requestId = execution.getVariable("mso-request-id")
		if (requestId == null || requestId == "") {
			requestId = getRequiredNodeText(execution, requestInfo, 'request-id')
		}
		execution.setVariable('DELVfModVol_requestId', requestId)
		execution.setVariable('DELVfModVol_source', getNodeTextForce(requestInfo, 'source'))

		def volumeInputs = getRequiredNodeXml(execution, request, 'volume-inputs')
		execution.setVariable('DELVfModVol_volumeInputs', volumeInputs)
		execution.setVariable('DELVfModVol_volumeGroupId', getRequiredNodeText(execution, volumeInputs, 'volume-group-id'))
		execution.setVariable('DELVfModVol_vnfType', getRequiredNodeText(execution, volumeInputs, 'vnf-type'))
		execution.setVariable('DELVfModVol_serviceId', utils.getNodeText(volumeInputs, 'service-id'))
		execution.setVariable('DELVfModVol_tenantId', getRequiredNodeText(execution, volumeInputs, 'tenant-id'))
		execution.setVariable('DELVfModVol_messageId', UUID.randomUUID().toString())
		execution.setVariable('DELVfModVol_volumeOutputs', utils.getNodeXml(request, 'volume-outputs', false))
		execution.setVariable('DELVfModVol_volumeParams', utils.getNodeXml(request, 'volume-params'))
		execution.setVariable('DELVfModVol_cloudRegion', utils.getNodeText(request, 'aic-cloud-region'))

		setBasicDBAuthHeader(execution, isDebugLogEnabled)

		msoLogger.debug('Request: ' + createVolumeIncoming)
	}

	public void sendSyncResponse (DelegateExecution execution, isDebugEnabled) {

		String volumeRequest = execution.getVariable("DELVfModVol_volumeRequest")
		msoLogger.debug(" DELVfModVol_volumeRequest - " + "\n" + volumeRequest)
		// RESTResponse (for API Handler (APIH) Reply Task)
		String deleteVolumeRequest =
				"""<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd" statusCode="200">
                <rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
                          contentType="text/xml">
			        ${volumeRequest}
                </rest:payload>
		       </rest:RESTResponse>""".trim()

		def isVidRequest = execution.getVariable('DELVfModVol_isVidRequest')
		def syncResponse = ''

		if(isVidRequest) {
			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			def volumeGroupId = execution.getVariable('volumeGroupId')
			def requestId = execution.getVariable('DELVfModVol_requestId')
			syncResponse = """{"requestReferences":{"instanceId":"${volumeGroupId}","requestId":"${requestId}"}}""".trim()
		}
		else {
			syncResponse = utils.formatXml(deleteVolumeRequest)
		}

		execution.setVariable('DELVfModVol_syncResponseSent', true)

		sendWorkflowResponse(execution, 200, syncResponse)
	}


	public void sendSyncError (DelegateExecution execution, isDebugEnabled) {
		WorkflowException we = execution.getVariable('WorkflowException')
		def errorCode = we?.getErrorCode()
		def errorMessage = we?.getErrorMessage()
		//default to 400 since only invalid request will trigger this method
		sendWorkflowResponse(execution, 400, errorMessage)
	}


	public void callRESTQueryAAICloudRegion (DelegateExecution execution, isDebugEnabled) {

		String cloudRegion = execution.getVariable('DELVfModVol_cloudRegion')

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION, Defaults.CLOUD_OWNER.toString(), cloudRegion)
		def queryCloudRegionRequest = aaiUtil.createAaiUri(uri)

		execution.setVariable("DELVfModVol_queryCloudRegionRequest", queryCloudRegionRequest)

		cloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		if ((cloudRegion != "ERROR")) {
			if(execution.getVariable("DELVfModVol_queryCloudRegionReturnCode") == "404"){
				execution.setVariable("DELVfModVol_aicCloudRegion", "AAIAIC25")
			}else{
				execution.setVariable("DELVfModVol_aicCloudRegion", cloudRegion)
			}
			execution.setVariable("DELVfModVol_cloudRegion", cloudRegion)
			execution.setVariable("DELVfModVol_isCloudRegionGood", true)

		} else {
			msoLogger.debug("AAI Query Cloud Region Unsuccessful.")
			execution.setVariable("DELVfModVol_isCloudRegionGood", false)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "AAI Query Cloud Region Unsuccessful. Return Code: " + execution.getVariable("DELVfModVol_queryCloudRegionReturnCode"))
		}

		msoLogger.debug(" is Cloud Region Good: " + execution.getVariable("DELVfModVol_isCloudRegionGood"))
	}

	/**
	 * Query volume group by id
	 * @param execution
	 */
	public void queryAAIForVolumeGroup(DelegateExecution execution, isDebugLogEnabled) {

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		def volumeGroupId = execution.getVariable('DELVfModVol_volumeGroupId')
		if(volumeGroupId == null) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, 'volume-group-id is not provided in the request')
			throw new Exception('volume-group-id is not provided in the request')
		}
		String cloudRegion = execution.getVariable('DELVfModVol_aicCloudRegion')

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), cloudRegion, volumeGroupId)
		def queryAAIVolumeGroupRequest = aaiUtil.createAaiUri(uri)

		msoLogger.debug('Query AAI volume group by ID: ' + queryAAIVolumeGroupRequest)

		APIResponse response = aaiUtil.executeAAIGetCall(execution, queryAAIVolumeGroupRequest)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI query volume group by id return code: " + returnCode)
		msoLogger.debug("AAI query volume group by id response: " + aaiResponseAsString)

		execution.setVariable("DELVfModVol_queryAAIVolGrpResponse", aaiResponseAsString)

		if (returnCode=='200' || returnCode == '204') {

			def heatStackId = getNodeTextForce(aaiResponseAsString, 'heat-stack-id')
			execution.setVariable('DELVfModVol_volumeGroupHeatStackId', heatStackId)

			if(hasVfModuleRelationship(aaiResponseAsString)){
				msoLogger.debug('Volume Group ' + volumeGroupId + ' currently in use')
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume Group ${volumeGroupId} currently in use - found vf-module relationship.")
			}

			def volumeGroupTenantId = getTenantIdFromVolumeGroup(aaiResponseAsString)
			if (volumeGroupTenantId == null) {
				msoLogger.debug("Could not find Tenant Id element in Volume Group with Volume Group Id ${volumeGroupId}")
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Could not find Tenant Id element in Volume Group with Volume Group Id ${volumeGroupId}")
			}

			execution.setVariable('DELVfModVol_volumeGroupTenantId', volumeGroupTenantId)
			msoLogger.debug('Received Tenant Id ' + volumeGroupTenantId + ' from AAI for Volume Group with Volume Group Id ' + volumeGroupId )
		}
		else {
			if (returnCode=='404') {
				msoLogger.debug("Volume Group ${volumeGroupId} not found in AAI")
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume Group ${volumeGroupId} not found in AAI. Response code: 404")
			}
			else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}

	/**
	 * Extract the Tenant Id from the Volume Group information returned by AAI.
	 *
	 * @param volumeGroupXml Volume Group XML returned by AAI.
	 * @return the Tenant Id extracted from the Volume Group information. 'null' is returned if
	 * the Tenant Id is missing or could not otherwise be extracted.
	 */
	private String getTenantIdFromVolumeGroup(String volumeGroupXml) {
		def Node volumeGroupNode = xmlParser.parseText(volumeGroupXml)
		def Node relationshipList = utils.getChildNode(volumeGroupNode, 'relationship-list')
		if (relationshipList != null) {
			def NodeList relationships = utils.getIdenticalChildren(relationshipList, 'relationship')
			for (Node relationship in relationships) {
				def Node relatedTo = utils.getChildNode(relationship, 'related-to')
				if ((relatedTo != null) && (relatedTo.text().equals('tenant'))) {
					def NodeList relationshipDataList = utils.getIdenticalChildren(relationship, 'relationship-data')
					for (Node relationshipData in relationshipDataList) {
						def Node relationshipKey = utils.getChildNode(relationshipData, 'relationship-key')
						if ((relationshipKey != null) && (relationshipKey.text().equals('tenant.tenant-id'))) {
							def Node relationshipValue = utils.getChildNode(relationshipData, 'relationship-value')
							if (relationshipValue != null) {
								return relationshipValue.text()
							}
						}
					}
				}
			}
		}
		return null
	}

	private boolean hasVnfRelationship(String volumeGroupXml) {
		def Node volumeGroupNode = xmlParser.parseText(volumeGroupXml)
		def Node relationshipList = utils.getChildNode(volumeGroupNode, 'relationship-list')
		if (relationshipList != null) {
			def NodeList relationships = utils.getIdenticalChildren(relationshipList, 'relationship')
			for (Node relationship in relationships) {
				def Node relatedTo = utils.getChildNode(relationship, 'related-to')
				if ((relatedTo != null) && (relatedTo.text().equals('generic-vnf'))) {
					def Node relatedLink = utils.getChildNode(relationship, 'related-link')
					if (relatedLink !=null && relatedLink.text() != null){
						return true
					}
				}
			}
		}
		return false
	}

	private boolean hasVfModuleRelationship(String volumeGroupXml) {
		def Node volumeGroupNode = xmlParser.parseText(volumeGroupXml)
		def Node relationshipList = utils.getChildNode(volumeGroupNode, 'relationship-list')
		if (relationshipList != null) {
			def NodeList relationships = utils.getIdenticalChildren(relationshipList, 'relationship')
			for (Node relationship in relationships) {
				def Node relatedTo = utils.getChildNode(relationship, 'related-to')
				if ((relatedTo != null) && (relatedTo.text().equals('vf-module'))) {
					def Node relatedLink = utils.getChildNode(relationship, 'related-link')
					if (relatedLink !=null && relatedLink.text() != null){
						return true
					}
				}
			}
		}
		return false
	}

	public void prepareVnfAdapterDeleteRequest(DelegateExecution execution, isDebugLogEnabled) {
		def cloudRegion = execution.getVariable('DELVfModVol_cloudRegion')
		def tenantId = execution.getVariable('DELVfModVol_tenantId')
		def volumeGroupId = execution.getVariable('DELVfModVol_volumeGroupId')
		def volumeGroupHeatStackId = execution.getVariable('DELVfModVol_volumeGroupHeatStackId')
		def requestId = execution.getVariable('DELVfModVol_requestId')
		def serviceId = execution.getVariable('DELVfModVol_serviceId')

		def messageId = execution.getVariable('DELVfModVol_messageId')
		def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
		def useQualifiedHostName = UrnPropertiesReader.getVariable("mso.use.qualified.host", execution)
		if ('true'.equals(useQualifiedHostName)) {
				notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}

		String vnfAdapterRestRequest = """
			<deleteVolumeGroupRequest>
				<cloudSiteId>${MsoUtils.xmlEscape(cloudRegion)}</cloudSiteId>
				<tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
				<volumeGroupId>${MsoUtils.xmlEscape(volumeGroupId)}</volumeGroupId>
				<volumeGroupStackId>${MsoUtils.xmlEscape(volumeGroupHeatStackId)}</volumeGroupStackId>
				<skipAAI>true</skipAAI>
			    <msoRequest>
			        <requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
			        <serviceInstanceId>${MsoUtils.xmlEscape(serviceId)}</serviceInstanceId>
			    </msoRequest>
			    <messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
			    <notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
			</deleteVolumeGroupRequest>
		"""
		vnfAdapterRestRequest = utils.formatXml(vnfAdapterRestRequest)
		execution.setVariable('DELVfModVol_deleteVnfARequest', vnfAdapterRestRequest)
		msoLogger.debug('Request for VNFAdapter Rest:\n' + vnfAdapterRestRequest)
	}


	public void deleteVolGrpId(DelegateExecution execution, isDebugEnabled) {

		// get variables
		String queryAAIVolGrpIdResponse = execution.getVariable("DELVfModVol_queryAAIVolGrpResponse")
		String groupId = utils.getNodeText(queryAAIVolGrpIdResponse, "volume-group-id")
		String resourceVersion = utils.getNodeText(queryAAIVolGrpIdResponse, "resource-version")
		String messageId = execution.getVariable('DELVfModVol_messageId')
		String cloudRegion = execution.getVariable('DELVfModVol_aicCloudRegion')

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), cloudRegion, groupId).resourceVersion(resourceVersion)
		def deleteAAIVolumeGrpIdRequest = aaiUtil.createAaiUri(uri)

		msoLogger.debug("Delete AAI volume group : " + deleteAAIVolumeGrpIdRequest)

		APIResponse response = aaiUtil.executeAAIDeleteCall(execution, deleteAAIVolumeGrpIdRequest)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI delete volume group return code: " + returnCode)
		msoLogger.debug("AAI delete volume group response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()
		if (returnCode=='200' || (returnCode == '204')) {
			msoLogger.debug("Volume group $groupId deleted.")
		} else {
			if (returnCode=='404') {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume group $groupId not found for delete in AAI Response code: 404")
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}


	public void prepareDBRequest (DelegateExecution execution, isDebugLogEnabled) {

		WorkflowException workflowExceptionObj = execution.getVariable("WorkflowException")
		ExceptionUtil exceptionUtil = new ExceptionUtil();
		def requestId = execution.getVariable('DELVfModVol_requestId')
		def volOutputs = execution.getVariable('DELVfModVol_volumeOutputs')
		def statusMessage = "VolumeGroup successfully deleted"
		def progress = "100"
		def requestStatus = "COMPLETE"

		if (workflowExceptionObj != null) {
			statusMessage = (workflowExceptionObj.getErrorMessage())
			execution.setVariable("DELVfModVol_WorkflowExceptionMessage", statusMessage)
			execution.setVariable("DELVfModVol_WorkflowExceptionCode", workflowExceptionObj.getErrorCode())
			requestStatus = "FAILURE"
			progress = ""
		}

		String updateInfraRequest = """
			<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
					xmlns:req="http://org.onap.so/requestsdb">
				<soapenv:Header/>
				<soapenv:Body>
					<req:updateInfraRequest>
						<requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
						<lastModifiedBy>BPMN</lastModifiedBy>
						<statusMessage>${MsoUtils.xmlEscape(statusMessage)}</statusMessage>
						<requestStatus>${MsoUtils.xmlEscape(requestStatus)}</requestStatus>
						<progress>${MsoUtils.xmlEscape(progress)}</progress>
						<vnfOutputs>${MsoUtils.xmlEscape(volOutputs)}</vnfOutputs>
					</req:updateInfraRequest>
				</soapenv:Body>
			</soapenv:Envelope>
		"""

		updateInfraRequest = utils.formatXml(updateInfraRequest)
		execution.setVariable('DELVfModVol_updateInfraRequest', updateInfraRequest)
		msoLogger.debug('Request for Update Infra Request:\n' + updateInfraRequest)

	}


	public void prepareCompletionHandlerRequest (DelegateExecution execution, isDebugLogEnabled) {
		def requestId = execution.getVariable("mso-request-id")
		def source = execution.getVariable("DELVfModVol_source")

		String msoCompletionRequest =
			"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
							xmlns:ns="http://org.onap/so/request/types/v1">
					<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
						<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
						<action>DELETE</action>
						<source>${MsoUtils.xmlEscape(source)}</source>
		   			</request-info>
					<aetgt:status-message>Volume Group has been deleted successfully.</aetgt:status-message>
		   			<aetgt:mso-bpel-name>BPMN VF Module Volume action: DELETE</aetgt:mso-bpel-name>
				</aetgt:MsoCompletionRequest>"""

		String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)
		execution.setVariable('DELVfModVol_CompleteMsoProcessRequest', xmlMsoCompletionRequest)
		msoLogger.debug(" Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)

	}



	public void prepareFalloutHandler (DelegateExecution execution, isDebugEnabled) {

		execution.setVariable("DELVfModVol_Success", false)
		String requestId = execution.getVariable("DELVfModVol_requestId")
		String source = execution.getVariable("DELVfModVol_source")

		WorkflowException workflowExceptionObj = execution.getVariable("WorkflowException")
		def errorMessage = workflowExceptionObj.getErrorMessage()
		def errorCode =  workflowExceptionObj.getErrorCode()

		String falloutHandlerRequest =
		    """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
				                             xmlns:ns="http://org.onap/so/request/types/v1"
				                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
				   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
				      <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
				      <action>DELETE</action>
				      <source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>
					<aetgt:WorkflowException>
				      <aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
				      <aetgt:ErrorCode>${MsoUtils.xmlEscape(errorCode)}</aetgt:ErrorCode>
					</aetgt:WorkflowException>
				</aetgt:FalloutHandlerRequest>"""

		// Format Response
		String xmlHandlerRequest = utils.formatXml(falloutHandlerRequest)
		msoLogger.debug(xmlHandlerRequest)

		execution.setVariable("DELVfModVol_FalloutHandlerRequest", xmlHandlerRequest)
		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "  Overall Error Response going to FalloutHandler", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "\n" + xmlHandlerRequest);

	}


	/**
	 * Create a WorkflowException for the error case where the Tenant Id from
	 * AAI did not match the Tenant Id in the incoming request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void handleTenantIdMismatch(DelegateExecution execution, isDebugLogEnabled) {

		def volumeGroupId = execution.getVariable('DELVfModVol_volumeGroupId')
		def aicCloudRegion = execution.getVariable('DELVfModVol_aicCloudRegion')
		def tenantId = execution.getVariable('DELVfModVol_tenantId')
		def volumeGroupTenantId = execution.getVariable('DELVfModVol_volumeGroupTenantId')

		def String errorMessage = 'TenantId ' + tenantId + ' in incoming request does not match Tenant Id ' + volumeGroupTenantId +
			' retrieved from AAI for Volume Group Id ' + volumeGroupId
		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Error in DeleteVfModuleVolume: " + "\n" + errorMessage, "BPMN", MsoLogger.getServiceName(),MsoLogger.ErrorCode.UnknownError);

		ExceptionUtil exceptionUtil = new ExceptionUtil()
		exceptionUtil.buildWorkflowException(execution, 5000, errorMessage)

	}

}
