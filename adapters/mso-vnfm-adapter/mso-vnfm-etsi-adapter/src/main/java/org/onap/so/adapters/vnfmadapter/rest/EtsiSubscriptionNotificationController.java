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

import static org.onap.so.adapters.vnfmadapter.Constants.ETSI_SUBSCRIPTION_NOTIFICATION_CONTROLLER_BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This controller handles the ETSI Subscription Notification Endpoints.
 *
 * @author Ronan Kenny (ronan.kenny@est.tech)
 * @author Gareth Roper (gareth.roper@est.tech)
 */
@Controller
@RequestMapping(value = ETSI_SUBSCRIPTION_NOTIFICATION_CONTROLLER_BASE_URL)
public class EtsiSubscriptionNotificationController {

    private static final Logger logger = getLogger(EtsiSubscriptionNotificationController.class);

    @GetMapping(value = "/notification")
    public ResponseEntity<Void> testSubscriptionNotificationEndPoint() {
        logger.debug("Testing Notification Endpoint");
        return ResponseEntity.noContent().build();
    }
}
