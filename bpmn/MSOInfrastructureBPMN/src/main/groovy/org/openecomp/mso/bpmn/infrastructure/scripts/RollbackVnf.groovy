/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.bpmn.infrastructure.scripts

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.Node
import groovy.util.XmlParser;
import groovy.xml.QName

import java.io.Serializable;
import java.util.UUID;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.cmd.AbstractSetVariableCmd
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.openecomp.mso.bpmn.common.scripts.VidUtils;
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.client.aai.*

import org.openecomp.mso.client.appc.ApplicationControllerClient;
import org.openecomp.mso.client.appc.ApplicationControllerSupport;
import org.openecomp.mso.client.aai.AAIResourcesClient
import org.openecomp.mso.client.aai.entities.AAIResultWrapper
import org.openecomp.mso.client.aai.entities.uri.AAIUri
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory
import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.ActionIdentifiers;
import org.onap.appc.client.lcm.model.LockInput
import org.onap.appc.client.lcm.model.UnlockInput
import org.onap.appc.client.lcm.model.HealthCheckInput
import org.onap.appc.client.lcm.model.StartInput
import org.onap.appc.client.lcm.model.StopInput
import org.onap.appc.client.lcm.model.Flags
import org.onap.appc.client.lcm.model.Status



public class RollbackVnf extends VnfCmBase {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtils = new JsonUtils()	
	def prefix = "VnfIPU_"

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable('prefix', 'RVnf_')
		
		execution.setVariable('rollbackSuccessful', false)		
		execution.setVariable('currentActivity', 'RVnf')
		execution.setVariable('workStep', null)
		execution.setVariable('failedActivity', null)
		execution.setVariable('errorCode', "0")
		execution.setVariable('actionUnlock', Action.Unlock)	
		execution.setVariable('actionStart', Action.Start)
		execution.setVariable('actionResumeTraffic', Action.ResumeTraffic)
		
	}

	/**
	 * Check for missing elements in the received request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
		'execution=' + execution.getId() +
		')'
		initProcessVariables(execution)
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')		
		logDebug('Entered ' + method, isDebugLogEnabled)

		initProcessVariables(execution)
		
		try {
		
			execution.setVariable("rollbackErrorCode", "0")
			
			if (execution.getVariable("rollbackSetClosedLoopDisabledFlag") == true) {
				logDebug("Will call setClosedLoopDisabledFlag", isDebugLogEnabled)
			}			

		
			logDebug('Exited ' + method, isDebugLogEnabled)

		}
		catch(Exception e) {
			String restFaultMessage = e.getMessage()
			utils.log("ERROR", " Exception Encountered - " + "\n" + restFaultMessage, isDebugLogEnabled)
			execution.setVariable("rollbackErrorCode", "1")
		}	
	}
	
	/**
	 * Determine success of rollback execution.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void setRollbackResult(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.setRollbackResult(' +
		'execution=' + execution.getId() +
		')'
		initProcessVariables(execution)
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		def rollbackErrorCode = execution.getVariable('rollbackErrorCode')
		if (rollbackErrorCode == "0") {
			execution.setVariable('rollbackSuccessful', true)
			logDebug("rollback successful", isDebugLogEnabled)
		}
		else {
			execution.setVariable('rollbackSuccessful', false)
			logDebug("rollback unsuccessful", isDebugLogEnabled)
		}		
		
		logDebug('Exited ' + method, isDebugLogEnabled)	
		
	}	
	
}
