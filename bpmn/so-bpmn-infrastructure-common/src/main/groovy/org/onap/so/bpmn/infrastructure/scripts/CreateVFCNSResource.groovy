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

import org.onap.so.client.HttpClientFactory
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.logger.MsoLogger
import org.onap.so.bpmn.core.UrnPropertiesReader

import javax.ws.rs.core.Response
import org.onap.so.utils.TargetEntity

/**
 * This groovy class supports the <class>DoCreateVFCNetworkServiceInstance.bpmn</class> process.
 * flow for VFC Network Service Create
 */
public class CreateVFCNSResource extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CreateVFCNSResource.class);

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
           execution.setVariable("nsServiceModelUUID", nsServiceModelUUID);

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
     * create NS task
     */
    public void createNetworkService(DelegateExecution execution) {
        msoLogger.trace("createNetworkService ")
        String vfcAdapterUrl = execution.getVariable("vfcAdapterUrl")
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
        Response apiResponse = postRequest(execution, vfcAdapterUrl + "/ns", reqBody)
        String returnCode = apiResponse.getStatus ()
        String aaiResponseAsString = apiResponse.readEntity(String.class)
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
        msoLogger.info(" *****Exit  instantiateNetworkService *****")
    }

    /**
     * query NS task
     */
    public void queryNSProgress(DelegateExecution execution) {
        msoLogger.trace("queryNSProgress ")
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
        msoLogger.info(" *****Exit  queryNSProgress *****")
    }

    /**
     * delay 5 sec
     */
    public void timeDelay(DelegateExecution execution) {
        try {
            Thread.sleep(5000);
        } catch(InterruptedException e) {
            msoLogger.error( "Time Delay exception" + e.getMessage());
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

        AAIResourceUri nsUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,globalSubscriberId,serviceType,nsInstanceId)
        AAIResourceUri relatedServiceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,globalSubscriberId,serviceType,serviceId)

        try{
            getAAIClient().connect(nsUri,relatedServiceUri)
            msoLogger.info("NS relationship to Service added successfully")
        }catch(Exception e){
            msoLogger.error("Exception occured while Creating NS relationship."+ e.getMessage());
            throw new BpmnError("MSOWorkflowException")
        }
    }

    /**
     * post request
     * url: the url of the request
     * requestBody: the body of the request
     */
    private Response postRequest(DelegateExecution execution, String urlString, String requestBody){
        msoLogger.trace("Started Execute VFC adapter Post Process ")
        msoLogger.info("url:" + urlString +"\nrequestBody:"+ requestBody)
        Response apiResponse = null
        try{

            URL url = new URL(urlString);
            
            // Get the Basic Auth credentials for the VFCAdapter, username is 'bpel', auth is '07a7159d3bf51a0e53be7a8f89699be7'
            // user 'bepl' authHeader is the same with mso.db.auth
            String basicAuthValuedb =  UrnPropertiesReader.getVariable("mso.db.auth", execution)
            HttpClient httpClient = new HttpClientFactory().newJsonClient(url, TargetEntity.VNF_ADAPTER)
            httpClient.addAdditionalHeader("Accept", "application/json")
            httpClient.addAdditionalHeader("Authorization", basicAuthValuedb)

            apiResponse = httpClient.post(requestBody)
            
            msoLogger.debug("response code:"+ apiResponse.getStatus() +"\nresponse body:"+ apiResponse.readEntity(String.class))

        }catch(Exception e){
            msoLogger.error("VFC Aatpter Post Call Exception:" + e.getMessage());
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "VFC Aatpter Post Call Exception")
        }		
		
        msoLogger.trace("Completed Execute VF-C adapter Post Process ")
        
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
