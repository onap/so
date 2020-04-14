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
package org.onap.so.adapters.etsi.sol003.adapter.common.configuration;

import java.util.ArrayList;
import java.util.Collection;
import org.onap.so.adapters.etsi.sol003.adapter.oauth.configuration.OAuth2AccessTokenAdapter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Configures message converter
 */
@Configuration
public class MessageConverterConfiguration {

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
