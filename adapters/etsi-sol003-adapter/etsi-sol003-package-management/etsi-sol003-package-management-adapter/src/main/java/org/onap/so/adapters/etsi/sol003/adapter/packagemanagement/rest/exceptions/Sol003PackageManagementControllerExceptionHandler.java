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

package org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.rest.exceptions;

import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.ProblemDetails;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.rest.Sol003PackageManagementController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Exception Handler for the Package Management Controller {@link Sol003PackageManagementController Sol003Controller}
 * 
 * @author gareth.roper@est.tech
 */
@ControllerAdvice(assignableTypes = Sol003PackageManagementController.class)

public class Sol003PackageManagementControllerExceptionHandler {

    @ExceptionHandler(EtsiCatalogManagerRequestFailureException.class)
    public ResponseEntity<ProblemDetails> handleEtsiCatalogManagerRequestFailureException(
            final EtsiCatalogManagerRequestFailureException etsiCatalogManagerRequestFailureException) {
        final ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setDetail(etsiCatalogManagerRequestFailureException.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetails);
    }

    @ExceptionHandler(EtsiCatalogManagerBadRequestException.class)
    public ResponseEntity<ProblemDetails> handleEtsiCatalogManagerBadRequestFailureException(
            final EtsiCatalogManagerBadRequestException etsiCatalogManagerBadRequestException) {
        final ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setDetail(etsiCatalogManagerBadRequestException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetails);
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleSubscriptionNotFoundException(
            final SubscriptionNotFoundException subscriptionNotFoundException) {
        final ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setDetail(subscriptionNotFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetails);
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ProblemDetails> handleConversionFailedException(
            final ConversionFailedException conversionFailedException) {
        final ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setDetail(conversionFailedException.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetails);
    }

    @ExceptionHandler(VnfPkgConflictException.class)
    public ResponseEntity<ProblemDetails> handleVnfPkgConflictException(
            final VnfPkgConflictException vnfPkgConflictException) {
        final ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setDetail(vnfPkgConflictException.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetails);
    }

    @ExceptionHandler(VnfPkgNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleVnfPkgNotFoundException(
            final VnfPkgNotFoundException vnfPkgNotFoundException) {
        final ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setDetail(vnfPkgNotFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetails);
    }

    @ExceptionHandler(VnfPkgBadRequestException.class)
    public ResponseEntity<ProblemDetails> handleVnfPkgBadRequestException(
            final VnfPkgBadRequestException vnfPkgBadRequestException) {
        final ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setDetail(vnfPkgBadRequestException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetails);
    }
}
