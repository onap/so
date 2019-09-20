/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.pnf.dmaap;


import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class PnfEventReadyDmaapClientTest {

    private static final String PNF_CORRELATION_ID = "corrTestId";
    private static final String OTHER_CORRELATION_ID = "otherCorrId";
    private static final String JSON_EXAMPLE_WITH_PNF_CORRELATION_ID = "[{\"correlationId\": \"%s\","
            + "\"value\":\"value1\"},{\"correlationId\": \"corr\",\"value\":\"value2\"}]";

    private static final String HOST = "hostTest";
    private static final int PORT = 1234;
    private static final String PROTOCOL = "http";
    private static final String URI_PATH_PREFIX = "eventsForTesting";
    private static final String EVENT_TOPIC_TEST = "pnfReadyTest";
    private static final String CONSUMER_ID = "consumerTestId";
    private static final String CONSUMER_GROUP = "consumerGroupTest";
    private static final int TOPIC_LISTENER_DELAY_IN_SECONDS = 1;
    private static final Map<String, String> SOME_UPDATE_INFO = Collections.emptyMap();

    @Mock
    private Environment env;
    @Mock
    private HttpClient httpClientMock;
    @Captor
    private ArgumentCaptor<HttpGet> dmaapRequestCaptor;

    private PnfEventReadyDmaapClient testedObject;

    @Before
    public void init() {
        when(env.getProperty(eq("pnf.dmaap.port"), eq(Integer.class))).thenReturn(PORT);
        when(env.getProperty(eq("pnf.dmaap.host"))).thenReturn(HOST);
        when(env.getProperty(eq("pnf.dmaap.protocol"))).thenReturn(PROTOCOL);
        when(env.getProperty(eq("pnf.dmaap.uriPathPrefix"))).thenReturn(URI_PATH_PREFIX);
        when(env.getProperty(eq("pnf.dmaap.topicName"))).thenReturn(EVENT_TOPIC_TEST);
        when(env.getProperty(eq("pnf.dmaap.consumerId"))).thenReturn(CONSUMER_ID);
        when(env.getProperty(eq("pnf.dmaap.consumerGroup"))).thenReturn(CONSUMER_GROUP);
        when(env.getProperty(eq("pnf.dmaap.topicListenerDelayInSeconds"), eq(Integer.class)))
                .thenReturn(TOPIC_LISTENER_DELAY_IN_SECONDS);

        testedObject = new PnfEventReadyDmaapClient(env, httpClientMock);
    }

    @After
    public void cleanup() {
        testedObject.stopDmaapThreadListener();
    }

    @Test
    public void shouldProperlyConstructDmaapPnfReadyTopicRequest() throws Exception {
        CountDownLatch dmaapRequestFiredLatch = new CountDownLatch(1);
        when(httpClientMock.execute(dmaapRequestCaptor.capture())).thenAnswer(invocation -> {
            dmaapRequestFiredLatch.countDown();
            return createResponse("[]");
        });

        testedObject.registerForUpdate("someCorrelationId", () -> {
        }, SOME_UPDATE_INFO);
        dmaapRequestFiredLatch.await(1, TimeUnit.SECONDS);

        URI dmaapRequestUri = dmaapRequestCaptor.getValue().getURI();
        assertEquals(HOST, dmaapRequestUri.getHost());
        assertEquals(PORT, dmaapRequestUri.getPort());
        assertEquals(PROTOCOL, dmaapRequestUri.getScheme());
        assertEquals("/" + URI_PATH_PREFIX + "/" + EVENT_TOPIC_TEST + "/" + CONSUMER_GROUP + "/" + CONSUMER_ID,
                dmaapRequestUri.getPath());
    }

    @Test()
    public void pnfCorrelationIdIsFoundInHttpResponse_notifyAboutPnfReady() throws Exception {
        when(httpClientMock.execute(any(HttpGet.class)))
                .thenReturn(createResponse(String.format(JSON_EXAMPLE_WITH_PNF_CORRELATION_ID, PNF_CORRELATION_ID)));

        CountDownLatch pnfReadyListenerLatch = new CountDownLatch(1);
        testedObject.registerForUpdate(PNF_CORRELATION_ID, pnfReadyListenerLatch::countDown, SOME_UPDATE_INFO);

        pnfReadyListenerLatch.await(1, TimeUnit.SECONDS);
        assertEquals("Listener should be notified", 0, pnfReadyListenerLatch.getCount());
    }

    @Test
    public void pnfCorrelationIdIsFoundInHttpResponse_NotFoundInMap() throws Exception {
        when(httpClientMock.execute(any(HttpGet.class)))
                .thenReturn(createResponse(String.format(JSON_EXAMPLE_WITH_PNF_CORRELATION_ID, PNF_CORRELATION_ID)));

        CountDownLatch pnfReadyListenerLatch = new CountDownLatch(1);
        CountDownLatch otherPnfReadyListenerLatch = new CountDownLatch(1);
        testedObject.registerForUpdate(PNF_CORRELATION_ID, pnfReadyListenerLatch::countDown, SOME_UPDATE_INFO);
        testedObject.registerForUpdate(OTHER_CORRELATION_ID, otherPnfReadyListenerLatch::countDown, SOME_UPDATE_INFO);

        pnfReadyListenerLatch.await(1, TimeUnit.SECONDS);
        assertEquals("listener that did not received pnf ready notification should not be notified", 1,
                otherPnfReadyListenerLatch.getCount());
    }

    private HttpResponse createResponse(String json) {
        HttpEntity entity = new InputStreamEntity(new ByteArrayInputStream(json.getBytes()));
        ProtocolVersion protocolVersion = new ProtocolVersion("", 1, 1);
        HttpResponse response = new BasicHttpResponse(protocolVersion, 1, "");
        response.setEntity(entity);
        response.setStatusCode(200);
        return response;
    }
}
