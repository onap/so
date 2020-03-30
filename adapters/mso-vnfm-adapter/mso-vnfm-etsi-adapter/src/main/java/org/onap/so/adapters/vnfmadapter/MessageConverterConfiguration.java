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
package org.onap.so.adapters.vnfmadapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Collection;
import org.onap.so.adapters.vnfmadapter.converters.etsicatalog.sol003.PkgChangeNotificationConverter;
import org.onap.so.adapters.vnfmadapter.converters.etsicatalog.sol003.PkgOnboardingNotificationConverter;
import org.onap.so.adapters.vnfmadapter.converters.etsicatalog.sol003.VnfPkgInfoConverter;
import org.onap.so.adapters.vnfmadapter.converters.sol003.etsicatalog.PkgmSubscriptionRequestConverter;
import org.onap.so.adapters.vnfmadapter.oauth.OAuth2AccessTokenAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * Configures message converter
 */
@Configuration
public class MessageConverterConfiguration {

    private final VnfmAdapterUrlProvider vnfmAdapterUrlProvider;

    @Autowired
    public MessageConverterConfiguration(final VnfmAdapterUrlProvider vnfmAdapterUrlProvider) {
        this.vnfmAdapterUrlProvider = vnfmAdapterUrlProvider;
    }

    @Bean
    public ConversionService conversionService() {
        final DefaultConversionService service = new DefaultConversionService();
        service.addConverter(new VnfPkgInfoConverter(vnfmAdapterUrlProvider));
        service.addConverter(new PkgmSubscriptionRequestConverter());
        service.addConverter(new PkgChangeNotificationConverter());
        service.addConverter(new PkgOnboardingNotificationConverter());
        return service;
    }

    @Bean
    public HttpMessageConverters customConverters() {
        final Collection<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        final Gson gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(OAuth2AccessToken.class, new OAuth2AccessTokenAdapter()).create();
        final GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter(gson);
        messageConverters.add(gsonHttpMessageConverter);
        return new HttpMessageConverters(true, messageConverters);
    }
}
