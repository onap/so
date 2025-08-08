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

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class ServiceDecompositionTest {
    private static final String RESOURCE_PATH = "src/test/resources/json-examples/";

    VnfResource vnfResource;
    NetworkResource networkResource;
    AllottedResource allottedResource;
    ConfigResource configResource;

    @Before
    public void before() {
        vnfResource = new VnfResource();
        vnfResource.setResourceId("vnfResourceId");
        vnfResource.setModules(new ArrayList<>());

        networkResource = new NetworkResource();
        networkResource.setResourceId("networkResourceId");

        allottedResource = new AllottedResource();
        allottedResource.setResourceId("allottedResourceId");

        configResource = new ConfigResource();
        configResource.setResourceId("configResourceId");
    }


    @Test
    public void serviceDecompositionWithGroupandVnfc() throws IOException {
        String sericeStr = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "ServiceWithGroupandVnfc.json")));
        ServiceDecomposition serviceDecomposition = new ServiceDecomposition(sericeStr);

        assertEquals(1, serviceDecomposition.getVnfResources().size());
        assertEquals(1, serviceDecomposition.getVnfResources().get(0).getGroups().size());
        assertEquals(1, serviceDecomposition.getVnfResources().get(0).getGroups().get(0).getVnfcs().size());

        VnfcResource vnfcResource = serviceDecomposition.getVnfResources().get(0).getGroups().get(0).getVnfcs().get(0);

        assertEquals("xfs", vnfcResource.getModelInfo().getModelName());
        assertEquals("22", vnfcResource.getModelInfo().getModelUuid());
    }

    @Test
    public void serviceDecompositionTest() throws IOException {
        // covering methods not covered by openpojo test
        String catalogRestOutput =
                new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "ServiceDecomposition.json")));

        ServiceDecomposition serviceDecomp = new ServiceDecomposition(catalogRestOutput);
        serviceDecomp.addVnfResource(vnfResource);
        serviceDecomp.addNetworkResource(networkResource);
        serviceDecomp.addAllottedResource(allottedResource);
        serviceDecomp.addConfigResource(configResource);

        assertThat(serviceDecomp.getServiceResource(vnfResource.getResourceId()), sameBeanAs(vnfResource));
        assertThat(serviceDecomp.getServiceResource(networkResource.getResourceId()), sameBeanAs(networkResource));
        assertThat(serviceDecomp.getServiceResource(allottedResource.getResourceId()), sameBeanAs(allottedResource));
        assertThat(serviceDecomp.getServiceResource(configResource.getResourceId()), sameBeanAs(configResource));

        VnfResource vnfResourceReplace = new VnfResource();
        vnfResourceReplace.setResourceId(vnfResource.getResourceId());
        vnfResourceReplace.setResourceInstanceName("vnfResourceReplaceInstanceName");

        assertTrue(serviceDecomp.replaceResource(vnfResourceReplace));
        assertTrue(serviceDecomp.getVnfResources().contains(vnfResourceReplace));

        assertTrue(serviceDecomp.deleteResource(vnfResourceReplace));
        assertFalse(serviceDecomp.deleteResource(vnfResourceReplace));
        assertFalse(serviceDecomp.deleteResource(new VnfResource()));
    }

    @Test
    public void serviceDecompositionJsonTest() throws IOException {
        String catalogRestOutput =
                new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "ServiceDecomposition.json")));
        String expectedCatalogRestOutput =
                new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "ServiceDecompositionExpected.json")));
        String vnfResourceJson = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "VnfResource.json")));
        String networkResourceJson = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "NetworkResource.json")));
        String allottedResourceJson =
                new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "AllottedResource.json")));
        String configResourceJson = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "ConfigResource.json")));

        ServiceDecomposition serviceDecomp = new ServiceDecomposition(catalogRestOutput, "serviceInstanceId");
        serviceDecomp.addResource(vnfResource);
        serviceDecomp.addResource(networkResource);
        serviceDecomp.addResource(allottedResource);
        serviceDecomp.addResource(configResource);

        System.out.println(serviceDecomp.toJsonString());

        assertThat(serviceDecomp.getServiceResource(vnfResource.getResourceId()), sameBeanAs(vnfResource));
        assertThat(serviceDecomp.getServiceResource(networkResource.getResourceId()), sameBeanAs(networkResource));
        assertThat(serviceDecomp.getServiceResource(allottedResource.getResourceId()), sameBeanAs(allottedResource));
        assertThat(serviceDecomp.getServiceResource(configResource.getResourceId()), sameBeanAs(configResource));

        serviceDecomp = new ServiceDecomposition(catalogRestOutput, "serviceInstanceId");
        serviceDecomp.addVnfResource(vnfResourceJson);
        serviceDecomp.addNetworkResource(networkResourceJson);
        serviceDecomp.addAllottedResource(allottedResourceJson);
        serviceDecomp.addConfigResource(configResourceJson);

        ServiceDecomposition expectedServiceDecomp =
                new ServiceDecomposition(expectedCatalogRestOutput, "serviceInstanceId");

        assertThat(serviceDecomp, sameBeanAs(expectedServiceDecomp));
        assertEquals(
                serviceDecomp.listToJson(Arrays.asList(networkResource))
                        + serviceDecomp.listToJson(Arrays.asList(vnfResource))
                        + serviceDecomp.listToJson(Arrays.asList(allottedResource))
                        + serviceDecomp.listToJson(Arrays.asList(configResource)),
                serviceDecomp.getServiceResourcesJsonString());
    }

}
