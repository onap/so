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
import org.onap.aai.domain.yang.Image;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.logger.MsoLogger;

/*
 * Used to delete image AAI object only if it doesn't have any relationships to other object.
 * Delete heatbridge data from AAI is a best effort operation.
 */
public class DeleteImages extends DeleteHeatbridgeAaiResource {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, AddSriovPfToPServerPif.class);
    private static final long serialVersionUID = -3949448534440479116L;
    private List<String> imageUris;

    public DeleteImages(final List<String> imageUris) {
        this.imageUris = imageUris;
    }

    @Override
    public void submit(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        for (String imageUri : imageUris) {
            try {
                Image image = aaiClient.getAaiObjectByUriIfPresent(imageUri, Image.class);
                if (image.getRelationshipList() == null || CollectionUtils.isEmpty(image.getRelationshipList()
                    .getRelationship())) {
                    aaiClient.deleteByUri(imageUri);
                    LOGGER.debug("Deleted image at: " + imageUri);
                } else {
                    LOGGER.debug("Image is in use, not deleting: " + imageUri);
                }
            } catch (ActiveAndAvailableInventoryException e) {
                updateFailedToDeleteResources(imageUri);
                LOGGER.debug("Failed to delete image at: " + imageUri + ". Error cause: " + e);
            }
        }
    }
}
