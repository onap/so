package org.onap.so.client.cds.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ConfigDeployRequestPnfTest {
    ConfigDeployRequestPnf configDeployRequestPnf = new ConfigDeployRequestPnf();
    private String resolutionKey;
    ConfigDeployPropertiesForPnf configDeployPropertiesForPnf = new ConfigDeployPropertiesForPnf();

    @Test
    public final void testConfigDeployRequestVnf() {
        configDeployRequestPnf.setResolutionKey("resolution-key");
        configDeployRequestPnf.setConfigDeployPropertiesForPnf(new ConfigDeployPropertiesForPnf());
        assertNotNull(configDeployRequestPnf.getResolutionKey());
        assertNotNull(configDeployRequestPnf.getConfigDeployPropertiesForPnf());
        assertEquals("resolution-key", configDeployRequestPnf.getResolutionKey());
        assertEquals(configDeployPropertiesForPnf, configDeployRequestPnf.getConfigDeployPropertiesForPnf());
    }

    @Test
    public void testtoString() {
        configDeployPropertiesForPnf.setServiceInstanceId("service-instance-id");
        configDeployPropertiesForPnf.setServiceModelUuid("service-model-uuid");
        configDeployPropertiesForPnf.setPnfCustomizationUuid("pnf-customization-uuid");
        configDeployPropertiesForPnf.setPnfId("pnf-id");
        configDeployPropertiesForPnf.setPnfName("pnf-name");
        configDeployRequestPnf.setConfigDeployPropertiesForPnf(configDeployPropertiesForPnf);
        final StringBuilder sb = new StringBuilder("{\"config-deploy-request\":{");
        sb.append("\"resolution-key\":").append("\"").append(resolutionKey).append("\"");
        sb.append(", \"config-deploy-properties\":").append(configDeployPropertiesForPnf.toString());
        sb.append('}');
        sb.append('}');
        String Expexted = sb.toString();

        assertEquals(Expexted, configDeployRequestPnf.toString());
    }

}
