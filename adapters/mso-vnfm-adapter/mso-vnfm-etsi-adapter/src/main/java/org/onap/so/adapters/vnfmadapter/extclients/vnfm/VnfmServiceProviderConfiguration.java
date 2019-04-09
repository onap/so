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

import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.onap.so.configuration.rest.HttpHeadersProvider;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.onap.so.rest.service.HttpRestServiceProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import java.util.Iterator;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;

/**
 * Configures the HttpRestServiceProvider for REST call to a VNFM.
 */
@Configuration
public class VnfmServiceProviderConfiguration {

    @Bean(name = "vnfmServiceProvider")
    public HttpRestServiceProvider httpRestServiceProvider(
            @Qualifier(CONFIGURABLE_REST_TEMPLATE) @Autowired final RestTemplate restTemplate) {
        return getHttpRestServiceProvider(restTemplate, new BasicHttpHeadersProvider());
    }

    private HttpRestServiceProvider getHttpRestServiceProvider(final RestTemplate restTemplate,
            final HttpHeadersProvider httpHeadersProvider) {
        setGsonMessageConverter(restTemplate);
        return new HttpRestServiceProviderImpl(restTemplate, httpHeadersProvider);
    }

    private void setGsonMessageConverter(final RestTemplate restTemplate) {
        final Iterator<HttpMessageConverter<?>> iterator = restTemplate.getMessageConverters().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof MappingJackson2HttpMessageConverter) {
                iterator.remove();
            }
        }
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
    }

}
