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
import org.onap.aai.domain.yang.v19.ModelVer
import org.onap.aai.domain.yang.v19.ServiceInstance
import org.onap.aai.domain.yang.v19.SliceProfile
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtilFactory
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.core.Response

import static org.onap.so.bpmn.common.scripts.GenericUtils.isBlank

class DoDeallocateCoreNSSI extends DoCommonCoreNSSI {
    private static final Logger LOGGER = LoggerFactory.getLogger( DoDeallocateCoreNSSI.class)
    private static final ObjectMapper mapper = new ObjectMapper()
    private final String PREFIX ="DoDeallocateCoreNSSI"
    private final  String ACTION = "Deallocate"

    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private MsoUtils utils = new MsoUtils()
    private JsonUtils jsonUtil = new JsonUtils()



    @Override
    void preProcessRequest(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start preProcessRequest")

        super.preProcessRequest(execution)

        execution.setVariable("operationType", "DEALLOCATE")

        LOGGER.debug("${getPrefix()} Exit preProcessRequest")
    }



    /**
     * Queries OOF for NSSI termination
     * @param execution
     */
    void executeTerminateNSSIQuery(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start executeTerminateNSSIQuery")

        String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)

        //API Path
        String apiPath =  "/api/oof/terminate/nxi/v1"
        LOGGER.debug("API path for DoAllocateCoreNSSI: "+apiPath)

        urlString = urlString + apiPath

        //Prepare auth for OOF
        def authHeader = ""
        String basicAuth = UrnPropertiesReader.getVariable("mso.oof.auth", execution)
        String msokey = UrnPropertiesReader.getVariable("mso.msoKey", execution)

        String basicAuthValue = utils.encrypt(basicAuth, msokey)

        try {
            authHeader = utils.getBasicAuth(basicAuthValue, msokey)
            execution.setVariable("BasicAuthHeaderValue", authHeader)
        } catch (Exception ex) {
            LOGGER.error( "Unable to encode username and password string: " + ex)
            exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - Unable to encode username and password string")
        }

        //Prepare send request to OOF
        String oofRequest = buildOOFRequest(execution)

        String callOOFResponse = callOOF(urlString, authHeader, oofRequest)
        LOGGER.debug("callOOFResponse=" + callOOFResponse)

        String errorCode = jsonUtil.getJsonValue(callOOFResponse, "errorCode")
        if(errorCode == null || errorCode.isEmpty()) { // No error
            String terminateNSSI = jsonUtil.getJsonValue(callOOFResponse, "terminateResponse")
            LOGGER.debug("isTerminateNSSI=" + terminateNSSI)

            execution.setVariable("isTerminateNSSI", terminateNSSI)
        }
        else {
            LOGGER.error(jsonUtil.getJsonValue(callOOFResponse, "errorMessage"))
            exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(callOOFResponse, "errorMessage"))
        }

        LOGGER.debug("${PREFIX} Exit executeTerminateNSSIQuery")
    }


    /**
     * Executes sync call to OOF
     * @return OOF response
     */
    String callOOF(String urlString, String authHeader, String oofRequest) {
        LOGGER.debug("${PREFIX} Start callOOF")

        String errorCode = ""
        String errorMessage = ""
        String response = ""

        try {
            URL url = new URL(urlString)
            HttpClient httpClient = getHttpClientFactory().newJsonClient(url, ONAPComponents.OOF)
            httpClient.addAdditionalHeader("Authorization", authHeader)
            httpClient.addAdditionalHeader("Content-Type", "application/json")

            Response httpResponse = httpClient.post(oofRequest)

            int responseCode = httpResponse.getStatus()
            LOGGER.debug("OOF sync response code is: " + responseCode)

            if(responseCode < 200 || responseCode >= 300) { // Wrong code
                errorCode = responseCode
                errorMessage = "Received a Bad Sync Response from OOF."

                response =  "{\n" +
                        " \"errorCode\": \"${errorCode}\",\n" +
                        " \"errorMessage\": \"${errorMessage}\"\n" +
                        "}"
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

        LOGGER.debug("${PREFIX} Exit callOOF")

        return response
    }


    /**
     * Builds OOF request
     * @param execution
     * @return
     */
    private String buildOOFRequest(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start buildOOFRequest")

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiId']

        ServiceInstance nssi = null

        AAIResourcesClient client = getAAIClient()

        // NSSI
        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))
        Optional<ServiceInstance> nssiOpt = client.get(ServiceInstance.class, nssiUri)

        if (nssiOpt.isPresent()) {
            nssi = nssiOpt.get()
        }
        else {
            String msg = "NSSI service instance not found in AAI for nssi id " + nssiId
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }


        // NSI
        String nsiId = currentNSSI['nsiId']
        ServiceInstance nsi = null
        AAIResourceUri nsiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nsiId))
        Optional<ServiceInstance> nsiOpt = client.get(ServiceInstance.class, nsiUri)
        if (nsiOpt.isPresent()) {
            nsi = nsiOpt.get()
        }
        else {
            String msg = "NSI service instance not found in AAI for nsi id " + nsiId
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }


        //Setting correlator as requestId
        String requestId = execution.getVariable("msoRequestId")
        execution.setVariable("NSSI_correlator", requestId)

        //Setting messageType for all Core slice as cn
        String messageType = "cn"
        execution.setVariable("NSSI_messageType", messageType)

        //Prepare Callback
        String timeout = execution.getVariable("timeout")
        if (isBlank(timeout)) {
            timeout = UrnPropertiesReader.getVariable("mso.oof.timeout", execution);
            if (isBlank(timeout)) {
                timeout = "PT30M"
            }
        }

        String nxlId = nssi.getServiceInstanceId()
        String nxlType = "NSSI"
        String oofRequest = getOofUtils().buildTerminateNxiRequest(requestId, nxlId, nxlType, messageType, nsi.getServiceInstanceId())
        LOGGER.debug("**** Terminate Nxi Request: "+oofRequest)

        LOGGER.debug("${PREFIX} Exit buildOOFRequest")

        return oofRequest
    }


    /**
     * Prepares ServiceOrderRequest
     * @param execution
     */
    private void prepareServiceOrderRequest(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start prepareServiceOrderRequest")

        def currentNSSI = execution.getVariable("currentNSSI")

        //extAPI path hardcoded for testing purposes, will be updated in next patch
        String extAPIPath = "https://nbi.onap:8443/nbi/api/v4" + "/serviceOrder"
        execution.setVariable("ExternalAPIURL", extAPIPath)

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

        //order item action will always be delete as we are triggering request for deletion
        String orderItemAction = "delete"
        orderItem.put("action", orderItemAction)

        // service Details
        AAIResourcesClient client = getAAIClient()
        ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance'] //(ServiceInstance) currentNSSI['nssi']
        AAIResourceUri modelVerUrl = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.serviceDesignAndCreation().model(networkServiceInstance.getModelInvariantId()).modelVer(networkServiceInstance.getModelVersionId()))

        Map<String, Object> service = new LinkedHashMap()
        // Service id
        service.put("id",  networkServiceInstance.getServiceInstanceId())

        //ServiceName
        String serviceName = networkServiceInstance.getServiceInstanceName()
        service.put("name",  serviceName)

        // Service Type
        service.put("serviceType", networkServiceInstance.getServiceType())
        //Service State
        service.put("serviceState", "active")

        Map<String, String> serviceSpecification = new LinkedHashMap()
        String modelUuid = networkServiceInstance.getModelVersionId()
        serviceSpecification.put("id", modelUuid)
        service.put("serviceSpecification", serviceSpecification)

        orderItem.put("service", service)
        List<Map<String, String>> orderItemList = new ArrayList()
        orderItemList.add(orderItem)
        serviceOrder.put("orderItem", orderItemList)
        String jsonServiceOrder = mapper.writeValueAsString(serviceOrder)
        LOGGER.debug("******* ServiceOrder :: "+jsonServiceOrder)
        execution.setVariable("serviceOrderRequest", jsonServiceOrder)

        LOGGER.debug("${PREFIX} End prepareServiceOrderRequest")
    }



    /**
     * Invokes deleteServiceOrder external API
     * @param execution
     */
    void deleteServiceOrder(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start deleteServiceOrder")

        def currentNSSI = execution.getVariable("currentNSSI")

        prepareServiceOrderRequest(execution)

        try {
            String extAPIPath = execution.getVariable("ExternalAPIURL")
            String payload = execution.getVariable("serviceOrderRequest")
            LOGGER.debug("externalAPIURL is: " + extAPIPath)
            LOGGER.debug("ServiceOrder payload is: " + payload)

            execution.setVariable("ServiceOrderId", "")

            String callDeleteServiceOrderResponse = callDeleteServiceOrder(execution, extAPIPath, payload)
            String errorCode = jsonUtil.getJsonValue(callDeleteServiceOrderResponse, "errorCode")

            if(errorCode == null || errorCode.isEmpty()) { // No error
                JSONObject responseObj = new JSONObject(callDeleteServiceOrderResponse)

                String serviceOrderId = responseObj.get("id")

                execution.setVariable("ServiceOrderId", serviceOrderId)
                LOGGER.info("Delete ServiceOrderid is: " + serviceOrderId)
            }
            else {
                LOGGER.error(jsonUtil.getJsonValue(callDeleteServiceOrderResponse, "errorMessage"))
                exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(errorCode), jsonUtil.getJsonValue(callDeleteServiceOrderResponse, "errorMessage"))
            }

        }catch (BpmnError e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Exception in ServiceOrder ExtAPI" + ex.getMessage()
            LOGGER.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        LOGGER.debug("${PREFIX} Exit deleteServiceOrder")
    }


    String callDeleteServiceOrder(DelegateExecution execution, String extAPIPath, String payload) {
        LOGGER.debug("${PREFIX} Start callDeleteServiceOrder")

        String errorCode = ""
        String errorMessage = ""
        String response = ""

        LOGGER.debug("callDeleteServiceOrder: url = " + extAPIPath)

        try {
            ExternalAPIUtil externalAPIUtil = getExternalAPIUtil()
            Response httpResponse = externalAPIUtil.executeExternalAPIPostCall(execution, extAPIPath, payload)

            int responseCode = httpResponse.getStatus()
            execution.setVariable("ServiceOrderResponseCode", responseCode)
            LOGGER.debug("Delete ServiceOrder response code is: " + responseCode)

            //Process Response
            if(responseCode == 200 || responseCode == 201 || responseCode == 202 ) {
            //200 OK 201 CREATED 202 ACCEPTED
                LOGGER.debug("Delete ServiceOrder Received a Good Response")

                response = httpResponse.readEntity(String.class)

                LOGGER.debug("callDeleteServiceInstance: response = " + response)

                execution.setVariable("DeleteServiceOrderResponse", response)

            }
            else {
                errorCode = 500
                errorMessage = "Response code is " + responseCode

                response =  "{\n" +
                        " \"errorCode\": \"${errorCode}\",\n" +
                        " \"errorMessage\": \"${errorMessage}\"\n" +
                        "}"
            }
        }
        catch (any) {
            String msg = "Exception in DoDeallocateCoreNSSI.callDeleteServiceOrder. " + any.getCause()

            response =  "{\n" +
                    " \"errorCode\": \"7000\",\n" +
                    " \"errorMessage\": \"${msg}\"\n" +
                    "}"
        }

        LOGGER.debug("${PREFIX} Exit callDeleteServiceOrder")

        return response
    }


    /**
     * Removes NSSI association with NSI
     * @param execution
     */
    void removeNSSIAssociationWithNSI(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start removeNSSIAssociationWithNSI")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiId']
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

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
                    // NSI
                    for (AAIResourceUri nsiUri : arRelationships.get().getRelatedUris(Types.SERVICE_INSTANCE)) {
                        Optional<ServiceInstance> nsiOpt = client.get(ServiceInstance.class, nsiUri)

                        if (nsiOpt.isPresent()) {
                            ServiceInstance nsi = nsiOpt.get()
                            if(nsi.getServiceRole().equals("nsi")) { // Service instance as NSI
                                // Removes NSSI association with NSI
                                try {
                                    client.disconnect(nssiUri, nsiUri)
                                }
                                catch (Exception e) {
                                    exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while NSSI association with NSI dosconnect call: " + e.getMessage())
                                }
                            }
                        }
                        else {
                            LOGGER.warn("No NSI found for NSSI id " + nssiId)
                        }
                    }
                }
                else {
                    LOGGER.warn("No relationships found for Allotted Resource for NSSI id " + nssiId)
                }

            }
        }
        else {
            LOGGER.warn("No relationships  found for nssi id = " + nssiId)
        }


        LOGGER.debug("${PREFIX} Exit removeNSSIAssociationWithNSI")
    }


    /**
     * Delets NSSI Service Instance
     * @param execution
     */
    void deleteNSSIServiceInstance(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start deleteNSSIServiceInstance")

        AAIResourcesClient client = getAAIClient()

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiId']
        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))

        try {
            client.delete(nssiUri)
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occurred while NSSI Service Instance delete call: " + e.getMessage())
        }

        LOGGER.debug("${PREFIX} Exit deleteNSSIServiceInstance")
    }


    /**
     * Gets Delete Service Order progress
     * @param execution
     */
    void getDeleteServiceOrderProgress(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start getDeleteServiceOrderProgress")

        String msg
        try {
            String extAPIPath = execution.getVariable("ExternalAPIURL")
            extAPIPath += "/" + execution.getVariable("ServiceOrderId")
            LOGGER.debug("externalAPIURL is: " + extAPIPath)

            ExternalAPIUtil externalAPIUtil = getExternalAPIUtil()
            Response response = externalAPIUtil.executeExternalAPIGetCall(execution, extAPIPath)
            int responseCode = response.getStatus()
            execution.setVariable("GetServiceOrderResponseCode", responseCode)
            LOGGER.debug("Get ServiceOrder response code is: " + responseCode)
            String extApiResponse = response.readEntity(String.class)
            JSONObject responseObj = new JSONObject(extApiResponse)
            execution.setVariable("GetServiceOrderResponse", extApiResponse)
            LOGGER.debug("Get response body is: " + extApiResponse)
            //Process Response //200 OK 201 CREATED 202 ACCEPTED
            if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
            {
                LOGGER.debug("Get Delete ServiceOrder Received a Good Response")
                String orderState = responseObj.get("state")
                if("REJECTED".equalsIgnoreCase(orderState)) {
                    prepareFailedOperationStatusUpdate(execution)
                    return
                }
                JSONArray items = responseObj.getJSONArray("orderItem")
                JSONObject item = items.get(0) as JSONObject
                JSONObject service = item.get("service") as JSONObject
                String networkServiceId = service.get("id")

                execution.setVariable("networkServiceId", networkServiceId)
                String serviceOrderState = item.get("state")
                execution.setVariable("ServiceOrderState", serviceOrderState)
                // Get serviceOrder State and process progress
                if("ACKNOWLEDGED".equalsIgnoreCase(serviceOrderState) || "INPROGRESS".equalsIgnoreCase(serviceOrderState) || "IN_PROGRESS".equalsIgnoreCase(serviceOrderState)) {
                    execution.setVariable("deleteStatus", "processing")
                }
                else if("COMPLETED".equalsIgnoreCase(serviceOrderState)) {
                    execution.setVariable("deleteStatus", "completed")
                }
                else if("FAILED".equalsIgnoreCase(serviceOrderState)) {
                    msg = "ServiceOrder failed"
                    exceptionUtil.buildAndThrowWorkflowException(execution, 7000,  msg)
                }
                else {
                    msg = "ServiceOrder failed"
                    exceptionUtil.buildAndThrowWorkflowException(execution, 7000,  msg)
                }
                LOGGER.debug("NBI serviceOrder state: "+serviceOrderState)
            }
            else{
                msg = "Get ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode
                prepareFailedOperationStatusUpdate(execution)
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000,  msg)
            }

        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000,  e.getMessage())
        }

        LOGGER.debug("${getPrefix()} Exit getDeleteServiceOrderProgress")
    }


    /**
     * Calculates a final list of S-NSSAI
     * @param execution
     */
    void calculateSNSSAI(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start calculateSNSSAI")

        def currentNSSI = execution.getVariable("currentNSSI")

        List<SliceProfile> associatedProfiles = (List<SliceProfile>)currentNSSI['associatedProfiles']

        String givenSliceProfileId = currentNSSI['sliceProfileId']

        List<String> snssais = new ArrayList<>()

        String isTerminateNSSIVar = execution.getVariable("isTerminateNSSI" )

        boolean isTerminateNSSI = Boolean.parseBoolean(isTerminateNSSIVar)

        if(!isTerminateNSSI) { // NSSI should not be terminated
            LOGGER.debug("calculateSNSSAI: associatedProfiles.size()" + associatedProfiles.size())
            for (SliceProfile associatedProfile : associatedProfiles) {
                if (!associatedProfile.getProfileId().equals(givenSliceProfileId)) { // not given profile id
                    LOGGER.debug("calculateSNSSAI: associatedProfile.getSNssai()" + associatedProfile.getSNssai())
                    snssais.add(associatedProfile.getSNssai())
                } else {
                    currentNSSI['sliceProfileS-NSSAI'] = associatedProfile
                }
            }
        }

        currentNSSI['S-NSSAIs'] = snssais

        LOGGER.debug("${getPrefix()} Exit calculateSNSSAI")
    }


    /**
     * OofUtils
     * @return new OofUtils()
     */
    OofUtils getOofUtils() {
        return new OofUtils()
    }


    private String getPrefix() {
        return PREFIX
    }

    @Override
    String getAction() {
        return ACTION
    }

    ExternalAPIUtil getExternalAPIUtil() {
        return new ExternalAPIUtilFactory().create()
    }
}
