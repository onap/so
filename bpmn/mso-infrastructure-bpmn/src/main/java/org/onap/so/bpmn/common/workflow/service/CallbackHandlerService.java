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

package org.onap.so.bpmn.common.workflow.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.so.logger.LoggingAnchor;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Abstract base class for callback services.
 */
@Service
public class CallbackHandlerService {
    public static final long DEFAULT_TIMEOUT_SECONDS = 60;
    public static final long FAST_POLL_DUR_SECONDS = 5;
    public static final long FAST_POLL_INT_MS = 100;
    public static final long SLOW_POLL_INT_MS = 1000;

    private static final Logger logger = LoggerFactory.getLogger(CallbackHandlerService.class);

    @Autowired
    protected ProcessEngine processEngine;

    @Bean
    public RuntimeService runtimeService() {
        return processEngine.getRuntimeService();
    }

    private RuntimeService runtimeService;

    @Autowired
    public CallbackHandlerService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    /**
     * Parameterized callback handler.
     */
    @Async
    protected CallbackResult handleCallback(String method, Object message, String messageEventName,
            String messageVariable, String correlationVariable, String correlationValue, String logMarker) {

        return handleCallback(method, message, messageEventName, messageVariable, correlationVariable, correlationValue,
                logMarker, null);
    }

    /**
     * Parameterized callback handler.
     */
    protected CallbackResult handleCallback(String method, Object message, String messageEventName,
            String messageVariable, String correlationVariable, String correlationValue, String logMarker,
            Map<String, Object> injectedVariables) {

        long startTime = System.currentTimeMillis();

        logger.debug(logMarker + " " + method + " received message: " + (message == null ? "" : System.lineSeparator())
                + message);

        try {
            Map<String, Object> variables = new HashMap<>();

            if (injectedVariables != null) {
                variables.putAll(injectedVariables);
            }

            variables.put(correlationVariable, correlationValue);
            variables.put(messageVariable, message == null ? null : message.toString());

            boolean ok = correlate(messageEventName, correlationVariable, correlationValue, variables, logMarker);

            if (!ok) {
                String msg = "No process is waiting for " + messageEventName + " with " + correlationVariable + " = '"
                        + correlationValue + "'";
                logCallbackError(method, startTime, msg);
                return new CallbackError(msg);
            }

            logCallbackSuccess(method, startTime);
            return new CallbackSuccess();
        } catch (Exception e) {
            logger.debug("Exception :", e);
            String msg = "Caught " + e.getClass().getSimpleName() + " processing " + messageEventName + " with "
                    + correlationVariable + " = '" + correlationValue + "'";
            logCallbackError(method, startTime, msg);
            return new CallbackError(msg);
        }
    }

    /**
     * Performs message correlation. Waits a limited amount of time for a process to become ready for correlation. The
     * return value indicates whether or not a process was found to receive the message. Due to the synchronous nature
     * of message injection in Camunda, by the time this method returns, one of 3 things will have happened: (1) the
     * process received the message and ended, (2) the process received the message and reached an activity that
     * suspended, or (3) an exception occurred during correlation or while the process was executing. Correlation
     * exceptions are handled differently from process execution exceptions. Correlation exceptions are thrown so the
     * client knows something went wrong with the delivery of the message. Process execution exceptions are logged but
     * not thrown.
     * 
     * @param messageEventName the message event name
     * @param correlationVariable the process variable used as the correlator
     * @param correlationValue the correlation value
     * @param variables variables to inject into the process
     * @param logMarker a marker for debug logging
     * @return true if a process could be found, false if not
     */
    protected boolean correlate(String messageEventName, String correlationVariable, String correlationValue,
            Map<String, Object> variables, String logMarker) {
        try {
            logger.debug(logMarker + " Attempting to find process waiting" + " for " + messageEventName + " with "
                    + correlationVariable + " = '" + correlationValue + "'");



            long timeout = DEFAULT_TIMEOUT_SECONDS;

            // The code is here in case we ever need to change the default.
            String correlationTimemout = UrnPropertiesReader.getVariable("mso.correlation.timeout");
            if (correlationTimemout != null) {
                try {
                    timeout = Long.parseLong(correlationTimemout);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }

            long now = System.currentTimeMillis();
            long fastPollEndTime = now + (FAST_POLL_DUR_SECONDS * 1000);
            long endTime = now + (timeout * 1000);
            long sleep = FAST_POLL_INT_MS;

            List<Execution> waitingProcesses = null;
            Exception queryException = null;
            int queryCount = 0;
            int queryFailCount = 0;

            while (true) {
                try {
                    ++queryCount;
                    waitingProcesses =
                            runtimeService.createExecutionQuery().messageEventSubscriptionName(messageEventName)
                                    .processVariableValueEquals(correlationVariable, correlationValue).list();
                } catch (Exception e) {
                    ++queryFailCount;
                    queryException = e;
                }

                if (waitingProcesses != null && waitingProcesses.size() > 0) {
                    break;
                }

                if (now > endTime - sleep) {
                    break;
                }

                Thread.sleep(sleep);
                now = System.currentTimeMillis();

                if (now > fastPollEndTime) {
                    sleep = SLOW_POLL_INT_MS;
                }
            }

            if (waitingProcesses == null) {
                waitingProcesses = new ArrayList<Execution>(0);
            }

            int count = waitingProcesses.size();

            List<ExecInfo> execInfoList = new ArrayList<>(count);
            for (Execution execution : waitingProcesses) {
                execInfoList.add(new ExecInfo(execution));
            }

            logger.debug(logMarker + " Found " + count + " process(es) waiting" + " for " + messageEventName + " with "
                    + correlationVariable + " = '" + correlationValue + "': " + execInfoList);

            if (count == 0) {
                if (queryFailCount > 0) {
                    String msg =
                            queryFailCount + "/" + queryCount + " execution queries failed attempting to correlate "
                                    + messageEventName + " with " + correlationVariable + " = '" + correlationValue
                                    + "'; last exception was:" + queryException;
                    logger.debug(msg);
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION.toString(), "BPMN",
                            ErrorCode.UnknownError.getValue(), msg, queryException);
                }

                return false;
            }

            if (count > 1) {
                // Only one process should be waiting. Throw an exception back to the client.
                throw new MismatchingMessageCorrelationException(messageEventName,
                        "more than 1 process is waiting with " + correlationVariable + " = '" + correlationValue + "'");
            }

            // We prototyped an asynchronous solution, i.e. resuming the process
            // flow in a separate thread, but this affected too many existing tests,
            // and we went back to the synchronous solution. The synchronous solution
            // has some troublesome characteristics though. For example, the
            // resumed flow may send request #2 to a remote system before MSO has
            // acknowledged the notification associated with request #1.

            try {
                logger.debug(logMarker + " Running " + execInfoList.get(0) + " to receive " + messageEventName
                        + " with " + correlationVariable + " = '" + correlationValue + "'");

                @SuppressWarnings("unused")
                MessageCorrelationResult result = runtimeService.createMessageCorrelation(messageEventName)
                        .setVariables(variables).processInstanceVariableEquals(correlationVariable, correlationValue)
                        .correlateWithResult();

            } catch (MismatchingMessageCorrelationException e) {
                // A correlation exception occurred even after we identified
                // one waiting process. Throw it back to the client.
                throw e;
            } catch (OptimisticLockingException ole) {

                String msg = "Caught " + ole.getClass().getSimpleName() + " after receiving " + messageEventName
                        + " with " + correlationVariable + " = '" + correlationValue + "': " + ole;
                logger.debug(msg);
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION.toString(),
                        "BPMN CORRELATION ERROR -", ErrorCode.UnknownError.getValue(), msg, ole);

                // Retry for OptimisticLocking Exceptions
                int retryCount = 0;
                String retryStr = UrnPropertiesReader.getVariable("mso.bpmn.optimisticlockingexception.retrycount");
                if (retryStr != null) {
                    try {
                        retryCount = Integer.parseInt(retryStr);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }

                logger.debug("Retry correlate for OptimisticLockingException, retryCount:{}", retryCount);

                for (; retryCount > 0; retryCount--) {

                    try {
                        Thread.sleep(SLOW_POLL_INT_MS);

                        @SuppressWarnings("unused")
                        MessageCorrelationResult result =
                                runtimeService.createMessageCorrelation(messageEventName).setVariables(variables)
                                        .processInstanceVariableEquals(correlationVariable, correlationValue)
                                        .correlateWithResult();
                        retryCount = 0;
                        logger.debug("OptimisticLockingException retry was successful, seting retryCount: {}",
                                retryCount);
                    } catch (OptimisticLockingException olex) {
                        // oleFlag = ex instanceof org.camunda.bpm.engine.OptimisticLockingException;
                        String strMsg = "Received exception, OptimisticLockingException retry failed, retryCount:"
                                + retryCount + " | exception returned: " + olex;
                        logger.debug(strMsg);
                        logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION.toString(), "BPMN",
                                ErrorCode.UnknownError.getValue(), strMsg, olex);
                    } catch (Exception excep) {
                        retryCount = 0;
                        // oleFlag = ex instanceof org.camunda.bpm.engine.OptimisticLockingException;
                        String strMsg = "Received exception, OptimisticLockingException retry failed, retryCount:"
                                + retryCount + " | exception returned: " + excep;
                        logger.debug(strMsg);
                        logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION.toString(), "BPMN",
                                ErrorCode.UnknownError.getValue(), strMsg, excep);
                    }

                }

            } catch (Exception e) {
                // This must be an exception from the flow itself. Log it, but don't
                // report it back to the client.
                String msg = "Caught " + e.getClass().getSimpleName() + " running " + execInfoList.get(0)
                        + " after receiving " + messageEventName + " with " + correlationVariable + " = '"
                        + correlationValue + "': " + e;
                logger.debug(msg);
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION.toString(), "BPMN",
                        ErrorCode.UnknownError.getValue(), msg, e);
            }
        } catch (Exception e) {
            // This must be an exception from the flow itself. Log it, but don't
            // report it back to the client.
            String msg = "Caught " + e.getClass().getSimpleName() + " after receiving " + messageEventName + " with "
                    + correlationVariable + " = '" + correlationValue + "': " + e;
            logger.debug(msg);
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION.toString(), "BPMN CORRELATION ERROR -",
                    ErrorCode.UnknownError.getValue(), msg, e);
        }

        return true;
    }

    /**
     * Records audit and metric events in the log for a callback success.
     * 
     * @param method the method name
     * @param startTime the request start time
     */
    protected void logCallbackSuccess(String method, long startTime) {}

    /**
     * Records error, audit and metric events in the log for a callback internal error.
     * 
     * @param method the method name
     * @param startTime the request start time
     * @param msg the error message
     */
    protected void logCallbackError(String method, long startTime, String msg) {
        logCallbackError(method, startTime, msg, null);
    }

    /**
     * Records error, audit and metric events in the log for a callback internal error.
     * 
     * @param method the method name
     * @param startTime the request start time
     * @param msg the error message
     * @param e the exception
     */
    protected void logCallbackError(String method, long startTime, String msg, Exception e) {
        if (e == null) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_CALLBACK_EXCEPTION.toString(), "BPMN",
                    ErrorCode.UnknownError.getValue(), msg);
        } else {
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_CALLBACK_EXCEPTION.toString(), "BPMN",
                    ErrorCode.UnknownError.getValue(), msg, e);
        }
    }

    /**
     * Abstract callback result object.
     */
    protected abstract class CallbackResult {
    }

    /**
     * Indicates that callback handling was successful.
     */
    protected class CallbackSuccess extends CallbackResult {
    }

    /**
     * Indicates that callback handling failed.
     */
    protected class CallbackError extends CallbackResult {
        private final String errorMessage;

        public CallbackError(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        /**
         * Gets the error message.
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }

    private static class ExecInfo {
        private final Execution execution;

        public ExecInfo(Execution execution) {
            this.execution = execution;
        }

        @Override
        public String toString() {
            return "Process[" + execution.getProcessInstanceId() + ":" + execution.getId() + "]";
        }
    }
}
