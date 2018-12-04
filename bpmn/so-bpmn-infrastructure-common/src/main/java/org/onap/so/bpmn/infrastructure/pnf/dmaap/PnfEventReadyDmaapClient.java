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
    private HttpGet getRequest;
    private int topicListenerDelayInSeconds;
    private volatile ScheduledThreadPoolExecutor executor;
    private volatile boolean dmaapThreadListenerIsRunning;

    @Autowired
    public PnfEventReadyDmaapClient(Environment env) {
        httpClient = HttpClientBuilder.create().build();
        pnfCorrelationIdToThreadMap = new ConcurrentHashMap<>();
        topicListenerDelayInSeconds = env.getProperty("pnf.dmaap.topicListenerDelayInSeconds", Integer.class);
        executor = null;
        getRequest = new HttpGet(UriBuilder.fromUri(env.getProperty("pnf.dmaap.uriPathPrefix"))
                .scheme(env.getProperty("pnf.dmaap.protocol"))
                .host(env.getProperty("pnf.dmaap.host"))
                .port(env.getProperty("pnf.dmaap.port", Integer.class))
                .path(env.getProperty("pnf.dmaap.topicName"))
                .path(env.getProperty("pnf.dmaap.consumerGroup"))
                .path(env.getProperty("pnf.dmaap.consumerId")).build());
    }

    @Override
    public synchronized void registerForUpdate(String correlationId, Runnable informConsumer) {
        logger.debug("registering for pnf ready dmaap event for correlation id: {}", correlationId);
        pnfCorrelationIdToThreadMap.put(correlationId, informConsumer);
        if (!dmaapThreadListenerIsRunning) {
            startDmaapThreadListener();
        }
    }

    @Override
    public synchronized Runnable unregister(String correlationId) {
        logger.debug("unregistering from pnf ready dmaap event for correlation id: {}", correlationId);
        Runnable runnable = pnfCorrelationIdToThreadMap.remove(correlationId);
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
            executor.scheduleWithFixedDelay(new DmaapTopicListenerThread(), 0,
                    topicListenerDelayInSeconds, TimeUnit.SECONDS);
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
                logger.debug("dmaap listener starts listening pnf ready dmaap topic");
                HttpResponse response = httpClient.execute(getRequest);
                getCorrelationIdListFromResponse(response).forEach(this::informAboutPnfReadyIfCorrelationIdFound);
            } catch (IOException e) {
                logger.error("Exception caught during sending rest request to dmaap for listening event topic", e);
            }
            finally {
                getRequest.reset();
            }
        }

        private List<String> getCorrelationIdListFromResponse(HttpResponse response) throws IOException {
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (responseString != null) {
                    return JsonUtilForCorrelationId.parseJsonToGelAllCorrelationId(responseString);
                }
            }
            return Collections.emptyList();
        }

        private void informAboutPnfReadyIfCorrelationIdFound(String correlationId) {
            Runnable runnable = unregister(correlationId);
            if (runnable != null) {
                logger.debug("dmaap listener gets pnf ready event for correlationId: {}", correlationId);
                runnable.run();
            }
        }
    }

}
