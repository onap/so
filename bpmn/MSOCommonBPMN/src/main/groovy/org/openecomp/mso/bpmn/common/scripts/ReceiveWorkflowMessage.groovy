package org.openecomp.mso.bpmn.common.scripts;

import groovy.json.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil


class ReceiveWorkflowMessage extends AbstractServiceTaskProcessor {

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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		def prefix="RCVWFMSG_"
		execution.setVariable("prefix", prefix)
		setSuccessIndicator(execution, false)

		try {

			// Confirm that timeout value has been provided in 'RCVWFMSG_timeout'.
			def timeout = execution.getVariable('RCVWFMSG_timeout')
			logDebug('Timeout value is \'' + timeout + '\'', isDebugLogEnabled)
			if ((timeout == null) || (timeout.isEmpty())) {
				String msg = getProcessKey(execution) + ': Missing or empty input variable \'RCVWFMSG_timeout\''
				logDebug(msg, isDebugLogEnabled)
				logError(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			// Confirm that message type has been provided in 'RCVWFMSG_messageType'
			def messageType = execution.getVariable('RCVWFMSG_messageType')
			logDebug('Message type is \'' + messageType + '\'', isDebugLogEnabled)
			if ((messageType == null) || (messageType.isEmpty())) {
				String msg = getProcessKey(execution) + ': Missing or empty input variable \'RCVWFMSG_messageType\''
				logDebug(msg, isDebugLogEnabled)
				logError(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			// Confirm that correlator value has been provided in 'RCVWFMSG_correlator'
			def correlator = execution.getVariable('RCVWFMSG_correlator')
			logDebug('Correlator value is \'' + correlator + '\'', isDebugLogEnabled)
			if ((correlator == null) || (correlator.isEmpty())) {
				String msg = getProcessKey(execution) + ': Missing or empty input variable \'RCVWFMSG_correlator\''
				logDebug(msg, isDebugLogEnabled)
				logError(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}
			execution.setVariable(messageType + '_CORRELATOR', correlator)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			String msg = 'Caught exception in ' + method + ": " + e
			logDebug(msg, isDebugLogEnabled)
			logError(msg)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		String messageType = null;
		String receivedMessage = null;

		try {
			messageType = execution.getVariable('RCVWFMSG_messageType')
			receivedMessage = execution.getVariable(messageType + '_MESSAGE')
			logDebug(getProcessKey(execution) + ": received message:\n" + receivedMessage, isDebugLogEnabled)

			// The received message is made available to the calling flow in WorkflowResponse
			execution.setVariable("WorkflowResponse", receivedMessage)

			setSuccessIndicator(execution, true)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (Exception e) {
			receivedMessage = receivedMessage == null || String.valueOf(receivedMessage).isEmpty() ? "NONE" : receivedMessage
			String msg = "Error processing received workflow message: " + receivedMessage
			logDebug(getProcessKey(execution) + ': ' + msg, isDebugLogEnabled)
			exceptionUtil.buildWorkflowException(execution, 7020, msg)
		}
	}
}
