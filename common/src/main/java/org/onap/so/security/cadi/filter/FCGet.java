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

package org.onap.so.security.cadi.filter;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.config.Get;

/*
 * A private method to query the Filter config and if not exists, return the default. This cleans up the initialization
 * code.
 */
public class FCGet implements Get {
    /**
     *
     */
    private final Access access;
    private FilterConfig filterConfig;
    private ServletContext context;

    public FCGet(Access access, ServletContext context, FilterConfig filterConfig) {
        this.access = access;
        this.context = context;
        this.filterConfig = filterConfig;
    }

    public String get(String name, String def, boolean print) {
        String str = null;
        // Try Server Context First
        if (context != null) {
            str = context.getInitParameter(name);
        }

        // Try Filter Context next
        if (str == null && filterConfig != null) {
            str = filterConfig.getInitParameter(name);
        }

        if (str == null) {
            str = access.getProperty(name, def);
        }
        // Take def if nothing else
        if (str == null) {
            str = def;
            // don't log defaults
        } else {
            str = str.trim(); // this is vital in Property File based values, as spaces can hide easily
            if (print) {
                access.log(Level.INFO, "Setting", name, "to", str);
            }
        }
        return str;
    }
}
