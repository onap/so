package org.openecomp.mso.bpmn.common.scripts

import java.util.HashMap;
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.hibernate.event.internal.AbstractLockUpgradeEventListener
import org.openecomp.mso.bpmn.core.HealthCheckHandler
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.client.appc.ApplicationControllerOrchestrator
import org.openecomp.mso.client.appc.ApplicationControllerOrchestratorException
import org.onap.appc.client.lcm.model.Action
import org.onap.appc.client.lcm.model.Status
import org.openecomp.mso.bpmn.appc.payload.PayloadClient
import org.openecomp.mso.bpmn.common.adapter.vnf.CreateVnfNotification.Outputs
import org.openecomp.mso.client.appc.ApplicationControllerAction;
import groovy.transform.TypeChecked;
import java.util.UUID;

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
 * @param - rollbackVnfStop
 * @param - rollbackVnfLock
 * @param - rollbackQuiesceTraffic
 */

public class AppCClient extends AbstractServiceTaskProcessor{
	
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtils = new JsonUtils()
	def prefix = "UPDVnfI_"
	
	public void preProcessRequest(DelegateExecution execution){

	}
	
	public void runAppcCommand(DelegateExecution execution) {
		String isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		utils.log("DEBUG", "***** Start runCommand *****", isDebugLogEnabled)
		def method = getClass().getSimpleName() + '.runAppcCommand(' +
		'execution=' + execution.getId() +
		')'
		logDebug('Entered ' + method, isDebugLogEnabled)
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
			String identityUrl = execution.getVariable("identityUrl")
			HashMap<String, String> payloadInfo = new HashMap<String, String>();
			payloadInfo.put("vnfName", vnfName)
			payloadInfo.put("aicIdentity", aicIdentity)
			payloadInfo.put("vnfHostIpAddress", vnfHostIpAddress)
			payloadInfo.put("vmIdList", vmIdList)
			payloadInfo.put("identityUrl", identityUrl)
			Optional<String> payload
			logDebug("Running APP-C action: " + action.toString(), isDebugLogEnabled)
			utils.log("DEBUG", "VNFID: " + vnfId, isDebugLogEnabled)
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
			utils.log("DEBUG", "Created Application Controller Action Object", isDebugLogEnabled)
			//PayloadInfo contains extra information that adds on to payload before making request to appc
			client.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo)
			utils.log("DEBUG", "ran through the main method for Application Contoller", isDebugLogEnabled)
			appcCode = client.getErrorCode()
			appcMessage = client.getErrorMessage()
		}
		catch (BpmnError e) {
			logError('Caught exception in ' + method, e)
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
		utils.log("DEBUG", "Error Message: " + appcMessage, isDebugLogEnabled)
		utils.log("DEBUG","ERROR CODE: " + execution.getVariable("errorCode"), isDebugLogEnabled)
		utils.log("DEBUG", "***** End of runCommand *****", isDebugLogEnabled)
	}
}