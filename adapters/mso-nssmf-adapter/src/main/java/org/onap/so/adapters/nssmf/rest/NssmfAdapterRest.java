/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.nssmf.rest;

import org.onap.so.adapters.nssmf.entity.RestResponse;
import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.beans.nsmf.JobStatusRequest;
import org.onap.so.beans.nsmf.NssiActDeActRequest;
import org.onap.so.beans.nsmf.NssiAllocateRequest;
import org.onap.so.beans.nsmf.NssiCreateRequest;
import org.onap.so.beans.nsmf.NssiDeAllocateRequest;
import org.onap.so.beans.nsmf.NssiTerminateRequest;
import org.onap.so.beans.nsmf.NssiUpdateRequest;
import org.onap.so.beans.nsmf.NssiUpdateRequestById;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.assertObjectNotNull;

@Deprecated
@Controller
@RequestMapping(value = "/api/rest/provMns/v1", produces = {APPLICATION_JSON}, consumes = {APPLICATION_JSON})
public class NssmfAdapterRest {

    private static final Logger logger = LoggerFactory.getLogger(NssmfAdapterRest.class);

    @Autowired
    private NssmfManager nssmfMgr;


    @PostMapping(value = "/NSS/nssi")
    public ResponseEntity createNssi(@RequestBody NssiCreateRequest create) {
        try {
            logger.info("Nssmf create request is invoked");
            assertObjectNotNull(create);
            RestResponse rsp = getNssmfMgr().createNssi(create);
            return buildResponse(rsp);
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    @PostMapping(value = "/NSS/nssi/{nssiId}")
    public ResponseEntity terminateNssi(@RequestBody NssiTerminateRequest terminate,
            @PathVariable("nssiId") String nssiId) {
        try {
            logger.info("Nssmf terminate request is invoked");
            assertObjectNotNull(terminate);
            RestResponse rsp = getNssmfMgr().terminateNssi(terminate, nssiId);
            return buildResponse(rsp);
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    @PutMapping(value = "/NSS/SliceProfiles/{sliceProfileId}")
    public ResponseEntity modifyNssi(@RequestBody NssiUpdateRequest update,
            @PathVariable("sliceProfileId") String sliceId) {
        try {
            logger.info("Nssmf modify request is invoked");
            assertObjectNotNull(update);
            RestResponse rsp = getNssmfMgr().updateNssi(update, sliceId);
            return buildResponse(rsp);
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    @PutMapping(value = "/NSS/nssi/{nssiId}")
    public ResponseEntity modifyNssiById(@RequestBody NssiUpdateRequestById updateById,
            @PathVariable("nssiId") String nssiId) {
        try {
            logger.info("Nssmf modify by ID request is invoked");
            assertObjectNotNull(updateById);
            RestResponse rsp = getNssmfMgr().updateNssiById(updateById, nssiId);
            return buildResponse(rsp);
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }


    @GetMapping(value = "/vendor/{vendorName}/type/{networkType}/NSS" + "/SliceProfiles/{sliceProfileId}")
    public ResponseEntity queryNssi(@PathVariable("vendorName") String vendorName,
            @PathVariable("networktype") String networkType, @PathVariable("sliceProfileId") String sliceId) {
        try {
            logger.info("Nssmf query nssi request is invoked");
            RestResponse rsp = getNssmfMgr().queryNssi(vendorName, networkType, sliceId);
            return buildResponse(rsp);
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    @GetMapping(value = "/vendor/{vendorName}/type/{networkType}/NSS/nssi" + "/{nssiId}")
    public ResponseEntity queryNssiById(@PathVariable("vendorName") String vendorName,
            @PathVariable("networkTtype") String networkType, @PathVariable("nssiId") String nssiId) {
        try {
            logger.info("Nssmf query nssi by ID request is invoked");
            RestResponse rsp = getNssmfMgr().queryNssiById(vendorName, networkType, nssiId);
            return buildResponse(rsp);
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    public void setNssmfMgr(NssmfManager nssmfMgr) {
        this.nssmfMgr = nssmfMgr;
    }

    public NssmfManager getNssmfMgr() {
        return nssmfMgr;
    }

    private ResponseEntity buildResponse(RestResponse rsp) {
        return ResponseEntity.status(rsp.getStatus()).body(rsp.getResponseContent());
    }
}
