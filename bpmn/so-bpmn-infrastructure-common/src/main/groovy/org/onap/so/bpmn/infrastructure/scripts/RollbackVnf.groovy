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

import groovy.json.JsonOutput

import groovy.json.JsonSlurper
import groovy.util.Node
import groovy.util.XmlParser;
import groovy.xml.QName

import java.io.Serializable;
import java.util.UUID;
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.cmd.AbstractSetVariableCmd
import org.camunda.bpm.engine.delegate.DelegateExecution

import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.onap.so.bpmn.common.scripts.VidUtils;
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.client.aai.*

import org.onap.so.client.appc.ApplicationControllerClient;
import org.onap.so.client.appc.ApplicationControllerSupport;
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.uri.AAIUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.appc.client.lcm.model.Action;

import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
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
			logger.error("{} {} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Encountered - " + "\n" + restFaultMessage, "BPMN",
					MsoLogger.ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
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
