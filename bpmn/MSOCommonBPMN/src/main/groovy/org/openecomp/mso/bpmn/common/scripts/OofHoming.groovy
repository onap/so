/*
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.mso.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;

import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.core.domain.InventoryType
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.Subscriber
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor

import org.json.JSONArray
import org.json.JSONObject

import static org.openecomp.mso.bpmn.common.scripts.GenericUtils.*

/**
 * This class contains the scripts used
 * by the OOF Homing Subflow building block. The
 * subflow attempts to home the provided
 * resources by calling OOF.
 */
class OofHoming extends AbstractServiceTaskProcessor {

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
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        execution.setVariable("prefix", "HOME_")
        utils.log("DEBUG", "*** Started Homing Call OOF ***", isDebugEnabled)
        try {
            execution.setVariable("rollbackData", null)
            execution.setVariable("rolledBack", false)

            String requestId = execution.getVariable("msoRequestId")
            utils.log("DEBUG", "Incoming Request Id is: " + requestId, isDebugEnabled)
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            utils.log("DEBUG", "Incoming Service Decomposition is: " + serviceDecomposition, isDebugEnabled)
            //String subscriberInfo = execution.getVariable("subscriberInfo")
            //utils.log("DEBUG", "Incoming Subscriber Information is: " + subscriberInfo, isDebugEnabled)
            Map customerLocation = execution.getVariable("customerLocation")
            utils.log("DEBUG", "Incoming Customer Location is: " + customerLocation.toString(), isDebugEnabled)
            String cloudOwner = execution.getVariable("cloudOwner")
            utils.log("DEBUG", "Incoming cloudOwner is: " + cloudOwner, isDebugEnabled)
            String cloudRegionId = execution.getVariable("cloudRegionId")
            utils.log("DEBUG", "Incoming cloudRegionId is: " + cloudRegionId, isDebugEnabled)

            if (isBlank(requestId) ||
                    isBlank(serviceInstanceId) ||
                    isBlank(serviceDecomposition.toString()) ||
                    isBlank(customerLocation.toString()) ||
                    isBlank(cloudOwner) ||
                    isBlank(cloudRegionId)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 4000,
                        "A required input variable is missing or null")
            } else {
                //Authentication
                def authHeader = ""
                String basicAuth = execution.getVariable("URN_mso_oof_auth")
                String msokey = execution.getVariable("URN_mso_msoKey")
                String basicAuthValue = utils.encrypt(basicAuth, msokey)
                if (basicAuthValue != null) {
                    utils.log("DEBUG", "Obtained BasicAuth username and password for OOF Adapter: " + basicAuthValue,
                            isDebugEnabled)
                    try {
                        authHeader = utils.getBasicAuth(basicAuthValue, msokey)
                        execution.setVariable("BasicAuthHeaderValue", authHeader)
                    } catch (Exception ex) {
                        utils.log("DEBUG", "Unable to encode username and password string: " + ex, isDebugEnabled)
                        exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - Unable to " +
                                "encode username and password string")
                    }
                } else {
                    utils.log("DEBUG", "Unable to obtain BasicAuth - BasicAuth value null", isDebugEnabled)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - BasicAuth " +
                            "value null")
                }

                //Prepare Callback
                String timeout = execution.getVariable("timeout")
                if (isBlank(timeout)) {
                    timeout = execution.getVariable("URN_mso_oof_timeout");
                    if (isBlank(timeout)) {
                        timeout = "PT30M"
                    }
                }
                utils.log("DEBUG", "Async Callback Timeout will be: " + timeout, isDebugEnabled)

                execution.setVariable("timeout", timeout)
                execution.setVariable("correlator", requestId)
                execution.setVariable("messageType", "oofResponse")

                //Build Request & Call OOF
                String oofRequest = oofUtils.buildRequest(execution, requestId, serviceDecomposition,
                        cloudOwner, cloudRegionId, customerLocation)
                execution.setVariable("oofRequest", oofRequest)
                utils.log("DEBUG", "OOF Request is: " + oofRequest, isDebugEnabled)

                String endpoint = execution.getVariable("URN_mso_service_agnostic_oof_endpoint")
                String host = execution.getVariable("URN_mso_service_agnostic_oof_host")
                String url = host + endpoint
                utils.log("DEBUG", "Posting to OOF Url: " + url, isDebugEnabled)

                logDebug("URL to be used is: " + url, isDebugEnabled)

                RESTConfig config = new RESTConfig(url)
                RESTClient client = new RESTClient(config).addAuthorizationHeader(authHeader).
                        addHeader("Content-Type", "application/json")
                APIResponse response = client.httpPost(oofRequest)

                int responseCode = response.getStatusCode()
                execution.setVariable("syncResponseCode", responseCode)
                logDebug("OOF sync response code is: " + responseCode, isDebugEnabled)
                String syncResponse = response.getResponseBodyAsString()
                execution.setVariable("syncResponse", syncResponse)
                logDebug("OOF sync response is: " + syncResponse, isDebugEnabled)

                utils.log("DEBUG", "*** Completed Homing Call OOF ***", isDebugEnabled)
            }
        } catch (BpmnError b) {
            throw b
        } catch (Exception e) {
            utils.log("DEBUG", "Error encountered within Homing callOof method: " + e, isDebugEnabled)
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
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("DEBUG", "*** Started Homing Process Homing Solution ***", isDebugEnabled)
        try {
            String response = execution.getVariable("asyncCallbackResponse")
            utils.log("DEBUG", "OOF Async Callback Response is: " + response, isDebugEnabled)
            utils.logAudit("OOF Async Callback Response is: " + response)

            oofUtils.validateCallbackResponse(execution, response)
            String placements = jsonUtil.getJsonValue(response, "solutions.placementSolutions")
            utils.log("DEBUG", "****** Solution Placements: " + placements + " *****", isDebugEnabled)

            ServiceDecomposition decomposition = execution.getVariable("serviceDecomposition")
            utils.log("DEBUG", "Service Decomposition: " + decomposition, isDebugEnabled)

            List<Resource> resourceList = decomposition.getServiceResources()
            JSONArray arr = new JSONArray(placements)
            for (int i = 0; i < arr.length(); i++) {
                JSONObject placement = arr.getJSONObject(i)
                utils.log("DEBUG", "****** JSONObject is: " + placement + " *****", "true")
                String jsonServiceResourceId = placement.getString("serviceResourceId")
                for (Resource resource : resourceList) {
                    String serviceResourceId = resource.getResourceId()
                    if (serviceResourceId.equalsIgnoreCase(jsonServiceResourceId)) {
                        JSONObject solution = placement.getJSONObject("solution")
                        String solutionType = solution.getString("identifierType")
                        String inventoryType = ""
                        if (solutionType.equalsIgnoreCase("serviceInstanceId")) {
                            inventoryType = "service"
                        } else {
                            inventoryType = "cloud"

                        }
                        resource.getHomingSolution().setInventoryType(InventoryType.valueOf(inventoryType))

                        // TODO Deal with Placement Solutions & Assignment Info here
                        JSONArray assignmentArr = placement.getJSONArray("assignmentInfo")
                        Map<String, String> assignmentMap = jsonUtil.entryArrayToMap(execution, assignmentArr.toString(), "key", "value")
                        resource.getHomingSolution().setCloudOwner(assignmentMap.get("cloudOwner"))
                        resource.getHomingSolution().setCloudRegionId(assignmentMap.get("cloudRegionId"))
                        if (inventoryType.equalsIgnoreCase("service")) {
                            resource.getHomingSolution().setRehome(assignmentMap.get("isRehome").toBoolean())
                            VnfResource vnf = new VnfResource()
                            vnf.setVnfHostname(assignmentMap.get("vnfHostName"))
                            resource.getHomingSolution().setVnf(vnf)
                            resource.getHomingSolution().setServiceInstanceId(solution.getJSONArray("identifiers")[0].toString())
                        }
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
            execution.setVariable("serviceDecomposition", decomposition)
            execution.setVariable("homingSolution", placements) //TODO - can be removed as output variable

            utils.log("DEBUG", "*** Completed Homing Process Homing Solution ***", isDebugEnabled)
        } catch (BpmnError b) {
            throw b
        } catch (Exception e) {
            utils.log("DEBUG", "Error encountered within Homing ProcessHomingSolution method: " + e, isDebugEnabled)
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
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        String requestId = execution.getVariable("testReqId")
        if (isBlank(requestId)) {
            requestId = execution.getVariable("msoRequestId")
        }
        execution.setVariable("DHVCS_requestId", requestId)
        utils.log("DEBUG", "***** STARTED Homing Subflow for request: " + requestId + " *****", "true")
        utils.log("DEBUG", "****** Homing Subflow Global Debug Enabled: " + isDebugEnabled + " *****", "true")
        utils.logAudit("***** STARTED Homing Subflow for request: " + requestId + " *****")
    }

    /**
     * Auto-generated method stub
     */
    public void preProcessRequest(DelegateExecution execution) {}
        // Not Implemented Method
}
