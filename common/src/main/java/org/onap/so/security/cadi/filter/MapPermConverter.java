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

package org.onap.so.security.cadi.filter;

import java.util.HashMap;
import java.util.Map;

public class MapPermConverter implements PermConverter {
    private HashMap<String, String> map;

    /**
     * Create with colon separated name value pairs i.e. teAdmin=com.att.myNS.myPerm|*|*:teUser=...
     *
     * @param value
     */
    public MapPermConverter() {
        map = new HashMap<>();
    }

    /**
     * use to instantiate entries
     *
     * @return
     */
    public Map<String, String> map() {
        return map;
    }

    public String convert(String minimal) {
        String rv = map.get(minimal);
        return (rv == null) ? minimal : rv;
    }

}
