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
import org.onap.so.adapters.oof.utils.OofUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * A generic call back handler to receive async response from OOF
 */
@RestController
@RequestMapping("/so/adapters/oof/callback/")
public class OofCallbackHandler {

    @Autowired
    OofUtils utils;

    @Autowired
    RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(OofCallbackHandler.class);

    @PostMapping("/{version:[vV][1]}/{messageEventName}/{correlator}")
    public ResponseEntity<String> processCallback(@PathVariable("messageEventName") String messageEventName,
            @PathVariable("correlator") String correlator, @RequestBody String oofCallbackRequest)
            throws OofAdapterException {
        logger.debug("Oof Async response received for event : {} , callback request body : {} ", messageEventName,
                oofCallbackRequest);
        String camundaMsgUrl = utils.getCamundaMsgUrl(messageEventName, correlator);
        HttpEntity<String> request = new HttpEntity<String>(oofCallbackRequest, utils.getCamundaHeaders());
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(camundaMsgUrl, request, String.class);
            logger.debug("Response from BPMN : {} ", response);
            return response;
        } catch (Exception e) {
            logger.warn("Error injecting message event into BPMN {} {} ", e.getCause(), e.getMessage());
            throw new OofAdapterException(e);
        }

    }

}
