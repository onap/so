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

package org.onap.so.security.cadi.taf;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.CachedPrincipal;
import org.onap.so.security.cadi.CachedPrincipal.Resp;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.Locator;
import org.onap.so.security.cadi.Taf.LifeForm;
import org.onap.so.security.cadi.TrustChecker;

/**
 * HttpEpiTaf
 *
 * An extension of the basic "EpiTAF" concept, check known HTTP Related TAFs for valid credentials
 *
 * @author Jonathan
 *
 */
public class HttpEpiTaf implements HttpTaf {
    private HttpTaf[] tafs;
    private Access access;
    private Locator<URI> locator;
    private TrustChecker trustChecker;

    /**
     * HttpEpiTaf constructor
     *
     * Construct the HttpEpiTaf from variable Http specific TAF parameters
     * 
     * @param tafs
     * @throws CadiException
     */
    public HttpEpiTaf(Access access, Locator<URI> locator, TrustChecker tc, HttpTaf... tafs) throws CadiException {
        this.tafs = tafs;
        this.access = access;
        this.locator = locator;
        this.trustChecker = tc;
        // Establish what Header Property to look for UserChain/Trust Props

        if (tafs.length == 0) {
            throw new CadiException("Need at least one HttpTaf implementation in constructor");
        }
    }

    /**
     * validate
     *
     * Respond with the first Http specific TAF to authenticate user based on variable info and "LifeForm" (is it a
     * human behind a browser, or a server utilizing HTTP Protocol).
     *
     * If there is no HttpTAF that can authenticate, respond with the first TAF that suggests it can establish an
     * Authentication conversation (TRY_AUTHENTICATING) (Examples include a redirect to CSP Servers for CSP Cookie, or
     * BasicAuth 401 response, suggesting User/Password for given Realm submission
     *
     * If no TAF declares either, respond with NullTafResp (which denies all questions)
     */
    public TafResp validate(LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
        // Given a LifeForm Neutral, for HTTP, we need to discover true Life-Form Readings
        if (reading == LifeForm.LFN) {
            reading = tricorderScan(req);
        }
        TafResp tresp = null;
        TafResp firstTry = null;
        List<Redirectable> redirectables = null;
        List<TafResp> log;
        if (access.willLog(Level.DEBUG)) {
            log = new ArrayList<>();
        } else {
            log = null;
        }
        try {
            for (HttpTaf taf : tafs) {
                final long start = System.nanoTime();
                tresp = taf.validate(reading, req, resp);
                addToLog(log, tresp, start);
                switch (tresp.isAuthenticated()) {
                    case TRY_ANOTHER_TAF:
                        break; // and loop
                    case TRY_AUTHENTICATING:
                        if (tresp instanceof Redirectable) {
                            if (redirectables == null) {
                                redirectables = new ArrayList<>();
                            }
                            redirectables.add((Redirectable) tresp);
                        } else if (firstTry == null) {
                            firstTry = tresp;
                        }
                        break;
                    case IS_AUTHENTICATED:
                        tresp = trustChecker.mayTrust(tresp, req);
                        return tresp;
                    default:
                        return tresp;
                }
            }
        } finally {
            printLog(log);
        }

        // If No TAFs configured, at this point. It is safer at this point to be "not validated",
        // rather than "let it go"
        // Note: if exists, there will always be more than 0 entries, according to above code
        if (redirectables == null) {
            return (firstTry != null) ? firstTry : NullTafResp.singleton();
        }

        // If there is one Tryable entry then return it
        if (redirectables.size() > 1) {
            return LoginPageTafResp.create(access, locator, resp, redirectables);
        } else {
            return redirectables.get(0);
        }
    }

    public boolean revalidate(Principal prin) throws Exception {
        return false;
    }

    /*
     * Since this is internal, we use a little Star Trek humor to indicate looking in the HTTP Request to see if we can
     * determine what kind of "LifeForm" reading we can determine, i.e. is there a Human (CarbonBasedLifeForm) behind a
     * browser, or is it mechanical id (SiliconBasedLifeForm)? This makes a difference in some Authentication, i.e CSP,
     * which doesn't work well for SBLFs
     */
    private LifeForm tricorderScan(HttpServletRequest req) {
        // For simplicity's sake, we'll say Humans use FQDNs, not IPs.

        // Current guess that only Browsers bother to set "Agent" codes that identify the kind of browser they are.
        // If mechanical frameworks are found that populate this, then more advanced analysis may be required
        // Jonathan 1/22/2013
        String agent = req.getHeader("User-Agent");
        if (agent != null && agent.startsWith("Mozilla")) { // covers I.E./Firefox/Safari/probably any other "advanced"
                                                            // Browser see http://en.wikipedia.org/wiki/User_agent
            return LifeForm.CBLF;
        }
        return LifeForm.SBLF; // notably skips "curl","wget", (which is desired behavior. We don't want to try CSP, etc
                              // on these)
    }

    public Resp revalidate(CachedPrincipal prin, Object state) {
        Resp resp;
        for (HttpTaf taf : tafs) {
            resp = taf.revalidate(prin, state);
            if (resp != Resp.NOT_MINE) {
                return resp;
            }
            // switch(resp) {
            // case NOT_MINE:
            // break;
            // default:
            // return resp;
            // }
        }
        return Resp.NOT_MINE;
    }

    private void addToLog(List<TafResp> log, final TafResp tresp, final long start) {
        if (log == null) {
            return;
        }
        tresp.timing(start);
        log.add(tresp);
    }

    private void printLog(List<TafResp> log) {
        if (log == null) {
            return;
        }
        for (TafResp tresp : log) {
            access.printf(Level.DEBUG, "%s: %s, ms=%f", tresp.taf(), tresp.desc(), tresp.timing());
        }
    }

    /**
     * List HttpTafs with their "toString" representations... primarily useful for Debugging in an IDE like Eclipse.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (HttpTaf ht : tafs) {
            sb.append(ht.toString());
            sb.append(". ");
        }
        return sb.toString();
    }
}
