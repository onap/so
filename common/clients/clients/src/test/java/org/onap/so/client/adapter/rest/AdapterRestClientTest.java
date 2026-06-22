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
import java.util.Base64;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.client.policy.JettisonStyleMapperProvider;

public class AdapterRestClientTest {

    private MultivaluedMap<String, Pair<String, String>> headerMap;
    private AdapterRestProperties adapterRestPropertiesMock;

    @Before
    public void setup() {
        headerMap = new MultivaluedHashMap<>();

        adapterRestPropertiesMock = mock(AdapterRestProperties.class);
    }

    @Test
    public void initializeHeaderMap_success() throws URISyntaxException {
        // given
        when(adapterRestPropertiesMock.getAuth()).thenReturn("testAdapter");
        when(adapterRestPropertiesMock.getKey()).thenReturn("");
        AdapterRestClient testedObject = new AdapterRestClient(adapterRestPropertiesMock, new URI(""));
        // when
        testedObject.initializeHeaderMap(headerMap);
        // then
        String expectedAuth = "Basic " + Base64.getEncoder().encodeToString("testAdapter".getBytes());
        assertThat(headerMap.get("ALL")).containsOnly(Pair.with("Authorization", expectedAuth));
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
    public void initializeHeaderMap_putNullToMapWhenAuthIsEmpty() throws URISyntaxException {
        // given
        when(adapterRestPropertiesMock.getAuth()).thenReturn("");
        when(adapterRestPropertiesMock.getKey()).thenReturn("");
        AdapterRestClient testedObject = new AdapterRestClient(adapterRestPropertiesMock, new URI(""), "accept",
                "contentType");
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
}
