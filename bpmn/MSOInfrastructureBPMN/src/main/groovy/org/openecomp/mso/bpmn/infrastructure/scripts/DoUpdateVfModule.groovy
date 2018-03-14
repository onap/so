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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.VfModule
import org.openecomp.mso.bpmn.common.scripts.VfModuleBase
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils;
import org.openecomp.mso.client.aai.AAIResourcesClient
import org.openecomp.mso.client.aai.entities.AAIResultWrapper
import org.openecomp.mso.client.aai.entities.uri.AAIUri
import org.openecomp.mso.rest.APIResponse
import org.springframework.web.util.UriUtils
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;


public class DoUpdateVfModule extends VfModuleBase {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	CatalogDbUtils catalog = new CatalogDbUtils()

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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			initProcessVariables(execution)
			def xml = getVariable(execution, 'DoUpdateVfModuleRequest')
			utils.logAudit("DoUpdateVfModule request: " + xml)
			logDebug('Received request xml:\n' + xml, isDebugLogEnabled)
			
			if (xml == null || xml.isEmpty()) {
				// Building Block-type request

				String cloudConfiguration = execution.getVariable("cloudConfiguration")
				String vfModuleModelInfo = execution.getVariable("vfModuleModelInfo")
				
				def serviceModelInfo = execution.getVariable("serviceModelInfo")
				logDebug("serviceModelInfo: " + serviceModelInfo, isDebugLogEnabled)
				String modelInvariantUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelInvariantUuid")
				logDebug("modelInvariantUuid: " + modelInvariantUuid, isDebugLogEnabled)
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
				
				logDebug("cloudSiteId: " + cloudSiteId, isDebugLogEnabled)
				//vnfType
				def vnfType = execution.getVariable("vnfType")
				execution.setVariable("DOUPVfMod_vnfType", vnfType)
				
				logDebug("vnfType: " + vnfType, isDebugLogEnabled)
				//vnfName
				def vnfName = execution.getVariable("vnfName")
				execution.setVariable("DOUPVfMod_vnfName", vnfName)
				
				logDebug("vnfName: " + vnfName, isDebugLogEnabled)
				//vnfId
				def vnfId = execution.getVariable("vnfId")
				execution.setVariable("DOUPVfMod_vnfId", vnfId)
				
				logDebug("vnfId: " + vnfId, isDebugLogEnabled)
				//vfModuleName
				def vfModuleName = execution.getVariable("vfModuleName")
				execution.setVariable("DOUPVfMod_vfModuleName", vfModuleName)
				
				logDebug("vfModuleName: " + vfModuleName, isDebugLogEnabled)
				//vfModuleModelName
				def vfModuleModelName = jsonUtil.getJsonValue(vfModuleModelInfo, "modelName")
				execution.setVariable("DOUPVfMod_vfModuleModelName", vfModuleModelName)
				
				logDebug("vfModuleModelName: " + vfModuleModelName, isDebugLogEnabled)
				//modelCustomizationUuid
				def modelCustomizationUuid = jsonUtil.getJsonValue(vfModuleModelInfo, "modelCustomizationUuid")
				if (modelCustomizationUuid == null) {
					modelCustomizationUuid = ""
				}
				execution.setVariable("DOUPVfMod_modelCustomizationUuid", modelCustomizationUuid)
				
				logDebug("modelCustomizationUuid: " + modelCustomizationUuid, isDebugLogEnabled)
				//vfModuleId
				def vfModuleId = execution.getVariable("vfModuleId")
				execution.setVariable("DOUPVfMod_vfModuleId", vfModuleId)
				logDebug("vfModuleId: " + vfModuleId, isDebugLogEnabled)
				def requestId = execution.getVariable("msoRequestId")
				execution.setVariable("DOUPVfMod_requestId", requestId)
				logDebug("requestId: " + requestId, isDebugLogEnabled)
				// Set mso-request-id to request-id for VNF Adapter interface
				execution.setVariable("mso-request-id", requestId)
				//serviceId
				def serviceId = execution.getVariable("serviceId")
				execution.setVariable("DOUPVfMod_serviceId", serviceId)
				logDebug("serviceId: " + serviceId, isDebugLogEnabled)
				//serviceInstanceId
				def serviceInstanceId = execution.getVariable("serviceInstanceId")
				execution.setVariable("DOUPVfMod_serviceInstanceId", serviceInstanceId)
				
				logDebug("serviceInstanceId: " + serviceInstanceId, isDebugLogEnabled)
				//source - HARDCODED
				def source = "VID"
				execution.setVariable("DOUPVfMod_source", source)
				
				logDebug("source: " + source, isDebugLogEnabled)
				//backoutOnFailure
				def disableRollback = execution.getVariable("disableRollback")
				def backoutOnFailure = true
				if (disableRollback != null && disableRollback.equals("true")) {
					backoutOnFailure = false
				}
				execution.setVariable("DOUPVfMod_backoutOnFailure", backoutOnFailure)
				logDebug("backoutOnFailure: " + backoutOnFailure, isDebugLogEnabled)
				//isBaseVfModule
				def isBaseVfModule = execution.getVariable("isBaseVfModule")
				execution.setVariable("DOUPVfMod_isBaseVfModule", isBaseVfModule)
				logDebug("isBaseVfModule: " + isBaseVfModule, isDebugLogEnabled)
				//asdcServiceModelVersion
				def asdcServiceModelVersion = execution.getVariable("asdcServiceModelVersion")
				execution.setVariable("DOUPVfMod_asdcServiceModelVersion", asdcServiceModelVersion)
				logDebug("asdcServiceModelVersion: " + asdcServiceModelVersion, isDebugLogEnabled)
				//personaModelId
				execution.setVariable("DOUPVfMod_personaModelId", jsonUtil.getJsonValue(vfModuleModelInfo, "modelInvariantUuid"))
				//personaModelVersion
				execution.setVariable("DOUPVfMod_personaModelVersion", jsonUtil.getJsonValue(vfModuleModelInfo, "modelVersion"))
				//Get or Generate UUID
				String uuid = execution.getVariable("DOUPVfMod_uuid")
				if(uuid == null){
					uuid = UUID.randomUUID()
					logDebug("Generated messageId (UUID) is: " + uuid, isDebugLogEnabled)
				}else{
					logDebug("Found messageId (UUID) is: " + uuid, isDebugLogEnabled)
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
				logDebug("usePreload: " + usePreload, isDebugLogEnabled)
				//globalSubscriberId
				String globalSubscriberId = execution.getVariable("globalSubscriberId")
				execution.setVariable("DOUPVfMod_globalSubscriberId", globalSubscriberId)
				logDebug("globalSubsrciberId: " + globalSubscriberId, isDebugLogEnabled)
				//vnfQueryPath
				String vnfQueryPath = execution.getVariable("vnfQueryPath")
				execution.setVariable("DOUPVfMod_vnfQueryPath", vnfQueryPath)
				logDebug("vnfQueryPath: " + vnfQueryPath, isDebugLogEnabled)
				
				Map<String,String> vfModuleInputParams = execution.getVariable("vfModuleInputParams")
				if (vfModuleInputParams != null) {
					execution.setVariable("DOUPVfMod_vnfParamsMap", vfModuleInputParams)					
				}	
				//get workload and environment context from parent SI
				String environmentContext = ""
				String workloadContext =""
				String serviceType =""
				
				try{
					String json = catalog.getServiceResourcesByServiceModelInvariantUuidString(execution,modelInvariantUuid )
					serviceType = jsonUtil.getJsonValue(json, "serviceResources.serviceType")
				}catch(BpmnError e){
					throw e
				} catch (Exception ex){
					String msg = "Exception in preProcessRequest " + ex.getMessage()
					utils.log("DEBUG", msg, isDebugLogEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
				}
				
				try{
					AAIUri serviceInstanceURI = AAIUriFactory.create(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId,serviceType,serviceInstanceId)
					AAIResourcesClient aaiRC = new AAIResourcesClient()
					AAIResultWrapper aaiRW = aaiRC.get(serviceInstanceURI)
					Map<String, Object> aaiJson = aaiRW.asMap()
					environmentContext = aaiJson.getOrDefault("environment-context","")
					workloadContext = aaiJson.getOrDefault("workload-context","")
					
				}catch (Exception ex) {
					utils.log("DEBUG","Error retreiving parent service instance information", isDebugLogEnabled)
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
				execution.setVariable('DOUPVfMod_tenantId', getRequiredNodeText(execution, vnfInputs, 'tenant-id'))
				//isBaseVfModule
				def isBaseVfModule = "false"
				if (utils.nodeExists(xml, "is-base-vf-module")) {
					isBaseVfModule = utils.getNodeText(xml, "is-base-vf-module")
					execution.setVariable("DOUPVfMod_isBaseVfModule", isBaseVfModule)
				}
				logDebug("isBaseVfModule: " + isBaseVfModule, isDebugLogEnabled)
	
				NetworkUtils networkUtils = new NetworkUtils()
				def backoutOnFailure = networkUtils.isRollbackEnabled(execution, xml)
				execution.setVariable("DOUPVfMod_backoutOnFailure", backoutOnFailure)
	
				def String vgi = getNodeTextForce(vnfInputs, 'volume-group-id')
				execution.setVariable('DOUPVfMod_volumeGroupId', vgi)
	
				execution.setVariable('DOUPVfMod_vnfParams', utils.getNodeXml(xml, 'vnf-params', false))
			}

			def sdncCallbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
				def msg = 'Required variable \'URN_mso_workflow_sdncadapter_callback\' is missing'
				logError(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def orchestrationStatus = 'pending-update'

			String prepareUpdateAAIVfModuleRequest = """
				<PrepareUpdateAAIVfModuleRequest>
					<vnf-id>${vnfId}</vnf-id>
					<vf-module-id>${vfModuleId}</vf-module-id>
					<orchestration-status>${orchestrationStatus}</orchestration-status>
				</PrepareUpdateAAIVfModuleRequest>
			"""
			prepareUpdateAAIVfModuleRequest = utils.formatXml(prepareUpdateAAIVfModuleRequest)
			execution.setVariable('DOUPVfMod_prepareUpdateAAIVfModuleRequest', prepareUpdateAAIVfModuleRequest)
			utils.logAudit("DoUpdateAAIVfModule request: " + prepareUpdateAAIVfModuleRequest)
			logDebug('Request for PrepareUpdateAAIVfModule:\n' + prepareUpdateAAIVfModuleRequest, isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			String cloudRegion = execution.getVariable(prefix + "aicCloudRegion")

			// Prepare AA&I url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUtil = new AaiUtil(this)
			String aai_uri = aaiUtil.getCloudInfrastructureCloudRegionUri(execution)
			String queryCloudRegionRequest = "${aai_endpoint}${aai_uri}/" + cloudRegion
			utils.logAudit("CloudRegion Request: " + queryCloudRegionRequest)

			execution.setVariable(prefix + "queryCloudRegionRequest", queryCloudRegionRequest)
			utils.log("DEBUG", prefix + "queryCloudRegionRequest - " + "\n" + queryCloudRegionRequest, isDebugLogEnabled)

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
				utils.log("DEBUG", errorMessage, isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
				execution.setVariable(prefix + "isCloudRegionGood", false)
			}
			utils.log("DEBUG", " is Cloud Region Good: " + execution.getVariable(prefix + "isCloudRegionGood"), isDebugLogEnabled)

		} catch(BpmnError b){
			utils.log("ERROR", "Rethrowing MSOWorkflowException", isDebugLogEnabled)
			throw b
		}catch (Exception e) {
			// try error
			String errorMessage = "Bpmn error encountered in CreateVfModule flow. Unexpected Response from AAI - " + e.getMessage()
			utils.log("ERROR", " AAI Query Cloud Region Failed.  Exception - " + "\n" + errorMessage, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception occured during prepConfirmVolumeGroupTenant(): " + e.getMessage())
		}
		logDebug('Exited ' + method, isDebugLogEnabled)
		
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestId = execution.getVariable('DOUPVfMod_requestId')
			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("DOUPVfMod_requestId") + "-" +  	System.currentTimeMillis()
			}
			def serviceInstanceId = execution.getVariable('DOUPVfMod_serviceInstanceId')
			def callbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			def serviceId = execution.getVariable('DOUPVfMod_serviceId')
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vnfType = execution.getVariable('DOUPVfMod_vnfType')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def vfModuleModelName = execution.getVariable('DOUPVfMod_vfModuleModelName')
			def VfModule vfModule = (VfModule) execution.getVariable('DOUPVfMod_vfModule')
			def vfModuleName = vfModule.getElementText('vf-module-name')
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
						xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>changeassign</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
					      <request-information>
					         <request-id>${requestId}</request-id>
					         <request-action>ChangeVNFActivateRequest</request-action>
					         <source>PORTAL</source>
					         <notification-url/>
					         <order-number/>
					         <order-version/>
					      </request-information>
					      <service-information>
					         <service-type>${serviceId}</service-type>
					         <service-instance-id>${vnfId}</service-instance-id>
					         <subscriber-name>dontcare</subscriber-name>
					      </service-information>
					      <vnf-request-information>
					         <vnf-id>${vfModuleId}</vnf-id>
					         <vnf-type>${vfModuleModelName}</vnf-type>
					         <vnf-name>${vfModuleName}</vnf-name>
					         <generic-vnf-id>${vnfId}</generic-vnf-id>
					         <generic-vnf-name>${vnfName}</generic-vnf-name>
							 <generic-vnf-type>${vnfType}</generic-vnf-type>
					         <tenant>${tenantId}</tenant>
					         <aic-cloud-region>${aicCloudRegion}</aic-cloud-region>
							 ${modelCustomizationUuidString}
							 <use-preload>${usePreloadToSDNC}</use-preload>
					         ${vnfNetworks}
					      </vnf-request-information>
 					</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
			sdncTopologyRequest = utils.formatXml(sdncTopologyRequest)
			execution.setVariable('DOUPVfMod_sdncChangeAssignRequest', sdncTopologyRequest)
			utils.logAudit("sdncChangeAssignRequest : " + sdncTopologyRequest)
			logDebug('Request for SDNCAdapter topology/changeassign:\n' + sdncTopologyRequest, isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("DOUPVfMod_requestId") + "-" +  	System.currentTimeMillis()
			}
			def requestId = execution.getVariable('DOUPVfMod_requestId')
			def serviceInstanceId = execution.getVariable('DOUPVfMod_serviceInstanceId')
			def callbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
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
						xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>/VNF-API:vnfs/vnf-list/${vfModuleId}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
						<sdncadapter:MsoAction>mobility</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
			sdncTopologyRequest = utils.formatXml(sdncTopologyRequest)
			execution.setVariable('DOUPVfMod_sdncTopologyRequest', sdncTopologyRequest)
			utils.logAudit("sdncTopologyRequest : " + sdncTopologyRequest)
			logDebug('Request for SDNCAdapter query:\n' + sdncTopologyRequest, isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

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
			def VfModule vfModule = (VfModule) execution.getVariable('DOUPVfMod_vfModule')
			def heatStackId = vfModule.getElementText('heat-stack-id')
			def cloudId = execution.getVariable('DOUPVfMod_aicCloudRegion')
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
			def useQualifiedHostName = execution.getVariable("URN_mso_use_qualified_host")
			if ('true'.equals(useQualifiedHostName)) {
					notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
			}
			
			String environmentContext = execution.getVariable("DOUPVEnvironment_context")
			String workloadContext = execution.getVariable("DOUPVWorkload_context")
			logDebug("workloadContext: " + workloadContext, isDebugLogEnabled)
			logDebug("environmentContext: " + environmentContext, isDebugLogEnabled)
			
			Map<String, String> vnfParamsMap = execution.getVariable("DOUPVfMod_vnfParamsMap")

			String sdncGetResponse = execution.getVariable('DOUPVfMod_sdncTopologyResponse')

			String vfModuleParams = buildVfModuleParams(vnfParamsMap, sdncGetResponse, vnfId, vnfName,
					vfModuleId, vfModuleName, null, environmentContext, workloadContext)


			String vnfAdapterRestRequest = """
				<updateVfModuleRequest>
					<cloudSiteId>${cloudId}</cloudSiteId>
					<tenantId>${tenantId}</tenantId>
					<vnfId>${vnfId}</vnfId>
					<vfModuleId>${vfModuleId}</vfModuleId>
					<vfModuleStackId>${heatStackId}</vfModuleStackId>
					<vnfType>${vnfType}</vnfType>
					<vnfVersion>${asdcServiceModelVersion}</vnfVersion>
					<modelCustomizationUuid>${modelCustomizationUuid}</modelCustomizationUuid>
					<vfModuleType>${vfModuleModelName}</vfModuleType>
					<volumeGroupId>${volumeGroupId}</volumeGroupId>
					<volumeGroupStackId>${volumeGroupStackId}</volumeGroupStackId>
					<baseVfModuleId>${baseVfModuleId}</baseVfModuleId>
    				<baseVfModuleStackId>${baseVfModuleStackId}</baseVfModuleStackId>
					<skipAAI>true</skipAAI>
					<backout>${backoutOnFailure}</backout>
				    <failIfExists>false</failIfExists>
					<vfModuleParams>
						${vfModuleParams}
				    </vfModuleParams>
				    <msoRequest>
				        <requestId>${requestId}</requestId>
				        <serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
				    </msoRequest>
				    <messageId>${messageId}</messageId>
				    <notificationUrl>${notificationUrl}</notificationUrl>
				</updateVfModuleRequest>
			"""
			vnfAdapterRestRequest = utils.formatXml(vnfAdapterRestRequest)
			execution.setVariable('DOUPVfMod_vnfAdapterRestRequest', vnfAdapterRestRequest)
			utils.logAudit("vnfAdapterRestRequest : " + vnfAdapterRestRequest)
			logDebug('Request for VNFAdapter Rest:\n' + vnfAdapterRestRequest, isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vnfInputs = execution.getVariable('DOUPVfMod_vnfInputs')

			def personaModelId = utils.getNodeText1(vnfInputs, 'vnf-persona-model-id')
			def personaModelVersion = utils.getNodeText1(vnfInputs, 'vnf-persona-model-version')
			if ((personaModelId == null) || (personaModelVersion == null)) {
				logDebug('Skipping update for Generic VNF ' + vnfId +
					' because either \'vnf-persona-model-id\' or \'vnf-persona-model-version\' is absent', isDebugLogEnabled)
				execution.setVariable('DOUPVfMod_skipUpdateGenericVnf', true)
			} else {
				def personaModelIdElement = '<model-invariant-id>' + personaModelId + '</model-invariant-id>'
				def personaModelVersionElement = '<model-version-id>' + personaModelVersion + '</model-version-id>'

				String updateAAIGenericVnfRequest = """
					<UpdateAAIGenericVnfRequest>
						<vnf-id>${vnfId}</vnf-id>
						${personaModelIdElement}
						${personaModelVersionElement}
					</UpdateAAIGenericVnfRequest>
				"""
				updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
				execution.setVariable('DOUPVfMod_updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
				utils.logAudit("updateAAIGenericVnfRequest : " + updateAAIGenericVnfRequest)
				logDebug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest, isDebugLogEnabled)
			}

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

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
			def personaModelId = utils.getNodeText1(vnfInputs, 'persona-model-id')
			if (personaModelId != null) {
				personaModelIdElement = '<model-invariant-id>' + personaModelId + '</model-invariant-id>'
			}
			def personaModelVersionElement = ''
			def personaModelVersion = utils.getNodeText1(vnfInputs, 'persona-model-version')
			if (personaModelVersion != null) {
				personaModelVersionElement = '<model-version-id>' + personaModelVersion + '</model-version-id>'
			}
			def contrailServiceInstanceFqdnElement = ''
			def contrailServiceInstanceFqdn = utils.getNodeText1(vnfInputs, 'contrail-service-instance-fqdn')
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
					<vnf-id>${vnfId}</vnf-id>
					<vf-module-id>${vfModuleId}</vf-module-id>
					<orchestration-status>${orchestrationStatus}</orchestration-status>
					${volumeGroupIdElement}
					${personaModelIdElement}
					${personaModelVersionElement}
					${contrailServiceInstanceFqdnElement}
					${personaModelCustomizationIdElement}
				</UpdateAAIVfModuleRequest>
			"""
			
			logDebug('Unformatted updateAAIVfModuleRequest: ' + updateAAIVfModuleRequest, isDebugLogEnabled)
			updateAAIVfModuleRequest = utils.formatXml(updateAAIVfModuleRequest)
			execution.setVariable('DOUPVfMod_updateAAIVfModuleRequest', updateAAIVfModuleRequest)
			utils.logAudit("updateAAIVfModuleRequest : " + updateAAIVfModuleRequest)
			logDebug('Request for UpdateAAIVfModule:\n' + updateAAIVfModuleRequest, isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("DOUPVfMod_requestId") + "-" +  	System.currentTimeMillis()
			}
			def requestId = execution.getVariable('DOUPVfMod_requestId')
			def serviceInstanceId = execution.getVariable('DOUPVfMod_serviceInstanceId')
			def callbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			def serviceId = execution.getVariable('DOUPVfMod_serviceId')
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vnfName = execution.getVariable('DOUPVfMod_vnfName')
			def vnfType = execution.getVariable('DOUPVfMod_vnfType')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def vfModuleModelName = execution.getVariable('DOUPVfMod_vfModuleModelName')
			def VfModule vfModule = (VfModule) execution.getVariable('DOUPVfMod_vfModule')
			def vfModuleName = vfModule.getElementText('vf-module-name')
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
						xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>
						<sdncadapter:SvcAction>activate</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
					      <request-information>
					         <request-id>${requestId}</request-id>
					         <request-action>ChangeVNFActivateRequest</request-action>
					         <source>PORTAL</source>
					         <notification-url/>
					         <order-number/>
					         <order-version/>
					      </request-information>
					      <service-information>
					         <service-type>${serviceId}</service-type>
					         <service-instance-id>${vnfId}</service-instance-id>
					         <subscriber-name>dontcare</subscriber-name>
					      </service-information>
					      <vnf-request-information>
					         <vnf-id>${vfModuleId}</vnf-id>
					         <vnf-type>${vfModuleModelName}</vnf-type>
					         <vnf-name>${vfModuleName}</vnf-name>
					         <generic-vnf-id>${vnfId}</generic-vnf-id>
					         <generic-vnf-name>${vnfName}</generic-vnf-name>
							 <generic-vnf-type>${vnfType}</generic-vnf-type>
					         <tenant>${tenantId}</tenant>
					         <aic-cloud-region>${aicCloudRegion}</aic-cloud-region>
							 ${modelCustomizationUuidString}
							<use-preload>${usePreloadToSDNC}</use-preload>
					      </vnf-request-information>
 					</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
			sdncTopologyRequest = utils.formatXml(sdncTopologyRequest)
			execution.setVariable('DOUPVfMod_sdncActivateRequest', sdncTopologyRequest)
			utils.logAudit("sdncActivateRequest : " + sdncTopologyRequest)
			logDebug('Request for SDNCAdapter topology/activate:\n' + sdncTopologyRequest, isDebugLogEnabled)


			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def WorkflowException workflowException = (WorkflowException) execution.getVariable('WorkflowException')
			logError(method + ' caught WorkflowException: ' + workflowException.getErrorMessage())

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildWorkflowException(execution, 1002, 'Error in handleWorkflowException(): ' + e.getMessage())
		}
	}

	public void validateSDNCResponse(DelegateExecution execution, String response, String method){
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def prefix = execution.getVariable("prefix")

		logDebug(" *** STARTED ValidateSDNCResponse Process*** ", isDebugLogEnabled)

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		utils.logAudit("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		utils.logAudit("SDNCResponse: " + response)

		String sdncResponse = response
		if(execution.getVariable(prefix + 'sdncResponseSuccess') == true){
			logDebug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse, isDebugLogEnabled)
		}else{
			logDebug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.", isDebugLogEnabled)
			throw new BpmnError("MSOWorkflowException")
		}
		logDebug(" *** COMPLETED ValidateSDNCResponse Process*** ", isDebugLogEnabled)
	}

	/**
	 * Using the received vnfId and vfModuleId, query AAI to get the corresponding VNF info.
	 * A 200 response is expected with the VNF info in the response body. Will find out the base module info.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIVfModule(DelegateExecution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def method = getClass().getSimpleName() + '.getVfModule(' +
			'execution=' + execution.getId() +
			')'
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String  aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)

			String endPoint = execution.getVariable("URN_aai_endpoint") + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"
			utils.logAudit("AAI endPoint: " + endPoint)

			try {
				def aaiRequestId = UUID.randomUUID().toString()
				logDebug('sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
				APIResponse response = aaiUriUtil.executeAAIGetCall(execution, endPoint)
				utils.logAudit("createVfModule - invoking httpGet() to AAI")

				def responseData = response.getResponseBodyAsString()
				if (responseData != null) {
					logDebug("Received generic VNF data: " + responseData, isDebugLogEnabled)

				}

				utils.logAudit("createVfModule - queryAAIVfModule Response: " + responseData)
				utils.logAudit("createVfModule - queryAAIVfModule ResponseCode: " + response.getStatusCode())

				execution.setVariable('DOUPVfMod_queryAAIVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('DOUPVfMod_queryAAIVfModuleResponse', responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
				if (response.getStatusCode() == 200) {
					// Parse the VNF record from A&AI to find base module info
					logDebug('Parsing the VNF data to find base module info', isDebugLogEnabled)
					if (responseData != null) {
						def vfModulesText = utils.getNodeXml(responseData, "vf-modules")
						def xmlVfModules= new XmlSlurper().parseText(vfModulesText)
						def vfModules = xmlVfModules.'**'.findAll {it.name() == "vf-module"}
						int vfModulesSize = 0
						for (i in 0..vfModules.size()-1) {
							def vfModuleXml = groovy.xml.XmlUtil.serialize(vfModules[i])
							def isBaseVfModule = utils.getNodeText(vfModuleXml, "is-base-vf-module")

							if (isBaseVfModule == "true") {
							    String baseModuleId = utils.getNodeText1(vfModuleXml, "vf-module-id")
							    execution.setVariable("DOUPVfMod_baseVfModuleId", baseModuleId)
							    logDebug('Received baseVfModuleId: ' + baseModuleId, isDebugLogEnabled)
							    String baseModuleHeatStackId = utils.getNodeText1(vfModuleXml, "heat-stack-id")
							    execution.setVariable("DOUPVfMod_baseVfModuleHeatStackId", baseModuleHeatStackId)
							    logDebug('Received baseVfModuleHeatStackId: ' + baseModuleHeatStackId, isDebugLogEnabled)
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI GET:' + ex.getMessage(),isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModule(): ' + e.getMessage())
		}
	}


}
