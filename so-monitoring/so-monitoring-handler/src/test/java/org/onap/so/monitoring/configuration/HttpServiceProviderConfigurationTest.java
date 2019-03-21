/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
package org.onap.so.monitoring.configuration;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.onap.so.monitoring.configuration.rest.HttpServiceProviderConfiguration;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.springframework.web.client.RestTemplate;

/**
 * @author waqas.ikram@ericsson.com
 *
 */
public class HttpServiceProviderConfigurationTest {

    private final HttpServiceProviderConfiguration objUnderTest = new HttpServiceProviderConfiguration();
    private static final String AUTHORIZATION =
            "Basic YWRtaW46S3A4Yko0U1hzek0wV1hsaGFrM2VIbGNzZTJnQXc4NHZhb0dHbUp2VXkyVQ==";

    @Test
    public void test_CamundaHttpRestServiceProvider_NotNull() {

        final HttpRestServiceProvider serviceProvider =
                objUnderTest.camundaHttpRestServiceProvider(new RestTemplate(), AUTHORIZATION);

        assertNotNull(serviceProvider);
    }

    @Test
    public void test_DatabaseHttpRestServiceProvider_NotNull() {

        final HttpRestServiceProvider serviceProvider =
                objUnderTest.databaseHttpRestServiceProvider(new RestTemplate(), AUTHORIZATION);

        assertNotNull(serviceProvider);
    }

    @Test
    public void test_DatabaseHttpRestServiceProviderWithAuthorizationNullOrEmpty_NotNull() {

        HttpRestServiceProvider serviceProvider =
                objUnderTest.databaseHttpRestServiceProvider(new RestTemplate(), null);

        assertNotNull(serviceProvider);

        serviceProvider = objUnderTest.databaseHttpRestServiceProvider(new RestTemplate(), "");

        assertNotNull(serviceProvider);
    }

}
