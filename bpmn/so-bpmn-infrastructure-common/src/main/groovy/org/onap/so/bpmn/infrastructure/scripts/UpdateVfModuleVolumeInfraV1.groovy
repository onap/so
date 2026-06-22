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

import static org.apache.cxf.common.util.CollectionUtils.isEmpty
import jakarta.ws.rs.core.UriBuilder
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.VfModule
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.VfModuleBase
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.constants.Defaults
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.json.JsonException
import groovy.json.JsonSlurper

class UpdateVfModuleVolumeInfraV1 extends VfModuleBase {
    private static final Logger logger = LoggerFactory.getLogger(UpdateVfModuleVolumeInfraV1.class)
    private ExceptionUtil exceptionUtil = new ExceptionUtil()

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
    public void preProcessRequest(DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
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

            logger.debug("modelInvariantId from request: {}", modelInvariantId)
            logger.debug("XML request:\n{}", request)
        }
        catch (JsonException je) {
            logger.debug(" Request is in XML format.")
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
        execution.setVariable('UPDVfModVol_cloudRegion', getRequiredNodeText(execution, volumeInputs, 'cloud-owner'))
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

        if (isVidRequest) {
            def volumeGroupId = execution.getVariable('volumeGroupId')
            syncResponse = """{"requestReferences":{"instanceId":"${volumeGroupId}","requestId":"${
                requestId
            }"}}""".trim()
        } else {
            syncResponse = utils.formatXml(xmlSyncResponse)
        }

        logger.debug('Sync response: {}', syncResponse)
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

        try {
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), aicCloudRegion).volumeGroup(volumeGroupId))
            AAIResultWrapper wrapper = getAAIClient().get(uri)
            Optional<VolumeGroup> volumeGroup = wrapper.asBean(VolumeGroup.class)
            if (volumeGroup.isPresent()) {
                execution.setVariable('UPDVfModVol_aaiVolumeGroupResponse', volumeGroup.get())
                Optional<Relationships> relationships = wrapper.getRelationships()
                if (relationships.isPresent()) {
                    List<AAIResourceUri> tenantURIList = relationships.get().getRelatedUris(Types.TENANT)
                    if (!isEmpty(tenantURIList)) {
                        String volumeGroupTenantId = tenantURIList.get(0).getURIKeys().get(AAIFluentTypeBuilder.Types.TENANT.getUriParams().tenantId)
                        execution.setVariable('UPDVfModVol_volumeGroupTenantId', volumeGroupTenantId)
                        logger.debug("Received Tenant Id {} from AAI for Volume Group with Volume Group Id {}, AIC Cloud Region ",
                                volumeGroupTenantId, volumeGroupId, aicCloudRegion)
                    } else {
                        exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Could not find Tenant Id element in Volume Group with Volume Group Id " + volumeGroupId
                                + ", AIC Cloud Region " + aicCloudRegion)
                    }
                    execution.setVariable('UPDVfModVol_relatedVfModuleLink', relationships.get().getRelatedLinks(Types.VF_MODULE).get(0))

                } else {
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Could not find Tenant Id element in Volume Group with Volume Group Id " + volumeGroupId
                            + ", AIC Cloud Region " + aicCloudRegion)
                }
            } else {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume Group " + volumeGroupId + " not found at AAI")
            }
        }catch(BpmnError bpmnError){
            throw  bpmnError
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During queryAAIForGenericVnf"+e.getMessage())
        }
    }

    /**
     * Query AAI service instance
     * @param execution
     * @param isDebugEnabled
     */
    public void queryAAIForGenericVnf(DelegateExecution execution, isDebugEnabled) {

        def vnfId = execution.getVariable('vnfId')

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
        try {
            Optional<GenericVnf> genericVnf = getAAIClient().get(GenericVnf.class, uri)
            if (genericVnf.isPresent()) {
                execution.setVariable('UPDVfModVol_AAIQueryGenericVfnResponse', genericVnf.get())
            } else {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, 'Generic vnf ' + vnfId + ' was not found in AAI. Return code: 404.')
            }
        }catch(BpmnError bpmnError){
            throw  bpmnError
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During queryAAIForGenericVnf"+e.getMessage())
        }
    }

    /**
     * Query AAI for VF Module using vf-module-id
     * @param execution
     * @param isDebugLogEnabled
     */
    public void queryAAIForVfModule(DelegateExecution execution, isDebugLogEnabled) {

        String queryAAIVfModuleRequest = execution.getVariable('UPDVfModVol_relatedVfModuleLink')
        execution.setVariable('UPDVfModVol_personaModelId', '')
        AAIResourceUri uri = AAIUriFactory.createResourceFromExistingURI(Types.VF_MODULE, UriBuilder.fromPath(queryAAIVfModuleRequest).build())
        try{
           Optional<VfModule> vfModule = getAAIClient().get(VfModule.class,uri)
            if(vfModule.isPresent()){
                execution.setVariable('UPDVfModVol_personaModelId',vfModule.get().getModelInvariantId())
            }else{
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "VF Module not found at AAI")
            }
        }catch(BpmnError bpmnError){
            throw bpmnError
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Error in queryAAIForVfModule: "+e.getMessage())
        }
    }

    /**
     * Prepare a Request for invoking the VnfAdapterRest subflow to do
     * a Volume Group update.
     *
     * @param execution The flow's execution instance.
     */
    public void prepVnfAdapterRest(DelegateExecution execution, isDebugLogEnabled) {

        def aicCloudRegion = execution.getVariable('UPDVfModVol_aicCloudRegion')
        def cloudOwner = execution.getVariable("UPDVfModVol_cloudRegion")
        def tenantId = execution.getVariable('UPDVfModVol_tenantId')
        def volumeGroupId = execution.getVariable('UPDVfModVol_volumeGroupId')

        VolumeGroup aaiVolumeGroupResponse = execution.getVariable('UPDVfModVol_aaiVolumeGroupResponse')
        def volumeGroupHeatStackId = aaiVolumeGroupResponse.getHeatStackId()
        def volumeGroupName = aaiVolumeGroupResponse.getVolumeGroupName()
        def modelCustomizationId = aaiVolumeGroupResponse.getModelCustomizationId()

        def vnfType = execution.getVariable('UPDVfModVol_vnfType')
        def vnfVersion = execution.getVariable('UPDVfModVol_vnfVersion')

        GenericVnf aaiGenericVnfResponse = execution.getVariable('UPDVfModVol_AAIQueryGenericVfnResponse')
        def vnfId = aaiGenericVnfResponse.getVnfId()
        def vnfName = aaiGenericVnfResponse.getVnfName()


        def volumeParamsXml = execution.getVariable('UPDVfModVol_volumeParams')
        def volumeGroupParams = transformVolumeParamsToEntries(volumeParamsXml)

        def requestId = execution.getVariable('UPDVfModVol_requestId')
        def serviceId = execution.getVariable('UPDVfModVol_serviceId')

        def messageId = execution.getVariable('mso-request-id') + '-' + System.currentTimeMillis()
        def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
        def useQualifiedHostName = UrnPropertiesReader.getVariable("mso.use.qualified.host", execution)
        if ('true'.equals(useQualifiedHostName)) {
            notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
        }

        String vnfAdapterRestRequest = """
			<updateVolumeGroupRequest>
				<cloudSiteId>${MsoUtils.xmlEscape(aicCloudRegion)}</cloudSiteId>
				<cloudOwner>${MsoUtils.xmlEscape(cloudOwner)}</cloudOwner>
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
        logger.debug('Request for VNFAdapter Rest:\n' + vnfAdapterRestRequest)
    }

    /**
     * Prepare a Request for updating the DB for this Infra request.
     *
     * @param execution The flow's execution instance.
     */
    public void prepDbInfraDbRequest(DelegateExecution execution, isDebugLogEnabled) {

        def requestId = execution.getVariable('UPDVfModVol_requestId')

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
        logger.debug('Request for Update Infra Request:\n' + updateInfraRequest)
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
        logger.debug('Request for Completion Handler:\n' + content)
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

        WorkflowException workflowException = execution.getVariable("WorkflowException")
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
        logger.debug('Request for Fallout Handler:\n' + content)
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

        String errorMessage = "TenantId " + tenantId + " in incoming request does not match Tenant Id " + volumeGroupTenantId +
                " retrieved from AAI for Volume Group Id " + volumeGroupId + ", AIC Cloud Region " + aicCloudRegion

        ExceptionUtil exceptionUtil = new ExceptionUtil()
        logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), 'Error in ' +
                'UpdateVfModuleVol: ' + errorMessage, "BPMN", ErrorCode.UnknownError.getValue(), "Exception")
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

        String errorMessage = "Model Invariant ID " + modelInvariantId + " in incoming request does not match persona model ID " + personaModelId +
                " retrieved from AAI for Volume Group Id "

        ExceptionUtil exceptionUtil = new ExceptionUtil()
        logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), 'Error in ' +
                'UpdateVfModuleVol: ' + errorMessage, "BPMN", ErrorCode.UnknownError.getValue(), "Exception")
        exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
    }

}
