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

package org.onap.so.adapters.etsisol003adapter.pkgm.subscriptionmanagement.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * @author Ronan Kenny (ronan.kenny@est.tech)
 * @author Gareth Roper (gareth.roper@est.tech)
 */
public abstract class AbstractCacheServiceProvider {

    private final CacheManager cacheManager;
    private final String cacheName;

    public AbstractCacheServiceProvider(final String cacheName, final CacheManager cacheManager) {
        this.cacheName = cacheName;
        this.cacheManager = cacheManager;
    }

    public Cache getCache() {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new CacheNotFoundException("Unable to find " + cacheName + " cache");
        }
        return cache;
    }
}
