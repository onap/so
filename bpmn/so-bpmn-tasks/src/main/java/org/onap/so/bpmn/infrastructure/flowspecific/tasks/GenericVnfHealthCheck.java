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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.HashMap;
import java.util.Optional;

import org.onap.appc.client.lcm.model.Action;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericVnfHealthCheck {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, GenericVnfHealthCheck.class);
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private CatalogDbClient catalogDbClient;
	@Autowired
	private ApplicationControllerAction appCClient;
	
	public void setParamsForGenericVnfHealthCheck(BuildingBlockExecution execution) {
		
		GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

		try {
			ControllerSelectionReference controllerSelectionReference;
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			String vnfId = vnf.getVnfId();
			String vnfName = vnf.getVnfName();	
			String vnfType = vnf.getVnfType();
			String oamIpAddress = vnf.getIpv4OamAddress();
			String actionCategory = Action.HealthCheck.toString();
			controllerSelectionReference = catalogDbClient.getControllerSelectionReferenceByVnfTypeAndActionCategory(vnfType, actionCategory);
			String controllerName = controllerSelectionReference.getControllerName();
			
			execution.setVariable("vnfId", vnfId);
			execution.setVariable("vnfName", vnfName);
			execution.setVariable("oamIpAddress", oamIpAddress);
			execution.setVariable("msoRequestId", gBBInput.getRequestContext().getMsoRequestId());
			execution.setVariable("action", actionCategory);
			execution.setVariable("controllerType", controllerName);
			
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void callAppcClient(BuildingBlockExecution execution) {
		
		try {
			Action action = null;
			action = Action.valueOf(execution.getVariable("action"));
			String msoRequestId = execution.getVariable("msoRequestId");
			String vnfId = execution.getVariable("vnfId");
			Optional<String> payload = null;
			if(execution.getVariable("payload") != null){
				String pay = execution.getVariable("payload");
				payload =  Optional.of(pay);
			}
			String controllerType = execution.getVariable("controllerType");
			HashMap<String, String> payloadInfo = new HashMap<String, String>();
			payloadInfo.put("vnfName", execution.getVariable("vnfName"));
			payloadInfo.put("vfModuleId",execution.getVariable("vfModuleId"));
			payloadInfo.put("oamIpAddress",execution.getVariable("oamIpAddress"));
			//PayloadInfo contains extra information that adds on to payload before making request to appc
			appCClient.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);
		
			} catch (Exception ex) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
			}
	}
}
