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

package org.onap.so.apihandlerinfra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.apihandler.common.CamundaClient;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandler.common.ResponseHandler;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tasksbeans.TaskList;
import org.onap.so.apihandlerinfra.tasksbeans.TaskVariableValue;
import org.onap.so.apihandlerinfra.tasksbeans.TaskVariables;
import org.onap.so.apihandlerinfra.tasksbeans.TasksGetResponse;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Path("onap/so/infra/tasks")
@OpenAPIDefinition(info = @Info(title = "onap/so/infra/tasks", description = "Queries of Manual Tasks"))
@Component
public class TasksHandler {


    private static Logger logger = LoggerFactory.getLogger(TasksHandler.class);

    @Value("${mso.camunda.rest.task.uri}")
    private String requestUrl;

    @Autowired
    private ResponseBuilder builder;

    @Autowired
    private CamundaClient camundaClient;

    @Autowired
    private ResponseHandler responseHandler;

    @Path("/{version:[vV]1}")
    @GET
    @Operation(description = "Finds Manual Tasks", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response queryFilters(@QueryParam("taskId") String taskId,
            @QueryParam("originalRequestId") String originalRequestId,
            @QueryParam("subscriptionServiceType") String subscriptionServiceType, @QueryParam("nfRole") String nfRole,
            @QueryParam("buildingBlockName") String buildingBlockName,
            @QueryParam("originalRequestDate") String originalRequestDate,
            @QueryParam("originalRequestorId") String originalRequestorId, @PathParam("version") String version)
            throws ApiException {

        String apiVersion = version.substring(1);

        // Prepare the query string to /task interface
        TaskVariables tv = new TaskVariables();

        List<TaskVariableValue> tvvList = new ArrayList<>();

        if (originalRequestId != null) {
            TaskVariableValue tvv = new TaskVariableValue();
            tvv.setName("originalRequestId");
            tvv.setValue(originalRequestId);
            tvv.setOperator("eq");
            tvvList.add(tvv);
        }
        if (subscriptionServiceType != null) {
            TaskVariableValue tvv = new TaskVariableValue();
            tvv.setName("subscriptionServiceType");
            tvv.setValue(subscriptionServiceType);
            tvv.setOperator("eq");
            tvvList.add(tvv);
        }
        if (nfRole != null) {
            TaskVariableValue tvv = new TaskVariableValue();
            tvv.setName("nfRole");
            tvv.setValue(nfRole);
            tvv.setOperator("eq");
            tvvList.add(tvv);
        }
        if (buildingBlockName != null) {
            TaskVariableValue tvv = new TaskVariableValue();
            tvv.setName("buildingBlockName");
            tvv.setValue(buildingBlockName);
            tvv.setOperator("eq");
            tvvList.add(tvv);
        }
        if (originalRequestDate != null) {
            TaskVariableValue tvv = new TaskVariableValue();
            tvv.setName("originalRequestDate");
            tvv.setValue(originalRequestDate);
            tvv.setOperator("eq");
            tvvList.add(tvv);
        }
        if (originalRequestorId != null) {
            TaskVariableValue tvv = new TaskVariableValue();
            tvv.setName("originalRequestorId");
            tvv.setValue(originalRequestorId);
            tvv.setOperator("eq");
            tvvList.add(tvv);
        }

        tv.setTaskVariables(tvvList);
        ResponseEntity<String> response = null;
        String camundaJsonReq = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            camundaJsonReq = mapper.writeValueAsString(tv);
        } catch (JsonProcessingException e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .build();
            throw new ValidateException.Builder("Mapping of request to JSON object failed : " + e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo)
                            .build();
        }

        response = camundaClient.post(camundaJsonReq, requestUrl);
        TasksGetResponse trr = new TasksGetResponse();
        List<TaskList> taskList = new ArrayList<>();

        int bpelStatus = responseHandler.setStatus(response.getStatusCodeValue());
        String respBody = response.getBody();
        responseHandler.acceptedOrNoContentResponse(response);
        if (null != respBody) {

            JSONArray data = new JSONArray(respBody);

            for (int i = 0; i < data.length(); i++) {
                JSONObject taskEntry = data.getJSONObject(i);
                String id = taskEntry.getString("id");
                if (taskId != null && !taskId.equals(id)) {
                    continue;
                }
                // Get variables info for each task ID
                TaskList taskListEntry = null;
                taskListEntry = getTaskInfo(id);

                taskList.add(taskListEntry);

            }
            trr.setTaskList(taskList);

        } else {

            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.BusinessProcessError)
                            .build();
            throw new BPMNFailureException.Builder(String.valueOf(bpelStatus), bpelStatus,
                    ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();
        }

        String jsonResponse = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonResponse = mapper.writeValueAsString(trr);
        } catch (JsonProcessingException e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .build();
            throw new ValidateException.Builder("Mapping of request to JSON object failed : " + e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo)
                            .build();

        }

        return builder.buildResponse(HttpStatus.SC_ACCEPTED, "", jsonResponse, apiVersion);
    }

    // Makes a GET call to Camunda to get variables for this task
    private TaskList getTaskInfo(String taskId) throws ApiException {
        TaskList taskList;
        String getRequestUrl = UriBuilder.fromUri(requestUrl).path(taskId).path("variables").build().toString();
        ResponseEntity<String> getResponse;

        getResponse = camundaClient.get(getRequestUrl);

        responseHandler.acceptedResponse(getResponse);
        String respBody = getResponse.getBody();
        if (respBody != null) {
            taskList = buildTaskList(taskId, respBody);
        } else {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, ErrorCode.AvailabilityError)
                            .build();
            throw new BPMNFailureException.Builder(String.valueOf(HttpStatus.SC_BAD_GATEWAY), HttpStatus.SC_BAD_GATEWAY,
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES).errorInfo(errorLoggerInfo).build();
        }
        return taskList;
    }

    private TaskList buildTaskList(String taskId, String respBody) {
        TaskList taskList = new TaskList();
        JSONObject variables = new JSONObject(respBody);

        taskList.setTaskId(taskId);
        taskList.setType(getOptVariableValue(variables, "type"));
        taskList.setNfRole(getOptVariableValue(variables, "nfRole"));
        taskList.setSubscriptionServiceType(getOptVariableValue(variables, "subscriptionServiceType"));
        taskList.setOriginalRequestId(getOptVariableValue(variables, "originalRequestId"));
        taskList.setOriginalRequestorId(getOptVariableValue(variables, "originalRequestorId"));
        taskList.setErrorSource(getOptVariableValue(variables, "errorSource"));
        taskList.setErrorCode(getOptVariableValue(variables, "errorCode"));
        taskList.setErrorMessage(getOptVariableValue(variables, "errorMessage"));
        taskList.setBuildingBlockName(getOptVariableValue(variables, "buildingBlockName"));
        taskList.setBuildingBlockStep(getOptVariableValue(variables, "buildingBlockStep"));
        taskList.setDescription(getOptVariableValue(variables, "description"));
        taskList.setTimeout(getOptVariableValue(variables, "timeout"));

        String validResponses = getOptVariableValue(variables, "validResponses").toLowerCase();
        List<String> items = Arrays.asList(validResponses.split("\\s*,\\s*"));
        taskList.setValidResponses(items);

        return taskList;
    }

    private String getOptVariableValue(JSONObject variables, String name) {
        String variableEntry = variables.optString(name);
        String value = "";
        if (!variableEntry.isEmpty()) {
            JSONObject variableEntryJson = new JSONObject(variableEntry);
            value = variableEntryJson.optString("value");
        }
        return value;
    }


}
