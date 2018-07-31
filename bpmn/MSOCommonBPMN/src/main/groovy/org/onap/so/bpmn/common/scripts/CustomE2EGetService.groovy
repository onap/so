/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved. 
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

import static org.apache.commons.lang3.StringUtils.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.rest.APIResponse
import org.springframework.web.util.UriUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger


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
 * Variable Mapping Below:
 *
 * In Mapping Variables:
 *   For Allotted-Resource:
 *     @param - GENGS_allottedResourceId
 *     @param - GENGS_type
 *     @param (Optional) - GENGS_serviceInstanceId
 *     @param (Optional) - GENGS_serviceType
 *     @param (Optional) - GENGS_globalCustomerId
 *
 *   For Service-Instance:
 *     @param - GENGS_serviceInstanceId or @param - GENGS_serviceInstanceName
 *     @param - GENGS_type
 *     @param (Optional) - GENGS_serviceType
 *     @param (Optional) - GENGS_globalCustomerId
 *
 *   For Service-Subscription:
 *     @param - GENGS_type
 *     @param - GENGS_serviceType
 *     @param - GENGS_globalCustomerId
 *
 *
 * Out Mapping Variables:
 *    @param - GENGS_service
 *    @param - GENGS_FoundIndicator
 *    @param - WorkflowException
 */
class CustomE2EGetService extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CustomE2EGetService.class);

	String Prefix = "GENGS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * This method validates the incoming variables and
	 * determines the subsequent event based on which
	 * variables the calling flow provided.
	 *
	 * @param - execution
	 *
	 */
	public void preProcessRequest(DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericGetService PreProcessRequest Process")

		execution.setVariable("GENGS_obtainObjectsUrl", false)
		execution.setVariable("GENGS_obtainServiceInstanceUrlByName", false)
		execution.setVariable("GENGS_SuccessIndicator", false)
		execution.setVariable("GENGS_FoundIndicator", false)
		execution.setVariable("GENGS_resourceLink", null)
		execution.setVariable("GENGS_siResourceLink", null)

		try{
			// Get Variables
			String allottedResourceId = execution.getVariable("GENGS_allottedResourceId")
			String serviceInstanceId = execution.getVariable("GENGS_serviceInstanceId")
			String serviceInstanceName = execution.getVariable("GENGS_serviceInstanceName")
			String serviceType = execution.getVariable("GENGS_serviceType")
			String globalCustomerId = execution.getVariable("GENGS_globalCustomerId")
			String type = execution.getVariable("GENGS_type")

			if(type != null){
				msoLogger.debug("Incoming GENGS_type is: " + type)
				if(type.equalsIgnoreCase("allotted-resource")){
					if(isBlank(allottedResourceId)){
						msoLogger.debug("Incoming allottedResourceId is null. Allotted Resource Id is required to Get an allotted-resource.")
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming allottedResourceId is null. Allotted Resource Id is required to Get an allotted-resource.")
					}else{
						msoLogger.debug("Incoming Allotted Resource Id is: " + allottedResourceId)
						if(isBlank(globalCustomerId) || isBlank(serviceType) || isBlank(serviceInstanceId)){
							execution.setVariable("GENGS_obtainObjectsUrl", true)
						}else{
							msoLogger.debug("Incoming Service Instance Id is: " + serviceInstanceId)
							msoLogger.debug("Incoming Service Type is: " + serviceType)
							msoLogger.debug("Incoming Global Customer Id is: " + globalCustomerId)
						}
					}
				}else if(type.equalsIgnoreCase("service-instance")){
					if(isBlank(serviceInstanceId) && isBlank(serviceInstanceName)){
						msoLogger.debug("Incoming serviceInstanceId and serviceInstanceName are null. ServiceInstanceId or ServiceInstanceName is required to Get a service-instance.")
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming serviceInstanceId and serviceInstanceName are null. ServiceInstanceId or ServiceInstanceName is required to Get a service-instance.")
					}else{
						msoLogger.debug("Incoming Service Instance Id is: " + serviceInstanceId)
						msoLogger.debug("Incoming Service Instance Name is: " + serviceInstanceName)
						if(isBlank(globalCustomerId) || isBlank(serviceType)){
							execution.setVariable("GENGS_obtainObjectsUrl", true)
							if(isBlank(serviceInstanceId)){
								execution.setVariable("GENGS_obtainServiceInstanceUrlByName", true)
							}
						}else{
							msoLogger.debug("Incoming Global Customer Id is: " + globalCustomerId)
							msoLogger.debug("Incoming Service Type is: " + serviceType)
						}
					}
				}else if(type.equalsIgnoreCase("service-subscription")){
					if(isBlank(serviceType) || isBlank(globalCustomerId)){
						msoLogger.debug("Incoming ServiceType or GlobalCustomerId is null. These variables are required to Get a service-subscription.")
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming ServiceType or GlobalCustomerId is null. These variables are required to Get a service-subscription.")
					}else{
						msoLogger.debug("Incoming Service Type is: " + serviceType)
						msoLogger.debug("Incoming Global Customer Id is: " + globalCustomerId)
					}
				}else{
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Type is Invalid. Please Specify Type as service-instance or service-subscription")
				}
			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Incoming GENGS_type is null. Variable is Required.")
			}

		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.debug("Internal Error encountered within GenericGetService PreProcessRequest method!" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in GenericGetService PreProcessRequest")

		}
		msoLogger.trace("COMPLETED GenericGetService PreProcessRequest Process ")
	}

	/**
	 * This method obtains the Url to the provided service instance
	 * using the Service Instance Id.
	 *
	 * @param - execution
	 */
	public void obtainServiceInstanceUrlById(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericGetService ObtainServiceInstanceUrlById Process")
		try {
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getSearchNodesQueryEndpoint(execution)
			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)

			String type = execution.getVariable("GENGS_type")
			String path = ""
			if(type.equalsIgnoreCase("service-instance")){
				String serviceInstanceId = execution.getVariable("GENGS_serviceInstanceId")
				msoLogger.debug(" Querying Node for Service-Instance URL by using Service-Instance Id: " + serviceInstanceId)
				path = "${aai_uri}?search-node-type=service-instance&filter=service-instance-id:EQUALS:${serviceInstanceId}"
				msoLogger.debug("Service Instance Node Query Url is: " + path)
				msoLogger.debug("Service Instance Node Query Url is: " + path)
			}else if(type.equalsIgnoreCase("allotted-resource")){
				String allottedResourceId = execution.getVariable("GENGS_allottedResourceId")
				msoLogger.debug(" Querying Node for Service-Instance URL by using Allotted Resource Id: " + allottedResourceId)
				path = "${aai_uri}?search-node-type=allotted-resource&filter=id:EQUALS:${allottedResourceId}"
				msoLogger.debug("Allotted Resource Node Query Url is: " + path)
				msoLogger.debug("Allotted Resource Node Query Url is: " + path)
			}

			//String url = "${aai_endpoint}${path}"  host name needs to be removed from property
			String url = "${path}"
			execution.setVariable("GENGS_genericQueryPath", url)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, url)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENGS_genericQueryResponseCode", responseCode)
			msoLogger.debug("  GET Service Instance response code is: " + responseCode)
			msoLogger.debug("GenericGetService AAI GET Response Code: " + responseCode)

			String aaiResponse = response.getResponseBodyAsString()
			execution.setVariable("GENGS_obtainSIUrlResponseBeforeUnescaping", aaiResponse)
			msoLogger.debug("GenericGetService AAI Response before unescaping: " + aaiResponse)
			execution.setVariable("GENGS_genericQueryResponse", aaiResponse)
			msoLogger.debug("GenericGetService AAI Response: " + aaiResponse)
			msoLogger.debug("GenericGetService AAI Response: " + aaiResponse)

			//Process Response
			if(responseCode == 200){
				msoLogger.debug("Generic Query Received a Good Response Code")
				execution.setVariable("GENGS_SuccessIndicator", true)
				if(utils.nodeExists(aaiResponse, "result-data")){
					msoLogger.debug("Generic Query Response Does Contain Data" )
					execution.setVariable("GENGS_FoundIndicator", true)
					String resourceLink = utils.getNodeText(aaiResponse, "resource-link")
					execution.setVariable("GENGS_resourceLink", resourceLink)
					execution.setVariable("GENGS_siResourceLink", resourceLink)
				}else{
					msoLogger.debug("Generic Query Response Does NOT Contains Data" )
					execution.setVariable("WorkflowResponse", "  ") //for junits
				}
			}else if(responseCode == 404){
				msoLogger.debug("Generic Query Received a Not Found (404) Response")
				execution.setVariable("GENGS_SuccessIndicator", true)
				execution.setVariable("WorkflowResponse", "  ") //for junits
			}else{
				msoLogger.debug("Generic Query Received a BAD REST Response: \n" + aaiResponse)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Error encountered within GenericGetService ObtainServiceInstanceUrlById method!" + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During ObtainServiceInstanceUrlById")
		}
		msoLogger.trace("COMPLETED GenericGetService ObtainServiceInstanceUrlById Process")
	}

	/**
	 * This method obtains the Url to the provided service instance
	 * using the Service Instance Name.
	 *
	 * @param - execution
	 */
	public void obtainServiceInstanceUrlByName(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericGetService ObtainServiceInstanceUrlByName Process")
		try {
			String serviceInstanceName = execution.getVariable("GENGS_serviceInstanceName")
			msoLogger.debug(" Querying Node for Service-Instance URL by using Service-Instance Name " + serviceInstanceName)

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getSearchNodesQueryEndpoint(execution)
			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			String path = "${aai_uri}?search-node-type=service-instance&filter=service-instance-name:EQUALS:${serviceInstanceName}"

			//String url = "${aai_endpoint}${path}"  host name needs to be removed from property
			String url = "${path}"
			execution.setVariable("GENGS_obtainSIUrlPath", url)

			msoLogger.debug("GenericGetService AAI Endpoint: " + aai_endpoint)
			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, url)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENGS_obtainSIUrlResponseCode", responseCode)
			msoLogger.debug("  GET Service Instance response code is: " + responseCode)
			msoLogger.debug("GenericGetService AAI Response Code: " + responseCode)

			String aaiResponse = response.getResponseBodyAsString()
			execution.setVariable("GENGS_obtainSIUrlResponse", aaiResponse)
			msoLogger.debug("GenericGetService AAI Response: " + aaiResponse)
			//Process Response
			if(responseCode == 200){
				msoLogger.debug("  Query for Service Instance Url Received a Good Response Code")
				execution.setVariable("GENGS_SuccessIndicator", true)
				if(utils.nodeExists(aaiResponse, "result-data")){
					msoLogger.debug("Query for Service Instance Url Response Does Contain Data" )
					execution.setVariable("GENGS_FoundIndicator", true)
					String resourceLink = utils.getNodeText(aaiResponse, "resource-link")
					execution.setVariable("GENGS_resourceLink", resourceLink)
					execution.setVariable("GENGS_siResourceLink", resourceLink)
				}else{
					msoLogger.debug("Query for Service Instance Url Response Does NOT Contains Data" )
					execution.setVariable("WorkflowResponse", "  ") //for junits
				}
			}else if(responseCode == 404){
				msoLogger.debug("  Query for Service Instance Received a Not Found (404) Response")
				execution.setVariable("GENGS_SuccessIndicator", true)
				execution.setVariable("WorkflowResponse", "  ") //for junits
			}else{
				msoLogger.debug("Query for Service Instance Received a BAD REST Response: \n" + aaiResponse)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Error encountered within GenericGetService ObtainServiceInstanceUrlByName method!" + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During ObtainServiceInstanceUrlByName")
		}
		msoLogger.trace("COMPLETED GenericGetService ObtainServiceInstanceUrlByName Process")
	}


	/**
	 * This method executes a GET call to AAI to obtain the
	 * service-instance or service-subscription
	 *
	 * @param - execution
	 */
	public void getServiceObject(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericGetService GetServiceObject Process")
		try {
			String type = execution.getVariable("GENGS_type")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			String serviceEndpoint = ""

			msoLogger.debug("GenericGetService getServiceObject AAI Endpoint: " + aai_endpoint)
			if(type.equalsIgnoreCase("service-instance")){
				String siResourceLink = execution.getVariable("GENGS_resourceLink")
				if(isBlank(siResourceLink)){
					String serviceInstanceId = execution.getVariable("GENGS_serviceInstanceId")
					msoLogger.debug(" Incoming GENGS_serviceInstanceId is: " + serviceInstanceId)
					String serviceType = execution.getVariable("GENGS_serviceType")
					msoLogger.debug(" Incoming GENGS_serviceType is: " + serviceType)
					String globalCustomerId = execution.getVariable("GENGS_globalCustomerId")
					msoLogger.debug("Incoming Global Customer Id is: " + globalCustomerId)

					String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
					msoLogger.debug('AAI URI is: ' + aai_uri)
					serviceEndpoint = "${aai_uri}/" + UriUtils.encode(globalCustomerId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + UriUtils.encode(serviceInstanceId,"UTF-8")
				}else{
					msoLogger.debug("Incoming Service Instance Url is: " + siResourceLink)
					String[] split = siResourceLink.split("/aai/")
					serviceEndpoint = "/aai/" + split[1]
				}
			}else if(type.equalsIgnoreCase("allotted-resource")){
				String siResourceLink = execution.getVariable("GENGS_resourceLink")
				if(isBlank(siResourceLink)){
					String allottedResourceId = execution.getVariable("GENGS_allottedResourceId")
					msoLogger.debug(" Incoming GENGS_allottedResourceId is: " + allottedResourceId)
					String serviceInstanceId = execution.getVariable("GENGS_serviceInstanceId")
					msoLogger.debug(" Incoming GENGS_serviceInstanceId is: " + serviceInstanceId)
					String serviceType = execution.getVariable("GENGS_serviceType")
					msoLogger.debug(" Incoming GENGS_serviceType is: " + serviceType)
					String globalCustomerId = execution.getVariable("GENGS_globalCustomerId")
					msoLogger.debug("Incoming Global Customer Id is: " + globalCustomerId)

					String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
					msoLogger.debug('AAI URI is: ' + aai_uri)
					serviceEndpoint = "${aai_uri}/" + UriUtils.encode(globalCustomerId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + UriUtils.encode(serviceInstanceId,"UTF-8") +  "/allotted-resources/allotted-resource/" + UriUtils.encode(allottedResourceId,"UTF-8")
				}else{
					msoLogger.debug("Incoming Allotted-Resource Url is: " + siResourceLink)
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
			msoLogger.debug("GET Service AAI Path is: \n" + serviceUrl)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, serviceUrl)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENGS_getServiceResponseCode", responseCode)
			msoLogger.debug("  GET Service response code is: " + responseCode)
			msoLogger.debug("GenericGetService AAI Response Code: " + responseCode)

			String aaiResponse = response.getResponseBodyAsString()
			execution.setVariable("GENGS_getServiceResponse", aaiResponse)
			msoLogger.debug("GenericGetService AAI Response: " + aaiResponse)
			//Process Response
			if(responseCode == 200 || responseCode == 202){
				msoLogger.debug("GET Service Received a Good Response Code")
				if(utils.nodeExists(aaiResponse, "service-instance") || utils.nodeExists(aaiResponse, "service-subscription")){
					msoLogger.debug("GET Service Response Contains a service-instance" )
					execution.setVariable("GENGS_FoundIndicator", true)
					execution.setVariable("GENGS_service", aaiResponse)
					execution.setVariable("WorkflowResponse", aaiResponse)

				}else{
					msoLogger.debug("GET Service Response Does NOT Contain Data" )
				}
			}else if(responseCode == 404){
				msoLogger.debug("GET Service Received a Not Found (404) Response")
				execution.setVariable("WorkflowResponse", "  ") //for junits
			}
			else{
				msoLogger.debug("  GET Service Received a Bad Response: \n" + aaiResponse)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.debug(" Error encountered within GenericGetService GetServiceObject method!" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During GenericGetService")
		}
		msoLogger.trace("COMPLETED GenericGetService GetServiceObject Process")
	}

}