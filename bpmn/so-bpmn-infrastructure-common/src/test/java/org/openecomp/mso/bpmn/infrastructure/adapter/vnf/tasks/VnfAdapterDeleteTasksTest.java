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

package org.openecomp.mso.bpmn.infrastructure.adapter.vnf.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.OrchestrationContext;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.springframework.beans.factory.annotation.Autowired;

public class VnfAdapterDeleteTasksTest extends BaseTaskTest{
	@Autowired
	private VnfAdapterDeleteTasks vnfAdapterDeleteTasks;

	private VolumeGroup volumeGroup;
	private VfModule vfModule;
	private GenericVnf genericVnf;
	private RequestContext requestContext;
	private CloudRegion cloudRegion;
	private ServiceInstance serviceInstance;
	private OrchestrationContext orchestrationContext;

	@Before
	public void before() throws Exception {
		requestContext = setRequestContext();

		serviceInstance = setServiceInstance();

		cloudRegion = setCloudRegion();

		genericVnf = setGenericVnf();

		vfModule = setVfModule();
		
		volumeGroup = setVolumeGroup();

		orchestrationContext = setOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);
	}

	@Test
	public void test_deleteVfModule() throws Exception {
		DeleteVfModuleRequest deleteVfModuleRequest = new DeleteVfModuleRequest();
		deleteVfModuleRequest.setVfModuleId("vfModuleId");
		
		doReturn(deleteVfModuleRequest).when(vnfAdapterVfModuleResources).deleteVfModuleRequest(requestContext, cloudRegion, serviceInstance, genericVnf, vfModule);
		
		vnfAdapterDeleteTasks.deleteVfModule(execution);
		
		verify(vnfAdapterVfModuleResources, times(1)).deleteVfModuleRequest(requestContext, cloudRegion, serviceInstance, genericVnf, vfModule);
		assertEquals(execution.getVariable("VNFREST_Request"), deleteVfModuleRequest.toXmlString());
	}

	@Test 
	public void deleteVfModuleExceptionTest() throws Exception {		
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(vnfAdapterVfModuleResources).deleteVfModuleRequest(requestContext, cloudRegion, serviceInstance, genericVnf, vfModule);
		
		vnfAdapterDeleteTasks.deleteVfModule(execution);
	}
	
	@Test
	public void test_deleteVolumeGroup() throws Exception {
		DeleteVolumeGroupResponse deleteVolumeGroupResponse = new DeleteVolumeGroupResponse();
		
		doReturn(deleteVolumeGroupResponse).when(vnfAdapterVolumeGroupResources).deleteVolumeGroup(requestContext, cloudRegion, serviceInstance, volumeGroup);
		
		vnfAdapterDeleteTasks.deleteVolumeGroup(execution);
		
		verify(vnfAdapterVolumeGroupResources, times(1)).deleteVolumeGroup(requestContext, cloudRegion, serviceInstance, volumeGroup);
	}
	
	@Test
	public void deleteVolumeGroupExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(vnfAdapterVolumeGroupResources).deleteVolumeGroup(requestContext, cloudRegion, serviceInstance, volumeGroup);
	
		vnfAdapterDeleteTasks.deleteVolumeGroup(execution);
	}
}
