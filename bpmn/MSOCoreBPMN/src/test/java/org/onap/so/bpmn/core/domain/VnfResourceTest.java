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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VnfResourceTest {

    private final static String ALL_VF_MODULES_JSON =
            "{\"ArrayList\":[{\"resourceType\":\"MODULE\",\"resourceInstance\":{},\"homingSolution\":{\"license\":{},\"rehome\":false},\"vfModuleName\":\"vfModuleName\",\"vfModuleType\":\"vfModuleType\",\"heatStackId\":\"heatStackId\",\"hasVolumeGroup\":true,\"isBase\":true,\"vfModuleLabel\":\"vfModuleLabel\",\"initialCount\":0},{\"resourceType\":\"MODULE\",\"resourceInstance\":{},\"homingSolution\":{\"license\":{},\"rehome\":false},\"vfModuleName\":\"vfModuleName\",\"vfModuleType\":\"vfModuleType\",\"heatStackId\":\"heatStackId\",\"hasVolumeGroup\":true,\"isBase\":true,\"vfModuleLabel\":\"vfModuleLabel\",\"initialCount\":0}]}";

    private VnfResource vnf = new VnfResource();
    List<ModuleResource> moduleResources;

    @Test
    public void testVnfResource() {
        vnf.setModules(moduleResources);
        vnf.setVnfHostname("vnfHostname");
        vnf.setVnfType("vnfType");
        vnf.setNfFunction("nfFunction");
        vnf.setNfType("nfType");
        vnf.setNfRole("nfRole");
        vnf.setNfNamingCode("nfNamingCode");
        vnf.setMultiStageDesign("multiStageDesign");
        assertEquals(vnf.getVfModules(), moduleResources);
        assertEquals(vnf.getVnfHostname(), "vnfHostname");
        assertEquals(vnf.getVnfType(), "vnfType");
        assertEquals(vnf.getNfFunction(), "nfFunction");
        assertEquals(vnf.getNfType(), "nfType");
        assertEquals(vnf.getNfRole(), "nfRole");
        assertEquals(vnf.getNfNamingCode(), "nfNamingCode");
        assertEquals(vnf.getMultiStageDesign(), "multiStageDesign");


    }

    @Test
    public void vnfResourceMapperTest() throws IOException {
        String jsonStr = "{\"vnfHostname\": \"home\", \"resourceInput\": \"sample\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        VnfResource vnfResource = objectMapper.readValue(jsonStr, VnfResource.class);

        assertTrue(vnfResource != null);
        assertEquals("sample", vnfResource.getResourceInput());
        assertEquals("home", vnfResource.getVnfHostname());
    }

    @Test
    public void vnfResourceMapperTestNoResourceInput() throws IOException {
        String jsonStr = "{\"vnfHostname\": \"home\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        VnfResource vnfResource = objectMapper.readValue(jsonStr, VnfResource.class);

        assertTrue(vnfResource != null);
        assertEquals(null, vnfResource.getResourceInput());
        assertEquals("home", vnfResource.getVnfHostname());
    }

    @Test
    public void testVfModules() {

        moduleResources = new ArrayList<>();

        ModuleResource moduleresource = new ModuleResource();
        moduleresource.setVfModuleName("vfModuleName");
        moduleresource.setHeatStackId("heatStackId");
        moduleresource.setIsBase(true);
        moduleresource.setVfModuleLabel("vfModuleLabel");
        moduleresource.setInitialCount(0);
        moduleresource.setVfModuleType("vfModuleType");
        moduleresource.setHasVolumeGroup(true);

        moduleResources.add(moduleresource);

        vnf.setModules(moduleResources);
        assertEquals(vnf.getVfModules(), moduleResources);

        List<ModuleResource> moduleResources = vnf.getAllVfModuleObjects();
        assertEquals(1, moduleResources.size());

        vnf.addVfModule(moduleresource);
        moduleResources = vnf.getAllVfModuleObjects();
        assertEquals(2, moduleResources.size());

        assertEquals(ALL_VF_MODULES_JSON, vnf.getAllVfModulesJson());

    }

}
