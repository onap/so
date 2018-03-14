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
package org.openecomp.mso.apihandlerinfra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openecomp.mso.apihandler.common.CommonConstants;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.apihandler.common.ResponseHandler;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.serviceinstancebeans.ModelInfo;
import org.openecomp.mso.serviceinstancebeans.ModelType;
import org.openecomp.mso.serviceinstancebeans.RelatedInstance;
import org.openecomp.mso.serviceinstancebeans.RelatedInstanceList;
import org.openecomp.mso.serviceinstancebeans.RequestParameters;
import org.openecomp.mso.serviceinstancebeans.RequestReferences;
import org.openecomp.mso.serviceinstancebeans.ServiceInstancesRequest;
import org.openecomp.mso.serviceinstancebeans.ServiceInstancesResponse;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.Recipe;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VnfRecipe;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.utils.UUIDChecker;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/serviceInstances")
@Api(value="/serviceInstances",description="API Requests for Service Instances")
public class ServiceInstances {

	private HashMap<String, String> instanceIdMap = new HashMap<>();
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();

	@POST
	@Path("/{version:[vV][4-6]}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create a Service Instance on a version provided",response=Response.class)
	public Response createServiceInstance(String request, @PathParam("version") String version) {

		Response response = serviceInstances(request, Action.createInstance, null, version);

		return response;
	}
	
	@POST
	@Path("/{version:[vV][5-6]}/{serviceInstanceId}/activate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Activate provided Service Instance",response=Response.class)
	public Response activateServiceInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = serviceInstances(request, Action.activateInstance, instanceIdMap, version);

		return response;
	}
	
	@POST
	@Path("/{version:[vV][5-6]}/{serviceInstanceId}/deactivate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Deactivate provided Service Instance",response=Response.class)
	public Response deactivateServiceInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = serviceInstances(request, Action.deactivateInstance, instanceIdMap, version);

		return response;
	}
	
	@DELETE
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Delete provided Service Instance",response=Response.class)
	public Response deleteServiceInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap, version);
		return response;
	}
	
	@POST
	@Path("/{version:[vV][5-6]}/{serviceInstanceId}/configurations")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create Port Mirroring Configuration",response=Response.class)
	public Response createPortConfiguration(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = configurationRecipeLookup(request, Action.createInstance, instanceIdMap, version);

		return response;
	}
	
	@DELETE
	@Path("/{version:[vV][5-6]}/{serviceInstanceId}/configurations/{configurationInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Delete provided Port",response=Response.class)
	public Response deletePortConfiguration(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
									@PathParam("configurationInstanceId") String configurationInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("configurationInstanceId", configurationInstanceId);
		Response response = configurationRecipeLookup(request, Action.deleteInstance, instanceIdMap, version);
		return response;
	}
	
	@POST
	@Path("/{version:[vV][5-6]}/{serviceInstanceId}/configurations/{configurationInstanceId}/enablePort")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Enable Port Mirroring",response=Response.class)
	public Response enablePort(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
								@PathParam("configurationInstanceId") String configurationInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("configurationInstanceId", configurationInstanceId);
		Response response = configurationRecipeLookup(request, Action.enablePort, instanceIdMap, version);

		return response;
	}
	
	@POST
	@Path("/{version:[vV][5-6]}/{serviceInstanceId}/configurations/{configurationInstanceId}/disablePort")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Disable Port Mirroring",response=Response.class)
	public Response disablePort(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
								@PathParam("configurationInstanceId") String configurationInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("configurationInstanceId", configurationInstanceId);
		Response response = configurationRecipeLookup(request, Action.disablePort, instanceIdMap, version);

		return response;
	}
	
	@POST
	@Path("/{version:[vV][5-6]}/{serviceInstanceId}/configurations/{configurationInstanceId}/activate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Activate Port Mirroring",response=Response.class)
	public Response activatePort(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
								@PathParam("configurationInstanceId") String configurationInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("configurationInstanceId", configurationInstanceId);
		Response response = configurationRecipeLookup(request, Action.activateInstance, instanceIdMap, version);

		return response;
	}
	
	@POST
	@Path("/{version:[vV][5-6]}/{serviceInstanceId}/configurations/{configurationInstanceId}/deactivate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Deactivate Port Mirroring",response=Response.class)
	public Response deactivatePort(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
								@PathParam("configurationInstanceId") String configurationInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("configurationInstanceId", configurationInstanceId);
		Response response = configurationRecipeLookup(request, Action.deactivateInstance, instanceIdMap, version);

		return response;
	}

	@POST
	@Path("/{version:[vV][6]}/{serviceInstanceId}/addRelationships")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Add Relationships to a Service Instance",response=Response.class)
	public Response addRelationships(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId) {
		msoLogger.debug ("version is: " + version);
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = configurationRecipeLookup(request, Action.addRelationships, instanceIdMap, version);

		return response;
	}
	
	@POST
	@Path("/{version:[vV][6]}/{serviceInstanceId}/removeRelationships")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Remove Relationships from Service Instance",response=Response.class)
	public Response removeRelationships(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId) {
		msoLogger.debug ("version is: " + version);
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = configurationRecipeLookup(request, Action.removeRelationships, instanceIdMap, version);

		return response;
	}
	
	@POST
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}/vnfs")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create VNF on a specified version and serviceInstance",response=Response.class)
	public Response createVnfInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId) {
		msoLogger.debug ("version is: " + version);
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap, version);

		return response;
	}
	
	@POST
	@Path("/{version:[vV][5-6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}/replace")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Replace provided VNF instance",response=Response.class)
	public Response replaceVnfInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId) {
		msoLogger.debug ("version is: " + version);
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		Response response = serviceInstances(request, Action.replaceInstance, instanceIdMap, version);

		return response;
	}
	
	@PUT
	@Path("/{version:[vV][5-6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Update VNF on a specified version, serviceInstance and vnfInstance",response=Response.class)
	public Response updateVnfInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId) {			
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);		
		Response response = serviceInstances(request, Action.updateInstance, instanceIdMap, version);

		return response;
	}
	
	@POST
	@Path("/{version:[vV][6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}/applyUpdatedConfig")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Apply updated configuration",response=Response.class)
	public Response applyUpdatedConfig(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId) {			
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);		
		Response response = serviceInstances(request, Action.applyUpdatedConfig, instanceIdMap, version);

		return response;
	}


	@DELETE
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Delete provided VNF instance",response=Response.class)
	public Response deleteVnfInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap, version);

		return response;
	}

	@POST
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create VfModule on a specified version, serviceInstance and vnfInstance",response=Response.class)
	public Response createVfModuleInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId) {
		msoLogger.debug ("version is: " + version);
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap, version);

		return response;
	}
	
	@POST
	@Path("/{version:[vV][5-6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}/replace")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create VfModule on a specified version, serviceInstance and vnfInstance",response=Response.class)
	public Response replaceVfModuleInstance(String request,  @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId,
			@PathParam("vfmoduleInstanceId") String vfmoduleInstanceId) {
		msoLogger.debug ("version is: " + version);
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
		Response response = serviceInstances(request, Action.replaceInstance, instanceIdMap, version);

		return response;
	}

	@PUT
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Update VfModule on a specified version, serviceInstance, vnfInstance and vfModule",response=Response.class)
	public Response updateVfModuleInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId,
			@PathParam("vfmoduleInstanceId") String vfmoduleInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
		Response response = serviceInstances(request, Action.updateInstance, instanceIdMap, version);

		return response;
	}
	
	@POST
	@Path("/{version:[vV][6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}/inPlaceSoftwareUpdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Perform VNF software update",response=Response.class)
	public Response inPlaceSoftwareUpdate(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId) {			
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);		
		Response response = serviceInstances(request, Action.inPlaceSoftwareUpdate, instanceIdMap, version);

		return response;
	}
	
	@DELETE
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfmoduleInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Delete provided VfModule instance",response=Response.class)
	public Response deleteVfModuleInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId,
			@PathParam("vfmoduleInstanceId") String vfmoduleInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("vfModuleInstanceId", vfmoduleInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap, version);

		return response;
	}


	@POST
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create VolumeGroup on a specified version, serviceInstance, vnfInstance",response=Response.class)
	public Response createVolumeGroupInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap, version);

		return response;
	}

	@PUT
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups/{volumeGroupInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Update VolumeGroup on a specified version, serviceInstance, vnfInstance and volumeGroup",response=Response.class)
	public Response updateVolumeGroupInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId,
			@PathParam("volumeGroupInstanceId") String volumeGroupInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("volumeGroupInstanceId", volumeGroupInstanceId);
		Response response = serviceInstances(request, Action.updateInstance, instanceIdMap, version);

		return response;
	}

	@DELETE
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}/vnfs/{vnfInstanceId}/volumeGroups/{volumeGroupInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Delete provided VolumeGroup instance",response=Response.class)
	public Response deleteVolumeGroupInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("vnfInstanceId") String vnfInstanceId,
			@PathParam("volumeGroupInstanceId") String volumeGroupInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("vnfInstanceId", vnfInstanceId);
		instanceIdMap.put("volumeGroupInstanceId", volumeGroupInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap, version);

		return response;
	}

	@POST
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}/networks")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create NetworkInstance on a specified version and serviceInstance ",response=Response.class)
	public Response createNetworkInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		Response response = serviceInstances(request, Action.createInstance, instanceIdMap, version);

		return response;
	}

	@PUT
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}/networks/{networkInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Update VolumeGroup on a specified version, serviceInstance, networkInstance",response=Response.class)
	public Response updateNetworkInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("networkInstanceId") String networkInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("networkInstanceId", networkInstanceId);
		Response response = serviceInstances(request, Action.updateInstance, instanceIdMap, version);

		return response;
	}

	@DELETE
	@Path("/{version:[vV][4-6]}/{serviceInstanceId}/networks/{networkInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Delete provided Network instance",response=Response.class)
	public Response deleteNetworkInstance(String request, @PathParam("version") String version, @PathParam("serviceInstanceId") String serviceInstanceId,
			@PathParam("networkInstanceId") String networkInstanceId) {
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("serviceInstanceId", serviceInstanceId);
		instanceIdMap.put("networkInstanceId", networkInstanceId);
		Response response = serviceInstances(request, Action.deleteInstance, instanceIdMap, version);

		return response;
	}

	private Response serviceInstances(String requestJSON, Action action, HashMap<String,String> instanceIdMap, String version) {

		String requestId = UUIDChecker.generateUUID(msoLogger);
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("requestId is: " + requestId);
		ServiceInstancesRequest sir = null;

		MsoRequest msoRequest = new MsoRequest (requestId);

		try {
			sir = convertJsonToServiceInstanceRequest(requestJSON, action, startTime, sir, msoRequest);
		} catch(Exception e) {
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
					"Mapping of request to JSON object failed.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		try {
			parseRequest(requestJSON, action, instanceIdMap, version, startTime, sir, msoRequest);
		} catch(Exception e) {
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
					"Error parsing request.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			if (msoRequest.getRequestId () != null) {
				msoLogger.debug ("Logging failed message to the database");
				msoRequest.createRequestRecord (Status.FAILED, action);
			}
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}
		
		String instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();
		String requestScope; 
		if(action == Action.inPlaceSoftwareUpdate || action == Action.applyUpdatedConfig){
			requestScope = (ModelType.vnf.name());
		}else{
			requestScope = sir.getRequestDetails().getModelInfo().getModelType().name();
		}
		InfraActiveRequests dup = null;
				
		try {
			dup = duplicateCheck(action, instanceIdMap, startTime, msoRequest, instanceName,requestScope);
		} catch(Exception e) {
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, MsoException.ServiceException,
					e.getMessage(),
					ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
					null) ;
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		if (dup != null) {
			return buildErrorOnDuplicateRecord(action, instanceIdMap, startTime, msoRequest, instanceName, requestScope, dup);
		}

		ServiceInstancesResponse serviceResponse = new ServiceInstancesResponse();

		RequestReferences referencesResponse = new RequestReferences();

		referencesResponse.setRequestId(requestId);

		serviceResponse.setRequestReferences(referencesResponse);

		CatalogDatabase db = null;
		try {
			db = CatalogDatabase.getInstance();
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communciate with Catalog DB", e);
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
					MsoException.ServiceException,
					"No communication to catalog DB " + e.getMessage (),
					ErrorNumbers.SVC_NO_SERVER_RESOURCES,
					null);
			alarmLogger.sendAlarm ("MsoDatabaseAccessError",
					MsoAlarmLogger.CRITICAL,
					Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB));
			msoRequest.createRequestRecord (Status.FAILED,action);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while communciate with DB");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		RecipeLookupResult recipeLookupResult = null;
		try {
			recipeLookupResult = getServiceInstanceOrchestrationURI (db, msoRequest, action);
		} catch (ValidationException e) {
			msoLogger.debug ("Validation failed: ", e);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
					"Error validating request.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			if (msoRequest.getRequestId () != null) {
				msoLogger.debug ("Logging failed message to the database");
				msoRequest.createRequestRecord (Status.FAILED, action);
			}
			msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Validation of the input request failed");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "Exception while querying Catalog DB", e);
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
					MsoException.ServiceException,
					"Recipe could not be retrieved from catalog DB " + e.getMessage (),
					ErrorNumbers.SVC_GENERAL_SERVICE_ERROR,
					null);
			alarmLogger.sendAlarm ("MsoDatabaseAccessError",
					MsoAlarmLogger.CRITICAL,
					Messages.errors.get (ErrorNumbers.ERROR_FROM_CATALOG_DB));
			msoRequest.createRequestRecord (Status.FAILED,action);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while querying Catalog DB");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			db.close();
			return response;
		}

		if (recipeLookupResult == null) {
			msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "No recipe found in DB");
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
					MsoException.ServiceException,
					"Recipe does not exist in catalog DB",
					ErrorNumbers.SVC_GENERAL_SERVICE_ERROR,
					null);
			msoRequest.createRequestRecord (Status.FAILED, action);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "No recipe found in DB");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			db.close();
			return response;
		}


		Boolean isBaseVfModule = false;
		
		if (msoRequest.getModelInfo() != null && (action == Action.applyUpdatedConfig ||
				action == Action.inPlaceSoftwareUpdate)) {
			
		}
		ModelType modelType;
		if (action == Action.applyUpdatedConfig || action == Action.inPlaceSoftwareUpdate) {
			modelType = ModelType.vnf;
		}
		else {
			modelType = msoRequest.getModelInfo().getModelType();
		}
		
		if (modelType.equals(ModelType.vfModule)) {
			String asdcServiceModelVersion = msoRequest.getAsdcServiceModelVersion ();

			// Get VF Module-specific base module indicator
			VfModule vfm;

			String modelVersionId = msoRequest.getModelInfo().getModelVersionId();

			if(modelVersionId != null) {
				vfm = db.getVfModuleByModelUuid(modelVersionId);
			} else {
				vfm = db.getVfModuleByModelInvariantUuidAndModelVersion(msoRequest.getModelInfo().getModelInvariantId(), msoRequest.getModelInfo().getModelVersion());
			}

			if (vfm != null) {
				if (vfm.getIsBase() == 1) {
					isBaseVfModule = true;
				}
			}
			else if (action == Action.createInstance || action == Action.updateInstance){
				// There is no entry for this vfModuleType with this version, if specified, in VF_MODULE table in Catalog DB.
				// This request cannot proceed
				msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, Constants.MSO_PROP_APIHANDLER_INFRA, "VF Module Type", "", MsoLogger.ErrorCode.DataError, "No VfModuleType found in DB");
				msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
				String serviceVersionText = "";
				if (asdcServiceModelVersion != null && !asdcServiceModelVersion.isEmpty ()) {
					serviceVersionText = " with version " + asdcServiceModelVersion;
				}
				Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
						MsoException.ServiceException,
						"VnfType " + msoRequest.getVnfType () + " and VF Module Model Name " + msoRequest.getVfModuleModelName() + serviceVersionText + " not found in MSO Catalog DB",
						ErrorNumbers.SVC_BAD_PARAMETER,
						null);
				msoRequest.createRequestRecord (Status.FAILED, action);
				msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "No matching vfModuleType found in DB");
				msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
				db.close();
				return response;
			}
		}

		db.close();

		String serviceInstanceId = "";
		String vnfId = "";
		String vfModuleId = "";
		String volumeGroupId = "";
		String networkId = "";
		ServiceInstancesRequest siReq = msoRequest.getServiceInstancesRequest();

		if(siReq.getServiceInstanceId () != null){
			serviceInstanceId = siReq.getServiceInstanceId ();
		}

		if(siReq.getVnfInstanceId () != null){
			vnfId = siReq.getVnfInstanceId ();
		}

		if(siReq.getVfModuleInstanceId () != null){
			vfModuleId = siReq.getVfModuleInstanceId ();
		}

		if(siReq.getVolumeGroupInstanceId () != null){
			volumeGroupId = siReq.getVolumeGroupInstanceId ();
		}

		if(siReq.getNetworkInstanceId () != null){
			networkId = siReq.getNetworkInstanceId ();
		}


		requestId = msoRequest.getRequestId ();
		msoLogger.debug ("requestId is: " + requestId);
		msoLogger.debug ("About to insert a record");

		try {
			createRequestRecord(action, startTime, msoRequest);
		} catch(Exception e) {
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
																	MsoException.ServiceException,
																	"Exception while creating record in DB " + e.getMessage(),
																	ErrorNumbers.SVC_BAD_PARAMETER,
																	null);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}
		
		return postBPELRequest(action, requestId, startTime, msoRequest, recipeLookupResult.getOrchestrationURI(), recipeLookupResult.getRecipeTimeout(), 
								isBaseVfModule, serviceInstanceId, vnfId, vfModuleId, volumeGroupId, networkId, null,
								msoRequest.getServiceInstanceType(), msoRequest.getVnfType(), msoRequest.getVfModuleType(), msoRequest.getNetworkType());
	}

	private Response postBPELRequest(Action action, String requestId, long startTime, MsoRequest msoRequest,
									String orchestrationUri, int timeOut, Boolean isBaseVfModule,
									String serviceInstanceId, String vnfId, String vfModuleId, String volumeGroupId, String networkId,
									String configurationId, String serviceInstanceType, String vnfType, String vfModuleType, String networkType) {
		RequestClient requestClient = null;
		HttpResponse response = null;
		long subStartTime = System.currentTimeMillis();
		try {
			requestClient = RequestClientFactory.getRequestClient (orchestrationUri, MsoPropertiesUtils.loadMsoProperties ());
			msoLogger.debug ("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl ());

			System.out.println("URL : " + requestClient.getUrl ());

			response = requestClient.post(requestId, isBaseVfModule, timeOut, action.name (),
					serviceInstanceId, vnfId, vfModuleId, volumeGroupId, networkId, configurationId,
					msoRequest.getServiceInstanceType (),
					msoRequest.getVnfType (), msoRequest.getVfModuleType (),
					msoRequest.getNetworkType (), msoRequest.getRequestJSON(), null);

			msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from BPMN engine", "BPMN", orchestrationUri, null);
		} catch (Exception e) {
			msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine", "BPMN", orchestrationUri, null);
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response resp = msoRequest.buildServiceErrorResponse (HttpStatus.SC_BAD_GATEWAY,
					MsoException.ServiceException,
					"Failed calling bpmn " + e.getMessage (),
					ErrorNumbers.SVC_NO_SERVER_RESOURCES,
					null);
			alarmLogger.sendAlarm ("MsoConfigurationError",
					MsoAlarmLogger.CRITICAL,
					Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_BPEL));
			msoRequest.updateFinalStatus (Status.FAILED);
			msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with BPMN engine");
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity (),e);
			return resp;
		}

		if (response == null) {
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response resp = msoRequest.buildServiceErrorResponse (HttpStatus.SC_BAD_GATEWAY,
					MsoException.ServiceException,
					"bpelResponse is null",
					ErrorNumbers.SVC_NO_SERVER_RESOURCES,
					null);
			msoRequest.updateFinalStatus (Status.FAILED);
			msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Null response from BPEL");
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Null response from BPMN");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
			return resp;
		}

		ResponseHandler respHandler = new ResponseHandler (response, requestClient.getType ());
		int bpelStatus = respHandler.getStatus ();

		// BPEL accepted the request, the request is in progress
		if (bpelStatus == HttpStatus.SC_ACCEPTED) {
			String camundaJSONResponseBody = respHandler.getResponseBody ();
			msoLogger.debug ("Received from Camunda: " + camundaJSONResponseBody);
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.IN_PROGRESS);
			(RequestsDatabase.getInstance()).updateInfraStatus (msoRequest.getRequestId (),
					Status.IN_PROGRESS.toString (),
					Constants.PROGRESS_REQUEST_IN_PROGRESS,
					Constants.MODIFIED_BY_APIHANDLER);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "BPMN accepted the request, the request is in progress");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) camundaJSONResponseBody);
			return Response.status (HttpStatus.SC_ACCEPTED).entity (camundaJSONResponseBody).build ();
		} else {
			List<String> variables = new ArrayList<>();
			variables.add(bpelStatus + "");
			String camundaJSONResponseBody = respHandler.getResponseBody ();
			if (camundaJSONResponseBody != null && !camundaJSONResponseBody.isEmpty ()) {
				msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
				Response resp =  msoRequest.buildServiceErrorResponse(bpelStatus,
						MsoException.ServiceException,
						"Request Failed due to BPEL error with HTTP Status= %1 " + '\n' + camundaJSONResponseBody,
						ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
						variables);
				msoRequest.updateFinalStatus (Status.FAILED);
				msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, requestClient.getUrl (), "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is failed with HTTP Status=" + bpelStatus);
				msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPMN engine is failed");
				msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
				return resp;
			} else {
				msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
				Response resp = msoRequest.buildServiceErrorResponse(bpelStatus,
						MsoException.ServiceException,
						"Request Failed due to BPEL error with HTTP Status= %1" ,
						ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
						variables);
				msoRequest.updateFinalStatus (Status.FAILED);
				msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, requestClient.getUrl (), "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is empty");
				msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPEL engine is empty");
				msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
				return resp;
			}
		}
	}

	private void createRequestRecord(Action action, long startTime, MsoRequest msoRequest) throws Exception {
		try {
			msoRequest.createRequestRecord (Status.PENDING, action);
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC_REASON, "Exception while creating record in DB", "", "", MsoLogger.ErrorCode.SchemaError, "Exception while creating record in DB", e);
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while creating record in DB");
			throw new Exception(e);
		}
	}

	private Response buildErrorOnDuplicateRecord(Action action, HashMap<String, String> instanceIdMap, long startTime, MsoRequest msoRequest, 
											String instanceName, String requestScope, InfraActiveRequests dup) {

		// Found the duplicate record. Return the appropriate error.
		String instance = null;
		if(instanceName != null){
			instance = instanceName;
		}else{
			instance = instanceIdMap.get(requestScope + "InstanceId");
		}
		String dupMessage = "Error: Locked instance - This " + requestScope + " (" + instance + ") " + "already has a request being worked with a status of " + dup.getRequestStatus() + " (RequestId - " + dup.getRequestId() + "). The existing request must finish or be cleaned up before proceeding.";
		//List<String> variables = new ArrayList<String>();
		//variables.add(dup.getRequestStatus());

		Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_CONFLICT, MsoException.ServiceException,
				dupMessage,
				ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
				null) ;


		msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND, dupMessage, "", "", MsoLogger.ErrorCode.SchemaError, "Duplicate request - Subscriber already has a request for this service");
		msoRequest.createRequestRecord (Status.FAILED, action);
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, dupMessage);
		msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
		return response;
	}

	private InfraActiveRequests duplicateCheck(Action action, HashMap<String, String> instanceIdMap, long startTime,
												MsoRequest msoRequest, String instanceName, String requestScope) throws Exception {
		InfraActiveRequests dup = null;
		try {
			if(!(instanceName==null && requestScope.equals("service") && (action == Action.createInstance || action == Action.activateInstance))){
				dup = (RequestsDatabase.getInstance()).checkInstanceNameDuplicate (instanceIdMap, instanceName, requestScope);
			}
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "Error during duplicate check ", e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Error during duplicate check");
			throw new Exception(e);
		}
		return dup;
	}

	private void parseRequest(String originalRequestJSON, Action action, HashMap<String, String> instanceIdMap, String version,
								long startTime, ServiceInstancesRequest sir, MsoRequest msoRequest) throws Exception {
		try{
			msoRequest.parse(sir, instanceIdMap, action, version, originalRequestJSON);
		} catch (Exception e) {
			msoLogger.debug ("Validation failed: ", e);
			msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, originalRequestJSON, e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Validation of the input request failed");
			throw new Exception(e);
		}
	}

	private ServiceInstancesRequest convertJsonToServiceInstanceRequest(String requestJSON, Action action, long startTime,
																		ServiceInstancesRequest sir, MsoRequest msoRequest) throws Exception {
		try{
			ObjectMapper mapper = new ObjectMapper();
			sir = mapper.readValue(requestJSON, ServiceInstancesRequest.class);

		} catch(Exception e){
			msoLogger.debug ("Mapping of request to JSON object failed : ", e);
			if (msoRequest.getRequestId () != null) {
				msoLogger.debug ("Mapping of request to JSON object failed");
				msoRequest.createRequestRecord (Status.FAILED, action);
			}
			msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Mapping of request to JSON object failed");
			throw new Exception(e);
		}
		return sir;
	}

	private RecipeLookupResult getServiceInstanceOrchestrationURI (CatalogDatabase db, MsoRequest msoRequest, Action action) throws Exception {
		RecipeLookupResult recipeLookupResult = null;
		//if the aLaCarte flag is set to TRUE, the API-H should choose the â€œVID_DEFAULTâ€ recipe for the requested action

		msoLogger.debug("aLaCarteFlag is " + msoRequest.getALaCarteFlag());
		// Query MSO Catalog DB
		
		if (action == Action.applyUpdatedConfig || action == Action.inPlaceSoftwareUpdate) {
			recipeLookupResult = getDefaultVnfUri(db, msoRequest, action);
		}
		else if (msoRequest.getModelInfo().getModelType().equals(ModelType.service)) {
			recipeLookupResult = getServiceURI(db, msoRequest, action);
		}
		else if (msoRequest.getModelInfo().getModelType().equals(ModelType.vfModule) ||
				msoRequest.getModelInfo().getModelType().equals(ModelType.volumeGroup) || msoRequest.getModelInfo().getModelType().equals(ModelType.vnf)) {

			recipeLookupResult = getVnfOrVfModuleUri(db, msoRequest, action);

		}else if (msoRequest.getModelInfo().getModelType().equals(ModelType.network)) {

			recipeLookupResult = getNetworkUri(db, msoRequest, action);
		}

		if (recipeLookupResult != null) {
			msoLogger.debug ("Orchestration URI is: " + recipeLookupResult.getOrchestrationURI() + ", recipe Timeout is: " + Integer.toString(recipeLookupResult.getRecipeTimeout ()));
		}
		else {
			msoLogger.debug("No matching recipe record found");
		}
		return recipeLookupResult;
	}


	private RecipeLookupResult getServiceURI (CatalogDatabase db, MsoRequest msoRequest, Action action) throws Exception {
		// SERVICE REQUEST
		// Construct the default service name
		// TODO need to make this a configurable property
		String defaultServiceModelName = "*";
		String defaultSourceServiceModelName = msoRequest.getRequestInfo().getSource() + "_DEFAULT";

		Service serviceRecord;
		ModelInfo modelInfo = msoRequest.getModelInfo();
		if(msoRequest.getALaCarteFlag()){
			serviceRecord = db.getServiceByModelName(defaultSourceServiceModelName);
			if (serviceRecord == null) {
				serviceRecord = db.getServiceByModelName(defaultServiceModelName);
			}
		}else{
			serviceRecord = db.getServiceByModelUUID(modelInfo.getModelVersionId()); // ModelVersionId is not required in v3
			if(serviceRecord == null) {
				serviceRecord = db.getServiceByVersionAndInvariantId(modelInfo.getModelInvariantId(), modelInfo.getModelVersion());
			}
		}

		ServiceRecipe recipe = null;
		if(serviceRecord !=null){
			recipe = db.getServiceRecipeByModelUUID(serviceRecord.getModelUUID(), action.name());
		}
		//if an aLaCarte flag was sent in the request, throw an error if the recipe was not found
		RequestParameters reqParam = msoRequest.getServiceInstancesRequest().getRequestDetails().getRequestParameters();
		if(reqParam!=null && reqParam.isaLaCarte() && recipe==null){
			return null;
		}

		//aLaCarte wasn't sent, so we'll try the default
		serviceRecord = db.getServiceByModelName(defaultSourceServiceModelName);
		if (serviceRecord == null) {
			serviceRecord = db.getServiceByModelName(defaultServiceModelName);
		}

		recipe = db.getServiceRecipeByModelUUID(serviceRecord.getModelUUID(), action.name());
		if(modelInfo.getModelVersionId() == null) {
			modelInfo.setModelVersionId(serviceRecord.getModelUUID());
		}
		if(recipe==null){
			return null;
		}
		return new RecipeLookupResult (recipe.getOrchestrationUri (), recipe.getRecipeTimeout ());
	}


	private RecipeLookupResult getVnfOrVfModuleUri (CatalogDatabase db, MsoRequest msoRequest, Action action) throws Exception {

		ModelInfo modelInfo = msoRequest.getModelInfo();
		String vnfComponentType = modelInfo.getModelType().name();

		RelatedInstanceList[] instanceList = null;
		if (msoRequest.getServiceInstancesRequest().getRequestDetails() != null) {
			instanceList = msoRequest.getServiceInstancesRequest().getRequestDetails().getRelatedInstanceList();
		}

		Recipe recipe = null;
		String defaultSource = msoRequest.getRequestInfo().getSource() + "_DEFAULT";
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
				//    					to â€œjoinâ€ service_to_resource_customizations with vnf_resource_customization to confirm a vnf_resource_customization.model_customization_uuid  record exists. 
				//    				**If relatedInstance.modelInfo[service].modelVersionId  was not provided, use relatedInstance.modelInfo[service].modelInvariantId + modelVersion instead to lookup modelVersionId 
				//    					(MODEL_UUID) in SERVICE table.
				//    				iii.	Regardless of how the value was provided/obtained above, APIH must always populate vnfModelCustomizationId in bpmnRequest.  It would be assumed it was MSO generated 
				//    					during 1707 data migration if VID did not provide it originally on request.
				//    				iv.	Note: continue to construct the â€œvnf-typeâ€ value and pass to BPMN (must still be populated in A&AI).  
				//    				1.	If modelCustomizationName is NOT provided on a vnf/vfModule request, use modelCustomizationId to look it up in our catalog to construct vnf-type value to pass to BPMN.

				VnfResource vnfResource = null;
				VnfResourceCustomization vrc;
				// Validation for vnfResource

				if(modelCustomizationId!=null) {
					vnfResource = db.getVnfResourceByModelCustomizationId(modelCustomizationId);
				} else {
					Service service = db.getServiceByModelUUID(relatedInstanceModelVersionId);
					if(service == null) {
						service = db.getServiceByVersionAndInvariantId(relatedInstanceModelInvariantId, relatedInstanceVersion);
					}

		    		if(service == null) {
		    			throw new ValidationException("service in relatedInstance");
		    		}

					vrc = db.getVnfResourceCustomizationByModelCustomizationName(modelCustomizationName, service.getModelUUID());
					if(vrc != null) {
						vnfResource = vrc.getVnfResource();
						modelInfo.setModelCustomizationId(vrc.getModelCustomizationUuid());
						modelInfo.setModelCustomizationUuid(vrc.getModelCustomizationUuid());
					}
				}

				if(vnfResource==null){
					throw new ValidationException("catalog entry");
				} else {
					if(modelInfo.getModelVersionId() == null) {
						modelInfo.setModelVersionId(vnfResource.getModelUuid());
					}
				}

				VnfRecipe vnfRecipe = db.getVnfRecipe(defaultSource, action.name());

				if (vnfRecipe == null) {
					return null;
				}

				return new RecipeLookupResult (vnfRecipe.getOrchestrationUri(), vnfRecipe.getRecipeTimeout());
			} else {
				//    			ii.	(v2-v4) If modelInfo.modelCustomizationId is NOT provided (because it is a pre-1702 ASDC model or pre-v3), then modelInfo.modelCustomizationName must have 
				//    			been provided (else create request should be rejected).  APIH should use the relatedInstance.modelInfo[vnf].modelVersionId** + modelInfo[vnf].modelCustomizationName 
				//    			to â€œjoinâ€ vnf_to_resource_customizations with vf_resource_customization to confirm a vf_resource_customization.model_customization_uuid  record exists. 
				//    			**If relatedInstance.modelInfo[vnf].modelVersionId  was not provided, use relatedInstance.modelInfo[vnf].modelInvariantId + modelVersion instead 
				//    			to lookup modelVersionId (MODEL_UUID) in vnf_resource table. Once the vnfâ€™s model_customization_uuid has been obtained, use it to find all vfModule customizations 
				//    			for that vnf customization in the vnf_res_custom_to_vf_module_custom join table. For each vf_module_cust_model_customization_uuid value returned, 
				//    			use that UUID to query vf_module_customization table along with modelInfo[vfModule|volumeGroup].modelVersionId** to confirm record matches request data 
				//    			(and to identify the modelCustomizationId associated with the vfModule in the request). **If modelInfo[vfModule|volumeGroup].modelVersionId was not 
				//    			provided (potentially in v2/v3), use modelInfo[vfModule|volumeGroup].modelInvariantId + modelVersion instead. This means taking each record found 
				//    			in vf_module_customization and looking up in vf_module (using vf_module_customizationâ€™s FK into vf_module) to find a match on MODEL_INVARIANT_UUID (modelInvariantId) 
				//    			and MODEL_VERSION (modelVersion).

				VfModuleCustomization vfmc = null;
					VnfResourceCustomization vnfrc;
				VfModule vfModule = null;

				if( modelInfo.getModelCustomizationId() != null) {
					vfmc = db.getVfModuleCustomizationByModelCustomizationId(modelInfo.getModelCustomizationId());
				} else {
					vnfrc =db.getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId(relatedInstanceModelCustomizationName, relatedInstanceModelVersionId);
					if(vnfrc == null) {
						vnfrc = db.getVnfResourceCustomizationByModelInvariantId(relatedInstanceModelInvariantId, relatedInstanceVersion, relatedInstanceModelCustomizationName);
					} 

					List<VfModuleCustomization> list = db.getVfModuleCustomizationByVnfModuleCustomizationUuid(vnfrc.getModelCustomizationUuid());

					String vfModuleModelUUID = modelInfo.getModelVersionId();
					for(VfModuleCustomization vf : list) {
						if(vfModuleModelUUID != null) {
							vfModule = db.getVfModuleByModelCustomizationIdAndVersion(vf.getModelCustomizationUuid(), vfModuleModelUUID);
						} else {
							vfModule = db.getVfModuleByModelCustomizationIdModelVersionAndModelInvariantId(vf.getModelCustomizationUuid(), modelInfo.getModelVersion(), modelInfo.getModelInvariantId());
						}

						if(vfModule != null) {
							modelInfo.setModelCustomizationId(vf.getModelCustomizationUuid());
							modelInfo.setModelCustomizationUuid(vf.getModelCustomizationUuid());
							break;
						}
					}
				}

				if(vfmc == null && vfModule == null) {
					throw new ValidationException("no catalog entry found");
				} else if (vfModule == null && vfmc != null) {
					vfModule = vfmc.getVfModule(); // can't be null as vfModuleModelUUID is not-null property in VfModuleCustomization table
				}

				if(modelInfo.getModelVersionId() == null) {
					modelInfo.setModelVersionId(vfModule.getModelUUID());
				}
				recipe = db.getVnfComponentsRecipeByVfModuleModelUUId(vfModule.getModelUUID(), vnfComponentType, action.name());

				if(recipe == null) {
					recipe = db.getVnfComponentsRecipeByVfModuleModelUUId(defaultSource, vnfComponentType, action.name());
					if (recipe == null) { 
						recipe = db.getVnfComponentsRecipeByVfModuleModelUUId("*", vnfComponentType, action.name());
					}

					if(recipe == null) {
						return null;
					}
				}
			}
		} else {
			msoLogger.debug("recipe is null, getting default");

			if(modelInfo.getModelType().equals(ModelType.vnf)) {
				recipe = db.getVnfRecipe(defaultSource, action.name());
				if (recipe == null) {
					return null;
				}
			} else {
				recipe = db.getVnfComponentsRecipeByVfModuleModelUUId(defaultSource, vnfComponentType, action.name());

				if (recipe == null) {
					return null;
				}
			}
		}

		return new RecipeLookupResult (recipe.getOrchestrationUri (), recipe.getRecipeTimeout ());
	}
	
	private RecipeLookupResult getDefaultVnfUri (CatalogDatabase db, MsoRequest msoRequest, Action action) throws Exception {

		String defaultSource = msoRequest.getRequestInfo().getSource() + "_DEFAULT";		

		VnfRecipe vnfRecipe = db.getVnfRecipe(defaultSource, action.name());

		if (vnfRecipe == null) {
			return null;
		}

		return new RecipeLookupResult (vnfRecipe.getOrchestrationUri(), vnfRecipe.getRecipeTimeout());		
	}


	private RecipeLookupResult getNetworkUri (CatalogDatabase db, MsoRequest msoRequest, Action action) throws Exception {

		String defaultNetworkType = msoRequest.getRequestInfo().getSource() + "_DEFAULT";

		ModelInfo modelInfo = msoRequest.getModelInfo();
		String modelName = modelInfo.getModelName();
		Recipe recipe = null;

		if(modelInfo.getModelCustomizationId()!=null){
			NetworkResource networkResource = db.getNetworkResourceByModelCustUuid(modelInfo.getModelCustomizationId());
			if(networkResource!=null){
				if(modelInfo.getModelVersionId() == null) {
					modelInfo.setModelVersionId(networkResource.getModelUUID());
				}
				recipe = db.getNetworkRecipe(networkResource.getModelName(), action.name());
			}else{
				throw new ValidationException("no catalog entry found");
			}
		}else{
			//ok for version < 3 and action delete
			recipe = db.getNetworkRecipe(modelName, action.name());
		}

		if(recipe == null){
			recipe = db.getNetworkRecipe(defaultNetworkType, action.name());
		}
		
		return recipe !=null ? new RecipeLookupResult(recipe.getOrchestrationUri(), recipe.getRecipeTimeout()) : null;
	}
	
	private Response configurationRecipeLookup(String requestJSON, Action action, HashMap<String,String> instanceIdMap, String version) {
		String requestId = UUIDChecker.generateUUID(msoLogger);
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("requestId is: " + requestId);
		ServiceInstancesRequest sir = null;
		MsoRequest msoRequest = new MsoRequest (requestId);

		try {
			sir = convertJsonToServiceInstanceRequest(requestJSON, action, startTime, sir, msoRequest);
		} catch(Exception e) {
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
					"Mapping of request to JSON object failed.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		try {
			parseRequest(requestJSON, action, instanceIdMap, version, startTime, sir, msoRequest);
		} catch(Exception e) {
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
					"Error parsing request.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		String instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();
		String requestScope;
		if(action == Action.inPlaceSoftwareUpdate || action == Action.applyUpdatedConfig){
			requestScope = (ModelType.vnf.name());
		}else{
			requestScope = sir.getRequestDetails().getModelInfo().getModelType().name();
		}
		InfraActiveRequests dup = null;
		
		try {
			dup = duplicateCheck(action, instanceIdMap, startTime, msoRequest, instanceName,requestScope);
		} catch(Exception e) {
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, MsoException.ServiceException,
					e.getMessage(),
					ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
					null) ;
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		if (dup != null) {
			return buildErrorOnDuplicateRecord(action, instanceIdMap, startTime, msoRequest, instanceName, requestScope, dup);
		}

		ServiceInstancesResponse serviceResponse = new ServiceInstancesResponse();
		RequestReferences referencesResponse = new RequestReferences();
		referencesResponse.setRequestId(requestId);
		serviceResponse.setRequestReferences(referencesResponse);
		
		MsoJavaProperties props = MsoPropertiesUtils.loadMsoProperties ();
		String orchestrationUri = props.getProperty(CommonConstants.ALACARTE_ORCHESTRATION, null);
		String timeOut = props.getProperty(CommonConstants.ALACARTE_RECIPE_TIMEOUT, null);
		
		if (StringUtils.isBlank(orchestrationUri) || StringUtils.isBlank(timeOut)) {
			String error = StringUtils.isBlank(orchestrationUri) ? "ALaCarte Orchestration URI not found in properties" : "ALaCarte Recipe Timeout not found in properties";
			
			msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", 
			MsoLogger.ErrorCode.DataError, error);
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
																	MsoException.ServiceException,
																	error,
																	ErrorNumbers.SVC_GENERAL_SERVICE_ERROR,
																	null);
			msoRequest.createRequestRecord (Status.FAILED, action);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, error);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
			
		}
		
		String serviceInstanceId = "";
		String configurationId = "";
		ServiceInstancesRequest siReq = msoRequest.getServiceInstancesRequest();

		if(siReq.getServiceInstanceId () != null){
			serviceInstanceId = siReq.getServiceInstanceId ();
		}

		if(siReq.getConfigurationId() != null){
			configurationId = siReq.getConfigurationId();
		}

		requestId = msoRequest.getRequestId ();
		msoLogger.debug ("requestId is: " + requestId);
		msoLogger.debug ("About to insert a record");

		try {
			createRequestRecord(action, startTime, msoRequest);
		} catch(Exception e) {
			Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_INTERNAL_SERVER_ERROR,
					MsoException.ServiceException,
					"Exception while creating record in DB " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER,
					null);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		return postBPELRequest(action, requestId, startTime, msoRequest, orchestrationUri, Integer.parseInt(timeOut), false, 
								serviceInstanceId, null, null, null, null, configurationId, null, null, null, null);
	}
}
