/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.adapter.cnf;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.HttpStatus;
import org.onap.so.client.adapter.cnf.entities.InstanceRequest;
import org.onap.so.client.adapter.cnf.entities.InstanceResponse;
import org.onap.so.client.adapter.cnf.entities.UpgradeInstanceResponse;
import org.onap.so.client.adapter.cnf.entities.UpgradeInstanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CnfAdapterClient {

    private static final Logger logger = LoggerFactory.getLogger(CnfAdapterClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    private static final String INSTANCE_CREATE_PATH = "/api/cnf-adapter/v1/instance";

    @Retryable(value = {HttpServerErrorException.class}, maxAttempts = 3, backoff = @Backoff(delay = 3000))
    public InstanceResponse createVfModule(InstanceRequest request) throws CnfAdapterClientException {
        try {
            // String uri = env.getRequiredProperty("mso.cnf.adapter.endpoint"); //TODO: This needs to be added as well
            // for configuration
            String uri = "http://so-cnf-adapter:8090";
            String endpoint = UriBuilder.fromUri(uri).path(INSTANCE_CREATE_PATH).build().toString();
            HttpEntity<?> entity = getHttpEntity(request);
            ResponseEntity<InstanceResponse> result =
                    restTemplate.exchange(endpoint, HttpMethod.POST, entity, InstanceResponse.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling CNF Adapter, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }


    @Retryable(value = {HttpServerErrorException.class}, maxAttempts = 3, backoff = @Backoff(delay = 3000))
    public void deleteVfModule(String heatStackId) throws CnfAdapterClientException {
        try {
            // String uri = env.getRequiredProperty("mso.cnf.adapter.endpoint"); //TODO: This needs to be added as well
            // for configuration
            String uri = "http://so-cnf-adapter:8090";
            String endpoint = UriBuilder.fromUri(uri).path(INSTANCE_CREATE_PATH + "/" + heatStackId).build().toString();
            HttpEntity<?> entity = new HttpEntity<>(getHttpHeaders());
            restTemplate.exchange(endpoint, HttpMethod.DELETE, entity, String.class);
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling CNF Adapter, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    @Retryable(value = {HttpServerErrorException.class}, maxAttempts = 3, backoff = @Backoff(delay = 3000))
    public InstanceResponse healthcheck() throws CnfAdapterClientException {
        try {
            // String uri = env.getRequiredProperty("mso.cnf.adapter.endpoint"); //TODO: This needs to be added as well
            // for configuration
            String uri = "http://so-cnf-adapter:8090";
            String endpoint = UriBuilder.fromUri(uri).path("/api/cnf-adapter/v1/healthcheck").build().toString();
            HttpEntity<?> entity = new HttpEntity<>(getHttpHeaders());
            ResponseEntity<InstanceResponse> result =
                    restTemplate.exchange(endpoint, HttpMethod.GET, entity, InstanceResponse.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling CNF Adapter, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    @Retryable(value = {HttpServerErrorException.class}, maxAttempts = 3, backoff = @Backoff(delay = 3000))
    public UpgradeInstanceResponse upgradeVfModule(UpgradeInstanceRequest request, String heatStackId)
            throws CnfAdapterClientException {
        try {
            String uri = "http://so-cnf-adapter:8090";
            String endpoint = UriBuilder.fromUri(uri).path(INSTANCE_CREATE_PATH + "/" + heatStackId + "/upgrade")
                    .build().toString();
            HttpEntity<?> entity = getHttpEntity(request);
            ResponseEntity<UpgradeInstanceResponse> result =
                    restTemplate.exchange(endpoint, HttpMethod.POST, entity, UpgradeInstanceResponse.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling CNF Adapter, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    protected HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        /*
         * try { String userCredentials = CryptoUtils.decrypt(env.getRequiredProperty("mso.cnf.adapter.auth"),
         * env.getRequiredProperty("mso.msoKey")); if (userCredentials != null) { headers.add(HttpHeaders.AUTHORIZATION,
         * "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes())); } } catch
         * (GeneralSecurityException e) { logger.error("Security exception", e); }
         */
        return headers;
    }

    protected HttpEntity<?> getHttpEntity(InstanceRequest request) {
        HttpHeaders headers = getHttpHeaders();
        return new HttpEntity<>(request, headers);
    }

    protected HttpEntity<?> getHttpEntity(UpgradeInstanceRequest request) {
        HttpHeaders headers = getHttpHeaders();
        return new HttpEntity<>(request, headers);
    }
}
