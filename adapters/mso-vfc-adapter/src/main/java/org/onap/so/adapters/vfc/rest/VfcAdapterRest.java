/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
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

package org.onap.so.adapters.vfc.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.onap.so.adapters.vfc.exceptions.ApplicationException;
import org.onap.so.adapters.vfc.model.NSResourceInputParameter;
import org.onap.so.adapters.vfc.model.NsOperationKey;
import org.onap.so.adapters.vfc.model.RestfulResponse;
import org.onap.so.adapters.vfc.util.JsonUtil;
import org.onap.so.adapters.vfc.util.ValidateUtil;
import org.onap.so.db.request.beans.InstanceNfvoMapping;
import org.onap.so.db.request.data.repository.InstanceNfvoMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The rest class for VF-c Adapter <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-08-28
 */
@Component
@Path("/v1/vfcadapter")
public class VfcAdapterRest {

    private static final Logger logger = LoggerFactory.getLogger(VfcAdapterRest.class);
    private static final String REQUEST_DEBUG_MSG = "body from request is {}";
    private static final String APPLICATION_EXCEPTION = "ApplicationException: ";
    @Autowired
    private VfcManagerSol005 vfcManagerSol005;

    @Autowired
    private VfcManager driverMgr;
    @Autowired
    private InstanceNfvoMappingRepository instanceNfvoMappingRepository;

    public VfcAdapterRest() {

    }

    /**
     * Create a NS <br>
     * 
     * @param data http request
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
            logger.debug(REQUEST_DEBUG_MSG + data);
            NSResourceInputParameter nsInput = JsonUtil.unMarshal(data, NSResourceInputParameter.class);
            RestfulResponse rsp;
            if (nsInput.getNsParameters().getAdditionalParamForNs().containsKey("isSol005Interface")) {
                rsp = vfcManagerSol005.createNs(nsInput);
            } else {
                rsp = driverMgr.createNs(nsInput);
            }

            return buildResponse(rsp);
        } catch (ApplicationException e) {
            logger.debug(APPLICATION_EXCEPTION, e);
            return e.buildErrorResponse();
        }
    }

    /**
     * Delete NS instance<br>
     *
     * @param data The http request
     * @param nsInstanceId The NS instance id
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
            logger.debug(REQUEST_DEBUG_MSG + data);
            NsOperationKey nsOperationKey = JsonUtil.unMarshal(data, NsOperationKey.class);
            RestfulResponse rsp;
            InstanceNfvoMapping instanceNfvoMapping = instanceNfvoMappingRepository.findOneByInstanceId(nsInstanceId);
            if (instanceNfvoMapping != null) {
                rsp = vfcManagerSol005.deleteNs(nsOperationKey, nsInstanceId);
            } else {
                rsp = driverMgr.deleteNs(nsOperationKey, nsInstanceId);
            }
            return buildResponse(rsp);
        } catch (ApplicationException e) {
            logger.debug(APPLICATION_EXCEPTION, e);
            return e.buildErrorResponse();
        }
    }

    /**
     * Query Operation job status <br>
     * 
     * @param data The Http Request
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
            logger.debug(REQUEST_DEBUG_MSG + data);
            NsOperationKey nsOperationKey = JsonUtil.unMarshal(data, NsOperationKey.class);
            RestfulResponse rsp;
            InstanceNfvoMapping instanceNfvoMapping = instanceNfvoMappingRepository.findOneByJobId(jobId);
            if (instanceNfvoMapping != null) {
                rsp = vfcManagerSol005.getNsProgress(nsOperationKey, jobId);
            } else {
                rsp = driverMgr.getNsProgress(nsOperationKey, jobId);
            }
            return buildResponse(rsp);
        } catch (ApplicationException e) {
            logger.debug(APPLICATION_EXCEPTION, e);
            return e.buildErrorResponse();
        }
    }

    /**
     * Instantiate NS instance <br>
     * 
     * @param data The http request
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
            logger.debug(REQUEST_DEBUG_MSG + data);
            NSResourceInputParameter nsInput = JsonUtil.unMarshal(data, NSResourceInputParameter.class);
            RestfulResponse rsp;
            if (nsInput.getNsParameters().getAdditionalParamForNs().containsKey("isSol005Interface")) {
                rsp = vfcManagerSol005.instantiateNs(nsInstanceId, nsInput);
            } else {
                rsp = driverMgr.instantiateNs(nsInstanceId, nsInput);
            }
            return buildResponse(rsp);
        } catch (ApplicationException e) {
            logger.debug(APPLICATION_EXCEPTION, e);
            return e.buildErrorResponse();
        }
    }

    /**
     * Terminate NS instance <br>
     * 
     * @param data The http request
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
            logger.debug(REQUEST_DEBUG_MSG + data);
            NsOperationKey nsOperationKey = JsonUtil.unMarshal(data, NsOperationKey.class);
            RestfulResponse rsp;
            InstanceNfvoMapping instanceNfvoMapping = instanceNfvoMappingRepository.findOneByInstanceId(nsInstanceId);
            if (instanceNfvoMapping != null) {
                rsp = vfcManagerSol005.terminateNs(nsOperationKey, nsInstanceId);
            } else {
                rsp = driverMgr.terminateNs(nsOperationKey, nsInstanceId);
            }

            return buildResponse(rsp);
        } catch (ApplicationException e) {
            logger.debug(APPLICATION_EXCEPTION, e);
            return e.buildErrorResponse();
        }
    }

    /**
     * Scale NS instance <br>
     * 
     * @param servletReq The http request
     * @param nsInstanceId The NS instance id
     * @return
     * @since ONAP Amsterdam Release
     */
    @POST
    @Path("/ns/{nsInstanceId}/scale")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response scaleNfvoNs(String data, @PathParam("nsInstanceId") String nsInstanceId) {
        try {
            ValidateUtil.assertObjectNotNull(data);
            logger.debug("Scale Ns Request Received.Body from request is {}" + data);
            NSResourceInputParameter nsInput = JsonUtil.unMarshal(data, NSResourceInputParameter.class);
            RestfulResponse rsp = driverMgr.scaleNs(nsInstanceId, nsInput);
            return buildResponse(rsp);
        } catch (ApplicationException e) {
            logger.debug(APPLICATION_EXCEPTION, e);
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
