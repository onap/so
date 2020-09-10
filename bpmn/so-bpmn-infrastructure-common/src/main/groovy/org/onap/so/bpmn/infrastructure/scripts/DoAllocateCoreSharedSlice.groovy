package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.serviceinstancebeans.CloudConfiguration
import org.onap.so.serviceinstancebeans.ModelInfo
import org.onap.so.serviceinstancebeans.ModelType
import org.onap.so.serviceinstancebeans.OwningEntity
import org.onap.so.serviceinstancebeans.Project
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.rest.catalog.beans.Vnf
import org.onap.so.serviceinstancebeans.RequestDetails
import org.onap.so.serviceinstancebeans.RequestInfo
import org.onap.so.serviceinstancebeans.RequestParameters
import org.onap.so.serviceinstancebeans.SubscriberInfo
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.RelatedToProperty
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.RelationshipData
import org.onap.aai.domain.yang.RelationshipList
import org.onap.aai.domain.yang.ServiceInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.db.request.beans.OperationStatus
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import com.fasterxml.jackson.databind.ObjectMapper

import static org.apache.commons.lang3.StringUtils.isBlank

import javax.ws.rs.NotFoundException
import javax.ws.rs.core.Response

class DoAllocateCoreSharedSlice extends AbstractServiceTaskProcessor {

	private static final Logger logger = LoggerFactory.getLogger(DoAllocateCoreSharedSlice.class);
	private CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()
	private RequestDBUtil requestDBUtil = new RequestDBUtil()
	private ExceptionUtil exceptionUtil = new ExceptionUtil()
	private JsonUtils jsonUtil = new JsonUtils()

	private final Long TIMEOUT = 60 * 60 * 1000

	@Override
	public void preProcessRequest(DelegateExecution execution) {

		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: preProcessRequest ****")

		//Get NSSI Solutions
		String nssisolutions = execution.getVariable("solutions")

		//Get First Object
		String nssiSolution = jsonUtil.StringArrayToList(nssisolutions).get(0)
		String nssiId = jsonUtil.getJsonValue(nssiSolution, "NSSIId")

		if (isBlank(nssiId)) {
			String msg = "solution nssiId is null"
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		} else {
			execution.setVariable("nssiId", nssiId)
		}

		logger.debug("**** Exit DoAllocateCoreSharedSlice ::: preProcessRequest ****")

	}

	public void getNetworkInstanceAssociatedWithNssiId(DelegateExecution execution) {

		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: getNetworkInstanceAssociatedWithNssiId ****")

		//NSSI Id as service Instance Id to get from AAI
		String serviceInstanceId = execution.getVariable("nssiId")

		String errorMsg = "query Network Service Instance from AAI failed"
		AAIResultWrapper wrapper = queryAAI(execution, AAIObjectType.SERVICE_INSTANCE, serviceInstanceId, errorMsg)
		Optional<ServiceInstance> nssi = wrapper.asBean(ServiceInstance.class)

		String networkServiceInstanceName = ""
		String networkServiceInstanceId =""
		if(nssi.isPresent()) {

			execution.setVariable("serviceInstanceName", nssi.get().getServiceInstanceName())
			List<Relationship> relationshipList = nssi.get().getRelationshipList()?.getRelationship()

			for (Relationship relationship : relationshipList) {
				String relatedTo = relationship.getRelatedTo()
				if (relatedTo == "service-instance") {
					List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
					List<RelatedToProperty> relatedToPropertyList = relationship.getRelatedToProperty()
					for (RelationshipData relationshipData : relationshipDataList) {
						if (relationshipData.getRelationshipKey() == "service-instance.service-instance-id") {
							execution.setVariable("networkServiceInstanceId", relationshipData.getRelationshipValue())
						}
					}
					for (RelatedToProperty relatedToProperty : relatedToPropertyList) {
						if (relatedToProperty.getPropertyKey() == "service-instance.service-instance-name") {
							execution.setVariable("networkServiceInstanceName", relatedToProperty.getPropertyValue())
						}
					}
					break
				}
			}
		}

		logger.debug("NSSI Id: ${serviceInstanceId}, network Service Instance Id: ${networkServiceInstanceId}, networkServiceInstanceName: ${networkServiceInstanceName}")

		//Get ServiceInstance Relationships
		getServiceInstanceRelationships(execution)

		//Get Vnf Relationships
		getVnfRelationships(execution)

		logger.debug("**** Exit DoAllocateCoreSharedSlice ::: getNetworkInstanceAssociatedWithNssiId ****")
	}

	private void getServiceInstanceRelationships(DelegateExecution execution) {

		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: getServiceInstanceRelationships ****")

		String serviceInstanceId = execution.getVariable("networkServiceInstanceId")

		String errorMsg = "query Network Service Instance from AAI failed"
		AAIResultWrapper wrapper = queryAAI(execution, AAIObjectType.SERVICE_INSTANCE, serviceInstanceId, errorMsg)
		Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)

		String networkServiceModelInvariantUuid = si.get().getModelInvariantId()
		execution.setVariable("networkServiceModelInvariantUuid", networkServiceModelInvariantUuid)
		if(si.isPresent()) {
			List<Relationship> relationshipList = si.get().getRelationshipList()?.getRelationship()
			for (Relationship relationship : relationshipList) {
				String relatedTo = relationship.getRelatedTo()
				if (relatedTo == "owning-entity") {
					List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
					for (RelationshipData relationshipData : relationshipDataList) {
						if (relationshipData.getRelationshipKey() == "owning-entity.owning-entity-id") {
							execution.setVariable("owningEntityId", relationshipData.getRelationshipValue())
						}
					}
				} else if (relatedTo == "generic-vnf") {
					List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
					List<RelatedToProperty> relatedToPropertyList = relationship.getRelatedToProperty()

					//Get VnfId
					for (RelationshipData relationshipData : relationshipDataList) {
						if (relationshipData.getRelationshipKey() == "generic-vnf.vnf-id") {
							execution.setVariable("vnfId", relationshipData.getRelationshipValue())
						}
					}

					//Get Vnf Name Check If necessary
					for (RelatedToProperty relatedToProperty : relatedToPropertyList) {
						if (relatedToProperty.getPropertyKey() == "generic-vnf.vnf-name") {
							execution.setVariable("vnfName", relatedToProperty.getPropertyValue())
						}
					}
				} else if (relatedTo == "project") {
					List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
					for (RelationshipData relationshipData : relationshipDataList) {
						if (relationshipData.getRelationshipKey() == "project.project-name") {
							execution.setVariable("projectName", relationshipData.getRelationshipValue())
						}
					}
				}
			}

			logger.debug("**** Exit DoAllocateCoreSharedSlice ::: getServiceInstanceRelationships ****")
		}
	}

	private void getVnfRelationships(DelegateExecution execution) {

		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: getVnfRelationships ****")

		String msg = "query Generic Vnf from AAI failed"

		//AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(aaiObjectType, globalSubscriberId, serviceType, instanceId)
		try {
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, execution.getVariable('vnfId'))
			if (!getAAIClient().exists(uri)) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
			}
			AAIResultWrapper wrapper = getAAIClient().get(uri, NotFoundException.class)

			Optional<GenericVnf> vnf = wrapper.asBean(GenericVnf.class)

			if(vnf.isPresent()) {
				List<Relationship> relationshipList = vnf.get().getRelationshipList()?.getRelationship()
				for (Relationship relationship : relationshipList) {
					String relatedTo = relationship.getRelatedTo()
					if (relatedTo == "tenant") {
						List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
						for (RelationshipData relationshipData : relationshipDataList) {
							if (relationshipData.getRelationshipKey() == "tenant.tenant-id") {
								execution.setVariable("tenantId", relationshipData.getRelationshipValue())
							}
						}
					} else if (relatedTo == "cloud-region") {
						List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
						//List<RelatedToProperty> relatedToPropertyList = relationship.getRelatedToProperty()

						for (RelationshipData relationshipData : relationshipDataList) {
							if (relationshipData.getRelationshipKey() == "cloud-region.cloud-owner") {
								execution.setVariable("cloudOwner", relationshipData.getRelationshipValue())
							} else if (relationshipData.getRelationshipKey() == "cloud-region.cloud-region-id") {
								execution.setVariable("lcpCloudRegionId", relationshipData.getRelationshipValue())
							}
						}
					} else if (relatedTo == "platform") {
						List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
						for (RelationshipData relationshipData : relationshipDataList) {
							if (relationshipData.getRelationshipKey() == "platform.platform-name") {
								execution.setVariable("platformName", relationshipData.getRelationshipValue())
							}
						}
					} else if (relatedTo == "line-of-business") {
						List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
						for (RelationshipData relationshipData : relationshipDataList) {
							if (relationshipData.getRelationshipKey() == "line-of-business.line-of-business-name") {
								execution.setVariable("lineOfBusinessName", relationshipData.getRelationshipValue())
							}
						}
					}
				}
			}
		} catch(BpmnError e){
			throw e
		} catch (Exception ex){
			msg = "Exception in getVnfRelationships " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug("**** Exit DoAllocateCoreSharedSlice ::: getVnfRelationships ****")
	}


	/**
	 * query AAI
	 * @param execution
	 * @param aaiObjectType
	 * @param instanceId
	 * @return AAIResultWrapper
	 */
	private AAIResultWrapper queryAAI(DelegateExecution execution, AAIObjectType aaiObjectType, String instanceId, String errorMsg) {
		
		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: queryAAI ****")
		
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String serviceType = execution.getVariable("serviceType")

		AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(aaiObjectType, globalSubscriberId, serviceType, instanceId)
		if (!getAAIClient().exists(resourceUri)) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMsg)
		}
		AAIResultWrapper wrapper = getAAIClient().get(resourceUri, NotFoundException.class)
		
		logger.debug("**** Exit DoAllocateCoreSharedSlice ::: queryAAI ****")
		
		return wrapper
	}

	public void getServiceVNFAndVFsFromCatalogDB(DelegateExecution execution) {
		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: getServiceVNFAndVFsFromCatalogDB ****")   

		String serviceVnfs="";
		String msg=""

		String modelInvariantUuid = execution.getVariable("networkServiceModelInvariantUuid")

		try{
			CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()
			String json = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuidString(execution, modelInvariantUuid)
			logger.debug("***** JSON IS: "+json)

			serviceVnfs = jsonUtil.getJsonValue(json, "serviceResources.serviceVnfs") ?: ""
			String serviceModelInfo = jsonUtil.getJsonValue(json, "serviceResources.modelInfo") ?: ""
			execution.setVariable("serviceModelInfo", serviceModelInfo)
			execution.setVariable("serviceVnfs",serviceVnfs)
			logger.debug("***** serviceVnfs is: "+ serviceVnfs)
		}catch(BpmnError e){
			throw e
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug("**** Exit DoAllocateCoreSharedSlice ::: getServiceVNFAndVFsFromCatalogDB ****")
	}

	public void prepareSOMacroRequestPayLoad(DelegateExecution execution) {

		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: prepareSOMacroRequestPayLoad ****")

		ObjectMapper mapper = new ObjectMapper();
		List vnfList = mapper.readValue(execution.getVariable("serviceVnfs"), List.class);     

		logger.debug("vnfList:  "+vnfList)

		Map<String,Object> serviceMap = mapper.readValue(execution.getVariable("serviceModelInfo"), Map.class);

		ModelInfo serviceModelInfo = new ModelInfo()
		serviceModelInfo.setModelType(ModelType.service)
		serviceModelInfo.setModelInvariantId(serviceMap.get("modelInvariantUuid"))
		serviceModelInfo.setModelVersionId(serviceMap.get("modelUuid"))
		serviceModelInfo.setModelName(serviceMap.get("modelName"))
		serviceModelInfo.setModelVersion(serviceMap.get("modelVersion"))

		logger.debug("serviceModelInfo:  "+serviceModelInfo)

		Map vnfMap = vnfList.get(0)
		ModelInfo vnfModelInfo = vnfMap.get("modelInfo")
		logger.debug("vnfModelInfo "+vnfModelInfo)

		//List of VFModules
		List<Map<String, Object>> vfModuleList = vnfMap.get("vfModules")
		logger.debug("vfModuleList "+vfModuleList)

		//List of VfModules
		List<ModelInfo> vfModelInfoList = new ArrayList<>()

		//Traverse VFModules List and add in vfModelInfoList
		for (vfModule in vfModuleList) {
			ModelInfo vfModelInfo = vfModule.get("modelInfo")
			logger.debug("vfModelInfo "+vfModelInfo)
			vfModelInfoList.add(vfModelInfo)
		}

		//RequestInfo
		RequestInfo requestInfo = new RequestInfo()

		//Dummy Product FamilyId == Since it is put, shouldn't we give same value as post one?
		requestInfo.setProductFamilyId("test1234")
		requestInfo.setSource("VID")
		requestInfo.setInstanceName(execution.getVariable("networkServiceInstanceName"))
		requestInfo.setSuppressRollback(false)
		requestInfo.setRequestorId("NBI")

		//Service Level InstanceParams
		List<Map<String, Object>> serviceParams = new ArrayList<>()

		//Cloud Configuration
		CloudConfiguration cloudConfiguration = new CloudConfiguration()
		cloudConfiguration.setLcpCloudRegionId(execution.getVariable("lcpCloudRegionId"))
		cloudConfiguration.setTenantId(execution.getVariable("tenantId"))
		cloudConfiguration.setCloudOwner(execution.getVariable("cloudOwner"))

		//VFModules List
		List<Map<String, Object>> vfModules = new ArrayList<>()
		for (ModelInfo vfModuleModelInfo : vfModelInfoList) {
			//Individual VFModule List
			Map<String, Object> vfModuleValues = new LinkedHashMap<>()
			vfModuleValues.put("modelInfo", vfModuleModelInfo)
			vfModuleValues.put("instanceName", vfModuleModelInfo.getModelInstanceName())

			List<Map<String, Object>> vfModuleInstanceParams = new ArrayList<>()
			vfModuleValues.put("instanceParams", vfModuleInstanceParams)
		}

		//Vnf intsanceParams
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> sliceProfile = objectMapper.readValue(execution.getVariable("sliceProfile"), Map.class);

		//List of VnfInstanceParams
		List vnfInstanceParamsList = new ArrayList<>()

		Map<String, Object> vnfInstanceParams= new LinkedHashMap<>()
		List<String> nssaiList = sliceProfile.get("snssaiList")
		if(!nssaiList.isEmpty()) {
			//Get First NSSAI value
			vnfInstanceParams.put("snssai", nssaiList.get(0))
		}

		vnfInstanceParamsList.add(vnfInstanceParams)

		//Vnf Values
		Map<String, Object> vnfValues = new LinkedHashMap<>()
		vnfValues.put("lineOfBusiness", execution.getVariable("lineOfBusiness"))
		vnfValues.put("productFamilyId", execution.getVariable("productFamilyId"))
		vnfValues.put("cloudConfiguration", cloudConfiguration)
		vnfValues.put("vfModules", vfModules)
		vnfValues.put("modelInfo", vnfModelInfo)
		vnfValues.put("instanceName", execution.getVariable("vnfInstanceName"))
		vnfValues.put("instanceParams",vnfInstanceParamsList)

		//Service Level Resources
		Map<String, Object> serviceResources = new LinkedHashMap<>()
		serviceResources.put("vnfs", vnfValues)

		//Service Values
		Map<String, Object> serviceValues = new LinkedHashMap<>()
		serviceValues.put("modelInfo", serviceModelInfo)
		serviceValues.put("instanceName", execution.getVariable("networkServiceInstanceName"))
		serviceValues.put("resources", serviceResources)
		serviceValues.put("instanceParams", serviceParams)

		//UserParams Values
		Map<String, Object> userParamsValues = new LinkedHashMap<>()
		userParamsValues.put("Homing_Solution", "none")
		userParamsValues.put("service", serviceValues)

		//UserParams
		List<Map<String, Object>> userParams = new ArrayList<>()
		userParams.add(userParamsValues)

		//Request Parameters
		RequestParameters requestParameters = new RequestParameters()
		requestParameters.setaLaCarte(false)
		requestParameters.setSubscriptionServiceType(execution.getVariable("serviceType"))
		requestParameters.setUserParams(userParams)


		//SubscriberInfo
		SubscriberInfo subscriberInfo = new SubscriberInfo()
		subscriberInfo.setGlobalSubscriberId(execution.getVariable("globalSubscriberId"))

		//Owning Entity
		OwningEntity owningEntity = new OwningEntity()
		owningEntity.setOwningEntityId(execution.getVariable("owningEntityId"))

		//Project
		Project project = new Project()
		project.setProjectName(execution.getVariable("projectName"))

		RequestDetails requestDetails = new RequestDetails()
		requestDetails.setModelInfo(serviceModelInfo)
		requestDetails.setSubscriberInfo(subscriberInfo)
		requestDetails.setRequestInfo(requestInfo)
		requestDetails.setRequestParameters(requestParameters)
		requestDetails.setCloudConfiguration(cloudConfiguration)
		requestDetails.setOwningEntity(owningEntity)

		String requestPayLoad = objectMapper.writeValueAsString(requestDetails)

		logger.debug("requestDetails "+requestPayLoad)
		execution.setVariable("requestPayLoad", requestPayLoad)

		logger.debug("**** Exit DoAllocateCoreSharedSlice ::: prepareSOMacroRequestPayLoad ****")
	}

	public void sendPutRequestToSOMacro(DelegateExecution execution) {

		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: sendPutRequestToSOMacro ****")
		try {
			String msoEndpoint = UrnPropertiesReader.getVariable("mso.infra.endpoint.url", execution)
			String url = msoEndpoint+"/serviceInstantiation/v7/serviceInstances/"+execution.getVariable("networkServiceInstanceId")+"/vnfs/"+execution.getVariable("vnfId")   

			String requestBody = execution.getVariable("requestPayload")

			String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)
			String basicAuth =  UrnPropertiesReader.getVariable("mso.infra.endpoint.auth", execution)
			String basicAuthValue = utils.encrypt(basicAuth, msoKey)
			String encodeString = utils.getBasicAuth(basicAuthValue, msoKey)

			HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.SO)
			httpClient.addAdditionalHeader("Authorization", encodeString)
			httpClient.addAdditionalHeader("Accept", "application/json")
			Response httpResponse = httpClient.put(requestBody)
			handleSOResponse(httpResponse, execution)

		} catch (BpmnError e) {
			throw e
		} catch (any) {
			String msg = "Exception in DeleteCommunicationService.preRequestSend2NSMF. " + any.getCause()
			logger.error(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}

		logger.debug("**** Exit DoAllocateCoreSharedSlice ::: sendPostRequestToSOMacro ****")

	}

	/**
	 * Handle SO Response for PUT and prepare update operation status
	 * @param execution
	 */
	private void handleSOResponse(Response httpResponse, DelegateExecution execution){
		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: handleSOResponse ****")

		int soResponseCode = httpResponse.getStatus()
		logger.debug("soResponseCode : "+soResponseCode)

		if (soResponseCode >= 200 && soResponseCode < 204 && httpResponse.hasEntity()) {
			String soResponse = httpResponse.readEntity(String.class)

			String operationId = execution.getVariable("operationId")
			def macroOperationId = jsonUtil.getJsonValue(soResponse, "operationId")
			execution.setVariable("macroOperationId", macroOperationId)
			execution.setVariable("nssiOperationId", operationId)
			execution.setVariable("progress","20")
			execution.setVariable("statusDescription","waiting for SO PUT to finish")
			execution.setVariable("currentCycle",0)
			execution.setVariable("isSOTimeOut", "no")
			execution.setVariable("isSOResponseSucceed","yes")
		}
		else
		{
			String serviceName = execution.getVariable("serviceInstanceName")
			execution.setVariable("progress", "100")
			execution.setVariable("result", "error")
			execution.setVariable("operationContent", "PUT service failure.")
			execution.setVariable("reason","SO asynchronous response failed, status Code:${soResponseCode}")
			execution.setVariable("isSOResponseSucceed","no")
			logger.error("SO async response error，SO ResponseCode：${soResponseCode}，serivceName:${serviceName}")
			setResourceOperationStatus(execution)
		}

		logger.debug("**** Exit DoAllocateCoreSharedSlice ::: handleSOResponse ****")
	}

	/**
	 * prepare to call sub process CheckProcessStatus
	 * @param execution
	 */
	void prepareCallCheckProcessStatus(DelegateExecution execution){
		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: prepareCallCheckProcessStatus ****")

		def successConditions = new ArrayList<>()
		successConditions.add("finished")
		execution.setVariable("successConditions", successConditions)

		def errorConditions = new ArrayList<>()
		errorConditions.add("error")
		execution.setVariable("errorConditions", errorConditions)

		execution.setVariable("processServiceType", "Network service")
		execution.setVariable("subOperationType", "PUT")
		execution.setVariable("initProgress", 20)
		execution.setVariable("endProgress",90)

		execution.setVariable("timeOut", TIMEOUT)

		//Changing operationId as macroOperationId to pass operationId in subflow to check MacroFlow status
		String macroOperationId = execution.getVariable("macroOperationId")
		execution.setVariable("operationId", macroOperationId)

		logger.debug("**** Exit DoAllocateCoreSharedSlice ::: prepareCallCheckProcessStatus ****")
	}

	void prepareUpdateResourceOperationStatus(DelegateExecution execution) {

		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: prepareUpdateResourceOperationStatus ****")

		String nssiOperationId = execution.getVariable("nssiOperationId")
		execution.setVariable("operationId", nssiOperationId)
		//Prepare Update Status for PUT failure and success
		if(execution.getVariable("isTimeOut").equals("YES")) {
			execution.setVariable("status", "finished")
			execution.setVariable("progress", "100")
			execution.setVariable("statusDescription", "AllocteCoreNSSI failure.")
			logger.debug("prepareFailureStatus,result:${execution.getVariable("result")}, reason: ${execution.getVariable("reason")}")

		} else {
			execution.setVariable("progress", "100")
			execution.setVariable("status", "finished")
			execution.setVariable("operationContent", "AllocteCoreNSSI successful.")
			logger.debug("prepareFailureStatus,result:${execution.getVariable("result")}, reason: ${execution.getVariable("reason")}")
		}

		setResourceOperationStatus(execution)
		logger.debug("**** Exit DoAllocateCoreSharedSlice ::: prepareUpdateResourceOperationStatus ****")

	}

	/**
	 * prepare ResourceOperation status
	 * @param execution
	 * @param operationType
	 */
	private void setResourceOperationStatus(DelegateExecution execution) {

		logger.debug("**** Enter DoAllocateCoreSharedSlice ::: setResourceOperationStatus ****")

		ResourceOperationStatus operationStatus = new ResourceOperationStatus()
		operationStatus.setStatus(execution.getVariable("status"))
		operationStatus.setProgress(execution.getVariable("progress"))
		operationStatus.setStatusDescription(execution.getVariable("statusDescription"))

		requestDBUtil.prepareUpdateResourceOperationStatus(execution, operationStatus)

		logger.debug("**** Exit DoAllocateCoreSharedSlice ::: setResourceOperationStatus ****")
	}


}
