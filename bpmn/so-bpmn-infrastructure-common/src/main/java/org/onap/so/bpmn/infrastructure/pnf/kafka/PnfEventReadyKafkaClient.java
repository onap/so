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
package org.onap.so.bpmn.infrastructure.pnf.kafka;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.onap.so.client.kafka.KafkaConsumerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PnfEventReadyKafkaClient implements KafkaClient {
    private static final Logger logger = LoggerFactory.getLogger(PnfEventReadyKafkaClient.class);
    private Map<String, Runnable> pnfCorrelationIdToThreadMap;
    private int topicListenerDelayInSeconds;
    private volatile ScheduledThreadPoolExecutor executor;
    private volatile boolean kafkaThreadListenerIsRunning;
    private KafkaConsumerImpl consumerForPnfReady;
    private KafkaConsumerImpl consumerForPnfUpdate;
    private String pnfReadyTopic;
    private String pnfUpdateTopic;
    private String consumerGroup;
    private String consumerId;
    private String consumerIdUpdate;



    @Autowired
    public PnfEventReadyKafkaClient(Environment env) throws IOException {
        pnfCorrelationIdToThreadMap = new ConcurrentHashMap<>();
        topicListenerDelayInSeconds = env.getProperty("pnf.kafka.topicListenerDelayInSeconds", Integer.class);
        executor = null;
        try {
            consumerForPnfReady = new KafkaConsumerImpl(env.getProperty("pnf.kafka.kafkaBootstrapServers"));
            consumerForPnfUpdate = new KafkaConsumerImpl(env.getProperty("pnf.kafka.kafkaBootstrapServers"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        pnfReadyTopic = env.getProperty("pnf.kafka.pnfReadyTopicName");
        pnfUpdateTopic = env.getProperty("pnf.kafka.pnfUpdateTopicName");
        consumerGroup = env.getProperty("pnf.kafka.consumerGroup");
        consumerId = env.getProperty("pnf.kafka.consumerId");
        consumerIdUpdate = env.getProperty("pnf.kafka.consumerIdUpdate");
    }


    @Override
    public synchronized void registerForUpdate(String pnfCorrelationId, Runnable informConsumer) {
        logger.debug("registering for pnf ready kafka event for pnf correlation id: {}", pnfCorrelationId);
        pnfCorrelationIdToThreadMap.put(pnfCorrelationId, informConsumer);
        if (!kafkaThreadListenerIsRunning) {
            startKafkaThreadListener();
        }
    }

    @Override
    public synchronized Runnable unregister(String pnfCorrelationId) {
        logger.debug("unregistering from pnf ready kafka event for pnf correlation id: {}", pnfCorrelationId);
        Runnable runnable = pnfCorrelationIdToThreadMap.remove(pnfCorrelationId);
        if (pnfCorrelationIdToThreadMap.isEmpty()) {
            consumerForPnfUpdate.close();
            consumerForPnfReady.close();
            stopKafkaThreadListener();
        }
        return runnable;
    }

    private synchronized void startKafkaThreadListener() {
        if (!kafkaThreadListenerIsRunning) {
            executor = new ScheduledThreadPoolExecutor(1);
            executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            executor.scheduleWithFixedDelay(new KafkaTopicListenerThread(), 0, topicListenerDelayInSeconds,
                    TimeUnit.SECONDS);
            kafkaThreadListenerIsRunning = true;
        }
    }

    private synchronized void stopKafkaThreadListener() {
        if (kafkaThreadListenerIsRunning) {
            executor.shutdown();
            kafkaThreadListenerIsRunning = false;
            executor = null;
        }
    }

    class KafkaTopicListenerThread implements Runnable {
        @Override
        public void run() {
            try {
                List<String> response;
                System.out.println(pnfUpdateTopic + "   " + consumerGroup);
                response = consumerForPnfUpdate.get(pnfUpdateTopic, consumerGroup, consumerIdUpdate);
                if (response.isEmpty()) {
                    response = consumerForPnfReady.get(pnfReadyTopic, consumerGroup, consumerId);
                    getPnfCorrelationIdListFromResponse(response)
                            .forEach(this::informAboutPnfReadyIfPnfCorrelationIdFound);
                } else {
                    getPnfCorrelationIdListFromResponse(response)
                            .forEach(this::informAboutPnfReadyIfPnfCorrelationIdFound);
                }
            } catch (IOException e) {
                logger.error("Exception caught during sending rest request to kafka for listening event topic", e);
            }
        }

        private List<String> getPnfCorrelationIdListFromResponse(List<String> response) {
            if (response != null) {
                return JsonUtilForPnfCorrelationId.parseJsonToGelAllPnfCorrelationId(response);
            }
            return Collections.emptyList();
        }

        private void informAboutPnfReadyIfPnfCorrelationIdFound(String pnfCorrelationId) {
            Runnable runnable = unregister(pnfCorrelationId);
            if (runnable != null) {
                logger.debug("kafka listener gets pnf ready event for pnfCorrelationId: {}", pnfCorrelationId);
                runnable.run();
            }
        }
    }
}

