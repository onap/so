/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.infrastructure.pnf.dmaap.DmaapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InformDmaapClient implements JavaDelegate {

    private final static int MAX_AMOUNT_OF_QUERIES_TO_CAMUNDA = 10;
    private final static int WAIT_FOR_NEXT_QUERY_TO_CAMUNDA_IN_MS = 1_000;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private DmaapClient dmaapClient;

    @Override
    public void execute(DelegateExecution execution) {
        String pnfCorrelationId = (String) execution.getVariable(ExecutionVariableNames.PNF_CORRELATION_ID);
        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
        String processBusinessKey = execution.getProcessBusinessKey();
        dmaapClient.registerForUpdate(pnfCorrelationId, () -> {
            waitForWorkflow(runtimeService, processBusinessKey);
            runtimeService.createMessageCorrelation("WorkflowMessage").processInstanceBusinessKey(processBusinessKey)
                    .correlateWithResult();
        });
    }

    private void waitForWorkflow(RuntimeService runtimeService, String processBusinessKey) {
        int counter = 0;
        while (!isWorkflowWaitingForMessage(runtimeService, processBusinessKey)) {
            counter++;
            if (counter > MAX_AMOUNT_OF_QUERIES_TO_CAMUNDA) {
                logger.error("Maximum wait for camunda reached. Aboting correlating message.");
                throw new RuntimeException("Timeout while waiting for workflow to proceed");
            }
            try {
                Thread.sleep(WAIT_FOR_NEXT_QUERY_TO_CAMUNDA_IN_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isWorkflowWaitingForMessage(RuntimeService runtimeService, String processBusinessKey) {
        return runtimeService.createExecutionQuery().messageEventSubscriptionName("WorkflowMessage")
                .processInstanceBusinessKey(processBusinessKey).singleResult() != null;
    }

    @Autowired
    public void setDmaapClient(DmaapClient dmaapClient) {
        this.dmaapClient = dmaapClient;
    }
}
