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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdnc.northbound.client.model.GenericResourceApiNetworkOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.infrastructure.sdnc.mapper.NetworkTopologyOperationRequestMapper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SDNCNetworkResourcesTest extends TestDataSetup {

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
    public void assignNetworkTest() {
        doReturn(new GenericResourceApiNetworkOperationInformation()).when(MOCK_networkTopologyOperationRequestMapper)
                .reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN,
                        GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance,
                        customer, requestContext, cloudRegion);
        sdncNetworkResources.assignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
        verify(MOCK_networkTopologyOperationRequestMapper, times(1)).reqMapper(
                SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN,
                GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    @Test
    public void rollbackAssignNetworkTest() {
        doReturn(new GenericResourceApiNetworkOperationInformation()).when(MOCK_networkTopologyOperationRequestMapper)
                .reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.UNASSIGN,
                        GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance,
                        customer, requestContext, cloudRegion);
        sdncNetworkResources.rollbackAssignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
        verify(MOCK_networkTopologyOperationRequestMapper, times(1)).reqMapper(
                SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.UNASSIGN,
                GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    @Test
    public void activateNetworkTest() {
        doReturn(new GenericResourceApiNetworkOperationInformation()).when(MOCK_networkTopologyOperationRequestMapper)
                .reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.ACTIVATE,
                        GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance,
                        customer, requestContext, cloudRegion);
        sdncNetworkResources.activateNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
        verify(MOCK_networkTopologyOperationRequestMapper, times(1)).reqMapper(
                SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.ACTIVATE,
                GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    @Test
    public void deleteNetworkTest() {
        doReturn(new GenericResourceApiNetworkOperationInformation()).when(MOCK_networkTopologyOperationRequestMapper)
                .reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.DELETE,
                        GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance,
                        customer, requestContext, cloudRegion);
        sdncNetworkResources.deleteNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
        verify(MOCK_networkTopologyOperationRequestMapper, times(1)).reqMapper(
                SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.DELETE,
                GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    @Test
    public void test_deactivateNetwork() {
        doReturn(new GenericResourceApiNetworkOperationInformation()).when(MOCK_networkTopologyOperationRequestMapper)
                .reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.DEACTIVATE,
                        GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance,
                        customer, requestContext, cloudRegion);
        sdncNetworkResources.deactivateNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
        verify(MOCK_networkTopologyOperationRequestMapper, times(1)).reqMapper(
                SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.DEACTIVATE,
                GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    @Test
    public void changeAssignNetworkTest() {
        doReturn(new GenericResourceApiNetworkOperationInformation()).when(MOCK_networkTopologyOperationRequestMapper)
                .reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.CHANGE_ASSIGN,
                        GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance,
                        customer, requestContext, cloudRegion);
        sdncNetworkResources.changeAssignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
        verify(MOCK_networkTopologyOperationRequestMapper, times(1)).reqMapper(
                SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.CHANGE_ASSIGN,
                GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    @Test
    public void unassignNetwork_Test() {
        doReturn(new GenericResourceApiNetworkOperationInformation()).when(MOCK_networkTopologyOperationRequestMapper)
                .reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.UNASSIGN,
                        GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance,
                        customer, requestContext, cloudRegion);
        sdncNetworkResources.unassignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
        verify(MOCK_networkTopologyOperationRequestMapper, times(1)).reqMapper(
                SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.UNASSIGN,
                GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }
}
