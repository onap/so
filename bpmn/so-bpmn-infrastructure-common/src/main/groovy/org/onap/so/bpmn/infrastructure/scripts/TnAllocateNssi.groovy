/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONArray
import com.google.gson.JsonArray
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import groovy.json.JsonSlurper
import org.onap.so.bpmn.common.scripts.OofUtils
import com.google.gson.Gson
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
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
    private static final Logger logger = LoggerFactory.getLogger(TnAllocateNssi.class)
    private static final ObjectMapper mapper = new ObjectMapper()
    String Prefix = "TNALLOC_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    JsonSlurper jsonSlurper = new JsonSlurper()
    OofUtils oofUtils = new OofUtils()
    TnNssmfUtils tnNssmfUtils = new TnNssmfUtils()

    void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")
        execution.setVariable("prefix", Prefix)
        String msg = ""

        try {
            execution.setVariable("startTime", System.currentTimeMillis())

            msg = tnNssmfUtils.getExecutionInputParams(execution)
            logger.debug("Allocate TN NSSI input parameters: " + msg)

            tnNssmfUtils.setSdncCallbackUrl(execution, true)
            logger.debug("SDNC Callback URL: " + execution.getVariable("sdncCallbackUrl"))

            String additionalPropJsonStr = execution.getVariable("sliceParams")

            String tnNssiId = jsonUtil.getJsonValue(additionalPropJsonStr, "serviceInstanceID") //for debug
            if (isBlank(tnNssiId)) {
                tnNssiId = UUID.randomUUID().toString()
            }

            String operationId = UUID.randomUUID().toString()
            execution.setVariable("operationId", operationId)

            logger.debug("Generate new TN NSSI ID:" + tnNssiId)
            tnNssiId = UriUtils.encode(tnNssiId, "UTF-8")
            execution.setVariable("sliceServiceInstanceId", tnNssiId)

            String sliceServiceInstanceName = execution.getVariable("servicename")
            execution.setVariable("sliceServiceInstanceName", sliceServiceInstanceName)

            String sst = execution.getVariable("sst")
            execution.setVariable("sst", sst)

            //additional properties
            String sliceProfile = jsonUtil.getJsonValue(additionalPropJsonStr, "sliceProfile")
            if (isBlank(sliceProfile)) {
                msg = "Input sliceProfile is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            else {
                execution.setVariable("sliceProfile", sliceProfile)
            }

            String transportSliceNetworks = jsonUtil.getJsonValue(additionalPropJsonStr, "transportSliceNetworks")
            if (isBlank(transportSliceNetworks)) {
                msg = "Input transportSliceNetworks is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            else {
                execution.setVariable("transportSliceNetworks", transportSliceNetworks)
            }
            logger.debug("transportSliceNetworks: " + transportSliceNetworks)

            String nsiInfoStr = jsonUtil.getJsonValue(additionalPropJsonStr, "nsiInfo")
            if (isBlank(nsiInfoStr)) {
                msg = "Input nsiInfo is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            else {
                execution.setVariable("nsiInfo", nsiInfoStr)
            }

            if (isBlank(tnNssmfUtils.setExecVarFromJsonIfExists(execution, additionalPropJsonStr,
                    "enableSdnc", "enableSdnc"))) {
                tnNssmfUtils.setEnableSdncConfig(execution)
            }

            if (isBlank(additionalPropJsonStr) ||
                    isBlank(tnNssmfUtils.setExecVarFromJsonIfExists(execution,
                            additionalPropJsonStr,
                            "enableOof", "enableOof"))) {
                tnNssmfUtils.setEnableOofConfig(execution)
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
        execution.setVariable("serviceModelInfo", serviceModelInfo)

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
            //exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        else {
            execution.setVariable("tnNsstInfoList", nsstInfoList)
            execution.setVariable("tnModelVersion", tnModelVersion)
            execution.setVariable("tnModelName", tnModelName)
            execution.setVariable("currentIndex", currentIndex)
            execution.setVariable("maxIndex", maxIndex)
        }

        execution.setVariable("tnNfSliceProfile", execution.getVariable("sliceProfile"))
        execution.setVariable("tnModelName", tnModelName)
        logger.debug("End processDecomposition")
    }

    void prepareOofSelection(DelegateExecution execution) {
        logger.debug("Start prepareOofSelection")

        String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
        logger.debug("get NSSI option OOF Url: " + urlString)
        //build oof request body

        String requestId = execution.getVariable("msoRequestId")
        String messageType = "NSSISelectionResponse"
        Map<String, Object> profileInfo = (Map<String, Object>) mapper.readValue(execution.getVariable("tnNfSliceProfile"), Map.class)
        String modelUuid = execution.getVariable("modelUuid")
        String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
        String modelName = execution.getVariable("tnModelName")
        String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);

        execution.setVariable("nssiSelection_Url", "/api/oof/selection/nssi/v1")
        execution.setVariable("nssiSelection_messageType", messageType)
        execution.setVariable("nssiSelection_correlator", requestId)
        execution.setVariable("nssiSelection_timeout", timeout)
        String oofRequest = oofUtils.buildSelectNSSIRequest(requestId, messageType, modelUuid, modelInvariantUuid, modelName, profileInfo)
        execution.setVariable("nssiSelection_oofRequest", oofRequest)

        logger.debug("Finish prepareOofSelection")
    }

    void processOofSelection(DelegateExecution execution) {
        logger.debug(Prefix + "processOofSelection method start")
        String oofResponse = execution.getVariable("nssiSelection_asyncCallbackResponse")
        String requestStatus = jsonUtil.getJsonValue(oofResponse, "requestStatus")
        if (requestStatus.equals("completed")) {
            String solutions = jsonUtil.getJsonValue(oofResponse, "solutions")
            JsonParser parser = new JsonParser()
            JsonArray solution = parser.parse(solutions) as JsonArray
            if (solution.size() >= 1) {
                JsonObject sol = solution.get(0) as JsonObject
                String tnNfNssiId = sol.get("NSSIId").getAsString()
                String invariantUuid = sol.get("invariantUUID").getAsString()
                String uuid = sol.get("UUID").getAsString()
                String nssiName = sol.get("NSSIName").getAsString()
                execution.setVariable("TnServiceInstanceId", tnNfNssiId)
                execution.setVariable("TNNFInvariantUUID", invariantUuid)
                execution.setVariable("TNNFUUID", uuid)
                execution.setVariable("servicename", nssiName)
                logger.debug("TnServiceInstanceId from OOF " + tnNfNssiId)
                execution.setVariable("isOofTnNssiSelected", true)
            }
            else {
                logger.debug("No solutions returned from OOF .. Create new TN NF NSSI")
                execution.setVariable("isOofTnNssiSelected", false)
            }
        }
        else {
            String statusMessage = jsonUtil.getJsonValue(oofResponse, "statusMessage")
            logger.error("received failed status from oof " + statusMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Received a failed Async Response from OOF : " + statusMessage)
        }

        logger.debug(Prefix + "processOofSelection method finished")
    }

    void prepareModifyTnNssiInputs(DelegateExecution execution) {
        logger.debug(Prefix + "prepareModifyTnNssiInputs method start")
        String additionalPropJsonStr = execution.getVariable("sliceParams")
        String sliceProfile = execution.getVariable("sliceProfile")
        String snssaiList = jsonUtil.getJsonValue(sliceProfile, "snssaiList")
        String sliceProfileId = jsonUtil.getJsonValue(sliceProfile, "sliceProfileId")
        String nsiInfo = jsonUtil.getJsonValue(additionalPropJsonStr, "nsiInfo")
        String scriptName = jsonUtil.getJsonValue(additionalPropJsonStr, "scriptName")
        String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
        String modelUuid = execution.getVariable("modelUuid")
        String serviceInstanceID = execution.getVariable("TnServiceInstanceId")
        String servicename = execution.getVariable("servicename")
        String transportSliceNetworks = jsonUtil.getJsonValue(additionalPropJsonStr, "transportSliceNetworks")

        JsonObject modifySliceParams = new JsonObject()
        modifySliceParams.addProperty("nsiInfo", nsiInfo)
        modifySliceParams.addProperty("serviceInstanceID", serviceInstanceID)
        modifySliceParams.addProperty("servicename", servicename)
        modifySliceParams.addProperty("sliceProfile", sliceProfile)
        modifySliceParams.addProperty("sliceProfileId", sliceProfileId)
        modifySliceParams.addProperty("modelInvariantUuid", modelInvariantUuid)
        modifySliceParams.addProperty("modelUuid", modelUuid)
        modifySliceParams.addProperty("scriptName", scriptName)
        modifySliceParams.addProperty("snssaiList", snssaiList)
        modifySliceParams.addProperty("transportSliceNetworks", transportSliceNetworks)

        execution.setVariable("modifySliceParams", modifySliceParams.toString())

        logger.debug(Prefix + "prepareModifyTnNssiInputs method finished")
    }

    def createModifyNssiQueryJobStatus = { DelegateExecution execution ->
        logger.debug(Prefix + "createModifyNssiQueryJobStatus method start")
        JsonObject esrInfo = new JsonObject()
        esrInfo.addProperty("networkType", "tn")
        esrInfo.addProperty("vendor", "ONAP_internal")

        execution.setVariable("esrInfo", esrInfo.toString())

        JsonObject serviceInfo = new JsonObject()
        serviceInfo.addProperty("nssiId", execution.getVariable("TnServiceInstanceId") as String)
        serviceInfo.addProperty("nsiId", execution.getVariable("nsiId") as String)
        serviceInfo.addProperty("nssiName", execution.getVariable("servicename") as String)
        serviceInfo.addProperty("sST", execution.getVariable("sst") as String)
        serviceInfo.addProperty("PLMNIdList", mapper.writeValueAsString(execution.getVariable("pLMNIdList")))
        serviceInfo.addProperty("globalSubscriberId", execution.getVariable("globalSubscriberId") as String)
        serviceInfo.addProperty("subscriptionServiceType", execution.getVariable("subscriptionServiceType") as String)
        serviceInfo.addProperty("serviceInvariantUuid", execution.getVariable("modelInvariantUuid") as String)
        serviceInfo.addProperty("serviceUuid", execution.getVariable("modelUuid") as String)
        execution.setVariable("serviceInfo", serviceInfo.toString())
        execution.setVariable("responseId", "")
    }

    def processModifyJobStatusRsp = { DelegateExecution execution ->
        logger.debug(Prefix + "processJobStatusRsp method start")
        String jobResponse = execution.getVariable("jobResponse")
        logger.debug("Job status response " + jobResponse)
        String status = jsonUtil.getJsonValue(jobResponse, "status")
        String nssi = jsonUtil.getJsonValue(jobResponse, "nssiId")
        if (status.equalsIgnoreCase("finished")) {
            logger.debug("Job successfully completed ... proceeding with flow for nssi : " + nssi)
        }
        else {
            String statusDescription = jsonUtil.getJsonValue(jobResponse, "statusDescription")
            logger.error("received failed status from job status query for nssi : " + nssi + " with status description : " + statusDescription)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "received failed status from job status query for nssi : " + nssi + " with status description : " + statusDescription)
        }
    }

    void updateAAIOrchStatus(DelegateExecution execution) {
        logger.debug("Start updateAAIOrchStatus")
        String sliceServiceInstanceId = execution.getVariable("sliceServiceInstanceId")
        String orchStatus = execution.getVariable("orchestrationStatus")

        try {
            ServiceInstance si = new ServiceInstance()
            si.setOrchestrationStatus(orchStatus)
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(sliceServiceInstanceId))
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
        String modelUuid = execution.getVariable("modelUuid")
        String ssInstanceId = execution.getVariable("sliceServiceInstanceId")
        String jobId = execution.getVariable("jobId")
        String nsiId = execution.getVariable("nsiId")

        ResourceOperationStatus roStatus = new ResourceOperationStatus()
        roStatus.setServiceId(nsiId)
        roStatus.setOperationId(jobId)
        roStatus.setResourceTemplateUUID(modelUuid)
        roStatus.setResourceInstanceID(ssInstanceId)
        roStatus.setOperType("ALLOCATE")
        roStatus.setProgress(progress)
        roStatus.setStatus(status)
        roStatus.setStatusDescription(statusDescription)
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, roStatus)
    }

}
