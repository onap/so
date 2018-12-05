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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.TIMEOUT_FOR_NOTIFICATION;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PnfCheckInputsTest {

    private static final String DEFAULT_TIMEOUT = "P1D";
    private static final String VALID_UUID = "0269085f-bf9f-48d7-9e00-4f1a8b20f0a6";
    private static final String RESERVED_UUID = "0269085f-bf9f-48d7-0e00-4f1a8b20f0a6";
    private static final String NOT_RANDOM_UUID = "0269085f-bf9f-38d7-9e00-4f1a8b20f0a6";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private DelegateExecution delegateExecution;
    private PnfCheckInputs testedObject;

    @Before
    public void setUp() {
        delegateExecution = mock(DelegateExecution.class);
        when(delegateExecution.getVariable("testProcessKey")).thenReturn("testProcessKeyValue");
    }

    @Test
    public void shouldThrowException_whenCorrelationIdNotSet() {
        prepareExecutionForCorrelationId(null);
        expectException();
    }

    @Test
    public void shouldThrowException_whenCorrelationIdIsEmptyString() {
        prepareExecutionForCorrelationId("");
        expectException();
    }

    @Test
    public void shouldThrowException_whenTimeoutIsNotSetAndDefaultIsNotDefined() {
        prepareExecutionForTimeout(null, null);
        expectException();
    }

    @Test
    public void shouldThrowException_whenTimeoutIsEmptyStringAndDefaultIsNotDefined() {
        prepareExecutionForTimeout(null, "");
        expectException();
    }

    @Test
    public void shouldSetDefaultTimeout_whenTimeoutIsNotSet() {
        prepareExecutionForTimeout(DEFAULT_TIMEOUT, null);
        testedObject.execute(delegateExecution);
        verify(delegateExecution).setVariable(eq(TIMEOUT_FOR_NOTIFICATION), eq(DEFAULT_TIMEOUT));
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsNotSet() {
        prepareExecutionForUuid(null);
        expectException();
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsEmptyString() {
        prepareExecutionForUuid("");
        expectException();
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsReservedUuid() {
        prepareExecutionForUuid(RESERVED_UUID);
        expectException();
    }

    @Test
    public void shouldThrowException_whenPnfUuidVersionIsNot4() {
        prepareExecutionForUuid(NOT_RANDOM_UUID);
        expectException();
    }

    private void prepareExecutionForCorrelationId(String correlationId) {
        testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        when(delegateExecution.getVariable(CORRELATION_ID)).thenReturn(correlationId);
        when(delegateExecution.getVariable(PNF_UUID)).thenReturn(VALID_UUID);
    }

    private void prepareExecutionForTimeout(String defaultTimeout, String timeout) {
        testedObject = new PnfCheckInputs(defaultTimeout);
        when(delegateExecution.getVariable(CORRELATION_ID)).thenReturn("testCorrelationId");
        when(delegateExecution.getVariable(PNF_UUID)).thenReturn(VALID_UUID);
        when(delegateExecution.getVariable(TIMEOUT_FOR_NOTIFICATION)).thenReturn(timeout);
    }

    private void prepareExecutionForUuid(String uuid) {
        testedObject = new PnfCheckInputs(DEFAULT_TIMEOUT);
        when(delegateExecution.getVariable(CORRELATION_ID)).thenReturn("testCorrelationId");
        when(delegateExecution.getVariable(PNF_UUID)).thenReturn(uuid);
    }

    private void expectException() {
        expectedException.expect(BpmnError.class);
        testedObject.execute(delegateExecution);
    }
}
