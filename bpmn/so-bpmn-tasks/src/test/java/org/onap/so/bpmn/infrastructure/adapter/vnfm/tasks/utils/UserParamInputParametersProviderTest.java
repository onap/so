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
package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.ADDITIONAL_PARAMS_VALUE;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.EXT_VIRTUAL_LINKS_VALUE;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.EXT_VIRTUAL_LINK_VALUE;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.getUserParamsMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.ExternalVirtualLink;

/**
 * @author Waqas Ikram (waqas.ikram@ericsson.com)
 *
 */
public class UserParamInputParametersProviderTest {

    @Test
    public void testGetInputParameter_ValidUserParams_NotEmptyInputParameter() {
        final InputParametersProvider<Map<String, Object>> objUnderTest = new UserParamInputParametersProvider();

        final InputParameter actual =
                objUnderTest.getInputParameter(getUserParamsMap(ADDITIONAL_PARAMS_VALUE, EXT_VIRTUAL_LINKS_VALUE));
        assertNotNull(actual);

        final Map<String, String> actualAdditionalParams = actual.getAdditionalParams();
        assertEquals(3, actualAdditionalParams.size());

        final String actualInstanceType = actualAdditionalParams.get("instance_type");
        assertEquals("m1.small", actualInstanceType);

        final List<ExternalVirtualLink> actualExtVirtualLinks = actual.getExtVirtualLinks();
        assertEquals(1, actualExtVirtualLinks.size());

        final ExternalVirtualLink actualExternalVirtualLink = actualExtVirtualLinks.get(0);
        assertEquals("ac1ed33d-8dc1-4800-8ce8-309b99c38eec", actualExternalVirtualLink.getId());

    }

    @Test
    public void testGetInputParameter_EmptyOrNullUserParams_EmptyInputParameter() {
        final InputParametersProvider<Map<String, Object>> objUnderTest = new UserParamInputParametersProvider();

        InputParameter actual = objUnderTest.getInputParameter(Collections.emptyMap());
        assertNotNull(actual);
        assertTrue(actual.getAdditionalParams().isEmpty());
        assertTrue(actual.getExtVirtualLinks().isEmpty());

        actual = objUnderTest.getInputParameter(null);
        assertNotNull(actual);
        assertTrue(actual instanceof NullInputParameter);
        assertTrue(actual.getAdditionalParams().isEmpty());
        assertTrue(actual.getExtVirtualLinks().isEmpty());

    }

    @Test
    public void testGetInputParameter_InValidExtVirtualLinks_NotEmptyInputParameter() {
        final InputParametersProvider<Map<String, Object>> objUnderTest = new UserParamInputParametersProvider();

        final InputParameter actual =
                objUnderTest.getInputParameter(getUserParamsMap(ADDITIONAL_PARAMS_VALUE, EXT_VIRTUAL_LINK_VALUE));
        assertNotNull(actual);

        final Map<String, String> actualAdditionalParams = actual.getAdditionalParams();
        assertEquals(3, actualAdditionalParams.size());

        final String actualInstanceType = actualAdditionalParams.get("instance_type");
        assertEquals("m1.small", actualInstanceType);

        assertTrue(actual.getExtVirtualLinks().isEmpty());

    }

}
