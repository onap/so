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
import org.junit.Test;

public class ConfigurationTest {
    private Configuration configuration = new Configuration();

    @Test
    public void testConfigurationTest() {
        configuration.setId("id");
        configuration.setName("name");
        configuration.setType("type");
        configuration.setOrchestrationStatus("orchestrationStatus");
        configuration.setTunnelBandwidth("tunnelBandwidth");
        configuration.setVendorAllowedMaxBandwidth("vendorAllowedMaxBandwidth");
        configuration.setResourceVersion("resourceVersion");
        assertEquals(configuration.getId(), "id");
        assertEquals(configuration.getName(), "name");
        assertEquals(configuration.getType(), "type");
        assertEquals(configuration.getOrchestrationStatus(), "orchestrationStatus");
        assertEquals(configuration.getTunnelBandwidth(), "tunnelBandwidth");
        assertEquals(configuration.getVendorAllowedMaxBandwidth(), "vendorAllowedMaxBandwidth");
        assertEquals(configuration.getResourceVersion(), "resourceVersion");
    }

}
