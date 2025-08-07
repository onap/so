/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.aai.domain.yang.Pnf;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.client.cds.PayloadConstants.PRC_TARGET_SOFTWARE_VERSION;

@RunWith(SpringJUnit4ClassRunner.class)
public class UpdatePnfEntryInAaiTest {

    @InjectMocks
    private UpdatePnfEntryInAai updatePnfEntryInAai;

    @Mock
    private PnfManagement pnfManagementTest;

    private DelegateExecution execution;



    @Test
    public void shouldSetSwVersion() throws Exception {
        // given
        setupPnf();
        setupExecution();

        // when
        updatePnfEntryInAai.execute(execution);

        // verify
        Optional<Pnf> modifiedEntry = pnfManagementTest.getEntryFor("testPnfCorrelationId");
        assertNotNull(modifiedEntry.get());
        assertThat(modifiedEntry.get().getPnfId()).isEqualTo("testtest");
        assertThat(modifiedEntry.get().getPnfName()).isEqualTo("testPnfCorrelationId");
        assertThat(modifiedEntry.get().getSwVersion()).isEqualTo("demo-1.2");
        verify(pnfManagementTest, times(2)).getEntryFor(anyString());
    }

    private void setupPnf() {
        try {
            Pnf pnf = new Pnf();
            pnf.setSwVersion("1");
            pnf.setPnfId("testtest");
            pnf.setPnfName("testPnfCorrelationId");
            doReturn(Optional.of(pnf)).when(pnfManagementTest).getEntryFor(anyString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupExecution() {
        execution = mock(DelegateExecution.class);
        given(execution.getVariable(eq(PNF_CORRELATION_ID))).willReturn("testPnfCorrelationId");
        given(execution.getVariable(eq(PNF_UUID))).willReturn("testtest");
        given(execution.getVariable(eq(PRC_TARGET_SOFTWARE_VERSION))).willReturn("demo-1.2");
    }
}
