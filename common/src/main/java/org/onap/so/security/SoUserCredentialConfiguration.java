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

import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
@ConfigurationProperties(prefix = "spring.security")
public class SoUserCredentialConfiguration {

    private List<UserCredentials> credentials = new ArrayList<>();
    private final List<String> roles = new ArrayList<>();

    public List<String> getRoles() {
        return roles;
    }

    @PostConstruct
    private void addRoles() {
        for (int i = 0; i < credentials.size(); i++) {
            roles.add(credentials.get(i).getRole());
        }
    }

    public List<UserCredentials> getUsercredentials() {
        return credentials;
    }

    public void setUsercredentials(final List<UserCredentials> usercredentials) {
        if (usercredentials != null) {
            this.credentials = usercredentials;
        }
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
