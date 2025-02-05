/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
package org.onap.so.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;
import org.springframework.security.config.Customizer;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Slf4j
@Component("basic")
public class SoBasicHttpSecurityConfigurer implements HttpSecurityConfigurer {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SoBasicHttpSecurityConfigurer.class);
    @Autowired
    private SoUserCredentialConfiguration soUserCredentialConfiguration;

    private static final String[] unauthenticatedEndpoints = new String[] {"/manage/health", "/manage/info", "/error"};

    @Override
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        LOGGER.debug("Inside SoBasicHttpSecurityConfigurer");
        String role = soUserCredentialConfiguration.getRoles().toString();
        LOGGER.debug("*****soUserCredentialConfiguration.getRoles*********>>>" + role);
        // authorise requests with basic authentication for authenticated users
        http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/manage/health", "/manage/info").permitAll().requestMatchers("/**").authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

}
