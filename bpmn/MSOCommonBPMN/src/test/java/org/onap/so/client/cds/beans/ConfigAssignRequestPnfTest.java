/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.client.cds.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class ConfigAssignRequestPnfTest {
    ConfigAssignRequestPnf configAssignRequestPnf = new ConfigAssignRequestPnf();
    ConfigAssignPropertiesForPnf configAssignPropertiesForPnf = new ConfigAssignPropertiesForPnf();
    private Map<String, Object> userParam = new HashMap<String, Object>();
    private String resolutionKey;

    @Test
    public final void testConfigAssignRequestPnfTest() {
        configAssignRequestPnf.setResolutionKey("resolution-key");
        configAssignRequestPnf.setConfigAssignPropertiesForPnf(configAssignPropertiesForPnf);
        assertNotNull(configAssignRequestPnf.getResolutionKey());
        assertNotNull(configAssignRequestPnf.getConfigAssignPropertiesForPnf());

        assertEquals("resolution-key", configAssignRequestPnf.getResolutionKey());
        assertEquals(configAssignPropertiesForPnf, configAssignRequestPnf.getConfigAssignPropertiesForPnf());
    }

    @Test
    public void testtoString() {
        userParam.put("Instance1", "instance1value");
        configAssignPropertiesForPnf.setPnfCustomizationUuid("pnf-customization-uuid");
        configAssignPropertiesForPnf.setPnfId("pnf-id");
        configAssignPropertiesForPnf.setPnfName("pnf-name");
        configAssignPropertiesForPnf.setServiceInstanceId("service-instance-id");
        configAssignPropertiesForPnf.setServiceModelUuid("service-model-uuid");
        configAssignPropertiesForPnf.setUserParam("user_params", userParam);
        configAssignRequestPnf.setConfigAssignPropertiesForPnf(configAssignPropertiesForPnf);
        final StringBuilder sb = new StringBuilder("{\"config-assign-request\":{");
        sb.append("\"resolution-key\":").append("\"").append(resolutionKey).append("\"");
        sb.append(", \"config-assign-properties\":").append(configAssignPropertiesForPnf.toString());
        sb.append('}');
        sb.append('}');
        String Expexted = sb.toString();

        assertEquals(Expexted, configAssignRequestPnf.toString());

    }
}
