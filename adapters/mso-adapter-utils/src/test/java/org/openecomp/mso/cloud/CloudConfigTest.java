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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.openstack.exceptions.MsoCloudIdentityNotFound;

public class CloudConfigTest {

    private static String cloudConfigJsonFilePath;

    private static final int NUMBER_OF_CLOUD_SITES_IN_JSON_FILE = 4;
    private static final int NUMBER_OF_IDENTITY_SERVICES_IN_JSON_FILE = 4;
    private static final String CLOUD_SITES_FIELD_NAME = "cloudSites";
    private static final String IDENTITY_SERVICE_FIELD_NAME = "identityServices";
    private static final String CLOUD_SITE_DEFAULT = "default";

    private CloudConfig testedObject;
    private CloudSite cloudSite;
    private CloudSite cloudSiteDefault;

    @BeforeClass
    public static void preparePaths() {
        ClassLoader classLoader = CloudConfigTest.class.getClassLoader();
        cloudConfigJsonFilePath = classLoader.getResource("cloud_config.json").getPath();
    }

    @Before
    public void init() {
        testedObject = new CloudConfig();
    }

    @Test
    public void cloudSite_returnEmptyOptionalIfIdIsNull() {
        Optional<CloudSite> cloudConfigOpt = new CloudConfig().getCloudSite(null);
        assertThat(cloudConfigOpt).isEmpty();
    }

    @Test
    public void cloudSiteIsGotById_when_IdFound() throws NoSuchFieldException, IllegalAccessException {
        setCloudSitesMap();
        Optional<CloudSite> cloudSiteOpt = testedObject.getCloudSite(cloudSite.getId());
        assertThat(cloudSiteOpt).isPresent();
        assertThat(cloudSiteOpt.get().getId()).isEqualTo(cloudSite.getId());
        assertThat(cloudSiteOpt.get().getClli()).isEqualTo(cloudSite.getClli());
    }

    @Test
    public void cloudSiteIsGotByClli_when_IdNotFound() throws NoSuchFieldException, IllegalAccessException {
        setCloudSitesMap();
        Optional<CloudSite> cloudSiteOpt = testedObject.getCloudSite(cloudSite.getClli());
        assertTrue(cloudSiteOpt.isPresent());
        assertThat(cloudSiteOpt.get().getId()).isEqualTo(cloudSite.getId());
        assertThat(cloudSiteOpt.get().getClli()).isEqualTo(cloudSite.getClli());
    }

    @Test
    public void cloudSiteIsGotByDefault_when_IdAndClliNotFound() throws NoSuchFieldException, IllegalAccessException {
        setCloudSitesMap();
        Optional<CloudSite> cloudSiteOpt = testedObject.getCloudSite("not_existing_id");
        assertTrue(cloudSiteOpt.isPresent());
        assertThat(cloudSiteOpt.get().getId()).isEqualTo("not_existing_id");
        assertThat(cloudSiteOpt.get().getClli()).isEqualTo(cloudSiteDefault.getClli());
    }

    @Test
    public void cloudSiteNotFound_returnNull() {
        assertThat(testedObject.getCloudSite("not_existing_id")).isEmpty();
    }

    @Test
    public void identityServiceFoundById() throws NoSuchFieldException, IllegalAccessException {
        CloudIdentity cloudIdentity = createCloudIdentity();
        setIdentityServiceMap();
        CloudIdentity cloudIdentityResult = testedObject.getIdentityService(cloudIdentity.getId());

        assertThat(cloudIdentityResult).isNotNull();
        assertThat(cloudIdentityResult.getId()).isEqualTo(cloudIdentity.getId());
        assertThat(cloudIdentityResult.getMsoId()).isEqualTo(cloudIdentity.getMsoId());
    }

    @Test
    public void defaultClodeSiteNotFound_returnNull() {
        assertThat(testedObject.getIdentityService("not_existing_id")).isNull();
    }

    @Test
    public void loadCloudConfigSuccessful() throws IOException, MsoCloudIdentityNotFound {
        testedObject.loadCloudConfig(cloudConfigJsonFilePath, 1);
        checkCloudSites();
        checkIdentityServices();
    }

    private void checkCloudSites() {
        Map<String, CloudSite> siteMap = testedObject.getCloudSites();
        assertThat(siteMap).isNotEmpty().hasSize(NUMBER_OF_CLOUD_SITES_IN_JSON_FILE);
        CloudSite site1 = siteMap.get("MT");
        CloudSite site2 = siteMap.get("DAN");
        CloudSite site3 = siteMap.get("MTINJVCC101");
        CloudSite site4 = siteMap.get("MTSNJA4LCP1");

        assertThat(site1.getId()).isEqualTo("MT");
        assertThat(site1.getRegionId()).isEqualTo("regionOne");
        assertThat(site1.getIdentityServiceId()).isEqualTo("MT_KEYSTONE");
        assertThat(site1.getIdentityService()).isNotNull();
        assertThat(site1.getIdentityService().getId()).isEqualTo(site1.getIdentityServiceId());

        assertThat(site2.getId()).isEqualTo("DAN");
        assertThat(site2.getRegionId()).isEqualTo("RegionOne");
        assertThat(site2.getIdentityServiceId()).isEqualTo("DAN_KEYSTONE");
        assertThat(site2.getIdentityService()).isNotNull();
        assertThat(site2.getIdentityService().getId()).isEqualTo(site2.getIdentityServiceId());

        assertThat(site3.getId()).isEqualTo("MTINJVCC101");
        assertThat(site3.getRegionId()).isEqualTo("regionTwo");
        assertThat(site3.getIdentityServiceId()).isEqualTo("MTINJVCC101_DCP");
        assertThat(site3.getIdentityService()).isNotNull();
        assertThat(site3.getIdentityService().getId()).isEqualTo(site3.getIdentityServiceId());

        assertThat(site4.getId()).isEqualTo("MTSNJA4LCP1");
        assertThat(site4.getRegionId()).isEqualTo("mtsnjlcp1");
        assertThat(site4.getIdentityServiceId()).isEqualTo("MTSNJA3DCP1");
        assertThat(site4.getIdentityService()).isNotNull();
        assertThat(site4.getIdentityService().getId()).isEqualTo(site4.getIdentityServiceId());
    }

    private void checkIdentityServices() {
        Map<String, CloudIdentity> identityMap = testedObject.getIdentityServices();
        assertThat(identityMap).isNotEmpty().hasSize(NUMBER_OF_IDENTITY_SERVICES_IN_JSON_FILE);

        CloudIdentity identity1 = identityMap.get("MT_KEYSTONE");
        CloudIdentity identity2 = identityMap.get("DAN_KEYSTONE");
        CloudIdentity identity3 = identityMap.get("MTINJVCC101_DCP");
        CloudIdentity identity4 = identityMap.get("MTSNJA3DCP1");

        assertThat(identity1.getMsoId()).isEqualTo("john");
        assertThat(identity1.getMsoPass()).isEqualTo("changeme");
        assertThat(identity1.getAdminTenant()).isEqualTo("admin");
        assertThat(identity1.getMemberRole()).isEqualTo("_member_");
        assertThat(identity1.hasTenantMetadata()).isFalse();

        assertThat(identity2.getMsoId()).isEqualTo("mockId");
        assertThat(identity2.getMsoPass()).isEqualTo("stack123");
        assertThat(identity2.getAdminTenant()).isEqualTo("service");
        assertThat(identity2.getMemberRole()).isEqualTo("_member_");
        assertThat(identity2.hasTenantMetadata()).isFalse();

        assertThat(identity3.getMsoId()).isEqualTo("mockIdToo");
        assertThat(identity3.getMsoPass()).isEqualTo("AICG@mm@@2015");
        assertThat(identity3.getAdminTenant()).isEqualTo("service");
        assertThat(identity3.getMemberRole()).isEqualTo("admin");
        assertThat(identity3.hasTenantMetadata()).isTrue();

        assertThat(identity4.getMsoId()).isEqualTo("mockIdToo");
        assertThat(identity4.getMsoPass()).isEqualTo("2315QRS2015srq");
        assertThat(identity4.getAdminTenant()).isEqualTo("service");
        assertThat(identity4.getMemberRole()).isEqualTo("admin");
        assertThat(identity4.hasTenantMetadata()).isTrue();
    }

    @Test
    public void cloneSuccessful() throws NoSuchFieldException, IllegalAccessException {
        setCloudSitesMap();
        setIdentityServiceMap();
        assertThat(testedObject.clone()).isEqualTo(testedObject);
    }

    private void setCloudSitesMap() throws NoSuchFieldException, IllegalAccessException {
        Field field = testedObject.getClass().getDeclaredField(CLOUD_SITES_FIELD_NAME);
        field.setAccessible(true);
        Map<String, CloudSite> cloudSites = new HashMap<>();
        cloudSite = createCloudSite("idTest1", "clliTest1");
        cloudSiteDefault = createCloudSite(CLOUD_SITE_DEFAULT, "clliTest2");
        cloudSites.put(cloudSite.getId(), cloudSite);
        cloudSites.put(cloudSiteDefault.getId(), cloudSiteDefault);
        field.set(testedObject, cloudSites);
    }

    private void setIdentityServiceMap() throws NoSuchFieldException, IllegalAccessException {
        Field field = testedObject.getClass().getDeclaredField(IDENTITY_SERVICE_FIELD_NAME);
        field.setAccessible(true);

        Map<String, CloudIdentity> cloudIdentityMap = new HashMap<>();
        CloudIdentity cloudIdentity = createCloudIdentity();
        cloudIdentityMap.put(cloudIdentity.getId(), cloudIdentity);
        field.set(testedObject, cloudIdentityMap);
    }

    private CloudIdentity createCloudIdentity() {
        CloudIdentity cloudIdentity = new CloudIdentity();
        cloudIdentity.setId("identityTestId");
        cloudIdentity.setMsoId("msoTestId");
        return cloudIdentity;
    }

    private CloudSite createCloudSite(String id, String clli) {
        CloudSite cloudSite = new CloudSite();
        cloudSite.setId(id);
        cloudSite.setClli(clli);
        cloudSite.setAic_version("2.5");
        cloudSite.setIdentityService(createCloudIdentity());
        return cloudSite;
    }
}
