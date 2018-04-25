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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openecomp.mso.jsonpath.JsonPathUtil;
import org.openecomp.mso.logger.MsoLogger;

public class PnfEventReadyDmaapClient implements DmaapClient {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

    private static final String JSON_PATH_CORRELATION_ID = "$.pnfRegistrationFields.correlationId";
    private HttpClient httpClient;
    private String dmaapHost;
    private int dmaapPort;
    private String dmaapProtocol;
    private String dmaapUriPathPrefix;
    private String dmaapTopicName;
    private String consumerId;
    private String consumerGroup;
    private Map<String, Runnable> pnfCorrelationIdToThreadMap;
    private HttpGet getRequest;
    private ScheduledExecutorService executor;
    private int dmaapClientDelayInSeconds;
    private volatile boolean dmaapThreadListenerIsRunning;

    public PnfEventReadyDmaapClient() {
        httpClient = HttpClientBuilder.create().build();
        pnfCorrelationIdToThreadMap = new ConcurrentHashMap<>();
        executor = null;
    }

    public void init() {
        getRequest = new HttpGet(buildURI());
    }

    @Override
    public synchronized void registerForUpdate(String correlationId, Runnable informConsumer) {
        pnfCorrelationIdToThreadMap.put(correlationId, informConsumer);
        if (!dmaapThreadListenerIsRunning) {
            startDmaapThreadListener();
        }
    }

    @Override
    public synchronized Runnable unregister(String correlationId) {
        Runnable runnable = pnfCorrelationIdToThreadMap.remove(correlationId);
        if (pnfCorrelationIdToThreadMap.isEmpty()) {
            stopDmaapThreadListener();
        }
        return runnable;
    }

    private synchronized void startDmaapThreadListener() {
        if (!dmaapThreadListenerIsRunning) {
            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleWithFixedDelay(new DmaapTopicListenerThread(), 0,
                    dmaapClientDelayInSeconds, TimeUnit.SECONDS);
            dmaapThreadListenerIsRunning = true;
        }
    }

    private synchronized void stopDmaapThreadListener() {
        if (dmaapThreadListenerIsRunning) {
            executor.shutdownNow();
            dmaapThreadListenerIsRunning = false;
            executor = null;
        }
    }

    private URI buildURI() {
        return UriBuilder.fromUri(dmaapUriPathPrefix)
                .scheme(dmaapProtocol)
                .host(dmaapHost)
                .port(dmaapPort).path(dmaapTopicName)
                .path(consumerGroup).path(consumerId).build();
    }

    public void setDmaapHost(String dmaapHost) {
        this.dmaapHost = dmaapHost;
    }

    public void setDmaapPort(int dmaapPort) {
        this.dmaapPort = dmaapPort;
    }

    public void setDmaapProtocol(String dmaapProtocol) {
        this.dmaapProtocol = dmaapProtocol;
    }

    public void setDmaapUriPathPrefix(String dmaapUriPathPrefix) {
        this.dmaapUriPathPrefix = dmaapUriPathPrefix;
    }

    public void setDmaapTopicName(String dmaapTopicName) {
        this.dmaapTopicName = dmaapTopicName;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public void setDmaapClientDelayInSeconds(int dmaapClientDelayInSeconds) {
        this.dmaapClientDelayInSeconds = dmaapClientDelayInSeconds;
    }

    class DmaapTopicListenerThread implements Runnable {

        @Override
        public void run() {
            try {
                HttpResponse response = httpClient.execute(getRequest);
                getCorrelationIdFromResponse(response).ifPresent(this::informAboutPnfReadyIfCorrelationIdFound);
            } catch (IOException e) {
                LOGGER.error("Exception caught during sending rest request to dmaap for listening event topic", e);
            }
        }

        private Optional<String> getCorrelationIdFromResponse(HttpResponse response) throws IOException {
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (responseString != null) {
                    return JsonPathUtil.getInstance().locateResult(responseString, JSON_PATH_CORRELATION_ID);
                }
            }
            return Optional.empty();
        }

        private synchronized void informAboutPnfReadyIfCorrelationIdFound(String correlationId) {
            Runnable runnable = unregister(correlationId);
            if (runnable != null) {
                runnable.run();
            }
        }
    }

}
