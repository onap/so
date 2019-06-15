/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.serviceinstancebeans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ModelTypeTest {



    @Test
    public void reflectionTest() {
        TestServiceInstanceBean a = new TestServiceInstanceBean();
        TestServiceInstanceBean b = new TestServiceInstanceBean();

        a.setServiceInstanceId("my-id-a");
        a.setServiceInstanceName("my-name-a");

        b.setServiceInstanceId("my-id-b");
        b.setServiceInstanceName("my-name-b");

        assertEquals("my-id-a", ModelType.service.getId(a));
        assertEquals("my-name-a", ModelType.service.getName(a));
        ModelType.service.setName(a, ModelType.service.getName(b));
        ModelType.service.setId(a, ModelType.service.getId(b));
        assertEquals("my-name-b", ModelType.service.getName(a));
        assertEquals("my-id-b", ModelType.service.getId(a));
    }

    @Test
    public void testSilentFail() {
        TestServiceInstanceBean a = new TestServiceInstanceBean();

        a.setServiceInstanceId("my-id-a");
        a.setServiceInstanceName("my-name-a");
        assertNull(ModelType.service.get(a, "NoField"));
    }
}
