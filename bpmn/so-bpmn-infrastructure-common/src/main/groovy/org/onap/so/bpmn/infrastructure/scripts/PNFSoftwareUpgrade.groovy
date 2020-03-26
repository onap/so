/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.workflow.context.WorkflowContext
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder
import org.onap.so.bpmn.core.WorkflowException
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*

class PNFSoftwareUpgrade extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PNFSoftwareUpgrade.class)

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    String prefix = "PnfSwUpgrade_"

    @Override
    void preProcessRequest(DelegateExecution execution) {
    }

    void sendResponse(DelegateExecution execution) {
        def requestId = execution.getVariable(REQUEST_ID)
        def instanceId = execution.getVariable(PNF_CORRELATION_ID)
        logger.debug("Send response for requestId: {}, instanceId: {}", requestId, instanceId)

        String response = """{"requestReferences":{"requestId":"${requestId}", "instanceId":"${instanceId}"}}""".trim()
        sendWorkflowResponse(execution, 200, response)
    }

    static WorkflowContext getWorkflowContext(DelegateExecution execution) {
        String requestId = execution.getVariable(REQUEST_ID)
        return WorkflowContextHolder.getInstance().getWorkflowContext(requestId)
    }

    void prepareCompletion(DelegateExecution execution) {
        try {
            String requestId = execution.getVariable(REQUEST_ID)
            logger.debug("Prepare Completion of PNF Software Upgrade for requestId: {}", requestId)

            String msoCompletionRequest =
                    """<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                xmlns:ns="http://org.onap/so/request/types/v1">
                        <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
                            <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
                            <action>UPDATE</action>
                            <source>VID</source>
                        </request-info>
                        <aetgt:status-message>PNF has been upgraded successfully.</aetgt:status-message>
                        <aetgt:mso-bpel-name>PNF_SOFTWARE_UPGRADE</aetgt:mso-bpel-name>
                    </aetgt:MsoCompletionRequest>"""
            String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

            execution.setVariable(prefix + "CompleteMsoProcessRequest", xmlMsoCompletionRequest)

            logger.debug("CompleteMsoProcessRequest of PNF Software Upgrade - " + "\n" + xmlMsoCompletionRequest)
        } catch (Exception e) {
            String msg = "Prepare Completion error for PNF software upgrade - " + e.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
        }
    }

    void prepareFalloutHandler(DelegateExecution execution) {
        WorkflowContext workflowContext = getWorkflowContext(execution)
        if (workflowContext == null) {
            logger.debug("Error occurred before sending response to API handler, and send it now")
            sendResponse(execution)
        }

        try {
            String requestId = execution.getVariable(REQUEST_ID)
            logger.debug("Prepare FalloutHandler of PNF Software Upgrade for requestId: {}", requestId)

            WorkflowException workflowException = execution.getVariable("WorkflowException")
            String errorCode = String.valueOf(workflowException.getErrorCode())
            String errorMessage = workflowException.getErrorMessage()
            String falloutHandlerRequest =
                    """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                xmlns:ns="http://org.onap/so/request/types/v1">
                        <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
                            <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
                            <action>UPDATE</action>
                            <source>VID</source>
                        </request-info>
                        <aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
                            <aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
                            <aetgt:ErrorCode>${MsoUtils.xmlEscape(errorCode)}</aetgt:ErrorCode>
                        </aetgt:WorkflowException>
                    </aetgt:FalloutHandlerRequest>"""
            String xmlFalloutHandlerRequest = utils.formatXml(falloutHandlerRequest)

            execution.setVariable(prefix + "FalloutHandlerRequest", xmlFalloutHandlerRequest)

            logger.debug("FalloutHandlerRequest of PNF Software Upgrade - " + "\n" + xmlFalloutHandlerRequest)
        } catch (Exception e) {
            String msg = "Prepare FalloutHandler error for PNF software upgrade - " + e.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
        }
    }
}
