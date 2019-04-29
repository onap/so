package org.onap.svnfm.simulator.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources.TypeEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsResource;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs.ChangeTypeEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201.InstantiationStateEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfoResourceHandle;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfoVnfcResourceInfo;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.model.VnfOperation;
import org.onap.svnfm.simulator.model.Vnfds;
import org.onap.svnfm.simulator.repository.VnfOperationRepository;

public class TerminateOperationProgressor extends OperationProgressor {

    public TerminateOperationProgressor(final VnfOperation operation, final SvnfmService svnfmService,
            final VnfOperationRepository vnfOperationRepository, final ApplicationConfig applicationConfig,
            final Vnfds vnfds, final SubscriptionService subscriptionService) {
        super(operation, svnfmService, vnfOperationRepository, applicationConfig, vnfds, subscriptionService);
    }

    @Override
    protected List<GrantsAddResources> getAddResources(final String vnfdId) {
        return Collections.emptyList();
    }

    @Override
    protected List<GrantsAddResources> getRemoveResources(final String vnfdId) {
        final List<GrantsAddResources> resources = new ArrayList<>();

        final org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201 vnf =
                svnfmService.getVnf(operation.getVnfInstanceId());
        for (final InlineResponse201InstantiatedVnfInfoVnfcResourceInfo vnfc : vnf.getInstantiatedVnfInfo()
                .getVnfcResourceInfo()) {
            final GrantsAddResources addResource = new GrantsAddResources();
            addResource.setId(UUID.randomUUID().toString());
            addResource.setType(TypeEnum.COMPUTE);
            addResource.setVduId(vnfc.getVduId());
            final GrantsResource resource = new GrantsResource();

            final InlineResponse201InstantiatedVnfInfoResourceHandle computeResource = vnfc.getComputeResource();
            resource.setResourceId(computeResource.getResourceId());
            resource.setVimConnectionId(computeResource.getVimConnectionId());
            resource.setVimLevelResourceType(computeResource.getVimLevelResourceType());
            addResource.setResource(resource);
            resources.add(addResource);

        }
        return resources;
    }

    @Override
    protected List<InlineResponse201InstantiatedVnfInfoVnfcResourceInfo> handleGrantResponse(
            final InlineResponse201 grantResponse) {
        final List<InlineResponse201InstantiatedVnfInfoVnfcResourceInfo> vnfcs =
                svnfmService.getVnf(operation.getVnfInstanceId()).getInstantiatedVnfInfo().getVnfcResourceInfo();
        svnfmService.updateVnf(InstantiationStateEnum.NOT_INSTANTIATED, null, operation.getVnfInstanceId(), null);
        return vnfcs;
    }

    @Override
    protected ChangeTypeEnum getVnfcChangeType() {
        return ChangeTypeEnum.REMOVED;
    }



}
