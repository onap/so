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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "spring.security")
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Pattern BCRYPT_HASH = Pattern.compile("\\A\\$2[aby]?\\$\\d\\d\\$[./0-9A-Za-z]{53}\\z");

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private List<UserCredentials> usercredentials;

    public List<UserCredentials> getUsercredentials() {
        return usercredentials;
    }

    /**
     * Passwords may be configured in plaintext or as BCrypt hashes. Plaintext passwords are hashed once here, so that
     * every authentication compares against a hash.
     */
    public void setUsercredentials(List<UserCredentials> usercredentials) {
        if (usercredentials != null) {
            usercredentials.stream().filter(credentials -> credentials.getPassword() != null)
                    .filter(credentials -> !BCRYPT_HASH.matcher(credentials.getPassword()).matches())
                    .forEach(credentials -> credentials.setPassword(passwordEncoder.encode(credentials.getPassword())));
        }
        this.usercredentials = usercredentials;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        for (int i = 0; usercredentials != null && i < usercredentials.size(); i++) {
            if (usercredentials.get(i).getUsername().equals(username)) {
                return User.withUsername(username).password(usercredentials.get(i).getPassword())
                        .roles(usercredentials.get(i).getRole()).build();
            }
        }

        throw new UsernameNotFoundException("User not found, username: " + username);
    }

}
