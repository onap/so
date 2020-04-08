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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.onap.so.adapters.vnfmadapter.PackageManagementConstants;
import org.onap.so.adapters.vnfmadapter.packagemanagement.model.PkgmSubscriptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Implementation which provides methods for communicating with the cache
 *
 * @author Ronan Kenny (ronan.kenny@est.tech)
 * @author Gareth Roper (gareth.roper@est.tech)
 */
@Service
public class PackageManagementCacheServiceProviderImpl extends AbstractCacheServiceProvider
        implements PackageManagementCacheServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageManagementCacheServiceProviderImpl.class);

    @Autowired
    public PackageManagementCacheServiceProviderImpl(final CacheManager cacheManager) {
        super(PackageManagementConstants.PACKAGE_MANAGEMENT_SUBSCRIPTION_CACHE, cacheManager);
    }

    @Override
    public void addSubscription(final String subscriptionId, final PkgmSubscriptionRequest pkgmSubscriptionRequest) {
        LOGGER.debug("Adding {} to cache with subscription id: {}", pkgmSubscriptionRequest, subscriptionId);
        getCache().put(subscriptionId, pkgmSubscriptionRequest);
    }

    @Override
    public Optional<PkgmSubscriptionRequest> getSubscription(final String subscriptionId) {
        LOGGER.debug("Getting subscription from cache using Id: {}", subscriptionId);
        final Cache cache = getCache();
        final PkgmSubscriptionRequest cacheValue = cache.get(subscriptionId, PkgmSubscriptionRequest.class);
        if (cacheValue != null) {
            return Optional.of(cacheValue);
        }
        LOGGER.error("Unable to find Subscription in cache using Id: {}", subscriptionId);
        return Optional.empty();
    }

    @Override
    public Map<String, PkgmSubscriptionRequest> getSubscriptions() {
        LOGGER.info("Getting all subscriptions from cache");
        final Cache cache = getCache();

        final Object nativeCache = cache.getNativeCache();
        if (nativeCache instanceof ConcurrentHashMap) {
            @SuppressWarnings("unchecked")
            final ConcurrentHashMap<Object, Object> concurrentHashMap = (ConcurrentHashMap<Object, Object>) nativeCache;
            final Map<String, PkgmSubscriptionRequest> result = new HashMap<>();
            concurrentHashMap.keySet().forEach(key -> {
                final Optional<PkgmSubscriptionRequest> optional = getSubscription(key.toString());
                optional.ifPresent(pkgmSubscriptionRequest -> result.put(key.toString(), pkgmSubscriptionRequest));
            });
            return result;
        }
        LOGGER.error("Unable to find Subscriptions in cache");
        return Collections.emptyMap();
    }

    @Override
    public boolean deleteSubscription(final String subscriptionId) {
        final Cache cache = getCache();
        final Optional<PkgmSubscriptionRequest> optional = getSubscription(subscriptionId);
        if (optional.isPresent()) {
            cache.evict(subscriptionId);
            return true;
        }
        return false;
    }

    @Override
    public Optional<String> getSubscriptionId(final PkgmSubscriptionRequest pkgmSubscriptionRequest) {
        final Cache cache = getCache();
        final Object nativeCache = cache.getNativeCache();
        if (nativeCache instanceof ConcurrentHashMap) {
            @SuppressWarnings("unchecked")
            final ConcurrentHashMap<Object, Object> concurrentHashMap = (ConcurrentHashMap<Object, Object>) nativeCache;
            final Optional<Entry<Object, Object>> optional = concurrentHashMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(pkgmSubscriptionRequest)).findAny();
            if (optional.isPresent()) {
                return Optional.of(optional.get().getKey().toString());
            }
        }
        return Optional.empty();
    }
}
