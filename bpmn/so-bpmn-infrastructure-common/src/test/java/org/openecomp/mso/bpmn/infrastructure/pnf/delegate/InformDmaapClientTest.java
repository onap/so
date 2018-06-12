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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InformDmaapClient.class, DmaapClientTestImpl.class})
public class InformDmaapClientTest {

    @Autowired
    private InformDmaapClient informDmaapClient;

    @Autowired
    private DmaapClientTestImpl dmaapClientTest;

    private DelegateExecution delegateExecution;

    private MessageCorrelationBuilder messageCorrelationBuilder;

    @Test
    public void shouldSendListenerToDmaapClient() throws Exception {
        // given
        mockDelegateExecution();
        // when
        informDmaapClient.execute(delegateExecution);
        // then
        assertThat(dmaapClientTest.getCorrelationId()).isEqualTo("testCorrelationId");
        assertThat(dmaapClientTest.getInformConsumer()).isNotNull();
        verifyZeroInteractions(messageCorrelationBuilder);
    }

    @Test
    public void shouldSendListenerToDmaapClientAndSendMessageToCamunda() throws Exception {
        // given
        mockDelegateExecution();
        // when
        informDmaapClient.execute(delegateExecution);
        dmaapClientTest.getInformConsumer().run();
        // then
        assertThat(dmaapClientTest.getCorrelationId()).isEqualTo("testCorrelationId");
        InOrder inOrder = inOrder(messageCorrelationBuilder);
        inOrder.verify(messageCorrelationBuilder).processInstanceBusinessKey("testBusinessKey");
        inOrder.verify(messageCorrelationBuilder).correlateWithResult();
    }

    private void mockDelegateExecution() {
        delegateExecution = mock(DelegateExecution.class);
        when(delegateExecution.getVariable(eq(ExecutionVariableNames.CORRELATION_ID))).thenReturn("testCorrelationId");
        when(delegateExecution.getProcessBusinessKey()).thenReturn("testBusinessKey");
        ProcessEngineServices processEngineServices = mock(ProcessEngineServices.class);
        when(delegateExecution.getProcessEngineServices()).thenReturn(processEngineServices);
        RuntimeService runtimeService = mock(RuntimeService.class);
        when(processEngineServices.getRuntimeService()).thenReturn(runtimeService);
        messageCorrelationBuilder = mock(MessageCorrelationBuilder.class);
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey(any())).thenReturn(messageCorrelationBuilder);
    }
}