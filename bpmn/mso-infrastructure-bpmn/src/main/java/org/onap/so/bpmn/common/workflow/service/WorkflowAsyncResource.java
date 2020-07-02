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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.bpmn.common.adapter.sdnc.SDNCAsyncResponse;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.client.HttpClient;
import org.onap.so.client.HttpClientFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;
import org.springframework.web.bind.annotation.RequestBody;
import org.onap.so.bpmn.common.workflow.context.WorkflowContext;
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder;
import org.onap.so.bpmn.common.workflow.context.WorkflowResponse;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


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
@Api(value = "/async", description = "Provides asynchronous starting of a bpmn process")
@Provider
@Component
public class WorkflowAsyncResource extends ProcessEngineAwareService {

    private static final WorkflowContextHolder contextHolder = WorkflowContextHolder.getInstance();

    long workflowPollInterval = 1000;

    @Autowired
    private WorkflowProcessor processor;

    @Autowired
    private WorkflowContextHolder workflowContext;

    public void setProcessor(WorkflowProcessor processor) {
        this.processor = processor;
    }

    protected static final Logger logger = LoggerFactory.getLogger(WorkflowAsyncResource.class);
    protected static final long DEFAULT_WAIT_TIME = 60000; // default wait time

    @POST
    @Consumes("application/json")
    @Path("/services/updateStatus")
    @Produces("application/json")

    public Response processAsyncResponse(@RequestBody SDNCAsyncResponse response) throws Exception {
        logger.debug("SDNC RESPONSE recived: \n" + response.toString());
        if (response != null && response.getResponseCode() != "") {
            try {
                String dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.db.endpoint");
                logger.debug("DB Adapter Endpoint is: " + dbAdapterEndpoint);
                String serviceId = response.getRequestId();
                String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n"
                        + "    		                        xmlns:ns=\"http://org.onap.so/requestsdb\">\r\n"
                        + "    		                        <soapenv:Header/>\r\n"
                        + "    		                        <soapenv:Body>\r\n"
                        + "    		                            <ns:getServiceOperationStatus xmlns:ns=\"http://org.onap.so/requestsdb\">\r\n"
                        + "    									<serviceId>$serviceId</serviceId>\r\n"
                        + "    									<operationId></operationId>\r\n"
                        + "    		                        </ns:getServiceOperationStatus>\r\n"
                        + "    		                    </soapenv:Body>\r\n"
                        + "    		                </soapenv:Envelope>";
                payload = payload.replace("$serviceId", serviceId);
                HttpPost httpPost = new HttpPost(dbAdapterEndpoint);
                httpPost.addHeader("Authorization", "Basic YnBlbDpwYXNzd29yZDEk");
                httpPost.addHeader("Content-type", "application/soap+xml");
                httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));
                String result = httpPostCall(dbAdapterEndpoint, httpPost);
                logger.debug("Result returned from request DB: \n" + result);
                if (validateDBResponse(result)) {
                    if (response.getResponseCode().equals("200")) {
                        logger.debug("Domain service SDNC response is success");
                        updateOperationStatus(result, "SUCCESS", "100");
                    } else {
                        logger.debug("Domain service SDNC response is failed");
                        updateOperationStatus(result, "FAILED", "0");
                    }
                } else {
                    throw new Exception("Result returned from req db is empty or null");
                }
            } catch (Exception e) {
                logger.debug("Exception occured" + e.getMessage());
                throw new Exception("Exception occured at processAsyncResponse due to " + e.getMessage());
            }
        } else {
            return Response.status(500).entity(response).build();
        }
        return Response.status(200).entity(response).build();

    }

    public String getUpdatePayload(String opStatus, String status, String progress) {
        String userId = "";
        String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n"
                + "                        xmlns:ns=\"http://org.onap.so/requestsdb\">\r\n"
                + "                        <soapenv:Header/>\r\n" + "                        <soapenv:Body>\r\n"
                + "                            <ns:updateServiceOperationStatus xmlns:ns=\"http://org.onap.so/requestsdb\">\r\n"
                + "                            <serviceId>$serviceId</serviceId>\r\n"
                + "                            <operationId>$operationId</operationId>\r\n"
                + "                            <operationType>$operationType</operationType>\r\n"
                + "                            <userId>$userId</userId>\r\n"
                + "                            <result>$result</result>\r\n"
                + "                            <operationContent>$operationContent</operationContent>\r\n"
                + "                            <progress>$progress</progress>\r\n"
                + "                            <reason>$reason</reason>\r\n"
                + "                        </ns:updateServiceOperationStatus>\r\n"
                + "                    </soapenv:Body>\r\n" + "                </soapenv:Envelope>";

        payload = payload.replace("$serviceId", getValueByName("serviceId", opStatus));
        payload = payload.replace("$operationId", getValueByName("operationId", opStatus));
        payload = payload.replace("$operationType", getValueByName("operation", opStatus));
        payload = payload.replace("$userId", userId);
        payload = payload.replace("$result", status);
        payload = payload.replace("$progress", progress);
        payload = payload.replace("$reason", "SDNC Asyncresponse Updated as " + status);
        payload = payload.replace("$operationContent", "Recived SDNC Async Response");

        logger.debug("Before format Outgoing ns:updateServiceOperationStatus Payload: \n" + payload);
        return payload;
    }

    public void updateOperationStatus(String opStatus, String status, String progress) {
        try {

            String dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.db.endpoint");
            String payload = getUpdatePayload(opStatus, status, progress);

            HttpPost httpPost = new HttpPost(dbAdapterEndpoint);
            httpPost.addHeader("Authorization", "Basic YnBlbDpwYXNzd29yZDEk");
            httpPost.addHeader("Content-type", "application/soap+xml");
            httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));
            String dbresult = httpPostCall(dbAdapterEndpoint, httpPost);
            logger.debug("Result returned from request DB: \n" + dbresult);

        } catch (Exception e) {
            logger.error("Exception Occured Processing updateOperationStatus. Exception is:\n" + e);
        }
    }

    private boolean validateDBResponse(String xml) {
        if (xml != null && xml != "" && xml.contains("return")) {
            return true;
        }
        return false;
    }

    private String getValueByName(String name, String xml) {
        if (xml != "" && xml.contains(name)) {
            String start = "<" + name + ">";
            String end = "</" + name + ">";
            return xml.substring(xml.indexOf(start), xml.indexOf(end)).replace(start, "");
        }
        return "";
    }

    protected String httpPostCall(String url, HttpPost httpPost) throws Exception {
        String result = null;
        String errorMsg;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpPost);
        try {
            result = EntityUtils.toString(closeableHttpResponse.getEntity());
            logger.debug("result = {}", result);
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                logger.info("exception: fail for status code = {}",
                        closeableHttpResponse.getStatusLine().getStatusCode());
                throw new Exception(result);
            }
            closeableHttpResponse.close();
        } catch (Exception e) {
            closeableHttpResponse.close();
            errorMsg = url + ":httpPostWithJSON connect faild";
            logger.debug("exception: POST_CONNECT_FAILD : {}", errorMsg);
            throw e;
        }
        return result;
    }

    /**
     * Asynchronous JAX-RS method that starts a process instance.
     * 
     * @param processKey the process key
     * @param variableMap input variables to the process
     * @return
     */

    @POST
    @Path("/services/{processKey}")
    @ApiOperation(value = "Starts a new process with the appropriate process Key",
            notes = "Aysnc fall outs are only logged")
    @Produces("application/json")
    @Consumes("application/json")
    public Response startProcessInstanceByKey(@PathParam("processKey") String processKey, VariableMapImpl variableMap) {
        Map<String, Object> inputVariables = getInputVariables(variableMap);
        try {
            MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, getRequestId(inputVariables));
            processor.startProcess(processKey, variableMap);
            WorkflowResponse response = waitForResponse(inputVariables);
            if (response.getMessageCode() == 500) {
                return Response.status(500).entity(response).build();
            } else {
                return Response.status(202).entity(response).build();
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
        long waitTime = getWaitTime(inputVariables);
        logger.debug("WorkflowAsyncResource.waitForResponse using timeout: " + waitTime);
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
    private long getWaitTime(Map<String, Object> inputVariables) {
        String timeout = inputVariables.get("mso-service-request-timeout") == null ? null
                : inputVariables.get("mso-service-request-timeout").toString();

        if (timeout != null) {
            try {
                return Long.parseLong(timeout) * 1000;
            } catch (NumberFormatException nex) {
                logger.debug("Invalid input for mso-service-request-timeout");
            }
        }
        return DEFAULT_WAIT_TIME;
    }

}
