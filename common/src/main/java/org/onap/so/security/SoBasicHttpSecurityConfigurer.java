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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component("basic")
public class SoBasicHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Autowired
    private SoUserCredentialConfiguration soUserCredentialConfiguration;

    @Override
    public SecurityFilterChain configure(final HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).authorizeRequests().requestMatchers("/manage/health", "/manage/info")
                .permitAll().requestMatchers("/**")
                .hasAnyRole(StringUtils.collectionToDelimitedString(soUserCredentialConfiguration.getRoles(), ","))
                .and().httpBasic(httpBasic -> httpBasic.disable());
        return null;
    }

}
