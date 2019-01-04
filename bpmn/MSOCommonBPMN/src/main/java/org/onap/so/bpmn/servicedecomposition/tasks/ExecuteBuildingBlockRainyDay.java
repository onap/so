/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.servicedecomposition.tasks;

import java.util.Map;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ExecuteBuildingBlockRainyDay {
	
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, ExecuteBuildingBlockRainyDay.class);
	public static final String HANDLING_CODE = "handlingCode";
	
	@Autowired
	private CatalogDbClient catalogDbClient;
	@Autowired
	private RequestsDbClient requestDbclient;
	private static final String ASTERISK = "*";
	
	@Autowired
    private Environment environment;
	protected String retryDurationPath = "mso.rainyDay.retryDurationMultiplier";

	public void setRetryTimer(DelegateExecution execution) {
		try {
			int retryDurationMult = Integer.parseInt(this.environment.getProperty(retryDurationPath));
			int retryCount = (int) execution.getVariable("retryCount");
			int retryTimeToWait = (int) Math.pow(retryDurationMult, retryCount) * 10;
			String RetryDuration = "PT" + retryTimeToWait + "S";
			execution.setVariable("RetryDuration", RetryDuration);
		} catch (Exception e) {
			msoLogger.error(e);
			throw new BpmnError("Unknown error incrementing retry counter");
		}
	}
	
	public void queryRainyDayTable(DelegateExecution execution, boolean primaryPolicy) {
		try {
			ExecuteBuildingBlock ebb = (ExecuteBuildingBlock) execution.getVariable("buildingBlock");
			String bbName = ebb.getBuildingBlock().getBpmnFlowName();
			GeneralBuildingBlock gBBInput = (GeneralBuildingBlock) execution.getVariable("gBBInput");
			String requestId = (String) execution.getVariable("mso-request-id");
			Map<ResourceKey, String> lookupKeyMap = (Map<ResourceKey, String>) execution.getVariable("lookupKeyMap");
			String serviceType = ASTERISK;
			boolean aLaCarte = (boolean) execution.getVariable("aLaCarte");
			boolean suppressRollback = (boolean) execution.getVariable("suppressRollback");
			String handlingCode = "";
			if(suppressRollback){
				handlingCode = "Abort";
			}else{
				try {
					serviceType = gBBInput.getCustomer().getServiceSubscription().getServiceInstances().get(0).getModelInfoServiceInstance().getServiceType();
				} catch (Exception ex) {
					// keep default serviceType value
				}
				String vnfType = ASTERISK;
				try {
					for(GenericVnf vnf : gBBInput.getCustomer().getServiceSubscription().getServiceInstances().get(0).getVnfs()) {
						if(vnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
							vnfType = vnf.getVnfType();
						}
					}
				} catch (Exception ex) {
					// keep default vnfType value
				}
				WorkflowException workflowException = (WorkflowException) execution.getVariable("WorkflowException");
				String errorCode = ASTERISK;
				try {
					errorCode = "" + workflowException.getErrorCode();
				} catch (Exception ex) {
					// keep default errorCode value
				}
				String workStep = ASTERISK;
				try {
					workStep = workflowException.getWorkStep();
				} catch (Exception ex) {
					// keep default workStep value
				}
				//Extract error data to be returned to WorkflowAction
				execution.setVariable("WorkflowExceptionErrorMessage", workflowException.getErrorMessage());
				RainyDayHandlerStatus rainyDayHandlerStatus;
				rainyDayHandlerStatus = catalogDbClient.getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(bbName,serviceType,vnfType,errorCode,workStep);
				if(rainyDayHandlerStatus==null){
					rainyDayHandlerStatus = catalogDbClient.getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(bbName,ASTERISK,ASTERISK,ASTERISK,ASTERISK);
					if(rainyDayHandlerStatus==null){
						handlingCode = "Abort";
					}else{
						if(primaryPolicy){
							handlingCode = rainyDayHandlerStatus.getPolicy();
						}else{
							handlingCode = rainyDayHandlerStatus.getSecondaryPolicy();
						}
					}
				}else{
					if(primaryPolicy){
						handlingCode = rainyDayHandlerStatus.getPolicy();
					}else{
						handlingCode = rainyDayHandlerStatus.getSecondaryPolicy();
					}
				}
				if(!primaryPolicy){
					try{
						InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
						request.setRetryStatusMessage("Retries have been exhausted.");
						requestDbclient.updateInfraActiveRequests(request);
					} catch(Exception ex){
						msoLogger.debug(ex.toString());
						msoLogger.error("Failed to update Request Db Infra Active Requests with Retry Status");
					}
				}
				if(handlingCode.equals("RollbackToAssigned")&&!aLaCarte){
					handlingCode = "Rollback";
				}
			}
			msoLogger.debug("RainyDayHandler Status Code is: " + handlingCode);
			execution.setVariable(HANDLING_CODE, handlingCode);
		} catch (Exception e) {
			msoLogger.error("Failed to determine RainyDayHandler Status. Seting handlingCode = Abort");
			execution.setVariable(HANDLING_CODE, "Abort");
		}
	}
	
	public void setHandlingStatusSuccess(DelegateExecution execution){
		execution.setVariable(HANDLING_CODE, "Success");
	}
}
