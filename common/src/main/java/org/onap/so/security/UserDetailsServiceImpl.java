/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.context.annotation.Profile;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

@Service
@Profile("basic")
@ConfigurationProperties(prefix = "spring.security")
public class UserDetailsServiceImpl implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private List<UserCredentials> usercredentials;

    // Granted Authorities
    private List<GrantedAuthority> getAuthorities(String roles) {
        return Arrays.stream(roles.split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public List<UserCredentials> getUsercredentials() {
        logger.debug("UserDetailsServiceImpl.getUsercredentials: usercredentials: {}", usercredentials);
        return usercredentials;
    }

    public void setUsercredentials(List<UserCredentials> usercredentials) {
        this.usercredentials = usercredentials;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        logger.debug("UserDetailsServiceImpl-----> username: {}", username);
        if (usercredentials != null) {
            for (UserCredentials user : usercredentials) {
                if (user.getUsername().equals(username)) {
                    logger.debug("UserDetailsServiceImpl.loadUserByUsername: user found: {}", username);
                    logger.debug("UserDetailsServiceImpl.loadUserByUsername: password found: {}", user.getPassword());
                    return User.withUsername(username).password(user.getPassword()).roles(user.getRole())
                            .authorities(getAuthorities(user.getRole())).build();
                }
            }
        }
        throw new UsernameNotFoundException("User not found, username: " + username);
    }
}
