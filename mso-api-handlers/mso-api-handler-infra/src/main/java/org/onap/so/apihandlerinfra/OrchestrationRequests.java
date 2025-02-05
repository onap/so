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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.http.HttpStatus;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.constants.OrchestrationRequestFormat;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.MessageEnum;
import org.onap.so.serviceinstancebeans.CloudRequestData;
import org.onap.so.serviceinstancebeans.GetOrchestrationListResponse;
import org.onap.so.serviceinstancebeans.GetOrchestrationResponse;
import org.onap.so.serviceinstancebeans.InstanceReferences;
import org.onap.so.serviceinstancebeans.Request;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestList;
import org.onap.so.serviceinstancebeans.RequestStatus;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.utils.UUIDChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Path("onap/so/infra/orchestrationRequests")
@OpenAPIDefinition(info = @Info(title = "onap/so/infra/orchestrationRequests",
        description = "API Requests for Orchestration requests"))

@Component
public class OrchestrationRequests {

    private static Logger logger = LoggerFactory.getLogger(OrchestrationRequests.class);
    private static final String ERROR_MESSAGE_PREFIX = "Error Source: %s, Error Message: %s";

    @Autowired
    private RequestsDbClient requestsDbClient;

    @Autowired
    private MsoRequest msoRequest;

    @Autowired
    private ResponseBuilder builder;

    @Autowired
    private CamundaRequestHandler camundaRequestHandler;

    @Autowired
    private Environment env;

    @GET
    @Path("/{version:[vV][4-8]}/{requestId}")
    @Operation(description = "Find Orchestrated Requests for a given requestId", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response getOrchestrationRequest(@PathParam("requestId") String requestId,
            @PathParam("version") String version, @QueryParam("includeCloudRequest") boolean includeCloudRequest,
            @QueryParam(value = "format") String format) throws ApiException {

        GetOrchestrationResponse orchestrationResponse = new GetOrchestrationResponse();

        InfraActiveRequests infraActiveRequest = null;
        List<org.onap.so.db.request.beans.RequestProcessingData> requestProcessingData = null;

        if (!UUIDChecker.isValidUUID(requestId)) {

            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .errorSource(Constants.MODIFIED_BY_APIHANDLER).build();
            throw new ValidateException.Builder("Request Id " + requestId + " is not a valid UUID",
                    HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo).build();
        }

        infraActiveRequest = infraActiveRequestLookup(requestId);

        if (isRequestProcessingDataRequired(format)) {
            try {
                requestProcessingData = requestsDbClient.getExternalRequestProcessingDataBySoRequestId(requestId);
            } catch (Exception e) {
                logger.error(
                        "Exception occurred while communicating with RequestDb during requestProcessingData lookup ",
                        e);
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.AvailabilityError)
                                .build();

                ValidateException validateException = new ValidateException.Builder(
                        "Exception occurred while communicating with RequestDb during requestProcessingData lookup",
                        HttpStatus.SC_NOT_FOUND, ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB).cause(e)
                                .errorInfo(errorLoggerInfo).build();

                throw validateException;
            }
        }

        Request request = mapInfraActiveRequestToRequest(infraActiveRequest, includeCloudRequest, format, version);

        if (null != requestProcessingData && !requestProcessingData.isEmpty()) {
            request.setRequestProcessingData(mapRequestProcessingData(requestProcessingData));
        }
        request.setRequestId(requestId);
        orchestrationResponse.setRequest(request);

        return builder.buildResponse(HttpStatus.SC_OK, MDC.get(ONAPLogConstants.MDCs.REQUEST_ID), orchestrationResponse,
                version);
    }

    @GET
    @Path("/{version:[vV][4-8]}")
    @Operation(description = "Find Orchestrated Requests for a URI Information", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response getOrchestrationRequest(@Context UriInfo ui, @PathParam("version") String version,
            @QueryParam("includeCloudRequest") boolean includeCloudRequest, @QueryParam(value = "format") String format)
            throws ApiException {

        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

        List<InfraActiveRequests> activeRequests;

        GetOrchestrationListResponse orchestrationList;
        Map<String, List<String>> orchestrationMap;
        String apiVersion = version.substring(1);

        try {
            orchestrationMap = msoRequest.getOrchestrationFilters(queryParams);
            if (orchestrationMap.isEmpty()) {
                throw new ValidationException("At least one filter query param must be specified");
            }
        } catch (ValidationException ex) {
            logger.error("Exception occurred", ex);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.DataError).build();
            ValidateException validateException =
                    new ValidateException.Builder(ex.getMessage(), HttpStatus.SC_BAD_REQUEST,
                            ErrorNumbers.SVC_GENERAL_SERVICE_ERROR).cause(ex).errorInfo(errorLoggerInfo).build();
            throw validateException;

        }

        activeRequests = requestsDbClient.getOrchestrationFiltersFromInfraActive(orchestrationMap);

        orchestrationList = new GetOrchestrationListResponse();
        List<RequestList> requestLists = new ArrayList<>();


        for (InfraActiveRequests infraActive : activeRequests) {
            RequestList requestList = new RequestList();
            Request request = mapInfraActiveRequestToRequest(infraActive, includeCloudRequest, format, version);

            if (isRequestProcessingDataRequired(format)) {
                List<RequestProcessingData> requestProcessingData =
                        requestsDbClient.getExternalRequestProcessingDataBySoRequestId(infraActive.getRequestId());
                if (null != requestProcessingData && !requestProcessingData.isEmpty()) {
                    request.setRequestProcessingData(mapRequestProcessingData(requestProcessingData));
                }
            }

            requestList.setRequest(request);
            requestLists.add(requestList);
        }

        orchestrationList.setRequestList(requestLists);
        return builder.buildResponse(HttpStatus.SC_OK, MDC.get(ONAPLogConstants.MDCs.REQUEST_ID), orchestrationList,
                apiVersion);
    }

    @POST
    @Path("/{version: [vV][4-7]}/{requestId}/unlock")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Unlock Orchestrated Requests for a given requestId", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response unlockOrchestrationRequest(String requestJSON, @PathParam("requestId") String requestId,
            @PathParam("version") String version) throws ApiException {

        logger.debug("requestId is: {}", requestId);
        ServiceInstancesRequest sir;

        try {
            ObjectMapper mapper = new ObjectMapper();
            sir = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
        } catch (IOException e) {
            logger.error("Exception occurred", e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .build();
            ValidateException validateException =
                    new ValidateException.Builder("Mapping of request to JSON object failed : " + e.getMessage(),
                            HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                                    .errorInfo(errorLoggerInfo).build();

            throw validateException;

        }
        try {
            msoRequest.parseOrchestration(sir);
        } catch (Exception e) {
            logger.error("Exception occurred", e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .build();
            ValidateException validateException =
                    new ValidateException.Builder("Error parsing request: " + e.getMessage(), HttpStatus.SC_BAD_REQUEST,
                            ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();
            throw validateException;
        }

        InfraActiveRequests infraActiveRequest = infraActiveRequestLookup(requestId);

        String status = infraActiveRequest.getRequestStatus();
        if (Status.IN_PROGRESS.toString().equalsIgnoreCase(status) || Status.PENDING.toString().equalsIgnoreCase(status)
                || Status.PENDING_MANUAL_TASK.toString().equalsIgnoreCase(status)) {
            infraActiveRequest.setRequestStatus(Status.UNLOCKED.toString());
            infraActiveRequest.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
            infraActiveRequest.setRequestId(requestId);
            requestsDbClient.save(infraActiveRequest);
        } else {

            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, ErrorCode.DataError).build();

            ValidateException validateException = new ValidateException.Builder(
                    "Orchestration RequestId " + requestId + " has a status of " + status + " and can not be unlocked",
                    HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo)
                            .build();

            throw validateException;
        }
        return Response.status(HttpStatus.SC_NO_CONTENT).entity("").build();
    }

    protected Request mapInfraActiveRequestToRequest(InfraActiveRequests iar, boolean includeCloudRequest,
            String format, String version) throws ApiException {
        String requestBody = iar.getRequestBody();
        Request request = new Request();

        ObjectMapper mapper = new ObjectMapper();

        request.setRequestId(iar.getRequestId());
        request.setRequestScope(iar.getRequestScope());
        request.setRequestType(iar.getRequestAction());

        String originalRequestId = iar.getOriginalRequestId();
        if (originalRequestId != null) {
            request.setOriginalRequestId(originalRequestId);
        }

        if (!version.matches("v[1-7]")) {
            String workflowName = iar.getWorkflowName();
            if (workflowName == null) {
                workflowName = iar.getRequestAction();
            }
            request.setWorkflowName(workflowName);

            String operationName = iar.getOperationName();
            if (operationName != null) {
                request.setOperationName(operationName);
            }
        }

        InstanceReferences ir = new InstanceReferences();
        if (iar.getNetworkId() != null)
            ir.setNetworkInstanceId(iar.getNetworkId());
        if (iar.getNetworkName() != null)
            ir.setNetworkInstanceName(iar.getNetworkName());
        if (iar.getServiceInstanceId() != null)
            ir.setServiceInstanceId(iar.getServiceInstanceId());
        if (iar.getServiceInstanceName() != null)
            ir.setServiceInstanceName(iar.getServiceInstanceName());
        if (iar.getVfModuleId() != null)
            ir.setVfModuleInstanceId(iar.getVfModuleId());
        if (iar.getVfModuleName() != null)
            ir.setVfModuleInstanceName(iar.getVfModuleName());
        if (iar.getVnfId() != null)
            ir.setVnfInstanceId(iar.getVnfId());
        if (iar.getVnfName() != null)
            ir.setVnfInstanceName(iar.getVnfName());
        if (iar.getVolumeGroupId() != null)
            ir.setVolumeGroupInstanceId(iar.getVolumeGroupId());
        if (iar.getVolumeGroupName() != null)
            ir.setVolumeGroupInstanceName(iar.getVolumeGroupName());
        if (iar.getInstanceGroupId() != null)
            ir.setInstanceGroupId(iar.getInstanceGroupId());
        if (iar.getInstanceGroupName() != null)
            ir.setInstanceGroupName(iar.getInstanceGroupName());

        request.setInstanceReferences(ir);

        RequestDetails requestDetails = null;

        if (StringUtils.isNotBlank(requestBody)) {
            try {
                if (requestBody.contains("\"requestDetails\":")) {
                    ServiceInstancesRequest sir = mapper.readValue(requestBody, ServiceInstancesRequest.class);
                    requestDetails = sir.getRequestDetails();
                } else {
                    requestDetails = mapper.readValue(requestBody, RequestDetails.class);
                }
                if (requestDetails.getRequestInfo() != null && iar.getProductFamilyName() != null) {
                    requestDetails.getRequestInfo().setProductFamilyName(iar.getProductFamilyName());
                }
                if (requestDetails.getCloudConfiguration() != null && iar.getTenantName() != null) {
                    requestDetails.getCloudConfiguration().setTenantName(iar.getTenantName());
                }

            } catch (IOException e) {
                logger.error(String.format("Failed to parse request (id: %s) : ", request.getRequestId()), e);
            }
        }
        request.setRequestDetails(requestDetails);

        if (iar.getStartTime() != null) {
            String startTimeStamp =
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(iar.getStartTime()) + " GMT";
            request.setStartTime(startTimeStamp);
        }
        if (iar.getEndTime() != null) {
            String endTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(iar.getEndTime()) + " GMT";
            request.setFinishTime(endTimeStamp);
        }

        RequestStatus status = new RequestStatus();

        if (iar.getModifyTime() != null) {
            String timeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(iar.getModifyTime()) + " GMT";
            status.setTimeStamp(timeStamp);
        }

        status.setRequestState(mapRequestStatusToRequest(iar, format));

        if (iar.getProgress() != null) {
            status.setPercentProgress(iar.getProgress().intValue());
        }

        if (iar.getCloudApiRequests() != null && !iar.getCloudApiRequests().isEmpty() && includeCloudRequest) {
            iar.getCloudApiRequests().stream().forEach(cloudRequest -> {
                try {
                    request.getCloudRequestData()
                            .add(new CloudRequestData(mapper.readValue(cloudRequest.getRequestBody(), Object.class),
                                    cloudRequest.getCloudIdentifier()));
                } catch (Exception e) {
                    logger.error("Error reading Cloud Request", e);
                }
            });
        }

        mapRequestStatusAndExtSysErrSrcToRequest(iar, status, format, version);

        request.setRequestStatus(status);
        return request;
    }

    protected String mapRequestStatusToRequest(InfraActiveRequests iar, String format) {
        if (iar.getRequestStatus() != null) {
            boolean requestFormat = false;
            if (format != null) {
                requestFormat = EnumUtils.isValidEnum(OrchestrationRequestFormat.class, format.toUpperCase());
            }
            if (requestFormat) {
                return iar.getRequestStatus();
            } else {
                if (Status.ABORTED.toString().equalsIgnoreCase(iar.getRequestStatus())
                        || Status.ROLLED_BACK.toString().equalsIgnoreCase(iar.getRequestStatus())
                        || Status.ROLLED_BACK_TO_ASSIGNED.toString().equalsIgnoreCase(iar.getRequestStatus())
                        || Status.ROLLED_BACK_TO_CREATED.toString().equalsIgnoreCase(iar.getRequestStatus())) {
                    return Status.FAILED.toString();
                } else {
                    return iar.getRequestStatus();
                }
            }
        }
        return null;
    }

    protected void mapRequestStatusAndExtSysErrSrcToRequest(InfraActiveRequests iar, RequestStatus status,
            String format, String version) {
        String rollbackStatusMessage = iar.getRollbackStatusMessage();
        String flowStatusMessage = iar.getFlowStatus();
        String retryStatusMessage = iar.getRetryStatusMessage();
        String taskName = null;

        if (daysSinceRequest(iar) <= camundaCleanupInterval()) {
            if (format == null || !format.equalsIgnoreCase(OrchestrationRequestFormat.SIMPLENOTASKINFO.toString())) {
                if (flowStatusMessage != null && !flowStatusMessage.equals("Successfully completed all Building Blocks")
                        && !flowStatusMessage.equals("All Rollback flows have completed successfully")) {
                    taskName = camundaRequestHandler.getTaskName(iar.getRequestId());
                    if (taskName != null) {
                        flowStatusMessage = flowStatusMessage + " TASK INFORMATION: " + taskName;
                    }
                }
            }
        }

        String statusMessages = null;
        if (iar.getStatusMessage() != null) {
            if (StringUtils.isNotBlank(iar.getExtSystemErrorSource())) {
                statusMessages = "STATUS: "
                        + String.format(ERROR_MESSAGE_PREFIX, iar.getExtSystemErrorSource(), iar.getStatusMessage());
            } else {
                statusMessages = "STATUS: " + iar.getStatusMessage();
            }
        }

        if (OrchestrationRequestFormat.STATUSDETAIL.toString().equalsIgnoreCase(format)) {
            if (flowStatusMessage != null) {
                status.setFlowStatus(flowStatusMessage);
            }
            if (retryStatusMessage != null) {
                status.setRetryStatusMessage(retryStatusMessage);
            }
            if (rollbackStatusMessage != null) {
                status.setRollbackStatusMessage(rollbackStatusMessage);
            }
            if (version.matches("v[8-9]|v[1-9][0-9]")) {
                if (iar.getResourceStatusMessage() != null) {
                    status.setResourceStatusMessage(iar.getResourceStatusMessage());
                }
            }

            status.setExtSystemErrorSource(iar.getExtSystemErrorSource());
            status.setRollbackExtSystemErrorSource(iar.getRollbackExtSystemErrorSource());
        } else {

            if (flowStatusMessage != null) {
                if (statusMessages != null) {
                    statusMessages = statusMessages + " " + "FLOW STATUS: " + flowStatusMessage;
                } else {
                    statusMessages = "FLOW STATUS: " + flowStatusMessage;
                }
            }
            if (retryStatusMessage != null) {
                if (statusMessages != null) {
                    statusMessages = statusMessages + " " + "RETRY STATUS: " + retryStatusMessage;
                } else {
                    statusMessages = "RETRY STATUS: " + retryStatusMessage;
                }
            }
            if (rollbackStatusMessage != null) {
                if (statusMessages != null) {
                    statusMessages = statusMessages + " " + "ROLLBACK STATUS: " + rollbackStatusMessage;
                } else {
                    statusMessages = "ROLLBACK STATUS: " + rollbackStatusMessage;
                }
            }
            if (iar.getResourceStatusMessage() != null) {
                if (statusMessages != null) {
                    statusMessages = statusMessages + " " + "RESOURCE STATUS: " + iar.getResourceStatusMessage();
                } else {
                    statusMessages = "RESOURCE STATUS: " + iar.getResourceStatusMessage();
                }
            }
        }

        if (statusMessages != null) {
            status.setStatusMessage(statusMessages);
        }
    }

    public List<org.onap.so.serviceinstancebeans.RequestProcessingData> mapRequestProcessingData(
            List<org.onap.so.db.request.beans.RequestProcessingData> processingData) {
        List<org.onap.so.serviceinstancebeans.RequestProcessingData> addedRequestProcessingData = new ArrayList<>();
        org.onap.so.serviceinstancebeans.RequestProcessingData finalProcessingData =
                new org.onap.so.serviceinstancebeans.RequestProcessingData();
        String currentGroupingId = null;
        HashMap<String, String> tempMap = new HashMap<>();
        List<HashMap<String, String>> tempList = new ArrayList<>();
        for (RequestProcessingData data : processingData) {
            String groupingId = data.getGroupingId();
            String tag = data.getTag();
            if (currentGroupingId == null || !currentGroupingId.equals(groupingId)) {
                if (!tempMap.isEmpty()) {
                    tempList.add(tempMap);
                    finalProcessingData.setDataPairs(tempList);
                    addedRequestProcessingData.add(finalProcessingData);
                }
                finalProcessingData = new org.onap.so.serviceinstancebeans.RequestProcessingData();
                if (groupingId != null) {
                    finalProcessingData.setGroupingId(groupingId);
                }
                if (tag != null) {
                    finalProcessingData.setTag(tag);
                }
                currentGroupingId = groupingId;
                tempMap = new HashMap<>();
                tempList = new ArrayList<>();
                if (data.getName() != null && data.getValue() != null) {
                    tempMap.put(data.getName(), data.getValue());
                }
            } else {
                if (data.getName() != null && data.getValue() != null) {
                    tempMap.put(data.getName(), data.getValue());
                }
            }
        }
        if (tempMap.size() > 0) {
            tempList.add(tempMap);
            finalProcessingData.setDataPairs(tempList);
        }
        addedRequestProcessingData.add(finalProcessingData);
        return addedRequestProcessingData;
    }

    protected boolean isRequestProcessingDataRequired(String format) {
        if (StringUtils.isNotEmpty(format) && (format.equalsIgnoreCase(OrchestrationRequestFormat.SIMPLE.name())
                || format.equalsIgnoreCase(OrchestrationRequestFormat.SIMPLENOTASKINFO.toString()))) {
            return false;
        } else {
            return true;
        }
    }

    protected InfraActiveRequests infraActiveRequestLookup(String requestId) throws ApiException {
        InfraActiveRequests infraActiveRequest = null;
        try {
            infraActiveRequest = requestsDbClient.getInfraActiveRequestbyRequestId(requestId);
        } catch (HttpClientErrorException e) {
            ValidateException validateException = getValidateExceptionFromHttpClientErrorException(e);
            throw validateException;
        } catch (Exception e) {
            ValidateException validateException = getValidateExceptionForInternalServerError(e);
            throw validateException;
        }

        if (infraActiveRequest == null) {
            ValidateException validateException = getValidateExceptionForNotFound(requestId);
            throw validateException;
        }
        return infraActiveRequest;
    }

    private ValidateException getValidateExceptionForNotFound(String requestId) {
        ErrorLoggerInfo errorLoggerInfo =
                new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, ErrorCode.BusinessProcessError)
                        .build();

        ValidateException validateException =
                new ValidateException.Builder("Null response from RequestDB when searching by RequestId " + requestId,
                        HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo)
                                .build();
        return validateException;
    }

    private ValidateException getValidateExceptionForInternalServerError(Exception e) {
        logger.error("Exception occurred while communicating with RequestDb during InfraActiveRequest lookup ", e);
        ErrorLoggerInfo errorLoggerInfo =
                new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.AvailabilityError).build();

        ValidateException validateException = new ValidateException.Builder(
                "Exception occurred while communicating with RequestDb during InfraActiveRequest lookup",
                HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_GENERAL_SERVICE_ERROR).cause(e)
                        .errorInfo(errorLoggerInfo).build();
        return validateException;
    }

    private ValidateException getValidateExceptionFromHttpClientErrorException(HttpClientErrorException e) {
        logger.error("Exception occurred while communicating with RequestDb during InfraActiveRequest lookup ", e);
        ErrorLoggerInfo errorLoggerInfo =
                new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.AvailabilityError).build();

        ValidateException validateException = new ValidateException.Builder(
                "Exception occurred while communicating with RequestDb during InfraActiveRequest lookup",
                e.getRawStatusCode(), ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB).cause(e).errorInfo(errorLoggerInfo)
                        .build();
        return validateException;
    }

    protected long daysSinceRequest(InfraActiveRequests request) {
        long startTime = request.getStartTime().getTime();
        long now = System.currentTimeMillis();

        return TimeUnit.MILLISECONDS.toDays(now - startTime);
    }

    protected int camundaCleanupInterval() {
        String cleanupInterval = env.getProperty("mso.camundaCleanupInterval");
        int days = 30;
        if (cleanupInterval != null) {
            days = Integer.parseInt(cleanupInterval);
        }
        return days;
    }
}
