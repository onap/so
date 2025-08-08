/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.namingservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.onap.namingservice.model.NameGenDeleteRequest;
import org.onap.namingservice.model.NameGenDeleteResponse;
import org.onap.namingservice.model.NameGenRequest;
import org.onap.namingservice.model.NameGenResponse;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;



@Component
public class NamingClient {
    private static final Logger logger = LoggerFactory.getLogger(NamingClient.class);
    private static final String ENDPOINT = "mso.naming.endpoint";
    private static final String AUTH = "mso.naming.auth";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Environment env;
    @Autowired
    private NamingClientResponseValidator namingClientResponseValidator;

    public String postNameGenRequest(NameGenRequest request) throws BadResponseException, IOException {
        String targetUrl = env.getProperty(ENDPOINT);
        HttpHeaders headers = setHeaders(env.getProperty(AUTH));
        logger.info("Sending postNameGenRequest to url: {}", targetUrl);
        HttpEntity<NameGenRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<NameGenResponse> response;
        try {
            response = restTemplate.postForEntity(targetUrl, requestEntity, NameGenResponse.class);
        } catch (HttpStatusCodeException e) {
            throw new BadResponseException(namingClientResponseValidator.formatError(e));
        }
        return namingClientResponseValidator.validateNameGenResponse(response);
    }

    public String deleteNameGenRequest(NameGenDeleteRequest request) throws BadResponseException, IOException {
        String targetUrl = env.getProperty(ENDPOINT);
        HttpHeaders headers = setHeaders(env.getProperty(AUTH));
        logger.info("Sending deleteNameGenRequest to url: {}", targetUrl);
        HttpEntity<NameGenDeleteRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<NameGenDeleteResponse> response;
        try {
            response = restTemplate.exchange(targetUrl, HttpMethod.DELETE, requestEntity, NameGenDeleteResponse.class);
        } catch (HttpStatusCodeException e) {
            throw new BadResponseException(namingClientResponseValidator.formatError(e));
        }
        return namingClientResponseValidator.validateNameGenDeleteResponse(response);
    }

    private HttpHeaders setHeaders(String auth) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(acceptableMediaTypes);
        headers.add(HttpHeaders.AUTHORIZATION, auth);
        return headers;
    }
}
