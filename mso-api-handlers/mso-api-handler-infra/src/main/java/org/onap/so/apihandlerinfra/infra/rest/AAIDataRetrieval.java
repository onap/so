package org.onap.so.apihandlerinfra.infra.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.Service;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aai.domain.yang.VolumeGroups;
import org.onap.aaiclient.client.aai.AAIDSLQueryClient;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.graphinventory.entities.DSLQuery;
import org.onap.aaiclient.client.graphinventory.entities.DSLQueryBuilder;
import org.onap.aaiclient.client.graphinventory.entities.DSLStartNode;
import org.onap.aaiclient.client.graphinventory.entities.Node;
import org.onap.aaiclient.client.graphinventory.entities.Start;
import org.onap.aaiclient.client.graphinventory.entities.TraversalBuilder;
import org.onap.aaiclient.client.graphinventory.entities.__;
import org.onap.so.apihandlerinfra.infra.rest.exception.AAIEntityNotFound;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AAIDataRetrieval {

    private static final String VF_MODULE_NOT_FOUND_IN_INVENTORY_VNF_ID = "VF Module Not Found In Inventory, VnfId: ";

    private AAIResourcesClient aaiResourcesClient;

    private AAIDSLQueryClient aaiDslQueryClient;

    private static final Logger logger = LoggerFactory.getLogger(AAIDataRetrieval.class);

    public ServiceInstance getServiceInstance(String serviceInstanceId) {
        return this.getAaiResourcesClient()
                .get(ServiceInstance.class,
                        AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId)))
                .orElseGet(() -> {
                    logger.debug("No Service Instance found in A&AI ServiceInstanceId: {}", serviceInstanceId);
                    return null;
                });
    }

    public VfModule getAAIVfModule(String vnfId, String vfModuleId) {
        return this.getAaiResourcesClient()
                .get(VfModule.class,
                        AAIUriFactory.createResourceUri(
                                AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId)))
                .orElseGet(() -> {
                    logger.debug("No Vf Module found in A&AI VnfId: {}" + ", VfModuleId: {}", vnfId, vfModuleId);
                    return null;
                });
    }

    public GenericVnf getGenericVnf(String vnfId) {
        return this.getAaiResourcesClient()
                .get(GenericVnf.class,
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)))
                .orElseGet(() -> {
                    logger.debug("No Generic VNF found in A&AI VnfId: {}", vnfId);
                    return null;
                });
    }

    public VolumeGroup getVolumeGroup(String vnfId, String volumeGroupId) throws AAIEntityNotFound {
        AAIResultWrapper wrapper = this.getAaiResourcesClient()
                .get(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
                        .relatedTo(Types.VOLUME_GROUP.getFragment(volumeGroupId)));
        Optional<VolumeGroup> volume = wrapper.asBean(VolumeGroup.class);
        if (volume.isPresent()) {
            return volume.get();
        } else {
            logger.debug("No VolumeGroup in A&AI found: {}", vnfId);
            return null;
        }
    }

    public L3Network getNetwork(String networkId) {
        return this.getAaiResourcesClient()
                .get(L3Network.class,
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId)))
                .orElseGet(() -> {
                    logger.debug("No Network found in A&AI NetworkId: {}", networkId);
                    return null;
                });
    }


    public boolean isVolumeGroupRelatedToVFModule(CloudConfiguration cloudConfig, String volumeGroupId) {
        return this.getAaiResourcesClient()
                .exists(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion(cloudConfig.getCloudOwner(), cloudConfig.getLcpCloudRegionId())
                        .volumeGroup(volumeGroupId)).relatedTo(Types.VF_MODULES.getFragment()));
    }

    public boolean isVnfRelatedToVolumes(String vnfId) {
        return this.getAaiResourcesClient()
                .exists(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
                        .relatedTo(Types.VOLUME_GROUPS.getFragment()));
    }

    public boolean isNetworkRelatedToModules(String networkId) {
        return this.getAaiResourcesClient()
                .exists(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId))
                        .relatedTo(Types.VF_MODULES.getFragment()));
    }

    public boolean isServiceRelatedToNetworks(String serviceInstanceId) {
        return this.getAaiResourcesClient()
                .exists(AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))
                        .relatedTo(Types.L3_NETWORKS.getFragment()));
    }

    public boolean isServiceRelatedToGenericVnf(String serviceInstanceId) {
        return this.getAaiResourcesClient()
                .exists(AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))
                        .relatedTo(Types.GENERIC_VNFS.getFragment()));
    }

    public boolean isServiceRelatedToConfiguration(String serviceInstanceId) {
        return this.getAaiResourcesClient()
                .exists(AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))
                        .relatedTo(Types.CONFIGURATIONS.getFragment()));
    }

    public Service getService(String serviceId) {
        return this.getAaiResourcesClient()
                .get(Service.class,
                        AAIUriFactory
                                .createResourceUri(AAIFluentTypeBuilder.serviceDesignAndCreation().service(serviceId)))
                .orElseGet(() -> {
                    logger.debug("No Service found in A&AI ServiceId: {}", serviceId);
                    return null;
                });
    }

    public Tenant getTenant(String cloudOwner, String cloudRegion, String tenantId) {
        return this.getAaiResourcesClient().get(Tenant.class, AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(cloudOwner, cloudRegion).tenant(tenantId)))
                .orElseGet(() -> {
                    logger.debug("No Tenant found in A&AI TenantId: {}", tenantId);
                    return null;
                });
    }

    public List<LInterface> getLinterfacesOfVnf(String vnfId) {
        DSLStartNode startNode = new DSLStartNode(Types.GENERIC_VNF, __.key("vnf-id", vnfId));
        DSLQueryBuilder<Start, Node> builder =
                TraversalBuilder.fragment(startNode).to(__.node(Types.VSERVER).to(__.node(Types.L_INTERFACE).output()));
        List<LInterface> linterfaces =
                getAAIDSLQueryClient().querySingleResource(new DSLQuery(builder.build()), LInterface.class);
        return linterfaces;
    }

    private AAIDSLQueryClient getAAIDSLQueryClient() {
        if (aaiDslQueryClient == null) {
            aaiDslQueryClient = new AAIDSLQueryClient();
        }
        return aaiDslQueryClient;
    }

    protected AAIResourcesClient getAaiResourcesClient() {
        if (aaiResourcesClient == null) {
            aaiResourcesClient = new AAIResourcesClient();
        }
        return aaiResourcesClient;
    }

    public boolean isVnfRelatedToVFModule(String vnfId) {
        return !getVfModulesOfVnf(vnfId).isEmpty();
    }

    public List<VfModule> getVfModulesOfVnf(String vnfId) {
        List<VfModule> vfModuleList = new ArrayList<VfModule>();
        AAIPluralResourceUri uri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModules());
        Optional<VfModules> vfModules = getAaiResourcesClient().get(VfModules.class, uri);
        if (!vfModules.isPresent() || vfModules.get().getVfModule().isEmpty()) {
            logger.debug("No VfModules attached to Vnf in AAI : {}", vnfId);
        } else {
            vfModuleList = vfModules.get().getVfModule();
        }
        return vfModuleList;
    }

    public Optional<String> getVfModuleIdsByVnfId(String vnfId) {
        List<VfModule> vfModulesList = getVfModulesOfVnf(vnfId);
        if (!vfModulesList.isEmpty()) {
            return Optional.of(vfModulesList.stream().map(item -> item.getVfModuleId()).collect(Collectors.toList())
                    .stream().sorted().collect(Collectors.joining(",")));
        } else {
            return Optional.empty();
        }
    }

    public List<VolumeGroup> getVolumeGroupsOfVnf(String vnfId) {
        List<VolumeGroup> volumeGroupList = new ArrayList<VolumeGroup>();
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
                .relatedTo(Types.VOLUME_GROUPS.getFragment());
        Optional<VolumeGroups> volumeGroups = getAaiResourcesClient().get(VolumeGroups.class, uri);
        if (!volumeGroups.isPresent() || volumeGroups.get().getVolumeGroup().isEmpty()) {
            logger.debug("No VolumeGroups attached to Vnf in AAI : {}", vnfId);
        } else {
            volumeGroupList = volumeGroups.get().getVolumeGroup();
        }
        return volumeGroupList;
    }

    public Optional<String> getVolumeGroupIdsByVnfId(String vnfId) {
        List<VolumeGroup> volumeGroupList = getVolumeGroupsOfVnf(vnfId);
        if (!volumeGroupList.isEmpty()) {
            return Optional.of(volumeGroupList.stream().map(item -> item.getVolumeGroupId())
                    .collect(Collectors.toList()).stream().sorted().collect(Collectors.joining(",")));
        } else {
            return Optional.empty();
        }
    }
}
