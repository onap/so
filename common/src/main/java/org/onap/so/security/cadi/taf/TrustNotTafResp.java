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
import org.onap.so.security.cadi.util.Timing;

public class TrustNotTafResp implements TafResp {
    private final TafResp delegate;
    private final String desc;
    private float timing;

    public TrustNotTafResp(final TafResp delegate, final String desc) {
        this.delegate = delegate;
        this.desc = desc;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public String desc() {
        return desc;
    }

    @Override
    public RESP isAuthenticated() {
        return RESP.NO_FURTHER_PROCESSING;
    }

    @Override
    public RESP authenticate() throws IOException {
        return RESP.NO_FURTHER_PROCESSING;
    }

    @Override
    public TaggedPrincipal getPrincipal() {
        return delegate.getPrincipal();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.taf.TafResp#getTarget()
     */
    @Override
    public String getTarget() {
        return delegate.getTarget();
    }

    @Override
    public Access getAccess() {
        return delegate.getAccess();
    }

    @Override
    public boolean isFailedAttempt() {
        return true;
    }

    @Override
    public float timing() {
        return timing;
    }

    @Override
    public void timing(long start) {
        timing = Timing.millis(start);
    }

    @Override
    public String toString() {
        return desc();
    }

    @Override
    public String taf() {
        return "TrustNot";
    }

}
