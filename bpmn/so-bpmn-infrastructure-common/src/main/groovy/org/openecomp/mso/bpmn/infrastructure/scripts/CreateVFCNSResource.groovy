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
import org.openecomp.mso.logger.MsoLogger

import org.openecomp.mso.logger.MessageEnum


import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError 
import org.camunda.bpm.engine.delegate.DelegateExecution
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
public class CreateVFCNSResource extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CreateVFCNSResource.class);


    String vfcUrl = "/vfc/rest/v1/vfcadapter"
            
    String host = "http://mso.mso.testlab.openecomp.org:8080"
    
    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    /**
     * CreateVFCNSResource
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */
    public void preProcessRequest (DelegateExecution execution) {
        JsonUtils jsonUtil = new JsonUtils()

       String msg = ""
       msoLogger.trace("preProcessRequest() ")
       try {
           //deal with nsName and Description
           String resourceInput = execution.getVariable("resourceInput")

           String resourceParameters = jsonUtil.getJsonValue(resourceInput, "resourceParameters")
           // get service name
           String resourceName = jsonUtil.getJsonValue(resourceInput, "resourceInstanceName")
           execution.setVariable("nsServiceName", resourceName)

           String nsServiceDescription = execution.getVariable("nsServiceDescription")
           msoLogger.info("nsServiceName:" + resourceName + " nsServiceDescription:" + nsServiceDescription)
           //deal with operation key
           String globalSubscriberId = jsonUtil.getJsonValue(resourceInput, "globalSubscriberId")
           msoLogger.info("globalSubscriberId:" + globalSubscriberId)
           //set local globalSubscriberId variable
           execution.setVariable("globalSubscriberId", globalSubscriberId);
           String serviceType = execution.getVariable("serviceType")
           msoLogger.info("serviceType:" + serviceType)

           String serviceId = execution.getVariable("serviceInstanceId")
           msoLogger.info("serviceId:" + serviceId)

           String operationId = jsonUtil.getJsonValue(resourceInput, "operationId")
           msoLogger.info("serviceType:" + serviceType)

           String nodeTemplateUUID = jsonUtil.getJsonValue(resourceInput, "resourceModelInfo.modelCustomizationUuid")
           String nsServiceModelUUID = jsonUtil.getJsonValue(resourceParameters, "requestInputs.nsd0_providing_service_uuid")
           msoLogger.info("nodeTemplateUUID:" + nodeTemplateUUID)
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
           String nsParameters = jsonUtil.getJsonValue(resourceInput, "resourceParameters")
           msoLogger.info("nsParameters:" + nsParameters)
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
           msoLogger.info(msg)
           exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
       }
       msoLogger.trace("Exit preProcessRequest ")
	}

    /**
     * create NS task
     */
    public void createNetworkService(DelegateExecution execution) {
        msoLogger.trace("createNetworkService ")
        String nsOperationKey = execution.getVariable("nsOperationKey");
        String nsServiceModelUUID = execution.getVariable("nsServiceModelUUID");
        String nsParameters = execution.getVariable("nsParameters");
        String nsServiceName = execution.getVariable("nsServiceName")
        String nsServiceDescription = execution.getVariable("nsServiceDescription")
        String locationConstraints = jsonUtil.getJsonValue(nsParameters, "locationConstraints")
        String requestInputs = jsonUtil.getJsonValue(nsParameters, "requestInputs")
        String reqBody ="""{
                "nsServiceName":"${nsServiceName}",
                "nsServiceDescription":"${nsServiceDescription}",
                "nsServiceModelUUID":"${nsServiceModelUUID}",
                "nsOperationKey":${nsOperationKey},
                "nsParameters":{
                     "locationConstraints":${locationConstraints},
                     "additionalParamForNs":${requestInputs}
                }
               }"""
        APIResponse apiResponse = postRequest(execution, host + vfcUrl + "/ns", reqBody)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String nsInstanceId = "";
        if(returnCode== "200" || returnCode == "201"){
            nsInstanceId =  jsonUtil.getJsonValue(aaiResponseAsString, "nsInstanceId")
        }
        execution.setVariable("nsInstanceId", nsInstanceId)
        msoLogger.info(" *****Exit  createNetworkService *****")
    }

    /**
     * instantiate NS task
     */
    public void instantiateNetworkService(DelegateExecution execution) {
        msoLogger.trace("instantiateNetworkService ")
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
        String url = host + vfcUrl + "/ns/" +nsInstanceId + "/instantiate"
        APIResponse apiResponse = postRequest(execution, url, reqBody)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String jobId = "";
        if(returnCode== "200"|| returnCode == "201"){
            jobId =  jsonUtil.getJsonValue(aaiResponseAsString, "jobId")
        }
        execution.setVariable("jobId", jobId)
        msoLogger.info(" *****Exit  instantiateNetworkService *****")
    }

    /**
     * query NS task
     */
    public void queryNSProgress(DelegateExecution execution) {
        msoLogger.trace("queryNSProgress ")
        String jobId = execution.getVariable("jobId")
        String nsOperationKey = execution.getVariable("nsOperationKey");
        String url = host + vfcUrl + "/jobs/" + jobId
        APIResponse apiResponse = postRequest(execution, url, nsOperationKey)
        String returnCode = apiResponse.getStatusCode()
        String aaiResponseAsString = apiResponse.getResponseBodyAsString()
        String operationStatus = "error"
        if(returnCode== "200"|| returnCode == "201"){
            operationStatus = jsonUtil.getJsonValue(aaiResponseAsString, "responseDescriptor.status")
        }
        execution.setVariable("operationStatus", operationStatus)
        msoLogger.info(" *****Exit  queryNSProgress *****")
    }

    /**
     * delay 5 sec 
     */
    public void timeDelay(DelegateExecution execution) {
        try {
            Thread.sleep(5000);
        } catch(InterruptedException e) {           
            msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Time Delay exception" + e , "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
        }
    }

    /**
     * finish NS task
     */
    public void addNSRelationship(DelegateExecution execution) {
        msoLogger.trace("addNSRelationship ")
        String nsInstanceId = execution.getVariable("nsInstanceId")
        if(nsInstanceId == null || nsInstanceId == ""){
            msoLogger.info(" create NS failed, so do not need to add relationship")
            return
        }
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")
        String serviceId = execution.getVariable("serviceInstanceId")
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
        msoLogger.info("Add Relationship req:\n" + addRelationPayload)
        String url = endpoint + "/aai/v11/business/customers/customer/" + globalSubscriberId + "/service-subscriptions/service-subscription/" + serviceType + "/service-instances/service-instance/" + serviceId + "/relationship-list/relationship"
        APIResponse aaiRsp = executeAAIPutCall(execution, url, addRelationPayload)
        msoLogger.info("aai response status code:" + aaiRsp.getStatusCode())
        msoLogger.info("aai response content:" + aaiRsp.getResponseBodyAsString())
        msoLogger.info(" *****Exit addNSRelationship *****")
    }
    
    public APIResponse executeAAIPutCall(DelegateExecution execution, String url, String payload){
        msoLogger.trace("Started Execute AAI Put Process ") 
        APIResponse apiResponse = null
        try{
            String uuid = utils.getRequestID()
            msoLogger.info("Generated uuid is: " + uuid) 
            msoLogger.info("URL to be used is: " + url) 
            String userName = execution.getVariable("URN_aai_auth")
            String password = execution.getVariable("URN_mso_msoKey")
            String basicAuthCred = utils.getBasicAuth(userName,password)
            RESTConfig config = new RESTConfig(url);
            RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Content-Type", "application/xml").addHeader("Accept","application/xml");
            if (basicAuthCred != null && !"".equals(basicAuthCred)) {
                client.addAuthorizationHeader(basicAuthCred)
            }
            apiResponse = client.httpPut(payload)
            msoLogger.trace("Completed Execute AAI Put Process ") 
        }catch(Exception e){
            msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception occured while executing AAI Put Call.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e); 
            throw new BpmnError("MSOWorkflowException")
        }
        return apiResponse
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
            RESTClient client = new RESTClient(config).addHeader("Content-Type", "application/json").addHeader("Accept","application/json").addHeader("Authorization","Basic QlBFTENsaWVudDpwYXNzd29yZDEk");
            apiResponse = client.httpPost(requestBody)
            msoLogger.info("response code:"+ apiResponse.getStatusCode() +"\nresponse body:"+ apiResponse.getResponseBodyAsString())    
            msoLogger.trace("Completed Execute VF-C adapter Post Process ")
        }catch(Exception e){
            msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception occured while executing AAI Post Call.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e);
            throw new BpmnError("MSOWorkflowException")
        }        
        return apiResponse
    }
    
	public void sendSyncResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** sendSyncResponse *** ", isDebugEnabled)

		try {
			String operationStatus = execution.getVariable("operationStatus")
			// RESTResponse for main flow
			String resourceOperationResp = """{"operationStatus":"${operationStatus}"}""".trim()
			utils.log("DEBUG", " sendSyncResponse to APIH:" + "\n" + resourceOperationResp, isDebugEnabled)
			sendWorkflowResponse(execution, 202, resourceOperationResp)
			execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit sendSyncResopnse *****",  isDebugEnabled)
	}

}
