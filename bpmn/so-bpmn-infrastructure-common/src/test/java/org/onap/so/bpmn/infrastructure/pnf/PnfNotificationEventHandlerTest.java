/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.pnf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.util.Map;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames;

public class PnfNotificationEventHandlerTest {

    private static final String TEST_PNF_CORRELATION_ID = "TEST_PNF_CORRELATION_ID";
    private static final String TEST_NOT_INTERESTEED_PNF_CORRELATION_ID = "correlationId2";
    public static final String TEST_BUSINESS_KEY = "testBusinessKey";

    private PnfNotificationEventHandler objectUnderTest;
    private DelegateExecution execution;
    private PnfNotificationEvent pnfNotificationEvent;
    private MessageCorrelationBuilder messageCorrelationBuilder;

    @Before
    public void setUp() {
        execution = mockDelegateExecution();

        objectUnderTest = new PnfNotificationEventHandler();
        objectUnderTest.registerPnf(TEST_PNF_CORRELATION_ID, execution);
        pnfNotificationEvent = new PnfNotificationEvent(this, TEST_PNF_CORRELATION_ID);
    }

    @Test
    public void registerPnf_validPnf_expectedOutput() {
        Map<String, DelegateExecution> pnfCorrelationIdMap = objectUnderTest.getPnfCorrelationIdMap();
        assertThat(pnfCorrelationIdMap).hasSize(1);
        assertThat(pnfCorrelationIdMap.get(TEST_PNF_CORRELATION_ID)).isEqualTo(execution);
    }

    @Test
    public void unregisterPnf_validPnf_exepectedOutput() {
        objectUnderTest.unregisterPnf(TEST_PNF_CORRELATION_ID);
        Map<String, DelegateExecution> pnfCorrelationIdMap = objectUnderTest.getPnfCorrelationIdMap();
        assertThat(pnfCorrelationIdMap).hasSize(0);
    }

    @Test
    public void onApplicationEvent_notificationNotEnabled_notCalled() {
        objectUnderTest.setUseNotification(false);
        objectUnderTest.onApplicationEvent(pnfNotificationEvent);
        verifyZeroInteractions(execution);
    }

    @Test
    public void onApplicationEvent_notificationEnabled_Called() {
        objectUnderTest.setUseNotification(true);
        objectUnderTest.onApplicationEvent(pnfNotificationEvent);

        InOrder inOrder = inOrder(messageCorrelationBuilder);
        inOrder.verify(messageCorrelationBuilder).processInstanceBusinessKey(TEST_BUSINESS_KEY);
        inOrder.verify(messageCorrelationBuilder).correlateWithResult();
    }

    @Test
    public void onApplicationEvent_notificationEnabledNotInterestedPnf_notCalled() {
        objectUnderTest.setUseNotification(true);
        pnfNotificationEvent = new PnfNotificationEvent(this, TEST_NOT_INTERESTEED_PNF_CORRELATION_ID);
        objectUnderTest.onApplicationEvent(pnfNotificationEvent);
        verifyZeroInteractions(execution);
    }

    private DelegateExecution mockDelegateExecution() {
        DelegateExecution delegateExecution = mock(DelegateExecution.class);
        when(delegateExecution.getVariable(eq(ExecutionVariableNames.PNF_CORRELATION_ID)))
                .thenReturn(TEST_PNF_CORRELATION_ID);
        when(delegateExecution.getProcessBusinessKey()).thenReturn(TEST_BUSINESS_KEY);
        ProcessEngineServices processEngineServices = mock(ProcessEngineServices.class);
        when(delegateExecution.getProcessEngineServices()).thenReturn(processEngineServices);
        RuntimeService runtimeService = mock(RuntimeService.class);
        when(processEngineServices.getRuntimeService()).thenReturn(runtimeService);
        messageCorrelationBuilder = mock(MessageCorrelationBuilder.class);
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey(any())).thenReturn(messageCorrelationBuilder);
        return delegateExecution;
    }
}
