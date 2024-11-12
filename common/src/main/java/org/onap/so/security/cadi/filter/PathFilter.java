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
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.config.Config;

/**
 * PathFilter
 *
 * This class implements Servlet Filter, and uses AAF to validate access to a Path.
 *
 * This class can be used in a standard J2EE Servlet manner.
 *
 * @author Jonathan, collaborating with Xue Gao
 *
 */
public class PathFilter implements Filter {
    private final Log log;

    private ServletContext context;
    private String aafType;
    private String notAuthorizedMsg;

    /**
     * Construct a viable Filter for installing in Container WEB.XML, etc.
     *
     */
    public PathFilter() {
        log = new Log() {
            public void info(String... msg) {
                context.log(build("INFO:", msg));
            }

            public void audit(String... msg) {
                context.log(build("AUDIT:", msg));
            }

            private String build(String type, String[] msg) {
                StringBuilder sb = new StringBuilder(type);
                for (String s : msg) {
                    sb.append(' ');
                    sb.append(s);
                }
                return sb.toString();
            }
        };
    }

    /**
     * Filter that can be constructed within Java
     * 
     * @param access
     */
    public PathFilter(final Access access) {
        log = new Log() {
            public void info(String... msg) {
                access.log(Level.INFO, (Object[]) msg);
            }

            public void audit(String... msg) {
                access.log(Level.AUDIT, (Object[]) msg);
            }
        };
    }

    /**
     * Init
     *
     * Standard Filter "init" call with FilterConfig to obtain properties. POJOs can construct a FilterConfig with the
     * mechanism of their choice, and standard J2EE Servlet engines utilize this mechanism already.
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        // need the Context for Logging, instantiating ClassLoader, etc
        context = filterConfig.getServletContext();
        StringBuilder sb = new StringBuilder();
        StringBuilder err = new StringBuilder();
        Object attr = context.getAttribute(Config.PATHFILTER_NS);
        if (attr == null) {
            err.append("PathFilter - pathfilter_ns is not set");
        } else {
            sb.append(attr.toString());
        }

        attr = context.getAttribute(Config.PATHFILTER_STACK);
        if (attr == null) {
            log.info("PathFilter - No pathfilter_stack set, ignoring");
        } else {
            sb.append('.');
            sb.append(attr.toString());
        }

        attr = context.getAttribute(Config.PATHFILTER_URLPATTERN);
        if (attr == null) {
            log.info("PathFilter - No pathfilter_urlpattern set, defaulting to 'urlpattern'");
            sb.append(".urlpattern");
        } else {
            sb.append('.');
            sb.append(attr.toString());
        }

        log.info("PathFilter - AAF Permission Type is", sb.toString());

        sb.append('|');

        aafType = sb.toString();

        attr = context.getAttribute(Config.PATHFILTER_NOT_AUTHORIZED_MSG);
        if (attr == null) {
            notAuthorizedMsg = "Forbidden - Not Authorized to access this Path";
        } else {
            notAuthorizedMsg = attr.toString();
        }

        if (err.length() > 0) {
            throw new ServletException(err.toString());
        }
    }

    private interface Log {
        public void info(String... msg);

        public void audit(String... msg);
    }

    /**
     * doFilter
     *
     * This is the standard J2EE invocation. Analyze the request, modify response as necessary, and only call the next
     * item in the filterChain if request is suitably Authenticated.
     */
    // TODO Always validate changes against Tomcat AbsCadiValve and Jaspi CadiSAM functions
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest) request;
        HttpServletResponse hresp = (HttpServletResponse) response;
        String perm = aafType + hreq.getPathInfo() + '|' + hreq.getMethod();
        if (hreq.isUserInRole(perm)) {
            chain.doFilter(request, response);
        } else {
            log.audit("PathFilter has denied", hreq.getUserPrincipal().getName(), "access to", perm);
            hresp.sendError(403, notAuthorizedMsg);
        }
    }

    /**
     * Containers call "destroy" when time to cleanup
     */
    public void destroy() {
        log.info("PathFilter destroyed.");
    }

}
