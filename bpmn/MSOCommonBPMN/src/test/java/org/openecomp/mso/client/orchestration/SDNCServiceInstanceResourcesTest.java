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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.BuildingBlockTestDataSetup;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.client.exception.BadResponseException;
import org.openecomp.mso.client.exception.MapperException;
import org.openecomp.mso.client.sdnc.SDNCClient;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcOperation;
import org.openecomp.mso.client.sdnc.endpoint.SDNCTopology;
import org.openecomp.mso.client.sdnc.mapper.ServiceTopologyOperationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;


@RunWith(MockitoJUnitRunner.class)
public class SDNCServiceInstanceResourcesTest  extends BuildingBlockTestDataSetup{
	@InjectMocks
    private SDNCServiceInstanceResources sdncServiceInstanceResources;
	

	@Mock
	protected ServiceTopologyOperationMapper MOCK_serviceTopologyOperationMapper;
	
	private RequestContext requestContext;
	private ServiceInstance serviceInstance;
	private Customer customer;
	
	@Mock
	protected SDNCClient MOCK_sdncClient;
	
	@Before
	public void before() {
		requestContext = buildRequestContext();
		
		serviceInstance = buildServiceInstance();

		customer = buildCustomer();

		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
	}
	
	@Test
	public void assignServiceInstanceSuccessTest() throws Exception {
		doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.ASSIGN), eq(GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE), any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
		doReturn("test").when(MOCK_sdncClient).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
		sdncServiceInstanceResources.assignServiceInstance(serviceInstance, customer, requestContext);
		verify(MOCK_sdncClient, times(1)).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
	}
	
	@Test
	public void assignServiceInstanceExceptionTest() throws Exception {
		expectedException.expect(Exception.class);
		doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.ASSIGN), eq(GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE), any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
		doThrow(Exception.class).when(MOCK_sdncClient).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
		sdncServiceInstanceResources.assignServiceInstance(serviceInstance, customer, requestContext);
	}
	
	@Test
	public void deleteServiceInstanceSuccessTest() throws Exception {
		doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DELETE), eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE), any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
		doReturn("test").when(MOCK_sdncClient).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
		sdncServiceInstanceResources.deleteServiceInstance(serviceInstance, customer, requestContext);
		verify(MOCK_sdncClient, times(1)).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
	}
	
	@Test
	public void deleteServiceInstanceExceptionTest() throws Exception {
		expectedException.expect(Exception.class);
		doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DELETE), eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE), any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
		doThrow(Exception.class).when(MOCK_sdncClient).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
		sdncServiceInstanceResources.deleteServiceInstance(serviceInstance, customer, requestContext);
	}
	
	@Test
	public void unassignServiceInstanceSuccessTest() throws Exception {
		doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DELETE), eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE), any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
		doReturn("test").when(MOCK_sdncClient).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
		sdncServiceInstanceResources.unassignServiceInstance(serviceInstance, customer, requestContext);
		verify(MOCK_sdncClient, times(1)).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
	}
	
	@Test
	public void unassignServiceInstanceExceptionTest() throws Exception {
		expectedException.expect(Exception.class);
		doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DELETE), eq(GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE), any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
		doThrow(Exception.class).when(MOCK_sdncClient).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
		sdncServiceInstanceResources.unassignServiceInstance(serviceInstance,customer, requestContext);
	}

	@Test
	public void deactivateServiceInstanceSuccessTest() throws Exception {
		doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DEACTIVATE), eq(GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE), any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
		doReturn("test").when(MOCK_sdncClient).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
		sdncServiceInstanceResources.deactivateServiceInstance(serviceInstance, customer, requestContext);
		verify(MOCK_sdncClient, times(1)).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
	}
	
	@Test
	public void deactivateServiceInstanceExceptionTest() throws Exception {
		expectedException.expect(Exception.class);
		doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DEACTIVATE), eq(GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE), any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
		doThrow(Exception.class).when(MOCK_sdncClient).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
		sdncServiceInstanceResources.deactivateServiceInstance(serviceInstance, customer, requestContext);
	}

	@Test
	public void test_changeModelServiceInstance() throws MapperException, BadResponseException {
		doReturn(new GenericResourceApiServiceOperationInformation()).when(MOCK_serviceTopologyOperationMapper).reqMapper(eq(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION), eq(SDNCSvcAction.CHANGE_ASSIGN), eq(GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE), any(ServiceInstance.class), any(Customer.class), any(RequestContext.class));
		doReturn("test").when(MOCK_sdncClient).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
		sdncServiceInstanceResources.changeModelServiceInstance(serviceInstance, customer, requestContext);
		verify(MOCK_sdncClient, times(1)).post(any(GenericResourceApiServiceOperationInformation.class), eq(SDNCTopology.SERVICE));
	}
}
