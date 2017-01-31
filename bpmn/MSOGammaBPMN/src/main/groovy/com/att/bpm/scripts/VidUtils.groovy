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

package com.att.bpm.scripts

import org.json.JSONObject;
import org.json.XML;

import org.openecomp.mso.bpmn.core.xml.XmlTool;

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.GPathResult
import groovy.xml.QName

class VidUtils {
	
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
		def rollbackEnabled = true
		if(suppressRollback != null && suppressRollback == true) {
			rollbackEnabled = false
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
				modelCustomizationName = it.relatedInstance.modelInfo?.modelCustomizationName
			}
		}
		
		vnfType = serviceName + '/' + modelCustomizationName
		
		def userParams = requestMap.requestDetails?.requestParameters?.userParams
		def userParamsNode = ''
		if(userParams != null) {
			userParamsNode = buildUserParams(userParams)
		}
		def xmlReq = """
		<volume-request xmlns="http://www.w3.org/2001/XMLSchema">
			<request-info>
				<action>${action}</action>
				<source>${requestMap.requestDetails.requestInfo.source}</source>
				<service-instance-id>${serviceInstanceId}</service-instance-id>
			</request-info>
			<volume-inputs>
				<volume-group-id>${volumeGroupId}</volume-group-id>
				<volume-group-name>${volGrpName}</volume-group-name>
				<vnf-type>${vnfType}</vnf-type>
				<vf-module-model-name>${requestMap.requestDetails.modelInfo.modelName}</vf-module-model-name>
				<asdc-service-model-version>${asdcServiceModelVersion}</asdc-service-model-version>
				<aic-cloud-region>${requestMap.requestDetails.cloudConfiguration.lcpCloudRegionId}</aic-cloud-region>
				<tenant-id>${requestMap.requestDetails.cloudConfiguration.tenantId}</tenant-id>
				<service-id>${serviceId}</service-id>
				<backout-on-failure>${rollbackEnabled}</backout-on-failure>
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
			xml += "<param name=\"${key}\">${value}</param>"
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
			def modelName = reqMap.requestDetails.modelInfo.modelName
			def lcpCloudRegionId = reqMap.requestDetails.cloudConfiguration.lcpCloudRegionId
			def tenantId = reqMap.requestDetails.cloudConfiguration.tenantId
			def serviceId = reqMap.requestDetails.requestInfo.productFamilyId 
			def backoutOnFailure = reqMap.requestDetails.requestInfo.suppressRollback
		
			//def userParams = reqMap.requestDetails.requestParameters.userParams
			//def userParamsNode = buildUserParams(userParams)
			def userParams = reqMap.requestDetails?.requestParameters?.userParams
			def userParamsNode = ''
			if(userParams != null) {
				userParamsNode = buildUserParams(userParams)
			}
		
			def xmlReq = """
			<network-request xmlns="http://www.w3.org/2001/XMLSchema"> 
			 <request-info> 
	            <request-id>${requestId}</request-id>
			 	<action>${requestAction}</action> 
			 	<source>VID</source> 
			 	<service-instance-id>${serviceInstanceId}</service-instance-id>
			 </request-info> 
			 <network-inputs> 
			 	<network-id>${networkId}</network-id> 
			 	<network-name>${instanceName}</network-name> 
			 	<network-type>${modelName}</network-type>
			 	<aic-cloud-region>${lcpCloudRegionId}</aic-cloud-region> 
			 	<tenant-id>${tenantId}</tenant-id>
			 	<service-id>${serviceId}</service-id> 
			 	<backout-on-failure>${backoutOnFailure}</backout-on-failure>
			 </network-inputs> 
			 <network-params>
				${userParamsNode}
			 </network-params> 
			</network-request>
			"""
			// return a pretty-print of the volume-request xml without the preamble
			return groovy.xml.XmlUtil.serialize(xmlReq.normalize().replaceAll("\t", "").replaceAll("\n", "")).replaceAll("(<\\?[^<]*\\?>\\s*[\\r\\n]*)?", "")
			
		} catch(Exception e) {
			throw e
		}
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
		
		def requestId = execution.getVariable("att-mso-request-id")		
		def vfModuleName = requestMap.requestDetails?.requestInfo?.instanceName ?: ''
		def vfModuleModelName = requestMap.requestDetails?.modelInfo?.modelName ?: ''
		def suppressRollback = requestMap.requestDetails?.requestInfo?.suppressRollback
		def backoutOnFailure = "true"
		if(suppressRollback != null && suppressRollback == true) {
			backoutOnFailure = "false"
		}
		
		def serviceId = requestMap.requestDetails?.requestParameters?.serviceId ?: ''
		def aicCloudRegion = requestMap.requestDetails?.cloudConfiguration?.lcpCloudRegionId ?: ''
		def tenantId = requestMap.requestDetails?.cloudConfiguration?.tenantId ?: ''
		def personaModelId = requestMap.requestDetails?.modelInfo?.modelInvariantId ?: ''
		def personaModelVersion = requestMap.requestDetails?.modelInfo?.modelVersion ?: ''
		
		def xmlReq = """
		<vnf-request>
			<request-info>
				<request-id>${requestId}</request-id>
				<action>${action}</action>
				<source>VID</source>
				<!-- new 1610 field -->
				<service-instance-id>${serviceInstanceId}</service-instance-id>
			</request-info>
			<vnf-inputs>
				<!-- not in use in 1610 -->
				<vnf-name>${vnfName}</vnf-name>					
				<vnf-type>${vnfType}</vnf-type>
				<vnf-id>${vnfId}</vnf-id>
				<volume-group-id>${volumeGroupId}</volume-group-id>
				<vf-module-id>${vfModuleId}</vf-module-id>
				<vf-module-name>${vfModuleName}</vf-module-name>				
				<vf-module-model-name>${vfModuleModelName}</vf-module-model-name>
				<is-base-vf-module>${isBaseVfModule}</is-base-vf-module>
				<asdc-service-model-version>${asdcServiceModelInfo}</asdc-service-model-version>
				<aic-cloud-region>${aicCloudRegion}</aic-cloud-region>				
				<tenant-id>${tenantId}</tenant-id>
				<service-id>${serviceId}</service-id>
				<backout-on-failure>${backoutOnFailure}</backout-on-failure>
				<persona-model-id>${personaModelId}</persona-model-id>
				<persona-model-version>${personaModelVersion}</persona-model-version>
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
