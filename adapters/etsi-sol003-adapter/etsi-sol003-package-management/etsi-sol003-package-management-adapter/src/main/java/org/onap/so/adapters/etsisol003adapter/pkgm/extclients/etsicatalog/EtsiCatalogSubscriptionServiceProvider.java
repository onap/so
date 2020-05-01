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
package org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog;

import java.util.Optional;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.NsdmSubscription;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.PkgmSubscription;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public interface EtsiCatalogSubscriptionServiceProvider {

    /**
     * POST the SubscriptionRequest Object.
     *
     * @return The ETSI Catalog Manager's PkgmSubscription object.
     */
    Optional<PkgmSubscription> postSubscription(
            final org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.PkgmSubscriptionRequest etsiCatalogManagerSubscriptionRequest);

    /**
     * Get the Subscription from ETSI Catalog.
     * 
     * @param subscriptionId Subscription ID
     * @return The Subscription {@link NsdmSubscription} from ETSI Catalog
     */
    Optional<NsdmSubscription> getSubscription(final String subscriptionId);

    /**
     * DELETE the SubscriptionRequest Object.
     *
     * @return A Boolean representing if the delete was successful or not.
     */
    boolean deleteSubscription(final String subscriptionId);

}
