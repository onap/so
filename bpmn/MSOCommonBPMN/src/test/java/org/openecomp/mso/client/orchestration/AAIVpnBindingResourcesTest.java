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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aai.domain.yang.VpnBindings;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.BuildingBlockTestDataSetup;
import org.openecomp.mso.bpmn.common.InjectionHelper;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.mapper.AAIObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

@RunWith(MockitoJUnitRunner.class)
public class AAIVpnBindingResourcesTest extends BuildingBlockTestDataSetup{

	@Mock
	protected AAIResourcesClient MOCK_aaiResourcesClient;

	@Mock
	protected AAIObjectMapper MOCK_aaiObjectMapper;

	@Mock
	protected InjectionHelper MOCK_injectionHelper;
	
	@InjectMocks
	private AAIVpnBindingResources aaiVpnBindingResources = new AAIVpnBindingResources();
	
	private Customer customer;

	@Before
	public void before() {
		customer = buildCustomer();
		 doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
	}

	@Test
	public void createCustomerTest() {
		org.onap.aai.domain.yang.Customer mappedCustomer = new org.onap.aai.domain.yang.Customer();
		mappedCustomer.setGlobalCustomerId(customer.getGlobalCustomerId());
		
		doReturn(mappedCustomer).when(MOCK_aaiObjectMapper).mapCustomer(customer);
		doNothing().when(MOCK_aaiResourcesClient).create(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.Customer.class));
		
		aaiVpnBindingResources.createCustomer(customer);
		
		verify(MOCK_aaiResourcesClient, times(1)).create(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.Customer.class));
		verify(MOCK_aaiObjectMapper, times(1)).mapCustomer(customer);
	}

	@Test
	public void getVpnBindingTest () {
		org.onap.aai.domain.yang.VpnBinding vpnBinding = new org.onap.aai.domain.yang.VpnBinding();
		vpnBinding.setVpnId("vnfId");
		when(MOCK_aaiResourcesClient.get(eq(org.onap.aai.domain.yang.VpnBinding.class),isA(AAIResourceUri.class))).thenReturn(Optional.of(vpnBinding));
		aaiVpnBindingResources.getVpnBinding("vpnId");
		verify(MOCK_aaiResourcesClient, times(1)).get(eq(org.onap.aai.domain.yang.VpnBinding.class),isA(AAIResourceUri.class));
	}

    @Test
    public void existsCustomerTest() {
		when(MOCK_aaiResourcesClient.exists(isA(AAIResourceUri.class))).thenReturn(true);
		boolean isCustomerExist = aaiVpnBindingResources.existsCustomer(customer);
		verify(MOCK_aaiResourcesClient, times(1)).exists(isA(AAIResourceUri.class));
		assertEquals(true,isCustomerExist);
	}

    @Test
    public void getVpnBindingByCustomerVpnIdTest() {
		when(MOCK_aaiResourcesClient.get(eq(VpnBindings.class),isA(AAIResourceUri.class))).thenReturn(Optional.of(new VpnBindings()));
		Optional<VpnBindings> vpnBindings = aaiVpnBindingResources.getVpnBindingByCustomerVpnId("testCustomerVpnId");
		assertNotNull(vpnBindings.get());
		verify(MOCK_aaiResourcesClient, times(1)).get(eq(org.onap.aai.domain.yang.VpnBindings.class),isA(AAIResourceUri.class));
	}

	@Test
	public void createVpnBindingTest() {
		doNothing().when(MOCK_aaiResourcesClient).create(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.VpnBinding.class));
		org.onap.aai.domain.yang.VpnBinding mappedVpnBinding = new org.onap.aai.domain.yang.VpnBinding();
		mappedVpnBinding.setVpnName("test");

		doReturn(mappedVpnBinding).when(MOCK_aaiObjectMapper).mapVpnBinding(isA(VpnBinding.class));
		VpnBinding vpnBinding = buildVpnBinding();
		aaiVpnBindingResources.createVpnBinding(vpnBinding);

		verify(MOCK_aaiResourcesClient, times(1)).create(isA(AAIResourceUri.class), isA(org.onap.aai.domain.yang.VpnBinding.class));
		verify(MOCK_aaiObjectMapper, times(1)).mapVpnBinding(isA(VpnBinding.class));
	}

	@Test
	public void connectCustomerToVpnBinding() {
		doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
		aaiVpnBindingResources.connectCustomerToVpnBinding("testCustId","testVpnId");
		verify(MOCK_aaiResourcesClient,times(1)).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));
	}
}
