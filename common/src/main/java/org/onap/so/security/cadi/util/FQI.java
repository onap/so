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

public class FQI {
    /**
     * Take a Fully Qualified User, and get a Namespace from it.
     * 
     * @param fqi
     * @return
     */
    public final static String reverseDomain(final String fqi) {
        StringBuilder sb = null;
        String[] split = Split.split('.', fqi);
        int at;
        for (int i = split.length - 1; i >= 0; --i) {
            if (sb == null) {
                sb = new StringBuilder();
            } else {
                sb.append('.');
            }

            if ((at = split[i].indexOf('@')) > 0) {
                sb.append(split[i].subSequence(at + 1, split[i].length()));
            } else {
                sb.append(split[i]);
            }
        }

        return sb == null ? "" : sb.toString();
    }

}
