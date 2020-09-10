package org.onap.so.bpmn.infrastructure.scripts

import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonOutput

import static org.hamcrest.CoreMatchers.instanceOf

import javax.json.JsonArray
import javax.ws.rs.core.Response
import org.apache.commons.collections.map.HashedMap
import org.apache.commons.lang.StringEscapeUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtilFactory
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.db.request.beans.OperationStatus
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.aai.domain.yang.v19.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceProxy
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity

import javax.ws.rs.NotFoundException

class DoAllocateCoreNonSharedSlice extends AbstractServiceTaskProcessor {

	private static final Logger logger = LoggerFactory.getLogger( DoAllocateCoreNonSharedSlice.class);
	private CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()
	private RequestDBUtil requestDBUtil = new RequestDBUtil()
	private ExceptionUtil exceptionUtil = new ExceptionUtil()
	private JsonUtils jsonUtil = new JsonUtils()

	@Override
	public void preProcessRequest(DelegateExecution execution) {
		logger.debug("**** Enter DoAllocateCoreNonSharedSlice:::  preProcessRequest ****")

		String nssiServiceInstanceId= execution.getVariable("dummyServiceId")
		execution.setVariable("nssiServiceInstanceId", nssiServiceInstanceId)

		//Set orchestration-status as created
		execution.setVariable("orchestrationStatus", "created")

		//networkServiceName
		String networkServiceName = jsonUtil.getJsonValue(execution.getVariable("networkServiceModelInfo"), "modelName") ?: ""
		execution.setVariable("networkServiceName", networkServiceName)

		//networkServiceModelUuid
		String networkServiceModelUuid = jsonUtil.getJsonValue(execution.getVariable("networkServiceModelInfo"), "modelUuid") ?: ""
		execution.setVariable("networkServiceModelUuid", networkServiceModelUuid)


		logger.debug("**** Exit DoAllocateCoreNonSharedSlice:::  preProcessRequest ****")
	}

	void createNSSIinAAI(DelegateExecution execution) {
		logger.debug("****  Enter DoAllocateCoreNonSharedSlice ::: Enter createNSSIinAAI ****")

		String msg=""
		String serviceInstanceId= execution.getVariable("nssiServiceInstanceId")

		logger.debug("ServiceInstanceId: "+serviceInstanceId)

		try {

			String serviceType = execution.getVariable("subscriptionServiceType")
			String oStatus = execution.getVariable("orchestrationStatus")

			//Get workload context and environment context from DB
			String environmentContext = ""
			String workloadContext =""
			String modelInvariantUuid = execution.getVariable("modelInvariantUuid")

			try{
				String json = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuidString(execution,modelInvariantUuid )

				logger.debug("JSON Response from DB: "+json)

				environmentContext = jsonUtil.getJsonValue(json, "serviceResources.environmentContext") ?: ""
				workloadContext = jsonUtil.getJsonValue(json, "serviceResources.workloadContext") ?: ""

				logger.debug("Env Context is: "+ environmentContext)
				logger.debug("Workload Context is: "+ workloadContext)
			} catch(BpmnError e){
				throw e
			} catch (Exception ex){
				msg = "Exception in createNSSIinAAI ::: DoAllocateCoreNonSharedSlice  " + ex.getMessage()
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}

			String serviceInstanceName = "nssi_"+execution.getVariable("nsstName")
			ServiceInstance si = new ServiceInstance()

			si.setServiceInstanceId(execution.getVariable("nssiServiceInstanceId"))
			si.setServiceInstanceName(serviceInstanceName)
			si.setServiceType(serviceType)
			si.setServiceRole("nssi")
			si.setOrchestrationStatus(oStatus)
			si.setModelInvariantId(modelInvariantUuid)
			si.setModelVersionId(execution.getVariable("modelUuid"))
			si.setEnvironmentContext(environmentContext)
			si.setWorkloadContext(workloadContext)

			logger.debug("AAI service Instance Request Payload : "+si.toString())

			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), serviceType, serviceInstanceId)
			Response response = getAAIClient().create(uri, si)

			if(response.getStatus()!=200) {
				exceptionUtil.buildAndThrowWorkflowException(execution, response.getStatus(), "AAI instance creation failed")
			}

			execution.setVariable("nssiServiceInstance", si)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in create AAI Instance" + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}

		logger.debug("**** Exit DoAllocateCoreNonSharedSlice ::: Enter createNSSIinAAI ****")
	}

	public void prepareServiceOrderRequest(DelegateExecution execution) {

		logger.debug("**** Enter DoAllocateCoreNonSharedSlice :::  prepareServiceOrderRequest ****")
		String extAPIPath = UrnPropertiesReader.getVariable("extapi.endpoint", execution) + '/serviceOrder'
		execution.setVariable("ExternalAPIURL", extAPIPath)
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> serviceOrder = new LinkedHashMap()

		//ExternalId
		serviceOrder.put("externalId", "ONAP001")

		//Requested Start Date
		String requestedStartDate = utils.generateCurrentTimeInUtc()
		String requestedCompletionDate = utils.generateCurrentTimeInUtc()

		serviceOrder.put("requestedStartDate", requestedStartDate)
		serviceOrder.put("requestedCompletionDate", requestedCompletionDate)

		//RelatedParty Fields
		String relatedPartyId = execution.getVariable("globalSubscriberId")
		String relatedPartyRole = "ONAPcustomer"

		Map<String, String> relatedParty = new LinkedHashMap()
		relatedParty.put("id", relatedPartyId)
		relatedParty.put("role", relatedPartyRole)

		List<Map<String, String>> relatedPartyList = new ArrayList()
		relatedPartyList.add(relatedParty)

		serviceOrder.put("relatedParty", relatedPartyList)


		Map<String, Object> orderItem = new LinkedHashMap()

		//orderItem id
		String orderItemId = "1"
		orderItem.put("id", orderItemId)

		//order item action will always be add as we are triggering request for instantiation
		String orderItemAction = "add"
		orderItem.put("action", orderItemAction)

		// service Details
		Map<String, Object> service = new LinkedHashMap()

		//ServiceName
		String serviceName= "nsi_"+execution.getVariable("networkServiceName")
		service.put("name",  serviceName)

		// Service Type
		service.put("serviceType", execution.getVariable("serviceType"))

		//Service State
		service.put("serviceState", "active")

		Map<String, String> serviceSpecification = new LinkedHashMap()
		serviceSpecification.put("id", execution.getVariable("networkServiceModelUuid"))

		service.put("serviceSpecification", serviceSpecification)

		//serviceCharacteristic List
		List serviceCharacteristicList = new ArrayList()


		//Map<String, Object> serviceCharacteristic = execution.getVariable("sliceProfile")
		Map<String, Object> serviceCharacteristic = objectMapper.readValue(execution.getVariable("sliceProfile"), Map.class);

		List serviceCharacteristicListMap = retrieveServiceCharacteristicsAsKeyValue(serviceCharacteristic)

		logger.debug("serviceCharacteristicListMap  "+serviceCharacteristicListMap)

		serviceCharacteristicList.add(serviceCharacteristic)

		//service.put("serviceCharacteristic", serviceCharacteristicList)
		service.put("serviceCharacteristic", serviceCharacteristicListMap)

		orderItem.put("service", service)

		List<Map<String, String>> orderItemList = new ArrayList()
		orderItemList.add(orderItem)

		serviceOrder.put("orderItem", orderItemList)

		String jsonServiceOrder = objectMapper.writeValueAsString(serviceOrder);

		logger.debug("******* ServiceOrder :: "+jsonServiceOrder)
		execution.setVariable("serviceOrderRequest", jsonServiceOrder)

		logger.debug("**** Exit DoAllocateCoreNonSharedSlice ::: prepareServiceOrderRequest****")
	}

	private List retrieveServiceCharacteristicsAsKeyValue(Map serviceCharacteristics) {

		logger.debug("**** Enter DoAllocateCoreNonSharedSlice ::: retrieveServiceCharacteristicsAsKeyValue ****")

		List serviceCharacteristicsList = new ArrayList()
		ObjectMapper mapperObj = new ObjectMapper();
		Map<String, Object> serviceCharacteristicsObject = new LinkedHashMap()

		for (Map.Entry<String, Integer> entry : serviceCharacteristics.entrySet()) {
			Map<String, Object> ServiceCharacteristicValueObject = new LinkedHashMap<>()
			System.out.println(entry.getKey() + ":" + entry.getValue());
			//For G Release we are sending single value from  snssaiList
			if(entry.getKey().equals("snssaiList")) {
				List sNssaiValue = entry.getValue()
				serviceCharacteristicsObject.put("name", "s-nssai")
				ServiceCharacteristicValueObject.put("serviceCharacteristicValue", sNssaiValue.get(0))
				serviceCharacteristicsObject.put("value", ServiceCharacteristicValueObject)
			}
		}

		serviceCharacteristicsList.add(serviceCharacteristicsObject)

		logger.debug("**** Exit DoAllocateCoreNonSharedSlice ::: retrieveServiceCharacteristicsAsKeyValue ****")
		return serviceCharacteristicsList
	}

	public void postNBIServiceOrder(DelegateExecution execution) {
		logger.debug("**** Enter DoAllocateCoreNonSharedSlice ::: postNBIServiceOrder ****")

		String msg=""
		try {
			String extAPIPath = execution.getVariable("ExternalAPIURL")
			String payload = execution.getVariable("serviceOrderRequest")
			logger.debug("externalAPIURL is: " + extAPIPath)
			logger.debug("ServiceOrder payload is: " + payload)

			ExternalAPIUtil externalAPIUtil = new ExternalAPIUtilFactory().create()
			execution.setVariable("ServiceOrderId", "")

			Response response = externalAPIUtil.executeExternalAPIPostCall(execution, extAPIPath, payload)

			int responseCode = response.getStatus()
			execution.setVariable("PostServiceOrderResponseCode", responseCode)
			logger.debug("Post ServiceOrder response code is: " + responseCode)

			String extApiResponse = response.readEntity(String.class)
			JSONObject responseObj = new JSONObject(extApiResponse)
			execution.setVariable("PostServiceOrderResponse", extApiResponse)

			logger.debug("ServiceOrder response body is: " + extApiResponse)

			//Process Response
			if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
				//200 OK 201 CREATED 202 ACCEPTED
			{
				logger.debug("Post ServiceOrder Received a Good Response")
				String serviceOrderId = responseObj.get("id")
				execution.setVariable("ServiceOrderId", serviceOrderId)
				logger.info("Post ServiceOrderid is: " + serviceOrderId)
			}
			else{
				exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Post ServiceOrder Received a bad response from extAPI serviceOrder API")
			}
		}catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in ServiceOrder ExtAPI" + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug("**** Exit DoAllocateCoreNonSharedSlice ::: postNBIServiceOrder ****")
	}

	public void getNBIServiceOrderProgress(DelegateExecution execution) {
		logger.debug("**** Enter DoAllocateCoreNonSharedSlice ::: getNBIServiceOrderProgress ****")
		try {

			String extAPIPath = execution.getVariable("ExternalAPIURL")
			extAPIPath += "/" + execution.getVariable("ServiceOrderId")
			logger.debug("getNBIServiceOrderProgress externalAPIURL is: " + extAPIPath)

			ExternalAPIUtil externalAPIUtil = new ExternalAPIUtilFactory().create()

			Response response = externalAPIUtil.executeExternalAPIGetCall(execution, extAPIPath)

			int responseCode = response.getStatus()
			execution.setVariable("GetServiceOrderResponseCode", responseCode)
			logger.debug("Get ServiceOrder response code is: " + responseCode)

			String extApiResponse = response.readEntity(String.class)
			JSONObject responseObj = new JSONObject(extApiResponse)
			execution.setVariable("GetServiceOrderResponse", extApiResponse)

			logger.debug("Create response body is: " + extApiResponse)

			//Process Response //200 OK 201 CREATED 202 ACCEPTED
			if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			{
				logger.debug("Get Create ServiceOrder Received a Good Response")

				String orderState = responseObj.get("state")
				if("REJECTED".equalsIgnoreCase(orderState)) {
					execution.setVariable("progress", 100)
					execution.setVariable("status", "error")
					execution.setVariable("statusDescription", "Create Service Order Status is REJECTED")
					setResourceOperationStatus(execution)
					return
				}

				JSONArray items = responseObj.getJSONArray("orderItem")
				JSONObject item = items.get(0)
				JSONObject service = item.get("service")
				String networkServiceId = service.get("id")

				if (networkServiceId == null || networkServiceId.equals("null")) {
					execution.setVariable("progress", 100)
					execution.setVariable("status", "error")
					execution.setVariable("statusDescription", "Create Service Order Status get null networkServiceId")
					setResourceOperationStatus(execution)
					logger.error("networkServiceId null while getting progress from externalAPI")
					return
				}

				execution.setVariable("networkServiceId", networkServiceId)

				String serviceOrderState = item.get("state")
				//execution.setVariable("SuccessIndicator", true)
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
					execution.setVariable("status", "completed")
					execution.setVariable("statusDescription", "Create Service Order Status is " + serviceOrderState)
				}
				else if("FAILED".equalsIgnoreCase(serviceOrderState)) {
					execution.setVariable("progress", 100)
					execution.setVariable("status", "error")
					execution.setVariable("statusDescription", "Create Service Order Status is " + serviceOrderState)
					setResourceOperationStatus(execution)
				}
				else {
					execution.setVariable("progress", 100)
					execution.setVariable("status", "error")
					execution.setVariable("statusDescription", "Create Service Order Status is unknown")
					setResourceOperationStatus(execution)
				}
			}
			else{
				logger.debug("Get ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode)
				execution.setVariable("progress", 100)
				execution.setVariable("status", "error")
				execution.setVariable("statusDescription", "Get Create ServiceOrder Received a bad response")
				setResourceOperationStatus(execution)
			}

		}catch(Exception e){
			execution.setVariable("progress", 100)
			execution.setVariable("status", "error")
			execution.setVariable("statusDescription", "Get Create ServiceOrder Exception")
			setResourceOperationStatus(execution)
			logger.error("getNBIServiceOrderProgress exception:" + e.getMessage())
		}
		logger.debug("**** Exit DoAllocateCoreNonSharedSlice ::: getNBIServiceOrderProgress ****")
	}


	/**
	 * delay 5 sec
	 */
	public void timeDelay(DelegateExecution execution) {
		try {
			logger.debug("**** DoAllocateCoreNonSharedSlice ::: timeDelay going to sleep for 5 sec")
			Thread.sleep(5000)
			logger.debug("**** DoAllocateCoreNonSharedSlice ::: timeDelay wakeup after 5 sec")
		} catch(InterruptedException e) {
			logger.error("**** DoAllocateCoreNonSharedSlice ::: timeDelay exception" + e)
		}
	}


	void updateRelationship(DelegateExecution execution) {
		logger.debug("**** Enter DoAllocateCoreNonSharedSlice ::: updateRelationship ****")

		String networkServiceInstanceId = execution.getVariable("networkServiceId")
		String nssiId = execution.getVariable("nssiServiceInstanceId")
		
		String globalCustId = execution.getVariable("globalSubscriberId")
		String serviceType = execution.getVariable("serviceType")

		try{

			//Update NSSI orchestration status nssiServiceInstance
			ServiceInstance si = execution.getVariable("nssiServiceInstance")
			si.setOrchestrationStatus("activated")

			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
			globalCustId, serviceType, networkServiceInstanceId)
			try {
				getAAIClient().update(uri, si)
			} catch (Exception e) {
				logger.info("Update OrchestrationStatus in AAI failed")
				String msg = "Update OrchestrationStatus in AAI failed, " + e.getMessage()
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}

			//URI for NSSI
			AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId);

			//URI for Network Service Instance
			AAIResourceUri networkServiceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, networkServiceInstanceId)



			// Update Relationship in AAI
			Response response = getAAIClient().connect(nssiUri, networkServiceInstanceUri, AAIEdgeLabel.COMPOSED_OF);

			if(response.getStatus()!=200 || response.getStatus()!=201 || response.getStatus()!=202) {
				exceptionUtil.buildAndThrowWorkflowException(execution, response.getStatus(), "Set association of NSSI and Network service instance has failed in AAI")
			} else {
				execution.setVariable("progress", 100)
				execution.setVariable("status", "finished")
				execution.setVariable("statusDescription", "DoAllocateCoreNonSharedNSSI success")
				setResourceOperationStatus(execution)
			}
		}catch(Exception ex) {
			String msg = "Exception while creating relationship " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}

		logger.debug("**** Exit DoAllocateCoreNonSharedSlice ::: updateRelationship ****")
	}

	/**
	 * prepare ResourceOperation status
	 * @param execution
	 * @param operationType
	 */
	private void setResourceOperationStatus(DelegateExecution execution) {

		logger.debug("**** Enter DoAllocateCoreNonSharedSlice ::: setResourceOperationStatus ****")

		ResourceOperationStatus operationStatus = new ResourceOperationStatus()
		operationStatus.setStatus(execution.getVariable("status"))
		operationStatus.setProgress(execution.getVariable("progress"))
		operationStatus.setStatusDescription(execution.getVariable("statusDescription"))

		requestDBUtil.prepareUpdateResourceOperationStatus(execution, operationStatus)

		logger.debug("**** Exit DoAllocateCoreNonSharedSlice ::: setResourceOperationStatus ****")
	}
}