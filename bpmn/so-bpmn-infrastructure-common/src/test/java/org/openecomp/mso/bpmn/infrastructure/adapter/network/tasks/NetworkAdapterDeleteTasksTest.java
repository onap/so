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

package org.openecomp.mso.bpmn.infrastructure.adapter.network.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.client.adapter.network.NetworkAdapterClientException;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;

public class NetworkAdapterDeleteTasksTest extends BaseTaskTest{
	@Autowired
	private NetworkAdapterDeleteTasks networkAdapterDeleteTasks;

	private ServiceInstance serviceInstance;
	private L3Network l3Network;
	private RequestContext requestContext;
	private CloudRegion cloudRegion;
	private String cloudRegionPo;

	@Before
	public void before() {
		serviceInstance = setServiceInstance();
		l3Network = setL3Network();
		requestContext = setRequestContext();
		cloudRegion = setCloudRegion();
		cloudRegionPo = "testCloudRegionPo";
	}
	
	@Test
	public void test_deleteNetwork() throws UnsupportedEncodingException, NetworkAdapterClientException {
		execution.setVariable("cloudRegionPo", cloudRegionPo);
		
		DeleteNetworkResponse deleteNetworkResponse = new DeleteNetworkResponse();
		deleteNetworkResponse.setNetworkDeleted(true);
		deleteNetworkResponse.setNetworkId(l3Network.getNetworkId());
		Optional<DeleteNetworkResponse> oDeleteNetworkResponse = Optional.of(deleteNetworkResponse);
		doReturn(oDeleteNetworkResponse).when(networkAdapterResources).deleteNetwork(requestContext, cloudRegion, serviceInstance, l3Network, cloudRegionPo);
		
		networkAdapterDeleteTasks.deleteNetwork(execution);
		
		verify(networkAdapterResources, times(1)).deleteNetwork(requestContext, cloudRegion, serviceInstance, l3Network, cloudRegionPo);
		assertEquals(deleteNetworkResponse, execution.getVariable("deleteNetworkResponse"));
	}
	
	@Test
	public void test_deleteNetwork_exception() {
		expectedException.expect(BpmnError.class);
		
		networkAdapterDeleteTasks.deleteNetwork(execution);
	}
}
