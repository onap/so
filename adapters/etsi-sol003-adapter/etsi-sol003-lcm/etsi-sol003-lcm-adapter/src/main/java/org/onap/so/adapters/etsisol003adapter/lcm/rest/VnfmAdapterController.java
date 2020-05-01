/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.etsisol003adapter.lcm.rest;

import static org.onap.so.adapters.etsi.sol003.adapter.common.CommonConstants.BASE_URL;
import javax.validation.Valid;
import javax.ws.rs.core.MediaType;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.adapters.etsisol003adapter.lcm.jobmanagement.JobManager;
import org.onap.so.adapters.etsisol003adapter.lcm.lifecycle.LifecycleManager;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.DeleteVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.QueryJobResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.annotations.ApiParam;

/**
 * Controller for handling requests to the VNFM (Virtual Network Function Manager) adapter REST API.
 */
@Controller
@RequestMapping(value = BASE_URL, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
        consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class VnfmAdapterController {

    private static final Logger logger = LoggerFactory.getLogger(VnfmAdapterController.class);
    private final LifecycleManager lifecycleManager;
    private final JobManager jobManager;

    @Autowired
    VnfmAdapterController(final LifecycleManager lifecycleManager, final JobManager jobManager) {
        this.lifecycleManager = lifecycleManager;
        this.jobManager = jobManager;
    }

    @PostMapping(value = "/vnfs/{vnfId}")
    public ResponseEntity<CreateVnfResponse> vnfCreate(
            @ApiParam(value = "The identifier of the VNF. This must be the vnf-id of an existing generic-vnf in AAI.",
                    required = true) @PathVariable("vnfId") final String vnfId,
            @ApiParam(value = "VNF creation parameters",
                    required = true) @Valid @RequestBody final CreateVnfRequest createVnfRequest,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies a single top level invocation of ONAP",
                    required = false) @RequestHeader(value = ONAPLogConstants.Headers.REQUEST_ID,
                            required = false) final String requestId,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies the client application user agent or user invoking the API",
                    required = false) @RequestHeader(value = ONAPLogConstants.Headers.PARTNER_NAME,
                            required = false) final String partnerName,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies a single invocation of a single component",
                    required = false) @RequestHeader(value = ONAPLogConstants.Headers.INVOCATION_ID,
                            required = false) final String invocationId) {

        setLoggingMDCs(requestId, partnerName, invocationId);

        logger.info("REST request vnfCreate with body: {}", createVnfRequest);

        try {
            final CreateVnfResponse createVnfResponse = lifecycleManager.createVnf(vnfId, createVnfRequest);
            return new ResponseEntity<>(createVnfResponse, HttpStatus.ACCEPTED);
        } finally {
            clearLoggingMDCs();
        }
    }

    @DeleteMapping(value = "/vnfs/{vnfId}")
    public ResponseEntity<DeleteVnfResponse> vnfDelete(
            @ApiParam(value = "The identifier of the VNF. This must be the vnf-id of an existing generic-vnf in AAI.",
                    required = true) @PathVariable("vnfId") final String vnfId,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies a single top level invocation of ONAP",
                    required = false) @RequestHeader(value = ONAPLogConstants.Headers.REQUEST_ID,
                            required = false) final String requestId,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies the client application user agent or user invoking the API",
                    required = false) @RequestHeader(value = ONAPLogConstants.Headers.PARTNER_NAME,
                            required = false) final String partnerName,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies a single invocation of a single component",
                    required = false) @RequestHeader(value = ONAPLogConstants.Headers.INVOCATION_ID,
                            required = false) final String invocationId) {

        setLoggingMDCs(requestId, partnerName, invocationId);

        logger.info("REST request vnfDelete for VNF: {}", vnfId);

        try {
            final DeleteVnfResponse response = lifecycleManager.deleteVnf(vnfId);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } finally {
            clearLoggingMDCs();
        }
    }

    @GetMapping(value = "/jobs/{jobId}")
    public ResponseEntity<QueryJobResponse> jobQuery(
            @ApiParam(value = "The identifier of the Job.", required = true) @PathVariable("jobId") final String jobId,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies a single top level invocation of ONAP",
                    required = false) @RequestHeader(value = ONAPLogConstants.Headers.REQUEST_ID,
                            required = false) final String requestId,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies the client application user agent or user invoking the API",
                    required = false) @RequestHeader(value = ONAPLogConstants.Headers.PARTNER_NAME,
                            required = false) final String partnerName,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies a single invocation of a single component",
                    required = false) @RequestHeader(value = ONAPLogConstants.Headers.INVOCATION_ID,
                            required = false) final String invocationId) {

        setLoggingMDCs(requestId, partnerName, invocationId);

        try {
            final QueryJobResponse response = jobManager.getVnfmOperation(jobId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } finally {
            clearLoggingMDCs();
        }
    }

    private void setLoggingMDCs(final String requestId, final String partnerName, final String invocationId) {
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, partnerName);
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
    }

    private void clearLoggingMDCs() {
        MDC.clear();
    }

}
