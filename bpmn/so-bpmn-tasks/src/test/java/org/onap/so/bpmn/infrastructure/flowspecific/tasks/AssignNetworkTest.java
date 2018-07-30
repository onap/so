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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class AssignNetworkTest extends BaseTaskTest {
	
	@Autowired
	private AssignNetwork assignNetwork;
	
	private ServiceInstance serviceInstance;
	private L3Network network;
	
	@Before
	public void before() {
		serviceInstance = setServiceInstance();
		network = setL3Network();
	}
	
	@Test
	public void hasCollectionTest() throws Exception {
		setServiceInstance();
		//collection present by default base test setup
		boolean hasCollection = assignNetwork.hasCollection(execution);
		assertEquals(true, hasCollection);
		
		boolean skip = assignNetwork.skipNetworkCreationInAAI(execution);
		assertEquals(false, skip);
	}
	
	@Test
	public void hasNoCollectionTest() throws Exception {
		//clear collection
		try {
			serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
		} catch(BBObjectNotFoundException e) {
			serviceInstance = setServiceInstance();
		}
		serviceInstance.setCollection(null);
		boolean hasCollection = assignNetwork.hasCollection(execution);
		assertEquals(false, hasCollection);
		
		boolean skip = assignNetwork.skipNetworkCreationInAAI(execution);
		assertEquals(true, skip);
	}
	
	@Test
	public void hasNoCollectionNoNetworkTest() throws Exception {
		//clear collection and updated network status to PRECREATED - when it was NOT found by name
		try {
			serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID,execution.getLookupMap().get(ResourceKey.NETWORK_ID));
		} catch(BBObjectNotFoundException e) {
			serviceInstance = setServiceInstance();
		}
		serviceInstance.setCollection(null);
		network.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		boolean hasCollection = assignNetwork.hasCollection(execution);
		boolean networkFound = assignNetwork.networkFoundByName(execution);
		assertEquals(false, hasCollection);
		assertEquals(false, networkFound);
		
		boolean skip = assignNetwork.skipNetworkCreationInAAI(execution);
		assertEquals(true, skip);
	}

	@Test
	public void hasNetworkNoCollectionTest() throws Exception {
		//clear collection and updated network status to INVENTORIED - when it was found by name
		try {
			serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID,execution.getLookupMap().get(ResourceKey.NETWORK_ID));
		} catch(BBObjectNotFoundException e) {
			serviceInstance = setServiceInstance();
		}
		serviceInstance.setCollection(null);
		network.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
		boolean hasCollection = assignNetwork.hasCollection(execution);
		boolean networkFound = assignNetwork.networkFoundByName(execution);
		assertEquals(false, hasCollection);
		assertEquals(true, networkFound);
		
		boolean skip = assignNetwork.skipNetworkCreationInAAI(execution);
		assertEquals(true, skip);
	}
	
}
