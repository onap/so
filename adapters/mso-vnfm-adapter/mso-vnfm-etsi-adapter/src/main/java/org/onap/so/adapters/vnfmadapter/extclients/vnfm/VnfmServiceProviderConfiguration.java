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

package org.onap.so.adapters.vnfmadapter.extclients.vnfm;

import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import com.google.gson.Gson;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.ListIterator;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.JSON;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.onap.so.configuration.rest.HttpHeadersProvider;
import org.onap.so.logging.jaxrs.filter.SpringClientFilter;
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
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Configures the HttpRestServiceProvider for REST call to a VNFM.
 */
@Configuration
public class VnfmServiceProviderConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(VnfmServiceProviderConfiguration.class);

    @Value("${http.client.ssl.trust-store}")
    private Resource keyStore;
    @Value("${http.client.ssl.trust-store-password}")
    private String keyStorePassword;

    @Bean(name = "vnfmServiceProvider")
    public HttpRestServiceProvider httpRestServiceProvider(
            @Qualifier(CONFIGURABLE_REST_TEMPLATE) @Autowired final RestTemplate restTemplate) {
        return getHttpRestServiceProvider(restTemplate, new BasicHttpHeadersProvider());
    }

    private HttpRestServiceProvider getHttpRestServiceProvider(final RestTemplate restTemplate,
            final HttpHeadersProvider httpHeadersProvider) {
        setGsonMessageConverter(restTemplate);
        setTrustStore(restTemplate);
        removeSpringClientFilter(restTemplate);
        return new HttpRestServiceProviderImpl(restTemplate, httpHeadersProvider);
    }

    private void setGsonMessageConverter(final RestTemplate restTemplate) {
        final Iterator<HttpMessageConverter<?>> iterator = restTemplate.getMessageConverters().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof MappingJackson2HttpMessageConverter) {
                iterator.remove();
            }
        }
        final Gson gson = new JSON().getGson();
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter(gson));
    }

    private void setTrustStore(final RestTemplate restTemplate) {
        SSLContext sslContext;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(keyStore.getURL(), keyStorePassword.toCharArray())
                    .build();
            logger.info("Setting truststore: {}", keyStore.getURL());
            final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            final HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            final HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);
            restTemplate.setRequestFactory(factory);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException
                | IOException exception) {
            logger.error("Error reading truststore, TLS connection to VNFM will fail.", exception);
        }
    }

    private void removeSpringClientFilter(final RestTemplate restTemplate) {
        ListIterator<ClientHttpRequestInterceptor> interceptorIterator = restTemplate.getInterceptors().listIterator();
        while (interceptorIterator.hasNext()) {
            if (interceptorIterator.next() instanceof SpringClientFilter) {
                interceptorIterator.remove();
            }
        }
    }

}
