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

package org.openecomp.mso.bpmn.infrastructure.pnf.dmaap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PnfEventReadyConsumerTest {

    private PnfEventReadyConsumer pnfEventReadyConsumer;
    private HttpClient httpClientMock;
    private Runnable threadMock;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        pnfEventReadyConsumer = new PnfEventReadyConsumer();
        pnfEventReadyConsumer.setDmaapHost("hostTest");
        pnfEventReadyConsumer.setDmaapPort(1234);
        pnfEventReadyConsumer.setDmaapProtocol("http");
        pnfEventReadyConsumer.setDmaapUriPathPrefix("eventsForTesting");
        pnfEventReadyConsumer.setDmaapTopicName("eventTopicTest");
        pnfEventReadyConsumer.setConsumerId("consumerTestId");
        pnfEventReadyConsumer.setConsumerGroup("consumerGroupTest");
        pnfEventReadyConsumer.setDmaapClientInitialDelayInSeconds(1);
        pnfEventReadyConsumer.setDmaapClientDelayInSeconds(1);
        pnfEventReadyConsumer.init();
        httpClientMock = mock(HttpClient.class);
        threadMock = mock(Runnable.class);
        setPrivateField();
    }

    @After
    public void closeThread() {
        pnfEventReadyConsumer.stopConsumerThreadImmediately();
    }

    @Test
    public void restClientInvokesWithProperURI() throws IOException {
        pnfEventReadyConsumer.run();
        ArgumentCaptor<HttpGet> captor1 = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClientMock).execute(captor1.capture());
        assertThat(captor1.getValue().getURI()).hasHost("hostTest").hasPort(1234).hasScheme("http")
                .hasPath("/eventsForTesting/eventTopicTest/consumerGroupTest/consumerTestId");
    }

    private void setPrivateField() throws NoSuchFieldException, IllegalAccessException {
        Field httpClientField = pnfEventReadyConsumer.getClass().getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(pnfEventReadyConsumer, httpClientMock);

        Field pnfCorrelationToThreadMapField = pnfEventReadyConsumer.getClass()
                .getDeclaredField("pnfCorrelationIdToThreadMap");
        pnfCorrelationToThreadMapField.setAccessible(true);
        String correlationId = "corrIdTest";
        Map<String, Runnable> pnfCorrelationToThreadMap = new ConcurrentHashMap<>();
        pnfCorrelationToThreadMap.put(correlationId, threadMock);
        pnfCorrelationToThreadMapField.set(pnfEventReadyConsumer, pnfCorrelationToThreadMap);
    }

}
