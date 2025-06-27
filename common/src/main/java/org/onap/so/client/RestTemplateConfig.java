/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client;

import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.configuration.rest.HttpComponentsClientConfiguration;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    public static final String CONFIGURABLE_REST_TEMPLATE = "configurableRestTemplate";
    private final HttpComponentsClientConfiguration httpComponentsClientConfiguration;

    @Bean
    @Primary
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        restTemplate
                .setRequestFactory(new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory()));
        restTemplate.getInterceptors().add(new SOSpringClientFilter());
        restTemplate.getInterceptors().add((new SpringClientPayloadFilter()));
        return restTemplate;
    }

    @Bean
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    RestTemplate configurableRestTemplate(RestTemplateBuilder builder) {
        final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                httpComponentsClientConfiguration.httpComponentsClientHttpRequestFactory();
        final RestTemplate restTemplate =
                builder.requestFactory(() -> new BufferingClientHttpRequestFactory(clientHttpRequestFactory)).build();
        restTemplate.getInterceptors().add(new SOSpringClientFilter());
        restTemplate.getInterceptors().add((new SpringClientPayloadFilter()));
        return restTemplate;
    }
}
