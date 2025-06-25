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
import org.onap.so.db.request.beans.OrchestrationTask

import static org.apache.commons.lang3.StringUtils.*
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HandleOrchestrationTask extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HandleOrchestrationTask.class)
    private static final ObjectMapper mapper = new ObjectMapper()

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    def supportedMethod = ["GET", "POST", "PUT"]
    def validStatus = [200, 201]

    @Override
    public void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")
        String method = execution.getVariable("method")
        if (!supportedMethod.contains(method)) {
            String msg = "Method: " + method + " is not supported"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        String taskId = execution.getVariable("taskId")
        if (isBlank(taskId)) {
            String msg = "taskId is empty"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.requestDb.endpoint",execution)
        def orchestrationTaskEndpoint = dbAdapterEndpoint + "/orchestrationTask/"
        if (!"POST".equals(method)) {
            orchestrationTaskEndpoint = orchestrationTaskEndpoint + taskId
        }
        execution.setVariable("url", orchestrationTaskEndpoint)
        logger.debug("DB Adapter Endpoint is: " + orchestrationTaskEndpoint)
        def dbAdapterAuth = UrnPropertiesReader.getVariable("mso.adapters.requestDb.auth")
        Map<String, String> headerMap = [:]
        headerMap.put("content-type", "application/json")
        headerMap.put("Authorization", dbAdapterAuth)
        execution.setVariable("headerMap", headerMap)
        logger.debug("DB Adapter Header is: " + headerMap)

        String requestId = execution.getVariable("requestId")
        if (("POST".equals(method) || "PUT".equals(method)) && isBlank(requestId)) {
            String msg = "requestId is empty"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        String taskName = execution.getVariable("taskName")
        if (("POST".equals(method) || "PUT".equals(method)) && isBlank(taskName)) {
            String msg = "task name is empty"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        String taskStatus = execution.getVariable("taskStatus")
        if (("POST".equals(method) || "PUT".equals(method)) && isBlank(taskStatus)) {
            String msg = "task status is empty"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        String isManual = execution.getVariable("isManual")
        if (("POST".equals(method) || "PUT".equals(method)) && isBlank(isManual)) {
            String msg = "isManual is empty"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        String paramJson = execution.getVariable("paramJson")

        String payload = ""
        if ("POST".equals(method) || "PUT".equals(method)) {
            OrchestrationTask task = new OrchestrationTask()
            task.setTaskId(taskId)
            task.setRequestId(requestId)
            task.setName(taskName)
            task.setStatus(taskStatus)
            task.setIsManual(isManual)
            task.setCreatedTime(new Date())
            task.setParams(paramJson)
            payload = mapper.writeValueAsString(task)
            logger.debug("Outgoing payload is \n" + payload)
        }
        execution.setVariable("payload", payload)
        logger.debug("End preProcessRequest")
    }

    public void postProcess(DelegateExecution execution) {
        Integer statusCode = execution.getVariable("statusCode")
        logger.debug("statusCode: " + statusCode)
        String response = execution.getVariable("response")
        logger.debug("response: " + response)
        if (!validStatus.contains(statusCode)) {
            String msg = "Error in sending orchestrationTask request. \nstatusCode: " + statusCode + "\nresponse: " + response
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }
}
