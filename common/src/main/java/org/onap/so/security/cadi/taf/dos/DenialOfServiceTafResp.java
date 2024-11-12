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

package org.onap.so.security.cadi.taf.dos;

import java.io.IOException;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.taf.AbsTafResp;

public class DenialOfServiceTafResp extends AbsTafResp {
    private static final String tafName = DenialOfServiceTaf.class.getSimpleName();

    private RESP ect; // Homage to Arethra Franklin

    public DenialOfServiceTafResp(Access access, RESP resp, String description) {
        super(access, tafName, "dos", description);
        ect = resp;
    }

    // Override base behavior of checking Principal and trying another TAF
    @Override
    public RESP isAuthenticated() {
        return ect;
    }


    public RESP authenticate() throws IOException {
        return ect;
    }

    @Override
    public String taf() {
        return "DOS";
    }

}
