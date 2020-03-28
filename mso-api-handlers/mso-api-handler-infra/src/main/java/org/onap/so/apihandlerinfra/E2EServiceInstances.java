/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
import java.util.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.onap.so.apihandlerinfra.e2eserviceinstancebeans.*;
import org.onap.aai.domain.yang.v16.ServiceInstance;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.logger.LoggingAnchor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClient;
import org.onap.so.apihandler.common.RequestClientFactory;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandler.common.ResponseHandler;
import org.onap.so.apihandlerinfra.e2eserviceinstancebeans.CompareModelsRequest;
import org.onap.so.apihandlerinfra.e2eserviceinstancebeans.E2EServiceInstanceDeleteRequest;
import org.onap.so.apihandlerinfra.e2eserviceinstancebeans.E2EServiceInstanceRequest;
import org.onap.so.apihandlerinfra.e2eserviceinstancebeans.E2EServiceInstanceScaleRequest;
import org.onap.so.apihandlerinfra.e2eserviceinstancebeans.GetE2EServiceInstanceResponse;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.SubscriberInfo;
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
@Path("/onap/so/infra/e2eServiceInstances")
@OpenAPIDefinition(info = @Info(title = "/onap/so/infra/e2eServiceInstances",
        description = "API Requests for E2E Service Instances"))

public class E2EServiceInstances {

    private HashMap<String, String> instanceIdMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(E2EServiceInstances.class);

    private static final String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static final String END_OF_THE_TRANSACTION = "End of the transaction, the final response is: ";

    private static final String SERVICE_ID = "serviceId";

    @Autowired
    private MsoRequest msoRequest;

    @Autowired
    private RequestClientFactory requestClientFactory;

    @Autowired
    private RequestsDbClient requestsDbClient;

    @Autowired
    private CatalogDbClient catalogDbClient;

    @Autowired
    private ResponseBuilder builder;

    /**
     * POST Requests for E2E Service create Instance on a version provided
     * 
     * @throws ApiException
     */

    @POST
    @Path("/{version:[vV][3-5]}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create an E2E Service Instance on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response createE2EServiceInstance(String request, @PathParam("version") String version) throws ApiException {

        return processE2EserviceInstances(request, Action.createInstance, null, version);
    }

    /**
     * PUT Requests for E2E Service update Instance on a version provided
     * 
     * @throws ApiException
     */

    @PUT
    @Path("/{version:[vV][3-5]}/{serviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update an E2E Service Instance on a version provided and serviceId",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response updateE2EServiceInstance(String request, @PathParam("version") String version,
            @PathParam("serviceId") String serviceId) throws ApiException {

        instanceIdMap.put(SERVICE_ID, serviceId);

        return updateE2EserviceInstances(request, Action.updateInstance, version);
    }

    /**
     * DELETE Requests for E2E Service delete Instance on a specified version and serviceId
     * 
     * @throws ApiException
     */

    @DELETE
    @Path("/{version:[vV][3-5]}/{serviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete E2E Service Instance on a specified version and serviceId",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response deleteE2EServiceInstance(String request, @PathParam("version") String version,
            @PathParam(SERVICE_ID) String serviceId) throws ApiException {

        instanceIdMap.put(SERVICE_ID, serviceId);

        return deleteE2EserviceInstances(request, Action.deleteInstance, instanceIdMap, version);
    }

    /**
     * Activate Requests for 5G slice Service on a specified version and serviceId
     *
     * @throws ApiException
     */

    @POST
    @Path("/{version:[vV][3-5]}/{serviceId}/{operationType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Activate 5G slice Service on a specified version and serviceId", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response Activate5GSliceServiceInstance(String request, @PathParam("version") String version,
            @PathParam("operationType") String operationType, @PathParam(SERVICE_ID) String serviceId)
            throws ApiException {
        if (operationType.equals("activate")) {
            instanceIdMap.put("operationType", "activation");
        } else {
            instanceIdMap.put("operationType", "deactivation");
        }
        instanceIdMap.put(SERVICE_ID, serviceId);
        return Activate5GSliceServiceInstances(request, Action.activateInstance, instanceIdMap, version);
    }

    @GET
    @Path("/{version:[vV][3-5]}/{serviceId}/operations/{operationId}")
    @Operation(description = "Find e2eServiceInstances Requests for a given serviceId and operationId",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Produces(MediaType.APPLICATION_JSON)
    public Response getE2EServiceInstances(@PathParam(SERVICE_ID) String serviceId,
            @PathParam("version") String version, @PathParam("operationId") String operationId) {
        return getE2EServiceInstance(serviceId, operationId, version);
    }

    /**
     * Scale Requests for E2E Service scale Instance on a specified version
     * 
     * @throws ApiException
     */

    @POST
    @Path("/{version:[vV][3-5]}/{serviceId}/scale")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Scale E2E Service Instance on a specified version", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response scaleE2EServiceInstance(String request, @PathParam("version") String version,
            @PathParam(SERVICE_ID) String serviceId) throws ApiException {

        logger.debug("------------------scale begin------------------");
        instanceIdMap.put(SERVICE_ID, serviceId);
        return scaleE2EserviceInstances(request, Action.scaleInstance, version);
    }

    /**
     * GET Requests for Comparing model of service instance with target version
     * 
     * @throws ApiException
     */

    @POST
    @Path("/{version:[vV][3-5]}/{serviceId}/modeldifferences")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            description = "Find added and deleted resources of target model for the e2eserviceInstance on a given serviceId ",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response compareModelwithTargetVersion(String request, @PathParam("serviceId") String serviceId,
            @PathParam("version") String version) throws ApiException {

        instanceIdMap.put(SERVICE_ID, serviceId);

        return compareModelwithTargetVersion(request, Action.compareModel, instanceIdMap, version);
    }

    private Response compareModelwithTargetVersion(String requestJSON, Action action,
            HashMap<String, String> instanceIdMap, String version) throws ApiException {

        String requestId = UUID.randomUUID().toString();

        CompareModelsRequest e2eCompareModelReq;

        ObjectMapper mapper = new ObjectMapper();
        try {
            e2eCompareModelReq = mapper.readValue(requestJSON, CompareModelsRequest.class);

        } catch (Exception e) {

            logger.debug("Mapping of request to JSON object failed : ", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
                    MsoException.ServiceException, "Mapping of request to JSON object failed.  " + e.getMessage(),
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_REQUEST_VALIDATION_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.SchemaError.getValue(), requestJSON, e);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity().toString());

            return response;
        }

        return runCompareModelBPMWorkflow(e2eCompareModelReq, requestJSON, requestId, action, version);

    }

    private Response runCompareModelBPMWorkflow(CompareModelsRequest e2eCompareModelReq, String requestJSON,
            String requestId, Action action, String version) throws ApiException {

        // Define RecipeLookupResult info here instead of query DB for efficiency
        String workflowUrl = "/mso/async/services/CompareModelofE2EServiceInstance";
        int recipeTimeout = 180;

        RequestClient requestClient;
        HttpResponse response;

        try {
            requestClient = requestClientFactory.getRequestClient(workflowUrl);

            JSONObject jjo = new JSONObject(requestJSON);
            String bpmnRequest = jjo.toString();

            // Capture audit event
            logger.debug("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl());
            String serviceId = instanceIdMap.get(SERVICE_ID);
            String serviceType = e2eCompareModelReq.getServiceType();
            RequestClientParameter postParam = new RequestClientParameter.Builder().setRequestId(requestId)
                    .setBaseVfModule(false).setRecipeTimeout(recipeTimeout).setRequestAction(action.name())
                    .setServiceInstanceId(serviceId).setServiceType(serviceType).setRequestDetails(bpmnRequest)
                    .setALaCarte(false).build();
            response = requestClient.post(postParam);
        } catch (Exception e) {
            Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY,
                    MsoException.ServiceException, "Failed calling bpmn " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
            logger.error("", MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
                    ErrorCode.AvailabilityError, "Exception while communicate with BPMN engine", e);
            logger.debug(END_OF_THE_TRANSACTION + resp.getEntity().toString());
            return resp;
        }

        if (response == null) {
            Response resp =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY, MsoException.ServiceException,
                            "bpelResponse is null", ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.BusinessProcessError.getValue(), "Null response from BPEL");
            logger.debug(END_OF_THE_TRANSACTION + resp.getEntity().toString());
            return resp;
        }

        ResponseHandler respHandler = new ResponseHandler(response, requestClient.getType());
        int bpelStatus = respHandler.getStatus();

        return beplStatusUpdate(requestClient, respHandler, bpelStatus, version);
    }

    private Response getE2EServiceInstance(String serviceId, String operationId, String version) {

        GetE2EServiceInstanceResponse e2eServiceResponse = new GetE2EServiceInstanceResponse();

        String apiVersion = version.substring(1);

        OperationStatus operationStatus;

        try {
            operationStatus = requestsDbClient.getOneByServiceIdAndOperationId(serviceId, operationId);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(),
                    "Exception while communciate with Request DB - Infra Request Lookup", e);
            Response response =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND, MsoException.ServiceException,
                            e.getMessage(), ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB, null, version);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;

        }

        if (operationStatus == null) {
            Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NO_CONTENT,
                    MsoException.ServiceException, "E2E serviceId " + serviceId + " is not found in DB",
                    ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.BusinessProcessError.getValue(),
                    "Null response from RequestDB when searching by serviceId");
            logger.debug(END_OF_THE_TRANSACTION + resp.getEntity());
            return resp;

        }

        e2eServiceResponse.setOperation(operationStatus);

        return builder.buildResponse(HttpStatus.SC_OK, null, e2eServiceResponse, apiVersion);
    }

    private Response Activate5GSliceServiceInstances(String requestJSON, Action action,
            HashMap<String, String> instanceIdMap, String version) throws ApiException {
        // TODO should be a new one or the same service instance Id
        E2ESliceServiceActivateRequest e2eActReq;

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            e2eActReq = mapper.readValue(requestJSON, E2ESliceServiceActivateRequest.class);

        } catch (Exception e) {

            logger.debug("Mapping of request to JSON object failed : ", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
                    MsoException.ServiceException, "Mapping of request to JSON object failed.  " + e.getMessage(),
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_REQUEST_VALIDATION_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.SchemaError.getValue(), requestJSON, e);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        String requestId = UUID.randomUUID().toString();
        RecipeLookupResult recipeLookupResult;
        try {
            // TODO Get the service template model version uuid from AAI.
            String modelVersionId = null;
            AAIResourcesClient client = new AAIResourcesClient();
            AAIResourceUri url = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                    e2eActReq.getGlobalSubscriberId(), e2eActReq.getServiceType(), instanceIdMap.get(SERVICE_ID));
            Optional<ServiceInstance> serviceInstanceOpt = client.get(ServiceInstance.class, url);
            if (serviceInstanceOpt.isPresent()) {
                modelVersionId = serviceInstanceOpt.get().getModelVersionId();
            }
            recipeLookupResult = getServiceInstanceOrchestrationURI(modelVersionId, action);
        } catch (Exception e) {
            logger.error(MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(), "Exception while communciate with Catalog DB", e);

            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                    MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);

            msoRequest.createErrorRequestRecord(Status.FAILED, requestId,
                    "Exception while communciate with " + "Catalog DB", action, ModelType.service.name(), requestJSON);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }
        if (recipeLookupResult == null) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.DataError.getValue(), "No recipe found in DB");
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                    MsoException.ServiceException, "Recipe does not exist in catalog DB",
                    ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null, version);

            msoRequest.createErrorRequestRecord(Status.FAILED, requestId, "Recipe does not exist in catalog DB", action,
                    ModelType.service.name(), requestJSON);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        RequestClient requestClient;
        HttpResponse response;

        try {
            requestClient = requestClientFactory.getRequestClient(recipeLookupResult.getOrchestrationURI());

            JSONObject jjo = new JSONObject(requestJSON);
            jjo.put("operationId", requestId);

            String bpmnRequest = jjo.toString();

            // Capture audit event
            logger.debug("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl());
            String serviceId = instanceIdMap.get(SERVICE_ID);
            String operationType = instanceIdMap.get("operationType");
            String serviceInstanceType = e2eActReq.getServiceType();
            RequestClientParameter clientParam = new RequestClientParameter.Builder().setRequestId(requestId)
                    .setBaseVfModule(false).setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                    .setRequestAction(action.name()).setServiceInstanceId(serviceId).setOperationType(operationType)
                    .setServiceType(serviceInstanceType).setRequestDetails(bpmnRequest).setApiVersion(version)
                    .setALaCarte(false).setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).build();
            response = requestClient.post(clientParam);

        } catch (Exception e) {
            Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY,
                    MsoException.ServiceException, "Failed calling bpmn " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.AvailabilityError.getValue(),
                    "Exception while communicate with BPMN engine");
            logger.debug("End of the transaction, the final response is: " + resp.getEntity());
            return resp;
        }

        if (response == null) {
            Response resp =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY, MsoException.ServiceException,
                            "bpelResponse is null", ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.BusinessProcessError.getValue(), "Null response from BPEL");
            logger.debug(END_OF_THE_TRANSACTION + resp.getEntity());
            return resp;
        }

        ResponseHandler respHandler = new ResponseHandler(response, requestClient.getType());
        int bpelStatus = respHandler.getStatus();

        return beplStatusUpdate(requestClient, respHandler, bpelStatus, version);
    }

    private Response deleteE2EserviceInstances(String requestJSON, Action action, HashMap<String, String> instanceIdMap,
            String version) throws ApiException {
        // TODO should be a new one or the same service instance Id
        E2EServiceInstanceDeleteRequest e2eDelReq;

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            e2eDelReq = mapper.readValue(requestJSON, E2EServiceInstanceDeleteRequest.class);

        } catch (Exception e) {

            logger.debug("Mapping of request to JSON object failed : ", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
                    MsoException.ServiceException, "Mapping of request to JSON object failed.  " + e.getMessage(),
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_REQUEST_VALIDATION_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.SchemaError.getValue(), requestJSON, e);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        String requestId = UUID.randomUUID().toString();
        RecipeLookupResult recipeLookupResult;
        try {
            // TODO Get the service template model version uuid from AAI.
            String modelVersionId = null;
            AAIResourcesClient client = new AAIResourcesClient();
            AAIResourceUri url = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                    e2eDelReq.getGlobalSubscriberId(), e2eDelReq.getServiceType(), instanceIdMap.get(SERVICE_ID));
            Optional<ServiceInstance> serviceInstanceOpt = client.get(ServiceInstance.class, url);
            if (serviceInstanceOpt.isPresent()) {
                modelVersionId = serviceInstanceOpt.get().getModelVersionId();
            }
            recipeLookupResult = getServiceInstanceOrchestrationURI(modelVersionId, action);
        } catch (Exception e) {
            logger.error(MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(), "Exception while communciate with Catalog DB", e);

            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                    MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);

            msoRequest.createErrorRequestRecord(Status.FAILED, requestId,
                    "Exception while communciate with " + "Catalog DB", action, ModelType.service.name(), requestJSON);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }
        if (recipeLookupResult == null) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.DataError.getValue(), "No recipe found in DB");
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                    MsoException.ServiceException, "Recipe does not exist in catalog DB",
                    ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null, version);

            msoRequest.createErrorRequestRecord(Status.FAILED, requestId, "Recipe does not exist in catalog DB", action,
                    ModelType.service.name(), requestJSON);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        RequestClient requestClient;
        HttpResponse response;

        try {
            requestClient = requestClientFactory.getRequestClient(recipeLookupResult.getOrchestrationURI());

            JSONObject jjo = new JSONObject(requestJSON);
            jjo.put("operationId", requestId);

            String bpmnRequest = jjo.toString();

            // Capture audit event
            logger.debug("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl());
            String serviceId = instanceIdMap.get(SERVICE_ID);
            String serviceInstanceType = e2eDelReq.getServiceType();
            RequestClientParameter clientParam = new RequestClientParameter.Builder().setRequestId(requestId)
                    .setBaseVfModule(false).setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                    .setRequestAction(action.name()).setServiceInstanceId(serviceId).setServiceType(serviceInstanceType)
                    .setRequestDetails(bpmnRequest).setApiVersion(version).setALaCarte(false)
                    .setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).build();
            response = requestClient.post(clientParam);

        } catch (Exception e) {
            Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY,
                    MsoException.ServiceException, "Failed calling bpmn " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.AvailabilityError.getValue(),
                    "Exception while communicate with BPMN engine");
            logger.debug("End of the transaction, the final response is: " + resp.getEntity());
            return resp;
        }

        if (response == null) {
            Response resp =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY, MsoException.ServiceException,
                            "bpelResponse is null", ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.BusinessProcessError.getValue(), "Null response from BPEL");
            logger.debug(END_OF_THE_TRANSACTION + resp.getEntity());
            return resp;
        }

        ResponseHandler respHandler = new ResponseHandler(response, requestClient.getType());
        int bpelStatus = respHandler.getStatus();

        return beplStatusUpdate(requestClient, respHandler, bpelStatus, version);
    }

    private Response updateE2EserviceInstances(String requestJSON, Action action, String version) throws ApiException {

        String requestId = UUID.randomUUID().toString();
        E2EServiceInstanceRequest e2eSir;
        String serviceId = instanceIdMap.get(SERVICE_ID);

        ObjectMapper mapper = new ObjectMapper();
        try {
            e2eSir = mapper.readValue(requestJSON, E2EServiceInstanceRequest.class);

        } catch (Exception e) {

            logger.debug("Mapping of request to JSON object failed : ", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
                    MsoException.ServiceException, "Mapping of request to JSON object failed.  " + e.getMessage(),
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_REQUEST_VALIDATION_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.SchemaError.getValue(), requestJSON, e);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        ServiceInstancesRequest sir = mapReqJsonToSvcInstReq(e2eSir, requestJSON);
        sir.getRequestDetails().getRequestParameters().setaLaCarte(true);
        try {
            parseRequest(sir, instanceIdMap, action, version, requestJSON, false, requestId);
        } catch (Exception e) {
            logger.debug("Validation failed: ", e);
            Response response =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
                            "Error parsing request.  " + e.getMessage(), ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            if (requestId != null) {
                logger.debug("Logging failed message to the database");
            }
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_REQUEST_VALIDATION_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.SchemaError.getValue(), requestJSON, e);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        RecipeLookupResult recipeLookupResult;
        try {
            recipeLookupResult = getServiceInstanceOrchestrationURI(e2eSir.getService().getServiceUuid(), action);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(), "Exception while communciate with Catalog DB", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                    MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);

            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());

            return response;
        }

        if (recipeLookupResult == null) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.DataError.getValue(), "No recipe found in DB");
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                    MsoException.ServiceException, "Recipe does not exist in catalog DB",
                    ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null, version);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());

            return response;
        }

        String serviceInstanceType = e2eSir.getService().getServiceType();

        RequestClient requestClient;
        HttpResponse response;

        String sirRequestJson = convertToString(sir);

        try {
            requestClient = requestClientFactory.getRequestClient(recipeLookupResult.getOrchestrationURI());

            // Capture audit event
            logger.debug("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl());
            RequestClientParameter postParam = new RequestClientParameter.Builder().setRequestId(requestId)
                    .setBaseVfModule(false).setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                    .setRequestAction(action.name()).setServiceInstanceId(serviceId).setServiceType(serviceInstanceType)
                    .setRequestDetails(sirRequestJson).setApiVersion(version).setALaCarte(false)
                    .setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).build();
            response = requestClient.post(postParam);
        } catch (Exception e) {
            logger.debug("Exception while communicate with BPMN engine", e);
            Response getBPMNResp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY,
                    MsoException.ServiceException, "Failed calling bpmn " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);

            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.AvailabilityError.getValue(),
                    "Exception while communicate with BPMN engine");
            logger.debug(END_OF_THE_TRANSACTION + getBPMNResp.getEntity());

            return getBPMNResp;
        }

        if (response == null) {
            Response getBPMNResp =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY, MsoException.ServiceException,
                            "bpelResponse is null", ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.BusinessProcessError.getValue(), "Null response from BPEL");
            logger.debug(END_OF_THE_TRANSACTION + getBPMNResp.getEntity());
            return getBPMNResp;
        }

        ResponseHandler respHandler = new ResponseHandler(response, requestClient.getType());
        int bpelStatus = respHandler.getStatus();

        return beplStatusUpdate(requestClient, respHandler, bpelStatus, version);
    }

    private Response processE2EserviceInstances(String requestJSON, Action action,
            HashMap<String, String> instanceIdMap, String version) throws ApiException {

        String requestId = UUID.randomUUID().toString();
        E2EServiceInstanceRequest e2eSir;

        MsoRequest msoRequest = new MsoRequest();
        ObjectMapper mapper = new ObjectMapper();
        try {
            e2eSir = mapper.readValue(requestJSON, E2EServiceInstanceRequest.class);

        } catch (Exception e) {

            logger.debug("Mapping of request to JSON object failed : ", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
                    MsoException.ServiceException, "Mapping of request to JSON object failed.  " + e.getMessage(),
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_REQUEST_VALIDATION_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.SchemaError.getValue(), requestJSON, e);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        ServiceInstancesRequest sir = mapReqJsonToSvcInstReq(e2eSir, requestJSON);
        sir.getRequestDetails().getRequestParameters().setaLaCarte(true);
        try {
            parseRequest(sir, instanceIdMap, action, version, requestJSON, false, requestId);
        } catch (Exception e) {
            logger.debug("Validation failed: ", e);
            Response response =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
                            "Error parsing request.  " + e.getMessage(), ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            if (requestId != null) {
                logger.debug("Logging failed message to the database");
            }
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_REQUEST_VALIDATION_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.SchemaError.getValue(), requestJSON, e);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        RecipeLookupResult recipeLookupResult;
        try {
            recipeLookupResult = getServiceInstanceOrchestrationURI(e2eSir.getService().getServiceUuid(), action);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(), "Exception while communciate with Catalog DB", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                    MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        if (recipeLookupResult == null) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.DataError.getValue(), "No recipe found in DB");
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                    MsoException.ServiceException, "Recipe does not exist in catalog DB",
                    ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null, version);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        String serviceInstanceType = e2eSir.getService().getServiceType();

        String serviceId = e2eSir.getService().getServiceId();
        RequestClient requestClient;
        HttpResponse response;

        String sirRequestJson = convertToString(sir);

        try {
            requestClient = requestClientFactory.getRequestClient(recipeLookupResult.getOrchestrationURI());

            // Capture audit event
            logger.debug("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl());
            RequestClientParameter parameter = new RequestClientParameter.Builder().setRequestId(requestId)
                    .setBaseVfModule(false).setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                    .setRequestAction(action.name()).setServiceInstanceId(serviceId).setServiceType(serviceInstanceType)
                    .setRequestDetails(sirRequestJson).setApiVersion(version).setALaCarte(false)
                    .setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).build();
            response = requestClient.post(parameter);
        } catch (Exception e) {
            Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY,
                    MsoException.ServiceException, "Failed calling bpmn " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);

            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.AvailabilityError.getValue(),
                    "Exception while communicate with BPMN engine");
            logger.debug(END_OF_THE_TRANSACTION + resp.getEntity());
            return resp;
        }

        if (response == null) {
            Response resp =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY, MsoException.ServiceException,
                            "bpelResponse is null", ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.BusinessProcessError.getValue(), "Null response from BPEL");
            logger.debug(END_OF_THE_TRANSACTION + resp.getEntity());
            return resp;
        }

        ResponseHandler respHandler = new ResponseHandler(response, requestClient.getType());
        int bpelStatus = respHandler.getStatus();

        return beplStatusUpdate(requestClient, respHandler, bpelStatus, version);
    }

    private Response scaleE2EserviceInstances(String requestJSON, Action action, String version) throws ApiException {

        String requestId = UUID.randomUUID().toString();
        E2EServiceInstanceScaleRequest e2eScaleReq;

        ObjectMapper mapper = new ObjectMapper();
        try {
            e2eScaleReq = mapper.readValue(requestJSON, E2EServiceInstanceScaleRequest.class);

        } catch (Exception e) {

            logger.debug("Mapping of request to JSON object failed : ", e);
            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
                    MsoException.ServiceException, "Mapping of request to JSON object failed.  " + e.getMessage(),
                    ErrorNumbers.SVC_BAD_PARAMETER, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_REQUEST_VALIDATION_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.SchemaError.getValue(), requestJSON, e);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        RecipeLookupResult recipeLookupResult;
        try {
            // TODO Get the service template model version uuid from AAI.
            recipeLookupResult = getServiceInstanceOrchestrationURI(null, action);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ACCESS_EXC.toString(), MSO_PROP_APIHANDLER_INFRA,
                    ErrorCode.AvailabilityError.getValue(), "Exception while communciate with Catalog DB", e);

            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                    MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);

            msoRequest.createErrorRequestRecord(Status.FAILED, requestId,
                    "No communication to catalog DB " + e.getMessage(), action, ModelType.service.name(), requestJSON);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }
        if (recipeLookupResult == null) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.DataError.getValue(), "No recipe found in DB");

            Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
                    MsoException.ServiceException, "Recipe does not exist in catalog DB",
                    ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null, version);
            msoRequest.createErrorRequestRecord(Status.FAILED, requestId, "No recipe found in DB", action,
                    ModelType.service.name(), requestJSON);
            logger.debug(END_OF_THE_TRANSACTION + response.getEntity());
            return response;
        }

        RequestClient requestClient;
        HttpResponse response;

        try {
            requestClient = requestClientFactory.getRequestClient(recipeLookupResult.getOrchestrationURI());

            JSONObject jjo = new JSONObject(requestJSON);
            jjo.put("operationId", requestId);

            String bpmnRequest = jjo.toString();

            // Capture audit event
            logger.debug("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl());
            String serviceId = instanceIdMap.get(SERVICE_ID);
            String serviceInstanceType = e2eScaleReq.getService().getServiceType();
            RequestClientParameter postParam = new RequestClientParameter.Builder().setRequestId(requestId)
                    .setBaseVfModule(false).setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                    .setRequestAction(action.name()).setServiceInstanceId(serviceId).setServiceType(serviceInstanceType)
                    .setRequestDetails(bpmnRequest).setApiVersion(version).setALaCarte(false)
                    .setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).build();
            response = requestClient.post(postParam);
        } catch (Exception e) {
            Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY,
                    MsoException.ServiceException, "Failed calling bpmn " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);

            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.AvailabilityError.getValue(),
                    "Exception while communicate with BPMN engine", e);
            logger.debug(END_OF_THE_TRANSACTION + resp.getEntity());
            return resp;
        }

        if (response == null) {
            Response resp =
                    msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY, MsoException.ServiceException,
                            "bpelResponse is null", ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.BusinessProcessError.getValue(), "Null response from BPEL");
            logger.debug(END_OF_THE_TRANSACTION + resp.getEntity());
            return resp;
        }

        ResponseHandler respHandler = new ResponseHandler(response, requestClient.getType());
        int bpelStatus = respHandler.getStatus();

        return beplStatusUpdate(requestClient, respHandler, bpelStatus, version);
    }

    private Response beplStatusUpdate(RequestClient requestClient, ResponseHandler respHandler, int bpelStatus,
            String version) {

        String apiVersion = version.substring(1);

        // BPMN accepted the request, the request is in progress
        if (bpelStatus == HttpStatus.SC_ACCEPTED) {
            String camundaJSONResponseBody = respHandler.getResponseBody();
            logger.debug("Received from Camunda: " + camundaJSONResponseBody);
            logger.debug(END_OF_THE_TRANSACTION + camundaJSONResponseBody);
            return builder.buildResponse(HttpStatus.SC_ACCEPTED, null, camundaJSONResponseBody, apiVersion);
        } else {
            List<String> variables = new ArrayList<>();
            variables.add(bpelStatus + "");
            String camundaJSONResponseBody = respHandler.getResponseBody();
            if (camundaJSONResponseBody != null && !camundaJSONResponseBody.isEmpty()) {
                Response resp = msoRequest.buildServiceErrorResponse(bpelStatus, MsoException.ServiceException,
                        "Request Failed due to BPEL error with HTTP Status= %1 " + '\n' + camundaJSONResponseBody,
                        ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, variables, version);
                logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_RESPONSE_ERROR.toString(),
                        requestClient.getUrl(), ErrorCode.BusinessProcessError.getValue(),
                        "Response from BPEL engine is failed with HTTP Status=" + bpelStatus);
                logger.debug(END_OF_THE_TRANSACTION + resp.getEntity());
                return resp;
            } else {
                Response resp = msoRequest.buildServiceErrorResponse(bpelStatus, MsoException.ServiceException,
                        "Request Failed due to BPEL error with HTTP Status= %1",
                        ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, variables, version);
                logger.error("", MessageEnum.APIH_BPEL_RESPONSE_ERROR.toString(), requestClient.getUrl(),
                        ErrorCode.BusinessProcessError.getValue(), "Response from BPEL engine is empty");
                logger.debug(END_OF_THE_TRANSACTION + resp.getEntity());
                return resp;
            }
        }
    }

    /**
     * Getting recipes from catalogDb
     * 
     * @param serviceModelUUID the service model version uuid
     * @param action the action for the service
     * @return the service recipe result
     */
    private RecipeLookupResult getServiceInstanceOrchestrationURI(String serviceModelUUID, Action action) {

        RecipeLookupResult recipeLookupResult = getServiceURI(serviceModelUUID, action);

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
     * @return the service recipe result.
     */
    private RecipeLookupResult getServiceURI(String serviceModelUUID, Action action) {

        String defaultServiceModelName = "UUI_DEFAULT";

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

    /**
     * Converting E2EServiceInstanceRequest to ServiceInstanceRequest and passing it to camunda engine.
     * 
     * @param e2eSir
     * @return
     */
    private ServiceInstancesRequest mapReqJsonToSvcInstReq(E2EServiceInstanceRequest e2eSir, String requestJSON) {

        ServiceInstancesRequest sir = new ServiceInstancesRequest();

        RequestDetails requestDetails = new RequestDetails();
        ModelInfo modelInfo = new ModelInfo();

        // ModelInvariantId
        modelInfo.setModelInvariantId(e2eSir.getService().getServiceInvariantUuid());

        // modelNameVersionId
        modelInfo.setModelNameVersionId(e2eSir.getService().getServiceUuid());

        // String modelInfoValue =
        // e2eSir.getService().getParameters().getNodeTemplateName();
        // String[] arrayOfInfo = modelInfoValue.split(":");
        // String modelName = arrayOfInfo[0];
        // String modelVersion = arrayOfInfo[1];

        // TODO: To ensure, if we dont get the values from the UUI
        String modelName = "voLTE";
        String modelVersion = "1.0";
        // modelName
        modelInfo.setModelName(modelName);

        // modelVersion
        modelInfo.setModelVersion(modelVersion);

        // modelType
        modelInfo.setModelType(ModelType.service);

        // setting modelInfo to requestDetails
        requestDetails.setModelInfo(modelInfo);

        SubscriberInfo subscriberInfo = new SubscriberInfo();

        // globalsubscriberId
        subscriberInfo.setGlobalSubscriberId(e2eSir.getService().getGlobalSubscriberId());

        // setting subscriberInfo to requestDetails
        requestDetails.setSubscriberInfo(subscriberInfo);

        RequestInfo requestInfo = new RequestInfo();

        // instanceName
        requestInfo.setInstanceName(e2eSir.getService().getName());

        // source
        requestInfo.setSource("UUI");

        // suppressRollback
        requestInfo.setSuppressRollback(true);

        // setting requestInfo to requestDetails
        requestDetails.setRequestInfo(requestInfo);

        RequestParameters requestParameters = new RequestParameters();

        // subscriptionServiceType
        requestParameters.setSubscriptionServiceType("MOG");


        List<Map<String, Object>> userParamList = new ArrayList<>();
        Map<String, Object> userParamMap = new HashMap<>();
        // complete json request updated in the camunda
        userParamMap.put("UUIRequest", requestJSON);
        userParamMap.put("ServiceInstanceName", e2eSir.getService().getName());


        userParamList.add(userParamMap);
        requestParameters.setUserParams(userParamList);

        // setting requestParameters to requestDetails
        requestDetails.setRequestParameters(requestParameters);

        sir.setRequestDetails(requestDetails);

        return sir;
    }


    private void parseRequest(ServiceInstancesRequest sir, HashMap<String, String> instanceIdMap, Action action,
            String version, String requestJSON, Boolean aLaCarte, String requestId) throws ValidateException {
        int reqVersion = Integer.parseInt(version.substring(1));
        try {
            msoRequest.parse(sir, instanceIdMap, action, version, requestJSON, reqVersion, aLaCarte);
        } catch (Exception e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException =
                    new ValidateException.Builder("Error parsing request: " + e.getMessage(), HttpStatus.SC_BAD_REQUEST,
                            ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();

            msoRequest.createErrorRequestRecord(Status.FAILED, requestId, validateException.getMessage(), action,
                    ModelType.service.name(), requestJSON);

            throw validateException;
        }
    }

    private String convertToString(ServiceInstancesRequest sir) {
        String returnString = null;
        // converting to string
        ObjectMapper mapper = new ObjectMapper();
        try {
            returnString = mapper.writeValueAsString(sir);
        } catch (IOException e) {
            logger.debug("Exception while converting ServiceInstancesRequest object to string", e);
        }

        return returnString;
    }
}
