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
import org.junit.Test;

public class ConfigDeployRequestPnfTest {
    ConfigDeployRequestPnf configDeployRequestPnf = new ConfigDeployRequestPnf();
    private String resolutionKey;
    ConfigDeployPropertiesForPnf configDeployPropertiesForPnf = new ConfigDeployPropertiesForPnf();

    @Test
    public final void testConfigDeployRequestVnf() {
        configDeployRequestPnf.setResolutionKey("resolution-key");
        configDeployRequestPnf.setConfigDeployPropertiesForPnf(configDeployPropertiesForPnf);
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
