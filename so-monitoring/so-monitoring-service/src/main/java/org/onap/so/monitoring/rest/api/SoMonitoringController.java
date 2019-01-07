/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.monitoring.rest.api;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.onap.so.monitoring.db.service.DatabaseServiceProvider;
import org.onap.so.monitoring.exception.InvalidRestRequestException;
import org.onap.so.monitoring.exception.RestProcessingException;
import org.onap.so.monitoring.model.ActivityInstanceDetail;
import org.onap.so.monitoring.model.ProcessDefinitionDetail;
import org.onap.so.monitoring.model.ProcessInstanceDetail;
import org.onap.so.monitoring.model.ProcessInstanceIdDetail;
import org.onap.so.monitoring.model.ProcessInstanceVariableDetail;
import org.onap.so.monitoring.model.SoInfraRequest;
import org.onap.so.monitoring.rest.service.CamundaProcessDataServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author waqas.ikram@ericsson.com
 */
@Component
@Path("/")
public class SoMonitoringController {

    private static final String INVALID_PROCESS_INSTANCE_ERROR_MESSAGE = "Invalid process instance id: ";

    private static final Logger LOGGER = LoggerFactory.getLogger(SoMonitoringController.class);

    private final DatabaseServiceProvider databaseServiceProvider;

    private final CamundaProcessDataServiceProvider camundaProcessDataServiceProvider;

    @Autowired
    public SoMonitoringController(final DatabaseServiceProvider databaseServiceProvider,
            final CamundaProcessDataServiceProvider camundaProcessDataServiceProvider) {
        this.databaseServiceProvider = databaseServiceProvider;
        this.camundaProcessDataServiceProvider = camundaProcessDataServiceProvider;
    }

    @GET
    @Path("/process-instance-id/{requestId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessInstanceId(final @PathParam("requestId") String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("Invalid Request id: " + requestId).build();
        }
        try {
            final Optional<ProcessInstanceIdDetail> processInstanceId =
                    camundaProcessDataServiceProvider.getProcessInstanceIdDetail(requestId);
            if (processInstanceId.isPresent()) {
                return Response.status(Status.OK).entity(processInstanceId.get()).build();
            }

            LOGGER.error("Unable to find process instance id for : " + requestId);
            return Response.status(Status.NO_CONTENT).build();

        } catch (final InvalidRestRequestException extensions) {
            final String message = "Unable to find process instance id for : " + requestId;
            LOGGER.error(message);
            return Response.status(Status.BAD_REQUEST).entity(message).build();
        } catch (final RestProcessingException restProcessingException) {
            final String message = "Unable to process request for id: " + requestId;
            LOGGER.error(message);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build();
        }
    }

    @GET
    @Path("/process-instance/{processInstanceId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSingleProcessInstance(final @PathParam("processInstanceId") String processInstanceId) {
        if (processInstanceId == null || processInstanceId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(INVALID_PROCESS_INSTANCE_ERROR_MESSAGE + processInstanceId).build();
        }
        try {
            final Optional<ProcessInstanceDetail> processInstanceDetail =
                    camundaProcessDataServiceProvider.getSingleProcessInstanceDetail(processInstanceId);
            if (processInstanceDetail.isPresent()) {
                return Response.status(Status.OK).entity(processInstanceDetail.get()).build();
            }

            LOGGER.error("Unable to find process instance id for : " + processInstanceId);
            return Response.status(Status.NO_CONTENT).build();

        } catch (final InvalidRestRequestException extensions) {
            final String message = "Unable to find process instance id for : " + processInstanceId;
            LOGGER.error(message);
            return Response.status(Status.BAD_REQUEST).entity(message).build();
        } catch (final RestProcessingException restProcessingException) {
            final String message = "Unable to process request for id: " + processInstanceId;
            LOGGER.error(message);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build();
        }
    }

    @GET
    @Path("/process-definition/{processDefinitionId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinitionXml(final @PathParam("processDefinitionId") String processDefinitionId) {
        if (processDefinitionId == null || processDefinitionId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("Invalid process definition id: " + 
                                                              processDefinitionId)
                    .build();
        }
        try {
            final Optional<ProcessDefinitionDetail> response =
                    camundaProcessDataServiceProvider.getProcessDefinition(processDefinitionId);
            if (response.isPresent()) {
                final ProcessDefinitionDetail definitionDetail = response.get();
                return Response.status(Status.OK).entity(definitionDetail).build();
            }
            LOGGER.error("Unable to find process definition xml for processDefinitionId: " + 
                         processDefinitionId);
            return Response.status(Status.NO_CONTENT).build();

        } catch (final InvalidRestRequestException extensions) {
            final String message =
                    "Unable to find process definition xml for processDefinitionId: {}" + 
                processDefinitionId;
            return Response.status(Status.BAD_REQUEST).entity(message).build();
        } catch (final RestProcessingException restProcessingException) {
            final String message = "Unable to get process definition xml for id: " + 
                processDefinitionId;
            LOGGER.error(message);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build();
        }
    }

    @GET
    @Path("/activity-instance/{processInstanceId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getActivityInstanceDetail(final @PathParam("processInstanceId") String processInstanceId) {
        if (processInstanceId == null || processInstanceId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(INVALID_PROCESS_INSTANCE_ERROR_MESSAGE + processInstanceId).build();
        }
        try {
            final List<ActivityInstanceDetail> activityInstanceDetails =
                    camundaProcessDataServiceProvider.getActivityInstance(processInstanceId);
            return Response.status(Status.OK).entity(activityInstanceDetails).build();
        } catch (final InvalidRestRequestException extensions) {
            final String message = "Unable to find activity instance for processInstanceId: " + 
                processInstanceId;
            LOGGER.error(message);
            return Response.status(Status.BAD_REQUEST).entity(message).build();
        } catch (final RestProcessingException restProcessingException) {
            final String message = "Unable to get activity instance detail for id: " + 
                processInstanceId;
            LOGGER.error(message);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build();
        }
    }

    @GET
    @Path("/variable-instance/{processInstanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProcessInstanceVariables(final @PathParam("processInstanceId") String processInstanceId) {
        if (processInstanceId == null || processInstanceId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(INVALID_PROCESS_INSTANCE_ERROR_MESSAGE + processInstanceId).build();
        }
        try {
            final List<ProcessInstanceVariableDetail> processInstanceVariable =
                    camundaProcessDataServiceProvider.getProcessInstanceVariable(processInstanceId);
            return Response.status(Status.OK).entity(processInstanceVariable).build();
        } catch (final InvalidRestRequestException extensions) {
            final String message =
                    "Unable to find process instance variables for processInstanceId: " + 
                processInstanceId;
            LOGGER.error(message);
            return Response.status(Status.BAD_REQUEST).entity(message).build();
        } catch (final RestProcessingException restProcessingException) {
            final String message = "Unable to get process instance variables for id: " + 
                processInstanceId;
            LOGGER.error(message);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build();
        }
    }

    @POST
    @Path("/v1/search")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getInfraActiveRequests(final Map<String, String[]> filters,
            @QueryParam("from") final long startTime, @QueryParam("to") final long endTime,
            @QueryParam("maxResult") final Integer maxResult) {

        if (filters == null) {
            return Response.status(Status.BAD_REQUEST).entity("Invalid filters: " + filters).build();
        }
        try {
            final List<SoInfraRequest> requests =
                    databaseServiceProvider.getSoInfraRequest(filters, startTime, endTime, maxResult);
            LOGGER.info("result size: " + requests.size());
            return Response.status(Status.OK).entity(requests).build();

        } catch (final InvalidRestRequestException extensions) {
            final String message = "Unable to search request for filters: " + filters + ", from: " + 
                startTime + ", to: " + endTime + ", maxResult: " + maxResult;
            LOGGER.error(message);
            return Response.status(Status.BAD_REQUEST).entity(message).build();
        } catch (final RestProcessingException restProcessingException) {
            final String message = "Unable to search request for filters: " + filters + ", from: " + 
                startTime + ", to: " + endTime + ", maxResult: " + maxResult;
            LOGGER.error(message);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build();
        }
    }

}
