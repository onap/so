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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_INSTANCE_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.TIMEOUT_FOR_NOTIFICATION;

import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;

public class PnfCheckInputsTest {

    private static final String DEFAULT_TIMEOUT = "P1D";
    private static final String VALID_UUID = UUID.nameUUIDFromBytes("testUuid".getBytes()).toString();
    private static final String RESERVED_UUID = new UUID(0, 0).toString();
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "da7d07d9-b71c-4128-809d-2ec01c807169";
    private static final String DEFAULT_CORRELATION_ID = "testCorrelationId";

    private DelegateExecutionBuilder delegateExecutionBuilder;
    private PnfCheckInputs testedObject;

    @Before
    public void setUp() {
        delegateExecutionBuilder = new DelegateExecutionBuilder();
    }

    @Test
    public void shouldThrowException_whenCorrelationIdNotSet() {
        testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        DelegateExecution execution = delegateExecutionBuilder.setCorrelationId(null).setPnfUuid(VALID_UUID).build();
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenTimeoutIsEmptyStringAndDefaultIsNotDefined() {
        testedObject = new PnfCheckInputs(null);
        DelegateExecution execution = delegateExecutionBuilder.setTimeoutForNotification("").build();
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldSetDefaultTimeout_whenTimeoutIsNotSet() {
        testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        DelegateExecution execution = delegateExecutionBuilder.setTimeoutForNotification(null).build();
        testedObject.execute(execution);
        assertThat(execution.getVariable(TIMEOUT_FOR_NOTIFICATION)).isEqualTo(DEFAULT_TIMEOUT);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsNotSet() {
        testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        DelegateExecution execution = delegateExecutionBuilder.setPnfUuid(null).build();
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsEmptyString() {
        testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        DelegateExecution execution = delegateExecutionBuilder.setPnfUuid("").build();
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsReservedUuid() {
        testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        DelegateExecution execution = delegateExecutionBuilder.setPnfUuid(RESERVED_UUID).build();
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenServiceInstanceIdIsNotSet() {
        testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        DelegateExecution execution = delegateExecutionBuilder.setServiceInstanceId(null).build();
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    private static class DelegateExecutionBuilder {
        private String correlationId = DEFAULT_CORRELATION_ID;
        private String pnfUuid = VALID_UUID;
        private String serviceInstanceId = DEFAULT_SERVICE_INSTANCE_ID;
        private String timeoutForNotification;

        public DelegateExecutionBuilder setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public DelegateExecutionBuilder setPnfUuid(String pnfUuid) {
            this.pnfUuid = pnfUuid;
            return this;
        }

        public DelegateExecutionBuilder setServiceInstanceId(String serviceInstanceId) {
            this.serviceInstanceId = serviceInstanceId;
            return this;
        }

        public DelegateExecutionBuilder setTimeoutForNotification(String timeoutForNotification) {
            this.timeoutForNotification = timeoutForNotification;
            return this;
        }

        public DelegateExecution build() {
            DelegateExecution execution = new DelegateExecutionFake();
            execution.setVariable("testProcessKey", "testProcessKeyValue");
            execution.setVariable(CORRELATION_ID, this.correlationId);
            execution.setVariable(PNF_UUID, this.pnfUuid);
            execution.setVariable(SERVICE_INSTANCE_ID, this.serviceInstanceId);
            execution.setVariable(TIMEOUT_FOR_NOTIFICATION, this.timeoutForNotification);
            return execution;
        }
    }
}
