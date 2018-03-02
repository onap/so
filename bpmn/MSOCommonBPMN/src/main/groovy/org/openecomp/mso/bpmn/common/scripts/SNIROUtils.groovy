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

package org.openecomp.mso.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.MsoUtils
import org.openecomp.mso.bpmn.core.domain.*
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.apache.commons.lang3.StringUtils

import static  org.openecomp.mso.bpmn.common.scripts.GenericUtils.*

class SNIROUtils{

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	private AbstractServiceTaskProcessor utils

	public MsoUtils msoUtils = new MsoUtils()

	public SNIROUtils(AbstractServiceTaskProcessor taskProcessor) {
		this.utils = taskProcessor
	}

	/**
	 * This method builds the service-agnostic
	 * sniro json request to get a homing solution
	 * and license solution
	 *
	 * @param execution
	 * @param requestId
	 * @param decomposition - ServiceDecomposition object
	 * @param subscriber - Subscriber information
	 * @param homingParams - Homing/Request parameters
	 *
	 * @return request - sniro v2 payload
	 *
	 * @author cb645j
	 */
	public String buildRequest(DelegateExecution execution, String requestId, ServiceDecomposition decomposition, Subscriber subscriber, String homingParams){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", "Started Building Sniro Request", isDebugEnabled)
		def callbackUrl = utils.createWorkflowMessageAdapterCallbackURL(execution, "SNIROResponse", requestId)
		def transactionId = requestId
		//ServiceInstance Info
		ServiceInstance serviceInstance = decomposition.getServiceInstance()
		def serviceInstanceId
		if(serviceInstance == null){
			utils.log("DEBUG", "Unable to obtain Service Instance Id, ServiceInstance Object is null" , isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Internal Error - Unable to obtain Service Instance Id, ServiceInstance Object is null")
		}else{
			serviceInstanceId = serviceInstance.getInstanceId()
		}
		//Model Info
		ModelInfo model = decomposition.getModelInfo()
		String modelType = model.getModelType()
		String modelInvariantId = model.getModelInvariantUuid()
		String modelVersionId = model.getModelUuid()
		String modelName = model.getModelName()
		String modelVersion = model.getModelVersion()
		//Subscriber Info
		String subscriberId = subscriber.getGlobalId()
		String subscriberName = subscriber.getName()
		String commonSiteId = subscriber.getCommonSiteId()
		//OrderInfo
		String orderInfo
		if(!isBlank(homingParams)){
			orderInfo = homingParams.replaceAll("\"", "\\\\\"").replaceAll("\n", "").replaceAll("\r", "")
			orderInfo = StringUtils.normalizeSpace(orderInfo)
		}

		//Determine RequestType
		//TODO Figure out better way to determine this
		String requestType = "initial"
		List<Resource> resources = decomposition.getServiceResources()
		for(Resource r:resources){
			HomingSolution currentSolution = r.getCurrentHomingSolution()
			if(currentSolution != null){
				requestType = "speed changed"
			}
		}

		int timeoutSeconds = 1800
		String timeout = execution.getVariable("timeout")
		if(isNotBlank(timeout)){
			String subT = timeout.substring(2, timeout.length() - 1)
			int timeoutInt = Integer.parseInt(subT)
			timeoutSeconds = timeoutInt * 60
		}

		//Demands
		String placementDemands = ""
		StringBuilder sb = new StringBuilder()
		List<Resource> resourceList = decomposition.getServiceAllottedResources()
		List<VnfResource> vnfResourceList = decomposition.getServiceVnfs()

		// TODO: We should include both alloted resources and service resources in the placementDeamnds- not one or the other.
		if(resourceList.isEmpty() || resourceList == null){
			utils.log("DEBUG", "Allotted Resources List is empty - will try to get service VNFs instead.", isDebugEnabled)
			resourceList = decomposition.getServiceVnfs()
		}

		if(resourceList.isEmpty() || resourceList == null){
			utils.log("DEBUG", "Resources List is Empty", isDebugEnabled)
		}else{
			for(Resource resource:resourceList){
				ModelInfo resourceModelInfo = resource.getModelInfo()
				ResourceInstance resourceInstance = resource.getResourceInstance()
				def resourceInstanceType = resource.getResourceType()
				def serviceResourceId = resource.getResourceId() //TODO - resourceId versus instanceId - should be what is put in AAI, whatever we put here will be what is in response, used to correlate
				def resourceModuleName = resourceModelInfo.getModelInstanceName()
				def resouceModelCustomizationId = resourceModelInfo.getModelCustomizationUuid()
				def resouceModelInvariantId = resourceModelInfo.getModelInvariantUuid()
				def resouceModelName = resourceModelInfo.getModelName()
				def resouceModelVersion = resourceModelInfo.getModelVersion()
				def resouceModelVersionId = resourceModelInfo.getModelUuid()
				def resouceModelType = resourceModelInfo.getModelType()
				def tenantId = "" //Optional
				def tenantName = "" //Optional


				String existingPlacement = ""
				HomingSolution currentPlacement = resource.getCurrentHomingSolution()
				if(currentPlacement != null){
					String homedServiceInstanceId = currentPlacement.getServiceInstanceId()
					existingPlacement =
					""","existingPlacement": {
                           "serviceInstanceId": "${homedServiceInstanceId}"
                    }"""
				}


				String demand =
					"""{
						"resourceInstanceType": "${resourceInstanceType}",
						"serviceResourceId": "${serviceResourceId}",
						"resourceModuleName": "${resourceModuleName}",
						"resourceModelInfo": {
							"modelCustomizationId": "${resouceModelCustomizationId}",
							"modelInvariantId": "${resouceModelInvariantId}",
							"modelName": "${resouceModelName}",
							"modelVersion": "${resouceModelVersion}",
							"modelVersionId": "${resouceModelVersionId}",
							"modelType": "${resouceModelType}"
						},
						"tenantId": "${tenantId}",
						"tenantName": "${tenantName}"
						${existingPlacement}
					},"""

				placementDemands = sb.append(demand)
			}
			placementDemands = placementDemands.substring(0, placementDemands.length() - 1);
		}

		String licenseDemands = ""
		sb = new StringBuilder()
		if(vnfResourceList.isEmpty() || vnfResourceList == null){
			utils.log("DEBUG", "Vnf Resources List is Empty", isDebugEnabled)
		}else{
			for(VnfResource vnfResource:vnfResourceList){
				ModelInfo vnfResourceModelInfo = vnfResource.getModelInfo()
				ResourceInstance vnfResourceInstance = vnfResource.getResourceInstance()
				def resourceInstanceType = vnfResource.getResourceType()
				def serviceResourceId = vnfResource.getResourceId()
				def resourceModuleName = vnfResourceModelInfo.getModelInstanceName()
				def resouceModelCustomizationId = vnfResourceModelInfo.getModelCustomizationUuid()
				def resouceModelInvariantId = vnfResourceModelInfo.getModelInvariantUuid()
				def resouceModelName = vnfResourceModelInfo.getModelName()
				def resouceModelVersion = vnfResourceModelInfo.getModelVersion()
				def resouceModelVersionId = vnfResourceModelInfo.getModelUuid()
				def resouceModelType = vnfResourceModelInfo.getModelType()

				String curentLicenseJson = ""
				HomingSolution currentSol = vnfResource.getCurrentHomingSolution()
				if(currentSol != null){
					JSONArray entitlementPoolList = currentSol.getLicense().getEntitlementPoolListAsString()
					JSONArray licenseKeyGroupList = currentSol.getLicense().getLicenseKeyGroupListAsString()
					curentLicenseJson =
					""" ,"existingLicense": [
                           {
                                 "entitlementPoolUUID":
									${entitlementPoolList},
                                 "licenseKeyGroupUUID":
									${licenseKeyGroupList}

                           }
                    	]"""
				}

				String demand =
				"""{
						"resourceInstanceType": "${resourceInstanceType}",
						"serviceResourceId": "${serviceResourceId}",
						"resourceModuleName": "${resourceModuleName}",
						"resourceModelInfo": {
							"modelCustomizationId": "${resouceModelCustomizationId}",
							"modelInvariantId": "${resouceModelInvariantId}",
							"modelName": "${resouceModelName}",
							"modelVersion": "${resouceModelVersion}",
							"modelVersionId": "${resouceModelVersionId}",
							"modelType": "${resouceModelType}"
						}
						${curentLicenseJson}
					},"""

					licenseDemands = sb.append(demand)
			}
			licenseDemands = licenseDemands.substring(0, licenseDemands.length() - 1);
		}

		String request =
				"""{
	  	"requestInfo": {
				"transactionId": "${transactionId}",
				"requestId": "${requestId}",
				"callbackUrl": "${callbackUrl}",
				"sourceId": "mso",
				"requestType": "${requestType}",
				"optimizer": [
					"placement",
					"license"
				],
				"numSolutions": 1,
				"timeout": ${timeoutSeconds}
				},
		"placementInfo": {
			"serviceModelInfo": {
				"modelType": "${modelType}",
				"modelInvariantId": "${modelInvariantId}",
				"modelVersionId": "${modelVersionId}",
				"modelName": "${modelName}",
				"modelVersion": "${modelVersion}"
				},
			"subscriberInfo": {
				"globalSubscriberId": "${subscriberId}",
				"subscriberName": "${subscriberName}",
				"subscriberCommonSiteId": "${commonSiteId}"
				},
			"demandInfo": {
				"placementDemand": [
					${placementDemands}
				],
				"licenseDemand": [
					${licenseDemands}
				]
			},
			"policyId": [],
			"serviceInstanceId": "${serviceInstanceId}",
			"orderInfo": "{\\\"requestParameters\\\": ${orderInfo}}"
		}
	  }"""

	  	utils.log("DEBUG", "Completed Building Sniro Request", isDebugEnabled)
		return request
	}

	/**
	 * This method validates the callback response
	 * from Sniro. If the response contains an
	 * exception the method will build and throw
	 * a workflow exception.
	 *
	 * @param execution
	 * @param response - the async callback response from sniro
	 *
	 * @author cb645j
	 */
	public void validateCallbackResponse(DelegateExecution execution, String response){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String placements = ""
		if(isBlank(response)){
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Sniro Async Callback Response is Empty")
		}else{
			if(JsonUtils.jsonElementExist(response, "solutionInfo.placementInfo")){
				placements = jsonUtil.getJsonValue(response, "solutionInfo.placementInfo")
				if(isBlank(placements) || placements.equalsIgnoreCase("[]")){
					String statusMessage = jsonUtil.getJsonValue(response, "statusMessage")
					if(isBlank(statusMessage)){
						utils.log("DEBUG", "Error Occured in Homing: Sniro Async Callback Response does not contain placement solution.", isDebugEnabled)
						exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Sniro Async Callback Response does not contain placement solution.")
					}else{
						utils.log("DEBUG", "Error Occured in Homing: " + statusMessage, isDebugEnabled)
						exceptionUtil.buildAndThrowWorkflowException(execution, 400, statusMessage)
					}
				}else{
					return
				}
			}else if(JsonUtils.jsonElementExist(response, "requestError") == true){
				String errorMessage = ""
				if(response.contains("policyException")){
					String text = jsonUtil.getJsonValue(response, "requestError.policyException.text")
					errorMessage = "Sniro Async Callback Response contains a Request Error Policy Exception: " + text
				}else if(response.contains("serviceException")){
					String text = jsonUtil.getJsonValue(response, "requestError.serviceException.text")
					errorMessage = "Sniro Async Callback Response contains a Request Error Service Exception: " + text
				}else{
					errorMessage = "Sniro Async Callback Response contains a Request Error. Unable to determine the Request Error Exception."
				}
				utils.log("DEBUG", "Error Occured in Homing: " + errorMessage, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 400, errorMessage)

			}else{
				utils.log("DEBUG", "Error Occured in Homing: Received an Unknown Async Callback Response from Sniro.", isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Received an Unknown Async Callback Response from Sniro.")
			}
		}

	}


}