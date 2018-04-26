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

package org.openecomp.mso.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*
import groovy.xml.XmlUtil
import groovy.json.*
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor 
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil 
import org.openecomp.mso.bpmn.core.WorkflowException 
import org.openecomp.mso.bpmn.core.json.JsonUtils 
import org.openecomp.mso.rest.APIResponse

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError 
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils 
import org.openecomp.mso.rest.RESTClient 
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.rest.RESTClient



/**
 * This groovy class supports the <class>DoDeleteVFCNetworkServiceInstance.bpmn</class> process.
 * flow for E2E ServiceInstance Delete
 */
public class DoDeleteVFCNetworkServiceInstance extends AbstractServiceTaskProcessor {

            
    String vfcUrl = "/vfc/rest/v1/vfcadapter"
    
    String host = "http://mso.mso.testlab.openecomp.org:8080"
    
    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     */
    public void preProcessRequest (DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        String msg = ""
        utils.log("INFO", " *** preProcessRequest() *** ", isDebugEnabled)
        try {
            //deal with operation key
            String globalSubscriberId = execution.getVariable("globalSubscriberId")
            utils.log("INFO", "globalSubscriberId:" + globalSubscriberId, isDebugEnabled)
            String serviceType = execution.getVariable("serviceType")
            utils.log("INFO", "serviceType:" + serviceType, isDebugEnabled)
            String serviceId = execution.getVariable("serviceId")
            utils.log("INFO", "serviceId:" + serviceId, isDebugEnabled)
            String operationId = execution.getVariable("operationId")
            utils.log("INFO", "serviceType:" + serviceType, isDebugEnabled)
            String nodeTemplateUUID = execution.getVariable("resourceTemplateId")
            utils.log("INFO", "nodeTemplateUUID:" + nodeTemplateUUID, isDebugEnabled)
            String nsInstanceId = execution.getVariable("resourceInstanceId")
            utils.log("INFO", "nsInstanceId:" + nsInstanceId, isDebugEnabled)
            execution.setVariable("nsInstanceId",nsInstanceId)
            String nsOperationKey = """{
            "globalSubscriberId":"${globalSubscriberId}",
            "serviceType":"${serviceType}",
            "serviceId":"${serviceId}",
            "operationId":"${operationId}",
            "nodeTemplateUUID":"${nodeTemplateUUID}"
             }"""
            execution.setVariable("nsOperationKey", nsOperationKey);
            utils.log("INFO", "nsOperationKey:" + nsOperationKey, isDebugEnabled)
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            utils.log("INFO", msg, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        utils.log("INFO"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

    /**
     * unwind NS from AAI relationship
     */
    public void deleteNSRelationship(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** addNSRelationship *****",  isDebugEnabled)
        String nsInstanceId = execution.getVariable("resourceInstanceId")
        if(nsInstanceId == null || nsInstanceId == ""){
            utils.log("INFO"," Delete NS failed",  isDebugEnabled)
            return
        }
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")
        String serviceId = execution.getVariable("serviceId")
        String deleteRelationPayload = """<relationship xmlns="http://org.openecomp.aai.inventory/v11">
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
        utils.log("INFO","Add Relationship req:\n" + deleteRelationPayload,  isDebugEnabled)
        String url = endpoint + "/aai/v11/business/customers/customer/" + globalSubscriberId + "/service-subscriptions/service-subscription/" + serviceType + "/service-instances/service-instance/" + serviceId + "/relationship-list/relationship"

        APIResponse aaiRsp = executeAAIDeleteCall(execution, url, deleteRelationPayload)
        utils.log("INFO","aai response status code:" + aaiRsp.getStatusCode(),  isDebugEnabled)
        utils.log("INFO","aai response content:" + aaiRsp.getResponseBodyAsString(),  isDebugEnabled)
        utils.log("INFO"," *****Exit addNSRelationship *****",  isDebugEnabled)
    }

    public APIResponse executeAAIDeleteCall(DelegateExecution execution, String url, String payload){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", " ======== Started Execute AAI Delete Process ======== ",  isDebugEnabled)
        APIResponse apiResponse = null
        try{
            String uuid = utils.getRequestID()
            utils.log("INFO","Generated uuid is: " + uuid,  isDebugEnabled)
            utils.log("INFO","URL to be used is: " + url,  isDebugEnabled)
            String userName = execution.getVariable("URN_aai_auth")
            String password = execution.getVariable("URN_mso_msoKey")
            String basicAuthCred = utils.getBasicAuth(userName,password)
            RESTConfig config = new RESTConfig(url);
            RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Content-Type", "application/xml").addHeader("Accept","application/xml");
            if (basicAuthCred != null && !"".equals(basicAuthCred)) {
                client.addAuthorizationHeader(basicAuthCred)
            }
            apiResponse = client.httpDelete(payload)
            utils.log("INFO","======== Completed Execute AAI Delete Process ======== ",  isDebugEnabled)
        }catch(Exception e){
            utils.log("ERROR","Exception occured while executing AAI Put Call. Exception is: \n" + e,  isDebugEnabled)
            throw new BpmnError("MSOWorkflowException")
        }
        return apiResponse
    }

    /**
     * delete NS task
     */
    public void deleteNetworkService(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", " *** deleteNetworkService  start *** ", isDebugEnabled)
        String nsOperationKey = execution.getVariable("nsOperationKey");
        String url = host + vfcUrl + "/ns/" + execution.getVariable("nsInstanceId") 
        APIResponse apiResponse = deleteRequest(execution, url, nsOperationKey)
        String returnCode = apiResponse.getStatusCode()
        String apiResponseAsString = apiResponse.getResponseBodyAsString()
        String operationStatus = "error";
        if(returnCode== "200" || returnCode== "202"){
            operationStatus = "finished"
        }
        execution.setVariable("operationStatus", operationStatus)
        
        utils.log("INFO", " *** deleteNetworkService  end *** ", isDebugEnabled)
    }

    /**
     * instantiate NS task
     */
    public void terminateNetworkService(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", " *** terminateNetworkService  start *** ", isDebugEnabled)
        String nsOperationKey = execution.getVariable("nsOperationKey") 
        String url =  host + vfcUrl + "/ns/" + execution.getVariable("nsInstanceId") + "/terminate"
        APIResponse apiResponse = postRequest(execution, url, nsOperationKey)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String jobId = "";
        if(returnCode== "200" || returnCode== "202"){
            jobId =  jsonUtil.getJsonValue(aaiResponseAsString, "jobId")
        }
        execution.setVariable("jobId", jobId)   
        utils.log("INFO", " *** terminateNetworkService  end *** ", isDebugEnabled)
    }

    /**
     * query NS task
     */
    public void queryNSProgress(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", " *** queryNSProgress  start *** ", isDebugEnabled)
        String jobId = execution.getVariable("jobId")
        String nsOperationKey = execution.getVariable("nsOperationKey");
        String url =  host + vfcUrl + "/jobs/" +  execution.getVariable("jobId") 
        APIResponse apiResponse = postRequest(execution, url, nsOperationKey)
        String returnCode = apiResponse.getStatusCode()
        String apiResponseAsString = apiResponse.getResponseBodyAsString()
        String operationProgress = "100"
        if(returnCode== "200"){
            operationProgress = jsonUtil.getJsonValue(apiResponseAsString, "responseDescriptor.progress")
        }
        execution.setVariable("operationProgress", operationProgress)
        utils.log("INFO", " *** queryNSProgress  end *** ", isDebugEnabled)
    }

    /**
     * delay 5 sec 
     */
    public void timeDelay(DelegateExecution execution) {
        try {
            Thread.sleep(5000);
        } catch(InterruptedException e) {           
            utils.log("INFO", "Time Delay exception" + e, isDebugEnabled)
        }
    }

    /**
     * finish NS task
     */
    public void finishNSDelete(DelegateExecution execution) {
        //no need to do anything util now
    }

    /**
     * post request
     * url: the url of the request
     * requestBody: the body of the request
     */
    private APIResponse postRequest(DelegateExecution execution, String url, String requestBody){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", " ======== Started Execute VFC adapter Post Process ======== ", isDebugEnabled)
        utils.log("INFO", "url:"+url +"\nrequestBody:"+ requestBody, isDebugEnabled)
        APIResponse apiResponse = null
        try{
            RESTConfig config = new RESTConfig(url);
            RESTClient client = new RESTClient(config).addHeader("Content-Type", "application/json").addHeader("Accept","application/json").addHeader("Authorization","Basic QlBFTENsaWVudDpwYXNzd29yZDEk");;
            apiResponse = client.httpPost(requestBody)
            utils.log("INFO", "response code:"+ apiResponse.getStatusCode() +"\nresponse body:"+ apiResponse.getResponseBodyAsString(), isDebugEnabled)
            utils.log("INFO", "======== Completed Execute VF-C adapter Post Process ======== ", isDebugEnabled)
        }catch(Exception e){
            utils.log("ERROR", "Exception occured while executing VF-C Post Call. Exception is: \n" + e, isDebugEnabled)
            throw new BpmnError("MSOWorkflowException")
        }        
        return apiResponse
    }
    /**
     * delete request
     * url: the url of the request
     * requestBody: the body of the request
     */
    private APIResponse deleteRequest(DelegateExecution execution, String url, String requestBody){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", " ======== Started Execute VFC adapter Delete Process ======== ", isDebugEnabled)       
        utils.log("INFO", "url:"+url +"\nrequestBody:"+ requestBody, isDebugEnabled)
        APIResponse apiResponse = null
        try{
            RESTConfig config = new RESTConfig(url);
            RESTClient client = new RESTClient(config).addHeader("Content-Type", "application/json").addHeader("Accept","application/json").addHeader("Authorization","Basic QlBFTENsaWVudDpwYXNzd29yZDEk");
            apiResponse = client.httpDelete(requestBody)
            utils.log("INFO", "response code:"+ apiResponse.getStatusCode() +"\nresponse body:"+ apiResponse.getResponseBodyAsString(), isDebugEnabled) 
            utils.log("INFO", "======== Completed Execute VF-C adapter Delete Process ======== ", isDebugEnabled) 
        }catch(Exception e){
            utils.log("ERROR", "Exception occured while executing VF-C Post Call. Exception is: \n" + e, isDebugEnabled) 
            throw new BpmnError("MSOWorkflowException")
        }        
        return apiResponse
    }
}
