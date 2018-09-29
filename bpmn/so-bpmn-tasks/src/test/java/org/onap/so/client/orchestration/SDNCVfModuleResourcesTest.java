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

import static org.mockito.Matchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.onap.so.client.sdnc.mapper.VfModuleTopologyOperationRequestMapper;

import org.onap.sdnc.northbound.client.model.GenericResourceApiVfModuleOperationInformation;;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SDNCVfModuleResourcesTest  extends TestDataSetup{	
	@InjectMocks
	private SDNCVfModuleResources sdncVfModuleResources = new SDNCVfModuleResources();
	
	private VfModule vfModule;
	private GenericVnf vnf;
	private ServiceInstance serviceInstance;
	private VolumeGroup volumeGroup;
	private Customer customer;
	private CloudRegion cloudRegion;
	private RequestContext requestContext;
	
	@Mock
	protected SDNCClient MOCK_sdncClient;
	
	@Spy
	protected VfModuleTopologyOperationRequestMapper vfModuleTopologyMapper;
	
	@Before
	public void before() {
		vfModule = buildVfModule();
		vnf = buildGenericVnf();
		serviceInstance = buildServiceInstance();
		volumeGroup = buildVolumeGroup();
		customer = buildCustomer();
		cloudRegion = buildCloudRegion();
		requestContext = buildRequestContext();
	}
	
	@Test
	public void assignVfModuleTest() throws MapperException, BadResponseException {
		doReturn("test").when(MOCK_sdncClient).post(isA(GenericResourceApiVfModuleOperationInformation.class), isA(SDNCTopology.class));
		
		sdncVfModuleResources.assignVfModule(vfModule, volumeGroup, vnf, serviceInstance, customer, cloudRegion, requestContext);
		
		verify(MOCK_sdncClient, times(1)).post(isA(GenericResourceApiVfModuleOperationInformation.class), eq(SDNCTopology.VFMODULE));
	}
	
	@Test
	public void unassignVfModuleTest() throws MapperException, BadResponseException {
		doReturn("test").when(MOCK_sdncClient).post(isA(GenericResourceApiVfModuleOperationInformation.class), eq(SDNCTopology.VFMODULE));
		
		sdncVfModuleResources.unassignVfModule(vfModule, vnf, serviceInstance);
		
		verify(MOCK_sdncClient, times(1)).post(isA(GenericResourceApiVfModuleOperationInformation.class), eq(SDNCTopology.VFMODULE));
	}
	
	@Test
	public void activateVfModuleTest() throws MapperException, BadResponseException {
		doReturn("test").when(MOCK_sdncClient).post(isA(GenericResourceApiVfModuleOperationInformation.class), isA(SDNCTopology.class));
		
		sdncVfModuleResources.activateVfModule(vfModule, vnf, serviceInstance, customer, cloudRegion, requestContext);
		
		verify(MOCK_sdncClient, times(1)).post(isA(GenericResourceApiVfModuleOperationInformation.class), eq(SDNCTopology.VFMODULE));
	}
	
	@Test
	public void deactivateVfModuleTest() throws MapperException, BadResponseException {
		doReturn("test").when(MOCK_sdncClient).post(isA(GenericResourceApiVfModuleOperationInformation.class), isA(SDNCTopology.class));
		
		sdncVfModuleResources.deactivateVfModule(vfModule, vnf, serviceInstance, customer, cloudRegion, requestContext);
		
		verify(MOCK_sdncClient, times(1)).post(isA(GenericResourceApiVfModuleOperationInformation.class), eq(SDNCTopology.VFMODULE));
	}
	
	@Test
	public void changeAssignVfModuleTest() throws MapperException, BadResponseException {
		doReturn("test").when(MOCK_sdncClient).post(isA(GenericResourceApiVfModuleOperationInformation.class), isA(SDNCTopology.class));
		
		sdncVfModuleResources.changeAssignVfModule(vfModule, vnf, serviceInstance, customer, cloudRegion, requestContext);
		
		verify(MOCK_sdncClient, times(1)).post(isA(GenericResourceApiVfModuleOperationInformation.class), eq(SDNCTopology.VFMODULE));
	}
}
