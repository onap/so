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

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.VnfmAdapterCreateVnfTaskConfiguration.VNFM_HTTP_REST_SERVICE_PROVIDER_BEAN;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.DeleteVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.QueryJobResponse;
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
import com.google.common.base.Optional;

/**
 * @author waqas.ikram@est.tech
 */
@Service
public class VnfmAdapterServiceProviderImpl implements VnfmAdapterServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(VnfmAdapterServiceProviderImpl.class);
    public static final String RECEIVED_RESPONSE_WITHOUT_BODY = "Received response without body: {}";

    private final VnfmAdapterUrlProvider urlProvider;
    private final HttpRestServiceProvider httpServiceProvider;

    @Autowired
    public VnfmAdapterServiceProviderImpl(@Qualifier("VnfmAdapterUrlProvider") final VnfmAdapterUrlProvider urlProvider,
            @Qualifier(VNFM_HTTP_REST_SERVICE_PROVIDER_BEAN) final HttpRestServiceProvider httpServiceProvider) {
        this.urlProvider = urlProvider;
        this.httpServiceProvider = httpServiceProvider;
    }

    @Override
    public Optional<CreateVnfResponse> invokeCreateInstantiationRequest(final String vnfId,
            final CreateVnfRequest request) {
        try {
            final String url = urlProvider.getCreateInstantiateUrl(vnfId);

            final ResponseEntity<CreateVnfResponse> response =
                    httpServiceProvider.postHttpRequest(request, url, CreateVnfResponse.class);

            final HttpStatus httpStatus = (HttpStatus) response.getStatusCode();
            if (!(httpStatus.equals(HttpStatus.ACCEPTED)) && !(httpStatus.equals(HttpStatus.OK))) {
                LOGGER.error("Unable to invoke HTTP POST using URL: {}, Response Code: {}", url, httpStatus.value());
                return Optional.absent();
            }

            if (!response.hasBody()) {
                LOGGER.error(RECEIVED_RESPONSE_WITHOUT_BODY, response);
                return Optional.absent();
            }

            final CreateVnfResponse createVnfResponse = response.getBody();

            if (createVnfResponse.getJobId() == null || createVnfResponse.getJobId().isEmpty()) {
                LOGGER.error("Received invalid instantiation response: {}", response);
                return Optional.absent();
            }

            return Optional.of(createVnfResponse);
        } catch (final RestProcessingException | InvalidRestRequestException
                | HttpResouceNotFoundException httpInvocationException) {
            LOGGER.error("Unexpected error while processing create and instantiation request", httpInvocationException);
            return Optional.absent();
        }

    }

    @Override
    public Optional<DeleteVnfResponse> invokeDeleteRequest(final String vnfId) {
        try {
            final String url = urlProvider.getDeleteUrl(vnfId);
            LOGGER.debug("Will send request to vnfm adapter using url: {}", url);

            final ResponseEntity<DeleteVnfResponse> response =
                    httpServiceProvider.deleteHttpRequest(url, DeleteVnfResponse.class);

            LOGGER.debug("Response received: ", response);

            final HttpStatus httpStatus = (HttpStatus) response.getStatusCode();

            if (!(httpStatus.equals(HttpStatus.ACCEPTED)) && !(httpStatus.equals(HttpStatus.OK))) {
                LOGGER.error("Unable to invoke HTTP DELETE using URL: {}, Response Code: {}", url, httpStatus.value());
                return Optional.absent();
            }

            if (!response.hasBody()) {
                LOGGER.error(RECEIVED_RESPONSE_WITHOUT_BODY, response);
                return Optional.absent();
            }
            final DeleteVnfResponse deleteVnfResponse = response.getBody();

            if (deleteVnfResponse.getJobId() == null || deleteVnfResponse.getJobId().isEmpty()) {
                LOGGER.error("Received invalid delete response: {}", response);
                return Optional.absent();
            }
            return Optional.of(deleteVnfResponse);
        } catch (final RestProcessingException | InvalidRestRequestException httpInvocationException) {
            LOGGER.error("Unexpected error while processing delete request", httpInvocationException);
            return Optional.absent();
        }
    }

    @Override
    public Optional<QueryJobResponse> getInstantiateOperationJobStatus(final String jobId) {
        try {
            final String url = urlProvider.getJobStatusUrl(jobId);

            final ResponseEntity<QueryJobResponse> response =
                    httpServiceProvider.getHttpResponse(url, QueryJobResponse.class);

            final HttpStatus httpStatus = (HttpStatus) response.getStatusCode();

            if (!(httpStatus.equals(HttpStatus.ACCEPTED)) && !(httpStatus.equals(HttpStatus.OK))) {
                LOGGER.error("Unable to invoke HTTP GET using URL: {}, Response Code: ", url, httpStatus.value());
                return Optional.absent();
            }

            if (!response.hasBody()) {
                LOGGER.error(RECEIVED_RESPONSE_WITHOUT_BODY, response);
                return Optional.absent();
            }
            return Optional.of(response.getBody());
        } catch (final RestProcessingException | InvalidRestRequestException
                | HttpResouceNotFoundException httpInvocationException) {
            LOGGER.error("Unexpected error while processing job request", httpInvocationException);
            throw httpInvocationException;
        }
    }
}
