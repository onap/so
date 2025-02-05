/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.infra.rest;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandler.filters.ResponseUpdater;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.infra.rest.exception.AAIEntityNotFound;
import org.onap.so.apihandlerinfra.infra.rest.exception.NoRecipeException;
import org.onap.so.apihandlerinfra.infra.rest.exception.WorkflowEngineConnectionException;
import org.onap.so.apihandlerinfra.infra.rest.handler.VFModuleRestHandler;
import org.onap.so.apihandlerinfra.infra.rest.handler.VolumeRestHandler;
import org.onap.so.db.catalog.beans.Recipe;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.logger.HttpHeadersConstants;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Component("VolumesV8")
@Path("/onap/so/infra/serviceInstantiation")
public class Volumes {

    @Autowired
    private BpmnRequestBuilder requestBuilder;

    @Autowired
    private VFModuleRestHandler vfModuleRestHandler;

    @Autowired
    private VolumeRestHandler volumeRestHandler;

    @DELETE
    @ResponseUpdater
    @Path("/{version:[vV][8]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups/{volumeGroupInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete a VfModule instance", responses = @ApiResponse(content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = ServiceInstancesResponse.class)))))
    @Transactional
    public Response deleteVfModuleInstance(@PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @PathParam("volumeGroupInstanceId") String volumeGroupId, @Context ContainerRequestContext requestContext)
            throws AAIEntityNotFound, NoRecipeException, JsonProcessingException, WorkflowEngineConnectionException,
            ValidateException {

        String requestId = volumeRestHandler.getRequestId(requestContext);
        String requestorId = MDC.get(HttpHeadersConstants.REQUESTOR_ID);
        String source = MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME);
        String requestURL = requestContext.getUriInfo().getAbsolutePath().toString();
        InfraActiveRequests currentRequest = volumeRestHandler.createInfraActiveRequestForDelete(requestId,
                volumeGroupId, serviceInstanceId, vnfInstanceId, requestorId, source, requestURL);
        ServiceInstancesRequest request = requestBuilder.buildVolumeGroupDeleteRequest(vnfInstanceId, volumeGroupId);
        volumeRestHandler.saveInstanceName(request, currentRequest);
        volumeRestHandler.checkDuplicateRequest(serviceInstanceId, vnfInstanceId, volumeGroupId,
                request.getRequestDetails().getRequestInfo().getInstanceName(), currentRequest.getRequestId());
        Recipe recipe = vfModuleRestHandler.findVfModuleRecipe(
                request.getRequestDetails().getModelInfo().getModelCustomizationId(), ModelType.volumeGroup.toString(),
                Action.deleteInstance.toString());
        volumeRestHandler
                .callWorkflowEngine(
                        volumeRestHandler.buildRequestParams(request, volumeRestHandler.getRequestUri(requestContext),
                                requestId, serviceInstanceId, vnfInstanceId, volumeGroupId),
                        recipe.getOrchestrationUri());
        ServiceInstancesResponse response = volumeRestHandler.createResponse(volumeGroupId, requestId, requestContext);
        return Response.status(HttpStatus.ACCEPTED.value()).entity(response).build();
    }
}
