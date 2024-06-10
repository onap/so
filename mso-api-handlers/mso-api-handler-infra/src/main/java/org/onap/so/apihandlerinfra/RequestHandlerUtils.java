/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2019 IBM.
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


import static org.onap.so.logger.HttpHeadersConstants.REQUESTOR_ID;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandler.camundabeans.CamundaResponse;
import org.onap.so.apihandler.common.CamundaClient;
import org.onap.so.apihandler.common.CommonConstants;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandler.common.ResponseHandler;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.ContactCamundaException;
import org.onap.so.apihandlerinfra.exceptions.DuplicateRequestException;
import org.onap.so.apihandlerinfra.exceptions.RecipeNotFoundException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.exceptions.VfModuleNotFoundException;
import org.onap.so.apihandlerinfra.infra.rest.handler.AbstractRestHandler;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.Recipe;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfRecipe;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.LogConstants;
import org.onap.so.logger.MessageEnum;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.Networks;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;
import org.onap.so.utils.UUIDChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RequestHandlerUtils extends AbstractRestHandler {

    private static Logger logger = LoggerFactory.getLogger(RequestHandlerUtils.class);

    protected static final String SAVE_TO_DB = "save instance to db";
    private static final String NAME = "name";
    private static final String VALUE = "value";

    @Autowired
    private Environment env;

    @Autowired
    private ResponseBuilder builder;

    @Autowired
    private MsoRequest msoRequest;

    @Autowired
    private CamundaRequestHandler camundaRequestHandler;

    @Autowired
    private CatalogDbClient catalogDbClient;

    @Autowired
    private CamundaClient camundaClient;

    @Autowired
    private ResponseHandler responseHandler;

    protected ResponseEntity<String> postRequest(InfraActiveRequests currentActiveReq,
            RequestClientParameter requestClientParameter, String orchestrationUri) throws ApiException {
        try {
            return camundaClient.post(requestClientParameter, orchestrationUri);
        } catch (ApiException e) {
            updateStatus(currentActiveReq, Status.FAILED, e.getMessage());
            throw e;
        }
    }

    public Response postBPELRequest(InfraActiveRequests currentActiveReq, RequestClientParameter requestClientParameter,
            String orchestrationUri, String requestScope) throws ApiException {
        ObjectMapper mapper = new ObjectMapper();
        ResponseEntity<String> response = postRequest(currentActiveReq, requestClientParameter, orchestrationUri);
        ServiceInstancesResponse jsonResponse = null;
        int bpelStatus = responseHandler.setStatus(response.getStatusCodeValue());
        try {
            responseHandler.acceptedResponse(response);
            CamundaResponse camundaResponse = responseHandler.getCamundaResponse(response);
            String responseBody = camundaResponse.getResponse();
            if ("Success".equalsIgnoreCase(camundaResponse.getMessage())) {
                jsonResponse = mapper.readValue(responseBody, ServiceInstancesResponse.class);
                jsonResponse.getRequestReferences().setRequestId(requestClientParameter.getRequestId());
                Optional<URL> selfLinkUrl =
                        buildSelfLinkUrl(currentActiveReq.getRequestUrl(), requestClientParameter.getRequestId());
                if (selfLinkUrl.isPresent()) {
                    jsonResponse.getRequestReferences().setRequestSelfLink(selfLinkUrl.get());
                } else {
                    jsonResponse.getRequestReferences().setRequestSelfLink(null);
                }
            } else {
                BPMNFailureException bpmnException =
                        new BPMNFailureException.Builder(String.valueOf(bpelStatus) + responseBody, bpelStatus,
                                ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).build();
                updateStatus(currentActiveReq, Status.FAILED, bpmnException.getMessage());
                throw bpmnException;
            }
        } catch (ApiException e) {
            updateStatus(currentActiveReq, Status.FAILED, e.getMessage());
            throw e;
        } catch (IOException e) {
            logger.error("Exception caught mapping Camunda JSON response to object: ", e);
            updateStatus(currentActiveReq, Status.FAILED, e.getMessage());
            throw new ValidateException.Builder("Exception caught mapping Camunda JSON response to object",
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).build();
        }
        return builder.buildResponse(HttpStatus.SC_ACCEPTED, requestClientParameter.getRequestId(), jsonResponse,
                requestClientParameter.getApiVersion());
    }

    @Override
    public void updateStatus(InfraActiveRequests aq, Status status, String errorMessage)
            throws RequestDbFailureException {
        if ((status == Status.FAILED) || (status == Status.COMPLETE)) {
            aq.setStatusMessage(errorMessage);
            aq.setProgress(new Long(100));
            aq.setRequestStatus(status.toString());
            Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
            aq.setEndTime(endTimeStamp);
            try {
                infraActiveRequestsClient.save(aq);
            } catch (Exception e) {
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.DataError)
                                .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
                throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(),
                        HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
                                .errorInfo(errorLoggerInfo).build();
            }
        }
    }

    public String deriveRequestScope(Actions action, ServiceInstancesRequest sir, String requestUri) {
        if (action == Action.inPlaceSoftwareUpdate || action == Action.applyUpdatedConfig) {
            return (ModelType.vnf.name());
        } else if (action == Action.addMembers || action == Action.removeMembers) {
            return (ModelType.instanceGroup.toString());
        } else {
            String requestScope = requestScopeFromUri(requestUri);;

            if (sir.getRequestDetails() == null) {
                return requestScope;
            }
            if (sir.getRequestDetails().getModelInfo() == null) {
                return requestScope;
            }
            if (sir.getRequestDetails().getModelInfo().getModelType() == null) {
                return requestScope;
            }
            requestScope = sir.getRequestDetails().getModelInfo().getModelType().name();
            return requestScope;
        }
    }


    public void validateHeaders(ContainerRequestContext context) throws ValidationException {
        MultivaluedMap<String, String> headers = context.getHeaders();
        if (!headers.containsKey(ONAPLogConstants.Headers.REQUEST_ID)) {
            throw new ValidationException(ONAPLogConstants.Headers.REQUEST_ID + " header", true);
        }
        if (!headers.containsKey(ONAPLogConstants.Headers.PARTNER_NAME)) {
            throw new ValidationException(ONAPLogConstants.Headers.PARTNER_NAME + " header", true);
        }
        if (!headers.containsKey(REQUESTOR_ID)) {
            throw new ValidationException(REQUESTOR_ID + " header", true);
        }
    }

    public String getRequestUri(ContainerRequestContext context, String uriPrefix) {
        String requestUri = context.getUriInfo().getPath();
        String httpUrl = MDC.get(LogConstants.URI_BASE).concat(requestUri);
        MDC.put(LogConstants.HTTP_URL, httpUrl);
        requestUri = requestUri.substring(requestUri.indexOf(uriPrefix) + uriPrefix.length());
        return requestUri;
    }

    public void checkForDuplicateRequests(Actions action, HashMap<String, String> instanceIdMap, String requestScope,
            InfraActiveRequests currentActiveReq, String instanceName) throws ApiException {
        InfraActiveRequests dup = null;
        boolean inProgress = false;

        dup = duplicateCheck(action, instanceIdMap, instanceName, requestScope, currentActiveReq);

        if (dup != null) {
            inProgress = camundaHistoryCheck(dup, currentActiveReq);
        }

        if (dup != null && inProgress) {
            buildErrorOnDuplicateRecord(currentActiveReq, action, instanceIdMap, instanceName, requestScope, dup);
        }
    }

    public InfraActiveRequests duplicateCheck(Actions action, Map<String, String> instanceIdMap, String instanceName,
            String requestScope, InfraActiveRequests currentActiveReq) throws ApiException {
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

    public boolean camundaHistoryCheck(InfraActiveRequests duplicateRecord, InfraActiveRequests currentActiveReq)
            throws RequestDbFailureException, ContactCamundaException {
        String requestId = duplicateRecord.getRequestId();
        ResponseEntity<List<HistoricProcessInstanceEntity>> response = null;
        try {
            response = camundaRequestHandler.getCamundaProcessInstanceHistory(requestId, true, true, false);
        } catch (RestClientException e) {
            logger.error("Error querying Camunda for process-instance history for requestId: {}, exception: {}",
                    requestId, e.getMessage());
            ContactCamundaException contactCamundaException =
                    new ContactCamundaException.Builder("process-instance", requestId, e.toString(),
                            HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
                                    .build();
            updateStatus(currentActiveReq, Status.FAILED, contactCamundaException.getMessage());
            throw contactCamundaException;
        }

        if (response.getBody().isEmpty()) {
            updateStatus(duplicateRecord, Status.COMPLETE, "Request Completed");
        } else {
            return true;
        }
        return false;
    }

    public ServiceInstancesRequest convertJsonToServiceInstanceRequest(String requestJSON, Actions action,
            String requestId, String requestUri) throws ApiException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(requestJSON, ServiceInstancesRequest.class);

        } catch (IOException e) {

            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();

            ValidateException validateException =
                    new ValidateException.Builder("Error mapping request: " + e.getMessage(), HttpStatus.SC_BAD_REQUEST,
                            ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();
            String requestScope = requestScopeFromUri(requestUri);

            msoRequest.createErrorRequestRecord(Status.FAILED, requestId, validateException.getMessage(), action,
                    requestScope, requestJSON, null, null);

            throw validateException;
        }
    }

    public void parseRequest(ServiceInstancesRequest sir, Map<String, String> instanceIdMap, Actions action,
            String version, String requestJSON, Boolean aLaCarte, String requestId,
            InfraActiveRequests currentActiveReq) throws ValidateException, RequestDbFailureException {
        int reqVersion = Integer.parseInt(version.substring(1));
        try {
            msoRequest.parse(sir, instanceIdMap, action, version, requestJSON, reqVersion, aLaCarte);
        } catch (Exception e) {
            logger.error("failed to parse request", e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException =
                    new ValidateException.Builder("Error parsing request: " + e.getMessage(), HttpStatus.SC_BAD_REQUEST,
                            ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();

            updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());

            throw validateException;
        }
    }

    public void buildErrorOnDuplicateRecord(InfraActiveRequests currentActiveReq, Actions action,
            Map<String, String> instanceIdMap, String instanceName, String requestScope, InfraActiveRequests dup)
            throws ApiException {

        String instance = null;
        if (instanceName != null) {
            instance = instanceName;
        } else {
            instance = instanceIdMap.get(requestScope + "InstanceId");
        }
        ErrorLoggerInfo errorLoggerInfo =
                new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_FOUND, ErrorCode.SchemaError)
                        .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();

        DuplicateRequestException dupException =
                new DuplicateRequestException.Builder(requestScope, instance, dup.getRequestStatus(),
                        dup.getRequestId(), HttpStatus.SC_CONFLICT, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
                                .errorInfo(errorLoggerInfo).build();

        updateStatus(currentActiveReq, Status.FAILED, dupException.getMessage());

        throw dupException;
    }

    @Override
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
            ValidateException validateException =
                    new ValidateException.Builder("Request Id " + requestId + " is not a valid UUID",
                            HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER)
                                    .errorInfo(errorLoggerInfo).build();

            throw validateException;
        }
    }

    public void setInstanceId(InfraActiveRequests currentActiveReq, String requestScope, String instanceId,
            Map<String, String> instanceIdMap) {
        if (StringUtils.isNotBlank(instanceId)) {
            if (ModelType.service.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setServiceInstanceId(instanceId);
            } else if (ModelType.vnf.name().equalsIgnoreCase(requestScope)
                    || ModelType.cnf.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setVnfId(instanceId);
            } else if (ModelType.vfModule.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setVfModuleId(instanceId);
            } else if (ModelType.volumeGroup.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setVolumeGroupId(instanceId);
            } else if (ModelType.network.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setNetworkId(instanceId);
            } else if (ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setConfigurationId(instanceId);
            } else if (ModelType.instanceGroup.toString().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setInstanceGroupId(instanceId);
            }
        } else if (instanceIdMap != null && !instanceIdMap.isEmpty()) {
            if (instanceIdMap.get("serviceInstanceId") != null) {
                currentActiveReq.setServiceInstanceId(instanceIdMap.get("serviceInstanceId"));
            }
            if (instanceIdMap.get("vnfInstanceId") != null) {
                currentActiveReq.setVnfId(instanceIdMap.get("vnfInstanceId"));
            }
            if (instanceIdMap.get("vfModuleInstanceId") != null) {
                currentActiveReq.setVfModuleId(instanceIdMap.get("vfModuleInstanceId"));
            }
            if (instanceIdMap.get("volumeGroupInstanceId") != null) {
                currentActiveReq.setVolumeGroupId(instanceIdMap.get("volumeGroupInstanceId"));
            }
            if (instanceIdMap.get("networkInstanceId") != null) {
                currentActiveReq.setNetworkId(instanceIdMap.get("networkInstanceId"));
            }
            if (instanceIdMap.get("configurationInstanceId") != null) {
                currentActiveReq.setConfigurationId(instanceIdMap.get("configurationInstanceId"));
            }
            if (instanceIdMap.get(CommonConstants.INSTANCE_GROUP_INSTANCE_ID) != null) {
                currentActiveReq.setInstanceGroupId(instanceIdMap.get(CommonConstants.INSTANCE_GROUP_INSTANCE_ID));
            }
            if (instanceIdMap.get("pnfName") != null) {
                currentActiveReq.setPnfName(instanceIdMap.get("pnfName"));
            }
        }
    }

    public String mapJSONtoMSOStyle(String msoRawRequest, ServiceInstancesRequest serviceInstRequest,
            boolean isAlaCarte, Actions action) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        if (serviceInstRequest != null) {
            return mapper.writeValueAsString(serviceInstRequest);
        } else {
            return msoRawRequest;
        }
    }

    public Optional<String> retrieveModelName(RequestParameters requestParams) {
        String requestTestApi = null;
        TestApi testApi = null;

        if (requestParams != null) {
            requestTestApi = requestParams.getTestApi();
        }

        if (requestTestApi == null) {
            if (requestParams != null && requestParams.getALaCarte() != null && !requestParams.getALaCarte()) {
                requestTestApi = env.getProperty(CommonConstants.MACRO_TEST_API);
            } else {
                requestTestApi = env.getProperty(CommonConstants.ALACARTE_TEST_API);
            }
        }

        try {
            testApi = TestApi.valueOf(requestTestApi);
            return Optional.of(testApi.getModelName());
        } catch (Exception e) {
            logger.warn("Catching the exception on the valueOf enum call and continuing", e);
            throw new IllegalArgumentException("Invalid TestApi is provided", e);
        }
    }

    public String getDefaultModel(ServiceInstancesRequest sir) {
        String defaultModel = sir.getRequestDetails().getRequestInfo().getSource() + "_DEFAULT";
        Optional<String> oModelName = retrieveModelName(sir.getRequestDetails().getRequestParameters());
        if (oModelName.isPresent()) {
            defaultModel = oModelName.get();
        }
        return defaultModel;
    }

    public String getServiceType(String requestScope, ServiceInstancesRequest sir, Boolean aLaCarteFlag) {
        String serviceType = null;
        if (requestScope.equalsIgnoreCase(ModelType.service.toString())) {
            String defaultServiceModelName = getDefaultModel(sir);
            org.onap.so.db.catalog.beans.Service serviceRecord;
            if (aLaCarteFlag) {
                serviceRecord = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
                if (serviceRecord != null) {
                    serviceType = serviceRecord.getServiceType();
                }
            } else {
                serviceRecord =
                        catalogDbClient.getServiceByID(sir.getRequestDetails().getModelInfo().getModelVersionId());
                if (serviceRecord != null) {
                    serviceType = serviceRecord.getServiceType();
                } else {
                    serviceRecord = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
                    if (serviceRecord != null) {
                        serviceType = serviceRecord.getServiceType();
                    }
                }
            }
        } else {
            serviceType = msoRequest.getServiceInstanceType(sir, requestScope);
        }
        return serviceType;
    }

    protected String setServiceInstanceId(String requestScope, ServiceInstancesRequest sir) {
        String serviceInstanceId = null;
        if (sir.getServiceInstanceId() != null) {
            serviceInstanceId = sir.getServiceInstanceId();
        } else {
            Optional<String> serviceInstanceIdForInstance = getServiceInstanceIdForInstanceGroup(requestScope, sir);
            if (serviceInstanceIdForInstance.isPresent()) {
                serviceInstanceId = serviceInstanceIdForInstance.get();
            }
        }
        return serviceInstanceId;
    }

    private String requestScopeFromUri(String requestUri) {
        String requestScope;
        if (requestUri.contains(ModelType.network.name())) {
            requestScope = ModelType.network.name();
        } else if (requestUri.contains(ModelType.vfModule.name())) {
            requestScope = ModelType.vfModule.name();
        } else if (requestUri.contains(ModelType.volumeGroup.name())) {
            requestScope = ModelType.volumeGroup.name();
        } else if (requestUri.contains(ModelType.configuration.name())) {
            requestScope = ModelType.configuration.name();
        } else if (requestUri.contains(ModelType.vnf.name())) {
            requestScope = ModelType.vnf.name();
        } else if (requestUri.contains(ModelType.pnf.name())) {
            requestScope = ModelType.pnf.name();

        } else if (requestUri.contains(ModelType.cnf.name())) {
            requestScope = ModelType.cnf.name();
        } else {
            requestScope = ModelType.service.name();
        }
        return requestScope;
    }

    protected InfraActiveRequests createNewRecordCopyFromInfraActiveRequest(InfraActiveRequests infraActiveRequest,
            String requestId, Timestamp startTimeStamp, String source, String requestUri, String requestorId,
            String originalRequestId) throws ApiException {
        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId(requestId);
        request.setStartTime(startTimeStamp);
        request.setSource(source);
        request.setRequestUrl(requestUri);
        request.setProgress(new Long(5));
        request.setRequestorId(requestorId);
        request.setRequestStatus(Status.IN_PROGRESS.toString());
        request.setOriginalRequestId(originalRequestId);
        request.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        if (infraActiveRequest != null) {
            request.setTenantId(infraActiveRequest.getTenantId());
            request.setRequestBody(updateRequestorIdInRequestBody(infraActiveRequest, requestorId));
            request.setCloudRegion(infraActiveRequest.getCloudRegion());
            request.setRequestScope(infraActiveRequest.getRequestScope());
            request.setRequestAction(infraActiveRequest.getRequestAction());
            setInstanceIdAndName(infraActiveRequest, request);
        }
        return request;
    }

    protected void setInstanceIdAndName(InfraActiveRequests infraActiveRequest,
            InfraActiveRequests currentActiveRequest) throws ApiException {
        String requestScope = infraActiveRequest.getRequestScope();
        try {
            ModelType type = ModelType.valueOf(requestScope);
            String instanceName = type.getName(infraActiveRequest);
            if (instanceName == null && type.equals(ModelType.vfModule)) {
                logger.error("vfModule for requestId: {} being resumed does not have an instanceName.",
                        infraActiveRequest.getRequestId());
                ValidateException validateException = new ValidateException.Builder(
                        "vfModule for requestId: " + infraActiveRequest.getRequestId()
                                + " being resumed does not have an instanceName.",
                        HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).build();
                updateStatus(currentActiveRequest, Status.FAILED, validateException.getMessage());
                throw validateException;
            }
            if (instanceName != null) {
                type.setName(currentActiveRequest, instanceName);
            }
            type.setId(currentActiveRequest, type.getId(infraActiveRequest));
        } catch (IllegalArgumentException e) {
            logger.error(
                    "requestScope \"{}\" does not match a ModelType enum. Unable to set instanceId and instanceName from the original request.",
                    requestScope);
        }
    }

    protected Boolean getIsBaseVfModule(ModelInfo modelInfo, Actions action, String vnfType,
            String sdcServiceModelVersion, InfraActiveRequests currentActiveReq) throws ApiException {
        // Get VF Module-specific base module indicator
        VfModule vfm = null;
        String modelVersionId = modelInfo.getModelVersionId();
        Boolean isBaseVfModule = false;

        if (modelVersionId != null) {
            vfm = catalogDbClient.getVfModuleByModelUUID(modelVersionId);
        } else if (modelInfo.getModelInvariantId() != null && modelInfo.getModelVersion() != null) {
            vfm = catalogDbClient.getVfModuleByModelInvariantUUIDAndModelVersion(modelInfo.getModelInvariantId(),
                    modelInfo.getModelVersion());
        }

        if (vfm != null) {
            if (vfm.getIsBase()) {
                isBaseVfModule = true;
            }
        } else if (action == Action.createInstance || action == Action.updateInstance) {
            String serviceVersionText = "";
            if (sdcServiceModelVersion != null && !sdcServiceModelVersion.isEmpty()) {
                serviceVersionText = " with version " + sdcServiceModelVersion;
            }
            String errorMessage = "VnfType " + vnfType + " and VF Module Model Name " + modelInfo.getModelName()
                    + serviceVersionText + " not found in MSO Catalog DB";
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, ErrorCode.DataError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            VfModuleNotFoundException vfModuleException = new VfModuleNotFoundException.Builder(errorMessage,
                    HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo).build();
            updateStatus(currentActiveReq, Status.FAILED, vfModuleException.getMessage());
            throw vfModuleException;
        }
        return isBaseVfModule;
    }

    protected ModelType getModelType(Actions action, ModelInfo modelInfo) {
        if (action == Action.applyUpdatedConfig || action == Action.inPlaceSoftwareUpdate) {
            return ModelType.vnf;
        } else if (action == Action.addMembers || action == Action.removeMembers) {
            return ModelType.instanceGroup;
        } else {
            return modelInfo.getModelType();
        }
    }

    protected String updateRequestorIdInRequestBody(InfraActiveRequests infraActiveRequest, String newRequestorId) {
        String requestBody = infraActiveRequest.getRequestBody();
        return requestBody.replaceAll(
                "(?s)(\"requestInfo\"\\s*?:\\s*?\\{.*?\"requestorId\"\\s*?:\\s*?\")(.*?)(\"[ ]*(?:,|\\R|\\}))",
                "$1" + newRequestorId + "$3");
    }

    public RecipeLookupResult getServiceInstanceOrchestrationURI(ServiceInstancesRequest sir, Actions action,
            boolean alaCarteFlag, InfraActiveRequests currentActiveReq) throws ApiException {
        RecipeLookupResult recipeLookupResult = null;
        // if the aLaCarte flag is set to TRUE, the API-H should choose the VID_DEFAULT recipe for the requested action
        ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
        // Query MSO Catalog DB

        if (action == Action.applyUpdatedConfig || action == Action.inPlaceSoftwareUpdate) {
            recipeLookupResult = getDefaultVnfUri(sir, action);
        } else if (action == Action.addMembers || action == Action.removeMembers) {
            recipeLookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 180);
        } else if (modelInfo.getModelType().equals(ModelType.service)) {
            try {
                recipeLookupResult = getServiceURI(sir, action, alaCarteFlag);
            } catch (IOException e) {
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                                .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


                ValidateException validateException =
                        new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST,
                                ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();

                updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());

                throw validateException;
            }
        } else if (modelInfo.getModelType().equals(ModelType.vfModule)
                || modelInfo.getModelType().equals(ModelType.volumeGroup)
                || modelInfo.getModelType().equals(ModelType.vnf)) {
            try {
                recipeLookupResult = getVnfOrVfModuleUri(sir, action);
            } catch (ValidationException e) {
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                                .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


                ValidateException validateException =
                        new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST,
                                ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();

                updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());

                throw validateException;
            }
        } else if (modelInfo.getModelType().equals(ModelType.network)) {
            try {
                recipeLookupResult = getNetworkUri(sir, action);
            } catch (ValidationException e) {

                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                                .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


                ValidateException validateException =
                        new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST,
                                ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();
                updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());

                throw validateException;
            }
        }

        else if (modelInfo.getModelType().equals(ModelType.pnf)) {
            recipeLookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 180);
        } else if (modelInfo.getModelType().equals(ModelType.instanceGroup)) {
            recipeLookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 180);
        } else if (modelInfo.getModelType().equals(ModelType.cnf)) {
            recipeLookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 180);
        }

        if (recipeLookupResult == null) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.DataError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


            RecipeNotFoundException recipeNotFoundExceptionException =
                    new RecipeNotFoundException.Builder("Recipe could not be retrieved from catalog DB.",
                            HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_GENERAL_SERVICE_ERROR).errorInfo(errorLoggerInfo)
                                    .build();

            updateStatus(currentActiveReq, Status.FAILED, recipeNotFoundExceptionException.getMessage());
            throw recipeNotFoundExceptionException;
        }
        return recipeLookupResult;
    }

    protected RecipeLookupResult getServiceURI(ServiceInstancesRequest servInstReq, Actions action,
            boolean alaCarteFlag) throws IOException {
        // SERVICE REQUEST
        // Construct the default service name
        // TODO need to make this a configurable property
        String defaultServiceModelName = getDefaultModel(servInstReq);
        RequestDetails requestDetails = servInstReq.getRequestDetails();
        ModelInfo modelInfo = requestDetails.getModelInfo();
        org.onap.so.db.catalog.beans.Service serviceRecord;
        List<org.onap.so.db.catalog.beans.Service> serviceRecordList;
        ServiceRecipe recipe = null;

        if (alaCarteFlag) {
            serviceRecord = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
            if (serviceRecord != null) {
                recipe = catalogDbClient.getFirstByServiceModelUUIDAndAction(serviceRecord.getModelUUID(),
                        action.toString());
            }
        } else {
            serviceRecord = catalogDbClient.getServiceByID(modelInfo.getModelVersionId());
            recipe = catalogDbClient.getFirstByServiceModelUUIDAndAction(modelInfo.getModelVersionId(),
                    action.toString());
            if (recipe == null) {
                serviceRecordList = catalogDbClient
                        .getServiceByModelInvariantUUIDOrderByModelVersionDesc(modelInfo.getModelInvariantId());
                if (!serviceRecordList.isEmpty()) {
                    for (org.onap.so.db.catalog.beans.Service record : serviceRecordList) {
                        recipe = catalogDbClient.getFirstByServiceModelUUIDAndAction(record.getModelUUID(),
                                action.toString());
                        if (recipe != null) {
                            break;
                        }
                    }
                }
            }
        }

        // if an aLaCarte flag was sent in the request, throw an error if the recipe was not found
        RequestParameters reqParam = requestDetails.getRequestParameters();
        if (reqParam != null && alaCarteFlag && recipe == null) {
            return null;
        } else if (!alaCarteFlag && recipe != null && Action.createInstance.equals(action)) {
            mapToLegacyRequest(requestDetails);
        } else if (recipe == null) { // aLaCarte wasn't sent, so we'll try the default
            serviceRecord = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
            recipe = catalogDbClient.getFirstByServiceModelUUIDAndAction(serviceRecord.getModelUUID(),
                    action.toString());
        }
        if (modelInfo.getModelVersionId() == null) {
            modelInfo.setModelVersionId(serviceRecord.getModelUUID());
        }
        if (recipe == null) {
            return null;
        }
        return new RecipeLookupResult(recipe.getOrchestrationUri(), recipe.getRecipeTimeout());
    }

    protected void mapToLegacyRequest(RequestDetails requestDetails) throws IOException {
        RequestParameters reqParam;
        if (requestDetails.getRequestParameters() == null) {
            reqParam = new RequestParameters();
        } else {
            reqParam = requestDetails.getRequestParameters();
        }
        if (requestDetails.getCloudConfiguration() == null) {
            CloudConfiguration cloudConfig = configureCloudConfig(reqParam);
            if (cloudConfig != null) {
                requestDetails.setCloudConfiguration(cloudConfig);
            }
        }

        List<Map<String, Object>> userParams = configureUserParams(reqParam);
        if (!userParams.isEmpty()) {
            if (reqParam == null) {
                requestDetails.setRequestParameters(new RequestParameters());
            }
            requestDetails.getRequestParameters().setUserParams(userParams);
        }
    }

    private Service serviceMapper(Map<String, Object> params) throws IOException {
        ObjectMapper obj = new ObjectMapper();
        String input = obj.writeValueAsString(params.get("service"));
        return obj.readValue(input, Service.class);
    }

    private void addUserParams(Map<String, Object> targetUserParams, List<Map<String, String>> sourceUserParams) {
        for (Map<String, String> map : sourceUserParams) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                targetUserParams.put(entry.getKey(), entry.getValue());
            }
        }
    }

    protected List<Map<String, Object>> configureUserParams(RequestParameters reqParams) throws IOException {
        logger.debug("Configuring UserParams for Macro Request");
        Map<String, Object> userParams = new HashMap<>();

        for (Map<String, Object> params : reqParams.getUserParams()) {
            if (params.containsKey("service")) {
                Service service = serviceMapper(params);

                // Filter out non-string params for backward compatibility
                Map<String, String> svcStrParams = service.getInstanceParams().stream().map(Map::entrySet)
                        .flatMap(Set::stream).filter(e -> e.getValue() instanceof String)
                        .collect(Collectors.toMap(Entry::getKey, e -> (String) e.getValue()));
                userParams.putAll(svcStrParams);

                for (Networks network : service.getResources().getNetworks()) {
                    addUserParams(userParams, network.getInstanceParams());
                }

                for (Vnfs vnf : service.getResources().getVnfs()) {
                    addUserParams(userParams, vnf.getInstanceParams());

                    for (VfModules vfModule : vnf.getVfModules()) {
                        addUserParams(userParams, vfModule.getInstanceParams());
                    }
                }
            }
        }

        return mapFlatMapToNameValue(userParams);
    }

    protected List<Map<String, Object>> mapFlatMapToNameValue(Map<String, Object> flatMap) {
        List<Map<String, Object>> targetUserParams = new ArrayList<>();

        for (Map.Entry<String, Object> map : flatMap.entrySet()) {
            Map<String, Object> targetMap = new HashMap<>();
            targetMap.put(NAME, map.getKey());
            targetMap.put(VALUE, map.getValue());
            targetUserParams.add(targetMap);
        }
        return targetUserParams;
    }

    protected CloudConfiguration configureCloudConfig(RequestParameters reqParams) throws IOException {

        for (Map<String, Object> params : reqParams.getUserParams()) {
            if (params.containsKey("service")) {
                Service service = serviceMapper(params);

                Optional<CloudConfiguration> targetConfiguration = addCloudConfig(service.getCloudConfiguration());

                if (targetConfiguration.isPresent()) {
                    return targetConfiguration.get();
                } else {
                    for (Networks network : service.getResources().getNetworks()) {
                        targetConfiguration = addCloudConfig(network.getCloudConfiguration());
                        if (targetConfiguration.isPresent()) {
                            return targetConfiguration.get();
                        }
                    }

                    for (Vnfs vnf : service.getResources().getVnfs()) {
                        targetConfiguration = addCloudConfig(vnf.getCloudConfiguration());

                        if (targetConfiguration.isPresent()) {
                            return targetConfiguration.get();
                        }

                        for (VfModules vfModule : vnf.getVfModules()) {
                            targetConfiguration = addCloudConfig(vfModule.getCloudConfiguration());

                            if (targetConfiguration.isPresent()) {
                                return targetConfiguration.get();
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    protected Optional<String> getServiceInstanceIdForValidationError(ServiceInstancesRequest sir,
            HashMap<String, String> instanceIdMap, String requestScope) {
        if (instanceIdMap != null && !instanceIdMap.isEmpty() && instanceIdMap.get("serviceInstanceId") != null) {
            return Optional.of(instanceIdMap.get("serviceInstanceId"));
        } else {
            return getServiceInstanceIdForInstanceGroup(requestScope, sir);
        }
    }

    protected Optional<String> getServiceInstanceIdForInstanceGroup(String requestScope, ServiceInstancesRequest sir) {
        if (requestScope.equalsIgnoreCase(ModelType.instanceGroup.toString())) {
            RelatedInstanceList[] relatedInstances = sir.getRequestDetails().getRelatedInstanceList();
            if (relatedInstances != null) {
                for (RelatedInstanceList relatedInstanceList : relatedInstances) {
                    RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
                    if (relatedInstance.getModelInfo().getModelType() == ModelType.service) {
                        return Optional.ofNullable(relatedInstance.getInstanceId());
                    }
                }
            }
        }
        return Optional.empty();
    }

    private RecipeLookupResult getDefaultVnfUri(ServiceInstancesRequest sir, Actions action) {
        String defaultSource = getDefaultModel(sir);
        VnfRecipe vnfRecipe = catalogDbClient.getFirstVnfRecipeByNfRoleAndAction(defaultSource, action.toString());
        if (vnfRecipe == null) {
            return null;
        }
        return new RecipeLookupResult(vnfRecipe.getOrchestrationUri(), vnfRecipe.getRecipeTimeout());
    }


    private RecipeLookupResult getNetworkUri(ServiceInstancesRequest sir, Actions action) throws ValidationException {
        String defaultNetworkType = getDefaultModel(sir);
        ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
        String modelName = modelInfo.getModelName();
        Recipe recipe = null;

        if (modelInfo.getModelCustomizationId() != null) {
            NetworkResourceCustomization networkResourceCustomization = catalogDbClient
                    .getNetworkResourceCustomizationByModelCustomizationUUID(modelInfo.getModelCustomizationId());
            if (networkResourceCustomization != null) {
                NetworkResource networkResource = networkResourceCustomization.getNetworkResource();
                if (networkResource != null) {
                    if (modelInfo.getModelVersionId() == null) {
                        modelInfo.setModelVersionId(networkResource.getModelUUID());
                    }
                    recipe = catalogDbClient.getFirstNetworkRecipeByModelNameAndAction(networkResource.getModelName(),
                            action.toString());
                } else {
                    throw new ValidationException("no catalog entry found");
                }
            } else if (action != Action.deleteInstance) {
                throw new ValidationException("modelCustomizationId for networkResourceCustomization lookup", true);
            }
        } else {
            // ok for version < 3 and action delete
            if (modelName != null) {
                recipe = catalogDbClient.getFirstNetworkRecipeByModelNameAndAction(modelName, action.toString());
            }
        }

        if (recipe == null) {
            recipe = catalogDbClient.getFirstNetworkRecipeByModelNameAndAction(defaultNetworkType, action.toString());
        }

        return recipe != null ? new RecipeLookupResult(recipe.getOrchestrationUri(), recipe.getRecipeTimeout()) : null;
    }


    private Optional<CloudConfiguration> addCloudConfig(CloudConfiguration sourceCloudConfiguration) {
        CloudConfiguration targetConfiguration = new CloudConfiguration();
        if (sourceCloudConfiguration != null) {
            targetConfiguration.setAicNodeClli(sourceCloudConfiguration.getAicNodeClli());
            targetConfiguration.setTenantId(sourceCloudConfiguration.getTenantId());
            targetConfiguration.setLcpCloudRegionId(sourceCloudConfiguration.getLcpCloudRegionId());
            targetConfiguration.setCloudOwner(sourceCloudConfiguration.getCloudOwner());
            return Optional.of(targetConfiguration);
        }
        return Optional.empty();
    }

    private RecipeLookupResult getVnfOrVfModuleUri(ServiceInstancesRequest servInstReq, Actions action)
            throws ValidationException {

        ModelInfo modelInfo = servInstReq.getRequestDetails().getModelInfo();
        String vnfComponentType = modelInfo.getModelType().name();

        RelatedInstanceList[] instanceList = null;
        if (servInstReq.getRequestDetails() != null) {
            instanceList = servInstReq.getRequestDetails().getRelatedInstanceList();
        }

        Recipe recipe;
        String defaultSource = getDefaultModel(servInstReq);
        String modelCustomizationId = modelInfo.getModelCustomizationId();
        String modelCustomizationName = modelInfo.getModelCustomizationName();
        String relatedInstanceModelVersionId = null;
        String relatedInstanceModelInvariantId = null;
        String relatedInstanceVersion = null;
        String relatedInstanceModelCustomizationName = null;

        if (instanceList != null) {

            for (RelatedInstanceList relatedInstanceList : instanceList) {

                RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
                ModelInfo relatedInstanceModelInfo = relatedInstance.getModelInfo();
                if (relatedInstanceModelInfo.getModelType().equals(ModelType.service)) {
                    relatedInstanceModelVersionId = relatedInstanceModelInfo.getModelVersionId();
                    relatedInstanceVersion = relatedInstanceModelInfo.getModelVersion();
                }

                if (relatedInstanceModelInfo.getModelType().equals(ModelType.vnf)) {
                    relatedInstanceModelVersionId = relatedInstanceModelInfo.getModelVersionId();
                    relatedInstanceModelInvariantId = relatedInstanceModelInfo.getModelInvariantId();
                    relatedInstanceVersion = relatedInstanceModelInfo.getModelVersion();
                    relatedInstanceModelCustomizationName = relatedInstanceModelInfo.getModelCustomizationName();
                }
            }

            if (modelInfo.getModelType().equals(ModelType.vnf)) {
                // a. For a vnf request (only create, no update currently):
                // i. (v3-v4) If modelInfo.modelCustomizationId is provided, use it to validate catalog DB has record in
                // vnf_resource_customization.model_customization_uuid.
                // ii. (v2-v4) If modelInfo.modelCustomizationId is NOT provided (because it is a pre-1702 ASDC model or
                // pre-v3), then modelInfo.modelCustomizationName must have
                // been provided (else create request should be rejected). APIH should use the
                // relatedInstance.modelInfo[service].modelVersionId** + modelInfo[vnf].modelCustomizationName
                // to â€œjoinâ€�? service_to_resource_customizations with vnf_resource_customization to confirm a
                // vnf_resource_customization.model_customization_uuid record exists.
                // **If relatedInstance.modelInfo[service].modelVersionId was not provided, use
                // relatedInstance.modelInfo[service].modelInvariantId + modelVersion instead to lookup modelVersionId
                // (MODEL_UUID) in SERVICE table.
                // iii. Regardless of how the value was provided/obtained above, APIH must always populate
                // vnfModelCustomizationId in bpmnRequest. It would be assumed it was MSO generated
                // during 1707 data migration if VID did not provide it originally on request.
                // iv. Note: continue to construct the â€œvnf-typeâ€�? value and pass to BPMN (must still be populated
                // in A&AI).
                // 1. If modelCustomizationName is NOT provided on a vnf/vfModule request, use modelCustomizationId to
                // look it up in our catalog to construct vnf-type value to pass to BPMN.

                VnfResource vnfResource = null;
                VnfResourceCustomization vrc = null;
                // Validation for vnfResource

                if (modelCustomizationId != null) {
                    vrc = catalogDbClient.getVnfResourceCustomizationByModelCustomizationUUID(modelCustomizationId);
                    if (vrc != null) {
                        vnfResource = vrc.getVnfResources();
                    }
                } else {
                    org.onap.so.db.catalog.beans.Service service =
                            catalogDbClient.getServiceByID(relatedInstanceModelVersionId);
                    if (service == null) {
                        service = catalogDbClient.getServiceByModelVersionAndModelInvariantUUID(relatedInstanceVersion,
                                relatedInstanceModelInvariantId);
                    }

                    if (service == null) {
                        throw new ValidationException("service in relatedInstance");
                    }
                    for (VnfResourceCustomization vnfResourceCustom : service.getVnfCustomizations()) {
                        if (vnfResourceCustom.getModelInstanceName().equals(modelCustomizationName)) {
                            vrc = vnfResourceCustom;
                        }
                    }

                    if (vrc != null) {
                        vnfResource = vrc.getVnfResources();
                        modelInfo.setModelCustomizationId(vrc.getModelCustomizationUUID());
                        modelInfo.setModelCustomizationUuid(vrc.getModelCustomizationUUID());
                    }
                }

                if (vnfResource == null) {
                    throw new ValidationException("vnfResource");
                } else {
                    if (modelInfo.getModelVersionId() == null) {
                        modelInfo.setModelVersionId(vnfResource.getModelUUID());
                    }
                }

                VnfRecipe vnfRecipe = null;

                if (vrc != null) {
                    String nfRole = vrc.getNfRole();
                    if (nfRole != null) {
                        vnfRecipe =
                                catalogDbClient.getFirstVnfRecipeByNfRoleAndAction(vrc.getNfRole(), action.toString());
                    }
                }

                if (vnfRecipe == null) {
                    vnfRecipe = catalogDbClient.getFirstVnfRecipeByNfRoleAndAction(defaultSource, action.toString());
                }

                if (vnfRecipe == null) {
                    return null;
                }

                return new RecipeLookupResult(vnfRecipe.getOrchestrationUri(), vnfRecipe.getRecipeTimeout());
            } else {
                /*
                 * (v5-v7) If modelInfo.modelCustomizationId is NOT provided (because it is a pre-1702 ASDC model or
                 * pre-v3), then modelInfo.modelCustomizationName must have // been provided (else create request should
                 * be rejected). APIH should use the relatedInstance.modelInfo[vnf].modelVersionId +
                 * modelInfo[vnf].modelCustomizationName // to join vnf_to_resource_customizations with
                 * vf_resource_customization to confirm a vf_resource_customization.model_customization_uuid record
                 * exists. // Once the vnfs model_customization_uuid has been obtained, use it to find all vfModule
                 * customizations for that vnf customization in the vnf_res_custom_to_vf_module_custom join table. //
                 * For each vf_module_cust_model_customization_uuid value returned, use that UUID to query
                 * vf_module_customization table along with modelInfo[vfModule|volumeGroup].modelVersionId to // confirm
                 * record matches request data (and to identify the modelCustomizationId associated with the vfModule in
                 * the request). This means taking each record found // in vf_module_customization and looking up in
                 * vf_module (using vf_module_customizationâ€™s FK into vf_module) to find a match on
                 * MODEL_INVARIANT_UUID (modelInvariantId) // and MODEL_VERSION (modelVersion).
                 */
                VfModuleCustomization vfmc = null;
                VnfResource vnfr;
                VnfResourceCustomization vnfrc;
                VfModule vfModule = null;

                if (modelInfo.getModelCustomizationId() != null) {
                    vfmc = catalogDbClient
                            .getVfModuleCustomizationByModelCuztomizationUUID(modelInfo.getModelCustomizationId());
                } else {
                    vnfr = catalogDbClient.getVnfResourceByModelUUID(relatedInstanceModelVersionId);
                    if (vnfr == null) {
                        vnfr = catalogDbClient.getFirstVnfResourceByModelInvariantUUIDAndModelVersion(
                                relatedInstanceModelInvariantId, relatedInstanceVersion);
                    }
                    vnfrc = catalogDbClient.getFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources(
                            relatedInstanceModelCustomizationName, vnfr);

                    List<VfModuleCustomization> list = vnfrc.getVfModuleCustomizations();

                    String vfModuleModelUUID = modelInfo.getModelVersionId();
                    for (VfModuleCustomization vf : list) {
                        VfModuleCustomization vfmCustom;
                        if (vfModuleModelUUID != null) {
                            vfmCustom = catalogDbClient
                                    .getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID(
                                            vf.getModelCustomizationUUID(), vfModuleModelUUID);
                            if (vfmCustom != null) {
                                vfModule = vfmCustom.getVfModule();
                            }
                        } else {
                            vfmCustom = catalogDbClient
                                    .getVfModuleCustomizationByModelCuztomizationUUID(vf.getModelCustomizationUUID());
                            if (vfmCustom != null) {
                                vfModule = vfmCustom.getVfModule();
                            } else {
                                vfModule = catalogDbClient.getVfModuleByModelInvariantUUIDAndModelVersion(
                                        relatedInstanceModelInvariantId, relatedInstanceVersion);
                            }
                        }

                        if (vfModule != null) {
                            modelInfo.setModelCustomizationId(vf.getModelCustomizationUUID());
                            modelInfo.setModelCustomizationUuid(vf.getModelCustomizationUUID());
                            break;
                        }
                    }
                }

                if (vfmc == null && vfModule == null) {
                    throw new ValidationException("vfModuleCustomization");
                } else if (vfModule == null && vfmc != null) {
                    vfModule = vfmc.getVfModule(); // can't be null as vfModuleModelUUID is not-null property in
                    // VfModuleCustomization table
                }

                if (modelInfo.getModelVersionId() == null) {
                    modelInfo.setModelVersionId(vfModule.getModelUUID());
                }


                recipe = catalogDbClient.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(
                        vfModule.getModelUUID(), vnfComponentType, action.toString());
                if (recipe == null) {
                    List<VfModule> vfModuleRecords = catalogDbClient
                            .getVfModuleByModelInvariantUUIDOrderByModelVersionDesc(vfModule.getModelInvariantUUID());
                    if (!vfModuleRecords.isEmpty()) {
                        for (VfModule record : vfModuleRecords) {
                            recipe = catalogDbClient
                                    .getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(
                                            record.getModelUUID(), vnfComponentType, action.toString());
                            if (recipe != null) {
                                break;
                            }
                        }
                    }
                }
                if (recipe == null) {
                    recipe = catalogDbClient.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(
                            defaultSource, vnfComponentType, action.toString());
                    if (recipe == null) {
                        recipe = catalogDbClient.getFirstVnfComponentsRecipeByVnfComponentTypeAndAction(
                                vnfComponentType, action.toString());
                    }

                    if (recipe == null) {
                        return null;
                    }
                }
            }
        } else {

            if (modelInfo.getModelType().equals(ModelType.vnf)) {
                recipe = catalogDbClient.getFirstVnfRecipeByNfRoleAndAction(defaultSource, action.toString());
                if (recipe == null) {
                    return null;
                }
            } else {
                recipe = catalogDbClient.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(
                        defaultSource, vnfComponentType, action.toString());

                if (recipe == null) {
                    return null;
                }
            }
        }

        return new RecipeLookupResult(recipe.getOrchestrationUri(), recipe.getRecipeTimeout());
    }
}
