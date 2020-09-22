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

class DoDeallocateCoreNSSI extends DoCommonCoreNSSI {
    private final String PREFIX ="DoDeallocateCoreNSSI"

    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private MsoUtils utils = new MsoUtils()
    private JsonUtils jsonUtil = new JsonUtils()

    private static final Logger LOGGER = LoggerFactory.getLogger( DoDeallocateCoreNSSI.class)

/**
     * Queries OOF for NSSI termination
     * @param execution
     */
    void executeTerminateNSSIQuery(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start executeTerminateNSSIQuery")

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



    @Override
    String getPrefix() {
        return PREFIX
    }

}
