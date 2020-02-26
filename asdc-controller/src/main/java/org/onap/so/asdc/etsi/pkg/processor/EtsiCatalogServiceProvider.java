/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
package org.onap.so.asdc.etsi.pkg.processor;

import static org.onap.so.asdc.etsi.pkg.processor.HttpRestServiceProviderConfiguration.ETSI_CATALOG_HTTP_REST_SERVICE_PROVIDER_BEAN;
import org.onap.so.asdc.etsi.pkg.processor.exceptions.EtsiCatalogManagerRequestFailureException;
import org.onap.so.rest.exceptions.InvalidRestRequestException;
import org.onap.so.rest.exceptions.RestProcessingException;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class EtsiCatalogServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtsiCatalogServiceProvider.class);

    private final HttpRestServiceProvider httpServiceProvider;

    @Value("${etsi-catalog-manager.endpoint:http://modeling-etsicatalog.onap:8806/api/catalog/v1}")
    private String etsiCatalogManagerEndpoint;

    @Autowired
    public EtsiCatalogServiceProvider(
            @Qualifier(ETSI_CATALOG_HTTP_REST_SERVICE_PROVIDER_BEAN) final HttpRestServiceProvider httpServiceProvider) {
        this.httpServiceProvider = httpServiceProvider;
    }

    public EtsiCatalogPackageOnboardingJob onBoardResource(
            final EtsiCatalogPackageOnboardingRequest packageOnboardingRequest) {
        try {
            final String url = etsiCatalogManagerEndpoint + "/vnfpackages";
            final ResponseEntity<EtsiCatalogPackageOnboardingJob> responseEntity = httpServiceProvider.postHttpRequest(
                    packageOnboardingRequest, url, getHeaders(), EtsiCatalogPackageOnboardingJob.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                if (responseEntity.hasBody()) {
                    return responseEntity.getBody();
                }
                LOGGER.error("Received response without body");
            }
            final String message = "Unexpected status code received " + responseEntity.getStatusCode();
            LOGGER.error(message);
            throw new EtsiCatalogManagerRequestFailureException(message);

        } catch (final InvalidRestRequestException | RestProcessingException exception) {
            final String message = "Unable to process onboarding request";
            LOGGER.error(message, exception);
            throw new EtsiCatalogManagerRequestFailureException(message);
        }

    }

    public EtsiCatalogPackageOnboadingJobStatus getJobStatus(final String jobId) {
        try {
            final String url = etsiCatalogManagerEndpoint + "/jobs/" + jobId;

            final ResponseEntity<EtsiCatalogPackageOnboadingJobStatus> responseEntity =
                    httpServiceProvider.getHttpResponse(url, getHeaders(), EtsiCatalogPackageOnboadingJobStatus.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                if (responseEntity.hasBody()) {
                    return responseEntity.getBody();
                }
                LOGGER.error("Received response without body");
            }
            final String message =
                    "Unexpected status code received while getting job status " + responseEntity.getStatusCode();
            LOGGER.error(message);
            throw new EtsiCatalogManagerRequestFailureException(message);
        } catch (final InvalidRestRequestException | RestProcessingException exception) {
            final String message = "Unable to get job status";
            LOGGER.error(message, exception);
            throw new EtsiCatalogManagerRequestFailureException(message);
        }

    }

    private HttpHeaders getHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
