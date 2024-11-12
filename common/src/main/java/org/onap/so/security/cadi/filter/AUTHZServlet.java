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

import java.io.IOException;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Jonathan
 *
 */
public class AUTHZServlet<S extends Servlet> implements Servlet {
    private String[] roles;
    private Servlet delegate;

    protected AUTHZServlet(Class<S> cls) {
        try {
            delegate = cls.newInstance();
        } catch (Exception e) {
            delegate = null;
        }
        RolesAllowed rolesAllowed = cls.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            roles = null;
        } else {
            roles = rolesAllowed.value();
        }
    }

    public void init(ServletConfig sc) throws ServletException {
        if (delegate == null) {
            throw new ServletException("Invalid Servlet Delegate");
        }
        delegate.init(sc);
    }

    public ServletConfig getServletConfig() {
        return delegate.getServletConfig();
    }

    public String getServletInfo() {
        return delegate.getServletInfo();
    }

    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        if (roles == null) {
            delegate.service(req, resp);
            return;
        }

        // Validate
        try {
            HttpServletRequest hreq = (HttpServletRequest) req;
            for (String role : roles) {
                if (hreq.isUserInRole(role)) {
                    delegate.service(req, resp);
                    return;
                }
            }

            ((HttpServletResponse) resp).sendError(403); // forbidden
        } catch (ClassCastException e) {
            throw new ServletException("JASPIServlet only supports HTTPServletRequest/HttpServletResponse");
        }
    }

    public void destroy() {
        delegate.destroy();
    }

}
