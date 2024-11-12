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

import jakarta.servlet.http.HttpServletRequest;
import org.onap.so.security.cadi.taf.TafResp;

/**
 * Change to another Principal based on Trust of caller and User Chain (if desired)
 *
 * @author Jonathan
 *
 */
public interface TrustChecker {
    public TafResp mayTrust(TafResp tresp, HttpServletRequest req);

    /**
     * A class that trusts no-one else, so just return same TResp
     */
    public static TrustChecker NOTRUST = new TrustChecker() {
        @Override
        public TafResp mayTrust(TafResp tresp, HttpServletRequest req) {
            return tresp;
        }

        @Override
        public void setLur(Lur lur) {}
    };

    public void setLur(Lur lur);
}
