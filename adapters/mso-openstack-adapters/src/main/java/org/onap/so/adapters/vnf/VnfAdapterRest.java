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
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.CreateVfModuleResponse;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleResponse;
import org.onap.so.adapters.vnfrest.QueryVfModuleResponse;
import org.onap.so.adapters.vnfrest.RollbackVfModuleRequest;
import org.onap.so.adapters.vnfrest.RollbackVfModuleResponse;
import org.onap.so.adapters.vnfrest.UpdateVfModuleRequest;
import org.onap.so.adapters.vnfrest.UpdateVfModuleResponse;
import org.onap.so.adapters.vnfrest.VfModuleExceptionResponse;
import org.onap.so.adapters.vnfrest.VfModuleRollback;
import org.onap.so.entity.MsoRequest;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.openstack.beans.VnfStatus;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class services calls to the REST interface for VF Modules (http://host:port/vnfs/rest/v1/vnfs)
 * Both XML and JSON can be produced/consumed.  Set Accept: and Content-Type: headers appropriately.  XML is the default.
 * For testing, call with cloudSiteId = ___TESTING___
 * To test exceptions, also set tenantId = ___TESTING___
 */
@Path("/v1/vnfs")
@Api(value = "/v1/vnfs", description = "root of vnf adapters restful web service")
@Transactional
@Component
public class VnfAdapterRest {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, VnfAdapterRest.class);
	private static final String TESTING_KEYWORD = "___TESTING___";
	private static final String RESP=", resp=";

	@Autowired
	private MsoVnfAdapterImpl vnfAdapter;
	//TODO Logging, SkipAAI, CREATED flags, Integrate with BPEL, Auth,

	@Autowired
	@Qualifier("VnfBpel")
	private Provider<BpelRestClient> bpelRestClientProvider;


   /*
	* URL:http://localhost:8080/vnfs/rest/v1/vnfs/<aaivnfid>/vf-modules/<aaimodid>
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
	@ApiOperation(value = "DeleteVfModule",
		response = Response.class,
		notes = "Delete an existing vnfModule, DeleteVfModuleRequest JSON is required")
	@ApiResponses({
		@ApiResponse(code = 200, message = "vnfModule has been successfully deleted"),
		@ApiResponse(code = 202, message = "delete vnfModule request has been accepted (async only)"),
		@ApiResponse(code = 500, message = "delete vnfModule failed, examine entity object for details") })
	public Response deleteVfModule (
		@ApiParam(value = "aaiVnfId", required = true)
   		@PathParam("aaiVnfId") String aaiVnfId,
   		@ApiParam(value = "aaiVfModuleId", required = true)
		@PathParam("aaiVfModuleId") String aaiVfModuleId,
		@ApiParam(value = "DeleteVfModuleRequest", required = true)
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
	   	DeleteVfModuleTask task = new DeleteVfModuleTask(req);
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

		public DeleteVfModuleTask(DeleteVfModuleRequest req) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
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
				Holder<Map<String, String>> outputs = new Holder <> ();
				if (cloudsite != null && !cloudsite.equals(TESTING_KEYWORD)) {
					//vnfAdapter.deleteVnf (req.getCloudSiteId(), req.getTenantId(), req.getVfModuleStackId(), req.getMsoRequest());
					vnfAdapter.deleteVfModule (req.getCloudSiteId(), req.getTenantId(), req.getVfModuleStackId(), req.getMsoRequest(), outputs);
				}
				response = new DeleteVfModuleResponse(req.getVnfId(), req.getVfModuleId(), Boolean.TRUE, req.getMessageId(), outputs.value);
			} catch (VnfException e) {
				LOGGER.error (MessageEnum.RA_DELETE_VNF_ERR, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "VnfException - Delete VNF Module", e);
				eresp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.TRUE, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				BpelRestClient bpelClient = bpelRestClientProvider.get();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug ("Delete vfModule exit: code=" + getStatusCode() + RESP+ getResponse());
		}
	}

	/*
	 * URL:http://localhost:8080/vnfs/rest/v1/vnfs/<aaiVnfId>/vf-modules/<aaiVfModuleId>?cloudSiteId=DAN&tenantId=vfModule?&skipAAI=TRUE&msoRequest.requestId=ra1&msoRequest.serviceInstanceId=si1&vfModuleName=T2N2S1
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
	@ApiOperation(value = "QueryVfModule",
		response = Response.class,
		notes = "Query an existing vnfModule")
	@ApiResponses({
		@ApiResponse(code = 200, message = "vnfModule has been successfully queried"),
		@ApiResponse(code = 500, message = "query vnfModule failed, examine entity object for details") })
	public Response queryVfModule(
		@ApiParam(value = "aaiVnfId", required = true)
		@PathParam("aaiVnfId") String aaiVnfId,
		@ApiParam(value = "aaiVfModuleId", required = true)
		@PathParam("aaiVfModuleId") String aaiVfModuleId,
		@ApiParam(value = "cloudSiteId", required = true)
		@QueryParam("cloudSiteId") String cloudSiteId,
		@ApiParam(value = "tenantId", required = true)
		@QueryParam("tenantId") String tenantId,
		@ApiParam(value = "vfModuleName", required = true)
		@QueryParam("vfModuleName") String vfModuleName, //RAA? Id in doc
		@ApiParam(value = "skipAAI", required = true)
		@QueryParam("skipAAI") Boolean skipAAI,
		@ApiParam(value = "msoRequest.requestId", required = true)
		@QueryParam("msoRequest.requestId") String requestId,
		@ApiParam(value = "msoRequest.serviceInstanceId", required = true)
		@QueryParam("msoRequest.serviceInstanceId") String serviceInstanceId)
	{
		//This request responds synchronously only
		LOGGER.debug ("Query vfModule enter:" + vfModuleName);
		MsoRequest msoRequest = new MsoRequest(requestId, serviceInstanceId);

		try {
			int respStatus = HttpStatus.SC_OK;
			QueryVfModuleResponse qryResp = new QueryVfModuleResponse(aaiVnfId, aaiVfModuleId, null, null, null);
			Holder<Boolean> vnfExists = new Holder<>();
			Holder<String> vfModuleId = new Holder<>();
			Holder<VnfStatus> status  = new Holder<>();
			Holder<Map<String, String>> outputs = new Holder <> ();
			vnfAdapter.queryVnf (cloudSiteId, tenantId, vfModuleName, msoRequest, vnfExists, vfModuleId, status, outputs);
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

	/*URL: http://localhost:8080/vnfs/rest/v1/vnfs/<aaivnfid>/vf-modules
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
	@ApiOperation(value = "CreateVfModule",
		response = Response.class,
		notes = "Create a vnfModule")
	@ApiResponses({
		@ApiResponse(code = 200, message = "vnfModule has been successfully created"),
		@ApiResponse(code = 202, message = "create vnfModule request has been successfully accepted (async only)"),
		@ApiResponse(code = 500, message = "create vnfModule failed, examine entity object for details") })
	public Response createVfModule(
		@ApiParam(value = "aaiVnfId", required = true)
		@PathParam("aaiVnfId") String aaiVnfId,
		@ApiParam(value = "CreateVfModuleRequest", required = true)
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
		CreateVfModuleTask task = new CreateVfModuleTask(req);
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

		public CreateVfModuleTask(CreateVfModuleRequest req) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
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
				Holder <String> vfModuleStackId = new Holder <> ();
				Holder <Map <String, String>> outputs = new Holder <> ();
				Holder <VnfRollback> vnfRollback = new Holder <> ();
				String completeVnfVfModuleType = req.getVnfType() + "::" + req.getVfModuleType();
				LOGGER.debug("completeVnfVfModuleType=" + completeVnfVfModuleType);
				String cloudsite = req.getCloudSiteId();
				if (cloudsite != null && cloudsite.equals(TESTING_KEYWORD)) {
					String tenant = req.getTenantId();
					if (tenant != null && tenant.equals(TESTING_KEYWORD)) {
						throw new VnfException("testing.");
					}
					vnfRollback.value = new VnfRollback(req.getVnfId(), tenant, cloudsite,
							true, false, new MsoRequest("reqid", "svcid"),
							req.getVolumeGroupId(), req.getVolumeGroupId(), req.getRequestType(), req.getModelCustomizationUuid());
					vfModuleStackId.value = "479D3D8B-6360-47BC-AB75-21CC91981484";
					outputs.value = VolumeAdapterRest.testMap();
				} else {
//					vnfAdapter.createVnf (createReq.getCloudSiteId(),
//						createReq.getTenantId(),
//						createReq.getVnfType(),
//						createReq.getVnfVersion(),
//						createReq.getVfModuleName(),
//						createReq.getRequestType(),
//						createReq.getVolumeGroupStackId(),
//						createReq.getVfModuleParams(),
//						createReq.getFailIfExists(),
//						createReq.getBackout(),
//						createReq.getMsoRequest(),
//						vfModuleStackId,
//						outputs,
//						vnfRollback);
					vnfAdapter.createVfModule(req.getCloudSiteId(),
						req.getTenantId(),
						//req.getVnfType(),
						completeVnfVfModuleType,
						req.getVnfVersion(),
						req.getVnfId(),
						req.getVfModuleName(),
						req.getVfModuleId(),
						req.getRequestType(),
						req.getVolumeGroupStackId(),
						req.getBaseVfModuleStackId(),
						req.getModelCustomizationUuid(),
						req.getVfModuleParams(),
						req.getFailIfExists(),
						req.getBackout(),
						req.getEnableBridge(),
						req.getMsoRequest(),
						vfModuleStackId,
						outputs,
						vnfRollback);
				}
				VfModuleRollback modRollback = new VfModuleRollback(vnfRollback.value, req.getVfModuleId(), vfModuleStackId.value, req.getMessageId());
				response = new CreateVfModuleResponse(req.getVnfId(), req.getVfModuleId(),
						vfModuleStackId.value, Boolean.TRUE, outputs.value, modRollback, req.getMessageId());
			} catch (VnfException e) {
				LOGGER.debug("Exception :",e);
				eresp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.TRUE, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				BpelRestClient bpelClient = bpelRestClientProvider.get();
				bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug ("CreateVfModuleTask exit: code=" + getStatusCode() + RESP+ getResponse());
		}
	}

	@PUT
	@Path("{aaiVnfId}/vf-modules/{aaiVfModuleId}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "UpdateVfModule",
		response = Response.class,
		notes = "Update an existing vnfModule")
	@ApiResponses({
		@ApiResponse(code = 200, message = "vnfModule has been successfully updated"),
		@ApiResponse(code = 202, message = "update vnfModule request has been successfully accepted (async only)"),
		@ApiResponse(code = 500, message = "update vnfModule failed, examine entity object for details") })
	public Response updateVfModule(
			@ApiParam(value = "aaiVnfId", required = true)
			@PathParam("aaiVnfId") String aaiVnfId,
			@ApiParam(value = "aaiVfModuleId", required = true)
			@PathParam("aaiVfModuleId") String aaiVfModuleId,
			@ApiParam(value = "UpdateVfModuleRequest", required = true)
			final UpdateVfModuleRequest req)
	{
		LOGGER.debug("Update VfModule enter: " + req.toJsonString());
		UpdateVfModulesTask task = new UpdateVfModulesTask(req);
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

		public UpdateVfModulesTask(UpdateVfModuleRequest req) {
			this.req = req;
			this.sendxml = true; // can be set with a field or header later
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
				Holder <String> vfModuleStackId = new Holder <> ();
				Holder <Map <String, String>> outputs = new Holder <> ();
				Holder <VnfRollback> vnfRollback = new Holder <> ();
				String completeVnfVfModuleType = req.getVnfType() + "::" + req.getVfModuleType();
				LOGGER.debug("in updateVf - completeVnfVfModuleType=" + completeVnfVfModuleType);

				vnfAdapter.updateVfModule (req.getCloudSiteId(),
						req.getTenantId(),
						//req.getVnfType(),
						completeVnfVfModuleType,
						req.getVnfVersion(),
						req.getVfModuleName(),
						req.getRequestType(),
						req.getVolumeGroupStackId(),
						req.getBaseVfModuleStackId(),
						req.getVfModuleStackId(),
						req.getModelCustomizationUuid(),
						req.getVfModuleParams(),
						req.getMsoRequest(),
						outputs,
						vnfRollback);

				response = new UpdateVfModuleResponse(req.getVnfId(), req.getVfModuleId(),
						vfModuleStackId.value, outputs.value, req.getMessageId());
			} catch (VnfException e) {
				LOGGER.debug("Exception :",e);
				eresp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.TRUE, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = bpelRestClientProvider.get();
				bpelClient.bpelPost (getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug ("Update VfModule exit: code=" + getStatusCode() + RESP+ getResponse());
		}
	}
	/*
	 * URL:http://localhost:8080/vnfs/rest/v1/vnfs/<aaivnfid>/vf-modules/<aaimodid>/rollback
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
	@ApiOperation(value = "RollbackVfModule",
		response = Response.class,
		notes = "Rollback an existing vnfModule")
	@ApiResponses({
		@ApiResponse(code = 200, message = "vnfModule has been successfully rolled back"),
		@ApiResponse(code = 202, message = "rollback vnfModule request has been successfully accepted (async only)"),
		@ApiResponse(code = 500, message = "rollback vnfModule failed, examine entity object for details") })
	public Response rollbackVfModule (
			@ApiParam(value = "aaiVnfId", required = true)
			@PathParam("aaiVnfId") String aaiVnfId,
			@ApiParam(value = "aaiVfModuleId", required = true)
			@PathParam("aaiVfModuleId") String aaiVfModuleId,
			@ApiParam(value = "RollbackVfModuleRequest", required = true)
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
						vmr.getVfModuleStackId(), vmr.getTenantId(), vmr.getCloudSiteId(), true, true,
						vmr.getMsoRequest(), null, null, null, null);
				vnfAdapter.rollbackVnf (vrb);
				response = new RollbackVfModuleResponse(Boolean.TRUE, req.getMessageId());
			} catch (VnfException e) {
				LOGGER.error (MessageEnum.RA_ROLLBACK_VNF_ERR, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - rollbackVfModule", e);
				eresp = new VfModuleExceptionResponse(e.getMessage(), MsoExceptionCategory.INTERNAL, false, req.getMessageId());
			}
			if (!req.isSynchronous()) {
				// This is asynch, so POST response back to caller
				BpelRestClient bpelClient = bpelRestClientProvider.get();
				bpelClient.bpelPost (getResponse(), req.getNotificationUrl(), sendxml);
			}
			LOGGER.debug ("RollbackVfModulesTask exit: code=" + getStatusCode() + RESP+ getResponse());
		}
	}
}