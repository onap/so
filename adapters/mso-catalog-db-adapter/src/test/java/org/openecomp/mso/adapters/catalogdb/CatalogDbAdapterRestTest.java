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