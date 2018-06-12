package org.openecomp.mso.bpmn.infrastructure.flowspecific.tasks;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
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
