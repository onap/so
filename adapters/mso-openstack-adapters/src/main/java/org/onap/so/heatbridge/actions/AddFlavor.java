/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
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
 */
package org.onap.so.heatbridge.actions;

import java.util.Collections;
import org.onap.aai.domain.yang.Flavor;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.logger.MsoLogger;

public class AddFlavor implements AaiAction<ActiveAndAvailableInventory> {

    private static final long serialVersionUID = -3792082901320130746L;
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, AddFlavor.class);
    private boolean submitted = false;
    private Flavor flavor;
    private String cloudOwner;
    private String cloudRegionId;

    public AddFlavor(final Flavor flavor, final String cloudOwner, final String cloudRegionId) {
        this.flavor = flavor;
        this.cloudOwner = cloudOwner;
        this.cloudRegionId = cloudRegionId;
    }

    @Override
    public boolean isSubmitted() {
        return submitted;
    }

    @Override
    public void submit(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        aaiClient.addFlavor(flavor, cloudOwner, cloudRegionId);
        this.submitted = true;
        LOGGER.debug("Added flavor: " + flavor.getFlavorId() + " to AAI.");
    }

    @Override
    public void rollback(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        aaiClient.deleteFlavors(cloudOwner, cloudRegionId, Collections.singletonList(flavor.getFlavorId()));
        this.submitted = false;
        LOGGER.debug("Rollback flavor: " + flavor.getFlavorId() + " from AAI.");
    }
}
