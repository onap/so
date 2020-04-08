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

package org.onap.so.adapters.vnfmadapter.packagemanagement.subscriptionmanagement.cache;

import java.util.Map;
import java.util.Optional;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.PkgmSubscriptionRequest;

/**
 * Interface which provides methods for communicating with the cache
 *
 * @author Ronan Kenny (ronan.kenny@est.tech)
 * @author Gareth Roper (gareth.roper@est.tech)
 *
 */
public interface PackageManagementCacheServiceProvider {

    /**
     * Checks cache if subscription request Id is already present. If not, it adds the subscription to the cache.
     * 
     * @param subscriptionId
     * @param pkgmSubscriptionRequest
     */
    void addSubscription(final String subscriptionId, final PkgmSubscriptionRequest pkgmSubscriptionRequest);

    /**
     * Gets individual subscription from cache
     * 
     * @param subscriptionId
     * @return <AbstractMap.SimpleImmutableEntry<String, PkgmSubscriptionRequest>>
     */
    Optional<PkgmSubscriptionRequest> getSubscription(final String subscriptionId);

    /**
     * Gets Map of subscriptions from cache
     * 
     * @return Map<String, PkgmSubscriptionRequest>>
     */
    Map<String, PkgmSubscriptionRequest> getSubscriptions();

    /**
     * Delete subscription from cache
     * 
     * @param subscriptionId
     * @return Boolean
     */
    boolean deleteSubscription(final String subscriptionId);

    /**
     * Checks if subscription exists in cache and return its subscriptionId
     * 
     * @param pkgmSubscriptionRequest
     * @return Subscription Id
     */
    Optional<String> getSubscriptionId(final PkgmSubscriptionRequest pkgmSubscriptionRequest);
}
