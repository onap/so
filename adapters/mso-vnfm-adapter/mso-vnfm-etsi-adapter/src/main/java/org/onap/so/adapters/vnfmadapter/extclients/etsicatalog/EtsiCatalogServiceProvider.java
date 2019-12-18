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

import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.InlineResponse2001;
import java.util.Optional;

/**
 * Provides methods for invoking REST calls to the ETSI Catalog Manager.
 * 
 * @author gareth.roper@est.tech
 */
public interface EtsiCatalogServiceProvider {

    /**
     * GET Package Content, from VNF Package.
     * 
     * @param vnfPkgId The ID of the VNF Package from which the "package_content" will be retrieved.
     * @return The Package Content of a VNF Package ("vnfPkgId").
     */
    Optional<byte[]> getVnfPackageContent(final String vnfPkgId);

    /**
     * GET VNF packages information from ETSI Catalog. Will return zero or more VNF package representations.
     *
     * @return An Array of all VNF packages retrieved from the ETSI Catalog.
     */
    Optional<InlineResponse2001[]> getVnfPackages();

    /**
     * GET specific VNF package information from ETSI Catalog.
     *
     * @param vnfPkgId The ID of the VNF Package that you want to query.
     * @return The VNF package retrieved from the ETSI Catalog
     */
    Optional<InlineResponse2001> getVnfPackage(final String vnfPkgId);

}
