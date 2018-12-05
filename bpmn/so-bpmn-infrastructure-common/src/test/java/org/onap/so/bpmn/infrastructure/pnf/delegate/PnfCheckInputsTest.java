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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.TIMEOUT_FOR_NOTIFICATION;

import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;

public class PnfCheckInputsTest {

    private static final String DEFAULT_TIMEOUT = "P1D";
    private static final String VALID_UUID = UUID.nameUUIDFromBytes("testUuid".getBytes()).toString();
    private static final String RESERVED_UUID = new UUID(0, 0).toString();

    private DelegateExecution delegateExecution;

    @Before
    public void setUp() {
        delegateExecution = mock(DelegateExecution.class);
        when(delegateExecution.getVariable("testProcessKey")).thenReturn("testProcessKeyValue");
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
        verify(delegateExecution).setVariable(eq(TIMEOUT_FOR_NOTIFICATION), eq(DEFAULT_TIMEOUT));
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
        when(delegateExecution.getVariable(CORRELATION_ID)).thenReturn(correlationId);
        when(delegateExecution.getVariable(PNF_UUID)).thenReturn(VALID_UUID);
        return testedObject;
    }

    private PnfCheckInputs prepareExecutionForTimeout(String defaultTimeout, String timeout) {
        PnfCheckInputs testedObject = new PnfCheckInputs(defaultTimeout);
        when(delegateExecution.getVariable(CORRELATION_ID)).thenReturn("testCorrelationId");
        when(delegateExecution.getVariable(PNF_UUID)).thenReturn(VALID_UUID);
        when(delegateExecution.getVariable(TIMEOUT_FOR_NOTIFICATION)).thenReturn(timeout);
        return testedObject;
    }

    private PnfCheckInputs prepareExecutionForUuid(String uuid) {
        PnfCheckInputs testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        when(delegateExecution.getVariable(CORRELATION_ID)).thenReturn("testCorrelationId");
        when(delegateExecution.getVariable(PNF_UUID)).thenReturn(uuid);
        return testedObject;
    }
}