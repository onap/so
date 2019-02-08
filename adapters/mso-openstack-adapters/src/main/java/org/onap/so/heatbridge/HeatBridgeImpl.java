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
package org.onap.so.heatbridge;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.onap.aai.domain.yang.Flavor;
import org.onap.aai.domain.yang.Image;
import org.onap.aai.domain.yang.L3InterfaceIpv4AddressList;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.SriovPf;
import org.onap.aai.domain.yang.SriovPfs;
import org.onap.aai.domain.yang.SriovVf;
import org.onap.aai.domain.yang.SriovVfs;
import org.onap.aai.domain.yang.Vlan;
import org.onap.aai.domain.yang.Vlans;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.heatbridge.actions.AaiAction;
import org.onap.so.heatbridge.actions.AddFlavor;
import org.onap.so.heatbridge.actions.AddImage;
import org.onap.so.heatbridge.actions.AddLInterfaceToVserver;
import org.onap.so.heatbridge.actions.AddSriovPfToPServerPif;
import org.onap.so.heatbridge.actions.AddVserver;
import org.onap.so.heatbridge.actions.RollbackFromAai;
import org.onap.so.heatbridge.constants.HeatBridgeConstants;
import org.onap.so.heatbridge.factory.MsoCloudClientFactoryImpl;
import org.onap.so.heatbridge.helpers.AaiHelper;
import org.onap.so.heatbridge.openstack.api.OpenstackClient;
import org.onap.so.heatbridge.openstack.factory.OpenstackClientFactoryImpl;
import org.onap.so.heatbridge.utils.HeatBridgeUtils;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.heat.Resource;
import org.openstack4j.model.network.IP;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.NetworkType;
import org.openstack4j.model.network.Port;

/**
 * This class provides an implementation of {@link HeatBridgeApi}
 */
public class HeatBridgeImpl implements HeatBridgeApi {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, HeatBridgeImpl.class);
    private static final String ERR_MSG_NULL_OS_CLIENT = "Initialization error: Null openstack client. Authenticate with Keystone first.";
    private static final String OOB_MGT_NETWORK_IDENTIFIER = "Management";
    private OpenstackClient osClient;
    private ActiveAndAvailableInventory aaiClient;
    private String cloudOwner;
    private String cloudRegionId;
    private String tenantId;
    private List<AaiAction<ActiveAndAvailableInventory>> aaiActions;
    private AaiHelper aaiHelper = new AaiHelper();
    private CloudIdentity cloudIdentity;


    public HeatBridgeImpl(final CloudIdentity cloudIdentity, @Nonnull final ActiveAndAvailableInventory aaiClient,
        @Nonnull final String cloudOwner, @Nonnull final String cloudRegionId, @Nonnull final String tenantId,
        @Nonnull List<AaiAction<ActiveAndAvailableInventory>> aaiActions) {
        Objects.requireNonNull(aaiClient, "Null ActiveAndAvailableInventory instance!");
        Objects.requireNonNull(cloudOwner, "Null cloud-owner value!");
        Objects.requireNonNull(cloudRegionId, "Null cloud-region identifier!");
        Objects.requireNonNull(tenantId, "Null tenant identifier!");
        Objects.requireNonNull(tenantId, "Null AAI actions list!");

        this.cloudIdentity = cloudIdentity;
        this.aaiClient = aaiClient;
        this.cloudOwner = cloudOwner;
        this.cloudRegionId = cloudRegionId;
        this.tenantId = tenantId;
        this.aaiActions = aaiActions;
    }

    @Override
    public OpenstackClient authenticate() throws HeatBridgeException {
        this.osClient = new MsoCloudClientFactoryImpl(new OpenstackClientFactoryImpl())
            .getOpenstackClient(cloudIdentity.getIdentityUrl(), cloudIdentity.getMsoId(), cloudIdentity.getMsoPass(), cloudRegionId, tenantId);
        LOGGER.debug("Successfully authenticated with keystone for tenant: " + tenantId + " and cloud "
            + "region: " + cloudRegionId);
        return osClient;
    }

    @Override
    public List<Resource> queryNestedHeatStackResources(final String heatStackId) {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);
        Preconditions.checkState(!Strings.isNullOrEmpty(heatStackId), "Invalid heatStackId!");
        List<Resource> stackBasedResources = osClient
            .getStackBasedResources(heatStackId, HeatBridgeConstants.OS_DEFAULT_HEAT_NESTING);
        LOGGER
            .debug(stackBasedResources.size() + " heat stack resources are extracted for stack: " + heatStackId);
        return stackBasedResources;
    }

    @Override
    public List<String> extractStackResourceIdsByResourceType(final List<Resource> stackResources,
        final String resourceType) {
        return stackResources.stream()
            .filter(stackResource -> stackResource.getType().equals(resourceType))
            .map(Resource::getPhysicalResourceId)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> extractNetworkIds(final List<String> networkNameList) {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);
        return networkNameList.stream()
            .map(netName -> osClient.listNetworksByFilter(ImmutableMap.of(HeatBridgeConstants.OS_NAME_KEY, netName)))
            .filter(nets -> nets != null && nets.size() == 1) //extract network-id only if network-name is unique
            .map(nets -> nets.get(0).getId())
            .collect(Collectors.toList());
    }

    @Override
    public List<Server> getAllOpenstackServers(final List<Resource> stackResources) {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);

        // Filter Openstack Compute resources
        List<String> serverIds = extractStackResourceIdsByResourceType(stackResources,
            HeatBridgeConstants.OS_SERVER_RESOURCE_TYPE);
        return serverIds.stream().map(serverId -> osClient.getServerById(serverId)).collect(Collectors.toList());
    }

    @Override
    public List<org.openstack4j.model.compute.Image> extractOpenstackImagesFromServers(final List<Server> servers) {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);
        return servers.stream().map(Server::getImage)
            .filter(distinctByProperty(org.openstack4j.model.compute.Image::getId)).collect(Collectors.toList());
    }

    @Override
    public List<org.openstack4j.model.compute.Flavor> extractOpenstackFlavorsFromServers(final List<Server> servers) {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);
        return servers.stream().map(Server::getFlavor)
            .filter(distinctByProperty(org.openstack4j.model.compute.Flavor::getId)).collect(Collectors.toList());
    }

    @Override
    public void buildAddImagesToAaiAction(final List<org.openstack4j.model.compute.Image> images)
        throws HeatBridgeException {
        for (org.openstack4j.model.compute.Image image : images) {
            Image aaiImage = aaiHelper.buildImage(image);
            try {
                if (Objects.isNull(aaiClient.getImageIfPresent(cloudOwner, cloudRegionId, aaiImage.getImageId()))) {
                    aaiActions.add(new AddImage(aaiImage, cloudOwner, cloudRegionId));
                    LOGGER.debug("Queuing AAI command to add image: " + aaiImage.getImageId());
                } else {
                    LOGGER.debug("Nothing to add since image: " + aaiImage.getImageId() + "already exists in AAI.");
                }
            } catch (ActiveAndAvailableInventoryException e) {
                throw new HeatBridgeException("Failed to update image to AAI: " + aaiImage.getImageId() + ". Error"
                    + " cause: " + e, e);
            }
        }
    }

    @Override
    public void buildAddFlavorsToAaiAction(final List<org.openstack4j.model.compute.Flavor> flavors)
        throws HeatBridgeException {
        for (org.openstack4j.model.compute.Flavor flavor : flavors) {
            Flavor aaiFlavor = aaiHelper.buildFlavor(flavor);
            try {
                if (Objects.isNull(aaiClient.getFlavorIfPresent(cloudOwner, cloudRegionId, aaiFlavor.getFlavorId()))) {
                    aaiActions.add(new AddFlavor(aaiFlavor, cloudOwner, cloudRegionId));
                    LOGGER.debug("Queuing AAI command to add flavor: " + aaiFlavor.getFlavorId());
                } else {
                    LOGGER.debug("Nothing to add since flavor: " + aaiFlavor.getFlavorId() + "already exists in AAI.");
                }
            } catch (ActiveAndAvailableInventoryException e) {
                throw new HeatBridgeException("Failed to update flavor to AAI: " + aaiFlavor.getFlavorId() + ". Error"
                    + " cause: " + e, e);
            }
        }
    }

    @Override
    public void buildAddVserversToAaiAction(final String genericVnfId, final String vfModuleId,
        final List<Server> servers) {
        servers.forEach(server -> {
            Vserver vserver = aaiHelper.buildVserver(server.getId(), server);

            // Build vserver relationships to: image, flavor, pserver, vf-module
            vserver.setRelationshipList(aaiHelper.getVserverRelationshipList(cloudOwner, cloudRegionId, genericVnfId,
                vfModuleId, server));
            aaiActions.add(new AddVserver(vserver, cloudOwner, cloudRegionId, tenantId));
        });
    }

    @Override
    public void buildAddVserverLInterfacesToAaiAction(final List<Resource> stackResources,
        final List<String> oobMgtNetIds) {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);
        List<String> portIds = extractStackResourceIdsByResourceType(stackResources,
            HeatBridgeConstants.OS_PORT_RESOURCE_TYPE);
        for (String portId : portIds) {
            Port port = osClient.getPortById(portId);
            LInterface lIf = new LInterface();
            lIf.setInterfaceId(port.getId());
            lIf.setInterfaceName(port.getName());
            lIf.setMacaddr(port.getMacAddress());
            if (oobMgtNetIds != null && oobMgtNetIds.contains(port.getNetworkId())) {
                lIf.setInterfaceRole(OOB_MGT_NETWORK_IDENTIFIER);
            } else {
                lIf.setInterfaceRole(port.getvNicType());
            }

            updateLInterfaceIps(port, lIf);
            updateLInterfaceVlan(port, lIf);

            // Update l-interface to the vserver
            aaiActions.add(new AddLInterfaceToVserver(lIf, cloudOwner, cloudRegionId, tenantId, port
                .getDeviceId()));
        }
    }

    private void updateLInterfaceVlan(final Port port, final LInterface lIf) {
        Vlan vlan = new Vlan();
        Network network = osClient.getNetworkById(port.getNetworkId());
        lIf.setNetworkName(network.getName());
        if (network.getNetworkType().equals(NetworkType.VLAN)) {
            vlan.setVlanInterface(network.getProviderSegID());
            Vlans vlans = new Vlans();
            List<Vlan> vlanList = vlans.getVlan();
            vlanList.add(vlan);
            lIf.setVlans(vlans);
        }
        // Build sriov-vf to the l-interface
        if (port.getvNicType().equalsIgnoreCase(HeatBridgeConstants.OS_SRIOV_PORT_TYPE)) {
            SriovVfs sriovVfs = new SriovVfs();
            // JAXB does not generate setters for list, however getter ensures its creation.
            // Thus, all list manipulations must be made on live list.
            List<SriovVf> sriovVfList = sriovVfs.getSriovVf();
            SriovVf sriovVf = new SriovVf();
            sriovVf.setPciId(port.getProfile().get(HeatBridgeConstants.OS_PCI_SLOT_KEY).toString());
            sriovVf.setNeutronNetworkId(port.getNetworkId());
            if (port.getVifDetails() != null) {
                sriovVf.setVfVlanFilter((String) port.getVifDetails().get(HeatBridgeConstants.OS_VLAN_NETWORK_KEY));
            }
            sriovVfList.add(sriovVf);

            lIf.setSriovVfs(sriovVfs);

            // For the given port create sriov-pf for host pserver/p-interface if absent
            updateSriovPfToPserver(port, lIf);
        }
    }

    /**
     * Needs to be corrected according to the specification that is in draft
     * If pserver/p-interface does not have a SRIOV-PF object matching the PCI-ID of the Openstack port object, then
     * create it in AAI.
     * Openstack SRIOV Port object has pci-id (to match sriov-pf on pserver/p-interface), physical-network ID (that
     * matches the p-interface name).
     *
     * @param port Openstack port object
     * @param lIf AAI l-interface object
     */
    private void updateSriovPfToPserver(final Port port, final LInterface lIf) {
        if (port.getProfile() == null || Strings
            .isNullOrEmpty(port.getProfile().get(HeatBridgeConstants.OS_PHYSICAL_NETWORK_KEY).toString())) {
            LOGGER.debug("The SRIOV port:" + port.getName() + " is missing physical-network-id, cannot update "
                + "sriov-pf object for host pserver: " + port.getHostId());
            return;
        }
        Optional<String> matchingPifName = HeatBridgeUtils
            .getMatchingPserverPifName(port.getProfile().get(HeatBridgeConstants.OS_PHYSICAL_NETWORK_KEY).toString());
        if (matchingPifName.isPresent()) {
            // Update l-interface description
            String pserverHostName = port.getHostId();
            lIf.setInterfaceDescription(
                "Attached to SR-IOV port: " + pserverHostName + "::" + matchingPifName.get());
            try {
                PInterface matchingPIf = aaiClient
                    .getPserverPInterfaceByName(pserverHostName, matchingPifName.get());
                SriovPfs pIfSriovPfs = matchingPIf.getSriovPfs();
                if (pIfSriovPfs == null) {
                    pIfSriovPfs = new SriovPfs();
                }
                // Extract PCI-ID from OS port object
                String pfPciId = port.getProfile().get(HeatBridgeConstants.OS_PCI_SLOT_KEY).toString();

                List<SriovPf> existingSriovPfs = pIfSriovPfs.getSriovPf();
                if (CollectionUtils.isEmpty(existingSriovPfs) || existingSriovPfs.stream()
                    .noneMatch(existingSriovPf -> existingSriovPf.getPfPciId().equals(pfPciId))) {
                    // Add sriov-pf object with PCI-ID to AAI
                    SriovPf sriovPf = new SriovPf();
                    sriovPf.setPfPciId(pfPciId);
                    LOGGER.debug("Queuing AAI command to update sriov-pf object to pserver: " + pserverHostName + "/" +
                        matchingPifName.get());
                    aaiActions.add(new AddSriovPfToPServerPif(sriovPf, pserverHostName, matchingPifName.get()));
                }
            } catch (ActiveAndAvailableInventoryException e) {
                // Silently log that we failed to update the Pserver p-interface with PCI-ID
                LOGGER.error(MessageEnum.GENERAL_EXCEPTION, matchingPifName.get(), pserverHostName, "OpenStack",
                    "Heatbridge", MsoLogger.ErrorCode.DataError, "Exception - Failed to add sriov-pf object to pserver", e);
            }
        }
    }

    private void updateLInterfaceIps(final Port port, final LInterface lIf) {
        List<L3InterfaceIpv4AddressList> lInterfaceIps = lIf.getL3InterfaceIpv4AddressList();
        for (IP ip : port.getFixedIps()) {
            String ipAddress = ip.getIpAddress();
            if (InetAddressValidator.getInstance().isValidInet4Address(ipAddress)) {
                L3InterfaceIpv4AddressList lInterfaceIp = new L3InterfaceIpv4AddressList();
                lInterfaceIp.setL3InterfaceIpv4Address(ipAddress);
                lInterfaceIp.setNeutronNetworkId(port.getNetworkId());
                lInterfaceIp.setNeutronSubnetId(ip.getSubnetId());
                lInterfaceIp.setL3InterfaceIpv4PrefixLength(32L);
                lInterfaceIps.add(lInterfaceIp);
            }
        }
    }

    @Override
    public void submitToAai() throws HeatBridgeException {
        for (AaiAction<ActiveAndAvailableInventory> aaiAction : aaiActions) {
            try {
                aaiAction.submit(aaiClient);
            } catch (ActiveAndAvailableInventoryException | UnsupportedEncodingException e) {
                String msg = "Failed to execute AAI command:" + aaiAction.getClass().getSimpleName();
                LOGGER.debug(msg + " with error: " + e);
                throw new HeatBridgeException(msg, e);
            }
        }
    }

    @Override
    public boolean rollbackFromAai() {
        final RollbackFromAai rollbackCmd = new RollbackFromAai(aaiClient);
        return rollbackCmd.execute(aaiActions);
    }

    private <T> Predicate<T> distinctByProperty(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
