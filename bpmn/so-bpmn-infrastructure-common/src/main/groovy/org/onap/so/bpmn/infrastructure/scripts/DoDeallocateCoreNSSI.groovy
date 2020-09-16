package org.onap.so.bpmn.infrastructure.scripts

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.CloudRegion
import org.onap.aai.domain.yang.Customer
import org.onap.aai.domain.yang.ModelVer
import org.onap.aai.domain.yang.OwningEntities
import org.onap.aai.domain.yang.ServiceSubscription
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aai.domain.yang.Tenant
import org.onap.aai.domain.yang.VfModule
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.db.request.beans.OperationStatus
import org.onap.so.requestsdb.RequestsDbConstant
import org.onap.so.serviceinstancebeans.CloudConfiguration
import org.onap.so.serviceinstancebeans.ModelInfo
import org.onap.so.serviceinstancebeans.ModelType
import org.onap.so.serviceinstancebeans.OwningEntity
import org.onap.so.serviceinstancebeans.Project
import org.onap.so.serviceinstancebeans.RequestDetails
import org.onap.so.serviceinstancebeans.RequestInfo
import org.onap.so.serviceinstancebeans.RequestParameters
import org.onap.so.serviceinstancebeans.Resources
import org.onap.so.serviceinstancebeans.Service
import org.onap.so.serviceinstancebeans.SubscriberInfo
import org.onap.so.serviceinstancebeans.VfModules
import org.onap.so.serviceinstancebeans.Vnfs
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.core.Response

class DoDeallocateCoreNSSI extends AbstractServiceTaskProcessor {
    private final String PREFIX ="DoDeallocateCoreNSSI"

    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private MsoUtils utils = new MsoUtils()
    private JsonUtils jsonUtil = new JsonUtils()

    private static final Logger LOGGER = LoggerFactory.getLogger( DoDeallocateCoreNSSI.class)

    @Override
    void preProcessRequest(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start preProcessRequest")

        def currentNSSI = execution.getVariable("currentNSSI")
        if (!currentNSSI) {
            String msg = "currentNSSI is null"
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }

        LOGGER.trace("***** ${PREFIX} Exit preProcessRequest")
    }


    /**
     * Queries OOF for NSSI termination
     * @param execution
     */
    void executeTerminateNSSIQuery(DelegateExecution execution) {
        // TO DO: Unit test
        LOGGER.trace("${PREFIX} Start executeTerminateNSSIQuery")

        def currentNSSI = execution.getVariable("currentNSSI")

        String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)

        //Prepare auth for OOF
        def authHeader = ""
        String basicAuth = UrnPropertiesReader.getVariable("mso.oof.auth", execution)
        String msokey = UrnPropertiesReader.getVariable("mso.msoKey", execution)

        String basicAuthValue = utils.encrypt(basicAuth, msokey)
        if (basicAuthValue != null) {
            logger.debug( "Obtained BasicAuth username and password for OOF: " + basicAuthValue)
            try {
                authHeader = utils.getBasicAuth(basicAuthValue, msokey)
                execution.setVariable("BasicAuthHeaderValue", authHeader)
            } catch (Exception ex) {
                logger.debug( "Unable to encode username and password string: " + ex)
                exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - Unable to " +
                        "encode username and password string")
            }
        } else {
            logger.debug( "Unable to obtain BasicAuth - BasicAuth value null")
            exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - BasicAuth " +
                    "value null")
        }

        //Prepare send request to OOF
        String oofRequest = buildOOFRequest(execution)

        URL url = new URL(urlString+"/api/oof/terminate/nxi/v1")
        HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.OOF)
        httpClient.addAdditionalHeader("Authorization", authHeader)
        httpClient.addAdditionalHeader("Accept", "application/json")
        httpClient.addAdditionalHeader("Content-Type", "application/json")

        Response httpResponse = httpClient.post(oofRequest)

        int responseCode = httpResponse.getStatus()
        logger.debug("OOF sync response code is: " + responseCode)

        if(responseCode != 202){ // Accepted
            exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from OOF.")
        }

        if(httpResponse.hasEntity()){
            String OOFResponse = httpResponse.readEntity(Boolean.class)
            String isTerminateNSSI = jsonUtil.getJsonValue(OOFResponse, "terminateResponse")

            execution.setVariable("isTerminateNSSI", Boolean.parseBoolean(isTerminateNSSI))
        }

        LOGGER.trace("${PREFIX} Exit executeTerminateNSSIQuery")
    }


    /**
     * Builds OOF request
     * @param execution
     * @return
     */
    private String buildOOFRequest(DelegateExecution execution) {

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiId']
        String requestId = execution.getVariable("mso-request-id")

        String request =    "{\n" +
                            "  \"type\": \"NSSI\",\n" +
                            "  \"NxIId\": \"${nssiId}\",\n" +
                            "  \"requestInfo\": {\n" +
                            "    \"transactionId\": \"${requestId}\",\n" +
                            "    \"requestId\": \"${requestId}\",\n" +
                            "    \"sourceId\": \"so\",\n" +
                            "    }\n" +
                            "}"

        return request
    }



    /**
     * Queries Network Service Instance in AAI
     * @param execution
     */
    void getNetworkServiceInstance(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start getNetworkServiceInstance")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        String globalSubscriberId = currentNSSI['globalSubscriberId']
        String serviceType = currentNSSI['serviceType']
        String nssiId = currentNSSI['nssiId']

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId) //AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, serviceType, nssiId)
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

        LOGGER.trace("${PREFIX} Exit getNetworkServiceInstance")
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

                    if (networkServiceInstance.getServiceRole().equals("Network Service")) { // Network Service role
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
     * Invokes deleteServiceOrder external API
     * @param execution
     */
    void deleteServiceOrder(DelegateExecution execution) {
        // TO DO: Unit test
        LOGGER.trace("${PREFIX} Start deleteServiceOrder")

        def currentNSSI = execution.getVariable("currentNSSI")

        try {
            //url:/nbi/api/v4/serviceOrder/"
            def nbiEndpointUrl = UrnPropertiesReader.getVariable("nbi.endpoint.url", execution) // ???

            ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']

            String url = String.format("${nbiEndpointUrl}/api/v4/serviceOrder/%s", networkServiceInstance.getServiceInstanceId()) // Service Order ID = Network Service Instance ID ???

            String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)
            String basicAuth =  UrnPropertiesReader.getVariable("mso.infra.endpoint.auth", execution)
            String basicAuthValue = utils.encrypt(basicAuth, msoKey)
            String encodeString = utils.getBasicAuth(basicAuthValue, msoKey)

            HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.EXTERNAL)
            httpClient.addAdditionalHeader("Authorization", encodeString)
            httpClient.addAdditionalHeader("Accept", "application/json")
            Response httpResponse = httpClient.delete() // check http code ???
        } catch (any) {
            String msg = "Exception in DoDeallocateCoreNSSI.deleteServiceOrder. " + any.getCause()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        LOGGER.trace("${PREFIX} Exit deleteServiceOrder")
    }


    /**
     * Queries constitute VNF from Network Service Instance
     * @param execution
     */
    void getConstituteVNFFromNetworkServiceInst(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start getConstituteVNFFromNetworkServiceInst")

        def currentNSSI = execution.getVariable("currentNSSI")

        AAIResourcesClient client = getAAIClient()

        AAIResourceUri networkServiceInstanceUri = (AAIResourceUri)currentNSSI['networkServiceInstanceUri']
        AAIResultWrapper wrapper = client.get(networkServiceInstanceUri);
        Optional<Relationships> relationships = wrapper.getRelationships()
        if (relationships.isPresent()) {
            for (AAIResourceUri constituteVnfUri : relationships.get().getRelatedAAIUris(AAIObjectType.GENERIC_VNF)) {  // ???
                execution.setVariable("constituteVnfUri", constituteVnfUri)
                Optional<GenericVnf> constituteVnfOpt = client.get(GenericVnf.class, constituteVnfUri)
                if(constituteVnfOpt.isPresent()) {
                    GenericVnf constituteVnf = constituteVnfOpt.get()
                    execution.setVariable("constituteVnf", constituteVnf)
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

        LOGGER.trace("${PREFIX} Exit getConstituteVNFFromNetworkServiceInst")

    }


    /**
     * Retrieves NSSI associated profiles from AAI
     * @param execution
     */
    void getNSSIAssociatedProfiles(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start getNSSIAssociatedProfiles")

        def currentNSSI = execution.getVariable("currentNSSI")

        ServiceInstance nssi = (ServiceInstance)currentNSSI['nssi']

        List<SliceProfile> associatedProfiles = nssi.getSliceProfiles().getSliceProfile()

        if(associatedProfiles.isEmpty()) {
            String msg = String.format("No associated profiles found for NSSI %s in AAI", nssi.getServiceInstanceId())
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
        }
        else {
            execution.setVariable("associatedProfiles", associatedProfiles)
        }

        LOGGER.trace("${PREFIX} Exit getNSSIAssociatedProfiles")
    }


    /**
     * Calculates a final list of S-NSSAI
     * @param execution
     */
    void calculateSNSSAI(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start calculateSNSSAI")

        List<SliceProfile> associatedProfiles = (List<SliceProfile>)execution.getVariable("associatedProfiles")

        def currentNSSI = execution.getVariable("currentNSSI")

        String currentSNSSAI = currentNSSI['S-NSSAI']

        List<String> snssais = new ArrayList<>()

        for(SliceProfile associatedProfile:associatedProfiles) {
            if(!associatedProfile.getSNssai().equals(currentNSSI)) { // not current S-NSSAI
                snssais.add(associatedProfile.getSNssai())
            }
        }

        execution.setVariable("S-NSSAIs", snssais)

        LOGGER.trace("${PREFIX} Exit calculateSNSSAI")
    }


    /**
     * Invoke PUT Service Instance API
     * @param execution
     */
    void invokePUTServiceInstance(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start invokePUTServiceInstance")

        def currentNSSI = execution.getVariable("currentNSSI")

        try {
            //url:/onap/so/infra/serviceInstantiation/v7/serviceInstances/{serviceInstanceId}/vnfs/{vnfId}"
            def nsmfЕndpoint = UrnPropertiesReader.getVariable("mso.infra.endpoint.url", execution) // ???

            ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']

            GenericVnf constituteVnf = (GenericVnf)execution.getVariable("constituteVnf")

            String url = String.format("${nsmfЕndpoint}/serviceInstantiation/v7/serviceInstances/%s/vnfs/%s", networkServiceInstance.getServiceInstanceId(), constituteVnf.getVnfId()) // ???

            String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)
            String basicAuth =  UrnPropertiesReader.getVariable("mso.infra.endpoint.auth", execution)
            String basicAuthValue = utils.encrypt(basicAuth, msoKey)
            String encodeString = utils.getBasicAuth(basicAuthValue, msoKey)

            HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.EXTERNAL)
            httpClient.addAdditionalHeader("Authorization", encodeString)
            httpClient.addAdditionalHeader("Accept", "application/json")

            RequestDetails requestDetails = prepareRequestDetails(execution)
            ObjectMapper mapper = new ObjectMapper()
            String requestDetailsStr = mapper.writeValueAsString(requestDetails)

            Response httpResponse = httpClient.put(requestDetailsStr) // check http code ???
        } catch (any) {
            String msg = "Exception in DoDeallocateCoreNSSI.deleteServiceOrder. " + any.getCause()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        LOGGER.trace("${PREFIX} Exit invokePUTServiceInstance")
    }


    /**
     * Prepare model info
     * @param execution
     * @param requestDetails
     * @return
     */
    private ModelInfo prepareModelInfo(DelegateExecution execution) {
        ModelInfo modelInfo = new ModelInfo()

        modelInfo.setModelType(ModelType.service)
        modelInfo.setModelInvariantId(networkServiceInstance.getModelInvariantId())

        AAIResourceUri modelVerUrl = AAIUriFactory.createResourceUri(AAIObjectType.MODEL_VER, networkServiceInstance.getModelInvariantId()) // model of Network Service Instance ???
        Optional<ModelVer> modelVerOpt = client.get(ModelVer.class, modelVerUrl)

        if (modelVerOpt.isPresent()) {
            modelInfo.setModelVersionId(modelVerOpt.get().getModelVersionId())
            modelInfo.setModelName(modelVerOpt.get().getModelName())
            modelInfo.setModelVersion(modelVerOpt.get().getModelVersion())
        }


        return modelInfo
    }


    /**
     * Prepares RequestDetails object
     * @param execution
     * @return
     */
    private RequestDetails prepareRequestDetails(DelegateExecution execution) {
        RequestDetails requestDetails = new RequestDetails()

        def currentNSSI = execution.getVariable("currentNSSI")

        String globalSubscriberId = currentNSSI['globalSubscriberId']

        ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']


        AAIResourcesClient client = getAAIClient()

        // Model Info
        requestDetails.setModelInfo(prepareModelInfo(execution))

        // Subscriber Info
        SubscriberInfo subscriberInfo = new SubscriberInfo()
        subscriberInfo.setGlobalSubscriberId(globalSubscriberId)

        Customer customer = null
        ServiceSubscription serviceSubscription = null

        AAIResourceUri networkServiceInstanceUri = currentNSSI['networkServiceInstanceUri']
        AAIResultWrapper wrapper = client.get(networkServiceInstanceUri)
        Optional<Relationships> serviceSubscriptionRelationshipsOps = wrapper.getRelationships()
        if(serviceSubscriptionRelationshipsOps.isPresent()) {
            List<AAIResourceUri> serviceSubscriptionRelatedAAIUris = serviceSubscriptionRelationshipsOps.get().getRelatedAAIUris(AAIObjectType.SERVICE_SUBSCRIPTION)
            if(!(serviceSubscriptionRelatedAAIUris == null || serviceSubscriptionRelatedAAIUris.isEmpty())) {
                AAIResourceUri serviceSubscriptionUri = serviceSubscriptionRelatedAAIUris.get(0) // Many-To-One relation
                Optional<ServiceSubscription> serviceSubscriptionOpt = client.get(ServiceSubscription.class, serviceSubscriptionUri)
                if(serviceSubscriptionOpt.isPresent()) {
                    serviceSubscription = serviceSubscriptionOpt.get()
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

        }
        requestDetails.setSubscriberInfo(subscriberInfo)

        // Request Info
        RequestInfo requestInfo = new RequestInfo()
        requestInfo.setInstanceName(networkServiceInstance.getServiceInstanceName())

        /* No found data to provide ???
        requestInfo.setSource()
        requestInfo.setSuppressRollback()
        requestInfo.setRequestorId()
        requestInfo.setProductFamilyId()
        */

        requestDetails.setRequestInfo(requestInfo)


        // Request Parameters
        RequestParameters requestParameters = new RequestParameters()

        // No found data to provide ??? requestParameters.setaLaCarte()
        requestParameters.setSubscriptionServiceType(serviceSubscription.getServiceType())

        // User params
        List<Map<String, Object>> userParams = new ArrayList<>()
        // Service
        Service service = new Service()
        // Model Info
        ModelInfo serviceModelInfo = new ModelInfo()
        serviceModelInfo.setModelType(ModelType.service)
        serviceModelInfo.setModelInvariantId(networkServiceInstance.getModelInvariantId())

        serviceModelInfo.setModelVersionId(modelInfo.get().getModelVersionId())
        serviceModelInfo.setModelName(modelInfo.get().getModelName())
        serviceModelInfo.setModelVersion(modelInfo.get().getModelVersion())

        service.setModelInfo(serviceModelInfo)

        // Resources
        Resources resources = new Resources()

        CloudRegion cloudRegion = null
        AAIResourceUri cloudRegionRelatedAAIUri = null
        // VNFs
        List<Vnfs> vnfs = new ArrayList<>()
        // VNF
        Vnfs vnf = new Vnfs()

        // Cloud configuration
        CloudConfiguration cloudConfiguration = new CloudConfiguration()

        AAIResourceUri constituteVnfUri = (AAIResourceUri)execution.getVariable("constituteVnfUri")
        wrapper = client.get(constituteVnfUri)
        Optional<Relationships> constituteVnfOps = wrapper.getRelationships()
        if(constituteVnfOps.isPresent()) {
            List<AAIResourceUri> cloudRegionRelatedAAIUris = serviceSubscriptionRelationshipsOps.get().getRelatedAAIUris(AAIObjectType.CLOUD_REGION)
            if(!(cloudRegionRelatedAAIUris == null || cloudRegionRelatedAAIUris.isEmpty())) {
                cloudRegionRelatedAAIUri = cloudRegionRelatedAAIUris.get(0)
                Optional<CloudRegion> cloudRegionrOpt = client.get(CloudRegion.class, cloudRegionRelatedAAIUris.get(0))
                if(cloudRegionrOpt.isPresent()) {
                    cloudRegion = cloudRegionrOpt.get()
                    cloudConfiguration.setLcpCloudRegionId(cloudRegion.getCloudRegionId())
                    for(Tenant tenant:cloudRegion.getTenants()) {
                        cloudConfiguration.setTenantId(tenant.getTenantId())
                        break // only one is required
                    }

                    cloudConfiguration.setCloudOwner(cloudRegion.getCloudOwner())
                }
            }
        }

        vnf.setCloudConfiguration(cloudConfiguration)

        // VF Modules
        GenericVnf constituteVnf = execution.getVariable("constituteVnf")
        List<VfModules> vfModuless = new ArrayList<>()
        for(VfModule vfModule:constituteVnf.getVfModules()) {
            VfModules vfmodules = new VfModules()

            ModelInfo vfModuleModelInfo = new ModelInfo()
            vfModuleModelInfo.setModelInvariantUuid(vfModule.getModelInvariantId())

            AAIResourceUri vfModuleUrl = AAIUriFactory.createResourceUri(AAIObjectType.MODEL_VER, vfModule.getModelInvariantId()) // ???
            Optional<ModelVer> vfModuleModelVerOpt = client.get(ModelVer.class, vfModuleUrl)

            if (vfModuleModelVerOpt.isPresent()) {
                vfModuleModelInfo.setModelVersionId(vfModuleModelVerOpt.get().getModelVersionId())
                vfModuleModelInfo.setModelName(vfModuleModelVerOpt.get().getModelName())
                vfModuleModelInfo.setModelVersion(vfModuleModelVerOpt.get().getModelVersion())

                // No model customization ID
            }
            vfmodules.setModelInfo(vfModuleModelInfo)

            vfmodules.setInstanceName(vfModule.getVfModuleName()) // ???

            vfModuless.add(vfmodules)
        }
        vnf.setVfModules(vfModuless)

        // Model Info
        ModelInfo vnfModelInfo = new ModelInfo()
        vnfModelInfo.setModelInvariantUuid(constituteVnf.getModelInvariantId())
        AAIResourceUri vnfModelUrl = AAIUriFactory.createResourceUri(AAIObjectType.MODEL_VER, constituteVnf.getModelInvariantId()) // ???
        Optional<ModelVer> vnfModelVerOpt = client.get(ModelVer.class, vnfModelUrl)

        if (vnfModelVerOpt.isPresent()) {
            vnfModelInfo.setModelVersionId(vnfModelVerOpt.get().getModelVersionId())
            vnfModelInfo.setModelName(vnfModelVerOpt.get().getModelName())
            vnfModelInfo.setModelVersion(vnfModelVerOpt.get().getModelVersion())

            // No model customization ID
            // No model instance name
        }

        vnf.setModelInfo(vnfModelInfo)

        // Instance name
        vnf.setInstanceName(constituteVnf.getVnfInstanceId())

        // Instance params
        List<Map<String, Object>> instanceParams = new ArrayList<>()
        Map<String, Object> supporrtedNSSAIMap = new HashMap<>()

        // Supported S-NSSAI
        List<String> snssais = ( List<String>)execution.getVariable("S-NSSAIs")
        supporrtedNSSAIMap.put("supporrtedNSSAI", snssais) // remaining S-NSSAIs ??? there is no status for each s-nssai
        instanceParams.add(supporrtedNSSAIMap)

        // No other instance params, e.g. config-type

        vnf.setInstanceParams(instanceParams)

        // No platform data

        vnfs.add(vnf)
        resources.setVnfs(vnfs)

        service.setResources(resources)

        Map<String, Object> serviceMap = new HashMap<>()
        serviceMap.put("service", service)
        userParams.add(serviceMap)
        requestParameters.setUserParams(userParams)

        // No other user params

        requestDetails.setRequestParameters(requestParameters)

        // No other request params

        // Cloud configuration
        requestDetails.setCloudConfiguration(cloudConfiguration)

        // Owning entity
        OwningEntity owningEntity = new OwningEntity()
        wrapper = client.get(networkServiceInstanceUri)
        Optional<Relationships> owningEntityRelationshipsOps = wrapper.getRelationships()
        if(owningEntityRelationshipsOps.isPresent()) {
            List<AAIResourceUri> owningEntityRelatedAAIUris = owningEntityRelationshipsOps.get().getRelatedAAIUris(AAIObjectType.OWNING_ENTITY)

            if(!(owningEntityRelatedAAIUris == null || owningEntityRelatedAAIUris.isEmpty())) {
                Optional<org.onap.aai.domain.yang.OwningEntity> owningEntityOpt = client.get(org.onap.aai.domain.yang.OwningEntity.class, owningEntityRelatedAAIUris.get(0)) // Many-To-One relation
                if(owningEntityOpt.isPresent()) {
                    owningEntity.setOwningEntityId(owningEntityOpt.get().getOwningEntityId())
                    owningEntity.setOwningEntityName(owningEntityOpt.get().getOwningEntityName())
                    requestDetails.setOwningEntity(owningEntity)
                }
            }
        }

        // Project
        Project project = new Project()
        if(cloudRegionRelatedAAIUri != null) {
            wrapper = client.get(cloudRegionRelatedAAIUri)
            Optional<Relationships> cloudRegionOps = wrapper.getRelationships()
            if(cloudRegionOps.isPresent()) {
                List<AAIResourceUri> projectAAIUris = cloudRegionOps.get().getRelatedAAIUris(AAIObjectType.PROJECT)
                if (!(projectAAIUris == null || projectAAIUris.isEmpty())) {
                    Optional<org.onap.aai.domain.yang.Project> projectOpt = client.get(org.onap.aai.domain.yang.Project.class, projectAAIUris.get(0))
                    if(projectOpt.isPresent()) {
                        project.setProjectName(projectOpt.get().getProjectName())
                    }
                }
            }
        }
        requestDetails.setProject(project)

        return requestDetails
    }


    /**
     * Removes NSSI association with NSI
     * @param execution
     */
    void removeNSSIAssociationWithNSI(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start removeNSSIAssociationWithNSI")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiServiceInstanceId']
        String nsiId = currentNSSI['nsiId']

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId)
        AAIResourceUri nsiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nsiId)

        try {
            getAAIClient().disconnect(nssiUri, nsiUri)
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while NSSI association with NSI disconnect call: " + e.getMessage())
        }

        LOGGER.trace("${PREFIX} Exit removeNSSIAssociationWithNSI")
    }


    /**
     * Removes Slice Profile association with NSSI
     * @param execution
     */
    void removeSPAssociationWithNSSI(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start removeSPAssociationWithNSSI")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        ServiceInstance nssi = (ServiceInstance)currentNSSI['nssi']

        String nssiId = currentNSSI['nssiServiceInstanceId']
        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId)

        List<SliceProfile> associatedProfiles = nssi.getSliceProfiles().getSliceProfile()

        String currentSNSSAI = currentNSSI['S-NSSAI']

        associatedProfiles.removeIf({ associatedProfile -> (associatedProfile.getSNssai().equals(currentSNSSAI)) })

        try {
            getAAIClient().update(nssiUri, nssi)
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while Slice Profile association with NSSI update call: " + e.getMessage())
        }

        LOGGER.trace("${PREFIX} Exit removeSPAssociationWithNSSI")
    }


    /**
     * Deletes Slice Profile Instance
     * @param execution
     */
    void deleteSliceProfileInstance(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start deleteSliceProfileInstance")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        ServiceInstance nssi = (ServiceInstance)currentNSSI['nssi']

        List<SliceProfile> associatedProfiles = nssi.getSliceProfiles().getSliceProfile()

        String currentSNSSAI = currentNSSI['S-NSSAI']

        AAIResourceUri sliceProfileUri = null

        for(SliceProfile associatedProfile:associatedProfiles) {
            if(!associatedProfile.getSNssai().equals(currentNSSI)) { // not current S-NSSAI
                sliceProfileUri = AAIUriFactory.createResourceUri(AAIObjectType.SLICE_PROFILE, associatedProfile.getProfileId())
                break
            }
        }

        try {
            getAAIClient().delete(sliceProfileUri)
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while Slice Profile Instance delete call: " + e.getMessage())
        }

        LOGGER.trace("${PREFIX} Exit deleteSliceProfileInstance")
    }


    /**
     * Delets NSSI Service Instance
     * @param execution
     */
    void deleteNSSIServiceInstance(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start deleteNSSIServiceInstance")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiServiceInstanceId']
        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId)

        try {
            getAAIClient().delete(nssiUri)
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while NSSI Service Instance delete call: " + e.getMessage())
        }

        LOGGER.trace("${PREFIX} Exit deleteNSSIServiceInstance")
    }


    /**
     * Updates operation status
     * @param execution
     */
    void updateServiceOperationStatus(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start updateServiceOperationStatus")

        def currentNSSI = execution.getVariable("currentNSSI")

        OperationStatus operationStatus = new OperationStatus()
        operationStatus.setServiceId(currentNSSI['e2eServiceInstanceId'] as String)
        operationStatus.setOperationId(currentNSSI['operationId'] as String)
        operationStatus.setOperation(currentNSSI['operationType'] as String)
        operationStatus.setResult(RequestsDbConstant.Status.FINISHED)

        requestDBUtil.prepareUpdateOperationStatus(execution, operationStatus)

        LOGGER.trace("${PREFIX} Exit updateServiceOperationStatus")
    }


    /**
     * Returns AAI client
     * @return AAI client
     */
    AAIResourcesClient getAAIClient() {
        return new AAIResourcesClient()
    }

}
