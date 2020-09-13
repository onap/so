/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.cnf.service;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.HttpStatus;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceMiniResponseList;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.InstanceStatusResponse;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class CnfAdapterService {
    private static final Logger logger = LoggerFactory.getLogger(CnfAdapterService.class);
    @Autowired
    private RestTemplate restTemplate;
    private static final String INSTANCE_CREATE_PATH = "/v1/instance";
    private static final String HEALTH_CHECK = "/v1/healthcheck";

    public String healthCheck() {

        try {

            logger.info("CnfAdapterService createInstance called");

            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String uri = "http://multicloud-k8s:9015"; // TODO: What is the correct uri?
            String endpoint = UriBuilder.fromUri(uri).path(HEALTH_CHECK).build().toString();
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            ResponseEntity<String> result =
                    restTemplate.exchange(endpoint, HttpMethod.GET, requestEntity, String.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    public InstanceResponse createInstance(BpmnInstanceRequest bpmnInstanceRequest) {
        try {
            logger.info("CnfAdapterService createInstance called");
            MulticloudInstanceRequest multicloudInstanceRequest = new MulticloudInstanceRequest();
            InstanceResponse instanceResponse = null;
            if (bpmnInstanceRequest.getK8sRBProfileName() != null) {
                multicloudInstanceRequest.setCloudRegion(bpmnInstanceRequest.getCloudRegionId());
                multicloudInstanceRequest.setLabels(bpmnInstanceRequest.getLabels());
                multicloudInstanceRequest.setOverrideValues(bpmnInstanceRequest.getOverrideValues());
                multicloudInstanceRequest.setProfileName(bpmnInstanceRequest.getK8sRBProfileName());
                multicloudInstanceRequest.setRbName(bpmnInstanceRequest.getModelInvariantId());
                multicloudInstanceRequest.setRbVersion(bpmnInstanceRequest.getModelVersionId());
                multicloudInstanceRequest.setVfModuleUuid(bpmnInstanceRequest.getVfModuleUUID());
            } else {
                logger.error("k8sProfileName should not be null");
                return instanceResponse;
            }
            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String uri = "http://multicloud-k8s:9015"; // TODO: What is the correct uri?
            String endpoint = UriBuilder.fromUri(uri).path(INSTANCE_CREATE_PATH).build().toString();
            HttpEntity<?> entity = getHttpEntity(multicloudInstanceRequest);
            ResponseEntity<InstanceResponse> result =
                    restTemplate.exchange(endpoint, HttpMethod.POST, entity, InstanceResponse.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    public InstanceResponse getInstanceByInstanceId(String instanceId) {

        try {

            logger.info("CnfAdapterService createInstance called");

            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String uri = "http://multicloud-k8s:9015"; // TODO: What is the correct uri?
            String path = "/v1/instance/" + instanceId;
            String endpoint = UriBuilder.fromUri(uri).path(path).build().toString();
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            ResponseEntity<InstanceResponse> result =
                    restTemplate.exchange(endpoint, HttpMethod.GET, requestEntity, InstanceResponse.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    public InstanceStatusResponse getInstanceStatusByInstanceId(String instanceId) {

        try {

            logger.info("CnfAdapterService createInstance called");

            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String uri = "http://multicloud-k8s:9015"; // TODO: What is the correct uri?
            String path = "/v1/instance/" + instanceId + "/status";
            String endpoint = UriBuilder.fromUri(uri).path(path).build().toString();
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            ResponseEntity<InstanceStatusResponse> result =
                    restTemplate.exchange(endpoint, HttpMethod.GET, requestEntity, InstanceStatusResponse.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    public InstanceMiniResponseList getInstanceByRBNameOrRBVersionOrProfileName(String rbName, String rbVersion,
            String profileName) {

        try {

            logger.info("CnfAdapterService createInstance called");

            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String uri = "http://multicloud-k8s:9015"; // TODO: What is the correct uri?
            String path =
                    "/v1/instance" + "?rb-name=" + rbName + "&rb-version=" + rbVersion + "&profile-name=" + profileName;
            String endPoint = uri + path;
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            ResponseEntity<InstanceMiniResponseList> result =
                    restTemplate.exchange(endPoint, HttpMethod.GET, requestEntity, InstanceMiniResponseList.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    public String deleteInstanceByInstanceId(String instanceId) {

        try {

            logger.info("CnfAdapterService createInstance called");

            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String uri = "http://multicloud-k8s:9015"; // TODO: What is the correct uri?
            String path = "/v1/instance/" + instanceId;
            String endpoint = UriBuilder.fromUri(uri).path(path).build().toString();
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            ResponseEntity<String> result =
                    restTemplate.exchange(endpoint, HttpMethod.DELETE, requestEntity, String.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
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

    protected HttpEntity<?> getHttpEntity(MulticloudInstanceRequest request) {
        HttpHeaders headers = getHttpHeaders();
        return new HttpEntity<>(request, headers);
    }
}
