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

package org.onap.so.adapters.vnfmadapter.rest;

import static org.onap.so.adapters.vnfmadapter.Constants.BASE_URL;
import java.util.UUID;
import javax.validation.Valid;
import javax.ws.rs.core.MediaType;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.vnfmadapter.v1.model.CreateVnfRequest;
import org.onap.vnfmadapter.v1.model.CreateVnfResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

    @PostMapping(value = "/vnfs/{vnfId}")
    public ResponseEntity<CreateVnfResponse> vnfCreate(
            @ApiParam(value = "The identifier of the VNF. This must be the vnf-id of an existing generic-vnf in AAI.",
                    required = true) @PathVariable("vnfId") final String vnfId,
            @ApiParam(value = "VNF creation parameters",
                    required = true) @Valid @RequestBody final CreateVnfRequest createVnfRequest,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies a single top level invocation of ONAP",
                    required = true) @RequestHeader(value = ONAPLogConstants.Headers.REQUEST_ID,
                            required = false) final String requestId,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies the client application user agent or user invoking the API",
                    required = true) @RequestHeader(value = ONAPLogConstants.Headers.PARTNER_NAME,
                            required = false) final String partnerName,
            @ApiParam(
                    value = "Used to track REST requests for logging purposes. Identifies a single invocation of a single component",
                    required = true) @RequestHeader(value = ONAPLogConstants.Headers.INVOCATION_ID,
                            required = false) final String invocationId) {

        setLoggingMDCs(requestId, partnerName, invocationId);

        logger.info("REST request vnfCreate with body: {}", createVnfRequest);

        final CreateVnfResponse response = new CreateVnfResponse();
        response.setJobId(UUID.randomUUID().toString());
        clearLoggingMDCs();
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
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
