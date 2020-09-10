package org.onap.so.bpmn.infrastructure.scripts

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.CloudRegion
import org.onap.aai.domain.yang.Customer
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.ModelVer
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aai.domain.yang.ServiceSubscription
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aai.domain.yang.Tenant
import org.onap.aai.domain.yang.VfModule
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel
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

class DoModifyCoreNSSI extends AbstractServiceTaskProcessor {

    private final String PREFIX ="DoModifyCoreNSSI"

    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private MsoUtils utils = new MsoUtils()
    private JsonUtils jsonUtil = new JsonUtils()

    private static final Logger LOGGER = LoggerFactory.getLogger( DoModifyCoreNSSI.class)

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
     * Queries Network Service Instance in AAI
     * @param execution
     */
    void getNetworkServiceInstance(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start getNetworkServiceInstance")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        String globalSubscriberId = currentNSSI['globalSubscriberId']
        String serviceType = currentNSSI['serviceType']
        String nssiId = currentNSSI['nssiServiceInstanceId']

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId) //AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, serviceType, nssiId)
        Optional<ServiceInstance> nssiOpt = client.get(ServiceInstance.class, nssiUri)

        if (nssiOpt.isPresent()) {
            ServiceInstance nssi = nssiOpt.get()
            execution.setVariable("nssi", nssi)

            execution.setVariable("nssiUri", nssiUrl)

            // Network Service Instance
            AAIResultWrapper wrapper = client.get(nssiUri);
            Optional<Relationships> relationships = wrapper.getRelationships()
            if (relationships.isPresent()) {
                for(AAIResourceUri networkServiceInstanceUri: relationships.get().getRelatedAAIUris(AAIObjectType.SERVICE_INSTANCE)){ // ???
                    Optional<ServiceInstance> networkServiceInstanceOpt = client.get(ServiceInstance.class, networkServiceInstanceUri)
                    if(networkServiceInstanceOpt.isPresent()) {
                        ServiceInstance networkServiceInstance = networkServiceInstanceOpt.get()

                        if(networkServiceInstance.getServiceRole().equals("Network Service")) { // Network Service
                            execution.setVariable("networkServiceInstance", networkServiceInstance)

                            execution.setVariable("networkServiceInstanceUri", networkServiceInstanceUri)
                            break // Should be only one Network Service Instance
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
        }
        else {
            String msg = String.format("NSSI %s not found in AAI", nssiId)
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
        }


        LOGGER.trace("***** ${PREFIX} Exit getNetworkServiceInstance")
    }


    /**
     * Queries constitute VNF from Network Service Instance
     * @param execution
     */
    void getConstituteVNFFromNetworkServiceInst(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start getConstituteVNFFromNetworkServiceInst")

        AAIResourcesClient client = getAAIClient()

        AAIResourceUri networkServiceInstanceUri = (AAIResourceUri)execution.getVariable("networkServiceInstanceUri")
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
                    String msg = String.format("No constitute VNF found for Network Service Instance %s in AAI", ((ServiceInstance)execution.getVariable("networkServiceInstance")).getServiceInstanceId())
                    LOGGER.error(msg)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
                }

                execution.setVariable("networkServiceInstanceUri", networkServiceInstanceUri)
                break  // Should be only one constitute VNF
            }
        }
        else {
            String msg = String.format("No relationship presented for Network Service Instance %s in AAI", ((ServiceInstance)execution.getVariable("networkServiceInstance")).getServiceInstanceId())
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

        AAIResourcesClient client = getAAIClient()

        ServiceInstance nssi = (ServiceInstance)execution.getVariable("nssi")

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

        if((Boolean)execution.getVariable("isCreateSliceProfileInstance" ).equals(Boolean.TRUE)) { // Slice Profile Instance has to be created
            for (SliceProfile associatedProfile : associatedProfiles) {
                snssais.add(associatedProfile.getSNssai())
            }

            snssais.add(currentSNSSAI)
        }
        else { // Slice profile instance has to be deleted
            for (SliceProfile associatedProfile : associatedProfiles) {
                if (!associatedProfile.getSNssai().equals(currentNSSI)) { // not current S-NSSAI
                    snssais.add(associatedProfile.getSNssai())
                }
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

        try {
            //url:/onap/so/infra/serviceInstantiation/v7/serviceInstances/{serviceInstanceId}/vnfs/{vnfId}"
            def nsmfЕndpoint = UrnPropertiesReader.getVariable("mso.infra.endpoint.url", execution) // ???

            ServiceInstance networkServiceInstance = (ServiceInstance)execution.getVariable("networkServiceInstance")

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

        ServiceInstance networkServiceInstance = (ServiceInstance)execution.getVariable("networkServiceInstance")


        AAIResourcesClient client = getAAIClient()

        // Model Info
        requestDetails.setModelInfo(prepareModelInfo(execution))

        // Subscriber Info
        SubscriberInfo subscriberInfo = new SubscriberInfo()
        subscriberInfo.setGlobalSubscriberId(globalSubscriberId)

        Customer customer = null
        ServiceSubscription serviceSubscription = null

        AAIResourceUri networkServiceInstanceUri = execution.getVariable("networkServiceInstanceUri")
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
     * Creates Slice Profile Instance
     * @param execution
     */
    void createSliceProfileInstance(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start createSliceProfileInstance")

        String sliceProfileID = execution.getVariable("sliceProfileID")
        Map<String, Object> sliceProfileMap = execution.getVariable("sliceProfileCn")
        Map<String, Object> serviceProfileMap = execution.getVariable("serviceProfile")

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setServiceAreaDimension("")
        sliceProfile.setPayloadSize(0)
        sliceProfile.setJitter(0)
        sliceProfile.setSurvivalTime(0)
        sliceProfile.setExpDataRate(0)
        sliceProfile.setTrafficDensity(0)
        sliceProfile.setConnDensity(0)
        sliceProfile.setSNssai(sliceProfileMap.get("sNSSAI").toString())
        sliceProfile.setExpDataRateUL(Integer.parseInt(sliceProfileMap.get("expDataRateUL").toString()))
        sliceProfile.setExpDataRateDL(Integer.parseInt(sliceProfileMap.get("expDataRateDL").toString()))
        sliceProfile.setActivityFactor(Integer.parseInt(sliceProfileMap.get("activityFactor").toString()))
        sliceProfile.setResourceSharingLevel(sliceProfileMap.get("activityFactor").toString())
        sliceProfile.setUeMobilityLevel(serviceProfileMap.get("uEMobilityLevel").toString())
        sliceProfile.setCoverageAreaTAList(serviceProfileMap.get("coverageAreaTAList").toString())
        sliceProfile.setMaxNumberOfUEs(Integer.parseInt(sliceProfileMap.get("activityFactor").toString()))
        sliceProfile.setLatency(Integer.parseInt(sliceProfileMap.get("latency").toString()))
        sliceProfile.setProfileId(sliceProfileID)
        sliceProfile.setE2ELatency(0)

        try {
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SLICE_PROFILE, sliceProfileID)
            client.create(uri, sliceProfile)

            execution.setVariable("createdSliceProfile", sliceProfile)
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while Slice Profile create call:" + ex.getMessage())
        }

        LOGGER.trace("${PREFIX} Exit createSliceProfileInstance")
    }


    /**
     * Creates Slice Profile association with NSSI
     * @param execution
     */
    void associateSliceProfileInstanceWithNSSI(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start associateSliceProfileInstanceWithNSSI")

        String sliceProfileID = execution.getVariable("sliceProfileID")

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiServiceInstanceId']

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId)
        AAIResourceUri sliceProfileUri = AAIUriFactory.createResourceUri(AAIObjectType.SLICE_PROFILE, sliceProfileID)

        try {
            SliceProfile createdSliceProfile = (SliceProfile)execution.getVariable("createdSliceProfile")
            ServiceInstance nssi = (ServiceInstance)execution.getVariable("nssi")
            List<SliceProfile> associatedProfiles = nssi.getSliceProfiles().getSliceProfile()
            associatedProfiles.add(createdSliceProfile)

            getAAIClient().update(nssiUri, nssi)

            getAAIClient().connect(sliceProfileUri, nsiUri, AAIEdgeLabel.BELONGS_TO)
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while Slice Profile association with NSSI disconnect call: " + e.getMessage())
        }

        LOGGER.trace("${PREFIX} Exit associateSliceProfileInstanceWithNSSI")
    }


    /**
    * Removes Slice Profile association with NSSI
    * @param execution
    */
    void removeSPAssociationWithNSSI(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start removeSPAssociationWithNSSI")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        ServiceInstance nssi = (ServiceInstance)execution.getVariable("nssi")

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

        ServiceInstance nssi = (ServiceInstance)execution.getVariable("nssi")

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
