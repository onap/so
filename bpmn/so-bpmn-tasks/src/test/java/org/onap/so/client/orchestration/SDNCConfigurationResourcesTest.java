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

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBondingLink;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.onap.so.client.sdnc.mapper.GCTopologyOperationRequestMapper;

import org.onap.sdnc.northbound.client.model.GenericResourceApiGcTopologyOperationInformation;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SDNCConfigurationResourcesTest extends TestDataSetup{
	
	@InjectMocks
    private SDNCConfigurationResources sdncConfigurationResources = new SDNCConfigurationResources();
	
	@Spy
	GCTopologyOperationRequestMapper MOCK_gcTopologyMapper ;
	
	@Mock
	protected SDNCClient MOCK_sdncClient;
	
    private RequestContext requestContext;
    private ServiceInstance serviceInstance;
    private VpnBondingLink vpnBondingLink;
    private GenericVnf vnf;
    private Customer customer;

    @Before
    public void setUp(){

    	customer = buildCustomer();

        requestContext = buildRequestContext();

        serviceInstance = buildServiceInstance();
        
        vpnBondingLink = buildVpnBondingLink();
        
        vnf = vpnBondingLink.getInfrastructureServiceProxy().getServiceInstance().getVnfs().get(0);
    }

    @Test
    public void activateVnrConfigurationTest() throws BadResponseException, MapperException {

        doReturn("success").when(MOCK_sdncClient).post(isA(GenericResourceApiGcTopologyOperationInformation.class), isA(SDNCTopology.class));
        String response = sdncConfigurationResources.activateVnrConfiguration(serviceInstance,requestContext,customer,vpnBondingLink.getVnrConfiguration(),vnf);
        verify(MOCK_sdncClient, times(1)).post(any(GenericResourceApiGcTopologyOperationInformation.class), any(SDNCTopology.class));
        assertNotNull(response);
    }

    @Test
    public void assignVnrConfigurationTest() throws BadResponseException, MapperException {

        doReturn("success").when(MOCK_sdncClient).post(isA(GenericResourceApiGcTopologyOperationInformation.class), isA(SDNCTopology.class));
        String response = sdncConfigurationResources.assignVnrConfiguration(serviceInstance,requestContext,customer,vpnBondingLink.getVnrConfiguration(),vnf);
        verify(MOCK_sdncClient, times(1)).post(any(GenericResourceApiGcTopologyOperationInformation.class), any(SDNCTopology.class));
        assertNotNull(response);
    }

    @Test
    public void unAssignVnrConfigurationTest() throws BadResponseException, MapperException {
        doReturn("success").when(MOCK_sdncClient).post(isA(GenericResourceApiGcTopologyOperationInformation.class), isA(SDNCTopology.class));
        String response = sdncConfigurationResources.unAssignVnrConfiguration(serviceInstance,requestContext,vpnBondingLink.getVnrConfiguration());
        verify(MOCK_sdncClient, times(1)).post(any(GenericResourceApiGcTopologyOperationInformation.class), any(SDNCTopology.class));
        assertNotNull(response);
    }

    @Test
    public void deactivateVnrConfigurationTest() throws BadResponseException, MapperException {
        doReturn("success").when(MOCK_sdncClient).post(isA(GenericResourceApiGcTopologyOperationInformation.class), isA(SDNCTopology.class));
        String response = sdncConfigurationResources.deactivateVnrConfiguration(serviceInstance,requestContext,vpnBondingLink.getVnrConfiguration());
        verify(MOCK_sdncClient, times(1)).post(any(GenericResourceApiGcTopologyOperationInformation.class), any(SDNCTopology.class));
        assertNotNull(response);
    }
}
