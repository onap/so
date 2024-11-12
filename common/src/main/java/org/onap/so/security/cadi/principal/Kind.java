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

public class Kind {
    public static final char X509 = 'X';
    public static final char OAUTH = 'O';
    public static final char AAF_OAUTH = 'A';
    public static final char BASIC_AUTH = 'B';
    public static final char UNKNOWN = 'U';


    public static char getKind(final Principal principal) {
        Principal check;
        if (principal instanceof TrustPrincipal) {
            check = ((TrustPrincipal) principal).original();
        } else {
            check = principal;
        }
        if (check instanceof X509Principal) {
            return X509;
        }
        if (check instanceof OAuth2FormPrincipal) {
            // Note: if AAF, will turn into 'A'
            return OAUTH;
        }
        if (check instanceof BasicPrincipal) {
            return BASIC_AUTH;
        }
        return UNKNOWN;
    }
}
