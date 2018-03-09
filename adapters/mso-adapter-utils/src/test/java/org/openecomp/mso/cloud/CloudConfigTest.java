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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import org.openecomp.mso.openstack.exceptions.MsoCloudIdentityNotFound;

public class CloudConfigTest {

    private static String cloudConfigJsonFilePath;
    private static String cloudDefaultConfigJsonFilePath;
    private static String cloudConfigInvalidJsonFilePath;

    @BeforeClass
    public static void preparePaths() {
        ClassLoader classLoader = CloudConfigTest.class.getClassLoader();
        cloudConfigJsonFilePath = classLoader.getResource("cloud_config.json").getPath();
        cloudDefaultConfigJsonFilePath = classLoader.getResource("cloud_default_config.json").getPath();
        cloudConfigInvalidJsonFilePath = classLoader.getResource("cloud_config_bad.json").getPath();
    }

    private CloudConfig createTestObject(String jsonFilePath) throws MsoCloudIdentityNotFound {
        CloudConfigFactory cloudConfigFactory = new CloudConfigFactory();
        cloudConfigFactory.initializeCloudConfig(jsonFilePath, 1);
        return cloudConfigFactory.getCloudConfig();
    }

    @Test
    public void testGetCloudSites() throws MsoCloudIdentityNotFound {
        CloudConfig con = createTestObject(cloudConfigJsonFilePath);
        Map<String, CloudSite> siteMap = con.getCloudSites();
        assertNotNull(siteMap);

        CloudSite site1 = siteMap.get("MT");
        CloudSite site2 = siteMap.get("DAN");
        CloudSite site3 = siteMap.get("MTINJVCC101");
        CloudSite site4 = siteMap.get("MTSNJA4LCP1");

        assertEquals("regionOne", site1.getRegionId());
        assertEquals("MT_KEYSTONE", site1.getIdentityServiceId());
        assertEquals("RegionOne", site2.getRegionId());
        assertEquals("DAN_KEYSTONE", site2.getIdentityServiceId());
        assertEquals("regionTwo", site3.getRegionId());
        assertEquals("MTINJVCC101_DCP", site3.getIdentityServiceId());
        assertEquals("mtsnjlcp1", site4.getRegionId());
        assertEquals("MTSNJA3DCP1", site4.getIdentityServiceId());
    }

    @Test
    public void testGetIdentityServices() throws MsoCloudIdentityNotFound {
        CloudConfig con = createTestObject(cloudConfigJsonFilePath);
        Map<String, CloudIdentity> identityMap = con.getIdentityServices();
        assertNotNull(identityMap);

        CloudIdentity identity1 = identityMap.get("MT_KEYSTONE");
        CloudIdentity identity2 = identityMap.get("DAN_KEYSTONE");
        CloudIdentity identity3 = identityMap.get("MTINJVCC101_DCP");
        CloudIdentity identity4 = identityMap.get("MTSNJA3DCP1");

        assertEquals("john", identity1.getMsoId());
        assertEquals("changeme", identity1.getMsoPass());
        assertEquals("admin", identity1.getAdminTenant());
        assertEquals("_member_", identity1.getMemberRole());
        assertFalse(identity1.hasTenantMetadata());

        assertEquals("mockId", identity2.getMsoId());
        assertEquals("stack123", identity2.getMsoPass());
        assertEquals("service", identity2.getAdminTenant());
        assertEquals("_member_", identity2.getMemberRole());
        assertFalse(identity2.hasTenantMetadata());

        assertEquals("mockIdToo", identity3.getMsoId());
        assertEquals("AICG@mm@@2015", identity3.getMsoPass());
        assertEquals("service", identity3.getAdminTenant());
        assertEquals("admin", identity3.getMemberRole());
        assertTrue(identity3.hasTenantMetadata());

        assertEquals("mockIdToo", identity4.getMsoId());
        assertEquals("2315QRS2015srq", identity4.getMsoPass());
        assertEquals("service", identity4.getAdminTenant());
        assertEquals("admin", identity4.getMemberRole());
        assertTrue(identity4.hasTenantMetadata());
    }

    @Test
    public void cloudSiteIsGotById_when_IdFound() throws MsoCloudIdentityNotFound {
        CloudConfig con = createTestObject(cloudConfigJsonFilePath);
        Optional<CloudSite> cloudSite = con.getCloudSite("MT");
        assertTrue(cloudSite.isPresent());
        assertEquals("regionOne", cloudSite.get().getRegionId());
        assertEquals("MT_KEYSTONE", cloudSite.get().getIdentityServiceId());
    }

    @Test
    public void cloudSiteIsGotByClli_when_IdNotFound() throws MsoCloudIdentityNotFound {
        CloudConfig con = createTestObject(cloudConfigJsonFilePath);
        Optional<CloudSite> cloudSite = con.getCloudSite("CS_clli");
        assertTrue(cloudSite.isPresent());
        assertEquals("clliRegion", cloudSite.get().getRegionId());
        assertEquals("CS_clli", cloudSite.get().getClli());
        assertEquals("CS_service", cloudSite.get().getIdentityServiceId());
    }

    @Test
    public void cloudSiteIsGotByDefault_when_IdAndClliNotFound() throws MsoCloudIdentityNotFound {
        CloudConfig con = createTestObject(cloudDefaultConfigJsonFilePath);
        Optional<CloudSite> cloudSite = con.getCloudSite("not_existing_id");
        assertTrue(cloudSite.isPresent());
        assertEquals("not_existing_id", cloudSite.get().getId());
        assertEquals("not_existing_id", cloudSite.get().getRegionId());
    }

    @Test
    public void testGetIdentityService() throws MsoCloudIdentityNotFound {
        CloudConfig con = createTestObject(cloudConfigJsonFilePath);
        CloudIdentity identity1 = con.getIdentityService("MT_KEYSTONE");
        assertNotNull(identity1);
        assertEquals("john", identity1.getMsoId());
        assertEquals("changeme", identity1.getMsoPass());
        assertEquals("admin", identity1.getAdminTenant());
        assertEquals("_member_", identity1.getMemberRole());
        assertFalse(identity1.hasTenantMetadata());

        CloudIdentity identity2 = con.getIdentityService("Test");
        assertNull(identity2);
    }

    @Test(expected = MsoCloudIdentityNotFound.class)
    public void testLoadWithWrongFile() throws MsoCloudIdentityNotFound {
        createTestObject(cloudConfigInvalidJsonFilePath);
    }

    @Test
    public void testReloadWithWrongFile() {
        CloudConfigFactory cloudConfigFactory = new CloudConfigFactory();
        try {
            cloudConfigFactory.initializeCloudConfig(cloudConfigInvalidJsonFilePath, 1);
            Assert.fail("MsoCloudIdentityNotFound was expected");
        } catch (MsoCloudIdentityNotFound e) {

        }
        assertTrue("Should be an empty CloudConfig", cloudConfigFactory.getCloudConfig().getCloudSites().isEmpty());
        assertTrue("Should be an empty CloudConfig",
                cloudConfigFactory.getCloudConfig().getIdentityServices().isEmpty());
        // Now reload the right config
        cloudConfigFactory.changeMsoPropertiesFilePath(cloudConfigJsonFilePath);
        cloudConfigFactory.reloadCloudConfig();
        assertTrue("Flag valid Config should be true now that the cloud_config is correct",
                cloudConfigFactory.getCloudConfig().isValidCloudConfig());
    }

}
