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

import org.onap.so.configuration.rest.HttpComponentsClientConfiguration;
import org.onap.so.logging.jaxrs.filter.SpringClientFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    public static final String CONFIGURABLE_REST_TEMPLATE = "configurableRestTemplate";

    @Autowired
    private HttpComponentsClientConfiguration httpComponentsClientConfiguration;

    @Bean
    public RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate
                .setRequestFactory(new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory()));
        restTemplate.getInterceptors().add(new SpringClientFilter());
        return restTemplate;
    }

    @Bean
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    public RestTemplate configurableRestTemplate() {
        final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                httpComponentsClientConfiguration.httpComponentsClientHttpRequestFactory();
        final RestTemplate restTemplate =
                new RestTemplate(new BufferingClientHttpRequestFactory(clientHttpRequestFactory));
        restTemplate.getInterceptors().add(new SpringClientFilter());
        return restTemplate;
    }
}
