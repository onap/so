/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 Huawei Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import org.apache.commons.lang3.StringUtils
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class HandlePNF extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger( HandlePNF.class);

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    CatalogDbUtils cutils = new CatalogDbUtils()

    @Override
    void preProcessRequest(DelegateExecution execution) {
        msoLogger.debug("Start preProcess for HandlePNF")

        // set correlation ID
        def resourceInput = execution.getVariable("resourceInput")
        String serInput = jsonUtil.getJsonValue(resourceInput, "requestsInputs")
        String correlationId = jsonUtil.getJsonValue(serInput, "service.parameters.requestInputs.ont_ont_pnf_name")
        if (!StringUtils.isEmpty(correlationId)) {
            execution.setVariable(ExecutionVariableNames.CORRELATION_ID, correlationId)
            msoLogger.debug("Found correlation id : " + correlationId)
        } else {
            msoLogger.error("== correlation id is empty ==")
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "correlation id is not provided")
        }

        // next task will set the uuid
        msoLogger.debug("exit preProcess for HandlePNF")
    }

    void postProcessRequest(DelegateExecution execution) {
        msoLogger.debug("start postProcess for HandlePNF")

        msoLogger.debug("exit postProcess for HandlePNF")
    }

    public void sendSyncResponse (DelegateExecution execution) {
        msoLogger.debug(" *** sendSyncResponse *** ")

        try {
            String operationStatus = "finished"
            // RESTResponse for main flow
            String resourceOperationResp = """{"operationStatus":"${operationStatus}"}""".trim()
            msoLogger.debug(" sendSyncResponse to APIH:" + "\n" + resourceOperationResp)
            sendWorkflowResponse(execution, 202, resourceOperationResp)
            execution.setVariable("sentSyncResponse", true)

        } catch (Exception ex) {
            String msg = "Exception in sendSyncResponse:" + ex.getMessage()
            msoLogger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        msoLogger.debug(" ***** Exit sendSyncResponse *****")
    }
}
