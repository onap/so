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

package org.onap.so.security.cadi.util;

import org.onap.so.security.cadi.UserChain;

public class UserChainManip {
    /**
     * Build an element in the correct format for UserChain. Format:<APP>:<ID>:<protocol>[:AS][,<APP>:<ID>:<protocol>]*
     * 
     * @see UserChain
     */
    public static StringBuilder build(StringBuilder sb, String app, String id, UserChain.Protocol proto, boolean as) {
        boolean mayAs;
        if (!(mayAs = sb.length() == 0)) {
            sb.append(',');
        }
        sb.append(app);
        sb.append(':');
        sb.append(id);
        sb.append(':');
        sb.append(proto.name());
        if (as && mayAs) {
            sb.append(":AS");
        }
        return sb;
    }

    public static String idToNS(String id) {
        if (id == null) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            char c;
            int end;
            boolean first = true;
            for (int idx = end = id.length() - 1; idx >= 0; --idx) {
                if ((c = id.charAt(idx)) == '@' || c == '.') {
                    if (idx < end) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append('.');
                        }
                        for (int i = idx + 1; i <= end; ++i) {
                            sb.append(id.charAt(i));
                        }
                    }
                    end = idx - 1;
                    if (c == '@') {
                        break;
                    }
                }
            }
            return sb.toString();
        }
    }
}
