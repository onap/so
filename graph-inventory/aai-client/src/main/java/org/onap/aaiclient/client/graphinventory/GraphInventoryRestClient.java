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

package org.onap.aaiclient.client.graphinventory;

import java.net.URI;
import java.util.Optional;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.javatuples.Pair;
import org.onap.aaiclient.client.CacheControlFeature;
import org.onap.aaiclient.client.FlushCache;
import org.onap.logging.filter.base.ONAPComponentsList;
import org.onap.so.client.AddCacheHeaders;
import org.onap.so.client.CacheFactory;
import org.onap.so.client.ResponseExceptionMapper;
import org.onap.so.client.RestClientSSL;
import org.onap.so.client.RestProperties;
import org.onap.so.client.policy.CommonObjectMapperProvider;

public abstract class GraphInventoryRestClient extends RestClientSSL {

    protected static final GraphInventoryCommonObjectMapperProvider standardProvider =
            new GraphInventoryCommonObjectMapperProvider();

    protected final GraphInventoryPatchConverter patchConverter = new GraphInventoryPatchConverter();

    protected GraphInventoryRestClient(RestProperties props, URI uri) {
        super(props, Optional.of(uri));
    }


    protected ClientBuilder enableCaching(ClientBuilder builder) {
        builder.register(new AddCacheHeaders(props.getCacheProperties()));
        builder.register(new FlushCache(props.getCacheProperties()));
        CacheControlFeature cacheControlFeature = new CacheControlFeature();
        cacheControlFeature.setCacheResponseInputStream(true);
        cacheControlFeature.setExpiryPolicyFactory(new CacheFactory(props.getCacheProperties()));
        builder.property("org.onap.aaiclient.client.CacheControlFeature.name",
                props.getCacheProperties().getCacheName());

        builder.register(cacheControlFeature);

        return builder;
    }

    @Override
    public abstract ONAPComponentsList getTargetEntity();

    @Override
    protected abstract void initializeHeaderMap(MultivaluedMap<String, Pair<String, String>> headerMap);

    @Override
    protected abstract Optional<ResponseExceptionMapper> addResponseExceptionMapper();

    @Override
    protected CommonObjectMapperProvider getCommonObjectMapperProvider() {
        return standardProvider;
    }

    @Override
    public Response patch(Object obj) {
        return super.patch(convertToPatchFormat(obj));
    }

    @Override
    public <T> T patch(Object obj, Class<T> resultClass) {
        return super.patch(convertToPatchFormat(obj), resultClass);
    }

    protected GraphInventoryPatchConverter getPatchConverter() {
        return this.patchConverter;
    }

    protected String convertToPatchFormat(Object obj) {
        return getPatchConverter().convertPatchFormat(obj);
    }

}
