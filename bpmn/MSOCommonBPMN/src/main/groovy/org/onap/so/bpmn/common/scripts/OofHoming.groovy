/*
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

import org.onap.so.bpmn.core.UrnPropertiesReader
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution

import org.onap.so.bpmn.core.domain.InventoryType
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ResourceType
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.Subscriber
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.DefaultProperties
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.db.catalog.beans.AuthenticationType
import org.onap.so.db.catalog.beans.CloudIdentity
import org.onap.so.db.catalog.beans.CloudSite
import org.onap.so.db.catalog.beans.HomingInstance
import org.onap.so.db.catalog.beans.ServerType
import org.onap.so.logging.filter.base.ONAPComponents;
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.json.JSONArray
import org.json.JSONObject
import org.springframework.web.util.UriUtils

import static org.onap.so.bpmn.common.scripts.GenericUtils.*

import javax.ws.rs.core.Response

/**
 * This class contains the scripts used
 * by the OOF Homing Subflow building block. The
 * subflow attempts to home the provided
 * resources by calling OOF.
 */
class OofHoming extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( OofHoming.class);

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    OofUtils oofUtils = new OofUtils(this)

    /**
     * This method validates the incoming variables.
     * The method then prepares the OOF request
     * and posts it to OOF's rest api.
     *
     * @param execution
     */
    public void callOof(DelegateExecution execution) {
        execution.setVariable("prefix", "HOME_")
        logger.debug( "*** Started Homing Call OOF ***")
        try {
            execution.setVariable("rollbackData", null)
            execution.setVariable("rolledBack", false)

            String requestId = execution.getVariable("msoRequestId")
            logger.debug( "Incoming Request Id is: " + requestId)
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            logger.debug( "Incoming Service Instance Id is: " + serviceInstanceId)
            String serviceInstanceName = execution.getVariable("serviceInstanceName")
            logger.debug( "Incoming Service Instance Name is: " + serviceInstanceName)
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            logger.debug( "Incoming Service Decomposition is: " + serviceDecomposition)
            String subscriberInfo = execution.getVariable("subscriberInfo")
            logger.debug( "Incoming Subscriber Information is: " + subscriberInfo)
            Map customerLocation = execution.getVariable("customerLocation")
            logger.debug( "Incoming Customer Location is: " + customerLocation.toString())
            String cloudOwner = execution.getVariable("cloudOwner")
            logger.debug( "Incoming cloudOwner is: " + cloudOwner)
            String cloudRegionId = execution.getVariable("cloudRegionId")
            logger.debug( "Incoming cloudRegionId is: " + cloudRegionId)

            if (isBlank(requestId) ||
                    isBlank(serviceInstanceId) ||
                    isBlank(serviceInstanceName) ||
                    isBlank(serviceDecomposition.toString()) ||
                    isBlank(customerLocation.toString())) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 4000,
                        "A required input variable is missing or null")
            } else {
                Subscriber subscriber = null
                if (isBlank(subscriberInfo)) {
                    subscriber = new Subscriber("", "", "")
                } else {
                    String subId = jsonUtil.getJsonValue(subscriberInfo, "globalSubscriberId")
                    String subName = jsonUtil.getJsonValue(subscriberInfo, "subscriberName")
                    String subCommonSiteId = ""
                    if (jsonUtil.jsonElementExist(subscriberInfo, "subscriberCommonSiteId")) {
                        subCommonSiteId = jsonUtil.getJsonValue(subscriberInfo, "subscriberCommonSiteId")
                    }
                    subscriber = new Subscriber(subId, subName, subCommonSiteId)
                }

                //Authentication
                def authHeader = ""
                String basicAuth = UrnPropertiesReader.getVariable("mso.oof.auth", execution)
                String msokey = UrnPropertiesReader.getVariable("mso.msoKey", execution)

                String basicAuthValue = utils.encrypt(basicAuth, msokey)
                if (basicAuthValue != null) {
                    logger.debug( "Obtained BasicAuth username and password for OOF Adapter: " + basicAuthValue)
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

                //Prepare Callback
                String timeout = execution.getVariable("timeout")
                if (isBlank(timeout)) {
                    timeout = UrnPropertiesReader.getVariable("mso.oof.timeout", execution);
                    if (isBlank(timeout)) {
                        timeout = "PT30M"
                    }
                }
                logger.debug( "Async Callback Timeout will be: " + timeout)

                execution.setVariable("timeout", timeout)
                execution.setVariable("correlator", requestId)
                execution.setVariable("messageType", "oofResponse")

                //Build Request & Call OOF
                String oofRequest = oofUtils.buildRequest(execution, requestId, serviceDecomposition,
                        subscriber, customerLocation)
                execution.setVariable("oofRequest", oofRequest)
                logger.debug( "OOF Request is: " + oofRequest)

                String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
                logger.debug( "Posting to OOF Url: " + urlString)


                URL url = new URL(urlString)
                HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.OOF)
                httpClient.addAdditionalHeader("Authorization", authHeader)
                Response httpResponse = httpClient.post(oofRequest)

                int responseCode = httpResponse.getStatus()
                logger.debug("OOF sync response code is: " + responseCode)

                if(responseCode != 202){
                    exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from OOF.")
                }

                logger.debug( "*** Completed Homing Call OOF ***")
            }
        } catch (BpmnError b) {
            throw b
        } catch (Exception e) {
            logger.error(e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500,
                    "Internal Error - Occured in Homing callOof: " + e.getMessage())
        }
    }

    /**
     * This method processes the callback response
     * and the contained homing solution. It sets
     * homing solution assignment and license
     * information to the corresponding resources
     *
     * @param execution
     */
    public void processHomingSolution(DelegateExecution execution) {

        logger.debug( "*** Started Homing Process Homing Solution ***")
        try {
            String response = execution.getVariable("asyncCallbackResponse")
            logger.debug( "OOF Async Callback Response is: " + response)

            oofUtils.validateCallbackResponse(execution, response)
            String placements = jsonUtil.getJsonValue(response, "solutions.placementSolutions")
            logger.debug( "****** Solution Placements: " + placements + " *****")

            ServiceDecomposition decomposition = execution.getVariable("serviceDecomposition")
            logger.debug( "Service Decomposition: " + decomposition)

            List<Resource> resourceList = decomposition.getServiceResources()
            JSONArray arr = new JSONArray(placements)
            for (int i = 0; i < arr.length(); i++) {
                JSONArray arrSol = arr.getJSONArray(i)
                for (int j = 0; j < arrSol.length(); j++) {
                    JSONObject placement = arrSol.getJSONObject(j)
                    logger.debug( "****** Placement Solution is: " + placement + " *****")
                    String jsonServiceResourceId = jsonUtil.getJsonValue( placement.toString(), "serviceResourceId")
                    logger.debug( "****** homing serviceResourceId is: " + jsonServiceResourceId + " *****")
                    for (Resource resource : resourceList) {
                        String serviceResourceId = resource.getResourceId()
                        logger.debug( "****** decomp serviceResourceId is: " + serviceResourceId + " *****")
                        if (serviceResourceId.equalsIgnoreCase(jsonServiceResourceId)) {
                            JSONObject solution = placement.getJSONObject("solution")
                            String solutionType = solution.getString("identifierType")
                            String inventoryType = ""
                            if (solutionType.equalsIgnoreCase("serviceInstanceId")) {
                                inventoryType = "service"
                            } else {
                                inventoryType = "cloud"
                            }
                            logger.debug( "****** homing inventoryType is: " + inventoryType + " *****")
                            resource.getHomingSolution().setInventoryType(InventoryType.valueOf(inventoryType))

                            JSONArray assignmentArr = placement.getJSONArray("assignmentInfo")
                            logger.debug( "****** assignmentInfo is: " + assignmentArr.toString() + " *****")

                            Map<String, String> assignmentMap = jsonUtil.entryArrayToMap(execution,
                                    assignmentArr.toString(), "key", "value")
                            String oofDirectives = null
                            assignmentMap.each { key, value ->
                                logger.debug( "****** element: " + key + " *****")
                                if (key == "oof_directives") {
                                    oofDirectives = value
                                    logger.debug( "****** homing oofDirectives: " + oofDirectives + " *****")
                                }
                            }
                            String cloudOwner = assignmentMap.get("cloudOwner")
                            logger.debug( "****** homing cloudOwner: " + cloudOwner + " *****")
                            String cloudRegionId = assignmentMap.get("locationId")
                            logger.debug( "****** homing cloudRegionId: " + cloudRegionId + " *****")
                            resource.getHomingSolution().setCloudOwner(cloudOwner)
                            resource.getHomingSolution().setCloudRegionId(cloudRegionId)

                            CloudSite cloudSite = new CloudSite()
                            cloudSite.setId(cloudRegionId)
                            cloudSite.setRegionId(cloudRegionId)
                            String orchestrator = execution.getVariable("orchestrator")
                            if ((orchestrator != null) && (orchestrator != "")) {
                                cloudSite.setOrchestrator(orchestrator)
                                logger.debug( "****** orchestrator: " + orchestrator + " *****")
                            } else {
                                cloudSite.setOrchestrator("multicloud")
                            }

                            CloudIdentity cloudIdentity = new CloudIdentity()
                            cloudIdentity.setId(cloudRegionId)
                            cloudIdentity.setIdentityServerType(ServerType."KEYSTONE")
                            cloudIdentity.setAdminTenant("service")
                            cloudIdentity.setIdentityAuthenticationType(AuthenticationType.USERNAME_PASSWORD)
                            String msoMulticloudUserName = UrnPropertiesReader
                                    .getVariable("mso.multicloud.api.username", execution,
                                    "apih")
                            String msoMulticloudPassword = UrnPropertiesReader
                                    .getVariable("mso.multicloud.api.password", execution,
                                    "abc123")
                            cloudIdentity.setMsoId(msoMulticloudUserName)
                            cloudIdentity.setMsoPass(msoMulticloudPassword)
                            // Get MSB Url
                            String msbHost = oofUtils.getMsbHost(execution)
                            String multicloudApiEndpoint = UrnPropertiesReader
                                    .getVariable("mso.multicloud.api.endpoint", execution,
                                    "/api/multicloud-titaniumcloud/v1")
                            cloudIdentity.setIdentityUrl(msbHost + multicloudApiEndpoint
                                    + "/" + cloudOwner + "/" +
                                    cloudRegionId + "/infra_workload")
                            logger.debug( "****** Cloud IdentityUrl: " + msbHost + multicloudApiEndpoint
                                    + "/" + cloudOwner + "/" +
                                    cloudRegionId + "/infra_workload"
                                    + " *****")
                            logger.debug( "****** CloudIdentity: " + cloudIdentity.toString()
                                    + " *****")
                            cloudSite.setIdentityService(cloudIdentity)
                            logger.debug( "****** CloudSite: " + cloudSite.toString()
                                    + " *****")

                            // Set cloudsite in catalog DB here
                            oofUtils.createCloudSite(cloudSite, execution)

                            if (oofDirectives != null && oofDirectives != "") {
                                resource.getHomingSolution().setOofDirectives(oofDirectives)
                                execution.setVariable("oofDirectives", oofDirectives)
                                logger.debug( "***** OofDirectives set to: " + oofDirectives +
                                        " *****", "true")
                            }

                            // Set Homing Instance
                            String serviceInstanceId = decomposition.getServiceInstance().getInstanceId()
                            HomingInstance homingInstance = new HomingInstance()
                            homingInstance.setServiceInstanceId(serviceInstanceId)
                            homingInstance.setCloudOwner(cloudOwner)
                            homingInstance.setCloudRegionId(cloudRegionId)
                            if (oofDirectives != null && oofDirectives != "") {
                                homingInstance.setOofDirectives(oofDirectives)}
                            else {
                                homingInstance.setOofDirectives("{}")
                            }
                            oofUtils.createHomingInstance(homingInstance, execution)

                            if (inventoryType.equalsIgnoreCase("service")) {
                                resource.getHomingSolution().setRehome(assignmentMap.get("isRehome").toBoolean())
                                VnfResource vnf = new VnfResource()
                                vnf.setVnfHostname(assignmentMap.get("vnfHostName"))
                                resource.getHomingSolution().setVnf(vnf)
                                resource.getHomingSolution().setServiceInstanceId(solution.getJSONArray("identifiers")[0].toString())
                            }
                        } else {
                            logger.debug( "ProcessHomingSolution Exception: no matching serviceResourceIds returned in " +
                                    "homing solution")
                            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - " +
                                    "Occurred in Homing ProcessHomingSolution: no matching serviceResourceIds returned")

                        }
                    }
                }
                if (JsonUtils.jsonElementExist(response, "solutions.licenseSolutions")) {
                    String licenseSolutions = jsonUtil.getJsonValue(response, "solutions.licenseSolutions")
                    JSONArray licenseArr = new JSONArray(licenseSolutions)
                    for (int l = 0; l < licenseArr.length(); l++) {
                        JSONObject license = licenseArr.getJSONObject(l)
                        String jsonServiceResourceId = license.getString("serviceResourceId")
                        for (Resource resource : resourceList) {
                            String serviceResourceId = resource.getResourceId()
                            if (serviceResourceId.equalsIgnoreCase(jsonServiceResourceId)) {
                                String jsonEntitlementPoolList = jsonUtil.getJsonValue(license.toString(), "entitlementPoolUUID")
                                List<String> entitlementPoolList = jsonUtil.StringArrayToList(execution, jsonEntitlementPoolList)
                                resource.getHomingSolution().getLicense().setEntitlementPoolList(entitlementPoolList)

                                String jsonLicenseKeyGroupList = jsonUtil.getJsonValue(license.toString(), "licenseKeyGroupUUID")
                                List<String> licenseKeyGroupList = jsonUtil.StringArrayToList(execution, jsonLicenseKeyGroupList)
                                resource.getHomingSolution().getLicense().setLicenseKeyGroupList(licenseKeyGroupList)
                            }
                        }
                    }
                }
            }
            execution.setVariable("serviceDecomposition", decomposition)
            execution.setVariable("homingSolution", placements) //TODO - can be removed as output variable

            logger.debug( "*** Completed Homing Process Homing Solution ***")
        } catch (BpmnError b) {
            logger.debug( "ProcessHomingSolution Error: " + b)
            throw b
        } catch (Exception e) {
            logger.debug( "ProcessHomingSolution Exception: " + e)
            logger.error(e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occurred in Homing ProcessHomingSolution")
        }
    }

    /**
     * This method logs the start of DHVCreateService
     * to make debugging easier.
     *
     * @param - execution
     */
    public String logStart(DelegateExecution execution) {

        String requestId = execution.getVariable("testReqId")
        if (isBlank(requestId)) {
            requestId = execution.getVariable("msoRequestId")
        }
        execution.setVariable("DHVCS_requestId", requestId)
        logger.debug( "***** STARTED Homing Subflow for request: " + requestId + " *****")
    }

    /**
     * Auto-generated method stub
     */
    public void preProcessRequest(DelegateExecution execution) {}
        // Not Implemented Method

    /**
     * Constructs a workflow message callback URL for the specified message type and correlator.
     * This type of callback URL is used when a workflow wants an MSO adapter (like the SDNC
     * adapter) to call it back.  In other words, this is for callbacks internal to the MSO
     * complex.  Use <code>createWorkflowMessageAdapterCallbackURL</code> if the callback
     * will come from outside the MSO complex.
     * @param endpoint endpoint address to contruct URL from
     * @param messageType the message type (e.g. SDNCAResponse or VNFAResponse)
     * @param correlator the correlator value (e.g. a request ID)
     */
    public String createHomingCallbackURL(String endpoint, String messageType, String correlator) {
        try {
            if (endpoint == null || endpoint.isEmpty()) {
                ExceptionUtil exceptionUtil = new ExceptionUtil()
                exceptionUtil.buildAndThrowWorkflowException(execution, 2000,
                        'mso:workflow:message:endpoint was not passed in')
            }

            logger.debug( "passed in endpoint: " + endpoint + " *****")

            while (endpoint.endsWith('/')) {
                endpoint = endpoint.substring(0, endpoint.length() - 1)
            }
            logger.debug( "processed endpoint: " + endpoint + " *****")

            return endpoint +
                    '/' + UriUtils.encodePathSegment(messageType, 'UTF-8') +
                    '/' + UriUtils.encodePathSegment(correlator, 'UTF-8')
        } catch (Exception ex) {
            logger.debug( "createCallbackURL Exception: " + ex + " *****")
        }
    }
}
