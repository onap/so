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
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.logger.MsoLogger.Catalog;

public class PnfEventReadyDmaapClient implements DmaapClient {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(Catalog.GENERAL);

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

    public void init() {
        httpClient = HttpClientBuilder.create().build();
        pnfCorrelationIdToThreadMap = new ConcurrentHashMap<>();
        dmaapHost = PropertyConfiguration.getInstance().getProperties(PropertyConfiguration.MSO_BPMN_URN_PROPERTIES)
                .get("dmaapHost");
        dmaapPort = Integer.parseInt(PropertyConfiguration.getInstance()
                .getProperties(PropertyConfiguration.MSO_BPMN_URN_PROPERTIES).get("dmaapPort"));
        executor = null;
        getRequest = new HttpGet(buildURI());
    }

    @Override
    public synchronized void registerForUpdate(String correlationId, Runnable informConsumer) {
        LOGGER.debug("registering for pnf ready dmaap event for correlation id: " + correlationId);
        pnfCorrelationIdToThreadMap.put(correlationId, informConsumer);
        if (!dmaapThreadListenerIsRunning) {
            startDmaapThreadListener();
        }
    }

    @Override
    public synchronized Runnable unregister(String correlationId) {
        LOGGER.debug("unregistering from pnf ready dmaap event for correlation id: " + correlationId);
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
                getCorrelationIdListFromResponse(response).forEach(this::informAboutPnfReadyIfCorrelationIdFound);
            } catch (IOException e) {
                LOGGER.error("Exception caught during sending rest request to dmaap for listening event topic", e);
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

        private synchronized void informAboutPnfReadyIfCorrelationIdFound(String correlationId) {
            Runnable runnable = unregister(correlationId);
            if (runnable != null) {
                LOGGER.debug("pnf ready event got from dmaap for correlationId: " + correlationId);
                runnable.run();
            }
        }
    }

}
