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

package org.onap.so.bpmn.common.scripts

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import org.json.JSONObject
import org.json.XML
import org.onap.so.bpmn.core.xml.XmlTool
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class VidUtils {
    private static final Logger logger = LoggerFactory.getLogger( VidUtils.class);

	public MsoUtils utils = new MsoUtils()
	private AbstractServiceTaskProcessor taskProcessor

	public VidUtils(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
	}

	/**
	 * Create a volume-request XML using a JSON string
	 * @param jsonReq - JSON request from VID
	 * @param action
	 * @return
	 */
	public String createXmlVolumeRequest(String jsonReq, String action, String serviceInstanceId) {
		def jsonSlurper = new JsonSlurper()
		try{
			Map reqMap = jsonSlurper.parseText(jsonReq)
			return createXmlVolumeRequest(reqMap, action, serviceInstanceId)
		}
		catch(Exception e) {
			throw e
		}
	}

	/**
	 * Create a volume-request XML using a map
	 * @param requestMap - map created from VID JSON
	 * @param action
	 * @param serviceInstanceId
	 * @return
	 */
	public String createXmlVolumeRequest(Map requestMap, String action, String serviceInstanceId) {
		createXmlVolumeRequest(requestMap, action, serviceInstanceId, '')
	}


	/**
	 * Create a volume-request XML using a map
	 * @param requestMap
	 * @param action
	 * @param serviceInstanceId
	 * @param volumeGroupId
	 * @return
	 */
	public String createXmlVolumeRequest(Map requestMap, String action, String serviceInstanceId, String volumeGroupId) {
		def vnfType = ''
		def serviceName = ''
		def modelCustomizationName = ''
		def asdcServiceModelVersion = ''

		def suppressRollback = requestMap.requestDetails.requestInfo.suppressRollback

		def backoutOnFailure = ""
		if(suppressRollback != null){
			if ( suppressRollback == true) {
				backoutOnFailure = "false"
			} else if ( suppressRollback == false) {
				backoutOnFailure = "true"
			}
		}

		def volGrpName = requestMap.requestDetails.requestInfo?.instanceName ?: ''
		def serviceId = requestMap.requestDetails.requestParameters?.serviceId ?: ''
		def relatedInstanceList = requestMap.requestDetails.relatedInstanceList
		relatedInstanceList.each {
			if (it.relatedInstance.modelInfo?.modelType == 'service') {
				serviceName = it.relatedInstance.modelInfo?.modelName
				asdcServiceModelVersion = it.relatedInstance.modelInfo?.modelVersion
			}
			if (it.relatedInstance.modelInfo?.modelType == 'vnf') {
				modelCustomizationName = it.relatedInstance.modelInfo?.modelInstanceName
			}
		}

		vnfType = serviceName + '/' + modelCustomizationName

		def userParams = requestMap.requestDetails?.requestParameters?.userParams
		def userParamsNode = ''
		if(userParams != null) {
			userParamsNode = buildUserParams(userParams)
		}
		def modelCustomizationId = requestMap.requestDetails?.modelInfo?.modelCustomizationUuid ?: ''

		String xmlReq = """
		<volume-request xmlns="http://www.w3.org/2001/XMLSchema">
			<request-info>
				<action>${MsoUtils.xmlEscape(action)}</action>
				<source>${MsoUtils.xmlEscape(requestMap.requestDetails.requestInfo.source)}</source>
				<service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
			</request-info>
			<volume-inputs>
				<volume-group-id>${MsoUtils.xmlEscape(volumeGroupId)}</volume-group-id>
				<volume-group-name>${MsoUtils.xmlEscape(volGrpName)}</volume-group-name>
				<vnf-type>${MsoUtils.xmlEscape(vnfType)}</vnf-type>
				<vf-module-model-name>${MsoUtils.xmlEscape(requestMap.requestDetails.modelInfo.modelName)}</vf-module-model-name>
				<asdc-service-model-version>${MsoUtils.xmlEscape(asdcServiceModelVersion)}</asdc-service-model-version>
				<aic-cloud-region>${MsoUtils.xmlEscape(requestMap.requestDetails.cloudConfiguration.lcpCloudRegionId)}</aic-cloud-region>
				<tenant-id>${MsoUtils.xmlEscape(requestMap.requestDetails.cloudConfiguration.tenantId)}</tenant-id>
				<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
				<backout-on-failure>${MsoUtils.xmlEscape(backoutOnFailure)}</backout-on-failure>
				<model-customization-id>${MsoUtils.xmlEscape(modelCustomizationId)}</model-customization-id>
			</volume-inputs>
			<volume-params>
				$userParamsNode
			</volume-params>
		</volume-request>
		"""
		// return a pretty-print of the volume-request xml without the preamble
		return groovy.xml.XmlUtil.serialize(xmlReq.normalize().replaceAll("\t", "").replaceAll("\n", "")).replaceAll("(<\\?[^<]*\\?>\\s*[\\r\\n]*)?", "") 
	}

	/**
	 * A common method that can be used to build volume-params node from a map.
	 * @param Map userParams
	 * @return
	 */
	public String buildUserParams(userParams) {
		if (userParams == null) return ""
		def xml = ""
		def key = ""
		def value = ""
		userParams.each {it ->
			key = it.name.replaceAll(/\B[A-Z]/) { '_' + it }.toLowerCase()
			value = it.value
			xml += "<param name=\"${key}\">${MsoUtils.xmlEscape(value)}</param>"
		}

		return xml
	}

	/**
	 * A common method that can be used to extract 'requestDetails'
	 * @param String json
	 * @return String json requestDetails
	 */
	@Deprecated
	public getJsonRequestDetails(String jsonInput) {
		String rtn = ""
		if (jsonInput.isEmpty() || jsonInput == null) {
			return rtn
		} else {
			def jsonMapObject = new JsonSlurper().parseText(jsonInput)
			if (jsonMapObject instanceof Map) {
				String jsonString = new JsonBuilder(jsonMapObject.requestDetails)
				rtn = '{'+"requestDetails"+":"+jsonString+'}'
				return rtn
			} else {
			    return rtn
			}
		}
	}

	/**
	 * A common method that can be used to extract 'requestDetails' in Xml
	 * @param String json
	 * @return String xml requestDetails
	 */
	@Deprecated
	public getJsonRequestDetailstoXml(String jsonInput) {
		String rtn = null
		def jsonString = getJsonRequestDetails(jsonInput)
		if (jsonString == null) {
			return rtn
		} else {
		    JSONObject jsonObj = new JSONObject(jsonString)
			return XmlTool.normalize(XML.toString(jsonObj))
		}
	}

	/**
	 * Create a network-request XML using a map
 	 * @param execution
	 * @param xmlRequestDetails - requestDetails in xml
	 * @return
	 * Note: See latest version: createXmlNetworkRequestInstance()
	 */

	public String createXmlNetworkRequestInfra(execution, def networkJsonIncoming) {

		def requestId = execution.getVariable("requestId")
		def serviceInstanceId = execution.getVariable("serviceInstanceId")
		def requestAction = execution.getVariable("requestAction")
		def networkId = (execution.getVariable("networkId")) != null ? execution.getVariable("networkId") : ""
		
		def jsonSlurper = new JsonSlurper()
		try {
			Map reqMap = jsonSlurper.parseText(networkJsonIncoming)
			def instanceName =  reqMap.requestDetails.requestInfo.instanceName
			def modelCustomizationId =  reqMap.requestDetails.modelInfo.modelCustomizationId
			if (modelCustomizationId == null) {
				modelCustomizationId =  reqMap.requestDetails.modelInfo.modelCustomizationUuid !=null ?
				                        reqMap.requestDetails.modelInfo.modelCustomizationUuid : ""
			}
			def modelName = reqMap.requestDetails.modelInfo.modelName
			def lcpCloudRegionId = reqMap.requestDetails.cloudConfiguration.lcpCloudRegionId
			def tenantId = reqMap.requestDetails.cloudConfiguration.tenantId
			def serviceId = reqMap.requestDetails.requestInfo.productFamilyId
			def suppressRollback = reqMap.requestDetails.requestInfo.suppressRollback.toString()
			def backoutOnFailure = "true"
			if(suppressRollback != null){
				if (suppressRollback == true || suppressRollback == "true") {
					backoutOnFailure = "false"
				} else if (suppressRollback == false || suppressRollback == "false") {
					backoutOnFailure = "true"
				}
			}

			//def userParams = reqMap.requestDetails.requestParameters.userParams
			//def userParamsNode = buildUserParams(userParams)
			def userParams = reqMap.requestDetails?.requestParameters?.userParams
			def userParamsNode = ''
			if(userParams != null) {
				userParamsNode = buildUserParams(userParams)
			}

			//'sdncVersion' = current, '1610' (non-RPC SDNC) or '1702' (RPC SDNC)
			def sdncVersion =  execution.getVariable("sdncVersion")

			String xmlReq = """
			<network-request xmlns="http://www.w3.org/2001/XMLSchema">
			 <request-info>
	            <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
			 	<action>${MsoUtils.xmlEscape(requestAction)}</action>
			 	<source>VID</source>
			 	<service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
			 </request-info>
			 <network-inputs>
			 	<network-id>${MsoUtils.xmlEscape(networkId)}</network-id>
			 	<network-name>${MsoUtils.xmlEscape(instanceName)}</network-name>
			 	<network-type>${MsoUtils.xmlEscape(modelName)}</network-type>
				<modelCustomizationId>${MsoUtils.xmlEscape(modelCustomizationId)}</modelCustomizationId>
			 	<aic-cloud-region>${MsoUtils.xmlEscape(lcpCloudRegionId)}</aic-cloud-region>
			 	<tenant-id>${MsoUtils.xmlEscape(tenantId)}</tenant-id>
			 	<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
			 	<backout-on-failure>${MsoUtils.xmlEscape(backoutOnFailure)}</backout-on-failure>
                <sdncVersion>${MsoUtils.xmlEscape(sdncVersion)}</sdncVersion>
			 </network-inputs>
			 <network-params>
				${userParamsNode}
			 </network-params>
			</network-request>
			"""
			// return a pretty-print of the volume-request xml without the preamble
			return groovy.xml.XmlUtil.serialize(xmlReq.normalize().replaceAll("\t", "").replaceAll("\n", "")).replaceAll("(<\\?[^<]*\\?>\\s*[\\r\\n]*)?", "")

		} catch(Exception e) {
			logger.debug("Error in Vid Utils: {}", e.getCause(), e)
			throw e
		}
	}

	/**
	 * Create a network-request XML using a map,
 	 * @param execution
	 * @return
	 */
	public String createXmlNetworkRequestInstance(execution) {

		def networkModelUuid = ""
		def networkModelName = ""
		def networkModelVersion = ""
		def networkModelCustomizationUuid = ""
		def networkModelInvariantUuid = ""

		// verify the DB Catalog response JSON structure
		def networkModelInfo = execution.getVariable("networkModelInfo")
		def jsonSlurper = new JsonSlurper()
		if (networkModelInfo != null) {
			try {
				Map modelMap = jsonSlurper.parseText(networkModelInfo)
				if (modelMap != null) {
					if (networkModelInfo.contains("modelUuid")) {
						networkModelUuid = modelMap.modelUuid !=null ? modelMap.modelUuid : ""
					}
					if (networkModelInfo.contains("modelName")) {
						networkModelName = modelMap.modelName !=null ? modelMap.modelName : ""
					}
					if (networkModelInfo.contains("modelVersion")) {
						networkModelVersion = modelMap.modelVersion !=null ? modelMap.modelVersion : ""
					}
					if (networkModelInfo.contains("modelCustomizationUuid")) {
						networkModelCustomizationUuid = modelMap.modelCustomizationUuid !=null ? modelMap.modelCustomizationUuid : ""
					}
					if (networkModelInfo.contains("modelInvariantUuid")) {
						networkModelInvariantUuid = modelMap.modelInvariantUuid !=null ? modelMap.modelInvariantUuid : ""
					}
				}
			} catch (Exception ex) {
		    	throw ex
			}
		}

		def serviceModelUuid = ""
		def serviceModelName = ""
		def serviceModelVersion = ""
		def serviceModelCustomizationUuid = ""
		def serviceModelInvariantUuid = ""

		// verify the DB Catalog response JSON structure
		def serviceModelInfo = execution.getVariable("serviceModelInfo")
		def jsonServiceSlurper = new JsonSlurper()
		if (serviceModelInfo != null) {
			try {
				Map modelMap = jsonServiceSlurper.parseText(serviceModelInfo)
				if (modelMap != null) {
					if (serviceModelInfo.contains("modelUuid")) {
						serviceModelUuid = modelMap.modelUuid !=null ? modelMap.modelUuid : ""
					}
					if (serviceModelInfo.contains("modelName")) {
						serviceModelName = modelMap.modelName !=null ? modelMap.modelName : ""
					}
					if (serviceModelInfo.contains("modelVersion")) {
						serviceModelVersion = modelMap.modelVersion !=null ? modelMap.modelVersion : ""
					}
					if (serviceModelInfo.contains("modelCustomizationUuid")) {
						serviceModelCustomizationUuid = modelMap.modelCustomizationUuid !=null ? modelMap.modelCustomizationUuid : ""
					}
					if (serviceModelInfo.contains("modelInvariantUuid")) {
						serviceModelInvariantUuid = modelMap.modelInvariantUuid !=null ? modelMap.modelInvariantUuid : ""
					}
				}
			} catch (Exception ex) {
				throw ex
			}
		}


		def subscriptionServiceType = execution.getVariable("subscriptionServiceType") != null ? execution.getVariable("subscriptionServiceType") : ""
		def globalSubscriberId = execution.getVariable("globalSubscriberId") != null ? execution.getVariable("globalSubscriberId") : ""
		def requestId = execution.getVariable("msoRequestId")
		def serviceInstanceId = execution.getVariable("serviceInstanceId") != null ? execution.getVariable("serviceInstanceId") : ""
		def networkId = (execution.getVariable("networkId")) != null ? execution.getVariable("networkId") : "" // optional
		def networkName =  execution.getVariable("networkName") != null ? execution.getVariable("networkName") : "" // optional
		def aicCloudReqion = execution.getVariable("lcpCloudRegionId") != null ? execution.getVariable("lcpCloudRegionId") : ""
		def tenantId = execution.getVariable("tenantId") != null ? execution.getVariable("tenantId") : ""
		def serviceId = execution.getVariable("productFamilyId") != null ? execution.getVariable("productFamilyId") : ""
		def failIfExist = execution.getVariable("failIfExists") != null ? execution.getVariable("failIfExists") : ""
		def suppressRollback = execution.getVariable("disableRollback")   
		def backoutOnFailure = "true"
		if(suppressRollback != null){
			if (suppressRollback == true || suppressRollback == "true") {
				backoutOnFailure = "false"
			} else if (suppressRollback == false || suppressRollback == "false") {
				backoutOnFailure = "true"
			}
		}

		//'sdncVersion' = current, '1610' (non-RPC SDNC) or '1702' (RPC SDNC)
		def sdncVersion =  execution.getVariable("sdncVersion")

		def source = "VID"
		def action = execution.getVariable("action")

		def userParamsNode = ""
		def userParams = execution.getVariable("networkInputParams")
		if(userParams != null) {
		   userParamsNode = buildUserParams(userParams)
		}

		String xmlReq = """
		<network-request xmlns="http://www.w3.org/2001/XMLSchema">
		 <request-info>
            <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
		 	<action>${MsoUtils.xmlEscape(action)}</action>
		 	<source>${MsoUtils.xmlEscape(source)}</source>
		 	<service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
		 </request-info>
		 <network-inputs>
		 	<network-id>${MsoUtils.xmlEscape(networkId)}</network-id>
		 	<network-name>${MsoUtils.xmlEscape(networkName)}</network-name>
		 	<network-type>${MsoUtils.xmlEscape(networkModelName)}</network-type>
		 	<subscription-service-type>${MsoUtils.xmlEscape(subscriptionServiceType)}</subscription-service-type>
            <global-customer-id>${MsoUtils.xmlEscape(globalSubscriberId)}</global-customer-id>
		 	<aic-cloud-region>${MsoUtils.xmlEscape(aicCloudReqion)}</aic-cloud-region>
		 	<tenant-id>${MsoUtils.xmlEscape(tenantId)}</tenant-id>
		 	<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
		 	<backout-on-failure>${MsoUtils.xmlEscape(backoutOnFailure)}</backout-on-failure>
			<failIfExist>${MsoUtils.xmlEscape(failIfExist)}</failIfExist>
            <networkModelInfo>
              <modelName>${MsoUtils.xmlEscape(networkModelName)}</modelName>
              <modelUuid>${MsoUtils.xmlEscape(networkModelUuid)}</modelUuid>
              <modelInvariantUuid>${MsoUtils.xmlEscape(networkModelInvariantUuid)}</modelInvariantUuid>
              <modelVersion>${MsoUtils.xmlEscape(networkModelVersion)}</modelVersion>
              <modelCustomizationUuid>${MsoUtils.xmlEscape(networkModelCustomizationUuid)}</modelCustomizationUuid>
		    </networkModelInfo>
            <serviceModelInfo>
              <modelName>${MsoUtils.xmlEscape(serviceModelName)}</modelName>
              <modelUuid>${MsoUtils.xmlEscape(serviceModelUuid)}</modelUuid>
              <modelInvariantUuid>${MsoUtils.xmlEscape(serviceModelInvariantUuid)}</modelInvariantUuid>
              <modelVersion>${MsoUtils.xmlEscape(serviceModelVersion)}</modelVersion>
              <modelCustomizationUuid>${MsoUtils.xmlEscape(serviceModelCustomizationUuid)}</modelCustomizationUuid>

		    </serviceModelInfo>
            <sdncVersion>${MsoUtils.xmlEscape(sdncVersion)}</sdncVersion>
		 </network-inputs>
		 <network-params>
			${userParamsNode}
		 </network-params>
		</network-request>
		"""
		// return a pretty-print of the volume-request xml without the preamble
		return groovy.xml.XmlUtil.serialize(xmlReq.normalize().replaceAll("\t", "").replaceAll("\n", "")).replaceAll("(<\\?[^<]*\\?>\\s*[\\r\\n]*)?", "")

	}

	/**
	 * Create a vnf-request XML using a map
	 * @param requestMap - map created from VID JSON
	 * @param action
	 * @return
	 */
	public String createXmlVfModuleRequest(execution, Map requestMap, String action, String serviceInstanceId) {

		//def relatedInstanceList = requestMap.requestDetails.relatedInstanceList

		//relatedInstanceList.each {
		//	if (it.relatedInstance.modelInfo.modelType == 'vnf') {
		//		vnfType = it.relatedInstance.modelInfo.modelName
		//		vnfId = it.relatedInstance.modelInfo.modelInvariantId
		//	}
		//}

		def vnfName = ''
		def asdcServiceModelInfo = ''

		def relatedInstanceList = requestMap.requestDetails?.relatedInstanceList


		if (relatedInstanceList != null) {
			relatedInstanceList.each {
				if (it.relatedInstance.modelInfo?.modelType == 'service') {
					asdcServiceModelInfo = it.relatedInstance.modelInfo?.modelVersion
				}
				if (it.relatedInstance.modelInfo.modelType == 'vnf') {
					vnfName = it.relatedInstance.instanceName ?: ''
				}
			}
		}

		def vnfType = execution.getVariable('vnfType')
		def vnfId = execution.getVariable('vnfId')

		def vfModuleId = execution.getVariable('vfModuleId')
		def volumeGroupId = execution.getVariable('volumeGroupId')
		def userParams = requestMap.requestDetails?.requestParameters?.userParams


		def userParamsNode = ''
		if(userParams != null) {
			userParamsNode = buildUserParams(userParams)
		}

		def isBaseVfModule = "false"
		if (execution.getVariable('isBaseVfModule') == true) {
			isBaseVfModule = "true"		
		}

		def requestId = execution.getVariable("mso-request-id")		
		def vfModuleName = requestMap.requestDetails?.requestInfo?.instanceName ?: ''
		def vfModuleModelName = requestMap.requestDetails?.modelInfo?.modelName ?: ''
		def suppressRollback = requestMap.requestDetails?.requestInfo?.suppressRollback

		def backoutOnFailure = ""
		if(suppressRollback != null){
			if ( suppressRollback == true) {
				backoutOnFailure = "false"
			} else if ( suppressRollback == false) {
				backoutOnFailure = "true"
			}
		}

		def serviceId = requestMap.requestDetails?.requestParameters?.serviceId ?: ''
		def aicCloudRegion = requestMap.requestDetails?.cloudConfiguration?.lcpCloudRegionId ?: ''
		def tenantId = requestMap.requestDetails?.cloudConfiguration?.tenantId ?: ''
		def personaModelId = requestMap.requestDetails?.modelInfo?.modelInvariantUuid ?: ''
		def personaModelVersion = requestMap.requestDetails?.modelInfo?.modelUuid ?: ''
		def modelCustomizationId = requestMap.requestDetails?.modelInfo?.modelCustomizationUuid ?: ''

		String xmlReq = """
		<vnf-request>
			<request-info>
				<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
				<action>${MsoUtils.xmlEscape(action)}</action>
				<source>VID</source>
				<service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
			</request-info>
			<vnf-inputs>
				<!-- not in use in 1610 -->
				<vnf-name>${MsoUtils.xmlEscape(vnfName)}</vnf-name>
				<vnf-type>${MsoUtils.xmlEscape(vnfType)}</vnf-type>
				<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
				<volume-group-id>${MsoUtils.xmlEscape(volumeGroupId)}</volume-group-id>
				<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
				<vf-module-name>${MsoUtils.xmlEscape(vfModuleName)}</vf-module-name>
				<vf-module-model-name>${MsoUtils.xmlEscape(vfModuleModelName)}</vf-module-model-name>
				<model-customization-id>${MsoUtils.xmlEscape(modelCustomizationId)}</model-customization-id>
				<is-base-vf-module>${MsoUtils.xmlEscape(isBaseVfModule)}</is-base-vf-module>
				<asdc-service-model-version>${MsoUtils.xmlEscape(asdcServiceModelInfo)}</asdc-service-model-version>
				<aic-cloud-region>${MsoUtils.xmlEscape(aicCloudRegion)}</aic-cloud-region>
				<tenant-id>${MsoUtils.xmlEscape(tenantId)}</tenant-id>
				<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
				<backout-on-failure>${MsoUtils.xmlEscape(backoutOnFailure)}</backout-on-failure>
				<persona-model-id>${MsoUtils.xmlEscape(personaModelId)}</persona-model-id>
				<persona-model-version>${MsoUtils.xmlEscape(personaModelVersion)}</persona-model-version>
			</vnf-inputs>
			<vnf-params>
				$userParamsNode
			</vnf-params>
		</vnf-request>
		"""

		// return a pretty-print of the volume-request xml without the preamble
		return groovy.xml.XmlUtil.serialize(xmlReq.normalize().replaceAll("\t", "").replaceAll("\n", "")).replaceAll("(<\\?[^<]*\\?>\\s*[\\r\\n]*)?", "") 
	}


}
