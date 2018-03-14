/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.adapters.vnf;


import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Holder;

import org.apache.http.HttpStatus;

import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.VnfStatus;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.openecomp.mso.adapters.vnfrest.*;

/**
 * This class services calls to the REST interface for VF Modules (http://host:port/vnfs/rest/v2/vnfs)
 * Both XML and JSON can be produced/consumed.  Set Accept: and Content-Type: headers appropriately.  XML is the default.
 * For testing, call with cloudSiteId = ___TESTING___
 * To test exceptions, also set tenantId = ___TESTING___
 * 
 * V2 incorporates run-time selection of sub-orchestrator implementation (Heat or Cloudify)
 * based on the target cloud.
 */
@Path("/v2/vnfs")
public class VnfAdapterRestV2 {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	private static final String TESTING_KEYWORD = "___TESTING___";
	//TODO Logging, SkipAAI, CREATED flags, Integrate with BPEL, Auth,

	@HEAD
	@GET
	@Path("/healthcheck")
	@Produces(MediaType.TEXT_HTML)
	public Response healthcheck () {
		String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application ready</body></html>";
		return Response.ok().entity(CHECK_HTML).build();
	}

   /*
	* URL:http://localhost:8080/vnfs/rest/v2/vnfs/<aaivnfid>/vf-modules/<aaimodid>
	* REQUEST:
	* {"deleteVfModuleRequest":
		{"cloudSiteId": "DAN",
		"tenantId": "214b428a1f554c02935e66330f6a5409",
		"vnfId": "somevnfid",
		"vfModuleId": "somemodid",
		"vfModuleStackId": "4e567676-e266-4594-a3a6-131c8a2baf73",
		"messageId": "ra.1",
		"notificationUrl": "http://localhost:8089/vnfmock",
		"skipAAI": true,
		"msoRequest": {
		"requestId": "ra1",
		"serviceInstanceId": "sa1"
		}}
		}
	*/
	@DELETE
	@Path("{aaiVnfId}/vf-modules/{aaiVfModuleId}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deleteVfModule (
   		@PathParam("aaiVnfId") String aaiVnfId,
		@PathParam("aaiVfModuleId") String aaiVfModuleId,
		@QueryParam("mode") String mode,
		final DeleteVfModuleRequest req)
	{
		LOGGER.debug("Delete VfModule enter: " + req.toJsonString());
		if (aaiVnfId == null || !aaiVnfId.equals(req.getVnfId())) {
			LOGGER.debug("Req rejected - aaiVnfId not provided or doesn't match URL");
			return Response
				.status(HttpStatus.SC_BAD_REQUEST)
				.type(MediaType.TEXT_PLAIN)
				.entity("vnfid in URL does not match content")
				.build();
		}
	   	if (aaiVfModuleId == null || !aaiVfModuleId.equals(req.getVfModuleId())) {
			LOGGER.debug("Req rejected - aaiVfModuleId not provided or doesn't match URL");
			return Response
				.status(HttpStatus.SC_BAD_REQUEST)
				.type(MediaType.TEXT_PLAIN)
				.entity("vfModuleId in URL does not match content")
				.build();
		}
	   	
	   	DeleteVfModuleTask task = new DeleteVfModuleTask(req, mode);
		if (req.isSynchronous()) {
   			// This is a synchronous request
			task.run();
			return Response
				.status(task.getStatusCode())
				.entity(task.getGenericEntityResponse())
   				.build();
   		} else {
			// This is an asynchronous request
			try {
				Thread t1 = new Thread(task);
   				t1.start();
   			} catch (Exception e) {
				// problem handling delete, send generic failure as sync resp to caller
				LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR, "", "deleteVfModule", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in deleteVfModule", e);
				return Response.serverError().build();
   			}
   			// send sync response (ACK) to caller
   			LOGGER.debug ("deleteVNFVolumes exit");
   			return Response.status(HttpStatus.SC_ACCEPTED).build();
   		}
	}

	public class DeleteVfModuleTask implements Runnable {
		private final DeleteVfModuleRequest req;
		private DeleteVfModuleResponse response = null;
		private VfModuleExceptionResponse eresp = null;
		private boolean sendxml;
		private String mode;

		public DeleteVfModuleTask(DeleteVfModuleRequest req, String mode) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
			this.mode = mode;
		}
		public int getStatusCode() {
			return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
		}
		public Object getGenericEntityResponse() {
			return (response != null)
				? new GenericEntity<DeleteVfModuleResponse>(response) {}
				: new GenericEntity<VfModuleExceptionResponse>(eresp) {};
		}
		private String getResponse() {
			if (response != null) {
				return sendxml ? response.toXmlString() : response.toJsonString();
			} else {
				return sendxml ? eresp.toXmlString() : eresp.toJsonString();
			}
		}

		@Override
		public void run() {
			try {
				String cloudsite = req.getCloudSiteId();
				Holder<Map<String, String>> outputs = new Holder <Map <String, String>> ();
				if (cloudsite != null && !cloudsite.equals(TESTING_KEYWORD)) {
					//vnfAdapter.deleteVnf (req.getCloudSiteId(), req.getTenantId(), req.getVfModuleStackId(), req.getMsoRequest());
					// Support different Adapter Implementations
					MsoVnfAdapter adapter = VnfAdapterRestUtils.getVnfAdapterImpl(mode, cloudsite);
					adapter.deleteVfModule (req.getCloudSiteId(), req.getTenantId(), req.getVfModuleStackId(), req.getMsoRequest(), outputs);
				}
				response = new DeleteVfModuleResponse(req.getVnfId(), req.getVfModuleId(), Boolean.TRUE, req.getMessageId(), outputs.value);
			} catch (VnfException e) {
				LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "VnfException - Delete VNF Module", e);
				eresp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.TRUE, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				BpelRestClient bpelClient = new BpelRestClient();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug ("Delete vfModule exit: code=" + getStatusCode() + ", resp="+ getResponse());
		}
	}

	/*
	 * URL:http://localhost:8080/vnfs/rest/v2/vnfs/<aaiVnfId>/vf-modules/<aaiVfModuleId>?cloudSiteId=DAN&tenantId=vfModule?&skipAAI=TRUE&msoRequest.requestId=ra1&msoRequest.serviceInstanceId=si1&vfModuleName=T2N2S1
	 * RESP:
	 * {"queryVfModuleResponse": {
		   "vfModuleId": "AvfmodId",
		   "vfModuleOutputs": {"entry": {
			  "key": "server_private_ip_1",
			  "value": "10.100.1.25"
		   }},
		   "vfModuleStackId": "RaaVnf1/abfa8a6d-feb1-40af-aea3-109403b1cf6b",
		   "vnfId": "AvnfID",
		   "vnfStatus": "ACTIVE"
		}}
	 */
	@GET
	@Path("{aaiVnfId}/vf-modules/{aaiVfModuleId}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response queryVfModule(
		@PathParam("aaiVnfId") String aaiVnfId,
		@PathParam("aaiVfModuleId") String aaiVfModuleId,
		@QueryParam("cloudSiteId") String cloudSiteId,
		@QueryParam("tenantId") String tenantId,
		@QueryParam("vfModuleName") String vfModuleName, //RAA? Id in doc
		@QueryParam("skipAAI") Boolean skipAAI,
		@QueryParam("msoRequest.requestId") String requestId,
		@QueryParam("msoRequest.serviceInstanceId") String serviceInstanceId,
		@QueryParam("mode") String mode)
	{
		//This request responds synchronously only
		LOGGER.debug ("Query vfModule enter:" + vfModuleName);
		MsoRequest msoRequest = new MsoRequest(requestId, serviceInstanceId);

		try {
			int respStatus = HttpStatus.SC_OK;
			QueryVfModuleResponse qryResp = new QueryVfModuleResponse(aaiVnfId, aaiVfModuleId, null, null, null);
			Holder<Boolean> vnfExists = new Holder<Boolean>();
			Holder<String> vfModuleId = new Holder<String>();
			Holder<VnfStatus> status  = new Holder<VnfStatus>();
			Holder<Map<String, String>> outputs = new Holder <Map <String, String>> ();
			
			// Support different Adapter Implementations
			MsoVnfAdapter adapter = VnfAdapterRestUtils.getVnfAdapterImpl(mode, cloudSiteId);
			adapter.queryVnf (cloudSiteId, tenantId, vfModuleName, msoRequest, vnfExists, vfModuleId, status, outputs);

			if (!vnfExists.value) {
				LOGGER.debug ("vfModule not found");
				respStatus = HttpStatus.SC_NOT_FOUND;
			} else {
				LOGGER.debug ("vfModule found" + vfModuleId.value + ", status=" + status.value);
				qryResp.setVfModuleId(vfModuleId.value);
				qryResp.setVnfStatus(status.value);
				qryResp.setVfModuleOutputs(outputs.value);
			}
			LOGGER.debug ("Query vfModule exit");
			return Response
				.status(respStatus)
				.entity(new GenericEntity<QueryVfModuleResponse>(qryResp) {})
				.build();
		} catch (VnfException e) {
			LOGGER.error (MessageEnum.RA_QUERY_VNF_ERR,  vfModuleName, "", "queryVfModule", MsoLogger.ErrorCode.BusinessProcesssError, "VnfException - queryVfModule", e);
			VfModuleExceptionResponse excResp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.FALSE, null);
			return Response
				.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				.entity(new GenericEntity<VfModuleExceptionResponse>(excResp) {})
				.build();
		}
	}

	/*URL: http://localhost:8080/vnfs/rest/v2/vnfs/<aaivnfid>/vf-modules
	 *REQUEST:
	 * {"createVfModuleRequest":
		{"cloudSiteId": "DAN",
		"tenantId": "214b428a1f554c02935e66330f6a5409",
		"vnfId": "somevnfid",
		"vfModuleId": "somemodid",
		"vfModuleName": "RaaVnf1",
		"vnfType": "ApacheVnf",
		"vfModuleParams": {"entry": [
			{"key": "network_id",
			"value": "59ed7b41-2983-413f-ba93-e7d437433916"},
			{"key": "subnet_id",
			"value": "086c9298-5c57-49b7-bb2b-6fd5730c5d92"},
			{"key": "server_name_0",
			"value": "RaaVnf1"}
			]},
		"failIfExists": true,
		"messageId": "ra.1",
		"notificationUrl": "http://localhost:8089/vnfmock",
		"skipAAI": true,
		"msoRequest": {
		"requestId": "ra1",
		"serviceInstanceId": "sa1"
		}}
		}
	 */
	@POST
	@Path("{aaiVnfId}/vf-modules")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response createVfModule(
		@PathParam("aaiVnfId") String aaiVnfId,
		@QueryParam("mode") String mode,
		final CreateVfModuleRequest req)
	{
		LOGGER.debug("Create VfModule enter inside VnfAdapterRest: " + req.toJsonString());
		if (aaiVnfId == null || !aaiVnfId.equals(req.getVnfId())) {
			LOGGER.debug("Req rejected - aaiVnfId not provided or doesn't match URL");
			return Response
				.status(HttpStatus.SC_BAD_REQUEST)
				.type(MediaType.TEXT_PLAIN)
				.entity("vnfid in URL does not match content")
				.build();
		}
		
		CreateVfModuleTask task = new CreateVfModuleTask(req, mode);
		if (req.isSynchronous()) {
   			// This is a synchronous request
			task.run();
			return Response
				.status(task.getStatusCode())
				.entity(task.getGenericEntityResponse())
   				.build();
   		} else {
			// This is an asynchronous request
			try {
				Thread t1 = new Thread(task);
   				t1.start();
   			} catch (Exception e) {
				// problem handling create, send generic failure as sync resp to caller
				LOGGER.error (MessageEnum.RA_CREATE_VNF_ERR, "", "createVfModule", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - createVfModule", e);
				return Response.serverError().build();
   			}
   			// send sync response (ACK) to caller
   			LOGGER.debug ("createVfModule exit");
   			return Response.status(HttpStatus.SC_ACCEPTED).build();
   		}
	}

	public class CreateVfModuleTask implements Runnable {
		private final CreateVfModuleRequest req;
		private CreateVfModuleResponse response = null;
		private VfModuleExceptionResponse eresp = null;
		private boolean sendxml;
		private String mode;

		public CreateVfModuleTask(CreateVfModuleRequest req, String mode) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
			this.mode = mode;
		}
		public int getStatusCode() {
			return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
		}
		public Object getGenericEntityResponse() {
			return (response != null)
				? new GenericEntity<CreateVfModuleResponse>(response) {}
				: new GenericEntity<VfModuleExceptionResponse>(eresp) {};
		}
		private String getResponse() {
			if (response != null) {
				return sendxml ? response.toXmlString() : response.toJsonString();
			} else {
				return sendxml ? eresp.toXmlString() : eresp.toJsonString();
			}
		}

		@Override
		public void run() {
			LOGGER.debug ("CreateVfModuleTask start");
			try {
				// Synchronous Web Service Outputs
				Holder <String> vfModuleStackId = new Holder <String> ();
				Holder <Map <String, String>> outputs = new Holder <Map <String, String>> ();
				Holder <VnfRollback> vnfRollback = new Holder <VnfRollback> ();
				String completeVnfVfModuleType = req.getVnfType() + "::" + req.getVfModuleType();
				LOGGER.debug("completeVnfVfModuleType=" + completeVnfVfModuleType);
				
				String cloudsiteId = req.getCloudSiteId();
				if (cloudsiteId != null && cloudsiteId.equals(TESTING_KEYWORD)) {
					String tenant = req.getTenantId();
					if (tenant != null && tenant.equals(TESTING_KEYWORD)) {
						throw new VnfException("testing.");
					}
					vnfRollback.value = new VnfRollback(req.getVnfId(), tenant, cloudsiteId,
							true, false, new MsoRequest("reqid", "svcid"),
							req.getVolumeGroupId(), req.getVolumeGroupId(), req.getRequestType(), req.getModelCustomizationUuid());
					vfModuleStackId.value = "479D3D8B-6360-47BC-AB75-21CC91981484";
					outputs.value = VolumeAdapterRest.testMap();
				} else {
					// Support different Adapter Implementations
					MsoVnfAdapter adapter = VnfAdapterRestUtils.getVnfAdapterImpl(mode, cloudsiteId);
					adapter.createVfModule(req.getCloudSiteId(),
						req.getTenantId(),
						completeVnfVfModuleType,
						req.getVnfVersion(),
						req.getVfModuleName(),
						req.getRequestType(),
						req.getVolumeGroupStackId(),
						req.getBaseVfModuleStackId(),
						req.getModelCustomizationUuid(),
						req.getVfModuleParams(),
						req.getFailIfExists(),
						req.getBackout(),
						req.getMsoRequest(),
						vfModuleStackId,
						outputs,
						vnfRollback);
				}
				VfModuleRollback modRollback = new VfModuleRollback(vnfRollback.value, req.getVfModuleId(), vfModuleStackId.value, req.getMessageId());
				response = new CreateVfModuleResponse(req.getVnfId(), req.getVfModuleId(),
						vfModuleStackId.value, Boolean.TRUE, outputs.value, modRollback, req.getMessageId());
			} catch (VnfException e) {
				eresp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.TRUE, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				BpelRestClient bpelClient = new BpelRestClient();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug ("CreateVfModuleTask exit: code=" + getStatusCode() + ", resp="+ getResponse());
		}
	}

	@PUT
	@Path("{aaiVnfId}/vf-modules/{aaiVfModuleId}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updateVfModule(
			@PathParam("aaiVnfId") String aaiVnfId,
			@PathParam("aaiVfModuleId") String aaiVfModuleId,
			@QueryParam("mode") String mode,
			final UpdateVfModuleRequest req)
	{
		LOGGER.debug("Update VfModule enter: " + req.toJsonString());
		UpdateVfModulesTask task = new UpdateVfModulesTask(req, mode);
		if (req.isSynchronous()) {
			// This is a synchronous request
			task.run();
			return Response
				.status(task.getStatusCode())
				.entity(task.getGenericEntityResponse())
				.build();
		} else {
			// This is an asynchronous request
	    	try {
	    		Thread t1 = new Thread(task);
	    		t1.start();
	    	} catch (Exception e) {
	    		// problem handling create, send generic failure as sync resp to caller
	    		LOGGER.error (MessageEnum.RA_UPDATE_VNF_ERR, "", "updateVfModule", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - updateVfModule", e);
	    		return Response.serverError().build();
	    	}
	    	// send sync response (ACK) to caller
	    	LOGGER.debug ("updateVfModules exit");
	    	return Response.status(HttpStatus.SC_ACCEPTED).build();
		}
	}

	public class UpdateVfModulesTask implements Runnable {
		private final UpdateVfModuleRequest req;
		private UpdateVfModuleResponse response = null;
		private VfModuleExceptionResponse eresp = null;
		private boolean sendxml;
		private String mode;

		public UpdateVfModulesTask(UpdateVfModuleRequest req, String mode) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
			this.mode = mode;
		}
		public int getStatusCode() {
			return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
		}
		public Object getGenericEntityResponse() {
			return (response != null)
				? new GenericEntity<UpdateVfModuleResponse>(response) {}
				: new GenericEntity<VfModuleExceptionResponse>(eresp) {};
		}
		private String getResponse() {
			if (response != null) {
				return sendxml ? response.toXmlString() : response.toJsonString();
			} else {
				return sendxml ? eresp.toXmlString() : eresp.toJsonString();
			}
		}
		@Override
		public void run() {
			try {
				//MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl (msoPropertiesFactory, cloudConfigFactory);

				// Synchronous Web Service Outputs
				Holder <String> vfModuleStackId = new Holder <String> ();
				Holder <Map <String, String>> outputs = new Holder <Map <String, String>> ();
				Holder <VnfRollback> vnfRollback = new Holder <VnfRollback> ();
				String completeVnfVfModuleType = req.getVnfType() + "::" + req.getVfModuleType();
				LOGGER.debug("in updateVf - completeVnfVfModuleType=" + completeVnfVfModuleType);

				String cloudsiteId = req.getCloudSiteId();

				// Support different Adapter Implementations
				MsoVnfAdapter adapter = VnfAdapterRestUtils.getVnfAdapterImpl(mode, cloudsiteId);
				adapter.updateVfModule (req.getCloudSiteId(),
						req.getTenantId(),
						completeVnfVfModuleType,
						req.getVnfVersion(),
						req.getVfModuleName(),
						req.getRequestType(),
						req.getVolumeGroupStackId(),
						req.getBaseVfModuleId(),
						req.getVfModuleStackId(),
						req.getModelCustomizationUuid(),
						req.getVfModuleParams(),
						req.getMsoRequest(),
						outputs,
						vnfRollback);

				response = new UpdateVfModuleResponse(req.getVnfId(), req.getVfModuleId(),
						vfModuleStackId.value, outputs.value, req.getMessageId());
			} catch (VnfException e) {
				eresp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.TRUE, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = new BpelRestClient ();
				bpelClient.bpelPost (getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug ("Update VfModule exit: code=" + getStatusCode() + ", resp="+ getResponse());
		}
	}
	/*
	 * URL:http://localhost:8080/vnfs/rest/v2/vnfs/<aaivnfid>/vf-modules/<aaimodid>/rollback
	 * REQUEST:
	 * {"deleteVfModuleRequest":
 		{"cloudSiteId": "DAN",
 		"tenantId": "214b428a1f554c02935e66330f6a5409",
 		"vnfId": "somevnfid",
 		"vfModuleId": "somemodid",
 		"vfModuleStackId": "4e567676-e266-4594-a3a6-131c8a2baf73",
 		"messageId": "ra.1",
 		"notificationUrl": "http://localhost:8089/vnfmock",
 		"skipAAI": true,
 		"msoRequest": {
 		"requestId": "ra1",
 		"serviceInstanceId": "sa1"
 		}}
 		}
	 */
	@DELETE
	@Path("{aaiVnfId}/vf-modules/{aaiVfModuleId}/rollback")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response rollbackVfModule (
			@PathParam("aaiVnfId") String aaiVnfId,
			@PathParam("aaiVfModuleId") String aaiVfModuleId,
			//@QueryParam("rollback") String rollback,
			final RollbackVfModuleRequest req)
	{
		LOGGER.debug("Rollback VfModule enter: " + req.toJsonString());
		RollbackVfModulesTask task = new RollbackVfModulesTask(req);
		if (req.isSynchronous()) {
			// This is a synchronous request
			task.run();
			return Response
				.status(task.getStatusCode())
				.entity(task.getGenericEntityResponse())
				.build();
		} else {
			// This is an asynchronous request
	    	try {
	    		Thread t1 = new Thread(task);
	    		t1.start();
	    	} catch (Exception e) {
	    		// problem handling create, send generic failure as sync resp to caller
	    		LOGGER.error (MessageEnum.RA_ROLLBACK_VNF_ERR, "", "rollbackVfModule", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - rollbackVfModule", e);
	    		return Response.serverError().build();
	    	}
	    	// send sync response (ACK) to caller
	    	LOGGER.debug ("rollbackVfModule exit");
	    	return Response.status(HttpStatus.SC_ACCEPTED).build();
		}
	}

	public class RollbackVfModulesTask implements Runnable {
		private final RollbackVfModuleRequest req;
		private RollbackVfModuleResponse response = null;
		private VfModuleExceptionResponse eresp = null;
		private boolean sendxml;

		public RollbackVfModulesTask(RollbackVfModuleRequest req) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
		}
		public int getStatusCode() {
			return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
		}
		public Object getGenericEntityResponse() {
			return (response != null)
				? new GenericEntity<RollbackVfModuleResponse>(response) {}
				: new GenericEntity<VfModuleExceptionResponse>(eresp) {};
		}
		private String getResponse() {
			if (response != null) {
				return sendxml ? response.toXmlString() : response.toJsonString();
			} else {
				return sendxml ? eresp.toXmlString() : eresp.toJsonString();
			}
		}
		@Override
		public void run() {
			try {
				VfModuleRollback vmr = req.getVfModuleRollback();
				VnfRollback vrb = new VnfRollback(
						vmr.getVfModuleStackId(), vmr.getTenantId(), vmr.getCloudSiteId(), true, vmr.isVfModuleCreated(),
						vmr.getMsoRequest(), null, null, null, null);
				
				// Support multiple adapter implementations
				MsoVnfAdapter adapter = VnfAdapterRestUtils.getVnfAdapterImpl (vmr.getMode(), vmr.getCloudSiteId());
				adapter.rollbackVnf (vrb);
				
				response = new RollbackVfModuleResponse(Boolean.TRUE, req.getMessageId());
			} catch (VnfException e) {
				LOGGER.error (MessageEnum.RA_ROLLBACK_VNF_ERR, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - rollbackVfModule", e);
				eresp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, false, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = new BpelRestClient ();
				bpelClient.bpelPost (getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug ("RollbackVfModulesTask exit: code=" + getStatusCode() + ", resp="+ getResponse());
		}
	}
}
