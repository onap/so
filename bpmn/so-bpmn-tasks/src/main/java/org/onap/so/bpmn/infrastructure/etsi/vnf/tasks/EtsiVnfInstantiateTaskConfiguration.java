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

package org.onap.so.bpmn.infrastructure.etsi.vnf.tasks;

import static org.onap.so.client.RestTemplateConfig.MULTI_THREADED_REST_TEMPLATE;

import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.onap.so.configuration.rest.HttpHeadersProvider;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.onap.so.rest.service.HttpRestServiceProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Provides {@link org.onap.so.rest.service.VnfmAdapterServiceProvider} configuration for
 * {@link EtsiVnfInstantiateTask}
 * 
 * @author waqas.ikram@est.tech
 */
@Configuration
public class EtsiVnfInstantiateTaskConfiguration {

    @Bean
    public HttpRestServiceProvider databaseHttpRestServiceProvider(
            @Qualifier(MULTI_THREADED_REST_TEMPLATE) @Autowired final RestTemplate restTemplate,
            @Autowired final VnfmBasicHttpConfigProvider etsiVnfmAdapter) {
        return getHttpRestServiceProvider(restTemplate, new BasicHttpHeadersProvider(etsiVnfmAdapter.getAuth()));
    }

    private HttpRestServiceProvider getHttpRestServiceProvider(final RestTemplate restTemplate,
            final HttpHeadersProvider httpHeadersProvider) {
        return new HttpRestServiceProviderImpl(restTemplate, httpHeadersProvider);
    }

}
