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
import org.onap.aai.domain.yang.Pserver;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.logger.MsoLogger;

public class DeleteSriovPfsFromPserver extends DeleteHeatbridgeAaiResource {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, DeleteSriovPfsFromPserver.class);
    private static final long serialVersionUID = 92587772795433108L;

    private String pserverName;
    private List<String> pciIds;

    public DeleteSriovPfsFromPserver(final String pserverName, final List<String> pciIds) {
        this.pserverName = pserverName;
        this.pciIds = pciIds;
    }

    @Override
    public void submit(final ActiveAndAvailableInventory aaiClient) throws ActiveAndAvailableInventoryException {
        Pserver pserver = aaiClient.getPserverByServerName(pserverName);
        if (pserver.getPInterfaces() == null || CollectionUtils.isEmpty(pserver.getPInterfaces().getPInterface())) {
            pciIds.forEach(pciId -> updateFailedToDeleteResources(pserverName + "/p-interface[Not Found]/" + pciId));
            return;
        }

        // For each pserver/p-interface match sriov-vfs by pic-id and delete them.
        pserver.getPInterfaces().getPInterface().stream()
            .filter(pIf -> pIf.getSriovPfs() != null && CollectionUtils.isNotEmpty(pIf.getSriovPfs().getSriovPf()))
            .forEach(pIf -> pIf.getSriovPfs().getSriovPf().forEach(sriovPf -> {
                if (pciIds.remove(sriovPf.getPfPciId())) {
                    try {
                        aaiClient.deleteSriovPfFromPserverPInterface(pserverName, pIf.getInterfaceName(),
                            sriovPf.getPfPciId());
                        LOGGER.debug(
                            "Deleted sriov-pf: " + sriovPf.getPfPciId() + " from pserver/p-if: " + pserverName + "/" +
                                pIf.getInterfaceName());
                    } catch (ActiveAndAvailableInventoryException ex) {
                        String sriovPfIdentifier =
                            pserverName + "/" + pIf.getInterfaceName() + "/" + sriovPf.getPfPciId();
                        LOGGER.debug(
                            "Failed to delete sriov-pf at: " + sriovPfIdentifier + ". Error cause: " + ex);
                        updateFailedToDeleteResources(sriovPfIdentifier);
                    }
                } else {
                    // Mark pci-id as not found under any existing p-interfaces
                    updateFailedToDeleteResources(pserverName + "/p-interface[No Match]/" + sriovPf.getPfPciId());
                }
            }));

        // For the pci-ids for which a match is not found add to failed-to-remove list
        pciIds.forEach(pciId -> updateFailedToDeleteResources(pserverName + "/" + pciId));
    }
}
