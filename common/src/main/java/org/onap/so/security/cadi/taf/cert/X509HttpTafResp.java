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

package org.onap.so.security.cadi.taf.cert;

import java.io.IOException;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.principal.TaggedPrincipal;
import org.onap.so.security.cadi.taf.AbsTafResp;
import org.onap.so.security.cadi.taf.TafResp;

public class X509HttpTafResp extends AbsTafResp implements TafResp {
    private static final String tafName = X509Taf.class.getSimpleName();

    private RESP status;

    public X509HttpTafResp(Access access, TaggedPrincipal principal, String description, RESP status) {
        super(access, tafName, principal, description);
        this.status = status;
    }

    public RESP authenticate() throws IOException {
        return RESP.TRY_ANOTHER_TAF;
    }

    public RESP isAuthenticated() {
        return status;
    }

    public String toString() {
        return status.name();
    }

}
