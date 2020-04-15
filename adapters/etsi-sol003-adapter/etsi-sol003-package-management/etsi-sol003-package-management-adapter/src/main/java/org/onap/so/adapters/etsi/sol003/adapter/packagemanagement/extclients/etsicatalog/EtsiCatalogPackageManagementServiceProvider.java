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
package org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog;

import java.util.Optional;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.InlineResponse2001;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public interface EtsiCatalogPackageManagementServiceProvider {

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

    /**
     * GET specific VNF package VNFD from ETSI Catalog.
     *
     * @param vnfPkgId The ID of the VNF Package that you want to query.
     * @return The VNF package retrieved from the ETSI Catalog
     */
    Optional<byte[]> getVnfPackageVnfd(final String vnfPkgId);

    /**
     * GET Package Artifact, from VNF Package.
     *
     * @param vnfPkgId The ID of the VNF Package from which the artifact will be retrieved.
     * @param artifactPath Sequence of one or more path segments representing the path of the artifact within the VNF
     *        Package, e.g., foo/bar/run.sh
     * @return The Package Artifact of a VNF Package ("vnfPkgId", "artifactPath").
     */
    Optional<byte[]> getVnfPackageArtifact(final String vnfPkgId, final String artifactPath);

}
