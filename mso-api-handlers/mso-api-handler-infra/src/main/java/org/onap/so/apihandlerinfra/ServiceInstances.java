/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2023 Nordix Foundation.
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
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.apihandler.common.CommonConstants;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.infra.rest.BpmnRequestBuilder;
import org.onap.so.apihandlerinfra.infra.rest.exception.CloudConfigurationNotFoundException;
import org.onap.so.apihandlerinfra.infra.rest.handler.AbstractRestHandler;
import org.onap.so.apihandlerinfra.infra.rest.validators.RequestValidatorListenerRunner;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.MessageEnum;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Component
@Path("/onap/so/infra/serviceInstantiation")
@OpenAPIDefinition(info = @Info(title = "/onap/so/infra/serviceInstantiation",
        description = "Infrastructure API Requests for Service Instances"))
public class ServiceInstances extends AbstractRestHandler {

    private static Logger logger = LoggerFactory.getLogger(MsoRequest.class);
    private static String uriPrefix = "/serviceInstantiation/";
    private static final String SAVE_TO_DB = "save instance to db";

    @Autowired
    private Environment env;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CatalogDbClient catalogDbClient;

    @Autowired
    private RequestsDbClient infraActiveRequestsClient;

    @Autowired
    private MsoRequest msoRequest;

    @Autowired
    private RequestHandlerUtils requestHandlerUtils;

    @Autowired
    private RequestValidatorListenerRunner requestValidatorListenerRunner;

    @Autowired
    private BpmnRequestBuilder bpmnRequestBuilder;

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create a Service Instance on a version provided", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response createServiceInstance(String request, @PathParam("version") String version,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        return serviceInstances(request, Action.createInstance, null, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Activate provided Service Instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response activateServiceInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return serviceInstances(request, Action.activateInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Deactivate provided Service Instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deactivateServiceInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return serviceInstances(request, Action.deactivateInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete provided Service Instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deleteServiceInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][7]}/serviceInstances/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Assign Service Instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response assignServiceInstance(String request, @PathParam("version") String version,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        return serviceInstances(request, Action.assignInstance, null, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/unassign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Unassign Service Instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response unassignServiceInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return serviceInstances(request, Action.unassignInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create Port Mirroring Configuration", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response createPortConfiguration(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return configurationRecipeLookup(request, Action.createInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations/{configurationInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete provided Port", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deletePortConfiguration(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId,
            @PathParam("configurationInstanceId") String configurationInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("configurationInstanceId", configurationInstanceId);
        return configurationRecipeLookup(request, Action.deleteInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations/{configurationInstanceId}/enablePort")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Enable Port Mirroring", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response enablePort(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId,
            @PathParam("configurationInstanceId") String configurationInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("configurationInstanceId", configurationInstanceId);
        return configurationRecipeLookup(request, Action.enablePort, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations/{configurationInstanceId}/disablePort")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Disable Port Mirroring", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response disablePort(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId,
            @PathParam("configurationInstanceId") String configurationInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("configurationInstanceId", configurationInstanceId);
        return configurationRecipeLookup(request, Action.disablePort, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations/{configurationInstanceId}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Activate Port Mirroring", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response activatePort(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId,
            @PathParam("configurationInstanceId") String configurationInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("configurationInstanceId", configurationInstanceId);
        return configurationRecipeLookup(request, Action.activateInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations/{configurationInstanceId}/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Deactivate Port Mirroring", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deactivatePort(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId,
            @PathParam("configurationInstanceId") String configurationInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("configurationInstanceId", configurationInstanceId);
        return configurationRecipeLookup(request, Action.deactivateInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][6-7]}/serviceInstances/{serviceInstanceId}/addRelationships")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add Relationships to a Service Instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response addRelationships(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return configurationRecipeLookup(request, Action.addRelationships, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][6-7]}/serviceInstances/{serviceInstanceId}/removeRelationships")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Remove Relationships from Service Instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response removeRelationships(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return configurationRecipeLookup(request, Action.removeRelationships, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create VNF on a specified version and serviceInstance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response createVnfInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        try {
            return serviceInstances(request, Action.createInstance, instanceIdMap, version, requestId,
                    requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
        } catch (Exception e) {
            logger.error("Error in vnf", e);
            throw e;
        }
    }

    @POST
    @Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/cnfs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create CNF on a specified version and serviceInstance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response createCnfInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        try {
            return serviceInstances(request, Action.createInstance, instanceIdMap, version, requestId,
                    requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
        } catch (Exception e) {
            logger.error("Error in cnf", e);
            throw e;
        }
    }

    @DELETE
    @Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/cnfs/{cnfInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete provided for CNF instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deleteCnfInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("cnfInstanceId") String cnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        logger.debug("Inside API Handler to perform delete CNF Instance");
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", cnfInstanceId);
        return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/upgrade")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Upgrade a Service Instance to newer model", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response upgradeServiceInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);

        return serviceInstances(request, Action.upgradeInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/replace")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Replace provided VNF instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response replaceVnfInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.replaceInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/healthcheck")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "HealthCheck for provided VNF instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response cnfHealthCheck(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.healthCheck, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/upgradeCnf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Upgrade CNF instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response cnfUpgrade(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.upgradeCnf, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @PUT
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update VNF on a specified version, serviceInstance and vnfInstance",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response updateVnfInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.updateInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][6-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/applyUpdatedConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Apply updated configuration", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response applyUpdatedConfig(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.applyUpdatedConfig, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/recreate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Recreate VNF Instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response recreateVnfInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.recreateInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete provided VNF instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deleteVnfInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create VfModule on a specified version, serviceInstance and vnfInstance",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response createVfModuleInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.createInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}/replace")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create VfModule on a specified version, serviceInstance and vnfInstance",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response replaceVfModuleInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
        return serviceInstances(request, Action.replaceInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @PUT
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update VfModule on a specified version, serviceInstance, vnfInstance and vfModule",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response updateVfModuleInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
        return serviceInstances(request, Action.updateInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][6-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/inPlaceSoftwareUpdate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Perform VNF software update", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response inPlaceSoftwareUpdate(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.inPlaceSoftwareUpdate, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete provided VfModule instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deleteVfModuleInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
        return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}/deactivateAndCloudDelete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Deactivate and Cloud Delete VfModule instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deactivateAndCloudDeleteVfModuleInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
        Response response = serviceInstances(request, Action.deactivateAndCloudDelete, instanceIdMap, version,
                requestId, requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
        return response;
    }

    @POST
    @Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/scaleOut")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "VF Auto Scale Out", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response scaleOutVfModule(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.scaleOut, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create VolumeGroup on a specified version, serviceInstance, vnfInstance",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response createVolumeGroupInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.createInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @PUT
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups/{volumeGroupInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update VolumeGroup on a specified version, serviceInstance, vnfInstance and volumeGroup",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response updateVolumeGroupInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @PathParam("volumeGroupInstanceId") String volumeGroupInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("volumeGroupInstanceId", volumeGroupInstanceId);
        return serviceInstances(request, Action.updateInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups/{volumeGroupInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete provided VolumeGroup instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deleteVolumeGroupInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @PathParam("volumeGroupInstanceId") String volumeGroupInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("volumeGroupInstanceId", volumeGroupInstanceId);
        return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }


    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/pnfs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create VNF on a specified version and serviceInstance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response createPnfInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        try {
            return serviceInstances(request, Action.createInstance, instanceIdMap, version, requestId,
                    requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
        } catch (Exception e) {
            logger.error("Error in pnf", e);
            throw e;
        }
    }


    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/pnfs/{pnfInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create PNF on a specified version and serviceInstance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deletePnfInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("pnfInstanceId") String pnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("pnfInstanceId", pnfInstanceId);
        try {
            return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId,
                    requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
        } catch (Exception e) {
            logger.error("Error in pnf", e);
            throw e;
        }
    }

    @PUT
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/pnfs/{pnfInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create PNF on a specified version and serviceInstance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response updatePnfInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("pnfInstanceId") String pnfInstanceId,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("pnfInstanceId", pnfInstanceId);
        try {
            return serviceInstances(request, Action.updateInstance, instanceIdMap, version, requestId,
                    requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
        } catch (Exception e) {
            logger.error("Error in pnf", e);
            throw e;
        }
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/networks")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create NetworkInstance on a specified version and serviceInstance ",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response createNetworkInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return serviceInstances(request, Action.createInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @PUT
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/networks/{networkInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update VolumeGroup on a specified version, serviceInstance, networkInstance",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response updateNetworkInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId,
            @PathParam("networkInstanceId") String networkInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("networkInstanceId", networkInstanceId);
        return serviceInstances(request, Action.updateInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/networks/{networkInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete provided Network instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deleteNetworkInstance(String request, @PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId,
            @PathParam("networkInstanceId") String networkInstanceId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("networkInstanceId", networkInstanceId);
        return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][7]}/instanceGroups")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create instanceGroups", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response createInstanceGroups(String request, @PathParam("version") String version,
            @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        return serviceInstances(request, Action.createInstance, null, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @DELETE
    @Path("/{version:[vV][7]}/instanceGroups/{instanceGroupId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete instanceGroup", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deleteInstanceGroups(@PathParam("version") String version,
            @PathParam("instanceGroupId") String instanceGroupId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put(CommonConstants.INSTANCE_GROUP_INSTANCE_ID, instanceGroupId);
        return deleteInstanceGroups(Action.deleteInstance, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix), requestContext);
    }

    @POST
    @Path("/{version:[vV][7]}/instanceGroups/{instanceGroupId}/addMembers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add instanceGroup members", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response addInstanceGroupMembers(String request, @PathParam("version") String version,
            @PathParam("instanceGroupId") String instanceGroupId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put(CommonConstants.INSTANCE_GROUP_INSTANCE_ID, instanceGroupId);
        return serviceInstances(request, Action.addMembers, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    @POST
    @Path("/{version:[vV][7]}/instanceGroups/{instanceGroupId}/removeMembers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Remove instanceGroup members", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response removeInstanceGroupMembers(String request, @PathParam("version") String version,
            @PathParam("instanceGroupId") String instanceGroupId, @Context ContainerRequestContext requestContext)
            throws ApiException {
        String requestId = requestHandlerUtils.getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put(CommonConstants.INSTANCE_GROUP_INSTANCE_ID, instanceGroupId);
        return serviceInstances(request, Action.removeMembers, instanceIdMap, version, requestId,
                requestHandlerUtils.getRequestUri(requestContext, uriPrefix));
    }

    /**
     * This method is used for POST a request to the BPEL client (BPMN).
     *
     * Convert the requestJson to ServiceInstanceRequest(sir), create the msoRequest object, check whether this request
     * is already being processed in requestdb for duplicate check.
     *
     * Based on the alacarte flag, sir and msoRequest will do the recipe lookup from the service and servicerecipe table
     * of catalogdb, and get the OrchestrationURI.
     *
     * If the present request is not the duplicate request then this request will be saved in the requestdb. and will
     * POST a request to the BPMN engine at the OrchestrationURI fetched.
     *
     * @param requestJSON Json fetched as body in the API call
     * @param action Type of action to be performed
     * @param instanceIdMap Map of instance ids of service/vnf/vf/configuration etc..
     * @param version Supported version of API
     * @param requestId Unique id for the request
     * @param requestUri
     * @return response object
     * @throws ApiException
     */
    public Response serviceInstances(String requestJSON, Actions action, HashMap<String, String> instanceIdMap,
            String version, String requestId, String requestUri) throws ApiException {
        return serviceInstances(requestJSON, action, instanceIdMap, version, requestId, requestUri, null);

    }

    public Response serviceInstances(String requestJSON, Actions action, HashMap<String, String> instanceIdMap,
            String version, String requestId, String requestUri, HashMap<String, String> queryParams)
            throws ApiException {
        String serviceInstanceId;
        Boolean aLaCarte = null;
        ServiceInstancesRequest sir;
        String apiVersion = version.substring(1);

        sir = requestHandlerUtils.convertJsonToServiceInstanceRequest(requestJSON, action, requestId, requestUri);
        action = handleReplaceInstance(action, sir);

        String requestScope = requestHandlerUtils.deriveRequestScope(action, sir, requestUri);
        try {
            requestValidatorListenerRunner.runValidations(requestUri, instanceIdMap, sir, queryParams, action);
        } catch (ApiException e) {
            msoRequest.createErrorRequestRecord(Status.FAILED, requestId, e.getMessage(), action, requestScope,
                    requestJSON, requestHandlerUtils
                            .getServiceInstanceIdForValidationError(sir, instanceIdMap, requestScope).orElse(null),
                    sir);
            throw e;
        }

        InfraActiveRequests currentActiveReq =
                msoRequest.createRequestObject(sir, action, requestId, Status.IN_PROGRESS, requestJSON, requestScope);
        if (sir.getRequestDetails().getRequestParameters() != null) {
            aLaCarte = sir.getRequestDetails().getRequestParameters().getALaCarte();
        }

        requestHandlerUtils.parseRequest(sir, instanceIdMap, action, version, requestJSON, aLaCarte, requestId,
                currentActiveReq);
        if ((action == Action.replaceInstance || action == Action.replaceInstanceRetainAssignments)
                && (requestScope.equals(ModelType.vnf.toString()) || requestScope.equals(ModelType.vfModule.toString())
                        || requestScope.equals(ModelType.cnf.toString()))
                && sir.getRequestDetails().getCloudConfiguration() == null) {
            CloudConfiguration cloudConfiguration =
                    getCloudConfigurationOnReplace(requestScope, instanceIdMap, currentActiveReq);
            sir.getRequestDetails().setCloudConfiguration(cloudConfiguration);
            setCloudConfigurationCurrentActiveRequest(cloudConfiguration, currentActiveReq);
        }
        requestHandlerUtils.setInstanceId(currentActiveReq, requestScope, null, instanceIdMap);

        String instanceName = null;
        if (sir.getRequestDetails().getRequestInfo() != null) {
            instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();
        }
        boolean alaCarteFlag = msoRequest.getAlacarteFlag(sir);
        String vnfType = msoRequest.getVnfType(sir, requestScope);
        String networkType = msoRequest.getNetworkType(sir, requestScope);
        String sdcServiceModelVersion = msoRequest.getSDCServiceModelVersion(sir);
        String vfModuleType = msoRequest.getVfModuleType(sir, requestScope);

        if (requestScope.equalsIgnoreCase(ModelType.vnf.name()) && vnfType != null) {
            currentActiveReq.setVnfType(vnfType);
        } else if (requestScope.equalsIgnoreCase(ModelType.network.name()) && networkType != null) {
            currentActiveReq.setNetworkType(networkType);
        }

        requestHandlerUtils.checkForDuplicateRequests(action, instanceIdMap, requestScope, currentActiveReq,
                instanceName);

        ServiceInstancesResponse serviceResponse = new ServiceInstancesResponse();

        RequestReferences referencesResponse = new RequestReferences();

        referencesResponse.setRequestId(requestId);

        serviceResponse.setRequestReferences(referencesResponse);
        RecipeLookupResult recipeLookupResult =
                requestHandlerUtils.getServiceInstanceOrchestrationURI(sir, action, alaCarteFlag, currentActiveReq);
        String serviceInstanceType = requestHandlerUtils.getServiceType(requestScope, sir, alaCarteFlag);

        ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
        ModelType modelType = requestHandlerUtils.getModelType(action, modelInfo);

        Boolean isBaseVfModule = false;

        if (modelType.equals(ModelType.vfModule)) {
            isBaseVfModule = requestHandlerUtils.getIsBaseVfModule(modelInfo, action, vnfType, sdcServiceModelVersion,
                    currentActiveReq);
        }

        serviceInstanceId = requestHandlerUtils.setServiceInstanceId(requestScope, sir);
        String vnfId = "";
        String vfModuleId = "";
        String volumeGroupId = "";
        String networkId = "";
        String pnfCorrelationId = "";
        String instanceGroupId = null;

        if (sir.getVnfInstanceId() != null) {
            vnfId = sir.getVnfInstanceId();
        }

        if (sir.getVfModuleInstanceId() != null) {
            vfModuleId = sir.getVfModuleInstanceId();
        }

        if (sir.getVolumeGroupInstanceId() != null) {
            volumeGroupId = sir.getVolumeGroupInstanceId();
        }

        if (sir.getNetworkInstanceId() != null) {
            networkId = sir.getNetworkInstanceId();
        }
        if (sir.getInstanceGroupId() != null) {
            instanceGroupId = sir.getInstanceGroupId();
        }

        pnfCorrelationId = getPnfCorrelationId(sir);

        try {
            infraActiveRequestsClient.save(currentActiveReq);
        } catch (Exception e) {
            logger.error("Exception occurred", e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.DataError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e).errorInfo(errorLoggerInfo).build();
        }

        if (!requestScope.equalsIgnoreCase(ModelType.service.name()) && action != Action.recreateInstance
                && !requestScope.equalsIgnoreCase(ModelType.vnf.name())
                && !requestScope.equalsIgnoreCase(ModelType.pnf.name())) {
            aLaCarte = true;
        } else if (aLaCarte == null) {
            aLaCarte = false;
        }


        RequestClientParameter requestClientParameter;
        try {
            requestClientParameter = new RequestClientParameter.Builder().setRequestId(requestId)
                    .setBaseVfModule(isBaseVfModule).setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                    .setRequestAction(action.toString()).setServiceInstanceId(serviceInstanceId)
                    .setPnfCorrelationId(pnfCorrelationId).setVnfId(vnfId).setVfModuleId(vfModuleId)
                    .setVolumeGroupId(volumeGroupId).setNetworkId(networkId).setServiceType(serviceInstanceType)
                    .setVnfType(vnfType).setVfModuleType(vfModuleType).setNetworkType(networkType)
                    .setRequestDetails(requestHandlerUtils.mapJSONtoMSOStyle(requestJSON, sir, aLaCarte, action))
                    .setApiVersion(apiVersion).setALaCarte(aLaCarte).setRequestUri(requestUri)
                    .setInstanceGroupId(instanceGroupId).build();
        } catch (IOException e) {
            logger.error("Exception occurred", e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.SchemaError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new ValidateException.Builder("Unable to generate RequestClientParamter object" + e.getMessage(),
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo)
                            .build();
        }
        return requestHandlerUtils.postBPELRequest(currentActiveReq, requestClientParameter,
                recipeLookupResult.getOrchestrationURI(), requestScope);
    }

    /**
     * @param action
     * @param sir
     * @return
     */
    protected Actions handleReplaceInstance(Actions action, ServiceInstancesRequest sir) {
        if (action != null && action.equals(Action.replaceInstance)
                && sir.getRequestDetails().getRequestParameters().getRetainAssignments() != null
                && sir.getRequestDetails().getRequestParameters().getRetainAssignments()) {
            action = Action.replaceInstanceRetainAssignments;
        }
        return action;
    }

    /**
     * This method deletes the Instance Groups.
     *
     * This method will check whether the request is not duplicate in requestdb. if its not then will save as a new
     * request. And will send a POST request to BEPL client to delete the Insatnce Groups.
     *
     * @param action
     * @param instanceIdMap
     * @param version
     * @param requestId
     * @param requestUri
     * @param requestContext
     * @return
     * @throws ApiException
     */
    public Response deleteInstanceGroups(Actions action, HashMap<String, String> instanceIdMap, String version,
            String requestId, String requestUri, ContainerRequestContext requestContext) throws ApiException {
        String instanceGroupId = instanceIdMap.get(CommonConstants.INSTANCE_GROUP_INSTANCE_ID);
        Boolean aLaCarte = true;
        String apiVersion = version.substring(1);
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        sir.setInstanceGroupId(instanceGroupId);

        String requestScope = ModelType.instanceGroup.toString();
        InfraActiveRequests currentActiveReq =
                msoRequest.createRequestObject(sir, action, requestId, Status.IN_PROGRESS, null, requestScope);
        requestHandlerUtils.setInstanceId(currentActiveReq, requestScope, null, instanceIdMap);
        try {
            requestHandlerUtils.validateHeaders(requestContext);
        } catch (ValidationException e) {
            logger.error("Exception occurred", e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException =
                    new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST,
                            ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();
            requestHandlerUtils.updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());
            throw validateException;
        }

        requestHandlerUtils.checkForDuplicateRequests(action, instanceIdMap, requestScope, currentActiveReq, null);

        ServiceInstancesResponse serviceResponse = new ServiceInstancesResponse();

        RequestReferences referencesResponse = new RequestReferences();

        referencesResponse.setRequestId(requestId);

        serviceResponse.setRequestReferences(referencesResponse);
        Boolean isBaseVfModule = false;

        RecipeLookupResult recipeLookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 180);

        try {
            infraActiveRequestsClient.save(currentActiveReq);
        } catch (Exception e) {
            logger.error("Exception occurred", e);
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.DataError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e).errorInfo(errorLoggerInfo).build();
        }

        RequestClientParameter requestClientParameter = new RequestClientParameter.Builder().setRequestId(requestId)
                .setBaseVfModule(isBaseVfModule).setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
                .setRequestAction(action.toString()).setApiVersion(apiVersion).setALaCarte(aLaCarte)
                .setRequestUri(requestUri).setInstanceGroupId(instanceGroupId).build();


        return requestHandlerUtils.postBPELRequest(currentActiveReq, requestClientParameter,
                recipeLookupResult.getOrchestrationURI(), requestScope);
    }

    protected String getPnfCorrelationId(ServiceInstancesRequest sir) {
        return Optional.of(sir).map(ServiceInstancesRequest::getRequestDetails)
                .map(RequestDetails::getRequestParameters).map(parameters -> parameters.getUserParamValue("pnfId"))
                .orElse("");
    }

    private Response configurationRecipeLookup(String requestJSON, Action action, HashMap<String, String> instanceIdMap,
            String version, String requestId, String requestUri) throws ApiException {
        String serviceInstanceId;
        Boolean aLaCarte = null;
        String apiVersion = version.substring(1);
        ServiceInstancesRequest sir;

        sir = requestHandlerUtils.convertJsonToServiceInstanceRequest(requestJSON, action, requestId, requestUri);
        String requestScope = requestHandlerUtils.deriveRequestScope(action, sir, requestUri);
        InfraActiveRequests currentActiveReq =
                msoRequest.createRequestObject(sir, action, requestId, Status.IN_PROGRESS, requestJSON, requestScope);
        if (sir.getRequestDetails().getRequestParameters() != null) {
            aLaCarte = sir.getRequestDetails().getRequestParameters().getALaCarte();
        }

        requestHandlerUtils.parseRequest(sir, instanceIdMap, action, version, requestJSON, aLaCarte, requestId,
                currentActiveReq);
        requestHandlerUtils.setInstanceId(currentActiveReq, requestScope, null, instanceIdMap);
        String instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();

        requestHandlerUtils.checkForDuplicateRequests(action, instanceIdMap, requestScope, currentActiveReq,
                instanceName);

        ServiceInstancesResponse serviceResponse = new ServiceInstancesResponse();
        RequestReferences referencesResponse = new RequestReferences();
        referencesResponse.setRequestId(requestId);
        serviceResponse.setRequestReferences(referencesResponse);

        String orchestrationUri = env.getProperty(CommonConstants.ALACARTE_ORCHESTRATION);
        String timeOut = env.getProperty(CommonConstants.ALACARTE_RECIPE_TIMEOUT);

        if (StringUtils.isBlank(orchestrationUri) || StringUtils.isBlank(timeOut)) {
            String error = StringUtils.isBlank(orchestrationUri) ? "ALaCarte Orchestration URI not found in properties"
                    : "ALaCarte Recipe Timeout not found in properties";

            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, ErrorCode.DataError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();

            ValidateException validateException = new ValidateException.Builder(error, HttpStatus.SC_NOT_FOUND,
                    ErrorNumbers.SVC_GENERAL_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();

            requestHandlerUtils.updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());

            throw validateException;

        }

        serviceInstanceId = "";
        String configurationId = "";
        String pnfCorrelationId = "";

        if (sir.getServiceInstanceId() != null) {
            serviceInstanceId = sir.getServiceInstanceId();
        }

        if (sir.getConfigurationId() != null) {
            configurationId = sir.getConfigurationId();
        }

        pnfCorrelationId = getPnfCorrelationId(sir);

        try {
            infraActiveRequestsClient.save(currentActiveReq);
        } catch (Exception e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.DataError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e).errorInfo(errorLoggerInfo).build();
        }

        if (!requestScope.equalsIgnoreCase(ModelType.service.name())) {
            aLaCarte = true;
        } else if (aLaCarte == null) {
            aLaCarte = false;
        }
        RequestClientParameter requestClientParameter = null;
        try {
            requestClientParameter = new RequestClientParameter.Builder().setRequestId(requestId).setBaseVfModule(false)
                    .setRecipeTimeout(Integer.parseInt(timeOut)).setRequestAction(action.toString())
                    .setServiceInstanceId(serviceInstanceId).setPnfCorrelationId(pnfCorrelationId)
                    .setConfigurationId(configurationId)
                    .setRequestDetails(requestHandlerUtils.mapJSONtoMSOStyle(requestJSON, sir, aLaCarte, action))
                    .setApiVersion(apiVersion).setALaCarte(aLaCarte).setRequestUri(requestUri).build();
        } catch (IOException e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.SchemaError)
                            .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new ValidateException.Builder("Unable to generate RequestClientParamter object" + e.getMessage(),
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo)
                            .build();
        }

        return requestHandlerUtils.postBPELRequest(currentActiveReq, requestClientParameter, orchestrationUri,
                requestScope);
    }

    protected CloudConfiguration getCloudConfigurationOnReplace(String requestScope,
            HashMap<String, String> instanceIdMap, InfraActiveRequests currentActiveReq) throws ApiException {
        logger.debug("Replace request is missing cloudConfiguration, autofilling from create.");
        CloudConfiguration cloudConfiguration = null;
        if (requestScope.equals(ModelType.vfModule.toString())) {
            cloudConfiguration = bpmnRequestBuilder.getCloudConfigurationVfModuleReplace(
                    instanceIdMap.get("vnfInstanceId"), instanceIdMap.get("vfModuleInstanceId"));
        } else {
            cloudConfiguration = bpmnRequestBuilder.mapCloudConfigurationVnf(instanceIdMap.get("vnfInstanceId"));
        }

        if (cloudConfiguration == null) {
            String errorMessage = "CloudConfiguration not found during autofill for replace request.";
            logger.error(errorMessage);
            updateStatus(currentActiveReq, Status.FAILED, errorMessage);
            throw new CloudConfigurationNotFoundException(
                    "CloudConfiguration not found during autofill for replace request.");
        }
        return cloudConfiguration;
    }

    protected void setCloudConfigurationCurrentActiveRequest(CloudConfiguration cloudConfiguration,
            InfraActiveRequests currentActiveRequest) {
        if (cloudConfiguration.getLcpCloudRegionId() != null) {
            currentActiveRequest.setCloudRegion(cloudConfiguration.getLcpCloudRegionId());
        }

        if (cloudConfiguration.getTenantId() != null) {
            currentActiveRequest.setTenantId(cloudConfiguration.getTenantId());
        }
    }
}
