/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

import org.json.JSONObject
import org.json.XML

import static org.apache.commons.lang3.StringUtils.*
import groovy.xml.XmlUtil
import groovy.json.*
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtil
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.recipe.ResourceInput
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.builder.AbstractBuilder
import org.onap.so.rest.APIResponse
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.infrastructure.workflow.service.ServicePluginFactory
import java.util.UUID
import org.onap.so.logger.MsoLogger

import org.camunda.bpm.engine.runtime.Execution
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64
import org.springframework.web.util.UriUtils
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig

/**
 * This groovy class supports the <class>Create3rdONAPE2EServiceInstance.bpmn</class> process.
 * flow for Create E2EServiceInstance in 3rdONAP 
 */
public class Create3rdONAPE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix = "CRE3rdONAPESI_"

	ExceptionUtil exceptionUtil = new ExceptionUtil()

	JsonUtils jsonUtil = new JsonUtils()
	
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, Create3rdONAPE2EServiceInstance.class)

	public void checkSPPartnerInfo (DelegateExecution execution) {
		msoLogger.info(" ***** Started checkSPPartnerInfo *****")
		try {
			//get bpmn inputs from resource request.
			String requestId = execution.getVariable("mso-request-id")
			String requestAction = execution.getVariable("requestAction")
			msoLogger.info("The requestAction is: " + requestAction)
			String recipeParamsFromRequest = execution.getVariable("recipeParams")
			msoLogger.info("The recipeParams is: " + recipeParamsFromRequest)
			String resourceInput = execution.getVariable("resourceInput")
			msoLogger.info("The resourceInput is: " + resourceInput)
			//Get ResourceInput Object
			ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(resourceInput, ResourceInput.class)			
			String resourceInputPrameters = resourceInputObj.getResourceParameters()
			String inputParametersJson = jsonUtil.getJsonValue(resourceInputPrameters, "requestInputs")
			JSONObject inputParameters = new JSONObject(customizeResourceParam(inputParametersJson))
			
			// set local resourceInput
			execution.setVariable(Prefix + "ResourceInput", resourceInputObj)
			
			boolean is3rdONAPExist = false

			if(inputParameters.has("url"))
			{
				String sppartnerUrl = inputParameters.get("url")
				if(!isBlank(sppartnerUrl)) {
					execution.setVariable(Prefix + "SppartnerUrl", sppartnerUrl)
					is3rdONAPExist = true
				}
				else {
					is3rdONAPExist = false
					String msg = "sppartner Url is blank."
					msoLogger.debug(msg)
				}
			}
			if(inputParameters.has("providingServiceInvarianteUuid"))
			{
				String sppartnerInvarianteUUID = inputParameters.get("providingServiceInvarianteUuid")
				execution.setVariable(Prefix + "SppartnerInvarianteUUID", sppartnerInvarianteUUID)
				is3rdONAPExist = true
			}
			else {
				is3rdONAPExist = false
				String msg = "sppartner providingServiceInvarianteUuid is blank."
				msoLogger.debug(msg)
			}
			if(inputParameters.has("providingServiceUuid"))
			{
				String sppartnerUUID = inputParameters.get("providingServiceUuid")
				execution.setVariable(Prefix + "SppartnerUUID", sppartnerUUID)
				is3rdONAPExist = true
			}
			else {
				is3rdONAPExist = false
				String msg = "sppartner providingServiceUuid is blank."
				msoLogger.debug(msg)
			}
			
			if(inputParameters.has("handoverMode"))
			{
				String handoverMode = inputParameters.get("handoverMode")
				execution.setVariable(Prefix + "HandoverMode", handoverMode)
			    is3rdONAPExist = true
			}
			else {
				is3rdONAPExist = false
				String msg = "sppartner handoverMode is blank."
				msoLogger.debug(msg)
			}
			
			execution.setVariable("Is3rdONAPExist", is3rdONAPExist)
			execution.setVariable(Prefix + "ServiceInstanceId", resourceInputObj.getServiceInstanceId())
			execution.setVariable("mso-request-id", requestId)
			execution.setVariable("mso-service-instance-id", resourceInputObj.getServiceInstanceId())

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			String msg = "Exception in checkSPPartnerInfo " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void checkLocallCall (DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		msoLogger.info(" ***** Started checkLocallCall *****")
		try {
					
			//Get ResourceInput Object
			ResourceInput resourceInputObj = execution.getVariable(Prefix + "ResourceInput")			

			//uuiRequest
			String incomingRequest = resourceInputObj.getRequestsInputs()
			String serviceParameters = jsonUtil.getJsonValue(incomingRequest, "service.parameters")
			String requestInputs = JsonUtils.getJsonValue(serviceParameters, "requestInputs")
			JSONObject inputParameters = new JSONObject(customizeResourceParam(requestInputs))
			execution.setVariable(Prefix + "ServiceParameters", inputParameters)
			
			// CallSource is added only when ONAP SO calling 3rdONAP(External API) SO(Remote call)
			boolean isLocalCall = true
			String callSource = "UUI"
			if(inputParameters.has("CallSource"))
			{
				callSource = inputParameters.get("CallSource")
				if("ExternalAPI".equalsIgnoreCase(callSource)) {
					String sppartnerId = inputParameters.get("SppartnerServiceId")
					execution.setVariable(Prefix + "SppartnerServiceId", sppartnerId)
					isLocalCall = false
				}							
			}
			execution.setVariable(Prefix + "CallSource", callSource)
			msoLogger.debug("callSource is: " + callSource )
			
			execution.setVariable("IsLocalCall", isLocalCall)

		} catch (Exception ex){
			String msg = "Exception in checkLocallCall " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void preProcessRequest(DelegateExecution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		msoLogger.info(" ***** Started preProcessRequest *****")
		try {
			ResourceInput resourceInputObj = execution.getVariable(Prefix + "ResourceInput")
			String msg = ""

			String globalSubscriberId = resourceInputObj.getGlobalSubscriberId()
			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId is null"
				msoLogger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			//set local variable
			execution.setVariable("globalSubscriberId", globalSubscriberId)
			msoLogger.info("globalSubscriberId:" + globalSubscriberId)

			String serviceType = resourceInputObj.getServiceType()
			if (isBlank(serviceType)) {
				msg = "Input serviceType is null"
				msoLogger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("serviceType", serviceType)
			msoLogger.info("serviceType:" + serviceType)
			
			String resourceName = resourceInputObj.getResourceInstanceName()
			if (isBlank(resourceName)) {
				msg = "Input resourceName is null"
				msoLogger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("resourceName", resourceName)
			msoLogger.info("resourceName:" + resourceName)
			
			int beginIndex = resourceName.indexOf("_") + 1
			String serviceInstanceName = resourceName.substring(beginIndex)
			execution.setVariable("serviceInstanceName", serviceInstanceName)
			
			String serviceInstanceId = resourceInputObj.getServiceInstanceId()
			if (isBlank(serviceInstanceId)) {
				msg = "Input serviceInstanceId is null"
				msoLogger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable(Prefix + "ServiceInstanceId", serviceInstanceId)
			msoLogger.info("serviceInstanceId:" + serviceInstanceId)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			String msg = "Exception in preProcessRequest " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void prepareUpdateProgress(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		msoLogger.info(" ***** Started prepareUpdateProgress *****")
		ResourceInput resourceInputObj = execution.getVariable(Prefix + "ResourceInput")
		String operType = resourceInputObj.getOperationType()
		String resourceCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
		String ServiceInstanceId = resourceInputObj.getServiceInstanceId()
		String modelName = resourceInputObj.getResourceModelInfo().getModelName()
		String operationId = resourceInputObj.getOperationId()
		String progress = execution.getVariable("progress")
		String status = execution.getVariable("status")
		String statusDescription = execution.getVariable("statusDescription")

		String body = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.openecomp.mso/requestsdb">
                        <soapenv:Header/>
                <soapenv:Body>
                    <ns:updateResourceOperationStatus>
                               <operType>${operType}</operType>
                               <operationId>${operationId}</operationId>
                               <progress>${progress}</progress>
                               <resourceTemplateUUID>${resourceCustomizationUuid}</resourceTemplateUUID>
                               <serviceId>${ServiceInstanceId}</serviceId>
                               <status>${status}</status>
                               <statusDescription>${statusDescription}</statusDescription>
                    </ns:updateResourceOperationStatus>
                </soapenv:Body>
                </soapenv:Envelope>"""

		setProgressUpdateVariables(execution, body)
		msoLogger.info(" ***** Exit prepareUpdateProgress *****")
	}

	public void allocateCrossONAPResource(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		msoLogger.info(" ***** Started allocateCrossONAPResource *****")
		
		//get TP links from AAI for SOTN handoverMode only
		String handoverMode = execution.getVariable(Prefix + "HandoverMode")
		if("SOTN".equalsIgnoreCase(handoverMode)) {
			// Put TP Link info into serviceParameters
			JSONObject inputParameters = execution.getVariable(Prefix + "ServiceParameters")

			Map<String, Object> crossTPs = new HashMap<String, Object>();
			crossTPs.put("local-access-provider-id", inputParameters.get("remote-access-provider-id"));
			crossTPs.put("local-access-client-id", inputParameters.get("remote-access-client-id"));
			crossTPs.put("local-access-topology-id", inputParameters.get("remote-access-topology-id"));
			crossTPs.put("local-access-node-id", inputParameters.get("remote-access-node-id"));
			crossTPs.put("local-access-ltp-id", inputParameters.get("remote-access-ltp-id"));
			crossTPs.put("remote-access-provider-id", inputParameters.get("local-access-provider-id"));
			crossTPs.put("remote-access-client-id", inputParameters.get("local-client-id"));
			crossTPs.put("remote-access-topology-id", inputParameters.get("local-topology-id"));
			crossTPs.put("remote-access-node-id", inputParameters.get("local-node-id"));
			crossTPs.put("remote-access-ltp-id", inputParameters.get("local-ltp-id"));

			inputParameters.put("local-access-provider-id", crossTPs.get("local-access-provider-id"));
			inputParameters.put("local-access-client-id", crossTPs.get("local-access-client-id"));
			inputParameters.put("local-access-topology-id", crossTPs.get("local-access-topology-id"));
			inputParameters.put("local-access-node-id", crossTPs.get("local-access-node-id"));
			inputParameters.put("local-access-ltp-id", crossTPs.get("local-access-ltp-id"));
			inputParameters.put("remote-access-provider-id", crossTPs.get("remote-access-provider-id"));
			inputParameters.put("remote-access-client-id", crossTPs.get("remote-client-id"));
			inputParameters.put("remote-access-topology-id", crossTPs.get("remote-topology-id"));
			inputParameters.put("remote-access-node-id", crossTPs.get("remote-node-id"));
			inputParameters.put("remote-access-ltp-id", crossTPs.get("remote-ltp-id"));

			execution.setVariable(Prefix + "ServiceParameters", inputParameters)
		}
		
		msoLogger.info("Exit " + allocateCrossONAPResource)
	}

	public void prepare3rdONAPRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		msoLogger.info(" ***** Started prepare3rdONAPRequest *****")
		
		String sppartnerUrl = execution.getVariable(Prefix + "SppartnerUrl")
		String extAPIPath = sppartnerUrl + 'serviceOrder'
		execution.setVariable("ExternalAPIURL", extAPIPath)
		
		// ExternalAPI message format
		String externalId = execution.getVariable("resourceName")
		String category = "E2E Service"
		String description = "Service Order from SPPartner"
		String requestedStartDate = utils.generateCurrentTimeInUtc()
		String requestedCompletionDate = utils.generateCurrentTimeInUtc()
		String priority = "1" // 0-4 0:highest
		String subscriberId = execution.getVariable("globalSubscriberId")
		String customerRole = ""
		String subscriberName = ""
		String referredType = "Consumer"
		String orderItemId = "1"
		String action = "add" //for create
		String serviceState = "active"
		String serviceName = execution.getVariable("serviceInstanceName")
		String serviceUuId = execution.setVariable(Prefix + "SppartnerUUID")
		
		Map<String, String> valueMap = new HashMap<>()
		valueMap.put("externalId", '"' + externalId + '"')
		valueMap.put("category", '"' + category + '"')
		valueMap.put("description", '"' + description + '"')
		valueMap.put("requestedStartDate", '"' + requestedStartDate + '"')
		valueMap.put("requestedCompletionDate", '"' + requestedCompletionDate + '"')
		valueMap.put("priority", '"'+ priority + '"')
		valueMap.put("subscriberId", '"' + subscriberId + '"')
		valueMap.put("customerRole", '"' + customerRole + '"')
		valueMap.put("subscriberName", '"' + subscriberName + '"')
		valueMap.put("referredType", '"' + referredType + '"')
		valueMap.put("orderItemId", '"' + orderItemId + '"')
		valueMap.put("action", '"' + action + '"')
		valueMap.put("serviceState", '"' + serviceState + '"')
		valueMap.put("serviceId", '""')//To be confirmed 
		valueMap.put("serviceName", '"' + serviceName + '"')
		valueMap.put("serviceUuId", '"' + serviceUuId + '"')
		
		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(this)
		
		// insert CallSource='ExternalAPI' to uuiRequest		
		Map<String, String> requestInputsMap = new HashMap<>()
		requestInputsMap.put("inputName", "CallSource")
		requestInputsMap.put("inputValue", "ExternalAPI")
		String _requestInputs_ = externalAPIUtil.setTemplate(ExternalAPIUtil.RequestInputsTemplate, requestInputsMap)
		
		requestInputsMap.clear()		
		String serviceInstanceId = execution.getVariable(Prefix + "ServiceInstanceId")
		requestInputsMap.put("inputName", "SppartnerServiceId")
		requestInputsMap.put("inputValue", serviceInstanceId)		
		_requestInputs_ +=  ",\n" + externalAPIUtil.setTemplate(ExternalAPIUtil.RequestInputsTemplate, requestInputsMap)
		
		requestInputsMap.clear()
		String serviceType = execution.getVariable("serviceType")
		requestInputsMap.put("inputName", "serviceType")
		requestInputsMap.put("inputValue", serviceType)
		_requestInputs_ +=  ",\n" + externalAPIUtil.setTemplate(ExternalAPIUtil.RequestInputsTemplate, requestInputsMap)		
		
		// Transfer all uuiRequest incomeParameters to ExternalAPI format
		JSONObject inputParameters = execution.getVariable(Prefix + "ServiceParameters")
		for(String key : inputParameters.keySet()) {			
			String inputName = key
			String inputValue = inputParameters.opt(key)
			requestInputsMap.clear()
			requestInputsMap.put("inputName", '"' + inputName+ '"')
			requestInputsMap.put("inputValue", '"' + inputValue + '"')
			_requestInputs_ += ",\n" + externalAPIUtil.setTemplate(ExternalAPIUtil.RequestInputsTemplate, requestInputsMap)
		}		 
		valueMap.put("_requestInputs_",  _requestInputs_)
		
		String payload = externalAPIUtil.setTemplate(ExternalAPIUtil.PostServiceOrderRequestsTemplate, valueMap)
		execution.setVariable(Prefix + "Payload", payload)
		msoLogger.info("Exit " + prepare3rdONAPRequest)
	}

	public void doCreateE2ESIin3rdONAP(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		msoLogger.info(" ***** Started doCreateE2ESIin3rdONAP *****")
		
		String extAPIPath = execution.getVariable("ExternalAPIURL")
		String payload = execution.getVariable(Prefix + "Payload")
		
		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(this)

		APIResponse response = externalAPIUtil.executeExternalAPIPostCall(execution, extAPIPath, payload)

		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "PostServiceOrderResponseCode", responseCode)
		msoLogger.debug("Post ServiceOrder response code is: " + responseCode)

		String extApiResponse = response.getResponseBodyAsString()
		JSONObject responseObj = new JSONObject(extApiResponse)
		execution.setVariable(Prefix + "PostServiceOrderResponse", extApiResponse)
		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			//200 OK 201 CREATED 202 ACCEPTED
		{
			msoLogger.debug("Post ServiceOrder Received a Good Response")
			String serviceOrderId = responseObj.get("ServiceOrderId")
			execution.setVariable(Prefix + "SuccessIndicator", true)
			execution.setVariable("serviceOrderId", serviceOrderId)
		}
		else{
			msoLogger.debug("Post ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Post ServiceOrder Received a bad response from 3rdONAP External API")
		}
		
		msoLogger.info("Exit " + doCreateE2ESIin3rdONAP)
	}
	

	public void getE2ESIProgressin3rdONAP(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		msoLogger.info(" ***** Started getE2ESIProgressin3rdONAP *****")
		
		String extAPIPath = execution.getVariable("ExternalAPIURL")
		extAPIPath += "/" + execution.getVariable("ServiceOrderId")
		
		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(this)

		APIResponse response = externalAPIUtil.executeExternalAPIGetCall(execution, extAPIPath)

		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "GetServiceOrderResponseCode", responseCode)
		msoLogger.debug("Get ServiceOrder response code is: " + responseCode)

		String extApiResponse = response.getResponseBodyAsString()
		JSONObject responseObj = new JSONObject(extApiResponse)
		execution.setVariable(Prefix + "GetServiceOrderResponse", extApiResponse)
		
		//Process Response //200 OK 201 CREATED 202 ACCEPTED
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )			
		{
			msoLogger.debug("Get ServiceOrder Received a Good Response")
			
			String sppartnerServiceId = responseObj.get("orderIterm.service.id")
			execution.setVariable(Prefix + "SppartnerServiceId", sppartnerServiceId)

			String serviceOrderState = responseObj.get("orderIterm.state")
			execution.setVariable(Prefix + "SuccessIndicator", true)
			execution.setVariable("serviceOrderState", serviceOrderState)			
			
			// Get serviceOrder State and process progress
			if("ACKNOWLEDGED".equalsIgnoreCase(serviceOrderState)) {
				execution.setVariable("progress", 15)
				execution.setVariable("status", "processing")				
			}
			if("INPROGRESS".equalsIgnoreCase(serviceOrderState)) {
				execution.setVariable("progress", 40)
				execution.setVariable("status", "processing")
			}
			if("COMPLETED".equalsIgnoreCase(serviceOrderState)) {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "finished")
			}
			if("FAILED".equalsIgnoreCase(serviceOrderState)) {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "error")
			}
			else {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "error")
				execution.setVariable("statusDescription", "Create Service Order Status is unknown")
			}
			execution.setVariable("statusDescription", "Create Service Order Status is " + serviceOrderState)
		}
		else{			
			msoLogger.debug("Get ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode)
			execution.setVariable("progress", 100)
			execution.setVariable("status", "error")
			execution.setVariable("statusDescription", "Get ServiceOrder Received a bad response")
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Get ServiceOrder Received a bad response from 3rdONAP External API")
		}		
		
		msoLogger.info("Exit " + getE2ESIProgressin3rdONAP)
	}
	
	/**
	 * delay 5 sec
	 */
	public void timeDelay(DelegateExecution execution) {
		def isDebugEnabled= execution.getVariable("isDebugLogEnabled")
		try {
			Thread.sleep(5000)
		} catch(InterruptedException e) {
			utils.log("ERROR", "Time Delay exception" + e )
		}
	}

	public void saveSPPartnerInAAI(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		msoLogger.info(" ***** Started postCreateE2ESIin3rdONAP *****")	
		
		String sppartnerId = execution.getVariable(Prefix + "SppartnerServiceId")
		String sppartnerUrl = execution.getVariable(Prefix + "SppartnerUrl")
		String callSource = execution.getVariable(Prefix + "CallSource")
		String serviceInstanceId = execution.getVariable(Prefix + "ServiceInstanceId")
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String serviceType = execution.getVariable("serviceType")
		
		AaiUtil aaiUriUtil = new AaiUtil(this)
		String aai_uri = aaiUriUtil.getBusinessSPPartnerUri(execution)
		String namespace = aaiUriUtil.getNamespaceFromUri(aai_uri)
		
		String payload =
				"""<sp-partner xmlns=\"${namespace}\">
			        <id>${sppartnerId}</id>
			        <url>${sppartnerUrl}</url>
			        <callsource>${callSource}</callsource>
			        <relationship-list>
			          <relationship>
			            <related-to>service-instance</related-to>
			            <related-link>/aai/v14/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${serviceType}/service-instances/service-instance/${serviceInstanceId}</related-link>
			            <relationship-data>
			                <relationship-key>service-instance.service-instance-id</relationship-key>
			                <relationship-value>${serviceInstanceId}</relationship-value>
			            </relationship-data> 
			          </relationship>
			        </relationship-list>
				</sp-partner>""".trim()
		utils.logAudit(payload)
		
		String aai_endpoint = execution.getVariable("URN_aai_endpoint")
		String serviceAaiPath = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(sppartnerId,"UTF-8")
		
		APIResponse response = aaiUriUtil.executeAAIPutCall(execution, serviceAaiPath, payload)
		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "PutSppartnerResponseCode", responseCode)
		msoLogger.debug("  Put sppartner response code is: " + responseCode)

		String aaiResponse = response.getResponseBodyAsString()
		aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
		execution.setVariable(Prefix + "PutSppartnerResponse", aaiResponse)

		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			//200 OK 201 CREATED 202 ACCEPTED
		{
			msoLogger.debug("PUT sppartner Received a Good Response")
			execution.setVariable(Prefix + "SuccessIndicator", true)
		}
		else
		{
			msoLogger.debug("Put sppartner Received a Bad Response Code. Response Code is: " + responseCode)
			exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
			throw new BpmnError("MSOWorkflowException")
		}
		
		msoLogger.info("Exit " + saveSPPartnerInAAI)
	}

	private void setProgressUpdateVariables(DelegateExecution execution, String body) {
		def dbAdapterEndpoint = execution.getVariable("URN_mso_adapters_openecomp_db_endpoint")
		execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
		execution.setVariable("CVFMI_updateResOperStatusRequest", body)
	}	

	public void postProcess(DelegateExecution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		msoLogger.info(" ***** Started postProcess *****")
		String responseCode = execution.getVariable(Prefix + "PutSppartnerResponseCode")
		String responseObj = execution.getVariable(Prefix + "PutSppartnerResponse")

		msoLogger.info("response from AAI for put sppartner, response code :" + responseCode + "  response object :" + responseObj)
		msoLogger.info(" ***** Exit postProcess *****")
	}

	public void sendSyncResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		msoLogger.debug(" *** sendSyncResponse *** ")

		try {
			String operationStatus = "finished"
			// RESTResponse for main flow
			String resourceOperationResp = """{"operationStatus":"${operationStatus}"}""".trim()
			msoLogger.debug(" sendSyncResponse to APIH:" + "\n" + resourceOperationResp)
			sendWorkflowResponse(execution, 202, resourceOperationResp)
			execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.debug(" ***** Exit sendSyncResopnse *****")
	}
	
	String customizeResourceParam(String inputParametersJson) {
		List<Map<String, Object>> paramList = new ArrayList()
		JSONObject jsonObject = new JSONObject(inputParametersJson)
		Iterator iterator = jsonObject.keys()
		while (iterator.hasNext()) {
			String key = iterator.next()
			HashMap<String, String> hashMap = new HashMap()
			hashMap.put("name", key)
			hashMap.put("value", jsonObject.get(key))
			paramList.add(hashMap)
		}
		Map<String, List<Map<String, Object>>> paramMap = new HashMap()
		paramMap.put("param", paramList)

		return  new JSONObject(paramMap).toString()
	}
}
