/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Huawei Technologies.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.http.HttpStatus;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.apihandler.camundabeans.CamundaResponse;
import org.onap.so.apihandler.common.CamundaClient;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandler.common.ResponseHandler;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.serviceintentinstancebeans.ServiceIntentCommonRequest;
import org.onap.so.apihandlerinfra.serviceintentinstancebeans.ServiceIntentCreationRequest;
import org.onap.so.apihandlerinfra.serviceintentinstancebeans.ServiceIntentDeletionRequest;
import org.onap.so.apihandlerinfra.serviceintentinstancebeans.ServiceIntentModificationRequest;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.logger.LogConstants;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.onap.so.serviceinstancebeans.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.function.Function;

/**
 * This class serves as the entry point for Service Intent APIs. Unlike User Intent, which describes Intent using
 * natural languages, Service Intent describes Intent through technology-agnostic and model-driven APIs. The Service
 * Intent APIs have the following format: {service-Intent-Root}/{operation}, where {operation} may be "create",
 * "delete", "modify", etc. And the parameters of the Intent service instance are specified in the payloads of the APIs.
 * <p>
 * For scalability, these APIs are designed to be generic, and thus support all the service Intent use-cases. i.e., The
 * actual intent use-case/application, e.g., Cloud Leased Line, is differentiated by the "serviceType" parameter in the
 * payload, rather than by specific APIs. Thus, this class does not need to grow when we add new Intent use-cases or
 * applications.
 * <p>
 */
@Component
@Path("/onap/so/infra/serviceIntent")
@OpenAPIDefinition(info = @Info(title = "/onap/so/infra/serviceIntent",
        description = "API Requests for Intent services and " + "applications"))
public class ServiceIntentApiHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServiceIntentApiHandler.class);

    private static final String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static final String END_OF_THE_TRANSACTION = "End of the transaction, the final response is: ";

    private static final String SAVE_TO_DB = "save instance to db";

    private static final String URI_PREFIX = "/serviceIntent/";

    @Autowired
    private MsoRequest msoRequest;

    @Autowired
    private CatalogDbClient catalogDbClient;

    @Autowired
    private RequestsDbClient requestsDbClient;

    @Autowired
    private RequestHandlerUtils requestHandlerUtils;

    @Autowired
    private ResponseBuilder builder;

    @Autowired
    private CamundaClient camundaClient;

    @Autowired
    private ResponseHandler responseHandler;

    // @Value("${serviceIntent.config.file}")
    // private String serviceIntentConfigFile;

    /**
     * POST Requests for create Service Intent Instance on a version provided
     *
     * @throws ApiException
     */

    @POST
    @Path("/{version:[vV][1]}/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create a SI Instance on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response createServiceIntentInstance(ServiceIntentCreationRequest request,
            @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        return processServiceIntentRequest(request, Action.createInstance, version, requestId, null,
                requestHandlerUtils.getRequestUri(requestContext, URI_PREFIX));
    }

    /**
     * PUT Requests for Service Intent Modification on a version provided
     *
     * @throws ApiException
     */

    @PUT
    @Path("/{version:[vV][1]}/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Modify a SI Instance on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response modifyServiceIntentInstance(ServiceIntentModificationRequest request,
            @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", request.getServiceInstanceID());
        return processServiceIntentRequest(request, Action.updateInstance, version, requestId, instanceIdMap,
                requestHandlerUtils.getRequestUri(requestContext, URI_PREFIX));
    }

    /**
     * DELETE Requests for Service Intent Instance on a specified version
     *
     * @throws ApiException
     */

    @DELETE
    @Path("/{version:[vV][1]}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Terminate/Delete a SI Service Instance on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response deleteServiceIntentInstance(ServiceIntentDeletionRequest request,
            @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", request.getServiceInstanceID());
        return processServiceIntentRequest(request, Action.deleteInstance, version, requestId, instanceIdMap,
                requestHandlerUtils.getRequestUri(requestContext, URI_PREFIX));
    }

    /**
     * Process Service Intent request and send request to corresponding workflow
     *
     * @param request
     * @param action
     * @param version
     * @return
     * @throws ApiException
     */
    private Response processServiceIntentRequest(ServiceIntentCommonRequest request, Action action, String version,
            String requestId, HashMap<String, String> instanceIdMap, String requestUri) throws ApiException {
        String defaultServiceModelName = "COMMON_SI_DEFAULT";
        String requestScope = ModelType.service.name();
        String apiVersion = version.substring(1);
        String serviceRequestJson = toString.apply(request);

        String instanceName = null;
        String modelUuid = null;
        String serviceInstanceId = null;

        try {
            if (action == Action.createInstance) {
                instanceName = ((ServiceIntentCreationRequest) request).getName();
                modelUuid = ((ServiceIntentCreationRequest) request).getModelUuid();
            } else if (action == Action.updateInstance) {
                instanceName = ((ServiceIntentModificationRequest) request).getName();
                serviceInstanceId = ((ServiceIntentModificationRequest) request).getServiceInstanceID();
            } else if (action == Action.deleteInstance) {
                serviceInstanceId = ((ServiceIntentDeletionRequest) request).getServiceInstanceID();
            }
        } catch (Exception e) {
            logger.error("ERROR: processCllServiceRequest: Exception: ", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, "processCllServiceRequest error", ErrorNumbers.SVC_BAD_PARAMETER,
                    null, version);
            return response;
        }

        if (serviceRequestJson != null) {
            InfraActiveRequests currentActiveReq = createRequestObject(request, action, requestId, Status.IN_PROGRESS,
                    requestScope, serviceRequestJson);

            requestHandlerUtils.checkForDuplicateRequests(action, instanceIdMap, requestScope, currentActiveReq,
                    instanceName);
            try {
                requestsDbClient.save(currentActiveReq);
            } catch (Exception e) {
                logger.error("Exception occurred", e);
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.DataError)
                                .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
                throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(),
                        HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
                                .errorInfo(errorLoggerInfo).build();
            }

            RecipeLookupResult recipeLookupResult;
            try {
                recipeLookupResult = getServiceInstanceOrchestrationURI(modelUuid, action, defaultServiceModelName);
            } catch (Exception e) {
                logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                        ErrorCode.AvailabilityError.getValue(), "Exception while communicate with Catalog DB", e);
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "No " + "communication to catalog DB " + e.getMessage(),
                        ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
                logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
                return response;
            }

            if (recipeLookupResult == null) {
                logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND.toString(),
                        MSO_PROP_APIHANDLER_INFRA, ErrorCode.DataError.getValue(), "No recipe found in DB");
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "Recipe does " + "not exist in catalog DB",
                        ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null, version);
                logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
                return response;
            }

            String serviceInstanceType = request.getSubscriptionServiceType();
            RequestClientParameter parameter;
            try {
                parameter = new RequestClientParameter.Builder().setRequestId(requestId).setBaseVfModule(false)
                        .setRecipeTimeout(recipeLookupResult.getRecipeTimeout()).setRequestAction(action.name())
                        .setServiceInstanceId(serviceInstanceId).setServiceType(serviceInstanceType)
                        .setRequestDetails(serviceRequestJson).setApiVersion(version).setALaCarte(false)
                        .setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).setApiVersion(apiVersion).build();
            } catch (Exception e) {
                logger.error("Exception occurred", e);
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.SchemaError)
                                .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
                throw new ValidateException.Builder("Unable to generate RequestClientParameter object" + e.getMessage(),
                        HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo)
                                .build();
            }
            return postBPELRequest(currentActiveReq, parameter, recipeLookupResult.getOrchestrationURI(), requestScope);
        } else {
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, "JsonProcessingException occurred - " + "serviceRequestJson is null",
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            return response;
        }
    }

    /**
     * Getting recipes from catalogDb
     *
     * @param serviceModelUUID the service model version uuid
     * @param action the action for the service
     * @param defaultServiceModelName default service name
     * @return the service recipe result
     */
    private RecipeLookupResult getServiceInstanceOrchestrationURI(String serviceModelUUID, Action action,
            String defaultServiceModelName) {

        RecipeLookupResult recipeLookupResult = getServiceURI(serviceModelUUID, action, defaultServiceModelName);

        if (recipeLookupResult != null) {
            logger.debug("Orchestration URI is: " + recipeLookupResult.getOrchestrationURI() + ", recipe Timeout is: "
                    + Integer.toString(recipeLookupResult.getRecipeTimeout()));
        } else {
            logger.debug("No matching recipe record found");
        }
        return recipeLookupResult;
    }

    /**
     * Getting recipes from catalogDb If Service recipe is not set, use default recipe, if set , use special recipe.
     *
     * @param serviceModelUUID the service version uuid
     * @param action the action of the service.
     * @param defaultServiceModelName default service name
     * @return the service recipe result.
     */
    private RecipeLookupResult getServiceURI(String serviceModelUUID, Action action, String defaultServiceModelName) {

        Service defaultServiceRecord =
                catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
        // set recipe as default generic recipe
        ServiceRecipe recipe =
                catalogDbClient.getFirstByServiceModelUUIDAndAction(defaultServiceRecord.getModelUUID(), action.name());
        // check the service special recipe
        if (null != serviceModelUUID && !serviceModelUUID.isEmpty()) {
            ServiceRecipe serviceSpecialRecipe =
                    catalogDbClient.getFirstByServiceModelUUIDAndAction(serviceModelUUID, action.name());
            if (null != serviceSpecialRecipe) {
                // set service special recipe.
                recipe = serviceSpecialRecipe;
            }
        }

        if (recipe == null) {
            return null;
        }
        return new RecipeLookupResult(recipe.getOrchestrationUri(), recipe.getRecipeTimeout(), recipe.getParamXsd());

    }

    Function<Object, String> toString = serviceRequest -> {
        ObjectMapper mapper = new ObjectMapper();
        String requestAsString = null;
        try {
            requestAsString = mapper.writeValueAsString(serviceRequest);
        } catch (JsonProcessingException e) {
            logger.debug("Exception while converting service request object to String {}", e);
        }
        return requestAsString;
    };

    private InfraActiveRequests createRequestObject(ServiceIntentCommonRequest request, Action action, String requestId,
            Status status, String requestScope, String requestJson) {
        InfraActiveRequests aq = new InfraActiveRequests();
        try {
            String serviceInstanceName = null;
            String serviceInstanceId = null;
            String serviceType = request.getServiceType();
            if (action.name().equals("createInstance")) {
                serviceInstanceName = ((ServiceIntentCreationRequest) request).getName();
                aq.setServiceInstanceName(serviceInstanceName);
            } else if (action.name().equals("updateInstance")) {
                serviceInstanceName = ((ServiceIntentModificationRequest) request).getName();
                serviceInstanceId = ((ServiceIntentModificationRequest) request).getServiceInstanceID();
                aq.setServiceInstanceName(serviceInstanceName);
                aq.setServiceInstanceId(serviceInstanceId);
            } else if (action.name().equals("deleteInstance")) {
                serviceInstanceId = ((ServiceIntentDeletionRequest) request).getServiceInstanceID();
                aq.setServiceInstanceId(serviceInstanceId);
            }

            aq.setRequestId(requestId);
            aq.setRequestAction(action.toString());
            aq.setRequestUrl(MDC.get(LogConstants.HTTP_URL));
            Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
            aq.setStartTime(startTimeStamp);
            aq.setRequestScope(requestScope);
            aq.setRequestBody(requestJson);
            aq.setRequestStatus(status.toString());
            aq.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
            aq.setServiceType(serviceType);
        } catch (Exception e) {
            logger.error("Exception when creation record request", e);

            if (!status.equals(Status.FAILED)) {
                throw e;
            }
        }
        return aq;
    }

    private Response postBPELRequest(InfraActiveRequests currentActiveReq, RequestClientParameter parameter,
            String orchestrationURI, String requestScope) throws ApiException {
        ResponseEntity<String> response =
                requestHandlerUtils.postRequest(currentActiveReq, parameter, orchestrationURI);
        logger.debug("BPEL response : " + response);
        int bpelStatus = responseHandler.setStatus(response.getStatusCodeValue());
        String jsonResponse;
        try {
            responseHandler.acceptedResponse(response);
            CamundaResponse camundaResponse = responseHandler.getCamundaResponse(response);
            String responseBody = camundaResponse.getResponse();
            if ("Success".equalsIgnoreCase(camundaResponse.getMessage())) {
                jsonResponse = responseBody;
            } else {
                BPMNFailureException bpmnException =
                        new BPMNFailureException.Builder(String.valueOf(bpelStatus) + responseBody, bpelStatus,
                                ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).build();
                requestHandlerUtils.updateStatus(currentActiveReq, Status.FAILED, bpmnException.getMessage());
                throw bpmnException;
            }
        } catch (ApiException e) {
            requestHandlerUtils.updateStatus(currentActiveReq, Status.FAILED, e.getMessage());
            throw e;
        }
        return builder.buildResponse(HttpStatus.SC_ACCEPTED, parameter.getRequestId(), jsonResponse,
                parameter.getApiVersion());
    }
}
