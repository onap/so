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

package org.onap.so.security.cadi.principal;

import java.security.Principal;
import org.onap.so.security.cadi.UserChain;

public class TrustPrincipal extends BearerPrincipal implements UserChain {
    private final String name;
    private final Principal original;
    private String userChain;

    public TrustPrincipal(final Principal actual, final String asName) {
        this.original = actual;
        name = asName.trim();
        if (actual instanceof UserChain) {
            UserChain uc = (UserChain) actual;
            userChain = uc.userChain();
        } else if (actual instanceof TaggedPrincipal) {
            userChain = ((TaggedPrincipal) actual).tag();
        } else {
            userChain = actual.getClass().getSimpleName();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String userChain() {
        return userChain;
    }

    public Principal original() {
        return original;
    }

    @Override
    public String tag() {
        return userChain;
    }

    @Override
    public String personalName() {
        return original.getName() + '[' + userChain + ']';
    }

}
