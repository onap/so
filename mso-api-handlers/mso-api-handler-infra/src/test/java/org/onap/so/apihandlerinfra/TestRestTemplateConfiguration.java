/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

package org.onap.so.apihandlerinfra;

import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Test-only override for the primary {@link RestTemplate} bean. Disables HTTP connection reuse to prevent
 * {@code NoHttpResponseException} caused by stale pooled connections to the embedded WireMock server.
 */
@Configuration
@Profile("test")
public class TestRestTemplateConfiguration {

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(requestFactory));
        restTemplate.getInterceptors().add(new SOSpringClientFilter());
        restTemplate.getInterceptors().add(new SpringClientPayloadFilter());
        return restTemplate;
    }
}
