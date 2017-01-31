/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package com.att.bpm.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution;

class VnfAdapterUtils {

	private AbstractServiceTaskProcessor taskProcessor

	public VnfAdapterUtils(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
	}

	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void validateVnfResponse(Execution execution, String responseVar, String responseCodeVar, String errorResponseVar) {
		def method = getClass().getSimpleName() + '.validateVnfResponse(' +
			'execution=' + execution.getId() +
			', responseVar=' + responseVar +
			', responseCodeVar=' + responseCodeVar +
			', errorResponseVar=' + errorResponseVar +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def prefix = execution.getVariable('prefix')

			def response = execution.getVariable(responseVar)
			def responseCode = execution.getVariable(responseCodeVar)
			def errorResponse = execution.getVariable(errorResponseVar)

			if (response.contains("WorkflowException")) {
				execution.setVariable(prefix + "ErrorResponse", response)
				//execution.setVariable(prefix + "ResponseCode", responseCode)
				taskProcessor.logDebug(" Sub Vnf flow Error WorkflowException Response - " + "\n" + response, isDebugLogEnabled)
				throw new BpmnError("MSOWorkflowException")
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			taskProcessor.logError('Caught exception in ' + method, e)
			taskProcessor.workflowException(execution, 'Internal Error- Unable to validate VNF Response ' + e.getMessage(), 500)
		}
	}

}
