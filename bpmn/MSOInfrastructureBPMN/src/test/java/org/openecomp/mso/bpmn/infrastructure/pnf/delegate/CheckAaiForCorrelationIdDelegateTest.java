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

package org.openecomp.mso.bpmn.infrastructure.pnf.delegate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.AaiConnectionTestImpl.ID_WITHOUT_ENTRY;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.AaiConnectionTestImpl.ID_WITH_ENTRY_AND_IP;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.AaiConnectionTestImpl.ID_WITH_ENTRY_NO_IP;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.AaiConnectionTestImpl.ID_WITH_IP_V6;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.AAI_CONTAINS_INFO_ABOUT_IP;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.AAI_CONTAINS_INFO_ABOUT_PNF;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(Enclosed.class)
public class CheckAaiForCorrelationIdDelegateTest {

    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = {CheckAaiForCorrelationIdDelegate.class, AaiConnectionTestImpl.class})
    public static class ConnectionOkTests {

        @Autowired
        private CheckAaiForCorrelationIdDelegate delegate;

        @Test
        public void shouldThrowExceptionWhenCorrelationIdIsNotSet() throws Exception {
            // given
            DelegateExecution execution = mock(DelegateExecution.class);
            when(execution.getVariable(CORRELATION_ID)).thenReturn(null);
            when(execution.getVariable("testProcessKey")).thenReturn("testProcessKeyValue");
            // when, then
            assertThatThrownBy(() -> delegate.execute(execution)).isInstanceOf(BpmnError.class);
            verify(execution).setVariable(eq("WorkflowException"), any(WorkflowException.class));
        }

        @Test
        public void shouldSetCorrectVariablesWhenAaiDoesNotContainInfoAboutPnf() throws Exception {
            // given
            DelegateExecution execution = mock(DelegateExecution.class);
            when(execution.getVariable(CORRELATION_ID)).thenReturn(ID_WITHOUT_ENTRY);
            // when
            delegate.execute(execution);
            // then
            verify(execution).setVariableLocal(AAI_CONTAINS_INFO_ABOUT_PNF, false);
        }

        @Test
        public void shouldSetCorrectVariablesWhenAaiContainsInfoAboutPnf() throws Exception {
            shouldSetCorrectVariablesWhenAaiContainsInfoAboutPnf(ID_WITH_ENTRY_AND_IP);
        }

        @Test
        public void shouldSetCorrectVariablesWhenAaiContainsInfoAboutPnfWithIpV6() throws Exception {
            shouldSetCorrectVariablesWhenAaiContainsInfoAboutPnf(ID_WITH_IP_V6);
        }

        private void shouldSetCorrectVariablesWhenAaiContainsInfoAboutPnf(String id) throws Exception {
            // given
            DelegateExecution execution = mock(DelegateExecution.class);
            when(execution.getVariable(CORRELATION_ID)).thenReturn(id);
            // when
            delegate.execute(execution);
            // then
            verify(execution).setVariableLocal(AAI_CONTAINS_INFO_ABOUT_PNF, true);
            verify(execution).setVariableLocal(AAI_CONTAINS_INFO_ABOUT_IP, true);
        }

        @Test
        public void shouldSetCorrectVariablesWhenAaiContainsInfoAboutPnfWithoutIp() throws Exception {
            // given
            DelegateExecution execution = mock(DelegateExecution.class);
            when(execution.getVariable(CORRELATION_ID)).thenReturn(ID_WITH_ENTRY_NO_IP);
            // when
            delegate.execute(execution);
            // then
            verify(execution).setVariableLocal(AAI_CONTAINS_INFO_ABOUT_PNF, true);
            verify(execution).setVariableLocal(AAI_CONTAINS_INFO_ABOUT_IP, false);
        }
    }


    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = {CheckAaiForCorrelationIdDelegate.class, AaiConnectionThrowingException.class})
    public static class NoConnectionTests {

        @Autowired
        private CheckAaiForCorrelationIdDelegate delegate;

        @Test
        public void shouldThrowExceptionWhenIoExceptionOnConnectionToAai() throws Exception {
            // given
            DelegateExecution execution = mock(DelegateExecution.class);
            when(execution.getVariable(CORRELATION_ID)).thenReturn(ID_WITH_ENTRY_NO_IP);
            when(execution.getVariable("testProcessKey")).thenReturn("testProcessKey");
            // when, then
            assertThatThrownBy(() -> delegate.execute(execution)).isInstanceOf(BpmnError.class);
            verify(execution).setVariable(eq("WorkflowException"), any(WorkflowException.class));
        }
    }
}