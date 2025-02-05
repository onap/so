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

package org.onap.so.apihandlerinfra;

import java.io.IOException;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.apihandler.common.CamundaClient;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandler.common.ResponseHandler;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tasksbeans.TaskRequestReference;
import org.onap.so.apihandlerinfra.tasksbeans.TasksRequest;
import org.onap.so.apihandlerinfra.tasksbeans.Value;
import org.onap.so.apihandlerinfra.tasksbeans.Variables;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Path("/onap/so/infra/tasks")
@Component
public class ManualTasks {
    private static Logger logger = LoggerFactory.getLogger(ManualTasks.class);


    @org.springframework.beans.factory.annotation.Value("${mso.camunda.rest.task.uri}")
    private String taskUri;

    @Autowired
    private ResponseBuilder builder;

    @Autowired
    private CamundaClient camundaClient;

    @Autowired
    private ResponseHandler responseHandler;

    @POST
    @Path("/{version:[vV]1}/{taskId}/complete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Complete specified task", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response completeTask(String request, @PathParam("version") String version,
            @PathParam("taskId") String taskId, @Context ContainerRequestContext requestContext) throws ApiException {

        String requestId = requestContext.getProperty("requestId").toString();
        logger.info(LoggingAnchor.TWO, MessageEnum.APIH_GENERATED_REQUEST_ID.toString(), requestId);
        logger.debug("requestId is: {}", requestId);
        TasksRequest taskRequest = null;
        String apiVersion = version.substring(1);

        try {
            ObjectMapper mapper = new ObjectMapper();
            taskRequest = mapper.readValue(request, TasksRequest.class);

            if (taskRequest.getRequestDetails() == null) {
                throw new ValidationException("requestDetails");
            }
            if (taskRequest.getRequestDetails().getRequestInfo() == null) {
                throw new ValidationException("requestInfo");
            }
            if (empty(taskRequest.getRequestDetails().getRequestInfo().getSource())) {
                throw new ValidationException("source");
            }
            if (empty(taskRequest.getRequestDetails().getRequestInfo().getRequestorId())) {
                throw new ValidationException("requestorId");
            }

        } catch (IOException e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .build();


            ValidateException validateException =
                    new ValidateException.Builder("Mapping of request to JSON object failed: " + e.getMessage(),
                            HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                                    .errorInfo(errorLoggerInfo).build();

            throw validateException;
        } catch (ValidationException e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


            ValidateException validateException =
                    new ValidateException.Builder("Mapping of request to JSON Object failed. " + e.getMessage(),
                            HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo)
                                    .build();
            throw validateException;

        }

        // Transform the request to Camunda-style Complete request
        Variables variablesForComplete = new Variables();
        Value sourceValue = new Value();
        sourceValue.setValue(taskRequest.getRequestDetails().getRequestInfo().getSource());
        Value responseValue = new Value();
        responseValue.setValue(taskRequest.getRequestDetails().getRequestInfo().getResponseValue().name());
        Value requestorIdValue = new Value();
        requestorIdValue.setValue(taskRequest.getRequestDetails().getRequestInfo().getRequestorId());
        variablesForComplete.setSource(sourceValue);
        variablesForComplete.setResponseValue(responseValue);
        variablesForComplete.setRequestorId(requestorIdValue);

        String camundaJsonReq;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            camundaJsonReq = mapper.writeValueAsString(variablesForComplete);
        } catch (JsonProcessingException e) {
            logger.error("Mapping of JSON object to Camunda request failed");
            ValidateException validateException =
                    new ValidateException.Builder("Mapping of JSON object to Camunda request failed",
                            HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_GENERAL_SERVICE_ERROR).build();
            throw validateException;
        }

        String requestUrl = taskUri + "/" + taskId + "/complete";
        ResponseEntity<String> response = camundaClient.post(camundaJsonReq, requestUrl);

        int bpelStatus = responseHandler.setStatus(response.getStatusCodeValue());

        responseHandler.acceptedOrNoContentResponse(response);
        logger.debug("Received good response from Camunda");
        TaskRequestReference trr = new TaskRequestReference();
        trr.setTaskId(taskId);
        String completeResp = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            completeResp = mapper.writeValueAsString(trr);
        } catch (JsonProcessingException e) {
            logger.error("Unable to map response from Camunda");
            ValidateException validateException =
                    new ValidateException.Builder("Request Failed due to bad response format", bpelStatus,
                            ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).build();
            throw validateException;
        }
        logger.debug("Response to the caller: {}", completeResp);
        logger.debug("End of the transaction, the final response is: {}", completeResp);
        return builder.buildResponse(HttpStatus.SC_ACCEPTED, requestId, completeResp, apiVersion);
    }

    private static boolean empty(String s) {
        return (s == null || s.trim().isEmpty());
    }

}
