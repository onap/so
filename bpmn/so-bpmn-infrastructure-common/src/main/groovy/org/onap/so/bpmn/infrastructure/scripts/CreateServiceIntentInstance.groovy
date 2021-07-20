/*
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, Wipro Limited.
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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.apache.commons.lang3.StringUtils.isBlank

class CreateServiceIntentInstance extends AbstractServiceTaskProcessor {

    String Prefix = "CreateSiInstance_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    JsonUtils jsonUtil = new JsonUtils()
    ServiceIntentUtils serviceIntentUtils = new ServiceIntentUtils()
    private static final Logger logger = LoggerFactory.getLogger(CreateServiceIntentInstance.class)

    @Override
    void preProcessRequest(DelegateExecution execution) {
        logger.debug(Prefix + "preProcessRequest Start")
        execution.setVariable("prefix", Prefix)
        execution.setVariable("startTime", System.currentTimeMillis())
        def msg
        try {
            // get request input
            String subnetInstanceReq = execution.getVariable("bpmnRequest")
            logger.debug(subnetInstanceReq)

            serviceIntentUtils.setCommonExecutionVars(execution)

            //modelInfo
            String modelInvariantUuid = jsonUtil.getJsonValue(subnetInstanceReq, "modelInvariantUuid")
            if (isBlank(modelInvariantUuid)) {
                msg = "Input modelInvariantUuid is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("modelInvariantUuid", modelInvariantUuid)
            }

            logger.debug("modelInvariantUuid: " + modelInvariantUuid)

            String modelUuid = jsonUtil.getJsonValue(subnetInstanceReq, "modelUuid")
            if (isBlank(modelUuid)) {
                msg = "Input modelUuid is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("modelUuid", modelUuid)
            }

            logger.debug("modelUuid: " + modelUuid)

            String additionalPropJsonStr = execution.getVariable("serviceIntentParams")
            String siId = jsonUtil.getJsonValue(additionalPropJsonStr, "serviceInstanceID") //for debug
            if (isBlank(siId)) {
                siId = UUID.randomUUID().toString()
            }

            logger.debug("serviceInstanceID: " + modelUuid)
            execution.setVariable("serviceInstanceID", siId)

            String sST = jsonUtil.getJsonValue(subnetInstanceReq, "sst")
            execution.setVariable("sst", sST)

            String jobId = UUID.randomUUID().toString()
            execution.setVariable("jobId", jobId)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in CreateServiceIntentInstance.preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "preProcessRequest Exit")
    }


    /**
     * create operation status in request db
     *
     * Init the Operation Status
     */
    def prepareInitOperationStatus = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareInitOperationStatus Start")

        String modelUuid = execution.getVariable("modelUuid")
        String jobId = execution.getVariable("jobId")
        String nsiId = execution.getVariable("serviceInstanceID")
        logger.debug("Generated new job for Service Instance serviceId:" + modelUuid + " jobId:" + jobId)

        ResourceOperationStatus initStatus = new ResourceOperationStatus()
        initStatus.setServiceId(nsiId)  // set nsiId to this field
        initStatus.setOperationId(jobId)    // set jobId to this field
        initStatus.setResourceTemplateUUID(modelUuid)   // set modelUuid to this field
        initStatus.setOperType("Create")
        //initStatus.setResourceInstanceID() // set nssiId to this field
        requestDBUtil.prepareInitResourceOperationStatus(execution, initStatus)

        logger.debug(Prefix + "prepareInitOperationStatus Exit")
    }


    /**
     * return sync response
     */
    def sendSyncResponse = { DelegateExecution execution ->
        logger.debug(Prefix + "sendSyncResponse Start")
        try {
            String jobId = execution.getVariable("jobId")
            String allocateSyncResponse = """{"jobId": "${jobId}","status": "processing"}"""
                    .trim().replaceAll(" ", "").trim().replaceAll(" ", "")

            logger.debug("sendSyncResponse to APIH:" + "\n" + allocateSyncResponse)
            sendWorkflowResponse(execution, 202, allocateSyncResponse)

            execution.setVariable("sentSyncResponse", true)
        } catch (Exception ex) {
            String msg = "Exception in sendSyncResponse:" + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "sendSyncResponse Exit")
    }

}
