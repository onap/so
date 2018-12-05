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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.onap.aai.domain.yang.Pnf;

public class CreatePnfEntryInAaiDelegateTest {

    @Test
    public void shouldSetPnfIdAndPnfName() throws Exception {
        // given
        CreatePnfEntryInAaiDelegate delegate = new CreatePnfEntryInAaiDelegate();
        AaiConnectionTestImpl aaiConnection = new AaiConnectionTestImpl();
        delegate.setAaiConnection(aaiConnection);
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable(eq(CORRELATION_ID))).thenReturn("testCorrelationId");
        when(execution.getVariable(eq(PNF_UUID))).thenReturn("0269085f-bf9f-48d7-9e00-4f1a8b20f0a6");
        // when
        delegate.execute(execution);
        // then
        Pnf createdEntry = aaiConnection.getCreated().get("testCorrelationId");
        assertThat(createdEntry.getPnfId()).isEqualTo("0269085f-bf9f-48d7-9e00-4f1a8b20f0a6");
        assertThat(createdEntry.getPnfName()).isEqualTo("testCorrelationId");
        assertThat(createdEntry.isInMaint()).isTrue();
    }
}