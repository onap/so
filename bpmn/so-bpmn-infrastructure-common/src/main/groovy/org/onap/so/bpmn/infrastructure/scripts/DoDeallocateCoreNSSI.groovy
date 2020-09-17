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
import org.onap.aaiclient.client.aai.AAIClient
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
import org.onap.so.serviceinstancebeans.LineOfBusiness
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

        String basicAuthValue = encryptBasicAuth(basicAuth, msokey)
        if (basicAuthValue != null) {
            String responseAuthHeader = getAuthHeader(execution, basicAuthValue, msokey)
            String errorCode = jsonUtil.getJsonValue(responseAuthHeader, "errorCode")
            if(errorCode == null || errorCode.isEmpty()) { // No error
                authHeader = responseAuthHeader
            }
            else {
                exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(responseAuthHeader, "errorMessage"))
            }
        } else {
            LOGGER.error( "Unable to obtain BasicAuth - BasicAuth value null")
            exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - BasicAuth " +
                    "value null")
        }

        //Prepare send request to OOF
        String oofRequest = buildOOFRequest(execution)

        String callOOFResponse = callOOF(urlString, authHeader, oofRequest)
        String errorCode = jsonUtil.getJsonValue(callOOFResponse, "errorCode")
        if(errorCode == null || errorCode.isEmpty()) { // No error
            String oofResponse = callOOFResponse
            String isTerminateNSSI = jsonUtil.getJsonValue(oofResponse, "terminateResponse")

            execution.setVariable("isTerminateNSSI", Boolean.parseBoolean(isTerminateNSSI))
        }
        else {
            LOGGER.error(jsonUtil.getJsonValue(callOOFResponse, "errorMessage"))
            exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(callOOFResponse, "errorMessage"))
        }


        LOGGER.trace("${PREFIX} Exit executeTerminateNSSIQuery")
    }


    /**
     * Executes sync call to OOF
     * @return OOF response
     */
    String callOOF(String urlString, String authHeader, String oofRequest) {
        String errorCode = ""
        String errorMessage = ""
        String response = ""

        try {
            URL url = new URL(urlString + "/api/oof/terminate/nxi/v1")
            HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.OOF)
            httpClient.addAdditionalHeader("Authorization", authHeader)
            httpClient.addAdditionalHeader("Accept", "application/json")
            httpClient.addAdditionalHeader("Content-Type", "application/json")

            Response httpResponse = httpClient.post(oofRequest)

            int responseCode = httpResponse.getStatus()
            LOGGER.debug("OOF sync response code is: " + responseCode)

            if (responseCode != 202) { // Accepted
                errorCode = responseCode
                errorMessage = "Received a Bad Sync Response from OOF."

                response =  "{\n" +
                        " \"errorCode\": \"${errorCode}\",\n" +
                        " \"errorMessage\": \"${errorMessage}\"\n" +
                        "}"
                //exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from OOF.")
            }

            if (httpResponse.hasEntity()) {
                response = httpResponse.readEntity(String.class)
            }
            else {
                errorCode = 500
                errorMessage = "No response received from OOF."

                response =  "{\n" +
                        " \"errorCode\": \"${errorCode}\",\n" +
                        " \"errorMessage\": \"${errorMessage}\"\n" +
                        "}"
            }
        }
        catch(Exception e) {
            errorCode = 400
            errorMessage = e.getMessage()

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
        LOGGER.trace("${PREFIX} Start deleteServiceOrder")

        def currentNSSI = execution.getVariable("currentNSSI")

        try {
            //url:/nbi/api/v4/serviceOrder/"
            def nbiEndpointUrl = UrnPropertiesReader.getVariable("nbi.endpoint.url", execution)

            ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']

            String url = String.format("${nbiEndpointUrl}/api/v4/serviceOrder/%s", networkServiceInstance.getServiceInstanceId()) // Service Order ID = Network Service Instance ID

            String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)
            String basicAuth =  UrnPropertiesReader.getVariable("mso.infra.endpoint.auth", execution)

            String basicAuthValue = encryptBasicAuth(basicAuth, msoKey)
            def authHeader = ""
            if (basicAuthValue != null) {
                String responseAuthHeader = getAuthHeader(execution, basicAuthValue, msoKey)
                String errorCode = jsonUtil.getJsonValue(responseAuthHeader, "errorCode")
                if(errorCode == null || errorCode.isEmpty()) { // No error
                    authHeader = responseAuthHeader
                }
                else {
                    exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(responseAuthHeader, "errorMessage"))
                }
            } else {
                LOGGER.error( "Unable to obtain BasicAuth - BasicAuth value null")
                exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - BasicAuth " +
                        "value null")
            }

            String callDeleteServiceOrderResponse = callDeleteServiceOrder(execution, url, authHeader)
            String errorCode = jsonUtil.getJsonValue(callDeleteServiceOrderResponse, "errorCode")
            String deleteServcieResponse = ""

            if(errorCode == null || errorCode.isEmpty()) { // No error
                deleteServcieResponse = callDeleteServiceOrderResponse // check the response ???
            }
            else {
                LOGGER.error(jsonUtil.getJsonValue(callDeleteServiceOrderResponse, "errorMessage"))
                exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(callDeleteServiceOrderResponse, "errorMessage"))
            }
        } catch (any) {
            String msg = "Exception in DoDeallocateCoreNSSI.deleteServiceOrder. " + any.getCause()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        LOGGER.trace("${PREFIX} Exit deleteServiceOrder")
    }


    String callDeleteServiceOrder(DelegateExecution execution, String urlString, String authHeader) {
        String errorCode = ""
        String errorMessage = ""
        String response = ""

        try {
            HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(urlString), ONAPComponents.EXTERNAL)
            httpClient.addAdditionalHeader("Authorization", authHeader)
            httpClient.addAdditionalHeader("Accept", "application/json")
            Response httpResponse = httpClient.delete()

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
            String msg = "Exception in DoDeallocateCoreNSSI.deleteServiceOrder. " + any.getCause()

            response =  "{\n" +
                    " \"errorCode\": \"7000\",\n" +
                    " \"errorMessage\": \"${msg}\"\n" +
                    "}"
        }

        return response
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
            currentNSSI['associatedProfiles'] =  associatedProfiles
        }

        LOGGER.trace("${PREFIX} Exit getNSSIAssociatedProfiles")
    }


    /**
     * Calculates a final list of S-NSSAI
     * @param execution
     */
    void calculateSNSSAI(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start calculateSNSSAI")

        def currentNSSI = execution.getVariable("currentNSSI")

        List<SliceProfile> associatedProfiles = (List<SliceProfile>)currentNSSI['associatedProfiles']

        String currentSNSSAI = currentNSSI['S-NSSAI']

        List<String> snssais = new ArrayList<>()

        for(SliceProfile associatedProfile:associatedProfiles) {
            if(!associatedProfile.getSNssai().equals(currentSNSSAI)) { // not current S-NSSAI
                snssais.add(associatedProfile.getSNssai())
            }
            else {
                currentNSSI['sliceProfileS-NSSAI'] = associatedProfile
            }
        }

        currentNSSI['S-NSSAIs'] = snssais

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

            GenericVnf constituteVnf = (GenericVnf)currentNSSI['constituteVnf']

            String url = String.format("${nsmfЕndpoint}/serviceInstantiation/v7/serviceInstances/%s/vnfs/%s", networkServiceInstance.getServiceInstanceId(), constituteVnf.getVnfId())

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
                putServiceInstanceResponse = callPUTServiceInstanceResponse // check the response ???
            }
            else {
                LOGGER.error(jsonUtil.getJsonValue(callPUTServiceInstanceResponse, "errorMessage"))
                exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(callPUTServiceInstanceResponse, "errorMessage"))
            }

        } catch (any) {
            String msg = "Exception in DoDeallocateCoreNSSI.invokePUTServiceInstance. " + any.getCause()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        LOGGER.trace("${PREFIX} Exit invokePUTServiceInstance")
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
            String msg = "Exception in DoDeallocateCoreNSSI.invokePUTServiceInstance. " + any.getCause()
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

        String globalSubscriberId = currentNSSI['globalSubscriberId']

        String subscriberName = currentNSSI['subscriberName']

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

        String serviceId = currentNSSI['serviceId']

        RequestInfo requestInfo = new RequestInfo()

        requestInfo.setInstanceName(networkServiceInstance.getServiceInstanceName())
        requestInfo.setSource("VID")
        requestInfo.setProductFamilyId(serviceId)
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

                // No model customization ID
            }
            vfmodules.setModelInfo(vfModuleModelInfo)

            vfmodules.setInstanceName(vfModule.getVfModuleName()) // ???

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

            // No model instance name
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

        // No other instance params, e.g. config-type

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

        // No other user params

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
             String msg = "Exception in DoDeallocateCoreNSSI.prepareRequestDetails. " + any.getCause()
             LOGGER.error(msg)

             response =  "{\n" +
                     " \"errorCode\": \"7000\",\n" +
                     " \"errorMessage\": \"${msg}\"\n" +
                     "}"

         }

         return response
    }


    /**
     * Removes NSSI association with NSI
     * @param execution
     */
    void removeNSSIAssociationWithNSI(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start removeNSSIAssociationWithNSI")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiId']
        String nsiId = currentNSSI['nsiId']

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId)
        AAIResourceUri nsiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nsiId)

        try {
            client.disconnect(nssiUri, nsiUri)
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

        SliceProfile sliceProfileContainsSNSSAI = (SliceProfile)currentNSSI['sliceProfileS-NSSAI']

        String globalSubscriberId = currentNSSI['globalSubscriberId']
        String serviceType = currentNSSI['serviceType']
        String nssiId = currentNSSI['nssiId']

        // global-customer-id, service-type, service-instance-id, profile-id
        AAIResourceUri sliceProfileUri = AAIUriFactory.createResourceUri(AAIObjectType.SLICE_PROFILE, globalSubscriberId, serviceType, nssiId, sliceProfileContainsSNSSAI.getProfileId())

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

        String nssiId = currentNSSI['nssiId']
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
