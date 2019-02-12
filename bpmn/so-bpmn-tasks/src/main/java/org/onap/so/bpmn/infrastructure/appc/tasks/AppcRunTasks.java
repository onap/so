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

package org.onap.so.bpmn.infrastructure.appc.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppcRunTasks {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, AppcRunTasks.class);
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private CatalogDbClient catalogDbClient;
	@Autowired
	private ApplicationControllerAction appCClient;
	
	public void preProcessActivity(BuildingBlockExecution execution) {
		execution.setVariable("actionSnapshot", Action.Snapshot);
		execution.setVariable("actionLock", Action.Lock);
		execution.setVariable("actionUnlock", Action.Unlock);
		execution.setVariable("actionUpgradePreCheck", Action.UpgradePreCheck);
		execution.setVariable("actionUpgradePostCheck", Action.UpgradePostCheck);
		execution.setVariable("actionQuiesceTraffic", Action.QuiesceTraffic);
		execution.setVariable("actionUpgradeBackup", Action.UpgradeBackup);
		execution.setVariable("actionUpgradeSoftware", Action.UpgradeSoftware);
		execution.setVariable("actionResumeTraffic", Action.ResumeTraffic);		
		execution.setVariable("actionStop", Action.Stop);
		execution.setVariable("actionStart", Action.Start);
		execution.setVariable("rollbackVnfStop", false);
		execution.setVariable("rollbackVnfLock", false);
		execution.setVariable("rollbackQuiesceTraffic", false);
	}
	
	public void runAppcCommand(BuildingBlockExecution execution, Action action) {
		msoLogger.trace("Start runAppcCommand ");
		String appcCode = "1002";
		String appcMessage = "";
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			GenericVnf vnf = null;
			try {
				vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			} catch (BBObjectNotFoundException e) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "No valid VNF exists");
			}
			String vnfId = null;
			if(null!=vnf){
				vnfId= vnf.getVnfId();
			}
			String msoRequestId = gBBInput.getRequestContext().getMsoRequestId();
			String vnfName = vnf.getVnfName();
			String vnfType = vnf.getVnfType();
			
			String aicIdentity = execution.getVariable("aicIdentity");
			String vnfHostIpAddress =  vnf.getIpv4OamAddress();
			String vmIdList = execution.getVariable("vmIdList");
			String vserverIdList = execution.getVariable("vserverIdList");
			String identityUrl =  execution.getVariable("identityUrl");
			
			ControllerSelectionReference controllerSelectionReference = catalogDbClient.getControllerSelectionReferenceByVnfTypeAndActionCategory(vnfType, action.toString());
			String controllerType = controllerSelectionReference.getControllerName();
			
			String vfModuleId = null;
			VfModule vfModule = null;
			try {
				vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
			} catch (BBObjectNotFoundException e) {
			}
			if (vfModule != null) {
				vfModuleId = vfModule.getVfModuleId();
			}
			
			HashMap<String, String> payloadInfo = buildPayloadInfo(vnfName, aicIdentity, vnfHostIpAddress, vmIdList, vserverIdList,
					identityUrl, vfModuleId);
			Optional<String> payload = null;
			RequestParameters requestParameters = gBBInput.getRequestContext().getRequestParameters();
			if(requestParameters != null){
				String pay = requestParameters.getPayload();
				if (pay != null) {
					payload =  Optional.of(pay);
				}
			}			
			msoLogger.debug("Running APP-C action: " + action.toString());
			msoLogger.debug("VNFID: " + vnfId);	
			appCClient.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);
			appcCode = appCClient.getErrorCode();
			appcMessage = appCClient.getErrorMessage();
			mapRollbackVariables(execution, action, appcCode);
		}
		catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "Caught exception in runAppcCommand", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "APPC Error", e);
			appcMessage = e.getMessage();
		}		
		
		msoLogger.error("Error Message: " + appcMessage);
		msoLogger.error("ERROR CODE: " + appcCode);
		msoLogger.trace("End of runAppCommand ");
		if (appcCode != null && !appcCode.equals("0")) {
			exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(appcCode), appcMessage);
		}
	}
	
	protected void mapRollbackVariables(BuildingBlockExecution execution, Action action, String appcCode) {
		if (appcCode.equals("0") && action != null) {
			if (action.equals(Action.Lock)) {
				execution.setVariable("rollbackVnfLock", true);
			} else if (action.equals(Action.Unlock)) {
				execution.setVariable("rollbackVnfLock", false);
			} else if (action.equals(Action.Start)) {
				execution.setVariable("rollbackVnfStop", false);
			} else if (action.equals(Action.Stop)) {
				execution.setVariable("rollbackVnfStop", true);
			} else if (action.equals(Action.QuiesceTraffic)) {
				execution.setVariable("rollbackQuiesceTraffic", true);
			} else if (action.equals(Action.ResumeTraffic)) {
				execution.setVariable("rollbackQuiesceTraffic", false);
			}
		}
	}
	
	private HashMap<String,String> buildPayloadInfo(String vnfName, String aicIdentity, String vnfHostIpAddress, 
			String vmIdList, String vserverIdList, String identityUrl, String vfModuleId) {
		HashMap<String, String> payloadInfo = new HashMap<String, String>();
		payloadInfo.put("vnfName", vnfName);
		payloadInfo.put("aicIdentity", aicIdentity);
		payloadInfo.put("vnfHostIpAddress", vnfHostIpAddress);
		payloadInfo.put("vmIdList", vmIdList);
		payloadInfo.put("vserverIdList", vserverIdList);
		payloadInfo.put("identityUrl", identityUrl);
		payloadInfo.put("vfModuleId",vfModuleId);
		return payloadInfo;
	}
}
