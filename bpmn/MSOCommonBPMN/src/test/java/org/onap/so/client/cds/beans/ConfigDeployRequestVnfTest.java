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

public class ConfigDeployRequestVnfTest {

    ConfigDeployRequestVnf configDeployRequestVnf = new ConfigDeployRequestVnf();
    private String resolutionKey;
    ConfigDeployPropertiesForVnf configDeployPropertiesForVnf = new ConfigDeployPropertiesForVnf();

    @Test
    public final void testConfigDeployRequestVnf() {
        configDeployRequestVnf.setResolutionKey("resolution-key");
        configDeployRequestVnf.setConfigDeployPropertiesForVnf(configDeployPropertiesForVnf);
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
