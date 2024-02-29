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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;

public class CancelKafkaSubscriptionTest {

    private static final String TEST_PNF_CORRELATION_ID = "testPnfCorrelationId";

    @Test
    public void shouldCancelSubscription() {
        // given
        CancelKafkaSubscription delegate = new CancelKafkaSubscription();
        KafkaClientTestImpl kafkaClientTest = new KafkaClientTestImpl();
        delegate.setKafkaClient(kafkaClientTest);
        DelegateExecution delegateExecution = mock(DelegateExecution.class);
        when(delegateExecution.getVariable(eq(ExecutionVariableNames.PNF_CORRELATION_ID)))
                .thenReturn(TEST_PNF_CORRELATION_ID);
        when(delegateExecution.getProcessBusinessKey()).thenReturn("testBusinessKey");
        kafkaClientTest.registerForUpdate("testPnfCorrelationId", () -> {
        });
        // when
        delegate.execute(delegateExecution);
        // then
        assertThat(kafkaClientTest.haveRegisteredConsumer()).isFalse();
    }
}
