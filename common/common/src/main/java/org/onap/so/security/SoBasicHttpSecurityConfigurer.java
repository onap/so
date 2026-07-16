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

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("basic")
public class SoBasicHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Autowired
    private SoUserCredentialConfiguration soUserCredentialConfiguration;

    private static final String[] unauthenticatedEndpoints = new String[] {"/manage/health", "/manage/info", "/error"};

    private static RequestMatcher[] antMatchers(String... patterns) {
        return Arrays.stream(patterns).map(AntPathRequestMatcher::new).toArray(RequestMatcher[]::new);
    }

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        if (soUserCredentialConfiguration.getRbacEnabled()) {
            String roles = StringUtils.collectionToDelimitedString(soUserCredentialConfiguration.getRoles(), ",");
            // Spring Security 6.5 rejects duplicate roles in authorizeHttpRequests().hasAnyRole(...)
            // with "duplicate element". getRoles() aggregates roles across all configured users, so
            // multiple users sharing a role (e.g. several BPEL-Client accounts) would repeat it.
            // Deduplicate before passing them in.
            String[] distinctRoles = Arrays.stream(roles.split(",")).distinct().toArray(String[]::new);
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(authorize -> authorize.requestMatchers(antMatchers(unauthenticatedEndpoints))
                            .permitAll().requestMatchers(antMatchers("/**")).hasAnyRole(distinctRoles))
                    .httpBasic(httpBasic -> {
                    });
        } else {
            log.debug("Not configuring RBAC for the app.");
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(authorize -> authorize.requestMatchers(antMatchers(unauthenticatedEndpoints))
                            .permitAll().requestMatchers(antMatchers("/**")).authenticated())
                    .httpBasic(httpBasic -> {
                    });
        }
    }

}
