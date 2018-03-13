/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
 * This groovy class supports the <class>CreateSDNCCNetworkResource.bpmn</class> process.
 * flow for SDNC Network Resource Create
 */
public class CreateSDNCCNetworkResource extends AbstractServiceTaskProcessor {

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
           execution.setVariable("nsOperationKey", nsOperationKey);
           execution.setVariable("nsParameters", nsParameters)
           

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
            RESTConfig config = new RESTConfig(url);
            RESTClient client = new RESTClient(config).addHeader("Content-Type", "application/json").addHeader("Accept","application/json").addHeader("Authorization","Basic QlBFTENsaWVudDpwYXNzd29yZDEk");
            apiResponse = client.httpPost(requestBody)
            utils.log("INFO","response code:"+ apiResponse.getStatusCode() +"\nresponse body:"+ apiResponse.getResponseBodyAsString(),  isDebugEnabled)    
            utils.log("INFO","======== Completed Execute VF-C adapter Post Process ======== ",  isDebugEnabled)
        }catch(Exception e){
            utils.log("ERROR","Exception occured while executing AAI Post Call. Exception is: \n" + e,  isDebugEnabled)
            throw new BpmnError("MSOWorkflowException")
        }        
        return apiResponse
    }
    
    public void postCreateSDNCCall(Execution execution){
        
    }
}
