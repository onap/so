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

import java.io.IOException;
import org.onap.so.security.cadi.BasicCred;
import org.onap.so.security.cadi.CachedPrincipal;
import org.onap.so.security.cadi.taf.HttpTaf;

/**
 * Cached Principals need to be able to revalidate in the Background
 *
 * @author Jonathan
 *
 */
public class CachedBasicPrincipal extends BasicPrincipal implements CachedPrincipal {
    private final HttpTaf creator;
    private long timeToLive;
    private long expires;

    public CachedBasicPrincipal(HttpTaf creator, BasicCred bc, String domain, long timeToLive) {
        super(bc, domain);
        this.creator = creator;
        this.timeToLive = timeToLive;
        expires = System.currentTimeMillis() + timeToLive;
    }

    public CachedBasicPrincipal(HttpTaf creator, String content, String domain, long timeToLive) throws IOException {
        super(content, domain);
        this.creator = creator;
        this.timeToLive = timeToLive;
        expires = System.currentTimeMillis() + timeToLive;
    }

    public CachedPrincipal.Resp revalidate(Object state) {
        Resp resp = creator.revalidate(this, state);
        if (resp.equals(Resp.REVALIDATED))
            expires = System.currentTimeMillis() + timeToLive;
        return resp;
    }

    public long expires() {
        return expires;
    }

}
