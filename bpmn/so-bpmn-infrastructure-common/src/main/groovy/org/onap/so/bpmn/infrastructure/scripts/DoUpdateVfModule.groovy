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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.NetworkUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.common.scripts.VfModuleBase
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.constants.Defaults
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class DoUpdateVfModule extends VfModuleBase {
    private static final Logger logger = LoggerFactory.getLogger( DoUpdateVfModule.class);

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable('prefix', 'DOUPVfMod_')
		execution.setVariable('DOUPVfMod_requestInfo', null)
		execution.setVariable('DOUPVfMod_serviceInstanceId', null)
		execution.setVariable('DOUPVfMod_requestId', null)
		execution.setVariable('DOUPVfMod_vnfInputs', null)
		execution.setVariable('DOUPVfMod_vnfId', null)
		execution.setVariable('DOUPVfMod_vnfName', null)
		execution.setVariable('DOUPVfMod_vnfNameFromAAI', null)
		execution.setVariable('DOUPVfMod_vfModuleName', null)
		execution.setVariable('DOUPVfMod_vfModuleId', null)
		execution.setVariable('DOUPVfMod_vnfType', null)
		execution.setVariable('DOUPVfMod_asdcServiceModelVersion', null)
		execution.setVariable('DOUPVfMod_vfModuleModelName', null)
		execution.setVariable('DOUPVfMod_modelCustomizationUuid', null)
		execution.setVariable("DOUPVfMod_isBaseVfModule", "false")
		execution.setVariable('DOUPVfMod_serviceId', null)
		execution.setVariable('DOUPVfMod_aicCloudRegion', null)
		execution.setVariable('DOUPVfMod_tenantId', null)
		execution.setVariable('DOUPVfMod_volumeGroupId', null)
		execution.setVariable("DOUPVfMod_volumeGroupStackId", "")
		execution.setVariable('DOUPVfMod_vfModule', null)
		execution.setVariable('DOUPVfMod_vnfParams', null)
		execution.setVariable("DOUPVfMod_baseVfModuleId", "")
		execution.setVariable("DOUPVfMod_baseVfModuleHeatStackId", "")
		execution.setVariable('DOUPVfMod_prepareUpdateAAIVfModuleRequest', null)
		execution.setVariable('DOUPVfMod_sdncChangeAssignRequest', null)
		execution.setVariable('DOUPVfMod_sdncChangeAssignResponse', null)
		execution.setVariable('DOUPVfMod_sdncActivateRequest', null)
		execution.setVariable('DOUPVfMod_sdncActivateResponse', null)
		execution.setVariable('DOUPVfMod_sdncTopologyRequest', null)
		execution.setVariable('DOUPVfMod_sdncTopologyResponse', null)
		execution.setVariable('DOUPVfMod_vnfAdapterRestRequest', null)
		execution.setVariable('DOUPVfMod_updateAAIGenericVnfRequest', null)
		execution.setVariable('DOUPVfMod_updateAAIVfModuleRequest', null)
		execution.setVariable('DOUPVfMod_skipUpdateGenericVnf', false)
		execution.setVariable('DoUpdateVfModuleSuccessIndicator', false)
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
			initProcessVariables(execution)
			def xml = getVariable(execution, 'DoUpdateVfModuleRequest')
			logger.debug("DoUpdateVfModule request: " + xml)
			logger.debug('Received request xml:\n' + xml)

			if (xml == null || xml.isEmpty()) {
				// Building Block-type request

				String cloudConfiguration = execution.getVariable("cloudConfiguration")
				String vfModuleModelInfo = execution.getVariable("vfModuleModelInfo")

				def serviceModelInfo = execution.getVariable("serviceModelInfo")
				logger.debug("serviceModelInfo: " + serviceModelInfo)
				String modelInvariantUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelInvariantUuid")
				logger.debug("modelInvariantUuid: " + modelInvariantUuid)
				def vnfModelInfo = execution.getVariable("vnfModelInfo")

				//tenantId
				def tenantId = execution.getVariable("tenantId")
				execution.setVariable("DOUPVfMod_tenantId", tenantId)

				//volumeGroupId
				def volumeGroupId = execution.getVariable("volumeGroupId")
				execution.setVariable("DOUPVfMod_volumeGroupId", volumeGroupId)

				//cloudSiteId
				def cloudSiteId = execution.getVariable("lcpCloudRegionId")
				execution.setVariable("DOUPVfMod_aicCloudRegion", cloudSiteId)

				logger.debug("cloudSiteId: " + cloudSiteId)

				//cloudOwner
				def cloudOwner = execution.getVariable("cloudOwner")
				execution.setVariable("DOUPVfMod_cloudOwner", cloudOwner)
				logger.debug("cloudOwner: " + cloudOwner)

				//vnfType
				def vnfType = execution.getVariable("vnfType")
				execution.setVariable("DOUPVfMod_vnfType", vnfType)

				logger.debug("vnfType: " + vnfType)
				//vnfName
				def vnfName = execution.getVariable("vnfName")
				execution.setVariable("DOUPVfMod_vnfName", vnfName)

				logger.debug("vnfName: " + vnfName)
				//vnfId
				def vnfId = execution.getVariable("vnfId")
				execution.setVariable("DOUPVfMod_vnfId", vnfId)

				logger.debug("vnfId: " + vnfId)
				//vfModuleName
				def vfModuleName = execution.getVariable("vfModuleName")
				execution.setVariable("DOUPVfMod_vfModuleName", vfModuleName)

				logger.debug("vfModuleName: " + vfModuleName)
				//vfModuleModelName
				def vfModuleModelName = jsonUtil.getJsonValue(vfModuleModelInfo, "modelName")
				execution.setVariable("DOUPVfMod_vfModuleModelName", vfModuleModelName)

				logger.debug("vfModuleModelName: " + vfModuleModelName)
				//modelCustomizationUuid
				def modelCustomizationUuid = jsonUtil.getJsonValue(vfModuleModelInfo, "modelCustomizationUuid")
				if (modelCustomizationUuid == null) {
					modelCustomizationUuid = ""
				}
				execution.setVariable("DOUPVfMod_modelCustomizationUuid", modelCustomizationUuid)

				logger.debug("modelCustomizationUuid: " + modelCustomizationUuid)
				//vfModuleId
				def vfModuleId = execution.getVariable("vfModuleId")
				execution.setVariable("DOUPVfMod_vfModuleId", vfModuleId)
				logger.debug("vfModuleId: " + vfModuleId)
				def requestId = execution.getVariable("msoRequestId")
				execution.setVariable("DOUPVfMod_requestId", requestId)
				logger.debug("requestId: " + requestId)
				// Set mso-request-id to request-id for VNF Adapter interface
				execution.setVariable("mso-request-id", requestId)
				//serviceId
				def serviceId = execution.getVariable("serviceId")
				execution.setVariable("DOUPVfMod_serviceId", serviceId)
				logger.debug("serviceId: " + serviceId)
				//serviceInstanceId
				def serviceInstanceId = execution.getVariable("serviceInstanceId")
				execution.setVariable("DOUPVfMod_serviceInstanceId", serviceInstanceId)

				logger.debug("serviceInstanceId: " + serviceInstanceId)
				//source - HARDCODED
				def source = "VID"
				execution.setVariable("DOUPVfMod_source", source)

				logger.debug("source: " + source)
				//backoutOnFailure
				def disableRollback = execution.getVariable("disableRollback")
				def backoutOnFailure = true
				if (disableRollback != null && disableRollback.equals("true")) {
					backoutOnFailure = false
				}
				execution.setVariable("DOUPVfMod_backoutOnFailure", backoutOnFailure)
				logger.debug("backoutOnFailure: " + backoutOnFailure)
				//isBaseVfModule
				def isBaseVfModule = execution.getVariable("isBaseVfModule")
				execution.setVariable("DOUPVfMod_isBaseVfModule", isBaseVfModule)
				logger.debug("isBaseVfModule: " + isBaseVfModule)
				//asdcServiceModelVersion
				def asdcServiceModelVersion = execution.getVariable("asdcServiceModelVersion")
				execution.setVariable("DOUPVfMod_asdcServiceModelVersion", asdcServiceModelVersion)
				logger.debug("asdcServiceModelVersion: " + asdcServiceModelVersion)
				//personaModelId
				execution.setVariable("DOUPVfMod_personaModelId", jsonUtil.getJsonValue(vfModuleModelInfo, "modelInvariantUuid"))
				//personaModelVersion
				execution.setVariable("DOUPVfMod_personaModelVersion", jsonUtil.getJsonValue(vfModuleModelInfo, "modelVersion"))
				//Get or Generate UUID
				String uuid = execution.getVariable("DOUPVfMod_uuid")
				if(uuid == null){
					uuid = UUID.randomUUID()
					logger.debug("Generated messageId (UUID) is: " + uuid)
				}else{
					logger.debug("Found messageId (UUID) is: " + uuid)
				}
				//isVidRequest
				String isVidRequest = execution.getVariable("isVidRequest")
				// default to true
				if (isVidRequest == null || isVidRequest.isEmpty()) {
					execution.setVariable("isVidRequest", "true")
				}
				//usePreload
				def usePreload = execution.getVariable("usePreload")
				execution.setVariable("DOUPVfMod_usePreload", usePreload)
				logger.debug("usePreload: " + usePreload)
				//globalSubscriberId
				String globalSubscriberId = execution.getVariable("globalSubscriberId")
				execution.setVariable("DOUPVfMod_globalSubscriberId", globalSubscriberId)
				logger.debug("globalSubsrciberId: " + globalSubscriberId)
				//vnfQueryPath
				String vnfQueryPath = execution.getVariable("vnfQueryPath")
				execution.setVariable("DOUPVfMod_vnfQueryPath", vnfQueryPath)
				logger.debug("vnfQueryPath: " + vnfQueryPath)

				Map<String,String> vfModuleInputParams = execution.getVariable("vfModuleInputParams")
				if (vfModuleInputParams != null) {
					execution.setVariable("DOUPVfMod_vnfParamsMap", vfModuleInputParams)
				}
				//get workload and environment context from parent SI
				String environmentContext = ""
				String workloadContext =""
				String serviceType =""

				try{
					String json = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuidString(execution,modelInvariantUuid )
					serviceType = jsonUtil.getJsonValue(json, "serviceResources.serviceType")
				}catch(BpmnError e){
					throw e
				} catch (Exception ex){
					String msg = "Exception in preProcessRequest " + ex.getMessage()
					logger.debug(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
				}

				try{
					AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId))
					AAIResourcesClient aaiRC = new AAIResourcesClient()
					AAIResultWrapper aaiRW = aaiRC.get(serviceInstanceURI)
					Map<String, Object> aaiJson = aaiRW.asMap()
					environmentContext = aaiJson.getOrDefault("environment-context","")
					workloadContext = aaiJson.getOrDefault("workload-context","")

				}catch (Exception ex) {
					logger.debug("Error retreiving parent service instance information")
				}

				execution.setVariable("DCVFM_environmentContext",environmentContext)
				execution.setVariable("DCVFM_workloadContext",workloadContext)
			}
			else {

				def requestInfo = getRequiredNodeXml(execution, xml, 'request-info')
				execution.setVariable('DOUPVfMod_requestInfo', requestInfo)
				execution.setVariable('DOUPVfMod_requestId', getRequiredNodeText(execution, requestInfo, 'request-id'))
				def serviceInstanceId = execution.getVariable('mso-service-instance-id')
				if (serviceInstanceId == null) {
					serviceInstanceId = ''
				}
				execution.setVariable('DOUPVfMod_serviceInstanceId', serviceInstanceId)

				def vnfInputs = getRequiredNodeXml(execution, xml, 'vnf-inputs')
				execution.setVariable('DOUPVfMod_vnfInputs', vnfInputs)
				execution.setVariable('DOUPVfMod_vnfId', getRequiredNodeText(execution, vnfInputs, 'vnf-id'))
				execution.setVariable('DOUPVfMod_vfModuleId', getRequiredNodeText(execution, vnfInputs, 'vf-module-id'))
				execution.setVariable('DOUPVfMod_vfModuleName', getNodeTextForce(vnfInputs, 'vf-module-name'))
				execution.setVariable('DOUPVfMod_vnfType', getNodeTextForce(vnfInputs, 'vnf-type'))
				execution.setVariable('DOUPVfMod_vnfName', getNodeTextForce(vnfInputs, 'vnf-name'))
				execution.setVariable('DOUPVfMod_asdcServiceModelVersion', getNodeTextForce(vnfInputs, 'asdc-service-model-version'))
				execution.setVariable('DOUPVfMod_vfModuleModelName', getRequiredNodeText(execution, vnfInputs, 'vf-module-model-name'))
				execution.setVariable('DOUPVfMod_modelCustomizationUuid', getNodeTextForce(vnfInputs, 'model-customization-id'))
				execution.setVariable('DOUPVfMod_serviceId', getRequiredNodeText(execution, vnfInputs, 'service-id'))
				execution.setVariable('DOUPVfMod_aicCloudRegion', getRequiredNodeText(execution, vnfInputs, 'aic-cloud-region'))
				execution.setVariable('DOUPVfMod_cloudOwner', getRequiredNodeText(execution, vnfInputs, 'cloud-owner'))
				execution.setVariable('DOUPVfMod_tenantId', getRequiredNodeText(execution, vnfInputs, 'tenant-id'))
				//isBaseVfModule
				def isBaseVfModule = "false"
				if (utils.nodeExists(xml, "is-base-vf-module")) {
					isBaseVfModule = utils.getNodeText(xml, "is-base-vf-module")
					execution.setVariable("DOUPVfMod_isBaseVfModule", isBaseVfModule)
				}
				logger.debug("isBaseVfModule: " + isBaseVfModule)

				NetworkUtils networkUtils = new NetworkUtils()
				def backoutOnFailure = networkUtils.isRollbackEnabled(execution, xml)
				execution.setVariable("DOUPVfMod_backoutOnFailure", backoutOnFailure)

				def String vgi = getNodeTextForce(vnfInputs, 'volume-group-id')
				execution.setVariable('DOUPVfMod_volumeGroupId', vgi)

				execution.setVariable('DOUPVfMod_vnfParams', utils.getNodeXml(xml, 'vnf-params', false))
			}

			def sdncCallbackUrl = (String) UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
				def msg = 'Required variable \'mso.workflow.sdncadapter.callback\' is missing'
				logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
						ErrorCode.UnknownError.getValue());
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the PrepareUpdateAAIVfModule subflow.  This will
	 * set the orchestration-status to 'pending-update'.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepPrepareUpdateAAIVfModule(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preparePrepareUpdateAAIVfModule(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def orchestrationStatus = 'pending-update'

			String prepareUpdateAAIVfModuleRequest = """
				<PrepareUpdateAAIVfModuleRequest>
					<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
					<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
					<orchestration-status>${MsoUtils.xmlEscape(orchestrationStatus)}</orchestration-status>
				</PrepareUpdateAAIVfModuleRequest>
			"""
			prepareUpdateAAIVfModuleRequest = utils.formatXml(prepareUpdateAAIVfModuleRequest)
			execution.setVariable('DOUPVfMod_prepareUpdateAAIVfModuleRequest', prepareUpdateAAIVfModuleRequest)
			logger.debug("DoUpdateAAIVfModule request: " + prepareUpdateAAIVfModuleRequest)
			logger.debug('Request for PrepareUpdateAAIVfModule:\n' + prepareUpdateAAIVfModuleRequest)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preparePrepareUpdateAAIVfModule(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the ConfirmVolumeGroupTenant subflow.
	 * Determine cloud region id for the volume group.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepConfirmVolumeGroupTenant(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepConfirmVolumeGroupTenant(' +
			'execution=' + execution.getId() +
			')'

		def prefix = execution.getVariable("prefix")

		logger.trace('Entered ' + method)

		try {
			String cloudRegion = execution.getVariable(prefix + "aicCloudRegion")

			// Prepare AA&I url
			AaiUtil aaiUtil = new AaiUtil(this)

			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), cloudRegion))
			def queryCloudRegionRequest = aaiUtil.createAaiUri(uri)

			execution.setVariable(prefix + "queryCloudRegionRequest", queryCloudRegionRequest)

			cloudRegion = aaiUtil.getAAICloudReqion(execution, queryCloudRegionRequest, "AAI", cloudRegion)

			if ((cloudRegion != "ERROR")) {
				if(execution.getVariable(prefix + "queryCloudRegionReturnCode") == "404"){
					execution.setVariable(prefix + "cloudRegionForVolume", "AAIAIC25")
				}else{
				execution.setVariable(prefix + "cloudRegionForVolume", cloudRegion)
				}
				execution.setVariable(prefix + "isCloudRegionGood", true)
			} else {
				String errorMessage = "AAI Query Cloud Region Unsuccessful. AAI Response Code: " + execution.getVariable(prefix + "queryCloudRegionReturnCode")
				logger.debug(errorMessage)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
				execution.setVariable(prefix + "isCloudRegionGood", false)
			}
			logger.debug(" is Cloud Region Good: " + execution.getVariable(prefix + "isCloudRegionGood"))

		} catch(BpmnError b){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Rethrowing MSOWorkflowException", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + b);
			throw b
		}catch (Exception e) {
			// try error
			String errorMessage = "Bpmn error encountered in CreateVfModule flow. Unexpected Response from AAI - " + e.getMessage()
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"AAI Query Cloud Region Failed. Exception - " + "\n" + errorMessage, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception occured during prepConfirmVolumeGroupTenant(): " + e.getMessage())
		}
		logger.trace('Exited ' + method)

	}

	/**
	 * Prepare a Request for invoking the SDNC Adapter subflow to perform
	 * a VNF topology 'changeassign' operation.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepSDNCTopologyChg(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepSDNCTopologyChg(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

		try {
			def requestId = execution.getVariable('DOUPVfMod_requestId')
			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("DOUPVfMod_requestId") + "-" +  	System.currentTimeMillis()
			}
			def serviceInstanceId = execution.getVariable('DOUPVfMod_serviceInstanceId')
			def callbackUrl = (String) UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			def serviceId = execution.getVariable('DOUPVfMod_serviceId')
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vnfType = execution.getVariable('DOUPVfMod_vnfType')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def vfModuleModelName = execution.getVariable('DOUPVfMod_vfModuleModelName')
			def vfModuleName = ""
			if (execution.getVariable('DOUPVfMod_vfModule') != null) {
				org.onap.aai.domain.yang.VfModule vfModule = execution.getVariable('DOUPVfMod_vfModule')
				vfModuleName = vfModule.getVfModuleName()			
			}			
			def tenantId = execution.getVariable('DOUPVfMod_tenantId')
			def aicCloudRegion = execution.getVariable('DOUPVfMod_aicCloudRegion')
			boolean usePreload = execution.getVariable("DOUPVfMod_usePreload")
			String usePreloadToSDNC = usePreload ? "Y" : "N"
			def modelCustomizationUuid = execution.getVariable("DoUPVfMod_modelCustomizationUuid")
			def modelCustomizationUuidString = ""
			if (!usePreload) {
				modelCustomizationUuidString = "<modelCustomizationUuid>" + modelCustomizationUuid + "</modelCustomizationUuid>"
			}

			// Retrieve vnf name from AAI response
			def vnfName = execution.getVariable('DOUPVfMod_vnfNameFromAAI')
			execution.setVariable('DOUPVfMod_vnfName', vnfName)

			def vnfParamsXml = execution.getVariable('DOUPVfMod_vnfParams')
			def vnfNetworks = transformNetworkParamsToVnfNetworks(vnfParamsXml)

			String sdncTopologyRequest = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest
						xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
						xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>changeassign</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
					      <request-information>
					         <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					         <request-action>ChangeVNFActivateRequest</request-action>
					         <source>PORTAL</source>
					         <notification-url/>
					         <order-number/>
					         <order-version/>
					      </request-information>
					      <service-information>
					         <service-type>${MsoUtils.xmlEscape(serviceId)}</service-type>
					         <service-instance-id>${MsoUtils.xmlEscape(vnfId)}</service-instance-id>
					         <subscriber-name>dontcare</subscriber-name>
					      </service-information>
					      <vnf-request-information>
					         <vnf-id>${MsoUtils.xmlEscape(vfModuleId)}</vnf-id>
					         <vnf-type>${MsoUtils.xmlEscape(vfModuleModelName)}</vnf-type>
					         <vnf-name>${MsoUtils.xmlEscape(vfModuleName)}</vnf-name>
					         <generic-vnf-id>${MsoUtils.xmlEscape(vnfId)}</generic-vnf-id>
					         <generic-vnf-name>${MsoUtils.xmlEscape(vnfName)}</generic-vnf-name>
							 <generic-vnf-type>${MsoUtils.xmlEscape(vnfType)}</generic-vnf-type>
					         <tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
					         <aic-cloud-region>${MsoUtils.xmlEscape(aicCloudRegion)}</aic-cloud-region>
							 ${modelCustomizationUuidString}
							 <use-preload>${MsoUtils.xmlEscape(usePreloadToSDNC)}</use-preload>
					         ${vnfNetworks}
					      </vnf-request-information>
 					</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
			sdncTopologyRequest = utils.formatXml(sdncTopologyRequest)
			execution.setVariable('DOUPVfMod_sdncChangeAssignRequest', sdncTopologyRequest)
			logger.debug("sdncChangeAssignRequest : " + sdncTopologyRequest)
			logger.debug('Request for SDNCAdapter topology/changeassign:\n' + sdncTopologyRequest)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepSDNCTopologyChg(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the SDNC Adapter subflow to perform
	 * a VNF topology 'query' operation.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepSDNCTopologyQuery(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepSDNCTopologyQuery(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

		try {
			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("DOUPVfMod_requestId") + "-" +  	System.currentTimeMillis()
			}
			def requestId = execution.getVariable('DOUPVfMod_requestId')
			def serviceInstanceId = execution.getVariable('DOUPVfMod_serviceInstanceId')
			def callbackUrl = (String) UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')

			def svcInstId = ""
			if (serviceInstanceId == null || serviceInstanceId.isEmpty()) {
				svcInstId = vfModuleId
			}
			else {
				svcInstId = serviceInstanceId
			}

			//!!!! TEMPORARY WORKAROUND FOR SDNC REPLICATION ISSUE
			sleep(5000)

			String sdncTopologyRequest = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest
						xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
						xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>/VNF-API:vnfs/vnf-list/${vfModuleId}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
						<sdncadapter:MsoAction>mobility</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
			sdncTopologyRequest = utils.formatXml(sdncTopologyRequest)
			execution.setVariable('DOUPVfMod_sdncTopologyRequest', sdncTopologyRequest)
			logger.debug("sdncTopologyRequest : " + sdncTopologyRequest)
			logger.debug('Request for SDNCAdapter query:\n' + sdncTopologyRequest)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepSDNCTopologyQuery(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the VnfAdapterRest subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepVnfAdapterRest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepVnfAdapterRest(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

		try {
			def requestId = execution.getVariable('DOUPVfMod_requestId')
			def serviceInstanceId = execution.getVariable('DOUPVfMod_serviceInstanceId')
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def vfModuleName = execution.getVariable('DOUPVfMod_vfModuleName')
			def vnfInputs = execution.getVariable('DOUPVfMod_vnfInputs')
			def tenantId = execution.getVariable('DOUPVfMod_tenantId')
			def volumeGroupId = execution.getVariable('DOUPVfMod_volumeGroupId')
			def volumeGroupStackId = execution.getVariable('DOUPVfMod_volumeGroupStackId')
			def heatStackId = ""
			if (execution.getVariable('DOUPVfMod_vfModule') != null) {
				org.onap.aai.domain.yang.VfModule vfModule = execution.getVariable('DOUPVfMod_vfModule')
				heatStackId = vfModule.getHeatStackId()
			}			
			def cloudId = execution.getVariable('DOUPVfMod_aicCloudRegion')
			def cloudOwner = execution.getVariable('DOUPVfMod_cloudOwner')
			def vnfType = execution.getVariable('DOUPVfMod_vnfType')
			def vnfName = execution.getVariable('DOUPVfMod_vnfName')
			def vfModuleModelName = execution.getVariable('DOUPVfMod_vfModuleModelName')
			def baseVfModuleId = execution.getVariable("DOUPVfMod_baseVfModuleId")
			def baseVfModuleStackId = execution.getVariable("DOUPVfMod_baseVfModuleHeatStackId")
			def asdcServiceModelVersion = execution.getVariable('DOUPVfMod_asdcServiceModelVersion')
			def modelCustomizationUuid = execution.getVariable('DOUPVfMod_modelCustomizationUuid')
			def backoutOnFailure = execution.getVariable("DOUPVfMod_backoutOnFailure")

			def messageId = execution.getVariable('mso-request-id') + '-' + System.currentTimeMillis()
			def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
			def useQualifiedHostName = UrnPropertiesReader.getVariable("mso.use.qualified.host",execution)
			if ('true'.equals(useQualifiedHostName)) {
					notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
			}

			String environmentContext = execution.getVariable("DOUPVEnvironment_context")
			String workloadContext = execution.getVariable("DOUPVWorkload_context")
			logger.debug("workloadContext: " + workloadContext)
			logger.debug("environmentContext: " + environmentContext)

			Map<String, String> vnfParamsMap = execution.getVariable("DOUPVfMod_vnfParamsMap")

			String sdncGetResponse = execution.getVariable('DOUPVfMod_sdncTopologyResponse')

			String vfModuleParams = buildVfModuleParams(vnfParamsMap, sdncGetResponse, vnfId, vnfName,
					vfModuleId, vfModuleName, null, environmentContext, workloadContext)


			String vnfAdapterRestRequest = """
				<updateVfModuleRequest>
					<cloudSiteId>${MsoUtils.xmlEscape(cloudId)}</cloudSiteId>
					<cloudOwner>${MsoUtils.xmlEscape(cloudOwner)}</cloudOwner>
					<tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
					<vnfId>${MsoUtils.xmlEscape(vnfId)}</vnfId>
					<vfModuleId>${MsoUtils.xmlEscape(vfModuleId)}</vfModuleId>
					<vfModuleStackId>${MsoUtils.xmlEscape(heatStackId)}</vfModuleStackId>
					<vnfType>${MsoUtils.xmlEscape(vnfType)}</vnfType>
					<vnfVersion>${MsoUtils.xmlEscape(asdcServiceModelVersion)}</vnfVersion>
					<modelCustomizationUuid>${MsoUtils.xmlEscape(modelCustomizationUuid)}</modelCustomizationUuid>
					<vfModuleType>${MsoUtils.xmlEscape(vfModuleModelName)}</vfModuleType>
					<volumeGroupId>${MsoUtils.xmlEscape(volumeGroupId)}</volumeGroupId>
					<volumeGroupStackId>${MsoUtils.xmlEscape(volumeGroupStackId)}</volumeGroupStackId>
					<baseVfModuleId>${MsoUtils.xmlEscape(baseVfModuleId)}</baseVfModuleId>
    				<baseVfModuleStackId>${MsoUtils.xmlEscape(baseVfModuleStackId)}</baseVfModuleStackId>
					<skipAAI>true</skipAAI>
					<backout>${MsoUtils.xmlEscape(backoutOnFailure)}</backout>
				    <failIfExists>false</failIfExists>
					<vfModuleParams>
						${vfModuleParams}
				    </vfModuleParams>
				    <msoRequest>
				        <requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
				        <serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
				    </msoRequest>
				    <messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
				    <notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
				</updateVfModuleRequest>
			"""
			vnfAdapterRestRequest = utils.formatXml(vnfAdapterRestRequest)
			execution.setVariable('DOUPVfMod_vnfAdapterRestRequest', vnfAdapterRestRequest)
			logger.debug("vnfAdapterRestRequest : " + vnfAdapterRestRequest)
			logger.debug('Request for VNFAdapter Rest:\n' + vnfAdapterRestRequest)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepVnfAdapterRest(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the UpdateAAIGenericVnf subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepUpdateAAIGenericVnf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepUpdateAAIGenericVnf(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vnfInputs = execution.getVariable('DOUPVfMod_vnfInputs')

			def personaModelId = utils.getNodeText(vnfInputs, 'vnf-persona-model-id')
			def personaModelVersion = utils.getNodeText(vnfInputs, 'vnf-persona-model-version')
			if ((personaModelId == null) || (personaModelVersion == null)) {
				logger.debug("Skipping update for Generic VNF ' + vnfId + ' because either \'vnf-persona-model-id\' or \'vnf-persona-model-version\' is absent")
				execution.setVariable('DOUPVfMod_skipUpdateGenericVnf', true)
			} else {
				def personaModelIdElement = '<model-invariant-id>' + personaModelId + '</model-invariant-id>'
				def personaModelVersionElement = '<model-version-id>' + personaModelVersion + '</model-version-id>'

				String updateAAIGenericVnfRequest = """
					<UpdateAAIGenericVnfRequest>
						<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
						${personaModelIdElement}
						${personaModelVersionElement}
					</UpdateAAIGenericVnfRequest>
				"""
				updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
				execution.setVariable('DOUPVfMod_updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
				logger.debug("updateAAIGenericVnfRequest : " + updateAAIGenericVnfRequest)
				logger.debug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest)
			}

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepUpdateAAIGenericVnf(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the UpdateAAIVfModule subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepUpdateAAIVfModule(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepUpdateAAIVfModule(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def orchestrationStatus = 'updated'
			def vnfInputs = execution.getVariable('DOUPVfMod_vnfInputs')

			def volumeGroupIdElement = ''
			def volumeGroupId = execution.getVariable('DOUPVfMod_volumeGroupId')
			if (volumeGroupId != null) {
				volumeGroupIdElement = '<volume-group-id>' + volumeGroupId + '</volume-group-id>'
			}
			def personaModelIdElement = ''
			def personaModelId = utils.getNodeText(vnfInputs, 'persona-model-id')
			if (personaModelId != null) {
				personaModelIdElement = '<model-invariant-id>' + personaModelId + '</model-invariant-id>'
			}
			def personaModelVersionElement = ''
			def personaModelVersion = utils.getNodeText(vnfInputs, 'persona-model-version')
			if (personaModelVersion != null) {
				personaModelVersionElement = '<model-version-id>' + personaModelVersion + '</model-version-id>'
			}
			def contrailServiceInstanceFqdnElement = ''
			def contrailServiceInstanceFqdn = utils.getNodeText(vnfInputs, 'contrail-service-instance-fqdn')
			if (contrailServiceInstanceFqdn != null) {
				contrailServiceInstanceFqdnElement = '<contrail-service-instance-fqdn>' + contrailServiceInstanceFqdn + '</contrail-service-instance-fqdn>'
			}
			def personaModelCustomizationIdElement = ''
			def modelCustomizationId = execution.getVariable('DOUPVfMod_modelCustomizationUuid')
			if (modelCustomizationId != null) {
				personaModelCustomizationIdElement = '<model-customization-id>' + modelCustomizationId + '</model-customization-id>'
			}

			String updateAAIVfModuleRequest = """
				<UpdateAAIVfModuleRequest>
					<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
					<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
					<orchestration-status>${MsoUtils.xmlEscape(orchestrationStatus)}</orchestration-status>
					${volumeGroupIdElement}
					${personaModelIdElement}
					${personaModelVersionElement}
					${contrailServiceInstanceFqdnElement}
					${personaModelCustomizationIdElement}
				</UpdateAAIVfModuleRequest>
			"""

			logger.debug('Unformatted updateAAIVfModuleRequest: ' + updateAAIVfModuleRequest)
			updateAAIVfModuleRequest = utils.formatXml(updateAAIVfModuleRequest)
			execution.setVariable('DOUPVfMod_updateAAIVfModuleRequest', updateAAIVfModuleRequest)
			logger.debug("updateAAIVfModuleRequest : " + updateAAIVfModuleRequest)
			logger.debug('Request for UpdateAAIVfModule:\n' + updateAAIVfModuleRequest)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepUpdateAAIVfModule(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the SDNC Adapter subflow to perform
	 * a VNF topology 'activate' operation.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepSDNCTopologyAct(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepSDNCTopologyAct(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

		try {
			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("DOUPVfMod_requestId") + "-" +  	System.currentTimeMillis()
			}
			def requestId = execution.getVariable('DOUPVfMod_requestId')
			def serviceInstanceId = execution.getVariable('DOUPVfMod_serviceInstanceId')
			def callbackUrl = (String) UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			def serviceId = execution.getVariable('DOUPVfMod_serviceId')
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vnfName = execution.getVariable('DOUPVfMod_vnfName')
			def vnfType = execution.getVariable('DOUPVfMod_vnfType')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def vfModuleModelName = execution.getVariable('DOUPVfMod_vfModuleModelName')
			def vfModuleName = ""
			if (execution.getVariable('DOUPVfMod_vfModule') != null) {
				org.onap.aai.domain.yang.VfModule vfModule = execution.getVariable('DOUPVfMod_vfModule')
				vfModuleName = vfModule.getVfModuleName()
			}			
			def tenantId = execution.getVariable('DOUPVfMod_tenantId')
			def aicCloudRegion = execution.getVariable('DOUPVfMod_aicCloudRegion')

			boolean usePreload = execution.getVariable("DOUPVfMod_usePreload")
			String usePreloadToSDNC = usePreload ? "Y" : "N"
			def modelCustomizationUuid = execution.getVariable("DoUPVfMod_modelCustomizationUuid")
			def modelCustomizationUuidString = ""
			if (!usePreload) {
				modelCustomizationUuidString = "<modelCustomizationUuid>" + modelCustomizationUuid + "</modelCustomizationUuid>"
			}

			def vnfParamsXml = execution.getVariable('DOUPVfMod_vnfParams')
			def vnfNetworks = transformNetworkParamsToVnfNetworks(vnfParamsXml)

			String sdncTopologyRequest = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest
						xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
						xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>activate</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
					      <request-information>
					         <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					         <request-action>ChangeVNFActivateRequest</request-action>
					         <source>PORTAL</source>
					         <notification-url/>
					         <order-number/>
					         <order-version/>
					      </request-information>
					      <service-information>
					         <service-type>${MsoUtils.xmlEscape(serviceId)}</service-type>
					         <service-instance-id>${MsoUtils.xmlEscape(vnfId)}</service-instance-id>
					         <subscriber-name>dontcare</subscriber-name>
					      </service-information>
					      <vnf-request-information>
					         <vnf-id>${MsoUtils.xmlEscape(vfModuleId)}</vnf-id>
					         <vnf-type>${MsoUtils.xmlEscape(vfModuleModelName)}</vnf-type>
					         <vnf-name>${MsoUtils.xmlEscape(vfModuleName)}</vnf-name>
					         <generic-vnf-id>${MsoUtils.xmlEscape(vnfId)}</generic-vnf-id>
					         <generic-vnf-name>${MsoUtils.xmlEscape(vnfName)}</generic-vnf-name>
							 <generic-vnf-type>${MsoUtils.xmlEscape(vnfType)}</generic-vnf-type>
					         <tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
					         <aic-cloud-region>${MsoUtils.xmlEscape(aicCloudRegion)}</aic-cloud-region>
							 ${modelCustomizationUuidString}
							<use-preload>${MsoUtils.xmlEscape(usePreloadToSDNC)}</use-preload>
					      </vnf-request-information>
 					</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
			sdncTopologyRequest = utils.formatXml(sdncTopologyRequest)
			execution.setVariable('DOUPVfMod_sdncActivateRequest', sdncTopologyRequest)
			logger.debug("sdncActivateRequest : " + sdncTopologyRequest)
			logger.debug('Request for SDNCAdapter topology/activate:\n' + sdncTopologyRequest)


			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepSDNCTopologyAct(): ' + e.getMessage())
		}
	}

	/**
	 * Log a WorkflowException that has been created.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void handleWorkflowException(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.handleWorkflowException(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

		try {
			def WorkflowException workflowException = (WorkflowException) execution.getVariable('WorkflowException')
			logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					method + ' caught WorkflowException: ' + workflowException.getErrorMessage(), "BPMN",
					ErrorCode.UnknownError.getValue());

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildWorkflowException(execution, 1002, 'Error in handleWorkflowException(): ' + e.getMessage())
		}
	}

	public void validateSDNCResponse(DelegateExecution execution, String response, String method){

		def prefix = execution.getVariable("prefix")

		logger.trace("STARTED ValidateSDNCResponse Process")

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		logger.debug("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		logger.debug("SDNCResponse: " + response)

		String sdncResponse = response
		if(execution.getVariable(prefix + 'sdncResponseSuccess') == true){
			logger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse)
		}else{
			logger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
			throw new BpmnError("MSOWorkflowException")
		}
		logger.trace("COMPLETED ValidateSDNCResponse Process")
	}

	/**
	 * Using the received vnfId and vfModuleId, query AAI to get the corresponding VNF info.
	 * A 200 response is expected with the VNF info in the response body. Will find out the base module info.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIVfModule(DelegateExecution execution) {

		def method = getClass().getSimpleName() + '.getVfModule(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)).depth(Depth.ONE)

			try {
				Optional<GenericVnf> genericVnf = getAAIClient().get(GenericVnf.class,uri)
				if (genericVnf.isPresent()) {
                    execution.setVariable('DOUPVfMod_queryAAIVfModuleResponseCode', 200)
                    execution.setVariable('DOUPVfMod_queryAAIVfModuleResponse', genericVnf.get())
                    // Parse the VNF record from A&AI to find base module info
					logger.debug('Parsing the VNF data to find base module info')
					if (genericVnf.get().getVfModules()!=null && !genericVnf.get().getVfModules().getVfModule().isEmpty()) {
                        Optional<org.onap.aai.domain.yang.VfModule> vfmodule =  genericVnf.get().getVfModules().getVfModule().stream().
                                filter{v-> v.isIsBaseVfModule()}.findFirst()
							if (vfmodule.isPresent()) {
							    String baseModuleId = vfmodule.get().getVfModuleId()
							    execution.setVariable("DOUPVfMod_baseVfModuleId", baseModuleId)
							    logger.debug('Received baseVfModuleId: ' + baseModuleId)
							    String baseModuleHeatStackId = vfmodule.get().getHeatStackId()
							    execution.setVariable("DOUPVfMod_baseVfModuleHeatStackId", baseModuleHeatStackId)
							    logger.debug('Received baseVfModuleHeatStackId: ' + baseModuleHeatStackId)
							}
					}
				}
			} catch (Exception ex) {
				logger.debug('Exception occurred while executing AAI GET: {}', ex.getMessage(), ex)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
			}
			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e, e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModule(): ' + e.getMessage())
		}
	}


}
