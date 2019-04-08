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

package org.onap.so.asdc.client.test.emulators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.so.asdc.BaseTest;

public class ResourceInfoImplTest extends BaseTest {
    @Mock
    private IResourceInstance iResourceInstance;

    @Test
    public void convertToJsonContainerTest() {
        List<IResourceInstance> resources = new ArrayList<IResourceInstance>();
        resources.add(iResourceInstance);
        ResourceInfoImpl.convertToJsonContainer(resources);

        assertEquals(1, ResourceInfoImpl.convertToJsonContainer(resources).size());
    }

    @Test
    public void convertToJsonContainerNullListTest() {
        assertTrue(ResourceInfoImpl.convertToJsonContainer(null).isEmpty());
    }
}
