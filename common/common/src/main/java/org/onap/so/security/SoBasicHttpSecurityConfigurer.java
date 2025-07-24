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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Slf4j
@Component("basic")
public class SoBasicHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Autowired
    private SoUserCredentialConfiguration soUserCredentialConfiguration;

    private static final String[] unauthenticatedEndpoints = new String[] {"/manage/health", "/manage/info", "/error"};

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        if (soUserCredentialConfiguration.getRbacEnabled()) {
            String roles = StringUtils.collectionToDelimitedString(soUserCredentialConfiguration.getRoles(), ",");
            http.csrf().disable().authorizeRequests().antMatchers(unauthenticatedEndpoints).permitAll()
                    .antMatchers("/**").hasAnyRole(roles).and().httpBasic();
        } else {
            log.debug("Not configuring RBAC for the app.");
            http.csrf().disable().authorizeRequests().antMatchers(unauthenticatedEndpoints).permitAll()
                    .antMatchers("/**").authenticated().and().httpBasic();
        }
    }

}
