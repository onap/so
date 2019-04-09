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

package org.onap.svnfm.simulator.controller;

import org.onap.svnfm.simulator.exception.InvalidRestRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * TO DO
 *
 * Implement Exception handling 
 * Implement VNFM adaptor call 
 * Identify the Create VNF response 
 * Test it with the VNFM Adaptor
 */

@RestController
@RequestMapping("/svnfm")
public class SvnfmHealthcheck {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SvnfmHealthcheck.class);

    @RequestMapping(method = RequestMethod.GET, value = "/healthcheck")
    public ResponseEntity<String> healthCheck() {
        try {
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (final InvalidRestRequestException extensions) {
            final String message = "Not Found";
            LOGGER.error(message);
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
    }
}
