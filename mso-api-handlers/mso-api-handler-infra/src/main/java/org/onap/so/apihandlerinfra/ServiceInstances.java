/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import joptsimple.internal.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.onap.so.apihandler.camundabeans.CamundaResponse;
import org.onap.so.apihandler.common.CommonConstants;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClient;
import org.onap.so.apihandler.common.RequestClientFactory;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandler.common.ResponseHandler;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.ClientConnectionException;
import org.onap.so.apihandlerinfra.exceptions.DuplicateRequestException;
import org.onap.so.apihandlerinfra.exceptions.RecipeNotFoundException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.exceptions.VfModuleNotFoundException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.Recipe;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfRecipe;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.Networks;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;
import org.onap.so.utils.UUIDChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
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
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@Path("/onap/so/infra/serviceInstantiation")
@Api(value = "/onap/so/infra/serviceInstantiation", description = "Infrastructure API Requests for Service Instances")
public class ServiceInstances {

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH, MsoRequest.class);

    private final Environment env;
    private final RequestClientFactory reqClientFactory;
    private final CatalogDbClient catalogDbClient;
    private final RequestsDbClient infraActiveRequestsClient;
    private final ResponseBuilder builder;
    private final MsoRequest msoRequest;

    @Autowired
    public ServiceInstances(Environment env, RequestClientFactory reqClientFactory, CatalogDbClient catalogDbClient,
                            RequestsDbClient infraActiveRequestsClient, ResponseBuilder builder, MsoRequest msoRequest) {
        this.env = env;
        this.reqClientFactory = reqClientFactory;
        this.catalogDbClient = catalogDbClient;
        this.infraActiveRequestsClient = infraActiveRequestsClient;
        this.builder = builder;
        this.msoRequest = msoRequest;
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a Service Instance on a version provided", response = Response.class)
    @Transactional
    public Response createServiceInstance(String request, @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        return serviceInstances(request, Action.createInstance, null, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Activate provided Service Instance", response = Response.class)
    @Transactional
    public Response activateServiceInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return serviceInstances(request, Action.activateInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deactivate provided Service Instance", response = Response.class)
    @Transactional
    public Response deactivateServiceInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return serviceInstances(request, Action.deactivateInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete provided Service Instance", response = Response.class)
    @Transactional
    public Response deleteServiceInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][7]}/serviceInstances/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Assign Service Instance", response = Response.class)
    @Transactional
    public Response assignServiceInstance(String request, @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        return serviceInstances(request, Action.assignInstance, null, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/unassign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Unassign Service Instance", response = Response.class)
    @Transactional
    public Response unassignServiceInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<String, String>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return serviceInstances(request, Action.unassignInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Port Mirroring Configuration", response = Response.class)
    @Transactional
    public Response createPortConfiguration(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return configurationRecipeLookup(request, Action.createInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations/{configurationInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete provided Port", response = Response.class)
    @Transactional
    public Response deletePortConfiguration(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                            @PathParam("configurationInstanceId") String configurationInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("configurationInstanceId", configurationInstanceId);
        return configurationRecipeLookup(request, Action.deleteInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations/{configurationInstanceId}/enablePort")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Enable Port Mirroring", response = Response.class)
    @Transactional
    public Response enablePort(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                               @PathParam("configurationInstanceId") String configurationInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("configurationInstanceId", configurationInstanceId);
        return configurationRecipeLookup(request, Action.enablePort, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations/{configurationInstanceId}/disablePort")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Disable Port Mirroring", response = Response.class)
    @Transactional
    public Response disablePort(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                @PathParam("configurationInstanceId") String configurationInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("configurationInstanceId", configurationInstanceId);
        return configurationRecipeLookup(request, Action.disablePort, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations/{configurationInstanceId}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Activate Port Mirroring", response = Response.class)
    @Transactional
    public Response activatePort(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                 @PathParam("configurationInstanceId") String configurationInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("configurationInstanceId", configurationInstanceId);
        return configurationRecipeLookup(request, Action.activateInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations/{configurationInstanceId}/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deactivate Port Mirroring", response = Response.class)
    @Transactional
    public Response deactivatePort(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                   @PathParam("configurationInstanceId") String configurationInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("configurationInstanceId", configurationInstanceId);
        return configurationRecipeLookup(request, Action.deactivateInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][6-7]}/serviceInstances/{serviceInstanceId}/addRelationships")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add Relationships to a Service Instance", response = Response.class)
    @Transactional
    public Response addRelationships(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return configurationRecipeLookup(request, Action.addRelationships, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][6-7]}/serviceInstances/{serviceInstanceId}/removeRelationships")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove Relationships from Service Instance", response = Response.class)
    @Transactional
    public Response removeRelationships(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return configurationRecipeLookup(request, Action.removeRelationships, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create VNF on a specified version and serviceInstance", response = Response.class)
    @Transactional
    public Response createVnfInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return serviceInstances(request, Action.createInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/replace")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Replace provided VNF instance", response = Response.class)
    @Transactional
    public Response replaceVnfInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                       @PathParam("vnfInstanceId") String vnfInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.replaceInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @PUT
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update VNF on a specified version, serviceInstance and vnfInstance", response = Response.class)
    @Transactional
    public Response updateVnfInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                      @PathParam("vnfInstanceId") String vnfInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.updateInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][6-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/applyUpdatedConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Apply updated configuration", response = Response.class)
    public Response applyUpdatedConfig(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                       @PathParam("vnfInstanceId") String vnfInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.applyUpdatedConfig, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/recreate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Recreate VNF Instance", response = Response.class)
    public Response recreateVnfInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                        @PathParam("vnfInstanceId") String vnfInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.recreateInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }


    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete provided VNF instance", response = Response.class)
    @Transactional
    public Response deleteVnfInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                      @PathParam("vnfInstanceId") String vnfInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create VfModule on a specified version, serviceInstance and vnfInstance", response = Response.class)
    @Transactional
    public Response createVfModuleInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                           @PathParam("vnfInstanceId") String vnfInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.createInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}/replace")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create VfModule on a specified version, serviceInstance and vnfInstance", response = Response.class)
    @Transactional
    public Response replaceVfModuleInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                            @PathParam("vnfInstanceId") String vnfInstanceId,
                                            @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
        return serviceInstances(request, Action.replaceInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @PUT
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update VfModule on a specified version, serviceInstance, vnfInstance and vfModule", response = Response.class)
    @Transactional
    public Response updateVfModuleInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                           @PathParam("vnfInstanceId") String vnfInstanceId,
                                           @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
        return serviceInstances(request, Action.updateInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][6-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/inPlaceSoftwareUpdate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Perform VNF software update", response = Response.class)
    @Transactional
    public Response inPlaceSoftwareUpdate(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                          @PathParam("vnfInstanceId") String vnfInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.inPlaceSoftwareUpdate, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete provided VfModule instance", response = Response.class)
    @Transactional
    public Response deleteVfModuleInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                           @PathParam("vnfInstanceId") String vnfInstanceId,
                                           @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
        return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}/deactivateAndCloudDelete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deactivate and Cloud Delete VfModule instance", response = Response.class)
    @Transactional
    public Response deactivateAndCloudDeleteVfModuleInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                                             @PathParam("vnfInstanceId") String vnfInstanceId, @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
        return serviceInstances(request, Action.deactivateAndCloudDelete, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/scaleOut")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "VF Auto Scale Out", response = Response.class)
    @Transactional
    public Response scaleOutVfModule(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                     @PathParam("vnfInstanceId") String vnfInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.scaleOut, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }


    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create VolumeGroup on a specified version, serviceInstance, vnfInstance", response = Response.class)
    @Transactional
    public Response createVolumeGroupInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                              @PathParam("vnfInstanceId") String vnfInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        return serviceInstances(request, Action.createInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @PUT
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups/{volumeGroupInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update VolumeGroup on a specified version, serviceInstance, vnfInstance and volumeGroup", response = Response.class)
    @Transactional
    public Response updateVolumeGroupInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                              @PathParam("vnfInstanceId") String vnfInstanceId,
                                              @PathParam("volumeGroupInstanceId") String volumeGroupInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("volumeGroupInstanceId", volumeGroupInstanceId);
        return serviceInstances(request, Action.updateInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups/{volumeGroupInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete provided VolumeGroup instance", response = Response.class)
    @Transactional
    public Response deleteVolumeGroupInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                              @PathParam("vnfInstanceId") String vnfInstanceId,
                                              @PathParam("volumeGroupInstanceId") String volumeGroupInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("vnfInstanceId", vnfInstanceId);
        instanceIdMap.put("volumeGroupInstanceId", volumeGroupInstanceId);
        return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @POST
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/networks")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create NetworkInstance on a specified version and serviceInstance ", response = Response.class)
    @Transactional
    public Response createNetworkInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        return serviceInstances(request, Action.createInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @PUT
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/networks/{networkInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update VolumeGroup on a specified version, serviceInstance, networkInstance", response = Response.class)
    @Transactional
    public Response updateNetworkInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                          @PathParam("networkInstanceId") String networkInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("networkInstanceId", networkInstanceId);
        return serviceInstances(request, Action.updateInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    @DELETE
    @Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/networks/{networkInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete provided Network instance", response = Response.class)
    @Transactional
    public Response deleteNetworkInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                          @PathParam("networkInstanceId") String networkInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
        String requestId = getRequestId(requestContext);
        HashMap<String, String> instanceIdMap = new HashMap<>();
        instanceIdMap.put("serviceInstanceId", serviceInstanceId);
        instanceIdMap.put("networkInstanceId", networkInstanceId);
        return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
    }

    private String getRequestUri(ContainerRequestContext context) {
        String requestUri = context.getUriInfo().getPath();
        requestUri = requestUri.substring(requestUri.indexOf("/serviceInstantiation/") + 22);
        return requestUri;
    }

    private Response serviceInstances(String requestJSON, Actions action, HashMap<String, String> instanceIdMap,
                                      String version, String requestId, String requestUri) throws ApiException {
        Boolean aLaCarte = null;
        String apiVersion = version.substring(1);

        ServiceInstancesRequest sir = convertJsonToServiceInstanceRequest(requestJSON, action, msoRequest, requestId, requestUri);
        String requestScope = deriveRequestScope(action, sir, requestUri);
        InfraActiveRequests currentActiveReq = msoRequest.createRequestObject(sir, action, requestId, Status.PENDING, requestJSON, requestScope);
        if (sir.getRequestDetails().getRequestParameters() != null) {
            aLaCarte = sir.getRequestDetails().getRequestParameters().getALaCarte();
        }
        parseRequest(sir, instanceIdMap, action, version, aLaCarte, currentActiveReq);
        setInstanceId(currentActiveReq, requestScope, null, instanceIdMap);

        int requestVersion = Integer.parseInt(version.substring(1));
        boolean alaCarteFlag = msoRequest.getAlacarteFlag(sir);
        String vnfType = msoRequest.getVnfType(sir, requestScope, action, requestVersion);
        String networkType = msoRequest.getNetworkType(sir, requestScope);

        if (requestScope.equalsIgnoreCase(ModelType.vnf.name()) && vnfType != null) {
            currentActiveReq.setVnfType(vnfType);
        } else if (requestScope.equalsIgnoreCase(ModelType.network.name()) && networkType != null) {
            currentActiveReq.setNetworkType(networkType);
        }

        String instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();
        InfraActiveRequests dup = duplicateCheck(action, instanceIdMap, instanceName, requestScope, currentActiveReq);

        if (dup != null) {
            buildErrorOnDuplicateRecord(currentActiveReq, instanceIdMap, instanceName, requestScope, dup);
        }

        ModelType modelType;
        ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
        if (action == Action.applyUpdatedConfig || action == Action.inPlaceSoftwareUpdate) {
            modelType = ModelType.vnf;
        } else {
            modelType = modelInfo.getModelType();
        }

        Boolean isBaseVfModule = false;

        if (modelType.equals(ModelType.vfModule)) {
            // Get VF Module-specific base module indicator
            VfModule vfm = null;

            String modelVersionId = modelInfo.getModelVersionId();

            if (modelVersionId != null) {
                vfm = catalogDbClient.getVfModuleByModelUUID(modelVersionId);
            } else if (modelInfo.getModelInvariantId() != null && modelInfo.getModelVersion() != null) {
                vfm = catalogDbClient.getVfModuleByModelInvariantUUIDAndModelVersion(modelInfo.getModelInvariantId(), modelInfo.getModelVersion());
            }

            if (vfm != null) {
                if (vfm.getIsBase()) {
                    isBaseVfModule = true;
                }
            } else if (action == Action.createInstance || action == Action.updateInstance) {
                // There is no entry for this vfModuleType with this version, if specified, in VF_MODULE table in Catalog DB.
                // This request cannot proceed

                String serviceVersionText = "";
                String sdcServiceModelVersion = msoRequest.getSDCServiceModelVersion(sir);

                if (sdcServiceModelVersion != null && !sdcServiceModelVersion.isEmpty()) {
                    serviceVersionText = " with version " + sdcServiceModelVersion;
                }

                String errorMessage = "VnfType " + vnfType + " and VF Module Model Name " + modelInfo.getModelName() + serviceVersionText + " not found in MSO Catalog DB";
                ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
                VfModuleNotFoundException vfModuleException = new VfModuleNotFoundException.Builder(errorMessage, HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo).build();
                updateStatusToFailed(currentActiveReq, vfModuleException.getMessage());

                throw vfModuleException;
            }
        }


        String serviceInstanceId = Objects.toString(sir.getServiceInstanceId(), "");
        String vnfId = Objects.toString(sir.getVnfInstanceId(), "");
        String vfModuleId = Objects.toString(sir.getVfModuleInstanceId(), "");
        String volumeGroupId = Objects.toString(sir.getVolumeGroupInstanceId(), "");
        String networkId = Objects.toString(sir.getNetworkInstanceId(), "");
        String correlationId = Objects.toString(sir.getCorrelationId(), "");

        infraActiveRequestsClient.save(currentActiveReq);

        if (!requestScope.equalsIgnoreCase(ModelType.service.name())) {
            aLaCarte = true;
        } else if (aLaCarte == null) {
            aLaCarte = false;
        }

        String serviceInstanceType = msoRequest.getServiceInstanceType(sir, requestScope);
        String vfModuleType = msoRequest.getVfModuleType(sir, requestScope, action, requestVersion);
        RecipeLookupResult recipeLookupResult = getServiceInstanceOrchestrationURI(sir, action, alaCarteFlag, currentActiveReq);

        return postBPELRequest(currentActiveReq, action, requestId, requestJSON, recipeLookupResult.getOrchestrationURI(), recipeLookupResult.getRecipeTimeout(),
                isBaseVfModule, serviceInstanceId, correlationId, vnfId, vfModuleId, volumeGroupId, networkId, null,
                serviceInstanceType, vnfType, vfModuleType, networkType, apiVersion, aLaCarte, requestUri, requestScope, sir);
    }

    private String deriveRequestScope(Actions action, ServiceInstancesRequest sir, String requestUri) {
        if (action == Action.inPlaceSoftwareUpdate || action == Action.applyUpdatedConfig) {
            return (ModelType.vnf.name());
        } else {
            String requestScope;
            if (sir.getRequestDetails().getModelInfo().getModelType() == null) {
                requestScope = requestScopeFromUri(requestUri);
            } else {
                requestScope = sir.getRequestDetails().getModelInfo().getModelType().name();
            }
            return requestScope;
        }
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
        } else {
            requestScope = ModelType.service.name();
        }
        return requestScope;
    }

    private Response postBPELRequest(InfraActiveRequests currentActiveReq, Actions action, String requestId,
                                     String msoRawRequest, String orchestrationUri, int timeOut, Boolean isBaseVfModule,
                                     String serviceInstanceId, String correlationId, String vnfId, String vfModuleId,
                                     String volumeGroupId, String networkId, String configurationId,
                                     String serviceInstanceType, String vnfType, String vfModuleType, String networkType,
                                     String apiVersion, boolean aLaCarte, String requestUri, String requestScope,
                                     ServiceInstancesRequest sir) throws ApiException {
        RequestClient requestClient = null;
        HttpResponse response;
        try {
            requestClient = reqClientFactory.getRequestClient(orchestrationUri);
            response = requestClient.post(new RequestClientParameter.Builder()
                    .setRequestId(requestId)
                    .setBaseVfModule(isBaseVfModule)
                    .setRecipeTimeout(timeOut)
                    .setRequestAction(action.toString())
                    .setServiceInstanceId(serviceInstanceId)
                    .setCorrelationId(correlationId)
                    .setVnfId(vnfId)
                    .setVfModuleId(vfModuleId)
                    .setVolumeGroupId(volumeGroupId)
                    .setNetworkId(networkId)
                    .setConfigurationId(configurationId)
                    .setServiceType(serviceInstanceType)
                    .setVnfType(vnfType)
                    .setVfModuleType(vfModuleType)
                    .setNetworkType(networkType)
                    .setRequestDetails(mapJSONtoMSOStyle(msoRawRequest, sir, aLaCarte, action))
                    .setApiVersion(apiVersion)
                    .setALaCarte(aLaCarte)
                    .setRecipeParamXsd(null)
                    .setRequestUri(requestUri).build());


        } catch (Exception e) {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MsoLogger.ErrorCode.AvailabilityError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            String url = requestClient != null ? requestClient.getUrl() : "";
            ClientConnectionException clientException = new ClientConnectionException.Builder(url, HttpStatus.SC_BAD_GATEWAY, ErrorNumbers.SVC_NO_SERVER_RESOURCES).cause(e).errorInfo(errorLoggerInfo).build();
            updateStatusToFailed(currentActiveReq, clientException.getMessage());
            throw clientException;
        }

        if (response == null) {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MsoLogger.ErrorCode.BusinessProcesssError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ClientConnectionException clientException = new ClientConnectionException.Builder(requestClient.getUrl(), HttpStatus.SC_BAD_GATEWAY, ErrorNumbers.SVC_NO_SERVER_RESOURCES).errorInfo(errorLoggerInfo).build();
            updateStatusToFailed(currentActiveReq, clientException.getMessage());
            throw clientException;
        }

        ResponseHandler respHandler;
        int bpelStatus;
        try {
            respHandler = new ResponseHandler(response, requestClient.getType());
            bpelStatus = respHandler.getStatus();
        } catch (ApiException e) {
            msoLogger.error(e);
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException = new ValidateException.Builder("Exception caught mapping Camunda JSON response to object", HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                    .errorInfo(errorLoggerInfo).build();
            updateStatusToFailed(currentActiveReq, validateException.getMessage());
            throw validateException;
        }

        // BPEL accepted the request, the request is in progress
        if (bpelStatus == HttpStatus.SC_ACCEPTED) {
            CamundaResponse camundaResp = respHandler.getResponse();
            if ("Success".equalsIgnoreCase(camundaResp.getMessage())) {
                ServiceInstancesResponse jsonResponse;
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    jsonResponse = mapper.readValue(camundaResp.getResponse(), ServiceInstancesResponse.class);
                } catch (IOException e) {
                    msoLogger.error(e);
                    ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
                    ValidateException validateException = new ValidateException.Builder("Exception caught mapping Camunda JSON response to object", HttpStatus.SC_NOT_ACCEPTABLE, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                            .errorInfo(errorLoggerInfo).build();
                    updateStatusToFailed(currentActiveReq, validateException.getMessage());
                    throw validateException;
                }

                currentActiveReq.setRequestStatus(Status.IN_PROGRESS.name());
                setInstanceId(currentActiveReq, requestScope, jsonResponse.getRequestReferences().getInstanceId(), new HashMap<>());

                infraActiveRequestsClient.save(currentActiveReq);
                return builder.buildResponse(HttpStatus.SC_ACCEPTED, requestId, jsonResponse, apiVersion);
            }
        }
        String camundaJSONResponseBody = respHandler.getResponseBody();
        if (!Strings.isNullOrEmpty(camundaJSONResponseBody)) {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.BusinessProcesssError).errorSource(requestClient.getUrl()).build();
            BPMNFailureException bpmnException = new BPMNFailureException.Builder(String.valueOf(bpelStatus) + camundaJSONResponseBody, bpelStatus, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
                    .errorInfo(errorLoggerInfo).build();

            updateStatusToFailed(currentActiveReq, bpmnException.getMessage());

            throw bpmnException;
        } else {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.BusinessProcesssError).errorSource(requestClient.getUrl()).build();
            BPMNFailureException servException = new BPMNFailureException.Builder(String.valueOf(bpelStatus), bpelStatus, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
                    .errorInfo(errorLoggerInfo).build();
            updateStatusToFailed(currentActiveReq, servException.getMessage());
            throw servException;
        }
    }

    private void setInstanceId(InfraActiveRequests currentActiveReq, String requestScope, String instanceId, Map<String, String> instanceIdMap) {
        if (StringUtils.isNotBlank(instanceId)) {
            if (ModelType.service.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setServiceInstanceId(instanceId);
            } else if (ModelType.vnf.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setVnfId(instanceId);
            } else if (ModelType.vfModule.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setVfModuleId(instanceId);
            } else if (ModelType.volumeGroup.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setVolumeGroupId(instanceId);
            } else if (ModelType.network.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setNetworkId(instanceId);
            } else if (ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
                currentActiveReq.setConfigurationId(instanceId);
            }
        } else if (CollectionUtils.isEmpty(instanceIdMap)) {
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
        }
    }

    @VisibleForTesting
    protected String mapJSONtoMSOStyle(String msoRawRequest, ServiceInstancesRequest serviceInstRequest, boolean isAlaCarte, Actions action) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        ServiceInstancesRequest sir = mapper.readValue(msoRawRequest, ServiceInstancesRequest.class);
        if (!isAlaCarte && Action.createInstance.equals(action) && serviceInstRequest != null &&
                serviceInstRequest.getRequestDetails() != null &&
                serviceInstRequest.getRequestDetails().getRequestParameters() != null) {
            sir.getRequestDetails().setCloudConfiguration(serviceInstRequest.getRequestDetails().getCloudConfiguration());
            sir.getRequestDetails().getRequestParameters().setUserParams(serviceInstRequest.getRequestDetails().getRequestParameters().getUserParams());
        }
        msoLogger.debug("Value as string: " + mapper.writeValueAsString(sir));
        return mapper.writeValueAsString(sir);
    }

    /**
     * Throws an exception about duplicate record. Use when duplicate record is found.
     */
    private void buildErrorOnDuplicateRecord(InfraActiveRequests currentActiveReq, HashMap<String, String> instanceIdMap,
                                             String instanceName, String requestScope, InfraActiveRequests dup) throws ApiException {
        String instance = Objects.toString(instanceName, instanceIdMap.get(requestScope + "InstanceId"));
        ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_FOUND, MsoLogger.ErrorCode.SchemaError)
                .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
        DuplicateRequestException dupException = new DuplicateRequestException.Builder(requestScope, instance, dup.getRequestStatus(), dup.getRequestId(), HttpStatus.SC_CONFLICT, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
                .errorInfo(errorLoggerInfo).build();
        updateStatusToFailed(currentActiveReq, dupException.getMessage());
        throw dupException;
    }

    private InfraActiveRequests duplicateCheck(Actions action, HashMap<String, String> instanceIdMap,
                                               String instanceName, String requestScope, InfraActiveRequests currentActiveReq) throws ApiException {
        try {
            if (!(instanceName == null && requestScope.equals("service") && (action == Action.createInstance || action == Action.activateInstance || action == Action.assignInstance))) {
                return infraActiveRequestsClient.checkInstanceNameDuplicate(instanceIdMap, instanceName, requestScope);
            } else {
                return null;
            }
        } catch (Exception e) {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_CHECK_EXC, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException = new ValidateException.Builder("Duplicate Check Request", HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
                    .errorInfo(errorLoggerInfo).build();
            updateStatusToFailed(currentActiveReq, validateException.getMessage());
            throw validateException;
        }
    }

    private ServiceInstancesRequest convertJsonToServiceInstanceRequest(String requestJSON, Actions action,
                                                                        MsoRequest msoRequest, String requestId, String requestUri) throws ApiException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(requestJSON, ServiceInstancesRequest.class);
        } catch (IOException e) {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR,
                    MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException = new ValidateException.Builder("Error mapping request: " + e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                    .errorInfo(errorLoggerInfo).build();
            String requestScope = requestScopeFromUri(requestUri);
            msoRequest.createErrorRequestRecord(Status.FAILED, requestId, validateException.getMessage(), action, requestScope, requestJSON);
            throw validateException;
        }
    }

    private void parseRequest(ServiceInstancesRequest sir, HashMap<String, String> instanceIdMap, Actions action, String version,
                              Boolean aLaCarte, InfraActiveRequests currentActiveReq) throws ValidateException {
        int reqVersion = Integer.parseInt(version.substring(1));
        try {
            msoRequest.parse(sir, instanceIdMap, action, version, reqVersion, aLaCarte);
        } catch (Exception e) {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException = new ValidateException.Builder("Error parsing request: " + e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                    .errorInfo(errorLoggerInfo).build();
            updateStatusToFailed(currentActiveReq, validateException.getMessage());
            throw validateException;
        }
    }

    private RecipeLookupResult getServiceInstanceOrchestrationURI(ServiceInstancesRequest sir, Actions action, boolean alaCarteFlag,
                                                                  InfraActiveRequests currentActiveReq) throws ApiException {
        RecipeLookupResult recipeLookupResult = null;
        //if the aLaCarte flag is set to TRUE, the API-H should choose the VID_DEFAULT recipe for the requested action
        ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
        // Query MSO Catalog DB

        if (action == Action.applyUpdatedConfig || action == Action.inPlaceSoftwareUpdate) {
            recipeLookupResult = getDefaultVnfUri(sir, action);
        } else if (modelInfo.getModelType().equals(ModelType.service)) {
            try {
                recipeLookupResult = getServiceURI(sir, action, alaCarteFlag);
            } catch (IOException e) {
                ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


                ValidateException validateException = new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                        .errorInfo(errorLoggerInfo).build();

                updateStatusToFailed(currentActiveReq, validateException.getMessage());

                throw validateException;
            }
        } else if (modelInfo.getModelType().equals(ModelType.vfModule) ||
                modelInfo.getModelType().equals(ModelType.volumeGroup) || modelInfo.getModelType().equals(ModelType.vnf)) {
            try {
                recipeLookupResult = getVnfOrVfModuleUri(sir, action);
            } catch (ValidationException e) {
                ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
                ValidateException validateException = new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                        .errorInfo(errorLoggerInfo).build();
                updateStatusToFailed(currentActiveReq, validateException.getMessage());
                throw validateException;
            }
        } else if (modelInfo.getModelType().equals(ModelType.network)) {
            try {
                recipeLookupResult = getNetworkUri(sir, action);
            } catch (ValidationException e) {
                ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
                ValidateException validateException = new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                        .errorInfo(errorLoggerInfo).build();
                updateStatusToFailed(currentActiveReq, validateException.getMessage());
                throw validateException;
            }
        }

        if (recipeLookupResult == null) {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            RecipeNotFoundException recipeNotFoundExceptionException = new RecipeNotFoundException.Builder("Recipe could not be retrieved from catalog DB.", HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_GENERAL_SERVICE_ERROR)
                    .errorInfo(errorLoggerInfo).build();
            updateStatusToFailed(currentActiveReq, recipeNotFoundExceptionException.getMessage());
            throw recipeNotFoundExceptionException;
        }
        return recipeLookupResult;
    }

    private RecipeLookupResult getServiceURI(ServiceInstancesRequest servInstReq, Actions action, boolean alaCarteFlag) throws IOException {
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
                recipe = catalogDbClient.getFirstByServiceModelUUIDAndAction(serviceRecord.getModelUUID(), action.toString());
            }
        } else {
            serviceRecord = catalogDbClient.getServiceByID(modelInfo.getModelVersionId());
            recipe = catalogDbClient.getFirstByServiceModelUUIDAndAction(modelInfo.getModelVersionId(), action.toString());
            if (recipe == null) {
                serviceRecordList = catalogDbClient.getServiceByModelInvariantUUIDOrderByModelVersionDesc(modelInfo.getModelInvariantId());
                if (!serviceRecordList.isEmpty()) {
                    for (org.onap.so.db.catalog.beans.Service record : serviceRecordList) {
                        recipe = catalogDbClient.getFirstByServiceModelUUIDAndAction(record.getModelUUID(), action.toString());
                        if (recipe != null) {
                            break;
                        }
                    }
                }
            }
        }

        //if an aLaCarte flag was sent in the request, throw an error if the recipe was not found
        RequestParameters reqParam = requestDetails.getRequestParameters();
        if (reqParam != null && alaCarteFlag && recipe == null) {
            return null;
        } else if (!alaCarteFlag && recipe != null && Action.createInstance.equals(action)) {
            mapToLegacyRequest(requestDetails);
        } else if (recipe == null) {  //aLaCarte wasn't sent, so we'll try the default
            serviceRecord = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
            recipe = catalogDbClient.getFirstByServiceModelUUIDAndAction(serviceRecord.getModelUUID(), action.toString());
        }
        if (modelInfo.getModelVersionId() == null) {
            modelInfo.setModelVersionId(serviceRecord.getModelUUID());
        }
        if (recipe == null) {
            return null;
        }
        return new RecipeLookupResult(recipe.getOrchestrationUri(), recipe.getRecipeTimeout());
    }

    @VisibleForTesting
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

    @VisibleForTesting
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

    private Optional<CloudConfiguration> addCloudConfig(CloudConfiguration sourceCloudConfiguration) {
        CloudConfiguration targetConfiguration = new CloudConfiguration();
        if (sourceCloudConfiguration != null) {
            targetConfiguration.setAicNodeClli(sourceCloudConfiguration.getAicNodeClli());
            targetConfiguration.setTenantId(sourceCloudConfiguration.getTenantId());
            targetConfiguration.setLcpCloudRegionId(sourceCloudConfiguration.getLcpCloudRegionId());
            return Optional.of(targetConfiguration);
        }
        return Optional.empty();
    }

    @VisibleForTesting
    protected List<Map<String, Object>> configureUserParams(RequestParameters reqParams) throws IOException {
        msoLogger.debug("Configuring UserParams for Macro Request");
        Map<String, Object> userParams = new HashMap<>();

        for (Map<String, Object> params : reqParams.getUserParams()) {
            if (params.containsKey("service")) {
                Service service = serviceMapper(params);

                addUserParams(userParams, service.getInstanceParams());

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

    private List<Map<String, Object>> mapFlatMapToNameValue(Map<String, Object> flatMap) {
        List<Map<String, Object>> targetUserParams = new ArrayList<>();

        for (Map.Entry<String, Object> map : flatMap.entrySet()) {
            Map<String, Object> targetMap = new HashMap<>();
            targetMap.put("name", map.getKey());
            targetMap.put("value", map.getValue());
            targetUserParams.add(targetMap);
        }
        return targetUserParams;
    }

    private RecipeLookupResult getVnfOrVfModuleUri(ServiceInstancesRequest servInstReq, Actions action) throws ValidationException {

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
                //    			a.	For a vnf request (only create, no update currently):
                //    				i.	(v3-v4) If modelInfo.modelCustomizationId is provided, use it to validate catalog DB has record in vnf_resource_customization.model_customization_uuid.
                //    				ii.	(v2-v4) If modelInfo.modelCustomizationId is NOT provided (because it is a pre-1702 ASDC model or pre-v3), then modelInfo.modelCustomizationName must have
                //    					been provided (else create request should be rejected).  APIH should use the relatedInstance.modelInfo[service].modelVersionId** + modelInfo[vnf].modelCustomizationName
                //    					to â€œjoinâ€�? service_to_resource_customizations with vnf_resource_customization to confirm a vnf_resource_customization.model_customization_uuid  record exists.
                //    				**If relatedInstance.modelInfo[service].modelVersionId  was not provided, use relatedInstance.modelInfo[service].modelInvariantId + modelVersion instead to lookup modelVersionId
                //    					(MODEL_UUID) in SERVICE table.
                //    				iii.	Regardless of how the value was provided/obtained above, APIH must always populate vnfModelCustomizationId in bpmnRequest.  It would be assumed it was MSO generated
                //    					during 1707 data migration if VID did not provide it originally on request.
                //    				iv.	Note: continue to construct the â€œvnf-typeâ€�? value and pass to BPMN (must still be populated in A&AI).
                //    				1.	If modelCustomizationName is NOT provided on a vnf/vfModule request, use modelCustomizationId to look it up in our catalog to construct vnf-type value to pass to BPMN.

                VnfResource vnfResource = null;
                VnfResourceCustomization vrc = null;
                // Validation for vnfResource

                if (modelCustomizationId != null) {
                    vrc = catalogDbClient.getVnfResourceCustomizationByModelCustomizationUUID(modelCustomizationId);
                    if (vrc != null) {
                        vnfResource = vrc.getVnfResources();
                    }
                } else {
                    org.onap.so.db.catalog.beans.Service service = catalogDbClient.getServiceByID(relatedInstanceModelVersionId);
                    if (service == null) {
                        service = catalogDbClient.getServiceByModelVersionAndModelInvariantUUID(relatedInstanceVersion, relatedInstanceModelInvariantId);
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
                        vnfRecipe = catalogDbClient.getFirstVnfRecipeByNfRoleAndAction(vrc.getNfRole(), action.toString());
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
				/*				(v5-v7) If modelInfo.modelCustomizationId is NOT provided (because it is a pre-1702 ASDC model or pre-v3), then modelInfo.modelCustomizationName must have 
				//				been provided (else create request should be rejected).  APIH should use the relatedInstance.modelInfo[vnf].modelVersionId + modelInfo[vnf].modelCustomizationName 
				//				to join vnf_to_resource_customizations with vf_resource_customization to confirm a vf_resource_customization.model_customization_uuid  record exists.
				//				Once the vnfs model_customization_uuid has been obtained, use it to find all vfModule customizations for that vnf customization in the vnf_res_custom_to_vf_module_custom join table. 
				//				For each vf_module_cust_model_customization_uuid value returned, use that UUID to query vf_module_customization table along with modelInfo[vfModule|volumeGroup].modelVersionId to 
				// 				confirm record matches request data (and to identify the modelCustomizationId associated with the vfModule in the request). This means taking each record found 
				//    			in vf_module_customization and looking up in vf_module (using vf_module_customizationâ€™s FK into vf_module) to find a match on MODEL_INVARIANT_UUID (modelInvariantId) 
				//				and MODEL_VERSION (modelVersion).
				*/
                VfModuleCustomization vfmc = null;
                VnfResource vnfr;
                VnfResourceCustomization vnfrc;
                VfModule vfModule = null;

                if (modelInfo.getModelCustomizationId() != null) {
                    vfmc = catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(modelInfo.getModelCustomizationId());
                } else {
                    vnfr = catalogDbClient.getVnfResourceByModelUUID(relatedInstanceModelVersionId);
                    if (vnfr == null) {
                        vnfr = catalogDbClient.getFirstVnfResourceByModelInvariantUUIDAndModelVersion(relatedInstanceModelInvariantId, relatedInstanceVersion);
                    }
                    vnfrc = catalogDbClient.getFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources(relatedInstanceModelCustomizationName, vnfr);

                    List<VfModuleCustomization> list = vnfrc.getVfModuleCustomizations();

                    String vfModuleModelUUID = modelInfo.getModelVersionId();
                    for (VfModuleCustomization vf : list) {
                        VfModuleCustomization vfmCustom;
                        if (vfModuleModelUUID != null) {
                            vfmCustom = catalogDbClient.getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID(vf.getModelCustomizationUUID(), vfModuleModelUUID);
                            if (vfmCustom != null) {
                                vfModule = vfmCustom.getVfModule();
                            }
                        } else {
                            vfmCustom = catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(vf.getModelCustomizationUUID());
                            if (vfmCustom != null) {
                                vfModule = vfmCustom.getVfModule();
                            } else {
                                vfModule = catalogDbClient.getVfModuleByModelInvariantUUIDAndModelVersion(relatedInstanceModelInvariantId, relatedInstanceVersion);
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
                    vfModule = vfmc.getVfModule(); // can't be null as vfModuleModelUUID is not-null property in VfModuleCustomization table
                }

                if (modelInfo.getModelVersionId() == null) {
                    modelInfo.setModelVersionId(vfModule.getModelUUID());
                }


                recipe = catalogDbClient.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(vfModule.getModelUUID(), vnfComponentType, action.toString());
                if (recipe == null) {
                    List<VfModule> vfModuleRecords = catalogDbClient.getVfModuleByModelInvariantUUIDOrderByModelVersionDesc(vfModule.getModelInvariantUUID());
                    if (!vfModuleRecords.isEmpty()) {
                        for (VfModule record : vfModuleRecords) {
                            recipe = catalogDbClient.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(record.getModelUUID(), vnfComponentType, action.toString());
                            if (recipe != null) {
                                break;
                            }
                        }
                    }
                }
                if (recipe == null) {
                    recipe = catalogDbClient.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(defaultSource, vnfComponentType, action.toString());
                    if (recipe == null) {
                        recipe = catalogDbClient.getFirstVnfComponentsRecipeByVnfComponentTypeAndAction(vnfComponentType, action.toString());
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
                recipe = catalogDbClient.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(defaultSource, vnfComponentType, action.toString());

                if (recipe == null) {
                    return null;
                }
            }
        }

        return new RecipeLookupResult(recipe.getOrchestrationUri(), recipe.getRecipeTimeout());
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
        ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
        String modelName = modelInfo.getModelName();
        Recipe recipe = null;

        if (modelInfo.getModelCustomizationId() != null) {
            NetworkResource networkResource = catalogDbClient.getNetworkResourceCustomizationByModelCustomizationUUID(modelInfo.getModelCustomizationId()).getNetworkResource();
            if (networkResource != null) {
                if (modelInfo.getModelVersionId() == null) {
                    modelInfo.setModelVersionId(networkResource.getModelUUID());
                }
                recipe = catalogDbClient.getFirstNetworkRecipeByModelNameAndAction(networkResource.getModelName(), action.toString());
            } else {
                throw new ValidationException("no catalog entry found");
            }
        } else {
            //ok for version < 3 and action delete
            if (modelName != null) {
                recipe = catalogDbClient.getFirstNetworkRecipeByModelNameAndAction(modelName, action.toString());
            }
        }
        String defaultNetworkType = getDefaultModel(sir);
        if (recipe == null) {
            recipe = catalogDbClient.getFirstNetworkRecipeByModelNameAndAction(defaultNetworkType, action.toString());
        }

        if (recipe == null) {
            return null;
        }
        return new RecipeLookupResult(recipe.getOrchestrationUri(), recipe.getRecipeTimeout());
    }

    private Optional<String> retrieveModelName(RequestParameters requestParams) {
        String requestTestApi = null;

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
            return Optional.of(TestApi.valueOf(requestTestApi).getModelName());
        } catch (Exception e) {
            msoLogger.warnSimple("Catching the exception on the valueOf enum call and continuing", e);
            throw new IllegalArgumentException("Invalid TestApi is provided", e);
        }
    }

    private String getDefaultModel(ServiceInstancesRequest sir) {
        String defaultModel = sir.getRequestDetails().getRequestInfo().getSource() + "_DEFAULT";
        Optional<String> oModelName = retrieveModelName(sir.getRequestDetails().getRequestParameters());
        if (oModelName.isPresent()) {
            defaultModel = oModelName.get();
        }
        return defaultModel;
    }

    private Response configurationRecipeLookup(String requestJSON, Action action, HashMap<String, String> instanceIdMap, String version, String requestId, String requestUri) throws ApiException {
        Boolean aLaCarte = null;

        ServiceInstancesRequest sir = convertJsonToServiceInstanceRequest(requestJSON, action, msoRequest, requestId, requestUri);
        String requestScope = deriveRequestScope(action, sir, requestUri);
        InfraActiveRequests currentActiveReq = msoRequest.createRequestObject(sir, action, requestId, Status.IN_PROGRESS, requestJSON, requestScope);
        if (sir.getRequestDetails().getRequestParameters() != null) {
            aLaCarte = sir.getRequestDetails().getRequestParameters().getALaCarte();
        }
        parseRequest(sir, instanceIdMap, action, version, aLaCarte, currentActiveReq);
        setInstanceId(currentActiveReq, requestScope, null, instanceIdMap);
        String instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();

        InfraActiveRequests dup = duplicateCheck(action, instanceIdMap, instanceName, requestScope, currentActiveReq);

        if (instanceIdMap != null && dup != null) {
            buildErrorOnDuplicateRecord(currentActiveReq, instanceIdMap, instanceName, requestScope, dup);
        }

        ServiceInstancesResponse serviceResponse = new ServiceInstancesResponse();
        RequestReferences referencesResponse = new RequestReferences();
        referencesResponse.setRequestId(requestId);
        serviceResponse.setRequestReferences(referencesResponse);


        String orchestrationUri = env.getProperty(CommonConstants.ALACARTE_ORCHESTRATION);
        String timeOut = env.getProperty(CommonConstants.ALACARTE_RECIPE_TIMEOUT);

        if (StringUtils.isBlank(orchestrationUri) || StringUtils.isBlank(timeOut)) {
            String error = StringUtils.isBlank(orchestrationUri) ? "ALaCarte Orchestration URI not found in properties" : "ALaCarte Recipe Timeout not found in properties";
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException = new ValidateException.Builder(error, HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_GENERAL_SERVICE_ERROR)
                    .errorInfo(errorLoggerInfo).build();
            updateStatusToFailed(currentActiveReq, validateException.getMessage());
            throw validateException;
        }

        String serviceInstanceId = Objects.toString(sir.getServiceInstanceId(), "");
        String configurationId = Objects.toString(sir.getConfigurationId(), "");
        String correlationId = Objects.toString(sir.getCorrelationId(), "");
        infraActiveRequestsClient.save(currentActiveReq);

        if (!requestScope.equalsIgnoreCase(ModelType.service.name())) {
            aLaCarte = true;
        } else if (aLaCarte == null) {
            aLaCarte = false;
        }

        String apiVersion = version.substring(1);
        return postBPELRequest(currentActiveReq, action, requestId, requestJSON, orchestrationUri, Integer.parseInt(timeOut), false,
                serviceInstanceId, correlationId, null, null, null, null, configurationId, null, null, null, null, apiVersion, aLaCarte, requestUri, requestScope, null);
    }

    public String getRequestId(ContainerRequestContext requestContext) throws ValidateException {
        String requestId = null;
        if (requestContext.getProperty("requestId") != null) {
            requestId = requestContext.getProperty("requestId").toString();
        }
        if (UUIDChecker.isValidUUID(requestId)) {
            return requestId;
        } else {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new ValidateException.Builder("Request Id " + requestId + " is not a valid UUID", HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER)
                    .errorInfo(errorLoggerInfo).build();
        }
    }

    private void updateStatusToFailed(InfraActiveRequests aq, String errorMessage) {
        aq.setStatusMessage(errorMessage);
        aq.setProgress(100L);
        aq.setRequestStatus(Status.FAILED.toString());
        Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
        aq.setEndTime(endTimeStamp);
        infraActiveRequestsClient.save(aq);
    }
}
