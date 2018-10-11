/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.vnf;


import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;
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
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.QueryVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.RollbackVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.RollbackVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.UpdateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.UpdateVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.VolumeGroupExceptionResponse;
import org.onap.so.adapters.vnfrest.VolumeGroupRollback;
import org.onap.so.entity.MsoRequest;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.openstack.beans.VnfStatus;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class services calls to the REST interface for VNF Volumes (http://host:port/vnfs/rest/v1/volume-groups)
 * Both XML and JSON can be produced/consumed.  Set Accept: and Content-Type: headers appropriately.  XML is the default.
 * For testing, call with cloudSiteId = ___TESTING___
 * To test exceptions, also set tenantId = ___TESTING___
 */
@Path("/v1/volume-groups")
@Api(value = "/v1/volume-groups", description = "root of volume-groups adapters restful web service")
@Component
public class VolumeAdapterRest {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, VolumeAdapterRest.class);
	private static final String TESTING_KEYWORD = "___TESTING___";
	private static final String EXCEPTION="Exception :";
	private static final String RESP=", resp=";
	private static final String VOLUME_GROUPID_IN_URL_DOESNT_MATCH_CONTENT="VolumeGroupId in URL does not match content";
	@Autowired
	private MsoVnfAdapterImpl vnfAdapter;

	@Autowired
	@Qualifier("VnfBpel")
	private Provider<BpelRestClient> bpelRestClientProvider;

	@POST
	@Path("")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "CreateVNFVolumes",
		response = Response.class,
		notes = "Create a new vnfVolume")
	@ApiResponses({
		@ApiResponse(code = 200, message = "vnfVolume has been successfully created"),
		@ApiResponse(code = 202, message = "create vnfVolume request has been successfully accepted (async only)"),
		@ApiResponse(code = 500, message = "create vnfVolume failed, examine entity object for details") })
	public Response createVNFVolumes(
			@ApiParam(value = "CreateVolumeGroupRequest", required = true)
			final CreateVolumeGroupRequest req
			) {
		LOGGER.debug("createVNFVolumes enter: " + req.toJsonString());
		CreateVNFVolumesTask task = new CreateVNFVolumesTask(req);
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

		public CreateVNFVolumesTask(CreateVolumeGroupRequest req) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
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

				String cloudsite = req.getCloudSiteId();
				if (cloudsite != null && cloudsite.equals(TESTING_KEYWORD)) {
					String tenant = req.getTenantId();
					if (tenant != null && tenant.equals(TESTING_KEYWORD)) {
						throw new VnfException("testing.");
					}
					stackId.value = "479D3D8B-6360-47BC-AB75-21CC91981484";
					outputs.value = testMap();
				} else {
//					vnfAdapter.createVnf(
//							req.getCloudSiteId(),
//							req.getTenantId(),
//							req.getVnfType(),
//							req.getVnfVersion(),
//							req.getVolumeGroupName(),
//							"VOLUME",			// request type is VOLUME
//							null,				// not sure about this
//							req.getVolumeGroupParams(),
//							req.getFailIfExists(),
//							req.getSuppressBackout(),
//							req.getMsoRequest(),
//							stackId,
//							outputs,
//							vnfRollback);
					vnfAdapter.createVfModule(
							req.getCloudSiteId(), //cloudSiteId,
							req.getTenantId(), //tenantId,
							//req.getVnfType(), //vnfType,
							completeVnfVfModuleType,
							req.getVnfVersion(), //vnfVersion,
							"", // genericVnfId
							req.getVolumeGroupName(), //vnfName,
							"", // vfModuleid
							"VOLUME", //requestType,
							null, //volumeGroupHeatStackId,
							null, //baseVfHeatStackId,
							req.getModelCustomizationUuid(),
							req.getVolumeGroupParams(), //inputs,
							req.getFailIfExists(), //failIfExists,
							req.getSuppressBackout(), //backout,
							req.getEnableBridge(),
							req.getMsoRequest(), // msoRequest,
							stackId,
							outputs,
							vnfRollback);
				}
				VolumeGroupRollback rb = new VolumeGroupRollback(
						req.getVolumeGroupId(),
						stackId.value,
						true, 						// TODO boolean volumeGroupCreated, when would it be false?
						req.getTenantId(),
						req.getCloudSiteId(),
						req.getMsoRequest(),
						req.getMessageId());
				response = new CreateVolumeGroupResponse(
						req.getVolumeGroupId(),
						stackId.value,
						true, 						// TODO boolean volumeGroupCreated, when would it be false?
						outputs.value,
						rb,
						req.getMessageId());
			} catch (VnfException e) {
				LOGGER.debug(EXCEPTION,e);
				eresp = new VolumeGroupExceptionResponse(
					e.getMessage(), MsoExceptionCategory.INTERNAL, true, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = bpelRestClientProvider.get();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug ("CreateVFModule VolumesTask exit: code=" + getStatusCode() + RESP+ getResponse());
		}
	}

	@DELETE
	@Path("{aaiVolumeGroupId}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "DeleteVNFVolumes",
		response = Response.class,
		notes = "Delete an existing vnfVolume")
	@ApiResponses({
		@ApiResponse(code = 200, message = "vnfVolume has been successfully deleted"),
		@ApiResponse(code = 202, message = "delete vnfVolume request has been successfully accepted (async only)"),
		@ApiResponse(code = 500, message = "delete vnfVolume failed, examine entity object for details") })
	public Response deleteVNFVolumes(
		@ApiParam(value = "aaiVolumeGroupId", required = true)
		@PathParam("aaiVolumeGroupId") String aaiVolumeGroupId,
		@ApiParam(value = "DeleteVolumeGroupRequest", required = true)
		final DeleteVolumeGroupRequest req
		)
	{
		LOGGER.debug("deleteVNFVolumes enter: " + req.toJsonString());
		if (aaiVolumeGroupId == null || !aaiVolumeGroupId.equals(req.getVolumeGroupId())) {
			return Response
				.status(HttpStatus.SC_BAD_REQUEST)
				.type(MediaType.TEXT_PLAIN)
				.entity(VOLUME_GROUPID_IN_URL_DOESNT_MATCH_CONTENT)
				.build();
		}
		DeleteVNFVolumesTask task = new DeleteVNFVolumesTask(req);
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

		public DeleteVNFVolumesTask(DeleteVolumeGroupRequest req) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
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
			try {
				if (!req.getCloudSiteId().equals(TESTING_KEYWORD)) {
					vnfAdapter.deleteVnf(req.getCloudSiteId(), req.getTenantId(), req.getVolumeGroupStackId(), req.getMsoRequest());
				}
				response = new DeleteVolumeGroupResponse(true, req.getMessageId());
			} catch (VnfException e) {
				LOGGER.debug(EXCEPTION,e);
				eresp = new VolumeGroupExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, true, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = bpelRestClientProvider.get();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug("DeleteVNFVolumesTask exit: code=" + getStatusCode() + RESP+ getResponse());
		}
	}

	@DELETE
	@Path("{aaiVolumeGroupId}/rollback")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "RollbackVNFVolumes",
		response = Response.class,
		notes = "Delete an existing vnfVolume")
	@ApiResponses({
		@ApiResponse(code = 200, message = "vnfVolume has been successfully rolled back"),
		@ApiResponse(code = 202, message = "rollback vnfVolume request has been successfully accepted (async only)"),
		@ApiResponse(code = 500, message = "rollback vnfVolume failed, examine entity object for details") })
	public Response rollbackVNFVolumes(
		@ApiParam(value = "aaiVolumeGroupId", required = true)
		@PathParam("aaiVolumeGroupId") String aaiVolumeGroupId,
		@ApiParam(value = "RollbackVolumeGroupRequest", required = true)
		final RollbackVolumeGroupRequest req
		)
	{
		LOGGER.debug("rollbackVNFVolumes enter: " + req.toJsonString());
		if (aaiVolumeGroupId == null || req.getVolumeGroupRollback() == null || !aaiVolumeGroupId.equals(req.getVolumeGroupRollback().getVolumeGroupId())) {
			return Response
				.status(HttpStatus.SC_BAD_REQUEST)
				.type(MediaType.TEXT_PLAIN)
				.entity(VOLUME_GROUPID_IN_URL_DOESNT_MATCH_CONTENT)
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
			LOGGER.debug("DeleteVNFVolumesTask start");
			try {
				VolumeGroupRollback vgr = req.getVolumeGroupRollback();
				VnfRollback vrb = new VnfRollback(
						vgr.getVolumeGroupStackId(), vgr.getTenantId(), vgr.getCloudSiteId(), true, true,
						vgr.getMsoRequest(), null, null, null, null);
				vnfAdapter.rollbackVnf(vrb);
				response = new RollbackVolumeGroupResponse(true, req.getMessageId());
			} catch (VnfException e) {
				LOGGER.debug(EXCEPTION,e);
				eresp = new VolumeGroupExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, true, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = bpelRestClientProvider.get();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug("DeleteVNFVolumesTask exit: code=" + getStatusCode() + RESP+ getResponse());
		}

	}

	@PUT
	@Path("{aaiVolumeGroupId}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "UpdateVNFVolumes",
		response = Response.class,
		notes = "Update an existing vnfVolume")
	@ApiResponses({
		@ApiResponse(code = 200, message = "vnfVolume has been successfully updated"),
		@ApiResponse(code = 202, message = "update vnfVolume request has been successfully accepted (async only)"),
		@ApiResponse(code = 500, message = "update vnfVolume failed, examine entity object for details") })
	public Response updateVNFVolumes(
		@ApiParam(value = "aaiVolumeGroupId", required = true)
		@PathParam("aaiVolumeGroupId") String aaiVolumeGroupId,
		@ApiParam(value = "UpdateVolumeGroupRequest", required = true)
		final UpdateVolumeGroupRequest req
		)
	{
		LOGGER.debug("updateVNFVolumes enter: " + req.toJsonString());
		if (aaiVolumeGroupId == null || !aaiVolumeGroupId.equals(req.getVolumeGroupId())) {
			return Response
				.status(HttpStatus.SC_BAD_REQUEST)
				.type(MediaType.TEXT_PLAIN)
				.entity(VOLUME_GROUPID_IN_URL_DOESNT_MATCH_CONTENT)
				.build();
		}
		UpdateVNFVolumesTask task = new UpdateVNFVolumesTask(req);
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

		public UpdateVNFVolumesTask(UpdateVolumeGroupRequest req) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
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
				Holder<Map<String, String>> outputs = new Holder<> ();
				Holder<VnfRollback> vnfRollback = new Holder<> ();
				String completeVnfVfModuleType = req.getVnfType() + "::" + req.getVfModuleType();
				LOGGER.debug("in updateVfModuleVolume - completeVnfVfModuleType=" + completeVnfVfModuleType);

				if (req.getCloudSiteId().equals(TESTING_KEYWORD)) {
					outputs.value = testMap();
				} else {
					//vnfAdapter.updateVnf(
					//		req.getCloudSiteId(),
					//		req.getTenantId(),
					//		req.getVnfType(),
					//		req.getVnfVersion(),
					//		req.getVfModuleType(),
					//		"VOLUME",			// request type is VOLUME
					//		req.getVolumeGroupStackId(),
					//		req.getVolumeGroupParams(),
					//		req.getMsoRequest(),
					//		outputs,
					//		vnfRollback);
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
				LOGGER.debug(EXCEPTION,e);
				eresp = new VolumeGroupExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, true, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = bpelRestClientProvider.get();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug("UpdateVNFVolumesTask exit: code=" + getStatusCode() + RESP+ getResponse());
		}
	}

	@GET
	@Path("{aaiVolumeGroupId}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "QueryVNFVolumes",
		response = Response.class,
		notes = "Query an existing vnfVolume")
	@ApiResponses({
		@ApiResponse(code = 200, message = "vnfVolume has been successfully queried"),
		@ApiResponse(code = 500, message = "query vnfVolume failed, examine entity object for details") })
	public Response queryVNFVolumes(
		@ApiParam(value = "aaiVolumeGroupId", required = true)
		@PathParam("aaiVolumeGroupId") String aaiVolumeGroupId,
		@ApiParam(value = "cloudSiteId", required = true)
		@QueryParam("cloudSiteId") String cloudSiteId,
		@ApiParam(value = "tenantId", required = true)
		@QueryParam("tenantId") String tenantId,
		@ApiParam(value = "volumeGroupStackId", required = true)
		@QueryParam("volumeGroupStackId") String volumeGroupStackId,
		@ApiParam(value = "skipAAI", required = true)
		@QueryParam("skipAAI") Boolean skipAAI,
		@ApiParam(value = "msoRequest.requestId", required = true)
		@QueryParam("msoRequest.requestId") String requestId,
		@ApiParam(value = "msoRequest.serviceInstanceId", required = true)
		@QueryParam("msoRequest.serviceInstanceId") String serviceInstanceId
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
