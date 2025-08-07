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
package org.onap.so.bpmn.core.domain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ModuleResourceTest {
    private ModuleResource moduleresource = new ModuleResource();

    @Test
    public void testModuleResource() {

        moduleresource.setVfModuleName("vfModuleName");
        moduleresource.setHeatStackId("heatStackId");
        moduleresource.setIsBase(true);
        moduleresource.setVfModuleLabel("vfModuleLabel");
        moduleresource.setInitialCount(0);
        moduleresource.setVfModuleType("vfModuleType");
        moduleresource.setHasVolumeGroup(true);
        assertEquals(moduleresource.getVfModuleName(), "vfModuleName");
        assertEquals(moduleresource.getHeatStackId(), "heatStackId");
        assertEquals(moduleresource.getIsBase(), true);
        assertEquals(moduleresource.getVfModuleLabel(), "vfModuleLabel");
        assertEquals(moduleresource.getInitialCount(), 0);
        assertEquals(moduleresource.getVfModuleType(), "vfModuleType");
        assertEquals(moduleresource.isHasVolumeGroup(), true);
    }

}
