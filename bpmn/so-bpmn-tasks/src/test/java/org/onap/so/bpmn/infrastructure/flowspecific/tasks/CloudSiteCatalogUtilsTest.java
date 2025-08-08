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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import java.util.Optional;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;

public class CloudSiteCatalogUtilsTest extends BaseTaskTest {

    @InjectMocks
    private CloudSiteCatalogUtils cloudSiteCatalogUtils = new CloudSiteCatalogUtils();

    @Test
    public void testGetCloudSiteGetVersion30Test() {
        CloudSite cloudSite = new CloudSite();
        String testCloudSiteId = "testCloudSiteId";
        cloudSite.setClli(testCloudSiteId);
        doReturn(cloudSite).when(catalogDbClient).getCloudSite(testCloudSiteId);
        Optional<CloudSite> actualCloudSite = cloudSiteCatalogUtils.getCloudSite(testCloudSiteId);
        assertEquals(actualCloudSite.get().getClli(), testCloudSiteId);
    }

    @Test
    public void testGetCloudSiteGetVersion25Test() {
        CloudSite cloudSite = new CloudSite();
        String testCloudSiteId = "testCloudSiteId";
        cloudSite.setClli(testCloudSiteId);
        doReturn(null).when(catalogDbClient).getCloudSite(testCloudSiteId);
        doReturn(cloudSite).when(catalogDbClient).getCloudSiteByClliAndAicVersion(testCloudSiteId, "2.5");
        Optional<CloudSite> actualCloudSite = cloudSiteCatalogUtils.getCloudSite(testCloudSiteId);
        assertEquals(actualCloudSite.get().getClli(), testCloudSiteId);
    }

    @Test
    public void testGetIdentityUrlFromCloudSiteSuccessTest() {
        CloudSite cloudSite = new CloudSite();
        String testCloudSiteId = "testCloudSiteId";
        String testIdentityUrl = "testIdentityUrl";
        delegateExecution.setVariable("lcpCloudRegionId", testCloudSiteId);
        cloudSite.setClli(testCloudSiteId);
        CloudIdentity cloudIdentity = new CloudIdentity();
        cloudIdentity.setIdentityUrl(testIdentityUrl);
        cloudSite.setIdentityService(cloudIdentity);
        doReturn(cloudSite).when(catalogDbClient).getCloudSite(testCloudSiteId);
        cloudSiteCatalogUtils.getIdentityUrlFromCloudSite(delegateExecution);
        String actualIdentityUrl = (String) delegateExecution.getVariable("identityUrl");
        assertEquals(testIdentityUrl, actualIdentityUrl);
    }

    @Test
    public void testGetIdentityUrlFromCloudSiteNoCloudIdProvidedTest() {
        CloudSite cloudSite = new CloudSite();
        String testCloudSiteId = "testCloudSiteId";
        String testIdentityUrl = "testIdentityUrl";
        cloudSite.setClli(testCloudSiteId);
        CloudIdentity cloudIdentity = new CloudIdentity();
        cloudIdentity.setIdentityUrl(testIdentityUrl);
        cloudSite.setIdentityService(cloudIdentity);
        doReturn(cloudSite).when(catalogDbClient).getCloudSite(testCloudSiteId);
        cloudSiteCatalogUtils.getIdentityUrlFromCloudSite(delegateExecution);
        String actualIdentityUrl = (String) delegateExecution.getVariable("identityUrl");
        assertEquals(null, actualIdentityUrl);
    }
}
