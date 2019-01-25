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
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.logger.MsoLogger
import org.onap.so.utils.TargetEntity
import org.onap.so.bpmn.core.UrnPropertiesReader

import javax.ws.rs.core.Response
/**
 * This groovy class supports the <class>DoDeleteVFCNetworkServiceInstance.bpmn</class> process.
 * flow for E2E ServiceInstance Delete
 */
public class DoDeleteVFCNetworkServiceInstance extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoDeleteVFCNetworkServiceInstance.class);

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()
    private final HttpClientFactory httpClientFactory = new HttpClientFactory()

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

            String vfcAdapterUrl = UrnPropertiesReader.getVariable("mso.adapters.vfc.rest.endpoint", execution)
			
            if (vfcAdapterUrl == null || vfcAdapterUrl.isEmpty()) {
                msg = getProcessKey(execution) + ': mso:adapters:vfcc:rest:endpoint URN mapping is not defined'
                msoLogger.debug(msg)
            }
 
            while (vfcAdapterUrl.endsWith('/')) {
                vfcAdapterUrl = vfcAdapterUrl.substring(0, vfcAdapterUrl.length()-1)
            }
			
            execution.setVariable("vfcAdapterUrl", vfcAdapterUrl)

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
        String vfcAdapterUrl = execution.getVariable("vfcAdapterUrl")
        String nsOperationKey = execution.getVariable("nsOperationKey");
        String url = vfcAdapterUrl + "/ns/" + execution.getVariable("nsInstanceId")
        Response apiResponse = deleteRequest(execution, url, nsOperationKey)
        String returnCode = apiResponse.getStatus()
        String operationStatus = "error";
        if(returnCode== "200" || returnCode== "202"){
            operationStatus = "finished"
        }
        execution.setVariable("operationStatus", operationStatus)

        msoLogger.trace("deleteNetworkService  end ")
    }

    /**
     *  terminate NS task
     */
    public void terminateNetworkService(DelegateExecution execution) {

        msoLogger.trace("terminateNetworkService  start ")
        String vfcAdapterUrl = execution.getVariable("vfcAdapterUrl")
        String nsOperationKey = execution.getVariable("nsOperationKey")
        String url =  vfcAdapterUrl + "/ns/" + execution.getVariable("nsInstanceId") + "/terminate"
        Response apiResponse = postRequest(execution, url, nsOperationKey)
        String returnCode = apiResponse.getStatus()
        String aaiResponseAsString = apiResponse.readEntity(String.class)
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
        String vfcAdapterUrl = execution.getVariable("vfcAdapterUrl")
        String jobId = execution.getVariable("jobId")
        String nsOperationKey = execution.getVariable("nsOperationKey");
        String url =  vfcAdapterUrl + "/jobs/" +  execution.getVariable("jobId")
        Response apiResponse = postRequest(execution, url, nsOperationKey)
        String returnCode = apiResponse.getStatus()
        String apiResponseAsString = apiResponse.readEntity(String.class)
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
    private Response postRequest(DelegateExecution execution, String urlString, String requestBody){

		msoLogger.trace("Started Execute VFC adapter Post Process ")
		msoLogger.info("url:"+urlString +"\nrequestBody:"+ requestBody)
		Response apiResponse = null
		try{
			URL url = new URL(urlString);

			// Get the Basic Auth credentials for the VFCAdapter, username is 'bpel', auth is '07a7159d3bf51a0e53be7a8f89699be7'
            // user 'bepl' authHeader is the same with mso.db.auth
            String basicAuthValuedb =  UrnPropertiesReader.getVariable("mso.db.auth", execution)
            HttpClient httpClient = httpClientFactory.newJsonClient(url, TargetEntity.VNF_ADAPTER)
            httpClient.addAdditionalHeader("Accept", "application/json")
            httpClient.addAdditionalHeader("Authorization", basicAuthValuedb)

			apiResponse = httpClient.post(requestBody)

			msoLogger.debug("response code:"+ apiResponse.getStatus() +"\nresponse body:"+ apiResponse.readEntity(String.class))

			msoLogger.trace("Completed Execute VF-C adapter Post Process ")
		}catch(Exception e){
            msoLogger.error("Exception occured while executing VF-C Post Call. Exception is: \n" + e.getMessage());
            throw new BpmnError("MSOWorkflowException")
        }
        return apiResponse
    }
    /**
     * delete request
     * url: the url of the request
     * requestBody: the body of the request
     */
    private Response deleteRequest(DelegateExecution execution, String url, String requestBody){

        msoLogger.trace("Started Execute VFC adapter Delete Process ")
        msoLogger.info("url:"+url +"\nrequestBody:"+ requestBody)
	Response r
        try{

		URL Url = new URL(url)
            // Get the Basic Auth credentials for the VFCAdapter, username is 'bpel', auth is '07a7159d3bf51a0e53be7a8f89699be7'
            // user 'bepl' authHeader is the same with mso.db.auth
            String basicAuthValuedb =  UrnPropertiesReader.getVariable("mso.db.auth", execution)
            HttpClient httpClient = httpClientFactory.newJsonClient(url, TargetEntity.VNF_ADAPTER)
            httpClient.addAdditionalHeader("Accept", "application/json")
            httpClient.addAdditionalHeader("Authorization", basicAuthValuedb)
            httpClient.addAdditionalHeader("Content-Type", "application/json")
            r = httpClient.delete(requestBody)
		
            msoLogger.trace("Completed Execute VF-C adapter Delete Process ")
        }catch(Exception e){
            msoLogger.error("Exception occured while executing VF-C Post Call. Exception is: \n" + e.getMessage());
            throw new BpmnError("MSOWorkflowException")
        }
        return r
    }
}
