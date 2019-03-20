package org.onap.so.client.cds.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ConfigAssignPropertiesForVnfTest {
    ConfigAssignPropertiesForVnf configAssignPropertiesForVnf = new ConfigAssignPropertiesForVnf();
    private Map<String, Object> userParam = new HashMap<String, Object>();
    private String serviceInstanceId;
    private String vnfId;
    private String vnfName;
    private String serviceModelUuid;
    private String vnfCustomizationUuid;

    @Test
    public final void testConfigAssignPropertiesForVnfTest() {
        userParam.put("Instance1", "instance1value");
        configAssignPropertiesForVnf.setServiceInstanceId("service-instance-id");
        configAssignPropertiesForVnf.setServiceModelUuid("service-model-uuid");
        configAssignPropertiesForVnf.setVnfCustomizationUuid("vnf-customization-uuid");
        configAssignPropertiesForVnf.setVnfId("vnf-id");
        configAssignPropertiesForVnf.setVnfName("vnf-name");
        configAssignPropertiesForVnf.setUserParam("Instance1", "instance1value");

        assertNotNull(configAssignPropertiesForVnf.getServiceInstanceId());
        assertNotNull(configAssignPropertiesForVnf.getServiceModelUuid());
        assertNotNull(configAssignPropertiesForVnf.getVnfCustomizationUuid());
        assertNotNull(configAssignPropertiesForVnf.getVnfId());
        assertNotNull(configAssignPropertiesForVnf.getVnfName());
        assertNotNull(configAssignPropertiesForVnf.getUserParam());

        assertEquals("service-instance-id", configAssignPropertiesForVnf.getServiceInstanceId());
        assertEquals("service-model-uuid", configAssignPropertiesForVnf.getServiceModelUuid());
        assertEquals("vnf-customization-uuid", configAssignPropertiesForVnf.getVnfCustomizationUuid());
        assertEquals("vnf-id", configAssignPropertiesForVnf.getVnfId());
        assertEquals("vnf-name", configAssignPropertiesForVnf.getVnfName());
        assertEquals(userParam, configAssignPropertiesForVnf.getUserParam());

    }

    @Test
    public void testtoString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"service-instance-id\":").append("\"").append(serviceInstanceId).append("\"");
        sb.append(", \"vnf-id\":").append("\"").append(vnfId).append("\"");
        sb.append(", \"vnf-name\":").append("\"").append(vnfName).append("\"");
        sb.append(", \"service-model-uuid\":").append("\"").append(serviceModelUuid).append("\"");
        sb.append(", \"vnf-customization-uuid\":").append("\"").append(vnfCustomizationUuid).append("\"");
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
        assertEquals(Expexted, configAssignPropertiesForVnf.toString());
    }
}
