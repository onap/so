/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.configuration;

import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class HttpComponentsClientConfiguration {

    private final HttpClientConnectionConfiguration clientConnectionConfiguration;

    @Autowired
    public HttpComponentsClientConfiguration(final HttpClientConnectionConfiguration clientConnectionConfiguration) {
        this.clientConnectionConfiguration = clientConnectionConfiguration;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClients.custom().setConnectionManager(poolingHttpClientConnectionManager())
                .setDefaultRequestConfig(requestConfig()).evictExpiredConnections().evictIdleConnections(TimeValue
                        .of(clientConnectionConfiguration.getEvictIdleConnectionsTimeInSec(), TimeUnit.SECONDS))
                .build();
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(clientConnectionConfiguration.getMaxConnections());
        cm.setDefaultMaxPerRoute(clientConnectionConfiguration.getMaxConnectionsPerRoute());
        return cm;
    }

    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setResponseTimeout(
                        Timeout.ofMilliseconds(clientConnectionConfiguration.getSocketTimeOutInMiliSeconds()))
                .setConnectTimeout(
                        Timeout.ofMilliseconds(clientConnectionConfiguration.getConnectionTimeOutInMilliSeconds()))
                .setConnectionRequestTimeout(
                        Timeout.ofMilliseconds(clientConnectionConfiguration.getConnectionTimeOutInMilliSeconds()))
                .build();
    }
}
