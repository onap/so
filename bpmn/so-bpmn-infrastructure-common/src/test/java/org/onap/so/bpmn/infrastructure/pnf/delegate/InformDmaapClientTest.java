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

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.onap.so.bpmn.infrastructure.pnf.PnfNotificationEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class InformDmaapClientTest {

    @Before
    public void setUp() throws Exception {
        informDmaapClient = new InformDmaapClient();
        dmaapClientTest = new DmaapClientTestImpl();
        informDmaapClient.setDmaapClient(dmaapClientTest);
        delegateExecution = mockDelegateExecution();
    }

    private InformDmaapClient informDmaapClient;

    private DmaapClientTestImpl dmaapClientTest;

    private DelegateExecution delegateExecution;

    private MessageCorrelationBuilder messageCorrelationBuilder;

    @Test
    public void shouldSendListenerToDmaapClient() throws Exception {
        // when
        informDmaapClient.execute(delegateExecution);
        // then
        assertThat(dmaapClientTest.getPnfCorrelationId()).isEqualTo("testPnfCorrelationId");
        assertThat(dmaapClientTest.getInformConsumer()).isNotNull();
        verifyZeroInteractions(messageCorrelationBuilder);
    }

    @Test
    public void shouldSendListenerToDmaapClientAndSendMessageToCamunda() throws Exception {
        // when
        informDmaapClient.execute(delegateExecution);
        dmaapClientTest.getInformConsumer().run();
        // then
        assertThat(dmaapClientTest.getPnfCorrelationId()).isEqualTo("testPnfCorrelationId");
        InOrder inOrder = inOrder(messageCorrelationBuilder);
        inOrder.verify(messageCorrelationBuilder).processInstanceBusinessKey("testBusinessKey");
        inOrder.verify(messageCorrelationBuilder).correlateWithResult();
    }

    @Test
    public void onApplicationEvent_validPnfNotificationEvent_expectedOutput() {

        PnfNotificationEvent pnfNotificationEvent = new PnfNotificationEvent(this, "testPnfCorrelationId");

        informDmaapClient.execute(delegateExecution);
        informDmaapClient.onApplicationEvent(pnfNotificationEvent);

        InOrder inOrder = inOrder(messageCorrelationBuilder);
        inOrder.verify(messageCorrelationBuilder).processInstanceBusinessKey("testBusinessKey");
        inOrder.verify(messageCorrelationBuilder).correlateWithResult();
    }

    private DelegateExecution mockDelegateExecution() {
        DelegateExecution delegateExecution = mock(DelegateExecution.class);
        when(delegateExecution.getVariable(eq(ExecutionVariableNames.PNF_CORRELATION_ID)))
                .thenReturn("testPnfCorrelationId");
        when(delegateExecution.getProcessBusinessKey()).thenReturn("testBusinessKey");
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
