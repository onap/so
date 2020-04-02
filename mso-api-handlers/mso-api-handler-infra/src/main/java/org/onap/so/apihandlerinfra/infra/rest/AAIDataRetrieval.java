package org.onap.so.apihandlerinfra.infra.rest;

import java.util.List;
import java.util.Optional;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.Service;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.so.apihandlerinfra.infra.rest.exception.AAIEntityNotFound;
import org.onap.so.client.aai.AAIDSLQueryClient;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.entities.DSLQuery;
import org.onap.so.client.graphinventory.entities.DSLQueryBuilder;
import org.onap.so.client.graphinventory.entities.DSLStartNode;
import org.onap.so.client.graphinventory.entities.Node;
import org.onap.so.client.graphinventory.entities.Start;
import org.onap.so.client.graphinventory.entities.TraversalBuilder;
import org.onap.so.client.graphinventory.entities.__;
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
                        AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId))
                .orElseGet(() -> {
                    logger.debug("No Service Instance found in A&AI ServiceInstanceId: {}", serviceInstanceId);
                    return null;
                });
    }

    public VfModule getAAIVfModule(String vnfId, String vfModuleId) {
        return this.getAaiResourcesClient()
                .get(VfModule.class, AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnfId, vfModuleId))
                .orElseGet(() -> {
                    logger.debug("No Vf Module found in A&AI VnfId: {}" + ", VfModuleId: {}", vnfId, vfModuleId);
                    return null;
                });
    }

    public GenericVnf getGenericVnf(String vnfId) {
        return this.getAaiResourcesClient()
                .get(GenericVnf.class, AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId))
                .orElseGet(() -> {
                    logger.debug("No Generic VNF found in A&AI VnfId: {}", vnfId);
                    return null;
                });
    }

    public VolumeGroup getVolumeGroup(String vnfId, String volumeGroupId) throws AAIEntityNotFound {
        AAIResultWrapper wrapper =
                this.getAaiResourcesClient().get(AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId)
                        .relatedTo(AAIObjectType.VOLUME_GROUP, volumeGroupId));
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
                .get(L3Network.class, AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, networkId))
                .orElseGet(() -> {
                    logger.debug("No Network found in A&AI NetworkId: {}", networkId);
                    return null;
                });
    }


    public boolean isVolumeGroupRelatedToVFModule(String volumeGroupId) {
        return this.getAaiResourcesClient().exists(AAIUriFactory
                .createResourceUri(AAIObjectType.VOLUME_GROUP, volumeGroupId).relatedTo(AAIObjectPlurals.VF_MODULE));
    }

    public boolean isVnfRelatedToVolumes(String vnfId) {
        return this.getAaiResourcesClient().exists(AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId)
                .relatedTo(AAIObjectPlurals.VOLUME_GROUP));
    }

    public boolean isNetworkRelatedToModules(String networkId) {
        return this.getAaiResourcesClient().exists(AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, networkId)
                .relatedTo(AAIObjectPlurals.VF_MODULE));
    }

    public boolean isServiceRelatedToNetworks(String serviceInstanceId) {
        return this.getAaiResourcesClient()
                .exists(AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)
                        .relatedTo(AAIObjectPlurals.L3_NETWORK));
    }

    public boolean isServiceRelatedToGenericVnf(String serviceInstanceId) {
        return this.getAaiResourcesClient()
                .exists(AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)
                        .relatedTo(AAIObjectPlurals.GENERIC_VNF));
    }

    public boolean isServiceRelatedToConfiguration(String serviceInstanceId) {
        return this.getAaiResourcesClient()
                .exists(AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)
                        .relatedTo(AAIObjectPlurals.CONFIGURATION));
    }

    public Service getService(String serviceId) {
        return this.getAaiResourcesClient()
                .get(Service.class, AAIUriFactory.createResourceUri(AAIObjectType.SERVICE, serviceId)).orElseGet(() -> {
                    logger.debug("No Service found in A&AI ServiceId: {}", serviceId);
                    return null;
                });
    }

    public Tenant getTenant(String cloudOwner, String cloudRegion, String tenantId) {
        return this.getAaiResourcesClient()
                .get(Tenant.class,
                        AAIUriFactory.createResourceUri(AAIObjectType.TENANT, cloudOwner, cloudRegion, tenantId))
                .orElseGet(() -> {
                    logger.debug("No Tenant found in A&AI TenantId: {}", tenantId);
                    return null;
                });
    }

    public List<LInterface> getLinterfacesOfVnf(String vnfId) {
        DSLStartNode startNode = new DSLStartNode(AAIObjectType.GENERIC_VNF, __.key("generic-vnf-id", vnfId));
        DSLQueryBuilder<Start, Node> builder = TraversalBuilder.fragment(startNode)
                .to(__.node(AAIObjectType.VSERVER).to(__.node(AAIObjectType.L_INTERFACE).output()));
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

}
