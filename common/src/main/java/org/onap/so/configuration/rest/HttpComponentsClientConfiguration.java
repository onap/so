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

package org.onap.so.configuration.rest;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.hc.client5.http.config.RequestConfig;
import javax.net.ssl.SSLContext;

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
        /*
         * 1. setConnectionManager = Assigns HttpClientConnectionManager instance. 2. setMaxConnPerRoute = Assigns
         * maximum connection per route value. Please note this value can be overridden by the setConnectionManager(
         * org.apache.http.conn.HttpClientConnectionManager) method. 3. setDefaultRequestConfig = Assigns default
         * ConnectionConfig. Please note this value can be overridden by the setConnectionManager(
         * org.apache.http.conn.HttpClientConnectionManager) method. 4. evictExpiredConnections = Makes this instance of
         * HttpClient proactively evict expired connections from the connection pool using a background thread. One MUST
         * explicitly close HttpClient with Closeable.close() in order to stop and release the background thread. Please
         * note this method has no effect if the instance of HttpClient is configuted to use a shared connection
         * manager. Please note this method may not be used when the instance of HttpClient is created inside an EJB
         * container.
         */
        // return HttpClientBuilder.create().setConnectionManager(poolingHttpClientConnectionManager())
        // .setMaxConnPerRoute(clientConnectionConfiguration.getMaxConnectionsPerRoute())
        // .setMaxConnTotal(clientConnectionConfiguration.getMaxConnections())
        // .setDefaultRequestConfig(requestConfig()).setConnectionReuseStrategy((ConnectionReuseStrategy)
        // NoConnectionReuseStrategy.INSTANCE)
        // .evictExpiredConnections().evictIdleConnections(
        // clientConnectionConfiguration.getEvictIdleConnectionsTimeInSec())
        // .build();
        return HttpClients.custom().setConnectionManager(poolingHttpClientConnectionManager())
                .setDefaultRequestConfig(requestConfig())
                .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE).evictExpiredConnections()
                .evictIdleConnections(clientConnectionConfiguration.getEvictIdleConnectionsTimeInSec()).build();

    }


    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {

        // return new org.apache.http.impl.conn.PoolingHttpClientConnectionManager(
        // clientConnectionConfiguration.getTimeToLiveInMins(), TimeUnit.MINUTES);

        // Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        // .register("http", PlainConnectionSocketFactory.getSocketFactory())
        // .register("https", SSLConnectionSocketFactory.getSocketFactory())
        // .build();
        // HttpConnectionFactory< ManagedHttpClientConnection > connFactory = new ManagedHttpClientConnection();
        // TimeValue timeToLive = TimeValue.ofDays(clientConnectionConfiguration.getTimeToLiveInMins());
        // return new PoolingHttpClientConnectionManager(socketFactoryRegistry, PoolConcurrencyPolicy
        // poolConcurrencyPolicy,timeToLive, connFactory);

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(clientConnectionConfiguration.getMaxConnections());
        connectionManager.setDefaultMaxPerRoute(clientConnectionConfiguration.getMaxConnectionsPerRoute());
        return connectionManager;

    }

    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom().setResponseTimeout(clientConnectionConfiguration.getSocketTimeOutInMiliSeconds())
                .setConnectionRequestTimeout(clientConnectionConfiguration.getConnectionTimeOutInMilliSeconds())
                .build();

    }
}
