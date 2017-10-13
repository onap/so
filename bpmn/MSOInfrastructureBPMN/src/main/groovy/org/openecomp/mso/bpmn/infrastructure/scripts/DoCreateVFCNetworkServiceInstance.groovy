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
 * This groovy class supports the <class>DoCreateVFCNetworkServiceInstance.bpmn</class> process.
 * flow for VFC Network Service Create
 */
public class DoCreateVFCNetworkServiceInstance extends AbstractServiceTaskProcessor {

    String createUrl = "/vfc/vfcadapters/v1/ns"
            
    String instantiateUrl = "/vfcvfcadatpers/v1/ns/{nsInstanceId}/instantiate"
    
    String queryJobUrl = "/vfc/vfcadatpers/v1/jobs/{jobId}"
    
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
       utils.log("DEBUG", " *** preProcessRequest() *** ", isDebugEnabled)
       try {
           //deal with nsName and Description
           String nsServiceName = execution.getVariable("nsServiceName")
           String nsServiceDescription = execution.getVariable("nsServiceDescription")
           utils.log("DEBUG", "nsServiceName:" + nsServiceName + " nsServiceDescription:" + nsServiceDescription, isDebugEnabled)
           //deal with operation key
           String globalSubscriberId = execution.getVariable("globalSubscriberId")
           utils.log("DEBUG", "globalSubscriberId:" + globalSubscriberId, isDebugEnabled)
           String serviceType = execution.getVariable("serviceType")
           utils.log("DEBUG", "serviceType:" + serviceType, isDebugEnabled)
           String serviceId = execution.getVariable("serviceId")
           utils.log("DEBUG", "serviceId:" + serviceId, isDebugEnabled)
           String operationId = execution.getVariable("operationId")
           utils.log("DEBUG", "serviceType:" + serviceType, isDebugEnabled)
           String nodeTemplateUUID = execution.getVariable("resourceUUID")
           utils.log("DEBUG", "nodeTemplateUUID:" + nodeTemplateUUID, isDebugEnabled)
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
           String nsParameters = execution.getVariable("resourceParamters")
           utils.log("DEBUG", "Input Request:" + siRequest, isDebugEnabled)
           String nsOperationKey = "{\"globalSubscriberId\":\"" + globalSubscriberId + "\",\"serviceType:\""
                 + serviceType + "\",\"serviceId\":\"" + serviceId + "\",\"operationId\":\"" + operationId
                 +"\",\"nodeTemplateUUID\":\"" + nodeTemplateUUID + "\"}";
           execution.setVariable("nsOperationKey", nsOperationKey);
           execution.setVariable("nsParameters", nsParameters)
           

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
     * create NS task
     */
    public void createNetworkService(Execution execution) {
        
        String nsOperationKey = excution.getVariable("nsOperationKey");
        String nsParameters = excution.getVariable("nsParameters");
        String nsServiceName = execution.getVariable("nsServiceName")
        String nsServiceDescription = execution.getVariable("nsServiceDescription")
        String reqBody = "{\"nsServiceName\":\"" + nsServiceName + "\",\"nsServiceDescription\":\"" + nsServiceDescription
              +"\",\"nsOperationKey\":" + nsOperationKey + ",\"nsParameters\":" + nsParameters
        APIResponse apiResponse = postRequest(createUrl, reqBody)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String nsInstanceId = "";
        if(returnCode== "200"){
            nsInstanceId =  jsonUtil.getJsonValue(aaiResponseAsString, "nsInstanceId")
        }
        execution.setVariable("nsInstanceId", nsInstanceId)
        
    }

    /**
     * instantiate NS task
     */
    public void instantiateNetworkService(Execution execution) {
        String nsOperationKey = excution.getVariable("nsOperationKey");
        String nsParameters = excution.getVariable("nsParameters");
        String nsServiceName = execution.getVariable("nsServiceName")
        String nsServiceDescription = execution.getVariable("nsServiceDescription")
        String reqBody = "{\"nsServiceName\":\"" + nsServiceName + "\",\"nsServiceDescription\":\"" + nsServiceDescription
              +"\",\"nsOperationKey\":" + nsOperationKey + ",\"nsParameters\":" + nsParameters
        String url = instantiateUrl.replaceAll("{nsInstanceId}", execution.getVariable("nsInstanceId")) 
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
        APIResponse apiResponse = postRequest(url, nsOperationKey)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String operationStatus = "error"
        if(returnCode== "200"){
            operationStatus = jsonUtil.getJsonValue(aaiResponseAsString, "responseDescriptor.status")
        }
        exection.setVariable("operationStatus", operationStatus)
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
    public void finishNSCreate(Execution execution) {
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
            taskProcessor.utils.log("ERROR", "Exception occured while executing AAI Post Call. Exception is: \n" + e, isDebugEnabled)
            throw new BpmnError("MSOWorkflowException")
        }        
        return apiResponse
    }
}
