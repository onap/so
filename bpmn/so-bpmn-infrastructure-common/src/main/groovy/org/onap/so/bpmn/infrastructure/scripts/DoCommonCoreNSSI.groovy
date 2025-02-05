/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020  Telecom Italia
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

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.aai.domain.yang.v19.*
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.*
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.onap.so.serviceinstancebeans.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response

import static org.apache.commons.lang3.StringUtils.isBlank

class DoCommonCoreNSSI extends AbstractServiceTaskProcessor {

    private final String PREFIX ="DoCommonCoreNSSI"

    private static final String SLICE_PROFILE_TEMPLATE = "{\"sliceProfileId\": \"%s\"}"

    private static final Logger LOGGER = LoggerFactory.getLogger( DoCommonCoreNSSI.class)

    private JsonUtils jsonUtil = new JsonUtils()
    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()

    @Override
    void preProcessRequest(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start preProcessRequest")

        def currentNSSI = execution.getVariable("currentNSSI")
        if (!currentNSSI) {
            currentNSSI = [:]
        }

        // NSSI ID
        String nssiId = execution.getVariable("serviceInstanceID")
        if (isBlank(nssiId)) {
            String msg = "NSSI service instance id is null"
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }
        else {
            currentNSSI['nssiId'] = nssiId
        }

        // NSI ID
        String nsiId = execution.getVariable("nsiId")
        if (isBlank(nsiId)) {
            String msg = "nsiId is null"
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }
        else {
            currentNSSI['nsiId'] = nsiId
        }


        // Slice Profile
        String sliceProfile = execution.getVariable("sliceParams")
        
      /*  if(jsonUtil.jsonValueExists(execution.getVariable("sliceParams"), "sliceProfile")) {
            sliceProfile = jsonUtil.getJsonValue(execution.getVariable("sliceParams"), "sliceProfile")
        }
        else { // In case of Deallocate flow or Modify flow with deletion of Slice Profile Instance
            if(jsonUtil.jsonValueExists(execution.getVariable("sliceParams"), "sliceProfileId")) {
                sliceProfile = String.format(SLICE_PROFILE_TEMPLATE, jsonUtil.getJsonValue(execution.getVariable("sliceParams"), "sliceProfileId"))
            }
            else {
                String msg = "Either Slice Profile or Slice Profile Id should be provided"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
        } */

        LOGGER.debug("sliceProfile=" + sliceProfile)
        currentNSSI['sliceProfile'] = sliceProfile

        // S-NSSAI
        String strList = jsonUtil.getJsonValue(sliceProfile, "snssaiList")

        if(strList != null) {
            def snssaiList = jsonUtil.StringArrayToList(strList)

            String sNssai = snssaiList.get(0)
            currentNSSI['S-NSSAI'] = sNssai
        }

        LOGGER.debug("S-NSSAI=" + currentNSSI['S-NSSAI'])

        // Slice Profile id
        String sliceProfileId = jsonUtil.getJsonValue(sliceProfile, "sliceProfileId")
        currentNSSI['sliceProfileId'] = sliceProfileId

        execution.setVariable("currentNSSI", currentNSSI)

        LOGGER.debug("***** ${getPrefix()} Exit preProcessRequest")
    }


    /**
     * Queries Network Service Instance in AAI
     * @param execution
     */
    void getNetworkServiceInstance(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start getNetworkServiceInstance")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiId']

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))
        Optional<ServiceInstance> nssiOpt = client.get(ServiceInstance.class, nssiUri)

        if (nssiOpt.isPresent()) {
            ServiceInstance nssi = nssiOpt.get()
            currentNSSI['nssi'] = nssi

            ServiceInstance networkServiceInstance = handleNetworkInstance(execution, nssiId, nssiUri, client)
            currentNSSI['networkServiceInstance'] = networkServiceInstance
        }
        else {
            String msg = String.format("NSSI %s not found in AAI", nssiId)
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
        }

        LOGGER.debug("${getPrefix()} Exit getNetworkServiceInstance")
    }


    /**
     * Handles Network Service
     * @param nssiId
     * @param nssiUri
     * @param client
     * @return Network Service Instance
     */
    private ServiceInstance handleNetworkInstance(DelegateExecution execution, String nssiId, AAIResourceUri nssiUri, AAIResourcesClient client ) {
        LOGGER.debug("${getPrefix()} Start handleNetworkInstance")

        ServiceInstance networkServiceInstance = null

        def currentNSSI = execution.getVariable("currentNSSI")

        AAIResultWrapper wrapper = client.get(nssiUri)
        Optional<Relationships> relationships = wrapper.getRelationships()

        if (relationships.isPresent()) {
            for (AAIResourceUri networkServiceInstanceUri : relationships.get().getRelatedUris(Types.SERVICE_INSTANCE)) {
                Optional<ServiceInstance> networkServiceInstanceOpt = client.get(ServiceInstance.class, networkServiceInstanceUri)
                if (networkServiceInstanceOpt.isPresent()) {
                    networkServiceInstance = networkServiceInstanceOpt.get()

                    if (networkServiceInstance.getServiceRole() == null /* WorkAround */ ||  networkServiceInstance.getServiceRole() == "Network Service") { // Network Service role
                        currentNSSI['networkServiceInstanceUri'] = networkServiceInstanceUri
                        break
                    }
                }
                else {
                    String msg = String.format("No Network Service Instance found for NSSI %s in AAI", nssiId)
                    LOGGER.error(msg)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
                }
            }

            if (currentNSSI['networkServiceInstanceUri'] == null) {
                String msg = String.format("Network Service Instance URI is null")
                LOGGER.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
            }
        }
        else {
            String msg = String.format("No relationship presented for NSSI %s in AAI", nssiId)
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
        }

        if(networkServiceInstance == null) {
            String msg = String.format("No Network Service Instance found for NSSI %s in AAI", nssiId)
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
        }

        LOGGER.debug("${getPrefix()} Exit handleNetworkInstance")

        return networkServiceInstance
    }


    /**
     * Queries constitute VNF from Network Service Instance
     * @param execution
     */
    void getConstituteVNFFromNetworkServiceInst(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start getConstituteVNFFromNetworkServiceInst")

        def currentNSSI = execution.getVariable("currentNSSI")

        AAIResourcesClient client = getAAIClient()

        AAIResourceUri networkServiceInstanceUri = (AAIResourceUri)currentNSSI['networkServiceInstanceUri']
        AAIResultWrapper wrapper = client.get(networkServiceInstanceUri);
        Optional<Relationships> relationships = wrapper.getRelationships()

        if (relationships.isPresent()) {
            for (AAIResourceUri constituteVnfUri : relationships.get().getRelatedUris(Types.GENERIC_VNF)) {
                currentNSSI['constituteVnfUri'] = constituteVnfUri
                Optional<GenericVnf> constituteVnfOpt = client.get(GenericVnf.class, constituteVnfUri)
                if(constituteVnfOpt.isPresent()) {
                    GenericVnf constituteVnf = constituteVnfOpt.get()
                    currentNSSI['constituteVnf'] = constituteVnf
                }
                else {
                    String msg = String.format("No constitute VNF found for Network Service Instance %s in AAI", ((ServiceInstance)currentNSSI['networkServiceInstance']).getServiceInstanceId())
                    LOGGER.error(msg)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
                }

                break  // Should be only one constitute VNF
            }
        }
        else {
            String msg = String.format("No relationship presented for Network Service Instance %s in AAI", ((ServiceInstance)currentNSSI['networkServiceInstance']).getServiceInstanceId())
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
        }

        LOGGER.debug("${getPrefix()} Exit getConstituteVNFFromNetworkServiceInst")

    }


    /**
     * Invoke PUT Service Instance API
     * @param execution
     */
    void invokePUTServiceInstance(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start invokePUTServiceInstance")

        def currentNSSI = execution.getVariable("currentNSSI")

        try {
            //url:/onap/so/infra/serviceInstantiation/v7/serviceInstances/{serviceInstanceId}"
            def nsmfЕndPoint = UrnPropertiesReader.getVariable("mso.infra.endpoint.url", execution) // ???

            ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']

            GenericVnf constituteVnf = (GenericVnf)currentNSSI['constituteVnf']

            // http://so.onap:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances/de6a0aa2-19f2-41fe-b313-a5a9f159acd7/vnfs/3abbb373-8d33-4977-aa4b-2bfee496b6d5
            String url = String.format("${nsmfЕndPoint}/serviceInstantiation/v7/serviceInstances/%s/vnfs/%s", networkServiceInstance.getServiceInstanceId(), constituteVnf.getVnfId())

            String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)

            String basicAuth =  UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)

            def authHeader = utils.getBasicAuth(basicAuth, msoKey) // ""
         /*   String basicAuthValue = encryptBasicAuth(basicAuth, msoKey) //utils.encrypt(basicAuth, msoKey)

            String responseAuthHeader = getAuthHeader(execution, basicAuthValue, msoKey) //utils.getBasicAuth(basicAuthValue, msoKey)

            String errorCode = jsonUtil.getJsonValue(responseAuthHeader, "errorCode")
            if(errorCode == null || errorCode.isEmpty()) { // No error
                authHeader = responseAuthHeader
            }
            else {
                exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(responseAuthHeader, "errorMessage"))
            } */

            def requestDetails = ""

            String prepareRequestDetailsResponse = prepareRequestDetails(execution)
            LOGGER.debug("invokePUTServiceInstance: prepareRequestDetailsResponse=" + prepareRequestDetailsResponse)

            String errorCode = jsonUtil.getJsonValue(prepareRequestDetailsResponse, "errorCode")
            LOGGER.debug("invokePUTServiceInstance: errorCode=" + errorCode)
            if(errorCode == null || errorCode.isEmpty()) { // No error
                requestDetails = prepareRequestDetailsResponse
            }
            else {
                exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(prepareRequestDetailsResponse, "errorMessage"))
            }

            String callPUTServiceInstanceResponse = callPUTServiceInstance(url, authHeader, requestDetails)

            String macroOperationId = jsonUtil.getJsonValue(callPUTServiceInstanceResponse, "requestReferences.requestId")
            String requestSelfLink = jsonUtil.getJsonValue(callPUTServiceInstanceResponse, "requestReferences.requestSelfLink")

            execution.setVariable("macroOperationId",  macroOperationId)
            execution.setVariable("requestSelfLink", requestSelfLink)
            currentNSSI['requestSelfLink'] = requestSelfLink

        } catch (any) {
            String msg = "Exception in ${getPrefix()}.invokePUTServiceInstance. " + any.getCause()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        LOGGER.debug("${getPrefix()} Exit invokePUTServiceInstance")
    }


    String callPUTServiceInstance(String url, String authHeader, String requestDetailsStr) {
        LOGGER.debug("${getPrefix()} Start callPUTServiceInstance")

        String errorCode = ""
        String errorMessage = ""
        String response

        LOGGER.debug("callPUTServiceInstance: url = " + url)
        LOGGER.debug("callPUTServiceInstance: authHeader = " + authHeader)

        try {
            HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.SO)
            httpClient.addAdditionalHeader("Authorization", authHeader)
            httpClient.addAdditionalHeader("Accept", "application/json")

            Response httpResponse = httpClient.put(requestDetailsStr)

            int soResponseCode = httpResponse.getStatus()
            if (soResponseCode >= 200 && soResponseCode < 204 && httpResponse.hasEntity()) {
                response = httpResponse.readEntity(String.class)

                LOGGER.debug("callPUTServiceInstance: response = " + response)
            }
            else {
                errorCode = 500
                errorMessage = "Response code is " + soResponseCode

                response =  "{\n" +
                        " \"errorCode\": \"${errorCode}\",\n" +
                        " \"errorMessage\": \"${errorMessage}\"\n" +
                        "}"
            }
        }
        catch (any) {
            String msg = "Exception in ${getPrefix()}.invokePUTServiceInstance. " + any.getCause()
            LOGGER.error(msg)

            response =  "{\n" +
                    " \"errorCode\": \"7000\",\n" +
                    " \"errorMessage\": \"${msg}\"\n" +
                    "}"

        }

        LOGGER.debug("${getPrefix()} Exit callPUTServiceInstance")

        return response

    }


    /**
     * Prepare model info
     * @param execution
     * @param requestDetails
     * @return ModelInfo
     */
    ModelInfo prepareModelInfo(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start prepareModelInfo")

        def currentNSSI = execution.getVariable("currentNSSI")
        ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']

        ModelInfo modelInfo = new ModelInfo()

        modelInfo.setModelType(ModelType.service)
        modelInfo.setModelInvariantId(networkServiceInstance.getModelInvariantId())

        AAIResourcesClient client = getAAIClient()

        AAIResourceUri modelVerUrl = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.serviceDesignAndCreation().model(networkServiceInstance.getModelInvariantId()).modelVer(networkServiceInstance.getModelVersionId()))
        Optional<ModelVer> modelVerOpt = client.get(ModelVer.class, modelVerUrl)

        if (modelVerOpt.isPresent()) {
            modelInfo.setModelVersionId(modelVerOpt.get().getModelVersionId())
            modelInfo.setModelName(modelVerOpt.get().getModelName())
            modelInfo.setModelVersion(modelVerOpt.get().getModelVersion())
        }

        LOGGER.debug("${getPrefix()} Exit prepareModelInfo")

        return modelInfo
    }


    /**
     * Prepares subscriber info
     * @param execution
     * @return SubscriberInfo
     */
    SubscriberInfo prepareSubscriberInfo(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start prepareSubscriberInfo")

        def currentNSSI = execution.getVariable("currentNSSI")

        String globalSubscriberId = execution.getVariable("globalSubscriberId")

        String subscriberName = execution.getVariable("subscriberName")

        SubscriberInfo subscriberInfo = new SubscriberInfo()
        subscriberInfo.setGlobalSubscriberId(globalSubscriberId)
        subscriberInfo.setSubscriberName(subscriberName)

        /*
        AAIResourcesClient client = getAAIClient()

        Customer customer = null

        AAIResourceUri networkServiceInstanceUri = currentNSSI['networkServiceInstanceUri']
        AAIResultWrapper wrapper = client.get(networkServiceInstanceUri)
        Optional<Relationships> serviceSubscriptionRelationshipsOps = wrapper.getRelationships()
        if(serviceSubscriptionRelationshipsOps.isPresent()) {
            List<AAIResourceUri> serviceSubscriptionRelatedAAIUris = serviceSubscriptionRelationshipsOps.get().getRelatedUris(Types.SERVICE_SUBSCRIPTION)
            if(!(serviceSubscriptionRelatedAAIUris == null || serviceSubscriptionRelatedAAIUris.isEmpty())) {
                AAIResourceUri serviceSubscriptionUri = serviceSubscriptionRelatedAAIUris.get(0) // Many-To-One relation
                Optional<ServiceSubscription> serviceSubscriptionOpt = client.get(ServiceSubscription.class, serviceSubscriptionUri)

                if(serviceSubscriptionOpt.isPresent()) {
                    currentNSSI['serviceSubscription'] = serviceSubscriptionOpt.get()
                }

                wrapper = client.get(serviceSubscriptionUri)
                Optional<Relationships> customerRelationshipsOps = wrapper.getRelationships()
                if(customerRelationshipsOps.isPresent()) {
                    List<AAIResourceUri> customerRelatedAAIUris = customerRelationshipsOps.get().getRelatedUris(Types.CUSTOMER)
                    if(!(customerRelatedAAIUris == null || customerRelatedAAIUris.isEmpty())) {
                        Optional<Customer> customerOpt = client.get(Customer.class, customerRelatedAAIUris.get(0)) // Many-To-One relation
                        if(customerOpt.isPresent()) {
                            customer = customerOpt.get()
                            subscriberInfo.setSubscriberName(customer.getSubscriberName())
                        }
                    }
                }
            }

        } */

        LOGGER.debug("${getPrefix()} Exit prepareSubscriberInfo")

        return subscriberInfo
    }


    /**
     * Prepares Request Info
     * @param execution
     * @return RequestInfo
     */
    RequestInfo prepareRequestInfo(DelegateExecution execution, ServiceInstance networkServiceInstance) {
        LOGGER.debug("${getPrefix()} Start prepareRequestInfo")

        def currentNSSI = execution.getVariable("currentNSSI")

        String productFamilyId = execution.getVariable("productFamilyId")

        RequestInfo requestInfo = new RequestInfo()

        requestInfo.setInstanceName(networkServiceInstance.getServiceInstanceName())
        requestInfo.setSource("VID")
        requestInfo.setProductFamilyId(productFamilyId)
        requestInfo.setRequestorId("NBI")

        LOGGER.debug("${getPrefix()} Exit prepareRequestInfo")

        return requestInfo
    }


    /**
     * Prepares Model Info
     * @param networkServiceInstance
     * @param modelInfo
     * @return ModelInfo
     */
    ModelInfo prepareServiceModelInfo(ServiceInstance networkServiceInstance, ModelInfo modelInfo) {
        LOGGER.debug("${getPrefix()} Start prepareServiceModelInfo")

        ModelInfo serviceModelInfo = new ModelInfo()
        serviceModelInfo.setModelType(ModelType.service)
        serviceModelInfo.setModelInvariantId(networkServiceInstance.getModelInvariantId())

        serviceModelInfo.setModelVersionId(modelInfo.getModelVersionId())
        serviceModelInfo.setModelName(modelInfo.getModelName())
        serviceModelInfo.setModelVersion(modelInfo.getModelVersion())

        LOGGER.debug("${getPrefix()} Exit prepareServiceModelInfo")

        return serviceModelInfo
    }


    /**
     * Prepares Cloud configuration
     * @param execution
     * @return CloudConfiguration
     */
    CloudConfiguration prepareCloudConfiguration(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start prepareCloudConfiguration")

        def currentNSSI = execution.getVariable("currentNSSI")

        CloudConfiguration cloudConfiguration = new CloudConfiguration()

        AAIResourcesClient client = getAAIClient()

        AAIResourceUri constituteVnfUri = currentNSSI['constituteVnfUri']

        AAIResultWrapper wrapper = client.get(constituteVnfUri)
        Optional<Relationships> cloudRegionRelationshipsOps = wrapper.getRelationships()

        if(cloudRegionRelationshipsOps.isPresent()) {
            List<AAIResourceUri> cloudRegionRelatedAAIUris = cloudRegionRelationshipsOps.get().getRelatedUris(Types.CLOUD_REGION)
            if (!(cloudRegionRelatedAAIUris == null || cloudRegionRelatedAAIUris.isEmpty())) {
                AAIResourceUri cloudRegionRelatedAAIUri = cloudRegionRelatedAAIUris.get(0)
                currentNSSI['cloudRegionRelatedAAIUri'] = cloudRegionRelatedAAIUri

                Optional<CloudRegion> cloudRegionrOpt = client.get(CloudRegion.class, cloudRegionRelatedAAIUris.get(0))
                CloudRegion cloudRegion = null
                if (cloudRegionrOpt.isPresent()) {
                    cloudRegion = cloudRegionrOpt.get()
                    cloudConfiguration.setLcpCloudRegionId(cloudRegion.getCloudRegionId())

                    if(cloudRegion.getTenants() != null && cloudRegion.getTenants().getTenant() != null) {
                        for (Tenant tenant : cloudRegion.getTenants().getTenant()) {
                            cloudConfiguration.setTenantId(tenant.getTenantId())
                            cloudConfiguration.setTenantName(tenant.getTenantName())
                            break // only one is required
                        }
                    }
                    else {
                        List<AAIResourceUri> tenantRelatedAAIUris = cloudRegionRelationshipsOps.get().getRelatedUris(Types.TENANT)
                        if (!(tenantRelatedAAIUris == null || tenantRelatedAAIUris.isEmpty())) {
                            Optional<Tenant> tenantOpt = client.get(Tenant.class, tenantRelatedAAIUris.get(0))
                            Tenant tenant = null
                            if (tenantOpt.isPresent()) {
                                tenant = tenantOpt.get()

                                LOGGER.debug("prepareCloudConfiguration: tenantId=" + tenant.getTenantId())
                                LOGGER.debug("prepareCloudConfiguration: tenantName=" + tenant.getTenantName())

                                cloudConfiguration.setTenantId(tenant.getTenantId())
                                cloudConfiguration.setTenantName(tenant.getTenantName())
                            }
                        }
                    }

                    cloudConfiguration.setCloudOwner(cloudRegion.getCloudOwner())
                }
            }
        }

        LOGGER.debug("${getPrefix()} Exit prepareCloudConfiguration")

        return cloudConfiguration
    }


    /**
     * Prepares a list of VF Modules
     * @param execution
     * @param constituteVnf
     * @return List<VfModules>
     */
    List<org.onap.so.serviceinstancebeans.VfModules> prepareVfModules(DelegateExecution execution, GenericVnf constituteVnf) {
        LOGGER.debug("${getPrefix()} Start prepareVfModules")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        List<org.onap.so.serviceinstancebeans.VfModules> vfModuless = new ArrayList<>()

        ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']

        String networkServiceModelInvariantUuid = networkServiceInstance.getModelInvariantId()

        String serviceVnfs="";
        String msg=""
        try{
            CatalogDbUtils catalogDbUtils = getCatalogDbUtilsFactory().create()

            String json = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuidString(execution, networkServiceModelInvariantUuid)
            LOGGER.debug("***** JSON IS: "+json)

            serviceVnfs = jsonUtil.getJsonValue(json, "serviceResources.serviceVnfs") ?: ""

            ObjectMapper mapper = new ObjectMapper()

            List<Object> vnfList = mapper.readValue(serviceVnfs, List.class)
            LOGGER.debug("vnfList:  "+vnfList)

            Map vnfMap = vnfList.get(0)
            ModelInfo vnfModelInfo = vnfMap.get("modelInfo")
            vnfModelInfo.setModelCustomizationId(vnfModelInfo.getModelCustomizationUuid())
            vnfModelInfo.setModelVersionId(vnfModelInfo.getModelId())
            LOGGER.debug("vnfModelInfo "+vnfModelInfo)

            //List of VFModules
            List<Map<String, Object>> vfModuleList = vnfMap.get("vfModules")
            LOGGER.debug("vfModuleList "+vfModuleList)

            //List of VfModules
            List<ModelInfo> vfModelInfoList = new ArrayList<>()

            //Traverse VFModules List and add in vfModelInfoList
            for (vfModule in vfModuleList) {
                ModelInfo vfModelInfo = vfModule.get("modelInfo")
                vfModelInfo.setModelCustomizationId(vfModelInfo.getModelCustomizationUuid())
                vfModelInfo.setModelVersionId(vfModelInfo.getModelId())
                LOGGER.debug("vfModelInfo "+vfModelInfo)
                vfModelInfoList.add(vfModelInfo)
            }

            for (ModelInfo vfModuleModelInfo : vfModelInfoList) {
                org.onap.so.serviceinstancebeans.VfModules vfModules = new org.onap.so.serviceinstancebeans.VfModules()
                vfModules.setModelInfo(vfModuleModelInfo)
                vfModules.setInstanceName(vfModuleModelInfo.getModelName())

                List<Map<String, Object>> vfModuleInstanceParams = new ArrayList<>()
                vfModules.setInstanceParams(vfModuleInstanceParams)
                vfModuless.add(vfModules)
            }

        } catch (Exception ex){
            msg = "Exception in prepareVfModules " + ex.getMessage()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        /*
        List<org.onap.so.serviceinstancebeans.VfModules> vfModuless = new ArrayList<>()
        if(constituteVnf.getVfModules() != null && constituteVnf.getVfModules().getVfModule() != null) {
            for (VfModule vfModule : constituteVnf.getVfModules().getVfModule()) {
                org.onap.so.serviceinstancebeans.VfModules vfmodules = new org.onap.so.serviceinstancebeans.VfModules()

                ModelInfo vfModuleModelInfo = new ModelInfo()
                vfModuleModelInfo.setModelInvariantUuid(vfModule.getModelInvariantId())
                vfModuleModelInfo.setModelCustomizationId(vfModule.getModelCustomizationId())

                AAIResourceUri vfModuleUrl = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.serviceDesignAndCreation().model(vfModule.getModelInvariantId()).modelVer(vfModule.getModelVersionId()))

                Optional<ModelVer> vfModuleModelVerOpt = client.get(ModelVer.class, vfModuleUrl)

                if (vfModuleModelVerOpt.isPresent()) {
                    vfModuleModelInfo.setModelVersionId(vfModuleModelVerOpt.get().getModelVersionId())
                    vfModuleModelInfo.setModelName(vfModuleModelVerOpt.get().getModelName())
                    vfModuleModelInfo.setModelVersion(vfModuleModelVerOpt.get().getModelVersion())
                }
                vfmodules.setModelInfo(vfModuleModelInfo)

                vfmodules.setInstanceName(vfModule.getVfModuleName())

                vfModuless.add(vfmodules)
            }
        } */

        LOGGER.debug("${getPrefix()} Exit prepareVfModules")

        return vfModuless
    }


    /**
     * prepares VNF Model Info
     * @param execution
     * @param constituteVnf
     * @return ModelInfo
     */
    ModelInfo prepareVNFModelInfo(DelegateExecution execution, GenericVnf constituteVnf) {
        LOGGER.debug("${getPrefix()} Start prepareVNFModelInfo")

        ModelInfo vnfModelInfo = new ModelInfo()

        AAIResourcesClient client = getAAIClient()

        vnfModelInfo.setModelInvariantUuid(constituteVnf.getModelInvariantId())
        vnfModelInfo.setModelCustomizationId(constituteVnf.getModelCustomizationId())
        vnfModelInfo.setModelInstanceName(constituteVnf.getVnfName())

        AAIResourceUri vnfModelUrl = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.serviceDesignAndCreation().model(constituteVnf.getModelInvariantId()).modelVer(constituteVnf.getModelVersionId()))

        Optional<ModelVer> vnfModelVerOpt = client.get(ModelVer.class, vnfModelUrl)

        if (vnfModelVerOpt.isPresent()) {
            vnfModelInfo.setModelVersionId(vnfModelVerOpt.get().getModelVersionId())
            vnfModelInfo.setModelName(vnfModelVerOpt.get().getModelName())
            vnfModelInfo.setModelVersion(vnfModelVerOpt.get().getModelVersion())
        }

        LOGGER.debug("${getPrefix()} Exit prepareVNFModelInfo")

        return vnfModelInfo
    }


    List<Map<String, Object>> prepareInstanceParams(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start prepareInstanceParams")

        def currentNSSI = execution.getVariable("currentNSSI")

        List<Map<String, Object>> instanceParams = new ArrayList<>()
        Map<String, Object> instanceParamsMap = new HashMap<>()

        // Supported S-NSSAI
        def snssaisList = currentNSSI['S-NSSAIs']
        List<String> snssais = new ArrayList<>()
        if(snssaisList != null) {
            snssais = new ArrayList<String>((List<String>)snssaisList)
        }

        LOGGER.debug("prepareInstanceParams: snssais size = " + snssais.size())

        ServiceInstance nssi = (ServiceInstance) currentNSSI['nssi']

        String orchStatus = nssi.getOrchestrationStatus()


        List<Map<String, String>> snssaiList = new ArrayList<>()

        for(String snssai:snssais) {
            LOGGER.debug("prepareInstanceParams: snssai = " + snssai)
            Map<String, String> snssaisMap = new HashMap<>()
            snssaisMap.put("snssai", snssai)
            snssaisMap.put("status", orchStatus)
            snssaiList.add(snssaisMap)
        }

        //    Map<String, List<Map<String, String>>> supportedNssaiDetails = new HashMap<>()
        //    supportedNssaiDetails.put("sNssai", supportedNssaiDetails)

        ObjectMapper mapper = new ObjectMapper()

        Map<String, Object> nSsai= new LinkedHashMap<>()
        nSsai.put("sNssai", snssaiList)

       // String supportedsNssaiJson = mapper.writeValueAsString(snssaiList)
        String supportedsNssaiJson = mapper.writeValueAsString(nSsai)

        instanceParamsMap.put("k8s-rb-profile-name", "default") // ???
        instanceParamsMap.put("config-type", "day2") // ???
        instanceParamsMap.put("supportedsNssai", supportedsNssaiJson)
        instanceParams.add(instanceParamsMap)

        LOGGER.debug("${getPrefix()} Exit prepareInstanceParams")

        return instanceParams
    }

    /**
     * Prepares Resources
     * @param execution
     * @return Resources
     */
    Resources prepareResources(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start prepareResources")

        def currentNSSI = execution.getVariable("currentNSSI")

        Resources resources = new Resources()

        // VNFs
        List<Vnfs> vnfs = new ArrayList<>()
        // VNF
        Vnfs vnf = new Vnfs()

        // Line of Business
        org.onap.so.serviceinstancebeans.LineOfBusiness lob = new org.onap.so.serviceinstancebeans.LineOfBusiness()
        lob.setLineOfBusinessName("VNF")
        vnf.setLineOfBusiness(lob)

        // Product family ID
        GenericVnf constituteVnf = (GenericVnf)currentNSSI['constituteVnf']
        vnf.setProductFamilyId(constituteVnf.getServiceId())

        // Cloud configuration
        vnf.setCloudConfiguration(prepareCloudConfiguration(execution))

        // VF Modules
        vnf.setVfModules(prepareVfModules(execution, constituteVnf))

        // Model Info
        vnf.setModelInfo(prepareVNFModelInfo(execution, constituteVnf))

        // Instance name
        vnf.setInstanceName(constituteVnf.getVnfName())

        // Instance params
        vnf.setInstanceParams(prepareInstanceParams(execution))

        // No platform data

        vnfs.add(vnf)
        resources.setVnfs(vnfs)

        LOGGER.debug("${getPrefix()} Exit prepareResources")

        return resources
    }


    /**
     * Prepare Service
     * @return Service
     */
    org.onap.so.serviceinstancebeans.Service prepareService(DelegateExecution execution, ServiceInstance networkServiceInstance, ModelInfo modelInfo) {
        LOGGER.debug("${getPrefix()} Start prepareService")

        org.onap.so.serviceinstancebeans.Service service = new org.onap.so.serviceinstancebeans.Service()

        // Model Info
        service.setModelInfo(prepareServiceModelInfo(networkServiceInstance, modelInfo))

        service.setInstanceName(networkServiceInstance.getServiceInstanceName())

        // Resources
        service.setResources(prepareResources(execution))

        LOGGER.debug("${getPrefix()} Exit prepareService")

        return service

    }


    /**
     * Prepares request parameters
     * @param execution
     * @return RequestParameters
     */
    RequestParameters prepareRequestParameters(DelegateExecution execution, ServiceInstance networkServiceInstance, ModelInfo modelInfo) {
        LOGGER.debug("${getPrefix()} Start prepareRequestParameters")

        def currentNSSI = execution.getVariable("currentNSSI")

        RequestParameters requestParameters = new RequestParameters()

        ServiceSubscription serviceSubscription = (ServiceSubscription)currentNSSI['serviceSubscription']

        if(serviceSubscription != null) {
            requestParameters.setSubscriptionServiceType(serviceSubscription.getServiceType())
        }

        // User params
        List<Map<String, Object>> userParams = new ArrayList<>()

        Map<String, Object> userParam = new HashMap<>()
        userParam.put("Homing_Solution", "none")
        userParams.add(userParam)

        // Service
        Map<String, Object> serviceMap = new HashMap<>()
        serviceMap.put("service", prepareService(execution, networkServiceInstance, modelInfo))
        userParams.add(serviceMap)
        requestParameters.setUserParams(userParams)

        requestParameters.setaLaCarte(false)

        LOGGER.debug("${getPrefix()} Exit prepareRequestParameters")

        return requestParameters
    }


    /**
     * Prepare Owning Entity
     * @param execution
     * @return OwningEntity
     */
    org.onap.so.serviceinstancebeans.OwningEntity prepareOwningEntity(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start prepareOwningEntity")

        def currentNSSI = execution.getVariable("currentNSSI")

        AAIResourcesClient client = getAAIClient()

        AAIResourceUri networkServiceInstanceUri = (AAIResourceUri)currentNSSI['networkServiceInstanceUri']

        org.onap.so.serviceinstancebeans.OwningEntity owningEntity = new org.onap.so.serviceinstancebeans.OwningEntity()
        AAIResultWrapper wrapper = client.get(networkServiceInstanceUri)
        Optional<Relationships> owningEntityRelationshipsOps = wrapper.getRelationships()
        if (owningEntityRelationshipsOps.isPresent()) {
            List<AAIResourceUri> owningEntityRelatedAAIUris = owningEntityRelationshipsOps.get().getRelatedUris(Types.OWNING_ENTITY)

            if (!(owningEntityRelatedAAIUris == null || owningEntityRelatedAAIUris.isEmpty())) {
                Optional<org.onap.aai.domain.yang.OwningEntity> owningEntityOpt = client.get(org.onap.aai.domain.yang.v19.OwningEntity.class, owningEntityRelatedAAIUris.get(0)) // Many-To-One relation
                if (owningEntityOpt.isPresent()) {
                    owningEntity.setOwningEntityId(owningEntityOpt.get().getOwningEntityId())
                    owningEntity.setOwningEntityName(owningEntityOpt.get().getOwningEntityName())

                }
            }
        }

        LOGGER.debug("${getPrefix()} Exit prepareOwningEntity")

        return owningEntity
    }


    /**
     * Prepares Project
     * @param execution
     * @return Project
     */
    org.onap.so.serviceinstancebeans.Project prepareProject(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start prepareProject")

        def currentNSSI = execution.getVariable("currentNSSI")

        AAIResourcesClient client = getAAIClient()

        org.onap.so.serviceinstancebeans.Project project = new org.onap.so.serviceinstancebeans.Project()

        AAIResourceUri cloudRegionRelatedAAIUri = (AAIResourceUri)currentNSSI['cloudRegionRelatedAAIUri']

        if (cloudRegionRelatedAAIUri != null) {
            AAIResultWrapper wrapper = client.get(cloudRegionRelatedAAIUri)
            Optional<Relationships> cloudRegionOps = wrapper.getRelationships()
            if (cloudRegionOps.isPresent()) {
                List<AAIResourceUri> projectAAIUris = cloudRegionOps.get().getRelatedUris(Types.PROJECT)
                if (!(projectAAIUris == null || projectAAIUris.isEmpty())) {
                    Optional<org.onap.aai.domain.yang.Project> projectOpt = client.get(org.onap.aai.domain.yang.v19.Project.class, projectAAIUris.get(0))
                    if (projectOpt.isPresent()) {
                        project.setProjectName(projectOpt.get().getProjectName())
                    }
                }
            }
        }

        LOGGER.debug("${getPrefix()} Exit prepareProject")

        return project
    }


    /**
     * Prepares RequestDetails object
     * @param execution
     * @return
     */
    String prepareRequestDetails(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start prepareRequestDetails")

        String errorCode = ""
        String errorMessage = ""
        String response

        RequestDetails requestDetails = new RequestDetails()

        def currentNSSI = execution.getVariable("currentNSSI")

        ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']

        try {
            // Model Info
            ModelInfo modelInfo = prepareModelInfo(execution)
            requestDetails.setModelInfo(modelInfo)

            // Subscriber Info
            requestDetails.setSubscriberInfo(prepareSubscriberInfo(execution))

            // Request Info
            requestDetails.setRequestInfo(prepareRequestInfo(execution, networkServiceInstance))

            // Request Parameters
            requestDetails.setRequestParameters(prepareRequestParameters(execution, networkServiceInstance, modelInfo))

            // Cloud configuration
            requestDetails.setCloudConfiguration(prepareCloudConfiguration(execution))

            // Owning entity
            requestDetails.setOwningEntity(prepareOwningEntity(execution))

            // Project
            requestDetails.setProject(prepareProject(execution))

            Map<String, Object> requestDetailsMap = new LinkedHashMap<>()
            requestDetailsMap.put("requestDetails", requestDetails)

            ObjectMapper mapper = new ObjectMapper()

            response = mapper.writeValueAsString(requestDetailsMap)
        }
        catch (any) {
            String msg = "Exception in ${getPrefix()}.prepareRequestDetails. " + any.getCause()
            LOGGER.error(msg)

            response =  "{\n" +
                    " \"errorCode\": \"7000\",\n" +
                    " \"errorMessage\": \"${msg}\"\n" +
                    "}"

        }

        LOGGER.debug("${getPrefix()} Exit prepareRequestDetails")

        return response
    }


    String getAuthHeader(DelegateExecution execution, String basicAuthValue, String msokey) {
        String response = ""
        String errorCode = ""
        String errorMessage = ""

        LOGGER.debug("Obtained BasicAuth username and password for OOF: " + basicAuthValue)
        try {
            response = utils.getBasicAuth(basicAuthValue, msokey)
        } catch (Exception ex) {
            LOGGER.error("Unable to encode username and password string: ", ex)

            errorCode = "401"
            errorMessage = "Internal Error - Unable to encode username and password string"

            response =  "{\n" +
                    " \"errorCode\": \"${errorCode}\",\n" +
                    " \"errorMessage\": \"${errorMessage}\"\n" +
                    "}"
        }

        return response
    }


    String encryptBasicAuth(String basicAuth, String msoKey) {
        return utils.encrypt(basicAuth, msoKey)
    }


    /**
     * Retrieves NSSI associated profiles from AAI
     * @param execution
     */
    void getNSSIAssociatedProfiles(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start getNSSIAssociatedProfiles")

        List<SliceProfile> associatedProfiles = new ArrayList<>()

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        ServiceInstance nssi = (ServiceInstance)currentNSSI['nssi']

        String nssiId = currentNSSI['nssiId']

        String givenSliceProfileId = currentNSSI['sliceProfileId']

        // NSSI
        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))
        AAIResultWrapper nssiWrapper = client.get(nssiUri)
        Optional<Relationships> nssiRelationships = nssiWrapper.getRelationships()

        if (nssiRelationships.isPresent()) {
            // Allotted Resource
            for (AAIResourceUri allottedResourceUri : nssiRelationships.get().getRelatedUris(Types.ALLOTTED_RESOURCE)) {
                AAIResultWrapper arWrapper = client.get(allottedResourceUri)
                Optional<Relationships> arRelationships = arWrapper.getRelationships()

                if(arRelationships.isPresent()) {
                    // Slice Profile Instance
                    for (AAIResourceUri sliceProfileInstanceUri : arRelationships.get().getRelatedUris(Types.SERVICE_INSTANCE)) {
                        Optional<ServiceInstance> sliceProfileInstanceOpt = client.get(ServiceInstance.class, sliceProfileInstanceUri)

                        if (sliceProfileInstanceOpt.isPresent()) {
                            ServiceInstance sliceProfileInstance = sliceProfileInstanceOpt.get()
                            if(sliceProfileInstance.getServiceRole().equals("slice-profile-instance")) { // Service instance as a Slice Profile Instance

                                String globalSubscriberId = execution.getVariable("globalSubscriberId")
                                String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

                                org.onap.aaiclient.client.generated.fluentbuilders.SliceProfiles sliceProfilesType =
                                        AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(sliceProfileInstance.getServiceInstanceId()).sliceProfiles()

                                def sliceProfilesUri = AAIUriFactory.createResourceUri(sliceProfilesType)
                                LOGGER.debug("client.exists(sliceProfilesUri = " + client.exists(sliceProfilesUri))
                                if (!client.exists(sliceProfilesUri)) {
                                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Slice Profiles URI doesn't exist")
                                }

                                AAIResultWrapper sliceProfilesWrapper = client.get(sliceProfilesUri)
                                Optional<SliceProfiles> sliceProfilesOpt = sliceProfilesWrapper.asBean(SliceProfiles.class)
                                if(sliceProfilesOpt.isPresent()) {
                                    SliceProfiles sliceProfiles = sliceProfilesOpt.get()

                                    LOGGER.debug("getNSSIAssociatedProfiles: sliceProfiles.getSliceProfile().size() = " + sliceProfiles.getSliceProfile().size())
                                    LOGGER.debug("getNSSIAssociatedProfiles: givenSliceProfileId = " + givenSliceProfileId)
                                    for(SliceProfile sliceProfile: sliceProfiles.getSliceProfile()) {
                                        LOGGER.debug("getNSSIAssociatedProfiles: sliceProfile.getProfileId() = " + sliceProfile.getProfileId())
                                        if(sliceProfile.getProfileId().equals(givenSliceProfileId)) { // Slice profile id equals to received slice profile id
                                            currentNSSI['sliceProfileInstanceUri'] = sliceProfileInstanceUri
                                        }

                                    }

                                    associatedProfiles.addAll(sliceProfiles.getSliceProfile()) // Adds all slice profiles
                                }

                            }
                        }
                        else {
                            exceptionUtil.buildAndThrowWorkflowException(execution, 500, "No Slice Profile Instance found")
                        }
                    }
                }
                else {
                    exceptionUtil.buildAndThrowWorkflowException(execution, 500, "No relationships found for Allotted Resource")
                }

            }
        }
        else {
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, "No relationships  found for nssi id = " + nssiId)
        }

        checkAssociatedProfiles(execution, associatedProfiles, nssi)

        currentNSSI['associatedProfiles'] =  associatedProfiles

        LOGGER.debug("${getPrefix()} Exit getNSSIAssociatedProfiles")
    }


    void checkAssociatedProfiles(DelegateExecution execution, List<SliceProfile> associatedProfiles, ServiceInstance nssi) {}


    /**
     * Removes Slice Profile association with NSSI
     * @param execution
     */
    void removeSPAssociationWithNSSI(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start removeSPAssociationWithNSSI")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiId']
        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))

        String isTerminateNSSIVar = execution.getVariable("isTerminateNSSI" )

        boolean isTerminateNSSI = Boolean.parseBoolean(isTerminateNSSIVar)

        AAIResourceUri sliceProfileInstanceUri = null
        if(!isTerminateNSSI) { // In case of NSSI non-termination associated Slice Profile Instance should be presented
            def spURI = currentNSSI['sliceProfileInstanceUri']
            if(spURI != null) {
                sliceProfileInstanceUri = (AAIResourceUri)spURI
            }
            else {
                String msg = "Slice Profile association with NSSI was already removed"
                LOGGER.info(msg)
            }
        }

        // Removes SLice Profile Instance association with NSSI
        if(sliceProfileInstanceUri != null) { // NSSI should not be terminated
            try {
                client.disconnect(nssiUri, sliceProfileInstanceUri)
            }
            catch (Exception e) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while Slice Profile Instance association with NSSI dosconnect call: " + e.getMessage())
            }
        }


        LOGGER.debug("${getPrefix()} Exit removeSPAssociationWithNSSI")
    }


    /**
     * Deletes Slice Profile Instance
     * @param execution
     */
    void deleteSliceProfileInstance(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start deleteSliceProfileInstance")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        String isTerminateNSSIVar = execution.getVariable("isTerminateNSSI" )

        boolean isTerminateNSSI = Boolean.parseBoolean(isTerminateNSSIVar)

        AAIResourceUri sliceProfileInstanceUri = null
        if(!isTerminateNSSI) { // In case of NSSI non-termination associated Slice Profile Instance should be presented
            def spURI = currentNSSI['sliceProfileInstanceUri']
            if(spURI != null) {
                sliceProfileInstanceUri = (AAIResourceUri)spURI
            }
            else {
                String msg = "Slice Profile instance was already deleted"
                LOGGER.info(msg)
            }
        }

        if(sliceProfileInstanceUri != null) {
            try {
                client.delete(sliceProfileInstanceUri)
            } catch (Exception e) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while Slice Profile Instance delete call: " + e.getMessage())
            }
        }

        LOGGER.debug("${getPrefix()} Exit deleteSliceProfileInstance")
    }


    /**
     * Prepares update resource operation status
     * @param execution
     */
    void prepareUpdateResourceOperationStatus(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start updateServiceOperationStatus")

        def currentNSSI = execution.getVariable("currentNSSI")

        //Prepare Update Status for PUT failure and success
        String isTimeOutVar = execution.getVariable("isTimeOut")
        if(!isBlank(isTimeOutVar) && isTimeOutVar.equals("YES")) {
            LOGGER.error("TIMEOUT - SO PUT Failure")
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "SO PUT Failure")
        } else {
            execution.setVariable("progress", "100")
            execution.setVariable("status", "finished")
            execution.setVariable("operationContent", "${getAction()} Core NSSI successful.")
        }

        setResourceOperationStatus(execution, "finished", "100", "Core NSSI ${getAction()} successful")

        LOGGER.debug("${getPrefix()} Exit updateServiceOperationStatus")
    }


    /**
     * Prepares ResourceOperation status
     * @param execution
     * @param operationType
     */
    void setResourceOperationStatus(DelegateExecution execution, String status, String progress, String statusDesc) {
        LOGGER.debug("${getPrefix()} Start setResourceOperationStatus")

        def currentNSSI = execution.getVariable("currentNSSI")

        String serviceId = currentNSSI['nsiId']
        String jobId = execution.getVariable("jobId")
        String nsiId = currentNSSI['nsiId']
        String operationType = execution.getVariable("operationType")
        String resourceInstanceId = currentNSSI['nssiId']

        ServiceInstance nssi = (ServiceInstance) currentNSSI['nssi']
        String modelUuid = nssi.getModelVersionId()

        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus()
        resourceOperationStatus.setServiceId(serviceId)
        resourceOperationStatus.setOperationId(jobId)
        resourceOperationStatus.setResourceTemplateUUID(modelUuid)
        resourceOperationStatus.setOperType(operationType)
        resourceOperationStatus.setResourceInstanceID(resourceInstanceId)
        resourceOperationStatus.setStatus(status)
        resourceOperationStatus.setProgress(progress)
        resourceOperationStatus.setStatusDescription(statusDesc)
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, resourceOperationStatus)

        LOGGER.debug("${getPrefix()} Exit setResourceOperationStatus")
    }


    /**
     * Prepares failed operation status update
     * @param execution
     */
    void prepareFailedOperationStatusUpdate(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start prepareFailedOperationStatusUpdate")

        setResourceOperationStatus(execution, "failed", "0", "Core NSSI ${getAction()} Failed")

        LOGGER.debug("${getPrefix()} Exit prepareFailedOperationStatusUpdate")
    }


    /**
     * Gets progress status of ServiceInstance PUT operation
     * @param execution
     */
    public void getPUTServiceInstanceProgress(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start getPUTServiceInstanceProgress")

        def currentNSSI = execution.getVariable("currentNSSI")

        ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']

        String url = currentNSSI['requestSelfLink']

        String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)

        String basicAuth =  UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)

        def authHeader = ""
        String basicAuthValue = utils.getBasicAuth(basicAuth, msoKey)

        getProgress(execution, url, basicAuthValue, "putStatus")

        LOGGER.debug("${getPrefix()} Exit getPUTServiceInstanceProgress")
    }


    void getProgress(DelegateExecution execution, String url, String authHeader, String statusVariableName) {
        LOGGER.debug("${getPrefix()} Start getProgress")

        LOGGER.debug("getProgress: url = " + url)
        LOGGER.debug("getProgress: authHeader = " + authHeader)

        String msg=""
        try {

            HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.SO)
            httpClient.addAdditionalHeader("Authorization", authHeader)
            httpClient.addAdditionalHeader("Accept", "application/json")

            Response response = httpClient.get()
            int responseCode = response.getStatus()
          //  execution.setVariable("GetServiceOrderResponseCode", responseCode)
            LOGGER.debug("Get ServiceOrder response code is: " + responseCode)

            String soResponse = ""
            if(response.hasEntity()) {
                soResponse = response.readEntity(String.class)
         //       execution.setVariable("GetServiceOrderResponse", extApiResponse)
                LOGGER.debug("Create response body is: " + soResponse)
            }

            //Process Response //200 OK 201 CREATED 202 ACCEPTED
            if (responseCode >= 200 && responseCode < 204) {
                LOGGER.debug("Get Create ServiceOrder Received a Good Response")
                String requestState = jsonUtil.getJsonValue(soResponse, "request.requestStatus.requestState")

                /*
                JSONArray items = responseObj.getJSONArray("orderItem")
                JSONObject item = items.get(0)
                JSONObject service = item.get("service")
                String networkServiceId = service.get("id")
                if (networkServiceId == null || networkServiceId.equals("null")) {
                    prepareFailedOperationStatusUpdate(execution)
                    return
                }

                execution.setVariable("networkServiceId", networkServiceId)
                String serviceOrderState = item.get("state")
                execution.setVariable("ServiceOrderState", serviceOrderState) */

                // Get serviceOrder State and process progress
                if("ACKNOWLEDGED".equalsIgnoreCase(requestState)) {
                    execution.setVariable(statusVariableName, "processing")
                }
                else if("IN_PROGRESS".equalsIgnoreCase(requestState)) {
                    execution.setVariable(statusVariableName, "processing")
                }
                else if("COMPLETE".equalsIgnoreCase(requestState)) {
                    execution.setVariable(statusVariableName, "completed")
                }
                else if("FAILED".equalsIgnoreCase(requestState)) {
                    msg = "ServiceOrder failed"
                    exceptionUtil.buildAndThrowWorkflowException(execution, 7000,  msg)
                }
                else if("REJECTED".equalsIgnoreCase(requestState)) {
                    prepareFailedOperationStatusUpdate(execution)
                }
                else {
                    msg = "ServiceOrder failed"
                    exceptionUtil.buildAndThrowWorkflowException(execution, 7000,  msg)
                }
            }
            else{
                msg = "Get ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode
                prepareFailedOperationStatusUpdate(execution)
            }

        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000,  e.getMessage())
        }

        LOGGER.debug("${getPrefix()} Exit getProgress")
    }



    /**
     * Delays 5 sec
     * @param execution
     */
    void timeDelay(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start timeDelay")

        try {
            LOGGER.debug("${getPrefix()} timeDelay going to sleep for 5 sec")

            Thread.sleep(5000)

            LOGGER.debug("${getPrefix()} ::: timeDelay wakeup after 5 sec")
        } catch(InterruptedException e) {
            LOGGER.error("${getPrefix()} ::: timeDelay exception" + e)
        }

        LOGGER.debug("${getPrefix()} Exit timeDelay")
    }


    void postProcessRequest(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start postProcessRequest")

        execution.removeVariable("currentNSSI")

        LOGGER.debug("***** ${getPrefix()} Exit postProcessRequest")
    }



    /**
     * Returns AAI client
     * @return AAI client
     */
    AAIResourcesClient getAAIClient() {
        return new AAIResourcesClient()
    }


    ExternalAPIUtilFactory getExternalAPIUtilFactory() {
        return new ExternalAPIUtilFactory()
    }


    /**
     * Returns Catalog DB Util Factory
     * @return ew CatalogDbUtilsFactory()
     */
    CatalogDbUtilsFactory getCatalogDbUtilsFactory() {
        return new CatalogDbUtilsFactory()
    }


    private String getPrefix() {
        return PREFIX
    }

    String getAction() {
        return ""
    }
}
