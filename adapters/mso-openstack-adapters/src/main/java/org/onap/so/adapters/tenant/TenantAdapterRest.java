/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.tenant;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.Holder;
import org.onap.so.adapters.tenant.exceptions.TenantAlreadyExists;
import org.onap.so.adapters.tenant.exceptions.TenantException;
import org.onap.so.adapters.tenantrest.CreateTenantError;
import org.onap.so.adapters.tenantrest.CreateTenantRequest;
import org.onap.so.adapters.tenantrest.CreateTenantResponse;
import org.onap.so.adapters.tenantrest.DeleteTenantError;
import org.onap.so.adapters.tenantrest.DeleteTenantRequest;
import org.onap.so.adapters.tenantrest.DeleteTenantResponse;
import org.onap.so.adapters.tenantrest.QueryTenantError;
import org.onap.so.adapters.tenantrest.QueryTenantResponse;
import org.onap.so.adapters.tenantrest.RollbackTenantError;
import org.onap.so.adapters.tenantrest.RollbackTenantRequest;
import org.onap.so.adapters.tenantrest.RollbackTenantResponse;
import org.onap.so.adapters.tenantrest.TenantRollback;
import org.onap.so.openstack.beans.MsoTenant;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class services calls to the REST interface for Tenants (http://host:port/vnfs/rest/v1/tenants)
 * Both XML and JSON can be produced/consumed.  Set Accept: and Content-Type: headers appropriately.  XML is the default.
 */
@Path("/v1/tenants")
@Api(value = "/v1/tenants", description = "root of tenant adapters restful web service")
@Component
public class TenantAdapterRest {

    private static Logger logger = LoggerFactory.getLogger(TenantAdapterRest.class);
	private static final String EXCEPTION= "Exception :";
	//RAA? No logging in wrappers
	@Autowired
	private MsoTenantAdapterImpl tenantImpl;
	
	

	/*
	URL:
	EP: http://host:8080/tenants/rest
	Resource: v1/tenants
	REQ - metadata?
	{
	"cloudSiteId": "DAN",
	"tenantName": "RAA_1",
	"failIfExists": true,
	"msoRequest": {
	"requestId": "ra1",
	"serviceInstanceId": "sa1"
	}}
	RESP-
	{
   "cloudSiteId": "DAN",
   "tenantId": "128e10b9996d43a7874f19bbc4eb6749",
   "tenantCreated": true,
   "tenantRollback":    {
      "tenantId": "128e10b9996d43a7874f19bbc4eb6749",
      "cloudId": "DAN", // RAA? cloudId instead of cloudSiteId
      "tenantCreated": true,
      "msoRequest":       {
         "requestId": "ra1",
         "serviceInstanceId": "sa1"
      }
   	 }
	}
	*/
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "CreateTenant", 
				response = Response.class,
				notes = "Creates a new tenant, CreateTenantRequest data is required")
	@ApiResponses({
		@ApiResponse(code = 200, message = "tenant has been successfully created"),
		@ApiResponse(code = 500, message = "create tenant failed") })	
	public Response createTenant(
			@ApiParam(value = "details of tenant being created", required = true) 			
			CreateTenantRequest req) {
      logger.debug("createTenant enter: {}", req.toJsonString());

		String newTenantId = null;
		TenantRollback tenantRollback = new TenantRollback ();

		try {
			Holder<String> htenant = new Holder<>();
			Holder<TenantRollback> hrollback = new Holder<>();
			MsoTenantAdapter impl = tenantImpl;
		    impl.createTenant(
		    	req.getCloudSiteId(),
		    	req.getTenantName(),
		    	req.getMetadata(),
		    	req.getFailIfExists(),
                req.getBackout(),
                req.getMsoRequest(),
                htenant,
                hrollback);
		    newTenantId = htenant.value;
		    tenantRollback = hrollback.value;

		}
		catch (TenantAlreadyExists tae) {
        logger.debug(EXCEPTION, tae);
			CreateTenantError exc = new CreateTenantError(tae.getMessage(), tae.getFaultInfo().getCategory(), Boolean.TRUE);
			return Response.status(HttpServletResponse.SC_NOT_IMPLEMENTED).entity(exc).build();
		}
		catch (TenantException te) {
        logger.debug(EXCEPTION, te);
			CreateTenantError exc = new CreateTenantError(te.getFaultInfo().getMessage(), te.getFaultInfo().getCategory(), Boolean.TRUE);
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(exc).build();
		}
		catch (Exception e) {
        logger.debug(EXCEPTION, e);
			CreateTenantError exc = new CreateTenantError(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.TRUE);
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(exc).build();
		}

		CreateTenantResponse resp = new CreateTenantResponse (req.getCloudSiteId(), newTenantId, tenantRollback.getTenantCreated(), tenantRollback);
		return Response.status(HttpServletResponse.SC_OK).entity(resp).build();
	}

	/*
	URL:
	http://host:8080/tenants/rest
	Resource: v1/tenant/tennatId
	REQ:
	{"cloudSiteId": "DAN",
	"tenantId": "ca84cd3d3df44272845da554656b3ace",
	"msoRequest": {
	"requestId": "ra1",
	"serviceInstanceId": "sa1"
	}
	}
	RESP:
	{"tenantDeleted": true}
	 */
	@DELETE
	@Path("{tenantId}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "DeleteTenant", 
		response = Response.class,
		notes = "Delete an existing tenant")
	@ApiResponses({
		@ApiResponse(code = 200, message = "tenant has been successfully deleted"),
		@ApiResponse(code = 500, message = "delete tenant failed") })	
	public Response deleteTenant(
		@ApiParam(value = "tenantId of tenant being deleted", required = true)
		@PathParam("tenantId") String tenantId,
		@ApiParam(value = "DeleteTenantRequest object containing additional information of tenant being deleted", required = false)
		DeleteTenantRequest req)
	{
		boolean tenantDeleted = false;

		try {
			Holder<Boolean> deleted = new Holder<>();
			MsoTenantAdapter impl = tenantImpl;
		    impl.deleteTenant(
		    	req.getCloudSiteId(),
		    	req.getTenantId(),
		    	req.getMsoRequest(),
		    	deleted);
		    tenantDeleted = deleted.value;
		}
		catch (TenantException te) {
        logger.debug(EXCEPTION, te);
			DeleteTenantError exc = new DeleteTenantError(te.getFaultInfo().getMessage(), te.getFaultInfo().getCategory(), Boolean.TRUE);
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(exc).build();
		}
		catch (Exception e) {
        logger.debug(EXCEPTION, e);
			DeleteTenantError exc = new DeleteTenantError(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.TRUE);
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(exc).build();
		}
		DeleteTenantResponse resp = new DeleteTenantResponse();
		resp.setTenantDeleted(tenantDeleted);
		return Response.status(HttpServletResponse.SC_OK).entity(resp).build();
	}

	/*
	URL
	EP://http://host:8080/tenants/rest
	Resource: /v1/tenants
	Params:?tenantNameOrId=RAA_1&cloudSiteId=DAN
	RESP
	{
		   "tenantId": "214b428a1f554c02935e66330f6a5409",
		   "tenantName": "RAA_1",
		   "metadata": {}
	}
	*/
	@GET
	@Path("{tenantId}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "QueryTenant", 
		response = Response.class,
		notes = "Query an existing tenant")
	@ApiResponses({
		@ApiResponse(code = 200, message = "tenant has been successfully queried"),
		@ApiResponse(code = 500, message = "query tenant failed") })	
	public Response queryTenant(
			@ApiParam(value = "tenantId", required = true)
			@PathParam("tenantId") String tenantId,
//			@QueryParam("tenantNameOrId") String tenantNameOrId, //RAA? diff from doc
			@ApiParam(value = "cloudSiteId", required = true)
			@QueryParam("cloudSiteId") String cloudSiteId,
			@ApiParam(value = "msoRequest.requestId", required = true)
			@QueryParam("msoRequest.requestId") String requestId,
			@ApiParam(value = "msoRequest.serviceInstanceId", required = true)
			@QueryParam("msoRequest.serviceInstanceId") String serviceInstanceId)
	{
		MsoTenant tenant = null;
		try {
			Holder<String> htenant = new Holder<>();
			Holder<String> tenantName = new Holder<>();
			Holder<Map<String,String>> metadata = new Holder<>();
			MsoTenantAdapter impl = tenantImpl;
		    impl.queryTenant(
		    	cloudSiteId,
		    	tenantId,
		    	null,
		    	htenant,
		    	tenantName,
		    	metadata
		    	);
		    tenant = new MsoTenant(htenant.value, tenantName.value, metadata.value);

		}
		catch (TenantException te) {
        logger.debug(EXCEPTION, te);
			QueryTenantError exc = new QueryTenantError(te.getFaultInfo().getMessage(), te.getFaultInfo().getCategory());
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(exc).build();
		}
		catch (Exception e) {
        logger.debug(EXCEPTION, e);
			QueryTenantError exc = new QueryTenantError(e.getMessage(), MsoExceptionCategory.INTERNAL);
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(exc).build();
		}
		QueryTenantResponse resp = new QueryTenantResponse(tenant.getTenantId(), tenant.getTenantName(), tenant.getMetadata());
		return Response.status(HttpServletResponse.SC_OK).entity(resp).build();
	}

	/*
	URL
	EP: //http://host:8080/tenants/rest
	Resource: /v1/tenants/rollback
	REQ
	{"cloudSiteId": "DAN",
	"tenantId": "f58abb05041d4ff384d4d22d1ccd2a6c",
	"msoRequest": {
	"requestId": "ra1",
	"serviceInstanceId": "sa1"
	}
	}
	RESP:
	{"tenantDeleted": true}
	 */
	@DELETE
	@Path("")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@ApiOperation(value = "RollbackTenant", 
		response = Response.class,
		notes = "Rollback an existing tenant")
	@ApiResponses({
		@ApiResponse(code = 200, message = "tenant has been successfully rolledback"),
		@ApiResponse(code = 500, message = "rollback tenant failed") })	
	public Response rollbackTenant(
		@ApiParam(value = "rollback, command action", required = true)
		@QueryParam("rollback") String action, 
		@ApiParam(value = "RollbackTenantRequest", required = true)
		RollbackTenantRequest req)
	{
		try {
			MsoTenantAdapter impl = tenantImpl;
		    impl.rollbackTenant(req.getTenantRollback());
		}
		catch (TenantException te) {
        logger.debug(EXCEPTION, te);
			RollbackTenantError exc = new RollbackTenantError(te.getFaultInfo().getMessage(), te.getFaultInfo().getCategory(), Boolean.TRUE);
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(exc).build();
		}
		catch (Exception e) {
        logger.debug(EXCEPTION, e);
			RollbackTenantError exc = new RollbackTenantError(e.getMessage(), MsoExceptionCategory.INTERNAL, Boolean.TRUE);
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(exc).build();
		}

		RollbackTenantResponse resp = new RollbackTenantResponse ();
		resp.setTenantRolledback(req != null);
		return Response.status(HttpServletResponse.SC_OK).entity(resp).build();
	}
}
