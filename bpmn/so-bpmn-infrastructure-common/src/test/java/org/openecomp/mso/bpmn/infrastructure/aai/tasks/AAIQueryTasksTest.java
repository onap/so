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
package org.openecomp.mso.bpmn.infrastructure.aai.tasks;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.NetworkPolicy;
import org.onap.aai.domain.yang.RouteTableReference;
import org.onap.aai.domain.yang.RouteTarget;
import org.onap.aai.domain.yang.RouteTargets;
import org.onap.aai.domain.yang.VpnBinding;
import org.openecomp.mso.adapters.nwrest.ContrailNetwork;
import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test class for JUnit coverage of AAI query bean supporting BPMN flows
 *
 */
public class AAIQueryTasksTest extends BaseTaskTest{
	@Autowired
	private AAIQueryTasks aaiQueryTasks;
	
	private L3Network network;
	private ServiceInstance serviceInstance;
	private Customer customer;
	
	private final static String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/Network/";
	
	@Before
	public void before() {
		serviceInstance = setServiceInstance();
		customer = setCustomer();
		network = setL3Network();
		
		CreateNetworkRequest createNetworkRequest = new CreateNetworkRequest();
		ContrailNetwork contrailNetwork = new ContrailNetwork();
		createNetworkRequest.setContrailNetwork(contrailNetwork);
		execution.setVariable("createNetworkRequest", createNetworkRequest);

		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
	}
	
	/**
	 * Test method to get network by it's id results in success
	 */
	@Test
	public void getNetworkWrapperByIdTest() throws Exception {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIResponse.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content); 
		doReturn(aaiResultWrapper).when(aaiNetworkResources).queryNetworkWrapperById(network);
		aaiQueryTasks.getNetworkWrapperById(execution);
		verify(aaiNetworkResources, times(1)).queryNetworkWrapperById(network);
	}
	
	@Test
	public void queryNetworkVpnBindingTest() throws Exception {
		org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTarget expectedRouteTarget = 
				new org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTarget();
		expectedRouteTarget.setGlobalRouteTarget("globalRouteTarget");
		expectedRouteTarget.setResourceVersion("resourceVersion");
		expectedRouteTarget.setRouteTargetRole("routeTargetRole");
		
		org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding expectedVpnBinding = 
				new org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding();
		expectedVpnBinding.getRouteTargets().add(expectedRouteTarget);
		
		List<org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding> expectedVpnBindings = new ArrayList<>();
		expectedVpnBindings.add(expectedVpnBinding);
		
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIResponse.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		RouteTarget routeTarget = new RouteTarget();
		routeTarget.setGlobalRouteTarget("globalRouteTarget");
		routeTarget.setResourceVersion("resourceVersion");
		routeTarget.setRouteTargetRole("routeTargetRole");
		
		RouteTargets routeTargets = new RouteTargets();
		routeTargets.getRouteTarget().add(routeTarget);
		
		VpnBinding vpnBinding = new VpnBinding();
		vpnBinding.setRouteTargets(routeTargets);
		
		doReturn(Optional.of(vpnBinding)).when(aaiNetworkResources).getVpnBinding(isA(AAIResourceUri.class));
		
		aaiQueryTasks.queryNetworkVpnBinding(execution);
		
		verify(aaiNetworkResources, times(aaiResultWrapper.getRelationships().get().getRelatedAAIUris(AAIObjectType.VPN_BINDING).size())).getVpnBinding(isA(AAIResourceUri.class));
		assertThat(customer.getVpnBindings(), sameBeanAs(expectedVpnBindings));
	}
	
	@Test
	public void queryNetworkVpnBindingNullTest() throws Exception {
		
		List<org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding> expectedVpnBindings = new ArrayList<>();
		
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAINetworkTestResponse.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		final String contentVpnBinding = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIVpnBindingTestResponse.json")));
		AAIResultWrapper aaiResultWrapperVpnBinding = new AAIResultWrapper(contentVpnBinding);
		doReturn(aaiResultWrapperVpnBinding.asBean(VpnBinding.class)).when(aaiNetworkResources).getVpnBinding(isA(AAIResourceUri.class));
		
		aaiQueryTasks.queryNetworkVpnBinding(execution);
		
		verify(aaiNetworkResources, times(aaiResultWrapper.getRelationships().get().getRelatedAAIUris(AAIObjectType.VPN_BINDING).size())).getVpnBinding(isA(AAIResourceUri.class));
		assertThat(customer.getVpnBindings(), sameBeanAs(expectedVpnBindings));
	}
	
	@Test
	public void queryNetworkVpnBindingEmptyUriTest() throws Exception {
		org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTarget expectedRouteTarget = 
				new org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTarget();
		expectedRouteTarget.setGlobalRouteTarget("globalRouteTarget");
		expectedRouteTarget.setResourceVersion("resourceVersion");
		expectedRouteTarget.setRouteTargetRole("routeTargetRole");
		
		org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding expectedVpnBinding = 
				new org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding();
		expectedVpnBinding.getRouteTargets().add(expectedRouteTarget);
		
		List<org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding> expectedVpnBindings = new ArrayList<>();
		expectedVpnBindings.add(expectedVpnBinding);
		
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIResponseEmptyUri.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		RouteTarget routeTarget = new RouteTarget();
		routeTarget.setGlobalRouteTarget("globalRouteTarget");
		routeTarget.setResourceVersion("resourceVersion");
		routeTarget.setRouteTargetRole("routeTargetRole");
		
		RouteTargets routeTargets = new RouteTargets();
		routeTargets.getRouteTarget().add(routeTarget);
		
		VpnBinding vpnBinding = new VpnBinding();
		vpnBinding.setRouteTargets(routeTargets);
		
		aaiQueryTasks.queryNetworkVpnBinding(execution);
		
		verify(aaiNetworkResources, times(0)).getVpnBinding(isA(AAIResourceUri.class));
		assertThat(customer.getVpnBindings(), sameBeanAs(new ArrayList<org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding>()));
	}
	
	@Test
	public void queryNetworkVpnBindingEmptyVpnBindingTest() throws Exception {
		org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTarget expectedRouteTarget = 
				new org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTarget();
		expectedRouteTarget.setGlobalRouteTarget("globalRouteTarget");
		expectedRouteTarget.setResourceVersion("resourceVersion");
		expectedRouteTarget.setRouteTargetRole("routeTargetRole");
		
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIResponse.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		RouteTarget routeTarget = new RouteTarget();
		routeTarget.setGlobalRouteTarget("globalRouteTarget");
		routeTarget.setResourceVersion("resourceVersion");
		routeTarget.setRouteTargetRole("routeTargetRole");
		
		RouteTargets routeTargets = new RouteTargets();
		routeTargets.getRouteTarget().add(routeTarget);
		
		doReturn(Optional.empty()).when(aaiNetworkResources).getVpnBinding(isA(AAIResourceUri.class));
		
		aaiQueryTasks.queryNetworkVpnBinding(execution);
		
		verify(aaiNetworkResources, times(aaiResultWrapper.getRelationships().get().getRelatedAAIUris(AAIObjectType.VPN_BINDING).size())).getVpnBinding(isA(AAIResourceUri.class));
		assertThat(customer.getVpnBindings(), sameBeanAs(new ArrayList<org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding>()));
	}
	
	@Test
	public void queryNetworkVpnBindingExceptionTest() {
		expectedException.expect(BpmnError.class);
		
		aaiQueryTasks.queryNetworkVpnBinding(execution);
	}
	
	@Test
	public void queryNetworkPolicyTest() throws Exception {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIResponse.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		NetworkPolicy networkPolicy = new NetworkPolicy();
		networkPolicy.setNetworkPolicyId("networkPolicyId");
		networkPolicy.setNetworkPolicyFqdn("networkPolicyFqdn");
		networkPolicy.setHeatStackId("heatStackId");
		networkPolicy.setResourceVersion("resourceVersion");
		
		org.openecomp.mso.bpmn.servicedecomposition.bbobjects.NetworkPolicy expectedNetworkPolicy = new org.openecomp.mso.bpmn.servicedecomposition.bbobjects.NetworkPolicy();
		expectedNetworkPolicy.setNetworkPolicyId("networkPolicyId");
		expectedNetworkPolicy.setNetworkPolicyFqdn("networkPolicyFqdn");
		expectedNetworkPolicy.setHeatStackId("heatStackId");
		expectedNetworkPolicy.setResourceVersion("resourceVersion");
				
		doReturn(Optional.of(networkPolicy)).when(aaiNetworkResources).getNetworkPolicy(isA(AAIResourceUri.class));
		
		aaiQueryTasks.queryNetworkPolicy(execution);
		
		verify(aaiNetworkResources, times(aaiResultWrapper.getRelationships().get().getRelatedAAIUris(AAIObjectType.NETWORK_POLICY).size())).getNetworkPolicy(isA(AAIResourceUri.class));
		assertThat(network.getNetworkPolicies(), sameBeanAs(Arrays.asList(expectedNetworkPolicy, expectedNetworkPolicy)));
	}
	
	@Test
	public void queryNetworkPolicyEmptyUriTest() throws Exception {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIResponseEmptyUri.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		NetworkPolicy networkPolicy = new NetworkPolicy();
		networkPolicy.setNetworkPolicyId("networkPolicyId");
		networkPolicy.setNetworkPolicyFqdn("networkPolicyFqdn");
				
		aaiQueryTasks.queryNetworkPolicy(execution);
		
		verify(aaiNetworkResources, times(0)).getNetworkPolicy(isA(AAIResourceUri.class));
		assertThat(network.getNetworkPolicies(), sameBeanAs(new ArrayList<String>()));
	}
	
	@Test
	public void queryNetworkPolicyEmptyNetworkPolicyTest() throws Exception {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIResponse.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		NetworkPolicy networkPolicy = new NetworkPolicy();
		networkPolicy.setNetworkPolicyId("networkPolicyId");
		networkPolicy.setNetworkPolicyFqdn("networkPolicyFqdn");
				
		doReturn(Optional.empty()).when(aaiNetworkResources).getNetworkPolicy(isA(AAIResourceUri.class));
		
		aaiQueryTasks.queryNetworkPolicy(execution);
		
		verify(aaiNetworkResources, times(aaiResultWrapper.getRelationships().get().getRelatedAAIUris(AAIObjectType.NETWORK_POLICY).size())).getNetworkPolicy(isA(AAIResourceUri.class));
		assertThat(network.getNetworkPolicies(), sameBeanAs(new ArrayList<String>()));
	}
	
	@Test
	public void queryNetworkPolicyExceptionTest() {
		expectedException.expect(BpmnError.class);
		
		aaiQueryTasks.queryNetworkPolicy(execution);
	}
	
	@Test
	public void queryNetworkTableRefTest() throws Exception {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIResponse.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		RouteTableReference routeTableReference= new RouteTableReference();
		routeTableReference.setRouteTableReferenceId("routeTableReferenceId");
		routeTableReference.setResourceVersion("resourceVersion");
		routeTableReference.setRouteTableReferenceFqdn("routeTableRefernceFqdn");
		
		org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTableReference expectedRouteTableReference = new org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTableReference();
		expectedRouteTableReference.setResourceVersion("resourceVersion");
		expectedRouteTableReference.setRouteTableReferenceFqdn("routeTableRefernceFqdn");
		expectedRouteTableReference.setRouteTableReferenceId("routeTableReferenceId");
		
		doReturn(Optional.of(routeTableReference)).when(aaiNetworkResources).getRouteTable(isA(AAIResourceUri.class));
		
		aaiQueryTasks.queryNetworkTableRef(execution);
		
		verify(aaiNetworkResources, times(aaiResultWrapper.getRelationships().get().getRelatedAAIUris(AAIObjectType.ROUTE_TABLE_REFERENCE).size())).getRouteTable(isA(AAIResourceUri.class));
		assertThat(network.getContrailNetworkRouteTableReferences(), sameBeanAs(Arrays.asList(expectedRouteTableReference)));
	}
	
	@Test
	public void queryNetworkTableRefEmptyUriTest() throws Exception {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIResponseEmptyUri.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		aaiQueryTasks.queryNetworkTableRef(execution);
		
		verify(aaiNetworkResources, times(0)).getRouteTable(isA(AAIResourceUri.class));
		assertThat(network.getContrailNetworkRouteTableReferences(), sameBeanAs(new ArrayList<org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTableReference>()));
	}
	
	@Test
	public void queryNetworkTableRefEmptyRouteTableReferenceTest() throws Exception {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIResponseEmptyUri.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		doReturn(Optional.empty()).when(aaiNetworkResources).getRouteTable(isA(AAIResourceUri.class));
		
		aaiQueryTasks.queryNetworkTableRef(execution);
		
		verify(aaiNetworkResources, times(aaiResultWrapper.getRelationships().get().getRelatedAAIUris(AAIObjectType.ROUTE_TABLE_REFERENCE).size())).getRouteTable(isA(AAIResourceUri.class));
		assertThat(network.getContrailNetworkRouteTableReferences(), sameBeanAs(new ArrayList<org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTableReference>()));
	}
	
	@Test
	public void queryNetworkTableRefExceptionTest() {
		expectedException.expect(BpmnError.class);
		
		aaiQueryTasks.queryNetworkTableRef(execution);
	}
	
	@Test
	public void modelMapperTest() throws Exception {
		org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTarget expectedRouteTarget = 
				new org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTarget();
		expectedRouteTarget.setGlobalRouteTarget("globalRouteTarget");
		//do not set value for ResourceVersion, expect NULL
		expectedRouteTarget.setRouteTargetRole("");// expect empty string value
		
		org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding expectedVpnBinding = 
				new org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding();
		expectedVpnBinding.getRouteTargets().add(expectedRouteTarget);
		
		List<org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding> expectedVpnBindings = new ArrayList<>();
		expectedVpnBindings.add(expectedVpnBinding);
		
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "queryAAIResponse.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(content);
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		RouteTarget routeTarget = new RouteTarget();
		routeTarget.setGlobalRouteTarget("globalRouteTarget");
		//do not set value for ResourceVersion
		routeTarget.setRouteTargetRole("");//set empty string
		
		RouteTargets routeTargets = new RouteTargets();
		routeTargets.getRouteTarget().add(routeTarget);
		
		VpnBinding vpnBinding = new VpnBinding();
		vpnBinding.setRouteTargets(routeTargets);
		
		doReturn(Optional.of(vpnBinding)).when(aaiNetworkResources).getVpnBinding(isA(AAIResourceUri.class));
		
		aaiQueryTasks.queryNetworkVpnBinding(execution);
		
		verify(aaiNetworkResources, times(aaiResultWrapper.getRelationships().get().getRelatedAAIUris(AAIObjectType.VPN_BINDING).size())).getVpnBinding(isA(AAIResourceUri.class));
		assertThat(customer.getVpnBindings(), sameBeanAs(expectedVpnBindings));
		//verify value was set correctly
		assertEquals(customer.getVpnBindings().get(0).getRouteTargets().get(0).getGlobalRouteTarget(), "globalRouteTarget");
		//verify NULL for value not set
		assertEquals(customer.getVpnBindings().get(0).getRouteTargets().get(0).getResourceVersion(), null);
		//verify empty string when String with length of 0 was present
		assertEquals(customer.getVpnBindings().get(0).getRouteTargets().get(0).getRouteTargetRole(), "");
	}
}
