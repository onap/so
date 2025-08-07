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

package org.onap.so.cloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Optional;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.onap.so.db.catalog.beans.CloudSite;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class implements test methods of the CloudConfig features.
 *
 *
 */
public class CloudConfigTest extends BaseTest {

    @Autowired
    private CloudConfig con;

    /**
     * This method implements a test for the getCloudSite method.
     */
    @Test
    public final void testGetCloudSite() {
        CloudSite site1 = con.getCloudSite("MTN13").get();

        assertEquals("mtn13", site1.getRegionId());
        assertEquals("mtn13", site1.getIdentityServiceId());
        assertEquals("MDT13", site1.getClli());
        assertEquals("3.0", site1.getCloudVersion());
    }

    /**
     * This method implements a test for the getCloudSite method.
     */
    @Test
    public final void testGetDefaultCloudSite() {
        Optional<CloudSite> site = con.getCloudSite("NotThere");
        assertTrue(site.isPresent());
        CloudSite site1 = site.get();
        assertEquals("NotThere", site1.getRegionId());
        assertEquals("MDT13", site1.getClli());
        assertEquals("NotThere", site1.getId());
    }

}
