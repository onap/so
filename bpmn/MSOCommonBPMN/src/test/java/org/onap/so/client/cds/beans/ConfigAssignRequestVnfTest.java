package org.onap.so.client.cds.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ConfigAssignRequestVnfTest {
    ConfigAssignRequestVnf configAssignRequestVnf = new ConfigAssignRequestVnf();
    ConfigAssignPropertiesForVnf configAssignPropertiesForVnf = new ConfigAssignPropertiesForVnf();
    private Map<String, Object> userParam = new HashMap<String, Object>();

    private String resolutionKey;

    @Test
    public final void testConfigAssignRequestVnf() {
        configAssignRequestVnf.setResolutionKey("resolution-key");
        configAssignRequestVnf.setConfigAssignPropertiesForVnf(configAssignPropertiesForVnf);
        assertNotNull(configAssignRequestVnf.getResolutionKey());
        assertNotNull(configAssignRequestVnf.getConfigAssignPropertiesForVnf());

        assertEquals("resolution-key", configAssignRequestVnf.getResolutionKey());
        assertEquals(configAssignPropertiesForVnf, configAssignRequestVnf.getConfigAssignPropertiesForVnf());

    }

    @Test
    public void testtoString() {
        userParam.put("Instance1", "instance1value");
        configAssignPropertiesForVnf.setServiceInstanceId("service-instance-id");
        configAssignPropertiesForVnf.setServiceModelUuid("service-model-uuid");
        configAssignPropertiesForVnf.setUserParam("user_params", userParam);
        configAssignPropertiesForVnf.setVnfCustomizationUuid("vnf-customization-uuid");
        configAssignPropertiesForVnf.setVnfId("vnf-id");
        configAssignPropertiesForVnf.setVnfName("vnf-name");
        configAssignRequestVnf.setConfigAssignPropertiesForVnf(configAssignPropertiesForVnf);

        final StringBuilder sb = new StringBuilder("{\"config-assign-request\":{");
        sb.append("\"resolution-key\":").append("\"").append(resolutionKey).append("\"");
        sb.append(", \"config-assign-properties\":").append(configAssignPropertiesForVnf.toString());
        sb.append('}');
        sb.append('}');

        String Expexted = sb.toString();

        assertEquals(Expexted, configAssignRequestVnf.toString());
    }
}
