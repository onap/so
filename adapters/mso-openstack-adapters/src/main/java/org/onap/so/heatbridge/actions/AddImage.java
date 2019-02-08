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
import org.onap.aai.domain.yang.Image;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.logger.MsoLogger;

public class AddImage implements AaiAction<ActiveAndAvailableInventory> {

    private static final long serialVersionUID = -2269712365822241604L;
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA,AddImage.class);

    private boolean submitted = false;
    private Image image;
    private String cloudOwner;
    private String cloudRegionId;

    public AddImage(final Image image, final String cloudOwner, final String cloudRegionId) {
        this.image = image;
        this.cloudOwner = cloudOwner;
        this.cloudRegionId = cloudRegionId;
    }

    @Override
    public boolean isSubmitted() {
        return submitted;
    }

    @Override
    public void submit(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        aaiClient.addImage(image, cloudOwner, cloudRegionId);
        this.submitted = true;
        LOGGER.debug("Added image: " + image.getImageId() + " to AAI.");
    }

    @Override
    public void rollback(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        aaiClient.deleteImages(cloudOwner, cloudRegionId, Collections.singletonList(image.getImageId()));
        this.submitted = false;
        LOGGER.debug("Rollback image: " + image.getImageId() + " to AAI.");
    }
}
