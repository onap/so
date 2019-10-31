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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PreCheckRequestPnfTest {
    PreCheckRequestPnf preCheckRequestPnf = new PreCheckRequestPnf();
    private String resolutionKey;
    PreCheckPropertiesForPnf preCheckPropertiesForPnf = new PreCheckPropertiesForPnf();

    @Test
    public final void testPreCheckRequestPnf() {
        preCheckRequestPnf.setResolutionKey("resolution-key");
        preCheckRequestPnf.setPreCheckPropertiesForPnf(preCheckPropertiesForPnf);
        assertNotNull(preCheckRequestPnf.getResolutionKey());
        assertNotNull(preCheckRequestPnf.getPreCheckPropertiesForPnf());
        assertEquals("resolution-key", preCheckRequestPnf.getResolutionKey());
        assertEquals(preCheckPropertiesForPnf, preCheckRequestPnf.getPreCheckPropertiesForPnf());
    }

    @Test
    public void testtoString() {
        preCheckPropertiesForPnf.setServiceInstanceId("service-instance-id");
        preCheckPropertiesForPnf.setServiceModelUuid("service-model-uuid");
        preCheckPropertiesForPnf.setPnfCustomizationUuid("pnf-customization-uuid");
        preCheckPropertiesForPnf.setPnfId("pnf-id");
        preCheckPropertiesForPnf.setPnfName("pnf-name");
        preCheckRequestPnf.setPreCheckPropertiesForPnf(preCheckPropertiesForPnf);

        final StringBuilder sb = new StringBuilder("{\"pre-check-request\":{");
        sb.append("\"resolution-key\":").append("\"").append(resolutionKey).append("\"");
        sb.append(", \"pre-check-properties\":").append(preCheckPropertiesForPnf.toString());
        sb.append('}');
        sb.append('}');
        String Expected = sb.toString();

        assertEquals(Expected, preCheckRequestPnf.toString());
    }

}
