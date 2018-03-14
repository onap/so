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


import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.Holder;

import org.apache.http.HttpStatus;

import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.QueryVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.RollbackVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.RollbackVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.UpdateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.VolumeGroupExceptionResponse;
import org.openecomp.mso.adapters.vnfrest.VolumeGroupRollback;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.openstack.beans.VnfStatus;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;

/**
 * This class services calls to the REST interface for VNF Volumes (http://host:port/vnfs/rest/v1/volume-groups)
 * Both XML and JSON can be produced/consumed.  Set Accept: and Content-Type: headers appropriately.  XML is the default.
 * For testing, call with cloudSiteId = ___TESTING___
 * To test exceptions, also set tenantId = ___TESTING___
 * 
 * V2 incorporates run-time selection of sub-orchestrator implementation (Heat or Cloudify)
 * based on the target cloud.
 */
@Path("/v2/volume-groups")
public class VolumeAdapterRestV2 {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
	private static final String TESTING_KEYWORD = "___TESTING___";

	@POST
	@Path("")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response createVNFVolumes(
			@QueryParam("mode") String mode,
			final CreateVolumeGroupRequest req)
	{
		LOGGER.debug("createVNFVolumes enter: " + req.toJsonString());
		CreateVNFVolumesTask task = new CreateVNFVolumesTask(req, mode);
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
				LOGGER.error (MessageEnum.RA_CREATE_VNF_ERR, "", "createVNFVolumes", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - createVNFVolumes", e);
				return Response.serverError().build();
			}
			// send sync response (ACK) to caller
			LOGGER.debug ("createVNFVolumes exit");
			return Response.status(HttpStatus.SC_ACCEPTED).build();
		}
	}

	public class CreateVNFVolumesTask implements Runnable {
		private final CreateVolumeGroupRequest req;
		private CreateVolumeGroupResponse response = null;
		private VolumeGroupExceptionResponse eresp = null;
		private boolean sendxml;
		private String mode;

		public CreateVNFVolumesTask(CreateVolumeGroupRequest req, String mode) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
			this.mode = mode;
		}
		public int getStatusCode() {
			return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
		}
		public Object getGenericEntityResponse() {
			return (response != null)
				? new GenericEntity<CreateVolumeGroupResponse>(response) {}
				: new GenericEntity<VolumeGroupExceptionResponse>(eresp) {};
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
			LOGGER.debug ("CreateVFModule VolumesTask start");
			try {
				// Synchronous Web Service Outputs
				Holder<String> stackId = new Holder<>();
				Holder<Map<String, String>> outputs = new Holder<>();
				Holder<VnfRollback> vnfRollback = new Holder<>();
				String completeVnfVfModuleType = req.getVnfType() + "::" + req.getVfModuleType();
				LOGGER.debug("in createVfModuleVolumes - completeVnfVfModuleType=" + completeVnfVfModuleType);

				String cloudsiteId = req.getCloudSiteId();
				if (cloudsiteId != null && cloudsiteId.equals(TESTING_KEYWORD)) {
					String tenant = req.getTenantId();
					if (tenant != null && tenant.equals(TESTING_KEYWORD)) {
						throw new VnfException("testing.");
					}
					stackId.value = "479D3D8B-6360-47BC-AB75-21CC91981484";
					outputs.value = testMap();
				} else {
					// Support different Adapter Implementations
					MsoVnfAdapter vnfAdapter = VnfAdapterRestUtils.getVnfAdapterImpl(mode, cloudsiteId);
					vnfAdapter.createVfModule(
							req.getCloudSiteId(), //cloudSiteId,
							req.getTenantId(), //tenantId,
							completeVnfVfModuleType, //vnfType,
							req.getVnfVersion(), //vnfVersion,
							req.getVolumeGroupName(), //vnfName,
							"VOLUME", //requestType,
							null, //volumeGroupHeatStackId,
							null, //baseVfHeatStackId,
							req.getModelCustomizationUuid(),
							req.getVolumeGroupParams(), //inputs,
							req.getFailIfExists(), //failIfExists,
							req.getSuppressBackout(), //backout,
							req.getMsoRequest(), // msoRequest,
							stackId,
							outputs,
							vnfRollback);
				}
				
				VolumeGroupRollback rb = new VolumeGroupRollback(
						req.getVolumeGroupId(),
						stackId.value,
						vnfRollback.value.getVnfCreated(),
						req.getTenantId(),
						req.getCloudSiteId(),
						req.getMsoRequest(),
						req.getMessageId());
				
				response = new CreateVolumeGroupResponse(
						req.getVolumeGroupId(),
						stackId.value,
						vnfRollback.value.getVnfCreated(),
						outputs.value,
						rb,
						req.getMessageId());
			} catch (VnfException e) {
				LOGGER.debug("Exception :",e);
				eresp = new VolumeGroupExceptionResponse(
					e.getMessage(), MsoExceptionCategory.INTERNAL, true, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = new BpelRestClient();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug ("CreateVFModule VolumesTask exit: code=" + getStatusCode() + ", resp="+ getResponse());
		}
	}

	@DELETE
	@Path("{aaiVolumeGroupId}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deleteVNFVolumes(
		@PathParam("aaiVolumeGroupId") String aaiVolumeGroupId,
		@QueryParam("mode") String mode,
		final DeleteVolumeGroupRequest req
		)
	{
		LOGGER.debug("deleteVNFVolumes enter: " + req.toJsonString());
		if (aaiVolumeGroupId == null || !aaiVolumeGroupId.equals(req.getVolumeGroupId())) {
			return Response
				.status(HttpStatus.SC_BAD_REQUEST)
				.type(MediaType.TEXT_PLAIN)
				.entity("VolumeGroupId in URL does not match content")
				.build();
		}
		DeleteVNFVolumesTask task = new DeleteVNFVolumesTask(req, mode);
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
				LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR, "", "deleteVNFVolumes", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - deleteVNFVolumes", e);
				return Response.serverError().build();
			}
			// send sync response (ACK) to caller
			LOGGER.debug ("deleteVNFVolumes exit");
			return Response.status(HttpStatus.SC_ACCEPTED).build();
		}
	}

	public class DeleteVNFVolumesTask implements Runnable {
		private final DeleteVolumeGroupRequest req;
		private DeleteVolumeGroupResponse response = null;
		private VolumeGroupExceptionResponse eresp = null;
		private boolean sendxml;
		private String mode;

		public DeleteVNFVolumesTask(DeleteVolumeGroupRequest req, String mode) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
			this.mode = mode;
		}
		public int getStatusCode() {
			return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
		}
		public Object getGenericEntityResponse() {
			return (response != null)
				? new GenericEntity<DeleteVolumeGroupResponse>(response) {}
				: new GenericEntity<VolumeGroupExceptionResponse>(eresp) {};
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
			LOGGER.debug("DeleteVNFVolumesTask start");
			String cloudSiteId = req.getCloudSiteId();
			try {
				if (! cloudSiteId.equals(TESTING_KEYWORD)) {
					// Support different Adapter Implementations
					MsoVnfAdapter vnfAdapter = VnfAdapterRestUtils.getVnfAdapterImpl(mode, cloudSiteId);
					vnfAdapter.deleteVnf(req.getCloudSiteId(), req.getTenantId(), req.getVolumeGroupStackId(), req.getMsoRequest());
				}
				response = new DeleteVolumeGroupResponse(true, req.getMessageId());
			} catch (VnfException e) {
				LOGGER.debug("Exception :",e);
				eresp = new VolumeGroupExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, true, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = new BpelRestClient();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug("DeleteVNFVolumesTask exit: code=" + getStatusCode() + ", resp="+ getResponse());
		}
	}

	@DELETE
	@Path("{aaiVolumeGroupId}/rollback")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response rollbackVNFVolumes(
		@PathParam("aaiVolumeGroupId") String aaiVolumeGroupId,
		final RollbackVolumeGroupRequest req
		)
	{
		LOGGER.debug("rollbackVNFVolumes enter: " + req.toJsonString());
		if (aaiVolumeGroupId == null || req.getVolumeGroupRollback() == null || !aaiVolumeGroupId.equals(req.getVolumeGroupRollback().getVolumeGroupId())) {
			return Response
				.status(HttpStatus.SC_BAD_REQUEST)
				.type(MediaType.TEXT_PLAIN)
				.entity("VolumeGroupId in URL does not match content")
				.build();
		}
		RollbackVNFVolumesTask task = new RollbackVNFVolumesTask(req);
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
				LOGGER.error (MessageEnum.RA_ROLLBACK_VNF_ERR, "", "rollbackVNFVolumes", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - rollbackVNFVolumes", e);
				return Response.serverError().build();
			}
			// send sync response (ACK) to caller
			LOGGER.debug("rollbackVNFVolumes exit");
			return Response.status(HttpStatus.SC_ACCEPTED).build();
		}
	}

	public class RollbackVNFVolumesTask implements Runnable {
		private final RollbackVolumeGroupRequest req;
		private RollbackVolumeGroupResponse response = null;
		private VolumeGroupExceptionResponse eresp = null;
		private boolean sendxml;

		public RollbackVNFVolumesTask(RollbackVolumeGroupRequest req) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
		}
		public int getStatusCode() {
			return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
		}
		public Object getGenericEntityResponse() {
			return (response != null)
				? new GenericEntity<RollbackVolumeGroupResponse>(response) {}
				: new GenericEntity<VolumeGroupExceptionResponse>(eresp) {};
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
			LOGGER.debug("RollbackVNFVolumesTask start");
			try {
				VolumeGroupRollback vgr = req.getVolumeGroupRollback();
				VnfRollback vrb = new VnfRollback(
						vgr.getVolumeGroupStackId(), vgr.getTenantId(), vgr.getCloudSiteId(), true, true,
						vgr.getMsoRequest(), null, null, null, null);

				// Support different Adapter Implementations
				MsoVnfAdapter vnfAdapter = VnfAdapterRestUtils.getVnfAdapterImpl(vrb.getMode(), vrb.getCloudSiteId());
				vnfAdapter.rollbackVnf(vrb);
				response = new RollbackVolumeGroupResponse(true, req.getMessageId());
			} catch (VnfException e) {
				LOGGER.debug("Exception :",e);
				eresp = new VolumeGroupExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, true, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = new BpelRestClient();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug("RollbackVNFVolumesTask exit: code=" + getStatusCode() + ", resp="+ getResponse());
		}

	}

	@PUT
	@Path("{aaiVolumeGroupId}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updateVNFVolumes(
		@PathParam("aaiVolumeGroupId") String aaiVolumeGroupId,
		@QueryParam("mode") String mode,
		final UpdateVolumeGroupRequest req
		)
	{
		LOGGER.debug("updateVNFVolumes enter: " + req.toJsonString());
		if (aaiVolumeGroupId == null || !aaiVolumeGroupId.equals(req.getVolumeGroupId())) {
			return Response
				.status(HttpStatus.SC_BAD_REQUEST)
				.type(MediaType.TEXT_PLAIN)
				.entity("VolumeGroupId in URL does not match content")
				.build();
		}
		UpdateVNFVolumesTask task = new UpdateVNFVolumesTask(req, mode);
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
	    		LOGGER.error (MessageEnum.RA_UPDATE_VNF_ERR, "", "updateVNFVolumes", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - updateVNFVolumes", e);
	    		return Response.serverError().build();
	    	}
	    	// send sync response (ACK) to caller
	    	LOGGER.debug ("updateVNFVolumes exit");
	    	return Response.status(HttpStatus.SC_ACCEPTED).build();
		}
	}

	public class UpdateVNFVolumesTask implements Runnable {
		private final UpdateVolumeGroupRequest req;
		private UpdateVolumeGroupResponse response = null;
		private VolumeGroupExceptionResponse eresp = null;
		private boolean sendxml;
		private String mode;

		public UpdateVNFVolumesTask(UpdateVolumeGroupRequest req, String mode) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
			this.mode = mode;
		}
		public int getStatusCode() {
			return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
		}
		public Object getGenericEntityResponse() {
			return (response != null)
				? new GenericEntity<UpdateVolumeGroupResponse>(response) {}
				: new GenericEntity<VolumeGroupExceptionResponse>(eresp) {};
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
			LOGGER.debug("UpdateVNFVolumesTask start");
			try {
				@SuppressWarnings("unused")
				Holder<Map<String, String>> outputs = new Holder<> ();
				Holder<VnfRollback> vnfRollback = new Holder<> ();
				String completeVnfVfModuleType = req.getVnfType() + "::" + req.getVfModuleType();
				LOGGER.debug("in updateVfModuleVolume - completeVnfVfModuleType=" + completeVnfVfModuleType);

				if (req.getCloudSiteId().equals(TESTING_KEYWORD)) {
					outputs.value = testMap();
				} else {
					// Support different Adapter Implementations
					MsoVnfAdapter vnfAdapter = VnfAdapterRestUtils.getVnfAdapterImpl(mode, req.getCloudSiteId());
					vnfAdapter.updateVfModule (req.getCloudSiteId(),
							req.getTenantId(),
							//req.getVnfType(),
							completeVnfVfModuleType,
							req.getVnfVersion(),
							req.getVolumeGroupStackId(),
							"VOLUME",
							null,
							null,
							req.getVolumeGroupStackId(),
							req.getModelCustomizationUuid(),
							req.getVolumeGroupParams(),
							req.getMsoRequest(),
							outputs,
							vnfRollback);
				}
				response = new UpdateVolumeGroupResponse(
						req.getVolumeGroupId(), req.getVolumeGroupStackId(),
						outputs.value, req.getMessageId());
			} catch (VnfException e) {
				LOGGER.debug("Exception :",e);
				eresp = new VolumeGroupExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, true, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = new BpelRestClient();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug("UpdateVNFVolumesTask exit: code=" + getStatusCode() + ", resp="+ getResponse());
		}
	}

	@GET
	@Path("{aaiVolumeGroupId}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response queryVNFVolumes(
		@PathParam("aaiVolumeGroupId") String aaiVolumeGroupId,
		@QueryParam("cloudSiteId") String cloudSiteId,
		@QueryParam("tenantId") String tenantId,
		@QueryParam("volumeGroupStackId") String volumeGroupStackId,
		@QueryParam("skipAAI") Boolean skipAAI,
		@QueryParam("msoRequest.requestId") String requestId,
		@QueryParam("msoRequest.serviceInstanceId") String serviceInstanceId,
		@QueryParam("mode") String mode
		)
	{
    	//This request responds synchronously only
    	LOGGER.debug ("queryVNFVolumes enter:" + aaiVolumeGroupId + " " + volumeGroupStackId);
    	MsoRequest msoRequest = new MsoRequest(requestId, serviceInstanceId);

    	try {
        	int respStatus = HttpStatus.SC_OK;
        	QueryVolumeGroupResponse qryResp = new QueryVolumeGroupResponse(aaiVolumeGroupId, volumeGroupStackId, null, null);
        	Holder<Boolean> vnfExists = new Holder<>();
        	Holder<String> vfModuleId = new Holder<>();
        	Holder<VnfStatus> status = new Holder<>();
        	Holder<Map<String, String>> outputs = new Holder<>();
			if (cloudSiteId != null && cloudSiteId.equals(TESTING_KEYWORD)) {
				if (tenantId != null && tenantId.equals(TESTING_KEYWORD)) {
					throw new VnfException("testing.");
				}
				vnfExists.value = true;
				vfModuleId.value = TESTING_KEYWORD;
				status.value = VnfStatus.ACTIVE;
				outputs.value = testMap();
			} else {
				// Support different Adapter Implementations
				MsoVnfAdapter vnfAdapter = VnfAdapterRestUtils.getVnfAdapterImpl(mode, cloudSiteId);
				vnfAdapter.queryVnf(cloudSiteId, tenantId, volumeGroupStackId, msoRequest, vnfExists, vfModuleId, status, outputs);
			}
    		if (!vnfExists.value) {
    			LOGGER.debug ("VNFVolumes not found");
    			qryResp.setVolumeGroupStatus(status.value);
    			respStatus = HttpStatus.SC_NOT_FOUND;
    		} else {
    			LOGGER.debug ("VNFVolumes found " + vfModuleId.value + ", status=" + status.value);
    			qryResp.setVolumeGroupStatus(status.value);
    			qryResp.setVolumeGroupOutputs(outputs.value);
    		}
        	LOGGER.debug("Query queryVNFVolumes exit");
    		return Response
    			.status(respStatus)
    			.entity(new GenericEntity<QueryVolumeGroupResponse>(qryResp) {})
    			.build();
    	} catch (VnfException e) {
    		LOGGER.error(MessageEnum.RA_QUERY_VNF_ERR,  aaiVolumeGroupId, "", "queryVNFVolumes", MsoLogger.ErrorCode.BusinessProcesssError, "VnfException - queryVNFVolumes", e);
    		VolumeGroupExceptionResponse excResp = new VolumeGroupExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.FALSE, null);
        	LOGGER.debug("Query queryVNFVolumes exit");
    		return Response
    			.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
    			.entity(new GenericEntity<VolumeGroupExceptionResponse>(excResp) {})
    			.build();
		}
	}
    public static Map<String, String> testMap() {
		Map<String, String> m = new HashMap<>();
		m.put("mickey", "7");
		m.put("clyde", "10");
		m.put("wayne", "99");
		return m;
    }
}
