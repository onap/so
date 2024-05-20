/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Nokia.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.codec.binary.Base64;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.client.policy.JettisonStyleMapperProvider;
import org.onap.so.utils.CryptoUtils;

public class AdapterRestClientTest {

    private static final String CRYPTO_KEY = "546573746F736973546573746F736973";
    private static final String INVALID_CRYPTO_KEY = "1234";

    private MultivaluedMap<String, Pair<String, String>> headerMap;
    private AdapterRestProperties adapterRestPropertiesMock;

    @Before
    public void setup() {
        headerMap = new MultivaluedHashMap<>();

        adapterRestPropertiesMock = mock(AdapterRestProperties.class);
    }

    @Test
    public void initializeHeaderMap_success() throws URISyntaxException, GeneralSecurityException {
        // given
        String encyptedMessage = CryptoUtils.encrypt("testAdapter", CRYPTO_KEY);
        when(adapterRestPropertiesMock.getAuth()).thenReturn(encyptedMessage);
        when(adapterRestPropertiesMock.getKey()).thenReturn(CRYPTO_KEY);
        AdapterRestClient testedObject = new AdapterRestClient(adapterRestPropertiesMock, new URI(""));
        // when
        testedObject.initializeHeaderMap(headerMap);
        // then
        assertThat(headerMap.get("ALL"))
                .containsOnly(Pair.with("Authorization", getExpectedEncodedString(encyptedMessage)));
    }

    @Test
    public void initializeHeaderMap_putNullToMapWhenAuthIsNull() throws URISyntaxException {
        // given
        AdapterRestClient testedObject = new AdapterRestClient(adapterRestPropertiesMock, new URI(""));
        // when
        testedObject.initializeHeaderMap(headerMap);
        // then
        assertThat(headerMap.get("ALL")).containsOnly(Pair.with("Authorization", null));
    }

    @Test
    public void initializeHeaderMap_putNullToMapWhenExOccurs() throws URISyntaxException, GeneralSecurityException {
        // given
        String encyptedMessage = CryptoUtils.encrypt("testAdapter", CRYPTO_KEY);
        when(adapterRestPropertiesMock.getAuth()).thenReturn(encyptedMessage);
        when(adapterRestPropertiesMock.getKey()).thenReturn(INVALID_CRYPTO_KEY);
        AdapterRestClient testedObject =
                new AdapterRestClient(adapterRestPropertiesMock, new URI(""), "accept", "contentType");
        // when
        testedObject.initializeHeaderMap(headerMap);
        // then
        assertThat(headerMap.get("ALL")).containsOnly(Pair.with("Authorization", null));
    }

    @Test
    public void getONAPComponents_success() throws URISyntaxException {
        AdapterRestClient testedObject = new AdapterRestClient(adapterRestPropertiesMock, new URI(""));
        assertThat(testedObject.getTargetEntity()).isEqualTo(ONAPComponents.OPENSTACK_ADAPTER);
    }

    @Test
    public void getCommonObjectMapperProvider_success() throws URISyntaxException {
        AdapterRestClient testedObject = new AdapterRestClient(adapterRestPropertiesMock, new URI(""));
        assertThat(testedObject.getCommonObjectMapperProvider()).isInstanceOf(JettisonStyleMapperProvider.class);
    }

    private String getExpectedEncodedString(String encryptedMessage) throws GeneralSecurityException {
        String auth = CryptoUtils.decrypt(encryptedMessage, CRYPTO_KEY);
        byte[] encoded = Base64.encodeBase64(auth.getBytes());
        String encodedString = new String(encoded);
        return "Basic " + encodedString;
    }
}
