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

import org.apache.commons.lang3.StringUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.json.JSONObject
import org.json.XML
import org.onap.so.bpmn.common.recipe.ResourceInput
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.logger.MsoLogger

/**
 * This groovy class supports the <class>ActivateSDNCCNetworkResource.bpmn</class> process.
 * flow for SDNC Network Resource Activate
 */
public class ActivateSDNCNetworkResource extends AbstractServiceTaskProcessor {
    private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CreateSDNCNetworkResource.class);
    
    String Prefix = "ACTSDNCRES_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

    public void preProcessRequest(DelegateExecution execution) {
        msoLogger.trace("Started preProcessRequest ")

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

            String sdnc_svcAction = "activate"
            operationType = "SOTNConnectivity"

            String sdnc_requestAction = StringUtils.capitalize(sdnc_svcAction) + operationType +"Instance"
            execution.setVariable(Prefix + "svcAction", sdnc_svcAction)
            execution.setVariable(Prefix + "requestAction", sdnc_requestAction)
            execution.setVariable(Prefix + "serviceInstanceId", resourceInputObj.getServiceInstanceId())
            execution.setVariable("mso-request-id", requestId)
            execution.setVariable("mso-service-instance-id", resourceInputObj.getServiceInstanceId())
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            msoLogger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    public void prepareUpdateAfterActivateSDNCResource(DelegateExecution execution) {
        msoLogger.trace("started prepareUpdateAfterActivateSDNCResource ")

        ResourceInput resourceInputObj = execution.getVariable(Prefix + "resourceInput")
        String operType = resourceInputObj.getOperationType()
        String resourceCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
        String ServiceInstanceId = resourceInputObj.getServiceInstanceId()
        String operationId = resourceInputObj.getOperationId()
        String progress = "100"
        String status = "finished"
        String statusDescription = "SDCN resource creation completed"

        execution.getVariable("operationId")

        String body = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                <soapenv:Body>
                    <ns:updateResourceOperationStatus>
                               <operType>${MsoUtils.xmlEscape(operType)}</operType>
                               <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                               <progress>${MsoUtils.xmlEscape(progress)}</progress>
                               <resourceTemplateUUID>${MsoUtils.xmlEscape(resourceCustomizationUuid)}</resourceTemplateUUID>
                               <serviceId>${MsoUtils.xmlEscape(ServiceInstanceId)}</serviceId>
                               <status>${MsoUtils.xmlEscape(status)}</status>
                               <statusDescription>${MsoUtils.xmlEscape(statusDescription)}</statusDescription>
                    </ns:updateResourceOperationStatus>
                </soapenv:Body>
                </soapenv:Envelope>""";

        setProgressUpdateVariables(execution, body)
    }

    public void prepareSDNCRequest (DelegateExecution execution) {
        msoLogger.trace("Started prepareSDNCRequest ")

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
            String networkInputParametersJson = jsonUtil.getJsonValue(resourceInputPrameters, "requestInputs")
            //here convert json string to xml string
            String netowrkInputParameters = XML.toString(new JSONObject(customizeResourceParam(networkInputParametersJson)))
            // 1. prepare assign topology via SDNC Adapter SUBFLOW call
            String sndcTopologyCreateRequest =
                    """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                                              xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1" 
                                                              xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
                                 <sdncadapter:RequestHeader>
                                    <sdncadapter:RequestId>${MsoUtils.xmlEscape(hdrRequestId)}</sdncadapter:RequestId>
                                    <sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
                                    <sdncadapter:SvcAction>${MsoUtils.xmlEscape(sdnc_svcAction)}</sdncadapter:SvcAction>
                                    <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
                                    <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                                    <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
                                 </sdncadapter:RequestHeader>
                                 <sdncadapterworkflow:SDNCRequestData>
                                     <request-information>
                                        <request-id>${MsoUtils.xmlEscape(hdrRequestId)}</request-id>
                                        <request-action>${MsoUtils.xmlEscape(sdnc_requestAction)}</request-action>
                                        <source>${MsoUtils.xmlEscape(source)}</source>
                                        <notification-url></notification-url>
                                        <order-number></order-number>
                                        <order-version></order-version>
                                     </request-information>
                                     <service-information>
                                        <service-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-id>
                                        <subscription-service-type>${MsoUtils.xmlEscape(serviceType)}</subscription-service-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${MsoUtils.xmlEscape(serviceModelInvariantUuid)}</model-invariant-uuid>
                                             <model-uuid>${MsoUtils.xmlEscape(serviceModelUuid)}</model-uuid>
                                             <model-version>${MsoUtils.xmlEscape(serviceModelVersion)}</model-version>
                                             <model-name>${MsoUtils.xmlEscape(serviceModelName)}</model-name>
                                        </onap-model-information>
                                        <service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
                                        <global-customer-id>${MsoUtils.xmlEscape(globalCustomerId)}</global-customer-id>
                                     </service-information>
                                     <network-information>
                                        <onap-model-information>
                                             <model-invariant-uuid>${MsoUtils.xmlEscape(modelInvariantUuid)}</model-invariant-uuid>
                                             <model-customization-uuid>${MsoUtils.xmlEscape(modelCustomizationUuid)}</model-customization-uuid>
                                             <model-uuid>${MsoUtils.xmlEscape(modelUuid)}</model-uuid>
                                             <model-version>${MsoUtils.xmlEscape(modelVersion)}</model-version>
                                             <model-name>${MsoUtils.xmlEscape(modelName)}</model-name>
                                        </onap-model-information>
                                     </network-information>
                                     <network-request-input>
                                       <network-input-parameters>${MsoUtils.xmlEscape(netowrkInputParameters)}</network-input-parameters>
                                     </network-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()

            String sndcTopologyCreateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
            msoLogger.debug(sndcTopologyCreateRequesAsString)
            execution.setVariable("sdncAdapterWorkflowRequest", sndcTopologyCreateRequesAsString)
            msoLogger.debug("sdncAdapterWorkflowRequest - " + "\n" +  sndcTopologyCreateRequesAsString)

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in CreateSDNCCNetworkResource flow. prepareSDNCRequest() - " + ex.getMessage()
            msoLogger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }
        msoLogger.trace("Exit prepareSDNCRequest ")
    }

    public void postCreateSDNCCall(DelegateExecution execution) {
        msoLogger.trace("started postCreateSDNCCall ")

        String responseCode = execution.getVariable(Prefix + "sdncCreateReturnCode")
        String responseObj = execution.getVariable(Prefix + "SuccessIndicator")

        msoLogger.info("response from sdnc, response code :" + responseCode + "  response object :" + responseObj)
    }

    public void sendSyncResponse(DelegateExecution execution) {
        msoLogger.trace("started sendSyncResponse ")

        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("DEBUG", " *** sendSyncResponse *** ", isDebugEnabled)

        try {
            String operationStatus = "finished"
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