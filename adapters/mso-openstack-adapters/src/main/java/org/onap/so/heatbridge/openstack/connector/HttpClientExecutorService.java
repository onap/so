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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openstack4j.core.transport.Config;
import org.openstack4j.core.transport.HttpExecutorService;
import org.openstack4j.core.transport.HttpMethod;
import org.openstack4j.core.transport.HttpRequest;
import org.openstack4j.core.transport.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientExecutorService implements HttpExecutorService {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientExecutorService.class);
    private static final String NAME = "Apache HttpClient Connector";

    @Override
    public <R> HttpResponse execute(HttpRequest<R> request) {
        CloseableHttpClient client = null;
        try {
            URI uri = buildUri(request);
            HttpRequestBase httpRequest = createHttpRequest(request.getMethod(), uri);
            applyHeaders(httpRequest, request);
            applyBody(httpRequest, request);

            client = buildClient(request.getConfig());
            CloseableHttpResponse response = client.execute(httpRequest);
            return new HttpClientResponse(response, client);
        } catch (IOException | URISyntaxException e) {
            // Clean up client if request failed before response creation
            if (client != null) {
                try {
                    client.close();
                } catch (IOException closeException) {
                    LOG.warn("Error closing HttpClient after failed request", closeException);
                }
            }
            LOG.error("Error during execution: {}", e.getMessage(), e);
            throw new RuntimeException("Error during HTTP execution", e);
        }
    }

    @Override
    public String getExecutorDisplayName() {
        return NAME;
    }

    private <R> URI buildUri(HttpRequest<R> request) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(request.getEndpoint() + request.getPath());
        if (request.hasQueryParams()) {
            for (Map.Entry<String, List<Object>> entry : request.getQueryParams().entrySet()) {
                for (Object value : entry.getValue()) {
                    builder.addParameter(entry.getKey(), value != null ? value.toString() : null);
                }
            }
        }
        return builder.build();
    }

    private HttpRequestBase createHttpRequest(HttpMethod method, URI uri) {
        switch (method) {
            case GET:
                return new HttpGet(uri);
            case POST:
                return new HttpPost(uri);
            case PUT:
                return new HttpPut(uri);
            case PATCH:
                return new HttpPatch(uri);
            case DELETE:
                return new HttpDelete(uri);
            case HEAD:
                return new HttpHead(uri);
            default:
                throw new UnsupportedOperationException("HTTP method not supported: " + method);
        }
    }

    private <R> void applyHeaders(HttpRequestBase httpRequest, HttpRequest<R> request) {
        if (request.hasHeaders()) {
            for (Map.Entry<String, Object> entry : request.getHeaders().entrySet()) {
                httpRequest.addHeader(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
            }
        }
    }

    private <R> void applyBody(HttpRequestBase httpRequest, HttpRequest<R> request) {
        if (request.hasJson() && httpRequest instanceof org.apache.http.client.methods.HttpEntityEnclosingRequestBase) {
            org.apache.http.client.methods.HttpEntityEnclosingRequestBase entityRequest =
                    (org.apache.http.client.methods.HttpEntityEnclosingRequestBase) httpRequest;
            ContentType contentType =
                    request.getContentType() != null ? ContentType.parse(request.getContentType().toString())
                            : ContentType.APPLICATION_JSON;
            entityRequest.setEntity(new StringEntity(request.getJson(), contentType));
        }
    }

    private CloseableHttpClient buildClient(Config config) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (config != null) {
            RequestConfig.Builder reqConfig = RequestConfig.custom();
            if (config.getConnectTimeout() > 0) {
                reqConfig.setConnectTimeout(config.getConnectTimeout());
            }
            if (config.getReadTimeout() > 0) {
                reqConfig.setSocketTimeout(config.getReadTimeout());
            }
            builder.setDefaultRequestConfig(reqConfig.build());

            if (config.getSslContext() != null) {
                builder.setSSLContext(config.getSslContext());
            }
            if (config.getHostNameVerifier() != null) {
                builder.setSSLHostnameVerifier(config.getHostNameVerifier());
            }
        }
        return builder.build();
    }
}
