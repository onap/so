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

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandler.filters.ResponseUpdater;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.infra.rest.handler.NetworkRestHandler;
import org.onap.so.db.catalog.beans.Recipe;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Component
@Path("/onap/so/infra/serviceInstantiation")
public class Network {

    @Autowired
    private NetworkRestHandler networkRestHandler;

    @Autowired
    private BpmnRequestBuilder requestBuilder;

    @DELETE
    @ResponseUpdater
    @Path("/{version:[vV][8]}/serviceInstances/{serviceInstanceId}/networks/{networkInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete provided Network instance", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response deleteNetworkInstance(@PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId,
            @PathParam("networkInstanceId") String networkInstanceId, @Context ContainerRequestContext requestContext)
            throws Exception {
        InfraActiveRequests currentRequest = null;
        String requestId = networkRestHandler.getRequestId(requestContext);
        String requestorId = "Unknown";
        String source = MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME);
        String requestURI = requestContext.getUriInfo().getAbsolutePath().toString();
        currentRequest = networkRestHandler.createInfraActiveRequestForDelete(requestId, serviceInstanceId,
                networkInstanceId, requestorId, source, requestURI);
        ServiceInstancesRequest request = requestBuilder.buildNetworkDeleteRequest(networkInstanceId);
        networkRestHandler.saveInstanceName(request, currentRequest);
        networkRestHandler.checkDuplicateRequest(serviceInstanceId, networkInstanceId,
                request.getRequestDetails().getRequestInfo().getInstanceName(), currentRequest.getRequestId());
        Recipe recipe = networkRestHandler.findNetworkRecipe(Action.deleteInstance.toString());
        networkRestHandler.callWorkflowEngine(networkRestHandler.buildRequestParams(request,
                networkRestHandler.getRequestUri(requestContext), requestId, serviceInstanceId, networkInstanceId),
                recipe.getOrchestrationUri());
        ServiceInstancesResponse response =
                networkRestHandler.createResponse(networkInstanceId, requestId, requestContext);
        return Response.status(HttpStatus.ACCEPTED.value()).entity(response).build();
    }
}
