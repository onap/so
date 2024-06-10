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

import org.onap.so.logger.LoggingAnchor;
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory




class ReceiveWorkflowMessage extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( ReceiveWorkflowMessage.class);


	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * Process the incoming variables.
	 *
	 * @param execution The flow's execution instance.
	 */
public void preProcessRequest (DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		def prefix="RCVWFMSG_"
		execution.setVariable("prefix", prefix)
		setSuccessIndicator(execution, false)

		try {

			// Confirm that timeout value has been provided in 'RCVWFMSG_timeout'.
			def timeout = execution.getVariable('RCVWFMSG_timeout')
			logger.debug('Timeout value is \'' + timeout + '\'')
			if ((timeout == null) || (timeout.isEmpty())) {
				String msg = getProcessKey(execution) + ': Missing or empty input variable \'RCVWFMSG_timeout\''
				logger.debug(msg)
				logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
						ErrorCode.UnknownError.getValue());
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			// Confirm that message type has been provided in 'RCVWFMSG_messageType'
			def messageType = execution.getVariable('RCVWFMSG_messageType')
			logger.debug('Message type is \'' + messageType + '\'')
			if ((messageType == null) || (messageType.isEmpty())) {
				String msg = getProcessKey(execution) + ': Missing or empty input variable \'RCVWFMSG_messageType\''
				logger.debug(msg)
				logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
						ErrorCode.UnknownError.getValue());
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			// Confirm that correlator value has been provided in 'RCVWFMSG_correlator'
			def correlator = execution.getVariable('RCVWFMSG_correlator')
			logger.debug('Correlator value is \'' + correlator + '\'')
			if ((correlator == null) || (correlator.isEmpty())) {
				String msg = getProcessKey(execution) + ': Missing or empty input variable \'RCVWFMSG_correlator\''
				logger.debug(msg)
				logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
						ErrorCode.UnknownError.getValue());
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}
			execution.setVariable(messageType + '_CORRELATOR', correlator)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			String msg = 'Caught exception in ' + method + ": " + e
			logger.debug(msg)
			logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
					ErrorCode.UnknownError.getValue());
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
		}
	}

	/**
	 * Process a received message.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void processReceivedMessage(DelegateExecution execution){
		def method = getClass().getSimpleName() + '.processReceivedMessage(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		String messageType = null;
		String receivedMessage = null;

		try {
			messageType = execution.getVariable('RCVWFMSG_messageType')
			receivedMessage = execution.getVariable(messageType + '_MESSAGE')
			logger.debug(getProcessKey(execution) + ": received message:\n" + receivedMessage)

			// The received message is made available to the calling flow in WorkflowResponse
			execution.setVariable("WorkflowResponse", receivedMessage)

			setSuccessIndicator(execution, true)

			logger.trace('Exited ' + method)
		} catch (Exception e) {
			receivedMessage = receivedMessage == null || String.valueOf(receivedMessage).isEmpty() ? "NONE" : receivedMessage
			String msg = "Error processing received workflow message: " + receivedMessage
			logger.debug(getProcessKey(execution) + ': ' + msg)
			exceptionUtil.buildWorkflowException(execution, 7020, msg)
		}
	}
}
