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
package org.onap.so.adapters.vnfmadapter.common;

import static org.onap.so.adapters.vnfmadapter.common.CommonConstants.BASE_URL;
import static org.onap.so.adapters.vnfmadapter.common.CommonConstants.ETSI_SUBSCRIPTION_NOTIFICATION_BASE_URL;
import static org.onap.so.adapters.vnfmadapter.common.CommonConstants.OPERATION_NOTIFICATION_ENDPOINT;
import static org.onap.so.adapters.vnfmadapter.common.CommonConstants.PACKAGE_MANAGEMENT_BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import java.security.GeneralSecurityException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * Provides VNFM Adapter endpoint URLs
 * 
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
public class VnfmAdapterUrlProvider {

    private static final Logger logger = getLogger(VnfmAdapterUrlProvider.class);
    private static final String COLON = ":";
    private static final int LIMIT = 2;

    private final String vnfmAdapterEndpoint;
    private final String msoKeyString;
    private final String vnfmAdapterAuth;

    @Autowired
    public VnfmAdapterUrlProvider(@Value("${vnfmadapter.endpoint}") final String vnfmAdapterEndpoint,
            @Value("${mso.key}") final String msoKeyString,
            @Value("${vnfmadapter.auth:BF29BA36F0CFE1C05507781F6B97EFBCA7EFAC9F595954D465FC43F646883EF585C20A58CBB02528A6FAAC}") final String vnfmAdapterAuth) {
        this.vnfmAdapterEndpoint = vnfmAdapterEndpoint;
        this.msoKeyString = msoKeyString;
        this.vnfmAdapterAuth = vnfmAdapterAuth;
    }

    public String getEtsiSubscriptionNotificationBaseUrl() {
        return vnfmAdapterEndpoint + ETSI_SUBSCRIPTION_NOTIFICATION_BASE_URL;
    }

    public URI getSubscriptionUri(final String subscriptionId) {
        return URI.create(getSubscriptionUriString(subscriptionId));
    }

    public ImmutablePair<String, String> getDecryptAuth() throws GeneralSecurityException {
        final String decryptedAuth = CryptoUtils.decrypt(vnfmAdapterAuth, msoKeyString);
        final String[] auth = decryptedAuth.split(COLON, LIMIT);
        if (auth.length > 1) {
            return ImmutablePair.of(auth[0], auth[1]);
        }
        logger.error("Unexpected auth value: {}", vnfmAdapterAuth);
        return ImmutablePair.nullPair();
    }

    public String getSubscriptionUriString(final String subscriptionId) {
        return vnfmAdapterEndpoint + PACKAGE_MANAGEMENT_BASE_URL + "/subscriptions/" + subscriptionId;
    }

    public String getVnfLcmOperationOccurrenceNotificationUrl() {
        return vnfmAdapterEndpoint + BASE_URL + OPERATION_NOTIFICATION_ENDPOINT;
    }

    public String getVnfPackageUrl(final String vnfPkgId) {
        return vnfmAdapterEndpoint + PACKAGE_MANAGEMENT_BASE_URL + "/vnf_packages/" + vnfPkgId;
    }

    public String getVnfPackageVnfdUrl(final String vnfPkgId) {
        return getVnfPackageUrl(vnfPkgId) + "/vnfd";
    }

    public String getVnfPackageContentUrl(final String vnfPkgId) {
        return getVnfPackageUrl(vnfPkgId) + "/package_content";
    }

    public String getOauthTokenUrl() {
        return vnfmAdapterEndpoint + "/oauth/token";
    }

}
