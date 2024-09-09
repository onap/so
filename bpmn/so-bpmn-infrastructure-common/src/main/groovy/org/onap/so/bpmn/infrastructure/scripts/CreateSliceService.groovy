/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.scripts

import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.serviceinstancebeans.Service

import static org.apache.commons.lang3.StringUtils.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.beans.nsmf.SliceTaskParams
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter
import org.onap.so.beans.nsmf.oof.TemplateInfo
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.NssmfAdapterUtils
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.OrchestrationTask
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper

public class CreateSliceService extends AbstractServiceTaskProcessor {
    String Prefix = "CRESS_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    JsonSlurper jsonSlurper = new JsonSlurper()

    ObjectMapper objectMapper = new ObjectMapper()

    OofUtils oofUtils = new OofUtils()

    AAIResourcesClient client = getAAIClient()

    private static final Logger logger = LoggerFactory.getLogger(CreateSliceService.class)

    public void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")
        execution.setVariable("prefix", Prefix)
        String msg = ""

        try {
            String ssRequest = execution.getVariable("bpmnRequest")
            logger.debug(ssRequest)

            String requestId = execution.getVariable("mso-request-id")
            execution.setVariable("msoRequestId", requestId)
            logger.debug("Input Request:" + ssRequest + " reqId:" + requestId)

            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)) {
                serviceInstanceId = UUID.randomUUID().toString()
            }

            String operationId = UUID.randomUUID().toString()
            execution.setVariable("operationId", operationId)

            logger.debug("Generated new Service Instance:" + serviceInstanceId)
            serviceInstanceId = UriUtils.encode(serviceInstanceId, "UTF-8")
            execution.setVariable("serviceInstanceId", serviceInstanceId)

            //subscriberInfo
            String globalSubscriberId = jsonUtil.getJsonValue(ssRequest, "requestDetails.subscriberInfo.globalSubscriberId")
            if (isBlank(globalSubscriberId)) {
                msg = "Input globalSubscriberId' is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("globalSubscriberId", globalSubscriberId)
            }

            //requestInfo
            execution.setVariable("source", jsonUtil.getJsonValue(ssRequest, "requestDetails.requestInfo.source"))
            execution.setVariable("serviceInstanceName", jsonUtil.getJsonValue(ssRequest, "requestDetails.requestInfo.instanceName"))
            execution.setVariable("disableRollback", jsonUtil.getJsonValue(ssRequest, "requestDetails.requestInfo.suppressRollback"))
            String productFamilyId = jsonUtil.getJsonValue(ssRequest, "requestDetails.requestInfo.productFamilyId")
            if (isBlank(productFamilyId)) {
                msg = "Input productFamilyId is null"
                logger.debug(msg)
                //exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("productFamilyId", productFamilyId)
            }

            //modelInfo
            String serviceModelInfo = jsonUtil.getJsonValue(ssRequest, "requestDetails.modelInfo")
            if (isBlank(serviceModelInfo)) {
                msg = "Input serviceModelInfo is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("serviceModelInfo", serviceModelInfo)
            }

            logger.debug("modelInfo: " + serviceModelInfo)

//            //requestParameters
//            String subscriptionServiceType = jsonUtil.getJsonValue(ssRequest, "requestDetails.requestParameters.subscriptionServiceType")
//            if (isBlank(subscriptionServiceType)) {
//                msg = "Input subscriptionServiceType is null"
//                logger.debug(msg)
//                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
//            } else {
//                subscriptionServiceType = "5G"
//                execution.setVariable("subscriptionServiceType", subscriptionServiceType)
//            }
//            logger.debug("subscriptionServiceType: " + subscriptionServiceType)

            /*
            * Extracting User Parameters from incoming Request and converting into a Map
            */
            Map reqMap = jsonSlurper.parseText(ssRequest) as Map

            //InputParams
            def userParamsList = reqMap.requestDetails?.requestParameters?.userParams

            Map<String, String> inputMap = [:]
            if (userParamsList) {
                for (def i = 0; i < userParamsList.size(); i++) {
                    def userParams1 = userParamsList.get(i)
                    userParams1.each { param -> inputMap.put(param.key, param.value) }
                }
            }

            logger.debug("User Input Parameters map: " + inputMap.toString())
            String uuiRequest = inputMap.get("UUIRequest")
            Map uuiReqMap = jsonSlurper.parseText(uuiRequest) as Map
            Map<String, Object> serviceObject = (Map<String, Object>) uuiReqMap.get("service")
            Map<String, Object> parameterObject = (Map<String, Object>) serviceObject.get("parameters")
            Map<String, Object> requestInputs = (Map<String, Object>) parameterObject.get("requestInputs")

            def serviceProfile = [:]
            for(entry in requestInputs) {
                serviceProfile[entry.key] = entry.value
            }

            execution.setVariable("serviceInputParams", inputMap)
            execution.setVariable("uuiRequest", uuiRequest)
            execution.setVariable("serviceProfile", serviceProfile)
            execution.setVariable("subscriptionServiceType", serviceObject.get("serviceType"))

            //TODO
            //execution.setVariable("serviceInputParams", jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.userParams"))
            //execution.setVariable("failExists", true)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("Finish preProcessRequest")
    }

    /**
     *
     * @param execution
     */
    public void prepareInitServiceOperationStatus(DelegateExecution execution) {
        logger.debug("Start prepareInitServiceOperationStatus")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = "CREATE"
            String userId = execution.getVariable("globalSubscriberId")
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service creation"
            logger.debug("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationType", operationType)

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint",execution)
            execution.setVariable("CSSOS_dbAdapterEndpoint", dbAdapterEndpoint)
            logger.debug("DB Adapter Endpoint is: " + dbAdapterEndpoint)
            def dbAdapterAuth = UrnPropertiesReader.getVariable("mso.requestDb.auth")
            Map<String, String> CSSOS_headerMap = [:]
            CSSOS_headerMap.put("content-type", "application/soap+xml")
            CSSOS_headerMap.put("Authorization", dbAdapterAuth)
            execution.setVariable("CSSOS_headerMap", CSSOS_headerMap)
            logger.debug("DB Adapter Header is: " + CSSOS_headerMap)

            String payload =
                    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:initServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                            <userId>${MsoUtils.xmlEscape(userId)}</userId>
                            <result>${MsoUtils.xmlEscape(result)}</result>
                            <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                            <progress>${MsoUtils.xmlEscape(progress)}</progress>
                            <reason>${MsoUtils.xmlEscape(reason)}</reason>
                        </ns:initServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

            payload = utils.formatXml(payload)
            execution.setVariable("CSSOS_updateServiceOperStatusRequest", payload)
            logger.debug("Outgoing updateServiceOperStatusRequest: \n" + payload)
        }catch(Exception e){
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception Occured Processing prepareInitServiceOperationStatus.", "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e)
        }
        logger.debug("Finish prepareInitServiceOperationStatus")
    }

    /**
     * prepare create OrchestrationTask
     * @param execution
     */
    public void prepareCreateOrchestrationTask(DelegateExecution execution) {
        logger.debug("Start createOrchestrationTask")
        String taskId = execution.getBusinessKey()
        execution.setVariable("orchestrationTaskId", taskId)
        logger.debug("BusinessKey: " + taskId)
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String serviceInstanceName = execution.getVariable("serviceInstanceName")
        String taskName = "SliceServiceTask"
        String taskStatus = "Planning"
        String isManual = "false"
        String requestMethod = "POST"
        execution.setVariable("CSSOT_taskId", taskId)
        execution.setVariable("CSSOT_name", taskName)
        execution.setVariable("CSSOT_status", taskStatus)
        execution.setVariable("CSSOT_isManual", isManual)
        execution.setVariable("CSSOT_requestMethod", requestMethod)

        Map<String, Object> serviceProfile = execution.getVariable("serviceProfile") as Map<String, Object>

        SliceTaskParamsAdapter sliceTaskParams = new SliceTaskParamsAdapter()
        sliceTaskParams.setServiceId(serviceInstanceId)
        sliceTaskParams.setServiceName(serviceInstanceName)
        sliceTaskParams.setServiceProfile(serviceProfile)

        execution.setVariable("sliceTaskParams", sliceTaskParams)

        execution.setVariable("CSSOT_paramJson", objectMapper.writeValueAsString(sliceTaskParams))

        logger.debug("Finish createOrchestrationTask")
    }

    /**
     *  send sync response to csmf
     * @param execution
     */
    public void sendSyncResponse(DelegateExecution execution) {
        logger.debug("Start sendSyncResponse")
        try {
            String operationId = execution.getVariable("operationId")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            // RESTResponse for API Handler (APIH) Reply Task
            String createServiceRestRequest = """
                {
                   "service": {
                        "serviceId":"${serviceInstanceId}",
                        "operationId":"${operationId}"
                   }
                }
                """.trim()

            logger.debug("sendSyncResponse to APIH:" + "\n" + createServiceRestRequest)
            sendWorkflowResponse(execution, 202, createServiceRestRequest)
            execution.setVariable("sentSyncResponse", true)
        } catch (Exception e) {
            String msg = "Exceptuion in sendSyncResponse:" + e.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("Finish sendSyncResponse")
    }

    public void prepareSelectNSTRequest(DelegateExecution execution) {
        logger.debug("Start prepareSelectNSTRequest")
        String requestId = execution.getVariable("msoRequestId")
        String messageType = "NSTSelectionResponse"
        execution.setVariable("nstSelectionUrl", "/api/oof/v1/selection/nst")
        execution.setVariable("nstSelection_messageType", messageType)
        execution.setVariable("nstSelection_correlator", requestId)
        String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);
        execution.setVariable("nstSelection_timeout", timeout)

        Map<String, Object> serviceProfile = execution.getVariable("serviceProfile") as Map<String, Object>
        serviceProfile.remove("profileId")
        String oofRequest = oofUtils.buildSelectNSTRequest(requestId, messageType, serviceProfile)
        execution.setVariable("nstSelection_oofRequest", oofRequest)
        logger.debug("Finish prepareSelectNSTRequest")

    }

    /**
     * process async response of oof, put the {@solutions} at {@nstSolution}
     * @param execution
     */
    public void processNSTSolutions(DelegateExecution execution) {
        Map<String, Object> nstSolution
        try {
            logger.debug("Start processing NSTSolutions")
            Map<String, Object> resMap =
                    objectMapper.readValue(execution.getVariable("nstSelection_oofResponse") as String,
                            Map.class)

            List<Map<String, Object>> nstSolutions = (List<Map<String, Object>>) resMap.get("solutions")
            nstSolution = nstSolutions.get(0)
            execution.setVariable("nstSolution", nstSolution)

            //set nst info into sliceTaskParams
            SliceTaskParamsAdapter sliceTaskParams =
                    execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
            TemplateInfo nstInfo = new TemplateInfo()
            nstInfo.setUUID(nstSolution.get("UUID") as String)
            nstInfo.setInvariantUUID(nstSolution.get("invariantUUID") as String)
            nstInfo.setName(nstSolution.get("NSTName") as String)

            sliceTaskParams.setNSTInfo(nstInfo)
            sliceTaskParams.setNstId(nstSolution.get("UUID") as String)
            sliceTaskParams.setNstName(nstSolution.get("NSTName") as String)

            execution.setVariable("sliceTaskParams", sliceTaskParams)

        } catch (Exception ex) {
            logger.debug( "Failed to get NST solution suggested by OOF.")
            exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Failed to get NST solution suggested by OOF.")
        }

    }

    public void prepareUpdateOrchestrationTask(DelegateExecution execution) {
        logger.debug("Start prepareUpdateOrchestrationTask")
        String requestMethod = "PUT"
        String taskStatus = execution.getVariable("taskStatus")
        SliceTaskParamsAdapter sliceTaskParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        int nsstCount=execution.getVariable("nsstCount") as int
	nsstCount=nsstCount+1
        execution.setVariable("nsstCount", nsstCount)
        execution.setVariable("CSSOT_status", taskStatus)
        execution.setVariable("CSSOT_paramJson", objectMapper.writeValueAsString(sliceTaskParams))
        execution.setVariable("CSSOT_requestMethod", requestMethod)
        logger.debug("Finish prepareUpdateOrchestrationTask")
    }


    public void prepareGetUserOptions(DelegateExecution execution) {
        logger.debug("Start prepareGetUserOptions")
        String requestMethod = "GET"
        execution.setVariable("taskAction", "commit")
        String taskAction = execution.getVariable("taskAction")
        logger.debug("task action is: " + taskAction)
        if (!"commit".equals(taskAction) && !"abort".equals(taskAction)) {
            String msg = "Unknown task action: " + taskAction
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }
        execution.setVariable("CSSOT_requestMethod", requestMethod)
        logger.debug("Finish prepareGetUserOptions")
    }

    public void processUserOptions(DelegateExecution execution) {
        logger.debug("Start processUserOptions")
        String response = execution.getVariable("CSSOT_dbResponse")
        OrchestrationTask orchestrationTask = objectMapper.readValue(response, OrchestrationTask.class)
        String paramJson = orchestrationTask.getParams()
        logger.debug("paramJson: " + paramJson)

        SliceTaskParamsAdapter sliceTaskParams = objectMapper.readValue(paramJson, SliceTaskParamsAdapter.class)

        execution.setVariable("sliceTaskParams", sliceTaskParams)
        logger.debug("Finish processUserOptions")
    }

    public void updateAAIOrchStatus(DelegateExecution execution) {
        logger.debug("Start updateAAIOrchStatus")
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String orchStatus = execution.getVariable("orchestrationStatus")

        try {
            ServiceInstance si = new ServiceInstance()
            si.setOrchestrationStatus(orchStatus)

            AAIResourceUri uri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))
            client.update(uri, si)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in CreateSliceService.updateAAIOrchStatus " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug("Finish updateAAIOrchStatus")
    }

    public void prepareUpdateServiceOperationStatus(DelegateExecution execution) {
        logger.debug("Start preUpdateServiceOperationStatus")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = execution.getVariable("operationType")
            String userId = execution.getVariable("globalSubscriberId")
            String result = execution.getVariable("operationResult")
            String progress = execution.getVariable("operationProgress")
            String reason = execution.getVariable("operationReason")
            String operationContent = "service: " + result + " progress: " + progress

            String payload =
                    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:initServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                            <userId>${MsoUtils.xmlEscape(userId)}</userId>
                            <result>${MsoUtils.xmlEscape(result)}</result>
                            <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                            <progress>${MsoUtils.xmlEscape(progress)}</progress>
                            <reason>${MsoUtils.xmlEscape(reason)}</reason>
                        </ns:initServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

            payload = utils.formatXml(payload)
            execution.setVariable("CSSOS_updateServiceOperStatusRequest", payload)
            logger.debug("Outgoing updateServiceOperStatusRequest: \n" + payload)

        }catch(Exception e){
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception Occured Processing preUpdateServiceOperationStatus.", "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e.getMessage())
        }
        logger.debug("Finish preUpdateServiceOperationStatus")
    }



    public void prepareCompletionRequest (DelegateExecution execution) {
        logger.trace("Start prepareCompletionRequest")
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
            logger.debug("Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)

        } catch (Exception ex) {
            String msg = " Exception in prepareCompletion:" + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.trace("Finish prepareCompletionRequest")
    }

}

