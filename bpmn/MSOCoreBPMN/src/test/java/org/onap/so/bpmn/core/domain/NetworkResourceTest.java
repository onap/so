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

public class NetworkResourceTest {
    private NetworkResource nr = new NetworkResource();

    @Test
    public void testNetworkResource() {
        nr.setNetworkType("networkType");
        nr.setNetworkRole("networkRole");
        nr.setNetworkTechnology("networkTechnology");
        nr.setNetworkScope("networkScope");
        assertEquals(nr.getNetworkType(), "networkType");
        assertEquals(nr.getNetworkRole(), "networkRole");
        assertEquals(nr.getNetworkTechnology(), "networkTechnology");
        assertEquals(nr.getNetworkScope(), "networkScope");

    }

    @Test
    public void networkResourceMapperTest() throws IOException {
        String jsonStr = "{\"networkScope\": \"code123\", \"resourceInput\": \"sample\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        NetworkResource networkResource = objectMapper.readValue(jsonStr, NetworkResource.class);

        assertTrue(networkResource != null);
    }

}
