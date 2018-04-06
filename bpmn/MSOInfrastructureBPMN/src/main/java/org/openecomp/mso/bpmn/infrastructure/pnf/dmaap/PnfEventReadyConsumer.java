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
import javax.ws.rs.core.UriBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class PnfEventReadyConsumer {

    private static final String JSON_PATH_CORRELATION_ID = "$.pnfRegistrationFields.correlationId";
    private HttpClient httpClient;

    private String dmaapHost;
    private int dmaapPort;
    private String dmaapProtocol;
    private String dmaapUriPathPrefix;
    private String dmaapTopicName;
    private String consumerId;
    private String consumerGroup;

    public PnfEventReadyConsumer() {
        httpClient = HttpClientBuilder.create().build();
    }

    public void notifyWhenPnfReady(String correlationId)
            throws IOException {
        HttpGet getRequest = new HttpGet(buildURI(consumerGroup, consumerId));
        HttpResponse response = httpClient.execute(getRequest);
        checkIfResponseIsAccepted(response, correlationId);
    }

    private boolean checkIfResponseIsAccepted(HttpResponse response, String correlationId) {
        // TODO parse response if contains proper correlationId
        return false;
    }

    private URI buildURI(String consumerGroup, String consumerId) {
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

}
