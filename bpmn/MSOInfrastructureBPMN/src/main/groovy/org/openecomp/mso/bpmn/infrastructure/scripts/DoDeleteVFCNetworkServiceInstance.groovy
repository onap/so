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

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor 
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil 
import org.openecomp.mso.bpmn.core.WorkflowException 
import org.openecomp.mso.bpmn.core.json.JsonUtils 
import org.openecomp.mso.rest.APIResponse

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError 
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils 
import org.openecomp.mso.rest.RESTClient 
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.rest.APIResponse;

/**
 * This groovy class supports the <class>DODeleteVFCNetworkServiceInstance.bpmn</class> process.
 * flow for E2E ServiceInstance Delete
 */
public class DODeleteVFCNetworkServiceInstance extends AbstractServiceTaskProcessor {

    String deleteUrl = "/vfc/vfcadapters/v1/ns/{nsInstanceId}"
            
    String terminateUrl = "/vfcvfcadatpers/v1/ns/{nsInstanceId}/terminate"
    
    String queryJobUrl = "/vfc/vfcadatpers/v1/jobs/{jobId}"
    
    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     */
    public void preProcessRequest (Execution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        String msg = ""
        utils.log("DEBUG", " *** preProcessRequest() *** ", isDebugEnabled)
        try {
            //deal with operation key
            String globalSubscriberId = execution.getVariable("globalSubscriberId")
            utils.log("DEBUG", "globalSubscriberId:" + globalSubscriberId, isDebugEnabled)
            String serviceType = execution.getVariable("serviceType")
            utils.log("DEBUG", "serviceType:" + serviceType, isDebugEnabled)
            String serviceId = execution.getVariable("serviceId")
            utils.log("DEBUG", "serviceId:" + serviceId, isDebugEnabled)
            String operationId = execution.getVariable("operationId")
            utils.log("DEBUG", "serviceType:" + serviceType, isDebugEnabled)
            String nodeTemplateUUID = execution.getVariable("resourceTemplateUUID")
            utils.log("DEBUG", "nodeTemplateUUID:" + nodeTemplateUUID, isDebugEnabled)
            String nsInstanceId = execution.getVariable("resourceInstanceId")
            utils.log("DEBUG", "nsInstanceId:" + nsInstanceId, isDebugEnabled)
            String nsOperationKey = "{\"globalSubscriberId\":\"" + globalSubscriberId + "\",\"serviceType:\""
                  + serviceType + "\",\"serviceId\":\"" + serviceId + "\",\"operationId\":\"" + operationId
                  +"\",\"nodeTemplateUUID\":\"" + nodeTemplateUUID + "\"}";
            execution.setVariable("nsOperationKey", nsOperationKey);
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            utils.log("DEBUG", msg, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        utils.log("DEBUG"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

    /**
     * delete NS task
     */
    public void deleteNetworkService(Execution execution) {
        String nsOperationKey = excution.getVariable("nsOperationKey");
        String url = deleteUrl.replaceAll("{nsInstanceId}", execution.getVariable("nsInstanceId")) 
        APIResponse apiResponse = deleteRequest(url, reqBody)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String operationStatus = "error";
        if(returnCode== "200"){
            operationStatus = "finished"
        }
        execution.setVariable("operationStatus", operationStatus)
    }

    /**
     * instantiate NS task
     */
    public void terminateNetworkService(Execution execution) {
        String nsOperationKey = execution.getVariable("nsOperationKey") 
        String url = terminateUrl.replaceAll("{nsInstanceId}", execution.getVariable("nsInstanceId")) 
        APIResponse apiResponse = postRequest(url, reqBody)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String jobId = "";
        if(returnCode== "200"){
            jobId =  jsonUtil.getJsonValue(aaiResponseAsString, "jobId")
        }
        execution.setVariable("jobId", nsInstanceId)   
    }

    /**
     * query NS task
     */
    public void queryNSProgress(Execution execution) {
        String jobId = execution.getVariable("jobId")
        String nsOperationKey = excution.getVariable("nsOperationKey");
        String url = queryJobUrl.replaceAll("{jobId}", execution.getVariable("jobId")) 
        APIResponse createRsp = postRequest(url, nsOperationKey)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String operationProgress = "100"
        if(returnCode== "200"){
            operationProgress = jsonUtil.getJsonValue(aaiResponseAsString, "responseDescriptor.progress")
        }
        exection.setVariable("operationProgress", operationProgress)
    }

    /**
     * delay 5 sec 
     */
    public void timeDelay(Execution execution) {
        try {
            Thread.sleep(5000);
        } catch(InterruptedException e) {           
            taskProcessor.utils.log("ERROR", "Time Delay exception" + e , isDebugEnabled)
        }
    }

    /**
     * finish NS task
     */
    public void finishNSDelete(Execution execution) {
        //no need to do anything util now
    }

    /**
     * post request
     * url: the url of the request
     * requestBody: the body of the request
     */
    private APIResponse postRequest(String url, String requestBody){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        taskProcessor.logDebug( " ======== Started Execute VFC adapter Post Process ======== ", isDebugEnabled)
        taskProcessor.logDebug( "url:"+url +"\nrequestBody:"+ requestBody, isDebugEnabled)
        APIResponse apiResponse = null
        try{
            RESTConfig config = new RESTConfig(url);
            RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/json");
            apiResponse = client.httpPost(requestBody)
            taskProcessor.logDebug( "response code:"+ apiResponse.getStatusCode() +"\nresponse body:"+ apiResponse.getResponseBodyAsString(), isDebugEnabled)
            taskProcessor.logDebug( "======== Completed Execute VF-C adapter Post Process ======== ", isDebugEnabled)
        }catch(Exception e){
            taskProcessor.utils.log("ERROR", "Exception occured while executing VF-C Post Call. Exception is: \n" + e, isDebugEnabled)
            throw new BpmnError("MSOWorkflowException")
        }        
        return apiResponse
    }
    /**
     * delete request
     * url: the url of the request
     * requestBody: the body of the request
     */
    private APIResponse deleteRequest(String url, String requestBody){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        taskProcessor.logDebug( " ======== Started Execute VFC adapter Delete Process ======== ", isDebugEnabled)
        taskProcessor.logDebug( "url:"+url +"\nrequestBody:"+ requestBody, isDebugEnabled)
        APIResponse apiResponse = null
        try{
            RESTConfig config = new RESTConfig(url);
            RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/json");
            apiResponse = client.httpDelete(requestBody)
            taskProcessor.logDebug( "response code:"+ apiResponse.getStatusCode() +"\nresponse body:"+ apiResponse.getResponseBodyAsString(), isDebugEnabled)
            taskProcessor.logDebug( "======== Completed Execute VF-C adapter Delete Process ======== ", isDebugEnabled)
        }catch(Exception e){
            taskProcessor.utils.log("ERROR", "Exception occured while executing VF-C Post Call. Exception is: \n" + e, isDebugEnabled)
            throw new BpmnError("MSOWorkflowException")
        }        
        return apiResponse
    }
}
