/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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
package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;

import static org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks.CnfmHttpServiceConfiguration.CNFM_HTTP_REST_SERVICE_PROVIDER_BEAN;
import java.net.URI;
import java.util.Optional;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;
import org.onap.so.cnfm.lcm.model.TerminateAsRequest;
import org.onap.so.rest.exceptions.HttpResouceNotFoundException;
import org.onap.so.rest.exceptions.InvalidRestRequestException;
import org.onap.so.rest.exceptions.RestProcessingException;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CnfmHttpServiceProviderImpl implements CnfmHttpServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CnfmHttpServiceProviderImpl.class);
    private final CnfmUrlProvider cnfmUrlProvider;
    private final HttpRestServiceProvider httpServiceProvider;

    @Autowired
    public CnfmHttpServiceProviderImpl(final CnfmUrlProvider cnfmUrlProvider,
            @Qualifier(CNFM_HTTP_REST_SERVICE_PROVIDER_BEAN) final HttpRestServiceProvider httpServiceProvider) {
        this.cnfmUrlProvider = cnfmUrlProvider;
        this.httpServiceProvider = httpServiceProvider;
    }

    @Override
    public Optional<AsInstance> invokeCreateAsRequest(final CreateAsRequest createAsRequest) {
        try {
            final String url = cnfmUrlProvider.getCreateAsRequestUrl();
            final ResponseEntity<AsInstance> response =
                    httpServiceProvider.postHttpRequest(createAsRequest, url, AsInstance.class);

            final HttpStatus httpStatus = (HttpStatus) response.getStatusCode();
            if (httpStatus.is2xxSuccessful()) {
                if (!response.hasBody()) {
                    LOGGER.error("Received response without body: {}", response);
                    return Optional.empty();
                }

                final AsInstance asInstance = response.getBody();
                if (asInstance.getAsInstanceid() == null || asInstance.getAsInstanceid().isEmpty()) {
                    LOGGER.error("Received invalid response missing asInstanceid: {}", response);
                    return Optional.empty();
                }
                return Optional.of(asInstance);
            }
            LOGGER.error("Unable to invoke HTTP POST using URL: {}, Response Code: {}", url, httpStatus.value());
            return Optional.empty();

        } catch (final RestProcessingException | InvalidRestRequestException
                | HttpResouceNotFoundException httpInvocationException) {
            LOGGER.error("Unexpected error while processing create and instantiation request", httpInvocationException);
            return Optional.empty();
        }
    }

    @Override
    public Optional<URI> invokeInstantiateAsRequest(InstantiateAsRequest instantiateAsRequest, String asInstanceId) {
        try {

            final String url = cnfmUrlProvider.getInstantiateAsRequestUrl(asInstanceId);
            final ResponseEntity<AsInstance> response =
                    httpServiceProvider.postHttpRequest(instantiateAsRequest, url, AsInstance.class);
            final HttpStatus httpStatus = (HttpStatus) response.getStatusCode();
            if (httpStatus.is2xxSuccessful()) {
                URI statusUri = response.getHeaders().getLocation();
                if (statusUri == null) {
                    LOGGER.error("Received response without status URL for instance ID: {}", asInstanceId);
                    return Optional.empty();
                }
                return Optional.of(statusUri);
            }
            LOGGER.error("Unable to invoke HTTP POST using URL: {}, Response Code: {}", url, httpStatus.value());
            return Optional.empty();

        } catch (final RestProcessingException | InvalidRestRequestException
                | HttpResouceNotFoundException httpInvocationException) {
            LOGGER.error("Unexpected error while processing instantiation request", httpInvocationException);
            return Optional.empty();
        }

    }

    @Override
    public Optional<AsLcmOpOcc> getOperationJobStatus(final String url) {
        try {
            final ResponseEntity<AsLcmOpOcc> response = httpServiceProvider.getHttpResponse(url, AsLcmOpOcc.class);

            final HttpStatus httpStatus = (HttpStatus) response.getStatusCode();

            if (!(httpStatus.equals(HttpStatus.ACCEPTED)) && !(httpStatus.equals(HttpStatus.OK))) {
                LOGGER.error("Unable to invoke HTTP GET using URL: {}, Response Code: {}", url, httpStatus.value());
                return Optional.empty();
            }

            if (!response.hasBody()) {
                LOGGER.error("CNFM status response recieved without body: {}", response);
                return Optional.empty();
            }
            return Optional.of(response.getBody());
        } catch (final RestProcessingException | InvalidRestRequestException
                | HttpResouceNotFoundException httpInvocationException) {
            LOGGER.error("Unexpected error while processing job request", httpInvocationException);
            throw httpInvocationException;
        }
    }

    @Override
    public Optional<Boolean> invokeDeleteAsRequest(String asInstanceId) {
        try {

            final String url = cnfmUrlProvider.getDeleteAsRequestUrl(asInstanceId);
            LOGGER.debug("Will send request to CNFM by uisng the url: {}", url);

            ResponseEntity<Void> response = httpServiceProvider.deleteHttpRequest(url, Void.class);
            final HttpStatus httpStatus = (HttpStatus) response.getStatusCode();
            if (!(httpStatus.is2xxSuccessful())) {
                LOGGER.error("Unable to invoke HTTP DELETE using URL: {}, Response Code: {}", url, httpStatus.value());
                return Optional.empty();
            }
            return Optional.of(Boolean.TRUE);
        } catch (final RestProcessingException | InvalidRestRequestException
                | HttpResouceNotFoundException httpInvocationException) {
            LOGGER.error("Unexpected error while processing delete request", httpInvocationException);
            return Optional.empty();
        }
    }

    @Override
    public Optional<URI> invokeTerminateAsRequest(String asInstanceId, TerminateAsRequest terminateAsRequest) {
        try {

            final String url = cnfmUrlProvider.getTerminateAsRequestUrl(asInstanceId);
            LOGGER.debug("Will send request to CNFM to terminate by uisng the url: {}", url);

            ResponseEntity<Void> response = httpServiceProvider.postHttpRequest(terminateAsRequest, url, Void.class);
            final HttpStatus httpStatus = (HttpStatus) response.getStatusCode();
            if (httpStatus.is2xxSuccessful()) {
                URI statusUri = response.getHeaders().getLocation();
                if (statusUri == null) {
                    LOGGER.error("Received response without status URL while terminating of instance with ID: {}",
                            asInstanceId);
                    return Optional.empty();
                }
                return Optional.of(statusUri);
            }
            LOGGER.error("Unable to invoke HTTP DELETE while terminating by using URL: {}, Response Code: {}", url,
                    httpStatus.value());
            return Optional.empty();

        } catch (final RestProcessingException | InvalidRestRequestException
                | HttpResouceNotFoundException httpInvocationException) {
            LOGGER.error("Unexpected error while processing terminate request", httpInvocationException);
            return Optional.empty();
        }
    }

}
