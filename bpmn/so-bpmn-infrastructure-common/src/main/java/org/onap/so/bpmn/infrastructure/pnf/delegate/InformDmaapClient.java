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
import org.onap.so.bpmn.infrastructure.pnf.PnfNotificationEvent;
import org.onap.so.bpmn.infrastructure.pnf.dmaap.DmaapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class InformDmaapClient implements JavaDelegate, ApplicationListener<PnfNotificationEvent> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private DmaapClient dmaapClient;
    private DelegateExecution execution;

    @Override
    public void execute(DelegateExecution execution) {
        String pnfCorrelationId = (String) execution.getVariable(ExecutionVariableNames.PNF_CORRELATION_ID);
        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
        dmaapClient.registerForUpdate(pnfCorrelationId, () ->
            runtimeService
                .createMessageCorrelation("WorkflowMessage")
                .processInstanceBusinessKey(execution.getProcessBusinessKey())
                .correlateWithResult()
        );
        this.execution = execution;
    }

    @Autowired
    public void setDmaapClient(DmaapClient dmaapClient) {
        this.dmaapClient = dmaapClient;
    }

    @Override
    public void onApplicationEvent(PnfNotificationEvent event) {
        logger.info("Received application event for pnfCorrelationId: {}", event.getPnfCorrelationId());
        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
        runtimeService
            .createMessageCorrelation("WorkflowMessage")
            .processInstanceBusinessKey(execution.getProcessBusinessKey())
            .correlateWithResult();
    }
}
