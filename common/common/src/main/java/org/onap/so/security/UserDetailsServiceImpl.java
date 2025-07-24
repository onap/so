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
import java.util.List;

@ConfigurationProperties(prefix = "spring.security")
public class UserDetailsServiceImpl implements UserDetailsService {

    private List<UserCredentials> usercredentials;

    public List<UserCredentials> getUsercredentials() {
        return usercredentials;
    }

    public void setUsercredentials(List<UserCredentials> usercredentials) {
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
