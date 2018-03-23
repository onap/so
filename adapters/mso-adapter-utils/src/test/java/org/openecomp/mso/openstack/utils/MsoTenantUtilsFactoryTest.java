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
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;

public class MsoTenantUtilsFactoryTest {
    
    MsoTenantUtils tenantU = null;
    
	@Test
	public void test() throws MsoCloudSiteNotFound {
	    tenantU = mock( MsoKeystoneUtils.class);
	  new MsoTenantUtilsFactory("ID");
	    MsoTenantUtilsFactory mti=mock(MsoTenantUtilsFactory.class);
	    mti.getTenantUtils("cloudSiteId");
	    when(mti.getTenantUtils("cloudSiteId")).thenReturn(tenantU);
	    mti.getTenantUtilsByServerType("type");
	    when(mti.getTenantUtilsByServerType("type")).thenReturn(tenantU);
	}

}
