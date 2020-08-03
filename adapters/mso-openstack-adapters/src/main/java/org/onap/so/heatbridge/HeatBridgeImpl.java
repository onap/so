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
 * Copyright (C) 2018 Bell Canada. All rights reserved.
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
package org.onap.so.heatbridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.onap.aai.domain.yang.Flavor;
import org.onap.aai.domain.yang.Image;
import org.onap.aai.domain.yang.L3InterfaceIpv4AddressList;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.SriovPf;
import org.onap.aai.domain.yang.SriovPfs;
import org.onap.aai.domain.yang.Subnets;
import org.onap.aai.domain.yang.SriovVf;
import org.onap.aai.domain.yang.SriovVfs;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.Vlan;
import org.onap.aai.domain.yang.Vlans;
import org.onap.aai.domain.yang.Vserver;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.AAISingleTransactionClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.aaiclient.client.graphinventory.exceptions.BulkProcessFailed;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.ServerType;
import org.onap.so.heatbridge.constants.HeatBridgeConstants;
import org.onap.so.heatbridge.factory.MsoCloudClientFactoryImpl;
import org.onap.so.heatbridge.helpers.AaiHelper;
import org.onap.so.heatbridge.openstack.api.OpenstackClient;
import org.onap.so.heatbridge.openstack.factory.OpenstackClientFactoryImpl;
import org.onap.so.heatbridge.utils.HeatBridgeUtils;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.onap.so.spring.SpringContextHelper;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.heat.Resource;
import org.openstack4j.model.network.IP;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.NetworkType;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.Subnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import inet.ipaddr.IPAddressString;

/**
 * This class provides an implementation of {@link HeatBridgeApi}
 */
public class HeatBridgeImpl implements HeatBridgeApi {

    private static final Logger logger = LoggerFactory.getLogger(HeatBridgeImpl.class);
    private static final String ERR_MSG_NULL_OS_CLIENT =
            "Initialization error: Null openstack client. Authenticate with Keystone first.";
    private static final String OOB_MGT_NETWORK_IDENTIFIER = "Management";
    private OpenstackClient osClient;
    private AAIResourcesClient resourcesClient;
    private AAISingleTransactionClient transaction;
    private String cloudOwner;
    private String cloudRegionId;
    private String regionId;
    private String tenantId;
    private AaiHelper aaiHelper = new AaiHelper();
    private CloudIdentity cloudIdentity;
    private Environment env;


    public HeatBridgeImpl(AAIResourcesClient resourcesClient, final CloudIdentity cloudIdentity,
            @Nonnull final String cloudOwner, @Nonnull final String cloudRegionId, @Nonnull final String regionId,
            @Nonnull final String tenantId) {
        Objects.requireNonNull(cloudOwner, "Null cloud-owner value!");
        Objects.requireNonNull(cloudRegionId, "Null cloud-region identifier!");
        Objects.requireNonNull(tenantId, "Null tenant identifier!");
        Objects.requireNonNull(regionId, "Null regionId identifier!");

        this.cloudIdentity = cloudIdentity;
        this.cloudOwner = cloudOwner;
        this.cloudRegionId = cloudRegionId;
        this.regionId = regionId;
        this.tenantId = tenantId;
        this.resourcesClient = resourcesClient;
        if (resourcesClient != null)
            this.transaction = resourcesClient.beginSingleTransaction();
        if (SpringContextHelper.getAppContext() != null)
            this.env = SpringContextHelper.getAppContext().getEnvironment();
    }

    public HeatBridgeImpl() {
        this.resourcesClient = new AAIResourcesClient();
        this.transaction = resourcesClient.beginSingleTransaction();
    }

    @Override
    public OpenstackClient authenticate() throws HeatBridgeException {
        String keystoneVersion = "";
        if (ServerType.KEYSTONE.equals(cloudIdentity.getIdentityServerType()))
            keystoneVersion = "v2.0";
        else if (ServerType.KEYSTONE_V3.equals(cloudIdentity.getIdentityServerType())) {
            keystoneVersion = "v3";
        } else {
            keystoneVersion = "UNKNOWN";
        }
        logger.trace("Keystone Version: {} ", keystoneVersion);
        this.osClient = new MsoCloudClientFactoryImpl(new OpenstackClientFactoryImpl()).getOpenstackClient(
                cloudIdentity.getIdentityUrl(), cloudIdentity.getMsoId(), cloudIdentity.getMsoPass(), regionId,
                tenantId, keystoneVersion, cloudIdentity.getUserDomainName(), cloudIdentity.getProjectDomainName());
        logger.trace("Successfully authenticated with keystone for tenant: {} and region: {}", tenantId, regionId);
        return osClient;
    }

    @Override
    public List<Resource> queryNestedHeatStackResources(final String heatStackId) {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);
        Preconditions.checkState(!Strings.isNullOrEmpty(heatStackId), "Invalid heatStackId!");
        List<Resource> stackBasedResources =
                osClient.getStackBasedResources(heatStackId, HeatBridgeConstants.OS_DEFAULT_HEAT_NESTING);
        logger.debug(stackBasedResources.size() + " heat stack resources are extracted for stack: " + heatStackId);
        return stackBasedResources;
    }

    @Override
    public List<String> extractStackResourceIdsByResourceType(final List<Resource> stackResources,
            final String resourceType) {
        return stackResources.stream().filter(stackResource -> stackResource.getType().equals(resourceType))
                .map(Resource::getPhysicalResourceId).collect(Collectors.toList());
    }

    @Override
    public List<String> extractNetworkIds(final List<String> networkNameList) {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);
        return networkNameList.stream()
                .map(netName -> osClient
                        .listNetworksByFilter(ImmutableMap.of(HeatBridgeConstants.OS_NAME_KEY, netName)))
                .filter(nets -> nets != null && nets.size() == 1) // extract network-id only if network-name is unique
                .map(nets -> nets.get(0).getId()).collect(Collectors.toList());
    }

    @Override
    public List<Server> getAllOpenstackServers(final List<Resource> stackResources) {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);
        // Filter Openstack Compute resources
        List<String> serverIds =
                extractStackResourceIdsByResourceType(stackResources, HeatBridgeConstants.OS_SERVER_RESOURCE_TYPE);
        return serverIds.stream().map(serverId -> osClient.getServerById(serverId)).collect(Collectors.toList());
    }

    @Override
    public List<Network> getAllOpenstackProviderNetworks(final List<Resource> stackResources) {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);
        // Filter Openstack Compute resources
        List<String> providerNetworkIds =
                extractStackResourceIdsByResourceType(stackResources, HeatBridgeConstants.OS_NEUTRON_PROVIDERNET);
        return providerNetworkIds.stream().map(providerNetworkId -> osClient.getNetworkById(providerNetworkId))
                .collect(Collectors.toList());
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

    public void buildAddNetworksToAaiAction(final String genericVnfId, final String vfModuleId,
            List<Network> networks) {
        networks.forEach(network -> {
            L3Network l3Network = aaiHelper.buildNetwork(network);
            if (l3Network != null) {
                l3Network.setSubnets(buildSunets(network));

                RelationshipList relationshipList = new RelationshipList();
                List<Relationship> relationships = relationshipList.getRelationship();

                relationships.add(aaiHelper.getRelationshipToVfModule(genericVnfId, vfModuleId));
                relationships.add(aaiHelper.getRelationshipToTenant(cloudOwner, cloudRegionId, tenantId));

                l3Network.setRelationshipList(relationshipList);
                transaction.createIfNotExists(
                        AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, l3Network.getNetworkId()),
                        Optional.of(l3Network));
            }
        });
    }

    @Override
    public void buildAddImagesToAaiAction(final List<org.openstack4j.model.compute.Image> images)
            throws HeatBridgeException {
        for (org.openstack4j.model.compute.Image image : images) {
            Image aaiImage = aaiHelper.buildImage(image);
            try {
                AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.IMAGE, cloudOwner, cloudRegionId,
                        aaiImage.getImageId());
                if (!resourcesClient.exists(uri)) {
                    transaction.create(uri, aaiImage);
                    logger.debug("Queuing AAI command to add image: " + aaiImage.getImageId());
                } else {
                    logger.debug("Nothing to add since image: " + aaiImage.getImageId() + "already exists in AAI.");
                }
            } catch (WebApplicationException e) {
                throw new HeatBridgeException(
                        "Failed to update image to AAI: " + aaiImage.getImageId() + ". Error" + " cause: " + e, e);
            }
        }
    }

    @Override
    public void buildAddFlavorsToAaiAction(final List<org.openstack4j.model.compute.Flavor> flavors)
            throws HeatBridgeException {
        for (org.openstack4j.model.compute.Flavor flavor : flavors) {
            Flavor aaiFlavor = aaiHelper.buildFlavor(flavor);
            try {
                AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.FLAVOR, cloudOwner, cloudRegionId,
                        aaiFlavor.getFlavorId());
                transaction.createIfNotExists(uri, Optional.of(aaiFlavor));
            } catch (WebApplicationException e) {
                throw new HeatBridgeException(
                        "Failed to update flavor to AAI: " + aaiFlavor.getFlavorId() + ". Error" + " cause: " + e, e);
            }
        }
    }

    @Override
    public void buildAddVserversToAaiAction(final String genericVnfId, final String vfModuleId,
            final List<Server> servers) {
        servers.forEach(server -> {
            Vserver vserver = aaiHelper.buildVserver(server.getId(), server);

            // Build vserver relationships to: image, flavor, pserver, vf-module
            vserver.setRelationshipList(
                    aaiHelper.getVserverRelationshipList(cloudOwner, cloudRegionId, genericVnfId, vfModuleId, server));
            transaction.createIfNotExists(AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner,
                    cloudRegionId, tenantId, vserver.getVserverId()), Optional.of(vserver));
        });
    }

    @Override
    public void buildAddVserverLInterfacesToAaiAction(final List<Resource> stackResources,
            final List<String> oobMgtNetIds, String cloudOwner) {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);
        List<String> portIds =
                extractStackResourceIdsByResourceType(stackResources, HeatBridgeConstants.OS_PORT_RESOURCE_TYPE);
        for (String portId : portIds) {
            Port port = osClient.getPortById(portId);
            Network network = osClient.getNetworkById(port.getNetworkId());
            LInterface lIf = new LInterface();
            lIf.setInterfaceId(port.getId());
            lIf.setInterfaceName(port.getName());
            lIf.setMacaddr(port.getMacAddress());
            lIf.setNetworkName(network.getName());
            lIf.setIsPortMirrored(false);
            lIf.setIsIpUnnumbered(false);
            lIf.setInMaint(false);
            if (oobMgtNetIds != null && oobMgtNetIds.contains(port.getNetworkId())) {
                lIf.setInterfaceRole(OOB_MGT_NETWORK_IDENTIFIER);
            } else {
                lIf.setInterfaceRole(port.getvNicType());
            }
            boolean isL2Multicast = false;
            if (port.getProfile().get("trusted") != null) {
                String trusted = port.getProfile().get("trusted").toString();
                if (Boolean.parseBoolean(trusted)) {
                    isL2Multicast = true;
                }
            }
            lIf.setL2Multicasting(isL2Multicast);

            transaction.createIfNotExists(AAIUriFactory.createResourceUri(AAIObjectType.L_INTERFACE, cloudOwner,
                    cloudRegionId, tenantId, port.getDeviceId(), lIf.getInterfaceName()), Optional.of(lIf));

            updateLInterfaceIps(port, lIf);
            if (cloudOwner.equals(env.getProperty("mso.cloudOwner.included", ""))) {
                updateLInterfaceVlan(port, lIf);
            }

            updateSriovPfToPserver(port, lIf);
        }
    }

    @Override
    public void createPserversAndPinterfacesIfNotPresentInAai(final List<Resource> stackResources)
            throws HeatBridgeException {
        if (stackResources == null) {
            return;
        }
        Map<String, Pserver> serverHostnames = getPserverMapping(stackResources);
        createPServerIfNotExists(serverHostnames);
        List<String> portIds =
                extractStackResourceIdsByResourceType(stackResources, HeatBridgeConstants.OS_PORT_RESOURCE_TYPE);
        for (String portId : portIds) {
            Port port = osClient.getPortById(portId);
            if (port.getvNicType().equalsIgnoreCase(HeatBridgeConstants.OS_SRIOV_PORT_TYPE)) {
                createPServerPInterfaceIfNotExists(serverHostnames.get(port.getHostId()).getHostname(),
                        aaiHelper.buildPInterface(port));
            }
        }
    }

    private Map<String, Pserver> getPserverMapping(final List<Resource> stackResources) {
        List<Server> osServers = getAllOpenstackServers(stackResources);
        Map<String, Pserver> pserverMap = new HashMap<>();
        if (osServers != null) {
            for (Server server : osServers) {
                Pserver pserver = aaiHelper.buildPserver(server);
                if (pserver != null) {
                    pserverMap.put(server.getHost(), pserver);
                }
            }
        }
        return pserverMap;
    }

    private Subnets buildSunets(Network network) {
        Subnets aaiSubnets = new Subnets();
        List<String> subnetIds = network.getSubnets();

        subnetIds.forEach(subnetId -> {
            Subnet subnet = osClient.getSubnetById(subnetId);
            org.onap.aai.domain.yang.Subnet aaiSubnet = aaiHelper.buildSubnet(subnet);
            if (aaiSubnet != null) {
                aaiSubnets.getSubnet().add(aaiSubnet);
            }
        });
        return aaiSubnets;
    }

    private void createPServerIfNotExists(Map<String, Pserver> serverHostnames) {
        for (Pserver pserver : serverHostnames.values()) {
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.PSERVER, pserver.getHostname());
            resourcesClient.createIfNotExists(uri, Optional.of(pserver));
        }
    }

    private void createPServerPInterfaceIfNotExists(String pserverHostname, PInterface pInterface) {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.P_INTERFACE, pserverHostname,
                pInterface.getInterfaceName());
        resourcesClient.createIfNotExists(uri, Optional.of(pInterface));
    }

    private void updateLInterfaceVlan(final Port port, final LInterface lIf) {
        Vlan vlan = new Vlan();
        Network network = osClient.getNetworkById(port.getNetworkId());
        if (network.getNetworkType() != null && network.getNetworkType().equals(NetworkType.VLAN)) {
            vlan.setVlanInterface(port.getName() + network.getProviderSegID());
            vlan.setVlanIdOuter(Long.parseLong(network.getProviderSegID()));
            vlan.setVlanIdInner(0L);
            vlan.setInMaint(false);
            vlan.setIsIpUnnumbered(false);
            vlan.setIsPrivate(false);

            transaction
                    .createIfNotExists(
                            AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                                    .cloudRegion(cloudOwner, cloudRegionId).tenant(tenantId).vserver(port.getDeviceId())
                                    .lInterface(lIf.getInterfaceName()).vlan(vlan.getVlanInterface())),
                            Optional.of(vlan));
        }

        if (port.getvNicType() != null && port.getvNicType().equalsIgnoreCase(HeatBridgeConstants.OS_SRIOV_PORT_TYPE)) {
            SriovVf sriovVf = new SriovVf();
            sriovVf.setPciId(port.getProfile().get(HeatBridgeConstants.OS_PCI_SLOT_KEY).toString());
            sriovVf.setNeutronNetworkId(port.getNetworkId());
            sriovVf.setVfVlanFilter("0");
            sriovVf.setVfVlanAntiSpoofCheck(false);
            sriovVf.setVfMacAntiSpoofCheck(false);

            transaction
                    .createIfNotExists(
                            AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                                    .cloudRegion(cloudOwner, cloudRegionId).tenant(tenantId).vserver(port.getDeviceId())
                                    .lInterface(lIf.getInterfaceName()).sriovVf(sriovVf.getPciId())),
                            Optional.of(sriovVf));
        }
    }

    /**
     * Needs to be corrected according to the specification that is in draft If pserver/p-interface does not have a
     * SRIOV-PF object matching the PCI-ID of the Openstack port object, then create it in AAI. Openstack SRIOV Port
     * object has pci-id (to match sriov-pf on pserver/p-interface), physical-network ID (that matches the p-interface
     * name).
     *
     * @param port Openstack port object
     * @param lIf AAI l-interface object
     */
    private void updateSriovPfToPserver(final Port port, final LInterface lIf) {
        if (port.getvNicType().equalsIgnoreCase(HeatBridgeConstants.OS_SRIOV_PORT_TYPE)) {
            if (port.getProfile() == null || Strings
                    .isNullOrEmpty(port.getProfile().get(HeatBridgeConstants.OS_PHYSICAL_NETWORK_KEY).toString())) {
                logger.debug("The SRIOV port:" + port.getName() + " is missing physical-network-id, cannot update "
                        + "sriov-pf object for host pserver: " + port.getHostId());
                return;
            }
            Optional<String> matchingPifName = HeatBridgeUtils.getMatchingPserverPifName(
                    port.getProfile().get(HeatBridgeConstants.OS_PHYSICAL_NETWORK_KEY).toString());
            if (matchingPifName.isPresent()) {
                // Update l-interface description
                String pserverHostName = port.getHostId();
                lIf.setInterfaceDescription(
                        "Attached to SR-IOV port: " + pserverHostName + "::" + matchingPifName.get());
                try {
                    Optional<PInterface> matchingPIf = resourcesClient.get(PInterface.class, AAIUriFactory
                            .createResourceUri(AAIObjectType.P_INTERFACE, pserverHostName, matchingPifName.get())
                            .depth(Depth.ONE));
                    if (matchingPIf.isPresent()) {
                        SriovPfs pIfSriovPfs = matchingPIf.get().getSriovPfs();
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
                            logger.debug("Queuing AAI command to update sriov-pf object to pserver: " + pserverHostName
                                    + "/" + matchingPifName.get());

                            AAIResourceUri sriovPfUri = AAIUriFactory.createResourceUri(AAIObjectType.SRIOV_PF,
                                    pserverHostName, matchingPifName.get(), sriovPf.getPfPciId());

                            if (!resourcesClient.exists(sriovPfUri)) {
                                transaction.create(sriovPfUri, sriovPf);

                                AAIResourceUri sriovVfUri = AAIUriFactory.createResourceUri(AAIObjectType.SRIOV_VF,
                                        cloudOwner, cloudRegionId, tenantId, port.getDeviceId(), lIf.getInterfaceName(),
                                        port.getProfile().get(HeatBridgeConstants.OS_PCI_SLOT_KEY).toString());

                                transaction.connect(sriovPfUri, sriovVfUri);
                            }
                        }
                    }
                } catch (WebApplicationException e) {
                    // Silently log that we failed to update the Pserver p-interface with PCI-ID
                    logger.error(LoggingAnchor.NINE, MessageEnum.GENERAL_EXCEPTION, pserverHostName,
                            matchingPifName.get(), cloudOwner, tenantId, "OpenStack", "Heatbridge",
                            ErrorCode.DataError.getValue(), "Exception - Failed to add sriov-pf object to pserver", e);
                }
            }
        }
    }

    private void updateLInterfaceIps(final Port port, final LInterface lIf) {
        for (IP ip : port.getFixedIps()) {
            String ipAddress = ip.getIpAddress();
            if (InetAddressValidator.getInstance().isValidInet4Address(ipAddress)) {
                Subnet subnet = osClient.getSubnetById(ip.getSubnetId());
                IPAddressString cidr = new IPAddressString(subnet.getCidr());
                L3InterfaceIpv4AddressList lInterfaceIp = new L3InterfaceIpv4AddressList();
                lInterfaceIp.setL3InterfaceIpv4Address(ipAddress);
                lInterfaceIp.setNeutronNetworkId(port.getNetworkId());
                lInterfaceIp.setNeutronSubnetId(ip.getSubnetId());
                lInterfaceIp.setL3InterfaceIpv4PrefixLength(Long.parseLong(cidr.getNetworkPrefixLength().toString()));

                transaction.createIfNotExists(
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                                .cloudRegion(cloudOwner, cloudRegionId).tenant(tenantId).vserver(port.getDeviceId())
                                .lInterface(lIf.getInterfaceName()).l3InterfaceIpv4AddressList(ipAddress)),
                        Optional.of(lInterfaceIp));
            }
        }
    }

    @Override
    public void submitToAai(boolean dryrun) throws HeatBridgeException {
        try {
            transaction.execute(dryrun);
        } catch (BulkProcessFailed e) {
            String msg = "Failed to commit transaction";
            logger.debug(msg + " with error: " + e);
            throw new HeatBridgeException(msg, e);
        }
    }

    @Override
    public void deleteVfModuleData(@Nonnull final String vnfId, @Nonnull final String vfModuleId)
            throws HeatBridgeException {
        Objects.requireNonNull(vnfId, "Null vnf-id!");
        Objects.requireNonNull(vfModuleId, "Null vf-module-id!");
        try {
            Optional<VfModule> vfModule = resourcesClient
                    .get(AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnfId, vfModuleId).depth(Depth.ONE),
                            NotFoundException.class)
                    .asBean(VfModule.class);

            AAIResultWrapper resultWrapper = new AAIResultWrapper(vfModule.get());
            Optional<Relationships> relationships = resultWrapper.getRelationships();
            logger.debug("VfModule contains relationships in AAI: {}", relationships.isPresent());
            if (relationships.isPresent()) {
                List<AAIResourceUri> vserverUris = relationships.get().getRelatedUris(AAIObjectType.VSERVER);
                logger.debug("VServer contains {} relationships in AAI", vserverUris.size());
                createTransactionToDeleteSriovPfFromPserver(vserverUris);

                if (!vserverUris.isEmpty()) {
                    for (AAIResourceUri vserverUri : vserverUris) {
                        if (env.getProperty("heatBridgeDryrun", Boolean.class, true)) {
                            logger.debug("Would delete Vserver: {}", vserverUri.build().toString());
                        } else {
                            resourcesClient.delete(vserverUri);
                        }
                    }
                }
            }

        } catch (NotFoundException e) {
            String msg = "Failed to commit delete heatbridge data transaction";
            logger.debug(msg + " with error: " + e);
            throw new HeatBridgeException(msg, e);
        } catch (Exception e) {
            String msg = "Failed to commit delete heatbridge data transaction";
            logger.debug(msg + " with error: " + e);
            throw new HeatBridgeException(msg, e);
        }
    }

    private void createTransactionToDeleteSriovPfFromPserver(List<AAIResourceUri> vserverUris) {
        Map<String, List<String>> pserverToPciIdMap = getPserverToPciIdMap(vserverUris);
        for (Map.Entry<String, List<String>> entry : pserverToPciIdMap.entrySet()) {
            String pserverName = entry.getKey();
            List<String> pciIds = entry.getValue();
            Optional<Pserver> pserver = resourcesClient.get(Pserver.class,
                    AAIUriFactory.createResourceUri(AAIObjectType.PSERVER, pserverName).depth(Depth.TWO));
            if (pserver.isPresent()) {
                // For each pserver/p-interface match sriov-vfs by pic-id and delete them.
                pserver.get().getPInterfaces().getPInterface().stream().filter(
                        pIf -> pIf.getSriovPfs() != null && CollectionUtils.isNotEmpty(pIf.getSriovPfs().getSriovPf()))
                        .forEach(pIf -> pIf.getSriovPfs().getSriovPf().forEach(sriovPf -> {
                            if (pciIds.contains(sriovPf.getPfPciId())) {
                                logger.debug("creating transaction to delete SR-IOV PF: " + pIf.getInterfaceName()
                                        + " from PServer: " + pserverName);
                                if (env.getProperty("heatBridgeDryrun", Boolean.class, true)) {
                                    logger.debug("Would delete Sriov Pf: {}",
                                            AAIUriFactory
                                                    .createResourceUri(AAIObjectType.SRIOV_PF, pserverName,
                                                            pIf.getInterfaceName(), sriovPf.getPfPciId())
                                                    .build().toString());
                                } else {
                                    resourcesClient.delete(AAIUriFactory.createResourceUri(AAIObjectType.SRIOV_PF,
                                            pserverName, pIf.getInterfaceName(), sriovPf.getPfPciId()));
                                }
                            }
                        }));
            }
        }
    }

    private Map<String, List<String>> getPserverToPciIdMap(List<AAIResourceUri> vserverUris) {
        Map<String, List<String>> pserverToPciIdMap = new HashMap<>();
        for (AAIResourceUri vserverUri : vserverUris) {
            AAIResultWrapper vserverWrapper = resourcesClient.get(vserverUri.depth(Depth.TWO));
            Optional<Relationships> vserverRelationships = vserverWrapper.getRelationships();
            if (vserverRelationships.isPresent()
                    && CollectionUtils.isNotEmpty(vserverRelationships.get().getRelatedLinks(AAIObjectType.PSERVER))) {
                Vserver vserver = vserverWrapper.asBean(Vserver.class).get();
                List<String> pciIds = HeatBridgeUtils.extractPciIdsFromVServer(vserver);
                if (CollectionUtils.isNotEmpty(pciIds)) {
                    List<AAIResourceUri> matchingPservers =
                            vserverRelationships.get().getRelatedUris(AAIObjectType.PSERVER);
                    if (matchingPservers != null && matchingPservers.size() == 1) {
                        pserverToPciIdMap.put(matchingPservers.get(0).getURIKeys().get("hostname"), pciIds);
                    }
                }
            }
        }
        return pserverToPciIdMap;
    }

    private <T> Predicate<T> distinctByProperty(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
