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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.openstack.exceptions.MsoCloudIdentityNotFound;

public class CloudConfigFactoryTest {

    private static final String CLOUD_CONFIG_FIELD_NAME = "cloudConfigCache";
    private static final int REFRESH_TIMER_VALUE = 1;

    private CloudConfigFactory testedObject;
    private CloudConfig cloudConfigMock;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        cloudConfigMock = mock(CloudConfig.class);
        testedObject = new CloudConfigFactory();
        setCloudConfig();
    }

    @Test
    public void initializeCloudConfigSuccessful() throws MsoCloudIdentityNotFound, IOException {
        ClassLoader classLoader = CloudConfigFactoryTest.class.getClassLoader();
        String cloudConfigJsonFilePath = classLoader.getResource("cloud_config.json").getPath();
        testedObject.initializeCloudConfig(cloudConfigJsonFilePath, REFRESH_TIMER_VALUE);
        verify(cloudConfigMock).loadCloudConfig(cloudConfigJsonFilePath, REFRESH_TIMER_VALUE);
    }

    @Test
    public void getValidCloudConfig() {
        when(cloudConfigMock.isValidCloudConfig()).thenReturn(true);

        testedObject.getCloudConfig();

        verify(cloudConfigMock).clone();
    }

    @Test
    public void getNotValidCloudConfig() {
        when(cloudConfigMock.isValidCloudConfig()).thenReturn(false);

        CloudConfig result = testedObject.getCloudConfig();

        assertThat(result).isNotNull();
        assertThat(result.getCloudSites()).isEmpty();
        assertThat(result.getIdentityServices()).isEmpty();
    }

    @Test
    public void reload_CloudConfigValid() throws IOException, MsoCloudIdentityNotFound {
        when(cloudConfigMock.isValidCloudConfig()).thenReturn(true);

        testedObject.reloadCloudConfig();

        verify(cloudConfigMock).clone();
        verify(cloudConfigMock).reloadPropertiesFile();
    }

    @Test
    public void reload_CloudConfigNotValid()
            throws IOException, MsoCloudIdentityNotFound {
        when(cloudConfigMock.isValidCloudConfig()).thenReturn(false);

        testedObject.reloadCloudConfig();

        verify(cloudConfigMock).reloadPropertiesFile();
    }

    @Test
    public void showCloudConfig() throws NoSuchFieldException, IllegalAccessException {
        when(cloudConfigMock.isValidCloudConfig()).thenReturn(true);
        when(cloudConfigMock.clone()).thenReturn(createCloudConfig("IdTest576", "identityTest456"));
        Response response = testedObject.showCloudConfig();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity().toString()).containsPattern("CloudSite:.*IdTest576")
                .containsPattern("Cloud Identity Service:.*identityTest456");

    }

    @Test
    public void resetClientCaches_Successful()  {
        Response response = testedObject.resetClientCaches();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity().toString()).isEqualTo("Client caches reset. All entries removed.");
    }

    @Test
    public void cleanUpClientCache_Successful()  {
        Response response = testedObject.cleanupClientCaches();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity().toString()).isEqualTo("Client caches cleaned up. All expired entries removed.");
    }

    @Test
    public void encryptPassword_Successful()  {
        Response response = testedObject.encryptPassword("passTest123");
        String expectedEncryptedPassword = CloudIdentity.encryptPassword("passTest123");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity().toString()).isEqualTo("Encrypted Password = "+expectedEncryptedPassword);
    }

    private CloudConfig createCloudConfig(String cloudSiteId, String identityServiceId)
            throws NoSuchFieldException, IllegalAccessException {
        CloudConfig cloudConfig = new CloudConfig();
        Map<String, CloudSite> cloudSiteMap = new HashMap<>();
        CloudSite cs = new CloudSite();
        cs.setId(cloudSiteId);
        cloudSiteMap.put("keyTest", cs);
        Field cloudSitesField = cloudConfig.getClass().getDeclaredField("cloudSites");
        cloudSitesField.setAccessible(true);
        cloudSitesField.set(cloudConfig, cloudSiteMap);

        Map<String, CloudIdentity> identityServicesMap = new HashMap<>();
        CloudIdentity cloudIdentity = new CloudIdentity();
        cloudIdentity.setId(identityServiceId);
        identityServicesMap.put("identityKey", cloudIdentity);

        Field identityServicesField = cloudConfig.getClass().getDeclaredField("identityServices");
        identityServicesField.setAccessible(true);
        identityServicesField.set(cloudConfig, identityServicesMap);

        return cloudConfig;
    }

    private void setCloudConfig()
            throws NoSuchFieldException, IllegalAccessException {
        Field field = testedObject.getClass().getDeclaredField(CLOUD_CONFIG_FIELD_NAME);
        field.setAccessible(true);
        field.set(testedObject, cloudConfigMock);
    }

}
