/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;

import javax.ws.rs.core.MediaType;
import org.onap.so.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.configuration.rest.HttpComponentsClientConfiguration;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.onap.so.rest.service.HttpRestServiceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author waqas.ikram@est.tech
 * @author Raviteja Karumuri (raviteja.karumuri@est.tech)
 */
@Configuration
public class CnfmHttpServiceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CnfmHttpServiceConfiguration.class);
    public static final String CNFM_REST_TEMPLATE_CLIENT_BEAN = "cnfmRestTemplateBean";
    public static final String CNFM_HTTP_REST_SERVICE_PROVIDER_BEAN = "cnfmHttpRestServiceProvider";

    @Bean
    @Qualifier(CNFM_REST_TEMPLATE_CLIENT_BEAN)
    public RestTemplate cnfmRestTemplateBean(
            @Autowired final HttpComponentsClientConfiguration httpComponentsClientConfiguration) {
        logger.debug("Setting up cnfm RestTemplate bean...");
        final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                httpComponentsClientConfiguration.httpComponentsClientHttpRequestFactory();

        final RestTemplate restTemplate =
                new RestTemplate(new BufferingClientHttpRequestFactory(clientHttpRequestFactory));
        restTemplate.getInterceptors().add(new SOSpringClientFilter());
        restTemplate.getInterceptors().add((new SpringClientPayloadFilter()));
        return restTemplate;

    }

    @Bean
    @Qualifier(CNFM_HTTP_REST_SERVICE_PROVIDER_BEAN)
    public HttpRestServiceProvider cnfmHttpRestServiceProvider(
            @Qualifier(CNFM_REST_TEMPLATE_CLIENT_BEAN) @Autowired final RestTemplate restTemplate) {

        logger.debug("Setting up cnfm HttpRestServiceProvider bean...");
        final HttpHeaders hearders = new HttpHeaders();
        hearders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        hearders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        logger.debug("Return cnfm HttpRestServiceProvider bean...");

        return new HttpRestServiceProviderImpl(restTemplate, hearders);
    }

}
