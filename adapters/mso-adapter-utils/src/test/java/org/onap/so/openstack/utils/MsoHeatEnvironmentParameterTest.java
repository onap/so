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

import static org.mockito.Mockito.mock;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class MsoHeatEnvironmentParameterTest {

    @Test
    public void test() {
        MsoHeatEnvironmentParameter hep = mock(MsoHeatEnvironmentParameter.class);
        Object op = hep.getName();
        MsoHeatEnvironmentParameter meo = new MsoHeatEnvironmentParameter();
        MsoHeatEnvironmentParameter mea = new MsoHeatEnvironmentParameter("name");
        MsoHeatEnvironmentParameter mep = new MsoHeatEnvironmentParameter("name", " value");
        mea.setName("name");
        mep.setValue("value");
        assert (mea.getName().equals("name"));
        assert (mep.getValue().equals("value"));
        assert (meo.toString() != null);
        // assertTrue(op.equals(hep));
        meo.equals(op);
        meo.hashCode();
        assertNotNull(mea);
    }

}
