/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.openstack.beans;

import java.io.Serializable;
import java.util.Calendar;

/*
 * An entry in the Neutron Client Cache. It saves the Neutron client object along with the token expiration. After this
 * interval, this cache item will no longer be used.
 */
public class NeutronCacheEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String neutronUrl;
    private String token;
    private Calendar expires;

    public NeutronCacheEntry(String neutronUrl, String token, Calendar expires) {
        this.neutronUrl = neutronUrl;
        this.token = token;
        this.expires = expires;
    }

    public String getNeutronUrl() {
        return neutronUrl;
    }

    public void setNeutronUrl(String neutronUrl) {
        this.neutronUrl = neutronUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isExpired() {
        if (expires == null) {
            return true;
        }

        return System.currentTimeMillis() > expires.getTimeInMillis();
    }
}
