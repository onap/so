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
package org.onap.so.adapters.etsisol003adapter.pkgm.extclients.vnfm;

import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.onap.so.adapters.etsi.sol003.adapter.common.configuration.AbstractServiceProviderConfiguration;
import org.onap.so.adapters.etsisol003adapter.pkgm.JSON;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.EtsiSubscriptionNotificationController;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.onap.so.rest.service.HttpRestServiceProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
public class VnfmHttpServiceProviderConfiguration extends AbstractServiceProviderConfiguration {
    private final JSON.OffsetDateTimeTypeAdapter offsetDateTimeTypeAdapter = new JSON.OffsetDateTimeTypeAdapter();

    public static final String VNFM_ADAPTER_HTTP_SERVICE_PROVIDER_BEAN = "vnfmAdapterHttpServiceProvider";

    @Bean
    @Qualifier(VNFM_ADAPTER_HTTP_SERVICE_PROVIDER_BEAN)
    public HttpRestServiceProvider vnfmAdapterHttpRestServiceProvider(
            @Autowired @Qualifier(CONFIGURABLE_REST_TEMPLATE) RestTemplate restTemplate) {
        setGsonMessageConverter(restTemplate);
        return new HttpRestServiceProviderImpl(restTemplate, new BasicHttpHeadersProvider().getHttpHeaders());
    }

    @Override
    protected Gson getGson() {
        return JSON.createGson().registerTypeAdapter(OffsetDateTime.class, offsetDateTimeTypeAdapter)
                .registerTypeAdapter(LocalDateTime.class,
                        new EtsiSubscriptionNotificationController.LocalDateTimeTypeAdapter())
                .create();
    }

}
