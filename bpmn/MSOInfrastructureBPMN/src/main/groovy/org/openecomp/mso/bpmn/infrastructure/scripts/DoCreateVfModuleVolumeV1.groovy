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

package org.openecomp.mso.bpmn.infrastructure.scripts

import static org.camunda.spin.Spin.XML

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils
import org.openecomp.mso.bpmn.common.scripts.VfModuleBase
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.springframework.web.util.UriUtils

class DoCreateVfModuleVolumeV1 extends VfModuleBase {
	
	String prefix='DCVFMODVOLV1_'
	
	
	/**
	 * Perform initial processing, such as request validation, initialization of variables, etc.
	 * * @param execution
	 */
	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		preProcessRequest(execution, isDebugEnabled)
	}
	
	/**
	 * Perform initial processing, such as request validation, initialization of variables, etc.
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void preProcessRequest (Execution execution, isDebugEnabled) {

		execution.setVariable("prefix",prefix)
		execution.setVariable(prefix+'SuccessIndicator', false)
		
	
		// INPUT: DoCreateVfModuleVolumeV1Request, mso-request-id, volume-group-id, vnf-id, is-vid-request
		// OUTPUT: DCVFMODVOLV1_SuccessIndicator. WorkflowException
		
		def volumeRequest  = getVariable(execution, 'DoCreateVfModuleVolumeV1Request')
		if (volumeRequest != null) {
			execution.setVariable(prefix+'Request', volumeRequest)
		} else {	
			volumeRequest  = getVariable(execution, prefix+'Request')
			if (volumeRequest == null) {
				(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, 'DoCreateVfModuleVolumeV1 received null request.')
			}
		}
		
		def vnfId = execution.getVariable('vnf-id')
		if (vnfId == null) {
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, 'DoCreateVfModuleVolumeV1 received null vnf-id.')
		}
		
		def volumeGroupId = execution.getVariable('volume-group-id')
		if (volumeGroupId == null) {
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, 'DoCreateVfModuleVolumeV1 received null volume-group-id.')
		}
		
		def requestId = execution.getVariable("mso-request-id")
		if (requestId == null || requestId == "") {
			requestId = utils.getNodeText1(volumeRequest, "request-id")
		}

		//def serviceId = execution.getVariable("mso-service-instance-id")
		//if (serviceId == null || serviceId == "") {
		def	serviceId = utils.getNodeText1(volumeRequest, "service-id")
		//}
		
		def source = utils.getNodeText1(volumeRequest, "source")
		
		execution.setVariable(prefix+'requestId', requestId)
		execution.setVariable(prefix+'serviceId', serviceId)
		execution.setVariable(prefix+'source', source)
		
		// @TODO: for better tracking of logs, should we strip all new lines in the log message?
		utils.logAudit('Incoming request: ' + volumeRequest)

		// Rollback settings
		NetworkUtils networkUtils = new NetworkUtils()
		def rollbackEnabled = networkUtils.isRollbackEnabled(execution,volumeRequest)
		execution.setVariable(prefix+"rollbackEnabled", rollbackEnabled)
		utils.log("DEBUG", 'rollbackEnabled: ' + rollbackEnabled, isDebugEnabled)
		
	}
	

	/**
	 * Get cloud region
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTQueryAAICloudRegion (Execution execution, isDebugEnabled) {
		
		def request = execution.getVariable(prefix+'Request')
		def cloudRegion = utils.getNodeText1(request, "aic-cloud-region")
		utils.log("DEBUG", 'Request cloud region is: ' + cloudRegion, isDebugEnabled)

		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = aaiUtil.getCloudInfrastructureCloudRegionEndpoint(execution)
		String queryCloudRegionRequest = aaiEndpoint + '/' + cloudRegion
		
		utils.logAudit(queryCloudRegionRequest)

		cloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)

		if ((cloudRegion != "ERROR")) {
			if(execution.getVariable(prefix+"queryCloudRegionReturnCode") == "404"){
				cloudRegion = "AAIAIC25"
			}
			execution.setVariable(prefix+"aicCloudRegion", cloudRegion)
			utils.log("DEBUG", "AIC Cloud Region: " + cloudRegion, isDebugEnabled)
		} else {
			String errorMessage = "AAI Query Cloud Region Unsuccessful. Return Code: " + execution.getVariable(prefix+"queryCloudRegionReturnCode")
			utils.log("DEBUG", errorMessage, isDebugEnabled)
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, errorMessage)
		}
	}
	

	/**
	 * Query AAI volume group by name
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTQueryAAIVolGrpName(Execution execution, isDebugEnabled) {

		def volumeRequest = execution.getVariable(prefix+'Request')
		def volumeGroupName = utils.getNodeText(volumeRequest, "volume-group-name")
		def cloudRegion = execution.getVariable(prefix+"aicCloudRegion")
		
		// Save volume group name
		execution.setVariable(prefix+'volumeGroupName', volumeGroupName)
		
		// This is for stub testing
		def testVolumeGroupName = execution.getVariable('test-volume-group-name')
		if (testVolumeGroupName != null && testVolumeGroupName.length() > 0) {
			volumeGroupName = testVolumeGroupName
			//reset to null
			execution.setVariable('test-volume-group-name', null)
		}
		
		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = aaiUtil.getCloudInfrastructureCloudRegionEndpoint(execution)
		String queryAAIVolumeNameRequest = aaiEndpoint + '/' + cloudRegion + "/volume-groups" + "?volume-group-name=" + UriUtils.encode(volumeGroupName, 'UTF-8')

		utils.logAudit('Query AAI volume group by name: ' + queryAAIVolumeNameRequest)
		
		APIResponse response = aaiUtil.executeAAIGetCall(execution, queryAAIVolumeNameRequest)
		
		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()
		aaiResponseAsString = StringEscapeUtils.unescapeXml(aaiResponseAsString)
		
		utils.logAudit("AAI query volume group by name return code: " + returnCode)
		utils.logAudit("AAI query volume group by name response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		execution.setVariable(prefix+"queryAAIVolGrpNameResponse", aaiResponseAsString)
		execution.setVariable(prefix+'AaiReturnCode', returnCode)

		if (returnCode=='200') {
			// @TODO: verify error code
			// @TODO: create class of literals representing error codes
			execution.setVariable(prefix+'queryAAIVolGrpNameResponse', aaiResponseAsString)
			utils.log("DEBUG", "Volume Group Name $volumeGroupName exists in AAI.", isDebugEnabled)
		} else {
			if (returnCode=='404') {
				utils.log("DEBUG", "Volume Group Name $volumeGroupName does not exist in AAI.", isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume group $volumeGroupName not found in AAI. Response code: 404")
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}
	

	/**
	 * Create a WorkflowException 
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void buildWorkflowException(Execution execution, int errorCode, errorMessage, isDebugEnabled) {
		utils.log("DEBUG", errorMessage, isDebugEnabled)
		(new ExceptionUtil()).buildWorkflowException(execution, 2500, errorMessage)
	}
	

	/**
	 * Create a WorkflowException
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void handleError(Execution execution, isDebugEnabled) {
		WorkflowException we = execution.getVariable('WorkflowException')
		if (we == null) {
			(new ExceptionUtil()).buildWorkflowException(execution, 2500, "Enexpected error encountered!")
		}
		throw new BpmnError("MSOWorkflowException")
	}
	
	/**
	 * Create volume group in AAI
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTCreateAAIVolGrpName(Execution execution, isDebugEnabled) {

		def volumeRequest  = execution.getVariable(prefix+'Request')
		def vnfId = execution.getVariable('vnf-id')
		def volumeName = utils.getNodeText(volumeRequest, "volume-group-name")
		def modelCustomizationId = getNodeTextForce(volumeRequest, "model-customization-id")
		
		def cloudRegion = execution.getVariable(prefix+"aicCloudRegion")
		def groupId = execution.getVariable('volume-group-id')
		utils.log("DEBUG", "volume group id: " + groupId, isDebugEnabled)
		
		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = aaiUtil.getCloudInfrastructureCloudRegionEndpoint(execution)
		String createAAIVolumeGrpNameUrlRequest = aaiEndpoint + '/' + cloudRegion + "/volume-groups/volume-group/" + UriUtils.encode(groupId, "UTF-8")
		
		String namespace =  aaiUtil.getNamespaceFromUri(aaiUtil.getCloudInfrastructureCloudRegionUri(execution))
		utils.log("DEBUG", "AAI namespace is: " + namespace, isDebugEnabled)
		
		utils.logAudit(createAAIVolumeGrpNameUrlRequest)
		
		//Prepare payload (PUT)
		def vnfType = utils.getNodeText(volumeRequest, "vnf-type")
		def tenantId = utils.getNodeText(volumeRequest, "tenant-id")
		
		NetworkUtils networkUtils = new NetworkUtils()
		String payload = networkUtils.createCloudRegionVolumeRequest(groupId, volumeName, vnfType, vnfId, tenantId, cloudRegion, namespace, modelCustomizationId)
		String payloadXml = utils.formatXml(payload)
		utils.logAudit(payloadXml)
		
		APIResponse response = aaiUtil.executeAAIPutCall(execution, createAAIVolumeGrpNameUrlRequest, payloadXml)
				
		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()
		aaiResponseAsString = StringEscapeUtils.unescapeXml(aaiResponseAsString)
		
		utils.logAudit("AAI create volume group return code: " + returnCode)
		utils.logAudit("AAI create volume group response: " + aaiResponseAsString)

		execution.setVariable(prefix+"createAAIVolumeGrpNameReturnCode", returnCode)
		execution.setVariable(prefix+"createAAIVolumeGrpNameResponse", aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()
		
		if (returnCode =='201') {
			execution.setVariable(prefix+"isAAIRollbackNeeded", true)
		} else {
			execution.setVariable(prefix+"isErrorMessageException", true)
			if (returnCode=='404') {
				// @TODO: verify return code and make static LITERAL
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Unable to create volume group in AAI. Response code: 404")
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				utils.log("DEBUG", " AAI Adapter Query Failed.  WorkflowException - " + "\n" + aWorkflowException, isDebugEnabled)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}

	/**
	 * Prepare VNF adapter create request XML
	 * @param execution
	 */
	public void prepareVnfAdapterCreateRequest(Execution execution, isDebugEnabled) {
		
		
		def volumeRequest  = execution.getVariable(prefix+'Request')
		def requestId = execution.getVariable(prefix+'requestId')
		def serviceId = execution.getVariable(prefix+'serviceId')
		
		def aaiGenericVnfResponse = execution.getVariable(prefix+'AAIQueryGenericVfnResponse')
		def vnfId = utils.getNodeText1(aaiGenericVnfResponse, 'vnf-id')
		def vnfName = utils.getNodeText1(aaiGenericVnfResponse, 'vnf-name')
		def modelCustomizationId = getNodeTextForce(volumeRequest, "model-customization-id")
				
		String messageId = UUID.randomUUID()
		utils.log("DEBUG", "messageId to be used is generated: " + messageId, isDebugEnabled)

		// prepare vnf request for vnfAdapterCreateV1
		def cloudSiteId = utils.getNodeText1(volumeRequest, 'aic-cloud-region')
		def tenantId = utils.getNodeText1(volumeRequest, "tenant-id")
		def vnfType = utils.getNodeText1(volumeRequest, "vnf-type")
		def vnfVersion = utils.getNodeText1(volumeRequest, "asdc-service-model-version")
		def vnfModuleType = utils.getNodeText1(volumeRequest, "vf-module-model-name")
		def volumeGroupnName = utils.getNodeText1(volumeRequest, "volume-group-name")

		def volumeParamsXml = utils.getNodeXml(volumeRequest, 'volume-params')
		def volumeGroupParams = transformVolumeParamsToEntries(volumeParamsXml)

		utils.log("DEBUG", "volumeGroupParams: "+ volumeGroupParams, isDebugEnabled)

		String volumeGroupId = execution.getVariable('volume-group-id')
		def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId) 
		def useQualifiedHostName = execution.getVariable("URN_mso_use_qualified_host")
		if ('true'.equals(useQualifiedHostName)) {
				notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}
		utils.log("DEBUG", "CreateVfModuleVolume - notificationUrl: "+ notificationUrl, isDebugEnabled)
		
		def backoutOnFailure = execution.getVariable("DCVFMODVOLV1_rollbackEnabled")
		utils.log("DEBUG", "backoutOnFailure: "+ backoutOnFailure, isDebugEnabled)
		
		// build request
		String vnfSubCreateWorkflowRequest =
		"""
			<createVolumeGroupRequest>
				<cloudSiteId>${cloudSiteId}</cloudSiteId>
				<tenantId>${tenantId}</tenantId>
				<vnfId>${vnfId}</vnfId>
				<vnfName>${vnfName}</vnfName>
				<volumeGroupId>${volumeGroupId}</volumeGroupId>
				<volumeGroupName>${volumeGroupnName}</volumeGroupName>
				<vnfType>${vnfType}</vnfType>
				<vnfVersion>${vnfVersion}</vnfVersion>
				<vfModuleType>${vnfModuleType}</vfModuleType>
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
						<value>${volumeGroupnName}</value>
					</entry>
					${volumeGroupParams}
			    </volumeGroupParams>
				<skipAAI>true</skipAAI>
				<backout>${backoutOnFailure}</backout>
				<failIfExists>true</failIfExists>
			    <msoRequest>
			        <requestId>${requestId}</requestId>
			        <serviceInstanceId>${serviceId}</serviceInstanceId>
			    </msoRequest>
			    <messageId>${messageId}</messageId>
			    <notificationUrl>${notificationUrl}</notificationUrl>
			</createVolumeGroupRequest>
		"""

		String vnfSubCreateWorkflowRequestAsString = utils.formatXml(vnfSubCreateWorkflowRequest)
		utils.logAudit(vnfSubCreateWorkflowRequestAsString)
		utils.log('DEBUG', vnfSubCreateWorkflowRequestAsString, isDebugEnabled)
		execution.setVariable(prefix+"createVnfARequest", vnfSubCreateWorkflowRequestAsString)
		
		// build rollback request for use later if needed
		
		String vnfSubRollbackWorkflowRequest =
		"""<rollbackVolumeGroupRequest>
				<cloudSiteId>${cloudSiteId}</cloudSiteId>
				<tenantId>${tenantId}</tenantId>
				<volumeGroupId>${volumeGroupId}</volumeGroupId>
				<skipAAI>true</skipAAI>
				<volumeGroupCreated>true</volumeGroupCreated>
			    <msoRequest>
			        <requestId>${requestId}</requestId>
			        <serviceInstanceId>${serviceId}</serviceInstanceId>
			    </msoRequest>
			    <messageId>${messageId}</messageId>
			    <notificationUrl>${notificationUrl}</notificationUrl>
			</rollbackVolumeGroupRequest>"""

		utils.log("DEBUG", "Sub Vnf flow rollback request: vnfSubRollbackWorkflowRequest " + "\n" + vnfSubRollbackWorkflowRequest, isDebugEnabled)
		
		String vnfSubRollbackWorkflowRequestAsString = utils.formatXml(vnfSubRollbackWorkflowRequest)
		execution.setVariable(prefix+"rollbackVnfARequest", vnfSubRollbackWorkflowRequestAsString)
	}

	

	/**
	 * Update voulume group in AAI 
	 * @TODO: Can we re-use the create method??
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTUpdateCreatedVolGrpName(Execution execution, isDebugEnabled) {
		
		// get variables
		String requeryAAIVolGrpNameResponse = execution.getVariable(prefix+"queryAAIVolGrpNameResponse")
		String volumeGroupId = utils.getNodeText(requeryAAIVolGrpNameResponse, "volume-group-id")
		String modelCustomizationId = getNodeTextForce(requeryAAIVolGrpNameResponse, "vf-module-persona-model-customization-id")
		String cloudRegion = execution.getVariable(prefix+"aicCloudRegion")
		
		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = aaiUtil.getCloudInfrastructureCloudRegionEndpoint(execution)
		String updateAAIVolumeGroupUrlRequest = aaiEndpoint + '/' + cloudRegion + "/volume-groups/volume-group/" + UriUtils.encode(volumeGroupId, 'UTF-8')
		
		String namespace =  aaiUtil.getNamespaceFromUri(aaiUtil.getCloudInfrastructureCloudRegionUri(execution))

		utils.logAudit(updateAAIVolumeGroupUrlRequest)

		//Prepare payload (PUT)
		String createVnfAResponse = execution.getVariable(prefix+"createVnfAResponse")
		
		// @TODO: revisit
		// if VID request createVnfresponse will be null, use vnf from JSON request
		def vnfId = ""
		if(createVnfAResponse == null || createVnfAResponse == "") {
			vnfId = execution.getVariable(prefix+'vnfId')
		}
		else {
			vnfId = utils.getNodeText(createVnfAResponse, "volumeGroupStackId")
		}
		
		NetworkUtils networkUtils = new NetworkUtils()
		String payload = networkUtils.updateCloudRegionVolumeRequest(requeryAAIVolGrpNameResponse, vnfId, namespace, modelCustomizationId)
		String payloadXml = utils.formatXml(payload)
		
		//execution.setVariable(prefix+"updateCreatedAAIVolumeGrpNamePayloadRequest", payloadXml)
		utils.logAudit(payload)
		//utils.log("DEBUG", " 'payload' to Update Created VolumeGroupName - " + "\n" + payloadXml, isDebugEnabled)

		APIResponse response = aaiUtil.executeAAIPutCall(execution, updateAAIVolumeGroupUrlRequest, payloadXml)
		
		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()
		aaiResponseAsString = StringEscapeUtils.unescapeXml(aaiResponseAsString)
		
		utils.logAudit("AAI create volume group return code: " + returnCode)
		utils.logAudit("AAI create volume group response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		if (returnCode =='200') {
			execution.setVariable(prefix+"updateCreatedAAIVolumeGrpNameResponse", aaiResponseAsString)
			execution.setVariable(prefix+"isPONR", true)
		} else {
			execution.setVariable(prefix+"isErrorMessageException", true)
			if (returnCode=='404') {
				// @TODO: verify return code and make static LITERAL
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Unable to update volume group in AAI. Response code: 404")
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				utils.log("DEBUG", " AAI Adapter Query Failed.  WorkflowException - " + "\n" + aWorkflowException, isDebugEnabled)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}

	
	/**
	 * Query AAI service instance
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTQueryAAIGenericVnf(Execution execution, isDebugEnabled) {
		
		def request = execution.getVariable(prefix+"Request")
		def vnfId = execution.getVariable('vnf-id')
		
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

		//utils.log("DEBUG", "AAI query generic vnf return code: " + returnCode, isDebugEnabled)
		//utils.log("DEBUG", "AAI query generic vnf response: " + aaiResponseAsString, isDebugEnabled)
		
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		
		if (returnCode=='200') {
			utils.log("DEBUG", 'Generic vnf ' + vnfId + ' found in AAI.', isDebugEnabled)
			execution.setVariable(prefix+'AAIQueryGenericVfnResponse', aaiResponseAsString)
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

	public void callRESTDeleteAAIVolumeGroup(Execution execution, isDebugEnabled) {

		callRESTQueryAAIVolGrpName(execution, isDebugEnabled)
		
		def queryAaiVolumeGroupResponse = execution.getVariable(prefix+'queryAAIVolGrpNameResponse')
		
		def volumeGroupId = utils.getNodeText(queryAaiVolumeGroupResponse, "volume-group-id")
		def resourceVersion = utils.getNodeText(queryAaiVolumeGroupResponse, "resource-version")

		def cloudRegion = execution.getVariable(prefix+"aicCloudRegion")
		
		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = aaiUtil.getCloudInfrastructureCloudRegionEndpoint(execution)
		String deleteAAIVolumeGrpIdRequest = aaiEndpoint + '/' + cloudRegion + "/volume-groups/volume-group" + '/' +  volumeGroupId + "?resource-version=" + UriUtils.encode(resourceVersion, "UTF-8")

		utils.logAudit('Delete AAI volume group : ' + deleteAAIVolumeGrpIdRequest)
		
		APIResponse response = aaiUtil.executeAAIDeleteCall(execution, deleteAAIVolumeGrpIdRequest)
		
		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()
		aaiResponseAsString = StringEscapeUtils.unescapeXml(aaiResponseAsString)
		
		utils.logAudit("AAI delete volume group return code: " + returnCode)
		utils.logAudit("AAI delete volume group response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		def volumeGroupNameFound = prefix+'volumeGroupNameFound'
		if (returnCode=='200' || returnCode=='204' ) {
			utils.log("DEBUG", "Volume group $volumeGroupId deleted.", isDebugEnabled)
		} else {
			if (returnCode=='404') {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume group $volumeGroupId not found for delete in AAI Response code: 404")
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}

	
	
	
	
	public void prepareSuccessDBRequest(Execution execution, isDebugEnabled) {
		String requestId = execution.getVariable(prefix+'requestId')
		String dbVnfOutputs = execution.getVariable(prefix+'volumeOutputs')
		prepareDBRequest(execution, requestId, "VolumeGroup successfully created.", "COMPLETED", "100", dbVnfOutputs, isDebugEnabled)
	}
	
	public void prepareFailDbRequest(Execution execution, isDebugEnabled) {
		
		WorkflowException we = execution.getVariable("WorkflowException")
		
		String requestId = execution.getVariable(prefix+'requestId')
		String dbVnfOutputs = execution.getVariable(prefix+'volumeOutputs')
		prepareDBRequest(execution, requestId, we.getErrorMessage(), "FAILURE", "", dbVnfOutputs, isDebugEnabled)

	}
	
	
	/**
	 * Prepare Infra DB XML Request 
	 * @param execution
	 */
	public void prepareDBRequest (Execution execution, String requestId, String statusMessage, String requestStatus, String progress, String dbVnfOutputs, isDebugEnabled) {
		
		String dbRequest =
		"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
			<soapenv:Header/>
			<soapenv:Body>
				<ns:updateInfraRequest xmlns:ns="http://org.openecomp.mso/requestsdb">
					<requestId>${requestId}</requestId>
					<lastModifiedBy>BPMN</lastModifiedBy>
					<statusMessage>${statusMessage}</statusMessage>
					<responseBody></responseBody>
					<requestStatus>${requestStatus}</requestStatus>
					<progress>${progress}</progress>
					<vnfOutputs>${dbVnfOutputs}</vnfOutputs>
				</ns:updateInfraRequest>
		   	</soapenv:Body>
		   </soapenv:Envelope>"""

	   utils.log("DEBUG", " DB Adapter Request - " + "\n" + dbRequest, isDebugEnabled)
	   String buildDeleteDBRequestAsString = utils.formatXml(dbRequest)
	   
	   execution.setVariable(prefix+"createDBRequest", buildDeleteDBRequestAsString)
	   
	   utils.logAudit(buildDeleteDBRequestAsString)
	 }



	
	public void postProcessResponse (Execution execution, isDebugEnabled) {

		String dbReturnCode = execution.getVariable(prefix+"dbReturnCode")
		utils.log("DEBUG", "DB Update Response Code  : " + dbReturnCode, isDebugEnabled)
		
		String createDBResponse =  execution.getVariable(prefix+"createDBResponse")
		utils.logAudit(createDBResponse)

		String source = execution.getVariable(prefix+"source")
		String requestId = execution.getVariable(prefix+'requestId')

		String msoCompletionRequest =
			"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
							xmlns:ns="http://org.openecomp/mso/request/types/v1">
					<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
						<request-id>${requestId}</request-id>
						<action>CREATE</action>
						<source>${source}</source>
		   			</request-info>
		   			<aetgt:mso-bpel-name>BPEL Volume Group action: CREATE</aetgt:mso-bpel-name>
				</aetgt:MsoCompletionRequest>"""

		// Format Response
		String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)
		utils.logAudit(xmlMsoCompletionRequest)
		
		// normal path
		if (dbReturnCode == "200") {
				execution.setVariable(prefix+"Success", true)
				execution.setVariable(prefix+"CompleteMsoProcessRequest", xmlMsoCompletionRequest)
				utils.log("DEBUG", " Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)
		} else {
				execution.setVariable(prefix+"isErrorMessageException", true)
				utils.log("DEBUG", " DB Update failed, code: " + dbReturnCode + ", going to Unexpected Error.", isDebugEnabled)
		}
	}

}
