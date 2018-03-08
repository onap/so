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

import static org.apache.commons.lang3.StringUtils.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.rest.APIResponse
import org.springframework.web.util.UriUtils


/**
 * This class supports the GenericDeleteService Sub Flow.
 * This Generic sub flow can be used by any flow for the
 * goal of deleting a Service-Instance or Service-Subscription.
 * The calling flow must set the GENDS_type variable as "service-instance"
 * or "service-subscription".
 *
 * If the resource-version is not provided by the calling flow
 * then this sub flow will query the service-instance or
 * service-subscription, prior to deleting it, in order to
 * obtain this resource version.  Upon successful completion of
 * this sub flow the GENDS_SuccessIndicator will be true.  A
 * MSOWorkflowException will be thrown if an error occurs within this flow.
 *
 * Please map variables to the corresponding variable names
 * below.
 *
 * Note - if this sub flow receives a Not Found (404) response
 * from AAI at any time this will be considered an acceptable
 * response.
 *
 *
 * Variable Mapping Below
 *
 * In Mapping Variables:
 *   For Service-Instance:
 *     @param - GENDS_serviceInstanceId
 *     @param - GENDS_serviceType
 *     @param - GENDS_globalCustomerId
 *     @param - GENDS_type
 *     @param (Optional) - GENDS_resourceVersion
 *
 *   For Service-Subscription:
 *     @param - GENDS_serviceType
 *     @param - GENDS_globalCustomerId
 *     @param - GENDS_type
 *     @param (Optional) - GENDS_resourceVersion
 *
 *
 * Out Mapping Variables:
 *    @param - GENDS_FoundIndicator
 *    @param - WorkflowException
 *
 *
 */
class GenericDeleteService extends AbstractServiceTaskProcessor{

	String Prefix = "GENDS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * This method validates the incoming variables and
	 * determines if the resource version was provided
	 *
	 * @param - execution
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericDeleteService PreProcessRequest Process*** ", isDebugEnabled)

		execution.setVariable("GENDS_resourceVersionProvidedFlag", true)
		execution.setVariable("GENDS_SuccessIndicator", false)
		execution.setVariable("GENDS_FoundIndicator", false)

		try{
			// Get Variables
			String globalCustomerId = execution.getVariable("GENDS_globalCustomerId")
			String serviceInstanceId = execution.getVariable("GENDS_serviceInstanceId")
			String serviceType = execution.getVariable("GENDS_serviceType")
			String type = execution.getVariable("GENDS_type")

			if(type != null){
				utils.log("DEBUG", "Incoming GENDS_type is: " + type, isDebugEnabled)
				if(isBlank(globalCustomerId) || isBlank(serviceType)){
					utils.log("DEBUG", "Incoming Required Variable is null!", isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Required Variable is Missing or Null!")
				}else{
					utils.log("DEBUG", "Incoming Global Customer Id is: " + globalCustomerId, isDebugEnabled)
					utils.log("DEBUG", "Incoming Service Type is: " + serviceType, isDebugEnabled)
					if(type.equalsIgnoreCase("service-instance")){
						if(isBlank(serviceInstanceId)){
							utils.log("DEBUG", "Incoming Required Variable is null!", isDebugEnabled)
							exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Required Variable is Missing or Null!")
						}else{
							utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)
							utils.log("DEBUG", "Preparing Delete Service-Instance Process", isDebugEnabled)
						}
					}else if(type.equalsIgnoreCase("service-subscription")){
						utils.log("DEBUG", "Preparing Delete Service-Subscription Process", isDebugEnabled)
					}else{
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Type is Invalid. Please Specify Type as service-instance or service-subscription")
					}
				}

				String resourceVersion = execution.getVariable('GENDS_resourceVersion')
				if(isBlank(resourceVersion)){
					utils.log("DEBUG", "Service Instance Resource Version is NOT Provided", isDebugEnabled)
					execution.setVariable("GENDS_resourceVersionProvidedFlag", false)
				}else{
					utils.log("DEBUG", "Incoming SI Resource Version is: " + resourceVersion, isDebugEnabled)
				}

			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Incoming GENDS_type is null. Variable is Required.")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("ERROR", " Error encountered within GenericDeleteService PreProcessRequest method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in GenericDeleteService PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED GenericDeleteService PreProcessRequest Process ***", isDebugEnabled)
	}

	/**
	 * This method executes a GET call to AAI for the service instance
	 * or service-subscription so that the objects's resource-version
	 * can be obtained.
	 *
	 * @param - execution
	 */
	public void getServiceResourceVersion(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericDeleteService GetServiceResourceVersion Process*** ", isDebugEnabled)
		try {
			String serviceType = execution.getVariable("GENDS_serviceType")
			utils.log("DEBUG", " Incoming GENDS_serviceType is: " + serviceType, isDebugEnabled)
			String globalCustomerId = execution.getVariable("GENDS_globalCustomerId")
			utils.log("DEBUG", "Incoming Global Customer Id is: " + globalCustomerId, isDebugEnabled)
			String type = execution.getVariable("GENDS_type")
			String serviceEndpoint = ""

			if(type.equalsIgnoreCase("service-instance")){
				String serviceInstanceId = execution.getVariable("GENDS_serviceInstanceId")
				utils.log("DEBUG", " Incoming GENDS_serviceInstanceId is: " + serviceInstanceId, isDebugEnabled)
				serviceEndpoint = UriUtils.encode(globalCustomerId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + UriUtils.encode(serviceInstanceId,"UTF-8")

			}else if(type.equalsIgnoreCase("service-subscription")){
				serviceEndpoint = UriUtils.encode(globalCustomerId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8")
			}

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugEnabled)

			String serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + serviceEndpoint

			execution.setVariable("GENDS_serviceAaiPath", serviceAaiPath)
			utils.log("DEBUG", "GET Service Instance AAI Path is: " + "\n" + serviceAaiPath, isDebugEnabled)
			utils.logAudit("GenericDeleteService GET AAI Path: " + serviceAaiPath)
			
			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, serviceAaiPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENDS_getServiceResponseCode", responseCode)
			utils.log("DEBUG", "  GET Service Instance response code is: " + responseCode, isDebugEnabled)
			utils.logAudit("GET Service Instance response code: " + responseCode)
			
			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			aaiResponse = aaiResponse.replaceAll("&", "&amp;")
			execution.setVariable("GENDS_getServiceResponse", aaiResponse)

			utils.logAudit("GET Service Instance response : " + aaiResponse)
			//Process Response
			if(responseCode == 200 || responseCode == 202){
				utils.log("DEBUG", "GET Service Received a Good Response: \n" + aaiResponse, isDebugEnabled)
				execution.setVariable("GENDS_SuccessIndicator", true)
				execution.setVariable("GENDS_FoundIndicator", true)
				String resourceVersion = utils.getNodeText1(aaiResponse, "resource-version")
				execution.setVariable("GENDS_resourceVersion", resourceVersion)
				utils.log("DEBUG", type + " Resource Version is: " + resourceVersion, isDebugEnabled)

			}else if(responseCode == 404){
				utils.log("DEBUG", "GET Service Received a Not Found (404) Response", isDebugEnabled)
				execution.setVariable("GENDS_SuccessIndicator", true)
				execution.setVariable("WorkflowResponse", "  ") // for junits
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
			utils.log("DEBUG", " Error encountered within GenericDeleteService GetServiceResourceVersion method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During GetServiceResourceVersion")
		}
		utils.log("DEBUG", " *** COMPLETED GenericDeleteService GetServiceResourceVersion Process*** ", isDebugEnabled)
	}

	/**
	 * This method executes a DELETE call to AAI for the provided
	 * service-instance or service-subscription.
	 *
	 * @param - execution
	 */
	public void deleteServiceObject(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericDeleteService DeleteServiceObject Process*** ", isDebugEnabled)
		try {
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String type = execution.getVariable("GENDS_type")
			String serviceAaiPath = execution.getVariable("GENDS_serviceAaiPath")
			String serviceEndpoint = ""

			if(isEmpty(serviceAaiPath)){
				String serviceType = execution.getVariable("GENDS_serviceType")
				utils.log("DEBUG", " Incoming GENDS_serviceType is: " + serviceType, isDebugEnabled)
				String globalCustomerId = execution.getVariable("GENDS_globalCustomerId")
				utils.log("DEBUG", "Incoming Global Customer Id is: " + globalCustomerId, isDebugEnabled)
				if(type.equalsIgnoreCase("service-instance")){
					String serviceInstanceId = execution.getVariable("GENDS_serviceInstanceId")
					utils.log("DEBUG", " Incoming GENDS_serviceInstanceId is: " + serviceInstanceId, isDebugEnabled)
					serviceEndpoint = UriUtils.encode(globalCustomerId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + UriUtils.encode(serviceInstanceId,"UTF-8")

				}else if(type.equalsIgnoreCase("service-subscription")){
					serviceEndpoint = UriUtils.encode(globalCustomerId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8")
				}

				String aai_endpoint = execution.getVariable("URN_aai_endpoint")
				String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
				logDebug('AAI URI is: ' + aai_uri, isDebugEnabled)

				serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + serviceEndpoint
			}

			String resourceVersion = execution.getVariable("GENDS_resourceVersion")
			utils.log("DEBUG", "Incoming Resource Version is: " + resourceVersion, isDebugEnabled)
			if(resourceVersion !=null){
				serviceAaiPath = serviceAaiPath +'?resource-version=' + UriUtils.encode(resourceVersion,"UTF-8")
			}

			execution.setVariable("GENDS_deleteServiceAaiPath", serviceAaiPath)
			utils.log("DEBUG", "DELETE Service AAI Path is: " + "\n" + serviceAaiPath, isDebugEnabled)
			utils.logAudit("DELETE Service AAI Path: " + serviceAaiPath)
			
			APIResponse response = aaiUriUtil.executeAAIDeleteCall(execution, serviceAaiPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENDS_deleteServiceResponseCode", responseCode)
			utils.log("DEBUG", "  DELETE Service response code is: " + responseCode, isDebugEnabled)
			utils.logAudit("DELETE Service Response Code: " + responseCode)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENDS_deleteServiceResponse", aaiResponse)
			utils.logAudit("DELETE Service Response: " + aaiResponse)
			
			//Process Response
			if(responseCode == 200 || responseCode == 204){
				utils.log("DEBUG", "  DELETE Service Received a Good Response", isDebugEnabled)
				execution.setVariable("GENDS_FoundIndicator", true)
			}else if(responseCode == 404){
				utils.log("DEBUG", "  DELETE Service Received a Not Found (404) Response", isDebugEnabled)
				execution.setVariable("GENDS_FoundIndicator", false)
			}else{
				utils.log("DEBUG", "DELETE Service Received a BAD REST Response: \n" + aaiResponse, isDebugEnabled)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error encountered within GenericDeleteService DeleteServiceObject method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During Delete Service Object")
		}
		utils.log("DEBUG", " *** COMPLETED GenericDeleteService DeleteServiceObject Process*** ", isDebugEnabled)
	}

}
