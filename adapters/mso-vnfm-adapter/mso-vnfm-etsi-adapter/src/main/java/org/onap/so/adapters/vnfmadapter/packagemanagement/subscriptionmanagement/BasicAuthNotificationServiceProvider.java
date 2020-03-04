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
package org.onap.so.adapters.vnfmadapter.packagemanagement.subscriptionmanagement;

import static org.slf4j.LoggerFactory.getLogger;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsAuthentication;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsAuthentication.AuthTypeEnum;
import org.onap.so.configuration.rest.HttpHeadersProvider;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Implementation of a NotificationServiceProvider which supports Basic Authentication
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 */
@Service
public class BasicAuthNotificationServiceProvider extends AbstractNotificationServiceProvider
        implements NotificationServiceProvider {

    private static final Logger logger = getLogger(BasicAuthNotificationServiceProvider.class);

    @Override
    public boolean send(final Object notification, final SubscriptionsAuthentication subscriptionsAuthentication,
            final String callbackUri) {
        logger.info("Sending notification to uri: {}", callbackUri);
        final HttpHeadersProvider httpHeadersProvider =
                getBasicHttpHeadersProviderWithBasicAuth(subscriptionsAuthentication.getParamsBasic().getUserName(),
                        subscriptionsAuthentication.getParamsBasic().getPassword());
        final HttpRestServiceProvider httpRestServiceProvider = getHttpRestServiceProvider();

        final ResponseEntity<Void> responseEntity = httpRestServiceProvider.postHttpRequest(notification, callbackUri,
                httpHeadersProvider.getHttpHeaders(), Void.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            logger.info("Notification sent successfully.");
            return true;
        }

        logger.error("Failed to send notification.");
        return false;
    }

    @Override
    public AuthTypeEnum getAuthType() {
        return AuthTypeEnum.BASIC;
    }

}
