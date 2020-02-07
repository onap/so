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

package org.onap.so.adapters.vnfmadapter.packagemanagement.subscriptionmanagement;

import static org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.SubscriptionAuthentication.AuthTypeEnum.BASIC;
import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.onap.so.adapters.vnfmadapter.Constants;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.EtsiCatalogServiceProvider;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.EtsiCatalogUrlProvider;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.BasicAuth;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscription;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.InlineResponse2002;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.PkgmSubscriptionRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsLinks;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.VnfPackagesLinksSelf;
import org.onap.so.adapters.vnfmadapter.packagemanagement.subscriptionmanagement.cache.PackageManagementCacheServiceProvider;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.InternalServerErrorException;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.SubscriptionRequestConversionException;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

/**
 * Manages package management subscriptions from the VNFMs
 *
 * @author Ronan Kenny (ronan.kenny@est.tech)
 * @author Gareth Roper (gareth.roper@est.tech)
 *
 */
@Service
public class SubscriptionManager {

    private static final Logger logger = getLogger(SubscriptionManager.class);
    private final PackageManagementCacheServiceProvider packageManagementCacheServiceProvider;
    private final EtsiCatalogUrlProvider etsiCatalogUrlProvider;
    private final HttpRestServiceProvider httpServiceProvider;
    private final ConversionService conversionService;
    private final EtsiCatalogServiceProvider etsiCatalogServiceProvider;
    private final String vnfmAdapterEndpoint;
    private final String msoKeyString;
    private final String vnfmAdapterAuth;


    @Autowired
    public SubscriptionManager(final PackageManagementCacheServiceProvider packageManagementCacheServiceProvider,
            final ConversionService conversionService, final HttpRestServiceProvider httpServiceProvider,
            final EtsiCatalogUrlProvider etsiCatalogUrlProvider,
            final EtsiCatalogServiceProvider etsiCatalogServiceProvider,
            @Value("${vnfmadapter.endpoint}") final String vnfmAdapterEndpoint,
            @Value("${mso.key}") final String msoKeyString,
            @Value("${vnfmadapter.auth:D6CFE56451508B75536C5E8A1E7AE06D0346006A693BF29293A6E1C762EFD59C671911DB6E9294E4FE15E4C1C5524B}") final String vnfmAdapterAuth) {
        this.packageManagementCacheServiceProvider = packageManagementCacheServiceProvider;
        this.etsiCatalogUrlProvider = etsiCatalogUrlProvider;
        this.conversionService = conversionService;
        this.httpServiceProvider = httpServiceProvider;
        this.etsiCatalogServiceProvider = etsiCatalogServiceProvider;
        this.vnfmAdapterEndpoint = vnfmAdapterEndpoint;
        this.vnfmAdapterAuth = vnfmAdapterAuth;
        this.msoKeyString = msoKeyString;
    }

    public Optional<InlineResponse2002> createSubscription(final PkgmSubscriptionRequest pkgmSubscriptionRequest)
            throws GeneralSecurityException {

        final org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest etsiCatalogManagerSubscriptionRequest =
                buildEtsiCatalogManagerPkgmSubscriptionRequest(pkgmSubscriptionRequest);

        final Optional<PkgmSubscription> optionalEtsiCatalogManagerSubscription =
                etsiCatalogServiceProvider.postSubscription(etsiCatalogManagerSubscriptionRequest);

        if (optionalEtsiCatalogManagerSubscription.isPresent()) {
            PkgmSubscription etsiCatalogManagerSubscription = optionalEtsiCatalogManagerSubscription.get();
            logger.debug("postPkgmSubscriptionRequest Response SubscriptionId: {}",
                    Objects.requireNonNull(etsiCatalogManagerSubscription.getId().toString()));
            final String subscriptionId = etsiCatalogManagerSubscription.getId().toString();

            packageManagementCacheServiceProvider.addSubscription(subscriptionId, pkgmSubscriptionRequest);

            final InlineResponse2002 response2002 = new InlineResponse2002();
            response2002.setId(subscriptionId);
            response2002.setFilter(pkgmSubscriptionRequest.getFilter());
            response2002.setCallbackUri(getSubscriptionUri(subscriptionId).toString());
            response2002.setLinks(new SubscriptionsLinks()
                    .self(new VnfPackagesLinksSelf().href(getSubscriptionUri(subscriptionId).toString())));

            return Optional.of(response2002);
        }
        throw new InternalServerErrorException(
                "Received empty response from POST to ETSI Catalog Manager Subscription Endpoint.");
    }



    public Optional<String> getSubscriptionId(final PkgmSubscriptionRequest pkgmSubscriptionRequest) {
        return packageManagementCacheServiceProvider.getSubscriptionId(pkgmSubscriptionRequest);
    }

    public Optional<InlineResponse2002> getSubscription(final String subscriptionId) {
        final Optional<PkgmSubscriptionRequest> optional =
                packageManagementCacheServiceProvider.getSubscription(subscriptionId);
        if (optional.isPresent()) {
            final PkgmSubscriptionRequest subscription = optional.get();
            return Optional.of(getInlineResponse2002(subscriptionId, subscription));
        }
        return Optional.empty();
    }

    public List<InlineResponse2002> getSubscriptions() {
        final Map<String, PkgmSubscriptionRequest> subscriptions =
                packageManagementCacheServiceProvider.getSubscriptions();
        final List<InlineResponse2002> response = new ArrayList<>();
        subscriptions.forEach((key, value) -> response.add(getInlineResponse2002(key, value)));
        return response;
    }

    public boolean deleteSubscription(final String subscriptionId) {
        if (getSubscription(subscriptionId).isPresent()) {
            if (etsiCatalogServiceProvider.deleteSubscription(subscriptionId)) {
                return packageManagementCacheServiceProvider.deleteSubscription(subscriptionId);
            }
        }
        return false;
    }

    public URI getSubscriptionUri(final String subscriptionId) {
        return URI.create(
                vnfmAdapterEndpoint + Constants.PACKAGE_MANAGEMENT_BASE_URL + "/subscriptions/" + subscriptionId);
    }

    private InlineResponse2002 getInlineResponse2002(final String id, final PkgmSubscriptionRequest subscription) {
        return new InlineResponse2002().id(id).filter(subscription.getFilter())
                .callbackUri(subscription.getCallbackUri());
    }

    private org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest buildEtsiCatalogManagerPkgmSubscriptionRequest(
            PkgmSubscriptionRequest pkgmSubscriptionRequest) throws GeneralSecurityException {

        final org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest etsiCatalogManagerSubscriptionRequest;
        try {
            etsiCatalogManagerSubscriptionRequest = conversionService.convert(pkgmSubscriptionRequest,
                    org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest.class);
        } catch (ConversionException conversionException) {
            logger.error(conversionException.getMessage());
            throw new SubscriptionRequestConversionException(
                    "Could not convert Sol003 PkgmSubscriptionRequest to ETSI-Catalog Manager PkgmSubscriptionRequest");
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            throw new InternalServerErrorException(
                    "Could not convert Sol003 PkgmSubscriptionRequest to ETSI-Catalog Manager PkgmSubscriptionRequest");
        }

        if (etsiCatalogManagerSubscriptionRequest != null) {
            etsiCatalogManagerSubscriptionRequest
                    .setCallbackUri(vnfmAdapterEndpoint + Constants.ETSI_SUBSCRIPTION_NOTIFICATION_BASE_URL);

            final String[] auth = decryptAuth();
            final String username = auth[0];
            final String password = auth[1];

            etsiCatalogManagerSubscriptionRequest.setAuthentication(
                    new org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.SubscriptionAuthentication()
                            .addAuthTypeItem(BASIC).paramsBasic(new BasicAuth().userName(username).password(password)));
            return etsiCatalogManagerSubscriptionRequest;
        }
        throw new SubscriptionRequestConversionException(
                "Failed to convert Sol003 PkgmSubscriptionRequest to ETSI-Catalog Manager PkgmSubscriptionRequest");
    }

    private String[] decryptAuth() throws GeneralSecurityException {
        final String decryptedAuth = CryptoUtils.decrypt(vnfmAdapterAuth, msoKeyString);
        final String[] auth = decryptedAuth.split(":");
        return auth;
    }

}
