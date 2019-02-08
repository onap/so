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
import org.apache.commons.collections.CollectionUtils;
import org.onap.aai.domain.yang.Flavor;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.logger.MsoLogger;

/*
 * Used to delete flavor AAI object only if it doesn't have any relationships to other object.
 * Delete heatbridge data from AAI is a best effort operation.
 */
public class DeleteFlavors extends DeleteHeatbridgeAaiResource {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, DeleteFlavors.class);
    private static final long serialVersionUID = -5005213008164954268L;
    private final List<String> flavorUris;

    public DeleteFlavors(final List<String> flavorUris) {
        this.flavorUris = flavorUris;
    }

    @Override
    public void submit(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        for (String flavorUri : flavorUris) {
            try {
                Flavor flavor = aaiClient.getAaiObjectByUriIfPresent(flavorUri, Flavor.class);
                if (flavor.getRelationshipList() == null || CollectionUtils.isEmpty(flavor.getRelationshipList()
                    .getRelationship())) {
                    aaiClient.deleteByUri(flavorUri);
                    LOGGER.debug("Deleted flavor at: " + flavorUri);
                } else {
                    LOGGER.debug("Flavor is in use, not deleting: " + flavorUri);
                }
            } catch (ActiveAndAvailableInventoryException e) {
                LOGGER.debug("Failed to delete flavor at: " + flavorUri + ". Error cause: " + e);
                updateFailedToDeleteResources(flavorUri);
            }
        }
    }
}
