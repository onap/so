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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * Allow user to configure {@link org.apache.http.client.HttpClient}
 *
 * @author waqas.ikram@est.tech
 */
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
        return HttpClientBuilder.create().setConnectionManager(poolingHttpClientConnectionManager())
                .setMaxConnPerRoute(clientConnectionConfiguration.getMaxConnectionsPerRoute())
                .setMaxConnTotal(clientConnectionConfiguration.getMaxConnections())
                .setDefaultRequestConfig(requestConfig()).setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE)
                .evictExpiredConnections().evictIdleConnections(
                        clientConnectionConfiguration.getEvictIdleConnectionsTimeInSec(), TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        return new PoolingHttpClientConnectionManager(clientConnectionConfiguration.getTimeToLiveInMins(),
                TimeUnit.MINUTES);
    }

    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom().setSocketTimeout(clientConnectionConfiguration.getSocketTimeOutInMiliSeconds())
                .setConnectTimeout(clientConnectionConfiguration.getConnectionTimeOutInMilliSeconds()).build();
    }
}
