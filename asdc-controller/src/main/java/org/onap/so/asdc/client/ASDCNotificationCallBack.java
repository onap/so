/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

package org.onap.so.asdc.client;

import java.util.Map;
import java.util.Objects;
import org.onap.sdc.api.consumer.INotificationCallback;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.NotificationMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.transport.Kind;
import io.micrometer.observation.transport.ReceiverContext;

/**
 * Inner class for Notification callback
 *
 *
 */
@Component
public final class ASDCNotificationCallBack implements INotificationCallback {

    @Lazy
    @Autowired
    private ASDCController asdcController;

    @Autowired(required = false)
    private ObservationRegistry observationRegistry = ObservationRegistry.NOOP;

    protected static final Logger logger = LoggerFactory.getLogger(ASDCNotificationCallBack.class);

    /**
     * This method can be called multiple times at the same moment. The controller must be thread safe !
     */
    @Override
    public void activateCallback(INotificationData iNotif) {
        String event = "Receive a callback notification in ASDC, nb of resources: " + iNotif.getResources().size();
        logger.debug(event);
        NotificationMessageContext messageContext = iNotif.getMessageContext();
        String topic = Objects.requireNonNullElse(messageContext.getTopic(), "unknown");
        ReceiverContext<Map<String, String>> receiverContext = new ReceiverContext<>(Map::get, Kind.CONSUMER);
        receiverContext.setCarrier(messageContext.getHeaders());
        receiverContext.setRemoteServiceName("sdc");
        Observation.createNotStarted("sdc.notification.process", () -> receiverContext, observationRegistry)
                .contextualName(topic + " process")
                .lowCardinalityKeyValues(KeyValues.of("messaging.system", "kafka", "messaging.operation", "process",
                        "messaging.destination.name", topic))
                .highCardinalityKeyValues(KeyValues.of("messaging.kafka.destination.partition",
                        String.valueOf(messageContext.getPartition()), "messaging.kafka.message.offset",
                        String.valueOf(messageContext.getOffset()), "sdc.distribution.id",
                        String.valueOf(iNotif.getDistributionID())))
                .observe(() -> asdcController.treatNotification(iNotif));
    }
}
