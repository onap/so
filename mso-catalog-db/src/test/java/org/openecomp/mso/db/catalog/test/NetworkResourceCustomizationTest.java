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

package org.openecomp.mso.db.catalog.test;

import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;

/**
 */

public class NetworkResourceCustomizationTest {

    @Test
    public final void networkResourceCustomizationDataTest() {
        NetworkResourceCustomization networkResourceCustomization = new NetworkResourceCustomization();
        networkResourceCustomization.setModelCustomizationUuid("modelCustomizationUuid");
        assertTrue(networkResourceCustomization.getModelCustomizationUuid().equalsIgnoreCase("modelCustomizationUuid"));
        networkResourceCustomization.setModelInstanceName("modelInstanceName");
        assertTrue(networkResourceCustomization.getModelInstanceName().equalsIgnoreCase("modelInstanceName"));
        networkResourceCustomization.setCreated(new Timestamp(System.currentTimeMillis()));
        assertTrue(networkResourceCustomization.getCreated() != null);
        networkResourceCustomization.setNetworkResource(new NetworkResource());
        assertTrue(networkResourceCustomization.getNetworkResource() != null);
        networkResourceCustomization.setNetworkResourceModelUuid("networkResourceModelUuid");
        assertTrue(networkResourceCustomization.getNetworkResourceModelUuid()
                .equalsIgnoreCase("networkResourceModelUuid"));
        networkResourceCustomization.setNetworkRole("networkRole");
        assertTrue(networkResourceCustomization.getNetworkRole().equalsIgnoreCase("networkRole"));
        networkResourceCustomization.setNetworkScope("networkScope");
        assertTrue(networkResourceCustomization.getNetworkScope().equalsIgnoreCase("networkScope"));
        networkResourceCustomization.setNetworkTechnology("networkTechnology");
        assertTrue(networkResourceCustomization.getNetworkTechnology().equalsIgnoreCase("networkTechnology"));
        networkResourceCustomization.setNetworkType("networkType");
        assertTrue(networkResourceCustomization.getNetworkType().equalsIgnoreCase("networkType"));
//		assertTrue(networkResourceCustomization.toString() != null);

    }

}
