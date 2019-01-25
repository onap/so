/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.VnfResource;

import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONException;
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.MsoLogger
import org.springframework.web.util.UriUtils

import groovy.json.*

/**
 * This groovy class supports the <class>CreateGenericALaCarteServiceInstance.bpmn</class> process.
 * AlaCarte flow for 1702 ServiceInstance Create
 *
 */
public class CreateGenericALaCarteServiceInstance extends AbstractServiceTaskProcessor {
    String Prefix="CRESI_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CreateGenericALaCarteServiceInstance.class);

    public void preProcessRequest (DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        execution.setVariable("prefix",Prefix)
        String msg = ""

        try {

            String siRequest = execution.getVariable("bpmnRequest")
            msoLogger.debug(siRequest)

            String requestId = execution.getVariable("mso-request-id")
            execution.setVariable("msoRequestId", requestId)
            msoLogger.debug("Input Request:" + siRequest + " reqId:" + requestId)

            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)) {
                serviceInstanceId = UUID.randomUUID().toString()
                msoLogger.debug("Generated new Service Instance ID:" + serviceInstanceId)
            } else {
                msoLogger.debug("Using provided Service Instance ID:" + serviceInstanceId)
            }

            serviceInstanceId = UriUtils.encode(serviceInstanceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceInstanceId)

            //subscriberInfo
            String globalSubscriberId = jsonUtil.getJsonValue(siRequest, "requestDetails.subscriberInfo.globalSubscriberId")
            if (isBlank(globalSubscriberId)) {
                msg = "Input globalSubscriberId' is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("globalSubscriberId", globalSubscriberId)
            }

            //requestInfo
            execution.setVariable("source", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.source"))
            execution.setVariable("serviceInstanceName", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.instanceName"))
            execution.setVariable("disableRollback", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.suppressRollback"))
            String productFamilyId = null;
            try {
                productFamilyId = jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.productFamilyId")
            } catch (JSONException e) {
                productFamilyId = null;
            }
            if (isBlank(productFamilyId))
            {
                msg = "Input productFamilyId is null"
                msoLogger.debug(msg)
                //exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("productFamilyId", productFamilyId)
            }

            //modelInfo
            String serviceModelInfo = jsonUtil.getJsonValue(siRequest, "requestDetails.modelInfo")
            if (isBlank(serviceModelInfo)) {
                msg = "Input serviceModelInfo is null"
                msoLogger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else
            {
                execution.setVariable("serviceModelInfo", serviceModelInfo)
            }

            msoLogger.debug("modelInfo" + serviceModelInfo)

            //requestParameters
            String subscriptionServiceType = jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.subscriptionServiceType")
            if (isBlank(subscriptionServiceType)) {
                msg = "Input subscriptionServiceType is null"
                msoLogger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("subscriptionServiceType", subscriptionServiceType)
            }


            /*
             * Extracting User Parameters from incoming Request and converting into a Map
             */
            def jsonSlurper = new JsonSlurper()
            def jsonOutput = new JsonOutput()

            Map reqMap = jsonSlurper.parseText(siRequest)

            //InputParams
            def userParams = reqMap.requestDetails?.requestParameters?.userParams

            Map<String, String> inputMap = [:]
            if (userParams) {
                userParams.each {
                    userParam ->
                        if ("Customer_Location".equals(userParam?.name)) {
                            msoLogger.debug("User Input customerLocation: " + userParam.value.toString())
                            Map<String, String> customerMap = [:]
                            userParam.value.each {
                                param ->

                                    inputMap.put(param.key, param.value)
                                    customerMap.put(param.key, param.value)
                            }
                            execution.setVariable("customerLocation", customerMap)
                        }
                        if ("Homing_Solution".equals(userParam?.name)) {
                            msoLogger.debug("User Input Homing_Solution: " + userParam.value.toString())
                            execution.setVariable("homingService", userParam.value)
                            execution.setVariable("callHoming", true)
                            inputMap.put("Homing_Solution", userParam.value)
                        }
                        if (!"Homing_Solution".equals(userParam?.name) && !"Customer_Location".equals(userParam?.name))
                        {
                            msoLogger.debug("User Input Parameter " + userParam.name + ": " + userParam.value.toString())
                            inputMap.put(userParam.name, userParam.value)
                        }
                        if ("Orchestrator".equalsIgnoreCase(userParam?.name)) {
                            execution.setVariable("orchestrator", userParam.value)
                            inputMap.put("orchestrator", userParam.value)
                        }
                }
            }

            msoLogger.debug("User Input Parameters map: " + userParams.toString())
            msoLogger.debug("User Input Map: " + inputMap.toString())
            if (inputMap.toString() != "[:]") {
                execution.setVariable("serviceInputParams", inputMap)
            }

            //TODO
            //execution.setVariable("failExists", true)

        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            msoLogger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        msoLogger.trace("Exit preProcessRequest")
    }

    public void sendSyncResponse (DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        msoLogger.trace("Start sendSyncResponse")

        try {
            String requestId = execution.getVariable("msoRequestId")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            // RESTResponse for API Handler (APIH) Reply Task
            String createServiceRestRequest = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()
            msoLogger.debug(" sendSyncResponse to APIH:" + "\n" + createServiceRestRequest)
            sendWorkflowResponse(execution, 202, createServiceRestRequest)
            execution.setVariable("sentSyncResponse", true)

        } catch (Exception ex) {
            String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
            msoLogger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        msoLogger.trace("Exit sendSyncResopnse")
    }


    public void sendSyncError (DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        msoLogger.trace("Start sendSyncError")

        try {
            String errorMessage = ""
            if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
                WorkflowException wfe = execution.getVariable("WorkflowException")
                errorMessage = wfe.getErrorMessage()
            } else {
                errorMessage = "Sending Sync Error."
            }

            String buildworkflowException =
                    """<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
                    <aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
                    <aetgt:ErrorCode>7000</aetgt:ErrorCode>
                   </aetgt:WorkflowException>"""

            msoLogger.debug(buildworkflowException)
            sendWorkflowResponse(execution, 500, buildworkflowException)

        } catch (Exception ex) {
            msoLogger.debug(" Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
        }

    }

    // *******************************
    //
    // *******************************
    public void prepareDecomposeService(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        msoLogger.trace("Inside prepareDecomposeService of CreateGenericALaCarteServiceInstance ")
        try {
            String siRequest = execution.getVariable("bpmnRequest")
            String serviceModelInfo = jsonUtil.getJsonValue(siRequest, "requestDetails.modelInfo")
            execution.setVariable("serviceModelInfo", serviceModelInfo)
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in CreateGenericALaCarteServiceInstance flow. Unexpected Error from method prepareDecomposeService() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
        msoLogger.trace("Completed prepareDecomposeService of CreateGenericALaCarteServiceInstance")
     }

    public void processDecomposition(DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

        msoLogger.trace("Inside processDecomposition() of CreateGenericALaCarteServiceInstance  ")

        try {

            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")

            // VNFs
            List<VnfResource> vnfList = serviceDecomposition.getVnfResources()
            serviceDecomposition.setVnfResources(vnfList)

            execution.setVariable("vnfList", vnfList)
            execution.setVariable("vnfListString", vnfList.toString())

            String vnfModelInfoString = ""
            if (vnfList != null && vnfList.size() > 0) {
                execution.setVariable(Prefix + "VNFsCount", vnfList.size())
                msoLogger.debug("vnfs to create: " + vnfList.size())
                ModelInfo vnfModelInfo = vnfList[0].getModelInfo()

                vnfModelInfoString = vnfModelInfo.toString()
                String vnfModelInfoWithRoot = vnfModelInfo.toString()
                vnfModelInfoString = jsonUtil.getJsonValue(vnfModelInfoWithRoot, "modelInfo")
            } else {
                execution.setVariable(Prefix + "VNFsCount", 0)
                msoLogger.debug("no vnfs to create based upon serviceDecomposition content")
            }

            execution.setVariable("vnfModelInfo", vnfModelInfoString)
            execution.setVariable("vnfModelInfoString", vnfModelInfoString)
            msoLogger.debug(" vnfModelInfoString :" + vnfModelInfoString)

            msoLogger.trace("Completed processDecomposition() of CreateGenericALaCarteServiceInstance ")
        } catch (Exception ex) {
            sendSyncError(execution)
            String exceptionMessage = "Bpmn error encountered in CreateGenericALaCarteServiceInstance flow. processDecomposition() - " + ex.getMessage()
            msoLogger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

     // *******************************
     //
     // *******************************
     public void prepareCreateServiceInstance(DelegateExecution execution) {
         def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
 
         try {
             msoLogger.trace("Inside prepareCreateServiceInstance of CreateGenericALaCarteServiceInstance")
 
             /*
              * Extracting User Parameters from incoming Request and converting into a Map
              */
             def jsonSlurper = new JsonSlurper()
             def jsonOutput = new JsonOutput()
             def siRequest = execution.getVariable("bpmnRequest")
             Map reqMap = jsonSlurper.parseText(siRequest)

             //InputParams
             def userParams = reqMap.requestDetails?.requestParameters?.userParams

             Map<String, String> inputMap = [:]
             if (userParams) {
                 userParams.each {
                     userParam ->
                         if ("Customer_Location".equals(userParam?.name)) {
                             msoLogger.debug("User Input customerLocation: " + userParam.value.toString())
                             Map<String, String> customerMap = [:]
                             userParam.value.each {
                                 param ->

                                     inputMap.put(param.key, param.value)
                                     customerMap.put(param.key, param.value)
                             }
                             execution.setVariable("customerLocation", customerMap)
                         }
                         if ("Homing_Solution".equals(userParam?.name)) {
                             msoLogger.debug("User Input Homing_Solution: " + userParam.value.toString())
                             execution.setVariable("homingService", userParam.value)
                             execution.setVariable("callHoming", true)
                             inputMap.put("Homing_Solution", userParam.value)
                         }
                         if (!"Homing_Solution".equals(userParam?.name) && !"Customer_Location".equals(userParam?.name))
                         {
                             msoLogger.debug("User Input Parameter " + userParam.name + ": " + userParam.value.toString())
                             inputMap.put(userParam.name, userParam.value)
                         }
                         if ("Orchestrator".equalsIgnoreCase(userParam?.name)) {
                             execution.setVariable("orchestrator", userParam.value)
                             inputMap.put("orchestrator", userParam.value)
                         }
                 }
             }

             msoLogger.debug("User Input Parameters map: " + userParams.toString())
             msoLogger.debug("User Input Map: " + inputMap.toString())
             if (inputMap.toString() != "[:]") {
                 execution.setVariable("serviceInputParams", inputMap)
             }
             ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")

             String serviceInstanceId = execution.getVariable("serviceInstanceId")
             serviceDecomposition.getServiceInstance().setInstanceId(serviceInstanceId)

             String serviceInstanceName = jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.instanceName")
             serviceDecomposition.getServiceInstance().setInstanceName(serviceInstanceName)
             execution.setVariable("serviceInstanceName", serviceInstanceName)
             execution.setVariable("serviceDecomposition", serviceDecomposition)
             execution.setVariable("serviceDecompositionString", serviceDecomposition.toJsonString())
             msoLogger.debug("serviceDecomposition.serviceInstanceName: " + serviceDecomposition.getServiceInstance().getInstanceName())

             msoLogger.trace("Completed prepareCreateServiceInstance of CreateGenericALaCarteServiceInstance ***** ")
         } catch (Exception ex) {
             // try error in method block
             String exceptionMessage = "Bpmn error encountered in CreateGenericALaCarteServiceInstance flow. Unexpected Error from method prepareCreateServiceInstance() - " + ex.getMessage()
             exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
         }
      }


    public void prepareCompletionRequest (DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        msoLogger.trace("prepareCompletion *** ")

        try {
            String requestId = execution.getVariable("msoRequestId")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            String source = execution.getVariable("source")

            String msoCompletionRequest =
                    """<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                xmlns:ns="http://org.onap/so/request/types/v1">
                        <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
                            <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
                            <action>CREATE</action>
                            <source>${MsoUtils.xmlEscape(source)}</source>
                        </request-info>
                        <status-message>Service Instance was created successfully.</status-message>
                        <serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
                        <mso-bpel-name>CreateGenericALaCarteServiceInstance</mso-bpel-name>
                    </aetgt:MsoCompletionRequest>"""

            // Format Response
            String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

            execution.setVariable("completionRequest", xmlMsoCompletionRequest)
            msoLogger.debug(" Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)

        } catch (Exception ex) {
            String msg = " Exception in prepareCompletion:" + ex.getMessage()
            msoLogger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        msoLogger.trace("Exit prepareCompletionRequest")
    }

    public void prepareFalloutRequest(DelegateExecution execution){
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        msoLogger.trace("prepareFalloutRequest")

        try {
            WorkflowException wfex = execution.getVariable("WorkflowException")
            msoLogger.debug(" Input Workflow Exception: " + wfex.toString())
            String requestId = execution.getVariable("msoRequestId")
            String source = execution.getVariable("source")
            String requestInfo =
                    """<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
                    <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
                    <action>CREATE</action>
                    <source>${MsoUtils.xmlEscape(source)}</source>
                   </request-info>"""

            String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)
            execution.setVariable("falloutRequest", falloutRequest)
        } catch (Exception ex) {
            msoLogger.debug("Exception prepareFalloutRequest:" + ex.getMessage())
            String errorException = "  Bpmn error encountered in CreateGenericALaCarteServiceInstance flow. FalloutHandlerRequest,  buildErrorResponse() - " + ex.getMessage()
            String requestId = execution.getVariable("msoRequestId")
            String falloutRequest =
                    """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                                 xmlns:ns="http://org.onap/so/request/types/v1"
                                                 xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
                       <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
                          <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
                          <action>CREATE</action>
                          <source>VID</source>
                       </request-info>
                        <aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
                            <aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorException)}</aetgt:ErrorMessage>
                            <aetgt:ErrorCode>7000</aetgt:ErrorCode>
                        </aetgt:WorkflowException>
                    </aetgt:FalloutHandlerRequest>"""

            execution.setVariable("falloutRequest", falloutRequest)
        }
        msoLogger.trace("Exit prepareFalloutRequest")
    }
}