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

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class DoActivateTnNssi extends AbstractServiceTaskProcessor {
    String Prefix = "TNACT_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    TnNssmfUtils tnNssmfUtils = new TnNssmfUtils()
    JsonSlurper jsonSlurper = new JsonSlurper()
    ObjectMapper objectMapper = new ObjectMapper()
    private static final Logger logger = LoggerFactory.getLogger(DoActivateTnNssi.class)


    public void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")

        execution.setVariable("startTime", System.currentTimeMillis())
        String msg = tnNssmfUtils.getExecutionInputParams(execution)
        logger.debug("Activate TN NSSI input parameters: " + msg)

        execution.setVariable("prefix", Prefix)

        tnNssmfUtils.setSdncCallbackUrl(execution, true)
        logger.debug("SDNC Callback URL: " + execution.getVariable("sdncCallbackUrl"))

        String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
        String modelUuid = execution.getVariable("modelUuid")
        //here modelVersion is not set, we use modelUuid to decompose the service.
        def isDebugLogEnabled = true
        execution.setVariable("isDebugLogEnabled", isDebugLogEnabled)
        String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("serviceModelInfo", serviceModelInfo)

        String sliceServiceInstanceId = execution.getVariable("serviceInstanceID")
        execution.setVariable("sliceServiceInstanceId", sliceServiceInstanceId)

        String sliceServiceInstanceName = execution.getVariable("servicename")
        execution.setVariable("sliceServiceInstanceName", sliceServiceInstanceName)

        String operationType = execution.getVariable("operationType")
        String actionType = operationType.equals("activateInstance") ? "activate" : "deactivate"
        execution.setVariable("actionType", actionType)


        logger.debug("Finish preProcessRequest")
    }

    void preprocessSdncActOrDeactTnNssiRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preprocessSdncActivateTnNssiRequest(' +
                'execution=' + execution.getId() + ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)

        try {
            String serviceInstanceId = execution.getVariable("sliceServiceInstanceId")
            String actionType = execution.getVariable("actionType")

            String sdncRequest = tnNssmfUtils.buildSDNCRequest(execution, serviceInstanceId, actionType)

            execution.setVariable("TNNSSMF_SDNCRequest", sdncRequest)
            logger.debug("Outgoing SDNCRequest is: \n" + sdncRequest)

        } catch (Exception e) {
            logger.debug("Exception Occured Processing preprocessSdncDeallocateTnNssiRequest. Exception is:\n" + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED preprocessSdncActivateTnNssiRequest Process")
    }


    void validateSDNCResponse(DelegateExecution execution, String response) {
        tnNssmfUtils.validateSDNCResponse(execution, response, method)
    }


    void updateAAIOrchStatus(DelegateExecution execution) {
        logger.debug("Start updateAAIOrchStatus")
        String tnNssiId = execution.getVariable("tnNssiId")
        String orchStatus = execution.getVariable("orchestrationStatus")

        try {
            ServiceInstance si = new ServiceInstance()
            si.setOrchestrationStatus(orchStatus)
            AAIResourcesClient client = getAAIClient()
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
        String serviceId = execution.getVariable("serviceInstanceID")
        String jobId = execution.getVariable("jobId")
        String nsiId = execution.getVariable("nsiId")
        String operType = execution.getVariable("actionType")


        ResourceOperationStatus roStatus = new ResourceOperationStatus()
        roStatus.setServiceId(serviceId)
        roStatus.setOperationId(jobId)
        roStatus.setResourceTemplateUUID(nsiId)
        roStatus.setOperType(operType)
        roStatus.setProgress(progress)
        roStatus.setStatus(status)
        roStatus.setStatusDescription(statusDescription)
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, status)
    }

}

