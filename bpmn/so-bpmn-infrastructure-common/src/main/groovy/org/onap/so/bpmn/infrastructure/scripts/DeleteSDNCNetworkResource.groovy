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

import org.onap.so.logger.MsoLogger

import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError 
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.recipe.ResourceInput;
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder 
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor 
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.MsoLogger

import groovy.json.*

/**
 * This groovy class supports the <class>DeleteSDNCCNetworkResource.bpmn</class> process.
 * flow for SDNC Network Resource 
 */
public class DeleteSDNCNetworkResource extends AbstractServiceTaskProcessor {
    private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DeleteSDNCNetworkResource.class);

    String Prefix="DELSDNCRES_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

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

            //Deal with recipeParams
            String recipeParamsFromWf = execution.getVariable("recipeParamXsd")
            String resourceModelName = resourceInputObj.getResourceModelInfo().getModelName()
            //For sdnc requestAction default is "NetworkInstance"
            String operationType = "Network"
            if(!StringUtils.isBlank(recipeParamsFromRequest) && "null" != recipeParamsFromRequest){
                //the operationType from worflow(first node) is second priority.
                operationType = jsonUtil.getJsonValue(recipeParamsFromRequest, "operationType")
            }
            if(!StringUtils.isBlank(recipeParamsFromWf)){
                //the operationType from worflow(first node) is highest priority.
                operationType = jsonUtil.getJsonValue(recipeParamsFromWf, "operationType")
            }


            //For sdnc, generate svc_action and request_action
            String sdnc_svcAction = "delete"
            switch (resourceInputObj.getResourceModelInfo().getModelName()) {

                case ~/[\w\s\W]*overlay[\w\s\W]*/ :
                    ///This will be resolved in R3.
                    sdnc_svcAction ="deactivate"
                    operationType = "NCINetwork"
                    break

                case ~/[\w\s\W]*underlay[\w\s\W]*/ :
                    //This will be resolved in R3.
                    operationType ="Network"
                    break

                case ~/[\w\s\W]*SOTNConnectivity[\w\s\W]*/ :
                    operationType = "SOTNConnectivity"
                    execution.setVariable("isActivateRequired", "true")
                    break

                case ~/[\w\s\W]*sotnvpnattachment[\w\s\W]*/ :
                    operationType = "SOTNAttachment"
                    execution.setVariable("isActivateRequired", "true")
                    break

                case ~/[\w\s\W]*SiteVF[\w\s\W]*/ :
                    operationType = "Site"
                    execution.setVariable("isActivateRequired", "true")
                    break

                case ~/[\w\s\W]*deviceVF[\w\s\W]*/ :
                    operationType = "SDWANDevice"
                    execution.setVariable("isActivateRequired", "true")
                    break

                case ~/[\w\s\W]*SDWANConnectivity[\w\s\W]*/ :
                    operationType = "SDWANConnectivity"
                    execution.setVariable("isActivateRequired", "true")
                    break

                case ~/[\w\s\W]*sdwanvpnattachment[\w\s\W]*/ :
                    operationType = "SDWANAttachment"
                    execution.setVariable("isActivateRequired", "true")
                    break

                case ~/[\w\s\W]*SiteWANVF[\w\s\W]*/ :
                    operationType = "SDWANPort"
                    execution.setVariable("isActivateRequired", "true")
                    break

                default:
                    break
            }
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
            msoLogger.debug( msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    /**
     * Pre Process the BPMN Flow Request
     * Includes:
     * generate the nsOperationKey
     * generate the nsParameters
     */
    public void prepareSDNCRequest (DelegateExecution execution) {
        msoLogger.info(" ***** Started prepareSDNCRequest *****")

        try {
            // get variables
            String sdnc_svcAction = execution.getVariable(Prefix + "svcAction")
            String sdnc_requestAction = execution.getVariable(Prefix + "requestAction")
            String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
            String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

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
            String resourceInstnaceId = resourceInputObj.getResourceInstancenUuid()
            // 1. prepare assign topology via SDNC Adapter SUBFLOW call
            String sdncTopologyDeleteRequest = ""

            switch (modelName) {
                case ~/[\w\s\W]*deviceVF[\w\s\W]*/ :
                case ~/[\w\s\W]*SiteWANVF[\w\s\W]*/ :
                case ~/[\w\s\W]*SiteVF[\w\s\W]*/:
                    sdncTopologyDeleteRequest = """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                                              xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1" 
                                                              xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
                                 <sdncadapter:RequestHeader>
                                    <sdncadapter:RequestId>${msoUtils.xmlEncode(hdrRequestId)}</sdncadapter:RequestId>
                                    <sdncadapter:SvcInstanceId>${msoUtils.xmlEncode(serviceInstanceId)}</sdncadapter:SvcInstanceId>
                                    <sdncadapter:SvcAction>${msoUtils.xmlEncode(sdnc_svcAction)}</sdncadapter:SvcAction>
                                    <sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
                                    <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                                    <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
                                 </sdncadapter:RequestHeader>
                                 <sdncadapterworkflow:SDNCRequestData>
                                     <request-information>
                                        <request-id>${msoUtils.xmlEncode(hdrRequestId)}</request-id>
                                        <request-action>${msoUtils.xmlEncode(sdnc_requestAction)}</request-action>
                                        <source>${msoUtils.xmlEncode(source)}</source>
                                        <notification-url></notification-url>
                                        <order-number></order-number>
                                        <order-version></order-version>
                                     </request-information>
                                     <service-information>
                                        <service-id>${msoUtils.xmlEncode(serviceInstanceId)}</service-id>
                                        <subscription-service-type>${msoUtils.xmlEncode(serviceType)}</subscription-service-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEncode(serviceModelInvariantUuid)}</model-invariant-uuid>
                                             <model-uuid>${msoUtils.xmlEncode(serviceModelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEncode(serviceModelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEncode(serviceModelName)}</model-name>
                                        </onap-model-information>
                                        <service-instance-id>${msoUtils.xmlEncode(serviceInstanceId)}</service-instance-id>
                                        <global-customer-id>${msoUtils.xmlEncode(globalCustomerId)}</global-customer-id>
                                        <subscriber-name>${msoUtils.xmlEncode(globalCustomerId)}</subscriber-name>
                                     </service-information>
                                     <vnf-information>
                                        <vnf-id>$resourceInstnaceId</vnf-id>
                                        <vnf-type></vnf-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEncode(modelInvariantUuid)}</model-invariant-uuid>
                                             <model-customization-uuid>${msoUtils.xmlEncode(modelCustomizationUuid)}</model-customization-uuid>
                                             <model-uuid>${msoUtils.xmlEncode(modelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEncode(modelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEncode(modelName)}</model-name>
                                        </onap-model-information>
                                     </vnf-information>
                                     <vnf-request-input>
                                         <vnf-input-parameters>
                                         </vnf-input-parameters>
                                         <request-version></request-version>
                                         <vnf-name></vnf-name>
                                         <vnf-networks>
                                        </vnf-networks>
                                      </vnf-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()
                    break

                case ~/[\w\s\W]*sdwanvpnattachment[\w\s\W]*/ :
                case ~/[\w\s\W]*sotnvpnattachment[\w\s\W]*/ :
                    sdncTopologyDeleteRequest = """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                                              xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1" 
                                                              xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
                                 <sdncadapter:RequestHeader>
                                    <sdncadapter:RequestId>${msoUtils.xmlEncode(hdrRequestId)}</sdncadapter:RequestId>
                                    <sdncadapter:SvcInstanceId>${msoUtils.xmlEncode(serviceInstanceId)}</sdncadapter:SvcInstanceId>
                                    <sdncadapter:SvcAction>${msoUtils.xmlEncode(sdnc_svcAction)}</sdncadapter:SvcAction>
                                    <sdncadapter:SvcOperation>connection-attachment-topology-operation</sdncadapter:SvcOperation>
                                    <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                                    <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
                                 </sdncadapter:RequestHeader>
                                 <sdncadapterworkflow:SDNCRequestData>
                                     <request-information>
                                        <request-id>${msoUtils.xmlEncode(hdrRequestId)}</request-id>
                                        <request-action>${msoUtils.xmlEncode(sdnc_requestAction)}</request-action>
                                        <source>${msoUtils.xmlEncode(source)}</source>
                                        <notification-url></notification-url>
                                        <order-number></order-number>
                                        <order-version></order-version>
                                     </request-information>
                                     <service-information>
                                        <service-id>${msoUtils.xmlEncode(serviceInstanceId)}</service-id>
                                        <subscription-service-type>${msoUtils.xmlEncode(serviceType)}</subscription-service-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEncode(serviceModelInvariantUuid)}</model-invariant-uuid>
                                             <model-uuid>${msoUtils.xmlEncode(serviceModelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEncode(serviceModelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEncode(serviceModelName)}</model-name>
                                        </onap-model-information>
                                        <service-instance-id>${msoUtils.xmlEncode(serviceInstanceId)}</service-instance-id>
                                        <global-customer-id>${msoUtils.xmlEncode(globalCustomerId)}</global-customer-id>
                                        <subscriber-name></subscriber-name>
                                     </service-information>
                                     <allotted-resource-information>
                                        <allotted-resource-id>$resourceInstnaceId</allotted-resource-id>
                                        <allotted-resource-type></allotted-resource-type>
                                        <parent-service-instance-id></parent-service-instance-id>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEncode(modelInvariantUuid)}</model-invariant-uuid>
                                             <model-customization-uuid>${msoUtils.xmlEncode(modelCustomizationUuid)}</model-customization-uuid>
                                             <model-uuid>${msoUtils.xmlEncode(modelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEncode(modelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEncode(modelName)}</model-name>
                                        </onap-model-information>
                                     </allotted-resource-information>
                                     <connection-attachment-request-input>
                                     </connection-attachment-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()
                    break

                default:
                    sdncTopologyDeleteRequest = """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                                              xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1" 
                                                              xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
                                 <sdncadapter:RequestHeader>
                                    <sdncadapter:RequestId>${msoUtils.xmlEncode(hdrRequestId)}</sdncadapter:RequestId>
                                    <sdncadapter:SvcInstanceId>${msoUtils.xmlEncode(serviceInstanceId)}</sdncadapter:SvcInstanceId>
                                    <sdncadapter:SvcAction>${msoUtils.xmlEncode(sdnc_svcAction)}</sdncadapter:SvcAction>
                                    <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
                                    <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                                    <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
                                 </sdncadapter:RequestHeader>
                                 <sdncadapterworkflow:SDNCRequestData>
                                     <request-information>
                                        <request-id>${msoUtils.xmlEncode(hdrRequestId)}</request-id>
                                        <request-action>${msoUtils.xmlEncode(sdnc_requestAction)}</request-action>
                                        <source>${msoUtils.xmlEncode(source)}</source>
                                        <notification-url></notification-url>
                                        <order-number></order-number>
                                        <order-version></order-version>
                                     </request-information>
                                     <service-information>
                                        <service-id>${msoUtils.xmlEncode(serviceInstanceId)}</service-id>
                                        <subscription-service-type>${msoUtils.xmlEncode(serviceType)}</subscription-service-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEncode(serviceModelInvariantUuid)}</model-invariant-uuid>
                                             <model-uuid>${msoUtils.xmlEncode(serviceModelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEncode(serviceModelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEncode(serviceModelName)}</model-name>
                                        </onap-model-information>
                                        <service-instance-id>${msoUtils.xmlEncode(serviceInstanceId)}</service-instance-id>
                                        <global-customer-id>${msoUtils.xmlEncode(globalCustomerId)}</global-customer-id>
                                     </service-information>
                                     <network-information>
                                        <network-id>$resourceInstnaceId</network-id>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEncode(modelInvariantUuid)}</model-invariant-uuid>
                                             <model-customization-uuid>${msoUtils.xmlEncode(modelCustomizationUuid)}</model-customization-uuid>
                                             <model-uuid>${msoUtils.xmlEncode(modelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEncode(modelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEncode(modelName)}</model-name>
                                        </onap-model-information>
                                     </network-information>
                                     <network-request-input>
                                       <network-input-parameters></network-input-parameters>
                                     </network-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()
            }

            String sdncTopologyDeleteRequesAsString = utils.formatXml(sdncTopologyDeleteRequest)
            utils.logAudit(sdncTopologyDeleteRequesAsString)
            execution.setVariable("sdncAdapterWorkflowRequest", sdncTopologyDeleteRequesAsString)
            msoLogger.info("sdncAdapterWorkflowRequest - " + "\n" +  sdncTopologyDeleteRequesAsString)

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DeleteSDNCCNetworkResource flow. prepareSDNCRequest() - " + ex.getMessage()
            msoLogger.debug( exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }
        msoLogger.info(" ***** Exit prepareSDNCRequest *****")
    }

    private void setProgressUpdateVariables(DelegateExecution execution, String body) {
        def dbAdapterEndpoint = execution.getVariable("URN_mso_adapters_openecomp_db_endpoint")
        execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
        execution.setVariable("CVFMI_updateResOperStatusRequest", body)
    }

    public void prepareUpdateBeforeDeleteSDNCResource(DelegateExecution execution) {
        ResourceInput resourceInputObj = execution.getVariable(Prefix + "resourceInput")
        String operType = resourceInputObj.getOperationType()
        String resourceCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
        String serviceInstanceId = resourceInputObj.getServiceInstanceId()
        String operationId = resourceInputObj.getOperationId()
        String progress = "20"
        String status = "processing"
        String statusDescription = "SDCN resource delete invoked"

        //String operationId = execution.getVariable("operationId")

        String body = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.openecomp.mso/requestsdb">
                        <soapenv:Header/>
                <soapenv:Body>
                    <ns:updateResourceOperationStatus>
                               <operType>${msoUtils.xmlEncode(operType)}</operType>
                               <operationId>${msoUtils.xmlEncode(operationId)}</operationId>
                               <progress>${msoUtils.xmlEncode(progress)}</progress>
                               <resourceTemplateUUID>${msoUtils.xmlEncode(resourceCustomizationUuid)}</resourceTemplateUUID>
                               <serviceId>${msoUtils.xmlEncode(serviceInstanceId)}</serviceId>
                               <status>${msoUtils.xmlEncode(status)}</status>
                               <statusDescription>${msoUtils.xmlEncode(statusDescription)}</statusDescription>
                    </ns:updateResourceOperationStatus>
                </soapenv:Body>
                </soapenv:Envelope>""";

        setProgressUpdateVariables(execution, body)

    }

    public void prepareUpdateAfterDeleteSDNCResource(DelegateExecution execution) {
        ResourceInput resourceInputObj = execution.getVariable(Prefix + "resourceInput")
        String operType = resourceInputObj.getOperationType()
        String resourceCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
        String serviceInstanceId = resourceInputObj.getServiceInstanceId()
        String operationId = resourceInputObj.getOperationId()
        String progress = "100"
        String status = "finished"
        String statusDescription = "SDCN resource delete and deactivation completed"

        //String operationId = execution.getVariable("operationId")

        String body = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.openecomp.mso/requestsdb">
                        <soapenv:Header/>
                <soapenv:Body>
                    <ns:updateResourceOperationStatus>
                               <operType>${msoUtils.xmlEncode(operType)}</operType>
                               <operationId>${msoUtils.xmlEncode(operationId)}</operationId>
                               <progress>${msoUtils.xmlEncode(progress)}</progress>
                               <resourceTemplateUUID>${msoUtils.xmlEncode(resourceCustomizationUuid)}</resourceTemplateUUID>
                               <serviceId>${msoUtils.xmlEncode(serviceInstanceId)}</serviceId>
                               <status>${msoUtils.xmlEncode(status)}</status>
                               <statusDescription>${msoUtils.xmlEncode(statusDescription)}</statusDescription>
                    </ns:updateResourceOperationStatus>
                </soapenv:Body>
                </soapenv:Envelope>""";

        setProgressUpdateVariables(execution, body)
    }

    public void postDeleteSDNCCall(DelegateExecution execution){
        msoLogger.info(" ***** Started prepareSDNCRequest *****")
        String responseCode = execution.getVariable(Prefix + "sdncDeleteReturnCode")
        String responseObj = execution.getVariable(Prefix + "SuccessIndicator")

        msoLogger.info("response from sdnc, response code :" + responseCode + "  response object :" + responseObj)
        msoLogger.info(" ***** Exit prepareSDNCRequest *****")
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
            String msg = "Exception in sendSyncResponse:" + ex.getMessage()
            msoLogger.debug( msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        msoLogger.debug(" ***** Exit sendSyncResponse *****")
    }
}
