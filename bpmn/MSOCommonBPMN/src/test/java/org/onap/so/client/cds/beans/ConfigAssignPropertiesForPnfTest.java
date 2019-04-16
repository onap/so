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

public class ConfigAssignPropertiesForPnfTest {
    ConfigAssignPropertiesForPnf configAssignPropertiesForPnf = new ConfigAssignPropertiesForPnf();
    private Map<String, Object> userParam = new HashMap<String, Object>();
    private String serviceInstanceId;
    private String pnfId;
    private String pnfName;
    private String serviceModelUuid;
    private String pnfCustomizationUuid;

    @Test
    public final void testConfigDeployPropertiesForPnfTest() {
        userParam.put("Instance1", "instance1value");
        userParam.put("Instance2", "instance2value");
        configAssignPropertiesForPnf.setPnfCustomizationUuid("pnf-customization-uuid");
        configAssignPropertiesForPnf.setPnfId("pnf-id");
        configAssignPropertiesForPnf.setPnfName("pnf-name");
        configAssignPropertiesForPnf.setServiceInstanceId("service-instance-id");
        configAssignPropertiesForPnf.setServiceModelUuid("service-model-uuid");
        configAssignPropertiesForPnf.setUserParam("Instance1", "instance1value");
        configAssignPropertiesForPnf.setUserParam("Instance2", "instance2value");

        assertNotNull(configAssignPropertiesForPnf.getPnfCustomizationUuid());
        assertNotNull(configAssignPropertiesForPnf.getPnfId());
        assertNotNull(configAssignPropertiesForPnf.getPnfName());
        assertNotNull(configAssignPropertiesForPnf.getServiceInstanceId());
        assertNotNull(configAssignPropertiesForPnf.getServiceModelUuid());
        assertNotNull(configAssignPropertiesForPnf.getUserParam());

        assertEquals("service-instance-id", configAssignPropertiesForPnf.getServiceInstanceId());
        assertEquals("service-model-uuid", configAssignPropertiesForPnf.getServiceModelUuid());
        assertEquals("pnf-customization-uuid", configAssignPropertiesForPnf.getPnfCustomizationUuid());
        assertEquals("pnf-id", configAssignPropertiesForPnf.getPnfId());
        assertEquals("pnf-name", configAssignPropertiesForPnf.getPnfName());
        assertEquals(userParam, configAssignPropertiesForPnf.getUserParam());
    }

    @Test
    public void testtoString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"service-instance-id\":").append("\"").append(serviceInstanceId).append("\"");
        sb.append(", \"pnf-id\":").append("\"").append(pnfId).append("\"");
        sb.append(", \"pnf-name\":").append("\"").append(pnfName).append("\"");
        sb.append(", \"service-model-uuid\":").append("\"").append(serviceModelUuid).append("\"");
        sb.append(", \"pnf-customization-uuid\":").append("\"").append(pnfCustomizationUuid).append("\"");
        for (Map.Entry<String, Object> entry : userParam.entrySet()) {
            sb.append(",");
            sb.append("\"");
            sb.append(entry.getKey());
            sb.append("\"");
            sb.append(":");
            sb.append("\"");
            sb.append(entry.getValue());
            sb.append("\"");
        }
        sb.append('}');
        String Expexted = sb.toString();
        assertEquals(Expexted, configAssignPropertiesForPnf.toString());

    }
}
