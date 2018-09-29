/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.adapter.network.NetworkAdapterClientException;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

public class NetworkAdapterUpdateTasksTest extends BaseTaskTest{
	@InjectMocks
	private NetworkAdapterUpdateTasks networkAdapterUpdateTasks = new NetworkAdapterUpdateTasks();
	
	private ServiceInstance serviceInstance;
	private L3Network network;
	private RequestContext requestContext;
	private CloudRegion cloudRegion;
	private OrchestrationContext orchestrationContext;
	private Map<String, String> userInput;
	private Customer customer;
	
	@Before
	public void before() throws BBObjectNotFoundException {
		customer = setCustomer();
		serviceInstance = setServiceInstance();
		network = setL3Network();
		requestContext = setRequestContext();
		cloudRegion = setCloudRegion();
		orchestrationContext = setOrchestrationContext();
		userInput = setUserInput();
		userInput.put("userInputKey1", "userInputValue1");
		
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.NETWORK_ID), any())).thenReturn(network);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID), any())).thenReturn(serviceInstance);
		
	}
	
	@Test
	public void updateNetworkTest() throws Exception {
		UpdateNetworkResponse updateNetworkResponse = new UpdateNetworkResponse();
		updateNetworkResponse.setMessageId("messageId");
		updateNetworkResponse.setNetworkId("networkId");
		Optional<UpdateNetworkResponse> oUpdateNetworkResponse = Optional.of(updateNetworkResponse);
		
		doReturn(oUpdateNetworkResponse).when(networkAdapterResources).updateNetwork(requestContext, cloudRegion, orchestrationContext, serviceInstance, network, userInput, customer);
		
		networkAdapterUpdateTasks.updateNetwork(execution);
		
		verify(networkAdapterResources, times(1)).updateNetwork(requestContext, cloudRegion, orchestrationContext, serviceInstance, network, userInput, customer);
		assertEquals(updateNetworkResponse, execution.getVariable("NetworkAdapterUpdateNetworkResponse"));
	}
	
	@Test
	public void updateNetworkNoResponseTest() throws Exception {
		doReturn(Optional.empty()).when(networkAdapterResources).updateNetwork(requestContext, cloudRegion, orchestrationContext, serviceInstance, network, userInput, customer);
		
		networkAdapterUpdateTasks.updateNetwork(execution);
		
		verify(networkAdapterResources, times(1)).updateNetwork(requestContext, cloudRegion, orchestrationContext, serviceInstance, network, userInput, customer);
		assertNull(execution.getVariable("NetworkAdapterUpdateNetworkResponse"));
	}
	
	@Test
	public void updateNetworkExceptionTest() throws UnsupportedEncodingException, NetworkAdapterClientException {
		expectedException.expect(BpmnError.class);
		doThrow(new NetworkAdapterClientException("ERROR")).when(networkAdapterResources).updateNetwork(any(RequestContext.class),any(CloudRegion.class), 
				any(OrchestrationContext.class),eq(serviceInstance),eq(network),any(Map.class),any(Customer.class));
		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
		networkAdapterUpdateTasks.updateNetwork(execution);
	}
}
