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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.onap.so.adapters.vnfmadapter.VnfmAdapterUrlProvider;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.EtsiCatalogServiceProvider;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.BasicAuth;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.NsdmSubscription;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscription;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.PkgmSubscriptionRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsLinks;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.VnfPackagesLinksSelf;
import org.onap.so.adapters.vnfmadapter.packagemanagement.subscriptionmanagement.cache.PackageManagementCacheServiceProvider;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.ConversionFailedException;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.InternalServerErrorException;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.SubscriptionNotFoundException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

/**
 * Manages package management subscriptions from the VNFMs
 *
 * @author Ronan Kenny (ronan.kenny@est.tech)
 * @author Gareth Roper (gareth.roper@est.tech)
 */
@Service
public class SubscriptionManager {

    private static final Logger logger = getLogger(SubscriptionManager.class);
    private final PackageManagementCacheServiceProvider packageManagementCacheServiceProvider;
    private final ConversionService conversionService;
    private final EtsiCatalogServiceProvider etsiCatalogServiceProvider;
    private final VnfmAdapterUrlProvider vnfmAdapterUrlProvider;

    @Autowired
    public SubscriptionManager(final PackageManagementCacheServiceProvider packageManagementCacheServiceProvider,
            final ConversionService conversionService, final EtsiCatalogServiceProvider etsiCatalogServiceProvider,
            final VnfmAdapterUrlProvider vnfmAdapterUrlProvider) {
        this.packageManagementCacheServiceProvider = packageManagementCacheServiceProvider;
        this.conversionService = conversionService;
        this.etsiCatalogServiceProvider = etsiCatalogServiceProvider;
        this.vnfmAdapterUrlProvider = vnfmAdapterUrlProvider;
    }

    public Optional<InlineResponse201> createSubscription(final PkgmSubscriptionRequest pkgmSubscriptionRequest)
            throws GeneralSecurityException {

        final org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest etsiCatalogManagerSubscriptionRequest =
                buildEtsiCatalogManagerPkgmSubscriptionRequest(pkgmSubscriptionRequest);

        final Optional<PkgmSubscription> optionalEtsiCatalogManagerSubscription =
                etsiCatalogServiceProvider.postSubscription(etsiCatalogManagerSubscriptionRequest);

        if (optionalEtsiCatalogManagerSubscription.isPresent()) {
            final PkgmSubscription etsiCatalogManagerSubscription = optionalEtsiCatalogManagerSubscription.get();
            logger.debug("postPkgmSubscriptionRequest Response SubscriptionId: {}",
                    Objects.requireNonNull(etsiCatalogManagerSubscription.getId()));
            final String subscriptionId = etsiCatalogManagerSubscription.getId();

            packageManagementCacheServiceProvider.addSubscription(subscriptionId, pkgmSubscriptionRequest);

            final InlineResponse201 response = new InlineResponse201();
            response.setId(subscriptionId);
            response.setFilter(pkgmSubscriptionRequest.getFilter());
            response.setCallbackUri(vnfmAdapterUrlProvider.getSubscriptionUriString(subscriptionId));
            response.setLinks(new SubscriptionsLinks()
                    .self(new VnfPackagesLinksSelf().href(getSubscriptionUri(subscriptionId).toString())));

            return Optional.of(response);
        }
        throw new InternalServerErrorException(
                "Received empty response from POST to ETSI Catalog Manager Subscription Endpoint.");
    }

    public Optional<String> getSubscriptionId(final PkgmSubscriptionRequest pkgmSubscriptionRequest) {
        return packageManagementCacheServiceProvider.getSubscriptionId(pkgmSubscriptionRequest);
    }

    public Optional<InlineResponse201> getSubscription(final String subscriptionId) {

        logger.debug("Checking if subscrition with id: {} exists in ETSI Catalog Manager", subscriptionId);
        final Optional<NsdmSubscription> etsiCatalogSubscriptionOption =
                etsiCatalogServiceProvider.getSubscription(subscriptionId);

        if (!etsiCatalogSubscriptionOption.isPresent()) {
            logger.debug("Unable to find subscription in ETSI Catalog Manager using id: {}", subscriptionId);
            if (packageManagementCacheServiceProvider.getSubscription(subscriptionId).isPresent()) {
                logger.debug("will remove subcription with id: {} from local cache", subscriptionId);
                packageManagementCacheServiceProvider.deleteSubscription(subscriptionId);
            }
        }

        final Optional<PkgmSubscriptionRequest> optional =
                packageManagementCacheServiceProvider.getSubscription(subscriptionId);
        if (optional.isPresent()) {
            final PkgmSubscriptionRequest subscription = optional.get();
            return Optional.of(getInlineResponse2002(subscriptionId, subscription));
        }
        return Optional.empty();
    }

    public List<InlineResponse201> getSubscriptions() {
        final Map<String, PkgmSubscriptionRequest> subscriptions =
                packageManagementCacheServiceProvider.getSubscriptions();
        final List<InlineResponse201> response = new ArrayList<>();
        subscriptions.forEach((key, value) -> {
            final Optional<InlineResponse201> optional = getSubscription(key);
            if (optional.isPresent()) {
                response.add(optional.get());
            }
        });
        return response;
    }

    public boolean deleteSubscription(final String subscriptionId) {
        if (packageManagementCacheServiceProvider.getSubscription(subscriptionId).isPresent()) {
            try {
                if (etsiCatalogServiceProvider.deleteSubscription(subscriptionId)) {
                    return packageManagementCacheServiceProvider.deleteSubscription(subscriptionId);
                }
            } catch (final SubscriptionNotFoundException subscriptionNotFoundException) {
                logger.error(
                        "Unable to find subscription in ETSI Catalog Manager using id: {} will delete it from local cache",
                        subscriptionId);
                return packageManagementCacheServiceProvider.deleteSubscription(subscriptionId);
            }
        }
        return false;
    }

    public URI getSubscriptionUri(final String subscriptionId) {
        return vnfmAdapterUrlProvider.getSubscriptionUri(subscriptionId);
    }

    public Optional<PkgmSubscriptionRequest> getSubscriptionRequest(final String subscriptionId) {
        return packageManagementCacheServiceProvider.getSubscription(subscriptionId);
    }

    private InlineResponse201 getInlineResponse2002(final String id, final PkgmSubscriptionRequest subscription) {
        return new InlineResponse201().id(id).filter(subscription.getFilter())
                .callbackUri(subscription.getCallbackUri());
    }

    private org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest buildEtsiCatalogManagerPkgmSubscriptionRequest(
            final PkgmSubscriptionRequest pkgmSubscriptionRequest) throws GeneralSecurityException {

        final org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest etsiCatalogManagerSubscriptionRequest;
        try {
            etsiCatalogManagerSubscriptionRequest = conversionService.convert(pkgmSubscriptionRequest,
                    org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest.class);
        } catch (final org.springframework.core.convert.ConversionException conversionException) {
            logger.error(conversionException.getMessage());
            throw new ConversionFailedException(
                    "Could not convert Sol003 PkgmSubscriptionRequest to ETSI-Catalog Manager PkgmSubscriptionRequest");
        } catch (final Exception exception) {
            logger.error(exception.getMessage());
            throw new InternalServerErrorException(
                    "Could not convert Sol003 PkgmSubscriptionRequest to ETSI-Catalog Manager PkgmSubscriptionRequest");
        }

        if (etsiCatalogManagerSubscriptionRequest != null) {
            etsiCatalogManagerSubscriptionRequest
                    .setCallbackUri(vnfmAdapterUrlProvider.getEtsiSubscriptionNotificationBaseUrl());

            final ImmutablePair<String, String> immutablePair = vnfmAdapterUrlProvider.getDecryptAuth();
            if (!immutablePair.equals(ImmutablePair.nullPair())) {
                etsiCatalogManagerSubscriptionRequest.setAuthentication(
                        new org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.SubscriptionAuthentication()
                                .addAuthTypeItem(BASIC).paramsBasic(new BasicAuth().userName(immutablePair.getLeft())
                                        .password(immutablePair.getRight())));
            }
            return etsiCatalogManagerSubscriptionRequest;
        }
        throw new ConversionFailedException(
                "Failed to convert Sol003 PkgmSubscriptionRequest to ETSI-Catalog Manager PkgmSubscriptionRequest");
    }

}
