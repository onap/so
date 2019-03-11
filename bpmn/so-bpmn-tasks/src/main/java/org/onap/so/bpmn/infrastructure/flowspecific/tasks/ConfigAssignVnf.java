/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra.
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

import java.util.UUID;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Get vnf related data
 *
 */
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
	@Autowired
	private CatalogDbClient catalogDbClient;

	/**
	 * Getting the vnf object and set in execution object
	 * 
	 * @param execution
	 */
	public void preProcessRequest(BuildingBlockExecution execution) {
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID,
					execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			execution.setVariable("requestObject", vnf);

			VnfResourceCustomization vrc = catalogDbClient.getVnfResourceCustomizationByModelCustomizationUUID(
					vnf.getModelInfoGenericVnf().getModelCustomizationUuid());
			//get blueprint name/version from VnfResourceCustomization
			//String blueprintName = vrc.getBlueprintName();
			//String blueprintVersion = vrc.getBlueprintVersion();
			//execution.setVariable("blueprintName", blueprintName);
			//execution.setVariable("blueprintVersion", blueprintVersion);
			
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}

	}

	/**
	 * Getting the vnf data, blueprint name, blueprint version etc and setting them
	 * in execution object
	 * 
	 * @param execution
	 */
	public void preProcessAbstractCDSProcessing(BuildingBlockExecution execution) {
		logger.info("Start preProcessAbstractCDSProcessing ");
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

			execution.setVariable("originatorId", ORIGINATOR_ID);
			execution.setVariable("requestId", gBBInput.getRequestContext().getMsoRequestId());
			execution.setVariable("subRequestId", UUID.randomUUID().toString());
			execution.setVariable("actionName", ACTION_NAME);
			execution.setVariable("mode", MODE);

		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
