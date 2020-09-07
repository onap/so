/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.vnfm;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Iterator;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.onap.so.configuration.rest.HttpComponentsClientConfiguration;
import org.onap.so.configuration.rest.HttpHeadersProvider;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.GsonProvider;
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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
public class Sol003AdapterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(Sol003AdapterConfiguration.class);

    public static final String SOL003_ADAPTER_REST_TEMPLATE_BEAN = "Sol003AdapterRestTemplateBean";
    public static final String SOL003_ADAPTER_HTTP_REST_SERVICE_PROVIDER_BEAN =
            "Sol003AdapterHttpRestServiceProviderBean";

    @Value("${rest.http.client.configuration.ssl.trustStore:#{null}}")
    private Resource trustStore;

    @Value("${rest.http.client.configuration.ssl.trustStorePassword:#{null}}")
    private String trustStorePassword;

    @Value("so.adapters.sol003-adapter.auth:Basic dm5mbTpwYXNzd29yZDEk")
    private String sol003AdapterBasicAuth;

    @Autowired
    private GsonProvider gsonProvider;

    @Autowired
    private HttpComponentsClientConfiguration httpComponentsClientConfiguration;

    @Bean
    @Qualifier(SOL003_ADAPTER_REST_TEMPLATE_BEAN)
    public RestTemplate sol003AdapterRestTemplate() {
        final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                httpComponentsClientConfiguration.httpComponentsClientHttpRequestFactory();
        final RestTemplate restTemplate =
                new RestTemplate(new BufferingClientHttpRequestFactory(clientHttpRequestFactory));
        restTemplate.getInterceptors().add(new SOSpringClientFilter());
        restTemplate.getInterceptors().add((new SpringClientPayloadFilter()));
        return restTemplate;

    }

    @Bean
    @Qualifier(SOL003_ADAPTER_HTTP_REST_SERVICE_PROVIDER_BEAN)
    public HttpRestServiceProvider sol003AdapaterHttpRestServiceProvider(
            @Qualifier(SOL003_ADAPTER_REST_TEMPLATE_BEAN) @Autowired final RestTemplate restTemplate) {

        if (trustStore != null) {
            setTrustStore(restTemplate);
        }
        setGsonMessageConverter(restTemplate);
        return getHttpRestServiceProvider(restTemplate, new BasicHttpHeadersProvider(sol003AdapterBasicAuth));
    }

    private void setTrustStore(final RestTemplate restTemplate) {
        try {
            final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(getSSLContext());
            final HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            final HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);
            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(factory));
        } catch (Exception exception) {
            logger.error("Error reading truststore, TLS connection to VNFM will fail.", exception);
        }
    }

    private SSLContext getSSLContext() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, IOException {
        if (trustStore != null) {
            logger.info("Setting truststore: {}", trustStore.getURL());
            return new SSLContextBuilder().loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
                    .build();
        }
        logger.info("Setting Default SSL ...");
        return SSLContext.getDefault();

    }

    private HttpRestServiceProvider getHttpRestServiceProvider(final RestTemplate restTemplate,
            final HttpHeadersProvider httpHeadersProvider) {
        return new HttpRestServiceProviderImpl(restTemplate, httpHeadersProvider.getHttpHeaders());
    }

    private void setGsonMessageConverter(final RestTemplate restTemplate) {
        final Iterator<HttpMessageConverter<?>> iterator = restTemplate.getMessageConverters().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof MappingJackson2HttpMessageConverter) {
                iterator.remove();
            }
        }
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter(gsonProvider.getGson()));
    }
}
