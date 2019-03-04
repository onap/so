/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Nokia.
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

package org.onap.so.bpmn.common.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.common.workflow.service.CallbackHandlerService.CallbackError;
import org.onap.so.bpmn.common.workflow.service.CallbackHandlerService.CallbackResult;
import org.onap.so.bpmn.common.workflow.service.CallbackHandlerService.CallbackSuccess;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.springframework.core.env.Environment;

public class CallbackHandlerServiceTest {

    private static final String METHOD_NAME = "testMethod";
    private static final String MESSAGE = "testMessage";
    private static final String EVENT_NAME = "eventNameTest";
    private static final String MESSAGE_VARIABLE = "messageVarTest";
    private static final String CORRELATION_VARIABLE = "corrVarTest";
    private static final String CORRELATION_VALUE = "corrValueTest";
    private static final String LOG_MARKER = "markerTest";

    private RuntimeService runtimeServiceMock;
    private CallbackHandlerService testedObject;

    @Before
    public void setup() {
        runtimeServiceMock = mock(RuntimeService.class);
        testedObject = new CallbackHandlerService(runtimeServiceMock);
        mockEnvironment();
    }

    @Test
    public void callbackSuccessful() {
        // given
        mockRuntimeService(new ArrayList<>(Arrays.asList(new ExecutionEntity())));
        // when
        CallbackResult callbackResult = testedObject.handleCallback(METHOD_NAME, MESSAGE, EVENT_NAME, MESSAGE_VARIABLE,
                CORRELATION_VARIABLE, CORRELATION_VALUE, LOG_MARKER, new HashMap<>());
        // then
        assertThat(callbackResult).isExactlyInstanceOf(CallbackSuccess.class);
    }

    @Test
    public void callbackNotSuccessful_noWaitingProcesses() {
        // given
        mockRuntimeService(Collections.emptyList());
        // when
        CallbackResult callbackResult = testedObject.handleCallback(METHOD_NAME, MESSAGE, EVENT_NAME, MESSAGE_VARIABLE,
                CORRELATION_VARIABLE, CORRELATION_VALUE, LOG_MARKER, new HashMap<>());
        // then
        assertThat(callbackResult).isExactlyInstanceOf(CallbackError.class);
    }

    private void mockRuntimeService(List<Execution> waitingProcesses) {
        ExecutionQuery executionQueryMock = mock(ExecutionQueryImpl.class);
        when(runtimeServiceMock.createExecutionQuery()).thenReturn(executionQueryMock);
        when(executionQueryMock.messageEventSubscriptionName("eventNameTest")).thenReturn(executionQueryMock);
        when(executionQueryMock.processVariableValueEquals("corrVarTest", "corrValueTest"))
                .thenReturn(executionQueryMock);
        when(executionQueryMock.list()).thenReturn(waitingProcesses);
    }

    private Environment mockEnvironment() {
        Environment mockEnvironment = mock(Environment.class);
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader();
        urnPropertiesReader.setEnvironment(mockEnvironment);
        when(mockEnvironment.getProperty("mso.correlation.timeout")).thenReturn("1");
        return mockEnvironment;
    }
}
