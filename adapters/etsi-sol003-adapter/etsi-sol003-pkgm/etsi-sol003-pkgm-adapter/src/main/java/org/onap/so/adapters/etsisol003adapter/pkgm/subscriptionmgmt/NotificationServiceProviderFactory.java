/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
package org.onap.so.adapters.etsisol003adapter.pkgm.subscriptionmgmt;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.so.adapters.etsisol003adapter.pkgm.model.SubscriptionsAuthentication.AuthTypeEnum;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.AuthenticationTypeNotSupportedException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory to provide a notification services
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 */
@Component
public class NotificationServiceProviderFactory {

    private static final Logger logger = getLogger(NotificationServiceProviderFactory.class);
    private static final Map<AuthTypeEnum, NotificationServiceProvider> CACHE = new HashMap<>();

    @Autowired
    public NotificationServiceProviderFactory(final List<NotificationServiceProvider> services) {
        for (final NotificationServiceProvider notificationServiceProvider : services) {
            logger.debug("Adding {} of type {} to cache", notificationServiceProvider.getClass().getCanonicalName(),
                    notificationServiceProvider.getAuthType());
            CACHE.put(notificationServiceProvider.getAuthType(), notificationServiceProvider);
        }
    }

    /**
     * Get a notification service for a given authorization type
     * 
     * @param type the type of authentication required
     * @return the notification service
     */
    public NotificationServiceProvider getNotificationSender(final AuthTypeEnum type) {
        final NotificationServiceProvider service = CACHE.get(type);
        if (service == null) {
            throw new AuthenticationTypeNotSupportedException("Unknown type: " + type);
        }
        return service;
    }

}
