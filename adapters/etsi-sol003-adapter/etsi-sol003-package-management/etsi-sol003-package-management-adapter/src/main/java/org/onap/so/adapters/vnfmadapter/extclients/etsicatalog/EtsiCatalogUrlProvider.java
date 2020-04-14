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

import static org.slf4j.LoggerFactory.getLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Provides the URLs for the REST Requests to the ETSI Catalog Manager.
 * 
 * @author gareth.roper@est.tech
 */
@Service
public class EtsiCatalogUrlProvider {

    private static final Logger logger = getLogger(EtsiCatalogUrlProvider.class);

    @Value("${etsi-catalog-manager.vnfpkgm.endpoint}")
    private String etsiCatalogManagerEndpoint;

    public EtsiCatalogUrlProvider() {}

    /**
     * Get the URL for retrieving the Package Content from the ETSI Catalog.".
     *
     * @param vnfPkgId The ID of the VNF Package
     * @return the URL for the GET operation
     */
    public String getVnfPackageContentUrl(final String vnfPkgId) {
        final String url = etsiCatalogManagerEndpoint + "/vnf_packages/" + vnfPkgId + "/package_content";
        logger.info("getEtsiCatalogVnfPackageContentUrl: {}", url);
        return url;
    }

    /**
     * Get the URL for retrieving VNF packages information from ETSI Catalog.".
     *
     * @return the URL for the GET operation
     */
    public String getVnfPackagesUrl() {
        final String url = etsiCatalogManagerEndpoint + "/vnf_packages";
        logger.info("getEtsiCatalogVnfPackagesEndpoint: {}", url);
        return url;
    }

    /**
     * Get the URL for retrieving specific VNF package information from the ETSI Catalog.".
     *
     * @param vnfPkgId The ID of the VNF Package
     * @return the URL for the GET operation
     */
    public String getVnfPackageUrl(final String vnfPkgId) {
        final String url = etsiCatalogManagerEndpoint + "/vnf_packages/" + vnfPkgId;
        logger.info("getEtsiCatalogVnfPackageEndpoint: {}", url);
        return url;
    }

    /**
     * Get the URL for retrieving VNF Package Artifacts
     *
     * @param vnfPkgId The ID of the VNF Package
     * @param artifactPath The path to the Artifact
     * @return the URL for the GET operation
     */
    public String getVnfPackageArtifactUrl(final String vnfPkgId, final String artifactPath) {
        final String url = etsiCatalogManagerEndpoint + "/vnf_packages/" + vnfPkgId + "/artifacts/" + artifactPath;
        logger.info("getVnfPackageArtifactUrl: {}", url);
        return url;
    }

    /**
     * Get the URL for retrieving VNF packages vnfd from ETSI Catalog.
     *
     * @param vnfPkgId The ID of the VNF Package
     * @return the URL for the GET operation
     */
    public String getVnfPackageVnfdUrl(final String vnfPkgId) {
        final String url = etsiCatalogManagerEndpoint + "/vnf_packages/" + vnfPkgId + "/vnfd";
        logger.info("getEtsiCatalogVnfPackageVnfd: {}", url);
        return url;
    }

    /**
     * Get the URL for posting/retrieving a Subscription
     *
     * @return the URL for the operation
     */
    public String getSubscriptionUrl() {
        final String url = etsiCatalogManagerEndpoint + "/subscriptions";
        logger.info("getSubscriptionNotificationUrl: {}", url);
        return url;
    }
}
