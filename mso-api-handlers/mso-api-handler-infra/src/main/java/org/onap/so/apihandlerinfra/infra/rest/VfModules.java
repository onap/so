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
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.infra.rest.handler.VFModuleRestHandler;
import org.onap.so.db.catalog.beans.Recipe;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.ModelType;
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
public class VfModules {

    private static Logger logger = LoggerFactory.getLogger(VfModules.class);

    @Autowired
    private VFModuleRestHandler restHandler;

    @Autowired
    private BpmnRequestBuilder requestBuilder;

    @DELETE
    @ResponseUpdater
    @Path("/{version:[vV][8]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete a VfModule instance", response = ServiceInstancesResponse.class)
    @Transactional
    public Response deleteVfModuleInstance(@PathParam("version") String version,
            @PathParam("serviceInstanceId") String serviceInstanceId, @PathParam("vnfInstanceId") String vnfInstanceId,
            @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId, @Context ContainerRequestContext requestContext)
            throws Exception {
        InfraActiveRequests currentRequest = null;

        String requestId = restHandler.getRequestId(requestContext);
        String requestorId = "Unknown";
        String source = MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME);
        String requestURL = requestContext.getUriInfo().getAbsolutePath().toString();
        currentRequest = restHandler.createInfraActiveRequestForDelete(requestId, vfmoduleInstanceId, serviceInstanceId,
                vnfInstanceId, requestorId, source, requestURL);
        ServiceInstancesRequest request =
                requestBuilder.buildVFModuleDeleteRequest(vnfInstanceId, vfmoduleInstanceId, ModelType.vfModule);
        restHandler.saveInstanceName(request, currentRequest);
        restHandler.checkDuplicateRequest(serviceInstanceId, vnfInstanceId, vfmoduleInstanceId,
                request.getRequestDetails().getRequestInfo().getInstanceName(), currentRequest.getRequestId());
        Recipe recipe =
                restHandler.findVfModuleRecipe(request.getRequestDetails().getModelInfo().getModelCustomizationId(),
                        ModelType.vfModule.toString(), Action.deleteInstance.toString());
        restHandler
                .callWorkflowEngine(restHandler.buildRequestParams(request, restHandler.getRequestUri(requestContext),
                        requestId, serviceInstanceId, vnfInstanceId, vfmoduleInstanceId), recipe.getOrchestrationUri());
        ServiceInstancesResponse response = restHandler.createResponse(vfmoduleInstanceId, requestId, requestContext);
        return Response.status(HttpStatus.ACCEPTED.value()).entity(response).build();

    }
}
