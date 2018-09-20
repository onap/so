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

package org.onap.so.bpmn.infrastructure.scripts

import org.json.JSONObject
import org.json.XML;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor 
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.recipe.ResourceInput;
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder 
import org.onap.so.bpmn.core.WorkflowException 
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.builder.AbstractBuilder
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError 
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils 
import org.onap.so.rest.RESTClient 
import org.onap.so.rest.RESTConfig
import org.onap.so.rest.APIResponse;
import org.onap.so.bpmn.common.scripts.AaiUtil

/**
 * This groovy class supports the <class>CreateDeviceResource.bpmn</class> process.
 * flow for Device Resource Create
 */
public class CreateDeviceResource extends AbstractServiceTaskProcessor {

    String Prefix="CREDEVRES_"
            
    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()
    
    private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CreateDeviceResource.class)

    public void preProcessRequest(DelegateExecution execution){
        msoLogger.info(" ***** Started preProcessRequest *****")
        String msg = ""
        try {           
            
            //get bpmn inputs from resource request.
            String requestId = execution.getVariable("mso-request-id")
            String requestAction = execution.getVariable("requestAction")
            msoLogger.info("The requestAction is: " + requestAction)
            String recipeParamsFromRequest = execution.getVariable("recipeParams")
            msoLogger.info("The recipeParams is: " + recipeParamsFromRequest)
            String resourceInput = execution.getVariable("resourceInput")
            msoLogger.info("The resourceInput is: " + resourceInput)
            //Get ResourceInput Object
            ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(resourceInput, ResourceInput.class)
            execution.setVariable(Prefix + "ResourceInput", resourceInputObj)

            String resourceInputPrameters = resourceInputObj.getResourceParameters()
            String inputParametersJson = jsonUtil.getJsonValue(resourceInputPrameters, "requestInputs")
            JSONObject inputParameters = new JSONObject(inputParametersJson)
            execution.setVariable(Prefix + "ResourceRequestInputs", inputParameters)

//            String incomingRequest = resourceInputObj.getRequestsInputs()
//            String serviceParameters = JsonUtils.getJsonValue(incomingRequest, "service.parameters")
//            String requestInputs = JsonUtils.getJsonValue(serviceParameters, "requestInputs")
//            JSONObject serviceInputParameters = new JSONObject(requestInputs)
//            execution.setVariable(Prefix + "ServiceParameters", serviceInputParameters)
            
            //Deal with recipeParams
            String recipeParamsFromWf = execution.getVariable("recipeParamXsd")
            String resourceName = resourceInputObj.getResourceInstanceName() 
            if (isBlank(resourceName)) {
                msg = "Input resourceName is null"
                msoLogger.error(msg)
            }
            execution.setVariable("resourceName", resourceName)
            msoLogger.info("resourceName:" + resourceName)

            String resourceModelInvariantUuid = resourceInputObj.getResourceModelInfo().getModelInvariantUuid()
            if (isBlank(resourceModelInvariantUuid)) {
                msg = "Input resourceModelInvariantUuid is null"
                msoLogger.error(msg)
            }
            execution.setVariable(Prefix + "ResourceModelInvariantUuid", resourceModelInvariantUuid)
            msoLogger.info("resourceModelInvariantUuid:" + resourceModelInvariantUuid)
				
            String resourceModelUuid = resourceInputObj.getResourceModelInfo().getModelUuid()
            if (isBlank(resourceModelUuid)) {
                msg = "Input resourceModelUuid is null"
                msoLogger.error(msg)
            }
            execution.setVariable(Prefix + "ResourceModelUuid", resourceModelUuid)
            msoLogger.info("resourceModelUuid:" + resourceModelUuid)
			
            String resourceModelCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
            if (isBlank(resourceModelCustomizationUuid)) {
                msg = "Input resourceModelCustomizationUuid is null"
                msoLogger.error(msg)
            }
            execution.setVariable(Prefix + "ResourceModelCustomizationUuid", resourceModelCustomizationUuid)
            msoLogger.info("resourceModelCustomizationUuid:" + resourceModelCustomizationUuid)

            execution.setVariable(Prefix + "serviceInstanceId", resourceInputObj.getServiceInstanceId())
            execution.setVariable("mso-request-id", requestId)
            
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            msoLogger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }
	
	public void checkDevType(DelegateExecution execution){
		msoLogger.info(" ***** Started checkDevType *****")
		try {

			JSONObject resourceInputParameters = execution.getVariable(Prefix + "ResourceRequestInputs")
			String devType = resourceInputParameters.get("device_class")
			
			if(StringUtils.isBlank(devType)) {
				devType = "OTHER"
			}
			// support VNF as PNF, to modify 
			else if(StringUtils.equalsIgnoreCase(devType, "VNF")) {
				devType = "PNF"
			}			
			
			execution.setVariable("device_class", devType)

		} catch (Exception ex){
			String msg = "Exception in checkDevType " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}
	
	private void setProgressUpdateVariables(DelegateExecution execution, String body) {
		def dbAdapterEndpoint = execution.getVariable("URN_mso_adapters_openecomp_db_endpoint")
		execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
		execution.setVariable("CVFMI_updateResOperStatusRequest", body)
	}
	
	public void prepareUpdateProgress(DelegateExecution execution) {
		msoLogger.info(" ***** Started prepareUpdateProgress *****")
		ResourceInput resourceInputObj = execution.getVariable(Prefix + "ResourceInput")
		String operType = resourceInputObj.getOperationType()
		String resourceCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
		String ServiceInstanceId = resourceInputObj.getServiceInstanceId()
		String modelName = resourceInputObj.getResourceModelInfo().getModelName()
		String operationId = resourceInputObj.getOperationId()
		String progress = execution.getVariable("progress")
		String status = execution.getVariable("status")
		String statusDescription = execution.getVariable("statusDescription")

		String body = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.openecomp.mso/requestsdb">
                        <soapenv:Header/>
                <soapenv:Body>
                    <ns:updateResourceOperationStatus>
                               <operType>${operType}</operType>
                               <operationId>${operationId}</operationId>
                               <progress>${progress}</progress>
                               <resourceTemplateUUID>${resourceCustomizationUuid}</resourceTemplateUUID>
                               <serviceId>${ServiceInstanceId}</serviceId>
                               <status>${status}</status>
                               <statusDescription>${statusDescription}</statusDescription>
                    </ns:updateResourceOperationStatus>
                </soapenv:Body>
                </soapenv:Envelope>"""

		setProgressUpdateVariables(execution, body)
		msoLogger.info(" ***** Exit prepareUpdateProgress *****")
	}
	
	public void getVNFTemplatefromSDC(DelegateExecution execution){
		msoLogger.info(" ***** Started getVNFTemplatefromSDC *****")
		try {
			// To do


		} catch (Exception ex){
			String msg = "Exception in getVNFTemplatefromSDC " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}
	
	public void postVNFInfoProcess(DelegateExecution execution){
		msoLogger.info(" ***** Started postVNFInfoProcess *****")
		try {
			// To do


		} catch (Exception ex){
			String msg = "Exception in postVNFInfoProcess " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}
    
	public void sendSyncResponse (DelegateExecution execution) {
		msoLogger.debug(" *** sendSyncResponse *** ")

		try {
			String operationStatus = "finished"
			// RESTResponse for main flow
			String resourceOperationResp = """{"operationStatus":"${operationStatus}"}""".trim()
			msoLogger.debug(" sendSyncResponse to APIH:" + "\n" + resourceOperationResp)
			sendWorkflowResponse(execution, 202, resourceOperationResp)
			execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit sendSyncResopnse *****")
	}
}
