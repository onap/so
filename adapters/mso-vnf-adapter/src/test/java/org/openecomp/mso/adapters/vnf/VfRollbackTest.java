/*
* ============LICENSE_START=======================================================
* ONAP : SO
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.openecomp.mso.adapters.vnf;
import static org.junit.Assert.assertEquals;
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
		assert(vfRollback.getVnfId() != null);
		assert(vfRollback.getTenantId() != null);
		assert(vfRollback.getCloudSiteId() != null);
		assert(vfRollback.getVolumeGroupName() != null);
		assert(vfRollback.getVolumeGroupId() != null);
		assert(vfRollback.getRequestType() != null);
		assert(vfRollback.getVolumeGroupHeatStackId() != null);
		assert(vfRollback.getBaseGroupHeatStackId() != null);
		assert(vfRollback.getVfModuleStackId() != null);
		assertEquals(vfRollback.getVnfId(), "vnfId");
		assertEquals(vfRollback.getTenantId(),"tenantId");
		assertEquals(vfRollback.getCloudSiteId(), "cloudId");
		assertEquals(vfRollback.getTenantCreated(), true);
		assertEquals(vfRollback.getVnfCreated(), true);
		assertEquals(vfRollback.getMsoRequest(), null);
		assertEquals(vfRollback.getVolumeGroupName(), "volumeGroupName");
		assertEquals(vfRollback.getVolumeGroupId(), "volumeGroupId");
		assertEquals(vfRollback.getRequestType(), "requestType");
		assertEquals(vfRollback.getVolumeGroupHeatStackId(), "volumeGroupHeatStackId");
		assertEquals(vfRollback.getBaseGroupHeatStackId(), "baseGroupHeatStackId");
		assertEquals(vfRollback.isBase(), true);
		assertEquals(vfRollback.getVfModuleStackId(), "vfModuleStackId");	
	}
	
	@Test
	public void testtoString() {
		assert(vfRollback.toString() != null);
		}
}
