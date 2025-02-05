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

import static org.apache.commons.lang3.StringUtils.isBlank
import javax.ws.rs.NotFoundException
import javax.ws.rs.core.Response
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.RelatedToProperty
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.RelationshipData
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectName
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.onap.so.serviceinstancebeans.CloudConfiguration
import org.onap.so.serviceinstancebeans.LineOfBusiness
import org.onap.so.serviceinstancebeans.ModelInfo
import org.onap.so.serviceinstancebeans.ModelType
import org.onap.so.serviceinstancebeans.OwningEntity
import org.onap.so.serviceinstancebeans.Platform
import org.onap.so.serviceinstancebeans.Project
import org.onap.so.serviceinstancebeans.RequestDetails
import org.onap.so.serviceinstancebeans.RequestInfo
import org.onap.so.serviceinstancebeans.RequestParameters
import org.onap.so.serviceinstancebeans.SubscriberInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper


class DoAllocateCoreSharedSlice extends AbstractServiceTaskProcessor {

    String Prefix="DACSNSSI_"
    private static final Logger logger = LoggerFactory.getLogger(DoAllocateCoreSharedSlice.class);
    private CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private JsonUtils jsonUtil = new JsonUtils()
    private static final ObjectMapper mapper = new ObjectMapper()

    private final Long TIMEOUT = 60 * 60 * 1000

    @Override
    public void preProcessRequest(DelegateExecution execution) {

        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: preProcessRequest ****")
        //Get NSSI Solutions
        String nssisolutions = execution.getVariable("solutions")
        String nssiId = jsonUtil.getJsonValue(nssisolutions, "NSSIId")

        if (isBlank(nssiId)) {
            String msg = "solution nssiId is null"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        } else {
            execution.setVariable("nssiId", nssiId)
        }

        String sNssaiListAsString = jsonUtil.getJsonValue(execution.getVariable("sliceProfile"), "snssaiList")
        List<String> sNssaiList = jsonUtil.StringArrayToList(sNssaiListAsString)
        String sNssai = sNssaiList.get(0)
        execution.setVariable("sNssai", sNssai)

        //Setting this value in Map
        Map<String, Object> spiWithsNssaiAndOrchStatus = new LinkedHashMap<>()
        spiWithsNssaiAndOrchStatus.put("snssai", sNssai)
        spiWithsNssaiAndOrchStatus.put("status", "created")

        List <Map<String, Object>> spiWithsNssaiAndOrchStatusList = new ArrayList<>();
        spiWithsNssaiAndOrchStatusList.add(spiWithsNssaiAndOrchStatus)

        execution.setVariable("snssaiAndOrchStatusList", spiWithsNssaiAndOrchStatusList)
        logger.debug("service Profile's NSSAI And Orchestration Status:  "+spiWithsNssaiAndOrchStatus)

        String serviceType = execution.getVariable("subscriptionServiceType")
        execution.setVariable("serviceType", serviceType)
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: preProcessRequest ****")
    }

    public void getNetworkInstanceAssociatedWithNssiId(DelegateExecution execution) {

        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: getNetworkInstanceAssociatedWithNssiId ****")

        //NSSI Id as service Instance Id to get from Request
        String serviceInstanceId = execution.getVariable("nssiId")

        String errorMsg = "query Network Service Instance from AAI failed"
        AAIResultWrapper wrapper = queryAAI(execution, Types.SERVICE_INSTANCE, serviceInstanceId, errorMsg)
        Optional<ServiceInstance> nsi = wrapper.asBean(ServiceInstance.class)

        String networkServiceInstanceName = ""
        String networkServiceInstanceId =""
        if(nsi.isPresent()) {
            List<Relationship> relationshipList = nsi.get().getRelationshipList()?.getRelationship()

            List spiWithsNssaiAndOrchStatusList = execution.getVariable("snssaiAndOrchStatusList")

            if(spiWithsNssaiAndOrchStatusList == null) {
                spiWithsNssaiAndOrchStatusList = new ArrayList<>();
            }

            for (Relationship relationship : relationshipList) {
                String relatedTo = relationship.getRelatedTo()
                if ("service-instance".equals(relatedTo)) {
                    List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
                    List<RelatedToProperty> relatedToPropertyList = relationship.getRelatedToProperty()
                    for (RelationshipData relationshipData : relationshipDataList) {
                        if ("service-instance.service-instance-id".equals(relationshipData.getRelationshipKey())) {
                            logger.debug("**** service-instance.service-instance-id 1 :: getServiceInstanceRelationships  :: "+ relationshipData.getRelationshipValue())

                            //Query Every related Service Instance From AAI by service Instance ID
                            AAIResultWrapper instanceWrapper = queryAAI(execution, Types.SERVICE_INSTANCE, relationshipData.getRelationshipValue(), "No Instance Present")
                            Optional<ServiceInstance> relatedServiceInstance = instanceWrapper.asBean(ServiceInstance.class)
                            if (relatedServiceInstance.isPresent()) {
                                ServiceInstance relatedServiceInstanceObj = relatedServiceInstance.get()

                                String role = relatedServiceInstanceObj.getServiceRole();

                                if(role == null || role.isEmpty()) {
                                    networkServiceInstanceId = relatedServiceInstanceObj.getServiceInstanceId()
                                    networkServiceInstanceName = relatedServiceInstanceObj.getServiceInstanceName()

                                    logger.debug("networkServiceInstanceId: {} networkServiceInstanceName: {} ",networkServiceInstanceId, networkServiceInstanceName)

                                    execution.setVariable("networkServiceInstanceId", networkServiceInstanceId)
                                    execution.setVariable("networkServiceInstanceName", networkServiceInstanceName)

                                } else if("slice-profile-instance".equals(role)) {

                                    String orchestrationStatus= relatedServiceInstanceObj.getOrchestrationStatus()
                                    String sNssai = relatedServiceInstanceObj.getEnvironmentContext()
                                    if(sNssai.equals(execution.getVariable("sNssai"))) {
                                        orchestrationStatus = execution.getVariable("oStatus")
                                        //Slice Profile Service Instance to be updated in AAI
                                        execution.setVariable("sliceProfileServiceInstance", relatedServiceInstanceObj)
                                    }

                                    Map<String, Object> spiWithsNssaiAndOrchStatus = new LinkedHashMap<>()
                                    spiWithsNssaiAndOrchStatus.put("snssai", sNssai)
                                    spiWithsNssaiAndOrchStatus.put("status", orchestrationStatus)
                                    spiWithsNssaiAndOrchStatusList.add(spiWithsNssaiAndOrchStatus)
                                    logger.debug("service Profile's NSSAI And Orchestration Status:  "+spiWithsNssaiAndOrchStatus)
                                }

                           }
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

        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: getNetworkInstanceAssociatedWithNssiId ****")
	}

    private void getServiceInstanceRelationships(DelegateExecution execution) {
        logger.debug(Prefix +" **** Enter DoAllocateCoreSharedSlice ::: getServiceInstanceRelationships ****")
        String serviceInstanceId = execution.getVariable("networkServiceInstanceId")
        String errorMsg = "query Network Service Instance from AAI failed"
        AAIResultWrapper wrapper = queryAAI(execution, Types.SERVICE_INSTANCE, serviceInstanceId, errorMsg)
        Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)

        String networkServiceModelInvariantUuid = si.get().getModelInvariantId()
        execution.setVariable("networkServiceModelInvariantUuid", networkServiceModelInvariantUuid)
        if(si.isPresent()) {
            List<Relationship> relationshipList = si.get().getRelationshipList()?.getRelationship()
            for (Relationship relationship : relationshipList) {
                String relatedTo = relationship.getRelatedTo()
                if (("owning-entity").equals(relatedTo)) {
                    List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
                    for (RelationshipData relationshipData : relationshipDataList) {
                        if (("owning-entity.owning-entity-id").equals(relationshipData.getRelationshipKey())) {
                            execution.setVariable("owningEntityId", relationshipData.getRelationshipValue())
                        }
                    }
                } else if (("generic-vnf").equals(relatedTo)) {
                    List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
                    List<RelatedToProperty> relatedToPropertyList = relationship.getRelatedToProperty()

                    //Get VnfId
                    for (RelationshipData relationshipData : relationshipDataList) {
                        if (("generic-vnf.vnf-id").equals(relationshipData.getRelationshipKey())) {
                            execution.setVariable("vnfId", relationshipData.getRelationshipValue())
                        }
                    }
                    //Get Vnf Name Check If necessary
                    for (RelatedToProperty relatedToProperty : relatedToPropertyList) {
                        if (("generic-vnf.vnf-name").equals(relatedToProperty.getPropertyKey())) {
                            execution.setVariable("vnfName", relatedToProperty.getPropertyValue())
                        }
                    }
                } else if (("project").equals(relatedTo)) {
                    List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
                    for (RelationshipData relationshipData : relationshipDataList) {
                        if (("project.project-name").equals(relationshipData.getRelationshipKey())) {
                            execution.setVariable("projectName", relationshipData.getRelationshipValue())
                        }
                    }
                }
            }
            logger.debug(Prefix +" **** Exit DoAllocateCoreSharedSlice ::: getServiceInstanceRelationships ****")
        }
    }

    private void getVnfRelationships(DelegateExecution execution) {

        logger.debug(Prefix +" **** Enter DoAllocateCoreSharedSlice ::: getVnfRelationships ****")
        String msg = "query Generic Vnf from AAI failed"
        try {
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(execution.getVariable('vnfId')))
            if (!getAAIClient().exists(uri)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
            }
            AAIResultWrapper wrapper = getAAIClient().get(uri, NotFoundException.class)
            Optional<GenericVnf> vnf = wrapper.asBean(GenericVnf.class)
            if(vnf.isPresent()) {
                List<Relationship> relationshipList = vnf.get().getRelationshipList()?.getRelationship()
                for (Relationship relationship : relationshipList) {
                    String relatedTo = relationship.getRelatedTo()
                    if (("tenant").equals(relatedTo)) {
                        List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
                        for (RelationshipData relationshipData : relationshipDataList) {
                            if (("tenant.tenant-id").equals(relationshipData.getRelationshipKey())) {
                                execution.setVariable("tenantId", relationshipData.getRelationshipValue())
                            }
                        }
                    } else if (("cloud-region").equals(relatedTo)) {
                        List<RelationshipData> relationshipDataList = relationship.getRelationshipData()

                        for (RelationshipData relationshipData : relationshipDataList) {
                            if (("cloud-region.cloud-owner").equals(relationshipData.getRelationshipKey())) {
                                execution.setVariable("cloudOwner", relationshipData.getRelationshipValue())
                            } else if (("cloud-region.cloud-region-id").equals(relationshipData.getRelationshipKey())) {
                                execution.setVariable("lcpCloudRegionId", relationshipData.getRelationshipValue())
                            }
                        }
                    } else if (("platform").equals(relatedTo)) {
                        List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
                        for (RelationshipData relationshipData : relationshipDataList) {
                            if (("platform.platform-name").equals(relationshipData.getRelationshipKey())) {
                                execution.setVariable("platformName", relationshipData.getRelationshipValue())
                            }
                        }
                    } else if (("line-of-business").equals(relatedTo)) {
                        List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
                        for (RelationshipData relationshipData : relationshipDataList) {
                            if (("line-of-business.line-of-business-name").equals(relationshipData.getRelationshipKey())) {
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
        logger.debug(Prefix +" **** Exit DoAllocateCoreSharedSlice ::: getVnfRelationships ****")
    }

	/**
	 * query AAI
	 * @param execution
	 * @param aaiObjectName
	 * @param instanceId
	 * @return AAIResultWrapper
	 */
    private AAIResultWrapper queryAAI(DelegateExecution execution, AAIObjectName aaiObjectName, String instanceId, String errorMsg) {
        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: queryAAI ****")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(instanceId))
        if (!getAAIClient().exists(resourceUri)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMsg)
        }
        AAIResultWrapper wrapper = getAAIClient().get(resourceUri, NotFoundException.class)
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: queryAAI ****")
        return wrapper
    }

    public void getServiceVNFAndVFsFromCatalogDB(DelegateExecution execution) {
        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: getServiceVNFAndVFsFromCatalogDB ****")
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
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: getServiceVNFAndVFsFromCatalogDB ****")
    }

    public void prepareSOMacroRequestPayload(DelegateExecution execution) {
        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: prepareSOMacroRequestPayLoad ****")
        String json = execution.getVariable("serviceVnfs")
        List<Object> vnfList = mapper.readValue(json, List.class);
        Map<String,Object> serviceMap = mapper.readValue(execution.getVariable("serviceModelInfo"), Map.class);
        ModelInfo serviceModelInfo = new ModelInfo()
        serviceModelInfo.setModelType(ModelType.service)
        serviceModelInfo.setModelInvariantId(serviceMap.get("modelInvariantUuid"))
        serviceModelInfo.setModelVersionId(serviceMap.get("modelUuid"))
        serviceModelInfo.setModelName(serviceMap.get("modelName"))
        serviceModelInfo.setModelVersion(serviceMap.get("modelVersion"))
        //List of Vnfs
        List<Object> vnfModelInfoList = new ArrayList<>()

        Map vnfMap = vnfList.get(0)
        //List of VFModules
        List<Map<String, Object>> vfModuleList = vnfMap.get("vfModules")

        //List of VfModules
        List<ModelInfo> vfModelInfoList = new ArrayList<>()

        //Traverse VFModules List and add in vfModelInfoList
        for (vfModule in vfModuleList) {
            ModelInfo vfModelInfo = vfModule.get("modelInfo")
            vfModelInfo.setModelCustomizationId(vfModelInfo.getModelCustomizationUuid())
            vfModelInfo.setModelVersionId(vfModelInfo.getModelId())
            vfModelInfoList.add(vfModelInfo)
        }

        String networkServiceInstanceName = execution.getVariable("networkServiceInstanceName")
        //RequestInfo
        RequestInfo requestInfo = new RequestInfo()

        //Dummy Product FamilyId
        requestInfo.setProductFamilyId("test1234")
        requestInfo.setSource("VID")
        requestInfo.setInstanceName(networkServiceInstanceName)
        requestInfo.setSuppressRollback(false)
        requestInfo.setRequestorId("NBI")

        //Service Level InstanceParams
        List<Map<String, Object>> serviceParams = new ArrayList<>()
        Map<String, Object> serviceParamsValues = new LinkedHashMap<>()
        serviceParams.add(serviceParamsValues)

        //Cloud Configuration
        String lcpCloudRegionId = execution.getVariable("lcpCloudRegionId")
        String tenantId = execution.getVariable("tenantId")
        String cloudOwner = execution.getVariable("cloudOwner")
        CloudConfiguration cloudConfiguration = new CloudConfiguration()
        cloudConfiguration.setLcpCloudRegionId(lcpCloudRegionId)
        cloudConfiguration.setTenantId(tenantId)
        cloudConfiguration.setCloudOwner(cloudOwner)

        //VFModules List
        List<Map<String, Object>> vfModules = new ArrayList<>()
        for (ModelInfo vfModuleModelInfo : vfModelInfoList) {
            //Individual VFModule List
            Map<String, Object> vfModuleValues = new LinkedHashMap<>()
            vfModuleValues.put("modelInfo", vfModuleModelInfo)
            vfModuleValues.put("instanceName", vfModuleModelInfo.getModelName())

            //VFModule InstanceParams should be empty or this field should not be there?
            List<Map<String, Object>> vfModuleInstanceParams = new ArrayList<>()
            vfModuleValues.put("instanceParams", vfModuleInstanceParams)
            vfModules.add(vfModuleValues)
        }

        //Vnf intsanceParams
        List<Map<String, Object>> vnfInstanceParamsList = new ArrayList<>()
        String supportedsNssaiJson= prepareVnfInstanceParamsJson(execution)

        Map<String, Object> supportedNssai= new LinkedHashMap<>()
        supportedNssai.put("supportedsNssai", supportedsNssaiJson)
        vnfInstanceParamsList.add(supportedNssai)

        Platform platform = new Platform()
        String platformName = execution.getVariable("platformName")
        platform.setPlatformName(platformName)

        LineOfBusiness lineOfbusiness = new LineOfBusiness()
        String lineOfBusinessName = execution.getVariable("lineOfBusinessName")
        lineOfbusiness.setLineOfBusinessName(lineOfBusinessName)

        ModelInfo vnfModelInfo = vnfMap.get("modelInfo")
        vnfModelInfo.setModelCustomizationId(vnfModelInfo.getModelCustomizationUuid())
        vnfModelInfo.setModelVersionId(vnfModelInfo.getModelId())

        //Vnf Values
        Map<String, Object> vnfValues = new LinkedHashMap<>()
        vnfValues.put("lineOfBusiness", lineOfbusiness)
        vnfValues.put("platform", platform)
        vnfValues.put("productFamilyId", "test1234")
        vnfValues.put("cloudConfiguration", cloudConfiguration)
        vnfValues.put("vfModules", vfModules)
        vnfValues.put("modelInfo", vnfModelInfo)
        vnfValues.put("instanceName", vnfModelInfo.getModelInstanceName())
        vnfValues.put("instanceParams",vnfInstanceParamsList)

        vnfModelInfoList.add(vnfValues)
        //Service Level Resources
        Map<String, Object> serviceResources = new LinkedHashMap<>()
        serviceResources.put("vnfs", vnfModelInfoList)

        //Service Values
        String serviceInstanceName = execution.getVariable("networkServiceInstanceName")
        Map<String, Object> serviceValues = new LinkedHashMap<>()
        serviceValues.put("modelInfo", serviceModelInfo)
        serviceValues.put("instanceName", serviceInstanceName)
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
        String serviceType = execution.getVariable("serviceType")
        RequestParameters requestParameters = new RequestParameters()
        requestParameters.setaLaCarte(false)
        requestParameters.setSubscriptionServiceType(serviceType)
        requestParameters.setUserParams(userParams)

        //SubscriberInfo
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        SubscriberInfo subscriberInfo = new SubscriberInfo()
        subscriberInfo.setGlobalSubscriberId(globalSubscriberId)

        //Owning Entity
        String owningEntityId = execution.getVariable("owningEntityId")
        OwningEntity owningEntity = new OwningEntity()
        owningEntity.setOwningEntityId(owningEntityId)

        //Project
        String projectName = execution.getVariable("projectName")
        Project project = new Project()
        project.setProjectName(projectName)

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
        String requestPayload = mapper.writeValueAsString(requestDetailsMap)
        logger.debug("requestDetails "+requestPayload)
        execution.setVariable("requestPayload", requestPayload)

        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: prepareSOMacroRequestPayLoad ****")
    }

    private String prepareVnfInstanceParamsJson(DelegateExecution execution) {
        logger.debug(Prefix +" **** Enter DoAllocateCoreSharedSlice ::: prepareVnfInstanceParamsJson ****")
        List instanceParamsvalues = execution.getVariable("snssaiAndOrchStatusList")
        Map<String, Object> nSsai= new LinkedHashMap<>()
        nSsai.put("sNssai", instanceParamsvalues)
        String supportedsNssaiJson = mapper.writeValueAsString(nSsai)
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: prepareVnfInstanceParamsJson ****")
        return supportedsNssaiJson
    }

    public void sendPutRequestToSOMacro(DelegateExecution execution) {
        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: sendPutRequestToSOMacro ****")
        try {
            String msoEndpoint = UrnPropertiesReader.getVariable("mso.infra.endpoint.url", execution)
            String networkServiceInstanceId = execution.getVariable("networkServiceInstanceId")
            String vnfId = execution.getVariable("vnfId")
            String url = msoEndpoint+"/serviceInstantiation/v7/serviceInstances/"+networkServiceInstanceId+"/vnfs/"+vnfId
            String requestBody = execution.getVariable("requestPayload")
            String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)
            String basicAuth =  UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)
            String encodeString = utils.getBasicAuth(basicAuth, msoKey)
            logger.debug("msoEndpoint: "+msoEndpoint +"  "+ "url: "+url  +" requestBody: "+requestBody +"  "+ "encodeString: "+encodeString)
            HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.SO)
            httpClient.addAdditionalHeader("Authorization", encodeString)
            httpClient.addAdditionalHeader("Accept", "application/json")
            Response httpResponse = httpClient.put(requestBody)
            handleSOResponse(httpResponse, execution)
        } catch (BpmnError e) {
            throw e
        } catch (any) {
            String msg = Prefix+" Exception in DoAllocate Shared " + any.getCause()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: sendPostRequestToSOMacro ****")
    }

	/**
	 * Handle SO Response for PUT and prepare update operation status
	 * @param execution
	 */
    private void handleSOResponse(Response httpResponse, DelegateExecution execution){
        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: handleSOResponse ****")

        int soResponseCode = httpResponse.getStatus()
        logger.debug("soResponseCode : "+soResponseCode)

        if (soResponseCode >= 200 && soResponseCode < 204 && httpResponse.hasEntity()) {
            String soResponse = httpResponse.readEntity(String.class)
            logger.debug("soResponse: "+soResponse)
            String macroOperationId = jsonUtil.getJsonValue(soResponse, "requestReferences.requestId")
            String requestSelfLink = jsonUtil.getJsonValue(soResponse, "requestReferences.requestSelfLink")
            execution.setVariable("macroOperationId", macroOperationId)
            execution.setVariable("requestSelfLink", requestSelfLink)
            execution.setVariable("isSOTimeOut", "no")
            execution.setVariable("isSOResponseSucceed","yes")
        }
        else {
            execution.setVariable("isSOResponseSucceed","no")
            prepareFailedOperationStatusUpdate(execution)
        }
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: handleSOResponse ****")
    }

    public void getSOPUTProgress(DelegateExecution execution) {
        logger.debug(Prefix+ " **** Enter DoAllocateCoreSharedSlice ::: getSOPUTProgress ****")
        String url= execution.getVariable("requestSelfLink")
        HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.SO)
        String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)
        String basicAuth =  UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)
        String encodeString = utils.getBasicAuth(basicAuth, msoKey)
        httpClient.addAdditionalHeader("Authorization", encodeString)
        httpClient.addAdditionalHeader("Accept", "application/json")
        Response httpResponse = httpClient.get()
        logger.debug("httpResponse "+httpResponse)
        int soResponseCode = httpResponse.getStatus()
        logger.debug("soResponseCode : "+soResponseCode)
        if (soResponseCode >= 200 && soResponseCode < 204 && httpResponse.hasEntity()) {
            String soResponse = httpResponse.readEntity(String.class)
            logger.debug("soResponse: "+soResponse)
            String requestState= jsonUtil.getJsonValue(soResponse, "request.requestStatus.requestState")
            logger.debug("requestState: "+requestState)
            execution.setVariable("requestState", requestState)
        } else {
            execution.setVariable("isSOResponseSucceed","no")
            prepareFailedOperationStatusUpdate(execution)
        }
        logger.debug(Prefix+ " **** Exit DoAllocateCoreSharedSlice ::: getSOPUTProgress ****")
    }

    public void timeDelay() {
        try {
            logger.debug(Prefix+ " **** DoAllocateCoreSharedSlice ::: timeDelay going to sleep for 5 sec")
            Thread.sleep(5000)
            logger.debug("**** DoActivateCoreNSSI ::: timeDelay wakeup after 5 sec")
        } catch(InterruptedException e) {
            logger.error(Prefix+ " **** DoAllocateCoreSharedSlice ::: timeDelay exception" + e)
        }
	}

    void prepareUpdateResourceOperationStatus(DelegateExecution execution) {

        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: prepareUpdateResourceOperationStatus ****")
        //Prepare Update Status for PUT failure and success
        if(execution.getVariable("isTimeOut").equals("YES")) {
            logger.debug("TIMEOUT - SO PUT Failure")
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "SO PUT Failure")
        } else {
            execution.setVariable("progress", "100")
            execution.setVariable("status", "finished")
            execution.setVariable("operationContent", "AllocteCoreNSSI Shared successful.")
            logger.debug("prepareFailureStatus,result:${execution.getVariable("result")}, reason: ${execution.getVariable("reason")}")
        }
        setResourceOperationStatus(execution)
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: prepareUpdateResourceOperationStatus ****")
    }

	/**
	 * prepare ResourceOperation status
	 * @param execution
	 * @param operationType
	 */
    private void setResourceOperationStatus(DelegateExecution execution) {
        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: setResourceOperationStatus ****")
        String serviceId = execution.getVariable("nsiId")
        String jobId = execution.getVariable("jobId")
        String nssiId = execution.getVariable("nssiId")
        String operationType = "ALLOCATE"
        String modelUuid= execution.getVariable("modelUuid")
        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus()
        resourceOperationStatus.setServiceId(serviceId)
        resourceOperationStatus.setJobId(jobId)
        resourceOperationStatus.setOperationId(jobId)
        resourceOperationStatus.setResourceTemplateUUID(modelUuid)
        resourceOperationStatus.setResourceInstanceID(nssiId)
        resourceOperationStatus.setOperType(operationType)
        resourceOperationStatus.setStatus(execution.getVariable("status"))
        resourceOperationStatus.setProgress(execution.getVariable("progress"))
        resourceOperationStatus.setStatusDescription(execution.getVariable("statusDescription"))
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, resourceOperationStatus)
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: setResourceOperationStatus ****")
    }

    void prepareFailedOperationStatusUpdate(DelegateExecution execution){
        logger.debug(Prefix + " **** Enter DoAllocateCoreSharedSlice ::: prepareFailedOperationStatusUpdate ****")
         String serviceId = execution.getVariable("nsiId")
        String jobId = execution.getVariable("jobId")
        String nssiId = execution.getVariable("nssiId")
        String operationType = "ALLOCATE"
        //modelUuid
        String modelUuid= execution.getVariable("modelUuid")
        logger.debug("serviceId: {}, jobId: {}, nssiId: {}, operationType: {}.", serviceId, jobId, nssiId, operationType)
        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus()
        resourceOperationStatus.setServiceId(serviceId)
        resourceOperationStatus.setJobId(jobId)
        resourceOperationStatus.setOperationId(jobId)
        resourceOperationStatus.setResourceInstanceID(nssiId)
        resourceOperationStatus.setResourceTemplateUUID(modelUuid)
        resourceOperationStatus.setOperType(operationType)
        resourceOperationStatus.setProgress("0")
        resourceOperationStatus.setStatus("failed")
        resourceOperationStatus.setStatusDescription("Core NSSI Shared Allocate Failed")
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, resourceOperationStatus)
        logger.debug(Prefix + " **** Exit DoAllocateCoreSharedSlice ::: prepareFailedOperationStatusUpdate ****")
    }
}
