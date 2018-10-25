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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.client.adapter.network.NetworkAdapterClientException;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.orchestration.NetworkAdapterResources;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;


public class NetworkAdapterDeleteTasksTest extends BaseTaskTest{	
	
	@InjectMocks
	private NetworkAdapterDeleteTasks networkAdapterDeleteTasks = new NetworkAdapterDeleteTasks();

	private ServiceInstance serviceInstance;
	private L3Network l3Network;
	private RequestContext requestContext;
	private CloudRegion cloudRegion;


	@Before
	public void before() throws BBObjectNotFoundException {
		serviceInstance = setServiceInstance();
		l3Network = setL3Network();
		requestContext = setRequestContext();
		cloudRegion = setCloudRegion();

		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.NETWORK_ID), any())).thenReturn(l3Network);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID), any())).thenReturn(serviceInstance);
		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
	}

	@Test
	public void test_deleteNetwork() throws UnsupportedEncodingException, NetworkAdapterClientException {		
		DeleteNetworkResponse deleteNetworkResponse = new DeleteNetworkResponse();
		deleteNetworkResponse.setNetworkDeleted(true);
		deleteNetworkResponse.setNetworkId(l3Network.getNetworkId());
		Optional<DeleteNetworkResponse> oDeleteNetworkResponse = Optional.of(deleteNetworkResponse);
		
		when(networkAdapterResources.deleteNetwork(any(RequestContext.class), any(CloudRegion.class), eq(serviceInstance), eq(l3Network))).thenReturn(oDeleteNetworkResponse);

		networkAdapterDeleteTasks.deleteNetwork(execution);

		verify(networkAdapterResources, times(1)).deleteNetwork(requestContext, cloudRegion, serviceInstance, l3Network);
		assertEquals(deleteNetworkResponse, execution.getVariable("deleteNetworkResponse"));
	}

	@Test
	public void test_deleteNetwork_exception() throws UnsupportedEncodingException, NetworkAdapterClientException {
		expectedException.expect(BpmnError.class);

		doThrow(NetworkAdapterClientException.class).when(networkAdapterResources).
		deleteNetwork(any(RequestContext.class), any(CloudRegion.class), any(ServiceInstance.class), eq(l3Network));
		networkAdapterDeleteTasks.deleteNetwork(execution);
	}
}
