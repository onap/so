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

package org.onap.so.adapters.network;


import java.util.HashMap;
import java.util.List;
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
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.adapters.network.exceptions.NetworkException;
import org.onap.so.adapters.nwrest.ContrailNetwork;
import org.onap.so.adapters.nwrest.CreateNetworkError;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkError;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.ProviderVlanNetwork;
import org.onap.so.adapters.nwrest.QueryNetworkError;
import org.onap.so.adapters.nwrest.QueryNetworkResponse;
import org.onap.so.adapters.nwrest.RollbackNetworkError;
import org.onap.so.adapters.nwrest.RollbackNetworkRequest;
import org.onap.so.adapters.nwrest.RollbackNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkError;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.adapters.vnf.BpelRestClient;
import org.onap.so.entity.MsoRequest;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.beans.NetworkRollback;
import org.onap.so.openstack.beans.NetworkStatus;
import org.onap.so.openstack.beans.RouteTarget;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/v1/networks")
@Api(value = "/v1/networks", description = "root of network adapters restful web service")
@Component
@Transactional
public class NetworkAdapterRest {

    private static final Logger logger = LoggerFactory.getLogger(NetworkAdapterRest.class);
    private static final String TESTING_KEYWORD = "___TESTING___";
    private String exceptionMsg = "Exception:";
    private static final String SHARED = "shared";
    private static final String EXTERNAL = "external";

    @Autowired
    private MsoNetworkAdapterImpl adapter;

    @Autowired
    private Provider<BpelRestClient> bpelRestClientProvider;


    @POST
    @Path("")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "CreateNetwork", response = Response.class,
            notes = "Creates a new network, CreateNetworkRquest JSON is required")
    @ApiResponses({@ApiResponse(code = 200, message = "network has been successfully created"),
            @ApiResponse(code = 202, message = "create network request has been accepted (async only)"),
            @ApiResponse(code = 500, message = "create network failed, examine entity object for details")})
    public Response createNetwork(
            @ApiParam(value = "details of network being created", required = true) CreateNetworkRequest req) {
        logger.debug("createNetwork enter: {}", req.toJsonString());
        CreateNetworkTask task = new CreateNetworkTask(req);
        if (req.isSynchronous()) {
            // This is a synchronous request
            task.run();
            return Response.status(task.getStatusCode()).entity(task.getGenericEntityResponse()).build();
        } else {
            // This is an asynchronous request
            try {
                Thread t1 = new Thread(task);
                t1.start();
            } catch (Exception e) {
                // problem handling create, send generic failure as sync resp to caller
                logger.error("{} {} Exception while create network ", MessageEnum.RA_CREATE_NETWORK_EXC,
                        ErrorCode.BusinessProcessError.getValue(), e);
                return Response.serverError().build();
            }
            // send sync response (ACK) to caller
            logger.debug("createNetwork exit");
            return Response.status(HttpStatus.SC_ACCEPTED).build();
        }
    }

    public class CreateNetworkTask implements Runnable {
        private final CreateNetworkRequest req;
        private CreateNetworkResponse response = null;
        private CreateNetworkError eresp = null;
        private boolean sendxml;

        public CreateNetworkTask(CreateNetworkRequest req) {
            this.req = req;
            this.sendxml = true; // can be set with a field or header later
        }

        public int getStatusCode() {
            return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
        }

        public Object getGenericEntityResponse() {
            return (response != null) ? new GenericEntity<CreateNetworkResponse>(response) {}
                    : new GenericEntity<CreateNetworkError>(eresp) {};
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
                MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, req.getMsoRequest().getRequestId());
            } catch (Exception e) {
                logger.error("Error adding RequestId to MDC", e);
            }
            logger.debug("CreateNetworkTask start");
            try {
                // Synchronous Web Service Outputs
                Holder<String> networkId = new Holder<>();
                Holder<String> neutronNetworkId = new Holder<>();
                Holder<String> networkFqdn = new Holder<>();
                Holder<Map<String, String>> subnetIdMap = new Holder<>();
                Holder<NetworkRollback> rollback = new Holder<>();

                HashMap<String, String> params = (HashMap<String, String>) req.getNetworkParams();
                if (params == null) {
                    params = new HashMap<>();
                }
                String shared = null;
                String external = null;

                String cloudsite = req.getCloudSiteId();
                if (cloudsite != null && cloudsite.equals(TESTING_KEYWORD)) {
                    String tenant = req.getTenantId();
                    if (tenant != null && tenant.equals(TESTING_KEYWORD)) {
                        throw new NetworkException("testing.");
                    }
                    networkId.value = "479D3D8B-6360-47BC-AB75-21CC91981484";
                    neutronNetworkId.value = "55e55884-28fa-11e6-8971-0017f20fe1b8";
                    networkFqdn.value = "086f70b6-28fb-11e6-8260-0017f20fe1b8";
                    subnetIdMap.value = testMap();
                    rollback.value = new NetworkRollback();
                } else if (req.isContrailRequest()) {
                    ContrailNetwork ctn = req.getContrailNetwork();
                    if (ctn == null) {
                        ctn = new ContrailNetwork();
                        req.setContrailNetwork(ctn);
                    }
                    if (params.containsKey(SHARED)) {
                        shared = params.get(SHARED);
                    } else {
                        if (ctn.getShared() != null) {
                            shared = ctn.getShared();
                        }
                    }
                    if (params.containsKey(EXTERNAL)) {
                        external = params.get(EXTERNAL);
                    } else {
                        if (ctn.getExternal() != null) {
                            external = ctn.getExternal();
                        }
                    }
                    adapter.createNetworkContrail(req.getCloudSiteId(), req.getTenantId(), req.getNetworkType(),
                            req.getModelCustomizationUuid(), req.getNetworkName(),
                            req.getContrailNetwork().getRouteTargets(), shared, external, req.getFailIfExists(),
                            req.getBackout(), req.getSubnets(), params, req.getContrailNetwork().getPolicyFqdns(),
                            req.getContrailNetwork().getRouteTableFqdns(), req.getMsoRequest(), networkId,
                            neutronNetworkId, networkFqdn, subnetIdMap, rollback);
                } else {
                    ProviderVlanNetwork pvn = req.getProviderVlanNetwork();
                    if (pvn == null) {
                        pvn = new ProviderVlanNetwork();
                        req.setProviderVlanNetwork(pvn);
                    }
                    if (params.containsKey(SHARED))
                        shared = params.get(SHARED);
                    if (params.containsKey(EXTERNAL))
                        external = params.get(EXTERNAL);
                    adapter.createNetwork(req.getCloudSiteId(), req.getTenantId(), req.getNetworkType(),
                            req.getModelCustomizationUuid(), req.getNetworkName(),
                            req.getProviderVlanNetwork().getPhysicalNetworkName(),
                            req.getProviderVlanNetwork().getVlans(), shared, external, req.getFailIfExists(),
                            req.getBackout(), req.getSubnets(), params, req.getMsoRequest(), networkId,
                            neutronNetworkId, subnetIdMap, rollback);
                }
                response = new CreateNetworkResponse(req.getNetworkId(), neutronNetworkId.value,
                        rollback.value.getNetworkStackId(), networkFqdn.value, rollback.value.getNetworkCreated(),
                        subnetIdMap.value, rollback.value, req.getMessageId());
            } catch (NetworkException e) {
                logger.debug(exceptionMsg, e);
                eresp = new CreateNetworkError(e.getMessage(), MsoExceptionCategory.INTERNAL, true, req.getMessageId());
            }
            if (!req.isSynchronous()) {
                // This is asynch, so POST response back to caller
                BpelRestClient bpelClient = bpelRestClientProvider.get();
                bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
            }
            logger.debug("CreateNetworkTask exit: code={}, resp={}", getStatusCode(), getResponse());
        }
    }

    @DELETE
    @Path("{aaiNetworkId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "DeleteNetwork", response = Response.class,
            notes = "Deletes an existing network, aaiNetworkId and DeleteNetworkRequest JSON are required")
    @ApiResponses({@ApiResponse(code = 200, message = "network has been successfully deleted"),
            @ApiResponse(code = 202, message = "request to delete network has been accepted (async only)"),
            @ApiResponse(code = 500, message = "delete network failed, examine entity object for details")})
    public Response deleteNetwork(
            @ApiParam(value = "aaiNetworkId to be deleted ",
                    required = true) @PathParam("aaiNetworkId") String aaiNetworkId,
            @ApiParam(value = "details of network being deleted", required = true) DeleteNetworkRequest req) {
        logger.debug("deleteNetwork enter: {}", req.toJsonString());
        if (aaiNetworkId == null || !aaiNetworkId.equals(req.getNetworkId())) {
            return Response.status(HttpStatus.SC_BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(
                    "A&AI NetworkId in URL (" + aaiNetworkId + ") does not match content (" + req.getNetworkId() + ")")
                    .build();
        }
        DeleteNetworkTask task = new DeleteNetworkTask(req);
        if (req.isSynchronous()) {
            // This is a synchronous request
            task.run();
            return Response.status(task.getStatusCode()).entity(task.getGenericEntityResponse()).build();
        } else {
            // This is an asynchronous request
            try {
                Thread t1 = new Thread(task);
                t1.start();
            } catch (Exception e) {
                // problem handling create, send generic failure as sync resp to caller
                logger.error("{} {} Exception while delete network ", MessageEnum.RA_DELETE_NETWORK_EXC,
                        ErrorCode.BusinessProcessError.getValue(), e);
                return Response.serverError().build();
            }
            // send sync response (ACK) to caller
            logger.debug("deleteNetwork exit");
            return Response.status(HttpStatus.SC_ACCEPTED).build();
        }
    }

    public class DeleteNetworkTask implements Runnable {
        private final DeleteNetworkRequest req;
        private DeleteNetworkResponse response = null;
        private DeleteNetworkError eresp = null;
        private boolean sendxml;

        public DeleteNetworkTask(DeleteNetworkRequest req) {
            this.req = req;
            this.sendxml = true; // can be set with a field or header later
        }

        public int getStatusCode() {
            return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
        }

        public Object getGenericEntityResponse() {
            return (response != null) ? new GenericEntity<DeleteNetworkResponse>(response) {}
                    : new GenericEntity<DeleteNetworkError>(eresp) {};
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
                MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, req.getMsoRequest().getRequestId());
            } catch (Exception e) {
                logger.error("Error adding RequestId to MDC", e);
            }
            logger.debug("DeleteNetworkTask start");
            try {
                Holder<Boolean> networkDeleted = new Holder<>();
                if (req.getCloudSiteId().equals(TESTING_KEYWORD)) {
                    networkDeleted.value = true;
                } else {
                    adapter.deleteNetwork(req.getCloudSiteId(), req.getTenantId(), req.getNetworkType(),
                            req.getModelCustomizationUuid(), req.getNetworkStackId(), req.getMsoRequest(),
                            networkDeleted);
                }
                response = new DeleteNetworkResponse(req.getNetworkId(), networkDeleted.value, req.getMessageId());
            } catch (NetworkException e) {
                logger.debug(exceptionMsg, e);
                eresp = new DeleteNetworkError(e.getMessage(), MsoExceptionCategory.INTERNAL, true, req.getMessageId());
            }
            if (!req.isSynchronous()) {
                // This is asynch, so POST response back to caller
                BpelRestClient bpelClient = bpelRestClientProvider.get();
                bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
            }
            logger.debug("DeleteNetworkTask exit: code={}, resp={}", getStatusCode(), getResponse());
        }
    }

    @GET
    @Path("{aaiNetworkId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "QueryNetwork", response = Response.class, notes = "Queries an existing network")
    @ApiResponses({@ApiResponse(code = 200, message = "Query network successful"),
            @ApiResponse(code = 500, message = "Query network failed, examine entity object for details")})
    public Response queryNetwork(
            @ApiParam(value = "cloudSiteId", required = false) @QueryParam("cloudSiteId") String cloudSiteId,
            @ApiParam(value = "tenantId", required = false) @QueryParam("tenantId") String tenantId,
            @ApiParam(value = "networkStackId", required = false) @QueryParam("networkStackId") String networkStackId,
            @ApiParam(value = "skipAAI", required = false) @QueryParam("skipAAI") String skipAAI,
            @ApiParam(value = "msoRequest.requestId",
                    required = false) @QueryParam("msoRequest.requestId") String requestId,
            @ApiParam(value = "msoRequest.serviceInstanceId",
                    required = false) @QueryParam("msoRequest.serviceInstanceId") String serviceInstanceId,
            @ApiParam(value = "aaiNetworkId", required = false) @PathParam("aaiNetworkId") String aaiNetworkId) {
        // This request responds synchronously only
        logger.debug("Query network enter:{}" + aaiNetworkId);
        MsoRequest msoRequest = new MsoRequest(requestId, serviceInstanceId);

        try {
            int respStatus = HttpStatus.SC_OK;
            QueryNetworkResponse resp = new QueryNetworkResponse(networkStackId, null, networkStackId, null, null);
            Holder<Boolean> networkExists = new Holder<>();
            Holder<String> networkId = new Holder<>();
            Holder<String> neutronNetworkId = new Holder<>();
            Holder<NetworkStatus> status = new Holder<>();
            Holder<List<RouteTarget>> routeTargets = new Holder<>();
            Holder<Map<String, String>> subnetIdMap = new Holder<>();

            adapter.queryNetworkContrail(cloudSiteId, tenantId, aaiNetworkId, msoRequest, networkExists, networkId,
                    neutronNetworkId, status, routeTargets, subnetIdMap);

            if (!networkExists.value) {
                logger.debug("network not found");
                respStatus = HttpStatus.SC_NOT_FOUND;
            } else {
                logger.debug("network found {}, status={}", networkId.value, status.value);
                resp.setNetworkExists(networkExists.value);
                resp.setNetworkId(networkId.value);
                resp.setNeutronNetworkId(neutronNetworkId.value);
                resp.setNetworkStatus(status.value);
                resp.setRouteTargets(routeTargets.value);
                resp.setSubnetIdMap(subnetIdMap.value);
            }
            logger.debug("Query network exit");
            return Response.status(respStatus).entity(new GenericEntity<QueryNetworkResponse>(resp) {}).build();
        } catch (NetworkException e) {
            logger.error("{} {} Exception when query VNF ", MessageEnum.RA_QUERY_VNF_ERR,
                    ErrorCode.BusinessProcessError.getValue(), e);
            QueryNetworkError err = new QueryNetworkError();
            err.setMessage(e.getMessage());
            err.setCategory(MsoExceptionCategory.INTERNAL);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity(new GenericEntity<QueryNetworkError>(err) {}).build();
        }
    }

    @DELETE
    @Path("{aaiNetworkId}/rollback")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "RollbackNetwork", response = Response.class, notes = "Rollback an existing network")
    @ApiResponses({@ApiResponse(code = 200, message = "Rollback network successful"),
            @ApiResponse(code = 202, message = "Rollback network request has been accepted (async only)"),
            @ApiResponse(code = 500, message = "Rollback network failed, examine entity object for details")})
    public Response rollbackNetwork(
            @ApiParam(value = "RollbackNetworkRequest in JSON format", required = true) RollbackNetworkRequest req) {
        logger.debug("rollbackNetwork enter: {}", req.toJsonString());
        RollbackNetworkTask task = new RollbackNetworkTask(req);
        if (req.isSynchronous()) {
            // This is a synchronous request
            task.run();
            return Response.status(task.getStatusCode()).entity(task.getGenericEntityResponse()).build();
        } else {
            // This is an asynchronous request
            try {
                Thread t1 = new Thread(task);
                t1.start();
            } catch (Exception e) {
                // problem handling create, send generic failure as sync resp to caller
                logger.error("{} {} Exception in rollbackNetwork ", MessageEnum.RA_ROLLBACK_NULL,
                        ErrorCode.BusinessProcessError.getValue(), e);
                return Response.serverError().build();
            }
            // send sync response (ACK) to caller
            logger.debug("rollbackNetwork exit");
            return Response.status(HttpStatus.SC_ACCEPTED).build();
        }
    }

    public class RollbackNetworkTask implements Runnable {
        private final RollbackNetworkRequest req;
        private RollbackNetworkResponse response = null;
        private RollbackNetworkError eresp = null;
        private boolean sendxml;

        public RollbackNetworkTask(RollbackNetworkRequest req) {
            this.req = req;
            this.sendxml = true; // can be set with a field or header later
        }

        public int getStatusCode() {
            return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
        }

        public Object getGenericEntityResponse() {
            return (response != null) ? new GenericEntity<RollbackNetworkResponse>(response) {}
                    : new GenericEntity<RollbackNetworkError>(eresp) {};
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
                MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, req.getNetworkRollback().getMsoRequest().getRequestId());
            } catch (Exception e) {
                logger.error("Error adding RequestId to MDC", e);
            }
            logger.debug("RollbackNetworkTask start");
            try {
                NetworkRollback nwr = req.getNetworkRollback();
                adapter.rollbackNetwork(nwr);
                response = new RollbackNetworkResponse(true, req.getMessageId());
            } catch (NetworkException e) {
                logger.debug(exceptionMsg, e);
                eresp = new RollbackNetworkError(e.getMessage(), MsoExceptionCategory.INTERNAL, true,
                        req.getMessageId());
            }
            if (!req.isSynchronous()) {
                // This is asynch, so POST response back to caller
                BpelRestClient bpelClient = bpelRestClientProvider.get();
                bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
            }
            logger.debug("RollbackNetworkTask exit: code={}, resp={}", getStatusCode(), getResponse());
        }
    }

    @PUT
    @Path("{aaiNetworkId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "UpdateNetwork", response = Response.class, notes = "Update an existing network")
    @ApiResponses({@ApiResponse(code = 200, message = "Update network successful"),
            @ApiResponse(code = 202, message = "Update network request has been accepted (async only)"),
            @ApiResponse(code = 500, message = "Update network failed, examine entity object for details")})
    public Response updateNetwork(
            @ApiParam(value = "aaiNetworkId", required = true) @PathParam("aaiNetworkId") String aaiNetworkId,
            @ApiParam(value = "UpdateNetworkRequest in JSON format", required = true) UpdateNetworkRequest req) {
        logger.debug("updateNetwork enter: {}", req.toJsonString());
        if (aaiNetworkId == null || !aaiNetworkId.equals(req.getNetworkId())) {
            return Response.status(HttpStatus.SC_BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(
                    "A&AI NetworkId in URL (" + aaiNetworkId + ") does not match content (" + req.getNetworkId() + ")")
                    .build();
        }
        UpdateNetworkTask task = new UpdateNetworkTask(req);
        if (req.isSynchronous()) {
            // This is a synchronous request
            task.run();
            return Response.status(task.getStatusCode()).entity(task.getGenericEntityResponse()).build();
        } else {
            // This is an asynchronous request
            try {
                Thread t1 = new Thread(task);
                t1.start();
            } catch (Exception e) {
                // problem handling create, send generic failure as sync resp to caller
                logger.error("{} {} Exception in updateNetwork ", MessageEnum.RA_UPDATE_NETWORK_ERR,
                        ErrorCode.BusinessProcessError.getValue(), e);
                return Response.serverError().build();
            }
            // send sync response (ACK) to caller
            logger.debug("updateNetwork exit");
            return Response.status(HttpStatus.SC_ACCEPTED).build();
        }
    }

    public class UpdateNetworkTask implements Runnable {
        private final UpdateNetworkRequest req;
        private UpdateNetworkResponse response = null;
        private UpdateNetworkError eresp = null;
        private boolean sendxml;

        public UpdateNetworkTask(UpdateNetworkRequest req) {
            this.req = req;
            this.sendxml = true; // can be set with a field or header later
        }

        public int getStatusCode() {
            return (response != null) ? HttpStatus.SC_OK : HttpStatus.SC_BAD_REQUEST;
        }

        public Object getGenericEntityResponse() {
            return (response != null) ? new GenericEntity<UpdateNetworkResponse>(response) {}
                    : new GenericEntity<UpdateNetworkError>(eresp) {};
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
                MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, req.getMsoRequest().getRequestId());
            } catch (Exception e) {
                logger.error("Error adding RequestId to MDC", e);
            }
            logger.debug("UpdateNetworkTask start");
            try {
                Holder<Map<String, String>> subnetIdMap = new Holder<>();
                Holder<NetworkRollback> rollback = new Holder<>();
                HashMap<String, String> params = (HashMap<String, String>) req.getNetworkParams();
                if (params == null) {
                    params = new HashMap<String, String>();
                }
                String shared = null;
                String external = null;

                if (req.getCloudSiteId().equals(TESTING_KEYWORD)) {
                    subnetIdMap.value = testMap();
                    NetworkRollback rb = new NetworkRollback();
                    rb.setCloudId(req.getCloudSiteId());
                    rb.setTenantId(req.getTenantId());
                    rb.setMsoRequest(req.getMsoRequest());
                    rollback.value = rb;
                } else if (req.isContrailRequest()) {
                    ContrailNetwork ctn = req.getContrailNetwork();
                    if (ctn == null) {
                        ctn = new ContrailNetwork();
                        req.setContrailNetwork(ctn);
                    }
                    if (params.containsKey(SHARED)) {
                        shared = params.get(SHARED);
                    } else {
                        if (ctn.getShared() != null) {
                            shared = ctn.getShared();
                        }
                    }
                    if (params.containsKey(EXTERNAL)) {
                        external = params.get(EXTERNAL);
                    } else {
                        if (ctn.getExternal() != null) {
                            external = ctn.getExternal();
                        }
                    }
                    adapter.updateNetworkContrail(req.getCloudSiteId(), req.getTenantId(), req.getNetworkType(),
                            req.getModelCustomizationUuid(), req.getNetworkStackId(), req.getNetworkName(),
                            req.getContrailNetwork().getRouteTargets(), shared, external, req.getSubnets(), params,
                            req.getContrailNetwork().getPolicyFqdns(), req.getContrailNetwork().getRouteTableFqdns(),
                            req.getMsoRequest(), subnetIdMap, rollback);
                } else {
                    ProviderVlanNetwork pvn = req.getProviderVlanNetwork();
                    if (pvn == null) {
                        pvn = new ProviderVlanNetwork();
                        req.setProviderVlanNetwork(pvn);
                    }
                    if (params.containsKey(SHARED)) {
                        shared = params.get(SHARED);
                    }
                    if (params.containsKey(EXTERNAL)) {
                        external = params.get(EXTERNAL);
                    }
                    adapter.updateNetwork(req.getCloudSiteId(), req.getTenantId(), req.getNetworkType(),
                            req.getModelCustomizationUuid(), req.getNetworkStackId(), req.getNetworkName(),
                            req.getProviderVlanNetwork().getPhysicalNetworkName(),
                            req.getProviderVlanNetwork().getVlans(), shared, external, req.getSubnets(), params,
                            req.getMsoRequest(), subnetIdMap, rollback);
                }
                response = new UpdateNetworkResponse(req.getNetworkId(), null, // NeutronNetworkId is not available from
                                                                               // an update
                        subnetIdMap.value, req.getMessageId());
            } catch (NetworkException e) {
                logger.debug(exceptionMsg, e);
                eresp = new UpdateNetworkError(e.getMessage(), MsoExceptionCategory.INTERNAL, true, req.getMessageId());
            }
            if (!req.isSynchronous()) {
                // This is asynch, so POST response back to caller
                BpelRestClient bpelClient = bpelRestClientProvider.get();
                bpelClient.bpelPost(getResponse(), req.getNotificationUrl(), sendxml);
            }
            logger.debug("UpdateNetworkTask exit: code={}, resp={}", getStatusCode(), getResponse());
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
