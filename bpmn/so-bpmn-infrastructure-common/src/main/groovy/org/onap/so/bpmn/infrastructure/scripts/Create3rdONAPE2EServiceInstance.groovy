/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import static org.apache.commons.lang3.StringUtils.*
import jakarta.ws.rs.core.Response
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.aai.domain.yang.SpPartner
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.recipe.ResourceInput
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtilFactory
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * This groovy class supports the <class>Create3rdONAPE2EServiceInstance.bpmn</class> process.
 * flow for Create E2EServiceInstance in 3rdONAP
 */
public class Create3rdONAPE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix = "CRE3rdONAPESI_"

	ExceptionUtil exceptionUtil = new ExceptionUtil()

	JsonUtils jsonUtil = new JsonUtils()

    private static final Logger logger = LoggerFactory.getLogger( Create3rdONAPE2EServiceInstance.class)

	public void checkSPPartnerInfo (DelegateExecution execution) {
		logger.info(" ***** Started checkSPPartnerInfo *****")
		try {
			//get bpmn inputs from resource request.
			String requestId = execution.getVariable("mso-request-id")
			String requestAction = execution.getVariable("requestAction")
			logger.info("The requestAction is: " + requestAction)
			String recipeParamsFromRequest = execution.getVariable("recipeParams")
			logger.info("The recipeParams is: " + recipeParamsFromRequest)
			String resourceInput = execution.getVariable("resourceInput")
			logger.info("The resourceInput is: " + resourceInput)
			//Get ResourceInput Object
			ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(resourceInput, ResourceInput.class)
			String resourceInputPrameters = resourceInputObj.getResourceParameters()
			String inputParametersJson = JsonUtils.getJsonValue(resourceInputPrameters, "requestInputs")
			JSONObject inputParameters = new JSONObject(inputParametersJson)

			// set local resourceInput
			execution.setVariable(Prefix + "ResourceInput", resourceInputObj)

			String spPartnerModelName = UrnPropertiesReader.getVariable("sp-partner.modelName")
			boolean is3rdONAPExist = false

			if(inputParameters.has(spPartnerModelName + "_url"))
			{
				String sppartnerUrl = inputParameters.get(spPartnerModelName + "_url")
				if(!isBlank(sppartnerUrl)) {
					execution.setVariable(Prefix + "SppartnerUrl", sppartnerUrl)
					is3rdONAPExist = true
				}
				else {
					is3rdONAPExist = false
					String msg = "sppartner Url is blank."
					logger.debug(msg)
				}
			}
			if(inputParameters.has(spPartnerModelName + "_providingServiceUuid"))
			{
				String sppartnerUUID= inputParameters.get(spPartnerModelName + "_providingServiceUuid")
				execution.setVariable(Prefix + "SppartnerUUID", sppartnerUUID)
				is3rdONAPExist = true
			}
			else {
				is3rdONAPExist = false
				String msg = "sppartner providingServiceUuid is blank."
				logger.debug(msg)
			}
			if(inputParameters.has(spPartnerModelName + "_providingServiceInvariantUuid"))
			{
				String sppartnerInvarianteUUID  = inputParameters.get(spPartnerModelName + "_providingServiceInvariantUuid")
				execution.setVariable(Prefix + "SppartnerInvarianteUUID", sppartnerInvarianteUUID)
				is3rdONAPExist = true
			}
			else {
				is3rdONAPExist = false
				String msg = "sppartner providingServiceInvarianteUuid is blank."
				logger.debug(msg)
			}

			if(inputParameters.has(spPartnerModelName + "_handoverMode"))
			{
				String handoverMode = inputParameters.get(spPartnerModelName + "_handoverMode")
				execution.setVariable(Prefix + "HandoverMode", handoverMode)
			    is3rdONAPExist = true
			}
			else {
				is3rdONAPExist = false
				String msg = "sppartner handoverMode is blank."
				logger.debug(msg)
			}

			execution.setVariable("Is3rdONAPExist", is3rdONAPExist)
			execution.setVariable(Prefix + "ServiceInstanceId", resourceInputObj.getServiceInstanceId())
			execution.setVariable("mso-request-id", requestId)
			execution.setVariable("mso-service-instance-id", resourceInputObj.getServiceInstanceId())

		} catch (Exception ex){
			String msg = "Exception in checkSPPartnerInfo " + ex.getMessage()
			logger.debug(msg)
//			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void checkLocallCall (DelegateExecution execution) {
		logger.info(" ***** Started checkLocallCall *****")
		try {

			//Get ResourceInput Object
			ResourceInput resourceInputObj = execution.getVariable(Prefix + "ResourceInput")

			//uuiRequest
			String incomingRequest = resourceInputObj.getRequestsInputs()
			String serviceParameters = JsonUtils.getJsonValue(incomingRequest, "service.parameters")
			String requestInputs = JsonUtils.getJsonValue(serviceParameters, "requestInputs")
			JSONObject inputParameters = new JSONObject(requestInputs)
			execution.setVariable(Prefix + "ServiceParameters", inputParameters.toString())

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
			logger.info("callSource is: " + callSource )

			execution.setVariable("IsLocalCall", isLocalCall)

		} catch (Exception ex){
			String msg = "Exception in checkLocallCall " + ex.getMessage()
			logger.debug(msg)
//			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void preProcessRequest(DelegateExecution execution){
		logger.info(" ***** Started preProcessRequest *****")
		String msg = ""

		try {
			ResourceInput resourceInputObj = execution.getVariable(Prefix + "ResourceInput")

			String globalSubscriberId = resourceInputObj.getGlobalSubscriberId()
			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId is null"
				logger.error(msg)
			}
			//set local variable
			execution.setVariable("globalSubscriberId", globalSubscriberId)
			logger.info("globalSubscriberId:" + globalSubscriberId)

			String serviceType = resourceInputObj.getServiceType()
			if (isBlank(serviceType)) {
				msg = "Input serviceType is null"
				logger.error(msg)
			}
			execution.setVariable("serviceType", serviceType)
			logger.info("serviceType:" + serviceType)

			String resourceName = resourceInputObj.getResourceInstanceName()
			if (isBlank(resourceName)) {
				msg = "Input resourceName is null"
				logger.error(msg)
			}
			execution.setVariable("resourceName", resourceName)
			logger.info("resourceName:" + resourceName)

			int beginIndex = resourceName.indexOf("_") + 1
			String serviceInstanceName = resourceName.substring(beginIndex)
			execution.setVariable("serviceInstanceName", serviceInstanceName)

			String serviceInstanceId = resourceInputObj.getServiceInstanceId()
			if (isBlank(serviceInstanceId)) {
				msg = "Input serviceInstanceId is null"
				logger.error(msg)
			}
			execution.setVariable(Prefix + "ServiceInstanceId", serviceInstanceId)
			logger.info("serviceInstanceId:" + serviceInstanceId)

			String resourceModelInvariantUuid = resourceInputObj.getResourceModelInfo().getModelInvariantUuid()
			if (isBlank(resourceModelInvariantUuid)) {
				msg = "Input resourceModelInvariantUuid is null"
				logger.error(msg)
			}
			execution.setVariable(Prefix + "ResourceModelInvariantUuid", resourceModelInvariantUuid)
			logger.info("resourceModelInvariantUuid:" + resourceModelInvariantUuid)

			String resourceModelUuid = resourceInputObj.getResourceModelInfo().getModelUuid()
			if (isBlank(resourceModelUuid)) {
				msg = "Input resourceModelUuid is null"
				logger.error(msg)
			}
			execution.setVariable(Prefix + "ResourceModelUuid", resourceModelUuid)
			logger.info("resourceModelUuid:" + resourceModelUuid)

			String resourceModelCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
			if (isBlank(resourceModelCustomizationUuid)) {
				msg = "Input resourceModelCustomizationUuid is null"
				logger.error(msg)
			}
			execution.setVariable(Prefix + "ResourceModelCustomizationUuid", resourceModelCustomizationUuid)
			logger.info("resourceModelCustomizationUuid:" + resourceModelCustomizationUuid)

		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.debug(msg)
//			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void prepareUpdateProgress(DelegateExecution execution) {
		logger.info(" ***** Started prepareUpdateProgress *****")
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
                        xmlns:ns="http://org.onap.so/requestsdb">
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
		logger.info(" ***** Exit prepareUpdateProgress *****")
	}

	public void allocateCrossONAPResource(DelegateExecution execution) {
		logger.info(" ***** Started allocateCrossONAPResource *****")

		//get TP links from AAI for SOTN handoverMode only
		String handoverMode = execution.getVariable(Prefix + "HandoverMode")
		if("SOTN".equalsIgnoreCase(handoverMode)) {
			// Put TP Link info into serviceParameters
			JSONObject inputParameters = new JSONObject(execution.getVariable(Prefix + "ServiceParameters"))
			if(inputParameters.has("remote-access-provider-id")) {
				Map<String, Object> crossTPs = new HashMap<String, Object>()
				crossTPs.put("local-access-provider-id", inputParameters.get("remote-access-provider-id"))
				crossTPs.put("local-access-client-id", inputParameters.get("remote-access-client-id"))
				crossTPs.put("local-access-topology-id", inputParameters.get("remote-access-topology-id"))
				crossTPs.put("local-access-node-id", inputParameters.get("remote-access-node-id"))
				crossTPs.put("local-access-ltp-id", inputParameters.get("remote-access-ltp-id"))
				crossTPs.put("remote-access-provider-id", inputParameters.get("local-access-provider-id"))
				crossTPs.put("remote-access-client-id", inputParameters.get("local-access-client-id"))
				crossTPs.put("remote-access-topology-id", inputParameters.get("local-access-topology-id"))
				crossTPs.put("remote-access-node-id", inputParameters.get("local-access-node-id"))
				crossTPs.put("remote-access-ltp-id", inputParameters.get("local-access-ltp-id"))

				inputParameters.put("local-access-provider-id", crossTPs.get("local-access-provider-id"))
				inputParameters.put("local-access-client-id", crossTPs.get("local-access-client-id"))
				inputParameters.put("local-access-topology-id", crossTPs.get("local-access-topology-id"))
				inputParameters.put("local-access-node-id", crossTPs.get("local-access-node-id"))
				inputParameters.put("local-access-ltp-id", crossTPs.get("local-access-ltp-id"))
				inputParameters.put("remote-access-provider-id", crossTPs.get("remote-access-provider-id"))
				inputParameters.put("remote-access-client-id", crossTPs.get("remote-access-client-id"))
				inputParameters.put("remote-access-topology-id", crossTPs.get("remote-access-topology-id"))
				inputParameters.put("remote-access-node-id", crossTPs.get("remote-access-node-id"))
				inputParameters.put("remote-access-ltp-id", crossTPs.get("remote-access-ltp-id"))

				execution.setVariable(Prefix + "ServiceParameters", inputParameters.toString())
			}
			else {
					logger.error("No allocated CrossONAPResource found in ServiceParameters")
			}
		}

		logger.info("Exit  allocateCrossONAPResource")
	}

	public void prepare3rdONAPRequest(DelegateExecution execution) {
		logger.info(" ***** Started prepare3rdONAPRequest *****")

		String sppartnerUrl = execution.getVariable(Prefix + "SppartnerUrl")
		String extAPIPath = UrnPropertiesReader.getVariable("extapi.endpoint", execution) + '/serviceOrder'
		execution.setVariable("ExternalAPIURL", extAPIPath)
		execution.setVariable("SPPartnerUrl",sppartnerUrl)

		// ExternalAPI message format
		String externalId = execution.getVariable("resourceName")
		String serviceType = execution.getVariable("serviceType")
		String category = "E2E Service"
		String description = "Service Order from SPPartner"
		String requestedStartDate = utils.generateCurrentTimeInUtc()
		String requestedCompletionDate = utils.generateCurrentTimeInUtc()
		String priority = "1" // 0-4 0:highest
		String subscriberId = execution.getVariable("globalSubscriberId")
		String customerRole = "ONAPcustomer"
		// Below SO will pass serviceType as subscriberName and externalAPI will use
		// the same serviceType in another domain instead of model name
		String subscriberName = serviceType
		String referredType = "Consumer"
		String orderItemId = "1"
		String action = "add" //for create
		String serviceState = "active"
		String serviceName = execution.getVariable("serviceInstanceName")
		String serviceUuId = execution.getVariable(Prefix + "SppartnerUUID")

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
		valueMap.put("serviceId", "null") //null for add
		valueMap.put("serviceName", '"' + serviceName + '"')
		valueMap.put("serviceUuId", '"' + serviceUuId + '"')

		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtilFactory().create()

		// insert CallSource='ExternalAPI' to uuiRequest
		Map<String, String> requestInputsMap = new HashMap<>()
		requestInputsMap.put("inputName",  '"CallSource"')
		requestInputsMap.put("inputValue", '"ExternalAPI"')
		String _requestInputs_ = externalAPIUtil.setTemplate(ExternalAPIUtil.RequestInputsTemplate, requestInputsMap)

		requestInputsMap.clear()
		String serviceInstanceId = execution.getVariable(Prefix + "ServiceInstanceId")
		requestInputsMap.put("inputName", '"SppartnerServiceId"')
		requestInputsMap.put("inputValue", '"' + serviceInstanceId + '"')
		_requestInputs_ +=  ",\n" + externalAPIUtil.setTemplate(ExternalAPIUtil.RequestInputsTemplate, requestInputsMap)

		requestInputsMap.clear()
		requestInputsMap.put("inputName", '"serviceType"')
		requestInputsMap.put("inputValue", '"' + serviceType + '"')
		_requestInputs_ +=  ",\n" + externalAPIUtil.setTemplate(ExternalAPIUtil.RequestInputsTemplate, requestInputsMap)

		// Transfer all uuiRequest incomeParameters to ExternalAPI format
		JSONObject inputParameters = new JSONObject(execution.getVariable(Prefix + "ServiceParameters"))
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
		logger.info(" ***** Exit prepare3rdONAPRequest *****")
	}

	public void doCreateE2ESIin3rdONAP(DelegateExecution execution) {
		logger.info(" ***** Started doCreateE2ESIin3rdONAP *****")
        try {
		String extAPIPath = execution.getVariable("ExternalAPIURL")
		String payload = execution.getVariable(Prefix + "Payload")
		logger.debug("doCreateE2ESIin3rdONAP externalAPIURL is: " + extAPIPath)
		logger.debug("doCreateE2ESIin3rdONAP payload is: " + payload)

		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtilFactory().create()
		execution.setVariable("ServiceOrderId", "")

		Response response = externalAPIUtil.executeExternalAPIPostCall(execution, extAPIPath, payload)

		int responseCode = response.getStatus()
		execution.setVariable(Prefix + "PostServiceOrderResponseCode", responseCode)
		logger.debug("Post ServiceOrder response code is: " + responseCode)

		String extApiResponse = response.readEntity(String.class)
		JSONObject responseObj = new JSONObject(extApiResponse)
		execution.setVariable(Prefix + "PostServiceOrderResponse", extApiResponse)

		logger.debug("doCreateE2ESIin3rdONAP response body is: " + extApiResponse)

		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
		//200 OK 201 CREATED 202 ACCEPTED
		{
			logger.debug("Post ServiceOrder Received a Good Response")
			String serviceOrderId = responseObj.get("id")
			execution.setVariable(Prefix + "SuccessIndicator", true)
			execution.setVariable("ServiceOrderId", serviceOrderId)
			logger.info("Post ServiceOrderid is: " + serviceOrderId)
		}
		else{
			logger.error("Post ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode)
//			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Post ServiceOrder Received a bad response from 3rdONAP External API")
		}
        }catch(Exception e){
            logger.error("doCreateE2ESIin3rdONAP exception:" + e.getMessage())
        }

		logger.info(" ***** Exit doCreateE2ESIin3rdONAP *****")
	}


	public void getE2ESIProgressin3rdONAP(DelegateExecution execution) {
		logger.info(" ***** Started getE2ESIProgressin3rdONAP *****")
        try {

		String extAPIPath = execution.getVariable("ExternalAPIURL")
		extAPIPath += "/" + execution.getVariable("ServiceOrderId")
		logger.debug("getE2ESIProgressin3rdONAP create externalAPIURL is: " + extAPIPath)

		ExternalAPIUtil externalAPIUtil = new ExternalAPIUtilFactory().create()

		Response response = externalAPIUtil.executeExternalAPIGetCall(execution, extAPIPath)

		int responseCode = response.getStatus()
		execution.setVariable(Prefix + "GetServiceOrderResponseCode", responseCode)
		logger.debug("Get ServiceOrder response code is: " + responseCode)

		String extApiResponse = response.readEntity(String.class)
		JSONObject responseObj = new JSONObject(extApiResponse)
		execution.setVariable(Prefix + "GetServiceOrderResponse", extApiResponse)

		logger.debug("getE2ESIProgressin3rdONAP create response body is: " + extApiResponse)

		//Process Response //200 OK 201 CREATED 202 ACCEPTED
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
		{
			logger.debug("Get Create ServiceOrder Received a Good Response")

			String orderState = responseObj.get("state")
			if("REJECTED".equalsIgnoreCase(orderState)) {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "error")
				execution.setVariable("statusDescription", "Create Service Order Status is REJECTED")
				return
			}

			JSONArray items = responseObj.getJSONArray("orderItem")
			JSONObject item = items.get(0)
			JSONObject service = item.get("service")
			String sppartnerServiceId = service.get("id")
			if(sppartnerServiceId == null || sppartnerServiceId.equals("null")) {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "error")
				execution.setVariable("statusDescription", "Create Service Order Status get null sppartnerServiceId")
				logger.error("null sppartnerServiceId while getting progress from externalAPI")
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
				execution.setVariable("statusDescription", "Create Service Order Status is " + serviceOrderState)
			}
			else if("INPROGRESS".equalsIgnoreCase(serviceOrderState)) {
				execution.setVariable("progress", 40)
				execution.setVariable("status", "processing")
				execution.setVariable("statusDescription", "Create Service Order Status is " + serviceOrderState)
			}
			else if("COMPLETED".equalsIgnoreCase(serviceOrderState)) {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "finished")
				execution.setVariable("statusDescription", "Create Service Order Status is " + serviceOrderState)
			}
			else if("FAILED".equalsIgnoreCase(serviceOrderState)) {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "error")
				execution.setVariable("statusDescription", "Create Service Order Status is " + serviceOrderState)
			}
			else {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "error")
				execution.setVariable("statusDescription", "Create Service Order Status is unknown")
			}
		}
		else{
			logger.debug("Get ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode)
			execution.setVariable("progress", 100)
			execution.setVariable("status", "error")
			execution.setVariable("statusDescription", "Get Create ServiceOrder Received a bad response")
//			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Get Create ServiceOrder Received a bad response from 3rdONAP External API")
		}

        }catch(Exception e){            
            execution.setVariable("progress", 100)
            execution.setVariable("status", "error")
            execution.setVariable("statusDescription", "Get Create ServiceOrder Exception")
            logger.error("getE2ESIProgressin3rdONAP exception:" + e.getMessage())
        }
		logger.info(" ***** Exit getE2ESIProgressin3rdONAP *****")
	}

	/**
	 * delay 5 sec
	 */
	public void timeDelay(DelegateExecution execution) {
		try {
			logger.debug("going to sleep for 5 sec")
			Thread.sleep(5000)
			logger.debug("wakeup after 5 sec")
		} catch(InterruptedException e) {
			logger.error("Time Delay exception" + e)
		}
	}

	public void saveSPPartnerInAAI(DelegateExecution execution) {
		logger.info(" ***** Started saveSPPartnerInAAI *****")
        try {
		String sppartnerId = execution.getVariable(Prefix + "SppartnerServiceId")
		String sppartnerUrl = execution.getVariable(Prefix + "SppartnerUrl")
		String callSource = execution.getVariable(Prefix + "CallSource")
		String serviceInstanceId = execution.getVariable(Prefix + "ServiceInstanceId")
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String serviceType = execution.getVariable("serviceType")
		String resourceModelInvariantUuid = execution.getVariable(Prefix + "ResourceModelInvariantUuid")
		String resourceModelUuid = execution.getVariable(Prefix + "ResourceModelUuid")
		String resourceModelCustomizationUuid = execution.getVariable(Prefix + "ResourceModelCustomizationUuid")
		
		SpPartner partner = new SpPartner()
		partner.setSpPartnerId(sppartnerId)
		partner.setUrl(sppartnerUrl)
		partner.setCallsource(callSource)
		partner.setModelInvariantId(resourceModelInvariantUuid)
		partner.setModelVersionId(resourceModelUuid)
		partner.setModelCustomizationId(resourceModelCustomizationUuid)

		AAIResourcesClient client = new AAIResourcesClient()
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().spPartner(sppartnerId))
		logger.info("sending request to create sp-partner: " +  uri.toString())
		logger.info("requestbody: " + partner)
		client.create(uri, partner)

		AAIResourceUri siUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId))
		client.connect(uri, siUri)
        } catch (Exception ex) {
            String msg = "Exception in Create3rdONAPE2EServiceInstance.saveSPPartnerInAAI. " + ex.getMessage()
            logger.info(msg)
//            throw new BpmnError("MSOWorkflowException")
        }
		logger.info(" ***** Exit saveSPPartnerInAAI *****")
	}

	private void setProgressUpdateVariables(DelegateExecution execution, String body) {
		def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
		execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
		execution.setVariable("CVFMI_updateResOperStatusRequest", body)
	}

	public void postProcess(DelegateExecution execution){
		logger.info(" ***** Started postProcess *****")
		String responseCode = execution.getVariable(Prefix + "PutSppartnerResponseCode")
		String responseObj = execution.getVariable(Prefix + "PutSppartnerResponse")

		logger.info("response from AAI for put sppartner, response code :" + responseCode + "  response object :" + responseObj)
		logger.info(" ***** Exit postProcess *****")
	}

	public void sendSyncResponse (DelegateExecution execution) {
		logger.debug(" *** sendSyncResponse *** ")

		try {
			String operationStatus = "finished"
			// RESTResponse for main flow
			String resourceOperationResp = """{"operationStatus":"${operationStatus}"}""".trim()
			logger.debug(" sendSyncResponse to APIH:" + "\n" + resourceOperationResp)
			sendWorkflowResponse(execution, 202, resourceOperationResp)
			execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			logger.debug(msg)
//			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug(" ***** Exit sendSyncResopnse *****")
	}
}
