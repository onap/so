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

package org.onap.so.client.grm;


import java.net.URI;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import org.javatuples.Pair;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.so.client.RestClient;

public class GRMRestClient extends RestClient {

    private final GRMProperties properties;

    public GRMRestClient(GRMProperties props, URI path) {
        super(props, Optional.of(path));
        this.properties = props;
    }

    @Override
    public ONAPComponents getTargetEntity() {
        return ONAPComponents.GRM;
    }

    @Override
    protected void initializeHeaderMap(MultivaluedMap<String, Pair<String, String>> headerMap) {
        String auth = properties.getAuth();
        String key = properties.getKey();

        if (auth != null && !auth.isEmpty() && key != null && !key.isEmpty()) {
            addBasicAuthHeader(auth, key);
        }
    }

}
