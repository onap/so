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

package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig

/**
 * This groovy class supports the <class>DoDeleteVFCNetworkServiceInstance.bpmn</class> process.
 * flow for E2E ServiceInstance Delete
 */
public class DoDeleteVFCNetworkServiceInstance extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoDeleteVFCNetworkServiceInstance.class);

            
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

        String msg = ""
        msoLogger.trace("preProcessRequest() ")
        try {
            //deal with operation key
            String globalSubscriberId = execution.getVariable("globalSubscriberId")
            msoLogger.info("globalSubscriberId:" + globalSubscriberId)
            String serviceType = execution.getVariable("serviceType")
            msoLogger.info("serviceType:" + serviceType)
            String serviceId = execution.getVariable("serviceId")
            msoLogger.info("serviceId:" + serviceId)
            String operationId = execution.getVariable("operationId")
            msoLogger.info("serviceType:" + serviceType)
            String nodeTemplateUUID = execution.getVariable("resourceTemplateId")
            msoLogger.info("nodeTemplateUUID:" + nodeTemplateUUID)
            String nsInstanceId = execution.getVariable("resourceInstanceId")
            msoLogger.info("nsInstanceId:" + nsInstanceId)
            execution.setVariable("nsInstanceId",nsInstanceId)
            String nsOperationKey = """{
            "globalSubscriberId":"${globalSubscriberId}",
            "serviceType":"${serviceType}",
            "serviceId":"${serviceId}",
            "operationId":"${operationId}",
            "nodeTemplateUUID":"${nodeTemplateUUID}"
             }"""
            execution.setVariable("nsOperationKey", nsOperationKey);
            msoLogger.info("nsOperationKey:" + nsOperationKey)
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            msoLogger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        msoLogger.trace("Exit preProcessRequest ")
	}

    /**
     * unwind NS from AAI relationship
     */
    public void deleteNSRelationship(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** deleteNSRelationship *****",  isDebugEnabled)
        String nsInstanceId = execution.getVariable("resourceInstanceId")
        if(nsInstanceId == null || nsInstanceId == ""){
            utils.log("INFO"," Delete NS failed",  isDebugEnabled)
            return
        }
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")
        String serviceId = execution.getVariable("serviceId")
        AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, serviceType, serviceId)
        AAIResourceUri nsServiceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, serviceType, nsInstanceId)
        try {
            getAAIClient().disconnect(serviceInstanceUri, nsServiceInstanceUri)
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution,25000,"Exception occured while NS disconnect call: " + e.getMessage())
        }
        utils.log("INFO"," *****Exit deleteNSRelationship *****",  isDebugEnabled)
    }

    /**
     * delete NS task
     */
    public void deleteNetworkService(DelegateExecution execution) {

        msoLogger.trace("deleteNetworkService  start ")
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
        
        msoLogger.trace("deleteNetworkService  end ")
    }

    /**
     * instantiate NS task
     */
    public void terminateNetworkService(DelegateExecution execution) {

        msoLogger.trace("terminateNetworkService  start ")
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
        msoLogger.trace("terminateNetworkService  end ")
    }

    /**
     * query NS task
     */
    public void queryNSProgress(DelegateExecution execution) {

        msoLogger.trace("queryNSProgress  start ")
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
        msoLogger.trace("queryNSProgress  end ")
    }

    /**
     * delay 5 sec 
     */
    public void timeDelay(DelegateExecution execution) {
        try {
            Thread.sleep(5000);
        } catch(InterruptedException e) {           
            msoLogger.info("Time Delay exception" + e)
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

        msoLogger.trace("Started Execute VFC adapter Post Process ")
        msoLogger.info("url:"+url +"\nrequestBody:"+ requestBody)
        APIResponse apiResponse = null
        try{
            RESTConfig config = new RESTConfig(url);
            RESTClient client = new RESTClient(config).addHeader("Content-Type", "application/json").addHeader("Accept","application/json").addHeader("Authorization","Basic QlBFTENsaWVudDpwYXNzd29yZDEk");;
            apiResponse = client.httpPost(requestBody)
            msoLogger.info("response code:"+ apiResponse.getStatusCode() +"\nresponse body:"+ apiResponse.getResponseBodyAsString())
            msoLogger.trace("Completed Execute VF-C adapter Post Process ")
        }catch(Exception e){
            msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception occured while executing VF-C Post Call. Exception is: \n" + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
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

        msoLogger.trace("Started Execute VFC adapter Delete Process ")       
        msoLogger.info("url:"+url +"\nrequestBody:"+ requestBody)
        APIResponse apiResponse = null
        try{
            RESTConfig config = new RESTConfig(url);
            RESTClient client = new RESTClient(config).addHeader("Content-Type", "application/json").addHeader("Accept","application/json").addHeader("Authorization","Basic QlBFTENsaWVudDpwYXNzd29yZDEk");
            apiResponse = client.httpDelete(requestBody)
            msoLogger.info("response code:"+ apiResponse.getStatusCode() +"\nresponse body:"+ apiResponse.getResponseBodyAsString()) 
            msoLogger.trace("Completed Execute VF-C adapter Delete Process ") 
        }catch(Exception e){
            msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception occured while executing VF-C Post Call. Exception is: \n" + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e); 
            throw new BpmnError("MSOWorkflowException")
        }        
        return apiResponse
    }
}
