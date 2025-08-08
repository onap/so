/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.db.catalog.data.repository;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.exceptions.NoEntityFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import java.util.List;

public class CloudSiteRepositoryTest extends BaseTest {

    @Autowired
    private CloudSiteRepository cloudSiteRepository;

    @Test
    public void findByClliAndAicVersionTest() {
        CloudSite cloudSite = cloudSiteRepository.findByClliAndCloudVersion("MDT13", "2.5");
        Assert.assertNotNull(cloudSite);
        Assert.assertEquals("mtn13", cloudSite.getId());
    }

    @Test
    public void findOneTest() throws Exception {
        CloudSite cloudSite = cloudSiteRepository.findById("mtn13")
                .orElseThrow(() -> new NoEntityFoundException("Cannot Find Operation"));

        Assert.assertNotNull(cloudSite);
        Assert.assertEquals("mtn13", cloudSite.getId());
    }

    @Test
    public void findAllTest() {
        List<CloudSite> cloudSiteList = cloudSiteRepository.findAll();
        Assert.assertFalse(CollectionUtils.isEmpty(cloudSiteList));
    }

}
