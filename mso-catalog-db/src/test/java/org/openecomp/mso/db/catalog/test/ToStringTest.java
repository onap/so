/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.Model;
import org.openecomp.mso.db.catalog.beans.ModelRecipe;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceMacroHolder;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.db.catalog.beans.ServiceToResourceCustomization;
import org.openecomp.mso.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VnfComponent;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;

public class ToStringTest {

    @Test
    public void testTModelRecipeToString() {
        ModelRecipe mr = new ModelRecipe();
        mr.setCreated(new Timestamp(10001));
        mr.setModelId(102);
        mr.setRecipeTimeout(100);
        String str = mr.toString();
        assertTrue(str != null);
    }

    @Test
    public void networkResourcetoStringTest() {
        NetworkResource nr = new NetworkResource();
        nr.setCreated(new Timestamp(10000));
        String str = nr.toString();
        assertTrue(str != null);
    }

    @Test
    public void modelTestToString() {
        Model m = new Model();
        m.setCreated(new Timestamp(100000));
        m.setId(1001);
        m.setModelCustomizationId("10012");
        String str = m.toString();
        assertTrue(str != null);
    }

    @Test
    public void serviceMacroHolderTest() {
        ServiceMacroHolder smh = new ServiceMacroHolder();
        Service service = new Service();
        Map<String, ServiceRecipe> recipes = new HashMap<>();
        recipes.put("test", new ServiceRecipe());
        service.setRecipes(recipes);

        Set<ServiceToResourceCustomization> serviceResourceCustomizations = new HashSet<>();
        ServiceToResourceCustomization sr = new ServiceToResourceCustomization();
        serviceResourceCustomizations.add(sr);
        service.setServiceResourceCustomizations(serviceResourceCustomizations);
        smh.setService(service);

        ArrayList<VnfResource> vnflist = new ArrayList<>();
        smh.setVnfResources(vnflist);

        VnfResource vr = new VnfResource();
        Set<VnfResourceCustomization> vnfResourceCustomization = new HashSet<>();
        vnfResourceCustomization.add(new VnfResourceCustomization());
        vr.setVnfResourceCustomizations(vnfResourceCustomization);

        Set<VfModule> vfModules = new HashSet<>();
        vfModules.add(new VfModule());
        vr.setVfModules(vfModules);
        smh.addVnfResource(vr);

        ArrayList<VnfResourceCustomization> vnfResourceCustomizations = new ArrayList<>();
        smh.setVnfResourceCustomizations(vnfResourceCustomizations);

        VnfResourceCustomization vrc = new VnfResourceCustomization();
        smh.addVnfResourceCustomizations(vrc);

        ArrayList<NetworkResourceCustomization> networkResourceCustomizations = new ArrayList<>();
        smh.setNetworkResourceCustomization(networkResourceCustomizations);

        NetworkResourceCustomization nrc = new NetworkResourceCustomization();
        smh.addNetworkResourceCustomization(nrc);

        ArrayList<AllottedResourceCustomization> allottedResourceCustomizations = new ArrayList<>();
        smh.setAllottedResourceCustomization(allottedResourceCustomizations);

        AllottedResourceCustomization arc = new AllottedResourceCustomization();
        smh.addAllottedResourceCustomization(arc);

        String str = smh.toString();
        assertTrue(str != null);
    }

    @Test
    public void heatFilesTest() {
        HeatFiles hf = new HeatFiles();
        String str = hf.toString();
        assertTrue(str != null);

    }

    @Test
    public void testVnfConponent() {
        VnfComponent vnf = new VnfComponent();
        String str = vnf.toString();
        assertTrue(str != null);
    }

    @Test
    public void testTempNetworkHeatTemplateLookup() {
        TempNetworkHeatTemplateLookup tn = new TempNetworkHeatTemplateLookup();
        String str = tn.toString();
        assertTrue(str != null);
    }


}
