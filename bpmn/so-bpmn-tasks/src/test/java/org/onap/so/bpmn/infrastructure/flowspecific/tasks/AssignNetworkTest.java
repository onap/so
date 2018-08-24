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
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class AssignNetworkTest extends BaseTaskTest {
	
	@Autowired
	private AssignNetwork assignNetwork;
	
	private L3Network network;
	
	@Before
	public void before() {
		network = setL3Network();
	}
	
	@Test
	public void networkNotFoundTest() throws Exception {
		//network status to PRECREATED - when it was NOT found by name
		try {
			network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID,execution.getLookupMap().get(ResourceKey.NETWORK_ID));
		} catch(BBObjectNotFoundException e) {
		}
		
		network.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		boolean networkFound = assignNetwork.networkFoundByName(execution);
		assertEquals(false, networkFound);
	}

	@Test
	public void networkFoundTest() throws Exception {
		try {
			network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID,execution.getLookupMap().get(ResourceKey.NETWORK_ID));
		} catch(BBObjectNotFoundException e) {
		}
		boolean networkFound = assignNetwork.networkFoundByName(execution);
		assertEquals(true, networkFound);
	}
}
