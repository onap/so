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

package org.onap.so.adapters.etsisol003adapter.pkgm.subscriptionmanagement;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.List;
import java.util.Optional;
import org.onap.so.adapters.etsisol003adapter.etsicatalog.notification.model.PkgChangeNotification;
import org.onap.so.adapters.etsisol003adapter.etsicatalog.notification.model.PkgOnboardingNotification;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.vnfm.notification.model.VnfPackageChangeNotification;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.vnfm.notification.model.VnfPackageOnboardingNotification;
import org.onap.so.adapters.etsisol003adapter.pkgm.model.PkgmSubscriptionRequest;
import org.onap.so.adapters.etsisol003adapter.pkgm.model.SubscriptionsAuthentication;
import org.onap.so.adapters.etsisol003adapter.pkgm.model.SubscriptionsAuthentication.AuthTypeEnum;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.AuthenticationTypeNotSupportedException;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.ConversionFailedException;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.NotificationTypeNotSupportedException;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.SubscriptionNotFoundException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

/**
 * Manages package management subscription notifications to the VNFMs
 *
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 *
 */
@Service
public class NotificationManager {

    private static final Logger logger = getLogger(NotificationManager.class);
    private final ConversionService conversionService;
    private final SubscriptionManager subscriptionManager;
    private final NotificationServiceProviderFactory notificationServiceProviderFactory;

    @Autowired
    public NotificationManager(final SubscriptionManager subscriptionManager, final ConversionService conversionService,
            final NotificationServiceProviderFactory notificationServiceProviderFactory) {
        this.subscriptionManager = subscriptionManager;
        this.conversionService = conversionService;
        this.notificationServiceProviderFactory = notificationServiceProviderFactory;
    }

    /**
     * Process a subscription notification. Checks for a subscription request stored in the adapter and if there is, it
     * sends the notification to the subscribed vnfm.
     * 
     * @param notification the notification to send to the vnfm
     * @param subscriptionId the id of the subscription request
     * @return true if the notification is successfully sent
     */
    public boolean processSubscriptionNotification(final Object notification, final String subscriptionId) {
        final Optional<PkgmSubscriptionRequest> optionalSubscription =
                subscriptionManager.getSubscriptionRequest(subscriptionId);
        if (optionalSubscription.isPresent()) {
            final PkgmSubscriptionRequest subscriptionRequest = optionalSubscription.get();
            return notifyVnfm(subscriptionRequest, notification);
        }
        final String errorMessage = "No subscription found with subscriptionId " + subscriptionId
                + ". Unable to forward notification to subscriber.";
        logger.error(errorMessage);
        throw new SubscriptionNotFoundException(errorMessage);
    }

    private boolean notifyVnfm(final PkgmSubscriptionRequest subscriptionRequest, final Object notification) {
        if (!(notification instanceof PkgOnboardingNotification) && !(notification instanceof PkgChangeNotification)) {
            final String errorMessage =
                    "An error occurred.  Notification type not supported for: " + notification.getClass();
            logger.error(errorMessage);
            throw new NotificationTypeNotSupportedException(errorMessage);
        }

        final SubscriptionsAuthentication subscriptionsAuthentication = subscriptionRequest.getAuthentication();
        final AuthTypeEnum authType = getAuthType(subscriptionsAuthentication.getAuthType());
        final NotificationServiceProvider sender = notificationServiceProviderFactory.getNotificationSender(authType);

        final Object vnfmNotificationObject = convertEtsiCatalogNotification(notification);

        if (sender.send(vnfmNotificationObject, subscriptionsAuthentication, subscriptionRequest.getCallbackUri())) {
            logger.info("Notification delivered successfully {}", notification);
            return true;
        }

        logger.error("Failed to deliver notification.");
        return false;
    }

    private SubscriptionsAuthentication.AuthTypeEnum getAuthType(final List<AuthTypeEnum> authTypes) {
        if (authTypes.contains(SubscriptionsAuthentication.AuthTypeEnum.TLS_CERT)) {
            return SubscriptionsAuthentication.AuthTypeEnum.TLS_CERT;
        }
        if (authTypes.contains(SubscriptionsAuthentication.AuthTypeEnum.OAUTH2_CLIENT_CREDENTIALS)) {
            return SubscriptionsAuthentication.AuthTypeEnum.OAUTH2_CLIENT_CREDENTIALS;
        }
        if (authTypes.contains(SubscriptionsAuthentication.AuthTypeEnum.BASIC)) {
            return SubscriptionsAuthentication.AuthTypeEnum.BASIC;
        }
        final String errorMessage =
                "An error occurred. No supported authentication type provided in subscription request.";
        logger.error(errorMessage);
        throw new AuthenticationTypeNotSupportedException(errorMessage);
    }

    private Object convertEtsiCatalogNotification(final Object etsiCatalogNotification) {
        logger.info("Converting notification:\n {}", etsiCatalogNotification);
        if (conversionService.canConvert(etsiCatalogNotification.getClass(), VnfPackageOnboardingNotification.class)) {
            return conversionService.convert(etsiCatalogNotification, VnfPackageOnboardingNotification.class);
        } else if (conversionService.canConvert(etsiCatalogNotification.getClass(),
                VnfPackageChangeNotification.class)) {
            return conversionService.convert(etsiCatalogNotification, VnfPackageChangeNotification.class);
        }
        final String errorMessage = "An error occurred. Unable to convert provided notification object.";
        logger.error(errorMessage + "\n" + etsiCatalogNotification);
        throw new ConversionFailedException(errorMessage);
    }

}
