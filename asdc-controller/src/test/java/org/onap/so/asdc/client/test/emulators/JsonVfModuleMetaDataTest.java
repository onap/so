/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.onap.so.asdc.client.test.emulators;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import org.junit.Test;

public class JsonVfModuleMetaDataTest {

    @Test
    public void attributesMapTest() {
        JsonVfModuleMetaData vfModuleMetadata = new JsonVfModuleMetaData();
        vfModuleMetadata.setAttribute("vfModuleModelDescription", "vfModuleModelDescription");
        vfModuleMetadata.setAttribute("vfModuleModelInvariantUUID", "vfModuleModelInvariantUUID");
        vfModuleMetadata.setAttribute("vfModuleModelCustomizationUUID", "vfModuleModelCustomizationUUID");
        vfModuleMetadata.setAttribute("vfModuleModelName", "vfModuleModelName");
        vfModuleMetadata.setAttribute("vfModuleModelUUID", "vfModuleModelUUID");
        vfModuleMetadata.setAttribute("vfModuleModelVersion", "vfModuleModelVersion");
        vfModuleMetadata.setAttribute("isBase", true);

        assertEquals("vfModuleModelDescription", vfModuleMetadata.getVfModuleModelDescription());
        assertEquals("vfModuleModelInvariantUUID", vfModuleMetadata.getVfModuleModelInvariantUUID());
        assertEquals("vfModuleModelCustomizationUUID", vfModuleMetadata.getVfModuleModelCustomizationUUID());
        assertEquals("vfModuleModelName", vfModuleMetadata.getVfModuleModelName());
        assertEquals("vfModuleModelUUID", vfModuleMetadata.getVfModuleModelUUID());
        assertEquals("vfModuleModelVersion", vfModuleMetadata.getVfModuleModelVersion());
        assertEquals(true, vfModuleMetadata.isBase());
        assertEquals(null, vfModuleMetadata.getArtifacts());
        assertEquals(new HashMap<String, String>(), vfModuleMetadata.getProperties());
    }
}
