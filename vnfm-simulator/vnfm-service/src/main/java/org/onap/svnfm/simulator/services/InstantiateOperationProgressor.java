package org.onap.svnfm.simulator.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources.TypeEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201AddResources;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201VimConnections;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs.ChangeTypeEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201.InstantiationStateEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfoResourceHandle;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfoVnfcResourceInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201VimConnectionInfo;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.model.VnfOperation;
import org.onap.svnfm.simulator.model.Vnfds;
import org.onap.svnfm.simulator.model.Vnfds.Vnfc;
import org.onap.svnfm.simulator.model.Vnfds.Vnfd;
import org.onap.svnfm.simulator.repository.VnfOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstantiateOperationProgressor extends OperationProgressor {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateOperationProgressor.class);

    public InstantiateOperationProgressor(final VnfOperation operation, final SvnfmService svnfmService,
            final VnfOperationRepository vnfOperationRepository, final ApplicationConfig applicationConfig,
            final Vnfds vnfds, final SubscriptionService subscriptionService) {
        super(operation, svnfmService, vnfOperationRepository, applicationConfig, vnfds, subscriptionService);
    }

    @Override
    protected List<GrantsAddResources> getAddResources(final String vnfdId) {
        final List<GrantsAddResources> resources = new ArrayList<>();

        for (final Vnfd vnfd : vnfds.getVnfdList()) {
            if (vnfd.getVnfdId().equals(vnfdId)) {
                for (final Vnfc vnfc : vnfd.getVnfcList()) {
                    final GrantsAddResources addResource = new GrantsAddResources();
                    vnfc.setGrantResourceId(UUID.randomUUID().toString());
                    addResource.setId(vnfc.getGrantResourceId());
                    addResource.setType(TypeEnum.fromValue(vnfc.getType()));
                    addResource.setResourceTemplateId(vnfc.getResourceTemplateId());
                    addResource.setVduId(vnfc.getVduId());
                    resources.add(addResource);
                }
            }
        }
        return resources;
    }

    @Override
    protected List<GrantsAddResources> getRemoveResources(final String vnfdId) {
        return Collections.emptyList();
    }

    @Override
    protected List<InlineResponse201InstantiatedVnfInfoVnfcResourceInfo> handleGrantResponse(
            final InlineResponse201 grantResponse) {
        final InlineResponse201InstantiatedVnfInfo instantiatedVnfInfo = createInstantiatedVnfInfo(grantResponse);
        svnfmService.updateVnf(InstantiationStateEnum.INSTANTIATED, instantiatedVnfInfo, operation.getVnfInstanceId(),
                getVimConnections(grantResponse));
        return instantiatedVnfInfo.getVnfcResourceInfo();
    }

    private InlineResponse201InstantiatedVnfInfo createInstantiatedVnfInfo(final InlineResponse201 grantResponse) {
        final InlineResponse201InstantiatedVnfInfo instantiatedVnfInfo = new InlineResponse201InstantiatedVnfInfo();

        final Map<String, String> mapOfGrantResourceIdToVimConnectionId = new HashMap<>();
        for (final InlineResponse201AddResources addResource : grantResponse.getAddResources()) {
            mapOfGrantResourceIdToVimConnectionId.put(addResource.getResourceDefinitionId(),
                    addResource.getVimConnectionId());
        }
        LOGGER.info("VIM connections in grant response: {}", mapOfGrantResourceIdToVimConnectionId);

        for (final Vnfd vnfd : vnfds.getVnfdList()) {
            if (vnfd.getVnfdId().equals(svnfmService.getVnf(operation.getVnfInstanceId()).getVnfdId())) {
                for (final Vnfc vnfc : vnfd.getVnfcList()) {
                    final InlineResponse201InstantiatedVnfInfoVnfcResourceInfo vnfcResourceInfoItem =
                            new InlineResponse201InstantiatedVnfInfoVnfcResourceInfo();
                    vnfcResourceInfoItem.setId(vnfc.getVnfcId());
                    vnfcResourceInfoItem.setVduId(vnfc.getVduId());
                    final InlineResponse201InstantiatedVnfInfoResourceHandle computeResource =
                            new InlineResponse201InstantiatedVnfInfoResourceHandle();
                    computeResource.setResourceId(UUID.randomUUID().toString());
                    LOGGER.info("Checking for VIM connection id for : {}", vnfc.getGrantResourceId());
                    computeResource
                            .setVimConnectionId(mapOfGrantResourceIdToVimConnectionId.get(vnfc.getGrantResourceId()));

                    computeResource.setVimLevelResourceType("OS::Nova::Server");
                    vnfcResourceInfoItem.setComputeResource(computeResource);
                    instantiatedVnfInfo.addVnfcResourceInfoItem(vnfcResourceInfoItem);
                }
            }
        }

        return instantiatedVnfInfo;
    }


    private List<InlineResponse201VimConnectionInfo> getVimConnections(final InlineResponse201 grantResponse) {
        final List<InlineResponse201VimConnectionInfo> vimConnectionInfo = new ArrayList<>();
        for (final InlineResponse201VimConnections vimConnection : grantResponse.getVimConnections()) {
            final ModelMapper modelMapper = new ModelMapper();
            vimConnectionInfo.add(modelMapper.map(vimConnection, InlineResponse201VimConnectionInfo.class));
        }
        return vimConnectionInfo;
    }

    @Override
    protected ChangeTypeEnum getVnfcChangeType() {
        return ChangeTypeEnum.ADDED;
    }

}
