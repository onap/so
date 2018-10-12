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

import org.json.JSONArray
import org.json.JSONObject
import org.json.XML

import static org.apache.commons.lang3.StringUtils.*
import groovy.xml.XmlUtil
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
 * This groovy class supports the <class>Delete3rdONAPE2EServiceInstance.bpmn</class> process.
 * flow for Delete 3rdONAPE2EServiceInstance in 3rdONAP 
 */
public class Delete3rdONAPE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix = "CRE3rdONAPESI_"

	ExceptionUtil exceptionUtil = new ExceptionUtil()

	JsonUtils jsonUtil = new JsonUtils()

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, Delete3rdONAPE2EServiceInstance.class)

	public void checkSPPartnerInfoFromAAI (DelegateExecution execution) {
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
			// set local resourceInput
			execution.setVariable(Prefix + "ResourceInput", resourceInputObj)
			
			String resourceInstanceId = resourceInputObj.getResourceInstancenUuid()
			String sppartnerId = resourceInstanceId
			execution.setVariable(Prefix + "SppartnerId", sppartnerId)
			
			// Get Sppartner from AAI
			AaiUtil aaiUriUtil = new AaiUtil()
			String aai_uri = aaiUriUtil.getBusinessSPPartnerUri(execution)
			String namespace = aaiUriUtil.getNamespaceFromUri(aai_uri)			
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			String serviceAaiPath = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(sppartnerId,"UTF-8")
			execution.setVariable(Prefix + "ServiceAaiPath", serviceAaiPath)
			
			getSPPartnerInAAI(execution)
			
			String callSource = "UUI"
			String sppartnerUrl = ""
			if(execution.hasVariable(Prefix + "CallSource")) {
				callSource = execution.getVariable(Prefix + "CallSource")
				sppartnerUrl = execution.getVariable(Prefix + "SppartnerUrl")
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
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void checkLocallCall (DelegateExecution execution) {
		msoLogger.info(" ***** Started checkLocallCall *****")
		
		boolean isLocalCall = true
		String callSource = execution.getVariable(Prefix + "CallSource")
		if("ExternalAPI".equalsIgnoreCase(callSource)) {
			isLocalCall = false
		}
		execution.setVariable("IsLocalCall", isLocalCall)
	}

	public void preProcessRequest(DelegateExecution execution){
		msoLogger.info(" ***** Started preProcessRequest *****")
		String msg = ""

		try {
			ResourceInput resourceInputObj = execution.getVariable(Prefix + "ResourceInput")			

			String globalSubscriberId = resourceInputObj.getGlobalSubscriberId()
			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId is null"
				msoLogger.error( msg)
			}
			//set local variable
			execution.setVariable("globalSubscriberId", globalSubscriberId)
			msoLogger.info( "globalSubscriberId:" + globalSubscriberId)

			String serviceType = resourceInputObj.getServiceType()
			if (isBlank(serviceType)) {
				msg = "Input serviceType is null"
				msoLogger.error( msg)
			}
			execution.setVariable("serviceType", serviceType)
			msoLogger.info( "serviceType:" + serviceType)
			
			String operationId = resourceInputObj.getOperationId()			
			if (isBlank(operationId)) {
				msg = "Input operationId is null"
				msoLogger.error( msg)
			}
			execution.setVariable("operationId", operationId)
			msoLogger.info( "operationId:" + operationId)
			
			String resourceName = resourceInputObj.getResourceInstanceName()
			if (isBlank(resourceName)) {
				msg = "Input resourceName is null"
				msoLogger.error( msg)
			}
			execution.setVariable("resourceName", resourceName)
			msoLogger.info("resourceName:" + resourceName)
			
			String resourceTemplateId = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
			if (isBlank(resourceTemplateId)) {
				msg = "Input resourceTemplateId is null"
				msoLogger.error( msg)
			}
			execution.setVariable("resourceTemplateId", resourceTemplateId)
			msoLogger.info( "resourceTemplateId:" + resourceTemplateId)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void prepareUpdateProgress(DelegateExecution execution) {
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
		msoLogger.info(" ***** End prepareUpdateProgress *****")
	}

	public void prepare3rdONAPRequest(DelegateExecution execution) {
		msoLogger.info(" ***** Started prepare3rdONAPRequest *****")
		
		String sppartnerUrl = execution.getVariable(Prefix + "SppartnerUrl")
		String extAPIPath = sppartnerUrl + '/serviceOrder'
		execution.setVariable("ExternalAPIURL", extAPIPath)
		
		// ExternalAPI message format
		String externalId = execution.getVariable("resourceName")
		String category = "E2E Service"
		String description = "Service Order from SPPartner"
		String requestedStartDate = utils.generateCurrentTimeInUtc()
		String requestedCompletionDate = utils.generateCurrentTimeInUtc()
		String priority = "1" // 0-4 0:highest
		String subscriberId = execution.getVariable("globalSubscriberId")
		String customerRole = "ONAPcustomer"
		String subscriberName = subscriberId
		String referredType = "Consumer"
		String orderItemId = "1"
		String action = "delete" //for delete
		String serviceState = "active"
		String serviceName = ""
		String serviceType = execution.getVariable("serviceType")
		String serviceId = execution.getVariable(Prefix + "SppartnerId")
		
		queryServicefrom3rdONAP(execution)
		String serviceSpecificationId = execution.getVariable(Prefix + "ServiceSpecificationId")		
		
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
		valueMap.put("serviceId", '"' + serviceId + '"')
		valueMap.put("serviceName", "null")
		valueMap.put("serviceUuId", '"' + serviceSpecificationId + '"')
		
		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil()
	 
		valueMap.put("_requestInputs_",  "")
		
		String payload = externalAPIUtil.setTemplate(ExternalAPIUtil.PostServiceOrderRequestsTemplate, valueMap)
		execution.setVariable(Prefix + "Payload", payload)
		msoLogger.info( "Exit " + prepare3rdONAPRequest)
	}
	
	private void queryServicefrom3rdONAP(DelegateExecution execution)
	{
		msoLogger.info(" ***** Started queryServicefrom3rdONAP *****")
		
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String SppartnerServiceId = execution.getVariable(Prefix + "SppartnerId")

		//https://{api_url}/nbi/api/v1/service?relatedParty.id=${globalSubscriberId}
		String sppartnerUrl = execution.getVariable(Prefix + "SppartnerUrl")
		String extAPIPath = sppartnerUrl + "/service?relatedParty.id=" + globalSubscriberId
		msoLogger.debug("queryServicefrom3rdONAP externalAPIURL is: " + extAPIPath)

		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil()

		APIResponse response = externalAPIUtil.executeExternalAPIGetCall(execution, extAPIPath)

		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "GetServiceResponseCode", responseCode)
		msoLogger.debug("Get Service response code is: " + responseCode)

		String extApiResponse = response.getResponseBodyAsString()

		execution.setVariable(Prefix + "GetServiceResponse", extApiResponse)		
		msoLogger.debug("queryServicefrom3rdONAP response body is: " + extApiResponse)
		
		//Process Response //200 OK 201 CREATED 202 ACCEPTED
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
		{
			msoLogger.debug("Get Service Received a Good Response")
			JSONArray responseList = new JSONArray(extApiResponse)
			for(JSONObject obj : responseList) {
				String svcId  = obj.get("id")
				if(StringUtils.equalsIgnoreCase(SppartnerServiceId, svcId)) {
					JSONObject serviceSpecification = obj.get("serviceSpecification")
					String serviceUuid = serviceSpecification.get("id")
					execution.setVariable(Prefix + "ServiceSpecificationId", serviceUuid)
					break
				}
			}
		}
		else{
			msoLogger.error("Get Service Received a Bad Response Code. Response Code is: " + responseCode)
//			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Get Service Received a bad response from 3rdONAP External API")
		}
		
		msoLogger.info( "Exit " + queryServicefrom3rdONAP)
	}

	public void doDeleteE2ESIin3rdONAP(DelegateExecution execution) {
		msoLogger.info(" ***** Started doDeleteE2ESIin3rdONAP *****")
		
		String extAPIPath = execution.getVariable("ExternalAPIURL")
		String payload = execution.getVariable(Prefix + "Payload")
		msoLogger.debug("doDeleteE2ESIin3rdONAP externalAPIURL is: " + extAPIPath)
		msoLogger.debug("doDeleteE2ESIin3rdONAP payload is: " + payload)
		
		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil()

		APIResponse response = externalAPIUtil.executeExternalAPIPostCall(execution, extAPIPath, payload)

		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "PostServiceOrderResponseCode", responseCode)
		msoLogger.debug("Post ServiceOrder response code is: " + responseCode)

		String extApiResponse = response.getResponseBodyAsString()
		JSONObject responseObj = new JSONObject(extApiResponse)
		execution.setVariable(Prefix + "PostServiceOrderResponse", extApiResponse)

		msoLogger.debug("doDeleteE2ESIin3rdONAP response body is: " + extApiResponse)

		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			//200 OK 201 CREATED 202 ACCEPTED
		{
			msoLogger.debug("Post ServiceOrder Received a Good Response")
			String serviceOrderId = responseObj.get("id")
			execution.setVariable(Prefix + "SuccessIndicator", true)
			execution.setVariable("ServiceOrderId", serviceOrderId)
			msoLogger.info("Post ServiceOrderid is: " + serviceOrderId)
		}
		else{
			msoLogger.error("Post ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Post ServiceOrder Received a bad response from 3rdONAP External API")
		}
		
		msoLogger.info( "Exit " + doDeleteE2ESIin3rdONAP)
	}
	

	public void getE2ESIProgressin3rdONAP(DelegateExecution execution) {
		msoLogger.info(" ***** Started getE2ESIProgressin3rdONAP *****")
		
		String extAPIPath = execution.getVariable("ExternalAPIURL")
		extAPIPath += "/" + execution.getVariable("ServiceOrderId")
		msoLogger.debug("getE2ESIProgressin3rdONAP delete externalAPIURL is: " + extAPIPath)
		
		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtil()

		APIResponse response = externalAPIUtil.executeExternalAPIGetCall(execution, extAPIPath)

		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "GetServiceOrderResponseCode", responseCode)
		msoLogger.debug("Get ServiceOrder response code is: " + responseCode)

		String extApiResponse = response.getResponseBodyAsString()
		JSONObject responseObj = new JSONObject(extApiResponse)
		execution.setVariable(Prefix + "GetServiceOrderResponse", extApiResponse)
		
		utils.log("DEBUG", "getE2ESIProgressin3rdONAP delete response body is: " + extApiResponse, isDebugEnabled)

		//Process Response //200 OK 201 CREATED 202 ACCEPTED
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
		{
			msoLogger.debug("Get ServiceOrder Received a Good Response")

			String orderState = responseObj.get("state")
			if("REJECTED".equalsIgnoreCase(orderState)) {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "error")
				execution.setVariable("statusDescription", "Delete Service Order Status is REJECTED")
				return
			}

			JSONArray items = responseObj.getJSONArray("orderItem")
			JSONObject item = items[0]
			JSONObject service = item.get("service")
			String sppartnerServiceId = service.get("id")
			if(sppartnerServiceId == null || sppartnerServiceId.equals("null")) {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "error")
				execution.setVariable("statusDescription", "Delete Service Order Status get null sppartnerServiceId")
				msoLogger.error("null sppartnerServiceId while getting progress from externalAPI")
				return
			}
			execution.setVariable(Prefix + "SppartnerServiceId", sppartnerServiceId)

			String serviceOrderState = item.get("state")
			execution.setVariable(Prefix + "SuccessIndicator", true)
			execution.setVariable("ServiceOrderState", serviceOrderState)

			// Get serviceOrder State and process progress
			if("ACKNOWLEDGED".equalsIgnoreCase(serviceOrderState)) {
				execution.setVariable("progress", 15)
				execution.setVariable("status", "processing")
				execution.setVariable("statusDescription", "Delete Service Order Status is " + serviceOrderState)
			}
			else if("INPROGRESS".equalsIgnoreCase(serviceOrderState)) {
				execution.setVariable("progress", 40)
				execution.setVariable("status", "processing")
				execution.setVariable("statusDescription", "Delete Service Order Status is " + serviceOrderState)
			}
			else if("COMPLETED".equalsIgnoreCase(serviceOrderState)) {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "finished")
				execution.setVariable("statusDescription", "Delete Service Order Status is " + serviceOrderState)
			}
			else if("FAILED".equalsIgnoreCase(serviceOrderState)) {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "error")
				execution.setVariable("statusDescription", "Delete Service Order Status is " + serviceOrderState)
			}
			else {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "error")
				execution.setVariable("statusDescription", "Delete Service Order Status is unknown")
			}
		}
		else{			
			msoLogger.debug("Get ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode)
			execution.setVariable("progress", 100)
			execution.setVariable("status", "error")
			execution.setVariable("statusDescription", "Get Delete ServiceOrder Received a bad response")
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Get Delete ServiceOrder Received a bad response from 3rdONAP External API")
		}		
		
		msoLogger.info( "Exit " + getE2ESIProgressin3rdONAP)
	}
	
	/**
	 * delay 5 sec
	 */
	public void timeDelay(DelegateExecution execution) {
		try {
			Thread.sleep(5000)
		} catch(InterruptedException e) {
			msoLogger.error("Time Delay exception" + e )
		}
	}

	private void getSPPartnerInAAI(DelegateExecution execution) {
		msoLogger.info(" ***** Started getSPPartnerInAAI *****")	
		
		AaiUtil aaiUriUtil = new AaiUtil()
		String serviceAaiPath = execution.getVariable(Prefix + "ServiceAaiPath")		
		APIResponse response = aaiUriUtil.executeAAIGetCall(execution, serviceAaiPath)
		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "GetSppartnerResponseCode", responseCode)
		msoLogger.debug("  Get sppartner response code is: " + responseCode)

		String aaiResponse = response.getResponseBodyAsString()
		aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
		aaiResponse = aaiResponse.replaceAll("&", "&amp;")
		execution.setVariable(Prefix + "GetSppartnerResponse", aaiResponse)

		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			//200 OK 201 CREATED 202 ACCEPTED
		{
			msoLogger.debug("GET sppartner Received a Good Response")
			execution.setVariable(Prefix + "SuccessIndicator", true)
			execution.setVariable(Prefix + "FoundIndicator", true)
			
			String sppartnerId = utils.getNodeText1(aaiResponse, "sp-partner-id")
			execution.setVariable(Prefix + "SppartnerId", sppartnerId)
			msoLogger.debug(" SppartnerId is: " + sppartnerId)
			String sppartnerUrl = utils.getNodeText1(aaiResponse, "url")
			execution.setVariable(Prefix + "SppartnerUrl", sppartnerUrl)
			msoLogger.debug(" SppartnerUrl is: " + sppartnerUrl)
			String callSource = utils.getNodeText1(aaiResponse, "callsource")
			execution.setVariable(Prefix + "CallSource", callSource)
			msoLogger.debug(" CallSource is: " + callSource)
			String sppartnerVersion = utils.getNodeText1(aaiResponse, "resource-version")
			execution.setVariable(Prefix + "SppartnerVersion", sppartnerVersion)
			msoLogger.debug(" Resource Version is: " + sppartnerVersion)
		}
		else
		{
			msoLogger.debug("Get sppartner Received a Bad Response Code. Response Code is: " + responseCode)
//			exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
//			throw new BpmnError("MSOWorkflowException")
		}
		
		msoLogger.info( "Exit " + getSPPartnerInAAI)
	}
	
	public void deleteSPPartnerInAAI(DelegateExecution execution) {
		msoLogger.info(" ***** Started deleteSPPartnerInAAI *****")
		
		String sppartnerId = execution.getVariable(Prefix + "SppartnerId")
		String sppartnerUrl = execution.getVariable(Prefix + "SppartnerUrl")
		String sppartnerVersion = execution.getVariable(Prefix + "SppartnerVersion")
		
		AaiUtil aaiUriUtil = new AaiUtil()
		String serviceAaiPath = execution.getVariable(Prefix + "ServiceAaiPath") + "?resource-version=${sppartnerVersion}"
		APIResponse response = aaiUriUtil.executeAAIDeleteCall(execution, serviceAaiPath)
		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "DeleteSppartnerResponseCode", responseCode)
		msoLogger.debug("  Get sppartner response code is: " + responseCode)

		String aaiResponse = response.getResponseBodyAsString()
		aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
		execution.setVariable(Prefix + "DeleteSppartnerResponse", aaiResponse)

		//Process Response
		if(responseCode == 200 || responseCode == 204 )		
		{
			msoLogger.debug("Delete sppartner Received a Good Response")
			execution.setVariable(Prefix + "SuccessIndicator", true)
		}
		else if(responseCode == 404){
			msoLogger.debug(" Delete sppartner Received a Not Found (404) Response")
			execution.setVariable(Prefix + "FoundIndicator", false)
		}
		else
		{
			msoLogger.debug("Delete sppartner Received a Bad Response Code. Response Code is: " + responseCode)
//			exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
//			throw new BpmnError("MSOWorkflowException")
		}
		
		msoLogger.info( "Exit " + deleteSPPartnerInAAI)
	}

	private void setProgressUpdateVariables(DelegateExecution execution, String body) {
		def dbAdapterEndpoint = execution.getVariable("URN_mso_adapters_openecomp_db_endpoint")
		execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
		execution.setVariable("CVFMI_updateResOperStatusRequest", body)
	}	

	public void postProcess(DelegateExecution execution){
		msoLogger.info(" ***** Started postProcess *****")
		String responseCode = execution.getVariable(Prefix + "PutSppartnerResponseCode")
		String responseObj = execution.getVariable(Prefix + "PutSppartnerResponse")

		msoLogger.info("response from AAI for put sppartner, response code :" + responseCode + "  response object :" + responseObj)
		msoLogger.info(" ***** Exit postProcess *****")
	}

	public void sendSyncResponse (DelegateExecution execution) {
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
//			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.debug(" ***** Exit sendSyncResopnse *****")
	}
}
