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

package org.onap.so.adapters.vnfmadapter.rest;

import static org.onap.so.adapters.vnfmadapter.common.CommonConstants.PACKAGE_MANAGEMENT_BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.ProblemDetails;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.PkgmSubscriptionRequest;
import org.onap.so.adapters.vnfmadapter.packagemanagement.subscriptionmanagement.SubscriptionManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for handling the Subscription Management. The client can use this resource to subscribe to notifications
 * related to the VNF package management, and to query its subscriptions. For further information please read:
 * https://www.etsi.org/deliver/etsi_gs/NFV-SOL/001_099/003/02.05.01_60/gs_nfv-sol003v020501p.pdf Use the section number
 * above each endpoint to find the corresponding section in the above document.
 *
 * @author Ronan Kenny (ronan.kenny@est.tech)
 * @author Gareth Roper (gareth.roper@est.tech)
 */
@Controller
@RequestMapping(value = PACKAGE_MANAGEMENT_BASE_URL, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
        consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class Sol003PackageManagementSubscriptionController {

    private static final String LOG_REQUEST_RECEIVED = "Subscription Management Controller: {} {}";
    private static final Logger logger = getLogger(Sol003PackageManagementSubscriptionController.class);
    private final SubscriptionManager subscriptionManager;

    @Autowired
    public Sol003PackageManagementSubscriptionController(final SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    /**
     * POST Subscribe request. Will send request and respond with the subscription that you subscribed to, if
     * successful. Section Number: 10.4.7
     * 
     * @param pkgmSubscriptionRequest This includes the details of the subscription to be created.
     * @return The subscription requested, if successful. Object: InlineRespone2002 Response Code: 201 Created Response
     *         Code: 303 Duplicate Subscription
     * @throws GeneralSecurityException
     */
    @PostMapping(value = "/subscriptions")
    public ResponseEntity<?> postSubscriptionRequest(@RequestBody final PkgmSubscriptionRequest pkgmSubscriptionRequest)
            throws GeneralSecurityException {
        logger.info(LOG_REQUEST_RECEIVED, " postSubscriptionRequest Endpoint Called", pkgmSubscriptionRequest);

        // Check if subscription exists already.
        final Optional<String> exists = subscriptionManager.getSubscriptionId(pkgmSubscriptionRequest);

        if (exists.isPresent()) {
            final URI subscriptionUri = subscriptionManager.getSubscriptionUri(exists.get());
            final HttpHeaders headers = createLocationHeader(subscriptionUri);
            logger.info("PkgmSubscriptionRequest already exists with uri {} ", subscriptionUri);
            return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
        }

        logger.debug("No duplicate Subscription exists, continuing with POST.");
        final Optional<InlineResponse201> optionalInlineResponse =
                subscriptionManager.createSubscription(pkgmSubscriptionRequest);

        if (optionalInlineResponse.isPresent()) {
            InlineResponse201 inlineResponse = optionalInlineResponse.get();
            final URI subscriptionUri = subscriptionManager.getSubscriptionUri(inlineResponse.getId());
            final HttpHeaders headers = createLocationHeader(subscriptionUri);
            logger.debug("Sending response with uri {} ", subscriptionUri);
            return new ResponseEntity<>(inlineResponse, headers, HttpStatus.CREATED);
        }
        final String errorMessage = "A null response was received during the postSubscriptionRequest call.";
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ProblemDetails().detail(errorMessage));
    }

    /**
     * GET all subscriptions. Will return a list of all subscriptions currently active. Section Number: 10.4.7
     * 
     * @return All of the current active subscriptions. Object: List<InlineResponse2002> Response Code: 200 OK
     */
    @GetMapping(value = "/subscriptions")
    public ResponseEntity<List<InlineResponse201>> getSubscriptions() {
        logger.info(LOG_REQUEST_RECEIVED, " getSubscriptions.");
        List<InlineResponse201> subscriptionsList = subscriptionManager.getSubscriptions();
        return new ResponseEntity<>(subscriptionsList, HttpStatus.OK);
    }

    /**
     * GET a specific subscription, by subscriptionId. Section Number: 10.4.8
     * 
     * @param subscriptionId The ID of the subscription that you wish to retrieve.
     * @return A subscription based on subscriptionId. Object: InlineResponse2002 Response Code: 200 OK
     */
    @GetMapping(value = "/subscriptions/{subscriptionId}")
    public ResponseEntity<?> getSubscription(@PathVariable("subscriptionId") final String subscriptionId) {
        logger.info(LOG_REQUEST_RECEIVED, " Getting Subscription: ", subscriptionId);
        final Optional<InlineResponse201> optional = subscriptionManager.getSubscription(subscriptionId);
        if (optional.isPresent()) {
            logger.debug("Return subscription with id {} and body {}", subscriptionId, optional);
            return new ResponseEntity<>(optional.get(), HttpStatus.OK);
        }
        final String errorMessage =
                "The requested subscription: " + subscriptionId + " was not found on call getSubscription";
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ProblemDetails().detail(errorMessage));
    }

    /**
     * DELETE a specific subscription, by subscriptionId. Section Number: 10.4.8.3.5
     *
     * @param subscriptionId The ID of the subscription that you wish to delete.
     * @return Empty response if successful. Object: Void Response Code: 204 No Content
     */
    @DeleteMapping(value = "/subscriptions/{subscriptionId}")
    public ResponseEntity<?> deleteSubscription(@PathVariable("subscriptionId") final String subscriptionId) {
        if (subscriptionManager.deleteSubscription(subscriptionId)) {
            logger.debug("Successfully deleted subscription with id {}", subscriptionId);
            return ResponseEntity.noContent().build();
        }
        final String errorMessage =
                "The requested subscription: " + subscriptionId + " was not found on call deleteSubscription";
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ProblemDetails().detail(errorMessage));
    }

    /**
     * Method to set the Location in the header with the URI parameter
     * 
     * @param subscriptionUri
     * @return header with callbackUri in Location
     */
    private HttpHeaders createLocationHeader(final URI subscriptionUri) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(subscriptionUri);
        return headers;
    }

}
