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

package org.onap.so.client.adapter.rest;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Optional;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.codec.binary.Base64;
import org.javatuples.Pair;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.client.RestClient;
import org.onap.so.client.policy.CommonObjectMapperProvider;
import org.onap.so.client.policy.JettisonStyleMapperProvider;
import org.onap.so.utils.CryptoUtils;

public class AdapterRestClient extends RestClient {

    private final AdapterRestProperties adapterRestProperties;

    public AdapterRestClient(AdapterRestProperties props, URI uri) {
        super(props, Optional.of(uri));
        this.adapterRestProperties = props;
    }

    public AdapterRestClient(AdapterRestProperties props, URI uri, String accept, String contentType) {
        super(props, Optional.of(uri), accept, contentType);
        this.adapterRestProperties = props;
    }

    @Override
    public ONAPComponents getTargetEntity() {
        return ONAPComponents.OPENSTACK_ADAPTER;
    }

    @Override
    protected void initializeHeaderMap(MultivaluedMap<String, Pair<String, String>> headerMap) {
        headerMap.add("ALL", Pair.with("Authorization",
                this.getBasicAuth(adapterRestProperties.getAuth(), adapterRestProperties.getKey())));
    }

    @Override
    protected CommonObjectMapperProvider getCommonObjectMapperProvider() {
        return new JettisonStyleMapperProvider();
    }

    private String getBasicAuth(String encryptedAuth, String msoKey) {
        if ((encryptedAuth == null || encryptedAuth.isEmpty()) || (msoKey == null || msoKey.isEmpty())) {
            return null;
        }
        try {
            String auth = CryptoUtils.decrypt(encryptedAuth, msoKey);
            byte[] encoded = Base64.encodeBase64(auth.getBytes());
            String encodedString = new String(encoded);
            encodedString = "Basic " + encodedString;
            return encodedString;
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
