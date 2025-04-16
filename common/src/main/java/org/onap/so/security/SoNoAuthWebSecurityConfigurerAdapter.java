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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@EnableWebSecurity
@Configuration
@Order(2)
@Profile({"aaf", "test", "serviceMesh"})
public class SoNoAuthWebSecurityConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SoNoAuthWebSecurityConfigurerAdapter.class);

    @Bean(name = "webSecurityBeanOfSoNoAuthWebSecurityConfigurerAdapter")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        LOGGER.debug("Inside NoAuthWebSecurityConfigurerAdapter");
        LOGGER.debug("Inside NoAuthWebSecurityConfigurerAdapter");
        http.authorizeHttpRequests((requests) -> requests.anyRequest().permitAll()).csrf(csrf -> csrf.disable());

        final StrictHttpFirewall firewall = new MSOSpringFirewall();
        http.setSharedObject(StrictHttpFirewall.class, firewall);

        return http.build();
    }
}
