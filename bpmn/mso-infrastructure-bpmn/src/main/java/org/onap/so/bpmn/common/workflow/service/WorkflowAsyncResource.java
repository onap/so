/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2026 Deutsche telekom
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

import java.util.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.bpmn.common.workflow.context.WorkflowContext;
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder;
import org.onap.so.bpmn.common.workflow.context.WorkflowResponse;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;


/**
 * 
 * @version 1.0 Asynchronous Workflow processing using JAX RS RESTeasy implementation Both Synchronous and Asynchronous
 *          BPMN process can benefit from this implementation since the workflow gets executed in the background and the
 *          server thread is freed up, server scales better to process more incoming requests
 * 
 *          Usage: For synchronous process, when you are ready to send the response invoke the callback to write the
 *          response For asynchronous process - the activity may send a acknowledgement response and then proceed
 *          further on executing the process
 */
@Path("/async")
@OpenAPIDefinition(info = @Info(title = "/async", description = "Provides asynchronous starting of a bpmn process"))
@Provider
@Component
public class WorkflowAsyncResource extends ProcessEngineAwareService {

    private static final WorkflowContextHolder contextHolder = WorkflowContextHolder.getInstance();

    long workflowPollInterval = 1000;
    private static final String ASYNC_WAIT_TIME = "mso.workflow.async.waitTime";

    @Autowired
    private WorkflowProcessor processor;

    @Autowired
    private WorkflowContextHolder workflowContext;

    @Autowired
    private Environment env;

    public void setProcessor(WorkflowProcessor processor) {
        this.processor = processor;
    }

    protected static final Logger logger = LoggerFactory.getLogger(WorkflowAsyncResource.class);
    protected static final long DEFAULT_WAIT_TIME = 60000; // default wait time
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Asynchronous JAX-RS method that starts a process instance.
     * 
     * @param processKey the process key
     * @param variableMap input variables to the process
     * @return
     */

    @POST
    @Path("/services/{processKey}")
    @Operation(description = "Starts a new process with the appropriate process Key. Aysnc fall outs are only logged")
    @Produces("application/json")
    @Consumes("application/json")
    public Response startProcessInstanceByKey(@PathParam("processKey") String processKey, VariableMapImpl variableMap) {
        Map<String, Object> inputVariables = getInputVariables(variableMap);
        String requestUrl = (String) inputVariables.get("requestUri");

        boolean pause = requestUrl.endsWith("pause");
        boolean abort = requestUrl.endsWith("abort");
        String resumeProcessInsId = getProcessInstanceId(inputVariables);
        boolean resume = (resumeProcessInsId != null && !resumeProcessInsId.isEmpty());
        try {
            MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, getRequestId(inputVariables));
            if (pause) {
                processor.pauseProcess(processKey);
                WorkflowResponse response = waitForProcResponse(inputVariables, processKey);
                if (response.getMessageCode() == 500) {
                    return Response.status(500).entity(response).build();
                } else {
                    return Response.status(202).entity(response).build();
                }
            } else if (abort) {
                processor.abortProcess(processKey);
                WorkflowResponse response = waitForProcResponse(inputVariables, processKey);
                if (response.getMessageCode() == 500) {
                    return Response.status(500).entity(response).build();
                } else {
                    return Response.status(202).entity(response).build();
                }
            } else if (resume) {
                processor.activateProcess(processKey, resumeProcessInsId);
                WorkflowResponse response = waitForProcResponse(inputVariables, processKey);
                if (response.getMessageCode() == 500) {
                    return Response.status(500).entity(response).build();
                } else {
                    return Response.status(202).entity(response).build();
                }
            } else {
                processor.startProcess(processKey, variableMap);
                WorkflowResponse response = waitForResponse(inputVariables);
                if (response.getMessageCode() == 500) {
                    return Response.status(500).entity(response).build();
                } else {
                    return Response.status(202).entity(response).build();
                }
            }

        } catch (WorkflowProcessorException e) {
            WorkflowResponse response = e.getWorkflowResponse();
            return Response.status(500).entity(response).build();
        } catch (Exception e) {
            WorkflowResponse response = buildUnkownError(getRequestId(inputVariables), e.getMessage());
            return Response.status(500).entity(response).build();
        }
    }

    protected WorkflowResponse waitForResponse(Map<String, Object> inputVariables) throws Exception {
        String requestId = getRequestId(inputVariables);
        long currentWaitTime = 0;
        long waitTime = getWaitTime();
        logger.debug("WorkflowAsyncResource.waitForResponse using timeout: {}", waitTime);
        while (waitTime > currentWaitTime) {
            Thread.sleep(workflowPollInterval);
            currentWaitTime = currentWaitTime + workflowPollInterval;
            WorkflowContext foundContext = contextHolder.getWorkflowContext(requestId);
            if (foundContext != null) {
                contextHolder.remove(foundContext);
                return buildResponse(foundContext);
            }
        }
        throw new Exception("TimeOutOccured in WorkflowAsyncResource.waitForResponse for time " + waitTime + "ms");
    }

    private WorkflowResponse buildUnkownError(String requestId, String error) {
        WorkflowResponse response = new WorkflowResponse();
        response.setMessage(error);
        response.setResponse("UnknownError, request id:" + requestId);
        response.setMessageCode(500);
        return response;
    }

    private WorkflowResponse buildResponse(WorkflowContext foundContext) {
        return foundContext.getWorkflowResponse();
    }

    protected static String getOrCreate(Map<String, Object> inputVariables, String key) {
        String value = Objects.toString(inputVariables.get(key), null);
        if (value == null) {
            value = UUID.randomUUID().toString();
            inputVariables.put(key, value);
        }
        return value;
    }

    protected static String getRequestId(Map<String, Object> inputVariables) {
        return getOrCreate(inputVariables, "mso-request-id");
    }

    protected boolean isProcessEnded(String processInstanceId) {
        ProcessEngineServices pes = getProcessEngineServices();
        return pes.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId)
                .singleResult() == null;
    }

    protected static Map<String, Object> getInputVariables(VariableMapImpl variableMap) {
        Map<String, Object> inputVariables = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> vMap = (Map<String, Object>) variableMap.get("variables");
        for (Map.Entry<String, Object> entry : vMap.entrySet()) {
            String vName = entry.getKey();
            Object value = entry.getValue();
            @SuppressWarnings("unchecked")
            Map<String, Object> valueMap = (Map<String, Object>) value; // value, type
            inputVariables.put(vName, valueMap.get("value"));
        }
        return inputVariables;
    }

    /**
     * Returns the wait time, this is used by the resource on how long it should wait to send a response If none
     * specified DEFAULT_WAIT_TIME is used
     *
     * @param inputVariables
     * @return
     */
    private long getWaitTime() {
        return env.getProperty(ASYNC_WAIT_TIME, Long.class, new Long(DEFAULT_WAIT_TIME));
    }

    protected WorkflowResponse waitForProcResponse(Map<String, Object> inputVariables, String processKey)
            throws Exception {
        String requestId = getRequestId(inputVariables);
        String serviceInstanceId = (String) inputVariables.get("serviceInstanceId");
        long currentWaitTime = 0;
        long waitTime = getWaitTime();
        logger.debug("WorkflowAsyncResource.waitForProcResponse using timeout: " + waitTime);
        while (waitTime > currentWaitTime) {
            Thread.sleep(workflowPollInterval);
            currentWaitTime = currentWaitTime + workflowPollInterval;
            ServiceInstancesResponse serviceInstancesResponse = new ServiceInstancesResponse();
            RequestReferences requestRef = new RequestReferences();
            requestRef.setInstanceId(serviceInstanceId);
            requestRef.setRequestId(requestId);
            serviceInstancesResponse.setRequestReferences(requestRef);
            String response = "";
            try {
                response = mapper.writeValueAsString(serviceInstancesResponse);
            } catch (JsonProcessingException e) {
                throw new Exception(
                        "Could not marshall ServiceInstancesRequest to Json string to respond to API Handler.", e);
            }
            WorkflowResponse workflowResponse = new WorkflowResponse();
            workflowResponse.setResponse(response);
            workflowResponse.setMessageCode(200);
            workflowResponse.setMessage("Success");
            WorkflowContext foundContext =
                    new WorkflowContext(processKey, requestId, workflowPollInterval, workflowResponse);
            if (foundContext != null) {
                return buildResponse(foundContext);
            }
        }
        throw new Exception("TimeOutOccured in WorkflowAsyncResource.waitForResponse for time " + waitTime + "ms");
    }

    protected String getProcessInstanceId(Map<String, Object> inputVariable) {
        String processInsId = null;
        String bpmnRequest = (String) inputVariable.get("bpmnRequest");
        try {
            ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);
            List<Map<String, Object>> userParams = sIRequest.getRequestDetails().getRequestParameters().getUserParams();
            for (Map<String, Object> param : userParams) {
                if (param.containsKey("processInstanceId")) {
                    processInsId = (String) param.get("processInstanceId");
                    logger.info("***processInstanceId*** {}", processInsId);
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return processInsId;
    }
}
