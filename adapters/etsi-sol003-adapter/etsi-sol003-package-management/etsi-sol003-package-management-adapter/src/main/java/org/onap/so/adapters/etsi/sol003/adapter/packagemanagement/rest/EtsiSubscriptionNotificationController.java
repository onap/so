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

package org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.rest;

import static org.onap.so.adapters.etsi.sol003.adapter.common.CommonConstants.ETSI_SUBSCRIPTION_NOTIFICATION_CONTROLLER_BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap;
import java.util.Map.Entry;
import javax.ws.rs.core.MediaType;
import org.onap.so.adapters.etsi.sol003.adapter.etsicatalog.notification.model.PkgChangeNotification;
import org.onap.so.adapters.etsi.sol003.adapter.etsicatalog.notification.model.PkgOnboardingNotification;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.rest.exceptions.InternalServerErrorException;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.rest.exceptions.NotificationTypeNotSupportedException;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.subscriptionmanagement.NotificationManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.time.LocalDateTime;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * This controller handles the ETSI Subscription Notification Endpoints.
 *
 * @author Ronan Kenny (ronan.kenny@est.tech)
 * @author Gareth Roper (gareth.roper@est.tech)
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 */
@Controller
@RequestMapping(value = ETSI_SUBSCRIPTION_NOTIFICATION_CONTROLLER_BASE_URL)
public class EtsiSubscriptionNotificationController {

    private static final Logger logger = getLogger(EtsiSubscriptionNotificationController.class);
    private final NotificationManager notificationManager;
    private final Gson gson;

    @Autowired
    public EtsiSubscriptionNotificationController(final NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter()).create();
    }

    @GetMapping(value = "/notification")
    public ResponseEntity<Void> testSubscriptionNotificationEndPoint() {
        logger.debug("Testing Notification Endpoint");
        return ResponseEntity.noContent().build();
    }

    /**
     * POST notification on to subscriber.
     *
     * @param notification The notification to send.
     * @return Response Code: 204 No Content if Successful, ProblemDetails Object if not.
     */
    @PostMapping(value = "/notification", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<?> postSubscriptionNotification(@RequestBody final Object notification) {
        logger.info("Posting subscription notification class: {} \n{}", notification.getClass(), notification);
        final String notificationString = gson.toJson(notification);

        final Entry<String, Object> notificationObject = getNotificationObject(notificationString);
        if (notificationManager.processSubscriptionNotification(notificationObject.getValue(),
                notificationObject.getKey())) {
            logger.info("Notification Delivered Successfully");
            return ResponseEntity.noContent().build();
        }
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed.";
        logger.error(errorMessage);
        throw new InternalServerErrorException(errorMessage);
    }

    private Entry<String, Object> getNotificationObject(final String notification) {
        logger.info("getNotificationObject() notification: {}", notification);
        final String notificationType = getNotificationType(notification);
        if (PkgOnboardingNotification.NotificationTypeEnum.VNFPACKAGEONBOARDINGNOTIFICATION.getValue()
                .equals(notificationType)) {
            final PkgOnboardingNotification pkgOnboardingNotification =
                    gson.fromJson(notification, PkgOnboardingNotification.class);
            logger.info("Onboarding notification received:\n{}", pkgOnboardingNotification);
            return new AbstractMap.SimpleEntry<>(pkgOnboardingNotification.getSubscriptionId(),
                    pkgOnboardingNotification);
        }
        if (PkgChangeNotification.NotificationTypeEnum.VNFPACKAGECHANGENOTIFICATION.getValue()
                .equals(notificationType)) {
            final PkgChangeNotification pkgChangeNotification =
                    gson.fromJson(notification, PkgChangeNotification.class);
            logger.info("Change notification received:\n{}", pkgChangeNotification);
            return new AbstractMap.SimpleEntry<>(pkgChangeNotification.getSubscriptionId(), pkgChangeNotification);

        }

        final String errorMessage = "An error occurred.  Notification type not supported for: " + notificationType;
        logger.error(errorMessage);
        throw new NotificationTypeNotSupportedException(errorMessage);

    }

    private String getNotificationType(final String notification) {
        try {
            logger.info("getNotificationType() notification: {}", notification);
            final JsonParser parser = new JsonParser();
            final JsonObject element = (JsonObject) parser.parse(notification);
            return element.get("notificationType").getAsString();
        } catch (final Exception e) {
            logger.error("An error occurred processing notificiation: {}", e.getMessage());
        }
        throw new NotificationTypeNotSupportedException(
                "Unable to parse notification type in object \n" + notification);
    }

    public static class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public void write(final JsonWriter out, final LocalDateTime localDateTime) throws IOException {
            if (localDateTime == null) {
                out.nullValue();
            } else {
                out.value(FORMATTER.format(localDateTime));
            }
        }

        @Override
        public LocalDateTime read(final JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    final String dateTime = in.nextString();
                    return LocalDateTime.parse(dateTime, FORMATTER);
            }
        }
    }

}
