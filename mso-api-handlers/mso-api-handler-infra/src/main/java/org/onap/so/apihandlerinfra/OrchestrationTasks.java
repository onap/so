/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.db.request.beans.OrchestrationTask;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static org.onap.so.apihandlerinfra.Constants.MSO_PROP_APIHANDLER_INFRA;

@Path("/onap/so/infra/orchestrationTasks")
@OpenAPIDefinition(
        info = @Info(title = "onap/so/infra/orchestrationTasks", description = "API Requests for Orchestration Task"))
@Component
public class OrchestrationTasks {

    private static Logger logger = LoggerFactory.getLogger(OrchestrationTasks.class);

    @Autowired
    private MsoRequest msoRequest;

    @Autowired
    private CamundaRequestHandler camundaRequestHandler;

    @Autowired
    private RequestsDbClient requestsDbClient;

    @Autowired
    private ResponseBuilder builder;

    private ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("/{version:[vV][4-7]}/")
    @Operation(description = "Find All Orchestrated Task", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response getAllOrchestrationTasks(@QueryParam("status") String status,
            @PathParam("version") String version) {
        List<OrchestrationTask> orchestrationTaskList = requestsDbClient.getAllOrchestrationTasks();
        if (status != null && !status.isEmpty()) {
            for (Iterator<OrchestrationTask> it = orchestrationTaskList.iterator(); it.hasNext();) {
                OrchestrationTask task = it.next();
                if (!status.equals(task.getStatus())) {
                    it.remove();
                }
            }
        }
        return builder.buildResponse(HttpStatus.SC_OK, null, orchestrationTaskList, version);
    }

    @GET
    @Path("/{version:[vV][4-7]}/{taskId}")
    @Operation(description = "Find Orchestrated Task for a given TaskId", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response getOrchestrationTask(@PathParam("taskId") String taskId, @PathParam("version") String version)
            throws ApiException {
        try {
            OrchestrationTask orchestrationTask = requestsDbClient.getOrchestrationTask(taskId);
            return builder.buildResponse(HttpStatus.SC_OK, null, orchestrationTask, version);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(),
                    "Exception while communciate with Request DB - Orchestration Task Lookup", e);
            Response response =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND, MsoException.ServiceException,
                            e.getMessage(), ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB, null, version);
            return response;
        }
    }

    @POST
    @Path("/{version:[vV][4-7]}/")
    @Operation(description = "Create an Orchestrated Task", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response CreateOrchestrationTask(String requestJson, @PathParam("version") String version) {
        try {
            OrchestrationTask orchestrationTask = mapper.readValue(requestJson, OrchestrationTask.class);
            requestsDbClient.createOrchestrationTask(orchestrationTask);
            return builder.buildResponse(HttpStatus.SC_OK, null, orchestrationTask, version);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(),
                    "Exception while communciate with Request DB - Orchestration Task Create", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, e.getMessage(), ErrorNumbers.COULD_NOT_WRITE_TO_REQUESTS_DB, null,
                    version);
            return response;
        }
    }

    @PUT
    @Path("/{version:[vV][4-7]}/{taskId}")
    @Operation(description = "Update an Orchestrated Task", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response UpdateOrchestrationTask(@PathParam("taskId") String taskId, String requestJson,
            @PathParam("version") String version) {
        try {
            OrchestrationTask orchestrationTask = requestsDbClient.getOrchestrationTask(taskId);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(),
                    "Exception while communciate with Request DB - Orchestration Task Update", e);
            Response response =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND, MsoException.ServiceException,
                            e.getMessage(), ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB, null, version);
            return response;
        }

        try {
            OrchestrationTask orchestrationTask = mapper.readValue(requestJson, OrchestrationTask.class);
            requestsDbClient.updateOrchestrationTask(taskId, orchestrationTask);
            return builder.buildResponse(HttpStatus.SC_OK, null, orchestrationTask, version);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(),
                    "Exception while communciate with Request DB - Orchestration Task Update", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, e.getMessage(), ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB, null,
                    version);
            return response;
        }
    }

    @DELETE
    @Path("/{version:[vV][4-7]}/{taskId}")
    @Operation(description = "Delete an Orchestrated Task", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response DeleteOrchestrationTask(@PathParam("taskId") String taskId, @PathParam("version") String version) {
        try {
            OrchestrationTask orchestrationTask = requestsDbClient.getOrchestrationTask(taskId);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(),
                    "Exception while communciate with Request DB - Orchestration Task Delete", e);
            Response response =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND, MsoException.ServiceException,
                            e.getMessage(), ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB, null, version);
            return response;
        }

        try {
            requestsDbClient.deleteOrchestrationTask(taskId);
            return builder.buildResponse(HttpStatus.SC_OK, null, null, version);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(),
                    "Exception while communciate with Request DB - Orchestration Task Delete", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, e.getMessage(), ErrorNumbers.COULD_NOT_WRITE_TO_REQUESTS_DB, null,
                    version);
            return response;
        }
    }

    @POST
    @Path("/{version:[vV][4-7]}/{taskId}/commit")
    @Operation(description = "Commit an Orchestrated Task", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response CommitOrchestrationTask(@PathParam("taskId") String taskId, @PathParam("version") String version) {
        OrchestrationTask orchestrationTask;
        try {
            orchestrationTask = requestsDbClient.getOrchestrationTask(taskId);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(),
                    "Exception while communciate with Request DB - Orchestration Task Commit", e);
            Response response =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND, MsoException.ServiceException,
                            e.getMessage(), ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB, null, version);
            return response;
        }
        try {
            String taskName = orchestrationTask.getName();
            Map<String, String> commitVar = new HashMap<>();
            commitVar.put("taskAction", "commit");
            JSONObject msgJson = createMessageBody(taskId, taskName, commitVar);
            camundaRequestHandler.sendCamundaMessages(msgJson);
            return builder.buildResponse(HttpStatus.SC_OK, null, orchestrationTask, version);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(),
                    "Exception while communciate with Request DB - Orchestration Task Delete", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, e.getMessage(), ErrorNumbers.ERROR_FROM_BPEL, null, version);
            return response;
        }

    }

    @POST
    @Path("/{version:[vV][4-7]}/{taskId}/abort")
    @Operation(description = "Commit an Orchestrated Task", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response AbortOrchestrationTask(@PathParam("taskId") String taskId, @PathParam("version") String version) {
        OrchestrationTask orchestrationTask;
        try {
            orchestrationTask = requestsDbClient.getOrchestrationTask(taskId);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(),
                    "Exception while communciate with Request DB - Orchestration Task Commit", e);
            Response response =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND, MsoException.ServiceException,
                            e.getMessage(), ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB, null, version);
            return response;
        }
        try {
            String taskName = orchestrationTask.getName();
            Map<String, String> commitVar = new HashMap<>();
            commitVar.put("taskAction", "abort");
            JSONObject msgJson = createMessageBody(taskId, taskName, commitVar);
            camundaRequestHandler.sendCamundaMessages(msgJson);
            return builder.buildResponse(HttpStatus.SC_OK, null, orchestrationTask, version);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(),
                    "Exception while communciate with Request DB - Orchestration Task Delete", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, e.getMessage(), ErrorNumbers.ERROR_FROM_BPEL, null, version);
            return response;
        }

    }

    private JSONObject createMessageBody(String taskId, String taskName, Map<String, ?> variables) {
        JSONObject msgJson = new JSONObject();
        msgJson.put("messageName", taskName);
        msgJson.put("businessKey", taskId);
        JSONObject processVariables = new JSONObject();
        for (Map.Entry<String, ?> entry : variables.entrySet()) {
            JSONObject valueInfo = new JSONObject();
            String key = entry.getKey();
            Object value = entry.getValue();
            valueInfo.put("value", value.toString());
            valueInfo.put("type", value.getClass().getSimpleName());
            processVariables.put(key, valueInfo);
        }
        msgJson.put("processVariables", processVariables);
        return msgJson;
    }

}
