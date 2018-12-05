/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Nokia.
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

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class GenerateUuidDelegateTest {

    private GenerateUuidDelegate delegate;

    @Before
    public void setUp() {
        delegate = new GenerateUuidDelegate();
    }

    @Test
    public void execute_shouldSetValidUuidAsPnfUuid() {
        // given
        DelegateExecution execution = mock(DelegateExecution.class);
        // when
        delegate.execute(execution);
        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(execution).setVariable(eq(PNF_UUID), captor.capture());
        assertTrue(captor.getValue().matches(PnfCheckInputs.UUID_V4_REGEX));
    }
}