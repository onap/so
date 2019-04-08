package org.onap.so.client.cds.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

public class ConfigDeployPropertiesForPnfTest {

    ConfigDeployPropertiesForPnf configDeployPropertiesForPnf = new ConfigDeployPropertiesForPnf();
    private static final String TEST_SERVICE_MODEL_UUID = "service-model-uuid";
    private static final String TEST_PNF_CUSTOMIZATION_UUID = "pnf-customization-uuid";
    private static final String TEST_PNF_ID = "pnf-id";
    private static final String TEST_PNF_NAME = "pnf-name";
    private static final String TEST_PNF_IP_V4_ADDRESS = "1.1.1.1";
    private static final String TEST_PNF_IP_V6_ADDRESS = "::/128";
    private static final String TEST_SERVICE_INSTANCE_ID = "service-instance-id";

    @Before
    public void setUp() {
        configDeployPropertiesForPnf.setServiceInstanceId(TEST_SERVICE_INSTANCE_ID);
        configDeployPropertiesForPnf.setServiceModelUuid(TEST_SERVICE_MODEL_UUID);
        configDeployPropertiesForPnf.setPnfCustomizationUuid(TEST_PNF_CUSTOMIZATION_UUID);
        configDeployPropertiesForPnf.setPnfId(TEST_PNF_ID);
        configDeployPropertiesForPnf.setPnfName(TEST_PNF_NAME);
        configDeployPropertiesForPnf.setPnfIpV4Address(TEST_PNF_IP_V4_ADDRESS);
        configDeployPropertiesForPnf.setPnfIpV6Address(TEST_PNF_IP_V6_ADDRESS);
    }

    @Test
    public final void testConfigDeployPropertiesForPnfTest() {
        assertNotNull(configDeployPropertiesForPnf.getServiceInstanceId());
        assertNotNull(configDeployPropertiesForPnf.getServiceModelUuid());
        assertNotNull(configDeployPropertiesForPnf.getPnfCustomizationUuid());
        assertNotNull(configDeployPropertiesForPnf.getPnfId());
        assertNotNull(configDeployPropertiesForPnf.getPnfName());

        assertEquals(TEST_SERVICE_INSTANCE_ID, configDeployPropertiesForPnf.getServiceInstanceId());
        assertEquals(TEST_SERVICE_MODEL_UUID, configDeployPropertiesForPnf.getServiceModelUuid());
        assertEquals(TEST_PNF_CUSTOMIZATION_UUID, configDeployPropertiesForPnf.getPnfCustomizationUuid());
        assertEquals(TEST_PNF_ID, configDeployPropertiesForPnf.getPnfId());
        assertEquals(TEST_PNF_NAME, configDeployPropertiesForPnf.getPnfName());
        assertEquals(TEST_PNF_IP_V4_ADDRESS, configDeployPropertiesForPnf.getPnfIpV4Address());
        assertEquals(TEST_PNF_IP_V6_ADDRESS, configDeployPropertiesForPnf.getPnfIpV6Address());

    }

    @Test
    public void testtoString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"service-instance-id\":").append("\"").append(TEST_SERVICE_INSTANCE_ID).append("\"");
        sb.append(", \"pnf-id\":").append("\"").append(TEST_PNF_ID).append("\"");
        sb.append(", \"pnf-name\":").append("\"").append(TEST_PNF_NAME).append("\"");
        sb.append(", \"pnf-ipv4-address\":").append("\"").append(TEST_PNF_IP_V4_ADDRESS).append("\"");
        sb.append(", \"pnf-ipv6-address\":").append("\"").append(TEST_PNF_IP_V6_ADDRESS).append("\"");
        sb.append(", \"service-model-uuid\":").append("\"").append(TEST_SERVICE_MODEL_UUID).append("\"");
        sb.append(", \"pnf-customization-uuid\":").append("\"").append(TEST_PNF_CUSTOMIZATION_UUID).append("\"");
        sb.append('}');
        String Expexted = sb.toString();
        assertEquals(Expexted, configDeployPropertiesForPnf.toString());
    }

}
