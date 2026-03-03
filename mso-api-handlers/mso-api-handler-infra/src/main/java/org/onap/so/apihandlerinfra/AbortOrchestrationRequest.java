/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.http.HttpStatus;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.logger.HttpHeadersConstants;
import org.onap.so.logger.LogConstants;
import org.onap.so.logger.MdcConstants;
import org.onap.so.logger.MessageEnum;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.List;

@Path("onap/so/infra/orchestrationRequests")
@OpenAPIDefinition(info = @Info(title = "onap/so/infra/orchestrationRequests"))

@Component
public class AbortOrchestrationRequest {
    private static final Logger logger = LoggerFactory.getLogger(AbortOrchestrationRequest.class);
    private static final String uriPrefix = "/orchestrationRequests/";

    @Autowired
    private RequestHandlerUtils requestHandlerUtils;

    @Autowired
    private MsoRequest msoRequest;

    @Autowired
    private ServiceInstances serviceInstances;

    @Autowired
    private PauseOrchestrationRequest pauseOrchestrationRequest;

    @Autowired
    private InfraActiveRequestsRepository infraActiveRequestsRepository;

    @POST
    @Path("/{version:[vV][7]}/abort")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Aborting process instance with the appropriate process instance id.",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response abortOrchestrationRequest(@PathParam("version") String version,
            @Context ContainerRequestContext requestContext) throws ApiException {

        Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
        String currentRequestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        logger.info("Beginning abort operation for new request: {}", currentRequestId);
        List<InfraActiveRequests> infraActiveRequestsList;
        String source = MDC.get(MdcConstants.ORIGINAL_PARTNER_NAME);
        String requestorId = MDC.get(HttpHeadersConstants.REQUESTOR_ID);
        requestHandlerUtils.getRequestUri(requestContext, uriPrefix);
        String requestUri = MDC.get(LogConstants.HTTP_URL);
        version = version.substring(1);

        try {
            infraActiveRequestsList = infraActiveRequestsRepository.getListOfRequestsInProgress();
        } catch (HttpClientErrorException e) {
            logger.error("Error occurred while performing requestDb for requests in progress: ", e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.AvailabilityError).build();
            throw new ValidateException.Builder("Exception while searching requests which are in progress ",
                    HttpStatus.SC_NOT_FOUND, ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB).cause(e)
                            .errorInfo(errorLoggerInfo).build();
        }

        InfraActiveRequests currentActiveRequest = requestHandlerUtils.createNewRecordCopyForPauseAbort(
                infraActiveRequestsList, currentRequestId, startTimeStamp, source, requestUri, requestorId);

        if (infraActiveRequestsList.isEmpty()) {
            logger.error("None of the Requests is in progress for abort:: {} in requesteDb", infraActiveRequestsList);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, ErrorCode.BusinessProcessError)
                            .build();
            ValidateException validateException =
                    new ValidateException.Builder("None of the SO(Service Instance) is InProgress status for Abort:",
                            HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo)
                                    .build();
            requestHandlerUtils.updateStatus(currentActiveRequest, Status.FAILED, validateException.getMessage());
            throw validateException;

        }

        return abortRequest(infraActiveRequestsList, currentActiveRequest, requestUri, version);
    }

    protected Response abortRequest(List<InfraActiveRequests> infraActiveRequests,
            InfraActiveRequests currentActiveRequest, String requestUri, String version) throws ApiException {

        String requestBody = currentActiveRequest.getRequestBody();
        Action action = Action.valueOf(currentActiveRequest.getRequestAction());
        String requestScope = currentActiveRequest.getRequestScope();
        String requestId = currentActiveRequest.getRequestId();

        ServiceInstancesRequest sir = null;
        sir = requestHandlerUtils.convertJsonToServiceInstanceRequest(requestBody, action, requestId, requestUri);
        Boolean aLaCarte = sir.getRequestDetails().getRequestParameters().getALaCarte();

        String pnfCorrelationId = serviceInstances.getPnfCorrelationId(sir);
        RecipeLookupResult recipeLookupResult = requestHandlerUtils.getServiceInstanceOrchestrationURI(sir, action,
                msoRequest.getAlacarteFlag(sir), currentActiveRequest);

        for (InfraActiveRequests infraActiveRequest : infraActiveRequests) {
            requestHandlerUtils.updateStatus(infraActiveRequest, Status.ABORTED,
                    "Service Instantiation is Aborted from this : ");
        }

        RequestClientParameter requestClientParameter = pauseOrchestrationRequest.setRequestClientParameter(
                recipeLookupResult, version, currentActiveRequest, pnfCorrelationId, sir, aLaCarte);

        return requestHandlerUtils.postBPELRequest(currentActiveRequest, requestClientParameter,
                recipeLookupResult.getOrchestrationURI(), requestScope);

    }
}
