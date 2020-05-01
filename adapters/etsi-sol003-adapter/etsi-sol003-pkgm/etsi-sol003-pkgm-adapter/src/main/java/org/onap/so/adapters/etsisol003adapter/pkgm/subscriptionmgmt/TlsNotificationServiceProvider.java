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
import org.onap.so.adapters.etsisol003adapter.pkgm.model.SubscriptionsAuthentication;
import org.onap.so.adapters.etsisol003adapter.pkgm.model.SubscriptionsAuthentication.AuthTypeEnum;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.AuthenticationTypeNotSupportedException;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 */
@Service
public class TlsNotificationServiceProvider extends AbstractNotificationServiceProvider
        implements NotificationServiceProvider {

    private static final Logger logger = getLogger(TlsNotificationServiceProvider.class);

    @Override
    public boolean send(final Object notification, final SubscriptionsAuthentication subscriptionsAuthentication,
            final String callbackUri) {
        final String errorMessage = "An error occurred.  Authentication type "
                + subscriptionsAuthentication.getAuthType().toString() + " not currently supported.";
        logger.error(errorMessage);
        throw new AuthenticationTypeNotSupportedException(errorMessage);
    }


    @Override
    public AuthTypeEnum getAuthType() {
        return AuthTypeEnum.TLS_CERT;
    }



}
