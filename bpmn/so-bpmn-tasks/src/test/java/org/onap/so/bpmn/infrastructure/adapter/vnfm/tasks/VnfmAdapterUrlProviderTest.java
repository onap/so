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

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.junit.Assert.assertEquals;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.DUMMY_GENERIC_VND_ID;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.EXPECTED_URL;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.getVnfmBasicHttpConfigProvider;

import org.junit.Test;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.VnfmAdapterUrlProvider;


/**
 * @author waqas.ikram@est.tech
 *
 */
public class VnfmAdapterUrlProviderTest {


    @Test
    public void test_getCreateInstantiateUrl_returnValidCreationInstantiationRequest() {
        final VnfmAdapterUrlProvider objUnderTest = new VnfmAdapterUrlProvider(getVnfmBasicHttpConfigProvider());

        final String actual = objUnderTest.getCreateInstantiateUrl(DUMMY_GENERIC_VND_ID);

        assertEquals(EXPECTED_URL, actual);
    }


}
