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
package org.openecomp.mso.bpmn.infrastructure.flowspecific.tasks;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.AAINetworkResources;
import org.openecomp.mso.client.orchestration.SDNCNetworkResources;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignNetworkBBUtils {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL,
			AssignNetworkBBUtils.class);
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private AAINetworkResources aaiNetworkResources;
	@Autowired
	private SDNCNetworkResources sdncNetworkResources;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;

	/**
	 * BPMN access method. Verify if network name was provided as input to BB
	 * @param execution
	 * @return
	 */
	public boolean networkFoundByName(BuildingBlockExecution execution) throws Exception {
		boolean found = false;
		//TODO - populate logic after iTrack MSO-2143 implemented
		return found;
	}
	
	/**
	 * BPMN access method to extract Cloud Region data
	 * @param execution
	 * @throws Exception
	 */
	public void getCloudRegion(BuildingBlockExecution execution) {
		try{
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			CloudRegion cloudRegion = gBBInput.getCloudRegion();
			String cloudRegionSdnc;
			String cloudRegionPo = cloudRegion.getLcpCloudRegionId();
			if (cloudRegion.getCloudRegionVersion().equalsIgnoreCase("2.5")){
				cloudRegionSdnc = "AAIAIC25";
			} else {
				cloudRegionSdnc = cloudRegionPo;
			}
			execution.setVariable("cloudRegionPo", cloudRegionPo);
			execution.setVariable("cloudRegionSdnc", cloudRegionSdnc);
		}catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	/**
	 * BPMN access method. Process silent success scenario
	 * @param execution
	 */
	public void processSilentSuccess(BuildingBlockExecution execution) {
			String msg = "Silent success processing network assign";
		    msoLogger.info(msg);
	}
	
	/**
	 * BPMN access method. Process silent success scenario
	 * @param execution
	 */
	public void failOrchestrationStatus(BuildingBlockExecution execution) {
		BpmnError error = new BpmnError("Failed orchestration status verificaiton");
		exceptionUtil.buildAndThrowWorkflowException(execution, 7000, error);
	}
}
