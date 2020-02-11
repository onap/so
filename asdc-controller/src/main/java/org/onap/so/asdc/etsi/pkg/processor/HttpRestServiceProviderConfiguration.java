/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
package org.onap.so.asdc.etsi.pkg.processor;

import static org.onap.so.asdc.etsi.pkg.processor.SslBasedHttpClientConfiguration.SSL_BASED_CONFIGURABLE_REST_TEMPLATE;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.onap.so.rest.service.HttpRestServiceProviderImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
public class HttpRestServiceProviderConfiguration {

    public static final String ETSI_CATALOG_HTTP_REST_SERVICE_PROVIDER_BEAN = "etsiCatalogHttpRestServiceProviderBean";
    public static final String SDC_HTTP_REST_SERVICE_PROVIDER_BEAN = "sdcHttpRestServiceProviderBean";

    @Bean
    @Qualifier(ETSI_CATALOG_HTTP_REST_SERVICE_PROVIDER_BEAN)
    public HttpRestServiceProvider etsiCataloghttpRestServiceProvider(
            @Qualifier(CONFIGURABLE_REST_TEMPLATE) final RestTemplate restTemplate) {
        return new HttpRestServiceProviderImpl(restTemplate);
    }

    @Bean
    @Qualifier(SDC_HTTP_REST_SERVICE_PROVIDER_BEAN)
    public HttpRestServiceProvider sdchttpRestServiceProvider(
            @Qualifier(SSL_BASED_CONFIGURABLE_REST_TEMPLATE) final RestTemplate restTemplate) {
        return new HttpRestServiceProviderImpl(restTemplate);
    }

}
