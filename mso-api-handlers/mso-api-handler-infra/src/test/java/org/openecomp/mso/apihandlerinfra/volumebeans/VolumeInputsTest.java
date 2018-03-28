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
package org.openecomp.mso.apihandlerinfra.volumebeans;

import static org.junit.Assert.*;

import org.junit.Test;

public class VolumeInputsTest {
	
	VolumeInputs vi = new VolumeInputs();
    @Test
	public void testVolumeInputs() {
		vi.setVolumeGroupId("volumeGroupId");
		vi.setVolumeGroupName("volumeGroupName");
		vi.setVnfType("vnfType");
		vi.setVnfId("vnfId");
		vi.setServiceInstanceId("serviceInstanceId");
		vi.setServiceType("serviceType");
		vi.setServiceId("serviceId");
		vi.setAicNodeClli("aicNodeClli");
		vi.setAicCloudRegion("aicCloudRegion");
		vi.setTenantId("tenantId");
		vi.setVfModuleModelName("vfModuleModelName");
		vi.setAsdcServiceModelVersion("asdcServiceModelVersion");
		vi.setBackoutOnFailure(true);
		assertEquals(vi.getVolumeGroupId(), "volumeGroupId");
		assertEquals(vi.getVolumeGroupName(), "volumeGroupName");
		assertEquals(vi.getVnfType(), "vnfType");
		assertEquals(vi.getVnfId(), "vnfId");
		assertEquals(vi.getServiceInstanceId(), "serviceInstanceId");
		assertEquals(vi.getServiceType(), "serviceType");
		assertEquals(vi.getServiceId(), "serviceId");
		assertEquals(vi.getAicNodeClli(), "aicNodeClli");
		assertEquals(vi.getAicCloudRegion(), "aicCloudRegion");
		assertEquals(vi.getTenantId(), "tenantId");
		assertEquals(vi.getVfModuleModelName(), "vfModuleModelName");
		assertEquals(vi.getAsdcServiceModelVersion(), "asdcServiceModelVersion");
		assertEquals(vi.getBackoutOnFailure(), true);	
	}
}
