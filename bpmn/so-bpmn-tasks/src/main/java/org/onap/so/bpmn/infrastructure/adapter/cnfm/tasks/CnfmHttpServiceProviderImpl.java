/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Ericsson. All rights reserved.
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
import java.util.Optional;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
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

            final HttpStatus httpStatus = response.getStatusCode();
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

}
