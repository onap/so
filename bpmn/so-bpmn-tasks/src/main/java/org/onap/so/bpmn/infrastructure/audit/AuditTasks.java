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

package org.onap.so.bpmn.infrastructure.audit;


import org.onap.so.audit.beans.AuditInventory;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AuditTasks {

	private static final Logger logger = LoggerFactory.getLogger(AuditTasks.class);

	@Autowired
	private ExceptionBuilder exceptionUtil;

	@Autowired
	private ExtractPojosForBB extractPojosForBB;

	@Autowired
	private Environment env;

	public void isAuditNeeded(BuildingBlockExecution execution) {
		try {
			logger.debug("auditInventoryNeeded Value: {}", env.getProperty("mso.infra.auditInventory"));
			execution.setVariable("auditInventoryNeeded", Boolean.parseBoolean(env.getProperty("mso.infra.auditInventory")));
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	public void setupAuditVariable(BuildingBlockExecution execution) {
		try {
			execution.setVariable("auditInventory",createAuditInventory(execution));
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	private AuditInventory createAuditInventory(BuildingBlockExecution execution) throws BBObjectNotFoundException {
			AuditInventory auditInventory = new AuditInventory();

			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
			CloudRegion cloudRegion = gBBInput.getCloudRegion();

			auditInventory.setCloudOwner(cloudRegion.getCloudOwner());
			auditInventory.setCloudRegion(cloudRegion.getLcpCloudRegionId());
			auditInventory.setTenantId(cloudRegion.getTenantId());
			auditInventory.setHeatStackName(vfModule.getVfModuleName());
			return auditInventory;
	}
}
