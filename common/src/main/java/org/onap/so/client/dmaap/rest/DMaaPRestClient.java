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

package org.onap.so.client.dmaap.rest;

import java.net.URL;
import java.util.UUID;
import jakarta.ws.rs.core.MultivaluedMap;
import org.javatuples.Pair;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.client.RestClient;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.slf4j.MDC;

public class DMaaPRestClient extends RestClient {

    private static final String SO = "SO";
    private final String auth;
    private final String key;

    public DMaaPRestClient(URL url, String contentType, String auth, String key) {
        super(url, contentType);
        this.auth = auth;
        this.key = key;
    }

    @Override
    public ONAPComponents getTargetEntity() {
        return ONAPComponents.DMAAP;
    }

    @Override
    protected void initializeHeaderMap(MultivaluedMap<String, Pair<String, String>> headerMap) {
        if (auth != null && !auth.isEmpty() && key != null && !key.isEmpty()) {
            addBasicAuthHeader(auth, key);
        }
        String onapRequestId = UUID.randomUUID().toString();
        headerMap.add("ALL", Pair.with(ONAPLogConstants.Headers.REQUEST_ID, onapRequestId));
        if (MDC.get(ONAPLogConstants.MDCs.REQUEST_ID) != null) {
            headerMap.add("ALL",
                    Pair.with(ONAPLogConstants.Headers.INVOCATION_ID, MDC.get(ONAPLogConstants.MDCs.REQUEST_ID)));
        }
    }
}
