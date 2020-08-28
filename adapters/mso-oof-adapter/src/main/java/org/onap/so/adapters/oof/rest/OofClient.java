/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited. All rights reserved.
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

package org.onap.so.adapters.oof.rest;

import org.onap.so.adapters.oof.exceptions.OofAdapterException;
import org.onap.so.adapters.oof.model.OofRequest;
import org.onap.so.adapters.oof.utils.OofUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * A generic client class to call OOF with request from BPMN
 */
@RestController
@RequestMapping("/so/adapters/oof/")
public class OofClient {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    OofUtils utils;

    private static final Logger logger = LoggerFactory.getLogger(OofClient.class);

    @PostMapping("/{version:[vV][1]}")
    public ResponseEntity<String> callOof(@RequestBody OofRequest oofRequest) throws OofAdapterException {
        try {
            logger.debug("Received Request from BPEL {} ", oofRequest);
            String oofUrl = utils.getOofurl(oofRequest.getApiPath());
            HttpEntity<?> request = new HttpEntity<>(oofRequest.getRequestDetails(), utils.getOofHttpHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(oofUrl, request, String.class);
            logger.debug("Response from OOF : {} ", response);
            return response;
        } catch (Exception e) {
            logger.warn("Error while calling OOF {} {} ", e.getCause(), e.getMessage());
            throw new OofAdapterException(e);
        }
    }

}
