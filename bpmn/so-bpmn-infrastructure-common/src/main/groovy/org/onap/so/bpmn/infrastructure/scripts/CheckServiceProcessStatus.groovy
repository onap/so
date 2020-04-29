/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2019, CMCC Technologies Co., Ltd.
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
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.so.db.request.beans.OperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.TimeUnit

import static org.apache.commons.lang3.StringUtils.isBlank

class CheckServiceProcessStatus extends AbstractServiceTaskProcessor  {


    String Prefix="CSPS_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    RequestDBUtil requestDBUtil = new RequestDBUtil()

    JsonUtils jsonUtil = new JsonUtils()

    AAIResourcesClient client = getAAIClient()

    private static final Logger logger = LoggerFactory.getLogger(CheckServiceProcessStatus.class)

    @Override
    void preProcessRequest(DelegateExecution execution) {
        logger.debug(Prefix + "CheckServiceProcessStatus preProcessRequest Start")
        execution.setVariable("prefix", Prefix)

        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String operationId = execution.getVariable("operationId")
        String parentServiceInstanceId = execution.getVariable("parentServiceInstanceId")
        String parentOperationId = execution.getVariable("parentOperationId")

        if (isBlank(serviceInstanceId) || isBlank(operationId)) {
            String msg = "Exception in" + Prefix + "preProcessRequest: Input serviceInstanceId or operationId is null"
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }

        if (isBlank(parentServiceInstanceId) || isBlank(parentOperationId)) {
            execution.setVariable("isNeedUpdateParentStatus", false)
        }

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        if (isBlank(globalSubscriberId)) {
            execution.setVariable("globalSubscriberId", "5GCustomer")
        }

        // serviceType: type of service
        String serviceType = execution.getVariable("processServiceType")
        if (isBlank(serviceType)) {
            execution.setVariable("processServiceType", "service")
        }

        // operationType: type of service
        String operationType = execution.getVariable("operationType")
        if (isBlank(operationType)) {
            execution.setVariable("operationType", "CREATE")
        }

        //successConditions: processing end success conditions
        List<String> successConditions = execution.getVariable("successConditions") as List

        //errorConditions: processing end error conditions
        List<String> errorConditions = execution.getVariable("errorConditions") as List

        if ((successConditions == null || successConditions.size() < 1)
                && (errorConditions == null || errorConditions.size() < 1)) {
            String msg = "Exception in" + Prefix + "preProcessRequest: conditions is null"
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        } else {
            for (int i = 0; i < successConditions.size(); i++) {
                String condition = successConditions.get(i)
                successConditions.set(i, condition.toLowerCase())
            }
            for (int i = 0; i < errorConditions.size(); i++) {
                String condition = errorConditions.get(i)
                errorConditions.set(i, condition.toLowerCase())
            }
        }

        execution.setVariable("startTime", System.currentTimeMillis())

        String initProgress = execution.getVariable("initProgress")

        if (isBlank(initProgress)) {
            execution.setVariable("initProgress", 0)
        }

        String endProgress = execution.getVariable("endProgress")

        if (isBlank(endProgress)) {
            execution.setVariable("endProgress", 100)
        }

        execution.setVariable("progress", 0)
        logger.debug(Prefix + "preProcessRequest Exit")
    }


    /**
     * check service status through request operation id, update operation status
    */
    def preCheckServiceStatusReq = { DelegateExecution execution ->
        logger.trace(Prefix + "preCheckServiceStatusReq Start")
        String serviceInstanceId = execution.getVariable("serviceInstanceId") as String
        String operationId = execution.getVariable("operationId") as String
        requestDBUtil.getOperationStatus(execution, serviceInstanceId, operationId)
        logger.trace(Prefix + "preCheckServiceStatusReq Exit")
    }


    /**
     * handle service status, if service status is finished or error, set the service status
     * @param execution
     */
    def handlerServiceStatusResp = { DelegateExecution execution ->
        logger.trace(Prefix + "handlerServiceStatusResp Start")
        String msg
        try {
            def dbResponseCode = execution.getVariable("dbResponseCode") as Integer
            if (dbResponseCode >= 200 && dbResponseCode < 400) {
                String dbResponse = execution.getVariable("dbResponse")
                def dbResponseJson = jsonUtil.xml2json(dbResponse) as String

                String result = jsonUtil.getJsonValue(dbResponseJson,
                        "Envelope.Body.getServiceOperationStatusResponse.return.result")

                if (isSuccessCompleted(execution, result)) {

                    handlerSuccess(execution, result)
                    execution.setVariable("isAllFinished", "true")

                    logger.debug(Prefix + "handlerServiceStatusResp: service success finished, dbResponse_result: "
                            + result)

                } else if (isErrorCompleted(execution, result)) {

                    handlerError(execution, result)
                    execution.setVariable("isAllFinished", "true")

                    logger.debug(Prefix + "handlerServiceStatusResp: service error finished, dbResponse_result: "
                            + result)

                } else {
                    String progress = jsonUtil.getJsonValue(dbResponseJson,
                            "Envelope.Body.getServiceOperationStatusResponse.return.progress")

                    String oldProgress = execution.getVariable("progress")

                    if (progress == oldProgress) {
                        execution.setVariable("isNeedUpdateDB", false)
                    } else {
                        execution.setVariable("progress", progress)
                        execution.setVariable("isNeedUpdateDB", true)
                    }
                    execution.setVariable("isAllFinished", "false")
                    TimeUnit.SECONDS.sleep(10)
                }
            } else {
                execution.setVariable("isAllFinished", "false")
                //todo: retry
                TimeUnit.MILLISECONDS.sleep(10)
            }

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in " + Prefix + "handlerServiceStatusResp: " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.trace(Prefix + "handlerServiceStatusResp Exit")
    }


    def timeWaitDelay = { DelegateExecution execution ->

        Long startTime = execution.getVariable("startTime") as Long
        Long timeOut = execution.getVariable("timeOut") as Long

        timeOut = timeOut == null ? 3 * 60 * 60 * 1000 : timeOut

        if (System.currentTimeMillis() - startTime > timeOut) {

            handlerTimeOut(execution)
            execution.setVariable("isTimeOut", "YES")

        } else {
            execution.setVariable("isTimeOut", "NO")
        }
    }


    private handlerTimeOut = { DelegateExecution execution ->

        Map<String, Object> paramMap = execution.getVariable("timeOutParamMap") as Map

        handlerProcess(execution, "error", paramMap, "error", "with timeout")
    }


    private handlerSuccess = { DelegateExecution execution, String result ->

        Map<String, Object> paramMap = execution.getVariable("successParamMap") as Map

        handlerProcess(execution, result, paramMap, "deactivated", "success")
    }


    private handlerError = { DelegateExecution execution, String result ->

        Map<String, Object> paramMap = execution.getVariable("errorParamMap") as Map

        handlerProcess(execution, result, paramMap, "error", "with error")
    }


    private handlerProcess = { DelegateExecution execution, String result, def paramMap, def status, def msg ->

        if (paramMap != null) {
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                execution.setVariable(entry.getKey(), entry.getValue())
            }
        }


        if (isBlank(execution.getVariable("operationStatus") as String)) {
            execution.setVariable("operationStatus", result)
        }


        if (isBlank(execution.getVariable("operationContent") as String)) {
            String operationContent =  execution.getVariable("processServiceType") + " " +
                    execution.getVariable("operationType") + " operation finished " + msg
            execution.setVariable("operationContent", operationContent)
        }

        if (isBlank(execution.getVariable("orchestrationStatus") as String)) {
            execution.setVariable("orchestrationStatus", status)
        }

    }


    /**
     * judge if the service processing success finished
     */
    private isSuccessCompleted = { DelegateExecution execution, String result ->

        //successConditions: processing end success conditions
        List<String> successConditions = execution.getVariable("successConditions") as List

        result = result.toLowerCase()
        if (successConditions.contains(result)) {
            return true
        }
        return false
    }


    /**
     * judge if the service processing error finished
     */
    private isErrorCompleted = { DelegateExecution execution, String result ->

        //errorConditions: processing end error conditions
        List<String> errorConditions = execution.getVariable("errorConditions") as List

        result = result.toLowerCase()
        if (errorConditions.contains(result)) {
            return true
        }
        return false
    }


    def preUpdateOperationProgress = { DelegateExecution execution ->
        logger.trace(Prefix + "prepareUpdateOperationStatus Start")

        def progress = execution.getVariable("progress") as Integer
        def initProgress = execution.getVariable("initProgress") as Integer
        def endProgress = execution.getVariable("endProgress") as Integer

        def resProgress = (initProgress + (endProgress - initProgress) / 100 * progress) as Integer

        def operationType = execution.getVariable("operationType")
        def operationContent =  execution.getVariable("processServiceType") + " " +
                operationType + " operation processing " + resProgress

        // update status creating
        OperationStatus status = new OperationStatus()
        status.setServiceId(execution.getVariable("parentServiceInstanceId") as String)
        status.setOperationId(execution.getVariable("parentOperationId") as String)
        status.setOperation(operationType as String)
        status.setResult("processing")
        status.setProgress(resProgress as String)
        status.setOperationContent(operationContent as String)
        status.setUserId(execution.getVariable("globalSubscriberId") as String)

        requestDBUtil.prepareUpdateOperationStatus(execution, status)
        logger.trace(Prefix + "prepareUpdateOperationStatus Exit")
    }
}
