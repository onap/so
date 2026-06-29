/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.apache.hc.client5.http.config.RequestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author waqas.ikram@est.tech
 */
public class HttpComponentsClientConfigurationTest {

    private HttpClientConnectionConfiguration clientConnectionConfiguration;
    private HttpComponentsClientConfiguration objUnderTest;

    @BeforeEach
    public void setUp() {
        // HttpClientConnectionConfiguration reads its values from @Value-injected properties. Constructed
        // directly in a unit test those fields default to 0, which makes the connection manager reject the
        // eviction timeout, so populate them with the same defaults the production properties use.
        clientConnectionConfiguration = new HttpClientConnectionConfiguration();
        ReflectionTestUtils.setField(clientConnectionConfiguration, "connectionTimeOutInSeconds", 10);
        ReflectionTestUtils.setField(clientConnectionConfiguration, "socketTimeOutInSeconds", 180);
        ReflectionTestUtils.setField(clientConnectionConfiguration, "timeToLiveInSeconds", 600);
        ReflectionTestUtils.setField(clientConnectionConfiguration, "maxConnections", 100);
        ReflectionTestUtils.setField(clientConnectionConfiguration, "maxConnectionsPerRoute", 20);
        ReflectionTestUtils.setField(clientConnectionConfiguration, "evictIdleConnectionsTimeInSec", 5);
        objUnderTest = new HttpComponentsClientConfiguration(clientConnectionConfiguration);
    }

    @Test
    public void test_httpComponentsClientHttpRequestFactory_HttpComponentsClientHttpRequestFactoryNotNull() {
        final HttpComponentsClientHttpRequestFactory factory = objUnderTest.httpComponentsClientHttpRequestFactory();
        assertNotNull(factory);
        assertNotNull(factory.getHttpClient());
    }

    @Test
    public void test_requestConfig_connectAndResponseTimeoutsAreApplied() {
        final RequestConfig requestConfig = objUnderTest.requestConfig();

        // The connect timeout must be set explicitly. Apache HttpClient 5 does not fall back to the
        // connection-request timeout for connection establishment, so leaving it unset would silently
        // apply the HC5 default of 3 minutes instead of the configured 10 seconds.
        assertEquals(10_000L, requestConfig.getConnectTimeout().toMilliseconds());
        assertEquals(180_000L, requestConfig.getResponseTimeout().toMilliseconds());
        assertEquals(10_000L, requestConfig.getConnectionRequestTimeout().toMilliseconds());
    }

}
