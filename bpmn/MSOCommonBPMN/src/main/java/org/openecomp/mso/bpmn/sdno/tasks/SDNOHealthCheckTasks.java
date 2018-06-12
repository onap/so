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

package org.openecomp.mso.bpmn.sdno.tasks;

import java.util.Map;

import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.SDNOHealthCheckResources;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SDNOHealthCheckTasks {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SDNOHealthCheckTasks.class);
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private SDNOHealthCheckResources sdnoHealthCheckResources;
	
	public void sdnoHealthCheck(BuildingBlockExecution execution) {
		boolean response = false;
		try {			
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			RequestContext requestContext = gBBInput.getRequestContext();
			
			GenericVnf vnf = null;
			Map<ResourceKey, String> lookupMap = execution.getLookupMap();
			for (Map.Entry<ResourceKey, String> entry : lookupMap.entrySet()) {
				if (entry.getKey().equals(ResourceKey.GENERIC_VNF_ID)) {
					vnf = extractPojosForBB.extractByKey(execution, entry.getKey(), entry.getValue());
				}
			}
			
			response = sdnoHealthCheckResources.healthCheck(vnf, requestContext);
		} 
		catch (Exception ex) {			
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex.getMessage());			
		}
		
		if (!response) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "SDNO Health Check failed.");
		}		
	}
}
