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

package org.onap.aaiclient.client.aai;

import java.net.URI;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriBuilder;
import org.javatuples.Pair;
import org.onap.aaiclient.client.graphinventory.GraphInventoryClient;
import org.onap.aaiclient.client.graphinventory.exceptions.GraphInventoryUriComputationException;
import org.onap.so.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAIClient extends GraphInventoryClient {

    private static final String AAI_ROOT = "/aai";
    protected static Logger logger = LoggerFactory.getLogger(AAIClient.class);
    protected AAIVersion version;

    protected AAIClient() {
        super(AAIProperties.class, new MultivaluedHashMap<>());
    }

    protected AAIClient(AAIVersion version) {
        super(AAIProperties.class, new MultivaluedHashMap<>());
        this.version = version;
    }

    protected AAIClient(MultivaluedMap<String, Pair<String, String>> additionalHeaders) {
        super(AAIProperties.class, additionalHeaders);
    }

    protected AAIClient(AAIVersion version, MultivaluedMap<String, Pair<String, String>> additionalHeaders) {
        super(AAIProperties.class, additionalHeaders);
        this.version = version;
    }

    @Override
    protected URI constructPath(URI uri) {

        return UriBuilder.fromUri(AAI_ROOT + "/" + this.getVersion().toString() + uri.toString()).build();
    }

    @Override
    protected RestClient createClient(URI uri) {
        try {

            return new AAIRestClient(getRestProperties(), constructPath(uri), additionalHeaders);
        } catch (GraphInventoryUriComputationException | NotFoundException e) {
            logger.debug("failed to construct A&AI uri", e);
            throw e;
        }
    }

    @Override
    public AAIVersion getVersion() {
        if (version == null) {
            return this.<AAIProperties>getRestProperties().getDefaultVersion();
        } else {
            return this.version;
        }
    }


    @Override
    public String getGraphDBName() {
        return "A&AI";
    }
}
