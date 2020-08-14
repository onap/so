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

import static org.onap.so.etsi.nfvo.ns.lcm.Constants.NS_LIFE_CYCLE_MANAGEMENT_BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import javax.ws.rs.core.MediaType;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsLcmOpOccsNsLcmOpOcc;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for handling NS lifecycle management operation occurrence requests see clause 6.4.9 and 6.4.10 in
 * https://www.etsi.org/deliver/etsi_gs/NFV-SOL/001_099/005/02.07.01_60/gs_NFV-SOL005v020701p.pdf
 * 
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Controller
@RequestMapping(value = NS_LIFE_CYCLE_MANAGEMENT_BASE_URL)
public class NsLcmOperationOccurrencesController {
    private static final Logger logger = getLogger(NsLcmOperationOccurrencesController.class);


    /**
     * The GET method to retrieve status information about a NS lifecycle management operation occurrence by reading an
     * individual "NS LCM operation occurrence" resource.
     * 
     * @param nsLcmOpOccId Identifier of a NS lifecycle management operation occurrence
     * @return "200 OK" with {@link NsLcmOpOccsNsLcmOpOcc NsLcmOpOcc} Information about a NS LCM operation occurrence
     *         was queried successfully. The response body shall contain status information about a NS lifecycle
     *         management operation occurrence (see clause 6.5.2.3).
     */
    @GetMapping(value = "/ns_lcm_op_occs/{nsLcmOpOccId}",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<?> getOperationStatus(@PathVariable("nsLcmOpOccId") final String nsLcmOpOccId) {
        logger.info("Received request to retrieve operation status for nsLcmOpOccId: {}", nsLcmOpOccId);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Operation is not supported yet");
    }

}
