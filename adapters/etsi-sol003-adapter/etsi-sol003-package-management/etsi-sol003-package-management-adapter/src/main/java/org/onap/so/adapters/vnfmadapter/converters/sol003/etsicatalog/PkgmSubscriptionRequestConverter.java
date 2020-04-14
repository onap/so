/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.adapters.vnfmadapter.converters.sol003.etsicatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.Version;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.VnfProducts;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.VnfProductsProviders;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.PkgmSubscriptionRequest;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.SubscriptionsFilter1;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.SubscriptionsFilter1.NotificationTypesEnum;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.SubscriptionsFilter1.OperationalStateEnum;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.SubscriptionsFilter1.UsageStateEnum;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.SubscriptionsFilterVersions;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.SubscriptionsFilterVnfProducts;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.SubscriptionsFilterVnfProductsFromProviders;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

/**
 * Converter to convert from an Etsi Catalog Manager {@link PkgmSubscriptionRequest} Object to its equivalent ETSI
 * Catalog Manager Object
 *
 * @author Ronan Kenny (ronan.kenny@est.tech)
 * @author Gareth Roper (gareth.roper@est.tech)
 */
@Service
public class PkgmSubscriptionRequestConverter implements
        Converter<PkgmSubscriptionRequest, org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest> {

    @Override
    public org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest convert(
            final PkgmSubscriptionRequest pkgmSubscriptionRequest) {
        final org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest etsiCatalogManagerSubscriptionRequest =
                new org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscriptionRequest();

        etsiCatalogManagerSubscriptionRequest
                .setFilter(getPkgmNotificationsFilter(pkgmSubscriptionRequest.getFilter()));

        return etsiCatalogManagerSubscriptionRequest;
    }


    private org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter getPkgmNotificationsFilter(
            final SubscriptionsFilter1 sol003SubscriptionsFilter) {
        final org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter etsiCatalogManagerFilters =
                new org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter();

        if (sol003SubscriptionsFilter.getNotificationTypes() != null) {
            etsiCatalogManagerFilters.setNotificationTypes(
                    getPkgmNotificationsFilterNotificationTypes(sol003SubscriptionsFilter.getNotificationTypes()));
        }

        etsiCatalogManagerFilters.setVnfProductsFromProviders(
                getVnfProductsProviders(sol003SubscriptionsFilter.getVnfProductsFromProviders()));

        etsiCatalogManagerFilters.setVnfdId(getVnfdIds(sol003SubscriptionsFilter.getVnfdId()));

        etsiCatalogManagerFilters.setVnfPkgId(getVnfPkgIds(sol003SubscriptionsFilter.getVnfPkgId()));

        etsiCatalogManagerFilters
                .setOperationalState(getOperationalState(sol003SubscriptionsFilter.getOperationalState()));

        etsiCatalogManagerFilters.setUsageState(getUsageState(sol003SubscriptionsFilter.getUsageState()));

        return etsiCatalogManagerFilters;
    }


    private List<org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter.UsageStateEnum> getUsageState(
            final List<UsageStateEnum> usageStates) {
        if (usageStates != null) {
            final List<org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter.UsageStateEnum> etsiCatalogUsageStates =
                    new ArrayList<>();
            usageStates.stream().forEach(state -> {
                etsiCatalogUsageStates.add(
                        org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter.UsageStateEnum
                                .fromValue(state.getValue()));
            });
            return etsiCatalogUsageStates;
        }
        return Collections.emptyList();
    }


    private List<org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter.OperationalStateEnum> getOperationalState(
            final List<OperationalStateEnum> operationalStates) {
        if (operationalStates != null) {
            final List<org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter.OperationalStateEnum> etsiCatalogOperationalStates =
                    new ArrayList<>();
            operationalStates.forEach(state -> {
                etsiCatalogOperationalStates.add(
                        org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter.OperationalStateEnum
                                .fromValue(state.getValue()));
            });

            return etsiCatalogOperationalStates;
        }
        return Collections.emptyList();
    }

    private List<String> getVnfPkgIds(final List<String> vnfPkgId) {
        if (vnfPkgId != null) {
            final List<String> etsiCatalogManagerVnfPkgId = new ArrayList<>();
            vnfPkgId.forEach(type -> {
                etsiCatalogManagerVnfPkgId.add(type);
            });
            return etsiCatalogManagerVnfPkgId;
        }
        return Collections.emptyList();
    }

    private List<String> getVnfdIds(final List<String> vnfdId) {
        if (vnfdId != null) {
            final List<String> etsiCatalogManagerVnfdId = new ArrayList<>();
            vnfdId.forEach(type -> {
                etsiCatalogManagerVnfdId.add(type);
            });
            return etsiCatalogManagerVnfdId;
        }
        return Collections.emptyList();
    }

    private List<VnfProductsProviders> getVnfProductsProviders(
            final List<SubscriptionsFilterVnfProductsFromProviders> filterProductsFromProvider) {

        if (filterProductsFromProvider != null && !filterProductsFromProvider.isEmpty()) {
            final List<VnfProductsProviders> etsiCatalogManagerVnfProductsProviders = new ArrayList<>();
            filterProductsFromProvider.forEach(vnfProduct -> {
                etsiCatalogManagerVnfProductsProviders
                        .add(new VnfProductsProviders().vnfProvider(vnfProduct.getVnfProvider())
                                .vnfProducts(getVnfProducts(vnfProduct.getVnfProducts())));
            });
            return etsiCatalogManagerVnfProductsProviders;
        }
        return Collections.emptyList();
    }

    private List<VnfProducts> getVnfProducts(final List<SubscriptionsFilterVnfProducts> sol003VnfProducts) {
        if (sol003VnfProducts != null) {
            final List<VnfProducts> etsiCatalogManagerVnfProductsList = new ArrayList<>();
            sol003VnfProducts.forEach(vnfProduct -> {
                etsiCatalogManagerVnfProductsList.add(new VnfProducts().vnfProductName(vnfProduct.getVnfProductName())
                        .versions(getVersion(vnfProduct.getVersions())));
            });
            return etsiCatalogManagerVnfProductsList;
        }
        return Collections.emptyList();
    }

    private List<Version> getVersion(final List<SubscriptionsFilterVersions> sol003FilterVersions) {
        if (sol003FilterVersions != null && !sol003FilterVersions.isEmpty()) {
            final List<Version> etsiCatalogVersionList = new ArrayList<>();
            sol003FilterVersions.forEach(vnfFilterVersion -> {
                etsiCatalogVersionList.add(new Version().vnfSoftwareVersion(vnfFilterVersion.getVnfSoftwareVersion())
                        .vnfdVersions(vnfFilterVersion.getVnfdVersions()));
            });
            return etsiCatalogVersionList;
        }
        return Collections.emptyList();
    }

    private List<org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter.NotificationTypesEnum> getPkgmNotificationsFilterNotificationTypes(
            final List<NotificationTypesEnum> notificationTypes) {

        if (notificationTypes != null && !notificationTypes.isEmpty()) {
            final List<org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter.NotificationTypesEnum> etsiCatalogNotificationTypes =
                    new ArrayList<>();
            notificationTypes.forEach(type -> etsiCatalogNotificationTypes.add(
                    org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter.NotificationTypesEnum
                            .fromValue(type.getValue())));
            return etsiCatalogNotificationTypes;
        }
        return Collections.emptyList();
    }


}
