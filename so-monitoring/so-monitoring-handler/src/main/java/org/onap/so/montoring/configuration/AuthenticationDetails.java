/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
package org.onap.so.montoring.configuration;

import static org.onap.so.montoring.utils.ObjectEqualsUtils.isEqual;

import org.springframework.stereotype.Service;

/**
 * @author waqas.ikram@ericsson.com
 */
@Service
public class AuthenticationDetails {

    private final String username;

    private final String password;

    public AuthenticationDetails(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    public boolean isValid() {
        return (username != null && !username.isEmpty()) && (password != null && !password.isEmpty());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof AuthenticationDetails) {
            final AuthenticationDetails other = (AuthenticationDetails) obj;
            return isEqual(username, other.username) && isEqual(password, other.password);
        }
        return false;
    }

    @Override
    public String toString() {
        return "CamundaAuthenticationDetails [username=" + username + ", password=" + password + "]";
    }

}
