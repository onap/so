/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.service;

import org.apache.logging.log4j.util.Strings;
import org.onap.so.adapters.etsisol003adapter.lcm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.adapters.vevnfm.aai.AaiConnection;
import org.onap.so.adapters.vevnfm.configuration.ConfigProperties;
import org.onap.so.adapters.vevnfm.constant.NotificationVnfFilterType;
import org.onap.so.adapters.vevnfm.event.DmaapEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DmaapConditionalSender {

    private static final Logger logger = LoggerFactory.getLogger(DmaapConditionalSender.class);

    private final NotificationVnfFilterType notificationVnfFilterType;
    private final AaiConnection aaiConnection;
    private final DmaapService dmaapService;

    public DmaapConditionalSender(final ConfigProperties configProperties, final AaiConnection aaiConnection,
            final DmaapService dmaapService) {
        this.notificationVnfFilterType = configProperties.getNotificationVnfFilterType();
        this.aaiConnection = aaiConnection;
        this.dmaapService = dmaapService;
    }

    public void send(final VnfLcmOperationOccurrenceNotification notification) {
        final String href = notification.getLinks().getVnfInstance().getHref();
        boolean logSent = false;

        switch (notificationVnfFilterType) {
            case ALL:
                logSent = conditionalSend(true, notification, href);
                break;
            case AAI_CHECKED:
                logSent = conditionalSend(false, notification, href);
                break;
            case NONE:
                break;
            default:
                throw new IllegalArgumentException(
                        "The value of VnfNotificationFilterType is not supported: " + notificationVnfFilterType);
        }

        final String vnfInstanceId = notification.getVnfInstanceId();
        final String not = logSent ? "" : "not ";
        logger.info("The info with the VNF id '{}' is " + not + "sent to DMaaP", vnfInstanceId);
    }

    private boolean conditionalSend(final boolean allowAll, final VnfLcmOperationOccurrenceNotification notification,
            final String href) {
        final String genericId = aaiConnection.receiveGenericVnfId(href);
        final boolean idNotBlank = Strings.isNotBlank(genericId);
        String vserverName = null;

        if (idNotBlank) {
            vserverName = aaiConnection.receiveVserverName(genericId);
        }

        if (allowAll || idNotBlank) {
            final DmaapEvent dmaapEvent = dmaapService.createDmaapEvent(notification, vserverName, genericId);
            dmaapService.send(dmaapEvent);
            return true;
        }

        return false;
    }
}
