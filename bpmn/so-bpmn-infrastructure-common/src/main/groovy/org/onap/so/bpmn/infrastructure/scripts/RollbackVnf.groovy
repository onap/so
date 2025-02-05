/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.scripts

import org.onap.so.logger.LoggingAnchor
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.appc.client.lcm.model.Action;

import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class RollbackVnf extends VnfCmBase {
    private static final Logger logger = LoggerFactory.getLogger( RollbackVnf.class);

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

		logger.trace('Entered ' + method)

		initProcessVariables(execution)

		try {

			execution.setVariable("rollbackErrorCode", "0")

			if (execution.getVariable("rollbackSetClosedLoopDisabledFlag") == true) {
				logger.debug("Will call setClosedLoopDisabledFlag")
			}


			logger.trace('Exited ' + method)

		}
		catch(Exception e) {
			String restFaultMessage = e.getMessage()
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Encountered - " + "\n" + restFaultMessage, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			execution.setVariable("rollbackErrorCode", "1")
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, restFaultMessage)
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

		logger.trace('Entered ' + method)

		def rollbackErrorCode = execution.getVariable('rollbackErrorCode')
		if (rollbackErrorCode == "0") {
			execution.setVariable('rollbackSuccessful', true)
			logger.debug("rollback successful")
		}
		else {
			execution.setVariable('rollbackSuccessful', false)
			logger.debug("rollback unsuccessful")
		}

		logger.trace('Exited ' + method)

	}

}
