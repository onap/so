/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright 2018 Nokia
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.GLOBAL_CUSTOMER_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_INSTANCE_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_TYPE;
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
    private static final String DEFAULT_GLOBAL_CUSTOMER_ID = "id123";
    private static final String DEFAULT_SERVICE_TYPE = "service1";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "instance123";
    private static final String DEFAULT_CORRELATION_ID = "testCorrelationId";

    private DelegateExecutionBuilder delegateExecutionBuilder;
    private PnfCheckInputs testedObject;

    @Before
    public void setUp() {
        delegateExecutionBuilder = new DelegateExecutionBuilder();
    }

    @Test
    public void shouldThrowException_whenCorrelationIdNotSet() {
        DelegateExecution execution = prepareExecutionForCorrelationId(null);
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenCorrelationIdIsEmptyString() {
        DelegateExecution execution = prepareExecutionForCorrelationId("");
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenTimeoutIsNotSetAndDefaultIsNotDefined() {
        DelegateExecution execution = prepareExecutionForTimeout(null, null);
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenTimeoutIsEmptyStringAndDefaultIsNotDefined() {
        DelegateExecution execution = prepareExecutionForTimeout(null, "");
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldSetDefaultTimeout_whenTimeoutIsNotSet() {
        DelegateExecution execution = prepareExecutionForTimeout(DEFAULT_TIMEOUT, null);
        testedObject.execute(execution);
        assertThat(execution.getVariable(TIMEOUT_FOR_NOTIFICATION)).isEqualTo(DEFAULT_TIMEOUT);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsNotSet() {
        DelegateExecution execution = prepareExecutionForUuid(null);
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsEmptyString() {
        DelegateExecution execution = prepareExecutionForUuid("");
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsReservedUuid() {
        DelegateExecution execution = prepareExecutionForUuid(RESERVED_UUID);
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenGlobalCustomerIdIsNotSet() {
        DelegateExecution execution = prepareExecutionForPnfConnectionParams(
            null, DEFAULT_SERVICE_TYPE, DEFAULT_SERVICE_INSTANCE_ID);
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenServiceTypeIsNotSet() {
        DelegateExecution execution = prepareExecutionForPnfConnectionParams(
            DEFAULT_GLOBAL_CUSTOMER_ID, null, DEFAULT_SERVICE_INSTANCE_ID);
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenServiceInstanceIdIsNotSet() {
        DelegateExecution execution = prepareExecutionForPnfConnectionParams(
            DEFAULT_GLOBAL_CUSTOMER_ID, DEFAULT_SERVICE_TYPE, null);
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(BpmnError.class);
    }

    private DelegateExecution prepareExecutionForCorrelationId(String correlationId) {
        testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        delegateExecutionBuilder.setCorrelationId(correlationId);
        delegateExecutionBuilder.setPnfUuid(VALID_UUID);
        return delegateExecutionBuilder.build();
    }

    private DelegateExecution prepareExecutionForTimeout(String defaultTimeout, String timeout) {
        testedObject = new PnfCheckInputs(defaultTimeout);
        delegateExecutionBuilder.setTimeoutForNotification(timeout);
        return delegateExecutionBuilder.build();
    }

    private DelegateExecution prepareExecutionForUuid(String uuid) {
        testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        delegateExecutionBuilder.setPnfUuid(uuid);
        return delegateExecutionBuilder.build();
    }

    private DelegateExecution prepareExecutionForPnfConnectionParams(String globalCustomerId, String serviceType, String serviceInstanceId) {
        testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        delegateExecutionBuilder.setGlobalCustomerId(globalCustomerId);
        delegateExecutionBuilder.setServiceType(serviceType);
        delegateExecutionBuilder.setServiceInstanceId(serviceInstanceId);
        return delegateExecutionBuilder.build();
    }

    private static class DelegateExecutionBuilder {
        private String correlationId = DEFAULT_CORRELATION_ID;
        private String pnfUuid = VALID_UUID;
        private String globalCustomerId = DEFAULT_GLOBAL_CUSTOMER_ID;
        private String serviceType = DEFAULT_SERVICE_TYPE;
        private String serviceInstanceId = DEFAULT_SERVICE_INSTANCE_ID;
        private String timeoutForNotification;

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
        }

        public void setPnfUuid(String pnfUuid) {
            this.pnfUuid = pnfUuid;
        }

        public void setGlobalCustomerId(String globalCustomerId) {
            this.globalCustomerId = globalCustomerId;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        public void setServiceInstanceId(String serviceInstanceId) {
            this.serviceInstanceId = serviceInstanceId;
        }

        public void setTimeoutForNotification(String timeoutForNotification) {
            this.timeoutForNotification = timeoutForNotification;
        }

        public DelegateExecution build() {
            DelegateExecution execution = new DelegateExecutionFake();
            execution.setVariable("testProcessKey", "testProcessKeyValue");
            execution.setVariable(CORRELATION_ID, this.correlationId);
            execution.setVariable(PNF_UUID, this.pnfUuid);
            execution.setVariable(GLOBAL_CUSTOMER_ID, this.globalCustomerId);
            execution.setVariable(SERVICE_TYPE, this.serviceType);
            execution.setVariable(SERVICE_INSTANCE_ID, this.serviceInstanceId);
            execution.setVariable(TIMEOUT_FOR_NOTIFICATION, this.timeoutForNotification);
            return execution;
        }
    }
}
