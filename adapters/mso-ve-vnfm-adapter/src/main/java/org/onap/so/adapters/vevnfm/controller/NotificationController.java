/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.vevnfm.controller;

import org.onap.so.adapters.etsi.sol003.adapter.lcm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.adapters.vevnfm.configuration.ConfigProperties;
import org.onap.so.adapters.vevnfm.constant.NotificationVnfFilterType;
import org.onap.so.adapters.vevnfm.service.DmaapService;
import org.onap.so.adapters.vevnfm.service.VnfAaiChecker;
import org.onap.so.adapters.vevnfm.util.StringUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationVnfFilterType notificationVnfFilterType;
    private final VnfAaiChecker vnfAaiChecker;
    private final DmaapService dmaapService;

    @Autowired
    public NotificationController(final ConfigProperties configProperties, final VnfAaiChecker vnfAaiChecker,
            final DmaapService dmaapService) {
        this.notificationVnfFilterType = configProperties.getNotificationVnfFilterType();
        this.vnfAaiChecker = vnfAaiChecker;
        this.dmaapService = dmaapService;
    }

    @PostMapping("${vnfm.notification}")
    public ResponseEntity receiveNotification(@RequestBody final VnfLcmOperationOccurrenceNotification notification) {
        logger.info("Notification received {}", notification);

        final String vnfInstanceId = notification.getVnfInstanceId();
        final StringUsage genericId = vnfAaiChecker.vnfCheck(notificationVnfFilterType, vnfInstanceId);

        if (genericId.isPresent()) {
            logger.info("The info with the VNF id '{}' is sent to DMaaP", vnfInstanceId);
            dmaapService.send(notification, genericId.get());
        } else {
            logger.info("This VNF id '{}' is not sent to DMaaP", vnfInstanceId);
        }

        return ResponseEntity.ok().build();
    }
}
