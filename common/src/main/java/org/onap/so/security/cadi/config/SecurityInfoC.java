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

package org.onap.so.security.cadi.config;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.SecuritySetter;


public class SecurityInfoC<CLIENT> extends SecurityInfo {
    public static final String DEF_ID = "ID not Set";
    private static Map<Class<?>, SecurityInfoC<?>> sicMap = new HashMap<>();
    public SecuritySetter<CLIENT> defSS;


    public SecurityInfoC(Access access) throws CadiException {
        super(access);
        defSS = new DEFSS<CLIENT>();
    }

    @SuppressWarnings("unchecked")
    public static synchronized <CLIENT> SecurityInfoC<CLIENT> instance(Access access, Class<CLIENT> cls)
            throws CadiException {
        SecurityInfoInit<CLIENT> sii;
        if (cls.isAssignableFrom(HttpURLConnection.class)) {
            try {
                @SuppressWarnings("rawtypes")
                Class<SecurityInfoInit> initCls =
                        (Class<SecurityInfoInit>) Class.forName("org.onap.so.security.cadi.http.HSecurityInfoInit");
                sii = initCls.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new CadiException("CADI using HttpURLConnection requires cadi-client jar", e);
            }
        } else {
            sii = new SecurityInfoInit<CLIENT>() {
                @Override
                public SecuritySetter<CLIENT> bestDefault(SecurityInfoC<CLIENT> si) throws CadiException {
                    return new DEFSS<CLIENT>();
                }
            };
        }

        SecurityInfoC<CLIENT> sic = (SecurityInfoC<CLIENT>) sicMap.get(cls);
        if (sic == null) {
            sic = new SecurityInfoC<CLIENT>(access);
            sic.set(sii.bestDefault(sic));
            sicMap.put(cls, sic);
        }
        return sic;
    }

    public SecurityInfoC<CLIENT> set(SecuritySetter<CLIENT> defSS) {
        this.defSS = defSS;
        return this;
    }

    private static class DEFSS<C> implements SecuritySetter<C> {
        @Override
        public String getID() {
            return DEF_ID;
        }

        @Override
        public void setSecurity(C client) throws CadiException {
            throw new CadiException("No Client Credentials set.");
        }

        @Override
        public int setLastResponse(int respCode) {
            return 0;
        }
    };
}
