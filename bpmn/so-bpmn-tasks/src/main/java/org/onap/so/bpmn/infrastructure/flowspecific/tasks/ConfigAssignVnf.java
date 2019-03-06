/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 - 2019 TechMahindra.
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

import java.util.Optional;
import java.util.UUID;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.aai.AAICreateResources;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigAssignVnf {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigAssignVnf.class);
	private static final String ORIGINATOR_ID = "SO";
	private static final String ACTION_NAME = "config-assign";
	private static final String MODE = "sync";
	
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	
	
	public void preProcessForConfig(BuildingBlockExecution execution) {
		//TODO: Add logic to fetch blueprint-name and blueprint-version and the object for forwarding to CDS
	}
	
	public void getVnfInstanceFromAAI(BuildingBlockExecution execution) {
		try {
			AAICreateResources aaiCreateRes = new AAICreateResources();
			
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			if(null != vnf.getVnfId() || !"".equals(vnf.getVnfId())){
				Optional<GenericVnf> actualVnf = aaiCreateRes.getVnfInstance(vnf.getVnfId());
				execution.setVariable("requestObject", actualVnf.get());
			}
		} 
		catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void PreProcessAbstractCDSProcessing(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			
			execution.setVariable("originatorId", ORIGINATOR_ID);
			execution.setVariable("requestId", gBBInput.getRequestContext().getMsoRequestId());
			execution.setVariable("subRequestId", UUID.randomUUID().toString());
			execution.setVariable("actionName", ACTION_NAME);
			execution.setVariable("mode", MODE);
			execution.setVariable("blueprintName", "");
			execution.setVariable("blueprintVersion", "");
			
		}catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
