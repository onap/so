/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.core.json;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.bpmn.core.domain.AllottedResource;
import org.onap.so.bpmn.core.domain.ConfigResource;
import org.onap.so.bpmn.core.domain.NetworkResource;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;
import org.onap.so.bpmn.core.domain.VnfResource;

public class DecomposeJsonUtilTest {

    private VnfResource vnfResource;
    private NetworkResource networkResource;
    private AllottedResource allottedResource;
    private ConfigResource configResource;
    private ServiceDecomposition serviceDecomposition;

    private String serviceInstanceId = "serviceInstanceId";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {

    }

    @Test
    public void testJsonToServiceDecomposition_twoParams() throws JsonDecomposingException {
        serviceDecomposition = createServiceDecompositionData();
        ServiceDecomposition serviceDecompositionObj =
                DecomposeJsonUtil.jsonToServiceDecomposition(serviceDecomposition.toString(), "serviceInstanceId");
        assertEquals(serviceInstanceId, serviceDecompositionObj.getServiceInstance().getInstanceId());
    }

    @Test
    public void testJsonToServiceDecomposition() throws JsonDecomposingException {
        serviceDecomposition = createServiceDecompositionData();
        ServiceDecomposition serviceDecompositionObj =
                DecomposeJsonUtil.jsonToServiceDecomposition(serviceDecomposition.toString());
        assertEquals(serviceDecomposition.getServiceType(), serviceDecompositionObj.getServiceType());
    }

    @Test
    public void testJsonToServiceDecomposition_JsonDecomposingException() throws JsonDecomposingException {
        expectedException.expect(JsonDecomposingException.class);
        vnfResource = createVnfResourceData(); // wrong object
        ServiceDecomposition serviceDecompositionObj =
                DecomposeJsonUtil.jsonToServiceDecomposition(vnfResource.toString());
    }

    @Test
    public void testJsonToVnfResource() throws JsonDecomposingException {
        vnfResource = createVnfResourceData();
        VnfResource vnfResourceObj = DecomposeJsonUtil.jsonToVnfResource(vnfResource.toString());
        assertEquals(vnfResource.getResourceId(), vnfResourceObj.getResourceId());
    }

    @Test
    public void testJsonToVnfResource_JsonDecomposingException() throws JsonDecomposingException {
        expectedException.expect(JsonDecomposingException.class);
        networkResource = createNetworkResourceData(); // wrong object
        VnfResource vnfResourceObj = DecomposeJsonUtil.jsonToVnfResource(networkResource.toString());
    }

    @Test
    public void testJsonToNetworkResource() throws JsonDecomposingException {
        networkResource = createNetworkResourceData();
        NetworkResource networkResourceObj = DecomposeJsonUtil.jsonToNetworkResource(networkResource.toString());
        assertEquals(networkResource.getResourceId(), networkResourceObj.getResourceId());
    }

    @Test
    public void testJsonToNetworkResource_JsonDecomposingException() throws JsonDecomposingException {
        expectedException.expect(JsonDecomposingException.class);
        vnfResource = createVnfResourceData(); // wrong object
        NetworkResource networkResourceObj = DecomposeJsonUtil.jsonToNetworkResource(vnfResource.toString());
    }

    @Test
    public void testJsonToAllottedResource() throws JsonDecomposingException {
        allottedResource = createAllottedResourceData();
        AllottedResource allottedResourceObj = DecomposeJsonUtil.jsonToAllottedResource(allottedResource.toString());
        assertEquals(allottedResource.getResourceId(), allottedResourceObj.getResourceId());
    }

    @Test
    public void testJsonToAllottedResource_JsonDecomposingException() throws JsonDecomposingException {
        expectedException.expect(JsonDecomposingException.class);
        configResource = createConfigResourceData(); // wrong object
        AllottedResource allottedResourceObj = DecomposeJsonUtil.jsonToAllottedResource(configResource.toString());
    }

    @Test
    public void testJsonToConfigResource() throws JsonDecomposingException {
        configResource = createConfigResourceData();
        ConfigResource configResourceObj = DecomposeJsonUtil.jsonToConfigResource(configResource.toString());
        assertEquals(configResource.getResourceId(), configResourceObj.getResourceId());
    }

    @Test
    public void testJsonToConfigResource_JsonDecomposingException() throws JsonDecomposingException {
        expectedException.expect(JsonDecomposingException.class);
        allottedResource = createAllottedResourceData(); // wrong object
        ConfigResource configResourceObj = DecomposeJsonUtil.jsonToConfigResource(allottedResource.toString());
    }

    // data creation section
    private VnfResource createVnfResourceData() {
        vnfResource = new VnfResource();
        vnfResource.setResourceId("resourceId");
        vnfResource.setNfFunction("nfFunction");
        vnfResource.setNfNamingCode("nfNamingCode");
        vnfResource.setNfRole("nfRole");
        return vnfResource;
    }

    private NetworkResource createNetworkResourceData() {
        networkResource = new NetworkResource();
        networkResource.setNetworkRole("networkRole");
        networkResource.setResourceId("resourceId");
        return networkResource;
    }

    private AllottedResource createAllottedResourceData() {
        allottedResource = new AllottedResource();
        allottedResource.setResourceId("resourceId");
        allottedResource.setNfFunction("nfFunction");
        allottedResource.setNfNamingCode("nfNamingCode");
        allottedResource.setNfRole("nfRole");
        return allottedResource;
    }

    private ConfigResource createConfigResourceData() {
        configResource = new ConfigResource();
        configResource.setResourceId("resourceId");
        configResource.setToscaNodeType("toscaNodeType");
        return configResource;
    }

    private ServiceDecomposition createServiceDecompositionData() {
        serviceDecomposition = new ServiceDecomposition();
        serviceDecomposition.setSdncVersion("sdncVersion");
        serviceDecomposition.setServiceRole("serviceRole");
        serviceDecomposition.setServiceType("serviceType");
        return serviceDecomposition;
    }
}
