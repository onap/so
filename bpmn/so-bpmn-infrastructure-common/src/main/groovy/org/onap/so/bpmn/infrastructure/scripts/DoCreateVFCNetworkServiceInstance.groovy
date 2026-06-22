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

import org.onap.so.logger.LoggingAnchor
import org.onap.so.client.HttpClientFactory
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.bpmn.core.UrnPropertiesReader

import org.onap.logging.filter.base.ONAPComponents;

import jakarta.ws.rs.core.Response

/**
 * This groovy class supports the <class>DoCreateVFCNetworkServiceInstance.bpmn</class> process.
 * flow for VFC Network Service Create
 */
public class DoCreateVFCNetworkServiceInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoCreateVFCNetworkServiceInstance.class);


    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */
    void preProcessRequest (DelegateExecution execution) {
       String msg = ""
       logger.trace("preProcessRequest()")
       try {
           //deal with nsName and Description
           String nsServiceName = execution.getVariable("nsServiceName")
           String nsServiceDescription = execution.getVariable("nsServiceDescription")
           logger.debug("nsServiceName:" + nsServiceName + " nsServiceDescription:" + nsServiceDescription)
           //deal with operation key
           String globalSubscriberId = execution.getVariable("globalSubscriberId")
           logger.debug("globalSubscriberId:" + globalSubscriberId)
           String serviceType = execution.getVariable("serviceType")
           logger.debug("serviceType:" + serviceType)
           String serviceId = execution.getVariable("serviceId")
           logger.debug("serviceId:" + serviceId)
           String operationId = execution.getVariable("operationId")
           logger.debug("serviceType:" + serviceType)
           String nodeTemplateUUID = execution.getVariable("resourceUUID")
           logger.debug("nodeTemplateUUID:" + nodeTemplateUUID)
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
           logger.debug("nsParameters:" + nsParameters)
           String nsOperationKey = """{
                   "globalSubscriberId":"${globalSubscriberId}",
                   "serviceType":"${serviceType}",
                   "serviceId":"${serviceId}",
                   "operationId":"${operationId}",
                   "nodeTemplateUUID":"${nodeTemplateUUID}"
                    }"""
           execution.setVariable("nsOperationKey", nsOperationKey);
           execution.setVariable("nsParameters", nsParameters)

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
           logger.debug(msg)
           exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
       }
       logger.trace("Exit preProcessRequest")
	}

    /**
     * create NS task
     */
    void createNetworkService(DelegateExecution execution) {
        logger.trace("createNetworkService")
        String vfcAdapterUrl = execution.getVariable("vfcAdapterUrl")
        String nsOperationKey = execution.getVariable("nsOperationKey");
        String nsParameters = execution.getVariable("nsParameters");
        String nsServiceName = execution.getVariable("nsServiceName")
        String nsServiceDescription = execution.getVariable("nsServiceDescription")
        String reqBody ="""{
                "nsServiceName":"${nsServiceName}",
                "nsServiceDescription":"${nsServiceDescription}",
                "nsOperationKey":${nsOperationKey},
                "nsParameters":${nsParameters}
               }"""
        Response apiResponse = postRequest(execution, vfcAdapterUrl + "/ns", reqBody)
        String returnCode = apiResponse.getStatus()
        String aaiResponseAsString = apiResponse.readEntity(String.class)
        String nsInstanceId = "";
        if(returnCode== "200" || returnCode == "201"){
            nsInstanceId =  jsonUtil.getJsonValue(aaiResponseAsString, "nsInstanceId")
        }
        execution.setVariable("nsInstanceId", nsInstanceId)
        logger.trace("Exit  createNetworkService")
    }

    /**
     * instantiate NS task
     */
    void instantiateNetworkService(DelegateExecution execution) {
        logger.trace("instantiateNetworkService")
        String vfcAdapterUrl = execution.getVariable("vfcAdapterUrl")
        String nsOperationKey = execution.getVariable("nsOperationKey");
        String nsParameters = execution.getVariable("nsParameters");
        String nsServiceName = execution.getVariable("nsServiceName")
        String nsServiceDescription = execution.getVariable("nsServiceDescription")
        String reqBody ="""{
        "nsServiceName":"${nsServiceName}",
        "nsServiceDescription":"${nsServiceDescription}",
        "nsOperationKey":${nsOperationKey},
        "nsParameters":${nsParameters}
       }"""
        String nsInstanceId = execution.getVariable("nsInstanceId")
        String url = vfcAdapterUrl + "/ns/" +nsInstanceId + "/instantiate"
        Response apiResponse = postRequest(execution, url, reqBody)
        String returnCode = apiResponse.getStatus()
        String aaiResponseAsString = apiResponse.readEntity(String.class)
        String jobId = "";
        if(returnCode== "200"|| returnCode == "201"){
            jobId =  jsonUtil.getJsonValue(aaiResponseAsString, "jobId")
        }
        execution.setVariable("jobId", jobId)
        logger.trace("Exit  instantiateNetworkService")
    }

    /**
     * query NS task
     */
    void queryNSProgress(DelegateExecution execution) {
        logger.trace("queryNSProgress")
        String vfcAdapterUrl = execution.getVariable("vfcAdapterUrl")
        String jobId = execution.getVariable("jobId")
        String nsOperationKey = execution.getVariable("nsOperationKey");
        String url = vfcAdapterUrl + "/jobs/" + jobId
        Response apiResponse = postRequest(execution, url, nsOperationKey)
        String returnCode = apiResponse.getStatus()
        String aaiResponseAsString = apiResponse.readEntity(String.class)
        String operationStatus = "error"
        if(returnCode== "200"|| returnCode == "201"){
            operationStatus = jsonUtil.getJsonValue(aaiResponseAsString, "responseDescriptor.status")
        }
        execution.setVariable("operationStatus", operationStatus)
        logger.trace("Exit  queryNSProgress")
    }

    /**
     * delay 5 sec
     */
    void timeDelay(DelegateExecution execution) {
        try {
            Thread.sleep(5000);
        } catch(InterruptedException e) {
           logger.debug("Time Delay exception" + e )
        }
    }

    /**
     * finish NS task
     */
    void addNSRelationship(DelegateExecution execution) {
        logger.trace("addNSRelationship")
        String nsInstanceId = execution.getVariable("nsInstanceId")
        if(nsInstanceId == null || nsInstanceId == ""){
            logger.debug(" create NS failed, so do not need to add relationship")
            return
        }
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")
        String serviceId = execution.getVariable("serviceId")

        AAIResourceUri nsUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(nsInstanceId))
        AAIResourceUri relatedServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceId))

        try{
            getAAIClient().connect(nsUri,relatedServiceUri)
            logger.info("NS relationship to Service added successfully")
        }catch(Exception e){
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception occured while executing AAI Put Call", "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
            throw new BpmnError("MSOWorkflowException")
        }
    }

    /**
     * post request
     * url: the url of the request
     * requestBody: the body of the request
     */
    private Response postRequest(DelegateExecution execution, String urlString, String requestBody){
        logger.trace("Started Execute VFC adapter Post Process")
        logger.debug("url:"+urlString +"\nrequestBody:"+ requestBody)
        Response apiResponse = null
        try{

			URL url = new URL(urlString);
            
            // Get the Basic Auth credentials for the VFCAdapter, username is 'bpel', auth is '07a7159d3bf51a0e53be7a8f89699be7'
            // user 'bepl' authHeader is the same with mso.db.auth
            String basicAuthValuedb =  UrnPropertiesReader.getVariable("mso.db.auth", execution)
            HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.VNF_ADAPTER)
            httpClient.addAdditionalHeader("Accept", "application/json")
            httpClient.addAdditionalHeader("Authorization", basicAuthValuedb)

            apiResponse = httpClient.post(requestBody)

            logger.debug("response code:"+ apiResponse.getStatus() +"\nresponse body:"+ apiResponse.readEntity(String.class))
            logger.trace("Completed Execute VF-C adapter Post Process")
        }catch(Exception e){
			logger.error("Exception occured while executing VFC Adapter Post Call"  + e.getMessage ());
            throw new BpmnError("MSOWorkflowException")
        }
        return apiResponse
    }
}
