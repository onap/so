/*
* ============LICENSE_START=======================================================
* ONAP : SO
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.openecomp.mso.bpmn.core.domain;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class ServiceDecompositionTest {
	private ServiceDecomposition sd = new ServiceDecomposition();
	ModelInfo model= new ModelInfo();
	ServiceInstance si= new ServiceInstance();
	Project project= new Project();
	OwningEntity oe= new OwningEntity();
	//VnfResource vnf = new VnfResource();
	List<VnfResource> vnfResources;
	List<NetworkResource> networkResources;
	List<ConfigResource> configResources;
	List<AllottedResource> allottedResources;
	Request request= new Request();
	Customer customer= new Customer();
	

	@Test
	public void testServiceDecomposition() {
		sd.setModelInfo(model);
		sd.setServiceInstance(si);
		sd.setProject(project);
		sd.setOwningEntity(oe);
		sd.setServiceVnfs(vnfResources);
		sd.setServiceConfigs(configResources);
		sd.setServiceNetworks(networkResources);
		sd.setServiceAllottedResources(allottedResources);
		sd.setServiceConfigResources(configResources);
		sd.setServiceType("serviceType");
		sd.setServiceRole("serviceRole");
		sd.setRequest(request);
		sd.setCustomer(customer);
		sd.setCallbackURN("callbackURN");
		sd.setSdncVersion("sdncVersion");
		assertEquals(sd.getModelInfo(), model);
		assertEquals(sd.getServiceInstance(), si);
		assertEquals(sd.getProject(), project);
		assertEquals(sd.getOwningEntity(), oe);
		assertEquals(sd.getServiceVnfs(), vnfResources);
		assertEquals(sd.getServiceConfigs(), configResources);
		assertEquals(sd.getServiceNetworks(), networkResources);
		assertEquals(sd.getServiceAllottedResources(), allottedResources);
		assertEquals(sd.getServiceConfigResources(), configResources);
		assertEquals(sd.getRequest(), request);
		assertEquals(sd.getCustomer(), customer);
		assertEquals(sd.getCallbackURN(), "callbackURN");
		assertEquals(sd.getSdncVersion(), "sdncVersion");
		
		
		
		
		
		
		
		
	}

}
