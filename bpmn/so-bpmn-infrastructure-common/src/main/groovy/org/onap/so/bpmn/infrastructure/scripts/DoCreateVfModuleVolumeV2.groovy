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
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.NetworkUtils
import org.onap.so.bpmn.common.scripts.VfModuleBase;
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.springframework.web.util.UriUtils
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIObjectPlurals
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.Relationships
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.constants.Defaults
import org.json.JSONObject
import javax.ws.rs.NotFoundException

class DoCreateVfModuleVolumeV2 extends VfModuleBase {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateVfModuleVolumeV2.class);
	String prefix='DCVFMODVOLV2_'
	JsonUtils jsonUtil = new JsonUtils()


    /**
     * Perform initial processing, such as request validation, initialization of variables, etc.
     * * @param execution
     */
    public void preProcessRequest(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        preProcessRequest(execution, isDebugEnabled)
    }

    public void preProcessRequest(DelegateExecution execution, isDebugLogEnabled) {

		execution.setVariable("prefix",prefix)
		execution.setVariable(prefix+'SuccessIndicator', false)
		execution.setVariable(prefix+'isPONR', false)

		displayInput(execution, isDebugLogEnabled)
		setRollbackData(execution, isDebugLogEnabled)
		setRollbackEnabled(execution, isDebugLogEnabled)


		def tenantId = execution.getVariable("tenantId")
		if (tenantId == null) {
			String cloudConfiguration = execution.getVariable("cloudConfiguration")
			tenantId = jsonUtil.getJsonValue(cloudConfiguration, "cloudConfiguration.tenantId")
			execution.setVariable("tenantId", tenantId)
		}

		def cloudSiteId = execution.getVariable("lcpCloudRegionId")
		if (cloudSiteId == null) {
			String cloudConfiguration = execution.getVariable("cloudConfiguration")
			cloudSiteId = jsonUtil.getJsonValue(cloudConfiguration, "cloudConfiguration.lcpCloudRegionId")
			def cloudOwner = jsonUtil.getJsonValue(cloudConfiguration, "cloudConfiguration.cloudOwner")
			execution.setVariable("lcpCloudRegionId", cloudSiteId)
			execution.setVariable("cloudOwner", cloudOwner)
		}

		// Extract attributes from modelInfo
		String vfModuleModelInfo = execution.getVariable("vfModuleModelInfo")

		//modelCustomizationUuid
		def modelCustomizationUuid = jsonUtil.getJsonValue(vfModuleModelInfo, "modelCustomizationUuid")
		execution.setVariable("modelCustomizationId", modelCustomizationUuid)
		msoLogger.debug("modelCustomizationId: " + modelCustomizationUuid)

		//modelName
		def modelName = jsonUtil.getJsonValue(vfModuleModelInfo, "modelName")
		execution.setVariable("modelName", modelName)
		msoLogger.debug("modelName: " + modelName)

		// The following is used on the get Generic Service Instance call
		execution.setVariable('GENGS_type', 'service-instance')
	}


	/**
	 * Display input variables
	 * @param execution
	 * @param isDebugLogEnabled
	 */
	public void displayInput(DelegateExecution execution, isDebugLogEnabled) {
		def input = ['mso-request-id', 'msoRequestId', 'isDebugLogEnabled', 'disableRollback', 'failIfExists', 'serviceInstanceId',
			'vnfId', 'vnfName', 'tenantId', 'volumeGroupId', 'volumeGroupName', 'lcpCloudRegionId', 'vnfType', 'vfModuleModelInfo',  'asdcServiceModelVersion',
			'test-volume-group-name', 'test-volume-group-id', 'vfModuleInputParams']

		msoLogger.debug('Begin input: ')
		input.each {
			msoLogger.debug(it + ': ' + execution.getVariable(it))
		}
		msoLogger.debug('End input.')
	}


	/**
	 * Define and set rollbackdata object
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void setRollbackData(DelegateExecution execution, isDebugEnabled) {
		def rollbackData = execution.getVariable("rollbackData")
		if (rollbackData == null) {
			rollbackData = new RollbackData()
		}
		def volumeGroupName = execution.getVariable('volumeGroupName')
		rollbackData.put("DCVFMODULEVOL", "volumeGroupName", volumeGroupName)
		execution.setVariable("rollbackData", rollbackData)
	}


	/**
	 * Gets the service instance uri from aai
	 */
	public void getServiceInstance(DelegateExecution execution) {
		try {
			String serviceInstanceId = execution.getVariable('serviceInstanceId')

			AAIResourcesClient resourceClient = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)

			if(!resourceClient.exists(uri)){
				(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, "Service instance was not found in aai")
			}

		}catch(BpmnError e) {
			throw e;
		}catch (Exception ex){
			String msg = "Exception in getServiceInstance. " + ex.getMessage()
			msoLogger.debug(msg)
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, msg)
		}
	}

	/**
	 * Get cloud region
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTQueryAAICloudRegion (DelegateExecution execution, isDebugEnabled) {

		def cloudRegion = execution.getVariable("lcpCloudRegionId")
		msoLogger.debug('Request cloud region is: ' + cloudRegion)

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION, Defaults.CLOUD_OWNER.toString(), cloudRegion)
		def queryCloudRegionRequest = aaiUtil.createAaiUri(uri)

		cloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)

		def aaiCloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "AAI", cloudRegion)
		if ((aaiCloudRegion != "ERROR")) {
			execution.setVariable("lcpCloudRegionId", aaiCloudRegion)
			msoLogger.debug("AIC Cloud Region for AAI: " + aaiCloudRegion)
		} else {
			String errorMessage = "AAI Query Cloud Region Unsuccessful. Return Code: " + execution.getVariable(prefix+"queryCloudRegionReturnCode")
			msoLogger.debug(errorMessage)
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, errorMessage)
		}

		def poCloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)
		if ((poCloudRegion != "ERROR")) {
			execution.setVariable("poLcpCloudRegionId", poCloudRegion)
			msoLogger.debug("AIC Cloud Region for PO: " + poCloudRegion)
		} else {
			String errorMessage = "AAI Query Cloud Region Unsuccessful. Return Code: " + execution.getVariable(prefix+"queryCloudRegionReturnCode")
			msoLogger.debug(errorMessage)
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, errorMessage)
		}

		def rollbackData = execution.getVariable("rollbackData")
		rollbackData.put("DCVFMODULEVOL", "aiccloudregion", cloudRegion)
	}


	/**
	 * Query AAI volume group by name
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTQueryAAIVolGrpName(DelegateExecution execution, isDebugEnabled) {

		def volumeGroupName = execution.getVariable('volumeGroupName')
		def cloudRegion = execution.getVariable('lcpCloudRegionId')

		// This is for stub testing
		def testVolumeGroupName = execution.getVariable('test-volume-group-name')
		if (testVolumeGroupName != null && testVolumeGroupName.length() > 0) {
			volumeGroupName = testVolumeGroupName
		}

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), cloudRegion).queryParam("volume-group-name", volumeGroupName)
		def queryAAIVolumeNameRequest = aaiUtil.createAaiUri(uri)

		msoLogger.debug('Query AAI volume group by name: ' + queryAAIVolumeNameRequest)

		APIResponse response = aaiUtil.executeAAIGetCall(execution, queryAAIVolumeNameRequest)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI query volume group by name return code: " + returnCode)
		msoLogger.debug("AAI query volume group by name response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		execution.setVariable(prefix+"queryAAIVolGrpNameResponse", aaiResponseAsString)
		execution.setVariable(prefix+'AaiReturnCode', returnCode)

		if (returnCode=='200') {
			execution.setVariable(prefix+'queryAAIVolGrpNameResponse', aaiResponseAsString)
			msoLogger.debug("Volume Group Name $volumeGroupName exists in AAI.")
		} else {
			if (returnCode=='404') {
				msoLogger.debug("Volume Group Name $volumeGroupName does not exist in AAI.")
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
	public void buildWorkflowException(DelegateExecution execution, int errorCode, errorMessage, isDebugEnabled) {
		msoLogger.debug(errorMessage)
		(new ExceptionUtil()).buildWorkflowException(execution, 2500, errorMessage)
	}


	/**
	 * Create a WorkflowException
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void handleError(DelegateExecution execution, isDebugEnabled) {
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
	public void callRESTCreateAAIVolGrpName(DelegateExecution execution, isDebugEnabled) {

		def vnfId = execution.getVariable('vnfId')
		def volumeGroupId = execution.getVariable('volumeGroupId')
		def volumeName = execution.getVariable("volumeGroupName")
		def modelCustomizationId = execution.getVariable("modelCustomizationId")
		def vnfType = execution.getVariable("vnfType")
		def tenantId = execution.getVariable("tenantId")
		def cloudRegion = execution.getVariable('lcpCloudRegionId')
		def cloudOwner = execution.getVariable('cloudOwner')
		msoLogger.debug("volumeGroupId: " + volumeGroupId)

		def testGroupId = execution.getVariable('test-volume-group-id')
		if (testGroupId != null && testGroupId.trim() != '') {
			msoLogger.debug("test volumeGroupId is present: " + testGroupId)
			volumeGroupId = testGroupId
			execution.setVariable("test-volume-group-name", "MSOTESTVOL101a-vSAMP12_base_vol_module-0")
		}

		msoLogger.debug("volumeGroupId to be used: " + volumeGroupId)

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), cloudRegion, volumeGroupId)
		def createAAIVolumeGrpNameUrlRequest = aaiUtil.createAaiUri(uri)

		String namespace =  aaiUtil.getNamespaceFromUri(createAAIVolumeGrpNameUrlRequest)
		msoLogger.debug("AAI namespace is: " + namespace)

		msoLogger.debug("Request URL for PUT: " + createAAIVolumeGrpNameUrlRequest)

		NetworkUtils networkUtils = new NetworkUtils()
		String payload = networkUtils.createCloudRegionVolumeRequest(volumeGroupId, volumeName, vnfType, vnfId, tenantId, cloudOwner, cloudRegion, namespace, modelCustomizationId)
		String payloadXml = utils.formatXml(payload)
		msoLogger.debug("Request payload for PUT: " + payloadXml)

		APIResponse response = aaiUtil.executeAAIPutCall(execution, createAAIVolumeGrpNameUrlRequest, payloadXml)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI create volume group return code: " + returnCode)
		msoLogger.debug("AAI create volume group response: " + aaiResponseAsString)

		execution.setVariable(prefix+"createAAIVolumeGrpNameReturnCode", returnCode)
		execution.setVariable(prefix+"createAAIVolumeGrpNameResponse", aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		if (returnCode =='201') {
			RollbackData rollbackData = execution.getVariable("rollbackData")
			rollbackData.put("DCVFMODULEVOL", "isAAIRollbackNeeded", "true")
		} else {
			execution.setVariable(prefix+"isErrorMessageException", true)
			if (returnCode=='404') {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Unable to create volume group in AAI. Response code: 404")
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				msoLogger.debug(" AAI Adapter Query Failed.  WorkflowException - " + "\n" + aWorkflowException)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}


	/**
	 * Prepare VNF adapter create request XML
	 * @param execution
	 */
	public void prepareVnfAdapterCreateRequest(DelegateExecution execution, isDebugEnabled) {

		def aaiGenericVnfResponse = execution.getVariable(prefix+'AAIQueryGenericVfnResponse')
		def vnfId = utils.getNodeText(aaiGenericVnfResponse, 'vnf-id')
		def vnfName = utils.getNodeText(aaiGenericVnfResponse, 'vnf-name')
		def vnfType = utils.getNodeText(aaiGenericVnfResponse, "vnf-type")

		def requestId = execution.getVariable('msoRequestId')
		def serviceId = execution.getVariable('serviceInstanceId')
		def cloudSiteId = execution.getVariable('poLcpCloudRegionId')
		def tenantId = execution.getVariable('tenantId')
		def volumeGroupId = execution.getVariable('volumeGroupId')
		def volumeGroupnName = execution.getVariable('volumeGroupName')

		def vnfVersion = execution.getVariable("asdcServiceModelVersion")
		def vnfModuleType = execution.getVariable("modelName")

		def modelCustomizationId = execution.getVariable("modelCustomizationId")

		// for testing
		msoLogger.debug("volumeGroupId: " + volumeGroupId)
		def testGroupId = execution.getVariable('test-volume-group-id')
		if (testGroupId != null && testGroupId.trim() != '') {
			msoLogger.debug("test volumeGroupId is present: " + testGroupId)
			volumeGroupId = testGroupId
			execution.setVariable("test-volume-group-name", "MSOTESTVOL101a-vSAMP12_base_vol_module-0")
		}
		msoLogger.debug("volumeGroupId to be used: " + volumeGroupId)

		// volume group parameters

		String volumeGroupParams = ''
		StringBuilder sbParams = new StringBuilder()
		Map<String, String> paramsMap = execution.getVariable("vfModuleInputParams")
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			String paramsXml
			String paramName = entry.getKey();
			String paramValue = entry.getValue()
			paramsXml =
				"""	<entry>
			   <key>${MsoUtils.xmlEscape(paramName)}</key>
			   <value>${MsoUtils.xmlEscape(paramValue)}</value>
			</entry>
			"""
			sbParams.append(paramsXml)
		}

		volumeGroupParams = sbParams.toString()
		msoLogger.debug("volumeGroupParams: "+ volumeGroupParams)

		def backoutOnFailure = execution.getVariable(prefix+"backoutOnFailure")
		msoLogger.debug("backoutOnFailure: "+ backoutOnFailure)

		def failIfExists = execution.getVariable("failIfExists")
		if(failIfExists == null) {
			failIfExists = 'true'
		}

		String messageId = UUID.randomUUID()
		msoLogger.debug("messageId to be used is generated: " + messageId)

		def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
		def useQualifiedHostName = UrnPropertiesReader.getVariable("mso.use.qualified.host",execution)
		if ('true'.equals(useQualifiedHostName)) {
			notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}
		msoLogger.debug("CreateVfModuleVolume - notificationUrl: "+ notificationUrl)

		// build request
		String vnfSubCreateWorkflowRequest =
				"""
			<createVolumeGroupRequest>
				<cloudSiteId>${MsoUtils.xmlEscape(cloudSiteId)}</cloudSiteId>
				<tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
				<vnfId>${MsoUtils.xmlEscape(vnfId)}</vnfId>
				<vnfName>${MsoUtils.xmlEscape(vnfName)}</vnfName>
				<volumeGroupId>${MsoUtils.xmlEscape(volumeGroupId)}</volumeGroupId>
				<volumeGroupName>${MsoUtils.xmlEscape(volumeGroupnName)}</volumeGroupName>
				<vnfType>${MsoUtils.xmlEscape(vnfType)}</vnfType>
				<vnfVersion>${MsoUtils.xmlEscape(vnfVersion)}</vnfVersion>
				<vfModuleType>${MsoUtils.xmlEscape(vnfModuleType)}</vfModuleType>
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
						<value>${MsoUtils.xmlEscape(volumeGroupnName)}</value>
					</entry>
					${volumeGroupParams}
			    </volumeGroupParams>
				<skipAAI>true</skipAAI>
				<backout>${MsoUtils.xmlEscape(backoutOnFailure)}</backout>
				<failIfExists>${MsoUtils.xmlEscape(failIfExists)}</failIfExists>
			    <msoRequest>
			        <requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
			        <serviceInstanceId>${MsoUtils.xmlEscape(serviceId)}</serviceInstanceId>
			    </msoRequest>
			    <messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
			    <notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
			</createVolumeGroupRequest>
		"""

		String vnfSubCreateWorkflowRequestAsString = utils.formatXml(vnfSubCreateWorkflowRequest)
		msoLogger.debug(vnfSubCreateWorkflowRequestAsString)
		msoLogger.debug(vnfSubCreateWorkflowRequestAsString)
		execution.setVariable(prefix+"createVnfARequest", vnfSubCreateWorkflowRequestAsString)

		// build rollback request for use later if needed
		String vnfSubRollbackWorkflowRequest = buildRollbackVolumeGroupRequestXml(volumeGroupId, cloudSiteId, tenantId, requestId, serviceId, messageId, notificationUrl)

		msoLogger.debug("Sub Vnf flow rollback request: vnfSubRollbackWorkflowRequest " + "\n" + vnfSubRollbackWorkflowRequest)

		String vnfSubRollbackWorkflowRequestAsString = utils.formatXml(vnfSubRollbackWorkflowRequest)
		execution.setVariable(prefix+"rollbackVnfARequest", vnfSubRollbackWorkflowRequestAsString)
	}

	public String buildRollbackVolumeGroupRequestXml(volumeGroupId, cloudSiteId, tenantId, requestId, serviceId, messageId, notificationUrl) {

		def request = """
		<rollbackVolumeGroupRequest>
			<volumeGroupRollback>
			   <volumeGroupId>${MsoUtils.xmlEscape(volumeGroupId)}</volumeGroupId>
			   <volumeGroupStackId>{{VOLUMEGROUPSTACKID}}</volumeGroupStackId>
			   <tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
			   <cloudSiteId>${MsoUtils.xmlEscape(cloudSiteId)}</cloudSiteId>
			   <volumeGroupCreated>true</volumeGroupCreated>
			   <msoRequest>
			      <requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
			      <serviceInstanceId>${MsoUtils.xmlEscape(serviceId)}</serviceInstanceId>
			   </msoRequest>
			   <messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
			</volumeGroupRollback>
			<skipAAI>true</skipAAI>
			<notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
		</rollbackVolumeGroupRequest>
		"""

		return request
	}

	public String updateRollbackVolumeGroupRequestXml(String rollabackRequest, String heatStackId) {
		String newRequest = rollabackRequest.replace("{{VOLUMEGROUPSTACKID}}", heatStackId)
		return newRequest
	}

	/**
	 * Validate VNF adapter response
	 * @param execution
	 */
	public void validateVnfResponse(DelegateExecution execution, isDebugEnabled) {
		def vnfSuccess = execution.getVariable('VNFREST_SuccessIndicator')
		msoLogger.debug("vnfAdapterSuccessIndicator: "+ vnfSuccess)
		if(vnfSuccess==true) {
			String createVnfAResponse = execution.getVariable(prefix+"createVnfAResponse")
			String heatStackID = utils.getNodeText(createVnfAResponse, "volumeGroupStackId")
			String vnfRollbackRequest = execution.getVariable(prefix+"rollbackVnfARequest")
			String updatedVnfRollbackRequest = updateRollbackVolumeGroupRequestXml(vnfRollbackRequest, heatStackID)
			msoLogger.debug("vnfAdapter rollback request: "+ updatedVnfRollbackRequest)
			RollbackData rollbackData = execution.getVariable("rollbackData")
			rollbackData.put("DCVFMODULEVOL", "rollbackVnfARequest", updatedVnfRollbackRequest)
			rollbackData.put("DCVFMODULEVOL", "isCreateVnfRollbackNeeded", "true")
		}
	}


	/**
	 * Update voulume group in AAI
	 * @TODO: Can we re-use the create method??
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTUpdateCreatedVolGrpName(DelegateExecution execution, isDebugEnabled) {

		String requeryAAIVolGrpNameResponse = execution.getVariable(prefix+"queryAAIVolGrpNameResponse")
		String volumeGroupId = utils.getNodeText(requeryAAIVolGrpNameResponse, "volume-group-id")
		String modelCustomizationId = execution.getVariable("modelCustomizationId")
		String cloudRegion = execution.getVariable("lcpCloudRegionId")

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), cloudRegion, volumeGroupId)
		def updateAAIVolumeGroupUrlRequest = aaiUtil.createAaiUri(uri)

		String namespace =  aaiUtil.getNamespaceFromUri(updateAAIVolumeGroupUrlRequest)

		msoLogger.debug("updateAAIVolumeGroupUrlRequest - " +  updateAAIVolumeGroupUrlRequest)

		String createVnfAResponse = execution.getVariable(prefix+"createVnfAResponse")
		def heatStackID = utils.getNodeText(createVnfAResponse, "volumeGroupStackId")

		execution.setVariable(prefix+"heatStackId", heatStackID)

		NetworkUtils networkUtils = new NetworkUtils()
		String payload = networkUtils.updateCloudRegionVolumeRequest(requeryAAIVolGrpNameResponse, heatStackID, namespace, modelCustomizationId)
		String payloadXml = utils.formatXml(payload)

		msoLogger.debug("Payload to Update Created VolumeGroupName - " + "\n" + payloadXml)

		APIResponse response = aaiUtil.executeAAIPutCall(execution, updateAAIVolumeGroupUrlRequest, payloadXml)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI create volume group return code: " + returnCode)
		msoLogger.debug("AAI create volume group response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		if (returnCode =='200') {
			execution.setVariable(prefix+"updateCreatedAAIVolumeGrpNameResponse", aaiResponseAsString)
			execution.setVariable(prefix+"isPONR", true)
		} else {
			execution.setVariable(prefix+"isErrorMessageException", true)
			if (returnCode=='404') {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Unable to update volume group in AAI. Response code: 404")
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				msoLogger.debug(" AAI Adapter Query Failed.  WorkflowException - " + "\n" + aWorkflowException)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}


	/**
	 * Query AAI Generic VNF
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTQueryAAIGenericVnf(DelegateExecution execution, isDebugEnabled) {

		def vnfId = execution.getVariable('vnfId')

		AaiUtil aaiUtil = new AaiUtil(this)
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId)
		String queryAAIRequest = aaiUtil.createAaiUri(uri)

		msoLogger.debug("AAI query generic vnf endpoint: " + queryAAIRequest)
		
		APIResponse response = aaiUtil.executeAAIGetCall(execution, queryAAIRequest)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI query generic vnf return code: " + returnCode)
		msoLogger.debug("AAI query generic vnf response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		if (returnCode=='200') {
			msoLogger.debug('Generic vnf ' + vnfId + ' found in AAI.')
			execution.setVariable(prefix+'AAIQueryGenericVfnResponse', aaiResponseAsString)
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

}
