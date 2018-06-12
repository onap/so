package org.openecomp.mso.bpmn.infrastructure.flowspecific.tasks;

import java.util.List;

import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.infrastructure.common.name.generation.AAIObjectInstanceNameGenerator;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.AAIInstanceGroupResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignVnf {
	
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private AAIInstanceGroupResources aaiInstanceGroupResources;
	@Autowired
	private AAIObjectInstanceNameGenerator aaiObjectInstanceNameGenerator;
	
	
	public void createInstanceGroups(BuildingBlockExecution execution) {
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			List<InstanceGroup> instanceGroups = vnf.getInstanceGroups();
			for(InstanceGroup instanceGroup : instanceGroups) {
				if(ModelInfoInstanceGroup.TYPE_VNFC.equalsIgnoreCase(instanceGroup.getModelInfoInstanceGroup().getType())) {
					instanceGroup.setInstanceGroupName(aaiObjectInstanceNameGenerator.generateInstanceGroupName(instanceGroup, vnf));
					aaiInstanceGroupResources.createInstanceGroup(instanceGroup);
					aaiInstanceGroupResources.connectInstanceGroupToVnf(instanceGroup, vnf);
				}
				else if(ModelInfoInstanceGroup.TYPE_NETWORK_INSTANCE_GROUP.equalsIgnoreCase(instanceGroup.getModelInfoInstanceGroup().getType())) {
					aaiInstanceGroupResources.connectInstanceGroupToVnf(instanceGroup, vnf);
				}
			}
		} 
		catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

}
