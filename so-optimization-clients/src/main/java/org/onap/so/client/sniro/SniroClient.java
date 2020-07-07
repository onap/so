/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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

package org.onap.so.client.sniro;

import java.util.LinkedHashMap;
import org.onap.so.client.BaseClient;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.sniro.beans.ConductorProperties;
import org.onap.so.client.sniro.beans.ManagerProperties;
import org.onap.so.client.sniro.beans.SniroConductorRequest;
import org.onap.so.client.sniro.beans.SniroManagerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;


@Component
public class SniroClient {

    private static final Logger logger = LoggerFactory.getLogger(SniroClient.class);

    @Autowired
    private ManagerProperties managerProperties;

    @Autowired
    private SniroValidator validator;


    /**
     * Makes a rest call to sniro manager to perform homing and licensing for a list of demands
     *
     * @param homingRequest
     * @return
     * @throws BadResponseException
     */
    public void postDemands(SniroManagerRequest homingRequest) throws BadResponseException {
        logger.trace("Started Sniro Client Post Demands");
        String url = managerProperties.getHost() + managerProperties.getUri().get("v2");
        logger.debug("Post demands url: {}", url);
        logger.debug("Post demands payload: {}", homingRequest.toJsonString());

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", managerProperties.getHeaders().get("auth"));
        header.set("X-patchVersion", managerProperties.getHeaders().get("patchVersion"));
        header.set("X-minorVersion", managerProperties.getHeaders().get("minorVersion"));
        header.set("X-latestVersion", managerProperties.getHeaders().get("latestVersion"));
        BaseClient<String, LinkedHashMap<String, Object>> baseClient = new BaseClient<>();

        baseClient.setTargetUrl(url);
        baseClient.setHttpHeader(header);

        LinkedHashMap<String, Object> response = baseClient.post(homingRequest.toJsonString(),
                new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {});
        validator.validateDemandsResponse(response);
        logger.trace("Completed Sniro Client Post Demands");
    }

    /**
     * Makes a rest call to sniro conductor to notify them of successful or unsuccessful vnf creation for previously
     * homed resources
     *
     * TODO Temporarily being used in groovy therefore can not utilize autowire. Once java "release" subflow is
     * developed it will be refactored to use autowire.
     *
     * @param releaseRequest
     * @return
     * @throws BadResponseException
     */
    public void postRelease(SniroConductorRequest releaseRequest) throws BadResponseException {
        logger.trace("Started Sniro Client Post Release");
        String url = ConductorProperties.getHost() + ConductorProperties.getUri();
        logger.debug("Post release url: {}", url);
        logger.debug("Post release payload: {}", releaseRequest.toJsonString());

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", ConductorProperties.getAuth());
        BaseClient<String, LinkedHashMap<String, Object>> baseClient = new BaseClient<>();

        baseClient.setTargetUrl(url);
        baseClient.setHttpHeader(header);

        LinkedHashMap<String, Object> response = baseClient.post(releaseRequest.toJsonString(),
                new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {});
        SniroValidator v = new SniroValidator();
        v.validateReleaseResponse(response);
        logger.trace("Completed Sniro Client Post Release");
    }

}
