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

package org.onap.so.client;

import java.net.MalformedURLException;
import java.net.URL;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.javatuples.Pair;

public interface RestProperties {

    public URL getEndpoint() throws MalformedURLException;

    public String getSystemName();

    public default Integer getRetries() {
        return Integer.valueOf(2);
    }

    public default Long getDelayBetweenRetries() {
        return Long.valueOf(500);
    }

    public default boolean mapNotFoundToEmpty() {
        return false;
    }

    /**
     * Time in milliseconds
     * 
     * @return
     */
    public default Long getReadTimeout() {
        return Long.valueOf(60000);
    }

    /**
     * Time in milliseconds
     * 
     * @return
     */
    public default Long getConnectionTimeout() {
        return Long.valueOf(60000);
    }

    public default boolean isCachingEnabled() {
        return false;
    }

    public default CacheProperties getCacheProperties() {
        return new CacheProperties() {};
    }

    public default MultivaluedMap<String, Pair<String, String>> additionalHeaders() {
        return new MultivaluedHashMap<>();
    }
}
