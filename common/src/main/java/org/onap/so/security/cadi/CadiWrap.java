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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.filter.NullPermConverter;
import org.onap.so.security.cadi.filter.PermConverter;
import org.onap.so.security.cadi.lur.EpiLur;
import org.onap.so.security.cadi.principal.TaggedPrincipal;
import org.onap.so.security.cadi.taf.TafResp;
import org.onap.so.security.cadi.util.Timing;



/**
 * Inherit the HttpServletRequestWrapper, which calls methods of delegate it's created with, but overload the key
 * security mechanisms with CADI mechanisms
 *
 * This works with mechanisms working strictly with HttpServletRequest (i.e. Servlet Filters)
 *
 * Specialty cases, i.e. Tomcat, which for their containers utilize their own mechanisms and Wrappers, you may need
 * something similar. See AppServer specific code (i.e. tomcat) for these.
 *
 * @author Jonathan
 *
 */
public class CadiWrap extends HttpServletRequestWrapper implements HttpServletRequest, BasicCred {
    private TaggedPrincipal principal;
    private Lur lur;
    private String user; // used to set user/pass from brain-dead protocols like WSSE
    private byte[] password;
    private PermConverter pconv;
    private Access access;

    /**
     * Standard Wrapper constructor for Delegate pattern
     * 
     * @param request
     */
    public CadiWrap(HttpServletRequest request, TafResp tafResp, Lur lur) {
        super(request);
        principal = tafResp.getPrincipal();
        access = tafResp.getAccess();
        this.lur = lur;
        pconv = NullPermConverter.singleton();
    }

    /**
     * Standard Wrapper constructor for Delegate pattern, with PermConverter
     * 
     * @param request
     */
    public CadiWrap(HttpServletRequest request, TafResp tafResp, Lur lur, PermConverter pc) {
        super(request);
        principal = tafResp.getPrincipal();
        access = tafResp.getAccess();
        this.lur = lur;
        pconv = pc;
    }


    /**
     * Part of the HTTP Security API. Declare the User associated with this HTTP Transaction. CADI does this by
     * reporting the name associated with the Principal obtained, if any.
     */
    @Override
    public String getRemoteUser() {
        return principal == null ? null : principal.getName();
    }

    /**
     * Part of the HTTP Security API. Return the User Principal associated with this HTTP Transaction.
     */
    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    /**
     * This is the key API call for AUTHZ in J2EE. Given a Role (String passed in), is the user associated with this
     * HTTP Transaction allowed to function in this Role?
     *
     * For CADI, we pass the responsibility for determining this to the "LUR", which may be determined by the
     * Enterprise.
     *
     * Note: Role check is also done in "CadiRealm" in certain cases...
     *
     *
     */
    @Override
    public boolean isUserInRole(String perm) {
        return perm == null ? false : checkPerm(access, "isUserInRole", principal, pconv, lur, perm);
    }

    public static boolean checkPerm(Access access, String caller, Principal principal, PermConverter pconv, Lur lur,
            String perm) {
        if (principal == null) {
            access.log(Level.AUDIT, caller, "No Principal in Transaction");
            return false;
        } else {
            final long start = System.nanoTime();
            perm = pconv.convert(perm);
            if (lur.fish(principal, lur.createPerm(perm))) {
                access.printf(Level.DEBUG, "%s: %s has %s, %f ms", caller, principal.getName(), perm,
                        Timing.millis(start));
                return true;
            } else {
                access.printf(Level.DEBUG, "%s: %s does not have %s, %f ms", caller, principal.getName(), perm,
                        Timing.millis(start));
                return false;
            }
        }

    }

    /**
     * CADI Function (Non J2EE standard). GetPermissions will read the Permissions from AAF (if configured) and Roles
     * from Local Lur, etc as implemented with lur.fishAll
     *
     * To utilize, the Request must be a "CadiWrap" object, then call.
     */
    public List<Permission> getPermissions(Principal p) {
        List<Permission> perms = new ArrayList<>();
        lur.fishAll(p, perms);
        return perms;
    }

    /**
     * Allow setting of tafResp and lur after construction
     *
     * This can happen if the CadiWrap is constructed in a Valve other than CadiValve
     */
    public void set(TafResp tafResp, Lur lur) {
        principal = tafResp.getPrincipal();
        access = tafResp.getAccess();
        this.lur = lur;
    }

    public String getUser() {
        if (user == null && principal != null) {
            user = principal.getName();
        }
        return user;
    }

    public byte[] getCred() {
        return password;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setCred(byte[] passwd) {
        password = passwd;
    }

    public CadiWrap setPermConverter(PermConverter pc) {
        pconv = pc;
        return this;
    }

    // Add a feature
    public void invalidate(String id) {
        if (lur instanceof EpiLur) {
            ((EpiLur) lur).remove(id);
        } else if (lur instanceof CachingLur) {
            ((CachingLur<?>) lur).remove(id);
        }
    }

    public Lur getLur() {
        return lur;
    }

    public Access access() {
        return access;
    }
}
