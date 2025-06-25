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

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
import org.onap.logging.filter.base.ErrorCode
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.NetworkUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.common.scripts.VfModuleBase
import org.onap.so.bpmn.common.util.OofInfraUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.DecomposeJsonUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.constants.Defaults
import org.onap.so.db.catalog.beans.HomingInstance
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import com.fasterxml.jackson.databind.ObjectMapper



public class DoCreateVfModule extends VfModuleBase {
  private static final Logger logger = LoggerFactory.getLogger( DoCreateVfModule.class)
	private static final ObjectMapper mapper = new ObjectMapper()
	private final HttpClientFactory httpClientFactory = new HttpClientFactory()

	String Prefix="DCVFM_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
	OofInfraUtils oofInfraUtils = new OofInfraUtils()
	CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()
	DecomposeJsonUtil decomposeJsonUtils = new DecomposeJsonUtil()

	/**
	 * Validates the request message and sets up the workflow.
	 * @param execution the execution
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

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
				logger.debug("serviceModelInfo: " + serviceModelInfo)
				String modelInvariantUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelInvariantUuid")
				logger.debug("modelInvariantUuid: " + modelInvariantUuid)

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
				logger.debug("cloudSiteId: " + cloudSiteId)
				//cloudOwner
				def cloudOwner = execution.getVariable("cloudOwner")
				execution.setVariable("DCVFM_cloudOwner", cloudOwner)
				rollbackData.put("VFMODULE", "cloudOwner", cloudOwner)
				logger.debug("cloudOwner: " + cloudOwner)
				//vnfType
				def vnfType = execution.getVariable("vnfType")
				execution.setVariable("DCVFM_vnfType", vnfType)
				rollbackData.put("VFMODULE", "vnftype", vnfType)
				logger.debug("vnfType: " + vnfType)
				//vnfName
				def vnfName = execution.getVariable("vnfName")
				execution.setVariable("DCVFM_vnfName", vnfName)
				rollbackData.put("VFMODULE", "vnfname", vnfName)
				logger.debug("vnfName: " + vnfName)
				//vnfId
				def vnfId = execution.getVariable("vnfId")
				execution.setVariable("DCVFM_vnfId", vnfId)
				rollbackData.put("VFMODULE", "vnfid", vnfId)
				logger.debug("vnfId: " + vnfId)
				//vfModuleName
				def vfModuleName = execution.getVariable("vfModuleName")
				execution.setVariable("DCVFM_vfModuleName", vfModuleName)
				rollbackData.put("VFMODULE", "vfmodulename", vfModuleName)
				logger.debug("vfModuleName: " + vfModuleName)
				//vfModuleModelName
				def vfModuleModelName = jsonUtil.getJsonValue(vfModuleModelInfo, "modelName")
				execution.setVariable("DCVFM_vfModuleModelName", vfModuleModelName)
				rollbackData.put("VFMODULE", "vfmodulemodelname", vfModuleModelName)
				logger.debug("vfModuleModelName: " + vfModuleModelName)
				//modelCustomizationUuid
				def modelCustomizationUuid = jsonUtil.getJsonValue(vfModuleModelInfo, "modelCustomizationUuid")
				execution.setVariable("DCVFM_modelCustomizationUuid", modelCustomizationUuid)
				rollbackData.put("VFMODULE", "modelcustomizationuuid", modelCustomizationUuid)
				logger.debug("modelCustomizationUuid: " + modelCustomizationUuid)
				//vfModuleId
				def vfModuleId = execution.getVariable("vfModuleId")
				execution.setVariable("DCVFM_vfModuleId", vfModuleId)
				logger.debug("vfModuleId: " + vfModuleId)
				def requestId = execution.getVariable("msoRequestId")
				execution.setVariable("DCVFM_requestId", requestId)
				logger.debug("requestId: " + requestId)
				rollbackData.put("VFMODULE", "msorequestid", requestId)
				// Set mso-request-id to request-id for VNF Adapter interface
				execution.setVariable("mso-request-id", requestId)
				//serviceId
				def serviceId = execution.getVariable("serviceId")
				execution.setVariable("DCVFM_serviceId", serviceId)
				logger.debug("serviceId: " + serviceId)
				//serviceInstanceId
				def serviceInstanceId = execution.getVariable("serviceInstanceId")
				execution.setVariable("DCVFM_serviceInstanceId", serviceInstanceId)
				rollbackData.put("VFMODULE", "serviceInstanceId", serviceInstanceId)
				logger.debug("serviceInstanceId: " + serviceInstanceId)
				//source - HARDCODED
				def source = "VID"
				execution.setVariable("DCVFM_source", source)
				rollbackData.put("VFMODULE", "source", source)
				logger.debug("source: " + source)
				//backoutOnFailure
				def disableRollback = execution.getVariable("disableRollback")
				def backoutOnFailure = true
				if (disableRollback != null && disableRollback == true) {
					backoutOnFailure = false
				}
				execution.setVariable("DCVFM_backoutOnFailure", backoutOnFailure)
				logger.debug("backoutOnFailure: " + backoutOnFailure)
				//isBaseVfModule
				def isBaseVfModule = execution.getVariable("isBaseVfModule")
				execution.setVariable("DCVFM_isBaseVfModule", isBaseVfModule)
				logger.debug("isBaseVfModule: " + isBaseVfModule)
				//asdcServiceModelVersion
				def asdcServiceModelVersion = execution.getVariable("asdcServiceModelVersion")
				execution.setVariable("DCVFM_asdcServiceModelVersion", asdcServiceModelVersion)
				logger.debug("asdcServiceModelVersion: " + asdcServiceModelVersion)
				//personaModelId
				execution.setVariable("DCVFM_personaModelId", jsonUtil.getJsonValue(vfModuleModelInfo, "modelInvariantUuid"))
				//personaModelVersion
				execution.setVariable("DCVFM_personaModelVersion", jsonUtil.getJsonValue(vfModuleModelInfo, "modelUuid"))
				//vfModuleLabel
				def vfModuleLabel = execution.getVariable("vfModuleLabel")
				if (vfModuleLabel != null) {
					execution.setVariable("DCVFM_vfModuleLabel", vfModuleLabel)
					logger.debug("vfModuleLabel: " + vfModuleLabel)
				}
				//Get or Generate UUID
				String uuid = execution.getVariable("DCVFM_uuid")
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
				//globalSubscriberId
				String globalSubscriberId = execution.getVariable("globalSubscriberId")
				execution.setVariable("DCVFM_globalSubscriberId", globalSubscriberId)
				logger.debug("globalSubsrciberId: " + globalSubscriberId)

				// Set Homing Info
				String oofDirectives = null
				try {
					HomingInstance homingInstance = oofInfraUtils.getHomingInstance(serviceInstanceId, execution)
					if (homingInstance != null) {
						execution.setVariable("DCVFM_cloudSiteId", homingInstance.getCloudRegionId())
						rollbackData.put("VFMODULE", "aiccloudregion", homingInstance.getCloudRegionId())
						logger.debug("Overwriting cloudSiteId with homing cloudSiteId: " +
								homingInstance.getCloudRegionId())
						execution.setVariable("DCVFM_cloudOwner", homingInstance.getCloudOwner())
						rollbackData.put("VFMODULE", "cloudOwner", homingInstance.getCloudOwner())
						logger.debug("Overwriting cloudOwner with homing cloudOwner: " +
								homingInstance.getCloudOwner())
						oofDirectives = homingInstance.getOofDirectives()
						execution.setVariable("DCVFM_oofDirectives", oofDirectives)
					}
				} catch (Exception exception) {
					logger.debug("Could not find homing information for service instance: " + serviceInstanceId +
							"... continuing")
					logger.debug("Could not find homing information for service instance error: " + exception)
				}
				//OofDirectives to Input Params
				Map<String,String> vfModuleInputParams = execution.getVariable("vfModuleInputParams")
				if (oofDirectives != null && vfModuleInputParams != null) {
					vfModuleInputParams.put("oof_directives", oofDirectives)
					//vfModuleInputParams.put("sdnc_directives", "{}")
					logger.debug("OofDirectives are: " + oofDirectives)
				} else if (vfModuleInputParams != null) {
					vfModuleInputParams.put("oof_directives", "{}")
					//vfModuleInputParams.put("sdnc_directives", "{}")
				}
				if (vfModuleInputParams != null) {
					execution.setVariable("DCVFM_vnfParamsMap", vfModuleInputParams)
					execution.setVariable("DCVFM_vnfParamsExistFlag", true)
				}
				//usePreload
				def usePreload = execution.getVariable("usePreload")
				execution.setVariable("DCVFM_usePreload", usePreload)
				logger.debug("usePreload: " + usePreload)
				//aLaCarte
				def aLaCarte = execution.getVariable("aLaCarte")
				execution.setVariable("DCVFM_aLaCarte", aLaCarte)
				logger.debug("aLaCarte: " + aLaCarte)

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
				// The info is inside the request - DEAD CODE
				logger.debug("DoCreateVfModule request: " + request)

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
				logger.debug("cloudSiteId: " + cloudSiteId)
				//vnfType
				def vnfType = ""
				if (utils.nodeExists(request, "vnf-type")) {
					vnfType = utils.getNodeText(request, "vnf-type")
				}
				execution.setVariable("DCVFM_vnfType", vnfType)
				rollbackData.put("VFMODULE", "vnftype", vnfType)
				logger.debug("vnfType: " + vnfType)
				//vnfName
				def vnfName = ""
				if (utils.nodeExists(request, "vnf-name")) {
					vnfName = utils.getNodeText(request, "vnf-name")
				}
				execution.setVariable("DCVFM_vnfName", vnfName)
				rollbackData.put("VFMODULE", "vnfname", vnfName)
				logger.debug("vnfName: " + vnfName)
				//vnfId
				def vnfId = ""
				if (utils.nodeExists(request, "vnf-id")) {
					vnfId = utils.getNodeText(request, "vnf-id")
				}
				execution.setVariable("DCVFM_vnfId", vnfId)
				rollbackData.put("VFMODULE", "vnfid", vnfId)
				logger.debug("vnfId: " + vnfId)
				//vfModuleName
				def vfModuleName = ""
				if (utils.nodeExists(request, "vf-module-name")) {
					vfModuleName = utils.getNodeText(request, "vf-module-name")
				}
				execution.setVariable("DCVFM_vfModuleName", vfModuleName)
				rollbackData.put("VFMODULE", "vfmodulename", vfModuleName)
				logger.debug("vfModuleName: " + vfModuleName)
				//vfModuleModelName
				def vfModuleModelName = ""
				if (utils.nodeExists(request, "vf-module-model-name")) {
					vfModuleModelName = utils.getNodeText(request, "vf-module-model-name")
				}
				execution.setVariable("DCVFM_vfModuleModelName", vfModuleModelName)
				rollbackData.put("VFMODULE", "vfmodulemodelname", vfModuleModelName)
				logger.debug("vfModuleModelName: " + vfModuleModelName)
				//modelCustomizationUuid
				def modelCustomizationUuid = ""
				if (utils.nodeExists(request, "model-customization-id")) {
					modelCustomizationUuid = utils.getNodeText(request, "model-customization-id")
				}
				execution.setVariable("DCVFM_modelCustomizationUuid", modelCustomizationUuid)
				rollbackData.put("VFMODULE", "modelcustomizationuuid", modelCustomizationUuid)
				logger.debug("modelCustomizationUuid: " + modelCustomizationUuid)
				//vfModuleId
				def vfModuleId = ""
				if (utils.nodeExists(request, "vf-module-id")) {
					vfModuleId = utils.getNodeText(request, "vf-module-id")
				}
				execution.setVariable("DCVFM_vfModuleId", vfModuleId)
				logger.debug("vfModuleId: " + vfModuleId)
				def requestId = ""
				if (utils.nodeExists(request, "request-id")) {
					requestId = utils.getNodeText(request, "request-id")
				}
				execution.setVariable("DCVFM_requestId", requestId)
				logger.debug("requestId: " + requestId)
				//serviceId
				def serviceId = ""
				if (utils.nodeExists(request, "service-id")) {
					serviceId = utils.getNodeText(request, "service-id")
				}
				execution.setVariable("DCVFM_serviceId", serviceId)
				logger.debug("serviceId: " + serviceId)
				//serviceInstanceId
				def serviceInstanceId = ""
				if (utils.nodeExists(request, "service-instance-id")) {
					serviceInstanceId = utils.getNodeText(request, "service-instance-id")
				}
				execution.setVariable("DCVFM_serviceInstanceId", serviceInstanceId)
				rollbackData.put("VFMODULE", "serviceInstanceId", serviceInstanceId)
				logger.debug("serviceInstanceId: " + serviceInstanceId)
				//source
				def source = ""
				if (utils.nodeExists(request, "source")) {
					source = utils.getNodeText(request, "source")
				}
				execution.setVariable("DCVFM_source", source)
				rollbackData.put("VFMODULE", "source", source)
				logger.debug("source: " + source)
				//backoutOnFailure
				NetworkUtils networkUtils = new NetworkUtils()
				def backoutOnFailure = networkUtils.isRollbackEnabled(execution,request)
				execution.setVariable("DCVFM_backoutOnFailure", backoutOnFailure)
				logger.debug("backoutOnFailure: " + backoutOnFailure)
				//isBaseVfModule
				def isBaseVfModule = "false"
				if (utils.nodeExists(request, "is-base-vf-module")) {
					isBaseVfModule = utils.getNodeText(request, "is-base-vf-module")
				}
				execution.setVariable("DCVFM_isBaseVfModule", isBaseVfModule)
				logger.debug("isBaseVfModule: " + isBaseVfModule)
				//asdcServiceModelVersion
				def asdcServiceModelVersion = ""
				if (utils.nodeExists(request, "asdc-service-model-version")) {
					asdcServiceModelVersion = utils.getNodeText(request, "asdc-service-model-version")
				}
				execution.setVariable("DCVFM_asdcServiceModelVersion", asdcServiceModelVersion)
				logger.debug("asdcServiceModelVersion: " + asdcServiceModelVersion)

				//personaModelId
				def personaModelId = ""
				if (utils.nodeExists(request, "persona-model-id")) {
					personaModelId = utils.getNodeText(request, "persona-model-id")
				}
				execution.setVariable("DCVFM_personaModelId", personaModelId)
				logger.debug("personaModelId: " + personaModelId)

				//personaModelVersion
				def personaModelVersion = ""
				if (utils.nodeExists(request, "persona-model-version")) {
					personaModelVersion = utils.getNodeText(request, "persona-model-version")
				}
				execution.setVariable("DCVFM_personaModelVersion", personaModelVersion)
				logger.debug("personaModelVersion: " + personaModelVersion)

				// Process the parameters

						String vnfParamsChildNodes = utils.getChildNodes(request, "vnf-params")
						if(vnfParamsChildNodes == null || vnfParamsChildNodes.length() < 1){
								logger.debug("Request contains NO VNF Params")
						}else{
								logger.debug("Request does contain VNF Params")
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

				//OofDirectives
				String oofDirectives = null
				try {
					HomingInstance homingInstance = oofInfraUtils.getHomingInstance(serviceInstanceId, execution)
					if (homingInstance != null) {
						execution.setVariable("DCVFM_cloudSiteId", homingInstance.getCloudRegionId())
						rollbackData.put("VFMODULE", "aiccloudregion", homingInstance.getCloudRegionId())
						logger.debug("Overwriting cloudSiteId with homing cloudSiteId: " +
								homingInstance.getCloudRegionId())
						execution.setVariable("DCVFM_cloudOwner", homingInstance.getCloudOwner())
						rollbackData.put("VFMODULE", "cloudOwner", homingInstance.getCloudOwner())
						logger.debug("Overwriting cloudOwner with homing cloudOwner: " +
								homingInstance.getCloudOwner())
						oofDirectives = homingInstance.getOofDirectives()
						execution.setVariable("DCVFM_oofDirectives", oofDirectives)
					}
				} catch (Exception exception) {
					logger.debug("Could not find homing information for service instance: " + serviceInstanceId +
							"... continuing")
					logger.debug("Could not find homing information for service instance error: " + exception)
				}
				if (oofDirectives != null) {
					Map<String, String> paramsMap = execution.getVariable("DCVFM_vnfParamsMap")
					paramsMap.put("oofDirectives", oofDirectives)
					logger.debug("OofDirectives are: " + oofDirectives)
					execution.setVariable("DCVFM_vnfParamsMap", paramsMap)
				}
			}


			//Get or Generate UUID
			String uuid = execution.getVariable("DCVFM_uuid")
			if(uuid == null){
				uuid = UUID.randomUUID()
				logger.debug("Generated messageId (UUID) is: " + uuid)
			}else{
				logger.debug("Found messageId (UUID) is: " + uuid)
			}
			// Get sdncVersion, default to empty
			String sdncVersion = execution.getVariable("sdncVersion")
			if (sdncVersion == null) {
				sdncVersion = ""
			}
			logger.debug("sdncVersion: " + sdncVersion)
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

			String sdncCallbackUrl = (String) UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
				if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
					def msg = 'Required variable \'mso.workflow.sdncadapter.callback\' is missing'
					logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
							ErrorCode.UnknownError.getValue(), msg);

					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}
				execution.setVariable("DCVFM_sdncCallbackUrl", sdncCallbackUrl)
			    logger.debug("SDNC Callback URL is: " + sdncCallbackUrl)


			execution.setVariable("rollbackData", rollbackData)
		}catch(BpmnError b){
            logger.error(b);
			throw b
		}catch(Exception e){
            logger.error(e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error encountered in PreProcess method!")
		}

		logger.trace('Exited ' + method)
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
		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
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

		logger.trace('Entered ' + method)

		try {
			sendWorkflowResponse(execution, 200, "")
			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);

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

		logger.trace('Entered ' + method)

		try {
			def createResponse = execution.getVariable('DCVFM_createVfModuleResponse')
			logger.debug("createVfModule Response: " + createResponse)

			def rollbackData = execution.getVariable("rollbackData")
			String vnfName = utils.getNodeText(createResponse, 'vnf-name')
			if (vnfName != null) {
				execution.setVariable('DCVFM_vnfName', vnfName)
				logger.debug("vnfName retrieved from AAI is: " + vnfName)
				rollbackData.put("VFMODULE", "vnfname", vnfName)
			}
			String vnfId = utils.getNodeText(createResponse, 'vnf-id')
			execution.setVariable('DCVFM_vnfId', vnfId)
			logger.debug("vnfId is: " + vnfId)
			String vfModuleId = utils.getNodeText(createResponse, 'vf-module-id')
			execution.setVariable('DCVFM_vfModuleId', vfModuleId)
			logger.debug("vfModuleId is: " + vfModuleId)
			String vfModuleIndex= utils.getNodeText(createResponse, 'vf-module-index')
			execution.setVariable('DCVFM_vfModuleIndex', vfModuleIndex)
			logger.debug("vfModuleIndex is: " + vfModuleIndex)
			rollbackData.put("VFMODULE", "vnfid", vnfId)
			rollbackData.put("VFMODULE", "vfmoduleid", vfModuleId)
			rollbackData.put("VFMODULE", "rollbackCreateAAIVfModule", "true")
			rollbackData.put("VFMODULE", "rollbackPrepareUpdateVfModule", "true")
			execution.setVariable("rollbackData", rollbackData)
		} catch (Exception ex) {
				logger.debug('Exception occurred while postProcessing CreateAAIVfModule request: {}', ex.getMessage(), ex)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Bad response from CreateAAIVfModule' + ex.getMessage())
		}
		logger.trace('Exited ' + method)
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
			def vnfId = execution.getVariable('DCVFM_vnfId')
			def vfModuleId = execution.getVariable('DCVFM_vfModuleId')

			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)).depth(Depth.ONE)
			String endPoint = aaiUriUtil.createAaiUri(uri)

			try {
				HttpClient client = httpClientFactory.newXmlClient(new URL(endPoint), ONAPComponents.AAI)
				client.addAdditionalHeader('X-TransactionId', UUID.randomUUID().toString())
				client.addAdditionalHeader('X-FromAppId', 'MSO')
				client.addAdditionalHeader('Content-Type', MediaType.APPLICATION_XML)
				client.addAdditionalHeader('Accept', MediaType.APPLICATION_XML)

				logger.debug('sending GET to AAI endpoint \'' + endPoint + '\'')
				Response response = client.get()

				String responseData = response.readEntity(String.class)
				if (responseData != null) {
					logger.debug("Received generic VNF data: " + responseData)

				}

				execution.setVariable('DCVFM_queryAAIVfModuleResponseCode', response.getStatus())
				execution.setVariable('DCVFM_queryAAIVfModuleResponse', responseData)
				logger.debug('Response code:' + response.getStatus())
				logger.debug('Response:' + System.lineSeparator() + responseData)
				if (response.getStatus() == 200) {
					// Parse the VNF record from A&AI to find base module info
					logger.debug('Parsing the VNF data to find base module info')
					if (responseData != null) {
						def vfModulesText = utils.getNodeXml(responseData, "vf-modules")
						def xmlVfModules= new XmlSlurper().parseText(vfModulesText)
						def vfModules = xmlVfModules.'**'.findAll {it.name() == "vf-module"}
						int vfModulesSize = 0
						for (i in 0..vfModules.size()-1) {
							def vfModuleXml = groovy.xml.XmlUtil.serialize(vfModules[i])
							def isBaseVfModule = utils.getNodeText(vfModuleXml, "is-base-vf-module")

							if (isBaseVfModule == "true") {
							    String baseModuleId = utils.getNodeText(vfModuleXml, "vf-module-id")
							    execution.setVariable("DCVFM_baseVfModuleId", baseModuleId)
							    logger.debug('Received baseVfModuleId: ' + baseModuleId)
							    String baseModuleHeatStackId = utils.getNodeText(vfModuleXml, "heat-stack-id")
							    execution.setVariable("DCVFM_baseVfModuleHeatStackId", baseModuleHeatStackId)
							    logger.debug('Received baseVfModuleHeatStackId: ' + baseModuleHeatStackId)
							}
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
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
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

		def method = getClass().getSimpleName() + '.queryAAIVfModuleForStatus(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		execution.setVariable('DCVFM_orchestrationStatus', '')

		try {
			def vnfId = execution.getVariable('DCVFM_vnfId')
			def vfModuleName = execution.getVariable('DCVFM_vfModuleName')

			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModules()).queryParam("vf-module-name",vfModuleName)
			String endPoint = aaiUriUtil.createAaiUri(uri)

			HttpClient client = httpClientFactory.newXmlClient(new URL(endPoint), ONAPComponents.AAI)
			client.addAdditionalHeader('X-TransactionId', UUID.randomUUID().toString())
			client.addAdditionalHeader('X-FromAppId', 'MSO')
			client.addAdditionalHeader('Content-Type', MediaType.APPLICATION_XML)
			client.addAdditionalHeader('Accept', MediaType.APPLICATION_XML)

			try {
				def responseData = ''

				logger.debug('sending GET to AAI endpoint \'' + endPoint + '\'')
				Response response = client.get()
				logger.debug("createVfModule - invoking httpGet() to AAI")

				responseData = response.readEntity(String.class)
				if (responseData != null) {
					logger.debug("Received generic VNF data: " + responseData)

				}

				execution.setVariable('DCVFM_queryAAIVfModuleForStatusResponseCode', response.getStatus())
				execution.setVariable('DCVFM_queryAAIVfModuleForStatusResponse', responseData)
				logger.debug('Response code:' + response.getStatus())
				logger.debug('Response:' + System.lineSeparator() + responseData)
				// Retrieve VF Module info and its orchestration status; if not found, do nothing
				if (response.getStatus() == 200) {
					// Parse the VNF record from A&AI to find base module info
					logger.debug('Parsing the VNF data to find orchestration status')
					if (responseData != null) {
						def vfModuleText = utils.getNodeXml(responseData, "vf-module")
						//def xmlVfModule= new XmlSlurper().parseText(vfModuleText)
						def orchestrationStatus = utils.getNodeText(vfModuleText, "orchestration-status")
						execution.setVariable("DCVFM_orchestrationStatus", orchestrationStatus)
						// Also retrieve vfModuleId
						def vfModuleId = utils.getNodeText(vfModuleText, "vf-module-id")
						execution.setVariable("DCVFM_vfModuleId", vfModuleId)
						logger.debug("Received orchestration status from A&AI: " + orchestrationStatus)

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
			logger.error(LoggingAnchor.SIX, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e, e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModuleForStatus(): ' + e.getMessage())
		}
	}


	public void preProcessSDNCAssignRequest(DelegateExecution execution){

		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCAssignRequest")
		def vnfId = execution.getVariable("DCVFM_vnfId")
		def vfModuleId = execution.getVariable("DCVFM_vfModuleId")
		def serviceInstanceId = execution.getVariable("DCVFM_serviceInstanceId")
		logger.debug("NEW VNF ID: " + vnfId)

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
			logger.debug("Outgoing AssignSDNCRequest is: \n" + assignSDNCRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occurred Processing preProcessSDNCAssignRequest", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during prepareProvision Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessSDNCAssignRequest")
	}

	public void preProcessSDNCGetRequest(DelegateExecution execution, String element){

		String sdncVersion = execution.getVariable("DCVFM_sdncVersion")
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCGetRequest Process")
		try{
			def serviceInstanceId = execution.getVariable('DCVFM_serviceInstanceId')

			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("mso-request-id") + "-" +  System.currentTimeMillis()
			}

			def callbackUrl = execution.getVariable("DCVFM_sdncCallbackUrl")
			logger.debug("callbackUrl:" + callbackUrl)

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
			// For VF Module, in 1707 serviceOperation will be retrieved from "object-path" element
			// in SDNC Assign Response
			// For VF Module for older versions, serviceOperation is constructed using vfModuleId

			String serviceOperation = ""
			if (element.equals("vnf")) {
				AAIResourcesClient resourceClient = new AAIResourcesClient()
				AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(execution.getVariable('DCVFM_vnfId')))
				AAIResultWrapper wrapper = resourceClient.get(uri)

				Optional<GenericVnf> vnf = wrapper.asBean(GenericVnf.class)
				serviceOperation = vnf.get().getSelflink()
				logger.debug("VNF - service operation: " + serviceOperation)
			}
			else if (element.equals("vfmodule")) {
				String response = execution.getVariable("DCVFM_assignSDNCAdapterResponse")
				logger.debug("DCVFM_assignSDNCAdapterResponse is: \n" + response)

				if (!sdncVersion.equals("1707")) {
					serviceOperation = "/VNF-API:vnfs/vnf-list/" + vfModuleId
					logger.debug("VF Module with sdncVersion before 1707 - service operation: " + serviceOperation)
				}
				else {
					String data = utils.getNodeXml(response, "response-data")
					logger.debug("responseData: " + data)
					serviceOperation = utils.getNodeText(data, "object-path")
					logger.debug("VF Module with sdncVersion of 1707 - service operation: " + serviceOperation)
				}
			}

			//!!!! TEMPORARY WORKAROUND FOR SDNC REPLICATION ISSUE
			sleep(5000)

			String SDNCGetRequest =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
											xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
											xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
					<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
					<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
					<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
					<sdncadapter:SvcOperation>${MsoUtils.xmlEscape(serviceOperation)}</sdncadapter:SvcOperation>
					<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
					<sdncadapter:MsoAction>${MsoUtils.xmlEscape(msoAction)}</sdncadapter:MsoAction>
				</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData></sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			execution.setVariable("DCVFM_getSDNCRequest", SDNCGetRequest)
			logger.debug("Outgoing GetSDNCRequest is: \n" + SDNCGetRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occurred Processing preProcessSDNCGetRequest", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during prepareProvision Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessSDNCGetRequest Process")
	}


	public void preProcessVNFAdapterRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.VNFAdapterCreateVfModule(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

		//def xml = execution.getVariable("DoCreateVfModuleRequest")
		//logger.debug('VNF REQUEST is: ' + xml)

		//Get variables
		//cloudSiteId
		def cloudSiteId = execution.getVariable("DCVFM_cloudSiteId")
		//cloudOwner
		def cloudOwner = execution.getVariable("DCVFM_cloudOwner")
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
		logger.debug("workloadContext: " + workloadContext)
		logger.debug("environmentContext: " + environmentContext)

		def messageId = execution.getVariable('mso-request-id') + '-' +
                                System.currentTimeMillis()

		def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
		def useQualifiedHostName = UrnPropertiesReader.getVariable("mso.use.qualified.host",execution)

		logger.debug("notificationUrl: " + notificationUrl)
		logger.debug("QualifiedHostName: " + useQualifiedHostName)

		if ('true'.equals(useQualifiedHostName)) {
			notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}

		Map<String, String> vnfParamsMap = execution.getVariable("DCVFM_vnfParamsMap")
		String vfModuleParams = ""
		//Get SDNC Response Data for VF Module Topology
		String vfModuleSdncGetResponse = execution.getVariable('DCVFM_getSDNCAdapterResponse')
		logger.debug("sdncGetResponse: " + vfModuleSdncGetResponse)
		def sdncVersion = execution.getVariable("sdncVersion")

		if (!sdncVersion.equals("1707")) {

			vfModuleParams = buildVfModuleParams(vnfParamsMap, vfModuleSdncGetResponse, vnfId, vnfName,
				vfModuleId, vfModuleName, vfModuleIndex, environmentContext, workloadContext)
		}
		else {
			//Get SDNC Response Data for Vnf Topology
			String vnfSdncGetResponse = execution.getVariable('DCVFM_getVnfSDNCAdapterResponse')
			logger.debug("vnfSdncGetResponse: " + vnfSdncGetResponse)

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

		String createVnfARequest = """
		<createVfModuleRequest>
		<cloudSiteId>${MsoUtils.xmlEscape(cloudSiteId)}</cloudSiteId>
		<cloudOwner>${MsoUtils.xmlEscape(cloudOwner)}</cloudOwner>
		<tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
		<vnfId>${MsoUtils.xmlEscape(vnfId)}</vnfId>
		<vnfName>${MsoUtils.xmlEscape(vnfName)}</vnfName>
		<vfModuleName>${MsoUtils.xmlEscape(vfModuleName)}</vfModuleName>
		<vfModuleId>${MsoUtils.xmlEscape(vfModuleId)}</vfModuleId>
		<vnfType>${MsoUtils.xmlEscape(vnfType)}</vnfType>
		<vfModuleType>${MsoUtils.xmlEscape(vfModuleModelName)}</vfModuleType>
		<vnfVersion>${MsoUtils.xmlEscape(asdcServiceModelVersion)}</vnfVersion>
		<modelCustomizationUuid>${MsoUtils.xmlEscape(modelCustomizationUuid)}</modelCustomizationUuid>
		<requestType></requestType>
		<volumeGroupId>${MsoUtils.xmlEscape(volumeGroupId)}</volumeGroupId>
    	<volumeGroupStackId>${MsoUtils.xmlEscape(volumeGroupStackId)}</volumeGroupStackId>
    	<baseVfModuleId>${MsoUtils.xmlEscape(baseVfModuleId)}</baseVfModuleId>
    	<baseVfModuleStackId>${MsoUtils.xmlEscape(baseVfModuleStackId)}</baseVfModuleStackId>
    	<skipAAI>true</skipAAI>
    	<backout>${MsoUtils.xmlEscape(backoutOnFailure)}</backout>
    	<failIfExists>true</failIfExists>
		<vfModuleParams>
		${vfModuleParams}
		</vfModuleParams>
		<msoRequest>
			<requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
			<serviceInstanceId>${MsoUtils.xmlEscape(svcInstId)}</serviceInstanceId>
		</msoRequest>
		<messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
		<notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
		</createVfModuleRequest>"""

		logger.debug("Create VfModule Request to VNF Adapter: " + createVnfARequest)
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

		logger.trace('Entered ' + method)

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
			logger.debug("DoCreateVfModule Request: " + request)

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
			logger.debug('Incoming message: ' + System.lineSeparator() + request)
			logger.trace('Exited ' + method)
			return request
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Invalid Message")
		}
	}

	public boolean isVolumeGroupIdPresent(DelegateExecution execution) {

		def method = getClass().getSimpleName() + '.isVolumeGroupIdPresent(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

		def request = execution.getVariable('DoCreateVfModuleRequest')
		String volumeGroupId = utils.getNodeText(request, "volume-group-id")
		if (volumeGroupId == null || volumeGroupId.isEmpty()) {
			logger.debug('No volume group id is present')
			return false
		}
		else {
			logger.debug('Volume group id is present')
			return true
		}

	}

	public boolean isVolumeGroupNamePresent(DelegateExecution execution) {

		def method = getClass().getSimpleName() + '.isVolumeGroupNamePresent(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

		def request = execution.getVariable('DoCreateVfModuleRequest')
		String volumeGroupName = utils.getNodeText(request, "volume-group-name")
		if (volumeGroupName == null || volumeGroupName.isEmpty()) {
			logger.debug('No volume group name is present')
			return false
		}
		else {
			logger.debug('Volume group name is present')
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
		def cloudOwner = execution.getVariable("DCVFM_cloudOwner")
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
			if (!sdncVersion.equals("1707")) {
				sdncVNFParamsXml = buildSDNCParamsXml(execution)
			}
			else {
				sdncVNFParamsXml = buildCompleteSDNCParamsXml(execution)
			}
		}else{
			sdncVNFParamsXml = ""
		}

		String sdncRequest = ""

		if (!sdncVersion.equals("1707")) {

			sdncRequest =
		"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
			<request-action>VNFActivateRequest</request-action>
			<source>${MsoUtils.xmlEscape(source)}</source>
			<notification-url/>
		</request-information>
		<service-information>
			<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
			<service-type>${MsoUtils.xmlEscape(serviceId)}</service-type>
			<service-instance-id>${MsoUtils.xmlEscape(svcInstId)}</service-instance-id>
			<subscriber-name>notsurewecare</subscriber-name>
		</service-information>
		<vnf-request-information>
			<vnf-id>${MsoUtils.xmlEscape(vfModuleId)}</vnf-id>
			<vnf-type>${MsoUtils.xmlEscape(vfModuleModelName)}</vnf-type>
			<vnf-name>${MsoUtils.xmlEscape(vfModuleName)}</vnf-name>
			<generic-vnf-id>${MsoUtils.xmlEscape(vnfId)}</generic-vnf-id>
			<generic-vnf-name>${MsoUtils.xmlEscape(vnfName)}</generic-vnf-name>
			<generic-vnf-type>${MsoUtils.xmlEscape(vnfType)}</generic-vnf-type>
			<aic-cloud-region>${MsoUtils.xmlEscape(cloudSiteId)}</aic-cloud-region>
			<tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
			${modelCustomizationUuidString}
			<use-preload>${MsoUtils.xmlEscape(usePreloadToSDNC)}</use-preload>
		${sdncVNFParamsXml}
		</vnf-request-information>
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

		}
		else {

			sdncRequest =
			"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vf-module-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
				<sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
			<request-action>CreateVfModuleInstance</request-action>
			<source>${MsoUtils.xmlEscape(source)}</source>
			<notification-url/>
		</request-information>
		<service-information>
			<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
			<subscription-service-type>${MsoUtils.xmlEscape(serviceId)}</subscription-service-type>
			${serviceEcompModelInformation}
			<service-instance-id>${MsoUtils.xmlEscape(svcInstId)}</service-instance-id>
			<global-customer-id>${MsoUtils.xmlEscape(globalSubscriberId)}</global-customer-id>
		</service-information>
		<vnf-information>
			<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
			<vnf-type>${MsoUtils.xmlEscape(vnfType)}</vnf-type>
			${vnfEcompModelInformation}
		</vnf-information>
		<vf-module-information>
			<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
			<vf-module-type>${MsoUtils.xmlEscape(vfModuleModelName)}</vf-module-type>
			${vfModuleEcompModelInformation}
		</vf-module-information>
		<vf-module-request-input>
			<vf-module-name>${MsoUtils.xmlEscape(vfModuleName)}</vf-module-name>
			<tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
			<aic-cloud-region>${MsoUtils.xmlEscape(cloudSiteId)}</aic-cloud-region>
		${sdncVNFParamsXml}
		</vf-module-request-input>
	  </sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""


			/*
			sdncRequest =
			"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${MsoUtils.xmlEscape(requestId)}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
			<request-action>CreateVfModuleInstance</request-action>
			<source>${MsoUtils.xmlEscape(source)}</source>
			<notification-url/>
		</request-information>
		<service-information>
			<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
			<service-type>${MsoUtils.xmlEscape(serviceId)}</service-type>
			${serviceEcompModelInformation}
			<service-instance-id>${MsoUtils.xmlEscape(svcInstId)}</service-instance-id>
			<global-customer-id>${MsoUtils.xmlEscape(globalSubscriberId)}</global-customer-id>
		</service-information>
		<vnf-information>
			<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
			<vnf-type>${MsoUtils.xmlEscape(vnfType)}</vnf-type>
			${vnfEcompModelInformation}
		</vnf-information>
		<vf-module-information>
			<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
			<vf-module-type>${MsoUtils.xmlEscape(vfModuleModelName)}</vf-module-type>
			${vfModuleEcompModelInformation}
		</vf-module-information>
		<vf-module-request-input>
			<vf-module-name>${MsoUtils.xmlEscape(vfModuleName)}</vf-module-name>
			<tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
			<aic-cloud-region>${MsoUtils.xmlEscape(cloudSiteId)}</aic-cloud-region>
		${sdncVNFParamsXml}
		</vf-module-request-input>
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
			*/

		}

	logger.debug("sdncRequest:  " + sdncRequest)
	return sdncRequest

	}

	public void preProcessSDNCActivateRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCActivateRequest(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCActivateRequest Process")
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
			logger.debug("Outgoing CommitSDNCRequest is: \n" + activateSDNCRequest)

		}catch(Exception e){
			logger.debug("Exception Occured Processing preProcessSDNCActivateRequest. Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED  preProcessSDNCActivateRequest Process")
	}

	public void postProcessVNFAdapterRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.postProcessVNFAdapterRequest(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)
		execution.setVariable("prefix",Prefix)
		try{
		logger.debug("STARTED postProcessVNFAdapterRequest Process")

		String vnfResponse = execution.getVariable("DCVFM_createVnfAResponse")
		logger.debug("VNF Adapter Response is: " + vnfResponse)

		RollbackData rollbackData = execution.getVariable("rollbackData")
		if(vnfResponse != null){

			if(vnfResponse.contains("createVfModuleResponse")){
				logger.debug("Received a Good Response from VNF Adapter for CREATE_VF_MODULE Call.")
				execution.setVariable("DCVFM_vnfVfModuleCreateCompleted", true)
				String heatStackId = utils.getNodeText(vnfResponse, "vfModuleStackId")
				execution.setVariable("DCVFM_heatStackId", heatStackId)
				logger.debug("Received heat stack id from VNF Adapter: " + heatStackId)
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
								logger.debug("Obtained contrailServiceInstanceFqdn: " + contrailServiceInstanceFqdn)
								execution.setVariable("DCVFM_contrailServiceInstanceFqdn", contrailServiceInstanceFqdn)
							}
							else if (key.endsWith("contrail_network_policy_fqdn")) {
								String contrailNetworkPolicyFqdn = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								logger.debug("Obtained contrailNetworkPolicyFqdn: " + contrailNetworkPolicyFqdn)
								contrailNetworkPolicyFqdnList.add(contrailNetworkPolicyFqdn)
							}
							else if (key.equals("oam_management_v4_address")) {
								String oamManagementV4Address = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								logger.debug("Obtained oamManagementV4Address: " + oamManagementV4Address)
								execution.setVariable("DCVFM_oamManagementV4Address", oamManagementV4Address)
							}
							else if (key.equals("oam_management_v6_address")) {
								String oamManagementV6Address = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								logger.debug("Obtained oamManagementV6Address: " + oamManagementV6Address)
								execution.setVariable("DCVFM_oamManagementV6Address", oamManagementV6Address)
							}

						}
					}
					if (!contrailNetworkPolicyFqdnList.isEmpty()) {
						execution.setVariable("DCVFM_contrailNetworkPolicyFqdnList", contrailNetworkPolicyFqdnList)
					}
				}
			}else{
				logger.debug("Received a BAD Response from VNF Adapter for CREATE_VF_MODULE Call.")
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "VNF Adapter Error")
			}
		}else{
			logger.debug("Response from VNF Adapter is Null for CREATE_VF_MODULE Call.")
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Empty response from VNF Adapter")
		}

		rollbackData.put("VFMODULE", "rollbackVnfAdapterCreate", "true")
		execution.setVariable("rollbackData", rollbackData)

		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			logger.debug("Internal Error Occured in PostProcess Method")
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Internal Error Occured in PostProcess Method")
		}
		logger.trace("COMPLETED postProcessVnfAdapterResponse Process")
	}


	public void preProcessUpdateAAIVfModuleRequestOrch(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessUpdateAAIVfModuleRequestOrch(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessUpdateAAIVfModuleRequestOrch")

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
			logger.debug("Outgoing UpdateAAIVfModuleRequest is: \n" + updateAAIVfModuleRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preProcessUpdateAAIVfModuleRequestOrch", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during preProcessUpdateAAIVfModuleRequestOrch Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessUpdateAAIVfModuleRequestOrch")

	}

	public void preProcessUpdateAAIVfModuleRequestStatus(DelegateExecution execution, String status) {
		def method = getClass().getSimpleName() + '.preProcessUpdateAAIVfModuleStatus(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessUpdateAAIVfModuleStatus")

		try{

			//Build UpdateAAIVfModule Request
			execution.setVariable("DCVFM_orchestrationStatus", status)

			String updateAAIVfModuleRequest = buildUpdateAAIVfModuleRequest(execution, false, true, false, false)

			updateAAIVfModuleRequest = utils.formatXml(updateAAIVfModuleRequest)
			execution.setVariable("DCVFM_updateAAIVfModuleRequest", updateAAIVfModuleRequest)
			logger.debug("Outgoing UpdateAAIVfModuleRequest is: \n" + updateAAIVfModuleRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preProcessUpdateAAIVfModuleStatus", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during preProcessUpdateAAIVfModuleStatus Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessUpdateAAIVfModuleStatus")

	}


	public void preProcessUpdateAAIVfModuleRequestGroup(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessUpdateAAIVfModuleRequestGroup(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessUpdateAAIVfModuleRequestGroup")

		try{

			//Build UpdateAAIVfModule Request

			String updateAAIVfModuleRequest = buildUpdateAAIVfModuleRequest(execution, true, false, false, false)

			updateAAIVfModuleRequest = utils.formatXml(updateAAIVfModuleRequest)
			execution.setVariable("DCVFM_updateAAIVfModuleRequest", updateAAIVfModuleRequest)
			logger.debug("Outgoing UpdateAAIVfModuleRequest is: \n" + updateAAIVfModuleRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preProcessUpdateAAIVfModuleRequestGroup", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during preProcessUpdateAAIVfModuleRequestGroup Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED  preProcessUpdateAAIVfModuleRequestGroup")

	}

	public void validateSDNCResponse(DelegateExecution execution, String response, String method){

		execution.setVariable("prefix",Prefix)
		logger.debug("STARTED ValidateSDNCResponse Process")

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		logger.debug("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		String sdncResponse = response
		if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
			logger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse)
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
			logger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
			throw new BpmnError("MSOWorkflowException")
		}
		logger.trace("COMPLETED ValidateSDNCResponse Process")
	}

	public void preProcessUpdateAfterCreateRequest(DelegateExecution execution){

		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessRequest Process")
		try{
			String response = execution.getVariable("DCVFM_assignSDNCAdapterResponse")
			logger.debug("DCVFM_assignSDNCAdapterResponse: " + response)

			String data = utils.getNodeXml(response, "response-data")
			String vnfId = utils.getNodeText(data, "vnf-id")

			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
			}

			String serviceOperation = "/VNF-API:vnfs/vnf-list/" + vnfId
			def callbackUrl = execution.getVariable("DCVFM_sdncCallbackUrl")
			logger.debug("callbackUrl: " + callbackUrl)

			String SDNCGetRequest =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
											xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
											xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
					<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
					<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
					<sdncadapter:SvcOperation>${MsoUtils.xmlEscape(serviceOperation)}</sdncadapter:SvcOperation>
					<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
					<sdncadapter:MsoAction>mobility</sdncadapter:MsoAction>
				</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData></sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			execution.setVariable("DCVFM_getSDNCRequest", SDNCGetRequest)
			logger.debug("Outgoing GetSDNCRequest is: \n" + SDNCGetRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preProcessSDNCGetRequest", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during prepareProvision Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessSDNCGetRequest Process")
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
				<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
				<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
				${heatStackIdString}
				${orchestrationStatusString}
				${volumeGroupIdString}
				${contrailFqdnString}
			</UpdateAAIVfModuleRequest>"""

	logger.trace("updateAAIVfModule Request: " + updateAAIVfModuleRequest)
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

	public String buildCompleteSDNCParamsXml(DelegateExecution execution){

		String params = ""
		StringBuilder sb = new StringBuilder()
		Map<String, String> paramsMap = execution.getVariable("DCVFM_vnfParamsMap")

		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			String paramsXml
			String key = entry.getKey();
			String value = entry.getValue()
			paramsXml =	"""<${key}>$value</$key>"""
			params = sb.append(paramsXml)
		}
		return params
	}

   public void queryCloudRegion (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED queryCloudRegion")

		try {
			String cloudRegion = execution.getVariable("DCVFM_cloudSiteId")

			// Prepare AA&I url
			AaiUtil aaiUtil = new AaiUtil(this)

			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), cloudRegion))
			def queryCloudRegionRequest = aaiUtil.createAaiUri(uri)

			execution.setVariable("DCVFM_queryCloudRegionRequest", queryCloudRegionRequest)

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
				logger.debug(errorMessage)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
				execution.setVariable("DCVFM_isCloudRegionGood", false)
			}
			logger.debug(" is Cloud Region Good: " + execution.getVariable("DCVFM_isCloudRegionGood"))

		} catch(BpmnError b){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Rethrowing MSOWorkflowException", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + b.getMessage());
			throw b
		}catch (Exception ex) {
			// try error
			String errorMessage = "Bpmn error encountered in CreateVfModule flow. Unexpected Response from AAI - " + ex.getMessage()
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"AAI Query Cloud Region Failed " + errorMessage, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + ex);
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

	   execution.setVariable("prefix",Prefix)
	   try{
		   logger.debug("Caught a BPMN Exception")
		   logger.debug("Started processBPMNException Method")
		   logger.debug("Variables List: " + execution.getVariables())
		   if(execution.getVariable("WorkflowException") == null){
			   exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception occured during DoCreateVfModule Sub Process")
		   }

	   }catch(Exception e){
		   logger.debug("Caught Exception during processBPMNException Method: " + e)
	   }
	   logger.debug("Completed processBPMNException Method")
   }

   public void prepareCreateAAIVfModuleVolumeGroupRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepareCreateAAIVfModuleVolumeGroupRequest(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED prepareCreateAAIVfModuleVolumeGroupRequest")

		try{

			//Build CreateAAIVfModuleVolumeGroup Request

			def vnfId = execution.getVariable("DCVFM_vnfId")
			def vfModuleId = execution.getVariable("DCVFM_vfModuleId")
			def volumeGroupId = execution.getVariable("DCVFM_volumeGroupId")
			//def aicCloudRegion = execution.getVariable("DCVFM_cloudSiteId")
			def aicCloudRegion = execution.getVariable("DCVFM_cloudRegionForVolume")
			def cloudOwner = execution.getVariable("DCVFM_cloudOwner")
			String createAAIVfModuleVolumeGroupRequest =
			"""<CreateAAIVfModuleVolumeGroupRequest>
				<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
				<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
				<volume-group-id>${MsoUtils.xmlEscape(volumeGroupId)}</volume-group-id>
				<aic-cloud-region>${MsoUtils.xmlEscape(aicCloudRegion)}</aic-cloud-region>
				<cloud-owner>${MsoUtils.xmlEscape(cloudOwner)}</cloud-owner>
			</CreateAAIVfModuleVolumeGroupRequest>"""

			createAAIVfModuleVolumeGroupRequest = utils.formatXml(createAAIVfModuleVolumeGroupRequest)
			execution.setVariable("DCVFM_createAAIVfModuleVolumeGroupRequest", createAAIVfModuleVolumeGroupRequest)
			logger.debug("Outgoing CreateAAIVfModuleVolumeGroupRequest is: \n" + createAAIVfModuleVolumeGroupRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Exception Occured Processing prepareCreateAAIVfModuleVolumeGroupRequest', "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during prepareCreateAAIVfModuleVolumeGroupRequest Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED  prepareCreateAAIVfModuleVolumeGroupRequest")

	}

   public void createNetworkPoliciesInAAI(DelegateExecution execution) {
	   def method = getClass().getSimpleName() + '.createNetworkPoliciesInAAI(' +
	   'execution=' + execution.getId() +
	   ')'

	   logger.trace('Entered ' + method)
	   execution.setVariable("prefix", Prefix)
	   logger.trace("STARTED createNetworkPoliciesInAAI")

	   try {
		   // get variables
		   List fqdnList = execution.getVariable("DCVFM_contrailNetworkPolicyFqdnList")
		   int fqdnCount = fqdnList.size()
		   def rollbackData = execution.getVariable("rollbackData")

		   execution.setVariable("DCVFM_networkPolicyFqdnCount", fqdnCount)
		   logger.debug("DCVFM_networkPolicyFqdnCount - " + fqdnCount)

		   AaiUtil aaiUriUtil = new AaiUtil(this)

			if (fqdnCount > 0) {

				// AII loop call over contrail network policy fqdn list
				for (i in 0..fqdnCount-1) {

					int counting = i+1
					String fqdn = fqdnList[i]

					// Query AAI for this network policy FQDN
					AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicies())
					uri.queryParam("network-policy-fqdn", fqdn)

					AAIResourcesClient resourceClient = new AAIResourcesClient()


					if (resourceClient.exists(uri)) {

						logger.debug(" QueryAAINetworkPolicyByFQDN Success REST Response, , NetworkPolicy #" + counting)

					} else {
						// This network policy FQDN is not in AAI yet. Add it now
						logger.debug("This network policy FQDN is not in AAI yet: " + fqdn)
						// Add the network policy with this FQDN to AAI
						def networkPolicyId = UUID.randomUUID().toString()
						logger.debug("Adding network-policy with network-policy-id " + networkPolicyId)

						NetworkPolicy policy = new NetworkPolicy()
						policy.setNetworkPolicyId(networkPolicyId)
						policy.setNetworkPolicyFqdn(fqdn)
						policy.setHeatStackId(execution.getVariable("DCVFM_heatStackId"))

						AAIResourceUri netUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicy(networkPolicyId))
						resourceClient.create(netUri, policy)

						rollbackData.put("VFMODULE", "rollbackCreateNetworkPoliciesAAI", "true")
						rollbackData.put("VFMODULE", "contrailNetworkPolicyFqdn" + i, fqdn)
						execution.setVariable("rollbackData", rollbackData)
					}

				} // end loop


		   } else {
		   	   logger.debug("No contrail network policies to query/create")

		   }

	   } catch (BpmnError e) {
		   throw e;
	   } catch (Exception ex) {
		   String exceptionMessage = "Bpmn error encountered in DoCreateVfModule flow. createNetworkPoliciesInAAI() - " + ex.getMessage()
		   logger.debug(exceptionMessage)
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

	   logger.trace('Entered ' + method)

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
						<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
						${ipv4OamAddressElement}
						${managementV6AddressElement}
					</UpdateAAIGenericVnfRequest>
				"""
			   updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
			   execution.setVariable('DCVM_updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
			   logger.debug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest)


		   logger.trace('Exited ' + method)
	   } catch (BpmnError e) {
		   throw e;
	   } catch (Exception e) {
		   logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				   "Exception Encountered in " + method, "BPMN",
				   ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);

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

	   logger.trace('Entered ' + method)

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

		   logger.trace('Exited ' + method)
	   } catch (BpmnError e) {
		   throw e;
	   } catch (Exception e) {
		   logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				   'Caught exception in ' + method, "BPMN",
				   ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in postProcessUpdateAAIGenericVnf(): ' + e.getMessage())
	   }
   }

   public void queryCatalogDB (DelegateExecution execution) {

	   String msg = ""
	   logger.trace("queryCatalogDB ")

	   try {
		   boolean twoPhaseDesign = false
		   // check for input

		   String vfModuleModelName = execution.getVariable("DCVFM_vfModuleModelName")
		   logger.debug("vfModuleModelName: " + vfModuleModelName)
		   def vnfModelInfo = execution.getVariable("vnfModelInfo")
		   def vnfModelCustomizationUuid = jsonUtil.getJsonValue(vnfModelInfo, "modelCustomizationUuid")

		   logger.debug("vnfModelCustomizationUuid: " + vnfModelCustomizationUuid)

		   JSONArray vnfs = catalogDbUtils.getAllVnfsByVnfModelCustomizationUuid(execution, vnfModelCustomizationUuid, "v2")

		   logger.debug("Incoming Query Catalog DB for Vnf Response is: " + vnfModelCustomizationUuid)
		   // Only one match here
		   if (vnfs != null) {
			   JSONObject vnfObject = vnfs.get(0)
			   if (vnfObject != null) {
				   String vnfJson = vnfObject.toString()
				   //
				   VnfResource vnf = mapper.readValue(vnfJson, VnfResource.class);

				   // Get multiStageDesign flag

				   String multiStageDesignValue = vnf.getMultiStageDesign()
				   logger.debug("multiStageDesign value from Catalog DB is: " + multiStageDesignValue)
				   if (multiStageDesignValue != null) {
					   if (multiStageDesignValue.equalsIgnoreCase("true")) {
			   				twoPhaseDesign = true
					   }
				   }
			   }
		   }

		   logger.debug("setting twoPhaseDesign flag to: " + twoPhaseDesign)

		   execution.setVariable("DCVFM_twoPhaseDesign", twoPhaseDesign)
	   } catch (BpmnError e) {
		   throw e;
	   } catch (Exception e) {
		   logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				   'Caught exception in queryCatalogDB', "BPMN",
				   ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryCatalogDB(): ' + e.getMessage())
	   }
   }


   public void preProcessRollback (DelegateExecution execution) {

	   logger.trace("preProcessRollback")
	   try {

		   Object workflowException = execution.getVariable("WorkflowException");

		   if (workflowException instanceof WorkflowException) {
			   logger.debug("Prev workflowException: " + workflowException.getErrorMessage())
			   execution.setVariable("prevWorkflowException", workflowException);
			   //execution.setVariable("WorkflowException", null);
		   }
	   } catch (BpmnError e) {
		   logger.debug("BPMN Error during preProcessRollback")
	   } catch(Exception ex) {
		   String msg = "Exception in preProcessRollback. " + ex.getMessage()
		   logger.debug(msg)
	   }
	   logger.trace("Exit preProcessRollback")
   }

   public void postProcessRollback (DelegateExecution execution) {

	   logger.trace("postProcessRollback")
	   String msg = ""
	   try {
		   Object workflowException = execution.getVariable("prevWorkflowException");
		   if (workflowException instanceof WorkflowException) {
			   logger.debug("Setting prevException to WorkflowException: ")
			   execution.setVariable("WorkflowException", workflowException);
		   }
		   execution.setVariable("rollbackData", null)
	   } catch (BpmnError b) {
		   logger.debug("BPMN Error during postProcessRollback")
		   throw b;
	   } catch(Exception ex) {
		   msg = "Exception in postProcessRollback. " + ex.getMessage()
		   logger.debug(msg)
	   }
	   logger.trace("Exit postProcessRollback")
   }

}
