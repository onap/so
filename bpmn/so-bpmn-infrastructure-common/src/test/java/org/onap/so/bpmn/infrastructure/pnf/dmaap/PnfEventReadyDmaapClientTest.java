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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.infrastructure.pnf.dmaap.PnfEventReadyDmaapClient.DmaapTopicListenerThread;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class PnfEventReadyDmaapClientTest {

    private static final String PNF_CORRELATION_ID = "corrTestId";
    private static final String PNF_CORRELATION_ID_NOT_FOUND_IN_MAP = "otherCorrId";
    private static final String JSON_EXAMPLE_WITH_PNF_CORRELATION_ID = "[{\"correlationId\": \"%s\","
            + "\"value\":\"value1\"},{\"correlationId\": \"corr\",\"value\":\"value2\"}]";

    private static final String JSON_EXAMPLE_WITH_NO_PNF_CORRELATION_ID = "[{\"key1\":\"value1\"}]";

    private static final String HOST = "hostTest";
    private static final int PORT = 1234;
    private static final String PROTOCOL = "http";
    private static final String URI_PATH_PREFIX = "eventsForTesting";
    private static final String TOPIC_NAME = "PNF_READY_Test PNF_UPDATE_Test";
    private static final String CONSUMER_ID = "consumerId consumerIdUpdate";
    private static final String CONSUMER_GROUP = "consumerGroup consumerGroupUpdate";
    private static final int TOPIC_LISTENER_DELAY_IN_SECONDS = 5;

    @Mock
    private Environment env;
    private PnfEventReadyDmaapClient testedObject;

    private DmaapTopicListenerThread testedObjectInnerClassThread;
    private HttpClient httpClientMock;
    private Runnable threadMockToNotifyCamundaFlow;
    private ScheduledThreadPoolExecutor executorMock;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        when(env.getProperty(eq("pnf.dmaap.port"), eq(Integer.class))).thenReturn(PORT);
        when(env.getProperty(eq("pnf.dmaap.host"))).thenReturn(HOST);
        when(env.getProperty(eq("pnf.dmaap.protocol"))).thenReturn(PROTOCOL);
        when(env.getProperty(eq("pnf.dmaap.uriPathPrefix"))).thenReturn(URI_PATH_PREFIX);
        when(env.getProperty(eq("pnf.dmaap.topicName"))).thenReturn(TOPIC_NAME);
        when(env.getProperty(eq("pnf.dmaap.consumerId"))).thenReturn(CONSUMER_ID);
        when(env.getProperty(eq("pnf.dmaap.consumerGroup"))).thenReturn(CONSUMER_GROUP);
        when(env.getProperty(eq("pnf.dmaap.topicListenerDelayInSeconds"), eq(Integer.class)))
                .thenReturn(TOPIC_LISTENER_DELAY_IN_SECONDS);
        testedObject = new PnfEventReadyDmaapClient(env);
        testedObjectInnerClassThread = testedObject.new DmaapTopicListenerThread();
        httpClientMock = mock(HttpClient.class);
        threadMockToNotifyCamundaFlow = mock(Runnable.class);
        executorMock = mock(ScheduledThreadPoolExecutor.class);
        setPrivateField();
    }

    /**
     * Test run method, where the are following conditions:
     * <p>
     * - DmaapThreadListener is running, flag is set to true
     * <p>
     * - map is filled with one entry with the key that we get from response
     * <p>
     * run method should invoke thread from map to notify camunda process, remove element from the map (map is empty)
     * and shutdown the executor because of empty map
     */
    @Test
    public void pnfCorrelationIdIsFoundInHttpResponse_notifyAboutPnfUpdate() throws IOException {
        when(httpClientMock.execute(any(HttpGet.class)))
                .thenReturn(createResponse(String.format(JSON_EXAMPLE_WITH_PNF_CORRELATION_ID, PNF_CORRELATION_ID)));
        testedObjectInnerClassThread.run();
        ArgumentCaptor<HttpGet> captor1 = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClientMock).execute(captor1.capture());
        assertEquals(captor1.getValue().getURI().getHost(), HOST);
        assertEquals(captor1.getValue().getURI().getPort(), PORT);
        assertEquals(captor1.getValue().getURI().getScheme(), PROTOCOL);
        String[] cid = CONSUMER_ID.split("\\s");
        String consumerId_update = null;

        for (String c : cid) {
            if (c.matches("consumerIdUpdate")) {
                consumerId_update = c;
            }
        }

        String[] cg = CONSUMER_GROUP.split("\\s");
        String consumerGroup_pnf_update = null;
        for (String c : cg) {
            if (c.matches("consumerGroupUpdate")) {
                consumerGroup_pnf_update = c;
            }
        }

        String[] topic = TOPIC_NAME.split("\\s");
        String pnf_update = null;
        for (String t : topic) {
            if (t.matches("(.*)PNF_UPDATE(.*)")) {
                pnf_update = t;
            }
        }

        assertEquals(captor1.getValue().getURI().getPath(), "/" + URI_PATH_PREFIX + "/" + pnf_update + "/"
                + consumerGroup_pnf_update + "/" + consumerId_update + "");

        verify(threadMockToNotifyCamundaFlow).run();
        verify(executorMock).shutdown();
    }


    @Test
    public void pnfCorrelationIdIsFoundInHttpResponse_notifyAboutPnfReady() throws IOException {
        ArgumentCaptor<HttpGet> captor1 = ArgumentCaptor.forClass(HttpGet.class);
        when(httpClientMock.execute(any(HttpGet.class)))
                .thenReturn(createResponse_forReady(
                        String.format(JSON_EXAMPLE_WITH_PNF_CORRELATION_ID, PNF_CORRELATION_ID)))
                .thenReturn(createResponse(String.format(JSON_EXAMPLE_WITH_PNF_CORRELATION_ID, PNF_CORRELATION_ID)));
        testedObjectInnerClassThread.run();
        verify(httpClientMock, times(2)).execute(captor1.capture());
        assertEquals(captor1.getValue().getURI().getHost(), HOST);
        assertEquals(captor1.getValue().getURI().getPort(), PORT);
        assertEquals(captor1.getValue().getURI().getScheme(), PROTOCOL);
        String[] cid = CONSUMER_ID.split("\\s");
        String consumerId_ready = null;

        for (String c : cid) {
            if (c.matches("consumerId")) {
                consumerId_ready = c;
            }
        }

        String[] cg = CONSUMER_GROUP.split("\\s");
        String consumerGroup_pnf_ready = null;
        for (String c : cg) {
            if (c.matches("consumerGroup")) {
                consumerGroup_pnf_ready = c;
            }
        }
        String[] topic = TOPIC_NAME.split("\\s");
        String pnf_ready = null;
        for (String t : topic) {
            if (t.matches("(.*)PNF_READY(.*)")) {
                pnf_ready = t;
                assertEquals(captor1.getValue().getURI().getPath(), "/" + URI_PATH_PREFIX + "/" + pnf_ready + "/"
                        + consumerGroup_pnf_ready + "/" + consumerId_ready + "");
            }
        }
        verify(threadMockToNotifyCamundaFlow).run();
        verify(executorMock).shutdown();
    }


    /**
     * Test run method, where the are following conditions:
     * <p>
     * - DmaapThreadListener is running, flag is set to true
     * <p>
     * - map is filled with one entry with the pnfCorrelationId that does not match to pnfCorrelationId taken from http
     * response. run method should not do anything with the map not run any thread to notify camunda process
     */
    @Test
    public void pnfCorrelationIdIsFoundInHttpResponse_NotFoundInMap() throws IOException {
        when(httpClientMock.execute(any(HttpGet.class))).thenReturn(createResponse(
                String.format(JSON_EXAMPLE_WITH_PNF_CORRELATION_ID, PNF_CORRELATION_ID_NOT_FOUND_IN_MAP)));
        testedObjectInnerClassThread.run();
        verifyZeroInteractions(threadMockToNotifyCamundaFlow, executorMock);
    }

    /**
     * Test run method, where the are following conditions:
     * <p>
     * - DmaapThreadListener is running, flag is set to true
     * <p>
     * - map is filled with one entry with the pnfCorrelationId but no correlation id is taken from HttpResponse run
     * method should not do anything with the map and not run any thread to notify camunda process
     */
    @Test
    public void pnfCorrelationIdIsNotFoundInHttpResponse() throws IOException {
        when(httpClientMock.execute(any(HttpGet.class)))
                .thenReturn(createResponse(JSON_EXAMPLE_WITH_NO_PNF_CORRELATION_ID));
        testedObjectInnerClassThread.run();
        verifyZeroInteractions(threadMockToNotifyCamundaFlow, executorMock);
    }

    private void setPrivateField() throws NoSuchFieldException, IllegalAccessException {
        Field httpClientField = testedObject.getClass().getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(testedObject, httpClientMock);
        httpClientField.setAccessible(false);

        Field executorField = testedObject.getClass().getDeclaredField("executor");
        executorField.setAccessible(true);
        executorField.set(testedObject, executorMock);
        executorField.setAccessible(false);

        Field pnfCorrelationToThreadMapField = testedObject.getClass().getDeclaredField("pnfCorrelationIdToThreadMap");
        pnfCorrelationToThreadMapField.setAccessible(true);
        Map<String, Runnable> pnfCorrelationToThreadMap = new ConcurrentHashMap<>();
        pnfCorrelationToThreadMap.put(PNF_CORRELATION_ID, threadMockToNotifyCamundaFlow);
        pnfCorrelationToThreadMapField.set(testedObject, pnfCorrelationToThreadMap);

        Field threadRunFlag = testedObject.getClass().getDeclaredField("dmaapThreadListenerIsRunning");
        threadRunFlag.setAccessible(true);
        threadRunFlag.set(testedObject, true);
        threadRunFlag.setAccessible(false);
    }

    private HttpResponse createResponse(String json) {
        HttpEntity entity = new InputStreamEntity(new ByteArrayInputStream(json.getBytes()));
        ProtocolVersion protocolVersion = new ProtocolVersion("", 1, 1);
        HttpResponse response = new BasicHttpResponse(protocolVersion, 1, "");
        response.setEntity(entity);
        response.setStatusCode(200);
        return response;
    }

    private HttpResponse createResponse_forReady(String json) {
        HttpEntity entity = new InputStreamEntity(new ByteArrayInputStream(json.getBytes()));
        ProtocolVersion protocolVersion = new ProtocolVersion("", 1, 1);
        HttpResponse response = new BasicHttpResponse(protocolVersion, 1, "");
        response.setEntity(entity);
        response.setStatusCode(500);
        return response;
    }

}


