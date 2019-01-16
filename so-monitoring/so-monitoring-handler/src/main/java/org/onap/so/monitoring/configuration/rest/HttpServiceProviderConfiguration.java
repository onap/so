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
package org.onap.so.monitoring.configuration.rest;

import static org.onap.so.monitoring.configuration.rest.RestTemplateConfiguration.CAMUNDA_REST_TEMPLATE;
import static org.onap.so.monitoring.configuration.rest.RestTemplateConfiguration.DATABASE_REST_TEMPLATE;

import org.onap.so.monitoring.rest.service.HttpRestServiceProvider;
import org.onap.so.monitoring.rest.service.HttpRestServiceProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * @author waqas.ikram@ericsson.com
 *
 */
@Configuration
public class HttpServiceProviderConfiguration {

    public static final String DATABASE_HTTP_REST_SERVICE_PROVIDER = "databaseHttpRestServiceProvider";
    public static final String CAMUNDA_HTTP_REST_SERVICE_PROVIDER = "camundaHttpRestServiceProvider";

    @Bean
    @Qualifier(CAMUNDA_HTTP_REST_SERVICE_PROVIDER)
    public HttpRestServiceProvider camundaHttpRestServiceProvider(
            @Qualifier(CAMUNDA_REST_TEMPLATE) @Autowired final RestTemplate restTemplate,
            @Value(value = "${camunda.rest.api.auth:#{null}}") final String authorization) {
        return getHttpRestServiceProvider(restTemplate, authorization);
    }

    @Bean
    @Qualifier(DATABASE_HTTP_REST_SERVICE_PROVIDER)
    public HttpRestServiceProvider databaseHttpRestServiceProvider(
            @Qualifier(DATABASE_REST_TEMPLATE) @Autowired final RestTemplate restTemplate,
            @Value(value = "${mso.database.rest.api.auth:#{null}}") final String authorization) {

        return getHttpRestServiceProvider(restTemplate, authorization);
    }

    private HttpRestServiceProvider getHttpRestServiceProvider(final RestTemplate restTemplate,
            final String authorization) {
        if (authorization != null && !authorization.isEmpty()) {
            final ClientHttpRequestInterceptor authorizationInterceptor =
                    new BasicAuthorizationHttpRequestInterceptor(authorization);
            restTemplate.getInterceptors().add(authorizationInterceptor);
        }
        return new HttpRestServiceProviderImpl(restTemplate);
    }


}
