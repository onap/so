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
import java.net.HttpURLConnection;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.CadiWrap;
import org.onap.so.security.cadi.Connector;
import org.onap.so.security.cadi.CredVal;
import org.onap.so.security.cadi.LocatorException;
import org.onap.so.security.cadi.Lur;
import org.onap.so.security.cadi.Taf;
import org.onap.so.security.cadi.TrustChecker;
import org.onap.so.security.cadi.config.Config;
import org.onap.so.security.cadi.config.SecurityInfoC;
import org.onap.so.security.cadi.lur.EpiLur;
import org.onap.so.security.cadi.taf.HttpTaf;
import org.onap.so.security.cadi.taf.TafResp;
import org.onap.so.security.cadi.util.UserChainManip;

/**
 * Encapsulate common HTTP Manipulation Behavior. It will appropriately set HTTPServletResponse for Redirect or
 * Forbidden, as needed.
 *
 * Further, this is useful, because it avoids multiple creates of Connections, where some Filters are created and
 * destroyed regularly.
 *
 * @author Jonathan
 *
 */
public class CadiHTTPManip {
    private static final String ACCESS_DENIED = "Access Denied";
    private static final String NO_TAF_WILL_AUTHORIZE = "No TAF will authorize";
    private static final String AUTHENTICATION_FAILURE = "Authentication Failure";
    private static final String AUTHENTICATING_VIA_REDIRECTION = "Authenticating via redirection";
    private static final String MSG_FMT = "user=%s,ip=%s:%d,msg=\"%s: %s\"";
    private static final String AUTHENTICATED = "Authenticated";
    private static final String ACCESS_CADI_CONTROL = ".access|cadi|control";
    private static final String METH = "OPTIONS";
    private static final String CADI = "/cadi/";
    private static final String CADI_CACHE_PRINT = "/cadi/cache/print";
    private static final String CADI_CACHE_CLEAR = "/cadi/cache/clear";
    private static final String CADI_LOG_SET = "/cadi/log/set/";
    private static final Object LOCK = new Object();
    private Access access;
    private HttpTaf taf;
    private CredVal up;
    private Lur lur;
    private String thisPerm, companyPerm, aaf_id;

    public static final Object[] noAdditional = new Object[0]; // CadiFilter can be created each call in some systems


    public CadiHTTPManip(Access access, Connector con, TrustChecker tc, Object... additionalTafLurs)
            throws CadiException, LocatorException {
        synchronized (LOCK) {
            this.access = access;
            // Get getter = new AccessGetter(access);
            Config.setDefaultRealm(access);

            aaf_id = access.getProperty(Config.CADI_ALIAS, access.getProperty(Config.AAF_APPID, null));
            if (aaf_id == null) {
                access.printf(Level.INIT, "%s is not set. %s can be used instead", Config.AAF_APPID, Config.CADI_ALIAS);
            } else {
                access.printf(Level.INIT, "%s is set to %s", Config.AAF_APPID, aaf_id);
            }
            String ns = aaf_id == null ? null : UserChainManip.idToNS(aaf_id);
            if (ns != null) {
                thisPerm = ns + ACCESS_CADI_CONTROL;
                int dot = ns.indexOf('.');
                if (dot >= 0) {
                    int dot2 = ns.indexOf('.', dot + 1);
                    if (dot2 < 0) {
                        dot2 = dot;
                    }
                    companyPerm = ns.substring(0, dot2) + ACCESS_CADI_CONTROL;
                } else {
                    companyPerm = "com" + ACCESS_CADI_CONTROL;
                }
            } else {
                thisPerm = companyPerm = "com" + ACCESS_CADI_CONTROL;
            }
            SecurityInfoC<HttpURLConnection> si;
            si = SecurityInfoC.instance(access, HttpURLConnection.class);

            lur = Config.configLur(si, con, additionalTafLurs);

            tc.setLur(lur);
            if (lur instanceof EpiLur) {
                up = ((EpiLur) lur).getUserPassImpl();
            } else if (lur instanceof CredVal) {
                up = (CredVal) lur;
            } else {
                up = null;
            }
            taf = Config.configHttpTaf(con, si, tc, up, lur, additionalTafLurs);
        }
    }

    public TafResp validate(HttpServletRequest hreq, HttpServletResponse hresp, Object state) throws IOException {
        TafResp tresp = taf.validate(Taf.LifeForm.LFN, hreq, hresp);
        switch (tresp.isAuthenticated()) {
            case IS_AUTHENTICATED:
                access.printf(Level.DEBUG, MSG_FMT, tresp.getTarget(), hreq.getRemoteAddr(), hreq.getRemotePort(),
                        AUTHENTICATED, tresp.desc());
                break;
            case TRY_AUTHENTICATING:
                switch (tresp.authenticate()) {
                    case IS_AUTHENTICATED:
                        access.printf(Level.DEBUG, MSG_FMT, tresp.getTarget(), hreq.getRemoteAddr(),
                                hreq.getRemotePort(), AUTHENTICATED, tresp.desc());
                        break;
                    case HTTP_REDIRECT_INVOKED:
                        access.printf(Level.DEBUG, MSG_FMT, tresp.getTarget(), hreq.getRemoteAddr(),
                                hreq.getRemotePort(), AUTHENTICATING_VIA_REDIRECTION, tresp.desc());
                        break;
                    case NO_FURTHER_PROCESSING:
                        access.printf(Level.AUDIT, MSG_FMT, tresp.getTarget(), hreq.getRemoteAddr(),
                                hreq.getRemotePort(), AUTHENTICATION_FAILURE, tresp.desc());
                        hresp.sendError(403, tresp.desc()); // Forbidden
                        break;

                    default:
                        access.printf(Level.AUDIT, MSG_FMT, tresp.getTarget(), hreq.getRemoteAddr(),
                                hreq.getRemotePort(), NO_TAF_WILL_AUTHORIZE, tresp.desc());
                        hresp.sendError(403, tresp.desc()); // Forbidden
                }
                break;
            case NO_FURTHER_PROCESSING:
                access.printf(Level.AUDIT, MSG_FMT, tresp.getTarget(), hreq.getRemoteAddr(), hreq.getRemotePort(),
                        NO_TAF_WILL_AUTHORIZE, tresp.desc());
                hresp.sendError(403, ACCESS_DENIED); // FORBIDDEN
                break;
            default:
                access.printf(Level.AUDIT, MSG_FMT, tresp.getTarget(), hreq.getRemoteAddr(), hreq.getRemotePort(),
                        NO_TAF_WILL_AUTHORIZE, tresp.desc());
                hresp.sendError(403, ACCESS_DENIED); // FORBIDDEN
        }

        return tresp;
    }

    public boolean notCadi(CadiWrap req, HttpServletResponse resp) {

        String pathInfo = req.getPathInfo();
        if (METH.equalsIgnoreCase(req.getMethod()) && pathInfo != null && pathInfo.contains(CADI)) {
            if (req.getUser().equals(aaf_id) || req.isUserInRole(thisPerm) || req.isUserInRole(companyPerm)) {
                try {
                    if (pathInfo.contains(CADI_CACHE_PRINT)) {
                        resp.getOutputStream().println(lur.toString());
                        resp.setStatus(200);
                        return false;
                    } else if (pathInfo.contains(CADI_CACHE_CLEAR)) {
                        StringBuilder report = new StringBuilder();
                        lur.clear(req.getUserPrincipal(), report);
                        resp.getOutputStream().println(report.toString());
                        resp.setStatus(200);
                        return false;
                    } else if (pathInfo.contains(CADI_LOG_SET)) {
                        Level l;
                        int slash = pathInfo.lastIndexOf('/');
                        String level = pathInfo.substring(slash + 1);
                        try {
                            l = Level.valueOf(level);
                            access.printf(Level.AUDIT, "%s has set CADI Log Level to '%s'", req.getUser(), l.name());
                            access.setLogLevel(l);
                        } catch (IllegalArgumentException e) {
                            access.printf(Level.AUDIT, "'%s' is not a valid CADI Log Level", level);
                        }
                        return false;
                    }
                } catch (IOException e) {
                    access.log(e);
                }
            }
        }
        return true;
    }

    public Lur getLur() {
        return lur;
    }

    public void destroy() {
        access.log(Level.INFO, "CadiHttpChecker destroyed.");
        if (lur != null) {
            lur.destroy();
            lur = null;
        }
    }

    public Access getAccess() {
        return access;
    }

}
