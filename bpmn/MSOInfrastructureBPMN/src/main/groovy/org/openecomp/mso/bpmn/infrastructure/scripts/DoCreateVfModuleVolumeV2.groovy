package org.openecomp.mso.bpmn.infrastructure.scripts

import static org.camunda.spin.Spin.XML;
import groovy.json.JsonSlurper
import groovy.lang.GroovyInterceptable;
import groovy.xml.XmlUtil

import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils
import org.openecomp.mso.bpmn.common.scripts.VfModuleBase;
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils;
import org.openecomp.mso.rest.APIResponse
import org.springframework.web.util.UriUtils




import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;

class DoCreateVfModuleVolumeV2 extends VfModuleBase {

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
			execution.setVariable("lcpCloudRegionId", cloudSiteId)
		}

		// Extract attributes from modelInfo
		String vfModuleModelInfo = execution.getVariable("vfModuleModelInfo")

		//modelCustomizationUuid
		def modelCustomizationUuid = jsonUtil.getJsonValue(vfModuleModelInfo, "modelCustomizationUuid")
		execution.setVariable("modelCustomizationId", modelCustomizationUuid)
		logDebug("modelCustomizationId: " + modelCustomizationUuid, isDebugLogEnabled)
		
		//modelName
		def modelName = jsonUtil.getJsonValue(vfModuleModelInfo, "modelName")
		execution.setVariable("modelName", modelName)
		logDebug("modelName: " + modelName, isDebugLogEnabled)
		
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

		logDebug('Begin input: ', isDebugLogEnabled)
		input.each {
			logDebug(it + ': ' + execution.getVariable(it), isDebugLogEnabled)
		}
		logDebug('End input.', isDebugLogEnabled)
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
	 * validate getServiceInstance response
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void validateGetServiceInstanceCall(DelegateExecution execution, isDebugEnabled) {
		def found = execution.getVariable('GENGS_FoundIndicator')
		def success = execution.getVariable('GENGS_SuccessIndicator')
		def serviceInstanceId = execution.getVariable('serviceInstanceId')
		utils.log("DEBUG", "getServiceInstance success: " + success, isDebugEnabled)
		utils.log("DEBUG", "getServiceInstance found: " + found, isDebugEnabled)
		if(!found || !success) {
			String errorMessage = "Service instance id not found in AAI: ${serviceInstanceId}."
			utils.log("DEBUG", errorMessage, isDebugEnabled)
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, errorMessage)
		}
		
	}

	/**
	 * Get cloud region
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTQueryAAICloudRegion (DelegateExecution execution, isDebugEnabled) {

		def cloudRegion = execution.getVariable("lcpCloudRegionId")
		utils.log("DEBUG", 'Request cloud region is: ' + cloudRegion, isDebugEnabled)

		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = aaiUtil.getCloudInfrastructureCloudRegionEndpoint(execution)
		String queryCloudRegionRequest = aaiEndpoint + '/' + cloudRegion

		utils.logAudit(queryCloudRegionRequest)

		cloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)

		def aaiCloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "AAI", cloudRegion)
		if ((aaiCloudRegion != "ERROR")) {
			execution.setVariable("lcpCloudRegionId", aaiCloudRegion)
			utils.log("DEBUG", "AIC Cloud Region for AAI: " + aaiCloudRegion, isDebugEnabled)
		} else {
			String errorMessage = "AAI Query Cloud Region Unsuccessful. Return Code: " + execution.getVariable(prefix+"queryCloudRegionReturnCode")
			utils.log("DEBUG", errorMessage, isDebugEnabled)
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, errorMessage)
		}
		
		def poCloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)
		if ((poCloudRegion != "ERROR")) {
			execution.setVariable("poLcpCloudRegionId", poCloudRegion)
			utils.log("DEBUG", "AIC Cloud Region for PO: " + poCloudRegion, isDebugEnabled)
		} else {
			String errorMessage = "AAI Query Cloud Region Unsuccessful. Return Code: " + execution.getVariable(prefix+"queryCloudRegionReturnCode")
			utils.log("DEBUG", errorMessage, isDebugEnabled)
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
	public void buildWorkflowException(DelegateExecution execution, int errorCode, errorMessage, isDebugEnabled) {
		utils.log("DEBUG", errorMessage, isDebugEnabled)
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
		
		utils.log("DEBUG", "volumeGroupId: " + volumeGroupId, isDebugEnabled)

		def testGroupId = execution.getVariable('test-volume-group-id')
		if (testGroupId != null && testGroupId.trim() != '') {
			utils.log("DEBUG", "test volumeGroupId is present: " + testGroupId, isDebugEnabled)
			volumeGroupId = testGroupId
			execution.setVariable("test-volume-group-name", "MSOTESTVOL101a-vSAMP12_base_vol_module-0")
		}

		utils.log("DEBUG", "volumeGroupId to be used: " + volumeGroupId, isDebugEnabled)

		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = aaiUtil.getCloudInfrastructureCloudRegionEndpoint(execution)
		String createAAIVolumeGrpNameUrlRequest = aaiEndpoint + '/' + cloudRegion + "/volume-groups/volume-group/" + UriUtils.encode(volumeGroupId, "UTF-8")

		String namespace =  aaiUtil.getNamespaceFromUri(aaiUtil.getCloudInfrastructureCloudRegionUri(execution))
		utils.log("DEBUG", "AAI namespace is: " + namespace, isDebugEnabled)

		utils.logAudit("Request URL for PUT: " + createAAIVolumeGrpNameUrlRequest)

		NetworkUtils networkUtils = new NetworkUtils()
		String payload = networkUtils.createCloudRegionVolumeRequest(volumeGroupId, volumeName, vnfType, vnfId, tenantId, cloudRegion, namespace, modelCustomizationId)
		String payloadXml = utils.formatXml(payload)
		utils.logAudit("Request payload for PUT: " + payloadXml)

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
			RollbackData rollbackData = execution.getVariable("rollbackData")
			rollbackData.put("DCVFMODULEVOL", "isAAIRollbackNeeded", "true")
		} else {
			execution.setVariable(prefix+"isErrorMessageException", true)
			if (returnCode=='404') {
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
	public void prepareVnfAdapterCreateRequest(DelegateExecution execution, isDebugEnabled) {

		def aaiGenericVnfResponse = execution.getVariable(prefix+'AAIQueryGenericVfnResponse')
		def vnfId = utils.getNodeText1(aaiGenericVnfResponse, 'vnf-id')
		def vnfName = utils.getNodeText1(aaiGenericVnfResponse, 'vnf-name')
		def vnfType = utils.getNodeText1(aaiGenericVnfResponse, "vnf-type")
		
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
		utils.log("DEBUG", "volumeGroupId: " + volumeGroupId, isDebugEnabled)
		def testGroupId = execution.getVariable('test-volume-group-id')
		if (testGroupId != null && testGroupId.trim() != '') {
			utils.log("DEBUG", "test volumeGroupId is present: " + testGroupId, isDebugEnabled)
			volumeGroupId = testGroupId
			execution.setVariable("test-volume-group-name", "MSOTESTVOL101a-vSAMP12_base_vol_module-0")
		}
		utils.log("DEBUG", "volumeGroupId to be used: " + volumeGroupId, isDebugEnabled)
				
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
			   <key>${paramName}</key>
			   <value>${paramValue}</value>
			</entry>
			"""
			sbParams.append(paramsXml)
		}

		volumeGroupParams = sbParams.toString()
		utils.log("DEBUG", "volumeGroupParams: "+ volumeGroupParams, isDebugEnabled)

		def backoutOnFailure = execution.getVariable(prefix+"backoutOnFailure")
		utils.log("DEBUG", "backoutOnFailure: "+ backoutOnFailure, isDebugEnabled)

		def failIfExists = execution.getVariable("failIfExists")
		if(failIfExists == null) {
			failIfExists = 'true'
		}
		
		String messageId = UUID.randomUUID()
		utils.log("DEBUG", "messageId to be used is generated: " + messageId, isDebugEnabled)
		
		def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
		def useQualifiedHostName = execution.getVariable("URN_mso_use_qualified_host")
		if ('true'.equals(useQualifiedHostName)) {
			notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}
		utils.log("DEBUG", "CreateVfModuleVolume - notificationUrl: "+ notificationUrl, isDebugEnabled)
		
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
				<failIfExists>${failIfExists}</failIfExists>
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
		String vnfSubRollbackWorkflowRequest = buildRollbackVolumeGroupRequestXml(volumeGroupId, cloudSiteId, tenantId, requestId, serviceId, messageId, notificationUrl)

		utils.log("DEBUG", "Sub Vnf flow rollback request: vnfSubRollbackWorkflowRequest " + "\n" + vnfSubRollbackWorkflowRequest, isDebugEnabled)

		String vnfSubRollbackWorkflowRequestAsString = utils.formatXml(vnfSubRollbackWorkflowRequest)
		execution.setVariable(prefix+"rollbackVnfARequest", vnfSubRollbackWorkflowRequestAsString)
	}
	
	public String buildRollbackVolumeGroupRequestXml(volumeGroupId, cloudSiteId, tenantId, requestId, serviceId, messageId, notificationUrl) {
		
		def request = """
		<rollbackVolumeGroupRequest>
			<volumeGroupRollback>
			   <volumeGroupId>${volumeGroupId}</volumeGroupId>
			   <volumeGroupStackId>{{VOLUMEGROUPSTACKID}}</volumeGroupStackId>
			   <tenantId>${tenantId}</tenantId>
			   <cloudSiteId>${cloudSiteId}</cloudSiteId>
			   <volumeGroupCreated>true</volumeGroupCreated>
			   <msoRequest>
			      <requestId>${requestId}</requestId>
			      <serviceInstanceId>${serviceId}</serviceInstanceId>
			   </msoRequest>
			   <messageId>${messageId}</messageId>
			</volumeGroupRollback>
			<skipAAI>true</skipAAI>
			<notificationUrl>${notificationUrl}</notificationUrl>
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
		utils.log("DEBUG", "vnfAdapterSuccessIndicator: "+ vnfSuccess, isDebugEnabled)
		if(vnfSuccess==true) {
			String createVnfAResponse = execution.getVariable(prefix+"createVnfAResponse")
			String heatStackID = utils.getNodeText1(createVnfAResponse, "volumeGroupStackId")
			String vnfRollbackRequest = execution.getVariable(prefix+"rollbackVnfARequest")
			String updatedVnfRollbackRequest = updateRollbackVolumeGroupRequestXml(vnfRollbackRequest, heatStackID)
			utils.log("DEBUG", "vnfAdapter rollback request: "+ updatedVnfRollbackRequest, isDebugEnabled)
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
		String volumeGroupId = utils.getNodeText1(requeryAAIVolGrpNameResponse, "volume-group-id")
		String modelCustomizationId = execution.getVariable("modelCustomizationId")  
		String cloudRegion = execution.getVariable("lcpCloudRegionId")

		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = aaiUtil.getCloudInfrastructureCloudRegionEndpoint(execution)
		String updateAAIVolumeGroupUrlRequest = aaiEndpoint + '/' + cloudRegion + "/volume-groups/volume-group/" + UriUtils.encode(volumeGroupId, 'UTF-8')

		String namespace =  aaiUtil.getNamespaceFromUri(aaiUtil.getCloudInfrastructureCloudRegionUri(execution))

		utils.logAudit(updateAAIVolumeGroupUrlRequest)
		utils.log("DEBUG", "updateAAIVolumeGroupUrlRequest - " +  updateAAIVolumeGroupUrlRequest, isDebugEnabled)

		String createVnfAResponse = execution.getVariable(prefix+"createVnfAResponse")
		def heatStackID = utils.getNodeText1(createVnfAResponse, "volumeGroupStackId")
		
		execution.setVariable(prefix+"heatStackId", heatStackID)

		NetworkUtils networkUtils = new NetworkUtils()
		String payload = networkUtils.updateCloudRegionVolumeRequest(requeryAAIVolGrpNameResponse, heatStackID, namespace, modelCustomizationId)
		String payloadXml = utils.formatXml(payload)

		utils.logAudit("Payload to Update Created VolumeGroupName - " + "\n" + payloadXml)

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
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Unable to update volume group in AAI. Response code: 404")
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				utils.log("DEBUG", " AAI Adapter Query Failed.  WorkflowException - " + "\n" + aWorkflowException, isDebugEnabled)
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

}
