/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;

import static org.junit.Assert.assertNotNull;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.configuration.HttpComponentsClientConfiguration;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * @author Raviteja Karumuri (raviteja.karumuri@est.tech)
 */
@RunWith(MockitoJUnitRunner.class)
public class CnfmHttpServiceConfigurationTest {

    @Mock
    private HttpComponentsClientConfiguration httpComponentsClientConfiguration;
    @Mock
    private CloseableHttpClient httpclient;

    @Test
    public void cnfmHttpRestServiceProvider_NotNull() {

        final CnfmHttpServiceConfiguration objForTest = new CnfmHttpServiceConfiguration();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpclient);
        Mockito.when(httpComponentsClientConfiguration.httpComponentsClientHttpRequestFactory())
                .thenReturn(requestFactory);
        final HttpRestServiceProvider returnedValue = objForTest
                .cnfmHttpRestServiceProvider(objForTest.cnfmRestTemplateBean(httpComponentsClientConfiguration));
        assertNotNull(returnedValue);
    }
}
