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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.onap.so.client.sdnc.mapper.NetworkTopologyOperationRequestMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

import org.onap.sdnc.northbound.client.model.GenericResourceApiNetworkOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SDNCNetworkResourcesTest extends TestDataSetup{
	
	@InjectMocks
	private SDNCNetworkResources sdncNetworkResources;
	
	@Mock
	protected SDNCClient MOCK_sdncClient;
	
	@Mock
	protected NetworkTopologyOperationRequestMapper MOCK_networkTopologyOperationRequestMapper;	
	
	private L3Network network;
	private ServiceInstance serviceInstance;
	private Customer customer;
	private RequestContext requestContext;
	private CloudRegion cloudRegion;
	
	@Before
	public void before() {
		network = buildL3Network();

		customer = buildCustomer();

		serviceInstance = buildServiceInstance();

		requestContext = buildRequestContext();
		
		cloudRegion = new CloudRegion();
	}

	@Test
	public void assignNetworkTest() throws Exception {
		network.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
		
		doReturn("test").when(MOCK_sdncClient).post(isA(GenericResourceApiNetworkOperationInformation.class), eq(SDNCTopology.NETWORK));
		
		sdncNetworkResources.assignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);

		verify(MOCK_sdncClient, times(1)).post(ArgumentMatchers.isNull(), eq(SDNCTopology.NETWORK));
		
		assertEquals(OrchestrationStatus.ASSIGNED, network.getOrchestrationStatus());
	}
	
	@Test
	public void rollbackAssignNetworkTest() throws Exception {
		network.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

		doReturn("test").when(MOCK_sdncClient).post(isA(GenericResourceApiNetworkOperationInformation.class), eq(SDNCTopology.NETWORK));
		
		sdncNetworkResources.rollbackAssignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);

		verify(MOCK_sdncClient, times(1)).post(ArgumentMatchers.isNull(), eq(SDNCTopology.NETWORK));
		
		assertEquals(OrchestrationStatus.ASSIGNED, network.getOrchestrationStatus());
	}
	
	@Test
	public void activateNetworkTest() throws Exception {
		network.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

		doReturn("test").when(MOCK_sdncClient).post(isA(GenericResourceApiNetworkOperationInformation.class), eq(SDNCTopology.NETWORK));
		
		sdncNetworkResources.activateNetwork(network, serviceInstance, customer, requestContext, cloudRegion);

		verify(MOCK_sdncClient, times(1)).post(ArgumentMatchers.isNull(), eq(SDNCTopology.NETWORK));
		
		assertEquals(OrchestrationStatus.ASSIGNED, network.getOrchestrationStatus());
	}
	
	@Test
	public void deleteNetworkTest() throws Exception {
		network.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

		doReturn("test").when(MOCK_sdncClient).post(isA(GenericResourceApiNetworkOperationInformation.class), eq(SDNCTopology.NETWORK));
		
		sdncNetworkResources.deleteNetwork(network, serviceInstance, customer, requestContext, cloudRegion);

		verify(MOCK_sdncClient, times(1)).post(ArgumentMatchers.isNull(), eq(SDNCTopology.NETWORK));
		
		assertEquals(OrchestrationStatus.ASSIGNED, network.getOrchestrationStatus());
	}
	
	@Test
	public void test_deactivateNetwork() throws MapperException, BadResponseException {
		serviceInstance.getNetworks().add(network);

		Customer customer = new Customer();
		customer.setGlobalCustomerId("gcustId");
		customer.setServiceSubscription(new ServiceSubscription());
		// set Customer on service instance
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);

		GenericResourceApiNetworkOperationInformation expectedGenericResourceApiNetworkOperationInformation = new GenericResourceApiNetworkOperationInformation();
		
		String expectedResponse = "response";
		
		doReturn(expectedResponse).when(MOCK_sdncClient).post(expectedGenericResourceApiNetworkOperationInformation, SDNCTopology.NETWORK);
		
		doReturn(expectedGenericResourceApiNetworkOperationInformation).when(MOCK_networkTopologyOperationRequestMapper).reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.DEACTIVATE, GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance, customer, requestContext, cloudRegion);
		
		String actualResponse = sdncNetworkResources.deactivateNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
		
		verify(MOCK_networkTopologyOperationRequestMapper, times(1)).reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.DEACTIVATE, GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance, customer, requestContext, cloudRegion);
		
		verify(MOCK_sdncClient).post(expectedGenericResourceApiNetworkOperationInformation, SDNCTopology.NETWORK);
		
		assertEquals(expectedResponse, actualResponse);
	}
	
	@Test
	public void changeAssignNetworkTest() throws MapperException, BadResponseException {
		String expectedSdncResponse = "SDNCChangeAssignNetworkResponse";

		serviceInstance.getNetworks().add(network);

		Customer customer = new Customer();
		customer.setGlobalCustomerId("globalCustomerId");
		customer.setServiceSubscription(new ServiceSubscription());
		// set Customer on service instance
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		
		GenericResourceApiNetworkOperationInformation sdncReq = new GenericResourceApiNetworkOperationInformation();
		
		doReturn(sdncReq).when(MOCK_networkTopologyOperationRequestMapper).reqMapper(isA(SDNCSvcOperation.class), isA(SDNCSvcAction.class), isA(GenericResourceApiRequestActionEnumeration.class), isA(L3Network.class), isA(ServiceInstance.class), isA(Customer.class), isA(RequestContext.class), isA(CloudRegion.class));
		
		doReturn(expectedSdncResponse).when(MOCK_sdncClient).post(isA(GenericResourceApiNetworkOperationInformation.class), isA(SDNCTopology.class));
		
		String actualSdncResponse = sdncNetworkResources.changeAssignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
		
		verify(MOCK_networkTopologyOperationRequestMapper, times(1)).reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.CHANGE_ASSIGN, GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance, customer, requestContext, cloudRegion);
		verify(MOCK_sdncClient, times(1)).post(sdncReq, SDNCTopology.NETWORK);
		assertEquals(actualSdncResponse, expectedSdncResponse);
	}
	
	@Test
	public void unassignNetwork_Test() throws Exception {
 		network.setOrchestrationStatus(OrchestrationStatus.CREATED);

 		doReturn("test").when(MOCK_sdncClient).post(isA(GenericResourceApiNetworkOperationInformation.class), eq(SDNCTopology.NETWORK));
 		
 		sdncNetworkResources.unassignNetwork(network, serviceInstance, customer,
 				requestContext, cloudRegion);
 
 		verify(MOCK_sdncClient, times(1)).post(ArgumentMatchers.isNull(), eq(SDNCTopology.NETWORK));
 		
 		assertEquals(OrchestrationStatus.CREATED, network.getOrchestrationStatus());		
	}		
}