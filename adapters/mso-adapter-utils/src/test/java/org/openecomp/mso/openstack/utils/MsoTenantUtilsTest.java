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

package org.openecomp.mso.openstack.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.cloud.CloudIdentity;

@RunWith(MockitoJUnitRunner.class)
public class MsoTenantUtilsTest {

	@Test
	public void test()throws Exception {
		MsoTenantUtils mtu =mock(MsoTenantUtils.class);
		//MsoTenantUtils mki = null;
		CloudIdentity cloudIdentity=mock(CloudIdentity.class);
		Map <String, String> metadata=new HashMap<>();
		mtu.createTenant("name", "id", metadata, true);
		verify(mtu).createTenant("name", "id", metadata, true);
		mtu.queryTenant("tenantId", "cloudSiteId");
		verify(mtu).queryTenant("tenantId", "cloudSiteId");
		mtu.deleteTenant("tenantId", "cloudSiteId");
		verify(mtu).deleteTenant("tenantId", "cloudSiteId");
		mtu.getKeystoneUrl("regionId", "msoPropID", cloudIdentity);
		verify(mtu).getKeystoneUrl("regionId", "msoPropID", cloudIdentity);
		mtu.queryTenantByName("tenantName", "cloudSiteId");
		verify(mtu).queryTenantByName("tenantName", "cloudSiteId");

	}
}