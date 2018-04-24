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

package org.openecomp.mso.bpmn.infrastructure.scripts

import org.json.JSONObject
import org.json.XML;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor 
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.recipe.ResourceInput;
import org.openecomp.mso.bpmn.common.resource.ResourceRequestBuilder 
import org.openecomp.mso.bpmn.core.WorkflowException 
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder.AbstractBuilder
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils

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
 * This groovy class supports the <class>CreateSDNCCNetworkResource.bpmn</class> process.
 * flow for SDNC Network Resource Create
 */
public class CreateSDNCNetworkResource extends AbstractServiceTaskProcessor {

    String Prefix="CRESDNCRES_"
            
    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
    
    public void preProcessRequest(DelegateExecution execution){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** Started preProcessRequest *****",  isDebugEnabled)
        try {           
            
            //get bpmn inputs from resource request.
            String requestId = execution.getVariable("requestId")
            String requestAction = execution.getVariable("requestAction")
            utils.log("INFO","The requestAction is: " + requestAction,  isDebugEnabled)
            String recipeParamsFromRequest = execution.getVariable("recipeParams")
            utils.log("INFO","The recipeParams is: " + recipeParamsFromRequest,  isDebugEnabled)
            String resourceInput = execution.getVariable("resourceInput")
            utils.log("INFO","The resourceInput is: " + resourceInput,  isDebugEnabled)
            //Get ResourceInput Object
            ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(resourceInput, ResourceInput.class)
            execution.setVariable(Prefix + "resourceInput", resourceInputObj)
            
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
            
            
            //For sdnc, generate svc_action and request_action
            String sdnc_svcAction = "create"
            if(StringUtils.containsIgnoreCase(resourceInputObj.getResourceInstanceName(), "overlay")){
                //This will be resolved in R3.
                sdnc_svcAction ="activate"
                operationType = "NCINetwork"        
            }
            if(StringUtils.containsIgnoreCase(resourceInputObj.getResourceInstanceName(), "underlay")){
                //This will be resolved in R3.
                operationType ="Network"
            }        
            String sdnc_requestAction = StringUtils.capitalize(sdnc_svcAction) + operationType +"Instance"                    
            execution.setVariable(Prefix + "svcAction", sdnc_svcAction)        
            execution.setVariable(Prefix + "requestAction", sdnc_requestAction)
            execution.setVariable(Prefix + "serviceInstanceId", resourceInputObj.getServiceInstanceId())
            execution.setVariable("mso-request-id", requestId)
            execution.setVariable("mso-service-instance-id", resourceInputObj.getServiceInstanceId())
            //TODO Here build networkrequest
            
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            utils.log("DEBUG", msg, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    String customizeResourceParam(String netowrkInputParametersJson) {
        List<Map<String, Object>> paramList = new ArrayList();
        JSONObject jsonObject = new JSONObject(netowrkInputParametersJson);
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
    
    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */
    public void prepareSDNCRequest (DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** Started prepareSDNCRequest *****",  isDebugEnabled)

        try {
            // get variables
            String sdnc_svcAction = execution.getVariable(Prefix + "svcAction")        
            String sdnc_requestAction = execution.getVariable(Prefix + "requestAction")
            String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
            String createNetworkInput = execution.getVariable(Prefix + "networkRequest")

            String hdrRequestId = execution.getVariable("mso-request-id")
            String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")
            String source = execution.getVariable("source")
            String sdnc_service_id = execution.getVariable(Prefix + "sdncServiceId")
            ResourceInput resourceInputObj = execution.getVariable(Prefix + "resourceInput")
            String serviceType = resourceInputObj.getServiceType()
            String serviceModelInvariantUuid = resourceInputObj.getServiceModelInfo().getModelInvariantUuid()
            String serviceModelUuid = resourceInputObj.getServiceModelInfo().getModelUuid()
            String serviceModelVersion = resourceInputObj.getServiceModelInfo().getModelVersion()
            String serviceModelName = resourceInputObj.getServiceModelInfo().getModelName()
            String globalCustomerId = resourceInputObj.getGlobalSubscriberId()
            String modelInvariantUuid = resourceInputObj.getResourceModelInfo().getModelInvariantUuid();
            String modelCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
            String modelUuid = resourceInputObj.getResourceModelInfo().getModelUuid()
            String modelName = resourceInputObj.getResourceModelInfo().getModelName()
            String modelVersion = resourceInputObj.getResourceModelInfo().getModelVersion()
            String resourceInputPrameters = resourceInputObj.getResourceParameters()
            String netowrkInputParametersJson = jsonUtil.getJsonValue(resourceInputPrameters, "requestInputs")
            //here convert json string to xml string
            String netowrkInputParameters = XML.toString(new JSONObject(customizeResourceParam(netowrkInputParametersJson)))
            // 1. prepare assign topology via SDNC Adapter SUBFLOW call
            String sndcTopologyCreateRequest =
                    """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                                                              xmlns:sdncadapter="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1" 
                                                              xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1">
                                 <sdncadapter:RequestHeader>
                                    <sdncadapter:RequestId>${hdrRequestId}</sdncadapter:RequestId>
                                    <sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>
                                    <sdncadapter:SvcAction>${sdnc_svcAction}</sdncadapter:SvcAction>
                                    <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
                                    <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                                    <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
                                 </sdncadapter:RequestHeader>
                                 <sdncadapterworkflow:SDNCRequestData>
                                     <request-information>
                                        <request-id>${hdrRequestId}</request-id>
                                        <request-action>${sdnc_requestAction}</request-action>
                                        <source>${source}</source>
                                        <notification-url></notification-url>
                                        <order-number></order-number>
                                        <order-version></order-version>
                                     </request-information>
                                     <service-information>
                                        <service-id>${serviceInstanceId}</service-id>
                                        <subscription-service-type>${serviceType}</subscription-service-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${serviceModelInvariantUuid}</model-invariant-uuid>
                                             <model-uuid>${serviceModelUuid}</model-uuid>
                                             <model-version>${serviceModelVersion}</model-version>
                                             <model-name>${serviceModelName}</model-name>
                                        </onap-model-information>
                                        <service-instance-id>${serviceInstanceId}</service-instance-id>
                                        <global-customer-id>${globalCustomerId}</global-customer-id>
                                     </service-information>
                                     <network-information>
                                        <onap-model-information>
                                             <model-invariant-uuid>${modelInvariantUuid}</model-invariant-uuid>
                                             <model-customization-uuid>${modelCustomizationUuid}</model-customization-uuid>
                                             <model-uuid>${modelUuid}</model-uuid>
                                             <model-version>${modelVersion}</model-version>
                                             <model-name>${modelName}</model-name>
                                        </onap-model-information>
                                     </network-information>
                                     <network-request-input>
                                       <network-input-parameters>${netowrkInputParameters}</network-input-parameters>
                                     </network-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()
            
            String sndcTopologyCreateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
            utils.logAudit(sndcTopologyCreateRequesAsString)
            execution.setVariable("sdncAdapterWorkflowRequest", sndcTopologyCreateRequesAsString)
            utils.log("DEBUG","sdncAdapterWorkflowRequest - " + "\n" +  sndcTopologyCreateRequesAsString, isDebugEnabled)

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in CreateSDNCCNetworkResource flow. prepareSDNCRequest() - " + ex.getMessage()
            utils.log("DEBUG", exceptionMessage, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }
       utils.log("INFO"," ***** Exit prepareSDNCRequest *****",  isDebugEnabled)
	}

    private void setProgressUpdateVariables(DelegateExecution execution, String body) {
        def dbAdapterEndpoint = execution.getVariable("URN_mso_adapters_openecomp_db_endpoint")
        execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
        execution.setVariable("CVFMI_updateResOperStatusRequest", body)
    }

    public void prepareUpdateBeforeCreateSDNCResource(DelegateExecution execution) {
        ResourceInput resourceInputObj = execution.getVariable(Prefix + "resourceInput")
        String operType = resourceInputObj.getOperationType()
        String resourceCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
        String ServiceInstanceId = resourceInputObj.getServiceInstanceId()
        String operationId = resourceInputObj.getOperationId()
        String progress = "20"
        String status = "processing"
        String statusDescription = "SDCN resource creation invoked"

        execution.getVariable("operationId")

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
                </soapenv:Envelope>""";

        setProgressUpdateVariables(execution, body)

    }

    public void prepareUpdateAfterCreateSDNCResource(execution) {
        ResourceInput resourceInputObj = execution.getVariable(Prefix + "resourceInput")
        String operType = resourceInputObj.getOperationType()
        String resourceCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
        String ServiceInstanceId = resourceInputObj.getServiceInstanceId()
        String operationId = resourceInputObj.getOperationId()
        String progress = "100"
        String status = "Created"
        String statusDescription = "SDCN resource creation completed"

        execution.getVariable("operationId")

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
                </soapenv:Envelope>""";

        setProgressUpdateVariables(execution, body)
    }

    public void postCreateSDNCCall(DelegateExecution execution){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** Started prepareSDNCRequest *****",  isDebugEnabled)
        String responseCode = execution.getVariable(Prefix + "sdncCreateReturnCode")
        String responseObj = execution.getVariable(Prefix + "SuccessIndicator")
        
        utils.log("INFO","response from sdnc, response code :" + responseCode + "  response object :" + responseObj,  isDebugEnabled)
        utils.log("INFO"," ***** Exit prepareSDNCRequest *****",  isDebugEnabled)
    }
}
