/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.configuration;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.onap.so.adapters.vevnfm.provider.AuthorizationHeadersProvider;
import org.onap.so.configuration.HttpHeadersProvider;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.onap.so.rest.service.HttpRestServiceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

    private final Resource clientKeyStore;
    private final String clientKeyStorePassword;
    private final Resource clientTrustStore;
    private final String clientTrustStorePassword;

    public ApplicationConfiguration(final ConfigProperties configProperties) {
        clientKeyStore = configProperties.getClientKeyStore();
        clientKeyStorePassword = configProperties.getClientKeyStorePassword();
        clientTrustStore = configProperties.getClientTrustStore();
        clientTrustStorePassword = configProperties.getClientTrustStorePassword();
    }

    @Bean
    public AuthorizationHeadersProvider headersProvider() {
        return new AuthorizationHeadersProvider();
    }

    @Bean
    public HttpRestServiceProvider restProvider(final RestTemplate restTemplate,
            final HttpHeadersProvider headersProvider) {
        modify(restTemplate);
        return new HttpRestServiceProviderImpl(restTemplate, headersProvider);
    }

    private void modify(final RestTemplate restTemplate) {

        if (clientKeyStore == null || clientTrustStore == null) {
            return;
        }

        try {
            final KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(clientKeyStore.getInputStream(), clientKeyStorePassword.toCharArray());

            final SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(clientTrustStore.getURL(), clientTrustStorePassword.toCharArray())
                    .loadKeyMaterial(keystore, clientKeyStorePassword.toCharArray()).build();

            logger.info("Setting truststore: {}", clientTrustStore.getURL());

            final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            final HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            final HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);

            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(factory));
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException
                | IOException | UnrecoverableKeyException e) {
            logger.error("Error reading truststore, TLS connection to VNFM will fail.", e);
        }
    }
}
