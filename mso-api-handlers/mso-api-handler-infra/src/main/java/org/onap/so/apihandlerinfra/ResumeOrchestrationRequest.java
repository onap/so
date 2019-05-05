/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import java.sql.Timestamp;
import java.util.HashMap;
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
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.logger.ErrorCode;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("onap/so/infra/orchestrationRequests")
@Api(value = "onap/so/infra/orchestrationRequests")
@Component
public class ResumeOrchestrationRequest {
    private static Logger logger = LoggerFactory.getLogger(ResumeOrchestrationRequest.class);
    private static final String SAVE_TO_DB = "save instance to db";
    private static String uriPrefix = "/orchestrationRequests/";

    @Autowired
    private RequestHandlerUtils requestHandlerUtils;

    @Autowired
    private ServiceInstances serviceInstances;

    @Autowired
    private RequestsDbClient requestsDbClient;


    @POST
    @Path("/{version:[vV][7]}/requests/{requestId}/resume")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Resume request for a given requestId", response = Response.class)
    @Transactional
    public Response resumeOrchestrationRequest(@PathParam("requestId") String requestId,
            @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException {

        Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
        String currentRequestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        logger.info("Beginning resume operation for new request: {}", currentRequestId);
        InfraActiveRequests infraActiveRequest = null;
        String source = MDC.get(MdcConstants.ORIGINAL_PARTNER_NAME);
        String requestorId = MDC.get(HttpHeadersConstants.REQUESTOR_ID);
        requestHandlerUtils.getRequestUri(requestContext, uriPrefix);
        String requestUri = MDC.get(LogConstants.HTTP_URL);
        version = version.substring(1);

        try {
            infraActiveRequest = requestsDbClient.getInfraActiveRequestbyRequestId(requestId);
        } catch (HttpClientErrorException e) {
            logger.error("Error occurred while performing requestDb lookup by requestId: " + requestId, e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.AvailabilityError).build();
            throw new ValidateException.Builder("Exception while performing requestDb lookup by requestId",
                    HttpStatus.SC_NOT_FOUND, ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB).cause(e)
                            .errorInfo(errorLoggerInfo).build();
        }

        InfraActiveRequests currentActiveRequest = requestHandlerUtils.createNewRecordCopyFromInfraActiveRequest(
                infraActiveRequest, currentRequestId, startTimeStamp, source, requestUri, requestorId);

        if (infraActiveRequest == null) {
            logger.error("No infraActiveRequest record found for requestId: {} in requesteDb lookup", requestId);
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND,
                    ErrorCode.BusinessProcesssError).build();
            ValidateException validateException = new ValidateException.Builder(
                    "Null response from requestDB when searching by requestId: " + requestId, HttpStatus.SC_NOT_FOUND,
                    ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();
            requestHandlerUtils.updateStatus(currentActiveRequest, Status.FAILED, validateException.getMessage());
            throw validateException;

        }

        return resumeRequest(infraActiveRequest, currentActiveRequest, version, requestUri);
    }

    protected Response resumeRequest(InfraActiveRequests infraActiveRequest, InfraActiveRequests currentActiveRequest,
            String version, String requestUri) throws ApiException {
        String requestBody = infraActiveRequest.getRequestBody();
        Action action = Action.valueOf(infraActiveRequest.getRequestAction());
        String requestId = currentActiveRequest.getRequestId();
        String serviceInstanceName = infraActiveRequest.getServiceInstanceName();
        String requestScope = infraActiveRequest.getRequestScope();
        String serviceInstanceId = infraActiveRequest.getServiceInstanceId();

        checkForInProgressRequest(currentActiveRequest, serviceInstanceId, requestScope, serviceInstanceName, action);

        ServiceInstancesRequest sir = null;
        sir = requestHandlerUtils.convertJsonToServiceInstanceRequest(requestBody, action, requestId, requestUri);
        Boolean aLaCarte = sir.getRequestDetails().getRequestParameters().getALaCarte();
        if (aLaCarte == null) {
            aLaCarte = false;
        }

        String pnfCorrelationId = serviceInstances.getPnfCorrelationId(sir);
        RecipeLookupResult recipeLookupResult = serviceRecipeLookup(currentActiveRequest, sir, action, aLaCarte);

        requestDbSave(currentActiveRequest);

        RequestClientParameter requestClientParameter = setRequestClientParameter(recipeLookupResult, version,
                infraActiveRequest, currentActiveRequest, pnfCorrelationId, aLaCarte, sir);

        return requestHandlerUtils.postBPELRequest(currentActiveRequest, requestClientParameter,
                recipeLookupResult.getOrchestrationURI(), requestScope);
    }

    protected void checkForInProgressRequest(InfraActiveRequests currentActiveRequest, String serviceInstanceId,
            String requestScope, String serviceInstanceName, Action action) throws ApiException {
        boolean inProgress = false;
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        InfraActiveRequests requestInProgress = requestHandlerUtils.duplicateCheck(action, instanceIdMap,
                serviceInstanceName, requestScope, currentActiveRequest);
        if (requestInProgress != null) {
            inProgress = requestHandlerUtils.camundaHistoryCheck(requestInProgress, currentActiveRequest);
        }
        if (inProgress) {
            requestHandlerUtils.buildErrorOnDuplicateRecord(currentActiveRequest, action, instanceIdMap,
                    serviceInstanceName, requestScope, requestInProgress);
        }
    }

    protected RecipeLookupResult serviceRecipeLookup(InfraActiveRequests currentActiveRequest,
            ServiceInstancesRequest sir, Action action, Boolean aLaCarte)
            throws ValidateException, RequestDbFailureException {
        RecipeLookupResult recipeLookupResult = null;
        try {
            recipeLookupResult = serviceInstances.getServiceURI(sir, action, aLaCarte);
        } catch (IOException e) {
            logger.error("IOException while performing service recipe lookup", e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException =
                    new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST,
                            ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();
            requestHandlerUtils.updateStatus(currentActiveRequest, Status.FAILED, validateException.getMessage());
            throw validateException;
        }
        return recipeLookupResult;
    }

    protected void requestDbSave(InfraActiveRequests currentActiveRequest) throws RequestDbFailureException {
        try {
            requestsDbClient.save(currentActiveRequest);
        } catch (Exception e) {
            logger.error("Exception while saving request to requestDb", e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.DataError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e).errorInfo(errorLoggerInfo).build();
        }
    }

    protected RequestClientParameter setRequestClientParameter(RecipeLookupResult recipeLookupResult, String version,
            InfraActiveRequests infraActiveRequest, InfraActiveRequests currentActiveRequest, String pnfCorrelationId,
            Boolean aLaCarte, ServiceInstancesRequest sir) throws ValidateException {
        RequestClientParameter requestClientParameter = null;
        try {
            requestClientParameter = new RequestClientParameter.Builder()
                    .setRequestId(currentActiveRequest.getRequestId())
                    .setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                    .setRequestAction(infraActiveRequest.getRequestAction())
                    .setServiceInstanceId(infraActiveRequest.getServiceInstanceId())
                    .setPnfCorrelationId(pnfCorrelationId).setVnfId(infraActiveRequest.getVnfId())
                    .setVfModuleId(infraActiveRequest.getVfModuleId())
                    .setVolumeGroupId(infraActiveRequest.getVolumeGroupId())
                    .setNetworkId(infraActiveRequest.getNetworkId()).setServiceType(infraActiveRequest.getServiceType())
                    .setVnfType(infraActiveRequest.getVnfType()).setNetworkType(infraActiveRequest.getNetworkType())
                    .setRequestDetails(requestHandlerUtils.mapJSONtoMSOStyle(infraActiveRequest.getRequestBody(), sir,
                            aLaCarte, Action.valueOf(infraActiveRequest.getRequestAction())))
                    .setApiVersion(version).setALaCarte(aLaCarte).setRequestUri(currentActiveRequest.getRequestUrl())
                    .setInstanceGroupId(infraActiveRequest.getInstanceGroupId()).build();
        } catch (IOException e) {
            logger.error("IOException while generating requestClientParameter to send to BPMN", e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.SchemaError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new ValidateException.Builder(
                    "IOException while generating requestClientParameter to send to BPMN: " + e.getMessage(),
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo)
                            .build();
        }
        return requestClientParameter;
    }
}
