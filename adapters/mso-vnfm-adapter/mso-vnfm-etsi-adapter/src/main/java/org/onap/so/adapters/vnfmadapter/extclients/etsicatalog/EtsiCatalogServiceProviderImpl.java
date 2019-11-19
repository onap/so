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

package org.onap.so.adapters.vnfmadapter.extclients.etsicatalog;

import java.util.Optional;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.EtsiCatalogManagerRequestFailureException;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.VnfPkgConflictException;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.VnfPkgNotFoundException;
import org.onap.so.rest.exceptions.HttpResouceNotFoundException;
import org.onap.so.rest.exceptions.RestProcessingException;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Provides the implementations of the REST Requests to the ETSI Catalog Manager.
 * 
 * @author gareth.roper@est.tech
 */
@Service
public class EtsiCatalogServiceProviderImpl implements EtsiCatalogServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(EtsiCatalogServiceProviderImpl.class);

    @Qualifier
    private final HttpRestServiceProvider httpServiceProvider;
    private final EtsiCatalogUrlProvider etsiCatalogUrlProvider;

    @Autowired
    public EtsiCatalogServiceProviderImpl(final EtsiCatalogUrlProvider etsiCatalogUrlProvider,
            final HttpRestServiceProvider httpServiceProvider) {
        this.etsiCatalogUrlProvider = etsiCatalogUrlProvider;
        this.httpServiceProvider = httpServiceProvider;
    }

    @Override
    public Optional<byte[]> getVnfPackageContent(String vnfPkgId) throws EtsiCatalogManagerRequestFailureException {
        try {
            ResponseEntity<byte[]> response = httpServiceProvider
                    .getHttpResponse(etsiCatalogUrlProvider.getVnfPackageContentUrl(vnfPkgId), byte[].class);
            logger.info("getVnfPackageContent Request to ETSI Catalog Manager Status Code: {}",
                    response.getStatusCodeValue());
            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (HttpResouceNotFoundException httpResouceNotFoundException) {
            logger.error("Caught HttpResouceNotFoundException", httpResouceNotFoundException);
            throw new VnfPkgNotFoundException("No Vnf Package found with vnfPkgId: " + vnfPkgId);
        } catch (RestProcessingException restProcessingException) {
            logger.error("Caught RestProcessingException with Status Code: {}", restProcessingException.getStatusCode(),
                    restProcessingException);
            if (restProcessingException.getStatusCode() == HttpStatus.CONFLICT.value()) {
                throw new VnfPkgConflictException("A conflict occurred with the state of the resource,\n"
                        + "due to the attribute: onboardingState not being set to ONBOARDED.");
            }
        }
        throw new EtsiCatalogManagerRequestFailureException("Internal Server Error Occurred.");
    }
}
