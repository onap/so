/*
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.InventoryType
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.Subscriber
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.logging.filter.base.ONAPComponents;

import org.json.JSONArray
import org.json.JSONObject

import static org.onap.so.bpmn.common.scripts.GenericUtils.*;

import jakarta.ws.rs.core.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * This class is contains the scripts used
 * by the Homing Subflow building block. The
 * subflow attempts to home the provided
 * resources by calling sniro.
 *
 * @author cb645j
 *
 */
class SniroHomingV1 extends AbstractServiceTaskProcessor{

    private static final Logger logger = LoggerFactory.getLogger( SniroHomingV1.class);
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    SniroUtils sniroUtils = new SniroUtils(this)

    /**
     * This method validates the incoming variables.
     * The method then prepares the sniro request
     * and posts it to sniro's rest api.
     *
     * @param execution
     *
     * @author cb645j
     */
    public void callSniro(DelegateExecution execution){
        execution.setVariable("prefix","HOME_")
        logger.trace("Started Sniro Homing Call Sniro ")
        try{
            execution.setVariable("rollbackData", null)
            execution.setVariable("rolledBack", false)

            String requestId = execution.getVariable("msoRequestId")
            logger.debug("Incoming Request Id is: "  + requestId)
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            logger.debug("Incoming Service Instance Id is: "  + serviceInstanceId)
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            logger.debug("Incoming Service Decomposition is: "  + serviceDecomposition)
            String subscriberInfo = execution.getVariable("subscriberInfo")
            logger.debug("Incoming Subscriber Information is: "  + subscriberInfo)

            if(isBlank(requestId) || isBlank(serviceInstanceId) || isBlank(serviceDecomposition.toString()) || isBlank(subscriberInfo)){
                exceptionUtil.buildAndThrowWorkflowException(execution, 4000, "A required input variable is missing or null")
            }else{
                String subId = jsonUtil.getJsonValue(subscriberInfo, "globalSubscriberId")
                String subName = jsonUtil.getJsonValue(subscriberInfo, "subscriberName")
                String subCommonSiteId = ""
                if(jsonUtil.jsonElementExist(subscriberInfo, "subscriberCommonSiteId")){
                    subCommonSiteId = jsonUtil.getJsonValue(subscriberInfo, "subscriberCommonSiteId")
                }
                Subscriber subscriber = new Subscriber(subId, subName, subCommonSiteId)

                String cloudConfiguration = execution.getVariable("cloudConfiguration") // TODO Currently not being used
                String homingParameters = execution.getVariable("homingParameters") // (aka. request parameters) Should be json format. TODO confirm its json format

                //Authentication
                String authHeader = UrnPropertiesReader.getVariable("sniro.manager.headers.auth", execution)
                execution.setVariable("BasicAuthHeaderValue", authHeader)

                //Prepare Callback
                String timeout = execution.getVariable("timeout")
                if(isBlank(timeout)){
                    timeout = UrnPropertiesReader.getVariable("sniro.manager.timeout", execution)
                    if(isBlank(timeout)) {
                        timeout = "PT30M";
                    }
                }
                logger.debug("Async Callback Timeout will be: " + timeout)

                execution.setVariable("timeout", timeout);
                execution.setVariable("correlator", requestId);
                execution.setVariable("messageType", "SNIROResponse");

                //Build Request & Call Sniro
                String sniroRequest = sniroUtils.buildRequest(execution, requestId, serviceDecomposition, subscriber, homingParameters)
                execution.setVariable("sniroRequest", sniroRequest)
                logger.debug("SNIRO Request is: " + sniroRequest)

                String endpoint = UrnPropertiesReader.getVariable("sniro.manager.uri.v1", execution)
                String host = UrnPropertiesReader.getVariable("sniro.manager.host", execution)
                String urlString = host + endpoint
                logger.debug("Sniro Url is: " + urlString)

                URL url = new URL(urlString);
                HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.SNIRO)
                httpClient.addAdditionalHeader("Authorization", authHeader)
                Response httpResponse = httpClient.post(sniroRequest)

                int responseCode = httpResponse.getStatus()

                logger.debug("Sniro sync response code is: " + responseCode)
                if(httpResponse.hasEntity()){
                    logger.debug("Sniro sync response is: " + httpResponse.readEntity(String.class))
                }

                if(responseCode != 202){
                    exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from Sniro.")
                }

                logger.trace("Completed Sniro Homing Call Sniro")
            }
        }catch(BpmnError b){
            throw b
        }catch(Exception e){
            logger.debug("Error encountered within Homing CallSniro method: " + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in Homing CallSniro: " + e.getMessage())
        }
    }

    /**
     * This method processes the callback response
     * and the contained homing solution. It sets
     * homing solution assignment and license
     * information to the corresponding resources
     *
     * @param execution
     *
     * @author cb645j 
     */
    public void processHomingSolution(DelegateExecution execution){
        logger.trace("Started Sniro Homing Process Homing Solution")
        try{
            String response = execution.getVariable("asyncCallbackResponse")
            logger.debug("Sniro Async Callback Response is: " + response)

            sniroUtils.validateCallbackResponse(execution, response)

            ServiceDecomposition decomposition = execution.getVariable("serviceDecomposition")
            List<Resource> resourceList = decomposition.getServiceResources()

            if(JsonUtils.jsonElementExist(response, "solutionInfo.placementInfo")){
                String placements = jsonUtil.getJsonValue(response, "solutionInfo.placementInfo")
                JSONArray arr = new JSONArray(placements)
                for(int i = 0; i < arr.length(); i++){
                    JSONObject placement = arr.getJSONObject(i)
                    String jsonServiceResourceId = placement.getString("serviceResourceId")
                    for(Resource resource:resourceList){
                        String serviceResourceId = resource.getResourceId()
                        if(serviceResourceId.equalsIgnoreCase(jsonServiceResourceId)){
                            //match
                            String inventoryType = placement.getString("inventoryType")
                            resource.getHomingSolution().setInventoryType(InventoryType.valueOf(inventoryType))
                            resource.getHomingSolution().setCloudRegionId(placement.getString("cloudRegionId"))
                            resource.getHomingSolution().setRehome(placement.getBoolean("isRehome"))
                            JSONArray assignmentArr = placement.getJSONArray("assignmentInfo")
                            Map<String, String> assignmentMap = jsonUtil.entryArrayToMap(execution, assignmentArr.toString(), "variableName", "variableValue")
                            resource.getHomingSolution().setCloudOwner(assignmentMap.get("cloudOwner"))
                            resource.getHomingSolution().setAicClli(assignmentMap.get("aicClli"))
                            resource.getHomingSolution().setAicVersion(assignmentMap.get("aicVersion"))
                            if(inventoryType.equalsIgnoreCase("service")){
                                VnfResource vnf = new VnfResource()
                                vnf.setVnfHostname(assignmentMap.get("vnfHostName"))
                                vnf.setResourceId(assignmentMap.get("vnfId"))
                                resource.getHomingSolution().setVnf(vnf)
                                resource.getHomingSolution().setServiceInstanceId(placement.getString("serviceInstanceId"))
                            }
                            if(placement.getBoolean("isRehome")) {
                                resource.getHomingSolution().setAllottedResourceId(assignmentMap.get("serviceResourceId"))
                            }
                        }
                    }
                }
            }

            if(JsonUtils.jsonElementExist(response, "solutionInfo.licenseInfo")){
                String licenseInfo = jsonUtil.getJsonValue(response, "solutionInfo.licenseInfo")
                JSONArray licenseArr = new JSONArray(licenseInfo)
                for(int l = 0; l < licenseArr.length(); l++){
                    JSONObject license = licenseArr.getJSONObject(l)
                    String jsonServiceResourceId = license.getString("serviceResourceId")
                    for(Resource resource:resourceList){
                        String serviceResourceId = resource.getResourceId()
                        if(serviceResourceId.equalsIgnoreCase(jsonServiceResourceId)){
                            //match
                            String jsonEntitlementPoolList = jsonUtil.getJsonValue(license.toString(), "entitlementPoolList")
                            List<String> entitlementPoolList = jsonUtil.StringArrayToList(execution, jsonEntitlementPoolList)
                            resource.getHomingSolution().getLicense().setEntitlementPoolList(entitlementPoolList)

                            String jsonLicenseKeyGroupList = jsonUtil.getJsonValue(license.toString(), "licenseKeyGroupList")
                            List<String> licenseKeyGroupList = jsonUtil.StringArrayToList(execution, jsonLicenseKeyGroupList)
                            resource.getHomingSolution().getLicense().setLicenseKeyGroupList(licenseKeyGroupList)
                        }
                    }
                }
            }
            execution.setVariable("serviceDecomposition", decomposition)

            logger.trace("Completed Sniro Homing Process Homing Solution")
        }catch(BpmnError b){
            throw b
        }catch(Exception e){
            logger.debug("Error encountered within Homing ProcessHomingSolution method: " + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in Sniro Homing Process Solution")
        }
    }

    /**
     * This method logs the start of DHVCreateService
     * to make debugging easier.
     *
     * @param - execution
     * @author cb645j
     */
    public String logStart(DelegateExecution execution){
        String requestId = execution.getVariable("testReqId")
        if(isBlank(requestId)){
            requestId = execution.getVariable("msoRequestId")
        }
        execution.setVariable("DHVCS_requestId", requestId)
        logger.trace("STARTED Homing Subflow for request: "  + requestId + " ")
        logger.debug("****** Homing Subflow Global Debug Enabled: " + execution.getVariable("isDebugLogEnabled")  + " *****")
        logger.trace("STARTED Homing Subflow for request: "  + requestId + " ")
    }


    /**
     * Auto-generated method stub
     */
    public void preProcessRequest(DelegateExecution execution){}

}
