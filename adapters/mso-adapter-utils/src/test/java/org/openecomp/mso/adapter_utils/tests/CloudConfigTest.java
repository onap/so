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

package org.openecomp.mso.adapter_utils.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.Map;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.openstack.exceptions.MsoCloudIdentityNotFound;

/**
 * This class implements test methods of the CloudConfig features.
 */
public class CloudConfigTest {

	private static CloudConfig con;
	private static CloudConfigFactory cloudConfigFactory= new CloudConfigFactory();

	public CloudConfigTest () {

	}

	/**
    * This method is called before any test occurs.
    * It creates a fake tree from scratch
	 * @throws MsoCloudIdentityNotFound 
    */
   @Before
   public final void prepare () throws MsoCloudIdentityNotFound {
	   ClassLoader classLoader = CloudConfigTest.class.getClassLoader();
	   String config = classLoader.getResource("cloud_config.json").toString().substring(5);

	   cloudConfigFactory.initializeCloudConfig(config,1);
	   con = cloudConfigFactory.getCloudConfig();
   }

   /**
    * This method implements a test for the getCloudConfig method.
    */
   @Test
   public final void testGetCloudConfig () {
	   assertNotNull(con);
   }

   /**
    * This method implements a test for the getCloudSites method.
    */
   @Test
   public final void testGetCloudSites () {
	   Map<String,CloudSite> siteMap = con.getCloudSites();
	   assertNotNull(siteMap);

	   CloudSite site1 = siteMap.get("MT");
	   CloudSite site2 = siteMap.get("DAN");
	   CloudSite site3 = siteMap.get("MTINJVCC101");
	   CloudSite site4 = siteMap.get("MTSNJA4LCP1");

	   assertEquals (site1.getRegionId(), "regionOne");
	   assertEquals (site1.getIdentityServiceId(), "MT_KEYSTONE");
	   assertEquals (site2.getRegionId(), "RegionOne");
	   assertEquals (site2.getIdentityServiceId(), "DAN_KEYSTONE");
	   assertEquals (site3.getRegionId(), "regionTwo");
	   assertEquals (site3.getIdentityServiceId(), "MTINJVCC101_DCP");
	   assertEquals (site4.getRegionId(), "mtsnjlcp1");
	   assertEquals (site4.getIdentityServiceId(), "MTSNJA3DCP1");
   }


   /**
    * This method implements a test for the getIdentityServices method.
    */
   @Test
   public final void testGetIdentityServices () {
	   Map<String,CloudIdentity> identityMap = con.getIdentityServices ();
	   assertNotNull(identityMap);

	   CloudIdentity identity1 = identityMap.get("MT_KEYSTONE");
	   CloudIdentity identity2 = identityMap.get("DAN_KEYSTONE");
	   CloudIdentity identity3 = identityMap.get("MTINJVCC101_DCP");
	   CloudIdentity identity4 = identityMap.get("MTSNJA3DCP1");

	   assertEquals("john", identity1.getMsoId());
	   assertEquals("changeme", identity1.getMsoPass());
	   assertEquals("admin", identity1.getAdminTenant());
	   assertEquals("_member_", identity1.getMemberRole());
	   assertEquals(false, identity1.hasTenantMetadata());

	   assertEquals("mockId", identity2.getMsoId());
	   assertEquals("stack123", identity2.getMsoPass());
	   assertEquals("service", identity2.getAdminTenant());
	   assertEquals("_member_", identity2.getMemberRole());
	   assertEquals(false, identity2.hasTenantMetadata());

	   assertEquals("mockIdToo", identity3.getMsoId());
	   assertEquals("AICG@mm@@2015", identity3.getMsoPass());
	   assertEquals("service", identity3.getAdminTenant());
	   assertEquals("admin", identity3.getMemberRole());
	   assertEquals(true, identity3.hasTenantMetadata());

	   assertEquals("mockIdToo", identity4.getMsoId());
	   assertEquals("2315QRS2015srq", identity4.getMsoPass());
	   assertEquals("service", identity4.getAdminTenant());
	   assertEquals("admin", identity4.getMemberRole());
	   assertEquals(true, identity4.hasTenantMetadata());

   }

   /**
    * This method implements a test for the getCloudSite method.
    */
   @Test
   public final void testGetCloudSite () {
	   CloudSite site1  = con.getCloudSite("MT");
	   assertNotNull(site1);
	   assertEquals (site1.getRegionId(), "regionOne");
	   assertEquals (site1.getIdentityServiceId(), "MT_KEYSTONE");
   }

   /**
    * This method implements a test for the getIdentityService method.
    */
   @Test
   public final void testGetIdentityService () {
	   CloudIdentity identity1  = con.getIdentityService("MT_KEYSTONE");
	   assertNotNull(identity1);
	   assertEquals (identity1.getMsoId(), "john");
	   assertEquals (identity1.getMsoPass(), "changeme");
	   assertEquals (identity1.getAdminTenant(), "admin");
	   assertEquals (identity1.getMemberRole(), "_member_");
	   assertEquals (identity1.hasTenantMetadata(), false);

	   CloudIdentity identity2  = con.getIdentityService("Test");
	   assertNull(identity2);
   }
   
   @Test (expected = MsoCloudIdentityNotFound.class)
   public final void testLoadWithWrongFile () throws MsoCloudIdentityNotFound {
       ClassLoader classLoader = CloudConfigTest.class.getClassLoader();
       String config = classLoader.getResource("cloud_config_bad.json").toString().substring(5);

       cloudConfigFactory.initializeCloudConfig(config,1);
   }
   
   @Test
   public final void testReloadWithWrongFile () {
       ClassLoader classLoader = CloudConfigTest.class.getClassLoader();
       String config = classLoader.getResource("cloud_config_bad.json").toString().substring(5);

       try {
           cloudConfigFactory.initializeCloudConfig(config,1);
           Assert.fail("MsoCloudIdentityNotFound was expected");
       } catch (MsoCloudIdentityNotFound e) {
           
       }
       Assert.assertTrue("Should be an empty CloudConfig", cloudConfigFactory.getCloudConfig().getCloudSites().isEmpty());
       Assert.assertTrue("Should be an empty CloudConfig", cloudConfigFactory.getCloudConfig().getIdentityServices().isEmpty());
       
       // Now reload the right config
       config = classLoader.getResource("cloud_config.json").toString().substring(5);
       cloudConfigFactory.changeMsoPropertiesFilePath(config);
       cloudConfigFactory.reloadCloudConfig();
       Assert.assertTrue("Flag valid Config should be true now that the cloud_config is correct", cloudConfigFactory.getCloudConfig().isValidCloudConfig());

   }

}
