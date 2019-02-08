/*-
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

package org.onap.so.heatbridge.aai.api;

import java.util.List;
import javax.annotation.Nonnull;
import org.onap.aai.domain.yang.Flavor;
import org.onap.aai.domain.yang.Image;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.SriovPf;
import org.onap.aai.domain.yang.Vserver;

/**
 * To get/create/delete operations on AAI objects.
 */
public interface ActiveAndAvailableInventory {

    /**
     * Add sriov-pf object to pserver/p-interface
     *
     * @param sriovPf sriov-pf AAI object
     * @param pServerName pserver-name identifier key
     * @param pInterfaceName pinterface-name identifier key
     * @throws ActiveAndAvailableInventoryException when failing to add to AAI
     */
    void addSriovPfToPserverPInterface(SriovPf sriovPf, String pServerName, String pInterfaceName)
        throws ActiveAndAvailableInventoryException;

    /**
     * Delete sriov-pf from pserver/p-interface
     *
     * @param pServerName pserver-name identifier key
     * @param pInterfaceName pinterface-name identifier key
     * @param pfPciId sriov-pf pf-pci-id identifier key
     * @throws ActiveAndAvailableInventoryException when failing to delete from AAI
     */
    void deleteSriovPfFromPserverPInterface(String pServerName, String pInterfaceName, String pfPciId) throws
        ActiveAndAvailableInventoryException;

    /**
     * Add vserver to AAI
     *
     * @param vserver vserver PoJo
     * @param cloudOwner cloud-owner ID
     * @param cloudRegionId cloud-region ID
     * @param tenantId cloud tenant ID
     * @throws ActiveAndAvailableInventoryException when failing to add to AAI
     */
    void addVserver(Vserver vserver, String cloudOwner, String cloudRegionId,
        String tenantId) throws ActiveAndAvailableInventoryException;

    /**
     * Delete vservers from AAI
     *
     * @param cloudOwner cloud-owner ID
     * @param cloudRegionId cloud-region ID
     * @param vserverIds a list of vserver-ids
     * @throws ActiveAndAvailableInventoryException when failing to delete from AAI
     */
    void deleteVservers(String cloudOwner, String cloudRegionId, String tenantId,
        List<String> vserverIds) throws ActiveAndAvailableInventoryException;

    /**
     * Add Image to AAI
     *
     * @param image image PoJo
     * @param cloudOwner cloud-owner ID
     * @param cloudRegionId cloud-region ID
     * @throws ActiveAndAvailableInventoryException when failing to add to AAI
     */
    void addImage(Image image, String cloudOwner, String cloudRegionId) throws ActiveAndAvailableInventoryException;

    /**
     * Get the pserver/p-interface by name
     *
     * @param pserverHostName p-server hostname key
     * @param pInterfaceName p-interface key
     * @return p-interface AAI object
     * @throws ActiveAndAvailableInventoryException when failing to read from AAI
     */
    PInterface getPserverPInterfaceByName(String pserverHostName, String pInterfaceName) throws
        ActiveAndAvailableInventoryException;

    /**
     * Get the physical server
     *
     * @param serverName - Name of the physical server.
     * @return pserver - Pserver
     * @throws ActiveAndAvailableInventoryException
     */
    Pserver getPserverByServerName(String serverName) throws ActiveAndAvailableInventoryException;

    /**
     * Get an AAI object of the specified type by URI
     *
     * @param uri URI pointing to the object
     * @param clazz AAI object type e.g: "Vserver.class"
     * @param <T> Describes the type of AAI object to be returned
     * @return AAI object of given type retrieved from the desired uri
     * @throws ActiveAndAvailableInventoryException when failing to get from AAI
     */
    <T> T getAaiObjectByUriIfPresent(@Nonnull String uri,
        @Nonnull Class<T> clazz) throws ActiveAndAvailableInventoryException;

    /**
     * Get Image by ID from AAI if present
     *
     * @param cloudOwner cloud-owner ID
     * @param cloudRegionId cloud-region ID
     * @param imageId image ID
     * @return Corresponding AAI Image object or null if absent
     * @throws ActiveAndAvailableInventoryException when failing to get from AAI
     */
    Image getImageIfPresent(String cloudOwner, String cloudRegionId, String imageId) throws
        ActiveAndAvailableInventoryException;

    /**
     * Get Flavor by ID from AAI
     *
     * @param cloudOwner cloud-owner ID
     * @param cloudRegionId cloud-region ID
     * @param flavorId flavor ID
     * @return Corresponding AAI Flavor object or null if absent
     * @throws ActiveAndAvailableInventoryException when failing to get from AAI
     */
    Flavor getFlavorIfPresent(String cloudOwner, String cloudRegionId,
        String flavorId) throws ActiveAndAvailableInventoryException;

    /**
     * Delete images from AAI
     *
     * @param cloudOwner cloud-owner ID
     * @param cloudRegionId cloud-region ID
     * @param imageIds a list of image-ids
     * @throws ActiveAndAvailableInventoryException when failing to delete from AAI
     */
    void deleteImages(String cloudOwner, String cloudRegionId,
        List<String> imageIds) throws ActiveAndAvailableInventoryException;

    /**
     * Add flavor to AAI
     *
     * @param flavor flavor PoJo
     * @param cloudOwner cloud-owner ID
     * @param cloudRegionId cloud-region ID
     * @throws ActiveAndAvailableInventoryException when failing to add to AAI
     */
    void addFlavor(Flavor flavor, String cloudOwner, String cloudRegionId) throws ActiveAndAvailableInventoryException;

    /**
     * Delete flavors from AAI
     *
     * @param cloudOwner cloud-owner ID
     * @param cloudRegionId cloud-region ID
     * @param flavorIds a list of flavor-ids
     * @throws ActiveAndAvailableInventoryException when failing to delete from AAI
     */
    void deleteFlavors(String cloudOwner, String cloudRegionId, List<String> flavorIds)
        throws ActiveAndAvailableInventoryException;


    /**
     * Add l-interface to a given vserver-id
     *
     * @param lIf l-interface PoJo
     * @param cloudOwner cloud-owner ID
     * @param cloudRegionId cloud-region ID
     * @param tenantId tenant ID
     * @param vserverId vserver-id to add the interface to
     * @throws ActiveAndAvailableInventoryException when failing to add to AAI
     */
    void addLInterfaceToVserver(LInterface lIf, String cloudOwner, String cloudRegionId,
        String tenantId, String vserverId) throws ActiveAndAvailableInventoryException;

    /**
     * Delete l-interface from a given vserver-id
     *
     * @param lIf l-interface PoJo
     * @param cloudOwner cloud-owner ID
     * @param cloudRegionId cloud-region ID
     * @param tenantId tenant ID
     * @param vserverId vserver-id to delete the interface from
     * @throws ActiveAndAvailableInventoryException when failing to delete from AAI
     */
    void deleteLInterfaceFromVserver(LInterface lIf, String cloudOwner,
        String cloudRegionId, String tenantId, String vserverId) throws ActiveAndAvailableInventoryException;

    /**
     * Delete an AAI objected by its URI
     *
     * @param uri URI pointing to the object
     * @throws ActiveAndAvailableInventoryException when failing to delete from AAI
     */
    void deleteByUri(String uri) throws ActiveAndAvailableInventoryException;
}
