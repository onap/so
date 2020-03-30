package org.onap.so.apihandlerinfra.infra.rest.validators;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.apihandlerinfra.infra.rest.AAIDataRetrieval;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.beans.factory.annotation.Autowired;

public class VolumeGroupDeleteValidator implements RequestValidator {

    @Autowired
    AAIDataRetrieval aaiDataRetrieval;

    @Override
    public boolean shouldRunFor(String requestUri, ServiceInstancesRequest request, Actions action) {
        return Pattern.compile("[Vv][5-8]/serviceInstances/[^/]+/volumeGroups/[^/]+").matcher(requestUri).matches()
                && action.equals(Action.deleteInstance);
    }

    @Override
    public Optional<String> validate(Map<String, String> instanceIdMap, ServiceInstancesRequest request,
            Map<String, String> queryParams) {
        if (aaiDataRetrieval.isVolumeGroupRelatedToVFModule(instanceIdMap.get("volumeGroupInstanceId"))) {
            return Optional.of("Cannot delete volume group it is related to existing vf-modules");
        } else {
            return Optional.empty();
        }
    }

}
