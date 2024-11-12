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

package org.onap.so.security.cadi.taf.basic;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.TreeMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.BasicCred;
import org.onap.so.security.cadi.CachedPrincipal;
import org.onap.so.security.cadi.CachedPrincipal.Resp;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.CredVal;
import org.onap.so.security.cadi.CredVal.Type;
import org.onap.so.security.cadi.CredValDomain;
import org.onap.so.security.cadi.Taf;
import org.onap.so.security.cadi.config.Config;
import org.onap.so.security.cadi.filter.MapBathConverter;
import org.onap.so.security.cadi.principal.BasicPrincipal;
import org.onap.so.security.cadi.principal.CachedBasicPrincipal;
import org.onap.so.security.cadi.taf.HttpTaf;
import org.onap.so.security.cadi.taf.TafResp;
import org.onap.so.security.cadi.taf.TafResp.RESP;
import org.onap.so.security.cadi.taf.dos.DenialOfServiceTaf;
import org.onap.so.security.cadi.util.CSV;

/**
 * BasicHttpTaf
 *
 * This TAF implements the "Basic Auth" protocol.
 *
 * WARNING! It is true for any implementation of "Basic Auth" that the password is passed unencrypted. This is because
 * the expectation, when designed years ago, was that it would only be used in conjunction with SSL (https). It is
 * common, however, for users to ignore this on the assumption that their internal network is secure, or just ignorance.
 * Therefore, a WARNING will be printed when the HTTP Channel is not encrypted (unless explicitly turned off).
 *
 * @author Jonathan
 *
 */
public class BasicHttpTaf implements HttpTaf {
    private Access access;
    private String realm;
    private CredVal rbac;
    private Map<String, CredVal> rbacs = new TreeMap<>();
    private boolean warn;
    private long timeToLive;
    private MapBathConverter mapIds;

    public BasicHttpTaf(Access access, CredVal rbac, String realm, long timeToLive, boolean turnOnWarning) {
        this.access = access;
        this.realm = realm;
        this.rbac = rbac;
        this.warn = turnOnWarning;
        this.timeToLive = timeToLive;
        String csvFile = access.getProperty(Config.CADI_BATH_CONVERT, null);
        if (csvFile == null) {
            mapIds = null;
        } else {
            try {
                mapIds = new MapBathConverter(access, new CSV(access, csvFile));
            } catch (IOException | CadiException e) {
                access.log(e, "Bath Map Conversion is not initialzed (non fatal)");
            }
        }
    }

    public void add(final CredValDomain cvd) {
        rbacs.put(cvd.domain(), cvd);
    }

    /**
     * Note: BasicHttp works for either Carbon Based (Humans) or Silicon Based (machine) Lifeforms.
     * 
     * @see Taf
     */
    public TafResp validate(Taf.LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
        // See if Request implements BasicCred (aka CadiWrap or other), and if User/Pass has already been set separately
        if (req instanceof BasicCred) {
            BasicCred bc = (BasicCred) req;
            if (bc.getUser() != null) { // CadiWrap, if set, makes sure User & Password are both valid, or both null
                if (DenialOfServiceTaf.isDeniedID(bc.getUser()) != null) {
                    return DenialOfServiceTaf.respDenyID(access, bc.getUser());
                }
                CachedBasicPrincipal bp = new CachedBasicPrincipal(this, bc, realm, timeToLive);

                // Be able to do Organizational specific lookups by Domain
                CredVal cv = rbacs.get(bp.getDomain());
                if (cv == null) {
                    cv = rbac;
                }

                // ONLY FOR Last Ditch DEBUGGING...
                // access.log(Level.WARN,bp.getName() + ":" + new String(bp.getCred()));
                if (cv.validate(bp.getName(), Type.PASSWORD, bp.getCred(), req)) {
                    return new BasicHttpTafResp(access, bp, bp.getName() + " authenticated by password",
                            RESP.IS_AUTHENTICATED, resp, realm, false);
                } else {
                    // TODO may need timed retries in a given time period
                    return new BasicHttpTafResp(access, bc.getUser(), buildMsg(bp, req, "user/pass combo invalid for ",
                            bc.getUser(), "from", req.getRemoteAddr()), RESP.TRY_AUTHENTICATING, resp, realm, true);
                }
            }
        }
        // Get User/Password from Authorization Header value
        String authz = req.getHeader("Authorization");
        String target = "unknown";

        if (authz != null && authz.startsWith("Basic ")) {
            if (warn && !req.isSecure()) {
                access.log(Level.WARN, "WARNING! BasicAuth has been used over an insecure channel");
            }
            if (mapIds != null) {
                authz = mapIds.convert(access, authz);
            }
            try {
                CachedBasicPrincipal ba = new CachedBasicPrincipal(this, authz, realm, timeToLive);
                target = ba.getName();
                if (DenialOfServiceTaf.isDeniedID(ba.getName()) != null) {
                    return DenialOfServiceTaf.respDenyID(access, ba.getName());
                }

                final int at = ba.getName().indexOf('@');
                CredVal cv = rbacs.get(ba.getName().substring(at + 1));
                if (cv == null) {
                    cv = rbac; // default
                }

                // ONLY FOR Last Ditch DEBUGGING...
                // access.log(Level.WARN,ba.getName() + ":" + new String(ba.getCred()));
                if (cv.validate(ba.getName(), Type.PASSWORD, ba.getCred(), req)) {
                    return new BasicHttpTafResp(access, ba, ba.getName() + " authenticated by BasicAuth password",
                            RESP.IS_AUTHENTICATED, resp, realm, false);
                } else {
                    // TODO may need timed retries in a given time period
                    return new BasicHttpTafResp(access, target, buildMsg(ba, req, "user/pass combo invalid"),
                            RESP.TRY_AUTHENTICATING, resp, realm, true);
                }
            } catch (IOException e) {
                String msg = buildMsg(null, req, "Failed HTTP Basic Authorization (", e.getMessage(), ')');
                access.log(Level.INFO, msg);
                return new BasicHttpTafResp(access, target, msg, RESP.TRY_AUTHENTICATING, resp, realm, true);
            }
        }
        return new BasicHttpTafResp(access, target, "Requesting HTTP Basic Authorization", RESP.TRY_AUTHENTICATING,
                resp, realm, false);
    }

    protected String buildMsg(Principal pr, HttpServletRequest req, Object... msg) {
        StringBuilder sb = new StringBuilder();
        if (pr != null) {
            sb.append("user=");
            sb.append(pr.getName());
            sb.append(',');
        }
        sb.append("ip=");
        sb.append(req.getRemoteAddr());
        sb.append(",port=");
        sb.append(req.getRemotePort());
        if (msg.length > 0) {
            sb.append(",msg=\"");
            for (Object s : msg) {
                sb.append(s.toString());
            }
            sb.append('"');
        }
        return sb.toString();
    }

    public void addCredVal(final String realm, final CredVal cv) {
        rbacs.put(realm, cv);
    }

    public CredVal getCredVal(String key) {
        CredVal cv = rbacs.get(key);
        if (cv == null) {
            cv = rbac;
        }
        return cv;
    }

    @Override
    public Resp revalidate(CachedPrincipal prin, Object state) {
        if (prin instanceof BasicPrincipal) {
            BasicPrincipal ba = (BasicPrincipal) prin;
            if (DenialOfServiceTaf.isDeniedID(ba.getName()) != null) {
                return Resp.UNVALIDATED;
            }
            return rbac.validate(ba.getName(), Type.PASSWORD, ba.getCred(), state) ? Resp.REVALIDATED
                    : Resp.UNVALIDATED;
        }
        return Resp.NOT_MINE;
    }

    public String toString() {
        return "Basic Auth enabled on realm: " + realm;
    }

}
