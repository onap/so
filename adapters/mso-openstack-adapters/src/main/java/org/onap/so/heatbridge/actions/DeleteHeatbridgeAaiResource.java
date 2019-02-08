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

import java.util.ArrayList;
import java.util.List;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.logger.MsoLogger;

/**
 * To be extended for deleting AAI objects representing Openstack resources: i.e. vserver, image and flavor etc.
 * Delete heatbridge data from AAI is a best effort operation.
 */
public abstract class DeleteHeatbridgeAaiResource implements AaiAction<ActiveAndAvailableInventory> {

    private static final long serialVersionUID = 2849718744624613501L;
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, DeleteHeatbridgeAaiResource.class);

    private List<String> failedToDeleteResources = new ArrayList<>();

    @Override
    public boolean isSubmitted() {
        return true;
    }

    @Override
    public void rollback(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        LOGGER.debug("No rollback for delete heatbridge related AAI objects.");
    }

    public List<String> getFailedToDeleteResources() {
        return failedToDeleteResources;
    }

    public void updateFailedToDeleteResources(String resourceName) {
        failedToDeleteResources.add(resourceName);
    }
}
