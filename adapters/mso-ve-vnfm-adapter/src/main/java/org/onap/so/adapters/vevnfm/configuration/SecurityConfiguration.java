/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.vevnfm.configuration;

import org.onap.so.security.SoBasicWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends SoBasicWebSecurityConfigurerAdapter {

    @Value("${notification.url}")
    private String notificationUrl;

    @Value("${notification.username}")
    private String notificationUsername;

    @Value("${notification.password}")
    private String notificationPassword;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(final HttpSecurity https) throws Exception {
        https.csrf().disable().authorizeRequests().antMatchers(notificationUrl).authenticated().and().httpBasic();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(notificationUsername)
                .password(passwordEncoder.encode(notificationPassword)).authorities("ROLE_USER");
    }
}
