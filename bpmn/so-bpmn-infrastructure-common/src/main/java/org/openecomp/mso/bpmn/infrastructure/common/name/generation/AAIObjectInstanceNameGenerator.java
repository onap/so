package org.openecomp.mso.bpmn.infrastructure.common.name.generation;

import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.springframework.stereotype.Component;

@Component
public class AAIObjectInstanceNameGenerator {

	public String generateInstanceGroupName(InstanceGroup instanceGroup, GenericVnf vnf) {
		return vnf.getVnfName() + "_" + instanceGroup.getModelInfoInstanceGroup().getFunction();
	}
	
}
