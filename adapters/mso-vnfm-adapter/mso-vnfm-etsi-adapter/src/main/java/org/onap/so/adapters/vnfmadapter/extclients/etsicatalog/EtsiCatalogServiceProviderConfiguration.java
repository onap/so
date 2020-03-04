/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.adapters.vnfmadapter.extclients.etsicatalog;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.adapters.vnfmadapter.extclients.AbstractServiceProviderConfiguration;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.onap.so.configuration.rest.HttpClientConnectionConfiguration;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.onap.so.rest.service.HttpRestServiceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configures the HttpRestServiceProvider to make REST calls to the ETSI Catalog Manager
 * 
 * @author gareth.roper@est.tech
 */

@Configuration
public class EtsiCatalogServiceProviderConfiguration extends AbstractServiceProviderConfiguration {

    public static final String ETSI_CATALOG_REST_TEMPLATE_BEAN = "etsiCatalogRestTemplate";

    public static final String ETSI_CATALOG_SERVICE_PROVIDER_BEAN = "etsiCatalogServiceProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(EtsiCatalogServiceProviderConfiguration.class);

    private final HttpClientConnectionConfiguration clientConnectionConfiguration;

    @Value("${etsi-catalog-manager.http.client.ssl.trust-store:#{null}}")
    private Resource trustStore;
    @Value("${etsi-catalog-manager.http.client.ssl.trust-store-password:#{null}}")
    private String trustStorePassword;

    @Autowired
    public EtsiCatalogServiceProviderConfiguration(
            final HttpClientConnectionConfiguration clientConnectionConfiguration) {
        this.clientConnectionConfiguration = clientConnectionConfiguration;
    }

    @Bean
    @Qualifier(ETSI_CATALOG_REST_TEMPLATE_BEAN)
    public RestTemplate etsiCatalogRestTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new SOSpringClientFilter());
        restTemplate.getInterceptors().add((new SpringClientPayloadFilter()));
        return restTemplate;
    }

    @Bean
    @Qualifier(ETSI_CATALOG_SERVICE_PROVIDER_BEAN)
    public HttpRestServiceProvider etsiCatalogHttpRestServiceProvider(
            @Qualifier(ETSI_CATALOG_REST_TEMPLATE_BEAN) final RestTemplate restTemplate) {
        setGsonMessageConverter(restTemplate);

        final HttpClientBuilder httpClientBuilder = getHttpClientBuilder();
        if (trustStore != null) {
            try {
                LOGGER.debug("Setting up HttpComponentsClientHttpRequestFactory with SSL Context");
                LOGGER.debug("Setting client trust-store: {}", trustStore.getURL());
                LOGGER.debug("Creating SSLConnectionSocketFactory with AllowAllHostsVerifier ... ");
                final SSLContext sslContext = new SSLContextBuilder()
                        .loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray()).build();
                final SSLConnectionSocketFactory sslConnectionSocketFactory =
                        new SSLConnectionSocketFactory(sslContext, AllowAllHostsVerifier.INSTANCE);
                httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
                final Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                        .<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", sslConnectionSocketFactory).build();

                httpClientBuilder.setConnectionManager(getConnectionManager(socketFactoryRegistry));
            } catch (final KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException
                    | IOException exception) {
                LOGGER.error("Error reading truststore, TLS connection will fail.", exception);
            }

        } else {
            LOGGER.debug("Setting connection manager without SSL ConnectionSocketFactory ...");
            httpClientBuilder.setConnectionManager(getConnectionManager());
        }

        final HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(factory));

        return new HttpRestServiceProviderImpl(restTemplate, new BasicHttpHeadersProvider().getHttpHeaders());
    }

    private PoolingHttpClientConnectionManager getConnectionManager(
            final Registry<ConnectionSocketFactory> socketFactoryRegistry) {
        return new PoolingHttpClientConnectionManager(socketFactoryRegistry, null, null, null,
                clientConnectionConfiguration.getTimeToLiveInMins(), TimeUnit.MINUTES);
    }

    private PoolingHttpClientConnectionManager getConnectionManager() {
        return new PoolingHttpClientConnectionManager(clientConnectionConfiguration.getTimeToLiveInMins(),
                TimeUnit.MINUTES);
    }

    private HttpClientBuilder getHttpClientBuilder() {
        return HttpClientBuilder.create().setMaxConnPerRoute(clientConnectionConfiguration.getMaxConnectionsPerRoute())
                .setMaxConnTotal(clientConnectionConfiguration.getMaxConnections())
                .setDefaultRequestConfig(getRequestConfig());
    }

    private RequestConfig getRequestConfig() {
        return RequestConfig.custom().setSocketTimeout(clientConnectionConfiguration.getSocketTimeOutInMiliSeconds())
                .setConnectTimeout(clientConnectionConfiguration.getConnectionTimeOutInMilliSeconds()).build();
    }

    private static final class AllowAllHostsVerifier implements HostnameVerifier {

        private static final AllowAllHostsVerifier INSTANCE = new AllowAllHostsVerifier();

        @Override
        public boolean verify(final String hostname, final SSLSession session) {
            LOGGER.debug("Skipping hostname verification ...");
            return true;
        }

    }

}
