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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;

public class CreateAaiEntryWithPnfIdDelegateTest {

    @Test
    public void shouldSetPnfIdAndPnfName() throws Exception {
        // given
        CreateAaiEntryWithPnfIdDelegate delegate = new CreateAaiEntryWithPnfIdDelegate();
        AaiConnectionTestImpl aaiConnection = new AaiConnectionTestImpl();
        delegate.setAaiConnection(aaiConnection);
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable(eq(CORRELATION_ID))).thenReturn("testCorrelationId");
        // when
        delegate.execute(execution);
        // then
        assertThat(aaiConnection.getCreated().get("testCorrelationId").getPnfId()).isEqualTo("testCorrelationId");
        assertThat(aaiConnection.getCreated().get("testCorrelationId").getPnfName()).isEqualTo("testCorrelationId");
    }
}