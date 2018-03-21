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
package org.openecomp.mso.adapters.catalogdb;

import org.junit.Test;

public class CatalogDbAdapterRestTest {

    CatalogDbAdapterRest catalogDbAdapterRest = new CatalogDbAdapterRest();

    @Test(expected = NullPointerException.class)
    public void respond() throws Exception {
        catalogDbAdapterRest.respond(null, 0, true, null);
    }

    @Test
    public void healthcheck() throws Exception {
    	catalogDbAdapterRest.healthcheck("test");
    }

    @Test
    public void serviceVnfs() throws Exception {
    	catalogDbAdapterRest.serviceVnfs("test", "test");
    }

    @Test
    public void serviceVnfs1() throws Exception {
    	catalogDbAdapterRest.serviceVnfs("test", "test", "test", "test", "test", "test");
    }

    @Test
    public void serviceVnfsImpl() throws Exception {
    	catalogDbAdapterRest.serviceVnfsImpl("test", false, "test", "test", "test", "test", "test");
    }

    @Test
    public void serviceNetworks() throws Exception {
    	catalogDbAdapterRest.serviceNetworks("test", "test");
    }

    @Test
    public void serviceNetworks1() throws Exception {
    	catalogDbAdapterRest.serviceNetworks("test", "test", "test", "test", "test", "test", "test", "test");
    }

    @Test
    public void serviceNetworksImpl() throws Exception {
    	catalogDbAdapterRest.serviceNetworksImpl("test", false, "test", "test", "test", "test", "test");
    }

    @Test
    public void serviceResources() throws Exception {
    	catalogDbAdapterRest.serviceResources("test", "test", "test", "test");
    }

    @Test
    public void serviceAllottedResources() throws Exception {
    	catalogDbAdapterRest.serviceAllottedResources("test", "test");
    }

    @Test
    public void serviceAllottedResources1() throws Exception {
    	catalogDbAdapterRest.serviceAllottedResources("test", "test", "test", "test", "test");
    }

    @Test
    public void serviceAllottedResourcesImpl() throws Exception {
    	catalogDbAdapterRest.serviceAllottedResourcesImpl("test", false, "test", "test", "test", "test");
    }

    @Test
    public void vfModules() throws Exception {
    	catalogDbAdapterRest.vfModules("test");
    }

    @Test
    public void serviceToscaCsar() throws Exception {
    	catalogDbAdapterRest.serviceToscaCsar("test");
    }

    @Test
    public void resourceRecipe() throws Exception {
    	catalogDbAdapterRest.resourceRecipe("test", "test");
    }

}