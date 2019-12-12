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
import static org.onap.so.bpmn.infrastructure.pnf.delegate.PnfInputCheckersTestUtils.*;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PnfCheckInputsTest {

    @Mock
    private AssignPnfInputsCheckerDelegate assignPnfInputsCheckerDelegate;
    private DelegateExecutionBuilder delegateExecutionBuilder;

    private PnfCheckInputs sut;
    private DelegateExecution execution;

    @Before
    public void setUp() {
        delegateExecutionBuilder = new DelegateExecutionBuilder();
    }

    @Test
    public void shouldThrowException_whenPnfEntryNotificationTimeoutIsNull() {
        prepareSutWithSetNotificationTimeout(null);
        execution = delegateExecutionBuilder.build();
        assertThatSutExecutionThrowsExceptionOfInstance(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenPnfEntryNotificationTimeoutIsEmpty() {
        prepareSutWithSetNotificationTimeout(StringUtils.EMPTY);
        execution = delegateExecutionBuilder.build();
        assertThatSutExecutionThrowsExceptionOfInstance(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenServiceInstanceIdIsNotSet() {
        prepareSutWithSetNotificationTimeout(PNF_ENTRY_NOTIFICATION_TIMEOUT);
        execution = delegateExecutionBuilder.setServiceInstanceId(null).build();
        assertThatSutExecutionThrowsExceptionOfInstance(BpmnError.class);
    }

    private void prepareSutWithSetNotificationTimeout(String pnfEntryNotificationTimeout) {
        sut = new PnfCheckInputs(pnfEntryNotificationTimeout, assignPnfInputsCheckerDelegate);
    }

    private void assertThatSutExecutionThrowsExceptionOfInstance(Class<?> type) {
        assertThatThrownBy(() -> sut.execute(execution)).isInstanceOf(type);
    }

}
