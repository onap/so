/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020  Tech Mahindra
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
import org.onap.so.serviceinstancebeans.LineOfBusiness
import org.onap.so.serviceinstancebeans.ModelInfo
import org.onap.so.serviceinstancebeans.ModelType
import org.onap.so.serviceinstancebeans.OwningEntity
import org.onap.so.serviceinstancebeans.Platform
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
import org.onap.aai.domain.yang.v19.SliceProfile
import org.onap.aai.domain.yang.v19.SliceProfiles
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

import javax.ws.rs.NotFoundException
import javax.ws.rs.core.Response

class DoActivateCoreNSSI extends AbstractServiceTaskProcessor {
	String Prefix="DACTCNSSI_"
	private static final Logger logger = LoggerFactory.getLogger(DoActivateCoreNSSI.class);
	CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()
	private RequestDBUtil requestDBUtil = new RequestDBUtil()
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	ObjectMapper mapper = new ObjectMapper();

	JsonUtils jsonUtil = new JsonUtils()

	private final Long TIMEOUT = 60 * 60 * 1000

	@Override
	public void preProcessRequest(DelegateExecution execution) {
		logger.debug("**** Enter DoActivateCoreNSSI ::: preProcessRequest ****")

		String operationType = execution.getVariable("operationType")
		String oStatus= ""

		if(operationType.equals("activateInstance")) {
			oStatus ="activated"
		} else {
			oStatus ="deactivated"
		}

		execution.setVariable("oStatus", oStatus)
		String sNssaiListAsString = jsonUtil.getJsonValue(execution.getVariable("sliceParams"), "snssaiList")

		logger.debug("sNssaiListAsString "+sNssaiListAsString)

		List<String> sNssaiList = jsonUtil.StringArrayToList(sNssaiListAsString)

		logger.debug("sNssaiList "+sNssaiList)


		String sNssai = sNssaiList.get(0)
		execution.setVariable("sNssai", sNssai)

		logger.debug("sNssai: "+sNssai)

		String serviceType = execution.getVariable("subscriptionServiceType")
		execution.setVariable("serviceType", serviceType)
		logger.debug("**** Exit DoActivateCoreNSSI ::: preProcessRequest ****")
	}

	public void getNetworkInstanceWithSPInstanceAssociatedWithNssiId(DelegateExecution execution) {

		logger.debug("**** Enter DoActivateCoreNSSI ::: getNetworkInstanceWithSPInstanceAssociatedWithNssiId ****")

		//NSSI Id as service Instance Id to get from Request
		String serviceInstanceId = execution.getVariable("serviceInstanceID")

		String errorMsg = "query Network Service Instance from AAI failed"
		AAIResultWrapper wrapper = queryAAI(execution, AAIObjectType.SERVICE_INSTANCE, serviceInstanceId, errorMsg)
		Optional<ServiceInstance> nsi = wrapper.asBean(ServiceInstance.class)

		String networkServiceInstanceName = ""
		String networkServiceInstanceId =""
		if(nsi.isPresent()) {
			List<Relationship> relationshipList = nsi.get().getRelationshipList()?.getRelationship()
			List spiWithsNssaiAndOrchStatusList = new ArrayList<>()

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
				}

				//If related to is allotted-Resource
				if (relatedTo == "allotted-resource") {
					//get slice Profile Instance Id from allotted resource in list by nssi
					List<String> sliceProfileInstanceIdList = new ArrayList<>()
					List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
					for (RelationshipData relationshipData : relationshipDataList) {
						if (relationshipData.getRelationshipKey() == "service-instance.service-instance-id") {
							sliceProfileInstanceIdList.add(relationshipData.getRelationshipValue())
						}
					}
					for (String sliceProfileServiceInstanceId : sliceProfileInstanceIdList) {
						String errorSliceProfileMsg = "Slice Profile Service Instance was not found in aai"

						//Query Slice Profile Service Instance From AAI by sliceProfileServiceInstanceId
						AAIResultWrapper sliceProfileInstanceWrapper = queryAAI(execution, AAIObjectType.SERVICE_INSTANCE, sliceProfileServiceInstanceId, errorSliceProfileMsg)
						Optional<ServiceInstance> sliceProfileServiceInstance = sliceProfileInstanceWrapper.asBean(ServiceInstance.class)
						if (sliceProfileServiceInstance.isPresent()) {
							String orchestrationStatus= sliceProfileServiceInstance.get().getOrchestrationStatus()
							String sNssai = sliceProfileServiceInstance.get().getSliceProfiles().getSliceProfile().get(0).getSNssai()
							if(sNssai.equals(execution.getVariable("sNssai"))) {
								orchestrationStatus = execution.getVariable("oStatus")
								//Slice Profile Service Instance to be updated in AAI
								execution.setVariable("sliceProfileServiceInstance", sliceProfileServiceInstance)
							}

							Map<String, Object> spiWithsNssaiAndOrchStatus = new LinkedHashMap<>()
							spiWithsNssaiAndOrchStatus.put("snssai", sNssai)
							spiWithsNssaiAndOrchStatus.put("orchestrationStatus", orchestrationStatus)
							spiWithsNssaiAndOrchStatusList.add(spiWithsNssaiAndOrchStatus)
						}
					}
				}
			}
			execution.setVariable("snssaiAndOrchStatusList", spiWithsNssaiAndOrchStatusList)
		}
		logger.debug("NSSI Id: ${serviceInstanceId}, network Service Instance Id: ${networkServiceInstanceId}, serviceName: ${networkServiceInstanceName}")
		//Get ServiceInstance Relationships
		getServiceInstanceRelationships(execution)
		//Get Vnf Relationships
		getVnfRelationships(execution)
		logger.debug("**** Exit DoActivateCoreNSSI ::: getNetworkInstanceWithSPInstanceAssociatedWithNssiId ****")
	}


	private String prepareVnfInstanceParamsJson(DelegateExecution execution) {

		logger.debug("**** Enter DoActivateCoreNSSI ::: prepareVnfInstanceParamsJson ****")
		List instanceParamsvalues = execution.getVariable("snssaiAndOrchStatusList")
		Map<String, Object> nSsai= new LinkedHashMap<>()
		nSsai.put("sNssai", instanceParamsvalues)

		String supportedsNssaiJson = mapper.writeValueAsString(nSsai)
		//SupportedNssai
		Map<String, Object> supportedNssai= new LinkedHashMap<>()
		supportedNssai.put("supportedNssai", supportedsNssaiJson)
		logger.debug("****  supportedsNssaiJson**** "+supportedNssai)
		logger.debug("**** Exit DoActivateCoreNSSI ::: prepareVnfInstanceParamsJson ****")
		return supportedNssai
	}

	private void getServiceInstanceRelationships(DelegateExecution execution) {

		logger.debug("**** Enter DoActivateCoreNSSI ::: getServiceInstanceRelationships ****")
		String serviceInstanceId = execution.getVariable("networkServiceInstanceId")
		logger.debug("**** serviceInstanceId :: getServiceInstanceRelationships  :: "+serviceInstanceId)
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
							String vnfId = relationshipData.getRelationshipValue()
							logger.debug("vnfId   :"+vnfId)
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
			logger.debug("**** Exit DoActivateCoreNSSI ::: getServiceInstanceRelationships ****")
		}
	}

	private void getVnfRelationships(DelegateExecution execution) {

		logger.debug("**** Enter DoActivateCoreNSSI ::: getVnfRelationships ****")
		String msg = "query Generic Vnf from AAI failed"
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
		logger.debug("**** Exit DoActivateCoreNSSI ::: getVnfRelationships ****")
	}


	/**
	 * query AAI
	 * @param execution
	 * @param aaiObjectType
	 * @param instanceId
	 * @return AAIResultWrapper
	 */
	private AAIResultWrapper queryAAI(DelegateExecution execution, AAIObjectType aaiObjectType, String instanceId, String errorMsg) {
		logger.debug("**** Enter DoActivateCoreNSSI ::: queryAAI ****")

		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String serviceType = execution.getVariable("serviceType")

		AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(aaiObjectType, globalSubscriberId, serviceType, instanceId)
		if (!getAAIClient().exists(resourceUri)) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMsg)
		}
		AAIResultWrapper wrapper = getAAIClient().get(resourceUri, NotFoundException.class)

		logger.debug("**** Exit DoActivateCoreNSSI ::: queryAAI ****")
		return wrapper
	}

	public void getServiceVNFAndVFsFromCatalogDB(DelegateExecution execution) {
		logger.debug("**** Exit DoActivateCoreNSSI ::: getServiceVNFAndVFsFromCatalogDB ****")

		String modelInvariantUuid = execution.getVariable("networkServiceModelInvariantUuid")

		try{
			CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()
			String json = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuidString(execution, modelInvariantUuid)
			logger.debug("***** JSON IS: "+json)

			String serviceVnfs = jsonUtil.getJsonValue(json, "serviceResources.serviceVnfs") ?: ""
			String serviceModelInfo = jsonUtil.getJsonValue(json, "serviceResources.modelInfo") ?: ""


			execution.setVariable("serviceVnfs",serviceVnfs)
			execution.setVariable("serviceModelInfo", serviceModelInfo)
			logger.debug("***** serviceVnfs is: "+ serviceVnfs)
		}catch(BpmnError e){
			throw e
		} catch (Exception ex){
			String msg = "Exception in getServiceVNFAndVFsFromCatalogDB " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug("**** Exit DoActivateCoreNSSI ::: getServiceVNFAndVFsFromCatalogDB ****")
	}

	public void prepareSOMacroRequestPayLoad(DelegateExecution execution) {
		logger.debug("**** Enter DoActivateCoreNSSI ::: prepareSOMacroRequestPayLoad ****")
		String json = execution.getVariable("serviceVnfs")
		logger.debug(">>>> json "+json)
		List<Object> vnfList = mapper.readValue(json, List.class);
		logger.debug("vnfList:  "+vnfList)
		Map<String,Object> serviceMap = mapper.readValue(execution.getVariable("serviceModelInfo"), Map.class);
		ModelInfo serviceModelInfo = new ModelInfo()
		serviceModelInfo.setModelType(ModelType.service)
		serviceModelInfo.setModelInvariantId(serviceMap.get("modelInvariantUuid"))
		serviceModelInfo.setModelVersionId(serviceMap.get("modelUuid"))
		serviceModelInfo.setModelName(serviceMap.get("modelName"))
		serviceModelInfo.setModelVersion(serviceMap.get("modelVersion"))

		logger.debug("serviceModelInfo:  "+serviceModelInfo)
		//List of Vnfs
		List<Object> vnfModelInfoList = new ArrayList<>()

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

		//Dummy Product FamilyId
		requestInfo.setProductFamilyId("test1234")
		requestInfo.setSource("VID")
		requestInfo.setInstanceName(execution.getVariable("networkServiceInstanceName"))
		requestInfo.setSuppressRollback(false)
		requestInfo.setRequestorId("NBI")

		//Service Level InstanceParams
		List<Map<String, Object>> serviceParams = new ArrayList<>()
		Map<String, Object> serviceParamsValues = new LinkedHashMap<>()
		serviceParams.add(serviceParamsValues)

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

			//VFModule InstanceParams should be empty or this field should not be there?
			List<Map<String, Object>> vfModuleInstanceParams = new ArrayList<>()
			vfModuleValues.put("instanceParams", vfModuleInstanceParams)
		}

		//Vnf intsanceParams
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> sliceProfile = objectMapper.readValue(execution.getVariable("sliceProfile"), Map.class);

		List vnfInstanceParamsList = new ArrayList<>()
		String supportedsNssaiJson= prepareVnfInstanceParamsJson(execution)
		vnfInstanceParamsList.add(supportedsNssaiJson)

		Platform platform = new Platform()
		platform.setPlatformName(execution.getVariable("platform"))

		LineOfBusiness lineOfbusiness = new LineOfBusiness()
		lineOfbusiness.setLineOfBusinessName(execution.getVariable("lineOfBusiness"))

		//Vnf Values
		Map<String, Object> vnfValues = new LinkedHashMap<>()
		vnfValues.put("lineOfBusiness", lineOfbusiness)
		vnfValues.put("platform", platform)
		vnfValues.put("productFamilyId", "test1234")
		vnfValues.put("cloudConfiguration", cloudConfiguration)
		vnfValues.put("vfModules", vfModules)
		vnfValues.put("modelInfo", vnfModelInfo)
		vnfValues.put("instanceName", execution.getVariable("vnfInstanceName"))
		vnfValues.put("instanceParams",vnfInstanceParamsList)

		vnfModelInfoList.add(vnfValues)
		//Service Level Resources
		Map<String, Object> serviceResources = new LinkedHashMap<>()
		serviceResources.put("vnfs", vnfModelInfoList)

		//Service Values
		Map<String, Object> serviceValues = new LinkedHashMap<>()
		serviceValues.put("modelInfo", serviceModelInfo)
		serviceValues.put("instanceName", execution.getVariable("networkServiceInstanceName"))
		serviceValues.put("resources", serviceResources)
		serviceValues.put("instanceParams", serviceParams)

		//UserParams Values
		Map<String, Object> userParamsValues = new LinkedHashMap<>()

		Map<String, Object> homingSolution = new LinkedHashMap<>()
		homingSolution.put("Homing_Solution", "none")

		userParamsValues.put("service", serviceValues)

		//UserParams
		List<Map<String, Object>> userParams = new ArrayList<>()
		userParams.add(homingSolution)
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
		requestDetails.setProject(project)

		Map<String, Object> requestDetailsMap = new LinkedHashMap<>()
		requestDetailsMap.put("requestDetails", requestDetails)
		String requestPayload = objectMapper.writeValueAsString(requestDetailsMap)

		logger.debug("requestDetails "+requestPayload)
		execution.setVariable("requestPayload", requestPayload)

		logger.debug("**** Exit DoActivateCoreNSSI ::: prepareSOMacroRequestPayLoad ****")
	}

	public void sendPutRequestToSOMacro(DelegateExecution execution) {

		logger.debug("**** Enter DoActivateCoreNSSI ::: sendPutRequestToSOMacro ****")
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
			String msg = "Exception in DoActivateCoreNSSSI " + any.getCause()
			logger.error(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug("**** Exit DoActivateCoreNSSI ::: sendPostRequestToSOMacro ****")
	}

	/**
	 * Handle SO Response for PUT and prepare update operation status
	 * @param execution
	 */
	private void handleSOResponse(Response httpResponse, DelegateExecution execution){
		logger.debug("**** Enter DoActivateCoreNSSI ::: handleSOResponse ****")

		int soResponseCode = httpResponse.getStatus()
		logger.debug("soResponseCode : "+soResponseCode)

		if (soResponseCode >= 200 && soResponseCode < 204 && httpResponse.hasEntity()) {
			String soResponse = httpResponse.readEntity(String.class)
			String operationId = execution.getVariable("operationId")
			def macroOperationId = jsonUtil.getJsonValue(soResponse, "operationId")
			execution.setVariable("macroOperationId", macroOperationId)
			execution.setVariable("isSOTimeOut", "no")
			execution.setVariable("isSOResponseSucceed","yes")
		}
		else
		{
			String serviceName = execution.getVariable("serviceInstanceName")
			execution.setVariable("isSOResponseSucceed","no")
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "SO async response error，SO ResponseCode：${soResponseCode}，serivceName:${serviceName}")
		}

		logger.debug("**** Exit DoActivateCoreNSSI ::: handleSOResponse ****")
	}

	/**
	 * prepare to call sub process CheckProcessStatus
	 * @param execution
	 */
	void prepareCallCheckProcessStatus(DelegateExecution execution){
		logger.debug("**** Enter DoActivateCoreNSSI ::: prepareCallCheckProcessStatus ****")
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
		logger.debug("**** Exit DoActivateCoreNSSI ::: prepareCallCheckProcessStatus ****")
	}

	void prepareUpdateResourceOperationStatus(DelegateExecution execution) {

		logger.debug("**** Enter DoActivateCoreNSSI ::: prepareUpdateResourceOperationStatus ****")

		String nssiOperationId = execution.getVariable("nssiOperationId")
		execution.setVariable("operationId", nssiOperationId)
		//Prepare Update Status for PUT failure and success
		if(execution.getVariable("isTimeOut").equals("YES")) {
			logger.debug("TIMEOUT - SO PUT Failure")
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "SO PUT Failure")
		}
		logger.debug("**** Exit DoActivateCoreNSSI ::: prepareUpdateResourceOperationStatus ****")
	}

	void updateSliceProfileOrchestrationStatus(DelegateExecution execution) {

		logger.debug("**** Enter DoActivateCoreNSSI ::: updateSliceProfileOrchestrationStatus ****")

		String globalCustId = execution.getVariable("globalSubscriberId")
		String serviceType = execution.getVariable("serviceType")
		String oStatus = execution.getVariable("oStatus")

		ServiceInstance si = execution.getVariable("sliceProfileServiceInstance")
		String sliceProfileInstanceId = si.getServiceInstanceId()
		si.setOrchestrationStatus(oStatus)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
				globalCustId, serviceType, sliceProfileInstanceId)
		try {

			Response response = getAAIClient().update(uri, si)

			if(response.getStatus()!=200 || response.getStatus()!=201 || response.getStatus()!=202) {
				exceptionUtil.buildAndThrowWorkflowException(execution, response.getStatus(), "AAI failed to update sliceProlie service Instance orchestration status")
			} else {
				setResourceOperationStatus(execution)
			}
		} catch (Exception e) {
			logger.info("Update OrchestrationStatus in AAI failed")
			String msg = "Update OrchestrationStatus in AAI failed, " + e.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}

		logger.debug("**** Exit DoActivateCoreNSSI ::: updateSliceProfileOrchestrationStatus ****")
	}

	/**
	 * prepare ResourceOperation status
	 * @param execution
	 * @param operationType
	 */
	private void setResourceOperationStatus(DelegateExecution execution) {

		logger.debug("**** Enter DoActivateCoreNSSI ::: setResourceOperationStatus ****")

		String serviceId = execution.getVariable("nssiId")
		String jobId = execution.getVariable("jobId")
		String nsiId = execution.getVariable("nsiId")
		String operationType = execution.getVariable("operationType")
		ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus()
		resourceOperationStatus.setServiceId(serviceId)
		resourceOperationStatus.setOperationId(jobId)
		resourceOperationStatus.setResourceTemplateUUID(nsiId)
		resourceOperationStatus.setOperType(operationType)
		resourceOperationStatus.setStatus("finished")
		resourceOperationStatus.setProgress("100")
		resourceOperationStatus.setStatusDescription("Core Activation Successful")
		requestDBUtil.prepareUpdateResourceOperationStatus(execution, resourceOperationStatus)

		logger.debug("**** Exit DoActivateCoreNSSI ::: setResourceOperationStatus ****")
	}

	void prepareFailedOperationStatusUpdate(DelegateExecution execution){
		logger.debug(Prefix + " **** Enter DoActivateCoreNSSI ::: prepareFailedOperationStatusUpdate ****")

		String serviceId = execution.getVariable("nssiId")
		String jobId = execution.getVariable("jobId")
		String nsiId = execution.getVariable("nsiId")
		String operationType = execution.getVariable("operationType")

		ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus()
		resourceOperationStatus.setServiceId(serviceId)
		resourceOperationStatus.setOperationId(jobId)
		resourceOperationStatus.setResourceTemplateUUID(nsiId)
		resourceOperationStatus.setOperType(operationType)
		resourceOperationStatus.setProgress(0)
		resourceOperationStatus.setStatus("failed")
		resourceOperationStatus.setStatusDescription("Core NSSI Activation Failed")
		requestDBUtil.prepareUpdateResourceOperationStatus(execution, resourceOperationStatus)

		logger.debug(Prefix + " **** Exit DoActivateCoreNSSI ::: prepareFailedOperationStatusUpdate ****")
	}

}
