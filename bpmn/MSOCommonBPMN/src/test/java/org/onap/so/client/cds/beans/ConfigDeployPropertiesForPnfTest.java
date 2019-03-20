package org.onap.so.client.cds.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ConfigDeployPropertiesForPnfTest {
    ConfigDeployPropertiesForPnf configDeployPropertiesForPnf = new ConfigDeployPropertiesForPnf();
    private String serviceInstanceId;
    private String pnfId;
    private String pnfName;
    private String serviceModelUuid;
    private String pnfCustomizationUuid;

    @Test
    public final void testConfigDeployPropertiesForPnfTest() {
        configDeployPropertiesForPnf.setServiceInstanceId("service-instance-id");
        configDeployPropertiesForPnf.setServiceModelUuid("service-model-uuid");
        configDeployPropertiesForPnf.setPnfCustomizationUuid("pnf-customization-uuid");
        configDeployPropertiesForPnf.setPnfId("pnf-id");
        configDeployPropertiesForPnf.setPnfName("pnf-name");
        assertNotNull(configDeployPropertiesForPnf.getServiceInstanceId());
        assertNotNull(configDeployPropertiesForPnf.getServiceModelUuid());
        assertNotNull(configDeployPropertiesForPnf.getPnfCustomizationUuid());
        assertNotNull(configDeployPropertiesForPnf.getPnfId());
        assertNotNull(configDeployPropertiesForPnf.getPnfName());

        assertEquals("service-instance-id", configDeployPropertiesForPnf.getServiceInstanceId());
        assertEquals("service-model-uuid", configDeployPropertiesForPnf.getServiceModelUuid());
        assertEquals("pnf-customization-uuid", configDeployPropertiesForPnf.getPnfCustomizationUuid());
        assertEquals("pnf-id", configDeployPropertiesForPnf.getPnfId());
        assertEquals("pnf-name", configDeployPropertiesForPnf.getPnfName());

    }

    @Test
    public void testtoString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"service-instance-id\":").append("\"").append(serviceInstanceId).append("\"");
        sb.append(", \"pnf-id\":").append("\"").append(pnfId).append("\"");
        sb.append(", \"pnf-name\":").append("\"").append(pnfName).append("\"");
        sb.append(", \"service-model-uuid\":").append("\"").append(serviceModelUuid).append("\"");
        sb.append(", \"pnf-customization-uuid\":").append("\"").append(pnfCustomizationUuid).append("\"");
        sb.append('}');
        String Expexted = sb.toString();
        assertEquals(Expexted, configDeployPropertiesForPnf.toString());
    }

}
