package org.openecomp.mso.bpmn.infrastructure.common.name.generation;


import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.bpmn.infrastructure.common.name.generation.AAIObjectInstanceNameGenerator;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;


public class AAIObjectInstanceNameGeneratorTest {

	@Before
	public void before() {
	}
		
	@Test
	public void generateInstanceGroupNameTest() throws Exception {
		
		ModelInfoInstanceGroup modelVnfc = new ModelInfoInstanceGroup();
		modelVnfc.setFunction("vre");
		modelVnfc.setType("VNFC");
		
		InstanceGroup instanceGroup = new InstanceGroup();
		instanceGroup.setId("test-001");
		instanceGroup.setModelInfoInstanceGroup(modelVnfc);
		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("vnf-123");
		vnf.setVnfName("test-vnf");
		
		assertEquals("test-vnf_vre", new AAIObjectInstanceNameGenerator().generateInstanceGroupName(instanceGroup, vnf));
	}
	
}
