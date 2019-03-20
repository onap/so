package org.onap.so.client.cds.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ConfigDeployRequestVnfTest {

    ConfigDeployRequestVnf configDeployRequestVnf = new ConfigDeployRequestVnf();
    private String resolutionKey;
    ConfigDeployPropertiesForVnf configDeployPropertiesForVnf = new ConfigDeployPropertiesForVnf();

    @Test
    public final void testConfigDeployRequestVnf() {
        configDeployRequestVnf.setResolutionKey("resolution-key");
        configDeployRequestVnf.setConfigDeployPropertiesForVnf(new ConfigDeployPropertiesForVnf());
        assertNotNull(configDeployRequestVnf.getResolutionKey());
        assertNotNull(configDeployRequestVnf.getConfigDeployPropertiesForVnf());
        assertEquals("resolution-key", configDeployRequestVnf.getResolutionKey());
        assertEquals(configDeployPropertiesForVnf, configDeployRequestVnf.getConfigDeployPropertiesForVnf());
    }

    @Test
    public void testtoString() {
        configDeployPropertiesForVnf.setServiceInstanceId("service-instance-id");
        configDeployPropertiesForVnf.setServiceModelUuid("service-model-uuid");
        configDeployPropertiesForVnf.setVnfCustomizationUuid("vnf-customization-uuid");
        configDeployPropertiesForVnf.setVnfId("vnf-id");
        configDeployPropertiesForVnf.setVnfName("vnf-name");
        configDeployRequestVnf.setConfigDeployPropertiesForVnf(configDeployPropertiesForVnf);
        final StringBuilder sb = new StringBuilder("{\"config-deploy-request\":{");
        sb.append("\"resolution-key\":").append("\"").append(resolutionKey).append("\"");
        sb.append(", \"config-deploy-properties\":").append(configDeployPropertiesForVnf.toString());
        sb.append('}');
        sb.append('}');
        String Expexted = sb.toString();

        assertEquals(Expexted, configDeployRequestVnf.toString());
    }

}
