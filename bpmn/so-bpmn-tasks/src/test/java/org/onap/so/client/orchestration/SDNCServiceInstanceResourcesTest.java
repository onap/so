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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceOperationInformation;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.infrastructure.sdnc.mapper.ServiceTopologyOperationMapper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SDNCServiceInstanceResourcesTest extends TestDataSetup {

    @InjectMocks
    private SDNCServiceInstanceResources sdncServiceInstanceResources;
    @Mock
    protected ServiceTopologyOperationMapper MOCK_serviceTopologyOperationMapper;
    private RequestContext requestContext;
    private ServiceInstance serviceInstance;
    private Customer customer;

    @Before
    public void before() {
        requestContext = buildRequestContext();
        serviceInstance = buildServiceInstance();
        customer = buildCustomer();
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
    }

    @Test
    public void assignServiceInstanceSuccessTest() {
        doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper)
                .reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.ASSIGN),
                        eq(GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE),
                        any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
        sdncServiceInstanceResources.assignServiceInstance(serviceInstance, customer, requestContext);
        verify(MOCK_serviceTopologyOperationMapper, times(1)).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION),
                eq(SDNCSvcAction.ASSIGN), eq(GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE),
                any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
    }

    @Test
    public void assignServiceInstanceExceptionTest() {
        expectedException.expect(Exception.class);
        doThrow(Exception.class).when(MOCK_serviceTopologyOperationMapper).reqMapper(
                eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.ASSIGN),
                eq(GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE), any(ServiceInstance.class),
                any(Customer.class), any(RequestContext.class));
        sdncServiceInstanceResources.assignServiceInstance(serviceInstance, customer, requestContext);
    }

    @Test
    public void deleteServiceInstanceSuccessTest() {
        doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper)
                .reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DELETE),
                        eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE),
                        any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
        sdncServiceInstanceResources.deleteServiceInstance(serviceInstance, customer, requestContext);
        verify(MOCK_serviceTopologyOperationMapper, times(1)).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION),
                eq(SDNCSvcAction.DELETE), eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE),
                any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
    }

    @Test
    public void deleteServiceInstanceExceptionTest() {
        expectedException.expect(Exception.class);
        doThrow(Exception.class).when(MOCK_serviceTopologyOperationMapper).reqMapper(
                eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DELETE),
                eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE), any(ServiceInstance.class),
                any(Customer.class), any(RequestContext.class));
        sdncServiceInstanceResources.deleteServiceInstance(serviceInstance, customer, requestContext);
    }

    @Test
    public void unassignServiceInstanceSuccessTest() {
        doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper)
                .reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DELETE),
                        eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE),
                        any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
        sdncServiceInstanceResources.unassignServiceInstance(serviceInstance, customer, requestContext);
        verify(MOCK_serviceTopologyOperationMapper, times(1)).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION),
                eq(SDNCSvcAction.DELETE), eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE),
                any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
    }

    @Test
    public void unassignServiceInstanceExceptionTest() {
        expectedException.expect(Exception.class);
        doThrow(Exception.class).when(MOCK_serviceTopologyOperationMapper).reqMapper(
                eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DELETE),
                eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE), any(ServiceInstance.class),
                any(Customer.class), any(RequestContext.class));
        sdncServiceInstanceResources.unassignServiceInstance(serviceInstance, customer, requestContext);
    }

    @Test
    public void deactivateServiceInstanceSuccessTest() {
        doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper)
                .reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DEACTIVATE),
                        eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE),
                        any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
        sdncServiceInstanceResources.deactivateServiceInstance(serviceInstance, customer, requestContext);
        verify(MOCK_serviceTopologyOperationMapper, times(1)).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION),
                eq(SDNCSvcAction.DEACTIVATE), eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE),
                any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
    }

    @Test
    public void deactivateServiceInstanceExceptionTest() {
        expectedException.expect(Exception.class);
        doThrow(Exception.class).when(MOCK_serviceTopologyOperationMapper).reqMapper(
                eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DEACTIVATE),
                eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE), any(ServiceInstance.class),
                any(Customer.class), any(RequestContext.class));
        sdncServiceInstanceResources.deactivateServiceInstance(serviceInstance, customer, requestContext);
    }

    @Test
    public void test_changeModelServiceInstance() {
        doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper)
                .reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.CHANGE_ASSIGN),
                        eq(GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE),
                        any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
        sdncServiceInstanceResources.changeModelServiceInstance(serviceInstance, customer, requestContext);
        verify(MOCK_serviceTopologyOperationMapper, times(1)).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION),
                eq(SDNCSvcAction.CHANGE_ASSIGN), eq(GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE),
                any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
    }
}
