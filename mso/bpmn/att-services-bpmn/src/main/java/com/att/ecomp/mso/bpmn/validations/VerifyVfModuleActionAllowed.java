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

package com.att.ecomp.mso.bpmn.validations;

import java.util.HashSet;
import java.util.Set;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.validation.PreBuildingBlockValidator;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VerifyVfModuleActionAllowed implements PreBuildingBlockValidator {
	
	private static  Logger logger = LoggerFactory.getLogger(VerifyVfModuleActionAllowed.class);	
	
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	
	@Autowired
	private ExceptionBuilder exceptionUtil;
	
	@Override
	public Set<String> forBuildingBlock() {
		Set<String> bbs= new HashSet<String>();
		bbs.add("UnassignVfModuleBB");
		bbs.add("DeleteVfModuleBB");
		bbs.add("DeactivateVfModuleBB");
		return bbs;
	}
	
	@Override
	public boolean validate(BuildingBlockExecution execution) {
		GenericVnf vnf = null;
		VfModule vfModule = null;
		try {
			vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
				
			String buildingBlockFlowName = execution.getFlowToBeCalled();
			
			if (vfModule.getModelInfoVfModule().getIsBaseBoolean()) {
				for (VfModule existingVfModule : vnf.getVfModules()) {
					if (!existingVfModule.getModelInfoVfModule().getIsBaseBoolean()) {
						String msg = null;					
						if (buildingBlockFlowName.startsWith("Deactivate") && existingVfModule.getOrchestrationStatus().equals(OrchestrationStatus.ACTIVE)) {
							msg = "Not allowed to deactivate a base VF Module when this VNF has an add-on VF Module with Active status in A&AI";
						}
						else if (buildingBlockFlowName.startsWith("Delete") && (existingVfModule.getOrchestrationStatus().equals(OrchestrationStatus.ACTIVE) ||
									existingVfModule.getOrchestrationStatus().equals(OrchestrationStatus.CREATED))) {
							msg = "Not allowed to delete a base VF Module when this VNF has an add-on VF Module with Active or Created status in A&AI";
						}
						else if (buildingBlockFlowName.startsWith("Unassign")) {
							msg = "Not allowed to unassign a base VF Module when this VNF has an add-on VF Module in A&AI";
						}					
						if (msg != null) {
							logger.error(msg);
							return false;
						}
					}
				}
			}
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
		return true;
	}	
}
