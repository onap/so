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

package org.onap.so.security.cadi.lur;

import java.security.Principal;
import java.util.List;
import org.onap.so.security.cadi.Lur;
import org.onap.so.security.cadi.Permission;

public class NullLur implements Lur {
    private static final Permission NULL = new Permission() {
        @Override
        public String permType() {
            return "";
        }

        @Override
        public String getKey() {
            return "";
        }

        @Override
        public boolean match(Permission p) {
            return false;
        }
    };

    public boolean fish(Principal bait, Permission... pond) {
        // Well, for Jenkins, this is ok... It finds out it can't do J2EE Security, and then looks at it's own
        // System.err.println("CADI's LUR has not been configured, but is still being called. Access is being denied");
        return false;
    }

    public void fishAll(Principal bait, List<Permission> permissions) {}

    public void destroy() {}

    public boolean handlesExclusively(Permission... pond) {
        return false;
    }

    public boolean handles(Principal p) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.Lur#createPerm(java.lang.String)
     */
    @Override
    public Permission createPerm(String p) {
        return NULL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.Lur#clear(java.security.Principal, java.lang.StringBuilder)
     */
    @Override
    public void clear(Principal p, StringBuilder report) {
        report.append(NullLur.class.getSimpleName());
        report.append('\n');
    }

    public String toString() {
        return NullLur.class.getSimpleName() + '\n';
    }
}
