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


import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.apache.commons.lang3.StringUtils.isBlank
import static org.apache.commons.lang3.StringUtils.isEmpty

class DoCloudLeasedLineDelete extends AbstractServiceTaskProcessor {
    String Prefix = "DCLL_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    ServiceIntentUtils serviceIntentUtils = new ServiceIntentUtils()
    private static final Logger logger = LoggerFactory.getLogger(DoCloudLeasedLineDelete.class)


    void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")

        execution.setVariable("startTime", System.currentTimeMillis())
        String msg = serviceIntentUtils.getExecutionInputParams(execution)
        logger.debug("Deallocate CLL input parameters: " + msg)

        execution.setVariable("prefix", Prefix)

        serviceIntentUtils.setSdncCallbackUrl(execution, true)
        logger.debug("SDNC Callback URL: " + execution.getVariable("sdncCallbackUrl"))

        String cllId = execution.getVariable("serviceInstanceID")
        execution.setVariable("cllId", cllId)

        String cllName = execution.getVariable("servicename")
        execution.setVariable("cllName", cllName)

        String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
        String modelUuid = execution.getVariable("modelUuid")
        if (isEmpty(modelUuid)) {
            modelUuid = serviceIntentUtils.getModelUuidFromServiceInstance(execution.getVariable("serviceInstanceID"))
        }
        def isDebugLogEnabled = true
        execution.setVariable("isDebugLogEnabled", isDebugLogEnabled)
        String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("serviceModelInfo", serviceModelInfo)

        String additionalPropJsonStr = execution.getVariable("serviceIntentParams")
        if (isBlank(additionalPropJsonStr) ||
                isBlank(serviceIntentUtils.setExecVarFromJsonIfExists(execution,
                        additionalPropJsonStr,
                        "enableSdnc", "enableSdnc"))) {
            serviceIntentUtils.setEnableSdncConfig(execution)
        }

        logger.debug("Finish preProcessRequest")
    }

    void preprocessSdncDeleteCllRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preprocessSdncDeallocateCllRequest(' +
                'execution=' + execution.getId() + ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)

        try {
            String serviceInstanceId = execution.getVariable("serviceInstanceID")

            String sdncRequest = serviceIntentUtils.buildSDNCRequest(execution, serviceInstanceId, "delete")

            execution.setVariable("CLL_SDNCRequest", sdncRequest)
            logger.debug("Outgoing SDNCRequest is: \n" + sdncRequest)

        } catch (Exception e) {
            logger.debug("Exception Occurred Processing preprocessSdncDeallocateCllRequest. Exception is:\n" + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED preprocessSdncDeallocateCllRequest Process")
    }


    void validateSDNCResponse(DelegateExecution execution, String response, String method) {
        serviceIntentUtils.validateSDNCResponse(execution, response, method)
    }

    void deleteServiceInstance(DelegateExecution execution) {
        try {
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(execution.getVariable("serviceInstanceID")))
            client.delete(uri)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoDeallocateCll.deleteServiceInstance. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    public void updateAAIOrchStatus(DelegateExecution execution) {
        logger.debug("Start updateAAIOrchStatus")
        String cllId = execution.getVariable("cllId")
        String orchStatus = execution.getVariable("orchestrationStatus")

        try {
            ServiceInstance si = new ServiceInstance()
            si.setOrchestrationStatus(orchStatus)
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(cllId))
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
        String cllId = execution.getVariable("cllId")
        String modelUuid = execution.getVariable("modelUuid")
        String jobId = execution.getVariable("jobId")
        String nsiId = execution.getVariable("nsiId")

        ResourceOperationStatus roStatus = serviceIntentUtils.buildRoStatus(modelUuid, cllId,
                jobId, nsiId, "DELETE", status, progress, statusDescription)

        logger.debug("prepareUpdateJobStatus: roStatus={}", roStatus)
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, roStatus)
    }
}

