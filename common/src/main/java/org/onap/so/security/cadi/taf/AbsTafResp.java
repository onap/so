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

import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.principal.TaggedPrincipal;
import org.onap.so.security.cadi.util.Timing;

/**
 * AbsTafResp
 *
 * Base class for TafResp (TAF Response Objects)
 *
 * @author Jonathan
 *
 */
public abstract class AbsTafResp implements TafResp {

    protected final Access access;
    protected final String tafName;
    // Note: Valid Resp is based on Principal being non-null
    protected final TaggedPrincipal principal;
    protected final String target;
    protected final String desc;
    private float timing;

    /**
     * AbsTafResp
     *
     * Set and hold Description (for logging) Principal (as created by derived class) Access (for access to underlying
     * container, i.e. for Logging, auditing, ClassLoaders, etc)
     *
     * @param access
     * @param tafname
     * @param principal
     * @param description
     */
    public AbsTafResp(Access access, String tafname, TaggedPrincipal principal, String description) {
        this.access = access;
        this.tafName = tafname;
        this.principal = principal;
        this.target = principal == null ? "unknown" : principal.getName();
        this.desc = description;
    }

    /**
     * AbsTafResp
     *
     * Set and hold Description (for logging) Principal (as created by derived class) Access (for access to underlying
     * container, i.e. for Logging, auditing, ClassLoaders, etc)
     *
     * @param access
     * @param tafname
     * @param principal
     * @param description
     */
    public AbsTafResp(Access access, String tafname, String target, String description) {
        this.access = access;
        this.tafName = tafname;
        this.principal = null;
        this.target = target;
        this.desc = description;
    }

    /**
     * isValid()
     *
     * Respond in the affirmative if the TAF was able to Authenticate
     */
    public boolean isValid() {
        return principal != null;
    }

    /**
     * desc()
     *
     * Respond with description of response as given by the TAF
     */
    public String desc() {
        return desc;
    }

    /**
     * isAuthenticated()
     *
     * Respond with the TAF's code of whether Authenticated, or suggested next steps default is either IS_AUTHENTICATED,
     * or TRY_ANOTHER_TAF. The TAF can overload and suggest others, such as "NO_FURTHER_PROCESSING", if it can detect
     * that this is some sort of security breach (i.e. Denial of Service)
     */
    public RESP isAuthenticated() {
        return principal == null ? RESP.TRY_ANOTHER_TAF : RESP.IS_AUTHENTICATED;
    }

    /**
     * getPrincipal()
     *
     * Return the principal created by the TAF based on Authentication.
     *
     * Returns "null" if Authentication failed (no principal)
     */
    public TaggedPrincipal getPrincipal() {
        return principal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.taf.TafResp#getTarget()
     */
    @Override
    public String getTarget() {
        return target;
    }

    /**
     * getAccess()
     *
     * Get the Access object from the TAF, so that appropriate Logging, etc can be coordinated.
     */
    public Access getAccess() {
        return access;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.taf.TafResp#isFailedAttempt()
     */
    public boolean isFailedAttempt() {
        return false;
    }

    @Override
    public float timing() {
        return timing;
    }

    @Override
    public void timing(final long start) {
        timing = Timing.millis(start);
    }

    @Override
    public String taf() {
        return tafName;
    }

}
