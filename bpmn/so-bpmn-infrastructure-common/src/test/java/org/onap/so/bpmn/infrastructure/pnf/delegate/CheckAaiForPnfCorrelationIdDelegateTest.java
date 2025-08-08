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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.PnfManagementTestImpl.ID_WITHOUT_ENTRY;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.PnfManagementTestImpl.ID_WITH_ENTRY;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.AAI_CONTAINS_INFO_ABOUT_PNF;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.so.bpmn.core.WorkflowException;

@RunWith(Enclosed.class)
public class CheckAaiForPnfCorrelationIdDelegateTest {

    public static class ConnectionOkTests {

        private CheckAaiForPnfCorrelationIdDelegate delegate;

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Before
        public void setUp() {
            delegate = new CheckAaiForPnfCorrelationIdDelegate();
            delegate.setPnfManagement(new PnfManagementTestImpl());
        }

        @Test
        public void shouldThrowExceptionWhenPnfCorrelationIdIsNotSet() {
            // given
            DelegateExecution execution = mock(DelegateExecution.class);
            when(execution.getVariable(PNF_CORRELATION_ID)).thenReturn(null);
            when(execution.getVariable("testProcessKey")).thenReturn("testProcessKeyValue");
            // when, then
            expectedException.expect(BpmnError.class);
            delegate.execute(execution);
            verify(execution).setVariable(eq("WorkflowException"), any(WorkflowException.class));
        }

        @Test
        public void shouldSetCorrectVariablesWhenAaiDoesNotContainInfoAboutPnf() {
            // given
            DelegateExecution execution = mock(DelegateExecution.class);
            when(execution.getVariable(PNF_CORRELATION_ID)).thenReturn(ID_WITHOUT_ENTRY);
            // when
            delegate.execute(execution);
            // then
            verify(execution).setVariableLocal(AAI_CONTAINS_INFO_ABOUT_PNF, false);
        }

        @Test
        public void shouldSetCorrectVariablesWhenAaiContainsInfoAboutPnfWithoutIp() {
            // given
            DelegateExecution execution = mock(DelegateExecution.class);
            when(execution.getVariable(PNF_CORRELATION_ID)).thenReturn(ID_WITH_ENTRY);
            // when
            delegate.execute(execution);
            // then
            verify(execution).setVariableLocal(AAI_CONTAINS_INFO_ABOUT_PNF, true);
        }
    }

    public static class NoConnectionTests {

        private CheckAaiForPnfCorrelationIdDelegate delegate;

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Before
        public void setUp() {
            delegate = new CheckAaiForPnfCorrelationIdDelegate();
            delegate.setPnfManagement(new PnfManagementThrowingException());
        }

        @Test
        public void shouldThrowExceptionWhenIoExceptionOnConnectionToAai() {
            // given
            DelegateExecution execution = mock(DelegateExecution.class);
            when(execution.getVariable(PNF_CORRELATION_ID)).thenReturn(ID_WITH_ENTRY);
            when(execution.getVariable("testProcessKey")).thenReturn("testProcessKey");
            // when, then
            expectedException.expect(BpmnError.class);
            delegate.execute(execution);
            verify(execution).setVariable(eq("WorkflowException"), any(WorkflowException.class));
        }
    }
}
