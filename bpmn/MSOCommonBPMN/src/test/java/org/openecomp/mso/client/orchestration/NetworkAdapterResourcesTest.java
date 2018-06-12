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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.BuildingBlockTestDataSetup;
import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkRequest;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkResponse;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkResponse;

import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.HostRoute;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.OrchestrationContext;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.client.adapter.network.NetworkAdapterClientException;
import org.openecomp.mso.client.adapter.network.NetworkAdapterClientImpl;
import org.openecomp.mso.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Subnet;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;

import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.openecomp.mso.entity.MsoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.shazam.shazamcrest.matcher.Matchers;


@RunWith(MockitoJUnitRunner.class)
public class NetworkAdapterResourcesTest  extends BuildingBlockTestDataSetup{
	
	@InjectMocks
	private NetworkAdapterResources networkAdapterResources = new NetworkAdapterResources();
	
	@Mock
	protected NetworkAdapterClientImpl MOCK_networkAdapterClient;
	
	@Mock
	protected NetworkAdapterObjectMapper MOCK_networkAdapterObjectMapper;
	
	private L3Network l3Network;
	private RequestContext requestContext;
	private ServiceInstance serviceInstance;
	private CloudRegion cloudRegion;
	private OrchestrationContext orchestrationContext;
	private Customer customer;
	Map<String, String> userInput;
	
	@Before
	public void before() {
		requestContext = buildRequestContext();

		customer = buildCustomer();

		serviceInstance = buildServiceInstance();
		
		cloudRegion = buildCloudRegion();
		
		orchestrationContext = buildOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);
		
		userInput = buildUserInput();

		l3Network = buildL3Network();
	}
	
	@Test
	public void createNetworTest() throws Exception {
		String cloudRegionPo = "cloudRegionPo";
		CreateNetworkRequest expectedCreateNetworkRequest = new CreateNetworkRequest();
		
		expectedCreateNetworkRequest.setCloudSiteId(cloudRegionPo);
		expectedCreateNetworkRequest.setTenantId(cloudRegion.getTenantId());
		expectedCreateNetworkRequest.setNetworkId(l3Network.getNetworkId());
		expectedCreateNetworkRequest.setNetworkName(l3Network.getNetworkName());
		expectedCreateNetworkRequest.setBackout(false);
		expectedCreateNetworkRequest.setFailIfExists(true);
		
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId(requestContext.getMsoRequestId());
		msoRequest.setServiceInstanceId(serviceInstance.getServiceInstanceId());
		expectedCreateNetworkRequest.setMsoRequest(msoRequest);
		expectedCreateNetworkRequest.setSkipAAI(true);
		
		Subnet openstackSubnet = new Subnet();
		HostRoute hostRoute = new HostRoute();
		hostRoute.setHostRouteId("hostRouteId");
		hostRoute.setNextHop("nextHop");
		hostRoute.setRoutePrefix("routePrefix");
		openstackSubnet.getHostRoutes().add(hostRoute);
		List<Subnet> subnetList = new ArrayList<Subnet>();
		subnetList.add(openstackSubnet);
		l3Network.getSubnets().add(openstackSubnet);
		
		l3Network.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

		CreateNetworkRequest createNetworkRequest = new CreateNetworkRequest();
		createNetworkRequest.setCloudSiteId("cloudSiteId");
		
		CreateNetworkResponse expectedCreateNetworkResponse = new CreateNetworkResponse();
		expectedCreateNetworkResponse.setNetworkStackId("networkStackId");
		expectedCreateNetworkResponse.setNetworkCreated(true);

		
		doReturn(expectedCreateNetworkResponse).when(MOCK_networkAdapterClient).createNetwork(isA(CreateNetworkRequest.class));
		
		doReturn(createNetworkRequest).when(MOCK_networkAdapterObjectMapper).createNetworkRequestMapper(isA(RequestContext.class), isA(CloudRegion.class), isA(OrchestrationContext.class), isA(ServiceInstance.class), isA(L3Network.class), isA(Map.class), isA(String.class), isA(Customer.class));

		CreateNetworkResponse actualCreateNetwrokResponse = (networkAdapterResources.createNetwork(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, customer)).get();

		verify(MOCK_networkAdapterClient, times(1)).createNetwork(createNetworkRequest);
		
		verify(MOCK_networkAdapterObjectMapper, times(1)).createNetworkRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, customer);

		assertThat(expectedCreateNetworkResponse, Matchers.sameBeanAs(actualCreateNetwrokResponse));
	}
	
	@Test
	public void rollbackCreateNetworkTest() throws Exception {
		String cloudRegionPo = "cloudRegionPo";
		RollbackNetworkResponse expectedRollbackNetworkResponse = new RollbackNetworkResponse();
		expectedRollbackNetworkResponse.setMessageId("messageId");
		expectedRollbackNetworkResponse.setNetworkRolledBack(true);

		RollbackNetworkRequest rollbackNetworkRequest = new RollbackNetworkRequest();
		rollbackNetworkRequest.setMessageId("messageId");
		
		RollbackNetworkResponse rollbackNetworkResponse = new RollbackNetworkResponse();
		rollbackNetworkResponse.setMessageId("messageId");
		rollbackNetworkResponse.setNetworkRolledBack(true);
		
		CreateNetworkResponse createNetworkResponse = new CreateNetworkResponse();
		createNetworkResponse.setMessageId("messageId");
		
		doReturn(rollbackNetworkResponse).when(MOCK_networkAdapterClient).rollbackNetwork(isA(String.class), isA(RollbackNetworkRequest.class));
		
		doReturn(rollbackNetworkRequest).when(MOCK_networkAdapterObjectMapper).createNetworkRollbackRequestMapper(isA(RequestContext.class), isA(CloudRegion.class), isA(OrchestrationContext.class), isA(ServiceInstance.class), isA(L3Network.class), isA(Map.class), isA(String.class), isA(CreateNetworkResponse.class));
		
		RollbackNetworkResponse actualRollbackCreateNetwrokResponse = (networkAdapterResources.rollbackCreateNetwork(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, createNetworkResponse)).get();

		verify(MOCK_networkAdapterClient, times(1)).rollbackNetwork(l3Network.getNetworkId(), rollbackNetworkRequest);
		
		verify(MOCK_networkAdapterObjectMapper, times(1)).createNetworkRollbackRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, createNetworkResponse);
		
		assertThat(expectedRollbackNetworkResponse, Matchers.sameBeanAs(actualRollbackCreateNetwrokResponse));
	}

	@Test
	public void updateNetworkTest() throws UnsupportedEncodingException, NetworkAdapterClientException {

		doReturn(new UpdateNetworkRequest()).when(MOCK_networkAdapterObjectMapper).createNetworkUpdateRequestMapper(isA(RequestContext.class), isA(CloudRegion.class), isA(OrchestrationContext.class), isA(ServiceInstance.class), isA(L3Network.class), isA(Map.class), isA(Customer.class));

		doReturn(new UpdateNetworkResponse()).when(MOCK_networkAdapterClient).updateNetwork(isA(String.class), isA(UpdateNetworkRequest.class));
		
		Optional<UpdateNetworkResponse> actualUpdateNetworkResponse = networkAdapterResources.updateNetwork(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, customer);
		

		verify(MOCK_networkAdapterObjectMapper, times(1)).createNetworkUpdateRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, customer);
		verify(MOCK_networkAdapterClient, times(1)).updateNetwork(isA(String.class), isA(UpdateNetworkRequest.class));
		assertNotNull(actualUpdateNetworkResponse);
	}
	
	@Test
	public void deleteNetwork_DeleteAction_Test() throws UnsupportedEncodingException, NetworkAdapterClientException {
		String cloudRegionPo = "cloudRegionPo";
		
		DeleteNetworkRequest deleteNetworkRequest = new DeleteNetworkRequest();
		doReturn(deleteNetworkRequest).when(MOCK_networkAdapterObjectMapper).deleteNetworkRequestMapper(requestContext, cloudRegion, serviceInstance, l3Network, cloudRegionPo);
		
		DeleteNetworkResponse expectedDeleteNetworkResponse = new DeleteNetworkResponse();
		
		doReturn(expectedDeleteNetworkResponse).when(MOCK_networkAdapterClient).deleteNetwork(l3Network.getNetworkId(), deleteNetworkRequest);
		
		Optional<DeleteNetworkResponse> actualODeleteNetworkResponse = networkAdapterResources.deleteNetwork(requestContext, cloudRegion, serviceInstance, l3Network, cloudRegionPo);
		DeleteNetworkResponse actualDeleteNetworkResponse = actualODeleteNetworkResponse.get();
		
		verify(MOCK_networkAdapterObjectMapper, times(1)).deleteNetworkRequestMapper(requestContext, cloudRegion, serviceInstance, l3Network, cloudRegionPo);
		verify(MOCK_networkAdapterClient, times(1)).deleteNetwork(l3Network.getNetworkId(), deleteNetworkRequest);
		assertThat(expectedDeleteNetworkResponse, Matchers.sameBeanAs(actualDeleteNetworkResponse));
	}
}
