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

package org.onap.so.bpmn.infrastructure.etsi.vnf.tasks;

import org.onap.so.rest.exceptions.InvalidRestRequestException;
import org.onap.so.rest.exceptions.RestProcessingException;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.onap.vnfmadapter.v1.model.CreateVnfRequest;
import org.onap.vnfmadapter.v1.model.CreateVnfResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final VnfmAdapterUrlProvider urlProvider;
    private final HttpRestServiceProvider httpServiceProvider;

    @Autowired
    public VnfmAdapterServiceProviderImpl(final VnfmAdapterUrlProvider urlProvider,
            final HttpRestServiceProvider httpServiceProvider) {
        this.urlProvider = urlProvider;
        this.httpServiceProvider = httpServiceProvider;
    }

    @Override
    public Optional<CreateVnfResponse> invokeCreateInstantiationRequest(final String vnfId,
            final CreateVnfRequest request) {
        try {
            final String url = urlProvider.getCreateInstantiateUrl(vnfId);
            LOGGER.debug("Will send request to vnfm adapter using url: {}", url);

            final ResponseEntity<CreateVnfResponse> response =
                    httpServiceProvider.postHttpRequest(request, url, CreateVnfResponse.class);

            LOGGER.debug("Response received: ", response);

            final HttpStatus httpStatus = response.getStatusCode();

            if (!(httpStatus.equals(HttpStatus.ACCEPTED)) && !(httpStatus.equals(HttpStatus.OK))) {
                LOGGER.error("Unable to invoke HTTP POST using URL: {}, Response Code: {}", url, httpStatus.value());
                return Optional.absent();
            }

            if (!response.hasBody()) {
                LOGGER.error("Received response without body: {}", response);
                return Optional.absent();
            }

            final CreateVnfResponse createVnfResponse = response.getBody();

            if (createVnfResponse.getJobId() == null || createVnfResponse.getJobId().isEmpty()) {
                LOGGER.error("Received invalid instantiation response: {}", response);
                return Optional.absent();
            }

            return Optional.of(createVnfResponse);
        } catch (final RestProcessingException | InvalidRestRequestException httpInvocationException) {
            LOGGER.error("Unexpected error while processing create and instantiation request", httpInvocationException);
            return Optional.absent();
        }

    }
}
