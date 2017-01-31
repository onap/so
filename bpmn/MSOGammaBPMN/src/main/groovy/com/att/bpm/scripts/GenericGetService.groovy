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

import static org.apache.commons.lang3.StringUtils.*;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution;
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.*

import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import org.springframework.web.util.UriUtils


/**
 * This class supports the GenericGetService Sub Flow.
 * This Generic sub flow can be used by any flow for accomplishing
 * the goal of getting a Service-Instance or Service-Subscription (from AAI).
 * The calling flow must set the GENGS_type variable as "service-instance"
 * or "service-subscription".
 *
 * When using to Get a Service-Instance:
 * If the global-customer-id and service-type are not provided
 * this flow executes a query to get the service- Url using the
 * Service  Id or Name (whichever is provided).
 *
 * When using to Get a Service-Subscription:
 * The global-customer-id and service-type must be
 * provided.
 *
 * Upon successful completion of this sub flow the
 * GENGS_SuccessIndicator will be true and the query response payload
 * will be set to GENGS_service.  An MSOWorkflowException will
 * be thrown upon unsuccessful completion or if an error occurs
 * at any time during this sub flow. Please map variables
 * to the corresponding variable names below.
 *
 * Note - If this sub flow receives a Not Found (404) response
 * from AAI at any time this will be considered an acceptable
 * successful response however the GENGS_FoundIndicator
 * will be set to false. This variable will allow the calling flow
 * to distinguish between the two Success scenarios,
 * "Success where service- is found" and
 * "Success where service- is NOT found".
 *
 *
 * Incoming Variables (Service-Instance):
 * @param - GENGS_serviceInstanceId or @param - GENGS_serviceInstanceName
 * @param - GENGS_type
 * @param (Optional) - GENGS_serviceType
 * @param (Optional) - GENGS_globalCustomerId
 *
 * Incoming Variables (Service-Subscription):
 * @param - GENGS_type
 * @param - GENGS_serviceType
 * @param - GENGS_globalCustomerId
 *
 *
 * Outgoing Variables:
 * @param - GENGS_service
 * @param - GENGS_SuccessIndicator
 * @param - GENGS_FoundIndicator
 * @param - WorkflowException
 */
class GenericGetService extends AbstractServiceTaskProcessor{

	String Prefix = "GENGS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * This method validates the incoming variables and
	 * determines the subsequent event based on which
	 * variables the calling flow provided.
	 *
	 * @param - execution
	 */
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericGetService PreProcessRequest Process*** ", isDebugEnabled)

		execution.setVariable("GENGS_obtainServiceInstanceUrl", false)
		execution.setVariable("GENGS_obtainServiceInstanceUrlByName", false)
		execution.setVariable("GENGS_SuccessIndicator", false)
		execution.setVariable("GENGS_FoundIndicator", false)
		execution.setVariable("GENGS_siResourceLink", null)

		try{
			// Get Variables
			String serviceInstanceId = execution.getVariable("GENGS_serviceInstanceId")
			String serviceInstanceName = execution.getVariable("GENGS_serviceInstanceName")
			String serviceType = execution.getVariable("GENGS_serviceType")
			String globalCustomerId = execution.getVariable("GENGS_globalCustomerId")
			String type = execution.getVariable("GENGS_type")

			if(type != null){
				utils.log("DEBUG", "Incoming GENGS_type is: " + type, isDebugEnabled)
				if(type.equalsIgnoreCase("service-instance")){
					if(isBlank(serviceInstanceId) && isBlank(serviceInstanceName)){
						utils.log("DEBUG", "Incoming serviceInstanceId and serviceInstanceName are null. ServiceInstanceId or ServiceInstanceName is required to Get a service-instance.", isDebugEnabled)
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming serviceInstanceId and serviceInstanceName are null. ServiceInstanceId or ServiceInstanceName is required to Get a service-instance.")
					}else{
						utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Service Instance Name is: " + serviceInstanceName, isDebugEnabled)
						if(isBlank(globalCustomerId) || isBlank(serviceType)){
							execution.setVariable("GENGS_obtainServiceInstanceUrl", true)
							if(isBlank(serviceInstanceId)){
								execution.setVariable("GENGS_obtainServiceInstanceUrlByName", true)
							}
						}else{
							utils.log("DEBUG", "Incoming Global Customer Id is: " + globalCustomerId, isDebugEnabled)
							utils.log("DEBUG", "Incoming Service Type is: " + serviceType, isDebugEnabled)
						}
					}
				}else if(type.equalsIgnoreCase("service-subscription")){
					if(isBlank(serviceType) || isBlank(globalCustomerId)){
						utils.log("DEBUG", "Incoming ServiceType or GlobalCustomerId is null. These variables are required to Get a service-subscription.", isDebugEnabled)
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming ServiceType or GlobalCustomerId is null. These variables are required to Get a service-subscription.")
					}else{
						utils.log("DEBUG", "Incoming Service Type is: " + serviceType, isDebugEnabled)
						utils.log("DEBUG", "Incoming Global Customer Id is: " + globalCustomerId, isDebugEnabled)
					}
				}else{
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Type is Invalid. Please Specify Type as service-instance or service-subscription")
				}
			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Incoming GENGS_type is null. Variable is Required.")
			}

		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", "Internal Error encountered within GenericGetService PreProcessRequest method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in GenericGetService PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED GenericGetService PreProcessRequest Process ***", isDebugEnabled)
	}

	/**
	 * This method obtains the Url to the provided service instance
	 * using the Service Instance Id.
	 *
	 * @param - execution
	 */
	public void obtainServiceInstanceUrlById(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericGetService ObtainServiceInstanceUrlById Process*** ", isDebugEnabled)
		try {
			String serviceInstanceId = execution.getVariable("GENGS_serviceInstanceId")
			utils.log("DEBUG", " Querying Node for Service-Instance URL by using Service-Instance Id: " + serviceInstanceId, isDebugEnabled)

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getSearchNodesQueryEndpoint(execution)
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			String path = "${aai_uri}?search-node-type=service-instance&filter=service-instance-id:EQUALS:${serviceInstanceId}"

			//String url = "${aai_endpoint}${path}"  host name needs to be removed from property
			String url = "${path}"
			execution.setVariable("GENGS_obtainSIUrlPath", url)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, url)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENGS_obtainSIUrlResponseCode", responseCode)
			utils.log("DEBUG", "  DELETE Service Instance response code is: " + responseCode, isDebugEnabled)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENGS_obtainSIUrlResponse", aaiResponse)

			//Process Response
			if(responseCode == 200){
				utils.log("DEBUG", "  Query for Service Instance Url Received a Good Response Code", isDebugEnabled)
				execution.setVariable("GENGS_SuccessIndicator", true)
				if(utils.nodeExists(aaiResponse, "result-data")){
					utils.log("DEBUG", "Query for Service Instance Url Response Does Contain Data" , isDebugEnabled)
					execution.setVariable("GENGS_FoundIndicator", true)
					String resourceLink = utils.getNodeText1(aaiResponse, "resource-link")
					execution.setVariable("GENGS_siResourceLink", resourceLink)
				}else{
					utils.log("DEBUG", "Query for Service Instance Url Response Does NOT Contains Data" , isDebugEnabled)
					execution.setVariable("WorkflowResponse", "  ") //for junits
				}
			}else if(responseCode == 404){
				utils.log("DEBUG", "  Query for Service Instance Received a Not Found (404) Response", isDebugEnabled)
				execution.setVariable("GENGS_SuccessIndicator", true)
				execution.setVariable("WorkflowResponse", "  ") //for junits
			}else{
				utils.log("DEBUG", "Query for Service Instance Received a BAD REST Response: \n" + aaiResponse, isDebugEnabled)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("ERROR", " Error encountered within GenericGetService ObtainServiceInstanceUrlById method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During ObtainServiceInstanceUrlById")
		}
		utils.log("DEBUG", " *** COMPLETED GenericGetService ObtainServiceInstanceUrlById Process*** ", isDebugEnabled)
	}

	/**
	 * This method obtains the Url to the provided service instance
	 * using the Service Instance Name.
	 *
	 * @param - execution
	 */
	public void obtainServiceInstanceUrlByName(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericGetService ObtainServiceInstanceUrlByName Process*** ", isDebugEnabled)
		try {
			String serviceInstanceName = execution.getVariable("GENGS_serviceInstanceName")
			utils.log("DEBUG", " Querying Node for Service-Instance URL by using Service-Instance Name " + serviceInstanceName, isDebugEnabled)

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getSearchNodesQueryEndpoint(execution)
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			String path = "${aai_uri}?search-node-type=service-instance&filter=service-instance-name:EQUALS:${serviceInstanceName}"

			//String url = "${aai_endpoint}${path}"  host name needs to be removed from property
			String url = "${path}"
			execution.setVariable("GENGS_obtainSIUrlPath", url)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, url)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENGS_obtainSIUrlResponseCode", responseCode)
			utils.log("DEBUG", "  DELETE Service Instance response code is: " + responseCode, isDebugEnabled)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENGS_obtainSIUrlResponse", aaiResponse)

			//Process Response
			if(responseCode == 200){
				utils.log("DEBUG", "  Query for Service Instance Url Received a Good Response Code", isDebugEnabled)
				execution.setVariable("GENGS_SuccessIndicator", true)
				if(utils.nodeExists(aaiResponse, "result-data")){
					utils.log("DEBUG", "Query for Service Instance Url Response Does Contain Data" , isDebugEnabled)
					execution.setVariable("GENGS_FoundIndicator", true)
					String resourceLink = utils.getNodeText1(aaiResponse, "resource-link")
					execution.setVariable("GENGS_siResourceLink", resourceLink)
				}else{
					utils.log("DEBUG", "Query for Service Instance Url Response Does NOT Contains Data" , isDebugEnabled)
					execution.setVariable("WorkflowResponse", "  ") //for junits
				}
			}else if(responseCode == 404){
				utils.log("DEBUG", "  Query for Service Instance Received a Not Found (404) Response", isDebugEnabled)
				execution.setVariable("GENGS_SuccessIndicator", true)
				execution.setVariable("WorkflowResponse", "  ") //for junits
			}else{
				utils.log("DEBUG", "Query for Service Instance Received a BAD REST Response: \n" + aaiResponse, isDebugEnabled)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("ERROR", " Error encountered within GenericGetService ObtainServiceInstanceUrlByName method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During ObtainServiceInstanceUrlByName")
		}
		utils.log("DEBUG", " *** COMPLETED GenericGetService ObtainServiceInstanceUrlByName Process*** ", isDebugEnabled)
	}


	/**
	 * This method executes a GET call to AAI to obtain the
	 * service-instance or service-subscription
	 *
	 * @param - execution
	 */
	public void getServiceObject(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericGetService GetServiceObject Process*** ", isDebugEnabled)
		try {
			String type = execution.getVariable("GENGS_type")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			String serviceEndpoint = ""

			if(type.equalsIgnoreCase("service-instance")){
				String siResourceLink = execution.getVariable("GENGS_siResourceLink")
				if(isBlank(siResourceLink)){
					String serviceInstanceId = execution.getVariable("GENGS_serviceInstanceId")
					utils.log("DEBUG", " Incoming GENGS_serviceInstanceId is: " + serviceInstanceId, isDebugEnabled)
					String serviceType = execution.getVariable("GENGS_serviceType")
					utils.log("DEBUG", " Incoming GENGS_serviceType is: " + serviceType, isDebugEnabled)
					String globalCustomerId = execution.getVariable("GENGS_globalCustomerId")
					utils.log("DEBUG", "Incoming Global Customer Id is: " + globalCustomerId, isDebugEnabled)

					String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
					logDebug('AAI URI is: ' + aai_uri, isDebugEnabled)
					serviceEndpoint = "${aai_uri}/" + UriUtils.encode(globalCustomerId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + UriUtils.encode(serviceInstanceId,"UTF-8")
				}else{
					utils.log("DEBUG", "Incoming Service Instance Resource Link is: " + siResourceLink, isDebugEnabled)
					String[] split = siResourceLink.split("/aai/")
					serviceEndpoint = "/aai/" + split[1]
				}
			}else if(type.equalsIgnoreCase("service-subscription")){
				String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
				String globalCustomerId = execution.getVariable("GENGS_globalCustomerId")
				String serviceType = execution.getVariable("GENGS_serviceType")
				serviceEndpoint = "${aai_uri}/" + UriUtils.encode(globalCustomerId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8")
			}

			String serviceUrl = "${aai_endpoint}" + serviceEndpoint

			execution.setVariable("GENGS_getServiceUrl", serviceUrl)
			utils.log("DEBUG", "GET Service AAI Path is: \n" + serviceUrl, isDebugEnabled)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, serviceUrl)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENGS_getServiceResponseCode", responseCode)
			utils.log("DEBUG", "  GET Service response code is: " + responseCode, isDebugEnabled)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENGS_getServiceResponse", aaiResponse)

			//Process Response
			if(responseCode == 200 || responseCode == 202){
				utils.log("DEBUG", "GET Service Received a Good Response Code", isDebugEnabled)
				if(utils.nodeExists(aaiResponse, "service-instance") || utils.nodeExists(aaiResponse, "service-subscription")){
					utils.log("DEBUG", "GET Service Response Contains a service-instance" , isDebugEnabled)
					execution.setVariable("GENGS_FoundIndicator", true)
					execution.setVariable("GENGS_service", aaiResponse)
					execution.setVariable("WorkflowResponse", aaiResponse)

				}else{
					utils.log("DEBUG", "GET Service Response Does NOT Contain Data" , isDebugEnabled)
				}
			}else if(responseCode == 404){
				utils.log("DEBUG", "GET Service Received a Not Found (404) Response", isDebugEnabled)
				execution.setVariable("WorkflowResponse", "  ") //for junits
			}
			else{
				utils.log("DEBUG", "  GET Service Received a Bad Response: \n" + aaiResponse, isDebugEnabled)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error encountered within GenericGetService GetServiceObject method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During GenericGetService")
		}
		utils.log("DEBUG", " *** COMPLETED GenericGetService GetServiceObject Process*** ", isDebugEnabled)
	}

}
