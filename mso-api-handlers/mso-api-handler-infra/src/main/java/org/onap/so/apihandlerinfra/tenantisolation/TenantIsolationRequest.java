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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.StringUtils;
import org.onap.so.apihandlerinfra.Constants;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Action;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Manifest;
import org.onap.so.apihandlerinfra.tenantisolationbeans.OperationalEnvironment;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RelatedInstance;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RelatedInstanceList;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestInfo;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestParameters;
import org.onap.so.apihandlerinfra.tenantisolationbeans.ResourceType;
import org.onap.so.apihandlerinfra.tenantisolationbeans.ServiceModelList;
import org.onap.so.apihandlerinfra.vnfbeans.RequestStatusType;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.utils.UUIDChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Scope("prototype")
public class TenantIsolationRequest {

    private static final ObjectMapper mapper = new ObjectMapper();

    private String requestId;
    private String requestJSON;
    private RequestInfo requestInfo;

    private String errorMessage;
    private String responseBody;
    private RequestStatusType status;
    private String operationalEnvironmentId;
    private long progress = Constants.PROGRESS_REQUEST_RECEIVED;
    private String requestScope;
    private CloudOrchestrationRequest cor;

    @Autowired
    private RequestsDbClient requestsDbClient;

    private static Logger logger = LoggerFactory.getLogger(TenantIsolationRequest.class);


    TenantIsolationRequest(String requestId) {
        this.requestId = requestId;
    }

    TenantIsolationRequest() {}

    void parse(CloudOrchestrationRequest request, HashMap<String, String> instanceIdMap, Action action)
            throws ValidationException {
        this.cor = request;
        this.requestInfo = request.getRequestDetails().getRequestInfo();

        try {
            requestJSON = mapper.writeValueAsString(request.getRequestDetails());
        } catch (JsonProcessingException e) {
            logger.error("Exception in JSON processing", e);
            throw new ValidationException("Parse ServiceInstanceRequest to JSON string", true);
        }

        String envId = null;
        if (instanceIdMap != null) {
            envId = instanceIdMap.get("operationalEnvironmentId");
            if (envId != null && !UUIDChecker.isValidUUID(envId)) {
                throw new ValidationException("operationalEnvironmentId", true);
            }
            cor.setOperationalEnvironmentId(envId);
        }

        this.operationalEnvironmentId = envId;

        RequestDetails requestDetails = request.getRequestDetails();
        RequestParameters requestParameters = requestDetails.getRequestParameters();

        requestInfoValidation(action, requestInfo);

        requestParamsValidation(action, requestParameters);

        relatedInstanceValidation(action, requestDetails, requestParameters);

    }

    private void relatedInstanceValidation(Action action, RequestDetails requestDetails,
            RequestParameters requestParameters) throws ValidationException {
        RelatedInstanceList[] instanceList = requestDetails.getRelatedInstanceList();

        if (requestParameters == null) {
            throw new ValidationException("requestParameters", true);
        }
        if ((Action.activate.equals(action) || Action.deactivate.equals(action))
                && OperationalEnvironment.ECOMP.equals(requestParameters.getOperationalEnvironmentType())) {
            throw new ValidationException("operationalEnvironmentType in requestParameters", true);
        }

        if (!Action.deactivate.equals(action)
                && OperationalEnvironment.VNF.equals(requestParameters.getOperationalEnvironmentType())) {
            if (instanceList != null && instanceList.length > 0) {
                for (RelatedInstanceList relatedInstanceList : instanceList) {
                    RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();

                    if (relatedInstance.getResourceType() == null) {
                        throw new ValidationException("ResourceType in relatedInstance", true);
                    }

                    if (!empty(relatedInstance.getInstanceName())
                            && !relatedInstance.getInstanceName().matches(Constants.VALID_INSTANCE_NAME_FORMAT)) {
                        throw new ValidationException("instanceName format", true);
                    }

                    if (empty(relatedInstance.getInstanceId())) {
                        throw new ValidationException("instanceId in relatedInstance", true);
                    }

                    if (!UUIDChecker.isValidUUID(relatedInstance.getInstanceId())) {
                        throw new ValidationException("instanceId format in relatedInstance", true);
                    }
                }
            } else {
                throw new ValidationException("relatedInstanceList", true);
            }
        }
    }

    private void requestParamsValidation(Action action, RequestParameters requestParameters)
            throws ValidationException {

        if (requestParameters != null) {
            if (!Action.deactivate.equals(action) && requestParameters.getOperationalEnvironmentType() == null) {
                throw new ValidationException("OperationalEnvironmentType", true);
            }

            if (Action.create.equals(action) && empty(requestParameters.getTenantContext())) {
                throw new ValidationException("Tenant Context", true);
            }
            if (!Action.deactivate.equals(action) && empty(requestParameters.getWorkloadContext())) {
                throw new ValidationException("Workload Context", true);
            }

            Manifest manifest = requestParameters.getManifest();

            if (Action.activate.equals(action)) {
                if (manifest == null) {
                    throw new ValidationException("Manifest on Activate", true);
                } else {
                    List<ServiceModelList> serviceModelList = manifest.getServiceModelList();

                    if (serviceModelList.isEmpty()) {
                        throw new ValidationException(" empty ServiceModelList", true);
                    }

                    for (ServiceModelList list : serviceModelList) {
                        if (empty(list.getServiceModelVersionId())) {
                            throw new ValidationException("ServiceModelVersionId", true);
                        }

                        if (!UUIDChecker.isValidUUID(list.getServiceModelVersionId())) {
                            throw new ValidationException("ServiceModelVersionId format", true);
                        }

                        if (list.getRecoveryAction() == null) {
                            throw new ValidationException("RecoveryAction", true);
                        }
                    }
                }
            }
        } else if (!Action.deactivate.equals(action)) {
            throw new ValidationException("request Parameters", true);
        }
    }

    private void requestInfoValidation(Action action, RequestInfo requestInfo) throws ValidationException {

        if (Action.create.equals(action) && empty(requestInfo.getInstanceName())) {
            throw new ValidationException("instanceName", true);
        }

        if (!empty(requestInfo.getInstanceName())
                && !requestInfo.getInstanceName().matches(Constants.VALID_INSTANCE_NAME_FORMAT)) {
            throw new ValidationException("instanceName format", true);
        }

        if (empty(requestInfo.getSource())) {
            throw new ValidationException("source", true);
        }

        if (empty(requestInfo.getRequestorId())) {
            throw new ValidationException("requestorId", true);
        }

        ResourceType resourceType = requestInfo.getResourceType();
        if (resourceType == null) {
            throw new ValidationException("resourceType", true);
        }

        this.requestScope = resourceType.name();
    }

    void parseOrchestration(CloudOrchestrationRequest cor) throws ValidationException {

        this.cor = cor;

        try {
            requestJSON = mapper.writeValueAsString(cor.getRequestDetails());

        } catch (JsonProcessingException e) {
            throw new ValidationException("Parse CloudOrchestrationRequest to JSON string", e);
        }

        if (cor.getRequestDetails() == null) {
            throw new ValidationException("requestDetails", true);
        }
        this.requestInfo = cor.getRequestDetails().getRequestInfo();

        if (this.requestInfo == null) {
            throw new ValidationException("requestInfo", true);
        }

        if (empty(requestInfo.getSource())) {
            throw new ValidationException("source", true);
        }
        if (empty(requestInfo.getRequestorId())) {
            throw new ValidationException("requestorId", true);
        }
    }

    public void createRequestRecord(Status status, Action action) {

        InfraActiveRequests aq = new InfraActiveRequests();
        aq.setRequestId(requestId);

        aq.setRequestAction(action.name());

        Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());

        aq.setStartTime(startTimeStamp);

        if (requestInfo != null) {

            if (requestInfo.getSource() != null) {
                aq.setSource(requestInfo.getSource());
            }
            if (requestInfo.getRequestorId() != null) {
                aq.setRequestorId(requestInfo.getRequestorId());
            }
            if (requestInfo.getResourceType() != null) {
                aq.setRequestScope(requestInfo.getResourceType().name());
            }
        }

        if (ResourceType.operationalEnvironment.name().equalsIgnoreCase(requestScope) && requestInfo != null) {
            aq.setOperationalEnvId(operationalEnvironmentId);
            aq.setOperationalEnvName(requestInfo.getInstanceName());
        }

        aq.setRequestBody(this.requestJSON);

        aq.setRequestStatus(status.toString());
        aq.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);

        if ((status == Status.FAILED) || (status == Status.COMPLETE)) {
            aq.setStatusMessage(this.errorMessage);
            aq.setResponseBody(this.responseBody);
            aq.setProgress(Long.valueOf(100));

            Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
            aq.setEndTime(endTimeStamp);
        } else if (status == Status.IN_PROGRESS) {
            aq.setProgress(Constants.PROGRESS_REQUEST_IN_PROGRESS);
        }
        requestsDbClient.save(aq);
    }


    public Map<String, String> getOrchestrationFilters(MultivaluedMap<String, String> queryParams)
            throws ValidationException {
        String queryParam = null;
        Map<String, String> orchestrationFilterParams = new HashMap<>();

        for (Entry<String, List<String>> entry : queryParams.entrySet()) {
            queryParam = entry.getKey();
            try {
                for (String value : entry.getValue()) {
                    if (StringUtils.isBlank(value)) {
                        throw (new Exception(queryParam + " value"));
                    }
                    orchestrationFilterParams.put(queryParam, value);
                }
            } catch (Exception e) {
                logger.error("Exception in getOrchestrationFilters", e);
                throw new ValidationException(e.getMessage(), true);
            }
        }

        return orchestrationFilterParams;
    }

    private static boolean empty(String s) {
        return (s == null || s.trim().isEmpty());
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void updateFinalStatus() {
        try {
            InfraActiveRequests request = new InfraActiveRequests(requestId);
            request.setRequestStatus(status.toString());
            request.setStatusMessage(this.errorMessage);
            request.setProgress(this.progress);
            request.setResponseBody(this.responseBody);
            request.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
            requestsDbClient.save(request);
        } catch (Exception e) {
            logger.error("Exception when updating record in DB", e);
            logger.debug("Exception: ", e);
        }
    }

    public void setStatus(RequestStatusType status) {
        this.status = status;
        switch (status) {
            case FAILED:
            case COMPLETE:
                this.progress = Constants.PROGRESS_REQUEST_COMPLETED;
                break;
            case IN_PROGRESS:
                this.progress = Constants.PROGRESS_REQUEST_IN_PROGRESS;
                break;
            case PENDING:
                break;
            case TIMEOUT:
                break;
            case UNLOCKED:
                break;
            default:
                break;
        }
    }

    public String getOperationalEnvironmentId() {
        return operationalEnvironmentId;
    }

    public void setOperationalEnvironmentId(String operationalEnvironmentId) {
        this.operationalEnvironmentId = operationalEnvironmentId;
    }
}
