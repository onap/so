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

package org.onap.so.security.cadi.taf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.onap.so.security.cadi.CachedPrincipal;
import org.onap.so.security.cadi.Taf.LifeForm;

/**
 * A TAF which is in a specific HTTP environment in which the engine implements jakarta Servlet.
 *
 * Using the Http Request and Response interfaces takes the effort out of implementing in almost any kind of HTTP
 * Container or Engine.
 *
 * @author Jonathan
 *
 */
public interface HttpTaf {
    /**
     * validate
     *
     * Validate the Request, and respond with created TafResp object.
     *
     * @param reading
     * @param req
     * @param resp
     * @return
     */
    public TafResp validate(LifeForm reading, HttpServletRequest req, HttpServletResponse resp);

    /**
     * Re-Validate Credential
     *
     * @param prin
     * @return
     */
    public CachedPrincipal.Resp revalidate(CachedPrincipal prin, Object state);
}
