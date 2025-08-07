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

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.junit.Assert.assertNotNull;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.getVnfmBasicHttpConfigProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.springframework.web.client.RestTemplate;

/**
 * @author waqas.ikram@est.tech
 */
@RunWith(MockitoJUnitRunner.class)
public class VnfmAdapterCreateVnfTaskConfigurationTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    public void test_databaseHttpRestServiceProvider_httpRestServiceProviderNotNull() {
        final VnfmAdapterCreateVnfTaskConfiguration objUnderTest = new VnfmAdapterCreateVnfTaskConfiguration();

        final HttpRestServiceProvider actual =
                objUnderTest.vnfmHttpRestServiceProvider(restTemplate, getVnfmBasicHttpConfigProvider());
        assertNotNull(actual);


    }

}
