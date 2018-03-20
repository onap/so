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
import org.openecomp.mso.bpmn.common.recipe.ResourceInput;
import org.openecomp.mso.bpmn.common.resource.ResourceRequestBuilder 
import org.openecomp.mso.bpmn.core.WorkflowException 
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder.AbstractBuilder
import org.openecomp.mso.rest.APIResponse

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
public class CreateSDNCCNetworkResource extends AbstractServiceTaskProcessor {

    String Prefix="CRESDNCRES_"
            
    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()
    
    public void preProcessRequest(DelegateExecution execution){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** Started preProcessRequest *****",  isDebugEnabled)
        try {           
            
            //get bpmn inputs from resource request.
            String requestId = execution.getVariable("requestId")
            String requestAction = execution.getVariable("requestAction")
            utils.log("INFO","The requestAction is: " + requestAction,  isDebugEnabled)
            String recipeParamsFromRequest = execution.getVariable("recipeParams")
            utils.log("INFO","The recipeParams is: " + recipeParams,  isDebugEnabled)
            String resourceInput = execution.getVariable("requestInput")
            utils.log("INFO","The resourceInput is: " + resourceInput,  isDebugEnabled)
            //Get ResourceInput Object
            ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(resourceInput, resourceInputObj)
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
            String serviceModelVersion = resourceInputObj.getServiceModelInfo().getModelName()
            String serviceModelName = resourceInputObj.getServiceModelInfo().getModelVersion()
            String globalCustomerId = resourceInputObj.getGlobalSubscriberId()
            String modelInvariantUuid = resourceInputObj.getResourceModelInfo().getModelInvariantUuid();
            String modelCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
            String modelUuid = resourceInputObj.getResourceModelInfo().getModelUuid()
            String modelName = resourceInputObj.getResourceModelInfo().getModelName()
            String modelVersion = resourceInputObj.getResourceModelInfo().getModelVersion()
            String resourceInputPrameters = resourceInputObj.getResourceParameters()
            String netowrkInputParametersJson = jsonUtil.getJsonValue(resourceInputPrameters, "requestInputs")
            //here convert json string to xml string
            String netowrkInputParameters = jsonUtil.json2xml(netowrkInputParametersJson)
            // 1. prepare assign topology via SDNC Adapter SUBFLOW call
            String sndcTopologyCreateRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, createNetworkInput, serviceInstanceId, sdncCallback, sdnc_svcAction, sdnc_requestAction, null, null, null)
                    String content =
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
                                        <service-id>${sdnc_service_id}</service-id>
                                        <subscription-service-type>${serviceType}</subscription-service-type>
                                        <ecomp-model-information>
                                             <model-invariant-uuid>${serviceModelInvariantUuid}</model-invariant-uuid>
                                             <model-uuid>${serviceModelUuid}</model-uuid>
                                             <model-version>${serviceModelVersion}</model-version>
                                             <model-name>${serviceModelName}</model-name>
                                        </ecomp-model-information>
                                        <service-instance-id>${serviceInstanceId}</service-instance-id>
                                        <global-customer-id>${globalCustomerId}</global-customer-id>
                                     </service-information>
                                     <network-information>
                                        <ecomp-model-information>
                                             <model-invariant-uuid>${modelInvariantUuid}</model-invariant-uuid>
                                             <model-customization-uuid>${modelCustomizationUuid}</model-customization-uuid>
                                             <model-uuid>${modelUuid}</model-uuid>
                                             <model-version>${modelVersion}</model-version>
                                             <model-name>${modelName}</model-name>
                                        </ecomp-model-information>
                                     </network-information>
                                     <network-request-input>
                                       <network-input-parameters>${netowrkInputParameters}</network-input-parameters>
                                     </network-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()
            
            String sndcTopologyCreateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
            utils.logAudit(sndcTopologyCreateRequesAsString)
            execution.setVariable(Prefix + "createSDNCRequest", sndcTopologyCreateRequesAsString)
            utils.log("DEBUG", Prefix + "createSDNCRequest - " + "\n" +  sndcTopologyCreateRequesAsString, isDebugEnabled)

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in CreateSDNCCNetworkResource flow. prepareSDNCRequest() - " + ex.getMessage()
            utils.log("DEBUG", exceptionMessage, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }
       utils.log("INFO"," ***** Exit prepareSDNCRequest *****",  isDebugEnabled)
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
