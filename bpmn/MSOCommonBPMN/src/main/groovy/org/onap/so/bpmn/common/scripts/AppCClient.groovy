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

package org.onap.so.bpmn.common.scripts

import org.onap.so.bpmn.core.UrnPropertiesReader

import java.util.HashMap;
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.hibernate.event.internal.AbstractLockUpgradeEventListener
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.appc.ApplicationControllerOrchestrator
import org.onap.so.client.appc.ApplicationControllerOrchestratorException
import org.onap.appc.client.lcm.model.Action
import org.onap.appc.client.lcm.model.Status
import org.onap.so.bpmn.appc.payload.PayloadClient
import org.onap.so.client.appc.ApplicationControllerAction;
import groovy.transform.TypeChecked;
import java.util.UUID;
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger

/**
 * This groovy class supports the <class>AppCClient.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - mso-request-Id
 * @param - isDebugLogEnabled
 * @param - requestId
 * @param - vnfId
 * @param - action
 * @param - payload
 * 
 * Outputs:
 * @param - errorcode
 * @param - errorText
 * @param - responsePayload
 * @param - healthCheckIndex
 * @param - workstep
 */

public class AppCClient extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, AppCClient.class);

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtils = new JsonUtils()
	def prefix = "UPDVnfI_"
	
    public void preProcessRequest(DelegateExecution execution){

	}
	
	public void runAppcCommand(DelegateExecution execution) {
		msoLogger.trace("Start runCommand ")
		def method = getClass().getSimpleName() + '.runAppcCommand(' +
		'execution=' + execution.getId() +
		')'
		msoLogger.trace('Entered ' + method)
		execution.setVariable("rollbackVnfStop", false)
		execution.setVariable("rollbackVnfLock", false)
		execution.setVariable("rollbackQuiesceTraffic", false)
		String appcCode = "1002"
		String responsePayload = ""
		String appcMessage = ""
		Action action = null
		try {
			action = (Action) execution.getVariable("action")
			String vnfId = execution.getVariable('vnfId')
			String msoRequestId = execution.getVariable('msoRequestId')
			String vnfName = execution.getVariable('vnfName')
			String aicIdentity = execution.getVariable('aicIdentity')
			String vnfHostIpAddress = execution.getVariable('vnfHostIpAddress')
			String vmIdList = execution.getVariable("vmIdList")
			String vserverIdList = execution.getVariable("vserverIdList")
			String identityUrl = execution.getVariable("identityUrl")
			String controllerType = execution.getVariable("controllerType")			
			String vfModuleId = execution.getVariable("vfModuleId")		
			HashMap<String, String> payloadInfo = new HashMap<String, String>();
			payloadInfo.put("vnfName", vnfName)
			payloadInfo.put("aicIdentity", aicIdentity)
			payloadInfo.put("vnfHostIpAddress", vnfHostIpAddress)
			payloadInfo.put("vmIdList", vmIdList)
			payloadInfo.put("vserverIdList", vserverIdList)
			payloadInfo.put("identityUrl", identityUrl)
			payloadInfo.put("vfModuleId",vfModuleId)
			Optional<String> payload
			msoLogger.debug("Running APP-C action: " + action.toString())
			msoLogger.debug("VNFID: " + vnfId)
			execution.setVariable('msoRequestId', msoRequestId)
			execution.setVariable("failedActivity", "APP-C")
			execution.setVariable('workStep', action.toString())
			if(execution.getVariable("payload") != null){
				String pay = execution.getVariable("payload")
				payload =  Optional.of(pay)
			}
			if(action.equals(Action.HealthCheck)){
				String healthCheckIndex = execution.getVariable('healthCheckIndex')
				execution.setVariable('workStep', action.toString() + healthCheckIndex)
				execution.setVariable('healthCheckIndex', healthCheckIndex + 1)
			}
			ApplicationControllerAction client = new ApplicationControllerAction()
			msoLogger.debug("Created Application Controller Action Object")
			//PayloadInfo contains extra information that adds on to payload before making request to appc
			client.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType)
			msoLogger.debug("ran through the main method for Application Contoller")
			appcCode = client.getErrorCode()
			appcMessage = client.getErrorMessage()
		}
		catch (BpmnError e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			appcMessage = e.getMessage()
		} 
		execution.setVariable("errorCode", appcCode)
		if (appcCode == '0' && action != null) {
			if (action.equals(Action.Lock)) {
				execution.setVariable("rollbackVnfLock", true)
			}
			if (action.equals(Action.Unlock)) {
				execution.setVariable("rollbackVnfLock", false)
			}
			if (action.equals(Action.Start)) {
				execution.setVariable("rollbackVnfStop", true)
			}
			if (action.equals(Action.Stop)) {
				execution.setVariable("rollbackVnfStop", false)
			}
			if (action.equals(Action.QuiesceTraffic)) {
				execution.setVariable("rollbackQuiesceTraffic", true)
			}
			if (action.equals(Action.ResumeTraffic)) {
				execution.setVariable("rollbackQuiesceTraffic", false)
			}
		}
		execution.setVariable("errorText", appcMessage)
		execution.setVariable("responsePayload", responsePayload)
		msoLogger.debug("Error Message: " + appcMessage)
		msoLogger.debug("ERROR CODE: " + execution.getVariable("errorCode"))
		msoLogger.trace("End of runCommand ")
	}    
}
