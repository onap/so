/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia.
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

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.TIMEOUT_FOR_NOTIFICATION;
import com.google.common.base.Strings;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.pnf.kafka.KafkaClient;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class is designed to be used within WaitForPnfReadyBB
 */
@Component
public class RegisterForPnfReadyEvent implements JavaDelegate {

    private static final String ERROR_MESSAGE_PNF_NOT_FOUND =
            "pnf resource not found in buildingBlockExecution while registering to kafka listener";
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterForPnfReadyEvent.class);

    private KafkaClient kafkaClient;
    private ExtractPojosForBB extractPojosForBB;
    private ExceptionBuilder exceptionBuilder;
    private String pnfEntryNotificationTimeout;

    @Autowired
    public RegisterForPnfReadyEvent(KafkaClient kafkaClient, ExtractPojosForBB extractPojosForBB,
            ExceptionBuilder exceptionBuilder,
            @Value("${aai.pnfEntryNotificationTimeout}") String pnfEntryNotificationTimeout) {
        this.kafkaClient = kafkaClient;
        this.extractPojosForBB = extractPojosForBB;
        this.exceptionBuilder = exceptionBuilder;
        this.pnfEntryNotificationTimeout = pnfEntryNotificationTimeout;
    }

    @Override
    public void execute(DelegateExecution execution) {
        try {
            String pnfName = getPnfName(execution);
            fillExecution(execution, pnfName);
            RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
            kafkaClient.registerForUpdate(pnfName, () -> runtimeService.createMessageCorrelation("WorkflowMessage")
                    .processInstanceId(execution.getProcessInstanceId()).correlateWithResult());
        } catch (BBObjectNotFoundException e) {
            LOGGER.error(ERROR_MESSAGE_PNF_NOT_FOUND);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, ERROR_MESSAGE_PNF_NOT_FOUND);
        }
    }

    private void fillExecution(DelegateExecution execution, String pnfName) {
        execution.setVariable(ExecutionVariableNames.PNF_CORRELATION_ID, pnfName);
        if (Strings.isNullOrEmpty(pnfEntryNotificationTimeout)) {
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,
                    "pnfEntryNotificationTimeout value not defined");
        }
        execution.setVariable(TIMEOUT_FOR_NOTIFICATION, pnfEntryNotificationTimeout);
    }

    private String getPnfName(DelegateExecution execution) throws BBObjectNotFoundException {
        BuildingBlockExecution buildingBlockExecution =
                (BuildingBlockExecution) execution.getVariable("gBuildingBlockExecution");
        Pnf pnf = extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.PNF);
        String pnfName = pnf.getPnfName();
        if (Strings.isNullOrEmpty(pnfName)) {
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, "pnf name is not set");
        }
        return pnfName;
    }
}
