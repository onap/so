/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
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

package org.openecomp.mso.adapters.vfc;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openecomp.mso.adapters.vfc.exceptions.ApplicationException;
import org.openecomp.mso.adapters.vfc.model.NSResourceInputParameter;
import org.openecomp.mso.adapters.vfc.model.NsOperationKey;
import org.openecomp.mso.adapters.vfc.model.RestfulResponse;
import org.openecomp.mso.adapters.vfc.util.JsonUtil;
import org.openecomp.mso.adapters.vfc.util.ValidateUtil;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * The rest class for VF-c Adapter <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-08-28
 */
@Path("/v1/vfcadapter")
public class VfcAdapterRest {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

    private final VfcManager driverMgr = new VfcManager();

    public VfcAdapterRest() {

    }

    /**
     * Create a NS <br>
     * 
     * @param data the http request
     * @return
     * @since ONAP Amsterdam Release
     */
    @POST
    @Path("/ns")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createNfvoNs(String data) {
        try {
            ValidateUtil.assertObjectNotNull(data);
            LOGGER.info(MessageEnum.RA_NS_EXC, "Create NS Request Received.Body from request is :\n" + data, "org.openecomp.mso.adapters.vfc.VfcAdapterRest", "VFC Adapter");
            NSResourceInputParameter nsInput = JsonUtil.unMarshal(data, NSResourceInputParameter.class);
            RestfulResponse rsp = driverMgr.createNs(nsInput);
            return buildResponse(rsp);
        } catch(ApplicationException e) {
            LOGGER.debug("ApplicationException: ", e);
            return e.buildErrorResponse();
        }
    }

    /**
     * Delete NS instance<br>
     *
     * @param servletReq http request
     * @return response
     * @since ONAP Amsterdam Release
     */
    @DELETE
    @Path("/ns/{nsInstanceId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteNfvoNs(String data, @PathParam("nsInstanceId") String nsInstanceId) {
        try {

            ValidateUtil.assertObjectNotNull(data);
            LOGGER.info(MessageEnum.RA_NS_EXC, "Delete NS Request Received.Body from request is :\n" + data, "org.openecomp.mso.adapters.vfc.VfcAdapterRest", "VFC Adapter");
            NsOperationKey nsOperationKey = JsonUtil.unMarshal(data, NsOperationKey.class);
            RestfulResponse rsp = driverMgr.deleteNs(nsOperationKey, nsInstanceId);
            return buildResponse(rsp);
        } catch(ApplicationException e) {
            LOGGER.debug("ApplicationException: ", e);
            return e.buildErrorResponse();
        }
    }

    /**
     * Query Operation job status <br>
     * 
     * @param servletReq The Http Request
     * @param jobId The job id
     * @return
     * @since ONAP Amsterdam Release
     */
    @POST
    @Path("/jobs/{jobId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response queryNfvoJobStatus(String data, @PathParam("jobId") String jobId) {
        try {
            ValidateUtil.assertObjectNotNull(data);
            LOGGER.info(MessageEnum.RA_NS_EXC, "Query Job Request Received.Body from request is :\n" + data, "org.openecomp.mso.adapters.vfc.VfcAdapterRest", "VFC Adapter");
            NsOperationKey nsOperationKey = JsonUtil.unMarshal(data, NsOperationKey.class);
            RestfulResponse rsp = driverMgr.getNsProgress(nsOperationKey, jobId);
            return buildResponse(rsp);
        } catch(ApplicationException e) {
            LOGGER.debug("ApplicationException: ", e);
            return e.buildErrorResponse();
        }
    }

    /**
     * Instantiate NS instance <br>
     * 
     * @param servletReq The http request
     * @param nsInstanceId The NS instance id
     * @return
     * @since ONAP Amsterdam Release
     */
    @POST
    @Path("/ns/{nsInstanceId}/instantiate")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response instantiateNfvoNs(String data, @PathParam("nsInstanceId") String nsInstanceId) {
        try {
            ValidateUtil.assertObjectNotNull(data);
            LOGGER.info(MessageEnum.RA_NS_EXC, "Instantiate Ns Request Received.Body from request is :\n" + data, "org.openecomp.mso.adapters.vfc.VfcAdapterRest", "VFC Adapter");
            NSResourceInputParameter nsInput = JsonUtil.unMarshal(data, NSResourceInputParameter.class);
            RestfulResponse rsp = driverMgr.instantiateNs(nsInstanceId, nsInput);
            return buildResponse(rsp);
        } catch(ApplicationException e) {
            LOGGER.debug("ApplicationException: ", e);
            return e.buildErrorResponse();
        }
    }

    /**
     * Terminate NS instance <br>
     * 
     * @param servletReq The http request
     * @param nsInstanceId The NS instance id
     * @return
     * @since ONAP Amsterdam Release
     */
    @POST
    @Path("/ns/{nsInstanceId}/terminate")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response terminateNfvoNs(String data, @PathParam("nsInstanceId") String nsInstanceId) {
        try {
            ValidateUtil.assertObjectNotNull(data);
            LOGGER.info(MessageEnum.RA_NS_EXC, "Terminate Ns Request Received.Body from request is :\n" + data, "org.openecomp.mso.adapters.vfc.VfcAdapterRest", "VFC Adapter");
            NsOperationKey nsOperationKey = JsonUtil.unMarshal(data, NsOperationKey.class);
            RestfulResponse rsp = driverMgr.terminateNs(nsOperationKey, nsInstanceId);
            return buildResponse(rsp);
        } catch(ApplicationException e) {
            LOGGER.debug("ApplicationException: ", e);
            return e.buildErrorResponse();
        }
    }

    /**
     * build response from restful response <br>
     * 
     * @param rsp general response object
     * @return
     * @since ONAP Amsterdam Release
     */
    private Response buildResponse(RestfulResponse rsp) {
        return Response.status(rsp.getStatus()).entity(rsp.getResponseContent()).build();
    }
}
