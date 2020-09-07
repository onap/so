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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceProxy
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils

import static org.apache.commons.lang3.StringUtils.isBlank

class TnAllocateNssi extends AbstractServiceTaskProcessor {
    String Prefix = "TNALLOC_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    JsonSlurper jsonSlurper = new JsonSlurper()
    ObjectMapper objectMapper = new ObjectMapper()
    OofUtils oofUtils = new OofUtils()
    private static final Logger logger = LoggerFactory.getLogger(TnAllocateNssi.class)

    void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")
        execution.setVariable("prefix", Prefix)
        String msg = ""

        try {
            execution.setVariable("startTime", System.currentTimeMillis())
            logger.debug("input variables : msoRequestId - " + execution.getVariable("msoRequestId")
                    + " modelInvariantUuid - " + execution.getVariable("modelInvariantUuid") +
                    " modelUuid - " + execution.getVariable("modelUuid") +
                    " globalSubscriberId - " + execution.getVariable("globalSubscriberId") +
                    " dummyServiceId - " + execution.getVariable("dummyServiceId") +
                    " nsiId - " + execution.getVariable("nsiId") +
                    " networkType - " + execution.getVariable("networkType") +
                    " subscriptionServiceType - " + execution.getVariable("subscriptionServiceType") +
                    " jobId - " + execution.getVariable("jobId") +
                    " sliceParams - " + execution.getVariable("sliceParams") +
                    " servicename - " + execution.getVariable("servicename"))

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

            String nsiInfoStr = jsonUtil.getJsonValue(additionalPropJsonStr, "nsiInfo")
            if (isBlank(nsiInfoStr)) {
                msg = "Input nsiInfo is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("nsiInfo", nsiInfoStr)
            }

            //nsiId is passed in from caller bpmn
            //String nsiIdStr = jsonUtil.getJsonValue(nsiInfo, "nsiId")
            //execution.setVariable("nsiId", nsiIdStr)

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
        String msg = ""
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

        ServiceDecomposition tnNsstServiceDecomposition = execution.getVariable("tnNsstServiceDecomposition")
        logger.debug("tnNsstServiceDecomposition : " + tnNsstServiceDecomposition.toString())
        //TN NSST decomposition
        String tnModelVersion = tnNsstServiceDecomposition.getModelInfo().getModelVersion()
        String tnModelName = tnNsstServiceDecomposition.getModelInfo().getModelName()
        List<ServiceProxy> serviceProxyList = tnNsstServiceDecomposition.getServiceProxy()
        List<String> nsstInfoList = new ArrayList<>()
        for (ServiceProxy serviceProxy : serviceProxyList) {
            String nsstModelUuid = serviceProxy.getModelInfo().getModelUuid()
            String nsstModelInvariantUuid = serviceProxy.getModelInfo().getModelInvariantUuid()
            String name = serviceProxy.getModelInfo().getModelName()
            String nsstServiceModelInfo = """{
            "UUID":"${nsstModelUuid}",
            "invariantUUID":"${nsstModelInvariantUuid}",
            "name":"${name}"
             }"""
            nsstInfoList.add(nsstServiceModelInfo)
        }
        int currentIndex = 0
        int maxIndex = nsstInfoList.size()
        if (maxIndex < 1) {
            String msg = "Exception in TN NSST processDecomposition. There is no NSST associated with TN NSST "
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        execution.setVariable("tnNsstInfoList", nsstInfoList)
        execution.setVariable("tnModelVersion", tnModelVersion)
        execution.setVariable("tnModelName", tnModelName)
        execution.setVariable("currentIndex", currentIndex)
        execution.setVariable("maxIndex", maxIndex)

        logger.debug("End processDecomposition")
    }

    void prepareOofSelection(DelegateExecution execution) {
        logger.debug("Start prepareOofSelection")

        String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
        logger.debug("get NSSI option OOF Url: " + urlString)
        //build oof request body
        String requestId = execution.getVariable("msoRequestId")
        String messageType = "NSISelectionResponse"
        Map<String, Object> profileInfo = objectMapper.readValue(execution.getVariable("sliceProfile"), Map.class)
        String modelUuid = execution.getVariable("modelUuid")
        String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
        String modelName = execution.getVariable("tnModelName")
        String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);
        List<String> nsstInfoList = objectMapper.readValue(execution.getVariable("nsstInfoList"), List.class)
        JsonArray capabilitiesList = new JsonArray()

        execution.setVariable("nssiSelection_Url", "/api/oof/selection/nsi/v1")
        execution.setVariable("nssiSelection_messageType", messageType)
        execution.setVariable("nssiSelection_correlator", requestId)
        execution.setVariable("nssiSelection_timeout", timeout)
        String oofRequest = buildSelectTnNssiRequest(requestId, messageType, modelUuid, modelInvariantUuid,
                modelName, profileInfo, nsstInfoList, capabilitiesList, false)
        execution.setVariable("nssiSelection_oofRequest", oofRequest)

        logger.debug("Finish prepareOofSelection")
    }

    String buildSelectTnNssiRequest(String requestId, String messageType, String UUID, String invariantUUID,
                                    String name, Map<String, Object> profileInfo,
                                    List<String> nsstInfoList, JsonArray capabilitiesList, Boolean preferReuse) {

        def transactionId = requestId
        logger.debug("transactionId is: " + transactionId)
        String correlator = requestId
        String callbackUrl = UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint") + "/" + messageType + "/" + correlator
        ObjectMapper objectMapper = new ObjectMapper()
        String profileJson = objectMapper.writeValueAsString(profileInfo)
        String nsstInfoListString = objectMapper.writeValueAsString(nsstInfoList)
        //Prepare requestInfo object
        JsonObject requestInfo = new JsonObject()
        requestInfo.addProperty("transactionId", transactionId)
        requestInfo.addProperty("requestId", requestId)
        requestInfo.addProperty("callbackUrl", callbackUrl)
        requestInfo.addProperty("sourceId", "SO")
        requestInfo.addProperty("timeout", 600)
        requestInfo.addProperty("numSolutions", 1)

        //Prepare serviceInfo object
        JsonObject ranNsstInfo = new JsonObject()
        ranNsstInfo.addProperty("UUID", UUID)
        ranNsstInfo.addProperty("invariantUUID", invariantUUID)
        ranNsstInfo.addProperty("name", name)

        JsonObject json = new JsonObject()
        json.add("requestInfo", requestInfo)
        json.add("NSTInfo", ranNsstInfo)
        json.addProperty("serviceProfile", profileJson)
        json.addProperty("NSSTInfo", nsstInfoListString)
        json.add("subnetCapabilities", capabilitiesList)
        json.addProperty("preferReuse", preferReuse)

        return json.toString()
    }

    void processOofSelection(DelegateExecution execution) {
        logger.debug(Prefix+"processOofSelection method start")
        String oofResponse = execution.getVariable("nssiSelection_asyncCallbackResponse")
        String requestStatus = jsonUtil.getJsonValue(oofResponse, "requestStatus")
        if(requestStatus.equals("completed")) {
            List<String> solution = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(oofResponse, "solutions"))
            boolean existingNSI = jsonUtil.getJsonValue(solution.get(0), "existingNSI")
            if(existingNSI) {
                def sharedNSISolution = jsonUtil.getJsonValue(solution.get(0), "sharedNSISolution")
                execution.setVariable("sharedTnNssiSolution", sharedNSISolution)
                logger.debug("sharedTnNssiSolution from OOF "+sharedNSISolution)
                String tnServiceInstanceId = jsonUtil.getJsonValue(solution.get(0), "sharedNSISolution.NSIId")
                execution.setVariable("tnServiceInstanceId", tnServiceInstanceId)
                org.onap.so.bpmn.core.domain.ServiceInstance serviceInstance = new org.onap.so.bpmn.core.domain.ServiceInstance();
                serviceInstance.setInstanceId(tnServiceInstanceId);
                ServiceDecomposition serviceDecomposition = execution.getVariable("tnNsstServiceDecomposition")
                serviceDecomposition.setServiceInstance(serviceInstance);
                execution.setVariable("tnNsstServiceDecomposition", serviceDecomposition)
                execution.setVariable("isOofTnNssiSelected", true)
            }else {
                def sliceProfiles = jsonUtil.getJsonValue(solution.get(0), "newNSISolution.sliceProfiles")
                execution.setVariable("tnConstituentSliceProfiles", sliceProfiles)
                execution.setVariable("isOofTnNssiSelected", false)
                logger.debug("tnConstituentSliceProfiles list from OOF "+sliceProfiles)
            }
        }else {
            String statusMessage = jsonUtil.getJsonValue(oofResponse, "statusMessage")
            logger.error("received failed status from oof "+ statusMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a failed Async Response from OOF : "+statusMessage)
        }

        logger.debug(Prefix+"processOofSelection method finished")
    }

    void updateAAIOrchStatus(DelegateExecution execution) {
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


    void prepareUpdateJobStatus(DelegateExecution execution,
                                String status,
                                String progress,
                                String statusDescription) {
        String serviceId = execution.getVariable("tnNssiId")
        String jobId = execution.getVariable("jobId")
        String nsiId = execution.getVariable("nsiId")

        ResourceOperationStatus roStatus = new ResourceOperationStatus()
        roStatus.setServiceId(serviceId)
        roStatus.setOperationId(jobId)
        roStatus.setResourceTemplateUUID(nsiId)
        roStatus.setOperType("Allocate")
        roStatus.setProgress(progress)
        roStatus.setStatus(status)
        roStatus.setStatusDescription(statusDescription)
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, status)
    }

}

