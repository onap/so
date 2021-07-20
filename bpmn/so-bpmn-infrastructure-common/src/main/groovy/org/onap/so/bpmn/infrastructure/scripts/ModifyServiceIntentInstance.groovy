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

class ModifyServiceIntentInstance extends AbstractServiceTaskProcessor {
    String Prefix="MCLL_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    ServiceIntentUtils serviceIntentUtils = new ServiceIntentUtils()

    private static final Logger logger = LoggerFactory.getLogger(ModifyServiceIntentInstance.class)

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

            String serviceInstanceID = jsonUtil.getJsonValue(subnetInstanceReq, "serviceInstanceID")
            if (isBlank(serviceInstanceID)) {
                msg = "Input serviceInstanceID is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else
            {
                execution.setVariable("serviceInstanceID", serviceInstanceID)
            }

            String jobId = UUID.randomUUID().toString()
            execution.setVariable("jobId", jobId)

        } catch(BpmnError e) {
            throw e
        } catch(Exception ex) {
            msg = "Exception in ModifyServiceIntentInstance.preProcessRequest " + ex.getMessage()
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

        String siId = execution.getVariable("serviceInstanceID")
        String jobId = execution.getVariable("jobId")
        String nsiId = siId
        String modelUuid = serviceIntentUtils.getModelUuidFromServiceInstance(siId)
        logger.debug("Generated new job for Service Instance serviceId:" + nsiId + "jobId:" + jobId)

        ResourceOperationStatus initStatus = new ResourceOperationStatus()
        initStatus.setServiceId(nsiId)
        initStatus.setOperationId(jobId)
        initStatus.setResourceTemplateUUID(modelUuid)
        initStatus.setOperType("Modify")
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
            String modifySyncResponse = """{"jobId": "${jobId}","status": "processing"}"""
                                                .trim().replaceAll(" ", "")
            logger.debug("sendSyncResponse to APIH:" + "\n" + modifySyncResponse)
            sendWorkflowResponse(execution, 202, modifySyncResponse)

            execution.setVariable("sentSyncResponse", true)
        } catch (Exception ex) {
            String msg = "Exception in sendSyncResponse:" + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "sendSyncResponse Exit")
    }

}
