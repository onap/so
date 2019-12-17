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

package org.onap.so.adapters.vnfmadapter.rest;

import static org.onap.so.adapters.vnfmadapter.Constants.PACKAGE_MANAGEMENT_BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.InlineResponse2002;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.PkgmSubscriptionRequest;
import org.slf4j.Logger;
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
 * Controller for handling the Subscription Management. For further information please read:
 * https://www.etsi.org/deliver/etsi_gs/NFV-SOL/001_099/003/02.05.01_60/gs_nfv-sol003v020501p.pdf Use the section number
 * above each endpoint to find the corresponding section in the above document.
 *
 * @author gareth.roper@est.tech
 */
@Controller
@RequestMapping(value = PACKAGE_MANAGEMENT_BASE_URL, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
        consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class Sol003SubscriptionManagementController {

    private static final String LOG_REQUEST_RECEIVED = "Subscription Management Controller: {} {}";
    private static final Logger logger = getLogger(Sol003SubscriptionManagementController.class);

    /**
     * POST Subscribe request. Will send request and respond with the subscription that you subscribed to, if
     * successful. Section Number: 10.4.7
     * 
     * @param pkgmSubscriptionRequest This includes the details of the subscription to be created.
     * @return The subscription requested, if successful. Object: InlineRespone2002 Response Code: 201 Created Response
     *         Code: 303 Duplicate Subscription
     */
    @PostMapping(value = "/subscriptions")
    public ResponseEntity<InlineResponse2002> postSubscriptionRequest(
            @RequestBody final PkgmSubscriptionRequest pkgmSubscriptionRequest) {
        logger.info(LOG_REQUEST_RECEIVED, " postSubscriptionRequest: ", pkgmSubscriptionRequest);
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * GET all subscriptions. Will return a list of all subscriptions currently active. Section Number: 10.4.7
     * 
     * @return All of the current active subscriptions. Object: List<InlineResponse2002> Response Code: 200 OK
     */
    @GetMapping(value = "/subscriptions")
    public ResponseEntity<List<InlineResponse2002>> getSubscriptions() {
        logger.info(LOG_REQUEST_RECEIVED, " getSubscriptions.");
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * GET a specific subscription, by subscriptionId. Section Number: 10.4.8
     * 
     * @param subscriptionId The ID of the subscription that you wish to retrieve.
     * @return A subscription based on subscriptionId. Object: InlineResponse2002 Response Code: 200 OK
     */
    @GetMapping(value = "/subscriptions/{subscriptionId}")
    public ResponseEntity<InlineResponse2002> getSubscription(
            @PathVariable("subscriptionId") final String subscriptionId) {
        logger.info(LOG_REQUEST_RECEIVED, " Getting Subscription: ", subscriptionId);
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * DELETE a specific subscription, by subscriptionId. Section Number: 10.4.7
     * 
     * @param subscriptionId The ID of the subscription that you wish to delete.
     * @return Empty response if successful. Object: Void Response Code: 204 No Content
     */
    @DeleteMapping(value = "/subscriptions/{subscriptionId}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable("subscriptionId") final String subscriptionId) {
        logger.info(LOG_REQUEST_RECEIVED, " Deleting Subscription: ", subscriptionId);
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


}
