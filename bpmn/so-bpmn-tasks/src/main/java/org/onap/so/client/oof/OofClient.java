/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp.  All rights reserved.
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

package org.onap.so.client.oof;


import org.camunda.bpm.engine.delegate.BpmnError;
import org.onap.so.bpmn.common.baseclient.BaseClient;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.oof.beans.OofProperties;
import org.onap.so.client.oof.beans.OofRequest;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.LinkedHashMap;

@Component
public class OofClient {

    private static final MsoLogger log = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, OofClient.class);

    @Autowired
    private OofProperties oofProperties;

    @Autowired
    private OofValidator validator;


    /**
     * Makes a rest call to oof to perform homing and licensing for a
     * list of demands
     *
     * @param homingRequest
     * @return
     * @throws JsonProcessingException
     * @throws BpmnError
     */
    public void postDemands(OofRequest homingRequest) throws BadResponseException, JsonProcessingException{
        log.trace("Started oof Client Post Demands");
        String url = oofProperties.getHost() + oofProperties.getUri();
        log.debug("Post demands url: " + url);
        log.debug("Post demands payload: " + homingRequest.toJsonString());

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", oofProperties.getHeaders().get("auth"));
        header.set("X-patchVersion", oofProperties.getHeaders().get("patchVersion"));
        header.set("X-minorVersion", oofProperties.getHeaders().get("minorVersion"));
        header.set("X-latestVersion", oofProperties.getHeaders().get("latestVersion"));
        BaseClient<String, LinkedHashMap<?, ?>> baseClient = new BaseClient<>();

        baseClient.setTargetUrl(url);
        baseClient.setHttpHeader(header);

        LinkedHashMap<?, ?> response = baseClient.post(homingRequest.toJsonString(), new ParameterizedTypeReference<LinkedHashMap<? ,?>>() {});
        validator.validateDemandsResponse(response);
        log.trace("Completed OOF Client Post Demands");
    }
}
