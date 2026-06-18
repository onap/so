/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
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
package org.onap.so.heatbridge.openstack.connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.openstack4j.core.transport.ExecutionOptions;
import org.openstack4j.core.transport.HttpResponse;
import org.openstack4j.core.transport.ObjectMapperSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientResponse implements HttpResponse {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientResponse.class);

    private final CloseableHttpResponse response;
    private final CloseableHttpClient client;

    HttpClientResponse(CloseableHttpResponse response, CloseableHttpClient client) {
        this.response = response;
        this.client = client;
    }

    @Override
    public int getStatus() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public String getStatusMessage() {
        return response.getStatusLine().getReasonPhrase();
    }

    @Override
    public InputStream getInputStream() {
        try {
            HttpEntity entity = response.getEntity();
            return entity != null ? entity.getContent() : null;
        } catch (IOException e) {
            LOG.error("Error getting response input stream", e);
            return null;
        }
    }

    @Override
    public String getContentType() {
        HttpEntity entity = response.getEntity();
        if (entity != null && entity.getContentType() != null) {
            return entity.getContentType().getValue();
        }
        return null;
    }

    @Override
    public String header(String name) {
        Header header = response.getFirstHeader(name);
        return header != null ? header.getValue() : null;
    }

    @Override
    public Map<String, String> headers() {
        Map<String, String> headers = new HashMap<>();
        for (Header header : response.getAllHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }

    @Override
    public <T> T getEntity(Class<T> type) {
        return getEntity(type, null);
    }

    @Override
    public <T> T getEntity(Class<T> type, ExecutionOptions<T> options) {
        try {
            InputStream is = getInputStream();
            if (is == null) {
                return null;
            }
            return ObjectMapperSingleton.getContext(type).readValue(is, type);
        } catch (IOException e) {
            LOG.error("Error deserializing response entity to {}", type.getName(), e);
            return null;
        }
    }

    @Override
    public <T> T readEntity(Class<T> type) {
        return getEntity(type, null);
    }

    @Override
    public void close() throws IOException {
        try {
            response.close();
        } finally {
            client.close();
        }
    }
}
