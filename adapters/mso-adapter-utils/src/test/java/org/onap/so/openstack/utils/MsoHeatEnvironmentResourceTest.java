/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 TechMahindra
 * ================================================================================ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.openstack.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MsoHeatEnvironmentResourceTest {
    @Test
    public void test() {
        Object op = true;

        MsoHeatEnvironmentResource mre = new MsoHeatEnvironmentResource("name");
        MsoHeatEnvironmentResource mae = new MsoHeatEnvironmentResource("name", "maeValue");
        MsoHeatEnvironmentResource msoHER = new MsoHeatEnvironmentResource();

        msoHER.setName("msoHERName");
        msoHER.setValue("msoHERValue");

        assertEquals("name", mre.getName());
        assertEquals("maeValue", mae.getValue());
        assertEquals("\"msoHERName\": msoHERValue", msoHER.toString());
        assertEquals("\"name\": maeValue", mae.toString());
        assertFalse(mae.equals(op));
        assertTrue(mae.equals(mre));
        assertEquals("name".hashCode(), mae.hashCode());
    }
}
