/**
 * ============LICENSE_START==================================================== org.onap.so
 * =========================================================================== Copyright (c) 2018 AT&T Intellectual
 * Property. All rights reserved. =========================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.so.security.cadi.principal;

public class OAuth2FormPrincipal extends TaggedPrincipal {
    private final String username;
    private final String client_id;

    /*
     * Note: client_id and username might be the same, if only authenticating the Client_ID
     */
    public OAuth2FormPrincipal(final String client_id, final String username) {
        this.username = username;
        this.client_id = client_id;
    }

    @Override
    public String getName() {
        return username;
    }

    public String client_id() {
        return client_id;
    }

    @Override
    public String tag() {
        return "OAuth";
    }

    @Override
    public String personalName() {
        if (username != null && username != client_id) {
            StringBuilder sb = new StringBuilder();
            sb.append(username);
            sb.append('|');
            sb.append(client_id);
            return sb.toString();
        }
        return client_id;
    }
}
