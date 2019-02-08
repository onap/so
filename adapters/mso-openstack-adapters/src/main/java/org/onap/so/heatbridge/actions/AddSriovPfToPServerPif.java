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

import org.onap.aai.domain.yang.SriovPf;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.logger.MsoLogger;

public class AddSriovPfToPServerPif implements AaiAction<ActiveAndAvailableInventory> {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, AddSriovPfToPServerPif.class);
    private static final long serialVersionUID = 92587772795433108L;

    private boolean submitted = false;
    private SriovPf sriovPf;
    private String pserverHostName;
    private String pInterfaceName;

    public AddSriovPfToPServerPif(final SriovPf sriovPf, final String pserverHostName, final String pInterfaceName) {
        this.sriovPf = sriovPf;
        this.pserverHostName = pserverHostName;
        this.pInterfaceName = pInterfaceName;
    }

    @Override
    public boolean isSubmitted() {
        return submitted;
    }

    @Override
    public void submit(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        aaiClient.addSriovPfToPserverPInterface(sriovPf, pserverHostName, pInterfaceName);
        this.submitted = true;
        LOGGER.debug("Added sriov-pf: " + sriovPf.getPfPciId() + " to pserver/p-if: " + pserverHostName + "/" + pInterfaceName);
    }

    @Override
    public void rollback(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        aaiClient.deleteSriovPfFromPserverPInterface(pserverHostName, pInterfaceName, sriovPf.getPfPciId());
        this.submitted = false;
        LOGGER.debug("Rollback sriov-pf: " + sriovPf.getPfPciId() + " from pserver/p-if: " + pserverHostName + "/" +
            pInterfaceName);
    }
}
