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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Test;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.springframework.beans.factory.annotation.Autowired;

public class UnassignVnfTest extends BaseTaskTest{
	@Autowired
	private UnassignVnf unassignVnf;

	@Test
	public void deleteInstanceGroupsSunnyDayTest() throws Exception {
		GenericVnf genericVnf = setGenericVnf();
		
		ModelInfoInstanceGroup modelVnfc = new ModelInfoInstanceGroup();
		modelVnfc.setType("VNFC");
		
		InstanceGroup instanceGroup1 = new InstanceGroup();
		instanceGroup1.setId("test-001");
		instanceGroup1.setModelInfoInstanceGroup(modelVnfc);
		genericVnf.getInstanceGroups().add(instanceGroup1);
		
		InstanceGroup instanceGroup2 = new InstanceGroup();
		instanceGroup2.setId("test-002");
		instanceGroup2.setModelInfoInstanceGroup(modelVnfc);
		genericVnf.getInstanceGroups().add(instanceGroup2);
		
		unassignVnf.deleteInstanceGroups(execution);
		verify(aaiInstanceGroupResources, times(1)).deleteInstanceGroup(eq(instanceGroup1));
		verify(aaiInstanceGroupResources, times(1)).deleteInstanceGroup(eq(instanceGroup2));	
	}
	
	@Test
	public void deletecreateVnfcInstanceGroupExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		unassignVnf.deleteInstanceGroups(execution);
	}
}
