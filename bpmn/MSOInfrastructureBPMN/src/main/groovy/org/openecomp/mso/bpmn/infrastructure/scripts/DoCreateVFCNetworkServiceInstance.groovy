/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.bpmn.infrastructure.scripts

import static org.apache.commons.lang3.StringUtils.*
import groovy.xml.XmlUtil
import groovy.json.*
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor 
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil 
import org.openecomp.mso.bpmn.core.WorkflowException 
import org.openecomp.mso.bpmn.core.json.JsonUtils 
import org.openecomp.mso.rest.APIResponse

import java.util.UUID

import org.camunda.bpm.engine.delegate.BpmnError 
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64
import org.springframework.web.util.UriUtils 
import org.openecomp.mso.rest.RESTClient 
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.rest.APIResponse

/**
 * This groovy class supports the <class>DoCreateVFCNetworkServiceInstance.bpmn</class> process.
 * flow for VFC Network Service Create
 */
public class DoCreateVFCNetworkServiceInstance extends AbstractServiceTaskProcessor {

    String vfcUrl = "/vfc/rest/v1/vfcadapter"
    
    String host = "http://mso.mso.testlab.openecomp.org:8080"
    
    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()
    
    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */
    public void preProcessRequest (Execution execution) {
	   def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
       String msg = ""
       utils.log("INFO", " *** preProcessRequest() *** ", isDebugEnabled)
       try {
           //deal with nsName and Description
           String nsServiceName = execution.getVariable("nsServiceName")
           String nsServiceDescription = execution.getVariable("nsServiceDescription")
           utils.log("INFO", "nsServiceName:" + nsServiceName + " nsServiceDescription:" + nsServiceDescription, isDebugEnabled)
           //deal with operation key
           String globalSubscriberId = execution.getVariable("globalSubscriberId")
           utils.log("INFO", "globalSubscriberId:" + globalSubscriberId, isDebugEnabled)
           String serviceType = execution.getVariable("serviceType")
           utils.log("INFO", "serviceType:" + serviceType, isDebugEnabled)
           String serviceId = execution.getVariable("serviceId")
           utils.log("INFO", "serviceId:" + serviceId, isDebugEnabled)
           String operationId = execution.getVariable("operationId")
           utils.log("INFO", "serviceType:" + serviceType, isDebugEnabled)
           String nodeTemplateUUID = execution.getVariable("resourceUUID")
           utils.log("INFO", "nodeTemplateUUID:" + nodeTemplateUUID, isDebugEnabled)
           /*
            * segmentInformation needed as a object of segment
            * {
            *     "domain":"",
            *     "nodeTemplateName":"",
            *     "nodeType":"",
            *     "nsParameters":{
            *       //this is the nsParameters sent to VF-C
            *     }
            * }
            */
           String nsParameters = execution.getVariable("resourceParameters")
           utils.log("INFO", "nsParameters:" + nsParameters, isDebugEnabled)
           String nsOperationKey = """{
                   "globalSubscriberId":"${globalSubscriberId}",
                   "serviceType":"${serviceType}",
                   "serviceId":"${serviceId}",
                   "operationId":"${operationId}",
                   "nodeTemplateUUID":"${nodeTemplateUUID}"
                    }"""
           execution.setVariable("nsOperationKey", nsOperationKey)
           execution.setVariable("nsParameters", nsParameters)
           

       } catch (BpmnError e) {
           throw e
       } catch (Exception ex){
           msg = "Exception in preProcessRequest " + ex.getMessage()
           utils.log("INFO", msg, isDebugEnabled)
           exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
       }
       utils.log("INFO"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

    /**
     * create NS task
     */
    public void createNetworkService(Execution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," *****  createNetworkService *****",  isDebugEnabled)
        String nsOperationKey = execution.getVariable("nsOperationKey")
        String nsParameters = execution.getVariable("nsParameters")
        String nsServiceName = execution.getVariable("nsServiceName")
        String nsServiceDescription = execution.getVariable("nsServiceDescription")
        String reqBody ="""{
                "nsServiceName":"${nsServiceName}",
                "nsServiceDescription":"${nsServiceDescription}",
                "nsOperationKey":${nsOperationKey},
                "nsParameters":${nsParameters}
               }"""
        APIResponse apiResponse = postRequest(execution, host + vfcUrl + "/ns", reqBody)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String nsInstanceId = ""
        if(returnCode== "200" || returnCode == "201"){
            nsInstanceId =  jsonUtil.getJsonValue(aaiResponseAsString, "nsInstanceId")
        }
        execution.setVariable("nsInstanceId", nsInstanceId)
        utils.log("INFO"," *****Exit  createNetworkService *****",  isDebugEnabled)
    }

    /**
     * instantiate NS task
     */
    public void instantiateNetworkService(Execution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," *****  instantiateNetworkService *****",  isDebugEnabled)
        String nsOperationKey = execution.getVariable("nsOperationKey")
        String nsParameters = execution.getVariable("nsParameters")
        String nsServiceName = execution.getVariable("nsServiceName")
        String nsServiceDescription = execution.getVariable("nsServiceDescription")
        String reqBody ="""{
        "nsServiceName":"${nsServiceName}",
        "nsServiceDescription":"${nsServiceDescription}",
        "nsOperationKey":${nsOperationKey},
        "nsParameters":${nsParameters}
       }"""
        String nsInstanceId = execution.getVariable("nsInstanceId")
        String url = host + vfcUrl + "/ns/" +nsInstanceId + "/instantiate"
        APIResponse apiResponse = postRequest(execution, url, reqBody)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String jobId = ""
        if(returnCode== "200"|| returnCode == "201"){
            jobId =  jsonUtil.getJsonValue(aaiResponseAsString, "jobId")
        }
        execution.setVariable("jobId", jobId)
        utils.log("INFO"," *****Exit  instantiateNetworkService *****",  isDebugEnabled)
    }

    /**
     * query NS task
     */
    public void queryNSProgress(Execution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," *****  queryNSProgress *****",  isDebugEnabled)
        String jobId = execution.getVariable("jobId")
        String nsOperationKey = execution.getVariable("nsOperationKey")
        String url = host + vfcUrl + "/jobs/" + jobId
        APIResponse apiResponse = postRequest(execution, url, nsOperationKey)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String operationStatus = "error"
        if(returnCode== "200"|| returnCode == "201"){
            operationStatus = jsonUtil.getJsonValue(aaiResponseAsString, "responseDescriptor.status")
        }
        execution.setVariable("operationStatus", operationStatus)
        utils.log("INFO"," *****Exit  queryNSProgress *****",  isDebugEnabled)
    }

    /**
     * delay 5 sec 
     */
    public void timeDelay(Execution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        try {
            Thread.sleep(5000)
        } catch(InterruptedException e) {           
            utils.log("ERROR", "Time Delay exception" + e , isDebugEnabled)
        }
    }

    /**
     * finish NS task
     */
    public void addNSRelationship(Execution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** addNSRelationship *****",  isDebugEnabled)
        String nsInstanceId = execution.getVariable("nsInstanceId")
        if(nsInstanceId == null || nsInstanceId == ""){
            utils.log("INFO"," create NS failed, so do not need to add relationship",  isDebugEnabled)
            return
        }
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")
        String serviceId = execution.getVariable("serviceId")
        String addRelationPayload = """<relationship xmlns="http://org.openecomp.aai.inventory/v11">
                                            <related-to>service-instance</related-to>
                                            <related-link>/aai/v11/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${serviceType}/service-instances/service-instance/${nsInstanceId}</related-link>
                                            <relationship-data>
                                                <relationship-key>customer.global-customer-id</relationship-key>
                                                <relationship-value>${globalSubscriberId}</relationship-value>
                                            </relationship-data>
                                            <relationship-data>
                                                <relationship-key>service-subscription.service-type</relationship-key>
                                                <relationship-value>${serviceType}</relationship-value>
                                            </relationship-data>
                                           <relationship-data>
                                                <relationship-key>service-instance.service-instance-id</relationship-key>
                                                <relationship-value>${nsInstanceId}</relationship-value>
                                            </relationship-data>           
                                        </relationship>"""
        String endpoint = execution.getVariable("URN_aai_endpoint")  
        utils.log("INFO","Add Relationship req:\n" + addRelationPayload,  isDebugEnabled)
        String url = endpoint + "/aai/v11/business/customers/customer/" + globalSubscriberId + "/service-subscriptions/service-subscription/" + serviceType + "/service-instances/service-instance/" + serviceId + "/relationship-list/relationship"
        APIResponse aaiRsp = executeAAIPutCall(execution, url, addRelationPayload)
        utils.log("INFO","aai response status code:" + aaiRsp.getStatusCode(),  isDebugEnabled)
        utils.log("INFO","aai response content:" + aaiRsp.getResponseBodyAsString(),  isDebugEnabled)
        utils.log("INFO"," *****Exit addNSRelationship *****",  isDebugEnabled)
    }
    
    public APIResponse executeAAIPutCall(Execution execution, String url, String payload){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", " ======== Started Execute AAI Put Process ======== ",  isDebugEnabled) 
        APIResponse apiResponse = null
        try{
            String uuid = utils.getRequestID()
            utils.log("INFO","Generated uuid is: " + uuid,  isDebugEnabled) 
            utils.log("INFO","URL to be used is: " + url,  isDebugEnabled) 
            String userName = execution.getVariable("URN_aai_auth")
            String password = execution.getVariable("URN_mso_msoKey")
            String basicAuthCred = utils.getBasicAuth(userName,password)
            RESTConfig config = new RESTConfig(url)
            RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Content-Type", "application/xml").addHeader("Accept","application/xml")
            if (basicAuthCred != null && !"".equals(basicAuthCred)) {
                client.addAuthorizationHeader(basicAuthCred)
            }
            apiResponse = client.httpPut(payload)
            utils.log("INFO","======== Completed Execute AAI Put Process ======== ",  isDebugEnabled) 
        }catch(Exception e){
            utils.log("ERROR","Exception occured while executing AAI Put Call. Exception is: \n" + e,  isDebugEnabled) 
            throw new BpmnError("MSOWorkflowException")
        }
        return apiResponse
    }
    
    /**
     * post request
     * url: the url of the request
     * requestBody: the body of the request
     */
    private APIResponse postRequest(Execution execution, String url, String requestBody){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** Started Execute VFC adapter Post Process *****",  isDebugEnabled)
        utils.log("INFO","url:"+url +"\nrequestBody:"+ requestBody,  isDebugEnabled)
        APIResponse apiResponse = null
        try{
            RESTConfig config = new RESTConfig(url)
            RESTClient client = new RESTClient(config).addHeader("Content-Type", "application/json").addHeader("Accept","application/json").addHeader("Authorization","Basic QlBFTENsaWVudDpwYXNzd29yZDEk")
            apiResponse = client.httpPost(requestBody)
            utils.log("INFO","response code:"+ apiResponse.getStatusCode() +"\nresponse body:"+ apiResponse.getResponseBodyAsString(),  isDebugEnabled)    
            utils.log("INFO","======== Completed Execute VF-C adapter Post Process ======== ",  isDebugEnabled)
        }catch(Exception e){
            utils.log("ERROR","Exception occured while executing AAI Post Call. Exception is: \n" + e,  isDebugEnabled)
            throw new BpmnError("MSOWorkflowException")
        }        
        return apiResponse
    }
}
