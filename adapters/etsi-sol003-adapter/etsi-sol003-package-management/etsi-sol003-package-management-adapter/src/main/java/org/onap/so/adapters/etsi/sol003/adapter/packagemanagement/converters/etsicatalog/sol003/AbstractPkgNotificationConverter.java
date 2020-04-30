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

package org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.converters.etsicatalog.sol003;

import org.onap.so.adapters.etsi.sol003.adapter.common.VnfmAdapterUrlProvider;
import org.onap.so.adapters.etsi.sol003.adapter.etsicatalog.notification.model.PkgmLinks;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.vnfm.notification.model.URIisprovidedbytheclientwhencreatingthesubscriptionVnfPackageOnboardingNotificationLinks;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.vnfm.notification.model.URIisprovidedbytheclientwhencreatingthesubscriptionVnfPackageOnboardingNotificationLinksVnfPackage;

/**
 * A base class that can be extended by classes for converting Etsi Catalog Manager Pkg Notification classes. Provides
 * common methods that will be useful to those classes.
 *
 * @author andrew.a.lamb@est.tech
 */
abstract public class AbstractPkgNotificationConverter {

    private final VnfmAdapterUrlProvider vnfmAdapterUrlProvider;

    public AbstractPkgNotificationConverter(final VnfmAdapterUrlProvider vnfmAdapterUrlProvider) {
        this.vnfmAdapterUrlProvider = vnfmAdapterUrlProvider;
    }

    protected URIisprovidedbytheclientwhencreatingthesubscriptionVnfPackageOnboardingNotificationLinks convert(
            final PkgmLinks pkgmLinks, final String vnfPkgId, final String subscriptionId) {
        final URIisprovidedbytheclientwhencreatingthesubscriptionVnfPackageOnboardingNotificationLinksVnfPackage linksVnfPackage =
                new URIisprovidedbytheclientwhencreatingthesubscriptionVnfPackageOnboardingNotificationLinksVnfPackage();
        if (pkgmLinks.getVnfPackage() != null) {
            linksVnfPackage.setHref(vnfmAdapterUrlProvider.getVnfPackageUrl(vnfPkgId));
        }

        final URIisprovidedbytheclientwhencreatingthesubscriptionVnfPackageOnboardingNotificationLinksVnfPackage linksSubscription =
                new URIisprovidedbytheclientwhencreatingthesubscriptionVnfPackageOnboardingNotificationLinksVnfPackage();
        if (pkgmLinks.getSubscription() != null) {
            linksSubscription.setHref(vnfmAdapterUrlProvider.getSubscriptionUriString(subscriptionId));
        }

        final URIisprovidedbytheclientwhencreatingthesubscriptionVnfPackageOnboardingNotificationLinks links =
                new URIisprovidedbytheclientwhencreatingthesubscriptionVnfPackageOnboardingNotificationLinks();
        links.setVnfPackage(linksVnfPackage);
        links.setSubscription(linksSubscription);
        return links;
    }

}
