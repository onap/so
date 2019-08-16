package org.onap.so.apihandlerinfra.infra.rest;

import java.util.Optional;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.so.apihandlerinfra.infra.rest.exception.AAIEntityNotFound;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AAIDataRetrieval {

    private static final String VF_MODULE_NOT_FOUND_IN_INVENTORY_VNF_ID = "VF Module Not Found In Inventory, VnfId: ";

    private AAIResourcesClient aaiResourcesClient;

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

    protected AAIResourcesClient getAaiResourcesClient() {
        if (aaiResourcesClient == null) {
            aaiResourcesClient = new AAIResourcesClient();
        }
        return aaiResourcesClient;
    }

}
