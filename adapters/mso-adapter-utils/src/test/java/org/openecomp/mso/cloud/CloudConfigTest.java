/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.cloud;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This class implements test methods of the CloudConfig features.
 *
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class CloudConfigTest {

	@Autowired
	private CloudConfig con;

   /**
    * This method implements a test for the getCloudSites method.
    */
   @Test
   public final void testGetCloudSites () {
	   Map<String,CloudSite> siteMap = con.getCloudSites();
	   assertNotNull(siteMap);

	   CloudSite site1 = siteMap.get("regionOne");

	   assertEquals ("regionOne", site1.getRegionId());
	   assertEquals ("MT_KEYSTONE", site1.getIdentityServiceId());
	   assertEquals ("MT2", site1.getClli());
	   assertEquals ("2.5", site1.getAicVersion());
   }


   /**
    * This method implements a test for the getIdentityServices method.
 * @throws MsoException 
    */
   @Test
   public final void testGetIdentityServices () throws MsoException {
	   Map<String,CloudIdentity> identityMap = con.getIdentityServices ();
	   assertNotNull(identityMap);

	   CloudIdentity identity1 = identityMap.get("MT_KEYSTONE");

	   assertEquals("john", identity1.getMsoId());
	   assertEquals("313DECE408AF7759D442D7B06DD9A6AA", identity1.getMsoPass());
	   assertEquals("admin", identity1.getAdminTenant());
	   assertEquals("_member_", identity1.getMemberRole());
	   assertEquals(false, identity1.hasTenantMetadata());
	   assertEquals("http://mtdnj02bh01wt.bvoip.labs.att.com:5000/v2.0", identity1.getIdentityUrl());
	   assertEquals(ServerType.KEYSTONE, identity1.getIdentityServerType());
	   assertEquals(AuthenticationType.USERNAME_PASSWORD, identity1.getIdentityAuthenticationType());

   }

   /**
    * This method implements a test for the getCloudSite method.
    */
   @Test
   public final void testGetCloudSite () {
	   Optional<CloudSite> site  = con.getCloudSite("MT");
	   assertTrue(site.isPresent());
	   CloudSite site1 = site.get();
	   assertEquals (site1.getRegionId(), "RegionOne");
	   assertEquals("DAN", site1.getId());
	   assertEquals (site1.getIdentityServiceId(), "DAN_KEYSTONE");
   }
   
   @Test
   public void testGetIdentityService() {
	   CloudIdentity identity = con.getIdentityService("MT_KEYSTONE");
	   assertEquals("john", identity.getMsoId());
	   assertEquals("MT_KEYSTONE", identity.getId());
   }

}
