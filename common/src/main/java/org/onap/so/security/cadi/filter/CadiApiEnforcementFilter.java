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
 */
package org.onap.so.security.cadi.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.ServletContextAccess;
import org.onap.so.security.cadi.config.Config;
import org.onap.so.security.cadi.util.Split;

/**
 * This filter allows one to protect the APIs from data stored in AAF
 *
 * @author Instrumental(Jonathan)
 */
public class CadiApiEnforcementFilter implements Filter {
    private String type;
    private Map<String, List<String>> publicPaths;
    private Access access;


    public CadiApiEnforcementFilter(Access access, String enforce) throws ServletException {
        this.access = access;
        init(enforce);
    }


    @Override
    public void init(FilterConfig fc) throws ServletException {
        init(fc.getInitParameter(Config.CADI_API_ENFORCEMENT));
        // need the Context for Logging, instantiating ClassLoader, etc
        ServletContextAccess sca = new ServletContextAccess(fc);
        if (access == null) {
            access = sca;
        }
    }

    private void init(final String ptypes) throws ServletException {
        if (ptypes == null) {
            throw new ServletException("CadiApiEnforcement requires " + Config.CADI_API_ENFORCEMENT + " property");
        }
        String[] full = Split.splitTrim(';', ptypes);
        if (full.length == 0) {
            throw new ServletException(Config.CADI_API_ENFORCEMENT + " property is empty");
        }
        if (full.length > 0) {
            type = full[0];
        }
        publicPaths = new TreeMap<String, List<String>>();
        if (full.length > 1) {
            for (int i = 1; i < full.length; ++i) {
                String pubArray[] = Split.split(':', full[i]);
                if (pubArray.length == 2) {
                    List<String> ls = publicPaths.get(pubArray[0]);
                    if (ls == null) {
                        ls = new ArrayList<String>();
                        publicPaths.put(pubArray[0], ls);
                    }
                    ls.add(pubArray[1]);
                }
            }
        }
    }


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc)
            throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest) req;
        final String meth = hreq.getMethod();
        String path = hreq.getContextPath() + hreq.getPathInfo();

        if (path == null || path.isEmpty() || "null".equals(path))
            path = hreq.getRequestURI().substring(hreq.getContextPath().length());

        List<String> list = publicPaths.get(meth);
        if (list != null) {
            for (String p : publicPaths.get(meth)) {
                if (path.startsWith(p)) {
                    access.printf(Level.INFO, "%s accessed public API %s %s\n", hreq.getUserPrincipal().getName(), meth,
                            path);
                    fc.doFilter(req, resp);
                    return;
                }
            }
        }
        if (hreq.isUserInRole(type + '|' + path + '|' + meth)) {
            access.printf(Level.INFO, "%s is allowed access to %s %s\n", hreq.getUserPrincipal().getName(), meth, path);
            fc.doFilter(req, resp);
        } else {
            access.printf(Level.AUDIT, "%s is denied access to %s %s\n", hreq.getUserPrincipal().getName(), meth, path);
        }
    }

    @Override
    public void destroy() {}
}
