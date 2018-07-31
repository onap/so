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

package org.onap.so.bpmn.infrastructure.adapter.network.tasks;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

public class NetworkAdapterCreateTasksTest extends BaseTaskTest{
	@Autowired
	private NetworkAdapterCreateTasks networkAdapterCreateTasks;

	private ServiceInstance serviceInstance;
	private RequestContext requestContext;
	private CloudRegion cloudRegion;
	private OrchestrationContext orchestrationContext;
	private L3Network l3Network;
	private Map<String, String> userInput;
	private Customer customer;

	@Before
	public void before() {
		customer = setCustomer();
		serviceInstance = setServiceInstance();
		l3Network = setL3Network();
		userInput = setUserInput();
		userInput.put("userInputKey1", "userInputValue1");
		requestContext = setRequestContext();
		cloudRegion = setCloudRegion();
		orchestrationContext = setOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);
	}
	
	@Test
	public void createNetworkTest() throws Exception {
		CreateNetworkResponse createNetworkResponse = new CreateNetworkResponse();
		createNetworkResponse.setNetworkStackId("networkStackId");
		createNetworkResponse.setNetworkCreated(true);
		Optional<CreateNetworkResponse> oCreateNetworkResponse = Optional.of(createNetworkResponse);

		String cloudRegionPo = "cloudRegionPo";
		execution.setVariable("cloudRegionPo", cloudRegionPo);

		doReturn(oCreateNetworkResponse).when(networkAdapterResources).createNetwork(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo,customer);
		networkAdapterCreateTasks.createNetwork(execution);
		verify(networkAdapterResources, times(1)).createNetwork(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo,customer);
	}
	
	@Test
	public void rollbackCreateNetworkTest() throws Exception {
		CreateNetworkResponse createNetworkResponse = new CreateNetworkResponse();
		createNetworkResponse.setNetworkStackId("networkStackId");
		createNetworkResponse.setNetworkCreated(true);
		execution.setVariable("createNetworkResponse", createNetworkResponse);
		Optional<CreateNetworkResponse> oCreateNetworkResponse = Optional.of(createNetworkResponse);

		String cloudRegionPo = "cloudRegionPo";
		execution.setVariable("cloudRegionPo", cloudRegionPo);

		doReturn(oCreateNetworkResponse).when(networkAdapterResources).rollbackCreateNetwork(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, createNetworkResponse);
		networkAdapterCreateTasks.rollbackCreateNetwork(execution);
		verify(networkAdapterResources, times(1)).rollbackCreateNetwork(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, createNetworkResponse);
	}
}
