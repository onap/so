/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 TechMahindra
 * ================================================================================ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.adapters.vnf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class VfRollbackTest {
    private VfRollback vfRollback = new VfRollback();

    @Test
    public void test() {
        vfRollback.setVnfId("vnfId");
        vfRollback.setTenantId("tenantId");
        vfRollback.setCloudSiteId("cloudId");
        vfRollback.setTenantCreated(true);
        vfRollback.setVnfCreated(true);
        vfRollback.setMsoRequest(null);
        vfRollback.setVolumeGroupName("volumeGroupName");
        vfRollback.setVolumeGroupId("volumeGroupId");
        vfRollback.setRequestType("requestType");
        vfRollback.setVolumeGroupHeatStackId("volumeGroupHeatStackId");
        vfRollback.setBaseGroupHeatStackId("baseGroupHeatStackId");
        vfRollback.setIsBase(true);
        vfRollback.setVfModuleStackId("vfModuleStackId");
        assert (vfRollback.getVnfId() != null);
        assert (vfRollback.getTenantId() != null);
        assert (vfRollback.getCloudSiteId() != null);
        assert (vfRollback.getVolumeGroupName() != null);
        assert (vfRollback.getVolumeGroupId() != null);
        assert (vfRollback.getRequestType() != null);
        assert (vfRollback.getVolumeGroupHeatStackId() != null);
        assert (vfRollback.getBaseGroupHeatStackId() != null);
        assert (vfRollback.getVfModuleStackId() != null);
        assertEquals("vnfId", vfRollback.getVnfId());
        assertEquals("tenantId", vfRollback.getTenantId());
        assertEquals("cloudId", vfRollback.getCloudSiteId());
        assertEquals(true, vfRollback.getTenantCreated());
        assertEquals(true, vfRollback.getVnfCreated());
        assertEquals(null, vfRollback.getMsoRequest());
        assertEquals("volumeGroupName", vfRollback.getVolumeGroupName());
        assertEquals("volumeGroupId", vfRollback.getVolumeGroupId());
        assertEquals("requestType", vfRollback.getRequestType());
        assertEquals("volumeGroupHeatStackId", vfRollback.getVolumeGroupHeatStackId());
        assertEquals("baseGroupHeatStackId", vfRollback.getBaseGroupHeatStackId());
        assertEquals(true, vfRollback.isBase());
        assertEquals("vfModuleStackId", vfRollback.getVfModuleStackId());
    }

    @Test
    public void testtoString() {
        assertNotNull(vfRollback.toString());
    }
}
