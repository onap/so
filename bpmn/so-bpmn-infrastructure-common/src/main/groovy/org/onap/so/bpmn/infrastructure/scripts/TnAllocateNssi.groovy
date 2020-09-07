/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.beans.nsmf.SliceTaskParams
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils

import static org.apache.commons.lang3.StringUtils.isBlank

public class TnAllocateNssi extends AbstractServiceTaskProcessor {
    String Prefix = "TNALLOC_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    JsonSlurper jsonSlurper = new JsonSlurper()
    ObjectMapper objectMapper = new ObjectMapper()
    OofUtils oofUtils = new OofUtils()
    private static final Logger logger = LoggerFactory.getLogger(TnAllocateNssi.class)


    public void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")
        execution.setVariable("prefix", Prefix)
        String msg = ""

        try {
            String additionalPropJsonStr = execution.getVariable("sliceParams")
            logger.debug(additionalPropJsonStr)

            String requestId = execution.getVariable("msoRequestId")
            logger.debug("Input Request:" + additionalPropJsonStr + " reqId:" + requestId)

            String tnNssiId = execution.getVariable("serviceInstanceID")
            if (isBlank(tnNssiId)) {
                tnNssiId = UUID.randomUUID().toString()
            }

            String operationId = UUID.randomUUID().toString()
            execution.setVariable("operationId", operationId)

            logger.debug("Generate new TN NSSI ID:" + tnNssiId)
            tnNssiId = UriUtils.encode(tnNssiId, "UTF-8")
            execution.setVariable("tnNssiId", tnNssiId)


            //additional properties
            String sliceProfile = jsonUtil.getJsonValue(additionalPropJsonStr, "sliceProfile")
            if (isBlank(sliceProfile)) {
                msg = "Input sliceProfile is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("sliceProfile", sliceProfile)
            }

            String transportSliceNetworks = jsonUtil.getJsonValue(additionalPropJsonStr, "transportSliceNetworks")
            if (isBlank(transportSliceNetworks)) {
                msg = "Input transportSliceNetworks is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("transportSliceNetworks", transportSliceNetworks)
            }
            logger.debug("transportSliceNetworks: " + transportSliceNetworks)

            String nsiInfo = jsonUtil.getJsonValue(additionalPropJsonStr, "nsiInfo")
            if (isBlank(nsiInfo)) {
                msg = "Input nsiInfo is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("nsiInfo", nsiInfo)
            }

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("Finish preProcessRequest")
    }


    void prepareDecomposeService(DelegateExecution execution) {
        logger.debug("Start prepareDecomposeService")
        String modelUuid = execution.getVariable("modelUuid")
        if (isBlank(modelUuid)) {
            msg = "Input modelUuid is null"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }

        String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
        if (isBlank(modelInvariantUuid)) {
            msg = "Input modelInvariantUuid is null"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }

        String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("ssServiceModelInfo", serviceModelInfo)

        logger.debug("Finish prepareDecomposeService")
    }

    void processDecomposition(DelegateExecution execution) {
        logger.debug("Start processDecomposition")

        ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
        String nstName = serviceDecomposition.getModelInfo().getModelName()
        String nstId = serviceDecomposition.getModelInfo().getModelUuid()
        sliceTaskParams.setNstName(nstName)
        sliceTaskParams.setNstId(nstId)

        logger.debug("End processDecomposition")
    }

    public void prepareOofSelection(DelegateExecution execution) {
        logger.debug("Start prepareOofSelection")
        String requestId = execution.getVariable("msoRequestId")
        String messageType = "NSTSelectionResponse"
        Map<String, Object> serviceProfile = execution.getVariable("serviceProfile")

        execution.setVariable("nstSelectionUrl", "/api/oof/v1/selection/nst")
        execution.setVariable("nstSelection_messageType",messageType)
        execution.setVariable("nstSelection_correlator",requestId)
        String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);
        execution.setVariable("nstSelection_timeout",timeout)
        String oofRequest = oofUtils.buildSelectNSTRequest(requestId,messageType, serviceProfile)
        execution.setVariable("nstSelection_oofRequest",oofRequest)
        logger.debug("Finish prepareOofSelection")
    }

    public void processOofSelection(DelegateExecution execution) {
        Map<String, Object> nstSolution
        try {
            logger.debug("Start processOofSelection")
            Map<String, Object> resMap = objectMapper.readValue(execution.getVariable("nstSelection_oofResponse"),Map.class)
            List<Map<String, Object>> nstSolutions = (List<Map<String, Object>>) resMap.get("solutions")
            nstSolution = nstSolutions.get(0)
            execution.setVariable("nstSolution", nstSolution)
            logger.debug("Finish processOofSelection")
        } catch (Exception ex) {
            logger.debug( "Failed to get NST solution suggested by OOF.")
            exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Failed to get NST solution suggested by OOF.")
        }

    }

    public void updateAAIOrchStatus(DelegateExecution execution) {
        logger.debug("Start updateAAIOrchStatus")
        String tnNssiId = execution.getVariable("tnNssiId")
        String orchStatus = execution.getVariable("orchestrationStatus")

        try {
            ServiceInstance si = new ServiceInstance()
            si.setOrchestrationStatus(orchStatus)
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, tnNssiId)
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

    public void prepareInitServiceOperationStatus(DelegateExecution execution) {
        logger.debug("Start prepareInitServiceOperationStatus")
        try {
            String tnNssiId = execution.getVariable("tnNssiId")
            String operationId = execution.getVariable("operationId")
            String operationType = "CREATE"
            String userId = execution.getVariable("globalSubscriberId")
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service creation"
            logger.debug("Generated new operation for Allocate TN NSSI tnNssiId:" + tnNssiId + " operationId:" +
                    operationId)
            tnNssiId = UriUtils.encode(tnNssiId, "UTF-8")
            execution.setVariable("tnNssiId", tnNssiId)
            execution.setVariable("operationType", operationType)

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
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
                            <tnNssiId>${MsoUtils.xmlEscape(tnNssiId)}</tnNssiId>
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
        } catch (Exception e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception Occured Processing prepareInitServiceOperationStatus.", "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e)
        }
        logger.debug("Finish prepareInitServiceOperationStatus")
    }

    public void prepareUpdateResourceOperationStatus(DelegateExecution execution) {
        logger.debug("Start preUpdateServiceOperationStatus")
        try {
            String tnNssiId = execution.getVariable("tnNssiId")
            String operationId = execution.getVariable("jobId")
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
                            <serviceId>${MsoUtils.xmlEscape(tnNssiId)}</serviceId>
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

        } catch (Exception e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception Occured Processing preUpdateServiceOperationStatus.", "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e.getMessage())
        }
        logger.debug("Finish preUpdateServiceOperationStatus")
    }


}

