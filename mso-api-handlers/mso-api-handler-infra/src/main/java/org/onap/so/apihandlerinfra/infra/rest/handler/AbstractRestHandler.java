/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.infra.rest.handler;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.apache.http.HttpStatus;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.apihandler.common.CamundaClient;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.apihandlerinfra.Constants;
import org.onap.so.apihandlerinfra.MsoRequest;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.infra.rest.exception.RequestConflictedException;
import org.onap.so.apihandlerinfra.infra.rest.exception.WorkflowEngineConnectionException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.logger.LogConstants;
import org.onap.so.logger.MessageEnum;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.onap.so.utils.UUIDChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractRestHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRestHandler.class);

    public static final String CONFLICT_FAIL_MESSAGE = "Error: Locked instance - This %s (%s) "
            + "already has a request being worked with a status of %s (RequestId - %s). The existing request must finish or be cleaned up before proceeding.";


    @Autowired
    protected CatalogDbClient catalogDbClient;

    @Autowired
    protected RequestsDbClient infraActiveRequestsClient;

    @Autowired
    private CamundaClient camundaClient;

    public String getRequestUri(ContainerRequestContext context) {
        String requestUri = context.getUriInfo().getPath();
        String httpUrl = MDC.get(LogConstants.URI_BASE).concat(requestUri);
        MDC.put(LogConstants.HTTP_URL, httpUrl);
        requestUri = requestUri.substring(requestUri.indexOf("/serviceInstantiation/") + 22);
        return requestUri;
    }

    public String getRequestId(ContainerRequestContext requestContext) throws ValidateException {
        String requestId = null;
        if (requestContext.getProperty("requestId") != null) {
            requestId = requestContext.getProperty("requestId").toString();
        }
        if (UUIDChecker.isValidUUID(requestId)) {
            return requestId;
        } else {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.SchemaError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new ValidateException.Builder("Request Id " + requestId + " is not a valid UUID",
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo)
                            .build();
        }
    }

    public InfraActiveRequests duplicateCheck(Actions action, Map<String, String> instanceIdMap, long startTime,
            MsoRequest msoRequest, String instanceName, String requestScope, InfraActiveRequests currentActiveReq)
            throws ApiException {
        return duplicateCheck(action, instanceIdMap, startTime, instanceName, requestScope, currentActiveReq);
    }

    public InfraActiveRequests duplicateCheck(Actions action, Map<String, String> instanceIdMap, long startTime,
            String instanceName, String requestScope, InfraActiveRequests currentActiveReq) throws ApiException {
        InfraActiveRequests dup = null;
        try {
            if (!(instanceName == null && "service".equals(requestScope) && (action == Action.createInstance
                    || action == Action.activateInstance || action == Action.assignInstance))) {
                dup = infraActiveRequestsClient.checkInstanceNameDuplicate(instanceIdMap, instanceName, requestScope);
            }
        } catch (Exception e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_CHECK_EXC, ErrorCode.DataError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            RequestDbFailureException requestDbFailureException =
                    new RequestDbFailureException.Builder("check for duplicate instance", e.toString(),
                            HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
                                    .errorInfo(errorLoggerInfo).build();
            updateStatus(currentActiveReq, Status.FAILED, requestDbFailureException.getMessage());
            throw requestDbFailureException;
        }
        return dup;
    }

    public void updateStatus(InfraActiveRequests aq, Status status, String errorMessage)
            throws RequestDbFailureException {
        if ((aq != null) && ((status == Status.FAILED) || (status == Status.COMPLETE))) {

            aq.setStatusMessage(errorMessage);
            aq.setProgress(100L);
            aq.setRequestStatus(status.toString());
            Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
            aq.setEndTime(endTimeStamp);
            try {
                infraActiveRequestsClient.updateInfraActiveRequests(aq);
            } catch (Exception e) {
                logger.error("Error updating status", e);
            }

        }
    }



    public void callWorkflowEngine(RequestClientParameter requestClientParameter, String orchestrationUri)
            throws WorkflowEngineConnectionException {
        try {
            camundaClient.post(requestClientParameter, orchestrationUri);
        } catch (ApiException e) {
            logger.error("Error Calling Workflow Engine", e);
            throw new WorkflowEngineConnectionException("Error Calling Workflow Engine", e);
        }
    }

    public Optional<URL> buildSelfLinkUrl(String url, String requestId) {
        Optional<URL> selfLinkUrl = Optional.empty();
        String version = "";
        try {
            URL aUrl = new URL(url);
            String aPath = aUrl.getPath();
            int indexOfVersion = Math.max(aPath.indexOf("/V"), aPath.indexOf("/v"));
            version = aPath.substring(indexOfVersion, indexOfVersion + 4);

            String pathWithSOAction = aPath.substring(0, indexOfVersion);
            String pathWithoutSOAction = pathWithSOAction.substring(0, pathWithSOAction.lastIndexOf("/"));

            String selfLinkPath =
                    pathWithoutSOAction.concat(Constants.ORCHESTRATION_REQUESTS_PATH).concat(version).concat(requestId);
            selfLinkUrl = Optional.of(new URL(aUrl.getProtocol(), aUrl.getHost(), aUrl.getPort(), selfLinkPath));
        } catch (Exception e) {
            selfLinkUrl = Optional.empty(); // ignore
            logger.error("Exception in buildSelfLinkUrl", e);
        }
        return selfLinkUrl;
    }

    /**
     * @param instanceId
     * @param requestId
     * @param requestContext
     */
    public ServiceInstancesResponse createResponse(String instanceId, String requestId,
            ContainerRequestContext requestContext) {
        ServiceInstancesResponse response = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId(instanceId);
        requestReferences.setRequestId(requestId);
        Optional<URL> optionalUrl = buildSelfLinkUrl(getRequestUri(requestContext), requestId);
        if (optionalUrl.isPresent()) {
            requestReferences.setRequestSelfLink(optionalUrl.get());
        }
        response.setRequestReferences(requestReferences);
        return response;
    }

    public void checkDuplicateRequest(Map<String, String> instanceIdMap, ModelType modelType, String instanceName,
            String requestId) throws RequestConflictedException {
        InfraActiveRequests conflictedRequest =
                infraActiveRequestsClient.checkInstanceNameDuplicate(instanceIdMap, instanceName, modelType.toString());
        if (conflictedRequest != null && !conflictedRequest.getRequestId().equals(requestId)) {
            throw new RequestConflictedException(String.format(CONFLICT_FAIL_MESSAGE, modelType.toString(),
                    instanceName, conflictedRequest.getRequestStatus(), conflictedRequest.getRequestId()));
        }
    }



}
