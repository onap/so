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
package org.openecomp.mso.client.adapter.network.mapper;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.mockito.Mockito.doReturn;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.BuildingBlockTestDataSetup;
import org.openecomp.mso.adapters.nwrest.ContrailNetwork;
import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.ProviderVlanNetwork;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.HostRoute;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.OrchestrationContext;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTableReference;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTarget;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Subnet;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.NetworkRollback;

public class NetworkAdapterObjectMapperTest extends BuildingBlockTestDataSetup{
	
	private NetworkAdapterObjectMapper SPY_networkAdapterObjectMapper = Mockito.spy(NetworkAdapterObjectMapper.class);

	private L3Network l3Network;
	private RequestContext requestContext;
	private ServiceInstance serviceInstance;
	private CloudRegion cloudRegion;
	private OrchestrationContext orchestrationContext;
	private Customer customer;
	Map<String, String> userInput;
	
	
	@Before
	public void before() {
		requestContext = setRequestContext();
		
		customer = buildCustomer();
		
		serviceInstance = setServiceInstance();
		
		cloudRegion = setCloudRegion();
		
		orchestrationContext = setOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(false);
		
		userInput = setUserInput();
		
		l3Network = setL3Network();

		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
	}
	
	@Test
	public void buildCreateNetworkRequestFromBbobjectTest() throws Exception {

		String cloudRegionPo = "cloudRegionPo";
		CreateNetworkRequest expectedCreateNetworkRequest = new CreateNetworkRequest();
		
		expectedCreateNetworkRequest.setCloudSiteId(cloudRegionPo);
		expectedCreateNetworkRequest.setTenantId(cloudRegion.getTenantId());
		expectedCreateNetworkRequest.setNetworkId(l3Network.getNetworkId());
		expectedCreateNetworkRequest.setNetworkName(l3Network.getNetworkName());
		expectedCreateNetworkRequest.setNetworkType(l3Network.getNetworkType());
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

		CreateNetworkRequest createNetworkRequest  = SPY_networkAdapterObjectMapper.createNetworkRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, customer);
		
		assertThat(createNetworkRequest, sameBeanAs(expectedCreateNetworkRequest).ignoring("contrailNetwork").ignoring("providerVlanNetwork").ignoring("subnets").ignoring("networkParams").ignoring("messageId"));
	}
	
	@Test
	public void createNetworkRollbackRequestMapperTest() throws Exception {

		String cloudRegionPo = "cloudRegionPo";
		RollbackNetworkRequest expectedRollbackNetworkRequest = new RollbackNetworkRequest();
		
		expectedRollbackNetworkRequest.setMessageId(requestContext.getMsoRequestId());
		NetworkRollback networkRollback = new NetworkRollback();
		networkRollback.setCloudId(cloudRegionPo);
		networkRollback.setNetworkCreated(true);
		networkRollback.setNetworkId(l3Network.getNetworkId());
		networkRollback.setNetworkType(l3Network.getNetworkType());
		networkRollback.setTenantId(cloudRegion.getTenantId());
		expectedRollbackNetworkRequest.setNetworkRollback(networkRollback);
		expectedRollbackNetworkRequest.setSkipAAI(true);
		
		CreateNetworkResponse createNetworkResponse = new CreateNetworkResponse();
		createNetworkResponse.setNetworkCreated(true);

		RollbackNetworkRequest rollbackNetworkRequest  = SPY_networkAdapterObjectMapper.createNetworkRollbackRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, createNetworkResponse);
		
		assertThat(rollbackNetworkRequest, sameBeanAs(expectedRollbackNetworkRequest).ignoring("contrailNetwork").ignoring("providerVlanNetwork").ignoring("subnets").ignoring("networkParams").ignoring("messageId"));
	}
	
	@Test
	public void updateNetworkRequestMapperTest() throws UnsupportedEncodingException {
		org.openecomp.mso.openstack.beans.Subnet subnet = new org.openecomp.mso.openstack.beans.Subnet();
		subnet.setSubnetId("subnetId");
		subnet.setHostRoutes(new ArrayList<org.openecomp.mso.openstack.beans.HostRoute>());
		
		List<org.openecomp.mso.openstack.beans.Subnet> subnets = new ArrayList<>();
		subnets.add(subnet);
		
		ProviderVlanNetwork providerVlanNetwork = new ProviderVlanNetwork("physicalNetworkName", new ArrayList<Integer>());
		
		List<String> policyFqdns = Arrays.asList("networkPolicyFqdn");
		
		org.openecomp.mso.openstack.beans.RouteTarget expectedRouteTarget = new org.openecomp.mso.openstack.beans.RouteTarget();
		expectedRouteTarget.setRouteTarget("globalRouteTarget");
		
		ContrailNetwork contrailNetwork = new ContrailNetwork();
		contrailNetwork.setPolicyFqdns(policyFqdns);
		contrailNetwork.setRouteTableFqdns(new ArrayList<String>());
		contrailNetwork.setRouteTargets(new ArrayList<org.openecomp.mso.openstack.beans.RouteTarget>());
		contrailNetwork.getRouteTargets().add(expectedRouteTarget);
		contrailNetwork.getRouteTableFqdns().add("routeTableReferenceFqdn");
		
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setServiceInstanceId("testServiceInstanceId1");
		
		ModelInfoNetwork modelInfoNetwork = new ModelInfoNetwork();
		modelInfoNetwork.setNetworkType("networkType");
		modelInfoNetwork.setModelCustomizationUUID("modelCustomizationUuid");
		modelInfoNetwork.setModelVersion("modelVersion");
		
		Subnet actualSubnet = new Subnet();
		actualSubnet.setSubnetId("subnetId");
		actualSubnet.setIpVersion("4");
		
		RouteTarget routeTarget = new RouteTarget();
		routeTarget.setGlobalRouteTarget("globalRouteTarget");
		
		VpnBinding vpnBinding = new VpnBinding();
		vpnBinding.setVpnId("vpnId");
		vpnBinding.getRouteTargets().add(routeTarget);
		
		Customer customer = new Customer();
		customer.getVpnBindings().add(vpnBinding);
		ServiceSubscription serviceSubscription = new ServiceSubscription();
		customer.setServiceSubscription(serviceSubscription);
		// set Customer on service instance
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		
		NetworkPolicy networkPolicy = new NetworkPolicy();
		networkPolicy.setNetworkPolicyId("networkPolicyId");
		networkPolicy.setNetworkPolicyFqdn("networkPolicyFqdn");
		
		RouteTableReference routeTableReference = new RouteTableReference();
		routeTableReference.setRouteTableReferenceFqdn("routeTableReferenceFqdn");
		
		l3Network.setModelInfoNetwork(modelInfoNetwork);
		l3Network.setPhysicalNetworkName("physicalNetworkName");
		l3Network.getSubnets().add(actualSubnet);
		l3Network.getNetworkPolicies().add(networkPolicy);
		l3Network.getContrailNetworkRouteTableReferences().add(routeTableReference);
				
		UpdateNetworkRequest expectedUpdateNetworkRequest = new UpdateNetworkRequest();
		expectedUpdateNetworkRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());
		expectedUpdateNetworkRequest.setTenantId(cloudRegion.getTenantId());
		expectedUpdateNetworkRequest.setNetworkId(l3Network.getNetworkId());
		expectedUpdateNetworkRequest.setNetworkStackId(l3Network.getHeatStackId());
		expectedUpdateNetworkRequest.setNetworkName(l3Network.getNetworkName());
		expectedUpdateNetworkRequest.setNetworkType(l3Network.getModelInfoNetwork().getNetworkType());
		expectedUpdateNetworkRequest.setNetworkTypeVersion(l3Network.getModelInfoNetwork().getModelVersion());
		expectedUpdateNetworkRequest.setModelCustomizationUuid(l3Network.getModelInfoNetwork().getModelCustomizationUUID());
		expectedUpdateNetworkRequest.setSubnets(subnets);
		expectedUpdateNetworkRequest.setProviderVlanNetwork(providerVlanNetwork);
		expectedUpdateNetworkRequest.setContrailNetwork(contrailNetwork);
		expectedUpdateNetworkRequest.setNetworkParams(userInput);
		expectedUpdateNetworkRequest.setMsoRequest(msoRequest);
		expectedUpdateNetworkRequest.setSkipAAI(true);
		expectedUpdateNetworkRequest.setBackout(!Boolean.valueOf(orchestrationContext.getIsRollbackEnabled()));
		expectedUpdateNetworkRequest.setMessageId("messageId");
		expectedUpdateNetworkRequest.setNotificationUrl("http://localhost:28080/mso/WorkflowMesssage/NetworkAResponse/messageId");

		doReturn("messageId").when(SPY_networkAdapterObjectMapper).getRandomUuid();
		doReturn("http://localhost:28080/mso/WorkflowMesssage").when(SPY_networkAdapterObjectMapper).getEndpoint();
		UpdateNetworkRequest actualUpdateNetworkRequest = SPY_networkAdapterObjectMapper.createNetworkUpdateRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, customer);
		
		assertThat(actualUpdateNetworkRequest, sameBeanAs(expectedUpdateNetworkRequest).ignoring("msoRequest.requestId"));
	}
	
	@Test
	public void deleteNetworkRequestMapperTest() throws Exception {
		DeleteNetworkRequest expectedDeleteNetworkRequest = new DeleteNetworkRequest();
		String cloudRegionPo = "cloudRegionPo";
		expectedDeleteNetworkRequest.setCloudSiteId(cloudRegionPo);
		
		String messageId = "messageId";
		expectedDeleteNetworkRequest.setMessageId(messageId);
		doReturn(messageId).when(SPY_networkAdapterObjectMapper).getRandomUuid();
		
		ModelInfoNetwork modelInfoNetwork = new ModelInfoNetwork();
		l3Network.setModelInfoNetwork(modelInfoNetwork);
		modelInfoNetwork.setModelCustomizationUUID("modelCustomizationUuid");
		expectedDeleteNetworkRequest.setModelCustomizationUuid(modelInfoNetwork.getModelCustomizationUUID());
		
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId(requestContext.getMsoRequestId());
		msoRequest.setServiceInstanceId(serviceInstance.getServiceInstanceId());
		expectedDeleteNetworkRequest.setMsoRequest(msoRequest);
		
		expectedDeleteNetworkRequest.setNetworkId(l3Network.getNetworkId());
		
		l3Network.setHeatStackId("heatStackId");
		expectedDeleteNetworkRequest.setNetworkStackId(l3Network.getHeatStackId());
		
		expectedDeleteNetworkRequest.setNetworkType(l3Network.getNetworkType());
		
		//TODO may be handled differently in the future. If needed can also ignore messageId on the assertThat call as well instead of mocking it by chaining a .ignoring call with the path
		String callbackUrl = "callbackUrl";
		expectedDeleteNetworkRequest.setNotificationUrl(callbackUrl);
		doReturn(callbackUrl).when(SPY_networkAdapterObjectMapper).createCallbackUrl("NetworkAResponse", messageId);
		
		expectedDeleteNetworkRequest.setSkipAAI(true);
		
		expectedDeleteNetworkRequest.setTenantId(cloudRegion.getTenantId());
		
		DeleteNetworkRequest deleteNetworkRequest = SPY_networkAdapterObjectMapper.deleteNetworkRequestMapper(requestContext, cloudRegion, serviceInstance, l3Network, cloudRegionPo);
		
		assertThat(expectedDeleteNetworkRequest, sameBeanAs(deleteNetworkRequest));
	}
}
