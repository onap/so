package org.openecomp.mso.bpmn.infrastructure.flowspecific.tasks;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
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
		
		ModelInfoInstanceGroup modelNetworkInstanceGroup = new ModelInfoInstanceGroup();
		modelNetworkInstanceGroup.setType("networkInstanceGroup");
		
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
		verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup1, genericVnf);
		verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup2, genericVnf);
		verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup3, genericVnf);
		verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup4, genericVnf);
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
