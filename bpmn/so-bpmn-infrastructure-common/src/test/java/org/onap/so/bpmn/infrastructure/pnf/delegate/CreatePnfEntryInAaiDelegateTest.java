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
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;

import java.util.UUID;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.onap.aai.domain.yang.Pnf;

public class CreatePnfEntryInAaiDelegateTest {

    @Test
    public void shouldSetPnfIdAndPnfName() throws Exception {
        // given
        String pnfUuid = UUID.nameUUIDFromBytes("testUuid".getBytes()).toString();
        CreatePnfEntryInAaiDelegate delegate = new CreatePnfEntryInAaiDelegate();
        PnfManagementTestImpl pnfManagementTest = new PnfManagementTestImpl();
        delegate.setPnfManagement(pnfManagementTest);
        DelegateExecution execution = mock(DelegateExecution.class);
        given(execution.getVariable(eq(CORRELATION_ID))).willReturn("testCorrelationId");
        given(execution.getVariable(eq(PNF_UUID))).willReturn(pnfUuid);
        // when
        delegate.execute(execution);
        // then
        Pnf createdEntry = pnfManagementTest.getCreated().get("testCorrelationId");
        assertThat(createdEntry.getPnfId()).isEqualTo(pnfUuid);
        assertThat(createdEntry.getPnfName()).isEqualTo("testCorrelationId");
        assertThat(createdEntry.isInMaint()).isNull();
    }
}
