/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.vnfm;

import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.vnfm.Sol003AdapterConfiguration.SOL003_ADAPTER_HTTP_REST_SERVICE_PROVIDER_BEAN;
import java.util.Optional;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
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

@Service
public class Sol003AdapterServiceProviderImpl implements Sol003AdapterServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sol003AdapterServiceProviderImpl.class);
    public static final String RECEIVED_RESPONSE_WITHOUT_BODY = "Received response without body: {}";

    private final Sol003AdapterUrlProvider urlProvider;
    private final HttpRestServiceProvider httpServiceProvider;

    @Autowired
    public Sol003AdapterServiceProviderImpl(final Sol003AdapterUrlProvider urlProvider,
            @Qualifier(SOL003_ADAPTER_HTTP_REST_SERVICE_PROVIDER_BEAN) final HttpRestServiceProvider httpServiceProvider) {
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

            final HttpStatus httpStatus = response.getStatusCode();
            if (!(httpStatus.equals(HttpStatus.ACCEPTED)) && !(httpStatus.equals(HttpStatus.OK))) {
                LOGGER.error("Unable to invoke HTTP POST using URL: {}, Response Code: {}", url, httpStatus.value());
                return Optional.empty();
            }

            if (!response.hasBody()) {
                LOGGER.error(RECEIVED_RESPONSE_WITHOUT_BODY, response);
                return Optional.empty();
            }

            final CreateVnfResponse createVnfResponse = response.getBody();

            if (createVnfResponse.getJobId() == null || createVnfResponse.getJobId().isEmpty()) {
                LOGGER.error("Received invalid instantiation response: {}", response);
                return Optional.empty();
            }

            return Optional.of(createVnfResponse);
        } catch (final RestProcessingException | InvalidRestRequestException
                | HttpResouceNotFoundException httpInvocationException) {
            LOGGER.error("Unexpected error while processing create and instantiation request", httpInvocationException);
            return Optional.empty();
        }

    }

    @Override
    public Optional<QueryJobResponse> getInstantiateOperationJobStatus(final String jobId) {
        try {
            final String url = urlProvider.getJobStatusUrl(jobId);

            final ResponseEntity<QueryJobResponse> response =
                    httpServiceProvider.getHttpResponse(url, QueryJobResponse.class);

            final HttpStatus httpStatus = response.getStatusCode();

            if (!(httpStatus.equals(HttpStatus.ACCEPTED)) && !(httpStatus.equals(HttpStatus.OK))) {
                LOGGER.error("Unable to invoke HTTP GET using URL: {}, Response Code: ", url, httpStatus.value());
                return Optional.empty();
            }

            if (!response.hasBody()) {
                LOGGER.error(RECEIVED_RESPONSE_WITHOUT_BODY, response);
                return Optional.empty();
            }
            return Optional.of(response.getBody());
        } catch (final RestProcessingException | InvalidRestRequestException | HttpResouceNotFoundException exception) {
            LOGGER.error("Unexpected error while processing job request", exception);
            throw exception;
        }
    }
}
