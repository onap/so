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

package org.openecomp.mso.bpmn.common.adapter.vnf;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class VnfRollbackTest {
	private VnfRollback vnfrollback = new VnfRollback();
	MsoRequest mso = new MsoRequest();
	@Test
	public void testVnfRollback() {
		vnfrollback.setCloudSiteId("cloudSiteId");
		vnfrollback.setMsoRequest(mso);
		vnfrollback.setTenantCreated(true);
		vnfrollback.setTenantId("tenantId");
		vnfrollback.setVnfCreated(true);
		vnfrollback.setVnfId("vnfId");
		assertEquals(vnfrollback.getCloudSiteId(), "cloudSiteId");
		assertEquals(vnfrollback.getMsoRequest(), mso);
		assertEquals(vnfrollback.isTenantCreated(), true);
		assertEquals(vnfrollback.getTenantId(), "tenantId");
		assertEquals(vnfrollback.isVnfCreated(), true);
		assertEquals(vnfrollback.getVnfId(), "vnfId");	
	}
	@Test
	public void testtoString() {
		assert(vnfrollback.toString() != null);
	}
}
