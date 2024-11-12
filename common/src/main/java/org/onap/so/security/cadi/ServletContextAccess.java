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

package org.onap.so.security.cadi;

import java.util.Enumeration;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;

public class ServletContextAccess extends PropAccess {

    private ServletContext context;

    public ServletContextAccess(FilterConfig filterConfig) {
        super(filterConfig); // protected constructor... does not have "init" called.
        context = filterConfig.getServletContext();

        for (Enumeration<?> en = filterConfig.getInitParameterNames(); en.hasMoreElements();) {
            String name = (String) en.nextElement();
            setProperty(name, filterConfig.getInitParameter(name));
        }
        init(getProperties());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.PropAccess#log(org.onap.so.security.cadi.Access.Level, java.lang.Object[])
     */
    @Override
    public void log(Level level, Object... elements) {
        if (willLog(level)) {
            StringBuilder sb = buildMsg(level, elements);
            context.log(sb.toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.PropAccess#log(java.lang.Exception, java.lang.Object[])
     */
    @Override
    public void log(Exception e, Object... elements) {
        StringBuilder sb = buildMsg(Level.ERROR, elements);
        context.log(sb.toString(), e);
    }

    public ServletContext context() {
        return context;
    }
}
