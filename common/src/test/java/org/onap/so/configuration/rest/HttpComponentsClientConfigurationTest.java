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

package org.onap.so.configuration.rest;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * @author waqas.ikram@est.tech
 */
public class HttpComponentsClientConfigurationTest {

    @Test
    public void test_httpComponentsClientHttpRequestFactory_HttpComponentsClientHttpRequestFactoryNotNull() {
        final HttpClientConnectionConfiguration clientConnectionConfiguration = new HttpClientConnectionConfiguration();
        final HttpComponentsClientConfiguration objUnderTest =
                new HttpComponentsClientConfiguration(clientConnectionConfiguration);

        final HttpComponentsClientHttpRequestFactory factory = objUnderTest.httpComponentsClientHttpRequestFactory();
        assertNotNull(factory);
        assertNotNull(factory.getHttpClient());

    }

}
