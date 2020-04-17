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
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.onap.etsi.sol003.adapter.lcm.v1.model.ExternalVirtualLink;

/**
 * @author Waqas Ikram (waqas.ikram@ericsson.com)
 *
 */
public class InputParameterTest {
    @Test
    public void test_putAdditionalParams_addsEntryToExistingMap() {
        final InputParameter objUnderTest = new InputParameter();
        objUnderTest.setAdditionalParams(getMap("name", "value"));
        objUnderTest.putAdditionalParams(getMap("name1", "value1"));

        final Map<String, String> additionalParams = objUnderTest.getAdditionalParams();
        assertEquals(2, additionalParams.size());
        assertTrue(additionalParams.containsKey("name"));
        assertTrue(additionalParams.containsKey("name1"));

    }

    @Test
    public void test_addExtVirtualLinks_adddistinctEntriesToExistingList() {
        final InputParameter objUnderTest = new InputParameter();
        String firstId = UUID.randomUUID().toString();
        String secondId = UUID.randomUUID().toString();
        objUnderTest.setExtVirtualLinks(getExternalVirtualLinkList(firstId));
        objUnderTest.addExtVirtualLinks(getExternalVirtualLinkList(secondId));
        objUnderTest.addExtVirtualLinks(getExternalVirtualLinkList(secondId));
        objUnderTest.addExtVirtualLinks(getExternalVirtualLinkList(secondId));

        final List<ExternalVirtualLink> externalVirtualLinks = objUnderTest.getExtVirtualLinks();
        assertEquals(2, externalVirtualLinks.size());

    }

    private List<ExternalVirtualLink> getExternalVirtualLinkList(final String id) {
        final ExternalVirtualLink externalVirtualLink = new ExternalVirtualLink();
        externalVirtualLink.setId(id);
        final List<ExternalVirtualLink> list = new ArrayList<>();
        list.add(externalVirtualLink);
        return list;
    }

    private Map<String, String> getMap(final String name, final String value) {
        final Map<String, String> map = new HashMap<>();
        map.put(name, value);
        return map;
    }

}
