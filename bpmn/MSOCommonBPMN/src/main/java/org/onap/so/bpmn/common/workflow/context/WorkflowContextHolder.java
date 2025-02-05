package org.onap.so.bpmn.common.workflow.context;
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



import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Workflow Context Holder instance which can be accessed elsewhere either in groovy scripts or Java
 * 
 * @version 1.0
 *
 */

@Component
public class WorkflowContextHolder {

    private static Logger logger = LoggerFactory.getLogger(WorkflowContextHolder.class);
    private static final String logMarker = "[WORKFLOW-CONTEXT-HOLDER]";
    private static WorkflowContextHolder instance = null;


    private long defaultContextTimeout = 60000;

    /**
     * Delay Queue which holds workflow context holder objects
     */
    private final DelayQueue<WorkflowContext> responseQueue = new DelayQueue<>();
    private final TimeoutThread timeoutThread = new TimeoutThread();

    private WorkflowContextHolder() {
        timeoutThread.start();
    }

    /**
     * Singleton holder which eliminates hot lock Since the JVM synchronizes static method there is no synchronization
     * needed for this method
     * 
     * @return
     */
    public static synchronized WorkflowContextHolder getInstance() {
        if (instance == null) {
            instance = new WorkflowContextHolder();
        }
        return instance;
    }

    public void put(WorkflowContext context) {
        logger.debug("{} Adding context to the queue: {}", logMarker, context.getRequestId());
        responseQueue.put(context);
    }

    public void remove(WorkflowContext context) {
        logger.debug("{} Removing context from the queue: {}", logMarker, context.getRequestId());
        responseQueue.remove(context);
    }

    public WorkflowContext getWorkflowContext(String requestId) {
        // Note: DelayQueue interator is threadsafe
        for (WorkflowContext context : responseQueue) {
            if (requestId.equals(context.getRequestId())) {
                return context;
            }
        }
        return null;
    }

    /**
     * Builds the callback response object to respond to client
     * 
     * @param processKey
     * @param processInstanceId
     * @param requestId
     * @param callbackResponse
     * @return
     */
    public void processCallback(String processKey, String processInstanceId, String requestId,
            WorkflowCallbackResponse callbackResponse) {
        WorkflowResponse workflowResponse = new WorkflowResponse();
        workflowResponse.setResponse(callbackResponse.getResponse());
        workflowResponse.setProcessInstanceID(processInstanceId);
        workflowResponse.setMessageCode(callbackResponse.getStatusCode());
        workflowResponse.setMessage(callbackResponse.getMessage());
        WorkflowContext context = new WorkflowContext(processKey, requestId, defaultContextTimeout, workflowResponse);
        put(context);
    }


    /**
     * Timeout thread which monitors the delay queue for expired context and send timeout response to client
     *
     */
    private class TimeoutThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    WorkflowContext requestObject = responseQueue.take();
                    logger.debug("Time remaining for request id: {}:{}", requestObject.getRequestId(),
                            requestObject.getDelay(TimeUnit.MILLISECONDS));
                    logger.debug("Preparing timeout response for {}:{}", requestObject.getProcessKey(),
                            requestObject.getRequestId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.debug("WorkflowContextHolder timeout thread caught exception: ", e);
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION.toString(), "BPMN",
                            ErrorCode.UnknownError.getValue(), "Error in WorkflowContextHolder timeout thread");
                }
            }
            logger.debug("WorkflowContextHolder timeout thread interrupted, quitting");
        }
    }
}
