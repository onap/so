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

package org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions;

import static org.slf4j.LoggerFactory.getLogger;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.ProblemDetails;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.EtsiSubscriptionNotificationController;
import org.onap.so.rest.exceptions.HttpResouceNotFoundException;
import org.onap.so.rest.exceptions.InvalidRestRequestException;
import org.onap.so.rest.exceptions.RestProcessingException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Exception Handler for the Etsi Subscription Notification Controller {@link EtsiSubscriptionNotificationController
 * EtsiSubscriptionNotificationController}
 * 
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 */
@ControllerAdvice(assignableTypes = EtsiSubscriptionNotificationController.class)
public class EtsiSubscriptionNotificationControllerExceptionHandler {

    private static final Logger logger = getLogger(EtsiSubscriptionNotificationControllerExceptionHandler.class);

    @ExceptionHandler(InvalidRestRequestException.class)
    public ResponseEntity<ProblemDetails> handleInvalidRestRequestException(
            final InvalidRestRequestException invalidRestRequestException) {
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed with response: "
                + HttpStatus.BAD_REQUEST + ".\n" + invalidRestRequestException.getMessage();
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ProblemDetails().detail(errorMessage));
    }

    @ExceptionHandler(HttpResouceNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleHttpResourceNotFoundException(
            final HttpResouceNotFoundException httpResourceNotFoundException) {
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed with response: "
                + HttpStatus.NOT_FOUND + ".\n" + httpResourceNotFoundException.getMessage();
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ProblemDetails().detail(errorMessage));
    }

    @ExceptionHandler(RestProcessingException.class)
    public ResponseEntity<ProblemDetails> handleRestProcessingException(
            final RestProcessingException restProcessingException) {
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed with response: "
                + restProcessingException.getStatusCode() + ".\n" + restProcessingException.getMessage();
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ProblemDetails().detail(errorMessage));
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ProblemDetails> handleInternalServerErrorException(
            final InternalServerErrorException internalServerErrorException) {
        logger.error(internalServerErrorException.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ProblemDetails().detail(internalServerErrorException.getMessage()));
    }

    @ExceptionHandler(AuthenticationTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetails> handleAuthenticationTypeNotSupportedException(
            final AuthenticationTypeNotSupportedException authenticationTypeNotSupportedException) {
        logger.error(authenticationTypeNotSupportedException.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ProblemDetails().detail(authenticationTypeNotSupportedException.getMessage()));
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ProblemDetails> handleConversionFailedException(
            final ConversionFailedException conversionFailedException) {
        logger.error(conversionFailedException.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ProblemDetails().detail(conversionFailedException.getMessage()));
    }

    @ExceptionHandler(NotificationTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetails> handleNotificationTypeNotSupportedException(
            final NotificationTypeNotSupportedException notificationTypeNotSupportedException) {
        logger.error(notificationTypeNotSupportedException.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ProblemDetails().detail(notificationTypeNotSupportedException.getMessage()));
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleSubscriptionNotFoundException(
            final SubscriptionNotFoundException subscriptionNotFoundException) {
        logger.error(subscriptionNotFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ProblemDetails().detail(subscriptionNotFoundException.getMessage()));
    }
}
