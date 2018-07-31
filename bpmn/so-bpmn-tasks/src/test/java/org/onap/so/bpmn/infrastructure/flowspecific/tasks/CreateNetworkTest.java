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

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateNetworkTest extends BaseTaskTest{
	@Autowired
	private CreateNetwork createNetwork;
	
	private L3Network network;
	private ServiceInstance serviceInstance;
	private CloudRegion cloudRegion;
	private OrchestrationContext orchestrationContext;
	private Map<String, String> userInput;
	private RequestContext requestContext;
	private String cloudRegionPo = "testCloudRegionPo";
	private Customer customer;
	
	@Before
	public void before() {
		customer = setCustomer();
		serviceInstance = setServiceInstance();
		network = setL3Network();
		cloudRegion = setCloudRegion();
		orchestrationContext = setOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);
		requestContext = setRequestContext();
		userInput = setUserInput();

		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);

	}
	
	@Test
	public void buildCreateNetworkRequestTest() throws Exception {
		execution.setVariable("cloudRegionPo", cloudRegionPo);
		
		CreateNetworkRequest expectedCreateNetworkRequest = new CreateNetworkRequest();
		
		doReturn(expectedCreateNetworkRequest).when(networkAdapterObjectMapper).createNetworkRequestMapper(requestContext, cloudRegion,  orchestrationContext, serviceInstance, network, userInput, cloudRegionPo, customer);
		
		createNetwork.buildCreateNetworkRequest(execution);
		
		verify(networkAdapterObjectMapper, times(1)).createNetworkRequestMapper(requestContext, cloudRegion,  orchestrationContext, serviceInstance, network, userInput, cloudRegionPo, customer);
		
		assertThat(expectedCreateNetworkRequest, sameBeanAs(execution.getVariable("createNetworkRequest")));
	}
}
