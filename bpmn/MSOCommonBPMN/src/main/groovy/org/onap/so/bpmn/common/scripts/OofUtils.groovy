/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.common.scripts

import com.google.gson.GsonBuilder
import org.onap.so.beans.nsmf.oof.NsiReqBody
import org.onap.so.beans.nsmf.oof.NssiReqBody
import org.onap.so.beans.nsmf.oof.RequestInfo
import org.onap.so.beans.nsmf.oof.SubnetCapability
import org.onap.so.beans.nsmf.oof.TemplateInfo

import static org.onap.so.bpmn.common.scripts.GenericUtils.*

import jakarta.ws.rs.core.UriBuilder

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.util.OofInfraUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.AllottedResource
import org.onap.so.bpmn.core.domain.HomingSolution
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInstance
import org.onap.so.bpmn.core.domain.Subscriber
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.catalog.beans.CloudSite
import org.onap.so.db.catalog.beans.HomingInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper

class OofUtils {
    private static final Logger logger = LoggerFactory.getLogger( OofUtils.class);

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    OofInfraUtils oofInfraUtils = new OofInfraUtils()

    private AbstractServiceTaskProcessor utils

    OofUtils(AbstractServiceTaskProcessor taskProcessor) {
        this.utils = taskProcessor
    }

    /**
     * This method builds the service-agnostic
     * OOF json request to get a homing solution
     * and license solution
     *
     * @param execution
     * @param requestId
     * @param decomposition - ServiceDecomposition object
     * @param customerLocation -
     * @param existingCandidates -
     * @param excludedCandidates -
     * @param requiredCandidates -
     *
     * @return request - OOF v1 payload - https://wiki.onap.org/pages/viewpage.action?pageId=25435066
     */
    String buildRequest(DelegateExecution execution,
                        String requestId,
                        ServiceDecomposition decomposition,
                        Subscriber subscriber = null,
                        Map customerLocation,
                        ArrayList existingCandidates = null,
                        ArrayList excludedCandidates = null,
                        ArrayList requiredCandidates = null) {
        logger.debug( "Started Building OOF Request")
        String callbackEndpoint = UrnPropertiesReader.getVariable("mso.oof.callbackEndpoint", execution)
        logger.debug( "mso.oof.callbackEndpoint is: " + callbackEndpoint)
        try {
            def callbackUrl = utils.createHomingCallbackURL(callbackEndpoint, "oofResponse", requestId)
            logger.debug( "callbackUrl is: " + callbackUrl)


            def transactionId = requestId
            logger.debug( "transactionId is: " + transactionId)
            //ServiceInstance Info
            ServiceInstance serviceInstance = decomposition.getServiceInstance()
            def serviceInstanceId = ""
            def serviceName = ""

            serviceInstanceId = execution.getVariable("serviceInstanceId")
            logger.debug( "serviceInstanceId is: " + serviceInstanceId)
            serviceName = execution.getVariable("subscriptionServiceType")
            logger.debug( "serviceName is: " + serviceName)

            if (serviceInstanceId == null || serviceInstanceId == "null") {
                logger.debug( "Unable to obtain Service Instance Id")
                exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Internal Error - Unable to " +
                        "obtain Service Instance Id, execution.getVariable(\"serviceInstanceId\") is null")
            }
            if (serviceName == null || serviceName == "null") {
                logger.debug( "Unable to obtain Service Name")
                exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Internal Error - Unable to " +
                        "obtain Service Name, execution.getVariable(\"subscriptionServiceType\") is null")
            }
            //Model Info
            ModelInfo model = decomposition.getModelInfo()
            logger.debug( "ModelInfo: " + model.toString())
            String modelType = model.getModelType()
            String modelInvariantId = model.getModelInvariantUuid()
            String modelVersionId = model.getModelUuid()
            String modelName = model.getModelName()
            String modelVersion = model.getModelVersion()
            //Subscriber Info
            String subscriberId = ""
            String subscriberName = ""
            String commonSiteId = ""
            if (subscriber != null) {
                subscriberId = subscriber.getGlobalId()
                subscriberName = subscriber.getName()
                commonSiteId = subscriber.getCommonSiteId()
            }

            //Determine RequestType
            //TODO Figure out better way to determine this
            String requestType = "create"
            List<Resource> resources = decomposition.getServiceResources()
            for (Resource r : resources) {
                HomingSolution currentSolution = (HomingSolution) r.getCurrentHomingSolution()
                if (currentSolution != null) {
                    requestType = "speed changed"
                }
            }

            //Demands
            String placementDemands = ""
            StringBuilder sb = new StringBuilder()
            List<AllottedResource> allottedResourceList = decomposition.getAllottedResources()
            List<VnfResource> vnfResourceList = decomposition.getVnfResources()

            if (allottedResourceList == null || allottedResourceList.isEmpty()) {
                logger.debug( "Allotted Resources List is empty - will try to get service VNFs instead.")
            } else {
                for (AllottedResource resource : allottedResourceList) {
                    logger.debug( "Allotted Resource: " + resource.toString())
                    def serviceResourceId = resource.getResourceId()
                    def toscaNodeType = resource.getToscaNodeType()
                    def resourceModuleName = toscaNodeType.substring(toscaNodeType.lastIndexOf(".") + 1)
                    def resourceModelInvariantId = resource.getModelInfo().getModelInvariantUuid()
                    def resourceModelVersionId = resource.getModelInfo().getModelUuid()
                    def resourceModelName = resource.getModelInfo().getModelName()
                    def resourceModelVersion = resource.getModelInfo().getModelVersion()
                    def resourceModelType = resource.getModelInfo().getModelType()
                    def tenantId = execution.getVariable("tenantId")
                    def requiredCandidatesJson = ""

                    requiredCandidatesJson = createCandidateJson(
                            existingCandidates,
                            excludedCandidates,
                            requiredCandidates)

                    String demand =
                            "      {\n" +
                                    "      \"resourceModuleName\": \"${resourceModuleName}\",\n" +
                                    "      \"serviceResourceId\": \"${serviceResourceId}\",\n" +
                                    "      \"tenantId\": \"${tenantId}\",\n" +
                                    "      \"resourceModelInfo\": {\n" +
                                    "        \"modelInvariantId\": \"${resourceModelInvariantId}\",\n" +
                                    "        \"modelVersionId\": \"${resourceModelVersionId}\",\n" +
                                    "        \"modelName\": \"${resourceModelName}\",\n" +
                                    "        \"modelType\": \"${resourceModelType}\",\n" +
                                    "        \"modelVersion\": \"${resourceModelVersion}\",\n" +
                                    "        \"modelCustomizationName\": \"\"\n" +
                                    "        }" + requiredCandidatesJson + "\n" +
                                    "      },"

                    placementDemands = sb.append(demand)
                }
            }

            if (vnfResourceList == null || vnfResourceList.isEmpty()) {
                logger.debug( "VNF Resources List is empty")
            } else {

                for (VnfResource vnfResource : vnfResourceList) {
                    logger.debug( "VNF Resource: " + vnfResource.toString())
                    ModelInfo vnfResourceModelInfo = vnfResource.getModelInfo()
                    def toscaNodeType = vnfResource.getToscaNodeType()
                    def resourceModuleName = toscaNodeType.substring(toscaNodeType.lastIndexOf(".") + 1)
                    def serviceResourceId = vnfResource.getResourceId()
                    def resourceModelInvariantId = vnfResourceModelInfo.getModelInvariantUuid()
                    def resourceModelName = vnfResourceModelInfo.getModelName()
                    def resourceModelVersion = vnfResourceModelInfo.getModelVersion()
                    def resourceModelVersionId = vnfResourceModelInfo.getModelUuid()
                    def resourceModelType = vnfResourceModelInfo.getModelType()
                    def tenantId = execution.getVariable("tenantId")
                    def requiredCandidatesJson = ""


                    String placementDemand =
                            "      {\n" +
                                    "      \"resourceModuleName\": \"${resourceModuleName}\",\n" +
                                    "      \"serviceResourceId\": \"${serviceResourceId}\",\n" +
                                    "      \"tenantId\": \"${tenantId}\",\n" +
                                    "      \"resourceModelInfo\": {\n" +
                                    "        \"modelInvariantId\": \"${resourceModelInvariantId}\",\n" +
                                    "        \"modelVersionId\": \"${resourceModelVersionId}\",\n" +
                                    "        \"modelName\": \"${resourceModelName}\",\n" +
                                    "        \"modelType\": \"${resourceModelType}\",\n" +
                                    "        \"modelVersion\": \"${resourceModelVersion}\",\n" +
                                    "        \"modelCustomizationName\": \"\"\n" +
                                    "        }" + requiredCandidatesJson + "\n" +
                                    "      },"

                    placementDemands = sb.append(placementDemand)
                }
                placementDemands = placementDemands.substring(0, placementDemands.length() - 1)
            }

            /* Commenting Out Licensing as OOF doesn't support for Beijing
        String licenseDemands = ""
        sb = new StringBuilder()
        if (vnfResourceList.isEmpty() || vnfResourceList == null) {
            logger.debug( "Vnf Resources List is Empty")
        } else {
            for (VnfResource vnfResource : vnfResourceList) {
                ModelInfo vnfResourceModelInfo = vnfResource.getModelInfo()
                def resourceInstanceType = vnfResource.getResourceType()
                def serviceResourceId = vnfResource.getResourceId()
                def resourceModuleName = vnfResource.getResourceType()
                def resouceModelInvariantId = vnfResourceModelInfo.getModelInvariantUuid()
                def resouceModelName = vnfResourceModelInfo.getModelName()
                def resouceModelVersion = vnfResourceModelInfo.getModelVersion()
                def resouceModelVersionId = vnfResourceModelInfo.getModelUuid()
                def resouceModelType = vnfResourceModelInfo.getModelType()

                // TODO Add Existing Licenses to demand
                //"existingLicenses": {
                //"entitlementPoolUUID": ["87257b49-9602-4ca1-9817-094e52bc873b",
                // "43257b49-9602-4fe5-9337-094e52bc9435"],
                //"licenseKeyGroupUUID": ["87257b49-9602-4ca1-9817-094e52bc873b",
                // "43257b49-9602-4fe5-9337-094e52bc9435"]
                //}

                    String licenseDemand =
                        "{\n" +
                        "\"resourceModuleName\": \"${resourceModuleName}\",\n" +
                        "\"serviceResourceId\": \"${serviceResourceId}\",\n" +
                        "\"resourceInstanceType\": \"${resourceInstanceType}\",\n" +
                        "\"resourceModelInfo\": {\n" +
                        "  \"modelInvariantId\": \"${resouceModelInvariantId}\",\n" +
                        "  \"modelVersionId\": \"${resouceModelVersionId}\",\n" +
                        "  \"modelName\": \"${resouceModelName}\",\n" +
                        "  \"modelType\": \"${resouceModelType}\",\n" +
                        "  \"modelVersion\": \"${resouceModelVersion}\",\n" +
                        "  \"modelCustomizationName\": \"\"\n" +
                        "  }\n"
                        "},"

                licenseDemands = sb.append(licenseDemand)
            }
            licenseDemands = licenseDemands.substring(0, licenseDemands.length() - 1)
        }*/

            String request =
                    "{\n" +
                            "  \"requestInfo\": {\n" +
                            "    \"transactionId\": \"${transactionId}\",\n" +
                            "    \"requestId\": \"${requestId}\",\n" +
                            "    \"callbackUrl\": \"${callbackUrl}\",\n" +
                            "    \"sourceId\": \"so\",\n" +
                            "    \"requestType\": \"${requestType}\"," +
                            "    \"numSolutions\": 1,\n" +
                            "    \"optimizers\": [\"placement\"],\n" +
                            "    \"timeout\": 600\n" +
                            "    },\n" +
                            "  \"placementInfo\": {\n" +
                            "    \"requestParameters\": {\n" +
                            "      \"customerLatitude\": \"${customerLocation.customerLatitude}\",\n" +
                            "      \"customerLongitude\": \"${customerLocation.customerLongitude}\",\n" +
                            "      \"customerName\": \"${customerLocation.customerName}\"\n" +
                            "    }," +
                            "    \"subscriberInfo\": { \n" +
                            "      \"globalSubscriberId\": \"${subscriberId}\",\n" +
                            "      \"subscriberName\": \"${subscriberName}\",\n" +
                            "      \"subscriberCommonSiteId\": \"${commonSiteId}\"\n" +
                            "    },\n" +
                            "    \"placementDemands\": [\n" +
                            "      ${placementDemands}\n" +
                            "      ]\n" +
                            "    },\n" +
                            "  \"serviceInfo\": {\n" +
                            "    \"serviceInstanceId\": \"${serviceInstanceId}\",\n" +
                            "    \"serviceName\": \"${serviceName}\",\n" +
                            "    \"modelInfo\": {\n" +
                            "      \"modelType\": \"${modelType}\",\n" +
                            "      \"modelInvariantId\": \"${modelInvariantId}\",\n" +
                            "      \"modelVersionId\": \"${modelVersionId}\",\n" +
                            "      \"modelName\": \"${modelName}\",\n" +
                            "      \"modelVersion\": \"${modelVersion}\",\n" +
                            "      \"modelCustomizationName\": \"\"\n" +
                            "    }\n" +
                            "  }\n" +
                            "}"


            logger.debug( "Completed Building OOF Request")
            return request
        } catch (Exception ex) {
             logger.debug( "buildRequest Exception: " + ex)
        }
    }

    /**
     * This method validates the callback response
     * from OOF. If the response contains an
     * exception the method will build and throw
     * a workflow exception.
     *
     * @param execution
     * @param response - the async callback response from oof
     */
    Void validateCallbackResponse(DelegateExecution execution, String response) {
        String placements = ""
        if (isBlank(response)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "OOF Async Callback Response is Empty")
        } else {
            if (JsonUtils.jsonElementExist(response, "solutions.placementSolutions")) {
                placements = jsonUtil.getJsonValue(response, "solutions.placementSolutions")
                if (isBlank(placements) || placements.equalsIgnoreCase("[]")) {
                    String statusMessage = jsonUtil.getJsonValue(response, "statusMessage")
                    if (isBlank(statusMessage)) {
                        logger.debug( "Error Occurred in Homing: OOF Async Callback Response does " +
                                "not contain placement solution.")
                        exceptionUtil.buildAndThrowWorkflowException(execution, 400,
                                "OOF Async Callback Response does not contain placement solution.")
                    } else {
                        logger.debug( "Error Occurred in Homing: " + statusMessage)
                        exceptionUtil.buildAndThrowWorkflowException(execution, 400, statusMessage)
                    }
                } else {
                    return
                }
            } else if (response.contains("error") || response.contains("Error") ) {
                String errorMessage = ""
                if (response.contains("policyException")) {
                    String text = jsonUtil.getJsonValue(response, "requestError.policyException.text")
                    errorMessage = "OOF Async Callback Response contains a Request Error Policy Exception: " + text
                } else if (response.contains("Unable to find any candidate for demand")) {
                    errorMessage = "OOF Async Callback Response contains error: Unable to find any candidate for " +
                            "demand *** Response: " + response.toString()
                } else if (response.contains("serviceException")) {
                    String text = jsonUtil.getJsonValue(response, "requestError.serviceException.text")
                    errorMessage = "OOF Async Callback Response contains a Request Error Service Exception: " + text
                } else {
                    errorMessage = "OOF Async Callback Response contains a Request Error. Unable to determine the Request Error Exception."
                }
                logger.debug( "Error Occurred in Homing: " + errorMessage)
                exceptionUtil.buildAndThrowWorkflowException(execution, 400, errorMessage)

            } else {
                logger.debug( "Error Occurred in Homing: Received an Unknown Async Callback Response from OOF.")
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Received an Unknown Async Callback Response from OOF.")
            }
        }

    }

    /**
     * This method creates candidates json for placement Demands.
     *
     * @param execution
     * @param existingCandidates -
     * @param excludedCandidates -
     * @param requiredCandidates -
     *
     * @return candidatesJson - a JSON string with candidates
     */
    String createCandidateJson(ArrayList existingCandidates = null,
                               ArrayList excludedCandidates = null,
                               ArrayList requiredCandidates = null) {
        def candidatesJson = ""
        def type = ""
        if (existingCandidates != null && existingCandidates != {}) {
            sb = new StringBuilder()
            sb.append(",\n" +
                    "  \"existingCandidates\": [\n")
            def existingCandidateJson = ""
            existingCandidates.each { existingCandidate ->
                type = existingCandidate.get('identifierType')
                if (type == 'vimId') {
                    def cloudOwner = existingCandidate.get('cloudOwner')
                    def cloudRegionId = existingCandidate.get('identifiers')
                    existingCandidateJson = "{\n" +
                            "    \"identifierType\": \"vimId\",\n" +
                            "    \"cloudOwner\": \"${cloudOwner}\",\n" +
                            "    \"identifiers\": [\"${cloudRegionId}\"]\n" +
                            "    },"
                    sb.append(existingCandidateJson)
                }
                if (type == 'serviceInstanceId') {
                    def serviceInstanceId = existingCandidate.get('identifiers')
                    existingCandidateJson += "{\n" +
                            "    \"identifierType\": \"serviceInstanceId\",\n" +
                            "    \"identifiers\": [\"${serviceInstanceId}\"]\n" +
                            "    },"
                    sb.append(existingCandidateJson)
                }
            }
            if (existingCandidateJson != "") {
                sb.setLength(sb.length() - 1)
                candidatesJson = sb.append(",\n],")
            }
        }
        if (excludedCandidates != null && excludedCandidates != {}) {
            sb = new StringBuilder()
            sb.append(",\n" +
                    "  \"excludedCandidates\": [\n")
            def excludedCandidateJson = ""
            excludedCandidates.each { excludedCandidate ->
                type = excludedCandidate.get('identifierType')
                if (type == 'vimId') {
                    def cloudOwner = excludedCandidate.get('cloudOwner')
                    def cloudRegionId = excludedCandidate.get('identifiers')
                    excludedCandidateJson = "{\n" +
                            "    \"identifierType\": \"vimId\",\n" +
                            "    \"cloudOwner\": \"${cloudOwner}\",\n" +
                            "    \"identifiers\": [\"${cloudRegionId}\"]\n" +
                            "    },"
                    sb.append(excludedCandidateJson)
                }
                if (type == 'serviceInstanceId') {
                    def serviceInstanceId = excludedCandidate.get('identifiers')
                    excludedCandidateJson += "{\n" +
                            "    \"identifierType\": \"serviceInstanceId\",\n" +
                            "    \"identifiers\": [\"${serviceInstanceId}\"]\n" +
                            "    },"
                    sb.append(excludedCandidateJson)
                }
            }
            if (excludedCandidateJson != "") {
                sb.setLength(sb.length() - 1)
                candidatesJson = sb.append(",\n],")
            }
        }
        if (requiredCandidates != null && requiredCandidates != {}) {
            sb = new StringBuilder()
            sb.append(",\n" +
                    "  \"requiredCandidates\": [\n")
            def requiredCandidatesJson = ""
            requiredCandidates.each { requiredCandidate ->
                type = requiredCandidate.get('identifierType')
                if (type == 'vimId') {
                    def cloudOwner = requiredCandidate.get('cloudOwner')
                    def cloudRegionId = requiredCandidate.get('identifiers')
                    requiredCandidatesJson = "{\n" +
                            "    \"identifierType\": \"vimId\",\n" +
                            "    \"cloudOwner\": \"${cloudOwner}\",\n" +
                            "    \"identifiers\": [\"${cloudRegionId}\"]\n" +
                            "    },"
                    sb.append(requiredCandidatesJson)
                }
                if (type == 'serviceInstanceId') {
                    def serviceInstanceId = requiredCandidate.get('identifiers')
                    requiredCandidatesJson += "{\n" +
                            "    \"identifierType\": \"serviceInstanceId\",\n" +
                            "    \"identifiers\": [\"${serviceInstanceId}\"]\n" +
                            "    },"
                    sb.append(requiredCandidatesJson)
                }
            }
            if (requiredCandidatesJson != "") {
                sb.setLength(sb.length() - 1)
                candidatesJson = sb.append(",\n],")
            }
        }
        if (candidatesJson != "") {candidatesJson = candidatesJson.substring(0, candidatesJson.length() - 1)}
        return candidatesJson
    }

    /**
     * This method creates a cloudsite in catalog database.
     *
     * @param CloudSite cloudSite
     *
     * @return void
     */
    Void createCloudSite(CloudSite cloudSite, DelegateExecution execution) {
        oofInfraUtils.createCloudSite(cloudSite, execution)
    }

    /**
     * This method creates a HomingInstance in catalog database.
     *
     * @param HomingInstance homingInstance
     *
     * @return void
     */
    Void createHomingInstance(HomingInstance homingInstance, DelegateExecution execution) {
        oofInfraUtils.createHomingInstance(homingInstance, execution)
    }

    String getMsbHost(DelegateExecution execution) {
        String msbHost = UrnPropertiesReader.getVariable("mso.msb.host", execution, "msb-iag.onap")

        Integer msbPort = UrnPropertiesReader.getVariable("mso.msb.port", execution, "80").toInteger()

        return UriBuilder.fromPath("").host(msbHost).port(msbPort).scheme("http").build().toString()
    }

    public String buildSelectNSTRequest(String requestId,String messageType, Map<String, Object> profileInfo) {
        def transactionId = requestId
        logger.debug( "transactionId is: " + transactionId)
		String correlator = requestId
        String callbackUrl = UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint") + "/" + messageType + "/" + correlator
        ObjectMapper objectMapper = new ObjectMapper()
        String json = objectMapper.writeValueAsString(profileInfo)
        StringBuilder response = new StringBuilder()
        response.append(
                "{\n" +
                        "  \"requestInfo\": {\n" +
                        "    \"transactionId\": \"${transactionId}\",\n" +
                        "    \"requestId\": \"${requestId}\",\n" +
                        "    \"sourceId\": \"so\",\n" +
                        "    \"timeout\": 600,\n" +
                        "    \"callbackUrl\": \"${callbackUrl}\"\n" +
                        "    },\n")
        response.append(" \"serviceProfile\":")
        response.append(json)
        response.append("\n}\n")
        return response.toString()
    }

    public String buildSelectNSSTRequest(String requestId,String messageType, Map<String, Object> profileInfo) {
        def transactionId = requestId
        logger.debug( "transactionId is: " + transactionId)
        String correlator = requestId
        String callbackUrl = UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint") + "/" + messageType + "/" + correlator
        ObjectMapper objectMapper = new ObjectMapper()
        String json = objectMapper.writeValueAsString(profileInfo)
        StringBuilder response = new StringBuilder()
        response.append(
                "{\n" +
                        "  \"requestInfo\": {\n" +
                        "    \"transactionId\": \"${transactionId}\",\n" +
                        "    \"requestId\": \"${requestId}\",\n" +
                        "    \"sourceId\": \"so\",\n" +
                        "    \"timeout\": 600,\n" +
                        "    \"callbackUrl\": \"${callbackUrl}\"\n" +
                        "    },\n")
        response.append(" \"SliceProfile\":")
        response.append(json)
        response.append("\n}\n")
        return response.toString()
    }


    public String buildSelectNSIRequest(String requestId, String nstInfo,String messageType, Map<String, Object> profileInfo){

        def transactionId = requestId
        logger.debug( "transactionId is: " + transactionId)
		String correlator = requestId
        String callbackUrl = UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint") + "/" + messageType + "/" + correlator
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(profileInfo);
        StringBuilder response = new StringBuilder();
        response.append(
                "{\n" +
                        "  \"requestInfo\": {\n" +
                        "    \"transactionId\": \"${transactionId}\",\n" +
                        "    \"requestId\": \"${requestId}\",\n" +
                        "    \"sourceId\": \"so\",\n" +
                        "    \"timeout\": 600,\n" +
                        "    \"callbackUrl\": \"${callbackUrl}\"\n" +
                        "    },\n" +
                        "  \"serviceInfo\": {\n" +
                        "    \"serviceInstanceId\": \"\",\n" +
                        "    \"serviceName\": \"\"\n" +
                        "    },\n" +
                        "  \"NSTInfoList\": [\n")
        response.append(nstInfo);
        response.append("\n  ],\n")
        response.append("\n \"serviceProfile\": \n")
        response.append(json);
        response.append("\n  }\n")
        return response.toString()
    }
/**
* Method to create select NSSI request
* @param requestId - mso-request-id
* @param messageType - Message type for callback correlation
* @param UUID - UUID of NSST
* @param invariantUUID - Invariant UUID of NSST
* @param name - name of the NSST model
* @param profileInfo - A JSON object containing slice profile parameters
* @return
*/
public String buildSelectNSSIRequest(String requestId, String messageType, String UUID,String invariantUUID,
String name, Map<String, Object> profileInfo){

def transactionId = requestId
logger.debug( "transactionId is: " + transactionId)
String correlator = requestId
String callbackUrl = UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint") + "/" + messageType + "/" + correlator
ObjectMapper objectMapper = new ObjectMapper();
String profileJson = objectMapper.writeValueAsString(profileInfo);
JsonParser parser = new JsonParser()

//Prepare requestInfo object
JsonObject requestInfo = new JsonObject()
requestInfo.addProperty("transactionId", transactionId)
requestInfo.addProperty("requestId", requestId)
requestInfo.addProperty("callbackUrl", callbackUrl)
requestInfo.addProperty("sourceId","SO" )
requestInfo.addProperty("timeout", 600)
requestInfo.addProperty("numSolutions", 1)

//Prepare serviceInfo object
JsonObject nsstInfo = new JsonObject()
nsstInfo.addProperty("UUID", UUID)
nsstInfo.addProperty("invariantUUID", invariantUUID)
nsstInfo.addProperty("name", name)

JsonObject json = new JsonObject()
json.add("requestInfo", requestInfo)
json.add("NSSTInfo", nsstInfo)
json.add("sliceProfile", (JsonObject) parser.parse(profileJson))

return json.toString()
}

/**
* Method to create NSI/NSSI termination request
* (OOF response will be synchronous in G-Release)
* @param requestId - mso-request-id
* @param nxlId        - NSI/NSSI Id to be terminated
* @param messageType - Message type for callback correlation
* @param serviceInstanceId - NSI/NSSI Id related to nxlId
* @return
*/
public String buildTerminateNxiRequest(String requestId,String nxlId, String nxlType, String messageType, String serviceInstanceId) {
def transactionId = requestId
logger.debug( "transactionId is: " + transactionId)
String correlator = requestId
String callbackUrl = UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint") + "/" + messageType + "/" + correlator
//Prepare Terminate Nxl Json
JsonObject json = new JsonObject()
json.addProperty("type", nxlType)
json.addProperty("NxIId", nxlId)
 
//Prepare requestInfo object
JsonObject requestInfo = new JsonObject()
requestInfo.addProperty("transactionId", transactionId)
requestInfo.addProperty("requestId", requestId)
requestInfo.addProperty("callbackUrl", callbackUrl)
requestInfo.addProperty("sourceId","SO" )
requestInfo.addProperty("timeout", 600)
 
//Prepare addtnlArgs object
JsonObject addtnlArgs = new JsonObject()
addtnlArgs.addProperty("serviceInstanceId", serviceInstanceId)
 
requestInfo.add("addtnlArgs", addtnlArgs)
json.add("requestInfo", requestInfo)
 
return json.toString()
 
}

    public String buildSelectNSIRequest(String requestId, TemplateInfo nstInfo, List<TemplateInfo> nsstInfo,
                                        String messageType, Map<String, Object> serviceProfile,
                                        List<SubnetCapability> subnetCapabilities, Integer timeOut, boolean preferReuse){

        def transactionId = requestId
        String correlator = requestId
        logger.debug( "transactionId is: " + transactionId)

        String callbackUrl = UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint") + "/" + messageType + "/" + correlator

        NsiReqBody nsiReqBody = new NsiReqBody()

        RequestInfo requestInfo = new RequestInfo()
        requestInfo.setRequestId(requestId)
        requestInfo.setTransactionId(transactionId)
        requestInfo.setCallbackUrl(callbackUrl)
        requestInfo.setSourceId("so")
        requestInfo.setTimeout(timeOut)
        requestInfo.setNumSolutions(1)

        nsiReqBody.setRequestInfo(requestInfo)
        nsiReqBody.setNSTInfo(nstInfo)
        nsiReqBody.setServiceProfile(serviceProfile)
        nsiReqBody.setSubnetCapabilities(subnetCapabilities)
        nsiReqBody.setNSSTInfo(nsstInfo)
        nsiReqBody.setPreferReuse(preferReuse)

        return bean2JsonStr(nsiReqBody)
    }

    public <T> String buildSelectNSSIRequest(String requestId, TemplateInfo nsstInfo, String messageType,
                                             T sliceProfile, Integer timeOut){

        def transactionId = requestId
        String correlator = requestId
        logger.debug( "transactionId is: " + transactionId)

        String callbackUrl = UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint") + "/" + messageType + "/" + correlator

        NssiReqBody nssiReqBody = new NssiReqBody()

        RequestInfo requestInfo = new RequestInfo()
        requestInfo.setRequestId(requestId)
        requestInfo.setTransactionId(transactionId)
        requestInfo.setCallbackUrl(callbackUrl)
        requestInfo.setSourceId("so")
        requestInfo.setTimeout(timeOut)
        requestInfo.setNumSolutions(100)

        nssiReqBody.setRequestInfo(requestInfo)
        nssiReqBody.setSliceProfile(sliceProfile)
        nssiReqBody.setNSSTInfo(nsstInfo)

        return bean2JsonStr(nssiReqBody)
    }

    private static <T> String bean2JsonStr(T t) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(t)
    }
}
