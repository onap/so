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
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.aai.domain.yang.*
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtilFactory
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.onap.so.serviceinstancebeans.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.core.Response

import static org.apache.commons.lang3.StringUtils.isBlank


class DoCommonCoreNSSI extends AbstractServiceTaskProcessor {

    private final String PREFIX ="DoCommonCoreNSSI"

    private static final Logger LOGGER = LoggerFactory.getLogger( DoCommonCoreNSSI.class)

    private JsonUtils jsonUtil = new JsonUtils()
    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()

    @Override
    void preProcessRequest(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start preProcessRequest")

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


        // SLice Profile
        String sliceProfile = jsonUtil.getJsonValue(execution.getVariable("sliceParams"), "sliceProfile")
        if (isBlank(sliceProfile)) {
            String msg = "Slice Profile is null"
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        } else {
            currentNSSI['sliceProfile'] = sliceProfile
        }

        // S-NSSAI
        def snssaiList = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(sliceProfile, "snssaiList"))

        String sNssai = snssaiList.get(0)
        currentNSSI['S-NSSAI'] = sNssai


        // Slice Profile id
        String sliceProfileId = jsonUtil.getJsonValue(sliceProfile, "sliceProfileId")
        currentNSSI['sliceProfileId'] = sliceProfileId

        execution.setVariable("currentNSSI", currentNSSI)


        LOGGER.trace("***** ${getPrefix()} Exit preProcessRequest")
    }


    /**
     * Queries Network Service Instance in AAI
     * @param execution
     */
    void getNetworkServiceInstance(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start getNetworkServiceInstance")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiId']

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId)
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

        LOGGER.trace("${getPrefix()} Exit getNetworkServiceInstance")
    }


    /**
     * Handles Network Service
     * @param nssiId
     * @param nssiUri
     * @param client
     * @return Network Service Instance
     */
    private ServiceInstance handleNetworkInstance(DelegateExecution execution, String nssiId, AAIResourceUri nssiUri, AAIResourcesClient client ) {
        ServiceInstance networkServiceInstance = null

        def currentNSSI = execution.getVariable("currentNSSI")

        AAIResultWrapper wrapper = client.get(nssiUri)
        Optional<Relationships> relationships = wrapper.getRelationships()

        if (relationships.isPresent()) {
            for (AAIResourceUri networkServiceInstanceUri : relationships.get().getRelatedAAIUris(AAIObjectType.SERVICE_INSTANCE)) {
                Optional<ServiceInstance> networkServiceInstanceOpt = client.get(ServiceInstance.class, networkServiceInstanceUri)
                if (networkServiceInstanceOpt.isPresent()) {
                    networkServiceInstance = networkServiceInstanceOpt.get()

                    if (networkServiceInstance.getServiceRole() == "Network Service") { // Network Service role
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

        return networkServiceInstance
    }


    /**
     * Queries constitute VNF from Network Service Instance
     * @param execution
     */
    void getConstituteVNFFromNetworkServiceInst(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start getConstituteVNFFromNetworkServiceInst")

        def currentNSSI = execution.getVariable("currentNSSI")

        AAIResourcesClient client = getAAIClient()

        AAIResourceUri networkServiceInstanceUri = (AAIResourceUri)currentNSSI['networkServiceInstanceUri']
        AAIResultWrapper wrapper = client.get(networkServiceInstanceUri);
        Optional<Relationships> relationships = wrapper.getRelationships()
        if (relationships.isPresent()) {
            for (AAIResourceUri constituteVnfUri : relationships.get().getRelatedAAIUris(AAIObjectType.GENERIC_VNF)) {
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

        LOGGER.trace("${getPrefix()} Exit getConstituteVNFFromNetworkServiceInst")

    }


    /**
     * Retrieves NSSI associated profiles from AAI
     * @param execution
     */
    void getNSSIAssociatedProfiles(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start getNSSIAssociatedProfiles")

        def currentNSSI = execution.getVariable("currentNSSI")

        ServiceInstance nssi = (ServiceInstance)currentNSSI['nssi']

        List<SliceProfile> associatedProfiles = nssi.getSliceProfiles().getSliceProfile()

        if(associatedProfiles.isEmpty()) {
            String msg = String.format("No associated profiles found for NSSI %s in AAI", nssi.getServiceInstanceId())
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
        }
        else {
            currentNSSI['associatedProfiles'] =  associatedProfiles
        }

        LOGGER.trace("${getPrefix()} Exit getNSSIAssociatedProfiles")
    }


    /**
     * Calculates a final list of S-NSSAI
     * @param execution
     */
    void calculateSNSSAI(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start calculateSNSSAI")

        def currentNSSI = execution.getVariable("currentNSSI")

        List<SliceProfile> associatedProfiles = (List<SliceProfile>)currentNSSI['associatedProfiles']

        String currentSNSSAI = currentNSSI['S-NSSAI']

        List<String> snssais = new ArrayList<>()

        String isCreateSliceProfileInstanceVar = execution.getVariable("isCreateSliceProfileInstance" ) // Not exist in case of Deallocate

        boolean isCreateSliceProfileInstance = Boolean.parseBoolean(isCreateSliceProfileInstanceVar)

        if(isCreateSliceProfileInstance) { // Slice Profile Instance has to be created
            for (SliceProfile associatedProfile : associatedProfiles) {
                snssais.add(associatedProfile.getSNssai())
            }

            snssais.add(currentSNSSAI)
        }
        else { // Slice profile instance has to be deleted
            for (SliceProfile associatedProfile : associatedProfiles) {
                if (!associatedProfile.getSNssai().equals(currentSNSSAI)) { // not current S-NSSAI
                    snssais.add(associatedProfile.getSNssai())
                } else {
                    currentNSSI['sliceProfileS-NSSAI'] = associatedProfile
                }
            }
        }

        currentNSSI['S-NSSAIs'] = snssais

        LOGGER.trace("${getPrefix()} Exit calculateSNSSAI")
    }


    /**
     * Invoke PUT Service Instance API
     * @param execution
     */
    void invokePUTServiceInstance(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start invokePUTServiceInstance")

        def currentNSSI = execution.getVariable("currentNSSI")

        try {
            //url:/onap/so/infra/serviceInstantiation/v7/serviceInstances/{serviceInstanceId}"
            def nsmfЕndPoint = UrnPropertiesReader.getVariable("mso.infra.endpoint.url", execution) // ???

            ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']

            String url = String.format("${nsmfЕndPoint}/serviceInstantiation/v7/serviceInstances/%s", networkServiceInstance.getServiceInstanceId())

            currentNSSI['putServiceInstanceURL'] = url

            String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)
            String basicAuth =  UrnPropertiesReader.getVariable("mso.infra.endpoint.auth", execution)

            def authHeader = ""
            String basicAuthValue = encryptBasicAuth(basicAuth, msoKey) //utils.encrypt(basicAuth, msoKey)
            String responseAuthHeader = getAuthHeader(execution, basicAuthValue, msoKey) //utils.getBasicAuth(basicAuthValue, msoKey)

            String errorCode = jsonUtil.getJsonValue(responseAuthHeader, "errorCode")
            if(errorCode == null || errorCode.isEmpty()) { // No error
                authHeader = responseAuthHeader
            }
            else {
                exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(responseAuthHeader, "errorMessage"))
            }

            def requestDetails = ""
            String prepareRequestDetailsResponse = prepareRequestDetails(execution)
            errorCode = jsonUtil.getJsonValue(prepareRequestDetailsResponse, "errorCode")
            if(errorCode == null || errorCode.isEmpty()) { // No error
                requestDetails = prepareRequestDetailsResponse
            }
            else {
                exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(prepareRequestDetailsResponse, "errorMessage"))
            }

            String callPUTServiceInstanceResponse = callPUTServiceInstance(url, authHeader, requestDetails)
            String putServiceInstanceResponse = ""

            if(errorCode == null || errorCode.isEmpty()) { // No error
                putServiceInstanceResponse = callPUTServiceInstanceResponse
            }
            else {
                LOGGER.error(jsonUtil.getJsonValue(callPUTServiceInstanceResponse, "errorMessage"))
                exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(callPUTServiceInstanceResponse, "errorMessage"))
            }

        } catch (any) {
            String msg = "Exception in ${getPrefix()}.invokePUTServiceInstance. " + any.getCause()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        LOGGER.trace("${getPrefix()} Exit invokePUTServiceInstance")
    }


    String callPUTServiceInstance(String url, String authHeader, String requestDetailsStr) {
        String errorCode = ""
        String errorMessage = ""
        String response

        try {
            HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.EXTERNAL)
            httpClient.addAdditionalHeader("Authorization", authHeader)
            httpClient.addAdditionalHeader("Accept", "application/json")

            Response httpResponse = httpClient.put(requestDetailsStr) // check http code ???


            if (httpResponse.hasEntity()) {
                response = httpResponse.readEntity(String.class)
            }
            else {
                errorCode = 500
                errorMessage = "No response received."

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

        return response

    }


    /**
     * Prepare model info
     * @param execution
     * @param requestDetails
     * @return ModelInfo
     */
    ModelInfo prepareModelInfo(DelegateExecution execution) {

        def currentNSSI = execution.getVariable("currentNSSI")
        ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']

        ModelInfo modelInfo = new ModelInfo()

        modelInfo.setModelType(ModelType.service)
        modelInfo.setModelInvariantId(networkServiceInstance.getModelInvariantId())

        AAIResourcesClient client = getAAIClient()

        AAIResourceUri modelVerUrl = AAIUriFactory.createResourceUri(AAIObjectType.MODEL_VER, networkServiceInstance.getModelInvariantId(), networkServiceInstance.getModelVersionId())
        Optional<ModelVer> modelVerOpt = client.get(ModelVer.class, modelVerUrl)

        if (modelVerOpt.isPresent()) {
            modelInfo.setModelVersionId(modelVerOpt.get().getModelVersionId())
            modelInfo.setModelName(modelVerOpt.get().getModelName())
            modelInfo.setModelVersion(modelVerOpt.get().getModelVersion())
        }

        return modelInfo
    }


    /**
     * Prepares subscriber info
     * @param execution
     * @return SubscriberInfo
     */
    SubscriberInfo prepareSubscriberInfo(DelegateExecution execution) {
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
            List<AAIResourceUri> serviceSubscriptionRelatedAAIUris = serviceSubscriptionRelationshipsOps.get().getRelatedAAIUris(AAIObjectType.SERVICE_SUBSCRIPTION)
            if(!(serviceSubscriptionRelatedAAIUris == null || serviceSubscriptionRelatedAAIUris.isEmpty())) {
                AAIResourceUri serviceSubscriptionUri = serviceSubscriptionRelatedAAIUris.get(0) // Many-To-One relation
                Optional<ServiceSubscription> serviceSubscriptionOpt = client.get(ServiceSubscription.class, serviceSubscriptionUri)

                if(serviceSubscriptionOpt.isPresent()) {
                    currentNSSI['serviceSubscription'] = serviceSubscriptionOpt.get()
                }

                wrapper = client.get(serviceSubscriptionUri)
                Optional<Relationships> customerRelationshipsOps = wrapper.getRelationships()
                if(customerRelationshipsOps.isPresent()) {
                    List<AAIResourceUri> customerRelatedAAIUris = customerRelationshipsOps.get().getRelatedAAIUris(AAIObjectType.CUSTOMER)
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

        return subscriberInfo
    }


    /**
     * Prepares Request Info
     * @param execution
     * @return RequestInfo
     */
    RequestInfo prepareRequestInfo(DelegateExecution execution, ServiceInstance networkServiceInstance) {
        def currentNSSI = execution.getVariable("currentNSSI")

        String productFamilyId = execution.getVariable("productFamilyId")

        RequestInfo requestInfo = new RequestInfo()

        requestInfo.setInstanceName(networkServiceInstance.getServiceInstanceName())
        requestInfo.setSource("VID")
        requestInfo.setProductFamilyId(productFamilyId)
        requestInfo.setRequestorId("NBI")

        return requestInfo
    }


    /**
     * Prepares Model Info
     * @param networkServiceInstance
     * @param modelInfo
     * @return ModelInfo
     */
    ModelInfo prepareServiceModelInfo(ServiceInstance networkServiceInstance, ModelInfo modelInfo) {

        ModelInfo serviceModelInfo = new ModelInfo()
        serviceModelInfo.setModelType(ModelType.service)
        serviceModelInfo.setModelInvariantId(networkServiceInstance.getModelInvariantId())

        serviceModelInfo.setModelVersionId(modelInfo.getModelVersionId())
        serviceModelInfo.setModelName(modelInfo.getModelName())
        serviceModelInfo.setModelVersion(modelInfo.getModelVersion())

        return serviceModelInfo
    }


    /**
     * Prepares Cloud configuration
     * @param execution
     * @return CloudConfiguration
     */
    CloudConfiguration prepareCloudConfiguration(DelegateExecution execution) {
        def currentNSSI = execution.getVariable("currentNSSI")

        CloudConfiguration cloudConfiguration = new CloudConfiguration()

        AAIResourcesClient client = getAAIClient()

        AAIResourceUri constituteVnfUri = currentNSSI['constituteVnfUri']
        AAIResultWrapper wrapper = client.get(constituteVnfUri)
        Optional<Relationships> cloudRegionRelationshipsOps = wrapper.getRelationships()

        if(cloudRegionRelationshipsOps.isPresent()) {
            List<AAIResourceUri> cloudRegionRelatedAAIUris = cloudRegionRelationshipsOps.get().getRelatedAAIUris(AAIObjectType.CLOUD_REGION)
            if (!(cloudRegionRelatedAAIUris == null || cloudRegionRelatedAAIUris.isEmpty())) {
                AAIResourceUri cloudRegionRelatedAAIUri = cloudRegionRelatedAAIUris.get(0)
                currentNSSI['cloudRegionRelatedAAIUri'] = cloudRegionRelatedAAIUri

                Optional<CloudRegion> cloudRegionrOpt = client.get(CloudRegion.class, cloudRegionRelatedAAIUris.get(0))
                CloudRegion cloudRegion = null
                if (cloudRegionrOpt.isPresent()) {
                    cloudRegion = cloudRegionrOpt.get()
                    cloudConfiguration.setLcpCloudRegionId(cloudRegion.getCloudRegionId())
                    for (Tenant tenant : cloudRegion.getTenants().getTenant()) {
                        cloudConfiguration.setTenantId(tenant.getTenantId())
                        break // only one is required
                    }

                    cloudConfiguration.setCloudOwner(cloudRegion.getCloudOwner())
                }
            }
        }

        return cloudConfiguration
    }


    /**
     * Prepares a list of VF Modules
     * @param execution
     * @param constituteVnf
     * @return List<VfModules>
     */
    List<VfModules> prepareVfModules(DelegateExecution execution, GenericVnf constituteVnf) {

        AAIResourcesClient client = getAAIClient()

        List<VfModules> vfModuless = new ArrayList<>()
        for (VfModule vfModule : constituteVnf.getVfModules().getVfModule()) {
            VfModules vfmodules = new VfModules()

            ModelInfo vfModuleModelInfo = new ModelInfo()
            vfModuleModelInfo.setModelInvariantUuid(vfModule.getModelInvariantId())
            vfModuleModelInfo.setModelCustomizationId(vfModule.getModelCustomizationId())

            AAIResourceUri vfModuleUrl = AAIUriFactory.createResourceUri(AAIObjectType.MODEL_VER, vfModule.getModelInvariantId(), vfModule.getModelVersionId())

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

        return vfModuless
    }


    /**
     * prepares VNF Model Info
     * @param execution
     * @param constituteVnf
     * @return ModelInfo
     */
    ModelInfo prepareVNFModelInfo(DelegateExecution execution, GenericVnf constituteVnf) {
        ModelInfo vnfModelInfo = new ModelInfo()

        AAIResourcesClient client = getAAIClient()

        vnfModelInfo.setModelInvariantUuid(constituteVnf.getModelInvariantId())
        vnfModelInfo.setModelCustomizationId(constituteVnf.getModelCustomizationId())
        vnfModelInfo.setModelInstanceName(constituteVnf.getVnfName())

        AAIResourceUri vnfModelUrl = AAIUriFactory.createResourceUri(AAIObjectType.MODEL_VER, constituteVnf.getModelInvariantId(), constituteVnf.getModelVersionId())

        Optional<ModelVer> vnfModelVerOpt = client.get(ModelVer.class, vnfModelUrl)

        if (vnfModelVerOpt.isPresent()) {
            vnfModelInfo.setModelVersionId(vnfModelVerOpt.get().getModelVersionId())
            vnfModelInfo.setModelName(vnfModelVerOpt.get().getModelName())
            vnfModelInfo.setModelVersion(vnfModelVerOpt.get().getModelVersion())
        }

        return vnfModelInfo
    }


    List<Map<String, Object>> prepareInstanceParams(DelegateExecution execution) {
        def currentNSSI = execution.getVariable("currentNSSI")

        List<Map<String, Object>> instanceParams = new ArrayList<>()
        Map<String, Object> instanceParamsMap = new HashMap<>()

        // Supported S-NSSAI
        List<String> snssais = (List<String>) currentNSSI['S-NSSAIs']

        ServiceInstance nssi = (ServiceInstance) currentNSSI['nssi']

        String orchStatus = nssi.getOrchestrationStatus()


        List<Map<String, String>> snssaiList = new ArrayList<>()

        for(String snssai:snssais) {
            Map<String, String> snssaisMap = new HashMap<>()
            snssaisMap.put("snssai", snssai)
            snssaisMap.put("status", orchStatus)
            snssaiList.add(snssaisMap)
        }

        //    Map<String, List<Map<String, String>>> supportedNssaiDetails = new HashMap<>()
        //    supportedNssaiDetails.put("sNssai", supportedNssaiDetails)

        ObjectMapper mapper = new ObjectMapper()

        String supportedNssaiDetailsStr = mapper.writeValueAsString(snssaiList)


        instanceParamsMap.put("k8s-rb-profile-name", "default") // ???
        instanceParamsMap.put("config-type", "day2") // ???
        instanceParamsMap.put("supportedNssai", supportedNssaiDetailsStr)
        instanceParams.add(instanceParamsMap)

        return instanceParams
    }

    /**
     * Prepares Resources
     * @param execution
     * @return Resources
     */
    Resources prepareResources(DelegateExecution execution) {
        def currentNSSI = execution.getVariable("currentNSSI")

        Resources resources = new Resources()

        // VNFs
        List<Vnfs> vnfs = new ArrayList<>()
        // VNF
        Vnfs vnf = new Vnfs()

        // Line of Business
        LineOfBusiness lob = new LineOfBusiness()
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

        return resources
    }


    /**
     * Prepare Service
     * @return Service
     */
    Service prepareService(DelegateExecution execution, ServiceInstance networkServiceInstance, ModelInfo modelInfo) {
        Service service = new Service()

        // Model Info
        service.setModelInfo(prepareServiceModelInfo(networkServiceInstance, modelInfo))

        service.setInstanceName(networkServiceInstance.getServiceInstanceName())

        // Resources
        service.setResources(prepareResources(execution))

        return service

    }


    /**
     * Prepares request parameters
     * @param execution
     * @return RequestParameters
     */
    RequestParameters prepareRequestParameters(DelegateExecution execution, ServiceInstance networkServiceInstance, ModelInfo modelInfo) {
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

        return requestParameters
    }


    /**
     * Prepare Owning Entity
     * @param execution
     * @return OwningEntity
     */
    OwningEntity prepareOwningEntity(DelegateExecution execution) {
        def currentNSSI = execution.getVariable("currentNSSI")

        AAIResourcesClient client = getAAIClient()

        AAIResourceUri networkServiceInstanceUri = (AAIResourceUri)currentNSSI['networkServiceInstanceUri']

        OwningEntity owningEntity = new OwningEntity()
        AAIResultWrapper wrapper = client.get(networkServiceInstanceUri)
        Optional<Relationships> owningEntityRelationshipsOps = wrapper.getRelationships()
        if (owningEntityRelationshipsOps.isPresent()) {
            List<AAIResourceUri> owningEntityRelatedAAIUris = owningEntityRelationshipsOps.get().getRelatedAAIUris(AAIObjectType.OWNING_ENTITY)

            if (!(owningEntityRelatedAAIUris == null || owningEntityRelatedAAIUris.isEmpty())) {
                Optional<org.onap.aai.domain.yang.OwningEntity> owningEntityOpt = client.get(org.onap.aai.domain.yang.OwningEntity.class, owningEntityRelatedAAIUris.get(0)) // Many-To-One relation
                if (owningEntityOpt.isPresent()) {
                    owningEntity.setOwningEntityId(owningEntityOpt.get().getOwningEntityId())
                    owningEntity.setOwningEntityName(owningEntityOpt.get().getOwningEntityName())

                }
            }
        }

        return owningEntity
    }


    /**
     * Prepares Project
     * @param execution
     * @return Project
     */
    Project prepareProject(DelegateExecution execution) {
        def currentNSSI = execution.getVariable("currentNSSI")

        AAIResourcesClient client = getAAIClient()

        Project project = new Project()

        AAIResourceUri cloudRegionRelatedAAIUri = (AAIResourceUri)currentNSSI['cloudRegionRelatedAAIUri']

        if (cloudRegionRelatedAAIUri != null) {
            AAIResultWrapper wrapper = client.get(cloudRegionRelatedAAIUri)
            Optional<Relationships> cloudRegionOps = wrapper.getRelationships()
            if (cloudRegionOps.isPresent()) {
                List<AAIResourceUri> projectAAIUris = cloudRegionOps.get().getRelatedAAIUris(AAIObjectType.PROJECT)
                if (!(projectAAIUris == null || projectAAIUris.isEmpty())) {
                    Optional<org.onap.aai.domain.yang.Project> projectOpt = client.get(org.onap.aai.domain.yang.Project.class, projectAAIUris.get(0))
                    if (projectOpt.isPresent()) {
                        project.setProjectName(projectOpt.get().getProjectName())
                    }
                }
            }
        }

        return project
    }


    /**
     * Prepares RequestDetails object
     * @param execution
     * @return
     */
    String prepareRequestDetails(DelegateExecution execution) {
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

            ObjectMapper mapper = new ObjectMapper()

            response = mapper.writeValueAsString(requestDetails)
        }
        catch (any) {
            String msg = "Exception in ${getPrefix()}.prepareRequestDetails. " + any.getCause()
            LOGGER.error(msg)

            response =  "{\n" +
                    " \"errorCode\": \"7000\",\n" +
                    " \"errorMessage\": \"${msg}\"\n" +
                    "}"

        }

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
     * Removes Slice Profile association with NSSI
     * @param execution
     */
    void removeSPAssociationWithNSSI(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start removeSPAssociationWithNSSI")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        ServiceInstance nssi = (ServiceInstance)currentNSSI['nssi']

        String nssiId = currentNSSI['nssiId']
        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId)

        List<SliceProfile> associatedProfiles = nssi.getSliceProfiles().getSliceProfile()

        String currentSNSSAI = currentNSSI['S-NSSAI']

        associatedProfiles.removeIf({ associatedProfile -> (associatedProfile.getSNssai().equals(currentSNSSAI)) })

        try {
            getAAIClient().update(nssiUri, nssi)
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while Slice Profile association with NSSI update call: " + e.getMessage())
        }

        LOGGER.trace("${getPrefix()} Exit removeSPAssociationWithNSSI")
    }


    /**
     * Deletes Slice Profile Instance
     * @param execution
     */
    void deleteSliceProfileInstance(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start deleteSliceProfileInstance")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        SliceProfile sliceProfileContainsSNSSAI = (SliceProfile)currentNSSI['sliceProfileS-NSSAI']

        String globalSubscriberId = currentNSSI['globalSubscriberId']
        String serviceType = currentNSSI['serviceType']
        String nssiId = currentNSSI['nssiId']

        // global-customer-id, service-type, service-instance-id, profile-id
        AAIResourceUri sliceProfileUri = AAIUriFactory.createResourceUri(AAIObjectType.SLICE_PROFILE, globalSubscriberId, serviceType, nssiId, sliceProfileContainsSNSSAI.getProfileId())

        try {
            client.delete(sliceProfileUri)
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while Slice Profile Instance delete call: " + e.getMessage())
        }

        LOGGER.trace("${getPrefix()} Exit deleteSliceProfileInstance")
    }


    /**
     * Prepares update resource operation status
     * @param execution
     */
    void prepareUpdateResourceOperationStatus(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start updateServiceOperationStatus")

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

        LOGGER.trace("${getPrefix()} Exit updateServiceOperationStatus")
    }


    /**
     * Prepares ResourceOperation status
     * @param execution
     * @param operationType
     */
    void setResourceOperationStatus(DelegateExecution execution, String status, String progress, String statusDesc) {
        LOGGER.trace("${getPrefix()} Start setResourceOperationStatus")

        def currentNSSI = execution.getVariable("currentNSSI")

        String serviceId = currentNSSI['nssiId']
        String jobId = execution.getVariable("jobId")
        String nsiId = currentNSSI['nsiId']
        String operationType = execution.getVariable("operationType")

        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus()
        resourceOperationStatus.setServiceId(serviceId)
        resourceOperationStatus.setOperationId(jobId)
        resourceOperationStatus.setResourceTemplateUUID(nsiId)
        resourceOperationStatus.setOperType(operationType)
        resourceOperationStatus.setStatus(status)
        resourceOperationStatus.setProgress(progress)
        resourceOperationStatus.setStatusDescription(statusDesc)
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, resourceOperationStatus)

        LOGGER.trace("${getPrefix()} Exit setResourceOperationStatus")
    }


    /**
     * Prepares failed operation status update
     * @param execution
     */
    void prepareFailedOperationStatusUpdate(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start prepareFailedOperationStatusUpdate")

        setResourceOperationStatus(execution, "failed", "0", "Core NSSI ${getAction()} Failed")

        LOGGER.trace("${getPrefix()} Exit prepareFailedOperationStatusUpdate")
    }


    /**
     * Gets progress status of ServiceInstance PUT operation
     * @param execution
     */
    public void getPUTServiceInstanceProgress(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start getPUTServiceInstanceProgress")

        def currentNSSI = execution.getVariable("currentNSSI")

        String url = currentNSSI['putServiceInstanceURL']

        getProgress(execution, url, "putStatus")

        LOGGER.trace("${getPrefix()} Exit getPUTServiceInstanceProgress")
    }


    void getProgress(DelegateExecution execution, String url, String statusVariableName) {
        String msg=""
        try {

            ExternalAPIUtil externalAPIUtil = getExternalAPIUtilFactory().create()
            Response response = externalAPIUtil.executeExternalAPIGetCall(execution, url)
            int responseCode = response.getStatus()
            execution.setVariable("GetServiceOrderResponseCode", responseCode)
            LOGGER.debug("Get ServiceOrder response code is: " + responseCode)

            String extApiResponse = response.readEntity(String.class)
            JSONObject responseObj = new JSONObject(extApiResponse)
            execution.setVariable("GetServiceOrderResponse", extApiResponse)
            LOGGER.debug("Create response body is: " + extApiResponse)
            //Process Response //200 OK 201 CREATED 202 ACCEPTED
            if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
            {
                LOGGER.debug("Get Create ServiceOrder Received a Good Response")
                String orderState = responseObj.get("state")
                if("REJECTED".equalsIgnoreCase(orderState)) {
                    prepareFailedOperationStatusUpdate(execution)
                    return
                }

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
                execution.setVariable("ServiceOrderState", serviceOrderState)
                // Get serviceOrder State and process progress
                if("ACKNOWLEDGED".equalsIgnoreCase(serviceOrderState)) {
                    execution.setVariable(statusVariableName, "processing")
                }
                else if("INPROGRESS".equalsIgnoreCase(serviceOrderState)) {
                    execution.setVariable(statusVariableName, "processing")
                }
                else if("COMPLETED".equalsIgnoreCase(serviceOrderState)) {
                    execution.setVariable(statusVariableName, "completed")
                }
                else if("FAILED".equalsIgnoreCase(serviceOrderState)) {
                    msg = "ServiceOrder failed"
                    exceptionUtil.buildAndThrowWorkflowException(execution, 7000,  msg)
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

    }



    /**
     * Delays 5 sec
     * @param execution
     */
    void timeDelay(DelegateExecution execution) {
        LOGGER.trace("${getPrefix()} Start timeDelay")

        try {
            LOGGER.debug("${getPrefix()} timeDelay going to sleep for 5 sec")

            Thread.sleep(5000)

            LOGGER.debug("${getPrefix()} ::: timeDelay wakeup after 5 sec")
        } catch(InterruptedException e) {
            LOGGER.error("${getPrefix()} ::: timeDelay exception" + e)
        }

        LOGGER.trace("${getPrefix()} Exit timeDelay")
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


    String getPrefix() {
        return PREFIX
    }

    String getAction() {
        return ""
    }
}
