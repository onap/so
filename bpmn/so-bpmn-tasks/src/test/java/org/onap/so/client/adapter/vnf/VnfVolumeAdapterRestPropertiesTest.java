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

package org.onap.so.client.adapter.vnf;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class VnfVolumeAdapterRestPropertiesTest {
    private VnfVolumeAdapterRestProperties props = new VnfVolumeAdapterRestProperties();

    @Test
    public void testGetAuth() {
        assertEquals("mso.adapters.po.auth", VnfVolumeAdapterRestProperties.authProp);
    }

    @Test
    public void testGetKey() {
        assertEquals("mso.msoKey", VnfVolumeAdapterRestProperties.keyProp);
    }

    @Test
    public void testGetUrl() {
        assertEquals("mso.adapters.volume-groups.rest.endpoint", VnfVolumeAdapterRestProperties.endpointProp);
    }
}
