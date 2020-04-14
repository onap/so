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

package org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.rest;

import static org.onap.so.adapters.etsi.sol003.adapter.common.CommonConstants.PACKAGE_MANAGEMENT_BASE_URL;
import static org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.PackageManagementConstants.APPLICATION_ZIP;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.ProblemDetails;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.EtsiCatalogServiceProvider;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.InlineResponse2001;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for handling the VNF Package Management. For further information please read:
 * https://www.etsi.org/deliver/etsi_gs/NFV-SOL/001_099/003/02.05.01_60/gs_nfv-sol003v020501p.pdf Use the section number
 * above each endpoint to find the corresponding section in the above document.
 *
 * @author gareth.roper@est.tech
 */
@Controller
@RequestMapping(value = PACKAGE_MANAGEMENT_BASE_URL, consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class Sol003PackageManagementController {

    private final EtsiCatalogServiceProvider etsiCatalogServiceProvider;
    private static final String LOG_REQUEST_RECEIVED = "VNF PackageManagement Controller: {} {} {} {}";
    private static final Logger logger = getLogger(Sol003PackageManagementController.class);

    @Autowired
    Sol003PackageManagementController(final EtsiCatalogServiceProvider etsiCatalogServiceProvider) {
        this.etsiCatalogServiceProvider = etsiCatalogServiceProvider;
    }

    /**
     * GET VNF packages information. Will return zero or more VNF package representations that match the attribute
     * filter. These representations will be in a list. Section Number: 10.4.2
     * 
     * @return An Array of all VNF packages. Object: InlineResponse2001[] Response Code: 200 OK
     */
    @GetMapping(value = "/vnf_packages", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<?> getVnfPackages() {
        logger.info(LOG_REQUEST_RECEIVED, "getVnfPackages.");
        final Optional<InlineResponse2001[]> response = etsiCatalogServiceProvider.getVnfPackages();
        if (response.isPresent()) {
            logger.info(LOG_REQUEST_RECEIVED, "getVnfPackages Response: ", HttpStatus.OK);
            return ResponseEntity.ok().body(response.get());
        }
        final String errorMessage = "An error occurred, a null response was received by the\n"
                + " Sol003PackageManagementController from the EtsiCatalogManager using the GET \"vnf_packages\" \n"
                + "endpoint.";
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ProblemDetails().detail(errorMessage));
    }

    /**
     * GET VNF package information. Will return a specific VNF package representation that match the attribute filter.
     * Section Number: 10.4.3
     *
     * @param vnfPkgId The ID of the VNF Package that you want to query.
     * @return A VNF package based on vnfPkgId. Object: VnfPkgInfo Response Code: 200 OK
     */
    @GetMapping(value = "/vnf_packages/{vnfPkgId}", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<?> getVnfPackage(@PathVariable("vnfPkgId") final String vnfPkgId) {
        logger.info(LOG_REQUEST_RECEIVED, "getVnfPackage: ", vnfPkgId);
        final Optional<InlineResponse2001> response = etsiCatalogServiceProvider.getVnfPackage(vnfPkgId);
        if (response.isPresent()) {
            logger.info(LOG_REQUEST_RECEIVED, "getVnfPackage Response: ", HttpStatus.OK);
            return ResponseEntity.ok().body(response.get());
        }
        final String errorMessage = "An error occurred, a null response was received by the\n"
                + " Sol003PackageManagementController from the EtsiCatalogManager using the GET \"vnf_packages\" by vnfPkgId: \""
                + vnfPkgId + "\" \n" + "endpoint.";
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ProblemDetails().detail(errorMessage));
    }

    /**
     * GET VNFD, from VNF package. Will return a copy of the file representing the VNFD or a ZIP file that contains the
     * file/multiple files representing the VNFD specified. Section Number: 10.4.4
     *
     * @param vnfPkgId The ID of the VNF Package that you want to retrieve the VNFD from.
     * @return The VNFD of a VNF Package as a single file or within a ZIP file. Object: byte[] Response Code: 200 OK
     */
    @GetMapping(value = "/vnf_packages/{vnfPkgId}/vnfd",
            produces = {MediaType.TEXT_PLAIN, APPLICATION_ZIP, MediaType.APPLICATION_JSON})
    public ResponseEntity<?> getVnfPackageVnfd(@PathVariable("vnfPkgId") final String vnfPkgId) {
        logger.info(LOG_REQUEST_RECEIVED, "getVnfPackageVnfd Endpoint Invoked with VNF Package ID: ", vnfPkgId);
        final Optional<byte[]> response = etsiCatalogServiceProvider.getVnfPackageVnfd(vnfPkgId);
        if (response.isPresent()) {
            logger.info(LOG_REQUEST_RECEIVED, "getVnfPackageVnfd Response: ", HttpStatus.OK);
            return new ResponseEntity<>(response.get(), HttpStatus.OK);
        }
        final String errorMessage = "An error occurred, a null response was received by the\n"
                + " Sol003PackageManagementController from the EtsiCatalogManager using the GET \"vnfd\" \n"
                + "endpoint.";

        logger.error(errorMessage);
        return new ResponseEntity<>(new ProblemDetails().detail(errorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * GET Package Content, from VNF Package. Will return a copy of the VNF package file that you specified. Section
     * Number: 10.4.5
     * 
     * @param vnfPkgId The ID of the VNF Package that you want to retrieve the "package_content" from.
     * @return The Package Content of a VNF Package. Object: byte[] Response Code: 200 OK
     */
    @GetMapping(value = "/vnf_packages/{vnfPkgId}/package_content",
            produces = {MediaType.APPLICATION_JSON, APPLICATION_ZIP, MediaType.APPLICATION_OCTET_STREAM})
    public ResponseEntity<?> getVnfPackageContent(@PathVariable("vnfPkgId") final String vnfPkgId) {
        logger.info(LOG_REQUEST_RECEIVED, "getVnfPackageContent Endpoint Invoked with VNF Package ID: ", vnfPkgId);
        final Optional<byte[]> response = etsiCatalogServiceProvider.getVnfPackageContent(vnfPkgId);
        if (response.isPresent()) {
            logger.info(LOG_REQUEST_RECEIVED, "getVnfPackageContent Response: ", HttpStatus.OK);
            return ResponseEntity.ok().body(response.get());
        }
        final String errorMessage = "An error occurred, a null response was received by the\n"
                + " Sol003PackageManagementController from the EtsiCatalogManager using the GET \"package_content\" \n"
                + "endpoint.";
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ProblemDetails().detail(errorMessage));
    }

    /**
     * GET Artifact, from VNF Package Will return a the content of the artifact that you specified. Section Number:
     * 10.4.6
     * 
     * @param vnfPkgId The ID of the VNF Package that you want to retrieve an artifact from.
     * @param artifactPath The path of the artifact that you want to retrieve.
     * @return An Artifact from a VNF Package. Object: byte[] Response Code: 200 OK
     */
    @GetMapping(value = "/vnf_packages/{vnfPkgId}/artifacts/{artifactPath}",
            produces = {MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public ResponseEntity<?> getVnfPackageArtifact(@PathVariable("vnfPkgId") final String vnfPkgId,
            @PathVariable("artifactPath") final String artifactPath) {
        logger.info(LOG_REQUEST_RECEIVED, "getVnfPackageArtifact: vnfPkgId= ", vnfPkgId, " artifactPath=",
                artifactPath);
        final Optional<byte[]> response = etsiCatalogServiceProvider.getVnfPackageArtifact(vnfPkgId, artifactPath);
        if (response.isPresent()) {
            logger.info(LOG_REQUEST_RECEIVED, "getVnfPackageArtifact Response: ", HttpStatus.OK);
            return ResponseEntity.ok().body(response.get());
        }
        final String errorMessage = "An error occurred, a null response was received by the\n"
                + " Sol003PackageManagementController from the EtsiCatalogManager using the\n GET \"vnf_packages\" by vnfPkgId: \""
                + vnfPkgId + "\" for artifactPath: \"" + artifactPath + "\"\n" + "endpoint.";
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ProblemDetails().detail(errorMessage));
    }

}
