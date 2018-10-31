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
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.TIMEOUT_FOR_NOTIFICATION;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;

public class PnfCheckInputsTest {

    private static final String DEFAULT_TIMEOUT = "P1D";

    private DelegateExecution mockDelegateExecution() {
        new PnfCheckInputs(DEFAULT_TIMEOUT);
        DelegateExecution delegateExecution = mock(DelegateExecution.class);
        when(delegateExecution.getVariable("testProcessKey")).thenReturn("testProcessKeyValue");
        return delegateExecution;
    }

    @Test
    public void shouldThrowException_whenPnfIdNotSet() {
        // given
        PnfCheckInputs testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        DelegateExecution delegateExecution = mockDelegateExecution();
        // when, then
        assertThatThrownBy(() -> testedObject.execute(delegateExecution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenPnfIdIsEmptyString() throws Exception {
        // given
        PnfCheckInputs testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        DelegateExecution delegateExecution = mockDelegateExecution();
        when(delegateExecution.getVariable(CORRELATION_ID)).thenReturn("");
        // when, then
        assertThatThrownBy(() -> testedObject.execute(delegateExecution)).isInstanceOf(BpmnError.class);
    }

    private DelegateExecution mockDelegateExecutionWithCorrelationId() {
        new PnfCheckInputs(DEFAULT_TIMEOUT);
        DelegateExecution delegateExecution = mockDelegateExecution();
        when(delegateExecution.getVariable(CORRELATION_ID)).thenReturn("testCorrelationId");
        return delegateExecution;
    }

    @Test
    public void shouldThrowException_whenTimeoutIsNotSetAndDefaultIsNotDefined() {
        // given
        PnfCheckInputs testedObject = new PnfCheckInputs(null);
        DelegateExecution delegateExecution = mockDelegateExecutionWithCorrelationId();
        // when, then
        assertThatThrownBy(() -> testedObject.execute(delegateExecution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenTimeoutIsEmptyStringAndDefaultIsNotDefined() throws Exception {
        // given
        PnfCheckInputs testedObject = new PnfCheckInputs(null);
        DelegateExecution delegateExecution = mockDelegateExecutionWithCorrelationId();
        when(delegateExecution.getVariable(TIMEOUT_FOR_NOTIFICATION)).thenReturn("");
        // when, then
        assertThatThrownBy(() -> testedObject.execute(delegateExecution)).isInstanceOf(BpmnError.class);
    }

    @Test
    public void shouldSetDefaultTimeout_whenTimeoutIsNotSet() {
        // given
        PnfCheckInputs testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        DelegateExecution delegateExecution = mockDelegateExecutionWithCorrelationId();
        // when
        testedObject.execute(delegateExecution);
        // then
        verify(delegateExecution).setVariable(eq(TIMEOUT_FOR_NOTIFICATION), eq(DEFAULT_TIMEOUT));
    }
}
