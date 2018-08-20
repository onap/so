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
public class DeleteDeviceResource extends AbstractServiceTaskProcessor {

    String Prefix="DELDEVRES_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DeleteDeviceResource.class)

    public void preProcessRequest(DelegateExecution execution){
        msoLogger.info(" ***** Started preProcessRequest *****")
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
            execution.setVariable(Prefix + "resourceInput", resourceInputObj)
            String resourceInputPrameters = resourceInputObj.getResourceParameters()
            String inputParametersJson = jsonUtil.getJsonValue(resourceInputPrameters, "requestInputs")
            JSONObject inputParameters = new JSONObject(customizeResourceParam(inputParametersJson))
            execution.setVariable(Prefix + "resourceRequestInputs", inputParameters)

            //Deal with recipeParams
            String recipeParamsFromWf = execution.getVariable("recipeParamXsd")
            String resourceName = resourceInputObj.getResourceInstanceName()
            //For sdnc requestAction default is "createNetworkInstance"
            String operationType = "Network"
            if(!StringUtils.isBlank(recipeParamsFromRequest)){
                //the operationType from worflow(first node) is second priority.
                operationType = jsonUtil.getJsonValue(recipeParamsFromRequest, "operationType")
            }
            if(!StringUtils.isBlank(recipeParamsFromWf)){
                //the operationType from worflow(first node) is highest priority.
                operationType = jsonUtil.getJsonValue(recipeParamsFromWf, "operationType")
            }

            execution.setVariable(Prefix + "serviceInstanceId", resourceInputObj.getServiceInstanceId())
            execution.setVariable("mso-request-id", requestId)

        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            String msg = "Exception in preProcessRequest " + ex.getMessage()
            msoLogger.debug( msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    String customizeResourceParam(String networkInputParametersJson) {
        List<Map<String, Object>> paramList = new ArrayList();
        JSONObject jsonObject = new JSONObject(networkInputParametersJson);
        Iterator iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            HashMap<String, String> hashMap = new HashMap();
            hashMap.put("name", key);
            hashMap.put("value", jsonObject.get(key))
            paramList.add(hashMap)
        }
        Map<String, List<Map<String, Object>>> paramMap = new HashMap();
        paramMap.put("param", paramList);

        return  new JSONObject(paramMap).toString();
    }

    public void checkDevType(DelegateExecution execution){
        utils.log("INFO"," ***** Started checkDevType *****")
        try {

            JSONObject inputParameters = execution.getVariable(Prefix + "resourceRequestInputs")

            String devType = inputParameters.get("device_class")

            if(StringUtils.isBlank(devType)) {
                devType = "OTHER"
            }

            execution.setVariable("device_class", devType)

        } catch (Exception ex){
            String msg = "Exception in checkDevType " + ex.getMessage()
            msoLogger.debug( msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    public void getVNFTemplatefromSDC(DelegateExecution execution){
        utils.log("INFO"," ***** Started getVNFTemplatefromSDC *****")
        try {
            // To do


        } catch (Exception ex){
            String msg = "Exception in getVNFTemplatefromSDC " + ex.getMessage()
            msoLogger.debug( msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    public void postVNFInfoProcess(DelegateExecution execution){
        utils.log("INFO"," ***** Started postVNFInfoProcess *****")
        try {
            // To do


        } catch (Exception ex){
            String msg = "Exception in postVNFInfoProcess " + ex.getMessage()
            msoLogger.debug( msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    public void sendSyncResponse (DelegateExecution execution) {
        msoLogger.debug( " *** sendSyncResponse *** ")

        try {
            String operationStatus = "finished"
            // RESTResponse for main flow
            String resourceOperationResp = """{"operationStatus":"${operationStatus}"}""".trim()
            msoLogger.debug( " sendSyncResponse to APIH:" + "\n" + resourceOperationResp)
            sendWorkflowResponse(execution, 202, resourceOperationResp)
            execution.setVariable("sentSyncResponse", true)

        } catch (Exception ex) {
            String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
            msoLogger.debug( msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        msoLogger.debug(" ***** Exit sendSyncResopnse *****")
    }
}
