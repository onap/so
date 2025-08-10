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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.RollbackNetworkRequest;
import org.onap.so.adapters.nwrest.RollbackNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.HostRoute;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.adapter.network.NetworkAdapterClientException;
import org.onap.so.client.adapter.network.NetworkAdapterClientImpl;
import org.onap.so.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.entity.MsoRequest;
import com.shazam.shazamcrest.matcher.Matchers;


@RunWith(MockitoJUnitRunner.Silent.class)
public class NetworkAdapterResourcesTest extends TestDataSetup {

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


        doReturn(expectedCreateNetworkResponse).when(MOCK_networkAdapterClient)
                .createNetwork(isA(CreateNetworkRequest.class));

        doReturn(createNetworkRequest).when(MOCK_networkAdapterObjectMapper).createNetworkRequestMapper(
                isA(RequestContext.class), isA(CloudRegion.class), isA(OrchestrationContext.class),
                isA(ServiceInstance.class), isA(L3Network.class), isA(Map.class), isA(String.class),
                isA(Customer.class));

        CreateNetworkResponse actualCreateNetwrokResponse =
                (networkAdapterResources.createNetwork(requestContext, cloudRegion, orchestrationContext,
                        serviceInstance, l3Network, userInput, cloudRegionPo, customer)).get();

        verify(MOCK_networkAdapterClient, times(1)).createNetwork(createNetworkRequest);

        verify(MOCK_networkAdapterObjectMapper, times(1)).createNetworkRequestMapper(requestContext, cloudRegion,
                orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, customer);

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

        doReturn(rollbackNetworkResponse).when(MOCK_networkAdapterClient).rollbackNetwork(isA(String.class),
                isA(RollbackNetworkRequest.class));

        doReturn(rollbackNetworkRequest).when(MOCK_networkAdapterObjectMapper).createNetworkRollbackRequestMapper(
                isA(RequestContext.class), isA(CloudRegion.class), isA(OrchestrationContext.class),
                isA(ServiceInstance.class), isA(L3Network.class), isA(Map.class), isA(String.class),
                isA(CreateNetworkResponse.class));

        RollbackNetworkResponse actualRollbackCreateNetwrokResponse =
                (networkAdapterResources.rollbackCreateNetwork(requestContext, cloudRegion, orchestrationContext,
                        serviceInstance, l3Network, userInput, cloudRegionPo, createNetworkResponse)).get();

        verify(MOCK_networkAdapterClient, times(1)).rollbackNetwork(l3Network.getNetworkId(), rollbackNetworkRequest);

        verify(MOCK_networkAdapterObjectMapper, times(1)).createNetworkRollbackRequestMapper(requestContext,
                cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo,
                createNetworkResponse);

        assertThat(expectedRollbackNetworkResponse, Matchers.sameBeanAs(actualRollbackCreateNetwrokResponse));
    }

    @Test
    public void updateNetworkTest() throws UnsupportedEncodingException, NetworkAdapterClientException {

        doReturn(new UpdateNetworkRequest()).when(MOCK_networkAdapterObjectMapper).createNetworkUpdateRequestMapper(
                isA(RequestContext.class), isA(CloudRegion.class), isA(OrchestrationContext.class),
                isA(ServiceInstance.class), isA(L3Network.class), isA(Map.class), isA(Customer.class));

        doReturn(new UpdateNetworkResponse()).when(MOCK_networkAdapterClient).updateNetwork(isA(String.class),
                isA(UpdateNetworkRequest.class));

        Optional<UpdateNetworkResponse> actualUpdateNetworkResponse = networkAdapterResources.updateNetwork(
                requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, customer);


        verify(MOCK_networkAdapterObjectMapper, times(1)).createNetworkUpdateRequestMapper(requestContext, cloudRegion,
                orchestrationContext, serviceInstance, l3Network, userInput, customer);
        verify(MOCK_networkAdapterClient, times(1)).updateNetwork(isA(String.class), isA(UpdateNetworkRequest.class));
        assertNotNull(actualUpdateNetworkResponse);
    }

    @Test
    public void deleteNetwork_DeleteAction_Test() throws UnsupportedEncodingException, NetworkAdapterClientException {

        DeleteNetworkRequest deleteNetworkRequest = new DeleteNetworkRequest();
        doReturn(deleteNetworkRequest).when(MOCK_networkAdapterObjectMapper).deleteNetworkRequestMapper(requestContext,
                cloudRegion, serviceInstance, l3Network);

        DeleteNetworkResponse expectedDeleteNetworkResponse = new DeleteNetworkResponse();

        doReturn(expectedDeleteNetworkResponse).when(MOCK_networkAdapterClient).deleteNetwork(l3Network.getNetworkId(),
                deleteNetworkRequest);

        Optional<DeleteNetworkResponse> actualODeleteNetworkResponse =
                networkAdapterResources.deleteNetwork(requestContext, cloudRegion, serviceInstance, l3Network);
        DeleteNetworkResponse actualDeleteNetworkResponse = actualODeleteNetworkResponse.get();

        verify(MOCK_networkAdapterObjectMapper, times(1)).deleteNetworkRequestMapper(requestContext, cloudRegion,
                serviceInstance, l3Network);
        verify(MOCK_networkAdapterClient, times(1)).deleteNetwork(l3Network.getNetworkId(), deleteNetworkRequest);
        assertThat(expectedDeleteNetworkResponse, Matchers.sameBeanAs(actualDeleteNetworkResponse));
    }
}
