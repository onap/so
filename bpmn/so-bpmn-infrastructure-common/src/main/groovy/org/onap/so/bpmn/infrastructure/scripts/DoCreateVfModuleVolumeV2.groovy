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

package org.onap.so.bpmn.infrastructure.scripts

import jakarta.ws.rs.NotFoundException
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.aai.domain.yang.VolumeGroups
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.VfModuleBase
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.constants.Defaults
import org.onap.so.db.catalog.beans.OrchestrationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoCreateVfModuleVolumeV2 extends VfModuleBase {

    private static final Logger logger = LoggerFactory.getLogger( DoCreateVfModuleVolumeV2.class);
    String prefix='DCVFMODVOLV2_'
    JsonUtils jsonUtil = new JsonUtils()
    private ExceptionUtil exceptionUtil = new ExceptionUtil()


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
        logger.debug("modelCustomizationId: " + modelCustomizationUuid)

        //modelName
        def modelName = jsonUtil.getJsonValue(vfModuleModelInfo, "modelName")
        execution.setVariable("modelName", modelName)
        logger.debug("modelName: " + modelName)

        // The following is used on the get Generic Service Instance call
        execution.setVariable('GENGS_type', 'service-instance')
    }


    /**
     * Display input variables
     * @param execution
     * @param isDebugLogEnabled
     */
    public void displayInput(DelegateExecution execution, isDebugLogEnabled) {
        def input = ['mso-request-id', 'msoRequestId', 'isDebugLogEnabled', 'disableRollback', 'failIfExists', 'serviceInstanceId', 'vnfId', 'vnfName', 'tenantId', 'volumeGroupId', 'volumeGroupName', 'lcpCloudRegionId', 'vnfType', 'vfModuleModelInfo', 'asdcServiceModelVersion', 'test-volume-group-name', 'test-volume-group-id', 'vfModuleInputParams']

        logger.debug('Begin input: ')
        input.each {
            logger.debug(it + ': ' + execution.getVariable(it))
        }
        logger.debug('End input.')
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
            AAIResourceUri uri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))

            if(!resourceClient.exists(uri)){
                (new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, "Service instance was not found in aai")
            }
        }catch(BpmnError e) {
            throw e
        }catch (Exception ex){
            String msg = "Exception in getServiceInstance. " + ex.getMessage()
            logger.debug(msg)
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
        logger.debug('Request cloud region is: ' + cloudRegion)

        AaiUtil aaiUtil = new AaiUtil(this)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), cloudRegion))
        def queryCloudRegionRequest = aaiUtil.createAaiUri(uri)

        cloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)

        def aaiCloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "AAI", cloudRegion)
        if ((aaiCloudRegion != "ERROR")) {
            execution.setVariable("lcpCloudRegionId", aaiCloudRegion)
            logger.debug("AIC Cloud Region for AAI: " + aaiCloudRegion)
        } else {
            String errorMessage = "AAI Query Cloud Region Unsuccessful. Return Code: " + execution.getVariable(prefix+"queryCloudRegionReturnCode")
            logger.debug(errorMessage)
            (new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, errorMessage)
        }

        def poCloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)
        if ((poCloudRegion != "ERROR")) {
            execution.setVariable("poLcpCloudRegionId", poCloudRegion)
            logger.debug("AIC Cloud Region for PO: " + poCloudRegion)
        } else {
            String errorMessage = "AAI Query Cloud Region Unsuccessful. Return Code: " + execution.getVariable(prefix+"queryCloudRegionReturnCode")
            logger.debug(errorMessage)
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

        try {
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), cloudRegion).volumeGroups()).queryParam("volume-group-name", volumeGroupName)
            Optional<VolumeGroups> volumeGroups = getAAIClient().get(VolumeGroups.class,uri)
            if(volumeGroups.isPresent()){
                VolumeGroup volumeGroup = volumeGroups.get().getVolumeGroup().get(0);
                execution.setVariable(prefix+'AaiReturnCode', 200)
                execution.setVariable("queriedVolumeGroupId",volumeGroup.getVolumeGroupId())
                logger.debug("Volume Group Name $volumeGroupName exists in AAI.")
            }else{
                execution.setVariable(prefix+'AaiReturnCode', 404)
                exceptionUtil.buildAndThrowWorkflowException(execution,25000, "Volume Group Name $volumeGroupName does not exist in AAI.")
            }
        }catch(BpmnError error){
            throw error
        }catch(Exception e){
            execution.setVariable(prefix+'AaiReturnCode', 500)
            exceptionUtil.buildAndThrowWorkflowException(execution,25000, "Exception in get volume group by name: " + e.getMessage())
        }
    }


    /**
     * Create a WorkflowException
     * @param execution
     * @param isDebugEnabled
     */
    public void buildWorkflowException(DelegateExecution execution, int errorCode, errorMessage) {
        logger.debug(errorMessage)
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

        def testGroupId = execution.getVariable('test-volume-group-id')
        if (testGroupId != null && testGroupId.trim() != '') {
            logger.debug("test volumeGroupId is present: " + testGroupId)
            volumeGroupId = testGroupId
            execution.setVariable("test-volume-group-name", "MSOTESTVOL101a-vSAMP12_base_vol_module-0")
        }

        VolumeGroup volumeGroup = new VolumeGroup()
        volumeGroup.setVolumeGroupId(volumeGroupId)
        volumeGroup.setVolumeGroupName(volumeName)
        volumeGroup.setVnfType(vnfType)
        volumeGroup.setOrchestrationStatus(OrchestrationStatus.PENDING.toString())
        volumeGroup.setModelCustomizationId(modelCustomizationId)

        logger.debug("volumeGroupId to be used: " + volumeGroupId)

        AAIResourceUri volumeGroupUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(cloudOwner, cloudRegion).volumeGroup(volumeGroupId))
        AAIResourceUri tenantUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(cloudOwner, cloudRegion).tenant(tenantId))
        AAIResourceUri vnfUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
        try {
            getAAIClient().create(volumeGroupUri, volumeGroup)
            getAAIClient().connect(volumeGroupUri, vnfUri)
            getAAIClient().connect(volumeGroupUri, tenantUri)
            execution.setVariable("queriedVolumeGroupId", volumeGroupId)
            RollbackData rollbackData = execution.getVariable("rollbackData")
            rollbackData.put("DCVFMODULEVOL", "isAAIRollbackNeeded", "true")
        } catch (NotFoundException ignored) {
            execution.setVariable(prefix + "isErrorMessageException", true)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Unable to create volume group in AAI. Response code: 404")
        } catch (Exception ex) {
            execution.setVariable(prefix + "isErrorMessageException", true)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, ex.getMessage())
        }
    }

    /**
     * Prepare VNF adapter create request XML
     * @param execution
     */
    public void prepareVnfAdapterCreateRequest(DelegateExecution execution, isDebugEnabled) {

        GenericVnf aaiGenericVnfResponse = execution.getVariable(prefix+'AAIQueryGenericVfnResponse')
        def vnfId = aaiGenericVnfResponse.getVnfId()
        def vnfName = aaiGenericVnfResponse.getVnfName()
        def vnfType = aaiGenericVnfResponse.getVnfType()

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
        logger.debug("volumeGroupId: " + volumeGroupId)
        def testGroupId = execution.getVariable('test-volume-group-id')
        if (testGroupId != null && testGroupId.trim() != '') {
            logger.debug("test volumeGroupId is present: " + testGroupId)
            volumeGroupId = testGroupId
            execution.setVariable("test-volume-group-name", "MSOTESTVOL101a-vSAMP12_base_vol_module-0")
        }
        logger.debug("volumeGroupId to be used: " + volumeGroupId)

        // volume group parameters

        String volumeGroupParams = ''
        StringBuilder sbParams = new StringBuilder()
        Map<String, String> paramsMap = execution.getVariable("vfModuleInputParams")
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            String paramsXml
            String paramName = entry.getKey()
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
        logger.debug("volumeGroupParams: "+ volumeGroupParams)

        def backoutOnFailure = execution.getVariable(prefix+"backoutOnFailure")
        logger.debug("backoutOnFailure: "+ backoutOnFailure)

        def failIfExists = execution.getVariable("failIfExists")
        if(failIfExists == null) {
            failIfExists = 'true'
        }

        String messageId = UUID.randomUUID()
        logger.debug("messageId to be used is generated: " + messageId)

        def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
        def useQualifiedHostName = UrnPropertiesReader.getVariable("mso.use.qualified.host",execution)
        if ('true'.equals(useQualifiedHostName)) {
            notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
        }
        logger.debug("CreateVfModuleVolume - notificationUrl: "+ notificationUrl)

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
        logger.debug(vnfSubCreateWorkflowRequestAsString)
        logger.debug(vnfSubCreateWorkflowRequestAsString)
        execution.setVariable(prefix+"createVnfARequest", vnfSubCreateWorkflowRequestAsString)

        // build rollback request for use later if needed
        String vnfSubRollbackWorkflowRequest = buildRollbackVolumeGroupRequestXml(volumeGroupId, cloudSiteId, tenantId, requestId, serviceId, messageId, notificationUrl)

        logger.debug("Sub Vnf flow rollback request: vnfSubRollbackWorkflowRequest " + "\n" + vnfSubRollbackWorkflowRequest)

        String vnfSubRollbackWorkflowRequestAsString = utils.formatXml(vnfSubRollbackWorkflowRequest)
        execution.setVariable(prefix+"rollbackVnfARequest", vnfSubRollbackWorkflowRequestAsString)
    }

    public String buildRollbackVolumeGroupRequestXml(volumeGroupId, cloudSiteId, tenantId, requestId, serviceId, messageId, notificationUrl) {

        String request = """
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
        logger.debug("vnfAdapterSuccessIndicator: "+ vnfSuccess)
        if(vnfSuccess==true) {
            String createVnfAResponse = execution.getVariable(prefix+"createVnfAResponse")
            String heatStackID = utils.getNodeText(createVnfAResponse, "volumeGroupStackId")
            String vnfRollbackRequest = execution.getVariable(prefix+"rollbackVnfARequest")
            String updatedVnfRollbackRequest = updateRollbackVolumeGroupRequestXml(vnfRollbackRequest, heatStackID)
            logger.debug("vnfAdapter rollback request: "+ updatedVnfRollbackRequest)
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
        String volumeGroupId = execution.getVariable("queriedVolumeGroupId")
        String modelCustomizationId = execution.getVariable("modelCustomizationId")
        String cloudRegion = execution.getVariable("lcpCloudRegionId")
        String cloudOwner = execution.getVariable('cloudOwner')
        String createVnfAResponse = execution.getVariable(prefix+"createVnfAResponse")
        def heatStackID = utils.getNodeText(createVnfAResponse, "volumeGroupStackId")
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(cloudOwner, cloudRegion).volumeGroup(volumeGroupId))

        execution.setVariable(prefix+"heatStackId", heatStackID)

        VolumeGroup volumeGroup = new VolumeGroup()
        volumeGroup.setHeatStackId(heatStackID)
        volumeGroup.setModelCustomizationId(modelCustomizationId)
        try {
            getAAIClient().update(uri, volumeGroup)
            execution.setVariable(prefix+"isPONR", true)
        }catch(NotFoundException ignored){
            execution.setVariable(prefix+"isErrorMessageException", true)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Unable to update volume group in AAI. Response code: 404")
        }catch(BpmnError error){
            throw error
        }catch(Exception e){
            execution.setVariable(prefix+"isErrorMessageException", true)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "AAI Adapter Query Failed. "+ e.getMessage())
        }
    }


    /**
     * Query AAI Generic VNF
     * @param execution
     * @param isDebugEnabled
     */
    public void callRESTQueryAAIGenericVnf(DelegateExecution execution, isDebugEnabled) {

        def vnfId = execution.getVariable('vnfId')
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
        try {
            Optional<GenericVnf> genericVnf = getAAIClient().get(GenericVnf.class, uri)
            if (genericVnf.isPresent()) {
                execution.setVariable(prefix + 'AAIQueryGenericVfnResponse', genericVnf.get())
            } else {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, 'Generic vnf ' + vnfId + ' was not found in AAI. Return code: 404.')
            }
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Exception in get generic VNF: " + e.getMessage())
        }
    }

}
