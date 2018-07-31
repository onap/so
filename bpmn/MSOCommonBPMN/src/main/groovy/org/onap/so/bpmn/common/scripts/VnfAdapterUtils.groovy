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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger



class VnfAdapterUtils {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, VnfAdapterUtils.class);


	private AbstractServiceTaskProcessor taskProcessor

	public VnfAdapterUtils(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
	}

	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void validateVnfResponse(DelegateExecution execution, String responseVar, String responseCodeVar, String errorResponseVar) {
		def method = getClass().getSimpleName() + '.validateVnfResponse(' +
			'execution=' + execution.getId() +
			', responseVar=' + responseVar +
			', responseCodeVar=' + responseCodeVar +
			', errorResponseVar=' + errorResponseVar +
			')'
		msoLogger.trace('Entered ' + method)

		try {
			def prefix = execution.getVariable('prefix')

			def response = execution.getVariable(responseVar)
			def responseCode = execution.getVariable(responseCodeVar)
			def errorResponse = execution.getVariable(errorResponseVar)

			// The following if statement never appears to be true as any VNF Adapter error seems to be stored in 'errorResponse'.
			// Also, the value is stored as a WorkflowException object, not a String. Added the else if to provide the proper
			// functionality but leaving the original code in case it is hit under some circumstances.
			if (response.contains("WorkflowException")) {
				execution.setVariable(prefix + "ErrorResponse", response)
				//execution.setVariable(prefix + "ResponseCode", responseCode)
				msoLogger.debug(" Sub Vnf flow Error WorkflowException Response - " + "\n" + response)
				throw new BpmnError("MSOWorkflowException")
			} else if (errorResponse != null && errorResponse instanceof WorkflowException) {
				// Not sure the variables with the associated prefix are still used
				execution.setVariable(prefix + "ErrorResponse", errorResponse.getErrorMessage())
				execution.setVariable(prefix + "ResponseCode", errorResponse.getErrorCode())
				msoLogger.debug("Sub Vnf flow Error WorkflowException " + prefix + "ErrorResponse" + " - " + errorResponse.getErrorMessage())
				// this is the important part to ensure we hit the Fallout Handler
				throw new BpmnError("MSOWorkflowException")
			} else if (errorResponse != null && errorResponse instanceof WorkflowException) {
				// Not sure the variables with the associated prefix are still used
				execution.setVariable(prefix + "ErrorResponse", errorResponse.getErrorMessage())
				execution.setVariable(prefix + "ResponseCode", errorResponse.getErrorCode())
				msoLogger.debug("Sub Vnf flow Error WorkflowException " + prefix + "ErrorResponse" + " - " + errorResponse.getErrorMessage())
				// this is the important part to ensure we hit the Fallout Handler
				throw new BpmnError("MSOWorkflowException")
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, 'Internal Error- Unable to validate VNF Response ' + e.getMessage())
		}
	}

}
