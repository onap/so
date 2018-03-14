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

package org.openecomp.mso.bpmn.infrastructure.scripts;

import java.util.Map
import java.util.Currency.CurrencyNameGetter

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.VfModuleBase
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.json.DecomposeJsonUtil
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.client.aai.entities.AAIResultWrapper
import org.openecomp.mso.client.aai.entities.uri.AAIUri
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.springframework.web.util.UriUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import com.fasterxml.jackson.databind.ObjectMapper

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.AAIResourcesClient
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;



public class DoCreateVfModule extends VfModuleBase {

	String Prefix="DCVFM_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
	CatalogDbUtils catalog = new CatalogDbUtils()
	DecomposeJsonUtil decomposeJsonUtils = new DecomposeJsonUtil()

	/**
	 * Validates the request message and sets up the workflow.
	 * @param execution the execution
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		execution.setVariable('prefix', Prefix)
		try{
			def rollbackData = execution.getVariable("rollbackData")
			if (rollbackData == null) {
				rollbackData = new RollbackData()
			}

			execution.setVariable("DCVFM_vnfParamsExistFlag", false)
			execution.setVariable("DCVFM_oamManagementV4Address", "")
			execution.setVariable("DCVFM_oamManagementV6Address", "")

			String request = execution.getVariable("DoCreateVfModuleRequest")

			if (request == null || request.isEmpty()) {
				// Building Block-type request

				String vfModuleModelInfo = execution.getVariable("vfModuleModelInfo")		
				
				def serviceModelInfo = execution.getVariable("serviceModelInfo")
				logDebug("serviceModelInfo: " + serviceModelInfo, isDebugLogEnabled)
				String modelInvariantUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelInvariantUuid")
				logDebug("modelInvariantUuid: " + modelInvariantUuid, isDebugLogEnabled)
				
				def vnfModelInfo = execution.getVariable("vnfModelInfo")

				//tenantId
				def tenantId = execution.getVariable("tenantId")
				execution.setVariable("DCVFM_tenantId", tenantId)
				rollbackData.put("VFMODULE", "tenantid", tenantId)
				//volumeGroupId
				def volumeGroupId = execution.getVariable("volumeGroupId")
				execution.setVariable("DCVFM_volumeGroupId", volumeGroupId)
				//volumeGroupName
				def volumeGroupName = execution.getVariable("volumeGroupName")
				execution.setVariable("DCVFM_volumeGroupName", volumeGroupName)
				//cloudSiteId
				def cloudSiteId = execution.getVariable("lcpCloudRegionId")
				execution.setVariable("DCVFM_cloudSiteId", cloudSiteId)
				rollbackData.put("VFMODULE", "aiccloudregion", cloudSiteId)
				logDebug("cloudSiteId: " + cloudSiteId, isDebugLogEnabled)
				//vnfType
				def vnfType = execution.getVariable("vnfType")
				execution.setVariable("DCVFM_vnfType", vnfType)
				rollbackData.put("VFMODULE", "vnftype", vnfType)
				logDebug("vnfType: " + vnfType, isDebugLogEnabled)
				//vnfName
				def vnfName = execution.getVariable("vnfName")
				execution.setVariable("DCVFM_vnfName", vnfName)
				rollbackData.put("VFMODULE", "vnfname", vnfName)
				logDebug("vnfName: " + vnfName, isDebugLogEnabled)
				//vnfId
				def vnfId = execution.getVariable("vnfId")
				execution.setVariable("DCVFM_vnfId", vnfId)
				rollbackData.put("VFMODULE", "vnfid", vnfId)
				logDebug("vnfId: " + vnfId, isDebugLogEnabled)
				//vfModuleName
				def vfModuleName = execution.getVariable("vfModuleName")
				execution.setVariable("DCVFM_vfModuleName", vfModuleName)
				rollbackData.put("VFMODULE", "vfmodulename", vfModuleName)
				logDebug("vfModuleName: " + vfModuleName, isDebugLogEnabled)
				//vfModuleModelName
				def vfModuleModelName = jsonUtil.getJsonValue(vfModuleModelInfo, "modelName")
				execution.setVariable("DCVFM_vfModuleModelName", vfModuleModelName)
				rollbackData.put("VFMODULE", "vfmodulemodelname", vfModuleModelName)
				logDebug("vfModuleModelName: " + vfModuleModelName, isDebugLogEnabled)
				//modelCustomizationUuid
				def modelCustomizationUuid = jsonUtil.getJsonValue(vfModuleModelInfo, "modelCustomizationUuid")
				execution.setVariable("DCVFM_modelCustomizationUuid", modelCustomizationUuid)
				rollbackData.put("VFMODULE", "modelcustomizationuuid", modelCustomizationUuid)
				logDebug("modelCustomizationUuid: " + modelCustomizationUuid, isDebugLogEnabled)
				//vfModuleId
				def vfModuleId = execution.getVariable("vfModuleId")
				execution.setVariable("DCVFM_vfModuleId", vfModuleId)
				logDebug("vfModuleId: " + vfModuleId, isDebugLogEnabled)
				def requestId = execution.getVariable("msoRequestId")
				execution.setVariable("DCVFM_requestId", requestId)
				logDebug("requestId: " + requestId, isDebugLogEnabled)
				rollbackData.put("VFMODULE", "msorequestid", requestId)
				// Set mso-request-id to request-id for VNF Adapter interface
				execution.setVariable("mso-request-id", requestId)
				//serviceId
				def serviceId = execution.getVariable("serviceId")
				execution.setVariable("DCVFM_serviceId", serviceId)
				logDebug("serviceId: " + serviceId, isDebugLogEnabled)
				//serviceInstanceId
				def serviceInstanceId = execution.getVariable("serviceInstanceId")
				execution.setVariable("DCVFM_serviceInstanceId", serviceInstanceId)
				rollbackData.put("VFMODULE", "serviceInstanceId", serviceInstanceId)
				logDebug("serviceInstanceId: " + serviceInstanceId, isDebugLogEnabled)
				//source - HARDCODED
				def source = "VID"
				execution.setVariable("DCVFM_source", source)
				rollbackData.put("VFMODULE", "source", source)
				logDebug("source: " + source, isDebugLogEnabled)
				//backoutOnFailure
				def disableRollback = execution.getVariable("disableRollback")
				def backoutOnFailure = true
				if (disableRollback != null && disableRollback == true) {
					backoutOnFailure = false
				}
				execution.setVariable("DCVFM_backoutOnFailure", backoutOnFailure)
				logDebug("backoutOnFailure: " + backoutOnFailure, isDebugLogEnabled)
				//isBaseVfModule
				def isBaseVfModule = execution.getVariable("isBaseVfModule")
				execution.setVariable("DCVFM_isBaseVfModule", isBaseVfModule)
				logDebug("isBaseVfModule: " + isBaseVfModule, isDebugLogEnabled)
				//asdcServiceModelVersion
				def asdcServiceModelVersion = execution.getVariable("asdcServiceModelVersion")
				execution.setVariable("DCVFM_asdcServiceModelVersion", asdcServiceModelVersion)
				logDebug("asdcServiceModelVersion: " + asdcServiceModelVersion, isDebugLogEnabled)
				//personaModelId
				execution.setVariable("DCVFM_personaModelId", jsonUtil.getJsonValue(vfModuleModelInfo, "modelInvariantUuid"))			
				//personaModelVersion
				execution.setVariable("DCVFM_personaModelVersion", jsonUtil.getJsonValue(vfModuleModelInfo, "modelUuid"))
				//vfModuleLabel
				def vfModuleLabel = execution.getVariable("vfModuleLabel")
				if (vfModuleLabel != null) {
					execution.setVariable("DCVFM_vfModuleLabel", vfModuleLabel)
					logDebug("vfModuleLabel: " + vfModuleLabel, isDebugLogEnabled)
				}
				//Get or Generate UUID
				String uuid = execution.getVariable("DCVFM_uuid")
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
				//globalSubscriberId
				String globalSubscriberId = execution.getVariable("globalSubscriberId")
				execution.setVariable("DCVFM_globalSubscriberId", globalSubscriberId)
				logDebug("globalSubsrciberId: " + globalSubscriberId, isDebugLogEnabled)				
				Map<String,String> vfModuleInputParams = execution.getVariable("vfModuleInputParams")
				if (vfModuleInputParams != null) {
					execution.setVariable("DCVFM_vnfParamsMap", vfModuleInputParams)
					execution.setVariable("DCVFM_vnfParamsExistFlag", true)
				}
				//usePreload
				def usePreload = execution.getVariable("usePreload")
				execution.setVariable("DCVFM_usePreload", usePreload)
				logDebug("usePreload: " + usePreload, isDebugLogEnabled)
				//aLaCarte
				def aLaCarte = execution.getVariable("aLaCarte")				
				execution.setVariable("DCVFM_aLaCarte", aLaCarte)
				logDebug("aLaCarte: " + aLaCarte, isDebugLogEnabled)
				
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
				// The info is inside the request - DEAD CODE
				utils.logAudit("DoCreateVfModule request: " + request)

				//tenantId
				def tenantId = ""
				if (utils.nodeExists(request, "tenant-id")) {
					tenantId = utils.getNodeText(request, "tenant-id")
				}
				execution.setVariable("DCVFM_tenantId", tenantId)
				rollbackData.put("VFMODULE", "tenantid", tenantId)
				//volumeGroupId
				def volumeGroupId = ""
				if (utils.nodeExists(request, "volume-group-id")) {
					volumeGroupId = utils.getNodeText(request, "volume-group-id")
				}
				execution.setVariable("DCVFM_volumeGroupId", volumeGroupId)
				//volumeGroupId
				def volumeGroupName = ""
				if (utils.nodeExists(request, "volume-group-name")) {
					volumeGroupName = utils.getNodeText(request, "volume-group-name")
				}
				execution.setVariable("DCVFM_volumeGroupName", volumeGroupName)
				//cloudSiteId
				def cloudSiteId = ""
				if (utils.nodeExists(request, "aic-cloud-region")) {
					cloudSiteId = utils.getNodeText(request, "aic-cloud-region")
				}
				execution.setVariable("DCVFM_cloudSiteId", cloudSiteId)
				rollbackData.put("VFMODULE", "aiccloudregion", cloudSiteId)
				logDebug("cloudSiteId: " + cloudSiteId, isDebugLogEnabled)
				//vnfType
				def vnfType = ""
				if (utils.nodeExists(request, "vnf-type")) {
					vnfType = utils.getNodeText(request, "vnf-type")
				}
				execution.setVariable("DCVFM_vnfType", vnfType)
				rollbackData.put("VFMODULE", "vnftype", vnfType)
				logDebug("vnfType: " + vnfType, isDebugLogEnabled)
				//vnfName
				def vnfName = ""
				if (utils.nodeExists(request, "vnf-name")) {
					vnfName = utils.getNodeText(request, "vnf-name")
				}
				execution.setVariable("DCVFM_vnfName", vnfName)
				rollbackData.put("VFMODULE", "vnfname", vnfName)
				logDebug("vnfName: " + vnfName, isDebugLogEnabled)
				//vnfId
				def vnfId = ""
				if (utils.nodeExists(request, "vnf-id")) {
					vnfId = utils.getNodeText(request, "vnf-id")
				}
				execution.setVariable("DCVFM_vnfId", vnfId)
				rollbackData.put("VFMODULE", "vnfid", vnfId)
				logDebug("vnfId: " + vnfId, isDebugLogEnabled)
				//vfModuleName
				def vfModuleName = ""
				if (utils.nodeExists(request, "vf-module-name")) {
					vfModuleName = utils.getNodeText(request, "vf-module-name")
				}
				execution.setVariable("DCVFM_vfModuleName", vfModuleName)
				rollbackData.put("VFMODULE", "vfmodulename", vfModuleName)
				logDebug("vfModuleName: " + vfModuleName, isDebugLogEnabled)
				//vfModuleModelName
				def vfModuleModelName = ""
				if (utils.nodeExists(request, "vf-module-model-name")) {
					vfModuleModelName = utils.getNodeText(request, "vf-module-model-name")
				}
				execution.setVariable("DCVFM_vfModuleModelName", vfModuleModelName)
				rollbackData.put("VFMODULE", "vfmodulemodelname", vfModuleModelName)
				logDebug("vfModuleModelName: " + vfModuleModelName, isDebugLogEnabled)
				//modelCustomizationUuid
				def modelCustomizationUuid = ""
				if (utils.nodeExists(request, "model-customization-id")) {
					modelCustomizationUuid = utils.getNodeText(request, "model-customization-id")
				}
				execution.setVariable("DCVFM_modelCustomizationUuid", modelCustomizationUuid)
				rollbackData.put("VFMODULE", "modelcustomizationuuid", modelCustomizationUuid)
				logDebug("modelCustomizationUuid: " + modelCustomizationUuid, isDebugLogEnabled)
				//vfModuleId
				def vfModuleId = ""
				if (utils.nodeExists(request, "vf-module-id")) {
					vfModuleId = utils.getNodeText(request, "vf-module-id")
				}
				execution.setVariable("DCVFM_vfModuleId", vfModuleId)
				logDebug("vfModuleId: " + vfModuleId, isDebugLogEnabled)
				def requestId = ""
				if (utils.nodeExists(request, "request-id")) {
					requestId = utils.getNodeText(request, "request-id")
				}
				execution.setVariable("DCVFM_requestId", requestId)
				logDebug("requestId: " + requestId, isDebugLogEnabled)
				//serviceId
				def serviceId = ""
				if (utils.nodeExists(request, "service-id")) {
					serviceId = utils.getNodeText(request, "service-id")
				}
				execution.setVariable("DCVFM_serviceId", serviceId)
				logDebug("serviceId: " + serviceId, isDebugLogEnabled)
				//serviceInstanceId
				def serviceInstanceId = ""
				if (utils.nodeExists(request, "service-instance-id")) {
					serviceInstanceId = utils.getNodeText(request, "service-instance-id")
				}
				execution.setVariable("DCVFM_serviceInstanceId", serviceInstanceId)
				rollbackData.put("VFMODULE", "serviceInstanceId", serviceInstanceId)
				logDebug("serviceInstanceId: " + serviceInstanceId, isDebugLogEnabled)
				//source
				def source = ""
				if (utils.nodeExists(request, "source")) {
					source = utils.getNodeText(request, "source")
				}
				execution.setVariable("DCVFM_source", source)
				rollbackData.put("VFMODULE", "source", source)
				logDebug("source: " + source, isDebugLogEnabled)
				//backoutOnFailure
				NetworkUtils networkUtils = new NetworkUtils()
				def backoutOnFailure = networkUtils.isRollbackEnabled(execution,request)
				execution.setVariable("DCVFM_backoutOnFailure", backoutOnFailure)
				logDebug("backoutOnFailure: " + backoutOnFailure, isDebugLogEnabled)
				//isBaseVfModule
				def isBaseVfModule = "false"
				if (utils.nodeExists(request, "is-base-vf-module")) {
					isBaseVfModule = utils.getNodeText(request, "is-base-vf-module")
				}
				execution.setVariable("DCVFM_isBaseVfModule", isBaseVfModule)
				logDebug("isBaseVfModule: " + isBaseVfModule, isDebugLogEnabled)				
				//asdcServiceModelVersion
				def asdcServiceModelVersion = ""
				if (utils.nodeExists(request, "asdc-service-model-version")) {
					asdcServiceModelVersion = utils.getNodeText(request, "asdc-service-model-version")
				}
				execution.setVariable("DCVFM_asdcServiceModelVersion", asdcServiceModelVersion)
				logDebug("asdcServiceModelVersion: " + asdcServiceModelVersion, isDebugLogEnabled)

				//personaModelId
				def personaModelId = ""
				if (utils.nodeExists(request, "persona-model-id")) {
					personaModelId = utils.getNodeText(request, "persona-model-id")
				}
				execution.setVariable("DCVFM_personaModelId", personaModelId)
				logDebug("personaModelId: " + personaModelId, isDebugLogEnabled)

				//personaModelVersion
				def personaModelVersion = ""
				if (utils.nodeExists(request, "persona-model-version")) {
					personaModelVersion = utils.getNodeText(request, "persona-model-version")
				}
				execution.setVariable("DCVFM_personaModelVersion", personaModelVersion)
				logDebug("personaModelVersion: " + personaModelVersion, isDebugLogEnabled)

				// Process the parameters

						String vnfParamsChildNodes = utils.getChildNodes(request, "vnf-params")
						if(vnfParamsChildNodes == null || vnfParamsChildNodes.length() < 1){
								utils.log("DEBUG", "Request contains NO VNF Params", isDebugLogEnabled)
						}else{
								utils.log("DEBUG", "Request does contain VNF Params", isDebugLogEnabled)
								execution.setVariable("DCVFM_vnfParamsExistFlag", true)

								InputSource xmlSource = new InputSource(new StringReader(request));
								DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
								docFactory.setNamespaceAware(true)
								DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
								Document xml = docBuilder.parse(xmlSource)
								//Get params, build map
								Map<String, String> paramsMap = new HashMap<String, String>()
								NodeList paramsList = xml.getElementsByTagNameNS("*", "param")

								for (int z = 0; z < paramsList.getLength(); z++) {
									Node node = paramsList.item(z)
									String paramValue = node.getTextContent()
									NamedNodeMap e = node.getAttributes()
									String paramName = e.getNamedItem("name").getTextContent()
									paramsMap.put(paramName, paramValue)
								}
								execution.setVariable("DCVFM_vnfParamsMap", paramsMap)
							}
			}

			//Get or Generate UUID
			String uuid = execution.getVariable("DCVFM_uuid")
			if(uuid == null){
				uuid = UUID.randomUUID()
				logDebug("Generated messageId (UUID) is: " + uuid, isDebugLogEnabled)
			}else{
				logDebug("Found messageId (UUID) is: " + uuid, isDebugLogEnabled)
			}
			// Get sdncVersion, default to empty
			String sdncVersion = execution.getVariable("sdncVersion")
			if (sdncVersion == null) {
				sdncVersion = ""
			}
			logDebug("sdncVersion: " + sdncVersion, isDebugLogEnabled)
			execution.setVariable("DCVFM_sdncVersion", sdncVersion)
			
			execution.setVariable("DCVFM_uuid", uuid)
			execution.setVariable("DCVFM_baseVfModuleId", "")
			execution.setVariable("DCVFM_baseVfModuleHeatStackId", "")
			execution.setVariable("DCVFM_heatStackId", "")
			execution.setVariable("DCVFM_contrailServiceInstanceFqdn", "")
			execution.setVariable("DCVFM_volumeGroupStackId", "")
			execution.setVariable("DCVFM_cloudRegionForVolume", "")
			execution.setVariable("DCVFM_contrailNetworkPolicyFqdnList", "")
			execution.setVariable("DCVFM_vnfTypeToQuery", "generic-vnf")
			rollbackData.put("VFMODULE", "rollbackPrepareUpdateVfModule", "false")
			rollbackData.put("VFMODULE", "rollbackUpdateAAIVfModule", "false")
			rollbackData.put("VFMODULE", "rollbackVnfAdapterCreate", "false")
			rollbackData.put("VFMODULE", "rollbackSDNCRequestActivate", "false")
			rollbackData.put("VFMODULE", "rollbackSDNCRequestAssign", "false")
			rollbackData.put("VFMODULE", "rollbackCreateAAIVfModule", "false")
			rollbackData.put("VFMODULE", "rollbackCreateNetworkPoliciesAAI", "false")
			rollbackData.put("VFMODULE", "rollbackUpdateVnfAAI", "false")
			rollbackData.put("VFMODULE", "heatstackid", "")

			String sdncCallbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
				if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
					def msg = 'Required variable \'URN_mso_workflow_sdncadapter_callback\' is missing'
					logError(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}
				execution.setVariable("DCVFM_sdncCallbackUrl", sdncCallbackUrl)
				utils.logAudit("SDNC Callback URL: " + sdncCallbackUrl)
			    logDebug("SDNC Callback URL is: " + sdncCallbackUrl, isDebugLogEnabled)


			execution.setVariable("rollbackData", rollbackData)
		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error encountered in PreProcess method!")
		}

		logDebug('Exited ' + method, isDebugLogEnabled)
	}

	/**
	 * Validates a workflow response.
	 * @param execution the execution
	 * @param responseVar the execution variable in which the response is stored
	 * @param responseCodeVar the execution variable in which the response code is stored
	 * @param errorResponseVar the execution variable in which the error response is stored
	 */
	public void validateWorkflowResponse(DelegateExecution execution, String responseVar,
			String responseCodeVar, String errorResponseVar) {
		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, responseVar, responseCodeVar, errorResponseVar)
	}


	/**
	 * Sends the empty, synchronous response back to the API Handler.
	 * @param execution the execution
	 */
	public void sendResponse(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.sendResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			sendWorkflowResponse(execution, 200, "")
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Internal Error')
		}
	}

	/**
	 * Using the received vnfId and vfModuleId, query AAI to get the corresponding VNF info.
	 * A 200 response is expected with the VNF info in the response body. Will find out the base module info
	 * and existing VNF's name for add-on modules
	 *
	 * @param execution The flow's execution instance.
	 */
	public void postProcessCreateAAIVfModule(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.getVfModule(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def createResponse = execution.getVariable('DCVFM_createVfModuleResponse')
			utils.logAudit("createVfModule Response: " + createResponse)

			def rollbackData = execution.getVariable("rollbackData")
			String vnfName = utils.getNodeText1(createResponse, 'vnf-name')
			if (vnfName != null) {
				execution.setVariable('DCVFM_vnfName', vnfName)
				logDebug("vnfName retrieved from AAI is: " + vnfName, isDebugLogEnabled)
				rollbackData.put("VFMODULE", "vnfname", vnfName)
			}
			String vnfId = utils.getNodeText1(createResponse, 'vnf-id')
			execution.setVariable('DCVFM_vnfId', vnfId)
			logDebug("vnfId is: " + vnfId, isDebugLogEnabled)
			String vfModuleId = utils.getNodeText1(createResponse, 'vf-module-id')
			execution.setVariable('DCVFM_vfModuleId', vfModuleId)
			logDebug("vfModuleId is: " + vfModuleId, isDebugLogEnabled)
			String vfModuleIndex= utils.getNodeText1(createResponse, 'vf-module-index')
			execution.setVariable('DCVFM_vfModuleIndex', vfModuleIndex)
			logDebug("vfModuleIndex is: " + vfModuleIndex, isDebugLogEnabled)
			rollbackData.put("VFMODULE", "vnfid", vnfId)
			rollbackData.put("VFMODULE", "vfmoduleid", vfModuleId)
			rollbackData.put("VFMODULE", "rollbackCreateAAIVfModule", "true")
			rollbackData.put("VFMODULE", "rollbackPrepareUpdateVfModule", "true")
			execution.setVariable("rollbackData", rollbackData)
		} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while postProcessing CreateAAIVfModule request:' + ex.getMessage(),isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Bad response from CreateAAIVfModule' + ex.getMessage())
		}
		logDebug('Exited ' + method, isDebugLogEnabled)
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
			def vnfId = execution.getVariable('DCVFM_vnfId')
			def vfModuleId = execution.getVariable('DCVFM_vfModuleId')

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String  aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)

			String endPoint = execution.getVariable("URN_aai_endpoint") + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"
			utils.logAudit("AAI endPoint: " + endPoint)

			try {
				RESTConfig config = new RESTConfig(endPoint);
				def responseData = ''
				def aaiRequestId = UUID.randomUUID().toString()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml');
				logDebug('sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
				APIResponse response = client.httpGet()
				utils.logAudit("createVfModule - invoking httpGet() to AAI")

				responseData = response.getResponseBodyAsString()
				if (responseData != null) {
					logDebug("Received generic VNF data: " + responseData, isDebugLogEnabled)

				}

				utils.logAudit("createVfModule - queryAAIVfModule Response: " + responseData)
				utils.logAudit("createVfModule - queryAAIVfModule ResponseCode: " + response.getStatusCode())

				execution.setVariable('DCVFM_queryAAIVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('DCVFM_queryAAIVfModuleResponse', responseData)
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
							    execution.setVariable("DCVFM_baseVfModuleId", baseModuleId)
							    logDebug('Received baseVfModuleId: ' + baseModuleId, isDebugLogEnabled)
							    String baseModuleHeatStackId = utils.getNodeText1(vfModuleXml, "heat-stack-id")
							    execution.setVariable("DCVFM_baseVfModuleHeatStackId", baseModuleHeatStackId)
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
	
	/**
	 * Using the vnfId and vfModuleName provided in the inputs, 
	 * query AAI to get the corresponding VF Module info.
	 * A 200 response is expected with the VF Module info in the response body, 
	 * or a 404 response if the module does not exist yet. Will determine VF Module's 
	 * orchestration status if one exists
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIVfModuleForStatus(DelegateExecution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def method = getClass().getSimpleName() + '.queryAAIVfModuleForStatus(' +
			'execution=' + execution.getId() +
			')'
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		execution.setVariable('DCVFM_orchestrationStatus', '')

		try {
			def vnfId = execution.getVariable('DCVFM_vnfId')
			def vfModuleName = execution.getVariable('DCVFM_vfModuleName')

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String  aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)

			String endPoint = execution.getVariable("URN_aai_endpoint") + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + 
					"/vf-modules/vf-module?vf-module-name=" + UriUtils.encode(vfModuleName, "UTF-8")
			utils.logAudit("AAI endPoint: " + endPoint)

			try {
				RESTConfig config = new RESTConfig(endPoint);
				def responseData = ''
				def aaiRequestId = UUID.randomUUID().toString()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml');
				logDebug('sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
				APIResponse response = client.httpGet()
				utils.logAudit("createVfModule - invoking httpGet() to AAI")

				responseData = response.getResponseBodyAsString()
				if (responseData != null) {
					logDebug("Received generic VNF data: " + responseData, isDebugLogEnabled)

				}

				utils.logAudit("createVfModule - queryAAIVfModule Response: " + responseData)
				utils.logAudit("createVfModule - queryAAIVfModule ResponseCode: " + response.getStatusCode())

				execution.setVariable('DCVFM_queryAAIVfModuleForStatusResponseCode', response.getStatusCode())
				execution.setVariable('DCVFM_queryAAIVfModuleForStatusResponse', responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
				// Retrieve VF Module info and its orchestration status; if not found, do nothing
				if (response.getStatusCode() == 200) {
					// Parse the VNF record from A&AI to find base module info
					logDebug('Parsing the VNF data to find orchestration status', isDebugLogEnabled)
					if (responseData != null) {
						def vfModuleText = utils.getNodeXml(responseData, "vf-module")
						//def xmlVfModule= new XmlSlurper().parseText(vfModuleText)
						def orchestrationStatus = utils.getNodeText1(vfModuleText, "orchestration-status")
						execution.setVariable("DCVFM_orchestrationStatus", orchestrationStatus)
						// Also retrieve vfModuleId
						def vfModuleId = utils.getNodeText1(vfModuleText, "vf-module-id")
						execution.setVariable("DCVFM_vfModuleId", vfModuleId)
						logDebug("Received orchestration status from A&AI: " + orchestrationStatus, isDebugLogEnabled)
						
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
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModuleForStatus(): ' + e.getMessage())
		}
	}


	public void preProcessSDNCAssignRequest(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
		def vnfId = execution.getVariable("DCVFM_vnfId")
		def vfModuleId = execution.getVariable("DCVFM_vfModuleId")
		def serviceInstanceId = execution.getVariable("DCVFM_serviceInstanceId")
		logDebug("NEW VNF ID: " + vnfId, isDebugLogEnabled)
		utils.logAudit("NEW VNF ID: " + vnfId)

		try{

			//Build SDNC Request

			def svcInstId = ""
			if (serviceInstanceId == null || serviceInstanceId.isEmpty()) {
				svcInstId = vfModuleId
			}
			else {
				svcInstId = serviceInstanceId
			}

			String assignSDNCRequest = buildSDNCRequest(execution, svcInstId, "assign")

			assignSDNCRequest = utils.formatXml(assignSDNCRequest)
			execution.setVariable("DCVFM_assignSDNCRequest", assignSDNCRequest)
			logDebug("Outgoing AssignSDNCRequest is: \n" + assignSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing AssignSDNCRequest is: \n" + assignSDNCRequest)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessSDNCAssignRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during prepareProvision Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
	}

	public void preProcessSDNCGetRequest(DelegateExecution execution, String element){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		String sdncVersion = execution.getVariable("DCVFM_sdncVersion")
		execution.setVariable("prefix", Prefix)
		utils.log("DEBUG", " ======== STARTED preProcessSDNCGetRequest Process ======== ", isDebugLogEnabled)
		try{
			def serviceInstanceId = execution.getVariable('DCVFM_serviceInstanceId')
			
			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("mso-request-id") + "-" +  System.currentTimeMillis()
			}
					
			def callbackUrl = execution.getVariable("DCVFM_sdncCallbackUrl")
			utils.logAudit("callbackUrl:" + callbackUrl)
			
			def vfModuleId = execution.getVariable('DCVFM_vfModuleId')
			
			def svcInstId = ""
			if (serviceInstanceId == null || serviceInstanceId.isEmpty()) {
				svcInstId = vfModuleId
			}
			else {
				svcInstId = serviceInstanceId
			}
			
			def msoAction = ""
			if (!sdncVersion.equals("1707")) {
				msoAction = "mobility"
			}
			else {
				msoAction = "vfmodule"
			}
			// For VNF, serviceOperation (URI for topology GET) will be retrieved from "selflink" element 
			// in the response from GenericGetVnf
			// For VF Module, in 1707 serviceOperation will be retrieved from "object-path" element
			// in SDNC Assign Response
			// For VF Module for older versions, serviceOperation is constructed using vfModuleId
			
			String serviceOperation = ""
			if (element.equals("vnf")) {
				def vnfQueryResponse = execution.getVariable("DCVFM_vnfQueryResponse")
				serviceOperation = utils.getNodeText1(vnfQueryResponse, "selflink")
				utils.log("DEBUG", "VNF - service operation: " + serviceOperation, isDebugLogEnabled)
			}
			else if (element.equals("vfmodule")) {
				String response = execution.getVariable("DCVFM_assignSDNCAdapterResponse")
				utils.logAudit("DCVFM_assignSDNCAdapterResponse is: \n" + response)							
			
				if (!sdncVersion.equals("1707")) {
					serviceOperation = "/VNF-API:vnfs/vnf-list/" + vfModuleId
					utils.log("DEBUG", "VF Module with sdncVersion before 1707 - service operation: " + serviceOperation, isDebugLogEnabled)
				}
				else {				
					String data = utils.getNodeXml(response, "response-data")					
					data = data.replaceAll("&lt;", "<")
					data = data.replaceAll("&gt;", ">")
					utils.log("DEBUG", "responseData: " + data, isDebugLogEnabled)
					serviceOperation = utils.getNodeText1(data, "object-path")
					utils.log("DEBUG", "VF Module with sdncVersion of 1707 - service operation: " + serviceOperation, isDebugLogEnabled)
				}				
			}		

			//!!!! TEMPORARY WORKAROUND FOR SDNC REPLICATION ISSUE
			sleep(5000)

			String SDNCGetRequest =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
											xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
											xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
					<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
					<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
					<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
					<sdncadapter:SvcOperation>${serviceOperation}</sdncadapter:SvcOperation>
					<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
					<sdncadapter:MsoAction>${msoAction}</sdncadapter:MsoAction>
				</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData></sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			utils.logAudit("SDNCGetRequest: \n" + SDNCGetRequest)
			execution.setVariable("DCVFM_getSDNCRequest", SDNCGetRequest)
			utils.log("DEBUG", "Outgoing GetSDNCRequest is: \n" + SDNCGetRequest, isDebugLogEnabled)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occurred Processing preProcessSDNCGetRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during prepareProvision Method:\n" + e.getMessage())
		}
		utils.log("DEBUG", "======== COMPLETED preProcessSDNCGetRequest Process ======== ", isDebugLogEnabled)
	}


	public void preProcessVNFAdapterRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.VNFAdapterCreateVfModule(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		//def xml = execution.getVariable("DoCreateVfModuleRequest")
		//logDebug('VNF REQUEST is: ' + xml, isDebugLogEnabled)

		//Get variables
		//cloudSiteId
		def cloudSiteId = execution.getVariable("DCVFM_cloudSiteId")
		//tenantId
		def tenantId = execution.getVariable("DCVFM_tenantId")
		//vnfType
		def vnfType = execution.getVariable("DCVFM_vnfType")
		//vnfName
		def vnfName = execution.getVariable("DCVFM_vnfName")
		//vnfId
		def vnfId = execution.getVariable("DCVFM_vnfId")
		//vfModuleName
		def vfModuleName = execution.getVariable("DCVFM_vfModuleName")
		//vfModuleModelName
		def vfModuleModelName = execution.getVariable("DCVFM_vfModuleModelName")
		//vfModuleId
		def vfModuleId = execution.getVariable("DCVFM_vfModuleId")
		//vfModuleIndex
		def vfModuleIndex = execution.getVariable("DCVFM_vfModuleIndex")
		//requestId
		def requestId = execution.getVariable("DCVFM_requestId")
		//serviceId
		def serviceId = execution.getVariable("DCVFM_serviceId")
		//serviceInstanceId
		def serviceInstanceId = execution.getVariable("DCVFM_serviceInstanceId")
		//backoutOnFailure
		def backoutOnFailure = execution.getVariable("DCVFM_backoutOnFailure")
		//volumeGroupId
		def volumeGroupId = execution.getVariable("DCVFM_volumeGroupId")
		// baseVfModuleId
		def baseVfModuleId = execution.getVariable("DCVFM_baseVfModuleId")
		// baseVfModuleStackId
		def baseVfModuleStackId = execution.getVariable("DCVFM_baseVfModuleHeatStackId")
		// asdcServiceModelVersion
		def asdcServiceModelVersion = execution.getVariable("DCVFM_asdcServiceModelVersion")
		//volumeGroupStackId
		def volumeGroupStackId = execution.getVariable("DCVFM_volumeGroupStackId")
		//modelCustomizationUuid
		def modelCustomizationUuid = execution.getVariable("DCVFM_modelCustomizationUuid")
		//environmentContext
		String environmentContext = execution.getVariable("DCVFM_environmentContext")
		//workloadContext
		String workloadContext = execution.getVariable("DCVFM_workloadContext")
		logDebug("workloadContext: " + workloadContext, isDebugLogEnabled)
		logDebug("environmentContext: " + environmentContext, isDebugLogEnabled)
		
		def messageId = execution.getVariable('mso-request-id') + '-' +
                                System.currentTimeMillis()

		def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
		def useQualifiedHostName = execution.getVariable("URN_mso_use_qualified_host")

		utils.logAudit("notificationUrl: " + notificationUrl)
		utils.logAudit("QualifiedHostName: " + useQualifiedHostName)

		if ('true'.equals(useQualifiedHostName)) {
			notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}

		Map<String, String> vnfParamsMap = execution.getVariable("DCVFM_vnfParamsMap")
		String vfModuleParams = ""
		//Get SDNC Response Data for VF Module Topology
		String vfModuleSdncGetResponse = execution.getVariable('DCVFM_getSDNCAdapterResponse')
		utils.logAudit("sdncGetResponse: " + vfModuleSdncGetResponse)
		def sdncVersion = execution.getVariable("sdncVersion")
		
		if (!sdncVersion.equals("1707")) {
						
			vfModuleParams = buildVfModuleParams(vnfParamsMap, vfModuleSdncGetResponse, vnfId, vnfName,
				vfModuleId, vfModuleName, vfModuleIndex, environmentContext, workloadContext)
		}
		else {
			//Get SDNC Response Data for Vnf Topology
			String vnfSdncGetResponse = execution.getVariable('DCVFM_getVnfSDNCAdapterResponse')
			utils.logAudit("vnfSdncGetResponse: " + vnfSdncGetResponse)
			
			vfModuleParams = buildVfModuleParamsFromCombinedTopologies(vnfParamsMap, vnfSdncGetResponse, vfModuleSdncGetResponse, vnfId, vnfName,
				vfModuleId, vfModuleName, vfModuleIndex, environmentContext, workloadContext)			
		}

		def svcInstId = ""
		if (serviceInstanceId == null || serviceInstanceId.isEmpty()) {
				svcInstId = serviceId
		}
		else {
			svcInstId = serviceInstanceId
		}

		def createVnfARequest = """
		<createVfModuleRequest>
		<cloudSiteId>${cloudSiteId}</cloudSiteId>
		<tenantId>${tenantId}</tenantId>
		<vnfId>${vnfId}</vnfId>
		<vnfName>${vnfName}</vnfName>
		<vfModuleName>${vfModuleName}</vfModuleName>
		<vfModuleId>${vfModuleId}</vfModuleId>
		<vnfType>${vnfType}</vnfType>
		<vfModuleType>${vfModuleModelName}</vfModuleType>
		<vnfVersion>${asdcServiceModelVersion}</vnfVersion>
		<modelCustomizationUuid>${modelCustomizationUuid}</modelCustomizationUuid>
		<requestType></requestType>
		<volumeGroupId>${volumeGroupId}</volumeGroupId>
    	<volumeGroupStackId>${volumeGroupStackId}</volumeGroupStackId>
    	<baseVfModuleId>${baseVfModuleId}</baseVfModuleId>
    	<baseVfModuleStackId>${baseVfModuleStackId}</baseVfModuleStackId>
    	<skipAAI>true</skipAAI>
    	<backout>${backoutOnFailure}</backout>
    	<failIfExists>true</failIfExists>
		<vfModuleParams>
		${vfModuleParams}
		</vfModuleParams>
		<msoRequest>
			<requestId>${requestId}</requestId>
			<serviceInstanceId>${svcInstId}</serviceInstanceId>
		</msoRequest>
		<messageId>${messageId}</messageId>
		<notificationUrl>${notificationUrl}</notificationUrl>
		</createVfModuleRequest>"""

		utils.logAudit("Create VfModule Request to VNF Adapter : " + createVnfARequest)
		logDebug("Create VfModule Request to VNF Adapter: " + createVnfARequest, isDebugLogEnabled)
		execution.setVariable("DCVFM_createVnfARequest", createVnfARequest)
	}

	/**
	 * Validates the request, request id and service instance id.  If a problem is found,
	 * a WorkflowException is generated and an MSOWorkflowException event is thrown. This
	 * method also sets up the log context for the workflow.
	 * @param execution the execution
	 * @return the validated request
	 */
	public String validateInfraRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.validateInfraRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		String processKey = getProcessKey(execution);
		def prefix = execution.getVariable("prefix")

		if (prefix == null) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " prefix is null")
		}

		try {
			def request = execution.getVariable(prefix + 'Request')

			if (request == null) {
				request = execution.getVariable(processKey + 'Request')

				if (request == null) {
					request = execution.getVariable('bpmnRequest')
				}

				setVariable(execution, processKey + 'Request', null);
				setVariable(execution, 'bpmnRequest', null);
				setVariable(execution, prefix + 'Request', request);
			}

			if (request == null) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " request is null")
			}
			utils.logAudit("DoCreateVfModule Request: " + request)

			/*

			def requestId = execution.getVariable("mso-request-id")

			if (requestId == null) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " request has no mso-request-id")
			}

			def serviceInstanceId = execution.getVariable("mso-service-instance-id")

			if (serviceInstanceId == null) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " request message has no mso-service-instance-id")
			}

			utils.logContext(requestId, serviceInstanceId)
			*/
			logDebug('Incoming message: ' + System.lineSeparator() + request, isDebugLogEnabled)
			logDebug('Exited ' + method, isDebugLogEnabled)
			return request
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Invalid Message")
		}
	}

	public boolean isVolumeGroupIdPresent(DelegateExecution execution) {

		def method = getClass().getSimpleName() + '.isVolumeGroupIdPresent(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		def request = execution.getVariable('DoCreateVfModuleRequest')
		String volumeGroupId = utils.getNodeText1(request, "volume-group-id")
		if (volumeGroupId == null || volumeGroupId.isEmpty()) {
			logDebug('No volume group id is present', isDebugLogEnabled)
			return false
		}
		else {
			logDebug('Volume group id is present', isDebugLogEnabled)
			return true
		}

	}

	public boolean isVolumeGroupNamePresent(DelegateExecution execution) {

		def method = getClass().getSimpleName() + '.isVolumeGroupNamePresent(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		def request = execution.getVariable('DoCreateVfModuleRequest')
		String volumeGroupName = utils.getNodeText1(request, "volume-group-name")
		if (volumeGroupName == null || volumeGroupName.isEmpty()) {
			logDebug('No volume group name is present', isDebugLogEnabled)
			return false
		}
		else {
			logDebug('Volume group name is present', isDebugLogEnabled)
			return true
		}

	}

	public String buildSDNCRequest(DelegateExecution execution, String svcInstId, String action){

		String uuid = execution.getVariable('testReqId') // for junits
		if(uuid==null){
			uuid = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
		}
		def callbackURL = execution.getVariable("DCVFM_sdncCallbackUrl")
		def requestId = execution.getVariable("DCVFM_requestId")
		def serviceId = execution.getVariable("DCVFM_serviceId")
		def vnfType = execution.getVariable("DCVFM_vnfType")
		def vnfName = execution.getVariable("DCVFM_vnfName")
		def tenantId = execution.getVariable("DCVFM_tenantId")
		def source = execution.getVariable("DCVFM_source")
		def backoutOnFailure = execution.getVariable("DCVFM_backoutOnFailure")
		def vfModuleId = execution.getVariable("DCVFM_vfModuleId")
		def vfModuleName = execution.getVariable("DCVFM_vfModuleName")
		def vfModuleModelName = execution.getVariable("DCVFM_vfModuleModelName")
		def vnfId = execution.getVariable("DCVFM_vnfId")
		def cloudSiteId = execution.getVariable("DCVFM_cloudSiteId")
		def sdncVersion = execution.getVariable("DCVFM_sdncVersion")
		def serviceModelInfo = execution.getVariable("serviceModelInfo")
		def vnfModelInfo = execution.getVariable("vnfModelInfo")
		def vfModuleModelInfo = execution.getVariable("vfModuleModelInfo")
		String serviceEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(serviceModelInfo)
		String vnfEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(vnfModelInfo)
		String vfModuleEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(vfModuleModelInfo)
		def globalSubscriberId = execution.getVariable("DCVFM_globalSubscriberId")
		boolean usePreload = execution.getVariable("DCVFM_usePreload")
		String usePreloadToSDNC = usePreload ? "Y" : "N"
		def modelCustomizationUuid = execution.getVariable("DCVFM_modelCustomizationUuid")
		def modelCustomizationUuidString = ""
		if (!usePreload) {
			modelCustomizationUuidString = "<model-customization-uuid>" + modelCustomizationUuid + "</model-customization-uuid>"
		}

		String sdncVNFParamsXml = ""

		if(execution.getVariable("DCVFM_vnfParamsExistFlag") == true){
			sdncVNFParamsXml = buildSDNCParamsXml(execution)
		}else{
			sdncVNFParamsXml = ""
		}
		
		String sdncRequest = ""
		
		if (!sdncVersion.equals("1707")) {

			sdncRequest =
		"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${requestId}</request-id>
			<request-action>VNFActivateRequest</request-action>
			<source>${source}</source>
			<notification-url/>
		</request-information>
		<service-information>
			<service-id>${serviceId}</service-id>
			<service-type>${serviceId}</service-type>
			<service-instance-id>${svcInstId}</service-instance-id>
			<subscriber-name>notsurewecare</subscriber-name>
		</service-information>
		<vnf-request-information>
			<vnf-id>${vfModuleId}</vnf-id>
			<vnf-type>${vfModuleModelName}</vnf-type>
			<vnf-name>${vfModuleName}</vnf-name>
			<generic-vnf-id>${vnfId}</generic-vnf-id>
			<generic-vnf-name>${vnfName}</generic-vnf-name>
			<generic-vnf-type>${vnfType}</generic-vnf-type>
			<aic-cloud-region>${cloudSiteId}</aic-cloud-region>
			<tenant>${tenantId}</tenant>
			${modelCustomizationUuidString}
			<use-preload>${usePreloadToSDNC}</use-preload>
		${sdncVNFParamsXml}
		</vnf-request-information>
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
		
		}
		else {	
			
			sdncRequest =
			"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vf-module-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
				<sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${requestId}</request-id>
			<request-action>CreateVfModuleInstance</request-action>
			<source>${source}</source>
			<notification-url/>
		</request-information>
		<service-information>
			<service-id>${serviceId}</service-id>
			<subscription-service-type>${serviceId}</subscription-service-type>
			${serviceEcompModelInformation}
			<service-instance-id>${svcInstId}</service-instance-id>
			<global-customer-id>${globalSubscriberId}</global-customer-id>			
		</service-information>		
		<vnf-information>
			<vnf-id>${vnfId}</vnf-id>
			<vnf-type>${vnfType}</vnf-type>
			${vnfEcompModelInformation}			
		</vnf-information>
		<vf-module-information>
			<vf-module-id>${vfModuleId}</vf-module-id>
			<vf-module-type>${vfModuleModelName}</vf-module-type>
			${vfModuleEcompModelInformation}			
		</vf-module-information>
		<vf-module-request-input>			
			<vf-module-name>${vfModuleName}</vf-module-name>
			<tenant>${tenantId}</tenant>
			<aic-cloud-region>${cloudSiteId}</aic-cloud-region>			
		${sdncVNFParamsXml}
		</vf-module-request-input>
	  </sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
			
			
			/*
			sdncRequest =
			"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${requestId}</request-id>
			<request-action>CreateVfModuleInstance</request-action>
			<source>${source}</source>
			<notification-url/>
		</request-information>
		<service-information>
			<service-id>${serviceId}</service-id>
			<service-type>${serviceId}</service-type>
			${serviceEcompModelInformation}			
			<service-instance-id>${svcInstId}</service-instance-id>
			<global-customer-id>${globalSubscriberId}</global-customer-id>
		</service-information>
		<vnf-information>
			<vnf-id>${vnfId}</vnf-id>
			<vnf-type>${vnfType}</vnf-type>
			${vnfEcompModelInformation}			
		</vnf-information>
		<vf-module-information>
			<vf-module-id>${vfModuleId}</vf-module-id>
			<vf-module-type>${vfModuleModelName}</vf-module-type>
			${vfModuleEcompModelInformation}			
		</vf-module-information>
		<vf-module-request-input>			
			<vf-module-name>${vfModuleName}</vf-module-name>
			<tenant>${tenantId}</tenant>
			<aic-cloud-region>${cloudSiteId}</aic-cloud-region>				
		${sdncVNFParamsXml}
		</vf-module-request-input>		
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
			*/
			
		}

	utils.logAudit("sdncRequest:  " + sdncRequest)
	return sdncRequest

	}

	public void preProcessSDNCActivateRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCActivateRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCActivateRequest Process ======== ", isDebugLogEnabled)
		try{
			String vnfId = execution.getVariable("DCVFM_vnfId")
			String vfModuleId = execution.getVariable("DCVFM_vfModuleId")
			String serviceInstanceId = execution.getVariable("DCVFM_serviceInstanceId")

			def svcInstId = ""
			if (serviceInstanceId == null || serviceInstanceId.isEmpty()) {
				svcInstId = vfModuleId
			}
			else {
				svcInstId = serviceInstanceId
			}
			String activateSDNCRequest = buildSDNCRequest(execution, svcInstId, "activate")

			execution.setVariable("DCVFM_activateSDNCRequest", activateSDNCRequest)
			logDebug("Outgoing CommitSDNCRequest is: \n" + activateSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing CommitSDNCRequest is: \n"  + activateSDNCRequest)

		}catch(Exception e){
			log.debug("Exception Occured Processing preProcessSDNCActivateRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED  preProcessSDNCActivateRequest Process ======== ", isDebugLogEnabled)
	}

	public void postProcessVNFAdapterRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.postProcessVNFAdapterRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix",Prefix)
		try{
		logDebug(" *** STARTED postProcessVNFAdapterRequest Process*** ", isDebugLogEnabled)

		String vnfResponse = execution.getVariable("DCVFM_createVnfAResponse")
		logDebug("VNF Adapter Response is: " + vnfResponse, isDebugLogEnabled)
		utils.logAudit("createVnfAResponse is: \n"  + vnfResponse)

		RollbackData rollbackData = execution.getVariable("rollbackData")
		if(vnfResponse != null){

			if(vnfResponse.contains("createVfModuleResponse")){
				logDebug("Received a Good Response from VNF Adapter for CREATE_VF_MODULE Call.", isDebugLogEnabled)
				execution.setVariable("DCVFM_vnfVfModuleCreateCompleted", true)
				String heatStackId = utils.getNodeText1(vnfResponse, "vfModuleStackId")
				execution.setVariable("DCVFM_heatStackId", heatStackId)
				logDebug("Received heat stack id from VNF Adapter: " + heatStackId, isDebugLogEnabled)
				rollbackData.put("VFMODULE", "heatstackid", heatStackId)
				// Parse vnfOutputs for network_fqdn
				if (vnfResponse.contains("vfModuleOutputs")) {
					def vfModuleOutputsXml = utils.getNodeXml(vnfResponse, "vfModuleOutputs")
					InputSource source = new InputSource(new StringReader(vfModuleOutputsXml));
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setNamespaceAware(true)
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
			Document outputsXml = docBuilder.parse(source)

					NodeList entries = outputsXml.getElementsByTagNameNS("*", "entry")
					List contrailNetworkPolicyFqdnList = []
					for (int i = 0; i< entries.getLength(); i++) {
						Node node = entries.item(i)
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element element = (Element) node
							String key = element.getElementsByTagNameNS("*", "key").item(0).getTextContent()
							if (key.equals("contrail-service-instance-fqdn")) {
								String contrailServiceInstanceFqdn = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								logDebug("Obtained contrailServiceInstanceFqdn: " + contrailServiceInstanceFqdn, isDebugLogEnabled)
								execution.setVariable("DCVFM_contrailServiceInstanceFqdn", contrailServiceInstanceFqdn)
							}
							else if (key.endsWith("contrail_network_policy_fqdn")) {
								String contrailNetworkPolicyFqdn = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								logDebug("Obtained contrailNetworkPolicyFqdn: " + contrailNetworkPolicyFqdn, isDebugLogEnabled)
								contrailNetworkPolicyFqdnList.add(contrailNetworkPolicyFqdn)
							}
							else if (key.equals("oam_management_v4_address")) {
								String oamManagementV4Address = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								logDebug("Obtained oamManagementV4Address: " + oamManagementV4Address, isDebugLogEnabled)
								execution.setVariable("DCVFM_oamManagementV4Address", oamManagementV4Address)
							}
							else if (key.equals("oam_management_v6_address")) {
								String oamManagementV6Address = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								logDebug("Obtained oamManagementV6Address: " + oamManagementV6Address, isDebugLogEnabled)
								execution.setVariable("DCVFM_oamManagementV6Address", oamManagementV6Address)
							}

						}
					}
					if (!contrailNetworkPolicyFqdnList.isEmpty()) {
						execution.setVariable("DCVFM_contrailNetworkPolicyFqdnList", contrailNetworkPolicyFqdnList)
					}
				}
			}else{
				logDebug("Received a BAD Response from VNF Adapter for CREATE_VF_MODULE Call.", isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "VNF Adapter Error")
			}
		}else{
			logDebug("Response from VNF Adapter is Null for CREATE_VF_MODULE Call.", isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Empty response from VNF Adapter")
		}

		rollbackData.put("VFMODULE", "rollbackVnfAdapterCreate", "true")
		execution.setVariable("rollbackData", rollbackData)

		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			logDebug("Internal Error Occured in PostProcess Method", isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Internal Error Occured in PostProcess Method")
		}
		logDebug(" *** COMPLETED postProcessVnfAdapterResponse Process*** ", isDebugLogEnabled)
	}


	public void preProcessUpdateAAIVfModuleRequestOrch(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessUpdateAAIVfModuleRequestOrch(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessUpdateAAIVfModuleRequestOrch ======== ", isDebugLogEnabled)

		try{

			//Build UpdateAAIVfModule Request
			boolean setContrailServiceInstanceFqdn = false
			def contrailServiceInstanceFqdn = execution.getVariable("DCVFM_contrailServiceInstanceFqdn")
			if (!contrailServiceInstanceFqdn.equals("")) {
				setContrailServiceInstanceFqdn = true
			}
			
			execution.setVariable("DCVFM_orchestrationStatus", "Created")

			String updateAAIVfModuleRequest = buildUpdateAAIVfModuleRequest(execution, false, true, true, setContrailServiceInstanceFqdn)

			updateAAIVfModuleRequest = utils.formatXml(updateAAIVfModuleRequest)
			execution.setVariable("DCVFM_updateAAIVfModuleRequest", updateAAIVfModuleRequest)
			logDebug("Outgoing UpdateAAIVfModuleRequest is: \n" + updateAAIVfModuleRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing UpdateAAIVfModuleRequest is: \n"  + updateAAIVfModuleRequest)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessUpdateAAIVfModuleRequestOrch. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during preProcessUpdateAAIVfModuleRequestOrch Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessUpdateAAIVfModuleRequestOrch ======== ", isDebugLogEnabled)

	}
	
	public void preProcessUpdateAAIVfModuleRequestStatus(DelegateExecution execution, String status) {
		def method = getClass().getSimpleName() + '.preProcessUpdateAAIVfModuleStatus(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessUpdateAAIVfModuleStatus ======== ", isDebugLogEnabled)

		try{

			//Build UpdateAAIVfModule Request
			execution.setVariable("DCVFM_orchestrationStatus", status)

			String updateAAIVfModuleRequest = buildUpdateAAIVfModuleRequest(execution, false, true, false, false)

			updateAAIVfModuleRequest = utils.formatXml(updateAAIVfModuleRequest)
			execution.setVariable("DCVFM_updateAAIVfModuleRequest", updateAAIVfModuleRequest)
			logDebug("Outgoing UpdateAAIVfModuleRequest is: \n" + updateAAIVfModuleRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing UpdateAAIVfModuleRequest is: \n"  + updateAAIVfModuleRequest)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessUpdateAAIVfModuleStatus. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during preProcessUpdateAAIVfModuleStatus Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessUpdateAAIVfModuleStatus ======== ", isDebugLogEnabled)

	}


	public void preProcessUpdateAAIVfModuleRequestGroup(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessUpdateAAIVfModuleRequestGroup(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessUpdateAAIVfModuleRequestGroup ======== ", isDebugLogEnabled)

		try{

			//Build UpdateAAIVfModule Request

			String updateAAIVfModuleRequest = buildUpdateAAIVfModuleRequest(execution, true, false, false, false)

			updateAAIVfModuleRequest = utils.formatXml(updateAAIVfModuleRequest)
			execution.setVariable("DCVFM_updateAAIVfModuleRequest", updateAAIVfModuleRequest)
			logDebug("Outgoing UpdateAAIVfModuleRequest is: \n" + updateAAIVfModuleRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing UpdateAAIVfModuleRequest is: \n"  + updateAAIVfModuleRequest)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessUpdateAAIVfModuleRequestGroup. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during preProcessUpdateAAIVfModuleRequestGroup Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED  preProcessUpdateAAIVfModuleRequestGroup ======== ", isDebugLogEnabled)

	}

	public void validateSDNCResponse(DelegateExecution execution, String response, String method){
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		logDebug(" *** STARTED ValidateSDNCResponse Process*** ", isDebugLogEnabled)

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		utils.logAudit("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		utils.logAudit("SDNCResponse: " + response)

		String sdncResponse = response
		if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
			logDebug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse, isDebugLogEnabled)
			RollbackData rollbackData = execution.getVariable("rollbackData")

			if(method.equals("assign")){
				rollbackData.put("VFMODULE", "rollbackSDNCRequestAssign", "true")
				execution.setVariable("CRTGVNF_sdncAssignCompleted", true)
			}
			else if (method.equals("activate")) {
				rollbackData.put("VFMODULE", "rollbackSDNCRequestActivate", "true")
			}
			execution.setVariable("rollbackData", rollbackData)
		}else{
			logDebug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.", isDebugLogEnabled)
			throw new BpmnError("MSOWorkflowException")
		}
		logDebug(" *** COMPLETED ValidateSDNCResponse Process*** ", isDebugLogEnabled)
	}

	public void preProcessUpdateAfterCreateRequest(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		utils.log("DEBUG", " ======== STARTED preProcessRequest Process ======== ", isDebugLogEnabled)
		try{
			String response = execution.getVariable("DCVFM_assignSDNCAdapterResponse")
			utils.logAudit("DCVFM_assignSDNCAdapterResponse: " + response)

			String data = utils.getNodeXml(response, "response-data")
			data = data.replaceAll("&lt;", "<")
			data = data.replaceAll("&gt;", ">")
			String vnfId = utils.getNodeText1(data, "vnf-id")

			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
			}

			String serviceOperation = "/VNF-API:vnfs/vnf-list/" + vnfId
			def callbackUrl = execution.getVariable("DCVFM_sdncCallbackUrl")
			utils.logAudit("callbackUrl: " + callbackUrl)

			String SDNCGetRequest =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
											xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
											xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
					<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
					<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
					<sdncadapter:SvcOperation>${serviceOperation}</sdncadapter:SvcOperation>
					<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
					<sdncadapter:MsoAction>mobility</sdncadapter:MsoAction>
				</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData></sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			execution.setVariable("DCVFM_getSDNCRequest", SDNCGetRequest)
			utils.log("DEBUG", "Outgoing GetSDNCRequest is: \n" + SDNCGetRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing GetSDNCRequest: " + SDNCGetRequest)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessSDNCGetRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during prepareProvision Method:\n" + e.getMessage())
		}
		utils.log("DEBUG", "======== COMPLETED preProcessSDNCGetRequest Process ======== ", isDebugLogEnabled)
	}

	public String buildUpdateAAIVfModuleRequest(DelegateExecution execution, boolean updateVolumeGroupId,
		boolean updateOrchestrationStatus, boolean updateHeatStackId, boolean updateContrailFqdn){

		def vnfId = execution.getVariable("DCVFM_vnfId")
		def vfModuleId = execution.getVariable("DCVFM_vfModuleId")
		def volumeGroupIdString = ""
		if (updateVolumeGroupId) {
			volumeGroupIdString = "<volume-group-id>" + execution.getVariable("DCVFM_volumeGroupId") +
					"</volume-group-id>"
		}
		def orchestrationStatusString = ""
		if (updateOrchestrationStatus) {
			orchestrationStatusString = "<orchestration-status>" + execution.getVariable("DCVFM_orchestrationStatus") + "</orchestration-status>"
		}
		def heatStackIdString = ""
		if (updateHeatStackId) {
			heatStackIdString = "<heat-stack-id>" + execution.getVariable("DCVFM_heatStackId") + "</heat-stack-id>"
		}
		def contrailFqdnString = ""
		if (updateContrailFqdn) {
			contrailFqdnString = "<contrail-service-instance-fqdn>" + execution.getVariable("DCVFM_contrailServiceInstanceFqdn") +
				"</contrail-service-instance-fqdn>"
		}

		String updateAAIVfModuleRequest =
			"""<UpdateAAIVfModuleRequest>
				<vnf-id>${vnfId}</vnf-id>
				<vf-module-id>${vfModuleId}</vf-module-id>
				${heatStackIdString}
				${orchestrationStatusString}
				${volumeGroupIdString}
				${contrailFqdnString}
			</UpdateAAIVfModuleRequest>"""

	utils.logAudit("updateAAIVfModule Request: " + updateAAIVfModuleRequest)
	return updateAAIVfModuleRequest

	}

	public String buildSDNCParamsXml(DelegateExecution execution){

		String params = ""
		StringBuilder sb = new StringBuilder()
		Map<String, String> paramsMap = execution.getVariable("DCVFM_vnfParamsMap")

		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			String paramsXml
			String key = entry.getKey();
			if(key.endsWith("_network")){
				String requestKey = key.substring(0, key.indexOf("_network"))
				String requestValue = entry.getValue()
				paramsXml =
"""<vnf-networks>
	<network-role>{ functx:substring-before-match(data($param/@name), '_network') }</network-role>
	<network-name>{ $param/text() }</network-name>
</vnf-networks>"""
			}else{
			paramsXml = ""
			}
			params = sb.append(paramsXml)
		}
		return params
	}

   public void queryCloudRegion (DelegateExecution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		utils.log("DEBUG", " ======== STARTED queryCloudRegion ======== ", isDebugLogEnabled)

		try {
			String cloudRegion = execution.getVariable("DCVFM_cloudSiteId")

			// Prepare AA&I url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUtil = new AaiUtil(this)
			String aai_uri = aaiUtil.getCloudInfrastructureCloudRegionUri(execution)
			String queryCloudRegionRequest = "${aai_endpoint}${aai_uri}/" + cloudRegion
			utils.logAudit("CloudRegion Request: " + queryCloudRegionRequest)

			execution.setVariable("DCVFM_queryCloudRegionRequest", queryCloudRegionRequest)
			utils.log("DEBUG", "DCVFM_queryCloudRegionRequest - " + "\n" + queryCloudRegionRequest, isDebugLogEnabled)

			cloudRegion = aaiUtil.getAAICloudReqion(execution, queryCloudRegionRequest, "PO", cloudRegion)

			if ((cloudRegion != "ERROR")) {
				if(execution.getVariable("DCVFM_queryCloudRegionReturnCode") == "404"){
					execution.setVariable("DCVFM_cloudRegionForVolume", "AAIAIC25")
				}else{
				execution.setVariable("DCVFM_cloudRegionForVolume", cloudRegion)
				}
				execution.setVariable("DCVFM_isCloudRegionGood", true)
			} else {
				String errorMessage = "AAI Query Cloud Region Unsuccessful. AAI Response Code: " + execution.getVariable("DCVFM_queryCloudRegionReturnCode")
				utils.log("DEBUG", errorMessage, isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
				execution.setVariable("DCVFM_isCloudRegionGood", false)
			}
			utils.log("DEBUG", " is Cloud Region Good: " + execution.getVariable("DCVFM_isCloudRegionGood"), isDebugLogEnabled)

		} catch(BpmnError b){
			utils.log("ERROR", "Rethrowing MSOWorkflowException", isDebugLogEnabled)
			throw b
		}catch (Exception ex) {
			// try error
			String errorMessage = "Bpmn error encountered in CreateVfModule flow. Unexpected Response from AAI - " + ex.getMessage()
			utils.log("ERROR", " AAI Query Cloud Region Failed.  Exception - " + "\n" + errorMessage, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception occured during queryCloudRegion method")
		}
	}

   /**
    *
    *This method occurs when an MSOWorkflowException is caught.  It logs the
    *variables and ensures that the "WorkflowException" Variable is set.
    *
    */
   public void processBPMNException(DelegateExecution execution){
	   def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
	   execution.setVariable("prefix",Prefix)
	   try{
		   utils.log("DEBUG", "Caught a BPMN Exception", isDebugEnabled)
		   utils.log("DEBUG", "Started processBPMNException Method", isDebugEnabled)
		   utils.log("DEBUG", "Variables List: " + execution.getVariables(), isDebugEnabled)
		   if(execution.getVariable("WorkflowException") == null){
			   exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception occured during DoCreateVfModule Sub Process")
		   }

	   }catch(Exception e){
		   utils.log("DEBUG", "Caught Exception during processBPMNException Method: " + e, isDebugEnabled)
	   }
	   utils.log("DEBUG", "Completed processBPMNException Method", isDebugEnabled)
   }

   public void prepareCreateAAIVfModuleVolumeGroupRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepareCreateAAIVfModuleVolumeGroupRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED prepareCreateAAIVfModuleVolumeGroupRequest ======== ", isDebugLogEnabled)

		try{

			//Build CreateAAIVfModuleVolumeGroup Request

			def vnfId = execution.getVariable("DCVFM_vnfId")
			def vfModuleId = execution.getVariable("DCVFM_vfModuleId")
			def volumeGroupId = execution.getVariable("DCVFM_volumeGroupId")
			//def aicCloudRegion = execution.getVariable("DCVFM_cloudSiteId")
			def aicCloudRegion = execution.getVariable("DCVFM_cloudRegionForVolume")
			String createAAIVfModuleVolumeGroupRequest =
			"""<CreateAAIVfModuleVolumeGroupRequest>
				<vnf-id>${vnfId}</vnf-id>
				<vf-module-id>${vfModuleId}</vf-module-id>
				<volume-group-id>${volumeGroupId}</volume-group-id>
				<aic-cloud-region>${aicCloudRegion}</aic-cloud-region>
			</CreateAAIVfModuleVolumeGroupRequest>"""

			createAAIVfModuleVolumeGroupRequest = utils.formatXml(createAAIVfModuleVolumeGroupRequest)
			execution.setVariable("DCVFM_createAAIVfModuleVolumeGroupRequest", createAAIVfModuleVolumeGroupRequest)
			logDebug("Outgoing CreateAAIVfModuleVolumeGroupRequest is: \n" + createAAIVfModuleVolumeGroupRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing CreateAAIVfModuleVolumeGroupRequest is: \n"  + createAAIVfModuleVolumeGroupRequest)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing prepareCreateAAIVfModuleVolumeGroupRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during prepareCreateAAIVfModuleVolumeGroupRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED  prepareCreateAAIVfModuleVolumeGroupRequest ======== ", isDebugLogEnabled)

	}

   public void createNetworkPoliciesInAAI(DelegateExecution execution) {
	   def method = getClass().getSimpleName() + '.createNetworkPoliciesInAAI(' +
	   'execution=' + execution.getId() +
	   ')'
	   def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
	   logDebug('Entered ' + method, isDebugLogEnabled)
	   execution.setVariable("prefix", Prefix)
	   logDebug(" ======== STARTED createNetworkPoliciesInAAI ======== ", isDebugLogEnabled)

	   try {
		   // get variables
		   List fqdnList = execution.getVariable("DCVFM_contrailNetworkPolicyFqdnList")
		   int fqdnCount = fqdnList.size()
		   def rollbackData = execution.getVariable("rollbackData")

		   execution.setVariable("DCVFM_networkPolicyFqdnCount", fqdnCount)
		   logDebug("DCVFM_networkPolicyFqdnCount - " + fqdnCount, isDebugLogEnabled)

		   String aai_endpoint = execution.getVariable("URN_aai_endpoint")
		   AaiUtil aaiUriUtil = new AaiUtil(this)
		   String aai_uri = aaiUriUtil.getNetworkPolicyUri(execution)

		   if (fqdnCount > 0) {

			   // AII loop call over contrail network policy fqdn list
			   for (i in 0..fqdnCount-1) {

				   int counting = i+1
				   String fqdn = fqdnList[i]

				   // Query AAI for this network policy FQDN

				   String queryNetworkPolicyByFqdnAAIRequest = "${aai_endpoint}${aai_uri}?network-policy-fqdn=" + UriUtils.encode(fqdn, "UTF-8")
				   utils.logAudit("AAI request endpoint: " + queryNetworkPolicyByFqdnAAIRequest)

				   def aaiRequestId = UUID.randomUUID().toString()
				   RESTConfig config = new RESTConfig(queryNetworkPolicyByFqdnAAIRequest);
				   RESTClient client = new RESTClient(config).addHeader("X-TransactionId", aaiRequestId)
															 .addHeader("X-FromAppId", "MSO")
															 .addHeader("Content-Type", "application/xml")
															 .addHeader("Accept","application/xml");
				   APIResponse response = client.get()
				   int returnCode = response.getStatusCode()
				   execution.setVariable("DCVFM_aaiQqueryNetworkPolicyByFqdnReturnCode", returnCode)
				   logDebug(" ***** AAI query network policy Response Code, NetworkPolicy #" + counting + " : " + returnCode, isDebugLogEnabled)

				   String aaiResponseAsString = response.getResponseBodyAsString()

				   if (isOneOf(returnCode, 200, 201)) {
					   logDebug("The return code is: "  + returnCode, isDebugLogEnabled)
					   // This network policy FQDN already exists in AAI
					   utils.logAudit(aaiResponseAsString)
					   execution.setVariable("DCVFM_queryNetworkPolicyByFqdnAAIResponse", aaiResponseAsString)
					   logDebug(" QueryAAINetworkPolicyByFQDN Success REST Response, , NetworkPolicy #" + counting + " : " + "\n" + aaiResponseAsString, isDebugLogEnabled)

				   } else {
					   if (returnCode == 404) {
						   // This network policy FQDN is not in AAI yet. Add it now
						   logDebug("The return code is: "  + returnCode, isDebugLogEnabled)
						   logDebug("This network policy FQDN is not in AAI yet: " + fqdn, isDebugLogEnabled)
						   utils.logAudit("Network policy FQDN is not in AAI yet")
						   // Add the network policy with this FQDN to AAI
						   def networkPolicyId = UUID.randomUUID().toString()
						   logDebug("Adding network-policy with network-policy-id " + networkPolicyId, isDebugLogEnabled)

						   String aaiNamespace = aaiUriUtil.getNamespaceFromUri(execution, aai_uri)
						   logDebug('AAI namespace is: ' + aaiNamespace, isDebugLogEnabled)
						   String payload = """<network-policy xmlns="${aaiNamespace}">
							   	<network-policy-id>${networkPolicyId}</network-policy-id>
								<network-policy-fqdn>${fqdn}</network-policy-fqdn>
								<heat-stack-id>${execution.getVariable("DCVFM_heatStackId")}</heat-stack-id>
								</network-policy>""" as String

						   execution.setVariable("DCVFM_addNetworkPolicyAAIRequestBody", payload)

						   String addNetworkPolicyAAIRequest = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(networkPolicyId, "UTF-8")
						   utils.logAudit("AAI request endpoint: " + addNetworkPolicyAAIRequest)
						   logDebug("AAI request endpoint: " + addNetworkPolicyAAIRequest, isDebugLogEnabled)

						   def aaiRequestIdPut = UUID.randomUUID().toString()
						   RESTConfig configPut = new RESTConfig(addNetworkPolicyAAIRequest);
						   RESTClient clientPut = new RESTClient(configPut).addHeader("X-TransactionId", aaiRequestIdPut)
																	 .addHeader("X-FromAppId", "MSO")
																	 .addHeader("Content-Type", "application/xml")
																	 .addHeader("Accept","application/xml");
						   logDebug("invoking PUT call to AAI with payload:"+System.lineSeparator()+payload, isDebugLogEnabled)
						   utils.logAudit("Sending PUT call to AAI with Endpoint /n" + addNetworkPolicyAAIRequest + " with payload /n" + payload)
						   APIResponse responsePut = clientPut.httpPut(payload)
						   int returnCodePut = responsePut.getStatusCode()
						   execution.setVariable("DCVFM_aaiAddNetworkPolicyReturnCode", returnCodePut)
						   logDebug(" ***** AAI add network policy Response Code, NetworkPolicy #" + counting + " : " + returnCodePut, isDebugLogEnabled)

						   String aaiResponseAsStringPut = responsePut.getResponseBodyAsString()
						   if (isOneOf(returnCodePut, 200, 201)) {
							   logDebug("The return code from adding network policy is: "  + returnCodePut, isDebugLogEnabled)
							   // This network policy was created in AAI successfully
							   utils.logAudit(aaiResponseAsStringPut)
							   execution.setVariable("DCVFM_addNetworkPolicyAAIResponse", aaiResponseAsStringPut)
							   logDebug(" AddAAINetworkPolicy Success REST Response, , NetworkPolicy #" + counting + " : " + "\n" + aaiResponseAsStringPut, isDebugLogEnabled)
							   rollbackData.put("VFMODULE", "rollbackCreateNetworkPoliciesAAI", "true")
							   rollbackData.put("VFMODULE", "contrailNetworkPolicyFqdn" + i, fqdn)
							   execution.setVariable("rollbackData", rollbackData)

						   } else {
						   		// aai all errors
						   		String putErrorMessage = "Unable to add network-policy to AAI createNetworkPoliciesInAAI - " + returnCodePut
								logDebug(putErrorMessage, isDebugLogEnabled)
								exceptionUtil.buildAndThrowWorkflowException(execution, 2500, putErrorMessage)
						   }

					   } else {
						  if (aaiResponseAsString.contains("RESTFault")) {
							  WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
							  execution.setVariable("WorkflowException", exceptionObject)
							  throw new BpmnError("MSOWorkflowException")

							  } else {
								   // aai all errors
								   String dataErrorMessage = "Unexpected Response from createNetworkPoliciesInAAI - " + returnCode
								   logDebug(dataErrorMessage, isDebugLogEnabled)
								   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

							 }
					   }
				   }

			   } // end loop


		   } else {
		   	   logDebug("No contrail network policies to query/create", isDebugLogEnabled)

		   }

	   } catch (BpmnError e) {
		   throw e;

	   } catch (Exception ex) {
		   String exceptionMessage = "Bpmn error encountered in DoCreateVfModule flow. createNetworkPoliciesInAAI() - " + ex.getMessage()
		   logDebug(exceptionMessage, isDebugLogEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
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
		   def rollbackData = execution.getVariable("rollbackData")
		   def vnfId = execution.getVariable('DCVFM_vnfId')
		   def oamManagementV4Address = execution.getVariable("DCVFM_oamManagementV4Address")
		   def oamManagementV6Address = execution.getVariable("DCVFM_oamManagementV6Address")
		   def ipv4OamAddressElement = ''
		   def managementV6AddressElement = ''

		   if (oamManagementV4Address != null && !oamManagementV4Address.isEmpty()) {
			   ipv4OamAddressElement = '<ipv4-oam-address>' + oamManagementV4Address + '</ipv4-oam-address>'
		   }

		   if (oamManagementV6Address != null && !oamManagementV6Address.isEmpty()) {
			   managementV6AddressElement = '<management-v6-address>' + oamManagementV6Address + '</management-v6-address>'
		   }

		   rollbackData.put("VFMODULE", "oamManagementV4Address", oamManagementV4Address)


		   String updateAAIGenericVnfRequest = """
					<UpdateAAIGenericVnfRequest>
						<vnf-id>${vnfId}</vnf-id>
						${ipv4OamAddressElement}
						${managementV6AddressElement}
					</UpdateAAIGenericVnfRequest>
				"""
			   updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
			   execution.setVariable('DCVM_updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
			   utils.logAudit("updateAAIGenericVnfRequest : " + updateAAIGenericVnfRequest)
			   logDebug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest, isDebugLogEnabled)


		   logDebug('Exited ' + method, isDebugLogEnabled)
	   } catch (BpmnError e) {
		   throw e;
	   } catch (Exception e) {
		   logError('Caught exception in ' + method, e)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepUpdateAAIGenericVnf(): ' + e.getMessage())
	   }
   }

   /**
	* Post process a result from invoking the UpdateAAIGenericVnf subflow.
	*
	* @param execution The flow's execution instance.
	*/
   public void postProcessUpdateAAIGenericVnf(DelegateExecution execution) {
	   def method = getClass().getSimpleName() + '.postProcessUpdateAAIGenericVnf(' +
		   'execution=' + execution.getId() +
		   ')'
	   def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
	   logDebug('Entered ' + method, isDebugLogEnabled)

	   try {
		   def rollbackData = execution.getVariable("rollbackData")

		   rollbackData.put("VFMODULE", "rollbackUpdateVnfAAI", "true")

		   def vnfId = execution.getVariable('DCVFM_vnfId')
		   def oamManagementV4Address = execution.getVariable("DCVFM_oamManagementV4Address")
		   def oamManagementV6Address = execution.getVariable("DCVFM_oamManagementV6Address")
		   def ipv4OamAddressElement = ''
		   def managementV6AddressElement = ''

		   if (oamManagementV4Address != null && !oamManagementV4Address.isEmpty()) {
			   rollbackData.put("VFMODULE", "oamManagementV4Address", oamManagementV4Address)
		   }

		   if (oamManagementV6Address != null && !oamManagementV6Address.isEmpty()) {
			   rollbackData.put("VFMODULE", "oamManagementV6Address", oamManagementV6Address)
		   }

		   execution.setVariable("rollbackData", rollbackData)

		   logDebug('Exited ' + method, isDebugLogEnabled)
	   } catch (BpmnError e) {
		   throw e;
	   } catch (Exception e) {
		   logError('Caught exception in ' + method, e)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in postProcessUpdateAAIGenericVnf(): ' + e.getMessage())
	   }
   }
   
   public void queryCatalogDB (DelegateExecution execution) {
	   def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
	   String msg = ""
	   utils.log("DEBUG"," ***** queryCatalogDB  *****",  isDebugEnabled)

	   try {
		   boolean twoPhaseDesign = false
		   // check for input
		   
		   String vfModuleModelName = execution.getVariable("DCVFM_vfModuleModelName")
		   utils.log("DEBUG", "vfModuleModelName: " + vfModuleModelName, isDebugEnabled)
		   def vnfModelInfo = execution.getVariable("vnfModelInfo")
		   def vnfModelCustomizationUuid = jsonUtil.getJsonValue(vnfModelInfo, "modelCustomizationUuid")
		  
		   utils.log("DEBUG", "vnfModelCustomizationUuid: " + vnfModelCustomizationUuid, isDebugEnabled)		   
		
		   JSONArray vnfs = catalog.getAllVnfsByVnfModelCustomizationUuid(execution, vnfModelCustomizationUuid, "v2")
		   
		   utils.log("DEBUG", "Incoming Query Catalog DB for Vnf Response is: " + vnfModelCustomizationUuid, isDebugEnabled)
		   utils.logAudit("Incoming Query Catalog DB for Vf Module Response is: " + vnfModelCustomizationUuid)
		   
		   utils.log("DEBUG", "obtained VNF list")
		   // Only one match here
		   if (vnfs != null) {
			   JSONObject vnfObject = vnfs.get(0)
			   if (vnfObject != null) {
				   String vnfJson = vnfObject.toString()
				   //
				   ObjectMapper om = new ObjectMapper();			  
				   VnfResource vnf = om.readValue(vnfJson, VnfResource.class);
		   
				   // Get multiStageDesign flag
		   
				   String multiStageDesignValue = vnf.getMultiStageDesign()
				   utils.log("DEBUG", "multiStageDesign value from Catalog DB is: " + multiStageDesignValue, isDebugEnabled)
				   if (multiStageDesignValue != null) {
					   if (multiStageDesignValue.equalsIgnoreCase("true")) {
			   				twoPhaseDesign = true
					   }
				   }
			   }
		   }
		   
		   utils.log("DEBUG", "setting twoPhaseDesign flag to: " + twoPhaseDesign, isDebugEnabled)
		   
		   execution.setVariable("DCVFM_twoPhaseDesign", twoPhaseDesign)		
	   } catch (BpmnError e) {
		   throw e;
	   } catch (Exception e) {
		   logError('Caught exception in queryCatalogDB()', e)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryCatalogDB(): ' + e.getMessage())
	   }
   }
	
   
   public void preProcessRollback (DelegateExecution execution) {
	   def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
	   utils.log("DEBUG"," ***** preProcessRollback ***** ", isDebugEnabled)
	   try {
		   
		   Object workflowException = execution.getVariable("WorkflowException");

		   if (workflowException instanceof WorkflowException) {
			   utils.log("DEBUG", "Prev workflowException: " + workflowException.getErrorMessage(), isDebugEnabled)
			   execution.setVariable("prevWorkflowException", workflowException);
			   //execution.setVariable("WorkflowException", null);
		   }
	   } catch (BpmnError e) {
		   utils.log("DEBUG", "BPMN Error during preProcessRollback", isDebugEnabled)
	   } catch(Exception ex) {
		   String msg = "Exception in preProcessRollback. " + ex.getMessage()
		   utils.log("DEBUG", msg, isDebugEnabled)
	   }
	   utils.log("DEBUG"," *** Exit preProcessRollback *** ", isDebugEnabled)
   }

   public void postProcessRollback (DelegateExecution execution) {
	   def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
	   utils.log("DEBUG"," ***** postProcessRollback ***** ", isDebugEnabled)
	   String msg = ""
	   try {
		   Object workflowException = execution.getVariable("prevWorkflowException");
		   if (workflowException instanceof WorkflowException) {
			   utils.log("DEBUG", "Setting prevException to WorkflowException: ", isDebugEnabled)
			   execution.setVariable("WorkflowException", workflowException);
		   }
		   execution.setVariable("rollbackData", null)
	   } catch (BpmnError b) {
		   utils.log("DEBUG", "BPMN Error during postProcessRollback", isDebugEnabled)
		   throw b;
	   } catch(Exception ex) {
		   msg = "Exception in postProcessRollback. " + ex.getMessage()
		   utils.log("DEBUG", msg, isDebugEnabled)
	   }
	   utils.log("DEBUG"," *** Exit postProcessRollback *** ", isDebugEnabled)
   }

}
