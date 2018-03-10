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
package org.openecomp.mso.bpmn.common.scripts

import static org.apache.commons.lang3.StringUtils.*

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.*

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.net.URLEncoder
import org.springframework.web.util.UriUtils

/**
 * This class supports the GenericPutService Sub Flow.
 * This Generic sub flow can be used by any flow for the
 * goal of creating a Service Instance or Service-Subscription in AAI. Upon successful completion of
 * this sub flow the GENPS_SuccessIndicator will be true.
 * The calling flow must set the GENPS_type variable as "service-instance"
 * or "service-subscription".
 *  A MSOWorkflowException will be thrown Upon unsuccessful
 * completion or if an error occurs within this flow.
 * Please map variables to the corresponding variable names
 * below.
 *
 *
 * Incoming Required Variables:
 * @param - GENPS_requestId
 * @param - GENPS_type - Required field. This will be required field populated as service-instance or service-subscription
 * @param - GENPS_globalSubscriberId - Required field
 * @param - GENPS_serviceType - Required Field
 * @param - GENPS_payload - Required Field This will be the payload that needs to be sent.
 *
 * @param - GENPS_serviceInstanceId - Conditional Field. Required for service-instance.
 * @param - GENPS_allottedResourceId - Conditional Field. Required for allotted-resource.
 * @param - GENPS_tunnelXconnectId - Conditional Field. Required for tunnel-xconnect.
 *
 * @param - GENPS_serviceResourceVersion - Conditional Field. Needs to be provided only in case of update for both service-instance and service subscription. The calling flows
 *          should check if a service-instance or servic-subscription exists by calling the subflow GenericGetService. if it exists then resourceversion should be
 *          obtained from aai and sent as an input parameter.
 *
 * Outgoing Variables:
 * @param - GENPS_SuccessIndicator
 * @param - WorkflowException
 *
 *
 */


class CustomE2EPutService extends AbstractServiceTaskProcessor{

	String Prefix = "GENPS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()


	public void preProcessRequest(Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericPutService PreProcessRequest Process*** ", isDebugEnabled)

		execution.setVariable("GENPS_SuccessIndicator", false)

		try{
			// Get Variables
			String requestId = execution.getVariable("GENPS_requestId")
			utils.log("DEBUG", "Incoming GENPS_requestId is: " + requestId, isDebugEnabled)

			String globalSubscriberId = execution.getVariable("GENPS_globalSubscriberId")
			String serviceInstanceId = execution.getVariable("GENPS_serviceInstanceId")
			String serviceType = execution.getVariable("GENPS_serviceType")
			String allottedResourceId = execution.getVariable("GENPS_allottedResourceId")
			String tunnelXconnectId = execution.getVariable("GENPS_tunnelXconnectId")
			String type = execution.getVariable("GENPS_type")

			if(type != null){
				utils.log("DEBUG", "Incoming GENPS_type is: " + type, isDebugEnabled)
				if(type.equalsIgnoreCase("service-instance")){
					if(isBlank(globalSubscriberId) || isBlank(serviceType) || isBlank(serviceInstanceId)){
						utils.log("DEBUG", "Incoming Required Variable is missing or null!", isDebugEnabled)
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Required Variable is Missing or Null!")
					}else{
						utils.log("DEBUG", "Incoming Global Subscriber Id is: " + globalSubscriberId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Service Type is: " + serviceType, isDebugEnabled)
					}
				}else if(type.equalsIgnoreCase("service-subscription")){
					if(isBlank(serviceType) || isBlank(globalSubscriberId)){
						utils.log("DEBUG", "Incoming ServiceType or GlobalSubscriberId is null. These variables are required to create a service-subscription.", isDebugEnabled)
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming ServiceType or GlobalCustomerId is null. These variables are required to Get a service-subscription.")
					}else{
						utils.log("DEBUG", "Incoming Service Type is: " + serviceType, isDebugEnabled)
						utils.log("DEBUG", "Incoming Global Subscriber Id is: " + globalSubscriberId, isDebugEnabled)
					}
				}else if(type.equalsIgnoreCase("allotted-resource")){
					if(isBlank(globalSubscriberId) || isBlank(serviceType) || isBlank(serviceInstanceId) || isBlank(allottedResourceId)){
						utils.log("DEBUG", "Incoming Global Subscriber Id is: " + globalSubscriberId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Service Type is: " + serviceType, isDebugEnabled)
						utils.log("DEBUG", "Incoming Allotted Resource Id is: " + allottedResourceId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Required Variable is missing or null!", isDebugEnabled)
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Required Variable is Missing or Null!")
					}else{
						utils.log("DEBUG", "Incoming Global Subscriber Id is: " + globalSubscriberId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Service Type is: " + serviceType, isDebugEnabled)
						utils.log("DEBUG", "Incoming Allotted Resource Id is: " + allottedResourceId, isDebugEnabled)
					}
				}else if(type.equalsIgnoreCase("tunnel-xconnect")){
					if(isBlank(globalSubscriberId) || isBlank(serviceType) || isBlank(serviceInstanceId) || isBlank(allottedResourceId) || isBlank(tunnelXconnectId)){
						utils.log("DEBUG", "Incoming Global Subscriber Id is: " + globalSubscriberId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Service Type is: " + serviceType, isDebugEnabled)
						utils.log("DEBUG", "Incoming Allotted Resource Id is: " + allottedResourceId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Tunnel Xconnect Id is: " + tunnelXconnectId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Required Variable is missing or null!", isDebugEnabled)
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Required Variable is Missing or Null!")
					}else{
						utils.log("DEBUG", "Incoming Global Subscriber Id is: " + globalSubscriberId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Service Type is: " + serviceType, isDebugEnabled)
						utils.log("DEBUG", "Incoming Allotted Resource Id is: " + allottedResourceId, isDebugEnabled)
						utils.log("DEBUG", "Incoming Tunnel Xconnect Id is: " + tunnelXconnectId, isDebugEnabled)
					}
				}else{
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Type is Invalid. Please Specify Type as service-instance or service-subscription")
				}
			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Incoming GENPS_type is null. Variable is Required.")
			}

		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("ERROR", " Error encountered within GenericPutService PreProcessRequest method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in GenericPutService PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED GenericPutService PreProcessRequest Process ***", isDebugEnabled)

	}



	/**
	 * This method executes a Put call to AAI for the provided
	 * service instance.
	 *
	 * @param - execution
	 *
	 */
	public void putServiceInstance(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericPutService PutServiceInstance method*** ", isDebugEnabled)
		try {
			String type = execution.getVariable("GENPS_type")

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugEnabled)
			String namespace = aaiUriUtil.getNamespaceFromUri(execution, aai_uri)
			logDebug('AAI namespace is: ' + namespace, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			String serviceAaiPath = ""
			String payload = execution.getVariable("GENPS_payload")
			execution.setVariable("GENPS_payload", payload)
			utils.log("DEBUG", "Incoming GENPS_payload is: " + payload, isDebugEnabled)
			utils.logAudit(payload)

			String serviceType = execution.getVariable("GENPS_serviceType")
			utils.log("DEBUG", " Incoming GENPS_serviceType is: " + serviceType, isDebugEnabled)
			String globalSubscriberId = execution.getVariable("GENPS_globalSubscriberId")
			utils.log("DEBUG", "Incoming Global Subscriber Id is: " + globalSubscriberId, isDebugEnabled)

			// This IF clause is if we need to create a new Service Instance
			if(type.equalsIgnoreCase("service-instance")){

				String serviceInstanceId = execution.getVariable("GENPS_serviceInstanceId")
				utils.log("DEBUG", " Incoming GENPS_serviceInstanceId is: " + serviceInstanceId, isDebugEnabled)

				//	serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + URLEncoder.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + URLEncoder.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + URLEncoder.encode(serviceInstanceId,"UTF-8")
				serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + UriUtils.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + UriUtils.encode(serviceInstanceId,"UTF-8")

			}else if(type.equalsIgnoreCase("service-subscription")){

				//	serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + URLEncoder.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + URLEncoder.encode(serviceType,"UTF-8")
				serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + UriUtils.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8")
			}else if(type.equalsIgnoreCase("allotted-resource")){

				String serviceInstanceId = execution.getVariable("GENPS_serviceInstanceId")
				utils.log("DEBUG", " Incoming GENPS_serviceInstanceId is: " + serviceInstanceId, isDebugEnabled)
				String allottedResourceId = execution.getVariable("GENPS_allottedResourceId")
				utils.log("DEBUG", " Incoming GENPS_allottedResourceId is: " + allottedResourceId, isDebugEnabled)

				//	serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + URLEncoder.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + URLEncoder.encode(serviceType,"UTF-8")
				serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + UriUtils.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + UriUtils.encode(serviceInstanceId,"UTF-8") + "/allotted-resources/allotted-resource/" + UriUtils.encode(allottedResourceId,"UTF-8")
			}else if(type.equalsIgnoreCase("tunnel-xconnect")){

				String serviceInstanceId = execution.getVariable("GENPS_serviceInstanceId")
				utils.log("DEBUG", " Incoming GENPS_serviceInstanceId is: " + serviceInstanceId, isDebugEnabled)
				String allottedResourceId = execution.getVariable("GENPS_allottedResourceId")
				utils.log("DEBUG", " Incoming GENPS_allottedResourceId is: " + allottedResourceId, isDebugEnabled)
				String tunnelXconnectId = execution.getVariable("GENPS_tunnelXconnectId")
				utils.log("DEBUG", " Incoming GENPS_tunnelXconnectId is: " + tunnelXconnectId, isDebugEnabled)

				//	serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + URLEncoder.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + URLEncoder.encode(serviceType,"UTF-8")
				serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + UriUtils.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + UriUtils.encode(serviceInstanceId,"UTF-8") + "/allotted-resources/allotted-resource/" + UriUtils.encode(allottedResourceId,"UTF-8") + "/tunnel-xconnects/tunnel-xconnect/" + UriUtils.encode(tunnelXconnectId,"UTF-8")
			}

			String resourceVersion = execution.getVariable("GENPS_serviceResourceVersion")
			utils.log("DEBUG", "Incoming Resource Version is: " + resourceVersion, isDebugEnabled)
			if(resourceVersion !=null){
				serviceAaiPath = serviceAaiPath +'?resource-version=' + UriUtils.encode(resourceVersion,"UTF-8")
			}

			execution.setVariable("GENPS_putServiceInstanceAaiPath", serviceAaiPath)
			utils.log("DEBUG", "PUT Service Instance AAI Path is: " + "\n" + serviceAaiPath, isDebugEnabled)

			APIResponse response = aaiUriUtil.executeAAIPutCall(execution, serviceAaiPath, payload)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENPS_putServiceInstanceResponseCode", responseCode)
			utils.log("DEBUG", "  Put Service Instance response code is: " + responseCode, isDebugEnabled)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENPS_putServiceInstanceResponse", aaiResponse)


			//Process Response
			if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
				//200 OK 201 CREATED 202 ACCEPTED
			{
				utils.log("DEBUG", "PUT Service Instance Received a Good Response", isDebugEnabled)
				execution.setVariable("GENPS_SuccessIndicator", true)
			}

			else{
				utils.log("DEBUG", "Put Generic Service Instance Received a Bad Response Code. Response Code is: " + responseCode, isDebugEnabled)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("ERROR", " Error encountered within GenericPutService PutServiceInstance method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During Put Service Instance")
		}
		utils.log("DEBUG", " *** COMPLETED GenericPutService PutServiceInstance Process*** ", isDebugEnabled)
	}



}
