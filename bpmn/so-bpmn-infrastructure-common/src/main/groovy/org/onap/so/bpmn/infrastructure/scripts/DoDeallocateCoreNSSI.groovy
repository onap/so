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
import com.google.gson.JsonObject
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.v19.AllottedResource
import org.onap.aai.domain.yang.v19.GenericVnf
import org.onap.aai.domain.yang.v19.ServiceInstance
import org.onap.aai.domain.yang.v19.SliceProfile
import org.onap.aai.domain.yang.v19.SliceProfiles
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.client.oof.adapter.beans.payload.OofRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.core.Response

import static org.apache.commons.lang3.StringUtils.isBlank
import static org.apache.commons.lang3.StringUtils.isBlank
import static org.onap.so.bpmn.common.scripts.GenericUtils.isBlank
import static org.onap.so.bpmn.common.scripts.GenericUtils.isBlank

class DoDeallocateCoreNSSI extends DoCommonCoreNSSI {
    private final String PREFIX ="DoDeallocateCoreNSSI"
    private final  String ACTION = "Deallocate"

    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private MsoUtils utils = new MsoUtils()
    private JsonUtils jsonUtil = new JsonUtils()

    private static final Logger LOGGER = LoggerFactory.getLogger( DoDeallocateCoreNSSI.class)


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
       // String urlString = UrnPropertiesReader.getVariable("mso.adapters.oof.endpoint", execution)

        //API Path
        String apiPath =  "/api/oof/terminate/nxi/v1"
        LOGGER.debug("API path for DoAllocateCoreNSSI: "+apiPath)

        urlString = urlString + apiPath

        //Prepare auth for OOF
        def authHeader = ""
        String basicAuth = UrnPropertiesReader.getVariable("mso.oof.auth", execution)
        String msokey = UrnPropertiesReader.getVariable("mso.msoKey", execution)

        String basicAuthValue = utils.encrypt(basicAuth, msokey)
     /*   if (basicAuthValue != null) {
            LOGGER.debug( "Obtained BasicAuth username and password for OOF Adapter: " + basicAuthValue)
            try {
                authHeader = utils.getBasicAuth(basicAuthValue, msokey)
                execution.setVariable("BasicAuthHeaderValue", authHeader)
            } catch (Exception ex) {
                LOGGER.error( "Unable to encode username and password string: " + ex)
                exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - Unable to encode username and password string")
            }
        } else {
            LOGGER.error( "Unable to obtain BasicAuth - BasicAuth value null")
            exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - BasicAuth value null")
        } */


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
        String oofRequest = getOofUtils().buildTerminateNxiRequest(requestId, nxlId, nxlType, messageType, nssi.getServiceInstanceId())
        LOGGER.debug("**** Terminate Nxi Request: "+oofRequest)

        LOGGER.debug("${PREFIX} Exit buildOOFRequest")

        return oofRequest
    }



    /**
     * Invokes deleteServiceOrder external API
     * @param execution
     */
    void deleteServiceOrder(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start deleteServiceOrder")

        def currentNSSI = execution.getVariable("currentNSSI")

        try {
            //url:/nbi/api/v4/serviceOrder/"
            def nsmfЕndPoint = UrnPropertiesReader.getVariable("mso.infra.endpoint.url", execution)

            ServiceInstance networkServiceInstance = (ServiceInstance)currentNSSI['networkServiceInstance']

            //String url = String.format("${nbiEndpointUrl}/api/v4/serviceOrder/%s", networkServiceInstance.getServiceInstanceId())

            GenericVnf constituteVnf = (GenericVnf)currentNSSI['constituteVnf']

            // http://so.onap:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances/de6a0aa2-19f2-41fe-b313-a5a9f159acd7/vnfs/3abbb373-8d33-4977-aa4b-2bfee496b6d5
            String url = String.format("${nsmfЕndPoint}/serviceInstantiation/v7/serviceInstances/%s/vnfs/%s", networkServiceInstance.getServiceInstanceId(), constituteVnf.getVnfId())

            currentNSSI['deleteServiceOrderURL'] = url

            String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)
            String basicAuth =  UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)

            /*String basicAuthValue = encryptBasicAuth(basicAuth, msoKey)
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
            } */

            def authHeader = utils.getBasicAuth(basicAuth, msoKey)

            String callDeleteServiceOrderResponse = callDeleteServiceOrder(execution, url, authHeader)
            String errorCode = jsonUtil.getJsonValue(callDeleteServiceOrderResponse, "errorCode")

            if(errorCode == null || errorCode.isEmpty()) { // No error
                String macroOperationId = jsonUtil.getJsonValue(callDeleteServiceOrderResponse, "requestReferences.requestId")
                String requestSelfLink = jsonUtil.getJsonValue(callDeleteServiceOrderResponse, "requestReferences.requestSelfLink")

                execution.setVariable("macroOperationId",  macroOperationId)
                execution.setVariable("requestSelfLink", requestSelfLink)

                currentNSSI['requestSelfLink'] = requestSelfLink
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

        LOGGER.debug("${PREFIX} Exit deleteServiceOrder")
    }


    String callDeleteServiceOrder(DelegateExecution execution, String url, String authHeader) {
        LOGGER.debug("${PREFIX} Start callDeleteServiceOrder")

        String errorCode = ""
        String errorMessage = ""
        String response = ""

        try {
            HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.SO)
            httpClient.addAdditionalHeader("Authorization", authHeader)
            httpClient.addAdditionalHeader("Accept", "application/json")
            Response httpResponse = httpClient.delete()

            int soResponseCode = httpResponse.getStatus()
            LOGGER.debug("callDeleteServiceInstance: soResponseCode = " + soResponseCode)

            if (soResponseCode >= 200 && soResponseCode < 204 && httpResponse.hasEntity()) {
                response = httpResponse.readEntity(String.class)

                LOGGER.debug("callDeleteServiceInstance: response = " + response)
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
        String nsiId = currentNSSI['nsiId']
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        // NSSI
        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))


        String allottedResourceId = null


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

        def currentNSSI = execution.getVariable("currentNSSI")

        String url = currentNSSI['requestSelfLink']

        String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)

        String basicAuth =  UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)

        def authHeader = ""
        String basicAuthValue = utils.getBasicAuth(basicAuth, msoKey)

        getProgress(execution, url, basicAuthValue, "deleteStatus")

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
}
