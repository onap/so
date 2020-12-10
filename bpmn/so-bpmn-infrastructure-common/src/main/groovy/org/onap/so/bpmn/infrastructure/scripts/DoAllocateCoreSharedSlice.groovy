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
import org.onap.logging.filter.base.ONAPComponents
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
    ObjectMapper mapper = new ObjectMapper();

    private final Long TIMEOUT = 60 * 60 * 1000

    @Override
    public void preProcessRequest(DelegateExecution execution) {

        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: preProcessRequest ****")
        //Get NSSI Solutions
        String nssisolutions = execution.getVariable("solutions")

        //Get First Object
        List<String> nssiSolutionList = jsonUtil.StringArrayToList(nssisolutions)

        logger.debug("nssiSolutionList : "+nssiSolutionList)

        String nssiId = jsonUtil.getJsonValue(nssiSolutionList.get(0), "NSSIId")
        logger.debug("NSSIId  : "+nssiId)

        if (isBlank(nssiId)) {
            String msg = "solution nssiId is null"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        } else {
            execution.setVariable("nssiId", nssiId)
        }

        String sNssaiListAsString = jsonUtil.getJsonValue(execution.getVariable("sliceProfile"), "snssaiList")
        logger.debug("sNssaiListAsString "+sNssaiListAsString)
        List<String> sNssaiList = jsonUtil.StringArrayToList(sNssaiListAsString)
        logger.debug("sNssaiList "+sNssaiList)
        String sNssai = sNssaiList.get(0)
        execution.setVariable("sNssai", sNssai)
        logger.debug("sNssai: "+sNssai)

        String serviceType = execution.getVariable("subscriptionServiceType")
        execution.setVariable("serviceType", serviceType)
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: preProcessRequest ****")
    }

    /**
     * Gets NetworkInstance Associated With NssiId
     * Gets service Level relationships
     * Gets vnf level relationships
     * @param execution
     */
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
            List spiWithsNssaiAndOrchStatusList = new ArrayList<>()

            for (Relationship relationship : relationshipList) {
                String relatedTo = relationship.getRelatedTo()
                if (relatedTo == "service-instance") {
                    List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
                    List<RelatedToProperty> relatedToPropertyList = relationship.getRelatedToProperty()
                    for (RelationshipData relationshipData : relationshipDataList) {
                        if (relationshipData.getRelationshipKey() == "service-instance.service-instance-id") {
                            logger.debug("**** service-instance.service-instance-id 1 :: getServiceInstanceRelationships  :: "+ relationshipData.getRelationshipValue())
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
                        AAIResultWrapper sliceProfileInstanceWrapper = queryAAI(execution, Types.SERVICE_INSTANCE, sliceProfileServiceInstanceId, errorSliceProfileMsg)
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
                            spiWithsNssaiAndOrchStatus.put("status", orchestrationStatus)
                            spiWithsNssaiAndOrchStatusList.add(spiWithsNssaiAndOrchStatus)
                            logger.debug("service Profile's NSSAI And Orchestration Status:  "+spiWithsNssaiAndOrchStatus)
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

    /**
     * Gets Service Relationships
     * @param execution
     */
    private void getServiceInstanceRelationships(DelegateExecution execution) {

        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: getServiceInstanceRelationships ****")

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

            logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: getServiceInstanceRelationships ****")
        }
    }

    /**
     * Gets vnf Relationships
     * @param execution
     */
    private void getVnfRelationships(DelegateExecution execution) {

        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: getVnfRelationships ****")
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
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: getVnfRelationships ****")
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

    /**
     * Prepares SO Macro Request Payload
     * @param execution
     */
    public void prepareSOMacroRequestPayload(DelegateExecution execution) {
        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: prepareSOMacroRequestPayLoad ****")
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
        vnfModelInfo.setModelCustomizationId(vnfModelInfo.getModelCustomizationUuid())
        vnfModelInfo.setModelVersionId(vnfModelInfo.getModelId())
        logger.debug("vnfModelInfo "+vnfModelInfo)

        //List of VFModules
        List<Map<String, Object>> vfModuleList = vnfMap.get("vfModules")
        logger.debug("vfModuleList "+vfModuleList)

        //List of VfModules
        List<ModelInfo> vfModelInfoList = new ArrayList<>()

        //Traverse VFModules List and add in vfModelInfoList
        for (vfModule in vfModuleList) {
            ModelInfo vfModelInfo = vfModule.get("modelInfo")
            vfModelInfo.setModelCustomizationId(vfModelInfo.getModelCustomizationUuid())
            vfModelInfo.setModelVersionId(vfModelInfo.getModelId())
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
            vfModuleValues.put("instanceName", vfModuleModelInfo.getModelName())

            //VFModule InstanceParams should be empty or this field should not be there?
            List<Map<String, Object>> vfModuleInstanceParams = new ArrayList<>()
            vfModuleValues.put("instanceParams", vfModuleInstanceParams)
            vfModules.add(vfModuleValues)
        }

        //Vnf intsanceParams
        Map<String, Object> sliceProfile = mapper.readValue(execution.getVariable("sliceProfile"), Map.class);

        List vnfInstanceParamsList = new ArrayList<>()
        String supportedsNssaiJson= prepareVnfInstanceParamsJson(execution)

        Map<String, Object> supportedNssai= new LinkedHashMap<>()
        supportedNssai.put("supportedsNssai", supportedsNssaiJson)
        vnfInstanceParamsList.add(supportedNssai)

        Platform platform = new Platform()
        platform.setPlatformName(execution.getVariable("platformName"))

        LineOfBusiness lineOfbusiness = new LineOfBusiness()
        lineOfbusiness.setLineOfBusinessName(execution.getVariable("lineOfBusinessName"))

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
        String requestPayload = mapper.writeValueAsString(requestDetailsMap)
        logger.debug("requestDetails "+requestPayload)
        execution.setVariable("requestPayload", requestPayload)
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: prepareSOMacroRequestPayLoad ****")
    }

    /**
     * prepares vnf Instance Params as JSON for PUT Operation in Macro flow
     * @param execution
     * @return
     */
    private String prepareVnfInstanceParamsJson(DelegateExecution execution) {
        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: prepareVnfInstanceParamsJson ****")
        List instanceParamsvalues = execution.getVariable("snssaiAndOrchStatusList")
        Map<String, Object> nSsai= new LinkedHashMap<>()
        nSsai.put("sNssai", instanceParamsvalues)
        String supportedsNssaiJson = mapper.writeValueAsString(nSsai)
        //SupportedNssai
        logger.debug("****  supportedsNssaiJson**** "+supportedsNssaiJson)
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: prepareVnfInstanceParamsJson ****")
        return supportedsNssaiJson
    }

    /**
     * sends PUT request to SO
     * @param execution
     */
    public void sendPutRequestToSOMacro(DelegateExecution execution) {
        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: sendPutRequestToSOMacro ****")
        try {
            String msoEndpoint = UrnPropertiesReader.getVariable("mso.infra.endpoint.url", execution)
            logger.debug("msoEndpoint: "+msoEndpoint)
            String url = msoEndpoint+"/serviceInstantiation/v7/serviceInstances/"+execution.getVariable("networkServiceInstanceId")+"/vnfs/"+execution.getVariable("vnfId")
            logger.debug("url: "+url)
            String requestBody = execution.getVariable("requestPayload")
            logger.debug("requestBody: "+requestBody)
            String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)
            logger.debug("msoKey: "+msoKey)
            String basicAuth =  UrnPropertiesReader.getVariable("mso.infra.endpoint.auth", execution)
            //String basicAuthValue = utils.encrypt(basicAuth, msoKey)
            //String encodeString = utils.getBasicAuth(basicAuthValue, msoKey)
            String encodeString = "Basic SW5mcmFQb3J0YWxDbGllbnQ6cGFzc3dvcmQxJA=="
            logger.debug("encodeString: "+encodeString)

            HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.SO)
            httpClient.addAdditionalHeader("Authorization", encodeString)
            httpClient.addAdditionalHeader("Accept", "application/json")
            Response httpResponse = httpClient.put(requestBody)
            handleSOResponse(httpResponse, execution)
        } catch (BpmnError e) {
            throw e
        } catch (any) {
            String msg = Prefix+" Exception in DoAllocateCoreSharedSlice " + any.getCause()
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
            String operationId = execution.getVariable("operationId")
            logger.debug("soResponse JsonUtil: "+jsonUtil.getJsonValue(soResponse, "requestReferences.requestId"))
            def macroOperationId = jsonUtil.getJsonValue(soResponse, "requestReferences.requestId")
            def requestSelfLink = jsonUtil.getJsonValue(soResponse, "requestReferences.requestSelfLink")
            execution.setVariable("macroOperationId", macroOperationId)
            execution.setVariable("requestSelfLink", requestSelfLink)
            execution.setVariable("isSOTimeOut", "no")
            execution.setVariable("isSOResponseSucceed","yes")
        }
        else {
            String serviceName = execution.getVariable("serviceInstanceName")
            execution.setVariable("isSOResponseSucceed","no")
            prepareFailedOperationStatusUpdate(execution)
        }
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: handleSOResponse ****")
    }

    /**
     * Get SO PUT Progress using requestSelfLink generated after triggering SO PUT
     * @param execution
     */
    public void getSOPUTProgress(DelegateExecution execution) {
        logger.debug(Prefix+ " **** Enter DoAllocateCoreSharedSlice ::: getSOPUTProgress ****")
        String url= execution.getVariable("requestSelfLink")
        logger.debug("url  "+url)
        HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.SO)
        httpClient.addAdditionalHeader("Authorization", "Basic SW5mcmFQb3J0YWxDbGllbnQ6cGFzc3dvcmQxJA==")
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
            String serviceName = execution.getVariable("serviceInstanceName")
            execution.setVariable("isSOResponseSucceed","no")
            prepareFailedOperationStatusUpdate(execution)
        }
        logger.debug(Prefix+ " **** Exit DoAllocateCoreSharedSlice ::: getSOPUTProgress ****")
    }

    /**
     * delay 5 sec
     */
    public void timeDelay(DelegateExecution execution) {
        try {
            logger.debug(Prefix+ " **** DoAllocateCoreSharedSlice ::: timeDelay going to sleep for 5 sec")
            Thread.sleep(5000)
            logger.debug("**** DoAllocateCoreNonSharedSlice ::: timeDelay wakeup after 5 sec")
        } catch(InterruptedException e) {
            logger.error(Prefix+ " **** DoAllocateCoreSharedSlice ::: timeDelay exception" + e)
        }
    }

    /**
     * prepares UpdateResourceOperationStatus for DB
     * @param execution
     */
    void prepareUpdateResourceOperationStatus(DelegateExecution execution) {

        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: prepareUpdateResourceOperationStatus ****")
        //Prepare Update Status for PUT failure and success
        if(execution.getVariable("requestState").equals("COMPLETED")) {
            execution.setVariable("progress", "100")
            execution.setVariable("status", "finished")
            execution.setVariable("operationContent", "AllocteCoreNSSI successful.")
            logger.debug("Success ,result:${execution.getVariable("result")}, reason: ${execution.getVariable("reason")}")
        } else {
            logger.debug("SO PUT Failure")
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "SO PUT Failure")
        }
        setResourceOperationStatus(execution)
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: prepareUpdateResourceOperationStatus ****")
    }

    /**
     * prepare ResourceOperation status
     * @param execution
     */
    private void setResourceOperationStatus(DelegateExecution execution) {
        logger.debug(Prefix+" **** Enter DoAllocateCoreSharedSlice ::: setResourceOperationStatus ****")
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
        resourceOperationStatus.setStatusDescription("Core Allocate successful")
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, resourceOperationStatus)
        logger.debug(Prefix+" **** Exit DoAllocateCoreSharedSlice ::: setResourceOperationStatus ****")
    }

    /**
     * prepares FailedOperationStatusUpdate for DB
     * @param execution
     */
    void prepareFailedOperationStatusUpdate(DelegateExecution execution){
        logger.debug(Prefix + " **** Enter DoAllocateCoreSharedSlice ::: prepareFailedOperationStatusUpdate ****")
        String serviceId = execution.getVariable("nsiId")
        logger.debug("serviceId: "+serviceId)
        String jobId = execution.getVariable("jobId")
        logger.debug("jobId: "+jobId)
        String nsiId = execution.getVariable("nsiId")
        logger.debug("nsiId: "+nsiId)
        String nssiId = execution.getVariable("nssiId")
        String operationType = "ALLOCATE"
        logger.debug("operationType: "+operationType)
        //modelUuid
        String modelUuid= execution.getVariable("modelUuid")
        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus()
        resourceOperationStatus.setServiceId(serviceId)
        resourceOperationStatus.setJobId(jobId)
        resourceOperationStatus.setOperationId(jobId)
        resourceOperationStatus.setResourceTemplateUUID(modelUuid)
        resourceOperationStatus.setResourceInstanceID(nssiId)
        resourceOperationStatus.setOperType(operationType)
        resourceOperationStatus.setProgress("0")
        resourceOperationStatus.setStatus("failed")
        resourceOperationStatus.setStatusDescription("Core NSSI Allocate Failed")
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, resourceOperationStatus)
        logger.debug(Prefix + " **** Exit DoAllocateCoreSharedSlice ::: prepareFailedOperationStatusUpdate ****")
    }
}
