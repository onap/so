/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.orchestration;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.RouteTableReference;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AAINetworkResourcesTest extends TestDataSetup {

    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/";

    private L3Network network;
    private Collection collection;
    private InstanceGroup instanceGroup;
    private ServiceInstance serviceInstance;
    private CloudRegion cloudRegion;
    private Subnet subnet;
    private NetworkPolicy networkPolicy;

    @Mock
    protected AAIResourcesClient MOCK_aaiResourcesClient;

    @Mock
    protected AAIObjectMapper MOCK_aaiObjectMapper;

    @Mock
    protected InjectionHelper MOCK_injectionHelper;

    @InjectMocks
    private AAINetworkResources aaiNetworkResources = new AAINetworkResources();

    @Before
    public void before() {
        network = buildL3Network();

        collection = buildCollection();

        List<L3Network> l3NetworkList = new ArrayList<L3Network>();
        l3NetworkList.add(network);

        instanceGroup = buildInstanceGroup();

        serviceInstance = buildServiceInstance();

        cloudRegion = buildCloudRegion();

        subnet = buildSubnet();

        networkPolicy = buildNetworkPolicy();

        doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
    }


    @Test
    public void updateNetworkTest() {

        network.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

        doReturn(new org.onap.aai.domain.yang.L3Network()).when(MOCK_aaiObjectMapper).mapNetwork(network);
        doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.L3Network.class));

        aaiNetworkResources.updateNetwork(network);

        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.L3Network.class));
        assertEquals(OrchestrationStatus.ASSIGNED, network.getOrchestrationStatus());
    }

    @Test
    public void createNetworkConnectToServiceInstanceTest() {

        network.setOrchestrationStatus(OrchestrationStatus.PRECREATED);

        doReturn(new org.onap.aai.domain.yang.L3Network()).when(MOCK_aaiObjectMapper).mapNetwork(network);
        doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(any(AAIResourceUri.class),
                any(Optional.class));
        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), any(AAIResourceUri.class));

        aaiNetworkResources.createNetworkConnectToServiceInstance(network, serviceInstance);

        assertEquals(OrchestrationStatus.INVENTORIED, network.getOrchestrationStatus());

        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void deleteNetworkTest() {

        network.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);

        doNothing().when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));

        aaiNetworkResources.deleteNetwork(network);

        verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
    }

    @Test
    public void getVpnBindingTest() throws Exception {
        final String content =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "queryAaiVpnBinding.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
        Optional<VpnBinding> oVpnBinding = Optional.empty();
        AAIResourceUri aaiUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().vpnBinding("ModelInvariantUUID"));

        doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(isA(AAIResourceUri.class));
        oVpnBinding = aaiNetworkResources.getVpnBinding(aaiUri);
        verify(MOCK_aaiResourcesClient, times(1)).get(any(AAIResourceUri.class));

        if (oVpnBinding.isPresent()) {
            VpnBinding vpnBinding = oVpnBinding.get();
            assertThat(aaiResultWrapper.asBean(VpnBinding.class).get(), sameBeanAs(vpnBinding));
        }
    }

    @Test
    public void getNetworkPolicyTest() throws Exception {
        final String content =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "queryAaiNetworkPolicy.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
        Optional<org.onap.aai.domain.yang.NetworkPolicy> oNetPolicy = Optional.empty();
        AAIResourceUri netPolicyUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicy("ModelInvariantUUID"));

        doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(isA(AAIResourceUri.class));
        oNetPolicy = aaiNetworkResources.getNetworkPolicy(netPolicyUri);
        verify(MOCK_aaiResourcesClient, times(1)).get(any(AAIResourceUri.class));
        if (oNetPolicy.isPresent()) {
            org.onap.aai.domain.yang.NetworkPolicy networkPolicy = oNetPolicy.get();
            assertThat(aaiResultWrapper.asBean(org.onap.aai.domain.yang.NetworkPolicy.class).get(),
                    sameBeanAs(networkPolicy));
        }
    }

    @Test
    public void getNetworkPoliciesTest() throws Exception {
        final String content =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "queryAaiNetworkPolicies.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
        Optional<org.onap.aai.domain.yang.NetworkPolicies> oNetPolicies = Optional.empty();
        AAIPluralResourceUri netPoliciesUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicies());

        doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(isA(AAIPluralResourceUri.class));
        oNetPolicies = aaiNetworkResources.getNetworkPolicies(netPoliciesUri);
        verify(MOCK_aaiResourcesClient, times(1)).get(any(AAIPluralResourceUri.class));
        if (oNetPolicies.isPresent()) {
            org.onap.aai.domain.yang.NetworkPolicies networkPolicies = oNetPolicies.get();
            assertThat(aaiResultWrapper.asBean(org.onap.aai.domain.yang.NetworkPolicies.class).get(),
                    sameBeanAs(networkPolicies));
        }
    }

    @Test
    public void getRouteTableTest() throws Exception {
        final String content =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "queryAaiNetworkTableRefs.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
        Optional<RouteTableReference> oRtref = Optional.empty();
        AAIResourceUri rTRefUri = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.network().routeTableReference("ModelInvariantUUID"));

        doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(isA(AAIResourceUri.class));
        oRtref = aaiNetworkResources.getRouteTable(rTRefUri);
        verify(MOCK_aaiResourcesClient, times(1)).get(any(AAIResourceUri.class));

        if (oRtref.isPresent()) {
            RouteTableReference rTref = oRtref.get();
            assertThat(aaiResultWrapper.asBean(RouteTableReference.class).get(), sameBeanAs(rTref));
        }
    }

    @Test
    public void queryNetworkByIdTest() throws Exception {
        final String content =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiQueryAAIResponse-Wrapper.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
        L3Network network = new L3Network();
        network.setNetworkId("0384d743-f69b-4cc8-9aa8-c3ae66662c44");
        network.setNetworkName("Dev_Bindings_1802_020118");
        network.setOrchestrationStatus(OrchestrationStatus.CREATED);

        doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(isA(AAIResourceUri.class));
        Optional<org.onap.aai.domain.yang.L3Network> l3NetworkOpt = aaiNetworkResources.queryNetworkById(network);
        org.onap.aai.domain.yang.L3Network l3Network = l3NetworkOpt.isPresent() ? l3NetworkOpt.get() : null;

        verify(MOCK_aaiResourcesClient, times(1)).get(isA(AAIResourceUri.class));
        assertNotNull(l3Network);
        assertEquals("0384d743-f69b-4cc8-9aa8-c3ae66662c44", l3Network.getNetworkId());
        assertEquals("Dev_Bindings_1802_020118", l3Network.getNetworkName());
    }

    @Test
    public void queryNetworkWrapperByIdTest() throws Exception {
        final String content =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiQueryAAIResponse-Wrapper.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
        L3Network network = new L3Network();
        network.setNetworkId("0384d743-f69b-4cc8-9aa8-c3ae66662c44");
        network.setNetworkName("Dev_Bindings_1802_020118");
        network.setOrchestrationStatus(OrchestrationStatus.CREATED);

        doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(isA(AAIResourceUri.class));
        AAIResultWrapper result = aaiNetworkResources.queryNetworkWrapperById(network);

        verify(MOCK_aaiResourcesClient, times(1)).get(isA(AAIResourceUri.class));
        assertEquals(aaiResultWrapper.getJson(), result.getJson());
        assertNotNull(result);
        Optional<Relationships> resultNetworkRelationships = result.getRelationships();
        assertTrue(resultNetworkRelationships.isPresent());
        Optional<org.onap.aai.domain.yang.L3Network> aaiL3Network =
                result.asBean(org.onap.aai.domain.yang.L3Network.class);
        assertEquals(network.getNetworkId(), aaiL3Network.get().getNetworkId());
        assertEquals(network.getNetworkName(), aaiL3Network.get().getNetworkName());


    }

    @Test
    public void createNetworkCollectionTest() {

        doNothing().when(MOCK_aaiResourcesClient).create(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.Collection.class));
        doReturn(new org.onap.aai.domain.yang.Collection()).when(MOCK_aaiObjectMapper).mapCollection(collection);
        collection.setOrchestrationStatus(OrchestrationStatus.PRECREATED);

        aaiNetworkResources.createNetworkCollection(collection);
        assertEquals(OrchestrationStatus.INVENTORIED, collection.getOrchestrationStatus());
        verify(MOCK_aaiResourcesClient, times(1)).create(any(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.Collection.class));
    }

    @Test
    public void createNetworkInstanceGroupTest() {
        doReturn(new org.onap.aai.domain.yang.InstanceGroup()).when(MOCK_aaiObjectMapper)
                .mapInstanceGroup(instanceGroup);
        doNothing().when(MOCK_aaiResourcesClient).create(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.InstanceGroup.class));
        aaiNetworkResources.createNetworkInstanceGroup(instanceGroup);
        verify(MOCK_aaiResourcesClient, times(1)).create(any(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.InstanceGroup.class));
    }

    @Test
    public void connectNetworkToNetworkCollectionInstanceGroupTest() {
        aaiNetworkResources.connectNetworkToNetworkCollectionInstanceGroup(network, instanceGroup);
        verify(MOCK_aaiResourcesClient, times(1)).connect(
                eq(AAIUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()))),
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(network.getNetworkId()))));
    }

    @Test
    public void connectNetworkToNetworkCollectionServiceInstanceTest() {
        aaiNetworkResources.connectNetworkToNetworkCollectionServiceInstance(network, serviceInstance);
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class),
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(network.getNetworkId()))));
    }

    @Test
    public void connectNetworkToCloudRegionTest() {
        aaiNetworkResources.connectNetworkToCloudRegion(network, cloudRegion);
        verify(MOCK_aaiResourcesClient, times(1)).connect(
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(network.getNetworkId()))),
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion(cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId()))));
    }

    @Test
    public void connectNetworkToTenantTest() {
        aaiNetworkResources.connectNetworkToTenant(network, cloudRegion);
        verify(MOCK_aaiResourcesClient, times(1)).connect(
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion(cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId())
                        .tenant(cloudRegion.getTenantId()))),
                (eq(AAIUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.network().l3Network(network.getNetworkId())))));
    }

    @Test
    public void connectNetworkCollectionInstanceGroupToNetworkCollectionTest() {
        aaiNetworkResources.connectNetworkCollectionInstanceGroupToNetworkCollection(instanceGroup, collection);
        verify(MOCK_aaiResourcesClient, times(1)).connect(
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().collection(collection.getId()))),
                eq(AAIUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()))));
    }

    @Test
    public void connectNetworkCollectionToServiceInstanceTest() {
        aaiNetworkResources.connectNetworkCollectionToServiceInstance(collection, serviceInstance);
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void deleteCollectionTest() {
        doNothing().when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));
        aaiNetworkResources.deleteCollection(collection);
        verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
    }

    @Test
    public void deleteInstanceGroupTest() {
        doNothing().when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));
        aaiNetworkResources.deleteNetworkInstanceGroup(instanceGroup);
        verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
    }

    @Test
    public void updateSubnetTest() {

        doReturn(new org.onap.aai.domain.yang.Subnet()).when(MOCK_aaiObjectMapper).mapSubnet(subnet);
        doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.Subnet.class));

        aaiNetworkResources.updateSubnet(network, subnet);

        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.Subnet.class));
    }

    @Test
    public void connectInstanceGroupToCloudRegionTest() {
        aaiNetworkResources.connectInstanceGroupToCloudRegion(instanceGroup, cloudRegion);
        verify(MOCK_aaiResourcesClient, times(1)).connect(
                eq(AAIUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()))),
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion(cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId()))),
                eq(AAIEdgeLabel.USES));
    }

    @Test
    public void getSubnetTest() throws Exception {
        final String content =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiSubnetsMapped_to_aai.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
        Optional<org.onap.aai.domain.yang.Subnet> oSubnet = Optional.empty();
        AAIResourceUri subnetUri = AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.network().l3Network("ModelInvariantUUID").subnet("serviceModelVersionId"));

        doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(isA(AAIResourceUri.class));
        oSubnet = aaiNetworkResources.getSubnet(subnetUri);
        verify(MOCK_aaiResourcesClient, times(1)).get(any(AAIResourceUri.class));

        if (oSubnet.isPresent()) {
            org.onap.aai.domain.yang.Subnet subnet = oSubnet.get();
            assertThat(aaiResultWrapper.asBean(org.onap.aai.domain.yang.Subnet.class).get(), sameBeanAs(subnet));
        }
    }

    @Test
    public void createNetworkPolicyTest() {
        doNothing().when(MOCK_aaiResourcesClient).create(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.NetworkPolicy.class));
        doReturn(new org.onap.aai.domain.yang.NetworkPolicy()).when(MOCK_aaiObjectMapper)
                .mapNetworkPolicy(networkPolicy);
        aaiNetworkResources.createNetworkPolicy(networkPolicy);
        verify(MOCK_aaiResourcesClient, times(1)).create(any(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.NetworkPolicy.class));
    }

    @Test
    public void deleteNetworkPolicyTest() {
        doNothing().when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));
        aaiNetworkResources.deleteNetworkPolicy(networkPolicy.getNetworkPolicyId());
        verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
    }

    @Test
    public void checkInstanceGroupNameInUseTrueTest() {
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Networks())
                .queryParam("network-name", "networkName");
        doReturn(true).when(MOCK_aaiResourcesClient).exists(eq(uri));
        boolean nameInUse = aaiNetworkResources.checkNetworkNameInUse("networkName");
        assertTrue(nameInUse);
    }

    @Test
    public void checkInstanceGroupNameInUseFalseTest() {
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Networks())
                .queryParam("network-name", "networkName");
        doReturn(false).when(MOCK_aaiResourcesClient).exists(eq(uri));
        boolean nameInUse = aaiNetworkResources.checkNetworkNameInUse("networkName");
        assertFalse(nameInUse);
    }

}
