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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import inet.ipaddr.IPAddressString;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.onap.aai.domain.yang.*;
import org.onap.aaiclient.client.aai.AAIDSLQueryClient;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.AAISingleTransactionClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.Results;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.graphinventory.Format;
import org.onap.aaiclient.client.graphinventory.entities.*;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.aaiclient.client.graphinventory.exceptions.BulkProcessFailed;
import org.onap.so.cloud.resource.beans.NodeType;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.ServerType;
import org.onap.so.heatbridge.constants.HeatBridgeConstants;
import org.onap.so.heatbridge.factory.MsoCloudClientFactoryImpl;
import org.onap.so.heatbridge.helpers.AaiHelper;
import org.onap.so.heatbridge.openstack.api.OpenstackClient;
import org.onap.so.heatbridge.openstack.factory.OpenstackClientFactoryImpl;
import org.onap.so.heatbridge.utils.HeatBridgeUtils;
import org.onap.so.spring.SpringContextHelper;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.heat.Resource;
import org.openstack4j.model.network.IP;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.NetworkType;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.model.network.*;
import org.openstack4j.model.storage.block.VolumeAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * This class provides an implementation of {@link HeatBridgeApi}
 */
public class HeatBridgeImpl implements HeatBridgeApi {

    private static final Logger logger = LoggerFactory.getLogger(HeatBridgeImpl.class);
    private static final String ERR_MSG_NULL_OS_CLIENT =
            "Initialization error: Null openstack client. Authenticate with Keystone first.";
    private static final String OOB_MGT_NETWORK_IDENTIFIER = "Management";

    protected static final String DIRECT = "direct";
    protected static final String PCI_SLOT = "pci_slot";
    protected static final String OVSNET = "ovsnet";
    protected static final String SRIOV = "SRIOV";
    protected static final String RESOURCE_LINK = "resource-link";

    protected static final Object PRIVATE_VLANS = "private_vlans";
    protected static final Object PUBLIC_VLANS = "public_vlans";

    protected ObjectMapper mapper = new ObjectMapper();

    private OpenstackClient osClient;
    private AAIResourcesClient resourcesClient;
    private AAIDSLQueryClient aaiDSLClient;
    private AAISingleTransactionClient transaction;
    private String cloudOwner;
    private String cloudRegionId;
    private String regionId;
    private String tenantId;
    private NodeType nodeType;
    private AaiHelper aaiHelper = new AaiHelper();
    private CloudIdentity cloudIdentity;
    private Environment env;

    public HeatBridgeImpl(AAIResourcesClient resourcesClient, final CloudIdentity cloudIdentity,
            @Nonnull final String cloudOwner, @Nonnull final String cloudRegionId, @Nonnull final String regionId,
            @Nonnull final String tenantId, @Nonnull final NodeType nodeType) {
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
        this.nodeType = nodeType;
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


    protected Server getOpenstackServerById(String deviceId) {
        return osClient.getServerById(deviceId);
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
        return servers.stream().filter(s -> s.getImage() != null).map(Server::getImage)
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
                        AAIUriFactory
                                .createResourceUri(AAIFluentTypeBuilder.network().l3Network(l3Network.getNetworkId())),
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
                AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion(cloudOwner, cloudRegionId).image(aaiImage.getImageId()));
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
                AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion(cloudOwner, cloudRegionId).flavor(aaiFlavor.getFlavorId()));
                transaction.createIfNotExists(uri, Optional.of(aaiFlavor));
            } catch (WebApplicationException e) {
                throw new HeatBridgeException(
                        "Failed to update flavor to AAI: " + aaiFlavor.getFlavorId() + ". Error" + " cause: " + e, e);
            }
        }
    }

    @Override
    public void buildAddVserversToAaiAction(final String genericVnfId, final String vfModuleId,
            final List<Server> servers) throws HeatBridgeException {
        for (Server server : servers) {
            Vserver vserver = aaiHelper.buildVserver(server.getId(), server);

            // Build vserver relationships to: image, flavor, pserver, vf-module
            vserver.setRelationshipList(
                    aaiHelper.getVserverRelationshipList(cloudOwner, cloudRegionId, genericVnfId, vfModuleId, server));
            AAIResourceUri vserverUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                    .cloudRegion(cloudOwner, cloudRegionId).tenant(tenantId).vserver(vserver.getVserverId()));

            if (resourcesClient.exists(vserverUri)) {
                AAIResultWrapper existingVserver = resourcesClient.get(vserverUri);
                AAIResourceUri vfModuleUri = AAIUriFactory.createResourceUri(
                        AAIFluentTypeBuilder.network().genericVnf(genericVnfId).vfModule(vfModuleId));
                if (!existingVserver.hasRelationshipsTo(Types.VNFC)) {
                    AAIResultWrapper vfModule = resourcesClient.get(vfModuleUri);
                    if (vfModule.hasRelationshipsTo(Types.VNFC)) {
                        List<AAIResourceUri> vnfcUris = vfModule.getRelationships().get().getRelatedUris(Types.VNFC);
                        Optional<AAIResourceUri> foundVnfcURI = vnfcUris.stream().filter(resourceUri -> resourceUri
                                .getURIKeys().get("vnfc-name").startsWith(vserver.getVserverName())).findFirst();
                        if (foundVnfcURI.isEmpty()) {
                            throw new HeatBridgeException("Cannot Find VNFC to create edge to VServer");
                        }
                        transaction.connect(vserverUri, foundVnfcURI.get());
                    } else {
                        /*
                         * throw new HeatBridgeException(
                         * "VF Module contains no relationships to VNFCS, cannot build edge to VServer");
                         */
                    }
                }

                if (!existingVserver.hasRelationshipsTo(Types.VF_MODULE)) {
                    transaction.connect(vserverUri, vfModuleUri);
                }
                if (!existingVserver.hasRelationshipsTo(Types.PSERVER)) {
                    AAIResourceUri pServerUri = AAIUriFactory.createResourceUri(
                            AAIFluentTypeBuilder.cloudInfrastructure().pserver(server.getHypervisorHostname()));
                    transaction.connect(vserverUri, pServerUri);
                }
            } else {
                transaction.create(vserverUri, vserver);
            }
        }
    }

    @Override
    public void buildAddVserverLInterfacesToAaiAction(final List<Resource> stackResources,
            final List<String> oobMgtNetIds, String cloudOwner) throws HeatBridgeException {
        Objects.requireNonNull(osClient, ERR_MSG_NULL_OS_CLIENT);
        List<String> portIds =
                extractStackResourceIdsByResourceType(stackResources, HeatBridgeConstants.OS_PORT_RESOURCE_TYPE);

        if (portIds == null)
            return;
        for (String portId : portIds) {
            boolean isL2Multicast = false;
            Port port = osClient.getPortById(portId);
            Network network = osClient.getNetworkById(port.getNetworkId());
            if (!StringUtils.isEmpty(port.getDeviceId())) {
                LInterface lIf = new LInterface();
                lIf.setInterfaceId(port.getId());
                lIf.setInterfaceName(port.getName());
                lIf.setMacaddr(port.getMacAddress());
                lIf.setNetworkName(network.getName());
                lIf.setIsPortMirrored(false);
                lIf.setIsIpUnnumbered(false);
                lIf.setInMaint(false);

                if (port.getProfile() != null && port.getProfile().get("trusted") != null) {
                    String trusted = port.getProfile().get("trusted").toString();
                    if (Boolean.parseBoolean(trusted)) {
                        isL2Multicast = true;
                    }
                }
                lIf.setL2Multicasting(isL2Multicast);
                lIf.setInterfaceType(getInterfaceType(nodeType, port.getvNicType()));
                lIf.setRelationshipList(new RelationshipList());

                if (oobMgtNetIds != null && oobMgtNetIds.contains(port.getNetworkId())) {
                    lIf.setInterfaceRole(OOB_MGT_NETWORK_IDENTIFIER);
                } else {
                    lIf.setInterfaceRole(port.getvNicType());
                }

                // Update l-interface to the vserver
                transaction
                        .createIfNotExists(
                                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                                        .cloudRegion(cloudOwner, cloudRegionId).tenant(tenantId)
                                        .vserver(port.getDeviceId()).lInterface(lIf.getInterfaceName())),
                                Optional.of(lIf));

                updateLInterfaceIps(port, lIf);

                if (cloudOwner.equals(env.getProperty("mso.cloudOwner.included", ""))) {
                    Server server = getOpenstackServerById(port.getDeviceId());
                    createVlanAndSriovVF(port, lIf, server.getHypervisorHostname());
                    updateSriovPfToSriovVF(port, lIf);
                }
            }
        }
    }

    @Override
    public void buildAddVolumes(List<Resource> stackResources) throws HeatBridgeException {
        try {
            if (stackResources.stream().anyMatch(r -> r.getType().equals("OS::Cinder::Volume"))) {
                stackResources.stream().filter(r -> r.getType().equalsIgnoreCase("OS::Cinder::Volume"))
                        .forEach(r -> createVolume(r));
            } else {
                logger.debug("Heat stack contains no volumes");
            }
        } catch (Exception e) {
            logger.error("Failed to add volumes to AAI", e);
            throw new HeatBridgeException("Failed to add volumes to AAI", e);
        }

    }

    protected void createVolume(Resource r) {
        org.openstack4j.model.storage.block.Volume osVolume = osClient.getVolumeById(r.getPhysicalResourceId());
        List<? extends VolumeAttachment> attachments = osVolume.getAttachments();
        if (attachments != null) {
            Optional<? extends VolumeAttachment> vserver = attachments.stream().findFirst();
            if (vserver.isPresent()) {
                Volume volume = new Volume();
                volume.setVolumeId(r.getPhysicalResourceId());
                AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion(cloudOwner, cloudRegionId).tenant(tenantId).vserver(vserver.get().getServerId())
                        .volume(r.getPhysicalResourceId()));
                transaction.createIfNotExists(uri, Optional.of(volume));
            } else {
                logger.warn(
                        "Volume {} contains no attachments in openstack. Unable to determine which vserver volume belongs too.",
                        r.getPhysicalResourceId());
            }
        } else {
            logger.warn(
                    "Volume {} contains no attachments in openstack. Unable to determine which vserver volume belongs too.",
                    r.getPhysicalResourceId());
        }
    }

    protected String getInterfaceType(NodeType nodeType, String nicType) {
        logger.debug("nicType: " + nicType + "nodeType: " + nodeType);
        if (DIRECT.equalsIgnoreCase(nicType)) {
            return SRIOV;
        } else {
            if (nodeType == NodeType.GREENFIELD) {
                return NodeType.GREENFIELD.getNetworkTechnologyName();
            } else {
                return NodeType.BROWNFIELD.getNetworkTechnologyName();
            }
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
                Pserver foundServer = serverHostnames.get(port.getHostId());
                if (foundServer != null) {
                    createPServerPInterfaceIfNotExists(foundServer.getHostname(), aaiHelper.buildPInterface(port));
                }
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
                    logger.debug("Adding Pserver: " + server.getHost());
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
            AAIResourceUri uri = AAIUriFactory
                    .createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().pserver(pserver.getHostname()));
            if (resourcesClient.exists(uri)) {
                Pserver updatePserver = new Pserver();
                updatePserver.setPserverId(pserver.getPserverId());
                resourcesClient.update(uri, updatePserver);
            } else {
                resourcesClient.create(uri, pserver);
            }
        }
    }

    private void createPServerPInterfaceIfNotExists(String pserverHostname, PInterface pInterface) {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .pserver(pserverHostname).pInterface(pInterface.getInterfaceName()));
        resourcesClient.createIfNotExists(uri, Optional.of(pInterface));
    }

    protected void createVlanAndSriovVF(final Port port, final LInterface lIf, final String hostName)
            throws HeatBridgeException {
        // add back all vlan logic
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

        if (!lIf.getInterfaceType().equals(SRIOV)) {
            if (nodeType == NodeType.GREENFIELD) {
                validatePhysicalNetwork(port, network);
                processOVS(lIf, hostName, NodeType.GREENFIELD.getInterfaceName());
            } else {
                processOVS(lIf, hostName, NodeType.BROWNFIELD.getInterfaceName());
            }
        }

        List<String> privateVlans = (ArrayList<String>) port.getProfile().get(PRIVATE_VLANS);
        List<String> publicVlans = (ArrayList<String>) port.getProfile().get(PUBLIC_VLANS);
        List<String> vlans = null;
        if (publicVlans != null && !publicVlans.isEmpty()) {
            vlans = publicVlans.stream().filter(publicVlan -> !Strings.isNullOrEmpty(publicVlan))
                    .collect(Collectors.toList());
        } else {
            vlans = new ArrayList<>();
        }

        if (privateVlans != null && !privateVlans.isEmpty()) {
            List<String> temp = privateVlans.stream().filter(privateVlan -> !Strings.isNullOrEmpty(privateVlan))
                    .collect(Collectors.toList());
            vlans.addAll(temp);
        }
        vlans.stream().forEach(vlanLocal -> logger.debug("Vlan Id: {}", vlanLocal));

        processVlanTag(lIf, vlans);

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

    protected String createVlanTagUri(String vlanIdOuter) throws HeatBridgeException {
        int vlanOuterInt = Integer.parseInt(vlanIdOuter);
        DSLQueryBuilder<Start, Node> builder = TraversalBuilder
                .fragment(new DSLStartNode(Types.CLOUD_REGION, __.key("cloud-owner", cloudOwner),
                        __.key("cloud-region-id", cloudRegionId)))
                .to(__.node(Types.VLAN_RANGE))
                .to(__.node(Types.VLAN_TAG, __.key("vlan-id-outer", vlanOuterInt)).output());
        String results = getAAIDSLClient().query(Format.PATHED, new DSLQuery(builder.build()));
        Optional<String> vlanTagURI = findLinkedURI(results);
        if (vlanTagURI.isPresent())
            return vlanTagURI.get();
        else
            throw new HeatBridgeException("Cannot find VlanTag Related Link " + vlanIdOuter);
    }


    protected void processVlanTag(LInterface lInterface, List<String> vlanTags) throws HeatBridgeException {
        for (String vlanTag : vlanTags) {
            Relationship vlanTagRelationship = new Relationship();
            vlanTagRelationship.setRelatedLink(createVlanTagUri(vlanTag));
            lInterface.getRelationshipList().getRelationship().add(vlanTagRelationship);
        }
    }

    protected void validatePhysicalNetwork(Port neutronPort, Network network) throws HeatBridgeException {
        String physicalNetworkType = network.getProviderPhyNet();
        if (!OVSNET.equalsIgnoreCase(physicalNetworkType)) {
            String exceptionMessage = String.format(
                    "The OVS-DPDK port is expected to have a physical network of type ovsnet but was found to have %s instead.",
                    physicalNetworkType);
            throw new HeatBridgeException(exceptionMessage);
        }
    }

    protected void processOVS(LInterface lInterface, String hostName, String interfaceName) {
        Relationship lagRelationship = new Relationship();
        lagRelationship.setRelatedLink(AAIUriFactory
                .createResourceUri(
                        AAIFluentTypeBuilder.cloudInfrastructure().pserver(hostName).lagInterface(interfaceName))
                .build().toString());
        lInterface.getRelationshipList().getRelationship().add(lagRelationship);
    }

    /**
     * Needs to be corrected according to the specification that is in draft If pserver/p-interface does not have a
     * SRIOV-PF object matching the PCI-ID of the Openstack port object, then create it in AAI. Openstack SRIOV Port
     * object has pci-id (to match sriov-pf on pserver/p-interface), physical-network ID (that matches the p-interface
     * name).
     *
     * @param port Openstack port object
     * @param lIf AAI l-interface object
     * @throws HeatBridgeException
     */
    protected void updateSriovPfToSriovVF(final Port port, final LInterface lIf) throws HeatBridgeException {
        if (port.getvNicType().equalsIgnoreCase(HeatBridgeConstants.OS_SRIOV_PORT_TYPE)) {

            AAIResourceUri sriovVfUri = AAIUriFactory
                    .createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(cloudOwner, cloudRegionId)
                            .tenant(tenantId).vserver(port.getDeviceId()).lInterface(lIf.getInterfaceName())
                            .sriovVf(port.getProfile().get(HeatBridgeConstants.OS_PCI_SLOT_KEY).toString()));

            boolean relationshipExist = sriovVfHasSriovPfRelationship(sriovVfUri);


            Server server = getOpenstackServerById(port.getDeviceId());
            String pserverHostName = server.getHypervisorHostname();
            lIf.setInterfaceDescription("Attached to SR-IOV port: " + pserverHostName);

            if (!relationshipExist) {
                AAIResourceUri pserverUri = AAIUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().pserver(pserverHostName));
                if (resourcesClient.exists(pserverUri)) {
                    String pfPciId = port.getProfile().get(HeatBridgeConstants.OS_PF_PCI_SLOT_KEY).toString();

                    DSLQueryBuilder<Start, Node> builder = TraversalBuilder
                            .fragment(new DSLStartNode(Types.PSERVER, __.key("hostname", pserverHostName)))
                            .to(__.node(Types.P_INTERFACE)
                                    .to(__.node(Types.SRIOV_PF, __.key("pf-pci-id", pfPciId)).output()));

                    List<Pathed> results = getAAIDSLClient().queryPathed(new DSLQuery(builder.build()));

                    if (results.size() == 1) {

                        AAIResourceUri sriovPfUri = AAIUriFactory.createResourceFromExistingURI(Types.SRIOV_PF,
                                UriBuilder.fromUri(results.get(0).getResourceLink()).build());

                        transaction.connect(sriovPfUri, sriovVfUri);

                    } else {
                        throw new HeatBridgeException("Unable to find sriov-pf related link " + pfPciId
                                + ". Unexpected results size" + results.size());
                    }
                } else {
                    logger.error("Pserver {} does not exist in AAI. Unable to build sriov-vf to sriov-pf relationship.",
                            pserverHostName);
                    throw new HeatBridgeException("Pserver " + pserverHostName + " does not exist in AAI");
                }
            }
        }
    }

    protected boolean sriovVfHasSriovPfRelationship(AAIResourceUri sriovVfUri) {
        boolean pfRelationshipsExist = false;
        if (resourcesClient.exists(sriovVfUri)) {
            Optional<Relationships> sriovVfRelationships = resourcesClient.get(sriovVfUri).getRelationships();

            if (sriovVfRelationships.isPresent()) {
                List<AAIResourceUri> sriovPfUris = sriovVfRelationships.get().getRelatedUris(Types.SRIOV_PF);
                if (sriovPfUris.size() != 0) {
                    pfRelationshipsExist = true;
                }
            }
        }
        return pfRelationshipsExist;
    }

    protected void updateLInterfaceIps(final Port port, final LInterface lIf) {
        for (IP ip : port.getFixedIps()) {
            String ipAddress = ip.getIpAddress();
            if (InetAddressValidator.getInstance().isValidInet4Address(ipAddress)) {
                Subnet subnet = osClient.getSubnetById(ip.getSubnetId());
                IPAddressString cidr = new IPAddressString(subnet.getCidr());
                L3InterfaceIpv4AddressList lInterfaceIp = new L3InterfaceIpv4AddressList();
                lInterfaceIp.setIsFloating(false);
                lInterfaceIp.setL3InterfaceIpv4Address(ipAddress);
                lInterfaceIp.setNeutronNetworkId(port.getNetworkId());
                lInterfaceIp.setNeutronSubnetId(ip.getSubnetId());
                lInterfaceIp.setL3InterfaceIpv4PrefixLength(Long.parseLong(cidr.getNetworkPrefixLength().toString()));

                transaction.createIfNotExists(
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                                .cloudRegion(cloudOwner, cloudRegionId).tenant(tenantId).vserver(port.getDeviceId())
                                .lInterface(lIf.getInterfaceName()).l3InterfaceIpv4AddressList(ipAddress)),
                        Optional.of(lInterfaceIp));
            } else if (InetAddressValidator.getInstance().isValidInet6Address(ipAddress)) {
                Subnet subnet = osClient.getSubnetById(ip.getSubnetId());
                IPAddressString cidr = new IPAddressString(subnet.getCidr());
                L3InterfaceIpv6AddressList ipv6 = new L3InterfaceIpv6AddressList();
                ipv6.setIsFloating(false);
                ipv6.setL3InterfaceIpv6Address(ipAddress);
                ipv6.setNeutronNetworkId(port.getNetworkId());
                ipv6.setNeutronSubnetId(ip.getSubnetId());
                ipv6.setL3InterfaceIpv6PrefixLength(Long.parseLong(cidr.getNetworkPrefixLength().toString()));

                transaction.createIfNotExists(
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                                .cloudRegion(cloudOwner, cloudRegionId).tenant(tenantId).vserver(port.getDeviceId())
                                .lInterface(lIf.getInterfaceName()).l3InterfaceIpv6AddressList(ipAddress)),
                        Optional.of(ipv6));
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
            AAIResultWrapper vfModule = resourcesClient.get(AAIUriFactory
                    .createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId))
                    .depth(Depth.ONE), NotFoundException.class);

            Optional<Relationships> relationships = vfModule.getRelationships();
            logger.debug("VfModule contains relationships in AAI: {}", relationships.isPresent());
            if (relationships.isPresent()) {

                deleteL3Networks(relationships.get());

                List<AAIResourceUri> vserverUris = relationships.get().getRelatedUris(Types.VSERVER);
                logger.debug("VServer contains {} relationships in AAI", vserverUris.size());
                createTransactionToDeleteSriovPfFromPserver(vserverUris);

                if (!vserverUris.isEmpty()) {
                    for (AAIResourceUri vserverUri : vserverUris) {
                        if (env.getProperty("heatBridgeDryrun", Boolean.class, false)) {
                            logger.debug("Would delete Vserver: {}", vserverUri.build().toString());
                        } else {
                            resourcesClient.deleteIfExists(vserverUri);
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

    protected void deleteL3Networks(Relationships relationships) {
        List<AAIResourceUri> l3NetworkUris = relationships.getRelatedUris(Types.L3_NETWORK);
        logger.debug("L3Network contains {} relationships in AAI", l3NetworkUris.size());

        if (!l3NetworkUris.isEmpty()) {
            for (AAIResourceUri l3NetworkUri : l3NetworkUris) {
                if (env.getProperty("heatBridgeDryrun", Boolean.class, true)) {
                    logger.debug("Would delete L3Network: {}", l3NetworkUri.build().toString());
                } else {
                    resourcesClient.delete(l3NetworkUri);
                }
            }
        }
    }

    private void createTransactionToDeleteSriovPfFromPserver(List<AAIResourceUri> vserverUris) {
        Map<String, List<String>> pserverToPciIdMap = getPserverToPciIdMap(vserverUris);
        for (Map.Entry<String, List<String>> entry : pserverToPciIdMap.entrySet()) {
            String pserverName = entry.getKey();
            List<String> pciIds = entry.getValue();
            Optional<Pserver> pserver = resourcesClient.get(Pserver.class,
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().pserver(pserverName))
                            .depth(Depth.TWO));
            if (pserver.isPresent()) {
                // For each pserver/p-interface match sriov-vfs by pic-id and delete them.
                pserver.get().getPInterfaces().getPInterface().stream().filter(
                        pIf -> pIf.getSriovPfs() != null && CollectionUtils.isNotEmpty(pIf.getSriovPfs().getSriovPf()))
                        .forEach(pIf -> pIf.getSriovPfs().getSriovPf().forEach(sriovPf -> {
                            if (pciIds.contains(sriovPf.getPfPciId())) {
                                logger.debug("creating transaction to delete SR-IOV PF: " + pIf.getInterfaceName()
                                        + " from PServer: " + pserverName);
                                if (env.getProperty("heatBridgeDryrun", Boolean.class, false)) {
                                    logger.debug("Would delete Sriov Pf: {}",
                                            AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                                                    .pserver(pserverName).pInterface(pIf.getInterfaceName())
                                                    .sriovPf(sriovPf.getPfPciId())).build());
                                } else {
                                    resourcesClient.delete(AAIUriFactory.createResourceUri(
                                            AAIFluentTypeBuilder.cloudInfrastructure().pserver(pserverName)
                                                    .pInterface(pIf.getInterfaceName()).sriovPf(sriovPf.getPfPciId())));
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
                    && CollectionUtils.isNotEmpty(vserverRelationships.get().getRelatedLinks(Types.PSERVER))) {
                Vserver vserver = vserverWrapper.asBean(Vserver.class).get();
                List<String> pciIds = HeatBridgeUtils.extractPciIdsFromVServer(vserver);
                if (CollectionUtils.isNotEmpty(pciIds)) {
                    List<AAIResourceUri> matchingPservers = vserverRelationships.get().getRelatedUris(Types.PSERVER);
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

    protected Optional<String> findLinkedURI(String jsonResultsString) {
        Results<Map<String, String>> results;
        try {
            results = mapper.readValue(jsonResultsString, new TypeReference<Results<Map<String, String>>>() {});
            if (results.getResult().size() == 1) {
                return Optional.of(results.getResult().get(0).get(RESOURCE_LINK));
            } else if (results.getResult().isEmpty()) {
                return Optional.empty();
            } else {
                throw new IllegalStateException("more than one result returned");
            }
        } catch (IOException e) {
            logger.error("Error retrieving URI from Results JSON", e);
            return Optional.empty();
        }
    }

    protected void setAAIHelper(AaiHelper aaiHelper) {
        this.aaiHelper = aaiHelper;
    }

    protected AAIDSLQueryClient getAAIDSLClient() {
        if (aaiDSLClient == null) {
            aaiDSLClient = new AAIDSLQueryClient();
        }
        return aaiDSLClient;
    }
}
