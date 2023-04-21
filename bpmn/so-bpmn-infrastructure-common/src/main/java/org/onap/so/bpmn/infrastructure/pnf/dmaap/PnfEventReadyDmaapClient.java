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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PnfEventReadyDmaapClient implements DmaapClient {
    private static final Logger logger = LoggerFactory.getLogger(PnfEventReadyDmaapClient.class);
    private HttpClient httpClient;
    private Map<String, Runnable> pnfCorrelationIdToThreadMap;
    private HttpGet getRequestForpnfReady;
    private HttpGet getRequestForPnfUpdate;
    private int topicListenerDelayInSeconds;
    private volatile ScheduledThreadPoolExecutor executor;
    private volatile boolean dmaapThreadListenerIsRunning;



    @Autowired
    public PnfEventReadyDmaapClient(Environment env) {
        httpClient = HttpClientBuilder.create().build();
        pnfCorrelationIdToThreadMap = new ConcurrentHashMap<>();
        topicListenerDelayInSeconds = env.getProperty("pnf.dmaap.topicListenerDelayInSeconds", Integer.class);
        executor = null;
        getRequestForpnfReady = new HttpGet(UriBuilder.fromUri(env.getProperty("pnf.dmaap.uriPathPrefix"))
                .scheme(env.getProperty("pnf.dmaap.protocol")).host(env.getProperty("pnf.dmaap.host"))
                .port(env.getProperty("pnf.dmaap.port", Integer.class))
                .path(env.getProperty("pnf.dmaap.pnfReadyTopicName")).path(env.getProperty("pnf.dmaap.consumerGroup"))
                .path(env.getProperty("pnf.dmaap.consumerId")).build());
        getRequestForPnfUpdate = new HttpGet(UriBuilder.fromUri(env.getProperty("pnf.dmaap.uriPathPrefix"))
                .scheme(env.getProperty("pnf.dmaap.protocol")).host(env.getProperty("pnf.dmaap.host"))
                .port(env.getProperty("pnf.dmaap.port", Integer.class))
                .path(env.getProperty("pnf.dmaap.pnfUpdateTopicName")).path(env.getProperty("pnf.dmaap.consumerGroup"))
                .path(env.getProperty("pnf.dmaap.consumerIdUpdate")).build());
    }


    @Override
    public synchronized void registerForUpdate(String pnfCorrelationId, Runnable informConsumer) {
        logger.debug("registering for pnf ready dmaap event for pnf correlation id: {}", pnfCorrelationId);
        pnfCorrelationIdToThreadMap.put(pnfCorrelationId, informConsumer);
        if (!dmaapThreadListenerIsRunning) {
            startDmaapThreadListener();
        }
    }

    @Override
    public synchronized Runnable unregister(String pnfCorrelationId) {
        logger.debug("unregistering from pnf ready dmaap event for pnf correlation id: {}", pnfCorrelationId);
        Runnable runnable = pnfCorrelationIdToThreadMap.remove(pnfCorrelationId);
        if (pnfCorrelationIdToThreadMap.isEmpty()) {
            stopDmaapThreadListener();
        }
        return runnable;
    }

    private synchronized void startDmaapThreadListener() {
        if (!dmaapThreadListenerIsRunning) {
            executor = new ScheduledThreadPoolExecutor(1);
            executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            executor.scheduleWithFixedDelay(new DmaapTopicListenerThread(), 0, topicListenerDelayInSeconds,
                    TimeUnit.SECONDS);
            dmaapThreadListenerIsRunning = true;
        }
    }

    private synchronized void stopDmaapThreadListener() {
        if (dmaapThreadListenerIsRunning) {
            executor.shutdown();
            dmaapThreadListenerIsRunning = false;
            executor = null;
        }
    }

    class DmaapTopicListenerThread implements Runnable {
        @Override
        public void run() {
            try {
                HttpResponse response;
                response = httpClient.execute(getRequestForPnfUpdate);
                List<String> pnfUpdateResponse = getPnfCorrelationIdListFromResponse(response);
                if (pnfUpdateResponse.isEmpty()) {
                    response = httpClient.execute(getRequestForpnfReady);
                    getPnfCorrelationIdListFromResponse(response)
                            .forEach(this::informAboutPnfReadyIfPnfCorrelationIdFound);
                } else {
                    pnfUpdateResponse.forEach(this::informAboutPnfReadyIfPnfCorrelationIdFound);
                }
            } catch (IOException e) {
                logger.error("Exception caught during sending rest request to dmaap for listening event topic", e);
            } finally {
                getRequestForpnfReady.reset();
                getRequestForPnfUpdate.reset();
            }
        }

        private List<String> getPnfCorrelationIdListFromResponse(HttpResponse response) throws IOException {
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (responseString != null) {
                    return JsonUtilForPnfCorrelationId.parseJsonToGelAllPnfCorrelationId(responseString);
                }
            }
            return Collections.emptyList();
        }

        private void informAboutPnfReadyIfPnfCorrelationIdFound(String pnfCorrelationId) {
            Runnable runnable = unregister(pnfCorrelationId);
            if (runnable != null) {
                logger.debug("dmaap listener gets pnf ready event for pnfCorrelationId: {}", pnfCorrelationId);
                runnable.run();
            }
        }
    }
}

