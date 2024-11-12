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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.CadiWrap;
import org.onap.so.security.cadi.LocatorException;
import org.onap.so.security.cadi.Lur;
import org.onap.so.security.cadi.PropAccess;
import org.onap.so.security.cadi.ServletContextAccess;
import org.onap.so.security.cadi.TrustChecker;
import org.onap.so.security.cadi.config.Config;
import org.onap.so.security.cadi.config.Get;
import org.onap.so.security.cadi.taf.TafResp;
import org.onap.so.security.cadi.taf.TafResp.RESP;
import org.onap.so.security.cadi.util.Timing;

/**
 * CadiFilter
 *
 * This class implements Servlet Filter, and ties together CADI implementations
 *
 * This class can be used in a standard J2EE Servlet manner. Optimal usage is for POJO operations, where one can enforce
 * this Filter being first and primary. Depending on the Container, it may be more effective, in some cases, to utilize
 * features that allow earlier determination of AUTHN (Authorization). An example would be "Tomcat Valve". These
 * implementations, however, should be modeled after the "init" and "doFilter" functions, and be kept up to date as this
 * class changes.
 *
 *
 * @author Jonathan
 *
 */
public class CadiFilter implements Filter {
    private static CadiHTTPManip httpChecker;
    private static String[] pathExceptions;
    private static List<Pair> mapPairs;
    private Access access;
    private Object[] additionalTafLurs;
    private SideChain sideChain;
    private static int count = 0;

    public Lur getLur() {
        return httpChecker.getLur();
    }

    /**
     * Construct a viable Filter
     *
     * Due to the vagaries of many containers, there is a tendency to create Objects and call "Init" on them at a later
     * time. Therefore, this object creates with an object that denies all access until appropriate Init happens, just
     * in case the container lets something slip by in the meantime.
     *
     */
    public CadiFilter() {
        additionalTafLurs = CadiHTTPManip.noAdditional;
    }

    /**
     * This constructor to be used when directly constructing and placing in HTTP Engine
     *
     * @param access
     * @param moreTafLurs
     * @throws ServletException
     */
    public CadiFilter(Access access, Object... moreTafLurs) throws ServletException {
        additionalTafLurs = moreTafLurs;
        init(new AccessGetter(this.access = access));
    }


    /**
     * Use this to pass in a PreContructed CADI Filter, but with initializing... let Servlet do it
     * 
     * @param init
     * @param access
     * @param moreTafLurs
     * @throws ServletException
     */
    public CadiFilter(boolean init, PropAccess access, Object... moreTafLurs) throws ServletException {
        this.access = access;
        additionalTafLurs = moreTafLurs;
        if (init) {
            init(new AccessGetter(access));
        }
    }

    /**
     * Init
     *
     * Standard Filter "init" call with FilterConfig to obtain properties. POJOs can construct a FilterConfig with the
     * mechanism of their choice, and standard J2EE Servlet engines utilize this mechanism already.
     */
    // TODO Always validate changes against Tomcat AbsCadiValve and Jaspi CadiSAM Init functions
    public void init(FilterConfig filterConfig) throws ServletException {
        // need the Context for Logging, instantiating ClassLoader, etc
        ServletContextAccess sca = new ServletContextAccess(filterConfig);
        if (access == null) {
            access = sca;
        }

        // Set Protected getter with base Access, for internal class instantiations
        init(new FCGet(access, sca.context(), filterConfig));
    }


    @SuppressWarnings("unchecked")
    protected void init(Get getter) throws ServletException {
        sideChain = new SideChain();
        // Start with the assumption of "Don't trust anyone".
        TrustChecker tc = TrustChecker.NOTRUST; // default position
        try {
            Class<TrustChecker> ctc =
                    (Class<TrustChecker>) Class.forName("org.onap.so.security.cadi.aaf.v2_0.AAFTrustChecker");
            if (ctc != null) {
                Constructor<TrustChecker> contc = ctc.getConstructor(Access.class);
                if (contc != null) {
                    tc = contc.newInstance(access);
                }
            }
        } catch (Exception e) {
            access.log(Level.INIT, "AAFTrustChecker cannot be loaded", e.getMessage());
        }

        try {
            Class<Filter> cf = null;
            try {
                cf = (Class<Filter>) Class.forName("org.onap.so.security.cadi.oauth.OAuthFilter");
                sideChain.add(cf.newInstance());
            } catch (ClassNotFoundException e) {
                access.log(Level.DEBUG, "OAuthFilter not enabled");
            }
        } catch (Exception e) {
            access.log(Level.INIT, "AAFTrustChecker cannot be loaded", e.getMessage());
        }


        // Synchronize, because some instantiations call init several times on the same object
        // In this case, the epiTaf will be changed to a non-NullTaf, and thus not instantiate twice.
        synchronized (CadiHTTPManip.noAdditional /* will always remain same Object */) {
            ++count;
            if (httpChecker == null) {
                if (access == null) {
                    access = new PropAccess();
                }
                try {
                    httpChecker = new CadiHTTPManip(access, null /* reuseable Con */, tc, additionalTafLurs);
                } catch (CadiException | LocatorException e1) {
                    throw new ServletException(e1);
                }
            } else if (access == null) {
                access = httpChecker.getAccess();
            }

            /*
             * Setup Authn Path Exceptions
             */
            if (pathExceptions == null) {
                String str = getter.get(Config.CADI_NOAUTHN, null, true);
                if (str != null) {
                    pathExceptions = str.split("\\s*:\\s*");
                }
            }

            /*
             * SETUP Permission Converters... those that can take Strings from a Vendor Product, and convert to
             * appropriate AAF Permissions
             */
            if (mapPairs == null) {
                String str = getter.get(Config.AAF_PERM_MAP, null, true);
                if (str != null) {
                    String mstr = getter.get(Config.AAF_PERM_MAP, null, true);
                    if (mstr != null) {
                        String map[] = mstr.split("\\s*:\\s*");
                        if (map.length > 0) {
                            MapPermConverter mpc = null;
                            int idx;
                            mapPairs = new ArrayList<>();
                            for (String entry : map) {
                                if ((idx = entry.indexOf('=')) < 0) { // it's a Path, so create a new converter
                                    access.log(Level.INIT, "Loading Perm Conversions for:", entry);
                                    mapPairs.add(new Pair(entry, mpc = new MapPermConverter()));
                                } else {
                                    if (mpc != null) {
                                        mpc.map().put(entry.substring(0, idx), entry.substring(idx + 1));
                                    } else {
                                        access.log(Level.ERROR, "cadi_perm_map is malformed; ", entry, "is skipped");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add API Enforcement Point
        String enforce = getter.get(Config.CADI_API_ENFORCEMENT, null, true);
        if (enforce != null && enforce.length() > 0) {
            sideChain.add(new CadiApiEnforcementFilter(access, enforce));
        }
        // Remove Getter
        getter = Get.NULL;
    }

    /**
     * Containers call "destroy" when time to cleanup
     */
    public void destroy() {
        // Synchronize, in case multiCadiFilters are used.
        synchronized (CadiHTTPManip.noAdditional) {
            if (--count <= 0 && httpChecker != null) {
                httpChecker.destroy();
                httpChecker = null;
                access = null;
                pathExceptions = null;
            }
        }
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
        final long startAll = System.nanoTime();
        long startCode, startValidate;
        float code = 0f, validate = 0f;
        String user = "n/a";
        String tag = "";
        TafResp tresp = null;
        try {
            HttpServletRequest hreq = (HttpServletRequest) request;
            if (noAuthn(hreq)) {
                startCode = System.nanoTime();
                chain.doFilter(request, response);
                code = Timing.millis(startCode);
            } else {
                HttpServletResponse hresp = (HttpServletResponse) response;
                startValidate = System.nanoTime();
                tresp = httpChecker.validate(hreq, hresp, hreq);
                validate = Timing.millis(startValidate);
                if (tresp.isAuthenticated() == RESP.IS_AUTHENTICATED) {
                    user = tresp.getPrincipal().personalName();
                    tag = tresp.getPrincipal().tag();
                    CadiWrap cw = new CadiWrap(hreq, tresp, httpChecker.getLur(), getConverter(hreq));
                    if (httpChecker.notCadi(cw, hresp)) {
                        startCode = System.nanoTime();
                        sideChain.doFilter(cw, response, chain);
                        code = Timing.millis(startCode);
                    }
                }
            }
        } catch (ClassCastException e) {
            throw new ServletException("CadiFilter expects Servlet to be an HTTP Servlet", e);
        } finally {
            if (tresp != null) {
                access.printf(Level.INFO, "Trans: user=%s[%s],ip=%s,ms=%f,validate=%f,code=%f,result=%s", user, tag,
                        request.getRemoteAddr(), Timing.millis(startAll), validate, code,
                        tresp.isAuthenticated().toString());
            } else {
                access.printf(Level.INFO, "Trans: user=%s[%s],ip=%s,ms=%f,validate=%f,code=%f,result=FAIL", user, tag,
                        request.getRemoteAddr(), Timing.millis(startAll), validate, code);
            }
        }
    }


    /**
     * If PathExceptions exist, report if these should not have Authn applied.
     * 
     * @param hreq
     * @return
     */
    private boolean noAuthn(HttpServletRequest hreq) {
        if (pathExceptions != null) {
            String pi = hreq.getPathInfo();
            if (pi == null) {
                // Attempt to get from URI only (Daniel Rose)
                pi = hreq.getRequestURI().substring(hreq.getContextPath().length());
                if (pi == null) {
                    // Nothing works.
                    return false; // JBoss sometimes leaves null
                }
            }
            for (String pe : pathExceptions) {
                if (pi.startsWith(pe))
                    return true;
            }
        }
        return false;
    }

    /**
     * Get Converter by Path
     */
    private PermConverter getConverter(HttpServletRequest hreq) {
        if (mapPairs != null) {
            String pi = hreq.getPathInfo();
            if (pi != null) {
                for (Pair p : mapPairs) {
                    if (pi.startsWith(p.name))
                        return p.pc;
                }
            }
        }
        return NullPermConverter.singleton();
    }

    /**
     * store PermConverters by Path prefix
     * 
     * @author Jonathan
     *
     */
    private class Pair {
        public Pair(String key, PermConverter pc) {
            name = key;
            this.pc = pc;
        }

        public String name;
        public PermConverter pc;
    }

}

