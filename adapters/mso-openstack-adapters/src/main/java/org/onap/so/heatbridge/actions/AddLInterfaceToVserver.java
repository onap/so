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

import org.onap.aai.domain.yang.LInterface;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.logger.MsoLogger;

public class AddLInterfaceToVserver implements AaiAction<ActiveAndAvailableInventory> {

    private static final long serialVersionUID = -6700923070697903237L;
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, AddLInterfaceToVserver.class);

    private boolean submitted = false;
    private LInterface lIf;
    private String cloudOwner;
    private String cloudRegionId;
    private String tenantId;
    private String vserverId;

    public AddLInterfaceToVserver(final LInterface lIf, final String cloudOwner, final String cloudRegionId,
        final String tenantId, final String vserverId) {
        this.lIf = lIf;
        this.cloudOwner = cloudOwner;
        this.cloudRegionId = cloudRegionId;
        this.tenantId = tenantId;
        this.vserverId = vserverId;
    }

    @Override
    public boolean isSubmitted() {
        return submitted;
    }

    @Override
    public void submit(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        aaiClient.addLInterfaceToVserver(lIf, cloudOwner, cloudRegionId, tenantId, vserverId);
        this.submitted = true;
        LOGGER.debug("Added l-interface: " + lIf.getInterfaceName() + " to vserver: " + vserverId);
    }

    @Override
    public void rollback(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        aaiClient.deleteLInterfaceFromVserver(lIf, cloudOwner, cloudRegionId, tenantId, vserverId);
        this.submitted = false;
        LOGGER.debug("Rollback l-interface: " + lIf.getInterfaceName() + " from vserver: " + vserverId);
    }
}
