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

package org.openecomp.mso.bpmn.infrastructure.aai.tasks;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Collection;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.springframework.beans.factory.annotation.Autowired;

public class AAIDeleteTasksTest extends BaseTaskTest {
	@Autowired
	private AAIDeleteTasks aaiDeleteTasks;
	
	private L3Network network;
	private ServiceInstance serviceInstance;
	private GenericVnf genericVnf;
	private VfModule vfModule;
	private Collection collection;
	private InstanceGroup instanceGroup;
	private VolumeGroup volumeGroup;
	private CloudRegion cloudRegion;
	
	@Before
	public void before() {
		serviceInstance = setServiceInstance();
		genericVnf = setGenericVnf();
		vfModule = setVfModule();
		network = setL3Network();
		collection = setCollection();
		instanceGroup = setInstanceGroup();
		volumeGroup = setVolumeGroup();
		cloudRegion = setCloudRegion();
	}
	
	@Test
	public void deleteVfModuleTest() throws Exception {
		doNothing().when(aaiVfModuleResources).deleteVfModule(vfModule, genericVnf);
		aaiDeleteTasks.deleteVfModule(execution);
		verify(aaiVfModuleResources, times(1)).deleteVfModule(vfModule, genericVnf);
	}
	
	@Test
	public void deleteVfModuleExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiVfModuleResources).deleteVfModule(vfModule, genericVnf);
		aaiDeleteTasks.deleteVfModule(execution);
	}
	
	@Test
	public void deleteServiceInstanceTest() throws Exception {
		doNothing().when(aaiServiceInstanceResources).deleteServiceInstance(serviceInstance);
		
		aaiDeleteTasks.deleteServiceInstance(execution);
		
		verify(aaiServiceInstanceResources, times(1)).deleteServiceInstance(serviceInstance);
	}
	
	@Test 
	public void deleteServiceInstanceExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(aaiServiceInstanceResources).deleteServiceInstance(serviceInstance);
		
		aaiDeleteTasks.deleteServiceInstance(execution);
	}	
	
	@Test
	public void deleteVnfTest() throws Exception {
		doNothing().when(aaiVnfResources).deleteVnf(genericVnf);
		aaiDeleteTasks.deleteVnf(execution);
		verify(aaiVnfResources, times(1)).deleteVnf(genericVnf);
	}
	
	@Test
	public void deleteVnfTestException() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiVnfResources).deleteVnf(genericVnf);
		
		aaiDeleteTasks.deleteVnf(execution);
		verify(aaiVnfResources, times(1)).deleteVnf(genericVnf);
	}
	
	@Test 
	public void deleteNetworkTest() throws Exception {
		doNothing().when(aaiNetworkResources).deleteNetwork(network);
		aaiDeleteTasks.deleteNetwork(execution);
		verify(aaiNetworkResources, times(1)).deleteNetwork(network);
	}
	
	@Test
	public void deleteNetworkExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiNetworkResources).deleteNetwork(network);
		
		aaiDeleteTasks.deleteNetwork(execution);
	}
	
	@Test 
	public void deleteCollectionTest() throws Exception {
		doNothing().when(aaiNetworkResources).deleteCollection(collection);
		aaiDeleteTasks.deleteCollection(execution);
		verify(aaiNetworkResources, times(1)).deleteCollection(collection);
	}
	
	@Test
	public void deleteCollectionExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiNetworkResources).deleteCollection(collection);
		
		aaiDeleteTasks.deleteCollection(execution);
	}
	
	@Test 
	public void deleteInstanceGroupTest() throws Exception {
		doNothing().when(aaiNetworkResources).deleteNetworkInstanceGroup(instanceGroup);
		aaiDeleteTasks.deleteInstanceGroup(execution);
		verify(aaiNetworkResources, times(1)).deleteNetworkInstanceGroup(instanceGroup);
	}
	
	@Test
	public void deletedInstanceGroupExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiNetworkResources).deleteNetworkInstanceGroup(instanceGroup);
		
		aaiDeleteTasks.deleteInstanceGroup(execution);
	}

	@Test
	public void deleteVolumeGroupTest() {
		doNothing().when(aaiVolumeGroupResources).deleteVolumeGroup(volumeGroup, cloudRegion);
		
		aaiDeleteTasks.deleteVolumeGroup(execution);
		
		verify(aaiVolumeGroupResources, times(1)).deleteVolumeGroup(volumeGroup, cloudRegion);
	}
	
	@Test
	public void deleteVolumeGroupExceptionTest() {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(aaiVolumeGroupResources).deleteVolumeGroup(volumeGroup, cloudRegion);
		
		aaiDeleteTasks.deleteVolumeGroup(execution);
	}
}

