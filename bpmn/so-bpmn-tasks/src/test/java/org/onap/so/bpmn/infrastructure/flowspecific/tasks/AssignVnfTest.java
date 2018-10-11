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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.onap.so.client.aai.entities.AAIEdgeLabel;
import org.springframework.beans.factory.annotation.Autowired;

public class AssignVnfTest extends BaseTaskTest {
	@Autowired
	private AssignVnf assignVnf;
	
	private InstanceGroup instanceGroup1;
	private InstanceGroup instanceGroup2;
	private InstanceGroup instanceGroup3;
	private InstanceGroup instanceGroup4;
	private GenericVnf genericVnf;
	
	@Before
	public void before() {
		ModelInfoInstanceGroup modelVnfc = new ModelInfoInstanceGroup();
		modelVnfc.setType("VNFC");
		modelVnfc.setFunction("function");
		
		ModelInfoInstanceGroup modelNetworkInstanceGroup = new ModelInfoInstanceGroup();
		modelNetworkInstanceGroup.setType("networkInstanceGroup");
		modelNetworkInstanceGroup.setFunction("function");
		
		instanceGroup1 = new InstanceGroup();
		instanceGroup1.setId("test-001");
		instanceGroup1.setModelInfoInstanceGroup(modelVnfc);
		
		instanceGroup2 = new InstanceGroup();
		instanceGroup2.setId("test-002");
		instanceGroup2.setModelInfoInstanceGroup(modelVnfc);
		
		instanceGroup3 = new InstanceGroup();
		instanceGroup3.setId("test-003");
		instanceGroup3.setModelInfoInstanceGroup(modelNetworkInstanceGroup);
		
		instanceGroup4 = new InstanceGroup();
		instanceGroup4.setId("test-004");
		instanceGroup4.setModelInfoInstanceGroup(modelNetworkInstanceGroup);
		
		genericVnf = setGenericVnf();
		genericVnf.setVnfName("vnfName");
	}

	@Test
	public void createInstanceGroupsSunnyDayTest() throws Exception {
		
		List<InstanceGroup> instanceGroupList = genericVnf.getInstanceGroups();
		instanceGroupList.add(instanceGroup1);
		instanceGroupList.add(instanceGroup2);
		instanceGroupList.add(instanceGroup3);
		instanceGroupList.add(instanceGroup4);

		assignVnf.createInstanceGroups(execution);
		verify(aaiInstanceGroupResources, times(1)).createInstanceGroup(instanceGroup1);
		verify(aaiInstanceGroupResources, times(1)).createInstanceGroup(instanceGroup2);
		verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup1, genericVnf, AAIEdgeLabel.BELONGS_TO);
		verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup2, genericVnf, AAIEdgeLabel.BELONGS_TO);
		verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup3, genericVnf, AAIEdgeLabel.USES);
		verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup4, genericVnf, AAIEdgeLabel.USES);
	}
	
	@Test
	public void createVnfcInstanceGroupNoneTest() throws Exception {
		assignVnf.createInstanceGroups(execution);
		verify(aaiInstanceGroupResources, times(0)).createInstanceGroup(any(InstanceGroup.class));
		verify(aaiInstanceGroupResources, times(0)).connectInstanceGroupToVnf(any(InstanceGroup.class), any(GenericVnf.class));
	}

	@Test
	public void createVnfcInstanceGroupExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		genericVnf.setVnfId("test-999");
		assignVnf.createInstanceGroups(execution);
	}
}
