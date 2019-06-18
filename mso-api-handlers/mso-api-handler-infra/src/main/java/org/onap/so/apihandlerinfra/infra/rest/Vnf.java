/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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
import org.onap.so.apihandlerinfra.infra.rest.handler.VnfRestHandler;
import org.onap.so.db.catalog.beans.Recipe;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import io.swagger.annotations.ApiOperation;

@Component
@Path("/onap/so/infra/serviceInstantiation")
public class Vnf {

    private static Logger logger = LoggerFactory.getLogger(Vnf.class);

    @Autowired
    private BpmnRequestBuilder requestBuilder;

    @Autowired
    private VnfRestHandler vnfRestHandler;

    @DELETE
    @ResponseUpdater
    @Path("/{version:[vV][8]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete a Vnf instance", response = ServiceInstancesResponse.class)
    @Transactional
    public Response deleteVnfInstance(@PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @Context ContainerRequestContext requestContext) throws Exception {
        InfraActiveRequests currentRequest = null;
        String requestId = vnfRestHandler.getRequestId(requestContext);
        String requestorId = "Unknown";
        String source = MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME);
        String requestURL = requestContext.getUriInfo().getAbsolutePath().toString();
        currentRequest = vnfRestHandler.createInfraActiveRequestForDelete(requestId, serviceInstanceId, vnfInstanceId,
                requestorId, source, requestURL);
        ServiceInstancesRequest request = requestBuilder.buildVnfDeleteRequest(vnfInstanceId);
        vnfRestHandler.saveInstanceName(request, currentRequest);
        vnfRestHandler.checkDuplicateRequest(serviceInstanceId, vnfInstanceId,
                request.getRequestDetails().getRequestInfo().getInstanceName(), currentRequest.getRequestId());
        Recipe recipe = vnfRestHandler.findVnfModuleRecipe(
                request.getRequestDetails().getModelInfo().getModelCustomizationId(), "vnf", "deleteInstance");
        vnfRestHandler.callWorkflowEngine(vnfRestHandler.buildRequestParams(request,
                vnfRestHandler.getRequestUri(requestContext), requestId, serviceInstanceId, vnfInstanceId),
                recipe.getOrchestrationUri());
        ServiceInstancesResponse response = vnfRestHandler.createResponse(vnfInstanceId, requestId, requestContext);
        return Response.status(HttpStatus.ACCEPTED.value()).entity(response).build();
    }
}
