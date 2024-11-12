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

/**
 * A Punt Resp to make it fast and easy for a Taf to respond that it cannot handle a particular kind of request. It is
 * always the same object, so there is no cost for memory, etc.
 * 
 * @author Jonathan
 *
 */
public class PuntTafResp implements TafResp {
    private final String name;
    private final String desc;
    private float timing;

    public PuntTafResp(String name, String explanation) {
        this.name = name;
        desc = "Not processing this transaction: " + explanation;
    }

    public boolean isValid() {
        return false;
    }

    public RESP isAuthenticated() {
        return RESP.TRY_ANOTHER_TAF;
    }

    public String desc() {
        return desc;
    }

    public RESP authenticate() throws IOException {
        return RESP.TRY_ANOTHER_TAF;
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
        return "punt";
    }

    public Access getAccess() {
        return NullTafResp.singleton().getAccess();
    }

    public boolean isFailedAttempt() {
        return false;
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
    public String taf() {
        return name;
    }

}
