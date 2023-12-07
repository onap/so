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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.infrastructure.pnf.dmaap.PnfEventReadyDmaapClient.DmaapTopicListenerThread;
import org.onap.so.client.kafka.KafkaConsumerImpl;
import org.springframework.core.env.Environment;


@RunWith(MockitoJUnitRunner.class)
public class PnfEventReadyDmaapClientTest {
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String PNF_CORRELATION_ID = "corrTestId";
    private static final String PNF_CORRELATION_ID_NOT_FOUND_IN_MAP = "otherCorrId";
    private static final String[] JSON_EXAMPLE_WITH_PNF_CORRELATION_ID =
            {"{\"correlationId\": \"%s\"," + "\"value\":\"value1\"}",
                    "{\"correlationId\": \"corr\",\"value\":\"value2\"}"};

    private static final String JSON_EXAMPLE_WITH_NO_PNF_CORRELATION_ID = "{\"key1\":\"value1\"}";
    private static final String TOPIC_NAME = "unauthenticated.PNF_READY";
    private static final String TOPIC_NAME_UPDATE = "unauthenticated.PNF_UPDATE";
    private static final String CONSUMER_ID = "so-bpmn-infra-pnfready";
    private static final String CONSUMER_ID_UPDATE = "so-bpmn-infra-pnfupdate";
    private static final String CONSUMER_GROUP = "so-consumer";
    private static final int TOPIC_LISTENER_DELAY_IN_SECONDS = 5;

    @Mock
    private Environment env;
    private PnfEventReadyDmaapClient testedObject;

    private DmaapTopicListenerThread testedObjectInnerClassThread;
    private KafkaConsumerImpl kafkaConsumerMock;
    private Runnable threadMockToNotifyCamundaFlow;
    private ScheduledThreadPoolExecutor executorMock;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException, IOException {
        when(env.getProperty(eq("pnf.kafka.kafkaBootstrapServers"))).thenReturn(KAFKA_BOOTSTRAP_SERVERS);
        when(env.getProperty(eq("pnf.kafka.pnfReadyTopicName"))).thenReturn(TOPIC_NAME);
        when(env.getProperty(eq("pnf.kafka.pnfUpdateTopicName"))).thenReturn(TOPIC_NAME_UPDATE);
        when(env.getProperty(eq("pnf.kafka.consumerId"))).thenReturn(CONSUMER_ID);
        when(env.getProperty(eq("pnf.kafka.consumerIdUpdate"))).thenReturn(CONSUMER_ID_UPDATE);
        when(env.getProperty(eq("pnf.kafka.consumerGroup"))).thenReturn(CONSUMER_GROUP);
        when(env.getProperty(eq("pnf.dmaap.topicListenerDelayInSeconds"), eq(Integer.class)))
                .thenReturn(TOPIC_LISTENER_DELAY_IN_SECONDS);
        testedObject = new PnfEventReadyDmaapClient(env);
        testedObjectInnerClassThread = testedObject.new DmaapTopicListenerThread();
        kafkaConsumerMock = mock(KafkaConsumerImpl.class);
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
        when(kafkaConsumerMock.get(any(String.class), any(String.class), any(String.class)))
                .thenReturn(Arrays.asList(String.format(JSON_EXAMPLE_WITH_PNF_CORRELATION_ID[0], PNF_CORRELATION_ID),
                        JSON_EXAMPLE_WITH_PNF_CORRELATION_ID[1]));
        testedObjectInnerClassThread.run();
        verify(kafkaConsumerMock).get(TOPIC_NAME_UPDATE, CONSUMER_GROUP, CONSUMER_ID_UPDATE);
        verify(threadMockToNotifyCamundaFlow).run();
        verify(executorMock).shutdown();
    }


    @Test
    public void pnfCorrelationIdIsFoundInHttpResponse_notifyAboutPnfReady() throws IOException {
        when(kafkaConsumerMock.get(any(String.class), any(String.class), any(String.class)))
                .thenReturn(Collections.emptyList())
                .thenReturn(Arrays.asList(String.format(JSON_EXAMPLE_WITH_PNF_CORRELATION_ID[0], PNF_CORRELATION_ID),
                        JSON_EXAMPLE_WITH_PNF_CORRELATION_ID[1]));
        testedObjectInnerClassThread.run();
        verify(kafkaConsumerMock).get(TOPIC_NAME, CONSUMER_GROUP, CONSUMER_ID);

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
        when(kafkaConsumerMock.get(any(String.class), any(String.class), any(String.class))).thenReturn(Arrays.asList(
                String.format(JSON_EXAMPLE_WITH_PNF_CORRELATION_ID[0], PNF_CORRELATION_ID_NOT_FOUND_IN_MAP),
                JSON_EXAMPLE_WITH_PNF_CORRELATION_ID[1]));
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
        when(kafkaConsumerMock.get(any(String.class), any(String.class), any(String.class)))
                .thenReturn(Arrays.asList(JSON_EXAMPLE_WITH_NO_PNF_CORRELATION_ID));
        testedObjectInnerClassThread.run();
        verifyZeroInteractions(threadMockToNotifyCamundaFlow, executorMock);
    }

    private void setPrivateField() throws NoSuchFieldException, IllegalAccessException {
        Field consumerForPnfReadyField = testedObject.getClass().getDeclaredField("consumerForPnfReady");
        consumerForPnfReadyField.setAccessible(true);
        consumerForPnfReadyField.set(testedObject, kafkaConsumerMock);
        consumerForPnfReadyField.setAccessible(false);

        Field consumerForPnfUpdateField = testedObject.getClass().getDeclaredField("consumerForPnfUpdate");
        consumerForPnfUpdateField.setAccessible(true);
        consumerForPnfUpdateField.set(testedObject, kafkaConsumerMock);
        consumerForPnfUpdateField.setAccessible(false);

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

}
