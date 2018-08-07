/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import org.camunda.bpm.engine.runtime.Execution
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64
import org.springframework.web.util.UriUtils
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig

/**
 * This groovy class supports the <class>Delete3rdONAPE2EServiceInstance.bpmn</class> process.
 * flow for Delete E2EServiceInstance in 3rdONAP 
 */
public class Delete3rdONAPE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="CRE3rdONAPESI_"

	ExceptionUtil exceptionUtil = new ExceptionUtil()

	JsonUtils jsonUtil = new JsonUtils()

	public void checkSPPartnerInfoFromAAI (DelegateExecution execution) {
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
			// set local resourceInput
			execution.setVariable(Prefix + "ResourceInput", resourceInputObj)
			
			String resourceInstanceId = resourceInputObj.getResourceInstancenUuid()
			String sppartnerId = resourceInstanceId
			execution.setVariable(Prefix + "SppartnerId", sppartnerId)
			utils.log("INFO", "sppartnerId:" + sppartnerId, isDebugEnabled)
			
			// Get Sppartner from AAI
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessSPPartnerUri(execution)
			String namespace = aaiUriUtil.getNamespaceFromUri(aai_uri)			
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			String serviceAaiPath = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(sppartnerId,"UTF-8")
			execution.setVariable(Prefix + "serviceAaiPath", serviceAaiPath)
			
			getSPPartnerInAAI(execution)
			
			String callSource = "UUI"
			String sppartnerUrl = ""
			String sppartnerVersion = ""
			if(execution.getVariable(Prefix + "SuccessIndicator")) {
				callSource = execution.getVariable(Prefix + "CallSource")
				sppartnerId = execution.getVariable(Prefix + "SppartnerId")
				sppartnerUrl = execution.getVariable(Prefix + "SppartnerUrl")
				sppartnerVersion = execution.getVariable(Prefix + "SppartnerVersion")				
			}
			
			boolean is3rdONAPExist = false	
			if(!isBlank(sppartnerUrl)) {				
				is3rdONAPExist = true
			}
			
			execution.setVariable("Is3rdONAPExist", is3rdONAPExist)
			execution.setVariable(Prefix + "ServiceInstanceId", resourceInputObj.getServiceInstanceId())
			execution.setVariable("mso-request-id", requestId)
			execution.setVariable("mso-service-instance-id", resourceInputObj.getServiceInstanceId())

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			String msg = "Exception in checkSPPartnerInfoFromAAI " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void checkLocallCall (DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started checkLocallCall *****",  isDebugEnabled)
		
		boolean isLocalCall = true
		String callSource = execution.getVariable(Prefix + "CallSource")
		if("ExternalAPI".equalsIgnoreCase(callSource)) {
			isLocalCall = false
		}
		execution.setVariable("IsLocalCall", isLocalCall)
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
			execution.setVariable("globalSubscriberId", globalSubscriberId)
			utils.log("INFO", "globalSubscriberId:" + globalSubscriberId, isDebugEnabled)

			String serviceType = resourceInputObj.getServiceType()
			if (isBlank(serviceType)) {
				msg = "Input serviceType is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("serviceType", serviceType)
			utils.log("INFO", "serviceType:" + serviceType, isDebugEnabled)
			
			String operationId = resourceInputObj.getOperationId()			
			if (isBlank(operationId)) {
				msg = "Input operationId is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("operationId", operationId)
			utils.log("INFO", "operationId:" + operationId, isDebugEnabled)
			
			String resourceName = resourceInputObj.getResourceInstanceName()			
			if (isBlank(resourceName)) {
				msg = "Input resourceName is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("resourceName", resourceName)
			utils.log("INFO", "resourceInstanceId:" + resourceName, isDebugEnabled)
			
			String resourceTemplateId = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
			if (isBlank(resourceTemplateId)) {
				msg = "Input resourceTemplateId is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("resourceTemplateId", resourceTemplateId)
			utils.log("INFO", "resourceTemplateId:" + resourceTemplateId, isDebugEnabled)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			String msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void prepareUpdateProgress(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started prepareUpdateProgress *****",  isDebugEnabled)
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
		utils.log("INFO"," ***** End prepareUpdateProgress *****",  isDebugEnabled)
	}

	public void prepare3rdONAPRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started prepare3rdONAPRequest *****",  isDebugEnabled)
		
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
		String referredType = execution.getVariable("serviceType")
		String orderItemId = "1"
		String action = "delete" //for delete
		String serviceState = "active"
		String serviceName = ""
		String serviceId = execution.getVariable(Prefix + "ServiceInstanceId")
		
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
	 
		valueMap.put("_requestInputs_",  "")
		
		String payload = externalAPIUtil.setTemplate(ExternalAPIUtil.PostServiceOrderRequestsTemplate, valueMap)
		execution.setVariable(Prefix + "Payload", payload)
		utils.log("INFO", "Exit " + prepare3rdONAPRequest, isDebugEnabled)
	}

	public void doDeleteE2ESIin3rdONAP(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started doDeleteE2ESIin3rdONAP *****",  isDebugEnabled)
		
		String extAPIPath = execution.getVariable("ExternalAPIURL")
		String payload = execution.getVariable(Prefix + "Payload")
		
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
		
		utils.log("INFO", "Exit " + doDeleteE2ESIin3rdONAP, isDebugEnabled)
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
				execution.setVariable("statusDescription", "Delete Service Order Status is unknown")
			}
			execution.setVariable("statusDescription", "Delete Service Order Status is " + serviceOrderState)
		}
		else{			
			utils.log("DEBUG", "Get ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode, isDebugEnabled)
			execution.setVariable("progress", 100)
			execution.setVariable("status", "error")
			execution.setVariable("statusDescription", "Get ServiceOrder Received a bad response")
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Get ServiceOrder Received a bad response from 3rdONAP External API")
		}		
		
		utils.log("INFO", "Exit " + getE2ESIProgressin3rdONAP, isDebugEnabled)
	}
	
	/**
	 * delay 5 sec
	 */
	public void timeDelay(DelegateExecution execution) {
		def isDebugEnabled= execution.getVariable("isDebugLogEnabled")
		try {
			Thread.sleep(5000)
		} catch(InterruptedException e) {
			utils.log("ERROR", "Time Delay exception" + e , isDebugEnabled)
		}
	}

	private void getSPPartnerInAAI(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started postDeleteE2ESIin3rdONAP *****",  isDebugEnabled)	
		
		AaiUtil aaiUriUtil = new AaiUtil(this)
		String serviceAaiPath = execution.getVariable(Prefix + "serviceAaiPath")		
		APIResponse response = aaiUriUtil.executeAAIGetCall(execution, serviceAaiPath)
		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "GetSppartnerResponseCode", responseCode)
		utils.log("DEBUG", "  Get sppartner response code is: " + responseCode, isDebugEnabled)

		String aaiResponse = response.getResponseBodyAsString()
		aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
		aaiResponse = aaiResponse.replaceAll("&", "&amp;")
		execution.setVariable(Prefix + "GetSppartnerResponse", aaiResponse)

		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			//200 OK 201 CREATED 202 ACCEPTED
		{
			utils.log("DEBUG", "GET sppartner Received a Good Response", isDebugEnabled)
			execution.setVariable(Prefix + "SuccessIndicator", true)
			execution.setVariable(Prefix + "FoundIndicator", true)
			
			String sppartnerId = utils.getNodeText1(aaiResponse, "sppartner-id")
			execution.setVariable(Prefix + "SppartnerId", sppartnerId)
			utils.log("DEBUG", " SppartnerId is: " + sppartnerId, isDebugEnabled)
			String sppartnerUrl = utils.getNodeText1(aaiResponse, "sppartner-url")
			execution.setVariable(Prefix + "SppartnerUrl", sppartnerUrl)
			utils.log("DEBUG", " SppartnerUrl is: " + sppartnerUrl, isDebugEnabled)
			String callSource = utils.getNodeText1(aaiResponse, "sppartner-callsource")
			execution.setVariable(Prefix + "CallSource", callSource)
			utils.log("DEBUG", " CallSource is: " + callSource, isDebugEnabled)
			String sppartnerVersion = utils.getNodeText1(aaiResponse, "resource-version")
			execution.setVariable(Prefix + "SppartnerVersion", sppartnerVersion)
			utils.log("DEBUG", " Resource Version is: " + sppartnerVersion, isDebugEnabled)
		}
		else
		{
			utils.log("DEBUG", "Get sppartner Received a Bad Response Code. Response Code is: " + responseCode, isDebugEnabled)
			exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
			throw new BpmnError("MSOWorkflowException")
		}
		
		utils.log("INFO", "Exit " + deleteSPPartnerInAAI, isDebugEnabled)
	}
	
	public void deleteSPPartnerInAAI(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started postDeleteE2ESIin3rdONAP *****",  isDebugEnabled)
		
		String sppartnerId = execution.getVariable(Prefix + "SppartnerId")
		String sppartnerUrl = execution.getVariable(Prefix + "sppartnerUrl")
		String sppartnerVersion = execution.getVariable(Prefix + "sppartnerVersion")
		
		AaiUtil aaiUriUtil = new AaiUtil(this)
		String serviceAaiPath = execution.getVariable(Prefix + "serviceAaiPath") + "/${sppartnerVersion}"
		APIResponse response = aaiUriUtil.executeAAIDeleteCall(execution, serviceAaiPath)
		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "DeleteSppartnerResponseCode", responseCode)
		utils.log("DEBUG", "  Get sppartner response code is: " + responseCode, isDebugEnabled)

		String aaiResponse = response.getResponseBodyAsString()
		aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
		execution.setVariable(Prefix + "DeleteSppartnerResponse", aaiResponse)

		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			//200 OK 201 CREATED 202 ACCEPTED
		{
			utils.log("DEBUG", "Delete sppartner Received a Good Response", isDebugEnabled)
			execution.setVariable(Prefix + "SuccessIndicator", true)
		}
		else if(responseCode == 404){
			utils.log("DEBUG", " Delete sppartner Received a Not Found (404) Response", isDebugEnabled)
			execution.setVariable(Prefix + "FoundIndicator", false)
		}
		else
		{
			utils.log("DEBUG", "Delete sppartner Received a Bad Response Code. Response Code is: " + responseCode, isDebugEnabled)
			exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
			throw new BpmnError("MSOWorkflowException")
		}
		
		utils.log("INFO", "Exit " + deleteSPPartnerInAAI, isDebugEnabled)
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
}
