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

import java.lang.reflect.Method;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;

public interface Get {
    public String get(String name, String def, boolean print);


    /**
     * A class for Getting info out of "JavaBean" format
     * 
     * @author Jonathan
     *
     */
    public static class Bean implements Get {
        private Object bean;
        private Class<?> bc;
        private Class<?>[] params;
        private Object[] args;

        public Bean(Object bean) {
            this.bean = bean;
            bc = bean.getClass();
            params = new Class<?>[0]; // note, this will allow to go out of scope after config
            args = new Object[0];
        }

        public String get(String name, String def, boolean print) {
            String str = null;
            String gname = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            try {
                Method meth = bc.getMethod(gname, params);
                Object obj = meth.invoke(bean, args);
                str = obj == null ? null : obj.toString(); // easy string convert...
            } catch (Exception e) {
            }

            // Take def if nothing else
            if (str == null) {
                str = def;
                // don't log defaults
            } else {
                str = str.trim(); // this is vital in Property File based values, as spaces can hide easily
            }
            // Note: Can't log during configuration
            return str;
        }
    }

    public static Get NULL = new Get() {
        public String get(String name, String def, boolean print) {
            return def;
        }
    };

    public static class AccessGet implements Get {
        private Access access;

        public AccessGet(Access access) {
            this.access = access;
        }

        public String get(String name, String def, boolean print) {
            String gotten = access.getProperty(name, def);
            if (print) {
                if (gotten == null) {
                    access.log(Level.INIT, name, "is not set");
                } else {
                    access.log(Level.INIT, name, "is set to", gotten);
                }
            }
            return gotten;
        }
    }

}
