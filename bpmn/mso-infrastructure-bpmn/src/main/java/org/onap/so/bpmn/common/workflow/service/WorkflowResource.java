/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.onap.so.logger.LoggingAnchor;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.onap.so.bpmn.common.workflow.context.WorkflowResponse;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;

@Path("/workflow")
@OpenAPIDefinition(info = @Info(title = "/workflow", description = "Root of workflow services"))
@Component
public class WorkflowResource extends ProcessEngineAwareService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowResource.class);
    private static final String LOGMARKER = "[WRKFLOW-RESOURCE]";

    private static final int DEFAULT_WAIT_TIME = 30000;

    @Context
    private UriInfo uriInfo = null;

    /**
     * Starts the process instance and responds to client synchronously If the request does not contain
     * mso-service-request-timeout then it waits for the value specified in DEFAULT_WAIT_TIME Note: value specified in
     * mso-service-request-timeout is in seconds During polling time, if there is an exception encountered in the
     * process execution then polling is stopped and the error response is returned to the client
     *
     * @param processKey
     * @param variableMap
     * @return
     */
    @POST
    @Path("/services/{processKey}")
    @Operation(description = "Starts a new process with the appropriate process synchronously")
    @Produces("application/json")
    @Consumes("application/json")
    public Response startProcessInstanceByKey(@PathParam("processKey") String processKey, VariableMapImpl variableMap) {

        Map<String, Object> inputVariables = getInputVariables(variableMap);
        setLogContext(processKey, inputVariables);

        WorkflowResponse workflowResponse = new WorkflowResponse();
        long startTime = System.currentTimeMillis();
        ProcessInstance processInstance = null;

        try {
            // Kickoff the process
            ProcessThread thread = new ProcessThread(inputVariables, processKey);
            thread.start();

            Map<String, Object> responseMap = null;

            // wait for process to be completed
            long waitTime = getWaitTime(inputVariables);
            long now = System.currentTimeMillis();
            long start = now;
            long endTime = start + waitTime;
            long pollingInterval = 500;

            // TEMPORARY LOGIC FOR UNIT TEST REFACTORING
            // If this is a unit test (method is invoked directly), wait a max
            // of 5 seconds after process ended for a result. In production,
            // wait up to 60 seconds.
            long timeToWaitAfterProcessEnded = uriInfo == null ? 5000 : 60000;
            AtomicLong timeProcessEnded = new AtomicLong(0);
            boolean endedWithNoResponse = false;
            logger.debug("{} WorkflowResource.startProcessInstanceByKey using timeout: {}", LOGMARKER, waitTime);
            while (now <= endTime) {
                Thread.sleep(pollingInterval);

                now = System.currentTimeMillis();

                // Increase the polling interval over time

                long elapsed = now - start;

                if (elapsed > 60000) {
                    pollingInterval = 5000;
                } else if (elapsed > 10000) {
                    pollingInterval = 1000;
                }
                Exception exception = thread.getException();
                if (exception != null) {
                    throw new Exception(exception);
                }

                processInstance = thread.getProcessInstance();

                if (processInstance == null) {
                    logger.debug("{} process has not been created yet", LOGMARKER + processKey);
                    continue;
                }

                String processInstanceId = processInstance.getId();
                workflowResponse.setProcessInstanceID(processInstanceId);

                responseMap = getResponseMap(processInstance, processKey, timeProcessEnded);

                if (responseMap == null) {
                    logger.debug("{} has not produced a response yet", LOGMARKER + processKey);

                    if (timeProcessEnded.longValue() != 0) {
                        long elapsedSinceEnded = System.currentTimeMillis() - timeProcessEnded.longValue();

                        if (elapsedSinceEnded > timeToWaitAfterProcessEnded) {
                            endedWithNoResponse = true;
                            break;
                        }
                    }
                } else {
                    processResponseMap(workflowResponse, responseMap);
                    recordEvents(processKey, workflowResponse, startTime);
                    return Response.status(workflowResponse.getMessageCode()).entity(workflowResponse).build();
                }
            }

            // if we dont get response after waiting then send timeout response

            String state;
            String processInstanceId;

            if (processInstance == null) {
                processInstanceId = "N/A";
                state = "NOT STARTED";
            } else {
                processInstanceId = processInstance.getProcessInstanceId();
                state = isProcessEnded(processInstanceId) ? "ENDED" : "NOT ENDED";
            }

            workflowResponse.setMessage("Fail");
            if (endedWithNoResponse) {
                workflowResponse.setResponse("Process ended without producing a response");
            } else {
                workflowResponse.setResponse("Request timed out, process state: " + state);
            }
            workflowResponse.setProcessInstanceID(processInstanceId);
            recordEvents(processKey, workflowResponse, startTime);
            workflowResponse.setMessageCode(500);
            return Response.status(500).entity(workflowResponse).build();
        } catch (Exception ex) {
            logger.debug("{} Exception in startProcessInstance by key", LOGMARKER, ex);
            workflowResponse.setMessage("Fail");
            workflowResponse.setResponse("Error occurred while executing the process: " + ex.getMessage());
            if (processInstance != null)
                workflowResponse.setProcessInstanceID(processInstance.getId());

            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "BPMN",
                    MDC.get(processKey), ErrorCode.UnknownError.getValue(), LOGMARKER + workflowResponse.getMessage()
                            + " for processKey: " + processKey + " with response: " + workflowResponse.getResponse());

            workflowResponse.setMessageCode(500);
            recordEvents(processKey, workflowResponse, startTime);
            return Response.status(500).entity(workflowResponse).build();
        }
    }

    /**
     * Returns the wait time, this is used by the resource on how long it should wait to send a response If none
     * specified DEFAULT_WAIT_TIME is used
     *
     * @param inputVariables
     * @return
     */
    private int getWaitTime(Map<String, Object> inputVariables) {
        String timeout = inputVariables.get("mso-service-request-timeout") == null ? null
                : inputVariables.get("mso-service-request-timeout").toString();

        if (timeout != null) {
            try {
                return Integer.parseInt(timeout) * 1000;
            } catch (NumberFormatException nex) {
                logger.debug("Invalid input for mso-service-request-timeout");
            }
        }
        return DEFAULT_WAIT_TIME;
    }

    private void recordEvents(String processKey, WorkflowResponse response, long startTime) {}

    private void setLogContext(String processKey, Map<String, Object> inputVariables) {}

    private String getValueFromInputVariables(Map<String, Object> inputVariables, String key) {
        Object value = inputVariables.get(key);
        if (value == null) {
            return "N/A";
        } else {
            return value.toString();
        }
    }

    /**
     * Checks to see if the specified process is ended.
     *
     * @param processInstanceId the process instance ID
     * @return true if the process is ended
     */
    private boolean isProcessEnded(String processInstanceId) {
        ProcessEngineServices pes = getProcessEngineServices();
        try {
            return pes.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId)
                    .singleResult() == null ? true : false;
        } catch (Exception e) {
            logger.debug("Exception :", e);
            return true;
        }
    }

    private void processResponseMap(WorkflowResponse workflowResponse, Map<String, Object> responseMap) {
        Object object = responseMap.get("Response");
        String response = object == null ? null : String.valueOf(object);
        if (response == null) {
            object = responseMap.get("WorkflowResponse");
            response = object == null ? null : String.valueOf(object);
        }

        workflowResponse.setResponse(response);

        object = responseMap.get("ResponseCode");
        String responseCode = object == null ? null : String.valueOf(object);

        try {
            workflowResponse.setMessageCode(Integer.parseInt(responseCode));
        } catch (NumberFormatException nex) {
            logger.debug("{} Failed to parse ResponseCode: {}", LOGMARKER, responseCode);
            workflowResponse.setMessageCode(-1);
        }

        Object status = responseMap.get("Status");

        if ("Success".equalsIgnoreCase(String.valueOf(status))) {
            workflowResponse.setMessage("Success");
        } else if ("Fail".equalsIgnoreCase(String.valueOf(status))) {
            workflowResponse.setMessage("Fail");
        } else {
            logger.debug("{} Unrecognized Status: {}", LOGMARKER, responseCode);
            workflowResponse.setMessage("Fail");
        }
    }

    /**
     * @version 1.0 Triggers the workflow in a separate thread
     */
    private class ProcessThread extends Thread {
        private final Map<String, Object> inputVariables;
        private final String processKey;
        private final String businessKey;
        private ProcessInstance processInstance = null;
        private Exception exception = null;

        public ProcessThread(Map<String, Object> inputVariables, String processKey) {
            this.inputVariables = inputVariables;
            this.processKey = processKey;
            this.businessKey = UUID.randomUUID().toString();
        }

        /**
         * If an exception occurs when starting the process instance, it may be obtained by calling this method. Note
         * that exceptions are only recorded while the process is executing in its original thread. Once a process is
         * suspended, exception recording stops.
         *
         * @return the exception, or null if none has occurred
         */
        public Exception getException() {
            return exception;
        }


        public ProcessInstance getProcessInstance() {
            return this.processInstance;
        }

        /**
         * Sets the process instance exception.
         *
         * @param exception the exception
         */
        private void setException(Exception exception) {
            this.exception = exception;
        }

        public void run() {
            setLogContext(processKey, inputVariables);

            try {

                RuntimeService runtimeService = getProcessEngineServices().getRuntimeService();

                // Note that this method doesn't return until the process suspends
                // itself or finishes. We provide a business key so we can identify
                // the process instance immediately.
                processInstance = runtimeService.startProcessInstanceByKey(processKey, inputVariables);

            } catch (Exception e) {
                logger.debug("{} ProcessThread caught an exception executing {}: {}", LOGMARKER, processKey, e);
                setException(e);
            }
        }

    }

    private Map<String, Object> getInputVariables(VariableMapImpl variableMap) {
        VariableMap inputVariables = Variables.createVariables();
        @SuppressWarnings("unchecked")
        Map<String, Object> vMap = (Map<String, Object>) variableMap.get("variables");
        for (String key : vMap.keySet()) { // variabe name vn
            @SuppressWarnings("unchecked")
            Map<String, Object> valueMap = (Map<String, Object>) vMap.get(key); // value, type
            inputVariables.putValueTyped(key,
                    Variables.objectValue(valueMap.get("value")).serializationDataFormat(SerializationDataFormats.JAVA) // tells
                                                                                                                        // the
                                                                                                                        // engine
                                                                                                                        // to
                                                                                                                        // use
                                                                                                                        // java
                                                                                                                        // serialization
                                                                                                                        // for
                                                                                                                        // persisting
                                                                                                                        // the
                                                                                                                        // value
                            .create());
        }
        return inputVariables;
    }

    /**
     * Attempts to get a response map from the specified process instance.
     *
     * @return the response map, or null if it is unavailable
     */
    private Map<String, Object> getResponseMap(ProcessInstance processInstance, String processKey,
            AtomicLong timeProcessEnded) {

        String responseMapVariable = processKey + "ResponseMap";
        String processInstanceId = processInstance.getId();

        // Query the runtime service to see if a response map is ready.

        /*
         * RuntimeService runtimeService = getProcessEngineServices().getRuntimeService(); List<Execution> executions =
         * runtimeService.createExecutionQuery() .processInstanceId(processInstanceId).list();
         *
         * for (Execution execution : executions) {
         *
         * @SuppressWarnings("unchecked") Map<String, Object> responseMap = (Map<String, Object>)
         * getVariableFromExecution(runtimeService, execution.getId(), responseMapVariable);
         *
         * if (responseMap != null) { msoLogger.debug(LOGMARKER + "Obtained " + responseMapVariable + " from process " +
         * processInstanceId + " execution " + execution.getId()); return responseMap; } }
         */
        // Querying history seem to return consistent results compared to querying the runtime service

        boolean alreadyEnded = timeProcessEnded.longValue() != 0;

        if (alreadyEnded || isProcessEnded(processInstance.getId())) {
            if (!alreadyEnded) {
                timeProcessEnded.set(System.currentTimeMillis());
            }

            // Query the history service to see if a response map exists.

            HistoryService historyService = getProcessEngineServices().getHistoryService();
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) getVariableFromHistory(historyService,
                    processInstance.getId(), responseMapVariable);

            if (responseMap != null) {
                logger.debug("{}Obtained {} from process {} history", LOGMARKER, responseMapVariable,
                        processInstanceId);
                return responseMap;
            }

            // Query the history service for old-style response variables.

            String prefix = (String) getVariableFromHistory(historyService, processInstanceId, "prefix");

            if (prefix != null) {

                // Check for 'WorkflowResponse' variable
                Object workflowResponseObject =
                        getVariableFromHistory(historyService, processInstanceId, "WorkflowResponse");
                String workflowResponse =
                        workflowResponseObject == null ? null : String.valueOf(workflowResponseObject);
                logger.debug("{}WorkflowResponse: {}", LOGMARKER, workflowResponse);

                if (workflowResponse != null) {
                    Object responseCodeObject =
                            getVariableFromHistory(historyService, processInstanceId, prefix + "ResponseCode");
                    String responseCode = responseCodeObject == null ? null : String.valueOf(responseCodeObject);
                    logger.debug("{}{}ResponseCode: {}", LOGMARKER, prefix, responseCode);
                    responseMap = new HashMap<>();
                    responseMap.put("WorkflowResponse", workflowResponse);
                    responseMap.put("ResponseCode", responseCode);
                    responseMap.put("Status", "Success");
                    return responseMap;
                }


                // Check for 'WorkflowException' variable
                WorkflowException workflowException = null;
                String workflowExceptionText = null;

                Object workflowExceptionObject =
                        getVariableFromHistory(historyService, processInstanceId, "WorkflowException");
                if (workflowExceptionObject != null) {
                    if (workflowExceptionObject instanceof WorkflowException) {
                        workflowException = (WorkflowException) workflowExceptionObject;
                        workflowExceptionText = workflowException.toString();
                        responseMap = new HashMap<>();
                        responseMap.put("WorkflowException", workflowExceptionText);
                        responseMap.put("ResponseCode", workflowException.getErrorCode());
                        responseMap.put("Status", "Fail");
                        return responseMap;
                    } else if (workflowExceptionObject instanceof String) {
                        Object object =
                                getVariableFromHistory(historyService, processInstanceId, prefix + "ResponseCode");
                        String responseCode = object == null ? null : String.valueOf(object);
                        workflowExceptionText = (String) workflowExceptionObject;
                        responseMap = new HashMap<>();
                        responseMap.put("WorkflowException", workflowExceptionText);
                        responseMap.put("ResponseCode", responseCode);
                        responseMap.put("Status", "Fail");
                        return responseMap;
                    }

                }
                logger.debug("{}WorkflowException: {}", LOGMARKER, workflowExceptionText);

                // BEGIN LEGACY SUPPORT. TODO: REMOVE THIS CODE
                Object object = getVariableFromHistory(historyService, processInstanceId, processKey + "Response");
                String response = object == null ? null : String.valueOf(object);
                logger.debug("{}{}Response: {}", LOGMARKER, processKey, response);

                if (response != null) {
                    object = getVariableFromHistory(historyService, processInstanceId, prefix + "ResponseCode");
                    String responseCode = object == null ? null : String.valueOf(object);
                    logger.debug("{}{}ResponseCode: {}", LOGMARKER, prefix, responseCode);
                    responseMap = new HashMap<>();
                    responseMap.put("Response", response);
                    responseMap.put("ResponseCode", responseCode);
                    responseMap.put("Status", "Success");
                    return responseMap;
                }

                object = getVariableFromHistory(historyService, processInstanceId, prefix + "ErrorResponse");
                String errorResponse = object == null ? null : String.valueOf(object);
                logger.debug("{}{}ErrorResponse: {}", LOGMARKER, prefix, errorResponse);

                if (errorResponse != null) {
                    object = getVariableFromHistory(historyService, processInstanceId, prefix + "ResponseCode");
                    String responseCode = object == null ? null : String.valueOf(object);
                    logger.debug("{}{}ResponseCode: {}", LOGMARKER, prefix, responseCode);
                    responseMap = new HashMap<>();
                    responseMap.put("Response", errorResponse);
                    responseMap.put("ResponseCode", responseCode);
                    responseMap.put("Status", "Fail");
                    return responseMap;
                }
                // END LEGACY SUPPORT. TODO: REMOVE THIS CODE
            }
        }
        return null;
    }

    /**
     * Gets a variable value from the specified execution.
     *
     * @return the variable value, or null if the variable could not be obtained
     */
    private Object getVariableFromExecution(RuntimeService runtimeService, String executionId, String variableName) {
        try {
            return runtimeService.getVariable(executionId, variableName);
        } catch (ProcessEngineException e) {
            // Most likely cause is that the execution no longer exists.
            logger.debug("Error retrieving execution {} variable {}: {}", executionId, variableName, e);
            return null;
        }
    }

    /**
     * Gets a variable value from specified historical process instance.
     *
     * @return the variable value, or null if the variable could not be obtained
     */
    private Object getVariableFromHistory(HistoryService historyService, String processInstanceId,
            String variableName) {
        try {
            HistoricVariableInstance v = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId).variableName(variableName).singleResult();
            return v == null ? null : v.getValue();
        } catch (Exception e) {
            logger.debug("Error retrieving process {} variable {} from history: ", processInstanceId, variableName, e);
            return null;
        }
    }

    @POST
    @Path("/services/{processKey}/{processInstanceId}")
    @Produces("application/json")
    @Consumes("application/json")
    @Operation(description = "Allows for retrieval of the variables for a given process")
    public WorkflowResponse getProcessVariables(@PathParam("processKey") String processKey,
            @PathParam("processInstanceId") String processInstanceId) {
        // TODO filter only set of variables
        WorkflowResponse response = new WorkflowResponse();

        try {
            ProcessEngineServices engine = getProcessEngineServices();
            List<HistoricVariableInstance> variables = engine.getHistoryService().createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId).list();
            Map<String, String> variablesMap = new HashMap<>();
            for (HistoricVariableInstance variableInstance : variables) {
                variablesMap.put(variableInstance.getName(), variableInstance.getValue().toString());
            }

            logger.debug("{}***Received MSO getProcessVariables with processKey:{} and variables: {}", LOGMARKER,
                    processKey, variablesMap.toString());

            response.setVariables(variablesMap);
            response.setMessage("Success");
            response.setResponse("Successfully retrieved the variables");
            response.setProcessInstanceID(processInstanceId);

            logger.debug("{}{} for processKey: {} with response: {}", LOGMARKER, response.getMessage(), processKey,
                    response.getResponse());
        } catch (Exception ex) {
            response.setMessage("Fail");
            response.setResponse("Failed to retrieve the variables," + ex.getMessage());
            response.setProcessInstanceID(processInstanceId);

            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "BPMN",
                    MDC.get(processKey), ErrorCode.UnknownError.getValue(), LOGMARKER + response.getMessage()
                            + " for processKey: " + processKey + " with response: " + response.getResponse());
            logger.debug("Exception :", ex);
        }
        return response;
    }
}
