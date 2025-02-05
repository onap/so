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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.onap.so.bpmn.core.domain.*
import org.onap.so.bpmn.core.json.JsonUtils
import org.apache.commons.lang3.StringUtils

import static  org.onap.so.bpmn.common.scripts.GenericUtils.*

import java.time.Duration

import org.slf4j.Logger
import org.slf4j.LoggerFactory



class SniroUtils{
    private static final Logger logger = LoggerFactory.getLogger( SniroUtils.class);


	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	private AbstractServiceTaskProcessor utils

	public MsoUtils msoUtils = new MsoUtils()

	public SniroUtils(AbstractServiceTaskProcessor taskProcessor) {
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
		logger.debug("Started Building Sniro Request")
		def callbackUrl = utils.createWorkflowMessageAdapterCallbackURL(execution, "SNIROResponse", requestId)
		def transactionId = requestId
		//ServiceInstance Info
		ServiceInstance serviceInstance = decomposition.getServiceInstance()
		def serviceInstanceId
		if(serviceInstance == null){
			logger.debug("Unable to obtain Service Instance Id, ServiceInstance Object is null" )
			exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Internal Error - Unable to obtain Service Instance Id, ServiceInstance Object is null")
		}else{
			serviceInstanceId = serviceInstance.getInstanceId()
		}
		String type = decomposition.getServiceType()
		String serviceType = ""
		if(isNotBlank(type)){
			serviceType = """ "serviceType": "${type}", """
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
		//TODO Implement better way to determine this
		String requestType = "initial"
		List<Resource> resources = decomposition.getServiceResources()
		for(Resource r:resources){
			HomingSolution currentSolution = r.getCurrentHomingSolution()
			if(currentSolution != null){
				String indicator = currentSolution.getServiceInstanceId()
				if(indicator != null){
					requestType = "speed changed"
				}
			}
		}

		String timeout = execution.getVariable("timeout")
		Duration d = Duration.parse(timeout);
		long timeoutSeconds = d.getSeconds();

		//Demands
		String placementDemands = ""
		StringBuilder sb = new StringBuilder()
		List<Resource> resourceList = decomposition.getAllottedResources()
		List<VnfResource> vnfResourceList = decomposition.getVnfResources()

		//TODO should be service agnostic so this is just a temp solution to all vnfs to be sent in placement container for adiod
		if(resourceList.isEmpty() || resourceList == null){
			search : {
				for(VnfResource vnf : vnfResourceList){
					if(StringUtils.containsIgnoreCase(vnf.getNfRole(), "vce") || StringUtils.containsIgnoreCase(vnf.getNfRole(), "vpe")){
						resourceList = decomposition.getVnfResources()
						break search
					}
				}
			}
		}

		if(resourceList.isEmpty() || resourceList == null){
			logger.debug("Resources List is Empty")
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
			logger.debug("Vnf Resources List is Empty")
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
					""" ,"existingLicense": {
                                 "entitlementPoolUUID":
									${entitlementPoolList},
                                 "licenseKeyGroupUUID":
									${licenseKeyGroupList}

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
			${serviceType}
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

		logger.debug("Completed Building Sniro Request")
		return request
	}

	/**
	 * This method validates the async callback response from Sniro.
	 * If the response contains an exception the method will build
	 * and throw a workflow exception.
	 *
	 * @param execution
	 * @param response - sniro async response
	 *
	 * @author cb645j
	 */
	//TODO needs updating per sniro changes
	public void validateCallbackResponse(DelegateExecution execution, String response){
		try{
			String placements = ""
			String licenses = ""
			if(isBlank(response)){
				exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Sniro Async Callback Response is Empty")
			}else{
				if(JsonUtils.jsonElementExist(response, "solutionInfo")){
					if(JsonUtils.jsonElementExist(response, "solutionInfo.placementInfo")){
						placements = jsonUtil.getJsonValue(response, "solutionInfo.placementInfo")
					}
					if(JsonUtils.jsonElementExist(response, "solutionInfo.licenseInfo")){
						licenses = jsonUtil.getJsonValue(response, "solutionInfo.licenseInfo")
					}
					if((isBlank(placements) || placements.equalsIgnoreCase("[]")) && (isBlank(licenses) || licenses.equalsIgnoreCase("[]"))){
						logger.debug("Sniro Async Response does not contain: licenses or placements")
					}else{
						return
					}
				}else if(JsonUtils.jsonElementExist(response, "requestError") == true){
					String errorMessage = ""
					if(response.contains("policyException")){
						String text = jsonUtil.getJsonValue(response, "requestError.policyException.text")
						errorMessage = "Sniro Async Response contains a policy error: " + text
					}else if(response.contains("serviceException")){
						String text = jsonUtil.getJsonValue(response, "requestError.serviceException.text")
						errorMessage = "Sniro Async Response contains a service error: " + text
					}else{
						errorMessage = "Sniro Async Response contains an error: not provided"
					}
					logger.debug("Sniro Async Response contains an error: " + errorMessage)
					exceptionUtil.buildAndThrowWorkflowException(execution, 400, errorMessage)

				}else{
					logger.debug("Sniro Async Response contains an error: not provided")
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Sniro Async Response contains an error: not provided")
				}
			}
		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			logger.debug("Error encountered within Homing validateCallbackResponse method: " + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in Sniro Homing Validate Async Response")
		}
	}

}
