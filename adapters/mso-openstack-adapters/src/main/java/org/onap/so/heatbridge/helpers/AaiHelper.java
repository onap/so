/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

/*
 * Copyright (C) 2018 Bell Canada.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.onap.so.heatbridge.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.onap.aai.domain.yang.Flavor;
import org.onap.aai.domain.yang.Image;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.SriovVf;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.heatbridge.constants.HeatBridgeConstants;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.network.Port;
import com.google.common.base.Preconditions;

/**
 * This class provides wrapper methods to manage creation of AAI objects and extracting objects from AAI and
 * transforming into required objects.
 */
public class AaiHelper {

    /**
     * Build vserver relationship object to entities: pserver, vf-module, image, flavor
     *
     * @param cloudOwner AAI cloudOwner value
     * @param cloudRegionId AAI cloud-region identifier
     * @param genericVnfId AAI generic-vnf identifier
     * @param vfModuleId AAI vf-module identifier
     * @param server Openstack Server object
     */
    public RelationshipList getVserverRelationshipList(final String cloudOwner, final String cloudRegionId,
            final String genericVnfId, final String vfModuleId, final Server server) {
        RelationshipList relationshipList = new RelationshipList();
        List<Relationship> relationships = relationshipList.getRelationship();

        // vserver to pserver relationship
        Relationship pserverRelationship = buildRelationship(
                AAIUriFactory.createResourceUri(AAIObjectType.PSERVER, server.getHypervisorHostname()));
        relationships.add(pserverRelationship);

        // vserver to generic-vnf relationship
        Relationship genericVnfRelationship =
                buildRelationship(AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, genericVnfId));
        relationships.add(genericVnfRelationship);

        // vserver to vnfc relationship
        Relationship vnfcRelationship =
                buildRelationship(AAIUriFactory.createResourceUri(AAIObjectType.VNFC, server.getName()));
        relationships.add(vnfcRelationship);


        // vserver to vf-module relationship
        Relationship vfModuleRelationship =
                buildRelationship(AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, genericVnfId, vfModuleId));
        relationships.add(vfModuleRelationship);

        // vserver to image relationship
        if (server.getImage() != null) {
            Relationship imageRel = buildRelationship(AAIUriFactory.createResourceUri(AAIObjectType.IMAGE, cloudOwner,
                    cloudRegionId, server.getImage().getId()));
            relationships.add(imageRel);
        }

        // vserver to flavor relationship
        Relationship flavorRel = buildRelationship(AAIUriFactory.createResourceUri(AAIObjectType.IMAGE, cloudOwner,
                cloudRegionId, server.getFlavor().getId()));
        relationships.add(flavorRel);
        return relationshipList;
    }

    public RelationshipList getLInterfaceRelationshipList(final String pserverName, final String pIfName,
            final String pfPciId) {
        RelationshipList relationshipList = new RelationshipList();
        List<Relationship> relationships = relationshipList.getRelationship();

        // sriov-vf to sriov-pf relationship
        Relationship sriovPfRelationship = buildRelationship(
                AAIUriFactory.createResourceUri(AAIObjectType.SRIOV_PF, pserverName, pIfName, pfPciId));
        relationships.add(sriovPfRelationship);

        return relationshipList;
    }

    /**
     * Transform Openstack Server object to AAI Vserver object
     *
     * @param serverId Openstack server identifier
     * @param server Openstack server object
     * @return AAI Vserver object
     */
    public Vserver buildVserver(final String serverId, final Server server) {
        Vserver vserver = new Vserver();
        vserver.setInMaint(false);
        vserver.setIsClosedLoopDisabled(false);
        vserver.setVserverId(serverId);
        vserver.setVserverName(server.getName());
        vserver.setVserverName2(server.getName());
        vserver.setProvStatus(server.getStatus().value());
        server.getLinks().stream().filter(link -> link.getRel().equals(HeatBridgeConstants.OS_RESOURCES_SELF_LINK_KEY))
                .findFirst().ifPresent(link -> vserver.setVserverSelflink(link.getHref()));
        return vserver;
    }

    /**
     * Transform Openstack Server object to AAI Pserver object
     *
     * @param server Openstack server object
     * @return AAI Pserver object
     */
    public Pserver buildPserver(final Server server) {
        Pserver pserver = new Pserver();
        pserver.setInMaint(false);
        pserver.setPserverId(server.getId());
        pserver.setHostname(server.getHypervisorHostname());
        pserver.setPserverName2(server.getHost());
        pserver.setProvStatus(server.getStatus().value());
        return pserver;
    }

    /**
     * Transform Openstack Server object to AAI PInterface object
     *
     * @param port Openstack port object
     * @return AAI PInterface object
     */
    public PInterface buildPInterface(Port port) {
        Map<String, Object> portProfile = port.getProfile();
        PInterface pInterface = new PInterface();
        pInterface.setInterfaceName(portProfile.get(HeatBridgeConstants.OS_PHYSICAL_NETWORK_KEY).toString());
        pInterface.setInMaint(false);
        pInterface.setInterfaceRole(HeatBridgeConstants.OS_PHYSICAL_INTERFACE_KEY);
        return pInterface;
    }

    /**
     * Transform Openstack Image object to AAI Image object
     *
     * @param image Openstack Image object
     * @return AAI Image object
     */
    public Image buildImage(final org.openstack4j.model.compute.Image image) {
        Image aaiImage = new Image();
        aaiImage.setImageId(image.getId());
        aaiImage.setImageName(image.getName());
        aaiImage.setImageOsDistro(HeatBridgeConstants.OS_UNKNOWN_KEY);
        aaiImage.setImageOsVersion(HeatBridgeConstants.OS_UNKNOWN_KEY);

        // application name/vendor/version needs to be set
        if (image.getLinks() != null) {
            image.getLinks().stream()
                    .filter(link -> link.getRel().equals(HeatBridgeConstants.OS_RESOURCES_SELF_LINK_KEY)).findFirst()
                    .ifPresent(link -> aaiImage.setImageSelflink(link.getHref()));
        }
        return aaiImage;
    }

    /**
     * Transform Openstack Flavor object to AAI Flavor object
     *
     * @param flavor Openstack Flavor object
     * @return AAI Flavor object
     */
    public Flavor buildFlavor(final org.openstack4j.model.compute.Flavor flavor) {
        Flavor aaiFlavor = new Flavor();
        aaiFlavor.setFlavorId(flavor.getId());
        aaiFlavor.setFlavorName(flavor.getName());
        aaiFlavor.setFlavorVcpus(flavor.getVcpus());
        aaiFlavor.setFlavorRam(flavor.getRam());
        aaiFlavor.setFlavorDisk(flavor.getDisk());
        aaiFlavor.setFlavorEphemeral(flavor.getEphemeral());
        aaiFlavor.setFlavorDisabled(flavor.isDisabled());
        aaiFlavor.setFlavorIsPublic(flavor.isPublic());
        aaiFlavor.setFlavorSwap(Integer.toString(flavor.getSwap()));
        flavor.getLinks().stream().filter(link -> link.getRel().equals(HeatBridgeConstants.OS_RESOURCES_SELF_LINK_KEY))
                .findFirst().ifPresent(link -> aaiFlavor.setFlavorSelflink(link.getHref()));
        return aaiFlavor;
    }

    /**
     * Extract a list of flavors URI associated with the list of vservers
     *
     * @param vservers List of vserver AAI objects
     * @return a list of related flavor related-links
     */
    public List<String> getFlavorsUriFromVserver(final List<Vserver> vservers) {
        List<String> flavorUris = new ArrayList<>();
        vservers.forEach(vserver -> flavorUris.addAll(
                filterRelatedLinksByRelatedToProperty(vserver.getRelationshipList(), HeatBridgeConstants.AAI_FLAVOR)));
        return flavorUris;
    }

    /**
     * Extract a list of images URI associated with the list of vservers
     *
     * @param vservers List of vserver AAI objects
     * @return a list of related image related-links
     */
    public List<String> getImagesUriFromVserver(final List<Vserver> vservers) {
        List<String> imageUris = new ArrayList<>();
        vservers.forEach(vserver -> imageUris.addAll(
                filterRelatedLinksByRelatedToProperty(vserver.getRelationshipList(), HeatBridgeConstants.AAI_IMAGE)));
        return imageUris;
    }

    /**
     * From the list vserver objects build a map of compute hosts's name and the PCI IDs linked to it.
     *
     * @param vservers List of vserver AAI objects
     * @return a map of compute names to the PCI ids associated with the compute
     */
    public Map<String, List<String>> getPserverToPciIdMap(final List<Vserver> vservers) {
        Map<String, List<String>> pserverToPciIdMap = new HashMap<>();
        for (Vserver vserver : vservers) {
            if (vserver.getLInterfaces() != null) {
                List<String> pciIds = vserver.getLInterfaces().getLInterface().stream()
                        .filter(lInterface -> lInterface.getSriovVfs() != null
                                && CollectionUtils.isNotEmpty(lInterface.getSriovVfs().getSriovVf()))
                        .flatMap(lInterface -> lInterface.getSriovVfs().getSriovVf().stream()).map(SriovVf::getPciId)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(pciIds)) {
                    List<String> matchingPservers = extractRelationshipDataValue(vserver.getRelationshipList(),
                            HeatBridgeConstants.AAI_PSERVER, HeatBridgeConstants.AAI_PSERVER_HOSTNAME);
                    Preconditions.checkState(matchingPservers != null && matchingPservers.size() == 1,
                            "Invalid pserver relationships for vserver: " + vserver.getVserverName());
                    pserverToPciIdMap.put(matchingPservers.get(0), pciIds);
                }
            }
        }
        return pserverToPciIdMap;
    }

    /**
     * Extract from relationship-list object all the relationship-value that match the related-to and relationship-key
     * fields.
     *
     * @param relationshipListObj AAI relationship-list object
     * @param relatedToProperty related-to value
     * @param relationshipKey relationship-key value
     * @return relationship-value matching the key requested for the relationship object of type related-to property
     */
    private List<String> extractRelationshipDataValue(final RelationshipList relationshipListObj,
            final String relatedToProperty, final String relationshipKey) {
        if (relationshipListObj != null && relationshipListObj.getRelationship() != null) {
            return relationshipListObj.getRelationship().stream()
                    .filter(relationship -> relationship.getRelatedTo().equals(relatedToProperty))
                    .map(Relationship::getRelationshipData).flatMap(Collection::stream)
                    .filter(data -> data.getRelationshipKey() != null
                            && relationshipKey.equals(data.getRelationshipKey()))
                    .map(RelationshipData::getRelationshipValue).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Extract and filter the related-links to all objects that match the type specified by the filter property
     *
     * @param relationshipListObj AAI object representing relationship object
     * @param relatedToProperty Value identifying the type of AAI object for related-to field
     * @return a list of related-links filtered by the specified related-to property
     */
    private List<String> filterRelatedLinksByRelatedToProperty(final RelationshipList relationshipListObj,
            final String relatedToProperty) {
        if (relationshipListObj != null && relationshipListObj.getRelationship() != null) {
            return relationshipListObj.getRelationship().stream()
                    .filter(relationship -> relationship.getRelatedTo().equals(relatedToProperty))
                    .map(Relationship::getRelatedLink).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Build the relationship object
     *
     * @param relatedTo Related to entity value
     * @param relationshipKeyValues Key value pairs of relationship data
     * @return AAI Relationship object
     */
    private Relationship buildRelationship(final AAIResourceUri relatedLink) {
        Relationship relationship = new Relationship();
        relationship.setRelatedLink(relatedLink.build().toString());
        return relationship;
    }
}
