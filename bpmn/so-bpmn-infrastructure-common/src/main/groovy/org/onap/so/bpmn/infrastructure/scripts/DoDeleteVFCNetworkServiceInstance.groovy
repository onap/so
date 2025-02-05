/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.core.UrnPropertiesReader

import javax.ws.rs.core.Response
/**
 * This groovy class supports the <class>DoDeleteVFCNetworkServiceInstance.bpmn</class> process.
 * flow for E2E ServiceInstance Delete
 */
public class DoDeleteVFCNetworkServiceInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoDeleteVFCNetworkServiceInstance.class);

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
        logger.trace("preProcessRequest() ")
        try {
            //deal with operation key
            String globalSubscriberId = execution.getVariable("globalSubscriberId")
            logger.info("globalSubscriberId:" + globalSubscriberId)
            String serviceType = execution.getVariable("serviceType")
            logger.info("serviceType:" + serviceType)
            String serviceId = execution.getVariable("serviceId")
            logger.info("serviceId:" + serviceId)
            String operationId = execution.getVariable("operationId")
            logger.info("serviceType:" + serviceType)
            String nodeTemplateUUID = execution.getVariable("resourceTemplateId")
            logger.info("nodeTemplateUUID:" + nodeTemplateUUID)
            String nsInstanceId = execution.getVariable("resourceInstanceId")
            logger.info("nsInstanceId:" + nsInstanceId)
            execution.setVariable("nsInstanceId",nsInstanceId)
            String nsOperationKey = """{
            "globalSubscriberId":"${globalSubscriberId}",
            "serviceType":"${serviceType}",
            "serviceId":"${serviceId}",
            "operationId":"${operationId}",
            "nodeTemplateUUID":"${nodeTemplateUUID}"
             }"""
            execution.setVariable("nsOperationKey", nsOperationKey);
            logger.info("nsOperationKey:" + nsOperationKey)

            String vfcAdapterUrl = UrnPropertiesReader.getVariable("mso.adapters.vfc.rest.endpoint", execution)
			
            if (vfcAdapterUrl == null || vfcAdapterUrl.isEmpty()) {
                msg = getProcessKey(execution) + ': mso:adapters:vfcc:rest:endpoint URN mapping is not defined'
                logger.debug(msg)
            }
 
            while (vfcAdapterUrl.endsWith('/')) {
                vfcAdapterUrl = vfcAdapterUrl.substring(0, vfcAdapterUrl.length()-1)
            }
			
            execution.setVariable("vfcAdapterUrl", vfcAdapterUrl)

        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.trace("Exit preProcessRequest ")
	}

    /**
     * unwind NS from AAI relationship
     */
    public void deleteNSRelationship(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        logger.info(" ***** deleteNSRelationship *****")
        String nsInstanceId = execution.getVariable("resourceInstanceId")
        if(nsInstanceId == null || nsInstanceId == ""){
            logger.info(" Delete NS failed")
            return
        }
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")
        String serviceId = execution.getVariable("serviceId")
        AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceId))
        AAIResourceUri nsServiceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(nsInstanceId))
        try {
            getAAIClient().disconnect(serviceInstanceUri, nsServiceInstanceUri)
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution,25000,"Exception occured while NS disconnect call: " + e.getMessage())
        }
        logger.info(" *****Exit deleteNSRelationship *****")
    }

    /**
     * delete NS task
     */
    public void deleteNetworkService(DelegateExecution execution) {

        logger.trace("deleteNetworkService  start ")
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

        logger.trace("deleteNetworkService  end ")
    }

    /**
     *  terminate NS task
     */
    public void terminateNetworkService(DelegateExecution execution) {

        logger.trace("terminateNetworkService  start ")
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
        logger.trace("terminateNetworkService  end ")
    }

    /**
     * query NS task
     */
    public void queryNSProgress(DelegateExecution execution) {

        logger.trace("queryNSProgress  start ")
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
        logger.trace("queryNSProgress  end ")
    }

    /**
     * delay 5 sec
     */
    public void timeDelay(DelegateExecution execution) {
        try {
            Thread.sleep(5000);
        } catch(InterruptedException e) {
            logger.info("Time Delay exception" + e)
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

		logger.trace("Started Execute VFC adapter Post Process ")
		logger.info("url:"+urlString +"\nrequestBody:"+ requestBody)
		Response apiResponse = null
		try{
			URL url = new URL(urlString);

			// Get the Basic Auth credentials for the VFCAdapter, username is 'bpel', auth is '07a7159d3bf51a0e53be7a8f89699be7'
            // user 'bepl' authHeader is the same with mso.db.auth
            String basicAuthValuedb =  UrnPropertiesReader.getVariable("mso.db.auth", execution)
            HttpClient httpClient = httpClientFactory.newJsonClient(url, ONAPComponents.VNF_ADAPTER)
            httpClient.addAdditionalHeader("Accept", "application/json")
            httpClient.addAdditionalHeader("Authorization", basicAuthValuedb)

			apiResponse = httpClient.post(requestBody)

			logger.debug("response code:"+ apiResponse.getStatus() +"\nresponse body:"+ apiResponse.readEntity(String.class))

			logger.trace("Completed Execute VF-C adapter Post Process ")
		}catch(Exception e){
            logger.error("Exception occured while executing VF-C Post Call. Exception is: \n" + e.getMessage());
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

        logger.trace("Started Execute VFC adapter Delete Process ")
        logger.info("url:"+url +"\nrequestBody:"+ requestBody)
	Response r
        try{

		URL Url = new URL(url)
            // Get the Basic Auth credentials for the VFCAdapter, username is 'bpel', auth is '07a7159d3bf51a0e53be7a8f89699be7'
            // user 'bepl' authHeader is the same with mso.db.auth
            String basicAuthValuedb =  UrnPropertiesReader.getVariable("mso.db.auth", execution)
            HttpClient httpClient = httpClientFactory.newJsonClient(url, ONAPComponents.VNF_ADAPTER)
            httpClient.addAdditionalHeader("Accept", "application/json")
            httpClient.addAdditionalHeader("Authorization", basicAuthValuedb)
            httpClient.addAdditionalHeader("Content-Type", "application/json")
            r = httpClient.delete(requestBody)
		
            logger.trace("Completed Execute VF-C adapter Delete Process ")
        }catch(Exception e){
            logger.error("Exception occured while executing VF-C Post Call. Exception is: \n" + e.getMessage());
            throw new BpmnError("MSOWorkflowException")
        }
        return r
    }
}
