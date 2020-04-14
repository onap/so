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
package org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.subscriptionmanagement;

import static org.slf4j.LoggerFactory.getLogger;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.SubscriptionsAuthentication;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.SubscriptionsAuthentication.AuthTypeEnum;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.rest.exceptions.InternalServerErrorException;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.onap.so.configuration.rest.HttpHeadersProvider;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 *
 */
@Service
public class OAuthNotificationServiceProvider extends AbstractNotificationServiceProvider
        implements NotificationServiceProvider {

    private static final Logger logger = getLogger(OAuthNotificationServiceProvider.class);

    @Override
    public boolean send(final Object notification, final SubscriptionsAuthentication subscriptionsAuthentication,
            final String callbackUri) {
        logger.info("Sending notification to uri: {}", callbackUri);
        final String token = getAccessToken(subscriptionsAuthentication);

        if (token == null) {
            logger.error("Failed to get access token");
            return false;
        }

        final HttpHeadersProvider httpHeadersProvider = getHttpHeadersProvider(token);
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
        return AuthTypeEnum.OAUTH2_CLIENT_CREDENTIALS;
    }

    private BasicHttpHeadersProvider getHttpHeadersProvider(final String token) {
        final String authHeader = "Bearer " + token;
        return new BasicHttpHeadersProvider(authHeader);
    }

    private String getAccessToken(final SubscriptionsAuthentication subscriptionsAuthentication) {
        logger.info("Requesting Access Token.");

        final String tokenEndpoint = subscriptionsAuthentication.getParamsOauth2ClientCredentials().getTokenEndpoint();

        final HttpHeadersProvider httpHeadersProvider = getBasicHttpHeadersProviderWithBasicAuth(
                subscriptionsAuthentication.getParamsOauth2ClientCredentials().getClientId(),
                subscriptionsAuthentication.getParamsOauth2ClientCredentials().getClientPassword());

        final HttpRestServiceProvider httpRestServiceProvider = getHttpRestServiceProvider();
        final ResponseEntity<OAuthTokenResponse> responseEntity = httpRestServiceProvider.postHttpRequest(null,
                tokenEndpoint, httpHeadersProvider.getHttpHeaders(), OAuthTokenResponse.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (responseEntity.getBody() != null) {
                logger.info("Returning Access Token.");
                return responseEntity.getBody().getAccessToken();
            }
        }

        final String errorMessage = "An error occurred.  Unable to retrieve OAuth Token from VNFM for notification.";
        logger.error(errorMessage);
        throw new InternalServerErrorException(errorMessage);
    }


}
