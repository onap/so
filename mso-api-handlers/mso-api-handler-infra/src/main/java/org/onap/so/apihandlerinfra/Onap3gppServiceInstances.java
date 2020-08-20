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

import java.util.UUID;
import java.util.function.Function;
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
import org.apache.http.HttpStatus;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.apihandler.camundabeans.CamundaResponse;
import org.onap.so.apihandler.common.CamundaClient;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandler.common.ResponseHandler;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.ActivateOrDeactivate3gppService;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.Allocate3gppService;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.DeAllocate3gppService;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.Modify3gppService;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.QuerySubnetCapability;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Component
@Path("/onap/so/infra/onap3gppServiceInstances")
@OpenAPIDefinition(info = @Info(title = "/onap/so/infra/onap3gppServiceInstances",
        description = "API Requests for 3GPP Service Instances"))
public class Onap3gppServiceInstances {

    private static final Logger logger = LoggerFactory.getLogger(Onap3gppServiceInstances.class);

    private static final String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static final String END_OF_THE_TRANSACTION = "End of the transaction, the final response is: ";

    @Autowired
    private MsoRequest msoRequest;

    @Autowired
    private CatalogDbClient catalogDbClient;

    @Autowired
    private ResponseBuilder builder;

    @Autowired
    private CamundaClient camundaClient;

    @Autowired
    private ResponseHandler responseHandler;

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
    public Response createServiceInstance(Allocate3gppService request, @PathParam("version") String version)
            throws ApiException {

        return processServiceInstanceRequest(request, Action.createInstance, version);
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
    public Response updateServiceInstance(Modify3gppService request, @PathParam("version") String version)
            throws ApiException {

        return updateServiceInstances(request, Action.updateInstance, version);
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
    public Response deleteServiceInstance(DeAllocate3gppService request, @PathParam("version") String version)
            throws ApiException {
        return deleteServiceInstances(request, Action.deleteInstance, version);
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
            @PathParam("version") String version) throws ApiException {
        return activateOrDeactivateServiceInstances(request, Action.activateInstance, version);
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
            @PathParam("version") String version) throws ApiException {
        return activateOrDeactivateServiceInstances(request, Action.deactivateInstance, version);
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
        String subnetType = null;
        return getSubnetCapabilities(subnetType, version);
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
    private Response processServiceInstanceRequest(Allocate3gppService request, Action action, String version)
            throws ApiException {
        String requestId = UUID.randomUUID().toString();
        String defaultServiceModelName = "COMMON_SS_DEFAULT";
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

        String serviceInstanceType = request.getSubscriptionServiceType();

        Function<Allocate3gppService, String> toString = serviceRequest -> {
            ObjectMapper mapper = new ObjectMapper();
            String requestAsString = null;
            try {
                requestAsString = mapper.writeValueAsString(serviceRequest);
            } catch (JsonProcessingException e) {
                logger.debug("Exception while converting service request object to String {}", e);
            }
            return requestAsString;
        };
        String serviceRequestJson = toString.apply(request);

        RequestClientParameter parameter = new RequestClientParameter.Builder().setRequestId(requestId)
                .setBaseVfModule(false).setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                .setRequestAction(action.name()).setServiceInstanceId(null).setServiceType(serviceInstanceType)
                .setRequestDetails(serviceRequestJson).setApiVersion(version).setALaCarte(false)
                .setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).build();

        return postRequest(recipeLookupResult.getOrchestrationURI(), parameter, version);
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
    private Response updateServiceInstances(Modify3gppService request, Action action, String version)
            throws ApiException {
        String requestId = UUID.randomUUID().toString();
        String defaultServiceModelName = "COMMON_SS_DEFAULT";
        RecipeLookupResult recipeLookupResult;
        try {
            recipeLookupResult = getServiceInstanceOrchestrationURI(null, action, defaultServiceModelName);
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

        String serviceInstanceType = request.getSubscriptionServiceType();

        Function<Modify3gppService, String> toString = serviceRequest -> {
            ObjectMapper mapper = new ObjectMapper();
            String requestAsString = null;
            try {
                requestAsString = mapper.writeValueAsString(serviceRequest);
            } catch (JsonProcessingException e) {
                logger.debug("Exception while converting service request object to String {}", e);
            }
            return requestAsString;
        };
        String serviceRequestJson = toString.apply(request);
        String serviceInstanceId = request.getServiceInstanceID();

        RequestClientParameter parameter = new RequestClientParameter.Builder().setRequestId(requestId)
                .setBaseVfModule(false).setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                .setRequestAction(action.name()).setServiceInstanceId(serviceInstanceId)
                .setServiceType(serviceInstanceType).setRequestDetails(serviceRequestJson).setApiVersion(version)
                .setALaCarte(false).setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).build();

        return postRequest(recipeLookupResult.getOrchestrationURI(), parameter, version);
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
    private Response deleteServiceInstances(DeAllocate3gppService request, Action action, String version)
            throws ApiException {
        String requestId = UUID.randomUUID().toString();
        String defaultServiceModelName = "COMMON_SS_DEFAULT";
        RecipeLookupResult recipeLookupResult;
        try {
            recipeLookupResult = getServiceInstanceOrchestrationURI(null, action, defaultServiceModelName);
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

        String serviceInstanceType = request.getSubscriptionServiceType();

        Function<DeAllocate3gppService, String> toString = serviceRequest -> {
            ObjectMapper mapper = new ObjectMapper();
            String requestAsString = null;
            try {
                requestAsString = mapper.writeValueAsString(serviceRequest);
            } catch (JsonProcessingException e) {
                logger.debug("Exception while converting service request object to String {}", e);
            }
            return requestAsString;
        };
        String serviceRequestJson = toString.apply(request);
        String serviceInstanceId = request.getServiceInstanceID();

        RequestClientParameter parameter = new RequestClientParameter.Builder().setRequestId(requestId)
                .setBaseVfModule(false).setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                .setRequestAction(action.name()).setServiceInstanceId(serviceInstanceId)
                .setServiceType(serviceInstanceType).setRequestDetails(serviceRequestJson).setApiVersion(version)
                .setALaCarte(false).setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).build();

        return postRequest(recipeLookupResult.getOrchestrationURI(), parameter, version);
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
            String version) throws ApiException {
        String requestId = UUID.randomUUID().toString();
        String defaultServiceModelName = "COMMON_SS_DEFAULT";
        RecipeLookupResult recipeLookupResult;
        try {
            recipeLookupResult = getServiceInstanceOrchestrationURI(null, action, defaultServiceModelName);
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

        String serviceInstanceType = request.getSubscriptionServiceType();

        Function<ActivateOrDeactivate3gppService, String> toString = serviceRequest -> {
            ObjectMapper mapper = new ObjectMapper();
            String requestAsString = null;
            try {
                requestAsString = mapper.writeValueAsString(serviceRequest);
            } catch (JsonProcessingException e) {
                logger.debug("Exception while converting service request object to String {}", e);
            }
            return requestAsString;
        };
        String serviceRequestJson = toString.apply(request);
        String serviceInstanceId = request.getServiceInstanceID();

        RequestClientParameter parameter = new RequestClientParameter.Builder().setRequestId(requestId)
                .setBaseVfModule(false).setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                .setRequestAction(action.name()).setServiceInstanceId(serviceInstanceId)
                .setServiceType(serviceInstanceType).setRequestDetails(serviceRequestJson).setApiVersion(version)
                .setALaCarte(false).setRecipeParamXsd(recipeLookupResult.getRecipeParamXsd()).build();

        return postRequest(recipeLookupResult.getOrchestrationURI(), parameter, version);
    }

    private Response getSubnetCapabilities(String subnetType, String version) throws ApiException {
        return null;
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

    protected Response postRequest(String orchestrationURI, RequestClientParameter postParam, String version)
            throws ApiException {
        ResponseEntity<String> response = null;
        try {
            response = camundaClient.post(postParam, orchestrationURI);
        } catch (BPMNFailureException e) {
            Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    MsoException.ServiceException, "Failed calling bpmn " + e.getMessage(),
                    ErrorNumbers.SVC_NO_SERVER_RESOURCES, null, version);
            logger.error(LoggingAnchor.FOUR, MessageEnum.APIH_BPEL_COMMUNICATE_ERROR.toString(),
                    MSO_PROP_APIHANDLER_INFRA, ErrorCode.AvailabilityError.getValue(),
                    "Exception while communicate with BPMN engine");
            logger.debug(END_OF_THE_TRANSACTION + resp.getEntity());
            return resp;
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
        return bpelStatusUpdate(response, version);
    }

    private Response bpelStatusUpdate(ResponseEntity<String> responseEntity, String version) throws ApiException {
        String apiVersion = version.substring(1);
        responseHandler.acceptedResponse(responseEntity);
        CamundaResponse camundaResponse = responseHandler.getCamundaResponse(responseEntity);
        String response = camundaResponse.getResponse();
        logger.debug(END_OF_THE_TRANSACTION + response);
        return builder.buildResponse(HttpStatus.SC_ACCEPTED, null, response, apiVersion);
    }
}
