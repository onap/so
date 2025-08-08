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
package org.onap.so.client.adapter.network.mapper;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.so.adapters.nwrest.ContrailNetwork;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.ProviderVlanNetwork;
import org.onap.so.adapters.nwrest.RollbackNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.HostRoute;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTableReference;
import org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTarget;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.NetworkRollback;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NetworkAdapterObjectMapperTest extends TestDataSetup {

    private NetworkAdapterObjectMapper SPY_networkAdapterObjectMapper = Mockito.spy(NetworkAdapterObjectMapper.class);

    private L3Network l3Network;
    private RequestContext requestContext;
    private ServiceInstance serviceInstance;
    private CloudRegion cloudRegion;
    private OrchestrationContext orchestrationContext;
    private Customer customer;
    Map<String, String> userInput;

    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/NetworkMapper/";

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
    public void buildCreateNetworkRequestFromBbobjectTest() {

        String cloudRegionPo = "cloudRegionPo";
        CreateNetworkRequest expectedCreateNetworkRequest = new CreateNetworkRequest();

        expectedCreateNetworkRequest.setCloudSiteId(cloudRegionPo);
        expectedCreateNetworkRequest.setTenantId(cloudRegion.getTenantId());
        expectedCreateNetworkRequest.setNetworkId(l3Network.getNetworkId());
        expectedCreateNetworkRequest.setNetworkName(l3Network.getNetworkName());
        expectedCreateNetworkRequest.setNetworkType(l3Network.getNetworkType());
        expectedCreateNetworkRequest.setBackout(false);
        expectedCreateNetworkRequest.setFailIfExists(false);
        expectedCreateNetworkRequest.setNetworkTechnology("CONTRAIL");
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId(requestContext.getMsoRequestId());
        msoRequest.setServiceInstanceId(serviceInstance.getServiceInstanceId());
        expectedCreateNetworkRequest.setMsoRequest(msoRequest);
        expectedCreateNetworkRequest.setSkipAAI(true);
        HashMap<String, String> networkParams = new HashMap<String, String>();
        networkParams.put("shared", "true");
        networkParams.put("external", "false");
        networkParams.put("testUserInputKey", "testUserInputValue");
        expectedCreateNetworkRequest.setNetworkParams(networkParams);

        expectedCreateNetworkRequest.setNotificationUrl("endpoint/NetworkAResponse/messageId");

        Subnet openstackSubnet = new Subnet();
        HostRoute hostRoute = new HostRoute();
        hostRoute.setHostRouteId("hostRouteId");
        hostRoute.setNextHop("nextHop");
        hostRoute.setRoutePrefix("routePrefix");
        openstackSubnet.getHostRoutes().add(hostRoute);
        List<Subnet> subnetList = new ArrayList<Subnet>();
        subnetList.add(openstackSubnet);
        l3Network.getSubnets().add(openstackSubnet);
        l3Network.setNetworkTechnology("Contrail");
        l3Network.setIsSharedNetwork(true);
        l3Network.setIsExternalNetwork(false);

        doReturn("endpoint/").when(SPY_networkAdapterObjectMapper).getEndpoint();
        doReturn("messageId").when(SPY_networkAdapterObjectMapper).getRandomUuid();

        CreateNetworkRequest createNetworkRequest =
                SPY_networkAdapterObjectMapper.createNetworkRequestMapper(requestContext, cloudRegion,
                        orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, customer);

        assertThat(createNetworkRequest, sameBeanAs(expectedCreateNetworkRequest).ignoring("contrailRequest")
                .ignoring("contrailNetwork").ignoring("providerVlanNetwork").ignoring("subnets").ignoring("messageId"));
    }

    @Test
    public void createNetworkRollbackRequestMapperTest() {

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

        RollbackNetworkRequest rollbackNetworkRequest = SPY_networkAdapterObjectMapper
                .createNetworkRollbackRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance,
                        l3Network, userInput, cloudRegionPo, createNetworkResponse);

        assertThat(rollbackNetworkRequest, sameBeanAs(expectedRollbackNetworkRequest).ignoring("contrailNetwork")
                .ignoring("providerVlanNetwork").ignoring("subnets").ignoring("networkParams").ignoring("messageId"));
    }

    @Test
    public void updateNetworkRequestMapperTest() {
        org.onap.so.openstack.beans.Subnet subnet = new org.onap.so.openstack.beans.Subnet();
        subnet.setSubnetId("subnetId");
        subnet.setGatewayIp("NULL");
        subnet.setHostRoutes(new ArrayList<org.onap.so.openstack.beans.HostRoute>());

        List<org.onap.so.openstack.beans.Subnet> subnets = new ArrayList<>();
        subnets.add(subnet);

        ProviderVlanNetwork providerVlanNetwork =
                new ProviderVlanNetwork("physicalNetworkName", new ArrayList<Integer>());

        List<String> policyFqdns = Arrays.asList("networkPolicyFqdn");

        org.onap.so.openstack.beans.RouteTarget expectedRouteTarget = new org.onap.so.openstack.beans.RouteTarget();
        expectedRouteTarget.setRouteTarget("globalRouteTarget");

        ContrailNetwork contrailNetwork = new ContrailNetwork();
        contrailNetwork.setPolicyFqdns(policyFqdns);
        contrailNetwork.setRouteTableFqdns(new ArrayList<String>());
        contrailNetwork.setRouteTargets(new ArrayList<org.onap.so.openstack.beans.RouteTarget>());
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
        actualSubnet.setDhcpEnabled(false);
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
        l3Network.setIsSharedNetwork(false);
        l3Network.setIsExternalNetwork(false);
        HashMap<String, String> networkParams = new HashMap<String, String>();
        networkParams.put("shared", "false");
        networkParams.put("external", "false");
        networkParams.put("testUserInputKey", "testUserInputValue");

        UpdateNetworkRequest expectedUpdateNetworkRequest = new UpdateNetworkRequest();
        expectedUpdateNetworkRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());
        expectedUpdateNetworkRequest.setTenantId(cloudRegion.getTenantId());
        expectedUpdateNetworkRequest.setNetworkId(l3Network.getNetworkId());
        expectedUpdateNetworkRequest.setNetworkStackId(l3Network.getHeatStackId());
        expectedUpdateNetworkRequest.setNetworkName(l3Network.getNetworkName());
        expectedUpdateNetworkRequest.setNetworkType(l3Network.getModelInfoNetwork().getNetworkType());
        expectedUpdateNetworkRequest.setNetworkTypeVersion(l3Network.getModelInfoNetwork().getModelVersion());
        expectedUpdateNetworkRequest
                .setModelCustomizationUuid(l3Network.getModelInfoNetwork().getModelCustomizationUUID());
        expectedUpdateNetworkRequest.setSubnets(subnets);
        expectedUpdateNetworkRequest.setProviderVlanNetwork(providerVlanNetwork);
        expectedUpdateNetworkRequest.setContrailNetwork(contrailNetwork);
        expectedUpdateNetworkRequest.setNetworkParams(networkParams);
        expectedUpdateNetworkRequest.setMsoRequest(msoRequest);
        expectedUpdateNetworkRequest.setSkipAAI(true);
        expectedUpdateNetworkRequest.setBackout(Boolean.TRUE.equals(orchestrationContext.getIsRollbackEnabled()));
        expectedUpdateNetworkRequest.setMessageId("messageId");
        expectedUpdateNetworkRequest
                .setNotificationUrl("http://localhost:28080/mso/WorkflowMesssage/NetworkAResponse/messageId");

        doReturn("messageId").when(SPY_networkAdapterObjectMapper).getRandomUuid();
        doReturn("http://localhost:28080/mso/WorkflowMesssage").when(SPY_networkAdapterObjectMapper).getEndpoint();
        UpdateNetworkRequest actualUpdateNetworkRequest =
                SPY_networkAdapterObjectMapper.createNetworkUpdateRequestMapper(requestContext, cloudRegion,
                        orchestrationContext, serviceInstance, l3Network, userInput, customer);

        assertThat(actualUpdateNetworkRequest,
                sameBeanAs(expectedUpdateNetworkRequest).ignoring("msoRequest.requestId"));
    }

    @Test
    public void deleteNetworkRequestMapperTest() {
        DeleteNetworkRequest expectedDeleteNetworkRequest = new DeleteNetworkRequest();

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

        expectedDeleteNetworkRequest.setSkipAAI(true);

        expectedDeleteNetworkRequest.setTenantId(cloudRegion.getTenantId());

        expectedDeleteNetworkRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());

        expectedDeleteNetworkRequest.setNotificationUrl("endpoint/NetworkAResponse/messageId");

        doReturn("endpoint/").when(SPY_networkAdapterObjectMapper).getEndpoint();
        doReturn("messageId").when(SPY_networkAdapterObjectMapper).getRandomUuid();

        DeleteNetworkRequest deleteNetworkRequest = SPY_networkAdapterObjectMapper
                .deleteNetworkRequestMapper(requestContext, cloudRegion, serviceInstance, l3Network);

        assertThat(expectedDeleteNetworkRequest, sameBeanAs(deleteNetworkRequest));
    }

    @Test
    public void deleteNetworkRequestNoHeatIdMapperTest() {
        DeleteNetworkRequest expectedDeleteNetworkRequest = new DeleteNetworkRequest();

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

        l3Network.setNetworkName("heatStackId");
        expectedDeleteNetworkRequest.setNetworkStackId("heatStackId");

        expectedDeleteNetworkRequest.setNetworkType(l3Network.getNetworkType());

        expectedDeleteNetworkRequest.setSkipAAI(true);

        expectedDeleteNetworkRequest.setTenantId(cloudRegion.getTenantId());

        expectedDeleteNetworkRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());

        expectedDeleteNetworkRequest.setNotificationUrl("endpoint/NetworkAResponse/messageId");

        doReturn("endpoint/").when(SPY_networkAdapterObjectMapper).getEndpoint();
        doReturn("messageId").when(SPY_networkAdapterObjectMapper).getRandomUuid();
        DeleteNetworkRequest deleteNetworkRequest = SPY_networkAdapterObjectMapper
                .deleteNetworkRequestMapper(requestContext, cloudRegion, serviceInstance, l3Network);

        assertThat(expectedDeleteNetworkRequest, sameBeanAs(deleteNetworkRequest));
    }

    @Test
    public void buildOpenstackSubnetListTest() throws Exception {

        ObjectMapper omapper = new ObjectMapper();
        String bbJson = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "generalBB.json")));
        org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock gbb =
                omapper.readValue(bbJson, org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock.class);
        L3Network myNetwork = gbb.getServiceInstance().getNetworks().get(0);

        String expectedCreateNetworkRequestJson =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "createNetworkRequest.json")));
        org.onap.so.adapters.nwrest.CreateNetworkRequest expectedCreateNetworkRequest = omapper
                .readValue(expectedCreateNetworkRequestJson, org.onap.so.adapters.nwrest.CreateNetworkRequest.class);

        String cloudRegionPo = "cloudRegionPo";

        expectedCreateNetworkRequest.setNotificationUrl("endpoint/NetworkAResponse/messageId");

        doReturn("endpoint/").when(SPY_networkAdapterObjectMapper).getEndpoint();
        doReturn("messageId").when(SPY_networkAdapterObjectMapper).getRandomUuid();
        CreateNetworkRequest createNetworkRequest =
                SPY_networkAdapterObjectMapper.createNetworkRequestMapper(requestContext, cloudRegion,
                        orchestrationContext, serviceInstance, myNetwork, userInput, cloudRegionPo, customer);
        // ignoring dynamic fields and networkParams that throws parsing exception on json file load
        assertThat(createNetworkRequest, sameBeanAs(expectedCreateNetworkRequest).ignoring("messageId")
                .ignoring("msoRequest.requestId").ignoring("networkParams"));
    }

    @Test
    public void buildOpenstackSubnetListMultipleHostRoutesTest() throws Exception {

        ObjectMapper omapper = new ObjectMapper();
        String l3NetworkJson =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "l3-network-multiple-subnets.json")));
        L3Network l3Network = omapper.readValue(l3NetworkJson, L3Network.class);

        List<org.onap.so.openstack.beans.Subnet> subnets =
                SPY_networkAdapterObjectMapper.buildOpenstackSubnetList(l3Network);
        assertEquals("192.168.0.0/16", subnets.get(0).getHostRoutes().get(0).getPrefix());
        assertEquals("192.168.1.5/16", subnets.get(0).getHostRoutes().get(1).getPrefix());


        assertEquals("NULL", subnets.get(1).getGatewayIp());
    }
}
