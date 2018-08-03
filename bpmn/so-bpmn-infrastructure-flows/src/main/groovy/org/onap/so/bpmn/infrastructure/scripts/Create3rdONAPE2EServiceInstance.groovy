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

package org.onap.so.bpmn.infrastructure.scripts;

import org.json.JSONObject
import org.json.XML;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtil
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.recipe.ResourceInput;
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.builder.AbstractBuilder
import org.onap.so.rest.APIResponse
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.infrastructure.workflow.service.ServicePluginFactory
import java.util.UUID;

import org.camunda.bpm.engine.runtime.Execution
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig

/**
 * This groovy class supports the <class>Create3rdONAPE2EServiceInstance.bpmn</class> process.
 * flow for Create E2EServiceInstance in 3rdONAP 
 */
public class Create3rdONAPE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="CRE3rdONAPESI_"

	ExceptionUtil exceptionUtil = new ExceptionUtil()

	JsonUtils jsonUtil = new JsonUtils()

	public void checkSPPartnerInfo (DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started checkSPPartnerInfo *****",  isDebugEnabled)
		try {
			//get bpmn inputs from resource request.
			String requestId = execution.getVariable("mso-request-id")
			String requestAction = execution.getVariable("requestAction")
			utils.log("INFO","The requestAction is: " + requestAction,  isDebugEnabled)
			String recipeParamsFromRequest = execution.getVariable("recipeParams")
			utils.log("INFO","The recipeParams is: " + recipeParamsFromRequest,  isDebugEnabled)
			String resourceInput = execution.getVariable("resourceInput")
			utils.log("INFO","The resourceInput is: " + resourceInput,  isDebugEnabled)
			//Get ResourceInput Object
			ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(resourceInput, ResourceInput.class)			
			String resourceInputPrameters = resourceInputObj.getResourceParameters()
			String inputParametersJson = jsonUtil.getJsonValue(resourceInputPrameters, "requestInputs")
			JSONObject inputParameters = new JSONObject(customizeResourceParam(inputParametersJson))
			
			// set local resourceInput
			execution.setVariable(Prefix + "resourceInput", resourceInputObj)
			
			boolean is3rdONAPExist = false

			if(inputParameters.has("id"))
			{
				String sppartnerId = inputParameters.get("id")
			}
			if(inputParameters.has("url"))
			{
				String sppartnerUrl = inputParameters.get("url")
				if(!isBlank(sppartnerUrl)) {
					execution.setVariable(Prefix + "sppartnerUrl", sppartnerUrl)
					is3rdONAPExist = true
				}
				else {
					is3rdONAPExist = false
					String msg = "sppartner Url is blank."
					utils.log("DEBUG", msg, isDebugEnabled)
				}
			}
			if(inputParameters.has("providingServiceInvarianteUuid"))
			{
				String sppartnerInvarianteUUID = inputParameters.get("providingServiceInvarianteUuid")
				execution.setVariable(Prefix + "sppartnerInvarianteUUID", sppartnerInvarianteUUID)
				is3rdONAPExist = true
			}
			else {
				is3rdONAPExist = false
				String msg = "sppartner providingServiceInvarianteUuid is blank."
				utils.log("DEBUG", msg, isDebugEnabled)
			}
			if(inputParameters.has("providingServiceUuid"))
			{
				String sppartnerUUID = inputParameters.get("providingServiceUuid")
				execution.setVariable(Prefix + "sppartnerUUID", sppartnerUUID)
				is3rdONAPExist = true
			}
			else {
				is3rdONAPExist = false
				String msg = "sppartner providingServiceUuid is blank."
				utils.log("DEBUG", msg, isDebugEnabled)
			}
			
			if(inputParameters.has("handoverMode"))
			{
				String handoverMode = inputParameters.get("handoverMode")
				execution.setVariable(Prefix + "handoverMode", handoverMode)
			    is3rdONAPExist = true
			}
			else {
				is3rdONAPExist = false
				String msg = "sppartner handoverMode is blank."
				utils.log("DEBUG", msg, isDebugEnabled)
			}
			
			execution.setVariable("Is3rdONAPExist", is3rdONAPExist)
			execution.setVariable(Prefix + "serviceInstanceId", resourceInputObj.getServiceInstanceId())
			execution.setVariable("mso-request-id", requestId)
			execution.setVariable("mso-service-instance-id", resourceInputObj.getServiceInstanceId())

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			String msg = "Exception in checkSPPartnerInfo " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void checkLocallCall (DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started checkLocallCall *****",  isDebugEnabled)
		try {
					
			//Get ResourceInput Object
			ResourceInput resourceInputObj = execution.getVariable(Prefix + "resourceInput")			

			//uuiRequest
			String incomingRequest = resourceInputObj.getRequestsInputs()
			String serviceParameters = jsonUtil.getJsonValue(incomingRequest, "service.parameters")
			JSONObject inputParameters = new JSONObject(customizeResourceParam(serviceParameters))
			execution.setVariable(Prefix + "serviceParameters", inputParameters)
			
			// CallSource is added only when ONAP SO calling 3rdONAP SO(Remote call)
			boolean isLocalCall = true
			if(inputParameters.has("CallSource"))
			{
				String callSource = inputParameters.get("CallSource")
				if("3rdONAP".equalsIgnoreCase(callSource)) {
					isLocalCall = false
				}
				execution.setVariable(Prefix + "CallSource", callSource)
				utils.log("DEBUG", "callSource is: " + callSource , isDebugEnabled)
				isLocalCall = true
			}
			
			execution.setVariable("IsLocalCall", isLocalCall)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			String msg = "Exception in checkLocallCall " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void preProcessRequest(DelegateExecution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started preProcessRequest *****",  isDebugEnabled)
		try {
			ResourceInput resourceInputObj = execution.getVariable(Prefix + "resourceInput")
			String msg = ""

			String globalSubscriberId = resourceInputObj.getGlobalSubscriberId()
			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			//set local variable
			execution.setVariable("globalSubscriberId", globalSubscriberId);
			utils.log("INFO", "globalSubscriberId:" + globalSubscriberId, isDebugEnabled)

			String serviceType = resourceInputObj.getServiceType()
			if (isBlank(serviceType)) {
				msg = "Input serviceType is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("serviceType", serviceType)
			utils.log("INFO", "serviceType:" + serviceType, isDebugEnabled)
			
			String resourceName = resourceInputObj.getResourceInstanceName();
			if (isBlank(resourceName)) {
				msg = "Input resourceName is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("resourceName", resourceName)
			utils.log("INFO", "resourceName:" + resourceName, isDebugEnabled)
			
			int beginIndex = resourceName.indexOf("_") + 1
			String serviceInstanceName = resourceName.substring(beginIndex)
			execution.setVariable("serviceInstanceName", serviceInstanceName)
			
			String serviceInstanceId = resourceInputObj.getServiceInstanceId();
			if (isBlank(serviceInstanceId)) {
				msg = "Input serviceInstanceId is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("serviceInstanceId", serviceInstanceId)
			utils.log("INFO", "serviceInstanceId:" + serviceInstanceId, isDebugEnabled)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			String msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void prepareUpdateProgress(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started prepareUpdateProgress *****",  isDebugEnabled)
		ResourceInput resourceInputObj = execution.getVariable(Prefix + "resourceInput")
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
                </soapenv:Envelope>""";

		setProgressUpdateVariables(execution, body)
		utils.log("INFO"," ***** End prepareUpdateProgress *****",  isDebugEnabled)
	}

	public void allocateCrossONAPResource(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started allocateCrossONAPResource *****",  isDebugEnabled)
		
		//get TP links from AAI for SOTN handoverMode only
		String handoverMode = execution.getVariable(Prefix + "handoverMode")
		if("SOTN".equalsIgnoreCase(handoverMode)) {
			//to do get tp link in AAI
			
			
			// Put TP Link info into serviceParameters
			String accessProviderId = ""
			String accessClientId = ""
			String accessTopologyId = ""
			String accessNodeId = ""
			String accessLtpId = ""
			JSONObject inputParameters = execution.getVariable(Prefix + "serviceParameters")			
			inputParameters.put("access-provider-id", accessProviderId)
			inputParameters.put("access-client-id", accessClientId)
			inputParameters.put("access-topology-id", accessTopologyId)
			inputParameters.put("access-node-id", accessNodeId)
			inputParameters.put("access-ltp-id", accessLtpId)
			execution.setVariable(Prefix + "serviceParameters", inputParameters)
		}
		
		utils.log("INFO", "Exited " + allocateCrossONAPResource, isDebugEnabled)
	}

	public void prepare3rdONAPRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started prepare3rdONAPRequest *****",  isDebugEnabled)
		
		String sppartnerUrl = execution.getVariable(Prefix + "sppartnerUrl")
		String extAPIPath = sppartnerUrl + 'serviceOrder'
		execution.setVariable("ExternalAPIURL", extAPIPath)
		
		// ExternalAPI message format
		String externalId = execution.getVariable("resourceName")
		String category = "Network Service"
		String description = "Service Order from SPPartner"
		String requestedStartDate = utils.generateCurrentTimeInUtc()
		String requestedCompletionDate = utils.generateCurrentTimeInUtc()
		String priority = "1" // 0-4 0:highest
		String subscriberId = execution.getVariable("globalSubscriberId")
		String customerRole = ""
		String subscriberName = ""
		String referredType = execution.getVariable("serviceType")
		String orderItemId = "1"
		String action = "add" //for create
		String serviceState = "active"
		String serviceName = execution.getVariable("serviceInstanceName")
		String serviceId = execution.getVariable("serviceInstanceId")
		
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
		valueMap.put("serviceName", '"' + serviceName + '"')
		valueMap.put("serviceId", '"' + serviceId + '"')
		
		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(this)
		
		// insert CallSource='3rdONAP' to uuiRequest		
		Map<String, String> callSourceMap = new HashMap<>()
		callSourceMap.put("inputName", "CallSource")
		callSourceMap.put("inputValue", "3rdONAP")
		String _requestInputs_ = externalAPIUtil.setTemplate(ExternalAPIUtil.RequestInputsTemplate, callSourceMap)
		
		// Transfer all uuiRequest incomeParameters to ExternalAPI format
		JSONObject inputParameters = execution.getVariable(Prefix + "serviceParameters")
		for(String key : inputParameters.keySet()) {			
			String inputName = key;
			String inputValue = inputParameters.opt(key);
			Map<String, String> requestInputsMap = new HashMap<>()
			requestInputsMap.put("inputName", '"' + inputName+ '"')
			requestInputsMap.put("inputValue", '"' + inputValue + '"')
			_requestInputs_ += ",\n" + externalAPIUtil.setTemplate(ExternalAPIUtil.RequestInputsTemplate, requestInputsMap)
		}		 
		valueMap.put("_requestInputs_",  _requestInputs_)
		
		String payload = externalAPIUtil.setTemplate(ExternalAPIUtil.PostServiceOrderRequestsTemplate, valueMap)
		execution.setVariable(Prefix + "payload", payload)
		utils.log("INFO", "Exited " + prepare3rdONAPRequest, isDebugEnabled)
	}

	public void doCreateE2ESIin3rdONAP(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started doCreateE2ESIin3rdONAP *****",  isDebugEnabled)
		
		String extAPIPath = execution.getVariable("ExternalAPIURL")
		String payload = execution.getVariable(Prefix + "payload")
		
		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(this)

		APIResponse response = externalAPIUtil.executeExternalAPIPostCall(execution, extAPIPath, payload)

		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "postServiceOrderResponseCode", responseCode)
		utils.log("DEBUG", "Post ServiceOrder response code is: " + responseCode, isDebugEnabled)

		String extApiResponse = response.getResponseBodyAsString()
		JSONObject responseObj = new JSONObject(extApiResponse)
		execution.setVariable(Prefix + "postServiceOrderResponse", extApiResponse)
		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			//200 OK 201 CREATED 202 ACCEPTED
		{
			utils.log("DEBUG", "Post ServiceOrder Received a Good Response", isDebugEnabled)
			String serviceOrderId = responseObj.get("ServiceOrderId")
			execution.setVariable(Prefix + "SuccessIndicator", true)
			execution.setVariable("serviceOrderId", serviceOrderId)
		}
		else{
			utils.log("DEBUG", "Post ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Post ServiceOrder Received a bad response from 3rdONAP External API")
		}
		
		utils.log("INFO", "Exited " + doCreateE2ESIin3rdONAP, isDebugEnabled)
	}
	

	public void getE2ESIProgressin3rdONAP(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started getE2ESIProgressin3rdONAP *****",  isDebugEnabled)
		
		String extAPIPath = execution.getVariable("ExternalAPIURL")
		extAPIPath += "/" + execution.getVariable("ServiceOrderId")
		
		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil(this)

		APIResponse response = externalAPIUtil.executeExternalAPIGetCall(execution, extAPIPath)

		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "getServiceOrderResponseCode", responseCode)
		utils.log("DEBUG", "Get ServiceOrder response code is: " + responseCode, isDebugEnabled)

		String extApiResponse = response.getResponseBodyAsString()
		JSONObject responseObj = new JSONObject(extApiResponse)
		execution.setVariable(Prefix + "getServiceOrderResponse", extApiResponse)
		
		//Process Response //200 OK 201 CREATED 202 ACCEPTED
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )			
		{
			utils.log("DEBUG", "Get ServiceOrder Received a Good Response", isDebugEnabled)
			String serviceOrderState = responseObj.get("State")
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
			utils.log("DEBUG", "Get ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode, isDebugEnabled)
			execution.setVariable("progress", 100)
			execution.setVariable("status", "error")
			execution.setVariable("statusDescription", "Get ServiceOrder Received a bad response")
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Get ServiceOrder Received a bad response from 3rdONAP External API")
		}		
		
		utils.log("INFO", "Exited " + getE2ESIProgressin3rdONAP, isDebugEnabled)
	}
	
	/**
	 * delay 5 sec
	 */
	public void timeDelay(DelegateExecution execution) {
		def isDebugEnabled= execution.getVariable("isDebugLogEnabled")
		try {
			Thread.sleep(5000);
		} catch(InterruptedException e) {
			utils.log("ERROR", "Time Delay exception" + e , isDebugEnabled)
		}
	}

	public void saveSPPartnerInAAI(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started postCreateE2ESIin3rdONAP *****",  isDebugEnabled)	
		
		String sppartnerId = UUID.randomUUID().toString()
		String sppartnerUrl = execution.getVariable(Prefix + "sppartnerUrl")
		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		
		AaiUtil aaiUriUtil = new AaiUtil(this)
		String aai_uri = aaiUriUtil.getBusinessSPPartnerUri(execution)
		String namespace = aaiUriUtil.getNamespaceFromUri(aai_uri)
		
		String payload =
				"""<sp-partner xmlns=\"${namespace}\">
			        <id>${sppartnerId}</id>
			        <url>${sppartnerUrl}</url>
					<service-instance>					
					    <service-instance-id>${serviceInstanceId}</service-instance-id>				    
				    </service-instance>
					</sp-partner>""".trim()
		utils.logAudit(payload)
		
		String aai_endpoint = execution.getVariable("URN_aai_endpoint")
		String serviceAaiPath = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(sppartnerId,"UTF-8")
		
		APIResponse response = aaiUriUtil.executeAAIPutCall(execution, serviceAaiPath, payload)
		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "putSppartnerResponseCode", responseCode)
		utils.log("DEBUG", "  Put sppartner response code is: " + responseCode, isDebugEnabled)

		String aaiResponse = response.getResponseBodyAsString()
		aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
		execution.setVariable(Prefix + "putSppartnerResponse", aaiResponse)

		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			//200 OK 201 CREATED 202 ACCEPTED
		{
			utils.log("DEBUG", "PUT sppartner Received a Good Response", isDebugEnabled)
			execution.setVariable(Prefix + "SuccessIndicator", true)
		}
		else
		{
			utils.log("DEBUG", "Put sppartner Received a Bad Response Code. Response Code is: " + responseCode, isDebugEnabled)
			exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
			throw new BpmnError("MSOWorkflowException")
		}
		
		utils.log("INFO", "Exited " + saveSPPartnerInAAI, isDebugEnabled)
	}

	private void setProgressUpdateVariables(DelegateExecution execution, String body) {
		def dbAdapterEndpoint = execution.getVariable("URN_mso_adapters_openecomp_db_endpoint")
		execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
		execution.setVariable("CVFMI_updateResOperStatusRequest", body)
	}	

	public void postProcess(DelegateExecution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started postProcess *****",  isDebugEnabled)
		String responseCode = execution.getVariable(Prefix + "putSppartnerResponseCode")
		String responseObj = execution.getVariable(Prefix + "putSppartnerResponse")

		utils.log("INFO","response from AAI for put sppartner, response code :" + responseCode + "  response object :" + responseObj,  isDebugEnabled)
		utils.log("INFO"," ***** Exit postProcess *****",  isDebugEnabled)
	}

	public void sendSyncResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** sendSyncResponse *** ", isDebugEnabled)

		try {
			String operationStatus = "finished"
			// RESTResponse for main flow
			String resourceOperationResp = """{"operationStatus":"${operationStatus}"}""".trim()
			utils.log("DEBUG", " sendSyncResponse to APIH:" + "\n" + resourceOperationResp, isDebugEnabled)
			sendWorkflowResponse(execution, 202, resourceOperationResp)
			execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit sendSyncResopnse *****",  isDebugEnabled)
	}
	
	String customizeResourceParam(String inputParametersJson) {
		List<Map<String, Object>> paramList = new ArrayList();
		JSONObject jsonObject = new JSONObject(inputParametersJson);
		Iterator iterator = jsonObject.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			HashMap<String, String> hashMap = new HashMap();
			hashMap.put("name", key);
			hashMap.put("value", jsonObject.get(key))
			paramList.add(hashMap)
		}
		Map<String, List<Map<String, Object>>> paramMap = new HashMap();
		paramMap.put("param", paramList);

		return  new JSONObject(paramMap).toString();
	}
}
