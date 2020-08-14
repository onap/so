/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix. All rights reserved.
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
package org.onap.so.etsi.nfvo.ns.lcm.rest;

import static org.onap.so.etsi.nfvo.ns.lcm.Constants.HTTP_GLOBAL_CUSTOMER_ID_HTTP_HEADER_PARM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.Constants.HTTP_SERVICETYPE_HEADER_DEFAULT_VALUE;
import static org.onap.so.etsi.nfvo.ns.lcm.Constants.HTTP_SERVICETYPE_HEADER_PARM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.Constants.NS_LIFE_CYCLE_MANAGEMENT_BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import javax.ws.rs.core.MediaType;
import org.onap.so.etsi.nfvo.ns.lcm.model.Body;
import org.onap.so.etsi.nfvo.ns.lcm.model.CreateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.InstantiateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.TerminateNsRequest;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for handling the NS Lifecycle Management. For further information please read:
 * https://www.etsi.org/deliver/etsi_gs/NFV-SOL/001_099/005/02.07.01_60/gs_NFV-SOL005v020701p.pdf Use the section number
 * above each endpoint to find the corresponding section in the above document.
 * 
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Controller
@RequestMapping(value = NS_LIFE_CYCLE_MANAGEMENT_BASE_URL)
public class NsLifecycleManagementController {
    private static final Logger logger = getLogger(NsLifecycleManagementController.class);


    /**
     * The POST method creates new {@link Body new NS instance resource} request. See Section Number: 6.3.1 for more
     * detail
     * 
     * @param globalCustomerId The global customer ID
     * @param serviceType The service type
     * @param createNsRequest create network service request (see clause 6.5.2.9)
     * @return "201 Created" response containing a representation of the NS instance resource {@link NsInstance} just
     *         created by the NFVO, and provides the URI of the newly-created resource in the "Location:" HTTP header
     */
    @PostMapping(value = "/ns_instances", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<?> createNs(
            @RequestHeader(value = HTTP_GLOBAL_CUSTOMER_ID_HTTP_HEADER_PARM_NAME,
                    required = true) final String globalCustomerId,
            @RequestHeader(value = HTTP_SERVICETYPE_HEADER_PARM_NAME, required = false,
                    defaultValue = HTTP_SERVICETYPE_HEADER_DEFAULT_VALUE) final String serviceType,
            @RequestBody final CreateNsRequest createNsRequest) {
        logger.info("Received Create NS Request: {}\n with globalCustomerId: {}\n serviceType: {}\n", createNsRequest,
                globalCustomerId, serviceType);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Operation is not supported yet");
    }

    /**
     * The DELETE method delete NS instance
     * 
     * @param nsInstanceId Identifier of the NS instance to be deleted.
     * @return "202 Accepted" response with an empty entity body
     */
    @DeleteMapping(value = "/ns_instances/{nsInstanceId}",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<?> deleteNs(@PathVariable("nsInstanceId") final String nsInstanceId) {
        logger.debug("Received delete NS request for nsInstanceId: {}", nsInstanceId);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Operation is not supported yet");
    }

    /**
     * The POST method instantiate NS instance
     * 
     * @param nsInstanceId Identifier of the NS instance to be instantiated.
     * @param instantiateNsRequest Instantiate network service request (see clause 6.5.2.11)
     * @return "202 Accepted" response with an empty entity body and a "Location" HTTP header that points to the new "NS
     *         Lifecycle Operation Occurrence" resource
     */
    @PostMapping(value = "/ns_instances/{nsInstanceId}/instantiate",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<?> instantiateNs(@PathVariable("nsInstanceId") final String nsInstanceId,
            @RequestBody final InstantiateNsRequest instantiateNsRequest) {
        logger.debug("Received instantiate NS request: {}\n with nsInstanceId: {}", instantiateNsRequest, nsInstanceId);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Operation is not supported yet");
    }

    /**
     * The POST method terminate NS instance
     * 
     * @param nsInstanceId Identifier of the NS instance to be terminated.
     * @param terminateNsRequest The terminate NS request parameters (see clause 6.5.2.15)
     * @return "202 Accepted" response with an empty entity body and a "Location" HTTP header that points to the new "NS
     *         Lifecycle Operation Occurrence" resource
     */
    @PostMapping(value = "/ns_instances/{nsInstanceId}/terminate",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<?> terminateNs(@PathVariable("nsInstanceId") final String nsInstanceId,
            @RequestBody final TerminateNsRequest terminateNsRequest) {
        logger.debug("Received terminate NS request: {}\n with nsInstanceId: {}", terminateNsRequest, nsInstanceId);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Operation is not supported yet");
    }

}
