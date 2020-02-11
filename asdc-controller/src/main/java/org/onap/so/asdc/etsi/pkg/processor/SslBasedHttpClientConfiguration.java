/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
package org.onap.so.asdc.etsi.pkg.processor;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.configuration.rest.HttpClientConnectionConfiguration;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
public class SslBasedHttpClientConfiguration {
    private final static Logger LOGGER = LoggerFactory.getLogger(EtsiResourcePackageProcessor.class);


    public static final String SSL_BASED_CONFIGURABLE_REST_TEMPLATE = "sslBasedConfigurableRestTemplate";
    private final HttpClientConnectionConfiguration clientConnectionConfiguration;

    @Autowired
    public SslBasedHttpClientConfiguration(final HttpClientConnectionConfiguration clientConnectionConfiguration) {
        this.clientConnectionConfiguration = clientConnectionConfiguration;
    }

    @Bean
    @Qualifier(SSL_BASED_CONFIGURABLE_REST_TEMPLATE)
    public RestTemplate sslBasedConfigurableRestTemplate() {
        final RestTemplate restTemplate =
                new RestTemplate(new BufferingClientHttpRequestFactory(httpComponentsClientHttpRequestFactory()));
        restTemplate.getInterceptors().add(new SOSpringClientFilter());
        restTemplate.getInterceptors().add((new SpringClientPayloadFilter()));
        return restTemplate;
    }

    private HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {
        try {
            LOGGER.debug("Setting up HttpComponentsClientHttpRequestFactory with default SSL Context");
            return new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create()
                    .setConnectionManager(getConnectionManager())
                    .setMaxConnPerRoute(clientConnectionConfiguration.getMaxConnectionsPerRoute())
                    .setMaxConnTotal(clientConnectionConfiguration.getMaxConnections())
                    .setDefaultRequestConfig(getRequestConfig()).setSSLContext(SSLContext.getDefault()).build());

        } catch (final NoSuchAlgorithmException exception) {
            LOGGER.error("Failed to create HttpComponentsClientHttpRequestFactory with default SSL Context", exception);
            throw new RuntimeException(exception);
        }
    }

    private PoolingHttpClientConnectionManager getConnectionManager() {
        return new PoolingHttpClientConnectionManager(clientConnectionConfiguration.getTimeToLiveInMins(),
                TimeUnit.MINUTES);
    }

    private RequestConfig getRequestConfig() {
        return RequestConfig.custom().setSocketTimeout(clientConnectionConfiguration.getSocketTimeOutInMiliSeconds())
                .setConnectTimeout(clientConnectionConfiguration.getConnectionTimeOutInMilliSeconds()).build();
    }

}
