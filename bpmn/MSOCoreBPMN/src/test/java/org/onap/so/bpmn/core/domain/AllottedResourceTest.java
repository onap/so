/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 TechMahindra
 * ================================================================================ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.core.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import java.io.IOException;

public class AllottedResourceTest {
    private AllottedResource ar = new AllottedResource();

    @Test
    public void testAllottedResource() {
        ar.setAllottedResourceType("allottedResourceType");
        ar.setAllottedResourceRole("allottedResourceRole");
        ar.setProvidingServiceModelName("providingServiceModelName");
        ar.setProvidingServiceModelInvariantUuid("providingServiceModelInvariantUuid");
        ar.setProvidingServiceModelUuid("providingServiceModelUuid");
        ar.setNfFunction("nfFunction");
        ar.setNfType("nfType");
        ar.setNfRole("nfRole");
        ar.setNfNamingCode("nfNamingCode");
        ar.setOrchestrationStatus("orchestrationStatus");
        assertEquals(ar.getAllottedResourceType(), "allottedResourceType");
        assertEquals(ar.getAllottedResourceRole(), "allottedResourceRole");
        assertEquals(ar.getProvidingServiceModelName(), "providingServiceModelName");
        assertEquals(ar.getProvidingServiceModelInvariantUuid(), "providingServiceModelInvariantUuid");
        assertEquals(ar.getProvidingServiceModelUuid(), "providingServiceModelUuid");
        assertEquals(ar.getNfFunction(), "nfFunction");
        assertEquals(ar.getNfType(), "nfType");
        assertEquals(ar.getNfRole(), "nfRole");
        assertEquals(ar.getNfNamingCode(), "nfNamingCode");
        assertEquals(ar.getOrchestrationStatus(), "orchestrationStatus");

    }

    @Test
    public void allottedResourceMapperTest() throws IOException {
        String jsonStr = "{\"allottedResourceType\": \"code123\", \"resourceInput\": \"sample\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        AllottedResource vnfResource = objectMapper.readValue(jsonStr, AllottedResource.class);

        assertTrue(vnfResource != null);
    }

}
