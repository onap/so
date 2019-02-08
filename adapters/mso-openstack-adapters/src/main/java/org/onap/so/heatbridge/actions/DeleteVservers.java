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

import java.util.List;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.logger.MsoLogger;

/**
 * Used to delete vserver AAI object.
 * Delete heatbridge data from AAI is a best effort operation.
 */
public class DeleteVservers extends DeleteHeatbridgeAaiResource {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, DeleteVservers.class);
    private static final long serialVersionUID = 4701006569917784734L;

    private List<String> vserverUris;

    public DeleteVservers(final List<String> vserverUris) {
        this.vserverUris = vserverUris;
    }

    @Override
    public void submit(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        for (String vserverUri : vserverUris) {
            try {
                aaiClient.deleteByUri(vserverUri);
                LOGGER.debug("Deleted vserver at: " + vserverUri);
            } catch (ActiveAndAvailableInventoryException e) {
                LOGGER.debug("Failed to delete AAI object at: " + vserverUri + ". Error cause: " + e);
                updateFailedToDeleteResources(vserverUri);
            }
        }
    }
}
