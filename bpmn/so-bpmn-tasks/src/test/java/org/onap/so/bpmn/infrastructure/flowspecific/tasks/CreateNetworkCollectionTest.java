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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateNetworkCollectionTest extends BaseTaskTest{
	@Autowired
	private CreateNetworkCollection createNetworkCollection;
	
	private L3Network network;
	private ServiceInstance serviceInstance;
	private OrchestrationContext orchestrationContext;
	private CloudRegion cloudRegion;
	
	@Before
	public void before() {
		serviceInstance = setServiceInstance();
		network = setL3Network();
		cloudRegion = setCloudRegion();
		
		List<L3Network> l3NetworkList = new ArrayList<L3Network>();
		l3NetworkList.add(network);
		ModelInfoInstanceGroup modelInfoInstanceGroup = new ModelInfoInstanceGroup();
		modelInfoInstanceGroup.setFunction("function");
		serviceInstance.getCollection().getInstanceGroup().setModelInfoInstanceGroup(modelInfoInstanceGroup);
		
		orchestrationContext = setOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);
	}
	
	@Test
	public void buildCreateNetworkRequestTest() throws Exception {
		createNetworkCollection.buildNetworkCollectionName(execution);
		
		assertEquals(serviceInstance.getServiceInstanceName() + "_" + serviceInstance.getCollection().getInstanceGroup().getModelInfoInstanceGroup().getFunction(), execution.getVariable("networkCollectionName"));
	}
	
	@Test(expected = BpmnError.class)
	public void buildCreateNetworkRequestInstanceGroupModelInfoFunctionNullExceptionTest() throws Exception {
		ModelInfoInstanceGroup modelInfoInstanceGroup = new ModelInfoInstanceGroup();
		serviceInstance.getCollection().getInstanceGroup().setModelInfoInstanceGroup(modelInfoInstanceGroup);
		createNetworkCollection.buildNetworkCollectionName(execution);
	}
	
	@Test(expected = BpmnError.class)
	public void buildCreateNetworkRequestInstanceGroupModelInfoNullTest() throws Exception {
		serviceInstance.getCollection().getInstanceGroup().setModelInfoInstanceGroup(null);
		createNetworkCollection.buildNetworkCollectionName(execution);
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
	
	@Test
	public void connectInstanceGroupToCloudRegionTest() throws Exception {
		doNothing().when(aaiNetworkResources).connectInstanceGroupToCloudRegion(serviceInstance.getCollection().getInstanceGroup(), cloudRegion);
		createNetworkCollection.connectInstanceGroupToCloudRegion(execution);
		verify(aaiNetworkResources, times(1)).connectInstanceGroupToCloudRegion(serviceInstance.getCollection().getInstanceGroup(), cloudRegion);
	}
}
