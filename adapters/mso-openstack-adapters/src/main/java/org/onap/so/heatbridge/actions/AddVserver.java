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
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.logger.MsoLogger;

public class AddVserver implements AaiAction<ActiveAndAvailableInventory> {

    private static final long serialVersionUID = 2176320011545471934L;
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, AddVserver.class);

    private boolean submitted = false;
    private Vserver vserver;
    private String cloudOwner;
    private String cloudRegionId;
    private String tenantId;

    public AddVserver(final Vserver vserver, final String cloudOwner, final String cloudRegionId,
        final String tenantId) {
        this.vserver = vserver;
        this.cloudOwner = cloudOwner;
        this.cloudRegionId = cloudRegionId;
        this.tenantId = tenantId;
    }

    @Override
    public boolean isSubmitted() {
        return submitted;
    }

    @Override
    public void submit(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        aaiClient.addVserver(vserver, cloudOwner, cloudRegionId, tenantId);
        this.submitted = true;
        LOGGER.debug("Added vserver: " + vserver.getVserverId() + " to AAI.");
    }

    @Override
    public void rollback(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        aaiClient
            .deleteVservers(cloudOwner, cloudRegionId, tenantId, Collections.singletonList(vserver.getVserverId()));
        this.submitted = false;
        LOGGER.debug("Rollback vserver: " + vserver.getVserverId() + " from AAI.");
    }
}
