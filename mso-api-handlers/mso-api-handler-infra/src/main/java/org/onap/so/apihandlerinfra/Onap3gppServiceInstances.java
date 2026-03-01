/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.apihandler.camundabeans.CamundaResponse;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandler.common.ResponseHandler;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.ActivateOrDeactivate3gppService;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.Allocate3gppService;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.DeAllocate3gppService;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.Modify3gppService;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.QuerySubnetCapability;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.SubnetTypes;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.client.CatalogDbClient;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;

@Component
@Path("/onap/so/infra/3gppservices")
@OpenAPIDefinition(
        info = @Info(title = "/onap/so/infra/3gppservices", description = "API Requests for 3GPP Service Instances"))
public class Onap3gppServiceInstances {

    private static final Logger logger = LoggerFactory.getLogger(Onap3gppServiceInstances.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static final String END_OF_THE_TRANSACTION = "End of the transaction, the final response is: ";

    private static final String SAVE_TO_DB = "save instance to db";

    private static final String URI_PREFIX = "/3gppservices/";

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
    private ResponseHandler responseHandler;

    @Value("${subnetCapability.config.file}")
    private String subnetCapabilityConfigFile;

    /**
     * POST Requests for 3GPP Service create Instance on a version provided
     *
     * @throws ApiException
     */

    @POST
    @Path("/{version:[vV][1]}/allocate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create a 3GPP Service Instance on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response createServiceInstance(Allocate3gppService request, @PathParam("version") String version,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        return processServiceInstanceRequest(request, Action.createInstance, version, requestId, null,
                requestHandlerUtils.getRequestUri(requestContext, URI_PREFIX));
    }

    /**
     * PUT Requests for 3GPP Service update Instance on a version provided
     *
     * @throws ApiException
     */

    @PUT
    @Path("/{version:[vV][1]}/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Modify a 3GPP Service Instance on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response updateServiceInstance(Modify3gppService request, @PathParam("version") String version,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", request.getServiceInstanceID());
        return updateServiceInstances(request, Action.updateInstance, version, requestId, instanceIdMap,
                requestHandlerUtils.getRequestUri(requestContext, URI_PREFIX));
    }

    /**
     * DELETE Requests for 3GPP Service delete Instance on a specified version
     *
     * @throws ApiException
     */

    @DELETE
    @Path("/{version:[vV][1]}/deAllocate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Terminate/Deallocate a 3GPP Service Instance on a version provided",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response deleteServiceInstance(DeAllocate3gppService request, @PathParam("version") String version,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", request.getServiceInstanceID());
        return deleteServiceInstances(request, Action.deleteInstance, version, requestId, instanceIdMap,
                requestHandlerUtils.getRequestUri(requestContext, URI_PREFIX));
    }

    /**
     * POST Requests for 3GPP Service Activate on a specified version
     *
     * @throws ApiException
     */

    @POST
    @Path("/{version:[vV][1]}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Activate a 3GPP Service Instance on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response activateServiceInstance(ActivateOrDeactivate3gppService request,
            @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", request.getServiceInstanceID());
        return activateOrDeactivateServiceInstances(request, Action.activateInstance, version, requestId, instanceIdMap,
                requestHandlerUtils.getRequestUri(requestContext, URI_PREFIX));
    }

    /**
     * POST Requests for 3GPP Service DeActivate on a specified version
     *
     * @throws ApiException
     */

    @POST
    @Path("/{version:[vV][1]}/deActivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Deactivate a 3GPP Service Instance on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response deActivateServiceInstance(ActivateOrDeactivate3gppService request,
            @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", request.getServiceInstanceID());
        return activateOrDeactivateServiceInstances(request, Action.deactivateInstance, version, requestId,
                instanceIdMap, requestHandlerUtils.getRequestUri(requestContext, URI_PREFIX));
    }

    /**
     *
     * GET requests for slice subnet capabilities on a specified version
     *
     * @param version
     * @return
     * @throws ApiException
     */
    @GET
    @Path("/{version:[vV][1]}/subnetCapabilityQuery")
    @Operation(description = "Provides subnet capability based on subnet types", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSliceSubnetCapabilities(QuerySubnetCapability request, @PathParam("version") String version)
            throws ApiException {
        logger.debug("Request received {}", request);
        List<SubnetTypes> subnetTypes = request.getSubnetTypes();
        return getSubnetCapabilities(subnetTypes, version);
    }

    /**
     * Process allocate service request and send request to corresponding workflow
     *
     * @param request
     * @param action
     * @param version
     * @return
     * @throws ApiException
     */
    private Response processServiceInstanceRequest(Allocate3gppService request, Action action, String version,
            String requestId, HashMap<String, String> instanceIdMap, String requestUri) throws ApiException {
        String defaultServiceModelName = "COMMON_SS_DEFAULT";
        String requestScope = ModelType.service.name();
        String apiVersion = version.substring(1);
        String serviceRequestJson = toString.apply(request);
        if (serviceRequestJson != null) {
            InfraActiveRequests currentActiveReq = createRequestObject(request, action, requestId, Status.IN_PROGRESS,
                    requestScope, serviceRequestJson);
            String instanceName = request.getName();
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
                recipeLookupResult =
                        getServiceInstanceOrchestrationURI(request.getModelUuid(), action, defaultServiceModelName);
            } catch (Exception e) {
                logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                        ErrorCode.AvailabilityError.getValue(), "Exception while communciate with Catalog DB", e);
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
                        ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
                logger.debug("{}{}", END_OF_THE_TRANSACTION, response.getEntity());
                return response;
            }

            if (recipeLookupResult == null) {
                logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND.toString(),
                        MSO_PROP_APIHANDLER_INFRA, ErrorCode.DataError.getValue(), "No recipe found in DB");
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "Recipe does not exist in catalog DB",
                        ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null, version);
                logger.debug("{}{}", END_OF_THE_TRANSACTION, response.getEntity());
                return response;
            }

            String serviceInstanceType = request.getSubscriptionServiceType();
            RequestClientParameter parameter;
            try {
                parameter = new RequestClientParameter.Builder().setRequestId(requestId).setBaseVfModule(false)
                        .setRecipeTimeout(recipeLookupResult.getRecipeTimeout()).setRequestAction(action.name())
                        .setServiceInstanceId(null).setServiceType(serviceInstanceType)
                        .setRequestDetails(serviceRequestJson).setApiVersion(version).setALaCarte(false)
                        .setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).setApiVersion(apiVersion).build();
            } catch (Exception e) {
                logger.error("Exception occurred", e);
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.SchemaError)
                                .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
                throw new ValidateException.Builder("Unable to generate RequestClientParamter object" + e.getMessage(),
                        HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo)
                                .build();
            }
            return postBPELRequest(currentActiveReq, parameter, recipeLookupResult.getOrchestrationURI(), requestScope);
        } else {
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, "JsonProcessingException occurred - serviceRequestJson is null",
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            return response;
        }
    }

    /**
     * process modify service request and call corresponding workflow
     *
     * @param request
     * @param action
     * @param version
     * @return
     * @throws ApiException
     */
    private Response updateServiceInstances(Modify3gppService request, Action action, String version, String requestId,
            HashMap<String, String> instanceIdMap, String requestUri) throws ApiException {
        String defaultServiceModelName = "COMMON_SS_DEFAULT";
        String requestScope = ModelType.service.name();
        String apiVersion = version.substring(1);
        String serviceRequestJson = toString.apply(request);
        if (serviceRequestJson != null) {
            InfraActiveRequests currentActiveReq = createRequestObject(request, action, requestId, Status.IN_PROGRESS,
                    requestScope, serviceRequestJson);
            String instanceName = request.getName();
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
                recipeLookupResult = getServiceInstanceOrchestrationURI(null, action, defaultServiceModelName);
            } catch (Exception e) {
                logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                        ErrorCode.AvailabilityError.getValue(), "Exception while communciate with Catalog DB", e);
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
                        ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
                logger.debug("{}{}", END_OF_THE_TRANSACTION, response.getEntity());
                return response;
            }

            if (recipeLookupResult == null) {
                logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND.toString(),
                        MSO_PROP_APIHANDLER_INFRA, ErrorCode.DataError.getValue(), "No recipe found in DB");
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "Recipe does not exist in catalog DB",
                        ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null, version);
                logger.debug("{}{}", END_OF_THE_TRANSACTION, response.getEntity());
                return response;
            }

            String serviceInstanceType = request.getSubscriptionServiceType();
            String serviceInstanceId = request.getServiceInstanceID();
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
                throw new ValidateException.Builder("Unable to generate RequestClientParamter object" + e.getMessage(),
                        HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo)
                                .build();
            }
            return postBPELRequest(currentActiveReq, parameter, recipeLookupResult.getOrchestrationURI(), requestScope);

        } else {
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, "JsonProcessingException occurred - serviceRequestJson is null",
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            return response;
        }
    }

    /**
     * process delete service instance request and call corresponding workflow
     *
     * @param request
     * @param action
     * @param version
     * @return
     * @throws ApiException
     */
    private Response deleteServiceInstances(DeAllocate3gppService request, Action action, String version,
            String requestId, HashMap<String, String> instanceIdMap, String requestUri) throws ApiException {
        String defaultServiceModelName = "COMMON_SS_DEFAULT";
        String requestScope = ModelType.service.name();
        String apiVersion = version.substring(1);
        String serviceRequestJson = toString.apply(request);
        if (serviceRequestJson != null) {
            InfraActiveRequests currentActiveReq = createRequestObject(request, action, requestId, Status.IN_PROGRESS,
                    requestScope, serviceRequestJson);
            requestHandlerUtils.checkForDuplicateRequests(action, instanceIdMap, requestScope, currentActiveReq, null);
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
                recipeLookupResult = getServiceInstanceOrchestrationURI(null, action, defaultServiceModelName);
            } catch (Exception e) {
                logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                        ErrorCode.AvailabilityError.getValue(), "Exception while communciate with Catalog DB", e);
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
                        ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
                logger.debug("{}{}", END_OF_THE_TRANSACTION, response.getEntity());
                return response;
            }

            if (recipeLookupResult == null) {
                logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND.toString(),
                        MSO_PROP_APIHANDLER_INFRA, ErrorCode.DataError.getValue(), "No recipe found in DB");
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "Recipe does not exist in catalog DB",
                        ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null, version);
                logger.debug("{}{}", END_OF_THE_TRANSACTION, response.getEntity());
                return response;
            }

            String serviceInstanceType = request.getSubscriptionServiceType();
            String serviceInstanceId = request.getServiceInstanceID();
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
                throw new ValidateException.Builder("Unable to generate RequestClientParamter object" + e.getMessage(),
                        HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo)
                                .build();
            }
            return postBPELRequest(currentActiveReq, parameter, recipeLookupResult.getOrchestrationURI(), requestScope);
        } else {
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, "JsonProcessingException occurred - serviceRequestJson is null",
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            return response;
        }
    }

    /**
     * process activate/deactivate service request and call corresponding workflow
     *
     * @param request the request object for activate/deactivate service
     * @param action the action for the service
     * @param version
     * @return
     * @throws ApiException
     */
    private Response activateOrDeactivateServiceInstances(ActivateOrDeactivate3gppService request, Action action,
            String version, String requestId, HashMap<String, String> instanceIdMap, String requestUri)
            throws ApiException {
        String defaultServiceModelName = "COMMON_SS_DEFAULT";
        String requestScope = ModelType.service.name();
        String apiVersion = version.substring(1);
        String serviceRequestJson = toString.apply(request);
        if (serviceRequestJson != null) {
            InfraActiveRequests currentActiveReq = createRequestObject(request, action, requestId, Status.IN_PROGRESS,
                    requestScope, serviceRequestJson);
            if (action == Action.activateInstance) {
                requestHandlerUtils.checkForDuplicateRequests(action, instanceIdMap, requestScope, currentActiveReq,
                        request.getServiceInstanceID());
            } else {
                requestHandlerUtils.checkForDuplicateRequests(action, instanceIdMap, requestScope, currentActiveReq,
                        null);
            }
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
                recipeLookupResult = getServiceInstanceOrchestrationURI(null, action, defaultServiceModelName);
            } catch (Exception e) {
                logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                        ErrorCode.AvailabilityError.getValue(), "Exception while communciate with Catalog DB", e);
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
                        ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
                logger.debug("{}{}", END_OF_THE_TRANSACTION, response.getEntity());
                return response;
            }

            if (recipeLookupResult == null) {
                logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND.toString(),
                        MSO_PROP_APIHANDLER_INFRA, ErrorCode.DataError.getValue(), "No recipe found in DB");
                Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                        MsoException.ServiceException, "Recipe does not exist in catalog DB",
                        ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null, version);
                logger.debug("{}{}", END_OF_THE_TRANSACTION, response.getEntity());
                return response;
            }

            String serviceInstanceType = request.getSubscriptionServiceType();
            String serviceInstanceId = request.getServiceInstanceID();
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
                throw new ValidateException.Builder("Unable to generate RequestClientParamter object" + e.getMessage(),
                        HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo)
                                .build();
            }
            return postBPELRequest(currentActiveReq, parameter, recipeLookupResult.getOrchestrationURI(), requestScope);
        } else {
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, "JsonProcessingException occurred - serviceRequestJson is null",
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            return response;
        }
    }

    private Response getSubnetCapabilities(List<SubnetTypes> subnetTypes, String version) {
        String inputFileString = "";
        Map<String, Object> subnetCapability = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(subnetCapabilityConfigFile)))) {
            logger.debug("Reading SubnetCapability file");
            StringBuilder sb = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = bufferedReader.readLine();
            }
            inputFileString = sb.toString();
            subnetCapability = mapper.readValue(inputFileString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.debug("Exception while reading subnet capability value from json", e);
        }
        Map<String, Object> responseMap = new HashMap<>();
        for (SubnetTypes value : subnetTypes) {
            if (subnetCapability.containsKey(value.toString())) {
                responseMap.put(value.toString(), subnetCapability.get(value.toString()));
            }
        }
        String response = null;
        try {
            response = mapper.writeValueAsString(responseMap);
        } catch (JsonProcessingException e) {
            logger.debug("Exception while converting subnet capability object to String {}", e);
        }
        return builder.buildResponse(HttpStatus.SC_OK, null, response, version);
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
            logger.debug("Orchestration URI is: {}, recipe Timeout is: {}", recipeLookupResult.getOrchestrationURI(),
                    Integer.toString(recipeLookupResult.getRecipeTimeout()));
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
        String requestAsString = null;
        try {
            requestAsString = mapper.writeValueAsString(serviceRequest);
        } catch (JsonProcessingException e) {
            logger.debug("Exception while converting service request object to String {}", e);
        }
        return requestAsString;
    };

    public InfraActiveRequests createRequestObject(Object request, Action action, String requestId, Status status,
            String requestScope, String requestJson) {
        InfraActiveRequests aq = new InfraActiveRequests();
        try {
            String networkType = null;
            String serviceInstanceName = null;
            String serviceInstanceId = null;
            if (action.name().equals("createInstance")) {
                networkType = ((Allocate3gppService) request).getNetworkType();
                serviceInstanceName = ((Allocate3gppService) request).getName();
                aq.setServiceInstanceName(serviceInstanceName);
            } else if (action.name().equals("updateInstance")) {
                networkType = ((Modify3gppService) request).getNetworkType();
                serviceInstanceName = ((Modify3gppService) request).getName();
                serviceInstanceId = ((Modify3gppService) request).getServiceInstanceID();
                aq.setServiceInstanceName(serviceInstanceName);
                aq.setServiceInstanceId(serviceInstanceId);
            } else if (action.name().equals("deleteInstance")) {
                networkType = ((DeAllocate3gppService) request).getNetworkType();
                serviceInstanceId = ((DeAllocate3gppService) request).getServiceInstanceID();
                aq.setServiceInstanceId(serviceInstanceId);
            } else if (action.name().equals("activateInstance")) {
                networkType = ((ActivateOrDeactivate3gppService) request).getNetworkType();
                serviceInstanceId = ((ActivateOrDeactivate3gppService) request).getServiceInstanceID();
                aq.setServiceInstanceName(serviceInstanceId); // setting serviceInstanceId as serviceInstanceName
                                                              // -->serviceInstanceName shouldn't be null for action -
                                                              // activateInstance duplicateRequests check
                aq.setServiceInstanceId(serviceInstanceId);
            } else if (action.name().equals("deactivateInstance")) {
                networkType = ((ActivateOrDeactivate3gppService) request).getNetworkType();
                serviceInstanceId = ((ActivateOrDeactivate3gppService) request).getServiceInstanceID();
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
            aq.setNetworkType(networkType);
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
        logger.debug("BPEL response : {}", response);
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
