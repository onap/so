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

package org.onap.so.bpmn.common.scripts

import org.onap.so.bpmn.core.UrnPropertiesReader

import static org.apache.commons.lang3.StringUtils.*;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.*

import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.rest.APIResponse;
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig

import java.net.URLEncoder;
import org.springframework.web.util.UriUtils
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger



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


class GenericPutService extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, GenericPutService.class);


	String Prefix = "GENPS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()


	public void preProcessRequest(DelegateExecution execution) {
		execution.setVariable("isDebugLogEnabled","true")
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericPutService PreProcessRequest Process")

		execution.setVariable("GENPS_SuccessIndicator", false)

		try{
			// Get Variables
			String requestId = execution.getVariable("GENPS_requestId")
			msoLogger.debug("Incoming GENPS_requestId is: " + requestId)

			String globalSubscriberId = execution.getVariable("GENPS_globalSubscriberId")
			String serviceInstanceId = execution.getVariable("GENPS_serviceInstanceId")
			String serviceType = execution.getVariable("GENPS_serviceType")
			String allottedResourceId = execution.getVariable("GENPS_allottedResourceId")
			String tunnelXconnectId = execution.getVariable("GENPS_tunnelXconnectId")
			String type = execution.getVariable("GENPS_type")
			
			if(type != null){
				msoLogger.debug("Incoming GENPS_type is: " + type)
				if(type.equalsIgnoreCase("service-instance")){
					if(isBlank(globalSubscriberId) || isBlank(serviceType) || isBlank(serviceInstanceId)){
						msoLogger.debug("Incoming Required Variable is missing or null!")
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Required Variable is Missing or Null!")
					}else{
						msoLogger.debug("Incoming Global Subscriber Id is: " + globalSubscriberId)
						msoLogger.debug("Incoming Service Instance Id is: " + serviceInstanceId)
						msoLogger.debug("Incoming Service Type is: " + serviceType)
					}
				}else if(type.equalsIgnoreCase("service-subscription")){
					if(isBlank(serviceType) || isBlank(globalSubscriberId)){
						msoLogger.debug("Incoming ServiceType or GlobalSubscriberId is null. These variables are required to create a service-subscription.")
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming ServiceType or GlobalCustomerId is null. These variables are required to Get a service-subscription.")
					}else{
						msoLogger.debug("Incoming Service Type is: " + serviceType)
						msoLogger.debug("Incoming Global Subscriber Id is: " + globalSubscriberId)
					}
				}else if(type.equalsIgnoreCase("allotted-resource")){
					if(isBlank(globalSubscriberId) || isBlank(serviceType) || isBlank(serviceInstanceId) || isBlank(allottedResourceId)){
						msoLogger.debug("Incoming Global Subscriber Id is: " + globalSubscriberId)
						msoLogger.debug("Incoming Service Instance Id is: " + serviceInstanceId)
						msoLogger.debug("Incoming Service Type is: " + serviceType)
						msoLogger.debug("Incoming Allotted Resource Id is: " + allottedResourceId)
						msoLogger.debug("Incoming Required Variable is missing or null!")
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Required Variable is Missing or Null!")
					}else{
						msoLogger.debug("Incoming Global Subscriber Id is: " + globalSubscriberId)
						msoLogger.debug("Incoming Service Instance Id is: " + serviceInstanceId)
						msoLogger.debug("Incoming Service Type is: " + serviceType)
						msoLogger.debug("Incoming Allotted Resource Id is: " + allottedResourceId)
					}
				}else if(type.equalsIgnoreCase("tunnel-xconnect")){
					if(isBlank(globalSubscriberId) || isBlank(serviceType) || isBlank(serviceInstanceId) || isBlank(allottedResourceId) || isBlank(tunnelXconnectId)){
						msoLogger.debug("Incoming Global Subscriber Id is: " + globalSubscriberId)
						msoLogger.debug("Incoming Service Instance Id is: " + serviceInstanceId)
						msoLogger.debug("Incoming Service Type is: " + serviceType)
						msoLogger.debug("Incoming Allotted Resource Id is: " + allottedResourceId)
						msoLogger.debug("Incoming Tunnel Xconnect Id is: " + tunnelXconnectId)
						msoLogger.debug("Incoming Required Variable is missing or null!")
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Required Variable is Missing or Null!")
					}else{
						msoLogger.debug("Incoming Global Subscriber Id is: " + globalSubscriberId)
						msoLogger.debug("Incoming Service Instance Id is: " + serviceInstanceId)
						msoLogger.debug("Incoming Service Type is: " + serviceType)
						msoLogger.debug("Incoming Allotted Resource Id is: " + allottedResourceId)
						msoLogger.debug("Incoming Tunnel Xconnect Id is: " + tunnelXconnectId)
					}
				}else{
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Type is Invalid. Please Specify Type as service-instance or service-subscription")
				}
			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Incoming GENPS_type is null. Variable is Required.")
			}

		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Error encountered within GenericPutService PreProcessRequest method!" + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in GenericPutService PreProcessRequest")

		}
		msoLogger.trace("COMPLETED GenericPutService PreProcessRequest Process ")

	}

	/**
	 * This method executes a Put call to AAI for the provided
	 * service instance.
	 *
	 * @param - execution
	 *
	 */
	public void putServiceInstance(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericPutService PutServiceInstance method")
		try {
			String type = execution.getVariable("GENPS_type")

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			msoLogger.debug('AAI URI is: ' + aai_uri)
			String namespace = aaiUriUtil.getNamespaceFromUri(execution, aai_uri)
			msoLogger.debug('AAI namespace is: ' + namespace)

			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			String serviceAaiPath = ""
			String payload = execution.getVariable("GENPS_payload")
			execution.setVariable("GENPS_payload", payload)
			msoLogger.debug("Incoming GENPS_payload is: " + payload)
			msoLogger.debug(payload)

			String serviceType = execution.getVariable("GENPS_serviceType")
			msoLogger.debug(" Incoming GENPS_serviceType is: " + serviceType)
			
			String globalSubscriberId = execution.getVariable("GENPS_globalSubscriberId")
			msoLogger.debug("Incoming Global Subscriber Id is: " + globalSubscriberId)

			// This IF clause is if we need to create a new Service Instance
			if(type.equalsIgnoreCase("service-instance")){

				String serviceInstanceId = execution.getVariable("GENPS_serviceInstanceId")
				msoLogger.debug(" Incoming GENPS_serviceInstanceId is: " + serviceInstanceId)

				//	serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + URLEncoder.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + URLEncoder.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + URLEncoder.encode(serviceInstanceId,"UTF-8")
				serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + UriUtils.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + UriUtils.encode(serviceInstanceId,"UTF-8")

			}else if(type.equalsIgnoreCase("service-subscription")){

				//	serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + URLEncoder.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + URLEncoder.encode(serviceType,"UTF-8")
				serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + UriUtils.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8")
			}else if(type.equalsIgnoreCase("allotted-resource")){

				String serviceInstanceId = execution.getVariable("GENPS_serviceInstanceId")
				msoLogger.debug(" Incoming GENPS_serviceInstanceId is: " + serviceInstanceId)
				String allottedResourceId = execution.getVariable("GENPS_allottedResourceId")
				msoLogger.debug(" Incoming GENPS_allottedResourceId is: " + allottedResourceId)

				//	serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + URLEncoder.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + URLEncoder.encode(serviceType,"UTF-8")
				serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + UriUtils.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + UriUtils.encode(serviceInstanceId,"UTF-8") + "/allotted-resources/allotted-resource/" + UriUtils.encode(allottedResourceId,"UTF-8")
			}else if(type.equalsIgnoreCase("tunnel-xconnect")){

				String serviceInstanceId = execution.getVariable("GENPS_serviceInstanceId")
				msoLogger.debug(" Incoming GENPS_serviceInstanceId is: " + serviceInstanceId)
				String allottedResourceId = execution.getVariable("GENPS_allottedResourceId")
				msoLogger.debug(" Incoming GENPS_allottedResourceId is: " + allottedResourceId)
				String tunnelXconnectId = execution.getVariable("GENPS_tunnelXconnectId")
				msoLogger.debug(" Incoming GENPS_tunnelXconnectId is: " + tunnelXconnectId)

				//	serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + URLEncoder.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + URLEncoder.encode(serviceType,"UTF-8")
				serviceAaiPath = "${aai_endpoint}${aai_uri}/"  + UriUtils.encode(globalSubscriberId,"UTF-8") + "/service-subscriptions/service-subscription/" + UriUtils.encode(serviceType,"UTF-8") + "/service-instances/service-instance/" + UriUtils.encode(serviceInstanceId,"UTF-8") + "/allotted-resources/allotted-resource/" + UriUtils.encode(allottedResourceId,"UTF-8") + "/tunnel-xconnects/tunnel-xconnect/" + UriUtils.encode(tunnelXconnectId,"UTF-8")
			}

			String resourceVersion = execution.getVariable("GENPS_serviceResourceVersion")
			msoLogger.debug("Incoming Resource Version is: " + resourceVersion)
			if(resourceVersion !=null){
				serviceAaiPath = serviceAaiPath +'?resource-version=' + UriUtils.encode(resourceVersion,"UTF-8")
			}

			execution.setVariable("GENPS_putServiceInstanceAaiPath", serviceAaiPath)
			msoLogger.debug("PUT Service Instance AAI Path is: " + "\n" + serviceAaiPath)
			APIResponse response = aaiUriUtil.executeAAIPutCall(execution, serviceAaiPath, payload)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENPS_putServiceInstanceResponseCode", responseCode)
			msoLogger.debug("  Put Service Instance response code is: " + responseCode)

			String aaiResponse = response.getResponseBodyAsString()
			execution.setVariable("GENPS_putServiceInstanceResponse", aaiResponse)

			//Process Response
			if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
				//200 OK 201 CREATED 202 ACCEPTED
			{
				msoLogger.debug("PUT Service Instance Received a Good Response")
				execution.setVariable("GENPS_SuccessIndicator", true)
			}

			else{
				msoLogger.debug("Put Generic Service Instance Received a Bad Response Code. Response Code is: " + responseCode)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Error encountered within GenericPutService PutServiceInstance method!" + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During Put Service Instance")
		}
		msoLogger.trace("COMPLETED GenericPutService PutServiceInstance Process")
	}

}
