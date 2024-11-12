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

import java.io.IOException;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.principal.TaggedPrincipal;

/**
 * A Null Pattern for setting responses to "Deny" before configuration is setup.
 * 
 * @author Jonathan
 *
 */
class NullTafResp implements TafResp {
    private NullTafResp() {}

    private static TafResp singleton = new NullTafResp();

    public static TafResp singleton() {
        return singleton;
    }

    public boolean isValid() {
        return false;
    }

    public RESP isAuthenticated() {
        return RESP.NO_FURTHER_PROCESSING;
    }

    public String desc() {
        return "All Authentication denied";
    }

    public RESP authenticate() throws IOException {
        return RESP.NO_FURTHER_PROCESSING;
    }

    public TaggedPrincipal getPrincipal() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.taf.TafResp#getTarget()
     */
    @Override
    public String getTarget() {
        return "unknown";
    }

    public Access getAccess() {
        return Access.NULL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.taf.TafResp#isFailedAttempt()
     */
    public boolean isFailedAttempt() {
        return true;
    }

    @Override
    public float timing() {
        return 0;
    }

    @Override
    public void timing(long start) {}

    @Override
    public String taf() {
        return "NULL";
    }

}
