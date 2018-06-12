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
package org.openecomp.mso.bpmn.infrastructure.flowspecific.tasks;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.OrchestrationContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateNetworkCollectionTest extends BaseTaskTest{
	@Autowired
	private CreateNetworkCollection createNetworkCollection;
	
	private L3Network network;
	private ServiceInstance serviceInstance;
	private OrchestrationContext orchestrationContext;
	
	@Before
	public void before() {
		serviceInstance = setServiceInstance();
		network = setL3Network();
		
		List<L3Network> l3NetworkList = new ArrayList<L3Network>();
		l3NetworkList.add(network);
		serviceInstance.getCollection().getInstanceGroup().setL3Networks(l3NetworkList);
		
		orchestrationContext = setOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);
	}
	
	@Test
	public void buildCreateNetworkRequestTest() throws Exception {
		createNetworkCollection.buildNetworkCollectionName(execution);
		
		assertEquals(serviceInstance.getServiceInstanceName() + "_" + serviceInstance.getCollection().getInstanceGroup().getInstanceGroupFunction(), execution.getVariable("networkCollectionName"));
	}
	
	@Test
	public void connectCollectionToInstanceGroupTest() throws Exception {
		doNothing().when(aaiNetworkResources).connectNetworkCollectionInstanceGroupToNetworkCollection(serviceInstance.getCollection().getInstanceGroup(), serviceInstance.getCollection());
		createNetworkCollection.connectCollectionToInstanceGroup(execution);
		verify(aaiNetworkResources, times(1)).connectNetworkCollectionInstanceGroupToNetworkCollection(serviceInstance.getCollection().getInstanceGroup(), serviceInstance.getCollection());
	}
	
	@Test
	public void connectCollectionToServiceInstanceTest() throws Exception {
		doNothing().when(aaiNetworkResources).connectNetworkCollectionToServiceInstance(serviceInstance.getCollection(), serviceInstance);
		createNetworkCollection.connectCollectionToServiceInstance(execution);
		verify(aaiNetworkResources, times(1)).connectNetworkCollectionToServiceInstance(serviceInstance.getCollection(), serviceInstance);
	}
}
