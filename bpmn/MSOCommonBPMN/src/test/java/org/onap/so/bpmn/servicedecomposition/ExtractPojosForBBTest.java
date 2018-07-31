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

package org.onap.so.bpmn.servicedecomposition;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.BaseTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.bbobjects.*;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;

public class ExtractPojosForBBTest extends BaseTest{
	ExtractPojosForBB extractPojos = new ExtractPojosForBB();
	private BuildingBlockExecution execution;
	private GeneralBuildingBlock gBBInput;
	private HashMap<ResourceKey, String> lookupKeyMap;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void before() {
		execution = new DelegateExecutionImpl(new ExecutionImpl());
		execution.setVariable("testProcessKey", "AAICreateTasksTests");
		gBBInput = new GeneralBuildingBlock();
		execution.setVariable("gBBInput", gBBInput);
		lookupKeyMap = new HashMap<>();
		execution.setVariable("lookupKeyMap", lookupKeyMap);
	}
	
	@Test
	public void get() throws BBObjectNotFoundException {
		ServiceInstance serviceInstancePend = new ServiceInstance();
		serviceInstancePend.setServiceInstanceId("abc");
		lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, serviceInstancePend.getServiceInstanceId());

		VpnBondingLink vpnBondingLinkPend = new VpnBondingLink();
		vpnBondingLinkPend.setVpnBondingLinkId("testVpnBondingLink");
		serviceInstancePend.getVpnBondingLinks().add(vpnBondingLinkPend);
		lookupKeyMap.put(ResourceKey.VPN_BONDING_LINK_ID, vpnBondingLinkPend.getVpnBondingLinkId());

		Customer customer = new Customer();
		customer.setServiceSubscription(new ServiceSubscription());
		VpnBinding vpnBinding = new VpnBinding();
		vpnBinding.setVpnId("abc");
		customer.getVpnBindings().add(vpnBinding);
		lookupKeyMap.put(ResourceKey.VPN_ID, vpnBinding.getVpnId());

		List<GenericVnf> vnfsPend = serviceInstancePend.getVnfs();
		GenericVnf vnfPend = new GenericVnf();
		vnfPend.setVnfId("abc");
		lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, vnfPend.getVnfId());

		List<VfModule> vfModulesPend = vnfPend.getVfModules();
		VfModule vfModulePend = new VfModule();
		vfModulePend.setVfModuleId("abc");
		vfModulesPend.add(vfModulePend);
		vnfsPend.add(vnfPend);
		lookupKeyMap.put(ResourceKey.VF_MODULE_ID, vfModulePend.getVfModuleId());

		List<L3Network> networksPend = serviceInstancePend.getNetworks();
		L3Network networkPend = new L3Network();
		networkPend.setNetworkId("abc");
		networksPend.add(networkPend);
		lookupKeyMap.put(ResourceKey.NETWORK_ID, networkPend.getNetworkId());

		List<VolumeGroup> volumeGroupsPend = serviceInstancePend.getVnfs().get(0).getVolumeGroups();
		VolumeGroup volumeGroupPend = new VolumeGroup();
		volumeGroupPend.setVolumeGroupId("abc");
		volumeGroupsPend.add(volumeGroupPend);
		lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, volumeGroupPend.getVolumeGroupId());

		List<AllottedResource> allotedResourcesPend = serviceInstancePend.getAllottedResources();
		AllottedResource allotedResourcePend = new AllottedResource();
		allotedResourcePend.setId("abc");
		allotedResourcesPend.add(allotedResourcePend);
		lookupKeyMap.put(ResourceKey.ALLOTTED_RESOURCE_ID, allotedResourcePend.getId());

		Configuration configurationPend = new Configuration();
		configurationPend.setConfigurationId("abc");
		serviceInstancePend.getConfigurations().add(configurationPend);
		lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, configurationPend.getConfigurationId());

		customer.getServiceSubscription().getServiceInstances().add(serviceInstancePend);
		gBBInput.setCustomer(customer);

		ServiceInstance extractServPend = extractPojos.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, "abc");
		assertEquals(extractServPend.getServiceInstanceId(), serviceInstancePend.getServiceInstanceId());
		GenericVnf extractVnfPend = extractPojos.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, "abc");
		assertEquals(extractVnfPend.getVnfId(), vnfPend.getVnfId());
		L3Network extractNetworkPend = extractPojos.extractByKey(execution, ResourceKey.NETWORK_ID, "abc");
		assertEquals(extractNetworkPend.getNetworkId(), networkPend.getNetworkId());
		VolumeGroup extractVolumeGroupPend = extractPojos.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID, "abc");
		assertEquals(extractVolumeGroupPend.getVolumeGroupId(), volumeGroupPend.getVolumeGroupId());
		AllottedResource extractallotedResourcePend = extractPojos.extractByKey(execution,
				ResourceKey.ALLOTTED_RESOURCE_ID, "abc");
		assertEquals(extractallotedResourcePend.getId(), allotedResourcePend.getId());
		Configuration extractConfigurationPend = extractPojos.extractByKey(execution, ResourceKey.CONFIGURATION_ID,
				"abc");
		assertEquals(extractConfigurationPend.getConfigurationId(), configurationPend.getConfigurationId());
		VpnBinding extractVpnBinding = extractPojos.extractByKey(execution, ResourceKey.VPN_ID, "abc");
		assertEquals(extractVpnBinding.getVpnId(), vpnBinding.getVpnId());
		
		VfModule extractVfModulePend = extractPojos.extractByKey(execution, ResourceKey.VF_MODULE_ID, "abc");
		assertEquals(extractVfModulePend.getVfModuleId(), vfModulePend.getVfModuleId());

		VpnBondingLink extractVpnBondingLinkPend = extractPojos.extractByKey(execution, ResourceKey.VPN_BONDING_LINK_ID, "testVpnBondingLink");
		assertEquals(extractVpnBondingLinkPend.getVpnBondingLinkId(), vpnBondingLinkPend.getVpnBondingLinkId());
	}

	@Test
	public void siError() throws BBObjectNotFoundException {
		expectedException.expect(BBObjectNotFoundException.class);

		Customer customer = new Customer();
		customer.setServiceSubscription(new ServiceSubscription());
		ServiceInstance serviceInstance = new ServiceInstance();
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		gBBInput.setCustomer(customer);

		extractPojos.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, "abc");
	}

	@Test
	public void vnfError() throws BBObjectNotFoundException {
		expectedException.expect(BBObjectNotFoundException.class);

		Customer customer = new Customer();
		customer.setServiceSubscription(new ServiceSubscription());
		ServiceInstance serviceInstance = new ServiceInstance();
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		gBBInput.setCustomer(customer);
		extractPojos.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, "bbb");
	}

	@Test
	public void vfModuleError() throws BBObjectNotFoundException {
		expectedException.expect(BBObjectNotFoundException.class);

		Customer customer = new Customer();
		customer.setServiceSubscription(new ServiceSubscription());
		ServiceInstance serviceInstance = new ServiceInstance();
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		gBBInput.setCustomer(customer);
		extractPojos.extractByKey(execution, ResourceKey.VF_MODULE_ID, "bbb");
	}
	
	@Test
	public void configurationError() throws BBObjectNotFoundException {
		expectedException.expect(BBObjectNotFoundException.class);

		Customer customer = new Customer();
		customer.setServiceSubscription(new ServiceSubscription());
		ServiceInstance serviceInstance = new ServiceInstance();
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		gBBInput.setCustomer(customer);
		extractPojos.extractByKey(execution, ResourceKey.CONFIGURATION_ID, "bbb");
	}
	@Test
	public void allotedError() throws BBObjectNotFoundException {
		expectedException.expect(BBObjectNotFoundException.class);

		Customer customer = new Customer();
		customer.setServiceSubscription(new ServiceSubscription());
		ServiceInstance serviceInstance = new ServiceInstance();
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		gBBInput.setCustomer(customer);
		extractPojos.extractByKey(execution, ResourceKey.ALLOTTED_RESOURCE_ID, "bbb");
	}
	@Test
	public void vpnBindingError() throws BBObjectNotFoundException {
		expectedException.expect(BBObjectNotFoundException.class);
		Customer customer = new Customer();
		customer.setServiceSubscription(new ServiceSubscription());
		ServiceInstance serviceInstance = new ServiceInstance();
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		gBBInput.setCustomer(customer);
		extractPojos.extractByKey(execution, ResourceKey.VPN_ID, "bbb");
	}

	@Test
	public void vpnBondingLinkError() throws BBObjectNotFoundException {
		expectedException.expect(BBObjectNotFoundException.class);
		Customer customer = new Customer();
		customer.setServiceSubscription(new ServiceSubscription());
		ServiceInstance serviceInstance = new ServiceInstance();
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		gBBInput.setCustomer(customer);
		extractPojos.extractByKey(execution, ResourceKey.VPN_BONDING_LINK_ID, "bbb");
	}
}
