/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.apihandler.filters;

import java.io.IOException;
import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriInfo;
import org.onap.so.apihandlerinfra.Constants;


@PreMatching
public class RequestUriFilter implements ContainerRequestFilter {

    private String requestURI;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        UriInfo uriInfo = context.getUriInfo();
        URI baseURI = uriInfo.getBaseUri();
        requestURI = uriInfo.getPath();

        if (requestURI.contains("onap/so/infra/serviceInstances")) {
            requestURI = requestURI.replaceFirst("serviceInstances", "serviceInstantiation");
            if (!requestURI.contains(Constants.SERVICE_INSTANCE_PATH)) {
                // Adds /serviceInstances after the version provided in the URI
                requestURI = new StringBuilder(requestURI)
                        .insert(requestURI.indexOf(Constants.SERVICE_INSTANTIATION_PATH) + 24,
                                Constants.SERVICE_INSTANCE_PATH)
                        .toString();
            }
            requestURI = baseURI + requestURI;
            URI uri = URI.create(requestURI);
            context.setRequestUri(uri);
        }
    }

    public String getRequestUri() {
        return requestURI;
    }
}
