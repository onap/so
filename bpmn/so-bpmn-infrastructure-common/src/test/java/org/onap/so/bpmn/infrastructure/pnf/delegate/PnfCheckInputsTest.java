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

    private DelegateExecution delegateExecution;

    @Before
    public void setUp() {
        delegateExecution = new DelegateExecutionFake();
        delegateExecution.setVariable("testProcessKey", "testProcessKeyValue");
    }

    @Test
    public void shouldThrowException_whenCorrelationIdNotSet() {
        PnfCheckInputs testedObject = prepareExecutionForCorrelationId(null);
        assertThatThrownBy(() -> testedObject.execute(delegateExecution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenTimeoutIsEmptyStringAndDefaultIsNotDefined() {
        PnfCheckInputs testedObject = prepareExecutionForTimeout(null, "");
        assertThatThrownBy(() -> testedObject.execute(delegateExecution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldSetDefaultTimeout_whenTimeoutIsNotSet() {
        PnfCheckInputs testedObject = prepareExecutionForTimeout(DEFAULT_TIMEOUT, null);
        testedObject.execute(delegateExecution);
        assertThat(delegateExecution.getVariable(TIMEOUT_FOR_NOTIFICATION)).isEqualTo(DEFAULT_TIMEOUT);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsNotSet() {
        PnfCheckInputs testedObject = prepareExecutionForUuid(null);
        assertThatThrownBy(() -> testedObject.execute(delegateExecution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsEmptyString() {
        PnfCheckInputs testedObject = prepareExecutionForUuid("");
        assertThatThrownBy(() -> testedObject.execute(delegateExecution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsReservedUuid() {
        PnfCheckInputs testedObject = prepareExecutionForUuid(RESERVED_UUID);
        assertThatThrownBy(() -> testedObject.execute(delegateExecution)).isInstanceOf(BpmnError.class);
    }

    private PnfCheckInputs prepareExecutionForCorrelationId(String correlationId) {
        PnfCheckInputs testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        delegateExecution.setVariable(CORRELATION_ID, correlationId);
        delegateExecution.setVariable(PNF_UUID, VALID_UUID);
        return testedObject;
    }

    private PnfCheckInputs prepareExecutionForTimeout(String defaultTimeout, String timeout) {
        PnfCheckInputs testedObject = new PnfCheckInputs(defaultTimeout);
        delegateExecution.setVariable(CORRELATION_ID, "testCorrelationId");
        delegateExecution.setVariable(PNF_UUID, VALID_UUID);
        delegateExecution.setVariable(TIMEOUT_FOR_NOTIFICATION, timeout);
        return testedObject;
    }

    private PnfCheckInputs prepareExecutionForUuid(String uuid) {
        PnfCheckInputs testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        delegateExecution.setVariable(CORRELATION_ID, "testCorrelationId");
        delegateExecution.setVariable(PNF_UUID, uuid);
        return testedObject;
    }
}
