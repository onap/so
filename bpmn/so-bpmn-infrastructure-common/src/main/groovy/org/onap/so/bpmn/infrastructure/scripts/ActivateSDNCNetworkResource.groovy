/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import org.apache.commons.lang3.StringUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject
import org.json.XML
import org.onap.so.bpmn.common.recipe.ResourceInput
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ResourceType
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This groovy class supports the <class>ActivateSDNCCNetworkResource.bpmn</class> process.
 * flow for SDNC Network Resource Activate
 */
public class ActivateSDNCNetworkResource extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( ActivateSDNCNetworkResource.class)

    String Prefix = "ACTSDNCRES_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

    MsoUtils msoUtils = new MsoUtils()

     void preProcessRequest(DelegateExecution execution) {
        logger.info(" ***** Started preProcessRequest *****")

        try {
            //get bpmn inputs from resource request.
            String requestId = execution.getVariable("mso-request-id")
            String requestAction = execution.getVariable("requestAction")
            String recipeParamsFromRequest = execution.getVariable("recipeParams")
            String resourceInput = execution.getVariable("resourceInput")
            //Get ResourceInput Object
            ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(resourceInput, ResourceInput.class)
            execution.setVariable(Prefix + "resourceInput", resourceInputObj.toString())

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
            String operationTypeFromConfig = UrnPropertiesReader.getVariable("resource-config." + resourceInputObj.resourceModelInfo.getModelName() + ".operation-type")
            if (StringUtils.isNotEmpty(operationTypeFromConfig)) {
                // highest priority if operation type configured
                operationType = operationTypeFromConfig
            }

            String sdnc_svcAction = "activate"
            String sdnc_requestAction = sdnc_svcAction.capitalize() + operationType + "Instance"

            execution.setVariable(Prefix + "svcAction", sdnc_svcAction)
            execution.setVariable(Prefix + "requestAction", sdnc_requestAction)
            execution.setVariable(Prefix + "serviceInstanceId", resourceInputObj.getServiceInstanceId())
            execution.setVariable("mso-request-id", requestId)
            execution.setVariable("mso-service-instance-id", resourceInputObj.getServiceInstanceId())
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex){
            String msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

     void prepareUpdateAfterActivateSDNCResource(DelegateExecution execution) {
        logger.info("started prepareUpdateAfterActivateSDNCResource ")

        ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(execution.getVariable(Prefix + "resourceInput"), ResourceInput.class)
        String operType = resourceInputObj.getOperationType()
        String resourceCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
        String ServiceInstanceId = resourceInputObj.getServiceInstanceId()
        String operationId = resourceInputObj.getOperationId()
        String progress = "80"
        String status = "activated"
        String statusDescription = "SDCN resource activation completed"

        execution.getVariable("operationId")

        String body = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                <soapenv:Body>
                    <ns:updateResourceOperationStatus>
                               <operType>${msoUtils.xmlEscape(operType)}</operType>
                               <operationId>${msoUtils.xmlEscape(operationId)}</operationId>
                               <progress>${msoUtils.xmlEscape(progress)}</progress>
                               <resourceTemplateUUID>${msoUtils.xmlEscape(resourceCustomizationUuid)}</resourceTemplateUUID>
                               <serviceId>${msoUtils.xmlEscape(ServiceInstanceId)}</serviceId>
                               <status>${msoUtils.xmlEscape(status)}</status>
                               <statusDescription>${msoUtils.xmlEscape(statusDescription)}</statusDescription>
                    </ns:updateResourceOperationStatus>
                </soapenv:Body>
                </soapenv:Envelope>"""

        setProgressUpdateVariables(execution, body)
    }

    private void setProgressUpdateVariables(DelegateExecution execution, String body) {
        def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
        execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
        execution.setVariable("CVFMI_updateResOperStatusRequest", body)
    }

    String customizeResourceParam(String networkInputParametersJson) {
        List<Map<String, Object>> paramList = new ArrayList()
        JSONObject jsonObject = new JSONObject(networkInputParametersJson)
        Iterator iterator = jsonObject.keys()
        while (iterator.hasNext()) {
            String key = iterator.next()
            HashMap<String, String> hashMap = new HashMap()
            hashMap.put("name", key)
            hashMap.put("value", jsonObject.get(key))
            paramList.add(hashMap)
        }
        Map<String, List<Map<String, Object>>> paramMap = new HashMap()
        paramMap.put("param", paramList)

        return  new JSONObject(paramMap).toString()
    }

     void prepareSDNCRequest (DelegateExecution execution) {
        logger.info("Started prepareSDNCRequest ")

        try {
            // get variables
            String sdnc_svcAction = execution.getVariable(Prefix + "svcAction")
            String sdnc_requestAction = execution.getVariable(Prefix + "requestAction")
            String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
            String createNetworkInput = execution.getVariable(Prefix + "networkRequest")

            String parentServiceInstanceId = execution.getVariable("parentServiceInstanceId")
            String hdrRequestId = execution.getVariable("mso-request-id")
            String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")
            String source = execution.getVariable("source")
            String sdnc_service_id = execution.getVariable(Prefix + "sdncServiceId")
            ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(execution.getVariable(Prefix + "resourceInput"), ResourceInput.class)
            String resourceInstanceId = execution.getVariable("resourceInstanceId")
            String serviceType = resourceInputObj.getServiceType()
            String serviceModelInvariantUuid = resourceInputObj.getServiceModelInfo().getModelInvariantUuid()
            String serviceModelUuid = resourceInputObj.getServiceModelInfo().getModelUuid()
            String serviceModelVersion = resourceInputObj.getServiceModelInfo().getModelVersion()
            String serviceModelName = resourceInputObj.getServiceModelInfo().getModelName()
            String globalCustomerId = resourceInputObj.getGlobalSubscriberId()
            String modelInvariantUuid = resourceInputObj.getResourceModelInfo().getModelInvariantUuid()
            String modelCustomizationUuid = resourceInputObj.getResourceModelInfo().getModelCustomizationUuid()
            String modelUuid = resourceInputObj.getResourceModelInfo().getModelUuid()
            String modelName = resourceInputObj.getResourceModelInfo().getModelName()
            String modelVersion = resourceInputObj.getResourceModelInfo().getModelVersion()
            String resourceInputPrameters = resourceInputObj.getResourceParameters()
            String networkInputParametersJson = jsonUtil.getJsonValue(resourceInputPrameters, "requestInputs")
            //here convert json string to xml string
            String netowrkInputParameters = XML.toString(new JSONObject(customizeResourceParam(networkInputParametersJson)))
            // 1. prepare assign topology via SDNC Adapter SUBFLOW call
            String sdncTopologyActivateRequest = ""
            String modelType = resourceInputObj.getResourceModelInfo().getModelType()

            switch (modelType) {
                case "VNF" :
                    sdncTopologyActivateRequest = """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                                              xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1"
                                                              xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
                                 <sdncadapter:RequestHeader>
                                    <sdncadapter:RequestId>${msoUtils.xmlEscape(hdrRequestId)}</sdncadapter:RequestId>
                                    <sdncadapter:SvcInstanceId>${msoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
                                    <sdncadapter:SvcAction>${msoUtils.xmlEscape(sdnc_svcAction)}</sdncadapter:SvcAction>
                                    <sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
                                    <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                                    <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
                                 </sdncadapter:RequestHeader>
                                 <sdncadapterworkflow:SDNCRequestData>
                                     <request-information>
                                        <request-id>${msoUtils.xmlEscape(hdrRequestId)}</request-id>
                                        <request-action>${msoUtils.xmlEscape(sdnc_requestAction)}</request-action>
                                        <source>${msoUtils.xmlEscape(source)}</source>
                                        <notification-url></notification-url>
                                        <order-number></order-number>
                                        <order-version></order-version>
                                     </request-information>
                                     <service-information>
                                        <service-id>${msoUtils.xmlEscape(serviceInstanceId)}</service-id>
                                        <subscription-service-type>${msoUtils.xmlEscape(serviceType)}</subscription-service-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEscape(serviceModelInvariantUuid)}</model-invariant-uuid>
                                             <model-uuid>${msoUtils.xmlEscape(serviceModelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEscape(serviceModelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEscape(serviceModelName)}</model-name>
                                        </onap-model-information>
                                        <service-instance-id>${msoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
                                        <global-customer-id>${msoUtils.xmlEscape(globalCustomerId)}</global-customer-id>
                                        <subscriber-name>${msoUtils.xmlEscape(globalCustomerId)}</subscriber-name>
                                     </service-information>
                                     <vnf-information>
                                        <vnf-id>${msoUtils.xmlEscape(resourceInstanceId)}</vnf-id>
                                        <vnf-type></vnf-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEscape(modelInvariantUuid)}</model-invariant-uuid>
                                             <model-customization-uuid>${msoUtils.xmlEscape(modelCustomizationUuid)}</model-customization-uuid>
                                             <model-uuid>${msoUtils.xmlEscape(modelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEscape(modelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEscape(modelName)}</model-name>
                                        </onap-model-information>
                                     </vnf-information>
                                     <vnf-request-input>
                                         <vnf-input-parameters>
                                           $netowrkInputParameters
                                         </vnf-input-parameters>
                                         <request-version></request-version>
                                         <vnf-name></vnf-name>
                                         <vnf-networks>
                                        </vnf-networks>
                                      </vnf-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()
                    break

                case "GROUP" :
                    //When a new resource creation request reaches SO, the parent resources information needs to be provided
                    //while creating the child resource.
                    String vnfid = resourceInputObj.getVnfId()
                    ModelInfo vfModelInfo = resourceInputObj.getVfModelInfo()
                    String vnfmodelInvariantUuid = vfModelInfo.getModelInvariantUuid()
                    String vnfmodelCustomizationUuid = vfModelInfo.getModelCustomizationUuid()
                    String vnfmodelUuid = vfModelInfo.getModelUuid()
                    String vnfmodelVersion = vfModelInfo.getModelVersion()
                    String vnfmodelName = vfModelInfo.getModelName()
                    sdncTopologyActivateRequest = """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                                              xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1"
                                                              xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
                                 <sdncadapter:RequestHeader>
                                    <sdncadapter:RequestId>${msoUtils.xmlEscape(hdrRequestId)}</sdncadapter:RequestId>
                                    <sdncadapter:SvcInstanceId>${msoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
                                    <sdncadapter:SvcAction>${msoUtils.xmlEscape(sdnc_svcAction)}</sdncadapter:SvcAction>
                                    <sdncadapter:SvcOperation>vf-module-topology-operation</sdncadapter:SvcOperation>
                                    <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                                    <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
                                 </sdncadapter:RequestHeader>
                                 <sdncadapterworkflow:SDNCRequestData>
                                     <request-information>
                                        <request-id>${msoUtils.xmlEscape(hdrRequestId)}</request-id>
                                        <request-action>${msoUtils.xmlEscape(sdnc_requestAction)}</request-action>
                                        <source>${msoUtils.xmlEscape(source)}</source>
                                        <notification-url></notification-url>
                                        <order-number></order-number>
                                        <order-version></order-version>
                                     </request-information>
                                     <service-information>
                                        <service-id>${msoUtils.xmlEscape(serviceInstanceId)}</service-id>
                                        <subscription-service-type>${msoUtils.xmlEscape(serviceType)}</subscription-service-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEscape(serviceModelInvariantUuid)}</model-invariant-uuid>
                                             <model-uuid>${msoUtils.xmlEscape(serviceModelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEscape(serviceModelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEscape(serviceModelName)}</model-name>
                                        </onap-model-information>
                                        <service-instance-id>${msoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
                                        <global-customer-id>${msoUtils.xmlEscape(globalCustomerId)}</global-customer-id>
                                        <subscriber-name>${msoUtils.xmlEscape(globalCustomerId)}</subscriber-name>
                                     </service-information>
                                     <vnf-information>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEscape(vnfmodelInvariantUuid)}</model-invariant-uuid>
                                             <model-customization-uuid>${msoUtils.xmlEscape(vnfmodelCustomizationUuid)}</model-customization-uuid>
                                             <model-uuid>${msoUtils.xmlEscape(vnfmodelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEscape(vnfmodelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEscape(vnfmodelName)}</model-name>
                                        </onap-model-information>
                                        <vnf-id>${msoUtils.xmlEscape(vnfid)}</vnf-id>
                                     </vnf-information>
                                     <vf-module-information>
                                        <from-preload>false</from-preload>
                                        <vf-module-id>${msoUtils.xmlEscape(resourceInstanceId)}</vf-module-id>
                                        <onap-model-information>
                                            <model-invariant-uuid>${msoUtils.xmlEscape(modelInvariantUuid)}</model-invariant-uuid>
                                             <model-customization-uuid>${msoUtils.xmlEscape(modelCustomizationUuid)}</model-customization-uuid>
                                             <model-uuid>${msoUtils.xmlEscape(modelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEscape(modelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEscape(modelName)}</model-name>
                                        </onap-model-information>
                                     </vf-module-information>
                                     <vf-module-request-input>
                                         <vf-module-input-parameters>
                                           $netowrkInputParameters
                                         </vf-module-input-parameters>
                                      </vf-module-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()
                    break

                // sdwanvpnattachment or sotnvpnattachment
                case "ALLOTTED_RESOURCE" :
                    sdncTopologyActivateRequest =
                            """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                                              xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1" 
                                                              xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
                                 <sdncadapter:RequestHeader>
                                    <sdncadapter:RequestId>${msoUtils.xmlEscape(hdrRequestId)}</sdncadapter:RequestId>
                                    <sdncadapter:SvcInstanceId>${msoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
                                    <sdncadapter:SvcAction>${msoUtils.xmlEscape(sdnc_svcAction)}</sdncadapter:SvcAction>
                                    <sdncadapter:SvcOperation>connection-attachment-topology-operation</sdncadapter:SvcOperation>
                                    <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                                    <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
                                 </sdncadapter:RequestHeader>
                                 <sdncadapterworkflow:SDNCRequestData>
                                     <request-information>
                                        <request-id>${msoUtils.xmlEscape(hdrRequestId)}</request-id>
                                        <request-action>${msoUtils.xmlEscape(sdnc_requestAction)}</request-action>
                                        <source>${msoUtils.xmlEscape(source)}</source>
                                        <notification-url></notification-url>
                                        <order-number></order-number>
                                        <order-version></order-version>
                                     </request-information>
                                     <service-information>
                                        <service-id>${msoUtils.xmlEscape(serviceInstanceId)}</service-id>
                                        <subscription-service-type>${msoUtils.xmlEscape(serviceType)}</subscription-service-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEscape(serviceModelInvariantUuid)}</model-invariant-uuid>
                                             <model-uuid>${msoUtils.xmlEscape(serviceModelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEscape(serviceModelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEscape(serviceModelName)}</model-name>
                                        </onap-model-information>
                                        <service-instance-id>${msoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
                                        <global-customer-id>${msoUtils.xmlEscape(globalCustomerId)}</global-customer-id>
                                     </service-information>
                                     <allotted-resource-information>
                                        <allotted-resource-id>${msoUtils.xmlEscape(resourceInstanceId)}</allotted-resource-id>
                                        <allotted-resource-type></allotted-resource-type>
                                        <parent-service-instance-id>$parentServiceInstanceId</parent-service-instance-id>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEscape(modelInvariantUuid)}</model-invariant-uuid>
                                             <model-customization-uuid>${msoUtils.xmlEscape(modelCustomizationUuid)}</model-customization-uuid>
                                             <model-uuid>${msoUtils.xmlEscape(modelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEscape(modelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEscape(modelName)}</model-name>
                                        </onap-model-information>
                                     </allotted-resource-information>
                                     <connection-attachment-request-input>
                                       $netowrkInputParameters
                                     </connection-attachment-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()

                    break

            // for SDWANConnectivity and SOTN Connectivity
                default:
                    sdncTopologyActivateRequest =
                            """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                                              xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1" 
                                                              xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
                                 <sdncadapter:RequestHeader>
                                    <sdncadapter:RequestId>${msoUtils.xmlEscape(hdrRequestId)}</sdncadapter:RequestId>
                                    <sdncadapter:SvcInstanceId>${msoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
                                    <sdncadapter:SvcAction>${msoUtils.xmlEscape(sdnc_svcAction)}</sdncadapter:SvcAction>
                                    <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
                                    <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                                    <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
                                 </sdncadapter:RequestHeader>
                                 <sdncadapterworkflow:SDNCRequestData>
                                     <request-information>
                                        <request-id>${msoUtils.xmlEscape(hdrRequestId)}</request-id>
                                        <request-action>${msoUtils.xmlEscape(sdnc_requestAction)}</request-action>
                                        <source>${msoUtils.xmlEscape(source)}</source>
                                        <notification-url></notification-url>
                                        <order-number></order-number>
                                        <order-version></order-version>
                                     </request-information>
                                     <service-information>
                                        <service-id>${msoUtils.xmlEscape(serviceInstanceId)}</service-id>
                                        <subscription-service-type>${msoUtils.xmlEscape(serviceType)}</subscription-service-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEscape(serviceModelInvariantUuid)}</model-invariant-uuid>
                                             <model-uuid>${msoUtils.xmlEscape(serviceModelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEscape(serviceModelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEscape(serviceModelName)}</model-name>
                                        </onap-model-information>
                                        <service-instance-id>${msoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
                                        <global-customer-id>${msoUtils.xmlEscape(globalCustomerId)}</global-customer-id>
                                     </service-information>
                                     <network-information>
                                        <network-id>${msoUtils.xmlEscape(resourceInstanceId)}</network-id>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEscape(modelInvariantUuid)}</model-invariant-uuid>
                                             <model-customization-uuid>${msoUtils.xmlEscape(modelCustomizationUuid)}</model-customization-uuid>
                                             <model-uuid>${msoUtils.xmlEscape(modelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEscape(modelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEscape(modelName)}</model-name>
                                        </onap-model-information>
                                     </network-information>
                                     <network-request-input>
                                       <network-input-parameters>$netowrkInputParameters</network-input-parameters>
                                     </network-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()

            }

            String sdncTopologyActivateRequesAsString = utils.formatXml(sdncTopologyActivateRequest)
            execution.setVariable("sdncAdapterWorkflowRequest", sdncTopologyActivateRequesAsString)

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in CreateSDNCCNetworkResource flow. prepareSDNCRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }
        logger.info(" ***** Exit prepareSDNCRequest *****")
    }

     void postActivateSDNCCall(DelegateExecution execution) {
        logger.info("started postCreateSDNCCall ")

        String responseCode = execution.getVariable(Prefix + "sdncCreateReturnCode")
        String responseObj = execution.getVariable(Prefix + "SuccessIndicator")

        logger.info("response from sdnc, response code :" + responseCode + "  response object :" + responseObj)
    }

     void sendSyncResponse(DelegateExecution execution) {
        logger.info("started sendsyncResp")

        try {
            String operationStatus = "finished"
            // RESTResponse for main flow
            String resourceOperationResp = """{"operationStatus":"${operationStatus}"}""".trim()
            logger.debug(" sendSyncResponse to APIH:" + "\n" + resourceOperationResp)
            sendWorkflowResponse(execution, 202, resourceOperationResp)
            execution.setVariable("sentSyncResponse", true)

        } catch (Exception ex) {
            String msg = "Exception in sendSyncResponse:" + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.info("exited send sync Resp")
    }
}
