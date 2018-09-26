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
import org.json.XML
import org.onap.so.bpmn.infrastructure.pnf.implementation.AaiResponse;

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
import org.onap.so.rest.APIResponse
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.common.scripts.AaiUtil

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.onap.so.logger.MsoLogger

/**
 * This groovy class supports the <class>CreateSDNCCNetworkResource.bpmn</class> process.
 * flow for SDNC Network Resource Create
 */
public class CreateSDNCNetworkResource extends AbstractServiceTaskProcessor {

    private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CreateSDNCNetworkResource.class);
    String Prefix="CRESDNCRES_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

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
            switch (resourceInputObj.getResourceInstanceName()) {

                case ~/[\w\s\W]*overlay[\w\s\W]*/ :
                    //This will be resolved in R3.
                    sdnc_svcAction ="activate"
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

                case ~/[\w\s\W]*SiteWANVF[\w\s\W]*/ :
                    operationType = "SDWANPort"
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
            msoLogger.debug(msg)
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

    /**
     * This method updates the resource input by collecting required info from AAI
     * @param execution
     */
    public void updateResourceInput(DelegateExecution execution) {
        ResourceInput resourceInputObj = execution.getVariable(Prefix + "resourceInput")
        String modelName = resourceInputObj.getResourceModelInfo().getModelName()

        switch (modelName) {
            case ~/[\w\s\W]*SOTNConnectivity[\w\s\W]*/:

                def resourceInput = resourceInputObj.getResourceParameters()
                String incomingRequest = resourceInputObj.getRequestsInputs()
                String serviceParameters = JsonUtils.getJsonValue(incomingRequest, "service.parameters")
                String requestInputs = JsonUtils.getJsonValue(serviceParameters, "requestInputs")
                JSONObject inputParameters = new JSONObject(requestInputs)
                if(inputParameters.has("local-access-provider-id")) {
                    String uResourceInput = jsonUtil.addJsonValue(resourceInput, "requestInputs.access-provider-id", inputParameters.get("local-access-provider-id"))
                    uResourceInput = jsonUtil.addJsonValue(uResourceInput, "requestInputs.access-client-id", inputParameters.get("local-access-client-id"))
                    uResourceInput = jsonUtil.addJsonValue(uResourceInput, "requestInputs.access-topology-id", inputParameters.get("local-access-topology-id"))
                    uResourceInput = jsonUtil.addJsonValue(uResourceInput, "requestInputs.access-ltp-id", inputParameters.get("local-access-ltp-id"))
                    uResourceInput = jsonUtil.addJsonValue(uResourceInput, "requestInputs.access-node-id", inputParameters.get("local-access-node-id"))
                    resourceInputObj.setResourceParameters(uResourceInput)
                    execution.setVariable(Prefix + "resourceInput", resourceInputObj)
                }

                break

            case ~/[\w\s\W]*sdwanvpnattachment[\w\s\W]*/ :
            case ~/[\w\s\W]*sotnvpnattachment[\w\s\W]*/ :
                // fill attachment TP in networkInputParamJson
                String customer = resourceInputObj.getGlobalSubscriberId()
                String serviceType = resourceInputObj.getServiceType()

                def vpnName = StringUtils.containsIgnoreCase(modelName, "sotnvpnattachment") ? "sotnvpnattachmentvf_sotncondition_sotnVpnName" : "sdwanvpnattachmentvf_sdwancondition_sdwanVpnName"
                String parentServiceName = jsonUtil.getJsonValueForKey(resourceInputObj.getRequestsInputs(), vpnName)

                AaiUtil aaiUtil = new AaiUtil(this)
                String aai_endpoint = execution.getVariable("URN_aai_endpoint")
                String customerUri = aaiUtil.getBusinessCustomerUri(execution) + "/" + customer
                String aai_service_query_url = aai_endpoint + customerUri + "/service-subscriptions/service-subscription/" + serviceType + "/service-instances?service-instance-name=" + parentServiceName

                APIResponse aaiResponse = aaiUtil.executeAAIGetCall(execution, aai_service_query_url)
                def parentServiceInstanceId = getParentServiceInstnaceId(aaiResponse)
                execution.setVariable("parentServiceInstanceId", parentServiceInstanceId)
                break

            default:
                break
        }
    }

    private String getParentServiceInstnaceId(APIResponse aaiResponse) {
        String response = aaiResponse.getResponseBodyAsString()
        def xmlResp = new XmlParser().parseText(response)
        return "${xmlResp?."service-instance"[0]?."service-instance-id"[0]?.text()}"
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
            String createNetworkInput = execution.getVariable(Prefix + "networkRequest")

            String parentServiceInstanceId = execution.getVariable("parentServiceInstanceId")
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
            String sdncTopologyCreateRequest = ""

            switch (modelName) {
                case ~/[\w\s\W]*deviceVF[\w\s\W]*/ :
                case ~/[\w\s\W]*SiteWANVF[\w\s\W]*/ :
                case ~/[\w\s\W]*SiteVF[\w\s\W]*/:
                    sdncTopologyCreateRequest = """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
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
                                        <vnf-id></vnf-id>
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

                case ~/[\w\s\W]*sdwanvpnattachment[\w\s\W]*/ :
                case ~/[\w\s\W]*sotnvpnattachment[\w\s\W]*/ :
                    sdncTopologyCreateRequest = """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
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
                                        <subscriber-name>${msoUtils.xmlEncode(globalCustomerId)}</subscriber-name>
                                     </service-information>
                                     <allotted-resource-information>
                                        <!-- TODO: to be filled as per the request input -->
                                        <allotted-resource-id></allotted-resource-id>
                                        <allotted-resource-type></allotted-resource-type>
                                        <parent-service-instance-id>$parentServiceInstanceId</parent-service-instance-id>
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEncode(modelInvariantUuid)}</model-invariant-uuid>
                                             <model-customization-uuid>${msoUtils.xmlEncode(modelCustomizationUuid)}</model-customization-uuid>
                                             <model-uuid>${msoUtils.xmlEncode(modelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEncode(modelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEncode(modelName)}</model-name>
                                        </onap-model-information>
                                     </allotted-resource-information>
                                     <connection-attachment-request-input>
                                       $netowrkInputParameters
                                     </connection-attachment-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()
                    break

            // for SDWANConnectivity and SOTNConnectivity:
                default:
                    sdncTopologyCreateRequest = """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                                              xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1" 
                                                              xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
                                 <sdncadapter:RequestHeader>
                                    <sdncadapter:RequestId>${hdrRequestId}</sdncadapter:RequestId>
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
                                        <onap-model-information>
                                             <model-invariant-uuid>${msoUtils.xmlEncode(modelInvariantUuid)}</model-invariant-uuid>
                                             <model-customization-uuid>${msoUtils.xmlEncode(modelCustomizationUuid)}</model-customization-uuid>
                                             <model-uuid>${msoUtils.xmlEncode(modelUuid)}</model-uuid>
                                             <model-version>${msoUtils.xmlEncode(modelVersion)}</model-version>
                                             <model-name>${msoUtils.xmlEncode(modelName)}</model-name>
                                        </onap-model-information>
                                     </network-information>
                                     <network-request-input>
                                       <network-input-parameters>$netowrkInputParameters</network-input-parameters>
                                     </network-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()
            }

            String sndcTopologyCreateRequesAsString = utils.formatXml(sdncTopologyCreateRequest)
            utils.logAudit(sndcTopologyCreateRequesAsString)
            execution.setVariable("sdncAdapterWorkflowRequest", sndcTopologyCreateRequesAsString)
            msoLogger.debug("sdncAdapterWorkflowRequest - " + "\n" +  sndcTopologyCreateRequesAsString)

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in CreateSDNCCNetworkResource flow. prepareSDNCRequest() - " + ex.getMessage()
            msoLogger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }
        msoLogger.info(" ***** Exit prepareSDNCRequest *****")
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
                               <operType>${msoUtils.xmlEncode(operType)}</operType>
                               <operationId>${msoUtils.xmlEncode(operationId)}</operationId>
                               <progress>${msoUtils.xmlEncode(progress)}</progress>
                               <resourceTemplateUUID>${msoUtils.xmlEncode(resourceCustomizationUuid)}</resourceTemplateUUID>
                               <serviceId>${msoUtils.xmlEncode(ServiceInstanceId)}</serviceId>
                               <status>${msoUtils.xmlEncode(status)}</status>
                               <statusDescription>${msoUtils.xmlEncode(statusDescription)}</statusDescription>
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
        String status = "finished"
        String statusDescription = "SDCN resource creation and activation completed"

        execution.getVariable("operationId")

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
                               <serviceId>${msoUtils.xmlEncode(ServiceInstanceId)}</serviceId>
                               <status>${msoUtils.xmlEncode(status)}</status>
                               <statusDescription>${msoUtils.xmlEncode(statusDescription)}</statusDescription>
                    </ns:updateResourceOperationStatus>
                </soapenv:Body>
                </soapenv:Envelope>""";

        setProgressUpdateVariables(execution, body)
    }

    public void afterCreateSDNCCall(DelegateExecution execution){
        msoLogger.info(" ***** Started prepareSDNCRequest *****")
        String responseCode = execution.getVariable(Prefix + "sdncCreateReturnCode")
        String responseObj = execution.getVariable(Prefix + "SuccessIndicator")

        def isActivateRequried = execution.getVariable("isActivateRequired")
        if (StringUtils.equalsIgnoreCase(isActivateRequried, "true")) {
            def instnaceId = getInstnaceId(execution)
            execution.setVariable("networkInstanceId", instnaceId)
        }

        msoLogger.info("response from sdnc, response code :" + responseCode + "  response object :" + responseObj)
        msoLogger.info(" ***** Exit prepareSDNCRequest *****")
    }

    private def getInstnaceId(DelegateExecution execution) {
        def responce  = new XmlSlurper().parseText(execution.getVariable("CRENWKI_createSDNCResponse"))
        def data = responce.toString()
        data = data.substring(data.indexOf("<"))

        def resp = new XmlSlurper().parseText(data)
        ResourceInput resourceInputObj = execution.getVariable(Prefix + "resourceInput")
        String modelName = resourceInputObj.getResourceModelInfo().getModelName()
        def val = ""

        switch (modelName) {
            case  ~/[\w\s\W]*SOTNConnectivity[\w\s\W]*/ :
            case ~/[\w\s\W]*SDWANConnectivity[\w\s\W]*/ :
                val = resp."network-response-information"."instance-id"
                break

            case ~/[\w\s\W]*deviceVF[\w\s\W]*/ :
            case ~/[\w\s\W]*SiteWANVF[\w\s\W]*/ :
            case ~/[\w\s\W]*Site[\w\s\W]*/:
                val = resp."vnf-response-information"."instance-id"
                break

            case ~/[\w\s\W]*sdwanvpnattachment[\w\s\W]*/ :
            case ~/[\w\s\W]*sotnvpnattachment[\w\s\W]*/:
                val = resp."connection-attachment-response-information"."instance-id"
                break
        }

        return val.toString()
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
            String msg = "Exception in sendSyncResponse:" + ex.getMessage()
            msoLogger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        msoLogger.debug(" ***** Exit sendSyncResponse *****")
    }
}
