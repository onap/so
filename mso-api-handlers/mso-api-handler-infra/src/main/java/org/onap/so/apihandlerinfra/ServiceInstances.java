/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
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
import org.onap.so.apihandlerinfra.exceptions.ContactCamundaException;
import org.onap.so.apihandlerinfra.exceptions.DuplicateRequestException;
import org.onap.so.apihandlerinfra.exceptions.RecipeNotFoundException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.exceptions.VfModuleNotFoundException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
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
import org.onap.so.logger.LogConstants;
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
import org.onap.so.utils.CryptoUtils;
import org.onap.so.utils.UUIDChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;

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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Path("/onap/so/infra/serviceInstantiation")
@Api(value="/onap/so/infra/serviceInstantiation",description="Infrastructure API Requests for Service Instances")
public class ServiceInstances {

	private static Logger logger = LoggerFactory.getLogger(MsoRequest.class);
	private static String NAME = "name";
	private static String VALUE = "value";
	private static final String SAVE_TO_DB = "save instance to db";

	@Autowired
	private Environment env;
	
	@Autowired
	private RequestClientFactory reqClientFactory;
	
	@Autowired
	private CatalogDbClient catalogDbClient;

	@Autowired
	private RequestsDbClient infraActiveRequestsClient;
	
	@Autowired
	private ResponseBuilder builder;
	
	@Autowired
	private MsoRequest msoRequest;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@POST
    @Path("/{version:[vV][5-7]}/serviceInstances")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create a Service Instance on a version provided",response=Response.class)
	@Transactional
    public Response createServiceInstance(String request, @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException {
		String requestId = getRequestId(requestContext);
		return serviceInstances(request, Action.createInstance, null, version, requestId, getRequestUri(requestContext));
	}
	
	@POST
	@Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/activate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Activate provided Service Instance",response=Response.class)
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
	@ApiOperation(value="Deactivate provided Service Instance",response=Response.class)
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
	@ApiOperation(value="Delete provided Service Instance",response=Response.class)
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
	@ApiOperation(value="Assign Service Instance", response=Response.class)
	@Transactional
	public Response assignServiceInstance(String request, @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException {
		String requestId = getRequestId(requestContext);
		return serviceInstances(request, Action.assignInstance, null, version, requestId, getRequestUri(requestContext));
	}

	@POST
	@Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/unassign")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Unassign Service Instance", response=Response.class)
	@Transactional
	public Response unassignServiceInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
		String requestId = getRequestId(requestContext);
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		return serviceInstances(request, Action.unassignInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
	}
	
	@POST
	@Path("/{version:[vV][5-7]}/serviceInstances/{serviceInstanceId}/configurations")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create Port Mirroring Configuration",response=Response.class)
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
	@ApiOperation(value="Delete provided Port",response=Response.class)
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
	@ApiOperation(value="Enable Port Mirroring",response=Response.class)
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
	@ApiOperation(value="Disable Port Mirroring",response=Response.class)
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
	@ApiOperation(value="Activate Port Mirroring",response=Response.class)
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
	@ApiOperation(value="Deactivate Port Mirroring",response=Response.class)
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
	@ApiOperation(value="Add Relationships to a Service Instance",response=Response.class)
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
	@ApiOperation(value="Remove Relationships from Service Instance",response=Response.class)
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
	@ApiOperation(value="Create VNF on a specified version and serviceInstance",response=Response.class)
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
	@ApiOperation(value="Replace provided VNF instance",response=Response.class)
	@Transactional
	public Response replaceVnfInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
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
	@ApiOperation(value="Update VNF on a specified version, serviceInstance and vnfInstance",response=Response.class)
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
	@ApiOperation(value="Apply updated configuration",response=Response.class)
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
	@ApiOperation(value="Recreate VNF Instance",response=Response.class)
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
	@ApiOperation(value="Delete provided VNF instance",response=Response.class)
	@Transactional
	public Response deleteVnfInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
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
	@ApiOperation(value="Create VfModule on a specified version, serviceInstance and vnfInstance",response=Response.class)
	@Transactional
	public Response createVfModuleInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
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
	@ApiOperation(value="Create VfModule on a specified version, serviceInstance and vnfInstance",response=Response.class)
	@Transactional
	public Response replaceVfModuleInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
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
	@ApiOperation(value="Update VfModule on a specified version, serviceInstance, vnfInstance and vfModule",response=Response.class)
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
	@ApiOperation(value="Perform VNF software update",response=Response.class)
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
	@ApiOperation(value="Delete provided VfModule instance",response=Response.class)
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
	@ApiOperation(value="Deactivate and Cloud Delete VfModule instance",response=Response.class)
	@Transactional
	public Response deactivateAndCloudDeleteVfModuleInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId, @PathParam("vfmoduleInstanceId") String vfmoduleInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
		String requestId = getRequestId(requestContext);
		HashMap<String, String> instanceIdMap = new HashMap<>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
		Response response = serviceInstances(request, Action.deactivateAndCloudDelete, instanceIdMap, version, requestId, getRequestUri(requestContext));
		return response;
	}
	
	@POST
	@Path("/{version:[vV][7]}/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/scaleOut")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="VF Auto Scale Out",response=Response.class)
	@Transactional
	public Response scaleOutVfModule(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
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
	@ApiOperation(value="Create VolumeGroup on a specified version, serviceInstance, vnfInstance",response=Response.class)
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
	@ApiOperation(value="Update VolumeGroup on a specified version, serviceInstance, vnfInstance and volumeGroup",response=Response.class)
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
	@ApiOperation(value="Delete provided VolumeGroup instance",response=Response.class)
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
	@ApiOperation(value="Create NetworkInstance on a specified version and serviceInstance ",response=Response.class)
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
	@ApiOperation(value="Update VolumeGroup on a specified version, serviceInstance, networkInstance",response=Response.class)
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
	@ApiOperation(value="Delete provided Network instance",response=Response.class)
	@Transactional
	public Response deleteNetworkInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
                                          @PathParam("networkInstanceId") String networkInstanceId, @Context ContainerRequestContext requestContext) throws ApiException {
		String requestId = getRequestId(requestContext);
		HashMap<String, String> instanceIdMap = new HashMap<>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("networkInstanceId", networkInstanceId);
		return serviceInstances(request, Action.deleteInstance, instanceIdMap, version, requestId, getRequestUri(requestContext));
	}
	
	@POST
    @Path("/{version:[vV][7]}/instanceGroups")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create instanceGroups",response=Response.class)
	@Transactional
    public Response createInstanceGroups(String request, @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException {
		String requestId = getRequestId(requestContext);
		return serviceInstances(request, Action.createInstance, null, version, requestId, getRequestUri(requestContext));
	}
	
	@DELETE
	@Path("/{version:[vV][7]}/instanceGroups/{instanceGroupId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Delete instanceGroup",response=Response.class)
	@Transactional
	public Response deleteInstanceGroups(@PathParam("version") String version, @PathParam("instanceGroupId") String instanceGroupId, @Context ContainerRequestContext requestContext) throws ApiException {
		String requestId = getRequestId(requestContext);
		HashMap<String, String> instanceIdMap = new HashMap<>();
		instanceIdMap.put(CommonConstants.INSTANCE_GROUP_ID, instanceGroupId);
		return deleteInstanceGroups(Action.deleteInstance, instanceIdMap, version, requestId, getRequestUri(requestContext), requestContext);
	}
	
	@POST
    @Path("/{version:[vV][7]}/instanceGroups/{instanceGroupId}/addMembers")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Add instanceGroup members",response=Response.class)
	@Transactional
    public Response addInstanceGroupMembers(String request, @PathParam("version") String version, @PathParam("instanceGroupId") String instanceGroupId, @Context ContainerRequestContext requestContext) throws ApiException {
		String requestId = getRequestId(requestContext);
		HashMap<String, String> instanceIdMap = new HashMap<>();
		instanceIdMap.put(CommonConstants.INSTANCE_GROUP_ID, instanceGroupId);
		return serviceInstances(request, Action.addMembers, instanceIdMap, version, requestId, getRequestUri(requestContext));
	}
	
	@POST
    @Path("/{version:[vV][7]}/instanceGroups/{instanceGroupId}/removeMembers")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Remove instanceGroup members",response=Response.class)
	@Transactional
    public Response removeInstanceGroupMembers(String request, @PathParam("version") String version, @PathParam("instanceGroupId") String instanceGroupId, @Context ContainerRequestContext requestContext) throws ApiException {
		String requestId = getRequestId(requestContext);
		HashMap<String, String> instanceIdMap = new HashMap<>();
		instanceIdMap.put(CommonConstants.INSTANCE_GROUP_ID, instanceGroupId);
		return serviceInstances(request, Action.removeMembers, instanceIdMap, version, requestId, getRequestUri(requestContext));
	}

	public String getRequestUri(ContainerRequestContext context){
		String requestUri = context.getUriInfo().getPath();
		String httpUrl = MDC.get(LogConstants.URI_BASE).concat(requestUri);
		MDC.put(LogConstants.HTTP_URL, httpUrl);
		requestUri = requestUri.substring(requestUri.indexOf("/serviceInstantiation/") + 22);
		return requestUri;
	}
	
	public void validateHeaders(ContainerRequestContext context) throws ValidationException{
		MultivaluedMap<String, String> headers = context.getHeaders();
		if(!headers.containsKey(ONAPLogConstants.Headers.REQUEST_ID)){
			 throw new ValidationException(ONAPLogConstants.Headers.REQUEST_ID + " header", true);
		}
		if(!headers.containsKey(ONAPLogConstants.Headers.PARTNER_NAME)){
			throw new ValidationException(ONAPLogConstants.Headers.PARTNER_NAME + " header", true);
		}
		if(!headers.containsKey(MsoLogger.REQUESTOR_ID)){
			throw new ValidationException(MsoLogger.REQUESTOR_ID + " header", true);
		}
	}
    
	public Response serviceInstances(String requestJSON, Actions action, HashMap<String, String> instanceIdMap, String version, String requestId, String requestUri) throws ApiException {
		String serviceInstanceId = (instanceIdMap ==null)? null:instanceIdMap.get("serviceInstanceId");
		Boolean aLaCarte = null;
		long startTime = System.currentTimeMillis ();
		ServiceInstancesRequest sir = null;
		String apiVersion = version.substring(1);
		
		sir = convertJsonToServiceInstanceRequest(requestJSON, action, startTime, sir, msoRequest, requestId, requestUri);
		String requestScope = deriveRequestScope(action, sir, requestUri);
		InfraActiveRequests currentActiveReq =  msoRequest.createRequestObject (sir,  action, requestId, Status.IN_PROGRESS, requestJSON, requestScope);
		if(sir.getRequestDetails().getRequestParameters() != null){
			aLaCarte = sir.getRequestDetails().getRequestParameters().getALaCarte();
		}
		parseRequest(sir, instanceIdMap, action, version, requestJSON, aLaCarte, requestId, currentActiveReq);
		setInstanceId(currentActiveReq, requestScope, null, instanceIdMap);
		 
		int requestVersion = Integer.parseInt(version.substring(1));
		String instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();
		boolean alaCarteFlag = msoRequest.getAlacarteFlag(sir);
		String vnfType = msoRequest.getVnfType(sir,requestScope,action,requestVersion);
		String networkType = msoRequest.getNetworkType(sir,requestScope);
		String sdcServiceModelVersion = msoRequest.getSDCServiceModelVersion(sir);
		String vfModuleType = msoRequest.getVfModuleType(sir,requestScope,action,requestVersion);
		
		if(requestScope.equalsIgnoreCase(ModelType.vnf.name()) && vnfType != null){
			currentActiveReq.setVnfType(vnfType);
		}else if(requestScope.equalsIgnoreCase(ModelType.network.name()) && networkType != null){
			currentActiveReq.setNetworkType(networkType);
		}
		
		InfraActiveRequests dup = null;
		boolean inProgress = false;		

		dup = duplicateCheck(action, instanceIdMap, startTime, msoRequest, instanceName,requestScope, currentActiveReq);

		if(dup != null){
			inProgress = camundaHistoryCheck(dup, currentActiveReq);
		}
		
		if (dup != null && inProgress) {
            buildErrorOnDuplicateRecord(currentActiveReq, action, instanceIdMap, startTime, msoRequest, instanceName, requestScope, dup);
		}
		ServiceInstancesResponse serviceResponse = new ServiceInstancesResponse();

		RequestReferences referencesResponse = new RequestReferences();

		referencesResponse.setRequestId(requestId);

		serviceResponse.setRequestReferences(referencesResponse);
		Boolean isBaseVfModule = false;

        RecipeLookupResult recipeLookupResult = getServiceInstanceOrchestrationURI(sir, action, alaCarteFlag, currentActiveReq);
        String serviceInstanceType = getServiceType(requestScope, sir, alaCarteFlag);						
			ModelType modelType;
			ModelInfo modelInfo =  sir.getRequestDetails().getModelInfo();
			if (action == Action.applyUpdatedConfig || action == Action.inPlaceSoftwareUpdate) {
				modelType = ModelType.vnf;
			}else if(action == Action.addMembers || action == Action.removeMembers){
				modelType = ModelType.instanceGroup;
			}else {
				modelType =modelInfo.getModelType();
			}

			if (modelType.equals(ModelType.vfModule)) {
				

				// Get VF Module-specific base module indicator
				VfModule vfm = null;

				String modelVersionId = modelInfo.getModelVersionId();

				if(modelVersionId != null) {
					vfm = catalogDbClient.getVfModuleByModelUUID(modelVersionId);
				} else if(modelInfo.getModelInvariantId() != null && modelInfo.getModelVersion() != null){
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
					if (sdcServiceModelVersion != null && !sdcServiceModelVersion.isEmpty ()) {
						serviceVersionText = " with version " + sdcServiceModelVersion;
					}

                String errorMessage = "VnfType " + vnfType + " and VF Module Model Name " + modelInfo.getModelName() + serviceVersionText + " not found in MSO Catalog DB";
                ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
                VfModuleNotFoundException vfModuleException = new VfModuleNotFoundException.Builder(errorMessage, HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo).build();
                updateStatus(currentActiveReq, Status.FAILED, vfModuleException.getMessage());

                throw vfModuleException;
		}
		}
		
		
		serviceInstanceId = "";
		String vnfId = "";
		String vfModuleId = "";
		String volumeGroupId = "";
		String networkId = "";
		String pnfCorrelationId = "";
		String instanceGroupId = null;
		if(sir.getServiceInstanceId () != null){
			serviceInstanceId = sir.getServiceInstanceId ();
		}

		if(sir.getVnfInstanceId () != null){
			vnfId = sir.getVnfInstanceId ();
		}

		if(sir.getVfModuleInstanceId () != null){
			vfModuleId = sir.getVfModuleInstanceId ();
		}

		if(sir.getVolumeGroupInstanceId () != null){
			volumeGroupId = sir.getVolumeGroupInstanceId ();
		}

		if(sir.getNetworkInstanceId () != null){
			networkId = sir.getNetworkInstanceId ();
		}
		if(sir.getInstanceGroupId() != null){
			instanceGroupId = sir.getInstanceGroupId();
		}

        pnfCorrelationId = getPnfCorrelationId(sir);

        try{
            infraActiveRequestsClient.save(currentActiveReq);
        }catch(Exception e){
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
                    .errorInfo(errorLoggerInfo).build();
        }
		
		if(!requestScope.equalsIgnoreCase(ModelType.service.name()) && action != Action.recreateInstance){
			aLaCarte = true;
		}else if(aLaCarte == null){
			aLaCarte = false;
		}
		
		RequestClientParameter requestClientParameter = null;
		try {
			requestClientParameter = new RequestClientParameter.Builder()
						.setRequestId(requestId)
						.setBaseVfModule(isBaseVfModule)
						.setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
						.setRequestAction(action.toString())
						.setServiceInstanceId(serviceInstanceId)
						.setPnfCorrelationId(pnfCorrelationId)
						.setVnfId(vnfId)
						.setVfModuleId(vfModuleId)
						.setVolumeGroupId(volumeGroupId)
						.setNetworkId(networkId)
						.setServiceType(serviceInstanceType)
						.setVnfType(vnfType)
						.setVfModuleType(vfModuleType)
						.setNetworkType(networkType)
						.setRequestDetails(mapJSONtoMSOStyle(requestJSON, sir, aLaCarte, action))
						.setApiVersion(apiVersion)
						.setALaCarte(aLaCarte)
						.setRequestUri(requestUri)
						.setInstanceGroupId(instanceGroupId).build();
		} catch (IOException e) {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
			throw new ValidateException.Builder("Unable to generate RequestClientParamter object" + e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER)
	                  .errorInfo(errorLoggerInfo).build();
		}
		return postBPELRequest(currentActiveReq, requestClientParameter, recipeLookupResult.getOrchestrationURI(), requestScope);
	}
	public Response deleteInstanceGroups(Actions action, HashMap<String, String> instanceIdMap, String version, String requestId, String requestUri, ContainerRequestContext requestContext) throws ApiException {
		String instanceGroupId = instanceIdMap.get(CommonConstants.INSTANCE_GROUP_ID);
		Boolean aLaCarte = true;
		long startTime = System.currentTimeMillis ();
		String apiVersion = version.substring(1);
		ServiceInstancesRequest sir = new ServiceInstancesRequest();
		sir.setInstanceGroupId(instanceGroupId);
	
		String requestScope = ModelType.instanceGroup.toString();
		InfraActiveRequests currentActiveReq =  msoRequest.createRequestObject (sir,  action, requestId, Status.IN_PROGRESS, null, requestScope);
		setInstanceId(currentActiveReq, requestScope, null, instanceIdMap);
		try {
			validateHeaders(requestContext);
		} catch (ValidationException e) {
			logger.error("Exception occurred", e);
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException = new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                        .errorInfo(errorLoggerInfo).build();
            updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());
            throw validateException;
		}
		
		InfraActiveRequests dup = duplicateCheck(action, instanceIdMap, startTime, msoRequest, null, requestScope, currentActiveReq);
		boolean inProgress = false;
		
		if(dup != null){
			inProgress = camundaHistoryCheck(dup, currentActiveReq);
		}
		
		if (dup != null && inProgress) {
            buildErrorOnDuplicateRecord(currentActiveReq, action, instanceIdMap, startTime, msoRequest, null, requestScope, dup);
		}
		
		ServiceInstancesResponse serviceResponse = new ServiceInstancesResponse();

		RequestReferences referencesResponse = new RequestReferences();

		referencesResponse.setRequestId(requestId);

		serviceResponse.setRequestReferences(referencesResponse);
		Boolean isBaseVfModule = false;

        RecipeLookupResult recipeLookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 180);
								
        try{
            infraActiveRequestsClient.save(currentActiveReq);
        }catch(Exception e){
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
                    .errorInfo(errorLoggerInfo).build();
        }
        
		RequestClientParameter requestClientParameter = new RequestClientParameter.Builder()
					.setRequestId(requestId)
					.setBaseVfModule(isBaseVfModule)
					.setRecipeTimeout(recipeLookupResult.getRecipeTimeout())
					.setRequestAction(action.toString())
					.setApiVersion(apiVersion)
					.setALaCarte(aLaCarte)
					.setRequestUri(requestUri)
					.setInstanceGroupId(instanceGroupId).build();
		
		return postBPELRequest(currentActiveReq, requestClientParameter, recipeLookupResult.getOrchestrationURI(), requestScope);
	}

	private String getPnfCorrelationId(ServiceInstancesRequest sir) {
		return Optional.of(sir)
				.map(ServiceInstancesRequest::getRequestDetails)
				.map(RequestDetails::getRequestParameters)
				.map(parameters -> parameters.getUserParamValue("pnfId"))
				.orElse("");
	}

	private String deriveRequestScope(Actions action, ServiceInstancesRequest sir, String requestUri) {
		if(action == Action.inPlaceSoftwareUpdate || action == Action.applyUpdatedConfig){
			return (ModelType.vnf.name());
		}else if(action == Action.addMembers || action == Action.removeMembers){
			return(ModelType.instanceGroup.toString());
		}else{
			String requestScope;
			if(sir.getRequestDetails().getModelInfo().getModelType() == null){
				requestScope = requestScopeFromUri(requestUri);
			}else{
				requestScope = sir.getRequestDetails().getModelInfo().getModelType().name(); 
			}
			return requestScope; 
		}
	}
	private String requestScopeFromUri(String requestUri){
		String requestScope;
		if(requestUri.contains(ModelType.network.name())){
			requestScope = ModelType.network.name();
		}else if(requestUri.contains(ModelType.vfModule.name())){
			requestScope = ModelType.vfModule.name();
		}else if(requestUri.contains(ModelType.volumeGroup.name())){
			requestScope = ModelType.volumeGroup.name();
		}else if(requestUri.contains(ModelType.configuration.name())){
			requestScope = ModelType.configuration.name();
		}else if(requestUri.contains(ModelType.vnf.name())){
			requestScope = ModelType.vnf.name();
		}else{
			requestScope = ModelType.service.name();
		}
		return requestScope;
	}
	private Response postBPELRequest(InfraActiveRequests currentActiveReq, RequestClientParameter requestClientParameter, String orchestrationUri, String requestScope)throws ApiException {
		RequestClient requestClient = null;
		HttpResponse response = null;
		try {
			requestClient = reqClientFactory.getRequestClient (orchestrationUri);
			response = requestClient.post(requestClientParameter);
		} catch (Exception e) {
			
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MsoLogger.ErrorCode.AvailabilityError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            String url = requestClient != null ? requestClient.getUrl() : "";
            ClientConnectionException clientException = new ClientConnectionException.Builder(url, HttpStatus.SC_BAD_GATEWAY, ErrorNumbers.SVC_NO_SERVER_RESOURCES).cause(e).errorInfo(errorLoggerInfo).build();
            updateStatus(currentActiveReq, Status.FAILED, clientException.getMessage());

            throw clientException;
		}

		if (response == null) {
			
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MsoLogger.ErrorCode.BusinessProcesssError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ClientConnectionException clientException = new ClientConnectionException.Builder(requestClient.getUrl(), HttpStatus.SC_BAD_GATEWAY, ErrorNumbers.SVC_NO_SERVER_RESOURCES).errorInfo(errorLoggerInfo).build();

            updateStatus(currentActiveReq, Status.FAILED, clientException.getMessage());

            throw clientException;
		}

		ResponseHandler respHandler = null;
        int bpelStatus = 500;
        try {
            respHandler = new ResponseHandler (response, requestClient.getType ());
            bpelStatus = respHandler.getStatus ();
        } catch (ApiException e) {
            logger.error("Exception occurred", e);
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException = new ValidateException.Builder("Exception caught mapping Camunda JSON response to object", HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                        .errorInfo(errorLoggerInfo).build();
            updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());
            throw validateException;
        }

		// BPEL accepted the request, the request is in progress
		if (bpelStatus == HttpStatus.SC_ACCEPTED) {
			ServiceInstancesResponse jsonResponse;
			CamundaResponse camundaResp = respHandler.getResponse();
			
			if("Success".equalsIgnoreCase(camundaResp.getMessage())) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					jsonResponse = mapper.readValue(camundaResp.getResponse(), ServiceInstancesResponse.class);
					jsonResponse.getRequestReferences().setRequestId(requestClientParameter.getRequestId());
					Optional<URL> selfLinkUrl = msoRequest.buildSelfLinkUrl(currentActiveReq.getRequestUrl(), requestClientParameter.getRequestId());
					if(selfLinkUrl.isPresent()){
						jsonResponse.getRequestReferences().setRequestSelfLink(selfLinkUrl.get());
					} else {
					    jsonResponse.getRequestReferences().setRequestSelfLink(null);
					}    
				} catch (IOException e) {
					logger.error("Exception occurred", e);
					ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
					ValidateException validateException = new ValidateException.Builder("Exception caught mapping Camunda JSON response to object", HttpStatus.SC_NOT_ACCEPTABLE, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
			                    .errorInfo(errorLoggerInfo).build();
					updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());
					throw validateException;
				}	
				return builder.buildResponse(HttpStatus.SC_ACCEPTED, requestClientParameter.getRequestId(), jsonResponse, requestClientParameter.getApiVersion());
			} 
		}
			
		List<String> variables = new ArrayList<>();
		variables.add(bpelStatus + "");
		String camundaJSONResponseBody = respHandler.getResponseBody ();
		if (camundaJSONResponseBody != null && !camundaJSONResponseBody.isEmpty ()) {
			
		    ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.BusinessProcesssError).errorSource(requestClient.getUrl()).build();
		    BPMNFailureException bpmnException = new BPMNFailureException.Builder(String.valueOf(bpelStatus) + camundaJSONResponseBody, bpelStatus, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
		            .errorInfo(errorLoggerInfo).build();

		    updateStatus(currentActiveReq, Status.FAILED, bpmnException.getMessage());

		    throw bpmnException;
		} else {
		
		    ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.BusinessProcesssError).errorSource(requestClient.getUrl()).build();


		    BPMNFailureException servException = new BPMNFailureException.Builder(String.valueOf(bpelStatus), bpelStatus, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
		            .errorInfo(errorLoggerInfo).build();
		    updateStatus(currentActiveReq, Status.FAILED, servException.getMessage());

		    throw servException;
		}
	}

	private void setInstanceId(InfraActiveRequests currentActiveReq, String requestScope, String instanceId, Map<String, String> instanceIdMap) {
		if(StringUtils.isNotBlank(instanceId)) {
			if(ModelType.service.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setServiceInstanceId(instanceId);
			} else if(ModelType.vnf.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setVnfId(instanceId);
			} else if(ModelType.vfModule.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setVfModuleId(instanceId);
			} else if(ModelType.volumeGroup.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setVolumeGroupId(instanceId);
			} else if(ModelType.network.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setNetworkId(instanceId);
			} else if(ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setConfigurationId(instanceId);
			}else if(ModelType.instanceGroup.toString().equalsIgnoreCase(requestScope)){
				currentActiveReq.setInstanceGroupId(instanceId);
			}
		} else if(instanceIdMap != null && !instanceIdMap.isEmpty()) {
			if(instanceIdMap.get("serviceInstanceId") != null){
				currentActiveReq.setServiceInstanceId(instanceIdMap.get("serviceInstanceId"));
        	}
        	if(instanceIdMap.get("vnfInstanceId") != null){
        		currentActiveReq.setVnfId(instanceIdMap.get("vnfInstanceId"));
        	}
        	if(instanceIdMap.get("vfModuleInstanceId") != null){
        		currentActiveReq.setVfModuleId(instanceIdMap.get("vfModuleInstanceId"));
        	}
        	if(instanceIdMap.get("volumeGroupInstanceId") != null){
        		currentActiveReq.setVolumeGroupId(instanceIdMap.get("volumeGroupInstanceId"));
        	}
        	if(instanceIdMap.get("networkInstanceId") != null){
        		currentActiveReq.setNetworkId(instanceIdMap.get("networkInstanceId"));
        	}
        	if(instanceIdMap.get("configurationInstanceId") != null){
        		currentActiveReq.setConfigurationId(instanceIdMap.get("configurationInstanceId"));
        	}
        	if(instanceIdMap.get("InstanceGroupInstanceId") != null){
        		currentActiveReq.setInstanceGroupId(instanceIdMap.get("InstanceGroupInstanceId"));
        	}
		}
	}

    protected String mapJSONtoMSOStyle(String msoRawRequest, ServiceInstancesRequest serviceInstRequest, boolean isAlaCarte, Actions action) throws IOException {
    	ObjectMapper mapper = new ObjectMapper();    	
    	mapper.setSerializationInclusion(Include.NON_NULL);    	
    	if(msoRawRequest != null){
	    	ServiceInstancesRequest sir = mapper.readValue(msoRawRequest, ServiceInstancesRequest.class);    	
	    	if(	!isAlaCarte && Action.createInstance.equals(action) && serviceInstRequest != null && 
	    		serviceInstRequest.getRequestDetails() != null && 
	    		serviceInstRequest.getRequestDetails().getRequestParameters() != null) {
		    	sir.getRequestDetails().setCloudConfiguration(serviceInstRequest.getRequestDetails().getCloudConfiguration());
		    	sir.getRequestDetails().getRequestParameters().setUserParams(serviceInstRequest.getRequestDetails().getRequestParameters().getUserParams());
	    	}
	    	logger.debug("Value as string: {}", mapper.writeValueAsString(sir));
	    	return mapper.writeValueAsString(sir);
    	}
    	return null;
	}

    private void buildErrorOnDuplicateRecord(InfraActiveRequests currentActiveReq, Actions action, HashMap<String, String> instanceIdMap, long startTime, MsoRequest msoRequest,
                                             String instanceName, String requestScope, InfraActiveRequests dup) throws ApiException {

		// Found the duplicate record. Return the appropriate error.
		String instance = null;
		if(instanceName != null){
			instance = instanceName;
		}else{
			instance = instanceIdMap.get(requestScope + "InstanceId");
		}
		//List<String> variables = new ArrayList<String>();
		//variables.add(dup.getRequestStatus());
        ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_FOUND, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


        DuplicateRequestException dupException = new DuplicateRequestException.Builder(requestScope,instance,dup.getRequestStatus(),dup.getRequestId(), HttpStatus.SC_CONFLICT, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
            .errorInfo(errorLoggerInfo).build();

        updateStatus(currentActiveReq, Status.FAILED, dupException.getMessage());

        throw dupException;
	}

	private InfraActiveRequests duplicateCheck(Actions action, HashMap<String, String> instanceIdMap, long startTime,
                                               MsoRequest msoRequest, String instanceName, String requestScope, InfraActiveRequests currentActiveReq) throws ApiException {
		InfraActiveRequests dup = null;
		try {
			if(!(instanceName==null && requestScope.equals("service") && (action == Action.createInstance || action == Action.activateInstance || action == Action.assignInstance))){
				dup = infraActiveRequestsClient.checkInstanceNameDuplicate (instanceIdMap, instanceName, requestScope);
			}
		} catch (Exception e) {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_CHECK_EXC, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            RequestDbFailureException requestDbFailureException = new RequestDbFailureException.Builder("check for duplicate instance", e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
                    .errorInfo(errorLoggerInfo).build();
            updateStatus(currentActiveReq, Status.FAILED, requestDbFailureException.getMessage());
            throw requestDbFailureException;
		}
		return dup;
	}
    protected boolean camundaHistoryCheck(InfraActiveRequests duplicateRecord, InfraActiveRequests currentActiveReq) throws RequestDbFailureException, ContactCamundaException{
    	String requestId = duplicateRecord.getRequestId();
    	String path = env.getProperty("mso.camunda.rest.history.uri") + requestId;
    	String targetUrl = env.getProperty("mso.camundaURL") + path;
    	HttpHeaders headers = setCamundaHeaders(env.getRequiredProperty("mso.camundaAuth"), env.getRequiredProperty("mso.msoKey")); 
    	HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    	ResponseEntity<List<HistoricProcessInstanceEntity>> response = null;
    	try{
    		response = restTemplate.exchange(targetUrl, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>(){});
    	}catch(HttpStatusCodeException e){
    		ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_CHECK_EXC, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ContactCamundaException contactCamundaException= new ContactCamundaException.Builder(requestId, e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
                    .errorInfo(errorLoggerInfo).build();
            updateStatus(currentActiveReq, Status.FAILED, contactCamundaException.getMessage());
            throw contactCamundaException;
		}
    	if(response.getBody().isEmpty()){
    		updateStatus(duplicateRecord, Status.COMPLETE, "Request Completed");
    	}
		for(HistoricProcessInstance instance : response.getBody()){
			if(instance.getState().equals("ACTIVE")){
				return true;
			}else{
				updateStatus(duplicateRecord, Status.COMPLETE, "Request Completed");
			}
    	}	
		return false;
	}
    protected HttpHeaders setCamundaHeaders(String auth, String msoKey) {
		HttpHeaders headers = new HttpHeaders();
		List<org.springframework.http.MediaType> acceptableMediaTypes = new ArrayList<>();
		acceptableMediaTypes.add(org.springframework.http.MediaType.APPLICATION_JSON);
		headers.setAccept(acceptableMediaTypes);
       	try {
       		String userCredentials = CryptoUtils.decrypt(auth, msoKey);
       		if(userCredentials != null) {
       			headers.add(HttpHeaders.AUTHORIZATION, "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes()));
       		}
        } catch(GeneralSecurityException e) {
                logger.error("Security exception", e);
        }
		return headers;
	}

	private ServiceInstancesRequest convertJsonToServiceInstanceRequest(String requestJSON, Actions action, long startTime,
                                                                        ServiceInstancesRequest sir, MsoRequest msoRequest, String requestId, String requestUri) throws ApiException {
		try{
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(requestJSON, ServiceInstancesRequest.class);

        } catch (IOException e) {

            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();

            ValidateException validateException = new ValidateException.Builder("Error mapping request: " + e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                    .errorInfo(errorLoggerInfo).build();
            String requestScope = requestScopeFromUri(requestUri);

            msoRequest.createErrorRequestRecord(Status.FAILED, requestId, validateException.getMessage(), action, requestScope, requestJSON);

            throw validateException;
		}
	}
	
	private void parseRequest(ServiceInstancesRequest sir, HashMap<String, String> instanceIdMap, Actions action, String version, 
								String requestJSON, Boolean aLaCarte, String requestId, InfraActiveRequests currentActiveReq) throws ValidateException, RequestDbFailureException {
		int reqVersion = Integer.parseInt(version.substring(1));
		try {
			msoRequest.parse(sir, instanceIdMap, action, version, requestJSON, reqVersion, aLaCarte);
		} catch (Exception e) {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
	        ValidateException validateException = new ValidateException.Builder("Error parsing request: " + e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                 .errorInfo(errorLoggerInfo).build();

	        updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());

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
        }else if(action == Action.addMembers || action == Action.removeMembers){
        	recipeLookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 180);
        }else if (modelInfo.getModelType().equals(ModelType.service)) {
			try {
			recipeLookupResult = getServiceURI(sir, action,alaCarteFlag);
			} catch (IOException e) {
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


                ValidateException validateException = new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                        .errorInfo(errorLoggerInfo).build();

                updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());

                throw validateException;
			}
        } else if (modelInfo.getModelType().equals(ModelType.vfModule) ||
				modelInfo.getModelType().equals(ModelType.volumeGroup) || modelInfo.getModelType().equals(ModelType.vnf)) {
            try {
			recipeLookupResult = getVnfOrVfModuleUri( sir, action);
            } catch (ValidationException e) {
                ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


                ValidateException validateException = new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                        .errorInfo(errorLoggerInfo).build();

                updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());

                throw validateException;
            }
		}else if (modelInfo.getModelType().equals(ModelType.network)) {
            try {
			recipeLookupResult = getNetworkUri( sir, action);
            } catch (ValidationException e) {

                ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


                ValidateException validateException = new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                        .errorInfo(errorLoggerInfo).build();
                updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());

                throw validateException;
            }
        }else if(modelInfo.getModelType().equals(ModelType.instanceGroup)){
        	recipeLookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 180);
        }

        if (recipeLookupResult == null) {
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


            RecipeNotFoundException recipeNotFoundExceptionException = new RecipeNotFoundException.Builder("Recipe could not be retrieved from catalog DB.", HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_GENERAL_SERVICE_ERROR)
                    .errorInfo(errorLoggerInfo).build();

            updateStatus(currentActiveReq, Status.FAILED, recipeNotFoundExceptionException.getMessage());
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
		
		if(alaCarteFlag){
			serviceRecord = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
			if(serviceRecord !=null){					
				recipe =catalogDbClient.getFirstByServiceModelUUIDAndAction(serviceRecord.getModelUUID(),action.toString());
			}
		}else{
			serviceRecord = catalogDbClient.getServiceByID(modelInfo.getModelVersionId());
			recipe = catalogDbClient.getFirstByServiceModelUUIDAndAction(modelInfo.getModelVersionId(), action.toString());
			if (recipe == null){
				serviceRecordList = catalogDbClient.getServiceByModelInvariantUUIDOrderByModelVersionDesc(modelInfo.getModelInvariantId());
				if(!serviceRecordList.isEmpty()){
					for(org.onap.so.db.catalog.beans.Service record : serviceRecordList){
						recipe = catalogDbClient.getFirstByServiceModelUUIDAndAction(record.getModelUUID(),action.toString());
						if(recipe != null){
							break;
						}
					}
				}
			}
		}
		
		//if an aLaCarte flag was sent in the request, throw an error if the recipe was not found
		RequestParameters reqParam = requestDetails.getRequestParameters();
		if(reqParam!=null && alaCarteFlag && recipe==null){
			return null;
		} else if(!alaCarteFlag && recipe != null && Action.createInstance.equals(action)) {
			mapToLegacyRequest(requestDetails);
		}else if (recipe == null) {  //aLaCarte wasn't sent, so we'll try the default
			serviceRecord = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
			recipe = catalogDbClient.getFirstByServiceModelUUIDAndAction(serviceRecord.getModelUUID(),action.toString());
		}
		if(modelInfo.getModelVersionId() == null) {
			modelInfo.setModelVersionId(serviceRecord.getModelUUID());
		}
		if(recipe==null){
			return null;
		}
		return new RecipeLookupResult (recipe.getOrchestrationUri (), recipe.getRecipeTimeout ());
	}

	protected void mapToLegacyRequest(RequestDetails requestDetails) throws IOException {
		RequestParameters reqParam;
		if (requestDetails.getRequestParameters() == null) {
			reqParam = new RequestParameters();
		} else {
			reqParam = requestDetails.getRequestParameters();
		}
		if(requestDetails.getCloudConfiguration() == null) {
			CloudConfiguration cloudConfig = configureCloudConfig(reqParam);
			if(cloudConfig != null) {
				requestDetails.setCloudConfiguration(cloudConfig);
			}
		}
		
		List<Map<String, Object>> userParams = configureUserParams(reqParam);
		if(!userParams.isEmpty()) {
			if (reqParam == null) {
				requestDetails.setRequestParameters(new RequestParameters());
			}
			requestDetails.getRequestParameters().setUserParams(userParams);
		}
	}

	protected CloudConfiguration configureCloudConfig(RequestParameters reqParams) throws IOException {

		for(Map<String, Object> params : reqParams.getUserParams()){
			if(params.containsKey("service")){
				Service service = serviceMapper(params);
				
				Optional<CloudConfiguration> targetConfiguration = addCloudConfig(service.getCloudConfiguration());
				
				if (targetConfiguration.isPresent()) {
					return targetConfiguration.get();
				} else {
					for(Networks network : service.getResources().getNetworks()) {
						targetConfiguration = addCloudConfig(network.getCloudConfiguration());
						if(targetConfiguration.isPresent()) {
							return targetConfiguration.get();
						}
					}
				
					for(Vnfs vnf : service.getResources().getVnfs()) {
						targetConfiguration = addCloudConfig(vnf.getCloudConfiguration());
						
						if(targetConfiguration.isPresent()) {
							return targetConfiguration.get();
						}
						
						for(VfModules vfModule : vnf.getVfModules()) {
							targetConfiguration = addCloudConfig(vfModule.getCloudConfiguration());
							
							if(targetConfiguration.isPresent()) {
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
		if(sourceCloudConfiguration != null) {
			targetConfiguration.setAicNodeClli(sourceCloudConfiguration.getAicNodeClli());
			targetConfiguration.setTenantId(sourceCloudConfiguration.getTenantId());
			targetConfiguration.setLcpCloudRegionId(sourceCloudConfiguration.getLcpCloudRegionId());
			targetConfiguration.setCloudOwner(sourceCloudConfiguration.getCloudOwner());
			return Optional.of(targetConfiguration);
		}
		return Optional.empty();
	}

	protected List<Map<String, Object>> configureUserParams(RequestParameters reqParams) throws IOException {
    	logger.debug("Configuring UserParams for Macro Request");
    	Map<String, Object> userParams = new HashMap<>();
    	
    	for(Map<String, Object> params : reqParams.getUserParams()){
    		if(params.containsKey("service")){
    			Service service = serviceMapper(params);
				
				addUserParams(userParams, service.getInstanceParams());
				
				for(Networks network : service.getResources().getNetworks()) {
					addUserParams(userParams, network.getInstanceParams());
				}
				
				for(Vnfs vnf: service.getResources().getVnfs()) {
					addUserParams(userParams, vnf.getInstanceParams());
					
					for(VfModules vfModule: vnf.getVfModules()) {
						addUserParams(userParams, vfModule.getInstanceParams());
					}
				}
    		}
    	}
    	
    	return mapFlatMapToNameValue(userParams);
    }

	private Service serviceMapper(Map<String, Object> params)
			throws JsonProcessingException, IOException, JsonParseException, JsonMappingException {
		ObjectMapper obj = new ObjectMapper();
		String input = obj.writeValueAsString(params.get("service"));
		return obj.readValue(input, Service.class);
	}

	private void addUserParams(Map<String, Object> targetUserParams, List<Map<String, String>> sourceUserParams) {
		for(Map<String, String> map : sourceUserParams) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				targetUserParams.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	protected List<Map<String, Object>> mapFlatMapToNameValue(Map<String, Object> flatMap) {
		List<Map<String, Object>> targetUserParams = new ArrayList<>();
		
		for(Map.Entry<String, Object> map : flatMap.entrySet()) {
			Map<String, Object> targetMap = new HashMap<>();
			targetMap.put(NAME, map.getKey());
			targetMap.put(VALUE, map.getValue());
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

		Recipe recipe = null;
		String defaultSource = getDefaultModel(servInstReq);
		String modelCustomizationId = modelInfo.getModelCustomizationId();
		String modelCustomizationName = modelInfo.getModelCustomizationName();
		String relatedInstanceModelVersionId = null;
		String relatedInstanceModelInvariantId = null;
		String relatedInstanceVersion = null;
		String relatedInstanceModelCustomizationName = null;

		if (instanceList != null) {

			for(RelatedInstanceList relatedInstanceList : instanceList){

				RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
				ModelInfo relatedInstanceModelInfo = relatedInstance.getModelInfo();
				if(relatedInstanceModelInfo.getModelType().equals(ModelType.service)){
					relatedInstanceModelVersionId = relatedInstanceModelInfo.getModelVersionId();
					relatedInstanceVersion = relatedInstanceModelInfo.getModelVersion();
				}

				if(relatedInstanceModelInfo.getModelType().equals(ModelType.vnf)){
					relatedInstanceModelVersionId = relatedInstanceModelInfo.getModelVersionId();
					relatedInstanceModelInvariantId = relatedInstanceModelInfo.getModelInvariantId();
					relatedInstanceVersion = relatedInstanceModelInfo.getModelVersion();
					relatedInstanceModelCustomizationName = relatedInstanceModelInfo.getModelCustomizationName();
				}
			}

			if(modelInfo.getModelType().equals(ModelType.vnf)) {
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
				VnfResourceCustomization vrc=null;
				// Validation for vnfResource

				if(modelCustomizationId!=null) {
                    vrc = catalogDbClient.getVnfResourceCustomizationByModelCustomizationUUID(modelCustomizationId);
                    if(vrc != null){
                    	vnfResource = vrc.getVnfResources();
                    }
				} else {
					org.onap.so.db.catalog.beans.Service service = catalogDbClient.getServiceByID(relatedInstanceModelVersionId);
					if(service == null) {
						service = catalogDbClient.getServiceByModelVersionAndModelInvariantUUID(relatedInstanceVersion, relatedInstanceModelInvariantId);
					}

		    		if(service == null) {
		    			throw new ValidationException("service in relatedInstance");
		    		}
                    for (VnfResourceCustomization vnfResourceCustom : service.getVnfCustomizations()) {
                        if (vnfResourceCustom.getModelInstanceName().equals(modelCustomizationName)) {
		    				vrc=vnfResourceCustom;
                        }
                    }
					
					if(vrc != null) {
						vnfResource = vrc.getVnfResources();
                        modelInfo.setModelCustomizationId(vrc.getModelCustomizationUUID());
                        modelInfo.setModelCustomizationUuid(vrc.getModelCustomizationUUID());
					}
				}

				if(vnfResource==null){
					throw new ValidationException("vnfResource");
				} else {
					if(modelInfo.getModelVersionId() == null) {
                        modelInfo.setModelVersionId(vnfResource.getModelUUID());
					}
				}

				VnfRecipe vnfRecipe = null;
				
				if(vrc != null) {
					String nfRole = vrc.getNfRole();
					if(nfRole != null) {
						vnfRecipe = catalogDbClient.getFirstVnfRecipeByNfRoleAndAction(vrc.getNfRole(), action.toString());
					}
				}

				if(vnfRecipe == null) {
					vnfRecipe = catalogDbClient.getFirstVnfRecipeByNfRoleAndAction(defaultSource, action.toString());
				}

				if (vnfRecipe == null) {
					return null;
				}

				return new RecipeLookupResult (vnfRecipe.getOrchestrationUri(), vnfRecipe.getRecipeTimeout());
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

				if(modelInfo.getModelCustomizationId() != null) {
					vfmc = catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(modelInfo.getModelCustomizationId());
				} else {
					vnfr = catalogDbClient.getVnfResourceByModelUUID(relatedInstanceModelVersionId);
					if(vnfr == null){
						vnfr = catalogDbClient.getFirstVnfResourceByModelInvariantUUIDAndModelVersion(relatedInstanceModelInvariantId, relatedInstanceVersion);
					}
					vnfrc = catalogDbClient.getFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources(relatedInstanceModelCustomizationName, vnfr);
					
					List<VfModuleCustomization> list = vnfrc.getVfModuleCustomizations();
							
					String vfModuleModelUUID = modelInfo.getModelVersionId();
					for(VfModuleCustomization vf : list) {
						VfModuleCustomization vfmCustom;
						if(vfModuleModelUUID != null){
							vfmCustom = catalogDbClient.getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID(vf.getModelCustomizationUUID(), vfModuleModelUUID);
							if(vfmCustom != null){
								vfModule = vfmCustom.getVfModule();
							}
						}else{ 
							vfmCustom = catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(vf.getModelCustomizationUUID());
							if(vfmCustom != null){
								vfModule = vfmCustom.getVfModule();
							}else{
								vfModule = catalogDbClient.getVfModuleByModelInvariantUUIDAndModelVersion(relatedInstanceModelInvariantId, relatedInstanceVersion);
							}
						}
						
						if(vfModule != null) {
							modelInfo.setModelCustomizationId(vf.getModelCustomizationUUID());
							modelInfo.setModelCustomizationUuid(vf.getModelCustomizationUUID());
							break;
						}
					}
				}

				if(vfmc == null && vfModule == null) {
					throw new ValidationException("vfModuleCustomization");
				} else if (vfModule == null && vfmc != null) {
					vfModule = vfmc.getVfModule(); // can't be null as vfModuleModelUUID is not-null property in VfModuleCustomization table
				}

				if(modelInfo.getModelVersionId() == null) {
					modelInfo.setModelVersionId(vfModule.getModelUUID());
				}
				
				
				recipe = catalogDbClient.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(vfModule.getModelUUID(), vnfComponentType, action.toString());
				if(recipe == null){
					List<VfModule> vfModuleRecords= catalogDbClient.getVfModuleByModelInvariantUUIDOrderByModelVersionDesc(vfModule.getModelInvariantUUID());
					if(!vfModuleRecords.isEmpty()){
						for(VfModule record : vfModuleRecords){
							recipe = catalogDbClient.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(record.getModelUUID(), vnfComponentType, action.toString());
							if(recipe != null){
								break;
							}
						}
					}
				}
				if(recipe == null) {
					recipe = catalogDbClient.getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(defaultSource, vnfComponentType, action.toString());
					if (recipe == null) { 
						recipe = catalogDbClient.getFirstVnfComponentsRecipeByVnfComponentTypeAndAction(vnfComponentType, action.toString());
					}

					if(recipe == null) {
						return null;
					}
				}
			}
		} else {

			if(modelInfo.getModelType().equals(ModelType.vnf)) {
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

		return new RecipeLookupResult (recipe.getOrchestrationUri (), recipe.getRecipeTimeout ());
	}
	
    private RecipeLookupResult getDefaultVnfUri(ServiceInstancesRequest sir, Actions action) {
    	
		String defaultSource = getDefaultModel(sir);

		VnfRecipe vnfRecipe = catalogDbClient.getFirstVnfRecipeByNfRoleAndAction(defaultSource, action.toString());

		if (vnfRecipe == null) {
			return null;
		}

		return new RecipeLookupResult (vnfRecipe.getOrchestrationUri(), vnfRecipe.getRecipeTimeout());		
	}


    private RecipeLookupResult getNetworkUri(ServiceInstancesRequest sir, Actions action) throws ValidationException {

		String defaultNetworkType = getDefaultModel(sir);

		ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
		String modelName = modelInfo.getModelName();
		Recipe recipe = null;

		if(modelInfo.getModelCustomizationId()!=null){
            NetworkResourceCustomization networkResourceCustomization = catalogDbClient.getNetworkResourceCustomizationByModelCustomizationUUID(modelInfo.getModelCustomizationId());
			if(networkResourceCustomization != null){
				NetworkResource networkResource = networkResourceCustomization.getNetworkResource();
			if(networkResource!=null){
				if(modelInfo.getModelVersionId() == null) {
					modelInfo.setModelVersionId(networkResource.getModelUUID());
				}
				recipe = catalogDbClient.getFirstNetworkRecipeByModelNameAndAction(networkResource.getModelName(), action.toString());
			}else{
				throw new ValidationException("no catalog entry found");
			}
			}else if(action != Action.deleteInstance){
				throw new ValidationException("modelCustomizationId for networkResourceCustomization lookup", true);
			}
		}else{
			//ok for version < 3 and action delete
			if(modelName != null){
				recipe = catalogDbClient.getFirstNetworkRecipeByModelNameAndAction(modelName, action.toString());
			}
		}

		if(recipe == null){
			recipe = catalogDbClient.getFirstNetworkRecipeByModelNameAndAction(defaultNetworkType, action.toString());
		}
		
		return recipe !=null ? new RecipeLookupResult(recipe.getOrchestrationUri(), recipe.getRecipeTimeout()) : null;
	}
    
    private Optional<String> retrieveModelName(RequestParameters requestParams) {
    	String requestTestApi = null;
    	TestApi testApi = null;
    	
    	if (requestParams != null) {
    		requestTestApi = requestParams.getTestApi();
    	}
    	
    	if (requestTestApi == null) {
    		if(requestParams != null && requestParams.getALaCarte() != null && !requestParams.getALaCarte()) {
		    	requestTestApi = env.getProperty(CommonConstants.MACRO_TEST_API);
    		} else {
    			requestTestApi = env.getProperty(CommonConstants.ALACARTE_TEST_API);
    		}
    	}
    	
		try {
			testApi = TestApi.valueOf(requestTestApi);
			return Optional.of(testApi.getModelName());
		} catch (Exception e) {
			logger.warn("Catching the exception on the valueOf enum call and continuing", e);
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
		String serviceInstanceId = (instanceIdMap ==null)? null:instanceIdMap.get("serviceInstanceId");
		Boolean aLaCarte = null;
		String apiVersion = version.substring(1);
		boolean inProgress = false;
		
		long startTime = System.currentTimeMillis ();
		ServiceInstancesRequest sir = null;		

		sir = convertJsonToServiceInstanceRequest(requestJSON, action, startTime, sir, msoRequest, requestId, requestUri);
		String requestScope = deriveRequestScope(action,sir, requestUri);
		InfraActiveRequests currentActiveReq =  msoRequest.createRequestObject ( sir,  action, requestId, Status.IN_PROGRESS, requestJSON, requestScope);
		if(sir.getRequestDetails().getRequestParameters() != null){
			aLaCarte = sir.getRequestDetails().getRequestParameters().getALaCarte();
		}
		parseRequest(sir, instanceIdMap, action, version, requestJSON, aLaCarte, requestId, currentActiveReq);
		setInstanceId(currentActiveReq, requestScope, null, instanceIdMap);
		String instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();

		InfraActiveRequests dup = null;
		
		dup = duplicateCheck(action, instanceIdMap, startTime, msoRequest, instanceName,requestScope, currentActiveReq);
		
		if(dup != null){
			inProgress = camundaHistoryCheck(dup, currentActiveReq);
		}

		if (instanceIdMap != null && dup != null && inProgress) {
            buildErrorOnDuplicateRecord(currentActiveReq, action, instanceIdMap, startTime, msoRequest, instanceName, requestScope, dup);
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

            updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());

            throw validateException;
			
		}
		
		serviceInstanceId = "";
		String configurationId = "";
		String pnfCorrelationId = "";

		if(sir.getServiceInstanceId () != null){
			serviceInstanceId = sir.getServiceInstanceId ();
		}

		if(sir.getConfigurationId() != null){
            configurationId = sir.getConfigurationId();
        }

        pnfCorrelationId = getPnfCorrelationId(sir);

		try{
			infraActiveRequestsClient.save(currentActiveReq);
		}catch(Exception e){
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
                    .errorInfo(errorLoggerInfo).build();
		}
		
		if(!requestScope.equalsIgnoreCase(ModelType.service.name())){
			aLaCarte = true;
		}else if(aLaCarte == null){
			aLaCarte = false;
		}
		RequestClientParameter requestClientParameter = null;
		try {
			requestClientParameter = new RequestClientParameter.Builder()
				.setRequestId(requestId)
				.setBaseVfModule(false)
				.setRecipeTimeout(Integer.parseInt(timeOut))
				.setRequestAction(action.toString())
				.setServiceInstanceId(serviceInstanceId)
				.setPnfCorrelationId(pnfCorrelationId)
				.setConfigurationId(configurationId)
				.setRequestDetails(mapJSONtoMSOStyle(requestJSON, sir, aLaCarte, action))
				.setApiVersion(apiVersion)
				.setALaCarte(aLaCarte)
				.setRequestUri(requestUri).build();
		} catch (IOException e) {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
			throw new ValidateException.Builder("Unable to generate RequestClientParamter object" + e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER)
	                    .errorInfo(errorLoggerInfo).build();
		}
				
			return postBPELRequest(currentActiveReq, requestClientParameter, orchestrationUri, requestScope);
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
			ValidateException validateException = new ValidateException.Builder("Request Id " + requestId + " is not a valid UUID", HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER)
	                    .errorInfo(errorLoggerInfo).build();
			
			throw validateException;
    	}
    }
	public void updateStatus(InfraActiveRequests aq, Status status, String errorMessage) throws RequestDbFailureException{
		if ((status == Status.FAILED) || (status == Status.COMPLETE)) {
			aq.setStatusMessage (errorMessage);
			aq.setProgress(new Long(100));
			aq.setRequestStatus(status.toString());
			Timestamp endTimeStamp = new Timestamp (System.currentTimeMillis());
			aq.setEndTime (endTimeStamp);
			try{
				infraActiveRequestsClient.save(aq);
			}catch(Exception e){
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
	            throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
	                    .errorInfo(errorLoggerInfo).build();
			}
		}
	}
	protected String getServiceType(String requestScope, ServiceInstancesRequest sir, Boolean aLaCarteFlag){
		String serviceType = null;
		if(requestScope.equalsIgnoreCase(ModelType.service.toString())){
			String defaultServiceModelName = getDefaultModel(sir);
			org.onap.so.db.catalog.beans.Service serviceRecord;
			if(aLaCarteFlag){
				 serviceRecord = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
				 if(serviceRecord != null){
					 serviceType = serviceRecord.getServiceType();
				 }
			}else{
				serviceRecord = catalogDbClient.getServiceByID(sir.getRequestDetails().getModelInfo().getModelVersionId());
				if(serviceRecord != null){
					 serviceType = serviceRecord.getServiceType();
				 }else{
					 serviceRecord = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
					 if(serviceRecord != null){
						 serviceType = serviceRecord.getServiceType();
					 }
				 }
			}
		}else{
			serviceType = msoRequest.getServiceInstanceType(sir, requestScope);
		}
		return serviceType;
	}
}
