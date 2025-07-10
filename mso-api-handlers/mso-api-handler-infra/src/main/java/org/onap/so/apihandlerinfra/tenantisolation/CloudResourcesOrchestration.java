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

package org.onap.so.apihandlerinfra.tenantisolation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.http.HttpStatus;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.Constants;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tenantisolationbeans.CloudOrchestrationRequestList;
import org.onap.so.apihandlerinfra.tenantisolationbeans.CloudOrchestrationResponse;
import org.onap.so.apihandlerinfra.tenantisolationbeans.InstanceReferences;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Request;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestStatus;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Component
@Path("onap/so/infra/cloudResourcesRequests")
@OpenAPIDefinition(info = @Info(title = "onap/so/infra/cloudResourcesRequests",
        description = "API GET Requests for cloud resources - Tenant Isolation"))
public class CloudResourcesOrchestration {

    private static Logger logger = LoggerFactory.getLogger(CloudResourcesOrchestration.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    RequestsDbClient requestDbClient;

    @Autowired
    private ResponseBuilder builder;

    @POST
    @Path("/{version: [vV][1]}/{requestId}/unlock")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Unlock CloudOrchestration requests for a specified requestId")
    @Transactional
    public Response unlockOrchestrationRequest(String requestJSON, @PathParam("requestId") String requestId,
            @PathParam("version") String version) throws ApiException {
        TenantIsolationRequest msoRequest = new TenantIsolationRequest(requestId);
        InfraActiveRequests infraActiveRequest;

        CloudOrchestrationRequest cor;

        logger.debug("requestId is: {}", requestId);

        try {
            cor = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
        } catch (IOException e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .build();

            ValidateException validateException =
                    new ValidateException.Builder("Mapping of request to JSON object failed.  " + e.getMessage(),
                            HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                                    .errorInfo(errorLoggerInfo).build();
            throw validateException;
        }

        try {
            msoRequest.parseOrchestration(cor);
        } catch (ValidationException e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .build();
            ValidateException validateException =
                    new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST,
                            ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();
            throw validateException;
        }
        try {
            infraActiveRequest = requestDbClient.getInfraActiveRequestbyRequestId(requestId);
        } catch (Exception e) {
            ValidateException validateException = new ValidateException.Builder(e.getMessage(),
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e).build();
            throw validateException;
        }
        if (infraActiveRequest == null) {

            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, ErrorCode.BusinessProcessError)
                            .build();
            ValidateException validateException =
                    new ValidateException.Builder("Orchestration RequestId " + requestId + " is not found in DB",
                            HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
                                    .errorInfo(errorLoggerInfo).build();

            throw validateException;

        } else {
            String status = infraActiveRequest.getRequestStatus();
            if ("IN_PROGRESS".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status)
                    || "PENDING_MANUAL_TASK".equalsIgnoreCase(status)) {
                infraActiveRequest.setRequestStatus("UNLOCKED");
                infraActiveRequest.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
                infraActiveRequest.setRequestId(requestId);
                requestDbClient.save(infraActiveRequest);
            } else {
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, ErrorCode.DataError)
                                .build();
                ValidateException validateException = new ValidateException.Builder(
                        "Orchestration RequestId " + requestId + " has a status of " + status
                                + " and can not be unlocked",
                        HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo)
                                .build();

                throw validateException;
            }
        }

        return Response.status(HttpStatus.SC_NO_CONTENT).entity("").build();
    }

    @GET
    @Path("/{version:[vV][1]}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get status of an Operational Environment based on filter criteria",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response getOperationEnvironmentStatusFilter(@Context UriInfo ui, @PathParam("version") String version)
            throws ApiException {

        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        List<String> requestIdKey = queryParams.get("requestId");
        String apiVersion = version.substring(1);

        if (queryParams.size() == 1 && requestIdKey != null) {
            String requestId = requestIdKey.get(0);

            CloudOrchestrationResponse cloudOrchestrationGetResponse = new CloudOrchestrationResponse();
            InfraActiveRequests requestDB;

            try {
                requestDB = requestDbClient.getInfraActiveRequestbyRequestId(requestId);
            } catch (Exception e) {
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.AvailabilityError)
                                .build();
                ValidateException validateException =
                        new ValidateException.Builder(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e).errorInfo(errorLoggerInfo).build();
                throw validateException;
            }

            if (requestDB == null) {
                ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR,
                        ErrorCode.BusinessProcessError).build();
                ValidateException validateException =
                        new ValidateException.Builder("Orchestration RequestId " + requestId + " is not found in DB",
                                HttpStatus.SC_NO_CONTENT, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
                                        .errorInfo(errorLoggerInfo).build();

                throw validateException;
            }

            Request request = mapInfraActiveRequestToRequest(requestDB);
            cloudOrchestrationGetResponse.setRequest(request);
            return builder.buildResponse(HttpStatus.SC_OK, requestId, cloudOrchestrationGetResponse, apiVersion);

        } else {
            TenantIsolationRequest tenantIsolationRequest = new TenantIsolationRequest();
            List<InfraActiveRequests> activeRequests;
            CloudOrchestrationRequestList orchestrationList;


            Map<String, String> orchestrationMap;
            try {
                orchestrationMap = tenantIsolationRequest.getOrchestrationFilters(queryParams);
            } catch (ValidationException ex) {
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, ErrorCode.BusinessProcessError)
                                .build();
                ValidateException validateException =
                        new ValidateException.Builder(ex.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                ErrorNumbers.SVC_GENERAL_SERVICE_ERROR).cause(ex).errorInfo(errorLoggerInfo).build();

                throw validateException;

            }
            activeRequests = requestDbClient.getCloudOrchestrationFiltersFromInfraActive(orchestrationMap);
            orchestrationList = new CloudOrchestrationRequestList();
            List<CloudOrchestrationResponse> requestLists = new ArrayList<>();

            for (InfraActiveRequests infraActive : activeRequests) {

                Request request = mapInfraActiveRequestToRequest(infraActive);
                CloudOrchestrationResponse requestList = new CloudOrchestrationResponse();
                requestList.setRequest(request);
                requestLists.add(requestList);
            }
            orchestrationList.setRequestList(requestLists);

            return builder.buildResponse(HttpStatus.SC_OK, null, orchestrationList, apiVersion);
        }
    }

    private Request mapInfraActiveRequestToRequest(InfraActiveRequests iar) throws ApiException {
        Request request = new Request();
        request.setRequestId(iar.getRequestId());
        request.setRequestScope(iar.getRequestScope());
        request.setRequestType(iar.getRequestAction());

        InstanceReferences ir = new InstanceReferences();

        if (iar.getOperationalEnvId() != null)
            ir.setOperationalEnvironmentId(iar.getOperationalEnvId());
        if (iar.getOperationalEnvName() != null)
            ir.setOperationalEnvName(iar.getOperationalEnvName());
        if (iar.getRequestorId() != null)
            ir.setRequestorId(iar.getRequestorId());

        request.setInstanceReferences(ir);
        String requestBody = iar.getRequestBody();
        RequestDetails requestDetails = null;

        if (requestBody != null) {
            try {
                requestDetails = mapper.readValue(requestBody, RequestDetails.class);
            } catch (IOException e) {
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                                .build();
                ValidateException validateException =
                        new ValidateException.Builder("Mapping of request to JSON object failed.  " + e.getMessage(),
                                HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                                        .errorInfo(errorLoggerInfo).build();
                throw validateException;
            }
        }

        request.setRequestDetails(requestDetails);
        String startTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(iar.getStartTime()) + " GMT";
        request.setStartTime(startTimeStamp);

        RequestStatus status = new RequestStatus();
        if (iar.getStatusMessage() != null) {
            status.setStatusMessage(iar.getStatusMessage());
        }

        if (iar.getEndTime() != null) {
            String endTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(iar.getEndTime()) + " GMT";
            status.setTimeStamp(endTimeStamp);
        }

        if (iar.getRequestStatus() != null) {
            status.setRequestState(iar.getRequestStatus());
        }

        if (iar.getProgress() != null) {
            status.setPercentProgress(iar.getProgress().toString());
        }

        request.setRequestStatus(status);

        return request;
    }

}
