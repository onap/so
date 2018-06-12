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

package org.openecomp.mso.client.orchestration;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aai.domain.yang.NetworkPolicy;
import org.onap.aai.domain.yang.RouteTableReference;
import org.onap.aai.domain.yang.VpnBinding;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.BuildingBlockTestDataSetup;
import org.openecomp.mso.bpmn.common.InjectionHelper;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Collection;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.entities.Relationships;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.aai.mapper.AAIObjectMapper;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;

@RunWith(MockitoJUnitRunner.class)
public class AAINetworkResourcesTest extends BuildingBlockTestDataSetup{
	
	@InjectMocks
	private AAINetworkResources aaiNetworkResources = new AAINetworkResources();
	
	private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/";
	
	private L3Network network;
	private Collection collection;
	private InstanceGroup instanceGroup;
	private ServiceInstance serviceInstance;
	
	@Mock
	protected AAIResourcesClient MOCK_aaiResourcesClient;
    
    @Mock
    protected AAIObjectMapper MOCK_aaiObjectMapper;
    
    @Mock
    protected InjectionHelper MOCK_injectionHelper;
	
	@Before
	public void before() {
		network = buildL3Network();
		
		collection = buildCollection();
		
		List<L3Network> l3NetworkList = new ArrayList<L3Network>();
		l3NetworkList.add(network);
		
		instanceGroup = buildInstanceGroup();
		instanceGroup.setL3Networks(l3NetworkList);
		
		serviceInstance = buildServiceInstance();
		doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
	}
	

	@Test
	public void updateNetworkTest() throws Exception {

		network.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
		
		doReturn(new org.onap.aai.domain.yang.L3Network()).when(MOCK_aaiObjectMapper).mapNetwork(network);
		doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.L3Network.class));
		
		aaiNetworkResources.updateNetwork(network);

		verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class), isA(org.onap.aai.domain.yang.L3Network.class));
		assertEquals(OrchestrationStatus.ASSIGNED, network.getOrchestrationStatus());
	}
	
	@Test
	public void createNetworkConnectToServiceInstanceTest() throws Exception {

		network.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		
		doReturn(new org.onap.aai.domain.yang.L3Network()).when(MOCK_aaiObjectMapper).mapNetwork(network);
		doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
		doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), any(AAIResourceUri.class));
		
		aaiNetworkResources.createNetworkConnectToServiceInstance(network, serviceInstance);
		
		assertEquals(OrchestrationStatus.INVENTORIED, network.getOrchestrationStatus());

        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
	}
	
	@Test
	public void deleteNetworkTest() throws Exception {

		network.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
		
		doNothing().when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));
		
		aaiNetworkResources.deleteNetwork(network);

		verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
	}
	
	@Test
	public void getVpnBindingTest() throws Exception {
		final String content = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "queryAaiVpnBinding.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		Optional<VpnBinding> oVpnBinding = Optional.empty();
		AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIObjectType.VPN_BINDING, "ModelInvariantUUID", "serviceModelVersionId");
		
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
		final String content = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "queryAaiNetworkPolicy.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		Optional<NetworkPolicy> oNetPolicy = Optional.empty();
		AAIResourceUri netPolicyUri = AAIUriFactory.createResourceUri(AAIObjectType.NETWORK_POLICY, "ModelInvariantUUID", "serviceModelVersionId");
		
		doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(isA(AAIResourceUri.class));
		oNetPolicy = aaiNetworkResources.getNetworkPolicy(netPolicyUri);
		verify(MOCK_aaiResourcesClient, times(1)).get(any(AAIResourceUri.class));
		
		if (oNetPolicy.isPresent()) {
			NetworkPolicy networkPolicy = oNetPolicy.get();
			assertThat(aaiResultWrapper.asBean(NetworkPolicy.class).get(), sameBeanAs(networkPolicy));
		}
	}
	
	@Test
	public void getRouteTableTest() throws Exception {
		final String content = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "queryAaiNetworkTableRefs.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		Optional<RouteTableReference> oRtref = Optional.empty();
		AAIResourceUri rTRefUri = AAIUriFactory.createResourceUri(AAIObjectType.ROUTE_TABLE_REFERENCE, "ModelInvariantUUID", "serviceModelVersionId");
		
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
		final String content = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiQueryAAIResponse-Wrapper.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		L3Network network = new L3Network();
		network.setNetworkId("0384d743-f69b-4cc8-9aa8-c3ae66662c44");
		network.setNetworkName("Dev_Bindings_1802_020118");
		network.setOrchestrationStatus(OrchestrationStatus.CREATED);

		doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(isA(AAIResourceUri.class));
		Optional<org.onap.aai.domain.yang.L3Network> l3NetworkOpt =  aaiNetworkResources.queryNetworkById(network);
		org.onap.aai.domain.yang.L3Network l3Network = l3NetworkOpt.isPresent() ? l3NetworkOpt.get() : null; 
		
		verify(MOCK_aaiResourcesClient, times(1)).get(isA(AAIResourceUri.class));
		assertNotNull(l3Network);
		assertEquals("0384d743-f69b-4cc8-9aa8-c3ae66662c44", l3Network.getNetworkId());		
		assertEquals("Dev_Bindings_1802_020118", l3Network.getNetworkName());
	}	
	
	@Test
	public void queryNetworkWrapperByIdTest() throws Exception {
		final String content = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiQueryAAIResponse-Wrapper.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		L3Network network = new L3Network();
		network.setNetworkId("0384d743-f69b-4cc8-9aa8-c3ae66662c44");
		network.setNetworkName("Dev_Bindings_1802_020118");
		network.setOrchestrationStatus(OrchestrationStatus.CREATED);

		doReturn(aaiResultWrapper).when(MOCK_aaiResourcesClient).get(isA(AAIResourceUri.class));
		AAIResultWrapper result =  aaiNetworkResources.queryNetworkWrapperById(network);
		
		verify(MOCK_aaiResourcesClient, times(1)).get(isA(AAIResourceUri.class));
		assertEquals(aaiResultWrapper.getJson(), result.getJson());
		assertNotNull(result);
		Optional<Relationships> resultNetworkRelationships = result.getRelationships();
		assertTrue(resultNetworkRelationships.isPresent());
		Optional<org.onap.aai.domain.yang.L3Network> aaiL3Network = result.asBean(org.onap.aai.domain.yang.L3Network.class);
		assertEquals(network.getNetworkId(),aaiL3Network.get().getNetworkId());
		assertEquals(network.getNetworkName(),aaiL3Network.get().getNetworkName());
		
 	
	}

	public void createNetworkCollectionTest() throws Exception {
		doNothing().when(MOCK_aaiResourcesClient).create(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.Collection.class));
		collection.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		
		aaiNetworkResources.createNetworkCollection(collection);
		assertEquals(OrchestrationStatus.INVENTORIED, collection.getOrchestrationStatus());
		verify(MOCK_aaiResourcesClient, times(1)).create(any(AAIResourceUri.class), isA(org.onap.aai.domain.yang.Collection.class));
	}
	
	@Test
	public void createNetworkInstanceGroupTest() throws Exception {
		doReturn(new org.onap.aai.domain.yang.InstanceGroup()).when(MOCK_aaiObjectMapper).mapInstanceGroup(instanceGroup);
		doNothing().when(MOCK_aaiResourcesClient).create(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.InstanceGroup.class));
		aaiNetworkResources.createNetworkInstanceGroup(instanceGroup);
		verify(MOCK_aaiResourcesClient, times(1)).create(any(AAIResourceUri.class), isA(org.onap.aai.domain.yang.InstanceGroup.class));
	}
	
	@Test
	public void connectNetworkToNetworkCollectionInstanceGroupTest() throws Exception {
		aaiNetworkResources.connectNetworkToNetworkCollectionInstanceGroup(network, instanceGroup);
		verify(MOCK_aaiResourcesClient, times(1)).connect(eq(AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId())), eq(AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, network.getNetworkId())));
	}
	
	@Test
	public void connectNetworkToNetworkCollectionServiceInstanceTest() throws Exception {
		aaiNetworkResources.connectNetworkToNetworkCollectionServiceInstance(network, serviceInstance);
		verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), eq(AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, network.getNetworkId())));
	}
	
	@Test
	public void connectNetworkToCloudRegionTest() throws Exception {
		aaiNetworkResources.connectNetworkToCloudRegion(network, "testCloudRegionId");
		verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), eq(AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, network.getNetworkId())));
	}
	
	@Test
	public void connectNetworkToTenantTest() throws Exception {
		aaiNetworkResources.connectNetworkToTenant(network, "testTenantId");
		verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), eq(AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, network.getNetworkId())));
	}
	
	@Test
	public void connectNetworkCollectionInstanceGroupToNetworkCollectionTest() throws Exception {
		aaiNetworkResources.connectNetworkCollectionInstanceGroupToNetworkCollection(instanceGroup, collection);
		verify(MOCK_aaiResourcesClient, times(1)).connect(eq(AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, collection.getId())), eq(AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId())));
	}
	
	@Test
	public void connectNetworkCollectionToServiceInstanceTest() throws Exception {
		aaiNetworkResources.connectNetworkCollectionToServiceInstance(collection, serviceInstance);
		verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
	}
	
	@Test
	public void deleteCollectionTest() throws Exception {
		doNothing().when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));
		aaiNetworkResources.deleteCollection(collection);
		verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
	}
	
	@Test
	public void deleteInstanceGroupTest() throws Exception {
		doNothing().when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));
		aaiNetworkResources.deleteNetworkInstanceGroup(instanceGroup);
		verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
	}
}