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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PreCheckPropertiesForPnfTest {

    private PreCheckPropertiesForPnf preCheckPropertiesForPnf = new PreCheckPropertiesForPnf();
    private static final String TEST_SERVICE_MODEL_UUID = "service-model-uuid";
    private static final String TEST_PNF_CUSTOMIZATION_UUID = "pnf-customization-uuid";
    private static final String TEST_PNF_ID = "pnf-id";
    private static final String TEST_PNF_NAME = "pnf-name";
    private static final String TEST_PNF_IP_V4_ADDRESS = "1.1.1.1";
    private static final String TEST_PNF_IP_V6_ADDRESS = "::/128";
    private static final String TEST_SERVICE_INSTANCE_ID = "service-instance-id";

    @Before
    public void setUp() {
        preCheckPropertiesForPnf.setServiceInstanceId(TEST_SERVICE_INSTANCE_ID);
        preCheckPropertiesForPnf.setServiceModelUuid(TEST_SERVICE_MODEL_UUID);
        preCheckPropertiesForPnf.setPnfCustomizationUuid(TEST_PNF_CUSTOMIZATION_UUID);
        preCheckPropertiesForPnf.setPnfId(TEST_PNF_ID);
        preCheckPropertiesForPnf.setPnfName(TEST_PNF_NAME);
        preCheckPropertiesForPnf.setPnfIpV4Address(TEST_PNF_IP_V4_ADDRESS);
        preCheckPropertiesForPnf.setPnfIpV6Address(TEST_PNF_IP_V6_ADDRESS);
    }

    @Test
    public final void testPreCheckPropertiesForPnfTest() {
        assertNotNull(preCheckPropertiesForPnf.getServiceInstanceId());
        assertNotNull(preCheckPropertiesForPnf.getServiceModelUuid());
        assertNotNull(preCheckPropertiesForPnf.getPnfCustomizationUuid());
        assertNotNull(preCheckPropertiesForPnf.getPnfId());
        assertNotNull(preCheckPropertiesForPnf.getPnfName());

        assertEquals(TEST_SERVICE_INSTANCE_ID, preCheckPropertiesForPnf.getServiceInstanceId());
        assertEquals(TEST_SERVICE_MODEL_UUID, preCheckPropertiesForPnf.getServiceModelUuid());
        assertEquals(TEST_PNF_CUSTOMIZATION_UUID, preCheckPropertiesForPnf.getPnfCustomizationUuid());
        assertEquals(TEST_PNF_ID, preCheckPropertiesForPnf.getPnfId());
        assertEquals(TEST_PNF_NAME, preCheckPropertiesForPnf.getPnfName());
        assertEquals(TEST_PNF_IP_V4_ADDRESS, preCheckPropertiesForPnf.getPnfIpV4Address());
        assertEquals(TEST_PNF_IP_V6_ADDRESS, preCheckPropertiesForPnf.getPnfIpV6Address());

    }

    @Test
    public void testtoString() {
        final String Expected = new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("service-instance-id", TEST_SERVICE_INSTANCE_ID).append("pnf-id", TEST_PNF_ID)
                .append("pnf-name", TEST_PNF_NAME).append("pnf-ipv4-address", TEST_PNF_IP_V4_ADDRESS)
                .append("pnf-ipv6-address", TEST_PNF_IP_V6_ADDRESS)
                .append("service-model-uuid", TEST_SERVICE_MODEL_UUID)
                .append("pnf-customization-uuid", TEST_PNF_CUSTOMIZATION_UUID).toString();
        assertEquals(Expected, preCheckPropertiesForPnf.toString());
    }

}
