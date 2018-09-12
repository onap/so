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

package org.onap.so.bpmn.infrastructure.pnf.dmaap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.so.bpmn.infrastructure.pnf.dmaap.PnfEventReadyDmaapClient.DmaapTopicListenerThread;
import org.springframework.core.env.Environment;
@RunWith(MockitoJUnitRunner.class)
public class PnfEventReadyDmaapClientTest {

    private static final String CORRELATION_ID = "corrTestId";
    private static final String CORRELATION_ID_NOT_FOUND_IN_MAP = "otherCorrId";
    private static final String JSON_EXAMPLE_WITH_CORRELATION_ID = "[\n"
            + "    {\n"
            + "        \"pnfRegistrationFields\" : {\n"
            + "        \"correlationId\" : \"%s\",\n"
            + "        \"value\" : \"value1\"\n"
            + "        }\n"
            + "    },\n"
            + "    {\n"
            + "        \"pnfRegistrationFields\" : {\n"
            + "        \"correlationId\" : \"corr\",\n"
            + "        \"value\" : \"value2\"\n"
            + "        }\n"
            + "    }\n"
            + "]";
    private static final String JSON_EXAMPLE_WITH_NO_CORRELATION_ID =
            "{\"pnfRegistrationFields\":{\"field\":\"value\"}}";

    private static final String HOST = "hostTest";
    private static final int PORT = 1234;
    private static final String PROTOCOL = "http";
    private static final String URI_PATH_PREFIX = "eventsForTesting";
    private static final String EVENT_TOPIC_TEST = "eventTopicTest";
    private static final String CONSUMER_ID = "consumerTestId";
    private static final String CONSUMER_GROUP = "consumerGroupTest";
    @Mock
    private Environment env;
    private PnfEventReadyDmaapClient testedObject;

    private DmaapTopicListenerThread testedObjectInnerClassThread;
    private HttpClient httpClientMock;
    private Runnable threadMockToNotifyCamundaFlow;
    private ScheduledThreadPoolExecutor executorMock;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        testedObject = new PnfEventReadyDmaapClient(env);
    	when(env.getProperty(eq("pnf.dmaap.port"), eq(Integer.class))).thenReturn(PORT);
    	when(env.getProperty(eq("pnf.dmaap.host"))).thenReturn(HOST);
        testedObject.setDmaapProtocol(PROTOCOL);
        testedObject.setDmaapUriPathPrefix(URI_PATH_PREFIX);
        testedObject.setDmaapTopicName(EVENT_TOPIC_TEST);
        testedObject.setConsumerId(CONSUMER_ID);
        testedObject.setConsumerGroup(CONSUMER_GROUP);
        testedObject.setDmaapClientDelayInSeconds(1);
        testedObject.init();
        testedObjectInnerClassThread = testedObject.new DmaapTopicListenerThread();
        httpClientMock = mock(HttpClient.class);
        threadMockToNotifyCamundaFlow = mock(Runnable.class);
        executorMock = mock(ScheduledThreadPoolExecutor.class);
        setPrivateField();
    }

    /**
     * Test run method, where the are following conditions:
     * <p> - DmaapThreadListener is running, flag is set to true
     * <p> - map is filled with one entry with the key that we get from response
     * <p> run method should invoke thread from map to notify camunda process, remove element from the map (map is
     * empty) and shutdown the executor because of empty map
     */
    @Test
    public void correlationIdIsFoundInHttpResponse_notifyAboutPnfReady()
            throws IOException {
        when(httpClientMock.execute(any(HttpGet.class))).
                thenReturn(createResponse(String.format(JSON_EXAMPLE_WITH_CORRELATION_ID, CORRELATION_ID)));
        testedObjectInnerClassThread.run();
        ArgumentCaptor<HttpGet> captor1 = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClientMock).execute(captor1.capture());
        assertThat(captor1.getValue().getURI()).hasHost(HOST).hasPort(PORT).hasScheme(PROTOCOL)
                .hasPath(
                        "/" + URI_PATH_PREFIX + "/" + EVENT_TOPIC_TEST + "/" + CONSUMER_GROUP + "/" + CONSUMER_ID + "");
        verify(threadMockToNotifyCamundaFlow).run();
        verify(executorMock).shutdown();
    }

    /**
     * Test run method, where the are following conditions:
     * <p> - DmaapThreadListener is running, flag is set to true
     * <p> - map is filled with one entry with the correlationId that does not match to correlationId
     * taken from http response. run method should not do anything with the map not run any thread to notify camunda
     * process
     */
    @Test
    public void correlationIdIsFoundInHttpResponse_NotFoundInMap()
            throws IOException {
        when(httpClientMock.execute(any(HttpGet.class))).
                thenReturn(createResponse(
                        String.format(JSON_EXAMPLE_WITH_CORRELATION_ID, CORRELATION_ID_NOT_FOUND_IN_MAP)));
        testedObjectInnerClassThread.run();
        verifyZeroInteractions(threadMockToNotifyCamundaFlow, executorMock);
    }

    /**
     * Test run method, where the are following conditions:
     * <p> - DmaapThreadListener is running, flag is set to true
     * <p> - map is filled with one entry with the correlationId but no correlation id is taken from HttpResponse
     * run method should not do anything with the map and not run any thread to notify camunda process
     */
    @Test
    public void correlationIdIsNotFoundInHttpResponse() throws IOException {
        when(httpClientMock.execute(any(HttpGet.class))).
                thenReturn(createResponse(JSON_EXAMPLE_WITH_NO_CORRELATION_ID));
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

        Field pnfCorrelationToThreadMapField = testedObject.getClass()
                .getDeclaredField("pnfCorrelationIdToThreadMap");
        pnfCorrelationToThreadMapField.setAccessible(true);
        Map<String, Runnable> pnfCorrelationToThreadMap = new ConcurrentHashMap<>();
        pnfCorrelationToThreadMap.put(CORRELATION_ID, threadMockToNotifyCamundaFlow);
        pnfCorrelationToThreadMapField.set(testedObject, pnfCorrelationToThreadMap);

        Field threadRunFlag = testedObject.getClass().getDeclaredField("dmaapThreadListenerIsRunning");
        threadRunFlag.setAccessible(true);
        threadRunFlag.set(testedObject, true);
        threadRunFlag.setAccessible(false);
    }

    private HttpResponse createResponse(String json) throws UnsupportedEncodingException {
        HttpEntity entity = new StringEntity(json);
        ProtocolVersion protocolVersion = new ProtocolVersion("", 1, 1);
        HttpResponse response = new BasicHttpResponse(protocolVersion, 1, "");
        response.setEntity(entity);
        response.setStatusCode(200);
        return response;
    }

}
