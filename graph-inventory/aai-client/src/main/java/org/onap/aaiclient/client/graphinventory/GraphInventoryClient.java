/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.graphinventory;

import java.net.URI;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.javatuples.Pair;
import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventoryUri;
import org.onap.aaiclient.client.graphinventory.entities.uri.HttpAwareUri;
import org.onap.so.client.RestClient;
import org.onap.so.client.RestProperties;
import org.onap.so.client.RestPropertiesLoader;

public abstract class GraphInventoryClient {

    private RestProperties props;
    protected final MultivaluedMap<String, Pair<String, String>> additionalHeaders;

    protected GraphInventoryClient(Class<? extends RestProperties> propertiesClass,
            MultivaluedMap<String, Pair<String, String>> additionalHeaders) {
        RestProperties props = RestPropertiesLoader.getInstance().getNewImpl(propertiesClass);
        this.props = props;
        this.additionalHeaders = additionalHeaders;
    }

    protected abstract URI constructPath(URI uri);

    protected abstract RestClient createClient(URI uri);

    public RestClient createClient(GraphInventoryUri uri) {
        final URI result;
        if (uri instanceof HttpAwareUri) {
            result = ((HttpAwareUri) uri).locateAndBuild();
        } else {
            result = uri.build();
        }

        return createClient(result);

    }


    public <T extends RestProperties> T getRestProperties() {
        if (props == null) {
            throw new IllegalStateException("No RestProperty implementation found on classpath");
        }
        return (T) props;
    }

    public abstract GraphInventoryVersion getVersion();

    public abstract String getGraphDBName();

    public MultivaluedMap<String, Pair<String, String>> getAdditionalHeaders() {
        return new MultivaluedHashMap<>(this.additionalHeaders);
    }
}
