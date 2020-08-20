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
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.etsicatalog;

import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.etsicatalog.EtsiCatalogServiceProviderConfiguration.ETSI_CATALOG_SERVICE_PROVIDER_BEAN;
import java.util.Optional;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.NsdInfo;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.VnfPkgInfo;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.exceptions.EtsiCatalogManagerRequestFailureException;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class EtsiCatalogPackageManagementServiceProviderImpl implements EtsiCatalogPackageManagementServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(EtsiCatalogPackageManagementServiceProviderImpl.class);

    private final HttpRestServiceProvider httpServiceProvider;
    private final EtsiCatalogUrlProvider etsiCatalogUrlProvider;

    @Autowired
    public EtsiCatalogPackageManagementServiceProviderImpl(final EtsiCatalogUrlProvider etsiCatalogUrlProvider,
            @Qualifier(ETSI_CATALOG_SERVICE_PROVIDER_BEAN) final HttpRestServiceProvider httpServiceProvider) {
        this.etsiCatalogUrlProvider = etsiCatalogUrlProvider;
        this.httpServiceProvider = httpServiceProvider;
    }

    @Override
    public Optional<NsdInfo> getNSPackageModel(final String nsdId) {
        try {
            final ResponseEntity<NsdInfo> response =
                    httpServiceProvider.getHttpResponse(etsiCatalogUrlProvider.getNsPackageUrl(nsdId), NsdInfo.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
            return Optional.empty();
        } catch (final Exception restProcessingException) {
            logger.error("Caught exception while getting NS package model for: {}", nsdId, restProcessingException);
            throw new EtsiCatalogManagerRequestFailureException("Internal Server Error Occurred.",
                    restProcessingException);
        }
    }

    @Override
    public Optional<VnfPkgInfo> getVnfPkgInfo(final String vnfPkgId) {
        try {
            final ResponseEntity<VnfPkgInfo> response = httpServiceProvider
                    .getHttpResponse(etsiCatalogUrlProvider.getVnfPackageUrl(vnfPkgId), VnfPkgInfo.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
            return Optional.empty();
        } catch (final Exception restProcessingException) {
            logger.error("Caught exception while getting VNF package model for: {}", vnfPkgId, restProcessingException);
            throw new EtsiCatalogManagerRequestFailureException("Internal Server Error Occurred.",
                    restProcessingException);
        }
    }

}
