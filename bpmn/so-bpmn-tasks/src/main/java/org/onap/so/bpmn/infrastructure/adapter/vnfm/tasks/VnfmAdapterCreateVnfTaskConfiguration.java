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

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.onap.so.configuration.BasicHttpHeadersProvider;
import org.onap.so.configuration.HttpHeadersProvider;
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
 * Provides {@link org.onap.so.rest.service.VnfmAdapterServiceProvider} configuration for
 * {@link VnfmAdapterCreateVnfTask}
 *
 * @author waqas.ikram@est.tech
 */
@Configuration
public class VnfmAdapterCreateVnfTaskConfiguration {

    public static final String VNFM_HTTP_REST_SERVICE_PROVIDER_BEAN = "vnfmHttpRestServiceProvider";

    private static final Logger logger = LoggerFactory.getLogger(VnfmAdapterCreateVnfTaskConfiguration.class);

    @Value("${rest.http.client.configuration.ssl.trustStore:#{null}}")
    private Resource trustStore;

    @Value("${rest.http.client.configuration.ssl.trustStorePassword:#{null}}")
    private String trustStorePassword;

    @Value("${rest.http.client.configuration.ssl.keyStore:#{null}}")
    private Resource keyStoreResource;

    @Value("${rest.http.client.configuration.ssl.keyStorePassword:#{null}}")
    private String keyStorePassword;

    @Bean
    @Qualifier(VNFM_HTTP_REST_SERVICE_PROVIDER_BEAN)
    public HttpRestServiceProvider vnfmHttpRestServiceProvider(
            @Qualifier(CONFIGURABLE_REST_TEMPLATE) @Autowired final RestTemplate restTemplate,
            @Autowired final VnfmBasicHttpConfigProvider etsiVnfmAdapter) {
        if (trustStore != null) {
            setTrustStore(restTemplate);
        }
        return getHttpRestServiceProvider(restTemplate, new BasicHttpHeadersProvider(etsiVnfmAdapter.getAuth()));
    }

    private void setTrustStore(final RestTemplate restTemplate) {
        SSLContext sslContext;
        try {
            if (keyStoreResource != null) {
                KeyStore keystore = KeyStore.getInstance("pkcs12");
                keystore.load(keyStoreResource.getInputStream(), keyStorePassword.toCharArray());
                sslContext =
                        new SSLContextBuilder().loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
                                .loadKeyMaterial(keystore, keyStorePassword.toCharArray()).build();
            } else {
                sslContext = new SSLContextBuilder()
                        .loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray()).build();
            }
            logger.info("Setting truststore: {}", trustStore.getURL());
            final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            final HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            final HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);
            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(factory));
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException
                | IOException | UnrecoverableKeyException exception) {
            logger.error("Error reading truststore, TLS connection to VNFM will fail.", exception);
        }
    }

    private HttpRestServiceProvider getHttpRestServiceProvider(final RestTemplate restTemplate,
            final HttpHeadersProvider httpHeadersProvider) {
        return new HttpRestServiceProviderImpl(restTemplate, httpHeadersProvider);
    }

}
