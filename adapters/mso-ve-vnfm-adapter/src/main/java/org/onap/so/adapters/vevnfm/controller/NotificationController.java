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

import org.onap.so.adapters.vevnfm.constant.VnfNotificationFilterType;
import org.onap.so.adapters.vevnfm.service.DmaapService;
import org.onap.so.adapters.vevnfm.service.VnfAaiChecker;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Value("${vnf.filter-type}")
    private VnfNotificationFilterType vnfFilterType;

    @Autowired
    private VnfAaiChecker vnfAaiChecker;

    @Autowired
    private DmaapService dmaapService;

    @PostMapping("${vnfm.notification}")
    public ResponseEntity receiveNotification(@RequestBody final VnfLcmOperationOccurrenceNotification notification) {
        logger.info("Notification received {}", notification);

        if (vnfAaiChecker.vnfCheck(vnfFilterType, notification.getId())) {
            dmaapService.send(notification);
        }

        return ResponseEntity.ok().build();
    }

}
