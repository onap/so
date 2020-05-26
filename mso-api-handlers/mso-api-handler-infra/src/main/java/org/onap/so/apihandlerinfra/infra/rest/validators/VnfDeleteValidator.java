package org.onap.so.apihandlerinfra.infra.rest.validators;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.apihandlerinfra.infra.rest.AAIDataRetrieval;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VnfDeleteValidator implements RequestValidator {

    @Autowired
    AAIDataRetrieval aaiDataRetrieval;

    @Override
    public boolean shouldRunFor(String requestUri, ServiceInstancesRequest request, Actions action) {
        return Pattern.compile("[Vv][5-8]/serviceInstances/[^/]+/vnfs/[^/]+").matcher(requestUri).matches()
                && action.equals(Action.deleteInstance);
    }

    @Override
    public Optional<String> validate(Map<String, String> instanceIdMap, ServiceInstancesRequest request,
            Map<String, String> queryParams) {
        final Optional<String> volumeGroupIds =
                aaiDataRetrieval.getVolumeGroupIdsByVnfId(instanceIdMap.get("vnfInstanceId"));
        final Optional<String> vfModuleIds = aaiDataRetrieval.getVfModuleIdsByVnfId(instanceIdMap.get("vnfInstanceId"));

        if (volumeGroupIds.isPresent()) {
            return Optional.of(String.format("Cannot delete vnf it is still related to existing volume group Ids - %s",
                    volumeGroupIds.get()));
        } else if (vfModuleIds.isPresent()) {
            return Optional.of(String.format("Cannot delete vnf it is still related to existing vfModule Ids - %s",
                    vfModuleIds.get()));
        } else {
            return Optional.empty();
        }
    }

}
