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
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.HttpHeadersConstants;
import org.onap.so.logger.LogConstants;
import org.onap.so.logger.MdcConstants;
import org.onap.so.logger.MessageEnum;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Path("onap/so/infra/orchestrationRequests")
@OpenAPIDefinition(info = @Info(title = "onap/so/infra/orchestrationRequests"))

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

    @Autowired
    private MsoRequest msoRequest;

    @POST
    @Path("/{version:[vV][7]}/{requestId}/resume")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Resume request for a given requestId", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
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
                infraActiveRequest, currentRequestId, startTimeStamp, source, requestUri, requestorId, requestId);

        if (infraActiveRequest == null) {
            logger.error("No infraActiveRequest record found for requestId: {} in requesteDb lookup", requestId);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, ErrorCode.BusinessProcessError)
                            .build();
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
        String requestScope = infraActiveRequest.getRequestScope();
        String instanceName = getInstanceName(infraActiveRequest, requestScope, currentActiveRequest);
        HashMap<String, String> instanceIdMap = setInstanceIdMap(infraActiveRequest, requestScope);

        checkForInProgressRequest(currentActiveRequest, instanceIdMap, requestScope, instanceName, action);

        ServiceInstancesRequest sir = null;
        sir = requestHandlerUtils.convertJsonToServiceInstanceRequest(requestBody, action, requestId, requestUri);
        Boolean aLaCarte = sir.getRequestDetails().getRequestParameters().getALaCarte();

        String pnfCorrelationId = serviceInstances.getPnfCorrelationId(sir);
        RecipeLookupResult recipeLookupResult = requestHandlerUtils.getServiceInstanceOrchestrationURI(sir, action,
                msoRequest.getAlacarteFlag(sir), currentActiveRequest);

        requestDbSave(currentActiveRequest);

        if (aLaCarte == null) {
            aLaCarte = setALaCarteFlagIfNull(requestScope, action);
        }

        RequestClientParameter requestClientParameter = setRequestClientParameter(recipeLookupResult, version,
                infraActiveRequest, currentActiveRequest, pnfCorrelationId, aLaCarte, sir);

        return requestHandlerUtils.postBPELRequest(currentActiveRequest, requestClientParameter,
                recipeLookupResult.getOrchestrationURI(), requestScope);
    }

    protected Boolean setALaCarteFlagIfNull(String requestScope, Action action) {
        Boolean aLaCarteFlag;
        if (!requestScope.equalsIgnoreCase(ModelType.service.name()) && action != Action.recreateInstance) {
            aLaCarteFlag = true;
        } else {
            aLaCarteFlag = false;
        }
        return aLaCarteFlag;
    }

    protected HashMap<String, String> setInstanceIdMap(InfraActiveRequests infraActiveRequest, String requestScope) {
        HashMap<String, String> instanceIdMap = new HashMap<>();
        ModelType type;
        try {
            type = ModelType.valueOf(requestScope);
            instanceIdMap.put(type.name() + "InstanceId", type.getId(infraActiveRequest));
        } catch (IllegalArgumentException e) {
            logger.error("requestScope \"{}\" does not match a ModelType enum.", requestScope);
        }
        return instanceIdMap;
    }

    protected String getInstanceName(InfraActiveRequests infraActiveRequest, String requestScope,
            InfraActiveRequests currentActiveRequest) throws ValidateException, RequestDbFailureException {
        ModelType type;
        String instanceName = "";
        try {
            type = ModelType.valueOf(requestScope);
            instanceName = type.getName(infraActiveRequest);
        } catch (IllegalArgumentException e) {
            logger.error("requestScope \"{}\" does not match a ModelType enum.", requestScope);
            ValidateException validateException = new ValidateException.Builder(
                    "requestScope: \"" + requestScope + "\" from request: " + infraActiveRequest.getRequestId()
                            + " does not match a ModelType enum.",
                    HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).build();
            requestHandlerUtils.updateStatus(currentActiveRequest, Status.FAILED, validateException.getMessage());
            throw validateException;
        }
        return instanceName;
    }

    protected void checkForInProgressRequest(InfraActiveRequests currentActiveRequest,
            HashMap<String, String> instanceIdMap, String requestScope, String instanceName, Action action)
            throws ApiException {
        boolean inProgress = false;
        InfraActiveRequests requestInProgress = requestHandlerUtils.duplicateCheck(action, instanceIdMap, instanceName,
                requestScope, currentActiveRequest);
        if (requestInProgress != null) {
            inProgress = requestHandlerUtils.camundaHistoryCheck(requestInProgress, currentActiveRequest);
        }
        if (inProgress) {
            requestHandlerUtils.buildErrorOnDuplicateRecord(currentActiveRequest, action, instanceIdMap, instanceName,
                    requestScope, requestInProgress);
        }
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
            Boolean aLaCarte, ServiceInstancesRequest sir) throws ApiException {
        RequestClientParameter requestClientParameter = null;
        Action action = Action.valueOf(infraActiveRequest.getRequestAction());
        ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();

        Boolean isBaseVfModule = false;
        if (requestHandlerUtils.getModelType(action, modelInfo).equals(ModelType.vfModule)) {
            isBaseVfModule = requestHandlerUtils.getIsBaseVfModule(modelInfo, action, infraActiveRequest.getVnfType(),
                    msoRequest.getSDCServiceModelVersion(sir), currentActiveRequest);
        }

        try {
            requestClientParameter = new RequestClientParameter.Builder()
                    .setRequestId(currentActiveRequest.getRequestId()).setBaseVfModule(isBaseVfModule)
                    .setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                    .setRequestAction(infraActiveRequest.getRequestAction())
                    .setServiceInstanceId(infraActiveRequest.getServiceInstanceId())
                    .setPnfCorrelationId(pnfCorrelationId).setVnfId(infraActiveRequest.getVnfId())
                    .setVfModuleId(infraActiveRequest.getVfModuleId())
                    .setVolumeGroupId(infraActiveRequest.getVolumeGroupId())
                    .setNetworkId(infraActiveRequest.getNetworkId()).setServiceType(infraActiveRequest.getServiceType())
                    .setVnfType(infraActiveRequest.getVnfType())
                    .setVfModuleType(msoRequest.getVfModuleType(sir, infraActiveRequest.getRequestScope()))
                    .setNetworkType(infraActiveRequest.getNetworkType())
                    .setRequestDetails(requestHandlerUtils.mapJSONtoMSOStyle(infraActiveRequest.getRequestBody(), sir,
                            aLaCarte, action))
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
