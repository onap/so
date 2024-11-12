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

import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.config.Get;

public class AccessGetter implements Get {
    private final Access access;

    public AccessGetter(Access access) {
        this.access = access;
    }

    public String get(String name, String def, boolean print) {
        return access.getProperty(name, def);
    }

}
